begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.test.integration.search
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|integration
operator|.
name|search
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|client
operator|.
name|Client
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|node
operator|.
name|internal
operator|.
name|InternalNode
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
name|Scroll
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
name|SearchContextMissingException
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
name|SearchService
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
name|SearchShardTarget
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
name|builder
operator|.
name|SearchSourceBuilder
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
name|controller
operator|.
name|SearchPhaseController
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
name|controller
operator|.
name|ShardDoc
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
name|dfs
operator|.
name|AggregatedDfs
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
name|dfs
operator|.
name|DfsSearchResult
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
name|fetch
operator|.
name|FetchSearchRequest
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
name|fetch
operator|.
name|FetchSearchResult
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
name|fetch
operator|.
name|QueryFetchSearchResult
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
name|InternalSearchRequest
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
name|QuerySearchRequest
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
name|QuerySearchResult
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
name|integration
operator|.
name|AbstractNodesTests
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
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
name|util
operator|.
name|trove
operator|.
name|ExtTIntArrayList
import|;
end_import

begin_import
import|import
name|org
operator|.
name|testng
operator|.
name|annotations
operator|.
name|AfterClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|testng
operator|.
name|annotations
operator|.
name|BeforeClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|testng
operator|.
name|annotations
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
name|Map
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|client
operator|.
name|Requests
operator|.
name|*
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
name|xcontent
operator|.
name|QueryBuilders
operator|.
name|*
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
name|builder
operator|.
name|SearchSourceBuilder
operator|.
name|*
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|collect
operator|.
name|Lists
operator|.
name|*
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|MatcherAssert
operator|.
name|*
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
name|*
import|;
end_import

begin_comment
comment|/**  * @author kimchy (Shay Banon)  */
end_comment

