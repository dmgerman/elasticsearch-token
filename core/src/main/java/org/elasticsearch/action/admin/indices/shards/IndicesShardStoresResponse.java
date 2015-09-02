begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.indices.shards
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|admin
operator|.
name|indices
operator|.
name|shards
package|;
end_package

begin_import
import|import
name|com
operator|.
name|carrotsearch
operator|.
name|hppc
operator|.
name|cursors
operator|.
name|IntObjectCursor
import|;
end_import

begin_import
import|import
name|com
operator|.
name|carrotsearch
operator|.
name|hppc
operator|.
name|cursors
operator|.
name|ObjectObjectCursor
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchException
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
name|ActionResponse
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
name|ShardOperationFailedException
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
name|DefaultShardOperationFailedException
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
name|DiscoveryNode
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
name|collect
operator|.
name|ImmutableOpenIntMap
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
name|collect
operator|.
name|ImmutableOpenMap
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
name|Streamable
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
name|XContentBuilderString
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
name|ArrayList
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
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|admin
operator|.
name|indices
operator|.
name|shards
operator|.
name|IndicesShardStoresResponse
operator|.
name|StoreStatus
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * Response for {@link IndicesShardStoresAction}  *  * Consists of {@link StoreStatus}s for requested indices grouped by  * indices and shard ids and a list of encountered node {@link Failure}s  */
end_comment

