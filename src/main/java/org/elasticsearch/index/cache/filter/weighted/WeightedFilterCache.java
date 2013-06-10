begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.cache.filter.weighted
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|cache
operator|.
name|filter
operator|.
name|weighted
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
name|cache
operator|.
name|Cache
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|cache
operator|.
name|Weigher
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
name|AtomicReaderContext
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
name|index
operator|.
name|SegmentReader
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
name|search
operator|.
name|DocIdSet
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
name|search
operator|.
name|Filter
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
name|Bits
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
name|BytesRef
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticSearchException
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
name|Strings
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
name|inject
operator|.
name|Inject
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
name|docset
operator|.
name|DocIdSets
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
name|search
operator|.
name|CachedFilter
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
name|search
operator|.
name|NoCacheFilter
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
name|util
operator|.
name|concurrent
operator|.
name|ConcurrentCollections
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
name|AbstractIndexComponent
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
name|Index
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
name|cache
operator|.
name|filter
operator|.
name|FilterCache
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
name|cache
operator|.
name|filter
operator|.
name|support
operator|.
name|CacheKeyFilter
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
name|service
operator|.
name|IndexService
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
name|settings
operator|.
name|IndexSettings
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
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|shard
operator|.
name|service
operator|.
name|IndexShard
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|cache
operator|.
name|filter
operator|.
name|IndicesFilterCache
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
name|concurrent
operator|.
name|ConcurrentMap
import|;
end_import

