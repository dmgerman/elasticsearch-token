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
name|index
operator|.
name|IndexRequest
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
name|Strings
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
name|collect
operator|.
name|Tuple
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
name|threadpool
operator|.
name|ThreadPool
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
name|function
operator|.
name|Consumer
import|;
end_import

begin_class
DECL|class|PipelineExecutionService
specifier|public
class|class
name|PipelineExecutionService
block|{
DECL|field|store
specifier|private
specifier|final
name|PipelineStore
name|store
decl_stmt|;
DECL|field|threadPool
specifier|private
specifier|final
name|ThreadPool
name|threadPool
decl_stmt|;
DECL|method|PipelineExecutionService
specifier|public
name|PipelineExecutionService
parameter_list|(
name|PipelineStore
name|store
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|)
block|{
name|this
operator|.
name|store
operator|=
name|store
expr_stmt|;
name|this
operator|.
name|threadPool
operator|=
name|threadPool
expr_stmt|;
block|}
DECL|method|execute
specifier|public
name|void
name|execute
parameter_list|(
name|IndexRequest
name|request
parameter_list|,
name|Consumer
argument_list|<
name|Throwable
argument_list|>
name|failureHandler
parameter_list|,
name|Consumer
argument_list|<
name|Boolean
argument_list|>
name|completionHandler
parameter_list|)
block|{
name|Pipeline
name|pipeline
init|=
name|getPipeline
argument_list|(
name|request
operator|.
name|pipeline
argument_list|()
argument_list|)
decl_stmt|;
name|threadPool
operator|.
name|executor
argument_list|(
name|ThreadPool
operator|.
name|Names
operator|.
name|INGEST
argument_list|)
operator|.
name|execute
argument_list|(
parameter_list|()
lambda|->
block|{
try|try
block|{
name|innerExecute
argument_list|(
name|request
argument_list|,
name|pipeline
argument_list|)
expr_stmt|;
name|completionHandler
operator|.
name|accept
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|failureHandler
operator|.
name|accept
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
DECL|method|execute
specifier|public
name|void
name|execute
parameter_list|(
name|Iterable
argument_list|<
name|ActionRequest
argument_list|>
name|actionRequests
parameter_list|,
name|Consumer
argument_list|<
name|Tuple
argument_list|<
name|IndexRequest
argument_list|,
name|Throwable
argument_list|>
argument_list|>
name|itemFailureHandler
parameter_list|,
name|Consumer
argument_list|<
name|Boolean
argument_list|>
name|completionHandler
parameter_list|)
block|{
name|threadPool
operator|.
name|executor
argument_list|(
name|ThreadPool
operator|.
name|Names
operator|.
name|INGEST
argument_list|)
operator|.
name|execute
argument_list|(
parameter_list|()
lambda|->
block|{
for|for
control|(
name|ActionRequest
name|actionRequest
range|:
name|actionRequests
control|)
block|{
if|if
condition|(
operator|(
name|actionRequest
operator|instanceof
name|IndexRequest
operator|)
condition|)
block|{
name|IndexRequest
name|indexRequest
init|=
operator|(
name|IndexRequest
operator|)
name|actionRequest
decl_stmt|;
if|if
condition|(
name|Strings
operator|.
name|hasText
argument_list|(
name|indexRequest
operator|.
name|pipeline
argument_list|()
argument_list|)
condition|)
block|{
try|try
block|{
name|innerExecute
argument_list|(
name|indexRequest
argument_list|,
name|getPipeline
argument_list|(
name|indexRequest
operator|.
name|pipeline
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|itemFailureHandler
operator|.
name|accept
argument_list|(
operator|new
name|Tuple
argument_list|<>
argument_list|(
name|indexRequest
argument_list|,
name|e
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
name|completionHandler
operator|.
name|accept
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
block|}
DECL|method|innerExecute
specifier|private
name|void
name|innerExecute
parameter_list|(
name|IndexRequest
name|indexRequest
parameter_list|,
name|Pipeline
name|pipeline
parameter_list|)
throws|throws
name|Exception
block|{
name|String
name|index
init|=
name|indexRequest
operator|.
name|index
argument_list|()
decl_stmt|;
name|String
name|type
init|=
name|indexRequest
operator|.
name|type
argument_list|()
decl_stmt|;
name|String
name|id
init|=
name|indexRequest
operator|.
name|id
argument_list|()
decl_stmt|;
name|String
name|routing
init|=
name|indexRequest
operator|.
name|routing
argument_list|()
decl_stmt|;
name|String
name|parent
init|=
name|indexRequest
operator|.
name|parent
argument_list|()
decl_stmt|;
name|String
name|timestamp
init|=
name|indexRequest
operator|.
name|timestamp
argument_list|()
decl_stmt|;
name|String
name|ttl
init|=
name|indexRequest
operator|.
name|ttl
argument_list|()
operator|==
literal|null
condition|?
literal|null
else|:
name|indexRequest
operator|.
name|ttl
argument_list|()
operator|.
name|toString
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|sourceAsMap
init|=
name|indexRequest
operator|.
name|sourceAsMap
argument_list|()
decl_stmt|;
name|IngestDocument
name|ingestDocument
init|=
operator|new
name|IngestDocument
argument_list|(
name|index
argument_list|,
name|type
argument_list|,
name|id
argument_list|,
name|routing
argument_list|,
name|parent
argument_list|,
name|timestamp
argument_list|,
name|ttl
argument_list|,
name|sourceAsMap
argument_list|)
decl_stmt|;
name|pipeline
operator|.
name|execute
argument_list|(
name|ingestDocument
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|IngestDocument
operator|.
name|MetaData
argument_list|,
name|String
argument_list|>
name|metadataMap
init|=
name|ingestDocument
operator|.
name|extractMetadata
argument_list|()
decl_stmt|;
comment|//it's fine to set all metadata fields all the time, as ingest document holds their starting values
comment|//before ingestion, which might also get modified during ingestion.
name|indexRequest
operator|.
name|index
argument_list|(
name|metadataMap
operator|.
name|get
argument_list|(
name|IngestDocument
operator|.
name|MetaData
operator|.
name|INDEX
argument_list|)
argument_list|)
expr_stmt|;
name|indexRequest
operator|.
name|type
argument_list|(
name|metadataMap
operator|.
name|get
argument_list|(
name|IngestDocument
operator|.
name|MetaData
operator|.
name|TYPE
argument_list|)
argument_list|)
expr_stmt|;
name|indexRequest
operator|.
name|id
argument_list|(
name|metadataMap
operator|.
name|get
argument_list|(
name|IngestDocument
operator|.
name|MetaData
operator|.
name|ID
argument_list|)
argument_list|)
expr_stmt|;
name|indexRequest
operator|.
name|routing
argument_list|(
name|metadataMap
operator|.
name|get
argument_list|(
name|IngestDocument
operator|.
name|MetaData
operator|.
name|ROUTING
argument_list|)
argument_list|)
expr_stmt|;
name|indexRequest
operator|.
name|parent
argument_list|(
name|metadataMap
operator|.
name|get
argument_list|(
name|IngestDocument
operator|.
name|MetaData
operator|.
name|PARENT
argument_list|)
argument_list|)
expr_stmt|;
name|indexRequest
operator|.
name|timestamp
argument_list|(
name|metadataMap
operator|.
name|get
argument_list|(
name|IngestDocument
operator|.
name|MetaData
operator|.
name|TIMESTAMP
argument_list|)
argument_list|)
expr_stmt|;
name|indexRequest
operator|.
name|ttl
argument_list|(
name|metadataMap
operator|.
name|get
argument_list|(
name|IngestDocument
operator|.
name|MetaData
operator|.
name|TTL
argument_list|)
argument_list|)
expr_stmt|;
name|indexRequest
operator|.
name|source
argument_list|(
name|ingestDocument
operator|.
name|getSourceAndMetadata
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|getPipeline
specifier|private
name|Pipeline
name|getPipeline
parameter_list|(
name|String
name|pipelineId
parameter_list|)
block|{
name|Pipeline
name|pipeline
init|=
name|store
operator|.
name|get
argument_list|(
name|pipelineId
argument_list|)
decl_stmt|;
if|if
condition|(
name|pipeline
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"pipeline with id ["
operator|+
name|pipelineId
operator|+
literal|"] does not exist"
argument_list|)
throw|;
block|}
return|return
name|pipeline
return|;
block|}
block|}
end_class

end_unit

