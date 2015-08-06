begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.transport.netty
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|transport
operator|.
name|netty
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
name|io
operator|.
name|stream
operator|.
name|NamedWriteableRegistry
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
name|NetworkService
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
name|util
operator|.
name|BigArrays
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
name|transport
operator|.
name|MockTransportService
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
name|AbstractSimpleTransportTests
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
name|ConnectTransportException
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

begin_class
DECL|class|SimpleNettyTransportTests
specifier|public
class|class
name|SimpleNettyTransportTests
extends|extends
name|AbstractSimpleTransportTests
block|{
annotation|@
name|Override
DECL|method|build
specifier|protected
name|MockTransportService
name|build
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|Version
name|version
parameter_list|,
name|NamedWriteableRegistry
name|namedWriteableRegistry
parameter_list|)
block|{
name|int
name|startPort
init|=
literal|11000
operator|+
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|255
argument_list|)
decl_stmt|;
name|int
name|endPort
init|=
name|startPort
operator|+
literal|10
decl_stmt|;
name|settings
operator|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|settings
argument_list|)
operator|.
name|put
argument_list|(
literal|"transport.tcp.port"
argument_list|,
name|startPort
operator|+
literal|"-"
operator|+
name|endPort
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|MockTransportService
name|transportService
init|=
operator|new
name|MockTransportService
argument_list|(
name|settings
argument_list|,
operator|new
name|NettyTransport
argument_list|(
name|settings
argument_list|,
name|threadPool
argument_list|,
operator|new
name|NetworkService
argument_list|(
name|settings
argument_list|)
argument_list|,
name|BigArrays
operator|.
name|NON_RECYCLING_INSTANCE
argument_list|,
name|version
argument_list|,
name|namedWriteableRegistry
argument_list|)
argument_list|,
name|threadPool
argument_list|)
decl_stmt|;
name|transportService
operator|.
name|start
argument_list|()
expr_stmt|;
return|return
name|transportService
return|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|ConnectTransportException
operator|.
name|class
argument_list|)
DECL|method|testConnectException
specifier|public
name|void
name|testConnectException
parameter_list|()
block|{
name|serviceA
operator|.
name|connectToNode
argument_list|(
operator|new
name|DiscoveryNode
argument_list|(
literal|"C"
argument_list|,
operator|new
name|InetSocketTransportAddress
argument_list|(
literal|"localhost"
argument_list|,
literal|9876
argument_list|)
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

