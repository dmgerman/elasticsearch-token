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
name|node
operator|.
name|DiscoveryNode
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
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|io
operator|.
name|stream
operator|.
name|StreamInput
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
name|io
operator|.
name|stream
operator|.
name|StreamOutput
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
name|io
operator|.
name|stream
operator|.
name|Writeable
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
name|xcontent
operator|.
name|ToXContent
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
name|xcontent
operator|.
name|XContentBuilder
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
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
name|List
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|Collectors
import|;
end_import

begin_comment
comment|/**  * An abstract class for representing various types of allocation decisions.  */
end_comment

begin_class
DECL|class|AbstractAllocationDecision
specifier|public
specifier|abstract
class|class
name|AbstractAllocationDecision
implements|implements
name|ToXContent
implements|,
name|Writeable
block|{
annotation|@
name|Nullable
DECL|field|targetNode
specifier|protected
specifier|final
name|DiscoveryNode
name|targetNode
decl_stmt|;
annotation|@
name|Nullable
DECL|field|nodeDecisions
specifier|protected
specifier|final
name|List
argument_list|<
name|NodeAllocationResult
argument_list|>
name|nodeDecisions
decl_stmt|;
DECL|method|AbstractAllocationDecision
specifier|protected
name|AbstractAllocationDecision
parameter_list|(
annotation|@
name|Nullable
name|DiscoveryNode
name|targetNode
parameter_list|,
annotation|@
name|Nullable
name|List
argument_list|<
name|NodeAllocationResult
argument_list|>
name|nodeDecisions
parameter_list|)
block|{
name|this
operator|.
name|targetNode
operator|=
name|targetNode
expr_stmt|;
name|this
operator|.
name|nodeDecisions
operator|=
name|nodeDecisions
operator|!=
literal|null
condition|?
name|sortNodeDecisions
argument_list|(
name|nodeDecisions
argument_list|)
else|:
literal|null
expr_stmt|;
block|}
DECL|method|AbstractAllocationDecision
specifier|protected
name|AbstractAllocationDecision
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|targetNode
operator|=
name|in
operator|.
name|readOptionalWriteable
argument_list|(
name|DiscoveryNode
operator|::
operator|new
argument_list|)
expr_stmt|;
name|nodeDecisions
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
condition|?
name|Collections
operator|.
name|unmodifiableList
argument_list|(
name|in
operator|.
name|readList
argument_list|(
name|NodeAllocationResult
operator|::
operator|new
argument_list|)
argument_list|)
else|:
literal|null
expr_stmt|;
block|}
comment|/**      * Returns {@code true} if a decision was taken by the allocator, {@code false} otherwise.      * If no decision was taken, then the rest of the fields in this object cannot be accessed and will      * throw an {@code IllegalStateException}.      */
DECL|method|isDecisionTaken
specifier|public
specifier|abstract
name|boolean
name|isDecisionTaken
parameter_list|()
function_decl|;
comment|/**      * Get the node that the allocator will assign the shard to, returning {@code null} if there is no node to      * which the shard will be assigned or moved.  If {@link #isDecisionTaken()} returns {@code false}, then      * invoking this method will throw an {@code IllegalStateException}.      */
annotation|@
name|Nullable
DECL|method|getTargetNode
specifier|public
name|DiscoveryNode
name|getTargetNode
parameter_list|()
block|{
name|checkDecisionState
argument_list|()
expr_stmt|;
return|return
name|targetNode
return|;
block|}
comment|/**      * Gets the sorted list of individual node-level decisions that went into making the ultimate decision whether      * to allocate or move the shard.  If {@link #isDecisionTaken()} returns {@code false}, then      * invoking this method will throw an {@code IllegalStateException}.      */
annotation|@
name|Nullable
DECL|method|getNodeDecisions
specifier|public
name|List
argument_list|<
name|NodeAllocationResult
argument_list|>
name|getNodeDecisions
parameter_list|()
block|{
name|checkDecisionState
argument_list|()
expr_stmt|;
return|return
name|nodeDecisions
return|;
block|}
comment|/**      * Gets the explanation for the decision.  If {@link #isDecisionTaken()} returns {@code false}, then invoking      * this method will throw an {@code IllegalStateException}.      */
DECL|method|getExplanation
specifier|public
specifier|abstract
name|String
name|getExplanation
parameter_list|()
function_decl|;
annotation|@
name|Override
DECL|method|writeTo
specifier|public
name|void
name|writeTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeOptionalWriteable
argument_list|(
name|targetNode
argument_list|)
expr_stmt|;
if|if
condition|(
name|nodeDecisions
operator|!=
literal|null
condition|)
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeList
argument_list|(
name|nodeDecisions
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|checkDecisionState
specifier|protected
name|void
name|checkDecisionState
parameter_list|()
block|{
if|if
condition|(
name|isDecisionTaken
argument_list|()
operator|==
literal|false
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"decision was not taken, individual object fields cannot be accessed"
argument_list|)
throw|;
block|}
block|}
comment|/**      * Generates X-Content for a {@link DiscoveryNode} that leaves off some of the non-critical fields.      */
DECL|method|discoveryNodeToXContent
specifier|public
specifier|static
name|XContentBuilder
name|discoveryNodeToXContent
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|,
name|boolean
name|outerObjectWritten
parameter_list|,
name|XContentBuilder
name|builder
parameter_list|)
throws|throws
name|IOException
block|{
name|builder
operator|.
name|field
argument_list|(
name|outerObjectWritten
condition|?
literal|"id"
else|:
literal|"node_id"
argument_list|,
name|node
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|outerObjectWritten
condition|?
literal|"name"
else|:
literal|"node_name"
argument_list|,
name|node
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"transport_address"
argument_list|,
name|node
operator|.
name|getAddress
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|node
operator|.
name|getAttributes
argument_list|()
operator|.
name|isEmpty
argument_list|()
operator|==
literal|false
condition|)
block|{
name|builder
operator|.
name|startObject
argument_list|(
name|outerObjectWritten
condition|?
literal|"attributes"
else|:
literal|"node_attributes"
argument_list|)
expr_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|entry
range|:
name|node
operator|.
name|getAttributes
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
return|return
name|builder
return|;
block|}
comment|/**      * Sorts a list of node level decisions by the decision type, then by weight ranking, and finally by node id.      */
DECL|method|sortNodeDecisions
specifier|public
name|List
argument_list|<
name|NodeAllocationResult
argument_list|>
name|sortNodeDecisions
parameter_list|(
name|List
argument_list|<
name|NodeAllocationResult
argument_list|>
name|nodeDecisions
parameter_list|)
block|{
return|return
name|Collections
operator|.
name|unmodifiableList
argument_list|(
name|nodeDecisions
operator|.
name|stream
argument_list|()
operator|.
name|sorted
argument_list|()
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
argument_list|)
return|;
block|}
comment|/**      * Generates X-Content for the node-level decisions, creating the outer "node_decisions" object      * in which they are serialized.      */
DECL|method|nodeDecisionsToXContent
specifier|public
name|XContentBuilder
name|nodeDecisionsToXContent
parameter_list|(
name|List
argument_list|<
name|NodeAllocationResult
argument_list|>
name|nodeDecisions
parameter_list|,
name|XContentBuilder
name|builder
parameter_list|,
name|Params
name|params
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|nodeDecisions
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|startArray
argument_list|(
literal|"node_allocation_decisions"
argument_list|)
expr_stmt|;
block|{
for|for
control|(
name|NodeAllocationResult
name|explanation
range|:
name|nodeDecisions
control|)
block|{
name|explanation
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
block|}
block|}
name|builder
operator|.
name|endArray
argument_list|()
expr_stmt|;
block|}
return|return
name|builder
return|;
block|}
comment|/**      * Returns {@code true} if there is at least one node that returned a {@link Type#YES} decision for allocating this shard.      */
DECL|method|atLeastOneNodeWithYesDecision
specifier|protected
name|boolean
name|atLeastOneNodeWithYesDecision
parameter_list|()
block|{
if|if
condition|(
name|nodeDecisions
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
for|for
control|(
name|NodeAllocationResult
name|result
range|:
name|nodeDecisions
control|)
block|{
if|if
condition|(
name|result
operator|.
name|getNodeDecision
argument_list|()
operator|==
name|AllocationDecision
operator|.
name|YES
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
block|}
end_class

end_unit
