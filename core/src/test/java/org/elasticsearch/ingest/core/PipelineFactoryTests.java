begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.ingest.core
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|core
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchParseException
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
name|ingest
operator|.
name|ProcessorsRegistry
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
name|TestProcessor
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
name|TestTemplateService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|ScriptService
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
name|ClusterServiceUtils
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
name|TestThreadPool
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
name|CoreMatchers
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
name|nullValue
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
name|mock
import|;
end_import

begin_class
DECL|class|PipelineFactoryTests
specifier|public
class|class
name|PipelineFactoryTests
extends|extends
name|ESTestCase
block|{
DECL|method|testCreate
specifier|public
name|void
name|testCreate
parameter_list|()
throws|throws
name|Exception
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|processorConfig0
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|processorConfig1
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|processorConfig0
operator|.
name|put
argument_list|(
name|AbstractProcessorFactory
operator|.
name|TAG_KEY
argument_list|,
literal|"first-processor"
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
name|Pipeline
operator|.
name|DESCRIPTION_KEY
argument_list|,
literal|"_description"
argument_list|)
expr_stmt|;
name|pipelineConfig
operator|.
name|put
argument_list|(
name|Pipeline
operator|.
name|PROCESSORS_KEY
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"test"
argument_list|,
name|processorConfig0
argument_list|)
argument_list|,
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"test"
argument_list|,
name|processorConfig1
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|Pipeline
operator|.
name|Factory
name|factory
init|=
operator|new
name|Pipeline
operator|.
name|Factory
argument_list|()
decl_stmt|;
name|ProcessorsRegistry
name|processorRegistry
init|=
name|createProcessorRegistry
argument_list|(
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"test"
argument_list|,
operator|new
name|TestProcessor
operator|.
name|Factory
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|Pipeline
name|pipeline
init|=
name|factory
operator|.
name|create
argument_list|(
literal|"_id"
argument_list|,
name|pipelineConfig
argument_list|,
name|processorRegistry
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|pipeline
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
literal|2
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
literal|"test-processor"
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
name|getTag
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"first-processor"
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
literal|1
argument_list|)
operator|.
name|getType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"test-processor"
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
literal|1
argument_list|)
operator|.
name|getTag
argument_list|()
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testCreateWithNoProcessorsField
specifier|public
name|void
name|testCreateWithNoProcessorsField
parameter_list|()
throws|throws
name|Exception
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
name|Pipeline
operator|.
name|DESCRIPTION_KEY
argument_list|,
literal|"_description"
argument_list|)
expr_stmt|;
name|Pipeline
operator|.
name|Factory
name|factory
init|=
operator|new
name|Pipeline
operator|.
name|Factory
argument_list|()
decl_stmt|;
try|try
block|{
name|factory
operator|.
name|create
argument_list|(
literal|"_id"
argument_list|,
name|pipelineConfig
argument_list|,
name|createProcessorRegistry
argument_list|(
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"should fail, missing required [processors] field"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ElasticsearchParseException
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
literal|"[processors] required property is missing"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testCreateWithPipelineOnFailure
specifier|public
name|void
name|testCreateWithPipelineOnFailure
parameter_list|()
throws|throws
name|Exception
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
name|Pipeline
operator|.
name|DESCRIPTION_KEY
argument_list|,
literal|"_description"
argument_list|)
expr_stmt|;
name|pipelineConfig
operator|.
name|put
argument_list|(
name|Pipeline
operator|.
name|PROCESSORS_KEY
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"test"
argument_list|,
name|processorConfig
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|pipelineConfig
operator|.
name|put
argument_list|(
name|Pipeline
operator|.
name|ON_FAILURE_KEY
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"test"
argument_list|,
name|processorConfig
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|Pipeline
operator|.
name|Factory
name|factory
init|=
operator|new
name|Pipeline
operator|.
name|Factory
argument_list|()
decl_stmt|;
name|ProcessorsRegistry
name|processorRegistry
init|=
name|createProcessorRegistry
argument_list|(
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"test"
argument_list|,
operator|new
name|TestProcessor
operator|.
name|Factory
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|Pipeline
name|pipeline
init|=
name|factory
operator|.
name|create
argument_list|(
literal|"_id"
argument_list|,
name|pipelineConfig
argument_list|,
name|processorRegistry
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|pipeline
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
literal|"test-processor"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|pipeline
operator|.
name|getOnFailureProcessors
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
name|getOnFailureProcessors
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
literal|"test-processor"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testCreateWithPipelineIgnoreFailure
specifier|public
name|void
name|testCreateWithPipelineIgnoreFailure
parameter_list|()
throws|throws
name|Exception
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
literal|"ignore_failure"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|ProcessorsRegistry
name|processorRegistry
init|=
name|createProcessorRegistry
argument_list|(
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"test"
argument_list|,
operator|new
name|TestProcessor
operator|.
name|Factory
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|Pipeline
operator|.
name|Factory
name|factory
init|=
operator|new
name|Pipeline
operator|.
name|Factory
argument_list|()
decl_stmt|;
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
name|Pipeline
operator|.
name|DESCRIPTION_KEY
argument_list|,
literal|"_description"
argument_list|)
expr_stmt|;
name|pipelineConfig
operator|.
name|put
argument_list|(
name|Pipeline
operator|.
name|PROCESSORS_KEY
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"test"
argument_list|,
name|processorConfig
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|Pipeline
name|pipeline
init|=
name|factory
operator|.
name|create
argument_list|(
literal|"_id"
argument_list|,
name|pipelineConfig
argument_list|,
name|processorRegistry
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|pipeline
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
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|pipeline
operator|.
name|getOnFailureProcessors
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
name|CompoundProcessor
name|processor
init|=
operator|(
name|CompoundProcessor
operator|)
name|pipeline
operator|.
name|getProcessors
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|processor
operator|.
name|isIgnoreFailure
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
name|processor
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
literal|"test-processor"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testCreateUnusedProcessorOptions
specifier|public
name|void
name|testCreateUnusedProcessorOptions
parameter_list|()
throws|throws
name|Exception
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
literal|"unused"
argument_list|,
literal|"value"
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
name|Pipeline
operator|.
name|DESCRIPTION_KEY
argument_list|,
literal|"_description"
argument_list|)
expr_stmt|;
name|pipelineConfig
operator|.
name|put
argument_list|(
name|Pipeline
operator|.
name|PROCESSORS_KEY
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"test"
argument_list|,
name|processorConfig
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|Pipeline
operator|.
name|Factory
name|factory
init|=
operator|new
name|Pipeline
operator|.
name|Factory
argument_list|()
decl_stmt|;
name|ProcessorsRegistry
name|processorRegistry
init|=
name|createProcessorRegistry
argument_list|(
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"test"
argument_list|,
operator|new
name|TestProcessor
operator|.
name|Factory
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
try|try
block|{
name|factory
operator|.
name|create
argument_list|(
literal|"_id"
argument_list|,
name|pipelineConfig
argument_list|,
name|processorRegistry
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ElasticsearchParseException
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
literal|"processor [test] doesn't support one or more provided configuration parameters [unused]"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testCreateProcessorsWithOnFailureProperties
specifier|public
name|void
name|testCreateProcessorsWithOnFailureProperties
parameter_list|()
throws|throws
name|Exception
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
name|Pipeline
operator|.
name|ON_FAILURE_KEY
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"test"
argument_list|,
operator|new
name|HashMap
argument_list|<>
argument_list|()
argument_list|)
argument_list|)
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
name|Pipeline
operator|.
name|DESCRIPTION_KEY
argument_list|,
literal|"_description"
argument_list|)
expr_stmt|;
name|pipelineConfig
operator|.
name|put
argument_list|(
name|Pipeline
operator|.
name|PROCESSORS_KEY
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"test"
argument_list|,
name|processorConfig
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|Pipeline
operator|.
name|Factory
name|factory
init|=
operator|new
name|Pipeline
operator|.
name|Factory
argument_list|()
decl_stmt|;
name|ProcessorsRegistry
name|processorRegistry
init|=
name|createProcessorRegistry
argument_list|(
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"test"
argument_list|,
operator|new
name|TestProcessor
operator|.
name|Factory
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|Pipeline
name|pipeline
init|=
name|factory
operator|.
name|create
argument_list|(
literal|"_id"
argument_list|,
name|pipelineConfig
argument_list|,
name|processorRegistry
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|pipeline
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
literal|"compound"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testFlattenProcessors
specifier|public
name|void
name|testFlattenProcessors
parameter_list|()
throws|throws
name|Exception
block|{
name|TestProcessor
name|testProcessor
init|=
operator|new
name|TestProcessor
argument_list|(
name|ingestDocument
lambda|->
block|{}
argument_list|)
decl_stmt|;
name|CompoundProcessor
name|processor1
init|=
operator|new
name|CompoundProcessor
argument_list|(
name|testProcessor
argument_list|,
name|testProcessor
argument_list|)
decl_stmt|;
name|CompoundProcessor
name|processor2
init|=
operator|new
name|CompoundProcessor
argument_list|(
literal|false
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
name|testProcessor
argument_list|)
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
name|testProcessor
argument_list|)
argument_list|)
decl_stmt|;
name|Pipeline
name|pipeline
init|=
operator|new
name|Pipeline
argument_list|(
literal|"_id"
argument_list|,
literal|"_description"
argument_list|,
operator|new
name|CompoundProcessor
argument_list|(
name|processor1
argument_list|,
name|processor2
argument_list|)
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Processor
argument_list|>
name|flattened
init|=
name|pipeline
operator|.
name|flattenAllProcessors
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|flattened
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|4
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|createProcessorRegistry
specifier|private
name|ProcessorsRegistry
name|createProcessorRegistry
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Processor
operator|.
name|Factory
argument_list|>
name|processorRegistry
parameter_list|)
block|{
name|ProcessorsRegistry
operator|.
name|Builder
name|builder
init|=
operator|new
name|ProcessorsRegistry
operator|.
name|Builder
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Processor
operator|.
name|Factory
argument_list|>
name|entry
range|:
name|processorRegistry
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|builder
operator|.
name|registerProcessor
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
operator|(
parameter_list|(
name|registry
parameter_list|)
lambda|->
name|entry
operator|.
name|getValue
argument_list|()
operator|)
argument_list|)
expr_stmt|;
block|}
return|return
name|builder
operator|.
name|build
argument_list|(
name|mock
argument_list|(
name|ScriptService
operator|.
name|class
argument_list|)
argument_list|,
name|mock
argument_list|(
name|ClusterService
operator|.
name|class
argument_list|)
argument_list|)
return|;
block|}
block|}
end_class

end_unit

