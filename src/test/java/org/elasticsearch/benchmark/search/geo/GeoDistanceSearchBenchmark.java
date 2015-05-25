begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.benchmark.search.geo
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|benchmark
operator|.
name|search
operator|.
name|geo
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
name|geo
operator|.
name|GeoDistance
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
name|node
operator|.
name|NodeBuilder
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
name|geoDistanceQuery
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
name|filteredQuery
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

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|GeoDistanceSearchBenchmark
specifier|public
class|class
name|GeoDistanceSearchBenchmark
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
name|Node
name|node
init|=
name|NodeBuilder
operator|.
name|nodeBuilder
argument_list|()
operator|.
name|clusterName
argument_list|(
name|GeoDistanceSearchBenchmark
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
argument_list|)
operator|.
name|node
argument_list|()
decl_stmt|;
name|Client
name|client
init|=
name|node
operator|.
name|client
argument_list|()
decl_stmt|;
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
literal|"Failed to wait for green status, bailing"
argument_list|)
expr_stmt|;
name|System
operator|.
name|exit
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
specifier|final
name|long
name|NUM_DOCS
init|=
name|SizeValue
operator|.
name|parseSizeValue
argument_list|(
literal|"1m"
argument_list|)
operator|.
name|singles
argument_list|()
decl_stmt|;
specifier|final
name|long
name|NUM_WARM
init|=
literal|50
decl_stmt|;
specifier|final
name|long
name|NUM_RUNS
init|=
literal|100
decl_stmt|;
if|if
condition|(
name|client
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareExists
argument_list|(
literal|"test"
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
operator|.
name|isExists
argument_list|()
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Found an index, count: "
operator|+
name|client
operator|.
name|prepareCount
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
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
block|}
else|else
block|{
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
literal|"location"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"geo_point"
argument_list|)
operator|.
name|field
argument_list|(
literal|"lat_lon"
argument_list|,
literal|true
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
name|string
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
name|prepareCreate
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setSettings
argument_list|(
name|Settings
operator|.
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
operator|.
name|addMapping
argument_list|(
literal|"type1"
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
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"--> Indexing ["
operator|+
name|NUM_DOCS
operator|+
literal|"]"
argument_list|)
expr_stmt|;
for|for
control|(
name|long
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
name|client
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
name|Long
operator|.
name|toString
argument_list|(
name|i
operator|++
argument_list|)
argument_list|)
operator|.
name|setSource
argument_list|(
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"name"
argument_list|,
literal|"New York"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"location"
argument_list|)
operator|.
name|field
argument_list|(
literal|"lat"
argument_list|,
literal|40.7143528
argument_list|)
operator|.
name|field
argument_list|(
literal|"lon"
argument_list|,
operator|-
literal|74.0059731
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
comment|// to NY: 5.286 km
name|client
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
name|Long
operator|.
name|toString
argument_list|(
name|i
operator|++
argument_list|)
argument_list|)
operator|.
name|setSource
argument_list|(
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"name"
argument_list|,
literal|"Times Square"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"location"
argument_list|)
operator|.
name|field
argument_list|(
literal|"lat"
argument_list|,
literal|40.759011
argument_list|)
operator|.
name|field
argument_list|(
literal|"lon"
argument_list|,
operator|-
literal|73.9844722
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
comment|// to NY: 0.4621 km
name|client
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
name|Long
operator|.
name|toString
argument_list|(
name|i
operator|++
argument_list|)
argument_list|)
operator|.
name|setSource
argument_list|(
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"name"
argument_list|,
literal|"Tribeca"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"location"
argument_list|)
operator|.
name|field
argument_list|(
literal|"lat"
argument_list|,
literal|40.718266
argument_list|)
operator|.
name|field
argument_list|(
literal|"lon"
argument_list|,
operator|-
literal|74.007819
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
comment|// to NY: 1.258 km
name|client
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
name|Long
operator|.
name|toString
argument_list|(
name|i
operator|++
argument_list|)
argument_list|)
operator|.
name|setSource
argument_list|(
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"name"
argument_list|,
literal|"Soho"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"location"
argument_list|)
operator|.
name|field
argument_list|(
literal|"lat"
argument_list|,
literal|40.7247222
argument_list|)
operator|.
name|field
argument_list|(
literal|"lon"
argument_list|,
operator|-
literal|74
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
comment|// to NY: 8.572 km
name|client
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
name|Long
operator|.
name|toString
argument_list|(
name|i
operator|++
argument_list|)
argument_list|)
operator|.
name|setSource
argument_list|(
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"name"
argument_list|,
literal|"Brooklyn"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"location"
argument_list|)
operator|.
name|field
argument_list|(
literal|"lat"
argument_list|,
literal|40.65
argument_list|)
operator|.
name|field
argument_list|(
literal|"lon"
argument_list|,
operator|-
literal|73.95
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
if|if
condition|(
operator|(
name|i
operator|%
literal|10000
operator|)
operator|==
literal|0
condition|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"--> indexed "
operator|+
name|i
argument_list|)
expr_stmt|;
block|}
block|}
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Done indexed"
argument_list|)
expr_stmt|;
name|client
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareFlush
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
block|}
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"--> Warming up (ARC) - optimize_bbox"
argument_list|)
expr_stmt|;
name|long
name|start
init|=
name|System
operator|.
name|currentTimeMillis
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
name|NUM_WARM
condition|;
name|i
operator|++
control|)
block|{
name|run
argument_list|(
name|client
argument_list|,
name|GeoDistance
operator|.
name|ARC
argument_list|,
literal|"memory"
argument_list|)
expr_stmt|;
block|}
name|long
name|totalTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|start
decl_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"--> Warmup (ARC)  - optimize_bbox (memory) "
operator|+
operator|(
name|totalTime
operator|/
name|NUM_WARM
operator|)
operator|+
literal|"ms"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"--> Perf (ARC) - optimize_bbox (memory)"
argument_list|)
expr_stmt|;
name|start
operator|=
name|System
operator|.
name|currentTimeMillis
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
name|NUM_RUNS
condition|;
name|i
operator|++
control|)
block|{
name|run
argument_list|(
name|client
argument_list|,
name|GeoDistance
operator|.
name|ARC
argument_list|,
literal|"memory"
argument_list|)
expr_stmt|;
block|}
name|totalTime
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|start
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"--> Perf (ARC) - optimize_bbox "
operator|+
operator|(
name|totalTime
operator|/
name|NUM_RUNS
operator|)
operator|+
literal|"ms"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"--> Warming up (ARC)  - optimize_bbox (indexed)"
argument_list|)
expr_stmt|;
name|start
operator|=
name|System
operator|.
name|currentTimeMillis
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
name|NUM_WARM
condition|;
name|i
operator|++
control|)
block|{
name|run
argument_list|(
name|client
argument_list|,
name|GeoDistance
operator|.
name|ARC
argument_list|,
literal|"indexed"
argument_list|)
expr_stmt|;
block|}
name|totalTime
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|start
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"--> Warmup (ARC) - optimize_bbox (indexed) "
operator|+
operator|(
name|totalTime
operator|/
name|NUM_WARM
operator|)
operator|+
literal|"ms"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"--> Perf (ARC) - optimize_bbox (indexed)"
argument_list|)
expr_stmt|;
name|start
operator|=
name|System
operator|.
name|currentTimeMillis
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
name|NUM_RUNS
condition|;
name|i
operator|++
control|)
block|{
name|run
argument_list|(
name|client
argument_list|,
name|GeoDistance
operator|.
name|ARC
argument_list|,
literal|"indexed"
argument_list|)
expr_stmt|;
block|}
name|totalTime
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|start
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"--> Perf (ARC) - optimize_bbox (indexed) "
operator|+
operator|(
name|totalTime
operator|/
name|NUM_RUNS
operator|)
operator|+
literal|"ms"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"--> Warming up (ARC)  - no optimize_bbox"
argument_list|)
expr_stmt|;
name|start
operator|=
name|System
operator|.
name|currentTimeMillis
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
name|NUM_WARM
condition|;
name|i
operator|++
control|)
block|{
name|run
argument_list|(
name|client
argument_list|,
name|GeoDistance
operator|.
name|ARC
argument_list|,
literal|"none"
argument_list|)
expr_stmt|;
block|}
name|totalTime
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|start
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"--> Warmup (ARC) - no optimize_bbox "
operator|+
operator|(
name|totalTime
operator|/
name|NUM_WARM
operator|)
operator|+
literal|"ms"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"--> Perf (ARC) - no optimize_bbox"
argument_list|)
expr_stmt|;
name|start
operator|=
name|System
operator|.
name|currentTimeMillis
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
name|NUM_RUNS
condition|;
name|i
operator|++
control|)
block|{
name|run
argument_list|(
name|client
argument_list|,
name|GeoDistance
operator|.
name|ARC
argument_list|,
literal|"none"
argument_list|)
expr_stmt|;
block|}
name|totalTime
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|start
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"--> Perf (ARC) - no optimize_bbox "
operator|+
operator|(
name|totalTime
operator|/
name|NUM_RUNS
operator|)
operator|+
literal|"ms"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"--> Warming up (SLOPPY_ARC)"
argument_list|)
expr_stmt|;
name|start
operator|=
name|System
operator|.
name|currentTimeMillis
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
name|NUM_WARM
condition|;
name|i
operator|++
control|)
block|{
name|run
argument_list|(
name|client
argument_list|,
name|GeoDistance
operator|.
name|SLOPPY_ARC
argument_list|,
literal|"memory"
argument_list|)
expr_stmt|;
block|}
name|totalTime
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|start
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"--> Warmup (SLOPPY_ARC) "
operator|+
operator|(
name|totalTime
operator|/
name|NUM_WARM
operator|)
operator|+
literal|"ms"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"--> Perf (SLOPPY_ARC)"
argument_list|)
expr_stmt|;
name|start
operator|=
name|System
operator|.
name|currentTimeMillis
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
name|NUM_RUNS
condition|;
name|i
operator|++
control|)
block|{
name|run
argument_list|(
name|client
argument_list|,
name|GeoDistance
operator|.
name|SLOPPY_ARC
argument_list|,
literal|"memory"
argument_list|)
expr_stmt|;
block|}
name|totalTime
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|start
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"--> Perf (SLOPPY_ARC) "
operator|+
operator|(
name|totalTime
operator|/
name|NUM_RUNS
operator|)
operator|+
literal|"ms"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"--> Warming up (PLANE)"
argument_list|)
expr_stmt|;
name|start
operator|=
name|System
operator|.
name|currentTimeMillis
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
name|NUM_WARM
condition|;
name|i
operator|++
control|)
block|{
name|run
argument_list|(
name|client
argument_list|,
name|GeoDistance
operator|.
name|PLANE
argument_list|,
literal|"memory"
argument_list|)
expr_stmt|;
block|}
name|totalTime
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|start
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"--> Warmup (PLANE) "
operator|+
operator|(
name|totalTime
operator|/
name|NUM_WARM
operator|)
operator|+
literal|"ms"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"--> Perf (PLANE)"
argument_list|)
expr_stmt|;
name|start
operator|=
name|System
operator|.
name|currentTimeMillis
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
name|NUM_RUNS
condition|;
name|i
operator|++
control|)
block|{
name|run
argument_list|(
name|client
argument_list|,
name|GeoDistance
operator|.
name|PLANE
argument_list|,
literal|"memory"
argument_list|)
expr_stmt|;
block|}
name|totalTime
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|start
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"--> Perf (PLANE) "
operator|+
operator|(
name|totalTime
operator|/
name|NUM_RUNS
operator|)
operator|+
literal|"ms"
argument_list|)
expr_stmt|;
name|node
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
DECL|method|run
specifier|public
specifier|static
name|void
name|run
parameter_list|(
name|Client
name|client
parameter_list|,
name|GeoDistance
name|geoDistance
parameter_list|,
name|String
name|optimizeBbox
parameter_list|)
block|{
name|client
operator|.
name|prepareSearch
argument_list|()
comment|// from NY
operator|.
name|setSize
argument_list|(
literal|0
argument_list|)
operator|.
name|setQuery
argument_list|(
name|filteredQuery
argument_list|(
name|matchAllQuery
argument_list|()
argument_list|,
name|geoDistanceQuery
argument_list|(
literal|"location"
argument_list|)
operator|.
name|distance
argument_list|(
literal|"2km"
argument_list|)
operator|.
name|optimizeBbox
argument_list|(
name|optimizeBbox
argument_list|)
operator|.
name|geoDistance
argument_list|(
name|geoDistance
argument_list|)
operator|.
name|point
argument_list|(
literal|40.7143528
argument_list|,
operator|-
literal|74.0059731
argument_list|)
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
block|}
end_class

end_unit

