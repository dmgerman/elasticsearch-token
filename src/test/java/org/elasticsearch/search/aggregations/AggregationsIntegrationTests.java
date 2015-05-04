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
name|Lists
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|index
operator|.
name|IndexRequestBuilder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|search
operator|.
name|SearchPhaseExecutionException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|search
operator|.
name|SearchResponse
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|search
operator|.
name|SearchType
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
name|TimeValue
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
name|Terms
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
name|ElasticsearchIntegrationTest
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
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|AggregationBuilders
operator|.
name|terms
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|hamcrest
operator|.
name|ElasticsearchAssertions
operator|.
name|assertAcked
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|hamcrest
operator|.
name|ElasticsearchAssertions
operator|.
name|assertSearchResponse
import|;
end_import

begin_class
annotation|@
name|ElasticsearchIntegrationTest
operator|.
name|SuiteScopeTest
DECL|class|AggregationsIntegrationTests
specifier|public
class|class
name|AggregationsIntegrationTests
extends|extends
name|ElasticsearchIntegrationTest
block|{
DECL|field|numDocs
specifier|static
name|int
name|numDocs
decl_stmt|;
annotation|@
name|Override
DECL|method|setupSuiteScopeCluster
specifier|public
name|void
name|setupSuiteScopeCluster
parameter_list|()
throws|throws
name|Exception
block|{
name|assertAcked
argument_list|(
name|prepareCreate
argument_list|(
literal|"index"
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"type"
argument_list|,
literal|"f"
argument_list|,
literal|"type=string"
argument_list|)
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|ensureYellow
argument_list|(
literal|"index"
argument_list|)
expr_stmt|;
name|numDocs
operator|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|20
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|IndexRequestBuilder
argument_list|>
name|docs
init|=
name|Lists
operator|.
name|newArrayList
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
name|numDocs
condition|;
operator|++
name|i
control|)
block|{
name|docs
operator|.
name|add
argument_list|(
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"index"
argument_list|,
literal|"type"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"f"
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|i
operator|/
literal|3
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|indexRandom
argument_list|(
literal|true
argument_list|,
name|docs
argument_list|)
expr_stmt|;
block|}
DECL|method|testScan
specifier|public
name|void
name|testScan
parameter_list|()
block|{
try|try
block|{
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"index"
argument_list|)
operator|.
name|setSearchType
argument_list|(
name|SearchType
operator|.
name|SCAN
argument_list|)
operator|.
name|setScroll
argument_list|(
operator|new
name|TimeValue
argument_list|(
literal|500
argument_list|)
argument_list|)
operator|.
name|addAggregation
argument_list|(
name|terms
argument_list|(
literal|"f"
argument_list|)
operator|.
name|field
argument_list|(
literal|"f"
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SearchPhaseExecutionException
name|e
parameter_list|)
block|{
name|assertTrue
argument_list|(
name|e
operator|.
name|toString
argument_list|()
argument_list|,
name|e
operator|.
name|toString
argument_list|()
operator|.
name|contains
argument_list|(
literal|"aggregations are not supported with search_type=scan"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testScroll
specifier|public
name|void
name|testScroll
parameter_list|()
block|{
specifier|final
name|int
name|size
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|4
argument_list|)
decl_stmt|;
name|SearchResponse
name|response
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"index"
argument_list|)
operator|.
name|setSize
argument_list|(
name|size
argument_list|)
operator|.
name|setScroll
argument_list|(
operator|new
name|TimeValue
argument_list|(
literal|500
argument_list|)
argument_list|)
operator|.
name|addAggregation
argument_list|(
name|terms
argument_list|(
literal|"f"
argument_list|)
operator|.
name|field
argument_list|(
literal|"f"
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertSearchResponse
argument_list|(
name|response
argument_list|)
expr_stmt|;
name|Aggregations
name|aggregations
init|=
name|response
operator|.
name|getAggregations
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
name|aggregations
argument_list|)
expr_stmt|;
name|Terms
name|terms
init|=
name|aggregations
operator|.
name|get
argument_list|(
literal|"f"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|Math
operator|.
name|min
argument_list|(
name|numDocs
argument_list|,
literal|3L
argument_list|)
argument_list|,
name|terms
operator|.
name|getBucketByKey
argument_list|(
literal|"0"
argument_list|)
operator|.
name|getDocCount
argument_list|()
argument_list|)
expr_stmt|;
name|int
name|total
init|=
name|response
operator|.
name|getHits
argument_list|()
operator|.
name|getHits
argument_list|()
operator|.
name|length
decl_stmt|;
while|while
condition|(
name|response
operator|.
name|getHits
argument_list|()
operator|.
name|hits
argument_list|()
operator|.
name|length
operator|>
literal|0
condition|)
block|{
name|response
operator|=
name|client
argument_list|()
operator|.
name|prepareSearchScroll
argument_list|(
name|response
operator|.
name|getScrollId
argument_list|()
argument_list|)
operator|.
name|setScroll
argument_list|(
operator|new
name|TimeValue
argument_list|(
literal|500
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|assertNull
argument_list|(
name|response
operator|.
name|getAggregations
argument_list|()
argument_list|)
expr_stmt|;
name|total
operator|+=
name|response
operator|.
name|getHits
argument_list|()
operator|.
name|hits
argument_list|()
operator|.
name|length
expr_stmt|;
block|}
name|clearScroll
argument_list|(
name|response
operator|.
name|getScrollId
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|numDocs
argument_list|,
name|total
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

