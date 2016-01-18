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
name|inject
operator|.
name|Inject
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
name|logging
operator|.
name|ESLogger
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
DECL|field|loader
specifier|private
specifier|final
name|GroovyClassLoader
name|loader
decl_stmt|;
annotation|@
name|Inject
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
name|ImportCustomizer
name|imports
init|=
operator|new
name|ImportCustomizer
argument_list|()
decl_stmt|;
name|imports
operator|.
name|addStarImports
argument_list|(
literal|"org.joda.time"
argument_list|)
expr_stmt|;
name|imports
operator|.
name|addStaticStars
argument_list|(
literal|"java.lang.Math"
argument_list|)
expr_stmt|;
name|CompilerConfiguration
name|config
init|=
operator|new
name|CompilerConfiguration
argument_list|()
decl_stmt|;
name|config
operator|.
name|addCompilationCustomizers
argument_list|(
name|imports
argument_list|)
expr_stmt|;
comment|// Add BigDecimal -> Double transformer
name|config
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
expr_stmt|;
comment|// always enable invokeDynamic, not the crazy softreference-based stuff
name|config
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
comment|// Groovy class loader to isolate Groovy-land code
comment|// classloader created here
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
operator|new
name|PrivilegedAction
argument_list|<
name|GroovyClassLoader
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|GroovyClassLoader
name|run
parameter_list|()
block|{
comment|// snapshot our context (which has permissions for classes), since the script has none
specifier|final
name|AccessControlContext
name|engineContext
init|=
name|AccessController
operator|.
name|getContext
argument_list|()
decl_stmt|;
return|return
operator|new
name|GroovyClassLoader
argument_list|(
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
name|engineContext
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
block|}
argument_list|,
name|config
argument_list|)
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
block|{
name|loader
operator|.
name|clearCache
argument_list|()
expr_stmt|;
comment|// close classloader here (why do we do this?)
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
name|AccessController
operator|.
name|doPrivileged
argument_list|(
operator|new
name|PrivilegedAction
argument_list|<
name|Void
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Void
name|run
parameter_list|()
block|{
try|try
block|{
name|loader
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"Unable to close Groovy loader"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
return|return
literal|null
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|scriptRemoved
specifier|public
name|void
name|scriptRemoved
parameter_list|(
annotation|@
name|Nullable
name|CompiledScript
name|script
parameter_list|)
block|{
comment|// script could be null, meaning the script has already been garbage collected
if|if
condition|(
name|script
operator|==
literal|null
operator|||
name|NAME
operator|.
name|equals
argument_list|(
name|script
operator|.
name|lang
argument_list|()
argument_list|)
condition|)
block|{
comment|// Clear the cache, this removes old script versions from the
comment|// cache to prevent running out of PermGen space
name|loader
operator|.
name|clearCache
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|types
specifier|public
name|String
index|[]
name|types
parameter_list|()
block|{
return|return
operator|new
name|String
index|[]
block|{
name|NAME
block|}
return|;
block|}
annotation|@
name|Override
DECL|method|extensions
specifier|public
name|String
index|[]
name|extensions
parameter_list|()
block|{
return|return
operator|new
name|String
index|[]
block|{
name|NAME
block|}
return|;
block|}
annotation|@
name|Override
DECL|method|sandboxed
specifier|public
name|boolean
name|sandboxed
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
DECL|method|compile
specifier|public
name|Object
name|compile
parameter_list|(
name|String
name|script
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
try|try
block|{
comment|// we reuse classloader, so do a security check just in case.
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
name|String
name|fake
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
name|script
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
comment|// same logic as GroovyClassLoader.parseClass() but with a different codesource string:
return|return
name|AccessController
operator|.
name|doPrivileged
argument_list|(
operator|new
name|PrivilegedAction
argument_list|<
name|Object
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Class
argument_list|<
name|?
argument_list|>
name|run
parameter_list|()
block|{
name|GroovyCodeSource
name|gcs
init|=
operator|new
name|GroovyCodeSource
argument_list|(
name|script
argument_list|,
name|fake
argument_list|,
name|BootstrapInfo
operator|.
name|UNTRUSTED_CODEBASE
argument_list|)
decl_stmt|;
name|gcs
operator|.
name|setCachable
argument_list|(
literal|false
argument_list|)
expr_stmt|;
comment|// TODO: we could be more complicated and paranoid, and move this to separate block, to
comment|// sandbox the compilation process itself better.
return|return
name|loader
operator|.
name|parseClass
argument_list|(
name|gcs
argument_list|)
return|;
block|}
block|}
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Throwable
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
literal|"exception compiling Groovy script:"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
throw|throw
operator|new
name|ScriptException
argument_list|(
literal|"failed to compile groovy script"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**      * Return a script object with the given vars from the compiled script object      */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|method|createScript
specifier|private
name|Script
name|createScript
parameter_list|(
name|Object
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
throws|throws
name|InstantiationException
throws|,
name|IllegalAccessException
block|{
name|Class
argument_list|<
name|?
argument_list|>
name|scriptClass
init|=
operator|(
name|Class
argument_list|<
name|?
argument_list|>
operator|)
name|compiledScript
decl_stmt|;
name|Script
name|scriptObject
init|=
operator|(
name|Script
operator|)
name|scriptClass
operator|.
name|newInstance
argument_list|()
decl_stmt|;
name|Binding
name|binding
init|=
operator|new
name|Binding
argument_list|()
decl_stmt|;
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
name|scriptObject
operator|.
name|setBinding
argument_list|(
name|binding
argument_list|)
expr_stmt|;
return|return
name|scriptObject
return|;
block|}
annotation|@
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
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ScriptException
argument_list|(
literal|"failed to build executable "
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
name|InstantiationException
decl||
name|IllegalAccessException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ScriptException
argument_list|(
literal|"failed to build search "
operator|+
name|compiledScript
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
name|ESLogger
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
name|ESLogger
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
name|ESLogger
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
operator|new
name|PrivilegedAction
argument_list|<
name|Object
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Object
name|run
parameter_list|()
block|{
return|return
name|script
operator|.
name|run
argument_list|()
return|;
block|}
block|}
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Throwable
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
literal|"failed to run "
operator|+
name|compiledScript
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
throw|throw
operator|new
name|ScriptException
argument_list|(
literal|"failed to run "
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
DECL|method|runAsFloat
specifier|public
name|float
name|runAsFloat
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
name|floatValue
argument_list|()
return|;
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
annotation|@
name|Override
DECL|method|unwrap
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
block|}
comment|/**      * A compilation customizer that is used to transform a number like 1.23,      * which would normally be a BigDecimal, into a double value.      */
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
comment|/**      * Groovy expression transformer that converts BigDecimals to doubles      */
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
block|}
end_class

end_unit

