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
name|Nullable
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
name|search
operator|.
name|lookup
operator|.
name|SearchLookup
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
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
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

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|equalTo
import|;
end_import

begin_class
DECL|class|ScriptSettingsTests
specifier|public
class|class
name|ScriptSettingsTests
extends|extends
name|ESTestCase
block|{
DECL|method|testDefaultLanguageIsPainless
specifier|public
name|void
name|testDefaultLanguageIsPainless
parameter_list|()
block|{
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
name|CustomScriptEngineService
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|ScriptContextRegistry
name|scriptContextRegistry
init|=
operator|new
name|ScriptContextRegistry
argument_list|(
name|Collections
operator|.
name|emptyList
argument_list|()
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
name|assertThat
argument_list|(
name|scriptSettings
operator|.
name|getDefaultScriptLanguageSetting
argument_list|()
operator|.
name|get
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"painless"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testCustomDefaultLanguage
specifier|public
name|void
name|testCustomDefaultLanguage
parameter_list|()
block|{
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
name|CustomScriptEngineService
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|ScriptContextRegistry
name|scriptContextRegistry
init|=
operator|new
name|ScriptContextRegistry
argument_list|(
name|Collections
operator|.
name|emptyList
argument_list|()
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
name|String
name|defaultLanguage
init|=
name|CustomScriptEngineService
operator|.
name|NAME
decl_stmt|;
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
literal|"script.default_lang"
argument_list|,
name|defaultLanguage
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|scriptSettings
operator|.
name|getDefaultScriptLanguageSetting
argument_list|()
operator|.
name|get
argument_list|(
name|settings
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|defaultLanguage
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testInvalidDefaultLanguage
specifier|public
name|void
name|testInvalidDefaultLanguage
parameter_list|()
block|{
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
name|CustomScriptEngineService
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|ScriptContextRegistry
name|scriptContextRegistry
init|=
operator|new
name|ScriptContextRegistry
argument_list|(
name|Collections
operator|.
name|emptyList
argument_list|()
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
literal|"script.default_lang"
argument_list|,
literal|"C++"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
try|try
block|{
name|scriptSettings
operator|.
name|getDefaultScriptLanguageSetting
argument_list|()
operator|.
name|get
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"should have seen unregistered default language"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
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
literal|"unregistered default language [C++]"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testSettingsAreProperlyPropogated
specifier|public
name|void
name|testSettingsAreProperlyPropogated
parameter_list|()
block|{
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
name|CustomScriptEngineService
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|ScriptContextRegistry
name|scriptContextRegistry
init|=
operator|new
name|ScriptContextRegistry
argument_list|(
name|Collections
operator|.
name|emptyList
argument_list|()
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
name|boolean
name|enabled
init|=
name|randomBoolean
argument_list|()
decl_stmt|;
name|Settings
name|s
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"script.inline"
argument_list|,
name|enabled
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
for|for
control|(
name|Iterator
argument_list|<
name|Setting
argument_list|<
name|Boolean
argument_list|>
argument_list|>
name|iter
init|=
name|scriptSettings
operator|.
name|getScriptLanguageSettings
argument_list|()
operator|.
name|iterator
argument_list|()
init|;
name|iter
operator|.
name|hasNext
argument_list|()
condition|;
control|)
block|{
name|Setting
argument_list|<
name|Boolean
argument_list|>
name|setting
init|=
name|iter
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|setting
operator|.
name|getKey
argument_list|()
operator|.
name|endsWith
argument_list|(
literal|".inline"
argument_list|)
condition|)
block|{
name|assertThat
argument_list|(
literal|"inline settings should have propagated"
argument_list|,
name|setting
operator|.
name|get
argument_list|(
name|s
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|enabled
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|setting
operator|.
name|getDefaultRaw
argument_list|(
name|s
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|Boolean
operator|.
name|toString
argument_list|(
name|enabled
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|class|CustomScriptEngineService
specifier|private
specifier|static
class|class
name|CustomScriptEngineService
implements|implements
name|ScriptEngineService
block|{
DECL|field|NAME
specifier|public
specifier|static
specifier|final
name|String
name|NAME
init|=
literal|"custom"
decl_stmt|;
annotation|@
name|Override
DECL|method|getType
specifier|public
name|String
name|getType
parameter_list|()
block|{
return|return
name|NAME
return|;
block|}
annotation|@
name|Override
DECL|method|getExtension
specifier|public
name|String
name|getExtension
parameter_list|()
block|{
return|return
name|NAME
return|;
block|}
annotation|@
name|Override
DECL|method|compile
specifier|public
name|Object
name|compile
parameter_list|(
name|String
name|scriptName
parameter_list|,
name|String
name|scriptSource
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|params
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|executable
specifier|public
name|ExecutableScript
name|executable
parameter_list|(
name|CompiledScript
name|compiledScript
parameter_list|,
annotation|@
name|Nullable
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|vars
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|search
specifier|public
name|SearchScript
name|search
parameter_list|(
name|CompiledScript
name|compiledScript
parameter_list|,
name|SearchLookup
name|lookup
parameter_list|,
annotation|@
name|Nullable
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|vars
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
block|{         }
block|}
block|}
end_class

end_unit

