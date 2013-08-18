begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_package
DECL|package|org.elasticsearch.test.integration.threadpool
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|integration
operator|.
name|threadpool
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
name|node
operator|.
name|info
operator|.
name|NodeInfo
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
name|info
operator|.
name|NodesInfoResponse
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
name|settings
operator|.
name|ImmutableSettings
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
name|ToXContent
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
name|common
operator|.
name|xcontent
operator|.
name|XContentParser
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
name|json
operator|.
name|JsonXContent
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
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|integration
operator|.
name|AbstractNodesTests
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
name|threadpool
operator|.
name|ThreadPool
operator|.
name|Names
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
name|ThreadPoolInfo
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
name|*
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
name|hamcrest
operator|.
name|Matchers
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|SimpleThreadPoolTests
specifier|public
class|class
name|SimpleThreadPoolTests
extends|extends
name|AbstractNodesTests
block|{
DECL|field|client1
specifier|private
name|Client
name|client1
decl_stmt|;
DECL|field|client2
specifier|private
name|Client
name|client2
decl_stmt|;
DECL|field|threadPool
specifier|private
name|ThreadPool
name|threadPool
decl_stmt|;
annotation|@
name|Override
DECL|method|beforeClass
specifier|protected
name|void
name|beforeClass
parameter_list|()
block|{
name|startNode
argument_list|(
literal|"node1"
argument_list|,
name|ImmutableSettings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"threadpool.search.type"
argument_list|,
literal|"cached"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|startNode
argument_list|(
literal|"node2"
argument_list|,
name|ImmutableSettings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"threadpool.search.type"
argument_list|,
literal|"cached"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|client1
operator|=
name|client
argument_list|(
literal|"node1"
argument_list|)
expr_stmt|;
name|client2
operator|=
name|client
argument_list|(
literal|"node2"
argument_list|)
expr_stmt|;
name|threadPool
operator|=
operator|(
operator|(
name|InternalNode
operator|)
name|node
argument_list|(
literal|"node1"
argument_list|)
operator|)
operator|.
name|injector
argument_list|()
operator|.
name|getInstance
argument_list|(
name|ThreadPool
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|20000
argument_list|)
DECL|method|testUpdatingThreadPoolSettings
specifier|public
name|void
name|testUpdatingThreadPoolSettings
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Check that settings are changed
name|assertThat
argument_list|(
operator|(
operator|(
name|ThreadPoolExecutor
operator|)
name|threadPool
operator|.
name|executor
argument_list|(
name|Names
operator|.
name|SEARCH
argument_list|)
operator|)
operator|.
name|getKeepAliveTime
argument_list|(
name|TimeUnit
operator|.
name|MINUTES
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|5L
argument_list|)
argument_list|)
expr_stmt|;
name|client1
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareUpdateSettings
argument_list|()
operator|.
name|setTransientSettings
argument_list|(
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"threadpool.search.keep_alive"
argument_list|,
literal|"10m"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|ThreadPoolExecutor
operator|)
name|threadPool
operator|.
name|executor
argument_list|(
name|Names
operator|.
name|SEARCH
argument_list|)
operator|)
operator|.
name|getKeepAliveTime
argument_list|(
name|TimeUnit
operator|.
name|MINUTES
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|10L
argument_list|)
argument_list|)
expr_stmt|;
comment|// Make sure that threads continue executing when executor is replaced
specifier|final
name|CyclicBarrier
name|barrier
init|=
operator|new
name|CyclicBarrier
argument_list|(
literal|2
argument_list|)
decl_stmt|;
name|Executor
name|oldExecutor
init|=
name|threadPool
operator|.
name|executor
argument_list|(
name|Names
operator|.
name|SEARCH
argument_list|)
decl_stmt|;
name|threadPool
operator|.
name|executor
argument_list|(
name|Names
operator|.
name|SEARCH
argument_list|)
operator|.
name|execute
argument_list|(
operator|new
name|Runnable
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
name|barrier
operator|.
name|await
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ex
parameter_list|)
block|{
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|interrupt
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|BrokenBarrierException
name|ex
parameter_list|)
block|{
comment|//
block|}
block|}
block|}
argument_list|)
expr_stmt|;
name|client1
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareUpdateSettings
argument_list|()
operator|.
name|setTransientSettings
argument_list|(
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"threadpool.search.type"
argument_list|,
literal|"fixed"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|threadPool
operator|.
name|executor
argument_list|(
name|Names
operator|.
name|SEARCH
argument_list|)
argument_list|,
name|not
argument_list|(
name|sameInstance
argument_list|(
name|oldExecutor
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|ThreadPoolExecutor
operator|)
name|oldExecutor
operator|)
operator|.
name|isShutdown
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|ThreadPoolExecutor
operator|)
name|oldExecutor
operator|)
operator|.
name|isTerminating
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|ThreadPoolExecutor
operator|)
name|oldExecutor
operator|)
operator|.
name|isTerminated
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|barrier
operator|.
name|await
argument_list|()
expr_stmt|;
comment|// Make sure that new thread executor is functional
name|threadPool
operator|.
name|executor
argument_list|(
name|Names
operator|.
name|SEARCH
argument_list|)
operator|.
name|execute
argument_list|(
operator|new
name|Runnable
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
name|barrier
operator|.
name|await
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ex
parameter_list|)
block|{
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|interrupt
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|BrokenBarrierException
name|ex
parameter_list|)
block|{
comment|//
block|}
block|}
block|}
argument_list|)
expr_stmt|;
name|client1
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareUpdateSettings
argument_list|()
operator|.
name|setTransientSettings
argument_list|(
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"threadpool.search.type"
argument_list|,
literal|"fixed"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|barrier
operator|.
name|await
argument_list|()
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|200
argument_list|)
expr_stmt|;
comment|// Check that node info is correct
name|NodesInfoResponse
name|nodesInfoResponse
init|=
name|client2
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareNodesInfo
argument_list|()
operator|.
name|all
argument_list|()
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
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
literal|2
condition|;
name|i
operator|++
control|)
block|{
name|NodeInfo
name|nodeInfo
init|=
name|nodesInfoResponse
operator|.
name|getNodes
argument_list|()
index|[
name|i
index|]
decl_stmt|;
name|boolean
name|found
init|=
literal|false
decl_stmt|;
for|for
control|(
name|ThreadPool
operator|.
name|Info
name|info
range|:
name|nodeInfo
operator|.
name|getThreadPool
argument_list|()
control|)
block|{
if|if
condition|(
name|info
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
name|Names
operator|.
name|SEARCH
argument_list|)
condition|)
block|{
name|assertThat
argument_list|(
name|info
operator|.
name|getType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"fixed"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|info
operator|.
name|getQueueType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"linked"
argument_list|)
argument_list|)
expr_stmt|;
name|found
operator|=
literal|true
expr_stmt|;
break|break;
block|}
block|}
name|assertThat
argument_list|(
name|found
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|poolMap
init|=
name|getPoolSettingsThroughJson
argument_list|(
name|nodeInfo
operator|.
name|getThreadPool
argument_list|()
argument_list|,
name|Names
operator|.
name|SEARCH
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|poolMap
operator|.
name|get
argument_list|(
literal|"queue_type"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"linked"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|getPoolSettingsThroughJson
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|getPoolSettingsThroughJson
parameter_list|(
name|ThreadPoolInfo
name|info
parameter_list|,
name|String
name|poolName
parameter_list|)
throws|throws
name|IOException
block|{
name|XContentBuilder
name|builder
init|=
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|info
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|ToXContent
operator|.
name|EMPTY_PARAMS
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|close
argument_list|()
expr_stmt|;
name|XContentParser
name|parser
init|=
name|JsonXContent
operator|.
name|jsonXContent
operator|.
name|createParser
argument_list|(
name|builder
operator|.
name|string
argument_list|()
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|poolsMap
init|=
name|parser
operator|.
name|mapAndClose
argument_list|()
decl_stmt|;
return|return
call|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
call|)
argument_list|(
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|poolsMap
operator|.
name|get
argument_list|(
literal|"thread_pool"
argument_list|)
argument_list|)
operator|.
name|get
argument_list|(
name|poolName
argument_list|)
return|;
block|}
block|}
end_class

end_unit

