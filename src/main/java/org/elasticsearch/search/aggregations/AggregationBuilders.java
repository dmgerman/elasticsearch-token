begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
package|;
end_package

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
name|bucket
operator|.
name|filter
operator|.
name|FilterAggregationBuilder
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
name|bucket
operator|.
name|geogrid
operator|.
name|GeoHashGridBuilder
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
name|bucket
operator|.
name|global
operator|.
name|GlobalBuilder
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
name|bucket
operator|.
name|histogram
operator|.
name|DateHistogramBuilder
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
name|bucket
operator|.
name|histogram
operator|.
name|HistogramBuilder
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
name|bucket
operator|.
name|missing
operator|.
name|MissingBuilder
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
name|bucket
operator|.
name|nested
operator|.
name|NestedBuilder
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
name|bucket
operator|.
name|range
operator|.
name|RangeBuilder
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
name|bucket
operator|.
name|range
operator|.
name|date
operator|.
name|DateRangeBuilder
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
name|bucket
operator|.
name|range
operator|.
name|geodistance
operator|.
name|GeoDistanceBuilder
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
name|bucket
operator|.
name|range
operator|.
name|ipv4
operator|.
name|IPv4RangeBuilder
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
name|bucket
operator|.
name|nested
operator|.
name|ReverseNestedBuilder
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
name|bucket
operator|.
name|significant
operator|.
name|SignificantTermsBuilder
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
name|bucket
operator|.
name|terms
operator|.
name|TermsBuilder
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
name|metrics
operator|.
name|avg
operator|.
name|AvgBuilder
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
name|metrics
operator|.
name|cardinality
operator|.
name|CardinalityBuilder
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
name|metrics
operator|.
name|max
operator|.
name|MaxBuilder
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
name|metrics
operator|.
name|min
operator|.
name|MinBuilder
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
name|metrics
operator|.
name|percentiles
operator|.
name|PercentilesBuilder
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
name|metrics
operator|.
name|stats
operator|.
name|StatsBuilder
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
name|metrics
operator|.
name|stats
operator|.
name|extended
operator|.
name|ExtendedStatsBuilder
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
name|metrics
operator|.
name|sum
operator|.
name|SumBuilder
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
name|metrics
operator|.
name|valuecount
operator|.
name|ValueCountBuilder
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|AggregationBuilders
specifier|public
class|class
name|AggregationBuilders
block|{
DECL|method|AggregationBuilders
specifier|protected
name|AggregationBuilders
parameter_list|()
block|{     }
DECL|method|count
specifier|public
specifier|static
name|ValueCountBuilder
name|count
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
operator|new
name|ValueCountBuilder
argument_list|(
name|name
argument_list|)
return|;
block|}
DECL|method|avg
specifier|public
specifier|static
name|AvgBuilder
name|avg
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
operator|new
name|AvgBuilder
argument_list|(
name|name
argument_list|)
return|;
block|}
DECL|method|max
specifier|public
specifier|static
name|MaxBuilder
name|max
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
operator|new
name|MaxBuilder
argument_list|(
name|name
argument_list|)
return|;
block|}
DECL|method|min
specifier|public
specifier|static
name|MinBuilder
name|min
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
operator|new
name|MinBuilder
argument_list|(
name|name
argument_list|)
return|;
block|}
DECL|method|sum
specifier|public
specifier|static
name|SumBuilder
name|sum
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
operator|new
name|SumBuilder
argument_list|(
name|name
argument_list|)
return|;
block|}
DECL|method|stats
specifier|public
specifier|static
name|StatsBuilder
name|stats
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
operator|new
name|StatsBuilder
argument_list|(
name|name
argument_list|)
return|;
block|}
DECL|method|extendedStats
specifier|public
specifier|static
name|ExtendedStatsBuilder
name|extendedStats
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
operator|new
name|ExtendedStatsBuilder
argument_list|(
name|name
argument_list|)
return|;
block|}
DECL|method|filter
specifier|public
specifier|static
name|FilterAggregationBuilder
name|filter
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
operator|new
name|FilterAggregationBuilder
argument_list|(
name|name
argument_list|)
return|;
block|}
DECL|method|global
specifier|public
specifier|static
name|GlobalBuilder
name|global
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
operator|new
name|GlobalBuilder
argument_list|(
name|name
argument_list|)
return|;
block|}
DECL|method|missing
specifier|public
specifier|static
name|MissingBuilder
name|missing
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
operator|new
name|MissingBuilder
argument_list|(
name|name
argument_list|)
return|;
block|}
DECL|method|nested
specifier|public
specifier|static
name|NestedBuilder
name|nested
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
operator|new
name|NestedBuilder
argument_list|(
name|name
argument_list|)
return|;
block|}
DECL|method|reverseNested
specifier|public
specifier|static
name|ReverseNestedBuilder
name|reverseNested
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
operator|new
name|ReverseNestedBuilder
argument_list|(
name|name
argument_list|)
return|;
block|}
DECL|method|geoDistance
specifier|public
specifier|static
name|GeoDistanceBuilder
name|geoDistance
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
operator|new
name|GeoDistanceBuilder
argument_list|(
name|name
argument_list|)
return|;
block|}
DECL|method|histogram
specifier|public
specifier|static
name|HistogramBuilder
name|histogram
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
operator|new
name|HistogramBuilder
argument_list|(
name|name
argument_list|)
return|;
block|}
DECL|method|geohashGrid
specifier|public
specifier|static
name|GeoHashGridBuilder
name|geohashGrid
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
operator|new
name|GeoHashGridBuilder
argument_list|(
name|name
argument_list|)
return|;
block|}
DECL|method|significantTerms
specifier|public
specifier|static
name|SignificantTermsBuilder
name|significantTerms
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
operator|new
name|SignificantTermsBuilder
argument_list|(
name|name
argument_list|)
return|;
block|}
DECL|method|dateHistogram
specifier|public
specifier|static
name|DateHistogramBuilder
name|dateHistogram
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
operator|new
name|DateHistogramBuilder
argument_list|(
name|name
argument_list|)
return|;
block|}
DECL|method|range
specifier|public
specifier|static
name|RangeBuilder
name|range
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
operator|new
name|RangeBuilder
argument_list|(
name|name
argument_list|)
return|;
block|}
DECL|method|dateRange
specifier|public
specifier|static
name|DateRangeBuilder
name|dateRange
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
operator|new
name|DateRangeBuilder
argument_list|(
name|name
argument_list|)
return|;
block|}
DECL|method|ipRange
specifier|public
specifier|static
name|IPv4RangeBuilder
name|ipRange
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
operator|new
name|IPv4RangeBuilder
argument_list|(
name|name
argument_list|)
return|;
block|}
DECL|method|terms
specifier|public
specifier|static
name|TermsBuilder
name|terms
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
operator|new
name|TermsBuilder
argument_list|(
name|name
argument_list|)
return|;
block|}
DECL|method|percentiles
specifier|public
specifier|static
name|PercentilesBuilder
name|percentiles
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
operator|new
name|PercentilesBuilder
argument_list|(
name|name
argument_list|)
return|;
block|}
DECL|method|cardinality
specifier|public
specifier|static
name|CardinalityBuilder
name|cardinality
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
operator|new
name|CardinalityBuilder
argument_list|(
name|name
argument_list|)
return|;
block|}
block|}
end_class

end_unit

