begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|support
operator|.
name|broadcast
operator|.
name|BroadcastOperationRequest
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
name|BroadcastOperationThreading
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
name|util
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
name|util
operator|.
name|Required
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
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
name|util
operator|.
name|Unicode
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
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
name|util
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
comment|/**  * A request to count the number of documents matching a specific query. Best created with  * {@link org.elasticsearch.client.Requests#countRequest(String...)}.  *  *<p>The request requires the query source to be set either using {@link #querySource(org.elasticsearch.index.query.QueryBuilder)},  * or {@link #querySource(byte[])}.  *  * @author kimchy (shay.banon)  * @see CountResponse  * @see org.elasticsearch.client.Client#count(CountRequest)  * @see org.elasticsearch.client.Requests#countRequest(String...)  */
end_comment

begin_class
DECL|class|CountRequest
specifier|public
class|class
name|CountRequest
extends|extends
name|BroadcastOperationRequest
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
DECL|field|minScore
specifier|private
name|float
name|minScore
init|=
name|DEFAULT_MIN_SCORE
decl_stmt|;
DECL|field|querySource
annotation|@
name|Required
specifier|private
name|byte
index|[]
name|querySource
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
DECL|field|queryParserName
annotation|@
name|Nullable
specifier|private
name|String
name|queryParserName
decl_stmt|;
DECL|method|CountRequest
name|CountRequest
parameter_list|()
block|{     }
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
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**      * Controls the operation threading model.      */
DECL|method|operationThreading
annotation|@
name|Override
specifier|public
name|CountRequest
name|operationThreading
parameter_list|(
name|BroadcastOperationThreading
name|operationThreading
parameter_list|)
block|{
name|super
operator|.
name|operationThreading
argument_list|(
name|operationThreading
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Should the listener be called on a separate thread if needed.      */
DECL|method|listenerThreaded
annotation|@
name|Override
specifier|public
name|CountRequest
name|listenerThreaded
parameter_list|(
name|boolean
name|threadedListener
parameter_list|)
block|{
name|super
operator|.
name|listenerThreaded
argument_list|(
name|threadedListener
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * A query hint to optionally later be used when routing the request.      */
DECL|method|queryHint
specifier|public
name|CountRequest
name|queryHint
parameter_list|(
name|String
name|queryHint
parameter_list|)
block|{
name|this
operator|.
name|queryHint
operator|=
name|queryHint
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The minimum score of the documents to include in the count.      */
DECL|method|minScore
name|float
name|minScore
parameter_list|()
block|{
return|return
name|minScore
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
name|minScore
operator|=
name|minScore
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The query source to execute.      */
DECL|method|querySource
name|byte
index|[]
name|querySource
parameter_list|()
block|{
return|return
name|querySource
return|;
block|}
comment|/**      * The query source to execute.      *      * @see org.elasticsearch.index.query.json.JsonQueryBuilders      */
DECL|method|querySource
annotation|@
name|Required
specifier|public
name|CountRequest
name|querySource
parameter_list|(
name|QueryBuilder
name|queryBuilder
parameter_list|)
block|{
return|return
name|querySource
argument_list|(
name|queryBuilder
operator|.
name|buildAsBytes
argument_list|()
argument_list|)
return|;
block|}
comment|/**      * The query source to execute. It is preferable to use either {@link #querySource(byte[])}      * or {@link #querySource(org.elasticsearch.index.query.QueryBuilder)}.      */
DECL|method|querySource
annotation|@
name|Required
specifier|public
name|CountRequest
name|querySource
parameter_list|(
name|String
name|querySource
parameter_list|)
block|{
return|return
name|querySource
argument_list|(
name|Unicode
operator|.
name|fromStringAsBytes
argument_list|(
name|querySource
argument_list|)
argument_list|)
return|;
block|}
comment|/**      * The query source to execute.      */
DECL|method|querySource
annotation|@
name|Required
specifier|public
name|CountRequest
name|querySource
parameter_list|(
name|byte
index|[]
name|querySource
parameter_list|)
block|{
name|this
operator|.
name|querySource
operator|=
name|querySource
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The query parse name to use. If not set, will use the default one.      */
DECL|method|queryParserName
name|String
name|queryParserName
parameter_list|()
block|{
return|return
name|queryParserName
return|;
block|}
comment|/**      * The query parse name to use. If not set, will use the default one.      */
DECL|method|queryParserName
specifier|public
name|CountRequest
name|queryParserName
parameter_list|(
name|String
name|queryParserName
parameter_list|)
block|{
name|this
operator|.
name|queryParserName
operator|=
name|queryParserName
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The types of documents the query will run against. Defaults to all types.      */
DECL|method|types
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
DECL|method|readFrom
annotation|@
name|Override
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
name|minScore
operator|=
name|in
operator|.
name|readFloat
argument_list|()
expr_stmt|;
name|querySource
operator|=
operator|new
name|byte
index|[
name|in
operator|.
name|readVInt
argument_list|()
index|]
expr_stmt|;
name|in
operator|.
name|readFully
argument_list|(
name|querySource
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|in
operator|.
name|readBoolean
argument_list|()
condition|)
block|{
name|queryParserName
operator|=
name|in
operator|.
name|readUTF
argument_list|()
expr_stmt|;
block|}
name|int
name|typesSize
init|=
name|in
operator|.
name|readVInt
argument_list|()
decl_stmt|;
if|if
condition|(
name|typesSize
operator|>
literal|0
condition|)
block|{
name|types
operator|=
operator|new
name|String
index|[
name|typesSize
index|]
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
name|typesSize
condition|;
name|i
operator|++
control|)
block|{
name|types
index|[
name|i
index|]
operator|=
name|in
operator|.
name|readUTF
argument_list|()
expr_stmt|;
block|}
block|}
block|}
DECL|method|writeTo
annotation|@
name|Override
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
name|writeFloat
argument_list|(
name|minScore
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|querySource
operator|.
name|length
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBytes
argument_list|(
name|querySource
argument_list|)
expr_stmt|;
if|if
condition|(
name|queryParserName
operator|==
literal|null
condition|)
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeUTF
argument_list|(
name|queryParserName
argument_list|)
expr_stmt|;
block|}
name|out
operator|.
name|writeVInt
argument_list|(
name|types
operator|.
name|length
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|type
range|:
name|types
control|)
block|{
name|out
operator|.
name|writeUTF
argument_list|(
name|type
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|toString
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
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
literal|"]["
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|types
argument_list|)
operator|+
literal|"], querySource["
operator|+
name|Unicode
operator|.
name|fromBytes
argument_list|(
name|querySource
argument_list|)
operator|+
literal|"]"
return|;
block|}
block|}
end_class

end_unit

