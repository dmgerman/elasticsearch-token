begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|apache
operator|.
name|http
operator|.
name|Header
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|HttpEntity
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
name|ElasticsearchStatusException
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
name|ActionRequestValidationException
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
name|GetResponse
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
name|main
operator|.
name|MainRequest
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
name|CheckedFunction
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
name|NamedXContentRegistry
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
name|XContentParser
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
name|rest
operator|.
name|BytesRestResponse
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
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Objects
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
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
name|Function
import|;
end_import

begin_comment
comment|/**  * High level REST client that wraps an instance of the low level {@link RestClient} and allows to build requests and read responses.  * The provided {@link RestClient} is externally built and closed.  */
end_comment

begin_class
DECL|class|RestHighLevelClient
specifier|public
class|class
name|RestHighLevelClient
block|{
DECL|field|client
specifier|private
specifier|final
name|RestClient
name|client
decl_stmt|;
DECL|method|RestHighLevelClient
specifier|public
name|RestHighLevelClient
parameter_list|(
name|RestClient
name|client
parameter_list|)
block|{
name|this
operator|.
name|client
operator|=
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|client
argument_list|)
expr_stmt|;
block|}
comment|/**      * Pings the remote Elasticsearch cluster and returns true if the ping succeeded, false otherwise      */
DECL|method|ping
specifier|public
name|boolean
name|ping
parameter_list|(
name|Header
modifier|...
name|headers
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|performRequest
argument_list|(
operator|new
name|MainRequest
argument_list|()
argument_list|,
parameter_list|(
name|request
parameter_list|)
lambda|->
name|Request
operator|.
name|ping
argument_list|()
argument_list|,
name|RestHighLevelClient
operator|::
name|convertExistsResponse
argument_list|,
name|Collections
operator|.
name|emptySet
argument_list|()
argument_list|,
name|headers
argument_list|)
return|;
block|}
comment|/**      * Retrieves a document by id using the get api      */
DECL|method|get
specifier|public
name|GetResponse
name|get
parameter_list|(
name|GetRequest
name|getRequest
parameter_list|,
name|Header
modifier|...
name|headers
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|performRequestAndParseEntity
argument_list|(
name|getRequest
argument_list|,
name|Request
operator|::
name|get
argument_list|,
name|GetResponse
operator|::
name|fromXContent
argument_list|,
name|Collections
operator|.
name|singleton
argument_list|(
literal|404
argument_list|)
argument_list|,
name|headers
argument_list|)
return|;
block|}
comment|/**      * Asynchronously retrieves a document by id using the get api      */
DECL|method|getAsync
specifier|public
name|void
name|getAsync
parameter_list|(
name|GetRequest
name|getRequest
parameter_list|,
name|ActionListener
argument_list|<
name|GetResponse
argument_list|>
name|listener
parameter_list|,
name|Header
modifier|...
name|headers
parameter_list|)
block|{
name|performRequestAsyncAndParseEntity
argument_list|(
name|getRequest
argument_list|,
name|Request
operator|::
name|get
argument_list|,
name|GetResponse
operator|::
name|fromXContent
argument_list|,
name|listener
argument_list|,
name|Collections
operator|.
name|singleton
argument_list|(
literal|404
argument_list|)
argument_list|,
name|headers
argument_list|)
expr_stmt|;
block|}
comment|/**      * Checks for the existence of a document. Returns true if it exists, false otherwise      */
DECL|method|exists
specifier|public
name|boolean
name|exists
parameter_list|(
name|GetRequest
name|getRequest
parameter_list|,
name|Header
modifier|...
name|headers
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|performRequest
argument_list|(
name|getRequest
argument_list|,
name|Request
operator|::
name|exists
argument_list|,
name|RestHighLevelClient
operator|::
name|convertExistsResponse
argument_list|,
name|Collections
operator|.
name|emptySet
argument_list|()
argument_list|,
name|headers
argument_list|)
return|;
block|}
comment|/**      * Asynchronously checks for the existence of a document. Returns true if it exists, false otherwise      */
DECL|method|existsAsync
specifier|public
name|void
name|existsAsync
parameter_list|(
name|GetRequest
name|getRequest
parameter_list|,
name|ActionListener
argument_list|<
name|Boolean
argument_list|>
name|listener
parameter_list|,
name|Header
modifier|...
name|headers
parameter_list|)
block|{
name|performRequestAsync
argument_list|(
name|getRequest
argument_list|,
name|Request
operator|::
name|exists
argument_list|,
name|RestHighLevelClient
operator|::
name|convertExistsResponse
argument_list|,
name|listener
argument_list|,
name|Collections
operator|.
name|emptySet
argument_list|()
argument_list|,
name|headers
argument_list|)
expr_stmt|;
block|}
DECL|method|performRequestAndParseEntity
specifier|private
parameter_list|<
name|Req
extends|extends
name|ActionRequest
parameter_list|,
name|Resp
parameter_list|>
name|Resp
name|performRequestAndParseEntity
parameter_list|(
name|Req
name|request
parameter_list|,
name|Function
argument_list|<
name|Req
argument_list|,
name|Request
argument_list|>
name|requestConverter
parameter_list|,
name|CheckedFunction
argument_list|<
name|XContentParser
argument_list|,
name|Resp
argument_list|,
name|IOException
argument_list|>
name|entityParser
parameter_list|,
name|Set
argument_list|<
name|Integer
argument_list|>
name|ignores
parameter_list|,
name|Header
modifier|...
name|headers
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|performRequest
argument_list|(
name|request
argument_list|,
name|requestConverter
argument_list|,
parameter_list|(
name|response
parameter_list|)
lambda|->
name|parseEntity
argument_list|(
name|response
operator|.
name|getEntity
argument_list|()
argument_list|,
name|entityParser
argument_list|)
argument_list|,
name|ignores
argument_list|,
name|headers
argument_list|)
return|;
block|}
DECL|method|performRequest
parameter_list|<
name|Req
extends|extends
name|ActionRequest
parameter_list|,
name|Resp
parameter_list|>
name|Resp
name|performRequest
parameter_list|(
name|Req
name|request
parameter_list|,
name|Function
argument_list|<
name|Req
argument_list|,
name|Request
argument_list|>
name|requestConverter
parameter_list|,
name|CheckedFunction
argument_list|<
name|Response
argument_list|,
name|Resp
argument_list|,
name|IOException
argument_list|>
name|responseConverter
parameter_list|,
name|Set
argument_list|<
name|Integer
argument_list|>
name|ignores
parameter_list|,
name|Header
modifier|...
name|headers
parameter_list|)
throws|throws
name|IOException
block|{
name|ActionRequestValidationException
name|validationException
init|=
name|request
operator|.
name|validate
argument_list|()
decl_stmt|;
if|if
condition|(
name|validationException
operator|!=
literal|null
condition|)
block|{
throw|throw
name|validationException
throw|;
block|}
name|Request
name|req
init|=
name|requestConverter
operator|.
name|apply
argument_list|(
name|request
argument_list|)
decl_stmt|;
name|Response
name|response
decl_stmt|;
try|try
block|{
name|response
operator|=
name|client
operator|.
name|performRequest
argument_list|(
name|req
operator|.
name|method
argument_list|,
name|req
operator|.
name|endpoint
argument_list|,
name|req
operator|.
name|params
argument_list|,
name|req
operator|.
name|entity
argument_list|,
name|headers
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ResponseException
name|e
parameter_list|)
block|{
if|if
condition|(
name|ignores
operator|.
name|contains
argument_list|(
name|e
operator|.
name|getResponse
argument_list|()
operator|.
name|getStatusLine
argument_list|()
operator|.
name|getStatusCode
argument_list|()
argument_list|)
condition|)
block|{
try|try
block|{
return|return
name|responseConverter
operator|.
name|apply
argument_list|(
name|e
operator|.
name|getResponse
argument_list|()
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|innerException
parameter_list|)
block|{
throw|throw
name|parseResponseException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
throw|throw
name|parseResponseException
argument_list|(
name|e
argument_list|)
throw|;
block|}
try|try
block|{
return|return
name|responseConverter
operator|.
name|apply
argument_list|(
name|response
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Unable to parse response body for "
operator|+
name|response
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
DECL|method|performRequestAsyncAndParseEntity
specifier|private
parameter_list|<
name|Req
extends|extends
name|ActionRequest
parameter_list|,
name|Resp
parameter_list|>
name|void
name|performRequestAsyncAndParseEntity
parameter_list|(
name|Req
name|request
parameter_list|,
name|Function
argument_list|<
name|Req
argument_list|,
name|Request
argument_list|>
name|requestConverter
parameter_list|,
name|CheckedFunction
argument_list|<
name|XContentParser
argument_list|,
name|Resp
argument_list|,
name|IOException
argument_list|>
name|entityParser
parameter_list|,
name|ActionListener
argument_list|<
name|Resp
argument_list|>
name|listener
parameter_list|,
name|Set
argument_list|<
name|Integer
argument_list|>
name|ignores
parameter_list|,
name|Header
modifier|...
name|headers
parameter_list|)
block|{
name|performRequestAsync
argument_list|(
name|request
argument_list|,
name|requestConverter
argument_list|,
parameter_list|(
name|response
parameter_list|)
lambda|->
name|parseEntity
argument_list|(
name|response
operator|.
name|getEntity
argument_list|()
argument_list|,
name|entityParser
argument_list|)
argument_list|,
name|listener
argument_list|,
name|ignores
argument_list|,
name|headers
argument_list|)
expr_stmt|;
block|}
DECL|method|performRequestAsync
parameter_list|<
name|Req
extends|extends
name|ActionRequest
parameter_list|,
name|Resp
parameter_list|>
name|void
name|performRequestAsync
parameter_list|(
name|Req
name|request
parameter_list|,
name|Function
argument_list|<
name|Req
argument_list|,
name|Request
argument_list|>
name|requestConverter
parameter_list|,
name|CheckedFunction
argument_list|<
name|Response
argument_list|,
name|Resp
argument_list|,
name|IOException
argument_list|>
name|responseConverter
parameter_list|,
name|ActionListener
argument_list|<
name|Resp
argument_list|>
name|listener
parameter_list|,
name|Set
argument_list|<
name|Integer
argument_list|>
name|ignores
parameter_list|,
name|Header
modifier|...
name|headers
parameter_list|)
block|{
name|ActionRequestValidationException
name|validationException
init|=
name|request
operator|.
name|validate
argument_list|()
decl_stmt|;
if|if
condition|(
name|validationException
operator|!=
literal|null
condition|)
block|{
name|listener
operator|.
name|onFailure
argument_list|(
name|validationException
argument_list|)
expr_stmt|;
return|return;
block|}
name|Request
name|req
init|=
name|requestConverter
operator|.
name|apply
argument_list|(
name|request
argument_list|)
decl_stmt|;
name|ResponseListener
name|responseListener
init|=
name|wrapResponseListener
argument_list|(
name|responseConverter
argument_list|,
name|listener
argument_list|,
name|ignores
argument_list|)
decl_stmt|;
name|client
operator|.
name|performRequestAsync
argument_list|(
name|req
operator|.
name|method
argument_list|,
name|req
operator|.
name|endpoint
argument_list|,
name|req
operator|.
name|params
argument_list|,
name|req
operator|.
name|entity
argument_list|,
name|responseListener
argument_list|,
name|headers
argument_list|)
expr_stmt|;
block|}
DECL|method|wrapResponseListener
specifier|static
parameter_list|<
name|Resp
parameter_list|>
name|ResponseListener
name|wrapResponseListener
parameter_list|(
name|CheckedFunction
argument_list|<
name|Response
argument_list|,
name|Resp
argument_list|,
name|IOException
argument_list|>
name|responseConverter
parameter_list|,
name|ActionListener
argument_list|<
name|Resp
argument_list|>
name|actionListener
parameter_list|,
name|Set
argument_list|<
name|Integer
argument_list|>
name|ignores
parameter_list|)
block|{
return|return
operator|new
name|ResponseListener
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onSuccess
parameter_list|(
name|Response
name|response
parameter_list|)
block|{
try|try
block|{
name|actionListener
operator|.
name|onResponse
argument_list|(
name|responseConverter
operator|.
name|apply
argument_list|(
name|response
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
name|IOException
name|ioe
init|=
operator|new
name|IOException
argument_list|(
literal|"Unable to parse response body for "
operator|+
name|response
argument_list|,
name|e
argument_list|)
decl_stmt|;
name|onFailure
argument_list|(
name|ioe
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
name|exception
parameter_list|)
block|{
if|if
condition|(
name|exception
operator|instanceof
name|ResponseException
condition|)
block|{
name|ResponseException
name|responseException
init|=
operator|(
name|ResponseException
operator|)
name|exception
decl_stmt|;
name|Response
name|response
init|=
name|responseException
operator|.
name|getResponse
argument_list|()
decl_stmt|;
if|if
condition|(
name|ignores
operator|.
name|contains
argument_list|(
name|response
operator|.
name|getStatusLine
argument_list|()
operator|.
name|getStatusCode
argument_list|()
argument_list|)
condition|)
block|{
try|try
block|{
name|actionListener
operator|.
name|onResponse
argument_list|(
name|responseConverter
operator|.
name|apply
argument_list|(
name|response
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|innerException
parameter_list|)
block|{
comment|//the exception is ignored as we now try to parse the response as an error.
comment|//this covers cases like get where 404 can either be a valid document not found response,
comment|//or an error for which parsing is completely different. We try to consider the 404 response as a valid one
comment|//first. If parsing of the response breaks, we fall back to parsing it as an error.
name|actionListener
operator|.
name|onFailure
argument_list|(
name|parseResponseException
argument_list|(
name|responseException
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|actionListener
operator|.
name|onFailure
argument_list|(
name|parseResponseException
argument_list|(
name|responseException
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|actionListener
operator|.
name|onFailure
argument_list|(
name|exception
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|;
block|}
comment|/**      * Converts a {@link ResponseException} obtained from the low level REST client into an {@link ElasticsearchException}.      * If a response body was returned, tries to parse it as an error returned from Elasticsearch.      * If no response body was returned or anything goes wrong while parsing the error, returns a new {@link ElasticsearchStatusException}      * that wraps the original {@link ResponseException}. The potential exception obtained while parsing is added to the returned      * exception as a suppressed exception. This method is guaranteed to not throw any exception eventually thrown while parsing.      */
DECL|method|parseResponseException
specifier|static
name|ElasticsearchStatusException
name|parseResponseException
parameter_list|(
name|ResponseException
name|responseException
parameter_list|)
block|{
name|Response
name|response
init|=
name|responseException
operator|.
name|getResponse
argument_list|()
decl_stmt|;
name|HttpEntity
name|entity
init|=
name|response
operator|.
name|getEntity
argument_list|()
decl_stmt|;
name|ElasticsearchStatusException
name|elasticsearchException
decl_stmt|;
if|if
condition|(
name|entity
operator|==
literal|null
condition|)
block|{
name|elasticsearchException
operator|=
operator|new
name|ElasticsearchStatusException
argument_list|(
name|responseException
operator|.
name|getMessage
argument_list|()
argument_list|,
name|RestStatus
operator|.
name|fromCode
argument_list|(
name|response
operator|.
name|getStatusLine
argument_list|()
operator|.
name|getStatusCode
argument_list|()
argument_list|)
argument_list|,
name|responseException
argument_list|)
expr_stmt|;
block|}
else|else
block|{
try|try
block|{
name|elasticsearchException
operator|=
name|parseEntity
argument_list|(
name|entity
argument_list|,
name|BytesRestResponse
operator|::
name|errorFromXContent
argument_list|)
expr_stmt|;
name|elasticsearchException
operator|.
name|addSuppressed
argument_list|(
name|responseException
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|RestStatus
name|restStatus
init|=
name|RestStatus
operator|.
name|fromCode
argument_list|(
name|response
operator|.
name|getStatusLine
argument_list|()
operator|.
name|getStatusCode
argument_list|()
argument_list|)
decl_stmt|;
name|elasticsearchException
operator|=
operator|new
name|ElasticsearchStatusException
argument_list|(
literal|"Unable to parse response body"
argument_list|,
name|restStatus
argument_list|,
name|responseException
argument_list|)
expr_stmt|;
name|elasticsearchException
operator|.
name|addSuppressed
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|elasticsearchException
return|;
block|}
DECL|method|parseEntity
specifier|static
parameter_list|<
name|Resp
parameter_list|>
name|Resp
name|parseEntity
parameter_list|(
name|HttpEntity
name|entity
parameter_list|,
name|CheckedFunction
argument_list|<
name|XContentParser
argument_list|,
name|Resp
argument_list|,
name|IOException
argument_list|>
name|entityParser
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|entity
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Response body expected but not returned"
argument_list|)
throw|;
block|}
if|if
condition|(
name|entity
operator|.
name|getContentType
argument_list|()
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Elasticsearch didn't return the [Content-Type] header, unable to parse response body"
argument_list|)
throw|;
block|}
name|XContentType
name|xContentType
init|=
name|XContentType
operator|.
name|fromMediaTypeOrFormat
argument_list|(
name|entity
operator|.
name|getContentType
argument_list|()
operator|.
name|getValue
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|xContentType
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Unsupported Content-Type: "
operator|+
name|entity
operator|.
name|getContentType
argument_list|()
operator|.
name|getValue
argument_list|()
argument_list|)
throw|;
block|}
try|try
init|(
name|XContentParser
name|parser
init|=
name|xContentType
operator|.
name|xContent
argument_list|()
operator|.
name|createParser
argument_list|(
name|NamedXContentRegistry
operator|.
name|EMPTY
argument_list|,
name|entity
operator|.
name|getContent
argument_list|()
argument_list|)
init|)
block|{
return|return
name|entityParser
operator|.
name|apply
argument_list|(
name|parser
argument_list|)
return|;
block|}
block|}
DECL|method|convertExistsResponse
specifier|static
name|boolean
name|convertExistsResponse
parameter_list|(
name|Response
name|response
parameter_list|)
block|{
return|return
name|response
operator|.
name|getStatusLine
argument_list|()
operator|.
name|getStatusCode
argument_list|()
operator|==
literal|200
return|;
block|}
block|}
end_class

end_unit

