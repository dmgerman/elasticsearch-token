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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_comment
comment|/**  * Mode for a specific script, used for script settings.  * Defines whether a certain script or category of scripts can be executed or not, or whether it can  * only be executed by a sandboxed scripting language.  */
end_comment

begin_enum
DECL|enum|ScriptMode
enum|enum
name|ScriptMode
block|{
DECL|enum constant|ON
name|ON
argument_list|(
literal|"true"
argument_list|)
block|,
DECL|enum constant|OFF
name|OFF
argument_list|(
literal|"false"
argument_list|)
block|,
DECL|enum constant|SANDBOX
name|SANDBOX
argument_list|(
literal|"sandbox"
argument_list|)
block|;
DECL|field|mode
specifier|private
specifier|final
name|String
name|mode
decl_stmt|;
DECL|method|ScriptMode
name|ScriptMode
parameter_list|(
name|String
name|mode
parameter_list|)
block|{
name|this
operator|.
name|mode
operator|=
name|mode
expr_stmt|;
block|}
DECL|field|SCRIPT_MODES
specifier|private
specifier|static
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|ScriptMode
argument_list|>
name|SCRIPT_MODES
decl_stmt|;
static|static
block|{
name|SCRIPT_MODES
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
expr_stmt|;
for|for
control|(
name|ScriptMode
name|scriptMode
range|:
name|ScriptMode
operator|.
name|values
argument_list|()
control|)
block|{
name|SCRIPT_MODES
operator|.
name|put
argument_list|(
name|scriptMode
operator|.
name|mode
argument_list|,
name|scriptMode
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|parse
specifier|static
name|ScriptMode
name|parse
parameter_list|(
name|String
name|input
parameter_list|)
block|{
name|ScriptMode
name|scriptMode
init|=
name|SCRIPT_MODES
operator|.
name|get
argument_list|(
name|input
argument_list|)
decl_stmt|;
if|if
condition|(
name|scriptMode
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"script mode ["
operator|+
name|input
operator|+
literal|"] not supported"
argument_list|)
throw|;
block|}
return|return
name|scriptMode
return|;
block|}
DECL|method|getMode
specifier|public
name|String
name|getMode
parameter_list|()
block|{
return|return
name|mode
return|;
block|}
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|mode
return|;
block|}
block|}
end_enum

end_unit

