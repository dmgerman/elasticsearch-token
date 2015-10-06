begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.script.python
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|python
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
name|MapBuilder
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
name|CompiledScript
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
name|ExecutableScript
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

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|instanceOf
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|PythonScriptEngineTests
specifier|public
class|class
name|PythonScriptEngineTests
extends|extends
name|ESTestCase
block|{
DECL|field|se
specifier|private
name|PythonScriptEngineService
name|se
decl_stmt|;
annotation|@
name|Before
DECL|method|setup
specifier|public
name|void
name|setup
parameter_list|()
block|{
name|se
operator|=
operator|new
name|PythonScriptEngineService
argument_list|(
name|Settings
operator|.
name|Builder
operator|.
name|EMPTY_SETTINGS
argument_list|)
expr_stmt|;
block|}
annotation|@
name|After
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
block|{
comment|// We need to clear some system properties
name|System
operator|.
name|clearProperty
argument_list|(
literal|"python.cachedir.skip"
argument_list|)
expr_stmt|;
name|System
operator|.
name|clearProperty
argument_list|(
literal|"python.console.encoding"
argument_list|)
expr_stmt|;
name|se
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testSimpleEquation
specifier|public
name|void
name|testSimpleEquation
parameter_list|()
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|vars
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|()
decl_stmt|;
name|Object
name|o
init|=
name|se
operator|.
name|execute
argument_list|(
operator|new
name|CompiledScript
argument_list|(
name|ScriptService
operator|.
name|ScriptType
operator|.
name|INLINE
argument_list|,
literal|"testSimpleEquation"
argument_list|,
literal|"python"
argument_list|,
name|se
operator|.
name|compile
argument_list|(
literal|"1 + 2"
argument_list|)
argument_list|)
argument_list|,
name|vars
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|Number
operator|)
name|o
operator|)
operator|.
name|intValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testMapAccess
specifier|public
name|void
name|testMapAccess
parameter_list|()
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|vars
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|obj2
init|=
name|MapBuilder
operator|.
expr|<
name|String
decl_stmt|,
name|Object
decl|>
name|newMapBuilder
argument_list|()
decl|.
name|put
argument_list|(
literal|"prop2"
argument_list|,
literal|"value2"
argument_list|)
decl|.
name|map
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|obj1
init|=
name|MapBuilder
operator|.
expr|<
name|String
decl_stmt|,
name|Object
decl|>
name|newMapBuilder
argument_list|()
decl|.
name|put
argument_list|(
literal|"prop1"
argument_list|,
literal|"value1"
argument_list|)
decl|.
name|put
argument_list|(
literal|"obj2"
argument_list|,
name|obj2
argument_list|)
decl|.
name|put
argument_list|(
literal|"l"
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
literal|"2"
argument_list|,
literal|"1"
argument_list|)
argument_list|)
decl|.
name|map
argument_list|()
decl_stmt|;
name|vars
operator|.
name|put
argument_list|(
literal|"obj1"
argument_list|,
name|obj1
argument_list|)
expr_stmt|;
name|Object
name|o
init|=
name|se
operator|.
name|execute
argument_list|(
operator|new
name|CompiledScript
argument_list|(
name|ScriptService
operator|.
name|ScriptType
operator|.
name|INLINE
argument_list|,
literal|"testMapAccess"
argument_list|,
literal|"python"
argument_list|,
name|se
operator|.
name|compile
argument_list|(
literal|"obj1"
argument_list|)
argument_list|)
argument_list|,
name|vars
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|o
argument_list|,
name|instanceOf
argument_list|(
name|Map
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|obj1
operator|=
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|o
expr_stmt|;
name|assertThat
argument_list|(
operator|(
name|String
operator|)
name|obj1
operator|.
name|get
argument_list|(
literal|"prop1"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"value1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
call|(
name|String
call|)
argument_list|(
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|obj1
operator|.
name|get
argument_list|(
literal|"obj2"
argument_list|)
argument_list|)
operator|.
name|get
argument_list|(
literal|"prop2"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"value2"
argument_list|)
argument_list|)
expr_stmt|;
name|o
operator|=
name|se
operator|.
name|execute
argument_list|(
operator|new
name|CompiledScript
argument_list|(
name|ScriptService
operator|.
name|ScriptType
operator|.
name|INLINE
argument_list|,
literal|"testMapAccess"
argument_list|,
literal|"python"
argument_list|,
name|se
operator|.
name|compile
argument_list|(
literal|"obj1['l'][0]"
argument_list|)
argument_list|)
argument_list|,
name|vars
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|String
operator|)
name|o
operator|)
argument_list|,
name|equalTo
argument_list|(
literal|"2"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testObjectMapInter
specifier|public
name|void
name|testObjectMapInter
parameter_list|()
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|vars
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|ctx
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|obj1
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|()
decl_stmt|;
name|obj1
operator|.
name|put
argument_list|(
literal|"prop1"
argument_list|,
literal|"value1"
argument_list|)
expr_stmt|;
name|ctx
operator|.
name|put
argument_list|(
literal|"obj1"
argument_list|,
name|obj1
argument_list|)
expr_stmt|;
name|vars
operator|.
name|put
argument_list|(
literal|"ctx"
argument_list|,
name|ctx
argument_list|)
expr_stmt|;
name|ExecutableScript
name|executable
init|=
name|se
operator|.
name|executable
argument_list|(
operator|new
name|CompiledScript
argument_list|(
name|ScriptService
operator|.
name|ScriptType
operator|.
name|INLINE
argument_list|,
literal|"testObjectInterMap"
argument_list|,
literal|"python"
argument_list|,
name|se
operator|.
name|compile
argument_list|(
literal|"ctx['obj2'] = { 'prop2' : 'value2' }; ctx['obj1']['prop1'] = 'uvalue1'"
argument_list|)
argument_list|)
argument_list|,
name|vars
argument_list|)
decl_stmt|;
name|executable
operator|.
name|run
argument_list|()
expr_stmt|;
name|ctx
operator|=
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|executable
operator|.
name|unwrap
argument_list|(
name|vars
operator|.
name|get
argument_list|(
literal|"ctx"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ctx
operator|.
name|containsKey
argument_list|(
literal|"obj1"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
call|(
name|String
call|)
argument_list|(
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|ctx
operator|.
name|get
argument_list|(
literal|"obj1"
argument_list|)
argument_list|)
operator|.
name|get
argument_list|(
literal|"prop1"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"uvalue1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ctx
operator|.
name|containsKey
argument_list|(
literal|"obj2"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
call|(
name|String
call|)
argument_list|(
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|ctx
operator|.
name|get
argument_list|(
literal|"obj2"
argument_list|)
argument_list|)
operator|.
name|get
argument_list|(
literal|"prop2"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"value2"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testAccessListInScript
specifier|public
name|void
name|testAccessListInScript
parameter_list|()
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|vars
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|obj2
init|=
name|MapBuilder
operator|.
expr|<
name|String
decl_stmt|,
name|Object
decl|>
name|newMapBuilder
argument_list|()
decl|.
name|put
argument_list|(
literal|"prop2"
argument_list|,
literal|"value2"
argument_list|)
decl|.
name|map
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|obj1
init|=
name|MapBuilder
operator|.
expr|<
name|String
decl_stmt|,
name|Object
decl|>
name|newMapBuilder
argument_list|()
decl|.
name|put
argument_list|(
literal|"prop1"
argument_list|,
literal|"value1"
argument_list|)
decl|.
name|put
argument_list|(
literal|"obj2"
argument_list|,
name|obj2
argument_list|)
decl|.
name|map
argument_list|()
decl_stmt|;
name|vars
operator|.
name|put
argument_list|(
literal|"l"
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
literal|"1"
argument_list|,
literal|"2"
argument_list|,
literal|"3"
argument_list|,
name|obj1
argument_list|)
argument_list|)
expr_stmt|;
comment|//        Object o = se.execute(se.compile("l.length"), vars);
comment|//        assertThat(((Number) o).intValue(), equalTo(4));
name|Object
name|o
init|=
name|se
operator|.
name|execute
argument_list|(
operator|new
name|CompiledScript
argument_list|(
name|ScriptService
operator|.
name|ScriptType
operator|.
name|INLINE
argument_list|,
literal|"testAccessListInScript"
argument_list|,
literal|"python"
argument_list|,
name|se
operator|.
name|compile
argument_list|(
literal|"l[0]"
argument_list|)
argument_list|)
argument_list|,
name|vars
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|String
operator|)
name|o
operator|)
argument_list|,
name|equalTo
argument_list|(
literal|"1"
argument_list|)
argument_list|)
expr_stmt|;
name|o
operator|=
name|se
operator|.
name|execute
argument_list|(
operator|new
name|CompiledScript
argument_list|(
name|ScriptService
operator|.
name|ScriptType
operator|.
name|INLINE
argument_list|,
literal|"testAccessListInScript"
argument_list|,
literal|"python"
argument_list|,
name|se
operator|.
name|compile
argument_list|(
literal|"l[3]"
argument_list|)
argument_list|)
argument_list|,
name|vars
argument_list|)
expr_stmt|;
name|obj1
operator|=
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|o
expr_stmt|;
name|assertThat
argument_list|(
operator|(
name|String
operator|)
name|obj1
operator|.
name|get
argument_list|(
literal|"prop1"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"value1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
call|(
name|String
call|)
argument_list|(
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|obj1
operator|.
name|get
argument_list|(
literal|"obj2"
argument_list|)
argument_list|)
operator|.
name|get
argument_list|(
literal|"prop2"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"value2"
argument_list|)
argument_list|)
expr_stmt|;
name|o
operator|=
name|se
operator|.
name|execute
argument_list|(
operator|new
name|CompiledScript
argument_list|(
name|ScriptService
operator|.
name|ScriptType
operator|.
name|INLINE
argument_list|,
literal|"testAccessListInScript"
argument_list|,
literal|"python"
argument_list|,
name|se
operator|.
name|compile
argument_list|(
literal|"l[3]['prop1']"
argument_list|)
argument_list|)
argument_list|,
name|vars
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|String
operator|)
name|o
operator|)
argument_list|,
name|equalTo
argument_list|(
literal|"value1"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testChangingVarsCrossExecution1
specifier|public
name|void
name|testChangingVarsCrossExecution1
parameter_list|()
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|vars
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|ctx
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|()
decl_stmt|;
name|vars
operator|.
name|put
argument_list|(
literal|"ctx"
argument_list|,
name|ctx
argument_list|)
expr_stmt|;
name|Object
name|compiledScript
init|=
name|se
operator|.
name|compile
argument_list|(
literal|"ctx['value']"
argument_list|)
decl_stmt|;
name|ExecutableScript
name|script
init|=
name|se
operator|.
name|executable
argument_list|(
operator|new
name|CompiledScript
argument_list|(
name|ScriptService
operator|.
name|ScriptType
operator|.
name|INLINE
argument_list|,
literal|"testChangingVarsCrossExecution1"
argument_list|,
literal|"python"
argument_list|,
name|compiledScript
argument_list|)
argument_list|,
name|vars
argument_list|)
decl_stmt|;
name|ctx
operator|.
name|put
argument_list|(
literal|"value"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|Object
name|o
init|=
name|script
operator|.
name|run
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|Number
operator|)
name|o
operator|)
operator|.
name|intValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|ctx
operator|.
name|put
argument_list|(
literal|"value"
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|o
operator|=
name|script
operator|.
name|run
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|Number
operator|)
name|o
operator|)
operator|.
name|intValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testChangingVarsCrossExecution2
specifier|public
name|void
name|testChangingVarsCrossExecution2
parameter_list|()
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|vars
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|ctx
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|()
decl_stmt|;
name|Object
name|compiledScript
init|=
name|se
operator|.
name|compile
argument_list|(
literal|"value"
argument_list|)
decl_stmt|;
name|ExecutableScript
name|script
init|=
name|se
operator|.
name|executable
argument_list|(
operator|new
name|CompiledScript
argument_list|(
name|ScriptService
operator|.
name|ScriptType
operator|.
name|INLINE
argument_list|,
literal|"testChangingVarsCrossExecution2"
argument_list|,
literal|"python"
argument_list|,
name|compiledScript
argument_list|)
argument_list|,
name|vars
argument_list|)
decl_stmt|;
name|script
operator|.
name|setNextVar
argument_list|(
literal|"value"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|Object
name|o
init|=
name|script
operator|.
name|run
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|Number
operator|)
name|o
operator|)
operator|.
name|intValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|script
operator|.
name|setNextVar
argument_list|(
literal|"value"
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|o
operator|=
name|script
operator|.
name|run
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|Number
operator|)
name|o
operator|)
operator|.
name|intValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

