begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.functionscore
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|functionscore
package|;
end_package

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
name|common
operator|.
name|settings
operator|.
name|ImmutableSettings
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
name|SearchHits
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
name|org
operator|.
name|hamcrest
operator|.
name|CoreMatchers
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Ignore
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|xcontent
operator|.
name|XContentFactory
operator|.
name|jsonBuilder
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|QueryBuilders
operator|.
name|functionScoreQuery
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|QueryBuilders
operator|.
name|matchAllQuery
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|functionscore
operator|.
name|ScoreFunctionBuilders
operator|.
name|randomFunction
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
name|hamcrest
operator|.
name|Matchers
operator|.
name|equalTo
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|nullValue
import|;
end_import

begin_class
DECL|class|RandomScoreFunctionTests
specifier|public
class|class
name|RandomScoreFunctionTests
extends|extends
name|ElasticsearchIntegrationTest
block|{
annotation|@
name|Test
DECL|method|consistentHitsWithSameSeed
specifier|public
name|void
name|consistentHitsWithSameSeed
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|int
name|replicas
init|=
name|between
argument_list|(
literal|0
argument_list|,
literal|2
argument_list|)
decl_stmt|;
comment|// needed for green status!
name|cluster
argument_list|()
operator|.
name|ensureAtLeastNumNodes
argument_list|(
name|replicas
operator|+
literal|1
argument_list|)
expr_stmt|;
name|assertAcked
argument_list|(
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareCreate
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setSettings
argument_list|(
name|ImmutableSettings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"index.number_of_shards"
argument_list|,
name|between
argument_list|(
literal|2
argument_list|,
literal|5
argument_list|)
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.number_of_replicas"
argument_list|,
name|replicas
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
comment|// make sure we are done otherwise preference could change?
name|int
name|docCount
init|=
name|atLeast
argument_list|(
literal|100
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
name|docCount
condition|;
name|i
operator|++
control|)
block|{
name|index
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
literal|""
operator|+
name|i
argument_list|,
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|endObject
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|flush
argument_list|()
expr_stmt|;
name|refresh
argument_list|()
expr_stmt|;
name|int
name|outerIters
init|=
name|atLeast
argument_list|(
literal|10
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|o
init|=
literal|0
init|;
name|o
operator|<
name|outerIters
condition|;
name|o
operator|++
control|)
block|{
specifier|final
name|long
name|seed
init|=
name|randomLong
argument_list|()
decl_stmt|;
specifier|final
name|String
name|preference
init|=
name|randomRealisticUnicodeOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
decl_stmt|;
comment|// at least one char!!
name|int
name|innerIters
init|=
name|atLeast
argument_list|(
literal|2
argument_list|)
decl_stmt|;
name|SearchHits
name|hits
init|=
literal|null
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
name|innerIters
condition|;
name|i
operator|++
control|)
block|{
name|SearchResponse
name|searchResponse
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|()
operator|.
name|setPreference
argument_list|(
name|preference
argument_list|)
operator|.
name|setQuery
argument_list|(
name|functionScoreQuery
argument_list|(
name|matchAllQuery
argument_list|()
argument_list|,
name|randomFunction
argument_list|(
name|seed
argument_list|)
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
literal|"Failures "
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|searchResponse
operator|.
name|getShardFailures
argument_list|()
argument_list|)
argument_list|,
name|searchResponse
operator|.
name|getShardFailures
argument_list|()
operator|.
name|length
argument_list|,
name|CoreMatchers
operator|.
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|int
name|hitCount
init|=
name|searchResponse
operator|.
name|getHits
argument_list|()
operator|.
name|getHits
argument_list|()
operator|.
name|length
decl_stmt|;
if|if
condition|(
name|i
operator|==
literal|0
condition|)
block|{
name|assertThat
argument_list|(
name|hits
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|hits
operator|=
name|searchResponse
operator|.
name|getHits
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|assertThat
argument_list|(
name|hits
operator|.
name|getHits
argument_list|()
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
name|searchResponse
operator|.
name|getHits
argument_list|()
operator|.
name|getHits
argument_list|()
operator|.
name|length
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|hitCount
condition|;
name|j
operator|++
control|)
block|{
name|assertThat
argument_list|(
name|searchResponse
operator|.
name|getHits
argument_list|()
operator|.
name|getAt
argument_list|(
name|j
argument_list|)
operator|.
name|score
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|hits
operator|.
name|getAt
argument_list|(
name|j
argument_list|)
operator|.
name|score
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|searchResponse
operator|.
name|getHits
argument_list|()
operator|.
name|getAt
argument_list|(
name|j
argument_list|)
operator|.
name|id
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|hits
operator|.
name|getAt
argument_list|(
name|j
argument_list|)
operator|.
name|id
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
annotation|@
name|Test
annotation|@
name|Ignore
DECL|method|distribution
specifier|public
name|void
name|distribution
parameter_list|()
throws|throws
name|Exception
block|{
name|int
name|count
init|=
literal|10000
decl_stmt|;
name|prepareCreate
argument_list|(
literal|"test"
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|count
condition|;
name|i
operator|++
control|)
block|{
name|index
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
literal|""
operator|+
name|i
argument_list|,
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|endObject
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|flush
argument_list|()
expr_stmt|;
name|refresh
argument_list|()
expr_stmt|;
name|int
index|[]
name|matrix
init|=
operator|new
name|int
index|[
name|count
index|]
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
name|count
condition|;
name|i
operator|++
control|)
block|{
name|SearchResponse
name|searchResponse
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|()
operator|.
name|setQuery
argument_list|(
name|functionScoreQuery
argument_list|(
name|matchAllQuery
argument_list|()
argument_list|,
name|randomFunction
argument_list|(
name|System
operator|.
name|nanoTime
argument_list|()
argument_list|)
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|matrix
index|[
name|Integer
operator|.
name|valueOf
argument_list|(
name|searchResponse
operator|.
name|getHits
argument_list|()
operator|.
name|getAt
argument_list|(
literal|0
argument_list|)
operator|.
name|id
argument_list|()
argument_list|)
index|]
operator|++
expr_stmt|;
block|}
name|int
name|filled
init|=
literal|0
decl_stmt|;
name|int
name|maxRepeat
init|=
literal|0
decl_stmt|;
name|int
name|sumRepeat
init|=
literal|0
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
name|matrix
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|int
name|value
init|=
name|matrix
index|[
name|i
index|]
decl_stmt|;
name|sumRepeat
operator|+=
name|value
expr_stmt|;
name|maxRepeat
operator|=
name|Math
operator|.
name|max
argument_list|(
name|maxRepeat
argument_list|,
name|value
argument_list|)
expr_stmt|;
if|if
condition|(
name|value
operator|>
literal|0
condition|)
block|{
name|filled
operator|++
expr_stmt|;
block|}
block|}
name|System
operator|.
name|out
operator|.
name|println
argument_list|()
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"max repeat: "
operator|+
name|maxRepeat
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"avg repeat: "
operator|+
name|sumRepeat
operator|/
operator|(
name|double
operator|)
name|filled
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"distribution: "
operator|+
name|filled
operator|/
operator|(
name|double
operator|)
name|count
argument_list|)
expr_stmt|;
name|int
name|percentile50
init|=
name|filled
operator|/
literal|2
decl_stmt|;
name|int
name|percentile25
init|=
operator|(
name|filled
operator|/
literal|4
operator|)
decl_stmt|;
name|int
name|percentile75
init|=
name|percentile50
operator|+
name|percentile25
decl_stmt|;
name|int
name|sum
init|=
literal|0
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
name|matrix
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|matrix
index|[
name|i
index|]
operator|==
literal|0
condition|)
block|{
continue|continue;
block|}
name|sum
operator|+=
name|i
operator|*
name|matrix
index|[
name|i
index|]
expr_stmt|;
if|if
condition|(
name|percentile50
operator|==
literal|0
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"median: "
operator|+
name|i
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|percentile25
operator|==
literal|0
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"percentile_25: "
operator|+
name|i
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|percentile75
operator|==
literal|0
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"percentile_75: "
operator|+
name|i
argument_list|)
expr_stmt|;
block|}
name|percentile50
operator|--
expr_stmt|;
name|percentile25
operator|--
expr_stmt|;
name|percentile75
operator|--
expr_stmt|;
block|}
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"mean: "
operator|+
name|sum
operator|/
operator|(
name|double
operator|)
name|count
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

