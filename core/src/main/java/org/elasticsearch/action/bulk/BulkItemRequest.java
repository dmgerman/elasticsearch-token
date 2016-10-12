begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.bulk
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|bulk
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
name|DocWriteRequest
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
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_class
DECL|class|BulkItemRequest
specifier|public
class|class
name|BulkItemRequest
implements|implements
name|Streamable
block|{
DECL|field|id
specifier|private
name|int
name|id
decl_stmt|;
DECL|field|request
specifier|private
name|DocWriteRequest
name|request
decl_stmt|;
DECL|field|primaryResponse
specifier|private
specifier|volatile
name|BulkItemResponse
name|primaryResponse
decl_stmt|;
DECL|field|ignoreOnReplica
specifier|private
specifier|volatile
name|boolean
name|ignoreOnReplica
decl_stmt|;
DECL|method|BulkItemRequest
name|BulkItemRequest
parameter_list|()
block|{      }
DECL|method|BulkItemRequest
specifier|public
name|BulkItemRequest
parameter_list|(
name|int
name|id
parameter_list|,
name|DocWriteRequest
name|request
parameter_list|)
block|{
name|this
operator|.
name|id
operator|=
name|id
expr_stmt|;
name|this
operator|.
name|request
operator|=
name|request
expr_stmt|;
block|}
DECL|method|id
specifier|public
name|int
name|id
parameter_list|()
block|{
return|return
name|id
return|;
block|}
DECL|method|request
specifier|public
name|DocWriteRequest
name|request
parameter_list|()
block|{
return|return
name|request
return|;
block|}
DECL|method|index
specifier|public
name|String
name|index
parameter_list|()
block|{
assert|assert
name|request
operator|.
name|indices
argument_list|()
operator|.
name|length
operator|==
literal|1
assert|;
return|return
name|request
operator|.
name|indices
argument_list|()
index|[
literal|0
index|]
return|;
block|}
DECL|method|getPrimaryResponse
name|BulkItemResponse
name|getPrimaryResponse
parameter_list|()
block|{
return|return
name|primaryResponse
return|;
block|}
DECL|method|setPrimaryResponse
name|void
name|setPrimaryResponse
parameter_list|(
name|BulkItemResponse
name|primaryResponse
parameter_list|)
block|{
name|this
operator|.
name|primaryResponse
operator|=
name|primaryResponse
expr_stmt|;
block|}
comment|/**      * Marks this request to be ignored and *not* execute on a replica.      */
DECL|method|setIgnoreOnReplica
name|void
name|setIgnoreOnReplica
parameter_list|()
block|{
name|this
operator|.
name|ignoreOnReplica
operator|=
literal|true
expr_stmt|;
block|}
DECL|method|isIgnoreOnReplica
name|boolean
name|isIgnoreOnReplica
parameter_list|()
block|{
return|return
name|ignoreOnReplica
return|;
block|}
DECL|method|readBulkItem
specifier|public
specifier|static
name|BulkItemRequest
name|readBulkItem
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|BulkItemRequest
name|item
init|=
operator|new
name|BulkItemRequest
argument_list|()
decl_stmt|;
name|item
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
return|return
name|item
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
name|id
operator|=
name|in
operator|.
name|readVInt
argument_list|()
expr_stmt|;
name|request
operator|=
name|DocWriteRequest
operator|.
name|readDocumentRequest
argument_list|(
name|in
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
name|primaryResponse
operator|=
name|BulkItemResponse
operator|.
name|readBulkItem
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
name|ignoreOnReplica
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
name|out
operator|.
name|writeVInt
argument_list|(
name|id
argument_list|)
expr_stmt|;
name|DocWriteRequest
operator|.
name|writeDocumentRequest
argument_list|(
name|out
argument_list|,
name|request
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeOptionalStreamable
argument_list|(
name|primaryResponse
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|ignoreOnReplica
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

