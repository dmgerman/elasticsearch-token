begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
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
name|ClusterModule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|settings
operator|.
name|Validator
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
name|AbstractModule
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
name|Binder
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
name|Module
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
name|test
operator|.
name|ESIntegTestCase
operator|.
name|ClusterScope
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
name|function
operator|.
name|Consumer
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|ESIntegTestCase
operator|.
name|Scope
operator|.
name|SUITE
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|hamcrest
operator|.
name|ElasticsearchAssertions
operator|.
name|assertAcked
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
name|nullValue
import|;
end_import

begin_class
annotation|@
name|ClusterScope
argument_list|(
name|scope
operator|=
name|SUITE
argument_list|,
name|numDataNodes
operator|=
literal|1
argument_list|,
name|transportClientRatio
operator|=
literal|0.0
argument_list|)
DECL|class|SettingsListenerIT
specifier|public
class|class
name|SettingsListenerIT
extends|extends
name|ESIntegTestCase
block|{
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
name|SettingsListenerPlugin
operator|.
name|class
argument_list|)
return|;
block|}
DECL|class|SettingsListenerPlugin
specifier|public
specifier|static
class|class
name|SettingsListenerPlugin
extends|extends
name|Plugin
block|{
DECL|field|service
specifier|private
specifier|final
name|SettingsTestingService
name|service
init|=
operator|new
name|SettingsTestingService
argument_list|()
decl_stmt|;
comment|/**          * The name of the plugin.          */
annotation|@
name|Override
DECL|method|name
specifier|public
name|String
name|name
parameter_list|()
block|{
return|return
literal|"settings-listener"
return|;
block|}
comment|/**          * The description of the plugin.          */
annotation|@
name|Override
DECL|method|description
specifier|public
name|String
name|description
parameter_list|()
block|{
return|return
literal|"Settings Listenern Plugin"
return|;
block|}
DECL|method|onModule
specifier|public
name|void
name|onModule
parameter_list|(
name|ClusterModule
name|clusterModule
parameter_list|)
block|{
name|clusterModule
operator|.
name|registerIndexDynamicSetting
argument_list|(
literal|"index.test.new.setting"
argument_list|,
name|Validator
operator|.
name|INTEGER
argument_list|)
expr_stmt|;
block|}
DECL|method|onModule
specifier|public
name|void
name|onModule
parameter_list|(
name|IndexModule
name|module
parameter_list|)
block|{
if|if
condition|(
name|module
operator|.
name|getIndex
argument_list|()
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
literal|"test"
argument_list|)
condition|)
block|{
comment|// only for the test index
name|module
operator|.
name|addIndexSettingsListener
argument_list|(
name|service
argument_list|)
expr_stmt|;
name|service
operator|.
name|accept
argument_list|(
name|module
operator|.
name|getSettings
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|nodeModules
specifier|public
name|Collection
argument_list|<
name|Module
argument_list|>
name|nodeModules
parameter_list|()
block|{
return|return
name|Collections
operator|.
expr|<
name|Module
operator|>
name|singletonList
argument_list|(
operator|new
name|SettingsListenerModule
argument_list|(
name|service
argument_list|)
argument_list|)
return|;
block|}
block|}
DECL|class|SettingsListenerModule
specifier|public
specifier|static
class|class
name|SettingsListenerModule
extends|extends
name|AbstractModule
block|{
DECL|field|service
specifier|private
specifier|final
name|SettingsTestingService
name|service
decl_stmt|;
DECL|method|SettingsListenerModule
specifier|public
name|SettingsListenerModule
parameter_list|(
name|SettingsTestingService
name|service
parameter_list|)
block|{
name|this
operator|.
name|service
operator|=
name|service
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|configure
specifier|protected
name|void
name|configure
parameter_list|()
block|{
name|bind
argument_list|(
name|SettingsTestingService
operator|.
name|class
argument_list|)
operator|.
name|toInstance
argument_list|(
name|service
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|SettingsTestingService
specifier|public
specifier|static
class|class
name|SettingsTestingService
implements|implements
name|Consumer
argument_list|<
name|Settings
argument_list|>
block|{
DECL|field|value
specifier|public
specifier|volatile
name|int
name|value
decl_stmt|;
annotation|@
name|Override
DECL|method|accept
specifier|public
name|void
name|accept
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|value
operator|=
name|settings
operator|.
name|getAsInt
argument_list|(
literal|"index.test.new.setting"
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testListener
specifier|public
name|void
name|testListener
parameter_list|()
block|{
name|assertAcked
argument_list|(
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareCreate
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setSettings
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"index.test.new.setting"
argument_list|,
literal|21
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|SettingsTestingService
name|instance
range|:
name|internalCluster
argument_list|()
operator|.
name|getInstances
argument_list|(
name|SettingsTestingService
operator|.
name|class
argument_list|)
control|)
block|{
name|assertEquals
argument_list|(
literal|21
argument_list|,
name|instance
operator|.
name|value
argument_list|)
expr_stmt|;
block|}
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareUpdateSettings
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setSettings
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"index.test.new.setting"
argument_list|,
literal|42
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
for|for
control|(
name|SettingsTestingService
name|instance
range|:
name|internalCluster
argument_list|()
operator|.
name|getInstances
argument_list|(
name|SettingsTestingService
operator|.
name|class
argument_list|)
control|)
block|{
name|assertEquals
argument_list|(
literal|42
argument_list|,
name|instance
operator|.
name|value
argument_list|)
expr_stmt|;
block|}
name|assertAcked
argument_list|(
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareCreate
argument_list|(
literal|"other"
argument_list|)
operator|.
name|setSettings
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"index.test.new.setting"
argument_list|,
literal|21
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|SettingsTestingService
name|instance
range|:
name|internalCluster
argument_list|()
operator|.
name|getInstances
argument_list|(
name|SettingsTestingService
operator|.
name|class
argument_list|)
control|)
block|{
name|assertEquals
argument_list|(
literal|42
argument_list|,
name|instance
operator|.
name|value
argument_list|)
expr_stmt|;
block|}
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareUpdateSettings
argument_list|(
literal|"other"
argument_list|)
operator|.
name|setSettings
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"index.test.new.setting"
argument_list|,
literal|84
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
for|for
control|(
name|SettingsTestingService
name|instance
range|:
name|internalCluster
argument_list|()
operator|.
name|getInstances
argument_list|(
name|SettingsTestingService
operator|.
name|class
argument_list|)
control|)
block|{
name|assertEquals
argument_list|(
literal|42
argument_list|,
name|instance
operator|.
name|value
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

