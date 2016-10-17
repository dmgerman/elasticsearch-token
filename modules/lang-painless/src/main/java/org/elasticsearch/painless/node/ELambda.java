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
name|Definition
operator|.
name|Type
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
name|node
operator|.
name|SFunction
operator|.
name|FunctionReserved
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
name|FunctionRef
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
name|objectweb
operator|.
name|asm
operator|.
name|Opcodes
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
name|LambdaMetafactory
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
name|Collections
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
name|org
operator|.
name|elasticsearch
operator|.
name|painless
operator|.
name|WriterConstants
operator|.
name|LAMBDA_BOOTSTRAP_HANDLE
import|;
end_import

begin_comment
comment|/**  * Lambda expression node.  *<p>  * This can currently only be the direct argument of a call (method/constructor).  * When the argument is of a known type, it uses  *<a href="http://cr.openjdk.java.net/~briangoetz/lambda/lambda-translation.html">  * Java's lambda translation</a>. However, if its a def call, then we don't have  * enough information, and have to defer this until link time. In that case a placeholder  * and all captures are pushed onto the stack and folded into the signature of the parent call.  *<p>  * For example:  *<br>  * {@code def list = new ArrayList(); int capture = 0; list.sort((x,y) -> x - y + capture)}  *<br>  * is converted into a call (pseudocode) such as:  *<br>  * {@code sort(list, lambda$0, capture)}  *<br>  * At link time, when we know the interface type, this is decomposed with MethodHandle  * combinators back into (pseudocode):  *<br>  * {@code sort(list, lambda$0(capture))}  */
end_comment

