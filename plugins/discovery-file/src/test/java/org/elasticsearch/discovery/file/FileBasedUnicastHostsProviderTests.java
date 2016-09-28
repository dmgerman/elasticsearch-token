begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.discovery.file
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|discovery
operator|.
name|file
package|;
end_package

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
name|env
operator|.
name|Environment
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
name|io
operator|.
name|BufferedWriter
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
name|nio
operator|.
name|file
operator|.
name|Files
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|file
operator|.
name|Path
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
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
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|discovery
operator|.
name|file
operator|.
name|FileBasedUnicastHostsProvider
operator|.
name|UNICAST_HOSTS_FILE
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|discovery
operator|.
name|file
operator|.
name|FileBasedUnicastHostsProvider
operator|.
name|UNICAST_HOST_PREFIX
import|;
end_import

begin_comment
comment|/**  * Tests for {@link FileBasedUnicastHostsProvider}.  */
end_comment

begin_class
DECL|class|FileBasedUnicastHostsProviderTests
specifier|public
class|class
name|FileBasedUnicastHostsProviderTests
extends|extends
name|ESTestCase
block|{
DECL|field|threadPool
specifier|private
specifier|static
name|ThreadPool
name|threadPool
decl_stmt|;
DECL|field|transportService
specifier|private
name|MockTransportService
name|transportService
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
name|FileBasedUnicastHostsProviderTests
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
name|terminate
argument_list|(
name|threadPool
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Before
DECL|method|createTransportSvc
specifier|public
name|void
name|createTransportSvc
parameter_list|()
block|{
name|MockTcpTransport
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
operator|new
name|NamedWriteableRegistry
argument_list|(
name|Collections
operator|.
name|emptyList
argument_list|()
argument_list|)
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
argument_list|)
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
argument_list|)
expr_stmt|;
block|}
DECL|method|testBuildDynamicNodes
specifier|public
name|void
name|testBuildDynamicNodes
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|hostEntries
init|=
name|Arrays
operator|.
name|asList
argument_list|(
literal|"#comment, should be ignored"
argument_list|,
literal|"192.168.0.1"
argument_list|,
literal|"192.168.0.2:9305"
argument_list|,
literal|"255.255.23.15"
argument_list|)
decl_stmt|;
specifier|final
name|List
argument_list|<
name|DiscoveryNode
argument_list|>
name|nodes
init|=
name|setupAndRunHostProvider
argument_list|(
name|hostEntries
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|hostEntries
operator|.
name|size
argument_list|()
operator|-
literal|1
argument_list|,
name|nodes
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// minus 1 because we are ignoring the first line that's a comment
name|assertEquals
argument_list|(
literal|"192.168.0.1"
argument_list|,
name|nodes
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getAddress
argument_list|()
operator|.
name|getHost
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|9300
argument_list|,
name|nodes
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getAddress
argument_list|()
operator|.
name|getPort
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|UNICAST_HOST_PREFIX
operator|+
literal|"1#"
argument_list|,
name|nodes
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"192.168.0.2"
argument_list|,
name|nodes
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|getAddress
argument_list|()
operator|.
name|getHost
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|9305
argument_list|,
name|nodes
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|getAddress
argument_list|()
operator|.
name|getPort
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|UNICAST_HOST_PREFIX
operator|+
literal|"2#"
argument_list|,
name|nodes
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"255.255.23.15"
argument_list|,
name|nodes
operator|.
name|get
argument_list|(
literal|2
argument_list|)
operator|.
name|getAddress
argument_list|()
operator|.
name|getHost
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|9300
argument_list|,
name|nodes
operator|.
name|get
argument_list|(
literal|2
argument_list|)
operator|.
name|getAddress
argument_list|()
operator|.
name|getPort
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|UNICAST_HOST_PREFIX
operator|+
literal|"3#"
argument_list|,
name|nodes
operator|.
name|get
argument_list|(
literal|2
argument_list|)
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testEmptyUnicastHostsFile
specifier|public
name|void
name|testEmptyUnicastHostsFile
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|hostEntries
init|=
name|Collections
operator|.
name|emptyList
argument_list|()
decl_stmt|;
specifier|final
name|List
argument_list|<
name|DiscoveryNode
argument_list|>
name|nodes
init|=
name|setupAndRunHostProvider
argument_list|(
name|hostEntries
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|nodes
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testUnicastHostsDoesNotExist
specifier|public
name|void
name|testUnicastHostsDoesNotExist
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|Settings
name|settings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|Environment
operator|.
name|PATH_HOME_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
name|createTempDir
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
specifier|final
name|FileBasedUnicastHostsProvider
name|provider
init|=
operator|new
name|FileBasedUnicastHostsProvider
argument_list|(
name|settings
argument_list|,
name|transportService
argument_list|)
decl_stmt|;
specifier|final
name|List
argument_list|<
name|DiscoveryNode
argument_list|>
name|nodes
init|=
name|provider
operator|.
name|buildDynamicNodes
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|nodes
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testInvalidHostEntries
specifier|public
name|void
name|testInvalidHostEntries
parameter_list|()
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|String
argument_list|>
name|hostEntries
init|=
name|Arrays
operator|.
name|asList
argument_list|(
literal|"192.168.0.1:9300:9300"
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|DiscoveryNode
argument_list|>
name|nodes
init|=
name|setupAndRunHostProvider
argument_list|(
name|hostEntries
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|nodes
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testSomeInvalidHostEntries
specifier|public
name|void
name|testSomeInvalidHostEntries
parameter_list|()
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|String
argument_list|>
name|hostEntries
init|=
name|Arrays
operator|.
name|asList
argument_list|(
literal|"192.168.0.1:9300:9300"
argument_list|,
literal|"192.168.0.1:9301"
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|DiscoveryNode
argument_list|>
name|nodes
init|=
name|setupAndRunHostProvider
argument_list|(
name|hostEntries
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|nodes
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// only one of the two is valid and will be used
name|assertEquals
argument_list|(
literal|"192.168.0.1"
argument_list|,
name|nodes
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getAddress
argument_list|()
operator|.
name|getHost
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|9301
argument_list|,
name|nodes
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getAddress
argument_list|()
operator|.
name|getPort
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// sets up the config dir, writes to the unicast hosts file in the config dir,
comment|// and then runs the file-based unicast host provider to get the list of discovery nodes
DECL|method|setupAndRunHostProvider
specifier|private
name|List
argument_list|<
name|DiscoveryNode
argument_list|>
name|setupAndRunHostProvider
parameter_list|(
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|hostEntries
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|Path
name|homeDir
init|=
name|createTempDir
argument_list|()
decl_stmt|;
specifier|final
name|Settings
name|settings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|Environment
operator|.
name|PATH_HOME_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
name|homeDir
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
specifier|final
name|Path
name|configDir
init|=
name|homeDir
operator|.
name|resolve
argument_list|(
literal|"config"
argument_list|)
operator|.
name|resolve
argument_list|(
literal|"discovery-file"
argument_list|)
decl_stmt|;
name|Files
operator|.
name|createDirectories
argument_list|(
name|configDir
argument_list|)
expr_stmt|;
specifier|final
name|Path
name|unicastHostsPath
init|=
name|configDir
operator|.
name|resolve
argument_list|(
name|UNICAST_HOSTS_FILE
argument_list|)
decl_stmt|;
try|try
init|(
name|BufferedWriter
name|writer
init|=
name|Files
operator|.
name|newBufferedWriter
argument_list|(
name|unicastHostsPath
argument_list|)
init|)
block|{
name|writer
operator|.
name|write
argument_list|(
name|String
operator|.
name|join
argument_list|(
literal|"\n"
argument_list|,
name|hostEntries
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|FileBasedUnicastHostsProvider
argument_list|(
name|settings
argument_list|,
name|transportService
argument_list|)
operator|.
name|buildDynamicNodes
argument_list|()
return|;
block|}
block|}
end_class

end_unit

