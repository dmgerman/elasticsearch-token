begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.get
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|get
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
name|ValidateActions
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
name|single
operator|.
name|shard
operator|.
name|SingleShardOperationRequest
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
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_comment
comment|/**  * A request to get a document (its source) from an index based on its type (optional) and id. Best created using  * {@link org.elasticsearch.client.Requests#getRequest(String)}.  *<p/>  *<p>The operation requires the {@link #index()}, {@link #type(String)} and {@link #id(String)}  * to be set.  *  * @see org.elasticsearch.action.get.GetResponse  * @see org.elasticsearch.client.Requests#getRequest(String)  * @see org.elasticsearch.client.Client#get(GetRequest)  */
end_comment

begin_class
DECL|class|GetRequest
specifier|public
class|class
name|GetRequest
extends|extends
name|SingleShardOperationRequest
block|{
DECL|field|type
specifier|protected
name|String
name|type
decl_stmt|;
DECL|field|id
specifier|protected
name|String
name|id
decl_stmt|;
DECL|field|routing
specifier|protected
name|String
name|routing
decl_stmt|;
DECL|field|preference
specifier|protected
name|String
name|preference
decl_stmt|;
DECL|field|fields
specifier|private
name|String
index|[]
name|fields
decl_stmt|;
DECL|field|refresh
specifier|private
name|boolean
name|refresh
init|=
literal|false
decl_stmt|;
DECL|field|realtime
name|Boolean
name|realtime
decl_stmt|;
DECL|method|GetRequest
name|GetRequest
parameter_list|()
block|{
name|type
operator|=
literal|"_all"
expr_stmt|;
block|}
comment|/**      * Constructs a new get request against the specified index. The {@link #type(String)} and {@link #id(String)}      * must be set.      */
DECL|method|GetRequest
specifier|public
name|GetRequest
parameter_list|(
name|String
name|index
parameter_list|)
block|{
name|super
argument_list|(
name|index
argument_list|)
expr_stmt|;
name|this
operator|.
name|type
operator|=
literal|"_all"
expr_stmt|;
block|}
comment|/**      * Constructs a new get request against the specified index with the type and id.      *      * @param index The index to get the document from      * @param type  The type of the document      * @param id    The id of the document      */
DECL|method|GetRequest
specifier|public
name|GetRequest
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
block|{
name|super
argument_list|(
name|index
argument_list|)
expr_stmt|;
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
name|this
operator|.
name|id
operator|=
name|id
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
if|if
condition|(
name|type
operator|==
literal|null
condition|)
block|{
name|validationException
operator|=
name|ValidateActions
operator|.
name|addValidationError
argument_list|(
literal|"type is missing"
argument_list|,
name|validationException
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|id
operator|==
literal|null
condition|)
block|{
name|validationException
operator|=
name|ValidateActions
operator|.
name|addValidationError
argument_list|(
literal|"id is missing"
argument_list|,
name|validationException
argument_list|)
expr_stmt|;
block|}
return|return
name|validationException
return|;
block|}
comment|/**      * Sets the index of the document to fetch.      */
annotation|@
name|Required
DECL|method|index
specifier|public
name|GetRequest
name|index
parameter_list|(
name|String
name|index
parameter_list|)
block|{
name|this
operator|.
name|index
operator|=
name|index
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets the type of the document to fetch.      */
DECL|method|type
specifier|public
name|GetRequest
name|type
parameter_list|(
annotation|@
name|Nullable
name|String
name|type
parameter_list|)
block|{
if|if
condition|(
name|type
operator|==
literal|null
condition|)
block|{
name|type
operator|=
literal|"_all"
expr_stmt|;
block|}
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets the id of the document to fetch.      */
annotation|@
name|Required
DECL|method|id
specifier|public
name|GetRequest
name|id
parameter_list|(
name|String
name|id
parameter_list|)
block|{
name|this
operator|.
name|id
operator|=
name|id
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets the parent id of this document. Will simply set the routing to this value, as it is only      * used for routing with delete requests.      */
DECL|method|parent
specifier|public
name|GetRequest
name|parent
parameter_list|(
name|String
name|parent
parameter_list|)
block|{
if|if
condition|(
name|routing
operator|==
literal|null
condition|)
block|{
name|routing
operator|=
name|parent
expr_stmt|;
block|}
return|return
name|this
return|;
block|}
comment|/**      * Controls the shard routing of the request. Using this value to hash the shard      * and not the id.      */
DECL|method|routing
specifier|public
name|GetRequest
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
comment|/**      * Sets the preference to execute the search. Defaults to randomize across shards. Can be set to      *<tt>_local</tt> to prefer local shards,<tt>_primary</tt> to execute only on primary shards, or      * a custom value, which guarantees that the same order will be used across different requests.      */
DECL|method|preference
specifier|public
name|GetRequest
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
DECL|method|type
specifier|public
name|String
name|type
parameter_list|()
block|{
return|return
name|type
return|;
block|}
DECL|method|id
specifier|public
name|String
name|id
parameter_list|()
block|{
return|return
name|id
return|;
block|}
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
comment|/**      * Explicitly specify the fields that will be returned. By default, the<tt>_source</tt>      * field will be returned.      */
DECL|method|fields
specifier|public
name|GetRequest
name|fields
parameter_list|(
name|String
modifier|...
name|fields
parameter_list|)
block|{
name|this
operator|.
name|fields
operator|=
name|fields
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Explicitly specify the fields that will be returned. By default, the<tt>_source</tt>      * field will be returned.      */
DECL|method|fields
specifier|public
name|String
index|[]
name|fields
parameter_list|()
block|{
return|return
name|this
operator|.
name|fields
return|;
block|}
comment|/**      * Should a refresh be executed before this get operation causing the operation to      * return the latest value. Note, heavy get should not set this to<tt>true</tt>. Defaults      * to<tt>false</tt>.      */
DECL|method|refresh
specifier|public
name|GetRequest
name|refresh
parameter_list|(
name|boolean
name|refresh
parameter_list|)
block|{
name|this
operator|.
name|refresh
operator|=
name|refresh
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|refresh
specifier|public
name|boolean
name|refresh
parameter_list|()
block|{
return|return
name|this
operator|.
name|refresh
return|;
block|}
DECL|method|realtime
specifier|public
name|boolean
name|realtime
parameter_list|()
block|{
return|return
name|this
operator|.
name|realtime
operator|==
literal|null
condition|?
literal|true
else|:
name|this
operator|.
name|realtime
return|;
block|}
DECL|method|realtime
specifier|public
name|GetRequest
name|realtime
parameter_list|(
name|Boolean
name|realtime
parameter_list|)
block|{
name|this
operator|.
name|realtime
operator|=
name|realtime
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Should the listener be called on a separate thread if needed.      */
annotation|@
name|Override
DECL|method|listenerThreaded
specifier|public
name|GetRequest
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
comment|/**      * Controls if the operation will be executed on a separate thread when executed locally.      */
annotation|@
name|Override
DECL|method|operationThreaded
specifier|public
name|GetRequest
name|operationThreaded
parameter_list|(
name|boolean
name|threadedOperation
parameter_list|)
block|{
name|super
operator|.
name|operationThreaded
argument_list|(
name|threadedOperation
argument_list|)
expr_stmt|;
return|return
name|this
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
name|type
operator|=
name|in
operator|.
name|readUTF
argument_list|()
expr_stmt|;
name|id
operator|=
name|in
operator|.
name|readUTF
argument_list|()
expr_stmt|;
if|if
condition|(
name|in
operator|.
name|readBoolean
argument_list|()
condition|)
block|{
name|routing
operator|=
name|in
operator|.
name|readUTF
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|in
operator|.
name|readBoolean
argument_list|()
condition|)
block|{
name|preference
operator|=
name|in
operator|.
name|readUTF
argument_list|()
expr_stmt|;
block|}
name|refresh
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
name|int
name|size
init|=
name|in
operator|.
name|readInt
argument_list|()
decl_stmt|;
if|if
condition|(
name|size
operator|>=
literal|0
condition|)
block|{
name|fields
operator|=
operator|new
name|String
index|[
name|size
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
name|size
condition|;
name|i
operator|++
control|)
block|{
name|fields
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
name|byte
name|realtime
init|=
name|in
operator|.
name|readByte
argument_list|()
decl_stmt|;
if|if
condition|(
name|realtime
operator|==
literal|0
condition|)
block|{
name|this
operator|.
name|realtime
operator|=
literal|false
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|realtime
operator|==
literal|1
condition|)
block|{
name|this
operator|.
name|realtime
operator|=
literal|true
expr_stmt|;
block|}
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
name|writeUTF
argument_list|(
name|type
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeUTF
argument_list|(
name|id
argument_list|)
expr_stmt|;
if|if
condition|(
name|routing
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
name|routing
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|preference
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
name|preference
argument_list|)
expr_stmt|;
block|}
name|out
operator|.
name|writeBoolean
argument_list|(
name|refresh
argument_list|)
expr_stmt|;
if|if
condition|(
name|fields
operator|==
literal|null
condition|)
block|{
name|out
operator|.
name|writeInt
argument_list|(
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|writeInt
argument_list|(
name|fields
operator|.
name|length
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|field
range|:
name|fields
control|)
block|{
name|out
operator|.
name|writeUTF
argument_list|(
name|field
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|realtime
operator|==
literal|null
condition|)
block|{
name|out
operator|.
name|writeByte
argument_list|(
operator|(
name|byte
operator|)
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|realtime
operator|==
literal|false
condition|)
block|{
name|out
operator|.
name|writeByte
argument_list|(
operator|(
name|byte
operator|)
literal|0
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|writeByte
argument_list|(
operator|(
name|byte
operator|)
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"["
operator|+
name|index
operator|+
literal|"]["
operator|+
name|type
operator|+
literal|"]["
operator|+
name|id
operator|+
literal|"]: routing ["
operator|+
name|routing
operator|+
literal|"]"
return|;
block|}
block|}
end_class

end_unit