begin_class
DECL|class|ELambda
specifier|public
specifier|final
class|class
name|ELambda
extends|extends
name|AExpression
implements|implements
name|ILambda
block|{
DECL|field|name
specifier|private
specifier|final
name|String
name|name
decl_stmt|;
DECL|field|reserved
specifier|private
specifier|final
name|FunctionReserved
name|reserved
decl_stmt|;
DECL|field|paramTypeStrs
specifier|private
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|paramTypeStrs
decl_stmt|;
DECL|field|paramNameStrs
specifier|private
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|paramNameStrs
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
comment|// desugared synthetic method (lambda body)
DECL|field|desugared
specifier|private
name|SFunction
name|desugared
decl_stmt|;
comment|// captured variables
DECL|field|captures
specifier|private
name|List
argument_list|<
name|Variable
argument_list|>
name|captures
decl_stmt|;
comment|// static parent, static lambda
DECL|field|ref
specifier|private
name|FunctionRef
name|ref
decl_stmt|;
comment|// dynamic parent, deferred until link time
DECL|field|defPointer
specifier|private
name|String
name|defPointer
decl_stmt|;
DECL|method|ELambda
specifier|public
name|ELambda
parameter_list|(
name|String
name|name
parameter_list|,
name|FunctionReserved
name|reserved
parameter_list|,
name|Location
name|location
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|paramTypes
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|paramNames
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
name|reserved
operator|=
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|reserved
argument_list|)
expr_stmt|;
name|this
operator|.
name|paramTypeStrs
operator|=
name|Collections
operator|.
name|unmodifiableList
argument_list|(
name|paramTypes
argument_list|)
expr_stmt|;
name|this
operator|.
name|paramNameStrs
operator|=
name|Collections
operator|.
name|unmodifiableList
argument_list|(
name|paramNames
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
name|extractVariables
argument_list|(
name|variables
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|analyze
name|void
name|analyze
parameter_list|(
name|Locals
name|locals
parameter_list|)
block|{
specifier|final
name|Type
name|returnType
decl_stmt|;
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|actualParamTypeStrs
decl_stmt|;
name|Method
name|interfaceMethod
decl_stmt|;
comment|// inspect the target first, set interface method if we know it.
if|if
condition|(
name|expected
operator|==
literal|null
condition|)
block|{
name|interfaceMethod
operator|=
literal|null
expr_stmt|;
comment|// we don't know anything: treat as def
name|returnType
operator|=
name|Definition
operator|.
name|DEF_TYPE
expr_stmt|;
comment|// don't infer any types
name|actualParamTypeStrs
operator|=
name|paramTypeStrs
expr_stmt|;
block|}
else|else
block|{
comment|// we know the method statically, infer return type and any unknown/def types
name|interfaceMethod
operator|=
name|expected
operator|.
name|struct
operator|.
name|getFunctionalMethod
argument_list|()
expr_stmt|;
if|if
condition|(
name|interfaceMethod
operator|==
literal|null
condition|)
block|{
throw|throw
name|createError
argument_list|(
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Cannot pass lambda to ["
operator|+
name|expected
operator|.
name|name
operator|+
literal|"], not a functional interface"
argument_list|)
argument_list|)
throw|;
block|}
comment|// check arity before we manipulate parameters
if|if
condition|(
name|interfaceMethod
operator|.
name|arguments
operator|.
name|size
argument_list|()
operator|!=
name|paramTypeStrs
operator|.
name|size
argument_list|()
condition|)
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Incorrect number of parameters for ["
operator|+
name|interfaceMethod
operator|.
name|name
operator|+
literal|"] in ["
operator|+
name|expected
operator|.
name|clazz
operator|+
literal|"]"
argument_list|)
throw|;
comment|// for method invocation, its allowed to ignore the return value
if|if
condition|(
name|interfaceMethod
operator|.
name|rtn
operator|==
name|Definition
operator|.
name|VOID_TYPE
condition|)
block|{
name|returnType
operator|=
name|Definition
operator|.
name|DEF_TYPE
expr_stmt|;
block|}
else|else
block|{
name|returnType
operator|=
name|interfaceMethod
operator|.
name|rtn
expr_stmt|;
block|}
comment|// replace any def types with the actual type (which could still be def)
name|actualParamTypeStrs
operator|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|paramTypeStrs
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|String
name|paramType
init|=
name|paramTypeStrs
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|paramType
operator|.
name|equals
argument_list|(
name|Definition
operator|.
name|DEF_TYPE
operator|.
name|name
argument_list|)
condition|)
block|{
name|actualParamTypeStrs
operator|.
name|add
argument_list|(
name|interfaceMethod
operator|.
name|arguments
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|name
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|actualParamTypeStrs
operator|.
name|add
argument_list|(
name|paramType
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|// gather any variables used by the lambda body first.
name|Set
argument_list|<
name|String
argument_list|>
name|variables
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
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
name|extractVariables
argument_list|(
name|variables
argument_list|)
expr_stmt|;
block|}
comment|// any of those variables defined in our scope need to be captured
name|captures
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
for|for
control|(
name|String
name|variable
range|:
name|variables
control|)
block|{
if|if
condition|(
name|locals
operator|.
name|hasVariable
argument_list|(
name|variable
argument_list|)
condition|)
block|{
name|captures
operator|.
name|add
argument_list|(
name|locals
operator|.
name|getVariable
argument_list|(
name|location
argument_list|,
name|variable
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|// prepend capture list to lambda's arguments
name|List
argument_list|<
name|String
argument_list|>
name|paramTypes
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|paramNames
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|Variable
name|var
range|:
name|captures
control|)
block|{
name|paramTypes
operator|.
name|add
argument_list|(
name|var
operator|.
name|type
operator|.
name|name
argument_list|)
expr_stmt|;
name|paramNames
operator|.
name|add
argument_list|(
name|var
operator|.
name|name
argument_list|)
expr_stmt|;
block|}
name|paramTypes
operator|.
name|addAll
argument_list|(
name|actualParamTypeStrs
argument_list|)
expr_stmt|;
name|paramNames
operator|.
name|addAll
argument_list|(
name|paramNameStrs
argument_list|)
expr_stmt|;
comment|// desugar lambda body into a synthetic method
name|desugared
operator|=
operator|new
name|SFunction
argument_list|(
name|reserved
argument_list|,
name|location
argument_list|,
name|returnType
operator|.
name|name
argument_list|,
name|name
argument_list|,
name|paramTypes
argument_list|,
name|paramNames
argument_list|,
name|statements
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|desugared
operator|.
name|generateSignature
argument_list|()
expr_stmt|;
name|desugared
operator|.
name|analyze
argument_list|(
name|Locals
operator|.
name|newLambdaScope
argument_list|(
name|locals
operator|.
name|getProgramScope
argument_list|()
argument_list|,
name|returnType
argument_list|,
name|desugared
operator|.
name|parameters
argument_list|,
name|captures
operator|.
name|size
argument_list|()
argument_list|,
name|reserved
operator|.
name|getMaxLoopCounter
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// setup method reference to synthetic method
if|if
condition|(
name|expected
operator|==
literal|null
condition|)
block|{
name|ref
operator|=
literal|null
expr_stmt|;
name|actual
operator|=
name|Definition
operator|.
name|getType
argument_list|(
literal|"String"
argument_list|)
expr_stmt|;
name|defPointer
operator|=
literal|"Sthis."
operator|+
name|name
operator|+
literal|","
operator|+
name|captures
operator|.
name|size
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|defPointer
operator|=
literal|null
expr_stmt|;
try|try
block|{
name|ref
operator|=
operator|new
name|FunctionRef
argument_list|(
name|expected
argument_list|,
name|interfaceMethod
argument_list|,
name|desugared
operator|.
name|method
argument_list|,
name|captures
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
throw|throw
name|createError
argument_list|(
name|e
argument_list|)
throw|;
block|}
name|actual
operator|=
name|expected
expr_stmt|;
block|}
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
name|writer
operator|.
name|writeDebugInfo
argument_list|(
name|location
argument_list|)
expr_stmt|;
if|if
condition|(
name|ref
operator|!=
literal|null
condition|)
block|{
name|writer
operator|.
name|writeDebugInfo
argument_list|(
name|location
argument_list|)
expr_stmt|;
comment|// load captures
for|for
control|(
name|Variable
name|capture
range|:
name|captures
control|)
block|{
name|writer
operator|.
name|visitVarInsn
argument_list|(
name|capture
operator|.
name|type
operator|.
name|type
operator|.
name|getOpcode
argument_list|(
name|Opcodes
operator|.
name|ILOAD
argument_list|)
argument_list|,
name|capture
operator|.
name|getSlot
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// convert MethodTypes to asm Type for the constant pool.
name|String
name|invokedType
init|=
name|ref
operator|.
name|invokedType
operator|.
name|toMethodDescriptorString
argument_list|()
decl_stmt|;
name|org
operator|.
name|objectweb
operator|.
name|asm
operator|.
name|Type
name|samMethodType
init|=
name|org
operator|.
name|objectweb
operator|.
name|asm
operator|.
name|Type
operator|.
name|getMethodType
argument_list|(
name|ref
operator|.
name|samMethodType
operator|.
name|toMethodDescriptorString
argument_list|()
argument_list|)
decl_stmt|;
name|org
operator|.
name|objectweb
operator|.
name|asm
operator|.
name|Type
name|interfaceType
init|=
name|org
operator|.
name|objectweb
operator|.
name|asm
operator|.
name|Type
operator|.
name|getMethodType
argument_list|(
name|ref
operator|.
name|interfaceMethodType
operator|.
name|toMethodDescriptorString
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|ref
operator|.
name|needsBridges
argument_list|()
condition|)
block|{
name|writer
operator|.
name|invokeDynamic
argument_list|(
name|ref
operator|.
name|invokedName
argument_list|,
name|invokedType
argument_list|,
name|LAMBDA_BOOTSTRAP_HANDLE
argument_list|,
name|samMethodType
argument_list|,
name|ref
operator|.
name|implMethodASM
argument_list|,
name|samMethodType
argument_list|,
name|LambdaMetafactory
operator|.
name|FLAG_BRIDGES
argument_list|,
literal|1
argument_list|,
name|interfaceType
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|writer
operator|.
name|invokeDynamic
argument_list|(
name|ref
operator|.
name|invokedName
argument_list|,
name|invokedType
argument_list|,
name|LAMBDA_BOOTSTRAP_HANDLE
argument_list|,
name|samMethodType
argument_list|,
name|ref
operator|.
name|implMethodASM
argument_list|,
name|samMethodType
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
comment|// placeholder
name|writer
operator|.
name|push
argument_list|(
operator|(
name|String
operator|)
literal|null
argument_list|)
expr_stmt|;
comment|// load captures
for|for
control|(
name|Variable
name|capture
range|:
name|captures
control|)
block|{
name|writer
operator|.
name|visitVarInsn
argument_list|(
name|capture
operator|.
name|type
operator|.
name|type
operator|.
name|getOpcode
argument_list|(
name|Opcodes
operator|.
name|ILOAD
argument_list|)
argument_list|,
name|capture
operator|.
name|getSlot
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|// add synthetic method to the queue to be written
name|globals
operator|.
name|addSyntheticMethod
argument_list|(
name|desugared
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getPointer
specifier|public
name|String
name|getPointer
parameter_list|()
block|{
return|return
name|defPointer
return|;
block|}
annotation|@
name|Override
DECL|method|getCaptures
specifier|public
name|org
operator|.
name|objectweb
operator|.
name|asm
operator|.
name|Type
index|[]
name|getCaptures
parameter_list|()
block|{
name|org
operator|.
name|objectweb
operator|.
name|asm
operator|.
name|Type
index|[]
name|types
init|=
operator|new
name|org
operator|.
name|objectweb
operator|.
name|asm
operator|.
name|Type
index|[
name|captures
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|types
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|types
index|[
name|i
index|]
operator|=
name|captures
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|type
operator|.
name|type
expr_stmt|;
block|}
return|return
name|types
return|;
block|}
block|}
end_class

end_unit
