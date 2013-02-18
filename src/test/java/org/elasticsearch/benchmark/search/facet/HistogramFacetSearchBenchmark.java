begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.benchmark.search.facet
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|benchmark
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
name|action
operator|.
name|admin
operator|.
name|cluster
operator|.
name|health
operator|.
name|ClusterHealthResponse
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
name|bulk
operator|.
name|BulkRequestBuilder
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
name|bulk
operator|.
name|BulkResponse
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
name|client
operator|.
name|Requests
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
name|StopWatch
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
name|common
operator|.
name|unit
operator|.
name|SizeValue
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
name|XContentBuilder
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
name|Node
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
name|FacetBuilder
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Date
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
name|createIndexRequest
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|metadata
operator|.
name|IndexMetaData
operator|.
name|SETTING_NUMBER_OF_REPLICAS
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|metadata
operator|.
name|IndexMetaData
operator|.
name|SETTING_NUMBER_OF_SHARDS
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
name|ImmutableSettings
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
name|matchAllQuery
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|node
operator|.
name|NodeBuilder
operator|.
name|nodeBuilder
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
name|facet
operator|.
name|FacetBuilders
operator|.
name|dateHistogramFacet
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
name|facet
operator|.
name|FacetBuilders
operator|.
name|histogramFacet
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|HistogramFacetSearchBenchmark
specifier|public
class|class
name|HistogramFacetSearchBenchmark
block|{
DECL|method|main
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|Exception
block|{
name|Settings
name|settings
init|=
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"index.engine.robin.refreshInterval"
argument_list|,
literal|"-1"
argument_list|)
operator|.
name|put
argument_list|(
literal|"gateway.type"
argument_list|,
literal|"local"
argument_list|)
operator|.
name|put
argument_list|(
name|SETTING_NUMBER_OF_SHARDS
argument_list|,
literal|2
argument_list|)
operator|.
name|put
argument_list|(
name|SETTING_NUMBER_OF_REPLICAS
argument_list|,
literal|1
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|Node
name|node1
init|=
name|nodeBuilder
argument_list|()
operator|.
name|settings
argument_list|(
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
name|settings
argument_list|)
operator|.
name|put
argument_list|(
literal|"name"
argument_list|,
literal|"node1"
argument_list|)
argument_list|)
operator|.
name|node
argument_list|()
decl_stmt|;
comment|//Node clientNode = nodeBuilder().settings(settingsBuilder().put(settings).put("name", "client")).client(true).node();
name|Client
name|client
init|=
name|node1
operator|.
name|client
argument_list|()
decl_stmt|;
name|long
name|COUNT
init|=
name|SizeValue
operator|.
name|parseSizeValue
argument_list|(
literal|"5m"
argument_list|)
operator|.
name|singles
argument_list|()
decl_stmt|;
name|int
name|BATCH
init|=
literal|500
decl_stmt|;
name|int
name|QUERY_WARMUP
init|=
literal|20
decl_stmt|;
name|int
name|QUERY_COUNT
init|=
literal|200
decl_stmt|;
name|int
name|NUMBER_OF_TERMS
init|=
literal|1000
decl_stmt|;
name|long
index|[]
name|lValues
init|=
operator|new
name|long
index|[
name|NUMBER_OF_TERMS
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
name|NUMBER_OF_TERMS
condition|;
name|i
operator|++
control|)
block|{
name|lValues
index|[
name|i
index|]
operator|=
name|i
expr_stmt|;
block|}
name|Thread
operator|.
name|sleep
argument_list|(
literal|10000
argument_list|)
expr_stmt|;
try|try
block|{
name|client
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
operator|.
name|setSettings
argument_list|(
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"index.number_of_shards"
argument_list|,
literal|1
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.number_of_replicas"
argument_list|,
literal|0
argument_list|)
argument_list|)
argument_list|)
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|StopWatch
name|stopWatch
init|=
operator|new
name|StopWatch
argument_list|()
operator|.
name|start
argument_list|()
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"--> Indexing ["
operator|+
name|COUNT
operator|+
literal|"] ..."
argument_list|)
expr_stmt|;
name|long
name|ITERS
init|=
name|COUNT
operator|/
name|BATCH
decl_stmt|;
name|long
name|i
init|=
literal|1
decl_stmt|;
name|int
name|counter
init|=
literal|0
decl_stmt|;
for|for
control|(
init|;
name|i
operator|<=
name|ITERS
condition|;
name|i
operator|++
control|)
block|{
name|BulkRequestBuilder
name|request
init|=
name|client
operator|.
name|prepareBulk
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|BATCH
condition|;
name|j
operator|++
control|)
block|{
name|counter
operator|++
expr_stmt|;
name|XContentBuilder
name|source
init|=
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"id"
argument_list|,
name|Integer
operator|.
name|valueOf
argument_list|(
name|counter
argument_list|)
argument_list|)
operator|.
name|field
argument_list|(
literal|"l_value"
argument_list|,
name|lValues
index|[
name|counter
operator|%
name|lValues
operator|.
name|length
index|]
argument_list|)
operator|.
name|field
argument_list|(
literal|"date"
argument_list|,
operator|new
name|Date
argument_list|()
argument_list|)
operator|.
name|endObject
argument_list|()
decl_stmt|;
name|request
operator|.
name|add
argument_list|(
name|Requests
operator|.
name|indexRequest
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setType
argument_list|(
literal|"type1"
argument_list|)
operator|.
name|setId
argument_list|(
name|Integer
operator|.
name|toString
argument_list|(
name|counter
argument_list|)
argument_list|)
operator|.
name|setSource
argument_list|(
name|source
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|BulkResponse
name|response
init|=
name|request
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
if|if
condition|(
name|response
operator|.
name|hasFailures
argument_list|()
condition|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"--> failures..."
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|(
operator|(
name|i
operator|*
name|BATCH
operator|)
operator|%
literal|10000
operator|)
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
literal|"--> Indexed "
operator|+
operator|(
name|i
operator|*
name|BATCH
operator|)
operator|+
literal|" took "
operator|+
name|stopWatch
operator|.
name|stop
argument_list|()
operator|.
name|lastTaskTime
argument_list|()
argument_list|)
expr_stmt|;
name|stopWatch
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
block|}
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"--> Indexing took "
operator|+
name|stopWatch
operator|.
name|totalTime
argument_list|()
operator|+
literal|", TPS "
operator|+
operator|(
operator|(
call|(
name|double
call|)
argument_list|(
name|COUNT
argument_list|)
operator|)
operator|/
name|stopWatch
operator|.
name|totalTime
argument_list|()
operator|.
name|secondsFrac
argument_list|()
operator|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"--> Index already exists, ignoring indexing phase, waiting for green"
argument_list|)
expr_stmt|;
name|ClusterHealthResponse
name|clusterHealthResponse
init|=
name|client
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareHealth
argument_list|()
operator|.
name|setWaitForGreenStatus
argument_list|()
operator|.
name|setTimeout
argument_list|(
literal|"10m"
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
if|if
condition|(
name|clusterHealthResponse
operator|.
name|isTimedOut
argument_list|()
condition|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"--> Timed out waiting for cluster health"
argument_list|)
expr_stmt|;
block|}
block|}
name|client
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareRefresh
argument_list|()
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|COUNT
operator|=
name|client
operator|.
name|prepareCount
argument_list|()
operator|.
name|setQuery
argument_list|(
name|matchAllQuery
argument_list|()
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
operator|.
name|getCount
argument_list|()
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"--> Number of docs in index: "
operator|+
name|COUNT
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"--> Warmup..."
argument_list|)
expr_stmt|;
comment|// run just the child query, warm up first
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|QUERY_WARMUP
condition|;
name|j
operator|++
control|)
block|{
name|SearchResponse
name|searchResponse
init|=
name|client
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
name|addFacet
argument_list|(
name|histogramFacet
argument_list|(
literal|"l_value"
argument_list|)
operator|.
name|field
argument_list|(
literal|"l_value"
argument_list|)
operator|.
name|interval
argument_list|(
literal|4
argument_list|)
argument_list|)
operator|.
name|addFacet
argument_list|(
name|histogramFacet
argument_list|(
literal|"date"
argument_list|)
operator|.
name|field
argument_list|(
literal|"date"
argument_list|)
operator|.
name|interval
argument_list|(
literal|1000
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
if|if
condition|(
name|j
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
literal|"--> Warmup took: "
operator|+
name|searchResponse
operator|.
name|getTook
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|searchResponse
operator|.
name|getHits
argument_list|()
operator|.
name|totalHits
argument_list|()
operator|!=
name|COUNT
condition|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"--> mismatch on hits"
argument_list|)
expr_stmt|;
block|}
block|}
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"--> Warmup DONE"
argument_list|)
expr_stmt|;
name|long
name|totalQueryTime
init|=
literal|0
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|QUERY_COUNT
condition|;
name|j
operator|++
control|)
block|{
name|SearchResponse
name|searchResponse
init|=
name|client
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
name|addFacet
argument_list|(
name|histogramFacet
argument_list|(
literal|"l_value"
argument_list|)
operator|.
name|field
argument_list|(
literal|"l_value"
argument_list|)
operator|.
name|interval
argument_list|(
literal|4
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
if|if
condition|(
name|searchResponse
operator|.
name|getHits
argument_list|()
operator|.
name|totalHits
argument_list|()
operator|!=
name|COUNT
condition|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"--> mismatch on hits"
argument_list|)
expr_stmt|;
block|}
name|totalQueryTime
operator|+=
name|searchResponse
operator|.
name|getTookInMillis
argument_list|()
expr_stmt|;
block|}
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"--> Histogram Facet (l_value) "
operator|+
operator|(
name|totalQueryTime
operator|/
name|QUERY_COUNT
operator|)
operator|+
literal|"ms"
argument_list|)
expr_stmt|;
name|totalQueryTime
operator|=
literal|0
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
name|QUERY_COUNT
condition|;
name|j
operator|++
control|)
block|{
name|SearchResponse
name|searchResponse
init|=
name|client
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
name|addFacet
argument_list|(
name|histogramFacet
argument_list|(
literal|"l_value"
argument_list|)
operator|.
name|field
argument_list|(
literal|"l_value"
argument_list|)
operator|.
name|valueField
argument_list|(
literal|"l_value"
argument_list|)
operator|.
name|interval
argument_list|(
literal|4
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
if|if
condition|(
name|searchResponse
operator|.
name|getHits
argument_list|()
operator|.
name|totalHits
argument_list|()
operator|!=
name|COUNT
condition|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"--> mismatch on hits"
argument_list|)
expr_stmt|;
block|}
name|totalQueryTime
operator|+=
name|searchResponse
operator|.
name|getTookInMillis
argument_list|()
expr_stmt|;
block|}
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"--> Histogram Facet (l_value/l_value) "
operator|+
operator|(
name|totalQueryTime
operator|/
name|QUERY_COUNT
operator|)
operator|+
literal|"ms"
argument_list|)
expr_stmt|;
name|totalQueryTime
operator|=
literal|0
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
name|QUERY_COUNT
condition|;
name|j
operator|++
control|)
block|{
name|SearchResponse
name|searchResponse
init|=
name|client
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
name|addFacet
argument_list|(
name|histogramFacet
argument_list|(
literal|"date"
argument_list|)
operator|.
name|field
argument_list|(
literal|"date"
argument_list|)
operator|.
name|interval
argument_list|(
literal|1000
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
if|if
condition|(
name|searchResponse
operator|.
name|getHits
argument_list|()
operator|.
name|totalHits
argument_list|()
operator|!=
name|COUNT
condition|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"--> mismatch on hits"
argument_list|)
expr_stmt|;
block|}
name|totalQueryTime
operator|+=
name|searchResponse
operator|.
name|getTookInMillis
argument_list|()
expr_stmt|;
block|}
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"--> Histogram Facet (date) "
operator|+
operator|(
name|totalQueryTime
operator|/
name|QUERY_COUNT
operator|)
operator|+
literal|"ms"
argument_list|)
expr_stmt|;
name|totalQueryTime
operator|=
literal|0
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
name|QUERY_COUNT
condition|;
name|j
operator|++
control|)
block|{
name|SearchResponse
name|searchResponse
init|=
name|client
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
name|addFacet
argument_list|(
name|histogramFacet
argument_list|(
literal|"date"
argument_list|)
operator|.
name|field
argument_list|(
literal|"date"
argument_list|)
operator|.
name|valueField
argument_list|(
literal|"l_value"
argument_list|)
operator|.
name|interval
argument_list|(
literal|1000
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
if|if
condition|(
name|searchResponse
operator|.
name|getHits
argument_list|()
operator|.
name|totalHits
argument_list|()
operator|!=
name|COUNT
condition|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"--> mismatch on hits"
argument_list|)
expr_stmt|;
block|}
name|totalQueryTime
operator|+=
name|searchResponse
operator|.
name|getTookInMillis
argument_list|()
expr_stmt|;
block|}
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"--> Histogram Facet (date/l_value) "
operator|+
operator|(
name|totalQueryTime
operator|/
name|QUERY_COUNT
operator|)
operator|+
literal|"ms"
argument_list|)
expr_stmt|;
name|totalQueryTime
operator|=
literal|0
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
name|QUERY_COUNT
condition|;
name|j
operator|++
control|)
block|{
name|SearchResponse
name|searchResponse
init|=
name|client
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
name|addFacet
argument_list|(
name|dateHistogramFacet
argument_list|(
literal|"date"
argument_list|)
operator|.
name|field
argument_list|(
literal|"date"
argument_list|)
operator|.
name|interval
argument_list|(
literal|"day"
argument_list|)
operator|.
name|mode
argument_list|(
name|FacetBuilder
operator|.
name|Mode
operator|.
name|COLLECTOR
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
if|if
condition|(
name|searchResponse
operator|.
name|getHits
argument_list|()
operator|.
name|totalHits
argument_list|()
operator|!=
name|COUNT
condition|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"--> mismatch on hits"
argument_list|)
expr_stmt|;
block|}
name|totalQueryTime
operator|+=
name|searchResponse
operator|.
name|getTookInMillis
argument_list|()
expr_stmt|;
block|}
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"--> Date Histogram Facet (mode/collector) (date) "
operator|+
operator|(
name|totalQueryTime
operator|/
name|QUERY_COUNT
operator|)
operator|+
literal|"ms"
argument_list|)
expr_stmt|;
name|totalQueryTime
operator|=
literal|0
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
name|QUERY_COUNT
condition|;
name|j
operator|++
control|)
block|{
name|SearchResponse
name|searchResponse
init|=
name|client
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
name|addFacet
argument_list|(
name|dateHistogramFacet
argument_list|(
literal|"date"
argument_list|)
operator|.
name|field
argument_list|(
literal|"date"
argument_list|)
operator|.
name|interval
argument_list|(
literal|"day"
argument_list|)
operator|.
name|mode
argument_list|(
name|FacetBuilder
operator|.
name|Mode
operator|.
name|POST
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
if|if
condition|(
name|searchResponse
operator|.
name|getHits
argument_list|()
operator|.
name|totalHits
argument_list|()
operator|!=
name|COUNT
condition|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"--> mismatch on hits"
argument_list|)
expr_stmt|;
block|}
name|totalQueryTime
operator|+=
name|searchResponse
operator|.
name|getTookInMillis
argument_list|()
expr_stmt|;
block|}
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"--> Date Histogram Facet (mode/post) (date) "
operator|+
operator|(
name|totalQueryTime
operator|/
name|QUERY_COUNT
operator|)
operator|+
literal|"ms"
argument_list|)
expr_stmt|;
name|node1
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

