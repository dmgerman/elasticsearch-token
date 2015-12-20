begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.plugin.ingest.transport
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|plugin
operator|.
name|ingest
operator|.
name|transport
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
name|ActionRequest
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
name|ActionResponse
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
name|bulk
operator|.
name|BulkItemResponse
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
name|bulk
operator|.
name|BulkRequest
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
name|bulk
operator|.
name|BulkResponse
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
name|delete
operator|.
name|DeleteRequest
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
name|support
operator|.
name|ActionFilterChain
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
name|update
operator|.
name|UpdateRequest
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
name|IngestDocument
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
name|Processor
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|plugin
operator|.
name|ingest
operator|.
name|IngestPlugin
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|plugin
operator|.
name|ingest
operator|.
name|PipelineExecutionService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|plugin
operator|.
name|ingest
operator|.
name|PipelineStore
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|plugin
operator|.
name|ingest
operator|.
name|IngestBootstrapper
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
name|elasticsearch
operator|.
name|threadpool
operator|.
name|ThreadPool
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
name|org
operator|.
name|mockito
operator|.
name|invocation
operator|.
name|InvocationOnMock
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|stubbing
operator|.
name|Answer
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
name|Set
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|plugin
operator|.
name|ingest
operator|.
name|transport
operator|.
name|IngestActionFilter
operator|.
name|*
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
name|*
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Matchers
operator|.
name|any
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Matchers
operator|.
name|eq
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|*
import|;
end_import

