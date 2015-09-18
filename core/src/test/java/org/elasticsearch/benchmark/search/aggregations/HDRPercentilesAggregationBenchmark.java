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
name|randomizedtesting
operator|.
name|generators
operator|.
name|RandomInts
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
name|SizeUnit
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
name|aggregations
operator|.
name|metrics
operator|.
name|percentiles
operator|.
name|PercentilesMethod
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
name|hdr
operator|.
name|InternalHDRPercentiles
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
name|tdigest
operator|.
name|InternalTDigestPercentiles
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
name|TimeUnit
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
name|aggregations
operator|.
name|AggregationBuilders
operator|.
name|percentiles
import|;
end_import

begin_class
DECL|class|HDRPercentilesAggregationBenchmark
specifier|public
class|class
name|HDRPercentilesAggregationBenchmark
block|{
DECL|field|TYPE_NAME
specifier|private
specifier|static
specifier|final
name|String
name|TYPE_NAME
init|=
literal|"type"
decl_stmt|;
DECL|field|INDEX_NAME
specifier|private
specifier|static
specifier|final
name|String
name|INDEX_NAME
init|=
literal|"index"
decl_stmt|;
DECL|field|HIGH_CARD_FIELD_NAME
specifier|private
specifier|static
specifier|final
name|String
name|HIGH_CARD_FIELD_NAME
init|=
literal|"high_card"
decl_stmt|;
DECL|field|LOW_CARD_FIELD_NAME
specifier|private
specifier|static
specifier|final
name|String
name|LOW_CARD_FIELD_NAME
init|=
literal|"low_card"
decl_stmt|;
DECL|field|GAUSSIAN_FIELD_NAME
specifier|private
specifier|static
specifier|final
name|String
name|GAUSSIAN_FIELD_NAME
init|=
literal|"gauss"
decl_stmt|;
DECL|field|R
specifier|private
specifier|static
specifier|final
name|Random
name|R
init|=
operator|new
name|Random
argument_list|()
decl_stmt|;
DECL|field|CLUSTER_NAME
specifier|private
specifier|static
specifier|final
name|String
name|CLUSTER_NAME
init|=
name|HDRPercentilesAggregationBenchmark
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
decl_stmt|;
DECL|field|NUM_DOCS
specifier|private
specifier|static
specifier|final
name|int
name|NUM_DOCS
init|=
literal|10000000
decl_stmt|;
DECL|field|LOW_CARD
specifier|private
specifier|static
specifier|final
name|int
name|LOW_CARD
init|=
literal|1000
decl_stmt|;
DECL|field|HIGH_CARD
specifier|private
specifier|static
specifier|final
name|int
name|HIGH_CARD
init|=
literal|1000000
decl_stmt|;
DECL|field|BATCH
specifier|private
specifier|static
specifier|final
name|int
name|BATCH
init|=
literal|100
decl_stmt|;
DECL|field|WARM
specifier|private
specifier|static
specifier|final
name|int
name|WARM
init|=
literal|5
decl_stmt|;
DECL|field|RUNS
specifier|private
specifier|static
specifier|final
name|int
name|RUNS
init|=
literal|10
decl_stmt|;
DECL|field|ITERS
specifier|private
specifier|static
specifier|final
name|int
name|ITERS
init|=
literal|5
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
block|{
name|long
name|overallStartTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|Settings
name|settings
init|=
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"index.refresh_interval"
argument_list|,
literal|"-1"
argument_list|)
operator|.
name|put
argument_list|(
name|SETTING_NUMBER_OF_SHARDS
argument_list|,
literal|5
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
name|Node
index|[]
name|nodes
init|=
operator|new
name|Node
index|[
literal|1
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
name|nodeBuilder
argument_list|()
operator|.
name|clusterName
argument_list|(
name|CLUSTER_NAME
argument_list|)
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
literal|"node"
operator|+
name|i
argument_list|)
argument_list|)
operator|.
name|node
argument_list|()
expr_stmt|;
block|}
name|Node
name|clientNode
init|=
name|nodeBuilder
argument_list|()
operator|.
name|clusterName
argument_list|(
name|CLUSTER_NAME
argument_list|)
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
literal|"client"
argument_list|)
argument_list|)
operator|.
name|client
argument_list|(
literal|true
argument_list|)
operator|.
name|node
argument_list|()
decl_stmt|;
name|Client
name|client
init|=
name|clientNode
operator|.
name|client
argument_list|()
decl_stmt|;
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
name|INDEX_NAME
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Indexing "
operator|+
name|NUM_DOCS
operator|+
literal|" documents"
argument_list|)
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
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|NUM_DOCS
condition|;
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
operator|&&
name|i
operator|<
name|NUM_DOCS
condition|;
operator|++
name|j
control|)
block|{
specifier|final
name|int
name|lowCard
init|=
name|RandomInts
operator|.
name|randomInt
argument_list|(
name|R
argument_list|,
name|LOW_CARD
argument_list|)
decl_stmt|;
specifier|final
name|int
name|highCard
init|=
name|RandomInts
operator|.
name|randomInt
argument_list|(
name|R
argument_list|,
name|HIGH_CARD
argument_list|)
decl_stmt|;
name|int
name|gauss
init|=
operator|-
literal|1
decl_stmt|;
while|while
condition|(
name|gauss
operator|<
literal|0
condition|)
block|{
name|gauss
operator|=
call|(
name|int
call|)
argument_list|(
name|R
operator|.
name|nextGaussian
argument_list|()
operator|*
literal|1000
argument_list|)
operator|+
literal|5000
expr_stmt|;
comment|// mean: 5 sec, std deviation: 1 sec
block|}
name|request
operator|.
name|add
argument_list|(
name|client
operator|.
name|prepareIndex
argument_list|(
name|INDEX_NAME
argument_list|,
name|TYPE_NAME
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
name|LOW_CARD_FIELD_NAME
argument_list|,
name|lowCard
argument_list|,
name|HIGH_CARD_FIELD_NAME
argument_list|,
name|highCard
argument_list|,
name|GAUSSIAN_FIELD_NAME
argument_list|,
name|gauss
argument_list|)
argument_list|)
expr_stmt|;
operator|++
name|i
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
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
name|response
operator|.
name|buildFailureMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|(
name|i
operator|%
literal|100000
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
name|i
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
name|INDEX_NAME
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
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
literal|"Index already exists, skipping index creation"
argument_list|)
expr_stmt|;
block|}
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
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Run\tField\tMethod\tAggregationTime\tEstimatedMemory"
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
name|WARM
operator|+
name|RUNS
condition|;
operator|++
name|i
control|)
block|{
for|for
control|(
name|String
name|field
range|:
operator|new
name|String
index|[]
block|{
name|LOW_CARD_FIELD_NAME
block|,
name|HIGH_CARD_FIELD_NAME
block|,
name|GAUSSIAN_FIELD_NAME
block|}
control|)
block|{
for|for
control|(
name|PercentilesMethod
name|method
range|:
operator|new
name|PercentilesMethod
index|[]
block|{
name|PercentilesMethod
operator|.
name|TDIGEST
block|,
name|PercentilesMethod
operator|.
name|HDR
block|}
control|)
block|{
name|long
name|start
init|=
name|System
operator|.
name|nanoTime
argument_list|()
decl_stmt|;
name|SearchResponse
name|resp
init|=
literal|null
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
name|ITERS
condition|;
operator|++
name|j
control|)
block|{
name|resp
operator|=
name|client
operator|.
name|prepareSearch
argument_list|(
name|INDEX_NAME
argument_list|)
operator|.
name|setSize
argument_list|(
literal|0
argument_list|)
operator|.
name|addAggregation
argument_list|(
name|percentiles
argument_list|(
literal|"percentiles"
argument_list|)
operator|.
name|field
argument_list|(
name|field
argument_list|)
operator|.
name|method
argument_list|(
name|method
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
block|}
name|long
name|end
init|=
name|System
operator|.
name|nanoTime
argument_list|()
decl_stmt|;
name|long
name|memoryEstimate
init|=
literal|0
decl_stmt|;
switch|switch
condition|(
name|method
condition|)
block|{
case|case
name|TDIGEST
case|:
name|memoryEstimate
operator|=
operator|(
operator|(
name|InternalTDigestPercentiles
operator|)
name|resp
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"percentiles"
argument_list|)
operator|)
operator|.
name|getEstimatedMemoryFootprint
argument_list|()
expr_stmt|;
break|break;
case|case
name|HDR
case|:
name|memoryEstimate
operator|=
operator|(
operator|(
name|InternalHDRPercentiles
operator|)
name|resp
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"percentiles"
argument_list|)
operator|)
operator|.
name|getEstimatedMemoryFootprint
argument_list|()
expr_stmt|;
break|break;
block|}
if|if
condition|(
name|i
operator|>=
name|WARM
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
operator|(
name|i
operator|-
name|WARM
operator|)
operator|+
literal|"\t"
operator|+
name|field
operator|+
literal|"\t"
operator|+
name|method
operator|+
literal|"\t"
operator|+
operator|new
name|TimeValue
argument_list|(
operator|(
name|end
operator|-
name|start
operator|)
operator|/
name|ITERS
argument_list|,
name|TimeUnit
operator|.
name|NANOSECONDS
argument_list|)
operator|.
name|millis
argument_list|()
operator|+
literal|"\t"
operator|+
operator|new
name|SizeValue
argument_list|(
name|memoryEstimate
argument_list|,
name|SizeUnit
operator|.
name|SINGLE
argument_list|)
operator|.
name|singles
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
name|long
name|overallEndTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Benchmark completed in "
operator|+
operator|(
operator|(
name|overallEndTime
operator|-
name|overallStartTime
operator|)
operator|/
literal|1000
operator|)
operator|+
literal|" seconds"
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit
