begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.client
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|client
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
name|*
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
name|BulkRequest
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
name|BulkRequestBuilder
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
name|count
operator|.
name|CountRequest
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
name|count
operator|.
name|CountRequestBuilder
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
name|count
operator|.
name|CountResponse
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
name|delete
operator|.
name|DeleteRequest
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
name|delete
operator|.
name|DeleteRequestBuilder
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
name|delete
operator|.
name|DeleteResponse
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
name|deletebyquery
operator|.
name|DeleteByQueryRequest
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
name|deletebyquery
operator|.
name|DeleteByQueryRequestBuilder
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
name|deletebyquery
operator|.
name|DeleteByQueryResponse
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
name|*
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
name|action
operator|.
name|mlt
operator|.
name|MoreLikeThisRequest
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
name|mlt
operator|.
name|MoreLikeThisRequestBuilder
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
name|percolate
operator|.
name|PercolateRequest
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
name|percolate
operator|.
name|PercolateRequestBuilder
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
name|percolate
operator|.
name|PercolateResponse
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
name|*
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
name|update
operator|.
name|UpdateRequest
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
name|update
operator|.
name|UpdateRequestBuilder
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
name|update
operator|.
name|UpdateResponse
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
name|Nullable
import|;
end_import

begin_comment
comment|/**  * A client provides a one stop interface for performing actions/operations against the cluster.  *<p/>  *<p>All operations performed are asynchronous by nature. Each action/operation has two flavors, the first  * simply returns an {@link org.elasticsearch.action.ActionFuture}, while the second accepts an  * {@link org.elasticsearch.action.ActionListener}.  *<p/>  *<p>A client can either be retrieved from a {@link org.elasticsearch.node.Node} started, or connected remotely  * to one or more nodes using {@link org.elasticsearch.client.transport.TransportClient}.  *  * @see org.elasticsearch.node.Node#client()  * @see org.elasticsearch.client.transport.TransportClient  */
end_comment

begin_interface
DECL|interface|Client
specifier|public
interface|interface
name|Client
block|{
comment|/**      * Closes the client.      */
DECL|method|close
name|void
name|close
parameter_list|()
function_decl|;
comment|/**      * The admin client that can be used to perform administrative operations.      */
DECL|method|admin
name|AdminClient
name|admin
parameter_list|()
function_decl|;
comment|/**      * Executes a generic action, denoted by an {@link Action}.      *      * @param action           The action type to execute.      * @param request          The action request.      * @param<Request>        Teh request type.      * @param<Response>       the response type.      * @param<RequestBuilder> The request builder type.      * @return A future allowing to get back the response.      */
DECL|method|execute
parameter_list|<
name|Request
extends|extends
name|ActionRequest
parameter_list|,
name|Response
extends|extends
name|ActionResponse
parameter_list|,
name|RequestBuilder
extends|extends
name|ActionRequestBuilder
argument_list|<
name|Request
argument_list|,
name|Response
argument_list|>
parameter_list|>
name|ActionFuture
argument_list|<
name|Response
argument_list|>
name|execute
parameter_list|(
specifier|final
name|Action
argument_list|<
name|Request
argument_list|,
name|Response
argument_list|,
name|RequestBuilder
argument_list|>
name|action
parameter_list|,
specifier|final
name|Request
name|request
parameter_list|)
function_decl|;
comment|/**      * Executes a generic action, denoted by an {@link Action}.      *      * @param action           The action type to execute.      * @param request          Teh action request.      * @param listener         The listener to receive the response back.      * @param<Request>        The request type.      * @param<Response>       The response type.      * @param<RequestBuilder> The request builder type.      */
DECL|method|execute
parameter_list|<
name|Request
extends|extends
name|ActionRequest
parameter_list|,
name|Response
extends|extends
name|ActionResponse
parameter_list|,
name|RequestBuilder
extends|extends
name|ActionRequestBuilder
argument_list|<
name|Request
argument_list|,
name|Response
argument_list|>
parameter_list|>
name|void
name|execute
parameter_list|(
specifier|final
name|Action
argument_list|<
name|Request
argument_list|,
name|Response
argument_list|,
name|RequestBuilder
argument_list|>
name|action
parameter_list|,
specifier|final
name|Request
name|request
parameter_list|,
name|ActionListener
argument_list|<
name|Response
argument_list|>
name|listener
parameter_list|)
function_decl|;
comment|/**      * Prepares a request builder to execute, specified by {@link Action}.      *      * @param action           The action type to execute.      * @param<Request>        The request type.      * @param<Response>       The response type.      * @param<RequestBuilder> The request builder.      * @return The request builder, that can, at a later stage, execute the request.      */
DECL|method|prepareExecute
parameter_list|<
name|Request
extends|extends
name|ActionRequest
parameter_list|,
name|Response
extends|extends
name|ActionResponse
parameter_list|,
name|RequestBuilder
extends|extends
name|ActionRequestBuilder
argument_list|<
name|Request
argument_list|,
name|Response
argument_list|>
parameter_list|>
name|RequestBuilder
name|prepareExecute
parameter_list|(
specifier|final
name|Action
argument_list|<
name|Request
argument_list|,
name|Response
argument_list|,
name|RequestBuilder
argument_list|>
name|action
parameter_list|)
function_decl|;
comment|/**      * Index a JSON source associated with a given index and type.      *<p/>      *<p>The id is optional, if it is not provided, one will be generated automatically.      *      * @param request The index request      * @return The result future      * @see Requests#indexRequest(String)      */
DECL|method|index
name|ActionFuture
argument_list|<
name|IndexResponse
argument_list|>
name|index
parameter_list|(
name|IndexRequest
name|request
parameter_list|)
function_decl|;
comment|/**      * Index a document associated with a given index and type.      *<p/>      *<p>The id is optional, if it is not provided, one will be generated automatically.      *      * @param request  The index request      * @param listener A listener to be notified with a result      * @see Requests#indexRequest(String)      */
DECL|method|index
name|void
name|index
parameter_list|(
name|IndexRequest
name|request
parameter_list|,
name|ActionListener
argument_list|<
name|IndexResponse
argument_list|>
name|listener
parameter_list|)
function_decl|;
comment|/**      * Index a document associated with a given index and type.      *<p/>      *<p>The id is optional, if it is not provided, one will be generated automatically.      */
DECL|method|prepareIndex
name|IndexRequestBuilder
name|prepareIndex
parameter_list|()
function_decl|;
comment|/**      * Updates a document based on a script.      *      * @param request The update request      * @return The result future      */
DECL|method|update
name|ActionFuture
argument_list|<
name|UpdateResponse
argument_list|>
name|update
parameter_list|(
name|UpdateRequest
name|request
parameter_list|)
function_decl|;
comment|/**      * Updates a document based on a script.      *      * @param request  The update request      * @param listener A listener to be notified with a result      */
DECL|method|update
name|void
name|update
parameter_list|(
name|UpdateRequest
name|request
parameter_list|,
name|ActionListener
argument_list|<
name|UpdateResponse
argument_list|>
name|listener
parameter_list|)
function_decl|;
comment|/**      * Updates a document based on a script.      */
DECL|method|prepareUpdate
name|UpdateRequestBuilder
name|prepareUpdate
parameter_list|()
function_decl|;
comment|/**      * Updates a document based on a script.      */
DECL|method|prepareUpdate
name|UpdateRequestBuilder
name|prepareUpdate
parameter_list|(
name|String
name|index
parameter_list|,
name|String
name|type
parameter_list|,
name|String
name|id
parameter_list|)
function_decl|;
comment|/**      * Index a document associated with a given index and type.      *<p/>      *<p>The id is optional, if it is not provided, one will be generated automatically.      *      * @param index The index to index the document to      * @param type  The type to index the document to      */
DECL|method|prepareIndex
name|IndexRequestBuilder
name|prepareIndex
parameter_list|(
name|String
name|index
parameter_list|,
name|String
name|type
parameter_list|)
function_decl|;
comment|/**      * Index a document associated with a given index and type.      *<p/>      *<p>The id is optional, if it is not provided, one will be generated automatically.      *      * @param index The index to index the document to      * @param type  The type to index the document to      * @param id    The id of the document      */
DECL|method|prepareIndex
name|IndexRequestBuilder
name|prepareIndex
parameter_list|(
name|String
name|index
parameter_list|,
name|String
name|type
parameter_list|,
annotation|@
name|Nullable
name|String
name|id
parameter_list|)
function_decl|;
comment|/**      * Deletes a document from the index based on the index, type and id.      *      * @param request The delete request      * @return The result future      * @see Requests#deleteRequest(String)      */
DECL|method|delete
name|ActionFuture
argument_list|<
name|DeleteResponse
argument_list|>
name|delete
parameter_list|(
name|DeleteRequest
name|request
parameter_list|)
function_decl|;
comment|/**      * Deletes a document from the index based on the index, type and id.      *      * @param request  The delete request      * @param listener A listener to be notified with a result      * @see Requests#deleteRequest(String)      */
DECL|method|delete
name|void
name|delete
parameter_list|(
name|DeleteRequest
name|request
parameter_list|,
name|ActionListener
argument_list|<
name|DeleteResponse
argument_list|>
name|listener
parameter_list|)
function_decl|;
comment|/**      * Deletes a document from the index based on the index, type and id.      */
DECL|method|prepareDelete
name|DeleteRequestBuilder
name|prepareDelete
parameter_list|()
function_decl|;
comment|/**      * Deletes a document from the index based on the index, type and id.      *      * @param index The index to delete the document from      * @param type  The type of the document to delete      * @param id    The id of the document to delete      */
DECL|method|prepareDelete
name|DeleteRequestBuilder
name|prepareDelete
parameter_list|(
name|String
name|index
parameter_list|,
name|String
name|type
parameter_list|,
name|String
name|id
parameter_list|)
function_decl|;
comment|/**      * Executes a bulk of index / delete operations.      *      * @param request The bulk request      * @return The result future      * @see org.elasticsearch.client.Requests#bulkRequest()      */
DECL|method|bulk
name|ActionFuture
argument_list|<
name|BulkResponse
argument_list|>
name|bulk
parameter_list|(
name|BulkRequest
name|request
parameter_list|)
function_decl|;
comment|/**      * Executes a bulk of index / delete operations.      *      * @param request  The bulk request      * @param listener A listener to be notified with a result      * @see org.elasticsearch.client.Requests#bulkRequest()      */
DECL|method|bulk
name|void
name|bulk
parameter_list|(
name|BulkRequest
name|request
parameter_list|,
name|ActionListener
argument_list|<
name|BulkResponse
argument_list|>
name|listener
parameter_list|)
function_decl|;
comment|/**      * Executes a bulk of index / delete operations.      */
DECL|method|prepareBulk
name|BulkRequestBuilder
name|prepareBulk
parameter_list|()
function_decl|;
comment|/**      * Deletes all documents from one or more indices based on a query.      *      * @param request The delete by query request      * @return The result future      * @see Requests#deleteByQueryRequest(String...)      */
DECL|method|deleteByQuery
name|ActionFuture
argument_list|<
name|DeleteByQueryResponse
argument_list|>
name|deleteByQuery
parameter_list|(
name|DeleteByQueryRequest
name|request
parameter_list|)
function_decl|;
comment|/**      * Deletes all documents from one or more indices based on a query.      *      * @param request  The delete by query request      * @param listener A listener to be notified with a result      * @see Requests#deleteByQueryRequest(String...)      */
DECL|method|deleteByQuery
name|void
name|deleteByQuery
parameter_list|(
name|DeleteByQueryRequest
name|request
parameter_list|,
name|ActionListener
argument_list|<
name|DeleteByQueryResponse
argument_list|>
name|listener
parameter_list|)
function_decl|;
comment|/**      * Deletes all documents from one or more indices based on a query.      */
DECL|method|prepareDeleteByQuery
name|DeleteByQueryRequestBuilder
name|prepareDeleteByQuery
parameter_list|(
name|String
modifier|...
name|indices
parameter_list|)
function_decl|;
comment|/**      * Gets the document that was indexed from an index with a type and id.      *      * @param request The get request      * @return The result future      * @see Requests#getRequest(String)      */
DECL|method|get
name|ActionFuture
argument_list|<
name|GetResponse
argument_list|>
name|get
parameter_list|(
name|GetRequest
name|request
parameter_list|)
function_decl|;
comment|/**      * Gets the document that was indexed from an index with a type and id.      *      * @param request  The get request      * @param listener A listener to be notified with a result      * @see Requests#getRequest(String)      */
DECL|method|get
name|void
name|get
parameter_list|(
name|GetRequest
name|request
parameter_list|,
name|ActionListener
argument_list|<
name|GetResponse
argument_list|>
name|listener
parameter_list|)
function_decl|;
comment|/**      * Gets the document that was indexed from an index with a type and id.      */
DECL|method|prepareGet
name|GetRequestBuilder
name|prepareGet
parameter_list|()
function_decl|;
comment|/**      * Gets the document that was indexed from an index with a type (optional) and id.      */
DECL|method|prepareGet
name|GetRequestBuilder
name|prepareGet
parameter_list|(
name|String
name|index
parameter_list|,
annotation|@
name|Nullable
name|String
name|type
parameter_list|,
name|String
name|id
parameter_list|)
function_decl|;
comment|/**      * Multi get documents.      */
DECL|method|multiGet
name|ActionFuture
argument_list|<
name|MultiGetResponse
argument_list|>
name|multiGet
parameter_list|(
name|MultiGetRequest
name|request
parameter_list|)
function_decl|;
comment|/**      * Multi get documents.      */
DECL|method|multiGet
name|void
name|multiGet
parameter_list|(
name|MultiGetRequest
name|request
parameter_list|,
name|ActionListener
argument_list|<
name|MultiGetResponse
argument_list|>
name|listener
parameter_list|)
function_decl|;
comment|/**      * Multi get documents.      */
DECL|method|prepareMultiGet
name|MultiGetRequestBuilder
name|prepareMultiGet
parameter_list|()
function_decl|;
comment|/**      * A count of all the documents matching a specific query.      *      * @param request The count request      * @return The result future      * @see Requests#countRequest(String...)      */
DECL|method|count
name|ActionFuture
argument_list|<
name|CountResponse
argument_list|>
name|count
parameter_list|(
name|CountRequest
name|request
parameter_list|)
function_decl|;
comment|/**      * A count of all the documents matching a specific query.      *      * @param request  The count request      * @param listener A listener to be notified of the result      * @see Requests#countRequest(String...)      */
DECL|method|count
name|void
name|count
parameter_list|(
name|CountRequest
name|request
parameter_list|,
name|ActionListener
argument_list|<
name|CountResponse
argument_list|>
name|listener
parameter_list|)
function_decl|;
comment|/**      * A count of all the documents matching a specific query.      */
DECL|method|prepareCount
name|CountRequestBuilder
name|prepareCount
parameter_list|(
name|String
modifier|...
name|indices
parameter_list|)
function_decl|;
comment|/**      * Search across one or more indices and one or more types with a query.      *      * @param request The search request      * @return The result future      * @see Requests#searchRequest(String...)      */
DECL|method|search
name|ActionFuture
argument_list|<
name|SearchResponse
argument_list|>
name|search
parameter_list|(
name|SearchRequest
name|request
parameter_list|)
function_decl|;
comment|/**      * Search across one or more indices and one or more types with a query.      *      * @param request  The search request      * @param listener A listener to be notified of the result      * @see Requests#searchRequest(String...)      */
DECL|method|search
name|void
name|search
parameter_list|(
name|SearchRequest
name|request
parameter_list|,
name|ActionListener
argument_list|<
name|SearchResponse
argument_list|>
name|listener
parameter_list|)
function_decl|;
comment|/**      * Search across one or more indices and one or more types with a query.      */
DECL|method|prepareSearch
name|SearchRequestBuilder
name|prepareSearch
parameter_list|(
name|String
modifier|...
name|indices
parameter_list|)
function_decl|;
comment|/**      * A search scroll request to continue searching a previous scrollable search request.      *      * @param request The search scroll request      * @return The result future      * @see Requests#searchScrollRequest(String)      */
DECL|method|searchScroll
name|ActionFuture
argument_list|<
name|SearchResponse
argument_list|>
name|searchScroll
parameter_list|(
name|SearchScrollRequest
name|request
parameter_list|)
function_decl|;
comment|/**      * A search scroll request to continue searching a previous scrollable search request.      *      * @param request  The search scroll request      * @param listener A listener to be notified of the result      * @see Requests#searchScrollRequest(String)      */
DECL|method|searchScroll
name|void
name|searchScroll
parameter_list|(
name|SearchScrollRequest
name|request
parameter_list|,
name|ActionListener
argument_list|<
name|SearchResponse
argument_list|>
name|listener
parameter_list|)
function_decl|;
comment|/**      * A search scroll request to continue searching a previous scrollable search request.      */
DECL|method|prepareSearchScroll
name|SearchScrollRequestBuilder
name|prepareSearchScroll
parameter_list|(
name|String
name|scrollId
parameter_list|)
function_decl|;
comment|/**      * A more like this action to search for documents that are "like" a specific document.      *      * @param request The more like this request      * @return The response future      */
DECL|method|moreLikeThis
name|ActionFuture
argument_list|<
name|SearchResponse
argument_list|>
name|moreLikeThis
parameter_list|(
name|MoreLikeThisRequest
name|request
parameter_list|)
function_decl|;
comment|/**      * A more like this action to search for documents that are "like" a specific document.      *      * @param request  The more like this request      * @param listener A listener to be notified of the result      */
DECL|method|moreLikeThis
name|void
name|moreLikeThis
parameter_list|(
name|MoreLikeThisRequest
name|request
parameter_list|,
name|ActionListener
argument_list|<
name|SearchResponse
argument_list|>
name|listener
parameter_list|)
function_decl|;
comment|/**      * A more like this action to search for documents that are "like" a specific document.      *      * @param index The index to load the document from      * @param type  The type of the document      * @param id    The id of the document      */
DECL|method|prepareMoreLikeThis
name|MoreLikeThisRequestBuilder
name|prepareMoreLikeThis
parameter_list|(
name|String
name|index
parameter_list|,
name|String
name|type
parameter_list|,
name|String
name|id
parameter_list|)
function_decl|;
comment|/**      * Percolates a request returning the matches documents.      */
DECL|method|percolate
name|ActionFuture
argument_list|<
name|PercolateResponse
argument_list|>
name|percolate
parameter_list|(
name|PercolateRequest
name|request
parameter_list|)
function_decl|;
comment|/**      * Percolates a request returning the matches documents.      */
DECL|method|percolate
name|void
name|percolate
parameter_list|(
name|PercolateRequest
name|request
parameter_list|,
name|ActionListener
argument_list|<
name|PercolateResponse
argument_list|>
name|listener
parameter_list|)
function_decl|;
comment|/**      * Percolates a request returning the matches documents.      *      * @param index The index to percolate the doc      * @param type  The type of the doc      */
DECL|method|preparePercolate
name|PercolateRequestBuilder
name|preparePercolate
parameter_list|(
name|String
name|index
parameter_list|,
name|String
name|type
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

