begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.percolator
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|percolator
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ResourceNotFoundException
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
name|get
operator|.
name|GetRequest
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
name|get
operator|.
name|MultiGetItemResponse
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
name|get
operator|.
name|MultiGetRequest
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
name|get
operator|.
name|MultiGetResponse
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
name|MultiSearchRequest
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
name|MultiSearchResponse
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
name|SearchRequest
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
name|support
operator|.
name|ActionFilters
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
name|support
operator|.
name|HandledTransportAction
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
name|cluster
operator|.
name|metadata
operator|.
name|IndexNameExpressionResolver
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
name|ParseFieldMatcher
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
name|indices
operator|.
name|query
operator|.
name|IndicesQueriesRegistry
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|AggregatorParsers
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
name|org
operator|.
name|elasticsearch
operator|.
name|transport
operator|.
name|TransportService
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
name|HashMap
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

begin_class
annotation|@
name|Deprecated
DECL|class|TransportMultiPercolateAction
specifier|public
class|class
name|TransportMultiPercolateAction
extends|extends
name|HandledTransportAction
argument_list|<
name|MultiPercolateRequest
argument_list|,
name|MultiPercolateResponse
argument_list|>
block|{
DECL|field|client
specifier|private
specifier|final
name|Client
name|client
decl_stmt|;
DECL|field|parseFieldMatcher
specifier|private
specifier|final
name|ParseFieldMatcher
name|parseFieldMatcher
decl_stmt|;
DECL|field|queryRegistry
specifier|private
specifier|final
name|IndicesQueriesRegistry
name|queryRegistry
decl_stmt|;
DECL|field|aggParsers
specifier|private
specifier|final
name|AggregatorParsers
name|aggParsers
decl_stmt|;
annotation|@
name|Inject
DECL|method|TransportMultiPercolateAction
specifier|public
name|TransportMultiPercolateAction
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|,
name|TransportService
name|transportService
parameter_list|,
name|ActionFilters
name|actionFilters
parameter_list|,
name|IndexNameExpressionResolver
name|indexNameExpressionResolver
parameter_list|,
name|Client
name|client
parameter_list|,
name|IndicesQueriesRegistry
name|queryRegistry
parameter_list|,
name|AggregatorParsers
name|aggParsers
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|MultiPercolateAction
operator|.
name|NAME
argument_list|,
name|threadPool
argument_list|,
name|transportService
argument_list|,
name|actionFilters
argument_list|,
name|indexNameExpressionResolver
argument_list|,
name|MultiPercolateRequest
operator|::
operator|new
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
name|aggParsers
operator|=
name|aggParsers
expr_stmt|;
name|this
operator|.
name|parseFieldMatcher
operator|=
operator|new
name|ParseFieldMatcher
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|queryRegistry
operator|=
name|queryRegistry
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doExecute
specifier|protected
name|void
name|doExecute
parameter_list|(
name|MultiPercolateRequest
name|request
parameter_list|,
name|ActionListener
argument_list|<
name|MultiPercolateResponse
argument_list|>
name|listener
parameter_list|)
block|{
name|List
argument_list|<
name|Tuple
argument_list|<
name|Integer
argument_list|,
name|GetRequest
argument_list|>
argument_list|>
name|getRequests
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|request
operator|.
name|requests
argument_list|()
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|GetRequest
name|getRequest
init|=
name|request
operator|.
name|requests
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getRequest
argument_list|()
decl_stmt|;
if|if
condition|(
name|getRequest
operator|!=
literal|null
condition|)
block|{
name|getRequests
operator|.
name|add
argument_list|(
operator|new
name|Tuple
argument_list|<>
argument_list|(
name|i
argument_list|,
name|getRequest
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|getRequests
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|innerDoExecute
argument_list|(
name|request
argument_list|,
name|listener
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|,
operator|new
name|HashMap
argument_list|<>
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|MultiGetRequest
name|multiGetRequest
init|=
operator|new
name|MultiGetRequest
argument_list|()
decl_stmt|;
for|for
control|(
name|Tuple
argument_list|<
name|Integer
argument_list|,
name|GetRequest
argument_list|>
name|tuple
range|:
name|getRequests
control|)
block|{
name|GetRequest
name|getRequest
init|=
name|tuple
operator|.
name|v2
argument_list|()
decl_stmt|;
name|multiGetRequest
operator|.
name|add
argument_list|(
operator|new
name|MultiGetRequest
operator|.
name|Item
argument_list|(
name|getRequest
operator|.
name|index
argument_list|()
argument_list|,
name|getRequest
operator|.
name|type
argument_list|()
argument_list|,
name|getRequest
operator|.
name|id
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|client
operator|.
name|multiGet
argument_list|(
name|multiGetRequest
argument_list|,
operator|new
name|ActionListener
argument_list|<
name|MultiGetResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onResponse
parameter_list|(
name|MultiGetResponse
name|response
parameter_list|)
block|{
name|Map
argument_list|<
name|Integer
argument_list|,
name|BytesReference
argument_list|>
name|getResponseSources
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|(
name|response
operator|.
name|getResponses
argument_list|()
operator|.
name|length
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|Integer
argument_list|,
name|MultiPercolateResponse
operator|.
name|Item
argument_list|>
name|preFailures
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|response
operator|.
name|getResponses
argument_list|()
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|MultiGetItemResponse
name|itemResponse
init|=
name|response
operator|.
name|getResponses
argument_list|()
index|[
name|i
index|]
decl_stmt|;
name|int
name|originalSlot
init|=
name|getRequests
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|v1
argument_list|()
decl_stmt|;
if|if
condition|(
name|itemResponse
operator|.
name|isFailed
argument_list|()
condition|)
block|{
name|preFailures
operator|.
name|put
argument_list|(
name|originalSlot
argument_list|,
operator|new
name|MultiPercolateResponse
operator|.
name|Item
argument_list|(
name|itemResponse
operator|.
name|getFailure
argument_list|()
operator|.
name|getFailure
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|itemResponse
operator|.
name|getResponse
argument_list|()
operator|.
name|isExists
argument_list|()
condition|)
block|{
name|getResponseSources
operator|.
name|put
argument_list|(
name|originalSlot
argument_list|,
name|itemResponse
operator|.
name|getResponse
argument_list|()
operator|.
name|getSourceAsBytesRef
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|GetRequest
name|getRequest
init|=
name|getRequests
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|v2
argument_list|()
decl_stmt|;
name|preFailures
operator|.
name|put
argument_list|(
name|originalSlot
argument_list|,
operator|new
name|MultiPercolateResponse
operator|.
name|Item
argument_list|(
operator|new
name|ResourceNotFoundException
argument_list|(
literal|"percolate document [{}/{}/{}] doesn't exist"
argument_list|,
name|getRequest
operator|.
name|index
argument_list|()
argument_list|,
name|getRequest
operator|.
name|type
argument_list|()
argument_list|,
name|getRequest
operator|.
name|id
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|innerDoExecute
argument_list|(
name|request
argument_list|,
name|listener
argument_list|,
name|getResponseSources
argument_list|,
name|preFailures
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
block|}
DECL|method|innerDoExecute
specifier|private
name|void
name|innerDoExecute
parameter_list|(
name|MultiPercolateRequest
name|request
parameter_list|,
name|ActionListener
argument_list|<
name|MultiPercolateResponse
argument_list|>
name|listener
parameter_list|,
name|Map
argument_list|<
name|Integer
argument_list|,
name|BytesReference
argument_list|>
name|getResponseSources
parameter_list|,
name|Map
argument_list|<
name|Integer
argument_list|,
name|MultiPercolateResponse
operator|.
name|Item
argument_list|>
name|preFailures
parameter_list|)
block|{
try|try
block|{
name|MultiSearchRequest
name|multiSearchRequest
init|=
name|createMultiSearchRequest
argument_list|(
name|request
argument_list|,
name|getResponseSources
argument_list|,
name|preFailures
argument_list|)
decl_stmt|;
if|if
condition|(
name|multiSearchRequest
operator|.
name|requests
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
comment|// we may failed to turn all percolate requests into search requests,
comment|// in that case just return the response...
name|listener
operator|.
name|onResponse
argument_list|(
name|createMultiPercolateResponse
argument_list|(
operator|new
name|MultiSearchResponse
argument_list|(
operator|new
name|MultiSearchResponse
operator|.
name|Item
index|[
literal|0
index|]
argument_list|)
argument_list|,
name|request
argument_list|,
name|preFailures
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|client
operator|.
name|multiSearch
argument_list|(
name|multiSearchRequest
argument_list|,
operator|new
name|ActionListener
argument_list|<
name|MultiSearchResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onResponse
parameter_list|(
name|MultiSearchResponse
name|response
parameter_list|)
block|{
try|try
block|{
name|listener
operator|.
name|onResponse
argument_list|(
name|createMultiPercolateResponse
argument_list|(
name|response
argument_list|,
name|request
argument_list|,
name|preFailures
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
name|onFailure
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
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
block|}
catch|catch
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
DECL|method|createMultiSearchRequest
specifier|private
name|MultiSearchRequest
name|createMultiSearchRequest
parameter_list|(
name|MultiPercolateRequest
name|multiPercolateRequest
parameter_list|,
name|Map
argument_list|<
name|Integer
argument_list|,
name|BytesReference
argument_list|>
name|getResponseSources
parameter_list|,
name|Map
argument_list|<
name|Integer
argument_list|,
name|MultiPercolateResponse
operator|.
name|Item
argument_list|>
name|preFailures
parameter_list|)
throws|throws
name|IOException
block|{
name|MultiSearchRequest
name|multiSearchRequest
init|=
operator|new
name|MultiSearchRequest
argument_list|()
decl_stmt|;
name|multiSearchRequest
operator|.
name|indicesOptions
argument_list|(
name|multiPercolateRequest
operator|.
name|indicesOptions
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|multiPercolateRequest
operator|.
name|requests
argument_list|()
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|preFailures
operator|.
name|keySet
argument_list|()
operator|.
name|contains
argument_list|(
name|i
argument_list|)
condition|)
block|{
continue|continue;
block|}
name|PercolateRequest
name|percolateRequest
init|=
name|multiPercolateRequest
operator|.
name|requests
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|BytesReference
name|docSource
init|=
name|getResponseSources
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
try|try
block|{
name|SearchRequest
name|searchRequest
init|=
name|TransportPercolateAction
operator|.
name|createSearchRequest
argument_list|(
name|percolateRequest
argument_list|,
name|docSource
argument_list|,
name|queryRegistry
argument_list|,
name|aggParsers
argument_list|,
name|parseFieldMatcher
argument_list|)
decl_stmt|;
name|multiSearchRequest
operator|.
name|add
argument_list|(
name|searchRequest
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|preFailures
operator|.
name|put
argument_list|(
name|i
argument_list|,
operator|new
name|MultiPercolateResponse
operator|.
name|Item
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|multiSearchRequest
return|;
block|}
DECL|method|createMultiPercolateResponse
specifier|private
name|MultiPercolateResponse
name|createMultiPercolateResponse
parameter_list|(
name|MultiSearchResponse
name|multiSearchResponse
parameter_list|,
name|MultiPercolateRequest
name|request
parameter_list|,
name|Map
argument_list|<
name|Integer
argument_list|,
name|MultiPercolateResponse
operator|.
name|Item
argument_list|>
name|preFailures
parameter_list|)
block|{
name|int
name|searchResponseIndex
init|=
literal|0
decl_stmt|;
name|MultiPercolateResponse
operator|.
name|Item
index|[]
name|percolateItems
init|=
operator|new
name|MultiPercolateResponse
operator|.
name|Item
index|[
name|request
operator|.
name|requests
argument_list|()
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|percolateItems
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|preFailures
operator|.
name|keySet
argument_list|()
operator|.
name|contains
argument_list|(
name|i
argument_list|)
condition|)
block|{
name|percolateItems
index|[
name|i
index|]
operator|=
name|preFailures
operator|.
name|get
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|MultiSearchResponse
operator|.
name|Item
name|searchItem
init|=
name|multiSearchResponse
operator|.
name|getResponses
argument_list|()
index|[
name|searchResponseIndex
operator|++
index|]
decl_stmt|;
if|if
condition|(
name|searchItem
operator|.
name|isFailure
argument_list|()
condition|)
block|{
name|percolateItems
index|[
name|i
index|]
operator|=
operator|new
name|MultiPercolateResponse
operator|.
name|Item
argument_list|(
name|searchItem
operator|.
name|getFailure
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|PercolateRequest
name|percolateRequest
init|=
name|request
operator|.
name|requests
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|percolateItems
index|[
name|i
index|]
operator|=
operator|new
name|MultiPercolateResponse
operator|.
name|Item
argument_list|(
name|TransportPercolateAction
operator|.
name|createPercolateResponse
argument_list|(
name|searchItem
operator|.
name|getResponse
argument_list|()
argument_list|,
name|percolateRequest
operator|.
name|onlyCount
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
operator|new
name|MultiPercolateResponse
argument_list|(
name|percolateItems
argument_list|)
return|;
block|}
block|}
end_class

end_unit

