begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.discovery.ec2
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|discovery
operator|.
name|ec2
package|;
end_package

begin_import
import|import
name|com
operator|.
name|amazonaws
operator|.
name|services
operator|.
name|ec2
operator|.
name|model
operator|.
name|Tag
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
name|cloud
operator|.
name|aws
operator|.
name|AwsEc2Service
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cloud
operator|.
name|aws
operator|.
name|AwsEc2Service
operator|.
name|DISCOVERY_EC2
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
name|collect
operator|.
name|CopyOnWriteHashMap
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
name|TransportAddress
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
name|indices
operator|.
name|breaker
operator|.
name|NoneCircuitBreakerService
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
name|ESTestCase
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
name|threadpool
operator|.
name|TestThreadPool
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
name|MockTcpTransport
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
name|Transport
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
name|junit
operator|.
name|AfterClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|BeforeClass
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
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
name|concurrent
operator|.
name|ConcurrentHashMap
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
name|containsString
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
name|hasSize
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
name|is
import|;
end_import

begin_class
DECL|class|Ec2DiscoveryTests
specifier|public
class|class
name|Ec2DiscoveryTests
extends|extends
name|ESTestCase
block|{
DECL|field|threadPool
specifier|protected
specifier|static
name|ThreadPool
name|threadPool
decl_stmt|;
DECL|field|transportService
specifier|protected
name|MockTransportService
name|transportService
decl_stmt|;
DECL|field|poorMansDNS
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|TransportAddress
argument_list|>
name|poorMansDNS
init|=
operator|new
name|ConcurrentHashMap
argument_list|<>
argument_list|()
decl_stmt|;
annotation|@
name|BeforeClass
DECL|method|createThreadPool
specifier|public
specifier|static
name|void
name|createThreadPool
parameter_list|()
block|{
name|threadPool
operator|=
operator|new
name|TestThreadPool
argument_list|(
name|Ec2DiscoveryTests
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
DECL|method|stopThreadPool
specifier|public
specifier|static
name|void
name|stopThreadPool
parameter_list|()
throws|throws
name|InterruptedException
block|{
if|if
condition|(
name|threadPool
operator|!=
literal|null
condition|)
block|{
name|terminate
argument_list|(
name|threadPool
argument_list|)
expr_stmt|;
name|threadPool
operator|=
literal|null
expr_stmt|;
block|}
block|}
annotation|@
name|Before
DECL|method|createTransportService
specifier|public
name|void
name|createTransportService
parameter_list|()
block|{
name|NamedWriteableRegistry
name|namedWriteableRegistry
init|=
operator|new
name|NamedWriteableRegistry
argument_list|(
name|Collections
operator|.
name|emptyList
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|Transport
name|transport
init|=
operator|new
name|MockTcpTransport
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
name|threadPool
argument_list|,
name|BigArrays
operator|.
name|NON_RECYCLING_INSTANCE
argument_list|,
operator|new
name|NoneCircuitBreakerService
argument_list|()
argument_list|,
name|namedWriteableRegistry
argument_list|,
operator|new
name|NetworkService
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
name|Collections
operator|.
name|emptyList
argument_list|()
argument_list|)
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|TransportAddress
index|[]
name|addressesFromString
parameter_list|(
name|String
name|address
parameter_list|,
name|int
name|perAddressLimit
parameter_list|)
throws|throws
name|Exception
block|{
comment|// we just need to ensure we don't resolve DNS here
return|return
operator|new
name|TransportAddress
index|[]
block|{
name|poorMansDNS
operator|.
name|getOrDefault
argument_list|(
name|address
argument_list|,
name|buildNewFakeTransportAddress
argument_list|()
argument_list|)
block|}
return|;
block|}
block|}
decl_stmt|;
name|transportService
operator|=
operator|new
name|MockTransportService
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
name|transport
argument_list|,
name|threadPool
argument_list|,
name|TransportService
operator|.
name|NOOP_TRANSPORT_INTERCEPTOR
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
DECL|method|buildDynamicNodes
specifier|protected
name|List
argument_list|<
name|DiscoveryNode
argument_list|>
name|buildDynamicNodes
parameter_list|(
name|Settings
name|nodeSettings
parameter_list|,
name|int
name|nodes
parameter_list|)
block|{
return|return
name|buildDynamicNodes
argument_list|(
name|nodeSettings
argument_list|,
name|nodes
argument_list|,
literal|null
argument_list|)
return|;
block|}
DECL|method|buildDynamicNodes
specifier|protected
name|List
argument_list|<
name|DiscoveryNode
argument_list|>
name|buildDynamicNodes
parameter_list|(
name|Settings
name|nodeSettings
parameter_list|,
name|int
name|nodes
parameter_list|,
name|List
argument_list|<
name|List
argument_list|<
name|Tag
argument_list|>
argument_list|>
name|tagsList
parameter_list|)
block|{
name|AwsEc2Service
name|awsEc2Service
init|=
operator|new
name|AwsEc2ServiceMock
argument_list|(
name|nodeSettings
argument_list|,
name|nodes
argument_list|,
name|tagsList
argument_list|)
decl_stmt|;
name|AwsEc2UnicastHostsProvider
name|provider
init|=
operator|new
name|AwsEc2UnicastHostsProvider
argument_list|(
name|nodeSettings
argument_list|,
name|transportService
argument_list|,
name|awsEc2Service
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|DiscoveryNode
argument_list|>
name|discoveryNodes
init|=
name|provider
operator|.
name|buildDynamicNodes
argument_list|()
decl_stmt|;
name|logger
operator|.
name|debug
argument_list|(
literal|"--> nodes found: {}"
argument_list|,
name|discoveryNodes
argument_list|)
expr_stmt|;
return|return
name|discoveryNodes
return|;
block|}
DECL|method|testDefaultSettings
specifier|public
name|void
name|testDefaultSettings
parameter_list|()
throws|throws
name|InterruptedException
block|{
name|int
name|nodes
init|=
name|randomInt
argument_list|(
literal|10
argument_list|)
decl_stmt|;
name|Settings
name|nodeSettings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|build
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|DiscoveryNode
argument_list|>
name|discoveryNodes
init|=
name|buildDynamicNodes
argument_list|(
name|nodeSettings
argument_list|,
name|nodes
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|discoveryNodes
argument_list|,
name|hasSize
argument_list|(
name|nodes
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testPrivateIp
specifier|public
name|void
name|testPrivateIp
parameter_list|()
throws|throws
name|InterruptedException
block|{
name|int
name|nodes
init|=
name|randomInt
argument_list|(
literal|10
argument_list|)
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
name|nodes
condition|;
name|i
operator|++
control|)
block|{
name|poorMansDNS
operator|.
name|put
argument_list|(
name|AmazonEC2Mock
operator|.
name|PREFIX_PRIVATE_IP
operator|+
operator|(
name|i
operator|+
literal|1
operator|)
argument_list|,
name|buildNewFakeTransportAddress
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|Settings
name|nodeSettings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|DISCOVERY_EC2
operator|.
name|HOST_TYPE_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
literal|"private_ip"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|DiscoveryNode
argument_list|>
name|discoveryNodes
init|=
name|buildDynamicNodes
argument_list|(
name|nodeSettings
argument_list|,
name|nodes
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|discoveryNodes
argument_list|,
name|hasSize
argument_list|(
name|nodes
argument_list|)
argument_list|)
expr_stmt|;
comment|// We check that we are using here expected address
name|int
name|node
init|=
literal|1
decl_stmt|;
for|for
control|(
name|DiscoveryNode
name|discoveryNode
range|:
name|discoveryNodes
control|)
block|{
name|TransportAddress
name|address
init|=
name|discoveryNode
operator|.
name|getAddress
argument_list|()
decl_stmt|;
name|TransportAddress
name|expected
init|=
name|poorMansDNS
operator|.
name|get
argument_list|(
name|AmazonEC2Mock
operator|.
name|PREFIX_PRIVATE_IP
operator|+
name|node
operator|++
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|address
argument_list|,
name|expected
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testPublicIp
specifier|public
name|void
name|testPublicIp
parameter_list|()
throws|throws
name|InterruptedException
block|{
name|int
name|nodes
init|=
name|randomInt
argument_list|(
literal|10
argument_list|)
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
name|nodes
condition|;
name|i
operator|++
control|)
block|{
name|poorMansDNS
operator|.
name|put
argument_list|(
name|AmazonEC2Mock
operator|.
name|PREFIX_PUBLIC_IP
operator|+
operator|(
name|i
operator|+
literal|1
operator|)
argument_list|,
name|buildNewFakeTransportAddress
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|Settings
name|nodeSettings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|DISCOVERY_EC2
operator|.
name|HOST_TYPE_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
literal|"public_ip"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|DiscoveryNode
argument_list|>
name|discoveryNodes
init|=
name|buildDynamicNodes
argument_list|(
name|nodeSettings
argument_list|,
name|nodes
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|discoveryNodes
argument_list|,
name|hasSize
argument_list|(
name|nodes
argument_list|)
argument_list|)
expr_stmt|;
comment|// We check that we are using here expected address
name|int
name|node
init|=
literal|1
decl_stmt|;
for|for
control|(
name|DiscoveryNode
name|discoveryNode
range|:
name|discoveryNodes
control|)
block|{
name|TransportAddress
name|address
init|=
name|discoveryNode
operator|.
name|getAddress
argument_list|()
decl_stmt|;
name|TransportAddress
name|expected
init|=
name|poorMansDNS
operator|.
name|get
argument_list|(
name|AmazonEC2Mock
operator|.
name|PREFIX_PUBLIC_IP
operator|+
name|node
operator|++
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|address
argument_list|,
name|expected
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testPrivateDns
specifier|public
name|void
name|testPrivateDns
parameter_list|()
throws|throws
name|InterruptedException
block|{
name|int
name|nodes
init|=
name|randomInt
argument_list|(
literal|10
argument_list|)
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
name|nodes
condition|;
name|i
operator|++
control|)
block|{
name|String
name|instanceId
init|=
literal|"node"
operator|+
operator|(
name|i
operator|+
literal|1
operator|)
decl_stmt|;
name|poorMansDNS
operator|.
name|put
argument_list|(
name|AmazonEC2Mock
operator|.
name|PREFIX_PRIVATE_DNS
operator|+
name|instanceId
operator|+
name|AmazonEC2Mock
operator|.
name|SUFFIX_PRIVATE_DNS
argument_list|,
name|buildNewFakeTransportAddress
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|Settings
name|nodeSettings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|DISCOVERY_EC2
operator|.
name|HOST_TYPE_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
literal|"private_dns"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|DiscoveryNode
argument_list|>
name|discoveryNodes
init|=
name|buildDynamicNodes
argument_list|(
name|nodeSettings
argument_list|,
name|nodes
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|discoveryNodes
argument_list|,
name|hasSize
argument_list|(
name|nodes
argument_list|)
argument_list|)
expr_stmt|;
comment|// We check that we are using here expected address
name|int
name|node
init|=
literal|1
decl_stmt|;
for|for
control|(
name|DiscoveryNode
name|discoveryNode
range|:
name|discoveryNodes
control|)
block|{
name|String
name|instanceId
init|=
literal|"node"
operator|+
name|node
operator|++
decl_stmt|;
name|TransportAddress
name|address
init|=
name|discoveryNode
operator|.
name|getAddress
argument_list|()
decl_stmt|;
name|TransportAddress
name|expected
init|=
name|poorMansDNS
operator|.
name|get
argument_list|(
name|AmazonEC2Mock
operator|.
name|PREFIX_PRIVATE_DNS
operator|+
name|instanceId
operator|+
name|AmazonEC2Mock
operator|.
name|SUFFIX_PRIVATE_DNS
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|address
argument_list|,
name|expected
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testPublicDns
specifier|public
name|void
name|testPublicDns
parameter_list|()
throws|throws
name|InterruptedException
block|{
name|int
name|nodes
init|=
name|randomInt
argument_list|(
literal|10
argument_list|)
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
name|nodes
condition|;
name|i
operator|++
control|)
block|{
name|String
name|instanceId
init|=
literal|"node"
operator|+
operator|(
name|i
operator|+
literal|1
operator|)
decl_stmt|;
name|poorMansDNS
operator|.
name|put
argument_list|(
name|AmazonEC2Mock
operator|.
name|PREFIX_PUBLIC_DNS
operator|+
name|instanceId
operator|+
name|AmazonEC2Mock
operator|.
name|SUFFIX_PUBLIC_DNS
argument_list|,
name|buildNewFakeTransportAddress
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|Settings
name|nodeSettings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|DISCOVERY_EC2
operator|.
name|HOST_TYPE_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
literal|"public_dns"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|DiscoveryNode
argument_list|>
name|discoveryNodes
init|=
name|buildDynamicNodes
argument_list|(
name|nodeSettings
argument_list|,
name|nodes
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|discoveryNodes
argument_list|,
name|hasSize
argument_list|(
name|nodes
argument_list|)
argument_list|)
expr_stmt|;
comment|// We check that we are using here expected address
name|int
name|node
init|=
literal|1
decl_stmt|;
for|for
control|(
name|DiscoveryNode
name|discoveryNode
range|:
name|discoveryNodes
control|)
block|{
name|String
name|instanceId
init|=
literal|"node"
operator|+
name|node
operator|++
decl_stmt|;
name|TransportAddress
name|address
init|=
name|discoveryNode
operator|.
name|getAddress
argument_list|()
decl_stmt|;
name|TransportAddress
name|expected
init|=
name|poorMansDNS
operator|.
name|get
argument_list|(
name|AmazonEC2Mock
operator|.
name|PREFIX_PUBLIC_DNS
operator|+
name|instanceId
operator|+
name|AmazonEC2Mock
operator|.
name|SUFFIX_PUBLIC_DNS
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|address
argument_list|,
name|expected
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testInvalidHostType
specifier|public
name|void
name|testInvalidHostType
parameter_list|()
throws|throws
name|InterruptedException
block|{
name|Settings
name|nodeSettings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|DISCOVERY_EC2
operator|.
name|HOST_TYPE_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
literal|"does_not_exist"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
try|try
block|{
name|buildDynamicNodes
argument_list|(
name|nodeSettings
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Expected IllegalArgumentException"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"No enum constant"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testFilterByTags
specifier|public
name|void
name|testFilterByTags
parameter_list|()
throws|throws
name|InterruptedException
block|{
name|int
name|nodes
init|=
name|randomIntBetween
argument_list|(
literal|5
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|Settings
name|nodeSettings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|DISCOVERY_EC2
operator|.
name|TAG_SETTING
operator|.
name|getKey
argument_list|()
operator|+
literal|"stage"
argument_list|,
literal|"prod"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|int
name|prodInstances
init|=
literal|0
decl_stmt|;
name|List
argument_list|<
name|List
argument_list|<
name|Tag
argument_list|>
argument_list|>
name|tagsList
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|node
init|=
literal|0
init|;
name|node
operator|<
name|nodes
condition|;
name|node
operator|++
control|)
block|{
name|List
argument_list|<
name|Tag
argument_list|>
name|tags
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|tags
operator|.
name|add
argument_list|(
operator|new
name|Tag
argument_list|(
literal|"stage"
argument_list|,
literal|"prod"
argument_list|)
argument_list|)
expr_stmt|;
name|prodInstances
operator|++
expr_stmt|;
block|}
else|else
block|{
name|tags
operator|.
name|add
argument_list|(
operator|new
name|Tag
argument_list|(
literal|"stage"
argument_list|,
literal|"dev"
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|tagsList
operator|.
name|add
argument_list|(
name|tags
argument_list|)
expr_stmt|;
block|}
name|logger
operator|.
name|info
argument_list|(
literal|"started [{}] instances with [{}] stage=prod tag"
argument_list|,
name|nodes
argument_list|,
name|prodInstances
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|DiscoveryNode
argument_list|>
name|discoveryNodes
init|=
name|buildDynamicNodes
argument_list|(
name|nodeSettings
argument_list|,
name|nodes
argument_list|,
name|tagsList
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|discoveryNodes
argument_list|,
name|hasSize
argument_list|(
name|prodInstances
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testFilterByMultipleTags
specifier|public
name|void
name|testFilterByMultipleTags
parameter_list|()
throws|throws
name|InterruptedException
block|{
name|int
name|nodes
init|=
name|randomIntBetween
argument_list|(
literal|5
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|Settings
name|nodeSettings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|putArray
argument_list|(
name|DISCOVERY_EC2
operator|.
name|TAG_SETTING
operator|.
name|getKey
argument_list|()
operator|+
literal|"stage"
argument_list|,
literal|"prod"
argument_list|,
literal|"preprod"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|int
name|prodInstances
init|=
literal|0
decl_stmt|;
name|List
argument_list|<
name|List
argument_list|<
name|Tag
argument_list|>
argument_list|>
name|tagsList
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|node
init|=
literal|0
init|;
name|node
operator|<
name|nodes
condition|;
name|node
operator|++
control|)
block|{
name|List
argument_list|<
name|Tag
argument_list|>
name|tags
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|tags
operator|.
name|add
argument_list|(
operator|new
name|Tag
argument_list|(
literal|"stage"
argument_list|,
literal|"prod"
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|tags
operator|.
name|add
argument_list|(
operator|new
name|Tag
argument_list|(
literal|"stage"
argument_list|,
literal|"preprod"
argument_list|)
argument_list|)
expr_stmt|;
name|prodInstances
operator|++
expr_stmt|;
block|}
block|}
else|else
block|{
name|tags
operator|.
name|add
argument_list|(
operator|new
name|Tag
argument_list|(
literal|"stage"
argument_list|,
literal|"dev"
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|tags
operator|.
name|add
argument_list|(
operator|new
name|Tag
argument_list|(
literal|"stage"
argument_list|,
literal|"preprod"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|tagsList
operator|.
name|add
argument_list|(
name|tags
argument_list|)
expr_stmt|;
block|}
name|logger
operator|.
name|info
argument_list|(
literal|"started [{}] instances with [{}] stage=prod tag"
argument_list|,
name|nodes
argument_list|,
name|prodInstances
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|DiscoveryNode
argument_list|>
name|discoveryNodes
init|=
name|buildDynamicNodes
argument_list|(
name|nodeSettings
argument_list|,
name|nodes
argument_list|,
name|tagsList
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|discoveryNodes
argument_list|,
name|hasSize
argument_list|(
name|prodInstances
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|class|DummyEc2HostProvider
specifier|abstract
class|class
name|DummyEc2HostProvider
extends|extends
name|AwsEc2UnicastHostsProvider
block|{
DECL|field|fetchCount
specifier|public
name|int
name|fetchCount
init|=
literal|0
decl_stmt|;
DECL|method|DummyEc2HostProvider
specifier|public
name|DummyEc2HostProvider
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|TransportService
name|transportService
parameter_list|,
name|AwsEc2Service
name|service
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|transportService
argument_list|,
name|service
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testGetNodeListEmptyCache
specifier|public
name|void
name|testGetNodeListEmptyCache
parameter_list|()
throws|throws
name|Exception
block|{
name|AwsEc2Service
name|awsEc2Service
init|=
operator|new
name|AwsEc2ServiceMock
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
literal|1
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|DummyEc2HostProvider
name|provider
init|=
operator|new
name|DummyEc2HostProvider
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
name|transportService
argument_list|,
name|awsEc2Service
argument_list|)
block|{
annotation|@
name|Override
specifier|protected
name|List
argument_list|<
name|DiscoveryNode
argument_list|>
name|fetchDynamicNodes
parameter_list|()
block|{
name|fetchCount
operator|++
expr_stmt|;
return|return
operator|new
name|ArrayList
argument_list|<>
argument_list|()
return|;
block|}
block|}
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
literal|3
condition|;
name|i
operator|++
control|)
block|{
name|provider
operator|.
name|buildDynamicNodes
argument_list|()
expr_stmt|;
block|}
name|assertThat
argument_list|(
name|provider
operator|.
name|fetchCount
argument_list|,
name|is
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testGetNodeListCached
specifier|public
name|void
name|testGetNodeListCached
parameter_list|()
throws|throws
name|Exception
block|{
name|Settings
operator|.
name|Builder
name|builder
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|DISCOVERY_EC2
operator|.
name|NODE_CACHE_TIME_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
literal|"500ms"
argument_list|)
decl_stmt|;
name|AwsEc2Service
name|awsEc2Service
init|=
operator|new
name|AwsEc2ServiceMock
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
literal|1
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|DummyEc2HostProvider
name|provider
init|=
operator|new
name|DummyEc2HostProvider
argument_list|(
name|builder
operator|.
name|build
argument_list|()
argument_list|,
name|transportService
argument_list|,
name|awsEc2Service
argument_list|)
block|{
annotation|@
name|Override
specifier|protected
name|List
argument_list|<
name|DiscoveryNode
argument_list|>
name|fetchDynamicNodes
parameter_list|()
block|{
name|fetchCount
operator|++
expr_stmt|;
return|return
name|Ec2DiscoveryTests
operator|.
name|this
operator|.
name|buildDynamicNodes
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
literal|1
argument_list|)
return|;
block|}
block|}
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
literal|3
condition|;
name|i
operator|++
control|)
block|{
name|provider
operator|.
name|buildDynamicNodes
argument_list|()
expr_stmt|;
block|}
name|assertThat
argument_list|(
name|provider
operator|.
name|fetchCount
argument_list|,
name|is
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|1_000L
argument_list|)
expr_stmt|;
comment|// wait for cache to expire
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|3
condition|;
name|i
operator|++
control|)
block|{
name|provider
operator|.
name|buildDynamicNodes
argument_list|()
expr_stmt|;
block|}
name|assertThat
argument_list|(
name|provider
operator|.
name|fetchCount
argument_list|,
name|is
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

