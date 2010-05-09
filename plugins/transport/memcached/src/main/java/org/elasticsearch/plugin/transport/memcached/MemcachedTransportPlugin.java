begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.plugin.transport.memcached
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|plugin
operator|.
name|transport
operator|.
name|memcached
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|memcached
operator|.
name|MemcachedServer
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|memcached
operator|.
name|MemcachedServerModule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|plugins
operator|.
name|AbstractPlugin
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
name|inject
operator|.
name|Module
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
name|Collection
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|collect
operator|.
name|Lists
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|MemcachedTransportPlugin
specifier|public
class|class
name|MemcachedTransportPlugin
extends|extends
name|AbstractPlugin
block|{
DECL|field|settings
specifier|private
specifier|final
name|Settings
name|settings
decl_stmt|;
DECL|method|MemcachedTransportPlugin
specifier|public
name|MemcachedTransportPlugin
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|this
operator|.
name|settings
operator|=
name|settings
expr_stmt|;
block|}
DECL|method|name
annotation|@
name|Override
specifier|public
name|String
name|name
parameter_list|()
block|{
return|return
literal|"transport-memcached"
return|;
block|}
DECL|method|description
annotation|@
name|Override
specifier|public
name|String
name|description
parameter_list|()
block|{
return|return
literal|"Exports elasticsearch APIs over memcached"
return|;
block|}
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
init|=
name|newArrayList
argument_list|()
decl_stmt|;
if|if
condition|(
name|settings
operator|.
name|getAsBoolean
argument_list|(
literal|"memcached.enabled"
argument_list|,
literal|true
argument_list|)
condition|)
block|{
name|modules
operator|.
name|add
argument_list|(
name|MemcachedServerModule
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
return|return
name|modules
return|;
block|}
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
init|=
name|newArrayList
argument_list|()
decl_stmt|;
if|if
condition|(
name|settings
operator|.
name|getAsBoolean
argument_list|(
literal|"memcached.enabled"
argument_list|,
literal|true
argument_list|)
condition|)
block|{
name|services
operator|.
name|add
argument_list|(
name|MemcachedServer
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
return|return
name|services
return|;
block|}
block|}
end_class

end_unit

