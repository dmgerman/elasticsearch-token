begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.indices.validate.query
package|package
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
name|validate
operator|.
name|query
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticSearchGenerationException
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
name|Required
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
name|QueryBuilder
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
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_comment
comment|/**  * A request to validate a specific query.  *<p/>  *<p>The request requires the query source to be set either using {@link #query(org.elasticsearch.index.query.QueryBuilder)},  * or {@link #query(byte[])}.  */
end_comment

begin_class
DECL|class|ValidateQueryRequest
specifier|public
class|class
name|ValidateQueryRequest
extends|extends
name|BroadcastOperationRequest
argument_list|<
name|ValidateQueryRequest
argument_list|>
block|{
DECL|field|contentType
specifier|private
specifier|static
specifier|final
name|XContentType
name|contentType
init|=
name|Requests
operator|.
name|CONTENT_TYPE
decl_stmt|;
DECL|field|querySource
specifier|private
name|BytesReference
name|querySource
decl_stmt|;
DECL|field|querySourceUnsafe
specifier|private
name|boolean
name|querySourceUnsafe
decl_stmt|;
DECL|field|explain
specifier|private
name|boolean
name|explain
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
DECL|method|ValidateQueryRequest
name|ValidateQueryRequest
parameter_list|()
block|{     }
comment|/**      * Constructs a new validate request against the provided indices. No indices provided means it will      * run against all indices.      */
DECL|method|ValidateQueryRequest
specifier|public
name|ValidateQueryRequest
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
annotation|@
name|Override
DECL|method|beforeStart
specifier|protected
name|void
name|beforeStart
parameter_list|()
block|{
if|if
condition|(
name|querySourceUnsafe
condition|)
block|{
name|querySource
operator|=
name|querySource
operator|.
name|copyBytesArray
argument_list|()
expr_stmt|;
name|querySourceUnsafe
operator|=
literal|false
expr_stmt|;
block|}
block|}
comment|/**      * The query source to execute.      */
DECL|method|querySource
name|BytesReference
name|querySource
parameter_list|()
block|{
return|return
name|querySource
return|;
block|}
comment|/**      * The query source to execute.      *      * @see org.elasticsearch.index.query.QueryBuilders      */
annotation|@
name|Required
DECL|method|query
specifier|public
name|ValidateQueryRequest
name|query
parameter_list|(
name|QueryBuilder
name|queryBuilder
parameter_list|)
block|{
name|this
operator|.
name|querySource
operator|=
name|queryBuilder
operator|.
name|buildAsBytes
argument_list|()
expr_stmt|;
name|this
operator|.
name|querySourceUnsafe
operator|=
literal|false
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The query source to execute in the form of a map.      */
annotation|@
name|Required
DECL|method|query
specifier|public
name|ValidateQueryRequest
name|query
parameter_list|(
name|Map
name|querySource
parameter_list|)
block|{
try|try
block|{
name|XContentBuilder
name|builder
init|=
name|XContentFactory
operator|.
name|contentBuilder
argument_list|(
name|contentType
argument_list|)
decl_stmt|;
name|builder
operator|.
name|map
argument_list|(
name|querySource
argument_list|)
expr_stmt|;
return|return
name|query
argument_list|(
name|builder
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticSearchGenerationException
argument_list|(
literal|"Failed to generate ["
operator|+
name|querySource
operator|+
literal|"]"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Required
DECL|method|query
specifier|public
name|ValidateQueryRequest
name|query
parameter_list|(
name|XContentBuilder
name|builder
parameter_list|)
block|{
name|this
operator|.
name|querySource
operator|=
name|builder
operator|.
name|bytes
argument_list|()
expr_stmt|;
name|this
operator|.
name|querySourceUnsafe
operator|=
literal|false
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The query source to validate. It is preferable to use either {@link #query(byte[])}      * or {@link #query(org.elasticsearch.index.query.QueryBuilder)}.      */
annotation|@
name|Required
DECL|method|query
specifier|public
name|ValidateQueryRequest
name|query
parameter_list|(
name|String
name|querySource
parameter_list|)
block|{
name|this
operator|.
name|querySource
operator|=
operator|new
name|BytesArray
argument_list|(
name|querySource
argument_list|)
expr_stmt|;
empty_stmt|;
name|this
operator|.
name|querySourceUnsafe
operator|=
literal|false
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The query source to validate.      */
annotation|@
name|Required
DECL|method|query
specifier|public
name|ValidateQueryRequest
name|query
parameter_list|(
name|byte
index|[]
name|querySource
parameter_list|)
block|{
return|return
name|query
argument_list|(
name|querySource
argument_list|,
literal|0
argument_list|,
name|querySource
operator|.
name|length
argument_list|,
literal|false
argument_list|)
return|;
block|}
comment|/**      * The query source to validate.      */
annotation|@
name|Required
DECL|method|query
specifier|public
name|ValidateQueryRequest
name|query
parameter_list|(
name|byte
index|[]
name|querySource
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|,
name|boolean
name|unsafe
parameter_list|)
block|{
return|return
name|query
argument_list|(
operator|new
name|BytesArray
argument_list|(
name|querySource
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
argument_list|,
name|unsafe
argument_list|)
return|;
block|}
comment|/**      * The query source to validate.      */
annotation|@
name|Required
DECL|method|query
specifier|public
name|ValidateQueryRequest
name|query
parameter_list|(
name|BytesReference
name|querySource
parameter_list|,
name|boolean
name|unsafe
parameter_list|)
block|{
name|this
operator|.
name|querySource
operator|=
name|querySource
expr_stmt|;
name|this
operator|.
name|querySourceUnsafe
operator|=
name|unsafe
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
name|ValidateQueryRequest
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
comment|/**      * Indicate if detailed information about query is requested      */
DECL|method|explain
specifier|public
name|void
name|explain
parameter_list|(
name|boolean
name|explain
parameter_list|)
block|{
name|this
operator|.
name|explain
operator|=
name|explain
expr_stmt|;
block|}
comment|/**      * Indicates if detailed information about query is requested      */
DECL|method|explain
specifier|public
name|boolean
name|explain
parameter_list|()
block|{
return|return
name|explain
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
name|querySourceUnsafe
operator|=
literal|false
expr_stmt|;
name|querySource
operator|=
name|in
operator|.
name|readBytesReference
argument_list|()
expr_stmt|;
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
name|explain
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
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
name|writeBytesReference
argument_list|(
name|querySource
argument_list|)
expr_stmt|;
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
name|out
operator|.
name|writeBoolean
argument_list|(
name|explain
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
name|querySource
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
name|Arrays
operator|.
name|toString
argument_list|(
name|types
argument_list|)
operator|+
literal|", querySource["
operator|+
name|sSource
operator|+
literal|"], explain:"
operator|+
name|explain
return|;
block|}
block|}
end_class

end_unit

