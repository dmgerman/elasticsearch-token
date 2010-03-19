begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.internal
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|internal
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|SearchHit
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|SearchHits
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
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|json
operator|.
name|JsonBuilder
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
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|internal
operator|.
name|InternalSearchHit
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * @author kimchy (Shay Banon)  */
end_comment

begin_class
DECL|class|InternalSearchHits
specifier|public
class|class
name|InternalSearchHits
implements|implements
name|SearchHits
block|{
DECL|field|EMPTY
specifier|private
specifier|static
specifier|final
name|SearchHit
index|[]
name|EMPTY
init|=
operator|new
name|SearchHit
index|[
literal|0
index|]
decl_stmt|;
DECL|field|hits
specifier|private
name|SearchHit
index|[]
name|hits
decl_stmt|;
DECL|field|totalHits
specifier|private
name|long
name|totalHits
decl_stmt|;
DECL|method|InternalSearchHits
specifier|private
name|InternalSearchHits
parameter_list|()
block|{      }
DECL|method|InternalSearchHits
specifier|public
name|InternalSearchHits
parameter_list|(
name|SearchHit
index|[]
name|hits
parameter_list|,
name|long
name|totalHits
parameter_list|)
block|{
name|this
operator|.
name|hits
operator|=
name|hits
expr_stmt|;
name|this
operator|.
name|totalHits
operator|=
name|totalHits
expr_stmt|;
block|}
DECL|method|totalHits
specifier|public
name|long
name|totalHits
parameter_list|()
block|{
return|return
name|totalHits
return|;
block|}
DECL|method|hits
specifier|public
name|SearchHit
index|[]
name|hits
parameter_list|()
block|{
return|return
name|this
operator|.
name|hits
return|;
block|}
DECL|method|toJson
annotation|@
name|Override
specifier|public
name|void
name|toJson
parameter_list|(
name|JsonBuilder
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
argument_list|(
literal|"hits"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"total"
argument_list|,
name|totalHits
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"hits"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|startArray
argument_list|()
expr_stmt|;
for|for
control|(
name|SearchHit
name|hit
range|:
name|hits
control|)
block|{
name|hit
operator|.
name|toJson
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
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
block|}
DECL|method|readSearchHits
specifier|public
specifier|static
name|InternalSearchHits
name|readSearchHits
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|InternalSearchHits
name|hits
init|=
operator|new
name|InternalSearchHits
argument_list|()
decl_stmt|;
name|hits
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
return|return
name|hits
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
name|totalHits
operator|=
name|in
operator|.
name|readVLong
argument_list|()
expr_stmt|;
name|int
name|size
init|=
name|in
operator|.
name|readVInt
argument_list|()
decl_stmt|;
if|if
condition|(
name|size
operator|==
literal|0
condition|)
block|{
name|hits
operator|=
name|EMPTY
expr_stmt|;
block|}
else|else
block|{
name|hits
operator|=
operator|new
name|SearchHit
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
name|hits
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|hits
index|[
name|i
index|]
operator|=
name|readSearchHit
argument_list|(
name|in
argument_list|)
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
name|out
operator|.
name|writeVLong
argument_list|(
name|totalHits
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|hits
operator|.
name|length
argument_list|)
expr_stmt|;
for|for
control|(
name|SearchHit
name|hit
range|:
name|hits
control|)
block|{
name|hit
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

