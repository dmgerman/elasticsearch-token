begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|routing
operator|.
name|RestoreSource
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
name|RoutingNodes
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
name|settings
operator|.
name|Settings
import|;
end_import

begin_comment
comment|/**  * An allocation decider that prevents relocation or allocation from nodes  * that might not be version compatible. If we relocate from a node that runs  * a newer version than the node we relocate to this might cause {@link org.apache.lucene.index.IndexFormatTooNewException}  * on the lowest level since it might have already written segments that use a new postings format or codec that is not  * available on the target node.  */
end_comment

begin_class
DECL|class|NodeVersionAllocationDecider
specifier|public
class|class
name|NodeVersionAllocationDecider
extends|extends
name|AllocationDecider
block|{
DECL|field|NAME
specifier|public
specifier|static
specifier|final
name|String
name|NAME
init|=
literal|"node_version"
decl_stmt|;
DECL|method|NodeVersionAllocationDecider
specifier|public
name|NodeVersionAllocationDecider
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|super
argument_list|(
name|settings
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
name|shardRouting
operator|.
name|primary
argument_list|()
condition|)
block|{
if|if
condition|(
name|shardRouting
operator|.
name|currentNodeId
argument_list|()
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|shardRouting
operator|.
name|restoreSource
argument_list|()
operator|!=
literal|null
condition|)
block|{
comment|// restoring from a snapshot - check that the node can handle the version
return|return
name|isVersionCompatible
argument_list|(
name|shardRouting
operator|.
name|restoreSource
argument_list|()
argument_list|,
name|node
argument_list|,
name|allocation
argument_list|)
return|;
block|}
else|else
block|{
comment|// fresh primary, we can allocate wherever
return|return
name|allocation
operator|.
name|decision
argument_list|(
name|Decision
operator|.
name|YES
argument_list|,
name|NAME
argument_list|,
literal|"the primary shard is new and can be allocated anywhere"
argument_list|)
return|;
block|}
block|}
else|else
block|{
comment|// relocating primary, only migrate to newer host
return|return
name|isVersionCompatible
argument_list|(
name|allocation
operator|.
name|routingNodes
argument_list|()
argument_list|,
name|shardRouting
operator|.
name|currentNodeId
argument_list|()
argument_list|,
name|node
argument_list|,
name|allocation
argument_list|)
return|;
block|}
block|}
else|else
block|{
specifier|final
name|ShardRouting
name|primary
init|=
name|allocation
operator|.
name|routingNodes
argument_list|()
operator|.
name|activePrimary
argument_list|(
name|shardRouting
operator|.
name|shardId
argument_list|()
argument_list|)
decl_stmt|;
comment|// check that active primary has a newer version so that peer recovery works
if|if
condition|(
name|primary
operator|!=
literal|null
condition|)
block|{
return|return
name|isVersionCompatible
argument_list|(
name|allocation
operator|.
name|routingNodes
argument_list|()
argument_list|,
name|primary
operator|.
name|currentNodeId
argument_list|()
argument_list|,
name|node
argument_list|,
name|allocation
argument_list|)
return|;
block|}
else|else
block|{
comment|// ReplicaAfterPrimaryActiveAllocationDecider should prevent this case from occurring
return|return
name|allocation
operator|.
name|decision
argument_list|(
name|Decision
operator|.
name|YES
argument_list|,
name|NAME
argument_list|,
literal|"no active primary shard yet"
argument_list|)
return|;
block|}
block|}
block|}
DECL|method|isVersionCompatible
specifier|private
name|Decision
name|isVersionCompatible
parameter_list|(
specifier|final
name|RoutingNodes
name|routingNodes
parameter_list|,
specifier|final
name|String
name|sourceNodeId
parameter_list|,
specifier|final
name|RoutingNode
name|target
parameter_list|,
name|RoutingAllocation
name|allocation
parameter_list|)
block|{
specifier|final
name|RoutingNode
name|source
init|=
name|routingNodes
operator|.
name|node
argument_list|(
name|sourceNodeId
argument_list|)
decl_stmt|;
if|if
condition|(
name|target
operator|.
name|node
argument_list|()
operator|.
name|getVersion
argument_list|()
operator|.
name|onOrAfter
argument_list|(
name|source
operator|.
name|node
argument_list|()
operator|.
name|getVersion
argument_list|()
argument_list|)
condition|)
block|{
comment|/* we can allocate if we can recover from a node that is younger or on the same version              * if the primary is already running on a newer version that won't work due to possible              * differences in the lucene index format etc.*/
return|return
name|allocation
operator|.
name|decision
argument_list|(
name|Decision
operator|.
name|YES
argument_list|,
name|NAME
argument_list|,
literal|"target node version [%s] is the same or newer than source node version [%s]"
argument_list|,
name|target
operator|.
name|node
argument_list|()
operator|.
name|getVersion
argument_list|()
argument_list|,
name|source
operator|.
name|node
argument_list|()
operator|.
name|getVersion
argument_list|()
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|allocation
operator|.
name|decision
argument_list|(
name|Decision
operator|.
name|NO
argument_list|,
name|NAME
argument_list|,
literal|"target node version [%s] is older than the source node version [%s]"
argument_list|,
name|target
operator|.
name|node
argument_list|()
operator|.
name|getVersion
argument_list|()
argument_list|,
name|source
operator|.
name|node
argument_list|()
operator|.
name|getVersion
argument_list|()
argument_list|)
return|;
block|}
block|}
DECL|method|isVersionCompatible
specifier|private
name|Decision
name|isVersionCompatible
parameter_list|(
name|RestoreSource
name|restoreSource
parameter_list|,
specifier|final
name|RoutingNode
name|target
parameter_list|,
name|RoutingAllocation
name|allocation
parameter_list|)
block|{
if|if
condition|(
name|target
operator|.
name|node
argument_list|()
operator|.
name|getVersion
argument_list|()
operator|.
name|onOrAfter
argument_list|(
name|restoreSource
operator|.
name|version
argument_list|()
argument_list|)
condition|)
block|{
comment|/* we can allocate if we can restore from a snapshot that is older or on the same version */
return|return
name|allocation
operator|.
name|decision
argument_list|(
name|Decision
operator|.
name|YES
argument_list|,
name|NAME
argument_list|,
literal|"target node version [%s] is the same or newer than snapshot version [%s]"
argument_list|,
name|target
operator|.
name|node
argument_list|()
operator|.
name|getVersion
argument_list|()
argument_list|,
name|restoreSource
operator|.
name|version
argument_list|()
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|allocation
operator|.
name|decision
argument_list|(
name|Decision
operator|.
name|NO
argument_list|,
name|NAME
argument_list|,
literal|"target node version [%s] is older than the snapshot version [%s]"
argument_list|,
name|target
operator|.
name|node
argument_list|()
operator|.
name|getVersion
argument_list|()
argument_list|,
name|restoreSource
operator|.
name|version
argument_list|()
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

