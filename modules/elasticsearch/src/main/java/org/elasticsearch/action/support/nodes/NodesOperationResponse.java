begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.support.nodes
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|support
operator|.
name|nodes
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
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|ActionResponse
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
name|ClusterName
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutput
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
name|Iterator
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

begin_comment
comment|/**  * @author kimchy (Shay Banon)  */
end_comment

begin_class
DECL|class|NodesOperationResponse
specifier|public
specifier|abstract
class|class
name|NodesOperationResponse
parameter_list|<
name|NodeResponse
extends|extends
name|NodeOperationResponse
parameter_list|>
implements|implements
name|ActionResponse
implements|,
name|Iterable
argument_list|<
name|NodeResponse
argument_list|>
block|{
DECL|field|clusterName
specifier|private
name|ClusterName
name|clusterName
decl_stmt|;
DECL|field|nodes
specifier|protected
name|NodeResponse
index|[]
name|nodes
decl_stmt|;
DECL|field|nodesMap
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|NodeResponse
argument_list|>
name|nodesMap
decl_stmt|;
DECL|method|NodesOperationResponse
specifier|protected
name|NodesOperationResponse
parameter_list|()
block|{     }
DECL|method|NodesOperationResponse
specifier|protected
name|NodesOperationResponse
parameter_list|(
name|ClusterName
name|clusterName
parameter_list|,
name|NodeResponse
index|[]
name|nodes
parameter_list|)
block|{
name|this
operator|.
name|clusterName
operator|=
name|clusterName
expr_stmt|;
name|this
operator|.
name|nodes
operator|=
name|nodes
expr_stmt|;
block|}
DECL|method|clusterName
specifier|public
name|ClusterName
name|clusterName
parameter_list|()
block|{
return|return
name|this
operator|.
name|clusterName
return|;
block|}
DECL|method|nodes
specifier|public
name|NodeResponse
index|[]
name|nodes
parameter_list|()
block|{
return|return
name|nodes
return|;
block|}
DECL|method|iterator
annotation|@
name|Override
specifier|public
name|Iterator
argument_list|<
name|NodeResponse
argument_list|>
name|iterator
parameter_list|()
block|{
return|return
name|nodesMap
argument_list|()
operator|.
name|values
argument_list|()
operator|.
name|iterator
argument_list|()
return|;
block|}
DECL|method|nodesMap
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|NodeResponse
argument_list|>
name|nodesMap
parameter_list|()
block|{
if|if
condition|(
name|nodesMap
operator|==
literal|null
condition|)
block|{
name|nodesMap
operator|=
name|Maps
operator|.
name|newHashMap
argument_list|()
expr_stmt|;
for|for
control|(
name|NodeResponse
name|nodeResponse
range|:
name|nodes
control|)
block|{
name|nodesMap
operator|.
name|put
argument_list|(
name|nodeResponse
operator|.
name|node
argument_list|()
operator|.
name|id
argument_list|()
argument_list|,
name|nodeResponse
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|nodesMap
return|;
block|}
DECL|method|readFrom
annotation|@
name|Override
specifier|public
name|void
name|readFrom
parameter_list|(
name|DataInput
name|in
parameter_list|)
throws|throws
name|IOException
throws|,
name|ClassNotFoundException
block|{
name|clusterName
operator|=
name|ClusterName
operator|.
name|readClusterName
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
DECL|method|writeTo
annotation|@
name|Override
specifier|public
name|void
name|writeTo
parameter_list|(
name|DataOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|clusterName
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

