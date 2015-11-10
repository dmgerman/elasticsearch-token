begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
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
name|settings
operator|.
name|Settings
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|EnumSet
import|;
end_import

begin_comment
comment|/**  * Matcher to use in combination with {@link ParseField} while parsing requests. Matches a {@link ParseField}  * against a field name and throw deprecation exception depending on the current value of the {@link #PARSE_STRICT} setting.  */
end_comment

begin_class
DECL|class|ParseFieldMatcher
specifier|public
class|class
name|ParseFieldMatcher
block|{
DECL|field|PARSE_STRICT
specifier|public
specifier|static
specifier|final
name|String
name|PARSE_STRICT
init|=
literal|"index.query.parse.strict"
decl_stmt|;
DECL|field|EMPTY
specifier|public
specifier|static
specifier|final
name|ParseFieldMatcher
name|EMPTY
init|=
operator|new
name|ParseFieldMatcher
argument_list|(
name|ParseField
operator|.
name|EMPTY_FLAGS
argument_list|)
decl_stmt|;
DECL|field|STRICT
specifier|public
specifier|static
specifier|final
name|ParseFieldMatcher
name|STRICT
init|=
operator|new
name|ParseFieldMatcher
argument_list|(
name|ParseField
operator|.
name|STRICT_FLAGS
argument_list|)
decl_stmt|;
DECL|field|parseFlags
specifier|private
specifier|final
name|EnumSet
argument_list|<
name|ParseField
operator|.
name|Flag
argument_list|>
name|parseFlags
decl_stmt|;
DECL|method|ParseFieldMatcher
specifier|public
name|ParseFieldMatcher
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
if|if
condition|(
name|settings
operator|.
name|getAsBoolean
argument_list|(
name|PARSE_STRICT
argument_list|,
literal|false
argument_list|)
condition|)
block|{
name|this
operator|.
name|parseFlags
operator|=
name|EnumSet
operator|.
name|of
argument_list|(
name|ParseField
operator|.
name|Flag
operator|.
name|STRICT
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|parseFlags
operator|=
name|ParseField
operator|.
name|EMPTY_FLAGS
expr_stmt|;
block|}
block|}
DECL|method|ParseFieldMatcher
specifier|public
name|ParseFieldMatcher
parameter_list|(
name|EnumSet
argument_list|<
name|ParseField
operator|.
name|Flag
argument_list|>
name|parseFlags
parameter_list|)
block|{
name|this
operator|.
name|parseFlags
operator|=
name|parseFlags
expr_stmt|;
block|}
comment|/**      * Matches a {@link ParseField} against a field name, and throws deprecation exception depending on the current      * value of the {@link #PARSE_STRICT} setting.      * @param fieldName the field name found in the request while parsing      * @param parseField the parse field that we are looking for      * @throws IllegalArgumentException whenever we are in strict mode and the request contained a deprecated field      * @return true whenever the parse field that we are looking for was found, false otherwise      */
DECL|method|match
specifier|public
name|boolean
name|match
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|ParseField
name|parseField
parameter_list|)
block|{
return|return
name|parseField
operator|.
name|match
argument_list|(
name|fieldName
argument_list|,
name|parseFlags
argument_list|)
return|;
block|}
block|}
end_class

end_unit

