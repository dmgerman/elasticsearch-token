begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.transport
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|transport
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
name|collect
operator|.
name|ImmutableMap
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|Version
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
name|repositories
operator|.
name|verify
operator|.
name|VerifyRepositoryAction
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
name|get
operator|.
name|GetIndexAction
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
name|bench
operator|.
name|AbortBenchmarkAction
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
name|bench
operator|.
name|BenchmarkAction
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
name|bench
operator|.
name|BenchmarkService
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
name|bench
operator|.
name|BenchmarkStatusAction
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
name|exists
operator|.
name|ExistsAction
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
name|indexedscripts
operator|.
name|delete
operator|.
name|DeleteIndexedScriptAction
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
name|indexedscripts
operator|.
name|get
operator|.
name|GetIndexedScriptAction
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
name|indexedscripts
operator|.
name|put
operator|.
name|PutIndexedScriptAction
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
name|node
operator|.
name|DiscoveryNode
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
name|node
operator|.
name|DiscoveryNodes
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
name|store
operator|.
name|IndicesStore
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
name|action
operator|.
name|SearchServiceTransportAction
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|repositories
operator|.
name|VerifyNodeRepositoryAction
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
name|ElasticsearchBackwardsCompatIntegrationTest
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
name|InternalTestCluster
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|threadpool
operator|.
name|ThreadPool
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
name|HashMap
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
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|CountDownLatch
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
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicReference
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|CoreMatchers
operator|.
name|*
import|;
end_import

