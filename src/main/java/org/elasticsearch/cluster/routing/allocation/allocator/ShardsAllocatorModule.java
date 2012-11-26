begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.cluster.routing.allocation.allocator
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|routing
operator|.
name|allocation
operator|.
name|allocator
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
name|inject
operator|.
name|AbstractModule
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
name|settings
operator|.
name|Settings
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|gateway
operator|.
name|none
operator|.
name|NoneGatewayAllocator
import|;
end_import

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|ShardsAllocatorModule
specifier|public
class|class
name|ShardsAllocatorModule
extends|extends
name|AbstractModule
block|{
DECL|field|settings
specifier|private
name|Settings
name|settings
decl_stmt|;
DECL|field|shardsAllocator
specifier|private
name|Class
argument_list|<
name|?
extends|extends
name|ShardsAllocator
argument_list|>
name|shardsAllocator
decl_stmt|;
DECL|field|gatewayAllocator
specifier|private
name|Class
argument_list|<
name|?
extends|extends
name|GatewayAllocator
argument_list|>
name|gatewayAllocator
init|=
name|NoneGatewayAllocator
operator|.
name|class
decl_stmt|;
DECL|method|ShardsAllocatorModule
specifier|public
name|ShardsAllocatorModule
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
DECL|method|setGatewayAllocator
specifier|public
name|void
name|setGatewayAllocator
parameter_list|(
name|Class
argument_list|<
name|?
extends|extends
name|GatewayAllocator
argument_list|>
name|gatewayAllocator
parameter_list|)
block|{
name|this
operator|.
name|gatewayAllocator
operator|=
name|gatewayAllocator
expr_stmt|;
block|}
DECL|method|setShardsAllocator
specifier|public
name|void
name|setShardsAllocator
parameter_list|(
name|Class
argument_list|<
name|?
extends|extends
name|ShardsAllocator
argument_list|>
name|shardsAllocator
parameter_list|)
block|{
name|this
operator|.
name|shardsAllocator
operator|=
name|shardsAllocator
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|configure
specifier|protected
name|void
name|configure
parameter_list|()
block|{
name|bind
argument_list|(
name|GatewayAllocator
operator|.
name|class
argument_list|)
operator|.
name|to
argument_list|(
name|gatewayAllocator
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|bind
argument_list|(
name|ShardsAllocator
operator|.
name|class
argument_list|)
operator|.
name|to
argument_list|(
name|shardsAllocator
operator|==
literal|null
condition|?
name|BalancedShardsAllocator
operator|.
name|class
else|:
name|shardsAllocator
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

