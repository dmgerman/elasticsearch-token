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
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|file
operator|.
name|Path
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
name|org
operator|.
name|elasticsearch
operator|.
name|ResourceNotFoundException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|admin
operator|.
name|cluster
operator|.
name|storedscripts
operator|.
name|GetStoredScriptRequest
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
name|ClusterName
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
name|ClusterState
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
name|metadata
operator|.
name|MetaData
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
name|breaker
operator|.
name|CircuitBreakingException
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
name|bytes
operator|.
name|BytesArray
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
name|bytes
operator|.
name|BytesReference
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
name|common
operator|.
name|xcontent
operator|.
name|XContentFactory
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
name|xcontent
operator|.
name|XContentType
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
name|search
operator|.
name|aggregations
operator|.
name|support
operator|.
name|ValuesSource
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
name|Before
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
name|Matchers
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
name|sameInstance
import|;
end_import

begin_class
DECL|class|ScriptServiceTests
specifier|public
class|class
name|ScriptServiceTests
extends|extends
name|ESTestCase
block|{
DECL|field|scriptEngine
specifier|private
name|ScriptEngine
name|scriptEngine
decl_stmt|;
DECL|field|engines
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|ScriptEngine
argument_list|>
name|engines
decl_stmt|;
DECL|field|contexts
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|ScriptContext
argument_list|<
name|?
argument_list|>
argument_list|>
name|contexts
decl_stmt|;
DECL|field|scriptService
specifier|private
name|ScriptService
name|scriptService
decl_stmt|;
DECL|field|baseSettings
specifier|private
name|Settings
name|baseSettings
decl_stmt|;
annotation|@
name|Before
DECL|method|setup
specifier|public
name|void
name|setup
parameter_list|()
throws|throws
name|IOException
block|{
name|Path
name|genericConfigFolder
init|=
name|createTempDir
argument_list|()
decl_stmt|;
name|baseSettings
operator|=
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
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|Environment
operator|.
name|PATH_CONF_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
name|genericConfigFolder
argument_list|)
operator|.
name|put
argument_list|(
name|ScriptService
operator|.
name|SCRIPT_MAX_COMPILATIONS_PER_MINUTE
operator|.
name|getKey
argument_list|()
argument_list|,
literal|10000
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Function
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|,
name|Object
argument_list|>
argument_list|>
name|scripts
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
literal|20
condition|;
operator|++
name|i
control|)
block|{
name|scripts
operator|.
name|put
argument_list|(
name|i
operator|+
literal|"+"
operator|+
name|i
argument_list|,
name|p
lambda|->
literal|null
argument_list|)
expr_stmt|;
comment|// only care about compilation, not execution
block|}
name|scripts
operator|.
name|put
argument_list|(
literal|"script"
argument_list|,
name|p
lambda|->
literal|null
argument_list|)
expr_stmt|;
name|scriptEngine
operator|=
operator|new
name|MockScriptEngine
argument_list|(
name|Script
operator|.
name|DEFAULT_SCRIPT_LANG
argument_list|,
name|scripts
argument_list|)
expr_stmt|;
comment|//prevent duplicates using map
name|contexts
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|(
name|ScriptModule
operator|.
name|CORE_CONTEXTS
argument_list|)
expr_stmt|;
name|engines
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
expr_stmt|;
name|engines
operator|.
name|put
argument_list|(
name|scriptEngine
operator|.
name|getType
argument_list|()
argument_list|,
name|scriptEngine
argument_list|)
expr_stmt|;
name|engines
operator|.
name|put
argument_list|(
literal|"test"
argument_list|,
operator|new
name|MockScriptEngine
argument_list|(
literal|"test"
argument_list|,
name|scripts
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> setup script service"
argument_list|)
expr_stmt|;
block|}
DECL|method|buildScriptService
specifier|private
name|void
name|buildScriptService
parameter_list|(
name|Settings
name|additionalSettings
parameter_list|)
throws|throws
name|IOException
block|{
name|Settings
name|finalSettings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|baseSettings
argument_list|)
operator|.
name|put
argument_list|(
name|additionalSettings
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|scriptService
operator|=
operator|new
name|ScriptService
argument_list|(
name|finalSettings
argument_list|,
name|engines
argument_list|,
name|contexts
argument_list|)
block|{
annotation|@
name|Override
name|StoredScriptSource
name|getScriptFromClusterState
parameter_list|(
name|String
name|id
parameter_list|,
name|String
name|lang
parameter_list|)
block|{
comment|//mock the script that gets retrieved from an index
return|return
operator|new
name|StoredScriptSource
argument_list|(
name|lang
argument_list|,
literal|"1+1"
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|)
return|;
block|}
block|}
expr_stmt|;
block|}
DECL|method|testCompilationCircuitBreaking
specifier|public
name|void
name|testCompilationCircuitBreaking
parameter_list|()
throws|throws
name|Exception
block|{
name|buildScriptService
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
expr_stmt|;
name|scriptService
operator|.
name|setMaxCompilationsPerMinute
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|scriptService
operator|.
name|checkCompilationLimit
argument_list|()
expr_stmt|;
comment|// should pass
name|expectThrows
argument_list|(
name|CircuitBreakingException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|scriptService
operator|.
name|checkCompilationLimit
argument_list|()
argument_list|)
expr_stmt|;
name|scriptService
operator|.
name|setMaxCompilationsPerMinute
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|scriptService
operator|.
name|checkCompilationLimit
argument_list|()
expr_stmt|;
comment|// should pass
name|scriptService
operator|.
name|checkCompilationLimit
argument_list|()
expr_stmt|;
comment|// should pass
name|expectThrows
argument_list|(
name|CircuitBreakingException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|scriptService
operator|.
name|checkCompilationLimit
argument_list|()
argument_list|)
expr_stmt|;
name|int
name|count
init|=
name|randomIntBetween
argument_list|(
literal|5
argument_list|,
literal|50
argument_list|)
decl_stmt|;
name|scriptService
operator|.
name|setMaxCompilationsPerMinute
argument_list|(
name|count
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
name|count
condition|;
name|i
operator|++
control|)
block|{
name|scriptService
operator|.
name|checkCompilationLimit
argument_list|()
expr_stmt|;
comment|// should pass
block|}
name|expectThrows
argument_list|(
name|CircuitBreakingException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|scriptService
operator|.
name|checkCompilationLimit
argument_list|()
argument_list|)
expr_stmt|;
name|scriptService
operator|.
name|setMaxCompilationsPerMinute
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|expectThrows
argument_list|(
name|CircuitBreakingException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|scriptService
operator|.
name|checkCompilationLimit
argument_list|()
argument_list|)
expr_stmt|;
name|scriptService
operator|.
name|setMaxCompilationsPerMinute
argument_list|(
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
expr_stmt|;
name|int
name|largeLimit
init|=
name|randomIntBetween
argument_list|(
literal|1000
argument_list|,
literal|10000
argument_list|)
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
name|largeLimit
condition|;
name|i
operator|++
control|)
block|{
name|scriptService
operator|.
name|checkCompilationLimit
argument_list|()
expr_stmt|;
block|}
block|}
DECL|method|testNotSupportedDisableDynamicSetting
specifier|public
name|void
name|testNotSupportedDisableDynamicSetting
parameter_list|()
throws|throws
name|IOException
block|{
try|try
block|{
name|buildScriptService
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|ScriptService
operator|.
name|DISABLE_DYNAMIC_SCRIPTING_SETTING
argument_list|,
name|randomUnicodeOfLength
argument_list|(
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"script service should have thrown exception due to non supported script.disable_dynamic setting"
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
name|ScriptService
operator|.
name|DISABLE_DYNAMIC_SCRIPTING_SETTING
operator|+
literal|" is not a supported setting, replace with fine-grained script settings"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testInlineScriptCompiledOnceCache
specifier|public
name|void
name|testInlineScriptCompiledOnceCache
parameter_list|()
throws|throws
name|IOException
block|{
name|buildScriptService
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
expr_stmt|;
name|Script
name|script
init|=
operator|new
name|Script
argument_list|(
name|ScriptType
operator|.
name|INLINE
argument_list|,
literal|"test"
argument_list|,
literal|"1+1"
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|)
decl_stmt|;
name|SearchScript
operator|.
name|Compiled
name|compiledScript1
init|=
name|scriptService
operator|.
name|compile
argument_list|(
name|script
argument_list|,
name|SearchScript
operator|.
name|CONTEXT
argument_list|)
decl_stmt|;
name|SearchScript
operator|.
name|Compiled
name|compiledScript2
init|=
name|scriptService
operator|.
name|compile
argument_list|(
name|script
argument_list|,
name|SearchScript
operator|.
name|CONTEXT
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|compiledScript1
argument_list|,
name|sameInstance
argument_list|(
name|compiledScript2
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testAllowAllScriptTypeSettings
specifier|public
name|void
name|testAllowAllScriptTypeSettings
parameter_list|()
throws|throws
name|IOException
block|{
name|buildScriptService
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
expr_stmt|;
name|assertCompileAccepted
argument_list|(
literal|"painless"
argument_list|,
literal|"script"
argument_list|,
name|ScriptType
operator|.
name|INLINE
argument_list|,
name|SearchScript
operator|.
name|CONTEXT
argument_list|)
expr_stmt|;
name|assertCompileAccepted
argument_list|(
literal|"painless"
argument_list|,
literal|"script"
argument_list|,
name|ScriptType
operator|.
name|STORED
argument_list|,
name|SearchScript
operator|.
name|CONTEXT
argument_list|)
expr_stmt|;
block|}
DECL|method|testAllowAllScriptContextSettings
specifier|public
name|void
name|testAllowAllScriptContextSettings
parameter_list|()
throws|throws
name|IOException
block|{
name|buildScriptService
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
expr_stmt|;
name|assertCompileAccepted
argument_list|(
literal|"painless"
argument_list|,
literal|"script"
argument_list|,
name|ScriptType
operator|.
name|INLINE
argument_list|,
name|SearchScript
operator|.
name|CONTEXT
argument_list|)
expr_stmt|;
name|assertCompileAccepted
argument_list|(
literal|"painless"
argument_list|,
literal|"script"
argument_list|,
name|ScriptType
operator|.
name|INLINE
argument_list|,
name|SearchScript
operator|.
name|AGGS_CONTEXT
argument_list|)
expr_stmt|;
name|assertCompileAccepted
argument_list|(
literal|"painless"
argument_list|,
literal|"script"
argument_list|,
name|ScriptType
operator|.
name|INLINE
argument_list|,
name|ExecutableScript
operator|.
name|UPDATE_CONTEXT
argument_list|)
expr_stmt|;
name|assertCompileAccepted
argument_list|(
literal|"painless"
argument_list|,
literal|"script"
argument_list|,
name|ScriptType
operator|.
name|INLINE
argument_list|,
name|ExecutableScript
operator|.
name|INGEST_CONTEXT
argument_list|)
expr_stmt|;
block|}
DECL|method|testAllowSomeScriptTypeSettings
specifier|public
name|void
name|testAllowSomeScriptTypeSettings
parameter_list|()
throws|throws
name|IOException
block|{
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
name|builder
operator|.
name|put
argument_list|(
literal|"script.allowed_types"
argument_list|,
literal|"inline"
argument_list|)
expr_stmt|;
name|buildScriptService
argument_list|(
name|builder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|assertCompileAccepted
argument_list|(
literal|"painless"
argument_list|,
literal|"script"
argument_list|,
name|ScriptType
operator|.
name|INLINE
argument_list|,
name|SearchScript
operator|.
name|CONTEXT
argument_list|)
expr_stmt|;
name|assertCompileRejected
argument_list|(
literal|"painless"
argument_list|,
literal|"script"
argument_list|,
name|ScriptType
operator|.
name|STORED
argument_list|,
name|SearchScript
operator|.
name|CONTEXT
argument_list|)
expr_stmt|;
block|}
DECL|method|testAllowSomeScriptContextSettings
specifier|public
name|void
name|testAllowSomeScriptContextSettings
parameter_list|()
throws|throws
name|IOException
block|{
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
name|builder
operator|.
name|put
argument_list|(
literal|"script.allowed_contexts"
argument_list|,
literal|"search, aggs"
argument_list|)
expr_stmt|;
name|buildScriptService
argument_list|(
name|builder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|assertCompileAccepted
argument_list|(
literal|"painless"
argument_list|,
literal|"script"
argument_list|,
name|ScriptType
operator|.
name|INLINE
argument_list|,
name|SearchScript
operator|.
name|CONTEXT
argument_list|)
expr_stmt|;
name|assertCompileAccepted
argument_list|(
literal|"painless"
argument_list|,
literal|"script"
argument_list|,
name|ScriptType
operator|.
name|INLINE
argument_list|,
name|SearchScript
operator|.
name|AGGS_CONTEXT
argument_list|)
expr_stmt|;
name|assertCompileRejected
argument_list|(
literal|"painless"
argument_list|,
literal|"script"
argument_list|,
name|ScriptType
operator|.
name|INLINE
argument_list|,
name|ExecutableScript
operator|.
name|UPDATE_CONTEXT
argument_list|)
expr_stmt|;
block|}
DECL|method|testAllowNoScriptTypeSettings
specifier|public
name|void
name|testAllowNoScriptTypeSettings
parameter_list|()
throws|throws
name|IOException
block|{
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
name|builder
operator|.
name|put
argument_list|(
literal|"script.allowed_types"
argument_list|,
literal|"none"
argument_list|)
expr_stmt|;
name|buildScriptService
argument_list|(
name|builder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|assertCompileRejected
argument_list|(
literal|"painless"
argument_list|,
literal|"script"
argument_list|,
name|ScriptType
operator|.
name|INLINE
argument_list|,
name|SearchScript
operator|.
name|CONTEXT
argument_list|)
expr_stmt|;
name|assertCompileRejected
argument_list|(
literal|"painless"
argument_list|,
literal|"script"
argument_list|,
name|ScriptType
operator|.
name|STORED
argument_list|,
name|SearchScript
operator|.
name|CONTEXT
argument_list|)
expr_stmt|;
block|}
DECL|method|testAllowNoScriptContextSettings
specifier|public
name|void
name|testAllowNoScriptContextSettings
parameter_list|()
throws|throws
name|IOException
block|{
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
name|builder
operator|.
name|put
argument_list|(
literal|"script.allowed_contexts"
argument_list|,
literal|"none"
argument_list|)
expr_stmt|;
name|buildScriptService
argument_list|(
name|builder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|assertCompileRejected
argument_list|(
literal|"painless"
argument_list|,
literal|"script"
argument_list|,
name|ScriptType
operator|.
name|INLINE
argument_list|,
name|SearchScript
operator|.
name|CONTEXT
argument_list|)
expr_stmt|;
name|assertCompileRejected
argument_list|(
literal|"painless"
argument_list|,
literal|"script"
argument_list|,
name|ScriptType
operator|.
name|INLINE
argument_list|,
name|SearchScript
operator|.
name|AGGS_CONTEXT
argument_list|)
expr_stmt|;
block|}
DECL|method|testCompileNonRegisteredContext
specifier|public
name|void
name|testCompileNonRegisteredContext
parameter_list|()
throws|throws
name|IOException
block|{
name|contexts
operator|.
name|remove
argument_list|(
name|ExecutableScript
operator|.
name|INGEST_CONTEXT
operator|.
name|name
argument_list|)
expr_stmt|;
name|buildScriptService
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
expr_stmt|;
name|String
name|type
init|=
name|scriptEngine
operator|.
name|getType
argument_list|()
decl_stmt|;
name|IllegalArgumentException
name|e
init|=
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|scriptService
operator|.
name|compile
argument_list|(
operator|new
name|Script
argument_list|(
name|randomFrom
argument_list|(
name|ScriptType
operator|.
name|values
argument_list|()
argument_list|)
argument_list|,
name|type
argument_list|,
literal|"test"
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|)
argument_list|,
name|ExecutableScript
operator|.
name|INGEST_CONTEXT
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"script context ["
operator|+
name|ExecutableScript
operator|.
name|INGEST_CONTEXT
operator|.
name|name
operator|+
literal|"] not supported"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testCompileCountedInCompilationStats
specifier|public
name|void
name|testCompileCountedInCompilationStats
parameter_list|()
throws|throws
name|IOException
block|{
name|buildScriptService
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
expr_stmt|;
name|scriptService
operator|.
name|compile
argument_list|(
operator|new
name|Script
argument_list|(
name|ScriptType
operator|.
name|INLINE
argument_list|,
literal|"test"
argument_list|,
literal|"1+1"
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|)
argument_list|,
name|randomFrom
argument_list|(
name|contexts
operator|.
name|values
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1L
argument_list|,
name|scriptService
operator|.
name|stats
argument_list|()
operator|.
name|getCompilations
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testMultipleCompilationsCountedInCompilationStats
specifier|public
name|void
name|testMultipleCompilationsCountedInCompilationStats
parameter_list|()
throws|throws
name|IOException
block|{
name|buildScriptService
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
expr_stmt|;
name|int
name|numberOfCompilations
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|20
argument_list|)
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
name|numberOfCompilations
condition|;
name|i
operator|++
control|)
block|{
name|scriptService
operator|.
name|compile
argument_list|(
operator|new
name|Script
argument_list|(
name|ScriptType
operator|.
name|INLINE
argument_list|,
literal|"test"
argument_list|,
name|i
operator|+
literal|"+"
operator|+
name|i
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|)
argument_list|,
name|randomFrom
argument_list|(
name|contexts
operator|.
name|values
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|numberOfCompilations
argument_list|,
name|scriptService
operator|.
name|stats
argument_list|()
operator|.
name|getCompilations
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testCompilationStatsOnCacheHit
specifier|public
name|void
name|testCompilationStatsOnCacheHit
parameter_list|()
throws|throws
name|IOException
block|{
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
name|builder
operator|.
name|put
argument_list|(
name|ScriptService
operator|.
name|SCRIPT_CACHE_SIZE_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|buildScriptService
argument_list|(
name|builder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|Script
name|script
init|=
operator|new
name|Script
argument_list|(
name|ScriptType
operator|.
name|INLINE
argument_list|,
literal|"test"
argument_list|,
literal|"1+1"
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|)
decl_stmt|;
name|ScriptContext
argument_list|<
name|?
argument_list|>
name|context
init|=
name|randomFrom
argument_list|(
name|contexts
operator|.
name|values
argument_list|()
argument_list|)
decl_stmt|;
name|scriptService
operator|.
name|compile
argument_list|(
name|script
argument_list|,
name|context
argument_list|)
expr_stmt|;
name|scriptService
operator|.
name|compile
argument_list|(
name|script
argument_list|,
name|context
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1L
argument_list|,
name|scriptService
operator|.
name|stats
argument_list|()
operator|.
name|getCompilations
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testIndexedScriptCountedInCompilationStats
specifier|public
name|void
name|testIndexedScriptCountedInCompilationStats
parameter_list|()
throws|throws
name|IOException
block|{
name|buildScriptService
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
expr_stmt|;
name|scriptService
operator|.
name|compile
argument_list|(
operator|new
name|Script
argument_list|(
name|ScriptType
operator|.
name|STORED
argument_list|,
literal|"test"
argument_list|,
literal|"script"
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|)
argument_list|,
name|randomFrom
argument_list|(
name|contexts
operator|.
name|values
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1L
argument_list|,
name|scriptService
operator|.
name|stats
argument_list|()
operator|.
name|getCompilations
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testCacheEvictionCountedInCacheEvictionsStats
specifier|public
name|void
name|testCacheEvictionCountedInCacheEvictionsStats
parameter_list|()
throws|throws
name|IOException
block|{
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
name|builder
operator|.
name|put
argument_list|(
name|ScriptService
operator|.
name|SCRIPT_CACHE_SIZE_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|buildScriptService
argument_list|(
name|builder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|scriptService
operator|.
name|compile
argument_list|(
operator|new
name|Script
argument_list|(
name|ScriptType
operator|.
name|INLINE
argument_list|,
literal|"test"
argument_list|,
literal|"1+1"
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|)
argument_list|,
name|randomFrom
argument_list|(
name|contexts
operator|.
name|values
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|scriptService
operator|.
name|compile
argument_list|(
operator|new
name|Script
argument_list|(
name|ScriptType
operator|.
name|INLINE
argument_list|,
literal|"test"
argument_list|,
literal|"2+2"
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|)
argument_list|,
name|randomFrom
argument_list|(
name|contexts
operator|.
name|values
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2L
argument_list|,
name|scriptService
operator|.
name|stats
argument_list|()
operator|.
name|getCompilations
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1L
argument_list|,
name|scriptService
operator|.
name|stats
argument_list|()
operator|.
name|getCacheEvictions
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testStoreScript
specifier|public
name|void
name|testStoreScript
parameter_list|()
throws|throws
name|Exception
block|{
name|BytesReference
name|script
init|=
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"script"
argument_list|,
literal|"abc"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|bytes
argument_list|()
decl_stmt|;
name|ScriptMetaData
name|scriptMetaData
init|=
name|ScriptMetaData
operator|.
name|putStoredScript
argument_list|(
literal|null
argument_list|,
literal|"_id"
argument_list|,
name|StoredScriptSource
operator|.
name|parse
argument_list|(
literal|"_lang"
argument_list|,
name|script
argument_list|,
name|XContentType
operator|.
name|JSON
argument_list|)
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|scriptMetaData
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"abc"
argument_list|,
name|scriptMetaData
operator|.
name|getStoredScript
argument_list|(
literal|"_id"
argument_list|,
literal|"_lang"
argument_list|)
operator|.
name|getCode
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testDeleteScript
specifier|public
name|void
name|testDeleteScript
parameter_list|()
throws|throws
name|Exception
block|{
name|ScriptMetaData
name|scriptMetaData
init|=
name|ScriptMetaData
operator|.
name|putStoredScript
argument_list|(
literal|null
argument_list|,
literal|"_id"
argument_list|,
name|StoredScriptSource
operator|.
name|parse
argument_list|(
literal|"_lang"
argument_list|,
operator|new
name|BytesArray
argument_list|(
literal|"{\"script\":\"abc\"}"
argument_list|)
argument_list|,
name|XContentType
operator|.
name|JSON
argument_list|)
argument_list|)
decl_stmt|;
name|scriptMetaData
operator|=
name|ScriptMetaData
operator|.
name|deleteStoredScript
argument_list|(
name|scriptMetaData
argument_list|,
literal|"_id"
argument_list|,
literal|"_lang"
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|scriptMetaData
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|scriptMetaData
operator|.
name|getStoredScript
argument_list|(
literal|"_id"
argument_list|,
literal|"_lang"
argument_list|)
argument_list|)
expr_stmt|;
name|ScriptMetaData
name|errorMetaData
init|=
name|scriptMetaData
decl_stmt|;
name|ResourceNotFoundException
name|e
init|=
name|expectThrows
argument_list|(
name|ResourceNotFoundException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
block|{
name|ScriptMetaData
operator|.
name|deleteStoredScript
argument_list|(
name|errorMetaData
argument_list|,
literal|"_id"
argument_list|,
literal|"_lang"
argument_list|)
expr_stmt|;
block|}
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"stored script [_id] using lang [_lang] does not exist and cannot be deleted"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testGetStoredScript
specifier|public
name|void
name|testGetStoredScript
parameter_list|()
throws|throws
name|Exception
block|{
name|buildScriptService
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
expr_stmt|;
name|ClusterState
name|cs
init|=
name|ClusterState
operator|.
name|builder
argument_list|(
operator|new
name|ClusterName
argument_list|(
literal|"_name"
argument_list|)
argument_list|)
operator|.
name|metaData
argument_list|(
name|MetaData
operator|.
name|builder
argument_list|()
operator|.
name|putCustom
argument_list|(
name|ScriptMetaData
operator|.
name|TYPE
argument_list|,
operator|new
name|ScriptMetaData
operator|.
name|Builder
argument_list|(
literal|null
argument_list|)
operator|.
name|storeScript
argument_list|(
literal|"_id"
argument_list|,
name|StoredScriptSource
operator|.
name|parse
argument_list|(
literal|"_lang"
argument_list|,
operator|new
name|BytesArray
argument_list|(
literal|"{\"script\":\"abc\"}"
argument_list|)
argument_list|,
name|XContentType
operator|.
name|JSON
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|"abc"
argument_list|,
name|scriptService
operator|.
name|getStoredScript
argument_list|(
name|cs
argument_list|,
operator|new
name|GetStoredScriptRequest
argument_list|(
literal|"_id"
argument_list|,
literal|"_lang"
argument_list|)
argument_list|)
operator|.
name|getCode
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|scriptService
operator|.
name|getStoredScript
argument_list|(
name|cs
argument_list|,
operator|new
name|GetStoredScriptRequest
argument_list|(
literal|"_id2"
argument_list|,
literal|"_lang"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|cs
operator|=
name|ClusterState
operator|.
name|builder
argument_list|(
operator|new
name|ClusterName
argument_list|(
literal|"_name"
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|assertNull
argument_list|(
name|scriptService
operator|.
name|getStoredScript
argument_list|(
name|cs
argument_list|,
operator|new
name|GetStoredScriptRequest
argument_list|(
literal|"_id"
argument_list|,
literal|"_lang"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|assertCompileRejected
specifier|private
name|void
name|assertCompileRejected
parameter_list|(
name|String
name|lang
parameter_list|,
name|String
name|script
parameter_list|,
name|ScriptType
name|scriptType
parameter_list|,
name|ScriptContext
name|scriptContext
parameter_list|)
block|{
try|try
block|{
name|scriptService
operator|.
name|compile
argument_list|(
operator|new
name|Script
argument_list|(
name|scriptType
argument_list|,
name|lang
argument_list|,
name|script
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|)
argument_list|,
name|scriptContext
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"compile should have been rejected for lang ["
operator|+
name|lang
operator|+
literal|"], script_type ["
operator|+
name|scriptType
operator|+
literal|"], scripted_op ["
operator|+
name|scriptContext
operator|+
literal|"]"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
decl||
name|IllegalStateException
name|e
parameter_list|)
block|{
comment|// pass
block|}
block|}
DECL|method|assertCompileAccepted
specifier|private
name|void
name|assertCompileAccepted
parameter_list|(
name|String
name|lang
parameter_list|,
name|String
name|script
parameter_list|,
name|ScriptType
name|scriptType
parameter_list|,
name|ScriptContext
name|scriptContext
parameter_list|)
block|{
name|assertThat
argument_list|(
name|scriptService
operator|.
name|compile
argument_list|(
operator|new
name|Script
argument_list|(
name|scriptType
argument_list|,
name|lang
argument_list|,
name|script
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|)
argument_list|,
name|scriptContext
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

