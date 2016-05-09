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
name|Settings
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
name|ScriptService
operator|.
name|ScriptType
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
name|org
operator|.
name|junit
operator|.
name|After
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
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
name|HashMap
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
name|Map
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
name|java
operator|.
name|util
operator|.
name|Collections
operator|.
name|unmodifiableMap
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|util
operator|.
name|Collections
operator|.
name|unmodifiableSet
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
name|util
operator|.
name|set
operator|.
name|Sets
operator|.
name|newHashSet
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
name|equalTo
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

begin_comment
comment|// TODO: this needs to be a base test class, and all scripting engines extend it
end_comment

begin_class
DECL|class|ScriptModesTests
specifier|public
class|class
name|ScriptModesTests
extends|extends
name|ESTestCase
block|{
DECL|field|ALL_LANGS
specifier|private
specifier|static
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|ALL_LANGS
init|=
name|unmodifiableSet
argument_list|(
name|newHashSet
argument_list|(
literal|"custom"
argument_list|,
literal|"test"
argument_list|)
argument_list|)
decl_stmt|;
DECL|field|ENABLE_VALUES
specifier|static
specifier|final
name|String
index|[]
name|ENABLE_VALUES
init|=
operator|new
name|String
index|[]
block|{
literal|"true"
block|}
decl_stmt|;
DECL|field|DISABLE_VALUES
specifier|static
specifier|final
name|String
index|[]
name|DISABLE_VALUES
init|=
operator|new
name|String
index|[]
block|{
literal|"false"
block|}
decl_stmt|;
DECL|field|scriptSettings
name|ScriptSettings
name|scriptSettings
decl_stmt|;
DECL|field|scriptContextRegistry
name|ScriptContextRegistry
name|scriptContextRegistry
decl_stmt|;
DECL|field|scriptContexts
specifier|private
name|ScriptContext
index|[]
name|scriptContexts
decl_stmt|;
DECL|field|scriptEngines
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|ScriptEngineService
argument_list|>
name|scriptEngines
decl_stmt|;
DECL|field|scriptModes
specifier|private
name|ScriptModes
name|scriptModes
decl_stmt|;
DECL|field|checkedSettings
specifier|private
name|Set
argument_list|<
name|String
argument_list|>
name|checkedSettings
decl_stmt|;
DECL|field|assertAllSettingsWereChecked
specifier|private
name|boolean
name|assertAllSettingsWereChecked
decl_stmt|;
DECL|field|assertScriptModesNonNull
specifier|private
name|boolean
name|assertScriptModesNonNull
decl_stmt|;
annotation|@
name|Before
DECL|method|setupScriptEngines
specifier|public
name|void
name|setupScriptEngines
parameter_list|()
block|{
comment|//randomly register custom script contexts
name|int
name|randomInt
init|=
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|3
argument_list|)
decl_stmt|;
comment|//prevent duplicates using map
name|Map
argument_list|<
name|String
argument_list|,
name|ScriptContext
operator|.
name|Plugin
argument_list|>
name|contexts
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|randomInt
condition|;
name|i
operator|++
control|)
block|{
name|String
name|plugin
init|=
name|randomAsciiOfLength
argument_list|(
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|operation
init|=
name|randomAsciiOfLength
argument_list|(
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|30
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|context
init|=
name|plugin
operator|+
literal|"-"
operator|+
name|operation
decl_stmt|;
name|contexts
operator|.
name|put
argument_list|(
name|context
argument_list|,
operator|new
name|ScriptContext
operator|.
name|Plugin
argument_list|(
name|plugin
argument_list|,
name|operation
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|scriptContextRegistry
operator|=
operator|new
name|ScriptContextRegistry
argument_list|(
name|contexts
operator|.
name|values
argument_list|()
argument_list|)
expr_stmt|;
name|scriptContexts
operator|=
name|scriptContextRegistry
operator|.
name|scriptContexts
argument_list|()
operator|.
name|toArray
argument_list|(
operator|new
name|ScriptContext
index|[
name|scriptContextRegistry
operator|.
name|scriptContexts
argument_list|()
operator|.
name|size
argument_list|()
index|]
argument_list|)
expr_stmt|;
name|scriptEngines
operator|=
name|buildScriptEnginesByLangMap
argument_list|(
name|newHashSet
argument_list|(
comment|//add the native engine just to make sure it gets filtered out
operator|new
name|NativeScriptEngineService
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
name|Collections
operator|.
expr|<
name|String
argument_list|,
name|NativeScriptFactory
operator|>
name|emptyMap
argument_list|()
argument_list|)
argument_list|,
operator|new
name|CustomScriptEngineService
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|ScriptEngineRegistry
name|scriptEngineRegistry
init|=
operator|new
name|ScriptEngineRegistry
argument_list|(
name|Arrays
operator|.
name|asList
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
argument_list|)
argument_list|,
operator|new
name|ScriptEngineRegistry
operator|.
name|ScriptEngineRegistration
argument_list|(
name|CustomScriptEngineService
operator|.
name|class
argument_list|,
name|CustomScriptEngineService
operator|.
name|TYPES
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|scriptSettings
operator|=
operator|new
name|ScriptSettings
argument_list|(
name|scriptEngineRegistry
argument_list|,
name|scriptContextRegistry
argument_list|)
expr_stmt|;
name|checkedSettings
operator|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
expr_stmt|;
name|assertAllSettingsWereChecked
operator|=
literal|true
expr_stmt|;
name|assertScriptModesNonNull
operator|=
literal|true
expr_stmt|;
block|}
annotation|@
name|After
DECL|method|assertNativeScriptsAreAlwaysAllowed
specifier|public
name|void
name|assertNativeScriptsAreAlwaysAllowed
parameter_list|()
block|{
if|if
condition|(
name|assertScriptModesNonNull
condition|)
block|{
name|assertThat
argument_list|(
name|scriptModes
operator|.
name|getScriptMode
argument_list|(
name|NativeScriptEngineService
operator|.
name|NAME
argument_list|,
name|randomFrom
argument_list|(
name|ScriptType
operator|.
name|values
argument_list|()
argument_list|)
argument_list|,
name|randomFrom
argument_list|(
name|scriptContexts
argument_list|)
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|ScriptMode
operator|.
name|ON
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|After
DECL|method|assertAllSettingsWereChecked
specifier|public
name|void
name|assertAllSettingsWereChecked
parameter_list|()
block|{
if|if
condition|(
name|assertScriptModesNonNull
condition|)
block|{
name|assertThat
argument_list|(
name|scriptModes
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
comment|//2 is the number of engines (native excluded), custom is counted twice though as it's associated with two different names
name|int
name|numberOfSettings
init|=
literal|2
operator|*
name|ScriptType
operator|.
name|values
argument_list|()
operator|.
name|length
operator|*
name|scriptContextRegistry
operator|.
name|scriptContexts
argument_list|()
operator|.
name|size
argument_list|()
decl_stmt|;
name|numberOfSettings
operator|+=
literal|6
expr_stmt|;
comment|// for top-level inline/store/file settings
name|assertThat
argument_list|(
name|scriptModes
operator|.
name|scriptModes
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|numberOfSettings
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|assertAllSettingsWereChecked
condition|)
block|{
name|assertThat
argument_list|(
name|checkedSettings
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|numberOfSettings
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|testDefaultSettings
specifier|public
name|void
name|testDefaultSettings
parameter_list|()
block|{
name|this
operator|.
name|scriptModes
operator|=
operator|new
name|ScriptModes
argument_list|(
name|scriptSettings
argument_list|,
name|Settings
operator|.
name|EMPTY
argument_list|)
expr_stmt|;
name|assertScriptModesAllOps
argument_list|(
name|ScriptMode
operator|.
name|ON
argument_list|,
name|ALL_LANGS
argument_list|,
name|ScriptType
operator|.
name|FILE
argument_list|)
expr_stmt|;
name|assertScriptModesAllOps
argument_list|(
name|ScriptMode
operator|.
name|OFF
argument_list|,
name|ALL_LANGS
argument_list|,
name|ScriptType
operator|.
name|STORED
argument_list|,
name|ScriptType
operator|.
name|INLINE
argument_list|)
expr_stmt|;
block|}
DECL|method|testMissingSetting
specifier|public
name|void
name|testMissingSetting
parameter_list|()
block|{
name|assertAllSettingsWereChecked
operator|=
literal|false
expr_stmt|;
name|this
operator|.
name|scriptModes
operator|=
operator|new
name|ScriptModes
argument_list|(
name|scriptSettings
argument_list|,
name|Settings
operator|.
name|EMPTY
argument_list|)
expr_stmt|;
try|try
block|{
name|scriptModes
operator|.
name|getScriptMode
argument_list|(
literal|"non_existing"
argument_list|,
name|randomFrom
argument_list|(
name|ScriptType
operator|.
name|values
argument_list|()
argument_list|)
argument_list|,
name|randomFrom
argument_list|(
name|scriptContexts
argument_list|)
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Expected IllegalArgumentException"
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
literal|"not found for lang [non_existing]"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testScriptTypeGenericSettings
specifier|public
name|void
name|testScriptTypeGenericSettings
parameter_list|()
block|{
name|int
name|randomInt
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
name|ScriptType
operator|.
name|values
argument_list|()
operator|.
name|length
operator|-
literal|1
argument_list|)
decl_stmt|;
name|Set
argument_list|<
name|ScriptType
argument_list|>
name|randomScriptTypesSet
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
name|ScriptMode
index|[]
name|randomScriptModes
init|=
operator|new
name|ScriptMode
index|[
name|randomInt
index|]
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|randomInt
condition|;
name|i
operator|++
control|)
block|{
name|boolean
name|added
init|=
literal|false
decl_stmt|;
while|while
condition|(
name|added
operator|==
literal|false
condition|)
block|{
name|added
operator|=
name|randomScriptTypesSet
operator|.
name|add
argument_list|(
name|randomFrom
argument_list|(
name|ScriptType
operator|.
name|values
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|randomScriptModes
index|[
name|i
index|]
operator|=
name|randomFrom
argument_list|(
name|ScriptMode
operator|.
name|values
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|ScriptType
index|[]
name|randomScriptTypes
init|=
name|randomScriptTypesSet
operator|.
name|toArray
argument_list|(
operator|new
name|ScriptType
index|[
name|randomScriptTypesSet
operator|.
name|size
argument_list|()
index|]
argument_list|)
decl_stmt|;
name|Settings
operator|.
name|Builder
name|builder
init|=
name|Settings
operator|.
name|builder
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|randomInt
condition|;
name|i
operator|++
control|)
block|{
name|builder
operator|.
name|put
argument_list|(
literal|"script"
operator|+
literal|"."
operator|+
name|randomScriptTypes
index|[
name|i
index|]
operator|.
name|getScriptType
argument_list|()
argument_list|,
name|randomScriptModes
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|scriptModes
operator|=
operator|new
name|ScriptModes
argument_list|(
name|scriptSettings
argument_list|,
name|builder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|randomInt
condition|;
name|i
operator|++
control|)
block|{
name|assertScriptModesAllOps
argument_list|(
name|randomScriptModes
index|[
name|i
index|]
argument_list|,
name|ALL_LANGS
argument_list|,
name|randomScriptTypes
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|randomScriptTypesSet
operator|.
name|contains
argument_list|(
name|ScriptType
operator|.
name|FILE
argument_list|)
operator|==
literal|false
condition|)
block|{
name|assertScriptModesAllOps
argument_list|(
name|ScriptMode
operator|.
name|ON
argument_list|,
name|ALL_LANGS
argument_list|,
name|ScriptType
operator|.
name|FILE
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|randomScriptTypesSet
operator|.
name|contains
argument_list|(
name|ScriptType
operator|.
name|STORED
argument_list|)
operator|==
literal|false
condition|)
block|{
name|assertScriptModesAllOps
argument_list|(
name|ScriptMode
operator|.
name|OFF
argument_list|,
name|ALL_LANGS
argument_list|,
name|ScriptType
operator|.
name|STORED
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|randomScriptTypesSet
operator|.
name|contains
argument_list|(
name|ScriptType
operator|.
name|INLINE
argument_list|)
operator|==
literal|false
condition|)
block|{
name|assertScriptModesAllOps
argument_list|(
name|ScriptMode
operator|.
name|OFF
argument_list|,
name|ALL_LANGS
argument_list|,
name|ScriptType
operator|.
name|INLINE
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testScriptContextGenericSettings
specifier|public
name|void
name|testScriptContextGenericSettings
parameter_list|()
block|{
name|int
name|randomInt
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
name|scriptContexts
operator|.
name|length
operator|-
literal|1
argument_list|)
decl_stmt|;
name|Set
argument_list|<
name|ScriptContext
argument_list|>
name|randomScriptContextsSet
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
name|ScriptMode
index|[]
name|randomScriptModes
init|=
operator|new
name|ScriptMode
index|[
name|randomInt
index|]
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|randomInt
condition|;
name|i
operator|++
control|)
block|{
name|boolean
name|added
init|=
literal|false
decl_stmt|;
while|while
condition|(
name|added
operator|==
literal|false
condition|)
block|{
name|added
operator|=
name|randomScriptContextsSet
operator|.
name|add
argument_list|(
name|randomFrom
argument_list|(
name|scriptContexts
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|randomScriptModes
index|[
name|i
index|]
operator|=
name|randomFrom
argument_list|(
name|ScriptMode
operator|.
name|values
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|ScriptContext
index|[]
name|randomScriptContexts
init|=
name|randomScriptContextsSet
operator|.
name|toArray
argument_list|(
operator|new
name|ScriptContext
index|[
name|randomScriptContextsSet
operator|.
name|size
argument_list|()
index|]
argument_list|)
decl_stmt|;
name|Settings
operator|.
name|Builder
name|builder
init|=
name|Settings
operator|.
name|builder
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|randomInt
condition|;
name|i
operator|++
control|)
block|{
name|builder
operator|.
name|put
argument_list|(
literal|"script"
operator|+
literal|"."
operator|+
name|randomScriptContexts
index|[
name|i
index|]
operator|.
name|getKey
argument_list|()
argument_list|,
name|randomScriptModes
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|scriptModes
operator|=
operator|new
name|ScriptModes
argument_list|(
name|scriptSettings
argument_list|,
name|builder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|randomInt
condition|;
name|i
operator|++
control|)
block|{
name|assertScriptModesAllTypes
argument_list|(
name|randomScriptModes
index|[
name|i
index|]
argument_list|,
name|ALL_LANGS
argument_list|,
name|randomScriptContexts
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
name|ScriptContext
index|[]
name|complementOf
init|=
name|complementOf
argument_list|(
name|randomScriptContexts
argument_list|)
decl_stmt|;
name|assertScriptModes
argument_list|(
name|ScriptMode
operator|.
name|ON
argument_list|,
name|ALL_LANGS
argument_list|,
operator|new
name|ScriptType
index|[]
block|{
name|ScriptType
operator|.
name|FILE
block|}
argument_list|,
name|complementOf
argument_list|)
expr_stmt|;
name|assertScriptModes
argument_list|(
name|ScriptMode
operator|.
name|OFF
argument_list|,
name|ALL_LANGS
argument_list|,
operator|new
name|ScriptType
index|[]
block|{
name|ScriptType
operator|.
name|STORED
block|,
name|ScriptType
operator|.
name|INLINE
block|}
argument_list|,
name|complementOf
argument_list|)
expr_stmt|;
block|}
DECL|method|testConflictingScriptTypeAndOpGenericSettings
specifier|public
name|void
name|testConflictingScriptTypeAndOpGenericSettings
parameter_list|()
block|{
name|ScriptContext
name|scriptContext
init|=
name|randomFrom
argument_list|(
name|scriptContexts
argument_list|)
decl_stmt|;
name|Settings
operator|.
name|Builder
name|builder
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"script"
operator|+
literal|"."
operator|+
name|scriptContext
operator|.
name|getKey
argument_list|()
argument_list|,
name|randomFrom
argument_list|(
name|DISABLE_VALUES
argument_list|)
argument_list|)
operator|.
name|put
argument_list|(
literal|"script.stored"
argument_list|,
name|randomFrom
argument_list|(
name|ENABLE_VALUES
argument_list|)
argument_list|)
operator|.
name|put
argument_list|(
literal|"script.inline"
argument_list|,
literal|"true"
argument_list|)
decl_stmt|;
comment|//operations generic settings have precedence over script type generic settings
name|this
operator|.
name|scriptModes
operator|=
operator|new
name|ScriptModes
argument_list|(
name|scriptSettings
argument_list|,
name|builder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|assertScriptModesAllTypes
argument_list|(
name|ScriptMode
operator|.
name|OFF
argument_list|,
name|ALL_LANGS
argument_list|,
name|scriptContext
argument_list|)
expr_stmt|;
name|ScriptContext
index|[]
name|complementOf
init|=
name|complementOf
argument_list|(
name|scriptContext
argument_list|)
decl_stmt|;
name|assertScriptModes
argument_list|(
name|ScriptMode
operator|.
name|ON
argument_list|,
name|ALL_LANGS
argument_list|,
operator|new
name|ScriptType
index|[]
block|{
name|ScriptType
operator|.
name|FILE
block|,
name|ScriptType
operator|.
name|STORED
block|}
argument_list|,
name|complementOf
argument_list|)
expr_stmt|;
name|assertScriptModes
argument_list|(
name|ScriptMode
operator|.
name|ON
argument_list|,
name|ALL_LANGS
argument_list|,
operator|new
name|ScriptType
index|[]
block|{
name|ScriptType
operator|.
name|INLINE
block|}
argument_list|,
name|complementOf
argument_list|)
expr_stmt|;
block|}
DECL|method|assertScriptModesAllOps
specifier|private
name|void
name|assertScriptModesAllOps
parameter_list|(
name|ScriptMode
name|expectedScriptMode
parameter_list|,
name|Set
argument_list|<
name|String
argument_list|>
name|langs
parameter_list|,
name|ScriptType
modifier|...
name|scriptTypes
parameter_list|)
block|{
name|assertScriptModes
argument_list|(
name|expectedScriptMode
argument_list|,
name|langs
argument_list|,
name|scriptTypes
argument_list|,
name|scriptContexts
argument_list|)
expr_stmt|;
block|}
DECL|method|assertScriptModesAllTypes
specifier|private
name|void
name|assertScriptModesAllTypes
parameter_list|(
name|ScriptMode
name|expectedScriptMode
parameter_list|,
name|Set
argument_list|<
name|String
argument_list|>
name|langs
parameter_list|,
name|ScriptContext
modifier|...
name|scriptContexts
parameter_list|)
block|{
name|assertScriptModes
argument_list|(
name|expectedScriptMode
argument_list|,
name|langs
argument_list|,
name|ScriptType
operator|.
name|values
argument_list|()
argument_list|,
name|scriptContexts
argument_list|)
expr_stmt|;
block|}
DECL|method|assertScriptModes
specifier|private
name|void
name|assertScriptModes
parameter_list|(
name|ScriptMode
name|expectedScriptMode
parameter_list|,
name|Set
argument_list|<
name|String
argument_list|>
name|langs
parameter_list|,
name|ScriptType
index|[]
name|scriptTypes
parameter_list|,
name|ScriptContext
modifier|...
name|scriptContexts
parameter_list|)
block|{
assert|assert
name|langs
operator|.
name|size
argument_list|()
operator|>
literal|0
assert|;
assert|assert
name|scriptTypes
operator|.
name|length
operator|>
literal|0
assert|;
assert|assert
name|scriptContexts
operator|.
name|length
operator|>
literal|0
assert|;
for|for
control|(
name|String
name|lang
range|:
name|langs
control|)
block|{
for|for
control|(
name|ScriptType
name|scriptType
range|:
name|scriptTypes
control|)
block|{
name|checkedSettings
operator|.
name|add
argument_list|(
literal|"script.engine."
operator|+
name|lang
operator|+
literal|"."
operator|+
name|scriptType
argument_list|)
expr_stmt|;
for|for
control|(
name|ScriptContext
name|scriptContext
range|:
name|scriptContexts
control|)
block|{
name|assertThat
argument_list|(
name|lang
operator|+
literal|"."
operator|+
name|scriptType
operator|+
literal|"."
operator|+
name|scriptContext
operator|.
name|getKey
argument_list|()
operator|+
literal|" doesn't have the expected value"
argument_list|,
name|scriptModes
operator|.
name|getScriptMode
argument_list|(
name|lang
argument_list|,
name|scriptType
argument_list|,
name|scriptContext
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|expectedScriptMode
argument_list|)
argument_list|)
expr_stmt|;
name|checkedSettings
operator|.
name|add
argument_list|(
name|lang
operator|+
literal|"."
operator|+
name|scriptType
operator|+
literal|"."
operator|+
name|scriptContext
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
DECL|method|complementOf
specifier|private
name|ScriptContext
index|[]
name|complementOf
parameter_list|(
name|ScriptContext
modifier|...
name|scriptContexts
parameter_list|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|ScriptContext
argument_list|>
name|copy
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
name|copy
operator|.
name|put
argument_list|(
name|scriptContext
operator|.
name|getKey
argument_list|()
argument_list|,
name|scriptContext
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|ScriptContext
name|scriptContext
range|:
name|scriptContexts
control|)
block|{
name|copy
operator|.
name|remove
argument_list|(
name|scriptContext
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|copy
operator|.
name|values
argument_list|()
operator|.
name|toArray
argument_list|(
operator|new
name|ScriptContext
index|[
name|copy
operator|.
name|size
argument_list|()
index|]
argument_list|)
return|;
block|}
DECL|method|buildScriptEnginesByLangMap
specifier|static
name|Map
argument_list|<
name|String
argument_list|,
name|ScriptEngineService
argument_list|>
name|buildScriptEnginesByLangMap
parameter_list|(
name|Set
argument_list|<
name|ScriptEngineService
argument_list|>
name|scriptEngines
parameter_list|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|ScriptEngineService
argument_list|>
name|builder
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|ScriptEngineService
name|scriptEngine
range|:
name|scriptEngines
control|)
block|{
for|for
control|(
name|String
name|type
range|:
name|scriptEngine
operator|.
name|getTypes
argument_list|()
control|)
block|{
name|builder
operator|.
name|put
argument_list|(
name|type
argument_list|,
name|scriptEngine
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|unmodifiableMap
argument_list|(
name|builder
argument_list|)
return|;
block|}
DECL|class|CustomScriptEngineService
specifier|private
specifier|static
class|class
name|CustomScriptEngineService
implements|implements
name|ScriptEngineService
block|{
DECL|field|TYPES
specifier|public
specifier|static
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|TYPES
init|=
name|Collections
operator|.
name|unmodifiableList
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"custom"
argument_list|,
literal|"test"
argument_list|)
argument_list|)
decl_stmt|;
annotation|@
name|Override
DECL|method|getTypes
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getTypes
parameter_list|()
block|{
return|return
name|TYPES
return|;
block|}
annotation|@
name|Override
DECL|method|getExtensions
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getExtensions
parameter_list|()
block|{
return|return
name|Collections
operator|.
name|singletonList
argument_list|(
name|TYPES
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
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
name|script
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
block|{          }
annotation|@
name|Override
DECL|method|scriptRemoved
specifier|public
name|void
name|scriptRemoved
parameter_list|(
annotation|@
name|Nullable
name|CompiledScript
name|script
parameter_list|)
block|{          }
block|}
block|}
end_class

end_unit

