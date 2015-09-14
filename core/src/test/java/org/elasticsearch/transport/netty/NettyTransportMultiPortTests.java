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
name|com
operator|.
name|carrotsearch
operator|.
name|hppc
operator|.
name|IntHashSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|charset
operator|.
name|StandardCharsets
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
name|cache
operator|.
name|recycler
operator|.
name|PageCacheRecycler
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
name|Lifecycle
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
name|common
operator|.
name|util
operator|.
name|MockBigArrays
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
name|junit
operator|.
name|rule
operator|.
name|RepeatOnExceptionRule
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
name|BindTransportException
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
name|Before
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Rule
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
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|OutputStream
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
name|InetSocketAddress
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|ServerSocket
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|Socket
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|settings
operator|.
name|Settings
operator|.
name|settingsBuilder
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
DECL|class|NettyTransportMultiPortTests
specifier|public
class|class
name|NettyTransportMultiPortTests
extends|extends
name|ESTestCase
block|{
DECL|field|MAX_RETRIES
specifier|private
specifier|static
specifier|final
name|int
name|MAX_RETRIES
init|=
literal|10
decl_stmt|;
DECL|field|host
specifier|private
name|String
name|host
decl_stmt|;
annotation|@
name|Rule
DECL|field|repeatOnBindExceptionRule
specifier|public
name|RepeatOnExceptionRule
name|repeatOnBindExceptionRule
init|=
operator|new
name|RepeatOnExceptionRule
argument_list|(
name|logger
argument_list|,
name|MAX_RETRIES
argument_list|,
name|BindTransportException
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Before
DECL|method|setup
specifier|public
name|void
name|setup
parameter_list|()
block|{
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|host
operator|=
literal|"localhost"
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|NetworkUtils
operator|.
name|SUPPORTS_V6
operator|&&
name|randomBoolean
argument_list|()
condition|)
block|{
name|host
operator|=
literal|"::1"
expr_stmt|;
block|}
else|else
block|{
name|host
operator|=
literal|"127.0.0.1"
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Test
DECL|method|testThatNettyCanBindToMultiplePorts
specifier|public
name|void
name|testThatNettyCanBindToMultiplePorts
parameter_list|()
throws|throws
name|Exception
block|{
name|int
index|[]
name|ports
init|=
name|getRandomPorts
argument_list|(
literal|3
argument_list|)
decl_stmt|;
name|Settings
name|settings
init|=
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"network.host"
argument_list|,
name|host
argument_list|)
operator|.
name|put
argument_list|(
literal|"transport.tcp.port"
argument_list|,
name|ports
index|[
literal|0
index|]
argument_list|)
operator|.
name|put
argument_list|(
literal|"transport.profiles.default.port"
argument_list|,
name|ports
index|[
literal|1
index|]
argument_list|)
operator|.
name|put
argument_list|(
literal|"transport.profiles.client1.port"
argument_list|,
name|ports
index|[
literal|2
index|]
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|ThreadPool
name|threadPool
init|=
operator|new
name|ThreadPool
argument_list|(
literal|"tst"
argument_list|)
decl_stmt|;
try|try
init|(
name|NettyTransport
name|ignored
init|=
name|startNettyTransport
argument_list|(
name|settings
argument_list|,
name|threadPool
argument_list|)
init|)
block|{
name|assertConnectionRefused
argument_list|(
name|ports
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|assertPortIsBound
argument_list|(
name|ports
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
name|assertPortIsBound
argument_list|(
name|ports
index|[
literal|2
index|]
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|terminate
argument_list|(
name|threadPool
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
DECL|method|testThatDefaultProfileInheritsFromStandardSettings
specifier|public
name|void
name|testThatDefaultProfileInheritsFromStandardSettings
parameter_list|()
throws|throws
name|Exception
block|{
name|int
index|[]
name|ports
init|=
name|getRandomPorts
argument_list|(
literal|2
argument_list|)
decl_stmt|;
name|Settings
name|settings
init|=
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"network.host"
argument_list|,
name|host
argument_list|)
operator|.
name|put
argument_list|(
literal|"transport.tcp.port"
argument_list|,
name|ports
index|[
literal|0
index|]
argument_list|)
operator|.
name|put
argument_list|(
literal|"transport.profiles.client1.port"
argument_list|,
name|ports
index|[
literal|1
index|]
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|ThreadPool
name|threadPool
init|=
operator|new
name|ThreadPool
argument_list|(
literal|"tst"
argument_list|)
decl_stmt|;
try|try
init|(
name|NettyTransport
name|ignored
init|=
name|startNettyTransport
argument_list|(
name|settings
argument_list|,
name|threadPool
argument_list|)
init|)
block|{
name|assertPortIsBound
argument_list|(
name|ports
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|assertPortIsBound
argument_list|(
name|ports
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|terminate
argument_list|(
name|threadPool
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
DECL|method|testThatProfileWithoutPortSettingsFails
specifier|public
name|void
name|testThatProfileWithoutPortSettingsFails
parameter_list|()
throws|throws
name|Exception
block|{
name|int
index|[]
name|ports
init|=
name|getRandomPorts
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|Settings
name|settings
init|=
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"network.host"
argument_list|,
name|host
argument_list|)
operator|.
name|put
argument_list|(
literal|"transport.tcp.port"
argument_list|,
name|ports
index|[
literal|0
index|]
argument_list|)
operator|.
name|put
argument_list|(
literal|"transport.profiles.client1.whatever"
argument_list|,
literal|"foo"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|ThreadPool
name|threadPool
init|=
operator|new
name|ThreadPool
argument_list|(
literal|"tst"
argument_list|)
decl_stmt|;
try|try
init|(
name|NettyTransport
name|ignored
init|=
name|startNettyTransport
argument_list|(
name|settings
argument_list|,
name|threadPool
argument_list|)
init|)
block|{
name|assertPortIsBound
argument_list|(
name|ports
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|terminate
argument_list|(
name|threadPool
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
DECL|method|testThatDefaultProfilePortOverridesGeneralConfiguration
specifier|public
name|void
name|testThatDefaultProfilePortOverridesGeneralConfiguration
parameter_list|()
throws|throws
name|Exception
block|{
name|int
index|[]
name|ports
init|=
name|getRandomPorts
argument_list|(
literal|3
argument_list|)
decl_stmt|;
name|Settings
name|settings
init|=
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"network.host"
argument_list|,
name|host
argument_list|)
operator|.
name|put
argument_list|(
literal|"transport.tcp.port"
argument_list|,
name|ports
index|[
literal|0
index|]
argument_list|)
operator|.
name|put
argument_list|(
literal|"transport.netty.port"
argument_list|,
name|ports
index|[
literal|1
index|]
argument_list|)
operator|.
name|put
argument_list|(
literal|"transport.profiles.default.port"
argument_list|,
name|ports
index|[
literal|2
index|]
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|ThreadPool
name|threadPool
init|=
operator|new
name|ThreadPool
argument_list|(
literal|"tst"
argument_list|)
decl_stmt|;
try|try
init|(
name|NettyTransport
name|ignored
init|=
name|startNettyTransport
argument_list|(
name|settings
argument_list|,
name|threadPool
argument_list|)
init|)
block|{
name|assertConnectionRefused
argument_list|(
name|ports
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|assertConnectionRefused
argument_list|(
name|ports
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
name|assertPortIsBound
argument_list|(
name|ports
index|[
literal|2
index|]
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|terminate
argument_list|(
name|threadPool
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
DECL|method|testThatProfileWithoutValidNameIsIgnored
specifier|public
name|void
name|testThatProfileWithoutValidNameIsIgnored
parameter_list|()
throws|throws
name|Exception
block|{
name|int
index|[]
name|ports
init|=
name|getRandomPorts
argument_list|(
literal|3
argument_list|)
decl_stmt|;
name|Settings
name|settings
init|=
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"network.host"
argument_list|,
name|host
argument_list|)
operator|.
name|put
argument_list|(
literal|"transport.tcp.port"
argument_list|,
name|ports
index|[
literal|0
index|]
argument_list|)
comment|// mimics someone trying to define a profile for .local which is the profile for a node request to itself
operator|.
name|put
argument_list|(
literal|"transport.profiles."
operator|+
name|TransportService
operator|.
name|DIRECT_RESPONSE_PROFILE
operator|+
literal|".port"
argument_list|,
name|ports
index|[
literal|1
index|]
argument_list|)
operator|.
name|put
argument_list|(
literal|"transport.profiles..port"
argument_list|,
name|ports
index|[
literal|2
index|]
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|ThreadPool
name|threadPool
init|=
operator|new
name|ThreadPool
argument_list|(
literal|"tst"
argument_list|)
decl_stmt|;
try|try
init|(
name|NettyTransport
name|ignored
init|=
name|startNettyTransport
argument_list|(
name|settings
argument_list|,
name|threadPool
argument_list|)
init|)
block|{
name|assertPortIsBound
argument_list|(
name|ports
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|assertConnectionRefused
argument_list|(
name|ports
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
name|assertConnectionRefused
argument_list|(
name|ports
index|[
literal|2
index|]
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|terminate
argument_list|(
name|threadPool
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|getRandomPorts
specifier|private
name|int
index|[]
name|getRandomPorts
parameter_list|(
name|int
name|numberOfPorts
parameter_list|)
block|{
name|IntHashSet
name|ports
init|=
operator|new
name|IntHashSet
argument_list|()
decl_stmt|;
name|int
name|nextPort
init|=
name|randomIntBetween
argument_list|(
literal|49152
argument_list|,
literal|65535
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
name|numberOfPorts
condition|;
name|i
operator|++
control|)
block|{
name|boolean
name|foundPortInRange
init|=
literal|false
decl_stmt|;
while|while
condition|(
operator|!
name|foundPortInRange
condition|)
block|{
if|if
condition|(
operator|!
name|ports
operator|.
name|contains
argument_list|(
name|nextPort
argument_list|)
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"looking to see if port [{}]is available"
argument_list|,
name|nextPort
argument_list|)
expr_stmt|;
try|try
init|(
name|ServerSocket
name|serverSocket
init|=
operator|new
name|ServerSocket
argument_list|()
init|)
block|{
comment|// Set SO_REUSEADDR as we may bind here and not be able
comment|// to reuse the address immediately without it.
name|serverSocket
operator|.
name|setReuseAddress
argument_list|(
name|NetworkUtils
operator|.
name|defaultReuseAddress
argument_list|()
argument_list|)
expr_stmt|;
name|serverSocket
operator|.
name|bind
argument_list|(
operator|new
name|InetSocketAddress
argument_list|(
name|InetAddress
operator|.
name|getLoopbackAddress
argument_list|()
argument_list|,
name|nextPort
argument_list|)
argument_list|)
expr_stmt|;
comment|// bind was a success
name|logger
operator|.
name|debug
argument_list|(
literal|"port [{}] available."
argument_list|,
name|nextPort
argument_list|)
expr_stmt|;
name|foundPortInRange
operator|=
literal|true
expr_stmt|;
name|ports
operator|.
name|add
argument_list|(
name|nextPort
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// Do nothing
name|logger
operator|.
name|debug
argument_list|(
literal|"port [{}] not available."
argument_list|,
name|e
argument_list|,
name|nextPort
argument_list|)
expr_stmt|;
block|}
block|}
name|nextPort
operator|=
name|randomIntBetween
argument_list|(
literal|49152
argument_list|,
literal|65535
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|ports
operator|.
name|toArray
argument_list|()
return|;
block|}
DECL|method|startNettyTransport
specifier|private
name|NettyTransport
name|startNettyTransport
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|)
block|{
name|BigArrays
name|bigArrays
init|=
operator|new
name|MockBigArrays
argument_list|(
operator|new
name|PageCacheRecycler
argument_list|(
name|settings
argument_list|,
name|threadPool
argument_list|)
argument_list|,
operator|new
name|NoneCircuitBreakerService
argument_list|()
argument_list|)
decl_stmt|;
name|NettyTransport
name|nettyTransport
init|=
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
name|bigArrays
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|,
operator|new
name|NamedWriteableRegistry
argument_list|()
argument_list|)
decl_stmt|;
name|nettyTransport
operator|.
name|start
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|nettyTransport
operator|.
name|lifecycleState
argument_list|()
argument_list|,
name|is
argument_list|(
name|Lifecycle
operator|.
name|State
operator|.
name|STARTED
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|nettyTransport
return|;
block|}
DECL|method|assertConnectionRefused
specifier|private
name|void
name|assertConnectionRefused
parameter_list|(
name|int
name|port
parameter_list|)
throws|throws
name|Exception
block|{
try|try
block|{
name|trySocketConnection
argument_list|(
operator|new
name|InetSocketTransportAddress
argument_list|(
name|InetAddress
operator|.
name|getByName
argument_list|(
name|host
argument_list|)
argument_list|,
name|port
argument_list|)
operator|.
name|address
argument_list|()
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Expected to get exception when connecting to port "
operator|+
name|port
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// expected
name|logger
operator|.
name|info
argument_list|(
literal|"Got expected connection message {}"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|assertPortIsBound
specifier|private
name|void
name|assertPortIsBound
parameter_list|(
name|int
name|port
parameter_list|)
throws|throws
name|Exception
block|{
name|assertPortIsBound
argument_list|(
name|host
argument_list|,
name|port
argument_list|)
expr_stmt|;
block|}
DECL|method|assertPortIsBound
specifier|private
name|void
name|assertPortIsBound
parameter_list|(
name|String
name|host
parameter_list|,
name|int
name|port
parameter_list|)
throws|throws
name|Exception
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"Trying to connect to [{}]:[{}]"
argument_list|,
name|host
argument_list|,
name|port
argument_list|)
expr_stmt|;
name|trySocketConnection
argument_list|(
operator|new
name|InetSocketTransportAddress
argument_list|(
name|InetAddress
operator|.
name|getByName
argument_list|(
name|host
argument_list|)
argument_list|,
name|port
argument_list|)
operator|.
name|address
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|trySocketConnection
specifier|private
name|void
name|trySocketConnection
parameter_list|(
name|InetSocketAddress
name|address
parameter_list|)
throws|throws
name|Exception
block|{
try|try
init|(
name|Socket
name|socket
init|=
operator|new
name|Socket
argument_list|()
init|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"Connecting to {}"
argument_list|,
name|address
argument_list|)
expr_stmt|;
name|socket
operator|.
name|connect
argument_list|(
name|address
argument_list|,
literal|500
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|socket
operator|.
name|isConnected
argument_list|()
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
try|try
init|(
name|OutputStream
name|os
init|=
name|socket
operator|.
name|getOutputStream
argument_list|()
init|)
block|{
name|os
operator|.
name|write
argument_list|(
literal|"foo"
operator|.
name|getBytes
argument_list|(
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
argument_list|)
expr_stmt|;
name|os
operator|.
name|flush
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

