begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.ingest.processor
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|processor
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
name|ingest
operator|.
name|SimulateProcessorResult
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
name|CompoundProcessor
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
name|Processor
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
name|List
import|;
end_import

begin_comment
comment|/**  * Processor to be used within Simulate API to keep track of processors executed in pipeline.  */
end_comment

begin_class
DECL|class|TrackingResultProcessor
specifier|public
specifier|final
class|class
name|TrackingResultProcessor
implements|implements
name|Processor
block|{
DECL|field|actualProcessor
specifier|private
specifier|final
name|Processor
name|actualProcessor
decl_stmt|;
DECL|field|processorResultList
specifier|private
specifier|final
name|List
argument_list|<
name|SimulateProcessorResult
argument_list|>
name|processorResultList
decl_stmt|;
DECL|method|TrackingResultProcessor
specifier|public
name|TrackingResultProcessor
parameter_list|(
name|Processor
name|actualProcessor
parameter_list|,
name|List
argument_list|<
name|SimulateProcessorResult
argument_list|>
name|processorResultList
parameter_list|)
block|{
name|this
operator|.
name|processorResultList
operator|=
name|processorResultList
expr_stmt|;
if|if
condition|(
name|actualProcessor
operator|instanceof
name|CompoundProcessor
condition|)
block|{
name|CompoundProcessor
name|trackedCompoundProcessor
init|=
name|decorate
argument_list|(
operator|(
name|CompoundProcessor
operator|)
name|actualProcessor
argument_list|,
name|processorResultList
argument_list|)
decl_stmt|;
name|this
operator|.
name|actualProcessor
operator|=
name|trackedCompoundProcessor
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|actualProcessor
operator|=
name|actualProcessor
expr_stmt|;
block|}
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
try|try
block|{
name|actualProcessor
operator|.
name|execute
argument_list|(
name|ingestDocument
argument_list|)
expr_stmt|;
name|processorResultList
operator|.
name|add
argument_list|(
operator|new
name|SimulateProcessorResult
argument_list|(
name|actualProcessor
operator|.
name|getTag
argument_list|()
argument_list|,
operator|new
name|IngestDocument
argument_list|(
name|ingestDocument
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|processorResultList
operator|.
name|add
argument_list|(
operator|new
name|SimulateProcessorResult
argument_list|(
name|actualProcessor
operator|.
name|getTag
argument_list|()
argument_list|,
name|e
argument_list|)
argument_list|)
expr_stmt|;
throw|throw
name|e
throw|;
block|}
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
name|actualProcessor
operator|.
name|getType
argument_list|()
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
name|actualProcessor
operator|.
name|getTag
argument_list|()
return|;
block|}
DECL|method|decorate
specifier|public
specifier|static
name|CompoundProcessor
name|decorate
parameter_list|(
name|CompoundProcessor
name|compoundProcessor
parameter_list|,
name|List
argument_list|<
name|SimulateProcessorResult
argument_list|>
name|processorResultList
parameter_list|)
block|{
name|List
argument_list|<
name|Processor
argument_list|>
name|processors
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|compoundProcessor
operator|.
name|getProcessors
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|Processor
name|processor
range|:
name|compoundProcessor
operator|.
name|getProcessors
argument_list|()
control|)
block|{
if|if
condition|(
name|processor
operator|instanceof
name|CompoundProcessor
condition|)
block|{
name|processors
operator|.
name|add
argument_list|(
name|decorate
argument_list|(
operator|(
name|CompoundProcessor
operator|)
name|processor
argument_list|,
name|processorResultList
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|processors
operator|.
name|add
argument_list|(
operator|new
name|TrackingResultProcessor
argument_list|(
name|processor
argument_list|,
name|processorResultList
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|List
argument_list|<
name|Processor
argument_list|>
name|onFailureProcessors
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|compoundProcessor
operator|.
name|getProcessors
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|Processor
name|processor
range|:
name|compoundProcessor
operator|.
name|getOnFailureProcessors
argument_list|()
control|)
block|{
if|if
condition|(
name|processor
operator|instanceof
name|CompoundProcessor
condition|)
block|{
name|onFailureProcessors
operator|.
name|add
argument_list|(
name|decorate
argument_list|(
operator|(
name|CompoundProcessor
operator|)
name|processor
argument_list|,
name|processorResultList
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|onFailureProcessors
operator|.
name|add
argument_list|(
operator|new
name|TrackingResultProcessor
argument_list|(
name|processor
argument_list|,
name|processorResultList
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|return
operator|new
name|CompoundProcessor
argument_list|(
name|processors
argument_list|,
name|onFailureProcessors
argument_list|)
return|;
block|}
block|}
end_class

end_unit

