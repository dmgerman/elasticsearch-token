begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|network
operator|.
name|NetworkModule
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
name|Setting
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
name|plugins
operator|.
name|Plugin
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
name|ESIntegTestCase
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
name|MockTcpTransportPlugin
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
name|Netty3Plugin
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
name|Netty4Plugin
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
name|util
operator|.
name|Collection
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

begin_class
DECL|class|HttpSmokeTestCase
specifier|public
specifier|abstract
class|class
name|HttpSmokeTestCase
extends|extends
name|ESIntegTestCase
block|{
DECL|field|nodeTransportTypeKey
specifier|private
specifier|static
name|String
name|nodeTransportTypeKey
decl_stmt|;
DECL|field|nodeHttpTypeKey
specifier|private
specifier|static
name|String
name|nodeHttpTypeKey
decl_stmt|;
DECL|field|clientTypeKey
specifier|private
specifier|static
name|String
name|clientTypeKey
decl_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
annotation|@
name|BeforeClass
DECL|method|setUpTransport
specifier|public
specifier|static
name|void
name|setUpTransport
parameter_list|()
block|{
name|nodeTransportTypeKey
operator|=
name|getTypeKey
argument_list|(
name|randomFrom
argument_list|(
name|MockTcpTransportPlugin
operator|.
name|class
argument_list|,
name|Netty3Plugin
operator|.
name|class
argument_list|,
name|Netty4Plugin
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|nodeHttpTypeKey
operator|=
name|getTypeKey
argument_list|(
name|randomFrom
argument_list|(
name|Netty3Plugin
operator|.
name|class
argument_list|,
name|Netty4Plugin
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|clientTypeKey
operator|=
name|getTypeKey
argument_list|(
name|randomFrom
argument_list|(
name|MockTcpTransportPlugin
operator|.
name|class
argument_list|,
name|Netty3Plugin
operator|.
name|class
argument_list|,
name|Netty4Plugin
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|getTypeKey
specifier|private
specifier|static
name|String
name|getTypeKey
parameter_list|(
name|Class
argument_list|<
name|?
extends|extends
name|Plugin
argument_list|>
name|clazz
parameter_list|)
block|{
if|if
condition|(
name|clazz
operator|.
name|equals
argument_list|(
name|MockTcpTransportPlugin
operator|.
name|class
argument_list|)
condition|)
block|{
return|return
name|MockTcpTransportPlugin
operator|.
name|MOCK_TCP_TRANSPORT_NAME
return|;
block|}
elseif|else
if|if
condition|(
name|clazz
operator|.
name|equals
argument_list|(
name|Netty3Plugin
operator|.
name|class
argument_list|)
condition|)
block|{
return|return
name|Netty3Plugin
operator|.
name|NETTY_TRANSPORT_NAME
return|;
block|}
else|else
block|{
assert|assert
name|clazz
operator|.
name|equals
argument_list|(
name|Netty4Plugin
operator|.
name|class
argument_list|)
assert|;
return|return
name|Netty4Plugin
operator|.
name|NETTY_TRANSPORT_NAME
return|;
block|}
block|}
annotation|@
name|Override
DECL|method|nodeSettings
specifier|protected
name|Settings
name|nodeSettings
parameter_list|(
name|int
name|nodeOrdinal
parameter_list|)
block|{
return|return
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|super
operator|.
name|nodeSettings
argument_list|(
name|nodeOrdinal
argument_list|)
argument_list|)
operator|.
name|put
argument_list|(
name|NetworkModule
operator|.
name|TRANSPORT_TYPE_KEY
argument_list|,
name|nodeTransportTypeKey
argument_list|)
operator|.
name|put
argument_list|(
name|NetworkModule
operator|.
name|HTTP_TYPE_KEY
argument_list|,
name|nodeHttpTypeKey
argument_list|)
operator|.
name|put
argument_list|(
name|NetworkModule
operator|.
name|HTTP_ENABLED
operator|.
name|getKey
argument_list|()
argument_list|,
literal|true
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|nodePlugins
specifier|protected
name|Collection
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|Plugin
argument_list|>
argument_list|>
name|nodePlugins
parameter_list|()
block|{
return|return
name|pluginList
argument_list|(
name|MockTcpTransportPlugin
operator|.
name|class
argument_list|,
name|Netty3Plugin
operator|.
name|class
argument_list|,
name|Netty4Plugin
operator|.
name|class
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|transportClientPlugins
specifier|protected
name|Collection
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|Plugin
argument_list|>
argument_list|>
name|transportClientPlugins
parameter_list|()
block|{
return|return
name|pluginList
argument_list|(
name|MockTcpTransportPlugin
operator|.
name|class
argument_list|,
name|Netty3Plugin
operator|.
name|class
argument_list|,
name|Netty4Plugin
operator|.
name|class
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|transportClientSettings
specifier|protected
name|Settings
name|transportClientSettings
parameter_list|()
block|{
return|return
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|super
operator|.
name|transportClientSettings
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|NetworkModule
operator|.
name|TRANSPORT_TYPE_KEY
argument_list|,
name|clientTypeKey
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|ignoreExternalCluster
specifier|protected
name|boolean
name|ignoreExternalCluster
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
block|}
end_class

end_unit

