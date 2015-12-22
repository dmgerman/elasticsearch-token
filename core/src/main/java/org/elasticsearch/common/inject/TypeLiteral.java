begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright (C) 2006 Google Inc.  *  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.inject
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|inject
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
name|inject
operator|.
name|internal
operator|.
name|MoreTypes
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
name|util
operator|.
name|Types
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
name|lang
operator|.
name|reflect
operator|.
name|Field
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
name|GenericArrayType
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
name|Member
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
name|Method
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
name|ParameterizedType
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
name|Type
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
name|TypeVariable
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
name|WildcardType
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
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
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|inject
operator|.
name|internal
operator|.
name|MoreTypes
operator|.
name|canonicalize
import|;
end_import

begin_comment
comment|/**  * Represents a generic type {@code T}. Java doesn't yet provide a way to  * represent generic types, so this class does. Forces clients to create a  * subclass of this class which enables retrieval the type information even at  * runtime.  *<p>  * For example, to create a type literal for {@code List<String>}, you can  * create an empty anonymous inner class:  *<p>  * {@code TypeLiteral<List<String>> list = new TypeLiteral<List<String>>() {};}  *<p>  * This syntax cannot be used to create type literals that have wildcard  * parameters, such as {@code Class<?>} or {@code List<? extends CharSequence>}.  * Such type literals must be constructed programatically, either by {@link  * Method#getGenericReturnType extracting types from members} or by using the  * {@link Types} factory class.  *<p>  * Along with modeling generic types, this class can resolve type parameters.  * For example, to figure out what type {@code keySet()} returns on a {@code  * Map<Integer, String>}, use this code:{@code  *<p>  *   TypeLiteral<Map<Integer, String>> mapType  *       = new TypeLiteral<Map<Integer, String>>() {};  *   TypeLiteral<?> keySetType  *       = mapType.getReturnType(Map.class.getMethod("keySet"));  *   System.out.println(keySetType); // prints "Set<Integer>"}  *  * @author crazybob@google.com (Bob Lee)  * @author jessewilson@google.com (Jesse Wilson)  */
end_comment

