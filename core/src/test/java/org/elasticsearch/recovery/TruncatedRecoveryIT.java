begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.recovery
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|recovery
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
name|util
operator|.
name|English
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|LuceneTestCase
operator|.
name|SuppressCodecs
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
name|NodeStats
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
name|ByteSizeUnit
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
name|ByteSizeValue
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|discovery
operator|.
name|Discovery
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
name|query
operator|.
name|QueryBuilders
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
name|RecoveryFileChunkRequest
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
name|RecoverySettings
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
name|test
operator|.
name|ESIntegTestCase
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
name|*
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
name|atomic
operator|.
name|AtomicBoolean
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
name|Settings
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
name|greaterThanOrEqualTo
import|;
end_import

begin_class
annotation|@
name|ESIntegTestCase
operator|.
name|ClusterScope
argument_list|(
name|numDataNodes
operator|=
literal|2
argument_list|,
name|numClientNodes
operator|=
literal|0
argument_list|,
name|scope
operator|=
name|ESIntegTestCase
operator|.
name|Scope
operator|.
name|TEST
argument_list|)
annotation|@
name|SuppressCodecs
argument_list|(
literal|"*"
argument_list|)
comment|// test relies on exact file extensions
DECL|class|TruncatedRecoveryIT
specifier|public
class|class
name|TruncatedRecoveryIT
extends|extends
name|ESIntegTestCase
block|{
DECL|method|nodeSettings
specifier|protected
name|Settings
name|nodeSettings
parameter_list|(
name|int
name|nodeOrdinal
parameter_list|)
block|{
name|Settings
operator|.
name|Builder
name|builder
init|=
name|Settings
operator|.
name|builder
argument_list|()
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
name|extendArray
argument_list|(
literal|"plugin.types"
argument_list|,
name|MockTransportService
operator|.
name|TestPlugin
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|RecoverySettings
operator|.
name|INDICES_RECOVERY_FILE_CHUNK_SIZE
argument_list|,
operator|new
name|ByteSizeValue
argument_list|(
name|randomIntBetween
argument_list|(
literal|50
argument_list|,
literal|300
argument_list|)
argument_list|,
name|ByteSizeUnit
operator|.
name|BYTES
argument_list|)
argument_list|)
decl_stmt|;
return|return
name|builder
operator|.
name|build
argument_list|()
return|;
block|}
comment|/**      * This test tries to truncate some of larger files in the index to trigger leftovers on the recovery      * target. This happens during recovery when the last chunk of the file is transferred to the replica      * we just throw an exception to make sure the recovery fails and we leave some half baked files on the target.      * Later we allow full recovery to ensure we can still recover and don't run into corruptions.      */
annotation|@
name|Test
DECL|method|testCancelRecoveryAndResume
specifier|public
name|void
name|testCancelRecoveryAndResume
parameter_list|()
throws|throws
name|Exception
block|{
name|NodesStatsResponse
name|nodeStats
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
argument_list|()
operator|.
name|get
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|NodeStats
argument_list|>
name|dataNodeStats
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|NodeStats
name|stat
range|:
name|nodeStats
operator|.
name|getNodes
argument_list|()
control|)
block|{
if|if
condition|(
name|stat
operator|.
name|getNode
argument_list|()
operator|.
name|isDataNode
argument_list|()
condition|)
block|{
name|dataNodeStats
operator|.
name|add
argument_list|(
name|stat
argument_list|)
expr_stmt|;
block|}
block|}
name|assertThat
argument_list|(
name|dataNodeStats
operator|.
name|size
argument_list|()
argument_list|,
name|greaterThanOrEqualTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|Collections
operator|.
name|shuffle
argument_list|(
name|dataNodeStats
argument_list|,
name|getRandom
argument_list|()
argument_list|)
expr_stmt|;
comment|// we use 2 nodes a lucky and unlucky one
comment|// the lucky one holds the primary
comment|// the unlucky one gets the replica and the truncated leftovers
name|NodeStats
name|primariesNode
init|=
name|dataNodeStats
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|NodeStats
name|unluckyNode
init|=
name|dataNodeStats
operator|.
name|get
argument_list|(
literal|1
argument_list|)
decl_stmt|;
comment|// create the index and prevent allocation on any other nodes than the lucky one
comment|// we have no replicas so far and make sure that we allocate the primary on the lucky node
name|assertAcked
argument_list|(
name|prepareCreate
argument_list|(
literal|"test"
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"type1"
argument_list|,
literal|"field1"
argument_list|,
literal|"type=string"
argument_list|,
literal|"the_id"
argument_list|,
literal|"type=string"
argument_list|)
operator|.
name|setSettings
argument_list|(
name|settingsBuilder
argument_list|()
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
name|IndexMetaData
operator|.
name|SETTING_NUMBER_OF_SHARDS
argument_list|,
name|numberOfShards
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.routing.allocation.include._name"
argument_list|,
name|primariesNode
operator|.
name|getNode
argument_list|()
operator|.
name|name
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// only allocate on the lucky node
comment|// index some docs and check if they are coming back
name|int
name|numDocs
init|=
name|randomIntBetween
argument_list|(
literal|100
argument_list|,
literal|200
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|IndexRequestBuilder
argument_list|>
name|builder
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
name|numDocs
condition|;
name|i
operator|++
control|)
block|{
name|String
name|id
init|=
name|Integer
operator|.
name|toString
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|builder
operator|.
name|add
argument_list|(
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
name|id
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"field1"
argument_list|,
name|English
operator|.
name|intToEnglish
argument_list|(
name|i
argument_list|)
argument_list|,
literal|"the_id"
argument_list|,
name|id
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|indexRandom
argument_list|(
literal|true
argument_list|,
name|builder
argument_list|)
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
name|numDocs
condition|;
name|i
operator|++
control|)
block|{
name|String
name|id
init|=
name|Integer
operator|.
name|toString
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|assertHitCount
argument_list|(
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|()
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
name|termQuery
argument_list|(
literal|"the_id"
argument_list|,
name|id
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
name|ensureGreen
argument_list|()
expr_stmt|;
comment|// ensure we have flushed segments and make them a big one via optimize
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
argument_list|()
operator|.
name|setForce
argument_list|(
literal|true
argument_list|)
operator|.
name|setWaitIfOngoing
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
name|prepareOptimize
argument_list|()
operator|.
name|setMaxNumSegments
argument_list|(
literal|1
argument_list|)
operator|.
name|setFlush
argument_list|(
literal|true
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
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
specifier|final
name|AtomicBoolean
name|truncate
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|true
argument_list|)
decl_stmt|;
for|for
control|(
name|NodeStats
name|dataNode
range|:
name|dataNodeStats
control|)
block|{
name|MockTransportService
name|mockTransportService
init|=
operator|(
operator|(
name|MockTransportService
operator|)
name|internalCluster
argument_list|()
operator|.
name|getInstance
argument_list|(
name|TransportService
operator|.
name|class
argument_list|,
name|dataNode
operator|.
name|getNode
argument_list|()
operator|.
name|name
argument_list|()
argument_list|)
operator|)
decl_stmt|;
name|mockTransportService
operator|.
name|addDelegate
argument_list|(
name|internalCluster
argument_list|()
operator|.
name|getInstance
argument_list|(
name|Discovery
operator|.
name|class
argument_list|,
name|unluckyNode
operator|.
name|getNode
argument_list|()
operator|.
name|name
argument_list|()
argument_list|)
operator|.
name|localNode
argument_list|()
argument_list|,
operator|new
name|MockTransportService
operator|.
name|DelegateTransport
argument_list|(
name|mockTransportService
operator|.
name|original
argument_list|()
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|void
name|sendRequest
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|,
name|long
name|requestId
parameter_list|,
name|String
name|action
parameter_list|,
name|TransportRequest
name|request
parameter_list|,
name|TransportRequestOptions
name|options
parameter_list|)
throws|throws
name|IOException
throws|,
name|TransportException
block|{
if|if
condition|(
name|action
operator|.
name|equals
argument_list|(
name|RecoveryTarget
operator|.
name|Actions
operator|.
name|FILE_CHUNK
argument_list|)
condition|)
block|{
name|RecoveryFileChunkRequest
name|req
init|=
operator|(
name|RecoveryFileChunkRequest
operator|)
name|request
decl_stmt|;
name|logger
operator|.
name|debug
argument_list|(
literal|"file chunk ["
operator|+
name|req
operator|.
name|toString
argument_list|()
operator|+
literal|"] lastChunk: "
operator|+
name|req
operator|.
name|lastChunk
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
operator|(
name|req
operator|.
name|name
argument_list|()
operator|.
name|endsWith
argument_list|(
literal|"cfs"
argument_list|)
operator|||
name|req
operator|.
name|name
argument_list|()
operator|.
name|endsWith
argument_list|(
literal|"fdt"
argument_list|)
operator|)
operator|&&
name|req
operator|.
name|lastChunk
argument_list|()
operator|&&
name|truncate
operator|.
name|get
argument_list|()
condition|)
block|{
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Caused some truncated files for fun and profit"
argument_list|)
throw|;
block|}
block|}
name|super
operator|.
name|sendRequest
argument_list|(
name|node
argument_list|,
name|requestId
argument_list|,
name|action
argument_list|,
name|request
argument_list|,
name|options
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
name|logger
operator|.
name|info
argument_list|(
literal|"--> bumping replicas to 1"
argument_list|)
expr_stmt|;
comment|//
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
name|IndexMetaData
operator|.
name|SETTING_NUMBER_OF_REPLICAS
argument_list|,
literal|1
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.routing.allocation.include._name"
argument_list|,
comment|// now allow allocation on all nodes
name|primariesNode
operator|.
name|getNode
argument_list|()
operator|.
name|name
argument_list|()
operator|+
literal|","
operator|+
name|unluckyNode
operator|.
name|getNode
argument_list|()
operator|.
name|name
argument_list|()
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|latch
operator|.
name|await
argument_list|()
expr_stmt|;
comment|// at this point we got some truncated left overs on the replica on the unlucky node
comment|// now we are allowing the recovery to allocate again and finish to see if we wipe the truncated files
name|truncate
operator|.
name|compareAndSet
argument_list|(
literal|true
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|(
literal|"test"
argument_list|)
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
name|numDocs
condition|;
name|i
operator|++
control|)
block|{
name|String
name|id
init|=
name|Integer
operator|.
name|toString
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|assertHitCount
argument_list|(
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|()
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
name|termQuery
argument_list|(
literal|"the_id"
argument_list|,
name|id
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

