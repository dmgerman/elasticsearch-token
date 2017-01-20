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
name|TaskGroup
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
name|unit
operator|.
name|TimeValue
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
name|RestResponseListener
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
name|joda
operator|.
name|time
operator|.
name|format
operator|.
name|DateTimeFormat
import|;
end_import

begin_import
import|import
name|org
operator|.
name|joda
operator|.
name|time
operator|.
name|format
operator|.
name|DateTimeFormatter
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
name|Set
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
name|GET
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
name|generateListTasksRequest
import|;
end_import

begin_class
DECL|class|RestTasksAction
specifier|public
class|class
name|RestTasksAction
extends|extends
name|AbstractCatAction
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
DECL|method|RestTasksAction
specifier|public
name|RestTasksAction
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
name|controller
operator|.
name|registerHandler
argument_list|(
name|GET
argument_list|,
literal|"/_cat/tasks"
argument_list|,
name|this
argument_list|)
expr_stmt|;
name|this
operator|.
name|nodesInCluster
operator|=
name|nodesInCluster
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
literal|"/_cat/tasks\n"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doCatRequest
specifier|public
name|RestChannelConsumer
name|doCatRequest
parameter_list|(
specifier|final
name|RestRequest
name|request
parameter_list|,
specifier|final
name|NodeClient
name|client
parameter_list|)
block|{
return|return
name|channel
lambda|->
name|client
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|listTasks
argument_list|(
name|generateListTasksRequest
argument_list|(
name|request
argument_list|)
argument_list|,
operator|new
name|RestResponseListener
argument_list|<
name|ListTasksResponse
argument_list|>
argument_list|(
name|channel
argument_list|)
block|{
block|@Override             public RestResponse buildResponse(ListTasksResponse listTasksResponse
block|)
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
name|request
argument_list|,
name|listTasksResponse
argument_list|)
argument_list|,
name|channel
argument_list|)
return|;
block|}
block|}
end_class

begin_empty_stmt
unit|)
empty_stmt|;
end_empty_stmt

begin_decl_stmt
unit|}      private
DECL|field|RESPONSE_PARAMS
specifier|static
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|RESPONSE_PARAMS
decl_stmt|;
end_decl_stmt

begin_static
static|static
block|{
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|responseParams
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
name|responseParams
operator|.
name|add
argument_list|(
literal|"detailed"
argument_list|)
expr_stmt|;
name|responseParams
operator|.
name|addAll
argument_list|(
name|AbstractCatAction
operator|.
name|RESPONSE_PARAMS
argument_list|)
expr_stmt|;
name|RESPONSE_PARAMS
operator|=
name|Collections
operator|.
name|unmodifiableSet
argument_list|(
name|responseParams
argument_list|)
expr_stmt|;
block|}
end_static

begin_function
annotation|@
name|Override
DECL|method|responseParams
specifier|protected
name|Set
argument_list|<
name|String
argument_list|>
name|responseParams
parameter_list|()
block|{
return|return
name|RESPONSE_PARAMS
return|;
block|}
end_function

begin_function
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
name|boolean
name|detailed
init|=
name|request
operator|.
name|paramAsBoolean
argument_list|(
literal|"detailed"
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|Table
name|table
init|=
operator|new
name|Table
argument_list|()
decl_stmt|;
name|table
operator|.
name|startHeaders
argument_list|()
expr_stmt|;
comment|// Task main info
name|table
operator|.
name|addCell
argument_list|(
literal|"id"
argument_list|,
literal|"default:false;desc:id of the task with the node"
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
literal|"action"
argument_list|,
literal|"alias:ac;desc:task action"
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
literal|"task_id"
argument_list|,
literal|"alias:ti;desc:unique task id"
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
literal|"parent_task_id"
argument_list|,
literal|"alias:pti;desc:parent task id"
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
literal|"type"
argument_list|,
literal|"alias:ty;desc:task type"
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
literal|"start_time"
argument_list|,
literal|"alias:start;desc:start time in ms"
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
literal|"timestamp"
argument_list|,
literal|"alias:ts,hms,hhmmss;desc:start time in HH:MM:SS"
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
literal|"running_time_ns"
argument_list|,
literal|"default:false;alias:time;desc:running time ns"
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
literal|"running_time"
argument_list|,
literal|"default:true;alias:time;desc:running time"
argument_list|)
expr_stmt|;
comment|// Node info
name|table
operator|.
name|addCell
argument_list|(
literal|"node_id"
argument_list|,
literal|"default:false;alias:ni;desc:unique node id"
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
literal|"ip"
argument_list|,
literal|"default:true;alias:i;desc:ip address"
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
literal|"port"
argument_list|,
literal|"default:false;alias:po;desc:bound transport port"
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
literal|"node"
argument_list|,
literal|"default:true;alias:n;desc:node name"
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
literal|"version"
argument_list|,
literal|"default:false;alias:v;desc:es version"
argument_list|)
expr_stmt|;
comment|// Task detailed info
if|if
condition|(
name|detailed
condition|)
block|{
name|table
operator|.
name|addCell
argument_list|(
literal|"description"
argument_list|,
literal|"default:true;alias:desc;desc:task action"
argument_list|)
expr_stmt|;
block|}
name|table
operator|.
name|endHeaders
argument_list|()
expr_stmt|;
return|return
name|table
return|;
block|}
end_function

begin_decl_stmt
DECL|field|dateFormat
specifier|private
name|DateTimeFormatter
name|dateFormat
init|=
name|DateTimeFormat
operator|.
name|forPattern
argument_list|(
literal|"HH:mm:ss"
argument_list|)
decl_stmt|;
end_decl_stmt

