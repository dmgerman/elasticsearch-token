begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.cluster.routing.allocation.decider
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
name|decider
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
name|metadata
operator|.
name|MetaData
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
name|RoutingNode
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
name|inject
operator|.
name|Inject
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
name|node
operator|.
name|settings
operator|.
name|NodeSettingsService
import|;
end_import

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|DisableAllocationDecider
specifier|public
class|class
name|DisableAllocationDecider
extends|extends
name|AllocationDecider
block|{
static|static
block|{
name|MetaData
operator|.
name|addDynamicSettings
argument_list|(
literal|"cluster.routing.allocation.disable_allocation"
argument_list|,
literal|"cluster.routing.allocation.disable_replica_allocation"
argument_list|)
expr_stmt|;
block|}
DECL|class|ApplySettings
class|class
name|ApplySettings
implements|implements
name|NodeSettingsService
operator|.
name|Listener
block|{
annotation|@
name|Override
DECL|method|onRefreshSettings
specifier|public
name|void
name|onRefreshSettings
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|boolean
name|disableAllocation
init|=
name|settings
operator|.
name|getAsBoolean
argument_list|(
literal|"cluster.routing.allocation.disable_allocation"
argument_list|,
name|DisableAllocationDecider
operator|.
name|this
operator|.
name|disableAllocation
argument_list|)
decl_stmt|;
if|if
condition|(
name|disableAllocation
operator|!=
name|DisableAllocationDecider
operator|.
name|this
operator|.
name|disableAllocation
condition|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"updating [cluster.routing.allocation.disable_allocation] from [{}] to [{}]"
argument_list|,
name|DisableAllocationDecider
operator|.
name|this
operator|.
name|disableAllocation
argument_list|,
name|disableAllocation
argument_list|)
expr_stmt|;
name|DisableAllocationDecider
operator|.
name|this
operator|.
name|disableAllocation
operator|=
name|disableAllocation
expr_stmt|;
block|}
name|boolean
name|disableReplicaAllocation
init|=
name|settings
operator|.
name|getAsBoolean
argument_list|(
literal|"cluster.routing.allocation.disable_replica_allocation"
argument_list|,
name|DisableAllocationDecider
operator|.
name|this
operator|.
name|disableReplicaAllocation
argument_list|)
decl_stmt|;
if|if
condition|(
name|disableReplicaAllocation
operator|!=
name|DisableAllocationDecider
operator|.
name|this
operator|.
name|disableReplicaAllocation
condition|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"updating [cluster.routing.allocation.disable_replica_allocation] from [{}] to [{}]"
argument_list|,
name|DisableAllocationDecider
operator|.
name|this
operator|.
name|disableReplicaAllocation
argument_list|,
name|disableReplicaAllocation
argument_list|)
expr_stmt|;
name|DisableAllocationDecider
operator|.
name|this
operator|.
name|disableReplicaAllocation
operator|=
name|disableReplicaAllocation
expr_stmt|;
block|}
block|}
block|}
DECL|field|disableAllocation
specifier|private
specifier|volatile
name|boolean
name|disableAllocation
decl_stmt|;
DECL|field|disableReplicaAllocation
specifier|private
specifier|volatile
name|boolean
name|disableReplicaAllocation
decl_stmt|;
annotation|@
name|Inject
DECL|method|DisableAllocationDecider
specifier|public
name|DisableAllocationDecider
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|NodeSettingsService
name|nodeSettingsService
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|disableAllocation
operator|=
name|settings
operator|.
name|getAsBoolean
argument_list|(
literal|"cluster.routing.allocation.disable_allocation"
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|this
operator|.
name|disableReplicaAllocation
operator|=
name|settings
operator|.
name|getAsBoolean
argument_list|(
literal|"cluster.routing.allocation.disable_replica_allocation"
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|nodeSettingsService
operator|.
name|addListener
argument_list|(
operator|new
name|ApplySettings
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|canAllocate
specifier|public
name|Decision
name|canAllocate
parameter_list|(
name|ShardRouting
name|shardRouting
parameter_list|,
name|RoutingNode
name|node
parameter_list|,
name|RoutingAllocation
name|allocation
parameter_list|)
block|{
if|if
condition|(
name|disableAllocation
condition|)
block|{
return|return
name|allocation
operator|.
name|shouldIgnoreDisable
argument_list|(
name|shardRouting
operator|.
name|shardId
argument_list|()
argument_list|,
name|node
operator|.
name|nodeId
argument_list|()
argument_list|)
condition|?
name|Decision
operator|.
name|YES
else|:
name|Decision
operator|.
name|NO
return|;
block|}
if|if
condition|(
name|disableReplicaAllocation
condition|)
block|{
return|return
name|shardRouting
operator|.
name|primary
argument_list|()
condition|?
name|Decision
operator|.
name|YES
else|:
name|allocation
operator|.
name|shouldIgnoreDisable
argument_list|(
name|shardRouting
operator|.
name|shardId
argument_list|()
argument_list|,
name|node
operator|.
name|nodeId
argument_list|()
argument_list|)
condition|?
name|Decision
operator|.
name|YES
else|:
name|Decision
operator|.
name|NO
return|;
block|}
return|return
name|Decision
operator|.
name|YES
return|;
block|}
block|}
end_class

end_unit

