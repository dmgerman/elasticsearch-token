begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.indices
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|indices
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
name|LockObtainFailedException
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
name|admin
operator|.
name|indices
operator|.
name|alias
operator|.
name|IndicesAliasesRequest
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
name|AliasAction
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
name|IndexGraveyard
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
name|MetaData
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
name|service
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
name|common
operator|.
name|UUIDs
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
name|gateway
operator|.
name|GatewayMetaState
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|gateway
operator|.
name|LocalAllocateDangledIndices
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|gateway
operator|.
name|MetaStateService
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
name|test
operator|.
name|ESSingleNodeTestCase
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
name|IndexSettingsModule
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
name|Arrays
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
name|CountDownLatch
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
name|test
operator|.
name|hamcrest
operator|.
name|ElasticsearchAssertions
operator|.
name|assertAcked
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
name|assertHitCount
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
name|containsString
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

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|not
import|;
end_import

begin_class
DECL|class|IndicesServiceTests
specifier|public
class|class
name|IndicesServiceTests
extends|extends
name|ESSingleNodeTestCase
block|{
DECL|method|getIndicesService
specifier|public
name|IndicesService
name|getIndicesService
parameter_list|()
block|{
return|return
name|getInstanceFromNode
argument_list|(
name|IndicesService
operator|.
name|class
argument_list|)
return|;
block|}
DECL|method|getNodeEnvironment
specifier|public
name|NodeEnvironment
name|getNodeEnvironment
parameter_list|()
block|{
return|return
name|getInstanceFromNode
argument_list|(
name|NodeEnvironment
operator|.
name|class
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|resetNodeAfterTest
specifier|protected
name|boolean
name|resetNodeAfterTest
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
DECL|method|testCanDeleteIndexContent
specifier|public
name|void
name|testCanDeleteIndexContent
parameter_list|()
throws|throws
name|IOException
block|{
specifier|final
name|IndicesService
name|indicesService
init|=
name|getIndicesService
argument_list|()
decl_stmt|;
name|IndexSettings
name|idxSettings
init|=
name|IndexSettingsModule
operator|.
name|newIndexSettings
argument_list|(
literal|"test"
argument_list|,
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_SHADOW_REPLICAS
argument_list|,
literal|true
argument_list|)
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_DATA_PATH
argument_list|,
literal|"/foo/bar"
argument_list|)
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_NUMBER_OF_SHARDS
argument_list|,
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|4
argument_list|)
argument_list|)
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_NUMBER_OF_REPLICAS
argument_list|,
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|3
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
literal|"shard on shared filesystem"
argument_list|,
name|indicesService
operator|.
name|canDeleteIndexContents
argument_list|(
name|idxSettings
operator|.
name|getIndex
argument_list|()
argument_list|,
name|idxSettings
argument_list|)
argument_list|)
expr_stmt|;
specifier|final
name|IndexMetaData
operator|.
name|Builder
name|newIndexMetaData
init|=
name|IndexMetaData
operator|.
name|builder
argument_list|(
name|idxSettings
operator|.
name|getIndexMetaData
argument_list|()
argument_list|)
decl_stmt|;
name|newIndexMetaData
operator|.
name|state
argument_list|(
name|IndexMetaData
operator|.
name|State
operator|.
name|CLOSE
argument_list|)
expr_stmt|;
name|idxSettings
operator|=
name|IndexSettingsModule
operator|.
name|newIndexSettings
argument_list|(
name|newIndexMetaData
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"shard on shared filesystem, but closed, so it should be deletable"
argument_list|,
name|indicesService
operator|.
name|canDeleteIndexContents
argument_list|(
name|idxSettings
operator|.
name|getIndex
argument_list|()
argument_list|,
name|idxSettings
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testCanDeleteShardContent
specifier|public
name|void
name|testCanDeleteShardContent
parameter_list|()
block|{
name|IndicesService
name|indicesService
init|=
name|getIndicesService
argument_list|()
decl_stmt|;
name|IndexMetaData
name|meta
init|=
name|IndexMetaData
operator|.
name|builder
argument_list|(
literal|"test"
argument_list|)
operator|.
name|settings
argument_list|(
name|settings
argument_list|(
name|Version
operator|.
name|CURRENT
argument_list|)
argument_list|)
operator|.
name|numberOfShards
argument_list|(
literal|1
argument_list|)
operator|.
name|numberOfReplicas
argument_list|(
literal|1
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|IndexSettings
name|indexSettings
init|=
name|IndexSettingsModule
operator|.
name|newIndexSettings
argument_list|(
literal|"test"
argument_list|,
name|meta
operator|.
name|getSettings
argument_list|()
argument_list|)
decl_stmt|;
name|ShardId
name|shardId
init|=
operator|new
name|ShardId
argument_list|(
name|meta
operator|.
name|getIndex
argument_list|()
argument_list|,
literal|0
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
literal|"no shard location"
argument_list|,
name|indicesService
operator|.
name|canDeleteShardContent
argument_list|(
name|shardId
argument_list|,
name|indexSettings
argument_list|)
argument_list|)
expr_stmt|;
name|IndexService
name|test
init|=
name|createIndex
argument_list|(
literal|"test"
argument_list|)
decl_stmt|;
name|shardId
operator|=
operator|new
name|ShardId
argument_list|(
name|test
operator|.
name|index
argument_list|()
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|test
operator|.
name|hasShard
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
literal|"shard is allocated"
argument_list|,
name|indicesService
operator|.
name|canDeleteShardContent
argument_list|(
name|shardId
argument_list|,
name|test
operator|.
name|getIndexSettings
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|test
operator|.
name|removeShard
argument_list|(
literal|0
argument_list|,
literal|"boom"
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"shard is removed"
argument_list|,
name|indicesService
operator|.
name|canDeleteShardContent
argument_list|(
name|shardId
argument_list|,
name|test
operator|.
name|getIndexSettings
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|ShardId
name|notAllocated
init|=
operator|new
name|ShardId
argument_list|(
name|test
operator|.
name|index
argument_list|()
argument_list|,
literal|100
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
literal|"shard that was never on this node should NOT be deletable"
argument_list|,
name|indicesService
operator|.
name|canDeleteShardContent
argument_list|(
name|notAllocated
argument_list|,
name|test
operator|.
name|getIndexSettings
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testDeleteIndexStore
specifier|public
name|void
name|testDeleteIndexStore
parameter_list|()
throws|throws
name|Exception
block|{
name|IndicesService
name|indicesService
init|=
name|getIndicesService
argument_list|()
decl_stmt|;
name|IndexService
name|test
init|=
name|createIndex
argument_list|(
literal|"test"
argument_list|)
decl_stmt|;
name|ClusterService
name|clusterService
init|=
name|getInstanceFromNode
argument_list|(
name|ClusterService
operator|.
name|class
argument_list|)
decl_stmt|;
name|IndexMetaData
name|firstMetaData
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
literal|"test"
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|test
operator|.
name|hasShard
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
try|try
block|{
name|indicesService
operator|.
name|deleteIndexStore
argument_list|(
literal|"boom"
argument_list|,
name|firstMetaData
argument_list|,
name|clusterService
operator|.
name|state
argument_list|()
argument_list|)
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalStateException
name|ex
parameter_list|)
block|{
comment|// all good
block|}
name|GatewayMetaState
name|gwMetaState
init|=
name|getInstanceFromNode
argument_list|(
name|GatewayMetaState
operator|.
name|class
argument_list|)
decl_stmt|;
name|MetaData
name|meta
init|=
name|gwMetaState
operator|.
name|loadMetaState
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
name|meta
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|meta
operator|.
name|index
argument_list|(
literal|"test"
argument_list|)
argument_list|)
expr_stmt|;
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
name|prepareDelete
argument_list|(
literal|"test"
argument_list|)
argument_list|)
expr_stmt|;
name|meta
operator|=
name|gwMetaState
operator|.
name|loadMetaState
argument_list|()
expr_stmt|;
name|assertNotNull
argument_list|(
name|meta
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|meta
operator|.
name|index
argument_list|(
literal|"test"
argument_list|)
argument_list|)
expr_stmt|;
name|test
operator|=
name|createIndex
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"field"
argument_list|,
literal|"value"
argument_list|)
operator|.
name|setRefresh
argument_list|(
literal|true
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareFlush
argument_list|(
literal|"test"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|assertHitCount
argument_list|(
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"test"
argument_list|)
operator|.
name|get
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|IndexMetaData
name|secondMetaData
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
literal|"test"
argument_list|)
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
name|prepareClose
argument_list|(
literal|"test"
argument_list|)
argument_list|)
expr_stmt|;
name|ShardPath
name|path
init|=
name|ShardPath
operator|.
name|loadShardPath
argument_list|(
name|logger
argument_list|,
name|getNodeEnvironment
argument_list|()
argument_list|,
operator|new
name|ShardId
argument_list|(
name|test
operator|.
name|index
argument_list|()
argument_list|,
literal|0
argument_list|)
argument_list|,
name|test
operator|.
name|getIndexSettings
argument_list|()
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|path
operator|.
name|exists
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
name|indicesService
operator|.
name|deleteIndexStore
argument_list|(
literal|"boom"
argument_list|,
name|secondMetaData
argument_list|,
name|clusterService
operator|.
name|state
argument_list|()
argument_list|)
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalStateException
name|ex
parameter_list|)
block|{
comment|// all good
block|}
name|assertTrue
argument_list|(
name|path
operator|.
name|exists
argument_list|()
argument_list|)
expr_stmt|;
comment|// now delete the old one and make sure we resolve against the name
try|try
block|{
name|indicesService
operator|.
name|deleteIndexStore
argument_list|(
literal|"boom"
argument_list|,
name|firstMetaData
argument_list|,
name|clusterService
operator|.
name|state
argument_list|()
argument_list|)
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalStateException
name|ex
parameter_list|)
block|{
comment|// all good
block|}
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
name|prepareOpen
argument_list|(
literal|"test"
argument_list|)
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
block|}
DECL|method|testPendingTasks
specifier|public
name|void
name|testPendingTasks
parameter_list|()
throws|throws
name|Exception
block|{
name|IndicesService
name|indicesService
init|=
name|getIndicesService
argument_list|()
decl_stmt|;
name|IndexService
name|test
init|=
name|createIndex
argument_list|(
literal|"test"
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|test
operator|.
name|hasShard
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|ShardPath
name|path
init|=
name|test
operator|.
name|getShardOrNull
argument_list|(
literal|0
argument_list|)
operator|.
name|shardPath
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|test
operator|.
name|getShardOrNull
argument_list|(
literal|0
argument_list|)
operator|.
name|routingEntry
argument_list|()
operator|.
name|started
argument_list|()
argument_list|)
expr_stmt|;
name|ShardPath
name|shardPath
init|=
name|ShardPath
operator|.
name|loadShardPath
argument_list|(
name|logger
argument_list|,
name|getNodeEnvironment
argument_list|()
argument_list|,
operator|new
name|ShardId
argument_list|(
name|test
operator|.
name|index
argument_list|()
argument_list|,
literal|0
argument_list|)
argument_list|,
name|test
operator|.
name|getIndexSettings
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|shardPath
argument_list|,
name|path
argument_list|)
expr_stmt|;
try|try
block|{
name|indicesService
operator|.
name|processPendingDeletes
argument_list|(
name|test
operator|.
name|index
argument_list|()
argument_list|,
name|test
operator|.
name|getIndexSettings
argument_list|()
argument_list|,
operator|new
name|TimeValue
argument_list|(
literal|0
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"can't get lock"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|LockObtainFailedException
name|ex
parameter_list|)
block|{          }
name|assertTrue
argument_list|(
name|path
operator|.
name|exists
argument_list|()
argument_list|)
expr_stmt|;
name|int
name|numPending
init|=
literal|1
decl_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|indicesService
operator|.
name|addPendingDelete
argument_list|(
operator|new
name|ShardId
argument_list|(
name|test
operator|.
name|index
argument_list|()
argument_list|,
literal|0
argument_list|)
argument_list|,
name|test
operator|.
name|getIndexSettings
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|numPending
operator|++
expr_stmt|;
name|indicesService
operator|.
name|addPendingDelete
argument_list|(
operator|new
name|ShardId
argument_list|(
name|test
operator|.
name|index
argument_list|()
argument_list|,
literal|0
argument_list|)
argument_list|,
name|test
operator|.
name|getIndexSettings
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|indicesService
operator|.
name|addPendingDelete
argument_list|(
name|test
operator|.
name|index
argument_list|()
argument_list|,
name|test
operator|.
name|getIndexSettings
argument_list|()
argument_list|)
expr_stmt|;
block|}
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
name|prepareClose
argument_list|(
literal|"test"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|path
operator|.
name|exists
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|indicesService
operator|.
name|numPendingDeletes
argument_list|(
name|test
operator|.
name|index
argument_list|()
argument_list|)
argument_list|,
name|numPending
argument_list|)
expr_stmt|;
comment|// shard lock released... we can now delete
name|indicesService
operator|.
name|processPendingDeletes
argument_list|(
name|test
operator|.
name|index
argument_list|()
argument_list|,
name|test
operator|.
name|getIndexSettings
argument_list|()
argument_list|,
operator|new
name|TimeValue
argument_list|(
literal|0
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|indicesService
operator|.
name|numPendingDeletes
argument_list|(
name|test
operator|.
name|index
argument_list|()
argument_list|)
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|path
operator|.
name|exists
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|indicesService
operator|.
name|addPendingDelete
argument_list|(
operator|new
name|ShardId
argument_list|(
name|test
operator|.
name|index
argument_list|()
argument_list|,
literal|0
argument_list|)
argument_list|,
name|test
operator|.
name|getIndexSettings
argument_list|()
argument_list|)
expr_stmt|;
name|indicesService
operator|.
name|addPendingDelete
argument_list|(
operator|new
name|ShardId
argument_list|(
name|test
operator|.
name|index
argument_list|()
argument_list|,
literal|1
argument_list|)
argument_list|,
name|test
operator|.
name|getIndexSettings
argument_list|()
argument_list|)
expr_stmt|;
name|indicesService
operator|.
name|addPendingDelete
argument_list|(
operator|new
name|ShardId
argument_list|(
literal|"bogus"
argument_list|,
literal|"_na_"
argument_list|,
literal|1
argument_list|)
argument_list|,
name|test
operator|.
name|getIndexSettings
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|indicesService
operator|.
name|numPendingDeletes
argument_list|(
name|test
operator|.
name|index
argument_list|()
argument_list|)
argument_list|,
literal|2
argument_list|)
expr_stmt|;
comment|// shard lock released... we can now delete
name|indicesService
operator|.
name|processPendingDeletes
argument_list|(
name|test
operator|.
name|index
argument_list|()
argument_list|,
name|test
operator|.
name|getIndexSettings
argument_list|()
argument_list|,
operator|new
name|TimeValue
argument_list|(
literal|0
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|indicesService
operator|.
name|numPendingDeletes
argument_list|(
name|test
operator|.
name|index
argument_list|()
argument_list|)
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
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
name|prepareOpen
argument_list|(
literal|"test"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testVerifyIfIndexContentDeleted
specifier|public
name|void
name|testVerifyIfIndexContentDeleted
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|Index
name|index
init|=
operator|new
name|Index
argument_list|(
literal|"test"
argument_list|,
name|UUIDs
operator|.
name|randomBase64UUID
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|IndicesService
name|indicesService
init|=
name|getIndicesService
argument_list|()
decl_stmt|;
specifier|final
name|NodeEnvironment
name|nodeEnv
init|=
name|getNodeEnvironment
argument_list|()
decl_stmt|;
specifier|final
name|MetaStateService
name|metaStateService
init|=
name|getInstanceFromNode
argument_list|(
name|MetaStateService
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|final
name|ClusterService
name|clusterService
init|=
name|getInstanceFromNode
argument_list|(
name|ClusterService
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|final
name|Settings
name|idxSettings
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
name|SETTING_INDEX_UUID
argument_list|,
name|index
operator|.
name|getUUID
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
specifier|final
name|IndexMetaData
name|indexMetaData
init|=
operator|new
name|IndexMetaData
operator|.
name|Builder
argument_list|(
name|index
operator|.
name|getName
argument_list|()
argument_list|)
operator|.
name|settings
argument_list|(
name|idxSettings
argument_list|)
operator|.
name|numberOfShards
argument_list|(
literal|1
argument_list|)
operator|.
name|numberOfReplicas
argument_list|(
literal|0
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|metaStateService
operator|.
name|writeIndex
argument_list|(
literal|"test index being created"
argument_list|,
name|indexMetaData
argument_list|)
expr_stmt|;
specifier|final
name|MetaData
name|metaData
init|=
name|MetaData
operator|.
name|builder
argument_list|(
name|clusterService
operator|.
name|state
argument_list|()
operator|.
name|metaData
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|indexMetaData
argument_list|,
literal|true
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
specifier|final
name|ClusterState
name|csWithIndex
init|=
operator|new
name|ClusterState
operator|.
name|Builder
argument_list|(
name|clusterService
operator|.
name|state
argument_list|()
argument_list|)
operator|.
name|metaData
argument_list|(
name|metaData
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
try|try
block|{
name|indicesService
operator|.
name|verifyIndexIsDeleted
argument_list|(
name|index
argument_list|,
name|csWithIndex
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Should not be able to delete index contents when the index is part of the cluster state."
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalStateException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"Cannot delete index"
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|final
name|ClusterState
name|withoutIndex
init|=
operator|new
name|ClusterState
operator|.
name|Builder
argument_list|(
name|csWithIndex
argument_list|)
operator|.
name|metaData
argument_list|(
name|MetaData
operator|.
name|builder
argument_list|(
name|csWithIndex
operator|.
name|metaData
argument_list|()
argument_list|)
operator|.
name|remove
argument_list|(
name|index
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|indicesService
operator|.
name|verifyIndexIsDeleted
argument_list|(
name|index
argument_list|,
name|withoutIndex
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
literal|"index files should be deleted"
argument_list|,
name|FileSystemUtils
operator|.
name|exists
argument_list|(
name|nodeEnv
operator|.
name|indexPaths
argument_list|(
name|index
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testDanglingIndicesWithAliasConflict
specifier|public
name|void
name|testDanglingIndicesWithAliasConflict
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|String
name|indexName
init|=
literal|"test-idx1"
decl_stmt|;
specifier|final
name|String
name|alias
init|=
literal|"test-alias"
decl_stmt|;
specifier|final
name|IndicesService
name|indicesService
init|=
name|getIndicesService
argument_list|()
decl_stmt|;
specifier|final
name|ClusterService
name|clusterService
init|=
name|getInstanceFromNode
argument_list|(
name|ClusterService
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|final
name|IndexService
name|test
init|=
name|createIndex
argument_list|(
name|indexName
argument_list|)
decl_stmt|;
comment|// create the alias for the index
name|AliasAction
name|action
init|=
operator|new
name|AliasAction
argument_list|(
name|AliasAction
operator|.
name|Type
operator|.
name|ADD
argument_list|,
name|indexName
argument_list|,
name|alias
argument_list|)
decl_stmt|;
name|IndicesAliasesRequest
name|request
init|=
operator|new
name|IndicesAliasesRequest
argument_list|()
operator|.
name|addAliasAction
argument_list|(
name|action
argument_list|)
decl_stmt|;
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|aliases
argument_list|(
name|request
argument_list|)
operator|.
name|actionGet
argument_list|()
expr_stmt|;
specifier|final
name|ClusterState
name|originalState
init|=
name|clusterService
operator|.
name|state
argument_list|()
decl_stmt|;
comment|// try to import a dangling index with the same name as the alias, it should fail
specifier|final
name|LocalAllocateDangledIndices
name|dangling
init|=
name|getInstanceFromNode
argument_list|(
name|LocalAllocateDangledIndices
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|final
name|Settings
name|idxSettings
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
name|SETTING_INDEX_UUID
argument_list|,
name|UUIDs
operator|.
name|randomBase64UUID
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
specifier|final
name|IndexMetaData
name|indexMetaData
init|=
operator|new
name|IndexMetaData
operator|.
name|Builder
argument_list|(
name|alias
argument_list|)
operator|.
name|settings
argument_list|(
name|idxSettings
argument_list|)
operator|.
name|numberOfShards
argument_list|(
literal|1
argument_list|)
operator|.
name|numberOfReplicas
argument_list|(
literal|0
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|DanglingListener
name|listener
init|=
operator|new
name|DanglingListener
argument_list|()
decl_stmt|;
name|dangling
operator|.
name|allocateDangled
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|indexMetaData
argument_list|)
argument_list|,
name|listener
argument_list|)
expr_stmt|;
name|listener
operator|.
name|latch
operator|.
name|await
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|clusterService
operator|.
name|state
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|originalState
argument_list|)
argument_list|)
expr_stmt|;
comment|// remove the alias
name|action
operator|=
operator|new
name|AliasAction
argument_list|(
name|AliasAction
operator|.
name|Type
operator|.
name|REMOVE
argument_list|,
name|indexName
argument_list|,
name|alias
argument_list|)
expr_stmt|;
name|request
operator|=
operator|new
name|IndicesAliasesRequest
argument_list|()
operator|.
name|addAliasAction
argument_list|(
name|action
argument_list|)
expr_stmt|;
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|aliases
argument_list|(
name|request
argument_list|)
operator|.
name|actionGet
argument_list|()
expr_stmt|;
comment|// now try importing a dangling index with the same name as the alias, it should succeed.
name|listener
operator|=
operator|new
name|DanglingListener
argument_list|()
expr_stmt|;
name|dangling
operator|.
name|allocateDangled
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|indexMetaData
argument_list|)
argument_list|,
name|listener
argument_list|)
expr_stmt|;
name|listener
operator|.
name|latch
operator|.
name|await
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|clusterService
operator|.
name|state
argument_list|()
argument_list|,
name|not
argument_list|(
name|originalState
argument_list|)
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|clusterService
operator|.
name|state
argument_list|()
operator|.
name|getMetaData
argument_list|()
operator|.
name|index
argument_list|(
name|alias
argument_list|)
argument_list|)
expr_stmt|;
comment|// cleanup
name|indicesService
operator|.
name|deleteIndex
argument_list|(
name|test
operator|.
name|index
argument_list|()
argument_list|,
literal|"finished with test"
argument_list|)
expr_stmt|;
block|}
comment|/**      * This test checks an edge case where, if a node had an index (lets call it A with UUID 1), then      * deleted it (so a tombstone entry for A will exist in the cluster state), then created      * a new index A with UUID 2, then shutdown, when the node comes back online, it will look at the      * tombstones for deletions, and it should proceed with trying to delete A with UUID 1 and not      * throw any errors that the index still exists in the cluster state.  This is a case of ensuring      * that tombstones that have the same name as current valid indices don't cause confusion by      * trying to delete an index that exists.      * See https://github.com/elastic/elasticsearch/issues/18054      */
DECL|method|testIndexAndTombstoneWithSameNameOnStartup
specifier|public
name|void
name|testIndexAndTombstoneWithSameNameOnStartup
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|String
name|indexName
init|=
literal|"test"
decl_stmt|;
specifier|final
name|Index
name|index
init|=
operator|new
name|Index
argument_list|(
name|indexName
argument_list|,
name|UUIDs
operator|.
name|randomBase64UUID
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|IndicesService
name|indicesService
init|=
name|getIndicesService
argument_list|()
decl_stmt|;
specifier|final
name|Settings
name|idxSettings
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
name|SETTING_INDEX_UUID
argument_list|,
name|index
operator|.
name|getUUID
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
specifier|final
name|IndexMetaData
name|indexMetaData
init|=
operator|new
name|IndexMetaData
operator|.
name|Builder
argument_list|(
name|index
operator|.
name|getName
argument_list|()
argument_list|)
operator|.
name|settings
argument_list|(
name|idxSettings
argument_list|)
operator|.
name|numberOfShards
argument_list|(
literal|1
argument_list|)
operator|.
name|numberOfReplicas
argument_list|(
literal|0
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
specifier|final
name|Index
name|tombstonedIndex
init|=
operator|new
name|Index
argument_list|(
name|indexName
argument_list|,
name|UUIDs
operator|.
name|randomBase64UUID
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|IndexGraveyard
name|graveyard
init|=
name|IndexGraveyard
operator|.
name|builder
argument_list|()
operator|.
name|addTombstone
argument_list|(
name|tombstonedIndex
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
specifier|final
name|MetaData
name|metaData
init|=
name|MetaData
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|indexMetaData
argument_list|,
literal|true
argument_list|)
operator|.
name|indexGraveyard
argument_list|(
name|graveyard
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
specifier|final
name|ClusterState
name|clusterState
init|=
operator|new
name|ClusterState
operator|.
name|Builder
argument_list|(
operator|new
name|ClusterName
argument_list|(
literal|"testCluster"
argument_list|)
argument_list|)
operator|.
name|metaData
argument_list|(
name|metaData
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
comment|// if all goes well, this won't throw an exception, otherwise, it will throw an IllegalStateException
name|indicesService
operator|.
name|verifyIndexIsDeleted
argument_list|(
name|tombstonedIndex
argument_list|,
name|clusterState
argument_list|)
expr_stmt|;
block|}
DECL|class|DanglingListener
specifier|private
specifier|static
class|class
name|DanglingListener
implements|implements
name|LocalAllocateDangledIndices
operator|.
name|Listener
block|{
DECL|field|latch
specifier|final
name|CountDownLatch
name|latch
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
annotation|@
name|Override
DECL|method|onResponse
specifier|public
name|void
name|onResponse
parameter_list|(
name|LocalAllocateDangledIndices
operator|.
name|AllocateDangledResponse
name|response
parameter_list|)
block|{
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|onFailure
specifier|public
name|void
name|onFailure
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

