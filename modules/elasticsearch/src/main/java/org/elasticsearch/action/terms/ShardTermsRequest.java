begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.terms
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|terms
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
name|BroadcastShardOperationRequest
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
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|ShardTermsRequest
class|class
name|ShardTermsRequest
extends|extends
name|BroadcastShardOperationRequest
block|{
DECL|field|fields
specifier|private
name|String
index|[]
name|fields
decl_stmt|;
DECL|field|from
specifier|private
name|String
name|from
decl_stmt|;
DECL|field|to
specifier|private
name|String
name|to
decl_stmt|;
DECL|field|fromInclusive
specifier|private
name|boolean
name|fromInclusive
init|=
literal|true
decl_stmt|;
DECL|field|toInclusive
specifier|private
name|boolean
name|toInclusive
init|=
literal|true
decl_stmt|;
DECL|field|prefix
specifier|private
name|String
name|prefix
decl_stmt|;
DECL|field|regexp
specifier|private
name|String
name|regexp
decl_stmt|;
DECL|field|size
specifier|private
name|int
name|size
init|=
literal|10
decl_stmt|;
DECL|field|convert
specifier|private
name|boolean
name|convert
init|=
literal|true
decl_stmt|;
DECL|field|sortType
specifier|private
name|TermsRequest
operator|.
name|SortType
name|sortType
decl_stmt|;
DECL|field|exact
specifier|private
name|boolean
name|exact
init|=
literal|false
decl_stmt|;
DECL|method|ShardTermsRequest
name|ShardTermsRequest
parameter_list|()
block|{     }
DECL|method|ShardTermsRequest
specifier|public
name|ShardTermsRequest
parameter_list|(
name|String
name|index
parameter_list|,
name|int
name|shardId
parameter_list|,
name|TermsRequest
name|request
parameter_list|)
block|{
name|super
argument_list|(
name|index
argument_list|,
name|shardId
argument_list|)
expr_stmt|;
name|this
operator|.
name|fields
operator|=
name|request
operator|.
name|fields
argument_list|()
expr_stmt|;
name|this
operator|.
name|from
operator|=
name|request
operator|.
name|from
argument_list|()
expr_stmt|;
name|this
operator|.
name|to
operator|=
name|request
operator|.
name|to
argument_list|()
expr_stmt|;
name|this
operator|.
name|fromInclusive
operator|=
name|request
operator|.
name|fromInclusive
argument_list|()
expr_stmt|;
name|this
operator|.
name|toInclusive
operator|=
name|request
operator|.
name|toInclusive
argument_list|()
expr_stmt|;
name|this
operator|.
name|prefix
operator|=
name|request
operator|.
name|prefix
argument_list|()
expr_stmt|;
name|this
operator|.
name|regexp
operator|=
name|request
operator|.
name|regexp
argument_list|()
expr_stmt|;
name|this
operator|.
name|size
operator|=
name|request
operator|.
name|size
argument_list|()
expr_stmt|;
name|this
operator|.
name|convert
operator|=
name|request
operator|.
name|convert
argument_list|()
expr_stmt|;
name|this
operator|.
name|sortType
operator|=
name|request
operator|.
name|sortType
argument_list|()
expr_stmt|;
name|this
operator|.
name|exact
operator|=
name|request
operator|.
name|exact
argument_list|()
expr_stmt|;
block|}
DECL|method|fields
specifier|public
name|String
index|[]
name|fields
parameter_list|()
block|{
return|return
name|fields
return|;
block|}
DECL|method|from
specifier|public
name|String
name|from
parameter_list|()
block|{
return|return
name|from
return|;
block|}
DECL|method|to
specifier|public
name|String
name|to
parameter_list|()
block|{
return|return
name|to
return|;
block|}
DECL|method|fromInclusive
specifier|public
name|boolean
name|fromInclusive
parameter_list|()
block|{
return|return
name|fromInclusive
return|;
block|}
DECL|method|toInclusive
specifier|public
name|boolean
name|toInclusive
parameter_list|()
block|{
return|return
name|toInclusive
return|;
block|}
DECL|method|prefix
specifier|public
name|String
name|prefix
parameter_list|()
block|{
return|return
name|prefix
return|;
block|}
DECL|method|regexp
specifier|public
name|String
name|regexp
parameter_list|()
block|{
return|return
name|regexp
return|;
block|}
DECL|method|size
specifier|public
name|int
name|size
parameter_list|()
block|{
return|return
name|size
return|;
block|}
DECL|method|convert
specifier|public
name|boolean
name|convert
parameter_list|()
block|{
return|return
name|convert
return|;
block|}
DECL|method|sortType
specifier|public
name|TermsRequest
operator|.
name|SortType
name|sortType
parameter_list|()
block|{
return|return
name|sortType
return|;
block|}
DECL|method|exact
specifier|public
name|boolean
name|exact
parameter_list|()
block|{
return|return
name|this
operator|.
name|exact
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
name|fields
operator|=
operator|new
name|String
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
name|fields
operator|.
name|length
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
if|if
condition|(
name|in
operator|.
name|readBoolean
argument_list|()
condition|)
block|{
name|from
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
name|to
operator|=
name|in
operator|.
name|readUTF
argument_list|()
expr_stmt|;
block|}
name|fromInclusive
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
name|toInclusive
operator|=
name|in
operator|.
name|readBoolean
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
name|prefix
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
name|regexp
operator|=
name|in
operator|.
name|readUTF
argument_list|()
expr_stmt|;
block|}
name|size
operator|=
name|in
operator|.
name|readVInt
argument_list|()
expr_stmt|;
name|convert
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
name|sortType
operator|=
name|TermsRequest
operator|.
name|SortType
operator|.
name|fromValue
argument_list|(
name|in
operator|.
name|readByte
argument_list|()
argument_list|)
expr_stmt|;
name|exact
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
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
name|writeVInt
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
if|if
condition|(
name|from
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
name|from
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|to
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
name|to
argument_list|)
expr_stmt|;
block|}
name|out
operator|.
name|writeBoolean
argument_list|(
name|fromInclusive
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|toInclusive
argument_list|)
expr_stmt|;
if|if
condition|(
name|prefix
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
name|prefix
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|regexp
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
name|regexp
argument_list|)
expr_stmt|;
block|}
name|out
operator|.
name|writeVInt
argument_list|(
name|size
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|convert
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeByte
argument_list|(
name|sortType
operator|.
name|value
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|exact
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

