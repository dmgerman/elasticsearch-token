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
name|support
operator|.
name|nodes
operator|.
name|NodesOperationRequestBuilder
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
name|ClusterAdminClient
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
name|common
operator|.
name|unit
operator|.
name|TimeValue
import|;
end_import

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|NodesHotThreadsRequestBuilder
specifier|public
class|class
name|NodesHotThreadsRequestBuilder
extends|extends
name|NodesOperationRequestBuilder
argument_list|<
name|NodesHotThreadsRequest
argument_list|,
name|NodesHotThreadsResponse
argument_list|,
name|NodesHotThreadsRequestBuilder
argument_list|>
block|{
DECL|method|NodesHotThreadsRequestBuilder
specifier|public
name|NodesHotThreadsRequestBuilder
parameter_list|(
name|ElasticsearchClient
name|client
parameter_list|,
name|NodesHotThreadsAction
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
name|NodesHotThreadsRequest
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|setThreads
specifier|public
name|NodesHotThreadsRequestBuilder
name|setThreads
parameter_list|(
name|int
name|threads
parameter_list|)
block|{
name|request
operator|.
name|threads
argument_list|(
name|threads
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|setIgnoreIdleThreads
specifier|public
name|NodesHotThreadsRequestBuilder
name|setIgnoreIdleThreads
parameter_list|(
name|boolean
name|ignoreIdleThreads
parameter_list|)
block|{
name|request
operator|.
name|ignoreIdleThreads
argument_list|(
name|ignoreIdleThreads
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|setType
specifier|public
name|NodesHotThreadsRequestBuilder
name|setType
parameter_list|(
name|String
name|type
parameter_list|)
block|{
name|request
operator|.
name|type
argument_list|(
name|type
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|setInterval
specifier|public
name|NodesHotThreadsRequestBuilder
name|setInterval
parameter_list|(
name|TimeValue
name|interval
parameter_list|)
block|{
name|request
operator|.
name|interval
argument_list|(
name|interval
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
block|}
end_class

end_unit
