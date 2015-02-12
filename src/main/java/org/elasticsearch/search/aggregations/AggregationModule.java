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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
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
name|common
operator|.
name|inject
operator|.
name|multibindings
operator|.
name|Multibinder
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
name|ChildrenParser
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
name|FilterParser
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
name|FiltersParser
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
name|GeoHashGridParser
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
name|GlobalParser
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
name|DateHistogramParser
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
name|HistogramParser
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
name|MissingParser
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
name|NestedParser
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
name|ReverseNestedParser
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
name|RangeParser
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
name|DateRangeParser
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
name|GeoDistanceParser
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
name|IpRangeParser
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
name|SignificantTermsParser
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
name|SignificantTermsHeuristicModule
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
name|TermsParser
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
name|AvgParser
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
name|CardinalityParser
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
name|GeoBoundsParser
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
name|MaxParser
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
name|MinParser
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
name|PercentileRanksParser
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
name|PercentilesParser
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
name|ScriptedMetricParser
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
name|StatsParser
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
name|ExtendedStatsParser
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
name|SumParser
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
name|TopHitsParser
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
name|ValueCountParser
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
name|reducers
operator|.
name|Reducer
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_comment
comment|/**  * The main module for the get (binding all get components together)  */
end_comment

begin_class
DECL|class|AggregationModule
specifier|public
class|class
name|AggregationModule
extends|extends
name|AbstractModule
implements|implements
name|SpawnModules
block|{
DECL|field|aggParsers
specifier|private
name|List
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|Aggregator
operator|.
name|Parser
argument_list|>
argument_list|>
name|aggParsers
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
DECL|field|reducerParsers
specifier|private
name|List
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|Reducer
operator|.
name|Parser
argument_list|>
argument_list|>
name|reducerParsers
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
DECL|method|AggregationModule
specifier|public
name|AggregationModule
parameter_list|()
block|{
name|aggParsers
operator|.
name|add
argument_list|(
name|AvgParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|aggParsers
operator|.
name|add
argument_list|(
name|SumParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|aggParsers
operator|.
name|add
argument_list|(
name|MinParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|aggParsers
operator|.
name|add
argument_list|(
name|MaxParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|aggParsers
operator|.
name|add
argument_list|(
name|StatsParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|aggParsers
operator|.
name|add
argument_list|(
name|ExtendedStatsParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|aggParsers
operator|.
name|add
argument_list|(
name|ValueCountParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|aggParsers
operator|.
name|add
argument_list|(
name|PercentilesParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|aggParsers
operator|.
name|add
argument_list|(
name|PercentileRanksParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|aggParsers
operator|.
name|add
argument_list|(
name|CardinalityParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|aggParsers
operator|.
name|add
argument_list|(
name|GlobalParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|aggParsers
operator|.
name|add
argument_list|(
name|MissingParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|aggParsers
operator|.
name|add
argument_list|(
name|FilterParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|aggParsers
operator|.
name|add
argument_list|(
name|FiltersParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|aggParsers
operator|.
name|add
argument_list|(
name|TermsParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|aggParsers
operator|.
name|add
argument_list|(
name|SignificantTermsParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|aggParsers
operator|.
name|add
argument_list|(
name|RangeParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|aggParsers
operator|.
name|add
argument_list|(
name|DateRangeParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|aggParsers
operator|.
name|add
argument_list|(
name|IpRangeParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|aggParsers
operator|.
name|add
argument_list|(
name|HistogramParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|aggParsers
operator|.
name|add
argument_list|(
name|DateHistogramParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|aggParsers
operator|.
name|add
argument_list|(
name|GeoDistanceParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|aggParsers
operator|.
name|add
argument_list|(
name|GeoHashGridParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|aggParsers
operator|.
name|add
argument_list|(
name|NestedParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|aggParsers
operator|.
name|add
argument_list|(
name|ReverseNestedParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|aggParsers
operator|.
name|add
argument_list|(
name|TopHitsParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|aggParsers
operator|.
name|add
argument_list|(
name|GeoBoundsParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|aggParsers
operator|.
name|add
argument_list|(
name|ScriptedMetricParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|aggParsers
operator|.
name|add
argument_list|(
name|ChildrenParser
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
comment|/**      * Enabling extending the get module by adding a custom aggregation parser.      *      * @param parser The parser for the custom aggregator.      */
DECL|method|addAggregatorParser
specifier|public
name|void
name|addAggregatorParser
parameter_list|(
name|Class
argument_list|<
name|?
extends|extends
name|Aggregator
operator|.
name|Parser
argument_list|>
name|parser
parameter_list|)
block|{
name|aggParsers
operator|.
name|add
argument_list|(
name|parser
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|configure
specifier|protected
name|void
name|configure
parameter_list|()
block|{
name|Multibinder
argument_list|<
name|Aggregator
operator|.
name|Parser
argument_list|>
name|multibinderAggParser
init|=
name|Multibinder
operator|.
name|newSetBinder
argument_list|(
name|binder
argument_list|()
argument_list|,
name|Aggregator
operator|.
name|Parser
operator|.
name|class
argument_list|)
decl_stmt|;
for|for
control|(
name|Class
argument_list|<
name|?
extends|extends
name|Aggregator
operator|.
name|Parser
argument_list|>
name|parser
range|:
name|aggParsers
control|)
block|{
name|multibinderAggParser
operator|.
name|addBinding
argument_list|()
operator|.
name|to
argument_list|(
name|parser
argument_list|)
expr_stmt|;
block|}
name|Multibinder
argument_list|<
name|Reducer
operator|.
name|Parser
argument_list|>
name|multibinderReducerParser
init|=
name|Multibinder
operator|.
name|newSetBinder
argument_list|(
name|binder
argument_list|()
argument_list|,
name|Reducer
operator|.
name|Parser
operator|.
name|class
argument_list|)
decl_stmt|;
for|for
control|(
name|Class
argument_list|<
name|?
extends|extends
name|Reducer
operator|.
name|Parser
argument_list|>
name|parser
range|:
name|reducerParsers
control|)
block|{
name|multibinderReducerParser
operator|.
name|addBinding
argument_list|()
operator|.
name|to
argument_list|(
name|parser
argument_list|)
expr_stmt|;
block|}
name|bind
argument_list|(
name|AggregatorParsers
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|bind
argument_list|(
name|AggregationParseElement
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|bind
argument_list|(
name|AggregationPhase
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
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
name|SignificantTermsHeuristicModule
argument_list|()
argument_list|)
return|;
block|}
block|}
end_class

end_unit

