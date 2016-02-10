begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright (C) 2008 Google Inc.  *  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.inject.spi
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|inject
operator|.
name|spi
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
name|ConfigurationException
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
name|inject
operator|.
name|Key
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
name|TypeLiteral
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
name|internal
operator|.
name|Annotations
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
name|internal
operator|.
name|Errors
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
name|internal
operator|.
name|ErrorsException
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
name|internal
operator|.
name|Nullability
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|annotation
operator|.
name|Annotation
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
name|AnnotatedElement
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
name|Modifier
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
name|Arrays
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
name|HashSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
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
name|unmodifiableSet
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
name|getRawType
import|;
end_import

begin_comment
comment|/**  * A constructor, field or method that can receive injections. Typically this is a member with the  * {@literal @}{@link Inject} annotation. For non-private, no argument constructors, the member may  * omit the annotation.  *  * @author crazybob@google.com (Bob Lee)  * @since 2.0  */
end_comment

begin_class
DECL|class|InjectionPoint
specifier|public
specifier|final
class|class
name|InjectionPoint
block|{
DECL|field|optional
specifier|private
specifier|final
name|boolean
name|optional
decl_stmt|;
DECL|field|member
specifier|private
specifier|final
name|Member
name|member
decl_stmt|;
DECL|field|dependencies
specifier|private
specifier|final
name|List
argument_list|<
name|Dependency
argument_list|<
name|?
argument_list|>
argument_list|>
name|dependencies
decl_stmt|;
DECL|method|InjectionPoint
specifier|private
name|InjectionPoint
parameter_list|(
name|Member
name|member
parameter_list|,
name|List
argument_list|<
name|Dependency
argument_list|<
name|?
argument_list|>
argument_list|>
name|dependencies
parameter_list|,
name|boolean
name|optional
parameter_list|)
block|{
name|this
operator|.
name|member
operator|=
name|member
expr_stmt|;
name|this
operator|.
name|dependencies
operator|=
name|dependencies
expr_stmt|;
name|this
operator|.
name|optional
operator|=
name|optional
expr_stmt|;
block|}
DECL|method|InjectionPoint
name|InjectionPoint
parameter_list|(
name|TypeLiteral
argument_list|<
name|?
argument_list|>
name|type
parameter_list|,
name|Method
name|method
parameter_list|)
block|{
name|this
operator|.
name|member
operator|=
name|method
expr_stmt|;
name|Inject
name|inject
init|=
name|method
operator|.
name|getAnnotation
argument_list|(
name|Inject
operator|.
name|class
argument_list|)
decl_stmt|;
name|this
operator|.
name|optional
operator|=
name|inject
operator|.
name|optional
argument_list|()
expr_stmt|;
name|this
operator|.
name|dependencies
operator|=
name|forMember
argument_list|(
name|method
argument_list|,
name|type
argument_list|,
name|method
operator|.
name|getParameterAnnotations
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|InjectionPoint
name|InjectionPoint
parameter_list|(
name|TypeLiteral
argument_list|<
name|?
argument_list|>
name|type
parameter_list|,
name|Constructor
argument_list|<
name|?
argument_list|>
name|constructor
parameter_list|)
block|{
name|this
operator|.
name|member
operator|=
name|constructor
expr_stmt|;
name|this
operator|.
name|optional
operator|=
literal|false
expr_stmt|;
name|this
operator|.
name|dependencies
operator|=
name|forMember
argument_list|(
name|constructor
argument_list|,
name|type
argument_list|,
name|constructor
operator|.
name|getParameterAnnotations
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|InjectionPoint
name|InjectionPoint
parameter_list|(
name|TypeLiteral
argument_list|<
name|?
argument_list|>
name|type
parameter_list|,
name|Field
name|field
parameter_list|)
block|{
name|this
operator|.
name|member
operator|=
name|field
expr_stmt|;
name|Inject
name|inject
init|=
name|field
operator|.
name|getAnnotation
argument_list|(
name|Inject
operator|.
name|class
argument_list|)
decl_stmt|;
name|this
operator|.
name|optional
operator|=
name|inject
operator|.
name|optional
argument_list|()
expr_stmt|;
name|Annotation
index|[]
name|annotations
init|=
name|field
operator|.
name|getAnnotations
argument_list|()
decl_stmt|;
name|Errors
name|errors
init|=
operator|new
name|Errors
argument_list|(
name|field
argument_list|)
decl_stmt|;
name|Key
argument_list|<
name|?
argument_list|>
name|key
init|=
literal|null
decl_stmt|;
try|try
block|{
name|key
operator|=
name|Annotations
operator|.
name|getKey
argument_list|(
name|type
operator|.
name|getFieldType
argument_list|(
name|field
argument_list|)
argument_list|,
name|field
argument_list|,
name|annotations
argument_list|,
name|errors
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ErrorsException
name|e
parameter_list|)
block|{
name|errors
operator|.
name|merge
argument_list|(
name|e
operator|.
name|getErrors
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|errors
operator|.
name|throwConfigurationExceptionIfErrorsExist
argument_list|()
expr_stmt|;
name|this
operator|.
name|dependencies
operator|=
name|Collections
operator|.
expr|<
name|Dependency
argument_list|<
name|?
argument_list|>
operator|>
name|singletonList
argument_list|(
name|newDependency
argument_list|(
name|key
argument_list|,
name|Nullability
operator|.
name|allowsNull
argument_list|(
name|annotations
argument_list|)
argument_list|,
operator|-
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|forMember
specifier|private
name|List
argument_list|<
name|Dependency
argument_list|<
name|?
argument_list|>
argument_list|>
name|forMember
parameter_list|(
name|Member
name|member
parameter_list|,
name|TypeLiteral
argument_list|<
name|?
argument_list|>
name|type
parameter_list|,
name|Annotation
index|[]
index|[]
name|parameterAnnotations
parameter_list|)
block|{
name|Errors
name|errors
init|=
operator|new
name|Errors
argument_list|(
name|member
argument_list|)
decl_stmt|;
name|Iterator
argument_list|<
name|Annotation
index|[]
argument_list|>
name|annotationsIterator
init|=
name|Arrays
operator|.
name|asList
argument_list|(
name|parameterAnnotations
argument_list|)
operator|.
name|iterator
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Dependency
argument_list|<
name|?
argument_list|>
argument_list|>
name|dependencies
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|int
name|index
init|=
literal|0
decl_stmt|;
for|for
control|(
name|TypeLiteral
argument_list|<
name|?
argument_list|>
name|parameterType
range|:
name|type
operator|.
name|getParameterTypes
argument_list|(
name|member
argument_list|)
control|)
block|{
try|try
block|{
name|Annotation
index|[]
name|paramAnnotations
init|=
name|annotationsIterator
operator|.
name|next
argument_list|()
decl_stmt|;
name|Key
argument_list|<
name|?
argument_list|>
name|key
init|=
name|Annotations
operator|.
name|getKey
argument_list|(
name|parameterType
argument_list|,
name|member
argument_list|,
name|paramAnnotations
argument_list|,
name|errors
argument_list|)
decl_stmt|;
name|dependencies
operator|.
name|add
argument_list|(
name|newDependency
argument_list|(
name|key
argument_list|,
name|Nullability
operator|.
name|allowsNull
argument_list|(
name|paramAnnotations
argument_list|)
argument_list|,
name|index
argument_list|)
argument_list|)
expr_stmt|;
name|index
operator|++
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ErrorsException
name|e
parameter_list|)
block|{
name|errors
operator|.
name|merge
argument_list|(
name|e
operator|.
name|getErrors
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|errors
operator|.
name|throwConfigurationExceptionIfErrorsExist
argument_list|()
expr_stmt|;
return|return
name|Collections
operator|.
name|unmodifiableList
argument_list|(
name|dependencies
argument_list|)
return|;
block|}
comment|// This method is necessary to create a Dependency<T> with proper generic type information
DECL|method|newDependency
specifier|private
parameter_list|<
name|T
parameter_list|>
name|Dependency
argument_list|<
name|T
argument_list|>
name|newDependency
parameter_list|(
name|Key
argument_list|<
name|T
argument_list|>
name|key
parameter_list|,
name|boolean
name|allowsNull
parameter_list|,
name|int
name|parameterIndex
parameter_list|)
block|{
return|return
operator|new
name|Dependency
argument_list|<>
argument_list|(
name|this
argument_list|,
name|key
argument_list|,
name|allowsNull
argument_list|,
name|parameterIndex
argument_list|)
return|;
block|}
comment|/**      * Returns the injected constructor, field, or method.      */
DECL|method|getMember
specifier|public
name|Member
name|getMember
parameter_list|()
block|{
return|return
name|member
return|;
block|}
comment|/**      * Returns the dependencies for this injection point. If the injection point is for a method or      * constructor, the dependencies will correspond to that member's parameters. Field injection      * points always have a single dependency for the field itself.      *      * @return a possibly-empty list      */
DECL|method|getDependencies
specifier|public
name|List
argument_list|<
name|Dependency
argument_list|<
name|?
argument_list|>
argument_list|>
name|getDependencies
parameter_list|()
block|{
return|return
name|dependencies
return|;
block|}
comment|/**      * Returns true if this injection point shall be skipped if the injector cannot resolve bindings      * for all required dependencies. Both explicit bindings (as specified in a module), and implicit      * bindings ({@literal @}{@link org.elasticsearch.common.inject.ImplementedBy ImplementedBy}, default      * constructors etc.) may be used to satisfy optional injection points.      */
DECL|method|isOptional
specifier|public
name|boolean
name|isOptional
parameter_list|()
block|{
return|return
name|optional
return|;
block|}
annotation|@
name|Override
DECL|method|equals
specifier|public
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
name|InjectionPoint
operator|&&
name|member
operator|.
name|equals
argument_list|(
operator|(
operator|(
name|InjectionPoint
operator|)
name|o
operator|)
operator|.
name|member
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|hashCode
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|member
operator|.
name|hashCode
argument_list|()
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
return|return
name|MoreTypes
operator|.
name|toString
argument_list|(
name|member
argument_list|)
return|;
block|}
comment|/**      * Returns a new injection point for the injectable constructor of {@code type}.      *      * @param type a concrete type with exactly one constructor annotated {@literal @}{@link Inject},      *             or a no-arguments constructor that is not private.      * @throws ConfigurationException if there is no injectable constructor, more than one injectable      *                                constructor, or if parameters of the injectable constructor are malformed, such as a      *                                parameter with multiple binding annotations.      */
DECL|method|forConstructorOf
specifier|public
specifier|static
name|InjectionPoint
name|forConstructorOf
parameter_list|(
name|TypeLiteral
argument_list|<
name|?
argument_list|>
name|type
parameter_list|)
block|{
name|Class
argument_list|<
name|?
argument_list|>
name|rawType
init|=
name|getRawType
argument_list|(
name|type
operator|.
name|getType
argument_list|()
argument_list|)
decl_stmt|;
name|Errors
name|errors
init|=
operator|new
name|Errors
argument_list|(
name|rawType
argument_list|)
decl_stmt|;
name|Constructor
argument_list|<
name|?
argument_list|>
name|injectableConstructor
init|=
literal|null
decl_stmt|;
for|for
control|(
name|Constructor
argument_list|<
name|?
argument_list|>
name|constructor
range|:
name|rawType
operator|.
name|getConstructors
argument_list|()
control|)
block|{
name|Inject
name|inject
init|=
name|constructor
operator|.
name|getAnnotation
argument_list|(
name|Inject
operator|.
name|class
argument_list|)
decl_stmt|;
if|if
condition|(
name|inject
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|inject
operator|.
name|optional
argument_list|()
condition|)
block|{
name|errors
operator|.
name|optionalConstructor
argument_list|(
name|constructor
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|injectableConstructor
operator|!=
literal|null
condition|)
block|{
name|errors
operator|.
name|tooManyConstructors
argument_list|(
name|rawType
argument_list|)
expr_stmt|;
block|}
name|injectableConstructor
operator|=
name|constructor
expr_stmt|;
name|checkForMisplacedBindingAnnotations
argument_list|(
name|injectableConstructor
argument_list|,
name|errors
argument_list|)
expr_stmt|;
block|}
block|}
name|errors
operator|.
name|throwConfigurationExceptionIfErrorsExist
argument_list|()
expr_stmt|;
if|if
condition|(
name|injectableConstructor
operator|!=
literal|null
condition|)
block|{
return|return
operator|new
name|InjectionPoint
argument_list|(
name|type
argument_list|,
name|injectableConstructor
argument_list|)
return|;
block|}
comment|// If no annotated constructor is found, look for a no-arg constructor instead.
try|try
block|{
name|Constructor
argument_list|<
name|?
argument_list|>
name|noArgConstructor
init|=
name|rawType
operator|.
name|getConstructor
argument_list|()
decl_stmt|;
comment|// Disallow private constructors on non-private classes (unless they have @Inject)
if|if
condition|(
name|Modifier
operator|.
name|isPrivate
argument_list|(
name|noArgConstructor
operator|.
name|getModifiers
argument_list|()
argument_list|)
operator|&&
operator|!
name|Modifier
operator|.
name|isPrivate
argument_list|(
name|rawType
operator|.
name|getModifiers
argument_list|()
argument_list|)
condition|)
block|{
name|errors
operator|.
name|missingConstructor
argument_list|(
name|rawType
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|ConfigurationException
argument_list|(
name|errors
operator|.
name|getMessages
argument_list|()
argument_list|)
throw|;
block|}
name|checkForMisplacedBindingAnnotations
argument_list|(
name|noArgConstructor
argument_list|,
name|errors
argument_list|)
expr_stmt|;
return|return
operator|new
name|InjectionPoint
argument_list|(
name|type
argument_list|,
name|noArgConstructor
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|NoSuchMethodException
name|e
parameter_list|)
block|{
name|errors
operator|.
name|missingConstructor
argument_list|(
name|rawType
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|ConfigurationException
argument_list|(
name|errors
operator|.
name|getMessages
argument_list|()
argument_list|)
throw|;
block|}
block|}
comment|/**      * Returns a new injection point for the injectable constructor of {@code type}.      *      * @param type a concrete type with exactly one constructor annotated {@literal @}{@link Inject},      *             or a no-arguments constructor that is not private.      * @throws ConfigurationException if there is no injectable constructor, more than one injectable      *                                constructor, or if parameters of the injectable constructor are malformed, such as a      *                                parameter with multiple binding annotations.      */
DECL|method|forConstructorOf
specifier|public
specifier|static
name|InjectionPoint
name|forConstructorOf
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|type
parameter_list|)
block|{
return|return
name|forConstructorOf
argument_list|(
name|TypeLiteral
operator|.
name|get
argument_list|(
name|type
argument_list|)
argument_list|)
return|;
block|}
comment|/**      * Returns all static method and field injection points on {@code type}.      *      * @return a possibly empty set of injection points. The set has a specified iteration order. All      *         fields are returned and then all methods. Within the fields, supertype fields are returned      *         before subtype fields. Similarly, supertype methods are returned before subtype methods.      * @throws ConfigurationException if there is a malformed injection point on {@code type}, such as      *                                a field with multiple binding annotations. The exception's {@link      *                                ConfigurationException#getPartialValue() partial value} is a {@code Set<InjectionPoint>}      *                                of the valid injection points.      */
DECL|method|forStaticMethodsAndFields
specifier|public
specifier|static
name|Set
argument_list|<
name|InjectionPoint
argument_list|>
name|forStaticMethodsAndFields
parameter_list|(
name|TypeLiteral
name|type
parameter_list|)
block|{
name|Set
argument_list|<
name|InjectionPoint
argument_list|>
name|result
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
name|Errors
name|errors
init|=
operator|new
name|Errors
argument_list|()
decl_stmt|;
name|addInjectionPoints
argument_list|(
name|type
argument_list|,
name|Factory
operator|.
name|FIELDS
argument_list|,
literal|true
argument_list|,
name|result
argument_list|,
name|errors
argument_list|)
expr_stmt|;
name|addInjectionPoints
argument_list|(
name|type
argument_list|,
name|Factory
operator|.
name|METHODS
argument_list|,
literal|true
argument_list|,
name|result
argument_list|,
name|errors
argument_list|)
expr_stmt|;
name|result
operator|=
name|unmodifiableSet
argument_list|(
name|result
argument_list|)
expr_stmt|;
if|if
condition|(
name|errors
operator|.
name|hasErrors
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|ConfigurationException
argument_list|(
name|errors
operator|.
name|getMessages
argument_list|()
argument_list|)
operator|.
name|withPartialValue
argument_list|(
name|result
argument_list|)
throw|;
block|}
return|return
name|result
return|;
block|}
comment|/**      * Returns all static method and field injection points on {@code type}.      *      * @return a possibly empty set of injection points. The set has a specified iteration order. All      *         fields are returned and then all methods. Within the fields, supertype fields are returned      *         before subtype fields. Similarly, supertype methods are returned before subtype methods.      * @throws ConfigurationException if there is a malformed injection point on {@code type}, such as      *                                a field with multiple binding annotations. The exception's {@link      *                                ConfigurationException#getPartialValue() partial value} is a {@code Set<InjectionPoint>}      *                                of the valid injection points.      */
DECL|method|forStaticMethodsAndFields
specifier|public
specifier|static
name|Set
argument_list|<
name|InjectionPoint
argument_list|>
name|forStaticMethodsAndFields
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|type
parameter_list|)
block|{
return|return
name|forStaticMethodsAndFields
argument_list|(
name|TypeLiteral
operator|.
name|get
argument_list|(
name|type
argument_list|)
argument_list|)
return|;
block|}
comment|/**      * Returns all instance method and field injection points on {@code type}.      *      * @return a possibly empty set of injection points. The set has a specified iteration order. All      *         fields are returned and then all methods. Within the fields, supertype fields are returned      *         before subtype fields. Similarly, supertype methods are returned before subtype methods.      * @throws ConfigurationException if there is a malformed injection point on {@code type}, such as      *                                a field with multiple binding annotations. The exception's {@link      *                                ConfigurationException#getPartialValue() partial value} is a {@code Set<InjectionPoint>}      *                                of the valid injection points.      */
DECL|method|forInstanceMethodsAndFields
specifier|public
specifier|static
name|Set
argument_list|<
name|InjectionPoint
argument_list|>
name|forInstanceMethodsAndFields
parameter_list|(
name|TypeLiteral
argument_list|<
name|?
argument_list|>
name|type
parameter_list|)
block|{
name|Set
argument_list|<
name|InjectionPoint
argument_list|>
name|result
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
name|Errors
name|errors
init|=
operator|new
name|Errors
argument_list|()
decl_stmt|;
comment|// TODO (crazybob): Filter out overridden members.
name|addInjectionPoints
argument_list|(
name|type
argument_list|,
name|Factory
operator|.
name|FIELDS
argument_list|,
literal|false
argument_list|,
name|result
argument_list|,
name|errors
argument_list|)
expr_stmt|;
name|addInjectionPoints
argument_list|(
name|type
argument_list|,
name|Factory
operator|.
name|METHODS
argument_list|,
literal|false
argument_list|,
name|result
argument_list|,
name|errors
argument_list|)
expr_stmt|;
name|result
operator|=
name|unmodifiableSet
argument_list|(
name|result
argument_list|)
expr_stmt|;
if|if
condition|(
name|errors
operator|.
name|hasErrors
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|ConfigurationException
argument_list|(
name|errors
operator|.
name|getMessages
argument_list|()
argument_list|)
operator|.
name|withPartialValue
argument_list|(
name|result
argument_list|)
throw|;
block|}
return|return
name|result
return|;
block|}
comment|/**      * Returns all instance method and field injection points on {@code type}.      *      * @return a possibly empty set of injection points. The set has a specified iteration order. All      *         fields are returned and then all methods. Within the fields, supertype fields are returned      *         before subtype fields. Similarly, supertype methods are returned before subtype methods.      * @throws ConfigurationException if there is a malformed injection point on {@code type}, such as      *                                a field with multiple binding annotations. The exception's {@link      *                                ConfigurationException#getPartialValue() partial value} is a {@code Set<InjectionPoint>}      *                                of the valid injection points.      */
DECL|method|forInstanceMethodsAndFields
specifier|public
specifier|static
name|Set
argument_list|<
name|InjectionPoint
argument_list|>
name|forInstanceMethodsAndFields
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|type
parameter_list|)
block|{
return|return
name|forInstanceMethodsAndFields
argument_list|(
name|TypeLiteral
operator|.
name|get
argument_list|(
name|type
argument_list|)
argument_list|)
return|;
block|}
DECL|method|checkForMisplacedBindingAnnotations
specifier|private
specifier|static
name|void
name|checkForMisplacedBindingAnnotations
parameter_list|(
name|Member
name|member
parameter_list|,
name|Errors
name|errors
parameter_list|)
block|{
name|Annotation
name|misplacedBindingAnnotation
init|=
name|Annotations
operator|.
name|findBindingAnnotation
argument_list|(
name|errors
argument_list|,
name|member
argument_list|,
operator|(
operator|(
name|AnnotatedElement
operator|)
name|member
operator|)
operator|.
name|getAnnotations
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|misplacedBindingAnnotation
operator|==
literal|null
condition|)
block|{
return|return;
block|}
comment|// don't warn about misplaced binding annotations on methods when there's a field with the same
comment|// name. In Scala, fields always get accessor methods (that we need to ignore). See bug 242.
if|if
condition|(
name|member
operator|instanceof
name|Method
condition|)
block|{
try|try
block|{
if|if
condition|(
name|member
operator|.
name|getDeclaringClass
argument_list|()
operator|.
name|getField
argument_list|(
name|member
operator|.
name|getName
argument_list|()
argument_list|)
operator|!=
literal|null
condition|)
block|{
return|return;
block|}
block|}
catch|catch
parameter_list|(
name|NoSuchFieldException
name|ignore
parameter_list|)
block|{             }
block|}
name|errors
operator|.
name|misplacedBindingAnnotation
argument_list|(
name|member
argument_list|,
name|misplacedBindingAnnotation
argument_list|)
expr_stmt|;
block|}
DECL|method|addInjectionPoints
specifier|private
specifier|static
parameter_list|<
name|M
extends|extends
name|Member
operator|&
name|AnnotatedElement
parameter_list|>
name|void
name|addInjectionPoints
parameter_list|(
name|TypeLiteral
argument_list|<
name|?
argument_list|>
name|type
parameter_list|,
name|Factory
argument_list|<
name|M
argument_list|>
name|factory
parameter_list|,
name|boolean
name|statics
parameter_list|,
name|Collection
argument_list|<
name|InjectionPoint
argument_list|>
name|injectionPoints
parameter_list|,
name|Errors
name|errors
parameter_list|)
block|{
if|if
condition|(
name|type
operator|.
name|getType
argument_list|()
operator|==
name|Object
operator|.
name|class
condition|)
block|{
return|return;
block|}
comment|// Add injectors for superclass first.
name|TypeLiteral
argument_list|<
name|?
argument_list|>
name|superType
init|=
name|type
operator|.
name|getSupertype
argument_list|(
name|type
operator|.
name|getRawType
argument_list|()
operator|.
name|getSuperclass
argument_list|()
argument_list|)
decl_stmt|;
name|addInjectionPoints
argument_list|(
name|superType
argument_list|,
name|factory
argument_list|,
name|statics
argument_list|,
name|injectionPoints
argument_list|,
name|errors
argument_list|)
expr_stmt|;
comment|// Add injectors for all members next
name|addInjectorsForMembers
argument_list|(
name|type
argument_list|,
name|factory
argument_list|,
name|statics
argument_list|,
name|injectionPoints
argument_list|,
name|errors
argument_list|)
expr_stmt|;
block|}
DECL|method|addInjectorsForMembers
specifier|private
specifier|static
parameter_list|<
name|M
extends|extends
name|Member
operator|&
name|AnnotatedElement
parameter_list|>
name|void
name|addInjectorsForMembers
parameter_list|(
name|TypeLiteral
argument_list|<
name|?
argument_list|>
name|typeLiteral
parameter_list|,
name|Factory
argument_list|<
name|M
argument_list|>
name|factory
parameter_list|,
name|boolean
name|statics
parameter_list|,
name|Collection
argument_list|<
name|InjectionPoint
argument_list|>
name|injectionPoints
parameter_list|,
name|Errors
name|errors
parameter_list|)
block|{
for|for
control|(
name|M
name|member
range|:
name|factory
operator|.
name|getMembers
argument_list|(
name|getRawType
argument_list|(
name|typeLiteral
operator|.
name|getType
argument_list|()
argument_list|)
argument_list|)
control|)
block|{
if|if
condition|(
name|isStatic
argument_list|(
name|member
argument_list|)
operator|!=
name|statics
condition|)
block|{
continue|continue;
block|}
name|Inject
name|inject
init|=
name|member
operator|.
name|getAnnotation
argument_list|(
name|Inject
operator|.
name|class
argument_list|)
decl_stmt|;
if|if
condition|(
name|inject
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
try|try
block|{
name|injectionPoints
operator|.
name|add
argument_list|(
name|factory
operator|.
name|create
argument_list|(
name|typeLiteral
argument_list|,
name|member
argument_list|,
name|errors
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ConfigurationException
name|ignorable
parameter_list|)
block|{
if|if
condition|(
operator|!
name|inject
operator|.
name|optional
argument_list|()
condition|)
block|{
name|errors
operator|.
name|merge
argument_list|(
name|ignorable
operator|.
name|getErrorMessages
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
DECL|method|isStatic
specifier|private
specifier|static
name|boolean
name|isStatic
parameter_list|(
name|Member
name|member
parameter_list|)
block|{
return|return
name|Modifier
operator|.
name|isStatic
argument_list|(
name|member
operator|.
name|getModifiers
argument_list|()
argument_list|)
return|;
block|}
DECL|interface|Factory
specifier|private
interface|interface
name|Factory
parameter_list|<
name|M
extends|extends
name|Member
operator|&
name|AnnotatedElement
parameter_list|>
block|{
DECL|field|FIELDS
name|Factory
argument_list|<
name|Field
argument_list|>
name|FIELDS
init|=
operator|new
name|Factory
argument_list|<
name|Field
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Field
index|[]
name|getMembers
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|type
parameter_list|)
block|{
return|return
name|type
operator|.
name|getFields
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|InjectionPoint
name|create
parameter_list|(
name|TypeLiteral
argument_list|<
name|?
argument_list|>
name|typeLiteral
parameter_list|,
name|Field
name|member
parameter_list|,
name|Errors
name|errors
parameter_list|)
block|{
return|return
operator|new
name|InjectionPoint
argument_list|(
name|typeLiteral
argument_list|,
name|member
argument_list|)
return|;
block|}
block|}
decl_stmt|;
DECL|field|METHODS
name|Factory
argument_list|<
name|Method
argument_list|>
name|METHODS
init|=
operator|new
name|Factory
argument_list|<
name|Method
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Method
index|[]
name|getMembers
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|type
parameter_list|)
block|{
return|return
name|type
operator|.
name|getMethods
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|InjectionPoint
name|create
parameter_list|(
name|TypeLiteral
argument_list|<
name|?
argument_list|>
name|typeLiteral
parameter_list|,
name|Method
name|member
parameter_list|,
name|Errors
name|errors
parameter_list|)
block|{
name|checkForMisplacedBindingAnnotations
argument_list|(
name|member
argument_list|,
name|errors
argument_list|)
expr_stmt|;
return|return
operator|new
name|InjectionPoint
argument_list|(
name|typeLiteral
argument_list|,
name|member
argument_list|)
return|;
block|}
block|}
decl_stmt|;
DECL|method|getMembers
name|M
index|[]
name|getMembers
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|type
parameter_list|)
function_decl|;
DECL|method|create
name|InjectionPoint
name|create
parameter_list|(
name|TypeLiteral
argument_list|<
name|?
argument_list|>
name|typeLiteral
parameter_list|,
name|M
name|member
parameter_list|,
name|Errors
name|errors
parameter_list|)
function_decl|;
block|}
block|}
end_class

end_unit

