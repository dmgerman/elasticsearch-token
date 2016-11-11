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
name|cluster
operator|.
name|node
operator|.
name|DiscoveryNode
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|function
operator|.
name|Supplier
import|;
end_import

begin_comment
comment|/**  * This interface allows plugins to intercept requests on both the sender and the receiver side.  */
end_comment

begin_interface
DECL|interface|TransportInterceptor
specifier|public
interface|interface
name|TransportInterceptor
block|{
comment|/**      * This is called for each handler that is registered via      * {@link TransportService#registerRequestHandler(String, Supplier, String, boolean, boolean, TransportRequestHandler)} or      * {@link TransportService#registerRequestHandler(String, Supplier, String, TransportRequestHandler)}. The returned handler is      * used instead of the passed in handler. By default the provided handler is returned.      */
DECL|method|interceptHandler
specifier|default
parameter_list|<
name|T
extends|extends
name|TransportRequest
parameter_list|>
name|TransportRequestHandler
argument_list|<
name|T
argument_list|>
name|interceptHandler
parameter_list|(
name|String
name|action
parameter_list|,
name|String
name|executor
parameter_list|,
name|TransportRequestHandler
argument_list|<
name|T
argument_list|>
name|actualHandler
parameter_list|)
block|{
return|return
name|actualHandler
return|;
block|}
comment|/**      * This is called up-front providing the actual low level {@link AsyncSender} that performs the low level send request.      * The returned sender is used to send all requests that come in via      * {@link TransportService#sendRequest(DiscoveryNode, String, TransportRequest, TransportResponseHandler)} or      * {@link TransportService#sendRequest(DiscoveryNode, String, TransportRequest, TransportRequestOptions, TransportResponseHandler)}.      * This allows plugins to perform actions on each send request including modifying the request context etc.      */
DECL|method|interceptSender
specifier|default
name|AsyncSender
name|interceptSender
parameter_list|(
name|AsyncSender
name|sender
parameter_list|)
block|{
return|return
name|sender
return|;
block|}
comment|/**      * A simple interface to decorate      * {@link #sendRequest(DiscoveryNode, String, TransportRequest, TransportRequestOptions, TransportResponseHandler)}      */
DECL|interface|AsyncSender
interface|interface
name|AsyncSender
block|{
DECL|method|sendRequest
parameter_list|<
name|T
extends|extends
name|TransportResponse
parameter_list|>
name|void
name|sendRequest
parameter_list|(
specifier|final
name|DiscoveryNode
name|node
parameter_list|,
specifier|final
name|String
name|action
parameter_list|,
specifier|final
name|TransportRequest
name|request
parameter_list|,
specifier|final
name|TransportRequestOptions
name|options
parameter_list|,
name|TransportResponseHandler
argument_list|<
name|T
argument_list|>
name|handler
parameter_list|)
function_decl|;
block|}
block|}
end_interface

end_unit

