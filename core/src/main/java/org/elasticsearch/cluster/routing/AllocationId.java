begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.cluster.routing
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|routing
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
name|ParseFieldMatcher
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
name|ParseFieldMatcherSupplier
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
name|UUIDs
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
import|import
name|java
operator|.
name|util
operator|.
name|Objects
import|;
end_import

begin_comment
comment|/**  * Uniquely identifies an allocation. An allocation is a shard moving from unassigned to initializing,  * or relocation.  *<p>  * Relocation is a special case, where the origin shard is relocating with a relocationId and same id, and  * the target shard (only materialized in RoutingNodes) is initializing with the id set to the origin shard  * relocationId. Once relocation is done, the new allocation id is set to the relocationId. This is similar  * behavior to how ShardRouting#currentNodeId is used.  */
end_comment

begin_class
DECL|class|AllocationId
specifier|public
class|class
name|AllocationId
implements|implements
name|ToXContent
implements|,
name|Writeable
block|{
DECL|field|ID_KEY
specifier|private
specifier|static
specifier|final
name|String
name|ID_KEY
init|=
literal|"id"
decl_stmt|;
DECL|field|RELOCATION_ID_KEY
specifier|private
specifier|static
specifier|final
name|String
name|RELOCATION_ID_KEY
init|=
literal|"relocation_id"
decl_stmt|;
DECL|field|ALLOCATION_ID_PARSER
specifier|private
specifier|static
specifier|final
name|ObjectParser
argument_list|<
name|AllocationId
operator|.
name|Builder
argument_list|,
name|ParseFieldMatcherSupplier
argument_list|>
name|ALLOCATION_ID_PARSER
init|=
operator|new
name|ObjectParser
argument_list|<>
argument_list|(
literal|"allocationId"
argument_list|)
decl_stmt|;
static|static
block|{
name|ALLOCATION_ID_PARSER
operator|.
name|declareString
argument_list|(
name|AllocationId
operator|.
name|Builder
operator|::
name|setId
argument_list|,
operator|new
name|ParseField
argument_list|(
name|ID_KEY
argument_list|)
argument_list|)
expr_stmt|;
name|ALLOCATION_ID_PARSER
operator|.
name|declareString
argument_list|(
name|AllocationId
operator|.
name|Builder
operator|::
name|setRelocationId
argument_list|,
operator|new
name|ParseField
argument_list|(
name|RELOCATION_ID_KEY
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|class|Builder
specifier|private
specifier|static
class|class
name|Builder
block|{
DECL|field|id
specifier|private
name|String
name|id
decl_stmt|;
DECL|field|relocationId
specifier|private
name|String
name|relocationId
decl_stmt|;
DECL|method|setId
specifier|public
name|void
name|setId
parameter_list|(
name|String
name|id
parameter_list|)
block|{
name|this
operator|.
name|id
operator|=
name|id
expr_stmt|;
block|}
DECL|method|setRelocationId
specifier|public
name|void
name|setRelocationId
parameter_list|(
name|String
name|relocationId
parameter_list|)
block|{
name|this
operator|.
name|relocationId
operator|=
name|relocationId
expr_stmt|;
block|}
DECL|method|build
specifier|public
name|AllocationId
name|build
parameter_list|()
block|{
return|return
operator|new
name|AllocationId
argument_list|(
name|id
argument_list|,
name|relocationId
argument_list|)
return|;
block|}
block|}
DECL|field|id
specifier|private
specifier|final
name|String
name|id
decl_stmt|;
annotation|@
name|Nullable
DECL|field|relocationId
specifier|private
specifier|final
name|String
name|relocationId
decl_stmt|;
DECL|method|AllocationId
name|AllocationId
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|id
operator|=
name|in
operator|.
name|readString
argument_list|()
expr_stmt|;
name|this
operator|.
name|relocationId
operator|=
name|in
operator|.
name|readOptionalString
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
name|out
operator|.
name|writeString
argument_list|(
name|this
operator|.
name|id
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeOptionalString
argument_list|(
name|this
operator|.
name|relocationId
argument_list|)
expr_stmt|;
block|}
DECL|method|AllocationId
specifier|private
name|AllocationId
parameter_list|(
name|String
name|id
parameter_list|,
name|String
name|relocationId
parameter_list|)
block|{
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|id
argument_list|,
literal|"Argument [id] must be non-null"
argument_list|)
expr_stmt|;
name|this
operator|.
name|id
operator|=
name|id
expr_stmt|;
name|this
operator|.
name|relocationId
operator|=
name|relocationId
expr_stmt|;
block|}
comment|/**      * Creates a new allocation id for initializing allocation.      */
DECL|method|newInitializing
specifier|public
specifier|static
name|AllocationId
name|newInitializing
parameter_list|()
block|{
return|return
operator|new
name|AllocationId
argument_list|(
name|UUIDs
operator|.
name|randomBase64UUID
argument_list|()
argument_list|,
literal|null
argument_list|)
return|;
block|}
comment|/**      * Creates a new allocation id for initializing allocation based on an existing id.      */
DECL|method|newInitializing
specifier|public
specifier|static
name|AllocationId
name|newInitializing
parameter_list|(
name|String
name|existingAllocationId
parameter_list|)
block|{
return|return
operator|new
name|AllocationId
argument_list|(
name|existingAllocationId
argument_list|,
literal|null
argument_list|)
return|;
block|}
comment|/**      * Creates a new allocation id for the target initializing shard that is the result      * of a relocation.      */
DECL|method|newTargetRelocation
specifier|public
specifier|static
name|AllocationId
name|newTargetRelocation
parameter_list|(
name|AllocationId
name|allocationId
parameter_list|)
block|{
assert|assert
name|allocationId
operator|.
name|getRelocationId
argument_list|()
operator|!=
literal|null
assert|;
return|return
operator|new
name|AllocationId
argument_list|(
name|allocationId
operator|.
name|getRelocationId
argument_list|()
argument_list|,
name|allocationId
operator|.
name|getId
argument_list|()
argument_list|)
return|;
block|}
comment|/**      * Creates a new allocation id for a shard that moves to be relocated, populating      * the transient holder for relocationId.      */
DECL|method|newRelocation
specifier|public
specifier|static
name|AllocationId
name|newRelocation
parameter_list|(
name|AllocationId
name|allocationId
parameter_list|)
block|{
assert|assert
name|allocationId
operator|.
name|getRelocationId
argument_list|()
operator|==
literal|null
assert|;
return|return
operator|new
name|AllocationId
argument_list|(
name|allocationId
operator|.
name|getId
argument_list|()
argument_list|,
name|UUIDs
operator|.
name|randomBase64UUID
argument_list|()
argument_list|)
return|;
block|}
comment|/**      * Creates a new allocation id representing a cancelled relocation.      *<p>      * Note that this is expected to be called on the allocation id      * of the *source* shard      */
DECL|method|cancelRelocation
specifier|public
specifier|static
name|AllocationId
name|cancelRelocation
parameter_list|(
name|AllocationId
name|allocationId
parameter_list|)
block|{
assert|assert
name|allocationId
operator|.
name|getRelocationId
argument_list|()
operator|!=
literal|null
assert|;
return|return
operator|new
name|AllocationId
argument_list|(
name|allocationId
operator|.
name|getId
argument_list|()
argument_list|,
literal|null
argument_list|)
return|;
block|}
comment|/**      * Creates a new allocation id finalizing a relocation.      *<p>      * Note that this is expected to be called on the allocation id      * of the *target* shard and thus it only needs to clear the relocating id.      */
DECL|method|finishRelocation
specifier|public
specifier|static
name|AllocationId
name|finishRelocation
parameter_list|(
name|AllocationId
name|allocationId
parameter_list|)
block|{
assert|assert
name|allocationId
operator|.
name|getRelocationId
argument_list|()
operator|!=
literal|null
assert|;
return|return
operator|new
name|AllocationId
argument_list|(
name|allocationId
operator|.
name|getId
argument_list|()
argument_list|,
literal|null
argument_list|)
return|;
block|}
comment|/**      * The allocation id uniquely identifying an allocation, note, if it is relocation      * the {@link #getRelocationId()} need to be taken into account as well.      */
DECL|method|getId
specifier|public
name|String
name|getId
parameter_list|()
block|{
return|return
name|id
return|;
block|}
comment|/**      * The transient relocation id holding the unique id that is used for relocation.      */
DECL|method|getRelocationId
specifier|public
name|String
name|getRelocationId
parameter_list|()
block|{
return|return
name|relocationId
return|;
block|}
annotation|@
name|Override
DECL|method|equals
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|o
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
name|o
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
name|AllocationId
name|that
init|=
operator|(
name|AllocationId
operator|)
name|o
decl_stmt|;
if|if
condition|(
operator|!
name|id
operator|.
name|equals
argument_list|(
name|that
operator|.
name|id
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
operator|!
operator|(
name|relocationId
operator|!=
literal|null
condition|?
operator|!
name|relocationId
operator|.
name|equals
argument_list|(
name|that
operator|.
name|relocationId
argument_list|)
else|:
name|that
operator|.
name|relocationId
operator|!=
literal|null
operator|)
return|;
block|}
annotation|@
name|Override
DECL|method|hashCode
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
name|int
name|result
init|=
name|id
operator|.
name|hashCode
argument_list|()
decl_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
operator|(
name|relocationId
operator|!=
literal|null
condition|?
name|relocationId
operator|.
name|hashCode
argument_list|()
else|:
literal|0
operator|)
expr_stmt|;
return|return
name|result
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
return|return
literal|"[id="
operator|+
name|id
operator|+
operator|(
name|relocationId
operator|==
literal|null
condition|?
literal|""
else|:
literal|", rId="
operator|+
name|relocationId
operator|)
operator|+
literal|"]"
return|;
block|}
annotation|@
name|Override
DECL|method|toXContent
specifier|public
name|XContentBuilder
name|toXContent
parameter_list|(
name|XContentBuilder
name|builder
parameter_list|,
name|Params
name|params
parameter_list|)
throws|throws
name|IOException
block|{
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|ID_KEY
argument_list|,
name|id
argument_list|)
expr_stmt|;
if|if
condition|(
name|relocationId
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|RELOCATION_ID_KEY
argument_list|,
name|relocationId
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
return|return
name|builder
return|;
block|}
DECL|method|fromXContent
specifier|public
specifier|static
name|AllocationId
name|fromXContent
parameter_list|(
name|XContentParser
name|parser
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|ALLOCATION_ID_PARSER
operator|.
name|parse
argument_list|(
name|parser
argument_list|,
operator|new
name|AllocationId
operator|.
name|Builder
argument_list|()
argument_list|,
parameter_list|()
lambda|->
name|ParseFieldMatcher
operator|.
name|STRICT
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
block|}
end_class

end_unit

