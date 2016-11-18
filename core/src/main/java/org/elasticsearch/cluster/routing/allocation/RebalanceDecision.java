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
name|cluster
operator|.
name|routing
operator|.
name|allocation
operator|.
name|decider
operator|.
name|Decision
operator|.
name|Type
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
name|Nullable
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
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
comment|/**  * Represents a decision to move a started shard to form a more optimally balanced cluster.  */
end_comment

begin_class
DECL|class|RebalanceDecision
specifier|public
specifier|final
class|class
name|RebalanceDecision
extends|extends
name|RelocationDecision
block|{
comment|/** a constant representing no decision taken */
DECL|field|NOT_TAKEN
specifier|public
specifier|static
specifier|final
name|RebalanceDecision
name|NOT_TAKEN
init|=
operator|new
name|RebalanceDecision
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|Float
operator|.
name|POSITIVE_INFINITY
argument_list|)
decl_stmt|;
annotation|@
name|Nullable
DECL|field|canRebalanceDecision
specifier|private
specifier|final
name|Decision
name|canRebalanceDecision
decl_stmt|;
annotation|@
name|Nullable
DECL|field|nodeDecisions
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|NodeRebalanceResult
argument_list|>
name|nodeDecisions
decl_stmt|;
DECL|field|currentWeight
specifier|private
name|float
name|currentWeight
decl_stmt|;
DECL|method|RebalanceDecision
specifier|public
name|RebalanceDecision
parameter_list|(
name|Decision
name|canRebalanceDecision
parameter_list|,
name|Type
name|finalDecision
parameter_list|,
name|String
name|finalExplanation
parameter_list|)
block|{
name|this
argument_list|(
name|canRebalanceDecision
argument_list|,
name|finalDecision
argument_list|,
name|finalExplanation
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|Float
operator|.
name|POSITIVE_INFINITY
argument_list|)
expr_stmt|;
block|}
DECL|method|RebalanceDecision
specifier|public
name|RebalanceDecision
parameter_list|(
name|Decision
name|canRebalanceDecision
parameter_list|,
name|Type
name|finalDecision
parameter_list|,
name|String
name|finalExplanation
parameter_list|,
name|String
name|assignedNodeId
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|NodeRebalanceResult
argument_list|>
name|nodeDecisions
parameter_list|,
name|float
name|currentWeight
parameter_list|)
block|{
name|super
argument_list|(
name|finalDecision
argument_list|,
name|finalExplanation
argument_list|,
name|assignedNodeId
argument_list|)
expr_stmt|;
name|this
operator|.
name|canRebalanceDecision
operator|=
name|canRebalanceDecision
expr_stmt|;
name|this
operator|.
name|nodeDecisions
operator|=
name|nodeDecisions
operator|!=
literal|null
condition|?
name|Collections
operator|.
name|unmodifiableMap
argument_list|(
name|nodeDecisions
argument_list|)
else|:
literal|null
expr_stmt|;
name|this
operator|.
name|currentWeight
operator|=
name|currentWeight
expr_stmt|;
block|}
comment|/**      * Creates a new {@link RebalanceDecision}, computing the explanation based on the decision parameters.      */
DECL|method|decision
specifier|public
specifier|static
name|RebalanceDecision
name|decision
parameter_list|(
name|Decision
name|canRebalanceDecision
parameter_list|,
name|Type
name|finalDecision
parameter_list|,
name|String
name|assignedNodeId
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|NodeRebalanceResult
argument_list|>
name|nodeDecisions
parameter_list|,
name|float
name|currentWeight
parameter_list|,
name|float
name|threshold
parameter_list|)
block|{
specifier|final
name|String
name|explanation
init|=
name|produceFinalExplanation
argument_list|(
name|finalDecision
argument_list|,
name|assignedNodeId
argument_list|,
name|threshold
argument_list|)
decl_stmt|;
return|return
operator|new
name|RebalanceDecision
argument_list|(
name|canRebalanceDecision
argument_list|,
name|finalDecision
argument_list|,
name|explanation
argument_list|,
name|assignedNodeId
argument_list|,
name|nodeDecisions
argument_list|,
name|currentWeight
argument_list|)
return|;
block|}
comment|/**      * Returns the decision for being allowed to rebalance the shard.      */
annotation|@
name|Nullable
DECL|method|getCanRebalanceDecision
specifier|public
name|Decision
name|getCanRebalanceDecision
parameter_list|()
block|{
return|return
name|canRebalanceDecision
return|;
block|}
comment|/**      * Gets the individual node-level decisions that went into making the final decision as represented by      * {@link #getFinalDecisionType()}.  The map that is returned has the node id as the key and a {@link NodeRebalanceResult}.      */
annotation|@
name|Nullable
DECL|method|getNodeDecisions
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|NodeRebalanceResult
argument_list|>
name|getNodeDecisions
parameter_list|()
block|{
return|return
name|nodeDecisions
return|;
block|}
DECL|method|produceFinalExplanation
specifier|private
specifier|static
name|String
name|produceFinalExplanation
parameter_list|(
specifier|final
name|Type
name|finalDecisionType
parameter_list|,
specifier|final
name|String
name|assignedNodeId
parameter_list|,
specifier|final
name|float
name|threshold
parameter_list|)
block|{
specifier|final
name|String
name|finalExplanation
decl_stmt|;
if|if
condition|(
name|assignedNodeId
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|finalDecisionType
operator|==
name|Type
operator|.
name|THROTTLE
condition|)
block|{
name|finalExplanation
operator|=
literal|"throttle moving shard to node ["
operator|+
name|assignedNodeId
operator|+
literal|"], as it is "
operator|+
literal|"currently busy with other shard relocations"
expr_stmt|;
block|}
else|else
block|{
name|finalExplanation
operator|=
literal|"moving shard to node ["
operator|+
name|assignedNodeId
operator|+
literal|"] to form a more balanced cluster"
expr_stmt|;
block|}
block|}
else|else
block|{
name|finalExplanation
operator|=
literal|"cannot rebalance shard, no other node exists that would form a more balanced "
operator|+
literal|"cluster within the defined threshold ["
operator|+
name|threshold
operator|+
literal|"]"
expr_stmt|;
block|}
return|return
name|finalExplanation
return|;
block|}
block|}
end_class

end_unit

