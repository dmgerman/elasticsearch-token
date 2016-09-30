begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.replication
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|replication
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
name|store
operator|.
name|AlreadyClosedException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|Version
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
name|DocWriteResponse
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
name|flush
operator|.
name|FlushRequest
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
name|index
operator|.
name|IndexRequest
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
name|index
operator|.
name|IndexResponse
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
name|index
operator|.
name|TransportIndexAction
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
name|PlainActionFuture
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
name|replication
operator|.
name|ReplicationOperation
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
name|replication
operator|.
name|ReplicationRequest
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
name|replication
operator|.
name|ReplicationResponse
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
name|replication
operator|.
name|TransportWriteAction
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
name|replication
operator|.
name|TransportWriteActionTestHelper
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
name|ShardRouting
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
name|ShardRoutingHelper
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
name|collect
operator|.
name|Iterators
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
name|transport
operator|.
name|LocalTransportAddress
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
name|engine
operator|.
name|Engine
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
name|mapper
operator|.
name|Uid
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
name|index
operator|.
name|shard
operator|.
name|IndexShardTestCase
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
name|indices
operator|.
name|recovery
operator|.
name|RecoveryTarget
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
name|TransportResponse
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
name|HashSet
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
name|List
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|Future
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
name|FutureTask
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
name|function
operator|.
name|BiFunction
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
name|Consumer
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|Collectors
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|StreamSupport
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|empty
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|equalTo
import|;
end_import

