begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
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

begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|invoke
operator|.
name|CallSite
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|invoke
operator|.
name|MethodHandle
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|invoke
operator|.
name|MethodHandles
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|invoke
operator|.
name|MethodType
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
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|ESTestCase
import|;
end_import

begin_class
DECL|class|DefBootstrapTests
specifier|public
class|class
name|DefBootstrapTests
extends|extends
name|ESTestCase
block|{
comment|/** calls toString() on integers, twice */
DECL|method|testOneType
specifier|public
name|void
name|testOneType
parameter_list|()
throws|throws
name|Throwable
block|{
name|CallSite
name|site
init|=
name|DefBootstrap
operator|.
name|bootstrap
argument_list|(
name|MethodHandles
operator|.
name|publicLookup
argument_list|()
argument_list|,
literal|"toString"
argument_list|,
name|MethodType
operator|.
name|methodType
argument_list|(
name|String
operator|.
name|class
argument_list|,
name|Object
operator|.
name|class
argument_list|)
argument_list|,
name|DefBootstrap
operator|.
name|METHOD_CALL
argument_list|,
literal|0L
argument_list|)
decl_stmt|;
name|MethodHandle
name|handle
init|=
name|site
operator|.
name|dynamicInvoker
argument_list|()
decl_stmt|;
name|assertDepthEquals
argument_list|(
name|site
argument_list|,
literal|0
argument_list|)
expr_stmt|;
comment|// invoke with integer, needs lookup
name|assertEquals
argument_list|(
literal|"5"
argument_list|,
operator|(
name|String
operator|)
name|handle
operator|.
name|invokeExact
argument_list|(
operator|(
name|Object
operator|)
literal|5
argument_list|)
argument_list|)
expr_stmt|;
name|assertDepthEquals
argument_list|(
name|site
argument_list|,
literal|1
argument_list|)
expr_stmt|;
comment|// invoked with integer again: should be cached
name|assertEquals
argument_list|(
literal|"6"
argument_list|,
operator|(
name|String
operator|)
name|handle
operator|.
name|invokeExact
argument_list|(
operator|(
name|Object
operator|)
literal|6
argument_list|)
argument_list|)
expr_stmt|;
name|assertDepthEquals
argument_list|(
name|site
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
DECL|method|testTwoTypes
specifier|public
name|void
name|testTwoTypes
parameter_list|()
throws|throws
name|Throwable
block|{
name|CallSite
name|site
init|=
name|DefBootstrap
operator|.
name|bootstrap
argument_list|(
name|MethodHandles
operator|.
name|publicLookup
argument_list|()
argument_list|,
literal|"toString"
argument_list|,
name|MethodType
operator|.
name|methodType
argument_list|(
name|String
operator|.
name|class
argument_list|,
name|Object
operator|.
name|class
argument_list|)
argument_list|,
name|DefBootstrap
operator|.
name|METHOD_CALL
argument_list|,
literal|0L
argument_list|)
decl_stmt|;
name|MethodHandle
name|handle
init|=
name|site
operator|.
name|dynamicInvoker
argument_list|()
decl_stmt|;
name|assertDepthEquals
argument_list|(
name|site
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"5"
argument_list|,
operator|(
name|String
operator|)
name|handle
operator|.
name|invokeExact
argument_list|(
operator|(
name|Object
operator|)
literal|5
argument_list|)
argument_list|)
expr_stmt|;
name|assertDepthEquals
argument_list|(
name|site
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"1.5"
argument_list|,
operator|(
name|String
operator|)
name|handle
operator|.
name|invokeExact
argument_list|(
operator|(
name|Object
operator|)
literal|1.5f
argument_list|)
argument_list|)
expr_stmt|;
name|assertDepthEquals
argument_list|(
name|site
argument_list|,
literal|2
argument_list|)
expr_stmt|;
comment|// both these should be cached
name|assertEquals
argument_list|(
literal|"6"
argument_list|,
operator|(
name|String
operator|)
name|handle
operator|.
name|invokeExact
argument_list|(
operator|(
name|Object
operator|)
literal|6
argument_list|)
argument_list|)
expr_stmt|;
name|assertDepthEquals
argument_list|(
name|site
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"2.5"
argument_list|,
operator|(
name|String
operator|)
name|handle
operator|.
name|invokeExact
argument_list|(
operator|(
name|Object
operator|)
literal|2.5f
argument_list|)
argument_list|)
expr_stmt|;
name|assertDepthEquals
argument_list|(
name|site
argument_list|,
literal|2
argument_list|)
expr_stmt|;
block|}
DECL|method|testTooManyTypes
specifier|public
name|void
name|testTooManyTypes
parameter_list|()
throws|throws
name|Throwable
block|{
comment|// if this changes, test must be rewritten
name|assertEquals
argument_list|(
literal|5
argument_list|,
name|DefBootstrap
operator|.
name|PIC
operator|.
name|MAX_DEPTH
argument_list|)
expr_stmt|;
name|CallSite
name|site
init|=
name|DefBootstrap
operator|.
name|bootstrap
argument_list|(
name|MethodHandles
operator|.
name|publicLookup
argument_list|()
argument_list|,
literal|"toString"
argument_list|,
name|MethodType
operator|.
name|methodType
argument_list|(
name|String
operator|.
name|class
argument_list|,
name|Object
operator|.
name|class
argument_list|)
argument_list|,
name|DefBootstrap
operator|.
name|METHOD_CALL
argument_list|,
literal|0L
argument_list|)
decl_stmt|;
name|MethodHandle
name|handle
init|=
name|site
operator|.
name|dynamicInvoker
argument_list|()
decl_stmt|;
name|assertDepthEquals
argument_list|(
name|site
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"5"
argument_list|,
operator|(
name|String
operator|)
name|handle
operator|.
name|invokeExact
argument_list|(
operator|(
name|Object
operator|)
literal|5
argument_list|)
argument_list|)
expr_stmt|;
name|assertDepthEquals
argument_list|(
name|site
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"1.5"
argument_list|,
operator|(
name|String
operator|)
name|handle
operator|.
name|invokeExact
argument_list|(
operator|(
name|Object
operator|)
literal|1.5f
argument_list|)
argument_list|)
expr_stmt|;
name|assertDepthEquals
argument_list|(
name|site
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"6"
argument_list|,
operator|(
name|String
operator|)
name|handle
operator|.
name|invokeExact
argument_list|(
operator|(
name|Object
operator|)
literal|6L
argument_list|)
argument_list|)
expr_stmt|;
name|assertDepthEquals
argument_list|(
name|site
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"3.2"
argument_list|,
operator|(
name|String
operator|)
name|handle
operator|.
name|invokeExact
argument_list|(
operator|(
name|Object
operator|)
literal|3.2d
argument_list|)
argument_list|)
expr_stmt|;
name|assertDepthEquals
argument_list|(
name|site
argument_list|,
literal|4
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"foo"
argument_list|,
operator|(
name|String
operator|)
name|handle
operator|.
name|invokeExact
argument_list|(
operator|(
name|Object
operator|)
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
name|assertDepthEquals
argument_list|(
name|site
argument_list|,
literal|5
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"c"
argument_list|,
operator|(
name|String
operator|)
name|handle
operator|.
name|invokeExact
argument_list|(
operator|(
name|Object
operator|)
literal|'c'
argument_list|)
argument_list|)
expr_stmt|;
name|assertDepthEquals
argument_list|(
name|site
argument_list|,
literal|5
argument_list|)
expr_stmt|;
block|}
comment|/** test that we revert to the megamorphic classvalue cache and that it works as expected */
DECL|method|testMegamorphic
specifier|public
name|void
name|testMegamorphic
parameter_list|()
throws|throws
name|Throwable
block|{
name|DefBootstrap
operator|.
name|PIC
name|site
init|=
operator|(
name|DefBootstrap
operator|.
name|PIC
operator|)
name|DefBootstrap
operator|.
name|bootstrap
argument_list|(
name|MethodHandles
operator|.
name|publicLookup
argument_list|()
argument_list|,
literal|"size"
argument_list|,
name|MethodType
operator|.
name|methodType
argument_list|(
name|int
operator|.
name|class
argument_list|,
name|Object
operator|.
name|class
argument_list|)
argument_list|,
name|DefBootstrap
operator|.
name|METHOD_CALL
argument_list|,
literal|0L
argument_list|)
decl_stmt|;
name|site
operator|.
name|depth
operator|=
name|DefBootstrap
operator|.
name|PIC
operator|.
name|MAX_DEPTH
expr_stmt|;
comment|// mark megamorphic
name|MethodHandle
name|handle
init|=
name|site
operator|.
name|dynamicInvoker
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
operator|(
name|int
operator|)
name|handle
operator|.
name|invokeExact
argument_list|(
operator|(
name|Object
operator|)
name|Arrays
operator|.
name|asList
argument_list|(
literal|"1"
argument_list|,
literal|"2"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
operator|(
name|int
operator|)
name|handle
operator|.
name|invokeExact
argument_list|(
operator|(
name|Object
operator|)
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"a"
argument_list|,
literal|"b"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
operator|(
name|int
operator|)
name|handle
operator|.
name|invokeExact
argument_list|(
operator|(
name|Object
operator|)
name|Arrays
operator|.
name|asList
argument_list|(
literal|"x"
argument_list|,
literal|"y"
argument_list|,
literal|"z"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
operator|(
name|int
operator|)
name|handle
operator|.
name|invokeExact
argument_list|(
operator|(
name|Object
operator|)
name|Arrays
operator|.
name|asList
argument_list|(
literal|"u"
argument_list|,
literal|"v"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
specifier|final
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|map
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|map
operator|.
name|put
argument_list|(
literal|"x"
argument_list|,
literal|"y"
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
literal|"a"
argument_list|,
literal|"b"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
operator|(
name|int
operator|)
name|handle
operator|.
name|invokeExact
argument_list|(
operator|(
name|Object
operator|)
name|map
argument_list|)
argument_list|)
expr_stmt|;
specifier|final
name|IllegalArgumentException
name|iae
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
name|Integer
operator|.
name|toString
argument_list|(
operator|(
name|int
operator|)
name|handle
operator|.
name|invokeExact
argument_list|(
operator|new
name|Object
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"Unable to find dynamic method [size] with [0] arguments for class [java.lang.Object]."
argument_list|,
name|iae
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Does not fail inside ClassValue.computeValue()"
argument_list|,
name|Arrays
operator|.
name|stream
argument_list|(
name|iae
operator|.
name|getStackTrace
argument_list|()
argument_list|)
operator|.
name|anyMatch
argument_list|(
name|e
lambda|->
block|{
return|return
name|e
operator|.
name|getMethodName
argument_list|()
operator|.
name|equals
argument_list|(
literal|"computeValue"
argument_list|)
operator|&&
name|e
operator|.
name|getClassName
argument_list|()
operator|.
name|startsWith
argument_list|(
literal|"org.elasticsearch.painless.DefBootstrap$PIC$"
argument_list|)
return|;
block|}
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// test operators with null guards
DECL|method|testNullGuardAdd
specifier|public
name|void
name|testNullGuardAdd
parameter_list|()
throws|throws
name|Throwable
block|{
name|DefBootstrap
operator|.
name|MIC
name|site
init|=
operator|(
name|DefBootstrap
operator|.
name|MIC
operator|)
name|DefBootstrap
operator|.
name|bootstrap
argument_list|(
name|MethodHandles
operator|.
name|publicLookup
argument_list|()
argument_list|,
literal|"add"
argument_list|,
name|MethodType
operator|.
name|methodType
argument_list|(
name|Object
operator|.
name|class
argument_list|,
name|Object
operator|.
name|class
argument_list|,
name|Object
operator|.
name|class
argument_list|)
argument_list|,
name|DefBootstrap
operator|.
name|BINARY_OPERATOR
argument_list|,
name|DefBootstrap
operator|.
name|OPERATOR_ALLOWS_NULL
argument_list|)
decl_stmt|;
name|MethodHandle
name|handle
init|=
name|site
operator|.
name|dynamicInvoker
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|"nulltest"
argument_list|,
operator|(
name|Object
operator|)
name|handle
operator|.
name|invokeExact
argument_list|(
operator|(
name|Object
operator|)
literal|null
argument_list|,
operator|(
name|Object
operator|)
literal|"test"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testNullGuardAddWhenCached
specifier|public
name|void
name|testNullGuardAddWhenCached
parameter_list|()
throws|throws
name|Throwable
block|{
name|DefBootstrap
operator|.
name|MIC
name|site
init|=
operator|(
name|DefBootstrap
operator|.
name|MIC
operator|)
name|DefBootstrap
operator|.
name|bootstrap
argument_list|(
name|MethodHandles
operator|.
name|publicLookup
argument_list|()
argument_list|,
literal|"add"
argument_list|,
name|MethodType
operator|.
name|methodType
argument_list|(
name|Object
operator|.
name|class
argument_list|,
name|Object
operator|.
name|class
argument_list|,
name|Object
operator|.
name|class
argument_list|)
argument_list|,
name|DefBootstrap
operator|.
name|BINARY_OPERATOR
argument_list|,
name|DefBootstrap
operator|.
name|OPERATOR_ALLOWS_NULL
argument_list|)
decl_stmt|;
name|MethodHandle
name|handle
init|=
name|site
operator|.
name|dynamicInvoker
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
operator|(
name|Object
operator|)
name|handle
operator|.
name|invokeExact
argument_list|(
operator|(
name|Object
operator|)
literal|1
argument_list|,
operator|(
name|Object
operator|)
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"nulltest"
argument_list|,
operator|(
name|Object
operator|)
name|handle
operator|.
name|invokeExact
argument_list|(
operator|(
name|Object
operator|)
literal|null
argument_list|,
operator|(
name|Object
operator|)
literal|"test"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testNullGuardEq
specifier|public
name|void
name|testNullGuardEq
parameter_list|()
throws|throws
name|Throwable
block|{
name|DefBootstrap
operator|.
name|MIC
name|site
init|=
operator|(
name|DefBootstrap
operator|.
name|MIC
operator|)
name|DefBootstrap
operator|.
name|bootstrap
argument_list|(
name|MethodHandles
operator|.
name|publicLookup
argument_list|()
argument_list|,
literal|"eq"
argument_list|,
name|MethodType
operator|.
name|methodType
argument_list|(
name|boolean
operator|.
name|class
argument_list|,
name|Object
operator|.
name|class
argument_list|,
name|Object
operator|.
name|class
argument_list|)
argument_list|,
name|DefBootstrap
operator|.
name|BINARY_OPERATOR
argument_list|,
name|DefBootstrap
operator|.
name|OPERATOR_ALLOWS_NULL
argument_list|)
decl_stmt|;
name|MethodHandle
name|handle
init|=
name|site
operator|.
name|dynamicInvoker
argument_list|()
decl_stmt|;
name|assertFalse
argument_list|(
operator|(
name|boolean
operator|)
name|handle
operator|.
name|invokeExact
argument_list|(
operator|(
name|Object
operator|)
literal|null
argument_list|,
operator|(
name|Object
operator|)
literal|"test"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
operator|(
name|boolean
operator|)
name|handle
operator|.
name|invokeExact
argument_list|(
operator|(
name|Object
operator|)
literal|null
argument_list|,
operator|(
name|Object
operator|)
literal|null
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testNullGuardEqWhenCached
specifier|public
name|void
name|testNullGuardEqWhenCached
parameter_list|()
throws|throws
name|Throwable
block|{
name|DefBootstrap
operator|.
name|MIC
name|site
init|=
operator|(
name|DefBootstrap
operator|.
name|MIC
operator|)
name|DefBootstrap
operator|.
name|bootstrap
argument_list|(
name|MethodHandles
operator|.
name|publicLookup
argument_list|()
argument_list|,
literal|"eq"
argument_list|,
name|MethodType
operator|.
name|methodType
argument_list|(
name|boolean
operator|.
name|class
argument_list|,
name|Object
operator|.
name|class
argument_list|,
name|Object
operator|.
name|class
argument_list|)
argument_list|,
name|DefBootstrap
operator|.
name|BINARY_OPERATOR
argument_list|,
name|DefBootstrap
operator|.
name|OPERATOR_ALLOWS_NULL
argument_list|)
decl_stmt|;
name|MethodHandle
name|handle
init|=
name|site
operator|.
name|dynamicInvoker
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
operator|(
name|boolean
operator|)
name|handle
operator|.
name|invokeExact
argument_list|(
operator|(
name|Object
operator|)
literal|1
argument_list|,
operator|(
name|Object
operator|)
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
operator|(
name|boolean
operator|)
name|handle
operator|.
name|invokeExact
argument_list|(
operator|(
name|Object
operator|)
literal|null
argument_list|,
operator|(
name|Object
operator|)
literal|"test"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
operator|(
name|boolean
operator|)
name|handle
operator|.
name|invokeExact
argument_list|(
operator|(
name|Object
operator|)
literal|null
argument_list|,
operator|(
name|Object
operator|)
literal|null
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// make sure these operators work without null guards too
comment|// for example, nulls are only legal for + if the other parameter is a String,
comment|// and can be disabled in some circumstances.
DECL|method|testNoNullGuardAdd
specifier|public
name|void
name|testNoNullGuardAdd
parameter_list|()
throws|throws
name|Throwable
block|{
name|DefBootstrap
operator|.
name|MIC
name|site
init|=
operator|(
name|DefBootstrap
operator|.
name|MIC
operator|)
name|DefBootstrap
operator|.
name|bootstrap
argument_list|(
name|MethodHandles
operator|.
name|publicLookup
argument_list|()
argument_list|,
literal|"add"
argument_list|,
name|MethodType
operator|.
name|methodType
argument_list|(
name|Object
operator|.
name|class
argument_list|,
name|int
operator|.
name|class
argument_list|,
name|Object
operator|.
name|class
argument_list|)
argument_list|,
name|DefBootstrap
operator|.
name|BINARY_OPERATOR
argument_list|,
literal|0
argument_list|)
decl_stmt|;
name|MethodHandle
name|handle
init|=
name|site
operator|.
name|dynamicInvoker
argument_list|()
decl_stmt|;
name|expectThrows
argument_list|(
name|NullPointerException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
block|{
name|assertNotNull
argument_list|(
operator|(
name|Object
operator|)
name|handle
operator|.
name|invokeExact
argument_list|(
literal|5
argument_list|,
operator|(
name|Object
operator|)
literal|null
argument_list|)
argument_list|)
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
block|}
DECL|method|testNoNullGuardAddWhenCached
specifier|public
name|void
name|testNoNullGuardAddWhenCached
parameter_list|()
throws|throws
name|Throwable
block|{
name|DefBootstrap
operator|.
name|MIC
name|site
init|=
operator|(
name|DefBootstrap
operator|.
name|MIC
operator|)
name|DefBootstrap
operator|.
name|bootstrap
argument_list|(
name|MethodHandles
operator|.
name|publicLookup
argument_list|()
argument_list|,
literal|"add"
argument_list|,
name|MethodType
operator|.
name|methodType
argument_list|(
name|Object
operator|.
name|class
argument_list|,
name|int
operator|.
name|class
argument_list|,
name|Object
operator|.
name|class
argument_list|)
argument_list|,
name|DefBootstrap
operator|.
name|BINARY_OPERATOR
argument_list|,
literal|0
argument_list|)
decl_stmt|;
name|MethodHandle
name|handle
init|=
name|site
operator|.
name|dynamicInvoker
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
operator|(
name|Object
operator|)
name|handle
operator|.
name|invokeExact
argument_list|(
literal|1
argument_list|,
operator|(
name|Object
operator|)
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|expectThrows
argument_list|(
name|NullPointerException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
block|{
name|assertNotNull
argument_list|(
operator|(
name|Object
operator|)
name|handle
operator|.
name|invokeExact
argument_list|(
literal|5
argument_list|,
operator|(
name|Object
operator|)
literal|null
argument_list|)
argument_list|)
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
block|}
DECL|method|assertDepthEquals
specifier|static
name|void
name|assertDepthEquals
parameter_list|(
name|CallSite
name|site
parameter_list|,
name|int
name|expected
parameter_list|)
block|{
name|DefBootstrap
operator|.
name|PIC
name|dsite
init|=
operator|(
name|DefBootstrap
operator|.
name|PIC
operator|)
name|site
decl_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|dsite
operator|.
name|depth
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

