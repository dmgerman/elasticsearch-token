begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.cluster.allocation
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|admin
operator|.
name|cluster
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
name|Version
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|ActionRequestValidationException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|support
operator|.
name|master
operator|.
name|MasterNodeRequest
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
name|ParseField
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
name|xcontent
operator|.
name|ObjectParser
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
name|XContentParser
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
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|ValidateActions
operator|.
name|addValidationError
import|;
end_import

begin_comment
comment|/**  * A request to explain the allocation of a shard in the cluster  */
end_comment

begin_class
DECL|class|ClusterAllocationExplainRequest
specifier|public
class|class
name|ClusterAllocationExplainRequest
extends|extends
name|MasterNodeRequest
argument_list|<
name|ClusterAllocationExplainRequest
argument_list|>
block|{
DECL|field|PARSER
specifier|private
specifier|static
name|ObjectParser
argument_list|<
name|ClusterAllocationExplainRequest
argument_list|,
name|Void
argument_list|>
name|PARSER
init|=
operator|new
name|ObjectParser
argument_list|<>
argument_list|(
literal|"cluster/allocation/explain"
argument_list|)
decl_stmt|;
static|static
block|{
name|PARSER
operator|.
name|declareString
argument_list|(
name|ClusterAllocationExplainRequest
operator|::
name|setIndex
argument_list|,
operator|new
name|ParseField
argument_list|(
literal|"index"
argument_list|)
argument_list|)
expr_stmt|;
name|PARSER
operator|.
name|declareInt
argument_list|(
name|ClusterAllocationExplainRequest
operator|::
name|setShard
argument_list|,
operator|new
name|ParseField
argument_list|(
literal|"shard"
argument_list|)
argument_list|)
expr_stmt|;
name|PARSER
operator|.
name|declareBoolean
argument_list|(
name|ClusterAllocationExplainRequest
operator|::
name|setPrimary
argument_list|,
operator|new
name|ParseField
argument_list|(
literal|"primary"
argument_list|)
argument_list|)
expr_stmt|;
name|PARSER
operator|.
name|declareString
argument_list|(
name|ClusterAllocationExplainRequest
operator|::
name|setCurrentNode
argument_list|,
operator|new
name|ParseField
argument_list|(
literal|"current_node"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Nullable
DECL|field|index
specifier|private
name|String
name|index
decl_stmt|;
annotation|@
name|Nullable
DECL|field|shard
specifier|private
name|Integer
name|shard
decl_stmt|;
annotation|@
name|Nullable
DECL|field|primary
specifier|private
name|Boolean
name|primary
decl_stmt|;
annotation|@
name|Nullable
DECL|field|currentNode
specifier|private
name|String
name|currentNode
decl_stmt|;
DECL|field|includeYesDecisions
specifier|private
name|boolean
name|includeYesDecisions
init|=
literal|false
decl_stmt|;
DECL|field|includeDiskInfo
specifier|private
name|boolean
name|includeDiskInfo
init|=
literal|false
decl_stmt|;
comment|/**      * Create a new allocation explain request to explain any unassigned shard in the cluster.      */
DECL|method|ClusterAllocationExplainRequest
specifier|public
name|ClusterAllocationExplainRequest
parameter_list|()
block|{
name|this
operator|.
name|index
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|shard
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|primary
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|currentNode
operator|=
literal|null
expr_stmt|;
block|}
comment|/**      * Create a new allocation explain request. If {@code primary} is false, the first unassigned replica      * will be picked for explanation. If no replicas are unassigned, the first assigned replica will      * be explained.      *      * Package private for testing.      */
DECL|method|ClusterAllocationExplainRequest
name|ClusterAllocationExplainRequest
parameter_list|(
name|String
name|index
parameter_list|,
name|int
name|shard
parameter_list|,
name|boolean
name|primary
parameter_list|,
annotation|@
name|Nullable
name|String
name|currentNode
parameter_list|)
block|{
name|this
operator|.
name|index
operator|=
name|index
expr_stmt|;
name|this
operator|.
name|shard
operator|=
name|shard
expr_stmt|;
name|this
operator|.
name|primary
operator|=
name|primary
expr_stmt|;
name|this
operator|.
name|currentNode
operator|=
name|currentNode
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|validate
specifier|public
name|ActionRequestValidationException
name|validate
parameter_list|()
block|{
name|ActionRequestValidationException
name|validationException
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|this
operator|.
name|useAnyUnassignedShard
argument_list|()
operator|==
literal|false
condition|)
block|{
if|if
condition|(
name|this
operator|.
name|index
operator|==
literal|null
condition|)
block|{
name|validationException
operator|=
name|addValidationError
argument_list|(
literal|"index must be specified"
argument_list|,
name|validationException
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|this
operator|.
name|shard
operator|==
literal|null
condition|)
block|{
name|validationException
operator|=
name|addValidationError
argument_list|(
literal|"shard must be specified"
argument_list|,
name|validationException
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|this
operator|.
name|primary
operator|==
literal|null
condition|)
block|{
name|validationException
operator|=
name|addValidationError
argument_list|(
literal|"primary must be specified"
argument_list|,
name|validationException
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|validationException
return|;
block|}
comment|/**      * Returns {@code true} iff the first unassigned shard is to be used      */
DECL|method|useAnyUnassignedShard
specifier|public
name|boolean
name|useAnyUnassignedShard
parameter_list|()
block|{
return|return
name|this
operator|.
name|index
operator|==
literal|null
operator|&&
name|this
operator|.
name|shard
operator|==
literal|null
operator|&&
name|this
operator|.
name|primary
operator|==
literal|null
operator|&&
name|this
operator|.
name|currentNode
operator|==
literal|null
return|;
block|}
comment|/**      * Sets the index name of the shard to explain.      */
DECL|method|setIndex
specifier|public
name|ClusterAllocationExplainRequest
name|setIndex
parameter_list|(
name|String
name|index
parameter_list|)
block|{
name|this
operator|.
name|index
operator|=
name|index
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Returns the index name of the shard to explain, or {@code null} to use any unassigned shard (see {@link #useAnyUnassignedShard()}).      */
annotation|@
name|Nullable
DECL|method|getIndex
specifier|public
name|String
name|getIndex
parameter_list|()
block|{
return|return
name|this
operator|.
name|index
return|;
block|}
comment|/**      * Sets the shard id of the shard to explain.      */
DECL|method|setShard
specifier|public
name|ClusterAllocationExplainRequest
name|setShard
parameter_list|(
name|Integer
name|shard
parameter_list|)
block|{
name|this
operator|.
name|shard
operator|=
name|shard
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Returns the shard id of the shard to explain, or {@code null} to use any unassigned shard (see {@link #useAnyUnassignedShard()}).      */
annotation|@
name|Nullable
DECL|method|getShard
specifier|public
name|Integer
name|getShard
parameter_list|()
block|{
return|return
name|this
operator|.
name|shard
return|;
block|}
comment|/**      * Sets whether to explain the allocation of the primary shard or a replica shard copy      * for the shard id (see {@link #getShard()}).      */
DECL|method|setPrimary
specifier|public
name|ClusterAllocationExplainRequest
name|setPrimary
parameter_list|(
name|Boolean
name|primary
parameter_list|)
block|{
name|this
operator|.
name|primary
operator|=
name|primary
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Returns {@code true} if explaining the primary shard for the shard id (see {@link #getShard()}),      * {@code false} if explaining a replica shard copy for the shard id, or {@code null} to use any      * unassigned shard (see {@link #useAnyUnassignedShard()}).      */
annotation|@
name|Nullable
DECL|method|isPrimary
specifier|public
name|Boolean
name|isPrimary
parameter_list|()
block|{
return|return
name|this
operator|.
name|primary
return|;
block|}
comment|/**      * Requests the explain API to explain an already assigned replica shard currently allocated to      * the given node.      */
DECL|method|setCurrentNode
specifier|public
name|ClusterAllocationExplainRequest
name|setCurrentNode
parameter_list|(
name|String
name|currentNodeId
parameter_list|)
block|{
name|this
operator|.
name|currentNode
operator|=
name|currentNodeId
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Returns the node holding the replica shard to be explained.  Returns {@code null} if any replica shard      * can be explained.      */
annotation|@
name|Nullable
DECL|method|getCurrentNode
specifier|public
name|String
name|getCurrentNode
parameter_list|()
block|{
return|return
name|currentNode
return|;
block|}
comment|/**      * Set to {@code true} to include yes decisions for a particular node.      */
DECL|method|includeYesDecisions
specifier|public
name|void
name|includeYesDecisions
parameter_list|(
name|boolean
name|includeYesDecisions
parameter_list|)
block|{
name|this
operator|.
name|includeYesDecisions
operator|=
name|includeYesDecisions
expr_stmt|;
block|}
comment|/**      * Returns {@code true} if yes decisions should be included.  Otherwise only "no" and "throttle"      * decisions are returned.      */
DECL|method|includeYesDecisions
specifier|public
name|boolean
name|includeYesDecisions
parameter_list|()
block|{
return|return
name|this
operator|.
name|includeYesDecisions
return|;
block|}
comment|/**      * Set to {@code true} to include information about the gathered disk information of nodes in the cluster.      */
DECL|method|includeDiskInfo
specifier|public
name|void
name|includeDiskInfo
parameter_list|(
name|boolean
name|includeDiskInfo
parameter_list|)
block|{
name|this
operator|.
name|includeDiskInfo
operator|=
name|includeDiskInfo
expr_stmt|;
block|}
comment|/**      * Returns {@code true} if information about disk usage and shard sizes should also be returned.      */
DECL|method|includeDiskInfo
specifier|public
name|boolean
name|includeDiskInfo
parameter_list|()
block|{
return|return
name|this
operator|.
name|includeDiskInfo
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
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|(
literal|"ClusterAllocationExplainRequest["
argument_list|)
decl_stmt|;
if|if
condition|(
name|this
operator|.
name|useAnyUnassignedShard
argument_list|()
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"useAnyUnassignedShard=true"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"index="
argument_list|)
operator|.
name|append
argument_list|(
name|index
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|",shard="
argument_list|)
operator|.
name|append
argument_list|(
name|shard
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|",primary?="
argument_list|)
operator|.
name|append
argument_list|(
name|primary
argument_list|)
expr_stmt|;
if|if
condition|(
name|currentNode
operator|!=
literal|null
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|",currentNode="
argument_list|)
operator|.
name|append
argument_list|(
name|currentNode
argument_list|)
expr_stmt|;
block|}
block|}
name|sb
operator|.
name|append
argument_list|(
literal|",includeYesDecisions?="
argument_list|)
operator|.
name|append
argument_list|(
name|includeYesDecisions
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
DECL|method|parse
specifier|public
specifier|static
name|ClusterAllocationExplainRequest
name|parse
parameter_list|(
name|XContentParser
name|parser
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|PARSER
operator|.
name|parse
argument_list|(
name|parser
argument_list|,
operator|new
name|ClusterAllocationExplainRequest
argument_list|()
argument_list|,
literal|null
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|readFrom
specifier|public
name|void
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|checkVersion
argument_list|(
name|in
operator|.
name|getVersion
argument_list|()
argument_list|)
expr_stmt|;
name|super
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|this
operator|.
name|index
operator|=
name|in
operator|.
name|readOptionalString
argument_list|()
expr_stmt|;
name|this
operator|.
name|shard
operator|=
name|in
operator|.
name|readOptionalVInt
argument_list|()
expr_stmt|;
name|this
operator|.
name|primary
operator|=
name|in
operator|.
name|readOptionalBoolean
argument_list|()
expr_stmt|;
name|this
operator|.
name|currentNode
operator|=
name|in
operator|.
name|readOptionalString
argument_list|()
expr_stmt|;
name|this
operator|.
name|includeYesDecisions
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
name|this
operator|.
name|includeDiskInfo
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
block|}
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
name|checkVersion
argument_list|(
name|out
operator|.
name|getVersion
argument_list|()
argument_list|)
expr_stmt|;
name|super
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeOptionalString
argument_list|(
name|index
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeOptionalVInt
argument_list|(
name|shard
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeOptionalBoolean
argument_list|(
name|primary
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeOptionalString
argument_list|(
name|currentNode
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|includeYesDecisions
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|includeDiskInfo
argument_list|)
expr_stmt|;
block|}
DECL|method|checkVersion
specifier|private
name|void
name|checkVersion
parameter_list|(
name|Version
name|version
parameter_list|)
block|{
if|if
condition|(
name|version
operator|.
name|before
argument_list|(
name|Version
operator|.
name|V_5_2_0
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"cannot explain shards in a mixed-cluster with pre-"
operator|+
name|Version
operator|.
name|V_5_2_0
operator|+
literal|" nodes, node version ["
operator|+
name|version
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

