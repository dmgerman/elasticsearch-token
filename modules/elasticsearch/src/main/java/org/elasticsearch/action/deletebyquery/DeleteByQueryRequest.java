begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.deletebyquery
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|deletebyquery
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|UnicodeUtil
import|;
end_import

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
name|replication
operator|.
name|IndicesReplicationOperationRequest
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
name|replication
operator|.
name|ReplicationType
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
name|Unicode
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
name|FastByteArrayOutputStream
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
name|unit
operator|.
name|TimeValue
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

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|Actions
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * A request to delete all documents that matching a specific query. Best created with  * {@link org.elasticsearch.client.Requests#deleteByQueryRequest(String...)}.  *  *<p>The request requires the query source to be set either using {@link #query(org.elasticsearch.index.query.QueryBuilder)},  * or {@link #query(byte[])}.  *  * @author kimchy (shay.banon)  * @see DeleteByQueryResponse  * @see org.elasticsearch.client.Requests#deleteByQueryRequest(String...)  * @see org.elasticsearch.client.Client#deleteByQuery(DeleteByQueryRequest)  */
end_comment

begin_class
DECL|class|DeleteByQueryRequest
specifier|public
class|class
name|DeleteByQueryRequest
extends|extends
name|IndicesReplicationOperationRequest
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
name|byte
index|[]
name|querySource
decl_stmt|;
DECL|field|querySourceOffset
specifier|private
name|int
name|querySourceOffset
decl_stmt|;
DECL|field|querySourceLength
specifier|private
name|int
name|querySourceLength
decl_stmt|;
DECL|field|querySourceUnsafe
specifier|private
name|boolean
name|querySourceUnsafe
decl_stmt|;
DECL|field|queryParserName
specifier|private
name|String
name|queryParserName
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
comment|/**      * Constructs a new delete by query request to run against the provided indices. No indices means      * it will run against all indices.      */
DECL|method|DeleteByQueryRequest
specifier|public
name|DeleteByQueryRequest
parameter_list|(
name|String
modifier|...
name|indices
parameter_list|)
block|{
name|this
operator|.
name|indices
operator|=
name|indices
expr_stmt|;
block|}
DECL|method|DeleteByQueryRequest
specifier|public
name|DeleteByQueryRequest
parameter_list|()
block|{     }
comment|/**      * Should the listener be called on a separate thread if needed.      */
DECL|method|listenerThreaded
annotation|@
name|Override
specifier|public
name|DeleteByQueryRequest
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
DECL|method|validate
annotation|@
name|Override
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
if|if
condition|(
name|querySource
operator|==
literal|null
condition|)
block|{
name|validationException
operator|=
name|addValidationError
argument_list|(
literal|"query is missing"
argument_list|,
name|validationException
argument_list|)
expr_stmt|;
block|}
return|return
name|validationException
return|;
block|}
comment|/**      * The indices the delete by query will run against.      */
DECL|method|indices
specifier|public
name|DeleteByQueryRequest
name|indices
parameter_list|(
name|String
modifier|...
name|indices
parameter_list|)
block|{
name|this
operator|.
name|indices
operator|=
name|indices
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
if|if
condition|(
name|querySourceUnsafe
operator|||
name|querySourceOffset
operator|>
literal|0
condition|)
block|{
name|querySource
operator|=
name|Arrays
operator|.
name|copyOfRange
argument_list|(
name|querySource
argument_list|,
name|querySourceOffset
argument_list|,
name|querySourceOffset
operator|+
name|querySourceLength
argument_list|)
expr_stmt|;
name|querySourceOffset
operator|=
literal|0
expr_stmt|;
name|querySourceUnsafe
operator|=
literal|false
expr_stmt|;
block|}
return|return
name|querySource
return|;
block|}
comment|/**      * The query source to execute.      *      * @see org.elasticsearch.index.query.xcontent.QueryBuilders      */
DECL|method|query
annotation|@
name|Required
specifier|public
name|DeleteByQueryRequest
name|query
parameter_list|(
name|QueryBuilder
name|queryBuilder
parameter_list|)
block|{
name|FastByteArrayOutputStream
name|bos
init|=
name|queryBuilder
operator|.
name|buildAsUnsafeBytes
argument_list|()
decl_stmt|;
name|this
operator|.
name|querySource
operator|=
name|bos
operator|.
name|unsafeByteArray
argument_list|()
expr_stmt|;
name|this
operator|.
name|querySourceOffset
operator|=
literal|0
expr_stmt|;
name|this
operator|.
name|querySourceLength
operator|=
name|bos
operator|.
name|size
argument_list|()
expr_stmt|;
name|this
operator|.
name|querySourceUnsafe
operator|=
literal|true
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The query source to execute. It is preferable to use either {@link #query(byte[])}      * or {@link #query(org.elasticsearch.index.query.QueryBuilder)}.      */
DECL|method|query
annotation|@
name|Required
specifier|public
name|DeleteByQueryRequest
name|query
parameter_list|(
name|String
name|querySource
parameter_list|)
block|{
name|UnicodeUtil
operator|.
name|UTF8Result
name|result
init|=
name|Unicode
operator|.
name|fromStringAsUtf8
argument_list|(
name|querySource
argument_list|)
decl_stmt|;
name|this
operator|.
name|querySource
operator|=
name|result
operator|.
name|result
expr_stmt|;
name|this
operator|.
name|querySourceOffset
operator|=
literal|0
expr_stmt|;
name|this
operator|.
name|querySourceLength
operator|=
name|result
operator|.
name|length
expr_stmt|;
name|this
operator|.
name|querySourceUnsafe
operator|=
literal|true
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The query source to execute in the form of a map.      */
DECL|method|query
annotation|@
name|Required
specifier|public
name|DeleteByQueryRequest
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
DECL|method|query
annotation|@
name|Required
specifier|public
name|DeleteByQueryRequest
name|query
parameter_list|(
name|XContentBuilder
name|builder
parameter_list|)
block|{
try|try
block|{
name|this
operator|.
name|querySource
operator|=
name|builder
operator|.
name|unsafeBytes
argument_list|()
expr_stmt|;
name|this
operator|.
name|querySourceOffset
operator|=
literal|0
expr_stmt|;
name|this
operator|.
name|querySourceLength
operator|=
name|builder
operator|.
name|unsafeBytesLength
argument_list|()
expr_stmt|;
name|this
operator|.
name|querySourceUnsafe
operator|=
literal|true
expr_stmt|;
return|return
name|this
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
name|builder
operator|+
literal|"]"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**      * The query source to execute.      */
DECL|method|query
annotation|@
name|Required
specifier|public
name|DeleteByQueryRequest
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
comment|/**      * The query source to execute.      */
DECL|method|query
annotation|@
name|Required
specifier|public
name|DeleteByQueryRequest
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
name|this
operator|.
name|querySource
operator|=
name|querySource
expr_stmt|;
name|this
operator|.
name|querySourceOffset
operator|=
name|offset
expr_stmt|;
name|this
operator|.
name|querySourceLength
operator|=
name|length
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
name|DeleteByQueryRequest
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
name|DeleteByQueryRequest
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
comment|/**      * A timeout to wait if the delete by query operation can't be performed immediately. Defaults to<tt>1m</tt>.      */
DECL|method|timeout
specifier|public
name|DeleteByQueryRequest
name|timeout
parameter_list|(
name|TimeValue
name|timeout
parameter_list|)
block|{
name|this
operator|.
name|timeout
operator|=
name|timeout
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * A timeout to wait if the delete by query operation can't be performed immediately. Defaults to<tt>1m</tt>.      */
DECL|method|timeout
specifier|public
name|DeleteByQueryRequest
name|timeout
parameter_list|(
name|String
name|timeout
parameter_list|)
block|{
name|this
operator|.
name|timeout
operator|=
name|TimeValue
operator|.
name|parseTimeValue
argument_list|(
name|timeout
argument_list|,
literal|null
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The replication type to use with this operation.      */
DECL|method|replicationType
specifier|public
name|DeleteByQueryRequest
name|replicationType
parameter_list|(
name|ReplicationType
name|replicationType
parameter_list|)
block|{
name|this
operator|.
name|replicationType
operator|=
name|replicationType
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The replication type to use with this operation.      */
DECL|method|replicationType
specifier|public
name|DeleteByQueryRequest
name|replicationType
parameter_list|(
name|String
name|replicationType
parameter_list|)
block|{
name|this
operator|.
name|replicationType
operator|=
name|ReplicationType
operator|.
name|fromString
argument_list|(
name|replicationType
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
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
name|querySourceOffset
operator|=
literal|0
expr_stmt|;
name|querySourceLength
operator|=
name|in
operator|.
name|readVInt
argument_list|()
expr_stmt|;
name|querySource
operator|=
operator|new
name|byte
index|[
name|querySourceLength
index|]
expr_stmt|;
name|in
operator|.
name|readFully
argument_list|(
name|querySource
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
block|}
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
name|writeVInt
argument_list|(
name|querySourceLength
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBytes
argument_list|(
name|querySource
argument_list|,
name|querySourceOffset
argument_list|,
name|querySourceLength
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

