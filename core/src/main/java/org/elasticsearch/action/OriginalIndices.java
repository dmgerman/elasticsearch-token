begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
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
name|IndicesOptions
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
import|;
end_import

begin_comment
comment|/**  * Used to keep track of original indices within internal (e.g. shard level) requests  */
end_comment

begin_class
DECL|class|OriginalIndices
specifier|public
specifier|final
class|class
name|OriginalIndices
implements|implements
name|IndicesRequest
block|{
comment|//constant to use when original indices are not applicable and will not be serialized across the wire
DECL|field|NONE
specifier|public
specifier|static
specifier|final
name|OriginalIndices
name|NONE
init|=
operator|new
name|OriginalIndices
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
DECL|field|indices
specifier|private
specifier|final
name|String
index|[]
name|indices
decl_stmt|;
DECL|field|indicesOptions
specifier|private
specifier|final
name|IndicesOptions
name|indicesOptions
decl_stmt|;
DECL|method|OriginalIndices
specifier|public
name|OriginalIndices
parameter_list|(
name|IndicesRequest
name|indicesRequest
parameter_list|)
block|{
name|this
argument_list|(
name|indicesRequest
operator|.
name|indices
argument_list|()
argument_list|,
name|indicesRequest
operator|.
name|indicesOptions
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|OriginalIndices
specifier|public
name|OriginalIndices
parameter_list|(
name|String
index|[]
name|indices
parameter_list|,
name|IndicesOptions
name|indicesOptions
parameter_list|)
block|{
name|this
operator|.
name|indices
operator|=
name|indices
expr_stmt|;
name|this
operator|.
name|indicesOptions
operator|=
name|indicesOptions
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|indices
specifier|public
name|String
index|[]
name|indices
parameter_list|()
block|{
return|return
name|indices
return|;
block|}
annotation|@
name|Override
DECL|method|indicesOptions
specifier|public
name|IndicesOptions
name|indicesOptions
parameter_list|()
block|{
return|return
name|indicesOptions
return|;
block|}
DECL|method|readOriginalIndices
specifier|public
specifier|static
name|OriginalIndices
name|readOriginalIndices
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|OriginalIndices
argument_list|(
name|in
operator|.
name|readStringArray
argument_list|()
argument_list|,
name|IndicesOptions
operator|.
name|readIndicesOptions
argument_list|(
name|in
argument_list|)
argument_list|)
return|;
block|}
DECL|method|writeOriginalIndices
specifier|public
specifier|static
name|void
name|writeOriginalIndices
parameter_list|(
name|OriginalIndices
name|originalIndices
parameter_list|,
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
assert|assert
name|originalIndices
operator|!=
name|NONE
assert|;
name|out
operator|.
name|writeStringArrayNullable
argument_list|(
name|originalIndices
operator|.
name|indices
argument_list|)
expr_stmt|;
name|originalIndices
operator|.
name|indicesOptions
operator|.
name|writeIndicesOptions
argument_list|(
name|out
argument_list|)
expr_stmt|;
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
literal|"OriginalIndices{"
operator|+
literal|"indices="
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|indices
argument_list|)
operator|+
literal|", indicesOptions="
operator|+
name|indicesOptions
operator|+
literal|'}'
return|;
block|}
block|}
end_class

end_unit

