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
name|ScorerAware
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
name|Before
import|;
end_import

begin_import
import|import
name|junit
operator|.
name|framework
operator|.
name|AssertionFailedError
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

begin_comment
comment|/**  * Base test case for scripting unit tests.  *<p>  * Typically just asserts the output of {@code exec()}  */
end_comment

begin_class
DECL|class|ScriptTestCase
specifier|public
specifier|abstract
class|class
name|ScriptTestCase
extends|extends
name|ESTestCase
block|{
DECL|field|scriptEngine
specifier|protected
name|PainlessScriptEngineService
name|scriptEngine
decl_stmt|;
annotation|@
name|Before
DECL|method|setup
specifier|public
name|void
name|setup
parameter_list|()
block|{
name|scriptEngine
operator|=
operator|new
name|PainlessScriptEngineService
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
expr_stmt|;
block|}
comment|/** Compiles and returns the result of {@code script} */
DECL|method|exec
specifier|public
name|Object
name|exec
parameter_list|(
name|String
name|script
parameter_list|)
block|{
return|return
name|exec
argument_list|(
name|script
argument_list|,
literal|null
argument_list|)
return|;
block|}
comment|/** Compiles and returns the result of {@code script} with access to {@code vars} */
DECL|method|exec
specifier|public
name|Object
name|exec
parameter_list|(
name|String
name|script
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|vars
parameter_list|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|compilerSettings
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|compilerSettings
operator|.
name|put
argument_list|(
name|CompilerSettings
operator|.
name|PICKY
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
return|return
name|exec
argument_list|(
name|script
argument_list|,
name|vars
argument_list|,
name|compilerSettings
argument_list|,
literal|null
argument_list|)
return|;
block|}
comment|/** Compiles and returns the result of {@code script} with access to {@code vars} and compile-time parameters */
DECL|method|exec
specifier|public
name|Object
name|exec
parameter_list|(
name|String
name|script
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|vars
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|compileParams
parameter_list|,
name|Scorer
name|scorer
parameter_list|)
block|{
name|Object
name|object
init|=
name|scriptEngine
operator|.
name|compile
argument_list|(
literal|null
argument_list|,
name|script
argument_list|,
name|compileParams
argument_list|)
decl_stmt|;
name|CompiledScript
name|compiled
init|=
operator|new
name|CompiledScript
argument_list|(
name|ScriptService
operator|.
name|ScriptType
operator|.
name|INLINE
argument_list|,
name|getTestName
argument_list|()
argument_list|,
literal|"painless"
argument_list|,
name|object
argument_list|)
decl_stmt|;
name|ExecutableScript
name|executableScript
init|=
name|scriptEngine
operator|.
name|executable
argument_list|(
name|compiled
argument_list|,
name|vars
argument_list|)
decl_stmt|;
if|if
condition|(
name|scorer
operator|!=
literal|null
condition|)
block|{
operator|(
operator|(
name|ScorerAware
operator|)
name|executableScript
operator|)
operator|.
name|setScorer
argument_list|(
name|scorer
argument_list|)
expr_stmt|;
block|}
return|return
name|executableScript
operator|.
name|run
argument_list|()
return|;
block|}
comment|/**      * Uses the {@link Debugger} to get the bytecode output for a script and compare      * it against an expected bytecode passed in as a String.      */
DECL|method|assertBytecodeExists
specifier|public
name|void
name|assertBytecodeExists
parameter_list|(
name|String
name|script
parameter_list|,
name|String
name|bytecode
parameter_list|)
block|{
specifier|final
name|String
name|asm
init|=
name|Debugger
operator|.
name|toString
argument_list|(
name|script
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"bytecode not found"
argument_list|,
name|asm
operator|.
name|contains
argument_list|(
name|bytecode
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/** Checks a specific exception class is thrown (boxed inside ScriptException) and returns it. */
DECL|method|expectScriptThrows
specifier|public
specifier|static
parameter_list|<
name|T
extends|extends
name|Throwable
parameter_list|>
name|T
name|expectScriptThrows
parameter_list|(
name|Class
argument_list|<
name|T
argument_list|>
name|expectedType
parameter_list|,
name|ThrowingRunnable
name|runnable
parameter_list|)
block|{
try|try
block|{
name|runnable
operator|.
name|run
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
if|if
condition|(
name|e
operator|instanceof
name|ScriptException
condition|)
block|{
name|e
operator|=
name|e
operator|.
name|getCause
argument_list|()
expr_stmt|;
if|if
condition|(
name|expectedType
operator|.
name|isInstance
argument_list|(
name|e
argument_list|)
condition|)
block|{
return|return
name|expectedType
operator|.
name|cast
argument_list|(
name|e
argument_list|)
return|;
block|}
block|}
else|else
block|{
name|AssertionFailedError
name|assertion
init|=
operator|new
name|AssertionFailedError
argument_list|(
literal|"Expected boxed ScriptException"
argument_list|)
decl_stmt|;
name|assertion
operator|.
name|initCause
argument_list|(
name|e
argument_list|)
expr_stmt|;
throw|throw
name|assertion
throw|;
block|}
name|AssertionFailedError
name|assertion
init|=
operator|new
name|AssertionFailedError
argument_list|(
literal|"Unexpected exception type, expected "
operator|+
name|expectedType
operator|.
name|getSimpleName
argument_list|()
argument_list|)
decl_stmt|;
name|assertion
operator|.
name|initCause
argument_list|(
name|e
argument_list|)
expr_stmt|;
throw|throw
name|assertion
throw|;
block|}
throw|throw
operator|new
name|AssertionFailedError
argument_list|(
literal|"Expected exception "
operator|+
name|expectedType
operator|.
name|getSimpleName
argument_list|()
argument_list|)
throw|;
block|}
block|}
end_class

end_unit

