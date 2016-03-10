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
name|ingest
operator|.
name|core
operator|.
name|AbstractProcessor
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
name|AbstractProcessorFactory
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
name|TemplateService
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

begin_comment
comment|/**  * Processor that raises a runtime exception with a provided  * error message.  */
end_comment

begin_class
DECL|class|FailProcessor
specifier|public
specifier|final
class|class
name|FailProcessor
extends|extends
name|AbstractProcessor
block|{
DECL|field|TYPE
specifier|public
specifier|static
specifier|final
name|String
name|TYPE
init|=
literal|"fail"
decl_stmt|;
DECL|field|message
specifier|private
specifier|final
name|TemplateService
operator|.
name|Template
name|message
decl_stmt|;
DECL|method|FailProcessor
name|FailProcessor
parameter_list|(
name|String
name|tag
parameter_list|,
name|TemplateService
operator|.
name|Template
name|message
parameter_list|)
block|{
name|super
argument_list|(
name|tag
argument_list|)
expr_stmt|;
name|this
operator|.
name|message
operator|=
name|message
expr_stmt|;
block|}
DECL|method|getMessage
specifier|public
name|TemplateService
operator|.
name|Template
name|getMessage
parameter_list|()
block|{
return|return
name|message
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
name|document
parameter_list|)
block|{
throw|throw
operator|new
name|FailProcessorException
argument_list|(
name|document
operator|.
name|renderTemplate
argument_list|(
name|message
argument_list|)
argument_list|)
throw|;
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
name|TYPE
return|;
block|}
DECL|class|Factory
specifier|public
specifier|static
specifier|final
class|class
name|Factory
extends|extends
name|AbstractProcessorFactory
argument_list|<
name|FailProcessor
argument_list|>
block|{
DECL|field|templateService
specifier|private
specifier|final
name|TemplateService
name|templateService
decl_stmt|;
DECL|method|Factory
specifier|public
name|Factory
parameter_list|(
name|TemplateService
name|templateService
parameter_list|)
block|{
name|this
operator|.
name|templateService
operator|=
name|templateService
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doCreate
specifier|public
name|FailProcessor
name|doCreate
parameter_list|(
name|String
name|processorTag
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|config
parameter_list|)
throws|throws
name|Exception
block|{
name|String
name|message
init|=
name|ConfigurationUtils
operator|.
name|readStringProperty
argument_list|(
name|TYPE
argument_list|,
name|processorTag
argument_list|,
name|config
argument_list|,
literal|"message"
argument_list|)
decl_stmt|;
return|return
operator|new
name|FailProcessor
argument_list|(
name|processorTag
argument_list|,
name|templateService
operator|.
name|compile
argument_list|(
name|message
argument_list|)
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit
