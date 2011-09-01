begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.percolate
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|percolate
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
name|single
operator|.
name|custom
operator|.
name|SingleCustomOperationRequest
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
name|XContentType
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
comment|/**  * @author kimchy  */
end_comment

begin_class
DECL|class|PercolateRequest
specifier|public
class|class
name|PercolateRequest
extends|extends
name|SingleCustomOperationRequest
block|{
DECL|field|index
specifier|private
name|String
name|index
decl_stmt|;
DECL|field|type
specifier|private
name|String
name|type
decl_stmt|;
DECL|field|source
specifier|private
name|byte
index|[]
name|source
decl_stmt|;
DECL|field|sourceOffset
specifier|private
name|int
name|sourceOffset
decl_stmt|;
DECL|field|sourceLength
specifier|private
name|int
name|sourceLength
decl_stmt|;
DECL|field|sourceUnsafe
specifier|private
name|boolean
name|sourceUnsafe
decl_stmt|;
DECL|method|PercolateRequest
specifier|public
name|PercolateRequest
parameter_list|()
block|{      }
comment|/**      * Constructs a new percolate request.      *      * @param index The index name      * @param type  The document type      */
DECL|method|PercolateRequest
specifier|public
name|PercolateRequest
parameter_list|(
name|String
name|index
parameter_list|,
name|String
name|type
parameter_list|)
block|{
name|this
operator|.
name|index
operator|=
name|index
expr_stmt|;
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
block|}
DECL|method|index
specifier|public
name|PercolateRequest
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
DECL|method|type
specifier|public
name|PercolateRequest
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
DECL|method|index
specifier|public
name|String
name|index
parameter_list|()
block|{
return|return
name|this
operator|.
name|index
return|;
block|}
DECL|method|type
specifier|public
name|String
name|type
parameter_list|()
block|{
return|return
name|this
operator|.
name|type
return|;
block|}
comment|/**      * Before we fork on a local thread, make sure we copy over the bytes if they are unsafe      */
DECL|method|beforeLocalFork
annotation|@
name|Override
specifier|public
name|void
name|beforeLocalFork
parameter_list|()
block|{
if|if
condition|(
name|sourceUnsafe
condition|)
block|{
name|source
argument_list|()
expr_stmt|;
block|}
block|}
DECL|method|source
specifier|public
name|byte
index|[]
name|source
parameter_list|()
block|{
if|if
condition|(
name|sourceUnsafe
operator|||
name|sourceOffset
operator|>
literal|0
operator|||
name|source
operator|.
name|length
operator|!=
name|sourceLength
condition|)
block|{
name|source
operator|=
name|Arrays
operator|.
name|copyOfRange
argument_list|(
name|source
argument_list|,
name|sourceOffset
argument_list|,
name|sourceOffset
operator|+
name|sourceLength
argument_list|)
expr_stmt|;
name|sourceOffset
operator|=
literal|0
expr_stmt|;
name|sourceUnsafe
operator|=
literal|false
expr_stmt|;
block|}
return|return
name|source
return|;
block|}
DECL|method|underlyingSource
specifier|public
name|byte
index|[]
name|underlyingSource
parameter_list|()
block|{
return|return
name|this
operator|.
name|source
return|;
block|}
DECL|method|underlyingSourceOffset
specifier|public
name|int
name|underlyingSourceOffset
parameter_list|()
block|{
return|return
name|this
operator|.
name|sourceOffset
return|;
block|}
DECL|method|underlyingSourceLength
specifier|public
name|int
name|underlyingSourceLength
parameter_list|()
block|{
return|return
name|this
operator|.
name|sourceLength
return|;
block|}
DECL|method|source
annotation|@
name|Required
specifier|public
name|PercolateRequest
name|source
parameter_list|(
name|Map
name|source
parameter_list|)
throws|throws
name|ElasticSearchGenerationException
block|{
return|return
name|source
argument_list|(
name|source
argument_list|,
name|XContentType
operator|.
name|SMILE
argument_list|)
return|;
block|}
DECL|method|source
annotation|@
name|Required
specifier|public
name|PercolateRequest
name|source
parameter_list|(
name|Map
name|source
parameter_list|,
name|XContentType
name|contentType
parameter_list|)
throws|throws
name|ElasticSearchGenerationException
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
name|source
argument_list|)
expr_stmt|;
return|return
name|source
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
name|source
operator|+
literal|"]"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
DECL|method|source
annotation|@
name|Required
specifier|public
name|PercolateRequest
name|source
parameter_list|(
name|String
name|source
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
name|source
argument_list|)
decl_stmt|;
name|this
operator|.
name|source
operator|=
name|result
operator|.
name|result
expr_stmt|;
name|this
operator|.
name|sourceOffset
operator|=
literal|0
expr_stmt|;
name|this
operator|.
name|sourceLength
operator|=
name|result
operator|.
name|length
expr_stmt|;
name|this
operator|.
name|sourceUnsafe
operator|=
literal|true
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|source
annotation|@
name|Required
specifier|public
name|PercolateRequest
name|source
parameter_list|(
name|XContentBuilder
name|sourceBuilder
parameter_list|)
block|{
try|try
block|{
name|source
operator|=
name|sourceBuilder
operator|.
name|underlyingBytes
argument_list|()
expr_stmt|;
name|sourceOffset
operator|=
literal|0
expr_stmt|;
name|sourceLength
operator|=
name|sourceBuilder
operator|.
name|underlyingBytesLength
argument_list|()
expr_stmt|;
name|sourceUnsafe
operator|=
literal|false
expr_stmt|;
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
name|sourceBuilder
operator|+
literal|"]"
argument_list|,
name|e
argument_list|)
throw|;
block|}
return|return
name|this
return|;
block|}
DECL|method|source
specifier|public
name|PercolateRequest
name|source
parameter_list|(
name|byte
index|[]
name|source
parameter_list|)
block|{
return|return
name|source
argument_list|(
name|source
argument_list|,
literal|0
argument_list|,
name|source
operator|.
name|length
argument_list|)
return|;
block|}
DECL|method|source
annotation|@
name|Required
specifier|public
name|PercolateRequest
name|source
parameter_list|(
name|byte
index|[]
name|source
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
block|{
return|return
name|source
argument_list|(
name|source
argument_list|,
name|offset
argument_list|,
name|length
argument_list|,
literal|false
argument_list|)
return|;
block|}
DECL|method|source
annotation|@
name|Required
specifier|public
name|PercolateRequest
name|source
parameter_list|(
name|byte
index|[]
name|source
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
name|source
operator|=
name|source
expr_stmt|;
name|this
operator|.
name|sourceOffset
operator|=
name|offset
expr_stmt|;
name|this
operator|.
name|sourceLength
operator|=
name|length
expr_stmt|;
name|this
operator|.
name|sourceUnsafe
operator|=
name|unsafe
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * if this operation hits a node with a local relevant shard, should it be preferred      * to be executed on, or just do plain round robin. Defaults to<tt>true</tt>      */
DECL|method|preferLocal
annotation|@
name|Override
specifier|public
name|PercolateRequest
name|preferLocal
parameter_list|(
name|boolean
name|preferLocal
parameter_list|)
block|{
name|super
operator|.
name|preferLocal
argument_list|(
name|preferLocal
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
name|index
operator|==
literal|null
condition|)
block|{
name|validationException
operator|=
name|addValidationError
argument_list|(
literal|"index is missing"
argument_list|,
name|validationException
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|type
operator|==
literal|null
condition|)
block|{
name|validationException
operator|=
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
name|source
operator|==
literal|null
condition|)
block|{
name|validationException
operator|=
name|addValidationError
argument_list|(
literal|"source is missing"
argument_list|,
name|validationException
argument_list|)
expr_stmt|;
block|}
return|return
name|validationException
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
name|index
operator|=
name|in
operator|.
name|readUTF
argument_list|()
expr_stmt|;
name|type
operator|=
name|in
operator|.
name|readUTF
argument_list|()
expr_stmt|;
name|sourceUnsafe
operator|=
literal|false
expr_stmt|;
name|sourceOffset
operator|=
literal|0
expr_stmt|;
name|sourceLength
operator|=
name|in
operator|.
name|readVInt
argument_list|()
expr_stmt|;
name|source
operator|=
operator|new
name|byte
index|[
name|sourceLength
index|]
expr_stmt|;
name|in
operator|.
name|readFully
argument_list|(
name|source
argument_list|)
expr_stmt|;
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
name|writeUTF
argument_list|(
name|index
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
name|writeVInt
argument_list|(
name|sourceLength
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBytes
argument_list|(
name|source
argument_list|,
name|sourceOffset
argument_list|,
name|sourceLength
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

