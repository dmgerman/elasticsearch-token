begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.shard
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|shard
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchIllegalArgumentException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchIllegalStateException
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
name|routing
operator|.
name|MutableShardRouting
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
name|ShardRoutingState
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
name|logging
operator|.
name|ESLogger
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
name|IndexService
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
name|test
operator|.
name|ElasticsearchSingleNodeTest
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
name|nio
operator|.
name|file
operator|.
name|Path
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
name|Set
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|settings
operator|.
name|ImmutableSettings
operator|.
name|settingsBuilder
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|hamcrest
operator|.
name|ElasticsearchAssertions
operator|.
name|assertAcked
import|;
end_import

begin_comment
comment|/**  * Simple unit-test IndexShard related operations.  */
end_comment

begin_class
DECL|class|IndexShardTests
specifier|public
class|class
name|IndexShardTests
extends|extends
name|ElasticsearchSingleNodeTest
block|{
DECL|method|testFlushOnDeleteSetting
specifier|public
name|void
name|testFlushOnDeleteSetting
parameter_list|()
throws|throws
name|Exception
block|{
name|boolean
name|initValue
init|=
name|randomBoolean
argument_list|()
decl_stmt|;
name|createIndex
argument_list|(
literal|"test"
argument_list|,
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
name|IndexShard
operator|.
name|INDEX_FLUSH_ON_CLOSE
argument_list|,
name|initValue
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
name|IndicesService
name|indicesService
init|=
name|getInstanceFromNode
argument_list|(
name|IndicesService
operator|.
name|class
argument_list|)
decl_stmt|;
name|IndexService
name|test
init|=
name|indicesService
operator|.
name|indexService
argument_list|(
literal|"test"
argument_list|)
decl_stmt|;
name|IndexShard
name|shard
init|=
name|test
operator|.
name|shard
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|initValue
argument_list|,
name|shard
operator|.
name|isFlushOnClose
argument_list|()
argument_list|)
expr_stmt|;
specifier|final
name|boolean
name|newValue
init|=
operator|!
name|initValue
decl_stmt|;
name|assertAcked
argument_list|(
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareUpdateSettings
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setSettings
argument_list|(
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
name|IndexShard
operator|.
name|INDEX_FLUSH_ON_CLOSE
argument_list|,
name|newValue
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|newValue
argument_list|,
name|shard
operator|.
name|isFlushOnClose
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
name|assertAcked
argument_list|(
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareUpdateSettings
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setSettings
argument_list|(
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
name|IndexShard
operator|.
name|INDEX_FLUSH_ON_CLOSE
argument_list|,
literal|"FOOBAR"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"exception expected"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ElasticsearchIllegalArgumentException
name|ex
parameter_list|)
block|{          }
name|assertEquals
argument_list|(
name|newValue
argument_list|,
name|shard
operator|.
name|isFlushOnClose
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testWriteShardState
specifier|public
name|void
name|testWriteShardState
parameter_list|()
throws|throws
name|Exception
block|{
try|try
init|(
name|NodeEnvironment
name|env
init|=
name|newNodeEnvironment
argument_list|()
init|)
block|{
name|ShardId
name|id
init|=
operator|new
name|ShardId
argument_list|(
literal|"foo"
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|long
name|version
init|=
name|between
argument_list|(
literal|1
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
operator|/
literal|2
argument_list|)
decl_stmt|;
name|boolean
name|primary
init|=
name|randomBoolean
argument_list|()
decl_stmt|;
name|ShardStateMetaData
name|state1
init|=
operator|new
name|ShardStateMetaData
argument_list|(
name|version
argument_list|,
name|primary
argument_list|,
literal|"foo"
argument_list|)
decl_stmt|;
name|write
argument_list|(
name|state1
argument_list|,
name|env
operator|.
name|availableShardPaths
argument_list|(
name|id
argument_list|)
argument_list|)
expr_stmt|;
name|ShardStateMetaData
name|shardStateMetaData
init|=
name|load
argument_list|(
name|logger
argument_list|,
name|env
operator|.
name|availableShardPaths
argument_list|(
name|id
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|shardStateMetaData
argument_list|,
name|state1
argument_list|)
expr_stmt|;
name|ShardStateMetaData
name|state2
init|=
operator|new
name|ShardStateMetaData
argument_list|(
name|version
argument_list|,
name|primary
argument_list|,
literal|"foo"
argument_list|)
decl_stmt|;
name|write
argument_list|(
name|state2
argument_list|,
name|env
operator|.
name|availableShardPaths
argument_list|(
name|id
argument_list|)
argument_list|)
expr_stmt|;
name|shardStateMetaData
operator|=
name|load
argument_list|(
name|logger
argument_list|,
name|env
operator|.
name|availableShardPaths
argument_list|(
name|id
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|shardStateMetaData
argument_list|,
name|state1
argument_list|)
expr_stmt|;
name|ShardStateMetaData
name|state3
init|=
operator|new
name|ShardStateMetaData
argument_list|(
name|version
operator|+
literal|1
argument_list|,
name|primary
argument_list|,
literal|"foo"
argument_list|)
decl_stmt|;
name|write
argument_list|(
name|state3
argument_list|,
name|env
operator|.
name|availableShardPaths
argument_list|(
name|id
argument_list|)
argument_list|)
expr_stmt|;
name|shardStateMetaData
operator|=
name|load
argument_list|(
name|logger
argument_list|,
name|env
operator|.
name|availableShardPaths
argument_list|(
name|id
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|shardStateMetaData
argument_list|,
name|state3
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"foo"
argument_list|,
name|state3
operator|.
name|indexUUID
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testPersistenceStateMetadataPersistence
specifier|public
name|void
name|testPersistenceStateMetadataPersistence
parameter_list|()
throws|throws
name|Exception
block|{
name|createIndex
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
name|IndicesService
name|indicesService
init|=
name|getInstanceFromNode
argument_list|(
name|IndicesService
operator|.
name|class
argument_list|)
decl_stmt|;
name|NodeEnvironment
name|env
init|=
name|getInstanceFromNode
argument_list|(
name|NodeEnvironment
operator|.
name|class
argument_list|)
decl_stmt|;
name|IndexService
name|test
init|=
name|indicesService
operator|.
name|indexService
argument_list|(
literal|"test"
argument_list|)
decl_stmt|;
name|IndexShard
name|shard
init|=
name|test
operator|.
name|shard
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|ShardStateMetaData
name|shardStateMetaData
init|=
name|load
argument_list|(
name|logger
argument_list|,
name|env
operator|.
name|availableShardPaths
argument_list|(
name|shard
operator|.
name|shardId
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|getShardStateMetadata
argument_list|(
name|shard
argument_list|)
argument_list|,
name|shardStateMetaData
argument_list|)
expr_stmt|;
name|ShardRouting
name|routing
init|=
operator|new
name|MutableShardRouting
argument_list|(
name|shard
operator|.
name|shardRouting
argument_list|,
name|shard
operator|.
name|shardRouting
operator|.
name|version
argument_list|()
operator|+
literal|1
argument_list|)
decl_stmt|;
name|shard
operator|.
name|updateRoutingEntry
argument_list|(
name|routing
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|shardStateMetaData
operator|=
name|load
argument_list|(
name|logger
argument_list|,
name|env
operator|.
name|availableShardPaths
argument_list|(
name|shard
operator|.
name|shardId
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|shardStateMetaData
argument_list|,
name|getShardStateMetadata
argument_list|(
name|shard
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|shardStateMetaData
argument_list|,
operator|new
name|ShardStateMetaData
argument_list|(
name|routing
operator|.
name|version
argument_list|()
argument_list|,
name|routing
operator|.
name|primary
argument_list|()
argument_list|,
name|shard
operator|.
name|indexSettings
operator|.
name|get
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_UUID
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|routing
operator|=
operator|new
name|MutableShardRouting
argument_list|(
name|shard
operator|.
name|shardRouting
argument_list|,
name|shard
operator|.
name|shardRouting
operator|.
name|version
argument_list|()
operator|+
literal|1
argument_list|)
expr_stmt|;
name|shard
operator|.
name|updateRoutingEntry
argument_list|(
name|routing
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|shardStateMetaData
operator|=
name|load
argument_list|(
name|logger
argument_list|,
name|env
operator|.
name|availableShardPaths
argument_list|(
name|shard
operator|.
name|shardId
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|shardStateMetaData
argument_list|,
name|getShardStateMetadata
argument_list|(
name|shard
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|shardStateMetaData
argument_list|,
operator|new
name|ShardStateMetaData
argument_list|(
name|routing
operator|.
name|version
argument_list|()
argument_list|,
name|routing
operator|.
name|primary
argument_list|()
argument_list|,
name|shard
operator|.
name|indexSettings
operator|.
name|get
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_UUID
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|routing
operator|=
operator|new
name|MutableShardRouting
argument_list|(
name|shard
operator|.
name|shardRouting
argument_list|,
name|shard
operator|.
name|shardRouting
operator|.
name|version
argument_list|()
operator|+
literal|1
argument_list|)
expr_stmt|;
name|shard
operator|.
name|updateRoutingEntry
argument_list|(
name|routing
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|shardStateMetaData
operator|=
name|load
argument_list|(
name|logger
argument_list|,
name|env
operator|.
name|availableShardPaths
argument_list|(
name|shard
operator|.
name|shardId
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|shardStateMetaData
argument_list|,
name|getShardStateMetadata
argument_list|(
name|shard
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|shardStateMetaData
argument_list|,
operator|new
name|ShardStateMetaData
argument_list|(
name|routing
operator|.
name|version
argument_list|()
argument_list|,
name|routing
operator|.
name|primary
argument_list|()
argument_list|,
name|shard
operator|.
name|indexSettings
operator|.
name|get
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_UUID
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// test if we still write it even if the shard is not active
name|MutableShardRouting
name|inactiveRouting
init|=
operator|new
name|MutableShardRouting
argument_list|(
name|shard
operator|.
name|shardRouting
operator|.
name|index
argument_list|()
argument_list|,
name|shard
operator|.
name|shardRouting
operator|.
name|shardId
argument_list|()
operator|.
name|id
argument_list|()
argument_list|,
name|shard
operator|.
name|shardRouting
operator|.
name|currentNodeId
argument_list|()
argument_list|,
literal|true
argument_list|,
name|ShardRoutingState
operator|.
name|INITIALIZING
argument_list|,
name|shard
operator|.
name|shardRouting
operator|.
name|version
argument_list|()
operator|+
literal|1
argument_list|)
decl_stmt|;
name|shard
operator|.
name|persistMetadata
argument_list|(
name|inactiveRouting
argument_list|,
name|shard
operator|.
name|shardRouting
argument_list|)
expr_stmt|;
name|shardStateMetaData
operator|=
name|load
argument_list|(
name|logger
argument_list|,
name|env
operator|.
name|availableShardPaths
argument_list|(
name|shard
operator|.
name|shardId
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"inactive shard state shouldn't be persisted"
argument_list|,
name|shardStateMetaData
argument_list|,
name|getShardStateMetadata
argument_list|(
name|shard
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"inactive shard state shouldn't be persisted"
argument_list|,
name|shardStateMetaData
argument_list|,
operator|new
name|ShardStateMetaData
argument_list|(
name|routing
operator|.
name|version
argument_list|()
argument_list|,
name|routing
operator|.
name|primary
argument_list|()
argument_list|,
name|shard
operator|.
name|indexSettings
operator|.
name|get
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_UUID
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|shard
operator|.
name|updateRoutingEntry
argument_list|(
operator|new
name|MutableShardRouting
argument_list|(
name|shard
operator|.
name|shardRouting
argument_list|,
name|shard
operator|.
name|shardRouting
operator|.
name|version
argument_list|()
operator|+
literal|1
argument_list|)
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|shardStateMetaData
operator|=
name|load
argument_list|(
name|logger
argument_list|,
name|env
operator|.
name|availableShardPaths
argument_list|(
name|shard
operator|.
name|shardId
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
literal|"shard state persisted despite of persist=false"
argument_list|,
name|shardStateMetaData
operator|.
name|equals
argument_list|(
name|getShardStateMetadata
argument_list|(
name|shard
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"shard state persisted despite of persist=false"
argument_list|,
name|shardStateMetaData
argument_list|,
operator|new
name|ShardStateMetaData
argument_list|(
name|routing
operator|.
name|version
argument_list|()
argument_list|,
name|routing
operator|.
name|primary
argument_list|()
argument_list|,
name|shard
operator|.
name|indexSettings
operator|.
name|get
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_UUID
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|routing
operator|=
operator|new
name|MutableShardRouting
argument_list|(
name|shard
operator|.
name|shardRouting
argument_list|,
name|shard
operator|.
name|shardRouting
operator|.
name|version
argument_list|()
operator|+
literal|1
argument_list|)
expr_stmt|;
name|shard
operator|.
name|updateRoutingEntry
argument_list|(
name|routing
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|shardStateMetaData
operator|=
name|load
argument_list|(
name|logger
argument_list|,
name|env
operator|.
name|availableShardPaths
argument_list|(
name|shard
operator|.
name|shardId
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|shardStateMetaData
argument_list|,
name|getShardStateMetadata
argument_list|(
name|shard
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|shardStateMetaData
argument_list|,
operator|new
name|ShardStateMetaData
argument_list|(
name|routing
operator|.
name|version
argument_list|()
argument_list|,
name|routing
operator|.
name|primary
argument_list|()
argument_list|,
name|shard
operator|.
name|indexSettings
operator|.
name|get
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_UUID
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testDeleteShardState
specifier|public
name|void
name|testDeleteShardState
parameter_list|()
throws|throws
name|IOException
block|{
name|createIndex
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
name|IndicesService
name|indicesService
init|=
name|getInstanceFromNode
argument_list|(
name|IndicesService
operator|.
name|class
argument_list|)
decl_stmt|;
name|NodeEnvironment
name|env
init|=
name|getInstanceFromNode
argument_list|(
name|NodeEnvironment
operator|.
name|class
argument_list|)
decl_stmt|;
name|IndexService
name|test
init|=
name|indicesService
operator|.
name|indexService
argument_list|(
literal|"test"
argument_list|)
decl_stmt|;
name|IndexShard
name|shard
init|=
name|test
operator|.
name|shard
argument_list|(
literal|0
argument_list|)
decl_stmt|;
try|try
block|{
name|shard
operator|.
name|deleteShardState
argument_list|()
expr_stmt|;
name|fail
argument_list|(
literal|"shard is active metadata delete must fail"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ElasticsearchIllegalStateException
name|ex
parameter_list|)
block|{
comment|// fine - only delete if non-active
block|}
name|ShardRouting
name|routing
init|=
name|shard
operator|.
name|routingEntry
argument_list|()
decl_stmt|;
name|ShardStateMetaData
name|shardStateMetaData
init|=
name|load
argument_list|(
name|logger
argument_list|,
name|env
operator|.
name|availableShardPaths
argument_list|(
name|shard
operator|.
name|shardId
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|shardStateMetaData
argument_list|,
name|getShardStateMetadata
argument_list|(
name|shard
argument_list|)
argument_list|)
expr_stmt|;
name|routing
operator|=
operator|new
name|MutableShardRouting
argument_list|(
name|shard
operator|.
name|shardId
operator|.
name|index
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|shard
operator|.
name|shardId
operator|.
name|id
argument_list|()
argument_list|,
name|routing
operator|.
name|currentNodeId
argument_list|()
argument_list|,
name|routing
operator|.
name|primary
argument_list|()
argument_list|,
name|ShardRoutingState
operator|.
name|INITIALIZING
argument_list|,
name|shard
operator|.
name|shardRouting
operator|.
name|version
argument_list|()
operator|+
literal|1
argument_list|)
expr_stmt|;
name|shard
operator|.
name|updateRoutingEntry
argument_list|(
name|routing
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|shard
operator|.
name|deleteShardState
argument_list|()
expr_stmt|;
name|assertNull
argument_list|(
literal|"no shard state expected after delete on initializing"
argument_list|,
name|load
argument_list|(
name|logger
argument_list|,
name|env
operator|.
name|availableShardPaths
argument_list|(
name|shard
operator|.
name|shardId
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|getShardStateMetadata
name|ShardStateMetaData
name|getShardStateMetadata
parameter_list|(
name|IndexShard
name|shard
parameter_list|)
block|{
name|ShardRouting
name|shardRouting
init|=
name|shard
operator|.
name|routingEntry
argument_list|()
decl_stmt|;
if|if
condition|(
name|shardRouting
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
else|else
block|{
return|return
operator|new
name|ShardStateMetaData
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
argument_list|,
name|shard
operator|.
name|indexSettings
operator|.
name|get
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_UUID
argument_list|)
argument_list|)
return|;
block|}
block|}
DECL|method|testShardStateMetaHashCodeEquals
specifier|public
name|void
name|testShardStateMetaHashCodeEquals
parameter_list|()
block|{
name|ShardStateMetaData
name|meta
init|=
operator|new
name|ShardStateMetaData
argument_list|(
name|randomLong
argument_list|()
argument_list|,
name|randomBoolean
argument_list|()
argument_list|,
name|randomRealisticUnicodeOfCodepointLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|meta
argument_list|,
operator|new
name|ShardStateMetaData
argument_list|(
name|meta
operator|.
name|version
argument_list|,
name|meta
operator|.
name|primary
argument_list|,
name|meta
operator|.
name|indexUUID
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|meta
operator|.
name|hashCode
argument_list|()
argument_list|,
operator|new
name|ShardStateMetaData
argument_list|(
name|meta
operator|.
name|version
argument_list|,
name|meta
operator|.
name|primary
argument_list|,
name|meta
operator|.
name|indexUUID
argument_list|)
operator|.
name|hashCode
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|meta
operator|.
name|equals
argument_list|(
operator|new
name|ShardStateMetaData
argument_list|(
name|meta
operator|.
name|version
argument_list|,
operator|!
name|meta
operator|.
name|primary
argument_list|,
name|meta
operator|.
name|indexUUID
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|meta
operator|.
name|equals
argument_list|(
operator|new
name|ShardStateMetaData
argument_list|(
name|meta
operator|.
name|version
operator|+
literal|1
argument_list|,
name|meta
operator|.
name|primary
argument_list|,
name|meta
operator|.
name|indexUUID
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|meta
operator|.
name|equals
argument_list|(
operator|new
name|ShardStateMetaData
argument_list|(
name|meta
operator|.
name|version
argument_list|,
operator|!
name|meta
operator|.
name|primary
argument_list|,
name|meta
operator|.
name|indexUUID
operator|+
literal|"foo"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|Set
argument_list|<
name|Integer
argument_list|>
name|hashCodes
init|=
operator|new
name|HashSet
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
literal|30
condition|;
name|i
operator|++
control|)
block|{
comment|// just a sanity check that we impl hashcode
name|meta
operator|=
operator|new
name|ShardStateMetaData
argument_list|(
name|randomLong
argument_list|()
argument_list|,
name|randomBoolean
argument_list|()
argument_list|,
name|randomRealisticUnicodeOfCodepointLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
argument_list|)
expr_stmt|;
name|hashCodes
operator|.
name|add
argument_list|(
name|meta
operator|.
name|hashCode
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|assertTrue
argument_list|(
literal|"more than one unique hashcode expected but got: "
operator|+
name|hashCodes
operator|.
name|size
argument_list|()
argument_list|,
name|hashCodes
operator|.
name|size
argument_list|()
operator|>
literal|1
argument_list|)
expr_stmt|;
block|}
DECL|method|load
specifier|public
specifier|static
name|ShardStateMetaData
name|load
parameter_list|(
name|ESLogger
name|logger
parameter_list|,
name|Path
modifier|...
name|shardPaths
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|ShardStateMetaData
operator|.
name|FORMAT
operator|.
name|loadLatestState
argument_list|(
name|logger
argument_list|,
name|shardPaths
argument_list|)
return|;
block|}
DECL|method|write
specifier|public
specifier|static
name|void
name|write
parameter_list|(
name|ShardStateMetaData
name|shardStateMetaData
parameter_list|,
name|Path
modifier|...
name|shardPaths
parameter_list|)
throws|throws
name|IOException
block|{
name|ShardStateMetaData
operator|.
name|FORMAT
operator|.
name|write
argument_list|(
name|shardStateMetaData
argument_list|,
name|shardStateMetaData
operator|.
name|version
argument_list|,
name|shardPaths
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

