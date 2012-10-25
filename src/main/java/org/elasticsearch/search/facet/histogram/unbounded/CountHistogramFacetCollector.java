begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.facet.histogram.unbounded
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|facet
operator|.
name|histogram
operator|.
name|unbounded
package|;
end_package

begin_import
import|import
name|gnu
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
name|NumericFieldData
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
name|facet
operator|.
name|histogram
operator|.
name|HistogramFacet
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

begin_comment
comment|/**  * A histogram facet collector that uses the same field as the key as well as the  * value.  */
end_comment

begin_class
DECL|class|CountHistogramFacetCollector
specifier|public
class|class
name|CountHistogramFacetCollector
extends|extends
name|AbstractFacetCollector
block|{
DECL|field|indexFieldName
specifier|private
specifier|final
name|String
name|indexFieldName
decl_stmt|;
DECL|field|comparatorType
specifier|private
specifier|final
name|HistogramFacet
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
name|NumericFieldData
name|fieldData
decl_stmt|;
DECL|field|histoProc
specifier|private
specifier|final
name|HistogramProc
name|histoProc
decl_stmt|;
DECL|method|CountHistogramFacetCollector
specifier|public
name|CountHistogramFacetCollector
parameter_list|(
name|String
name|facetName
parameter_list|,
name|String
name|fieldName
parameter_list|,
name|long
name|interval
parameter_list|,
name|HistogramFacet
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
name|smartFieldMappers
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
name|explicitTypeInNameWithDocMapper
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
name|histoProc
operator|=
operator|new
name|HistogramProc
argument_list|(
name|interval
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doCollect
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
name|histoProc
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doSetNextReader
specifier|protected
name|void
name|doSetNextReader
parameter_list|(
name|AtomicReaderContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|fieldData
operator|=
operator|(
name|NumericFieldData
operator|)
name|fieldDataCache
operator|.
name|cache
argument_list|(
name|fieldDataType
argument_list|,
name|context
operator|.
name|reader
argument_list|()
argument_list|,
name|indexFieldName
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|facet
specifier|public
name|Facet
name|facet
parameter_list|()
block|{
return|return
operator|new
name|InternalCountHistogramFacet
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
literal|true
argument_list|)
return|;
block|}
DECL|method|bucket
specifier|public
specifier|static
name|long
name|bucket
parameter_list|(
name|double
name|value
parameter_list|,
name|long
name|interval
parameter_list|)
block|{
return|return
operator|(
operator|(
call|(
name|long
call|)
argument_list|(
name|value
operator|/
name|interval
argument_list|)
operator|)
operator|*
name|interval
operator|)
return|;
block|}
DECL|class|HistogramProc
specifier|public
specifier|static
class|class
name|HistogramProc
implements|implements
name|NumericFieldData
operator|.
name|DoubleValueInDocProc
block|{
DECL|field|interval
specifier|private
specifier|final
name|long
name|interval
decl_stmt|;
DECL|field|counts
specifier|private
specifier|final
name|TLongLongHashMap
name|counts
init|=
name|CacheRecycler
operator|.
name|popLongLongMap
argument_list|()
decl_stmt|;
DECL|method|HistogramProc
specifier|public
name|HistogramProc
parameter_list|(
name|long
name|interval
parameter_list|)
block|{
name|this
operator|.
name|interval
operator|=
name|interval
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|onValue
specifier|public
name|void
name|onValue
parameter_list|(
name|int
name|docId
parameter_list|,
name|double
name|value
parameter_list|)
block|{
name|long
name|bucket
init|=
name|bucket
argument_list|(
name|value
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
block|}
block|}
end_class

end_unit

