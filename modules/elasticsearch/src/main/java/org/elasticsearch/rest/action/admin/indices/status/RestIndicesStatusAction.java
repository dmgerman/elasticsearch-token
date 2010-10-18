begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.rest.action.admin.indices.status
package|package
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
name|indices
operator|.
name|status
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
name|indices
operator|.
name|status
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
name|action
operator|.
name|support
operator|.
name|broadcast
operator|.
name|BroadcastOperationThreading
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
name|common
operator|.
name|settings
operator|.
name|SettingsFilter
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
name|RestXContentBuilder
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
name|*
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
name|RestResponse
operator|.
name|Status
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
name|rest
operator|.
name|action
operator|.
name|support
operator|.
name|RestActions
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * @author kimchy (Shay Banon)  */
end_comment

begin_class
DECL|class|RestIndicesStatusAction
specifier|public
class|class
name|RestIndicesStatusAction
extends|extends
name|BaseRestHandler
block|{
DECL|field|settingsFilter
specifier|private
specifier|final
name|SettingsFilter
name|settingsFilter
decl_stmt|;
DECL|method|RestIndicesStatusAction
annotation|@
name|Inject
specifier|public
name|RestIndicesStatusAction
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|Client
name|client
parameter_list|,
name|RestController
name|controller
parameter_list|,
name|SettingsFilter
name|settingsFilter
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
literal|"/_status"
argument_list|,
name|this
argument_list|)
expr_stmt|;
name|controller
operator|.
name|registerHandler
argument_list|(
name|GET
argument_list|,
literal|"/{index}/_status"
argument_list|,
name|this
argument_list|)
expr_stmt|;
name|this
operator|.
name|settingsFilter
operator|=
name|settingsFilter
expr_stmt|;
block|}
DECL|method|handleRequest
annotation|@
name|Override
specifier|public
name|void
name|handleRequest
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
name|IndicesStatusRequest
name|indicesStatusRequest
init|=
operator|new
name|IndicesStatusRequest
argument_list|(
name|splitIndices
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
comment|// we just send back a response, no need to fork a listener
name|indicesStatusRequest
operator|.
name|listenerThreaded
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|BroadcastOperationThreading
name|operationThreading
init|=
name|BroadcastOperationThreading
operator|.
name|fromString
argument_list|(
name|request
operator|.
name|param
argument_list|(
literal|"operation_threading"
argument_list|)
argument_list|,
name|BroadcastOperationThreading
operator|.
name|SINGLE_THREAD
argument_list|)
decl_stmt|;
if|if
condition|(
name|operationThreading
operator|==
name|BroadcastOperationThreading
operator|.
name|NO_THREADS
condition|)
block|{
comment|// since we don't spawn, don't allow no_threads, but change it to a single thread
name|operationThreading
operator|=
name|BroadcastOperationThreading
operator|.
name|SINGLE_THREAD
expr_stmt|;
block|}
name|indicesStatusRequest
operator|.
name|operationThreading
argument_list|(
name|operationThreading
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
name|status
argument_list|(
name|indicesStatusRequest
argument_list|,
operator|new
name|ActionListener
argument_list|<
name|IndicesStatusResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onResponse
parameter_list|(
name|IndicesStatusResponse
name|response
parameter_list|)
block|{
try|try
block|{
name|XContentBuilder
name|builder
init|=
name|RestXContentBuilder
operator|.
name|restContentBuilder
argument_list|(
name|request
argument_list|)
decl_stmt|;
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"ok"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|buildBroadcastShardsHeader
argument_list|(
name|builder
argument_list|,
name|response
argument_list|)
expr_stmt|;
name|builder
operator|.
name|startObject
argument_list|(
literal|"indices"
argument_list|)
expr_stmt|;
for|for
control|(
name|IndexStatus
name|indexStatus
range|:
name|response
operator|.
name|indices
argument_list|()
operator|.
name|values
argument_list|()
control|)
block|{
name|builder
operator|.
name|startObject
argument_list|(
name|indexStatus
operator|.
name|index
argument_list|()
argument_list|,
name|XContentBuilder
operator|.
name|FieldCaseConversion
operator|.
name|NONE
argument_list|)
expr_stmt|;
name|builder
operator|.
name|array
argument_list|(
literal|"aliases"
argument_list|,
name|indexStatus
operator|.
name|settings
argument_list|()
operator|.
name|getAsArray
argument_list|(
literal|"index.aliases"
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|.
name|startObject
argument_list|(
literal|"settings"
argument_list|)
expr_stmt|;
name|Settings
name|settings
init|=
name|settingsFilter
operator|.
name|filterSettings
argument_list|(
name|indexStatus
operator|.
name|settings
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|entry
range|:
name|settings
operator|.
name|getAsMap
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
if|if
condition|(
name|indexStatus
operator|.
name|storeSize
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"store_size"
argument_list|,
name|indexStatus
operator|.
name|storeSize
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"store_size_in_bytes"
argument_list|,
name|indexStatus
operator|.
name|storeSize
argument_list|()
operator|.
name|bytes
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|indexStatus
operator|.
name|translogOperations
argument_list|()
operator|!=
operator|-
literal|1
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"translog_operations"
argument_list|,
name|indexStatus
operator|.
name|translogOperations
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|indexStatus
operator|.
name|docs
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|startObject
argument_list|(
literal|"docs"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"num_docs"
argument_list|,
name|indexStatus
operator|.
name|docs
argument_list|()
operator|.
name|numDocs
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"max_doc"
argument_list|,
name|indexStatus
operator|.
name|docs
argument_list|()
operator|.
name|maxDoc
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"deleted_docs"
argument_list|,
name|indexStatus
operator|.
name|docs
argument_list|()
operator|.
name|deletedDocs
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
name|builder
operator|.
name|startObject
argument_list|(
literal|"shards"
argument_list|)
expr_stmt|;
for|for
control|(
name|IndexShardStatus
name|indexShardStatus
range|:
name|indexStatus
control|)
block|{
name|builder
operator|.
name|startArray
argument_list|(
name|Integer
operator|.
name|toString
argument_list|(
name|indexShardStatus
operator|.
name|shardId
argument_list|()
operator|.
name|id
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|ShardStatus
name|shardStatus
range|:
name|indexShardStatus
control|)
block|{
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|startObject
argument_list|(
literal|"routing"
argument_list|)
operator|.
name|field
argument_list|(
literal|"state"
argument_list|,
name|shardStatus
operator|.
name|shardRouting
argument_list|()
operator|.
name|state
argument_list|()
argument_list|)
operator|.
name|field
argument_list|(
literal|"primary"
argument_list|,
name|shardStatus
operator|.
name|shardRouting
argument_list|()
operator|.
name|primary
argument_list|()
argument_list|)
operator|.
name|field
argument_list|(
literal|"node"
argument_list|,
name|shardStatus
operator|.
name|shardRouting
argument_list|()
operator|.
name|currentNodeId
argument_list|()
argument_list|)
operator|.
name|field
argument_list|(
literal|"relocating_node"
argument_list|,
name|shardStatus
operator|.
name|shardRouting
argument_list|()
operator|.
name|relocatingNodeId
argument_list|()
argument_list|)
operator|.
name|field
argument_list|(
literal|"shard"
argument_list|,
name|shardStatus
operator|.
name|shardRouting
argument_list|()
operator|.
name|shardId
argument_list|()
operator|.
name|id
argument_list|()
argument_list|)
operator|.
name|field
argument_list|(
literal|"index"
argument_list|,
name|shardStatus
operator|.
name|shardRouting
argument_list|()
operator|.
name|shardId
argument_list|()
operator|.
name|index
argument_list|()
operator|.
name|name
argument_list|()
argument_list|)
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"state"
argument_list|,
name|shardStatus
operator|.
name|state
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|shardStatus
operator|.
name|storeSize
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|startObject
argument_list|(
literal|"index"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"size"
argument_list|,
name|shardStatus
operator|.
name|storeSize
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"size_in_bytes"
argument_list|,
name|shardStatus
operator|.
name|storeSize
argument_list|()
operator|.
name|bytes
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|shardStatus
operator|.
name|translogId
argument_list|()
operator|!=
operator|-
literal|1
condition|)
block|{
name|builder
operator|.
name|startObject
argument_list|(
literal|"translog"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"id"
argument_list|,
name|shardStatus
operator|.
name|translogId
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"operations"
argument_list|,
name|shardStatus
operator|.
name|translogOperations
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|shardStatus
operator|.
name|docs
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|startObject
argument_list|(
literal|"docs"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"num_docs"
argument_list|,
name|shardStatus
operator|.
name|docs
argument_list|()
operator|.
name|numDocs
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"max_doc"
argument_list|,
name|shardStatus
operator|.
name|docs
argument_list|()
operator|.
name|maxDoc
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"deleted_docs"
argument_list|,
name|shardStatus
operator|.
name|docs
argument_list|()
operator|.
name|deletedDocs
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|shardStatus
operator|.
name|peerRecoveryStatus
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|PeerRecoveryStatus
name|peerRecoveryStatus
init|=
name|shardStatus
operator|.
name|peerRecoveryStatus
argument_list|()
decl_stmt|;
name|builder
operator|.
name|startObject
argument_list|(
literal|"peer_recovery"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"stage"
argument_list|,
name|peerRecoveryStatus
operator|.
name|stage
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"start_time_in_millis"
argument_list|,
name|peerRecoveryStatus
operator|.
name|startTime
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"time"
argument_list|,
name|peerRecoveryStatus
operator|.
name|time
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"time_in_millis"
argument_list|,
name|peerRecoveryStatus
operator|.
name|time
argument_list|()
operator|.
name|millis
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|startObject
argument_list|(
literal|"index"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"progress"
argument_list|,
name|peerRecoveryStatus
operator|.
name|indexRecoveryProgress
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"size"
argument_list|,
name|peerRecoveryStatus
operator|.
name|indexSize
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"size_in_bytes"
argument_list|,
name|peerRecoveryStatus
operator|.
name|indexSize
argument_list|()
operator|.
name|bytes
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"reused_size"
argument_list|,
name|peerRecoveryStatus
operator|.
name|reusedIndexSize
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"reused_size_in_bytes"
argument_list|,
name|peerRecoveryStatus
operator|.
name|reusedIndexSize
argument_list|()
operator|.
name|bytes
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"expected_recovered_size"
argument_list|,
name|peerRecoveryStatus
operator|.
name|expectedRecoveredIndexSize
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"expected_recovered_size_in_bytes"
argument_list|,
name|peerRecoveryStatus
operator|.
name|expectedRecoveredIndexSize
argument_list|()
operator|.
name|bytes
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"recovered_size"
argument_list|,
name|peerRecoveryStatus
operator|.
name|recoveredIndexSize
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"recovered_size_in_bytes"
argument_list|,
name|peerRecoveryStatus
operator|.
name|recoveredIndexSize
argument_list|()
operator|.
name|bytes
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|startObject
argument_list|(
literal|"translog"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"recovered"
argument_list|,
name|peerRecoveryStatus
operator|.
name|recoveredTranslogOperations
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|shardStatus
operator|.
name|gatewayRecoveryStatus
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|GatewayRecoveryStatus
name|gatewayRecoveryStatus
init|=
name|shardStatus
operator|.
name|gatewayRecoveryStatus
argument_list|()
decl_stmt|;
name|builder
operator|.
name|startObject
argument_list|(
literal|"gateway_recovery"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"stage"
argument_list|,
name|gatewayRecoveryStatus
operator|.
name|stage
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"start_time_in_millis"
argument_list|,
name|gatewayRecoveryStatus
operator|.
name|startTime
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"time"
argument_list|,
name|gatewayRecoveryStatus
operator|.
name|time
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"time_in_millis"
argument_list|,
name|gatewayRecoveryStatus
operator|.
name|time
argument_list|()
operator|.
name|millis
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|startObject
argument_list|(
literal|"index"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"progress"
argument_list|,
name|gatewayRecoveryStatus
operator|.
name|indexRecoveryProgress
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"size"
argument_list|,
name|gatewayRecoveryStatus
operator|.
name|indexSize
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"size_in_bytes"
argument_list|,
name|gatewayRecoveryStatus
operator|.
name|indexSize
argument_list|()
operator|.
name|bytes
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"reused_size"
argument_list|,
name|gatewayRecoveryStatus
operator|.
name|reusedIndexSize
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"reused_size_in_bytes"
argument_list|,
name|gatewayRecoveryStatus
operator|.
name|reusedIndexSize
argument_list|()
operator|.
name|bytes
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"expected_recovered_size"
argument_list|,
name|gatewayRecoveryStatus
operator|.
name|expectedRecoveredIndexSize
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"expected_recovered_size_in_bytes"
argument_list|,
name|gatewayRecoveryStatus
operator|.
name|expectedRecoveredIndexSize
argument_list|()
operator|.
name|bytes
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"recovered_size"
argument_list|,
name|gatewayRecoveryStatus
operator|.
name|recoveredIndexSize
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"recovered_size_in_bytes"
argument_list|,
name|gatewayRecoveryStatus
operator|.
name|recoveredIndexSize
argument_list|()
operator|.
name|bytes
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|startObject
argument_list|(
literal|"translog"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"recovered"
argument_list|,
name|gatewayRecoveryStatus
operator|.
name|recoveredTranslogOperations
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|shardStatus
operator|.
name|gatewaySnapshotStatus
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|GatewaySnapshotStatus
name|gatewaySnapshotStatus
init|=
name|shardStatus
operator|.
name|gatewaySnapshotStatus
argument_list|()
decl_stmt|;
name|builder
operator|.
name|startObject
argument_list|(
literal|"gateway_snapshot"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"stage"
argument_list|,
name|gatewaySnapshotStatus
operator|.
name|stage
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"start_time_in_millis"
argument_list|,
name|gatewaySnapshotStatus
operator|.
name|startTime
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"time"
argument_list|,
name|gatewaySnapshotStatus
operator|.
name|time
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"time_in_millis"
argument_list|,
name|gatewaySnapshotStatus
operator|.
name|time
argument_list|()
operator|.
name|millis
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|startObject
argument_list|(
literal|"index"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"size"
argument_list|,
name|gatewaySnapshotStatus
operator|.
name|indexSize
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"size_in_bytes"
argument_list|,
name|gatewaySnapshotStatus
operator|.
name|indexSize
argument_list|()
operator|.
name|bytes
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|startObject
argument_list|(
literal|"index"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"expected_operations"
argument_list|,
name|gatewaySnapshotStatus
operator|.
name|expectedNumberOfOperations
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
name|builder
operator|.
name|endArray
argument_list|()
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|channel
operator|.
name|sendResponse
argument_list|(
operator|new
name|XContentRestResponse
argument_list|(
name|request
argument_list|,
name|OK
argument_list|,
name|builder
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|onFailure
argument_list|(
name|e
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
name|XContentThrowableRestResponse
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
block|}
end_class

end_unit

