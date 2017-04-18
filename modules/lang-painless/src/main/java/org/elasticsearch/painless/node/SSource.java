begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.painless.node
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|painless
operator|.
name|node
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|painless
operator|.
name|CompilerSettings
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
name|Constant
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
name|Definition
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
name|Definition
operator|.
name|Method
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
name|Definition
operator|.
name|MethodKey
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
name|Globals
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
name|Locals
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
name|Locals
operator|.
name|Variable
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
name|Location
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
name|MethodWriter
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
name|ScriptInterface
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
name|SimpleChecksAdapter
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
name|WriterConstants
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
name|ClassVisitor
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
name|ClassWriter
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
name|Label
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
name|Opcodes
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
name|Type
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
name|org
operator|.
name|objectweb
operator|.
name|asm
operator|.
name|util
operator|.
name|TraceClassVisitor
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
name|BitSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
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
name|java
operator|.
name|util
operator|.
name|HashSet
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
import|import
name|java
operator|.
name|util
operator|.
name|Objects
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
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

begin_import
import|import static
name|java
operator|.
name|util
operator|.
name|Collections
operator|.
name|unmodifiableSet
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
name|BASE_CLASS_TYPE
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
name|BOOTSTRAP_METHOD_ERROR_TYPE
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
name|CLASS_TYPE
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
name|COLLECTIONS_TYPE
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
name|CONSTRUCTOR
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
name|CONVERT_TO_SCRIPT_EXCEPTION_METHOD
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
name|DEFINITION_TYPE
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
name|DEF_BOOTSTRAP_DELEGATE_METHOD
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
name|DEF_BOOTSTRAP_DELEGATE_TYPE
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
name|DEF_BOOTSTRAP_METHOD
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
name|EMPTY_MAP_METHOD
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
name|EXCEPTION_TYPE
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
name|OUT_OF_MEMORY_ERROR_TYPE
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
name|PAINLESS_ERROR_TYPE
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
name|PAINLESS_EXPLAIN_ERROR_GET_HEADERS_METHOD
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
name|PAINLESS_EXPLAIN_ERROR_TYPE
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
name|STACK_OVERFLOW_ERROR_TYPE
import|;
end_import

begin_comment
comment|/**  * The root of all Painless trees.  Contains a series of statements.  */
end_comment

