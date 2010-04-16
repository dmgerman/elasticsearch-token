begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.memcached.netty
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|memcached
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
name|memcached
operator|.
name|MemcachedRestRequest
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|RestController
import|;
end_import

begin_import
import|import
name|org
operator|.
name|jboss
operator|.
name|netty
operator|.
name|channel
operator|.
name|ChannelHandlerContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|jboss
operator|.
name|netty
operator|.
name|channel
operator|.
name|MessageEvent
import|;
end_import

begin_import
import|import
name|org
operator|.
name|jboss
operator|.
name|netty
operator|.
name|channel
operator|.
name|SimpleChannelUpstreamHandler
import|;
end_import

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|MemcachedDispatcher
specifier|public
class|class
name|MemcachedDispatcher
extends|extends
name|SimpleChannelUpstreamHandler
block|{
DECL|field|restController
specifier|private
specifier|final
name|RestController
name|restController
decl_stmt|;
DECL|method|MemcachedDispatcher
specifier|public
name|MemcachedDispatcher
parameter_list|(
name|RestController
name|restController
parameter_list|)
block|{
name|this
operator|.
name|restController
operator|=
name|restController
expr_stmt|;
block|}
DECL|method|messageReceived
annotation|@
name|Override
specifier|public
name|void
name|messageReceived
parameter_list|(
name|ChannelHandlerContext
name|ctx
parameter_list|,
name|MessageEvent
name|e
parameter_list|)
throws|throws
name|Exception
block|{
name|MemcachedRestRequest
name|request
init|=
operator|(
name|MemcachedRestRequest
operator|)
name|e
operator|.
name|getMessage
argument_list|()
decl_stmt|;
name|restController
operator|.
name|dispatchRequest
argument_list|(
name|request
argument_list|,
operator|new
name|MemcachedRestChannel
argument_list|(
name|ctx
operator|.
name|getChannel
argument_list|()
argument_list|,
name|request
argument_list|)
argument_list|)
expr_stmt|;
name|super
operator|.
name|messageReceived
argument_list|(
name|ctx
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

