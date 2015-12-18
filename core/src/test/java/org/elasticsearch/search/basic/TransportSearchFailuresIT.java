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
name|WriteConsistencyLevel
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
name|cluster
operator|.
name|health
operator|.
name|ClusterHealthStatus
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
name|index
operator|.
name|query
operator|.
name|GeohashCellQuery
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
name|test
operator|.
name|ESIntegTestCase
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
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|client
operator|.
name|Requests
operator|.
name|clusterHealthRequest
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
name|refreshRequest
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
name|searchRequest
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
name|anyOf
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
name|instanceOf
import|;
end_import

begin_class
DECL|class|TransportSearchFailuresIT
specifier|public
class|class
name|TransportSearchFailuresIT
extends|extends
name|ESIntegTestCase
block|{
annotation|@
name|Override
DECL|method|maximumNumberOfReplicas
specifier|protected
name|int
name|maximumNumberOfReplicas
parameter_list|()
block|{
return|return
literal|1
return|;
block|}
DECL|method|testFailedSearchWithWrongQuery
specifier|public
name|void
name|testFailedSearchWithWrongQuery
parameter_list|()
throws|throws
name|Exception
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"Start Testing failed search with wrong query"
argument_list|)
expr_stmt|;
name|assertAcked
argument_list|(
name|prepareCreate
argument_list|(
literal|"test"
argument_list|,
literal|1
argument_list|,
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"routing.hash.type"
argument_list|,
literal|"simple"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|ensureYellow
argument_list|()
expr_stmt|;
name|NumShards
name|test
init|=
name|getNumShards
argument_list|(
literal|"test"
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
literal|100
condition|;
name|i
operator|++
control|)
block|{
name|index
argument_list|(
name|client
argument_list|()
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|i
argument_list|)
argument_list|,
literal|"test"
argument_list|,
name|i
argument_list|)
expr_stmt|;
block|}
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
decl_stmt|;
name|assertThat
argument_list|(
name|refreshResponse
operator|.
name|getTotalShards
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|test
operator|.
name|totalNumShards
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|refreshResponse
operator|.
name|getSuccessfulShards
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|test
operator|.
name|numPrimaries
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|refreshResponse
operator|.
name|getFailedShards
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
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
literal|5
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
name|client
argument_list|()
operator|.
name|search
argument_list|(
name|searchRequest
argument_list|(
literal|"test"
argument_list|)
operator|.
name|source
argument_list|(
operator|new
name|SearchSourceBuilder
argument_list|()
operator|.
name|query
argument_list|(
operator|new
name|GeohashCellQuery
operator|.
name|Builder
argument_list|(
literal|"foo"
argument_list|,
literal|"biz"
argument_list|)
argument_list|)
argument_list|)
argument_list|)
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|searchResponse
operator|.
name|getTotalShards
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|test
operator|.
name|numPrimaries
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|searchResponse
operator|.
name|getSuccessfulShards
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|searchResponse
operator|.
name|getFailedShards
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|test
operator|.
name|numPrimaries
argument_list|)
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"search should fail"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ElasticsearchException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|e
operator|.
name|unwrapCause
argument_list|()
argument_list|,
name|instanceOf
argument_list|(
name|SearchPhaseExecutionException
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
comment|// all is well
block|}
block|}
name|allowNodes
argument_list|(
literal|"test"
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
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
name|setWaitForEvents
argument_list|(
name|Priority
operator|.
name|LANGUID
argument_list|)
operator|.
name|setWaitForNodes
argument_list|(
literal|">=2"
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
operator|.
name|isTimedOut
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"Running Cluster Health"
argument_list|)
expr_stmt|;
name|ClusterHealthResponse
name|clusterHealth
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
name|clusterHealthRequest
argument_list|(
literal|"test"
argument_list|)
operator|.
name|waitForYellowStatus
argument_list|()
operator|.
name|waitForRelocatingShards
argument_list|(
literal|0
argument_list|)
operator|.
name|waitForActiveShards
argument_list|(
name|test
operator|.
name|totalNumShards
argument_list|)
argument_list|)
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"Done Cluster Health, status "
operator|+
name|clusterHealth
operator|.
name|getStatus
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|clusterHealth
operator|.
name|isTimedOut
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|clusterHealth
operator|.
name|getStatus
argument_list|()
argument_list|,
name|anyOf
argument_list|(
name|equalTo
argument_list|(
name|ClusterHealthStatus
operator|.
name|YELLOW
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|ClusterHealthStatus
operator|.
name|GREEN
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|clusterHealth
operator|.
name|getActiveShards
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|test
operator|.
name|totalNumShards
argument_list|)
argument_list|)
expr_stmt|;
name|refreshResponse
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
name|assertThat
argument_list|(
name|refreshResponse
operator|.
name|getTotalShards
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|test
operator|.
name|totalNumShards
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|refreshResponse
operator|.
name|getSuccessfulShards
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|test
operator|.
name|totalNumShards
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|refreshResponse
operator|.
name|getFailedShards
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
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
literal|5
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
name|client
argument_list|()
operator|.
name|search
argument_list|(
name|searchRequest
argument_list|(
literal|"test"
argument_list|)
operator|.
name|source
argument_list|(
operator|new
name|SearchSourceBuilder
argument_list|()
operator|.
name|query
argument_list|(
operator|new
name|GeohashCellQuery
operator|.
name|Builder
argument_list|(
literal|"foo"
argument_list|,
literal|"biz"
argument_list|)
argument_list|)
argument_list|)
argument_list|)
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|searchResponse
operator|.
name|getTotalShards
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|test
operator|.
name|numPrimaries
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|searchResponse
operator|.
name|getSuccessfulShards
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|searchResponse
operator|.
name|getFailedShards
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|test
operator|.
name|numPrimaries
argument_list|)
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"search should fail"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ElasticsearchException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|e
operator|.
name|unwrapCause
argument_list|()
argument_list|,
name|instanceOf
argument_list|(
name|SearchPhaseExecutionException
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
comment|// all is well
block|}
block|}
name|logger
operator|.
name|info
argument_list|(
literal|"Done Testing failed search"
argument_list|)
expr_stmt|;
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
throws|throws
name|IOException
block|{
name|client
operator|.
name|index
argument_list|(
name|Requests
operator|.
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
operator|.
name|consistencyLevel
argument_list|(
name|WriteConsistencyLevel
operator|.
name|ONE
argument_list|)
argument_list|)
operator|.
name|actionGet
argument_list|()
expr_stmt|;
block|}
DECL|method|source
specifier|private
name|XContentBuilder
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
throws|throws
name|IOException
block|{
name|StringBuilder
name|multi
init|=
operator|new
name|StringBuilder
argument_list|()
operator|.
name|append
argument_list|(
name|nameValue
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
name|age
condition|;
name|i
operator|++
control|)
block|{
name|multi
operator|.
name|append
argument_list|(
literal|" "
argument_list|)
operator|.
name|append
argument_list|(
name|nameValue
argument_list|)
expr_stmt|;
block|}
return|return
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
name|id
argument_list|)
operator|.
name|field
argument_list|(
literal|"name"
argument_list|,
name|nameValue
operator|+
name|id
argument_list|)
operator|.
name|field
argument_list|(
literal|"age"
argument_list|,
name|age
argument_list|)
operator|.
name|field
argument_list|(
literal|"multi"
argument_list|,
name|multi
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|endObject
argument_list|()
return|;
block|}
block|}
end_class

end_unit

