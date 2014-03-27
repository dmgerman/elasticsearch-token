begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.cluster.routing.allocation
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
name|ClusterInfo
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
name|node
operator|.
name|DiscoveryNodes
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
name|RoutingTable
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
name|decider
operator|.
name|AllocationDeciders
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
name|decider
operator|.
name|Decision
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|shard
operator|.
name|ShardId
import|;
end_import

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
comment|/**  * The {@link RoutingAllocation} keep the state of the current allocation  * of shards and holds the {@link AllocationDeciders} which are responsible  *  for the current routing state.  */
end_comment

begin_class
DECL|class|RoutingAllocation
specifier|public
class|class
name|RoutingAllocation
block|{
comment|/**      * this class is used to describe results of a {@link RoutingAllocation}        */
DECL|class|Result
specifier|public
specifier|static
class|class
name|Result
block|{
DECL|field|changed
specifier|private
specifier|final
name|boolean
name|changed
decl_stmt|;
DECL|field|routingTable
specifier|private
specifier|final
name|RoutingTable
name|routingTable
decl_stmt|;
DECL|field|explanations
specifier|private
name|RoutingExplanations
name|explanations
init|=
operator|new
name|RoutingExplanations
argument_list|()
decl_stmt|;
comment|/**          * Creates a new {@link RoutingAllocation.Result}          *          * @param changed a flag to determine whether the actual {@link RoutingTable} has been changed          * @param routingTable the {@link RoutingTable} this Result references          */
DECL|method|Result
specifier|public
name|Result
parameter_list|(
name|boolean
name|changed
parameter_list|,
name|RoutingTable
name|routingTable
parameter_list|)
block|{
name|this
operator|.
name|changed
operator|=
name|changed
expr_stmt|;
name|this
operator|.
name|routingTable
operator|=
name|routingTable
expr_stmt|;
block|}
comment|/**          * Creates a new {@link RoutingAllocation.Result}          *           * @param changed a flag to determine whether the actual {@link RoutingTable} has been changed          * @param routingTable the {@link RoutingTable} this Result references          * @param explanations Explanation for the reroute actions          */
DECL|method|Result
specifier|public
name|Result
parameter_list|(
name|boolean
name|changed
parameter_list|,
name|RoutingTable
name|routingTable
parameter_list|,
name|RoutingExplanations
name|explanations
parameter_list|)
block|{
name|this
operator|.
name|changed
operator|=
name|changed
expr_stmt|;
name|this
operator|.
name|routingTable
operator|=
name|routingTable
expr_stmt|;
name|this
operator|.
name|explanations
operator|=
name|explanations
expr_stmt|;
block|}
comment|/** determine whether the actual {@link RoutingTable} has been changed          * @return<code>true</code> if the {@link RoutingTable} has been changed by allocation. Otherwise<code>false</code>          */
DECL|method|changed
specifier|public
name|boolean
name|changed
parameter_list|()
block|{
return|return
name|this
operator|.
name|changed
return|;
block|}
comment|/**          * Get the {@link RoutingTable} referenced by this result          * @return referenced {@link RoutingTable}          */
DECL|method|routingTable
specifier|public
name|RoutingTable
name|routingTable
parameter_list|()
block|{
return|return
name|routingTable
return|;
block|}
comment|/**          * Get the explanation of this result          * @return explanation          */
DECL|method|explanations
specifier|public
name|RoutingExplanations
name|explanations
parameter_list|()
block|{
return|return
name|explanations
return|;
block|}
block|}
DECL|field|deciders
specifier|private
specifier|final
name|AllocationDeciders
name|deciders
decl_stmt|;
DECL|field|routingNodes
specifier|private
specifier|final
name|RoutingNodes
name|routingNodes
decl_stmt|;
DECL|field|nodes
specifier|private
specifier|final
name|DiscoveryNodes
name|nodes
decl_stmt|;
DECL|field|explanation
specifier|private
specifier|final
name|AllocationExplanation
name|explanation
init|=
operator|new
name|AllocationExplanation
argument_list|()
decl_stmt|;
DECL|field|clusterInfo
specifier|private
specifier|final
name|ClusterInfo
name|clusterInfo
decl_stmt|;
DECL|field|ignoredShardToNodes
specifier|private
name|Map
argument_list|<
name|ShardId
argument_list|,
name|String
argument_list|>
name|ignoredShardToNodes
init|=
literal|null
decl_stmt|;
DECL|field|ignoreDisable
specifier|private
name|boolean
name|ignoreDisable
init|=
literal|false
decl_stmt|;
DECL|field|debugDecision
specifier|private
name|boolean
name|debugDecision
init|=
literal|false
decl_stmt|;
comment|/**      * Creates a new {@link RoutingAllocation}      *       * @param deciders {@link AllocationDeciders} to used to make decisions for routing allocations      * @param routingNodes Routing nodes in the current cluster       * @param nodes TODO: Documentation      */
DECL|method|RoutingAllocation
specifier|public
name|RoutingAllocation
parameter_list|(
name|AllocationDeciders
name|deciders
parameter_list|,
name|RoutingNodes
name|routingNodes
parameter_list|,
name|DiscoveryNodes
name|nodes
parameter_list|,
name|ClusterInfo
name|clusterInfo
parameter_list|)
block|{
name|this
operator|.
name|deciders
operator|=
name|deciders
expr_stmt|;
name|this
operator|.
name|routingNodes
operator|=
name|routingNodes
expr_stmt|;
name|this
operator|.
name|nodes
operator|=
name|nodes
expr_stmt|;
name|this
operator|.
name|clusterInfo
operator|=
name|clusterInfo
expr_stmt|;
block|}
comment|/**      * Get {@link AllocationDeciders} used for allocation      * @return {@link AllocationDeciders} used for allocation      */
DECL|method|deciders
specifier|public
name|AllocationDeciders
name|deciders
parameter_list|()
block|{
return|return
name|this
operator|.
name|deciders
return|;
block|}
comment|/**      * Get routing table of current nodes      * @return current routing table      */
DECL|method|routingTable
specifier|public
name|RoutingTable
name|routingTable
parameter_list|()
block|{
return|return
name|routingNodes
operator|.
name|routingTable
argument_list|()
return|;
block|}
comment|/**      * Get current routing nodes      * @return routing nodes      */
DECL|method|routingNodes
specifier|public
name|RoutingNodes
name|routingNodes
parameter_list|()
block|{
return|return
name|routingNodes
return|;
block|}
comment|/**      * Get metadata of routing nodes      * @return Metadata of routing nodes      */
DECL|method|metaData
specifier|public
name|MetaData
name|metaData
parameter_list|()
block|{
return|return
name|routingNodes
operator|.
name|metaData
argument_list|()
return|;
block|}
comment|/**      * Get discovery nodes in current routing      * @return discovery nodes      */
DECL|method|nodes
specifier|public
name|DiscoveryNodes
name|nodes
parameter_list|()
block|{
return|return
name|nodes
return|;
block|}
DECL|method|clusterInfo
specifier|public
name|ClusterInfo
name|clusterInfo
parameter_list|()
block|{
return|return
name|clusterInfo
return|;
block|}
comment|/**      * Get explanations of current routing      * @return explanation of routing      */
DECL|method|explanation
specifier|public
name|AllocationExplanation
name|explanation
parameter_list|()
block|{
return|return
name|explanation
return|;
block|}
DECL|method|ignoreDisable
specifier|public
name|void
name|ignoreDisable
parameter_list|(
name|boolean
name|ignoreDisable
parameter_list|)
block|{
name|this
operator|.
name|ignoreDisable
operator|=
name|ignoreDisable
expr_stmt|;
block|}
DECL|method|ignoreDisable
specifier|public
name|boolean
name|ignoreDisable
parameter_list|()
block|{
return|return
name|this
operator|.
name|ignoreDisable
return|;
block|}
DECL|method|debugDecision
specifier|public
name|void
name|debugDecision
parameter_list|(
name|boolean
name|debug
parameter_list|)
block|{
name|this
operator|.
name|debugDecision
operator|=
name|debug
expr_stmt|;
block|}
DECL|method|debugDecision
specifier|public
name|boolean
name|debugDecision
parameter_list|()
block|{
return|return
name|this
operator|.
name|debugDecision
return|;
block|}
DECL|method|addIgnoreShardForNode
specifier|public
name|void
name|addIgnoreShardForNode
parameter_list|(
name|ShardId
name|shardId
parameter_list|,
name|String
name|nodeId
parameter_list|)
block|{
if|if
condition|(
name|ignoredShardToNodes
operator|==
literal|null
condition|)
block|{
name|ignoredShardToNodes
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
expr_stmt|;
block|}
name|ignoredShardToNodes
operator|.
name|put
argument_list|(
name|shardId
argument_list|,
name|nodeId
argument_list|)
expr_stmt|;
block|}
DECL|method|shouldIgnoreShardForNode
specifier|public
name|boolean
name|shouldIgnoreShardForNode
parameter_list|(
name|ShardId
name|shardId
parameter_list|,
name|String
name|nodeId
parameter_list|)
block|{
return|return
name|ignoredShardToNodes
operator|!=
literal|null
operator|&&
name|nodeId
operator|.
name|equals
argument_list|(
name|ignoredShardToNodes
operator|.
name|get
argument_list|(
name|shardId
argument_list|)
argument_list|)
return|;
block|}
comment|/**      * Create a routing decision, including the reason if the debug flag is      * turned on      * @param decision decision whether to allow/deny allocation      * @param deciderLabel a human readable label for the AllocationDecider      * @param reason a format string explanation of the decision      * @param params format string parameters      */
DECL|method|decision
specifier|public
name|Decision
name|decision
parameter_list|(
name|Decision
name|decision
parameter_list|,
name|String
name|deciderLabel
parameter_list|,
name|String
name|reason
parameter_list|,
name|Object
modifier|...
name|params
parameter_list|)
block|{
if|if
condition|(
name|debugDecision
argument_list|()
condition|)
block|{
return|return
name|Decision
operator|.
name|single
argument_list|(
name|decision
operator|.
name|type
argument_list|()
argument_list|,
name|deciderLabel
argument_list|,
name|reason
argument_list|,
name|params
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|decision
return|;
block|}
block|}
block|}
end_class

end_unit

