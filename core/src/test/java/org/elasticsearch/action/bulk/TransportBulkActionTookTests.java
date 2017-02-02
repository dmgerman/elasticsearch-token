begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.bulk
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|bulk
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
name|util
operator|.
name|Constants
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
name|IndicesRequest
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
name|create
operator|.
name|CreateIndexRequest
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
name|create
operator|.
name|CreateIndexResponse
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
name|create
operator|.
name|TransportCreateIndexAction
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
name|ClusterState
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
name|metadata
operator|.
name|MetaDataCreateIndexService
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
name|Strings
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
name|util
operator|.
name|concurrent
operator|.
name|AtomicArray
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
name|XContentType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|tasks
operator|.
name|Task
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
name|ESTestCase
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
name|transport
operator|.
name|CapturingTransport
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
name|TestThreadPool
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
name|Before
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
name|nio
operator|.
name|charset
operator|.
name|StandardCharsets
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
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
name|AtomicLong
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|function
operator|.
name|LongSupplier
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
name|ClusterServiceUtils
operator|.
name|createClusterService
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
name|StreamsUtils
operator|.
name|copyToStringFromClasspath
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
name|greaterThanOrEqualTo
import|;
end_import

begin_class
DECL|class|TransportBulkActionTookTests
specifier|public
class|class
name|TransportBulkActionTookTests
extends|extends
name|ESTestCase
block|{
DECL|field|threadPool
specifier|private
specifier|static
name|ThreadPool
name|threadPool
decl_stmt|;
DECL|field|clusterService
specifier|private
name|ClusterService
name|clusterService
decl_stmt|;
annotation|@
name|BeforeClass
DECL|method|beforeClass
specifier|public
specifier|static
name|void
name|beforeClass
parameter_list|()
block|{
name|threadPool
operator|=
operator|new
name|TestThreadPool
argument_list|(
literal|"TransportBulkActionTookTests"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
DECL|method|afterClass
specifier|public
specifier|static
name|void
name|afterClass
parameter_list|()
block|{
name|ThreadPool
operator|.
name|terminate
argument_list|(
name|threadPool
argument_list|,
literal|30
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
name|threadPool
operator|=
literal|null
expr_stmt|;
block|}
annotation|@
name|Before
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
name|clusterService
operator|=
name|createClusterService
argument_list|(
name|threadPool
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
block|}
DECL|method|createAction
specifier|private
name|TransportBulkAction
name|createAction
parameter_list|(
name|boolean
name|controlled
parameter_list|,
name|AtomicLong
name|expected
parameter_list|)
block|{
name|CapturingTransport
name|capturingTransport
init|=
operator|new
name|CapturingTransport
argument_list|()
decl_stmt|;
name|TransportService
name|transportService
init|=
operator|new
name|TransportService
argument_list|(
name|clusterService
operator|.
name|getSettings
argument_list|()
argument_list|,
name|capturingTransport
argument_list|,
name|threadPool
argument_list|,
name|TransportService
operator|.
name|NOOP_TRANSPORT_INTERCEPTOR
argument_list|,
name|boundAddress
lambda|->
name|clusterService
operator|.
name|localNode
argument_list|()
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|transportService
operator|.
name|start
argument_list|()
expr_stmt|;
name|transportService
operator|.
name|acceptIncomingRequests
argument_list|()
expr_stmt|;
name|IndexNameExpressionResolver
name|resolver
init|=
operator|new
name|Resolver
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
decl_stmt|;
name|ActionFilters
name|actionFilters
init|=
operator|new
name|ActionFilters
argument_list|(
operator|new
name|HashSet
argument_list|<>
argument_list|()
argument_list|)
decl_stmt|;
name|TransportCreateIndexAction
name|createIndexAction
init|=
operator|new
name|TransportCreateIndexAction
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
name|transportService
argument_list|,
name|clusterService
argument_list|,
name|threadPool
argument_list|,
literal|null
argument_list|,
name|actionFilters
argument_list|,
name|resolver
argument_list|)
decl_stmt|;
if|if
condition|(
name|controlled
condition|)
block|{
return|return
operator|new
name|TestTransportBulkAction
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
name|threadPool
argument_list|,
name|transportService
argument_list|,
name|clusterService
argument_list|,
literal|null
argument_list|,
name|createIndexAction
argument_list|,
name|actionFilters
argument_list|,
name|resolver
argument_list|,
literal|null
argument_list|,
name|expected
operator|::
name|get
argument_list|)
block|{
annotation|@
name|Override
name|void
name|executeBulk
parameter_list|(
name|Task
name|task
parameter_list|,
name|BulkRequest
name|bulkRequest
parameter_list|,
name|long
name|startTimeNanos
parameter_list|,
name|ActionListener
argument_list|<
name|BulkResponse
argument_list|>
name|listener
parameter_list|,
name|AtomicArray
argument_list|<
name|BulkItemResponse
argument_list|>
name|responses
parameter_list|)
block|{
name|expected
operator|.
name|set
argument_list|(
literal|1000000
argument_list|)
expr_stmt|;
name|super
operator|.
name|executeBulk
argument_list|(
name|task
argument_list|,
name|bulkRequest
argument_list|,
name|startTimeNanos
argument_list|,
name|listener
argument_list|,
name|responses
argument_list|)
expr_stmt|;
block|}
block|}
return|;
block|}
else|else
block|{
return|return
operator|new
name|TestTransportBulkAction
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
name|threadPool
argument_list|,
name|transportService
argument_list|,
name|clusterService
argument_list|,
literal|null
argument_list|,
name|createIndexAction
argument_list|,
name|actionFilters
argument_list|,
name|resolver
argument_list|,
literal|null
argument_list|,
name|System
operator|::
name|nanoTime
argument_list|)
block|{
annotation|@
name|Override
name|void
name|executeBulk
parameter_list|(
name|Task
name|task
parameter_list|,
name|BulkRequest
name|bulkRequest
parameter_list|,
name|long
name|startTimeNanos
parameter_list|,
name|ActionListener
argument_list|<
name|BulkResponse
argument_list|>
name|listener
parameter_list|,
name|AtomicArray
argument_list|<
name|BulkItemResponse
argument_list|>
name|responses
parameter_list|)
block|{
name|long
name|elapsed
init|=
name|spinForAtLeastOneMillisecond
argument_list|()
decl_stmt|;
name|expected
operator|.
name|set
argument_list|(
name|elapsed
argument_list|)
expr_stmt|;
name|super
operator|.
name|executeBulk
argument_list|(
name|task
argument_list|,
name|bulkRequest
argument_list|,
name|startTimeNanos
argument_list|,
name|listener
argument_list|,
name|responses
argument_list|)
expr_stmt|;
block|}
block|}
return|;
block|}
block|}
comment|// test unit conversion with a controlled clock
DECL|method|testTookWithControlledClock
specifier|public
name|void
name|testTookWithControlledClock
parameter_list|()
throws|throws
name|Exception
block|{
name|runTestTook
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
comment|// test took advances with System#nanoTime
DECL|method|testTookWithRealClock
specifier|public
name|void
name|testTookWithRealClock
parameter_list|()
throws|throws
name|Exception
block|{
name|runTestTook
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
DECL|method|runTestTook
specifier|private
name|void
name|runTestTook
parameter_list|(
name|boolean
name|controlled
parameter_list|)
throws|throws
name|Exception
block|{
name|String
name|bulkAction
init|=
name|copyToStringFromClasspath
argument_list|(
literal|"/org/elasticsearch/action/bulk/simple-bulk.json"
argument_list|)
decl_stmt|;
comment|// translate Windows line endings (\r\n) to standard ones (\n)
if|if
condition|(
name|Constants
operator|.
name|WINDOWS
condition|)
block|{
name|bulkAction
operator|=
name|Strings
operator|.
name|replace
argument_list|(
name|bulkAction
argument_list|,
literal|"\r\n"
argument_list|,
literal|"\n"
argument_list|)
expr_stmt|;
block|}
name|BulkRequest
name|bulkRequest
init|=
operator|new
name|BulkRequest
argument_list|()
decl_stmt|;
name|bulkRequest
operator|.
name|add
argument_list|(
name|bulkAction
operator|.
name|getBytes
argument_list|(
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
argument_list|,
literal|0
argument_list|,
name|bulkAction
operator|.
name|length
argument_list|()
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|XContentType
operator|.
name|JSON
argument_list|)
expr_stmt|;
name|AtomicLong
name|expected
init|=
operator|new
name|AtomicLong
argument_list|()
decl_stmt|;
name|TransportBulkAction
name|action
init|=
name|createAction
argument_list|(
name|controlled
argument_list|,
name|expected
argument_list|)
decl_stmt|;
name|action
operator|.
name|doExecute
argument_list|(
literal|null
argument_list|,
name|bulkRequest
argument_list|,
operator|new
name|ActionListener
argument_list|<
name|BulkResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onResponse
parameter_list|(
name|BulkResponse
name|bulkItemResponses
parameter_list|)
block|{
if|if
condition|(
name|controlled
condition|)
block|{
name|assertThat
argument_list|(
name|bulkItemResponses
operator|.
name|getTook
argument_list|()
operator|.
name|getMillis
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|TimeUnit
operator|.
name|MILLISECONDS
operator|.
name|convert
argument_list|(
name|expected
operator|.
name|get
argument_list|()
argument_list|,
name|TimeUnit
operator|.
name|NANOSECONDS
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertThat
argument_list|(
name|bulkItemResponses
operator|.
name|getTook
argument_list|()
operator|.
name|getMillis
argument_list|()
argument_list|,
name|greaterThanOrEqualTo
argument_list|(
name|TimeUnit
operator|.
name|MILLISECONDS
operator|.
name|convert
argument_list|(
name|expected
operator|.
name|get
argument_list|()
argument_list|,
name|TimeUnit
operator|.
name|NANOSECONDS
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|onFailure
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{              }
block|}
argument_list|)
expr_stmt|;
block|}
DECL|class|Resolver
specifier|static
class|class
name|Resolver
extends|extends
name|IndexNameExpressionResolver
block|{
DECL|method|Resolver
specifier|public
name|Resolver
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|concreteIndexNames
specifier|public
name|String
index|[]
name|concreteIndexNames
parameter_list|(
name|ClusterState
name|state
parameter_list|,
name|IndicesRequest
name|request
parameter_list|)
block|{
return|return
name|request
operator|.
name|indices
argument_list|()
return|;
block|}
block|}
DECL|class|TestTransportBulkAction
specifier|static
class|class
name|TestTransportBulkAction
extends|extends
name|TransportBulkAction
block|{
DECL|method|TestTransportBulkAction
specifier|public
name|TestTransportBulkAction
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|,
name|TransportService
name|transportService
parameter_list|,
name|ClusterService
name|clusterService
parameter_list|,
name|TransportShardBulkAction
name|shardBulkAction
parameter_list|,
name|TransportCreateIndexAction
name|createIndexAction
parameter_list|,
name|ActionFilters
name|actionFilters
parameter_list|,
name|IndexNameExpressionResolver
name|indexNameExpressionResolver
parameter_list|,
name|AutoCreateIndex
name|autoCreateIndex
parameter_list|,
name|LongSupplier
name|relativeTimeProvider
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|threadPool
argument_list|,
name|transportService
argument_list|,
name|clusterService
argument_list|,
literal|null
argument_list|,
name|shardBulkAction
argument_list|,
name|createIndexAction
argument_list|,
name|actionFilters
argument_list|,
name|indexNameExpressionResolver
argument_list|,
name|autoCreateIndex
argument_list|,
name|relativeTimeProvider
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|needToCheck
name|boolean
name|needToCheck
parameter_list|()
block|{
return|return
name|randomBoolean
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|shouldAutoCreate
name|boolean
name|shouldAutoCreate
parameter_list|(
name|String
name|index
parameter_list|,
name|ClusterState
name|state
parameter_list|)
block|{
return|return
name|randomBoolean
argument_list|()
return|;
block|}
block|}
DECL|class|TestTransportCreateIndexAction
specifier|static
class|class
name|TestTransportCreateIndexAction
extends|extends
name|TransportCreateIndexAction
block|{
DECL|method|TestTransportCreateIndexAction
specifier|public
name|TestTransportCreateIndexAction
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|TransportService
name|transportService
parameter_list|,
name|ClusterService
name|clusterService
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|,
name|MetaDataCreateIndexService
name|createIndexService
parameter_list|,
name|ActionFilters
name|actionFilters
parameter_list|,
name|IndexNameExpressionResolver
name|indexNameExpressionResolver
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|transportService
argument_list|,
name|clusterService
argument_list|,
name|threadPool
argument_list|,
name|createIndexService
argument_list|,
name|actionFilters
argument_list|,
name|indexNameExpressionResolver
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doExecute
specifier|protected
name|void
name|doExecute
parameter_list|(
name|Task
name|task
parameter_list|,
name|CreateIndexRequest
name|request
parameter_list|,
name|ActionListener
argument_list|<
name|CreateIndexResponse
argument_list|>
name|listener
parameter_list|)
block|{
name|listener
operator|.
name|onResponse
argument_list|(
name|newResponse
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