begin_function
DECL|method|buildRow
specifier|private
name|void
name|buildRow
parameter_list|(
name|Table
name|table
parameter_list|,
name|boolean
name|fullId
parameter_list|,
name|boolean
name|detailed
parameter_list|,
name|DiscoveryNodes
name|discoveryNodes
parameter_list|,
name|TaskInfo
name|taskInfo
parameter_list|)
block|{
name|table
operator|.
name|startRow
argument_list|()
expr_stmt|;
name|String
name|nodeId
init|=
name|taskInfo
operator|.
name|getTaskId
argument_list|()
operator|.
name|getNodeId
argument_list|()
decl_stmt|;
name|DiscoveryNode
name|node
init|=
name|discoveryNodes
operator|.
name|get
argument_list|(
name|nodeId
argument_list|)
decl_stmt|;
name|table
operator|.
name|addCell
argument_list|(
name|taskInfo
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
name|taskInfo
operator|.
name|getAction
argument_list|()
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
name|taskInfo
operator|.
name|getTaskId
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|taskInfo
operator|.
name|getParentTaskId
argument_list|()
operator|.
name|isSet
argument_list|()
condition|)
block|{
name|table
operator|.
name|addCell
argument_list|(
name|taskInfo
operator|.
name|getParentTaskId
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|table
operator|.
name|addCell
argument_list|(
literal|"-"
argument_list|)
expr_stmt|;
block|}
name|table
operator|.
name|addCell
argument_list|(
name|taskInfo
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
name|taskInfo
operator|.
name|getStartTime
argument_list|()
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
name|dateFormat
operator|.
name|print
argument_list|(
name|taskInfo
operator|.
name|getStartTime
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
name|taskInfo
operator|.
name|getRunningTimeNanos
argument_list|()
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
name|TimeValue
operator|.
name|timeValueNanos
argument_list|(
name|taskInfo
operator|.
name|getRunningTimeNanos
argument_list|()
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
comment|// Node information. Note that the node may be null because it has left the cluster between when we got this response and now.
name|table
operator|.
name|addCell
argument_list|(
name|fullId
condition|?
name|nodeId
else|:
name|Strings
operator|.
name|substring
argument_list|(
name|nodeId
argument_list|,
literal|0
argument_list|,
literal|4
argument_list|)
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
name|node
operator|==
literal|null
condition|?
literal|"-"
else|:
name|node
operator|.
name|getHostAddress
argument_list|()
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
name|node
operator|.
name|getAddress
argument_list|()
operator|.
name|address
argument_list|()
operator|.
name|getPort
argument_list|()
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
name|node
operator|==
literal|null
condition|?
literal|"-"
else|:
name|node
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
name|node
operator|==
literal|null
condition|?
literal|"-"
else|:
name|node
operator|.
name|getVersion
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|detailed
condition|)
block|{
name|table
operator|.
name|addCell
argument_list|(
name|taskInfo
operator|.
name|getDescription
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|table
operator|.
name|endRow
argument_list|()
expr_stmt|;
block|}
end_function

begin_function
DECL|method|buildGroups
specifier|private
name|void
name|buildGroups
parameter_list|(
name|Table
name|table
parameter_list|,
name|boolean
name|fullId
parameter_list|,
name|boolean
name|detailed
parameter_list|,
name|List
argument_list|<
name|TaskGroup
argument_list|>
name|taskGroups
parameter_list|)
block|{
name|DiscoveryNodes
name|discoveryNodes
init|=
name|nodesInCluster
operator|.
name|get
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|TaskGroup
argument_list|>
name|sortedGroups
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|taskGroups
argument_list|)
decl_stmt|;
name|sortedGroups
operator|.
name|sort
argument_list|(
parameter_list|(
name|o1
parameter_list|,
name|o2
parameter_list|)
lambda|->
name|Long
operator|.
name|compare
argument_list|(
name|o1
operator|.
name|getTaskInfo
argument_list|()
operator|.
name|getStartTime
argument_list|()
argument_list|,
name|o2
operator|.
name|getTaskInfo
argument_list|()
operator|.
name|getStartTime
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|TaskGroup
name|taskGroup
range|:
name|sortedGroups
control|)
block|{
name|buildRow
argument_list|(
name|table
argument_list|,
name|fullId
argument_list|,
name|detailed
argument_list|,
name|discoveryNodes
argument_list|,
name|taskGroup
operator|.
name|getTaskInfo
argument_list|()
argument_list|)
expr_stmt|;
name|buildGroups
argument_list|(
name|table
argument_list|,
name|fullId
argument_list|,
name|detailed
argument_list|,
name|taskGroup
operator|.
name|getChildTasks
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_function

begin_function
DECL|method|buildTable
specifier|private
name|Table
name|buildTable
parameter_list|(
name|RestRequest
name|request
parameter_list|,
name|ListTasksResponse
name|listTasksResponse
parameter_list|)
block|{
name|boolean
name|fullId
init|=
name|request
operator|.
name|paramAsBoolean
argument_list|(
literal|"full_id"
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|boolean
name|detailed
init|=
name|request
operator|.
name|paramAsBoolean
argument_list|(
literal|"detailed"
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|Table
name|table
init|=
name|getTableWithHeader
argument_list|(
name|request
argument_list|)
decl_stmt|;
name|buildGroups
argument_list|(
name|table
argument_list|,
name|fullId
argument_list|,
name|detailed
argument_list|,
name|listTasksResponse
operator|.
name|getTaskGroups
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|table
return|;
block|}
end_function

unit|}
end_unit

