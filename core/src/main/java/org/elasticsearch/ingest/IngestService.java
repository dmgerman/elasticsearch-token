begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.ingest
package|package
name|org
operator|.
name|elasticsearch
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
name|settings
operator|.
name|Settings
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|ScriptService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|threadpool
operator|.
name|ThreadPool
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Closeable
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

begin_comment
comment|/**  * Holder class for several ingest related services.  */
end_comment

begin_class
DECL|class|IngestService
specifier|public
class|class
name|IngestService
implements|implements
name|Closeable
block|{
DECL|field|pipelineStore
specifier|private
specifier|final
name|PipelineStore
name|pipelineStore
decl_stmt|;
DECL|field|pipelineExecutionService
specifier|private
specifier|final
name|PipelineExecutionService
name|pipelineExecutionService
decl_stmt|;
DECL|field|processorsRegistryBuilder
specifier|private
specifier|final
name|ProcessorsRegistry
operator|.
name|Builder
name|processorsRegistryBuilder
decl_stmt|;
DECL|method|IngestService
specifier|public
name|IngestService
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|,
name|ProcessorsRegistry
operator|.
name|Builder
name|processorsRegistryBuilder
parameter_list|)
block|{
name|this
operator|.
name|processorsRegistryBuilder
operator|=
name|processorsRegistryBuilder
expr_stmt|;
name|this
operator|.
name|pipelineStore
operator|=
operator|new
name|PipelineStore
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|pipelineExecutionService
operator|=
operator|new
name|PipelineExecutionService
argument_list|(
name|pipelineStore
argument_list|,
name|threadPool
argument_list|)
expr_stmt|;
block|}
DECL|method|getPipelineStore
specifier|public
name|PipelineStore
name|getPipelineStore
parameter_list|()
block|{
return|return
name|pipelineStore
return|;
block|}
DECL|method|getPipelineExecutionService
specifier|public
name|PipelineExecutionService
name|getPipelineExecutionService
parameter_list|()
block|{
return|return
name|pipelineExecutionService
return|;
block|}
DECL|method|setScriptService
specifier|public
name|void
name|setScriptService
parameter_list|(
name|ScriptService
name|scriptService
parameter_list|)
block|{
name|pipelineStore
operator|.
name|buildProcessorFactoryRegistry
argument_list|(
name|processorsRegistryBuilder
argument_list|,
name|scriptService
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
name|pipelineStore
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

