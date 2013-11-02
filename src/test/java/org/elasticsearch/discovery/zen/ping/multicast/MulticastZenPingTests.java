begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.discovery.zen.ping.multicast
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
operator|.
name|multicast
package|;
end_package

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
name|cluster
operator|.
name|node
operator|.
name|DiscoveryNodes
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
name|unit
operator|.
name|TimeValue
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
name|XContentFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|discovery
operator|.
name|zen
operator|.
name|DiscoveryNodesProvider
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|discovery
operator|.
name|zen
operator|.
name|ping
operator|.
name|ZenPing
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|node
operator|.
name|service
operator|.
name|NodeService
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
name|ElasticsearchTestCase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|threadpool
operator|.
name|ThreadPool
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
name|TransportService
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
name|local
operator|.
name|LocalTransport
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|DatagramPacket
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|InetAddress
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|MulticastSocket
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

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|MulticastZenPingTests
specifier|public
class|class
name|MulticastZenPingTests
extends|extends
name|ElasticsearchTestCase
block|{
DECL|method|buildRandomMulticast
specifier|private
name|Settings
name|buildRandomMulticast
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|ImmutableSettings
operator|.
name|Builder
name|builder
init|=
name|ImmutableSettings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|settings
argument_list|)
decl_stmt|;
name|builder
operator|.
name|put
argument_list|(
literal|"discovery.zen.ping.multicast.group"
argument_list|,
literal|"224.2.3."
operator|+
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|255
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|.
name|put
argument_list|(
literal|"discovery.zen.ping.multicast.port"
argument_list|,
name|randomIntBetween
argument_list|(
literal|55000
argument_list|,
literal|56000
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|builder
operator|.
name|build
argument_list|()
return|;
block|}
annotation|@
name|Test
DECL|method|testSimplePings
specifier|public
name|void
name|testSimplePings
parameter_list|()
block|{
name|Settings
name|settings
init|=
name|ImmutableSettings
operator|.
name|EMPTY
decl_stmt|;
name|settings
operator|=
name|buildRandomMulticast
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|ThreadPool
name|threadPool
init|=
operator|new
name|ThreadPool
argument_list|()
decl_stmt|;
name|ClusterName
name|clusterName
init|=
operator|new
name|ClusterName
argument_list|(
literal|"test"
argument_list|)
decl_stmt|;
specifier|final
name|TransportService
name|transportServiceA
init|=
operator|new
name|TransportService
argument_list|(
operator|new
name|LocalTransport
argument_list|(
name|settings
argument_list|,
name|threadPool
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
argument_list|,
name|threadPool
argument_list|)
operator|.
name|start
argument_list|()
decl_stmt|;
specifier|final
name|DiscoveryNode
name|nodeA
init|=
operator|new
name|DiscoveryNode
argument_list|(
literal|"A"
argument_list|,
name|transportServiceA
operator|.
name|boundAddress
argument_list|()
operator|.
name|publishAddress
argument_list|()
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
decl_stmt|;
specifier|final
name|TransportService
name|transportServiceB
init|=
operator|new
name|TransportService
argument_list|(
operator|new
name|LocalTransport
argument_list|(
name|settings
argument_list|,
name|threadPool
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
argument_list|,
name|threadPool
argument_list|)
operator|.
name|start
argument_list|()
decl_stmt|;
specifier|final
name|DiscoveryNode
name|nodeB
init|=
operator|new
name|DiscoveryNode
argument_list|(
literal|"B"
argument_list|,
name|transportServiceA
operator|.
name|boundAddress
argument_list|()
operator|.
name|publishAddress
argument_list|()
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
decl_stmt|;
name|MulticastZenPing
name|zenPingA
init|=
operator|new
name|MulticastZenPing
argument_list|(
name|threadPool
argument_list|,
name|transportServiceA
argument_list|,
name|clusterName
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
decl_stmt|;
name|zenPingA
operator|.
name|setNodesProvider
argument_list|(
operator|new
name|DiscoveryNodesProvider
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|DiscoveryNodes
name|nodes
parameter_list|()
block|{
return|return
name|DiscoveryNodes
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|nodeA
argument_list|)
operator|.
name|localNodeId
argument_list|(
literal|"A"
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|NodeService
name|nodeService
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|zenPingA
operator|.
name|start
argument_list|()
expr_stmt|;
name|MulticastZenPing
name|zenPingB
init|=
operator|new
name|MulticastZenPing
argument_list|(
name|threadPool
argument_list|,
name|transportServiceB
argument_list|,
name|clusterName
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
decl_stmt|;
name|zenPingB
operator|.
name|setNodesProvider
argument_list|(
operator|new
name|DiscoveryNodesProvider
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|DiscoveryNodes
name|nodes
parameter_list|()
block|{
return|return
name|DiscoveryNodes
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|nodeB
argument_list|)
operator|.
name|localNodeId
argument_list|(
literal|"B"
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|NodeService
name|nodeService
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|zenPingB
operator|.
name|start
argument_list|()
expr_stmt|;
try|try
block|{
name|ZenPing
operator|.
name|PingResponse
index|[]
name|pingResponses
init|=
name|zenPingA
operator|.
name|pingAndWait
argument_list|(
name|TimeValue
operator|.
name|timeValueSeconds
argument_list|(
literal|1
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|pingResponses
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
name|pingResponses
index|[
literal|0
index|]
operator|.
name|target
argument_list|()
operator|.
name|id
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"B"
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|zenPingA
operator|.
name|close
argument_list|()
expr_stmt|;
name|zenPingB
operator|.
name|close
argument_list|()
expr_stmt|;
name|transportServiceA
operator|.
name|close
argument_list|()
expr_stmt|;
name|transportServiceB
operator|.
name|close
argument_list|()
expr_stmt|;
name|threadPool
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Test
DECL|method|testExternalPing
specifier|public
name|void
name|testExternalPing
parameter_list|()
throws|throws
name|Exception
block|{
name|Settings
name|settings
init|=
name|ImmutableSettings
operator|.
name|EMPTY
decl_stmt|;
name|settings
operator|=
name|buildRandomMulticast
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|ThreadPool
name|threadPool
init|=
operator|new
name|ThreadPool
argument_list|()
decl_stmt|;
name|ClusterName
name|clusterName
init|=
operator|new
name|ClusterName
argument_list|(
literal|"test"
argument_list|)
decl_stmt|;
specifier|final
name|TransportService
name|transportServiceA
init|=
operator|new
name|TransportService
argument_list|(
operator|new
name|LocalTransport
argument_list|(
name|settings
argument_list|,
name|threadPool
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
argument_list|,
name|threadPool
argument_list|)
operator|.
name|start
argument_list|()
decl_stmt|;
specifier|final
name|DiscoveryNode
name|nodeA
init|=
operator|new
name|DiscoveryNode
argument_list|(
literal|"A"
argument_list|,
name|transportServiceA
operator|.
name|boundAddress
argument_list|()
operator|.
name|publishAddress
argument_list|()
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
decl_stmt|;
name|MulticastZenPing
name|zenPingA
init|=
operator|new
name|MulticastZenPing
argument_list|(
name|threadPool
argument_list|,
name|transportServiceA
argument_list|,
name|clusterName
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
decl_stmt|;
name|zenPingA
operator|.
name|setNodesProvider
argument_list|(
operator|new
name|DiscoveryNodesProvider
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|DiscoveryNodes
name|nodes
parameter_list|()
block|{
return|return
name|DiscoveryNodes
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|nodeA
argument_list|)
operator|.
name|localNodeId
argument_list|(
literal|"A"
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|NodeService
name|nodeService
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|zenPingA
operator|.
name|start
argument_list|()
expr_stmt|;
name|MulticastSocket
name|multicastSocket
init|=
literal|null
decl_stmt|;
try|try
block|{
name|Loggers
operator|.
name|getLogger
argument_list|(
name|MulticastZenPing
operator|.
name|class
argument_list|)
operator|.
name|setLevel
argument_list|(
literal|"TRACE"
argument_list|)
expr_stmt|;
name|multicastSocket
operator|=
operator|new
name|MulticastSocket
argument_list|(
literal|54328
argument_list|)
expr_stmt|;
name|multicastSocket
operator|.
name|setReceiveBufferSize
argument_list|(
literal|2048
argument_list|)
expr_stmt|;
name|multicastSocket
operator|.
name|setSendBufferSize
argument_list|(
literal|2048
argument_list|)
expr_stmt|;
name|multicastSocket
operator|.
name|setSoTimeout
argument_list|(
literal|60000
argument_list|)
expr_stmt|;
name|DatagramPacket
name|datagramPacket
init|=
operator|new
name|DatagramPacket
argument_list|(
operator|new
name|byte
index|[
literal|2048
index|]
argument_list|,
literal|2048
argument_list|,
name|InetAddress
operator|.
name|getByName
argument_list|(
literal|"224.2.2.4"
argument_list|)
argument_list|,
literal|54328
argument_list|)
decl_stmt|;
name|XContentBuilder
name|builder
init|=
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"request"
argument_list|)
operator|.
name|field
argument_list|(
literal|"cluster_name"
argument_list|,
literal|"test"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
decl_stmt|;
name|datagramPacket
operator|.
name|setData
argument_list|(
name|builder
operator|.
name|bytes
argument_list|()
operator|.
name|toBytes
argument_list|()
argument_list|)
expr_stmt|;
name|multicastSocket
operator|.
name|send
argument_list|(
name|datagramPacket
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|Loggers
operator|.
name|getLogger
argument_list|(
name|MulticastZenPing
operator|.
name|class
argument_list|)
operator|.
name|setLevel
argument_list|(
literal|"INFO"
argument_list|)
expr_stmt|;
if|if
condition|(
name|multicastSocket
operator|!=
literal|null
condition|)
name|multicastSocket
operator|.
name|close
argument_list|()
expr_stmt|;
name|zenPingA
operator|.
name|close
argument_list|()
expr_stmt|;
name|threadPool
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

