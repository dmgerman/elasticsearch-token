begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.blobstore
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|blobstore
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
name|collect
operator|.
name|ImmutableList
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

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|BlobPath
specifier|public
class|class
name|BlobPath
implements|implements
name|Iterable
argument_list|<
name|String
argument_list|>
block|{
DECL|field|paths
specifier|private
specifier|final
name|ImmutableList
argument_list|<
name|String
argument_list|>
name|paths
decl_stmt|;
DECL|method|BlobPath
specifier|public
name|BlobPath
parameter_list|()
block|{
name|this
operator|.
name|paths
operator|=
name|ImmutableList
operator|.
name|of
argument_list|()
expr_stmt|;
block|}
DECL|method|cleanPath
specifier|public
specifier|static
name|BlobPath
name|cleanPath
parameter_list|()
block|{
return|return
operator|new
name|BlobPath
argument_list|()
return|;
block|}
DECL|method|BlobPath
specifier|private
name|BlobPath
parameter_list|(
name|ImmutableList
argument_list|<
name|String
argument_list|>
name|paths
parameter_list|)
block|{
name|this
operator|.
name|paths
operator|=
name|paths
expr_stmt|;
block|}
DECL|method|iterator
annotation|@
name|Override
specifier|public
name|Iterator
argument_list|<
name|String
argument_list|>
name|iterator
parameter_list|()
block|{
return|return
name|paths
operator|.
name|iterator
argument_list|()
return|;
block|}
DECL|method|toArray
specifier|public
name|String
index|[]
name|toArray
parameter_list|()
block|{
return|return
name|paths
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
name|paths
operator|.
name|size
argument_list|()
index|]
argument_list|)
return|;
block|}
DECL|method|add
specifier|public
name|BlobPath
name|add
parameter_list|(
name|String
name|path
parameter_list|)
block|{
name|ImmutableList
operator|.
name|Builder
argument_list|<
name|String
argument_list|>
name|builder
init|=
name|ImmutableList
operator|.
name|builder
argument_list|()
decl_stmt|;
return|return
operator|new
name|BlobPath
argument_list|(
name|builder
operator|.
name|addAll
argument_list|(
name|paths
argument_list|)
operator|.
name|add
argument_list|(
name|path
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
return|;
block|}
DECL|method|toString
annotation|@
name|Override
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
for|for
control|(
name|String
name|path
range|:
name|paths
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|'['
argument_list|)
operator|.
name|append
argument_list|(
name|path
argument_list|)
operator|.
name|append
argument_list|(
literal|']'
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
block|}
end_class

end_unit

