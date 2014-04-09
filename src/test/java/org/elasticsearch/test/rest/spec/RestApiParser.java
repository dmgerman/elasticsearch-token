begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.test.rest.spec
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|rest
operator|.
name|spec
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
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_comment
comment|/**  * Parser for a REST api spec (single json file)  */
end_comment

begin_class
DECL|class|RestApiParser
specifier|public
class|class
name|RestApiParser
block|{
DECL|method|parse
specifier|public
name|RestApi
name|parse
parameter_list|(
name|XContentParser
name|parser
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
while|while
condition|(
name|parser
operator|.
name|nextToken
argument_list|()
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|FIELD_NAME
condition|)
block|{
comment|//move to first field name
block|}
name|RestApi
name|restApi
init|=
operator|new
name|RestApi
argument_list|(
name|parser
operator|.
name|currentName
argument_list|()
argument_list|)
decl_stmt|;
name|int
name|level
init|=
operator|-
literal|1
decl_stmt|;
while|while
condition|(
name|parser
operator|.
name|nextToken
argument_list|()
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|END_OBJECT
operator|||
name|level
operator|>=
literal|0
condition|)
block|{
if|if
condition|(
name|parser
operator|.
name|currentToken
argument_list|()
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|FIELD_NAME
condition|)
block|{
if|if
condition|(
literal|"methods"
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
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
while|while
condition|(
name|parser
operator|.
name|nextToken
argument_list|()
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|VALUE_STRING
condition|)
block|{
name|restApi
operator|.
name|addMethod
argument_list|(
name|parser
operator|.
name|text
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
literal|"url"
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
name|String
name|currentFieldName
init|=
literal|"url"
decl_stmt|;
name|int
name|innerLevel
init|=
operator|-
literal|1
decl_stmt|;
while|while
condition|(
name|parser
operator|.
name|nextToken
argument_list|()
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|END_OBJECT
operator|||
name|innerLevel
operator|>=
literal|0
condition|)
block|{
if|if
condition|(
name|parser
operator|.
name|currentToken
argument_list|()
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|FIELD_NAME
condition|)
block|{
name|currentFieldName
operator|=
name|parser
operator|.
name|currentName
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|parser
operator|.
name|currentToken
argument_list|()
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|START_ARRAY
operator|&&
literal|"paths"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
while|while
condition|(
name|parser
operator|.
name|nextToken
argument_list|()
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|VALUE_STRING
condition|)
block|{
name|restApi
operator|.
name|addPath
argument_list|(
name|parser
operator|.
name|text
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
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
operator|&&
literal|"parts"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
while|while
condition|(
name|parser
operator|.
name|nextToken
argument_list|()
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|FIELD_NAME
condition|)
block|{
name|restApi
operator|.
name|addPathPart
argument_list|(
name|parser
operator|.
name|currentName
argument_list|()
argument_list|)
expr_stmt|;
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
if|if
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
name|START_OBJECT
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Expected parts field in rest api definition to contain an object"
argument_list|)
throw|;
block|}
name|parser
operator|.
name|skipChildren
argument_list|()
expr_stmt|;
block|}
block|}
if|if
condition|(
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
operator|&&
literal|"params"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
while|while
condition|(
name|parser
operator|.
name|nextToken
argument_list|()
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|FIELD_NAME
condition|)
block|{
name|restApi
operator|.
name|addParam
argument_list|(
name|parser
operator|.
name|currentName
argument_list|()
argument_list|)
expr_stmt|;
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
if|if
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
name|START_OBJECT
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Expected params field in rest api definition to contain an object"
argument_list|)
throw|;
block|}
name|parser
operator|.
name|skipChildren
argument_list|()
expr_stmt|;
block|}
block|}
if|if
condition|(
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
condition|)
block|{
name|innerLevel
operator|++
expr_stmt|;
block|}
if|if
condition|(
name|parser
operator|.
name|currentToken
argument_list|()
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|END_OBJECT
condition|)
block|{
name|innerLevel
operator|--
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
literal|"body"
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
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
if|if
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
name|VALUE_NULL
condition|)
block|{
name|boolean
name|requiredFound
init|=
literal|false
decl_stmt|;
while|while
condition|(
name|parser
operator|.
name|nextToken
argument_list|()
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|END_OBJECT
condition|)
block|{
if|if
condition|(
name|parser
operator|.
name|currentToken
argument_list|()
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|FIELD_NAME
condition|)
block|{
if|if
condition|(
literal|"required"
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
name|requiredFound
operator|=
literal|true
expr_stmt|;
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
if|if
condition|(
name|parser
operator|.
name|booleanValue
argument_list|()
condition|)
block|{
name|restApi
operator|.
name|setBodyRequired
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|restApi
operator|.
name|setBodyOptional
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
if|if
condition|(
operator|!
name|requiredFound
condition|)
block|{
name|restApi
operator|.
name|setBodyOptional
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
if|if
condition|(
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
condition|)
block|{
name|level
operator|++
expr_stmt|;
block|}
if|if
condition|(
name|parser
operator|.
name|currentToken
argument_list|()
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|END_OBJECT
condition|)
block|{
name|level
operator|--
expr_stmt|;
block|}
block|}
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
name|END_OBJECT
operator|:
literal|"Expected [END_OBJECT] but was ["
operator|+
name|parser
operator|.
name|currentToken
argument_list|()
operator|+
literal|"]"
assert|;
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
return|return
name|restApi
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
block|}
end_class

end_unit

