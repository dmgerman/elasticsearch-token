begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.support
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|support
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
name|ObjectObjectOpenHashMap
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
name|search
operator|.
name|Scorer
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
name|ArrayUtil
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
name|cache
operator|.
name|recycler
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
name|cache
operator|.
name|recycler
operator|.
name|PageCacheRecycler
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
name|ReaderContextAware
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
name|ScorerAware
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
name|fielddata
operator|.
name|IndexFieldData
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
name|fielddata
operator|.
name|IndexGeoPointFieldData
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
name|fielddata
operator|.
name|IndexNumericFieldData
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|AggregationExecutionException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|support
operator|.
name|bytes
operator|.
name|BytesValuesSource
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|support
operator|.
name|geopoints
operator|.
name|GeoPointValuesSource
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|support
operator|.
name|numeric
operator|.
name|NumericValuesSource
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|internal
operator|.
name|SearchContext
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
name|Arrays
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

begin_comment
comment|/**  *  */
end_comment

begin_class
annotation|@
name|SuppressWarnings
argument_list|(
block|{
literal|"unchecked"
block|,
literal|"ForLoopReplaceableByForEach"
block|}
argument_list|)
DECL|class|AggregationContext
specifier|public
class|class
name|AggregationContext
implements|implements
name|ReaderContextAware
implements|,
name|ScorerAware
block|{
DECL|field|searchContext
specifier|private
specifier|final
name|SearchContext
name|searchContext
decl_stmt|;
DECL|field|perDepthFieldDataSources
specifier|private
name|ObjectObjectOpenHashMap
argument_list|<
name|String
argument_list|,
name|FieldDataSource
argument_list|>
index|[]
name|perDepthFieldDataSources
init|=
operator|new
name|ObjectObjectOpenHashMap
index|[
literal|4
index|]
decl_stmt|;
DECL|field|readerAwares
specifier|private
name|List
argument_list|<
name|ReaderContextAware
argument_list|>
name|readerAwares
init|=
operator|new
name|ArrayList
argument_list|<
name|ReaderContextAware
argument_list|>
argument_list|()
decl_stmt|;
DECL|field|scorerAwares
specifier|private
name|List
argument_list|<
name|ScorerAware
argument_list|>
name|scorerAwares
init|=
operator|new
name|ArrayList
argument_list|<
name|ScorerAware
argument_list|>
argument_list|()
decl_stmt|;
DECL|field|reader
specifier|private
name|AtomicReaderContext
name|reader
decl_stmt|;
DECL|field|scorer
specifier|private
name|Scorer
name|scorer
decl_stmt|;
DECL|method|AggregationContext
specifier|public
name|AggregationContext
parameter_list|(
name|SearchContext
name|searchContext
parameter_list|)
block|{
name|this
operator|.
name|searchContext
operator|=
name|searchContext
expr_stmt|;
block|}
DECL|method|searchContext
specifier|public
name|SearchContext
name|searchContext
parameter_list|()
block|{
return|return
name|searchContext
return|;
block|}
DECL|method|cacheRecycler
specifier|public
name|CacheRecycler
name|cacheRecycler
parameter_list|()
block|{
return|return
name|searchContext
operator|.
name|cacheRecycler
argument_list|()
return|;
block|}
DECL|method|pageCacheRecycler
specifier|public
name|PageCacheRecycler
name|pageCacheRecycler
parameter_list|()
block|{
return|return
name|searchContext
operator|.
name|pageCacheRecycler
argument_list|()
return|;
block|}
DECL|method|currentReader
specifier|public
name|AtomicReaderContext
name|currentReader
parameter_list|()
block|{
return|return
name|reader
return|;
block|}
DECL|method|currentScorer
specifier|public
name|Scorer
name|currentScorer
parameter_list|()
block|{
return|return
name|scorer
return|;
block|}
DECL|method|setNextReader
specifier|public
name|void
name|setNextReader
parameter_list|(
name|AtomicReaderContext
name|reader
parameter_list|)
block|{
name|this
operator|.
name|reader
operator|=
name|reader
expr_stmt|;
for|for
control|(
name|ReaderContextAware
name|aware
range|:
name|readerAwares
control|)
block|{
name|aware
operator|.
name|setNextReader
argument_list|(
name|reader
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|setScorer
specifier|public
name|void
name|setScorer
parameter_list|(
name|Scorer
name|scorer
parameter_list|)
block|{
name|this
operator|.
name|scorer
operator|=
name|scorer
expr_stmt|;
for|for
control|(
name|ScorerAware
name|scorerAware
range|:
name|scorerAwares
control|)
block|{
name|scorerAware
operator|.
name|setScorer
argument_list|(
name|scorer
argument_list|)
expr_stmt|;
block|}
block|}
comment|/** Get a value source given its configuration and the depth of the aggregator in the aggregation tree. */
DECL|method|valuesSource
specifier|public
parameter_list|<
name|VS
extends|extends
name|ValuesSource
parameter_list|>
name|VS
name|valuesSource
parameter_list|(
name|ValuesSourceConfig
argument_list|<
name|VS
argument_list|>
name|config
parameter_list|,
name|int
name|depth
parameter_list|)
block|{
assert|assert
name|config
operator|.
name|valid
argument_list|()
operator|:
literal|"value source config is invalid - must have either a field context or a script or marked as unmapped"
assert|;
assert|assert
operator|!
name|config
operator|.
name|unmapped
operator|:
literal|"value source should not be created for unmapped fields"
assert|;
if|if
condition|(
name|perDepthFieldDataSources
operator|.
name|length
operator|<=
name|depth
condition|)
block|{
name|perDepthFieldDataSources
operator|=
name|Arrays
operator|.
name|copyOf
argument_list|(
name|perDepthFieldDataSources
argument_list|,
name|ArrayUtil
operator|.
name|oversize
argument_list|(
literal|1
operator|+
name|depth
argument_list|,
name|RamUsageEstimator
operator|.
name|NUM_BYTES_OBJECT_REF
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|perDepthFieldDataSources
index|[
name|depth
index|]
operator|==
literal|null
condition|)
block|{
name|perDepthFieldDataSources
index|[
name|depth
index|]
operator|=
operator|new
name|ObjectObjectOpenHashMap
argument_list|<
name|String
argument_list|,
name|FieldDataSource
argument_list|>
argument_list|()
expr_stmt|;
block|}
specifier|final
name|ObjectObjectOpenHashMap
argument_list|<
name|String
argument_list|,
name|FieldDataSource
argument_list|>
name|fieldDataSources
init|=
name|perDepthFieldDataSources
index|[
name|depth
index|]
decl_stmt|;
if|if
condition|(
name|config
operator|.
name|fieldContext
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|NumericValuesSource
operator|.
name|class
operator|.
name|isAssignableFrom
argument_list|(
name|config
operator|.
name|valueSourceType
argument_list|)
condition|)
block|{
return|return
operator|(
name|VS
operator|)
name|numericScript
argument_list|(
name|config
argument_list|)
return|;
block|}
if|if
condition|(
name|BytesValuesSource
operator|.
name|class
operator|.
name|isAssignableFrom
argument_list|(
name|config
operator|.
name|valueSourceType
argument_list|)
condition|)
block|{
return|return
operator|(
name|VS
operator|)
name|bytesScript
argument_list|(
name|config
argument_list|)
return|;
block|}
throw|throw
operator|new
name|AggregationExecutionException
argument_list|(
literal|"value source of type ["
operator|+
name|config
operator|.
name|valueSourceType
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|"] is not supported by scripts"
argument_list|)
throw|;
block|}
if|if
condition|(
name|NumericValuesSource
operator|.
name|class
operator|.
name|isAssignableFrom
argument_list|(
name|config
operator|.
name|valueSourceType
argument_list|)
condition|)
block|{
return|return
operator|(
name|VS
operator|)
name|numericField
argument_list|(
name|fieldDataSources
argument_list|,
name|config
argument_list|)
return|;
block|}
if|if
condition|(
name|GeoPointValuesSource
operator|.
name|class
operator|.
name|isAssignableFrom
argument_list|(
name|config
operator|.
name|valueSourceType
argument_list|)
condition|)
block|{
return|return
operator|(
name|VS
operator|)
name|geoPointField
argument_list|(
name|fieldDataSources
argument_list|,
name|config
argument_list|)
return|;
block|}
comment|// falling back to bytes values
return|return
operator|(
name|VS
operator|)
name|bytesField
argument_list|(
name|fieldDataSources
argument_list|,
name|config
argument_list|)
return|;
block|}
DECL|method|numericScript
specifier|private
name|NumericValuesSource
name|numericScript
parameter_list|(
name|ValuesSourceConfig
argument_list|<
name|?
argument_list|>
name|config
parameter_list|)
block|{
name|setScorerIfNeeded
argument_list|(
name|config
operator|.
name|script
argument_list|)
expr_stmt|;
name|setReaderIfNeeded
argument_list|(
name|config
operator|.
name|script
argument_list|)
expr_stmt|;
name|scorerAwares
operator|.
name|add
argument_list|(
name|config
operator|.
name|script
argument_list|)
expr_stmt|;
name|readerAwares
operator|.
name|add
argument_list|(
name|config
operator|.
name|script
argument_list|)
expr_stmt|;
name|FieldDataSource
operator|.
name|Numeric
name|source
init|=
operator|new
name|FieldDataSource
operator|.
name|Numeric
operator|.
name|Script
argument_list|(
name|config
operator|.
name|script
argument_list|,
name|config
operator|.
name|scriptValueType
argument_list|)
decl_stmt|;
if|if
condition|(
name|config
operator|.
name|ensureUnique
operator|||
name|config
operator|.
name|ensureSorted
condition|)
block|{
name|source
operator|=
operator|new
name|FieldDataSource
operator|.
name|Numeric
operator|.
name|SortedAndUnique
argument_list|(
name|source
argument_list|)
expr_stmt|;
name|readerAwares
operator|.
name|add
argument_list|(
operator|(
name|ReaderContextAware
operator|)
name|source
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|NumericValuesSource
argument_list|(
name|source
argument_list|,
name|config
operator|.
name|formatter
argument_list|()
argument_list|,
name|config
operator|.
name|parser
argument_list|()
argument_list|)
return|;
block|}
DECL|method|numericField
specifier|private
name|NumericValuesSource
name|numericField
parameter_list|(
name|ObjectObjectOpenHashMap
argument_list|<
name|String
argument_list|,
name|FieldDataSource
argument_list|>
name|fieldDataSources
parameter_list|,
name|ValuesSourceConfig
argument_list|<
name|?
argument_list|>
name|config
parameter_list|)
block|{
name|FieldDataSource
operator|.
name|Numeric
name|dataSource
init|=
operator|(
name|FieldDataSource
operator|.
name|Numeric
operator|)
name|fieldDataSources
operator|.
name|get
argument_list|(
name|config
operator|.
name|fieldContext
operator|.
name|field
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|dataSource
operator|==
literal|null
condition|)
block|{
name|FieldDataSource
operator|.
name|MetaData
name|metaData
init|=
name|FieldDataSource
operator|.
name|MetaData
operator|.
name|load
argument_list|(
name|config
operator|.
name|fieldContext
operator|.
name|indexFieldData
argument_list|()
argument_list|,
name|searchContext
argument_list|)
decl_stmt|;
name|dataSource
operator|=
operator|new
name|FieldDataSource
operator|.
name|Numeric
operator|.
name|FieldData
argument_list|(
operator|(
name|IndexNumericFieldData
argument_list|<
name|?
argument_list|>
operator|)
name|config
operator|.
name|fieldContext
operator|.
name|indexFieldData
argument_list|()
argument_list|,
name|metaData
argument_list|)
expr_stmt|;
name|setReaderIfNeeded
argument_list|(
operator|(
name|ReaderContextAware
operator|)
name|dataSource
argument_list|)
expr_stmt|;
name|readerAwares
operator|.
name|add
argument_list|(
operator|(
name|ReaderContextAware
operator|)
name|dataSource
argument_list|)
expr_stmt|;
name|fieldDataSources
operator|.
name|put
argument_list|(
name|config
operator|.
name|fieldContext
operator|.
name|field
argument_list|()
argument_list|,
name|dataSource
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|config
operator|.
name|script
operator|!=
literal|null
condition|)
block|{
name|setScorerIfNeeded
argument_list|(
name|config
operator|.
name|script
argument_list|)
expr_stmt|;
name|setReaderIfNeeded
argument_list|(
name|config
operator|.
name|script
argument_list|)
expr_stmt|;
name|scorerAwares
operator|.
name|add
argument_list|(
name|config
operator|.
name|script
argument_list|)
expr_stmt|;
name|readerAwares
operator|.
name|add
argument_list|(
name|config
operator|.
name|script
argument_list|)
expr_stmt|;
name|dataSource
operator|=
operator|new
name|FieldDataSource
operator|.
name|Numeric
operator|.
name|WithScript
argument_list|(
name|dataSource
argument_list|,
name|config
operator|.
name|script
argument_list|)
expr_stmt|;
if|if
condition|(
name|config
operator|.
name|ensureUnique
operator|||
name|config
operator|.
name|ensureSorted
condition|)
block|{
name|dataSource
operator|=
operator|new
name|FieldDataSource
operator|.
name|Numeric
operator|.
name|SortedAndUnique
argument_list|(
name|dataSource
argument_list|)
expr_stmt|;
name|readerAwares
operator|.
name|add
argument_list|(
operator|(
name|ReaderContextAware
operator|)
name|dataSource
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|config
operator|.
name|needsHashes
condition|)
block|{
name|dataSource
operator|.
name|setNeedsHashes
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|NumericValuesSource
argument_list|(
name|dataSource
argument_list|,
name|config
operator|.
name|formatter
argument_list|()
argument_list|,
name|config
operator|.
name|parser
argument_list|()
argument_list|)
return|;
block|}
DECL|method|bytesField
specifier|private
name|ValuesSource
name|bytesField
parameter_list|(
name|ObjectObjectOpenHashMap
argument_list|<
name|String
argument_list|,
name|FieldDataSource
argument_list|>
name|fieldDataSources
parameter_list|,
name|ValuesSourceConfig
argument_list|<
name|?
argument_list|>
name|config
parameter_list|)
block|{
name|FieldDataSource
name|dataSource
init|=
name|fieldDataSources
operator|.
name|get
argument_list|(
name|config
operator|.
name|fieldContext
operator|.
name|field
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|dataSource
operator|==
literal|null
condition|)
block|{
specifier|final
name|IndexFieldData
argument_list|<
name|?
argument_list|>
name|indexFieldData
init|=
name|config
operator|.
name|fieldContext
operator|.
name|indexFieldData
argument_list|()
decl_stmt|;
name|FieldDataSource
operator|.
name|MetaData
name|metaData
init|=
name|FieldDataSource
operator|.
name|MetaData
operator|.
name|load
argument_list|(
name|config
operator|.
name|fieldContext
operator|.
name|indexFieldData
argument_list|()
argument_list|,
name|searchContext
argument_list|)
decl_stmt|;
if|if
condition|(
name|indexFieldData
operator|instanceof
name|IndexFieldData
operator|.
name|WithOrdinals
condition|)
block|{
name|dataSource
operator|=
operator|new
name|FieldDataSource
operator|.
name|Bytes
operator|.
name|WithOrdinals
operator|.
name|FieldData
argument_list|(
operator|(
name|IndexFieldData
operator|.
name|WithOrdinals
operator|)
name|indexFieldData
argument_list|,
name|metaData
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|dataSource
operator|=
operator|new
name|FieldDataSource
operator|.
name|Bytes
operator|.
name|FieldData
argument_list|(
name|indexFieldData
argument_list|,
name|metaData
argument_list|)
expr_stmt|;
block|}
name|setReaderIfNeeded
argument_list|(
operator|(
name|ReaderContextAware
operator|)
name|dataSource
argument_list|)
expr_stmt|;
name|readerAwares
operator|.
name|add
argument_list|(
operator|(
name|ReaderContextAware
operator|)
name|dataSource
argument_list|)
expr_stmt|;
name|fieldDataSources
operator|.
name|put
argument_list|(
name|config
operator|.
name|fieldContext
operator|.
name|field
argument_list|()
argument_list|,
name|dataSource
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|config
operator|.
name|script
operator|!=
literal|null
condition|)
block|{
name|setScorerIfNeeded
argument_list|(
name|config
operator|.
name|script
argument_list|)
expr_stmt|;
name|setReaderIfNeeded
argument_list|(
name|config
operator|.
name|script
argument_list|)
expr_stmt|;
name|scorerAwares
operator|.
name|add
argument_list|(
name|config
operator|.
name|script
argument_list|)
expr_stmt|;
name|readerAwares
operator|.
name|add
argument_list|(
name|config
operator|.
name|script
argument_list|)
expr_stmt|;
name|dataSource
operator|=
operator|new
name|FieldDataSource
operator|.
name|WithScript
argument_list|(
name|dataSource
argument_list|,
name|config
operator|.
name|script
argument_list|)
expr_stmt|;
block|}
comment|// Even in case we wrap field data, we might still need to wrap for sorting, because the wrapped field data might be
comment|// eg. a numeric field data that doesn't sort according to the byte order. However field data values are unique so no
comment|// need to wrap for uniqueness
if|if
condition|(
operator|(
name|config
operator|.
name|ensureUnique
operator|&&
operator|!
name|dataSource
operator|.
name|metaData
argument_list|()
operator|.
name|uniqueness
argument_list|()
operator|.
name|unique
argument_list|()
operator|)
operator|||
name|config
operator|.
name|ensureSorted
condition|)
block|{
name|dataSource
operator|=
operator|new
name|FieldDataSource
operator|.
name|Bytes
operator|.
name|SortedAndUnique
argument_list|(
name|dataSource
argument_list|)
expr_stmt|;
name|readerAwares
operator|.
name|add
argument_list|(
operator|(
name|ReaderContextAware
operator|)
name|dataSource
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|config
operator|.
name|needsHashes
condition|)
block|{
comment|// the data source needs hash if at least one consumer needs hashes
name|dataSource
operator|.
name|setNeedsHashes
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|dataSource
operator|instanceof
name|FieldDataSource
operator|.
name|Bytes
operator|.
name|WithOrdinals
condition|)
block|{
return|return
operator|new
name|BytesValuesSource
operator|.
name|WithOrdinals
argument_list|(
operator|(
name|FieldDataSource
operator|.
name|Bytes
operator|.
name|WithOrdinals
operator|)
name|dataSource
argument_list|)
return|;
block|}
else|else
block|{
return|return
operator|new
name|BytesValuesSource
argument_list|(
name|dataSource
argument_list|)
return|;
block|}
block|}
DECL|method|bytesScript
specifier|private
name|BytesValuesSource
name|bytesScript
parameter_list|(
name|ValuesSourceConfig
argument_list|<
name|?
argument_list|>
name|config
parameter_list|)
block|{
name|setScorerIfNeeded
argument_list|(
name|config
operator|.
name|script
argument_list|)
expr_stmt|;
name|setReaderIfNeeded
argument_list|(
name|config
operator|.
name|script
argument_list|)
expr_stmt|;
name|scorerAwares
operator|.
name|add
argument_list|(
name|config
operator|.
name|script
argument_list|)
expr_stmt|;
name|readerAwares
operator|.
name|add
argument_list|(
name|config
operator|.
name|script
argument_list|)
expr_stmt|;
name|FieldDataSource
operator|.
name|Bytes
name|source
init|=
operator|new
name|FieldDataSource
operator|.
name|Bytes
operator|.
name|Script
argument_list|(
name|config
operator|.
name|script
argument_list|)
decl_stmt|;
if|if
condition|(
name|config
operator|.
name|ensureUnique
operator|||
name|config
operator|.
name|ensureSorted
condition|)
block|{
name|source
operator|=
operator|new
name|FieldDataSource
operator|.
name|Bytes
operator|.
name|SortedAndUnique
argument_list|(
name|source
argument_list|)
expr_stmt|;
name|readerAwares
operator|.
name|add
argument_list|(
operator|(
name|ReaderContextAware
operator|)
name|source
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|BytesValuesSource
argument_list|(
name|source
argument_list|)
return|;
block|}
DECL|method|geoPointField
specifier|private
name|GeoPointValuesSource
name|geoPointField
parameter_list|(
name|ObjectObjectOpenHashMap
argument_list|<
name|String
argument_list|,
name|FieldDataSource
argument_list|>
name|fieldDataSources
parameter_list|,
name|ValuesSourceConfig
argument_list|<
name|?
argument_list|>
name|config
parameter_list|)
block|{
name|FieldDataSource
operator|.
name|GeoPoint
name|dataSource
init|=
operator|(
name|FieldDataSource
operator|.
name|GeoPoint
operator|)
name|fieldDataSources
operator|.
name|get
argument_list|(
name|config
operator|.
name|fieldContext
operator|.
name|field
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|dataSource
operator|==
literal|null
condition|)
block|{
name|FieldDataSource
operator|.
name|MetaData
name|metaData
init|=
name|FieldDataSource
operator|.
name|MetaData
operator|.
name|load
argument_list|(
name|config
operator|.
name|fieldContext
operator|.
name|indexFieldData
argument_list|()
argument_list|,
name|searchContext
argument_list|)
decl_stmt|;
name|dataSource
operator|=
operator|new
name|FieldDataSource
operator|.
name|GeoPoint
argument_list|(
operator|(
name|IndexGeoPointFieldData
argument_list|<
name|?
argument_list|>
operator|)
name|config
operator|.
name|fieldContext
operator|.
name|indexFieldData
argument_list|()
argument_list|,
name|metaData
argument_list|)
expr_stmt|;
name|setReaderIfNeeded
argument_list|(
name|dataSource
argument_list|)
expr_stmt|;
name|readerAwares
operator|.
name|add
argument_list|(
name|dataSource
argument_list|)
expr_stmt|;
name|fieldDataSources
operator|.
name|put
argument_list|(
name|config
operator|.
name|fieldContext
operator|.
name|field
argument_list|()
argument_list|,
name|dataSource
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|config
operator|.
name|needsHashes
condition|)
block|{
name|dataSource
operator|.
name|setNeedsHashes
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|GeoPointValuesSource
argument_list|(
name|dataSource
argument_list|)
return|;
block|}
DECL|method|registerReaderContextAware
specifier|public
name|void
name|registerReaderContextAware
parameter_list|(
name|ReaderContextAware
name|readerContextAware
parameter_list|)
block|{
name|setReaderIfNeeded
argument_list|(
name|readerContextAware
argument_list|)
expr_stmt|;
name|readerAwares
operator|.
name|add
argument_list|(
name|readerContextAware
argument_list|)
expr_stmt|;
block|}
DECL|method|registerScorerAware
specifier|public
name|void
name|registerScorerAware
parameter_list|(
name|ScorerAware
name|scorerAware
parameter_list|)
block|{
name|setScorerIfNeeded
argument_list|(
name|scorerAware
argument_list|)
expr_stmt|;
name|scorerAwares
operator|.
name|add
argument_list|(
name|scorerAware
argument_list|)
expr_stmt|;
block|}
DECL|method|setReaderIfNeeded
specifier|private
name|void
name|setReaderIfNeeded
parameter_list|(
name|ReaderContextAware
name|readerAware
parameter_list|)
block|{
if|if
condition|(
name|reader
operator|!=
literal|null
condition|)
block|{
name|readerAware
operator|.
name|setNextReader
argument_list|(
name|reader
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|setScorerIfNeeded
specifier|private
name|void
name|setScorerIfNeeded
parameter_list|(
name|ScorerAware
name|scorerAware
parameter_list|)
block|{
if|if
condition|(
name|scorer
operator|!=
literal|null
condition|)
block|{
name|scorerAware
operator|.
name|setScorer
argument_list|(
name|scorer
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

