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
name|HashMap
import|;
end_import

begin_class
DECL|class|ConditionalTests
specifier|public
class|class
name|ConditionalTests
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
literal|2
argument_list|,
name|exec
argument_list|(
literal|"boolean x = true; return x ? 2 : 3;"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|exec
argument_list|(
literal|"boolean x = false; return x ? 2 : 3;"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|exec
argument_list|(
literal|"boolean x = false, y = true; return x&& y ? 2 : 3;"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|exec
argument_list|(
literal|"boolean x = true, y = true; return x&& y ? 2 : 3;"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|exec
argument_list|(
literal|"boolean x = true, y = false; return x || y ? 2 : 3;"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|exec
argument_list|(
literal|"boolean x = false, y = false; return x || y ? 2 : 3;"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testPrecedence
specifier|public
name|void
name|testPrecedence
parameter_list|()
block|{
name|assertEquals
argument_list|(
literal|4
argument_list|,
name|exec
argument_list|(
literal|"boolean x = false, y = true; return x ? (y ? 2 : 3) : 4;"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|exec
argument_list|(
literal|"boolean x = true, y = true; return x ? (y ? 2 : 3) : 4;"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|exec
argument_list|(
literal|"boolean x = true, y = false; return x ? (y ? 2 : 3) : 4;"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|exec
argument_list|(
literal|"boolean x = true, y = true; return x ? y ? 2 : 3 : 4;"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|4
argument_list|,
name|exec
argument_list|(
literal|"boolean x = false, y = true; return x ? y ? 2 : 3 : 4;"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|exec
argument_list|(
literal|"boolean x = true, y = false; return x ? y ? 2 : 3 : 4;"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|exec
argument_list|(
literal|"boolean x = false, y = true; return x ? 2 : y ? 3 : 4;"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|exec
argument_list|(
literal|"boolean x = true, y = false; return x ? 2 : y ? 3 : 4;"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|4
argument_list|,
name|exec
argument_list|(
literal|"boolean x = false, y = false; return x ? 2 : y ? 3 : 4;"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|4
argument_list|,
name|exec
argument_list|(
literal|"boolean x = false, y = false; return (x ? true : y) ? 3 : 4;"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|4
argument_list|,
name|exec
argument_list|(
literal|"boolean x = true, y = false; return (x ? false : y) ? 3 : 4;"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|exec
argument_list|(
literal|"boolean x = false, y = true; return (x ? false : y) ? 3 : 4;"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|exec
argument_list|(
literal|"boolean x = true, y = false; return (x ? false : y) ? (x ? 3 : 4) : x ? 2 : 1;"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|exec
argument_list|(
literal|"boolean x = true, y = false; return (x ? false : y) ? x ? 3 : 4 : x ? 2 : 1;"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|4
argument_list|,
name|exec
argument_list|(
literal|"boolean x = false, y = true; return x ? false : y ? x ? 3 : 4 : x ? 2 : 1;"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testAssignment
specifier|public
name|void
name|testAssignment
parameter_list|()
block|{
name|assertEquals
argument_list|(
literal|4D
argument_list|,
name|exec
argument_list|(
literal|"boolean x = false; double z = x ? 2 : 4.0F; return z;"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|(
name|byte
operator|)
literal|7
argument_list|,
name|exec
argument_list|(
literal|"boolean x = false; int y = 2; byte z = x ? (byte)y : 7; return z;"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|(
name|byte
operator|)
literal|7
argument_list|,
name|exec
argument_list|(
literal|"boolean x = false; int y = 2; byte z = (byte)(x ? y : 7); return z;"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|ArrayList
operator|.
name|class
argument_list|,
name|exec
argument_list|(
literal|"boolean x = false; Object z = x ? new HashMap() : new ArrayList(); return z;"
argument_list|)
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testNullArguments
specifier|public
name|void
name|testNullArguments
parameter_list|()
block|{
name|assertEquals
argument_list|(
literal|null
argument_list|,
name|exec
argument_list|(
literal|"boolean b = false, c = true; Object x; Map y; return b&& c ? x : y;"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|HashMap
operator|.
name|class
argument_list|,
name|exec
argument_list|(
literal|"boolean b = false, c = true; Object x; Map y = new HashMap(); return b&& c ? x : y;"
argument_list|)
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testPromotion
specifier|public
name|void
name|testPromotion
parameter_list|()
block|{
name|assertEquals
argument_list|(
literal|false
argument_list|,
name|exec
argument_list|(
literal|"boolean x = false; boolean y = true; return (x ? 2 : 4.0F) == (y ? 2 : 4.0F);"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|false
argument_list|,
name|exec
argument_list|(
literal|"boolean x = false; boolean y = true; "
operator|+
literal|"return (x ? new HashMap() : new ArrayList()) == (y ? new HashMap() : new ArrayList());"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testIncompatibleAssignment
specifier|public
name|void
name|testIncompatibleAssignment
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
literal|"boolean x = false; byte z = x ? 2 : 4.0F; return z;"
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
literal|"boolean x = false; Map z = x ? 4 : (byte)7; return z;"
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
literal|"boolean x = false; Map z = x ? new HashMap() : new ArrayList(); return z;"
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
literal|"boolean x = false; int y = 2; byte z = x ? y : 7; return z;"
argument_list|)
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

