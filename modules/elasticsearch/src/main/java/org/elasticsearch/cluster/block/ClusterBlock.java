begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.cluster.block
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|block
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
name|xcontent
operator|.
name|ToXContent
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
name|io
operator|.
name|Serializable
import|;
end_import

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|ClusterBlock
specifier|public
class|class
name|ClusterBlock
implements|implements
name|Serializable
implements|,
name|Streamable
implements|,
name|ToXContent
block|{
DECL|field|id
specifier|private
name|int
name|id
decl_stmt|;
DECL|field|description
specifier|private
name|String
name|description
decl_stmt|;
DECL|field|levels
specifier|private
name|ClusterBlockLevel
index|[]
name|levels
decl_stmt|;
DECL|method|ClusterBlock
specifier|private
name|ClusterBlock
parameter_list|()
block|{     }
DECL|method|ClusterBlock
specifier|public
name|ClusterBlock
parameter_list|(
name|int
name|id
parameter_list|,
name|String
name|description
parameter_list|,
name|ClusterBlockLevel
modifier|...
name|levels
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
name|description
operator|=
name|description
expr_stmt|;
name|this
operator|.
name|levels
operator|=
name|levels
expr_stmt|;
block|}
DECL|method|id
specifier|public
name|int
name|id
parameter_list|()
block|{
return|return
name|this
operator|.
name|id
return|;
block|}
DECL|method|description
specifier|public
name|String
name|description
parameter_list|()
block|{
return|return
name|this
operator|.
name|description
return|;
block|}
DECL|method|levels
specifier|public
name|ClusterBlockLevel
index|[]
name|levels
parameter_list|()
block|{
return|return
name|this
operator|.
name|levels
argument_list|()
return|;
block|}
DECL|method|contains
specifier|public
name|boolean
name|contains
parameter_list|(
name|ClusterBlockLevel
name|level
parameter_list|)
block|{
for|for
control|(
name|ClusterBlockLevel
name|testLevel
range|:
name|levels
control|)
block|{
if|if
condition|(
name|testLevel
operator|==
name|level
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
DECL|method|toXContent
annotation|@
name|Override
specifier|public
name|void
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
argument_list|(
name|Integer
operator|.
name|toString
argument_list|(
name|id
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"description"
argument_list|,
name|description
argument_list|)
expr_stmt|;
name|builder
operator|.
name|startArray
argument_list|(
literal|"levels"
argument_list|)
expr_stmt|;
for|for
control|(
name|ClusterBlockLevel
name|level
range|:
name|levels
control|)
block|{
name|builder
operator|.
name|value
argument_list|(
name|level
operator|.
name|name
argument_list|()
operator|.
name|toLowerCase
argument_list|()
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
DECL|method|readClusterBlock
specifier|public
specifier|static
name|ClusterBlock
name|readClusterBlock
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|ClusterBlock
name|block
init|=
operator|new
name|ClusterBlock
argument_list|()
decl_stmt|;
name|block
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
return|return
name|block
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
name|id
operator|=
name|in
operator|.
name|readVInt
argument_list|()
expr_stmt|;
name|description
operator|=
name|in
operator|.
name|readUTF
argument_list|()
expr_stmt|;
name|levels
operator|=
operator|new
name|ClusterBlockLevel
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
name|levels
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|levels
index|[
name|i
index|]
operator|=
name|ClusterBlockLevel
operator|.
name|fromId
argument_list|(
name|in
operator|.
name|readVInt
argument_list|()
argument_list|)
expr_stmt|;
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
name|writeVInt
argument_list|(
name|id
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeUTF
argument_list|(
name|description
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|levels
operator|.
name|length
argument_list|)
expr_stmt|;
for|for
control|(
name|ClusterBlockLevel
name|level
range|:
name|levels
control|)
block|{
name|out
operator|.
name|writeVInt
argument_list|(
name|level
operator|.
name|id
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|id
argument_list|)
operator|.
name|append
argument_list|(
literal|","
argument_list|)
operator|.
name|append
argument_list|(
name|description
argument_list|)
operator|.
name|append
argument_list|(
literal|", blocks "
argument_list|)
expr_stmt|;
for|for
control|(
name|ClusterBlockLevel
name|level
range|:
name|levels
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|level
operator|.
name|name
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|","
argument_list|)
expr_stmt|;
block|}
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
DECL|method|equals
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|o
condition|)
return|return
literal|true
return|;
if|if
condition|(
name|o
operator|==
literal|null
operator|||
name|getClass
argument_list|()
operator|!=
name|o
operator|.
name|getClass
argument_list|()
condition|)
return|return
literal|false
return|;
name|ClusterBlock
name|that
init|=
operator|(
name|ClusterBlock
operator|)
name|o
decl_stmt|;
if|if
condition|(
name|id
operator|!=
name|that
operator|.
name|id
condition|)
return|return
literal|false
return|;
return|return
literal|true
return|;
block|}
DECL|method|hashCode
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|id
return|;
block|}
block|}
end_class

end_unit

