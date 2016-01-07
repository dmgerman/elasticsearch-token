begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.percolator
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|percolator
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
name|LuceneTestCase
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
name|TestUtil
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
name|percolate
operator|.
name|PercolateResponse
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
name|percolate
operator|.
name|PercolateSourceBuilder
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
name|SearchResponse
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
name|xcontent
operator|.
name|support
operator|.
name|XContentMapValues
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|sort
operator|.
name|SortOrder
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
name|java
operator|.
name|io
operator|.
name|InputStream
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
name|index
operator|.
name|query
operator|.
name|QueryBuilders
operator|.
name|matchQuery
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
name|termQuery
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

begin_class
annotation|@
name|ESIntegTestCase
operator|.
name|ClusterScope
argument_list|(
name|scope
operator|=
name|ESIntegTestCase
operator|.
name|Scope
operator|.
name|TEST
argument_list|,
name|numDataNodes
operator|=
literal|0
argument_list|,
name|numClientNodes
operator|=
literal|0
argument_list|)
annotation|@
name|LuceneTestCase
operator|.
name|SuppressFileSystems
argument_list|(
literal|"ExtrasFS"
argument_list|)
DECL|class|PercolatorBackwardsCompatibilityIT
specifier|public
class|class
name|PercolatorBackwardsCompatibilityIT
extends|extends
name|ESIntegTestCase
block|{
DECL|field|INDEX_NAME
specifier|private
specifier|final
specifier|static
name|String
name|INDEX_NAME
init|=
literal|"percolator_index"
decl_stmt|;
DECL|method|testOldPercolatorIndex
specifier|public
name|void
name|testOldPercolatorIndex
parameter_list|()
throws|throws
name|Exception
block|{
name|setupNode
argument_list|()
expr_stmt|;
comment|// verify cluster state:
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
name|assertThat
argument_list|(
name|state
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
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|state
operator|.
name|metaData
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|get
argument_list|(
name|INDEX_NAME
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|state
operator|.
name|metaData
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|get
argument_list|(
name|INDEX_NAME
argument_list|)
operator|.
name|getCreationVersion
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|Version
operator|.
name|V_2_0_0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|state
operator|.
name|metaData
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|get
argument_list|(
name|INDEX_NAME
argument_list|)
operator|.
name|getUpgradedVersion
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|Version
operator|.
name|CURRENT
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|state
operator|.
name|metaData
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|get
argument_list|(
name|INDEX_NAME
argument_list|)
operator|.
name|getMappings
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|state
operator|.
name|metaData
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|get
argument_list|(
name|INDEX_NAME
argument_list|)
operator|.
name|getMappings
argument_list|()
operator|.
name|get
argument_list|(
literal|".percolator"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
comment|// important: verify that the query field in the .percolator mapping is of type object (from 3.0.0 this is of type percolator)
name|MappingMetaData
name|mappingMetaData
init|=
name|state
operator|.
name|metaData
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|get
argument_list|(
name|INDEX_NAME
argument_list|)
operator|.
name|getMappings
argument_list|()
operator|.
name|get
argument_list|(
literal|".percolator"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|XContentMapValues
operator|.
name|extractValue
argument_list|(
literal|"properties.query.type"
argument_list|,
name|mappingMetaData
operator|.
name|sourceAsMap
argument_list|()
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"object"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|state
operator|.
name|metaData
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|get
argument_list|(
name|INDEX_NAME
argument_list|)
operator|.
name|getMappings
argument_list|()
operator|.
name|get
argument_list|(
literal|"message"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
comment|// verify existing percolator queries:
name|SearchResponse
name|searchResponse
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
name|INDEX_NAME
argument_list|)
operator|.
name|setTypes
argument_list|(
literal|".percolator"
argument_list|)
operator|.
name|addSort
argument_list|(
literal|"_id"
argument_list|,
name|SortOrder
operator|.
name|ASC
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|searchResponse
operator|.
name|getHits
argument_list|()
operator|.
name|getTotalHits
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|3L
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|searchResponse
operator|.
name|getHits
argument_list|()
operator|.
name|getAt
argument_list|(
literal|0
argument_list|)
operator|.
name|id
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|searchResponse
operator|.
name|getHits
argument_list|()
operator|.
name|getAt
argument_list|(
literal|1
argument_list|)
operator|.
name|id
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"2"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|searchResponse
operator|.
name|getHits
argument_list|()
operator|.
name|getAt
argument_list|(
literal|2
argument_list|)
operator|.
name|id
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"3"
argument_list|)
argument_list|)
expr_stmt|;
comment|// verify percolate response
name|PercolateResponse
name|percolateResponse
init|=
name|client
argument_list|()
operator|.
name|preparePercolate
argument_list|()
operator|.
name|setIndices
argument_list|(
name|INDEX_NAME
argument_list|)
operator|.
name|setDocumentType
argument_list|(
literal|"message"
argument_list|)
operator|.
name|setPercolateDoc
argument_list|(
operator|new
name|PercolateSourceBuilder
operator|.
name|DocBuilder
argument_list|()
operator|.
name|setDoc
argument_list|(
literal|"message"
argument_list|,
literal|"the quick brown fox jumps over the lazy dog"
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|percolateResponse
operator|.
name|getCount
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|2L
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|percolateResponse
operator|.
name|getMatches
argument_list|()
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|percolateResponse
operator|.
name|getMatches
argument_list|()
index|[
literal|0
index|]
operator|.
name|getId
argument_list|()
operator|.
name|string
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|percolateResponse
operator|.
name|getMatches
argument_list|()
index|[
literal|1
index|]
operator|.
name|getId
argument_list|()
operator|.
name|string
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"2"
argument_list|)
argument_list|)
expr_stmt|;
comment|// add an extra query and verify the results
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
name|INDEX_NAME
argument_list|,
literal|".percolator"
argument_list|,
literal|"4"
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
name|field
argument_list|(
literal|"query"
argument_list|,
name|matchQuery
argument_list|(
literal|"message"
argument_list|,
literal|"fox jumps"
argument_list|)
argument_list|)
operator|.
name|endObject
argument_list|()
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|refresh
argument_list|()
expr_stmt|;
name|percolateResponse
operator|=
name|client
argument_list|()
operator|.
name|preparePercolate
argument_list|()
operator|.
name|setIndices
argument_list|(
name|INDEX_NAME
argument_list|)
operator|.
name|setDocumentType
argument_list|(
literal|"message"
argument_list|)
operator|.
name|setPercolateDoc
argument_list|(
operator|new
name|PercolateSourceBuilder
operator|.
name|DocBuilder
argument_list|()
operator|.
name|setDoc
argument_list|(
literal|"message"
argument_list|,
literal|"the quick brown fox jumps over the lazy dog"
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|percolateResponse
operator|.
name|getCount
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|3L
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|percolateResponse
operator|.
name|getMatches
argument_list|()
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|percolateResponse
operator|.
name|getMatches
argument_list|()
index|[
literal|0
index|]
operator|.
name|getId
argument_list|()
operator|.
name|string
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|percolateResponse
operator|.
name|getMatches
argument_list|()
index|[
literal|1
index|]
operator|.
name|getId
argument_list|()
operator|.
name|string
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"2"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|percolateResponse
operator|.
name|getMatches
argument_list|()
index|[
literal|2
index|]
operator|.
name|getId
argument_list|()
operator|.
name|string
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"4"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|setupNode
specifier|private
name|void
name|setupNode
parameter_list|()
throws|throws
name|Exception
block|{
name|Path
name|dataDir
init|=
name|createTempDir
argument_list|()
decl_stmt|;
name|Path
name|clusterDir
init|=
name|Files
operator|.
name|createDirectory
argument_list|(
name|dataDir
operator|.
name|resolve
argument_list|(
name|cluster
argument_list|()
operator|.
name|getClusterName
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
try|try
init|(
name|InputStream
name|stream
init|=
name|PercolatorBackwardsCompatibilityIT
operator|.
name|class
operator|.
name|getResourceAsStream
argument_list|(
literal|"/indices/percolator/bwc_index_2.0.0.zip"
argument_list|)
init|)
block|{
name|TestUtil
operator|.
name|unzip
argument_list|(
name|stream
argument_list|,
name|clusterDir
argument_list|)
expr_stmt|;
block|}
name|Settings
operator|.
name|Builder
name|nodeSettings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"path.data"
argument_list|,
name|dataDir
argument_list|)
decl_stmt|;
name|internalCluster
argument_list|()
operator|.
name|startNode
argument_list|(
name|nodeSettings
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|(
name|INDEX_NAME
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

