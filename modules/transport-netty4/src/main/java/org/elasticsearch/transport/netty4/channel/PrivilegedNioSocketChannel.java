begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.transport.netty4.channel
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|transport
operator|.
name|netty4
operator|.
name|channel
package|;
end_package

begin_import
import|import
name|io
operator|.
name|netty
operator|.
name|channel
operator|.
name|socket
operator|.
name|nio
operator|.
name|NioSocketChannel
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|SpecialPermission
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|SocketAddress
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|AccessController
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|PrivilegedAction
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|PrivilegedActionException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|PrivilegedExceptionAction
import|;
end_import

begin_comment
comment|/**  * Wraps netty calls to {@link java.nio.channels.SocketChannel#connect(SocketAddress)} in  * {@link AccessController#doPrivileged(PrivilegedAction)} blocks. This is necessary to limit  * {@link java.net.SocketPermission} to the transport module.  */
end_comment

begin_class
DECL|class|PrivilegedNioSocketChannel
specifier|public
class|class
name|PrivilegedNioSocketChannel
extends|extends
name|NioSocketChannel
block|{
annotation|@
name|Override
DECL|method|doConnect
specifier|protected
name|boolean
name|doConnect
parameter_list|(
name|SocketAddress
name|remoteAddress
parameter_list|,
name|SocketAddress
name|localAddress
parameter_list|)
throws|throws
name|Exception
block|{
name|SecurityManager
name|sm
init|=
name|System
operator|.
name|getSecurityManager
argument_list|()
decl_stmt|;
if|if
condition|(
name|sm
operator|!=
literal|null
condition|)
block|{
name|sm
operator|.
name|checkPermission
argument_list|(
operator|new
name|SpecialPermission
argument_list|()
argument_list|)
expr_stmt|;
block|}
try|try
block|{
return|return
name|AccessController
operator|.
name|doPrivileged
argument_list|(
call|(
name|PrivilegedExceptionAction
argument_list|<
name|Boolean
argument_list|>
call|)
argument_list|()
operator|->
name|super
operator|.
name|doConnect
argument_list|(
name|remoteAddress
argument_list|,
name|localAddress
argument_list|)
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|PrivilegedActionException
name|e
parameter_list|)
block|{
throw|throw
operator|(
name|Exception
operator|)
name|e
operator|.
name|getCause
argument_list|()
throw|;
block|}
block|}
block|}
end_class

end_unit