begin_class
DECL|class|ESIndexLevelReplicationTestCase
specifier|public
specifier|abstract
class|class
name|ESIndexLevelReplicationTestCase
extends|extends
name|IndexShardTestCase
block|{
DECL|field|index
specifier|protected
specifier|final
name|Index
name|index
init|=
operator|new
name|Index
argument_list|(
literal|"test"
argument_list|,
literal|"uuid"
argument_list|)
decl_stmt|;
DECL|field|shardId
specifier|private
specifier|final
name|ShardId
name|shardId
init|=
operator|new
name|ShardId
argument_list|(
name|index
argument_list|,
literal|0
argument_list|)
decl_stmt|;
DECL|field|indexMapping
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|indexMapping
init|=
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"type"
argument_list|,
literal|"{ \"type\": {} }"
argument_list|)
decl_stmt|;
DECL|method|createGroup
specifier|protected
name|ReplicationGroup
name|createGroup
parameter_list|(
name|int
name|replicas
parameter_list|)
throws|throws
name|IOException
block|{
name|Settings
name|settings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_VERSION_CREATED
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_NUMBER_OF_REPLICAS
argument_list|,
name|replicas
argument_list|)
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_NUMBER_OF_SHARDS
argument_list|,
literal|1
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|IndexMetaData
operator|.
name|Builder
name|metaData
init|=
name|IndexMetaData
operator|.
name|builder
argument_list|(
name|index
operator|.
name|getName
argument_list|()
argument_list|)
operator|.
name|settings
argument_list|(
name|settings
argument_list|)
operator|.
name|primaryTerm
argument_list|(
literal|0
argument_list|,
literal|1
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
name|typeMapping
range|:
name|indexMapping
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|metaData
operator|.
name|putMapping
argument_list|(
name|typeMapping
operator|.
name|getKey
argument_list|()
argument_list|,
name|typeMapping
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|ReplicationGroup
argument_list|(
name|metaData
operator|.
name|build
argument_list|()
argument_list|)
return|;
block|}
DECL|method|getDiscoveryNode
specifier|protected
name|DiscoveryNode
name|getDiscoveryNode
parameter_list|(
name|String
name|id
parameter_list|)
block|{
return|return
operator|new
name|DiscoveryNode
argument_list|(
name|id
argument_list|,
name|id
argument_list|,
operator|new
name|LocalTransportAddress
argument_list|(
name|id
argument_list|)
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|,
name|Collections
operator|.
name|singleton
argument_list|(
name|DiscoveryNode
operator|.
name|Role
operator|.
name|DATA
argument_list|)
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
return|;
block|}
DECL|class|ReplicationGroup
specifier|protected
class|class
name|ReplicationGroup
implements|implements
name|AutoCloseable
implements|,
name|Iterable
argument_list|<
name|IndexShard
argument_list|>
block|{
DECL|field|primary
specifier|private
specifier|final
name|IndexShard
name|primary
decl_stmt|;
DECL|field|replicas
specifier|private
specifier|final
name|List
argument_list|<
name|IndexShard
argument_list|>
name|replicas
decl_stmt|;
DECL|field|indexMetaData
specifier|private
specifier|final
name|IndexMetaData
name|indexMetaData
decl_stmt|;
DECL|field|replicaId
specifier|private
specifier|final
name|AtomicInteger
name|replicaId
init|=
operator|new
name|AtomicInteger
argument_list|()
decl_stmt|;
DECL|field|docId
specifier|private
specifier|final
name|AtomicInteger
name|docId
init|=
operator|new
name|AtomicInteger
argument_list|()
decl_stmt|;
DECL|field|closed
name|boolean
name|closed
init|=
literal|false
decl_stmt|;
DECL|method|ReplicationGroup
name|ReplicationGroup
parameter_list|(
specifier|final
name|IndexMetaData
name|indexMetaData
parameter_list|)
throws|throws
name|IOException
block|{
name|primary
operator|=
name|newShard
argument_list|(
name|shardId
argument_list|,
literal|true
argument_list|,
literal|"s0"
argument_list|,
name|indexMetaData
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|replicas
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
name|this
operator|.
name|indexMetaData
operator|=
name|indexMetaData
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
name|indexMetaData
operator|.
name|getNumberOfReplicas
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|addReplica
argument_list|()
expr_stmt|;
block|}
block|}
DECL|method|indexDocs
specifier|public
name|int
name|indexDocs
parameter_list|(
specifier|final
name|int
name|numOfDoc
parameter_list|)
throws|throws
name|Exception
block|{
for|for
control|(
name|int
name|doc
init|=
literal|0
init|;
name|doc
operator|<
name|numOfDoc
condition|;
name|doc
operator|++
control|)
block|{
specifier|final
name|IndexRequest
name|indexRequest
init|=
operator|new
name|IndexRequest
argument_list|(
name|index
operator|.
name|getName
argument_list|()
argument_list|,
literal|"type"
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|docId
operator|.
name|incrementAndGet
argument_list|()
argument_list|)
argument_list|)
operator|.
name|source
argument_list|(
literal|"{}"
argument_list|)
decl_stmt|;
specifier|final
name|IndexResponse
name|response
init|=
name|index
argument_list|(
name|indexRequest
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|DocWriteResponse
operator|.
name|Result
operator|.
name|CREATED
argument_list|,
name|response
operator|.
name|getResult
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|numOfDoc
return|;
block|}
DECL|method|appendDocs
specifier|public
name|int
name|appendDocs
parameter_list|(
specifier|final
name|int
name|numOfDoc
parameter_list|)
throws|throws
name|Exception
block|{
for|for
control|(
name|int
name|doc
init|=
literal|0
init|;
name|doc
operator|<
name|numOfDoc
condition|;
name|doc
operator|++
control|)
block|{
specifier|final
name|IndexRequest
name|indexRequest
init|=
operator|new
name|IndexRequest
argument_list|(
name|index
operator|.
name|getName
argument_list|()
argument_list|,
literal|"type"
argument_list|)
operator|.
name|source
argument_list|(
literal|"{}"
argument_list|)
decl_stmt|;
specifier|final
name|IndexResponse
name|response
init|=
name|index
argument_list|(
name|indexRequest
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|DocWriteResponse
operator|.
name|Result
operator|.
name|CREATED
argument_list|,
name|response
operator|.
name|getResult
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|numOfDoc
return|;
block|}
DECL|method|index
specifier|public
name|IndexResponse
name|index
parameter_list|(
name|IndexRequest
name|indexRequest
parameter_list|)
throws|throws
name|Exception
block|{
name|PlainActionFuture
argument_list|<
name|IndexResponse
argument_list|>
name|listener
init|=
operator|new
name|PlainActionFuture
argument_list|<>
argument_list|()
decl_stmt|;
operator|new
name|IndexingAction
argument_list|(
name|indexRequest
argument_list|,
name|listener
argument_list|,
name|this
argument_list|)
operator|.
name|execute
argument_list|()
expr_stmt|;
return|return
name|listener
operator|.
name|get
argument_list|()
return|;
block|}
DECL|method|startAll
specifier|public
specifier|synchronized
name|void
name|startAll
parameter_list|()
throws|throws
name|IOException
block|{
specifier|final
name|DiscoveryNode
name|pNode
init|=
name|getDiscoveryNode
argument_list|(
name|primary
operator|.
name|routingEntry
argument_list|()
operator|.
name|currentNodeId
argument_list|()
argument_list|)
decl_stmt|;
name|primary
operator|.
name|markAsRecovering
argument_list|(
literal|"store"
argument_list|,
operator|new
name|RecoveryState
argument_list|(
name|primary
operator|.
name|routingEntry
argument_list|()
argument_list|,
name|pNode
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|primary
operator|.
name|recoverFromStore
argument_list|()
expr_stmt|;
name|primary
operator|.
name|updateRoutingEntry
argument_list|(
name|ShardRoutingHelper
operator|.
name|moveToStarted
argument_list|(
name|primary
operator|.
name|routingEntry
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|IndexShard
name|replicaShard
range|:
name|replicas
control|)
block|{
name|recoverReplica
argument_list|(
name|replicaShard
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|addReplica
specifier|public
specifier|synchronized
name|IndexShard
name|addReplica
parameter_list|()
throws|throws
name|IOException
block|{
specifier|final
name|IndexShard
name|replica
init|=
name|newShard
argument_list|(
name|shardId
argument_list|,
literal|false
argument_list|,
literal|"s"
operator|+
name|replicaId
operator|.
name|incrementAndGet
argument_list|()
argument_list|,
name|indexMetaData
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|replicas
operator|.
name|add
argument_list|(
name|replica
argument_list|)
expr_stmt|;
return|return
name|replica
return|;
block|}
DECL|method|recoverReplica
specifier|public
name|void
name|recoverReplica
parameter_list|(
name|IndexShard
name|replica
parameter_list|)
throws|throws
name|IOException
block|{
name|recoverReplica
argument_list|(
name|replica
argument_list|,
parameter_list|(
name|r
parameter_list|,
name|sourceNode
parameter_list|)
lambda|->
operator|new
name|RecoveryTarget
argument_list|(
name|r
argument_list|,
name|sourceNode
argument_list|,
name|recoveryListener
argument_list|,
name|version
lambda|->
block|{}
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|recoverReplica
specifier|public
name|void
name|recoverReplica
parameter_list|(
name|IndexShard
name|replica
parameter_list|,
name|BiFunction
argument_list|<
name|IndexShard
argument_list|,
name|DiscoveryNode
argument_list|,
name|RecoveryTarget
argument_list|>
name|targetSupplier
parameter_list|)
throws|throws
name|IOException
block|{
name|recoverReplica
argument_list|(
name|replica
argument_list|,
name|targetSupplier
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
DECL|method|recoverReplica
specifier|public
name|void
name|recoverReplica
parameter_list|(
name|IndexShard
name|replica
parameter_list|,
name|BiFunction
argument_list|<
name|IndexShard
argument_list|,
name|DiscoveryNode
argument_list|,
name|RecoveryTarget
argument_list|>
name|targetSupplier
parameter_list|,
name|boolean
name|markAsRecovering
parameter_list|)
throws|throws
name|IOException
block|{
name|ESIndexLevelReplicationTestCase
operator|.
name|this
operator|.
name|recoverReplica
argument_list|(
name|replica
argument_list|,
name|primary
argument_list|,
name|targetSupplier
argument_list|,
name|markAsRecovering
argument_list|)
expr_stmt|;
block|}
DECL|method|getPrimaryNode
specifier|public
specifier|synchronized
name|DiscoveryNode
name|getPrimaryNode
parameter_list|()
block|{
return|return
name|getDiscoveryNode
argument_list|(
name|primary
operator|.
name|routingEntry
argument_list|()
operator|.
name|currentNodeId
argument_list|()
argument_list|)
return|;
block|}
DECL|method|asyncRecoverReplica
specifier|public
name|Future
argument_list|<
name|Void
argument_list|>
name|asyncRecoverReplica
parameter_list|(
name|IndexShard
name|replica
parameter_list|,
name|BiFunction
argument_list|<
name|IndexShard
argument_list|,
name|DiscoveryNode
argument_list|,
name|RecoveryTarget
argument_list|>
name|targetSupplier
parameter_list|)
throws|throws
name|IOException
block|{
name|FutureTask
argument_list|<
name|Void
argument_list|>
name|task
init|=
operator|new
name|FutureTask
argument_list|<>
argument_list|(
parameter_list|()
lambda|->
block|{
name|recoverReplica
argument_list|(
name|replica
argument_list|,
name|targetSupplier
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
argument_list|)
decl_stmt|;
name|threadPool
operator|.
name|generic
argument_list|()
operator|.
name|execute
argument_list|(
name|task
argument_list|)
expr_stmt|;
return|return
name|task
return|;
block|}
DECL|method|assertAllEqual
specifier|public
specifier|synchronized
name|void
name|assertAllEqual
parameter_list|(
name|int
name|expectedCount
parameter_list|)
throws|throws
name|IOException
block|{
name|Set
argument_list|<
name|Uid
argument_list|>
name|primaryIds
init|=
name|getShardDocUIDs
argument_list|(
name|primary
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|primaryIds
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|expectedCount
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|IndexShard
name|replica
range|:
name|replicas
control|)
block|{
name|Set
argument_list|<
name|Uid
argument_list|>
name|replicaIds
init|=
name|getShardDocUIDs
argument_list|(
name|replica
argument_list|)
decl_stmt|;
name|Set
argument_list|<
name|Uid
argument_list|>
name|temp
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|primaryIds
argument_list|)
decl_stmt|;
name|temp
operator|.
name|removeAll
argument_list|(
name|replicaIds
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|replica
operator|.
name|routingEntry
argument_list|()
operator|+
literal|" is missing docs"
argument_list|,
name|temp
argument_list|,
name|empty
argument_list|()
argument_list|)
expr_stmt|;
name|temp
operator|=
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|replicaIds
argument_list|)
expr_stmt|;
name|temp
operator|.
name|removeAll
argument_list|(
name|primaryIds
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|replica
operator|.
name|routingEntry
argument_list|()
operator|+
literal|" has extra docs"
argument_list|,
name|temp
argument_list|,
name|empty
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|refresh
specifier|public
specifier|synchronized
name|void
name|refresh
parameter_list|(
name|String
name|source
parameter_list|)
block|{
for|for
control|(
name|IndexShard
name|shard
range|:
name|this
control|)
block|{
name|shard
operator|.
name|refresh
argument_list|(
name|source
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|flush
specifier|public
specifier|synchronized
name|void
name|flush
parameter_list|()
block|{
specifier|final
name|FlushRequest
name|request
init|=
operator|new
name|FlushRequest
argument_list|()
decl_stmt|;
for|for
control|(
name|IndexShard
name|shard
range|:
name|this
control|)
block|{
name|shard
operator|.
name|flush
argument_list|(
name|request
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|shardRoutings
specifier|public
specifier|synchronized
name|List
argument_list|<
name|ShardRouting
argument_list|>
name|shardRoutings
parameter_list|()
block|{
return|return
name|StreamSupport
operator|.
name|stream
argument_list|(
name|this
operator|.
name|spliterator
argument_list|()
argument_list|,
literal|false
argument_list|)
operator|.
name|map
argument_list|(
name|IndexShard
operator|::
name|routingEntry
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|close
specifier|public
specifier|synchronized
name|void
name|close
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
name|closed
operator|==
literal|false
condition|)
block|{
name|closed
operator|=
literal|true
expr_stmt|;
name|closeShards
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|AlreadyClosedException
argument_list|(
literal|"too bad"
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
DECL|method|iterator
specifier|public
name|Iterator
argument_list|<
name|IndexShard
argument_list|>
name|iterator
parameter_list|()
block|{
return|return
name|Iterators
operator|.
name|concat
argument_list|(
name|replicas
operator|.
name|iterator
argument_list|()
argument_list|,
name|Collections
operator|.
name|singleton
argument_list|(
name|primary
argument_list|)
operator|.
name|iterator
argument_list|()
argument_list|)
return|;
block|}
DECL|method|getPrimary
specifier|public
name|IndexShard
name|getPrimary
parameter_list|()
block|{
return|return
name|primary
return|;
block|}
block|}
DECL|class|ReplicationAction
specifier|abstract
class|class
name|ReplicationAction
parameter_list|<
name|Request
extends|extends
name|ReplicationRequest
parameter_list|<
name|Request
parameter_list|>
parameter_list|,
name|ReplicaRequest
extends|extends
name|ReplicationRequest
parameter_list|<
name|ReplicaRequest
parameter_list|>
parameter_list|,
name|Response
extends|extends
name|ReplicationResponse
parameter_list|>
block|{
DECL|field|request
specifier|private
specifier|final
name|Request
name|request
decl_stmt|;
DECL|field|listener
specifier|private
name|ActionListener
argument_list|<
name|Response
argument_list|>
name|listener
decl_stmt|;
DECL|field|replicationGroup
specifier|private
specifier|final
name|ReplicationGroup
name|replicationGroup
decl_stmt|;
DECL|field|opType
specifier|private
specifier|final
name|String
name|opType
decl_stmt|;
DECL|method|ReplicationAction
specifier|public
name|ReplicationAction
parameter_list|(
name|Request
name|request
parameter_list|,
name|ActionListener
argument_list|<
name|Response
argument_list|>
name|listener
parameter_list|,
name|ReplicationGroup
name|group
parameter_list|,
name|String
name|opType
parameter_list|)
block|{
name|this
operator|.
name|request
operator|=
name|request
expr_stmt|;
name|this
operator|.
name|listener
operator|=
name|listener
expr_stmt|;
name|this
operator|.
name|replicationGroup
operator|=
name|group
expr_stmt|;
name|this
operator|.
name|opType
operator|=
name|opType
expr_stmt|;
block|}
DECL|method|execute
specifier|public
name|void
name|execute
parameter_list|()
throws|throws
name|Exception
block|{
operator|new
name|ReplicationOperation
argument_list|<
name|Request
argument_list|,
name|ReplicaRequest
argument_list|,
name|PrimaryResult
argument_list|>
argument_list|(
name|request
argument_list|,
operator|new
name|PrimaryRef
argument_list|()
argument_list|,
operator|new
name|ActionListener
argument_list|<
name|PrimaryResult
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onResponse
parameter_list|(
name|PrimaryResult
name|result
parameter_list|)
block|{
name|result
operator|.
name|respond
argument_list|(
name|listener
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|onFailure
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
block|}
block|}
argument_list|,
literal|true
argument_list|,
operator|new
name|ReplicasRef
argument_list|()
argument_list|,
parameter_list|()
lambda|->
literal|null
argument_list|,
name|logger
argument_list|,
name|opType
argument_list|)
block|{                 @
name|Override
specifier|protected
name|List
argument_list|<
name|ShardRouting
argument_list|>
name|getShards
argument_list|(
name|ShardId
name|shardId
argument_list|,
name|ClusterState
name|state
argument_list|)
block|{
return|return
name|replicationGroup
operator|.
name|shardRoutings
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|protected
name|String
name|checkActiveShardCount
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
expr|@
name|Override
specifier|protected
name|Set
argument_list|<
name|String
argument_list|>
name|getInSyncAllocationIds
argument_list|(
name|ShardId
name|shardId
argument_list|,
name|ClusterState
name|clusterState
argument_list|)
block|{
return|return
name|replicationGroup
operator|.
name|shardRoutings
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|filter
argument_list|(
name|ShardRouting
operator|::
name|active
argument_list|)
operator|.
name|map
argument_list|(
name|r
lambda|->
name|r
operator|.
name|allocationId
argument_list|()
operator|.
name|getId
argument_list|()
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toSet
argument_list|()
argument_list|)
return|;
block|}
block|}
operator|.
name|execute
argument_list|()
expr_stmt|;
block|}
DECL|method|performOnPrimary
specifier|protected
specifier|abstract
name|PrimaryResult
name|performOnPrimary
parameter_list|(
name|IndexShard
name|primary
parameter_list|,
name|Request
name|request
parameter_list|)
throws|throws
name|Exception
function_decl|;
DECL|method|performOnReplica
specifier|protected
specifier|abstract
name|void
name|performOnReplica
parameter_list|(
name|ReplicaRequest
name|request
parameter_list|,
name|IndexShard
name|replica
parameter_list|)
function_decl|;
DECL|class|PrimaryRef
class|class
name|PrimaryRef
implements|implements
name|ReplicationOperation
operator|.
name|Primary
argument_list|<
name|Request
argument_list|,
name|ReplicaRequest
argument_list|,
name|PrimaryResult
argument_list|>
block|{
annotation|@
name|Override
DECL|method|routingEntry
specifier|public
name|ShardRouting
name|routingEntry
parameter_list|()
block|{
return|return
name|replicationGroup
operator|.
name|primary
operator|.
name|routingEntry
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|failShard
specifier|public
name|void
name|failShard
parameter_list|(
name|String
name|message
parameter_list|,
name|Exception
name|exception
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
DECL|method|perform
specifier|public
name|PrimaryResult
name|perform
parameter_list|(
name|Request
name|request
parameter_list|)
throws|throws
name|Exception
block|{
name|PrimaryResult
name|response
init|=
name|performOnPrimary
argument_list|(
name|replicationGroup
operator|.
name|primary
argument_list|,
name|request
argument_list|)
decl_stmt|;
name|response
operator|.
name|replicaRequest
argument_list|()
operator|.
name|primaryTerm
argument_list|(
name|replicationGroup
operator|.
name|primary
operator|.
name|getPrimaryTerm
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|response
return|;
block|}
block|}
DECL|class|ReplicasRef
class|class
name|ReplicasRef
implements|implements
name|ReplicationOperation
operator|.
name|Replicas
argument_list|<
name|ReplicaRequest
argument_list|>
block|{
annotation|@
name|Override
DECL|method|performOn
specifier|public
name|void
name|performOn
parameter_list|(
name|ShardRouting
name|replicaRouting
parameter_list|,
name|ReplicaRequest
name|request
parameter_list|,
name|ActionListener
argument_list|<
name|TransportResponse
operator|.
name|Empty
argument_list|>
name|listener
parameter_list|)
block|{
try|try
block|{
name|IndexShard
name|replica
init|=
name|replicationGroup
operator|.
name|replicas
operator|.
name|stream
argument_list|()
operator|.
name|filter
argument_list|(
name|s
lambda|->
name|replicaRouting
operator|.
name|isSameAllocation
argument_list|(
name|s
operator|.
name|routingEntry
argument_list|()
argument_list|)
argument_list|)
operator|.
name|findFirst
argument_list|()
operator|.
name|get
argument_list|()
decl_stmt|;
name|performOnReplica
argument_list|(
name|request
argument_list|,
name|replica
argument_list|)
expr_stmt|;
name|listener
operator|.
name|onResponse
argument_list|(
name|TransportResponse
operator|.
name|Empty
operator|.
name|INSTANCE
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
block|}
block|}
annotation|@
name|Override
DECL|method|failShard
specifier|public
name|void
name|failShard
parameter_list|(
name|ShardRouting
name|replica
parameter_list|,
name|long
name|primaryTerm
parameter_list|,
name|String
name|message
parameter_list|,
name|Exception
name|exception
parameter_list|,
name|Runnable
name|onSuccess
parameter_list|,
name|Consumer
argument_list|<
name|Exception
argument_list|>
name|onPrimaryDemoted
parameter_list|,
name|Consumer
argument_list|<
name|Exception
argument_list|>
name|onIgnoredFailure
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
DECL|method|markShardCopyAsStale
specifier|public
name|void
name|markShardCopyAsStale
parameter_list|(
name|ShardId
name|shardId
parameter_list|,
name|String
name|allocationId
parameter_list|,
name|long
name|primaryTerm
parameter_list|,
name|Runnable
name|onSuccess
parameter_list|,
name|Consumer
argument_list|<
name|Exception
argument_list|>
name|onPrimaryDemoted
parameter_list|,
name|Consumer
argument_list|<
name|Exception
argument_list|>
name|onIgnoredFailure
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
block|}
DECL|class|PrimaryResult
class|class
name|PrimaryResult
implements|implements
name|ReplicationOperation
operator|.
name|PrimaryResult
argument_list|<
name|ReplicaRequest
argument_list|>
block|{
DECL|field|replicaRequest
specifier|final
name|ReplicaRequest
name|replicaRequest
decl_stmt|;
DECL|field|finalResponse
specifier|final
name|Response
name|finalResponse
decl_stmt|;
DECL|method|PrimaryResult
specifier|public
name|PrimaryResult
parameter_list|(
name|ReplicaRequest
name|replicaRequest
parameter_list|,
name|Response
name|finalResponse
parameter_list|)
block|{
name|this
operator|.
name|replicaRequest
operator|=
name|replicaRequest
expr_stmt|;
name|this
operator|.
name|finalResponse
operator|=
name|finalResponse
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|replicaRequest
specifier|public
name|ReplicaRequest
name|replicaRequest
parameter_list|()
block|{
return|return
name|replicaRequest
return|;
block|}
annotation|@
name|Override
DECL|method|setShardInfo
specifier|public
name|void
name|setShardInfo
parameter_list|(
name|ReplicationResponse
operator|.
name|ShardInfo
name|shardInfo
parameter_list|)
block|{
name|finalResponse
operator|.
name|setShardInfo
argument_list|(
name|shardInfo
argument_list|)
expr_stmt|;
block|}
DECL|method|respond
specifier|public
name|void
name|respond
parameter_list|(
name|ActionListener
argument_list|<
name|Response
argument_list|>
name|listener
parameter_list|)
block|{
name|listener
operator|.
name|onResponse
argument_list|(
name|finalResponse
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

begin_class
DECL|class|IndexingAction
class|class
name|IndexingAction
extends|extends
name|ReplicationAction
argument_list|<
name|IndexRequest
argument_list|,
name|IndexRequest
argument_list|,
name|IndexResponse
argument_list|>
block|{
DECL|method|IndexingAction
specifier|public
name|IndexingAction
parameter_list|(
name|IndexRequest
name|request
parameter_list|,
name|ActionListener
argument_list|<
name|IndexResponse
argument_list|>
name|listener
parameter_list|,
name|ReplicationGroup
name|replicationGroup
parameter_list|)
block|{
name|super
argument_list|(
name|request
argument_list|,
name|listener
argument_list|,
name|replicationGroup
argument_list|,
literal|"indexing"
argument_list|)
expr_stmt|;
name|request
operator|.
name|process
argument_list|(
literal|null
argument_list|,
literal|true
argument_list|,
name|request
operator|.
name|index
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|performOnPrimary
specifier|protected
name|PrimaryResult
name|performOnPrimary
parameter_list|(
name|IndexShard
name|primary
parameter_list|,
name|IndexRequest
name|request
parameter_list|)
throws|throws
name|Exception
block|{
name|TransportWriteAction
operator|.
name|WriteResult
argument_list|<
name|IndexResponse
argument_list|>
name|result
init|=
name|TransportIndexAction
operator|.
name|executeIndexRequestOnPrimary
argument_list|(
name|request
argument_list|,
name|primary
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|request
operator|.
name|primaryTerm
argument_list|(
name|primary
operator|.
name|getPrimaryTerm
argument_list|()
argument_list|)
expr_stmt|;
name|TransportWriteActionTestHelper
operator|.
name|performPostWriteActions
argument_list|(
name|primary
argument_list|,
name|request
argument_list|,
name|result
operator|.
name|getLocation
argument_list|()
argument_list|,
name|logger
argument_list|)
expr_stmt|;
return|return
operator|new
name|PrimaryResult
argument_list|(
name|request
argument_list|,
name|result
operator|.
name|getResponse
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|performOnReplica
specifier|protected
name|void
name|performOnReplica
parameter_list|(
name|IndexRequest
name|request
parameter_list|,
name|IndexShard
name|replica
parameter_list|)
block|{
name|Engine
operator|.
name|Index
name|index
init|=
name|TransportIndexAction
operator|.
name|executeIndexRequestOnReplica
argument_list|(
name|request
argument_list|,
name|replica
argument_list|)
decl_stmt|;
name|TransportWriteActionTestHelper
operator|.
name|performPostWriteActions
argument_list|(
name|replica
argument_list|,
name|request
argument_list|,
name|index
operator|.
name|getTranslogLocation
argument_list|()
argument_list|,
name|logger
argument_list|)
expr_stmt|;
block|}
block|}
end_class

unit|}
end_unit

