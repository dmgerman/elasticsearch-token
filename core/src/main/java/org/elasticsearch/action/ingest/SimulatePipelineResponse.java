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
name|common
operator|.
name|io
operator|.
name|stream
operator|.
name|StreamOutput
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
name|StatusToXContent
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
name|XContentBuilderString
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
name|PipelineFactoryError
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|RestStatus
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
name|Collections
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

begin_class
DECL|class|SimulatePipelineResponse
specifier|public
class|class
name|SimulatePipelineResponse
extends|extends
name|ActionResponse
implements|implements
name|StatusToXContent
block|{
DECL|field|pipelineId
specifier|private
name|String
name|pipelineId
decl_stmt|;
DECL|field|verbose
specifier|private
name|boolean
name|verbose
decl_stmt|;
DECL|field|results
specifier|private
name|List
argument_list|<
name|SimulateDocumentResult
argument_list|>
name|results
decl_stmt|;
DECL|field|error
specifier|private
name|PipelineFactoryError
name|error
decl_stmt|;
DECL|method|SimulatePipelineResponse
specifier|public
name|SimulatePipelineResponse
parameter_list|()
block|{      }
DECL|method|SimulatePipelineResponse
specifier|public
name|SimulatePipelineResponse
parameter_list|(
name|PipelineFactoryError
name|error
parameter_list|)
block|{
name|this
operator|.
name|error
operator|=
name|error
expr_stmt|;
block|}
DECL|method|SimulatePipelineResponse
specifier|public
name|SimulatePipelineResponse
parameter_list|(
name|String
name|pipelineId
parameter_list|,
name|boolean
name|verbose
parameter_list|,
name|List
argument_list|<
name|SimulateDocumentResult
argument_list|>
name|responses
parameter_list|)
block|{
name|this
operator|.
name|pipelineId
operator|=
name|pipelineId
expr_stmt|;
name|this
operator|.
name|verbose
operator|=
name|verbose
expr_stmt|;
name|this
operator|.
name|results
operator|=
name|Collections
operator|.
name|unmodifiableList
argument_list|(
name|responses
argument_list|)
expr_stmt|;
block|}
DECL|method|getPipelineId
specifier|public
name|String
name|getPipelineId
parameter_list|()
block|{
return|return
name|pipelineId
return|;
block|}
DECL|method|getResults
specifier|public
name|List
argument_list|<
name|SimulateDocumentResult
argument_list|>
name|getResults
parameter_list|()
block|{
return|return
name|results
return|;
block|}
DECL|method|isVerbose
specifier|public
name|boolean
name|isVerbose
parameter_list|()
block|{
return|return
name|verbose
return|;
block|}
DECL|method|isError
specifier|public
name|boolean
name|isError
parameter_list|()
block|{
return|return
name|error
operator|!=
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|status
specifier|public
name|RestStatus
name|status
parameter_list|()
block|{
if|if
condition|(
name|isError
argument_list|()
condition|)
block|{
return|return
name|RestStatus
operator|.
name|BAD_REQUEST
return|;
block|}
return|return
name|RestStatus
operator|.
name|OK
return|;
block|}
annotation|@
name|Override
DECL|method|writeTo
specifier|public
name|void
name|writeTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|isError
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|isError
argument_list|()
condition|)
block|{
name|error
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|writeString
argument_list|(
name|pipelineId
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|verbose
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|results
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|SimulateDocumentResult
name|response
range|:
name|results
control|)
block|{
name|response
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
DECL|method|readFrom
specifier|public
name|void
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|boolean
name|isError
init|=
name|in
operator|.
name|readBoolean
argument_list|()
decl_stmt|;
if|if
condition|(
name|isError
condition|)
block|{
name|error
operator|=
operator|new
name|PipelineFactoryError
argument_list|()
expr_stmt|;
name|error
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|pipelineId
operator|=
name|in
operator|.
name|readString
argument_list|()
expr_stmt|;
name|boolean
name|verbose
init|=
name|in
operator|.
name|readBoolean
argument_list|()
decl_stmt|;
name|int
name|responsesLength
init|=
name|in
operator|.
name|readVInt
argument_list|()
decl_stmt|;
name|results
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
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
name|responsesLength
condition|;
name|i
operator|++
control|)
block|{
name|SimulateDocumentResult
argument_list|<
name|?
argument_list|>
name|simulateDocumentResult
decl_stmt|;
if|if
condition|(
name|verbose
condition|)
block|{
name|simulateDocumentResult
operator|=
name|SimulateDocumentVerboseResult
operator|.
name|readSimulateDocumentVerboseResultFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|simulateDocumentResult
operator|=
name|SimulateDocumentBaseResult
operator|.
name|readSimulateDocumentSimpleResult
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
name|results
operator|.
name|add
argument_list|(
name|simulateDocumentResult
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
DECL|method|toXContent
specifier|public
name|XContentBuilder
name|toXContent
parameter_list|(
name|XContentBuilder
name|builder
parameter_list|,
name|Params
name|params
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|isError
argument_list|()
condition|)
block|{
name|error
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|builder
operator|.
name|startArray
argument_list|(
name|Fields
operator|.
name|DOCUMENTS
argument_list|)
expr_stmt|;
for|for
control|(
name|SimulateDocumentResult
name|response
range|:
name|results
control|)
block|{
name|response
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endArray
argument_list|()
expr_stmt|;
block|}
return|return
name|builder
return|;
block|}
DECL|class|Fields
specifier|static
specifier|final
class|class
name|Fields
block|{
DECL|field|DOCUMENTS
specifier|static
specifier|final
name|XContentBuilderString
name|DOCUMENTS
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"docs"
argument_list|)
decl_stmt|;
block|}
block|}
end_class

end_unit

