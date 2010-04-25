begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.support.replication
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|support
operator|.
name|replication
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|guice
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
name|ElasticSearchException
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
name|ActionResponse
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
name|BaseAction
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
name|GroupShardsIterator
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
name|ShardsIterator
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
name|BaseTransportRequestHandler
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
name|TransportChannel
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
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|settings
operator|.
name|Settings
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
name|AtomicInteger
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
comment|/**  * @author kimchy (Shay Banon)  */
end_comment

begin_class
DECL|class|TransportIndexReplicationOperationAction
specifier|public
specifier|abstract
class|class
name|TransportIndexReplicationOperationAction
parameter_list|<
name|Request
extends|extends
name|IndexReplicationOperationRequest
parameter_list|,
name|Response
extends|extends
name|ActionResponse
parameter_list|,
name|ShardRequest
extends|extends
name|ShardReplicationOperationRequest
parameter_list|,
name|ShardResponse
extends|extends
name|ActionResponse
parameter_list|>
extends|extends
name|BaseAction
argument_list|<
name|Request
argument_list|,
name|Response
argument_list|>
block|{
DECL|field|threadPool
specifier|protected
specifier|final
name|ThreadPool
name|threadPool
decl_stmt|;
DECL|field|shardAction
specifier|protected
specifier|final
name|TransportShardReplicationOperationAction
argument_list|<
name|ShardRequest
argument_list|,
name|ShardResponse
argument_list|>
name|shardAction
decl_stmt|;
DECL|method|TransportIndexReplicationOperationAction
annotation|@
name|Inject
specifier|public
name|TransportIndexReplicationOperationAction
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|TransportService
name|transportService
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|,
name|TransportShardReplicationOperationAction
argument_list|<
name|ShardRequest
argument_list|,
name|ShardResponse
argument_list|>
name|shardAction
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|threadPool
operator|=
name|threadPool
expr_stmt|;
name|this
operator|.
name|shardAction
operator|=
name|shardAction
expr_stmt|;
name|transportService
operator|.
name|registerHandler
argument_list|(
name|transportAction
argument_list|()
argument_list|,
operator|new
name|TransportHandler
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|doExecute
annotation|@
name|Override
specifier|protected
name|void
name|doExecute
parameter_list|(
specifier|final
name|Request
name|request
parameter_list|,
specifier|final
name|ActionListener
argument_list|<
name|Response
argument_list|>
name|listener
parameter_list|)
block|{
name|GroupShardsIterator
name|groups
decl_stmt|;
try|try
block|{
name|groups
operator|=
name|shards
argument_list|(
name|request
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|listener
operator|.
name|onFailure
argument_list|(
name|e
argument_list|)
expr_stmt|;
return|return;
block|}
specifier|final
name|AtomicInteger
name|indexCounter
init|=
operator|new
name|AtomicInteger
argument_list|()
decl_stmt|;
specifier|final
name|AtomicInteger
name|completionCounter
init|=
operator|new
name|AtomicInteger
argument_list|(
name|groups
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|AtomicReferenceArray
argument_list|<
name|Object
argument_list|>
name|shardsResponses
init|=
operator|new
name|AtomicReferenceArray
argument_list|<
name|Object
argument_list|>
argument_list|(
name|groups
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
specifier|final
name|ShardsIterator
name|shards
range|:
name|groups
control|)
block|{
name|ShardRequest
name|shardRequest
init|=
name|newShardRequestInstance
argument_list|(
name|request
argument_list|,
name|shards
operator|.
name|shardId
argument_list|()
operator|.
name|id
argument_list|()
argument_list|)
decl_stmt|;
comment|// TODO for now, we fork operations on shards of the index
name|shardRequest
operator|.
name|operationThreaded
argument_list|(
literal|true
argument_list|)
expr_stmt|;
comment|// no need for threaded listener, we will fork when its done based on the index request
name|shardRequest
operator|.
name|listenerThreaded
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|shardAction
operator|.
name|execute
argument_list|(
name|shardRequest
argument_list|,
operator|new
name|ActionListener
argument_list|<
name|ShardResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onResponse
parameter_list|(
name|ShardResponse
name|result
parameter_list|)
block|{
name|shardsResponses
operator|.
name|set
argument_list|(
name|indexCounter
operator|.
name|getAndIncrement
argument_list|()
argument_list|,
name|result
argument_list|)
expr_stmt|;
if|if
condition|(
name|completionCounter
operator|.
name|decrementAndGet
argument_list|()
operator|==
literal|0
condition|)
block|{
if|if
condition|(
name|request
operator|.
name|listenerThreaded
argument_list|()
condition|)
block|{
name|threadPool
operator|.
name|execute
argument_list|(
operator|new
name|Runnable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|listener
operator|.
name|onResponse
argument_list|(
name|newResponseInstance
argument_list|(
name|request
argument_list|,
name|shardsResponses
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|listener
operator|.
name|onResponse
argument_list|(
name|newResponseInstance
argument_list|(
name|request
argument_list|,
name|shardsResponses
argument_list|)
argument_list|)
expr_stmt|;
block|}
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
name|int
name|index
init|=
name|indexCounter
operator|.
name|getAndIncrement
argument_list|()
decl_stmt|;
if|if
condition|(
name|accumulateExceptions
argument_list|()
condition|)
block|{
name|shardsResponses
operator|.
name|set
argument_list|(
name|index
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|completionCounter
operator|.
name|decrementAndGet
argument_list|()
operator|==
literal|0
condition|)
block|{
if|if
condition|(
name|request
operator|.
name|listenerThreaded
argument_list|()
condition|)
block|{
name|threadPool
operator|.
name|execute
argument_list|(
operator|new
name|Runnable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|listener
operator|.
name|onResponse
argument_list|(
name|newResponseInstance
argument_list|(
name|request
argument_list|,
name|shardsResponses
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|listener
operator|.
name|onResponse
argument_list|(
name|newResponseInstance
argument_list|(
name|request
argument_list|,
name|shardsResponses
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|newRequestInstance
specifier|protected
specifier|abstract
name|Request
name|newRequestInstance
parameter_list|()
function_decl|;
DECL|method|newResponseInstance
specifier|protected
specifier|abstract
name|Response
name|newResponseInstance
parameter_list|(
name|Request
name|request
parameter_list|,
name|AtomicReferenceArray
name|shardsResponses
parameter_list|)
function_decl|;
DECL|method|transportAction
specifier|protected
specifier|abstract
name|String
name|transportAction
parameter_list|()
function_decl|;
DECL|method|shards
specifier|protected
specifier|abstract
name|GroupShardsIterator
name|shards
parameter_list|(
name|Request
name|request
parameter_list|)
throws|throws
name|ElasticSearchException
function_decl|;
DECL|method|newShardRequestInstance
specifier|protected
specifier|abstract
name|ShardRequest
name|newShardRequestInstance
parameter_list|(
name|Request
name|request
parameter_list|,
name|int
name|shardId
parameter_list|)
function_decl|;
DECL|method|accumulateExceptions
specifier|protected
specifier|abstract
name|boolean
name|accumulateExceptions
parameter_list|()
function_decl|;
DECL|class|TransportHandler
specifier|private
class|class
name|TransportHandler
extends|extends
name|BaseTransportRequestHandler
argument_list|<
name|Request
argument_list|>
block|{
DECL|method|newInstance
annotation|@
name|Override
specifier|public
name|Request
name|newInstance
parameter_list|()
block|{
return|return
name|newRequestInstance
argument_list|()
return|;
block|}
DECL|method|messageReceived
annotation|@
name|Override
specifier|public
name|void
name|messageReceived
parameter_list|(
specifier|final
name|Request
name|request
parameter_list|,
specifier|final
name|TransportChannel
name|channel
parameter_list|)
throws|throws
name|Exception
block|{
comment|// no need to use threaded listener, since we just send a response
name|request
operator|.
name|listenerThreaded
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|execute
argument_list|(
name|request
argument_list|,
operator|new
name|ActionListener
argument_list|<
name|Response
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onResponse
parameter_list|(
name|Response
name|result
parameter_list|)
block|{
try|try
block|{
name|channel
operator|.
name|sendResponse
argument_list|(
name|result
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
name|e
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e1
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"Failed to send error response for action ["
operator|+
name|transportAction
argument_list|()
operator|+
literal|"] and request ["
operator|+
name|request
operator|+
literal|"]"
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
DECL|method|spawn
annotation|@
name|Override
specifier|public
name|boolean
name|spawn
parameter_list|()
block|{
comment|// no need to spawn, since in the doExecute we always execute with threaded operation set to true
return|return
literal|false
return|;
block|}
block|}
block|}
end_class

end_unit

