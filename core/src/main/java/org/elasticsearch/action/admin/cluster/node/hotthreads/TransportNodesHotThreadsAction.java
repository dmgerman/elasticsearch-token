begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.cluster.node.hotthreads
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
name|hotthreads
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
name|TransportNodesAction
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
name|ClusterService
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
name|monitor
operator|.
name|jvm
operator|.
name|HotThreads
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

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|TransportNodesHotThreadsAction
specifier|public
class|class
name|TransportNodesHotThreadsAction
extends|extends
name|TransportNodesAction
argument_list|<
name|NodesHotThreadsRequest
argument_list|,
name|NodesHotThreadsResponse
argument_list|,
name|TransportNodesHotThreadsAction
operator|.
name|NodeRequest
argument_list|,
name|NodeHotThreads
argument_list|>
block|{
annotation|@
name|Inject
DECL|method|TransportNodesHotThreadsAction
specifier|public
name|TransportNodesHotThreadsAction
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
name|NodesHotThreadsAction
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
name|actionFilters
argument_list|,
name|indexNameExpressionResolver
argument_list|,
name|NodesHotThreadsRequest
operator|.
name|class
argument_list|,
name|NodeRequest
operator|.
name|class
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
name|NodesHotThreadsResponse
name|newResponse
parameter_list|(
name|NodesHotThreadsRequest
name|request
parameter_list|,
name|AtomicReferenceArray
name|responses
parameter_list|)
block|{
specifier|final
name|List
argument_list|<
name|NodeHotThreads
argument_list|>
name|nodes
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
name|NodeHotThreads
condition|)
block|{
name|nodes
operator|.
name|add
argument_list|(
operator|(
name|NodeHotThreads
operator|)
name|resp
argument_list|)
expr_stmt|;
block|}
block|}
return|return
operator|new
name|NodesHotThreadsResponse
argument_list|(
name|clusterName
argument_list|,
name|nodes
operator|.
name|toArray
argument_list|(
operator|new
name|NodeHotThreads
index|[
name|nodes
operator|.
name|size
argument_list|()
index|]
argument_list|)
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
name|NodesHotThreadsRequest
name|request
parameter_list|)
block|{
return|return
operator|new
name|NodeRequest
argument_list|(
name|nodeId
argument_list|,
name|request
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|newNodeResponse
specifier|protected
name|NodeHotThreads
name|newNodeResponse
parameter_list|()
block|{
return|return
operator|new
name|NodeHotThreads
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|nodeOperation
specifier|protected
name|NodeHotThreads
name|nodeOperation
parameter_list|(
name|NodeRequest
name|request
parameter_list|)
block|{
name|HotThreads
name|hotThreads
init|=
operator|new
name|HotThreads
argument_list|()
operator|.
name|busiestThreads
argument_list|(
name|request
operator|.
name|request
operator|.
name|threads
argument_list|)
operator|.
name|type
argument_list|(
name|request
operator|.
name|request
operator|.
name|type
argument_list|)
operator|.
name|interval
argument_list|(
name|request
operator|.
name|request
operator|.
name|interval
argument_list|)
operator|.
name|threadElementsSnapshotCount
argument_list|(
name|request
operator|.
name|request
operator|.
name|snapshots
argument_list|)
operator|.
name|ignoreIdleThreads
argument_list|(
name|request
operator|.
name|request
operator|.
name|ignoreIdleThreads
argument_list|)
decl_stmt|;
try|try
block|{
return|return
operator|new
name|NodeHotThreads
argument_list|(
name|clusterService
operator|.
name|localNode
argument_list|()
argument_list|,
name|hotThreads
operator|.
name|detect
argument_list|()
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticsearchException
argument_list|(
literal|"failed to detect hot threads"
argument_list|,
name|e
argument_list|)
throw|;
block|}
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
literal|false
return|;
block|}
DECL|class|NodeRequest
specifier|static
class|class
name|NodeRequest
extends|extends
name|BaseNodeRequest
block|{
DECL|field|request
name|NodesHotThreadsRequest
name|request
decl_stmt|;
DECL|method|NodeRequest
name|NodeRequest
parameter_list|()
block|{         }
DECL|method|NodeRequest
name|NodeRequest
parameter_list|(
name|String
name|nodeId
parameter_list|,
name|NodesHotThreadsRequest
name|request
parameter_list|)
block|{
name|super
argument_list|(
name|request
argument_list|,
name|nodeId
argument_list|)
expr_stmt|;
name|this
operator|.
name|request
operator|=
name|request
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
name|request
operator|=
operator|new
name|NodesHotThreadsRequest
argument_list|()
expr_stmt|;
name|request
operator|.
name|readFrom
argument_list|(
name|in
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
name|request
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

