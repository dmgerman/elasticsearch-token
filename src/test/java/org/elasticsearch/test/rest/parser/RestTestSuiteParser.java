begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements. See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the  * License for the specific language governing permissions and limitations under  * the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.test.rest.parser
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|rest
operator|.
name|parser
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|xcontent
operator|.
name|XContentParser
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|xcontent
operator|.
name|yaml
operator|.
name|YamlXContent
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|rest
operator|.
name|section
operator|.
name|RestTestSuite
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|File
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|FileInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_comment
comment|/**  * Parser for a complete test suite (yaml file)  *  * Depending on the elasticsearch version the tests are going to run against, a whole test suite might need to get skipped  * In that case the relevant test sections parsing is entirely skipped  */
end_comment

begin_class
DECL|class|RestTestSuiteParser
specifier|public
class|class
name|RestTestSuiteParser
implements|implements
name|RestTestFragmentParser
argument_list|<
name|RestTestSuite
argument_list|>
block|{
DECL|method|parse
specifier|public
name|RestTestSuite
name|parse
parameter_list|(
name|String
name|currentVersion
parameter_list|,
name|String
name|api
parameter_list|,
name|File
name|file
parameter_list|)
throws|throws
name|IOException
throws|,
name|RestTestParseException
block|{
if|if
condition|(
operator|!
name|file
operator|.
name|isFile
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|file
operator|.
name|getAbsolutePath
argument_list|()
operator|+
literal|" is not a file"
argument_list|)
throw|;
block|}
name|XContentParser
name|parser
init|=
name|YamlXContent
operator|.
name|yamlXContent
operator|.
name|createParser
argument_list|(
operator|new
name|FileInputStream
argument_list|(
name|file
argument_list|)
argument_list|)
decl_stmt|;
try|try
block|{
name|String
name|filename
init|=
name|file
operator|.
name|getName
argument_list|()
decl_stmt|;
comment|//remove the file extension
name|int
name|i
init|=
name|filename
operator|.
name|lastIndexOf
argument_list|(
literal|'.'
argument_list|)
decl_stmt|;
if|if
condition|(
name|i
operator|>
literal|0
condition|)
block|{
name|filename
operator|=
name|filename
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|i
argument_list|)
expr_stmt|;
block|}
name|RestTestSuiteParseContext
name|testParseContext
init|=
operator|new
name|RestTestSuiteParseContext
argument_list|(
name|api
argument_list|,
name|filename
argument_list|,
name|parser
argument_list|,
name|currentVersion
argument_list|)
decl_stmt|;
return|return
name|parse
argument_list|(
name|testParseContext
argument_list|)
return|;
block|}
finally|finally
block|{
name|parser
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|parse
specifier|public
name|RestTestSuite
name|parse
parameter_list|(
name|RestTestSuiteParseContext
name|parseContext
parameter_list|)
throws|throws
name|IOException
throws|,
name|RestTestParseException
block|{
name|XContentParser
name|parser
init|=
name|parseContext
operator|.
name|parser
argument_list|()
decl_stmt|;
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
assert|assert
name|parser
operator|.
name|currentToken
argument_list|()
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|START_OBJECT
assert|;
name|RestTestSuite
name|restTestSuite
init|=
operator|new
name|RestTestSuite
argument_list|(
name|parseContext
operator|.
name|getApi
argument_list|()
argument_list|,
name|parseContext
operator|.
name|getSuiteName
argument_list|()
argument_list|)
decl_stmt|;
name|restTestSuite
operator|.
name|setSetupSection
argument_list|(
name|parseContext
operator|.
name|parseSetupSection
argument_list|()
argument_list|)
expr_stmt|;
name|boolean
name|skip
init|=
name|restTestSuite
operator|.
name|getSetupSection
argument_list|()
operator|.
name|getSkipSection
argument_list|()
operator|.
name|skipVersion
argument_list|(
name|parseContext
operator|.
name|getCurrentVersion
argument_list|()
argument_list|)
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
comment|//the "---" section separator is not understood by the yaml parser. null is returned, same as when the parser is closed
comment|//we need to somehow distinguish between a null in the middle of a test ("---")
comment|// and a null at the end of the file (at least two consecutive null tokens)
if|if
condition|(
name|parser
operator|.
name|currentToken
argument_list|()
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|parser
operator|.
name|nextToken
argument_list|()
operator|==
literal|null
condition|)
block|{
break|break;
block|}
block|}
if|if
condition|(
name|skip
condition|)
block|{
comment|//if there was a skip section, there was a setup section as well, which means that we are sure
comment|// the current token is at the beginning of a new object
assert|assert
name|parser
operator|.
name|currentToken
argument_list|()
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|START_OBJECT
assert|;
comment|//we need to be at the beginning of an object to be able to skip children
name|parser
operator|.
name|skipChildren
argument_list|()
expr_stmt|;
comment|//after skipChildren we are at the end of the skipped object, need to move on
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|restTestSuite
operator|.
name|addTestSection
argument_list|(
name|parseContext
operator|.
name|parseTestSection
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|restTestSuite
return|;
block|}
block|}
end_class

end_unit