begin_class
DECL|class|IndicesShardStoresResponse
specifier|public
class|class
name|IndicesShardStoresResponse
extends|extends
name|ActionResponse
implements|implements
name|ToXContent
block|{
comment|/**      * Shard store information from a node      */
DECL|class|StoreStatus
specifier|public
specifier|static
class|class
name|StoreStatus
implements|implements
name|Streamable
implements|,
name|ToXContent
implements|,
name|Comparable
argument_list|<
name|StoreStatus
argument_list|>
block|{
DECL|field|node
specifier|private
name|DiscoveryNode
name|node
decl_stmt|;
DECL|field|version
specifier|private
name|long
name|version
decl_stmt|;
DECL|field|storeException
specifier|private
name|Throwable
name|storeException
decl_stmt|;
DECL|field|allocation
specifier|private
name|Allocation
name|allocation
decl_stmt|;
comment|/**          * The status of the shard store with respect to the cluster          */
DECL|enum|Allocation
specifier|public
enum|enum
name|Allocation
block|{
comment|/**              * Allocated as primary              */
DECL|enum constant|PRIMARY
name|PRIMARY
argument_list|(
operator|(
name|byte
operator|)
literal|0
argument_list|)
block|,
comment|/**              * Allocated as a replica              */
DECL|enum constant|REPLICA
name|REPLICA
argument_list|(
operator|(
name|byte
operator|)
literal|1
argument_list|)
block|,
comment|/**              * Not allocated              */
DECL|enum constant|UNUSED
name|UNUSED
argument_list|(
operator|(
name|byte
operator|)
literal|2
argument_list|)
block|;
DECL|field|id
specifier|private
specifier|final
name|byte
name|id
decl_stmt|;
DECL|method|Allocation
name|Allocation
parameter_list|(
name|byte
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
DECL|method|fromId
specifier|private
specifier|static
name|Allocation
name|fromId
parameter_list|(
name|byte
name|id
parameter_list|)
block|{
switch|switch
condition|(
name|id
condition|)
block|{
case|case
literal|0
case|:
return|return
name|PRIMARY
return|;
case|case
literal|1
case|:
return|return
name|REPLICA
return|;
case|case
literal|2
case|:
return|return
name|UNUSED
return|;
default|default:
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"unknown id for allocation ["
operator|+
name|id
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
DECL|method|value
specifier|public
name|String
name|value
parameter_list|()
block|{
switch|switch
condition|(
name|id
condition|)
block|{
case|case
literal|0
case|:
return|return
literal|"primary"
return|;
case|case
literal|1
case|:
return|return
literal|"replica"
return|;
case|case
literal|2
case|:
return|return
literal|"unused"
return|;
default|default:
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"unknown id for allocation ["
operator|+
name|id
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
DECL|method|readFrom
specifier|private
specifier|static
name|Allocation
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|fromId
argument_list|(
name|in
operator|.
name|readByte
argument_list|()
argument_list|)
return|;
block|}
DECL|method|writeTo
specifier|private
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
name|writeByte
argument_list|(
name|id
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|StoreStatus
specifier|private
name|StoreStatus
parameter_list|()
block|{         }
DECL|method|StoreStatus
specifier|public
name|StoreStatus
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|,
name|long
name|version
parameter_list|,
name|Allocation
name|allocation
parameter_list|,
name|Throwable
name|storeException
parameter_list|)
block|{
name|this
operator|.
name|node
operator|=
name|node
expr_stmt|;
name|this
operator|.
name|version
operator|=
name|version
expr_stmt|;
name|this
operator|.
name|allocation
operator|=
name|allocation
expr_stmt|;
name|this
operator|.
name|storeException
operator|=
name|storeException
expr_stmt|;
block|}
comment|/**          * Node the store belongs to          */
DECL|method|getNode
specifier|public
name|DiscoveryNode
name|getNode
parameter_list|()
block|{
return|return
name|node
return|;
block|}
comment|/**          * Version of the store, used to select the store that will be          * used as a primary.          */
DECL|method|getVersion
specifier|public
name|long
name|getVersion
parameter_list|()
block|{
return|return
name|version
return|;
block|}
comment|/**          * Exception while trying to open the          * shard index or from when the shard failed          */
DECL|method|getStoreException
specifier|public
name|Throwable
name|getStoreException
parameter_list|()
block|{
return|return
name|storeException
return|;
block|}
comment|/**          * The allocation status of the store.          * {@link Allocation#PRIMARY} indicates a primary shard copy          * {@link Allocation#REPLICA} indicates a replica shard copy          * {@link Allocation#UNUSED} indicates an unused shard copy          */
DECL|method|getAllocation
specifier|public
name|Allocation
name|getAllocation
parameter_list|()
block|{
return|return
name|allocation
return|;
block|}
DECL|method|readStoreStatus
specifier|static
name|StoreStatus
name|readStoreStatus
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|StoreStatus
name|storeStatus
init|=
operator|new
name|StoreStatus
argument_list|()
decl_stmt|;
name|storeStatus
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
return|return
name|storeStatus
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
name|node
operator|=
name|DiscoveryNode
operator|.
name|readNode
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|version
operator|=
name|in
operator|.
name|readLong
argument_list|()
expr_stmt|;
name|allocation
operator|=
name|Allocation
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
if|if
condition|(
name|in
operator|.
name|readBoolean
argument_list|()
condition|)
block|{
name|storeException
operator|=
name|in
operator|.
name|readThrowable
argument_list|()
expr_stmt|;
block|}
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
name|node
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeLong
argument_list|(
name|version
argument_list|)
expr_stmt|;
name|allocation
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
if|if
condition|(
name|storeException
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
name|writeThrowable
argument_list|(
name|storeException
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
name|node
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|VERSION
argument_list|,
name|version
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|ALLOCATED
argument_list|,
name|allocation
operator|.
name|value
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|storeException
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|startObject
argument_list|(
name|Fields
operator|.
name|STORE_EXCEPTION
argument_list|)
expr_stmt|;
name|ElasticsearchException
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|,
name|storeException
argument_list|)
expr_stmt|;
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
annotation|@
name|Override
DECL|method|compareTo
specifier|public
name|int
name|compareTo
parameter_list|(
name|StoreStatus
name|other
parameter_list|)
block|{
if|if
condition|(
name|storeException
operator|!=
literal|null
operator|&&
name|other
operator|.
name|storeException
operator|==
literal|null
condition|)
block|{
return|return
literal|1
return|;
block|}
elseif|else
if|if
condition|(
name|other
operator|.
name|storeException
operator|!=
literal|null
operator|&&
name|storeException
operator|==
literal|null
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
else|else
block|{
name|int
name|compare
init|=
name|Long
operator|.
name|compare
argument_list|(
name|other
operator|.
name|version
argument_list|,
name|version
argument_list|)
decl_stmt|;
if|if
condition|(
name|compare
operator|==
literal|0
condition|)
block|{
return|return
name|Integer
operator|.
name|compare
argument_list|(
name|allocation
operator|.
name|id
argument_list|,
name|other
operator|.
name|allocation
operator|.
name|id
argument_list|)
return|;
block|}
return|return
name|compare
return|;
block|}
block|}
block|}
comment|/**      * Single node failure while retrieving shard store information      */
DECL|class|Failure
specifier|public
specifier|static
class|class
name|Failure
extends|extends
name|DefaultShardOperationFailedException
block|{
DECL|field|nodeId
specifier|private
name|String
name|nodeId
decl_stmt|;
DECL|method|Failure
specifier|public
name|Failure
parameter_list|(
name|String
name|nodeId
parameter_list|,
name|String
name|index
parameter_list|,
name|int
name|shardId
parameter_list|,
name|Throwable
name|reason
parameter_list|)
block|{
name|super
argument_list|(
name|index
argument_list|,
name|shardId
argument_list|,
name|reason
argument_list|)
expr_stmt|;
name|this
operator|.
name|nodeId
operator|=
name|nodeId
expr_stmt|;
block|}
DECL|method|Failure
specifier|private
name|Failure
parameter_list|()
block|{         }
DECL|method|nodeId
specifier|public
name|String
name|nodeId
parameter_list|()
block|{
return|return
name|nodeId
return|;
block|}
DECL|method|readFailure
specifier|public
specifier|static
name|Failure
name|readFailure
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|Failure
name|failure
init|=
operator|new
name|Failure
argument_list|()
decl_stmt|;
name|failure
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
return|return
name|failure
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
name|nodeId
operator|=
name|in
operator|.
name|readString
argument_list|()
expr_stmt|;
name|super
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
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
name|nodeId
argument_list|)
expr_stmt|;
name|super
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
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
name|field
argument_list|(
literal|"node"
argument_list|,
name|nodeId
argument_list|()
argument_list|)
expr_stmt|;
name|super
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
return|return
name|builder
return|;
block|}
block|}
DECL|field|storeStatuses
specifier|private
name|ImmutableOpenMap
argument_list|<
name|String
argument_list|,
name|ImmutableOpenIntMap
argument_list|<
name|List
argument_list|<
name|StoreStatus
argument_list|>
argument_list|>
argument_list|>
name|storeStatuses
decl_stmt|;
DECL|field|failures
specifier|private
name|List
argument_list|<
name|Failure
argument_list|>
name|failures
decl_stmt|;
DECL|method|IndicesShardStoresResponse
specifier|public
name|IndicesShardStoresResponse
parameter_list|(
name|ImmutableOpenMap
argument_list|<
name|String
argument_list|,
name|ImmutableOpenIntMap
argument_list|<
name|List
argument_list|<
name|StoreStatus
argument_list|>
argument_list|>
argument_list|>
name|storeStatuses
parameter_list|,
name|List
argument_list|<
name|Failure
argument_list|>
name|failures
parameter_list|)
block|{
name|this
operator|.
name|storeStatuses
operator|=
name|storeStatuses
expr_stmt|;
name|this
operator|.
name|failures
operator|=
name|failures
expr_stmt|;
block|}
DECL|method|IndicesShardStoresResponse
name|IndicesShardStoresResponse
parameter_list|()
block|{
name|this
argument_list|(
name|ImmutableOpenMap
operator|.
expr|<
name|String
argument_list|,
name|ImmutableOpenIntMap
argument_list|<
name|List
argument_list|<
name|StoreStatus
argument_list|>
argument_list|>
operator|>
name|of
argument_list|()
argument_list|,
name|Collections
operator|.
expr|<
name|Failure
operator|>
name|emptyList
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**      * Returns {@link StoreStatus}s      * grouped by their index names and shard ids.      */
DECL|method|getStoreStatuses
specifier|public
name|ImmutableOpenMap
argument_list|<
name|String
argument_list|,
name|ImmutableOpenIntMap
argument_list|<
name|List
argument_list|<
name|StoreStatus
argument_list|>
argument_list|>
argument_list|>
name|getStoreStatuses
parameter_list|()
block|{
return|return
name|storeStatuses
return|;
block|}
comment|/**      * Returns node {@link Failure}s encountered      * while executing the request      */
DECL|method|getFailures
specifier|public
name|List
argument_list|<
name|Failure
argument_list|>
name|getFailures
parameter_list|()
block|{
return|return
name|failures
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
name|super
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|int
name|numResponse
init|=
name|in
operator|.
name|readVInt
argument_list|()
decl_stmt|;
name|ImmutableOpenMap
operator|.
name|Builder
argument_list|<
name|String
argument_list|,
name|ImmutableOpenIntMap
argument_list|<
name|List
argument_list|<
name|StoreStatus
argument_list|>
argument_list|>
argument_list|>
name|storeStatusesBuilder
init|=
name|ImmutableOpenMap
operator|.
name|builder
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numResponse
condition|;
name|i
operator|++
control|)
block|{
name|String
name|index
init|=
name|in
operator|.
name|readString
argument_list|()
decl_stmt|;
name|int
name|indexEntries
init|=
name|in
operator|.
name|readVInt
argument_list|()
decl_stmt|;
name|ImmutableOpenIntMap
operator|.
name|Builder
argument_list|<
name|List
argument_list|<
name|StoreStatus
argument_list|>
argument_list|>
name|shardEntries
init|=
name|ImmutableOpenIntMap
operator|.
name|builder
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|shardCount
init|=
literal|0
init|;
name|shardCount
operator|<
name|indexEntries
condition|;
name|shardCount
operator|++
control|)
block|{
name|int
name|shardID
init|=
name|in
operator|.
name|readInt
argument_list|()
decl_stmt|;
name|int
name|nodeEntries
init|=
name|in
operator|.
name|readVInt
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|StoreStatus
argument_list|>
name|storeStatuses
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|nodeEntries
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|nodeCount
init|=
literal|0
init|;
name|nodeCount
operator|<
name|nodeEntries
condition|;
name|nodeCount
operator|++
control|)
block|{
name|storeStatuses
operator|.
name|add
argument_list|(
name|readStoreStatus
argument_list|(
name|in
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|shardEntries
operator|.
name|put
argument_list|(
name|shardID
argument_list|,
name|storeStatuses
argument_list|)
expr_stmt|;
block|}
name|storeStatusesBuilder
operator|.
name|put
argument_list|(
name|index
argument_list|,
name|shardEntries
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|int
name|numFailure
init|=
name|in
operator|.
name|readVInt
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Failure
argument_list|>
name|failureBuilder
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numFailure
condition|;
name|i
operator|++
control|)
block|{
name|failureBuilder
operator|.
name|add
argument_list|(
name|Failure
operator|.
name|readFailure
argument_list|(
name|in
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|storeStatuses
operator|=
name|storeStatusesBuilder
operator|.
name|build
argument_list|()
expr_stmt|;
name|failures
operator|=
name|Collections
operator|.
name|unmodifiableList
argument_list|(
name|failureBuilder
argument_list|)
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
name|super
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|storeStatuses
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|ObjectObjectCursor
argument_list|<
name|String
argument_list|,
name|ImmutableOpenIntMap
argument_list|<
name|List
argument_list|<
name|StoreStatus
argument_list|>
argument_list|>
argument_list|>
name|indexShards
range|:
name|storeStatuses
control|)
block|{
name|out
operator|.
name|writeString
argument_list|(
name|indexShards
operator|.
name|key
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|indexShards
operator|.
name|value
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|IntObjectCursor
argument_list|<
name|List
argument_list|<
name|StoreStatus
argument_list|>
argument_list|>
name|shardStatusesEntry
range|:
name|indexShards
operator|.
name|value
control|)
block|{
name|out
operator|.
name|writeInt
argument_list|(
name|shardStatusesEntry
operator|.
name|key
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|shardStatusesEntry
operator|.
name|value
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|StoreStatus
name|storeStatus
range|:
name|shardStatusesEntry
operator|.
name|value
control|)
block|{
name|storeStatus
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|out
operator|.
name|writeVInt
argument_list|(
name|failures
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|ShardOperationFailedException
name|failure
range|:
name|failures
control|)
block|{
name|failure
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
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
if|if
condition|(
name|failures
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|builder
operator|.
name|startArray
argument_list|(
name|Fields
operator|.
name|FAILURES
argument_list|)
expr_stmt|;
for|for
control|(
name|ShardOperationFailedException
name|failure
range|:
name|failures
control|)
block|{
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|failure
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
name|builder
operator|.
name|endArray
argument_list|()
expr_stmt|;
block|}
name|builder
operator|.
name|startObject
argument_list|(
name|Fields
operator|.
name|INDICES
argument_list|)
expr_stmt|;
for|for
control|(
name|ObjectObjectCursor
argument_list|<
name|String
argument_list|,
name|ImmutableOpenIntMap
argument_list|<
name|List
argument_list|<
name|StoreStatus
argument_list|>
argument_list|>
argument_list|>
name|indexShards
range|:
name|storeStatuses
control|)
block|{
name|builder
operator|.
name|startObject
argument_list|(
name|indexShards
operator|.
name|key
argument_list|)
expr_stmt|;
name|builder
operator|.
name|startObject
argument_list|(
name|Fields
operator|.
name|SHARDS
argument_list|)
expr_stmt|;
for|for
control|(
name|IntObjectCursor
argument_list|<
name|List
argument_list|<
name|StoreStatus
argument_list|>
argument_list|>
name|shardStatusesEntry
range|:
name|indexShards
operator|.
name|value
control|)
block|{
name|builder
operator|.
name|startObject
argument_list|(
name|String
operator|.
name|valueOf
argument_list|(
name|shardStatusesEntry
operator|.
name|key
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|.
name|startArray
argument_list|(
name|Fields
operator|.
name|STORES
argument_list|)
expr_stmt|;
for|for
control|(
name|StoreStatus
name|storeStatus
range|:
name|shardStatusesEntry
operator|.
name|value
control|)
block|{
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|storeStatus
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
name|builder
operator|.
name|endArray
argument_list|()
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
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
DECL|class|Fields
specifier|static
specifier|final
class|class
name|Fields
block|{
DECL|field|INDICES
specifier|static
specifier|final
name|XContentBuilderString
name|INDICES
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"indices"
argument_list|)
decl_stmt|;
DECL|field|SHARDS
specifier|static
specifier|final
name|XContentBuilderString
name|SHARDS
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"shards"
argument_list|)
decl_stmt|;
DECL|field|FAILURES
specifier|static
specifier|final
name|XContentBuilderString
name|FAILURES
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"failures"
argument_list|)
decl_stmt|;
DECL|field|STORES
specifier|static
specifier|final
name|XContentBuilderString
name|STORES
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"stores"
argument_list|)
decl_stmt|;
comment|// StoreStatus fields
DECL|field|VERSION
specifier|static
specifier|final
name|XContentBuilderString
name|VERSION
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"version"
argument_list|)
decl_stmt|;
DECL|field|STORE_EXCEPTION
specifier|static
specifier|final
name|XContentBuilderString
name|STORE_EXCEPTION
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"store_exception"
argument_list|)
decl_stmt|;
DECL|field|ALLOCATED
specifier|static
specifier|final
name|XContentBuilderString
name|ALLOCATED
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"allocation"
argument_list|)
decl_stmt|;
block|}
block|}
end_class

end_unit

