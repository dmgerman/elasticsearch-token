begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.cluster
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
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
name|template
operator|.
name|get
operator|.
name|GetIndexTemplatesResponse
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
name|IndicesOptions
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|client
operator|.
name|Client
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|client
operator|.
name|Requests
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
name|MappingMetaData
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
name|Strings
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
name|common
operator|.
name|xcontent
operator|.
name|XContentBuilder
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
name|xcontent
operator|.
name|XContentFactory
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
name|IndexNotFoundException
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
name|hamcrest
operator|.
name|CollectionAssertions
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
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
name|assertIndexTemplateExists
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
name|greaterThanOrEqualTo
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
name|is
import|;
end_import

begin_comment
comment|/**  * Checking simple filtering capabilities of the cluster state  *  */
end_comment

begin_class
DECL|class|SimpleClusterStateIT
specifier|public
class|class
name|SimpleClusterStateIT
extends|extends
name|ESIntegTestCase
block|{
annotation|@
name|Before
DECL|method|indexData
specifier|public
name|void
name|indexData
parameter_list|()
throws|throws
name|Exception
block|{
name|index
argument_list|(
literal|"foo"
argument_list|,
literal|"bar"
argument_list|,
literal|"1"
argument_list|,
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"foo"
argument_list|,
literal|"foo"
argument_list|)
operator|.
name|endObject
argument_list|()
argument_list|)
expr_stmt|;
name|index
argument_list|(
literal|"fuu"
argument_list|,
literal|"buu"
argument_list|,
literal|"1"
argument_list|,
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"fuu"
argument_list|,
literal|"fuu"
argument_list|)
operator|.
name|endObject
argument_list|()
argument_list|)
expr_stmt|;
name|index
argument_list|(
literal|"baz"
argument_list|,
literal|"baz"
argument_list|,
literal|"1"
argument_list|,
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"baz"
argument_list|,
literal|"baz"
argument_list|)
operator|.
name|endObject
argument_list|()
argument_list|)
expr_stmt|;
name|refresh
argument_list|()
expr_stmt|;
block|}
DECL|method|testRoutingTable
specifier|public
name|void
name|testRoutingTable
parameter_list|()
throws|throws
name|Exception
block|{
name|ClusterStateResponse
name|clusterStateResponseUnfiltered
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
name|clear
argument_list|()
operator|.
name|setRoutingTable
argument_list|(
literal|true
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|clusterStateResponseUnfiltered
operator|.
name|getState
argument_list|()
operator|.
name|routingTable
argument_list|()
operator|.
name|hasIndex
argument_list|(
literal|"foo"
argument_list|)
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|clusterStateResponseUnfiltered
operator|.
name|getState
argument_list|()
operator|.
name|routingTable
argument_list|()
operator|.
name|hasIndex
argument_list|(
literal|"fuu"
argument_list|)
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|clusterStateResponseUnfiltered
operator|.
name|getState
argument_list|()
operator|.
name|routingTable
argument_list|()
operator|.
name|hasIndex
argument_list|(
literal|"baz"
argument_list|)
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|clusterStateResponseUnfiltered
operator|.
name|getState
argument_list|()
operator|.
name|routingTable
argument_list|()
operator|.
name|hasIndex
argument_list|(
literal|"non-existent"
argument_list|)
argument_list|,
name|is
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
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
name|clear
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
name|routingTable
argument_list|()
operator|.
name|hasIndex
argument_list|(
literal|"foo"
argument_list|)
argument_list|,
name|is
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|clusterStateResponse
operator|.
name|getState
argument_list|()
operator|.
name|routingTable
argument_list|()
operator|.
name|hasIndex
argument_list|(
literal|"fuu"
argument_list|)
argument_list|,
name|is
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|clusterStateResponse
operator|.
name|getState
argument_list|()
operator|.
name|routingTable
argument_list|()
operator|.
name|hasIndex
argument_list|(
literal|"baz"
argument_list|)
argument_list|,
name|is
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|clusterStateResponse
operator|.
name|getState
argument_list|()
operator|.
name|routingTable
argument_list|()
operator|.
name|hasIndex
argument_list|(
literal|"non-existent"
argument_list|)
argument_list|,
name|is
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testNodes
specifier|public
name|void
name|testNodes
parameter_list|()
throws|throws
name|Exception
block|{
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
name|clear
argument_list|()
operator|.
name|setNodes
argument_list|(
literal|true
argument_list|)
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
name|nodes
argument_list|()
operator|.
name|nodes
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|is
argument_list|(
name|cluster
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|ClusterStateResponse
name|clusterStateResponseFiltered
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
name|clear
argument_list|()
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|clusterStateResponseFiltered
operator|.
name|getState
argument_list|()
operator|.
name|nodes
argument_list|()
operator|.
name|nodes
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|is
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testMetadata
specifier|public
name|void
name|testMetadata
parameter_list|()
throws|throws
name|Exception
block|{
name|ClusterStateResponse
name|clusterStateResponseUnfiltered
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
name|clear
argument_list|()
operator|.
name|setMetaData
argument_list|(
literal|true
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|clusterStateResponseUnfiltered
operator|.
name|getState
argument_list|()
operator|.
name|metaData
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|is
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
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
name|clear
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
name|metaData
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|is
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testIndexTemplates
specifier|public
name|void
name|testIndexTemplates
parameter_list|()
throws|throws
name|Exception
block|{
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|preparePutTemplate
argument_list|(
literal|"foo_template"
argument_list|)
operator|.
name|setTemplate
argument_list|(
literal|"te*"
argument_list|)
operator|.
name|setOrder
argument_list|(
literal|0
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"type1"
argument_list|,
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"type1"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"properties"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"field1"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"text"
argument_list|)
operator|.
name|field
argument_list|(
literal|"store"
argument_list|,
literal|true
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"field2"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"keyword"
argument_list|)
operator|.
name|field
argument_list|(
literal|"store"
argument_list|,
literal|true
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
operator|.
name|endObject
argument_list|()
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
name|preparePutTemplate
argument_list|(
literal|"fuu_template"
argument_list|)
operator|.
name|setTemplate
argument_list|(
literal|"test*"
argument_list|)
operator|.
name|setOrder
argument_list|(
literal|1
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"type1"
argument_list|,
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"type1"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"properties"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"field2"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"text"
argument_list|)
operator|.
name|field
argument_list|(
literal|"store"
argument_list|,
literal|"no"
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
operator|.
name|endObject
argument_list|()
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|ClusterStateResponse
name|clusterStateResponseUnfiltered
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
name|clusterStateResponseUnfiltered
operator|.
name|getState
argument_list|()
operator|.
name|metaData
argument_list|()
operator|.
name|templates
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|is
argument_list|(
name|greaterThanOrEqualTo
argument_list|(
literal|2
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|GetIndexTemplatesResponse
name|getIndexTemplatesResponse
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
name|prepareGetTemplates
argument_list|(
literal|"foo_template"
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertIndexTemplateExists
argument_list|(
name|getIndexTemplatesResponse
argument_list|,
literal|"foo_template"
argument_list|)
expr_stmt|;
block|}
DECL|method|testThatFilteringByIndexWorksForMetadataAndRoutingTable
specifier|public
name|void
name|testThatFilteringByIndexWorksForMetadataAndRoutingTable
parameter_list|()
throws|throws
name|Exception
block|{
name|ClusterStateResponse
name|clusterStateResponseFiltered
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
name|clear
argument_list|()
operator|.
name|setMetaData
argument_list|(
literal|true
argument_list|)
operator|.
name|setRoutingTable
argument_list|(
literal|true
argument_list|)
operator|.
name|setIndices
argument_list|(
literal|"foo"
argument_list|,
literal|"fuu"
argument_list|,
literal|"non-existent"
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
comment|// metadata
name|assertThat
argument_list|(
name|clusterStateResponseFiltered
operator|.
name|getState
argument_list|()
operator|.
name|metaData
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|is
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|clusterStateResponseFiltered
operator|.
name|getState
argument_list|()
operator|.
name|metaData
argument_list|()
operator|.
name|indices
argument_list|()
argument_list|,
name|CollectionAssertions
operator|.
name|hasKey
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|clusterStateResponseFiltered
operator|.
name|getState
argument_list|()
operator|.
name|metaData
argument_list|()
operator|.
name|indices
argument_list|()
argument_list|,
name|CollectionAssertions
operator|.
name|hasKey
argument_list|(
literal|"fuu"
argument_list|)
argument_list|)
expr_stmt|;
comment|// routing table
name|assertThat
argument_list|(
name|clusterStateResponseFiltered
operator|.
name|getState
argument_list|()
operator|.
name|routingTable
argument_list|()
operator|.
name|hasIndex
argument_list|(
literal|"foo"
argument_list|)
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|clusterStateResponseFiltered
operator|.
name|getState
argument_list|()
operator|.
name|routingTable
argument_list|()
operator|.
name|hasIndex
argument_list|(
literal|"fuu"
argument_list|)
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|clusterStateResponseFiltered
operator|.
name|getState
argument_list|()
operator|.
name|routingTable
argument_list|()
operator|.
name|hasIndex
argument_list|(
literal|"baz"
argument_list|)
argument_list|,
name|is
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testLargeClusterStatePublishing
specifier|public
name|void
name|testLargeClusterStatePublishing
parameter_list|()
throws|throws
name|Exception
block|{
name|int
name|estimatedBytesSize
init|=
name|scaledRandomIntBetween
argument_list|(
name|ByteSizeValue
operator|.
name|parseBytesSizeValue
argument_list|(
literal|"10k"
argument_list|,
literal|"estimatedBytesSize"
argument_list|)
operator|.
name|bytesAsInt
argument_list|()
argument_list|,
name|ByteSizeValue
operator|.
name|parseBytesSizeValue
argument_list|(
literal|"256k"
argument_list|,
literal|"estimatedBytesSize"
argument_list|)
operator|.
name|bytesAsInt
argument_list|()
argument_list|)
decl_stmt|;
name|XContentBuilder
name|mapping
init|=
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"type"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"properties"
argument_list|)
decl_stmt|;
name|int
name|counter
init|=
literal|0
decl_stmt|;
name|int
name|numberOfFields
init|=
literal|0
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
name|mapping
operator|.
name|startObject
argument_list|(
name|Strings
operator|.
name|randomBase64UUID
argument_list|()
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"text"
argument_list|)
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|counter
operator|+=
literal|10
expr_stmt|;
comment|// each field is about 10 bytes, assuming compression in place
name|numberOfFields
operator|++
expr_stmt|;
if|if
condition|(
name|counter
operator|>
name|estimatedBytesSize
condition|)
block|{
break|break;
block|}
block|}
name|logger
operator|.
name|info
argument_list|(
literal|"number of fields [{}], estimated bytes [{}]"
argument_list|,
name|numberOfFields
argument_list|,
name|estimatedBytesSize
argument_list|)
expr_stmt|;
name|mapping
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|int
name|numberOfShards
init|=
name|scaledRandomIntBetween
argument_list|(
literal|1
argument_list|,
name|cluster
argument_list|()
operator|.
name|numDataNodes
argument_list|()
argument_list|)
decl_stmt|;
comment|// if the create index is ack'ed, then all nodes have successfully processed the cluster state
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
name|prepareCreate
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setSettings
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_NUMBER_OF_SHARDS
argument_list|,
name|numberOfShards
argument_list|,
name|IndexMetaData
operator|.
name|SETTING_NUMBER_OF_REPLICAS
argument_list|,
literal|0
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"type"
argument_list|,
name|mapping
argument_list|)
operator|.
name|setTimeout
argument_list|(
literal|"60s"
argument_list|)
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
comment|// wait for green state, so its both green, and there are no more pending events
name|MappingMetaData
name|masterMappingMetaData
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
literal|"test"
argument_list|)
operator|.
name|setTypes
argument_list|(
literal|"type"
argument_list|)
operator|.
name|get
argument_list|()
operator|.
name|getMappings
argument_list|()
operator|.
name|get
argument_list|(
literal|"test"
argument_list|)
operator|.
name|get
argument_list|(
literal|"type"
argument_list|)
decl_stmt|;
for|for
control|(
name|Client
name|client
range|:
name|clients
argument_list|()
control|)
block|{
name|MappingMetaData
name|mappingMetadata
init|=
name|client
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareGetMappings
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setTypes
argument_list|(
literal|"type"
argument_list|)
operator|.
name|setLocal
argument_list|(
literal|true
argument_list|)
operator|.
name|get
argument_list|()
operator|.
name|getMappings
argument_list|()
operator|.
name|get
argument_list|(
literal|"test"
argument_list|)
operator|.
name|get
argument_list|(
literal|"type"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|mappingMetadata
operator|.
name|source
argument_list|()
operator|.
name|string
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|masterMappingMetaData
operator|.
name|source
argument_list|()
operator|.
name|string
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|mappingMetadata
argument_list|,
name|equalTo
argument_list|(
name|masterMappingMetaData
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testIndicesOptions
specifier|public
name|void
name|testIndicesOptions
parameter_list|()
throws|throws
name|Exception
block|{
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
name|clear
argument_list|()
operator|.
name|setMetaData
argument_list|(
literal|true
argument_list|)
operator|.
name|setIndices
argument_list|(
literal|"f*"
argument_list|)
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
name|metaData
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|is
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
comment|// close one index
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|close
argument_list|(
name|Requests
operator|.
name|closeIndexRequest
argument_list|(
literal|"fuu"
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|clusterStateResponse
operator|=
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
name|clear
argument_list|()
operator|.
name|setMetaData
argument_list|(
literal|true
argument_list|)
operator|.
name|setIndices
argument_list|(
literal|"f*"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|clusterStateResponse
operator|.
name|getState
argument_list|()
operator|.
name|metaData
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|is
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|clusterStateResponse
operator|.
name|getState
argument_list|()
operator|.
name|metaData
argument_list|()
operator|.
name|index
argument_list|(
literal|"foo"
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
comment|// expand_wildcards_closed should toggle return only closed index fuu
name|IndicesOptions
name|expandCloseOptions
init|=
name|IndicesOptions
operator|.
name|fromOptions
argument_list|(
literal|false
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|clusterStateResponse
operator|=
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
name|clear
argument_list|()
operator|.
name|setMetaData
argument_list|(
literal|true
argument_list|)
operator|.
name|setIndices
argument_list|(
literal|"f*"
argument_list|)
operator|.
name|setIndicesOptions
argument_list|(
name|expandCloseOptions
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|clusterStateResponse
operator|.
name|getState
argument_list|()
operator|.
name|metaData
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|is
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|clusterStateResponse
operator|.
name|getState
argument_list|()
operator|.
name|metaData
argument_list|()
operator|.
name|index
argument_list|(
literal|"fuu"
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
comment|// ignore_unavailable set to true should not raise exception on fzzbzz
name|IndicesOptions
name|ignoreUnavailabe
init|=
name|IndicesOptions
operator|.
name|fromOptions
argument_list|(
literal|true
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|clusterStateResponse
operator|=
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
name|clear
argument_list|()
operator|.
name|setMetaData
argument_list|(
literal|true
argument_list|)
operator|.
name|setIndices
argument_list|(
literal|"fzzbzz"
argument_list|)
operator|.
name|setIndicesOptions
argument_list|(
name|ignoreUnavailabe
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|clusterStateResponse
operator|.
name|getState
argument_list|()
operator|.
name|metaData
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
comment|// empty wildcard expansion result should work when allowNoIndices is
comment|// turned on
name|IndicesOptions
name|allowNoIndices
init|=
name|IndicesOptions
operator|.
name|fromOptions
argument_list|(
literal|false
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|clusterStateResponse
operator|=
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
name|clear
argument_list|()
operator|.
name|setMetaData
argument_list|(
literal|true
argument_list|)
operator|.
name|setIndices
argument_list|(
literal|"a*"
argument_list|)
operator|.
name|setIndicesOptions
argument_list|(
name|allowNoIndices
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|clusterStateResponse
operator|.
name|getState
argument_list|()
operator|.
name|metaData
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testIndicesOptionsOnAllowNoIndicesFalse
specifier|public
name|void
name|testIndicesOptionsOnAllowNoIndicesFalse
parameter_list|()
throws|throws
name|Exception
block|{
comment|// empty wildcard expansion throws exception when allowNoIndices is turned off
name|IndicesOptions
name|allowNoIndices
init|=
name|IndicesOptions
operator|.
name|fromOptions
argument_list|(
literal|false
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|)
decl_stmt|;
try|try
block|{
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
name|clear
argument_list|()
operator|.
name|setMetaData
argument_list|(
literal|true
argument_list|)
operator|.
name|setIndices
argument_list|(
literal|"a*"
argument_list|)
operator|.
name|setIndicesOptions
argument_list|(
name|allowNoIndices
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|fail
argument_list|(
literal|"Expected IndexNotFoundException"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IndexNotFoundException
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
name|is
argument_list|(
literal|"no such index"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testIndicesIgnoreUnavailableFalse
specifier|public
name|void
name|testIndicesIgnoreUnavailableFalse
parameter_list|()
throws|throws
name|Exception
block|{
comment|// ignore_unavailable set to false throws exception when allowNoIndices is turned off
name|IndicesOptions
name|allowNoIndices
init|=
name|IndicesOptions
operator|.
name|fromOptions
argument_list|(
literal|false
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|)
decl_stmt|;
try|try
block|{
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
name|clear
argument_list|()
operator|.
name|setMetaData
argument_list|(
literal|true
argument_list|)
operator|.
name|setIndices
argument_list|(
literal|"fzzbzz"
argument_list|)
operator|.
name|setIndicesOptions
argument_list|(
name|allowNoIndices
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|fail
argument_list|(
literal|"Expected IndexNotFoundException"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IndexNotFoundException
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
name|is
argument_list|(
literal|"no such index"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

