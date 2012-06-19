begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.gateway.local.state.shards
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|gateway
operator|.
name|local
operator|.
name|state
operator|.
name|shards
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Maps
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|io
operator|.
name|Closeables
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
name|ClusterChangedEvent
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
name|ClusterStateListener
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
name|routing
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
name|common
operator|.
name|Nullable
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
name|component
operator|.
name|AbstractComponent
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
name|FileSystemUtils
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
name|Streams
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
name|CachedStreamOutput
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
name|common
operator|.
name|xcontent
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
name|env
operator|.
name|NodeEnvironment
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|shard
operator|.
name|ShardId
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|File
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|FileInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|FileOutputStream
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
name|Iterator
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
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|LocalGatewayShardsState
specifier|public
class|class
name|LocalGatewayShardsState
extends|extends
name|AbstractComponent
implements|implements
name|ClusterStateListener
block|{
DECL|field|nodeEnv
specifier|private
specifier|final
name|NodeEnvironment
name|nodeEnv
decl_stmt|;
DECL|field|currentState
specifier|private
specifier|volatile
name|Map
argument_list|<
name|ShardId
argument_list|,
name|ShardStateInfo
argument_list|>
name|currentState
init|=
name|Maps
operator|.
name|newHashMap
argument_list|()
decl_stmt|;
annotation|@
name|Inject
DECL|method|LocalGatewayShardsState
specifier|public
name|LocalGatewayShardsState
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|NodeEnvironment
name|nodeEnv
parameter_list|,
name|TransportNodesListGatewayStartedShards
name|listGatewayStartedShards
parameter_list|)
throws|throws
name|Exception
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|nodeEnv
operator|=
name|nodeEnv
expr_stmt|;
name|listGatewayStartedShards
operator|.
name|initGateway
argument_list|(
name|this
argument_list|)
expr_stmt|;
if|if
condition|(
name|DiscoveryNode
operator|.
name|dataNode
argument_list|(
name|settings
argument_list|)
condition|)
block|{
try|try
block|{
name|pre019Upgrade
argument_list|()
expr_stmt|;
name|long
name|start
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|loadStartedShards
argument_list|()
expr_stmt|;
name|logger
operator|.
name|debug
argument_list|(
literal|"took {} to load started shards state"
argument_list|,
name|TimeValue
operator|.
name|timeValueMillis
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|start
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
name|logger
operator|.
name|error
argument_list|(
literal|"failed to read local state (started shards), exiting..."
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
name|e
throw|;
block|}
block|}
block|}
DECL|method|currentStartedShards
specifier|public
name|Map
argument_list|<
name|ShardId
argument_list|,
name|ShardStateInfo
argument_list|>
name|currentStartedShards
parameter_list|()
block|{
return|return
name|this
operator|.
name|currentState
return|;
block|}
annotation|@
name|Override
DECL|method|clusterChanged
specifier|public
name|void
name|clusterChanged
parameter_list|(
name|ClusterChangedEvent
name|event
parameter_list|)
block|{
if|if
condition|(
name|event
operator|.
name|state
argument_list|()
operator|.
name|blocks
argument_list|()
operator|.
name|disableStatePersistence
argument_list|()
condition|)
block|{
return|return;
block|}
if|if
condition|(
operator|!
name|event
operator|.
name|state
argument_list|()
operator|.
name|nodes
argument_list|()
operator|.
name|localNode
argument_list|()
operator|.
name|dataNode
argument_list|()
condition|)
block|{
return|return;
block|}
if|if
condition|(
operator|!
name|event
operator|.
name|routingTableChanged
argument_list|()
condition|)
block|{
return|return;
block|}
name|Map
argument_list|<
name|ShardId
argument_list|,
name|ShardStateInfo
argument_list|>
name|newState
init|=
name|Maps
operator|.
name|newHashMap
argument_list|()
decl_stmt|;
name|newState
operator|.
name|putAll
argument_list|(
name|this
operator|.
name|currentState
argument_list|)
expr_stmt|;
comment|// remove from the current state all the shards that are completely started somewhere, we won't need them anymore
comment|// and if they are still here, we will add them in the next phase
comment|// Also note, this works well when closing an index, since a closed index will have no routing shards entries
comment|// so they won't get removed (we want to keep the fact that those shards are allocated on this node if needed)
for|for
control|(
name|IndexRoutingTable
name|indexRoutingTable
range|:
name|event
operator|.
name|state
argument_list|()
operator|.
name|routingTable
argument_list|()
control|)
block|{
for|for
control|(
name|IndexShardRoutingTable
name|indexShardRoutingTable
range|:
name|indexRoutingTable
control|)
block|{
if|if
condition|(
name|indexShardRoutingTable
operator|.
name|countWithState
argument_list|(
name|ShardRoutingState
operator|.
name|STARTED
argument_list|)
operator|==
name|indexShardRoutingTable
operator|.
name|size
argument_list|()
condition|)
block|{
name|newState
operator|.
name|remove
argument_list|(
name|indexShardRoutingTable
operator|.
name|shardId
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|// remove deleted indices from the started shards
for|for
control|(
name|ShardId
name|shardId
range|:
name|currentState
operator|.
name|keySet
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|event
operator|.
name|state
argument_list|()
operator|.
name|metaData
argument_list|()
operator|.
name|hasIndex
argument_list|(
name|shardId
operator|.
name|index
argument_list|()
operator|.
name|name
argument_list|()
argument_list|)
condition|)
block|{
name|newState
operator|.
name|remove
argument_list|(
name|shardId
argument_list|)
expr_stmt|;
block|}
block|}
comment|// now, add all the ones that are active and on this node
name|RoutingNode
name|routingNode
init|=
name|event
operator|.
name|state
argument_list|()
operator|.
name|readOnlyRoutingNodes
argument_list|()
operator|.
name|node
argument_list|(
name|event
operator|.
name|state
argument_list|()
operator|.
name|nodes
argument_list|()
operator|.
name|localNodeId
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|routingNode
operator|!=
literal|null
condition|)
block|{
comment|// our node is not in play yet...
for|for
control|(
name|MutableShardRouting
name|shardRouting
range|:
name|routingNode
control|)
block|{
if|if
condition|(
name|shardRouting
operator|.
name|active
argument_list|()
condition|)
block|{
name|newState
operator|.
name|put
argument_list|(
name|shardRouting
operator|.
name|shardId
argument_list|()
argument_list|,
operator|new
name|ShardStateInfo
argument_list|(
name|shardRouting
operator|.
name|version
argument_list|()
argument_list|,
name|shardRouting
operator|.
name|primary
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|// go over the write started shards if needed
for|for
control|(
name|Iterator
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|ShardId
argument_list|,
name|ShardStateInfo
argument_list|>
argument_list|>
name|it
init|=
name|newState
operator|.
name|entrySet
argument_list|()
operator|.
name|iterator
argument_list|()
init|;
name|it
operator|.
name|hasNext
argument_list|()
condition|;
control|)
block|{
name|Map
operator|.
name|Entry
argument_list|<
name|ShardId
argument_list|,
name|ShardStateInfo
argument_list|>
name|entry
init|=
name|it
operator|.
name|next
argument_list|()
decl_stmt|;
name|ShardId
name|shardId
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|ShardStateInfo
name|shardStateInfo
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|String
name|writeReason
init|=
literal|null
decl_stmt|;
name|ShardStateInfo
name|currentShardStateInfo
init|=
name|currentState
operator|.
name|get
argument_list|(
name|shardId
argument_list|)
decl_stmt|;
if|if
condition|(
name|currentShardStateInfo
operator|==
literal|null
condition|)
block|{
name|writeReason
operator|=
literal|"freshly started, version ["
operator|+
name|shardStateInfo
operator|.
name|version
operator|+
literal|"]"
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|currentShardStateInfo
operator|.
name|version
operator|!=
name|shardStateInfo
operator|.
name|version
condition|)
block|{
name|writeReason
operator|=
literal|"version changed from ["
operator|+
name|currentShardStateInfo
operator|.
name|version
operator|+
literal|"] to ["
operator|+
name|shardStateInfo
operator|.
name|version
operator|+
literal|"]"
expr_stmt|;
block|}
comment|// we update the write reason if we really need to write a new one...
if|if
condition|(
name|writeReason
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
try|try
block|{
name|writeShardState
argument_list|(
name|writeReason
argument_list|,
name|shardId
argument_list|,
name|shardStateInfo
argument_list|,
name|currentShardStateInfo
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// we failed to write the shard state, remove it from our builder, we will try and write
comment|// it next time...
name|it
operator|.
name|remove
argument_list|()
expr_stmt|;
block|}
block|}
comment|// now, go over the current ones and delete ones that are not in the new one
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|ShardId
argument_list|,
name|ShardStateInfo
argument_list|>
name|entry
range|:
name|currentState
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|ShardId
name|shardId
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|newState
operator|.
name|containsKey
argument_list|(
name|shardId
argument_list|)
condition|)
block|{
name|deleteShardState
argument_list|(
name|shardId
argument_list|)
expr_stmt|;
block|}
block|}
name|this
operator|.
name|currentState
operator|=
name|newState
expr_stmt|;
block|}
DECL|method|loadStartedShards
specifier|private
name|void
name|loadStartedShards
parameter_list|()
throws|throws
name|Exception
block|{
name|Set
argument_list|<
name|ShardId
argument_list|>
name|shardIds
init|=
name|nodeEnv
operator|.
name|findAllShardIds
argument_list|()
decl_stmt|;
name|long
name|highestVersion
init|=
operator|-
literal|1
decl_stmt|;
name|Map
argument_list|<
name|ShardId
argument_list|,
name|ShardStateInfo
argument_list|>
name|shardsState
init|=
name|Maps
operator|.
name|newHashMap
argument_list|()
decl_stmt|;
for|for
control|(
name|ShardId
name|shardId
range|:
name|shardIds
control|)
block|{
name|long
name|highestShardVersion
init|=
operator|-
literal|1
decl_stmt|;
name|ShardStateInfo
name|highestShardState
init|=
literal|null
decl_stmt|;
for|for
control|(
name|File
name|shardLocation
range|:
name|nodeEnv
operator|.
name|shardLocations
argument_list|(
name|shardId
argument_list|)
control|)
block|{
name|File
name|shardStateDir
init|=
operator|new
name|File
argument_list|(
name|shardLocation
argument_list|,
literal|"_state"
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|shardStateDir
operator|.
name|exists
argument_list|()
operator|||
operator|!
name|shardStateDir
operator|.
name|isDirectory
argument_list|()
condition|)
block|{
continue|continue;
block|}
comment|// now, iterate over the current versions, and find latest one
name|File
index|[]
name|stateFiles
init|=
name|shardStateDir
operator|.
name|listFiles
argument_list|()
decl_stmt|;
if|if
condition|(
name|stateFiles
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
for|for
control|(
name|File
name|stateFile
range|:
name|stateFiles
control|)
block|{
if|if
condition|(
operator|!
name|stateFile
operator|.
name|getName
argument_list|()
operator|.
name|startsWith
argument_list|(
literal|"state-"
argument_list|)
condition|)
block|{
continue|continue;
block|}
try|try
block|{
name|long
name|version
init|=
name|Long
operator|.
name|parseLong
argument_list|(
name|stateFile
operator|.
name|getName
argument_list|()
operator|.
name|substring
argument_list|(
literal|"state-"
operator|.
name|length
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|version
operator|>
name|highestShardVersion
condition|)
block|{
name|byte
index|[]
name|data
init|=
name|Streams
operator|.
name|copyToByteArray
argument_list|(
operator|new
name|FileInputStream
argument_list|(
name|stateFile
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|data
operator|.
name|length
operator|==
literal|0
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"[{}][{}]: not data for ["
operator|+
name|stateFile
operator|.
name|getAbsolutePath
argument_list|()
operator|+
literal|"], ignoring..."
argument_list|,
name|shardId
operator|.
name|index
argument_list|()
operator|.
name|name
argument_list|()
argument_list|,
name|shardId
operator|.
name|id
argument_list|()
argument_list|)
expr_stmt|;
continue|continue;
block|}
name|ShardStateInfo
name|readState
init|=
name|readShardState
argument_list|(
name|data
argument_list|)
decl_stmt|;
if|if
condition|(
name|readState
operator|==
literal|null
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"[{}][{}]: not data for ["
operator|+
name|stateFile
operator|.
name|getAbsolutePath
argument_list|()
operator|+
literal|"], ignoring..."
argument_list|,
name|shardId
operator|.
name|index
argument_list|()
operator|.
name|name
argument_list|()
argument_list|,
name|shardId
operator|.
name|id
argument_list|()
argument_list|)
expr_stmt|;
continue|continue;
block|}
assert|assert
name|readState
operator|.
name|version
operator|==
name|version
assert|;
name|highestShardState
operator|=
name|readState
expr_stmt|;
name|highestShardVersion
operator|=
name|version
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"[{}][{}]: failed to read ["
operator|+
name|stateFile
operator|.
name|getAbsolutePath
argument_list|()
operator|+
literal|"], ignoring..."
argument_list|,
name|e
argument_list|,
name|shardId
operator|.
name|index
argument_list|()
operator|.
name|name
argument_list|()
argument_list|,
name|shardId
operator|.
name|id
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|// did we find a state file?
if|if
condition|(
name|highestShardState
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
name|shardsState
operator|.
name|put
argument_list|(
name|shardId
argument_list|,
name|highestShardState
argument_list|)
expr_stmt|;
comment|// update the global version
if|if
condition|(
name|highestShardVersion
operator|>
name|highestVersion
condition|)
block|{
name|highestVersion
operator|=
name|highestShardVersion
expr_stmt|;
block|}
block|}
comment|// update the current started shards only if there is data there...
if|if
condition|(
name|highestVersion
operator|!=
operator|-
literal|1
condition|)
block|{
name|currentState
operator|=
name|shardsState
expr_stmt|;
block|}
block|}
annotation|@
name|Nullable
DECL|method|readShardState
specifier|private
name|ShardStateInfo
name|readShardState
parameter_list|(
name|byte
index|[]
name|data
parameter_list|)
throws|throws
name|Exception
block|{
name|XContentParser
name|parser
init|=
literal|null
decl_stmt|;
try|try
block|{
name|parser
operator|=
name|XContentHelper
operator|.
name|createParser
argument_list|(
name|data
argument_list|,
literal|0
argument_list|,
name|data
operator|.
name|length
argument_list|)
expr_stmt|;
name|XContentParser
operator|.
name|Token
name|token
init|=
name|parser
operator|.
name|nextToken
argument_list|()
decl_stmt|;
if|if
condition|(
name|token
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|long
name|version
init|=
operator|-
literal|1
decl_stmt|;
name|Boolean
name|primary
init|=
literal|null
decl_stmt|;
name|String
name|currentFieldName
init|=
literal|null
decl_stmt|;
while|while
condition|(
operator|(
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
operator|)
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|END_OBJECT
condition|)
block|{
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|FIELD_NAME
condition|)
block|{
name|currentFieldName
operator|=
name|parser
operator|.
name|currentName
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|token
operator|.
name|isValue
argument_list|()
condition|)
block|{
if|if
condition|(
literal|"version"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|version
operator|=
name|parser
operator|.
name|longValue
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"primary"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|primary
operator|=
name|parser
operator|.
name|booleanValue
argument_list|()
expr_stmt|;
block|}
block|}
block|}
return|return
operator|new
name|ShardStateInfo
argument_list|(
name|version
argument_list|,
name|primary
argument_list|)
return|;
block|}
finally|finally
block|{
if|if
condition|(
name|parser
operator|!=
literal|null
condition|)
block|{
name|parser
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
DECL|method|writeShardState
specifier|private
name|void
name|writeShardState
parameter_list|(
name|String
name|reason
parameter_list|,
name|ShardId
name|shardId
parameter_list|,
name|ShardStateInfo
name|shardStateInfo
parameter_list|,
annotation|@
name|Nullable
name|ShardStateInfo
name|previousStateInfo
parameter_list|)
throws|throws
name|Exception
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"[{}][{}] writing shard state, reason [{}]"
argument_list|,
name|shardId
operator|.
name|index
argument_list|()
operator|.
name|name
argument_list|()
argument_list|,
name|shardId
operator|.
name|id
argument_list|()
argument_list|,
name|reason
argument_list|)
expr_stmt|;
name|CachedStreamOutput
operator|.
name|Entry
name|cachedEntry
init|=
name|CachedStreamOutput
operator|.
name|popEntry
argument_list|()
decl_stmt|;
try|try
block|{
name|XContentBuilder
name|builder
init|=
name|XContentFactory
operator|.
name|contentBuilder
argument_list|(
name|XContentType
operator|.
name|JSON
argument_list|,
name|cachedEntry
operator|.
name|cachedBytes
argument_list|()
argument_list|)
decl_stmt|;
name|builder
operator|.
name|prettyPrint
argument_list|()
expr_stmt|;
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"version"
argument_list|,
name|shardStateInfo
operator|.
name|version
argument_list|)
expr_stmt|;
if|if
condition|(
name|shardStateInfo
operator|.
name|primary
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"primary"
argument_list|,
name|shardStateInfo
operator|.
name|primary
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|flush
argument_list|()
expr_stmt|;
name|Exception
name|lastFailure
init|=
literal|null
decl_stmt|;
name|boolean
name|wroteAtLeastOnce
init|=
literal|false
decl_stmt|;
for|for
control|(
name|File
name|shardLocation
range|:
name|nodeEnv
operator|.
name|shardLocations
argument_list|(
name|shardId
argument_list|)
control|)
block|{
name|File
name|shardStateDir
init|=
operator|new
name|File
argument_list|(
name|shardLocation
argument_list|,
literal|"_state"
argument_list|)
decl_stmt|;
name|FileSystemUtils
operator|.
name|mkdirs
argument_list|(
name|shardStateDir
argument_list|)
expr_stmt|;
name|File
name|stateFile
init|=
operator|new
name|File
argument_list|(
name|shardStateDir
argument_list|,
literal|"state-"
operator|+
name|shardStateInfo
operator|.
name|version
argument_list|)
decl_stmt|;
name|FileOutputStream
name|fos
init|=
literal|null
decl_stmt|;
try|try
block|{
name|fos
operator|=
operator|new
name|FileOutputStream
argument_list|(
name|stateFile
argument_list|)
expr_stmt|;
name|fos
operator|.
name|write
argument_list|(
name|cachedEntry
operator|.
name|bytes
argument_list|()
operator|.
name|underlyingBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|cachedEntry
operator|.
name|bytes
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|fos
operator|.
name|getChannel
argument_list|()
operator|.
name|force
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|Closeables
operator|.
name|closeQuietly
argument_list|(
name|fos
argument_list|)
expr_stmt|;
name|wroteAtLeastOnce
operator|=
literal|true
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|lastFailure
operator|=
name|e
expr_stmt|;
block|}
finally|finally
block|{
name|Closeables
operator|.
name|closeQuietly
argument_list|(
name|fos
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
operator|!
name|wroteAtLeastOnce
condition|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"[{}][{}]: failed to write shard state"
argument_list|,
name|shardId
operator|.
name|index
argument_list|()
operator|.
name|name
argument_list|()
argument_list|,
name|shardId
operator|.
name|id
argument_list|()
argument_list|,
name|lastFailure
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
literal|"failed to write shard state for "
operator|+
name|shardId
argument_list|,
name|lastFailure
argument_list|)
throw|;
block|}
comment|// delete the old files
if|if
condition|(
name|previousStateInfo
operator|!=
literal|null
operator|&&
name|previousStateInfo
operator|.
name|version
operator|!=
name|shardStateInfo
operator|.
name|version
condition|)
block|{
for|for
control|(
name|File
name|shardLocation
range|:
name|nodeEnv
operator|.
name|shardLocations
argument_list|(
name|shardId
argument_list|)
control|)
block|{
name|File
name|stateFile
init|=
operator|new
name|File
argument_list|(
operator|new
name|File
argument_list|(
name|shardLocation
argument_list|,
literal|"_state"
argument_list|)
argument_list|,
literal|"state-"
operator|+
name|previousStateInfo
operator|.
name|version
argument_list|)
decl_stmt|;
name|stateFile
operator|.
name|delete
argument_list|()
expr_stmt|;
block|}
block|}
block|}
finally|finally
block|{
name|CachedStreamOutput
operator|.
name|pushEntry
argument_list|(
name|cachedEntry
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|deleteShardState
specifier|private
name|void
name|deleteShardState
parameter_list|(
name|ShardId
name|shardId
parameter_list|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"[{}][{}] delete shard state"
argument_list|,
name|shardId
operator|.
name|index
argument_list|()
operator|.
name|name
argument_list|()
argument_list|,
name|shardId
operator|.
name|id
argument_list|()
argument_list|)
expr_stmt|;
name|File
index|[]
name|shardLocations
init|=
name|nodeEnv
operator|.
name|shardLocations
argument_list|(
name|shardId
argument_list|)
decl_stmt|;
for|for
control|(
name|File
name|shardLocation
range|:
name|shardLocations
control|)
block|{
if|if
condition|(
operator|!
name|shardLocation
operator|.
name|exists
argument_list|()
condition|)
block|{
continue|continue;
block|}
name|FileSystemUtils
operator|.
name|deleteRecursively
argument_list|(
operator|new
name|File
argument_list|(
name|shardLocation
argument_list|,
literal|"_state"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|pre019Upgrade
specifier|private
name|void
name|pre019Upgrade
parameter_list|()
throws|throws
name|Exception
block|{
name|long
name|index
init|=
operator|-
literal|1
decl_stmt|;
name|File
name|latest
init|=
literal|null
decl_stmt|;
for|for
control|(
name|File
name|dataLocation
range|:
name|nodeEnv
operator|.
name|nodeDataLocations
argument_list|()
control|)
block|{
name|File
name|stateLocation
init|=
operator|new
name|File
argument_list|(
name|dataLocation
argument_list|,
literal|"_state"
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|stateLocation
operator|.
name|exists
argument_list|()
condition|)
block|{
continue|continue;
block|}
name|File
index|[]
name|stateFiles
init|=
name|stateLocation
operator|.
name|listFiles
argument_list|()
decl_stmt|;
if|if
condition|(
name|stateFiles
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
for|for
control|(
name|File
name|stateFile
range|:
name|stateFiles
control|)
block|{
if|if
condition|(
name|logger
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"[find_latest_state]: processing ["
operator|+
name|stateFile
operator|.
name|getName
argument_list|()
operator|+
literal|"]"
argument_list|)
expr_stmt|;
block|}
name|String
name|name
init|=
name|stateFile
operator|.
name|getName
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|name
operator|.
name|startsWith
argument_list|(
literal|"shards-"
argument_list|)
condition|)
block|{
continue|continue;
block|}
name|long
name|fileIndex
init|=
name|Long
operator|.
name|parseLong
argument_list|(
name|name
operator|.
name|substring
argument_list|(
name|name
operator|.
name|indexOf
argument_list|(
literal|'-'
argument_list|)
operator|+
literal|1
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|fileIndex
operator|>=
name|index
condition|)
block|{
comment|// try and read the meta data
try|try
block|{
name|byte
index|[]
name|data
init|=
name|Streams
operator|.
name|copyToByteArray
argument_list|(
operator|new
name|FileInputStream
argument_list|(
name|stateFile
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|data
operator|.
name|length
operator|==
literal|0
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"[upgrade]: not data for ["
operator|+
name|name
operator|+
literal|"], ignoring..."
argument_list|)
expr_stmt|;
block|}
name|pre09ReadState
argument_list|(
name|data
argument_list|)
expr_stmt|;
name|index
operator|=
name|fileIndex
expr_stmt|;
name|latest
operator|=
name|stateFile
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"[upgrade]: failed to read state from ["
operator|+
name|name
operator|+
literal|"], ignoring..."
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
if|if
condition|(
name|latest
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|logger
operator|.
name|info
argument_list|(
literal|"found old shards state, loading started shards from [{}] and converting to new shards state locations..."
argument_list|,
name|latest
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|ShardId
argument_list|,
name|ShardStateInfo
argument_list|>
name|shardsState
init|=
name|pre09ReadState
argument_list|(
name|Streams
operator|.
name|copyToByteArray
argument_list|(
operator|new
name|FileInputStream
argument_list|(
name|latest
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|ShardId
argument_list|,
name|ShardStateInfo
argument_list|>
name|entry
range|:
name|shardsState
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|writeShardState
argument_list|(
literal|"upgrade"
argument_list|,
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
comment|// rename shards state to backup state
name|File
name|backupFile
init|=
operator|new
name|File
argument_list|(
name|latest
operator|.
name|getParentFile
argument_list|()
argument_list|,
literal|"backup-"
operator|+
name|latest
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|latest
operator|.
name|renameTo
argument_list|(
name|backupFile
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"failed to rename old state to backup state ["
operator|+
name|latest
operator|.
name|getAbsolutePath
argument_list|()
operator|+
literal|"]"
argument_list|)
throw|;
block|}
comment|// delete all other shards state files
for|for
control|(
name|File
name|dataLocation
range|:
name|nodeEnv
operator|.
name|nodeDataLocations
argument_list|()
control|)
block|{
name|File
name|stateLocation
init|=
operator|new
name|File
argument_list|(
name|dataLocation
argument_list|,
literal|"_state"
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|stateLocation
operator|.
name|exists
argument_list|()
condition|)
block|{
continue|continue;
block|}
name|File
index|[]
name|stateFiles
init|=
name|stateLocation
operator|.
name|listFiles
argument_list|()
decl_stmt|;
if|if
condition|(
name|stateFiles
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
for|for
control|(
name|File
name|stateFile
range|:
name|stateFiles
control|)
block|{
name|String
name|name
init|=
name|stateFile
operator|.
name|getName
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|name
operator|.
name|startsWith
argument_list|(
literal|"shards-"
argument_list|)
condition|)
block|{
continue|continue;
block|}
name|stateFile
operator|.
name|delete
argument_list|()
expr_stmt|;
block|}
block|}
name|logger
operator|.
name|info
argument_list|(
literal|"conversion to new shards state location and format done, backup create at [{}]"
argument_list|,
name|backupFile
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|pre09ReadState
specifier|private
name|Map
argument_list|<
name|ShardId
argument_list|,
name|ShardStateInfo
argument_list|>
name|pre09ReadState
parameter_list|(
name|byte
index|[]
name|data
parameter_list|)
throws|throws
name|IOException
block|{
name|XContentParser
name|parser
init|=
literal|null
decl_stmt|;
try|try
block|{
name|Map
argument_list|<
name|ShardId
argument_list|,
name|ShardStateInfo
argument_list|>
name|shardsState
init|=
name|Maps
operator|.
name|newHashMap
argument_list|()
decl_stmt|;
name|parser
operator|=
name|XContentHelper
operator|.
name|createParser
argument_list|(
name|data
argument_list|,
literal|0
argument_list|,
name|data
operator|.
name|length
argument_list|)
expr_stmt|;
name|String
name|currentFieldName
init|=
literal|null
decl_stmt|;
name|XContentParser
operator|.
name|Token
name|token
init|=
name|parser
operator|.
name|nextToken
argument_list|()
decl_stmt|;
if|if
condition|(
name|token
operator|==
literal|null
condition|)
block|{
comment|// no data...
return|return
name|shardsState
return|;
block|}
while|while
condition|(
operator|(
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
operator|)
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|END_OBJECT
condition|)
block|{
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|FIELD_NAME
condition|)
block|{
name|currentFieldName
operator|=
name|parser
operator|.
name|currentName
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|START_ARRAY
condition|)
block|{
if|if
condition|(
literal|"shards"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
while|while
condition|(
operator|(
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
operator|)
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|END_ARRAY
condition|)
block|{
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|START_OBJECT
condition|)
block|{
name|String
name|shardIndex
init|=
literal|null
decl_stmt|;
name|int
name|shardId
init|=
operator|-
literal|1
decl_stmt|;
name|long
name|version
init|=
operator|-
literal|1
decl_stmt|;
while|while
condition|(
operator|(
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
operator|)
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|END_OBJECT
condition|)
block|{
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|FIELD_NAME
condition|)
block|{
name|currentFieldName
operator|=
name|parser
operator|.
name|currentName
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|token
operator|.
name|isValue
argument_list|()
condition|)
block|{
if|if
condition|(
literal|"index"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|shardIndex
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"id"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|shardId
operator|=
name|parser
operator|.
name|intValue
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"version"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|version
operator|=
name|parser
operator|.
name|longValue
argument_list|()
expr_stmt|;
block|}
block|}
block|}
name|shardsState
operator|.
name|put
argument_list|(
operator|new
name|ShardId
argument_list|(
name|shardIndex
argument_list|,
name|shardId
argument_list|)
argument_list|,
operator|new
name|ShardStateInfo
argument_list|(
name|version
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
return|return
name|shardsState
return|;
block|}
finally|finally
block|{
if|if
condition|(
name|parser
operator|!=
literal|null
condition|)
block|{
name|parser
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

