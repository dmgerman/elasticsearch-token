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
name|collect
operator|.
name|Tuple
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
name|Setting
operator|.
name|Property
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
name|Collections
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
name|function
operator|.
name|Function
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|Collectors
import|;
end_import

begin_class
DECL|class|ScriptSettings
specifier|public
class|class
name|ScriptSettings
block|{
DECL|field|DEFAULT_LANG
specifier|public
specifier|final
specifier|static
name|String
name|DEFAULT_LANG
init|=
literal|"groovy"
decl_stmt|;
DECL|field|SCRIPT_TYPE_SETTING_MAP
specifier|private
specifier|final
specifier|static
name|Map
argument_list|<
name|ScriptService
operator|.
name|ScriptType
argument_list|,
name|Setting
argument_list|<
name|ScriptMode
argument_list|>
argument_list|>
name|SCRIPT_TYPE_SETTING_MAP
decl_stmt|;
static|static
block|{
name|Map
argument_list|<
name|ScriptService
operator|.
name|ScriptType
argument_list|,
name|Setting
argument_list|<
name|ScriptMode
argument_list|>
argument_list|>
name|scriptTypeSettingMap
init|=
operator|new
name|HashMap
argument_list|<>
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
name|scriptTypeSettingMap
operator|.
name|put
argument_list|(
name|scriptType
argument_list|,
operator|new
name|Setting
argument_list|<>
argument_list|(
name|ScriptModes
operator|.
name|sourceKey
argument_list|(
name|scriptType
argument_list|)
argument_list|,
name|scriptType
operator|.
name|getDefaultScriptMode
argument_list|()
operator|.
name|getMode
argument_list|()
argument_list|,
name|ScriptMode
operator|::
name|parse
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|SCRIPT_TYPE_SETTING_MAP
operator|=
name|Collections
operator|.
name|unmodifiableMap
argument_list|(
name|scriptTypeSettingMap
argument_list|)
expr_stmt|;
block|}
DECL|field|scriptContextSettingMap
specifier|private
specifier|final
name|Map
argument_list|<
name|ScriptContext
argument_list|,
name|Setting
argument_list|<
name|ScriptMode
argument_list|>
argument_list|>
name|scriptContextSettingMap
decl_stmt|;
DECL|field|scriptLanguageSettings
specifier|private
specifier|final
name|List
argument_list|<
name|Setting
argument_list|<
name|ScriptMode
argument_list|>
argument_list|>
name|scriptLanguageSettings
decl_stmt|;
DECL|field|defaultScriptLanguageSetting
specifier|private
specifier|final
name|Setting
argument_list|<
name|String
argument_list|>
name|defaultScriptLanguageSetting
decl_stmt|;
DECL|method|ScriptSettings
specifier|public
name|ScriptSettings
parameter_list|(
name|ScriptEngineRegistry
name|scriptEngineRegistry
parameter_list|,
name|ScriptContextRegistry
name|scriptContextRegistry
parameter_list|)
block|{
name|Map
argument_list|<
name|ScriptContext
argument_list|,
name|Setting
argument_list|<
name|ScriptMode
argument_list|>
argument_list|>
name|scriptContextSettingMap
init|=
name|contextSettings
argument_list|(
name|scriptContextRegistry
argument_list|)
decl_stmt|;
name|this
operator|.
name|scriptContextSettingMap
operator|=
name|Collections
operator|.
name|unmodifiableMap
argument_list|(
name|scriptContextSettingMap
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Setting
argument_list|<
name|ScriptMode
argument_list|>
argument_list|>
name|scriptLanguageSettings
init|=
name|languageSettings
argument_list|(
name|SCRIPT_TYPE_SETTING_MAP
argument_list|,
name|scriptContextSettingMap
argument_list|,
name|scriptEngineRegistry
argument_list|,
name|scriptContextRegistry
argument_list|)
decl_stmt|;
name|this
operator|.
name|scriptLanguageSettings
operator|=
name|Collections
operator|.
name|unmodifiableList
argument_list|(
name|scriptLanguageSettings
argument_list|)
expr_stmt|;
name|this
operator|.
name|defaultScriptLanguageSetting
operator|=
operator|new
name|Setting
argument_list|<>
argument_list|(
literal|"script.default_lang"
argument_list|,
name|DEFAULT_LANG
argument_list|,
name|setting
lambda|->
block|{
if|if
condition|(
operator|!
literal|"groovy"
operator|.
name|equals
argument_list|(
name|setting
argument_list|)
operator|&&
operator|!
name|scriptEngineRegistry
operator|.
name|getRegisteredLanguages
argument_list|()
operator|.
name|containsKey
argument_list|(
name|setting
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"unregistered default language ["
operator|+
name|setting
operator|+
literal|"]"
argument_list|)
throw|;
block|}
return|return
name|setting
return|;
block|}
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|)
expr_stmt|;
block|}
DECL|method|contextSettings
specifier|private
specifier|static
name|Map
argument_list|<
name|ScriptContext
argument_list|,
name|Setting
argument_list|<
name|ScriptMode
argument_list|>
argument_list|>
name|contextSettings
parameter_list|(
name|ScriptContextRegistry
name|scriptContextRegistry
parameter_list|)
block|{
name|Map
argument_list|<
name|ScriptContext
argument_list|,
name|Setting
argument_list|<
name|ScriptMode
argument_list|>
argument_list|>
name|scriptContextSettingMap
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|ScriptContext
name|scriptContext
range|:
name|scriptContextRegistry
operator|.
name|scriptContexts
argument_list|()
control|)
block|{
name|scriptContextSettingMap
operator|.
name|put
argument_list|(
name|scriptContext
argument_list|,
operator|new
name|Setting
argument_list|<>
argument_list|(
name|ScriptModes
operator|.
name|operationKey
argument_list|(
name|scriptContext
argument_list|)
argument_list|,
name|ScriptMode
operator|.
name|OFF
operator|.
name|getMode
argument_list|()
argument_list|,
name|ScriptMode
operator|::
name|parse
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|scriptContextSettingMap
return|;
block|}
DECL|method|languageSettings
specifier|private
specifier|static
name|List
argument_list|<
name|Setting
argument_list|<
name|ScriptMode
argument_list|>
argument_list|>
name|languageSettings
parameter_list|(
name|Map
argument_list|<
name|ScriptService
operator|.
name|ScriptType
argument_list|,
name|Setting
argument_list|<
name|ScriptMode
argument_list|>
argument_list|>
name|scriptTypeSettingMap
parameter_list|,
name|Map
argument_list|<
name|ScriptContext
argument_list|,
name|Setting
argument_list|<
name|ScriptMode
argument_list|>
argument_list|>
name|scriptContextSettingMap
parameter_list|,
name|ScriptEngineRegistry
name|scriptEngineRegistry
parameter_list|,
name|ScriptContextRegistry
name|scriptContextRegistry
parameter_list|)
block|{
name|List
argument_list|<
name|Setting
argument_list|<
name|ScriptMode
argument_list|>
argument_list|>
name|scriptModeSettings
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|Class
argument_list|<
name|?
extends|extends
name|ScriptEngineService
argument_list|>
name|scriptEngineService
range|:
name|scriptEngineRegistry
operator|.
name|getRegisteredScriptEngineServices
argument_list|()
control|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|languages
init|=
name|scriptEngineRegistry
operator|.
name|getLanguages
argument_list|(
name|scriptEngineService
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|language
range|:
name|languages
control|)
block|{
if|if
condition|(
name|NativeScriptEngineService
operator|.
name|TYPES
operator|.
name|contains
argument_list|(
name|language
argument_list|)
condition|)
block|{
comment|// native scripts are always enabled, and their settings can not be changed
continue|continue;
block|}
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
for|for
control|(
name|ScriptContext
name|scriptContext
range|:
name|scriptContextRegistry
operator|.
name|scriptContexts
argument_list|()
control|)
block|{
name|Function
argument_list|<
name|Settings
argument_list|,
name|String
argument_list|>
name|defaultSetting
init|=
name|settings
lambda|->
block|{
comment|// fallback logic for script mode settings
comment|// the first fallback is other types registered by the same script engine service
comment|// e.g., "py.inline.aggs" is in the settings but a script with lang "python" is executed
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|languageSettings
init|=
name|languages
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|lang
lambda|->
name|Tuple
operator|.
name|tuple
argument_list|(
name|lang
argument_list|,
name|settings
operator|.
name|get
argument_list|(
name|ScriptModes
operator|.
name|getKey
argument_list|(
name|lang
argument_list|,
name|scriptType
argument_list|,
name|scriptContext
argument_list|)
argument_list|)
argument_list|)
argument_list|)
operator|.
name|filter
argument_list|(
name|tuple
lambda|->
name|tuple
operator|.
name|v2
argument_list|()
operator|!=
literal|null
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|groupingBy
argument_list|(
name|Tuple
operator|::
name|v2
argument_list|,
name|Collectors
operator|.
name|mapping
argument_list|(
name|Tuple
operator|::
name|v1
argument_list|,
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|languageSettings
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
if|if
condition|(
name|languageSettings
operator|.
name|size
argument_list|()
operator|>
literal|1
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"conflicting settings ["
operator|+
name|languageSettings
operator|.
name|toString
argument_list|()
operator|+
literal|"] for language ["
operator|+
name|language
operator|+
literal|"]"
argument_list|)
throw|;
block|}
return|return
name|languageSettings
operator|.
name|keySet
argument_list|()
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
return|;
block|}
comment|// the next fallback is global operation-based settings (e.g., "script.aggs: false")
name|Setting
argument_list|<
name|ScriptMode
argument_list|>
name|setting
init|=
name|scriptContextSettingMap
operator|.
name|get
argument_list|(
name|scriptContext
argument_list|)
decl_stmt|;
if|if
condition|(
name|setting
operator|.
name|exists
argument_list|(
name|settings
argument_list|)
condition|)
block|{
return|return
name|setting
operator|.
name|get
argument_list|(
name|settings
argument_list|)
operator|.
name|getMode
argument_list|()
return|;
block|}
comment|// the next fallback is global source-based settings (e.g., "script.inline: false")
name|Setting
argument_list|<
name|ScriptMode
argument_list|>
name|scriptTypeSetting
init|=
name|scriptTypeSettingMap
operator|.
name|get
argument_list|(
name|scriptType
argument_list|)
decl_stmt|;
if|if
condition|(
name|scriptTypeSetting
operator|.
name|exists
argument_list|(
name|settings
argument_list|)
condition|)
block|{
return|return
name|scriptTypeSetting
operator|.
name|get
argument_list|(
name|settings
argument_list|)
operator|.
name|getMode
argument_list|()
return|;
block|}
comment|// the final fallback is the default for the type
return|return
name|scriptType
operator|.
name|getDefaultScriptMode
argument_list|()
operator|.
name|toString
argument_list|()
return|;
block|}
decl_stmt|;
name|Setting
argument_list|<
name|ScriptMode
argument_list|>
name|setting
init|=
operator|new
name|Setting
argument_list|<>
argument_list|(
name|ScriptModes
operator|.
name|getKey
argument_list|(
name|language
argument_list|,
name|scriptType
argument_list|,
name|scriptContext
argument_list|)
argument_list|,
name|defaultSetting
argument_list|,
name|ScriptMode
operator|::
name|parse
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|)
decl_stmt|;
name|scriptModeSettings
operator|.
name|add
argument_list|(
name|setting
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
return|return
name|scriptModeSettings
return|;
block|}
DECL|method|getScriptTypeSettings
specifier|public
name|Iterable
argument_list|<
name|Setting
argument_list|<
name|ScriptMode
argument_list|>
argument_list|>
name|getScriptTypeSettings
parameter_list|()
block|{
return|return
name|Collections
operator|.
name|unmodifiableCollection
argument_list|(
name|SCRIPT_TYPE_SETTING_MAP
operator|.
name|values
argument_list|()
argument_list|)
return|;
block|}
DECL|method|getScriptContextSettings
specifier|public
name|Iterable
argument_list|<
name|Setting
argument_list|<
name|ScriptMode
argument_list|>
argument_list|>
name|getScriptContextSettings
parameter_list|()
block|{
return|return
name|Collections
operator|.
name|unmodifiableCollection
argument_list|(
name|scriptContextSettingMap
operator|.
name|values
argument_list|()
argument_list|)
return|;
block|}
DECL|method|getScriptLanguageSettings
specifier|public
name|Iterable
argument_list|<
name|Setting
argument_list|<
name|ScriptMode
argument_list|>
argument_list|>
name|getScriptLanguageSettings
parameter_list|()
block|{
return|return
name|scriptLanguageSettings
return|;
block|}
DECL|method|getDefaultScriptLanguageSetting
specifier|public
name|Setting
argument_list|<
name|String
argument_list|>
name|getDefaultScriptLanguageSetting
parameter_list|()
block|{
return|return
name|defaultScriptLanguageSetting
return|;
block|}
block|}
end_class

end_unit