begin_class
DECL|class|IngestActionFilterTests
specifier|public
class|class
name|IngestActionFilterTests
extends|extends
name|ESTestCase
block|{
DECL|field|filter
specifier|private
name|IngestActionFilter
name|filter
decl_stmt|;
DECL|field|executionService
specifier|private
name|PipelineExecutionService
name|executionService
decl_stmt|;
annotation|@
name|Before
DECL|method|setup
specifier|public
name|void
name|setup
parameter_list|()
block|{
name|executionService
operator|=
name|mock
argument_list|(
name|PipelineExecutionService
operator|.
name|class
argument_list|)
expr_stmt|;
name|IngestBootstrapper
name|bootstrapper
init|=
name|mock
argument_list|(
name|IngestBootstrapper
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|bootstrapper
operator|.
name|getPipelineExecutionService
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|executionService
argument_list|)
expr_stmt|;
name|filter
operator|=
operator|new
name|IngestActionFilter
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
name|bootstrapper
argument_list|)
expr_stmt|;
block|}
DECL|method|testApplyNoIngestId
specifier|public
name|void
name|testApplyNoIngestId
parameter_list|()
throws|throws
name|Exception
block|{
name|IndexRequest
name|indexRequest
init|=
operator|new
name|IndexRequest
argument_list|()
decl_stmt|;
name|ActionListener
name|actionListener
init|=
name|mock
argument_list|(
name|ActionListener
operator|.
name|class
argument_list|)
decl_stmt|;
name|ActionFilterChain
name|actionFilterChain
init|=
name|mock
argument_list|(
name|ActionFilterChain
operator|.
name|class
argument_list|)
decl_stmt|;
name|filter
operator|.
name|apply
argument_list|(
literal|"_action"
argument_list|,
name|indexRequest
argument_list|,
name|actionListener
argument_list|,
name|actionFilterChain
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|actionFilterChain
argument_list|)
operator|.
name|proceed
argument_list|(
literal|"_action"
argument_list|,
name|indexRequest
argument_list|,
name|actionListener
argument_list|)
expr_stmt|;
name|verifyZeroInteractions
argument_list|(
name|executionService
argument_list|,
name|actionFilterChain
argument_list|)
expr_stmt|;
block|}
DECL|method|testApplyIngestIdViaRequestParam
specifier|public
name|void
name|testApplyIngestIdViaRequestParam
parameter_list|()
throws|throws
name|Exception
block|{
name|IndexRequest
name|indexRequest
init|=
operator|new
name|IndexRequest
argument_list|(
literal|"_index"
argument_list|,
literal|"_type"
argument_list|,
literal|"_id"
argument_list|)
decl_stmt|;
name|indexRequest
operator|.
name|source
argument_list|(
literal|"field"
argument_list|,
literal|"value"
argument_list|)
expr_stmt|;
name|indexRequest
operator|.
name|putHeader
argument_list|(
name|IngestPlugin
operator|.
name|PIPELINE_ID_PARAM
argument_list|,
literal|"_id"
argument_list|)
expr_stmt|;
name|ActionListener
name|actionListener
init|=
name|mock
argument_list|(
name|ActionListener
operator|.
name|class
argument_list|)
decl_stmt|;
name|ActionFilterChain
name|actionFilterChain
init|=
name|mock
argument_list|(
name|ActionFilterChain
operator|.
name|class
argument_list|)
decl_stmt|;
name|filter
operator|.
name|apply
argument_list|(
literal|"_action"
argument_list|,
name|indexRequest
argument_list|,
name|actionListener
argument_list|,
name|actionFilterChain
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|executionService
argument_list|)
operator|.
name|execute
argument_list|(
name|any
argument_list|(
name|IndexRequest
operator|.
name|class
argument_list|)
argument_list|,
name|eq
argument_list|(
literal|"_id"
argument_list|)
argument_list|,
name|any
argument_list|(
name|ActionListener
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|verifyZeroInteractions
argument_list|(
name|actionFilterChain
argument_list|)
expr_stmt|;
block|}
DECL|method|testApplyIngestIdViaContext
specifier|public
name|void
name|testApplyIngestIdViaContext
parameter_list|()
throws|throws
name|Exception
block|{
name|IndexRequest
name|indexRequest
init|=
operator|new
name|IndexRequest
argument_list|(
literal|"_index"
argument_list|,
literal|"_type"
argument_list|,
literal|"_id"
argument_list|)
decl_stmt|;
name|indexRequest
operator|.
name|source
argument_list|(
literal|"field"
argument_list|,
literal|"value"
argument_list|)
expr_stmt|;
name|indexRequest
operator|.
name|putInContext
argument_list|(
name|IngestPlugin
operator|.
name|PIPELINE_ID_PARAM_CONTEXT_KEY
argument_list|,
literal|"_id"
argument_list|)
expr_stmt|;
name|ActionListener
name|actionListener
init|=
name|mock
argument_list|(
name|ActionListener
operator|.
name|class
argument_list|)
decl_stmt|;
name|ActionFilterChain
name|actionFilterChain
init|=
name|mock
argument_list|(
name|ActionFilterChain
operator|.
name|class
argument_list|)
decl_stmt|;
name|filter
operator|.
name|apply
argument_list|(
literal|"_action"
argument_list|,
name|indexRequest
argument_list|,
name|actionListener
argument_list|,
name|actionFilterChain
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|executionService
argument_list|)
operator|.
name|execute
argument_list|(
name|any
argument_list|(
name|IndexRequest
operator|.
name|class
argument_list|)
argument_list|,
name|eq
argument_list|(
literal|"_id"
argument_list|)
argument_list|,
name|any
argument_list|(
name|ActionListener
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|verifyZeroInteractions
argument_list|(
name|actionFilterChain
argument_list|)
expr_stmt|;
block|}
DECL|method|testApplyAlreadyProcessed
specifier|public
name|void
name|testApplyAlreadyProcessed
parameter_list|()
throws|throws
name|Exception
block|{
name|IndexRequest
name|indexRequest
init|=
operator|new
name|IndexRequest
argument_list|(
literal|"_index"
argument_list|,
literal|"_type"
argument_list|,
literal|"_id"
argument_list|)
decl_stmt|;
name|indexRequest
operator|.
name|source
argument_list|(
literal|"field"
argument_list|,
literal|"value"
argument_list|)
expr_stmt|;
name|indexRequest
operator|.
name|putHeader
argument_list|(
name|IngestPlugin
operator|.
name|PIPELINE_ID_PARAM
argument_list|,
literal|"_id"
argument_list|)
expr_stmt|;
name|indexRequest
operator|.
name|putHeader
argument_list|(
name|IngestPlugin
operator|.
name|PIPELINE_ALREADY_PROCESSED
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|ActionListener
name|actionListener
init|=
name|mock
argument_list|(
name|ActionListener
operator|.
name|class
argument_list|)
decl_stmt|;
name|ActionFilterChain
name|actionFilterChain
init|=
name|mock
argument_list|(
name|ActionFilterChain
operator|.
name|class
argument_list|)
decl_stmt|;
name|filter
operator|.
name|apply
argument_list|(
literal|"_action"
argument_list|,
name|indexRequest
argument_list|,
name|actionListener
argument_list|,
name|actionFilterChain
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|actionFilterChain
argument_list|)
operator|.
name|proceed
argument_list|(
literal|"_action"
argument_list|,
name|indexRequest
argument_list|,
name|actionListener
argument_list|)
expr_stmt|;
name|verifyZeroInteractions
argument_list|(
name|executionService
argument_list|,
name|actionListener
argument_list|)
expr_stmt|;
block|}
DECL|method|testApplyExecuted
specifier|public
name|void
name|testApplyExecuted
parameter_list|()
throws|throws
name|Exception
block|{
name|IndexRequest
name|indexRequest
init|=
operator|new
name|IndexRequest
argument_list|(
literal|"_index"
argument_list|,
literal|"_type"
argument_list|,
literal|"_id"
argument_list|)
decl_stmt|;
name|indexRequest
operator|.
name|source
argument_list|(
literal|"field"
argument_list|,
literal|"value"
argument_list|)
expr_stmt|;
name|indexRequest
operator|.
name|putHeader
argument_list|(
name|IngestPlugin
operator|.
name|PIPELINE_ID_PARAM
argument_list|,
literal|"_id"
argument_list|)
expr_stmt|;
name|ActionListener
name|actionListener
init|=
name|mock
argument_list|(
name|ActionListener
operator|.
name|class
argument_list|)
decl_stmt|;
name|ActionFilterChain
name|actionFilterChain
init|=
name|mock
argument_list|(
name|ActionFilterChain
operator|.
name|class
argument_list|)
decl_stmt|;
name|Answer
name|answer
init|=
name|invocationOnMock
lambda|->
block|{
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
name|ActionListener
argument_list|<
name|Void
argument_list|>
name|listener
init|=
operator|(
name|ActionListener
argument_list|<
name|Void
argument_list|>
operator|)
name|invocationOnMock
operator|.
name|getArguments
argument_list|()
index|[
literal|2
index|]
decl_stmt|;
name|listener
operator|.
name|onResponse
argument_list|(
literal|null
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
decl_stmt|;
name|doAnswer
argument_list|(
name|answer
argument_list|)
operator|.
name|when
argument_list|(
name|executionService
argument_list|)
operator|.
name|execute
argument_list|(
name|any
argument_list|(
name|IndexRequest
operator|.
name|class
argument_list|)
argument_list|,
name|eq
argument_list|(
literal|"_id"
argument_list|)
argument_list|,
name|any
argument_list|(
name|ActionListener
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|filter
operator|.
name|apply
argument_list|(
literal|"_action"
argument_list|,
name|indexRequest
argument_list|,
name|actionListener
argument_list|,
name|actionFilterChain
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|executionService
argument_list|)
operator|.
name|execute
argument_list|(
name|any
argument_list|(
name|IndexRequest
operator|.
name|class
argument_list|)
argument_list|,
name|eq
argument_list|(
literal|"_id"
argument_list|)
argument_list|,
name|any
argument_list|(
name|ActionListener
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|actionFilterChain
argument_list|)
operator|.
name|proceed
argument_list|(
literal|"_action"
argument_list|,
name|indexRequest
argument_list|,
name|actionListener
argument_list|)
expr_stmt|;
name|verifyZeroInteractions
argument_list|(
name|actionListener
argument_list|)
expr_stmt|;
block|}
DECL|method|testApplyFailed
specifier|public
name|void
name|testApplyFailed
parameter_list|()
throws|throws
name|Exception
block|{
name|IndexRequest
name|indexRequest
init|=
operator|new
name|IndexRequest
argument_list|(
literal|"_index"
argument_list|,
literal|"_type"
argument_list|,
literal|"_id"
argument_list|)
decl_stmt|;
name|indexRequest
operator|.
name|source
argument_list|(
literal|"field"
argument_list|,
literal|"value"
argument_list|)
expr_stmt|;
name|indexRequest
operator|.
name|putHeader
argument_list|(
name|IngestPlugin
operator|.
name|PIPELINE_ID_PARAM
argument_list|,
literal|"_id"
argument_list|)
expr_stmt|;
name|ActionListener
name|actionListener
init|=
name|mock
argument_list|(
name|ActionListener
operator|.
name|class
argument_list|)
decl_stmt|;
name|ActionFilterChain
name|actionFilterChain
init|=
name|mock
argument_list|(
name|ActionFilterChain
operator|.
name|class
argument_list|)
decl_stmt|;
name|RuntimeException
name|exception
init|=
operator|new
name|RuntimeException
argument_list|()
decl_stmt|;
name|Answer
name|answer
init|=
operator|new
name|Answer
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Object
name|answer
parameter_list|(
name|InvocationOnMock
name|invocationOnMock
parameter_list|)
throws|throws
name|Throwable
block|{
name|ActionListener
name|listener
init|=
operator|(
name|ActionListener
operator|)
name|invocationOnMock
operator|.
name|getArguments
argument_list|()
index|[
literal|2
index|]
decl_stmt|;
name|listener
operator|.
name|onFailure
argument_list|(
name|exception
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
decl_stmt|;
name|doAnswer
argument_list|(
name|answer
argument_list|)
operator|.
name|when
argument_list|(
name|executionService
argument_list|)
operator|.
name|execute
argument_list|(
name|any
argument_list|(
name|IndexRequest
operator|.
name|class
argument_list|)
argument_list|,
name|eq
argument_list|(
literal|"_id"
argument_list|)
argument_list|,
name|any
argument_list|(
name|ActionListener
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|filter
operator|.
name|apply
argument_list|(
literal|"_action"
argument_list|,
name|indexRequest
argument_list|,
name|actionListener
argument_list|,
name|actionFilterChain
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|executionService
argument_list|)
operator|.
name|execute
argument_list|(
name|any
argument_list|(
name|IndexRequest
operator|.
name|class
argument_list|)
argument_list|,
name|eq
argument_list|(
literal|"_id"
argument_list|)
argument_list|,
name|any
argument_list|(
name|ActionListener
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|actionListener
argument_list|)
operator|.
name|onFailure
argument_list|(
name|exception
argument_list|)
expr_stmt|;
name|verifyZeroInteractions
argument_list|(
name|actionFilterChain
argument_list|)
expr_stmt|;
block|}
DECL|method|testApplyWithBulkRequest
specifier|public
name|void
name|testApplyWithBulkRequest
parameter_list|()
throws|throws
name|Exception
block|{
name|ThreadPool
name|threadPool
init|=
operator|new
name|ThreadPool
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"name"
argument_list|,
literal|"_name"
argument_list|)
operator|.
name|put
argument_list|(
name|PipelineExecutionService
operator|.
name|additionalSettings
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|PipelineStore
name|store
init|=
name|mock
argument_list|(
name|PipelineStore
operator|.
name|class
argument_list|)
decl_stmt|;
name|Processor
name|processor
init|=
operator|new
name|Processor
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|execute
parameter_list|(
name|IngestDocument
name|ingestDocument
parameter_list|)
block|{
name|ingestDocument
operator|.
name|setFieldValue
argument_list|(
literal|"field2"
argument_list|,
literal|"value2"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getType
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
block|}
decl_stmt|;
name|when
argument_list|(
name|store
operator|.
name|get
argument_list|(
literal|"_id"
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
operator|new
name|Pipeline
argument_list|(
literal|"_id"
argument_list|,
literal|"_description"
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
name|processor
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|executionService
operator|=
operator|new
name|PipelineExecutionService
argument_list|(
name|store
argument_list|,
name|threadPool
argument_list|)
expr_stmt|;
name|IngestBootstrapper
name|bootstrapper
init|=
name|mock
argument_list|(
name|IngestBootstrapper
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|bootstrapper
operator|.
name|getPipelineExecutionService
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|executionService
argument_list|)
expr_stmt|;
name|filter
operator|=
operator|new
name|IngestActionFilter
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
name|bootstrapper
argument_list|)
expr_stmt|;
name|BulkRequest
name|bulkRequest
init|=
operator|new
name|BulkRequest
argument_list|()
decl_stmt|;
name|bulkRequest
operator|.
name|putHeader
argument_list|(
name|IngestPlugin
operator|.
name|PIPELINE_ID_PARAM
argument_list|,
literal|"_id"
argument_list|)
expr_stmt|;
name|int
name|numRequest
init|=
name|scaledRandomIntBetween
argument_list|(
literal|8
argument_list|,
literal|64
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
name|numRequest
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|rarely
argument_list|()
condition|)
block|{
name|ActionRequest
name|request
decl_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|request
operator|=
operator|new
name|DeleteRequest
argument_list|(
literal|"_index"
argument_list|,
literal|"_type"
argument_list|,
literal|"_id"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|request
operator|=
operator|new
name|UpdateRequest
argument_list|(
literal|"_index"
argument_list|,
literal|"_type"
argument_list|,
literal|"_id"
argument_list|)
expr_stmt|;
block|}
name|bulkRequest
operator|.
name|add
argument_list|(
name|request
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|IndexRequest
name|indexRequest
init|=
operator|new
name|IndexRequest
argument_list|(
literal|"_index"
argument_list|,
literal|"_type"
argument_list|,
literal|"_id"
argument_list|)
decl_stmt|;
name|indexRequest
operator|.
name|source
argument_list|(
literal|"field1"
argument_list|,
literal|"value1"
argument_list|)
expr_stmt|;
name|bulkRequest
operator|.
name|add
argument_list|(
name|indexRequest
argument_list|)
expr_stmt|;
block|}
block|}
name|ActionListener
name|actionListener
init|=
name|mock
argument_list|(
name|ActionListener
operator|.
name|class
argument_list|)
decl_stmt|;
name|ActionFilterChain
name|actionFilterChain
init|=
name|mock
argument_list|(
name|ActionFilterChain
operator|.
name|class
argument_list|)
decl_stmt|;
name|filter
operator|.
name|apply
argument_list|(
literal|"_action"
argument_list|,
name|bulkRequest
argument_list|,
name|actionListener
argument_list|,
name|actionFilterChain
argument_list|)
expr_stmt|;
name|assertBusy
argument_list|(
operator|new
name|Runnable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|verify
argument_list|(
name|actionFilterChain
argument_list|)
operator|.
name|proceed
argument_list|(
literal|"_action"
argument_list|,
name|bulkRequest
argument_list|,
name|actionListener
argument_list|)
expr_stmt|;
name|verifyZeroInteractions
argument_list|(
name|actionListener
argument_list|)
expr_stmt|;
name|int
name|assertedRequests
init|=
literal|0
decl_stmt|;
for|for
control|(
name|ActionRequest
name|actionRequest
range|:
name|bulkRequest
operator|.
name|requests
argument_list|()
control|)
block|{
if|if
condition|(
name|actionRequest
operator|instanceof
name|IndexRequest
condition|)
block|{
name|IndexRequest
name|indexRequest
init|=
operator|(
name|IndexRequest
operator|)
name|actionRequest
decl_stmt|;
name|assertThat
argument_list|(
name|indexRequest
operator|.
name|sourceAsMap
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
name|indexRequest
operator|.
name|sourceAsMap
argument_list|()
operator|.
name|get
argument_list|(
literal|"field1"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"value1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|indexRequest
operator|.
name|sourceAsMap
argument_list|()
operator|.
name|get
argument_list|(
literal|"field2"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"value2"
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|assertedRequests
operator|++
expr_stmt|;
block|}
name|assertThat
argument_list|(
name|assertedRequests
argument_list|,
name|equalTo
argument_list|(
name|numRequest
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
name|threadPool
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
DECL|method|testApplyWithBulkRequestWithFailureAllFailed
specifier|public
name|void
name|testApplyWithBulkRequestWithFailureAllFailed
parameter_list|()
throws|throws
name|Exception
block|{
name|BulkRequest
name|bulkRequest
init|=
operator|new
name|BulkRequest
argument_list|()
decl_stmt|;
name|bulkRequest
operator|.
name|putHeader
argument_list|(
name|IngestPlugin
operator|.
name|PIPELINE_ID_PARAM
argument_list|,
literal|"_id"
argument_list|)
expr_stmt|;
name|int
name|numRequest
init|=
name|scaledRandomIntBetween
argument_list|(
literal|0
argument_list|,
literal|8
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
name|numRequest
condition|;
name|i
operator|++
control|)
block|{
name|IndexRequest
name|indexRequest
init|=
operator|new
name|IndexRequest
argument_list|(
literal|"_index"
argument_list|,
literal|"_type"
argument_list|,
literal|"_id"
argument_list|)
decl_stmt|;
name|indexRequest
operator|.
name|source
argument_list|(
literal|"field1"
argument_list|,
literal|"value1"
argument_list|)
expr_stmt|;
name|bulkRequest
operator|.
name|add
argument_list|(
name|indexRequest
argument_list|)
expr_stmt|;
block|}
name|RuntimeException
name|exception
init|=
operator|new
name|RuntimeException
argument_list|()
decl_stmt|;
name|Answer
name|answer
init|=
parameter_list|(
name|invocationOnMock
parameter_list|)
lambda|->
block|{
name|ActionListener
name|listener
init|=
operator|(
name|ActionListener
operator|)
name|invocationOnMock
operator|.
name|getArguments
argument_list|()
index|[
literal|2
index|]
decl_stmt|;
name|listener
operator|.
name|onFailure
argument_list|(
name|exception
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
decl_stmt|;
name|doAnswer
argument_list|(
name|answer
argument_list|)
operator|.
name|when
argument_list|(
name|executionService
argument_list|)
operator|.
name|execute
argument_list|(
name|any
argument_list|(
name|IndexRequest
operator|.
name|class
argument_list|)
argument_list|,
name|eq
argument_list|(
literal|"_id"
argument_list|)
argument_list|,
name|any
argument_list|(
name|ActionListener
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|CaptureActionListener
name|actionListener
init|=
operator|new
name|CaptureActionListener
argument_list|()
decl_stmt|;
name|RecordRequestAFC
name|actionFilterChain
init|=
operator|new
name|RecordRequestAFC
argument_list|()
decl_stmt|;
name|filter
operator|.
name|apply
argument_list|(
literal|"_action"
argument_list|,
name|bulkRequest
argument_list|,
name|actionListener
argument_list|,
name|actionFilterChain
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|actionFilterChain
operator|.
name|request
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|ActionResponse
name|response
init|=
name|actionListener
operator|.
name|response
decl_stmt|;
name|assertThat
argument_list|(
name|response
argument_list|,
name|instanceOf
argument_list|(
name|BulkResponse
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|BulkResponse
name|bulkResponse
init|=
operator|(
name|BulkResponse
operator|)
name|response
decl_stmt|;
name|assertThat
argument_list|(
name|bulkResponse
operator|.
name|getItems
argument_list|()
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
name|numRequest
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|BulkItemResponse
name|bulkItemResponse
range|:
name|bulkResponse
control|)
block|{
name|assertThat
argument_list|(
name|bulkItemResponse
operator|.
name|isFailed
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testApplyWithBulkRequestWithFailure
specifier|public
name|void
name|testApplyWithBulkRequestWithFailure
parameter_list|()
throws|throws
name|Exception
block|{
name|BulkRequest
name|bulkRequest
init|=
operator|new
name|BulkRequest
argument_list|()
decl_stmt|;
name|bulkRequest
operator|.
name|putHeader
argument_list|(
name|IngestPlugin
operator|.
name|PIPELINE_ID_PARAM
argument_list|,
literal|"_id"
argument_list|)
expr_stmt|;
name|int
name|numRequest
init|=
name|scaledRandomIntBetween
argument_list|(
literal|8
argument_list|,
literal|64
argument_list|)
decl_stmt|;
name|int
name|numNonIndexRequests
init|=
literal|0
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
name|numRequest
condition|;
name|i
operator|++
control|)
block|{
name|ActionRequest
name|request
decl_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|numNonIndexRequests
operator|++
expr_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|request
operator|=
operator|new
name|DeleteRequest
argument_list|(
literal|"_index"
argument_list|,
literal|"_type"
argument_list|,
literal|"_id"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|request
operator|=
operator|new
name|UpdateRequest
argument_list|(
literal|"_index"
argument_list|,
literal|"_type"
argument_list|,
literal|"_id"
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|IndexRequest
name|indexRequest
init|=
operator|new
name|IndexRequest
argument_list|(
literal|"_index"
argument_list|,
literal|"_type"
argument_list|,
literal|"_id"
argument_list|)
decl_stmt|;
name|indexRequest
operator|.
name|source
argument_list|(
literal|"field1"
argument_list|,
literal|"value1"
argument_list|)
expr_stmt|;
name|request
operator|=
name|indexRequest
expr_stmt|;
block|}
name|bulkRequest
operator|.
name|add
argument_list|(
name|request
argument_list|)
expr_stmt|;
block|}
name|RuntimeException
name|exception
init|=
operator|new
name|RuntimeException
argument_list|()
decl_stmt|;
name|Answer
name|answer
init|=
parameter_list|(
name|invocationOnMock
parameter_list|)
lambda|->
block|{
name|ActionListener
name|listener
init|=
operator|(
name|ActionListener
operator|)
name|invocationOnMock
operator|.
name|getArguments
argument_list|()
index|[
literal|2
index|]
decl_stmt|;
name|listener
operator|.
name|onFailure
argument_list|(
name|exception
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
decl_stmt|;
name|doAnswer
argument_list|(
name|answer
argument_list|)
operator|.
name|when
argument_list|(
name|executionService
argument_list|)
operator|.
name|execute
argument_list|(
name|any
argument_list|(
name|IndexRequest
operator|.
name|class
argument_list|)
argument_list|,
name|eq
argument_list|(
literal|"_id"
argument_list|)
argument_list|,
name|any
argument_list|(
name|ActionListener
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|ActionListener
name|actionListener
init|=
name|mock
argument_list|(
name|ActionListener
operator|.
name|class
argument_list|)
decl_stmt|;
name|RecordRequestAFC
name|actionFilterChain
init|=
operator|new
name|RecordRequestAFC
argument_list|()
decl_stmt|;
name|filter
operator|.
name|apply
argument_list|(
literal|"_action"
argument_list|,
name|bulkRequest
argument_list|,
name|actionListener
argument_list|,
name|actionFilterChain
argument_list|)
expr_stmt|;
name|BulkRequest
name|interceptedRequests
init|=
name|actionFilterChain
operator|.
name|getRequest
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|interceptedRequests
operator|.
name|requests
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|numNonIndexRequests
argument_list|)
argument_list|)
expr_stmt|;
name|verifyZeroInteractions
argument_list|(
name|actionListener
argument_list|)
expr_stmt|;
block|}
DECL|method|testBulkRequestModifier
specifier|public
name|void
name|testBulkRequestModifier
parameter_list|()
block|{
name|int
name|numRequests
init|=
name|scaledRandomIntBetween
argument_list|(
literal|8
argument_list|,
literal|64
argument_list|)
decl_stmt|;
name|BulkRequest
name|bulkRequest
init|=
operator|new
name|BulkRequest
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
name|numRequests
condition|;
name|i
operator|++
control|)
block|{
name|bulkRequest
operator|.
name|add
argument_list|(
operator|new
name|IndexRequest
argument_list|(
literal|"_index"
argument_list|,
literal|"_type"
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|i
argument_list|)
argument_list|)
operator|.
name|source
argument_list|(
literal|"{}"
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|CaptureActionListener
name|actionListener
init|=
operator|new
name|CaptureActionListener
argument_list|()
decl_stmt|;
name|BulkRequestModifier
name|bulkRequestModifier
init|=
operator|new
name|BulkRequestModifier
argument_list|(
name|bulkRequest
argument_list|)
decl_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
name|Set
argument_list|<
name|Integer
argument_list|>
name|failedSlots
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
while|while
condition|(
name|bulkRequestModifier
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|bulkRequestModifier
operator|.
name|next
argument_list|()
expr_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|bulkRequestModifier
operator|.
name|markCurrentItemAsFailed
argument_list|(
operator|new
name|RuntimeException
argument_list|()
argument_list|)
expr_stmt|;
name|failedSlots
operator|.
name|add
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
name|i
operator|++
expr_stmt|;
block|}
name|assertThat
argument_list|(
name|bulkRequestModifier
operator|.
name|getBulkRequest
argument_list|()
operator|.
name|requests
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|numRequests
operator|-
name|failedSlots
operator|.
name|size
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// simulate that we actually executed the modified bulk request:
name|ActionListener
argument_list|<
name|BulkResponse
argument_list|>
name|result
init|=
name|bulkRequestModifier
operator|.
name|wrapActionListenerIfNeeded
argument_list|(
name|actionListener
argument_list|)
decl_stmt|;
name|result
operator|.
name|onResponse
argument_list|(
operator|new
name|BulkResponse
argument_list|(
operator|new
name|BulkItemResponse
index|[
name|numRequests
operator|-
name|failedSlots
operator|.
name|size
argument_list|()
index|]
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|BulkResponse
name|bulkResponse
init|=
name|actionListener
operator|.
name|getResponse
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|bulkResponse
operator|.
name|getItems
argument_list|()
operator|.
name|length
condition|;
name|j
operator|++
control|)
block|{
if|if
condition|(
name|failedSlots
operator|.
name|contains
argument_list|(
name|j
argument_list|)
condition|)
block|{
name|BulkItemResponse
name|item
init|=
name|bulkResponse
operator|.
name|getItems
argument_list|()
index|[
name|j
index|]
decl_stmt|;
name|assertThat
argument_list|(
name|item
operator|.
name|isFailed
argument_list|()
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|item
operator|.
name|getFailure
argument_list|()
operator|.
name|getIndex
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"_index"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|item
operator|.
name|getFailure
argument_list|()
operator|.
name|getType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"_type"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|item
operator|.
name|getFailure
argument_list|()
operator|.
name|getId
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|String
operator|.
name|valueOf
argument_list|(
name|j
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|item
operator|.
name|getFailure
argument_list|()
operator|.
name|getMessage
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"java.lang.RuntimeException"
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertThat
argument_list|(
name|bulkResponse
operator|.
name|getItems
argument_list|()
index|[
name|j
index|]
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|class|RecordRequestAFC
specifier|private
specifier|final
specifier|static
class|class
name|RecordRequestAFC
implements|implements
name|ActionFilterChain
block|{
DECL|field|request
specifier|private
name|ActionRequest
name|request
decl_stmt|;
annotation|@
name|Override
DECL|method|proceed
specifier|public
name|void
name|proceed
parameter_list|(
name|String
name|action
parameter_list|,
name|ActionRequest
name|request
parameter_list|,
name|ActionListener
name|listener
parameter_list|)
block|{
name|this
operator|.
name|request
operator|=
name|request
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|proceed
specifier|public
name|void
name|proceed
parameter_list|(
name|String
name|action
parameter_list|,
name|ActionResponse
name|response
parameter_list|,
name|ActionListener
name|listener
parameter_list|)
block|{          }
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|method|getRequest
specifier|public
parameter_list|<
name|T
extends|extends
name|ActionRequest
argument_list|<
name|T
argument_list|>
parameter_list|>
name|T
name|getRequest
parameter_list|()
block|{
return|return
operator|(
name|T
operator|)
name|request
return|;
block|}
block|}
DECL|class|CaptureActionListener
specifier|private
specifier|final
specifier|static
class|class
name|CaptureActionListener
implements|implements
name|ActionListener
argument_list|<
name|BulkResponse
argument_list|>
block|{
DECL|field|response
specifier|private
name|BulkResponse
name|response
decl_stmt|;
annotation|@
name|Override
DECL|method|onResponse
specifier|public
name|void
name|onResponse
parameter_list|(
name|BulkResponse
name|bulkItemResponses
parameter_list|)
block|{
name|this
operator|.
name|response
operator|=
name|bulkItemResponses
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
block|{         }
DECL|method|getResponse
specifier|public
name|BulkResponse
name|getResponse
parameter_list|()
block|{
return|return
name|response
return|;
block|}
block|}
block|}
end_class

end_unit

