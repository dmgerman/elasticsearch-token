begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.gateway
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|gateway
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
name|BaseNodeResponse
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
name|BaseNodesRequest
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
name|BaseNodesResponse
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
name|ClusterState
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
name|IndexMetaData
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
name|Index
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
name|IndexSettings
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
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|shard
operator|.
name|ShardPath
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
name|ShardStateMetaData
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
name|store
operator|.
name|Store
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
name|Collections
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
comment|/**  * This transport action is used to fetch the shard version from each node during primary allocation in {@link GatewayAllocator}.  * We use this to find out which node holds the latest shard version and which of them used to be a primary in order to allocate  * shards after node or cluster restarts.  */
end_comment

begin_class
DECL|class|TransportNodesListGatewayStartedShards
specifier|public
class|class
name|TransportNodesListGatewayStartedShards
extends|extends
name|TransportNodesAction
argument_list|<
name|TransportNodesListGatewayStartedShards
operator|.
name|Request
argument_list|,
name|TransportNodesListGatewayStartedShards
operator|.
name|NodesGatewayStartedShards
argument_list|,
name|TransportNodesListGatewayStartedShards
operator|.
name|NodeRequest
argument_list|,
name|TransportNodesListGatewayStartedShards
operator|.
name|NodeGatewayStartedShards
argument_list|>
implements|implements
name|AsyncShardFetch
operator|.
name|List
argument_list|<
name|TransportNodesListGatewayStartedShards
operator|.
name|NodesGatewayStartedShards
argument_list|,
name|TransportNodesListGatewayStartedShards
operator|.
name|NodeGatewayStartedShards
argument_list|>
block|{
DECL|field|ACTION_NAME
specifier|public
specifier|static
specifier|final
name|String
name|ACTION_NAME
init|=
literal|"internal:gateway/local/started_shards"
decl_stmt|;
DECL|field|nodeEnv
specifier|private
specifier|final
name|NodeEnvironment
name|nodeEnv
decl_stmt|;
annotation|@
name|Inject
DECL|method|TransportNodesListGatewayStartedShards
specifier|public
name|TransportNodesListGatewayStartedShards
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
parameter_list|,
name|NodeEnvironment
name|env
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|ACTION_NAME
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
name|Request
operator|::
operator|new
argument_list|,
name|NodeRequest
operator|::
operator|new
argument_list|,
name|ThreadPool
operator|.
name|Names
operator|.
name|FETCH_SHARD_STARTED
argument_list|)
expr_stmt|;
name|this
operator|.
name|nodeEnv
operator|=
name|env
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|list
specifier|public
name|void
name|list
parameter_list|(
name|ShardId
name|shardId
parameter_list|,
name|IndexMetaData
name|indexMetaData
parameter_list|,
name|String
index|[]
name|nodesIds
parameter_list|,
name|ActionListener
argument_list|<
name|NodesGatewayStartedShards
argument_list|>
name|listener
parameter_list|)
block|{
name|execute
argument_list|(
operator|new
name|Request
argument_list|(
name|shardId
argument_list|,
name|indexMetaData
operator|.
name|getIndexUUID
argument_list|()
argument_list|,
name|nodesIds
argument_list|)
argument_list|,
name|listener
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|resolveNodes
specifier|protected
name|String
index|[]
name|resolveNodes
parameter_list|(
name|Request
name|request
parameter_list|,
name|ClusterState
name|clusterState
parameter_list|)
block|{
comment|// default implementation may filter out non existent nodes. it's important to keep exactly the ids
comment|// we were given for accounting on the caller
return|return
name|request
operator|.
name|nodesIds
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|transportCompress
specifier|protected
name|boolean
name|transportCompress
parameter_list|()
block|{
return|return
literal|true
return|;
comment|// this can become big...
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
name|Request
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
name|NodeGatewayStartedShards
name|newNodeResponse
parameter_list|()
block|{
return|return
operator|new
name|NodeGatewayStartedShards
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|newResponse
specifier|protected
name|NodesGatewayStartedShards
name|newResponse
parameter_list|(
name|Request
name|request
parameter_list|,
name|AtomicReferenceArray
name|responses
parameter_list|)
block|{
specifier|final
name|List
argument_list|<
name|NodeGatewayStartedShards
argument_list|>
name|nodesList
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
specifier|final
name|List
argument_list|<
name|FailedNodeException
argument_list|>
name|failures
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
name|NodeGatewayStartedShards
condition|)
block|{
comment|// will also filter out null response for unallocated ones
name|nodesList
operator|.
name|add
argument_list|(
operator|(
name|NodeGatewayStartedShards
operator|)
name|resp
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|resp
operator|instanceof
name|FailedNodeException
condition|)
block|{
name|failures
operator|.
name|add
argument_list|(
operator|(
name|FailedNodeException
operator|)
name|resp
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"unknown response type [{}], expected NodeLocalGatewayStartedShards or FailedNodeException"
argument_list|,
name|resp
argument_list|)
expr_stmt|;
block|}
block|}
return|return
operator|new
name|NodesGatewayStartedShards
argument_list|(
name|clusterName
argument_list|,
name|nodesList
operator|.
name|toArray
argument_list|(
operator|new
name|NodeGatewayStartedShards
index|[
name|nodesList
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|,
name|failures
operator|.
name|toArray
argument_list|(
operator|new
name|FailedNodeException
index|[
name|failures
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
DECL|method|nodeOperation
specifier|protected
name|NodeGatewayStartedShards
name|nodeOperation
parameter_list|(
name|NodeRequest
name|request
parameter_list|)
block|{
try|try
block|{
specifier|final
name|ShardId
name|shardId
init|=
name|request
operator|.
name|getShardId
argument_list|()
decl_stmt|;
specifier|final
name|String
name|indexUUID
init|=
name|request
operator|.
name|getIndexUUID
argument_list|()
decl_stmt|;
name|logger
operator|.
name|trace
argument_list|(
literal|"{} loading local shard state info"
argument_list|,
name|shardId
argument_list|)
expr_stmt|;
name|ShardStateMetaData
name|shardStateMetaData
init|=
name|ShardStateMetaData
operator|.
name|FORMAT
operator|.
name|loadLatestState
argument_list|(
name|logger
argument_list|,
name|nodeEnv
operator|.
name|availableShardPaths
argument_list|(
name|request
operator|.
name|shardId
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|shardStateMetaData
operator|!=
literal|null
condition|)
block|{
specifier|final
name|IndexMetaData
name|metaData
init|=
name|clusterService
operator|.
name|state
argument_list|()
operator|.
name|metaData
argument_list|()
operator|.
name|index
argument_list|(
name|shardId
operator|.
name|index
argument_list|()
operator|.
name|name
argument_list|()
argument_list|)
decl_stmt|;
comment|// it's a mystery why this is sometimes null
if|if
condition|(
name|metaData
operator|!=
literal|null
condition|)
block|{
name|ShardPath
name|shardPath
init|=
literal|null
decl_stmt|;
try|try
block|{
name|IndexSettings
name|indexSettings
init|=
operator|new
name|IndexSettings
argument_list|(
name|metaData
argument_list|,
name|settings
argument_list|,
name|Collections
operator|.
name|emptyList
argument_list|()
argument_list|)
decl_stmt|;
name|shardPath
operator|=
name|ShardPath
operator|.
name|loadShardPath
argument_list|(
name|logger
argument_list|,
name|nodeEnv
argument_list|,
name|shardId
argument_list|,
name|indexSettings
argument_list|)
expr_stmt|;
if|if
condition|(
name|shardPath
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
name|shardId
operator|+
literal|" no shard path found"
argument_list|)
throw|;
block|}
name|Store
operator|.
name|tryOpenIndex
argument_list|(
name|shardPath
operator|.
name|resolveIndex
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|exception
parameter_list|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"{} can't open index for shard [{}] in path [{}]"
argument_list|,
name|exception
argument_list|,
name|shardId
argument_list|,
name|shardStateMetaData
argument_list|,
operator|(
name|shardPath
operator|!=
literal|null
operator|)
condition|?
name|shardPath
operator|.
name|resolveIndex
argument_list|()
else|:
literal|""
argument_list|)
expr_stmt|;
return|return
operator|new
name|NodeGatewayStartedShards
argument_list|(
name|clusterService
operator|.
name|localNode
argument_list|()
argument_list|,
name|shardStateMetaData
operator|.
name|version
argument_list|,
name|exception
argument_list|)
return|;
block|}
block|}
comment|// old shard metadata doesn't have the actual index UUID so we need to check if the actual uuid in the metadata
comment|// is equal to IndexMetaData.INDEX_UUID_NA_VALUE otherwise this shard doesn't belong to the requested index.
if|if
condition|(
name|indexUUID
operator|.
name|equals
argument_list|(
name|shardStateMetaData
operator|.
name|indexUUID
argument_list|)
operator|==
literal|false
operator|&&
name|IndexMetaData
operator|.
name|INDEX_UUID_NA_VALUE
operator|.
name|equals
argument_list|(
name|shardStateMetaData
operator|.
name|indexUUID
argument_list|)
operator|==
literal|false
condition|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"{} shard state info found but indexUUID didn't match expected [{}] actual [{}]"
argument_list|,
name|shardId
argument_list|,
name|indexUUID
argument_list|,
name|shardStateMetaData
operator|.
name|indexUUID
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"{} shard state info found: [{}]"
argument_list|,
name|shardId
argument_list|,
name|shardStateMetaData
argument_list|)
expr_stmt|;
return|return
operator|new
name|NodeGatewayStartedShards
argument_list|(
name|clusterService
operator|.
name|localNode
argument_list|()
argument_list|,
name|shardStateMetaData
operator|.
name|version
argument_list|)
return|;
block|}
block|}
name|logger
operator|.
name|trace
argument_list|(
literal|"{} no local shard info found"
argument_list|,
name|shardId
argument_list|)
expr_stmt|;
return|return
operator|new
name|NodeGatewayStartedShards
argument_list|(
name|clusterService
operator|.
name|localNode
argument_list|()
argument_list|,
operator|-
literal|1
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
literal|"failed to load started shards"
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
literal|true
return|;
block|}
DECL|class|Request
specifier|public
specifier|static
class|class
name|Request
extends|extends
name|BaseNodesRequest
argument_list|<
name|Request
argument_list|>
block|{
DECL|field|shardId
specifier|private
name|ShardId
name|shardId
decl_stmt|;
DECL|field|indexUUID
specifier|private
name|String
name|indexUUID
decl_stmt|;
DECL|method|Request
specifier|public
name|Request
parameter_list|()
block|{         }
DECL|method|Request
specifier|public
name|Request
parameter_list|(
name|ShardId
name|shardId
parameter_list|,
name|String
name|indexUUID
parameter_list|,
name|String
index|[]
name|nodesIds
parameter_list|)
block|{
name|super
argument_list|(
name|nodesIds
argument_list|)
expr_stmt|;
name|this
operator|.
name|shardId
operator|=
name|shardId
expr_stmt|;
name|this
operator|.
name|indexUUID
operator|=
name|indexUUID
expr_stmt|;
block|}
DECL|method|shardId
specifier|public
name|ShardId
name|shardId
parameter_list|()
block|{
return|return
name|this
operator|.
name|shardId
return|;
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
name|shardId
operator|=
name|ShardId
operator|.
name|readShardId
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|indexUUID
operator|=
name|in
operator|.
name|readString
argument_list|()
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
name|shardId
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeString
argument_list|(
name|indexUUID
argument_list|)
expr_stmt|;
block|}
DECL|method|getIndexUUID
specifier|public
name|String
name|getIndexUUID
parameter_list|()
block|{
return|return
name|indexUUID
return|;
block|}
block|}
DECL|class|NodesGatewayStartedShards
specifier|public
specifier|static
class|class
name|NodesGatewayStartedShards
extends|extends
name|BaseNodesResponse
argument_list|<
name|NodeGatewayStartedShards
argument_list|>
block|{
DECL|field|failures
specifier|private
name|FailedNodeException
index|[]
name|failures
decl_stmt|;
DECL|method|NodesGatewayStartedShards
specifier|public
name|NodesGatewayStartedShards
parameter_list|(
name|ClusterName
name|clusterName
parameter_list|,
name|NodeGatewayStartedShards
index|[]
name|nodes
parameter_list|,
name|FailedNodeException
index|[]
name|failures
parameter_list|)
block|{
name|super
argument_list|(
name|clusterName
argument_list|,
name|nodes
argument_list|)
expr_stmt|;
name|this
operator|.
name|failures
operator|=
name|failures
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|failures
specifier|public
name|FailedNodeException
index|[]
name|failures
parameter_list|()
block|{
return|return
name|failures
return|;
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
name|nodes
operator|=
operator|new
name|NodeGatewayStartedShards
index|[
name|in
operator|.
name|readVInt
argument_list|()
index|]
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|nodes
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|nodes
index|[
name|i
index|]
operator|=
operator|new
name|NodeGatewayStartedShards
argument_list|()
expr_stmt|;
name|nodes
index|[
name|i
index|]
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
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
name|out
operator|.
name|writeVInt
argument_list|(
name|nodes
operator|.
name|length
argument_list|)
expr_stmt|;
for|for
control|(
name|NodeGatewayStartedShards
name|response
range|:
name|nodes
control|)
block|{
name|response
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|class|NodeRequest
specifier|public
specifier|static
class|class
name|NodeRequest
extends|extends
name|BaseNodeRequest
block|{
DECL|field|shardId
specifier|private
name|ShardId
name|shardId
decl_stmt|;
DECL|field|indexUUID
specifier|private
name|String
name|indexUUID
decl_stmt|;
DECL|method|NodeRequest
specifier|public
name|NodeRequest
parameter_list|()
block|{         }
DECL|method|NodeRequest
name|NodeRequest
parameter_list|(
name|String
name|nodeId
parameter_list|,
name|TransportNodesListGatewayStartedShards
operator|.
name|Request
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
name|shardId
operator|=
name|request
operator|.
name|shardId
argument_list|()
expr_stmt|;
name|this
operator|.
name|indexUUID
operator|=
name|request
operator|.
name|getIndexUUID
argument_list|()
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
name|shardId
operator|=
name|ShardId
operator|.
name|readShardId
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|indexUUID
operator|=
name|in
operator|.
name|readString
argument_list|()
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
name|shardId
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeString
argument_list|(
name|indexUUID
argument_list|)
expr_stmt|;
block|}
DECL|method|getShardId
specifier|public
name|ShardId
name|getShardId
parameter_list|()
block|{
return|return
name|shardId
return|;
block|}
DECL|method|getIndexUUID
specifier|public
name|String
name|getIndexUUID
parameter_list|()
block|{
return|return
name|indexUUID
return|;
block|}
block|}
DECL|class|NodeGatewayStartedShards
specifier|public
specifier|static
class|class
name|NodeGatewayStartedShards
extends|extends
name|BaseNodeResponse
block|{
DECL|field|version
specifier|private
name|long
name|version
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|storeException
specifier|private
name|Throwable
name|storeException
init|=
literal|null
decl_stmt|;
DECL|method|NodeGatewayStartedShards
specifier|public
name|NodeGatewayStartedShards
parameter_list|()
block|{         }
DECL|method|NodeGatewayStartedShards
specifier|public
name|NodeGatewayStartedShards
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|,
name|long
name|version
parameter_list|)
block|{
name|this
argument_list|(
name|node
argument_list|,
name|version
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
DECL|method|NodeGatewayStartedShards
specifier|public
name|NodeGatewayStartedShards
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|,
name|long
name|version
parameter_list|,
name|Throwable
name|storeException
parameter_list|)
block|{
name|super
argument_list|(
name|node
argument_list|)
expr_stmt|;
name|this
operator|.
name|version
operator|=
name|version
expr_stmt|;
name|this
operator|.
name|storeException
operator|=
name|storeException
expr_stmt|;
block|}
DECL|method|version
specifier|public
name|long
name|version
parameter_list|()
block|{
return|return
name|this
operator|.
name|version
return|;
block|}
DECL|method|storeException
specifier|public
name|Throwable
name|storeException
parameter_list|()
block|{
return|return
name|this
operator|.
name|storeException
return|;
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
name|version
operator|=
name|in
operator|.
name|readLong
argument_list|()
expr_stmt|;
if|if
condition|(
name|in
operator|.
name|readBoolean
argument_list|()
condition|)
block|{
name|storeException
operator|=
name|in
operator|.
name|readThrowable
argument_list|()
expr_stmt|;
block|}
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
name|out
operator|.
name|writeLong
argument_list|(
name|version
argument_list|)
expr_stmt|;
if|if
condition|(
name|storeException
operator|!=
literal|null
condition|)
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeThrowable
argument_list|(
name|storeException
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

