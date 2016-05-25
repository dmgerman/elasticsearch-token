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
name|ElasticsearchException
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
name|hasSize
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

begin_class
DECL|class|CompoundProcessorTests
specifier|public
class|class
name|CompoundProcessorTests
extends|extends
name|ESTestCase
block|{
DECL|field|ingestDocument
specifier|private
name|IngestDocument
name|ingestDocument
decl_stmt|;
annotation|@
name|Before
DECL|method|init
specifier|public
name|void
name|init
parameter_list|()
block|{
name|ingestDocument
operator|=
operator|new
name|IngestDocument
argument_list|(
operator|new
name|HashMap
argument_list|<>
argument_list|()
argument_list|,
operator|new
name|HashMap
argument_list|<>
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testEmpty
specifier|public
name|void
name|testEmpty
parameter_list|()
throws|throws
name|Exception
block|{
name|CompoundProcessor
name|processor
init|=
operator|new
name|CompoundProcessor
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|processor
operator|.
name|getProcessors
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
name|assertThat
argument_list|(
name|processor
operator|.
name|getOnFailureProcessors
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
name|processor
operator|.
name|execute
argument_list|(
name|ingestDocument
argument_list|)
expr_stmt|;
block|}
DECL|method|testSingleProcessor
specifier|public
name|void
name|testSingleProcessor
parameter_list|()
throws|throws
name|Exception
block|{
name|TestProcessor
name|processor
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
name|compoundProcessor
init|=
operator|new
name|CompoundProcessor
argument_list|(
name|processor
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|compoundProcessor
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
name|compoundProcessor
operator|.
name|getProcessors
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|processor
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|compoundProcessor
operator|.
name|getOnFailureProcessors
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
name|compoundProcessor
operator|.
name|execute
argument_list|(
name|ingestDocument
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|processor
operator|.
name|getInvokedCounter
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testSingleProcessorWithException
specifier|public
name|void
name|testSingleProcessorWithException
parameter_list|()
throws|throws
name|Exception
block|{
name|TestProcessor
name|processor
init|=
operator|new
name|TestProcessor
argument_list|(
name|ingestDocument
lambda|->
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"error"
argument_list|)
throw|;
block|}
argument_list|)
decl_stmt|;
name|CompoundProcessor
name|compoundProcessor
init|=
operator|new
name|CompoundProcessor
argument_list|(
name|processor
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|compoundProcessor
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
name|compoundProcessor
operator|.
name|getProcessors
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|processor
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|compoundProcessor
operator|.
name|getOnFailureProcessors
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
try|try
block|{
name|compoundProcessor
operator|.
name|execute
argument_list|(
name|ingestDocument
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"should throw exception"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ElasticsearchException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|e
operator|.
name|getRootCause
argument_list|()
operator|.
name|getMessage
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"error"
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|assertThat
argument_list|(
name|processor
operator|.
name|getInvokedCounter
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testSingleProcessorWithOnFailureProcessor
specifier|public
name|void
name|testSingleProcessorWithOnFailureProcessor
parameter_list|()
throws|throws
name|Exception
block|{
name|TestProcessor
name|processor1
init|=
operator|new
name|TestProcessor
argument_list|(
literal|"id"
argument_list|,
literal|"first"
argument_list|,
name|ingestDocument
lambda|->
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"error"
argument_list|)
throw|;
block|}
argument_list|)
decl_stmt|;
name|TestProcessor
name|processor2
init|=
operator|new
name|TestProcessor
argument_list|(
name|ingestDocument
lambda|->
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|ingestMetadata
init|=
name|ingestDocument
operator|.
name|getIngestMetadata
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|ingestMetadata
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ingestMetadata
operator|.
name|get
argument_list|(
name|CompoundProcessor
operator|.
name|ON_FAILURE_MESSAGE_FIELD
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"error"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ingestMetadata
operator|.
name|get
argument_list|(
name|CompoundProcessor
operator|.
name|ON_FAILURE_PROCESSOR_TYPE_FIELD
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"first"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ingestMetadata
operator|.
name|get
argument_list|(
name|CompoundProcessor
operator|.
name|ON_FAILURE_PROCESSOR_TAG_FIELD
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"id"
argument_list|)
argument_list|)
expr_stmt|;
block|}
argument_list|)
decl_stmt|;
name|CompoundProcessor
name|compoundProcessor
init|=
operator|new
name|CompoundProcessor
argument_list|(
name|Collections
operator|.
name|singletonList
argument_list|(
name|processor1
argument_list|)
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
name|processor2
argument_list|)
argument_list|)
decl_stmt|;
name|compoundProcessor
operator|.
name|execute
argument_list|(
name|ingestDocument
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|processor1
operator|.
name|getInvokedCounter
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
name|processor2
operator|.
name|getInvokedCounter
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testSingleProcessorWithNestedFailures
specifier|public
name|void
name|testSingleProcessorWithNestedFailures
parameter_list|()
throws|throws
name|Exception
block|{
name|TestProcessor
name|processor
init|=
operator|new
name|TestProcessor
argument_list|(
literal|"id"
argument_list|,
literal|"first"
argument_list|,
name|ingestDocument
lambda|->
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"error"
argument_list|)
throw|;
block|}
argument_list|)
decl_stmt|;
name|TestProcessor
name|processorToFail
init|=
operator|new
name|TestProcessor
argument_list|(
literal|"id2"
argument_list|,
literal|"second"
argument_list|,
name|ingestDocument
lambda|->
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|ingestMetadata
init|=
name|ingestDocument
operator|.
name|getIngestMetadata
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|ingestMetadata
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ingestMetadata
operator|.
name|get
argument_list|(
name|CompoundProcessor
operator|.
name|ON_FAILURE_MESSAGE_FIELD
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"error"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ingestMetadata
operator|.
name|get
argument_list|(
name|CompoundProcessor
operator|.
name|ON_FAILURE_PROCESSOR_TYPE_FIELD
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"first"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ingestMetadata
operator|.
name|get
argument_list|(
name|CompoundProcessor
operator|.
name|ON_FAILURE_PROCESSOR_TAG_FIELD
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"id"
argument_list|)
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"error"
argument_list|)
throw|;
block|}
argument_list|)
decl_stmt|;
name|TestProcessor
name|lastProcessor
init|=
operator|new
name|TestProcessor
argument_list|(
name|ingestDocument
lambda|->
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|ingestMetadata
init|=
name|ingestDocument
operator|.
name|getIngestMetadata
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|ingestMetadata
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ingestMetadata
operator|.
name|get
argument_list|(
name|CompoundProcessor
operator|.
name|ON_FAILURE_MESSAGE_FIELD
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"error"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ingestMetadata
operator|.
name|get
argument_list|(
name|CompoundProcessor
operator|.
name|ON_FAILURE_PROCESSOR_TYPE_FIELD
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"second"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ingestMetadata
operator|.
name|get
argument_list|(
name|CompoundProcessor
operator|.
name|ON_FAILURE_PROCESSOR_TAG_FIELD
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"id2"
argument_list|)
argument_list|)
expr_stmt|;
block|}
argument_list|)
decl_stmt|;
name|CompoundProcessor
name|compoundOnFailProcessor
init|=
operator|new
name|CompoundProcessor
argument_list|(
name|Collections
operator|.
name|singletonList
argument_list|(
name|processorToFail
argument_list|)
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
name|lastProcessor
argument_list|)
argument_list|)
decl_stmt|;
name|CompoundProcessor
name|compoundProcessor
init|=
operator|new
name|CompoundProcessor
argument_list|(
name|Collections
operator|.
name|singletonList
argument_list|(
name|processor
argument_list|)
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
name|compoundOnFailProcessor
argument_list|)
argument_list|)
decl_stmt|;
name|compoundProcessor
operator|.
name|execute
argument_list|(
name|ingestDocument
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|processorToFail
operator|.
name|getInvokedCounter
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
name|lastProcessor
operator|.
name|getInvokedCounter
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testCompoundProcessorExceptionFailWithoutOnFailure
specifier|public
name|void
name|testCompoundProcessorExceptionFailWithoutOnFailure
parameter_list|()
throws|throws
name|Exception
block|{
name|TestProcessor
name|firstProcessor
init|=
operator|new
name|TestProcessor
argument_list|(
literal|"id1"
argument_list|,
literal|"first"
argument_list|,
name|ingestDocument
lambda|->
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"error"
argument_list|)
throw|;
block|}
argument_list|)
decl_stmt|;
name|TestProcessor
name|secondProcessor
init|=
operator|new
name|TestProcessor
argument_list|(
literal|"id3"
argument_list|,
literal|"second"
argument_list|,
name|ingestDocument
lambda|->
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|ingestMetadata
init|=
name|ingestDocument
operator|.
name|getIngestMetadata
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|ingestMetadata
operator|.
name|entrySet
argument_list|()
argument_list|,
name|hasSize
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ingestMetadata
operator|.
name|get
argument_list|(
name|CompoundProcessor
operator|.
name|ON_FAILURE_MESSAGE_FIELD
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"error"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ingestMetadata
operator|.
name|get
argument_list|(
name|CompoundProcessor
operator|.
name|ON_FAILURE_PROCESSOR_TYPE_FIELD
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"first"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ingestMetadata
operator|.
name|get
argument_list|(
name|CompoundProcessor
operator|.
name|ON_FAILURE_PROCESSOR_TAG_FIELD
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"id1"
argument_list|)
argument_list|)
expr_stmt|;
block|}
argument_list|)
decl_stmt|;
name|CompoundProcessor
name|failCompoundProcessor
init|=
operator|new
name|CompoundProcessor
argument_list|(
name|firstProcessor
argument_list|)
decl_stmt|;
name|CompoundProcessor
name|compoundProcessor
init|=
operator|new
name|CompoundProcessor
argument_list|(
name|Collections
operator|.
name|singletonList
argument_list|(
name|failCompoundProcessor
argument_list|)
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
name|secondProcessor
argument_list|)
argument_list|)
decl_stmt|;
name|compoundProcessor
operator|.
name|execute
argument_list|(
name|ingestDocument
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|firstProcessor
operator|.
name|getInvokedCounter
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
name|secondProcessor
operator|.
name|getInvokedCounter
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testCompoundProcessorExceptionFail
specifier|public
name|void
name|testCompoundProcessorExceptionFail
parameter_list|()
throws|throws
name|Exception
block|{
name|TestProcessor
name|firstProcessor
init|=
operator|new
name|TestProcessor
argument_list|(
literal|"id1"
argument_list|,
literal|"first"
argument_list|,
name|ingestDocument
lambda|->
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"error"
argument_list|)
throw|;
block|}
argument_list|)
decl_stmt|;
name|TestProcessor
name|failProcessor
init|=
operator|new
name|TestProcessor
argument_list|(
literal|"tag_fail"
argument_list|,
literal|"fail"
argument_list|,
name|ingestDocument
lambda|->
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"custom error message"
argument_list|)
throw|;
block|}
argument_list|)
decl_stmt|;
name|TestProcessor
name|secondProcessor
init|=
operator|new
name|TestProcessor
argument_list|(
literal|"id3"
argument_list|,
literal|"second"
argument_list|,
name|ingestDocument
lambda|->
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|ingestMetadata
init|=
name|ingestDocument
operator|.
name|getIngestMetadata
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|ingestMetadata
operator|.
name|entrySet
argument_list|()
argument_list|,
name|hasSize
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ingestMetadata
operator|.
name|get
argument_list|(
name|CompoundProcessor
operator|.
name|ON_FAILURE_MESSAGE_FIELD
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"custom error message"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ingestMetadata
operator|.
name|get
argument_list|(
name|CompoundProcessor
operator|.
name|ON_FAILURE_PROCESSOR_TYPE_FIELD
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"fail"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ingestMetadata
operator|.
name|get
argument_list|(
name|CompoundProcessor
operator|.
name|ON_FAILURE_PROCESSOR_TAG_FIELD
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"tag_fail"
argument_list|)
argument_list|)
expr_stmt|;
block|}
argument_list|)
decl_stmt|;
name|CompoundProcessor
name|failCompoundProcessor
init|=
operator|new
name|CompoundProcessor
argument_list|(
name|Collections
operator|.
name|singletonList
argument_list|(
name|firstProcessor
argument_list|)
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
name|failProcessor
argument_list|)
argument_list|)
decl_stmt|;
name|CompoundProcessor
name|compoundProcessor
init|=
operator|new
name|CompoundProcessor
argument_list|(
name|Collections
operator|.
name|singletonList
argument_list|(
name|failCompoundProcessor
argument_list|)
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
name|secondProcessor
argument_list|)
argument_list|)
decl_stmt|;
name|compoundProcessor
operator|.
name|execute
argument_list|(
name|ingestDocument
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|firstProcessor
operator|.
name|getInvokedCounter
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
name|secondProcessor
operator|.
name|getInvokedCounter
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testCompoundProcessorExceptionFailInOnFailure
specifier|public
name|void
name|testCompoundProcessorExceptionFailInOnFailure
parameter_list|()
throws|throws
name|Exception
block|{
name|TestProcessor
name|firstProcessor
init|=
operator|new
name|TestProcessor
argument_list|(
literal|"id1"
argument_list|,
literal|"first"
argument_list|,
name|ingestDocument
lambda|->
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"error"
argument_list|)
throw|;
block|}
argument_list|)
decl_stmt|;
name|TestProcessor
name|failProcessor
init|=
operator|new
name|TestProcessor
argument_list|(
literal|"tag_fail"
argument_list|,
literal|"fail"
argument_list|,
name|ingestDocument
lambda|->
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"custom error message"
argument_list|)
throw|;
block|}
argument_list|)
decl_stmt|;
name|TestProcessor
name|secondProcessor
init|=
operator|new
name|TestProcessor
argument_list|(
literal|"id3"
argument_list|,
literal|"second"
argument_list|,
name|ingestDocument
lambda|->
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|ingestMetadata
init|=
name|ingestDocument
operator|.
name|getIngestMetadata
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|ingestMetadata
operator|.
name|entrySet
argument_list|()
argument_list|,
name|hasSize
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ingestMetadata
operator|.
name|get
argument_list|(
name|CompoundProcessor
operator|.
name|ON_FAILURE_MESSAGE_FIELD
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"custom error message"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ingestMetadata
operator|.
name|get
argument_list|(
name|CompoundProcessor
operator|.
name|ON_FAILURE_PROCESSOR_TYPE_FIELD
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"fail"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ingestMetadata
operator|.
name|get
argument_list|(
name|CompoundProcessor
operator|.
name|ON_FAILURE_PROCESSOR_TAG_FIELD
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"tag_fail"
argument_list|)
argument_list|)
expr_stmt|;
block|}
argument_list|)
decl_stmt|;
name|CompoundProcessor
name|failCompoundProcessor
init|=
operator|new
name|CompoundProcessor
argument_list|(
name|Collections
operator|.
name|singletonList
argument_list|(
name|firstProcessor
argument_list|)
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
operator|new
name|CompoundProcessor
argument_list|(
name|failProcessor
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|CompoundProcessor
name|compoundProcessor
init|=
operator|new
name|CompoundProcessor
argument_list|(
name|Collections
operator|.
name|singletonList
argument_list|(
name|failCompoundProcessor
argument_list|)
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
name|secondProcessor
argument_list|)
argument_list|)
decl_stmt|;
name|compoundProcessor
operator|.
name|execute
argument_list|(
name|ingestDocument
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|firstProcessor
operator|.
name|getInvokedCounter
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
name|secondProcessor
operator|.
name|getInvokedCounter
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

