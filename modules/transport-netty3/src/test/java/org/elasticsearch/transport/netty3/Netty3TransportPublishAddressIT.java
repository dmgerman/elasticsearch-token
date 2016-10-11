begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.transport.netty3
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|transport
operator|.
name|netty3
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ESNetty3IntegTestCase
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
name|common
operator|.
name|network
operator|.
name|NetworkUtils
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
name|common
operator|.
name|transport
operator|.
name|BoundTransportAddress
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
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|ESIntegTestCase
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|Inet4Address
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|equalTo
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|greaterThan
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|instanceOf
import|;
end_import

begin_comment
comment|/**  * Checks that Elasticsearch produces a sane publish_address when it binds to  * different ports on ipv4 and ipv6.  */
end_comment

begin_class
annotation|@
name|ESIntegTestCase
operator|.
name|ClusterScope
argument_list|(
name|scope
operator|=
name|ESIntegTestCase
operator|.
name|Scope
operator|.
name|TEST
argument_list|,
name|numDataNodes
operator|=
literal|0
argument_list|)
DECL|class|Netty3TransportPublishAddressIT
specifier|public
class|class
name|Netty3TransportPublishAddressIT
extends|extends
name|ESNetty3IntegTestCase
block|{
DECL|method|testDifferentPorts
specifier|public
name|void
name|testDifferentPorts
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
operator|!
name|NetworkUtils
operator|.
name|SUPPORTS_V6
condition|)
block|{
return|return;
block|}
name|logger
operator|.
name|info
argument_list|(
literal|"--> starting a node on ipv4 only"
argument_list|)
expr_stmt|;
name|Settings
name|ipv4Settings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"network.host"
argument_list|,
literal|"127.0.0.1"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|String
name|ipv4OnlyNode
init|=
name|internalCluster
argument_list|()
operator|.
name|startNode
argument_list|(
name|ipv4Settings
argument_list|)
decl_stmt|;
comment|// should bind 127.0.0.1:XYZ
name|logger
operator|.
name|info
argument_list|(
literal|"--> starting a node on ipv4 and ipv6"
argument_list|)
expr_stmt|;
name|Settings
name|bothSettings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"network.host"
argument_list|,
literal|"_local_"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|internalCluster
argument_list|()
operator|.
name|startNode
argument_list|(
name|bothSettings
argument_list|)
expr_stmt|;
comment|// should bind [::1]:XYZ and 127.0.0.1:XYZ+1
name|logger
operator|.
name|info
argument_list|(
literal|"--> waiting for the cluster to declare itself stable"
argument_list|)
expr_stmt|;
name|ensureStableCluster
argument_list|(
literal|2
argument_list|)
expr_stmt|;
comment|// fails if port of publish address does not match corresponding bound address
name|logger
operator|.
name|info
argument_list|(
literal|"--> checking if boundAddress matching publishAddress has same port"
argument_list|)
expr_stmt|;
name|NodesInfoResponse
name|nodesInfoResponse
init|=
name|client
argument_list|()
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
name|get
argument_list|()
decl_stmt|;
for|for
control|(
name|NodeInfo
name|nodeInfo
range|:
name|nodesInfoResponse
operator|.
name|getNodes
argument_list|()
control|)
block|{
name|BoundTransportAddress
name|boundTransportAddress
init|=
name|nodeInfo
operator|.
name|getTransport
argument_list|()
operator|.
name|getAddress
argument_list|()
decl_stmt|;
if|if
condition|(
name|nodeInfo
operator|.
name|getNode
argument_list|()
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
name|ipv4OnlyNode
argument_list|)
condition|)
block|{
name|assertThat
argument_list|(
name|boundTransportAddress
operator|.
name|boundAddresses
argument_list|()
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|boundTransportAddress
operator|.
name|boundAddresses
argument_list|()
index|[
literal|0
index|]
operator|.
name|getPort
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|boundTransportAddress
operator|.
name|publishAddress
argument_list|()
operator|.
name|getPort
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertThat
argument_list|(
name|boundTransportAddress
operator|.
name|boundAddresses
argument_list|()
operator|.
name|length
argument_list|,
name|greaterThan
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|TransportAddress
name|boundAddress
range|:
name|boundTransportAddress
operator|.
name|boundAddresses
argument_list|()
control|)
block|{
name|assertThat
argument_list|(
name|boundAddress
argument_list|,
name|instanceOf
argument_list|(
name|TransportAddress
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|TransportAddress
name|inetBoundAddress
init|=
operator|(
name|TransportAddress
operator|)
name|boundAddress
decl_stmt|;
if|if
condition|(
name|inetBoundAddress
operator|.
name|address
argument_list|()
operator|.
name|getAddress
argument_list|()
operator|instanceof
name|Inet4Address
condition|)
block|{
comment|// IPv4 address is preferred publish address for _local_
name|assertThat
argument_list|(
name|inetBoundAddress
operator|.
name|getPort
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|boundTransportAddress
operator|.
name|publishAddress
argument_list|()
operator|.
name|getPort
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
block|}
end_class

end_unit

