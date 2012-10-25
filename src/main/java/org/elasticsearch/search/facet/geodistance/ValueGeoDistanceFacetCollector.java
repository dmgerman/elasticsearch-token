begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.facet.geodistance
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|facet
operator|.
name|geodistance
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
name|unit
operator|.
name|DistanceUnit
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
name|MapperService
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
name|geo
operator|.
name|GeoPointFieldData
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
name|search
operator|.
name|geo
operator|.
name|GeoDistance
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

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|ValueGeoDistanceFacetCollector
specifier|public
class|class
name|ValueGeoDistanceFacetCollector
extends|extends
name|GeoDistanceFacetCollector
block|{
DECL|field|indexValueFieldName
specifier|private
specifier|final
name|String
name|indexValueFieldName
decl_stmt|;
DECL|field|valueFieldDataType
specifier|private
specifier|final
name|FieldDataType
name|valueFieldDataType
decl_stmt|;
DECL|method|ValueGeoDistanceFacetCollector
specifier|public
name|ValueGeoDistanceFacetCollector
parameter_list|(
name|String
name|facetName
parameter_list|,
name|String
name|fieldName
parameter_list|,
name|double
name|lat
parameter_list|,
name|double
name|lon
parameter_list|,
name|DistanceUnit
name|unit
parameter_list|,
name|GeoDistance
name|geoDistance
parameter_list|,
name|GeoDistanceFacet
operator|.
name|Entry
index|[]
name|entries
parameter_list|,
name|SearchContext
name|context
parameter_list|,
name|String
name|valueFieldName
parameter_list|)
block|{
name|super
argument_list|(
name|facetName
argument_list|,
name|fieldName
argument_list|,
name|lat
argument_list|,
name|lon
argument_list|,
name|unit
argument_list|,
name|geoDistance
argument_list|,
name|entries
argument_list|,
name|context
argument_list|)
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
name|valueFieldName
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
name|valueFieldName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|this
operator|.
name|indexValueFieldName
operator|=
name|smartMappers
operator|.
name|mapper
argument_list|()
operator|.
name|names
argument_list|()
operator|.
name|indexName
argument_list|()
expr_stmt|;
name|this
operator|.
name|valueFieldDataType
operator|=
name|smartMappers
operator|.
name|mapper
argument_list|()
operator|.
name|fieldDataType
argument_list|()
expr_stmt|;
name|this
operator|.
name|aggregator
operator|=
operator|new
name|Aggregator
argument_list|(
name|fixedSourceDistance
argument_list|,
name|entries
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
name|super
operator|.
name|doSetNextReader
argument_list|(
name|context
argument_list|)
expr_stmt|;
operator|(
operator|(
name|Aggregator
operator|)
name|this
operator|.
name|aggregator
operator|)
operator|.
name|valueFieldData
operator|=
operator|(
name|NumericFieldData
operator|)
name|fieldDataCache
operator|.
name|cache
argument_list|(
name|valueFieldDataType
argument_list|,
name|context
operator|.
name|reader
argument_list|()
argument_list|,
name|indexValueFieldName
argument_list|)
expr_stmt|;
block|}
DECL|class|Aggregator
specifier|public
specifier|static
class|class
name|Aggregator
implements|implements
name|GeoPointFieldData
operator|.
name|ValueInDocProc
block|{
DECL|field|fixedSourceDistance
specifier|private
specifier|final
name|GeoDistance
operator|.
name|FixedSourceDistance
name|fixedSourceDistance
decl_stmt|;
DECL|field|entries
specifier|private
specifier|final
name|GeoDistanceFacet
operator|.
name|Entry
index|[]
name|entries
decl_stmt|;
DECL|field|valueFieldData
name|NumericFieldData
name|valueFieldData
decl_stmt|;
DECL|field|valueAggregator
specifier|final
name|ValueAggregator
name|valueAggregator
init|=
operator|new
name|ValueAggregator
argument_list|()
decl_stmt|;
DECL|method|Aggregator
specifier|public
name|Aggregator
parameter_list|(
name|GeoDistance
operator|.
name|FixedSourceDistance
name|fixedSourceDistance
parameter_list|,
name|GeoDistanceFacet
operator|.
name|Entry
index|[]
name|entries
parameter_list|)
block|{
name|this
operator|.
name|fixedSourceDistance
operator|=
name|fixedSourceDistance
expr_stmt|;
name|this
operator|.
name|entries
operator|=
name|entries
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
name|lat
parameter_list|,
name|double
name|lon
parameter_list|)
block|{
name|double
name|distance
init|=
name|fixedSourceDistance
operator|.
name|calculate
argument_list|(
name|lat
argument_list|,
name|lon
argument_list|)
decl_stmt|;
for|for
control|(
name|GeoDistanceFacet
operator|.
name|Entry
name|entry
range|:
name|entries
control|)
block|{
if|if
condition|(
name|entry
operator|.
name|foundInDoc
condition|)
block|{
continue|continue;
block|}
if|if
condition|(
name|distance
operator|>=
name|entry
operator|.
name|getFrom
argument_list|()
operator|&&
name|distance
operator|<
name|entry
operator|.
name|getTo
argument_list|()
condition|)
block|{
name|entry
operator|.
name|foundInDoc
operator|=
literal|true
expr_stmt|;
name|entry
operator|.
name|count
operator|++
expr_stmt|;
name|valueAggregator
operator|.
name|entry
operator|=
name|entry
expr_stmt|;
name|valueFieldData
operator|.
name|forEachValueInDoc
argument_list|(
name|docId
argument_list|,
name|valueAggregator
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
DECL|class|ValueAggregator
specifier|public
specifier|static
class|class
name|ValueAggregator
implements|implements
name|NumericFieldData
operator|.
name|DoubleValueInDocProc
block|{
DECL|field|entry
name|GeoDistanceFacet
operator|.
name|Entry
name|entry
decl_stmt|;
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
name|entry
operator|.
name|totalCount
operator|++
expr_stmt|;
name|entry
operator|.
name|total
operator|+=
name|value
expr_stmt|;
if|if
condition|(
name|value
operator|<
name|entry
operator|.
name|min
condition|)
block|{
name|entry
operator|.
name|min
operator|=
name|value
expr_stmt|;
block|}
if|if
condition|(
name|value
operator|>
name|entry
operator|.
name|max
condition|)
block|{
name|entry
operator|.
name|max
operator|=
name|value
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

