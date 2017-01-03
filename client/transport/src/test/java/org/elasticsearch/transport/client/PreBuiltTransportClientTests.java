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
name|com
operator|.
name|carrotsearch
operator|.
name|randomizedtesting
operator|.
name|RandomizedTest
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|Constants
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
name|Netty4Plugin
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
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
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertEquals
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertTrue
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|fail
import|;
end_import

begin_class
DECL|class|PreBuiltTransportClientTests
specifier|public
class|class
name|PreBuiltTransportClientTests
extends|extends
name|RandomizedTest
block|{
annotation|@
name|Test
DECL|method|testPluginInstalled
specifier|public
name|void
name|testPluginInstalled
parameter_list|()
block|{
comment|// TODO: remove when Netty 4.1.6 is upgraded to Netty 4.1.7 including https://github.com/netty/netty/pull/6068
name|assumeFalse
argument_list|(
name|Constants
operator|.
name|JRE_IS_MINIMUM_JAVA9
argument_list|)
expr_stmt|;
try|try
init|(
name|TransportClient
name|client
init|=
operator|new
name|PreBuiltTransportClient
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
init|)
block|{
name|Settings
name|settings
init|=
name|client
operator|.
name|settings
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|Netty4Plugin
operator|.
name|NETTY_TRANSPORT_NAME
argument_list|,
name|NetworkModule
operator|.
name|HTTP_DEFAULT_TYPE_SETTING
operator|.
name|get
argument_list|(
name|settings
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Netty4Plugin
operator|.
name|NETTY_TRANSPORT_NAME
argument_list|,
name|NetworkModule
operator|.
name|TRANSPORT_DEFAULT_TYPE_SETTING
operator|.
name|get
argument_list|(
name|settings
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
DECL|method|testInstallPluginTwice
specifier|public
name|void
name|testInstallPluginTwice
parameter_list|()
block|{
for|for
control|(
name|Class
argument_list|<
name|?
extends|extends
name|Plugin
argument_list|>
name|plugin
range|:
name|Arrays
operator|.
name|asList
argument_list|(
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
control|)
block|{
try|try
block|{
operator|new
name|PreBuiltTransportClient
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
name|plugin
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"exception expected"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|ex
parameter_list|)
block|{
name|assertTrue
argument_list|(
literal|"Expected message to start with [plugin already exists: ] but was instead ["
operator|+
name|ex
operator|.
name|getMessage
argument_list|()
operator|+
literal|"]"
argument_list|,
name|ex
operator|.
name|getMessage
argument_list|()
operator|.
name|startsWith
argument_list|(
literal|"plugin already exists: "
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

