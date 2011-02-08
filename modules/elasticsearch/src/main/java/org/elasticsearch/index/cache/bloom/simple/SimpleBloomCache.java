begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.cache.bloom.simple
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|cache
operator|.
name|bloom
operator|.
name|simple
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
name|index
operator|.
name|Term
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
name|TermDocs
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
name|TermEnum
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
name|StringHelper
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
name|UnicodeUtil
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
name|Unicode
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
name|bloom
operator|.
name|BloomFilter
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
name|bloom
operator|.
name|BloomFilterFactory
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
name|MapMaker
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
name|SizeUnit
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
name|SizeValue
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
name|bloom
operator|.
name|BloomCache
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicBoolean
import|;
end_import

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|SimpleBloomCache
specifier|public
class|class
name|SimpleBloomCache
extends|extends
name|AbstractIndexComponent
implements|implements
name|BloomCache
block|{
DECL|field|threadPool
specifier|private
specifier|final
name|ThreadPool
name|threadPool
decl_stmt|;
DECL|field|maxSize
specifier|private
specifier|final
name|long
name|maxSize
decl_stmt|;
DECL|field|cache
specifier|private
specifier|final
name|ConcurrentMap
argument_list|<
name|Object
argument_list|,
name|ConcurrentMap
argument_list|<
name|String
argument_list|,
name|BloomFilterEntry
argument_list|>
argument_list|>
name|cache
decl_stmt|;
DECL|field|creationMutex
specifier|private
specifier|final
name|Object
name|creationMutex
init|=
operator|new
name|Object
argument_list|()
decl_stmt|;
DECL|method|SimpleBloomCache
annotation|@
name|Inject
specifier|public
name|SimpleBloomCache
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
name|threadPool
operator|=
name|threadPool
expr_stmt|;
name|this
operator|.
name|maxSize
operator|=
name|indexSettings
operator|.
name|getAsSize
argument_list|(
literal|"index.cache.bloom.max_size"
argument_list|,
operator|new
name|SizeValue
argument_list|(
literal|500
argument_list|,
name|SizeUnit
operator|.
name|MEGA
argument_list|)
argument_list|)
operator|.
name|singles
argument_list|()
expr_stmt|;
comment|// weak keys is fine, it will only be cleared once IndexReader references will be removed
comment|// (assuming clear(...) will not be called)
name|this
operator|.
name|cache
operator|=
operator|new
name|MapMaker
argument_list|()
operator|.
name|weakKeys
argument_list|()
operator|.
name|makeMap
argument_list|()
expr_stmt|;
block|}
DECL|method|close
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|ElasticSearchException
block|{
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
DECL|method|clear
annotation|@
name|Override
specifier|public
name|void
name|clear
parameter_list|(
name|IndexReader
name|reader
parameter_list|)
block|{
name|ConcurrentMap
argument_list|<
name|String
argument_list|,
name|BloomFilterEntry
argument_list|>
name|map
init|=
name|cache
operator|.
name|remove
argument_list|(
name|reader
operator|.
name|getFieldCacheKey
argument_list|()
argument_list|)
decl_stmt|;
comment|// help soft/weak handling GC
if|if
condition|(
name|map
operator|!=
literal|null
condition|)
block|{
name|map
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
block|}
DECL|method|clearUnreferenced
annotation|@
name|Override
specifier|public
name|void
name|clearUnreferenced
parameter_list|()
block|{
comment|// nothing to do here...
block|}
DECL|method|sizeInBytes
annotation|@
name|Override
specifier|public
name|long
name|sizeInBytes
parameter_list|()
block|{
comment|// the overhead of the map is not really relevant...
name|long
name|sizeInBytes
init|=
literal|0
decl_stmt|;
for|for
control|(
name|ConcurrentMap
argument_list|<
name|String
argument_list|,
name|BloomFilterEntry
argument_list|>
name|map
range|:
name|cache
operator|.
name|values
argument_list|()
control|)
block|{
for|for
control|(
name|BloomFilterEntry
name|filter
range|:
name|map
operator|.
name|values
argument_list|()
control|)
block|{
name|sizeInBytes
operator|+=
name|filter
operator|.
name|filter
operator|.
name|sizeInBytes
argument_list|()
expr_stmt|;
block|}
block|}
return|return
name|sizeInBytes
return|;
block|}
DECL|method|sizeInBytes
annotation|@
name|Override
specifier|public
name|long
name|sizeInBytes
parameter_list|(
name|String
name|fieldName
parameter_list|)
block|{
name|long
name|sizeInBytes
init|=
literal|0
decl_stmt|;
for|for
control|(
name|ConcurrentMap
argument_list|<
name|String
argument_list|,
name|BloomFilterEntry
argument_list|>
name|map
range|:
name|cache
operator|.
name|values
argument_list|()
control|)
block|{
name|BloomFilterEntry
name|filter
init|=
name|map
operator|.
name|get
argument_list|(
name|fieldName
argument_list|)
decl_stmt|;
if|if
condition|(
name|filter
operator|!=
literal|null
condition|)
block|{
name|sizeInBytes
operator|+=
name|filter
operator|.
name|filter
operator|.
name|sizeInBytes
argument_list|()
expr_stmt|;
block|}
block|}
return|return
name|sizeInBytes
return|;
block|}
DECL|method|filter
annotation|@
name|Override
specifier|public
name|BloomFilter
name|filter
parameter_list|(
name|IndexReader
name|reader
parameter_list|,
name|String
name|fieldName
parameter_list|,
name|boolean
name|asyncLoad
parameter_list|)
block|{
name|int
name|currentNumDocs
init|=
name|reader
operator|.
name|numDocs
argument_list|()
decl_stmt|;
if|if
condition|(
name|currentNumDocs
operator|==
literal|0
condition|)
block|{
return|return
name|BloomFilter
operator|.
name|EMPTY
return|;
block|}
name|ConcurrentMap
argument_list|<
name|String
argument_list|,
name|BloomFilterEntry
argument_list|>
name|fieldCache
init|=
name|cache
operator|.
name|get
argument_list|(
name|reader
operator|.
name|getFieldCacheKey
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|fieldCache
operator|==
literal|null
condition|)
block|{
synchronized|synchronized
init|(
name|creationMutex
init|)
block|{
name|fieldCache
operator|=
name|cache
operator|.
name|get
argument_list|(
name|reader
operator|.
name|getFieldCacheKey
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|fieldCache
operator|==
literal|null
condition|)
block|{
name|fieldCache
operator|=
name|ConcurrentCollections
operator|.
name|newConcurrentMap
argument_list|()
expr_stmt|;
name|cache
operator|.
name|put
argument_list|(
name|reader
operator|.
name|getFieldCacheKey
argument_list|()
argument_list|,
name|fieldCache
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|BloomFilterEntry
name|filter
init|=
name|fieldCache
operator|.
name|get
argument_list|(
name|fieldName
argument_list|)
decl_stmt|;
if|if
condition|(
name|filter
operator|==
literal|null
condition|)
block|{
synchronized|synchronized
init|(
name|fieldCache
init|)
block|{
name|filter
operator|=
name|fieldCache
operator|.
name|get
argument_list|(
name|fieldName
argument_list|)
expr_stmt|;
if|if
condition|(
name|filter
operator|==
literal|null
condition|)
block|{
name|filter
operator|=
operator|new
name|BloomFilterEntry
argument_list|(
name|currentNumDocs
argument_list|,
name|BloomFilter
operator|.
name|NONE
argument_list|)
expr_stmt|;
name|filter
operator|.
name|loading
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|fieldCache
operator|.
name|put
argument_list|(
name|fieldName
argument_list|,
name|filter
argument_list|)
expr_stmt|;
comment|// now, do the async load of it...
if|if
condition|(
name|currentNumDocs
operator|<
name|maxSize
condition|)
block|{
name|BloomFilterLoader
name|loader
init|=
operator|new
name|BloomFilterLoader
argument_list|(
name|reader
argument_list|,
name|fieldName
argument_list|)
decl_stmt|;
if|if
condition|(
name|asyncLoad
condition|)
block|{
name|threadPool
operator|.
name|cached
argument_list|()
operator|.
name|execute
argument_list|(
name|loader
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|loader
operator|.
name|run
argument_list|()
expr_stmt|;
name|filter
operator|=
name|fieldCache
operator|.
name|get
argument_list|(
name|fieldName
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
comment|// if we too many deletes, we need to reload the bloom filter so it will be more effective
if|if
condition|(
name|filter
operator|.
name|numDocs
operator|>
literal|1000
operator|&&
name|filter
operator|.
name|numDocs
operator|<
name|maxSize
operator|&&
operator|(
name|currentNumDocs
operator|/
name|filter
operator|.
name|numDocs
operator|)
operator|<
literal|0.6
condition|)
block|{
if|if
condition|(
name|filter
operator|.
name|loading
operator|.
name|compareAndSet
argument_list|(
literal|false
argument_list|,
literal|true
argument_list|)
condition|)
block|{
comment|// do the async loading
name|BloomFilterLoader
name|loader
init|=
operator|new
name|BloomFilterLoader
argument_list|(
name|reader
argument_list|,
name|fieldName
argument_list|)
decl_stmt|;
if|if
condition|(
name|asyncLoad
condition|)
block|{
name|threadPool
operator|.
name|cached
argument_list|()
operator|.
name|execute
argument_list|(
name|loader
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|loader
operator|.
name|run
argument_list|()
expr_stmt|;
name|filter
operator|=
name|fieldCache
operator|.
name|get
argument_list|(
name|fieldName
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|filter
operator|.
name|filter
return|;
block|}
DECL|class|BloomFilterLoader
class|class
name|BloomFilterLoader
implements|implements
name|Runnable
block|{
DECL|field|reader
specifier|private
specifier|final
name|IndexReader
name|reader
decl_stmt|;
DECL|field|field
specifier|private
specifier|final
name|String
name|field
decl_stmt|;
DECL|method|BloomFilterLoader
name|BloomFilterLoader
parameter_list|(
name|IndexReader
name|reader
parameter_list|,
name|String
name|field
parameter_list|)
block|{
name|this
operator|.
name|reader
operator|=
name|reader
expr_stmt|;
name|this
operator|.
name|field
operator|=
name|StringHelper
operator|.
name|intern
argument_list|(
name|field
argument_list|)
expr_stmt|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
block|{
literal|"StringEquality"
block|}
argument_list|)
DECL|method|run
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|TermDocs
name|termDocs
init|=
literal|null
decl_stmt|;
name|TermEnum
name|termEnum
init|=
literal|null
decl_stmt|;
try|try
block|{
name|BloomFilter
name|filter
init|=
name|BloomFilterFactory
operator|.
name|getFilter
argument_list|(
name|reader
operator|.
name|numDocs
argument_list|()
argument_list|,
literal|15
argument_list|)
decl_stmt|;
name|termDocs
operator|=
name|reader
operator|.
name|termDocs
argument_list|()
expr_stmt|;
name|termEnum
operator|=
name|reader
operator|.
name|terms
argument_list|(
operator|new
name|Term
argument_list|(
name|field
argument_list|)
argument_list|)
expr_stmt|;
do|do
block|{
name|Term
name|term
init|=
name|termEnum
operator|.
name|term
argument_list|()
decl_stmt|;
if|if
condition|(
name|term
operator|==
literal|null
operator|||
name|term
operator|.
name|field
argument_list|()
operator|!=
name|field
condition|)
break|break;
comment|// LUCENE MONITOR: 4.0, move to use bytes!
name|UnicodeUtil
operator|.
name|UTF8Result
name|utf8Result
init|=
name|Unicode
operator|.
name|fromStringAsUtf8
argument_list|(
name|term
operator|.
name|text
argument_list|()
argument_list|)
decl_stmt|;
name|termDocs
operator|.
name|seek
argument_list|(
name|termEnum
argument_list|)
expr_stmt|;
while|while
condition|(
name|termDocs
operator|.
name|next
argument_list|()
condition|)
block|{
comment|// when traversing, make sure to ignore deleted docs, so the key->docId will be correct
if|if
condition|(
operator|!
name|reader
operator|.
name|isDeleted
argument_list|(
name|termDocs
operator|.
name|doc
argument_list|()
argument_list|)
condition|)
block|{
name|filter
operator|.
name|add
argument_list|(
name|utf8Result
operator|.
name|result
argument_list|,
literal|0
argument_list|,
name|utf8Result
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
block|}
block|}
do|while
condition|(
name|termEnum
operator|.
name|next
argument_list|()
condition|)
do|;
name|ConcurrentMap
argument_list|<
name|String
argument_list|,
name|BloomFilterEntry
argument_list|>
name|fieldCache
init|=
name|cache
operator|.
name|get
argument_list|(
name|reader
operator|.
name|getFieldCacheKey
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|fieldCache
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|fieldCache
operator|.
name|containsKey
argument_list|(
name|field
argument_list|)
condition|)
block|{
name|BloomFilterEntry
name|filterEntry
init|=
operator|new
name|BloomFilterEntry
argument_list|(
name|reader
operator|.
name|numDocs
argument_list|()
argument_list|,
name|filter
argument_list|)
decl_stmt|;
name|filterEntry
operator|.
name|loading
operator|.
name|set
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|fieldCache
operator|.
name|put
argument_list|(
name|field
argument_list|,
name|filterEntry
argument_list|)
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"failed to load bloom filter for [{}]"
argument_list|,
name|e
argument_list|,
name|field
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
try|try
block|{
if|if
condition|(
name|termDocs
operator|!=
literal|null
condition|)
block|{
name|termDocs
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// ignore
block|}
try|try
block|{
if|if
condition|(
name|termEnum
operator|!=
literal|null
condition|)
block|{
name|termEnum
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// ignore
block|}
block|}
block|}
block|}
DECL|class|BloomFilterEntry
specifier|static
class|class
name|BloomFilterEntry
block|{
DECL|field|numDocs
specifier|final
name|int
name|numDocs
decl_stmt|;
DECL|field|filter
specifier|final
name|BloomFilter
name|filter
decl_stmt|;
DECL|field|loading
specifier|final
name|AtomicBoolean
name|loading
init|=
operator|new
name|AtomicBoolean
argument_list|()
decl_stmt|;
DECL|method|BloomFilterEntry
specifier|public
name|BloomFilterEntry
parameter_list|(
name|int
name|numDocs
parameter_list|,
name|BloomFilter
name|filter
parameter_list|)
block|{
name|this
operator|.
name|numDocs
operator|=
name|numDocs
expr_stmt|;
name|this
operator|.
name|filter
operator|=
name|filter
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

