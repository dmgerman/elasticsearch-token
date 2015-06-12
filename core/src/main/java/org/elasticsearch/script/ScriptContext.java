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
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|Strings
import|;
end_import

begin_comment
comment|/**  * Context of an operation that uses scripts as part of its execution.  */
end_comment

begin_interface
DECL|interface|ScriptContext
specifier|public
interface|interface
name|ScriptContext
block|{
comment|/**      * @return the name of the operation      */
DECL|method|getKey
name|String
name|getKey
parameter_list|()
function_decl|;
comment|/**      * Standard operations that make use of scripts as part of their execution.      * Note that the suggest api is considered part of search for simplicity, as well as the percolate api.      */
DECL|enum|Standard
enum|enum
name|Standard
implements|implements
name|ScriptContext
block|{
DECL|enum constant|AGGS
DECL|enum constant|MAPPING
DECL|enum constant|SEARCH
DECL|enum constant|UPDATE
name|AGGS
argument_list|(
literal|"aggs"
argument_list|)
block|,
name|MAPPING
argument_list|(
literal|"mapping"
argument_list|)
block|,
name|SEARCH
argument_list|(
literal|"search"
argument_list|)
block|,
name|UPDATE
argument_list|(
literal|"update"
argument_list|)
block|;
DECL|field|key
specifier|private
specifier|final
name|String
name|key
decl_stmt|;
DECL|method|Standard
name|Standard
parameter_list|(
name|String
name|key
parameter_list|)
block|{
name|this
operator|.
name|key
operator|=
name|key
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getKey
specifier|public
name|String
name|getKey
parameter_list|()
block|{
return|return
name|key
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
name|getKey
argument_list|()
return|;
block|}
block|}
comment|/**      * Custom operation exposed via plugin, which makes use of scripts as part of its execution      */
DECL|class|Plugin
specifier|final
class|class
name|Plugin
implements|implements
name|ScriptContext
block|{
DECL|field|pluginName
specifier|private
specifier|final
name|String
name|pluginName
decl_stmt|;
DECL|field|operation
specifier|private
specifier|final
name|String
name|operation
decl_stmt|;
DECL|field|key
specifier|private
specifier|final
name|String
name|key
decl_stmt|;
comment|/**          * Creates a new custom scripts based operation exposed via plugin.          * The name of the plugin combined with the operation name can be used to enable/disable scripts via fine-grained settings.          *          * @param pluginName the name of the plugin          * @param operation the name of the operation          */
DECL|method|Plugin
specifier|public
name|Plugin
parameter_list|(
name|String
name|pluginName
parameter_list|,
name|String
name|operation
parameter_list|)
block|{
if|if
condition|(
name|Strings
operator|.
name|hasLength
argument_list|(
name|pluginName
argument_list|)
operator|==
literal|false
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"plugin name cannot be empty when registering a custom script context"
argument_list|)
throw|;
block|}
if|if
condition|(
name|Strings
operator|.
name|hasLength
argument_list|(
name|operation
argument_list|)
operator|==
literal|false
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"operation name cannot be empty when registering a custom script context"
argument_list|)
throw|;
block|}
name|this
operator|.
name|pluginName
operator|=
name|pluginName
expr_stmt|;
name|this
operator|.
name|operation
operator|=
name|operation
expr_stmt|;
name|this
operator|.
name|key
operator|=
name|pluginName
operator|+
literal|"_"
operator|+
name|operation
expr_stmt|;
block|}
DECL|method|getPluginName
specifier|public
specifier|final
name|String
name|getPluginName
parameter_list|()
block|{
return|return
name|pluginName
return|;
block|}
DECL|method|getOperation
specifier|public
specifier|final
name|String
name|getOperation
parameter_list|()
block|{
return|return
name|operation
return|;
block|}
annotation|@
name|Override
DECL|method|getKey
specifier|public
specifier|final
name|String
name|getKey
parameter_list|()
block|{
return|return
name|key
return|;
block|}
annotation|@
name|Override
DECL|method|toString
specifier|public
specifier|final
name|String
name|toString
parameter_list|()
block|{
return|return
name|getKey
argument_list|()
return|;
block|}
block|}
block|}
end_interface

end_unit
