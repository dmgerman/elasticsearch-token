begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.rest.action.mlt
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|action
operator|.
name|mlt
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
name|action
operator|.
name|search
operator|.
name|SearchType
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
name|rest
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
name|search
operator|.
name|Scroll
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
name|org
operator|.
name|elasticsearch
operator|.
name|client
operator|.
name|Requests
operator|.
name|moreLikeThisRequest
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|unit
operator|.
name|TimeValue
operator|.
name|parseTimeValue
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|RestRequest
operator|.
name|Method
operator|.
name|GET
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|RestRequest
operator|.
name|Method
operator|.
name|POST
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|RestStatus
operator|.
name|BAD_REQUEST
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|RestStatus
operator|.
name|OK
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|action
operator|.
name|support
operator|.
name|RestXContentBuilder
operator|.
name|restContentBuilder
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|RestMoreLikeThisAction
specifier|public
class|class
name|RestMoreLikeThisAction
extends|extends
name|BaseRestHandler
block|{
annotation|@
name|Inject
DECL|method|RestMoreLikeThisAction
specifier|public
name|RestMoreLikeThisAction
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|Client
name|client
parameter_list|,
name|RestController
name|controller
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|client
argument_list|)
expr_stmt|;
name|controller
operator|.
name|registerHandler
argument_list|(
name|GET
argument_list|,
literal|"/{index}/{type}/{id}/_mlt"
argument_list|,
name|this
argument_list|)
expr_stmt|;
name|controller
operator|.
name|registerHandler
argument_list|(
name|POST
argument_list|,
literal|"/{index}/{type}/{id}/_mlt"
argument_list|,
name|this
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|handleRequest
specifier|public
name|void
name|handleRequest
parameter_list|(
specifier|final
name|RestRequest
name|request
parameter_list|,
specifier|final
name|RestChannel
name|channel
parameter_list|)
block|{
name|MoreLikeThisRequest
name|mltRequest
init|=
name|moreLikeThisRequest
argument_list|(
name|request
operator|.
name|param
argument_list|(
literal|"index"
argument_list|)
argument_list|)
operator|.
name|type
argument_list|(
name|request
operator|.
name|param
argument_list|(
literal|"type"
argument_list|)
argument_list|)
operator|.
name|id
argument_list|(
name|request
operator|.
name|param
argument_list|(
literal|"id"
argument_list|)
argument_list|)
decl_stmt|;
name|mltRequest
operator|.
name|listenerThreaded
argument_list|(
literal|false
argument_list|)
expr_stmt|;
try|try
block|{
name|mltRequest
operator|.
name|fields
argument_list|(
name|request
operator|.
name|paramAsStringArray
argument_list|(
literal|"mlt_fields"
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|mltRequest
operator|.
name|percentTermsToMatch
argument_list|(
name|request
operator|.
name|paramAsFloat
argument_list|(
literal|"percent_terms_to_match"
argument_list|,
operator|-
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|mltRequest
operator|.
name|minTermFreq
argument_list|(
name|request
operator|.
name|paramAsInt
argument_list|(
literal|"min_term_freq"
argument_list|,
operator|-
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|mltRequest
operator|.
name|maxQueryTerms
argument_list|(
name|request
operator|.
name|paramAsInt
argument_list|(
literal|"max_query_terms"
argument_list|,
operator|-
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|mltRequest
operator|.
name|stopWords
argument_list|(
name|request
operator|.
name|paramAsStringArray
argument_list|(
literal|"stop_words"
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|mltRequest
operator|.
name|minDocFreq
argument_list|(
name|request
operator|.
name|paramAsInt
argument_list|(
literal|"min_doc_freq"
argument_list|,
operator|-
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|mltRequest
operator|.
name|maxDocFreq
argument_list|(
name|request
operator|.
name|paramAsInt
argument_list|(
literal|"max_doc_freq"
argument_list|,
operator|-
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|mltRequest
operator|.
name|minWordLen
argument_list|(
name|request
operator|.
name|paramAsInt
argument_list|(
literal|"min_word_len"
argument_list|,
operator|-
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|mltRequest
operator|.
name|maxWordLen
argument_list|(
name|request
operator|.
name|paramAsInt
argument_list|(
literal|"max_word_len"
argument_list|,
operator|-
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|mltRequest
operator|.
name|boostTerms
argument_list|(
name|request
operator|.
name|paramAsFloat
argument_list|(
literal|"boost_terms"
argument_list|,
operator|-
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|mltRequest
operator|.
name|searchType
argument_list|(
name|SearchType
operator|.
name|fromString
argument_list|(
name|request
operator|.
name|param
argument_list|(
literal|"search_type"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|mltRequest
operator|.
name|searchIndices
argument_list|(
name|request
operator|.
name|paramAsStringArray
argument_list|(
literal|"search_indices"
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|mltRequest
operator|.
name|searchTypes
argument_list|(
name|request
operator|.
name|paramAsStringArray
argument_list|(
literal|"search_types"
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|mltRequest
operator|.
name|searchQueryHint
argument_list|(
name|request
operator|.
name|param
argument_list|(
literal|"search_query_hint"
argument_list|)
argument_list|)
expr_stmt|;
name|mltRequest
operator|.
name|searchSize
argument_list|(
name|request
operator|.
name|paramAsInt
argument_list|(
literal|"search_size"
argument_list|,
name|mltRequest
operator|.
name|searchSize
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|mltRequest
operator|.
name|searchFrom
argument_list|(
name|request
operator|.
name|paramAsInt
argument_list|(
literal|"search_from"
argument_list|,
name|mltRequest
operator|.
name|searchFrom
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|searchScroll
init|=
name|request
operator|.
name|param
argument_list|(
literal|"search_scroll"
argument_list|)
decl_stmt|;
if|if
condition|(
name|searchScroll
operator|!=
literal|null
condition|)
block|{
name|mltRequest
operator|.
name|searchScroll
argument_list|(
operator|new
name|Scroll
argument_list|(
name|parseTimeValue
argument_list|(
name|searchScroll
argument_list|,
literal|null
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|request
operator|.
name|hasContent
argument_list|()
condition|)
block|{
name|mltRequest
operator|.
name|searchSource
argument_list|(
name|request
operator|.
name|content
argument_list|()
argument_list|,
name|request
operator|.
name|contentUnsafe
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|String
name|searchSource
init|=
name|request
operator|.
name|param
argument_list|(
literal|"search_source"
argument_list|)
decl_stmt|;
if|if
condition|(
name|searchSource
operator|!=
literal|null
condition|)
block|{
name|mltRequest
operator|.
name|searchSource
argument_list|(
name|searchSource
argument_list|)
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
try|try
block|{
name|XContentBuilder
name|builder
init|=
name|restContentBuilder
argument_list|(
name|request
argument_list|)
decl_stmt|;
name|channel
operator|.
name|sendResponse
argument_list|(
operator|new
name|XContentRestResponse
argument_list|(
name|request
argument_list|,
name|BAD_REQUEST
argument_list|,
name|builder
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"error"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
operator|.
name|endObject
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e1
parameter_list|)
block|{
name|logger
operator|.
name|error
argument_list|(
literal|"Failed to send failure response"
argument_list|,
name|e1
argument_list|)
expr_stmt|;
block|}
return|return;
block|}
name|client
operator|.
name|moreLikeThis
argument_list|(
name|mltRequest
argument_list|,
operator|new
name|ActionListener
argument_list|<
name|SearchResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onResponse
parameter_list|(
name|SearchResponse
name|response
parameter_list|)
block|{
try|try
block|{
name|XContentBuilder
name|builder
init|=
name|restContentBuilder
argument_list|(
name|request
argument_list|)
decl_stmt|;
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|response
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|request
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|channel
operator|.
name|sendResponse
argument_list|(
operator|new
name|XContentRestResponse
argument_list|(
name|request
argument_list|,
name|OK
argument_list|,
name|builder
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
name|Throwable
name|e
parameter_list|)
block|{
try|try
block|{
name|channel
operator|.
name|sendResponse
argument_list|(
operator|new
name|XContentThrowableRestResponse
argument_list|(
name|request
argument_list|,
name|e
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e1
parameter_list|)
block|{
name|logger
operator|.
name|error
argument_list|(
literal|"Failed to send failure response"
argument_list|,
name|e1
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

