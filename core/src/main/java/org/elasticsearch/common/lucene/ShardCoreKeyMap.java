begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.lucene
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|lucene
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|index
operator|.
name|LeafReader
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
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|shard
operator|.
name|ShardUtils
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
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
name|IdentityHashMap
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

begin_comment
comment|/**  * A map between segment core cache keys and the shard that these segments  * belong to. This allows to get the shard that a segment belongs to or to get  * the entire set of live core cache keys for a given index. In order to work  * this class needs to be notified about new segments. It modifies the current  * mappings as segments that were not known before are added and prevents the  * structure from growing indefinitely by registering close listeners on these  * segments so that at any time it only tracks live segments.  *  * NOTE: This is heavy. Avoid using this class unless absolutely required.  */
end_comment

begin_class
DECL|class|ShardCoreKeyMap
specifier|public
specifier|final
class|class
name|ShardCoreKeyMap
block|{
DECL|field|coreKeyToShard
specifier|private
specifier|final
name|Map
argument_list|<
name|Object
argument_list|,
name|ShardId
argument_list|>
name|coreKeyToShard
decl_stmt|;
DECL|field|indexToCoreKey
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Set
argument_list|<
name|Object
argument_list|>
argument_list|>
name|indexToCoreKey
decl_stmt|;
DECL|method|ShardCoreKeyMap
specifier|public
name|ShardCoreKeyMap
parameter_list|()
block|{
name|coreKeyToShard
operator|=
operator|new
name|IdentityHashMap
argument_list|<>
argument_list|()
expr_stmt|;
name|indexToCoreKey
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
expr_stmt|;
block|}
comment|/**      * Register a {@link LeafReader}. This is necessary so that the core cache      * key of this reader can be found later using {@link #getCoreKeysForIndex(String)}.      */
DECL|method|add
specifier|public
name|void
name|add
parameter_list|(
name|LeafReader
name|reader
parameter_list|)
block|{
specifier|final
name|ShardId
name|shardId
init|=
name|ShardUtils
operator|.
name|extractShardId
argument_list|(
name|reader
argument_list|)
decl_stmt|;
if|if
condition|(
name|shardId
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Could not extract shard id from "
operator|+
name|reader
argument_list|)
throw|;
block|}
specifier|final
name|Object
name|coreKey
init|=
name|reader
operator|.
name|getCoreCacheKey
argument_list|()
decl_stmt|;
specifier|final
name|String
name|index
init|=
name|shardId
operator|.
name|getIndex
argument_list|()
decl_stmt|;
synchronized|synchronized
init|(
name|this
init|)
block|{
if|if
condition|(
name|coreKeyToShard
operator|.
name|put
argument_list|(
name|coreKey
argument_list|,
name|shardId
argument_list|)
operator|==
literal|null
condition|)
block|{
name|Set
argument_list|<
name|Object
argument_list|>
name|objects
init|=
name|indexToCoreKey
operator|.
name|get
argument_list|(
name|index
argument_list|)
decl_stmt|;
if|if
condition|(
name|objects
operator|==
literal|null
condition|)
block|{
name|objects
operator|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
expr_stmt|;
name|indexToCoreKey
operator|.
name|put
argument_list|(
name|index
argument_list|,
name|objects
argument_list|)
expr_stmt|;
block|}
specifier|final
name|boolean
name|added
init|=
name|objects
operator|.
name|add
argument_list|(
name|coreKey
argument_list|)
decl_stmt|;
assert|assert
name|added
assert|;
name|reader
operator|.
name|addCoreClosedListener
argument_list|(
name|ownerCoreCacheKey
lambda|->
block|{
assert|assert
name|coreKey
operator|==
name|ownerCoreCacheKey
assert|;
synchronized|synchronized
init|(
name|ShardCoreKeyMap
operator|.
name|this
init|)
block|{
name|coreKeyToShard
operator|.
name|remove
argument_list|(
name|ownerCoreCacheKey
argument_list|)
expr_stmt|;
specifier|final
name|Set
argument_list|<
name|Object
argument_list|>
name|coreKeys
init|=
name|indexToCoreKey
operator|.
name|get
argument_list|(
name|index
argument_list|)
decl_stmt|;
specifier|final
name|boolean
name|removed
init|=
name|coreKeys
operator|.
name|remove
argument_list|(
name|coreKey
argument_list|)
decl_stmt|;
assert|assert
name|removed
assert|;
if|if
condition|(
name|coreKeys
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|indexToCoreKey
operator|.
name|remove
argument_list|(
name|index
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**      * Return the {@link ShardId} that holds the given segment, or {@code null}      * if this segment is not tracked.      */
DECL|method|getShardId
specifier|public
specifier|synchronized
name|ShardId
name|getShardId
parameter_list|(
name|Object
name|coreKey
parameter_list|)
block|{
return|return
name|coreKeyToShard
operator|.
name|get
argument_list|(
name|coreKey
argument_list|)
return|;
block|}
comment|/**      * Get the set of core cache keys associated with the given index.      */
DECL|method|getCoreKeysForIndex
specifier|public
specifier|synchronized
name|Set
argument_list|<
name|Object
argument_list|>
name|getCoreKeysForIndex
parameter_list|(
name|String
name|index
parameter_list|)
block|{
specifier|final
name|Set
argument_list|<
name|Object
argument_list|>
name|objects
init|=
name|indexToCoreKey
operator|.
name|get
argument_list|(
name|index
argument_list|)
decl_stmt|;
if|if
condition|(
name|objects
operator|==
literal|null
condition|)
block|{
return|return
name|Collections
operator|.
name|emptySet
argument_list|()
return|;
block|}
comment|// we have to copy otherwise we risk ConcurrentModificationException
return|return
name|Collections
operator|.
name|unmodifiableSet
argument_list|(
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|objects
argument_list|)
argument_list|)
return|;
block|}
comment|/**      * Return the number of tracked segments.      */
DECL|method|size
specifier|public
specifier|synchronized
name|int
name|size
parameter_list|()
block|{
assert|assert
name|assertSize
argument_list|()
assert|;
return|return
name|coreKeyToShard
operator|.
name|size
argument_list|()
return|;
block|}
DECL|method|assertSize
specifier|private
specifier|synchronized
name|boolean
name|assertSize
parameter_list|()
block|{
comment|// this is heavy and should only used in assertions
name|boolean
name|assertionsEnabled
init|=
literal|false
decl_stmt|;
assert|assert
name|assertionsEnabled
operator|=
literal|true
assert|;
if|if
condition|(
name|assertionsEnabled
operator|==
literal|false
condition|)
block|{
throw|throw
operator|new
name|AssertionError
argument_list|(
literal|"only run this if assertions are enabled"
argument_list|)
throw|;
block|}
name|Collection
argument_list|<
name|Set
argument_list|<
name|Object
argument_list|>
argument_list|>
name|values
init|=
name|indexToCoreKey
operator|.
name|values
argument_list|()
decl_stmt|;
name|int
name|size
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Set
argument_list|<
name|Object
argument_list|>
name|value
range|:
name|values
control|)
block|{
name|size
operator|+=
name|value
operator|.
name|size
argument_list|()
expr_stmt|;
block|}
return|return
name|size
operator|==
name|coreKeyToShard
operator|.
name|size
argument_list|()
return|;
block|}
block|}
end_class

end_unit

