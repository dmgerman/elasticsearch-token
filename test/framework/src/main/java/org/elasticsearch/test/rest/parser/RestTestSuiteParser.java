begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|TestSection
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

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|ByteBuffer
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|channels
operator|.
name|FileChannel
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|file
operator|.
name|Files
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|file
operator|.
name|Path
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|file
operator|.
name|StandardOpenOption
import|;
end_import

begin_comment
comment|/**  * Parser for a complete test suite (yaml file)  */
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
name|api
parameter_list|,
name|Path
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
name|Files
operator|.
name|isRegularFile
argument_list|(
name|file
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|file
operator|.
name|toAbsolutePath
argument_list|()
operator|+
literal|" is not a file"
argument_list|)
throw|;
block|}
name|String
name|filename
init|=
name|file
operator|.
name|getFileName
argument_list|()
operator|.
name|toString
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
comment|//our yaml parser seems to be too tolerant. Each yaml suite must end with \n, otherwise clients tests might break.
try|try
init|(
name|FileChannel
name|channel
init|=
name|FileChannel
operator|.
name|open
argument_list|(
name|file
argument_list|,
name|StandardOpenOption
operator|.
name|READ
argument_list|)
init|)
block|{
name|ByteBuffer
name|bb
init|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
operator|new
name|byte
index|[
literal|1
index|]
argument_list|)
decl_stmt|;
name|channel
operator|.
name|read
argument_list|(
name|bb
argument_list|,
name|channel
operator|.
name|size
argument_list|()
operator|-
literal|1
argument_list|)
expr_stmt|;
if|if
condition|(
name|bb
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|!=
literal|10
condition|)
block|{
throw|throw
operator|new
name|RestTestParseException
argument_list|(
literal|"test suite ["
operator|+
name|api
operator|+
literal|"/"
operator|+
name|filename
operator|+
literal|"] doesn't end with line feed (\\n)"
argument_list|)
throw|;
block|}
block|}
try|try
init|(
name|XContentParser
name|parser
init|=
name|YamlXContent
operator|.
name|yamlXContent
operator|.
name|createParser
argument_list|(
name|Files
operator|.
name|newInputStream
argument_list|(
name|file
argument_list|)
argument_list|)
init|)
block|{
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
argument_list|)
decl_stmt|;
return|return
name|parse
argument_list|(
name|testParseContext
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RestTestParseException
argument_list|(
literal|"Error parsing "
operator|+
name|api
operator|+
literal|"/"
operator|+
name|filename
argument_list|,
name|e
argument_list|)
throw|;
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
operator|:
literal|"expected token to be START_OBJECT but was "
operator|+
name|parser
operator|.
name|currentToken
argument_list|()
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
name|TestSection
name|testSection
init|=
name|parseContext
operator|.
name|parseTestSection
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|restTestSuite
operator|.
name|addTestSection
argument_list|(
name|testSection
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|RestTestParseException
argument_list|(
literal|"duplicate test section ["
operator|+
name|testSection
operator|.
name|getName
argument_list|()
operator|+
literal|"] found in ["
operator|+
name|restTestSuite
operator|.
name|getPath
argument_list|()
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
return|return
name|restTestSuite
return|;
block|}
block|}
end_class

end_unit

