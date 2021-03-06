begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.test.gateway
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|gateway
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|routing
operator|.
name|ShardRouting
import|;
end_import

begin_import
import|import
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
name|FailedShard
import|;
end_import

begin_import
import|import
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
name|RoutingAllocation
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
name|GatewayAllocator
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_comment
comment|/**  * An allocator used for tests that doesn't do anything  */
end_comment

begin_class
DECL|class|NoopGatewayAllocator
specifier|public
class|class
name|NoopGatewayAllocator
extends|extends
name|GatewayAllocator
block|{
DECL|field|INSTANCE
specifier|public
specifier|static
specifier|final
name|NoopGatewayAllocator
name|INSTANCE
init|=
operator|new
name|NoopGatewayAllocator
argument_list|()
decl_stmt|;
DECL|method|NoopGatewayAllocator
specifier|protected
name|NoopGatewayAllocator
parameter_list|()
block|{
name|super
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|applyStartedShards
specifier|public
name|void
name|applyStartedShards
parameter_list|(
name|RoutingAllocation
name|allocation
parameter_list|,
name|List
argument_list|<
name|ShardRouting
argument_list|>
name|startedShards
parameter_list|)
block|{
comment|// noop
block|}
annotation|@
name|Override
DECL|method|applyFailedShards
specifier|public
name|void
name|applyFailedShards
parameter_list|(
name|RoutingAllocation
name|allocation
parameter_list|,
name|List
argument_list|<
name|FailedShard
argument_list|>
name|failedShards
parameter_list|)
block|{
comment|// noop
block|}
annotation|@
name|Override
DECL|method|allocateUnassigned
specifier|public
name|void
name|allocateUnassigned
parameter_list|(
name|RoutingAllocation
name|allocation
parameter_list|)
block|{
comment|// noop
block|}
block|}
end_class

end_unit

