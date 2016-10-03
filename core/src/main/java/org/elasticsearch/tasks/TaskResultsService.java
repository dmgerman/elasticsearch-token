begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.tasks
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|tasks
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|logging
operator|.
name|log4j
operator|.
name|message
operator|.
name|ParameterizedMessage
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|logging
operator|.
name|log4j
operator|.
name|util
operator|.
name|Supplier
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|IOUtils
import|;
end_import

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
name|ExceptionsHelper
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
name|admin
operator|.
name|indices
operator|.
name|create
operator|.
name|CreateIndexRequest
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
name|admin
operator|.
name|indices
operator|.
name|create
operator|.
name|CreateIndexResponse
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
name|admin
operator|.
name|indices
operator|.
name|create
operator|.
name|TransportCreateIndexAction
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
name|admin
operator|.
name|indices
operator|.
name|mapping
operator|.
name|put
operator|.
name|PutMappingResponse
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
name|IndexRequestBuilder
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
name|IndexResponse
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|client
operator|.
name|Client
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|client
operator|.
name|Requests
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|ClusterState
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|metadata
operator|.
name|IndexMetaData
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|service
operator|.
name|ClusterService
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
name|component
operator|.
name|AbstractComponent
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
name|inject
operator|.
name|Inject
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
name|Streams
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
name|XContentFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|IndexAlreadyExistsException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayOutputStream
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
name|io
operator|.
name|InputStream
import|;
end_import

begin_comment
comment|/**  * Service that can store task results.  */
end_comment

