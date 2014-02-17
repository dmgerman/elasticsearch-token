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
name|ImmutableMap
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
name|apache
operator|.
name|lucene
operator|.
name|search
operator|.
name|Filter
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
name|Query
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
name|ElasticsearchException
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
name|Inject
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
name|lease
operator|.
name|Releasables
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
name|search
operator|.
name|Queries
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
name|search
operator|.
name|XCollector
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
name|search
operator|.
name|XConstantScoreQuery
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
name|search
operator|.
name|XFilteredQuery
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
name|SearchParseElement
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
name|SearchPhase
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
name|GlobalAggregator
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
name|support
operator|.
name|AggregationContext
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
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|query
operator|.
name|QueryPhaseExecutionException
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
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
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
DECL|class|AggregationPhase
specifier|public
class|class
name|AggregationPhase
implements|implements
name|SearchPhase
block|{
DECL|field|parseElement
specifier|private
specifier|final
name|AggregationParseElement
name|parseElement
decl_stmt|;
DECL|field|binaryParseElement
specifier|private
specifier|final
name|AggregationBinaryParseElement
name|binaryParseElement
decl_stmt|;
annotation|@
name|Inject
DECL|method|AggregationPhase
specifier|public
name|AggregationPhase
parameter_list|(
name|AggregationParseElement
name|parseElement
parameter_list|,
name|AggregationBinaryParseElement
name|binaryParseElement
parameter_list|)
block|{
name|this
operator|.
name|parseElement
operator|=
name|parseElement
expr_stmt|;
name|this
operator|.
name|binaryParseElement
operator|=
name|binaryParseElement
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|parseElements
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|?
extends|extends
name|SearchParseElement
argument_list|>
name|parseElements
parameter_list|()
block|{
return|return
name|ImmutableMap
operator|.
expr|<
name|String
operator|,
name|SearchParseElement
operator|>
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"aggregations"
argument_list|,
name|parseElement
argument_list|)
operator|.
name|put
argument_list|(
literal|"aggs"
argument_list|,
name|parseElement
argument_list|)
operator|.
name|put
argument_list|(
literal|"aggregations_binary"
argument_list|,
name|binaryParseElement
argument_list|)
operator|.
name|put
argument_list|(
literal|"aggregationsBinary"
argument_list|,
name|binaryParseElement
argument_list|)
operator|.
name|put
argument_list|(
literal|"aggs_binary"
argument_list|,
name|binaryParseElement
argument_list|)
operator|.
name|put
argument_list|(
literal|"aggsBinary"
argument_list|,
name|binaryParseElement
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|preProcess
specifier|public
name|void
name|preProcess
parameter_list|(
name|SearchContext
name|context
parameter_list|)
block|{
if|if
condition|(
name|context
operator|.
name|aggregations
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|AggregationContext
name|aggregationContext
init|=
operator|new
name|AggregationContext
argument_list|(
name|context
argument_list|)
decl_stmt|;
name|context
operator|.
name|aggregations
argument_list|()
operator|.
name|aggregationContext
argument_list|(
name|aggregationContext
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Aggregator
argument_list|>
name|collectors
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|Aggregator
index|[]
name|aggregators
init|=
name|context
operator|.
name|aggregations
argument_list|()
operator|.
name|factories
argument_list|()
operator|.
name|createTopLevelAggregators
argument_list|(
name|aggregationContext
argument_list|)
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
name|aggregators
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
operator|!
operator|(
name|aggregators
index|[
name|i
index|]
operator|instanceof
name|GlobalAggregator
operator|)
condition|)
block|{
name|Aggregator
name|aggregator
init|=
name|aggregators
index|[
name|i
index|]
decl_stmt|;
if|if
condition|(
name|aggregator
operator|.
name|shouldCollect
argument_list|()
condition|)
block|{
name|collectors
operator|.
name|add
argument_list|(
name|aggregator
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|context
operator|.
name|aggregations
argument_list|()
operator|.
name|aggregators
argument_list|(
name|aggregators
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|collectors
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|context
operator|.
name|searcher
argument_list|()
operator|.
name|addMainQueryCollector
argument_list|(
operator|new
name|AggregationsCollector
argument_list|(
name|collectors
argument_list|,
name|aggregationContext
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|aggregationContext
operator|.
name|setNextReader
argument_list|(
name|context
operator|.
name|searcher
argument_list|()
operator|.
name|getIndexReader
argument_list|()
operator|.
name|getContext
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|execute
specifier|public
name|void
name|execute
parameter_list|(
name|SearchContext
name|context
parameter_list|)
throws|throws
name|ElasticsearchException
block|{
if|if
condition|(
name|context
operator|.
name|aggregations
argument_list|()
operator|==
literal|null
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|context
operator|.
name|queryResult
argument_list|()
operator|.
name|aggregations
argument_list|()
operator|!=
literal|null
condition|)
block|{
comment|// no need to compute the facets twice, they should be computed on a per context basis
return|return;
block|}
name|Aggregator
index|[]
name|aggregators
init|=
name|context
operator|.
name|aggregations
argument_list|()
operator|.
name|aggregators
argument_list|()
decl_stmt|;
name|boolean
name|success
init|=
literal|false
decl_stmt|;
try|try
block|{
name|List
argument_list|<
name|Aggregator
argument_list|>
name|globals
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
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
name|aggregators
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|aggregators
index|[
name|i
index|]
operator|instanceof
name|GlobalAggregator
condition|)
block|{
name|globals
operator|.
name|add
argument_list|(
name|aggregators
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
block|}
comment|// optimize the global collector based execution
if|if
condition|(
operator|!
name|globals
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|AggregationsCollector
name|collector
init|=
operator|new
name|AggregationsCollector
argument_list|(
name|globals
argument_list|,
name|context
operator|.
name|aggregations
argument_list|()
operator|.
name|aggregationContext
argument_list|()
argument_list|)
decl_stmt|;
name|Query
name|query
init|=
operator|new
name|XConstantScoreQuery
argument_list|(
name|Queries
operator|.
name|MATCH_ALL_FILTER
argument_list|)
decl_stmt|;
name|Filter
name|searchFilter
init|=
name|context
operator|.
name|searchFilter
argument_list|(
name|context
operator|.
name|types
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|searchFilter
operator|!=
literal|null
condition|)
block|{
name|query
operator|=
operator|new
name|XFilteredQuery
argument_list|(
name|query
argument_list|,
name|searchFilter
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|context
operator|.
name|searcher
argument_list|()
operator|.
name|search
argument_list|(
name|query
argument_list|,
name|collector
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|QueryPhaseExecutionException
argument_list|(
name|context
argument_list|,
literal|"Failed to execute global aggregators"
argument_list|,
name|e
argument_list|)
throw|;
block|}
name|collector
operator|.
name|postCollection
argument_list|()
expr_stmt|;
block|}
name|List
argument_list|<
name|InternalAggregation
argument_list|>
name|aggregations
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|aggregators
operator|.
name|length
argument_list|)
decl_stmt|;
for|for
control|(
name|Aggregator
name|aggregator
range|:
name|context
operator|.
name|aggregations
argument_list|()
operator|.
name|aggregators
argument_list|()
control|)
block|{
name|aggregations
operator|.
name|add
argument_list|(
name|aggregator
operator|.
name|buildAggregation
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|context
operator|.
name|queryResult
argument_list|()
operator|.
name|aggregations
argument_list|(
operator|new
name|InternalAggregations
argument_list|(
name|aggregations
argument_list|)
argument_list|)
expr_stmt|;
name|success
operator|=
literal|true
expr_stmt|;
block|}
finally|finally
block|{
name|Releasables
operator|.
name|release
argument_list|(
name|success
argument_list|,
name|aggregators
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|AggregationsCollector
specifier|public
specifier|static
class|class
name|AggregationsCollector
extends|extends
name|XCollector
block|{
DECL|field|aggregationContext
specifier|private
specifier|final
name|AggregationContext
name|aggregationContext
decl_stmt|;
DECL|field|collectors
specifier|private
specifier|final
name|Aggregator
index|[]
name|collectors
decl_stmt|;
DECL|method|AggregationsCollector
specifier|public
name|AggregationsCollector
parameter_list|(
name|Collection
argument_list|<
name|Aggregator
argument_list|>
name|collectors
parameter_list|,
name|AggregationContext
name|aggregationContext
parameter_list|)
block|{
name|this
operator|.
name|collectors
operator|=
name|collectors
operator|.
name|toArray
argument_list|(
operator|new
name|Aggregator
index|[
name|collectors
operator|.
name|size
argument_list|()
index|]
argument_list|)
expr_stmt|;
name|this
operator|.
name|aggregationContext
operator|=
name|aggregationContext
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
name|aggregationContext
operator|.
name|setScorer
argument_list|(
name|scorer
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|collect
specifier|public
name|void
name|collect
parameter_list|(
name|int
name|doc
parameter_list|)
throws|throws
name|IOException
block|{
for|for
control|(
name|Aggregator
name|collector
range|:
name|collectors
control|)
block|{
name|collector
operator|.
name|collect
argument_list|(
name|doc
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|setNextReader
specifier|public
name|void
name|setNextReader
parameter_list|(
name|AtomicReaderContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|aggregationContext
operator|.
name|setNextReader
argument_list|(
name|context
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|acceptsDocsOutOfOrder
specifier|public
name|boolean
name|acceptsDocsOutOfOrder
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
DECL|method|postCollection
specifier|public
name|void
name|postCollection
parameter_list|()
block|{
for|for
control|(
name|Aggregator
name|collector
range|:
name|collectors
control|)
block|{
name|collector
operator|.
name|postCollection
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

