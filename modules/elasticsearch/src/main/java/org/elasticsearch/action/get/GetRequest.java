begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|support
operator|.
name|single
operator|.
name|SingleOperationRequest
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

begin_comment
comment|/**  * A request to get a document (its source) from an index based on its type and id. Best created using  * {@link org.elasticsearch.client.Requests#getRequest(String)}.  *  *<p>The operation requires the {@link #index()}, {@link #type(String)} and {@link #id(String)}  * to be set.  *  * @author kimchy (shay.banon)  * @see org.elasticsearch.action.get.GetResponse  * @see org.elasticsearch.client.Requests#getRequest(String)  * @see org.elasticsearch.client.Client#get(GetRequest)  */
end_comment

begin_class
DECL|class|GetRequest
specifier|public
class|class
name|GetRequest
extends|extends
name|SingleOperationRequest
block|{
DECL|field|fields
specifier|private
name|String
index|[]
name|fields
decl_stmt|;
DECL|method|GetRequest
name|GetRequest
parameter_list|()
block|{     }
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
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
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
argument_list|,
name|type
argument_list|,
name|id
argument_list|)
expr_stmt|;
block|}
comment|/**      * Sets the type of the document to fetch.      */
DECL|method|type
annotation|@
name|Required
specifier|public
name|GetRequest
name|type
parameter_list|(
name|String
name|type
parameter_list|)
block|{
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
DECL|method|id
annotation|@
name|Required
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
comment|/**      * Should the listener be called on a separate thread if needed.      */
DECL|method|listenerThreaded
annotation|@
name|Override
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
DECL|method|threadedOperation
annotation|@
name|Override
specifier|public
name|GetRequest
name|threadedOperation
parameter_list|(
name|boolean
name|threadedOperation
parameter_list|)
block|{
name|super
operator|.
name|threadedOperation
argument_list|(
name|threadedOperation
argument_list|)
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
literal|"]"
return|;
block|}
block|}
end_class

end_unit

