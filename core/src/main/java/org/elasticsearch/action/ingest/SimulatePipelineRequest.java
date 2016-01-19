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
name|ActionRequestValidationException
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
name|BytesReference
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
name|ingest
operator|.
name|core
operator|.
name|ConfigurationUtils
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
name|PipelineStore
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
name|elasticsearch
operator|.
name|action
operator|.
name|ValidateActions
operator|.
name|addValidationError
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
name|core
operator|.
name|IngestDocument
operator|.
name|MetaData
import|;
end_import

begin_class
DECL|class|SimulatePipelineRequest
specifier|public
class|class
name|SimulatePipelineRequest
extends|extends
name|ActionRequest
argument_list|<
name|SimulatePipelineRequest
argument_list|>
block|{
DECL|field|id
specifier|private
name|String
name|id
decl_stmt|;
DECL|field|verbose
specifier|private
name|boolean
name|verbose
decl_stmt|;
DECL|field|source
specifier|private
name|BytesReference
name|source
decl_stmt|;
annotation|@
name|Override
DECL|method|validate
specifier|public
name|ActionRequestValidationException
name|validate
parameter_list|()
block|{
name|ActionRequestValidationException
name|validationException
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|source
operator|==
literal|null
condition|)
block|{
name|validationException
operator|=
name|addValidationError
argument_list|(
literal|"source is missing"
argument_list|,
name|validationException
argument_list|)
expr_stmt|;
block|}
return|return
name|validationException
return|;
block|}
DECL|method|getId
specifier|public
name|String
name|getId
parameter_list|()
block|{
return|return
name|id
return|;
block|}
DECL|method|setId
specifier|public
name|void
name|setId
parameter_list|(
name|String
name|id
parameter_list|)
block|{
name|this
operator|.
name|id
operator|=
name|id
expr_stmt|;
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
DECL|method|setVerbose
specifier|public
name|void
name|setVerbose
parameter_list|(
name|boolean
name|verbose
parameter_list|)
block|{
name|this
operator|.
name|verbose
operator|=
name|verbose
expr_stmt|;
block|}
DECL|method|getSource
specifier|public
name|BytesReference
name|getSource
parameter_list|()
block|{
return|return
name|source
return|;
block|}
DECL|method|setSource
specifier|public
name|void
name|setSource
parameter_list|(
name|BytesReference
name|source
parameter_list|)
block|{
name|this
operator|.
name|source
operator|=
name|source
expr_stmt|;
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
name|id
operator|=
name|in
operator|.
name|readString
argument_list|()
expr_stmt|;
name|verbose
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
name|source
operator|=
name|in
operator|.
name|readBytesReference
argument_list|()
expr_stmt|;
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
name|writeString
argument_list|(
name|id
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
name|writeBytesReference
argument_list|(
name|source
argument_list|)
expr_stmt|;
block|}
DECL|class|Fields
specifier|public
specifier|static
specifier|final
class|class
name|Fields
block|{
DECL|field|PIPELINE
specifier|static
specifier|final
name|String
name|PIPELINE
init|=
literal|"pipeline"
decl_stmt|;
DECL|field|DOCS
specifier|static
specifier|final
name|String
name|DOCS
init|=
literal|"docs"
decl_stmt|;
DECL|field|SOURCE
specifier|static
specifier|final
name|String
name|SOURCE
init|=
literal|"_source"
decl_stmt|;
block|}
DECL|class|Parsed
specifier|static
class|class
name|Parsed
block|{
DECL|field|documents
specifier|private
specifier|final
name|List
argument_list|<
name|IngestDocument
argument_list|>
name|documents
decl_stmt|;
DECL|field|pipeline
specifier|private
specifier|final
name|Pipeline
name|pipeline
decl_stmt|;
DECL|field|verbose
specifier|private
specifier|final
name|boolean
name|verbose
decl_stmt|;
DECL|method|Parsed
name|Parsed
parameter_list|(
name|Pipeline
name|pipeline
parameter_list|,
name|List
argument_list|<
name|IngestDocument
argument_list|>
name|documents
parameter_list|,
name|boolean
name|verbose
parameter_list|)
block|{
name|this
operator|.
name|pipeline
operator|=
name|pipeline
expr_stmt|;
name|this
operator|.
name|documents
operator|=
name|Collections
operator|.
name|unmodifiableList
argument_list|(
name|documents
argument_list|)
expr_stmt|;
name|this
operator|.
name|verbose
operator|=
name|verbose
expr_stmt|;
block|}
DECL|method|getPipeline
specifier|public
name|Pipeline
name|getPipeline
parameter_list|()
block|{
return|return
name|pipeline
return|;
block|}
DECL|method|getDocuments
specifier|public
name|List
argument_list|<
name|IngestDocument
argument_list|>
name|getDocuments
parameter_list|()
block|{
return|return
name|documents
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
block|}
DECL|field|PIPELINE_FACTORY
specifier|private
specifier|static
specifier|final
name|Pipeline
operator|.
name|Factory
name|PIPELINE_FACTORY
init|=
operator|new
name|Pipeline
operator|.
name|Factory
argument_list|()
decl_stmt|;
DECL|field|SIMULATED_PIPELINE_ID
specifier|static
specifier|final
name|String
name|SIMULATED_PIPELINE_ID
init|=
literal|"_simulate_pipeline"
decl_stmt|;
DECL|method|parseWithPipelineId
specifier|static
name|Parsed
name|parseWithPipelineId
parameter_list|(
name|String
name|pipelineId
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|config
parameter_list|,
name|boolean
name|verbose
parameter_list|,
name|PipelineStore
name|pipelineStore
parameter_list|)
block|{
if|if
condition|(
name|pipelineId
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"param [pipeline] is null"
argument_list|)
throw|;
block|}
name|Pipeline
name|pipeline
init|=
name|pipelineStore
operator|.
name|get
argument_list|(
name|pipelineId
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|IngestDocument
argument_list|>
name|ingestDocumentList
init|=
name|parseDocs
argument_list|(
name|config
argument_list|)
decl_stmt|;
return|return
operator|new
name|Parsed
argument_list|(
name|pipeline
argument_list|,
name|ingestDocumentList
argument_list|,
name|verbose
argument_list|)
return|;
block|}
DECL|method|parse
specifier|static
name|Parsed
name|parse
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|config
parameter_list|,
name|boolean
name|verbose
parameter_list|,
name|PipelineStore
name|pipelineStore
parameter_list|)
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
name|ConfigurationUtils
operator|.
name|readMap
argument_list|(
name|config
argument_list|,
name|Fields
operator|.
name|PIPELINE
argument_list|)
decl_stmt|;
name|Pipeline
name|pipeline
init|=
name|PIPELINE_FACTORY
operator|.
name|create
argument_list|(
name|SIMULATED_PIPELINE_ID
argument_list|,
name|pipelineConfig
argument_list|,
name|pipelineStore
operator|.
name|getProcessorFactoryRegistry
argument_list|()
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|IngestDocument
argument_list|>
name|ingestDocumentList
init|=
name|parseDocs
argument_list|(
name|config
argument_list|)
decl_stmt|;
return|return
operator|new
name|Parsed
argument_list|(
name|pipeline
argument_list|,
name|ingestDocumentList
argument_list|,
name|verbose
argument_list|)
return|;
block|}
DECL|method|parseDocs
specifier|private
specifier|static
name|List
argument_list|<
name|IngestDocument
argument_list|>
name|parseDocs
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|config
parameter_list|)
block|{
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
name|ConfigurationUtils
operator|.
name|readList
argument_list|(
name|config
argument_list|,
name|Fields
operator|.
name|DOCS
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|IngestDocument
argument_list|>
name|ingestDocumentList
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|dataMap
range|:
name|docs
control|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|document
init|=
name|ConfigurationUtils
operator|.
name|readMap
argument_list|(
name|dataMap
argument_list|,
name|Fields
operator|.
name|SOURCE
argument_list|)
decl_stmt|;
name|IngestDocument
name|ingestDocument
init|=
operator|new
name|IngestDocument
argument_list|(
name|ConfigurationUtils
operator|.
name|readStringProperty
argument_list|(
name|dataMap
argument_list|,
name|MetaData
operator|.
name|INDEX
operator|.
name|getFieldName
argument_list|()
argument_list|,
literal|"_index"
argument_list|)
argument_list|,
name|ConfigurationUtils
operator|.
name|readStringProperty
argument_list|(
name|dataMap
argument_list|,
name|MetaData
operator|.
name|TYPE
operator|.
name|getFieldName
argument_list|()
argument_list|,
literal|"_type"
argument_list|)
argument_list|,
name|ConfigurationUtils
operator|.
name|readStringProperty
argument_list|(
name|dataMap
argument_list|,
name|MetaData
operator|.
name|ID
operator|.
name|getFieldName
argument_list|()
argument_list|,
literal|"_id"
argument_list|)
argument_list|,
name|ConfigurationUtils
operator|.
name|readOptionalStringProperty
argument_list|(
name|dataMap
argument_list|,
name|MetaData
operator|.
name|ROUTING
operator|.
name|getFieldName
argument_list|()
argument_list|)
argument_list|,
name|ConfigurationUtils
operator|.
name|readOptionalStringProperty
argument_list|(
name|dataMap
argument_list|,
name|MetaData
operator|.
name|PARENT
operator|.
name|getFieldName
argument_list|()
argument_list|)
argument_list|,
name|ConfigurationUtils
operator|.
name|readOptionalStringProperty
argument_list|(
name|dataMap
argument_list|,
name|MetaData
operator|.
name|TIMESTAMP
operator|.
name|getFieldName
argument_list|()
argument_list|)
argument_list|,
name|ConfigurationUtils
operator|.
name|readOptionalStringProperty
argument_list|(
name|dataMap
argument_list|,
name|MetaData
operator|.
name|TTL
operator|.
name|getFieldName
argument_list|()
argument_list|)
argument_list|,
name|document
argument_list|)
decl_stmt|;
name|ingestDocumentList
operator|.
name|add
argument_list|(
name|ingestDocument
argument_list|)
expr_stmt|;
block|}
return|return
name|ingestDocumentList
return|;
block|}
block|}
end_class

end_unit

