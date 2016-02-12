begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.indices.memory.breaker
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|memory
operator|.
name|breaker
package|;
end_package

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
name|DirectoryReader
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
name|LeafReader
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
name|node
operator|.
name|stats
operator|.
name|NodeStats
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
name|node
operator|.
name|stats
operator|.
name|NodesStatsResponse
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
name|refresh
operator|.
name|RefreshResponse
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
name|SearchRequestBuilder
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
name|breaker
operator|.
name|CircuitBreaker
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
name|Setting
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
name|settings
operator|.
name|SettingsModule
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
name|XContentFactory
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
name|MockEngineFactoryPlugin
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
name|QueryBuilders
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
name|IndicesService
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
name|fielddata
operator|.
name|cache
operator|.
name|IndicesFieldDataCache
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|plugins
operator|.
name|Plugin
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
name|sort
operator|.
name|SortOrder
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
name|ESIntegTestCase
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
name|engine
operator|.
name|MockEngineSupport
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
name|engine
operator|.
name|ThrowingLeafReaderWrapper
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
name|Arrays
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
name|ExecutionException
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
name|test
operator|.
name|hamcrest
operator|.
name|ElasticsearchAssertions
operator|.
name|assertAllSuccessful
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

begin_comment
comment|/**  * Tests for the circuit breaker while random exceptions are happening  */
end_comment

