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
name|ElasticsearchException
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
name|io
operator|.
name|stream
operator|.
name|Writeable
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
name|ToXContent
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
name|IngestDocument
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
name|Collections
import|;
end_import

begin_class
DECL|class|SimulateProcessorResult
specifier|public
class|class
name|SimulateProcessorResult
implements|implements
name|Writeable
argument_list|<
name|SimulateProcessorResult
argument_list|>
implements|,
name|ToXContent
block|{
DECL|field|PROTOTYPE
specifier|private
specifier|static
specifier|final
name|SimulateProcessorResult
name|PROTOTYPE
init|=
operator|new
name|SimulateProcessorResult
argument_list|(
literal|"_na"
argument_list|,
operator|new
name|WriteableIngestDocument
argument_list|(
operator|new
name|IngestDocument
argument_list|(
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
DECL|field|processorId
specifier|private
name|String
name|processorId
decl_stmt|;
DECL|field|ingestDocument
specifier|private
name|WriteableIngestDocument
name|ingestDocument
decl_stmt|;
DECL|field|failure
specifier|private
name|Exception
name|failure
decl_stmt|;
DECL|method|SimulateProcessorResult
specifier|public
name|SimulateProcessorResult
parameter_list|(
name|String
name|processorId
parameter_list|,
name|IngestDocument
name|ingestDocument
parameter_list|)
block|{
name|this
operator|.
name|processorId
operator|=
name|processorId
expr_stmt|;
name|this
operator|.
name|ingestDocument
operator|=
operator|new
name|WriteableIngestDocument
argument_list|(
name|ingestDocument
argument_list|)
expr_stmt|;
block|}
DECL|method|SimulateProcessorResult
specifier|private
name|SimulateProcessorResult
parameter_list|(
name|String
name|processorId
parameter_list|,
name|WriteableIngestDocument
name|ingestDocument
parameter_list|)
block|{
name|this
operator|.
name|processorId
operator|=
name|processorId
expr_stmt|;
name|this
operator|.
name|ingestDocument
operator|=
name|ingestDocument
expr_stmt|;
block|}
DECL|method|SimulateProcessorResult
specifier|public
name|SimulateProcessorResult
parameter_list|(
name|String
name|processorId
parameter_list|,
name|Exception
name|failure
parameter_list|)
block|{
name|this
operator|.
name|processorId
operator|=
name|processorId
expr_stmt|;
name|this
operator|.
name|failure
operator|=
name|failure
expr_stmt|;
block|}
DECL|method|getIngestDocument
specifier|public
name|IngestDocument
name|getIngestDocument
parameter_list|()
block|{
if|if
condition|(
name|ingestDocument
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|ingestDocument
operator|.
name|getIngestDocument
argument_list|()
return|;
block|}
DECL|method|getProcessorId
specifier|public
name|String
name|getProcessorId
parameter_list|()
block|{
return|return
name|processorId
return|;
block|}
DECL|method|getFailure
specifier|public
name|Exception
name|getFailure
parameter_list|()
block|{
return|return
name|failure
return|;
block|}
DECL|method|readSimulateProcessorResultFrom
specifier|public
specifier|static
name|SimulateProcessorResult
name|readSimulateProcessorResultFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|PROTOTYPE
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|readFrom
specifier|public
name|SimulateProcessorResult
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|processorId
init|=
name|in
operator|.
name|readString
argument_list|()
decl_stmt|;
if|if
condition|(
name|in
operator|.
name|readBoolean
argument_list|()
condition|)
block|{
name|Exception
name|exception
init|=
name|in
operator|.
name|readThrowable
argument_list|()
decl_stmt|;
return|return
operator|new
name|SimulateProcessorResult
argument_list|(
name|processorId
argument_list|,
name|exception
argument_list|)
return|;
block|}
return|return
operator|new
name|SimulateProcessorResult
argument_list|(
name|processorId
argument_list|,
name|WriteableIngestDocument
operator|.
name|readWriteableIngestDocumentFrom
argument_list|(
name|in
argument_list|)
argument_list|)
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
name|out
operator|.
name|writeString
argument_list|(
name|processorId
argument_list|)
expr_stmt|;
if|if
condition|(
name|failure
operator|==
literal|null
condition|)
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|ingestDocument
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
name|writeBoolean
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeThrowable
argument_list|(
name|failure
argument_list|)
expr_stmt|;
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
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|PROCESSOR_ID
argument_list|,
name|processorId
argument_list|)
expr_stmt|;
if|if
condition|(
name|failure
operator|==
literal|null
condition|)
block|{
name|ingestDocument
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
name|ElasticsearchException
operator|.
name|renderThrowable
argument_list|(
name|builder
argument_list|,
name|params
argument_list|,
name|failure
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
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
DECL|field|PROCESSOR_ID
specifier|static
specifier|final
name|XContentBuilderString
name|PROCESSOR_ID
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"processor_id"
argument_list|)
decl_stmt|;
block|}
block|}
end_class

end_unit

