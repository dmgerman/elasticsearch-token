begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.indices.mapping.put
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|admin
operator|.
name|indices
operator|.
name|mapping
operator|.
name|put
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|ack
operator|.
name|IndicesClusterStateUpdateRequest
import|;
end_import

begin_comment
comment|/**  * Cluster state update request that allows to put a mapping  */
end_comment

begin_class
DECL|class|PutMappingClusterStateUpdateRequest
specifier|public
class|class
name|PutMappingClusterStateUpdateRequest
extends|extends
name|IndicesClusterStateUpdateRequest
argument_list|<
name|PutMappingClusterStateUpdateRequest
argument_list|>
block|{
DECL|field|type
specifier|private
name|String
name|type
decl_stmt|;
DECL|field|source
specifier|private
name|String
name|source
decl_stmt|;
DECL|field|ignoreConflicts
specifier|private
name|boolean
name|ignoreConflicts
init|=
literal|false
decl_stmt|;
DECL|method|PutMappingClusterStateUpdateRequest
name|PutMappingClusterStateUpdateRequest
parameter_list|()
block|{      }
DECL|method|type
specifier|public
name|String
name|type
parameter_list|()
block|{
return|return
name|type
return|;
block|}
DECL|method|type
specifier|public
name|PutMappingClusterStateUpdateRequest
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
DECL|method|source
specifier|public
name|String
name|source
parameter_list|()
block|{
return|return
name|source
return|;
block|}
DECL|method|source
specifier|public
name|PutMappingClusterStateUpdateRequest
name|source
parameter_list|(
name|String
name|source
parameter_list|)
block|{
name|this
operator|.
name|source
operator|=
name|source
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|ignoreConflicts
specifier|public
name|boolean
name|ignoreConflicts
parameter_list|()
block|{
return|return
name|ignoreConflicts
return|;
block|}
DECL|method|ignoreConflicts
specifier|public
name|PutMappingClusterStateUpdateRequest
name|ignoreConflicts
parameter_list|(
name|boolean
name|ignoreConflicts
parameter_list|)
block|{
name|this
operator|.
name|ignoreConflicts
operator|=
name|ignoreConflicts
expr_stmt|;
return|return
name|this
return|;
block|}
block|}
end_class

end_unit

