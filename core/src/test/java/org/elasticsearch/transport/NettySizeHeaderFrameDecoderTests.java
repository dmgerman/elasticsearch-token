begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.transport
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|transport
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
name|cache
operator|.
name|recycler
operator|.
name|MockPageCacheRecycler
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
name|node
operator|.
name|settings
operator|.
name|NodeSettingsService
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
name|netty
operator|.
name|NettyTransport
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|After
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
name|java
operator|.
name|io
operator|.
name|BufferedReader
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|InputStreamReader
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
name|Socket
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

begin_comment
comment|/**  * This test checks, if a HTTP look-alike request (starting with a HTTP method and a space)  * actually returns text response instead of just dropping the connection  */
end_comment

begin_class
DECL|class|NettySizeHeaderFrameDecoderTests
specifier|public
class|class
name|NettySizeHeaderFrameDecoderTests
extends|extends
name|ESTestCase
block|{
DECL|field|settings
specifier|private
specifier|final
name|Settings
name|settings
init|=
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"name"
argument_list|,
literal|"foo"
argument_list|)
operator|.
name|put
argument_list|(
literal|"transport.host"
argument_list|,
literal|"127.0.0.1"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
DECL|field|threadPool
specifier|private
name|ThreadPool
name|threadPool
decl_stmt|;
DECL|field|nettyTransport
specifier|private
name|NettyTransport
name|nettyTransport
decl_stmt|;
DECL|field|port
specifier|private
name|int
name|port
decl_stmt|;
DECL|field|host
specifier|private
name|InetAddress
name|host
decl_stmt|;
annotation|@
name|Before
DECL|method|startThreadPool
specifier|public
name|void
name|startThreadPool
parameter_list|()
block|{
name|threadPool
operator|=
operator|new
name|ThreadPool
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|threadPool
operator|.
name|setNodeSettingsService
argument_list|(
operator|new
name|NodeSettingsService
argument_list|(
name|settings
argument_list|)
argument_list|)
expr_stmt|;
name|NetworkService
name|networkService
init|=
operator|new
name|NetworkService
argument_list|(
name|settings
argument_list|)
decl_stmt|;
name|BigArrays
name|bigArrays
init|=
operator|new
name|MockBigArrays
argument_list|(
operator|new
name|MockPageCacheRecycler
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
name|nettyTransport
operator|=
operator|new
name|NettyTransport
argument_list|(
name|settings
argument_list|,
name|threadPool
argument_list|,
name|networkService
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
expr_stmt|;
name|nettyTransport
operator|.
name|start
argument_list|()
expr_stmt|;
name|TransportService
name|transportService
init|=
operator|new
name|TransportService
argument_list|(
name|nettyTransport
argument_list|,
name|threadPool
argument_list|)
decl_stmt|;
name|nettyTransport
operator|.
name|transportServiceAdapter
argument_list|(
name|transportService
operator|.
name|createAdapter
argument_list|()
argument_list|)
expr_stmt|;
name|InetSocketTransportAddress
name|transportAddress
init|=
operator|(
name|InetSocketTransportAddress
operator|)
name|randomFrom
argument_list|(
name|nettyTransport
operator|.
name|boundAddress
argument_list|()
operator|.
name|boundAddresses
argument_list|()
argument_list|)
decl_stmt|;
name|port
operator|=
name|transportAddress
operator|.
name|address
argument_list|()
operator|.
name|getPort
argument_list|()
expr_stmt|;
name|host
operator|=
name|transportAddress
operator|.
name|address
argument_list|()
operator|.
name|getAddress
argument_list|()
expr_stmt|;
block|}
annotation|@
name|After
DECL|method|terminateThreadPool
specifier|public
name|void
name|terminateThreadPool
parameter_list|()
throws|throws
name|InterruptedException
block|{
name|nettyTransport
operator|.
name|stop
argument_list|()
expr_stmt|;
name|terminate
argument_list|(
name|threadPool
argument_list|)
expr_stmt|;
block|}
DECL|method|testThatTextMessageIsReturnedOnHTTPLikeRequest
specifier|public
name|void
name|testThatTextMessageIsReturnedOnHTTPLikeRequest
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|randomMethod
init|=
name|randomFrom
argument_list|(
literal|"GET"
argument_list|,
literal|"POST"
argument_list|,
literal|"PUT"
argument_list|,
literal|"DELETE"
argument_list|,
literal|"HEAD"
argument_list|,
literal|"OPTIONS"
argument_list|,
literal|"PATCH"
argument_list|)
decl_stmt|;
name|String
name|data
init|=
name|randomMethod
operator|+
literal|" / HTTP/1.1"
decl_stmt|;
try|try
init|(
name|Socket
name|socket
init|=
operator|new
name|Socket
argument_list|(
name|host
argument_list|,
name|port
argument_list|)
init|)
block|{
name|socket
operator|.
name|getOutputStream
argument_list|()
operator|.
name|write
argument_list|(
name|data
operator|.
name|getBytes
argument_list|(
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
argument_list|)
expr_stmt|;
name|socket
operator|.
name|getOutputStream
argument_list|()
operator|.
name|flush
argument_list|()
expr_stmt|;
try|try
init|(
name|BufferedReader
name|reader
init|=
operator|new
name|BufferedReader
argument_list|(
operator|new
name|InputStreamReader
argument_list|(
name|socket
operator|.
name|getInputStream
argument_list|()
argument_list|,
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
argument_list|)
init|)
block|{
name|assertThat
argument_list|(
name|reader
operator|.
name|readLine
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"This is not a HTTP port"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|testThatNothingIsReturnedForOtherInvalidPackets
specifier|public
name|void
name|testThatNothingIsReturnedForOtherInvalidPackets
parameter_list|()
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
argument_list|(
name|host
argument_list|,
name|port
argument_list|)
init|)
block|{
name|socket
operator|.
name|getOutputStream
argument_list|()
operator|.
name|write
argument_list|(
literal|"FOOBAR"
operator|.
name|getBytes
argument_list|(
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
argument_list|)
expr_stmt|;
name|socket
operator|.
name|getOutputStream
argument_list|()
operator|.
name|flush
argument_list|()
expr_stmt|;
comment|// end of stream
name|assertThat
argument_list|(
name|socket
operator|.
name|getInputStream
argument_list|()
operator|.
name|read
argument_list|()
argument_list|,
name|is
argument_list|(
operator|-
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

