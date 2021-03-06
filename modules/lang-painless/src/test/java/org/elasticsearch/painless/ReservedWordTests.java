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

begin_comment
comment|/** Tests for special reserved words such as _score */
end_comment

begin_class
DECL|class|ReservedWordTests
specifier|public
class|class
name|ReservedWordTests
extends|extends
name|ScriptTestCase
block|{
comment|/** check that we can't declare a variable of _score, its really reserved! */
DECL|method|testScoreVar
specifier|public
name|void
name|testScoreVar
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
literal|"int _score = 5; return _score;"
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
literal|"Variable [_score] is already defined"
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/** check that we can't write to _score, its read-only! */
DECL|method|testScoreStore
specifier|public
name|void
name|testScoreStore
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
literal|"_score = 5; return _score;"
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
literal|"Variable [_score] is read-only"
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/** check that we can't declare a variable of doc, its really reserved! */
DECL|method|testDocVar
specifier|public
name|void
name|testDocVar
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
literal|"int doc = 5; return doc;"
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
literal|"Variable [doc] is already defined"
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/** check that we can't write to doc, its read-only! */
DECL|method|testDocStore
specifier|public
name|void
name|testDocStore
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
literal|"doc = 5; return doc;"
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
literal|"Variable [doc] is read-only"
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/** check that we can't declare a variable of ctx, its really reserved! */
DECL|method|testCtxVar
specifier|public
name|void
name|testCtxVar
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
literal|"int ctx = 5; return ctx;"
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
literal|"Variable [ctx] is already defined"
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/** check that we can't write to ctx, its read-only! */
DECL|method|testCtxStore
specifier|public
name|void
name|testCtxStore
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
literal|"ctx = 5; return ctx;"
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
literal|"Variable [ctx] is read-only"
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/** check that we can modify its contents though */
DECL|method|testCtxStoreMap
specifier|public
name|void
name|testCtxStoreMap
parameter_list|()
block|{
name|assertEquals
argument_list|(
literal|5
argument_list|,
name|exec
argument_list|(
literal|"ctx.foo = 5; return ctx.foo;"
argument_list|,
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"ctx"
argument_list|,
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|()
argument_list|)
argument_list|,
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/** check that we can't declare a variable of _value, its really reserved! */
DECL|method|testAggregationValueVar
specifier|public
name|void
name|testAggregationValueVar
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
literal|"int _value = 5; return _value;"
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
literal|"Variable [_value] is already defined"
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/** check that we can't write to _value, its read-only! */
DECL|method|testAggregationValueStore
specifier|public
name|void
name|testAggregationValueStore
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
literal|"_value = 5; return _value;"
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
literal|"Variable [_value] is read-only"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

