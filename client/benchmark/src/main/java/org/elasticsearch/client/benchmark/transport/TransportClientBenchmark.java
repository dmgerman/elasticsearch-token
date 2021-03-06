begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.client.benchmark.transport
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|client
operator|.
name|benchmark
operator|.
name|transport
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
name|action
operator|.
name|bulk
operator|.
name|BulkResponse
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
name|action
operator|.
name|search
operator|.
name|SearchResponse
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
name|benchmark
operator|.
name|AbstractBenchmark
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
name|benchmark
operator|.
name|ops
operator|.
name|bulk
operator|.
name|BulkRequestExecutor
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
name|benchmark
operator|.
name|ops
operator|.
name|search
operator|.
name|SearchRequestExecutor
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
name|transport
operator|.
name|TransportClient
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
name|transport
operator|.
name|TransportAddress
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
name|XContentType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|QueryBuilders
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|plugin
operator|.
name|noop
operator|.
name|NoopPlugin
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|plugin
operator|.
name|noop
operator|.
name|action
operator|.
name|bulk
operator|.
name|NoopBulkAction
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|plugin
operator|.
name|noop
operator|.
name|action
operator|.
name|bulk
operator|.
name|NoopBulkRequestBuilder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|plugin
operator|.
name|noop
operator|.
name|action
operator|.
name|search
operator|.
name|NoopSearchAction
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|plugin
operator|.
name|noop
operator|.
name|action
operator|.
name|search
operator|.
name|NoopSearchRequestBuilder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|RestStatus
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|transport
operator|.
name|client
operator|.
name|PreBuiltTransportClient
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|InetAddress
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|charset
operator|.
name|StandardCharsets
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
name|concurrent
operator|.
name|ExecutionException
import|;
end_import

