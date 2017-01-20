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
name|client
operator|.
name|node
operator|.
name|NodeClient
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
name|BaseRestHandler
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
name|RestController
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
name|RestRequest
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
name|function
operator|.
name|Supplier
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|RestRequest
operator|.
name|Method
operator|.
name|POST
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|action
operator|.
name|admin
operator|.
name|cluster
operator|.
name|RestListTasksAction
operator|.
name|listTasksResponseListener
import|;
end_import

begin_class
DECL|class|RestRethrottleAction
specifier|public
class|class
name|RestRethrottleAction
extends|extends
name|BaseRestHandler
block|{
DECL|field|nodesInCluster
specifier|private
specifier|final
name|Supplier
argument_list|<
name|DiscoveryNodes
argument_list|>
name|nodesInCluster
decl_stmt|;
DECL|method|RestRethrottleAction
specifier|public
name|RestRethrottleAction
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|RestController
name|controller
parameter_list|,
name|Supplier
argument_list|<
name|DiscoveryNodes
argument_list|>
name|nodesInCluster
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|nodesInCluster
operator|=
name|nodesInCluster
expr_stmt|;
name|controller
operator|.
name|registerHandler
argument_list|(
name|POST
argument_list|,
literal|"/_update_by_query/{taskId}/_rethrottle"
argument_list|,
name|this
argument_list|)
expr_stmt|;
name|controller
operator|.
name|registerHandler
argument_list|(
name|POST
argument_list|,
literal|"/_delete_by_query/{taskId}/_rethrottle"
argument_list|,
name|this
argument_list|)
expr_stmt|;
name|controller
operator|.
name|registerHandler
argument_list|(
name|POST
argument_list|,
literal|"/_reindex/{taskId}/_rethrottle"
argument_list|,
name|this
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|prepareRequest
specifier|public
name|RestChannelConsumer
name|prepareRequest
parameter_list|(
specifier|final
name|RestRequest
name|request
parameter_list|,
specifier|final
name|NodeClient
name|client
parameter_list|)
throws|throws
name|IOException
block|{
name|RethrottleRequest
name|internalRequest
init|=
operator|new
name|RethrottleRequest
argument_list|()
decl_stmt|;
name|internalRequest
operator|.
name|setTaskId
argument_list|(
operator|new
name|TaskId
argument_list|(
name|request
operator|.
name|param
argument_list|(
literal|"taskId"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|Float
name|requestsPerSecond
init|=
name|AbstractBaseReindexRestHandler
operator|.
name|parseRequestsPerSecond
argument_list|(
name|request
argument_list|)
decl_stmt|;
if|if
condition|(
name|requestsPerSecond
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"requests_per_second is a required parameter"
argument_list|)
throw|;
block|}
name|internalRequest
operator|.
name|setRequestsPerSecond
argument_list|(
name|requestsPerSecond
argument_list|)
expr_stmt|;
specifier|final
name|String
name|groupBy
init|=
name|request
operator|.
name|param
argument_list|(
literal|"group_by"
argument_list|,
literal|"nodes"
argument_list|)
decl_stmt|;
return|return
name|channel
lambda|->
name|client
operator|.
name|execute
argument_list|(
name|RethrottleAction
operator|.
name|INSTANCE
argument_list|,
name|internalRequest
argument_list|,
name|listTasksResponseListener
argument_list|(
name|nodesInCluster
argument_list|,
name|groupBy
argument_list|,
name|channel
argument_list|)
argument_list|)
return|;
block|}
block|}
end_class

end_unit

