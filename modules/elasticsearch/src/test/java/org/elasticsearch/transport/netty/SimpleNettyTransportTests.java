begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|cluster
operator|.
name|node
operator|.
name|Node
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
name|threadpool
operator|.
name|dynamic
operator|.
name|DynamicThreadPool
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
name|*
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|io
operator|.
name|Streamable
import|;
end_import

begin_import
import|import
name|org
operator|.
name|testng
operator|.
name|annotations
operator|.
name|AfterClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|testng
operator|.
name|annotations
operator|.
name|BeforeClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|testng
operator|.
name|annotations
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
name|DataInput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutput
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
name|hamcrest
operator|.
name|MatcherAssert
operator|.
name|*
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
name|*
import|;
end_import

begin_class
DECL|class|SimpleNettyTransportTests
specifier|public
class|class
name|SimpleNettyTransportTests
block|{
DECL|field|threadPool
specifier|private
name|ThreadPool
name|threadPool
decl_stmt|;
DECL|field|serviceA
specifier|private
name|TransportService
name|serviceA
decl_stmt|;
DECL|field|serviceB
specifier|private
name|TransportService
name|serviceB
decl_stmt|;
DECL|field|serviceANode
specifier|private
name|Node
name|serviceANode
decl_stmt|;
DECL|field|serviceBNode
specifier|private
name|Node
name|serviceBNode
decl_stmt|;
DECL|method|setUp
annotation|@
name|BeforeClass
specifier|public
name|void
name|setUp
parameter_list|()
block|{
name|threadPool
operator|=
operator|new
name|DynamicThreadPool
argument_list|()
expr_stmt|;
name|serviceA
operator|=
operator|new
name|TransportService
argument_list|(
operator|new
name|NettyTransport
argument_list|(
name|threadPool
argument_list|)
argument_list|)
operator|.
name|start
argument_list|()
expr_stmt|;
name|serviceANode
operator|=
operator|new
name|Node
argument_list|(
literal|"A"
argument_list|,
name|serviceA
operator|.
name|boundAddress
argument_list|()
operator|.
name|publishAddress
argument_list|()
argument_list|)
expr_stmt|;
name|serviceB
operator|=
operator|new
name|TransportService
argument_list|(
operator|new
name|NettyTransport
argument_list|(
name|threadPool
argument_list|)
argument_list|)
operator|.
name|start
argument_list|()
expr_stmt|;
name|serviceBNode
operator|=
operator|new
name|Node
argument_list|(
literal|"B"
argument_list|,
name|serviceB
operator|.
name|boundAddress
argument_list|()
operator|.
name|publishAddress
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|tearDown
annotation|@
name|AfterClass
specifier|public
name|void
name|tearDown
parameter_list|()
block|{
name|serviceA
operator|.
name|close
argument_list|()
expr_stmt|;
name|serviceB
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
DECL|method|testHelloWorld
annotation|@
name|Test
specifier|public
name|void
name|testHelloWorld
parameter_list|()
block|{
name|serviceA
operator|.
name|registerHandler
argument_list|(
literal|"sayHello"
argument_list|,
operator|new
name|BaseTransportRequestHandler
argument_list|<
name|StringMessage
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|StringMessage
name|newInstance
parameter_list|()
block|{
return|return
operator|new
name|StringMessage
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|messageReceived
parameter_list|(
name|StringMessage
name|request
parameter_list|,
name|TransportChannel
name|channel
parameter_list|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"got message: "
operator|+
name|request
operator|.
name|message
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"moshe"
argument_list|,
name|equalTo
argument_list|(
name|request
operator|.
name|message
argument_list|)
argument_list|)
expr_stmt|;
try|try
block|{
name|channel
operator|.
name|sendResponse
argument_list|(
operator|new
name|StringMessage
argument_list|(
literal|"hello "
operator|+
name|request
operator|.
name|message
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
literal|false
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
name|TransportFuture
argument_list|<
name|StringMessage
argument_list|>
name|res
init|=
name|serviceB
operator|.
name|submitRequest
argument_list|(
name|serviceANode
argument_list|,
literal|"sayHello"
argument_list|,
operator|new
name|StringMessage
argument_list|(
literal|"moshe"
argument_list|)
argument_list|,
operator|new
name|BaseTransportResponseHandler
argument_list|<
name|StringMessage
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|StringMessage
name|newInstance
parameter_list|()
block|{
return|return
operator|new
name|StringMessage
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|handleResponse
parameter_list|(
name|StringMessage
name|response
parameter_list|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"got response: "
operator|+
name|response
operator|.
name|message
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"hello moshe"
argument_list|,
name|equalTo
argument_list|(
name|response
operator|.
name|message
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|handleException
parameter_list|(
name|RemoteTransportException
name|exp
parameter_list|)
block|{
name|exp
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
literal|"got exception instead of a response: "
operator|+
name|exp
operator|.
name|getMessage
argument_list|()
argument_list|,
literal|false
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
decl_stmt|;
try|try
block|{
name|StringMessage
name|message
init|=
name|res
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
literal|"hello moshe"
argument_list|,
name|equalTo
argument_list|(
name|message
operator|.
name|message
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
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
literal|false
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"after ..."
argument_list|)
expr_stmt|;
block|}
DECL|method|testErrorMessage
annotation|@
name|Test
specifier|public
name|void
name|testErrorMessage
parameter_list|()
block|{
name|serviceA
operator|.
name|registerHandler
argument_list|(
literal|"sayHelloException"
argument_list|,
operator|new
name|BaseTransportRequestHandler
argument_list|<
name|StringMessage
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|StringMessage
name|newInstance
parameter_list|()
block|{
return|return
operator|new
name|StringMessage
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|messageReceived
parameter_list|(
name|StringMessage
name|request
parameter_list|,
name|TransportChannel
name|channel
parameter_list|)
throws|throws
name|Exception
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"got message: "
operator|+
name|request
operator|.
name|message
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"moshe"
argument_list|,
name|equalTo
argument_list|(
name|request
operator|.
name|message
argument_list|)
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"bad message !!!"
argument_list|)
throw|;
block|}
block|}
argument_list|)
expr_stmt|;
name|TransportFuture
argument_list|<
name|StringMessage
argument_list|>
name|res
init|=
name|serviceB
operator|.
name|submitRequest
argument_list|(
name|serviceANode
argument_list|,
literal|"sayHelloException"
argument_list|,
operator|new
name|StringMessage
argument_list|(
literal|"moshe"
argument_list|)
argument_list|,
operator|new
name|BaseTransportResponseHandler
argument_list|<
name|StringMessage
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|StringMessage
name|newInstance
parameter_list|()
block|{
return|return
operator|new
name|StringMessage
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|handleResponse
parameter_list|(
name|StringMessage
name|response
parameter_list|)
block|{
name|assertThat
argument_list|(
literal|"got response instead of exception"
argument_list|,
literal|false
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|handleException
parameter_list|(
name|RemoteTransportException
name|exp
parameter_list|)
block|{
name|assertThat
argument_list|(
literal|"bad message !!!"
argument_list|,
name|equalTo
argument_list|(
name|exp
operator|.
name|getCause
argument_list|()
operator|.
name|getMessage
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
decl_stmt|;
try|try
block|{
name|res
operator|.
name|txGet
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
literal|"exception should be thrown"
argument_list|,
literal|false
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
literal|"bad message !!!"
argument_list|,
name|equalTo
argument_list|(
name|e
operator|.
name|getCause
argument_list|()
operator|.
name|getMessage
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"after ..."
argument_list|)
expr_stmt|;
block|}
DECL|class|StringMessage
specifier|private
class|class
name|StringMessage
implements|implements
name|Streamable
block|{
DECL|field|message
specifier|private
name|String
name|message
decl_stmt|;
DECL|method|StringMessage
specifier|private
name|StringMessage
parameter_list|(
name|String
name|message
parameter_list|)
block|{
name|this
operator|.
name|message
operator|=
name|message
expr_stmt|;
block|}
DECL|method|StringMessage
specifier|private
name|StringMessage
parameter_list|()
block|{         }
DECL|method|readFrom
annotation|@
name|Override
specifier|public
name|void
name|readFrom
parameter_list|(
name|DataInput
name|in
parameter_list|)
throws|throws
name|IOException
throws|,
name|ClassNotFoundException
block|{
name|message
operator|=
name|in
operator|.
name|readUTF
argument_list|()
expr_stmt|;
block|}
DECL|method|writeTo
annotation|@
name|Override
specifier|public
name|void
name|writeTo
parameter_list|(
name|DataOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeUTF
argument_list|(
name|message
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

