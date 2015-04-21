begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.get
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|get
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
name|ExceptionsHelper
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
name|TransportActions
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
name|single
operator|.
name|shard
operator|.
name|TransportShardSingleOperationAction
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
name|routing
operator|.
name|ShardIterator
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
name|index
operator|.
name|get
operator|.
name|GetResult
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
name|IndexService
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
name|IndexShard
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
name|IndicesService
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

begin_class
DECL|class|TransportShardMultiGetAction
specifier|public
class|class
name|TransportShardMultiGetAction
extends|extends
name|TransportShardSingleOperationAction
argument_list|<
name|MultiGetShardRequest
argument_list|,
name|MultiGetShardResponse
argument_list|>
block|{
DECL|field|ACTION_NAME
specifier|private
specifier|static
specifier|final
name|String
name|ACTION_NAME
init|=
name|MultiGetAction
operator|.
name|NAME
operator|+
literal|"[shard]"
decl_stmt|;
DECL|field|indicesService
specifier|private
specifier|final
name|IndicesService
name|indicesService
decl_stmt|;
DECL|field|realtime
specifier|private
specifier|final
name|boolean
name|realtime
decl_stmt|;
annotation|@
name|Inject
DECL|method|TransportShardMultiGetAction
specifier|public
name|TransportShardMultiGetAction
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ClusterService
name|clusterService
parameter_list|,
name|TransportService
name|transportService
parameter_list|,
name|IndicesService
name|indicesService
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|,
name|ActionFilters
name|actionFilters
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|ACTION_NAME
argument_list|,
name|threadPool
argument_list|,
name|clusterService
argument_list|,
name|transportService
argument_list|,
name|actionFilters
argument_list|,
name|MultiGetShardRequest
operator|.
name|class
argument_list|,
name|ThreadPool
operator|.
name|Names
operator|.
name|GET
argument_list|)
expr_stmt|;
name|this
operator|.
name|indicesService
operator|=
name|indicesService
expr_stmt|;
name|this
operator|.
name|realtime
operator|=
name|settings
operator|.
name|getAsBoolean
argument_list|(
literal|"action.get.realtime"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|isSubAction
specifier|protected
name|boolean
name|isSubAction
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
DECL|method|newResponse
specifier|protected
name|MultiGetShardResponse
name|newResponse
parameter_list|()
block|{
return|return
operator|new
name|MultiGetShardResponse
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|resolveIndex
specifier|protected
name|boolean
name|resolveIndex
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
DECL|method|shards
specifier|protected
name|ShardIterator
name|shards
parameter_list|(
name|ClusterState
name|state
parameter_list|,
name|InternalRequest
name|request
parameter_list|)
block|{
return|return
name|clusterService
operator|.
name|operationRouting
argument_list|()
operator|.
name|getShards
argument_list|(
name|state
argument_list|,
name|request
operator|.
name|request
argument_list|()
operator|.
name|index
argument_list|()
argument_list|,
name|request
operator|.
name|request
argument_list|()
operator|.
name|shardId
argument_list|()
argument_list|,
name|request
operator|.
name|request
argument_list|()
operator|.
name|preference
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|resolveRequest
specifier|protected
name|void
name|resolveRequest
parameter_list|(
name|ClusterState
name|state
parameter_list|,
name|InternalRequest
name|request
parameter_list|)
block|{
if|if
condition|(
name|request
operator|.
name|request
argument_list|()
operator|.
name|realtime
operator|==
literal|null
condition|)
block|{
name|request
operator|.
name|request
argument_list|()
operator|.
name|realtime
operator|=
name|this
operator|.
name|realtime
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|shardOperation
specifier|protected
name|MultiGetShardResponse
name|shardOperation
parameter_list|(
name|MultiGetShardRequest
name|request
parameter_list|,
name|ShardId
name|shardId
parameter_list|)
throws|throws
name|ElasticsearchException
block|{
name|IndexService
name|indexService
init|=
name|indicesService
operator|.
name|indexServiceSafe
argument_list|(
name|shardId
operator|.
name|getIndex
argument_list|()
argument_list|)
decl_stmt|;
name|IndexShard
name|indexShard
init|=
name|indexService
operator|.
name|shardSafe
argument_list|(
name|shardId
operator|.
name|id
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|request
operator|.
name|refresh
argument_list|()
operator|&&
operator|!
name|request
operator|.
name|realtime
argument_list|()
condition|)
block|{
name|indexShard
operator|.
name|refresh
argument_list|(
literal|"refresh_flag_mget"
argument_list|)
expr_stmt|;
block|}
name|MultiGetShardResponse
name|response
init|=
operator|new
name|MultiGetShardResponse
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
name|request
operator|.
name|locations
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|MultiGetRequest
operator|.
name|Item
name|item
init|=
name|request
operator|.
name|items
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
try|try
block|{
name|GetResult
name|getResult
init|=
name|indexShard
operator|.
name|getService
argument_list|()
operator|.
name|get
argument_list|(
name|item
operator|.
name|type
argument_list|()
argument_list|,
name|item
operator|.
name|id
argument_list|()
argument_list|,
name|item
operator|.
name|fields
argument_list|()
argument_list|,
name|request
operator|.
name|realtime
argument_list|()
argument_list|,
name|item
operator|.
name|version
argument_list|()
argument_list|,
name|item
operator|.
name|versionType
argument_list|()
argument_list|,
name|item
operator|.
name|fetchSourceContext
argument_list|()
argument_list|,
name|request
operator|.
name|ignoreErrorsOnGeneratedFields
argument_list|()
argument_list|)
decl_stmt|;
name|response
operator|.
name|add
argument_list|(
name|request
operator|.
name|locations
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
operator|new
name|GetResponse
argument_list|(
name|getResult
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
if|if
condition|(
name|TransportActions
operator|.
name|isShardNotAvailableException
argument_list|(
name|t
argument_list|)
condition|)
block|{
throw|throw
operator|(
name|ElasticsearchException
operator|)
name|t
throw|;
block|}
else|else
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"{} failed to execute multi_get for [{}]/[{}]"
argument_list|,
name|t
argument_list|,
name|shardId
argument_list|,
name|item
operator|.
name|type
argument_list|()
argument_list|,
name|item
operator|.
name|id
argument_list|()
argument_list|)
expr_stmt|;
name|response
operator|.
name|add
argument_list|(
name|request
operator|.
name|locations
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
operator|new
name|MultiGetResponse
operator|.
name|Failure
argument_list|(
name|request
operator|.
name|index
argument_list|()
argument_list|,
name|item
operator|.
name|type
argument_list|()
argument_list|,
name|item
operator|.
name|id
argument_list|()
argument_list|,
name|ExceptionsHelper
operator|.
name|detailedMessage
argument_list|(
name|t
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|response
return|;
block|}
block|}
end_class

end_unit

