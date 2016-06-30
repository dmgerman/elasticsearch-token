begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.rest.action.cat
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|action
operator|.
name|cat
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
name|health
operator|.
name|ClusterHealthRequest
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
name|common
operator|.
name|Table
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
name|RestChannel
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
name|rest
operator|.
name|RestResponse
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
name|action
operator|.
name|support
operator|.
name|RestResponseListener
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
name|action
operator|.
name|support
operator|.
name|RestTable
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Locale
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
name|GET
import|;
end_import

begin_class
DECL|class|RestHealthAction
specifier|public
class|class
name|RestHealthAction
extends|extends
name|AbstractCatAction
block|{
annotation|@
name|Inject
DECL|method|RestHealthAction
specifier|public
name|RestHealthAction
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|RestController
name|controller
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|controller
argument_list|)
expr_stmt|;
name|controller
operator|.
name|registerHandler
argument_list|(
name|GET
argument_list|,
literal|"/_cat/health"
argument_list|,
name|this
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|documentation
specifier|protected
name|void
name|documentation
parameter_list|(
name|StringBuilder
name|sb
parameter_list|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"/_cat/health\n"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doRequest
specifier|public
name|void
name|doRequest
parameter_list|(
specifier|final
name|RestRequest
name|request
parameter_list|,
specifier|final
name|RestChannel
name|channel
parameter_list|,
specifier|final
name|NodeClient
name|client
parameter_list|)
block|{
name|ClusterHealthRequest
name|clusterHealthRequest
init|=
operator|new
name|ClusterHealthRequest
argument_list|()
decl_stmt|;
name|client
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|health
argument_list|(
name|clusterHealthRequest
argument_list|,
operator|new
name|RestResponseListener
argument_list|<
name|ClusterHealthResponse
argument_list|>
argument_list|(
name|channel
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|RestResponse
name|buildResponse
parameter_list|(
specifier|final
name|ClusterHealthResponse
name|health
parameter_list|)
throws|throws
name|Exception
block|{
return|return
name|RestTable
operator|.
name|buildResponse
argument_list|(
name|buildTable
argument_list|(
name|health
argument_list|,
name|request
argument_list|)
argument_list|,
name|channel
argument_list|)
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getTableWithHeader
specifier|protected
name|Table
name|getTableWithHeader
parameter_list|(
specifier|final
name|RestRequest
name|request
parameter_list|)
block|{
name|Table
name|t
init|=
operator|new
name|Table
argument_list|()
decl_stmt|;
name|t
operator|.
name|startHeadersWithTimestamp
argument_list|()
expr_stmt|;
name|t
operator|.
name|addCell
argument_list|(
literal|"cluster"
argument_list|,
literal|"alias:cl;desc:cluster name"
argument_list|)
expr_stmt|;
name|t
operator|.
name|addCell
argument_list|(
literal|"status"
argument_list|,
literal|"alias:st;desc:health status"
argument_list|)
expr_stmt|;
name|t
operator|.
name|addCell
argument_list|(
literal|"node.total"
argument_list|,
literal|"alias:nt,nodeTotal;text-align:right;desc:total number of nodes"
argument_list|)
expr_stmt|;
name|t
operator|.
name|addCell
argument_list|(
literal|"node.data"
argument_list|,
literal|"alias:nd,nodeData;text-align:right;desc:number of nodes that can store data"
argument_list|)
expr_stmt|;
name|t
operator|.
name|addCell
argument_list|(
literal|"shards"
argument_list|,
literal|"alias:t,sh,shards.total,shardsTotal;text-align:right;desc:total number of shards"
argument_list|)
expr_stmt|;
name|t
operator|.
name|addCell
argument_list|(
literal|"pri"
argument_list|,
literal|"alias:p,shards.primary,shardsPrimary;text-align:right;desc:number of primary shards"
argument_list|)
expr_stmt|;
name|t
operator|.
name|addCell
argument_list|(
literal|"relo"
argument_list|,
literal|"alias:r,shards.relocating,shardsRelocating;text-align:right;desc:number of relocating nodes"
argument_list|)
expr_stmt|;
name|t
operator|.
name|addCell
argument_list|(
literal|"init"
argument_list|,
literal|"alias:i,shards.initializing,shardsInitializing;text-align:right;desc:number of initializing nodes"
argument_list|)
expr_stmt|;
name|t
operator|.
name|addCell
argument_list|(
literal|"unassign"
argument_list|,
literal|"alias:u,shards.unassigned,shardsUnassigned;text-align:right;desc:number of unassigned shards"
argument_list|)
expr_stmt|;
name|t
operator|.
name|addCell
argument_list|(
literal|"pending_tasks"
argument_list|,
literal|"alias:pt,pendingTasks;text-align:right;desc:number of pending tasks"
argument_list|)
expr_stmt|;
name|t
operator|.
name|addCell
argument_list|(
literal|"max_task_wait_time"
argument_list|,
literal|"alias:mtwt,maxTaskWaitTime;text-align:right;desc:wait time of longest task pending"
argument_list|)
expr_stmt|;
name|t
operator|.
name|addCell
argument_list|(
literal|"active_shards_percent"
argument_list|,
literal|"alias:asp,activeShardsPercent;text-align:right;desc:active number of shards in percent"
argument_list|)
expr_stmt|;
name|t
operator|.
name|endHeaders
argument_list|()
expr_stmt|;
return|return
name|t
return|;
block|}
DECL|method|buildTable
specifier|private
name|Table
name|buildTable
parameter_list|(
specifier|final
name|ClusterHealthResponse
name|health
parameter_list|,
specifier|final
name|RestRequest
name|request
parameter_list|)
block|{
name|Table
name|t
init|=
name|getTableWithHeader
argument_list|(
name|request
argument_list|)
decl_stmt|;
name|t
operator|.
name|startRow
argument_list|()
expr_stmt|;
name|t
operator|.
name|addCell
argument_list|(
name|health
operator|.
name|getClusterName
argument_list|()
argument_list|)
expr_stmt|;
name|t
operator|.
name|addCell
argument_list|(
name|health
operator|.
name|getStatus
argument_list|()
operator|.
name|name
argument_list|()
operator|.
name|toLowerCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
argument_list|)
expr_stmt|;
name|t
operator|.
name|addCell
argument_list|(
name|health
operator|.
name|getNumberOfNodes
argument_list|()
argument_list|)
expr_stmt|;
name|t
operator|.
name|addCell
argument_list|(
name|health
operator|.
name|getNumberOfDataNodes
argument_list|()
argument_list|)
expr_stmt|;
name|t
operator|.
name|addCell
argument_list|(
name|health
operator|.
name|getActiveShards
argument_list|()
argument_list|)
expr_stmt|;
name|t
operator|.
name|addCell
argument_list|(
name|health
operator|.
name|getActivePrimaryShards
argument_list|()
argument_list|)
expr_stmt|;
name|t
operator|.
name|addCell
argument_list|(
name|health
operator|.
name|getRelocatingShards
argument_list|()
argument_list|)
expr_stmt|;
name|t
operator|.
name|addCell
argument_list|(
name|health
operator|.
name|getInitializingShards
argument_list|()
argument_list|)
expr_stmt|;
name|t
operator|.
name|addCell
argument_list|(
name|health
operator|.
name|getUnassignedShards
argument_list|()
argument_list|)
expr_stmt|;
name|t
operator|.
name|addCell
argument_list|(
name|health
operator|.
name|getNumberOfPendingTasks
argument_list|()
argument_list|)
expr_stmt|;
name|t
operator|.
name|addCell
argument_list|(
name|health
operator|.
name|getTaskMaxWaitingTime
argument_list|()
operator|.
name|millis
argument_list|()
operator|==
literal|0
condition|?
literal|"-"
else|:
name|health
operator|.
name|getTaskMaxWaitingTime
argument_list|()
argument_list|)
expr_stmt|;
name|t
operator|.
name|addCell
argument_list|(
name|String
operator|.
name|format
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|,
literal|"%1.1f%%"
argument_list|,
name|health
operator|.
name|getActiveShardsPercent
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|t
operator|.
name|endRow
argument_list|()
expr_stmt|;
return|return
name|t
return|;
block|}
block|}
end_class

end_unit

