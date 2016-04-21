begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.cluster.node.tasks
package|package
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
name|tasks
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
name|Action
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
name|ActionModule
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
name|ActionRequestBuilder
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
name|FailedNodeException
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
name|TaskOperationFailure
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
name|nodes
operator|.
name|BaseNodeRequest
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
name|nodes
operator|.
name|BaseNodeResponse
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
name|nodes
operator|.
name|BaseNodesRequest
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
name|nodes
operator|.
name|BaseNodesResponse
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
name|nodes
operator|.
name|TransportNodesAction
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
name|tasks
operator|.
name|BaseTasksRequest
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
name|tasks
operator|.
name|BaseTasksResponse
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
name|tasks
operator|.
name|TransportTasksAction
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
name|ElasticsearchClient
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
name|ClusterName
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
name|inject
operator|.
name|Inject
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
name|StreamInput
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
name|StreamOutput
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
name|Writeable
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
name|tasks
operator|.
name|CancellableTask
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
name|tasks
operator|.
name|TaskId
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
name|ArrayList
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
name|HashSet
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
name|atomic
operator|.
name|AtomicReferenceArray
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
name|ESTestCase
operator|.
name|awaitBusy
import|;
end_import

begin_comment
comment|/**  * A plugin that adds a cancellable blocking test task of integration testing of the task manager.  */
end_comment

