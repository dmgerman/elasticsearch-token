begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.indices
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|indices
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
name|ObjectHashSet
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
name|ObjectSet
import|;
end_import

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
name|DirectoryReader
import|;
end_import

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
name|IndexReader
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|Accountable
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|RamUsageEstimator
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
name|bytes
operator|.
name|BytesReference
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
name|cache
operator|.
name|Cache
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
name|cache
operator|.
name|CacheBuilder
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
name|cache
operator|.
name|CacheLoader
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
name|cache
operator|.
name|RemovalListener
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
name|cache
operator|.
name|RemovalNotification
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
name|component
operator|.
name|AbstractComponent
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
name|lucene
operator|.
name|index
operator|.
name|ElasticsearchDirectoryReader
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
name|Setting
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
name|Setting
operator|.
name|Property
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
name|common
operator|.
name|unit
operator|.
name|ByteSizeValue
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
name|unit
operator|.
name|TimeValue
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
name|util
operator|.
name|concurrent
operator|.
name|ConcurrentCollections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Closeable
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
name|Iterator
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
name|concurrent
operator|.
name|ConcurrentMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|TimeUnit
import|;
end_import

begin_comment
comment|/**  * The indices request cache allows to cache a shard level request stage responses, helping with improving  * similar requests that are potentially expensive (because of aggs for example). The cache is fully coherent  * with the semantics of NRT (the index reader version is part of the cache key), and relies on size based  * eviction to evict old reader associated cache entries as well as scheduler reaper to clean readers that  * are no longer used or closed shards.  *<p>  * Currently, the cache is only enabled for count requests, and can only be opted in on an index  * level setting that can be dynamically changed and defaults to false.  *<p>  * There are still several TODOs left in this class, some easily addressable, some more complex, but the support  * is functional.  */
end_comment

