begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.script.groovy
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|groovy
package|;
end_package

begin_import
import|import
name|groovy
operator|.
name|lang
operator|.
name|Binding
import|;
end_import

begin_import
import|import
name|groovy
operator|.
name|lang
operator|.
name|GroovyClassLoader
import|;
end_import

begin_import
import|import
name|groovy
operator|.
name|lang
operator|.
name|GroovyCodeSource
import|;
end_import

begin_import
import|import
name|groovy
operator|.
name|lang
operator|.
name|Script
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|logging
operator|.
name|log4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|logging
operator|.
name|log4j
operator|.
name|message
operator|.
name|ParameterizedMessage
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|logging
operator|.
name|log4j
operator|.
name|util
operator|.
name|Supplier
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
name|search
operator|.
name|Scorer
import|;
end_import

begin_import
import|import
name|org
operator|.
name|codehaus
operator|.
name|groovy
operator|.
name|ast
operator|.
name|ClassCodeExpressionTransformer
import|;
end_import

begin_import
import|import
name|org
operator|.
name|codehaus
operator|.
name|groovy
operator|.
name|ast
operator|.
name|ClassNode
import|;
end_import

begin_import
import|import
name|org
operator|.
name|codehaus
operator|.
name|groovy
operator|.
name|ast
operator|.
name|Parameter
import|;
end_import

begin_import
import|import
name|org
operator|.
name|codehaus
operator|.
name|groovy
operator|.
name|ast
operator|.
name|expr
operator|.
name|ConstantExpression
import|;
end_import

begin_import
import|import
name|org
operator|.
name|codehaus
operator|.
name|groovy
operator|.
name|ast
operator|.
name|expr
operator|.
name|Expression
import|;
end_import

begin_import
import|import
name|org
operator|.
name|codehaus
operator|.
name|groovy
operator|.
name|classgen
operator|.
name|GeneratorContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|codehaus
operator|.
name|groovy
operator|.
name|control
operator|.
name|CompilationFailedException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|codehaus
operator|.
name|groovy
operator|.
name|control
operator|.
name|CompilePhase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|codehaus
operator|.
name|groovy
operator|.
name|control
operator|.
name|CompilerConfiguration
import|;
end_import

begin_import
import|import
name|org
operator|.
name|codehaus
operator|.
name|groovy
operator|.
name|control
operator|.
name|MultipleCompilationErrorsException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|codehaus
operator|.
name|groovy
operator|.
name|control
operator|.
name|SourceUnit
import|;
end_import

begin_import
import|import
name|org
operator|.
name|codehaus
operator|.
name|groovy
operator|.
name|control
operator|.
name|customizers
operator|.
name|CompilationCustomizer
import|;
end_import

begin_import
import|import
name|org
operator|.
name|codehaus
operator|.
name|groovy
operator|.
name|control
operator|.
name|customizers
operator|.
name|ImportCustomizer
import|;
end_import

begin_import
import|import
name|org
operator|.
name|codehaus
operator|.
name|groovy
operator|.
name|control
operator|.
name|messages
operator|.
name|Message
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|SpecialPermission
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|bootstrap
operator|.
name|BootstrapInfo
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
name|component
operator|.
name|AbstractComponent
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
name|hash
operator|.
name|MessageDigests
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
name|ClassPermission
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
name|ScoreAccessor
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
name|ScriptEngineService
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
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|lookup
operator|.
name|LeafSearchLookup
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
name|io
operator|.
name|PrintWriter
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|StringWriter
import|;
end_import

begin_import
import|import
name|java
operator|.
name|math
operator|.
name|BigDecimal
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|charset
operator|.
name|StandardCharsets
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|AccessControlContext
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|AccessController
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|PrivilegedAction
import|;
end_import

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
import|import static
name|java
operator|.
name|util
operator|.
name|Collections
operator|.
name|emptyList
import|;
end_import

begin_comment
comment|/**  * Provides the infrastructure for Groovy as a scripting language for Elasticsearch  */
end_comment

