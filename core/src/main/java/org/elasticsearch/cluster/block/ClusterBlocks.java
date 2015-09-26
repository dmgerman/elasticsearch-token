begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.cluster.block
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|block
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableMap
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
name|AbstractDiffable
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
name|IndexMetaData
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
name|MetaDataIndexStateService
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
name|rest
operator|.
name|RestStatus
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
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
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
name|Set
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|function
operator|.
name|Function
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|function
operator|.
name|Predicate
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
name|Stream
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|util
operator|.
name|Collections
operator|.
name|emptyMap
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|util
operator|.
name|Collections
operator|.
name|emptySet
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|util
operator|.
name|Collections
operator|.
name|unmodifiableSet
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|Collectors
operator|.
name|toSet
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|Stream
operator|.
name|concat
import|;
end_import

begin_comment
comment|/**  * Represents current cluster level blocks to block dirty operations done against the cluster.  */
end_comment

begin_class
DECL|class|ClusterBlocks
specifier|public
class|class
name|ClusterBlocks
extends|extends
name|AbstractDiffable
argument_list|<
name|ClusterBlocks
argument_list|>
block|{
DECL|field|EMPTY_CLUSTER_BLOCK
specifier|public
specifier|static
specifier|final
name|ClusterBlocks
name|EMPTY_CLUSTER_BLOCK
init|=
operator|new
name|ClusterBlocks
argument_list|(
name|emptySet
argument_list|()
argument_list|,
name|emptyMap
argument_list|()
argument_list|)
decl_stmt|;
DECL|field|PROTO
specifier|public
specifier|static
specifier|final
name|ClusterBlocks
name|PROTO
init|=
name|EMPTY_CLUSTER_BLOCK
decl_stmt|;
DECL|field|global
specifier|private
specifier|final
name|Set
argument_list|<
name|ClusterBlock
argument_list|>
name|global
decl_stmt|;
DECL|field|indicesBlocks
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Set
argument_list|<
name|ClusterBlock
argument_list|>
argument_list|>
name|indicesBlocks
decl_stmt|;
DECL|field|levelHolders
specifier|private
specifier|final
name|ImmutableLevelHolder
index|[]
name|levelHolders
decl_stmt|;
DECL|method|ClusterBlocks
name|ClusterBlocks
parameter_list|(
name|Set
argument_list|<
name|ClusterBlock
argument_list|>
name|global
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Set
argument_list|<
name|ClusterBlock
argument_list|>
argument_list|>
name|indicesBlocks
parameter_list|)
block|{
name|this
operator|.
name|global
operator|=
name|global
expr_stmt|;
name|this
operator|.
name|indicesBlocks
operator|=
name|indicesBlocks
expr_stmt|;
name|levelHolders
operator|=
operator|new
name|ImmutableLevelHolder
index|[
name|ClusterBlockLevel
operator|.
name|values
argument_list|()
operator|.
name|length
index|]
expr_stmt|;
for|for
control|(
specifier|final
name|ClusterBlockLevel
name|level
range|:
name|ClusterBlockLevel
operator|.
name|values
argument_list|()
control|)
block|{
name|Predicate
argument_list|<
name|ClusterBlock
argument_list|>
name|containsLevel
init|=
name|block
lambda|->
name|block
operator|.
name|contains
argument_list|(
name|level
argument_list|)
decl_stmt|;
name|Set
argument_list|<
name|ClusterBlock
argument_list|>
name|newGlobal
init|=
name|unmodifiableSet
argument_list|(
name|global
operator|.
name|stream
argument_list|()
operator|.
name|filter
argument_list|(
name|containsLevel
argument_list|)
operator|.
name|collect
argument_list|(
name|toSet
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|ImmutableMap
operator|.
name|Builder
argument_list|<
name|String
argument_list|,
name|Set
argument_list|<
name|ClusterBlock
argument_list|>
argument_list|>
name|indicesBuilder
init|=
name|ImmutableMap
operator|.
name|builder
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Set
argument_list|<
name|ClusterBlock
argument_list|>
argument_list|>
name|entry
range|:
name|indicesBlocks
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|indicesBuilder
operator|.
name|put
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|unmodifiableSet
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|filter
argument_list|(
name|containsLevel
argument_list|)
operator|.
name|collect
argument_list|(
name|toSet
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|levelHolders
index|[
name|level
operator|.
name|id
argument_list|()
index|]
operator|=
operator|new
name|ImmutableLevelHolder
argument_list|(
name|newGlobal
argument_list|,
name|indicesBuilder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|global
specifier|public
name|Set
argument_list|<
name|ClusterBlock
argument_list|>
name|global
parameter_list|()
block|{
return|return
name|global
return|;
block|}
DECL|method|indices
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|Set
argument_list|<
name|ClusterBlock
argument_list|>
argument_list|>
name|indices
parameter_list|()
block|{
return|return
name|indicesBlocks
return|;
block|}
DECL|method|global
specifier|public
name|Set
argument_list|<
name|ClusterBlock
argument_list|>
name|global
parameter_list|(
name|ClusterBlockLevel
name|level
parameter_list|)
block|{
return|return
name|levelHolders
index|[
name|level
operator|.
name|id
argument_list|()
index|]
operator|.
name|global
argument_list|()
return|;
block|}
DECL|method|indices
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|Set
argument_list|<
name|ClusterBlock
argument_list|>
argument_list|>
name|indices
parameter_list|(
name|ClusterBlockLevel
name|level
parameter_list|)
block|{
return|return
name|levelHolders
index|[
name|level
operator|.
name|id
argument_list|()
index|]
operator|.
name|indices
argument_list|()
return|;
block|}
DECL|method|blocksForIndex
specifier|private
name|Set
argument_list|<
name|ClusterBlock
argument_list|>
name|blocksForIndex
parameter_list|(
name|ClusterBlockLevel
name|level
parameter_list|,
name|String
name|index
parameter_list|)
block|{
return|return
name|indices
argument_list|(
name|level
argument_list|)
operator|.
name|getOrDefault
argument_list|(
name|index
argument_list|,
name|emptySet
argument_list|()
argument_list|)
return|;
block|}
comment|/**      * Returns<tt>true</tt> if one of the global blocks as its disable state persistence flag set.      */
DECL|method|disableStatePersistence
specifier|public
name|boolean
name|disableStatePersistence
parameter_list|()
block|{
for|for
control|(
name|ClusterBlock
name|clusterBlock
range|:
name|global
control|)
block|{
if|if
condition|(
name|clusterBlock
operator|.
name|disableStatePersistence
argument_list|()
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
DECL|method|hasGlobalBlock
specifier|public
name|boolean
name|hasGlobalBlock
parameter_list|(
name|ClusterBlock
name|block
parameter_list|)
block|{
return|return
name|global
operator|.
name|contains
argument_list|(
name|block
argument_list|)
return|;
block|}
DECL|method|hasGlobalBlock
specifier|public
name|boolean
name|hasGlobalBlock
parameter_list|(
name|int
name|blockId
parameter_list|)
block|{
for|for
control|(
name|ClusterBlock
name|clusterBlock
range|:
name|global
control|)
block|{
if|if
condition|(
name|clusterBlock
operator|.
name|id
argument_list|()
operator|==
name|blockId
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
DECL|method|hasGlobalBlock
specifier|public
name|boolean
name|hasGlobalBlock
parameter_list|(
name|ClusterBlockLevel
name|level
parameter_list|)
block|{
return|return
name|global
argument_list|(
name|level
argument_list|)
operator|.
name|size
argument_list|()
operator|>
literal|0
return|;
block|}
comment|/**      * Is there a global block with the provided status?      */
DECL|method|hasGlobalBlock
specifier|public
name|boolean
name|hasGlobalBlock
parameter_list|(
name|RestStatus
name|status
parameter_list|)
block|{
for|for
control|(
name|ClusterBlock
name|clusterBlock
range|:
name|global
control|)
block|{
if|if
condition|(
name|clusterBlock
operator|.
name|status
argument_list|()
operator|.
name|equals
argument_list|(
name|status
argument_list|)
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
DECL|method|hasIndexBlock
specifier|public
name|boolean
name|hasIndexBlock
parameter_list|(
name|String
name|index
parameter_list|,
name|ClusterBlock
name|block
parameter_list|)
block|{
return|return
name|indicesBlocks
operator|.
name|containsKey
argument_list|(
name|index
argument_list|)
operator|&&
name|indicesBlocks
operator|.
name|get
argument_list|(
name|index
argument_list|)
operator|.
name|contains
argument_list|(
name|block
argument_list|)
return|;
block|}
DECL|method|globalBlockedRaiseException
specifier|public
name|void
name|globalBlockedRaiseException
parameter_list|(
name|ClusterBlockLevel
name|level
parameter_list|)
throws|throws
name|ClusterBlockException
block|{
name|ClusterBlockException
name|blockException
init|=
name|globalBlockedException
argument_list|(
name|level
argument_list|)
decl_stmt|;
if|if
condition|(
name|blockException
operator|!=
literal|null
condition|)
block|{
throw|throw
name|blockException
throw|;
block|}
block|}
DECL|method|globalBlockedException
specifier|public
name|ClusterBlockException
name|globalBlockedException
parameter_list|(
name|ClusterBlockLevel
name|level
parameter_list|)
block|{
if|if
condition|(
name|global
argument_list|(
name|level
argument_list|)
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
operator|new
name|ClusterBlockException
argument_list|(
name|global
argument_list|(
name|level
argument_list|)
argument_list|)
return|;
block|}
DECL|method|indexBlockedRaiseException
specifier|public
name|void
name|indexBlockedRaiseException
parameter_list|(
name|ClusterBlockLevel
name|level
parameter_list|,
name|String
name|index
parameter_list|)
throws|throws
name|ClusterBlockException
block|{
name|ClusterBlockException
name|blockException
init|=
name|indexBlockedException
argument_list|(
name|level
argument_list|,
name|index
argument_list|)
decl_stmt|;
if|if
condition|(
name|blockException
operator|!=
literal|null
condition|)
block|{
throw|throw
name|blockException
throw|;
block|}
block|}
DECL|method|indexBlockedException
specifier|public
name|ClusterBlockException
name|indexBlockedException
parameter_list|(
name|ClusterBlockLevel
name|level
parameter_list|,
name|String
name|index
parameter_list|)
block|{
if|if
condition|(
operator|!
name|indexBlocked
argument_list|(
name|level
argument_list|,
name|index
argument_list|)
condition|)
block|{
return|return
literal|null
return|;
block|}
name|Stream
argument_list|<
name|ClusterBlock
argument_list|>
name|blocks
init|=
name|concat
argument_list|(
name|global
argument_list|(
name|level
argument_list|)
operator|.
name|stream
argument_list|()
argument_list|,
name|blocksForIndex
argument_list|(
name|level
argument_list|,
name|index
argument_list|)
operator|.
name|stream
argument_list|()
argument_list|)
decl_stmt|;
return|return
operator|new
name|ClusterBlockException
argument_list|(
name|unmodifiableSet
argument_list|(
name|blocks
operator|.
name|collect
argument_list|(
name|toSet
argument_list|()
argument_list|)
argument_list|)
argument_list|)
return|;
block|}
DECL|method|indexBlocked
specifier|public
name|boolean
name|indexBlocked
parameter_list|(
name|ClusterBlockLevel
name|level
parameter_list|,
name|String
name|index
parameter_list|)
block|{
if|if
condition|(
operator|!
name|global
argument_list|(
name|level
argument_list|)
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
literal|true
return|;
block|}
return|return
operator|!
name|blocksForIndex
argument_list|(
name|level
argument_list|,
name|index
argument_list|)
operator|.
name|isEmpty
argument_list|()
return|;
block|}
DECL|method|indicesBlockedException
specifier|public
name|ClusterBlockException
name|indicesBlockedException
parameter_list|(
name|ClusterBlockLevel
name|level
parameter_list|,
name|String
index|[]
name|indices
parameter_list|)
block|{
name|boolean
name|indexIsBlocked
init|=
literal|false
decl_stmt|;
for|for
control|(
name|String
name|index
range|:
name|indices
control|)
block|{
if|if
condition|(
name|indexBlocked
argument_list|(
name|level
argument_list|,
name|index
argument_list|)
condition|)
block|{
name|indexIsBlocked
operator|=
literal|true
expr_stmt|;
block|}
block|}
if|if
condition|(
operator|!
name|indexIsBlocked
condition|)
block|{
return|return
literal|null
return|;
block|}
name|Function
argument_list|<
name|String
argument_list|,
name|Stream
argument_list|<
name|ClusterBlock
argument_list|>
argument_list|>
name|blocksForIndexAtLevel
init|=
name|index
lambda|->
name|blocksForIndex
argument_list|(
name|level
argument_list|,
name|index
argument_list|)
operator|.
name|stream
argument_list|()
decl_stmt|;
name|Stream
argument_list|<
name|ClusterBlock
argument_list|>
name|blocks
init|=
name|concat
argument_list|(
name|global
argument_list|(
name|level
argument_list|)
operator|.
name|stream
argument_list|()
argument_list|,
name|Stream
operator|.
name|of
argument_list|(
name|indices
argument_list|)
operator|.
name|flatMap
argument_list|(
name|blocksForIndexAtLevel
argument_list|)
argument_list|)
decl_stmt|;
return|return
operator|new
name|ClusterBlockException
argument_list|(
name|unmodifiableSet
argument_list|(
name|blocks
operator|.
name|collect
argument_list|(
name|toSet
argument_list|()
argument_list|)
argument_list|)
argument_list|)
return|;
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
name|writeBlockSet
argument_list|(
name|global
argument_list|,
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|indicesBlocks
operator|.
name|size
argument_list|()
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
name|Set
argument_list|<
name|ClusterBlock
argument_list|>
argument_list|>
name|entry
range|:
name|indicesBlocks
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|out
operator|.
name|writeString
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
name|writeBlockSet
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|,
name|out
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|writeBlockSet
specifier|private
specifier|static
name|void
name|writeBlockSet
parameter_list|(
name|Set
argument_list|<
name|ClusterBlock
argument_list|>
name|blocks
parameter_list|,
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeVInt
argument_list|(
name|blocks
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|ClusterBlock
name|block
range|:
name|blocks
control|)
block|{
name|block
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
DECL|method|readFrom
specifier|public
name|ClusterBlocks
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|Set
argument_list|<
name|ClusterBlock
argument_list|>
name|global
init|=
name|readBlockSet
argument_list|(
name|in
argument_list|)
decl_stmt|;
name|ImmutableMap
operator|.
name|Builder
argument_list|<
name|String
argument_list|,
name|Set
argument_list|<
name|ClusterBlock
argument_list|>
argument_list|>
name|indicesBuilder
init|=
name|ImmutableMap
operator|.
name|builder
argument_list|()
decl_stmt|;
name|int
name|size
init|=
name|in
operator|.
name|readVInt
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|size
condition|;
name|j
operator|++
control|)
block|{
name|indicesBuilder
operator|.
name|put
argument_list|(
name|in
operator|.
name|readString
argument_list|()
operator|.
name|intern
argument_list|()
argument_list|,
name|readBlockSet
argument_list|(
name|in
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|ClusterBlocks
argument_list|(
name|global
argument_list|,
name|indicesBuilder
operator|.
name|build
argument_list|()
argument_list|)
return|;
block|}
DECL|method|readBlockSet
specifier|private
specifier|static
name|Set
argument_list|<
name|ClusterBlock
argument_list|>
name|readBlockSet
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|totalBlocks
init|=
name|in
operator|.
name|readVInt
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|ClusterBlock
argument_list|>
name|blocks
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|totalBlocks
argument_list|)
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
name|totalBlocks
condition|;
name|i
operator|++
control|)
block|{
name|blocks
operator|.
name|add
argument_list|(
name|ClusterBlock
operator|.
name|readClusterBlock
argument_list|(
name|in
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|unmodifiableSet
argument_list|(
name|blocks
argument_list|)
return|;
block|}
DECL|class|ImmutableLevelHolder
specifier|static
class|class
name|ImmutableLevelHolder
block|{
DECL|field|EMPTY
specifier|static
specifier|final
name|ImmutableLevelHolder
name|EMPTY
init|=
operator|new
name|ImmutableLevelHolder
argument_list|(
name|emptySet
argument_list|()
argument_list|,
name|ImmutableMap
operator|.
name|of
argument_list|()
argument_list|)
decl_stmt|;
DECL|field|global
specifier|private
specifier|final
name|Set
argument_list|<
name|ClusterBlock
argument_list|>
name|global
decl_stmt|;
DECL|field|indices
specifier|private
specifier|final
name|ImmutableMap
argument_list|<
name|String
argument_list|,
name|Set
argument_list|<
name|ClusterBlock
argument_list|>
argument_list|>
name|indices
decl_stmt|;
DECL|method|ImmutableLevelHolder
name|ImmutableLevelHolder
parameter_list|(
name|Set
argument_list|<
name|ClusterBlock
argument_list|>
name|global
parameter_list|,
name|ImmutableMap
argument_list|<
name|String
argument_list|,
name|Set
argument_list|<
name|ClusterBlock
argument_list|>
argument_list|>
name|indices
parameter_list|)
block|{
name|this
operator|.
name|global
operator|=
name|global
expr_stmt|;
name|this
operator|.
name|indices
operator|=
name|indices
expr_stmt|;
block|}
DECL|method|global
specifier|public
name|Set
argument_list|<
name|ClusterBlock
argument_list|>
name|global
parameter_list|()
block|{
return|return
name|global
return|;
block|}
DECL|method|indices
specifier|public
name|ImmutableMap
argument_list|<
name|String
argument_list|,
name|Set
argument_list|<
name|ClusterBlock
argument_list|>
argument_list|>
name|indices
parameter_list|()
block|{
return|return
name|indices
return|;
block|}
block|}
DECL|method|builder
specifier|public
specifier|static
name|Builder
name|builder
parameter_list|()
block|{
return|return
operator|new
name|Builder
argument_list|()
return|;
block|}
DECL|class|Builder
specifier|public
specifier|static
class|class
name|Builder
block|{
DECL|field|global
specifier|private
name|Set
argument_list|<
name|ClusterBlock
argument_list|>
name|global
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
DECL|field|indices
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|Set
argument_list|<
name|ClusterBlock
argument_list|>
argument_list|>
name|indices
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
DECL|method|Builder
specifier|public
name|Builder
parameter_list|()
block|{         }
DECL|method|blocks
specifier|public
name|Builder
name|blocks
parameter_list|(
name|ClusterBlocks
name|blocks
parameter_list|)
block|{
name|global
operator|.
name|addAll
argument_list|(
name|blocks
operator|.
name|global
argument_list|()
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
name|Set
argument_list|<
name|ClusterBlock
argument_list|>
argument_list|>
name|entry
range|:
name|blocks
operator|.
name|indices
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|indices
operator|.
name|containsKey
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
condition|)
block|{
name|indices
operator|.
name|put
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
operator|new
name|HashSet
argument_list|<>
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|indices
operator|.
name|get
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
operator|.
name|addAll
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|this
return|;
block|}
DECL|method|addBlocks
specifier|public
name|Builder
name|addBlocks
parameter_list|(
name|IndexMetaData
name|indexMetaData
parameter_list|)
block|{
if|if
condition|(
name|indexMetaData
operator|.
name|state
argument_list|()
operator|==
name|IndexMetaData
operator|.
name|State
operator|.
name|CLOSE
condition|)
block|{
name|addIndexBlock
argument_list|(
name|indexMetaData
operator|.
name|index
argument_list|()
argument_list|,
name|MetaDataIndexStateService
operator|.
name|INDEX_CLOSED_BLOCK
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|indexMetaData
operator|.
name|settings
argument_list|()
operator|.
name|getAsBoolean
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_READ_ONLY
argument_list|,
literal|false
argument_list|)
condition|)
block|{
name|addIndexBlock
argument_list|(
name|indexMetaData
operator|.
name|index
argument_list|()
argument_list|,
name|IndexMetaData
operator|.
name|INDEX_READ_ONLY_BLOCK
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|indexMetaData
operator|.
name|settings
argument_list|()
operator|.
name|getAsBoolean
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_BLOCKS_READ
argument_list|,
literal|false
argument_list|)
condition|)
block|{
name|addIndexBlock
argument_list|(
name|indexMetaData
operator|.
name|index
argument_list|()
argument_list|,
name|IndexMetaData
operator|.
name|INDEX_READ_BLOCK
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|indexMetaData
operator|.
name|settings
argument_list|()
operator|.
name|getAsBoolean
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_BLOCKS_WRITE
argument_list|,
literal|false
argument_list|)
condition|)
block|{
name|addIndexBlock
argument_list|(
name|indexMetaData
operator|.
name|index
argument_list|()
argument_list|,
name|IndexMetaData
operator|.
name|INDEX_WRITE_BLOCK
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|indexMetaData
operator|.
name|settings
argument_list|()
operator|.
name|getAsBoolean
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_BLOCKS_METADATA
argument_list|,
literal|false
argument_list|)
condition|)
block|{
name|addIndexBlock
argument_list|(
name|indexMetaData
operator|.
name|index
argument_list|()
argument_list|,
name|IndexMetaData
operator|.
name|INDEX_METADATA_BLOCK
argument_list|)
expr_stmt|;
block|}
return|return
name|this
return|;
block|}
DECL|method|addGlobalBlock
specifier|public
name|Builder
name|addGlobalBlock
parameter_list|(
name|ClusterBlock
name|block
parameter_list|)
block|{
name|global
operator|.
name|add
argument_list|(
name|block
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|removeGlobalBlock
specifier|public
name|Builder
name|removeGlobalBlock
parameter_list|(
name|ClusterBlock
name|block
parameter_list|)
block|{
name|global
operator|.
name|remove
argument_list|(
name|block
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|addIndexBlock
specifier|public
name|Builder
name|addIndexBlock
parameter_list|(
name|String
name|index
parameter_list|,
name|ClusterBlock
name|block
parameter_list|)
block|{
if|if
condition|(
operator|!
name|indices
operator|.
name|containsKey
argument_list|(
name|index
argument_list|)
condition|)
block|{
name|indices
operator|.
name|put
argument_list|(
name|index
argument_list|,
operator|new
name|HashSet
argument_list|<>
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|indices
operator|.
name|get
argument_list|(
name|index
argument_list|)
operator|.
name|add
argument_list|(
name|block
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|removeIndexBlocks
specifier|public
name|Builder
name|removeIndexBlocks
parameter_list|(
name|String
name|index
parameter_list|)
block|{
if|if
condition|(
operator|!
name|indices
operator|.
name|containsKey
argument_list|(
name|index
argument_list|)
condition|)
block|{
return|return
name|this
return|;
block|}
name|indices
operator|.
name|remove
argument_list|(
name|index
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|removeIndexBlock
specifier|public
name|Builder
name|removeIndexBlock
parameter_list|(
name|String
name|index
parameter_list|,
name|ClusterBlock
name|block
parameter_list|)
block|{
if|if
condition|(
operator|!
name|indices
operator|.
name|containsKey
argument_list|(
name|index
argument_list|)
condition|)
block|{
return|return
name|this
return|;
block|}
name|indices
operator|.
name|get
argument_list|(
name|index
argument_list|)
operator|.
name|remove
argument_list|(
name|block
argument_list|)
expr_stmt|;
if|if
condition|(
name|indices
operator|.
name|get
argument_list|(
name|index
argument_list|)
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|indices
operator|.
name|remove
argument_list|(
name|index
argument_list|)
expr_stmt|;
block|}
return|return
name|this
return|;
block|}
DECL|method|build
specifier|public
name|ClusterBlocks
name|build
parameter_list|()
block|{
comment|// We copy the block sets here in case of the builder is modified after build is called
name|ImmutableMap
operator|.
name|Builder
argument_list|<
name|String
argument_list|,
name|Set
argument_list|<
name|ClusterBlock
argument_list|>
argument_list|>
name|indicesBuilder
init|=
name|ImmutableMap
operator|.
name|builder
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Set
argument_list|<
name|ClusterBlock
argument_list|>
argument_list|>
name|entry
range|:
name|indices
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|indicesBuilder
operator|.
name|put
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|unmodifiableSet
argument_list|(
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|ClusterBlocks
argument_list|(
name|unmodifiableSet
argument_list|(
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|global
argument_list|)
argument_list|)
argument_list|,
name|indicesBuilder
operator|.
name|build
argument_list|()
argument_list|)
return|;
block|}
DECL|method|readClusterBlocks
specifier|public
specifier|static
name|ClusterBlocks
name|readClusterBlocks
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|PROTO
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