begin_class
DECL|class|RandomExceptionCircuitBreakerIT
specifier|public
class|class
name|RandomExceptionCircuitBreakerIT
extends|extends
name|ESIntegTestCase
block|{
annotation|@
name|Override
DECL|method|nodePlugins
specifier|protected
name|Collection
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|Plugin
argument_list|>
argument_list|>
name|nodePlugins
parameter_list|()
block|{
return|return
name|pluginList
argument_list|(
name|RandomExceptionDirectoryReaderWrapper
operator|.
name|TestPlugin
operator|.
name|class
argument_list|,
name|MockEngineFactoryPlugin
operator|.
name|class
argument_list|)
return|;
block|}
DECL|method|testBreakerWithRandomExceptions
specifier|public
name|void
name|testBreakerWithRandomExceptions
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
throws|,
name|ExecutionException
block|{
for|for
control|(
name|NodeStats
name|node
range|:
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareNodesStats
argument_list|()
operator|.
name|clear
argument_list|()
operator|.
name|setBreaker
argument_list|(
literal|true
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
operator|.
name|getNodes
argument_list|()
control|)
block|{
name|assertThat
argument_list|(
literal|"Breaker is not set to 0"
argument_list|,
name|node
operator|.
name|getBreaker
argument_list|()
operator|.
name|getStats
argument_list|(
name|CircuitBreaker
operator|.
name|FIELDDATA
argument_list|)
operator|.
name|getEstimated
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|String
name|mapping
init|=
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"type"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"properties"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"test-str"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"keyword"
argument_list|)
operator|.
name|field
argument_list|(
literal|"doc_values"
argument_list|,
name|randomBoolean
argument_list|()
argument_list|)
operator|.
name|endObject
argument_list|()
comment|// test-str
operator|.
name|startObject
argument_list|(
literal|"test-num"
argument_list|)
comment|// I don't use randomNumericType() here because I don't want "byte", and I want "float" and "double"
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
name|randomFrom
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"float"
argument_list|,
literal|"long"
argument_list|,
literal|"double"
argument_list|,
literal|"short"
argument_list|,
literal|"integer"
argument_list|)
argument_list|)
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"fielddata"
argument_list|)
operator|.
name|endObject
argument_list|()
comment|// fielddata
operator|.
name|endObject
argument_list|()
comment|// test-num
operator|.
name|endObject
argument_list|()
comment|// properties
operator|.
name|endObject
argument_list|()
comment|// type
operator|.
name|endObject
argument_list|()
comment|// {}
operator|.
name|string
argument_list|()
decl_stmt|;
specifier|final
name|double
name|topLevelRate
decl_stmt|;
specifier|final
name|double
name|lowLevelRate
decl_stmt|;
if|if
condition|(
name|frequently
argument_list|()
condition|)
block|{
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|lowLevelRate
operator|=
literal|1.0
operator|/
name|between
argument_list|(
literal|2
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|topLevelRate
operator|=
literal|0.0d
expr_stmt|;
block|}
else|else
block|{
name|topLevelRate
operator|=
literal|1.0
operator|/
name|between
argument_list|(
literal|2
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|lowLevelRate
operator|=
literal|0.0d
expr_stmt|;
block|}
block|}
else|else
block|{
name|lowLevelRate
operator|=
literal|1.0
operator|/
name|between
argument_list|(
literal|2
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|topLevelRate
operator|=
literal|1.0
operator|/
name|between
argument_list|(
literal|2
argument_list|,
literal|10
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
comment|// rarely no exception
name|topLevelRate
operator|=
literal|0d
expr_stmt|;
name|lowLevelRate
operator|=
literal|0d
expr_stmt|;
block|}
name|Settings
operator|.
name|Builder
name|settings
init|=
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
name|indexSettings
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|EXCEPTION_TOP_LEVEL_RATIO_KEY
argument_list|,
name|topLevelRate
argument_list|)
operator|.
name|put
argument_list|(
name|EXCEPTION_LOW_LEVEL_RATIO_KEY
argument_list|,
name|lowLevelRate
argument_list|)
operator|.
name|put
argument_list|(
name|MockEngineSupport
operator|.
name|WRAP_READER_RATIO
operator|.
name|getKey
argument_list|()
argument_list|,
literal|1.0d
argument_list|)
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"creating index: [test] using settings: [{}]"
argument_list|,
name|settings
operator|.
name|build
argument_list|()
operator|.
name|getAsMap
argument_list|()
argument_list|)
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
name|prepareCreate
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setSettings
argument_list|(
name|settings
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"type"
argument_list|,
name|mapping
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|ClusterHealthResponse
name|clusterHealthResponse
init|=
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|health
argument_list|(
name|Requests
operator|.
name|clusterHealthRequest
argument_list|()
operator|.
name|waitForYellowStatus
argument_list|()
operator|.
name|timeout
argument_list|(
name|TimeValue
operator|.
name|timeValueSeconds
argument_list|(
literal|5
argument_list|)
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
comment|// it's OK to timeout here
specifier|final
name|int
name|numDocs
decl_stmt|;
if|if
condition|(
name|clusterHealthResponse
operator|.
name|isTimedOut
argument_list|()
condition|)
block|{
comment|/* some seeds just won't let you create the index at all and we enter a ping-pong mode              * trying one node after another etc. that is ok but we need to make sure we don't wait              * forever when indexing documents so we set numDocs = 1 and expect all shards to fail              * when we search below.*/
name|logger
operator|.
name|info
argument_list|(
literal|"ClusterHealth timed out - only index one doc and expect searches to fail"
argument_list|)
expr_stmt|;
name|numDocs
operator|=
literal|1
expr_stmt|;
block|}
else|else
block|{
name|numDocs
operator|=
name|between
argument_list|(
literal|10
argument_list|,
literal|100
argument_list|)
expr_stmt|;
block|}
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
name|i
operator|++
control|)
block|{
try|try
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
literal|""
operator|+
name|i
argument_list|)
operator|.
name|setTimeout
argument_list|(
name|TimeValue
operator|.
name|timeValueSeconds
argument_list|(
literal|1
argument_list|)
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"test-str"
argument_list|,
name|randomUnicodeOfLengthBetween
argument_list|(
literal|5
argument_list|,
literal|25
argument_list|)
argument_list|,
literal|"test-num"
argument_list|,
name|i
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ElasticsearchException
name|ex
parameter_list|)
block|{             }
block|}
name|logger
operator|.
name|info
argument_list|(
literal|"Start Refresh"
argument_list|)
expr_stmt|;
name|RefreshResponse
name|refreshResponse
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
name|prepareRefresh
argument_list|(
literal|"test"
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|get
argument_list|()
decl_stmt|;
comment|// don't assert on failures here
specifier|final
name|boolean
name|refreshFailed
init|=
name|refreshResponse
operator|.
name|getShardFailures
argument_list|()
operator|.
name|length
operator|!=
literal|0
operator|||
name|refreshResponse
operator|.
name|getFailedShards
argument_list|()
operator|!=
literal|0
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"Refresh failed: [{}] numShardsFailed: [{}], shardFailuresLength: [{}], successfulShards: [{}], totalShards: [{}] "
argument_list|,
name|refreshFailed
argument_list|,
name|refreshResponse
operator|.
name|getFailedShards
argument_list|()
argument_list|,
name|refreshResponse
operator|.
name|getShardFailures
argument_list|()
operator|.
name|length
argument_list|,
name|refreshResponse
operator|.
name|getSuccessfulShards
argument_list|()
argument_list|,
name|refreshResponse
operator|.
name|getTotalShards
argument_list|()
argument_list|)
expr_stmt|;
specifier|final
name|int
name|numSearches
init|=
name|scaledRandomIntBetween
argument_list|(
literal|50
argument_list|,
literal|150
argument_list|)
decl_stmt|;
name|NodesStatsResponse
name|resp
init|=
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareNodesStats
argument_list|()
operator|.
name|clear
argument_list|()
operator|.
name|setBreaker
argument_list|(
literal|true
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
for|for
control|(
name|NodeStats
name|stats
range|:
name|resp
operator|.
name|getNodes
argument_list|()
control|)
block|{
name|assertThat
argument_list|(
literal|"Breaker is set to 0"
argument_list|,
name|stats
operator|.
name|getBreaker
argument_list|()
operator|.
name|getStats
argument_list|(
name|CircuitBreaker
operator|.
name|FIELDDATA
argument_list|)
operator|.
name|getEstimated
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
block|}
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
name|SearchRequestBuilder
name|searchRequestBuilder
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|()
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
name|matchAllQuery
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|random
argument_list|()
operator|.
name|nextBoolean
argument_list|()
condition|)
block|{
name|searchRequestBuilder
operator|.
name|addSort
argument_list|(
literal|"test-str"
argument_list|,
name|SortOrder
operator|.
name|ASC
argument_list|)
expr_stmt|;
block|}
name|searchRequestBuilder
operator|.
name|addSort
argument_list|(
literal|"test-num"
argument_list|,
name|SortOrder
operator|.
name|ASC
argument_list|)
expr_stmt|;
name|boolean
name|success
init|=
literal|false
decl_stmt|;
try|try
block|{
comment|// Sort by the string and numeric fields, to load them into field data
name|searchRequestBuilder
operator|.
name|get
argument_list|()
expr_stmt|;
name|success
operator|=
literal|true
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SearchPhaseExecutionException
name|ex
parameter_list|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"expected SearchPhaseException: [{}]"
argument_list|,
name|ex
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|frequently
argument_list|()
condition|)
block|{
comment|// Now, clear the cache and check that the circuit breaker has been
comment|// successfully set back to zero. If there is a bug in the circuit
comment|// breaker adjustment code, it should show up here by the breaker
comment|// estimate being either positive or negative.
name|ensureGreen
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
comment|// make sure all shards are there - there could be shards that are still starting up.
name|assertAllSuccessful
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
name|prepareClearCache
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setFieldDataCache
argument_list|(
literal|true
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
argument_list|)
expr_stmt|;
comment|// Since .cleanUp() is no longer called on cache clear, we need to call it on each node manually
for|for
control|(
name|String
name|node
range|:
name|internalCluster
argument_list|()
operator|.
name|getNodeNames
argument_list|()
control|)
block|{
specifier|final
name|IndicesFieldDataCache
name|fdCache
init|=
name|internalCluster
argument_list|()
operator|.
name|getInstance
argument_list|(
name|IndicesService
operator|.
name|class
argument_list|,
name|node
argument_list|)
operator|.
name|getIndicesFieldDataCache
argument_list|()
decl_stmt|;
comment|// Clean up the cache, ensuring that entries' listeners have been called
name|fdCache
operator|.
name|getCache
argument_list|()
operator|.
name|refresh
argument_list|()
expr_stmt|;
block|}
name|NodesStatsResponse
name|nodeStats
init|=
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareNodesStats
argument_list|()
operator|.
name|clear
argument_list|()
operator|.
name|setBreaker
argument_list|(
literal|true
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
for|for
control|(
name|NodeStats
name|stats
range|:
name|nodeStats
operator|.
name|getNodes
argument_list|()
control|)
block|{
name|assertThat
argument_list|(
literal|"Breaker reset to 0 last search success: "
operator|+
name|success
operator|+
literal|" mapping: "
operator|+
name|mapping
argument_list|,
name|stats
operator|.
name|getBreaker
argument_list|()
operator|.
name|getStats
argument_list|(
name|CircuitBreaker
operator|.
name|FIELDDATA
argument_list|)
operator|.
name|getEstimated
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
DECL|field|EXCEPTION_TOP_LEVEL_RATIO_KEY
specifier|public
specifier|static
specifier|final
name|String
name|EXCEPTION_TOP_LEVEL_RATIO_KEY
init|=
literal|"index.engine.exception.ratio.top"
decl_stmt|;
DECL|field|EXCEPTION_LOW_LEVEL_RATIO_KEY
specifier|public
specifier|static
specifier|final
name|String
name|EXCEPTION_LOW_LEVEL_RATIO_KEY
init|=
literal|"index.engine.exception.ratio.low"
decl_stmt|;
comment|// TODO: Generalize this class and add it as a utility
DECL|class|RandomExceptionDirectoryReaderWrapper
specifier|public
specifier|static
class|class
name|RandomExceptionDirectoryReaderWrapper
extends|extends
name|MockEngineSupport
operator|.
name|DirectoryReaderWrapper
block|{
DECL|field|EXCEPTION_TOP_LEVEL_RATIO_SETTING
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|Double
argument_list|>
name|EXCEPTION_TOP_LEVEL_RATIO_SETTING
init|=
name|Setting
operator|.
name|doubleSetting
argument_list|(
name|EXCEPTION_TOP_LEVEL_RATIO_KEY
argument_list|,
literal|0.1d
argument_list|,
literal|0.0d
argument_list|,
literal|false
argument_list|,
name|Setting
operator|.
name|Scope
operator|.
name|INDEX
argument_list|)
decl_stmt|;
DECL|field|EXCEPTION_LOW_LEVEL_RATIO_SETTING
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|Double
argument_list|>
name|EXCEPTION_LOW_LEVEL_RATIO_SETTING
init|=
name|Setting
operator|.
name|doubleSetting
argument_list|(
name|EXCEPTION_LOW_LEVEL_RATIO_KEY
argument_list|,
literal|0.1d
argument_list|,
literal|0.0d
argument_list|,
literal|false
argument_list|,
name|Setting
operator|.
name|Scope
operator|.
name|INDEX
argument_list|)
decl_stmt|;
DECL|class|TestPlugin
specifier|public
specifier|static
class|class
name|TestPlugin
extends|extends
name|Plugin
block|{
annotation|@
name|Override
DECL|method|name
specifier|public
name|String
name|name
parameter_list|()
block|{
return|return
literal|"random-exception-reader-wrapper"
return|;
block|}
annotation|@
name|Override
DECL|method|description
specifier|public
name|String
name|description
parameter_list|()
block|{
return|return
literal|"a mock reader wrapper that throws random exceptions for testing"
return|;
block|}
DECL|method|onModule
specifier|public
name|void
name|onModule
parameter_list|(
name|SettingsModule
name|module
parameter_list|)
block|{
name|module
operator|.
name|registerSetting
argument_list|(
name|EXCEPTION_TOP_LEVEL_RATIO_SETTING
argument_list|)
expr_stmt|;
name|module
operator|.
name|registerSetting
argument_list|(
name|EXCEPTION_LOW_LEVEL_RATIO_SETTING
argument_list|)
expr_stmt|;
block|}
DECL|method|onModule
specifier|public
name|void
name|onModule
parameter_list|(
name|MockEngineFactoryPlugin
operator|.
name|MockEngineReaderModule
name|module
parameter_list|)
block|{
name|module
operator|.
name|setReaderClass
argument_list|(
name|RandomExceptionDirectoryReaderWrapper
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
block|}
DECL|field|settings
specifier|private
specifier|final
name|Settings
name|settings
decl_stmt|;
DECL|class|ThrowingSubReaderWrapper
specifier|static
class|class
name|ThrowingSubReaderWrapper
extends|extends
name|SubReaderWrapper
implements|implements
name|ThrowingLeafReaderWrapper
operator|.
name|Thrower
block|{
DECL|field|random
specifier|private
specifier|final
name|Random
name|random
decl_stmt|;
DECL|field|topLevelRatio
specifier|private
specifier|final
name|double
name|topLevelRatio
decl_stmt|;
DECL|field|lowLevelRatio
specifier|private
specifier|final
name|double
name|lowLevelRatio
decl_stmt|;
DECL|method|ThrowingSubReaderWrapper
name|ThrowingSubReaderWrapper
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
specifier|final
name|long
name|seed
init|=
name|ESIntegTestCase
operator|.
name|INDEX_TEST_SEED_SETTING
operator|.
name|get
argument_list|(
name|settings
argument_list|)
decl_stmt|;
name|this
operator|.
name|topLevelRatio
operator|=
name|EXCEPTION_TOP_LEVEL_RATIO_SETTING
operator|.
name|get
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|lowLevelRatio
operator|=
name|EXCEPTION_LOW_LEVEL_RATIO_SETTING
operator|.
name|get
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|random
operator|=
operator|new
name|Random
argument_list|(
name|seed
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|wrap
specifier|public
name|LeafReader
name|wrap
parameter_list|(
name|LeafReader
name|reader
parameter_list|)
block|{
return|return
operator|new
name|ThrowingLeafReaderWrapper
argument_list|(
name|reader
argument_list|,
name|this
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|maybeThrow
specifier|public
name|void
name|maybeThrow
parameter_list|(
name|ThrowingLeafReaderWrapper
operator|.
name|Flags
name|flag
parameter_list|)
throws|throws
name|IOException
block|{
switch|switch
condition|(
name|flag
condition|)
block|{
case|case
name|Fields
case|:
break|break;
case|case
name|TermVectors
case|:
break|break;
case|case
name|Terms
case|:
case|case
name|TermsEnum
case|:
if|if
condition|(
name|random
operator|.
name|nextDouble
argument_list|()
operator|<
name|topLevelRatio
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Forced top level Exception on ["
operator|+
name|flag
operator|.
name|name
argument_list|()
operator|+
literal|"]"
argument_list|)
throw|;
block|}
break|break;
case|case
name|Intersect
case|:
break|break;
case|case
name|Norms
case|:
break|break;
case|case
name|NumericDocValues
case|:
break|break;
case|case
name|BinaryDocValues
case|:
break|break;
case|case
name|SortedDocValues
case|:
break|break;
case|case
name|SortedSetDocValues
case|:
break|break;
case|case
name|DocsEnum
case|:
case|case
name|DocsAndPositionsEnum
case|:
if|if
condition|(
name|random
operator|.
name|nextDouble
argument_list|()
operator|<
name|lowLevelRatio
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Forced low level Exception on ["
operator|+
name|flag
operator|.
name|name
argument_list|()
operator|+
literal|"]"
argument_list|)
throw|;
block|}
break|break;
block|}
block|}
annotation|@
name|Override
DECL|method|wrapTerms
specifier|public
name|boolean
name|wrapTerms
parameter_list|(
name|String
name|field
parameter_list|)
block|{
return|return
name|field
operator|.
name|startsWith
argument_list|(
literal|"test"
argument_list|)
return|;
block|}
block|}
DECL|method|RandomExceptionDirectoryReaderWrapper
specifier|public
name|RandomExceptionDirectoryReaderWrapper
parameter_list|(
name|DirectoryReader
name|in
parameter_list|,
name|Settings
name|settings
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|in
argument_list|,
operator|new
name|ThrowingSubReaderWrapper
argument_list|(
name|settings
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|settings
operator|=
name|settings
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doWrapDirectoryReader
specifier|protected
name|DirectoryReader
name|doWrapDirectoryReader
parameter_list|(
name|DirectoryReader
name|in
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|RandomExceptionDirectoryReaderWrapper
argument_list|(
name|in
argument_list|,
name|settings
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

