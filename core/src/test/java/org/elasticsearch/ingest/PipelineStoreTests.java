begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.ingest
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ResourceNotFoundException
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
name|ingest
operator|.
name|DeletePipelineRequest
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
name|ingest
operator|.
name|PutPipelineRequest
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
name|ingest
operator|.
name|WritePipelineResponse
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
name|bytes
operator|.
name|BytesArray
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
name|ingest
operator|.
name|core
operator|.
name|Pipeline
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|processor
operator|.
name|SetProcessor
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
name|ESTestCase
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
name|HashMap
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
name|is
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

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|nullValue
import|;
end_import

begin_class
DECL|class|PipelineStoreTests
specifier|public
class|class
name|PipelineStoreTests
extends|extends
name|ESTestCase
block|{
DECL|field|store
specifier|private
name|PipelineStore
name|store
decl_stmt|;
annotation|@
name|Before
DECL|method|init
specifier|public
name|void
name|init
parameter_list|()
throws|throws
name|Exception
block|{
name|store
operator|=
operator|new
name|PipelineStore
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
expr_stmt|;
name|ProcessorsRegistry
name|registry
init|=
operator|new
name|ProcessorsRegistry
argument_list|()
decl_stmt|;
name|registry
operator|.
name|registerProcessor
argument_list|(
literal|"set"
argument_list|,
parameter_list|(
name|templateService
parameter_list|)
lambda|->
operator|new
name|SetProcessor
operator|.
name|Factory
argument_list|(
name|TestTemplateService
operator|.
name|instance
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|store
operator|.
name|buildProcessorFactoryRegistry
argument_list|(
name|registry
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
DECL|method|testUpdatePipelines
specifier|public
name|void
name|testUpdatePipelines
parameter_list|()
block|{
name|ClusterState
name|clusterState
init|=
name|ClusterState
operator|.
name|builder
argument_list|(
operator|new
name|ClusterName
argument_list|(
literal|"_name"
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|store
operator|.
name|innerUpdatePipelines
argument_list|(
name|clusterState
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|store
operator|.
name|pipelines
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
name|PipelineConfiguration
name|pipeline
init|=
operator|new
name|PipelineConfiguration
argument_list|(
literal|"_id"
argument_list|,
operator|new
name|BytesArray
argument_list|(
literal|"{\"processors\": [{\"set\" : {\"field\": \"_field\", \"value\": \"_value\"}}]}"
argument_list|)
argument_list|)
decl_stmt|;
name|IngestMetadata
name|ingestMetadata
init|=
operator|new
name|IngestMetadata
argument_list|(
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"_id"
argument_list|,
name|pipeline
argument_list|)
argument_list|)
decl_stmt|;
name|clusterState
operator|=
name|ClusterState
operator|.
name|builder
argument_list|(
name|clusterState
argument_list|)
operator|.
name|metaData
argument_list|(
name|MetaData
operator|.
name|builder
argument_list|()
operator|.
name|putCustom
argument_list|(
name|IngestMetadata
operator|.
name|TYPE
argument_list|,
name|ingestMetadata
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|store
operator|.
name|innerUpdatePipelines
argument_list|(
name|clusterState
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|store
operator|.
name|pipelines
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
name|store
operator|.
name|pipelines
operator|.
name|get
argument_list|(
literal|"_id"
argument_list|)
operator|.
name|getId
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"_id"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|store
operator|.
name|pipelines
operator|.
name|get
argument_list|(
literal|"_id"
argument_list|)
operator|.
name|getDescription
argument_list|()
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|store
operator|.
name|pipelines
operator|.
name|get
argument_list|(
literal|"_id"
argument_list|)
operator|.
name|getProcessors
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
name|store
operator|.
name|pipelines
operator|.
name|get
argument_list|(
literal|"_id"
argument_list|)
operator|.
name|getProcessors
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"set"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testPut
specifier|public
name|void
name|testPut
parameter_list|()
block|{
name|String
name|id
init|=
literal|"_id"
decl_stmt|;
name|Pipeline
name|pipeline
init|=
name|store
operator|.
name|get
argument_list|(
name|id
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|pipeline
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|ClusterState
name|clusterState
init|=
name|ClusterState
operator|.
name|builder
argument_list|(
operator|new
name|ClusterName
argument_list|(
literal|"_name"
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
comment|// add a new pipeline:
name|PutPipelineRequest
name|putRequest
init|=
operator|new
name|PutPipelineRequest
argument_list|(
name|id
argument_list|,
operator|new
name|BytesArray
argument_list|(
literal|"{\"processors\": []}"
argument_list|)
argument_list|)
decl_stmt|;
name|clusterState
operator|=
name|store
operator|.
name|innerPut
argument_list|(
name|putRequest
argument_list|,
name|clusterState
argument_list|)
expr_stmt|;
name|store
operator|.
name|innerUpdatePipelines
argument_list|(
name|clusterState
argument_list|)
expr_stmt|;
name|pipeline
operator|=
name|store
operator|.
name|get
argument_list|(
name|id
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|pipeline
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|pipeline
operator|.
name|getId
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|id
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|pipeline
operator|.
name|getDescription
argument_list|()
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|pipeline
operator|.
name|getProcessors
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
comment|// overwrite existing pipeline:
name|putRequest
operator|=
operator|new
name|PutPipelineRequest
argument_list|(
name|id
argument_list|,
operator|new
name|BytesArray
argument_list|(
literal|"{\"processors\": [], \"description\": \"_description\"}"
argument_list|)
argument_list|)
expr_stmt|;
name|clusterState
operator|=
name|store
operator|.
name|innerPut
argument_list|(
name|putRequest
argument_list|,
name|clusterState
argument_list|)
expr_stmt|;
name|store
operator|.
name|innerUpdatePipelines
argument_list|(
name|clusterState
argument_list|)
expr_stmt|;
name|pipeline
operator|=
name|store
operator|.
name|get
argument_list|(
name|id
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|pipeline
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|pipeline
operator|.
name|getId
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|id
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|pipeline
operator|.
name|getDescription
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"_description"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|pipeline
operator|.
name|getProcessors
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testPutWithErrorResponse
specifier|public
name|void
name|testPutWithErrorResponse
parameter_list|()
block|{      }
DECL|method|testConstructPipelineResponseSuccess
specifier|public
name|void
name|testConstructPipelineResponseSuccess
parameter_list|()
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|processorConfig
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|processorConfig
operator|.
name|put
argument_list|(
literal|"field"
argument_list|,
literal|"foo"
argument_list|)
expr_stmt|;
name|processorConfig
operator|.
name|put
argument_list|(
literal|"value"
argument_list|,
literal|"bar"
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|pipelineConfig
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|pipelineConfig
operator|.
name|put
argument_list|(
literal|"description"
argument_list|,
literal|"_description"
argument_list|)
expr_stmt|;
name|pipelineConfig
operator|.
name|put
argument_list|(
literal|"processors"
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"set"
argument_list|,
name|processorConfig
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|WritePipelineResponse
name|response
init|=
name|store
operator|.
name|validatePipelineResponse
argument_list|(
literal|"test_id"
argument_list|,
name|pipelineConfig
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|response
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testConstructPipelineResponseMissingProcessorsFieldException
specifier|public
name|void
name|testConstructPipelineResponseMissingProcessorsFieldException
parameter_list|()
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|pipelineConfig
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|pipelineConfig
operator|.
name|put
argument_list|(
literal|"description"
argument_list|,
literal|"_description"
argument_list|)
expr_stmt|;
name|WritePipelineResponse
name|response
init|=
name|store
operator|.
name|validatePipelineResponse
argument_list|(
literal|"test_id"
argument_list|,
name|pipelineConfig
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getError
argument_list|()
operator|.
name|getProcessorType
argument_list|()
argument_list|,
name|is
argument_list|(
name|nullValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getError
argument_list|()
operator|.
name|getProcessorTag
argument_list|()
argument_list|,
name|is
argument_list|(
name|nullValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getError
argument_list|()
operator|.
name|getProcessorPropertyName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"processors"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getError
argument_list|()
operator|.
name|getReason
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"[processors] required property is missing"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testConstructPipelineResponseConfigurationException
specifier|public
name|void
name|testConstructPipelineResponseConfigurationException
parameter_list|()
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|processorConfig
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|processorConfig
operator|.
name|put
argument_list|(
literal|"field"
argument_list|,
literal|"foo"
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|pipelineConfig
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|pipelineConfig
operator|.
name|put
argument_list|(
literal|"description"
argument_list|,
literal|"_description"
argument_list|)
expr_stmt|;
name|pipelineConfig
operator|.
name|put
argument_list|(
literal|"processors"
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"set"
argument_list|,
name|processorConfig
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|WritePipelineResponse
name|response
init|=
name|store
operator|.
name|validatePipelineResponse
argument_list|(
literal|"test_id"
argument_list|,
name|pipelineConfig
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getError
argument_list|()
operator|.
name|getProcessorTag
argument_list|()
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getError
argument_list|()
operator|.
name|getProcessorType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"set"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getError
argument_list|()
operator|.
name|getProcessorPropertyName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"value"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getError
argument_list|()
operator|.
name|getReason
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"[value] required property is missing"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testDelete
specifier|public
name|void
name|testDelete
parameter_list|()
block|{
name|PipelineConfiguration
name|config
init|=
operator|new
name|PipelineConfiguration
argument_list|(
literal|"_id"
argument_list|,
operator|new
name|BytesArray
argument_list|(
literal|"{\"processors\": [{\"set\" : {\"field\": \"_field\", \"value\": \"_value\"}}]}"
argument_list|)
argument_list|)
decl_stmt|;
name|IngestMetadata
name|ingestMetadata
init|=
operator|new
name|IngestMetadata
argument_list|(
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"_id"
argument_list|,
name|config
argument_list|)
argument_list|)
decl_stmt|;
name|ClusterState
name|clusterState
init|=
name|ClusterState
operator|.
name|builder
argument_list|(
operator|new
name|ClusterName
argument_list|(
literal|"_name"
argument_list|)
argument_list|)
operator|.
name|metaData
argument_list|(
name|MetaData
operator|.
name|builder
argument_list|()
operator|.
name|putCustom
argument_list|(
name|IngestMetadata
operator|.
name|TYPE
argument_list|,
name|ingestMetadata
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|store
operator|.
name|innerUpdatePipelines
argument_list|(
name|clusterState
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|store
operator|.
name|get
argument_list|(
literal|"_id"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
comment|// Delete pipeline:
name|DeletePipelineRequest
name|deleteRequest
init|=
operator|new
name|DeletePipelineRequest
argument_list|(
literal|"_id"
argument_list|)
decl_stmt|;
name|clusterState
operator|=
name|store
operator|.
name|innerDelete
argument_list|(
name|deleteRequest
argument_list|,
name|clusterState
argument_list|)
expr_stmt|;
name|store
operator|.
name|innerUpdatePipelines
argument_list|(
name|clusterState
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|store
operator|.
name|get
argument_list|(
literal|"_id"
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
comment|// Delete existing pipeline:
try|try
block|{
name|store
operator|.
name|innerDelete
argument_list|(
name|deleteRequest
argument_list|,
name|clusterState
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
name|ResourceNotFoundException
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
name|equalTo
argument_list|(
literal|"pipeline [_id] is missing"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testGetPipelines
specifier|public
name|void
name|testGetPipelines
parameter_list|()
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|PipelineConfiguration
argument_list|>
name|configs
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|configs
operator|.
name|put
argument_list|(
literal|"_id1"
argument_list|,
operator|new
name|PipelineConfiguration
argument_list|(
literal|"_id1"
argument_list|,
operator|new
name|BytesArray
argument_list|(
literal|"{\"processors\": []}"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|configs
operator|.
name|put
argument_list|(
literal|"_id2"
argument_list|,
operator|new
name|PipelineConfiguration
argument_list|(
literal|"_id2"
argument_list|,
operator|new
name|BytesArray
argument_list|(
literal|"{\"processors\": []}"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|store
operator|.
name|innerGetPipelines
argument_list|(
literal|null
argument_list|,
literal|"_id1"
argument_list|)
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
name|IngestMetadata
name|ingestMetadata
init|=
operator|new
name|IngestMetadata
argument_list|(
name|configs
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|PipelineConfiguration
argument_list|>
name|pipelines
init|=
name|store
operator|.
name|innerGetPipelines
argument_list|(
name|ingestMetadata
argument_list|,
literal|"_id1"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|pipelines
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
name|pipelines
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getId
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"_id1"
argument_list|)
argument_list|)
expr_stmt|;
name|pipelines
operator|=
name|store
operator|.
name|innerGetPipelines
argument_list|(
name|ingestMetadata
argument_list|,
literal|"_id1"
argument_list|,
literal|"_id2"
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|pipelines
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
name|pipelines
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getId
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"_id1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|pipelines
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|getId
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"_id2"
argument_list|)
argument_list|)
expr_stmt|;
name|pipelines
operator|=
name|store
operator|.
name|innerGetPipelines
argument_list|(
name|ingestMetadata
argument_list|,
literal|"_id*"
argument_list|)
expr_stmt|;
name|pipelines
operator|.
name|sort
argument_list|(
parameter_list|(
name|o1
parameter_list|,
name|o2
parameter_list|)
lambda|->
name|o1
operator|.
name|getId
argument_list|()
operator|.
name|compareTo
argument_list|(
name|o2
operator|.
name|getId
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|pipelines
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
name|pipelines
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getId
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"_id1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|pipelines
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|getId
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"_id2"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testCrud
specifier|public
name|void
name|testCrud
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|id
init|=
literal|"_id"
decl_stmt|;
name|Pipeline
name|pipeline
init|=
name|store
operator|.
name|get
argument_list|(
name|id
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|pipeline
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|ClusterState
name|clusterState
init|=
name|ClusterState
operator|.
name|builder
argument_list|(
operator|new
name|ClusterName
argument_list|(
literal|"_name"
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
comment|// Start empty
name|PutPipelineRequest
name|putRequest
init|=
operator|new
name|PutPipelineRequest
argument_list|(
name|id
argument_list|,
operator|new
name|BytesArray
argument_list|(
literal|"{\"processors\": [{\"set\" : {\"field\": \"_field\", \"value\": \"_value\"}}]}"
argument_list|)
argument_list|)
decl_stmt|;
name|clusterState
operator|=
name|store
operator|.
name|innerPut
argument_list|(
name|putRequest
argument_list|,
name|clusterState
argument_list|)
expr_stmt|;
name|store
operator|.
name|innerUpdatePipelines
argument_list|(
name|clusterState
argument_list|)
expr_stmt|;
name|pipeline
operator|=
name|store
operator|.
name|get
argument_list|(
name|id
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|pipeline
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|pipeline
operator|.
name|getId
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|id
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|pipeline
operator|.
name|getDescription
argument_list|()
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|pipeline
operator|.
name|getProcessors
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
name|pipeline
operator|.
name|getProcessors
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"set"
argument_list|)
argument_list|)
expr_stmt|;
name|DeletePipelineRequest
name|deleteRequest
init|=
operator|new
name|DeletePipelineRequest
argument_list|(
name|id
argument_list|)
decl_stmt|;
name|clusterState
operator|=
name|store
operator|.
name|innerDelete
argument_list|(
name|deleteRequest
argument_list|,
name|clusterState
argument_list|)
expr_stmt|;
name|store
operator|.
name|innerUpdatePipelines
argument_list|(
name|clusterState
argument_list|)
expr_stmt|;
name|pipeline
operator|=
name|store
operator|.
name|get
argument_list|(
name|id
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|pipeline
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