begin_class
DECL|class|WeightedFilterCache
specifier|public
class|class
name|WeightedFilterCache
extends|extends
name|AbstractIndexComponent
implements|implements
name|FilterCache
implements|,
name|SegmentReader
operator|.
name|CoreClosedListener
block|{
DECL|field|indicesFilterCache
specifier|final
name|IndicesFilterCache
name|indicesFilterCache
decl_stmt|;
DECL|field|indexService
name|IndexService
name|indexService
decl_stmt|;
DECL|field|seenReaders
specifier|final
name|ConcurrentMap
argument_list|<
name|Object
argument_list|,
name|Boolean
argument_list|>
name|seenReaders
init|=
name|ConcurrentCollections
operator|.
name|newConcurrentMap
argument_list|()
decl_stmt|;
annotation|@
name|Inject
DECL|method|WeightedFilterCache
specifier|public
name|WeightedFilterCache
parameter_list|(
name|Index
name|index
parameter_list|,
annotation|@
name|IndexSettings
name|Settings
name|indexSettings
parameter_list|,
name|IndicesFilterCache
name|indicesFilterCache
parameter_list|)
block|{
name|super
argument_list|(
name|index
argument_list|,
name|indexSettings
argument_list|)
expr_stmt|;
name|this
operator|.
name|indicesFilterCache
operator|=
name|indicesFilterCache
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|setIndexService
specifier|public
name|void
name|setIndexService
parameter_list|(
name|IndexService
name|indexService
parameter_list|)
block|{
name|this
operator|.
name|indexService
operator|=
name|indexService
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|type
specifier|public
name|String
name|type
parameter_list|()
block|{
return|return
literal|"weighted"
return|;
block|}
annotation|@
name|Override
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|ElasticSearchException
block|{
name|clear
argument_list|(
literal|"close"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|clear
specifier|public
name|void
name|clear
parameter_list|(
name|String
name|reason
parameter_list|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"full cache clear, reason [{}]"
argument_list|,
name|reason
argument_list|)
expr_stmt|;
for|for
control|(
name|Object
name|readerKey
range|:
name|seenReaders
operator|.
name|keySet
argument_list|()
control|)
block|{
name|Boolean
name|removed
init|=
name|seenReaders
operator|.
name|remove
argument_list|(
name|readerKey
argument_list|)
decl_stmt|;
if|if
condition|(
name|removed
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|indicesFilterCache
operator|.
name|addReaderKeyToClean
argument_list|(
name|readerKey
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|clear
specifier|public
name|void
name|clear
parameter_list|(
name|String
name|reason
parameter_list|,
name|String
index|[]
name|keys
parameter_list|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"clear keys [], reason [{}]"
argument_list|,
name|reason
argument_list|,
name|keys
argument_list|)
expr_stmt|;
specifier|final
name|BytesRef
name|spare
init|=
operator|new
name|BytesRef
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|key
range|:
name|keys
control|)
block|{
specifier|final
name|byte
index|[]
name|keyBytes
init|=
name|Strings
operator|.
name|toUTF8Bytes
argument_list|(
name|key
argument_list|,
name|spare
argument_list|)
decl_stmt|;
for|for
control|(
name|Object
name|readerKey
range|:
name|seenReaders
operator|.
name|keySet
argument_list|()
control|)
block|{
name|indicesFilterCache
operator|.
name|cache
argument_list|()
operator|.
name|invalidate
argument_list|(
operator|new
name|FilterCacheKey
argument_list|(
name|readerKey
argument_list|,
operator|new
name|CacheKeyFilter
operator|.
name|Key
argument_list|(
name|keyBytes
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
DECL|method|onClose
specifier|public
name|void
name|onClose
parameter_list|(
name|SegmentReader
name|owner
parameter_list|)
block|{
name|clear
argument_list|(
name|owner
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|clear
specifier|public
name|void
name|clear
parameter_list|(
name|IndexReader
name|reader
parameter_list|)
block|{
comment|// we add the seen reader before we add the first cache entry for this reader
comment|// so, if we don't see it here, its won't be in the cache
name|Boolean
name|removed
init|=
name|seenReaders
operator|.
name|remove
argument_list|(
name|reader
operator|.
name|getCoreCacheKey
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|removed
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|indicesFilterCache
operator|.
name|addReaderKeyToClean
argument_list|(
name|reader
operator|.
name|getCoreCacheKey
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|cache
specifier|public
name|Filter
name|cache
parameter_list|(
name|Filter
name|filterToCache
parameter_list|)
block|{
if|if
condition|(
name|filterToCache
operator|instanceof
name|NoCacheFilter
condition|)
block|{
return|return
name|filterToCache
return|;
block|}
if|if
condition|(
name|CachedFilter
operator|.
name|isCached
argument_list|(
name|filterToCache
argument_list|)
condition|)
block|{
return|return
name|filterToCache
return|;
block|}
return|return
operator|new
name|FilterCacheFilterWrapper
argument_list|(
name|filterToCache
argument_list|,
name|this
argument_list|)
return|;
block|}
DECL|class|FilterCacheFilterWrapper
specifier|static
class|class
name|FilterCacheFilterWrapper
extends|extends
name|CachedFilter
block|{
DECL|field|filter
specifier|private
specifier|final
name|Filter
name|filter
decl_stmt|;
DECL|field|cache
specifier|private
specifier|final
name|WeightedFilterCache
name|cache
decl_stmt|;
DECL|method|FilterCacheFilterWrapper
name|FilterCacheFilterWrapper
parameter_list|(
name|Filter
name|filter
parameter_list|,
name|WeightedFilterCache
name|cache
parameter_list|)
block|{
name|this
operator|.
name|filter
operator|=
name|filter
expr_stmt|;
name|this
operator|.
name|cache
operator|=
name|cache
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getDocIdSet
specifier|public
name|DocIdSet
name|getDocIdSet
parameter_list|(
name|AtomicReaderContext
name|context
parameter_list|,
name|Bits
name|acceptDocs
parameter_list|)
throws|throws
name|IOException
block|{
name|Object
name|filterKey
init|=
name|filter
decl_stmt|;
if|if
condition|(
name|filter
operator|instanceof
name|CacheKeyFilter
condition|)
block|{
name|filterKey
operator|=
operator|(
operator|(
name|CacheKeyFilter
operator|)
name|filter
operator|)
operator|.
name|cacheKey
argument_list|()
expr_stmt|;
block|}
name|FilterCacheKey
name|cacheKey
init|=
operator|new
name|FilterCacheKey
argument_list|(
name|context
operator|.
name|reader
argument_list|()
operator|.
name|getCoreCacheKey
argument_list|()
argument_list|,
name|filterKey
argument_list|)
decl_stmt|;
name|Cache
argument_list|<
name|FilterCacheKey
argument_list|,
name|DocIdSet
argument_list|>
name|innerCache
init|=
name|cache
operator|.
name|indicesFilterCache
operator|.
name|cache
argument_list|()
decl_stmt|;
name|DocIdSet
name|cacheValue
init|=
name|innerCache
operator|.
name|getIfPresent
argument_list|(
name|cacheKey
argument_list|)
decl_stmt|;
if|if
condition|(
name|cacheValue
operator|==
literal|null
condition|)
block|{
if|if
condition|(
operator|!
name|cache
operator|.
name|seenReaders
operator|.
name|containsKey
argument_list|(
name|context
operator|.
name|reader
argument_list|()
operator|.
name|getCoreCacheKey
argument_list|()
argument_list|)
condition|)
block|{
name|Boolean
name|previous
init|=
name|cache
operator|.
name|seenReaders
operator|.
name|putIfAbsent
argument_list|(
name|context
operator|.
name|reader
argument_list|()
operator|.
name|getCoreCacheKey
argument_list|()
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
comment|// we add a core closed listener only, for non core IndexReaders we rely on clear being called (percolator for example)
if|if
condition|(
name|context
operator|.
name|reader
argument_list|()
operator|instanceof
name|SegmentReader
condition|)
block|{
operator|(
operator|(
name|SegmentReader
operator|)
name|context
operator|.
name|reader
argument_list|()
operator|)
operator|.
name|addCoreClosedListener
argument_list|(
name|cache
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|// we can't pass down acceptedDocs provided, because we are caching the result, and acceptedDocs
comment|// might be specific to a query AST, we do pass down the live docs to make sure we optimize the execution
name|cacheValue
operator|=
name|DocIdSets
operator|.
name|toCacheable
argument_list|(
name|context
operator|.
name|reader
argument_list|()
argument_list|,
name|filter
operator|.
name|getDocIdSet
argument_list|(
name|context
argument_list|,
name|context
operator|.
name|reader
argument_list|()
operator|.
name|getLiveDocs
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// we might put the same one concurrently, that's fine, it will be replaced and the removal
comment|// will be called
name|ShardId
name|shardId
init|=
name|ShardUtils
operator|.
name|extractShardId
argument_list|(
name|context
operator|.
name|reader
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|shardId
operator|!=
literal|null
condition|)
block|{
name|IndexShard
name|shard
init|=
name|cache
operator|.
name|indexService
operator|.
name|shard
argument_list|(
name|shardId
operator|.
name|id
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|shard
operator|!=
literal|null
condition|)
block|{
name|cacheKey
operator|.
name|removalListener
operator|=
name|shard
operator|.
name|filterCache
argument_list|()
expr_stmt|;
name|shard
operator|.
name|filterCache
argument_list|()
operator|.
name|onCached
argument_list|(
name|DocIdSets
operator|.
name|sizeInBytes
argument_list|(
name|cacheValue
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|innerCache
operator|.
name|put
argument_list|(
name|cacheKey
argument_list|,
name|cacheValue
argument_list|)
expr_stmt|;
block|}
comment|// note, we don't wrap the return value with a BitsFilteredDocIdSet.wrap(docIdSet, acceptDocs) because
comment|// we rely on our custom XFilteredQuery to do the wrapping if needed, so we don't have the wrap each
comment|// filter on its own
return|return
name|cacheValue
operator|==
name|DocIdSet
operator|.
name|EMPTY_DOCIDSET
condition|?
literal|null
else|:
name|cacheValue
return|;
block|}
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"cache("
operator|+
name|filter
operator|+
literal|")"
return|;
block|}
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
operator|!
operator|(
name|o
operator|instanceof
name|FilterCacheFilterWrapper
operator|)
condition|)
return|return
literal|false
return|;
return|return
name|this
operator|.
name|filter
operator|.
name|equals
argument_list|(
operator|(
operator|(
name|FilterCacheFilterWrapper
operator|)
name|o
operator|)
operator|.
name|filter
argument_list|)
return|;
block|}
DECL|method|hashCode
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|filter
operator|.
name|hashCode
argument_list|()
operator|^
literal|0x1117BF25
return|;
block|}
block|}
DECL|class|FilterCacheValueWeigher
specifier|public
specifier|static
class|class
name|FilterCacheValueWeigher
implements|implements
name|Weigher
argument_list|<
name|WeightedFilterCache
operator|.
name|FilterCacheKey
argument_list|,
name|DocIdSet
argument_list|>
block|{
annotation|@
name|Override
DECL|method|weigh
specifier|public
name|int
name|weigh
parameter_list|(
name|FilterCacheKey
name|key
parameter_list|,
name|DocIdSet
name|value
parameter_list|)
block|{
name|int
name|weight
init|=
operator|(
name|int
operator|)
name|Math
operator|.
name|min
argument_list|(
name|DocIdSets
operator|.
name|sizeInBytes
argument_list|(
name|value
argument_list|)
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
decl_stmt|;
return|return
name|weight
operator|==
literal|0
condition|?
literal|1
else|:
name|weight
return|;
block|}
block|}
DECL|class|FilterCacheKey
specifier|public
specifier|static
class|class
name|FilterCacheKey
block|{
DECL|field|readerKey
specifier|private
specifier|final
name|Object
name|readerKey
decl_stmt|;
DECL|field|filterKey
specifier|private
specifier|final
name|Object
name|filterKey
decl_stmt|;
comment|// if we know, we will try and set the removal listener (for statistics)
comment|// its ok that its not volatile because we make sure we only set it when the object is created before its shared between threads
annotation|@
name|Nullable
DECL|field|removalListener
specifier|public
name|RemovalListener
argument_list|<
name|WeightedFilterCache
operator|.
name|FilterCacheKey
argument_list|,
name|DocIdSet
argument_list|>
name|removalListener
decl_stmt|;
DECL|method|FilterCacheKey
specifier|public
name|FilterCacheKey
parameter_list|(
name|Object
name|readerKey
parameter_list|,
name|Object
name|filterKey
parameter_list|)
block|{
name|this
operator|.
name|readerKey
operator|=
name|readerKey
expr_stmt|;
name|this
operator|.
name|filterKey
operator|=
name|filterKey
expr_stmt|;
block|}
DECL|method|readerKey
specifier|public
name|Object
name|readerKey
parameter_list|()
block|{
return|return
name|readerKey
return|;
block|}
DECL|method|filterKey
specifier|public
name|Object
name|filterKey
parameter_list|()
block|{
return|return
name|filterKey
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
comment|//            if (o == null || getClass() != o.getClass()) return false;
name|FilterCacheKey
name|that
init|=
operator|(
name|FilterCacheKey
operator|)
name|o
decl_stmt|;
return|return
operator|(
name|readerKey
argument_list|()
operator|.
name|equals
argument_list|(
name|that
operator|.
name|readerKey
argument_list|()
argument_list|)
operator|&&
name|filterKey
operator|.
name|equals
argument_list|(
name|that
operator|.
name|filterKey
argument_list|)
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
return|return
name|readerKey
argument_list|()
operator|.
name|hashCode
argument_list|()
operator|+
literal|31
operator|*
name|filterKey
operator|.
name|hashCode
argument_list|()
return|;
block|}
block|}
block|}
end_class

end_unit

