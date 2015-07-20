begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.script.expression
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|expression
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|expressions
operator|.
name|Bindings
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|expressions
operator|.
name|Expression
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|expressions
operator|.
name|SimpleBindings
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|index
operator|.
name|LeafReaderContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|queries
operator|.
name|function
operator|.
name|FunctionValues
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|queries
operator|.
name|function
operator|.
name|ValueSource
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|search
operator|.
name|Scorer
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
name|lucene
operator|.
name|Lucene
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
name|LeafSearchScript
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
name|ScriptException
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
name|SearchScript
import|;
end_import

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
name|Map
import|;
end_import

begin_comment
comment|/**  * A bridge to evaluate an {@link Expression} against {@link Bindings} in the context  * of a {@link SearchScript}.  */
end_comment

begin_class
DECL|class|ExpressionSearchScript
class|class
name|ExpressionSearchScript
implements|implements
name|SearchScript
block|{
DECL|field|compiledScript
specifier|final
name|CompiledScript
name|compiledScript
decl_stmt|;
DECL|field|bindings
specifier|final
name|SimpleBindings
name|bindings
decl_stmt|;
DECL|field|source
specifier|final
name|ValueSource
name|source
decl_stmt|;
DECL|field|specialValue
specifier|final
name|ReplaceableConstValueSource
name|specialValue
decl_stmt|;
comment|// _value
DECL|field|scorer
name|Scorer
name|scorer
decl_stmt|;
DECL|field|docid
name|int
name|docid
decl_stmt|;
DECL|method|ExpressionSearchScript
name|ExpressionSearchScript
parameter_list|(
name|CompiledScript
name|c
parameter_list|,
name|SimpleBindings
name|b
parameter_list|,
name|ReplaceableConstValueSource
name|v
parameter_list|)
block|{
name|compiledScript
operator|=
name|c
expr_stmt|;
name|bindings
operator|=
name|b
expr_stmt|;
name|source
operator|=
operator|(
operator|(
name|Expression
operator|)
name|compiledScript
operator|.
name|compiled
argument_list|()
operator|)
operator|.
name|getValueSource
argument_list|(
name|bindings
argument_list|)
expr_stmt|;
name|specialValue
operator|=
name|v
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getLeafSearchScript
specifier|public
name|LeafSearchScript
name|getLeafSearchScript
parameter_list|(
specifier|final
name|LeafReaderContext
name|leaf
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|LeafSearchScript
argument_list|()
block|{
name|FunctionValues
name|values
init|=
name|source
operator|.
name|getValues
argument_list|(
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"scorer"
argument_list|,
name|Lucene
operator|.
name|illegalScorer
argument_list|(
literal|"Scores are not available in the current context"
argument_list|)
argument_list|)
argument_list|,
name|leaf
argument_list|)
decl_stmt|;
name|double
name|evaluate
parameter_list|()
block|{
try|try
block|{
return|return
name|values
operator|.
name|doubleVal
argument_list|(
name|docid
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|exception
parameter_list|)
block|{
throw|throw
operator|new
name|ScriptException
argument_list|(
literal|"Error evaluating "
operator|+
name|compiledScript
argument_list|,
name|exception
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|Object
name|run
parameter_list|()
block|{
return|return
operator|new
name|Double
argument_list|(
name|evaluate
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|float
name|runAsFloat
parameter_list|()
block|{
return|return
operator|(
name|float
operator|)
name|evaluate
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|runAsLong
parameter_list|()
block|{
return|return
operator|(
name|long
operator|)
name|evaluate
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|double
name|runAsDouble
parameter_list|()
block|{
return|return
name|evaluate
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|unwrap
parameter_list|(
name|Object
name|value
parameter_list|)
block|{
return|return
name|value
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setDocument
parameter_list|(
name|int
name|d
parameter_list|)
block|{
name|docid
operator|=
name|d
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setScorer
parameter_list|(
name|Scorer
name|s
parameter_list|)
block|{
name|scorer
operator|=
name|s
expr_stmt|;
try|try
block|{
comment|// We have a new binding for the scorer so we need to reset the values
name|values
operator|=
name|source
operator|.
name|getValues
argument_list|(
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"scorer"
argument_list|,
name|scorer
argument_list|)
argument_list|,
name|leaf
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Can't get values using "
operator|+
name|compiledScript
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|setSource
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|source
parameter_list|)
block|{
comment|// noop: expressions don't use source data
block|}
annotation|@
name|Override
specifier|public
name|void
name|setNextVar
parameter_list|(
name|String
name|name
parameter_list|,
name|Object
name|value
parameter_list|)
block|{
assert|assert
operator|(
name|specialValue
operator|!=
literal|null
operator|)
assert|;
comment|// this should only be used for the special "_value" variable used in aggregations
assert|assert
operator|(
name|name
operator|.
name|equals
argument_list|(
literal|"_value"
argument_list|)
operator|)
assert|;
if|if
condition|(
name|value
operator|instanceof
name|Number
condition|)
block|{
name|specialValue
operator|.
name|setValue
argument_list|(
operator|(
operator|(
name|Number
operator|)
name|value
operator|)
operator|.
name|doubleValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|ExpressionScriptExecutionException
argument_list|(
literal|"Cannot use expression with text variable using "
operator|+
name|compiledScript
argument_list|)
throw|;
block|}
block|}
block|}
return|;
block|}
block|}
end_class

end_unit