begin_class
DECL|class|TransportClientBenchmark
specifier|public
specifier|final
class|class
name|TransportClientBenchmark
extends|extends
name|AbstractBenchmark
argument_list|<
name|TransportClient
argument_list|>
block|{
DECL|method|main
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|Exception
block|{
name|TransportClientBenchmark
name|benchmark
init|=
operator|new
name|TransportClientBenchmark
argument_list|()
decl_stmt|;
name|benchmark
operator|.
name|run
argument_list|(
name|args
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|client
specifier|protected
name|TransportClient
name|client
parameter_list|(
name|String
name|benchmarkTargetHost
parameter_list|)
throws|throws
name|Exception
block|{
name|TransportClient
name|client
init|=
operator|new
name|PreBuiltTransportClient
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
name|NoopPlugin
operator|.
name|class
argument_list|)
decl_stmt|;
name|client
operator|.
name|addTransportAddress
argument_list|(
operator|new
name|TransportAddress
argument_list|(
name|InetAddress
operator|.
name|getByName
argument_list|(
name|benchmarkTargetHost
argument_list|)
argument_list|,
literal|9300
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|client
return|;
block|}
annotation|@
name|Override
DECL|method|bulkRequestExecutor
specifier|protected
name|BulkRequestExecutor
name|bulkRequestExecutor
parameter_list|(
name|TransportClient
name|client
parameter_list|,
name|String
name|indexName
parameter_list|,
name|String
name|typeName
parameter_list|)
block|{
return|return
operator|new
name|TransportBulkRequestExecutor
argument_list|(
name|client
argument_list|,
name|indexName
argument_list|,
name|typeName
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|searchRequestExecutor
specifier|protected
name|SearchRequestExecutor
name|searchRequestExecutor
parameter_list|(
name|TransportClient
name|client
parameter_list|,
name|String
name|indexName
parameter_list|)
block|{
return|return
operator|new
name|TransportSearchRequestExecutor
argument_list|(
name|client
argument_list|,
name|indexName
argument_list|)
return|;
block|}
DECL|class|TransportBulkRequestExecutor
specifier|private
specifier|static
specifier|final
class|class
name|TransportBulkRequestExecutor
implements|implements
name|BulkRequestExecutor
block|{
DECL|field|client
specifier|private
specifier|final
name|TransportClient
name|client
decl_stmt|;
DECL|field|indexName
specifier|private
specifier|final
name|String
name|indexName
decl_stmt|;
DECL|field|typeName
specifier|private
specifier|final
name|String
name|typeName
decl_stmt|;
DECL|method|TransportBulkRequestExecutor
name|TransportBulkRequestExecutor
parameter_list|(
name|TransportClient
name|client
parameter_list|,
name|String
name|indexName
parameter_list|,
name|String
name|typeName
parameter_list|)
block|{
name|this
operator|.
name|client
operator|=
name|client
expr_stmt|;
name|this
operator|.
name|indexName
operator|=
name|indexName
expr_stmt|;
name|this
operator|.
name|typeName
operator|=
name|typeName
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|bulkIndex
specifier|public
name|boolean
name|bulkIndex
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|bulkData
parameter_list|)
block|{
name|NoopBulkRequestBuilder
name|builder
init|=
name|NoopBulkAction
operator|.
name|INSTANCE
operator|.
name|newRequestBuilder
argument_list|(
name|client
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|bulkItem
range|:
name|bulkData
control|)
block|{
name|builder
operator|.
name|add
argument_list|(
operator|new
name|IndexRequest
argument_list|(
name|indexName
argument_list|,
name|typeName
argument_list|)
operator|.
name|source
argument_list|(
name|bulkItem
operator|.
name|getBytes
argument_list|(
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
argument_list|,
name|XContentType
operator|.
name|JSON
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|BulkResponse
name|bulkResponse
decl_stmt|;
try|try
block|{
name|bulkResponse
operator|=
name|builder
operator|.
name|execute
argument_list|()
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|interrupt
argument_list|()
expr_stmt|;
return|return
literal|false
return|;
block|}
catch|catch
parameter_list|(
name|ExecutionException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticsearchException
argument_list|(
name|e
argument_list|)
throw|;
block|}
return|return
operator|!
name|bulkResponse
operator|.
name|hasFailures
argument_list|()
return|;
block|}
block|}
DECL|class|TransportSearchRequestExecutor
specifier|private
specifier|static
specifier|final
class|class
name|TransportSearchRequestExecutor
implements|implements
name|SearchRequestExecutor
block|{
DECL|field|client
specifier|private
specifier|final
name|TransportClient
name|client
decl_stmt|;
DECL|field|indexName
specifier|private
specifier|final
name|String
name|indexName
decl_stmt|;
DECL|method|TransportSearchRequestExecutor
specifier|private
name|TransportSearchRequestExecutor
parameter_list|(
name|TransportClient
name|client
parameter_list|,
name|String
name|indexName
parameter_list|)
block|{
name|this
operator|.
name|client
operator|=
name|client
expr_stmt|;
name|this
operator|.
name|indexName
operator|=
name|indexName
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|search
specifier|public
name|boolean
name|search
parameter_list|(
name|String
name|source
parameter_list|)
block|{
specifier|final
name|SearchResponse
name|response
decl_stmt|;
name|NoopSearchRequestBuilder
name|builder
init|=
name|NoopSearchAction
operator|.
name|INSTANCE
operator|.
name|newRequestBuilder
argument_list|(
name|client
argument_list|)
decl_stmt|;
try|try
block|{
name|builder
operator|.
name|setIndices
argument_list|(
name|indexName
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
name|wrapperQuery
argument_list|(
name|source
argument_list|)
argument_list|)
expr_stmt|;
name|response
operator|=
name|client
operator|.
name|execute
argument_list|(
name|NoopSearchAction
operator|.
name|INSTANCE
argument_list|,
name|builder
operator|.
name|request
argument_list|()
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
return|return
name|response
operator|.
name|status
argument_list|()
operator|==
name|RestStatus
operator|.
name|OK
return|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|interrupt
argument_list|()
expr_stmt|;
return|return
literal|false
return|;
block|}
catch|catch
parameter_list|(
name|ExecutionException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticsearchException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
block|}
block|}
end_class

end_unit

