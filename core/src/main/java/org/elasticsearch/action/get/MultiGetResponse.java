begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|ElasticsearchException
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
name|ActionResponse
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
name|io
operator|.
name|stream
operator|.
name|Streamable
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
name|ToXContentObject
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
name|Iterator
import|;
end_import

begin_class
DECL|class|MultiGetResponse
specifier|public
class|class
name|MultiGetResponse
extends|extends
name|ActionResponse
implements|implements
name|Iterable
argument_list|<
name|MultiGetItemResponse
argument_list|>
implements|,
name|ToXContentObject
block|{
comment|/**      * Represents a failure.      */
DECL|class|Failure
specifier|public
specifier|static
class|class
name|Failure
implements|implements
name|Streamable
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
DECL|field|id
specifier|private
name|String
name|id
decl_stmt|;
DECL|field|exception
specifier|private
name|Exception
name|exception
decl_stmt|;
DECL|method|Failure
name|Failure
parameter_list|()
block|{          }
DECL|method|Failure
specifier|public
name|Failure
parameter_list|(
name|String
name|index
parameter_list|,
name|String
name|type
parameter_list|,
name|String
name|id
parameter_list|,
name|Exception
name|exception
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
name|this
operator|.
name|id
operator|=
name|id
expr_stmt|;
name|this
operator|.
name|exception
operator|=
name|exception
expr_stmt|;
block|}
comment|/**          * The index name of the action.          */
DECL|method|getIndex
specifier|public
name|String
name|getIndex
parameter_list|()
block|{
return|return
name|this
operator|.
name|index
return|;
block|}
comment|/**          * The type of the action.          */
DECL|method|getType
specifier|public
name|String
name|getType
parameter_list|()
block|{
return|return
name|type
return|;
block|}
comment|/**          * The id of the action.          */
DECL|method|getId
specifier|public
name|String
name|getId
parameter_list|()
block|{
return|return
name|id
return|;
block|}
comment|/**          * The failure message.          */
DECL|method|getMessage
specifier|public
name|String
name|getMessage
parameter_list|()
block|{
return|return
name|exception
operator|!=
literal|null
condition|?
name|exception
operator|.
name|getMessage
argument_list|()
else|:
literal|null
return|;
block|}
DECL|method|readFailure
specifier|public
specifier|static
name|Failure
name|readFailure
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|Failure
name|failure
init|=
operator|new
name|Failure
argument_list|()
decl_stmt|;
name|failure
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
return|return
name|failure
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
name|index
operator|=
name|in
operator|.
name|readString
argument_list|()
expr_stmt|;
name|type
operator|=
name|in
operator|.
name|readOptionalString
argument_list|()
expr_stmt|;
name|id
operator|=
name|in
operator|.
name|readString
argument_list|()
expr_stmt|;
name|exception
operator|=
name|in
operator|.
name|readException
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
name|out
operator|.
name|writeString
argument_list|(
name|index
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeOptionalString
argument_list|(
name|type
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeString
argument_list|(
name|id
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeException
argument_list|(
name|exception
argument_list|)
expr_stmt|;
block|}
DECL|method|getFailure
specifier|public
name|Exception
name|getFailure
parameter_list|()
block|{
return|return
name|exception
return|;
block|}
block|}
DECL|field|responses
specifier|private
name|MultiGetItemResponse
index|[]
name|responses
decl_stmt|;
DECL|method|MultiGetResponse
name|MultiGetResponse
parameter_list|()
block|{     }
DECL|method|MultiGetResponse
specifier|public
name|MultiGetResponse
parameter_list|(
name|MultiGetItemResponse
index|[]
name|responses
parameter_list|)
block|{
name|this
operator|.
name|responses
operator|=
name|responses
expr_stmt|;
block|}
DECL|method|getResponses
specifier|public
name|MultiGetItemResponse
index|[]
name|getResponses
parameter_list|()
block|{
return|return
name|this
operator|.
name|responses
return|;
block|}
annotation|@
name|Override
DECL|method|iterator
specifier|public
name|Iterator
argument_list|<
name|MultiGetItemResponse
argument_list|>
name|iterator
parameter_list|()
block|{
return|return
name|Arrays
operator|.
name|stream
argument_list|(
name|responses
argument_list|)
operator|.
name|iterator
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|toXContent
specifier|public
name|XContentBuilder
name|toXContent
parameter_list|(
name|XContentBuilder
name|builder
parameter_list|,
name|Params
name|params
parameter_list|)
throws|throws
name|IOException
block|{
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|startArray
argument_list|(
name|Fields
operator|.
name|DOCS
argument_list|)
expr_stmt|;
for|for
control|(
name|MultiGetItemResponse
name|response
range|:
name|responses
control|)
block|{
if|if
condition|(
name|response
operator|.
name|isFailed
argument_list|()
condition|)
block|{
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|Failure
name|failure
init|=
name|response
operator|.
name|getFailure
argument_list|()
decl_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|_INDEX
argument_list|,
name|failure
operator|.
name|getIndex
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|_TYPE
argument_list|,
name|failure
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|_ID
argument_list|,
name|failure
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
name|ElasticsearchException
operator|.
name|generateFailureXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|,
name|failure
operator|.
name|getFailure
argument_list|()
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|GetResponse
name|getResponse
init|=
name|response
operator|.
name|getResponse
argument_list|()
decl_stmt|;
name|getResponse
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
block|}
block|}
name|builder
operator|.
name|endArray
argument_list|()
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
DECL|class|Fields
specifier|static
specifier|final
class|class
name|Fields
block|{
DECL|field|DOCS
specifier|static
specifier|final
name|String
name|DOCS
init|=
literal|"docs"
decl_stmt|;
DECL|field|_INDEX
specifier|static
specifier|final
name|String
name|_INDEX
init|=
literal|"_index"
decl_stmt|;
DECL|field|_TYPE
specifier|static
specifier|final
name|String
name|_TYPE
init|=
literal|"_type"
decl_stmt|;
DECL|field|_ID
specifier|static
specifier|final
name|String
name|_ID
init|=
literal|"_id"
decl_stmt|;
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
name|responses
operator|=
operator|new
name|MultiGetItemResponse
index|[
name|in
operator|.
name|readVInt
argument_list|()
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
name|responses
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|responses
index|[
name|i
index|]
operator|=
name|MultiGetItemResponse
operator|.
name|readItemResponse
argument_list|(
name|in
argument_list|)
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
name|writeVInt
argument_list|(
name|responses
operator|.
name|length
argument_list|)
expr_stmt|;
for|for
control|(
name|MultiGetItemResponse
name|response
range|:
name|responses
control|)
block|{
name|response
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

