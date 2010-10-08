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
name|script
operator|.
name|search
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
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|ScriptGeoDistanceFacetCollector
specifier|public
class|class
name|ScriptGeoDistanceFacetCollector
extends|extends
name|GeoDistanceFacetCollector
block|{
DECL|field|script
specifier|private
specifier|final
name|SearchScript
name|script
decl_stmt|;
DECL|method|ScriptGeoDistanceFacetCollector
specifier|public
name|ScriptGeoDistanceFacetCollector
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
name|scriptLang
parameter_list|,
name|String
name|script
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|params
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
name|script
operator|=
operator|new
name|SearchScript
argument_list|(
name|context
operator|.
name|scriptSearchLookup
argument_list|()
argument_list|,
name|scriptLang
argument_list|,
name|script
argument_list|,
name|params
argument_list|,
name|context
operator|.
name|scriptService
argument_list|()
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
name|super
operator|.
name|doSetNextReader
argument_list|(
name|reader
argument_list|,
name|docBase
argument_list|)
expr_stmt|;
name|script
operator|.
name|setNextReader
argument_list|(
name|reader
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
name|double
name|value
init|=
operator|(
operator|(
name|Number
operator|)
name|script
operator|.
name|execute
argument_list|(
name|doc
argument_list|)
operator|)
operator|.
name|doubleValue
argument_list|()
decl_stmt|;
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
for|for
control|(
name|GeoPoint
name|point
range|:
name|points
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
name|entry
operator|.
name|total
operator|+=
name|value
expr_stmt|;
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
name|entry
operator|.
name|total
operator|+=
name|value
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
end_class

end_unit

