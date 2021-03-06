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
name|painless
operator|.
name|antlr
operator|.
name|Walker
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
name|node
operator|.
name|SSource
import|;
end_import

begin_import
import|import
name|org
operator|.
name|objectweb
operator|.
name|asm
operator|.
name|util
operator|.
name|Printer
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Constructor
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|MalformedURLException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URL
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|CodeSource
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|SecureClassLoader
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|cert
operator|.
name|Certificate
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicInteger
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|painless
operator|.
name|WriterConstants
operator|.
name|CLASS_NAME
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|painless
operator|.
name|node
operator|.
name|SSource
operator|.
name|MainMethodReserved
import|;
end_import

begin_comment
comment|/**  * The Compiler is the entry point for generating a Painless script.  The compiler will receive a Painless  * tree based on the type of input passed in (currently only ANTLR).  Two passes will then be run over the tree,  * one for analysis and another to generate the actual byte code using ASM using the root of the tree {@link SSource}.  */
end_comment

begin_class
DECL|class|Compiler
specifier|final
class|class
name|Compiler
block|{
comment|/**      * The maximum number of characters allowed in the script source.      */
DECL|field|MAXIMUM_SOURCE_LENGTH
specifier|static
specifier|final
name|int
name|MAXIMUM_SOURCE_LENGTH
init|=
literal|16384
decl_stmt|;
comment|/**      * Define the class with lowest privileges.      */
DECL|field|CODESOURCE
specifier|private
specifier|static
specifier|final
name|CodeSource
name|CODESOURCE
decl_stmt|;
comment|/**      * Setup the code privileges.      */
static|static
block|{
try|try
block|{
comment|// Setup the code privileges.
name|CODESOURCE
operator|=
operator|new
name|CodeSource
argument_list|(
operator|new
name|URL
argument_list|(
literal|"file:"
operator|+
name|BootstrapInfo
operator|.
name|UNTRUSTED_CODEBASE
argument_list|)
argument_list|,
operator|(
name|Certificate
index|[]
operator|)
literal|null
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|MalformedURLException
name|impossible
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|impossible
argument_list|)
throw|;
block|}
block|}
comment|/**      * A secure class loader used to define Painless scripts.      */
DECL|class|Loader
specifier|static
specifier|final
class|class
name|Loader
extends|extends
name|SecureClassLoader
block|{
DECL|field|lambdaCounter
specifier|private
specifier|final
name|AtomicInteger
name|lambdaCounter
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
decl_stmt|;
comment|/**          * @param parent The parent ClassLoader.          */
DECL|method|Loader
name|Loader
parameter_list|(
name|ClassLoader
name|parent
parameter_list|)
block|{
name|super
argument_list|(
name|parent
argument_list|)
expr_stmt|;
block|}
comment|/**          * Generates a Class object from the generated byte code.          * @param name The name of the class.          * @param bytes The generated byte code.          * @return A Class object defining a factory.          */
DECL|method|defineFactory
name|Class
argument_list|<
name|?
argument_list|>
name|defineFactory
parameter_list|(
name|String
name|name
parameter_list|,
name|byte
index|[]
name|bytes
parameter_list|)
block|{
return|return
name|defineClass
argument_list|(
name|name
argument_list|,
name|bytes
argument_list|,
literal|0
argument_list|,
name|bytes
operator|.
name|length
argument_list|,
name|CODESOURCE
argument_list|)
return|;
block|}
comment|/**          * Generates a Class object from the generated byte code.          * @param name The name of the class.          * @param bytes The generated byte code.          * @return A Class object extending {@link PainlessScript}.          */
DECL|method|defineScript
name|Class
argument_list|<
name|?
extends|extends
name|PainlessScript
argument_list|>
name|defineScript
parameter_list|(
name|String
name|name
parameter_list|,
name|byte
index|[]
name|bytes
parameter_list|)
block|{
return|return
name|defineClass
argument_list|(
name|name
argument_list|,
name|bytes
argument_list|,
literal|0
argument_list|,
name|bytes
operator|.
name|length
argument_list|,
name|CODESOURCE
argument_list|)
operator|.
name|asSubclass
argument_list|(
name|PainlessScript
operator|.
name|class
argument_list|)
return|;
block|}
comment|/**          * Generates a Class object for a lambda method.          * @param name The name of the class.          * @param bytes The generated byte code.          * @return A Class object.          */
DECL|method|defineLambda
name|Class
argument_list|<
name|?
argument_list|>
name|defineLambda
parameter_list|(
name|String
name|name
parameter_list|,
name|byte
index|[]
name|bytes
parameter_list|)
block|{
return|return
name|defineClass
argument_list|(
name|name
argument_list|,
name|bytes
argument_list|,
literal|0
argument_list|,
name|bytes
operator|.
name|length
argument_list|,
name|CODESOURCE
argument_list|)
return|;
block|}
comment|/**          * A counter used to generate a unique name for each lambda          * function/reference class in this classloader.          */
DECL|method|newLambdaIdentifier
name|int
name|newLambdaIdentifier
parameter_list|()
block|{
return|return
name|lambdaCounter
operator|.
name|getAndIncrement
argument_list|()
return|;
block|}
block|}
comment|/**      * The class/interface the script is guaranteed to derive/implement.      */
DECL|field|base
specifier|private
specifier|final
name|Class
argument_list|<
name|?
argument_list|>
name|base
decl_stmt|;
comment|/**      * The whitelist the script will use.      */
DECL|field|definition
specifier|private
specifier|final
name|Definition
name|definition
decl_stmt|;
comment|/**      * Standard constructor.      * @param base The class/interface the script is guaranteed to derive/implement.      * @param definition The whitelist the script will use.      */
DECL|method|Compiler
name|Compiler
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|base
parameter_list|,
name|Definition
name|definition
parameter_list|)
block|{
name|this
operator|.
name|base
operator|=
name|base
expr_stmt|;
name|this
operator|.
name|definition
operator|=
name|definition
expr_stmt|;
block|}
comment|/**      * Runs the two-pass compiler to generate a Painless script.      * @param loader The ClassLoader used to define the script.      * @param name The name of the script.      * @param source The source code for the script.      * @param settings The CompilerSettings to be used during the compilation.      * @return An executable script that implements both a specified interface and is a subclass of {@link PainlessScript}      */
DECL|method|compile
name|Constructor
argument_list|<
name|?
argument_list|>
name|compile
parameter_list|(
name|Loader
name|loader
parameter_list|,
name|MainMethodReserved
name|reserved
parameter_list|,
name|String
name|name
parameter_list|,
name|String
name|source
parameter_list|,
name|CompilerSettings
name|settings
parameter_list|)
block|{
if|if
condition|(
name|source
operator|.
name|length
argument_list|()
operator|>
name|MAXIMUM_SOURCE_LENGTH
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Scripts may be no longer than "
operator|+
name|MAXIMUM_SOURCE_LENGTH
operator|+
literal|" characters.  The passed in script is "
operator|+
name|source
operator|.
name|length
argument_list|()
operator|+
literal|" characters.  Consider using a"
operator|+
literal|" plugin if a script longer than this length is a requirement."
argument_list|)
throw|;
block|}
name|ScriptClassInfo
name|scriptClassInfo
init|=
operator|new
name|ScriptClassInfo
argument_list|(
name|definition
argument_list|,
name|base
argument_list|)
decl_stmt|;
name|SSource
name|root
init|=
name|Walker
operator|.
name|buildPainlessTree
argument_list|(
name|scriptClassInfo
argument_list|,
name|reserved
argument_list|,
name|name
argument_list|,
name|source
argument_list|,
name|settings
argument_list|,
name|definition
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|root
operator|.
name|analyze
argument_list|(
name|definition
argument_list|)
expr_stmt|;
name|root
operator|.
name|write
argument_list|()
expr_stmt|;
try|try
block|{
name|Class
argument_list|<
name|?
extends|extends
name|PainlessScript
argument_list|>
name|clazz
init|=
name|loader
operator|.
name|defineScript
argument_list|(
name|CLASS_NAME
argument_list|,
name|root
operator|.
name|getBytes
argument_list|()
argument_list|)
decl_stmt|;
name|clazz
operator|.
name|getField
argument_list|(
literal|"$NAME"
argument_list|)
operator|.
name|set
argument_list|(
literal|null
argument_list|,
name|name
argument_list|)
expr_stmt|;
name|clazz
operator|.
name|getField
argument_list|(
literal|"$SOURCE"
argument_list|)
operator|.
name|set
argument_list|(
literal|null
argument_list|,
name|source
argument_list|)
expr_stmt|;
name|clazz
operator|.
name|getField
argument_list|(
literal|"$STATEMENTS"
argument_list|)
operator|.
name|set
argument_list|(
literal|null
argument_list|,
name|root
operator|.
name|getStatements
argument_list|()
argument_list|)
expr_stmt|;
name|clazz
operator|.
name|getField
argument_list|(
literal|"$DEFINITION"
argument_list|)
operator|.
name|set
argument_list|(
literal|null
argument_list|,
name|definition
argument_list|)
expr_stmt|;
return|return
name|clazz
operator|.
name|getConstructors
argument_list|()
index|[
literal|0
index|]
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|exception
parameter_list|)
block|{
comment|// Catch everything to let the user know this is something caused internally.
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"An internal error occurred attempting to define the script ["
operator|+
name|name
operator|+
literal|"]."
argument_list|,
name|exception
argument_list|)
throw|;
block|}
block|}
comment|/**      * Runs the two-pass compiler to generate a Painless script.  (Used by the debugger.)      * @param source The source code for the script.      * @param settings The CompilerSettings to be used during the compilation.      * @return The bytes for compilation.      */
DECL|method|compile
name|byte
index|[]
name|compile
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|source
parameter_list|,
name|CompilerSettings
name|settings
parameter_list|,
name|Printer
name|debugStream
parameter_list|)
block|{
if|if
condition|(
name|source
operator|.
name|length
argument_list|()
operator|>
name|MAXIMUM_SOURCE_LENGTH
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Scripts may be no longer than "
operator|+
name|MAXIMUM_SOURCE_LENGTH
operator|+
literal|" characters.  The passed in script is "
operator|+
name|source
operator|.
name|length
argument_list|()
operator|+
literal|" characters.  Consider using a"
operator|+
literal|" plugin if a script longer than this length is a requirement."
argument_list|)
throw|;
block|}
name|ScriptClassInfo
name|scriptClassInfo
init|=
operator|new
name|ScriptClassInfo
argument_list|(
name|definition
argument_list|,
name|base
argument_list|)
decl_stmt|;
name|SSource
name|root
init|=
name|Walker
operator|.
name|buildPainlessTree
argument_list|(
name|scriptClassInfo
argument_list|,
operator|new
name|MainMethodReserved
argument_list|()
argument_list|,
name|name
argument_list|,
name|source
argument_list|,
name|settings
argument_list|,
name|definition
argument_list|,
name|debugStream
argument_list|)
decl_stmt|;
name|root
operator|.
name|analyze
argument_list|(
name|definition
argument_list|)
expr_stmt|;
name|root
operator|.
name|write
argument_list|()
expr_stmt|;
return|return
name|root
operator|.
name|getBytes
argument_list|()
return|;
block|}
block|}
end_class

end_unit