begin_class
DECL|class|TaskResultsService
specifier|public
class|class
name|TaskResultsService
extends|extends
name|AbstractComponent
block|{
DECL|field|TASK_INDEX
specifier|public
specifier|static
specifier|final
name|String
name|TASK_INDEX
init|=
literal|".tasks"
decl_stmt|;
DECL|field|TASK_TYPE
specifier|public
specifier|static
specifier|final
name|String
name|TASK_TYPE
init|=
literal|"task"
decl_stmt|;
DECL|field|TASK_RESULT_INDEX_MAPPING_FILE
specifier|public
specifier|static
specifier|final
name|String
name|TASK_RESULT_INDEX_MAPPING_FILE
init|=
literal|"task-index-mapping.json"
decl_stmt|;
DECL|field|client
specifier|private
specifier|final
name|Client
name|client
decl_stmt|;
DECL|field|clusterService
specifier|private
specifier|final
name|ClusterService
name|clusterService
decl_stmt|;
DECL|field|createIndexAction
specifier|private
specifier|final
name|TransportCreateIndexAction
name|createIndexAction
decl_stmt|;
annotation|@
name|Inject
DECL|method|TaskResultsService
specifier|public
name|TaskResultsService
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|Client
name|client
parameter_list|,
name|ClusterService
name|clusterService
parameter_list|,
name|TransportCreateIndexAction
name|createIndexAction
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|client
operator|=
name|client
expr_stmt|;
name|this
operator|.
name|clusterService
operator|=
name|clusterService
expr_stmt|;
name|this
operator|.
name|createIndexAction
operator|=
name|createIndexAction
expr_stmt|;
block|}
DECL|method|storeResult
specifier|public
name|void
name|storeResult
parameter_list|(
name|TaskResult
name|taskResult
parameter_list|,
name|ActionListener
argument_list|<
name|Void
argument_list|>
name|listener
parameter_list|)
block|{
name|ClusterState
name|state
init|=
name|clusterService
operator|.
name|state
argument_list|()
decl_stmt|;
if|if
condition|(
name|state
operator|.
name|routingTable
argument_list|()
operator|.
name|hasIndex
argument_list|(
name|TASK_INDEX
argument_list|)
operator|==
literal|false
condition|)
block|{
name|CreateIndexRequest
name|createIndexRequest
init|=
operator|new
name|CreateIndexRequest
argument_list|()
decl_stmt|;
name|createIndexRequest
operator|.
name|settings
argument_list|(
name|taskResultIndexSettings
argument_list|()
argument_list|)
expr_stmt|;
name|createIndexRequest
operator|.
name|index
argument_list|(
name|TASK_INDEX
argument_list|)
expr_stmt|;
name|createIndexRequest
operator|.
name|mapping
argument_list|(
name|TASK_TYPE
argument_list|,
name|taskResultIndexMapping
argument_list|()
argument_list|)
expr_stmt|;
name|createIndexRequest
operator|.
name|cause
argument_list|(
literal|"auto(task api)"
argument_list|)
expr_stmt|;
name|createIndexAction
operator|.
name|execute
argument_list|(
literal|null
argument_list|,
name|createIndexRequest
argument_list|,
operator|new
name|ActionListener
argument_list|<
name|CreateIndexResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onResponse
parameter_list|(
name|CreateIndexResponse
name|result
parameter_list|)
block|{
name|doStoreResult
argument_list|(
name|taskResult
argument_list|,
name|listener
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|onFailure
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
if|if
condition|(
name|ExceptionsHelper
operator|.
name|unwrapCause
argument_list|(
name|e
argument_list|)
operator|instanceof
name|IndexAlreadyExistsException
condition|)
block|{
comment|// we have the index, do it
try|try
block|{
name|doStoreResult
argument_list|(
name|taskResult
argument_list|,
name|listener
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|inner
parameter_list|)
block|{
name|inner
operator|.
name|addSuppressed
argument_list|(
name|e
argument_list|)
expr_stmt|;
name|listener
operator|.
name|onFailure
argument_list|(
name|inner
argument_list|)
expr_stmt|;
block|}
block|}
else|else
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
block|}
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|IndexMetaData
name|metaData
init|=
name|state
operator|.
name|getMetaData
argument_list|()
operator|.
name|index
argument_list|(
name|TASK_INDEX
argument_list|)
decl_stmt|;
if|if
condition|(
name|metaData
operator|.
name|getMappings
argument_list|()
operator|.
name|containsKey
argument_list|(
name|TASK_TYPE
argument_list|)
operator|==
literal|false
condition|)
block|{
comment|// The index already exists but doesn't have our mapping
name|client
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|preparePutMapping
argument_list|(
name|TASK_INDEX
argument_list|)
operator|.
name|setType
argument_list|(
name|TASK_TYPE
argument_list|)
operator|.
name|setSource
argument_list|(
name|taskResultIndexMapping
argument_list|()
argument_list|)
operator|.
name|execute
argument_list|(
operator|new
name|ActionListener
argument_list|<
name|PutMappingResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onResponse
parameter_list|(
name|PutMappingResponse
name|putMappingResponse
parameter_list|)
block|{
name|doStoreResult
argument_list|(
name|taskResult
argument_list|,
name|listener
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|onFailure
parameter_list|(
name|Exception
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
else|else
block|{
name|doStoreResult
argument_list|(
name|taskResult
argument_list|,
name|listener
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|doStoreResult
specifier|private
name|void
name|doStoreResult
parameter_list|(
name|TaskResult
name|taskResult
parameter_list|,
name|ActionListener
argument_list|<
name|Void
argument_list|>
name|listener
parameter_list|)
block|{
name|IndexRequestBuilder
name|index
init|=
name|client
operator|.
name|prepareIndex
argument_list|(
name|TASK_INDEX
argument_list|,
name|TASK_TYPE
argument_list|,
name|taskResult
operator|.
name|getTask
argument_list|()
operator|.
name|getTaskId
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
try|try
init|(
name|XContentBuilder
name|builder
init|=
name|XContentFactory
operator|.
name|contentBuilder
argument_list|(
name|Requests
operator|.
name|INDEX_CONTENT_TYPE
argument_list|)
init|)
block|{
name|taskResult
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|ToXContent
operator|.
name|EMPTY_PARAMS
argument_list|)
expr_stmt|;
name|index
operator|.
name|setSource
argument_list|(
name|builder
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticsearchException
argument_list|(
literal|"Couldn't convert task result to XContent for [{}]"
argument_list|,
name|e
argument_list|,
name|taskResult
operator|.
name|getTask
argument_list|()
argument_list|)
throw|;
block|}
name|index
operator|.
name|execute
argument_list|(
operator|new
name|ActionListener
argument_list|<
name|IndexResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onResponse
parameter_list|(
name|IndexResponse
name|indexResponse
parameter_list|)
block|{
name|listener
operator|.
name|onResponse
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|onFailure
parameter_list|(
name|Exception
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
DECL|method|taskResultIndexSettings
specifier|private
name|Settings
name|taskResultIndexSettings
parameter_list|()
block|{
return|return
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|INDEX_NUMBER_OF_SHARDS_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
literal|1
argument_list|)
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|INDEX_AUTO_EXPAND_REPLICAS_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
literal|"0-1"
argument_list|)
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_PRIORITY
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
DECL|method|taskResultIndexMapping
specifier|public
name|String
name|taskResultIndexMapping
parameter_list|()
block|{
try|try
init|(
name|InputStream
name|is
init|=
name|getClass
argument_list|()
operator|.
name|getResourceAsStream
argument_list|(
name|TASK_RESULT_INDEX_MAPPING_FILE
argument_list|)
init|)
block|{
name|ByteArrayOutputStream
name|out
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|Streams
operator|.
name|copy
argument_list|(
name|is
argument_list|,
name|out
argument_list|)
expr_stmt|;
return|return
name|out
operator|.
name|toString
argument_list|(
name|IOUtils
operator|.
name|UTF_8
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|logger
operator|.
name|error
argument_list|(
call|(
name|Supplier
argument_list|<
name|?
argument_list|>
call|)
argument_list|()
operator|->
operator|new
name|ParameterizedMessage
argument_list|(
literal|"failed to create tasks results index template [{}]"
argument_list|,
name|TASK_RESULT_INDEX_MAPPING_FILE
argument_list|)
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"failed to create tasks results index template ["
operator|+
name|TASK_RESULT_INDEX_MAPPING_FILE
operator|+
literal|"]"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

