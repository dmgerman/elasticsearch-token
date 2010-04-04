begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.cache.filter.support
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
name|support
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
name|threadpool
operator|.
name|ThreadPool
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
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
name|util
operator|.
name|settings
operator|.
name|Settings
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
name|Iterator
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
name|Future
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ConcurrentMaps
operator|.
name|*
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|lucene
operator|.
name|docidset
operator|.
name|DocIdSets
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * @author kimchy (Shay Banon)  */
end_comment

begin_class
DECL|class|AbstractConcurrentMapFilterCache
specifier|public
specifier|abstract
class|class
name|AbstractConcurrentMapFilterCache
extends|extends
name|AbstractIndexComponent
implements|implements
name|FilterCache
block|{
DECL|field|cache
specifier|private
specifier|final
name|ConcurrentMap
argument_list|<
name|IndexReader
argument_list|,
name|ConcurrentMap
argument_list|<
name|Filter
argument_list|,
name|DocIdSet
argument_list|>
argument_list|>
name|cache
decl_stmt|;
DECL|field|readerCleanerSchedule
specifier|private
specifier|final
name|TimeValue
name|readerCleanerSchedule
decl_stmt|;
DECL|field|scheduleFuture
specifier|private
specifier|final
name|Future
name|scheduleFuture
decl_stmt|;
DECL|method|AbstractConcurrentMapFilterCache
specifier|protected
name|AbstractConcurrentMapFilterCache
parameter_list|(
name|Index
name|index
parameter_list|,
annotation|@
name|IndexSettings
name|Settings
name|indexSettings
parameter_list|,
name|ThreadPool
name|threadPool
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
name|readerCleanerSchedule
operator|=
name|componentSettings
operator|.
name|getAsTime
argument_list|(
literal|"reader_cleaner_schedule"
argument_list|,
name|TimeValue
operator|.
name|timeValueMinutes
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|debug
argument_list|(
literal|"Using ["
operator|+
name|type
argument_list|()
operator|+
literal|"] filter cache with readerCleanerSchedule [{}]"
argument_list|,
name|readerCleanerSchedule
argument_list|)
expr_stmt|;
name|this
operator|.
name|cache
operator|=
name|newConcurrentMap
argument_list|()
expr_stmt|;
name|this
operator|.
name|scheduleFuture
operator|=
name|threadPool
operator|.
name|scheduleWithFixedDelay
argument_list|(
operator|new
name|IndexReaderCleaner
argument_list|()
argument_list|,
name|readerCleanerSchedule
argument_list|)
expr_stmt|;
block|}
DECL|method|close
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
block|{
name|scheduleFuture
operator|.
name|cancel
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|cache
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
DECL|method|clear
annotation|@
name|Override
specifier|public
name|void
name|clear
parameter_list|()
block|{
name|cache
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
DECL|method|cache
annotation|@
name|Override
specifier|public
name|Filter
name|cache
parameter_list|(
name|Filter
name|filterToCache
parameter_list|)
block|{
return|return
operator|new
name|FilterCacheFilterWrapper
argument_list|(
name|filterToCache
argument_list|)
return|;
block|}
DECL|class|IndexReaderCleaner
specifier|private
class|class
name|IndexReaderCleaner
implements|implements
name|Runnable
block|{
DECL|method|run
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
for|for
control|(
name|Iterator
argument_list|<
name|IndexReader
argument_list|>
name|readerIt
init|=
name|cache
operator|.
name|keySet
argument_list|()
operator|.
name|iterator
argument_list|()
init|;
name|readerIt
operator|.
name|hasNext
argument_list|()
condition|;
control|)
block|{
name|IndexReader
name|reader
init|=
name|readerIt
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|reader
operator|.
name|getRefCount
argument_list|()
operator|<=
literal|0
condition|)
block|{
name|readerIt
operator|.
name|remove
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
DECL|method|buildMap
specifier|protected
specifier|abstract
name|ConcurrentMap
argument_list|<
name|Filter
argument_list|,
name|DocIdSet
argument_list|>
name|buildMap
parameter_list|()
function_decl|;
DECL|class|FilterCacheFilterWrapper
specifier|private
class|class
name|FilterCacheFilterWrapper
extends|extends
name|Filter
block|{
DECL|field|filter
specifier|private
specifier|final
name|Filter
name|filter
decl_stmt|;
DECL|method|FilterCacheFilterWrapper
specifier|private
name|FilterCacheFilterWrapper
parameter_list|(
name|Filter
name|filter
parameter_list|)
block|{
name|this
operator|.
name|filter
operator|=
name|filter
expr_stmt|;
block|}
DECL|method|getDocIdSet
annotation|@
name|Override
specifier|public
name|DocIdSet
name|getDocIdSet
parameter_list|(
name|IndexReader
name|reader
parameter_list|)
throws|throws
name|IOException
block|{
name|ConcurrentMap
argument_list|<
name|Filter
argument_list|,
name|DocIdSet
argument_list|>
name|cachedFilters
init|=
name|cache
operator|.
name|get
argument_list|(
name|reader
argument_list|)
decl_stmt|;
if|if
condition|(
name|cachedFilters
operator|==
literal|null
condition|)
block|{
name|cachedFilters
operator|=
name|buildMap
argument_list|()
expr_stmt|;
name|cache
operator|.
name|putIfAbsent
argument_list|(
name|reader
argument_list|,
name|cachedFilters
argument_list|)
expr_stmt|;
block|}
name|DocIdSet
name|docIdSet
init|=
name|cachedFilters
operator|.
name|get
argument_list|(
name|filter
argument_list|)
decl_stmt|;
if|if
condition|(
name|docIdSet
operator|!=
literal|null
condition|)
block|{
return|return
name|docIdSet
return|;
block|}
name|docIdSet
operator|=
name|filter
operator|.
name|getDocIdSet
argument_list|(
name|reader
argument_list|)
expr_stmt|;
name|docIdSet
operator|=
name|cacheable
argument_list|(
name|reader
argument_list|,
name|docIdSet
argument_list|)
expr_stmt|;
name|cachedFilters
operator|.
name|putIfAbsent
argument_list|(
name|filter
argument_list|,
name|docIdSet
argument_list|)
expr_stmt|;
return|return
name|docIdSet
return|;
block|}
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"FilterCacheFilterWrapper("
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
block|}
end_class

end_unit

