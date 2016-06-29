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
name|env
operator|.
name|Environment
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
name|ESTestCase
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
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
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
name|Set
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
name|containsString
import|;
end_import

begin_class
DECL|class|ScriptContextTests
specifier|public
class|class
name|ScriptContextTests
extends|extends
name|ESTestCase
block|{
DECL|field|PLUGIN_NAME
specifier|private
specifier|static
specifier|final
name|String
name|PLUGIN_NAME
init|=
literal|"testplugin"
decl_stmt|;
DECL|method|makeScriptService
name|ScriptService
name|makeScriptService
parameter_list|()
throws|throws
name|Exception
block|{
name|Settings
name|settings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|Environment
operator|.
name|PATH_HOME_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
name|createTempDir
argument_list|()
argument_list|)
comment|// no file watching, so we don't need a ResourceWatcherService
operator|.
name|put
argument_list|(
name|ScriptService
operator|.
name|SCRIPT_AUTO_RELOAD_ENABLED_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
literal|"off"
argument_list|)
operator|.
name|put
argument_list|(
literal|"script."
operator|+
name|PLUGIN_NAME
operator|+
literal|"_custom_globally_disabled_op"
argument_list|,
literal|"false"
argument_list|)
operator|.
name|put
argument_list|(
literal|"script.engine."
operator|+
name|MockScriptEngine
operator|.
name|NAME
operator|+
literal|".inline."
operator|+
name|PLUGIN_NAME
operator|+
literal|"_custom_exp_disabled_op"
argument_list|,
literal|"false"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|ScriptEngineRegistry
name|scriptEngineRegistry
init|=
operator|new
name|ScriptEngineRegistry
argument_list|(
name|Collections
operator|.
name|singletonList
argument_list|(
operator|new
name|MockScriptEngine
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|ScriptContext
operator|.
name|Plugin
argument_list|>
name|customContexts
init|=
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|ScriptContext
operator|.
name|Plugin
argument_list|(
name|PLUGIN_NAME
argument_list|,
literal|"custom_op"
argument_list|)
argument_list|,
operator|new
name|ScriptContext
operator|.
name|Plugin
argument_list|(
name|PLUGIN_NAME
argument_list|,
literal|"custom_exp_disabled_op"
argument_list|)
argument_list|,
operator|new
name|ScriptContext
operator|.
name|Plugin
argument_list|(
name|PLUGIN_NAME
argument_list|,
literal|"custom_globally_disabled_op"
argument_list|)
argument_list|)
decl_stmt|;
name|ScriptContextRegistry
name|scriptContextRegistry
init|=
operator|new
name|ScriptContextRegistry
argument_list|(
name|customContexts
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
return|return
operator|new
name|ScriptService
argument_list|(
name|settings
argument_list|,
operator|new
name|Environment
argument_list|(
name|settings
argument_list|)
argument_list|,
literal|null
argument_list|,
name|scriptEngineRegistry
argument_list|,
name|scriptContextRegistry
argument_list|,
name|scriptSettings
argument_list|)
return|;
block|}
DECL|method|testCustomGlobalScriptContextSettings
specifier|public
name|void
name|testCustomGlobalScriptContextSettings
parameter_list|()
throws|throws
name|Exception
block|{
name|ScriptService
name|scriptService
init|=
name|makeScriptService
argument_list|()
decl_stmt|;
for|for
control|(
name|ScriptService
operator|.
name|ScriptType
name|scriptType
range|:
name|ScriptService
operator|.
name|ScriptType
operator|.
name|values
argument_list|()
control|)
block|{
try|try
block|{
name|Script
name|script
init|=
operator|new
name|Script
argument_list|(
literal|"1"
argument_list|,
name|scriptType
argument_list|,
name|MockScriptEngine
operator|.
name|NAME
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|scriptService
operator|.
name|compile
argument_list|(
name|script
argument_list|,
operator|new
name|ScriptContext
operator|.
name|Plugin
argument_list|(
name|PLUGIN_NAME
argument_list|,
literal|"custom_globally_disabled_op"
argument_list|)
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"script compilation should have been rejected"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalStateException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"scripts of type ["
operator|+
name|scriptType
operator|+
literal|"], operation ["
operator|+
name|PLUGIN_NAME
operator|+
literal|"_custom_globally_disabled_op] and lang ["
operator|+
name|MockScriptEngine
operator|.
name|NAME
operator|+
literal|"] are disabled"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|testCustomScriptContextSettings
specifier|public
name|void
name|testCustomScriptContextSettings
parameter_list|()
throws|throws
name|Exception
block|{
name|ScriptService
name|scriptService
init|=
name|makeScriptService
argument_list|()
decl_stmt|;
name|Script
name|script
init|=
operator|new
name|Script
argument_list|(
literal|"1"
argument_list|,
name|ScriptService
operator|.
name|ScriptType
operator|.
name|INLINE
argument_list|,
name|MockScriptEngine
operator|.
name|NAME
argument_list|,
literal|null
argument_list|)
decl_stmt|;
try|try
block|{
name|scriptService
operator|.
name|compile
argument_list|(
name|script
argument_list|,
operator|new
name|ScriptContext
operator|.
name|Plugin
argument_list|(
name|PLUGIN_NAME
argument_list|,
literal|"custom_exp_disabled_op"
argument_list|)
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"script compilation should have been rejected"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalStateException
name|e
parameter_list|)
block|{
name|assertTrue
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"scripts of type [inline], operation ["
operator|+
name|PLUGIN_NAME
operator|+
literal|"_custom_exp_disabled_op] and lang ["
operator|+
name|MockScriptEngine
operator|.
name|NAME
operator|+
literal|"] are disabled"
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// still works for other script contexts
name|assertNotNull
argument_list|(
name|scriptService
operator|.
name|compile
argument_list|(
name|script
argument_list|,
name|ScriptContext
operator|.
name|Standard
operator|.
name|AGGS
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|scriptService
operator|.
name|compile
argument_list|(
name|script
argument_list|,
name|ScriptContext
operator|.
name|Standard
operator|.
name|SEARCH
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|scriptService
operator|.
name|compile
argument_list|(
name|script
argument_list|,
operator|new
name|ScriptContext
operator|.
name|Plugin
argument_list|(
name|PLUGIN_NAME
argument_list|,
literal|"custom_op"
argument_list|)
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testUnknownPluginScriptContext
specifier|public
name|void
name|testUnknownPluginScriptContext
parameter_list|()
throws|throws
name|Exception
block|{
name|ScriptService
name|scriptService
init|=
name|makeScriptService
argument_list|()
decl_stmt|;
for|for
control|(
name|ScriptService
operator|.
name|ScriptType
name|scriptType
range|:
name|ScriptService
operator|.
name|ScriptType
operator|.
name|values
argument_list|()
control|)
block|{
try|try
block|{
name|Script
name|script
init|=
operator|new
name|Script
argument_list|(
literal|"1"
argument_list|,
name|scriptType
argument_list|,
name|MockScriptEngine
operator|.
name|NAME
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|scriptService
operator|.
name|compile
argument_list|(
name|script
argument_list|,
operator|new
name|ScriptContext
operator|.
name|Plugin
argument_list|(
name|PLUGIN_NAME
argument_list|,
literal|"unknown"
argument_list|)
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"script compilation should have been rejected"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
name|assertTrue
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"script context ["
operator|+
name|PLUGIN_NAME
operator|+
literal|"_unknown] not supported"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|testUnknownCustomScriptContext
specifier|public
name|void
name|testUnknownCustomScriptContext
parameter_list|()
throws|throws
name|Exception
block|{
name|ScriptContext
name|context
init|=
operator|new
name|ScriptContext
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|String
name|getKey
parameter_list|()
block|{
return|return
literal|"test"
return|;
block|}
block|}
decl_stmt|;
name|ScriptService
name|scriptService
init|=
name|makeScriptService
argument_list|()
decl_stmt|;
for|for
control|(
name|ScriptService
operator|.
name|ScriptType
name|scriptType
range|:
name|ScriptService
operator|.
name|ScriptType
operator|.
name|values
argument_list|()
control|)
block|{
try|try
block|{
name|Script
name|script
init|=
operator|new
name|Script
argument_list|(
literal|"1"
argument_list|,
name|scriptType
argument_list|,
name|MockScriptEngine
operator|.
name|NAME
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|scriptService
operator|.
name|compile
argument_list|(
name|script
argument_list|,
name|context
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"script compilation should have been rejected"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
name|assertTrue
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"script context [test] not supported"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

