begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.discovery.zen.ping
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|discovery
operator|.
name|zen
operator|.
name|ping
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchException
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
name|cluster
operator|.
name|node
operator|.
name|DiscoveryNode
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
name|component
operator|.
name|LifecycleComponent
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
name|unit
operator|.
name|TimeValue
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
name|cluster
operator|.
name|ClusterName
operator|.
name|readClusterName
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|node
operator|.
name|DiscoveryNode
operator|.
name|readNode
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_interface
DECL|interface|ZenPing
specifier|public
interface|interface
name|ZenPing
extends|extends
name|LifecycleComponent
argument_list|<
name|ZenPing
argument_list|>
block|{
DECL|method|setPingContextProvider
name|void
name|setPingContextProvider
parameter_list|(
name|PingContextProvider
name|contextProvider
parameter_list|)
function_decl|;
DECL|method|ping
name|void
name|ping
parameter_list|(
name|PingListener
name|listener
parameter_list|,
name|TimeValue
name|timeout
parameter_list|)
throws|throws
name|ElasticsearchException
function_decl|;
DECL|interface|PingListener
specifier|public
interface|interface
name|PingListener
block|{
DECL|method|onPing
name|void
name|onPing
parameter_list|(
name|PingResponse
index|[]
name|pings
parameter_list|)
function_decl|;
block|}
DECL|class|PingResponse
specifier|public
specifier|static
class|class
name|PingResponse
implements|implements
name|Streamable
block|{
DECL|field|EMPTY
specifier|public
specifier|static
specifier|final
name|PingResponse
index|[]
name|EMPTY
init|=
operator|new
name|PingResponse
index|[
literal|0
index|]
decl_stmt|;
DECL|field|clusterName
specifier|private
name|ClusterName
name|clusterName
decl_stmt|;
DECL|field|node
specifier|private
name|DiscoveryNode
name|node
decl_stmt|;
DECL|field|master
specifier|private
name|DiscoveryNode
name|master
decl_stmt|;
DECL|field|hasJoinedOnce
specifier|private
name|boolean
name|hasJoinedOnce
decl_stmt|;
DECL|method|PingResponse
specifier|private
name|PingResponse
parameter_list|()
block|{         }
comment|/**          * @param node          the node which this ping describes          * @param master        the current master of the node          * @param clusterName   the cluster name of the node          * @param hasJoinedOnce true if the joined has successfully joined the cluster before          */
DECL|method|PingResponse
specifier|public
name|PingResponse
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|,
name|DiscoveryNode
name|master
parameter_list|,
name|ClusterName
name|clusterName
parameter_list|,
name|boolean
name|hasJoinedOnce
parameter_list|)
block|{
name|this
operator|.
name|node
operator|=
name|node
expr_stmt|;
name|this
operator|.
name|master
operator|=
name|master
expr_stmt|;
name|this
operator|.
name|clusterName
operator|=
name|clusterName
expr_stmt|;
name|this
operator|.
name|hasJoinedOnce
operator|=
name|hasJoinedOnce
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
comment|/** the node which this ping describes */
DECL|method|node
specifier|public
name|DiscoveryNode
name|node
parameter_list|()
block|{
return|return
name|node
return|;
block|}
comment|/** the current master of the node */
DECL|method|master
specifier|public
name|DiscoveryNode
name|master
parameter_list|()
block|{
return|return
name|master
return|;
block|}
comment|/** true if the joined has successfully joined the cluster before */
DECL|method|hasJoinedOnce
specifier|public
name|boolean
name|hasJoinedOnce
parameter_list|()
block|{
return|return
name|hasJoinedOnce
return|;
block|}
DECL|method|readPingResponse
specifier|public
specifier|static
name|PingResponse
name|readPingResponse
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|PingResponse
name|response
init|=
operator|new
name|PingResponse
argument_list|()
decl_stmt|;
name|response
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
return|return
name|response
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
name|clusterName
operator|=
name|readClusterName
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|node
operator|=
name|readNode
argument_list|(
name|in
argument_list|)
expr_stmt|;
if|if
condition|(
name|in
operator|.
name|readBoolean
argument_list|()
condition|)
block|{
name|master
operator|=
name|readNode
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|in
operator|.
name|getVersion
argument_list|()
operator|.
name|onOrAfter
argument_list|(
name|Version
operator|.
name|V_1_4_0_Beta
argument_list|)
condition|)
block|{
name|this
operator|.
name|hasJoinedOnce
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
block|}
else|else
block|{
comment|// As of 1.4.0 we prefer to elect nodes which have previously successfully joined the cluster.
comment|// Nodes before 1.4.0 do not take this into consideration. If pre<1.4.0 node elects it self as master
comment|// based on the pings, we need to make sure we do the same. We therefore can not demote it
comment|// and thus mark it as if it has previously joined.
name|this
operator|.
name|hasJoinedOnce
operator|=
literal|true
expr_stmt|;
block|}
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
name|clusterName
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|node
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
if|if
condition|(
name|master
operator|==
literal|null
condition|)
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|master
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|out
operator|.
name|getVersion
argument_list|()
operator|.
name|onOrAfter
argument_list|(
name|Version
operator|.
name|V_1_4_0_Beta
argument_list|)
condition|)
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
name|hasJoinedOnce
argument_list|)
expr_stmt|;
block|}
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
literal|"ping_response{node ["
operator|+
name|node
operator|+
literal|"], master ["
operator|+
name|master
operator|+
literal|"], hasJoinedOnce ["
operator|+
name|hasJoinedOnce
operator|+
literal|"], cluster_name["
operator|+
name|clusterName
operator|.
name|value
argument_list|()
operator|+
literal|"]}"
return|;
block|}
block|}
block|}
end_interface

end_unit

