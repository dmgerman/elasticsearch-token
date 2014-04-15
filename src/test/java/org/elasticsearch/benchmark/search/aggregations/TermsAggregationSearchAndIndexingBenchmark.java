begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.benchmark.search.aggregations
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|benchmark
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
name|carrotsearch
operator|.
name|hppc
operator|.
name|ObjectOpenHashSet
import|;
end_import

begin_import
import|import
name|com
operator|.
name|carrotsearch
operator|.
name|randomizedtesting
operator|.
name|generators
operator|.
name|RandomStrings
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
name|admin
operator|.
name|cluster
operator|.
name|stats
operator|.
name|ClusterStatsResponse
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
name|get
operator|.
name|GetResponse
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
name|jna
operator|.
name|Natives
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
name|ByteSizeValue
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
name|discovery
operator|.
name|Discovery
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|IndexAlreadyExistsException
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
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Locale
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Random
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
name|ThreadLocalRandom
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|benchmark
operator|.
name|search
operator|.
name|aggregations
operator|.
name|TermsAggregationSearchBenchmark
operator|.
name|Method
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

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|TermsAggregationSearchAndIndexingBenchmark
specifier|public
class|class
name|TermsAggregationSearchAndIndexingBenchmark
block|{
DECL|field|indexName
specifier|static
name|String
name|indexName
init|=
literal|"test"
decl_stmt|;
DECL|field|typeName
specifier|static
name|String
name|typeName
init|=
literal|"type1"
decl_stmt|;
DECL|field|random
specifier|static
name|Random
name|random
init|=
operator|new
name|Random
argument_list|()
decl_stmt|;
DECL|field|COUNT
specifier|static
name|long
name|COUNT
init|=
name|SizeValue
operator|.
name|parseSizeValue
argument_list|(
literal|"2m"
argument_list|)
operator|.
name|singles
argument_list|()
decl_stmt|;
DECL|field|BATCH
specifier|static
name|int
name|BATCH
init|=
literal|1000
decl_stmt|;
DECL|field|NUMBER_OF_TERMS
specifier|static
name|int
name|NUMBER_OF_TERMS
init|=
operator|(
name|int
operator|)
name|SizeValue
operator|.
name|parseSizeValue
argument_list|(
literal|"100k"
argument_list|)
operator|.
name|singles
argument_list|()
decl_stmt|;
DECL|field|NUMBER_OF_MULTI_VALUE_TERMS
specifier|static
name|int
name|NUMBER_OF_MULTI_VALUE_TERMS
init|=
literal|10
decl_stmt|;
DECL|field|STRING_TERM_SIZE
specifier|static
name|int
name|STRING_TERM_SIZE
init|=
literal|5
decl_stmt|;
DECL|field|nodes
specifier|static
name|InternalNode
index|[]
name|nodes
decl_stmt|;
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
name|Natives
operator|.
name|tryMlockall
argument_list|()
expr_stmt|;
name|Settings
name|settings
init|=
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"refresh_interval"
argument_list|,
literal|"-1"
argument_list|)
operator|.
name|put
argument_list|(
name|SETTING_NUMBER_OF_SHARDS
argument_list|,
literal|1
argument_list|)
operator|.
name|put
argument_list|(
name|SETTING_NUMBER_OF_REPLICAS
argument_list|,
literal|0
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|String
name|clusterName
init|=
name|TermsAggregationSearchAndIndexingBenchmark
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
decl_stmt|;
name|nodes
operator|=
operator|new
name|InternalNode
index|[
literal|1
index|]
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
name|nodes
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|nodes
index|[
name|i
index|]
operator|=
operator|(
name|InternalNode
operator|)
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
name|clusterName
argument_list|(
name|clusterName
argument_list|)
operator|.
name|node
argument_list|()
expr_stmt|;
block|}
name|Client
name|client
init|=
name|nodes
index|[
literal|0
index|]
operator|.
name|client
argument_list|()
decl_stmt|;
name|client
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareHealth
argument_list|(
name|indexName
argument_list|)
operator|.
name|setWaitForGreenStatus
argument_list|()
operator|.
name|setTimeout
argument_list|(
literal|"10s"
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
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
name|prepareCreate
argument_list|(
name|indexName
argument_list|)
operator|.
name|addMapping
argument_list|(
name|typeName
argument_list|,
name|generateMapping
argument_list|(
literal|"eager"
argument_list|,
literal|"lazy"
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|5000
argument_list|)
expr_stmt|;
name|long
name|startTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|ObjectOpenHashSet
argument_list|<
name|String
argument_list|>
name|uniqueTerms
init|=
name|ObjectOpenHashSet
operator|.
name|newInstance
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
name|NUMBER_OF_TERMS
condition|;
name|i
operator|++
control|)
block|{
name|boolean
name|added
decl_stmt|;
do|do
block|{
name|added
operator|=
name|uniqueTerms
operator|.
name|add
argument_list|(
name|RandomStrings
operator|.
name|randomAsciiOfLength
argument_list|(
name|random
argument_list|,
name|STRING_TERM_SIZE
argument_list|)
argument_list|)
expr_stmt|;
block|}
do|while
condition|(
operator|!
name|added
condition|)
do|;
block|}
name|String
index|[]
name|sValues
init|=
name|uniqueTerms
operator|.
name|toArray
argument_list|(
name|String
operator|.
name|class
argument_list|)
decl_stmt|;
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
name|builder
init|=
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
decl_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"id"
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|counter
argument_list|)
argument_list|)
expr_stmt|;
specifier|final
name|String
name|sValue
init|=
name|sValues
index|[
name|counter
operator|%
name|sValues
operator|.
name|length
index|]
decl_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"s_value"
argument_list|,
name|sValue
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"s_value_dv"
argument_list|,
name|sValue
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|field
range|:
operator|new
name|String
index|[]
block|{
literal|"sm_value"
block|,
literal|"sm_value_dv"
block|}
control|)
block|{
name|builder
operator|.
name|startArray
argument_list|(
name|field
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|k
init|=
literal|0
init|;
name|k
operator|<
name|NUMBER_OF_MULTI_VALUE_TERMS
condition|;
name|k
operator|++
control|)
block|{
name|builder
operator|.
name|value
argument_list|(
name|sValues
index|[
name|ThreadLocalRandom
operator|.
name|current
argument_list|()
operator|.
name|nextInt
argument_list|(
name|sValues
operator|.
name|length
argument_list|)
index|]
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endArray
argument_list|()
expr_stmt|;
block|}
name|request
operator|.
name|add
argument_list|(
name|Requests
operator|.
name|indexRequest
argument_list|(
name|indexName
argument_list|)
operator|.
name|type
argument_list|(
literal|"type1"
argument_list|)
operator|.
name|id
argument_list|(
name|Integer
operator|.
name|toString
argument_list|(
name|counter
argument_list|)
argument_list|)
operator|.
name|source
argument_list|(
name|builder
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
literal|"--> Indexing took "
operator|+
operator|(
operator|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|startTime
operator|)
operator|/
literal|1000
operator|)
operator|+
literal|" seconds."
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IndexAlreadyExistsException
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
argument_list|(
name|indexName
argument_list|)
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
name|preparePutMapping
argument_list|(
name|indexName
argument_list|)
operator|.
name|setType
argument_list|(
name|typeName
argument_list|)
operator|.
name|setSource
argument_list|(
name|generateMapping
argument_list|(
literal|"lazy"
argument_list|,
literal|"lazy"
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
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
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"--> Number of docs in index: "
operator|+
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
argument_list|)
expr_stmt|;
name|String
index|[]
name|nodeIds
init|=
operator|new
name|String
index|[
name|nodes
operator|.
name|length
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
name|nodeIds
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|nodeIds
index|[
name|i
index|]
operator|=
name|nodes
index|[
name|i
index|]
operator|.
name|injector
argument_list|()
operator|.
name|getInstance
argument_list|(
name|Discovery
operator|.
name|class
argument_list|)
operator|.
name|localNode
argument_list|()
operator|.
name|getId
argument_list|()
expr_stmt|;
block|}
name|List
argument_list|<
name|TestRun
argument_list|>
name|testRuns
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|testRuns
operator|.
name|add
argument_list|(
operator|new
name|TestRun
argument_list|(
literal|"Regular field ordinals"
argument_list|,
literal|"eager"
argument_list|,
literal|"lazy"
argument_list|,
literal|"s_value"
argument_list|,
literal|"ordinals"
argument_list|)
argument_list|)
expr_stmt|;
name|testRuns
operator|.
name|add
argument_list|(
operator|new
name|TestRun
argument_list|(
literal|"Docvalues field ordinals"
argument_list|,
literal|"lazy"
argument_list|,
literal|"eager"
argument_list|,
literal|"s_value_dv"
argument_list|,
literal|"ordinals"
argument_list|)
argument_list|)
expr_stmt|;
name|testRuns
operator|.
name|add
argument_list|(
operator|new
name|TestRun
argument_list|(
literal|"Regular field global ordinals"
argument_list|,
literal|"eager_global_ordinals"
argument_list|,
literal|"lazy"
argument_list|,
literal|"s_value"
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|testRuns
operator|.
name|add
argument_list|(
operator|new
name|TestRun
argument_list|(
literal|"Docvalues field global"
argument_list|,
literal|"lazy"
argument_list|,
literal|"eager_global_ordinals"
argument_list|,
literal|"s_value_dv"
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|TestResult
argument_list|>
name|testResults
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|TestRun
name|testRun
range|:
name|testRuns
control|)
block|{
name|client
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|preparePutMapping
argument_list|(
name|indexName
argument_list|)
operator|.
name|setType
argument_list|(
name|typeName
argument_list|)
operator|.
name|setSource
argument_list|(
name|generateMapping
argument_list|(
name|testRun
operator|.
name|indexedFieldEagerLoading
argument_list|,
name|testRun
operator|.
name|docValuesEagerLoading
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|client
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareClearCache
argument_list|(
name|indexName
argument_list|)
operator|.
name|setFieldDataCache
argument_list|(
literal|true
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|SearchThread
name|searchThread
init|=
operator|new
name|SearchThread
argument_list|(
name|client
argument_list|,
name|testRun
operator|.
name|termsAggsField
argument_list|,
name|testRun
operator|.
name|termsAggsExecutionHint
argument_list|)
decl_stmt|;
name|RefreshThread
name|refreshThread
init|=
operator|new
name|RefreshThread
argument_list|(
name|client
argument_list|)
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"--> Running '"
operator|+
name|testRun
operator|.
name|name
operator|+
literal|"' round..."
argument_list|)
expr_stmt|;
operator|new
name|Thread
argument_list|(
name|refreshThread
argument_list|)
operator|.
name|start
argument_list|()
expr_stmt|;
operator|new
name|Thread
argument_list|(
name|searchThread
argument_list|)
operator|.
name|start
argument_list|()
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|2
operator|*
literal|60
operator|*
literal|1000
argument_list|)
expr_stmt|;
name|refreshThread
operator|.
name|stop
argument_list|()
expr_stmt|;
name|searchThread
operator|.
name|stop
argument_list|()
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"--> Avg refresh time: "
operator|+
name|refreshThread
operator|.
name|avgRefreshTime
operator|+
literal|" ms"
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"--> Avg query time: "
operator|+
name|searchThread
operator|.
name|avgQueryTime
operator|+
literal|" ms"
argument_list|)
expr_stmt|;
name|ClusterStatsResponse
name|clusterStateResponse
init|=
name|client
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareClusterStats
argument_list|()
operator|.
name|setNodesIds
argument_list|(
name|nodeIds
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"--> Heap used: "
operator|+
name|clusterStateResponse
operator|.
name|getNodesStats
argument_list|()
operator|.
name|getJvm
argument_list|()
operator|.
name|getHeapUsed
argument_list|()
argument_list|)
expr_stmt|;
name|ByteSizeValue
name|fieldDataMemoryUsed
init|=
name|clusterStateResponse
operator|.
name|getIndicesStats
argument_list|()
operator|.
name|getFieldData
argument_list|()
operator|.
name|getMemorySize
argument_list|()
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"--> Fielddata memory size: "
operator|+
name|fieldDataMemoryUsed
argument_list|)
expr_stmt|;
name|testResults
operator|.
name|add
argument_list|(
operator|new
name|TestResult
argument_list|(
name|testRun
operator|.
name|name
argument_list|,
name|refreshThread
operator|.
name|avgRefreshTime
argument_list|,
name|searchThread
operator|.
name|avgQueryTime
argument_list|,
name|fieldDataMemoryUsed
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"----------------------------------------- SUMMARY ----------------------------------------------"
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|format
argument_list|(
name|Locale
operator|.
name|ENGLISH
argument_list|,
literal|"%30s%18s%15s%15s\n"
argument_list|,
literal|"name"
argument_list|,
literal|"avg refresh time"
argument_list|,
literal|"avg query time"
argument_list|,
literal|"fieldata size"
argument_list|)
expr_stmt|;
for|for
control|(
name|TestResult
name|testResult
range|:
name|testResults
control|)
block|{
name|System
operator|.
name|out
operator|.
name|format
argument_list|(
name|Locale
operator|.
name|ENGLISH
argument_list|,
literal|"%30s%18s%15s%15s\n"
argument_list|,
name|testResult
operator|.
name|name
argument_list|,
name|testResult
operator|.
name|avgRefreshTime
argument_list|,
name|testResult
operator|.
name|avgQueryTime
argument_list|,
name|testResult
operator|.
name|fieldDataSizeInMemory
argument_list|)
expr_stmt|;
block|}
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"----------------------------------------- SUMMARY ----------------------------------------------"
argument_list|)
expr_stmt|;
name|client
operator|.
name|close
argument_list|()
expr_stmt|;
for|for
control|(
name|InternalNode
name|node
range|:
name|nodes
control|)
block|{
name|node
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
DECL|class|RefreshThread
specifier|static
class|class
name|RefreshThread
implements|implements
name|Runnable
block|{
DECL|field|client
specifier|private
specifier|final
name|Client
name|client
decl_stmt|;
DECL|field|run
specifier|private
specifier|volatile
name|boolean
name|run
init|=
literal|true
decl_stmt|;
DECL|field|stopped
specifier|private
specifier|volatile
name|boolean
name|stopped
init|=
literal|false
decl_stmt|;
DECL|field|avgRefreshTime
specifier|private
specifier|volatile
name|long
name|avgRefreshTime
init|=
literal|0
decl_stmt|;
DECL|method|RefreshThread
name|RefreshThread
parameter_list|(
name|Client
name|client
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|client
operator|=
name|client
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|run
specifier|public
name|void
name|run
parameter_list|()
block|{
name|long
name|totalRefreshTime
init|=
literal|0
decl_stmt|;
name|int
name|numExecutedRefreshed
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|run
condition|)
block|{
name|long
name|docIdLimit
init|=
name|COUNT
decl_stmt|;
for|for
control|(
name|long
name|docId
init|=
literal|1
init|;
name|run
operator|&&
name|docId
operator|<
name|docIdLimit
condition|;
control|)
block|{
try|try
block|{
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
literal|8
condition|;
name|j
operator|++
control|)
block|{
name|GetResponse
name|getResponse
init|=
name|client
operator|.
name|prepareGet
argument_list|(
name|indexName
argument_list|,
literal|"type1"
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
operator|++
name|docId
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|client
operator|.
name|prepareIndex
argument_list|(
name|indexName
argument_list|,
literal|"type1"
argument_list|,
name|getResponse
operator|.
name|getId
argument_list|()
argument_list|)
operator|.
name|setSource
argument_list|(
name|getResponse
operator|.
name|getSource
argument_list|()
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
name|long
name|startTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|client
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareRefresh
argument_list|(
name|indexName
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|totalRefreshTime
operator|+=
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|startTime
expr_stmt|;
name|numExecutedRefreshed
operator|++
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|500
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
block|}
block|}
name|avgRefreshTime
operator|=
name|totalRefreshTime
operator|/
name|numExecutedRefreshed
expr_stmt|;
name|stopped
operator|=
literal|true
expr_stmt|;
block|}
DECL|method|stop
specifier|public
name|void
name|stop
parameter_list|()
throws|throws
name|InterruptedException
block|{
name|run
operator|=
literal|false
expr_stmt|;
while|while
condition|(
operator|!
name|stopped
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|class|TestRun
specifier|private
specifier|static
class|class
name|TestRun
block|{
DECL|field|name
specifier|final
name|String
name|name
decl_stmt|;
DECL|field|indexedFieldEagerLoading
specifier|final
name|String
name|indexedFieldEagerLoading
decl_stmt|;
DECL|field|docValuesEagerLoading
specifier|final
name|String
name|docValuesEagerLoading
decl_stmt|;
DECL|field|termsAggsField
specifier|final
name|String
name|termsAggsField
decl_stmt|;
DECL|field|termsAggsExecutionHint
specifier|final
name|String
name|termsAggsExecutionHint
decl_stmt|;
DECL|method|TestRun
specifier|private
name|TestRun
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|indexedFieldEagerLoading
parameter_list|,
name|String
name|docValuesEagerLoading
parameter_list|,
name|String
name|termsAggsField
parameter_list|,
name|String
name|termsAggsExecutionHint
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
name|this
operator|.
name|indexedFieldEagerLoading
operator|=
name|indexedFieldEagerLoading
expr_stmt|;
name|this
operator|.
name|docValuesEagerLoading
operator|=
name|docValuesEagerLoading
expr_stmt|;
name|this
operator|.
name|termsAggsField
operator|=
name|termsAggsField
expr_stmt|;
name|this
operator|.
name|termsAggsExecutionHint
operator|=
name|termsAggsExecutionHint
expr_stmt|;
block|}
block|}
DECL|class|TestResult
specifier|private
specifier|static
class|class
name|TestResult
block|{
DECL|field|name
specifier|final
name|String
name|name
decl_stmt|;
DECL|field|avgRefreshTime
specifier|final
name|TimeValue
name|avgRefreshTime
decl_stmt|;
DECL|field|avgQueryTime
specifier|final
name|TimeValue
name|avgQueryTime
decl_stmt|;
DECL|field|fieldDataSizeInMemory
specifier|final
name|ByteSizeValue
name|fieldDataSizeInMemory
decl_stmt|;
DECL|method|TestResult
specifier|private
name|TestResult
parameter_list|(
name|String
name|name
parameter_list|,
name|long
name|avgRefreshTime
parameter_list|,
name|long
name|avgQueryTime
parameter_list|,
name|ByteSizeValue
name|fieldDataSizeInMemory
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
name|this
operator|.
name|avgRefreshTime
operator|=
name|TimeValue
operator|.
name|timeValueMillis
argument_list|(
name|avgRefreshTime
argument_list|)
expr_stmt|;
name|this
operator|.
name|avgQueryTime
operator|=
name|TimeValue
operator|.
name|timeValueMillis
argument_list|(
name|avgQueryTime
argument_list|)
expr_stmt|;
name|this
operator|.
name|fieldDataSizeInMemory
operator|=
name|fieldDataSizeInMemory
expr_stmt|;
block|}
block|}
DECL|class|SearchThread
specifier|static
class|class
name|SearchThread
implements|implements
name|Runnable
block|{
DECL|field|client
specifier|private
specifier|final
name|Client
name|client
decl_stmt|;
DECL|field|field
specifier|private
specifier|final
name|String
name|field
decl_stmt|;
DECL|field|executionHint
specifier|private
specifier|final
name|String
name|executionHint
decl_stmt|;
DECL|field|run
specifier|private
specifier|volatile
name|boolean
name|run
init|=
literal|true
decl_stmt|;
DECL|field|stopped
specifier|private
specifier|volatile
name|boolean
name|stopped
init|=
literal|false
decl_stmt|;
DECL|field|avgQueryTime
specifier|private
specifier|volatile
name|long
name|avgQueryTime
init|=
literal|0
decl_stmt|;
DECL|method|SearchThread
name|SearchThread
parameter_list|(
name|Client
name|client
parameter_list|,
name|String
name|field
parameter_list|,
name|String
name|executionHint
parameter_list|)
block|{
name|this
operator|.
name|client
operator|=
name|client
expr_stmt|;
name|this
operator|.
name|field
operator|=
name|field
expr_stmt|;
name|this
operator|.
name|executionHint
operator|=
name|executionHint
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|run
specifier|public
name|void
name|run
parameter_list|()
block|{
name|long
name|totalQueryTime
init|=
literal|0
decl_stmt|;
name|int
name|numExecutedQueries
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|run
condition|)
block|{
try|try
block|{
name|SearchResponse
name|searchResponse
init|=
name|Method
operator|.
name|AGGREGATION
operator|.
name|addTermsAgg
argument_list|(
name|client
operator|.
name|prepareSearch
argument_list|()
operator|.
name|setSearchType
argument_list|(
name|SearchType
operator|.
name|COUNT
argument_list|)
operator|.
name|setQuery
argument_list|(
name|matchAllQuery
argument_list|()
argument_list|)
argument_list|,
literal|"test"
argument_list|,
name|field
argument_list|,
name|executionHint
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
name|numExecutedQueries
operator|++
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
block|}
name|avgQueryTime
operator|=
name|totalQueryTime
operator|/
name|numExecutedQueries
expr_stmt|;
name|stopped
operator|=
literal|true
expr_stmt|;
block|}
DECL|method|stop
specifier|public
name|void
name|stop
parameter_list|()
throws|throws
name|InterruptedException
block|{
name|run
operator|=
literal|false
expr_stmt|;
while|while
condition|(
operator|!
name|stopped
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|generateMapping
specifier|private
specifier|static
name|XContentBuilder
name|generateMapping
parameter_list|(
name|String
name|loading1
parameter_list|,
name|String
name|loading2
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"type1"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"properties"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"s_value"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"string"
argument_list|)
operator|.
name|field
argument_list|(
literal|"index"
argument_list|,
literal|"not_analyzed"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"fielddata"
argument_list|)
operator|.
name|field
argument_list|(
literal|"loading"
argument_list|,
name|loading1
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"s_value_dv"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"string"
argument_list|)
operator|.
name|field
argument_list|(
literal|"index"
argument_list|,
literal|"no"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"fielddata"
argument_list|)
operator|.
name|field
argument_list|(
literal|"loading"
argument_list|,
name|loading2
argument_list|)
operator|.
name|field
argument_list|(
literal|"format"
argument_list|,
literal|"doc_values"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
return|;
block|}
block|}
end_class

end_unit

