begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.thrift
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|thrift
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TBinaryProtocol
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TCompactProtocol
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TProtocolFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|server
operator|.
name|TServer
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|server
operator|.
name|TThreadPoolServer
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|transport
operator|.
name|TFramedTransport
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|transport
operator|.
name|TServerSocket
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|transport
operator|.
name|TTransportFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticSearchException
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
name|TransportNodesInfoAction
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
name|AbstractLifecycleComponent
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
name|inject
operator|.
name|Inject
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
name|PortsRange
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
name|ByteSizeValue
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
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicReference
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
name|util
operator|.
name|concurrent
operator|.
name|EsExecutors
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|ThriftServer
specifier|public
class|class
name|ThriftServer
extends|extends
name|AbstractLifecycleComponent
argument_list|<
name|ThriftServer
argument_list|>
block|{
DECL|field|frame
specifier|final
name|int
name|frame
decl_stmt|;
DECL|field|port
specifier|final
name|String
name|port
decl_stmt|;
DECL|field|bindHost
specifier|final
name|String
name|bindHost
decl_stmt|;
DECL|field|publishHost
specifier|final
name|String
name|publishHost
decl_stmt|;
DECL|field|networkService
specifier|private
specifier|final
name|NetworkService
name|networkService
decl_stmt|;
DECL|field|nodesInfoAction
specifier|private
specifier|final
name|TransportNodesInfoAction
name|nodesInfoAction
decl_stmt|;
DECL|field|client
specifier|private
specifier|final
name|ThriftRestImpl
name|client
decl_stmt|;
DECL|field|protocolFactory
specifier|private
specifier|final
name|TProtocolFactory
name|protocolFactory
decl_stmt|;
DECL|field|server
specifier|private
specifier|volatile
name|TServer
name|server
decl_stmt|;
DECL|field|portNumber
specifier|private
specifier|volatile
name|int
name|portNumber
decl_stmt|;
DECL|method|ThriftServer
annotation|@
name|Inject
specifier|public
name|ThriftServer
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|NetworkService
name|networkService
parameter_list|,
name|TransportNodesInfoAction
name|nodesInfoAction
parameter_list|,
name|ThriftRestImpl
name|client
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|client
operator|=
name|client
expr_stmt|;
name|this
operator|.
name|networkService
operator|=
name|networkService
expr_stmt|;
name|this
operator|.
name|nodesInfoAction
operator|=
name|nodesInfoAction
expr_stmt|;
name|this
operator|.
name|frame
operator|=
operator|(
name|int
operator|)
name|componentSettings
operator|.
name|getAsBytesSize
argument_list|(
literal|"frame"
argument_list|,
operator|new
name|ByteSizeValue
argument_list|(
operator|-
literal|1
argument_list|)
argument_list|)
operator|.
name|bytes
argument_list|()
expr_stmt|;
name|this
operator|.
name|port
operator|=
name|componentSettings
operator|.
name|get
argument_list|(
literal|"port"
argument_list|,
literal|"9500-9600"
argument_list|)
expr_stmt|;
name|this
operator|.
name|bindHost
operator|=
name|componentSettings
operator|.
name|get
argument_list|(
literal|"bind_host"
argument_list|,
name|settings
operator|.
name|get
argument_list|(
literal|"transport.bind_host"
argument_list|,
name|settings
operator|.
name|get
argument_list|(
literal|"transport.host"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|publishHost
operator|=
name|componentSettings
operator|.
name|get
argument_list|(
literal|"publish_host"
argument_list|,
name|settings
operator|.
name|get
argument_list|(
literal|"transport.publish_host"
argument_list|,
name|settings
operator|.
name|get
argument_list|(
literal|"transport.host"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|componentSettings
operator|.
name|get
argument_list|(
literal|"protocol"
argument_list|,
literal|"binary"
argument_list|)
operator|.
name|equals
argument_list|(
literal|"compact"
argument_list|)
condition|)
block|{
name|protocolFactory
operator|=
operator|new
name|TCompactProtocol
operator|.
name|Factory
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|protocolFactory
operator|=
operator|new
name|TBinaryProtocol
operator|.
name|Factory
argument_list|()
expr_stmt|;
block|}
block|}
DECL|method|doStart
annotation|@
name|Override
specifier|protected
name|void
name|doStart
parameter_list|()
throws|throws
name|ElasticSearchException
block|{
name|InetAddress
name|bindAddrX
decl_stmt|;
try|try
block|{
name|bindAddrX
operator|=
name|networkService
operator|.
name|resolveBindHostAddress
argument_list|(
name|bindHost
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|BindTransportException
argument_list|(
literal|"Failed to resolve host ["
operator|+
name|bindHost
operator|+
literal|"]"
argument_list|,
name|e
argument_list|)
throw|;
block|}
specifier|final
name|InetAddress
name|bindAddr
init|=
name|bindAddrX
decl_stmt|;
name|PortsRange
name|portsRange
init|=
operator|new
name|PortsRange
argument_list|(
name|port
argument_list|)
decl_stmt|;
specifier|final
name|AtomicReference
argument_list|<
name|Exception
argument_list|>
name|lastException
init|=
operator|new
name|AtomicReference
argument_list|<
name|Exception
argument_list|>
argument_list|()
decl_stmt|;
name|boolean
name|success
init|=
name|portsRange
operator|.
name|iterate
argument_list|(
operator|new
name|PortsRange
operator|.
name|PortCallback
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|onPortNumber
parameter_list|(
name|int
name|portNumber
parameter_list|)
block|{
name|ThriftServer
operator|.
name|this
operator|.
name|portNumber
operator|=
name|portNumber
expr_stmt|;
try|try
block|{
name|Rest
operator|.
name|Processor
name|processor
init|=
operator|new
name|Rest
operator|.
name|Processor
argument_list|(
name|client
argument_list|)
decl_stmt|;
comment|// Bind and start to accept incoming connections.
name|TServerSocket
name|serverSocket
init|=
operator|new
name|TServerSocket
argument_list|(
operator|new
name|InetSocketAddress
argument_list|(
name|bindAddr
argument_list|,
name|portNumber
argument_list|)
argument_list|)
decl_stmt|;
name|TThreadPoolServer
operator|.
name|Args
name|args
init|=
operator|new
name|TThreadPoolServer
operator|.
name|Args
argument_list|(
name|serverSocket
argument_list|)
operator|.
name|minWorkerThreads
argument_list|(
literal|16
argument_list|)
operator|.
name|maxWorkerThreads
argument_list|(
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
operator|.
name|inputProtocolFactory
argument_list|(
name|protocolFactory
argument_list|)
operator|.
name|outputProtocolFactory
argument_list|(
name|protocolFactory
argument_list|)
operator|.
name|processor
argument_list|(
name|processor
argument_list|)
decl_stmt|;
if|if
condition|(
name|frame
operator|<=
literal|0
condition|)
block|{
name|args
operator|.
name|inputTransportFactory
argument_list|(
operator|new
name|TTransportFactory
argument_list|()
argument_list|)
expr_stmt|;
name|args
operator|.
name|outputTransportFactory
argument_list|(
operator|new
name|TTransportFactory
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|args
operator|.
name|inputTransportFactory
argument_list|(
operator|new
name|TFramedTransport
operator|.
name|Factory
argument_list|(
name|frame
argument_list|)
argument_list|)
expr_stmt|;
name|args
operator|.
name|outputTransportFactory
argument_list|(
operator|new
name|TFramedTransport
operator|.
name|Factory
argument_list|(
name|frame
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|server
operator|=
operator|new
name|TThreadPoolServer
argument_list|(
name|args
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|lastException
operator|.
name|set
argument_list|(
name|e
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
block|}
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|success
condition|)
block|{
throw|throw
operator|new
name|BindTransportException
argument_list|(
literal|"Failed to bind to ["
operator|+
name|port
operator|+
literal|"]"
argument_list|,
name|lastException
operator|.
name|get
argument_list|()
argument_list|)
throw|;
block|}
name|logger
operator|.
name|info
argument_list|(
literal|"bound on port [{}]"
argument_list|,
name|portNumber
argument_list|)
expr_stmt|;
try|try
block|{
name|nodesInfoAction
operator|.
name|putNodeAttribute
argument_list|(
literal|"thrift_address"
argument_list|,
operator|new
name|InetSocketAddress
argument_list|(
name|networkService
operator|.
name|resolvePublishHostAddress
argument_list|(
name|publishHost
argument_list|)
argument_list|,
name|portNumber
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// ignore
block|}
name|daemonThreadFactory
argument_list|(
name|settings
argument_list|,
literal|"thrift_server"
argument_list|)
operator|.
name|newThread
argument_list|(
operator|new
name|Runnable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|server
operator|.
name|serve
argument_list|()
expr_stmt|;
block|}
block|}
argument_list|)
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
DECL|method|doStop
annotation|@
name|Override
specifier|protected
name|void
name|doStop
parameter_list|()
throws|throws
name|ElasticSearchException
block|{
name|nodesInfoAction
operator|.
name|removeNodeAttribute
argument_list|(
literal|"thrift_address"
argument_list|)
expr_stmt|;
name|server
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
DECL|method|doClose
annotation|@
name|Override
specifier|protected
name|void
name|doClose
parameter_list|()
throws|throws
name|ElasticSearchException
block|{     }
block|}
end_class

end_unit

