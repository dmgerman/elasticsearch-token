begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.mapper
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
package|;
end_package

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
name|ActionListener
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
name|IndexRequest
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
name|IndexResponse
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
name|TransportIndexAction
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
name|support
operator|.
name|ActionFilters
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
name|support
operator|.
name|AutoCreateIndex
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
name|action
operator|.
name|shard
operator|.
name|ShardStateAction
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
name|metadata
operator|.
name|IndexNameExpressionResolver
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
name|service
operator|.
name|ClusterService
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
name|io
operator|.
name|stream
operator|.
name|NamedWriteableRegistry
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
name|index
operator|.
name|IndexNotFoundException
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
name|test
operator|.
name|ESSingleNodeTestCase
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
name|elasticsearch
operator|.
name|transport
operator|.
name|TransportService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|transport
operator|.
name|local
operator|.
name|LocalTransport
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|After
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|AfterClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|BeforeClass
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
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
name|AtomicBoolean
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
name|service
operator|.
name|ClusterServiceUtils
operator|.
name|createClusterService
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
name|instanceOf
import|;
end_import

begin_class
DECL|class|DynamicMappingDisabledTests
specifier|public
class|class
name|DynamicMappingDisabledTests
extends|extends
name|ESSingleNodeTestCase
block|{
DECL|field|THREAD_POOL
specifier|private
specifier|static
name|ThreadPool
name|THREAD_POOL
decl_stmt|;
DECL|field|clusterService
specifier|private
name|ClusterService
name|clusterService
decl_stmt|;
DECL|field|transport
specifier|private
name|LocalTransport
name|transport
decl_stmt|;
DECL|field|transportService
specifier|private
name|TransportService
name|transportService
decl_stmt|;
DECL|field|indicesService
specifier|private
name|IndicesService
name|indicesService
decl_stmt|;
DECL|field|shardStateAction
specifier|private
name|ShardStateAction
name|shardStateAction
decl_stmt|;
DECL|field|actionFilters
specifier|private
name|ActionFilters
name|actionFilters
decl_stmt|;
DECL|field|indexNameExpressionResolver
specifier|private
name|IndexNameExpressionResolver
name|indexNameExpressionResolver
decl_stmt|;
DECL|field|autoCreateIndex
specifier|private
name|AutoCreateIndex
name|autoCreateIndex
decl_stmt|;
DECL|field|settings
specifier|private
name|Settings
name|settings
decl_stmt|;
annotation|@
name|BeforeClass
DECL|method|createThreadPool
specifier|public
specifier|static
name|void
name|createThreadPool
parameter_list|()
block|{
name|THREAD_POOL
operator|=
operator|new
name|ThreadPool
argument_list|(
literal|"DynamicMappingDisabledTests"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|setUp
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|setUp
argument_list|()
expr_stmt|;
name|settings
operator|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|MapperService
operator|.
name|INDEX_MAPPER_DYNAMIC_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
literal|false
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|clusterService
operator|=
name|createClusterService
argument_list|(
name|THREAD_POOL
argument_list|)
expr_stmt|;
name|transport
operator|=
operator|new
name|LocalTransport
argument_list|(
name|settings
argument_list|,
name|THREAD_POOL
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|,
operator|new
name|NamedWriteableRegistry
argument_list|()
argument_list|)
expr_stmt|;
name|transportService
operator|=
operator|new
name|TransportService
argument_list|(
name|transport
argument_list|,
name|THREAD_POOL
argument_list|)
expr_stmt|;
name|indicesService
operator|=
name|getInstanceFromNode
argument_list|(
name|IndicesService
operator|.
name|class
argument_list|)
expr_stmt|;
name|shardStateAction
operator|=
operator|new
name|ShardStateAction
argument_list|(
name|settings
argument_list|,
name|clusterService
argument_list|,
name|transportService
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|THREAD_POOL
argument_list|)
expr_stmt|;
name|actionFilters
operator|=
operator|new
name|ActionFilters
argument_list|(
name|Collections
operator|.
name|emptySet
argument_list|()
argument_list|)
expr_stmt|;
name|indexNameExpressionResolver
operator|=
operator|new
name|IndexNameExpressionResolver
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|autoCreateIndex
operator|=
operator|new
name|AutoCreateIndex
argument_list|(
name|settings
argument_list|,
name|indexNameExpressionResolver
argument_list|)
expr_stmt|;
block|}
annotation|@
name|After
DECL|method|tearDown
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|tearDown
argument_list|()
expr_stmt|;
name|clusterService
operator|.
name|close
argument_list|()
expr_stmt|;
name|transportService
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|AfterClass
DECL|method|destroyThreadPool
specifier|public
specifier|static
name|void
name|destroyThreadPool
parameter_list|()
block|{
name|ThreadPool
operator|.
name|terminate
argument_list|(
name|THREAD_POOL
argument_list|,
literal|30
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
comment|// since static must set to null to be eligible for collection
name|THREAD_POOL
operator|=
literal|null
expr_stmt|;
block|}
DECL|method|testDynamicDisabled
specifier|public
name|void
name|testDynamicDisabled
parameter_list|()
block|{
name|TransportIndexAction
name|action
init|=
operator|new
name|TransportIndexAction
argument_list|(
name|settings
argument_list|,
name|transportService
argument_list|,
name|clusterService
argument_list|,
name|indicesService
argument_list|,
name|THREAD_POOL
argument_list|,
name|shardStateAction
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|actionFilters
argument_list|,
name|indexNameExpressionResolver
argument_list|,
name|autoCreateIndex
argument_list|)
decl_stmt|;
name|IndexRequest
name|request
init|=
operator|new
name|IndexRequest
argument_list|(
literal|"index"
argument_list|,
literal|"type"
argument_list|,
literal|"1"
argument_list|)
decl_stmt|;
name|request
operator|.
name|source
argument_list|(
literal|"foo"
argument_list|,
literal|3
argument_list|)
expr_stmt|;
specifier|final
name|AtomicBoolean
name|onFailureCalled
init|=
operator|new
name|AtomicBoolean
argument_list|()
decl_stmt|;
name|action
operator|.
name|execute
argument_list|(
name|request
argument_list|,
operator|new
name|ActionListener
argument_list|<
name|IndexResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onResponse
parameter_list|(
name|IndexResponse
name|indexResponse
parameter_list|)
block|{
name|fail
argument_list|(
literal|"Indexing request should have failed"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|onFailure
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|onFailureCalled
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|e
argument_list|,
name|instanceOf
argument_list|(
name|IndexNotFoundException
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
literal|"no such index"
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|onFailureCalled
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

