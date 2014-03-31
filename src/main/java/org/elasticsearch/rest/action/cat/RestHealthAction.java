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
name|BytesRestResponse
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
name|action
operator|.
name|support
operator|.
name|RestTable
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
name|Locale
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
name|Client
name|client
parameter_list|,
name|RestController
name|controller
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|client
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
name|ActionListener
argument_list|<
name|ClusterHealthResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onResponse
parameter_list|(
specifier|final
name|ClusterHealthResponse
name|health
parameter_list|)
block|{
try|try
block|{
name|channel
operator|.
name|sendResponse
argument_list|(
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
name|request
argument_list|,
name|channel
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|onFailure
argument_list|(
name|t
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
name|Throwable
name|e
parameter_list|)
block|{
try|try
block|{
name|channel
operator|.
name|sendResponse
argument_list|(
operator|new
name|BytesRestResponse
argument_list|(
name|request
argument_list|,
name|e
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e1
parameter_list|)
block|{
name|logger
operator|.
name|error
argument_list|(
literal|"Failed to send failure response"
argument_list|,
name|e1
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getTableWithHeader
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
name|startHeaders
argument_list|()
expr_stmt|;
name|t
operator|.
name|addCell
argument_list|(
literal|"epoch"
argument_list|,
literal|"alias:t,time;desc:seconds since 1970-01-01 00:00:00"
argument_list|)
expr_stmt|;
name|t
operator|.
name|addCell
argument_list|(
literal|"timestamp"
argument_list|,
literal|"alias:ts,hms,hhmmss;desc:time in HH:MM:SS"
argument_list|)
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
name|endHeaders
argument_list|()
expr_stmt|;
return|return
name|t
return|;
block|}
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
name|long
name|time
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
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
name|TimeUnit
operator|.
name|SECONDS
operator|.
name|convert
argument_list|(
name|time
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
argument_list|)
expr_stmt|;
name|t
operator|.
name|addCell
argument_list|(
name|dateFormat
operator|.
name|print
argument_list|(
name|time
argument_list|)
argument_list|)
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

