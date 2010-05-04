begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.plugins
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|plugins
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|collect
operator|.
name|ImmutableList
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|component
operator|.
name|CloseableIndexComponent
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|component
operator|.
name|LifecycleComponent
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|guice
operator|.
name|inject
operator|.
name|Module
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
import|;
end_import

begin_comment
comment|/**  * A base class for a plugin.  *  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|AbstractPlugin
specifier|public
specifier|abstract
class|class
name|AbstractPlugin
implements|implements
name|Plugin
block|{
comment|/**      * Defaults to return an empty list.      */
DECL|method|modules
annotation|@
name|Override
specifier|public
name|Collection
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|Module
argument_list|>
argument_list|>
name|modules
parameter_list|()
block|{
return|return
name|ImmutableList
operator|.
name|of
argument_list|()
return|;
block|}
comment|/**      * Defaults to return an empty list.      */
DECL|method|services
annotation|@
name|Override
specifier|public
name|Collection
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|LifecycleComponent
argument_list|>
argument_list|>
name|services
parameter_list|()
block|{
return|return
name|ImmutableList
operator|.
name|of
argument_list|()
return|;
block|}
comment|/**      * Defaults to return an empty list.      */
DECL|method|indexModules
annotation|@
name|Override
specifier|public
name|Collection
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|Module
argument_list|>
argument_list|>
name|indexModules
parameter_list|()
block|{
return|return
name|ImmutableList
operator|.
name|of
argument_list|()
return|;
block|}
comment|/**      * Defaults to return an empty list.      */
DECL|method|indexServices
annotation|@
name|Override
specifier|public
name|Collection
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|CloseableIndexComponent
argument_list|>
argument_list|>
name|indexServices
parameter_list|()
block|{
return|return
name|ImmutableList
operator|.
name|of
argument_list|()
return|;
block|}
comment|/**      * Defaults to return an empty list.      */
DECL|method|shardModules
annotation|@
name|Override
specifier|public
name|Collection
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|Module
argument_list|>
argument_list|>
name|shardModules
parameter_list|()
block|{
return|return
name|ImmutableList
operator|.
name|of
argument_list|()
return|;
block|}
comment|/**      * Defaults to return an empty list.      */
DECL|method|shardServices
annotation|@
name|Override
specifier|public
name|Collection
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|CloseableIndexComponent
argument_list|>
argument_list|>
name|shardServices
parameter_list|()
block|{
return|return
name|ImmutableList
operator|.
name|of
argument_list|()
return|;
block|}
DECL|method|processModule
annotation|@
name|Override
specifier|public
name|void
name|processModule
parameter_list|(
name|Module
name|module
parameter_list|)
block|{
comment|// nothing to do here
block|}
block|}
end_class

end_unit

