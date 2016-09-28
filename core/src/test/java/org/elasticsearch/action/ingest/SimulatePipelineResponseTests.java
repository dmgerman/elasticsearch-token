begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.ingest
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
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
name|common
operator|.
name|io
operator|.
name|stream
operator|.
name|BytesStreamOutput
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
name|io
operator|.
name|stream
operator|.
name|StreamInput
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
name|RandomDocumentPicks
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
name|Iterator
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
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|IngestDocumentMatcher
operator|.
name|assertIngestDocument
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
name|CoreMatchers
operator|.
name|instanceOf
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
name|nullValue
import|;
end_import

begin_class
DECL|class|SimulatePipelineResponseTests
specifier|public
class|class
name|SimulatePipelineResponseTests
extends|extends
name|ESTestCase
block|{
DECL|method|testSerialization
specifier|public
name|void
name|testSerialization
parameter_list|()
throws|throws
name|IOException
block|{
name|boolean
name|isVerbose
init|=
name|randomBoolean
argument_list|()
decl_stmt|;
name|String
name|id
init|=
name|randomBoolean
argument_list|()
condition|?
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
else|:
literal|null
decl_stmt|;
name|int
name|numResults
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|SimulateDocumentResult
argument_list|>
name|results
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|numResults
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
name|numResults
condition|;
name|i
operator|++
control|)
block|{
name|boolean
name|isFailure
init|=
name|randomBoolean
argument_list|()
decl_stmt|;
name|IngestDocument
name|ingestDocument
init|=
name|RandomDocumentPicks
operator|.
name|randomIngestDocument
argument_list|(
name|random
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|isVerbose
condition|)
block|{
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
name|List
argument_list|<
name|SimulateProcessorResult
argument_list|>
name|processorResults
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|numProcessors
argument_list|)
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
name|numProcessors
condition|;
name|j
operator|++
control|)
block|{
name|String
name|processorTag
init|=
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|SimulateProcessorResult
name|processorResult
decl_stmt|;
if|if
condition|(
name|isFailure
condition|)
block|{
name|processorResult
operator|=
operator|new
name|SimulateProcessorResult
argument_list|(
name|processorTag
argument_list|,
operator|new
name|IllegalArgumentException
argument_list|(
literal|"test"
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|processorResult
operator|=
operator|new
name|SimulateProcessorResult
argument_list|(
name|processorTag
argument_list|,
name|ingestDocument
argument_list|)
expr_stmt|;
block|}
name|processorResults
operator|.
name|add
argument_list|(
name|processorResult
argument_list|)
expr_stmt|;
block|}
name|results
operator|.
name|add
argument_list|(
operator|new
name|SimulateDocumentVerboseResult
argument_list|(
name|processorResults
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|results
operator|.
name|add
argument_list|(
operator|new
name|SimulateDocumentBaseResult
argument_list|(
name|ingestDocument
argument_list|)
argument_list|)
expr_stmt|;
name|SimulateDocumentBaseResult
name|simulateDocumentBaseResult
decl_stmt|;
if|if
condition|(
name|isFailure
condition|)
block|{
name|simulateDocumentBaseResult
operator|=
operator|new
name|SimulateDocumentBaseResult
argument_list|(
operator|new
name|IllegalArgumentException
argument_list|(
literal|"test"
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|simulateDocumentBaseResult
operator|=
operator|new
name|SimulateDocumentBaseResult
argument_list|(
name|ingestDocument
argument_list|)
expr_stmt|;
block|}
name|results
operator|.
name|add
argument_list|(
name|simulateDocumentBaseResult
argument_list|)
expr_stmt|;
block|}
block|}
name|SimulatePipelineResponse
name|response
init|=
operator|new
name|SimulatePipelineResponse
argument_list|(
name|id
argument_list|,
name|isVerbose
argument_list|,
name|results
argument_list|)
decl_stmt|;
name|BytesStreamOutput
name|out
init|=
operator|new
name|BytesStreamOutput
argument_list|()
decl_stmt|;
name|response
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|StreamInput
name|streamInput
init|=
name|out
operator|.
name|bytes
argument_list|()
operator|.
name|streamInput
argument_list|()
decl_stmt|;
name|SimulatePipelineResponse
name|otherResponse
init|=
operator|new
name|SimulatePipelineResponse
argument_list|()
decl_stmt|;
name|otherResponse
operator|.
name|readFrom
argument_list|(
name|streamInput
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|otherResponse
operator|.
name|getPipelineId
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|response
operator|.
name|getPipelineId
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|otherResponse
operator|.
name|getResults
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|response
operator|.
name|getResults
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|Iterator
argument_list|<
name|SimulateDocumentResult
argument_list|>
name|expectedResultIterator
init|=
name|response
operator|.
name|getResults
argument_list|()
operator|.
name|iterator
argument_list|()
decl_stmt|;
for|for
control|(
name|SimulateDocumentResult
name|result
range|:
name|otherResponse
operator|.
name|getResults
argument_list|()
control|)
block|{
if|if
condition|(
name|isVerbose
condition|)
block|{
name|SimulateDocumentVerboseResult
name|expectedSimulateDocumentVerboseResult
init|=
operator|(
name|SimulateDocumentVerboseResult
operator|)
name|expectedResultIterator
operator|.
name|next
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|result
argument_list|,
name|instanceOf
argument_list|(
name|SimulateDocumentVerboseResult
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|SimulateDocumentVerboseResult
name|simulateDocumentVerboseResult
init|=
operator|(
name|SimulateDocumentVerboseResult
operator|)
name|result
decl_stmt|;
name|assertThat
argument_list|(
name|simulateDocumentVerboseResult
operator|.
name|getProcessorResults
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|expectedSimulateDocumentVerboseResult
operator|.
name|getProcessorResults
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|Iterator
argument_list|<
name|SimulateProcessorResult
argument_list|>
name|expectedProcessorResultIterator
init|=
name|expectedSimulateDocumentVerboseResult
operator|.
name|getProcessorResults
argument_list|()
operator|.
name|iterator
argument_list|()
decl_stmt|;
for|for
control|(
name|SimulateProcessorResult
name|simulateProcessorResult
range|:
name|simulateDocumentVerboseResult
operator|.
name|getProcessorResults
argument_list|()
control|)
block|{
name|SimulateProcessorResult
name|expectedProcessorResult
init|=
name|expectedProcessorResultIterator
operator|.
name|next
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|simulateProcessorResult
operator|.
name|getProcessorTag
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|expectedProcessorResult
operator|.
name|getProcessorTag
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|simulateProcessorResult
operator|.
name|getIngestDocument
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|assertIngestDocument
argument_list|(
name|simulateProcessorResult
operator|.
name|getIngestDocument
argument_list|()
argument_list|,
name|expectedProcessorResult
operator|.
name|getIngestDocument
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|expectedProcessorResult
operator|.
name|getFailure
argument_list|()
operator|==
literal|null
condition|)
block|{
name|assertThat
argument_list|(
name|simulateProcessorResult
operator|.
name|getFailure
argument_list|()
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertThat
argument_list|(
name|simulateProcessorResult
operator|.
name|getFailure
argument_list|()
argument_list|,
name|instanceOf
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|IllegalArgumentException
name|e
init|=
operator|(
name|IllegalArgumentException
operator|)
name|simulateProcessorResult
operator|.
name|getFailure
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"test"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
name|SimulateDocumentBaseResult
name|expectedSimulateDocumentBaseResult
init|=
operator|(
name|SimulateDocumentBaseResult
operator|)
name|expectedResultIterator
operator|.
name|next
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|result
argument_list|,
name|instanceOf
argument_list|(
name|SimulateDocumentBaseResult
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|SimulateDocumentBaseResult
name|simulateDocumentBaseResult
init|=
operator|(
name|SimulateDocumentBaseResult
operator|)
name|result
decl_stmt|;
if|if
condition|(
name|simulateDocumentBaseResult
operator|.
name|getIngestDocument
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|assertIngestDocument
argument_list|(
name|simulateDocumentBaseResult
operator|.
name|getIngestDocument
argument_list|()
argument_list|,
name|expectedSimulateDocumentBaseResult
operator|.
name|getIngestDocument
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|expectedSimulateDocumentBaseResult
operator|.
name|getFailure
argument_list|()
operator|==
literal|null
condition|)
block|{
name|assertThat
argument_list|(
name|simulateDocumentBaseResult
operator|.
name|getFailure
argument_list|()
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertThat
argument_list|(
name|simulateDocumentBaseResult
operator|.
name|getFailure
argument_list|()
argument_list|,
name|instanceOf
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|IllegalArgumentException
name|e
init|=
operator|(
name|IllegalArgumentException
operator|)
name|simulateDocumentBaseResult
operator|.
name|getFailure
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"test"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
end_class

end_unit