begin_class
DECL|class|TypeLiteral
specifier|public
class|class
name|TypeLiteral
parameter_list|<
name|T
parameter_list|>
block|{
DECL|field|rawType
specifier|final
name|Class
argument_list|<
name|?
super|super
name|T
argument_list|>
name|rawType
decl_stmt|;
DECL|field|type
specifier|final
name|Type
name|type
decl_stmt|;
DECL|field|hashCode
specifier|final
name|int
name|hashCode
decl_stmt|;
comment|/**      * Constructs a new type literal. Derives represented class from type      * parameter.      *<p>      * Clients create an empty anonymous subclass. Doing so embeds the type      * parameter in the anonymous class's type hierarchy so we can reconstitute it      * at runtime despite erasure.      */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|method|TypeLiteral
specifier|protected
name|TypeLiteral
parameter_list|()
block|{
name|this
operator|.
name|type
operator|=
name|getSuperclassTypeParameter
argument_list|(
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|rawType
operator|=
operator|(
name|Class
argument_list|<
name|?
super|super
name|T
argument_list|>
operator|)
name|MoreTypes
operator|.
name|getRawType
argument_list|(
name|type
argument_list|)
expr_stmt|;
name|this
operator|.
name|hashCode
operator|=
name|MoreTypes
operator|.
name|hashCode
argument_list|(
name|type
argument_list|)
expr_stmt|;
block|}
comment|/**      * Unsafe. Constructs a type literal manually.      */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|method|TypeLiteral
name|TypeLiteral
parameter_list|(
name|Type
name|type
parameter_list|)
block|{
name|this
operator|.
name|type
operator|=
name|canonicalize
argument_list|(
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|type
argument_list|,
literal|"type"
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|rawType
operator|=
operator|(
name|Class
argument_list|<
name|?
super|super
name|T
argument_list|>
operator|)
name|MoreTypes
operator|.
name|getRawType
argument_list|(
name|this
operator|.
name|type
argument_list|)
expr_stmt|;
name|this
operator|.
name|hashCode
operator|=
name|MoreTypes
operator|.
name|hashCode
argument_list|(
name|this
operator|.
name|type
argument_list|)
expr_stmt|;
block|}
comment|/**      * Returns the type from super class's type parameter in {@link MoreTypes#canonicalize(Type)      * canonical form}.      */
DECL|method|getSuperclassTypeParameter
specifier|static
name|Type
name|getSuperclassTypeParameter
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|subclass
parameter_list|)
block|{
name|Type
name|superclass
init|=
name|subclass
operator|.
name|getGenericSuperclass
argument_list|()
decl_stmt|;
if|if
condition|(
name|superclass
operator|instanceof
name|Class
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Missing type parameter."
argument_list|)
throw|;
block|}
name|ParameterizedType
name|parameterized
init|=
operator|(
name|ParameterizedType
operator|)
name|superclass
decl_stmt|;
return|return
name|canonicalize
argument_list|(
name|parameterized
operator|.
name|getActualTypeArguments
argument_list|()
index|[
literal|0
index|]
argument_list|)
return|;
block|}
comment|/**      * Gets type literal from super class's type parameter.      */
DECL|method|fromSuperclassTypeParameter
specifier|static
name|TypeLiteral
argument_list|<
name|?
argument_list|>
name|fromSuperclassTypeParameter
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|subclass
parameter_list|)
block|{
return|return
operator|new
name|TypeLiteral
argument_list|<
name|Object
argument_list|>
argument_list|(
name|getSuperclassTypeParameter
argument_list|(
name|subclass
argument_list|)
argument_list|)
return|;
block|}
comment|/**      * Returns the raw (non-generic) type for this type.      *      * @since 2.0      */
DECL|method|getRawType
specifier|public
specifier|final
name|Class
argument_list|<
name|?
super|super
name|T
argument_list|>
name|getRawType
parameter_list|()
block|{
return|return
name|rawType
return|;
block|}
comment|/**      * Gets underlying {@code Type} instance.      */
DECL|method|getType
specifier|public
specifier|final
name|Type
name|getType
parameter_list|()
block|{
return|return
name|type
return|;
block|}
comment|/**      * Gets the type of this type's provider.      */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|method|providerType
specifier|final
name|TypeLiteral
argument_list|<
name|Provider
argument_list|<
name|T
argument_list|>
argument_list|>
name|providerType
parameter_list|()
block|{
comment|// This cast is safe and wouldn't generate a warning if Type had a type
comment|// parameter.
return|return
operator|(
name|TypeLiteral
argument_list|<
name|Provider
argument_list|<
name|T
argument_list|>
argument_list|>
operator|)
name|get
argument_list|(
name|Types
operator|.
name|providerOf
argument_list|(
name|getType
argument_list|()
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|hashCode
specifier|public
specifier|final
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|this
operator|.
name|hashCode
return|;
block|}
annotation|@
name|Override
DECL|method|equals
specifier|public
specifier|final
name|boolean
name|equals
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
return|return
name|o
operator|instanceof
name|TypeLiteral
argument_list|<
name|?
argument_list|>
operator|&&
name|MoreTypes
operator|.
name|equals
argument_list|(
name|type
argument_list|,
operator|(
operator|(
name|TypeLiteral
operator|)
name|o
operator|)
operator|.
name|type
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|toString
specifier|public
specifier|final
name|String
name|toString
parameter_list|()
block|{
return|return
name|MoreTypes
operator|.
name|toString
argument_list|(
name|type
argument_list|)
return|;
block|}
comment|/**      * Gets type literal for the given {@code Type} instance.      */
DECL|method|get
specifier|public
specifier|static
name|TypeLiteral
argument_list|<
name|?
argument_list|>
name|get
parameter_list|(
name|Type
name|type
parameter_list|)
block|{
return|return
operator|new
name|TypeLiteral
argument_list|<
name|Object
argument_list|>
argument_list|(
name|type
argument_list|)
return|;
block|}
comment|/**      * Gets type literal for the given {@code Class} instance.      */
DECL|method|get
specifier|public
specifier|static
parameter_list|<
name|T
parameter_list|>
name|TypeLiteral
argument_list|<
name|T
argument_list|>
name|get
parameter_list|(
name|Class
argument_list|<
name|T
argument_list|>
name|type
parameter_list|)
block|{
return|return
operator|new
name|TypeLiteral
argument_list|<>
argument_list|(
name|type
argument_list|)
return|;
block|}
comment|/**      * Returns an immutable list of the resolved types.      */
DECL|method|resolveAll
specifier|private
name|List
argument_list|<
name|TypeLiteral
argument_list|<
name|?
argument_list|>
argument_list|>
name|resolveAll
parameter_list|(
name|Type
index|[]
name|types
parameter_list|)
block|{
name|TypeLiteral
argument_list|<
name|?
argument_list|>
index|[]
name|result
init|=
operator|new
name|TypeLiteral
argument_list|<
name|?
argument_list|>
index|[
name|types
operator|.
name|length
index|]
decl_stmt|;
for|for
control|(
name|int
name|t
init|=
literal|0
init|;
name|t
operator|<
name|types
operator|.
name|length
condition|;
name|t
operator|++
control|)
block|{
name|result
index|[
name|t
index|]
operator|=
name|resolve
argument_list|(
name|types
index|[
name|t
index|]
argument_list|)
expr_stmt|;
block|}
return|return
name|Arrays
operator|.
name|asList
argument_list|(
name|result
argument_list|)
return|;
block|}
comment|/**      * Resolves known type parameters in {@code toResolve} and returns the result.      */
DECL|method|resolve
name|TypeLiteral
argument_list|<
name|?
argument_list|>
name|resolve
parameter_list|(
name|Type
name|toResolve
parameter_list|)
block|{
return|return
name|TypeLiteral
operator|.
name|get
argument_list|(
name|resolveType
argument_list|(
name|toResolve
argument_list|)
argument_list|)
return|;
block|}
DECL|method|resolveType
name|Type
name|resolveType
parameter_list|(
name|Type
name|toResolve
parameter_list|)
block|{
comment|// this implementation is made a little more complicated in an attempt to avoid object-creation
while|while
condition|(
literal|true
condition|)
block|{
if|if
condition|(
name|toResolve
operator|instanceof
name|TypeVariable
condition|)
block|{
name|TypeVariable
name|original
init|=
operator|(
name|TypeVariable
operator|)
name|toResolve
decl_stmt|;
name|toResolve
operator|=
name|MoreTypes
operator|.
name|resolveTypeVariable
argument_list|(
name|type
argument_list|,
name|rawType
argument_list|,
name|original
argument_list|)
expr_stmt|;
if|if
condition|(
name|toResolve
operator|==
name|original
condition|)
block|{
return|return
name|toResolve
return|;
block|}
block|}
elseif|else
if|if
condition|(
name|toResolve
operator|instanceof
name|GenericArrayType
condition|)
block|{
name|GenericArrayType
name|original
init|=
operator|(
name|GenericArrayType
operator|)
name|toResolve
decl_stmt|;
name|Type
name|componentType
init|=
name|original
operator|.
name|getGenericComponentType
argument_list|()
decl_stmt|;
name|Type
name|newComponentType
init|=
name|resolveType
argument_list|(
name|componentType
argument_list|)
decl_stmt|;
return|return
name|componentType
operator|==
name|newComponentType
condition|?
name|original
else|:
name|Types
operator|.
name|arrayOf
argument_list|(
name|newComponentType
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|toResolve
operator|instanceof
name|ParameterizedType
condition|)
block|{
name|ParameterizedType
name|original
init|=
operator|(
name|ParameterizedType
operator|)
name|toResolve
decl_stmt|;
name|Type
name|ownerType
init|=
name|original
operator|.
name|getOwnerType
argument_list|()
decl_stmt|;
name|Type
name|newOwnerType
init|=
name|resolveType
argument_list|(
name|ownerType
argument_list|)
decl_stmt|;
name|boolean
name|changed
init|=
name|newOwnerType
operator|!=
name|ownerType
decl_stmt|;
name|Type
index|[]
name|args
init|=
name|original
operator|.
name|getActualTypeArguments
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|t
init|=
literal|0
init|,
name|length
init|=
name|args
operator|.
name|length
init|;
name|t
operator|<
name|length
condition|;
name|t
operator|++
control|)
block|{
name|Type
name|resolvedTypeArgument
init|=
name|resolveType
argument_list|(
name|args
index|[
name|t
index|]
argument_list|)
decl_stmt|;
if|if
condition|(
name|resolvedTypeArgument
operator|!=
name|args
index|[
name|t
index|]
condition|)
block|{
if|if
condition|(
operator|!
name|changed
condition|)
block|{
name|args
operator|=
name|args
operator|.
name|clone
argument_list|()
expr_stmt|;
name|changed
operator|=
literal|true
expr_stmt|;
block|}
name|args
index|[
name|t
index|]
operator|=
name|resolvedTypeArgument
expr_stmt|;
block|}
block|}
return|return
name|changed
condition|?
name|Types
operator|.
name|newParameterizedTypeWithOwner
argument_list|(
name|newOwnerType
argument_list|,
name|original
operator|.
name|getRawType
argument_list|()
argument_list|,
name|args
argument_list|)
else|:
name|original
return|;
block|}
elseif|else
if|if
condition|(
name|toResolve
operator|instanceof
name|WildcardType
condition|)
block|{
name|WildcardType
name|original
init|=
operator|(
name|WildcardType
operator|)
name|toResolve
decl_stmt|;
name|Type
index|[]
name|originalLowerBound
init|=
name|original
operator|.
name|getLowerBounds
argument_list|()
decl_stmt|;
name|Type
index|[]
name|originalUpperBound
init|=
name|original
operator|.
name|getUpperBounds
argument_list|()
decl_stmt|;
if|if
condition|(
name|originalLowerBound
operator|.
name|length
operator|==
literal|1
condition|)
block|{
name|Type
name|lowerBound
init|=
name|resolveType
argument_list|(
name|originalLowerBound
index|[
literal|0
index|]
argument_list|)
decl_stmt|;
if|if
condition|(
name|lowerBound
operator|!=
name|originalLowerBound
index|[
literal|0
index|]
condition|)
block|{
return|return
name|Types
operator|.
name|supertypeOf
argument_list|(
name|lowerBound
argument_list|)
return|;
block|}
block|}
elseif|else
if|if
condition|(
name|originalUpperBound
operator|.
name|length
operator|==
literal|1
condition|)
block|{
name|Type
name|upperBound
init|=
name|resolveType
argument_list|(
name|originalUpperBound
index|[
literal|0
index|]
argument_list|)
decl_stmt|;
if|if
condition|(
name|upperBound
operator|!=
name|originalUpperBound
index|[
literal|0
index|]
condition|)
block|{
return|return
name|Types
operator|.
name|subtypeOf
argument_list|(
name|upperBound
argument_list|)
return|;
block|}
block|}
return|return
name|original
return|;
block|}
else|else
block|{
return|return
name|toResolve
return|;
block|}
block|}
block|}
comment|/**      * Returns the generic form of {@code supertype}. For example, if this is {@code      * ArrayList<String>}, this returns {@code Iterable<String>} given the input {@code      * Iterable.class}.      *      * @param supertype a superclass of, or interface implemented by, this.      * @since 2.0      */
DECL|method|getSupertype
specifier|public
name|TypeLiteral
argument_list|<
name|?
argument_list|>
name|getSupertype
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|supertype
parameter_list|)
block|{
if|if
condition|(
operator|!
name|supertype
operator|.
name|isAssignableFrom
argument_list|(
name|rawType
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|supertype
operator|+
literal|" is not a supertype of "
operator|+
name|type
argument_list|)
throw|;
block|}
return|return
name|resolve
argument_list|(
name|MoreTypes
operator|.
name|getGenericSupertype
argument_list|(
name|type
argument_list|,
name|rawType
argument_list|,
name|supertype
argument_list|)
argument_list|)
return|;
block|}
comment|/**      * Returns the resolved generic type of {@code field}.      *      * @param field a field defined by this or any superclass.      * @since 2.0      */
DECL|method|getFieldType
specifier|public
name|TypeLiteral
argument_list|<
name|?
argument_list|>
name|getFieldType
parameter_list|(
name|Field
name|field
parameter_list|)
block|{
if|if
condition|(
operator|!
name|field
operator|.
name|getDeclaringClass
argument_list|()
operator|.
name|isAssignableFrom
argument_list|(
name|rawType
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|field
operator|+
literal|" is not defined by a supertype of "
operator|+
name|type
argument_list|)
throw|;
block|}
return|return
name|resolve
argument_list|(
name|field
operator|.
name|getGenericType
argument_list|()
argument_list|)
return|;
block|}
comment|/**      * Returns the resolved generic parameter types of {@code methodOrConstructor}.      *      * @param methodOrConstructor a method or constructor defined by this or any supertype.      * @since 2.0      */
DECL|method|getParameterTypes
specifier|public
name|List
argument_list|<
name|TypeLiteral
argument_list|<
name|?
argument_list|>
argument_list|>
name|getParameterTypes
parameter_list|(
name|Member
name|methodOrConstructor
parameter_list|)
block|{
name|Type
index|[]
name|genericParameterTypes
decl_stmt|;
if|if
condition|(
name|methodOrConstructor
operator|instanceof
name|Method
condition|)
block|{
name|Method
name|method
init|=
operator|(
name|Method
operator|)
name|methodOrConstructor
decl_stmt|;
if|if
condition|(
operator|!
name|method
operator|.
name|getDeclaringClass
argument_list|()
operator|.
name|isAssignableFrom
argument_list|(
name|rawType
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|method
operator|+
literal|" is not defined by a supertype of "
operator|+
name|type
argument_list|)
throw|;
block|}
name|genericParameterTypes
operator|=
name|method
operator|.
name|getGenericParameterTypes
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|methodOrConstructor
operator|instanceof
name|Constructor
condition|)
block|{
name|Constructor
name|constructor
init|=
operator|(
name|Constructor
operator|)
name|methodOrConstructor
decl_stmt|;
if|if
condition|(
operator|!
name|constructor
operator|.
name|getDeclaringClass
argument_list|()
operator|.
name|isAssignableFrom
argument_list|(
name|rawType
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|constructor
operator|+
literal|" does not construct a supertype of "
operator|+
name|type
argument_list|)
throw|;
block|}
name|genericParameterTypes
operator|=
name|constructor
operator|.
name|getGenericParameterTypes
argument_list|()
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Not a method or a constructor: "
operator|+
name|methodOrConstructor
argument_list|)
throw|;
block|}
return|return
name|resolveAll
argument_list|(
name|genericParameterTypes
argument_list|)
return|;
block|}
comment|/**      * Returns the resolved generic exception types thrown by {@code constructor}.      *      * @param methodOrConstructor a method or constructor defined by this or any supertype.      * @since 2.0      */
DECL|method|getExceptionTypes
specifier|public
name|List
argument_list|<
name|TypeLiteral
argument_list|<
name|?
argument_list|>
argument_list|>
name|getExceptionTypes
parameter_list|(
name|Member
name|methodOrConstructor
parameter_list|)
block|{
name|Type
index|[]
name|genericExceptionTypes
decl_stmt|;
if|if
condition|(
name|methodOrConstructor
operator|instanceof
name|Method
condition|)
block|{
name|Method
name|method
init|=
operator|(
name|Method
operator|)
name|methodOrConstructor
decl_stmt|;
if|if
condition|(
operator|!
name|method
operator|.
name|getDeclaringClass
argument_list|()
operator|.
name|isAssignableFrom
argument_list|(
name|rawType
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|method
operator|+
literal|" is not defined by a supertype of "
operator|+
name|type
argument_list|)
throw|;
block|}
name|genericExceptionTypes
operator|=
name|method
operator|.
name|getGenericExceptionTypes
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|methodOrConstructor
operator|instanceof
name|Constructor
condition|)
block|{
name|Constructor
argument_list|<
name|?
argument_list|>
name|constructor
init|=
operator|(
name|Constructor
argument_list|<
name|?
argument_list|>
operator|)
name|methodOrConstructor
decl_stmt|;
if|if
condition|(
operator|!
name|constructor
operator|.
name|getDeclaringClass
argument_list|()
operator|.
name|isAssignableFrom
argument_list|(
name|rawType
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|constructor
operator|+
literal|" does not construct a supertype of "
operator|+
name|type
argument_list|)
throw|;
block|}
name|genericExceptionTypes
operator|=
name|constructor
operator|.
name|getGenericExceptionTypes
argument_list|()
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Not a method or a constructor: "
operator|+
name|methodOrConstructor
argument_list|)
throw|;
block|}
return|return
name|resolveAll
argument_list|(
name|genericExceptionTypes
argument_list|)
return|;
block|}
comment|/**      * Returns the resolved generic return type of {@code method}.      *      * @param method a method defined by this or any supertype.      * @since 2.0      */
DECL|method|getReturnType
specifier|public
name|TypeLiteral
argument_list|<
name|?
argument_list|>
name|getReturnType
parameter_list|(
name|Method
name|method
parameter_list|)
block|{
if|if
condition|(
operator|!
name|method
operator|.
name|getDeclaringClass
argument_list|()
operator|.
name|isAssignableFrom
argument_list|(
name|rawType
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|method
operator|+
literal|" is not defined by a supertype of "
operator|+
name|type
argument_list|)
throw|;
block|}
return|return
name|resolve
argument_list|(
name|method
operator|.
name|getGenericReturnType
argument_list|()
argument_list|)
return|;
block|}
block|}
end_class

end_unit