begin_class
DECL|class|IndicesRequestCache
specifier|public
specifier|final
class|class
name|IndicesRequestCache
extends|extends
name|AbstractComponent
implements|implements
name|RemovalListener
argument_list|<
name|IndicesRequestCache
operator|.
name|Key
argument_list|,
name|IndicesRequestCache
operator|.
name|Value
argument_list|>
implements|,
name|Closeable
block|{
comment|/**      * A setting to enable or disable request caching on an index level. Its dynamic by default      * since we are checking on the cluster state IndexMetaData always.      */
DECL|field|INDEX_CACHE_REQUEST_ENABLED_SETTING
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|Boolean
argument_list|>
name|INDEX_CACHE_REQUEST_ENABLED_SETTING
init|=
name|Setting
operator|.
name|boolSetting
argument_list|(
literal|"index.requests.cache.enable"
argument_list|,
literal|true
argument_list|,
name|Property
operator|.
name|Dynamic
argument_list|,
name|Property
operator|.
name|IndexScope
argument_list|)
decl_stmt|;
DECL|field|INDICES_CACHE_QUERY_SIZE
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|ByteSizeValue
argument_list|>
name|INDICES_CACHE_QUERY_SIZE
init|=
name|Setting
operator|.
name|byteSizeSetting
argument_list|(
literal|"indices.requests.cache.size"
argument_list|,
literal|"1%"
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|)
decl_stmt|;
DECL|field|INDICES_CACHE_QUERY_EXPIRE
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|TimeValue
argument_list|>
name|INDICES_CACHE_QUERY_EXPIRE
init|=
name|Setting
operator|.
name|positiveTimeSetting
argument_list|(
literal|"indices.requests.cache.expire"
argument_list|,
operator|new
name|TimeValue
argument_list|(
literal|0
argument_list|)
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|)
decl_stmt|;
DECL|field|registeredClosedListeners
specifier|private
specifier|final
name|ConcurrentMap
argument_list|<
name|CleanupKey
argument_list|,
name|Boolean
argument_list|>
name|registeredClosedListeners
init|=
name|ConcurrentCollections
operator|.
name|newConcurrentMap
argument_list|()
decl_stmt|;
DECL|field|keysToClean
specifier|private
specifier|final
name|Set
argument_list|<
name|CleanupKey
argument_list|>
name|keysToClean
init|=
name|ConcurrentCollections
operator|.
name|newConcurrentSet
argument_list|()
decl_stmt|;
DECL|field|size
specifier|private
specifier|final
name|ByteSizeValue
name|size
decl_stmt|;
DECL|field|expire
specifier|private
specifier|final
name|TimeValue
name|expire
decl_stmt|;
DECL|field|cache
specifier|private
specifier|final
name|Cache
argument_list|<
name|Key
argument_list|,
name|Value
argument_list|>
name|cache
decl_stmt|;
DECL|method|IndicesRequestCache
name|IndicesRequestCache
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
name|this
operator|.
name|size
operator|=
name|INDICES_CACHE_QUERY_SIZE
operator|.
name|get
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|expire
operator|=
name|INDICES_CACHE_QUERY_EXPIRE
operator|.
name|exists
argument_list|(
name|settings
argument_list|)
condition|?
name|INDICES_CACHE_QUERY_EXPIRE
operator|.
name|get
argument_list|(
name|settings
argument_list|)
else|:
literal|null
expr_stmt|;
name|long
name|sizeInBytes
init|=
name|size
operator|.
name|bytes
argument_list|()
decl_stmt|;
name|CacheBuilder
argument_list|<
name|Key
argument_list|,
name|Value
argument_list|>
name|cacheBuilder
init|=
name|CacheBuilder
operator|.
expr|<
name|Key
decl_stmt|,
name|Value
decl|>
name|builder
argument_list|()
decl|.
name|setMaximumWeight
argument_list|(
name|sizeInBytes
argument_list|)
decl|.
name|weigher
argument_list|(
parameter_list|(
name|k
parameter_list|,
name|v
parameter_list|)
lambda|->
name|k
operator|.
name|ramBytesUsed
argument_list|()
operator|+
name|v
operator|.
name|ramBytesUsed
argument_list|()
argument_list|)
operator|.
name|removalListener
argument_list|(
name|this
argument_list|)
decl_stmt|;
if|if
condition|(
name|expire
operator|!=
literal|null
condition|)
block|{
name|cacheBuilder
operator|.
name|setExpireAfterAccess
argument_list|(
name|TimeUnit
operator|.
name|MILLISECONDS
operator|.
name|toNanos
argument_list|(
name|expire
operator|.
name|millis
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|cache
operator|=
name|cacheBuilder
operator|.
name|build
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
block|{
name|cache
operator|.
name|invalidateAll
argument_list|()
expr_stmt|;
block|}
DECL|method|clear
name|void
name|clear
parameter_list|(
name|CacheEntity
name|entity
parameter_list|)
block|{
name|keysToClean
operator|.
name|add
argument_list|(
operator|new
name|CleanupKey
argument_list|(
name|entity
argument_list|,
operator|-
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|cleanCache
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|onRemoval
specifier|public
name|void
name|onRemoval
parameter_list|(
name|RemovalNotification
argument_list|<
name|Key
argument_list|,
name|Value
argument_list|>
name|notification
parameter_list|)
block|{
name|notification
operator|.
name|getKey
argument_list|()
operator|.
name|entity
operator|.
name|onRemoval
argument_list|(
name|notification
argument_list|)
expr_stmt|;
block|}
DECL|method|getOrCompute
name|BytesReference
name|getOrCompute
parameter_list|(
name|CacheEntity
name|cacheEntity
parameter_list|,
name|DirectoryReader
name|reader
parameter_list|,
name|BytesReference
name|cacheKey
parameter_list|)
throws|throws
name|Exception
block|{
specifier|final
name|Key
name|key
init|=
operator|new
name|Key
argument_list|(
name|cacheEntity
argument_list|,
name|reader
operator|.
name|getVersion
argument_list|()
argument_list|,
name|cacheKey
argument_list|)
decl_stmt|;
name|Loader
name|loader
init|=
operator|new
name|Loader
argument_list|(
name|cacheEntity
argument_list|)
decl_stmt|;
name|Value
name|value
init|=
name|cache
operator|.
name|computeIfAbsent
argument_list|(
name|key
argument_list|,
name|loader
argument_list|)
decl_stmt|;
if|if
condition|(
name|loader
operator|.
name|isLoaded
argument_list|()
condition|)
block|{
name|key
operator|.
name|entity
operator|.
name|onMiss
argument_list|()
expr_stmt|;
comment|// see if its the first time we see this reader, and make sure to register a cleanup key
name|CleanupKey
name|cleanupKey
init|=
operator|new
name|CleanupKey
argument_list|(
name|cacheEntity
argument_list|,
name|reader
operator|.
name|getVersion
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|registeredClosedListeners
operator|.
name|containsKey
argument_list|(
name|cleanupKey
argument_list|)
condition|)
block|{
name|Boolean
name|previous
init|=
name|registeredClosedListeners
operator|.
name|putIfAbsent
argument_list|(
name|cleanupKey
argument_list|,
name|Boolean
operator|.
name|TRUE
argument_list|)
decl_stmt|;
if|if
condition|(
name|previous
operator|==
literal|null
condition|)
block|{
name|ElasticsearchDirectoryReader
operator|.
name|addReaderCloseListener
argument_list|(
name|reader
argument_list|,
name|cleanupKey
argument_list|)
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
name|key
operator|.
name|entity
operator|.
name|onHit
argument_list|()
expr_stmt|;
block|}
return|return
name|value
operator|.
name|reference
return|;
block|}
DECL|class|Loader
specifier|private
specifier|static
class|class
name|Loader
implements|implements
name|CacheLoader
argument_list|<
name|Key
argument_list|,
name|Value
argument_list|>
block|{
DECL|field|entity
specifier|private
specifier|final
name|CacheEntity
name|entity
decl_stmt|;
DECL|field|loaded
specifier|private
name|boolean
name|loaded
decl_stmt|;
DECL|method|Loader
name|Loader
parameter_list|(
name|CacheEntity
name|entity
parameter_list|)
block|{
name|this
operator|.
name|entity
operator|=
name|entity
expr_stmt|;
block|}
DECL|method|isLoaded
specifier|public
name|boolean
name|isLoaded
parameter_list|()
block|{
return|return
name|this
operator|.
name|loaded
return|;
block|}
annotation|@
name|Override
DECL|method|load
specifier|public
name|Value
name|load
parameter_list|(
name|Key
name|key
parameter_list|)
throws|throws
name|Exception
block|{
name|Value
name|value
init|=
name|entity
operator|.
name|loadValue
argument_list|()
decl_stmt|;
name|entity
operator|.
name|onCached
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|loaded
operator|=
literal|true
expr_stmt|;
return|return
name|value
return|;
block|}
block|}
comment|/**      * Basic interface to make this cache testable.      */
DECL|interface|CacheEntity
interface|interface
name|CacheEntity
block|{
comment|/**          * Loads the actual cache value. this is the heavy lifting part.          */
DECL|method|loadValue
name|Value
name|loadValue
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**          * Called after the value was loaded via {@link #loadValue()}          */
DECL|method|onCached
name|void
name|onCached
parameter_list|(
name|Key
name|key
parameter_list|,
name|Value
name|value
parameter_list|)
function_decl|;
comment|/**          * Returns<code>true</code> iff the resource behind this entity is still open ie.          * entities assiciated with it can remain in the cache. ie. IndexShard is still open.          */
DECL|method|isOpen
name|boolean
name|isOpen
parameter_list|()
function_decl|;
comment|/**          * Returns the cache identity. this is, similar to {@link #isOpen()} the resource identity behind this cache entity.          * For instance IndexShard is the identity while a CacheEntity is per DirectoryReader. Yet, we group by IndexShard instance.          */
DECL|method|getCacheIdentity
name|Object
name|getCacheIdentity
parameter_list|()
function_decl|;
comment|/**          * Called each time this entity has a cache hit.          */
DECL|method|onHit
name|void
name|onHit
parameter_list|()
function_decl|;
comment|/**          * Called each time this entity has a cache miss.          */
DECL|method|onMiss
name|void
name|onMiss
parameter_list|()
function_decl|;
comment|/**          * Called when this entity instance is removed          */
DECL|method|onRemoval
name|void
name|onRemoval
parameter_list|(
name|RemovalNotification
argument_list|<
name|Key
argument_list|,
name|Value
argument_list|>
name|notification
parameter_list|)
function_decl|;
block|}
DECL|class|Value
specifier|static
class|class
name|Value
implements|implements
name|Accountable
block|{
DECL|field|reference
specifier|final
name|BytesReference
name|reference
decl_stmt|;
DECL|field|ramBytesUsed
specifier|final
name|long
name|ramBytesUsed
decl_stmt|;
DECL|method|Value
name|Value
parameter_list|(
name|BytesReference
name|reference
parameter_list|,
name|long
name|ramBytesUsed
parameter_list|)
block|{
name|this
operator|.
name|reference
operator|=
name|reference
expr_stmt|;
name|this
operator|.
name|ramBytesUsed
operator|=
name|ramBytesUsed
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|ramBytesUsed
specifier|public
name|long
name|ramBytesUsed
parameter_list|()
block|{
return|return
name|ramBytesUsed
return|;
block|}
annotation|@
name|Override
DECL|method|getChildResources
specifier|public
name|Collection
argument_list|<
name|Accountable
argument_list|>
name|getChildResources
parameter_list|()
block|{
return|return
name|Collections
operator|.
name|emptyList
argument_list|()
return|;
block|}
block|}
DECL|class|Key
specifier|static
class|class
name|Key
implements|implements
name|Accountable
block|{
DECL|field|entity
specifier|public
specifier|final
name|CacheEntity
name|entity
decl_stmt|;
comment|// use as identity equality
DECL|field|readerVersion
specifier|public
specifier|final
name|long
name|readerVersion
decl_stmt|;
comment|// use the reader version to now keep a reference to a "short" lived reader until its reaped
DECL|field|value
specifier|public
specifier|final
name|BytesReference
name|value
decl_stmt|;
DECL|method|Key
name|Key
parameter_list|(
name|CacheEntity
name|entity
parameter_list|,
name|long
name|readerVersion
parameter_list|,
name|BytesReference
name|value
parameter_list|)
block|{
name|this
operator|.
name|entity
operator|=
name|entity
expr_stmt|;
name|this
operator|.
name|readerVersion
operator|=
name|readerVersion
expr_stmt|;
name|this
operator|.
name|value
operator|=
name|value
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|ramBytesUsed
specifier|public
name|long
name|ramBytesUsed
parameter_list|()
block|{
return|return
name|RamUsageEstimator
operator|.
name|NUM_BYTES_OBJECT_REF
operator|+
name|Long
operator|.
name|BYTES
operator|+
name|value
operator|.
name|length
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|getChildResources
specifier|public
name|Collection
argument_list|<
name|Accountable
argument_list|>
name|getChildResources
parameter_list|()
block|{
comment|// TODO: more detailed ram usage?
return|return
name|Collections
operator|.
name|emptyList
argument_list|()
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
return|return
literal|true
return|;
name|Key
name|key
init|=
operator|(
name|Key
operator|)
name|o
decl_stmt|;
if|if
condition|(
name|readerVersion
operator|!=
name|key
operator|.
name|readerVersion
condition|)
return|return
literal|false
return|;
if|if
condition|(
operator|!
name|entity
operator|.
name|getCacheIdentity
argument_list|()
operator|.
name|equals
argument_list|(
name|key
operator|.
name|entity
operator|.
name|getCacheIdentity
argument_list|()
argument_list|)
condition|)
return|return
literal|false
return|;
if|if
condition|(
operator|!
name|value
operator|.
name|equals
argument_list|(
name|key
operator|.
name|value
argument_list|)
condition|)
return|return
literal|false
return|;
return|return
literal|true
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
name|entity
operator|.
name|getCacheIdentity
argument_list|()
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
name|Long
operator|.
name|hashCode
argument_list|(
name|readerVersion
argument_list|)
expr_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
name|value
operator|.
name|hashCode
argument_list|()
expr_stmt|;
return|return
name|result
return|;
block|}
block|}
DECL|class|CleanupKey
specifier|private
class|class
name|CleanupKey
implements|implements
name|IndexReader
operator|.
name|ReaderClosedListener
block|{
DECL|field|entity
specifier|final
name|CacheEntity
name|entity
decl_stmt|;
DECL|field|readerVersion
specifier|final
name|long
name|readerVersion
decl_stmt|;
comment|// use the reader version to now keep a reference to a "short" lived reader until its reaped
DECL|method|CleanupKey
specifier|private
name|CleanupKey
parameter_list|(
name|CacheEntity
name|entity
parameter_list|,
name|long
name|readerVersion
parameter_list|)
block|{
name|this
operator|.
name|entity
operator|=
name|entity
expr_stmt|;
name|this
operator|.
name|readerVersion
operator|=
name|readerVersion
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|onClose
specifier|public
name|void
name|onClose
parameter_list|(
name|IndexReader
name|reader
parameter_list|)
block|{
name|Boolean
name|remove
init|=
name|registeredClosedListeners
operator|.
name|remove
argument_list|(
name|this
argument_list|)
decl_stmt|;
if|if
condition|(
name|remove
operator|!=
literal|null
condition|)
block|{
name|keysToClean
operator|.
name|add
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
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
return|return
literal|true
return|;
name|CleanupKey
name|that
init|=
operator|(
name|CleanupKey
operator|)
name|o
decl_stmt|;
if|if
condition|(
name|readerVersion
operator|!=
name|that
operator|.
name|readerVersion
condition|)
return|return
literal|false
return|;
if|if
condition|(
operator|!
name|entity
operator|.
name|getCacheIdentity
argument_list|()
operator|.
name|equals
argument_list|(
name|that
operator|.
name|entity
operator|.
name|getCacheIdentity
argument_list|()
argument_list|)
condition|)
return|return
literal|false
return|;
return|return
literal|true
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
name|entity
operator|.
name|getCacheIdentity
argument_list|()
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
name|Long
operator|.
name|hashCode
argument_list|(
name|readerVersion
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
block|}
DECL|method|cleanCache
specifier|synchronized
name|void
name|cleanCache
parameter_list|()
block|{
specifier|final
name|ObjectSet
argument_list|<
name|CleanupKey
argument_list|>
name|currentKeysToClean
init|=
operator|new
name|ObjectHashSet
argument_list|<>
argument_list|()
decl_stmt|;
specifier|final
name|ObjectSet
argument_list|<
name|Object
argument_list|>
name|currentFullClean
init|=
operator|new
name|ObjectHashSet
argument_list|<>
argument_list|()
decl_stmt|;
name|currentKeysToClean
operator|.
name|clear
argument_list|()
expr_stmt|;
name|currentFullClean
operator|.
name|clear
argument_list|()
expr_stmt|;
for|for
control|(
name|Iterator
argument_list|<
name|CleanupKey
argument_list|>
name|iterator
init|=
name|keysToClean
operator|.
name|iterator
argument_list|()
init|;
name|iterator
operator|.
name|hasNext
argument_list|()
condition|;
control|)
block|{
name|CleanupKey
name|cleanupKey
init|=
name|iterator
operator|.
name|next
argument_list|()
decl_stmt|;
name|iterator
operator|.
name|remove
argument_list|()
expr_stmt|;
if|if
condition|(
name|cleanupKey
operator|.
name|readerVersion
operator|==
operator|-
literal|1
operator|||
name|cleanupKey
operator|.
name|entity
operator|.
name|isOpen
argument_list|()
operator|==
literal|false
condition|)
block|{
comment|// -1 indicates full cleanup, as does a closed shard
name|currentFullClean
operator|.
name|add
argument_list|(
name|cleanupKey
operator|.
name|entity
operator|.
name|getCacheIdentity
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|currentKeysToClean
operator|.
name|add
argument_list|(
name|cleanupKey
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
operator|!
name|currentKeysToClean
operator|.
name|isEmpty
argument_list|()
operator|||
operator|!
name|currentFullClean
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
for|for
control|(
name|Iterator
argument_list|<
name|Key
argument_list|>
name|iterator
init|=
name|cache
operator|.
name|keys
argument_list|()
operator|.
name|iterator
argument_list|()
init|;
name|iterator
operator|.
name|hasNext
argument_list|()
condition|;
control|)
block|{
name|Key
name|key
init|=
name|iterator
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|currentFullClean
operator|.
name|contains
argument_list|(
name|key
operator|.
name|entity
operator|.
name|getCacheIdentity
argument_list|()
argument_list|)
condition|)
block|{
name|iterator
operator|.
name|remove
argument_list|()
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|currentKeysToClean
operator|.
name|contains
argument_list|(
operator|new
name|CleanupKey
argument_list|(
name|key
operator|.
name|entity
argument_list|,
name|key
operator|.
name|readerVersion
argument_list|)
argument_list|)
condition|)
block|{
name|iterator
operator|.
name|remove
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
name|cache
operator|.
name|refresh
argument_list|()
expr_stmt|;
block|}
comment|/**      * Returns the current size of the cache      */
DECL|method|count
name|int
name|count
parameter_list|()
block|{
return|return
name|cache
operator|.
name|count
argument_list|()
return|;
block|}
DECL|method|numRegisteredCloseListeners
name|int
name|numRegisteredCloseListeners
parameter_list|()
block|{
comment|// for testing
return|return
name|registeredClosedListeners
operator|.
name|size
argument_list|()
return|;
block|}
block|}
end_class

end_unit