begin_class
DECL|class|ActionNamesBackwardsCompatibilityTest
specifier|public
class|class
name|ActionNamesBackwardsCompatibilityTest
extends|extends
name|ElasticsearchBackwardsCompatIntegrationTest
block|{
annotation|@
name|Test
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|method|testTransportHandlers
specifier|public
name|void
name|testTransportHandlers
parameter_list|()
throws|throws
name|NoSuchFieldException
throws|,
name|IllegalAccessException
throws|,
name|InterruptedException
block|{
name|InternalTestCluster
name|internalCluster
init|=
name|backwardsCluster
argument_list|()
operator|.
name|internalCluster
argument_list|()
decl_stmt|;
name|TransportService
name|transportService
init|=
name|internalCluster
operator|.
name|getInstance
argument_list|(
name|TransportService
operator|.
name|class
argument_list|)
decl_stmt|;
name|ImmutableMap
argument_list|<
name|String
argument_list|,
name|TransportRequestHandler
argument_list|>
name|requestHandlers
init|=
name|transportService
operator|.
name|serverHandlers
decl_stmt|;
name|DiscoveryNodes
name|nodes
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
name|prepareState
argument_list|()
operator|.
name|get
argument_list|()
operator|.
name|getState
argument_list|()
operator|.
name|nodes
argument_list|()
decl_stmt|;
name|DiscoveryNode
name|selectedNode
init|=
literal|null
decl_stmt|;
for|for
control|(
name|DiscoveryNode
name|node
range|:
name|nodes
control|)
block|{
if|if
condition|(
name|node
operator|.
name|getVersion
argument_list|()
operator|.
name|before
argument_list|(
name|Version
operator|.
name|CURRENT
argument_list|)
condition|)
block|{
name|selectedNode
operator|=
name|node
expr_stmt|;
break|break;
block|}
block|}
name|assertThat
argument_list|(
name|selectedNode
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
specifier|final
name|TransportRequest
name|transportRequest
init|=
operator|new
name|TransportRequest
argument_list|()
block|{}
decl_stmt|;
for|for
control|(
name|String
name|action
range|:
name|requestHandlers
operator|.
name|keySet
argument_list|()
control|)
block|{
specifier|final
name|CountDownLatch
name|latch
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
specifier|final
name|AtomicReference
argument_list|<
name|TransportException
argument_list|>
name|failure
init|=
operator|new
name|AtomicReference
argument_list|<>
argument_list|()
decl_stmt|;
name|transportService
operator|.
name|sendRequest
argument_list|(
name|selectedNode
argument_list|,
name|action
argument_list|,
name|transportRequest
argument_list|,
operator|new
name|TransportResponseHandler
argument_list|<
name|TransportResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|TransportResponse
name|newInstance
parameter_list|()
block|{
return|return
operator|new
name|TransportResponse
argument_list|()
block|{}
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|handleResponse
parameter_list|(
name|TransportResponse
name|response
parameter_list|)
block|{
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|handleException
parameter_list|(
name|TransportException
name|exp
parameter_list|)
block|{
name|failure
operator|.
name|set
argument_list|(
name|exp
argument_list|)
expr_stmt|;
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|executor
parameter_list|()
block|{
return|return
name|ThreadPool
operator|.
name|Names
operator|.
name|SAME
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|latch
operator|.
name|await
argument_list|(
literal|5
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|failure
operator|.
name|get
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|Throwable
name|cause
init|=
name|failure
operator|.
name|get
argument_list|()
operator|.
name|unwrapCause
argument_list|()
decl_stmt|;
if|if
condition|(
name|isActionNotFoundExpected
argument_list|(
name|selectedNode
operator|.
name|version
argument_list|()
argument_list|,
name|action
argument_list|)
condition|)
block|{
name|assertThat
argument_list|(
name|cause
argument_list|,
name|instanceOf
argument_list|(
name|ActionNotFoundTransportException
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertThat
argument_list|(
name|cause
argument_list|,
name|not
argument_list|(
name|instanceOf
argument_list|(
name|ActionNotFoundTransportException
operator|.
name|class
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
operator|(
name|cause
operator|instanceof
name|IndexOutOfBoundsException
operator|)
condition|)
block|{
name|cause
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
DECL|method|isActionNotFoundExpected
specifier|private
specifier|static
name|boolean
name|isActionNotFoundExpected
parameter_list|(
name|Version
name|version
parameter_list|,
name|String
name|action
parameter_list|)
block|{
name|Version
name|actionVersion
init|=
name|actionsVersions
operator|.
name|get
argument_list|(
name|action
argument_list|)
decl_stmt|;
return|return
name|actionVersion
operator|!=
literal|null
operator|&&
name|version
operator|.
name|before
argument_list|(
name|actionVersion
argument_list|)
return|;
block|}
DECL|field|actionsVersions
specifier|private
specifier|static
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Version
argument_list|>
name|actionsVersions
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
static|static
block|{
name|actionsVersions
operator|.
name|put
argument_list|(
name|BenchmarkService
operator|.
name|STATUS_ACTION_NAME
argument_list|,
name|Version
operator|.
name|V_1_4_0_Beta1
argument_list|)
expr_stmt|;
name|actionsVersions
operator|.
name|put
argument_list|(
name|BenchmarkService
operator|.
name|START_ACTION_NAME
argument_list|,
name|Version
operator|.
name|V_1_4_0_Beta1
argument_list|)
expr_stmt|;
name|actionsVersions
operator|.
name|put
argument_list|(
name|BenchmarkService
operator|.
name|ABORT_ACTION_NAME
argument_list|,
name|Version
operator|.
name|V_1_4_0_Beta1
argument_list|)
expr_stmt|;
name|actionsVersions
operator|.
name|put
argument_list|(
name|BenchmarkAction
operator|.
name|NAME
argument_list|,
name|Version
operator|.
name|V_1_4_0_Beta1
argument_list|)
expr_stmt|;
name|actionsVersions
operator|.
name|put
argument_list|(
name|BenchmarkStatusAction
operator|.
name|NAME
argument_list|,
name|Version
operator|.
name|V_1_4_0_Beta1
argument_list|)
expr_stmt|;
name|actionsVersions
operator|.
name|put
argument_list|(
name|AbortBenchmarkAction
operator|.
name|NAME
argument_list|,
name|Version
operator|.
name|V_1_4_0_Beta1
argument_list|)
expr_stmt|;
name|actionsVersions
operator|.
name|put
argument_list|(
name|GetIndexAction
operator|.
name|NAME
argument_list|,
name|Version
operator|.
name|V_1_4_0_Beta1
argument_list|)
expr_stmt|;
name|actionsVersions
operator|.
name|put
argument_list|(
name|ExistsAction
operator|.
name|NAME
argument_list|,
name|Version
operator|.
name|V_1_4_0_Beta1
argument_list|)
expr_stmt|;
name|actionsVersions
operator|.
name|put
argument_list|(
name|ExistsAction
operator|.
name|NAME
operator|+
literal|"[s]"
argument_list|,
name|Version
operator|.
name|V_1_4_0_Beta1
argument_list|)
expr_stmt|;
name|actionsVersions
operator|.
name|put
argument_list|(
name|IndicesStore
operator|.
name|ACTION_SHARD_EXISTS
argument_list|,
name|Version
operator|.
name|V_1_3_0
argument_list|)
expr_stmt|;
name|actionsVersions
operator|.
name|put
argument_list|(
name|GetIndexedScriptAction
operator|.
name|NAME
argument_list|,
name|Version
operator|.
name|V_1_3_0
argument_list|)
expr_stmt|;
name|actionsVersions
operator|.
name|put
argument_list|(
name|DeleteIndexedScriptAction
operator|.
name|NAME
argument_list|,
name|Version
operator|.
name|V_1_3_0
argument_list|)
expr_stmt|;
name|actionsVersions
operator|.
name|put
argument_list|(
name|PutIndexedScriptAction
operator|.
name|NAME
argument_list|,
name|Version
operator|.
name|V_1_3_0
argument_list|)
expr_stmt|;
name|actionsVersions
operator|.
name|put
argument_list|(
name|SearchServiceTransportAction
operator|.
name|FREE_CONTEXT_SCROLL_ACTION_NAME
argument_list|,
name|Version
operator|.
name|V_1_4_0_Beta1
argument_list|)
expr_stmt|;
name|actionsVersions
operator|.
name|put
argument_list|(
name|SearchServiceTransportAction
operator|.
name|FETCH_ID_SCROLL_ACTION_NAME
argument_list|,
name|Version
operator|.
name|V_1_4_0_Beta1
argument_list|)
expr_stmt|;
name|actionsVersions
operator|.
name|put
argument_list|(
name|VerifyRepositoryAction
operator|.
name|NAME
argument_list|,
name|Version
operator|.
name|V_1_4_0
argument_list|)
expr_stmt|;
name|actionsVersions
operator|.
name|put
argument_list|(
name|VerifyNodeRepositoryAction
operator|.
name|ACTION_NAME
argument_list|,
name|Version
operator|.
name|V_1_4_0
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

