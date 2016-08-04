begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.transport.client
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|transport
operator|.
name|client
package|;
end_package

begin_import
import|import
name|io
operator|.
name|netty
operator|.
name|util
operator|.
name|ThreadDeathWatcher
import|;
end_import

begin_import
import|import
name|io
operator|.
name|netty
operator|.
name|util
operator|.
name|concurrent
operator|.
name|GlobalEventExecutor
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|client
operator|.
name|transport
operator|.
name|TransportClient
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
name|index
operator|.
name|reindex
operator|.
name|ReindexPlugin
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|percolator
operator|.
name|PercolatorPlugin
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
name|script
operator|.
name|mustache
operator|.
name|MustachePlugin
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
name|java
operator|.
name|util
operator|.
name|Arrays
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|TimeUnit
import|;
end_import

begin_comment
comment|/**  * A builder to create an instance of {@link TransportClient}  * This class pre-installs the  * {@link Netty3Plugin},  * {@link Netty4Plugin},  * {@link ReindexPlugin},  * {@link PercolatorPlugin},  * and {@link MustachePlugin}  * for the client. These plugins are all elasticsearch core modules required.  */
end_comment

begin_class
annotation|@
name|SuppressWarnings
argument_list|(
block|{
literal|"unchecked"
block|,
literal|"varargs"
block|}
argument_list|)
DECL|class|PreBuiltTransportClient
specifier|public
class|class
name|PreBuiltTransportClient
extends|extends
name|TransportClient
block|{
DECL|field|PRE_INSTALLED_PLUGINS
specifier|private
specifier|static
specifier|final
name|Collection
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|Plugin
argument_list|>
argument_list|>
name|PRE_INSTALLED_PLUGINS
init|=
name|Collections
operator|.
name|unmodifiableList
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|Netty3Plugin
operator|.
name|class
argument_list|,
name|Netty4Plugin
operator|.
name|class
argument_list|,
name|TransportPlugin
operator|.
name|class
argument_list|,
name|ReindexPlugin
operator|.
name|class
argument_list|,
name|PercolatorPlugin
operator|.
name|class
argument_list|,
name|MustachePlugin
operator|.
name|class
argument_list|)
argument_list|)
decl_stmt|;
annotation|@
name|SafeVarargs
DECL|method|PreBuiltTransportClient
specifier|public
name|PreBuiltTransportClient
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|Plugin
argument_list|>
modifier|...
name|plugins
parameter_list|)
block|{
name|this
argument_list|(
name|settings
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|plugins
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|PreBuiltTransportClient
specifier|public
name|PreBuiltTransportClient
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|Collection
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|Plugin
argument_list|>
argument_list|>
name|plugins
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|Settings
operator|.
name|EMPTY
argument_list|,
name|addPlugins
argument_list|(
name|plugins
argument_list|,
name|PRE_INSTALLED_PLUGINS
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|class|TransportPlugin
specifier|public
specifier|static
specifier|final
class|class
name|TransportPlugin
extends|extends
name|Plugin
block|{
DECL|field|ASSERT_NETTY_BUGLEVEL
specifier|private
specifier|static
specifier|final
name|Setting
argument_list|<
name|Boolean
argument_list|>
name|ASSERT_NETTY_BUGLEVEL
init|=
name|Setting
operator|.
name|boolSetting
argument_list|(
literal|"netty.assert.buglevel"
argument_list|,
literal|true
argument_list|,
name|Setting
operator|.
name|Property
operator|.
name|NodeScope
argument_list|)
decl_stmt|;
annotation|@
name|Override
DECL|method|getSettings
specifier|public
name|List
argument_list|<
name|Setting
argument_list|<
name|?
argument_list|>
argument_list|>
name|getSettings
parameter_list|()
block|{
return|return
name|Collections
operator|.
name|singletonList
argument_list|(
name|ASSERT_NETTY_BUGLEVEL
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|additionalSettings
specifier|public
name|Settings
name|additionalSettings
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
literal|"netty.assert.buglevel"
argument_list|,
literal|true
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
block|}
annotation|@
name|Override
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
block|{
name|super
operator|.
name|close
argument_list|()
expr_stmt|;
if|if
condition|(
operator|!
name|NetworkModule
operator|.
name|TRANSPORT_TYPE_SETTING
operator|.
name|exists
argument_list|(
name|settings
argument_list|)
operator|||
name|NetworkModule
operator|.
name|TRANSPORT_TYPE_SETTING
operator|.
name|get
argument_list|(
name|settings
argument_list|)
operator|.
name|equals
argument_list|(
name|Netty4Plugin
operator|.
name|NETTY_TRANSPORT_NAME
argument_list|)
condition|)
block|{
try|try
block|{
name|GlobalEventExecutor
operator|.
name|INSTANCE
operator|.
name|awaitInactivity
argument_list|(
literal|5
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
name|ThreadDeathWatcher
operator|.
name|awaitInactivity
argument_list|(
literal|5
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
block|}
block|}
end_class

end_unit

