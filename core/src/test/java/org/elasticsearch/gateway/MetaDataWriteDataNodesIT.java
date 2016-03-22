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
name|action
operator|.
name|admin
operator|.
name|cluster
operator|.
name|state
operator|.
name|ClusterStateResponse
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
name|mapping
operator|.
name|get
operator|.
name|GetMappingsResponse
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
name|common
operator|.
name|collect
operator|.
name|ImmutableOpenMap
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
name|ESIntegTestCase
operator|.
name|ClusterScope
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
operator|.
name|Scope
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
name|InternalTestCluster
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
name|InternalTestCluster
operator|.
name|RestartCallback
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
name|Files
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
name|LinkedHashMap
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
name|xcontent
operator|.
name|XContentFactory
operator|.
name|jsonBuilder
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
name|equalTo
import|;
end_import

begin_class
annotation|@
name|ClusterScope
argument_list|(
name|scope
operator|=
name|Scope
operator|.
name|TEST
argument_list|,
name|numDataNodes
operator|=
literal|0
argument_list|)
DECL|class|MetaDataWriteDataNodesIT
specifier|public
class|class
name|MetaDataWriteDataNodesIT
extends|extends
name|ESIntegTestCase
block|{
DECL|method|testMetaWrittenAlsoOnDataNode
specifier|public
name|void
name|testMetaWrittenAlsoOnDataNode
parameter_list|()
throws|throws
name|Exception
block|{
comment|// this test checks that index state is written on data only nodes if they have a shard allocated
name|String
name|masterNode
init|=
name|internalCluster
argument_list|()
operator|.
name|startMasterOnlyNode
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
decl_stmt|;
name|String
name|dataNode
init|=
name|internalCluster
argument_list|()
operator|.
name|startDataOnlyNode
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
decl_stmt|;
name|assertAcked
argument_list|(
name|prepareCreate
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setSettings
argument_list|(
literal|"index.number_of_replicas"
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|index
argument_list|(
literal|"test"
argument_list|,
literal|"doc"
argument_list|,
literal|"1"
argument_list|,
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"text"
argument_list|,
literal|"some text"
argument_list|)
operator|.
name|endObject
argument_list|()
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
name|assertIndexInMetaState
argument_list|(
name|dataNode
argument_list|,
literal|"test"
argument_list|)
expr_stmt|;
name|assertIndexInMetaState
argument_list|(
name|masterNode
argument_list|,
literal|"test"
argument_list|)
expr_stmt|;
block|}
DECL|method|testMetaIsRemovedIfAllShardsFromIndexRemoved
specifier|public
name|void
name|testMetaIsRemovedIfAllShardsFromIndexRemoved
parameter_list|()
throws|throws
name|Exception
block|{
comment|// this test checks that the index state is removed from a data only node once all shards have been allocated away from it
name|String
name|masterNode
init|=
name|internalCluster
argument_list|()
operator|.
name|startMasterOnlyNode
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
decl_stmt|;
name|InternalTestCluster
operator|.
name|Async
argument_list|<
name|String
argument_list|>
name|nodeName1
init|=
name|internalCluster
argument_list|()
operator|.
name|startDataOnlyNodeAsync
argument_list|()
decl_stmt|;
name|InternalTestCluster
operator|.
name|Async
argument_list|<
name|String
argument_list|>
name|nodeName2
init|=
name|internalCluster
argument_list|()
operator|.
name|startDataOnlyNodeAsync
argument_list|()
decl_stmt|;
name|String
name|node1
init|=
name|nodeName1
operator|.
name|get
argument_list|()
decl_stmt|;
name|String
name|node2
init|=
name|nodeName2
operator|.
name|get
argument_list|()
decl_stmt|;
name|String
name|index
init|=
literal|"index"
decl_stmt|;
name|assertAcked
argument_list|(
name|prepareCreate
argument_list|(
name|index
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
literal|"index.number_of_replicas"
argument_list|,
literal|0
argument_list|)
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|INDEX_ROUTING_INCLUDE_GROUP_SETTING
operator|.
name|getKey
argument_list|()
operator|+
literal|"_name"
argument_list|,
name|node1
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|index
argument_list|(
name|index
argument_list|,
literal|"doc"
argument_list|,
literal|"1"
argument_list|,
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"text"
argument_list|,
literal|"some text"
argument_list|)
operator|.
name|endObject
argument_list|()
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
name|assertIndexInMetaState
argument_list|(
name|node1
argument_list|,
name|index
argument_list|)
expr_stmt|;
name|Index
name|resolveIndex
init|=
name|resolveIndex
argument_list|(
name|index
argument_list|)
decl_stmt|;
name|assertIndexDirectoryDeleted
argument_list|(
name|node2
argument_list|,
name|resolveIndex
argument_list|)
expr_stmt|;
name|assertIndexInMetaState
argument_list|(
name|masterNode
argument_list|,
name|index
argument_list|)
expr_stmt|;
name|logger
operator|.
name|debug
argument_list|(
literal|"relocating index..."
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
name|prepareUpdateSettings
argument_list|(
name|index
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
name|IndexMetaData
operator|.
name|INDEX_ROUTING_INCLUDE_GROUP_SETTING
operator|.
name|getKey
argument_list|()
operator|+
literal|"_name"
argument_list|,
name|node2
argument_list|)
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
name|cluster
argument_list|()
operator|.
name|prepareHealth
argument_list|()
operator|.
name|setWaitForRelocatingShards
argument_list|(
literal|0
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
name|assertIndexDirectoryDeleted
argument_list|(
name|node1
argument_list|,
name|resolveIndex
argument_list|)
expr_stmt|;
name|assertIndexInMetaState
argument_list|(
name|node2
argument_list|,
name|index
argument_list|)
expr_stmt|;
name|assertIndexInMetaState
argument_list|(
name|masterNode
argument_list|,
name|index
argument_list|)
expr_stmt|;
block|}
DECL|method|testMetaWrittenWhenIndexIsClosedAndMetaUpdated
specifier|public
name|void
name|testMetaWrittenWhenIndexIsClosedAndMetaUpdated
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|masterNode
init|=
name|internalCluster
argument_list|()
operator|.
name|startMasterOnlyNode
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
decl_stmt|;
specifier|final
name|String
name|dataNode
init|=
name|internalCluster
argument_list|()
operator|.
name|startDataOnlyNode
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
decl_stmt|;
specifier|final
name|String
name|index
init|=
literal|"index"
decl_stmt|;
name|assertAcked
argument_list|(
name|prepareCreate
argument_list|(
name|index
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
literal|"index.number_of_replicas"
argument_list|,
literal|0
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> wait for green index"
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> wait for meta state written for index"
argument_list|)
expr_stmt|;
name|assertIndexInMetaState
argument_list|(
name|dataNode
argument_list|,
name|index
argument_list|)
expr_stmt|;
name|assertIndexInMetaState
argument_list|(
name|masterNode
argument_list|,
name|index
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> close index"
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
name|prepareClose
argument_list|(
name|index
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
comment|// close the index
name|ClusterStateResponse
name|clusterStateResponse
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
decl_stmt|;
name|assertThat
argument_list|(
name|clusterStateResponse
operator|.
name|getState
argument_list|()
operator|.
name|getMetaData
argument_list|()
operator|.
name|index
argument_list|(
name|index
argument_list|)
operator|.
name|getState
argument_list|()
operator|.
name|name
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|IndexMetaData
operator|.
name|State
operator|.
name|CLOSE
operator|.
name|name
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// update the mapping. this should cause the new meta data to be written although index is closed
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|preparePutMapping
argument_list|(
name|index
argument_list|)
operator|.
name|setType
argument_list|(
literal|"doc"
argument_list|)
operator|.
name|setSource
argument_list|(
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"properties"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"integer_field"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"integer"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|GetMappingsResponse
name|getMappingsResponse
init|=
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareGetMappings
argument_list|(
name|index
argument_list|)
operator|.
name|addTypes
argument_list|(
literal|"doc"
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
operator|(
call|(
name|LinkedHashMap
call|)
argument_list|(
name|getMappingsResponse
operator|.
name|getMappings
argument_list|()
operator|.
name|get
argument_list|(
name|index
argument_list|)
operator|.
name|get
argument_list|(
literal|"doc"
argument_list|)
operator|.
name|getSourceAsMap
argument_list|()
operator|.
name|get
argument_list|(
literal|"properties"
argument_list|)
argument_list|)
operator|)
operator|.
name|get
argument_list|(
literal|"integer_field"
argument_list|)
argument_list|)
expr_stmt|;
comment|// make sure it was also written on red node although index is closed
name|ImmutableOpenMap
argument_list|<
name|String
argument_list|,
name|IndexMetaData
argument_list|>
name|indicesMetaData
init|=
name|getIndicesMetaDataOnNode
argument_list|(
name|dataNode
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
operator|(
call|(
name|LinkedHashMap
call|)
argument_list|(
name|indicesMetaData
operator|.
name|get
argument_list|(
name|index
argument_list|)
operator|.
name|getMappings
argument_list|()
operator|.
name|get
argument_list|(
literal|"doc"
argument_list|)
operator|.
name|getSourceAsMap
argument_list|()
operator|.
name|get
argument_list|(
literal|"properties"
argument_list|)
argument_list|)
operator|)
operator|.
name|get
argument_list|(
literal|"integer_field"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|indicesMetaData
operator|.
name|get
argument_list|(
name|index
argument_list|)
operator|.
name|getState
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|IndexMetaData
operator|.
name|State
operator|.
name|CLOSE
argument_list|)
argument_list|)
expr_stmt|;
comment|/* Try the same and see if this also works if node was just restarted.          * Each node holds an array of indices it knows of and checks if it should          * write new meta data by looking up in this array. We need it because if an          * index is closed it will not appear in the shard routing and we therefore          * need to keep track of what we wrote before. However, when the node is          * restarted this array is empty and we have to fill it before we decide          * what we write. This is why we explicitly test for it.          */
name|internalCluster
argument_list|()
operator|.
name|restartNode
argument_list|(
name|dataNode
argument_list|,
operator|new
name|RestartCallback
argument_list|()
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
name|preparePutMapping
argument_list|(
name|index
argument_list|)
operator|.
name|setType
argument_list|(
literal|"doc"
argument_list|)
operator|.
name|setSource
argument_list|(
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"properties"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"float_field"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"float"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|getMappingsResponse
operator|=
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareGetMappings
argument_list|(
name|index
argument_list|)
operator|.
name|addTypes
argument_list|(
literal|"doc"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|assertNotNull
argument_list|(
operator|(
call|(
name|LinkedHashMap
call|)
argument_list|(
name|getMappingsResponse
operator|.
name|getMappings
argument_list|()
operator|.
name|get
argument_list|(
name|index
argument_list|)
operator|.
name|get
argument_list|(
literal|"doc"
argument_list|)
operator|.
name|getSourceAsMap
argument_list|()
operator|.
name|get
argument_list|(
literal|"properties"
argument_list|)
argument_list|)
operator|)
operator|.
name|get
argument_list|(
literal|"float_field"
argument_list|)
argument_list|)
expr_stmt|;
comment|// make sure it was also written on red node although index is closed
name|indicesMetaData
operator|=
name|getIndicesMetaDataOnNode
argument_list|(
name|dataNode
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
operator|(
call|(
name|LinkedHashMap
call|)
argument_list|(
name|indicesMetaData
operator|.
name|get
argument_list|(
name|index
argument_list|)
operator|.
name|getMappings
argument_list|()
operator|.
name|get
argument_list|(
literal|"doc"
argument_list|)
operator|.
name|getSourceAsMap
argument_list|()
operator|.
name|get
argument_list|(
literal|"properties"
argument_list|)
argument_list|)
operator|)
operator|.
name|get
argument_list|(
literal|"float_field"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|indicesMetaData
operator|.
name|get
argument_list|(
name|index
argument_list|)
operator|.
name|getState
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|IndexMetaData
operator|.
name|State
operator|.
name|CLOSE
argument_list|)
argument_list|)
expr_stmt|;
comment|// finally check that meta data is also written of index opened again
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
name|index
argument_list|)
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
comment|// make sure index is fully initialized and nothing is changed anymore
name|ensureGreen
argument_list|()
expr_stmt|;
name|indicesMetaData
operator|=
name|getIndicesMetaDataOnNode
argument_list|(
name|dataNode
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|indicesMetaData
operator|.
name|get
argument_list|(
name|index
argument_list|)
operator|.
name|getState
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|IndexMetaData
operator|.
name|State
operator|.
name|OPEN
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|assertIndexDirectoryDeleted
specifier|protected
name|void
name|assertIndexDirectoryDeleted
parameter_list|(
specifier|final
name|String
name|nodeName
parameter_list|,
specifier|final
name|Index
name|index
parameter_list|)
throws|throws
name|Exception
block|{
name|assertBusy
argument_list|(
parameter_list|()
lambda|->
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"checking if index directory exists..."
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
literal|"Expecting index directory of "
operator|+
name|index
operator|+
literal|" to be deleted from node "
operator|+
name|nodeName
argument_list|,
name|indexDirectoryExists
argument_list|(
name|nodeName
argument_list|,
name|index
argument_list|)
argument_list|)
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
block|}
DECL|method|assertIndexInMetaState
specifier|protected
name|void
name|assertIndexInMetaState
parameter_list|(
specifier|final
name|String
name|nodeName
parameter_list|,
specifier|final
name|String
name|indexName
parameter_list|)
throws|throws
name|Exception
block|{
name|assertBusy
argument_list|(
parameter_list|()
lambda|->
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"checking if meta state exists..."
argument_list|)
expr_stmt|;
try|try
block|{
name|assertTrue
argument_list|(
literal|"Expecting meta state of index "
operator|+
name|indexName
operator|+
literal|" to be on node "
operator|+
name|nodeName
argument_list|,
name|getIndicesMetaDataOnNode
argument_list|(
name|nodeName
argument_list|)
operator|.
name|containsKey
argument_list|(
name|indexName
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
name|logger
operator|.
name|info
argument_list|(
literal|"failed to load meta state"
argument_list|,
name|t
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"could not load meta state"
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
DECL|method|indexDirectoryExists
specifier|private
name|boolean
name|indexDirectoryExists
parameter_list|(
name|String
name|nodeName
parameter_list|,
name|Index
name|index
parameter_list|)
block|{
name|NodeEnvironment
name|nodeEnv
init|=
operator|(
operator|(
name|InternalTestCluster
operator|)
name|cluster
argument_list|()
operator|)
operator|.
name|getInstance
argument_list|(
name|NodeEnvironment
operator|.
name|class
argument_list|,
name|nodeName
argument_list|)
decl_stmt|;
for|for
control|(
name|Path
name|path
range|:
name|nodeEnv
operator|.
name|indexPaths
argument_list|(
name|index
argument_list|)
control|)
block|{
if|if
condition|(
name|Files
operator|.
name|exists
argument_list|(
name|path
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
DECL|method|getIndicesMetaDataOnNode
specifier|private
name|ImmutableOpenMap
argument_list|<
name|String
argument_list|,
name|IndexMetaData
argument_list|>
name|getIndicesMetaDataOnNode
parameter_list|(
name|String
name|nodeName
parameter_list|)
throws|throws
name|Exception
block|{
name|GatewayMetaState
name|nodeMetaState
init|=
operator|(
operator|(
name|InternalTestCluster
operator|)
name|cluster
argument_list|()
operator|)
operator|.
name|getInstance
argument_list|(
name|GatewayMetaState
operator|.
name|class
argument_list|,
name|nodeName
argument_list|)
decl_stmt|;
name|MetaData
name|nodeMetaData
init|=
name|nodeMetaState
operator|.
name|loadMetaState
argument_list|()
decl_stmt|;
return|return
name|nodeMetaData
operator|.
name|getIndices
argument_list|()
return|;
block|}
block|}
end_class

end_unit

