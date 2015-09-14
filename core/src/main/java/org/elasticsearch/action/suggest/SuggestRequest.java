begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.suggest
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|suggest
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
name|support
operator|.
name|broadcast
operator|.
name|BroadcastRequest
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
name|common
operator|.
name|Nullable
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
name|io
operator|.
name|stream
operator|.
name|StreamInput
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
name|stream
operator|.
name|StreamOutput
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
name|XContentHelper
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
name|suggest
operator|.
name|SuggestBuilder
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
name|Arrays
import|;
end_import

begin_comment
comment|/**  * A request to get suggestions for corrections of phrases. Best created with  * {@link org.elasticsearch.client.Requests#suggestRequest(String...)}.  *<p/>  *<p>The request requires the suggest query source to be set either using  * {@link #suggest(org.elasticsearch.common.bytes.BytesReference)} / {@link #suggest(org.elasticsearch.common.bytes.BytesReference)}  * or by using {@link #suggest(org.elasticsearch.search.suggest.SuggestBuilder)}  * (Best created using the {link @org.elasticsearch.search.suggest.SuggestBuilders)}).  *  * @see SuggestResponse  * @see org.elasticsearch.client.Client#suggest(SuggestRequest)  * @see org.elasticsearch.client.Requests#suggestRequest(String...)  * @see org.elasticsearch.search.suggest.SuggestBuilders  */
end_comment

begin_class
DECL|class|SuggestRequest
specifier|public
specifier|final
class|class
name|SuggestRequest
extends|extends
name|BroadcastRequest
argument_list|<
name|SuggestRequest
argument_list|>
block|{
annotation|@
name|Nullable
DECL|field|routing
specifier|private
name|String
name|routing
decl_stmt|;
annotation|@
name|Nullable
DECL|field|preference
specifier|private
name|String
name|preference
decl_stmt|;
DECL|field|suggestSource
specifier|private
name|BytesReference
name|suggestSource
decl_stmt|;
DECL|method|SuggestRequest
specifier|public
name|SuggestRequest
parameter_list|()
block|{     }
comment|/**      * Constructs a new suggest request against the provided indices. No indices provided means it will      * run against all indices.      */
DECL|method|SuggestRequest
specifier|public
name|SuggestRequest
parameter_list|(
name|String
modifier|...
name|indices
parameter_list|)
block|{
name|super
argument_list|(
name|indices
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|validate
specifier|public
name|ActionRequestValidationException
name|validate
parameter_list|()
block|{
name|ActionRequestValidationException
name|validationException
init|=
name|super
operator|.
name|validate
argument_list|()
decl_stmt|;
return|return
name|validationException
return|;
block|}
comment|/**      * The Phrase to get correction suggestions for       */
DECL|method|suggest
specifier|public
name|BytesReference
name|suggest
parameter_list|()
block|{
return|return
name|suggestSource
return|;
block|}
comment|/**      * set a new source for the suggest query        */
DECL|method|suggest
specifier|public
name|SuggestRequest
name|suggest
parameter_list|(
name|BytesReference
name|suggestSource
parameter_list|)
block|{
name|this
operator|.
name|suggestSource
operator|=
name|suggestSource
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * set a new source using a {@link org.elasticsearch.search.suggest.SuggestBuilder}      * for phrase and term suggestion lookup      */
DECL|method|suggest
specifier|public
name|SuggestRequest
name|suggest
parameter_list|(
name|SuggestBuilder
name|suggestBuilder
parameter_list|)
block|{
return|return
name|suggest
argument_list|(
name|suggestBuilder
operator|.
name|buildAsBytes
argument_list|(
name|Requests
operator|.
name|CONTENT_TYPE
argument_list|)
argument_list|)
return|;
block|}
comment|/**      * set a new source using a {@link org.elasticsearch.search.suggest.SuggestBuilder.SuggestionBuilder}      * for completion suggestion lookup      */
DECL|method|suggest
specifier|public
name|SuggestRequest
name|suggest
parameter_list|(
name|SuggestBuilder
operator|.
name|SuggestionBuilder
name|suggestionBuilder
parameter_list|)
block|{
return|return
name|suggest
argument_list|(
name|suggestionBuilder
operator|.
name|buildAsBytes
argument_list|(
name|Requests
operator|.
name|CONTENT_TYPE
argument_list|)
argument_list|)
return|;
block|}
DECL|method|suggest
specifier|public
name|SuggestRequest
name|suggest
parameter_list|(
name|String
name|source
parameter_list|)
block|{
return|return
name|suggest
argument_list|(
operator|new
name|BytesArray
argument_list|(
name|source
argument_list|)
argument_list|)
return|;
block|}
comment|/**      * A comma separated list of routing values to control the shards the search will be executed on.      */
DECL|method|routing
specifier|public
name|String
name|routing
parameter_list|()
block|{
return|return
name|this
operator|.
name|routing
return|;
block|}
comment|/**      * A comma separated list of routing values to control the shards the search will be executed on.      */
DECL|method|routing
specifier|public
name|SuggestRequest
name|routing
parameter_list|(
name|String
name|routing
parameter_list|)
block|{
name|this
operator|.
name|routing
operator|=
name|routing
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The routing values to control the shards that the search will be executed on.      */
DECL|method|routing
specifier|public
name|SuggestRequest
name|routing
parameter_list|(
name|String
modifier|...
name|routings
parameter_list|)
block|{
name|this
operator|.
name|routing
operator|=
name|Strings
operator|.
name|arrayToCommaDelimitedString
argument_list|(
name|routings
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|preference
specifier|public
name|SuggestRequest
name|preference
parameter_list|(
name|String
name|preference
parameter_list|)
block|{
name|this
operator|.
name|preference
operator|=
name|preference
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|preference
specifier|public
name|String
name|preference
parameter_list|()
block|{
return|return
name|this
operator|.
name|preference
return|;
block|}
annotation|@
name|Override
DECL|method|readFrom
specifier|public
name|void
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|routing
operator|=
name|in
operator|.
name|readOptionalString
argument_list|()
expr_stmt|;
name|preference
operator|=
name|in
operator|.
name|readOptionalString
argument_list|()
expr_stmt|;
name|suggest
argument_list|(
name|in
operator|.
name|readBytesReference
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|writeTo
specifier|public
name|void
name|writeTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeOptionalString
argument_list|(
name|routing
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeOptionalString
argument_list|(
name|preference
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBytesReference
argument_list|(
name|suggestSource
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|String
name|sSource
init|=
literal|"_na_"
decl_stmt|;
try|try
block|{
name|sSource
operator|=
name|XContentHelper
operator|.
name|convertToJson
argument_list|(
name|suggestSource
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// ignore
block|}
return|return
literal|"["
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|indices
argument_list|)
operator|+
literal|"]"
operator|+
literal|", suggestSource["
operator|+
name|sSource
operator|+
literal|"]"
return|;
block|}
block|}
end_class

end_unit

