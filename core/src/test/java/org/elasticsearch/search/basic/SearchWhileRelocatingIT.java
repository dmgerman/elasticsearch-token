begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.basic
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|basic
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
name|common
operator|.
name|Priority
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
name|ESIntegTestCase
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
name|concurrent
operator|.
name|CopyOnWriteArrayList
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
name|atomic
operator|.
name|AtomicBoolean
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
name|test
operator|.
name|hamcrest
operator|.
name|ElasticsearchAssertions
operator|.
name|assertHitCount
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
name|assertNoTimeout
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
name|formatShardStatus
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

begin_class
annotation|@
name|ESIntegTestCase
operator|.
name|ClusterScope
argument_list|(
name|minNumDataNodes
operator|=
literal|2
argument_list|)
DECL|class|SearchWhileRelocatingIT
specifier|public
class|class
name|SearchWhileRelocatingIT
extends|extends
name|ESIntegTestCase
block|{
DECL|method|testSearchAndRelocateConcurrentlyRandomReplicas
specifier|public
name|void
name|testSearchAndRelocateConcurrentlyRandomReplicas
parameter_list|()
throws|throws
name|Exception
block|{
name|testSearchAndRelocateConcurrently
argument_list|(
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testSearchAndRelocateConcurrently
specifier|private
name|void
name|testSearchAndRelocateConcurrently
parameter_list|(
specifier|final
name|int
name|numberOfReplicas
parameter_list|)
throws|throws
name|Exception
block|{
specifier|final
name|int
name|numShards
init|=
name|between
argument_list|(
literal|1
argument_list|,
literal|20
argument_list|)
decl_stmt|;
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
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"index.number_of_shards"
argument_list|,
name|numShards
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.number_of_replicas"
argument_list|,
name|numberOfReplicas
argument_list|)
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"type"
argument_list|,
literal|"loc"
argument_list|,
literal|"type=geo_point"
argument_list|,
literal|"test"
argument_list|,
literal|"type=text"
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
name|List
argument_list|<
name|IndexRequestBuilder
argument_list|>
name|indexBuilders
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
specifier|final
name|int
name|numDocs
init|=
name|between
argument_list|(
literal|10
argument_list|,
literal|20
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
name|numDocs
condition|;
name|i
operator|++
control|)
block|{
name|indexBuilders
operator|.
name|add
argument_list|(
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
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"test"
argument_list|,
literal|"value"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"loc"
argument_list|)
operator|.
name|field
argument_list|(
literal|"lat"
argument_list|,
literal|11
argument_list|)
operator|.
name|field
argument_list|(
literal|"lon"
argument_list|,
literal|21
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|indexRandom
argument_list|(
literal|true
argument_list|,
name|indexBuilders
operator|.
name|toArray
argument_list|(
operator|new
name|IndexRequestBuilder
index|[
name|indexBuilders
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|assertHitCount
argument_list|(
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|()
operator|.
name|get
argument_list|()
argument_list|,
operator|(
name|numDocs
operator|)
argument_list|)
expr_stmt|;
specifier|final
name|int
name|numIters
init|=
name|scaledRandomIntBetween
argument_list|(
literal|5
argument_list|,
literal|20
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
name|numIters
condition|;
name|i
operator|++
control|)
block|{
specifier|final
name|AtomicBoolean
name|stop
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|nonCriticalExceptions
init|=
operator|new
name|CopyOnWriteArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|Thread
index|[]
name|threads
init|=
operator|new
name|Thread
index|[
name|scaledRandomIntBetween
argument_list|(
literal|1
argument_list|,
literal|3
argument_list|)
index|]
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
name|threads
operator|.
name|length
condition|;
name|j
operator|++
control|)
block|{
name|threads
index|[
name|j
index|]
operator|=
operator|new
name|Thread
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
while|while
condition|(
operator|!
name|stop
operator|.
name|get
argument_list|()
condition|)
block|{
name|SearchResponse
name|sr
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|()
operator|.
name|setSize
argument_list|(
name|numDocs
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|sr
operator|.
name|getHits
argument_list|()
operator|.
name|totalHits
argument_list|()
operator|!=
name|numDocs
condition|)
block|{
comment|// if we did not search all shards but had no failures that is potentially fine
comment|// if only the hit-count is wrong. this can happen if the cluster-state is behind when the
comment|// request comes in. It's a small window but a known limitation.
if|if
condition|(
name|sr
operator|.
name|getTotalShards
argument_list|()
operator|!=
name|sr
operator|.
name|getSuccessfulShards
argument_list|()
operator|&&
name|sr
operator|.
name|getFailedShards
argument_list|()
operator|==
literal|0
condition|)
block|{
name|nonCriticalExceptions
operator|.
name|add
argument_list|(
literal|"Count is "
operator|+
name|sr
operator|.
name|getHits
argument_list|()
operator|.
name|totalHits
argument_list|()
operator|+
literal|" but "
operator|+
name|numDocs
operator|+
literal|" was expected. "
operator|+
name|formatShardStatus
argument_list|(
name|sr
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertHitCount
argument_list|(
name|sr
argument_list|,
name|numDocs
argument_list|)
expr_stmt|;
block|}
block|}
specifier|final
name|SearchHits
name|sh
init|=
name|sr
operator|.
name|getHits
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
literal|"Expected hits to be the same size the actual hits array"
argument_list|,
name|sh
operator|.
name|getTotalHits
argument_list|()
argument_list|,
name|equalTo
argument_list|(
call|(
name|long
call|)
argument_list|(
name|sh
operator|.
name|getHits
argument_list|()
operator|.
name|length
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// this is the more critical but that we hit the actual hit array has a different size than the
comment|// actual number of hits.
block|}
block|}
catch|catch
parameter_list|(
name|SearchPhaseExecutionException
name|ex
parameter_list|)
block|{
comment|// it's possible that all shards fail if we have a small number of shards.
comment|// with replicas this should not happen
if|if
condition|(
name|numberOfReplicas
operator|==
literal|1
operator|||
operator|!
name|ex
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"all shards failed"
argument_list|)
condition|)
block|{
throw|throw
name|ex
throw|;
block|}
block|}
block|}
block|}
expr_stmt|;
block|}
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|threads
operator|.
name|length
condition|;
name|j
operator|++
control|)
block|{
name|threads
index|[
name|j
index|]
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
name|allowNodes
argument_list|(
literal|"test"
argument_list|,
name|between
argument_list|(
literal|1
argument_list|,
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareReroute
argument_list|()
operator|.
name|get
argument_list|()
expr_stmt|;
name|stop
operator|.
name|set
argument_list|(
literal|true
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
name|threads
operator|.
name|length
condition|;
name|j
operator|++
control|)
block|{
name|threads
index|[
name|j
index|]
operator|.
name|join
argument_list|()
expr_stmt|;
block|}
comment|// this might time out on some machines if they are really busy and you hit lots of throttling
name|ClusterHealthResponse
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
name|prepareHealth
argument_list|()
operator|.
name|setWaitForYellowStatus
argument_list|()
operator|.
name|setWaitForRelocatingShards
argument_list|(
literal|0
argument_list|)
operator|.
name|setWaitForEvents
argument_list|(
name|Priority
operator|.
name|LANGUID
argument_list|)
operator|.
name|setTimeout
argument_list|(
literal|"5m"
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertNoTimeout
argument_list|(
name|resp
argument_list|)
expr_stmt|;
comment|// if we hit only non-critical exceptions we make sure that the post search works
if|if
condition|(
operator|!
name|nonCriticalExceptions
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"non-critical exceptions: {}"
argument_list|,
name|nonCriticalExceptions
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
literal|10
condition|;
name|j
operator|++
control|)
block|{
name|assertHitCount
argument_list|(
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|()
operator|.
name|get
argument_list|()
argument_list|,
name|numDocs
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
end_class

end_unit

