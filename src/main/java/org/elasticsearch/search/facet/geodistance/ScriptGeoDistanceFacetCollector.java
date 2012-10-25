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
comment|/**  *  */
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
DECL|field|scriptAggregator
specifier|private
name|Aggregator
name|scriptAggregator
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
name|script
argument_list|,
name|params
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
name|this
operator|.
name|scriptAggregator
operator|=
operator|(
name|Aggregator
operator|)
name|this
operator|.
name|aggregator
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|setScorer
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
name|script
operator|.
name|setScorer
argument_list|(
name|scorer
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
name|script
operator|.
name|setNextReader
argument_list|(
name|context
operator|.
name|reader
argument_list|()
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
name|script
operator|.
name|setNextDocId
argument_list|(
name|doc
argument_list|)
expr_stmt|;
name|this
operator|.
name|scriptAggregator
operator|.
name|scriptValue
operator|=
name|script
operator|.
name|runAsDouble
argument_list|()
expr_stmt|;
name|super
operator|.
name|doCollect
argument_list|(
name|doc
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
DECL|field|scriptValue
name|double
name|scriptValue
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
name|entry
operator|.
name|totalCount
operator|++
expr_stmt|;
name|entry
operator|.
name|total
operator|+=
name|scriptValue
expr_stmt|;
if|if
condition|(
name|scriptValue
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
name|scriptValue
expr_stmt|;
block|}
if|if
condition|(
name|scriptValue
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
name|scriptValue
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
block|}
end_class

end_unit