begin_class
DECL|class|GroovyScriptEngineService
specifier|public
class|class
name|GroovyScriptEngineService
extends|extends
name|AbstractComponent
implements|implements
name|ScriptEngineService
block|{
comment|/**      * The name of the scripting engine/language.      */
DECL|field|NAME
specifier|public
specifier|static
specifier|final
name|String
name|NAME
init|=
literal|"groovy"
decl_stmt|;
comment|/**      * The name of the Groovy compiler setting to use associated with activating<code>invokedynamic</code> support.      */
DECL|field|GROOVY_INDY_SETTING_NAME
specifier|public
specifier|static
specifier|final
name|String
name|GROOVY_INDY_SETTING_NAME
init|=
literal|"indy"
decl_stmt|;
comment|/**      * Classloader used as a parent classloader for all Groovy scripts      */
DECL|field|loader
specifier|private
specifier|final
name|ClassLoader
name|loader
decl_stmt|;
DECL|method|GroovyScriptEngineService
specifier|public
name|GroovyScriptEngineService
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|deprecationLogger
operator|.
name|deprecated
argument_list|(
literal|"[groovy] scripts are deprecated, use [painless] scripts instead"
argument_list|)
expr_stmt|;
comment|// Creates the classloader here in order to isolate Groovy-land code
specifier|final
name|SecurityManager
name|sm
init|=
name|System
operator|.
name|getSecurityManager
argument_list|()
decl_stmt|;
if|if
condition|(
name|sm
operator|!=
literal|null
condition|)
block|{
name|sm
operator|.
name|checkPermission
argument_list|(
operator|new
name|SpecialPermission
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|loader
operator|=
name|AccessController
operator|.
name|doPrivileged
argument_list|(
call|(
name|PrivilegedAction
argument_list|<
name|ClassLoader
argument_list|>
call|)
argument_list|()
operator|->
block|{
comment|// snapshot our context (which has permissions for classes), since the script has none
name|AccessControlContext
name|context
operator|=
name|AccessController
operator|.
name|getContext
argument_list|()
block|;
return|return
operator|new
name|ClassLoader
argument_list|(
name|getClass
argument_list|()
operator|.
name|getClassLoader
argument_list|()
argument_list|)
block|{
annotation|@
name|Override
specifier|protected
name|Class
argument_list|<
name|?
argument_list|>
name|loadClass
parameter_list|(
name|String
name|name
parameter_list|,
name|boolean
name|resolve
parameter_list|)
throws|throws
name|ClassNotFoundException
block|{
if|if
condition|(
name|sm
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|context
operator|.
name|checkPermission
argument_list|(
operator|new
name|ClassPermission
argument_list|(
name|name
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SecurityException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ClassNotFoundException
argument_list|(
name|name
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
return|return
name|super
operator|.
name|loadClass
argument_list|(
name|name
argument_list|,
name|resolve
argument_list|)
return|;
block|}
end_class

begin_empty_stmt
unit|};         })
empty_stmt|;
end_empty_stmt

begin_function
unit|}      @
name|Override
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
comment|// Nothing to do here
block|}
end_function

begin_function
annotation|@
name|Override
DECL|method|getType
specifier|public
name|String
name|getType
parameter_list|()
block|{
return|return
name|NAME
return|;
block|}
end_function

begin_function
annotation|@
name|Override
DECL|method|getExtension
specifier|public
name|String
name|getExtension
parameter_list|()
block|{
return|return
name|NAME
return|;
block|}
end_function

begin_function
annotation|@
name|Override
DECL|method|compile
specifier|public
name|Object
name|compile
parameter_list|(
name|String
name|scriptName
parameter_list|,
name|String
name|scriptSource
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
comment|// Create the script class name
name|String
name|className
init|=
name|MessageDigests
operator|.
name|toHexString
argument_list|(
name|MessageDigests
operator|.
name|sha1
argument_list|()
operator|.
name|digest
argument_list|(
name|scriptSource
operator|.
name|getBytes
argument_list|(
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
specifier|final
name|SecurityManager
name|sm
init|=
name|System
operator|.
name|getSecurityManager
argument_list|()
decl_stmt|;
if|if
condition|(
name|sm
operator|!=
literal|null
condition|)
block|{
name|sm
operator|.
name|checkPermission
argument_list|(
operator|new
name|SpecialPermission
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|AccessController
operator|.
name|doPrivileged
argument_list|(
call|(
name|PrivilegedAction
argument_list|<
name|Object
argument_list|>
call|)
argument_list|()
operator|->
block|{
try|try
block|{
name|GroovyCodeSource
name|codeSource
init|=
operator|new
name|GroovyCodeSource
argument_list|(
name|scriptSource
argument_list|,
name|className
argument_list|,
name|BootstrapInfo
operator|.
name|UNTRUSTED_CODEBASE
argument_list|)
decl_stmt|;
name|codeSource
operator|.
name|setCachable
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|CompilerConfiguration
name|configuration
init|=
operator|new
name|CompilerConfiguration
argument_list|()
operator|.
name|addCompilationCustomizers
argument_list|(
operator|new
name|ImportCustomizer
argument_list|()
operator|.
name|addStarImports
argument_list|(
literal|"org.joda.time"
argument_list|)
operator|.
name|addStaticStars
argument_list|(
literal|"java.lang.Math"
argument_list|)
argument_list|)
operator|.
name|addCompilationCustomizers
argument_list|(
operator|new
name|GroovyBigDecimalTransformer
argument_list|(
name|CompilePhase
operator|.
name|CONVERSION
argument_list|)
argument_list|)
decl_stmt|;
comment|// always enable invokeDynamic, not the crazy softreference-based stuff
name|configuration
operator|.
name|getOptimizationOptions
argument_list|()
operator|.
name|put
argument_list|(
name|GROOVY_INDY_SETTING_NAME
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|GroovyClassLoader
name|groovyClassLoader
init|=
operator|new
name|GroovyClassLoader
argument_list|(
name|loader
argument_list|,
name|configuration
argument_list|)
decl_stmt|;
return|return
name|groovyClassLoader
operator|.
name|parseClass
argument_list|(
name|codeSource
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
if|if
condition|(
name|logger
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"Exception compiling Groovy script:"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
throw|throw
name|convertToScriptException
argument_list|(
literal|"Error compiling script "
operator|+
name|className
argument_list|,
name|scriptSource
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
end_function

begin_empty_stmt
unit|)
empty_stmt|;
end_empty_stmt

begin_comment
unit|}
comment|/**      * Return a script object with the given vars from the compiled script object      */
end_comment

begin_expr_stmt
unit|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|method|createScript
specifier|private
name|Script
name|createScript
argument_list|(
name|Object
name|compiledScript
argument_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|vars
argument_list|)
throws|throws
name|ReflectiveOperationException
block|{
name|Class
argument_list|<
name|?
argument_list|>
name|scriptClass
operator|=
operator|(
name|Class
argument_list|<
name|?
argument_list|>
operator|)
name|compiledScript
expr_stmt|;
end_expr_stmt

begin_decl_stmt
name|Script
name|scriptObject
init|=
operator|(
name|Script
operator|)
name|scriptClass
operator|.
name|getConstructor
argument_list|()
operator|.
name|newInstance
argument_list|()
decl_stmt|;
end_decl_stmt

begin_decl_stmt
name|Binding
name|binding
init|=
operator|new
name|Binding
argument_list|()
decl_stmt|;
end_decl_stmt

begin_expr_stmt
name|binding
operator|.
name|getVariables
argument_list|()
operator|.
name|putAll
argument_list|(
name|vars
argument_list|)
expr_stmt|;
end_expr_stmt

begin_expr_stmt
name|scriptObject
operator|.
name|setBinding
argument_list|(
name|binding
argument_list|)
expr_stmt|;
end_expr_stmt

begin_return
return|return
name|scriptObject
return|;
end_return

begin_function
unit|}      @
name|Override
DECL|method|executable
specifier|public
name|ExecutableScript
name|executable
parameter_list|(
name|CompiledScript
name|compiledScript
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
name|deprecationLogger
operator|.
name|deprecated
argument_list|(
literal|"[groovy] scripts are deprecated, use [painless] scripts instead"
argument_list|)
expr_stmt|;
try|try
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|allVars
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
if|if
condition|(
name|vars
operator|!=
literal|null
condition|)
block|{
name|allVars
operator|.
name|putAll
argument_list|(
name|vars
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|GroovyScript
argument_list|(
name|compiledScript
argument_list|,
name|createScript
argument_list|(
name|compiledScript
operator|.
name|compiled
argument_list|()
argument_list|,
name|allVars
argument_list|)
argument_list|,
name|this
operator|.
name|logger
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|ReflectiveOperationException
name|e
parameter_list|)
block|{
throw|throw
name|convertToScriptException
argument_list|(
literal|"Failed to build executable script"
argument_list|,
name|compiledScript
operator|.
name|name
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
end_function

begin_function
annotation|@
name|Override
DECL|method|search
specifier|public
name|SearchScript
name|search
parameter_list|(
specifier|final
name|CompiledScript
name|compiledScript
parameter_list|,
specifier|final
name|SearchLookup
name|lookup
parameter_list|,
annotation|@
name|Nullable
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|vars
parameter_list|)
block|{
name|deprecationLogger
operator|.
name|deprecated
argument_list|(
literal|"[groovy] scripts are deprecated, use [painless] scripts instead"
argument_list|)
expr_stmt|;
return|return
operator|new
name|SearchScript
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|LeafSearchScript
name|getLeafSearchScript
parameter_list|(
name|LeafReaderContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|LeafSearchLookup
name|leafLookup
init|=
name|lookup
operator|.
name|getLeafSearchLookup
argument_list|(
name|context
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|allVars
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|allVars
operator|.
name|putAll
argument_list|(
name|leafLookup
operator|.
name|asMap
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|vars
operator|!=
literal|null
condition|)
block|{
name|allVars
operator|.
name|putAll
argument_list|(
name|vars
argument_list|)
expr_stmt|;
block|}
name|Script
name|scriptObject
decl_stmt|;
try|try
block|{
name|scriptObject
operator|=
name|createScript
argument_list|(
name|compiledScript
operator|.
name|compiled
argument_list|()
argument_list|,
name|allVars
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ReflectiveOperationException
name|e
parameter_list|)
block|{
throw|throw
name|convertToScriptException
argument_list|(
literal|"Failed to build search script"
argument_list|,
name|compiledScript
operator|.
name|name
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
return|return
operator|new
name|GroovyScript
argument_list|(
name|compiledScript
argument_list|,
name|scriptObject
argument_list|,
name|leafLookup
argument_list|,
name|logger
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|needsScores
parameter_list|()
block|{
comment|// TODO: can we reliably know if a groovy script makes use of _score
return|return
literal|true
return|;
block|}
block|}
return|;
block|}
end_function

begin_comment
comment|/**      * Converts a {@link Throwable} to a {@link ScriptException}      */
end_comment

begin_function
DECL|method|convertToScriptException
specifier|private
name|ScriptException
name|convertToScriptException
parameter_list|(
name|String
name|message
parameter_list|,
name|String
name|source
parameter_list|,
name|Throwable
name|cause
parameter_list|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|stack
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
if|if
condition|(
name|cause
operator|instanceof
name|MultipleCompilationErrorsException
condition|)
block|{
annotation|@
name|SuppressWarnings
argument_list|(
block|{
literal|"unchecked"
block|}
argument_list|)
name|List
argument_list|<
name|Message
argument_list|>
name|errors
init|=
call|(
name|List
argument_list|<
name|Message
argument_list|>
call|)
argument_list|(
operator|(
name|MultipleCompilationErrorsException
operator|)
name|cause
argument_list|)
operator|.
name|getErrorCollector
argument_list|()
operator|.
name|getErrors
argument_list|()
decl_stmt|;
for|for
control|(
name|Message
name|error
range|:
name|errors
control|)
block|{
try|try
init|(
name|StringWriter
name|writer
init|=
operator|new
name|StringWriter
argument_list|()
init|)
block|{
name|error
operator|.
name|write
argument_list|(
operator|new
name|PrintWriter
argument_list|(
name|writer
argument_list|)
argument_list|)
expr_stmt|;
name|stack
operator|.
name|add
argument_list|(
name|writer
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e1
parameter_list|)
block|{
name|logger
operator|.
name|error
argument_list|(
literal|"failed to write compilation error message to the stack"
argument_list|,
name|e1
argument_list|)
expr_stmt|;
block|}
block|}
block|}
elseif|else
if|if
condition|(
name|cause
operator|instanceof
name|CompilationFailedException
condition|)
block|{
name|CompilationFailedException
name|error
init|=
operator|(
name|CompilationFailedException
operator|)
name|cause
decl_stmt|;
name|stack
operator|.
name|add
argument_list|(
name|error
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
throw|throw
operator|new
name|ScriptException
argument_list|(
name|message
argument_list|,
name|cause
argument_list|,
name|stack
argument_list|,
name|source
argument_list|,
name|NAME
argument_list|)
throw|;
block|}
end_function

begin_class
DECL|class|GroovyScript
specifier|public
specifier|static
specifier|final
class|class
name|GroovyScript
implements|implements
name|ExecutableScript
implements|,
name|LeafSearchScript
block|{
DECL|field|compiledScript
specifier|private
specifier|final
name|CompiledScript
name|compiledScript
decl_stmt|;
DECL|field|script
specifier|private
specifier|final
name|Script
name|script
decl_stmt|;
DECL|field|lookup
specifier|private
specifier|final
name|LeafSearchLookup
name|lookup
decl_stmt|;
DECL|field|variables
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|variables
decl_stmt|;
DECL|field|logger
specifier|private
specifier|final
name|Logger
name|logger
decl_stmt|;
DECL|method|GroovyScript
specifier|public
name|GroovyScript
parameter_list|(
name|CompiledScript
name|compiledScript
parameter_list|,
name|Script
name|script
parameter_list|,
name|Logger
name|logger
parameter_list|)
block|{
name|this
argument_list|(
name|compiledScript
argument_list|,
name|script
argument_list|,
literal|null
argument_list|,
name|logger
argument_list|)
expr_stmt|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|method|GroovyScript
specifier|public
name|GroovyScript
parameter_list|(
name|CompiledScript
name|compiledScript
parameter_list|,
name|Script
name|script
parameter_list|,
annotation|@
name|Nullable
name|LeafSearchLookup
name|lookup
parameter_list|,
name|Logger
name|logger
parameter_list|)
block|{
name|this
operator|.
name|compiledScript
operator|=
name|compiledScript
expr_stmt|;
name|this
operator|.
name|script
operator|=
name|script
expr_stmt|;
name|this
operator|.
name|lookup
operator|=
name|lookup
expr_stmt|;
name|this
operator|.
name|logger
operator|=
name|logger
expr_stmt|;
name|this
operator|.
name|variables
operator|=
name|script
operator|.
name|getBinding
argument_list|()
operator|.
name|getVariables
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|setScorer
specifier|public
name|void
name|setScorer
parameter_list|(
name|Scorer
name|scorer
parameter_list|)
block|{
name|this
operator|.
name|variables
operator|.
name|put
argument_list|(
literal|"_score"
argument_list|,
operator|new
name|ScoreAccessor
argument_list|(
name|scorer
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|setDocument
specifier|public
name|void
name|setDocument
parameter_list|(
name|int
name|doc
parameter_list|)
block|{
if|if
condition|(
name|lookup
operator|!=
literal|null
condition|)
block|{
name|lookup
operator|.
name|setDocument
argument_list|(
name|doc
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|setNextVar
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
name|variables
operator|.
name|put
argument_list|(
name|name
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|setSource
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
if|if
condition|(
name|lookup
operator|!=
literal|null
condition|)
block|{
name|lookup
operator|.
name|source
argument_list|()
operator|.
name|setSource
argument_list|(
name|source
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|run
specifier|public
name|Object
name|run
parameter_list|()
block|{
try|try
block|{
comment|// NOTE: we truncate the stack because IndyInterface has security issue (needs getClassLoader)
comment|// we don't do a security check just as a tradeoff, it cannot really escalate to anything.
return|return
name|AccessController
operator|.
name|doPrivileged
argument_list|(
operator|(
name|PrivilegedAction
argument_list|<
name|Object
argument_list|>
operator|)
name|script
operator|::
name|run
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|AssertionError
name|ae
parameter_list|)
block|{
comment|// Groovy asserts are not java asserts, and cannot be disabled, so we do a best-effort trying to determine if this is a
comment|// Groovy assert (in which case we wrap it and throw), or a real Java assert, in which case we rethrow it as-is, likely
comment|// resulting in the uncaughtExceptionHandler handling it.
specifier|final
name|StackTraceElement
index|[]
name|elements
init|=
name|ae
operator|.
name|getStackTrace
argument_list|()
decl_stmt|;
if|if
condition|(
name|elements
operator|.
name|length
operator|>
literal|0
operator|&&
literal|"org.codehaus.groovy.runtime.InvokerHelper"
operator|.
name|equals
argument_list|(
name|elements
index|[
literal|0
index|]
operator|.
name|getClassName
argument_list|()
argument_list|)
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
call|(
name|Supplier
argument_list|<
name|?
argument_list|>
call|)
argument_list|()
operator|->
operator|new
name|ParameterizedMessage
argument_list|(
literal|"failed to run {}"
argument_list|,
name|compiledScript
argument_list|)
argument_list|,
name|ae
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|ScriptException
argument_list|(
literal|"Error evaluating "
operator|+
name|compiledScript
operator|.
name|name
argument_list|()
argument_list|,
name|ae
argument_list|,
name|emptyList
argument_list|()
argument_list|,
literal|""
argument_list|,
name|compiledScript
operator|.
name|lang
argument_list|()
argument_list|)
throw|;
block|}
throw|throw
name|ae
throw|;
block|}
catch|catch
parameter_list|(
name|Exception
decl||
name|NoClassDefFoundError
name|e
parameter_list|)
block|{
name|logger
operator|.
name|trace
argument_list|(
call|(
name|Supplier
argument_list|<
name|?
argument_list|>
call|)
argument_list|()
operator|->
operator|new
name|ParameterizedMessage
argument_list|(
literal|"failed to run {}"
argument_list|,
name|compiledScript
argument_list|)
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|ScriptException
argument_list|(
literal|"Error evaluating "
operator|+
name|compiledScript
operator|.
name|name
argument_list|()
argument_list|,
name|e
argument_list|,
name|emptyList
argument_list|()
argument_list|,
literal|""
argument_list|,
name|compiledScript
operator|.
name|lang
argument_list|()
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
DECL|method|runAsLong
specifier|public
name|long
name|runAsLong
parameter_list|()
block|{
return|return
operator|(
operator|(
name|Number
operator|)
name|run
argument_list|()
operator|)
operator|.
name|longValue
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|runAsDouble
specifier|public
name|double
name|runAsDouble
parameter_list|()
block|{
return|return
operator|(
operator|(
name|Number
operator|)
name|run
argument_list|()
operator|)
operator|.
name|doubleValue
argument_list|()
return|;
block|}
block|}
end_class

begin_comment
comment|/**      * A compilation customizer that is used to transform a number like 1.23,      * which would normally be a BigDecimal, into a double value.      */
end_comment

begin_class
DECL|class|GroovyBigDecimalTransformer
specifier|private
class|class
name|GroovyBigDecimalTransformer
extends|extends
name|CompilationCustomizer
block|{
DECL|method|GroovyBigDecimalTransformer
specifier|private
name|GroovyBigDecimalTransformer
parameter_list|(
name|CompilePhase
name|phase
parameter_list|)
block|{
name|super
argument_list|(
name|phase
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|call
specifier|public
name|void
name|call
parameter_list|(
specifier|final
name|SourceUnit
name|source
parameter_list|,
specifier|final
name|GeneratorContext
name|context
parameter_list|,
specifier|final
name|ClassNode
name|classNode
parameter_list|)
throws|throws
name|CompilationFailedException
block|{
operator|new
name|BigDecimalExpressionTransformer
argument_list|(
name|source
argument_list|)
operator|.
name|visitClass
argument_list|(
name|classNode
argument_list|)
expr_stmt|;
block|}
block|}
end_class

begin_comment
comment|/**      * Groovy expression transformer that converts BigDecimals to doubles      */
end_comment

begin_class
DECL|class|BigDecimalExpressionTransformer
specifier|private
class|class
name|BigDecimalExpressionTransformer
extends|extends
name|ClassCodeExpressionTransformer
block|{
DECL|field|source
specifier|private
specifier|final
name|SourceUnit
name|source
decl_stmt|;
DECL|method|BigDecimalExpressionTransformer
specifier|private
name|BigDecimalExpressionTransformer
parameter_list|(
name|SourceUnit
name|source
parameter_list|)
block|{
name|this
operator|.
name|source
operator|=
name|source
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getSourceUnit
specifier|protected
name|SourceUnit
name|getSourceUnit
parameter_list|()
block|{
return|return
name|this
operator|.
name|source
return|;
block|}
annotation|@
name|Override
DECL|method|transform
specifier|public
name|Expression
name|transform
parameter_list|(
name|Expression
name|expr
parameter_list|)
block|{
name|Expression
name|newExpr
init|=
name|expr
decl_stmt|;
if|if
condition|(
name|expr
operator|instanceof
name|ConstantExpression
condition|)
block|{
name|ConstantExpression
name|constExpr
init|=
operator|(
name|ConstantExpression
operator|)
name|expr
decl_stmt|;
name|Object
name|val
init|=
name|constExpr
operator|.
name|getValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|val
operator|!=
literal|null
operator|&&
name|val
operator|instanceof
name|BigDecimal
condition|)
block|{
name|newExpr
operator|=
operator|new
name|ConstantExpression
argument_list|(
operator|(
operator|(
name|BigDecimal
operator|)
name|val
operator|)
operator|.
name|doubleValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|super
operator|.
name|transform
argument_list|(
name|newExpr
argument_list|)
return|;
block|}
block|}
end_class

unit|}
end_unit

