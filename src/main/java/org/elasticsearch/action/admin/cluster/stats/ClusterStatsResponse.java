begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.cluster.stats
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
name|stats
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
name|nodes
operator|.
name|NodesOperationResponse
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
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|xcontent
operator|.
name|XContentBuilderString
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
name|XContentFactory
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
comment|/**  *  */
end_comment

begin_class
DECL|class|ClusterStatsResponse
specifier|public
class|class
name|ClusterStatsResponse
extends|extends
name|NodesOperationResponse
argument_list|<
name|ClusterStatsNodeResponse
argument_list|>
implements|implements
name|ToXContent
block|{
DECL|field|nodesStats
name|ClusterStatsNodes
name|nodesStats
decl_stmt|;
DECL|field|indicesStats
name|ClusterStatsIndices
name|indicesStats
decl_stmt|;
DECL|field|clusterUUID
name|String
name|clusterUUID
decl_stmt|;
DECL|method|ClusterStatsResponse
name|ClusterStatsResponse
parameter_list|()
block|{     }
DECL|method|ClusterStatsResponse
specifier|public
name|ClusterStatsResponse
parameter_list|(
name|ClusterName
name|clusterName
parameter_list|,
name|String
name|clusterUUID
parameter_list|,
name|ClusterStatsNodeResponse
index|[]
name|nodes
parameter_list|)
block|{
name|super
argument_list|(
name|clusterName
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|this
operator|.
name|clusterUUID
operator|=
name|clusterUUID
expr_stmt|;
name|nodesStats
operator|=
operator|new
name|ClusterStatsNodes
argument_list|(
name|nodes
argument_list|)
expr_stmt|;
name|indicesStats
operator|=
operator|new
name|ClusterStatsIndices
argument_list|(
name|nodes
argument_list|)
expr_stmt|;
block|}
DECL|method|getNodesStats
specifier|public
name|ClusterStatsNodes
name|getNodesStats
parameter_list|()
block|{
return|return
name|nodesStats
return|;
block|}
DECL|method|getIndicesStats
specifier|public
name|ClusterStatsIndices
name|getIndicesStats
parameter_list|()
block|{
return|return
name|indicesStats
return|;
block|}
annotation|@
name|Override
DECL|method|getNodes
specifier|public
name|ClusterStatsNodeResponse
index|[]
name|getNodes
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
DECL|method|getNodesMap
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|ClusterStatsNodeResponse
argument_list|>
name|getNodesMap
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
DECL|method|getAt
specifier|public
name|ClusterStatsNodeResponse
name|getAt
parameter_list|(
name|int
name|position
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
DECL|method|iterator
specifier|public
name|Iterator
argument_list|<
name|ClusterStatsNodeResponse
argument_list|>
name|iterator
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
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
name|clusterUUID
operator|=
name|in
operator|.
name|readString
argument_list|()
expr_stmt|;
name|nodesStats
operator|=
name|ClusterStatsNodes
operator|.
name|readNodeStats
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|indicesStats
operator|=
name|ClusterStatsIndices
operator|.
name|readIndicesStats
argument_list|(
name|in
argument_list|)
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
name|writeString
argument_list|(
name|clusterUUID
argument_list|)
expr_stmt|;
name|nodesStats
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|indicesStats
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
DECL|class|Fields
specifier|static
specifier|final
class|class
name|Fields
block|{
DECL|field|NODES
specifier|static
specifier|final
name|XContentBuilderString
name|NODES
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"nodes"
argument_list|)
decl_stmt|;
DECL|field|INDICES
specifier|static
specifier|final
name|XContentBuilderString
name|INDICES
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"indices"
argument_list|)
decl_stmt|;
DECL|field|UUID
specifier|static
specifier|final
name|XContentBuilderString
name|UUID
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"uuid"
argument_list|)
decl_stmt|;
DECL|field|CLUSTER_NAME
specifier|static
specifier|final
name|XContentBuilderString
name|CLUSTER_NAME
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"cluster_name"
argument_list|)
decl_stmt|;
block|}
annotation|@
name|Override
DECL|method|toXContent
specifier|public
name|XContentBuilder
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
name|field
argument_list|(
name|Fields
operator|.
name|CLUSTER_NAME
argument_list|,
name|getClusterName
argument_list|()
operator|.
name|value
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|params
operator|.
name|paramAsBoolean
argument_list|(
literal|"output_uuid"
argument_list|,
literal|false
argument_list|)
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|UUID
argument_list|,
name|clusterUUID
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|startObject
argument_list|(
name|Fields
operator|.
name|INDICES
argument_list|)
expr_stmt|;
name|indicesStats
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|startObject
argument_list|(
name|Fields
operator|.
name|NODES
argument_list|)
expr_stmt|;
name|nodesStats
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
return|return
name|builder
return|;
block|}
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
try|try
block|{
name|XContentBuilder
name|builder
init|=
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|prettyPrint
argument_list|()
decl_stmt|;
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|toXContent
argument_list|(
name|builder
argument_list|,
name|EMPTY_PARAMS
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
return|return
name|builder
operator|.
name|string
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
return|return
literal|"{ \"error\" : \""
operator|+
name|e
operator|.
name|getMessage
argument_list|()
operator|+
literal|"\"}"
return|;
block|}
block|}
block|}
end_class

end_unit