begin_class
DECL|class|SingleInstanceEmbeddedSearchTests
specifier|public
class|class
name|SingleInstanceEmbeddedSearchTests
extends|extends
name|AbstractNodesTests
block|{
DECL|field|searchService
specifier|private
name|SearchService
name|searchService
decl_stmt|;
DECL|field|searchPhaseController
specifier|private
name|SearchPhaseController
name|searchPhaseController
decl_stmt|;
DECL|method|createNodeAndInitWithData
annotation|@
name|BeforeClass
specifier|public
name|void
name|createNodeAndInitWithData
parameter_list|()
throws|throws
name|Exception
block|{
name|startNode
argument_list|(
literal|"server1"
argument_list|)
expr_stmt|;
name|client
argument_list|(
literal|"server1"
argument_list|)
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|create
argument_list|(
name|createIndexRequest
argument_list|(
literal|"test"
argument_list|)
argument_list|)
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|index
argument_list|(
name|client
argument_list|(
literal|"server1"
argument_list|)
argument_list|,
literal|"1"
argument_list|,
literal|"test1"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|index
argument_list|(
name|client
argument_list|(
literal|"server1"
argument_list|)
argument_list|,
literal|"2"
argument_list|,
literal|"test2"
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|index
argument_list|(
name|client
argument_list|(
literal|"server1"
argument_list|)
argument_list|,
literal|"3"
argument_list|,
literal|"test3"
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|index
argument_list|(
name|client
argument_list|(
literal|"server1"
argument_list|)
argument_list|,
literal|"4"
argument_list|,
literal|"test4"
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|index
argument_list|(
name|client
argument_list|(
literal|"server1"
argument_list|)
argument_list|,
literal|"5"
argument_list|,
literal|"test5"
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|client
argument_list|(
literal|"server1"
argument_list|)
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|refresh
argument_list|(
name|refreshRequest
argument_list|(
literal|"test"
argument_list|)
argument_list|)
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|searchService
operator|=
operator|(
operator|(
name|InternalNode
operator|)
name|node
argument_list|(
literal|"server1"
argument_list|)
operator|)
operator|.
name|injector
argument_list|()
operator|.
name|getInstance
argument_list|(
name|SearchService
operator|.
name|class
argument_list|)
expr_stmt|;
name|searchPhaseController
operator|=
operator|(
operator|(
name|InternalNode
operator|)
name|node
argument_list|(
literal|"server1"
argument_list|)
operator|)
operator|.
name|injector
argument_list|()
operator|.
name|getInstance
argument_list|(
name|SearchPhaseController
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
DECL|method|closeNode
annotation|@
name|AfterClass
specifier|public
name|void
name|closeNode
parameter_list|()
block|{
name|closeAllNodes
argument_list|()
expr_stmt|;
block|}
DECL|method|testDirectDfs
annotation|@
name|Test
specifier|public
name|void
name|testDirectDfs
parameter_list|()
throws|throws
name|Exception
block|{
name|DfsSearchResult
name|dfsResult
init|=
name|searchService
operator|.
name|executeDfsPhase
argument_list|(
name|searchRequest
argument_list|(
name|searchSource
argument_list|()
operator|.
name|query
argument_list|(
name|termQuery
argument_list|(
literal|"name"
argument_list|,
literal|"test1"
argument_list|)
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|dfsResult
operator|.
name|terms
argument_list|()
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|dfsResult
operator|.
name|freqs
argument_list|()
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|dfsResult
operator|.
name|terms
argument_list|()
index|[
literal|0
index|]
operator|.
name|field
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"name"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|dfsResult
operator|.
name|terms
argument_list|()
index|[
literal|0
index|]
operator|.
name|text
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"test1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|dfsResult
operator|.
name|freqs
argument_list|()
index|[
literal|0
index|]
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testDirectQuery
annotation|@
name|Test
specifier|public
name|void
name|testDirectQuery
parameter_list|()
throws|throws
name|Exception
block|{
name|QuerySearchResult
name|queryResult
init|=
name|searchService
operator|.
name|executeQueryPhase
argument_list|(
name|searchRequest
argument_list|(
name|searchSource
argument_list|()
operator|.
name|query
argument_list|(
name|termQuery
argument_list|(
literal|"name"
argument_list|,
literal|"test1"
argument_list|)
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|queryResult
operator|.
name|topDocs
argument_list|()
operator|.
name|totalHits
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testDirectFetch
annotation|@
name|Test
specifier|public
name|void
name|testDirectFetch
parameter_list|()
throws|throws
name|Exception
block|{
name|QueryFetchSearchResult
name|queryFetchResult
init|=
name|searchService
operator|.
name|executeFetchPhase
argument_list|(
name|searchRequest
argument_list|(
name|searchSource
argument_list|()
operator|.
name|query
argument_list|(
name|termQuery
argument_list|(
literal|"name"
argument_list|,
literal|"test1"
argument_list|)
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|queryFetchResult
operator|.
name|queryResult
argument_list|()
operator|.
name|topDocs
argument_list|()
operator|.
name|totalHits
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|queryFetchResult
operator|.
name|fetchResult
argument_list|()
operator|.
name|hits
argument_list|()
operator|.
name|hits
argument_list|()
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|queryFetchResult
operator|.
name|fetchResult
argument_list|()
operator|.
name|hits
argument_list|()
operator|.
name|hits
argument_list|()
index|[
literal|0
index|]
operator|.
name|sourceAsString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|source
argument_list|(
literal|"1"
argument_list|,
literal|"test1"
argument_list|,
literal|1
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|queryFetchResult
operator|.
name|fetchResult
argument_list|()
operator|.
name|hits
argument_list|()
operator|.
name|hits
argument_list|()
index|[
literal|0
index|]
operator|.
name|id
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|queryFetchResult
operator|.
name|fetchResult
argument_list|()
operator|.
name|hits
argument_list|()
operator|.
name|hits
argument_list|()
index|[
literal|0
index|]
operator|.
name|type
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"type1"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testQueryThenFetch
annotation|@
name|Test
specifier|public
name|void
name|testQueryThenFetch
parameter_list|()
throws|throws
name|Exception
block|{
name|QuerySearchResult
name|queryResult
init|=
name|searchService
operator|.
name|executeQueryPhase
argument_list|(
name|searchRequest
argument_list|(
name|searchSource
argument_list|()
operator|.
name|query
argument_list|(
name|termQuery
argument_list|(
literal|"name"
argument_list|,
literal|"test1"
argument_list|)
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|queryResult
operator|.
name|topDocs
argument_list|()
operator|.
name|totalHits
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|ShardDoc
index|[]
name|sortedShardList
init|=
name|searchPhaseController
operator|.
name|sortDocs
argument_list|(
name|newArrayList
argument_list|(
name|queryResult
argument_list|)
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|SearchShardTarget
argument_list|,
name|ExtTIntArrayList
argument_list|>
name|docIdsToLoad
init|=
name|searchPhaseController
operator|.
name|docIdsToLoad
argument_list|(
name|sortedShardList
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|docIdsToLoad
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|docIdsToLoad
operator|.
name|values
argument_list|()
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|FetchSearchResult
name|fetchResult
init|=
name|searchService
operator|.
name|executeFetchPhase
argument_list|(
operator|new
name|FetchSearchRequest
argument_list|(
name|queryResult
operator|.
name|id
argument_list|()
argument_list|,
name|docIdsToLoad
operator|.
name|values
argument_list|()
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|fetchResult
operator|.
name|hits
argument_list|()
operator|.
name|hits
argument_list|()
index|[
literal|0
index|]
operator|.
name|sourceAsString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|source
argument_list|(
literal|"1"
argument_list|,
literal|"test1"
argument_list|,
literal|1
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|fetchResult
operator|.
name|hits
argument_list|()
operator|.
name|hits
argument_list|()
index|[
literal|0
index|]
operator|.
name|id
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|fetchResult
operator|.
name|hits
argument_list|()
operator|.
name|hits
argument_list|()
index|[
literal|0
index|]
operator|.
name|type
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"type1"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testQueryAndFetch
annotation|@
name|Test
specifier|public
name|void
name|testQueryAndFetch
parameter_list|()
throws|throws
name|Exception
block|{
name|QueryFetchSearchResult
name|result
init|=
name|searchService
operator|.
name|executeFetchPhase
argument_list|(
name|searchRequest
argument_list|(
name|searchSource
argument_list|()
operator|.
name|query
argument_list|(
name|termQuery
argument_list|(
literal|"name"
argument_list|,
literal|"test1"
argument_list|)
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|FetchSearchResult
name|fetchResult
init|=
name|result
operator|.
name|fetchResult
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|fetchResult
operator|.
name|hits
argument_list|()
operator|.
name|hits
argument_list|()
index|[
literal|0
index|]
operator|.
name|sourceAsString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|source
argument_list|(
literal|"1"
argument_list|,
literal|"test1"
argument_list|,
literal|1
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|fetchResult
operator|.
name|hits
argument_list|()
operator|.
name|hits
argument_list|()
index|[
literal|0
index|]
operator|.
name|id
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|fetchResult
operator|.
name|hits
argument_list|()
operator|.
name|hits
argument_list|()
index|[
literal|0
index|]
operator|.
name|type
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"type1"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testDfsQueryThenFetch
annotation|@
name|Test
specifier|public
name|void
name|testDfsQueryThenFetch
parameter_list|()
throws|throws
name|Exception
block|{
name|DfsSearchResult
name|dfsResult
init|=
name|searchService
operator|.
name|executeDfsPhase
argument_list|(
name|searchRequest
argument_list|(
name|searchSource
argument_list|()
operator|.
name|query
argument_list|(
name|termQuery
argument_list|(
literal|"name"
argument_list|,
literal|"test1"
argument_list|)
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|AggregatedDfs
name|dfs
init|=
name|searchPhaseController
operator|.
name|aggregateDfs
argument_list|(
name|newArrayList
argument_list|(
name|dfsResult
argument_list|)
argument_list|)
decl_stmt|;
name|QuerySearchResult
name|queryResult
init|=
name|searchService
operator|.
name|executeQueryPhase
argument_list|(
operator|new
name|QuerySearchRequest
argument_list|(
name|dfsResult
operator|.
name|id
argument_list|()
argument_list|,
name|dfs
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|queryResult
operator|.
name|topDocs
argument_list|()
operator|.
name|totalHits
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|ShardDoc
index|[]
name|sortedShardList
init|=
name|searchPhaseController
operator|.
name|sortDocs
argument_list|(
name|newArrayList
argument_list|(
name|queryResult
argument_list|)
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|SearchShardTarget
argument_list|,
name|ExtTIntArrayList
argument_list|>
name|docIdsToLoad
init|=
name|searchPhaseController
operator|.
name|docIdsToLoad
argument_list|(
name|sortedShardList
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|docIdsToLoad
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|docIdsToLoad
operator|.
name|values
argument_list|()
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|FetchSearchResult
name|fetchResult
init|=
name|searchService
operator|.
name|executeFetchPhase
argument_list|(
operator|new
name|FetchSearchRequest
argument_list|(
name|queryResult
operator|.
name|id
argument_list|()
argument_list|,
name|docIdsToLoad
operator|.
name|values
argument_list|()
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|fetchResult
operator|.
name|hits
argument_list|()
operator|.
name|hits
argument_list|()
index|[
literal|0
index|]
operator|.
name|sourceAsString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|source
argument_list|(
literal|"1"
argument_list|,
literal|"test1"
argument_list|,
literal|1
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|fetchResult
operator|.
name|hits
argument_list|()
operator|.
name|hits
argument_list|()
index|[
literal|0
index|]
operator|.
name|id
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|fetchResult
operator|.
name|hits
argument_list|()
operator|.
name|hits
argument_list|()
index|[
literal|0
index|]
operator|.
name|type
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"type1"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testSimpleQueryFacetsNoExecutionType
annotation|@
name|Test
specifier|public
name|void
name|testSimpleQueryFacetsNoExecutionType
parameter_list|()
throws|throws
name|Exception
block|{
name|QuerySearchResult
name|queryResult
init|=
name|searchService
operator|.
name|executeQueryPhase
argument_list|(
name|searchRequest
argument_list|(
name|searchSource
argument_list|()
operator|.
name|query
argument_list|(
name|wildcardQuery
argument_list|(
literal|"name"
argument_list|,
literal|"te*"
argument_list|)
argument_list|)
operator|.
name|facets
argument_list|(
name|facets
argument_list|()
operator|.
name|facet
argument_list|(
literal|"age2"
argument_list|,
name|termQuery
argument_list|(
literal|"age"
argument_list|,
literal|2
argument_list|)
argument_list|)
operator|.
name|facet
argument_list|(
literal|"age1"
argument_list|,
name|termQuery
argument_list|(
literal|"age"
argument_list|,
literal|1
argument_list|)
argument_list|)
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|queryResult
operator|.
name|facets
argument_list|()
operator|.
name|countFacet
argument_list|(
literal|"age2"
argument_list|)
operator|.
name|count
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|4l
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|queryResult
operator|.
name|facets
argument_list|()
operator|.
name|countFacet
argument_list|(
literal|"age1"
argument_list|)
operator|.
name|count
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1l
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testSimpleQueryFacetsQueryExecutionCollect
annotation|@
name|Test
specifier|public
name|void
name|testSimpleQueryFacetsQueryExecutionCollect
parameter_list|()
throws|throws
name|Exception
block|{
name|QuerySearchResult
name|queryResult
init|=
name|searchService
operator|.
name|executeQueryPhase
argument_list|(
name|searchRequest
argument_list|(
name|searchSource
argument_list|()
operator|.
name|query
argument_list|(
name|wildcardQuery
argument_list|(
literal|"name"
argument_list|,
literal|"te*"
argument_list|)
argument_list|)
operator|.
name|facets
argument_list|(
name|facets
argument_list|()
operator|.
name|queryExecution
argument_list|(
literal|"collect"
argument_list|)
operator|.
name|facet
argument_list|(
literal|"age2"
argument_list|,
name|termQuery
argument_list|(
literal|"age"
argument_list|,
literal|2
argument_list|)
argument_list|)
operator|.
name|facet
argument_list|(
literal|"age1"
argument_list|,
name|termQuery
argument_list|(
literal|"age"
argument_list|,
literal|1
argument_list|)
argument_list|)
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|queryResult
operator|.
name|facets
argument_list|()
operator|.
name|countFacet
argument_list|(
literal|"age2"
argument_list|)
operator|.
name|count
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|4l
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|queryResult
operator|.
name|facets
argument_list|()
operator|.
name|countFacet
argument_list|(
literal|"age1"
argument_list|)
operator|.
name|count
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1l
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testSimpleQueryFacetsQueryExecutionIdset
annotation|@
name|Test
specifier|public
name|void
name|testSimpleQueryFacetsQueryExecutionIdset
parameter_list|()
throws|throws
name|Exception
block|{
name|QuerySearchResult
name|queryResult
init|=
name|searchService
operator|.
name|executeQueryPhase
argument_list|(
name|searchRequest
argument_list|(
name|searchSource
argument_list|()
operator|.
name|query
argument_list|(
name|wildcardQuery
argument_list|(
literal|"name"
argument_list|,
literal|"te*"
argument_list|)
argument_list|)
operator|.
name|facets
argument_list|(
name|facets
argument_list|()
operator|.
name|queryExecution
argument_list|(
literal|"idset"
argument_list|)
operator|.
name|facet
argument_list|(
literal|"age2"
argument_list|,
name|termQuery
argument_list|(
literal|"age"
argument_list|,
literal|2
argument_list|)
argument_list|)
operator|.
name|facet
argument_list|(
literal|"age1"
argument_list|,
name|termQuery
argument_list|(
literal|"age"
argument_list|,
literal|1
argument_list|)
argument_list|)
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|queryResult
operator|.
name|facets
argument_list|()
operator|.
name|countFacet
argument_list|(
literal|"age2"
argument_list|)
operator|.
name|count
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|4l
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|queryResult
operator|.
name|facets
argument_list|()
operator|.
name|countFacet
argument_list|(
literal|"age1"
argument_list|)
operator|.
name|count
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1l
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testQueryFetchKeepAliveTimeout
annotation|@
name|Test
specifier|public
name|void
name|testQueryFetchKeepAliveTimeout
parameter_list|()
throws|throws
name|Exception
block|{
name|QuerySearchResult
name|queryResult
init|=
name|searchService
operator|.
name|executeQueryPhase
argument_list|(
name|searchRequest
argument_list|(
name|searchSource
argument_list|()
operator|.
name|query
argument_list|(
name|termQuery
argument_list|(
literal|"name"
argument_list|,
literal|"test1"
argument_list|)
argument_list|)
argument_list|)
operator|.
name|scroll
argument_list|(
operator|new
name|Scroll
argument_list|(
name|TimeValue
operator|.
name|timeValueMillis
argument_list|(
literal|10
argument_list|)
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|queryResult
operator|.
name|topDocs
argument_list|()
operator|.
name|totalHits
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|ShardDoc
index|[]
name|sortedShardList
init|=
name|searchPhaseController
operator|.
name|sortDocs
argument_list|(
name|newArrayList
argument_list|(
name|queryResult
argument_list|)
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|SearchShardTarget
argument_list|,
name|ExtTIntArrayList
argument_list|>
name|docIdsToLoad
init|=
name|searchPhaseController
operator|.
name|docIdsToLoad
argument_list|(
name|sortedShardList
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|docIdsToLoad
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|docIdsToLoad
operator|.
name|values
argument_list|()
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
comment|// sleep more than the 100ms the timeout wheel it set to
name|Thread
operator|.
name|sleep
argument_list|(
literal|300
argument_list|)
expr_stmt|;
try|try
block|{
name|searchService
operator|.
name|executeFetchPhase
argument_list|(
operator|new
name|FetchSearchRequest
argument_list|(
name|queryResult
operator|.
name|id
argument_list|()
argument_list|,
name|docIdsToLoad
operator|.
name|values
argument_list|()
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
assert|assert
literal|true
operator|:
literal|"context should be missing since it timed out"
assert|;
block|}
catch|catch
parameter_list|(
name|SearchContextMissingException
name|e
parameter_list|)
block|{
comment|// all is well
block|}
block|}
DECL|method|searchRequest
specifier|private
name|InternalSearchRequest
name|searchRequest
parameter_list|(
name|SearchSourceBuilder
name|builder
parameter_list|)
block|{
return|return
operator|new
name|InternalSearchRequest
argument_list|(
literal|"test"
argument_list|,
literal|0
argument_list|,
name|builder
operator|.
name|buildAsBytes
argument_list|()
argument_list|)
return|;
block|}
DECL|method|index
specifier|private
name|void
name|index
parameter_list|(
name|Client
name|client
parameter_list|,
name|String
name|id
parameter_list|,
name|String
name|nameValue
parameter_list|,
name|int
name|age
parameter_list|)
block|{
name|client
operator|.
name|index
argument_list|(
name|indexRequest
argument_list|(
literal|"test"
argument_list|)
operator|.
name|type
argument_list|(
literal|"type1"
argument_list|)
operator|.
name|id
argument_list|(
name|id
argument_list|)
operator|.
name|source
argument_list|(
name|source
argument_list|(
name|id
argument_list|,
name|nameValue
argument_list|,
name|age
argument_list|)
argument_list|)
argument_list|)
operator|.
name|actionGet
argument_list|()
expr_stmt|;
block|}
DECL|method|source
specifier|private
name|String
name|source
parameter_list|(
name|String
name|id
parameter_list|,
name|String
name|nameValue
parameter_list|,
name|int
name|age
parameter_list|)
block|{
return|return
literal|"{ type1 : { \"id\" : \""
operator|+
name|id
operator|+
literal|"\", \"name\" : \""
operator|+
name|nameValue
operator|+
literal|"\", age : "
operator|+
name|age
operator|+
literal|" } }"
return|;
block|}
block|}
end_class

end_unit

