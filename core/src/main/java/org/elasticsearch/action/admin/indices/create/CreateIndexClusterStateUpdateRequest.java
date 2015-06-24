begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.indices.create
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
name|create
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Maps
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Sets
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
name|admin
operator|.
name|indices
operator|.
name|alias
operator|.
name|Alias
import|;
end_import

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
name|ClusterStateUpdateRequest
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|block
operator|.
name|ClusterBlock
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|metadata
operator|.
name|IndexMetaData
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
name|settings
operator|.
name|Settings
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|transport
operator|.
name|TransportMessage
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_import
import|import static
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Maps
operator|.
name|newHashMap
import|;
end_import

begin_comment
comment|/**  * Cluster state update request that allows to create an index  */
end_comment

begin_class
DECL|class|CreateIndexClusterStateUpdateRequest
specifier|public
class|class
name|CreateIndexClusterStateUpdateRequest
extends|extends
name|ClusterStateUpdateRequest
argument_list|<
name|CreateIndexClusterStateUpdateRequest
argument_list|>
block|{
DECL|field|originalMessage
specifier|private
specifier|final
name|TransportMessage
name|originalMessage
decl_stmt|;
DECL|field|cause
specifier|private
specifier|final
name|String
name|cause
decl_stmt|;
DECL|field|index
specifier|private
specifier|final
name|String
name|index
decl_stmt|;
DECL|field|updateAllTypes
specifier|private
specifier|final
name|boolean
name|updateAllTypes
decl_stmt|;
DECL|field|state
specifier|private
name|IndexMetaData
operator|.
name|State
name|state
init|=
name|IndexMetaData
operator|.
name|State
operator|.
name|OPEN
decl_stmt|;
DECL|field|settings
specifier|private
name|Settings
name|settings
init|=
name|Settings
operator|.
name|Builder
operator|.
name|EMPTY_SETTINGS
decl_stmt|;
DECL|field|mappings
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|mappings
init|=
name|Maps
operator|.
name|newHashMap
argument_list|()
decl_stmt|;
DECL|field|aliases
specifier|private
specifier|final
name|Set
argument_list|<
name|Alias
argument_list|>
name|aliases
init|=
name|Sets
operator|.
name|newHashSet
argument_list|()
decl_stmt|;
DECL|field|customs
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|IndexMetaData
operator|.
name|Custom
argument_list|>
name|customs
init|=
name|newHashMap
argument_list|()
decl_stmt|;
DECL|field|blocks
specifier|private
specifier|final
name|Set
argument_list|<
name|ClusterBlock
argument_list|>
name|blocks
init|=
name|Sets
operator|.
name|newHashSet
argument_list|()
decl_stmt|;
DECL|method|CreateIndexClusterStateUpdateRequest
name|CreateIndexClusterStateUpdateRequest
parameter_list|(
name|TransportMessage
name|originalMessage
parameter_list|,
name|String
name|cause
parameter_list|,
name|String
name|index
parameter_list|,
name|boolean
name|updateAllTypes
parameter_list|)
block|{
name|this
operator|.
name|originalMessage
operator|=
name|originalMessage
expr_stmt|;
name|this
operator|.
name|cause
operator|=
name|cause
expr_stmt|;
name|this
operator|.
name|index
operator|=
name|index
expr_stmt|;
name|this
operator|.
name|updateAllTypes
operator|=
name|updateAllTypes
expr_stmt|;
block|}
DECL|method|settings
specifier|public
name|CreateIndexClusterStateUpdateRequest
name|settings
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|this
operator|.
name|settings
operator|=
name|settings
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|mappings
specifier|public
name|CreateIndexClusterStateUpdateRequest
name|mappings
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|mappings
parameter_list|)
block|{
name|this
operator|.
name|mappings
operator|.
name|putAll
argument_list|(
name|mappings
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|aliases
specifier|public
name|CreateIndexClusterStateUpdateRequest
name|aliases
parameter_list|(
name|Set
argument_list|<
name|Alias
argument_list|>
name|aliases
parameter_list|)
block|{
name|this
operator|.
name|aliases
operator|.
name|addAll
argument_list|(
name|aliases
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|customs
specifier|public
name|CreateIndexClusterStateUpdateRequest
name|customs
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|IndexMetaData
operator|.
name|Custom
argument_list|>
name|customs
parameter_list|)
block|{
name|this
operator|.
name|customs
operator|.
name|putAll
argument_list|(
name|customs
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|blocks
specifier|public
name|CreateIndexClusterStateUpdateRequest
name|blocks
parameter_list|(
name|Set
argument_list|<
name|ClusterBlock
argument_list|>
name|blocks
parameter_list|)
block|{
name|this
operator|.
name|blocks
operator|.
name|addAll
argument_list|(
name|blocks
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|state
specifier|public
name|CreateIndexClusterStateUpdateRequest
name|state
parameter_list|(
name|IndexMetaData
operator|.
name|State
name|state
parameter_list|)
block|{
name|this
operator|.
name|state
operator|=
name|state
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|originalMessage
specifier|public
name|TransportMessage
name|originalMessage
parameter_list|()
block|{
return|return
name|originalMessage
return|;
block|}
DECL|method|cause
specifier|public
name|String
name|cause
parameter_list|()
block|{
return|return
name|cause
return|;
block|}
DECL|method|index
specifier|public
name|String
name|index
parameter_list|()
block|{
return|return
name|index
return|;
block|}
DECL|method|state
specifier|public
name|IndexMetaData
operator|.
name|State
name|state
parameter_list|()
block|{
return|return
name|state
return|;
block|}
DECL|method|settings
specifier|public
name|Settings
name|settings
parameter_list|()
block|{
return|return
name|settings
return|;
block|}
DECL|method|mappings
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|mappings
parameter_list|()
block|{
return|return
name|mappings
return|;
block|}
DECL|method|aliases
specifier|public
name|Set
argument_list|<
name|Alias
argument_list|>
name|aliases
parameter_list|()
block|{
return|return
name|aliases
return|;
block|}
DECL|method|customs
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|IndexMetaData
operator|.
name|Custom
argument_list|>
name|customs
parameter_list|()
block|{
return|return
name|customs
return|;
block|}
DECL|method|blocks
specifier|public
name|Set
argument_list|<
name|ClusterBlock
argument_list|>
name|blocks
parameter_list|()
block|{
return|return
name|blocks
return|;
block|}
comment|/** True if all fields that span multiple types should be updated, false otherwise */
DECL|method|updateAllTypes
specifier|public
name|boolean
name|updateAllTypes
parameter_list|()
block|{
return|return
name|updateAllTypes
return|;
block|}
block|}
end_class

end_unit

