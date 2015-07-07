begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.store
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|store
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
name|Lists
import|;
end_import

begin_import
import|import
name|com
operator|.
name|carrotsearch
operator|.
name|randomizedtesting
operator|.
name|generators
operator|.
name|RandomPicks
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
name|cluster
operator|.
name|node
operator|.
name|stats
operator|.
name|NodesStatsResponse
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
name|IndexRequestBuilder
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
name|search
operator|.
name|SearchPhaseExecutionException
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
name|ShardIterator
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
name|common
operator|.
name|io
operator|.
name|PathUtils
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
name|translog
operator|.
name|TranslogConfig
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|monitor
operator|.
name|fs
operator|.
name|FsInfo
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
name|elasticsearch
operator|.
name|test
operator|.
name|engine
operator|.
name|MockEngineSupport
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
name|junit
operator|.
name|annotations
operator|.
name|TestLogging
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
name|transport
operator|.
name|MockTransportService
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
name|TransportModule
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
name|ByteBuffer
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|channels
operator|.
name|FileChannel
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
name|*
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
name|List
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
name|TreeSet
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|QueryBuilders
operator|.
name|matchAllQuery
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
name|hamcrest
operator|.
name|Matchers
operator|.
name|notNullValue
import|;
end_import

begin_comment
comment|/**  * Integration test for corrupted translog files  */
end_comment

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
name|SUITE
argument_list|,
name|numDataNodes
operator|=
literal|0
argument_list|)
DECL|class|CorruptedTranslogTests
specifier|public
class|class
name|CorruptedTranslogTests
extends|extends
name|ElasticsearchIntegrationTest
block|{
annotation|@
name|Override
DECL|method|nodeSettings
specifier|protected
name|Settings
name|nodeSettings
parameter_list|(
name|int
name|nodeOrdinal
parameter_list|)
block|{
return|return
name|Settings
operator|.
name|builder
argument_list|()
comment|// we really need local GW here since this also checks for corruption etc.
comment|// and we need to make sure primaries are not just trashed if we don't have replicas
operator|.
name|put
argument_list|(
name|super
operator|.
name|nodeSettings
argument_list|(
name|nodeOrdinal
argument_list|)
argument_list|)
operator|.
name|put
argument_list|(
name|TransportModule
operator|.
name|TRANSPORT_SERVICE_TYPE_KEY
argument_list|,
name|MockTransportService
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
annotation|@
name|Test
annotation|@
name|TestLogging
argument_list|(
literal|"index.translog:TRACE,index.gateway:TRACE"
argument_list|)
DECL|method|testCorruptTranslogFiles
specifier|public
name|void
name|testCorruptTranslogFiles
parameter_list|()
throws|throws
name|Exception
block|{
name|internalCluster
argument_list|()
operator|.
name|startNodesAsync
argument_list|(
literal|1
argument_list|,
name|Settings
operator|.
name|EMPTY
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|assertAcked
argument_list|(
name|prepareCreate
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setSettings
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"index.number_of_shards"
argument_list|,
literal|1
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.number_of_replicas"
argument_list|,
literal|0
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.refresh_interval"
argument_list|,
literal|"-1"
argument_list|)
operator|.
name|put
argument_list|(
name|MockEngineSupport
operator|.
name|FLUSH_ON_CLOSE_RATIO
argument_list|,
literal|0.0d
argument_list|)
comment|// never flush - always recover from translog
operator|.
name|put
argument_list|(
name|IndexShard
operator|.
name|INDEX_FLUSH_ON_CLOSE
argument_list|,
literal|false
argument_list|)
comment|// never flush - always recover from translog
operator|.
name|put
argument_list|(
name|TranslogConfig
operator|.
name|INDEX_TRANSLOG_SYNC_INTERVAL
argument_list|,
literal|"1s"
argument_list|)
comment|// fsync the translog every second
argument_list|)
argument_list|)
expr_stmt|;
name|ensureYellow
argument_list|()
expr_stmt|;
comment|// Index some documents
name|int
name|numDocs
init|=
name|scaledRandomIntBetween
argument_list|(
literal|100
argument_list|,
literal|1000
argument_list|)
decl_stmt|;
name|IndexRequestBuilder
index|[]
name|builders
init|=
operator|new
name|IndexRequestBuilder
index|[
name|numDocs
index|]
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
name|builders
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|builders
index|[
name|i
index|]
operator|=
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"foo"
argument_list|,
literal|"bar"
argument_list|)
expr_stmt|;
block|}
name|disableTranslogFlush
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
name|indexRandom
argument_list|(
literal|false
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|builders
argument_list|)
argument_list|)
expr_stmt|;
comment|// this one
comment|// Corrupt the translog file(s)
name|corruptRandomTranslogFiles
argument_list|()
expr_stmt|;
comment|// Restart the single node
name|internalCluster
argument_list|()
operator|.
name|fullRestart
argument_list|()
expr_stmt|;
comment|// node needs time to start recovery and discover the translog corruption
name|Thread
operator|.
name|sleep
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
name|enableTranslogFlush
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
try|try
block|{
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|matchAllQuery
argument_list|()
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|fail
argument_list|(
literal|"all shards should be failed due to a corrupted translog"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SearchPhaseExecutionException
name|e
parameter_list|)
block|{
comment|// Good, all shards should be failed because there is only a
comment|// single shard and its translog is corrupt
block|}
block|}
DECL|method|corruptRandomTranslogFiles
specifier|private
name|void
name|corruptRandomTranslogFiles
parameter_list|()
throws|throws
name|IOException
block|{
name|ClusterState
name|state
init|=
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareState
argument_list|()
operator|.
name|get
argument_list|()
operator|.
name|getState
argument_list|()
decl_stmt|;
name|GroupShardsIterator
name|shardIterators
init|=
name|state
operator|.
name|getRoutingNodes
argument_list|()
operator|.
name|getRoutingTable
argument_list|()
operator|.
name|activePrimaryShardsGrouped
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"test"
block|}
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|ShardIterator
argument_list|>
name|iterators
init|=
name|Lists
operator|.
name|newArrayList
argument_list|(
name|shardIterators
argument_list|)
decl_stmt|;
name|ShardIterator
name|shardIterator
init|=
name|RandomPicks
operator|.
name|randomFrom
argument_list|(
name|getRandom
argument_list|()
argument_list|,
name|iterators
argument_list|)
decl_stmt|;
name|ShardRouting
name|shardRouting
init|=
name|shardIterator
operator|.
name|nextOrNull
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
name|shardRouting
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|shardRouting
operator|.
name|primary
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|shardRouting
operator|.
name|assignedToNode
argument_list|()
argument_list|)
expr_stmt|;
name|String
name|nodeId
init|=
name|shardRouting
operator|.
name|currentNodeId
argument_list|()
decl_stmt|;
name|NodesStatsResponse
name|nodeStatses
init|=
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareNodesStats
argument_list|(
name|nodeId
argument_list|)
operator|.
name|setFs
argument_list|(
literal|true
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|Path
argument_list|>
name|files
init|=
operator|new
name|TreeSet
argument_list|<>
argument_list|()
decl_stmt|;
comment|// treeset makes sure iteration order is deterministic
for|for
control|(
name|FsInfo
operator|.
name|Path
name|fsPath
range|:
name|nodeStatses
operator|.
name|getNodes
argument_list|()
index|[
literal|0
index|]
operator|.
name|getFs
argument_list|()
control|)
block|{
name|String
name|path
init|=
name|fsPath
operator|.
name|getPath
argument_list|()
decl_stmt|;
specifier|final
name|String
name|relativeDataLocationPath
init|=
literal|"indices/test/"
operator|+
name|Integer
operator|.
name|toString
argument_list|(
name|shardRouting
operator|.
name|getId
argument_list|()
argument_list|)
operator|+
literal|"/translog"
decl_stmt|;
name|Path
name|file
init|=
name|PathUtils
operator|.
name|get
argument_list|(
name|path
argument_list|)
operator|.
name|resolve
argument_list|(
name|relativeDataLocationPath
argument_list|)
decl_stmt|;
if|if
condition|(
name|Files
operator|.
name|exists
argument_list|(
name|file
argument_list|)
condition|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"--> path: {}"
argument_list|,
name|file
argument_list|)
expr_stmt|;
try|try
init|(
name|DirectoryStream
argument_list|<
name|Path
argument_list|>
name|stream
init|=
name|Files
operator|.
name|newDirectoryStream
argument_list|(
name|file
argument_list|)
init|)
block|{
for|for
control|(
name|Path
name|item
range|:
name|stream
control|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"--> File: {}"
argument_list|,
name|item
argument_list|)
expr_stmt|;
if|if
condition|(
name|Files
operator|.
name|isRegularFile
argument_list|(
name|item
argument_list|)
operator|&&
name|item
operator|.
name|getFileName
argument_list|()
operator|.
name|toString
argument_list|()
operator|.
name|startsWith
argument_list|(
literal|"translog-"
argument_list|)
condition|)
block|{
name|files
operator|.
name|add
argument_list|(
name|item
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
name|Path
name|fileToCorrupt
init|=
literal|null
decl_stmt|;
if|if
condition|(
operator|!
name|files
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|int
name|corruptions
init|=
name|randomIntBetween
argument_list|(
literal|5
argument_list|,
literal|20
argument_list|)
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
name|corruptions
condition|;
name|i
operator|++
control|)
block|{
name|fileToCorrupt
operator|=
name|RandomPicks
operator|.
name|randomFrom
argument_list|(
name|getRandom
argument_list|()
argument_list|,
name|files
argument_list|)
expr_stmt|;
try|try
init|(
name|FileChannel
name|raf
init|=
name|FileChannel
operator|.
name|open
argument_list|(
name|fileToCorrupt
argument_list|,
name|StandardOpenOption
operator|.
name|READ
argument_list|,
name|StandardOpenOption
operator|.
name|WRITE
argument_list|)
init|)
block|{
comment|// read
name|raf
operator|.
name|position
argument_list|(
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
operator|(
name|int
operator|)
name|Math
operator|.
name|min
argument_list|(
name|Integer
operator|.
name|MAX_VALUE
argument_list|,
name|raf
operator|.
name|size
argument_list|()
operator|-
literal|1
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|long
name|filePointer
init|=
name|raf
operator|.
name|position
argument_list|()
decl_stmt|;
name|ByteBuffer
name|bb
init|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
operator|new
name|byte
index|[
literal|1
index|]
argument_list|)
decl_stmt|;
name|raf
operator|.
name|read
argument_list|(
name|bb
argument_list|)
expr_stmt|;
name|bb
operator|.
name|flip
argument_list|()
expr_stmt|;
comment|// corrupt
name|byte
name|oldValue
init|=
name|bb
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|byte
name|newValue
init|=
call|(
name|byte
call|)
argument_list|(
name|oldValue
operator|+
literal|1
argument_list|)
decl_stmt|;
name|bb
operator|.
name|put
argument_list|(
literal|0
argument_list|,
name|newValue
argument_list|)
expr_stmt|;
comment|// rewrite
name|raf
operator|.
name|position
argument_list|(
name|filePointer
argument_list|)
expr_stmt|;
name|raf
operator|.
name|write
argument_list|(
name|bb
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> corrupting file {} --  flipping at position {} from {} to {} file: {}"
argument_list|,
name|fileToCorrupt
argument_list|,
name|filePointer
argument_list|,
name|Integer
operator|.
name|toHexString
argument_list|(
name|oldValue
argument_list|)
argument_list|,
name|Integer
operator|.
name|toHexString
argument_list|(
name|newValue
argument_list|)
argument_list|,
name|fileToCorrupt
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|assertThat
argument_list|(
literal|"no file corrupted"
argument_list|,
name|fileToCorrupt
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

