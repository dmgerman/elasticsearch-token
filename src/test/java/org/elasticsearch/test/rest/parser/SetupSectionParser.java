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
name|test
operator|.
name|rest
operator|.
name|section
operator|.
name|SetupSection
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
comment|/**  * Parser for setup sections  */
end_comment

begin_class
DECL|class|SetupSectionParser
specifier|public
class|class
name|SetupSectionParser
implements|implements
name|RestTestFragmentParser
argument_list|<
name|SetupSection
argument_list|>
block|{
annotation|@
name|Override
DECL|method|parse
specifier|public
name|SetupSection
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
name|SetupSection
name|setupSection
init|=
operator|new
name|SetupSection
argument_list|()
decl_stmt|;
name|setupSection
operator|.
name|setSkipSection
argument_list|(
name|parseContext
operator|.
name|parseSkipSection
argument_list|()
argument_list|)
expr_stmt|;
name|boolean
name|skip
init|=
name|setupSection
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
name|parser
operator|.
name|currentToken
argument_list|()
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|END_ARRAY
condition|)
block|{
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
name|parseContext
operator|.
name|advanceToFieldName
argument_list|()
expr_stmt|;
if|if
condition|(
operator|!
literal|"do"
operator|.
name|equals
argument_list|(
name|parser
operator|.
name|currentName
argument_list|()
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|RestTestParseException
argument_list|(
literal|"section ["
operator|+
name|parser
operator|.
name|currentName
argument_list|()
operator|+
literal|"] not supported within setup section"
argument_list|)
throw|;
block|}
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
name|setupSection
operator|.
name|addDoSection
argument_list|(
name|parseContext
operator|.
name|parseDoSection
argument_list|()
argument_list|)
expr_stmt|;
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
block|}
block|}
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
return|return
name|setupSection
return|;
block|}
block|}
end_class

end_unit

