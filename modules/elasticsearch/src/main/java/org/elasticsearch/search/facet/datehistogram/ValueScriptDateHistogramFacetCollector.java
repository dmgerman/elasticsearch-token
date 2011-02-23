begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.facet.datehistogram
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|facet
operator|.
name|datehistogram
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
name|Scorer
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
name|joda
operator|.
name|time
operator|.
name|MutableDateTime
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
name|trove
operator|.
name|map
operator|.
name|hash
operator|.
name|TLongDoubleHashMap
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
name|trove
operator|.
name|map
operator|.
name|hash
operator|.
name|TLongLongHashMap
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
name|field
operator|.
name|data
operator|.
name|FieldDataCache
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
name|field
operator|.
name|data
operator|.
name|FieldDataType
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
name|field
operator|.
name|data
operator|.
name|longs
operator|.
name|LongFieldData
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
name|mapper
operator|.
name|FieldMapper
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
name|mapper
operator|.
name|MapperService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|SearchScript
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
name|facet
operator|.
name|AbstractFacetCollector
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
name|facet
operator|.
name|Facet
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
name|facet
operator|.
name|FacetPhaseExecutionException
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
name|Map
import|;
end_import

begin_comment
comment|/**  * A histogram facet collector that uses the same field as the key as well as the  * value.  *  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|ValueScriptDateHistogramFacetCollector
specifier|public
class|class
name|ValueScriptDateHistogramFacetCollector
extends|extends
name|AbstractFacetCollector
block|{
DECL|field|indexFieldName
specifier|private
specifier|final
name|String
name|indexFieldName
decl_stmt|;
DECL|field|dateTime
specifier|private
specifier|final
name|MutableDateTime
name|dateTime
decl_stmt|;
DECL|field|comparatorType
specifier|private
specifier|final
name|DateHistogramFacet
operator|.
name|ComparatorType
name|comparatorType
decl_stmt|;
DECL|field|fieldDataCache
specifier|private
specifier|final
name|FieldDataCache
name|fieldDataCache
decl_stmt|;
DECL|field|fieldDataType
specifier|private
specifier|final
name|FieldDataType
name|fieldDataType
decl_stmt|;
DECL|field|fieldData
specifier|private
name|LongFieldData
name|fieldData
decl_stmt|;
DECL|field|valueScript
specifier|private
specifier|final
name|SearchScript
name|valueScript
decl_stmt|;
DECL|field|histoProc
specifier|private
specifier|final
name|DateHistogramProc
name|histoProc
decl_stmt|;
DECL|method|ValueScriptDateHistogramFacetCollector
specifier|public
name|ValueScriptDateHistogramFacetCollector
parameter_list|(
name|String
name|facetName
parameter_list|,
name|String
name|fieldName
parameter_list|,
name|String
name|scriptLang
parameter_list|,
name|String
name|valueScript
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|params
parameter_list|,
name|MutableDateTime
name|dateTime
parameter_list|,
name|long
name|interval
parameter_list|,
name|DateHistogramFacet
operator|.
name|ComparatorType
name|comparatorType
parameter_list|,
name|SearchContext
name|context
parameter_list|)
block|{
name|super
argument_list|(
name|facetName
argument_list|)
expr_stmt|;
name|this
operator|.
name|dateTime
operator|=
name|dateTime
expr_stmt|;
name|this
operator|.
name|comparatorType
operator|=
name|comparatorType
expr_stmt|;
name|this
operator|.
name|fieldDataCache
operator|=
name|context
operator|.
name|fieldDataCache
argument_list|()
expr_stmt|;
name|MapperService
operator|.
name|SmartNameFieldMappers
name|smartMappers
init|=
name|context
operator|.
name|mapperService
argument_list|()
operator|.
name|smartName
argument_list|(
name|fieldName
argument_list|)
decl_stmt|;
if|if
condition|(
name|smartMappers
operator|==
literal|null
operator|||
operator|!
name|smartMappers
operator|.
name|hasMapper
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|FacetPhaseExecutionException
argument_list|(
name|facetName
argument_list|,
literal|"No mapping found for field ["
operator|+
name|fieldName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
comment|// add type filter if there is exact doc mapper associated with it
if|if
condition|(
name|smartMappers
operator|.
name|hasDocMapper
argument_list|()
condition|)
block|{
name|setFilter
argument_list|(
name|context
operator|.
name|filterCache
argument_list|()
operator|.
name|cache
argument_list|(
name|smartMappers
operator|.
name|docMapper
argument_list|()
operator|.
name|typeFilter
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|valueScript
operator|=
name|context
operator|.
name|scriptService
argument_list|()
operator|.
name|search
argument_list|(
name|context
operator|.
name|lookup
argument_list|()
argument_list|,
name|scriptLang
argument_list|,
name|valueScript
argument_list|,
name|params
argument_list|)
expr_stmt|;
name|FieldMapper
name|mapper
init|=
name|smartMappers
operator|.
name|mapper
argument_list|()
decl_stmt|;
name|indexFieldName
operator|=
name|mapper
operator|.
name|names
argument_list|()
operator|.
name|indexName
argument_list|()
expr_stmt|;
name|fieldDataType
operator|=
name|mapper
operator|.
name|fieldDataType
argument_list|()
expr_stmt|;
if|if
condition|(
name|interval
operator|==
literal|1
condition|)
block|{
name|histoProc
operator|=
operator|new
name|DateHistogramProc
argument_list|(
name|this
operator|.
name|valueScript
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|histoProc
operator|=
operator|new
name|IntervalDateHistogramProc
argument_list|(
name|interval
argument_list|,
name|this
operator|.
name|valueScript
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|doCollect
annotation|@
name|Override
specifier|protected
name|void
name|doCollect
parameter_list|(
name|int
name|doc
parameter_list|)
throws|throws
name|IOException
block|{
name|fieldData
operator|.
name|forEachValueInDoc
argument_list|(
name|doc
argument_list|,
name|dateTime
argument_list|,
name|histoProc
argument_list|)
expr_stmt|;
block|}
DECL|method|setScorer
annotation|@
name|Override
specifier|public
name|void
name|setScorer
parameter_list|(
name|Scorer
name|scorer
parameter_list|)
throws|throws
name|IOException
block|{
name|valueScript
operator|.
name|setScorer
argument_list|(
name|scorer
argument_list|)
expr_stmt|;
block|}
DECL|method|doSetNextReader
annotation|@
name|Override
specifier|protected
name|void
name|doSetNextReader
parameter_list|(
name|IndexReader
name|reader
parameter_list|,
name|int
name|docBase
parameter_list|)
throws|throws
name|IOException
block|{
name|fieldData
operator|=
operator|(
name|LongFieldData
operator|)
name|fieldDataCache
operator|.
name|cache
argument_list|(
name|fieldDataType
argument_list|,
name|reader
argument_list|,
name|indexFieldName
argument_list|)
expr_stmt|;
name|valueScript
operator|.
name|setNextReader
argument_list|(
name|reader
argument_list|)
expr_stmt|;
block|}
DECL|method|facet
annotation|@
name|Override
specifier|public
name|Facet
name|facet
parameter_list|()
block|{
return|return
operator|new
name|InternalCountAndTotalDateHistogramFacet
argument_list|(
name|facetName
argument_list|,
name|comparatorType
argument_list|,
name|histoProc
operator|.
name|counts
argument_list|()
argument_list|,
name|histoProc
operator|.
name|totals
argument_list|()
argument_list|)
return|;
block|}
DECL|class|DateHistogramProc
specifier|public
specifier|static
class|class
name|DateHistogramProc
implements|implements
name|LongFieldData
operator|.
name|DateValueInDocProc
block|{
DECL|field|valueScript
specifier|protected
specifier|final
name|SearchScript
name|valueScript
decl_stmt|;
DECL|field|counts
specifier|protected
specifier|final
name|TLongLongHashMap
name|counts
init|=
operator|new
name|TLongLongHashMap
argument_list|()
decl_stmt|;
DECL|field|totals
specifier|protected
specifier|final
name|TLongDoubleHashMap
name|totals
init|=
operator|new
name|TLongDoubleHashMap
argument_list|()
decl_stmt|;
DECL|method|DateHistogramProc
specifier|public
name|DateHistogramProc
parameter_list|(
name|SearchScript
name|valueScript
parameter_list|)
block|{
name|this
operator|.
name|valueScript
operator|=
name|valueScript
expr_stmt|;
block|}
DECL|method|onValue
annotation|@
name|Override
specifier|public
name|void
name|onValue
parameter_list|(
name|int
name|docId
parameter_list|,
name|MutableDateTime
name|dateTime
parameter_list|)
block|{
name|valueScript
operator|.
name|setNextDocId
argument_list|(
name|docId
argument_list|)
expr_stmt|;
name|long
name|time
init|=
name|dateTime
operator|.
name|getMillis
argument_list|()
decl_stmt|;
name|counts
operator|.
name|adjustOrPutValue
argument_list|(
name|time
argument_list|,
literal|1
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|double
name|scriptValue
init|=
name|valueScript
operator|.
name|runAsDouble
argument_list|()
decl_stmt|;
name|totals
operator|.
name|adjustOrPutValue
argument_list|(
name|time
argument_list|,
name|scriptValue
argument_list|,
name|scriptValue
argument_list|)
expr_stmt|;
block|}
DECL|method|counts
specifier|public
name|TLongLongHashMap
name|counts
parameter_list|()
block|{
return|return
name|counts
return|;
block|}
DECL|method|totals
specifier|public
name|TLongDoubleHashMap
name|totals
parameter_list|()
block|{
return|return
name|totals
return|;
block|}
block|}
DECL|class|IntervalDateHistogramProc
specifier|public
specifier|static
class|class
name|IntervalDateHistogramProc
extends|extends
name|DateHistogramProc
block|{
DECL|field|interval
specifier|private
specifier|final
name|long
name|interval
decl_stmt|;
DECL|method|IntervalDateHistogramProc
specifier|public
name|IntervalDateHistogramProc
parameter_list|(
name|long
name|interval
parameter_list|,
name|SearchScript
name|valueScript
parameter_list|)
block|{
name|super
argument_list|(
name|valueScript
argument_list|)
expr_stmt|;
name|this
operator|.
name|interval
operator|=
name|interval
expr_stmt|;
block|}
DECL|method|onValue
annotation|@
name|Override
specifier|public
name|void
name|onValue
parameter_list|(
name|int
name|docId
parameter_list|,
name|MutableDateTime
name|dateTime
parameter_list|)
block|{
name|valueScript
operator|.
name|setNextDocId
argument_list|(
name|docId
argument_list|)
expr_stmt|;
name|long
name|bucket
init|=
name|CountDateHistogramFacetCollector
operator|.
name|bucket
argument_list|(
name|dateTime
operator|.
name|getMillis
argument_list|()
argument_list|,
name|interval
argument_list|)
decl_stmt|;
name|counts
operator|.
name|adjustOrPutValue
argument_list|(
name|bucket
argument_list|,
literal|1
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|double
name|scriptValue
init|=
name|valueScript
operator|.
name|runAsDouble
argument_list|()
decl_stmt|;
name|totals
operator|.
name|adjustOrPutValue
argument_list|(
name|bucket
argument_list|,
name|scriptValue
argument_list|,
name|scriptValue
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

