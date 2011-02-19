begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.facet
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|facet
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|xcontent
operator|.
name|XContentFilterBuilder
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
name|query
operator|.
name|xcontent
operator|.
name|XContentQueryBuilder
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
name|datehistogram
operator|.
name|DateHistogramFacetBuilder
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
name|filter
operator|.
name|FilterFacetBuilder
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
name|geodistance
operator|.
name|GeoDistanceFacetBuilder
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
name|HistogramFacetBuilder
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
name|HistogramScriptFacetBuilder
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
name|query
operator|.
name|QueryFacetBuilder
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
name|range
operator|.
name|RangeFacetBuilder
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
name|range
operator|.
name|RangeScriptFacetBuilder
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
name|statistical
operator|.
name|StatisticalFacetBuilder
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
name|statistical
operator|.
name|StatisticalScriptFacetBuilder
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
name|terms
operator|.
name|TermsFacetBuilder
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
name|termsstats
operator|.
name|TermsStatsFacetBuilder
import|;
end_import

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|FacetBuilders
specifier|public
class|class
name|FacetBuilders
block|{
DECL|method|queryFacet
specifier|public
specifier|static
name|QueryFacetBuilder
name|queryFacet
parameter_list|(
name|String
name|facetName
parameter_list|)
block|{
return|return
operator|new
name|QueryFacetBuilder
argument_list|(
name|facetName
argument_list|)
return|;
block|}
DECL|method|queryFacet
specifier|public
specifier|static
name|QueryFacetBuilder
name|queryFacet
parameter_list|(
name|String
name|facetName
parameter_list|,
name|XContentQueryBuilder
name|query
parameter_list|)
block|{
return|return
operator|new
name|QueryFacetBuilder
argument_list|(
name|facetName
argument_list|)
operator|.
name|query
argument_list|(
name|query
argument_list|)
return|;
block|}
DECL|method|filterFacet
specifier|public
specifier|static
name|FilterFacetBuilder
name|filterFacet
parameter_list|(
name|String
name|facetName
parameter_list|)
block|{
return|return
operator|new
name|FilterFacetBuilder
argument_list|(
name|facetName
argument_list|)
return|;
block|}
DECL|method|filterFacet
specifier|public
specifier|static
name|FilterFacetBuilder
name|filterFacet
parameter_list|(
name|String
name|facetName
parameter_list|,
name|XContentFilterBuilder
name|filter
parameter_list|)
block|{
return|return
operator|new
name|FilterFacetBuilder
argument_list|(
name|facetName
argument_list|)
operator|.
name|filter
argument_list|(
name|filter
argument_list|)
return|;
block|}
DECL|method|termsFacet
specifier|public
specifier|static
name|TermsFacetBuilder
name|termsFacet
parameter_list|(
name|String
name|facetName
parameter_list|)
block|{
return|return
operator|new
name|TermsFacetBuilder
argument_list|(
name|facetName
argument_list|)
return|;
block|}
DECL|method|termsStats
specifier|public
specifier|static
name|TermsStatsFacetBuilder
name|termsStats
parameter_list|(
name|String
name|facetName
parameter_list|)
block|{
return|return
operator|new
name|TermsStatsFacetBuilder
argument_list|(
name|facetName
argument_list|)
return|;
block|}
DECL|method|statisticalFacet
specifier|public
specifier|static
name|StatisticalFacetBuilder
name|statisticalFacet
parameter_list|(
name|String
name|facetName
parameter_list|)
block|{
return|return
operator|new
name|StatisticalFacetBuilder
argument_list|(
name|facetName
argument_list|)
return|;
block|}
DECL|method|statisticalScriptFacet
specifier|public
specifier|static
name|StatisticalScriptFacetBuilder
name|statisticalScriptFacet
parameter_list|(
name|String
name|facetName
parameter_list|)
block|{
return|return
operator|new
name|StatisticalScriptFacetBuilder
argument_list|(
name|facetName
argument_list|)
return|;
block|}
DECL|method|histogramFacet
specifier|public
specifier|static
name|HistogramFacetBuilder
name|histogramFacet
parameter_list|(
name|String
name|facetName
parameter_list|)
block|{
return|return
operator|new
name|HistogramFacetBuilder
argument_list|(
name|facetName
argument_list|)
return|;
block|}
DECL|method|dateHistogramFacet
specifier|public
specifier|static
name|DateHistogramFacetBuilder
name|dateHistogramFacet
parameter_list|(
name|String
name|facetName
parameter_list|)
block|{
return|return
operator|new
name|DateHistogramFacetBuilder
argument_list|(
name|facetName
argument_list|)
return|;
block|}
DECL|method|histogramScriptFacet
specifier|public
specifier|static
name|HistogramScriptFacetBuilder
name|histogramScriptFacet
parameter_list|(
name|String
name|facetName
parameter_list|)
block|{
return|return
operator|new
name|HistogramScriptFacetBuilder
argument_list|(
name|facetName
argument_list|)
return|;
block|}
DECL|method|rangeFacet
specifier|public
specifier|static
name|RangeFacetBuilder
name|rangeFacet
parameter_list|(
name|String
name|facetName
parameter_list|)
block|{
return|return
operator|new
name|RangeFacetBuilder
argument_list|(
name|facetName
argument_list|)
return|;
block|}
DECL|method|rangeScriptFacet
specifier|public
specifier|static
name|RangeScriptFacetBuilder
name|rangeScriptFacet
parameter_list|(
name|String
name|facetName
parameter_list|)
block|{
return|return
operator|new
name|RangeScriptFacetBuilder
argument_list|(
name|facetName
argument_list|)
return|;
block|}
DECL|method|geoDistanceFacet
specifier|public
specifier|static
name|GeoDistanceFacetBuilder
name|geoDistanceFacet
parameter_list|(
name|String
name|facetName
parameter_list|)
block|{
return|return
operator|new
name|GeoDistanceFacetBuilder
argument_list|(
name|facetName
argument_list|)
return|;
block|}
block|}
end_class

end_unit

