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
name|index
operator|.
name|LeafReaderContext
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
name|painless
operator|.
name|Compiler
operator|.
name|Loader
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
name|ScriptEngine
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
name|Permissions
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
name|security
operator|.
name|ProtectionDomain
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

begin_comment
comment|/**  * Implementation of a ScriptEngine for the Painless language.  */
end_comment

begin_class
DECL|class|PainlessScriptEngine
specifier|public
specifier|final
class|class
name|PainlessScriptEngine
extends|extends
name|AbstractComponent
implements|implements
name|ScriptEngine
block|{
comment|/**      * Standard name of the Painless language.      */
DECL|field|NAME
specifier|public
specifier|static
specifier|final
name|String
name|NAME
init|=
literal|"painless"
decl_stmt|;
comment|/**      * Permissions context used during compilation.      */
DECL|field|COMPILATION_CONTEXT
specifier|private
specifier|static
specifier|final
name|AccessControlContext
name|COMPILATION_CONTEXT
decl_stmt|;
comment|/**      * Setup the allowed permissions.      */
static|static
block|{
specifier|final
name|Permissions
name|none
init|=
operator|new
name|Permissions
argument_list|()
decl_stmt|;
name|none
operator|.
name|setReadOnly
argument_list|()
expr_stmt|;
name|COMPILATION_CONTEXT
operator|=
operator|new
name|AccessControlContext
argument_list|(
operator|new
name|ProtectionDomain
index|[]
block|{
operator|new
name|ProtectionDomain
argument_list|(
literal|null
argument_list|,
name|none
argument_list|)
block|}
argument_list|)
expr_stmt|;
block|}
comment|/**      * Default compiler settings to be used. Note that {@link CompilerSettings} is mutable but this instance shouldn't be mutated outside      * of {@link PainlessScriptEngine#PainlessScriptEngine(Settings)}.      */
DECL|field|defaultCompilerSettings
specifier|private
specifier|final
name|CompilerSettings
name|defaultCompilerSettings
init|=
operator|new
name|CompilerSettings
argument_list|()
decl_stmt|;
comment|/**      * Constructor.      * @param settings The settings to initialize the engine with.      */
DECL|method|PainlessScriptEngine
specifier|public
name|PainlessScriptEngine
parameter_list|(
specifier|final
name|Settings
name|settings
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|defaultCompilerSettings
operator|.
name|setRegexesEnabled
argument_list|(
name|CompilerSettings
operator|.
name|REGEX_ENABLED
operator|.
name|get
argument_list|(
name|settings
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**      * Get the type name(s) for the language.      * @return Always contains only the single name of the language.      */
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
comment|/**      * When a script is anonymous (inline), we give it this name.      */
DECL|field|INLINE_NAME
specifier|static
specifier|final
name|String
name|INLINE_NAME
init|=
literal|"<inline>"
decl_stmt|;
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
specifier|final
name|String
name|scriptSource
parameter_list|,
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|params
parameter_list|)
block|{
return|return
name|compile
argument_list|(
name|GenericElasticsearchScript
operator|.
name|class
argument_list|,
name|scriptName
argument_list|,
name|scriptSource
argument_list|,
name|params
argument_list|)
return|;
block|}
DECL|method|compile
parameter_list|<
name|T
parameter_list|>
name|T
name|compile
parameter_list|(
name|Class
argument_list|<
name|T
argument_list|>
name|iface
parameter_list|,
name|String
name|scriptName
parameter_list|,
specifier|final
name|String
name|scriptSource
parameter_list|,
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|params
parameter_list|)
block|{
specifier|final
name|CompilerSettings
name|compilerSettings
decl_stmt|;
if|if
condition|(
name|params
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
comment|// Use the default settings.
name|compilerSettings
operator|=
name|defaultCompilerSettings
expr_stmt|;
block|}
else|else
block|{
comment|// Use custom settings specified by params.
name|compilerSettings
operator|=
operator|new
name|CompilerSettings
argument_list|()
expr_stmt|;
comment|// Except regexes enabled - this is a node level setting and can't be changed in the request.
name|compilerSettings
operator|.
name|setRegexesEnabled
argument_list|(
name|defaultCompilerSettings
operator|.
name|areRegexesEnabled
argument_list|()
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|copy
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|(
name|params
argument_list|)
decl_stmt|;
name|String
name|value
init|=
name|copy
operator|.
name|remove
argument_list|(
name|CompilerSettings
operator|.
name|MAX_LOOP_COUNTER
argument_list|)
decl_stmt|;
if|if
condition|(
name|value
operator|!=
literal|null
condition|)
block|{
name|compilerSettings
operator|.
name|setMaxLoopCounter
argument_list|(
name|Integer
operator|.
name|parseInt
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|value
operator|=
name|copy
operator|.
name|remove
argument_list|(
name|CompilerSettings
operator|.
name|PICKY
argument_list|)
expr_stmt|;
if|if
condition|(
name|value
operator|!=
literal|null
condition|)
block|{
name|compilerSettings
operator|.
name|setPicky
argument_list|(
name|Boolean
operator|.
name|parseBoolean
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|value
operator|=
name|copy
operator|.
name|remove
argument_list|(
name|CompilerSettings
operator|.
name|INITIAL_CALL_SITE_DEPTH
argument_list|)
expr_stmt|;
if|if
condition|(
name|value
operator|!=
literal|null
condition|)
block|{
name|compilerSettings
operator|.
name|setInitialCallSiteDepth
argument_list|(
name|Integer
operator|.
name|parseInt
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|value
operator|=
name|copy
operator|.
name|remove
argument_list|(
name|CompilerSettings
operator|.
name|REGEX_ENABLED
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|value
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"[painless.regex.enabled] can only be set on node startup."
argument_list|)
throw|;
block|}
if|if
condition|(
operator|!
name|copy
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Unrecognized compile-time parameter(s): "
operator|+
name|copy
argument_list|)
throw|;
block|}
block|}
comment|// Check we ourselves are not being called by unprivileged code.
name|SpecialPermission
operator|.
name|check
argument_list|()
expr_stmt|;
comment|// Create our loader (which loads compiled code with no permissions).
specifier|final
name|Loader
name|loader
init|=
name|AccessController
operator|.
name|doPrivileged
argument_list|(
operator|new
name|PrivilegedAction
argument_list|<
name|Loader
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Loader
name|run
parameter_list|()
block|{
return|return
operator|new
name|Loader
argument_list|(
name|getClass
argument_list|()
operator|.
name|getClassLoader
argument_list|()
argument_list|)
return|;
block|}
block|}
argument_list|)
decl_stmt|;
try|try
block|{
comment|// Drop all permissions to actually compile the code itself.
return|return
name|AccessController
operator|.
name|doPrivileged
argument_list|(
operator|new
name|PrivilegedAction
argument_list|<
name|T
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|T
name|run
parameter_list|()
block|{
name|String
name|name
init|=
name|scriptName
operator|==
literal|null
condition|?
name|INLINE_NAME
else|:
name|scriptName
decl_stmt|;
return|return
name|Compiler
operator|.
name|compile
argument_list|(
name|loader
argument_list|,
name|iface
argument_list|,
name|name
argument_list|,
name|scriptSource
argument_list|,
name|compilerSettings
argument_list|)
return|;
block|}
block|}
argument_list|,
name|COMPILATION_CONTEXT
argument_list|)
return|;
comment|// Note that it is safe to catch any of the following errors since Painless is stateless.
block|}
catch|catch
parameter_list|(
name|OutOfMemoryError
decl||
name|StackOverflowError
decl||
name|VerifyError
decl||
name|Exception
name|e
parameter_list|)
block|{
throw|throw
name|convertToScriptException
argument_list|(
name|scriptName
operator|==
literal|null
condition|?
name|scriptSource
else|:
name|scriptName
argument_list|,
name|scriptSource
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**      * Retrieve an {@link ExecutableScript} for later use.      * @param compiledScript A previously compiled script.      * @param vars The variables to be used in the script.      * @return An {@link ExecutableScript} with the currently specified variables.      */
annotation|@
name|Override
DECL|method|executable
specifier|public
name|ExecutableScript
name|executable
parameter_list|(
specifier|final
name|Object
name|compiledScript
parameter_list|,
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
name|ScriptImpl
argument_list|(
operator|(
name|GenericElasticsearchScript
operator|)
name|compiledScript
argument_list|,
name|vars
argument_list|,
literal|null
argument_list|)
return|;
block|}
comment|/**      * Retrieve a {@link SearchScript} for later use.      * @param compiledScript A previously compiled script.      * @param lookup The object that ultimately allows access to search fields.      * @param vars The variables to be used in the script.      * @return An {@link SearchScript} with the currently specified variables.      */
annotation|@
name|Override
DECL|method|search
specifier|public
name|SearchScript
name|search
parameter_list|(
specifier|final
name|Object
name|compiledScript
parameter_list|,
specifier|final
name|SearchLookup
name|lookup
parameter_list|,
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
comment|/**              * Get the search script that will have access to search field values.              * @param context The LeafReaderContext to be used.              * @return A script that will have the search fields from the current context available for use.              */
annotation|@
name|Override
specifier|public
name|LeafSearchScript
name|getLeafSearchScript
parameter_list|(
specifier|final
name|LeafReaderContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|ScriptImpl
argument_list|(
operator|(
name|GenericElasticsearchScript
operator|)
name|compiledScript
argument_list|,
name|vars
argument_list|,
name|lookup
operator|.
name|getLeafSearchLookup
argument_list|(
name|context
argument_list|)
argument_list|)
return|;
block|}
comment|/**              * Whether or not the score is needed.              */
annotation|@
name|Override
specifier|public
name|boolean
name|needsScores
parameter_list|()
block|{
return|return
operator|(
operator|(
name|GenericElasticsearchScript
operator|)
name|compiledScript
operator|)
operator|.
name|uses$_score
argument_list|()
return|;
block|}
block|}
return|;
block|}
DECL|method|convertToScriptException
specifier|private
name|ScriptException
name|convertToScriptException
parameter_list|(
name|String
name|scriptName
parameter_list|,
name|String
name|scriptSource
parameter_list|,
name|Throwable
name|t
parameter_list|)
block|{
comment|// create a script stack: this is just the script portion
name|List
argument_list|<
name|String
argument_list|>
name|scriptStack
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|StackTraceElement
name|element
range|:
name|t
operator|.
name|getStackTrace
argument_list|()
control|)
block|{
if|if
condition|(
name|WriterConstants
operator|.
name|CLASS_NAME
operator|.
name|equals
argument_list|(
name|element
operator|.
name|getClassName
argument_list|()
argument_list|)
condition|)
block|{
comment|// found the script portion
name|int
name|offset
init|=
name|element
operator|.
name|getLineNumber
argument_list|()
decl_stmt|;
if|if
condition|(
name|offset
operator|==
operator|-
literal|1
condition|)
block|{
name|scriptStack
operator|.
name|add
argument_list|(
literal|"<<< unknown portion of script>>>"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|offset
operator|--
expr_stmt|;
comment|// offset is 1 based, line numbers must be!
name|int
name|startOffset
init|=
name|getPreviousStatement
argument_list|(
name|scriptSource
argument_list|,
name|offset
argument_list|)
decl_stmt|;
name|int
name|endOffset
init|=
name|getNextStatement
argument_list|(
name|scriptSource
argument_list|,
name|offset
argument_list|)
decl_stmt|;
name|StringBuilder
name|snippet
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
if|if
condition|(
name|startOffset
operator|>
literal|0
condition|)
block|{
name|snippet
operator|.
name|append
argument_list|(
literal|"... "
argument_list|)
expr_stmt|;
block|}
name|snippet
operator|.
name|append
argument_list|(
name|scriptSource
operator|.
name|substring
argument_list|(
name|startOffset
argument_list|,
name|endOffset
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|endOffset
operator|<
name|scriptSource
operator|.
name|length
argument_list|()
condition|)
block|{
name|snippet
operator|.
name|append
argument_list|(
literal|" ..."
argument_list|)
expr_stmt|;
block|}
name|scriptStack
operator|.
name|add
argument_list|(
name|snippet
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|StringBuilder
name|pointer
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
if|if
condition|(
name|startOffset
operator|>
literal|0
condition|)
block|{
name|pointer
operator|.
name|append
argument_list|(
literal|"    "
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
name|startOffset
init|;
name|i
operator|<
name|offset
condition|;
name|i
operator|++
control|)
block|{
name|pointer
operator|.
name|append
argument_list|(
literal|' '
argument_list|)
expr_stmt|;
block|}
name|pointer
operator|.
name|append
argument_list|(
literal|"^---- HERE"
argument_list|)
expr_stmt|;
name|scriptStack
operator|.
name|add
argument_list|(
name|pointer
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
break|break;
block|}
block|}
throw|throw
operator|new
name|ScriptException
argument_list|(
literal|"compile error"
argument_list|,
name|t
argument_list|,
name|scriptStack
argument_list|,
name|scriptSource
argument_list|,
name|PainlessScriptEngine
operator|.
name|NAME
argument_list|)
throw|;
block|}
comment|// very simple heuristic: +/- 25 chars. can be improved later.
DECL|method|getPreviousStatement
specifier|private
name|int
name|getPreviousStatement
parameter_list|(
name|String
name|scriptSource
parameter_list|,
name|int
name|offset
parameter_list|)
block|{
return|return
name|Math
operator|.
name|max
argument_list|(
literal|0
argument_list|,
name|offset
operator|-
literal|25
argument_list|)
return|;
block|}
DECL|method|getNextStatement
specifier|private
name|int
name|getNextStatement
parameter_list|(
name|String
name|scriptSource
parameter_list|,
name|int
name|offset
parameter_list|)
block|{
return|return
name|Math
operator|.
name|min
argument_list|(
name|scriptSource
operator|.
name|length
argument_list|()
argument_list|,
name|offset
operator|+
literal|25
argument_list|)
return|;
block|}
block|}
end_class

end_unit