begin_class
DECL|class|SSource
specifier|public
specifier|final
class|class
name|SSource
extends|extends
name|AStatement
block|{
comment|/**      * Tracks derived arguments and the loop counter.  Must be given to any source of input      * prior to beginning the analysis phase so that reserved variables      * are known ahead of time to assign appropriate slots without      * being wasteful.      */
DECL|interface|Reserved
specifier|public
interface|interface
name|Reserved
block|{
DECL|method|markUsedVariable
name|void
name|markUsedVariable
parameter_list|(
name|String
name|name
parameter_list|)
function_decl|;
DECL|method|setMaxLoopCounter
name|void
name|setMaxLoopCounter
parameter_list|(
name|int
name|max
parameter_list|)
function_decl|;
DECL|method|getMaxLoopCounter
name|int
name|getMaxLoopCounter
parameter_list|()
function_decl|;
block|}
DECL|class|MainMethodReserved
specifier|public
specifier|static
specifier|final
class|class
name|MainMethodReserved
implements|implements
name|Reserved
block|{
DECL|field|usedVariables
specifier|private
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|usedVariables
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
DECL|field|maxLoopCounter
specifier|private
name|int
name|maxLoopCounter
init|=
literal|0
decl_stmt|;
annotation|@
name|Override
DECL|method|markUsedVariable
specifier|public
name|void
name|markUsedVariable
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|usedVariables
operator|.
name|add
argument_list|(
name|name
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|setMaxLoopCounter
specifier|public
name|void
name|setMaxLoopCounter
parameter_list|(
name|int
name|max
parameter_list|)
block|{
name|maxLoopCounter
operator|=
name|max
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getMaxLoopCounter
specifier|public
name|int
name|getMaxLoopCounter
parameter_list|()
block|{
return|return
name|maxLoopCounter
return|;
block|}
DECL|method|getUsedVariables
specifier|public
name|Set
argument_list|<
name|String
argument_list|>
name|getUsedVariables
parameter_list|()
block|{
return|return
name|unmodifiableSet
argument_list|(
name|usedVariables
argument_list|)
return|;
block|}
block|}
DECL|field|scriptInterface
specifier|private
specifier|final
name|ScriptInterface
name|scriptInterface
decl_stmt|;
DECL|field|settings
specifier|private
specifier|final
name|CompilerSettings
name|settings
decl_stmt|;
DECL|field|name
specifier|private
specifier|final
name|String
name|name
decl_stmt|;
DECL|field|source
specifier|private
specifier|final
name|String
name|source
decl_stmt|;
DECL|field|debugStream
specifier|private
specifier|final
name|Printer
name|debugStream
decl_stmt|;
DECL|field|reserved
specifier|private
specifier|final
name|MainMethodReserved
name|reserved
decl_stmt|;
DECL|field|functions
specifier|private
specifier|final
name|List
argument_list|<
name|SFunction
argument_list|>
name|functions
decl_stmt|;
DECL|field|globals
specifier|private
specifier|final
name|Globals
name|globals
decl_stmt|;
DECL|field|statements
specifier|private
specifier|final
name|List
argument_list|<
name|AStatement
argument_list|>
name|statements
decl_stmt|;
DECL|field|mainMethod
specifier|private
name|Locals
name|mainMethod
decl_stmt|;
DECL|field|bytes
specifier|private
name|byte
index|[]
name|bytes
decl_stmt|;
DECL|method|SSource
specifier|public
name|SSource
parameter_list|(
name|ScriptInterface
name|scriptInterface
parameter_list|,
name|CompilerSettings
name|settings
parameter_list|,
name|String
name|name
parameter_list|,
name|String
name|source
parameter_list|,
name|Printer
name|debugStream
parameter_list|,
name|MainMethodReserved
name|reserved
parameter_list|,
name|Location
name|location
parameter_list|,
name|List
argument_list|<
name|SFunction
argument_list|>
name|functions
parameter_list|,
name|Globals
name|globals
parameter_list|,
name|List
argument_list|<
name|AStatement
argument_list|>
name|statements
parameter_list|)
block|{
name|super
argument_list|(
name|location
argument_list|)
expr_stmt|;
name|this
operator|.
name|scriptInterface
operator|=
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|scriptInterface
argument_list|)
expr_stmt|;
name|this
operator|.
name|settings
operator|=
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|name
operator|=
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|this
operator|.
name|source
operator|=
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|source
argument_list|)
expr_stmt|;
name|this
operator|.
name|debugStream
operator|=
name|debugStream
expr_stmt|;
name|this
operator|.
name|reserved
operator|=
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|reserved
argument_list|)
expr_stmt|;
comment|// process any synthetic functions generated by walker (because right now, thats still easy)
name|functions
operator|.
name|addAll
argument_list|(
name|globals
operator|.
name|getSyntheticMethods
argument_list|()
operator|.
name|values
argument_list|()
argument_list|)
expr_stmt|;
name|globals
operator|.
name|getSyntheticMethods
argument_list|()
operator|.
name|clear
argument_list|()
expr_stmt|;
name|this
operator|.
name|functions
operator|=
name|Collections
operator|.
name|unmodifiableList
argument_list|(
name|functions
argument_list|)
expr_stmt|;
name|this
operator|.
name|statements
operator|=
name|Collections
operator|.
name|unmodifiableList
argument_list|(
name|statements
argument_list|)
expr_stmt|;
name|this
operator|.
name|globals
operator|=
name|globals
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|extractVariables
name|void
name|extractVariables
parameter_list|(
name|Set
argument_list|<
name|String
argument_list|>
name|variables
parameter_list|)
block|{
comment|// we should never be extracting from a function, as functions are top-level!
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Illegal tree structure."
argument_list|)
throw|;
block|}
DECL|method|analyze
specifier|public
name|void
name|analyze
parameter_list|(
name|Definition
name|definition
parameter_list|)
block|{
name|Map
argument_list|<
name|MethodKey
argument_list|,
name|Method
argument_list|>
name|methods
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|SFunction
name|function
range|:
name|functions
control|)
block|{
name|function
operator|.
name|generateSignature
argument_list|(
name|definition
argument_list|)
expr_stmt|;
name|MethodKey
name|key
init|=
operator|new
name|MethodKey
argument_list|(
name|function
operator|.
name|name
argument_list|,
name|function
operator|.
name|parameters
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|methods
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|function
operator|.
name|method
argument_list|)
operator|!=
literal|null
condition|)
block|{
throw|throw
name|createError
argument_list|(
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Duplicate functions with name ["
operator|+
name|function
operator|.
name|name
operator|+
literal|"]."
argument_list|)
argument_list|)
throw|;
block|}
block|}
name|analyze
argument_list|(
name|Locals
operator|.
name|newProgramScope
argument_list|(
name|definition
argument_list|,
name|methods
operator|.
name|values
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|analyze
name|void
name|analyze
parameter_list|(
name|Locals
name|program
parameter_list|)
block|{
for|for
control|(
name|SFunction
name|function
range|:
name|functions
control|)
block|{
name|Locals
name|functionLocals
init|=
name|Locals
operator|.
name|newFunctionScope
argument_list|(
name|program
argument_list|,
name|function
operator|.
name|rtnType
argument_list|,
name|function
operator|.
name|parameters
argument_list|,
name|function
operator|.
name|reserved
operator|.
name|getMaxLoopCounter
argument_list|()
argument_list|)
decl_stmt|;
name|function
operator|.
name|analyze
argument_list|(
name|functionLocals
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|statements
operator|==
literal|null
operator|||
name|statements
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
name|createError
argument_list|(
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Cannot generate an empty script."
argument_list|)
argument_list|)
throw|;
block|}
name|mainMethod
operator|=
name|Locals
operator|.
name|newMainMethodScope
argument_list|(
name|scriptInterface
argument_list|,
name|program
argument_list|,
name|reserved
operator|.
name|getMaxLoopCounter
argument_list|()
argument_list|)
expr_stmt|;
name|AStatement
name|last
init|=
name|statements
operator|.
name|get
argument_list|(
name|statements
operator|.
name|size
argument_list|()
operator|-
literal|1
argument_list|)
decl_stmt|;
for|for
control|(
name|AStatement
name|statement
range|:
name|statements
control|)
block|{
comment|// Note that we do not need to check after the last statement because
comment|// there is no statement that can be unreachable after the last.
if|if
condition|(
name|allEscape
condition|)
block|{
throw|throw
name|createError
argument_list|(
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Unreachable statement."
argument_list|)
argument_list|)
throw|;
block|}
name|statement
operator|.
name|lastSource
operator|=
name|statement
operator|==
name|last
expr_stmt|;
name|statement
operator|.
name|analyze
argument_list|(
name|mainMethod
argument_list|)
expr_stmt|;
name|methodEscape
operator|=
name|statement
operator|.
name|methodEscape
expr_stmt|;
name|allEscape
operator|=
name|statement
operator|.
name|allEscape
expr_stmt|;
block|}
block|}
DECL|method|write
specifier|public
name|void
name|write
parameter_list|()
block|{
comment|// Create the ClassWriter.
name|int
name|classFrames
init|=
name|ClassWriter
operator|.
name|COMPUTE_FRAMES
operator||
name|ClassWriter
operator|.
name|COMPUTE_MAXS
decl_stmt|;
name|int
name|classAccess
init|=
name|Opcodes
operator|.
name|ACC_PUBLIC
operator||
name|Opcodes
operator|.
name|ACC_SUPER
operator||
name|Opcodes
operator|.
name|ACC_FINAL
decl_stmt|;
name|String
name|classBase
init|=
name|BASE_CLASS_TYPE
operator|.
name|getInternalName
argument_list|()
decl_stmt|;
name|String
name|className
init|=
name|CLASS_TYPE
operator|.
name|getInternalName
argument_list|()
decl_stmt|;
name|String
name|classInterfaces
index|[]
init|=
operator|new
name|String
index|[]
block|{
name|Type
operator|.
name|getType
argument_list|(
name|scriptInterface
operator|.
name|getInterface
argument_list|()
argument_list|)
operator|.
name|getInternalName
argument_list|()
block|}
decl_stmt|;
name|ClassWriter
name|writer
init|=
operator|new
name|ClassWriter
argument_list|(
name|classFrames
argument_list|)
decl_stmt|;
name|ClassVisitor
name|visitor
init|=
name|writer
decl_stmt|;
comment|// if picky is enabled, turn on some checks. instead of VerifyError at the end, you get a helpful stacktrace.
if|if
condition|(
name|settings
operator|.
name|isPicky
argument_list|()
condition|)
block|{
name|visitor
operator|=
operator|new
name|SimpleChecksAdapter
argument_list|(
name|visitor
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|debugStream
operator|!=
literal|null
condition|)
block|{
name|visitor
operator|=
operator|new
name|TraceClassVisitor
argument_list|(
name|visitor
argument_list|,
name|debugStream
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
name|visitor
operator|.
name|visit
argument_list|(
name|WriterConstants
operator|.
name|CLASS_VERSION
argument_list|,
name|classAccess
argument_list|,
name|className
argument_list|,
literal|null
argument_list|,
name|classBase
argument_list|,
name|classInterfaces
argument_list|)
expr_stmt|;
name|visitor
operator|.
name|visitSource
argument_list|(
name|Location
operator|.
name|computeSourceName
argument_list|(
name|name
argument_list|,
name|source
argument_list|)
argument_list|,
literal|null
argument_list|)
expr_stmt|;
comment|// Write the a method to bootstrap def calls
name|MethodWriter
name|bootstrapDef
init|=
operator|new
name|MethodWriter
argument_list|(
name|Opcodes
operator|.
name|ACC_STATIC
operator||
name|Opcodes
operator|.
name|ACC_VARARGS
argument_list|,
name|DEF_BOOTSTRAP_METHOD
argument_list|,
name|visitor
argument_list|,
name|globals
operator|.
name|getStatements
argument_list|()
argument_list|,
name|settings
argument_list|)
decl_stmt|;
name|bootstrapDef
operator|.
name|visitCode
argument_list|()
expr_stmt|;
name|bootstrapDef
operator|.
name|getStatic
argument_list|(
name|CLASS_TYPE
argument_list|,
literal|"$DEFINITION"
argument_list|,
name|DEFINITION_TYPE
argument_list|)
expr_stmt|;
name|bootstrapDef
operator|.
name|loadArgs
argument_list|()
expr_stmt|;
name|bootstrapDef
operator|.
name|invokeStatic
argument_list|(
name|DEF_BOOTSTRAP_DELEGATE_TYPE
argument_list|,
name|DEF_BOOTSTRAP_DELEGATE_METHOD
argument_list|)
expr_stmt|;
name|bootstrapDef
operator|.
name|returnValue
argument_list|()
expr_stmt|;
name|bootstrapDef
operator|.
name|endMethod
argument_list|()
expr_stmt|;
comment|// Write the static variable used by the method to bootstrap def calls
name|visitor
operator|.
name|visitField
argument_list|(
name|Opcodes
operator|.
name|ACC_PUBLIC
operator||
name|Opcodes
operator|.
name|ACC_STATIC
argument_list|,
literal|"$DEFINITION"
argument_list|,
name|DEFINITION_TYPE
operator|.
name|getDescriptor
argument_list|()
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
operator|.
name|visitEnd
argument_list|()
expr_stmt|;
comment|// Write the constructor:
name|MethodWriter
name|constructor
init|=
operator|new
name|MethodWriter
argument_list|(
name|Opcodes
operator|.
name|ACC_PUBLIC
argument_list|,
name|CONSTRUCTOR
argument_list|,
name|visitor
argument_list|,
name|globals
operator|.
name|getStatements
argument_list|()
argument_list|,
name|settings
argument_list|)
decl_stmt|;
name|constructor
operator|.
name|visitCode
argument_list|()
expr_stmt|;
name|constructor
operator|.
name|loadThis
argument_list|()
expr_stmt|;
name|constructor
operator|.
name|loadArgs
argument_list|()
expr_stmt|;
name|constructor
operator|.
name|invokeConstructor
argument_list|(
name|BASE_CLASS_TYPE
argument_list|,
name|CONSTRUCTOR
argument_list|)
expr_stmt|;
name|constructor
operator|.
name|returnValue
argument_list|()
expr_stmt|;
name|constructor
operator|.
name|endMethod
argument_list|()
expr_stmt|;
comment|// Write the method defined in the interface:
name|MethodWriter
name|executeMethod
init|=
operator|new
name|MethodWriter
argument_list|(
name|Opcodes
operator|.
name|ACC_PUBLIC
argument_list|,
name|scriptInterface
operator|.
name|getExecuteMethod
argument_list|()
argument_list|,
name|visitor
argument_list|,
name|globals
operator|.
name|getStatements
argument_list|()
argument_list|,
name|settings
argument_list|)
decl_stmt|;
name|executeMethod
operator|.
name|visitCode
argument_list|()
expr_stmt|;
name|write
argument_list|(
name|executeMethod
argument_list|,
name|globals
argument_list|)
expr_stmt|;
name|executeMethod
operator|.
name|endMethod
argument_list|()
expr_stmt|;
comment|// Write all functions:
for|for
control|(
name|SFunction
name|function
range|:
name|functions
control|)
block|{
name|function
operator|.
name|write
argument_list|(
name|visitor
argument_list|,
name|settings
argument_list|,
name|globals
argument_list|)
expr_stmt|;
block|}
comment|// Write all synthetic functions. Note that this process may add more :)
while|while
condition|(
operator|!
name|globals
operator|.
name|getSyntheticMethods
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|List
argument_list|<
name|SFunction
argument_list|>
name|current
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|globals
operator|.
name|getSyntheticMethods
argument_list|()
operator|.
name|values
argument_list|()
argument_list|)
decl_stmt|;
name|globals
operator|.
name|getSyntheticMethods
argument_list|()
operator|.
name|clear
argument_list|()
expr_stmt|;
for|for
control|(
name|SFunction
name|function
range|:
name|current
control|)
block|{
name|function
operator|.
name|write
argument_list|(
name|visitor
argument_list|,
name|settings
argument_list|,
name|globals
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Write the constants
if|if
condition|(
literal|false
operator|==
name|globals
operator|.
name|getConstantInitializers
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|Collection
argument_list|<
name|Constant
argument_list|>
name|inits
init|=
name|globals
operator|.
name|getConstantInitializers
argument_list|()
operator|.
name|values
argument_list|()
decl_stmt|;
comment|// Fields
for|for
control|(
name|Constant
name|constant
range|:
name|inits
control|)
block|{
name|visitor
operator|.
name|visitField
argument_list|(
name|Opcodes
operator|.
name|ACC_FINAL
operator||
name|Opcodes
operator|.
name|ACC_PRIVATE
operator||
name|Opcodes
operator|.
name|ACC_STATIC
argument_list|,
name|constant
operator|.
name|name
argument_list|,
name|constant
operator|.
name|type
operator|.
name|getDescriptor
argument_list|()
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
operator|.
name|visitEnd
argument_list|()
expr_stmt|;
block|}
comment|// Initialize the constants in a static initializer
specifier|final
name|MethodWriter
name|clinit
init|=
operator|new
name|MethodWriter
argument_list|(
name|Opcodes
operator|.
name|ACC_STATIC
argument_list|,
name|WriterConstants
operator|.
name|CLINIT
argument_list|,
name|visitor
argument_list|,
name|globals
operator|.
name|getStatements
argument_list|()
argument_list|,
name|settings
argument_list|)
decl_stmt|;
name|clinit
operator|.
name|visitCode
argument_list|()
expr_stmt|;
for|for
control|(
name|Constant
name|constant
range|:
name|inits
control|)
block|{
name|constant
operator|.
name|initializer
operator|.
name|accept
argument_list|(
name|clinit
argument_list|)
expr_stmt|;
name|clinit
operator|.
name|putStatic
argument_list|(
name|CLASS_TYPE
argument_list|,
name|constant
operator|.
name|name
argument_list|,
name|constant
operator|.
name|type
argument_list|)
expr_stmt|;
block|}
name|clinit
operator|.
name|returnValue
argument_list|()
expr_stmt|;
name|clinit
operator|.
name|endMethod
argument_list|()
expr_stmt|;
block|}
comment|// Write any uses$varName methods for used variables
for|for
control|(
name|org
operator|.
name|objectweb
operator|.
name|asm
operator|.
name|commons
operator|.
name|Method
name|usesMethod
range|:
name|scriptInterface
operator|.
name|getUsesMethods
argument_list|()
control|)
block|{
name|MethodWriter
name|ifaceMethod
init|=
operator|new
name|MethodWriter
argument_list|(
name|Opcodes
operator|.
name|ACC_PUBLIC
argument_list|,
name|usesMethod
argument_list|,
name|visitor
argument_list|,
name|globals
operator|.
name|getStatements
argument_list|()
argument_list|,
name|settings
argument_list|)
decl_stmt|;
name|ifaceMethod
operator|.
name|visitCode
argument_list|()
expr_stmt|;
name|ifaceMethod
operator|.
name|push
argument_list|(
name|reserved
operator|.
name|getUsedVariables
argument_list|()
operator|.
name|contains
argument_list|(
name|usesMethod
operator|.
name|getName
argument_list|()
operator|.
name|substring
argument_list|(
literal|"uses$"
operator|.
name|length
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|ifaceMethod
operator|.
name|returnValue
argument_list|()
expr_stmt|;
name|ifaceMethod
operator|.
name|endMethod
argument_list|()
expr_stmt|;
block|}
comment|// End writing the class and store the generated bytes.
name|visitor
operator|.
name|visitEnd
argument_list|()
expr_stmt|;
name|bytes
operator|=
name|writer
operator|.
name|toByteArray
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|write
name|void
name|write
parameter_list|(
name|MethodWriter
name|writer
parameter_list|,
name|Globals
name|globals
parameter_list|)
block|{
comment|// We wrap the whole method in a few try/catches to handle and/or convert other exceptions to ScriptException
name|Label
name|startTry
init|=
operator|new
name|Label
argument_list|()
decl_stmt|;
name|Label
name|endTry
init|=
operator|new
name|Label
argument_list|()
decl_stmt|;
name|Label
name|startExplainCatch
init|=
operator|new
name|Label
argument_list|()
decl_stmt|;
name|Label
name|startOtherCatch
init|=
operator|new
name|Label
argument_list|()
decl_stmt|;
name|Label
name|endCatch
init|=
operator|new
name|Label
argument_list|()
decl_stmt|;
name|writer
operator|.
name|mark
argument_list|(
name|startTry
argument_list|)
expr_stmt|;
if|if
condition|(
name|reserved
operator|.
name|getMaxLoopCounter
argument_list|()
operator|>
literal|0
condition|)
block|{
comment|// if there is infinite loop protection, we do this once:
comment|// int #loop = settings.getMaxLoopCounter()
name|Variable
name|loop
init|=
name|mainMethod
operator|.
name|getVariable
argument_list|(
literal|null
argument_list|,
name|Locals
operator|.
name|LOOP
argument_list|)
decl_stmt|;
name|writer
operator|.
name|push
argument_list|(
name|reserved
operator|.
name|getMaxLoopCounter
argument_list|()
argument_list|)
expr_stmt|;
name|writer
operator|.
name|visitVarInsn
argument_list|(
name|Opcodes
operator|.
name|ISTORE
argument_list|,
name|loop
operator|.
name|getSlot
argument_list|()
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|AStatement
name|statement
range|:
name|statements
control|)
block|{
name|statement
operator|.
name|write
argument_list|(
name|writer
argument_list|,
name|globals
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|methodEscape
condition|)
block|{
switch|switch
condition|(
name|scriptInterface
operator|.
name|getExecuteMethod
argument_list|()
operator|.
name|getReturnType
argument_list|()
operator|.
name|getSort
argument_list|()
condition|)
block|{
case|case
name|org
operator|.
name|objectweb
operator|.
name|asm
operator|.
name|Type
operator|.
name|VOID
case|:
break|break;
case|case
name|org
operator|.
name|objectweb
operator|.
name|asm
operator|.
name|Type
operator|.
name|BOOLEAN
case|:
name|writer
operator|.
name|push
argument_list|(
literal|false
argument_list|)
expr_stmt|;
break|break;
case|case
name|org
operator|.
name|objectweb
operator|.
name|asm
operator|.
name|Type
operator|.
name|BYTE
case|:
name|writer
operator|.
name|push
argument_list|(
literal|0
argument_list|)
expr_stmt|;
break|break;
case|case
name|org
operator|.
name|objectweb
operator|.
name|asm
operator|.
name|Type
operator|.
name|SHORT
case|:
name|writer
operator|.
name|push
argument_list|(
literal|0
argument_list|)
expr_stmt|;
break|break;
case|case
name|org
operator|.
name|objectweb
operator|.
name|asm
operator|.
name|Type
operator|.
name|INT
case|:
name|writer
operator|.
name|push
argument_list|(
literal|0
argument_list|)
expr_stmt|;
break|break;
case|case
name|org
operator|.
name|objectweb
operator|.
name|asm
operator|.
name|Type
operator|.
name|LONG
case|:
name|writer
operator|.
name|push
argument_list|(
literal|0L
argument_list|)
expr_stmt|;
break|break;
case|case
name|org
operator|.
name|objectweb
operator|.
name|asm
operator|.
name|Type
operator|.
name|FLOAT
case|:
name|writer
operator|.
name|push
argument_list|(
literal|0f
argument_list|)
expr_stmt|;
break|break;
case|case
name|org
operator|.
name|objectweb
operator|.
name|asm
operator|.
name|Type
operator|.
name|DOUBLE
case|:
name|writer
operator|.
name|push
argument_list|(
literal|0d
argument_list|)
expr_stmt|;
break|break;
default|default:
name|writer
operator|.
name|visitInsn
argument_list|(
name|Opcodes
operator|.
name|ACONST_NULL
argument_list|)
expr_stmt|;
block|}
name|writer
operator|.
name|returnValue
argument_list|()
expr_stmt|;
block|}
name|writer
operator|.
name|mark
argument_list|(
name|endTry
argument_list|)
expr_stmt|;
name|writer
operator|.
name|goTo
argument_list|(
name|endCatch
argument_list|)
expr_stmt|;
comment|// This looks like:
comment|// } catch (PainlessExplainError e) {
comment|//   throw this.convertToScriptException(e, e.getHeaders($DEFINITION))
comment|// }
name|writer
operator|.
name|visitTryCatchBlock
argument_list|(
name|startTry
argument_list|,
name|endTry
argument_list|,
name|startExplainCatch
argument_list|,
name|PAINLESS_EXPLAIN_ERROR_TYPE
operator|.
name|getInternalName
argument_list|()
argument_list|)
expr_stmt|;
name|writer
operator|.
name|mark
argument_list|(
name|startExplainCatch
argument_list|)
expr_stmt|;
name|writer
operator|.
name|loadThis
argument_list|()
expr_stmt|;
name|writer
operator|.
name|swap
argument_list|()
expr_stmt|;
name|writer
operator|.
name|dup
argument_list|()
expr_stmt|;
name|writer
operator|.
name|getStatic
argument_list|(
name|CLASS_TYPE
argument_list|,
literal|"$DEFINITION"
argument_list|,
name|DEFINITION_TYPE
argument_list|)
expr_stmt|;
name|writer
operator|.
name|invokeVirtual
argument_list|(
name|PAINLESS_EXPLAIN_ERROR_TYPE
argument_list|,
name|PAINLESS_EXPLAIN_ERROR_GET_HEADERS_METHOD
argument_list|)
expr_stmt|;
name|writer
operator|.
name|invokeVirtual
argument_list|(
name|BASE_CLASS_TYPE
argument_list|,
name|CONVERT_TO_SCRIPT_EXCEPTION_METHOD
argument_list|)
expr_stmt|;
name|writer
operator|.
name|throwException
argument_list|()
expr_stmt|;
comment|// This looks like:
comment|// } catch (PainlessError | BootstrapMethodError | OutOfMemoryError | StackOverflowError | Exception e) {
comment|//   throw this.convertToScriptException(e, e.getHeaders())
comment|// }
comment|// We *think* it is ok to catch OutOfMemoryError and StackOverflowError because Painless is stateless
name|writer
operator|.
name|visitTryCatchBlock
argument_list|(
name|startTry
argument_list|,
name|endTry
argument_list|,
name|startOtherCatch
argument_list|,
name|PAINLESS_ERROR_TYPE
operator|.
name|getInternalName
argument_list|()
argument_list|)
expr_stmt|;
name|writer
operator|.
name|visitTryCatchBlock
argument_list|(
name|startTry
argument_list|,
name|endTry
argument_list|,
name|startOtherCatch
argument_list|,
name|BOOTSTRAP_METHOD_ERROR_TYPE
operator|.
name|getInternalName
argument_list|()
argument_list|)
expr_stmt|;
name|writer
operator|.
name|visitTryCatchBlock
argument_list|(
name|startTry
argument_list|,
name|endTry
argument_list|,
name|startOtherCatch
argument_list|,
name|OUT_OF_MEMORY_ERROR_TYPE
operator|.
name|getInternalName
argument_list|()
argument_list|)
expr_stmt|;
name|writer
operator|.
name|visitTryCatchBlock
argument_list|(
name|startTry
argument_list|,
name|endTry
argument_list|,
name|startOtherCatch
argument_list|,
name|STACK_OVERFLOW_ERROR_TYPE
operator|.
name|getInternalName
argument_list|()
argument_list|)
expr_stmt|;
name|writer
operator|.
name|visitTryCatchBlock
argument_list|(
name|startTry
argument_list|,
name|endTry
argument_list|,
name|startOtherCatch
argument_list|,
name|EXCEPTION_TYPE
operator|.
name|getInternalName
argument_list|()
argument_list|)
expr_stmt|;
name|writer
operator|.
name|mark
argument_list|(
name|startOtherCatch
argument_list|)
expr_stmt|;
name|writer
operator|.
name|loadThis
argument_list|()
expr_stmt|;
name|writer
operator|.
name|swap
argument_list|()
expr_stmt|;
name|writer
operator|.
name|invokeStatic
argument_list|(
name|COLLECTIONS_TYPE
argument_list|,
name|EMPTY_MAP_METHOD
argument_list|)
expr_stmt|;
name|writer
operator|.
name|invokeVirtual
argument_list|(
name|BASE_CLASS_TYPE
argument_list|,
name|CONVERT_TO_SCRIPT_EXCEPTION_METHOD
argument_list|)
expr_stmt|;
name|writer
operator|.
name|throwException
argument_list|()
expr_stmt|;
name|writer
operator|.
name|mark
argument_list|(
name|endCatch
argument_list|)
expr_stmt|;
block|}
DECL|method|getStatements
specifier|public
name|BitSet
name|getStatements
parameter_list|()
block|{
return|return
name|globals
operator|.
name|getStatements
argument_list|()
return|;
block|}
DECL|method|getBytes
specifier|public
name|byte
index|[]
name|getBytes
parameter_list|()
block|{
return|return
name|bytes
return|;
block|}
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|List
argument_list|<
name|Object
argument_list|>
name|subs
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|functions
operator|.
name|size
argument_list|()
operator|+
name|statements
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
name|subs
operator|.
name|addAll
argument_list|(
name|functions
argument_list|)
expr_stmt|;
name|subs
operator|.
name|addAll
argument_list|(
name|statements
argument_list|)
expr_stmt|;
return|return
name|multilineToString
argument_list|(
name|emptyList
argument_list|()
argument_list|,
name|subs
argument_list|)
return|;
block|}
block|}
end_class

end_unit

