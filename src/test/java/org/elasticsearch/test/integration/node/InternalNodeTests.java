begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.test.integration.node
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|integration
operator|.
name|node
package|;
end_package

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
name|component
operator|.
name|Lifecycle
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
name|LifecycleComponent
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
name|inject
operator|.
name|Singleton
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
name|node
operator|.
name|internal
operator|.
name|InternalNode
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
name|AbstractPlugin
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
name|integration
operator|.
name|AbstractNodesTests
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
name|util
operator|.
name|ArrayList
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
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|settings
operator|.
name|ImmutableSettings
operator|.
name|settingsBuilder
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
name|assertThat
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
name|is
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|InternalNodeTests
specifier|public
class|class
name|InternalNodeTests
extends|extends
name|AbstractNodesTests
block|{
annotation|@
name|Test
DECL|method|testDefaultPluginConfiguration
specifier|public
name|void
name|testDefaultPluginConfiguration
parameter_list|()
throws|throws
name|Exception
block|{
name|Settings
name|settings
init|=
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"plugin.types"
argument_list|,
name|TestPlugin
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|InternalNode
name|node
init|=
operator|(
name|InternalNode
operator|)
name|buildNode
argument_list|(
literal|"test"
argument_list|,
name|settings
argument_list|)
decl_stmt|;
name|TestService
name|service
init|=
name|node
operator|.
name|injector
argument_list|()
operator|.
name|getInstance
argument_list|(
name|TestService
operator|.
name|class
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|service
operator|.
name|state
operator|.
name|initialized
argument_list|()
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|node
operator|.
name|start
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|service
operator|.
name|state
operator|.
name|started
argument_list|()
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|node
operator|.
name|stop
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|service
operator|.
name|state
operator|.
name|stopped
argument_list|()
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|node
operator|.
name|close
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|service
operator|.
name|state
operator|.
name|closed
argument_list|()
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|class|TestPlugin
specifier|public
specifier|static
class|class
name|TestPlugin
extends|extends
name|AbstractPlugin
block|{
DECL|method|TestPlugin
specifier|public
name|TestPlugin
parameter_list|()
block|{         }
annotation|@
name|Override
DECL|method|name
specifier|public
name|String
name|name
parameter_list|()
block|{
return|return
literal|"test"
return|;
block|}
annotation|@
name|Override
DECL|method|description
specifier|public
name|String
name|description
parameter_list|()
block|{
return|return
literal|"test plugin"
return|;
block|}
annotation|@
name|Override
DECL|method|services
specifier|public
name|Collection
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|LifecycleComponent
argument_list|>
argument_list|>
name|services
parameter_list|()
block|{
name|Collection
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|LifecycleComponent
argument_list|>
argument_list|>
name|services
init|=
operator|new
name|ArrayList
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|LifecycleComponent
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|services
operator|.
name|add
argument_list|(
name|TestService
operator|.
name|class
argument_list|)
expr_stmt|;
return|return
name|services
return|;
block|}
block|}
annotation|@
name|Singleton
DECL|class|TestService
specifier|public
specifier|static
class|class
name|TestService
extends|extends
name|AbstractLifecycleComponent
argument_list|<
name|TestService
argument_list|>
block|{
DECL|field|state
specifier|private
name|Lifecycle
name|state
decl_stmt|;
annotation|@
name|Inject
DECL|method|TestService
specifier|public
name|TestService
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"initializing"
argument_list|)
expr_stmt|;
name|state
operator|=
operator|new
name|Lifecycle
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doStart
specifier|protected
name|void
name|doStart
parameter_list|()
throws|throws
name|ElasticSearchException
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"starting"
argument_list|)
expr_stmt|;
name|state
operator|.
name|moveToStarted
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doStop
specifier|protected
name|void
name|doStop
parameter_list|()
throws|throws
name|ElasticSearchException
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"stopping"
argument_list|)
expr_stmt|;
name|state
operator|.
name|moveToStopped
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doClose
specifier|protected
name|void
name|doClose
parameter_list|()
throws|throws
name|ElasticSearchException
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"closing"
argument_list|)
expr_stmt|;
name|state
operator|.
name|moveToClosed
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

