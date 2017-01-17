begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.rest
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|rest
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
name|Logger
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
name|common
operator|.
name|bytes
operator|.
name|BytesArray
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
name|logging
operator|.
name|ESLoggerFactory
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
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|util
operator|.
name|Collections
operator|.
name|singletonMap
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchException
operator|.
name|REST_EXCEPTION_SKIP_STACK_TRACE
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchException
operator|.
name|REST_EXCEPTION_SKIP_STACK_TRACE_DEFAULT
import|;
end_import

begin_class
DECL|class|BytesRestResponse
specifier|public
class|class
name|BytesRestResponse
extends|extends
name|RestResponse
block|{
DECL|field|TEXT_CONTENT_TYPE
specifier|public
specifier|static
specifier|final
name|String
name|TEXT_CONTENT_TYPE
init|=
literal|"text/plain; charset=UTF-8"
decl_stmt|;
DECL|field|status
specifier|private
specifier|final
name|RestStatus
name|status
decl_stmt|;
DECL|field|content
specifier|private
specifier|final
name|BytesReference
name|content
decl_stmt|;
DECL|field|contentType
specifier|private
specifier|final
name|String
name|contentType
decl_stmt|;
comment|/**      * Creates a new response based on {@link XContentBuilder}.      */
DECL|method|BytesRestResponse
specifier|public
name|BytesRestResponse
parameter_list|(
name|RestStatus
name|status
parameter_list|,
name|XContentBuilder
name|builder
parameter_list|)
block|{
name|this
argument_list|(
name|status
argument_list|,
name|builder
operator|.
name|contentType
argument_list|()
operator|.
name|mediaType
argument_list|()
argument_list|,
name|builder
operator|.
name|bytes
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**      * Creates a new plain text response.      */
DECL|method|BytesRestResponse
specifier|public
name|BytesRestResponse
parameter_list|(
name|RestStatus
name|status
parameter_list|,
name|String
name|content
parameter_list|)
block|{
name|this
argument_list|(
name|status
argument_list|,
name|TEXT_CONTENT_TYPE
argument_list|,
operator|new
name|BytesArray
argument_list|(
name|content
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**      * Creates a new plain text response.      */
DECL|method|BytesRestResponse
specifier|public
name|BytesRestResponse
parameter_list|(
name|RestStatus
name|status
parameter_list|,
name|String
name|contentType
parameter_list|,
name|String
name|content
parameter_list|)
block|{
name|this
argument_list|(
name|status
argument_list|,
name|contentType
argument_list|,
operator|new
name|BytesArray
argument_list|(
name|content
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**      * Creates a binary response.      */
DECL|method|BytesRestResponse
specifier|public
name|BytesRestResponse
parameter_list|(
name|RestStatus
name|status
parameter_list|,
name|String
name|contentType
parameter_list|,
name|byte
index|[]
name|content
parameter_list|)
block|{
name|this
argument_list|(
name|status
argument_list|,
name|contentType
argument_list|,
operator|new
name|BytesArray
argument_list|(
name|content
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**      * Creates a binary response.      */
DECL|method|BytesRestResponse
specifier|public
name|BytesRestResponse
parameter_list|(
name|RestStatus
name|status
parameter_list|,
name|String
name|contentType
parameter_list|,
name|BytesReference
name|content
parameter_list|)
block|{
name|this
operator|.
name|status
operator|=
name|status
expr_stmt|;
name|this
operator|.
name|content
operator|=
name|content
expr_stmt|;
name|this
operator|.
name|contentType
operator|=
name|contentType
expr_stmt|;
block|}
DECL|method|BytesRestResponse
specifier|public
name|BytesRestResponse
parameter_list|(
name|RestChannel
name|channel
parameter_list|,
name|Exception
name|e
parameter_list|)
throws|throws
name|IOException
block|{
name|this
argument_list|(
name|channel
argument_list|,
name|ExceptionsHelper
operator|.
name|status
argument_list|(
name|e
argument_list|)
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
DECL|method|BytesRestResponse
specifier|public
name|BytesRestResponse
parameter_list|(
name|RestChannel
name|channel
parameter_list|,
name|RestStatus
name|status
parameter_list|,
name|Exception
name|e
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|status
operator|=
name|status
expr_stmt|;
if|if
condition|(
name|channel
operator|.
name|request
argument_list|()
operator|.
name|method
argument_list|()
operator|==
name|RestRequest
operator|.
name|Method
operator|.
name|HEAD
condition|)
block|{
name|this
operator|.
name|content
operator|=
name|BytesArray
operator|.
name|EMPTY
expr_stmt|;
name|this
operator|.
name|contentType
operator|=
name|TEXT_CONTENT_TYPE
expr_stmt|;
block|}
else|else
block|{
try|try
init|(
specifier|final
name|XContentBuilder
name|builder
init|=
name|build
argument_list|(
name|channel
argument_list|,
name|status
argument_list|,
name|e
argument_list|)
init|)
block|{
name|this
operator|.
name|content
operator|=
name|builder
operator|.
name|bytes
argument_list|()
expr_stmt|;
name|this
operator|.
name|contentType
operator|=
name|builder
operator|.
name|contentType
argument_list|()
operator|.
name|mediaType
argument_list|()
expr_stmt|;
block|}
block|}
if|if
condition|(
name|e
operator|instanceof
name|ElasticsearchException
condition|)
block|{
name|copyHeaders
argument_list|(
operator|(
operator|(
name|ElasticsearchException
operator|)
name|e
operator|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|contentType
specifier|public
name|String
name|contentType
parameter_list|()
block|{
return|return
name|this
operator|.
name|contentType
return|;
block|}
annotation|@
name|Override
DECL|method|content
specifier|public
name|BytesReference
name|content
parameter_list|()
block|{
return|return
name|this
operator|.
name|content
return|;
block|}
annotation|@
name|Override
DECL|method|status
specifier|public
name|RestStatus
name|status
parameter_list|()
block|{
return|return
name|this
operator|.
name|status
return|;
block|}
DECL|field|SUPPRESSED_ERROR_LOGGER
specifier|private
specifier|static
specifier|final
name|Logger
name|SUPPRESSED_ERROR_LOGGER
init|=
name|ESLoggerFactory
operator|.
name|getLogger
argument_list|(
literal|"rest.suppressed"
argument_list|)
decl_stmt|;
DECL|method|build
specifier|private
specifier|static
name|XContentBuilder
name|build
parameter_list|(
name|RestChannel
name|channel
parameter_list|,
name|RestStatus
name|status
parameter_list|,
name|Exception
name|e
parameter_list|)
throws|throws
name|IOException
block|{
name|ToXContent
operator|.
name|Params
name|params
init|=
name|channel
operator|.
name|request
argument_list|()
decl_stmt|;
if|if
condition|(
name|params
operator|.
name|paramAsBoolean
argument_list|(
literal|"error_trace"
argument_list|,
operator|!
name|REST_EXCEPTION_SKIP_STACK_TRACE_DEFAULT
argument_list|)
condition|)
block|{
name|params
operator|=
operator|new
name|ToXContent
operator|.
name|DelegatingMapParams
argument_list|(
name|singletonMap
argument_list|(
name|REST_EXCEPTION_SKIP_STACK_TRACE
argument_list|,
literal|"false"
argument_list|)
argument_list|,
name|params
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|e
operator|!=
literal|null
condition|)
block|{
name|Supplier
argument_list|<
name|?
argument_list|>
name|messageSupplier
init|=
parameter_list|()
lambda|->
operator|new
name|ParameterizedMessage
argument_list|(
literal|"path: {}, params: {}"
argument_list|,
name|channel
operator|.
name|request
argument_list|()
operator|.
name|rawPath
argument_list|()
argument_list|,
name|channel
operator|.
name|request
argument_list|()
operator|.
name|params
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|status
operator|.
name|getStatus
argument_list|()
operator|<
literal|500
condition|)
block|{
name|SUPPRESSED_ERROR_LOGGER
operator|.
name|debug
argument_list|(
name|messageSupplier
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|SUPPRESSED_ERROR_LOGGER
operator|.
name|warn
argument_list|(
name|messageSupplier
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
name|XContentBuilder
name|builder
init|=
name|channel
operator|.
name|newErrorBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
decl_stmt|;
name|ElasticsearchException
operator|.
name|generateFailureXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|,
name|e
argument_list|,
name|channel
operator|.
name|detailedErrorsEnabled
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"status"
argument_list|,
name|status
operator|.
name|getStatus
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
return|return
name|builder
return|;
block|}
block|}
end_class

end_unit

