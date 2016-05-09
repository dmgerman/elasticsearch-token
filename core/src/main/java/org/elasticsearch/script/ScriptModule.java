begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.script
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|script
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
name|multibindings
operator|.
name|MapBinder
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
name|multibindings
operator|.
name|Multibinder
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
name|SettingsModule
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
name|ScriptMode
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
name|HashMap
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
name|Objects
import|;
end_import

begin_comment
comment|/**  * An {@link org.elasticsearch.common.inject.Module} which manages {@link ScriptEngineService}s, as well  * as named script  */
end_comment

begin_class
DECL|class|ScriptModule
specifier|public
class|class
name|ScriptModule
extends|extends
name|AbstractModule
block|{
DECL|field|scriptEngineRegistrations
specifier|private
specifier|final
name|List
argument_list|<
name|ScriptEngineRegistry
operator|.
name|ScriptEngineRegistration
argument_list|>
name|scriptEngineRegistrations
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
block|{
name|scriptEngineRegistrations
operator|.
name|add
argument_list|(
operator|new
name|ScriptEngineRegistry
operator|.
name|ScriptEngineRegistration
argument_list|(
name|NativeScriptEngineService
operator|.
name|class
argument_list|,
name|NativeScriptEngineService
operator|.
name|TYPES
argument_list|,
name|ScriptMode
operator|.
name|ON
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|field|scripts
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Class
argument_list|<
name|?
extends|extends
name|NativeScriptFactory
argument_list|>
argument_list|>
name|scripts
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
DECL|field|customScriptContexts
specifier|private
specifier|final
name|List
argument_list|<
name|ScriptContext
operator|.
name|Plugin
argument_list|>
name|customScriptContexts
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
DECL|method|addScriptEngine
specifier|public
name|void
name|addScriptEngine
parameter_list|(
name|ScriptEngineRegistry
operator|.
name|ScriptEngineRegistration
name|scriptEngineRegistration
parameter_list|)
block|{
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|scriptEngineRegistration
argument_list|)
expr_stmt|;
name|scriptEngineRegistrations
operator|.
name|add
argument_list|(
name|scriptEngineRegistration
argument_list|)
expr_stmt|;
block|}
DECL|method|registerScript
specifier|public
name|void
name|registerScript
parameter_list|(
name|String
name|name
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|NativeScriptFactory
argument_list|>
name|script
parameter_list|)
block|{
name|scripts
operator|.
name|put
argument_list|(
name|name
argument_list|,
name|script
argument_list|)
expr_stmt|;
block|}
comment|/**      * Registers a custom script context that can be used by plugins to categorize the different operations that they use scripts for.      * Fine-grained settings allow to enable/disable scripts per context.      */
DECL|method|registerScriptContext
specifier|public
name|void
name|registerScriptContext
parameter_list|(
name|ScriptContext
operator|.
name|Plugin
name|scriptContext
parameter_list|)
block|{
name|customScriptContexts
operator|.
name|add
argument_list|(
name|scriptContext
argument_list|)
expr_stmt|;
block|}
comment|/**      * This method is called after all modules have been processed but before we actually validate all settings. This allows the      * script extensions to add all their settings.      */
DECL|method|prepareSettings
specifier|public
name|void
name|prepareSettings
parameter_list|(
name|SettingsModule
name|settingsModule
parameter_list|)
block|{
name|ScriptContextRegistry
name|scriptContextRegistry
init|=
operator|new
name|ScriptContextRegistry
argument_list|(
name|customScriptContexts
argument_list|)
decl_stmt|;
name|ScriptEngineRegistry
name|scriptEngineRegistry
init|=
operator|new
name|ScriptEngineRegistry
argument_list|(
name|scriptEngineRegistrations
argument_list|)
decl_stmt|;
name|ScriptSettings
name|scriptSettings
init|=
operator|new
name|ScriptSettings
argument_list|(
name|scriptEngineRegistry
argument_list|,
name|scriptContextRegistry
argument_list|)
decl_stmt|;
name|scriptSettings
operator|.
name|getScriptTypeSettings
argument_list|()
operator|.
name|forEach
argument_list|(
name|settingsModule
operator|::
name|registerSetting
argument_list|)
expr_stmt|;
name|scriptSettings
operator|.
name|getScriptContextSettings
argument_list|()
operator|.
name|forEach
argument_list|(
name|settingsModule
operator|::
name|registerSetting
argument_list|)
expr_stmt|;
name|scriptSettings
operator|.
name|getScriptLanguageSettings
argument_list|()
operator|.
name|forEach
argument_list|(
name|settingsModule
operator|::
name|registerSetting
argument_list|)
expr_stmt|;
name|settingsModule
operator|.
name|registerSetting
argument_list|(
name|scriptSettings
operator|.
name|getDefaultScriptLanguageSetting
argument_list|()
argument_list|)
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
name|MapBinder
argument_list|<
name|String
argument_list|,
name|NativeScriptFactory
argument_list|>
name|scriptsBinder
init|=
name|MapBinder
operator|.
name|newMapBinder
argument_list|(
name|binder
argument_list|()
argument_list|,
name|String
operator|.
name|class
argument_list|,
name|NativeScriptFactory
operator|.
name|class
argument_list|)
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Class
argument_list|<
name|?
extends|extends
name|NativeScriptFactory
argument_list|>
argument_list|>
name|entry
range|:
name|scripts
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|scriptsBinder
operator|.
name|addBinding
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
operator|.
name|to
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
block|}
name|Multibinder
argument_list|<
name|ScriptEngineService
argument_list|>
name|multibinder
init|=
name|Multibinder
operator|.
name|newSetBinder
argument_list|(
name|binder
argument_list|()
argument_list|,
name|ScriptEngineService
operator|.
name|class
argument_list|)
decl_stmt|;
name|multibinder
operator|.
name|addBinding
argument_list|()
operator|.
name|to
argument_list|(
name|NativeScriptEngineService
operator|.
name|class
argument_list|)
expr_stmt|;
for|for
control|(
name|ScriptEngineRegistry
operator|.
name|ScriptEngineRegistration
name|scriptEngineRegistration
range|:
name|scriptEngineRegistrations
control|)
block|{
name|multibinder
operator|.
name|addBinding
argument_list|()
operator|.
name|to
argument_list|(
name|scriptEngineRegistration
operator|.
name|getScriptEngineService
argument_list|()
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
block|}
name|ScriptContextRegistry
name|scriptContextRegistry
init|=
operator|new
name|ScriptContextRegistry
argument_list|(
name|customScriptContexts
argument_list|)
decl_stmt|;
name|ScriptEngineRegistry
name|scriptEngineRegistry
init|=
operator|new
name|ScriptEngineRegistry
argument_list|(
name|scriptEngineRegistrations
argument_list|)
decl_stmt|;
name|ScriptSettings
name|scriptSettings
init|=
operator|new
name|ScriptSettings
argument_list|(
name|scriptEngineRegistry
argument_list|,
name|scriptContextRegistry
argument_list|)
decl_stmt|;
name|bind
argument_list|(
name|ScriptContextRegistry
operator|.
name|class
argument_list|)
operator|.
name|toInstance
argument_list|(
name|scriptContextRegistry
argument_list|)
expr_stmt|;
name|bind
argument_list|(
name|ScriptEngineRegistry
operator|.
name|class
argument_list|)
operator|.
name|toInstance
argument_list|(
name|scriptEngineRegistry
argument_list|)
expr_stmt|;
name|bind
argument_list|(
name|ScriptSettings
operator|.
name|class
argument_list|)
operator|.
name|toInstance
argument_list|(
name|scriptSettings
argument_list|)
expr_stmt|;
name|bind
argument_list|(
name|ScriptService
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

