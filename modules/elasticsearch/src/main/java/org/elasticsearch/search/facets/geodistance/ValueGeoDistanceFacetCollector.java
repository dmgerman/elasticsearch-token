begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.facets.geodistance
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|facets
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
name|IndexReader
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
name|xcontent
operator|.
name|geo
operator|.
name|GeoPoint
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
name|facets
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
name|facets
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
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|ValueGeoDistanceFacetCollector
specifier|public
class|class
name|ValueGeoDistanceFacetCollector
extends|extends
name|GeoDistanceFacetCollector
block|{
DECL|field|valueFieldName
specifier|private
specifier|final
name|String
name|valueFieldName
decl_stmt|;
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
DECL|field|valueFieldData
specifier|private
name|NumericFieldData
name|valueFieldData
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
name|this
operator|.
name|valueFieldName
operator|=
name|valueFieldName
expr_stmt|;
name|FieldMapper
name|mapper
init|=
name|context
operator|.
name|mapperService
argument_list|()
operator|.
name|smartNameFieldMapper
argument_list|(
name|valueFieldName
argument_list|)
decl_stmt|;
if|if
condition|(
name|mapper
operator|==
literal|null
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
name|valueFieldName
expr_stmt|;
name|this
operator|.
name|valueFieldDataType
operator|=
name|mapper
operator|.
name|fieldDataType
argument_list|()
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
name|super
operator|.
name|doSetNextReader
argument_list|(
name|reader
argument_list|,
name|docBase
argument_list|)
expr_stmt|;
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
name|reader
argument_list|,
name|indexValueFieldName
argument_list|)
expr_stmt|;
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
if|if
condition|(
operator|!
name|fieldData
operator|.
name|hasValue
argument_list|(
name|doc
argument_list|)
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|fieldData
operator|.
name|multiValued
argument_list|()
condition|)
block|{
name|GeoPoint
index|[]
name|points
init|=
name|fieldData
operator|.
name|values
argument_list|(
name|doc
argument_list|)
decl_stmt|;
name|double
index|[]
name|values
init|=
name|valueFieldData
operator|.
name|multiValued
argument_list|()
condition|?
name|valueFieldData
operator|.
name|doubleValues
argument_list|(
name|doc
argument_list|)
else|:
literal|null
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|points
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|double
name|distance
init|=
name|geoDistance
operator|.
name|calculate
argument_list|(
name|lat
argument_list|,
name|lon
argument_list|,
name|points
index|[
name|i
index|]
operator|.
name|lat
argument_list|()
argument_list|,
name|points
index|[
name|i
index|]
operator|.
name|lon
argument_list|()
argument_list|,
name|unit
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
name|count
operator|++
expr_stmt|;
if|if
condition|(
name|values
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|i
operator|<
name|values
operator|.
name|length
condition|)
block|{
name|entry
operator|.
name|total
operator|+=
name|values
index|[
name|i
index|]
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|valueFieldData
operator|.
name|hasValue
argument_list|(
name|doc
argument_list|)
condition|)
block|{
name|entry
operator|.
name|total
operator|+=
name|valueFieldData
operator|.
name|doubleValue
argument_list|(
name|doc
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
else|else
block|{
name|GeoPoint
name|point
init|=
name|fieldData
operator|.
name|value
argument_list|(
name|doc
argument_list|)
decl_stmt|;
name|double
name|distance
init|=
name|geoDistance
operator|.
name|calculate
argument_list|(
name|lat
argument_list|,
name|lon
argument_list|,
name|point
operator|.
name|lat
argument_list|()
argument_list|,
name|point
operator|.
name|lon
argument_list|()
argument_list|,
name|unit
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
name|count
operator|++
expr_stmt|;
if|if
condition|(
name|valueFieldData
operator|.
name|multiValued
argument_list|()
condition|)
block|{
name|double
index|[]
name|values
init|=
name|valueFieldData
operator|.
name|doubleValues
argument_list|(
name|doc
argument_list|)
decl_stmt|;
for|for
control|(
name|double
name|value
range|:
name|values
control|)
block|{
name|entry
operator|.
name|total
operator|+=
name|value
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|valueFieldData
operator|.
name|hasValue
argument_list|(
name|doc
argument_list|)
condition|)
block|{
name|entry
operator|.
name|total
operator|+=
name|valueFieldData
operator|.
name|doubleValue
argument_list|(
name|doc
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
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
name|InternalGeoDistanceFacet
argument_list|(
name|facetName
argument_list|,
name|fieldName
argument_list|,
name|valueFieldName
argument_list|,
name|unit
argument_list|,
name|entries
argument_list|)
return|;
block|}
block|}
end_class

end_unit

