begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.painless
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|painless
package|;
end_package

begin_comment
comment|/**  * A set of methods for non-native boxing and non-native  * exact math operations used at both compile-time and runtime.  */
end_comment

begin_class
DECL|class|Utility
specifier|public
class|class
name|Utility
block|{
DECL|method|charToString
specifier|public
specifier|static
name|String
name|charToString
parameter_list|(
specifier|final
name|char
name|value
parameter_list|)
block|{
return|return
name|String
operator|.
name|valueOf
argument_list|(
name|value
argument_list|)
return|;
block|}
DECL|method|StringTochar
specifier|public
specifier|static
name|char
name|StringTochar
parameter_list|(
specifier|final
name|String
name|value
parameter_list|)
block|{
if|if
condition|(
name|value
operator|.
name|length
argument_list|()
operator|!=
literal|1
condition|)
block|{
throw|throw
operator|new
name|ClassCastException
argument_list|(
literal|"Cannot cast [String] with length greater than one to [char]."
argument_list|)
throw|;
block|}
return|return
name|value
operator|.
name|charAt
argument_list|(
literal|0
argument_list|)
return|;
block|}
DECL|method|Utility
specifier|private
name|Utility
parameter_list|()
block|{}
block|}
end_class

end_unit

