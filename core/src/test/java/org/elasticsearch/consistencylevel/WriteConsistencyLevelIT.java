begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.consistencylevel
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|consistencylevel
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
name|UnavailableShardsException
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
name|rest
operator|.
name|RestStatus
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
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|unit
operator|.
name|TimeValue
operator|.
name|timeValueMillis
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
name|unit
operator|.
name|TimeValue
operator|.
name|timeValueSeconds
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
comment|/**  *  */
end_comment

begin_class
DECL|class|WriteConsistencyLevelIT
specifier|public
class|class
name|WriteConsistencyLevelIT
extends|extends
name|ESIntegTestCase
block|{
DECL|method|testWriteConsistencyLevelReplication2
specifier|public
name|void
name|testWriteConsistencyLevelReplication2
parameter_list|()
throws|throws
name|Exception
block|{
name|prepareCreate
argument_list|(
literal|"test"
argument_list|,
literal|1
argument_list|,
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
literal|2
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
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
name|setWaitForActiveShards
argument_list|(
literal|1
argument_list|)
operator|.
name|setWaitForYellowStatus
argument_list|()
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"Done Cluster Health, status {}"
argument_list|,
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
name|equalTo
argument_list|(
name|ClusterHealthStatus
operator|.
name|YELLOW
argument_list|)
argument_list|)
expr_stmt|;
comment|// indexing, by default, will work (ONE consistency level)
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|(
name|source
argument_list|(
literal|"1"
argument_list|,
literal|"test"
argument_list|)
argument_list|)
operator|.
name|setConsistencyLevel
argument_list|(
name|WriteConsistencyLevel
operator|.
name|ONE
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
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|(
name|source
argument_list|(
literal|"1"
argument_list|,
literal|"test"
argument_list|)
argument_list|)
operator|.
name|setConsistencyLevel
argument_list|(
name|WriteConsistencyLevel
operator|.
name|QUORUM
argument_list|)
operator|.
name|setTimeout
argument_list|(
name|timeValueMillis
argument_list|(
literal|100
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|fail
argument_list|(
literal|"can't index, does not match consistency"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UnavailableShardsException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|e
operator|.
name|status
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|RestStatus
operator|.
name|SERVICE_UNAVAILABLE
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"[test][0] Not enough active copies to meet write consistency of [QUORUM] (have 1, needed 2). Timeout: [100ms], request: [index {[test][type1][1], source[{ type1 : { \"id\" : \"1\", \"name\" : \"test\" } }]}]"
argument_list|)
argument_list|)
expr_stmt|;
comment|// but really, all is well
block|}
name|allowNodes
argument_list|(
literal|"test"
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|clusterHealth
operator|=
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
name|setWaitForActiveShards
argument_list|(
literal|2
argument_list|)
operator|.
name|setWaitForYellowStatus
argument_list|()
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"Done Cluster Health, status {}"
argument_list|,
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
name|equalTo
argument_list|(
name|ClusterHealthStatus
operator|.
name|YELLOW
argument_list|)
argument_list|)
expr_stmt|;
comment|// this should work, since we now have
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|(
name|source
argument_list|(
literal|"1"
argument_list|,
literal|"test"
argument_list|)
argument_list|)
operator|.
name|setConsistencyLevel
argument_list|(
name|WriteConsistencyLevel
operator|.
name|QUORUM
argument_list|)
operator|.
name|setTimeout
argument_list|(
name|timeValueSeconds
argument_list|(
literal|1
argument_list|)
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
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|(
name|source
argument_list|(
literal|"1"
argument_list|,
literal|"test"
argument_list|)
argument_list|)
operator|.
name|setConsistencyLevel
argument_list|(
name|WriteConsistencyLevel
operator|.
name|ALL
argument_list|)
operator|.
name|setTimeout
argument_list|(
name|timeValueMillis
argument_list|(
literal|100
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|fail
argument_list|(
literal|"can't index, does not match consistency"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UnavailableShardsException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|e
operator|.
name|status
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|RestStatus
operator|.
name|SERVICE_UNAVAILABLE
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"[test][0] Not enough active copies to meet write consistency of [ALL] (have 2, needed 3). Timeout: [100ms], request: [index {[test][type1][1], source[{ type1 : { \"id\" : \"1\", \"name\" : \"test\" } }]}]"
argument_list|)
argument_list|)
expr_stmt|;
comment|// but really, all is well
block|}
name|allowNodes
argument_list|(
literal|"test"
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|clusterHealth
operator|=
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
name|setWaitForActiveShards
argument_list|(
literal|3
argument_list|)
operator|.
name|setWaitForGreenStatus
argument_list|()
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"Done Cluster Health, status {}"
argument_list|,
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
name|equalTo
argument_list|(
name|ClusterHealthStatus
operator|.
name|GREEN
argument_list|)
argument_list|)
expr_stmt|;
comment|// this should work, since we now have
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|(
name|source
argument_list|(
literal|"1"
argument_list|,
literal|"test"
argument_list|)
argument_list|)
operator|.
name|setConsistencyLevel
argument_list|(
name|WriteConsistencyLevel
operator|.
name|ALL
argument_list|)
operator|.
name|setTimeout
argument_list|(
name|timeValueSeconds
argument_list|(
literal|1
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
literal|"\" } }"
return|;
block|}
block|}
end_class

end_unit

