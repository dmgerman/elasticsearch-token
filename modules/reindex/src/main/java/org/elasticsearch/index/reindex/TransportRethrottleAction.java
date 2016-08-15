begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.reindex
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|reindex
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
name|admin
operator|.
name|cluster
operator|.
name|node
operator|.
name|tasks
operator|.
name|list
operator|.
name|ListTasksResponse
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
name|tasks
operator|.
name|TaskInfo
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
name|List
import|;
end_import

begin_class
DECL|class|TransportRethrottleAction
specifier|public
class|class
name|TransportRethrottleAction
extends|extends
name|TransportTasksAction
argument_list|<
name|BulkByScrollTask
argument_list|,
name|RethrottleRequest
argument_list|,
name|ListTasksResponse
argument_list|,
name|TaskInfo
argument_list|>
block|{
annotation|@
name|Inject
DECL|method|TransportRethrottleAction
specifier|public
name|TransportRethrottleAction
parameter_list|(
name|Settings
name|settings
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
name|RethrottleAction
operator|.
name|NAME
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
name|RethrottleRequest
operator|::
operator|new
argument_list|,
name|ListTasksResponse
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
DECL|method|taskOperation
specifier|protected
name|TaskInfo
name|taskOperation
parameter_list|(
name|RethrottleRequest
name|request
parameter_list|,
name|BulkByScrollTask
name|task
parameter_list|)
block|{
comment|// Apply the new throttle and fetch status of the task. The user might not want that status but they likely do and it is cheap.
name|task
operator|.
name|rethrottle
argument_list|(
name|request
operator|.
name|getRequestsPerSecond
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|task
operator|.
name|taskInfo
argument_list|(
name|clusterService
operator|.
name|localNode
argument_list|()
argument_list|,
literal|true
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|readTaskResponse
specifier|protected
name|TaskInfo
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
name|TaskInfo
argument_list|(
name|in
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|newResponse
specifier|protected
name|ListTasksResponse
name|newResponse
parameter_list|(
name|RethrottleRequest
name|request
parameter_list|,
name|List
argument_list|<
name|TaskInfo
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
name|ListTasksResponse
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
end_class

end_unit

