begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.plugin.ingest
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|plugin
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
name|ActionListener
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
name|common
operator|.
name|util
operator|.
name|concurrent
operator|.
name|EsExecutors
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

begin_class
DECL|class|PipelineExecutionService
specifier|public
class|class
name|PipelineExecutionService
block|{
DECL|field|THREAD_POOL_NAME
specifier|static
specifier|final
name|String
name|THREAD_POOL_NAME
init|=
name|IngestPlugin
operator|.
name|NAME
decl_stmt|;
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
name|indexRequest
parameter_list|,
name|String
name|pipelineId
parameter_list|,
name|ActionListener
argument_list|<
name|Void
argument_list|>
name|listener
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
name|listener
operator|.
name|onFailure
argument_list|(
operator|new
name|IllegalArgumentException
argument_list|(
literal|"pipeline with id ["
operator|+
name|pipelineId
operator|+
literal|"] does not exist"
argument_list|)
argument_list|)
expr_stmt|;
return|return;
block|}
name|threadPool
operator|.
name|executor
argument_list|(
name|THREAD_POOL_NAME
argument_list|)
operator|.
name|execute
argument_list|(
parameter_list|()
lambda|->
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
try|try
block|{
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
name|listener
operator|.
name|onResponse
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|listener
operator|.
name|onFailure
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
DECL|method|additionalSettings
specifier|public
specifier|static
name|Settings
name|additionalSettings
parameter_list|(
name|Settings
name|nodeSettings
parameter_list|)
block|{
name|Settings
name|settings
init|=
name|nodeSettings
operator|.
name|getAsSettings
argument_list|(
literal|"threadpool."
operator|+
name|THREAD_POOL_NAME
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|settings
operator|.
name|names
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
comment|// the TP is already configured in the node settings
comment|// no need for additional settings
return|return
name|Settings
operator|.
name|EMPTY
return|;
block|}
name|int
name|availableProcessors
init|=
name|EsExecutors
operator|.
name|boundedNumberOfProcessors
argument_list|(
name|nodeSettings
argument_list|)
decl_stmt|;
return|return
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"threadpool."
operator|+
name|THREAD_POOL_NAME
operator|+
literal|".type"
argument_list|,
literal|"fixed"
argument_list|)
operator|.
name|put
argument_list|(
literal|"threadpool."
operator|+
name|THREAD_POOL_NAME
operator|+
literal|".size"
argument_list|,
name|availableProcessors
argument_list|)
operator|.
name|put
argument_list|(
literal|"threadpool."
operator|+
name|THREAD_POOL_NAME
operator|+
literal|".queue_size"
argument_list|,
literal|200
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
block|}
end_class

end_unit

