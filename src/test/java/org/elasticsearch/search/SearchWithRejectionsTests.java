begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
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
name|base
operator|.
name|Predicate
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
name|util
operator|.
name|LuceneTestCase
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
name|admin
operator|.
name|indices
operator|.
name|stats
operator|.
name|IndicesStatsResponse
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
name|settings
operator|.
name|Settings
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
name|concurrent
operator|.
name|Future
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|TimeUnit
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
name|settings
operator|.
name|Settings
operator|.
name|settingsBuilder
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
name|greaterThanOrEqualTo
import|;
end_import

begin_class
annotation|@
name|ElasticsearchIntegrationTest
operator|.
name|ClusterScope
argument_list|(
name|scope
operator|=
name|ElasticsearchIntegrationTest
operator|.
name|Scope
operator|.
name|SUITE
argument_list|)
annotation|@
name|LuceneTestCase
operator|.
name|Slow
DECL|class|SearchWithRejectionsTests
specifier|public
class|class
name|SearchWithRejectionsTests
extends|extends
name|ElasticsearchIntegrationTest
block|{
annotation|@
name|Override
DECL|method|nodeSettings
specifier|public
name|Settings
name|nodeSettings
parameter_list|(
name|int
name|nodeOrdinal
parameter_list|)
block|{
return|return
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
name|super
operator|.
name|nodeSettings
argument_list|(
name|nodeOrdinal
argument_list|)
argument_list|)
operator|.
name|put
argument_list|(
literal|"threadpool.search.type"
argument_list|,
literal|"fixed"
argument_list|)
operator|.
name|put
argument_list|(
literal|"threadpool.search.size"
argument_list|,
literal|1
argument_list|)
operator|.
name|put
argument_list|(
literal|"threadpool.search.queue_size"
argument_list|,
literal|1
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
annotation|@
name|Test
DECL|method|testOpenContextsAfterRejections
specifier|public
name|void
name|testOpenContextsAfterRejections
parameter_list|()
throws|throws
name|InterruptedException
block|{
name|createIndex
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
specifier|final
name|int
name|docs
init|=
name|scaledRandomIntBetween
argument_list|(
literal|20
argument_list|,
literal|50
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
name|docs
condition|;
name|i
operator|++
control|)
block|{
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|i
argument_list|)
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"field"
argument_list|,
literal|"value"
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
block|}
name|IndicesStatsResponse
name|indicesStats
init|=
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareStats
argument_list|()
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|indicesStats
operator|.
name|getTotal
argument_list|()
operator|.
name|getSearch
argument_list|()
operator|.
name|getOpenContexts
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0l
argument_list|)
argument_list|)
expr_stmt|;
name|refresh
argument_list|()
expr_stmt|;
name|int
name|numSearches
init|=
literal|10
decl_stmt|;
name|Future
argument_list|<
name|SearchResponse
argument_list|>
index|[]
name|responses
init|=
operator|new
name|Future
index|[
name|numSearches
index|]
decl_stmt|;
name|SearchType
name|searchType
init|=
name|randomFrom
argument_list|(
name|SearchType
operator|.
name|DEFAULT
argument_list|,
name|SearchType
operator|.
name|QUERY_AND_FETCH
argument_list|,
name|SearchType
operator|.
name|QUERY_THEN_FETCH
argument_list|,
name|SearchType
operator|.
name|DFS_QUERY_AND_FETCH
argument_list|,
name|SearchType
operator|.
name|DFS_QUERY_THEN_FETCH
argument_list|)
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"search type is {}"
argument_list|,
name|searchType
argument_list|)
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
name|numSearches
condition|;
name|i
operator|++
control|)
block|{
name|responses
index|[
name|i
index|]
operator|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|()
operator|.
name|setQuery
argument_list|(
name|matchAllQuery
argument_list|()
argument_list|)
operator|.
name|setSearchType
argument_list|(
name|searchType
argument_list|)
operator|.
name|execute
argument_list|()
expr_stmt|;
block|}
name|int
name|failures
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
name|numSearches
condition|;
name|i
operator|++
control|)
block|{
try|try
block|{
name|SearchResponse
name|searchResponse
init|=
name|responses
index|[
name|i
index|]
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|searchResponse
operator|.
name|getFailedShards
argument_list|()
operator|>
literal|0
condition|)
block|{
name|failures
operator|++
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|failures
operator|++
expr_stmt|;
block|}
block|}
name|assertThat
argument_list|(
name|failures
argument_list|,
name|greaterThanOrEqualTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|awaitBusy
argument_list|(
operator|new
name|Predicate
argument_list|<
name|Object
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|apply
parameter_list|(
name|Object
name|input
parameter_list|)
block|{
comment|// we must wait here because the requests to release search contexts might still be in flight
comment|// although the search request has already returned
return|return
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareStats
argument_list|()
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
operator|.
name|getTotal
argument_list|()
operator|.
name|getSearch
argument_list|()
operator|.
name|getOpenContexts
argument_list|()
operator|==
literal|0
return|;
block|}
block|}
argument_list|,
literal|1
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
name|indicesStats
operator|=
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareStats
argument_list|()
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|indicesStats
operator|.
name|getTotal
argument_list|()
operator|.
name|getSearch
argument_list|()
operator|.
name|getOpenContexts
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0l
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

