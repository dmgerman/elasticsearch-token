begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_comment
comment|//TODO(simonw): can all these classes go into org.elasticsearch.ingest?
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
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|Collectors
import|;
end_import

begin_comment
comment|/**  * A Processor that executes a list of other "processors". It executes a separate list of  * "onFailureProcessors" when any of the processors throw an {@link Exception}.  */
end_comment

begin_class
DECL|class|CompoundProcessor
specifier|public
class|class
name|CompoundProcessor
implements|implements
name|Processor
block|{
DECL|field|ON_FAILURE_MESSAGE_FIELD
specifier|public
specifier|static
specifier|final
name|String
name|ON_FAILURE_MESSAGE_FIELD
init|=
literal|"on_failure_message"
decl_stmt|;
DECL|field|ON_FAILURE_PROCESSOR_TYPE_FIELD
specifier|public
specifier|static
specifier|final
name|String
name|ON_FAILURE_PROCESSOR_TYPE_FIELD
init|=
literal|"on_failure_processor_type"
decl_stmt|;
DECL|field|ON_FAILURE_PROCESSOR_TAG_FIELD
specifier|public
specifier|static
specifier|final
name|String
name|ON_FAILURE_PROCESSOR_TAG_FIELD
init|=
literal|"on_failure_processor_tag"
decl_stmt|;
DECL|field|ignoreFailure
specifier|private
specifier|final
name|boolean
name|ignoreFailure
decl_stmt|;
DECL|field|processors
specifier|private
specifier|final
name|List
argument_list|<
name|Processor
argument_list|>
name|processors
decl_stmt|;
DECL|field|onFailureProcessors
specifier|private
specifier|final
name|List
argument_list|<
name|Processor
argument_list|>
name|onFailureProcessors
decl_stmt|;
DECL|method|CompoundProcessor
specifier|public
name|CompoundProcessor
parameter_list|(
name|Processor
modifier|...
name|processor
parameter_list|)
block|{
name|this
argument_list|(
literal|false
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|processor
argument_list|)
argument_list|,
name|Collections
operator|.
name|emptyList
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|CompoundProcessor
specifier|public
name|CompoundProcessor
parameter_list|(
name|boolean
name|ignoreFailure
parameter_list|,
name|List
argument_list|<
name|Processor
argument_list|>
name|processors
parameter_list|,
name|List
argument_list|<
name|Processor
argument_list|>
name|onFailureProcessors
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|ignoreFailure
operator|=
name|ignoreFailure
expr_stmt|;
name|this
operator|.
name|processors
operator|=
name|processors
expr_stmt|;
name|this
operator|.
name|onFailureProcessors
operator|=
name|onFailureProcessors
expr_stmt|;
block|}
DECL|method|isIgnoreFailure
specifier|public
name|boolean
name|isIgnoreFailure
parameter_list|()
block|{
return|return
name|ignoreFailure
return|;
block|}
DECL|method|getOnFailureProcessors
specifier|public
name|List
argument_list|<
name|Processor
argument_list|>
name|getOnFailureProcessors
parameter_list|()
block|{
return|return
name|onFailureProcessors
return|;
block|}
DECL|method|getProcessors
specifier|public
name|List
argument_list|<
name|Processor
argument_list|>
name|getProcessors
parameter_list|()
block|{
return|return
name|processors
return|;
block|}
DECL|method|flattenProcessors
specifier|public
name|List
argument_list|<
name|Processor
argument_list|>
name|flattenProcessors
parameter_list|()
block|{
name|List
argument_list|<
name|Processor
argument_list|>
name|allProcessors
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|flattenProcessors
argument_list|(
name|processors
argument_list|)
argument_list|)
decl_stmt|;
name|allProcessors
operator|.
name|addAll
argument_list|(
name|flattenProcessors
argument_list|(
name|onFailureProcessors
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|allProcessors
return|;
block|}
DECL|method|flattenProcessors
specifier|private
specifier|static
name|List
argument_list|<
name|Processor
argument_list|>
name|flattenProcessors
parameter_list|(
name|List
argument_list|<
name|Processor
argument_list|>
name|processors
parameter_list|)
block|{
name|List
argument_list|<
name|Processor
argument_list|>
name|flattened
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|Processor
name|processor
range|:
name|processors
control|)
block|{
if|if
condition|(
name|processor
operator|instanceof
name|CompoundProcessor
condition|)
block|{
name|flattened
operator|.
name|addAll
argument_list|(
operator|(
operator|(
name|CompoundProcessor
operator|)
name|processor
operator|)
operator|.
name|flattenProcessors
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|flattened
operator|.
name|add
argument_list|(
name|processor
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|flattened
return|;
block|}
annotation|@
name|Override
DECL|method|getType
specifier|public
name|String
name|getType
parameter_list|()
block|{
return|return
literal|"compound"
return|;
block|}
annotation|@
name|Override
DECL|method|getTag
specifier|public
name|String
name|getTag
parameter_list|()
block|{
return|return
literal|"CompoundProcessor-"
operator|+
name|flattenProcessors
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|Processor
operator|::
name|getTag
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|joining
argument_list|(
literal|"-"
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|execute
specifier|public
name|void
name|execute
parameter_list|(
name|IngestDocument
name|ingestDocument
parameter_list|)
throws|throws
name|Exception
block|{
for|for
control|(
name|Processor
name|processor
range|:
name|processors
control|)
block|{
try|try
block|{
name|processor
operator|.
name|execute
argument_list|(
name|ingestDocument
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
if|if
condition|(
name|ignoreFailure
condition|)
block|{
continue|continue;
block|}
name|ElasticsearchException
name|compoundProcessorException
init|=
name|newCompoundProcessorException
argument_list|(
name|e
argument_list|,
name|processor
operator|.
name|getType
argument_list|()
argument_list|,
name|processor
operator|.
name|getTag
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|onFailureProcessors
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
name|compoundProcessorException
throw|;
block|}
else|else
block|{
name|executeOnFailure
argument_list|(
name|ingestDocument
argument_list|,
name|compoundProcessorException
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
DECL|method|executeOnFailure
name|void
name|executeOnFailure
parameter_list|(
name|IngestDocument
name|ingestDocument
parameter_list|,
name|ElasticsearchException
name|exception
parameter_list|)
throws|throws
name|Exception
block|{
try|try
block|{
name|putFailureMetadata
argument_list|(
name|ingestDocument
argument_list|,
name|exception
argument_list|)
expr_stmt|;
for|for
control|(
name|Processor
name|processor
range|:
name|onFailureProcessors
control|)
block|{
try|try
block|{
name|processor
operator|.
name|execute
argument_list|(
name|ingestDocument
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
name|newCompoundProcessorException
argument_list|(
name|e
argument_list|,
name|processor
operator|.
name|getType
argument_list|()
argument_list|,
name|processor
operator|.
name|getTag
argument_list|()
argument_list|)
throw|;
block|}
block|}
block|}
finally|finally
block|{
name|removeFailureMetadata
argument_list|(
name|ingestDocument
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|putFailureMetadata
specifier|private
name|void
name|putFailureMetadata
parameter_list|(
name|IngestDocument
name|ingestDocument
parameter_list|,
name|ElasticsearchException
name|cause
parameter_list|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|processorTypeHeader
init|=
name|cause
operator|.
name|getHeader
argument_list|(
literal|"processor_type"
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|processorTagHeader
init|=
name|cause
operator|.
name|getHeader
argument_list|(
literal|"processor_tag"
argument_list|)
decl_stmt|;
name|String
name|failedProcessorType
init|=
operator|(
name|processorTypeHeader
operator|!=
literal|null
operator|)
condition|?
name|processorTypeHeader
operator|.
name|get
argument_list|(
literal|0
argument_list|)
else|:
literal|null
decl_stmt|;
name|String
name|failedProcessorTag
init|=
operator|(
name|processorTagHeader
operator|!=
literal|null
operator|)
condition|?
name|processorTagHeader
operator|.
name|get
argument_list|(
literal|0
argument_list|)
else|:
literal|null
decl_stmt|;
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
name|ingestMetadata
operator|.
name|put
argument_list|(
name|ON_FAILURE_MESSAGE_FIELD
argument_list|,
name|cause
operator|.
name|getRootCause
argument_list|()
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|ingestMetadata
operator|.
name|put
argument_list|(
name|ON_FAILURE_PROCESSOR_TYPE_FIELD
argument_list|,
name|failedProcessorType
argument_list|)
expr_stmt|;
name|ingestMetadata
operator|.
name|put
argument_list|(
name|ON_FAILURE_PROCESSOR_TAG_FIELD
argument_list|,
name|failedProcessorTag
argument_list|)
expr_stmt|;
block|}
DECL|method|removeFailureMetadata
specifier|private
name|void
name|removeFailureMetadata
parameter_list|(
name|IngestDocument
name|ingestDocument
parameter_list|)
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
name|ingestMetadata
operator|.
name|remove
argument_list|(
name|ON_FAILURE_MESSAGE_FIELD
argument_list|)
expr_stmt|;
name|ingestMetadata
operator|.
name|remove
argument_list|(
name|ON_FAILURE_PROCESSOR_TYPE_FIELD
argument_list|)
expr_stmt|;
name|ingestMetadata
operator|.
name|remove
argument_list|(
name|ON_FAILURE_PROCESSOR_TAG_FIELD
argument_list|)
expr_stmt|;
block|}
DECL|method|newCompoundProcessorException
specifier|private
name|ElasticsearchException
name|newCompoundProcessorException
parameter_list|(
name|Exception
name|e
parameter_list|,
name|String
name|processorType
parameter_list|,
name|String
name|processorTag
parameter_list|)
block|{
if|if
condition|(
name|e
operator|instanceof
name|ElasticsearchException
operator|&&
operator|(
operator|(
name|ElasticsearchException
operator|)
name|e
operator|)
operator|.
name|getHeader
argument_list|(
literal|"processor_type"
argument_list|)
operator|!=
literal|null
condition|)
block|{
return|return
operator|(
name|ElasticsearchException
operator|)
name|e
return|;
block|}
name|ElasticsearchException
name|exception
init|=
operator|new
name|ElasticsearchException
argument_list|(
operator|new
name|IllegalArgumentException
argument_list|(
name|e
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|processorType
operator|!=
literal|null
condition|)
block|{
name|exception
operator|.
name|addHeader
argument_list|(
literal|"processor_type"
argument_list|,
name|processorType
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|processorTag
operator|!=
literal|null
condition|)
block|{
name|exception
operator|.
name|addHeader
argument_list|(
literal|"processor_tag"
argument_list|,
name|processorTag
argument_list|)
expr_stmt|;
block|}
return|return
name|exception
return|;
block|}
block|}
end_class

end_unit

