begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.http
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|http
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|inject
operator|.
name|AbstractModule
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|inject
operator|.
name|Module
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
name|action
operator|.
name|HttpActionModule
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
name|Classes
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
name|settings
operator|.
name|Settings
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|guice
operator|.
name|ModulesFactory
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * @author kimchy (Shay Banon)  */
end_comment

begin_class
DECL|class|HttpServerModule
specifier|public
class|class
name|HttpServerModule
extends|extends
name|AbstractModule
block|{
DECL|field|settings
specifier|private
specifier|final
name|Settings
name|settings
decl_stmt|;
DECL|method|HttpServerModule
specifier|public
name|HttpServerModule
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|this
operator|.
name|settings
operator|=
name|settings
expr_stmt|;
block|}
DECL|method|configure
annotation|@
name|SuppressWarnings
argument_list|(
block|{
literal|"unchecked"
block|}
argument_list|)
annotation|@
name|Override
specifier|protected
name|void
name|configure
parameter_list|()
block|{
name|bind
argument_list|(
name|HttpServer
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|Class
argument_list|<
name|?
extends|extends
name|Module
argument_list|>
name|defaultHttpServerTransportModule
init|=
literal|null
decl_stmt|;
try|try
block|{
name|Classes
operator|.
name|getDefaultClassLoader
argument_list|()
operator|.
name|loadClass
argument_list|(
literal|"org.elasticsearch.http.netty.NettyHttpServerTransport"
argument_list|)
expr_stmt|;
name|defaultHttpServerTransportModule
operator|=
operator|(
name|Class
argument_list|<
name|?
extends|extends
name|Module
argument_list|>
operator|)
name|Classes
operator|.
name|getDefaultClassLoader
argument_list|()
operator|.
name|loadClass
argument_list|(
literal|"org.elasticsearch.http.netty.NettyHttpServerTransportModule"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ClassNotFoundException
name|e
parameter_list|)
block|{
comment|// no netty one, ok...
if|if
condition|(
name|settings
operator|.
name|get
argument_list|(
literal|"http.type"
argument_list|)
operator|==
literal|null
condition|)
block|{
comment|// no explicit one is configured, bail
return|return;
block|}
block|}
name|Class
argument_list|<
name|?
extends|extends
name|Module
argument_list|>
name|moduleClass
init|=
name|settings
operator|.
name|getAsClass
argument_list|(
literal|"http.type"
argument_list|,
name|defaultHttpServerTransportModule
argument_list|,
literal|"org.elasticsearch.http."
argument_list|,
literal|"HttpServerTransportModule"
argument_list|)
decl_stmt|;
name|createModule
argument_list|(
name|moduleClass
argument_list|,
name|settings
argument_list|)
operator|.
name|configure
argument_list|(
name|binder
argument_list|()
argument_list|)
expr_stmt|;
operator|new
name|HttpActionModule
argument_list|()
operator|.
name|configure
argument_list|(
name|binder
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

