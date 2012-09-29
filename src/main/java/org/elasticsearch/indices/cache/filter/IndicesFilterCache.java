begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.indices.cache.filter
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|cache
operator|.
name|filter
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
name|base
operator|.
name|Objects
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
name|CacheBuilder
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
name|RemovalNotification
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
name|collect
operator|.
name|ImmutableMap
import|;
end_import

begin_import
import|import
name|gnu
operator|.
name|trove
operator|.
name|set
operator|.
name|hash
operator|.
name|THashSet
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
name|common
operator|.
name|CacheRecycler
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
name|MapBuilder
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
name|DocSet
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
operator|.
name|WeightedFilterCache
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|monitor
operator|.
name|jvm
operator|.
name|JvmInfo
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|node
operator|.
name|settings
operator|.
name|NodeSettingsService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|threadpool
operator|.
name|ThreadPool
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
name|concurrent
operator|.
name|TimeUnit
import|;
end_import

begin_class
DECL|class|IndicesFilterCache
specifier|public
class|class
name|IndicesFilterCache
extends|extends
name|AbstractComponent
implements|implements
name|RemovalListener
argument_list|<
name|WeightedFilterCache
operator|.
name|FilterCacheKey
argument_list|,
name|DocSet
argument_list|>
block|{
DECL|field|threadPool
specifier|private
specifier|final
name|ThreadPool
name|threadPool
decl_stmt|;
DECL|field|cache
specifier|private
name|Cache
argument_list|<
name|WeightedFilterCache
operator|.
name|FilterCacheKey
argument_list|,
name|DocSet
argument_list|>
name|cache
decl_stmt|;
DECL|field|size
specifier|private
specifier|volatile
name|String
name|size
decl_stmt|;
DECL|field|sizeInBytes
specifier|private
specifier|volatile
name|long
name|sizeInBytes
decl_stmt|;
DECL|field|expire
specifier|private
specifier|volatile
name|TimeValue
name|expire
decl_stmt|;
DECL|field|cleanInterval
specifier|private
specifier|final
name|TimeValue
name|cleanInterval
decl_stmt|;
DECL|field|readersKeysToClean
specifier|private
specifier|final
name|Set
argument_list|<
name|Object
argument_list|>
name|readersKeysToClean
init|=
name|ConcurrentCollections
operator|.
name|newConcurrentSet
argument_list|()
decl_stmt|;
DECL|field|closed
specifier|private
specifier|volatile
name|boolean
name|closed
decl_stmt|;
DECL|field|removalListeners
specifier|private
specifier|volatile
name|Map
argument_list|<
name|String
argument_list|,
name|RemovalListener
argument_list|<
name|WeightedFilterCache
operator|.
name|FilterCacheKey
argument_list|,
name|DocSet
argument_list|>
argument_list|>
name|removalListeners
init|=
name|ImmutableMap
operator|.
name|of
argument_list|()
decl_stmt|;
static|static
block|{
name|MetaData
operator|.
name|addDynamicSettings
argument_list|(
literal|"indices.cache.filter.size"
argument_list|,
literal|"indices.cache.filter.expire"
argument_list|)
expr_stmt|;
block|}
DECL|class|ApplySettings
class|class
name|ApplySettings
implements|implements
name|NodeSettingsService
operator|.
name|Listener
block|{
annotation|@
name|Override
DECL|method|onRefreshSettings
specifier|public
name|void
name|onRefreshSettings
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|boolean
name|replace
init|=
literal|false
decl_stmt|;
name|String
name|size
init|=
name|settings
operator|.
name|get
argument_list|(
literal|"indices.cache.filter.size"
argument_list|,
name|IndicesFilterCache
operator|.
name|this
operator|.
name|size
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|size
operator|.
name|equals
argument_list|(
name|IndicesFilterCache
operator|.
name|this
operator|.
name|size
argument_list|)
condition|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"updating [indices.cache.filter.size] from [{}] to [{}]"
argument_list|,
name|IndicesFilterCache
operator|.
name|this
operator|.
name|size
argument_list|,
name|size
argument_list|)
expr_stmt|;
name|IndicesFilterCache
operator|.
name|this
operator|.
name|size
operator|=
name|size
expr_stmt|;
name|replace
operator|=
literal|true
expr_stmt|;
block|}
name|TimeValue
name|expire
init|=
name|settings
operator|.
name|getAsTime
argument_list|(
literal|"indices.cache.filter.expire"
argument_list|,
name|IndicesFilterCache
operator|.
name|this
operator|.
name|expire
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|Objects
operator|.
name|equal
argument_list|(
name|expire
argument_list|,
name|IndicesFilterCache
operator|.
name|this
operator|.
name|expire
argument_list|)
condition|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"updating [indices.cache.filter.expire] from [{}] to [{}]"
argument_list|,
name|IndicesFilterCache
operator|.
name|this
operator|.
name|expire
argument_list|,
name|expire
argument_list|)
expr_stmt|;
name|IndicesFilterCache
operator|.
name|this
operator|.
name|expire
operator|=
name|expire
expr_stmt|;
name|replace
operator|=
literal|true
expr_stmt|;
block|}
if|if
condition|(
name|replace
condition|)
block|{
name|Cache
argument_list|<
name|WeightedFilterCache
operator|.
name|FilterCacheKey
argument_list|,
name|DocSet
argument_list|>
name|oldCache
init|=
name|IndicesFilterCache
operator|.
name|this
operator|.
name|cache
decl_stmt|;
name|computeSizeInBytes
argument_list|()
expr_stmt|;
name|buildCache
argument_list|()
expr_stmt|;
name|oldCache
operator|.
name|invalidateAll
argument_list|()
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Inject
DECL|method|IndicesFilterCache
specifier|public
name|IndicesFilterCache
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|,
name|NodeSettingsService
name|nodeSettingsService
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|threadPool
operator|=
name|threadPool
expr_stmt|;
name|this
operator|.
name|size
operator|=
name|componentSettings
operator|.
name|get
argument_list|(
literal|"size"
argument_list|,
literal|"20%"
argument_list|)
expr_stmt|;
name|this
operator|.
name|expire
operator|=
name|componentSettings
operator|.
name|getAsTime
argument_list|(
literal|"expire"
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|this
operator|.
name|cleanInterval
operator|=
name|componentSettings
operator|.
name|getAsTime
argument_list|(
literal|"clean_interval"
argument_list|,
name|TimeValue
operator|.
name|timeValueSeconds
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|computeSizeInBytes
argument_list|()
expr_stmt|;
name|buildCache
argument_list|()
expr_stmt|;
name|logger
operator|.
name|debug
argument_list|(
literal|"using [node] filter cache with size [{}], actual_size [{}]"
argument_list|,
name|size
argument_list|,
operator|new
name|ByteSizeValue
argument_list|(
name|sizeInBytes
argument_list|)
argument_list|)
expr_stmt|;
name|nodeSettingsService
operator|.
name|addListener
argument_list|(
operator|new
name|ApplySettings
argument_list|()
argument_list|)
expr_stmt|;
name|threadPool
operator|.
name|schedule
argument_list|(
name|cleanInterval
argument_list|,
name|ThreadPool
operator|.
name|Names
operator|.
name|SAME
argument_list|,
operator|new
name|ReaderCleaner
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|buildCache
specifier|private
name|void
name|buildCache
parameter_list|()
block|{
name|CacheBuilder
argument_list|<
name|WeightedFilterCache
operator|.
name|FilterCacheKey
argument_list|,
name|DocSet
argument_list|>
name|cacheBuilder
init|=
name|CacheBuilder
operator|.
name|newBuilder
argument_list|()
operator|.
name|removalListener
argument_list|(
name|this
argument_list|)
operator|.
name|maximumWeight
argument_list|(
name|sizeInBytes
argument_list|)
operator|.
name|weigher
argument_list|(
operator|new
name|WeightedFilterCache
operator|.
name|FilterCacheValueWeigher
argument_list|()
argument_list|)
decl_stmt|;
comment|// defaults to 4, but this is a busy map for all indices, increase it a bit
name|cacheBuilder
operator|.
name|concurrencyLevel
argument_list|(
literal|16
argument_list|)
expr_stmt|;
if|if
condition|(
name|expire
operator|!=
literal|null
condition|)
block|{
name|cacheBuilder
operator|.
name|expireAfterAccess
argument_list|(
name|expire
operator|.
name|millis
argument_list|()
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
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
DECL|method|computeSizeInBytes
specifier|private
name|void
name|computeSizeInBytes
parameter_list|()
block|{
if|if
condition|(
name|size
operator|.
name|endsWith
argument_list|(
literal|"%"
argument_list|)
condition|)
block|{
name|double
name|percent
init|=
name|Double
operator|.
name|parseDouble
argument_list|(
name|size
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|size
operator|.
name|length
argument_list|()
operator|-
literal|1
argument_list|)
argument_list|)
decl_stmt|;
name|sizeInBytes
operator|=
call|(
name|long
call|)
argument_list|(
operator|(
name|percent
operator|/
literal|100
operator|)
operator|*
name|JvmInfo
operator|.
name|jvmInfo
argument_list|()
operator|.
name|getMem
argument_list|()
operator|.
name|getHeapMax
argument_list|()
operator|.
name|bytes
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|sizeInBytes
operator|=
name|ByteSizeValue
operator|.
name|parseBytesSizeValue
argument_list|(
name|size
argument_list|)
operator|.
name|bytes
argument_list|()
expr_stmt|;
block|}
block|}
DECL|method|addRemovalListener
specifier|public
specifier|synchronized
name|void
name|addRemovalListener
parameter_list|(
name|String
name|index
parameter_list|,
name|RemovalListener
argument_list|<
name|WeightedFilterCache
operator|.
name|FilterCacheKey
argument_list|,
name|DocSet
argument_list|>
name|listener
parameter_list|)
block|{
name|removalListeners
operator|=
name|MapBuilder
operator|.
name|newMapBuilder
argument_list|(
name|removalListeners
argument_list|)
operator|.
name|put
argument_list|(
name|index
argument_list|,
name|listener
argument_list|)
operator|.
name|immutableMap
argument_list|()
expr_stmt|;
block|}
DECL|method|removeRemovalListener
specifier|public
specifier|synchronized
name|void
name|removeRemovalListener
parameter_list|(
name|String
name|index
parameter_list|)
block|{
name|removalListeners
operator|=
name|MapBuilder
operator|.
name|newMapBuilder
argument_list|(
name|removalListeners
argument_list|)
operator|.
name|remove
argument_list|(
name|index
argument_list|)
operator|.
name|immutableMap
argument_list|()
expr_stmt|;
block|}
DECL|method|addReaderKeyToClean
specifier|public
name|void
name|addReaderKeyToClean
parameter_list|(
name|Object
name|readerKey
parameter_list|)
block|{
name|readersKeysToClean
operator|.
name|add
argument_list|(
name|readerKey
argument_list|)
expr_stmt|;
block|}
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
block|{
name|closed
operator|=
literal|true
expr_stmt|;
name|cache
operator|.
name|invalidateAll
argument_list|()
expr_stmt|;
block|}
DECL|method|cache
specifier|public
name|Cache
argument_list|<
name|WeightedFilterCache
operator|.
name|FilterCacheKey
argument_list|,
name|DocSet
argument_list|>
name|cache
parameter_list|()
block|{
return|return
name|this
operator|.
name|cache
return|;
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
name|WeightedFilterCache
operator|.
name|FilterCacheKey
argument_list|,
name|DocSet
argument_list|>
name|removalNotification
parameter_list|)
block|{
name|WeightedFilterCache
operator|.
name|FilterCacheKey
name|key
init|=
name|removalNotification
operator|.
name|getKey
argument_list|()
decl_stmt|;
if|if
condition|(
name|key
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|RemovalListener
argument_list|<
name|WeightedFilterCache
operator|.
name|FilterCacheKey
argument_list|,
name|DocSet
argument_list|>
name|listener
init|=
name|removalListeners
operator|.
name|get
argument_list|(
name|key
operator|.
name|index
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|listener
operator|!=
literal|null
condition|)
block|{
name|listener
operator|.
name|onRemoval
argument_list|(
name|removalNotification
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      * The reason we need this class ie because we need to clean all the filters that are associated      * with a reader. We don't want to do it every time a reader closes, since iterating over all the map      * is expensive. There doesn't seem to be a nicer way to do it (and maintaining a list per reader      * of the filters will cost more).      */
DECL|class|ReaderCleaner
class|class
name|ReaderCleaner
implements|implements
name|Runnable
block|{
annotation|@
name|Override
DECL|method|run
specifier|public
name|void
name|run
parameter_list|()
block|{
if|if
condition|(
name|closed
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|readersKeysToClean
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|threadPool
operator|.
name|schedule
argument_list|(
name|cleanInterval
argument_list|,
name|ThreadPool
operator|.
name|Names
operator|.
name|SAME
argument_list|,
name|this
argument_list|)
expr_stmt|;
return|return;
block|}
name|threadPool
operator|.
name|executor
argument_list|(
name|ThreadPool
operator|.
name|Names
operator|.
name|GENERIC
argument_list|)
operator|.
name|execute
argument_list|(
operator|new
name|Runnable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|THashSet
argument_list|<
name|Object
argument_list|>
name|keys
init|=
name|CacheRecycler
operator|.
name|popHashSet
argument_list|()
decl_stmt|;
try|try
block|{
for|for
control|(
name|Iterator
argument_list|<
name|Object
argument_list|>
name|it
init|=
name|readersKeysToClean
operator|.
name|iterator
argument_list|()
init|;
name|it
operator|.
name|hasNext
argument_list|()
condition|;
control|)
block|{
name|keys
operator|.
name|add
argument_list|(
name|it
operator|.
name|next
argument_list|()
argument_list|)
expr_stmt|;
name|it
operator|.
name|remove
argument_list|()
expr_stmt|;
block|}
name|cache
operator|.
name|cleanUp
argument_list|()
expr_stmt|;
if|if
condition|(
operator|!
name|keys
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
for|for
control|(
name|Iterator
argument_list|<
name|WeightedFilterCache
operator|.
name|FilterCacheKey
argument_list|>
name|it
init|=
name|cache
operator|.
name|asMap
argument_list|()
operator|.
name|keySet
argument_list|()
operator|.
name|iterator
argument_list|()
init|;
name|it
operator|.
name|hasNext
argument_list|()
condition|;
control|)
block|{
name|WeightedFilterCache
operator|.
name|FilterCacheKey
name|filterCacheKey
init|=
name|it
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|keys
operator|.
name|contains
argument_list|(
name|filterCacheKey
operator|.
name|readerKey
argument_list|()
argument_list|)
condition|)
block|{
comment|// same as invalidate
name|it
operator|.
name|remove
argument_list|()
expr_stmt|;
block|}
block|}
block|}
name|threadPool
operator|.
name|schedule
argument_list|(
name|cleanInterval
argument_list|,
name|ThreadPool
operator|.
name|Names
operator|.
name|SAME
argument_list|,
name|ReaderCleaner
operator|.
name|this
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|CacheRecycler
operator|.
name|pushHashSet
argument_list|(
name|keys
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
end_class

end_unit

