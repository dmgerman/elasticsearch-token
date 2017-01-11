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
name|common
operator|.
name|ParseFieldMatcher
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
name|xcontent
operator|.
name|XContentParser
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
name|xcontent
operator|.
name|json
operator|.
name|JsonXContent
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
name|IndexService
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
name|QueryParseContext
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
name|test
operator|.
name|ESSingleNodeTestCase
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

begin_class
DECL|class|AggregationCollectorTests
specifier|public
class|class
name|AggregationCollectorTests
extends|extends
name|ESSingleNodeTestCase
block|{
DECL|method|testNeedsScores
specifier|public
name|void
name|testNeedsScores
parameter_list|()
throws|throws
name|Exception
block|{
name|IndexService
name|index
init|=
name|createIndex
argument_list|(
literal|"idx"
argument_list|)
decl_stmt|;
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"idx"
argument_list|,
literal|"type"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"f"
argument_list|,
literal|5
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|get
argument_list|()
expr_stmt|;
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareRefresh
argument_list|(
literal|"idx"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
comment|// simple field aggregation, no scores needed
name|String
name|fieldAgg
init|=
literal|"{ \"my_terms\": {\"terms\": {\"field\": \"f\"}}}"
decl_stmt|;
name|assertFalse
argument_list|(
name|needsScores
argument_list|(
name|index
argument_list|,
name|fieldAgg
argument_list|)
argument_list|)
expr_stmt|;
comment|// agg on a script => scores are needed
comment|// TODO: can we use a mock script service here?
comment|// String scriptAgg = "{ \"my_terms\": {\"terms\": {\"script\": \"doc['f'].value\"}}}";
comment|// assertTrue(needsScores(index, scriptAgg));
comment|//
comment|// String subScriptAgg = "{ \"my_outer_terms\": { \"terms\": { \"field\": \"f\" }, \"aggs\": " + scriptAgg + "}}";
comment|// assertTrue(needsScores(index, subScriptAgg));
comment|// make sure the information is propagated to sub aggregations
name|String
name|subFieldAgg
init|=
literal|"{ \"my_outer_terms\": { \"terms\": { \"field\": \"f\" }, \"aggs\": "
operator|+
name|fieldAgg
operator|+
literal|"}}"
decl_stmt|;
name|assertFalse
argument_list|(
name|needsScores
argument_list|(
name|index
argument_list|,
name|subFieldAgg
argument_list|)
argument_list|)
expr_stmt|;
comment|// top_hits is a particular example of an aggregation that needs scores
name|String
name|topHitsAgg
init|=
literal|"{ \"my_hits\": {\"top_hits\": {}}}"
decl_stmt|;
name|assertTrue
argument_list|(
name|needsScores
argument_list|(
name|index
argument_list|,
name|topHitsAgg
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|needsScores
specifier|private
name|boolean
name|needsScores
parameter_list|(
name|IndexService
name|index
parameter_list|,
name|String
name|agg
parameter_list|)
throws|throws
name|IOException
block|{
name|XContentParser
name|aggParser
init|=
name|createParser
argument_list|(
name|JsonXContent
operator|.
name|jsonXContent
argument_list|,
name|agg
argument_list|)
decl_stmt|;
name|QueryParseContext
name|parseContext
init|=
operator|new
name|QueryParseContext
argument_list|(
name|aggParser
argument_list|,
name|ParseFieldMatcher
operator|.
name|STRICT
argument_list|)
decl_stmt|;
name|aggParser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
name|SearchContext
name|context
init|=
name|createSearchContext
argument_list|(
name|index
argument_list|)
decl_stmt|;
specifier|final
name|AggregatorFactories
name|factories
init|=
name|AggregatorFactories
operator|.
name|parseAggregators
argument_list|(
name|parseContext
argument_list|)
operator|.
name|build
argument_list|(
name|context
argument_list|,
literal|null
argument_list|)
decl_stmt|;
specifier|final
name|Aggregator
index|[]
name|aggregators
init|=
name|factories
operator|.
name|createTopLevelAggregators
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|aggregators
operator|.
name|length
argument_list|)
expr_stmt|;
return|return
name|aggregators
index|[
literal|0
index|]
operator|.
name|needsScores
argument_list|()
return|;
block|}
block|}
end_class

end_unit

