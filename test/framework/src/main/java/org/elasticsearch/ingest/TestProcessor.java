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
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicInteger
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|function
operator|.
name|Consumer
import|;
end_import

begin_comment
comment|/**  * Processor used for testing, keeps track of how many times it is invoked and  * accepts a {@link Consumer} of {@link IngestDocument} to be called when executed.  */
end_comment

begin_class
DECL|class|TestProcessor
specifier|public
class|class
name|TestProcessor
implements|implements
name|Processor
block|{
DECL|field|type
specifier|private
specifier|final
name|String
name|type
decl_stmt|;
DECL|field|tag
specifier|private
specifier|final
name|String
name|tag
decl_stmt|;
DECL|field|ingestDocumentConsumer
specifier|private
specifier|final
name|Consumer
argument_list|<
name|IngestDocument
argument_list|>
name|ingestDocumentConsumer
decl_stmt|;
DECL|field|invokedCounter
specifier|private
specifier|final
name|AtomicInteger
name|invokedCounter
init|=
operator|new
name|AtomicInteger
argument_list|()
decl_stmt|;
DECL|method|TestProcessor
specifier|public
name|TestProcessor
parameter_list|(
name|Consumer
argument_list|<
name|IngestDocument
argument_list|>
name|ingestDocumentConsumer
parameter_list|)
block|{
name|this
argument_list|(
literal|null
argument_list|,
literal|"test-processor"
argument_list|,
name|ingestDocumentConsumer
argument_list|)
expr_stmt|;
block|}
DECL|method|TestProcessor
specifier|public
name|TestProcessor
parameter_list|(
name|String
name|tag
parameter_list|,
name|String
name|type
parameter_list|,
name|Consumer
argument_list|<
name|IngestDocument
argument_list|>
name|ingestDocumentConsumer
parameter_list|)
block|{
name|this
operator|.
name|ingestDocumentConsumer
operator|=
name|ingestDocumentConsumer
expr_stmt|;
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
name|this
operator|.
name|tag
operator|=
name|tag
expr_stmt|;
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
name|invokedCounter
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
name|ingestDocumentConsumer
operator|.
name|accept
argument_list|(
name|ingestDocument
argument_list|)
expr_stmt|;
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
name|type
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
name|tag
return|;
block|}
DECL|method|getInvokedCounter
specifier|public
name|int
name|getInvokedCounter
parameter_list|()
block|{
return|return
name|invokedCounter
operator|.
name|get
argument_list|()
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
name|TestProcessor
argument_list|>
block|{
annotation|@
name|Override
DECL|method|doCreate
specifier|public
name|TestProcessor
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
return|return
operator|new
name|TestProcessor
argument_list|(
name|processorTag
argument_list|,
literal|"test-processor"
argument_list|,
name|ingestDocument
lambda|->
block|{}
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

