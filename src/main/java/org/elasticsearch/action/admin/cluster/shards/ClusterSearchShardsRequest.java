begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.cluster.shards
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|admin
operator|.
name|cluster
operator|.
name|shards
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticSearchIllegalArgumentException
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
name|IndicesOptions
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
name|master
operator|.
name|MasterNodeOperationRequest
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
name|Nullable
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
name|Strings
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

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|ClusterSearchShardsRequest
specifier|public
class|class
name|ClusterSearchShardsRequest
extends|extends
name|MasterNodeOperationRequest
argument_list|<
name|ClusterSearchShardsRequest
argument_list|>
block|{
DECL|field|indices
specifier|private
name|String
index|[]
name|indices
decl_stmt|;
annotation|@
name|Nullable
DECL|field|routing
specifier|private
name|String
name|routing
decl_stmt|;
annotation|@
name|Nullable
DECL|field|preference
specifier|private
name|String
name|preference
decl_stmt|;
DECL|field|local
specifier|private
name|boolean
name|local
init|=
literal|false
decl_stmt|;
DECL|field|types
specifier|private
name|String
index|[]
name|types
init|=
name|Strings
operator|.
name|EMPTY_ARRAY
decl_stmt|;
DECL|field|indicesOptions
specifier|private
name|IndicesOptions
name|indicesOptions
init|=
name|IndicesOptions
operator|.
name|lenient
argument_list|()
decl_stmt|;
DECL|method|ClusterSearchShardsRequest
specifier|public
name|ClusterSearchShardsRequest
parameter_list|()
block|{     }
DECL|method|ClusterSearchShardsRequest
specifier|public
name|ClusterSearchShardsRequest
parameter_list|(
name|String
modifier|...
name|indices
parameter_list|)
block|{
name|indices
argument_list|(
name|indices
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|validate
specifier|public
name|ActionRequestValidationException
name|validate
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
comment|/**      * Sets the indices the search will be executed on.      */
DECL|method|indices
specifier|public
name|ClusterSearchShardsRequest
name|indices
parameter_list|(
name|String
modifier|...
name|indices
parameter_list|)
block|{
if|if
condition|(
name|indices
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ElasticSearchIllegalArgumentException
argument_list|(
literal|"indices must not be null"
argument_list|)
throw|;
block|}
else|else
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|indices
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|indices
index|[
name|i
index|]
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ElasticSearchIllegalArgumentException
argument_list|(
literal|"indices["
operator|+
name|i
operator|+
literal|"] must not be null"
argument_list|)
throw|;
block|}
block|}
block|}
name|this
operator|.
name|indices
operator|=
name|indices
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The indices      */
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
DECL|method|indicesOptions
specifier|public
name|ClusterSearchShardsRequest
name|indicesOptions
parameter_list|(
name|IndicesOptions
name|indicesOptions
parameter_list|)
block|{
name|this
operator|.
name|indicesOptions
operator|=
name|indicesOptions
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The document types to execute the search against. Defaults to be executed against      * all types.      */
DECL|method|types
specifier|public
name|String
index|[]
name|types
parameter_list|()
block|{
return|return
name|types
return|;
block|}
comment|/**      * The document types to execute the search against. Defaults to be executed against      * all types.      */
DECL|method|types
specifier|public
name|ClusterSearchShardsRequest
name|types
parameter_list|(
name|String
modifier|...
name|types
parameter_list|)
block|{
name|this
operator|.
name|types
operator|=
name|types
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * A comma separated list of routing values to control the shards the search will be executed on.      */
DECL|method|routing
specifier|public
name|String
name|routing
parameter_list|()
block|{
return|return
name|this
operator|.
name|routing
return|;
block|}
comment|/**      * A comma separated list of routing values to control the shards the search will be executed on.      */
DECL|method|routing
specifier|public
name|ClusterSearchShardsRequest
name|routing
parameter_list|(
name|String
name|routing
parameter_list|)
block|{
name|this
operator|.
name|routing
operator|=
name|routing
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The routing values to control the shards that the search will be executed on.      */
DECL|method|routing
specifier|public
name|ClusterSearchShardsRequest
name|routing
parameter_list|(
name|String
modifier|...
name|routings
parameter_list|)
block|{
name|this
operator|.
name|routing
operator|=
name|Strings
operator|.
name|arrayToCommaDelimitedString
argument_list|(
name|routings
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets the preference to execute the search. Defaults to randomize across shards. Can be set to      *<tt>_local</tt> to prefer local shards,<tt>_primary</tt> to execute only on primary shards, or      * a custom value, which guarantees that the same order will be used across different requests.      */
DECL|method|preference
specifier|public
name|ClusterSearchShardsRequest
name|preference
parameter_list|(
name|String
name|preference
parameter_list|)
block|{
name|this
operator|.
name|preference
operator|=
name|preference
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|preference
specifier|public
name|String
name|preference
parameter_list|()
block|{
return|return
name|this
operator|.
name|preference
return|;
block|}
DECL|method|local
specifier|public
name|ClusterSearchShardsRequest
name|local
parameter_list|(
name|boolean
name|local
parameter_list|)
block|{
name|this
operator|.
name|local
operator|=
name|local
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|local
specifier|public
name|boolean
name|local
parameter_list|()
block|{
return|return
name|this
operator|.
name|local
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
name|super
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|indices
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
name|indices
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|indices
index|[
name|i
index|]
operator|=
name|in
operator|.
name|readString
argument_list|()
expr_stmt|;
block|}
name|routing
operator|=
name|in
operator|.
name|readOptionalString
argument_list|()
expr_stmt|;
name|preference
operator|=
name|in
operator|.
name|readOptionalString
argument_list|()
expr_stmt|;
name|types
operator|=
name|in
operator|.
name|readStringArray
argument_list|()
expr_stmt|;
name|indicesOptions
operator|=
name|IndicesOptions
operator|.
name|readIndicesOptions
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|local
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
name|indices
operator|.
name|length
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|index
range|:
name|indices
control|)
block|{
name|out
operator|.
name|writeString
argument_list|(
name|index
argument_list|)
expr_stmt|;
block|}
name|out
operator|.
name|writeOptionalString
argument_list|(
name|routing
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeOptionalString
argument_list|(
name|preference
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeStringArray
argument_list|(
name|types
argument_list|)
expr_stmt|;
name|indicesOptions
operator|.
name|writeIndicesOptions
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|local
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

