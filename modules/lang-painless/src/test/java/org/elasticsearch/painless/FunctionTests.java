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

begin_class
DECL|class|FunctionTests
specifier|public
class|class
name|FunctionTests
extends|extends
name|ScriptTestCase
block|{
DECL|method|testBasic
specifier|public
name|void
name|testBasic
parameter_list|()
block|{
name|assertEquals
argument_list|(
literal|5
argument_list|,
name|exec
argument_list|(
literal|"int get() {5;} get()"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testReference
specifier|public
name|void
name|testReference
parameter_list|()
block|{
name|assertEquals
argument_list|(
literal|5
argument_list|,
name|exec
argument_list|(
literal|"void get(int[] x) {x[0] = 5;} int[] y = new int[1]; y[0] = 1; get(y); y[0]"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testConcat
specifier|public
name|void
name|testConcat
parameter_list|()
block|{
name|assertEquals
argument_list|(
literal|"xyxy"
argument_list|,
name|exec
argument_list|(
literal|"String catcat(String single) {single + single;} catcat('xy')"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testMultiArgs
specifier|public
name|void
name|testMultiArgs
parameter_list|()
block|{
name|assertEquals
argument_list|(
literal|5
argument_list|,
name|exec
argument_list|(
literal|"int add(int x, int y) {return x + y;} int x = 1, y = 2; add(add(x, x), add(x, y))"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testMultiFuncs
specifier|public
name|void
name|testMultiFuncs
parameter_list|()
block|{
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|exec
argument_list|(
literal|"int add(int x, int y) {return x + y;} int sub(int x, int y) {return x - y;} add(2, sub(3, 4))"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|exec
argument_list|(
literal|"int sub2(int x, int y) {sub(x, y) - y;} int sub(int x, int y) {return x - y;} sub2(5, 1)"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testRecursion
specifier|public
name|void
name|testRecursion
parameter_list|()
block|{
name|assertEquals
argument_list|(
literal|55
argument_list|,
name|exec
argument_list|(
literal|"int fib(int n) {if (n<= 1) return n; else return fib(n-1) + fib(n-2);} fib(10)"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testEmpty
specifier|public
name|void
name|testEmpty
parameter_list|()
block|{
name|Exception
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
literal|"void test(int x) {} test()"
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
literal|"Cannot generate an empty function"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testDuplicates
specifier|public
name|void
name|testDuplicates
parameter_list|()
block|{
name|Exception
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
literal|"void test(int x) {x = 2;} void test(def y) {y = 3;} test()"
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
literal|"Duplicate functions"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testInfiniteLoop
specifier|public
name|void
name|testInfiniteLoop
parameter_list|()
block|{
name|Error
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
literal|"void test() {boolean x = true; while (x) {}} test()"
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
block|}
end_class

end_unit
