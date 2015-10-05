begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.count
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|count
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
name|index
operator|.
name|query
operator|.
name|QueryBuilder
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
name|builder
operator|.
name|SearchSourceBuilder
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

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|internal
operator|.
name|SearchContext
operator|.
name|DEFAULT_TERMINATE_AFTER
import|;
end_import

begin_comment
comment|/**  * A request to count the number of documents matching a specific query. Best created with  * {@link org.elasticsearch.client.Requests#countRequest(String...)}.  *  * @see CountResponse  * @see org.elasticsearch.client.Client#count(CountRequest)  * @see org.elasticsearch.client.Requests#countRequest(String...)  */
end_comment

begin_class
DECL|class|CountRequest
specifier|public
class|class
name|CountRequest
extends|extends
name|BroadcastRequest
argument_list|<
name|CountRequest
argument_list|>
block|{
DECL|field|DEFAULT_MIN_SCORE
specifier|public
specifier|static
specifier|final
name|float
name|DEFAULT_MIN_SCORE
init|=
operator|-
literal|1f
decl_stmt|;
annotation|@
name|Nullable
DECL|field|routing
specifier|protected
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
DECL|field|types
specifier|private
name|String
index|[]
name|types
init|=
name|Strings
operator|.
name|EMPTY_ARRAY
decl_stmt|;
DECL|field|searchSourceBuilder
specifier|private
name|SearchSourceBuilder
name|searchSourceBuilder
init|=
operator|new
name|SearchSourceBuilder
argument_list|()
decl_stmt|;
comment|/**      * Constructs a new count request against the provided indices. No indices provided means it will      * run against all indices.      */
DECL|method|CountRequest
specifier|public
name|CountRequest
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
name|searchSourceBuilder
operator|.
name|size
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|searchSourceBuilder
operator|.
name|minScore
argument_list|(
name|DEFAULT_MIN_SCORE
argument_list|)
expr_stmt|;
name|searchSourceBuilder
operator|.
name|terminateAfter
argument_list|(
name|DEFAULT_TERMINATE_AFTER
argument_list|)
expr_stmt|;
block|}
comment|/**      * The minimum score of the documents to include in the count.      */
DECL|method|minScore
specifier|public
name|float
name|minScore
parameter_list|()
block|{
return|return
name|searchSourceBuilder
operator|.
name|minScore
argument_list|()
return|;
block|}
comment|/**      * The minimum score of the documents to include in the count. Defaults to<tt>-1</tt> which means all      * documents will be included in the count.      */
DECL|method|minScore
specifier|public
name|CountRequest
name|minScore
parameter_list|(
name|float
name|minScore
parameter_list|)
block|{
name|this
operator|.
name|searchSourceBuilder
operator|.
name|minScore
argument_list|(
name|minScore
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The query to execute      */
DECL|method|query
specifier|public
name|CountRequest
name|query
parameter_list|(
name|QueryBuilder
argument_list|<
name|?
argument_list|>
name|queryBuilder
parameter_list|)
block|{
name|this
operator|.
name|searchSourceBuilder
operator|.
name|query
argument_list|(
name|queryBuilder
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The types of documents the query will run against. Defaults to all types.      */
DECL|method|types
specifier|public
name|String
index|[]
name|types
parameter_list|()
block|{
return|return
name|this
operator|.
name|types
return|;
block|}
comment|/**      * The types of documents the query will run against. Defaults to all types.      */
DECL|method|types
specifier|public
name|CountRequest
name|types
parameter_list|(
name|String
modifier|...
name|types
parameter_list|)
block|{
name|this
operator|.
name|types
operator|=
name|types
expr_stmt|;
return|return
name|this
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
name|CountRequest
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
name|CountRequest
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
name|CountRequest
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
comment|/**      * Upon reaching<code>terminateAfter</code> counts, the count request will early terminate      */
DECL|method|terminateAfter
specifier|public
name|CountRequest
name|terminateAfter
parameter_list|(
name|int
name|terminateAfterCount
parameter_list|)
block|{
name|this
operator|.
name|searchSourceBuilder
operator|.
name|terminateAfter
argument_list|(
name|terminateAfterCount
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|terminateAfter
specifier|public
name|int
name|terminateAfter
parameter_list|()
block|{
return|return
name|this
operator|.
name|searchSourceBuilder
operator|.
name|terminateAfter
argument_list|()
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
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"CountRequest doesn't support being sent over the wire, just a shortcut to the search api"
argument_list|)
throw|;
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
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"CountRequest doesn't support being sent over the wire, just a shortcut to the search api"
argument_list|)
throw|;
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
name|toString
argument_list|(
name|searchSourceBuilder
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
name|Arrays
operator|.
name|toString
argument_list|(
name|types
argument_list|)
operator|+
literal|", source["
operator|+
name|sSource
operator|+
literal|"]"
return|;
block|}
DECL|method|sourceBuilderString
specifier|public
name|String
name|sourceBuilderString
parameter_list|()
block|{
return|return
name|searchSourceBuilder
operator|.
name|toString
argument_list|()
return|;
block|}
DECL|method|toSearchRequest
specifier|public
name|SearchRequest
name|toSearchRequest
parameter_list|()
block|{
name|SearchRequest
name|searchRequest
init|=
operator|new
name|SearchRequest
argument_list|(
name|indices
argument_list|()
argument_list|)
decl_stmt|;
name|searchRequest
operator|.
name|source
argument_list|(
name|searchSourceBuilder
argument_list|)
expr_stmt|;
name|searchRequest
operator|.
name|indicesOptions
argument_list|(
name|indicesOptions
argument_list|()
argument_list|)
expr_stmt|;
name|searchRequest
operator|.
name|types
argument_list|(
name|types
argument_list|()
argument_list|)
expr_stmt|;
name|searchRequest
operator|.
name|routing
argument_list|(
name|routing
argument_list|()
argument_list|)
expr_stmt|;
name|searchRequest
operator|.
name|preference
argument_list|(
name|preference
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|searchRequest
return|;
block|}
block|}
end_class

end_unit

