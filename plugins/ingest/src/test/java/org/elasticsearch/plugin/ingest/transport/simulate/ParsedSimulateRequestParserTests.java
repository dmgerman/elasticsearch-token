begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.plugin.ingest.transport.simulate
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
operator|.
name|simulate
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
name|Data
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
name|PipelineStore
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
name|*
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
name|simulate
operator|.
name|SimulatePipelineRequest
operator|.
name|Fields
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

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|when
import|;
end_import

begin_class
DECL|class|ParsedSimulateRequestParserTests
specifier|public
class|class
name|ParsedSimulateRequestParserTests
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
name|IOException
block|{
name|Pipeline
name|pipeline
init|=
operator|new
name|Pipeline
argument_list|(
name|ParsedSimulateRequest
operator|.
name|Parser
operator|.
name|SIMULATED_PIPELINE_ID
argument_list|,
literal|null
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
name|mock
argument_list|(
name|Processor
operator|.
name|class
argument_list|)
argument_list|)
argument_list|)
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
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|processorRegistry
operator|.
name|put
argument_list|(
literal|"mock_processor"
argument_list|,
name|mock
argument_list|(
name|Processor
operator|.
name|Factory
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|store
operator|=
name|mock
argument_list|(
name|PipelineStore
operator|.
name|class
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|store
operator|.
name|get
argument_list|(
name|ParsedSimulateRequest
operator|.
name|Parser
operator|.
name|SIMULATED_PIPELINE_ID
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|pipeline
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|store
operator|.
name|getProcessorFactoryRegistry
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|processorRegistry
argument_list|)
expr_stmt|;
block|}
DECL|method|testParseUsingPipelineStore
specifier|public
name|void
name|testParseUsingPipelineStore
parameter_list|()
throws|throws
name|Exception
block|{
name|int
name|numDocs
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|requestContent
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|>
name|docs
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|>
name|expectedDocs
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|requestContent
operator|.
name|put
argument_list|(
name|Fields
operator|.
name|DOCS
argument_list|,
name|docs
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
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|doc
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|String
name|index
init|=
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|String
name|type
init|=
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|String
name|id
init|=
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|doc
operator|.
name|put
argument_list|(
name|Fields
operator|.
name|INDEX
argument_list|,
name|index
argument_list|)
expr_stmt|;
name|doc
operator|.
name|put
argument_list|(
name|Fields
operator|.
name|TYPE
argument_list|,
name|type
argument_list|)
expr_stmt|;
name|doc
operator|.
name|put
argument_list|(
name|Fields
operator|.
name|ID
argument_list|,
name|id
argument_list|)
expr_stmt|;
name|String
name|fieldName
init|=
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|String
name|fieldValue
init|=
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|doc
operator|.
name|put
argument_list|(
name|Fields
operator|.
name|SOURCE
argument_list|,
name|Collections
operator|.
name|singletonMap
argument_list|(
name|fieldName
argument_list|,
name|fieldValue
argument_list|)
argument_list|)
expr_stmt|;
name|docs
operator|.
name|add
argument_list|(
name|doc
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|expectedDoc
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|expectedDoc
operator|.
name|put
argument_list|(
name|Fields
operator|.
name|INDEX
argument_list|,
name|index
argument_list|)
expr_stmt|;
name|expectedDoc
operator|.
name|put
argument_list|(
name|Fields
operator|.
name|TYPE
argument_list|,
name|type
argument_list|)
expr_stmt|;
name|expectedDoc
operator|.
name|put
argument_list|(
name|Fields
operator|.
name|ID
argument_list|,
name|id
argument_list|)
expr_stmt|;
name|expectedDoc
operator|.
name|put
argument_list|(
name|Fields
operator|.
name|SOURCE
argument_list|,
name|Collections
operator|.
name|singletonMap
argument_list|(
name|fieldName
argument_list|,
name|fieldValue
argument_list|)
argument_list|)
expr_stmt|;
name|expectedDocs
operator|.
name|add
argument_list|(
name|expectedDoc
argument_list|)
expr_stmt|;
block|}
name|ParsedSimulateRequest
name|actualRequest
init|=
operator|new
name|ParsedSimulateRequest
operator|.
name|Parser
argument_list|()
operator|.
name|parseWithPipelineId
argument_list|(
name|ParsedSimulateRequest
operator|.
name|Parser
operator|.
name|SIMULATED_PIPELINE_ID
argument_list|,
name|requestContent
argument_list|,
literal|false
argument_list|,
name|store
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|actualRequest
operator|.
name|isVerbose
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|actualRequest
operator|.
name|getDocuments
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|numDocs
argument_list|)
argument_list|)
expr_stmt|;
name|Iterator
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|>
name|expectedDocsIterator
init|=
name|expectedDocs
operator|.
name|iterator
argument_list|()
decl_stmt|;
for|for
control|(
name|Data
name|data
range|:
name|actualRequest
operator|.
name|getDocuments
argument_list|()
control|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|expectedDocument
init|=
name|expectedDocsIterator
operator|.
name|next
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|data
operator|.
name|getDocument
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|expectedDocument
operator|.
name|get
argument_list|(
name|Fields
operator|.
name|SOURCE
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|data
operator|.
name|getIndex
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|expectedDocument
operator|.
name|get
argument_list|(
name|Fields
operator|.
name|INDEX
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|data
operator|.
name|getType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|expectedDocument
operator|.
name|get
argument_list|(
name|Fields
operator|.
name|TYPE
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|data
operator|.
name|getId
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|expectedDocument
operator|.
name|get
argument_list|(
name|Fields
operator|.
name|ID
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|assertThat
argument_list|(
name|actualRequest
operator|.
name|getPipeline
argument_list|()
operator|.
name|getId
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|ParsedSimulateRequest
operator|.
name|Parser
operator|.
name|SIMULATED_PIPELINE_ID
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|actualRequest
operator|.
name|getPipeline
argument_list|()
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
name|actualRequest
operator|.
name|getPipeline
argument_list|()
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
block|}
DECL|method|testParseWithProvidedPipeline
specifier|public
name|void
name|testParseWithProvidedPipeline
parameter_list|()
throws|throws
name|Exception
block|{
name|int
name|numDocs
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|requestContent
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|>
name|docs
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|>
name|expectedDocs
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|requestContent
operator|.
name|put
argument_list|(
name|Fields
operator|.
name|DOCS
argument_list|,
name|docs
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
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|doc
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|String
name|index
init|=
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|String
name|type
init|=
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|String
name|id
init|=
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|doc
operator|.
name|put
argument_list|(
name|Fields
operator|.
name|INDEX
argument_list|,
name|index
argument_list|)
expr_stmt|;
name|doc
operator|.
name|put
argument_list|(
name|Fields
operator|.
name|TYPE
argument_list|,
name|type
argument_list|)
expr_stmt|;
name|doc
operator|.
name|put
argument_list|(
name|Fields
operator|.
name|ID
argument_list|,
name|id
argument_list|)
expr_stmt|;
name|String
name|fieldName
init|=
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|String
name|fieldValue
init|=
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|doc
operator|.
name|put
argument_list|(
name|Fields
operator|.
name|SOURCE
argument_list|,
name|Collections
operator|.
name|singletonMap
argument_list|(
name|fieldName
argument_list|,
name|fieldValue
argument_list|)
argument_list|)
expr_stmt|;
name|docs
operator|.
name|add
argument_list|(
name|doc
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|expectedDoc
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|expectedDoc
operator|.
name|put
argument_list|(
name|Fields
operator|.
name|INDEX
argument_list|,
name|index
argument_list|)
expr_stmt|;
name|expectedDoc
operator|.
name|put
argument_list|(
name|Fields
operator|.
name|TYPE
argument_list|,
name|type
argument_list|)
expr_stmt|;
name|expectedDoc
operator|.
name|put
argument_list|(
name|Fields
operator|.
name|ID
argument_list|,
name|id
argument_list|)
expr_stmt|;
name|expectedDoc
operator|.
name|put
argument_list|(
name|Fields
operator|.
name|SOURCE
argument_list|,
name|Collections
operator|.
name|singletonMap
argument_list|(
name|fieldName
argument_list|,
name|fieldValue
argument_list|)
argument_list|)
expr_stmt|;
name|expectedDocs
operator|.
name|add
argument_list|(
name|expectedDoc
argument_list|)
expr_stmt|;
block|}
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
name|List
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|>
name|processors
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|int
name|numProcessors
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|10
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
name|numProcessors
condition|;
name|i
operator|++
control|)
block|{
name|processors
operator|.
name|add
argument_list|(
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"mock_processor"
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|pipelineConfig
operator|.
name|put
argument_list|(
literal|"processors"
argument_list|,
name|processors
argument_list|)
expr_stmt|;
name|requestContent
operator|.
name|put
argument_list|(
name|Fields
operator|.
name|PIPELINE
argument_list|,
name|pipelineConfig
argument_list|)
expr_stmt|;
name|ParsedSimulateRequest
name|actualRequest
init|=
operator|new
name|ParsedSimulateRequest
operator|.
name|Parser
argument_list|()
operator|.
name|parse
argument_list|(
name|requestContent
argument_list|,
literal|false
argument_list|,
name|store
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|actualRequest
operator|.
name|isVerbose
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|actualRequest
operator|.
name|getDocuments
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|numDocs
argument_list|)
argument_list|)
expr_stmt|;
name|Iterator
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|>
name|expectedDocsIterator
init|=
name|expectedDocs
operator|.
name|iterator
argument_list|()
decl_stmt|;
for|for
control|(
name|Data
name|data
range|:
name|actualRequest
operator|.
name|getDocuments
argument_list|()
control|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|expectedDocument
init|=
name|expectedDocsIterator
operator|.
name|next
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|data
operator|.
name|getDocument
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|expectedDocument
operator|.
name|get
argument_list|(
name|Fields
operator|.
name|SOURCE
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|data
operator|.
name|getIndex
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|expectedDocument
operator|.
name|get
argument_list|(
name|Fields
operator|.
name|INDEX
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|data
operator|.
name|getType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|expectedDocument
operator|.
name|get
argument_list|(
name|Fields
operator|.
name|TYPE
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|data
operator|.
name|getId
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|expectedDocument
operator|.
name|get
argument_list|(
name|Fields
operator|.
name|ID
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|assertThat
argument_list|(
name|actualRequest
operator|.
name|getPipeline
argument_list|()
operator|.
name|getId
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|ParsedSimulateRequest
operator|.
name|Parser
operator|.
name|SIMULATED_PIPELINE_ID
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|actualRequest
operator|.
name|getPipeline
argument_list|()
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
name|actualRequest
operator|.
name|getPipeline
argument_list|()
operator|.
name|getProcessors
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|numProcessors
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

