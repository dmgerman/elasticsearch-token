begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.indices.memory
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|memory
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
name|base
operator|.
name|Predicate
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
name|util
operator|.
name|concurrent
operator|.
name|EsExecutors
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
name|EngineConfig
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
name|node
operator|.
name|internal
operator|.
name|InternalSettingsPreparer
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
name|ElasticsearchIntegrationTest
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_class
annotation|@
name|ElasticsearchIntegrationTest
operator|.
name|ClusterScope
argument_list|(
name|scope
operator|=
name|ElasticsearchIntegrationTest
operator|.
name|Scope
operator|.
name|TEST
argument_list|,
name|numDataNodes
operator|=
literal|0
argument_list|)
DECL|class|IndexingMemoryControllerTests
specifier|public
class|class
name|IndexingMemoryControllerTests
extends|extends
name|ElasticsearchIntegrationTest
block|{
annotation|@
name|Test
DECL|method|testIndexBufferSizeUpdateAfterCreationRemoval
specifier|public
name|void
name|testIndexBufferSizeUpdateAfterCreationRemoval
parameter_list|()
throws|throws
name|InterruptedException
block|{
name|createNode
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
expr_stmt|;
name|prepareCreate
argument_list|(
literal|"test1"
argument_list|)
operator|.
name|setSettings
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_NUMBER_OF_SHARDS
argument_list|,
literal|1
argument_list|,
name|IndexMetaData
operator|.
name|SETTING_NUMBER_OF_REPLICAS
argument_list|,
literal|0
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
specifier|final
name|IndexShard
name|shard1
init|=
name|internalCluster
argument_list|()
operator|.
name|getInstance
argument_list|(
name|IndicesService
operator|.
name|class
argument_list|)
operator|.
name|indexService
argument_list|(
literal|"test1"
argument_list|)
operator|.
name|shard
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|prepareCreate
argument_list|(
literal|"test2"
argument_list|)
operator|.
name|setSettings
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_NUMBER_OF_SHARDS
argument_list|,
literal|1
argument_list|,
name|IndexMetaData
operator|.
name|SETTING_NUMBER_OF_REPLICAS
argument_list|,
literal|0
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
specifier|final
name|IndexShard
name|shard2
init|=
name|internalCluster
argument_list|()
operator|.
name|getInstance
argument_list|(
name|IndicesService
operator|.
name|class
argument_list|)
operator|.
name|indexService
argument_list|(
literal|"test2"
argument_list|)
operator|.
name|shard
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|final
name|long
name|expected1ShardSize
init|=
name|internalCluster
argument_list|()
operator|.
name|getInstance
argument_list|(
name|IndexingMemoryController
operator|.
name|class
argument_list|)
operator|.
name|indexingBufferSize
argument_list|()
operator|.
name|bytes
argument_list|()
decl_stmt|;
specifier|final
name|long
name|expected2ShardsSize
init|=
name|expected1ShardSize
operator|/
literal|2
decl_stmt|;
name|boolean
name|success
init|=
name|awaitBusy
argument_list|(
operator|new
name|Predicate
argument_list|<
name|Object
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|apply
parameter_list|(
name|Object
name|input
parameter_list|)
block|{
return|return
name|shard1
operator|.
name|engine
argument_list|()
operator|.
name|config
argument_list|()
operator|.
name|getIndexingBufferSize
argument_list|()
operator|.
name|bytes
argument_list|()
operator|<=
name|expected2ShardsSize
operator|&&
name|shard2
operator|.
name|engine
argument_list|()
operator|.
name|config
argument_list|()
operator|.
name|getIndexingBufferSize
argument_list|()
operator|.
name|bytes
argument_list|()
operator|<=
name|expected2ShardsSize
return|;
block|}
block|}
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|success
condition|)
block|{
name|fail
argument_list|(
literal|"failed to update shard indexing buffer size. expected ["
operator|+
name|expected2ShardsSize
operator|+
literal|"] shard1 ["
operator|+
name|shard1
operator|.
name|engine
argument_list|()
operator|.
name|config
argument_list|()
operator|.
name|getIndexingBufferSize
argument_list|()
operator|.
name|bytes
argument_list|()
operator|+
literal|"] shard2  ["
operator|+
name|shard2
operator|.
name|engine
argument_list|()
operator|.
name|config
argument_list|()
operator|.
name|getIndexingBufferSize
argument_list|()
operator|.
name|bytes
argument_list|()
operator|+
literal|"]"
argument_list|)
expr_stmt|;
block|}
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareDelete
argument_list|(
literal|"test2"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|success
operator|=
name|awaitBusy
argument_list|(
operator|new
name|Predicate
argument_list|<
name|Object
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|apply
parameter_list|(
name|Object
name|input
parameter_list|)
block|{
return|return
name|shard1
operator|.
name|engine
argument_list|()
operator|.
name|config
argument_list|()
operator|.
name|getIndexingBufferSize
argument_list|()
operator|.
name|bytes
argument_list|()
operator|>=
name|expected1ShardSize
return|;
block|}
block|}
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|success
condition|)
block|{
name|fail
argument_list|(
literal|"failed to update shard indexing buffer size after deleting shards. expected ["
operator|+
name|expected1ShardSize
operator|+
literal|"] got ["
operator|+
name|shard1
operator|.
name|engine
argument_list|()
operator|.
name|config
argument_list|()
operator|.
name|getIndexingBufferSize
argument_list|()
operator|.
name|bytes
argument_list|()
operator|+
literal|"]"
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
DECL|method|testIndexBufferSizeUpdateInactiveShard
specifier|public
name|void
name|testIndexBufferSizeUpdateInactiveShard
parameter_list|()
throws|throws
name|InterruptedException
block|{
name|createNode
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"indices.memory.shard_inactive_time"
argument_list|,
literal|"100ms"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|prepareCreate
argument_list|(
literal|"test1"
argument_list|)
operator|.
name|setSettings
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_NUMBER_OF_SHARDS
argument_list|,
literal|1
argument_list|,
name|IndexMetaData
operator|.
name|SETTING_NUMBER_OF_REPLICAS
argument_list|,
literal|0
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
specifier|final
name|IndexShard
name|shard1
init|=
name|internalCluster
argument_list|()
operator|.
name|getInstance
argument_list|(
name|IndicesService
operator|.
name|class
argument_list|)
operator|.
name|indexService
argument_list|(
literal|"test1"
argument_list|)
operator|.
name|shard
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|boolean
name|success
init|=
name|awaitBusy
argument_list|(
operator|new
name|Predicate
argument_list|<
name|Object
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|apply
parameter_list|(
name|Object
name|input
parameter_list|)
block|{
return|return
name|shard1
operator|.
name|engine
argument_list|()
operator|.
name|config
argument_list|()
operator|.
name|getIndexingBufferSize
argument_list|()
operator|.
name|bytes
argument_list|()
operator|==
name|EngineConfig
operator|.
name|INACTIVE_SHARD_INDEXING_BUFFER
operator|.
name|bytes
argument_list|()
return|;
block|}
block|}
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|success
condition|)
block|{
name|fail
argument_list|(
literal|"failed to update shard indexing buffer size due to inactive state. expected ["
operator|+
name|EngineConfig
operator|.
name|INACTIVE_SHARD_INDEXING_BUFFER
operator|+
literal|"] got ["
operator|+
name|shard1
operator|.
name|engine
argument_list|()
operator|.
name|config
argument_list|()
operator|.
name|getIndexingBufferSize
argument_list|()
operator|.
name|bytes
argument_list|()
operator|+
literal|"]"
argument_list|)
expr_stmt|;
block|}
name|index
argument_list|(
literal|"test1"
argument_list|,
literal|"type"
argument_list|,
literal|"1"
argument_list|,
literal|"f"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|success
operator|=
name|awaitBusy
argument_list|(
operator|new
name|Predicate
argument_list|<
name|Object
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|apply
parameter_list|(
name|Object
name|input
parameter_list|)
block|{
return|return
name|shard1
operator|.
name|engine
argument_list|()
operator|.
name|config
argument_list|()
operator|.
name|getIndexingBufferSize
argument_list|()
operator|.
name|bytes
argument_list|()
operator|>
name|EngineConfig
operator|.
name|INACTIVE_SHARD_INDEXING_BUFFER
operator|.
name|bytes
argument_list|()
return|;
block|}
block|}
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|success
condition|)
block|{
name|fail
argument_list|(
literal|"failed to update shard indexing buffer size due to inactive state. expected something larger then ["
operator|+
name|EngineConfig
operator|.
name|INACTIVE_SHARD_INDEXING_BUFFER
operator|+
literal|"] got ["
operator|+
name|shard1
operator|.
name|engine
argument_list|()
operator|.
name|config
argument_list|()
operator|.
name|getIndexingBufferSize
argument_list|()
operator|.
name|bytes
argument_list|()
operator|+
literal|"]"
argument_list|)
expr_stmt|;
block|}
name|flush
argument_list|()
expr_stmt|;
comment|// clean translogs
name|success
operator|=
name|awaitBusy
argument_list|(
operator|new
name|Predicate
argument_list|<
name|Object
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|apply
parameter_list|(
name|Object
name|input
parameter_list|)
block|{
return|return
name|shard1
operator|.
name|engine
argument_list|()
operator|.
name|config
argument_list|()
operator|.
name|getIndexingBufferSize
argument_list|()
operator|.
name|bytes
argument_list|()
operator|==
name|EngineConfig
operator|.
name|INACTIVE_SHARD_INDEXING_BUFFER
operator|.
name|bytes
argument_list|()
return|;
block|}
block|}
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|success
condition|)
block|{
name|fail
argument_list|(
literal|"failed to update shard indexing buffer size due to inactive state. expected ["
operator|+
name|EngineConfig
operator|.
name|INACTIVE_SHARD_INDEXING_BUFFER
operator|+
literal|"] got ["
operator|+
name|shard1
operator|.
name|engine
argument_list|()
operator|.
name|config
argument_list|()
operator|.
name|getIndexingBufferSize
argument_list|()
operator|.
name|bytes
argument_list|()
operator|+
literal|"]"
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|createNode
specifier|private
name|void
name|createNode
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|internalCluster
argument_list|()
operator|.
name|startNode
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|ClusterName
operator|.
name|SETTING
argument_list|,
literal|"IndexingMemoryControllerTests"
argument_list|)
operator|.
name|put
argument_list|(
literal|"node.name"
argument_list|,
literal|"IndexingMemoryControllerTests"
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
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_NUMBER_OF_REPLICAS
argument_list|,
literal|0
argument_list|)
operator|.
name|put
argument_list|(
name|EsExecutors
operator|.
name|PROCESSORS
argument_list|,
literal|1
argument_list|)
comment|// limit the number of threads created
operator|.
name|put
argument_list|(
literal|"http.enabled"
argument_list|,
literal|false
argument_list|)
operator|.
name|put
argument_list|(
name|InternalSettingsPreparer
operator|.
name|IGNORE_SYSTEM_PROPERTIES_SETTING
argument_list|,
literal|true
argument_list|)
comment|// make sure we get what we set :)
operator|.
name|put
argument_list|(
literal|"indices.memory.interval"
argument_list|,
literal|"100ms"
argument_list|)
operator|.
name|put
argument_list|(
name|settings
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit
