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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableList
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
name|inject
operator|.
name|AbstractModule
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
name|inject
operator|.
name|Module
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
name|inject
operator|.
name|SpawnModules
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
name|children
operator|.
name|InternalChildren
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
name|filter
operator|.
name|InternalFilter
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
name|filters
operator|.
name|InternalFilters
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
name|InternalGeoHashGrid
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
name|InternalGlobal
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
name|InternalHistogram
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
name|InternalMissing
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
name|InternalNested
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
name|InternalReverseNested
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
name|InternalRange
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
name|InternalDateRange
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
name|InternalGeoDistance
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
name|InternalIPv4Range
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
name|sampler
operator|.
name|InternalSampler
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
name|sampler
operator|.
name|UnmappedSampler
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
name|SignificantLongTerms
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
name|SignificantStringTerms
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
name|UnmappedSignificantTerms
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
name|heuristics
operator|.
name|TransportSignificantTermsHeuristicModule
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
name|DoubleTerms
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
name|LongTerms
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
name|StringTerms
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
name|UnmappedTerms
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
name|InternalAvg
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
name|InternalCardinality
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
name|geobounds
operator|.
name|InternalGeoBounds
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
name|InternalMax
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
name|InternalMin
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
name|InternalPercentileRanks
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
name|InternalPercentiles
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
name|scripted
operator|.
name|InternalScriptedMetric
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
name|InternalStats
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
name|InternalExtendedStats
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
name|InternalSum
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
name|tophits
operator|.
name|InternalTopHits
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
name|InternalValueCount
import|;
end_import

begin_comment
comment|/**  * A module that registers all the transport streams for the addAggregation  */
end_comment

begin_class
DECL|class|TransportAggregationModule
specifier|public
class|class
name|TransportAggregationModule
extends|extends
name|AbstractModule
implements|implements
name|SpawnModules
block|{
annotation|@
name|Override
DECL|method|configure
specifier|protected
name|void
name|configure
parameter_list|()
block|{
comment|// calcs
name|InternalAvg
operator|.
name|registerStreams
argument_list|()
expr_stmt|;
name|InternalSum
operator|.
name|registerStreams
argument_list|()
expr_stmt|;
name|InternalMin
operator|.
name|registerStreams
argument_list|()
expr_stmt|;
name|InternalMax
operator|.
name|registerStreams
argument_list|()
expr_stmt|;
name|InternalStats
operator|.
name|registerStreams
argument_list|()
expr_stmt|;
name|InternalExtendedStats
operator|.
name|registerStreams
argument_list|()
expr_stmt|;
name|InternalValueCount
operator|.
name|registerStreams
argument_list|()
expr_stmt|;
name|InternalPercentiles
operator|.
name|registerStreams
argument_list|()
expr_stmt|;
name|InternalPercentileRanks
operator|.
name|registerStreams
argument_list|()
expr_stmt|;
name|InternalCardinality
operator|.
name|registerStreams
argument_list|()
expr_stmt|;
name|InternalScriptedMetric
operator|.
name|registerStreams
argument_list|()
expr_stmt|;
comment|// buckets
name|InternalGlobal
operator|.
name|registerStreams
argument_list|()
expr_stmt|;
name|InternalFilter
operator|.
name|registerStreams
argument_list|()
expr_stmt|;
name|InternalFilters
operator|.
name|registerStream
argument_list|()
expr_stmt|;
name|InternalSampler
operator|.
name|registerStreams
argument_list|()
expr_stmt|;
name|UnmappedSampler
operator|.
name|registerStreams
argument_list|()
expr_stmt|;
name|InternalMissing
operator|.
name|registerStreams
argument_list|()
expr_stmt|;
name|StringTerms
operator|.
name|registerStreams
argument_list|()
expr_stmt|;
name|LongTerms
operator|.
name|registerStreams
argument_list|()
expr_stmt|;
name|SignificantStringTerms
operator|.
name|registerStreams
argument_list|()
expr_stmt|;
name|SignificantLongTerms
operator|.
name|registerStreams
argument_list|()
expr_stmt|;
name|UnmappedSignificantTerms
operator|.
name|registerStreams
argument_list|()
expr_stmt|;
name|InternalGeoHashGrid
operator|.
name|registerStreams
argument_list|()
expr_stmt|;
name|DoubleTerms
operator|.
name|registerStreams
argument_list|()
expr_stmt|;
name|UnmappedTerms
operator|.
name|registerStreams
argument_list|()
expr_stmt|;
name|InternalRange
operator|.
name|registerStream
argument_list|()
expr_stmt|;
name|InternalDateRange
operator|.
name|registerStream
argument_list|()
expr_stmt|;
name|InternalIPv4Range
operator|.
name|registerStream
argument_list|()
expr_stmt|;
name|InternalHistogram
operator|.
name|registerStream
argument_list|()
expr_stmt|;
name|InternalGeoDistance
operator|.
name|registerStream
argument_list|()
expr_stmt|;
name|InternalNested
operator|.
name|registerStream
argument_list|()
expr_stmt|;
name|InternalReverseNested
operator|.
name|registerStream
argument_list|()
expr_stmt|;
name|InternalTopHits
operator|.
name|registerStreams
argument_list|()
expr_stmt|;
name|InternalGeoBounds
operator|.
name|registerStream
argument_list|()
expr_stmt|;
name|InternalChildren
operator|.
name|registerStream
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|spawnModules
specifier|public
name|Iterable
argument_list|<
name|?
extends|extends
name|Module
argument_list|>
name|spawnModules
parameter_list|()
block|{
return|return
name|ImmutableList
operator|.
name|of
argument_list|(
operator|new
name|TransportSignificantTermsHeuristicModule
argument_list|()
argument_list|)
return|;
block|}
block|}
end_class

end_unit

