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
name|fielddata
operator|.
name|GeoPointValues
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
DECL|class|GeoDistanceFacetCollector
specifier|public
class|class
name|GeoDistanceFacetCollector
extends|extends
name|AbstractFacetCollector
block|{
DECL|field|indexFieldData
specifier|protected
specifier|final
name|IndexGeoPointFieldData
name|indexFieldData
decl_stmt|;
DECL|field|lat
specifier|protected
specifier|final
name|double
name|lat
decl_stmt|;
DECL|field|lon
specifier|protected
specifier|final
name|double
name|lon
decl_stmt|;
DECL|field|unit
specifier|protected
specifier|final
name|DistanceUnit
name|unit
decl_stmt|;
DECL|field|geoDistance
specifier|protected
specifier|final
name|GeoDistance
name|geoDistance
decl_stmt|;
DECL|field|fixedSourceDistance
specifier|protected
specifier|final
name|GeoDistance
operator|.
name|FixedSourceDistance
name|fixedSourceDistance
decl_stmt|;
DECL|field|values
specifier|protected
name|GeoPointValues
name|values
decl_stmt|;
DECL|field|entries
specifier|protected
specifier|final
name|GeoDistanceFacet
operator|.
name|Entry
index|[]
name|entries
decl_stmt|;
DECL|field|aggregator
specifier|protected
name|GeoPointValues
operator|.
name|LatLonValueInDocProc
name|aggregator
decl_stmt|;
DECL|method|GeoDistanceFacetCollector
specifier|public
name|GeoDistanceFacetCollector
parameter_list|(
name|String
name|facetName
parameter_list|,
name|IndexGeoPointFieldData
name|indexFieldData
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
parameter_list|)
block|{
name|super
argument_list|(
name|facetName
argument_list|)
expr_stmt|;
name|this
operator|.
name|lat
operator|=
name|lat
expr_stmt|;
name|this
operator|.
name|lon
operator|=
name|lon
expr_stmt|;
name|this
operator|.
name|unit
operator|=
name|unit
expr_stmt|;
name|this
operator|.
name|entries
operator|=
name|entries
expr_stmt|;
name|this
operator|.
name|geoDistance
operator|=
name|geoDistance
expr_stmt|;
name|this
operator|.
name|indexFieldData
operator|=
name|indexFieldData
expr_stmt|;
name|this
operator|.
name|fixedSourceDistance
operator|=
name|geoDistance
operator|.
name|fixedSourceDistance
argument_list|(
name|lat
argument_list|,
name|lon
argument_list|,
name|unit
argument_list|)
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
name|values
operator|=
name|indexFieldData
operator|.
name|load
argument_list|(
name|context
argument_list|)
operator|.
name|getGeoPointValues
argument_list|()
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
name|entry
operator|.
name|foundInDoc
operator|=
literal|false
expr_stmt|;
block|}
name|values
operator|.
name|forEachLatLonValueInDoc
argument_list|(
name|doc
argument_list|,
name|aggregator
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
name|InternalGeoDistanceFacet
argument_list|(
name|facetName
argument_list|,
name|entries
argument_list|)
return|;
block|}
DECL|class|Aggregator
specifier|public
specifier|static
class|class
name|Aggregator
implements|implements
name|GeoPointValues
operator|.
name|LatLonValueInDocProc
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
DECL|method|onMissing
specifier|public
name|void
name|onMissing
parameter_list|(
name|int
name|docId
parameter_list|)
block|{         }
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
name|entry
operator|.
name|totalCount
operator|++
expr_stmt|;
name|entry
operator|.
name|total
operator|+=
name|distance
expr_stmt|;
if|if
condition|(
name|distance
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
name|distance
expr_stmt|;
block|}
if|if
condition|(
name|distance
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
name|distance
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
block|}
end_class

end_unit

