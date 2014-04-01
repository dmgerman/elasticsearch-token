begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.test
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|test
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
name|Lists
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
name|cluster
operator|.
name|node
operator|.
name|info
operator|.
name|NodeInfo
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
name|cluster
operator|.
name|node
operator|.
name|info
operator|.
name|NodesInfoResponse
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|client
operator|.
name|Client
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|client
operator|.
name|transport
operator|.
name|TransportClient
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
name|logging
operator|.
name|ESLogger
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
name|logging
operator|.
name|Loggers
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
name|ImmutableSettings
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
name|transport
operator|.
name|InetSocketTransportAddress
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
name|transport
operator|.
name|TransportAddress
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|InetSocketAddress
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
comment|/**  * External cluster to run the tests against.  * It is a pure immutable test cluster that allows to send requests to a pre-existing cluster  * and supports by nature all the needed test operations like wipeIndices etc.  */
end_comment

begin_class
DECL|class|ExternalTestCluster
specifier|public
specifier|final
class|class
name|ExternalTestCluster
extends|extends
name|ImmutableTestCluster
block|{
DECL|field|logger
specifier|private
specifier|final
name|ESLogger
name|logger
init|=
name|Loggers
operator|.
name|getLogger
argument_list|(
name|getClass
argument_list|()
argument_list|)
decl_stmt|;
DECL|field|client
specifier|private
specifier|final
name|Client
name|client
decl_stmt|;
DECL|field|httpAddresses
specifier|private
specifier|final
name|InetSocketAddress
index|[]
name|httpAddresses
decl_stmt|;
DECL|field|dataNodes
specifier|private
specifier|final
name|int
name|dataNodes
decl_stmt|;
DECL|method|ExternalTestCluster
specifier|public
name|ExternalTestCluster
parameter_list|(
name|TransportAddress
modifier|...
name|transportAddresses
parameter_list|)
block|{
name|this
operator|.
name|client
operator|=
operator|new
name|TransportClient
argument_list|(
name|ImmutableSettings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"client.transport.ignore_cluster_name"
argument_list|,
literal|true
argument_list|)
argument_list|)
operator|.
name|addTransportAddresses
argument_list|(
name|transportAddresses
argument_list|)
expr_stmt|;
name|NodesInfoResponse
name|nodeInfos
init|=
name|this
operator|.
name|client
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareNodesInfo
argument_list|()
operator|.
name|clear
argument_list|()
operator|.
name|setSettings
argument_list|(
literal|true
argument_list|)
operator|.
name|setHttp
argument_list|(
literal|true
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|httpAddresses
operator|=
operator|new
name|InetSocketAddress
index|[
name|nodeInfos
operator|.
name|getNodes
argument_list|()
operator|.
name|length
index|]
expr_stmt|;
name|int
name|dataNodes
init|=
literal|0
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|nodeInfos
operator|.
name|getNodes
argument_list|()
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|NodeInfo
name|nodeInfo
init|=
name|nodeInfos
operator|.
name|getNodes
argument_list|()
index|[
name|i
index|]
decl_stmt|;
name|httpAddresses
index|[
name|i
index|]
operator|=
operator|(
operator|(
name|InetSocketTransportAddress
operator|)
name|nodeInfo
operator|.
name|getHttp
argument_list|()
operator|.
name|address
argument_list|()
operator|.
name|publishAddress
argument_list|()
operator|)
operator|.
name|address
argument_list|()
expr_stmt|;
if|if
condition|(
name|nodeInfo
operator|.
name|getSettings
argument_list|()
operator|.
name|getAsBoolean
argument_list|(
literal|"node.data"
argument_list|,
literal|true
argument_list|)
condition|)
block|{
name|dataNodes
operator|++
expr_stmt|;
block|}
block|}
name|this
operator|.
name|dataNodes
operator|=
name|dataNodes
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"Setup ExternalTestCluster [{}] made of [{}] nodes"
argument_list|,
name|nodeInfos
operator|.
name|getClusterName
argument_list|()
operator|.
name|value
argument_list|()
argument_list|,
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|afterTest
specifier|public
name|void
name|afterTest
parameter_list|()
block|{      }
annotation|@
name|Override
DECL|method|client
specifier|public
name|Client
name|client
parameter_list|()
block|{
return|return
name|client
return|;
block|}
annotation|@
name|Override
DECL|method|size
specifier|public
name|int
name|size
parameter_list|()
block|{
return|return
name|httpAddresses
operator|.
name|length
return|;
block|}
annotation|@
name|Override
DECL|method|dataNodes
specifier|public
name|int
name|dataNodes
parameter_list|()
block|{
return|return
name|dataNodes
return|;
block|}
annotation|@
name|Override
DECL|method|httpAddresses
specifier|public
name|InetSocketAddress
index|[]
name|httpAddresses
parameter_list|()
block|{
return|return
name|httpAddresses
return|;
block|}
annotation|@
name|Override
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
block|{
name|client
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|iterator
specifier|public
name|Iterator
argument_list|<
name|Client
argument_list|>
name|iterator
parameter_list|()
block|{
return|return
name|Lists
operator|.
name|newArrayList
argument_list|(
name|client
argument_list|)
operator|.
name|iterator
argument_list|()
return|;
block|}
block|}
end_class

end_unit

