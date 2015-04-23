begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.script
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|script
package|;
end_package

begin_comment
comment|/**  * CompiledScript holds all the parameters necessary to execute a previously compiled script.  */
end_comment

begin_class
DECL|class|CompiledScript
specifier|public
class|class
name|CompiledScript
block|{
DECL|field|lang
specifier|private
specifier|final
name|String
name|lang
decl_stmt|;
DECL|field|compiled
specifier|private
specifier|final
name|Object
name|compiled
decl_stmt|;
comment|/**      * Constructor for CompiledScript.      * @param lang The language of the script to be executed.      * @param compiled The compiled script Object that is executable.      */
DECL|method|CompiledScript
specifier|public
name|CompiledScript
parameter_list|(
name|String
name|lang
parameter_list|,
name|Object
name|compiled
parameter_list|)
block|{
name|this
operator|.
name|lang
operator|=
name|lang
expr_stmt|;
name|this
operator|.
name|compiled
operator|=
name|compiled
expr_stmt|;
block|}
comment|/**      * Method to get the language.      * @return The language of the script to be executed.      */
DECL|method|lang
specifier|public
name|String
name|lang
parameter_list|()
block|{
return|return
name|lang
return|;
block|}
comment|/**      * Method to get the compiled script object.      * @return The compiled script Object that is executable.      */
DECL|method|compiled
specifier|public
name|Object
name|compiled
parameter_list|()
block|{
return|return
name|compiled
return|;
block|}
block|}
end_class

end_unit

