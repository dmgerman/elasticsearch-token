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
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|CollectionUtil
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
name|recovery
operator|.
name|RecoveryRequest
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
name|recovery
operator|.
name|RecoveryResponse
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
name|recovery
operator|.
name|ShardRecoveryResponse
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
name|IndicesOptions
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
name|indices
operator|.
name|recovery
operator|.
name|RecoveryState
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
name|*
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
name|Comparator
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

begin_comment
comment|/**  * RestRecoveryAction provides information about the status of replica recovery  * in a string format, designed to be used at the command line. An Index can  * be specified to limit output to a particular index or indices.  */
end_comment

begin_class
DECL|class|RestRecoveryAction
specifier|public
class|class
name|RestRecoveryAction
extends|extends
name|AbstractCatAction
block|{
annotation|@
name|Inject
DECL|method|RestRecoveryAction
specifier|protected
name|RestRecoveryAction
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|RestController
name|restController
parameter_list|,
name|RestController
name|controller
parameter_list|,
name|Client
name|client
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|controller
argument_list|,
name|client
argument_list|)
expr_stmt|;
name|restController
operator|.
name|registerHandler
argument_list|(
name|GET
argument_list|,
literal|"/_cat/recovery"
argument_list|,
name|this
argument_list|)
expr_stmt|;
name|restController
operator|.
name|registerHandler
argument_list|(
name|GET
argument_list|,
literal|"/_cat/recovery/{index}"
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
literal|"/_cat/recovery\n"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"/_cat/recovery/{index}\n"
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
name|Client
name|client
parameter_list|)
block|{
specifier|final
name|RecoveryRequest
name|recoveryRequest
init|=
operator|new
name|RecoveryRequest
argument_list|(
name|Strings
operator|.
name|splitStringByCommaToArray
argument_list|(
name|request
operator|.
name|param
argument_list|(
literal|"index"
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|recoveryRequest
operator|.
name|detailed
argument_list|(
name|request
operator|.
name|paramAsBoolean
argument_list|(
literal|"detailed"
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|recoveryRequest
operator|.
name|activeOnly
argument_list|(
name|request
operator|.
name|paramAsBoolean
argument_list|(
literal|"active_only"
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|recoveryRequest
operator|.
name|listenerThreaded
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|recoveryRequest
operator|.
name|indicesOptions
argument_list|(
name|IndicesOptions
operator|.
name|fromRequest
argument_list|(
name|request
argument_list|,
name|recoveryRequest
operator|.
name|indicesOptions
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|client
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|recoveries
argument_list|(
name|recoveryRequest
argument_list|,
operator|new
name|RestResponseListener
argument_list|<
name|RecoveryResponse
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
name|RecoveryResponse
name|response
parameter_list|)
throws|throws
name|Exception
block|{
return|return
name|RestTable
operator|.
name|buildResponse
argument_list|(
name|buildRecoveryTable
argument_list|(
name|request
argument_list|,
name|response
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
name|Table
name|getTableWithHeader
parameter_list|(
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
operator|.
name|addCell
argument_list|(
literal|"index"
argument_list|,
literal|"alias:i,idx;desc:index name"
argument_list|)
operator|.
name|addCell
argument_list|(
literal|"shard"
argument_list|,
literal|"alias:s,sh;desc:shard name"
argument_list|)
operator|.
name|addCell
argument_list|(
literal|"time"
argument_list|,
literal|"alias:t,ti;desc:recovery time"
argument_list|)
operator|.
name|addCell
argument_list|(
literal|"type"
argument_list|,
literal|"alias:ty;desc:recovery type"
argument_list|)
operator|.
name|addCell
argument_list|(
literal|"stage"
argument_list|,
literal|"alias:st;desc:recovery stage"
argument_list|)
operator|.
name|addCell
argument_list|(
literal|"source_host"
argument_list|,
literal|"alias:shost;desc:source host"
argument_list|)
operator|.
name|addCell
argument_list|(
literal|"target_host"
argument_list|,
literal|"alias:thost;desc:target host"
argument_list|)
operator|.
name|addCell
argument_list|(
literal|"repository"
argument_list|,
literal|"alias:rep;desc:repository"
argument_list|)
operator|.
name|addCell
argument_list|(
literal|"snapshot"
argument_list|,
literal|"alias:snap;desc:snapshot"
argument_list|)
operator|.
name|addCell
argument_list|(
literal|"files"
argument_list|,
literal|"alias:f;desc:number of files"
argument_list|)
operator|.
name|addCell
argument_list|(
literal|"files_percent"
argument_list|,
literal|"alias:fp;desc:percent of files recovered"
argument_list|)
operator|.
name|addCell
argument_list|(
literal|"bytes"
argument_list|,
literal|"alias:b;desc:size in bytes"
argument_list|)
operator|.
name|addCell
argument_list|(
literal|"bytes_percent"
argument_list|,
literal|"alias:bp;desc:percent of bytes recovered"
argument_list|)
operator|.
name|endHeaders
argument_list|()
expr_stmt|;
return|return
name|t
return|;
block|}
comment|/**      * buildRecoveryTable will build a table of recovery information suitable      * for displaying at the command line.      *      * @param request  A Rest request      * @param response A recovery status response      * @return A table containing index, shardId, node, target size, recovered size and percentage for each recovering replica      */
DECL|method|buildRecoveryTable
specifier|public
name|Table
name|buildRecoveryTable
parameter_list|(
name|RestRequest
name|request
parameter_list|,
name|RecoveryResponse
name|response
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
for|for
control|(
name|String
name|index
range|:
name|response
operator|.
name|shardResponses
argument_list|()
operator|.
name|keySet
argument_list|()
control|)
block|{
name|List
argument_list|<
name|ShardRecoveryResponse
argument_list|>
name|shardRecoveryResponses
init|=
name|response
operator|.
name|shardResponses
argument_list|()
operator|.
name|get
argument_list|(
name|index
argument_list|)
decl_stmt|;
if|if
condition|(
name|shardRecoveryResponses
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
continue|continue;
block|}
comment|// Sort ascending by shard id for readability
name|CollectionUtil
operator|.
name|introSort
argument_list|(
name|shardRecoveryResponses
argument_list|,
operator|new
name|Comparator
argument_list|<
name|ShardRecoveryResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|int
name|compare
parameter_list|(
name|ShardRecoveryResponse
name|o1
parameter_list|,
name|ShardRecoveryResponse
name|o2
parameter_list|)
block|{
name|int
name|id1
init|=
name|o1
operator|.
name|recoveryState
argument_list|()
operator|.
name|getShardId
argument_list|()
operator|.
name|id
argument_list|()
decl_stmt|;
name|int
name|id2
init|=
name|o2
operator|.
name|recoveryState
argument_list|()
operator|.
name|getShardId
argument_list|()
operator|.
name|id
argument_list|()
decl_stmt|;
if|if
condition|(
name|id1
operator|<
name|id2
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
elseif|else
if|if
condition|(
name|id1
operator|>
name|id2
condition|)
block|{
return|return
literal|1
return|;
block|}
else|else
block|{
return|return
literal|0
return|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
for|for
control|(
name|ShardRecoveryResponse
name|shardResponse
range|:
name|shardRecoveryResponses
control|)
block|{
name|RecoveryState
name|state
init|=
name|shardResponse
operator|.
name|recoveryState
argument_list|()
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
name|index
argument_list|)
expr_stmt|;
name|t
operator|.
name|addCell
argument_list|(
name|shardResponse
operator|.
name|getShardId
argument_list|()
argument_list|)
expr_stmt|;
name|t
operator|.
name|addCell
argument_list|(
name|state
operator|.
name|getTimer
argument_list|()
operator|.
name|time
argument_list|()
argument_list|)
expr_stmt|;
name|t
operator|.
name|addCell
argument_list|(
name|state
operator|.
name|getType
argument_list|()
operator|.
name|toString
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
name|state
operator|.
name|getStage
argument_list|()
operator|.
name|toString
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
name|state
operator|.
name|getSourceNode
argument_list|()
operator|==
literal|null
condition|?
literal|"n/a"
else|:
name|state
operator|.
name|getSourceNode
argument_list|()
operator|.
name|getHostName
argument_list|()
argument_list|)
expr_stmt|;
name|t
operator|.
name|addCell
argument_list|(
name|state
operator|.
name|getTargetNode
argument_list|()
operator|.
name|getHostName
argument_list|()
argument_list|)
expr_stmt|;
name|t
operator|.
name|addCell
argument_list|(
name|state
operator|.
name|getRestoreSource
argument_list|()
operator|==
literal|null
condition|?
literal|"n/a"
else|:
name|state
operator|.
name|getRestoreSource
argument_list|()
operator|.
name|snapshotId
argument_list|()
operator|.
name|getRepository
argument_list|()
argument_list|)
expr_stmt|;
name|t
operator|.
name|addCell
argument_list|(
name|state
operator|.
name|getRestoreSource
argument_list|()
operator|==
literal|null
condition|?
literal|"n/a"
else|:
name|state
operator|.
name|getRestoreSource
argument_list|()
operator|.
name|snapshotId
argument_list|()
operator|.
name|getSnapshot
argument_list|()
argument_list|)
expr_stmt|;
name|t
operator|.
name|addCell
argument_list|(
name|state
operator|.
name|getIndex
argument_list|()
operator|.
name|totalFileCount
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
name|state
operator|.
name|getIndex
argument_list|()
operator|.
name|percentFilesRecovered
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|t
operator|.
name|addCell
argument_list|(
name|state
operator|.
name|getIndex
argument_list|()
operator|.
name|totalByteCount
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
name|state
operator|.
name|getIndex
argument_list|()
operator|.
name|percentBytesRecovered
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|t
operator|.
name|endRow
argument_list|()
expr_stmt|;
block|}
block|}
return|return
name|t
return|;
block|}
block|}
end_class

end_unit

