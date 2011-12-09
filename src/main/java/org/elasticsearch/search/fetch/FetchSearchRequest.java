begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.fetch
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|fetch
package|;
end_package

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
name|trove
operator|.
name|ExtTIntArrayList
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
comment|/**  *  */
end_comment

begin_class
DECL|class|FetchSearchRequest
specifier|public
class|class
name|FetchSearchRequest
implements|implements
name|Streamable
block|{
DECL|field|id
specifier|private
name|long
name|id
decl_stmt|;
DECL|field|docIds
specifier|private
name|int
index|[]
name|docIds
decl_stmt|;
DECL|field|size
specifier|private
name|int
name|size
decl_stmt|;
DECL|method|FetchSearchRequest
specifier|public
name|FetchSearchRequest
parameter_list|()
block|{     }
DECL|method|FetchSearchRequest
specifier|public
name|FetchSearchRequest
parameter_list|(
name|long
name|id
parameter_list|,
name|ExtTIntArrayList
name|list
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
name|docIds
operator|=
name|list
operator|.
name|unsafeArray
argument_list|()
expr_stmt|;
name|this
operator|.
name|size
operator|=
name|list
operator|.
name|size
argument_list|()
expr_stmt|;
block|}
DECL|method|FetchSearchRequest
specifier|public
name|FetchSearchRequest
parameter_list|(
name|long
name|id
parameter_list|,
name|int
index|[]
name|docIds
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
name|docIds
operator|=
name|docIds
expr_stmt|;
name|this
operator|.
name|size
operator|=
name|docIds
operator|.
name|length
expr_stmt|;
block|}
DECL|method|id
specifier|public
name|long
name|id
parameter_list|()
block|{
return|return
name|id
return|;
block|}
DECL|method|docIds
specifier|public
name|int
index|[]
name|docIds
parameter_list|()
block|{
return|return
name|docIds
return|;
block|}
DECL|method|docIdsSize
specifier|public
name|int
name|docIdsSize
parameter_list|()
block|{
return|return
name|size
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
name|readLong
argument_list|()
expr_stmt|;
name|size
operator|=
name|in
operator|.
name|readVInt
argument_list|()
expr_stmt|;
name|docIds
operator|=
operator|new
name|int
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
name|docIds
index|[
name|i
index|]
operator|=
name|in
operator|.
name|readVInt
argument_list|()
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
name|out
operator|.
name|writeLong
argument_list|(
name|id
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|size
argument_list|)
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
name|out
operator|.
name|writeVInt
argument_list|(
name|docIds
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

