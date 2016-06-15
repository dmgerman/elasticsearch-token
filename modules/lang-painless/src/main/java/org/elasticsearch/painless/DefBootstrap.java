begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
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
name|common
operator|.
name|SuppressForbidden
import|;
end_import

begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|invoke
operator|.
name|CallSite
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
name|MethodHandle
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
name|MethodHandles
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
name|MethodHandles
operator|.
name|Lookup
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
name|MethodType
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
name|MutableCallSite
import|;
end_import

begin_comment
comment|/**  * Painless invokedynamic bootstrap for the call site.  *<p>  * Has 7 flavors (passed as static bootstrap parameters): dynamic method call,  * dynamic field load (getter), and dynamic field store (setter), dynamic array load,  * dynamic array store, iterator, and method reference.  *<p>  * When a new type is encountered at the call site, we lookup from the appropriate  * whitelist, and cache with a guard. If we encounter too many types, we stop caching.  *<p>  * Based on the cascaded inlining cache from the JSR 292 cookbook  * (https://code.google.com/archive/p/jsr292-cookbook/, BSD license)  */
end_comment

begin_comment
comment|// NOTE: this class must be public, because generated painless classes are in a different classloader,
end_comment

begin_comment
comment|// and it needs to be accessible by that code.
end_comment

