begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.painless
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|painless
package|;
end_package

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|invoke
operator|.
name|WrongMethodTypeException
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

begin_class
DECL|class|WhenThingsGoWrongTests
specifier|public
class|class
name|WhenThingsGoWrongTests
extends|extends
name|ScriptTestCase
block|{
DECL|method|testNullPointer
specifier|public
name|void
name|testNullPointer
parameter_list|()
block|{
name|expectScriptThrows
argument_list|(
name|NullPointerException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
block|{
name|exec
argument_list|(
literal|"int x = params['missing']; return x;"
argument_list|)
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
block|}
comment|/** test "line numbers" in the bytecode, which are really 1-based offsets */
DECL|method|testLineNumbers
specifier|public
name|void
name|testLineNumbers
parameter_list|()
block|{
comment|// trigger NPE at line 1 of the script
name|NullPointerException
name|exception
init|=
name|expectScriptThrows
argument_list|(
name|NullPointerException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
block|{
name|exec
argument_list|(
literal|"String x = null; boolean y = x.isEmpty();\n"
operator|+
literal|"return y;"
argument_list|)
expr_stmt|;
block|}
argument_list|)
decl_stmt|;
comment|// null deref at x.isEmpty(), the '.' is offset 30 (+1)
name|assertEquals
argument_list|(
literal|30
operator|+
literal|1
argument_list|,
name|exception
operator|.
name|getStackTrace
argument_list|()
index|[
literal|0
index|]
operator|.
name|getLineNumber
argument_list|()
argument_list|)
expr_stmt|;
comment|// trigger NPE at line 2 of the script
name|exception
operator|=
name|expectScriptThrows
argument_list|(
name|NullPointerException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
block|{
name|exec
argument_list|(
literal|"String x = null;\n"
operator|+
literal|"return x.isEmpty();"
argument_list|)
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
comment|// null deref at x.isEmpty(), the '.' is offset 25 (+1)
name|assertEquals
argument_list|(
literal|25
operator|+
literal|1
argument_list|,
name|exception
operator|.
name|getStackTrace
argument_list|()
index|[
literal|0
index|]
operator|.
name|getLineNumber
argument_list|()
argument_list|)
expr_stmt|;
comment|// trigger NPE at line 3 of the script
name|exception
operator|=
name|expectScriptThrows
argument_list|(
name|NullPointerException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
block|{
name|exec
argument_list|(
literal|"String x = null;\n"
operator|+
literal|"String y = x;\n"
operator|+
literal|"return y.isEmpty();"
argument_list|)
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
comment|// null deref at y.isEmpty(), the '.' is offset 39 (+1)
name|assertEquals
argument_list|(
literal|39
operator|+
literal|1
argument_list|,
name|exception
operator|.
name|getStackTrace
argument_list|()
index|[
literal|0
index|]
operator|.
name|getLineNumber
argument_list|()
argument_list|)
expr_stmt|;
comment|// trigger NPE at line 4 in script (inside conditional)
name|exception
operator|=
name|expectScriptThrows
argument_list|(
name|NullPointerException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
block|{
name|exec
argument_list|(
literal|"String x = null;\n"
operator|+
literal|"boolean y = false;\n"
operator|+
literal|"if (!y) {\n"
operator|+
literal|"  y = x.isEmpty();\n"
operator|+
literal|"}\n"
operator|+
literal|"return y;"
argument_list|)
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
comment|// null deref at x.isEmpty(), the '.' is offset 53 (+1)
name|assertEquals
argument_list|(
literal|53
operator|+
literal|1
argument_list|,
name|exception
operator|.
name|getStackTrace
argument_list|()
index|[
literal|0
index|]
operator|.
name|getLineNumber
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testInvalidShift
specifier|public
name|void
name|testInvalidShift
parameter_list|()
block|{
name|expectScriptThrows
argument_list|(
name|ClassCastException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
block|{
name|exec
argument_list|(
literal|"float x = 15F; x<<= 2; return x;"
argument_list|)
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
name|expectScriptThrows
argument_list|(
name|ClassCastException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
block|{
name|exec
argument_list|(
literal|"double x = 15F; x<<= 2; return x;"
argument_list|)
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
block|}
DECL|method|testBogusParameter
specifier|public
name|void
name|testBogusParameter
parameter_list|()
block|{
name|IllegalArgumentException
name|expected
init|=
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
block|{
name|exec
argument_list|(
literal|"return 5;"
argument_list|,
literal|null
argument_list|,
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"bogusParameterKey"
argument_list|,
literal|"bogusParameterValue"
argument_list|)
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|expected
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"Unrecognized compile-time parameter"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testInfiniteLoops
specifier|public
name|void
name|testInfiniteLoops
parameter_list|()
block|{
name|PainlessError
name|expected
init|=
name|expectScriptThrows
argument_list|(
name|PainlessError
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
block|{
name|exec
argument_list|(
literal|"boolean x = true; while (x) {}"
argument_list|)
expr_stmt|;
block|}
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|expected
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"The maximum number of statements that can be executed in a loop has been reached."
argument_list|)
argument_list|)
expr_stmt|;
name|expected
operator|=
name|expectScriptThrows
argument_list|(
name|PainlessError
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
block|{
name|exec
argument_list|(
literal|"while (true) {int y = 5;}"
argument_list|)
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|expected
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"The maximum number of statements that can be executed in a loop has been reached."
argument_list|)
argument_list|)
expr_stmt|;
name|expected
operator|=
name|expectScriptThrows
argument_list|(
name|PainlessError
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
block|{
name|exec
argument_list|(
literal|"while (true) { boolean x = true; while (x) {} }"
argument_list|)
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|expected
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"The maximum number of statements that can be executed in a loop has been reached."
argument_list|)
argument_list|)
expr_stmt|;
name|expected
operator|=
name|expectScriptThrows
argument_list|(
name|PainlessError
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
block|{
name|exec
argument_list|(
literal|"while (true) { boolean x = false; while (x) {} }"
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"should have hit PainlessError"
argument_list|)
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|expected
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"The maximum number of statements that can be executed in a loop has been reached."
argument_list|)
argument_list|)
expr_stmt|;
name|expected
operator|=
name|expectScriptThrows
argument_list|(
name|PainlessError
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
block|{
name|exec
argument_list|(
literal|"boolean x = true; for (;x;) {}"
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"should have hit PainlessError"
argument_list|)
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|expected
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"The maximum number of statements that can be executed in a loop has been reached."
argument_list|)
argument_list|)
expr_stmt|;
name|expected
operator|=
name|expectScriptThrows
argument_list|(
name|PainlessError
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
block|{
name|exec
argument_list|(
literal|"for (;;) {int x = 5;}"
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"should have hit PainlessError"
argument_list|)
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|expected
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"The maximum number of statements that can be executed in a loop has been reached."
argument_list|)
argument_list|)
expr_stmt|;
name|expected
operator|=
name|expectScriptThrows
argument_list|(
name|PainlessError
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
block|{
name|exec
argument_list|(
literal|"def x = true; do {int y = 5;} while (x)"
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"should have hit PainlessError"
argument_list|)
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|expected
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"The maximum number of statements that can be executed in a loop has been reached."
argument_list|)
argument_list|)
expr_stmt|;
name|RuntimeException
name|parseException
init|=
name|expectScriptThrows
argument_list|(
name|RuntimeException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
block|{
name|exec
argument_list|(
literal|"try { int x; } catch (PainlessError error) {}"
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"should have hit ParseException"
argument_list|)
expr_stmt|;
block|}
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|parseException
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"unexpected token ['PainlessError']"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testLoopLimits
specifier|public
name|void
name|testLoopLimits
parameter_list|()
block|{
comment|// right below limit: ok
name|exec
argument_list|(
literal|"for (int x = 0; x< 9999; ++x) {}"
argument_list|)
expr_stmt|;
name|PainlessError
name|expected
init|=
name|expectScriptThrows
argument_list|(
name|PainlessError
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
block|{
name|exec
argument_list|(
literal|"for (int x = 0; x< 10000; ++x) {}"
argument_list|)
expr_stmt|;
block|}
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|expected
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"The maximum number of statements that can be executed in a loop has been reached."
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testSourceLimits
specifier|public
name|void
name|testSourceLimits
parameter_list|()
block|{
specifier|final
name|char
index|[]
name|tooManyChars
init|=
operator|new
name|char
index|[
name|Compiler
operator|.
name|MAXIMUM_SOURCE_LENGTH
operator|+
literal|1
index|]
decl_stmt|;
name|Arrays
operator|.
name|fill
argument_list|(
name|tooManyChars
argument_list|,
literal|'0'
argument_list|)
expr_stmt|;
name|IllegalArgumentException
name|expected
init|=
name|expectScriptThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
block|{
name|exec
argument_list|(
operator|new
name|String
argument_list|(
name|tooManyChars
argument_list|)
argument_list|)
expr_stmt|;
block|}
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|expected
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"Scripts may be no longer than"
argument_list|)
argument_list|)
expr_stmt|;
specifier|final
name|char
index|[]
name|exactlyAtLimit
init|=
operator|new
name|char
index|[
name|Compiler
operator|.
name|MAXIMUM_SOURCE_LENGTH
index|]
decl_stmt|;
name|Arrays
operator|.
name|fill
argument_list|(
name|exactlyAtLimit
argument_list|,
literal|'0'
argument_list|)
expr_stmt|;
comment|// ok
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|exec
argument_list|(
operator|new
name|String
argument_list|(
name|exactlyAtLimit
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testIllegalDynamicMethod
specifier|public
name|void
name|testIllegalDynamicMethod
parameter_list|()
block|{
name|IllegalArgumentException
name|expected
init|=
name|expectScriptThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
block|{
name|exec
argument_list|(
literal|"def x = 'test'; return x.getClass().toString()"
argument_list|)
expr_stmt|;
block|}
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|expected
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"Unable to find dynamic method"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testDynamicNPE
specifier|public
name|void
name|testDynamicNPE
parameter_list|()
block|{
name|expectScriptThrows
argument_list|(
name|NullPointerException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
block|{
name|exec
argument_list|(
literal|"def x = null; return x.toString()"
argument_list|)
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
block|}
DECL|method|testDynamicWrongArgs
specifier|public
name|void
name|testDynamicWrongArgs
parameter_list|()
block|{
name|expectScriptThrows
argument_list|(
name|WrongMethodTypeException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
block|{
name|exec
argument_list|(
literal|"def x = new ArrayList(); return x.get('bogus');"
argument_list|)
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
block|}
DECL|method|testDynamicArrayWrongIndex
specifier|public
name|void
name|testDynamicArrayWrongIndex
parameter_list|()
block|{
name|expectScriptThrows
argument_list|(
name|WrongMethodTypeException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
block|{
name|exec
argument_list|(
literal|"def x = new long[1]; x[0]=1; return x['bogus'];"
argument_list|)
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
block|}
DECL|method|testDynamicListWrongIndex
specifier|public
name|void
name|testDynamicListWrongIndex
parameter_list|()
block|{
name|expectScriptThrows
argument_list|(
name|WrongMethodTypeException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
block|{
name|exec
argument_list|(
literal|"def x = new ArrayList(); x.add('foo'); return x['bogus'];"
argument_list|)
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

