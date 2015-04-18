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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableSet
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
name|LuceneTestCase
operator|.
name|Slow
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchIllegalArgumentException
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
name|ImmutableSettings
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
name|AbstractPlugin
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
name|expression
operator|.
name|ExpressionScriptEngineService
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
name|groovy
operator|.
name|GroovyScriptEngineService
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
name|MustacheScriptEngineService
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
name|ElasticsearchIntegrationTest
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
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|CoreMatchers
operator|.
name|containsString
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|CoreMatchers
operator|.
name|notNullValue
import|;
end_import

begin_class
annotation|@
name|Slow
DECL|class|CustomScriptContextTests
specifier|public
class|class
name|CustomScriptContextTests
extends|extends
name|ElasticsearchIntegrationTest
block|{
DECL|field|LANG_SET
specifier|private
specifier|static
specifier|final
name|ImmutableSet
argument_list|<
name|String
argument_list|>
name|LANG_SET
init|=
name|ImmutableSet
operator|.
name|of
argument_list|(
name|GroovyScriptEngineService
operator|.
name|NAME
argument_list|,
name|MustacheScriptEngineService
operator|.
name|NAME
argument_list|,
name|ExpressionScriptEngineService
operator|.
name|NAME
argument_list|)
decl_stmt|;
DECL|field|PLUGIN_NAME
specifier|private
specifier|static
specifier|final
name|String
name|PLUGIN_NAME
init|=
literal|"testplugin"
decl_stmt|;
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
name|ImmutableSettings
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
literal|"plugin.types"
argument_list|,
name|CustomScriptContextPlugin
operator|.
name|class
operator|.
name|getName
argument_list|()
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
literal|"off"
argument_list|)
operator|.
name|put
argument_list|(
literal|"script.engine.expression.inline."
operator|+
name|PLUGIN_NAME
operator|+
literal|"_custom_exp_disabled_op"
argument_list|,
literal|"off"
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
annotation|@
name|Test
DECL|method|testCustomScriptContextsSettings
specifier|public
name|void
name|testCustomScriptContextsSettings
parameter_list|()
block|{
name|ScriptService
name|scriptService
init|=
name|internalCluster
argument_list|()
operator|.
name|getInstance
argument_list|(
name|ScriptService
operator|.
name|class
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|lang
range|:
name|LANG_SET
control|)
block|{
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
name|scriptService
operator|.
name|compile
argument_list|(
name|lang
argument_list|,
literal|"test"
argument_list|,
name|scriptType
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
expr_stmt|;
name|fail
argument_list|(
literal|"script compilation should have been rejected"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ScriptException
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
name|lang
operator|+
literal|"] are disabled"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
try|try
block|{
name|scriptService
operator|.
name|compile
argument_list|(
literal|"expression"
argument_list|,
literal|"1"
argument_list|,
name|ScriptService
operator|.
name|ScriptType
operator|.
name|INLINE
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
name|ScriptException
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
literal|"scripts of type [inline], operation ["
operator|+
name|PLUGIN_NAME
operator|+
literal|"_custom_exp_disabled_op] and lang [expression] are disabled"
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|CompiledScript
name|compiledScript
init|=
name|scriptService
operator|.
name|compile
argument_list|(
literal|"expression"
argument_list|,
literal|"1"
argument_list|,
name|ScriptService
operator|.
name|ScriptType
operator|.
name|INLINE
argument_list|,
name|randomFrom
argument_list|(
name|ScriptContext
operator|.
name|Standard
operator|.
name|values
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|compiledScript
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|compiledScript
operator|=
name|scriptService
operator|.
name|compile
argument_list|(
literal|"mustache"
argument_list|,
literal|"1"
argument_list|,
name|ScriptService
operator|.
name|ScriptType
operator|.
name|INLINE
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
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|compiledScript
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|lang
range|:
name|LANG_SET
control|)
block|{
name|compiledScript
operator|=
name|scriptService
operator|.
name|compile
argument_list|(
name|lang
argument_list|,
literal|"1"
argument_list|,
name|ScriptService
operator|.
name|ScriptType
operator|.
name|INLINE
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
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|compiledScript
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
DECL|method|testCompileNonRegisteredPluginContext
specifier|public
name|void
name|testCompileNonRegisteredPluginContext
parameter_list|()
block|{
name|ScriptService
name|scriptService
init|=
name|internalCluster
argument_list|()
operator|.
name|getInstance
argument_list|(
name|ScriptService
operator|.
name|class
argument_list|)
decl_stmt|;
try|try
block|{
name|scriptService
operator|.
name|compile
argument_list|(
name|randomFrom
argument_list|(
name|LANG_SET
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
name|LANG_SET
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|)
argument_list|,
literal|"test"
argument_list|,
name|randomFrom
argument_list|(
name|ScriptService
operator|.
name|ScriptType
operator|.
name|values
argument_list|()
argument_list|)
argument_list|,
operator|new
name|ScriptContext
operator|.
name|Plugin
argument_list|(
literal|"test"
argument_list|,
literal|"unknown"
argument_list|)
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
name|ElasticsearchIllegalArgumentException
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
literal|"script context [test_unknown] not supported"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
DECL|method|testCompileNonRegisteredScriptContext
specifier|public
name|void
name|testCompileNonRegisteredScriptContext
parameter_list|()
block|{
name|ScriptService
name|scriptService
init|=
name|internalCluster
argument_list|()
operator|.
name|getInstance
argument_list|(
name|ScriptService
operator|.
name|class
argument_list|)
decl_stmt|;
try|try
block|{
name|scriptService
operator|.
name|compile
argument_list|(
name|randomFrom
argument_list|(
name|LANG_SET
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
name|LANG_SET
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|)
argument_list|,
literal|"test"
argument_list|,
name|randomFrom
argument_list|(
name|ScriptService
operator|.
name|ScriptType
operator|.
name|values
argument_list|()
argument_list|)
argument_list|,
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
name|ElasticsearchIllegalArgumentException
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
literal|"script context [test] not supported"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|CustomScriptContextPlugin
specifier|public
specifier|static
class|class
name|CustomScriptContextPlugin
extends|extends
name|AbstractPlugin
block|{
annotation|@
name|Override
DECL|method|name
specifier|public
name|String
name|name
parameter_list|()
block|{
return|return
literal|"custom_script_context_plugin"
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
literal|"Custom script context plugin"
return|;
block|}
annotation|@
name|Override
DECL|method|processModule
specifier|public
name|void
name|processModule
parameter_list|(
name|Module
name|module
parameter_list|)
block|{
if|if
condition|(
name|module
operator|instanceof
name|ScriptModule
condition|)
block|{
name|ScriptModule
name|scriptModule
init|=
operator|(
name|ScriptModule
operator|)
name|module
decl_stmt|;
name|scriptModule
operator|.
name|registerScriptContext
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
argument_list|)
expr_stmt|;
name|scriptModule
operator|.
name|registerScriptContext
argument_list|(
operator|new
name|ScriptContext
operator|.
name|Plugin
argument_list|(
name|PLUGIN_NAME
argument_list|,
literal|"custom_exp_disabled_op"
argument_list|)
argument_list|)
expr_stmt|;
name|scriptModule
operator|.
name|registerScriptContext
argument_list|(
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
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