begin_class
DECL|class|DefBootstrap
specifier|public
specifier|final
class|class
name|DefBootstrap
block|{
DECL|method|DefBootstrap
specifier|private
name|DefBootstrap
parameter_list|()
block|{}
comment|// no instance!
comment|// NOTE: these must be primitive types, see https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-6.html#jvms-6.5.invokedynamic
comment|/** static bootstrap parameter indicating a dynamic method call, e.g. foo.bar(...) */
DECL|field|METHOD_CALL
specifier|public
specifier|static
specifier|final
name|int
name|METHOD_CALL
init|=
literal|0
decl_stmt|;
comment|/** static bootstrap parameter indicating a dynamic load (getter), e.g. baz = foo.bar */
DECL|field|LOAD
specifier|public
specifier|static
specifier|final
name|int
name|LOAD
init|=
literal|1
decl_stmt|;
comment|/** static bootstrap parameter indicating a dynamic store (setter), e.g. foo.bar = baz */
DECL|field|STORE
specifier|public
specifier|static
specifier|final
name|int
name|STORE
init|=
literal|2
decl_stmt|;
comment|/** static bootstrap parameter indicating a dynamic array load, e.g. baz = foo[bar] */
DECL|field|ARRAY_LOAD
specifier|public
specifier|static
specifier|final
name|int
name|ARRAY_LOAD
init|=
literal|3
decl_stmt|;
comment|/** static bootstrap parameter indicating a dynamic array store, e.g. foo[bar] = baz */
DECL|field|ARRAY_STORE
specifier|public
specifier|static
specifier|final
name|int
name|ARRAY_STORE
init|=
literal|4
decl_stmt|;
comment|/** static bootstrap parameter indicating a dynamic iteration, e.g. for (x : y) */
DECL|field|ITERATOR
specifier|public
specifier|static
specifier|final
name|int
name|ITERATOR
init|=
literal|5
decl_stmt|;
comment|/** static bootstrap parameter indicating a dynamic method reference, e.g. foo::bar */
DECL|field|REFERENCE
specifier|public
specifier|static
specifier|final
name|int
name|REFERENCE
init|=
literal|6
decl_stmt|;
comment|/** static bootstrap parameter indicating a unary math operator, e.g. ~foo */
DECL|field|UNARY_OPERATOR
specifier|public
specifier|static
specifier|final
name|int
name|UNARY_OPERATOR
init|=
literal|7
decl_stmt|;
comment|/** static bootstrap parameter indicating a binary math operator, e.g. foo / bar */
DECL|field|BINARY_OPERATOR
specifier|public
specifier|static
specifier|final
name|int
name|BINARY_OPERATOR
init|=
literal|8
decl_stmt|;
comment|/** static bootstrap parameter indicating a shift operator, e.g. foo&gt;&gt; bar */
DECL|field|SHIFT_OPERATOR
specifier|public
specifier|static
specifier|final
name|int
name|SHIFT_OPERATOR
init|=
literal|9
decl_stmt|;
comment|/**      * CallSite that implements the polymorphic inlining cache (PIC).      */
DECL|class|PIC
specifier|static
specifier|final
class|class
name|PIC
extends|extends
name|MutableCallSite
block|{
comment|/** maximum number of types before we go megamorphic */
DECL|field|MAX_DEPTH
specifier|static
specifier|final
name|int
name|MAX_DEPTH
init|=
literal|5
decl_stmt|;
DECL|field|lookup
specifier|private
specifier|final
name|Lookup
name|lookup
decl_stmt|;
DECL|field|name
specifier|private
specifier|final
name|String
name|name
decl_stmt|;
DECL|field|flavor
specifier|private
specifier|final
name|int
name|flavor
decl_stmt|;
DECL|field|args
specifier|private
specifier|final
name|Object
index|[]
name|args
decl_stmt|;
DECL|field|depth
name|int
name|depth
decl_stmt|;
comment|// pkg-protected for testing
DECL|method|PIC
name|PIC
parameter_list|(
name|Lookup
name|lookup
parameter_list|,
name|String
name|name
parameter_list|,
name|MethodType
name|type
parameter_list|,
name|int
name|flavor
parameter_list|,
name|Object
index|[]
name|args
parameter_list|)
block|{
name|super
argument_list|(
name|type
argument_list|)
expr_stmt|;
name|this
operator|.
name|lookup
operator|=
name|lookup
expr_stmt|;
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
name|this
operator|.
name|flavor
operator|=
name|flavor
expr_stmt|;
name|this
operator|.
name|args
operator|=
name|args
expr_stmt|;
comment|// For operators use a monomorphic cache, fallback is fast.
comment|// Just start with a depth of MAX-1, to keep it a constant.
if|if
condition|(
name|flavor
operator|==
name|UNARY_OPERATOR
operator|||
name|flavor
operator|==
name|BINARY_OPERATOR
operator|||
name|flavor
operator|==
name|SHIFT_OPERATOR
condition|)
block|{
name|depth
operator|=
name|MAX_DEPTH
operator|-
literal|1
expr_stmt|;
block|}
specifier|final
name|MethodHandle
name|fallback
init|=
name|FALLBACK
operator|.
name|bindTo
argument_list|(
name|this
argument_list|)
operator|.
name|asCollector
argument_list|(
name|Object
index|[]
operator|.
expr|class
argument_list|,
name|type
operator|.
name|parameterCount
argument_list|()
argument_list|)
operator|.
name|asType
argument_list|(
name|type
argument_list|)
decl_stmt|;
name|setTarget
argument_list|(
name|fallback
argument_list|)
expr_stmt|;
block|}
comment|/**          * guard method for inline caching: checks the receiver's class is the same          * as the cached class          */
DECL|method|checkClass
specifier|static
name|boolean
name|checkClass
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|clazz
parameter_list|,
name|Object
name|receiver
parameter_list|)
block|{
return|return
name|receiver
operator|.
name|getClass
argument_list|()
operator|==
name|clazz
return|;
block|}
comment|/**          * guard method for inline caching: checks the receiver's class and the first argument          * are the same as the cached receiver and first argument.          */
DECL|method|checkBinary
specifier|static
name|boolean
name|checkBinary
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|left
parameter_list|,
name|Class
argument_list|<
name|?
argument_list|>
name|right
parameter_list|,
name|Object
name|leftObject
parameter_list|,
name|Object
name|rightObject
parameter_list|)
block|{
return|return
name|leftObject
operator|.
name|getClass
argument_list|()
operator|==
name|left
operator|&&
name|rightObject
operator|.
name|getClass
argument_list|()
operator|==
name|right
return|;
block|}
comment|/**          * guard method for inline caching: checks the first argument is the same          * as the cached first argument.          */
DECL|method|checkBinaryArg
specifier|static
name|boolean
name|checkBinaryArg
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|left
parameter_list|,
name|Class
argument_list|<
name|?
argument_list|>
name|right
parameter_list|,
name|Object
name|leftObject
parameter_list|,
name|Object
name|rightObject
parameter_list|)
block|{
return|return
name|rightObject
operator|.
name|getClass
argument_list|()
operator|==
name|right
return|;
block|}
comment|/**          * Does a slow lookup against the whitelist.          */
DECL|method|lookup
specifier|private
name|MethodHandle
name|lookup
parameter_list|(
name|int
name|flavor
parameter_list|,
name|String
name|name
parameter_list|,
name|Object
index|[]
name|args
parameter_list|)
throws|throws
name|Throwable
block|{
switch|switch
condition|(
name|flavor
condition|)
block|{
case|case
name|METHOD_CALL
case|:
return|return
name|Def
operator|.
name|lookupMethod
argument_list|(
name|lookup
argument_list|,
name|type
argument_list|()
argument_list|,
name|args
index|[
literal|0
index|]
operator|.
name|getClass
argument_list|()
argument_list|,
name|name
argument_list|,
name|args
argument_list|,
operator|(
name|Long
operator|)
name|this
operator|.
name|args
index|[
literal|0
index|]
argument_list|)
return|;
case|case
name|LOAD
case|:
return|return
name|Def
operator|.
name|lookupGetter
argument_list|(
name|args
index|[
literal|0
index|]
operator|.
name|getClass
argument_list|()
argument_list|,
name|name
argument_list|)
return|;
case|case
name|STORE
case|:
return|return
name|Def
operator|.
name|lookupSetter
argument_list|(
name|args
index|[
literal|0
index|]
operator|.
name|getClass
argument_list|()
argument_list|,
name|name
argument_list|)
return|;
case|case
name|ARRAY_LOAD
case|:
return|return
name|Def
operator|.
name|lookupArrayLoad
argument_list|(
name|args
index|[
literal|0
index|]
operator|.
name|getClass
argument_list|()
argument_list|)
return|;
case|case
name|ARRAY_STORE
case|:
return|return
name|Def
operator|.
name|lookupArrayStore
argument_list|(
name|args
index|[
literal|0
index|]
operator|.
name|getClass
argument_list|()
argument_list|)
return|;
case|case
name|ITERATOR
case|:
return|return
name|Def
operator|.
name|lookupIterator
argument_list|(
name|args
index|[
literal|0
index|]
operator|.
name|getClass
argument_list|()
argument_list|)
return|;
case|case
name|REFERENCE
case|:
return|return
name|Def
operator|.
name|lookupReference
argument_list|(
name|lookup
argument_list|,
operator|(
name|String
operator|)
name|this
operator|.
name|args
index|[
literal|0
index|]
argument_list|,
name|args
index|[
literal|0
index|]
operator|.
name|getClass
argument_list|()
argument_list|,
name|name
argument_list|)
return|;
case|case
name|UNARY_OPERATOR
case|:
case|case
name|SHIFT_OPERATOR
case|:
comment|// shifts are treated as unary, as java allows long arguments without a cast (but bits are ignored)
return|return
name|DefMath
operator|.
name|lookupUnary
argument_list|(
name|args
index|[
literal|0
index|]
operator|.
name|getClass
argument_list|()
argument_list|,
name|name
argument_list|)
return|;
case|case
name|BINARY_OPERATOR
case|:
if|if
condition|(
name|args
index|[
literal|0
index|]
operator|==
literal|null
operator|||
name|args
index|[
literal|1
index|]
operator|==
literal|null
condition|)
block|{
return|return
name|getGeneric
argument_list|(
name|flavor
argument_list|,
name|name
argument_list|)
return|;
comment|// can handle nulls
block|}
else|else
block|{
return|return
name|DefMath
operator|.
name|lookupBinary
argument_list|(
name|args
index|[
literal|0
index|]
operator|.
name|getClass
argument_list|()
argument_list|,
name|args
index|[
literal|1
index|]
operator|.
name|getClass
argument_list|()
argument_list|,
name|name
argument_list|)
return|;
block|}
default|default:
throw|throw
operator|new
name|AssertionError
argument_list|()
throw|;
block|}
block|}
comment|/**          * Installs a permanent, generic solution that works with any parameter types, if possible.          */
DECL|method|getGeneric
specifier|private
name|MethodHandle
name|getGeneric
parameter_list|(
name|int
name|flavor
parameter_list|,
name|String
name|name
parameter_list|)
throws|throws
name|Throwable
block|{
switch|switch
condition|(
name|flavor
condition|)
block|{
case|case
name|UNARY_OPERATOR
case|:
case|case
name|BINARY_OPERATOR
case|:
case|case
name|SHIFT_OPERATOR
case|:
return|return
name|DefMath
operator|.
name|lookupGeneric
argument_list|(
name|name
argument_list|)
return|;
default|default:
return|return
literal|null
return|;
block|}
block|}
comment|/**          * Called when a new type is encountered (or, when we have encountered more than {@code MAX_DEPTH}          * types at this call site and given up on caching).          */
annotation|@
name|SuppressForbidden
argument_list|(
name|reason
operator|=
literal|"slow path"
argument_list|)
DECL|method|fallback
name|Object
name|fallback
parameter_list|(
name|Object
index|[]
name|args
parameter_list|)
throws|throws
name|Throwable
block|{
if|if
condition|(
name|depth
operator|>=
name|MAX_DEPTH
condition|)
block|{
comment|// caching defeated
name|MethodHandle
name|generic
init|=
name|getGeneric
argument_list|(
name|flavor
argument_list|,
name|name
argument_list|)
decl_stmt|;
if|if
condition|(
name|generic
operator|!=
literal|null
condition|)
block|{
name|setTarget
argument_list|(
name|generic
operator|.
name|asType
argument_list|(
name|type
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|generic
operator|.
name|invokeWithArguments
argument_list|(
name|args
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|lookup
argument_list|(
name|flavor
argument_list|,
name|name
argument_list|,
name|args
argument_list|)
operator|.
name|invokeWithArguments
argument_list|(
name|args
argument_list|)
return|;
block|}
block|}
specifier|final
name|MethodType
name|type
init|=
name|type
argument_list|()
decl_stmt|;
specifier|final
name|MethodHandle
name|target
init|=
name|lookup
argument_list|(
name|flavor
argument_list|,
name|name
argument_list|,
name|args
argument_list|)
operator|.
name|asType
argument_list|(
name|type
argument_list|)
decl_stmt|;
specifier|final
name|MethodHandle
name|test
decl_stmt|;
if|if
condition|(
name|flavor
operator|==
name|BINARY_OPERATOR
operator|||
name|flavor
operator|==
name|SHIFT_OPERATOR
condition|)
block|{
comment|// some binary operators support nulls, we handle them separate
name|Class
argument_list|<
name|?
argument_list|>
name|clazz0
init|=
name|args
index|[
literal|0
index|]
operator|==
literal|null
condition|?
literal|null
else|:
name|args
index|[
literal|0
index|]
operator|.
name|getClass
argument_list|()
decl_stmt|;
name|Class
argument_list|<
name|?
argument_list|>
name|clazz1
init|=
name|args
index|[
literal|1
index|]
operator|==
literal|null
condition|?
literal|null
else|:
name|args
index|[
literal|1
index|]
operator|.
name|getClass
argument_list|()
decl_stmt|;
if|if
condition|(
name|type
operator|.
name|parameterType
argument_list|(
literal|1
argument_list|)
operator|!=
name|Object
operator|.
name|class
condition|)
block|{
comment|// case 1: only the receiver is unknown, just check that
name|MethodHandle
name|unaryTest
init|=
name|CHECK_CLASS
operator|.
name|bindTo
argument_list|(
name|clazz0
argument_list|)
decl_stmt|;
name|test
operator|=
name|unaryTest
operator|.
name|asType
argument_list|(
name|unaryTest
operator|.
name|type
argument_list|()
operator|.
name|changeParameterType
argument_list|(
literal|0
argument_list|,
name|type
operator|.
name|parameterType
argument_list|(
literal|0
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|type
operator|.
name|parameterType
argument_list|(
literal|0
argument_list|)
operator|!=
name|Object
operator|.
name|class
condition|)
block|{
comment|// case 2: only the argument is unknown, just check that
name|MethodHandle
name|unaryTest
init|=
name|CHECK_BINARY_ARG
operator|.
name|bindTo
argument_list|(
name|clazz0
argument_list|)
operator|.
name|bindTo
argument_list|(
name|clazz1
argument_list|)
decl_stmt|;
name|test
operator|=
name|unaryTest
operator|.
name|asType
argument_list|(
name|unaryTest
operator|.
name|type
argument_list|()
operator|.
name|changeParameterType
argument_list|(
literal|0
argument_list|,
name|type
operator|.
name|parameterType
argument_list|(
literal|0
argument_list|)
argument_list|)
operator|.
name|changeParameterType
argument_list|(
literal|1
argument_list|,
name|type
operator|.
name|parameterType
argument_list|(
literal|1
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// case 3: check both receiver and argument
name|MethodHandle
name|binaryTest
init|=
name|CHECK_BINARY
operator|.
name|bindTo
argument_list|(
name|clazz0
argument_list|)
operator|.
name|bindTo
argument_list|(
name|clazz1
argument_list|)
decl_stmt|;
name|test
operator|=
name|binaryTest
operator|.
name|asType
argument_list|(
name|binaryTest
operator|.
name|type
argument_list|()
operator|.
name|changeParameterType
argument_list|(
literal|0
argument_list|,
name|type
operator|.
name|parameterType
argument_list|(
literal|0
argument_list|)
argument_list|)
operator|.
name|changeParameterType
argument_list|(
literal|1
argument_list|,
name|type
operator|.
name|parameterType
argument_list|(
literal|1
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|MethodHandle
name|receiverTest
init|=
name|CHECK_CLASS
operator|.
name|bindTo
argument_list|(
name|args
index|[
literal|0
index|]
operator|.
name|getClass
argument_list|()
argument_list|)
decl_stmt|;
name|test
operator|=
name|receiverTest
operator|.
name|asType
argument_list|(
name|receiverTest
operator|.
name|type
argument_list|()
operator|.
name|changeParameterType
argument_list|(
literal|0
argument_list|,
name|type
operator|.
name|parameterType
argument_list|(
literal|0
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|MethodHandle
name|guard
init|=
name|MethodHandles
operator|.
name|guardWithTest
argument_list|(
name|test
argument_list|,
name|target
argument_list|,
name|getTarget
argument_list|()
argument_list|)
decl_stmt|;
comment|// very special cases, where even the receiver can be null (see JLS rules for string concat)
comment|// we wrap + with an NPE catcher, and use our generic method in that case.
if|if
condition|(
name|flavor
operator|==
name|BINARY_OPERATOR
operator|&&
literal|"add"
operator|.
name|equals
argument_list|(
name|name
argument_list|)
operator|||
literal|"eq"
operator|.
name|equals
argument_list|(
name|name
argument_list|)
condition|)
block|{
name|MethodHandle
name|handler
init|=
name|MethodHandles
operator|.
name|dropArguments
argument_list|(
name|getGeneric
argument_list|(
name|flavor
argument_list|,
name|name
argument_list|)
operator|.
name|asType
argument_list|(
name|type
argument_list|()
argument_list|)
argument_list|,
literal|0
argument_list|,
name|NullPointerException
operator|.
name|class
argument_list|)
decl_stmt|;
name|guard
operator|=
name|MethodHandles
operator|.
name|catchException
argument_list|(
name|guard
argument_list|,
name|NullPointerException
operator|.
name|class
argument_list|,
name|handler
argument_list|)
expr_stmt|;
block|}
name|depth
operator|++
expr_stmt|;
name|setTarget
argument_list|(
name|guard
argument_list|)
expr_stmt|;
return|return
name|target
operator|.
name|invokeWithArguments
argument_list|(
name|args
argument_list|)
return|;
block|}
DECL|field|CHECK_CLASS
specifier|private
specifier|static
specifier|final
name|MethodHandle
name|CHECK_CLASS
decl_stmt|;
DECL|field|CHECK_BINARY
specifier|private
specifier|static
specifier|final
name|MethodHandle
name|CHECK_BINARY
decl_stmt|;
DECL|field|CHECK_BINARY_ARG
specifier|private
specifier|static
specifier|final
name|MethodHandle
name|CHECK_BINARY_ARG
decl_stmt|;
DECL|field|FALLBACK
specifier|private
specifier|static
specifier|final
name|MethodHandle
name|FALLBACK
decl_stmt|;
static|static
block|{
specifier|final
name|Lookup
name|lookup
init|=
name|MethodHandles
operator|.
name|lookup
argument_list|()
decl_stmt|;
try|try
block|{
name|CHECK_CLASS
operator|=
name|lookup
operator|.
name|findStatic
argument_list|(
name|lookup
operator|.
name|lookupClass
argument_list|()
argument_list|,
literal|"checkClass"
argument_list|,
name|MethodType
operator|.
name|methodType
argument_list|(
name|boolean
operator|.
name|class
argument_list|,
name|Class
operator|.
name|class
argument_list|,
name|Object
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|CHECK_BINARY
operator|=
name|lookup
operator|.
name|findStatic
argument_list|(
name|lookup
operator|.
name|lookupClass
argument_list|()
argument_list|,
literal|"checkBinary"
argument_list|,
name|MethodType
operator|.
name|methodType
argument_list|(
name|boolean
operator|.
name|class
argument_list|,
name|Class
operator|.
name|class
argument_list|,
name|Class
operator|.
name|class
argument_list|,
name|Object
operator|.
name|class
argument_list|,
name|Object
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|CHECK_BINARY_ARG
operator|=
name|lookup
operator|.
name|findStatic
argument_list|(
name|lookup
operator|.
name|lookupClass
argument_list|()
argument_list|,
literal|"checkBinaryArg"
argument_list|,
name|MethodType
operator|.
name|methodType
argument_list|(
name|boolean
operator|.
name|class
argument_list|,
name|Class
operator|.
name|class
argument_list|,
name|Class
operator|.
name|class
argument_list|,
name|Object
operator|.
name|class
argument_list|,
name|Object
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|FALLBACK
operator|=
name|lookup
operator|.
name|findVirtual
argument_list|(
name|lookup
operator|.
name|lookupClass
argument_list|()
argument_list|,
literal|"fallback"
argument_list|,
name|MethodType
operator|.
name|methodType
argument_list|(
name|Object
operator|.
name|class
argument_list|,
name|Object
index|[]
operator|.
expr|class
argument_list|)
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
operator|new
name|AssertionError
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
block|}
comment|/**      * invokeDynamic bootstrap method      *<p>      * In addition to ordinary parameters, we also take a static parameter {@code flavor} which      * tells us what type of dynamic call it is (and which part of whitelist to look at).      *<p>      * see https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-6.html#jvms-6.5.invokedynamic      */
DECL|method|bootstrap
specifier|public
specifier|static
name|CallSite
name|bootstrap
parameter_list|(
name|Lookup
name|lookup
parameter_list|,
name|String
name|name
parameter_list|,
name|MethodType
name|type
parameter_list|,
name|int
name|flavor
parameter_list|,
name|Object
modifier|...
name|args
parameter_list|)
block|{
comment|// validate arguments
switch|switch
condition|(
name|flavor
condition|)
block|{
case|case
name|METHOD_CALL
case|:
if|if
condition|(
name|args
operator|.
name|length
operator|!=
literal|1
condition|)
block|{
throw|throw
operator|new
name|BootstrapMethodError
argument_list|(
literal|"Invalid number of parameters for method call"
argument_list|)
throw|;
block|}
if|if
condition|(
name|args
index|[
literal|0
index|]
operator|instanceof
name|Long
operator|==
literal|false
condition|)
block|{
throw|throw
operator|new
name|BootstrapMethodError
argument_list|(
literal|"Illegal parameter for method call: "
operator|+
name|args
index|[
literal|0
index|]
argument_list|)
throw|;
block|}
name|long
name|recipe
init|=
operator|(
name|Long
operator|)
name|args
index|[
literal|0
index|]
decl_stmt|;
if|if
condition|(
name|Long
operator|.
name|bitCount
argument_list|(
name|recipe
argument_list|)
operator|>
name|type
operator|.
name|parameterCount
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|BootstrapMethodError
argument_list|(
literal|"Illegal recipe for method call: too many bits"
argument_list|)
throw|;
block|}
break|break;
case|case
name|REFERENCE
case|:
if|if
condition|(
name|args
operator|.
name|length
operator|!=
literal|1
condition|)
block|{
throw|throw
operator|new
name|BootstrapMethodError
argument_list|(
literal|"Invalid number of parameters for reference call"
argument_list|)
throw|;
block|}
if|if
condition|(
name|args
index|[
literal|0
index|]
operator|instanceof
name|String
operator|==
literal|false
condition|)
block|{
throw|throw
operator|new
name|BootstrapMethodError
argument_list|(
literal|"Illegal parameter for reference call: "
operator|+
name|args
index|[
literal|0
index|]
argument_list|)
throw|;
block|}
break|break;
default|default:
if|if
condition|(
name|args
operator|.
name|length
operator|>
literal|0
condition|)
block|{
throw|throw
operator|new
name|BootstrapMethodError
argument_list|(
literal|"Illegal static bootstrap parameters for flavor: "
operator|+
name|flavor
argument_list|)
throw|;
block|}
break|break;
block|}
return|return
operator|new
name|PIC
argument_list|(
name|lookup
argument_list|,
name|name
argument_list|,
name|type
argument_list|,
name|flavor
argument_list|,
name|args
argument_list|)
return|;
block|}
block|}
end_class

end_unit

