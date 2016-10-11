begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.plugins
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|plugins
package|;
end_package

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
name|http
operator|.
name|HttpServerTransport
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
name|CircuitBreakerService
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
name|TransportInterceptor
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
name|function
operator|.
name|Supplier
import|;
end_import

begin_comment
comment|/**  * Plugin for extending network and transport related classes  */
end_comment

begin_interface
DECL|interface|NetworkPlugin
specifier|public
interface|interface
name|NetworkPlugin
block|{
comment|/**      * Returns a list of {@link TransportInterceptor} instances that are used to intercept incoming and outgoing      * transport (inter-node) requests. This must not return<code>null</code>      */
DECL|method|getTransportInterceptors
specifier|default
name|List
argument_list|<
name|TransportInterceptor
argument_list|>
name|getTransportInterceptors
parameter_list|(
name|NamedWriteableRegistry
name|namedWriteableRegistry
parameter_list|)
block|{
return|return
name|Collections
operator|.
name|emptyList
argument_list|()
return|;
block|}
comment|/**      * Returns a map of {@link Transport} suppliers.      * See {@link org.elasticsearch.common.network.NetworkModule#TRANSPORT_TYPE_KEY} to configure a specific implementation.      */
DECL|method|getTransports
specifier|default
name|Map
argument_list|<
name|String
argument_list|,
name|Supplier
argument_list|<
name|Transport
argument_list|>
argument_list|>
name|getTransports
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|,
name|BigArrays
name|bigArrays
parameter_list|,
name|CircuitBreakerService
name|circuitBreakerService
parameter_list|,
name|NamedWriteableRegistry
name|namedWriteableRegistry
parameter_list|,
name|NetworkService
name|networkService
parameter_list|)
block|{
return|return
name|Collections
operator|.
name|emptyMap
argument_list|()
return|;
block|}
comment|/**      * Returns a map of {@link HttpServerTransport} suppliers.      * See {@link org.elasticsearch.common.network.NetworkModule#HTTP_TYPE_SETTING} to configure a specific implementation.      */
DECL|method|getHttpTransports
specifier|default
name|Map
argument_list|<
name|String
argument_list|,
name|Supplier
argument_list|<
name|HttpServerTransport
argument_list|>
argument_list|>
name|getHttpTransports
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|,
name|BigArrays
name|bigArrays
parameter_list|,
name|CircuitBreakerService
name|circuitBreakerService
parameter_list|,
name|NamedWriteableRegistry
name|namedWriteableRegistry
parameter_list|,
name|NetworkService
name|networkService
parameter_list|)
block|{
return|return
name|Collections
operator|.
name|emptyMap
argument_list|()
return|;
block|}
block|}
end_interface

end_unit