begin_class
DECL|class|TestTaskPlugin
specifier|public
class|class
name|TestTaskPlugin
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
literal|"test-task-plugin"
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
literal|"Test plugin for testing task management"
return|;
block|}
DECL|method|onModule
specifier|public
name|void
name|onModule
parameter_list|(
name|ActionModule
name|module
parameter_list|)
block|{
name|module
operator|.
name|registerAction
argument_list|(
name|TestTaskAction
operator|.
name|INSTANCE
argument_list|,
name|TransportTestTaskAction
operator|.
name|class
argument_list|)
expr_stmt|;
name|module
operator|.
name|registerAction
argument_list|(
name|UnblockTestTasksAction
operator|.
name|INSTANCE
argument_list|,
name|TransportUnblockTestTasksAction
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
DECL|class|TestTask
specifier|static
class|class
name|TestTask
extends|extends
name|CancellableTask
block|{
DECL|field|blocked
specifier|private
specifier|volatile
name|boolean
name|blocked
init|=
literal|true
decl_stmt|;
DECL|method|TestTask
specifier|public
name|TestTask
parameter_list|(
name|long
name|id
parameter_list|,
name|String
name|type
parameter_list|,
name|String
name|action
parameter_list|,
name|String
name|description
parameter_list|,
name|TaskId
name|parentTaskId
parameter_list|)
block|{
name|super
argument_list|(
name|id
argument_list|,
name|type
argument_list|,
name|action
argument_list|,
name|description
argument_list|,
name|parentTaskId
argument_list|)
expr_stmt|;
block|}
DECL|method|isBlocked
specifier|public
name|boolean
name|isBlocked
parameter_list|()
block|{
return|return
name|blocked
return|;
block|}
DECL|method|unblock
specifier|public
name|void
name|unblock
parameter_list|()
block|{
name|blocked
operator|=
literal|false
expr_stmt|;
block|}
block|}
DECL|class|NodeResponse
specifier|public
specifier|static
class|class
name|NodeResponse
extends|extends
name|BaseNodeResponse
block|{
DECL|method|NodeResponse
specifier|protected
name|NodeResponse
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
DECL|method|NodeResponse
specifier|public
name|NodeResponse
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|)
block|{
name|super
argument_list|(
name|node
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|NodesResponse
specifier|public
specifier|static
class|class
name|NodesResponse
extends|extends
name|BaseNodesResponse
argument_list|<
name|NodeResponse
argument_list|>
block|{
DECL|field|failureCount
specifier|private
name|int
name|failureCount
decl_stmt|;
DECL|method|NodesResponse
name|NodesResponse
parameter_list|()
block|{          }
DECL|method|NodesResponse
specifier|public
name|NodesResponse
parameter_list|(
name|ClusterName
name|clusterName
parameter_list|,
name|NodeResponse
index|[]
name|nodes
parameter_list|,
name|int
name|failureCount
parameter_list|)
block|{
name|super
argument_list|(
name|clusterName
argument_list|,
name|nodes
argument_list|)
expr_stmt|;
name|this
operator|.
name|failureCount
operator|=
name|failureCount
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|readFrom
specifier|public
name|void
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|failureCount
operator|=
name|in
operator|.
name|readVInt
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|writeTo
specifier|public
name|void
name|writeTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|failureCount
argument_list|)
expr_stmt|;
block|}
DECL|method|failureCount
specifier|public
name|int
name|failureCount
parameter_list|()
block|{
return|return
name|failureCount
return|;
block|}
block|}
DECL|class|NodeRequest
specifier|public
specifier|static
class|class
name|NodeRequest
extends|extends
name|BaseNodeRequest
block|{
DECL|field|requestName
specifier|protected
name|String
name|requestName
decl_stmt|;
DECL|field|nodeId
specifier|protected
name|String
name|nodeId
decl_stmt|;
DECL|method|NodeRequest
specifier|public
name|NodeRequest
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
DECL|method|NodeRequest
specifier|public
name|NodeRequest
parameter_list|(
name|NodesRequest
name|request
parameter_list|,
name|String
name|nodeId
parameter_list|)
block|{
name|super
argument_list|(
name|nodeId
argument_list|)
expr_stmt|;
name|requestName
operator|=
name|request
operator|.
name|requestName
expr_stmt|;
name|this
operator|.
name|nodeId
operator|=
name|nodeId
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|readFrom
specifier|public
name|void
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|requestName
operator|=
name|in
operator|.
name|readString
argument_list|()
expr_stmt|;
name|nodeId
operator|=
name|in
operator|.
name|readString
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|writeTo
specifier|public
name|void
name|writeTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeString
argument_list|(
name|requestName
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeString
argument_list|(
name|nodeId
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getDescription
specifier|public
name|String
name|getDescription
parameter_list|()
block|{
return|return
literal|"NodeRequest["
operator|+
name|requestName
operator|+
literal|", "
operator|+
name|nodeId
operator|+
literal|"]"
return|;
block|}
annotation|@
name|Override
DECL|method|createTask
specifier|public
name|Task
name|createTask
parameter_list|(
name|long
name|id
parameter_list|,
name|String
name|type
parameter_list|,
name|String
name|action
parameter_list|,
name|TaskId
name|parentTaskId
parameter_list|)
block|{
return|return
operator|new
name|TestTask
argument_list|(
name|id
argument_list|,
name|type
argument_list|,
name|action
argument_list|,
name|this
operator|.
name|getDescription
argument_list|()
argument_list|,
name|parentTaskId
argument_list|)
return|;
block|}
block|}
DECL|class|NodesRequest
specifier|public
specifier|static
class|class
name|NodesRequest
extends|extends
name|BaseNodesRequest
argument_list|<
name|NodesRequest
argument_list|>
block|{
DECL|field|requestName
specifier|private
name|String
name|requestName
decl_stmt|;
DECL|method|NodesRequest
name|NodesRequest
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
DECL|method|NodesRequest
specifier|public
name|NodesRequest
parameter_list|(
name|String
name|requestName
parameter_list|,
name|String
modifier|...
name|nodesIds
parameter_list|)
block|{
name|super
argument_list|(
name|nodesIds
argument_list|)
expr_stmt|;
name|this
operator|.
name|requestName
operator|=
name|requestName
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|readFrom
specifier|public
name|void
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|requestName
operator|=
name|in
operator|.
name|readString
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|writeTo
specifier|public
name|void
name|writeTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeString
argument_list|(
name|requestName
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getDescription
specifier|public
name|String
name|getDescription
parameter_list|()
block|{
return|return
literal|"NodesRequest["
operator|+
name|requestName
operator|+
literal|"]"
return|;
block|}
annotation|@
name|Override
DECL|method|createTask
specifier|public
name|Task
name|createTask
parameter_list|(
name|long
name|id
parameter_list|,
name|String
name|type
parameter_list|,
name|String
name|action
parameter_list|)
block|{
return|return
operator|new
name|CancellableTask
argument_list|(
name|id
argument_list|,
name|type
argument_list|,
name|action
argument_list|,
name|getDescription
argument_list|()
argument_list|)
return|;
block|}
block|}
DECL|class|TransportTestTaskAction
specifier|public
specifier|static
class|class
name|TransportTestTaskAction
extends|extends
name|TransportNodesAction
argument_list|<
name|NodesRequest
argument_list|,
name|NodesResponse
argument_list|,
name|NodeRequest
argument_list|,
name|NodeResponse
argument_list|>
block|{
annotation|@
name|Inject
DECL|method|TransportTestTaskAction
specifier|public
name|TransportTestTaskAction
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ClusterName
name|clusterName
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|,
name|ClusterService
name|clusterService
parameter_list|,
name|TransportService
name|transportService
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|TestTaskAction
operator|.
name|NAME
argument_list|,
name|clusterName
argument_list|,
name|threadPool
argument_list|,
name|clusterService
argument_list|,
name|transportService
argument_list|,
operator|new
name|ActionFilters
argument_list|(
operator|new
name|HashSet
argument_list|<>
argument_list|()
argument_list|)
argument_list|,
operator|new
name|IndexNameExpressionResolver
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
argument_list|,
name|NodesRequest
operator|::
operator|new
argument_list|,
name|NodeRequest
operator|::
operator|new
argument_list|,
name|ThreadPool
operator|.
name|Names
operator|.
name|GENERIC
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|newResponse
specifier|protected
name|NodesResponse
name|newResponse
parameter_list|(
name|NodesRequest
name|request
parameter_list|,
name|AtomicReferenceArray
name|responses
parameter_list|)
block|{
specifier|final
name|List
argument_list|<
name|NodeResponse
argument_list|>
name|nodesList
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|int
name|failureCount
init|=
literal|0
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
name|responses
operator|.
name|length
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|Object
name|resp
init|=
name|responses
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|resp
operator|instanceof
name|NodeResponse
condition|)
block|{
comment|// will also filter out null response for unallocated ones
name|nodesList
operator|.
name|add
argument_list|(
operator|(
name|NodeResponse
operator|)
name|resp
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|resp
operator|instanceof
name|FailedNodeException
condition|)
block|{
name|failureCount
operator|++
expr_stmt|;
block|}
else|else
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"unknown response type [{}], expected NodeLocalGatewayMetaState or FailedNodeException"
argument_list|,
name|resp
argument_list|)
expr_stmt|;
block|}
block|}
return|return
operator|new
name|NodesResponse
argument_list|(
name|clusterName
argument_list|,
name|nodesList
operator|.
name|toArray
argument_list|(
operator|new
name|NodeResponse
index|[
name|nodesList
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|,
name|failureCount
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|filterNodeIds
specifier|protected
name|String
index|[]
name|filterNodeIds
parameter_list|(
name|DiscoveryNodes
name|nodes
parameter_list|,
name|String
index|[]
name|nodesIds
parameter_list|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|list
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|node
range|:
name|nodesIds
control|)
block|{
if|if
condition|(
name|nodes
operator|.
name|getDataNodes
argument_list|()
operator|.
name|containsKey
argument_list|(
name|node
argument_list|)
condition|)
block|{
name|list
operator|.
name|add
argument_list|(
name|node
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|list
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
name|list
operator|.
name|size
argument_list|()
index|]
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|newNodeRequest
specifier|protected
name|NodeRequest
name|newNodeRequest
parameter_list|(
name|String
name|nodeId
parameter_list|,
name|NodesRequest
name|request
parameter_list|)
block|{
return|return
operator|new
name|NodeRequest
argument_list|(
name|request
argument_list|,
name|nodeId
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|newNodeResponse
specifier|protected
name|NodeResponse
name|newNodeResponse
parameter_list|()
block|{
return|return
operator|new
name|NodeResponse
argument_list|()
return|;
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
name|NodesRequest
name|request
parameter_list|,
name|ActionListener
argument_list|<
name|NodesResponse
argument_list|>
name|listener
parameter_list|)
block|{
name|super
operator|.
name|doExecute
argument_list|(
name|task
argument_list|,
name|request
argument_list|,
name|listener
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|nodeOperation
specifier|protected
name|NodeResponse
name|nodeOperation
parameter_list|(
name|NodeRequest
name|request
parameter_list|,
name|Task
name|task
parameter_list|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"Test task started on the node {}"
argument_list|,
name|clusterService
operator|.
name|localNode
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
name|awaitBusy
argument_list|(
parameter_list|()
lambda|->
block|{
if|if
condition|(
operator|(
operator|(
name|CancellableTask
operator|)
name|task
operator|)
operator|.
name|isCancelled
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Cancelled!"
argument_list|)
throw|;
block|}
return|return
operator|(
operator|(
name|TestTask
operator|)
name|task
operator|)
operator|.
name|isBlocked
argument_list|()
operator|==
literal|false
return|;
block|}
argument_list|)
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
name|logger
operator|.
name|info
argument_list|(
literal|"Test task finished on the node {}"
argument_list|,
name|clusterService
operator|.
name|localNode
argument_list|()
argument_list|)
expr_stmt|;
return|return
operator|new
name|NodeResponse
argument_list|(
name|clusterService
operator|.
name|localNode
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|nodeOperation
specifier|protected
name|NodeResponse
name|nodeOperation
parameter_list|(
name|NodeRequest
name|request
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"the task parameter is required"
argument_list|)
throw|;
block|}
annotation|@
name|Override
DECL|method|accumulateExceptions
specifier|protected
name|boolean
name|accumulateExceptions
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
block|}
DECL|class|TestTaskAction
specifier|public
specifier|static
class|class
name|TestTaskAction
extends|extends
name|Action
argument_list|<
name|NodesRequest
argument_list|,
name|NodesResponse
argument_list|,
name|NodesRequestBuilder
argument_list|>
block|{
DECL|field|INSTANCE
specifier|public
specifier|static
specifier|final
name|TestTaskAction
name|INSTANCE
init|=
operator|new
name|TestTaskAction
argument_list|()
decl_stmt|;
DECL|field|NAME
specifier|public
specifier|static
specifier|final
name|String
name|NAME
init|=
literal|"cluster:admin/tasks/test"
decl_stmt|;
DECL|method|TestTaskAction
specifier|private
name|TestTaskAction
parameter_list|()
block|{
name|super
argument_list|(
name|NAME
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|newResponse
specifier|public
name|NodesResponse
name|newResponse
parameter_list|()
block|{
return|return
operator|new
name|NodesResponse
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|newRequestBuilder
specifier|public
name|NodesRequestBuilder
name|newRequestBuilder
parameter_list|(
name|ElasticsearchClient
name|client
parameter_list|)
block|{
return|return
operator|new
name|NodesRequestBuilder
argument_list|(
name|client
argument_list|,
name|this
argument_list|)
return|;
block|}
block|}
DECL|class|NodesRequestBuilder
specifier|public
specifier|static
class|class
name|NodesRequestBuilder
extends|extends
name|ActionRequestBuilder
argument_list|<
name|NodesRequest
argument_list|,
name|NodesResponse
argument_list|,
name|NodesRequestBuilder
argument_list|>
block|{
DECL|method|NodesRequestBuilder
specifier|protected
name|NodesRequestBuilder
parameter_list|(
name|ElasticsearchClient
name|client
parameter_list|,
name|Action
argument_list|<
name|NodesRequest
argument_list|,
name|NodesResponse
argument_list|,
name|NodesRequestBuilder
argument_list|>
name|action
parameter_list|)
block|{
name|super
argument_list|(
name|client
argument_list|,
name|action
argument_list|,
operator|new
name|NodesRequest
argument_list|(
literal|"test"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|UnblockTestTaskResponse
specifier|public
specifier|static
class|class
name|UnblockTestTaskResponse
implements|implements
name|Writeable
block|{
DECL|method|UnblockTestTaskResponse
specifier|public
name|UnblockTestTaskResponse
parameter_list|()
block|{          }
DECL|method|UnblockTestTaskResponse
specifier|public
name|UnblockTestTaskResponse
parameter_list|(
name|StreamInput
name|in
parameter_list|)
block|{         }
annotation|@
name|Override
DECL|method|writeTo
specifier|public
name|void
name|writeTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{         }
block|}
DECL|class|UnblockTestTasksRequest
specifier|public
specifier|static
class|class
name|UnblockTestTasksRequest
extends|extends
name|BaseTasksRequest
argument_list|<
name|UnblockTestTasksRequest
argument_list|>
block|{
annotation|@
name|Override
DECL|method|match
specifier|public
name|boolean
name|match
parameter_list|(
name|Task
name|task
parameter_list|)
block|{
return|return
name|task
operator|instanceof
name|TestTask
operator|&&
name|super
operator|.
name|match
argument_list|(
name|task
argument_list|)
return|;
block|}
block|}
DECL|class|UnblockTestTasksResponse
specifier|public
specifier|static
class|class
name|UnblockTestTasksResponse
extends|extends
name|BaseTasksResponse
block|{
DECL|field|tasks
specifier|private
name|List
argument_list|<
name|UnblockTestTaskResponse
argument_list|>
name|tasks
decl_stmt|;
DECL|method|UnblockTestTasksResponse
specifier|public
name|UnblockTestTasksResponse
parameter_list|()
block|{          }
DECL|method|UnblockTestTasksResponse
specifier|public
name|UnblockTestTasksResponse
parameter_list|(
name|List
argument_list|<
name|UnblockTestTaskResponse
argument_list|>
name|tasks
parameter_list|,
name|List
argument_list|<
name|TaskOperationFailure
argument_list|>
name|taskFailures
parameter_list|,
name|List
argument_list|<
name|?
extends|extends
name|FailedNodeException
argument_list|>
name|nodeFailures
parameter_list|)
block|{
name|super
argument_list|(
name|taskFailures
argument_list|,
name|nodeFailures
argument_list|)
expr_stmt|;
name|this
operator|.
name|tasks
operator|=
name|tasks
operator|==
literal|null
condition|?
name|Collections
operator|.
name|emptyList
argument_list|()
else|:
name|Collections
operator|.
name|unmodifiableList
argument_list|(
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|tasks
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|readFrom
specifier|public
name|void
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|int
name|taskCount
init|=
name|in
operator|.
name|readVInt
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|UnblockTestTaskResponse
argument_list|>
name|builder
init|=
operator|new
name|ArrayList
argument_list|<>
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
name|taskCount
condition|;
name|i
operator|++
control|)
block|{
name|builder
operator|.
name|add
argument_list|(
operator|new
name|UnblockTestTaskResponse
argument_list|(
name|in
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|tasks
operator|=
name|Collections
operator|.
name|unmodifiableList
argument_list|(
name|builder
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|writeTo
specifier|public
name|void
name|writeTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|tasks
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|UnblockTestTaskResponse
name|task
range|:
name|tasks
control|)
block|{
name|task
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**      * Test class for testing task operations      */
DECL|class|TransportUnblockTestTasksAction
specifier|public
specifier|static
class|class
name|TransportUnblockTestTasksAction
extends|extends
name|TransportTasksAction
argument_list|<
name|Task
argument_list|,
name|UnblockTestTasksRequest
argument_list|,
name|UnblockTestTasksResponse
argument_list|,
name|UnblockTestTaskResponse
argument_list|>
block|{
annotation|@
name|Inject
DECL|method|TransportUnblockTestTasksAction
specifier|public
name|TransportUnblockTestTasksAction
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ClusterName
name|clusterName
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|,
name|ClusterService
name|clusterService
parameter_list|,
name|TransportService
name|transportService
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|UnblockTestTasksAction
operator|.
name|NAME
argument_list|,
name|clusterName
argument_list|,
name|threadPool
argument_list|,
name|clusterService
argument_list|,
name|transportService
argument_list|,
operator|new
name|ActionFilters
argument_list|(
operator|new
name|HashSet
argument_list|<>
argument_list|()
argument_list|)
argument_list|,
operator|new
name|IndexNameExpressionResolver
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
argument_list|,
name|UnblockTestTasksRequest
operator|::
operator|new
argument_list|,
name|UnblockTestTasksResponse
operator|::
operator|new
argument_list|,
name|ThreadPool
operator|.
name|Names
operator|.
name|MANAGEMENT
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|newResponse
specifier|protected
name|UnblockTestTasksResponse
name|newResponse
parameter_list|(
name|UnblockTestTasksRequest
name|request
parameter_list|,
name|List
argument_list|<
name|UnblockTestTaskResponse
argument_list|>
name|tasks
parameter_list|,
name|List
argument_list|<
name|TaskOperationFailure
argument_list|>
name|taskOperationFailures
parameter_list|,
name|List
argument_list|<
name|FailedNodeException
argument_list|>
name|failedNodeExceptions
parameter_list|)
block|{
return|return
operator|new
name|UnblockTestTasksResponse
argument_list|(
name|tasks
argument_list|,
name|taskOperationFailures
argument_list|,
name|failedNodeExceptions
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|readTaskResponse
specifier|protected
name|UnblockTestTaskResponse
name|readTaskResponse
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|UnblockTestTaskResponse
argument_list|(
name|in
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|taskOperation
specifier|protected
name|UnblockTestTaskResponse
name|taskOperation
parameter_list|(
name|UnblockTestTasksRequest
name|request
parameter_list|,
name|Task
name|task
parameter_list|)
block|{
operator|(
operator|(
name|TestTask
operator|)
name|task
operator|)
operator|.
name|unblock
argument_list|()
expr_stmt|;
return|return
operator|new
name|UnblockTestTaskResponse
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|accumulateExceptions
specifier|protected
name|boolean
name|accumulateExceptions
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
block|}
DECL|class|UnblockTestTasksAction
specifier|public
specifier|static
class|class
name|UnblockTestTasksAction
extends|extends
name|Action
argument_list|<
name|UnblockTestTasksRequest
argument_list|,
name|UnblockTestTasksResponse
argument_list|,
name|UnblockTestTasksRequestBuilder
argument_list|>
block|{
DECL|field|INSTANCE
specifier|public
specifier|static
specifier|final
name|UnblockTestTasksAction
name|INSTANCE
init|=
operator|new
name|UnblockTestTasksAction
argument_list|()
decl_stmt|;
DECL|field|NAME
specifier|public
specifier|static
specifier|final
name|String
name|NAME
init|=
literal|"cluster:admin/tasks/testunblock"
decl_stmt|;
DECL|method|UnblockTestTasksAction
specifier|private
name|UnblockTestTasksAction
parameter_list|()
block|{
name|super
argument_list|(
name|NAME
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|newResponse
specifier|public
name|UnblockTestTasksResponse
name|newResponse
parameter_list|()
block|{
return|return
operator|new
name|UnblockTestTasksResponse
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|newRequestBuilder
specifier|public
name|UnblockTestTasksRequestBuilder
name|newRequestBuilder
parameter_list|(
name|ElasticsearchClient
name|client
parameter_list|)
block|{
return|return
operator|new
name|UnblockTestTasksRequestBuilder
argument_list|(
name|client
argument_list|,
name|this
argument_list|)
return|;
block|}
block|}
DECL|class|UnblockTestTasksRequestBuilder
specifier|public
specifier|static
class|class
name|UnblockTestTasksRequestBuilder
extends|extends
name|ActionRequestBuilder
argument_list|<
name|UnblockTestTasksRequest
argument_list|,
name|UnblockTestTasksResponse
argument_list|,
name|UnblockTestTasksRequestBuilder
argument_list|>
block|{
DECL|method|UnblockTestTasksRequestBuilder
specifier|protected
name|UnblockTestTasksRequestBuilder
parameter_list|(
name|ElasticsearchClient
name|client
parameter_list|,
name|Action
argument_list|<
name|UnblockTestTasksRequest
argument_list|,
name|UnblockTestTasksResponse
argument_list|,
name|UnblockTestTasksRequestBuilder
argument_list|>
name|action
parameter_list|)
block|{
name|super
argument_list|(
name|client
argument_list|,
name|action
argument_list|,
operator|new
name|UnblockTestTasksRequest
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

