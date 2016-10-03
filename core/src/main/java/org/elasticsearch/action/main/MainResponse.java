begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.main
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|main
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|Build
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|Version
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
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_class
DECL|class|MainResponse
specifier|public
class|class
name|MainResponse
extends|extends
name|ActionResponse
implements|implements
name|ToXContent
block|{
DECL|field|nodeName
specifier|private
name|String
name|nodeName
decl_stmt|;
DECL|field|version
specifier|private
name|Version
name|version
decl_stmt|;
DECL|field|clusterName
specifier|private
name|ClusterName
name|clusterName
decl_stmt|;
DECL|field|clusterUuid
specifier|private
name|String
name|clusterUuid
decl_stmt|;
DECL|field|build
specifier|private
name|Build
name|build
decl_stmt|;
DECL|field|available
specifier|private
name|boolean
name|available
decl_stmt|;
DECL|method|MainResponse
name|MainResponse
parameter_list|()
block|{     }
DECL|method|MainResponse
specifier|public
name|MainResponse
parameter_list|(
name|String
name|nodeName
parameter_list|,
name|Version
name|version
parameter_list|,
name|ClusterName
name|clusterName
parameter_list|,
name|String
name|clusterUuid
parameter_list|,
name|Build
name|build
parameter_list|,
name|boolean
name|available
parameter_list|)
block|{
name|this
operator|.
name|nodeName
operator|=
name|nodeName
expr_stmt|;
name|this
operator|.
name|version
operator|=
name|version
expr_stmt|;
name|this
operator|.
name|clusterName
operator|=
name|clusterName
expr_stmt|;
name|this
operator|.
name|clusterUuid
operator|=
name|clusterUuid
expr_stmt|;
name|this
operator|.
name|build
operator|=
name|build
expr_stmt|;
name|this
operator|.
name|available
operator|=
name|available
expr_stmt|;
block|}
DECL|method|getNodeName
specifier|public
name|String
name|getNodeName
parameter_list|()
block|{
return|return
name|nodeName
return|;
block|}
DECL|method|getVersion
specifier|public
name|Version
name|getVersion
parameter_list|()
block|{
return|return
name|version
return|;
block|}
DECL|method|getClusterName
specifier|public
name|ClusterName
name|getClusterName
parameter_list|()
block|{
return|return
name|clusterName
return|;
block|}
DECL|method|getClusterUuid
specifier|public
name|String
name|getClusterUuid
parameter_list|()
block|{
return|return
name|clusterUuid
return|;
block|}
DECL|method|getBuild
specifier|public
name|Build
name|getBuild
parameter_list|()
block|{
return|return
name|build
return|;
block|}
DECL|method|isAvailable
specifier|public
name|boolean
name|isAvailable
parameter_list|()
block|{
return|return
name|available
return|;
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
name|nodeName
argument_list|)
expr_stmt|;
name|Version
operator|.
name|writeVersion
argument_list|(
name|version
argument_list|,
name|out
argument_list|)
expr_stmt|;
name|clusterName
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
name|clusterUuid
argument_list|)
expr_stmt|;
name|Build
operator|.
name|writeBuild
argument_list|(
name|build
argument_list|,
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|available
argument_list|)
expr_stmt|;
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
name|nodeName
operator|=
name|in
operator|.
name|readString
argument_list|()
expr_stmt|;
name|version
operator|=
name|Version
operator|.
name|readVersion
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|clusterName
operator|=
operator|new
name|ClusterName
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|clusterUuid
operator|=
name|in
operator|.
name|readString
argument_list|()
expr_stmt|;
name|build
operator|=
name|Build
operator|.
name|readBuild
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|available
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
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
name|startObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"name"
argument_list|,
name|nodeName
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"cluster_name"
argument_list|,
name|clusterName
operator|.
name|value
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"cluster_uuid"
argument_list|,
name|clusterUuid
argument_list|)
expr_stmt|;
name|builder
operator|.
name|startObject
argument_list|(
literal|"version"
argument_list|)
operator|.
name|field
argument_list|(
literal|"number"
argument_list|,
name|version
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|field
argument_list|(
literal|"build_hash"
argument_list|,
name|build
operator|.
name|shortHash
argument_list|()
argument_list|)
operator|.
name|field
argument_list|(
literal|"build_date"
argument_list|,
name|build
operator|.
name|date
argument_list|()
argument_list|)
operator|.
name|field
argument_list|(
literal|"build_snapshot"
argument_list|,
name|build
operator|.
name|isSnapshot
argument_list|()
argument_list|)
operator|.
name|field
argument_list|(
literal|"lucene_version"
argument_list|,
name|version
operator|.
name|luceneVersion
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"tagline"
argument_list|,
literal|"You Know, for Search"
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
block|}
end_class

end_unit

