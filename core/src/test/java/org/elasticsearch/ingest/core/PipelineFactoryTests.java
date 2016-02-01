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
name|processor
operator|.
name|ConfigurationPropertyException
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
name|nullValue
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
name|Map
argument_list|<
name|String
argument_list|,
name|Processor
operator|.
name|Factory
argument_list|>
name|processorRegistry
init|=
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
name|Collections
operator|.
name|emptyMap
argument_list|()
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
name|ConfigurationPropertyException
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
name|Map
argument_list|<
name|String
argument_list|,
name|Processor
operator|.
name|Factory
argument_list|>
name|processorRegistry
init|=
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
name|Map
argument_list|<
name|String
argument_list|,
name|Processor
operator|.
name|Factory
argument_list|>
name|processorRegistry
init|=
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
name|ConfigurationPropertyException
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
name|Map
argument_list|<
name|String
argument_list|,
name|Processor
operator|.
name|Factory
argument_list|>
name|processorRegistry
init|=
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
block|}
end_class

end_unit

