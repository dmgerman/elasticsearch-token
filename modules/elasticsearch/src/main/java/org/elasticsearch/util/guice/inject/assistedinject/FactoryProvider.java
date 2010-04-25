begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright (C) 2007 Google Inc.  *  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.util.guice.inject.assistedinject
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|guice
operator|.
name|inject
operator|.
name|assistedinject
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|guice
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
name|util
operator|.
name|guice
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
name|util
operator|.
name|guice
operator|.
name|inject
operator|.
name|Injector
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|guice
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
name|util
operator|.
name|guice
operator|.
name|inject
operator|.
name|Provider
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|guice
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
name|util
operator|.
name|guice
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
name|util
operator|.
name|guice
operator|.
name|inject
operator|.
name|spi
operator|.
name|Dependency
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|guice
operator|.
name|inject
operator|.
name|spi
operator|.
name|HasDependencies
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|guice
operator|.
name|inject
operator|.
name|spi
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
name|util
operator|.
name|gcommon
operator|.
name|collect
operator|.
name|ImmutableMap
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|gcommon
operator|.
name|collect
operator|.
name|ImmutableSet
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|gcommon
operator|.
name|collect
operator|.
name|Lists
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|gcommon
operator|.
name|collect
operator|.
name|Maps
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
name|InvocationHandler
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
name|Proxy
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
name|Set
import|;
end_import

begin_comment
comment|/**  * Provides a factory that combines the caller's arguments with injector-supplied values to  * construct objects.  *  *<h3>Defining a factory</h3>  * Create an interface whose methods return the constructed type, or any of its supertypes. The  * method's parameters are the arguments required to build the constructed type.  *<pre>public interface PaymentFactory {  *   Payment create(Date startDate, Money amount);  * }</pre>  * You can name your factory methods whatever you like, such as<i>create</i>,<i>createPayment</i>  * or<i>newPayment</i>.  *  *<h3>Creating a type that accepts factory parameters</h3>  * {@code constructedType} is a concrete class with an {@literal @}{@link Inject}-annotated  * constructor. In addition to injector-supplied parameters, the constructor should have  * parameters that match each of the factory method's parameters. Each factory-supplied parameter  * requires an {@literal @}{@link Assisted} annotation. This serves to document that the parameter  * is not bound by your application's modules.  *<pre>public class RealPayment implements Payment {  *   {@literal @}Inject  *   public RealPayment(  *      CreditService creditService,  *      AuthService authService,  *<strong>{@literal @}Assisted Date startDate</strong>,  *<strong>{@literal @}Assisted Money amount</strong>) {  *     ...  *   }  * }</pre>  * Any parameter that permits a null value should also be annotated {@code @Nullable}.  *  *<h3>Configuring factories</h3>  * In your {@link org.elasticsearch.util.guice.inject.Module module}, bind the factory interface to the returned  * factory:  *<pre>bind(PaymentFactory.class).toProvider(  *     FactoryProvider.newFactory(PaymentFactory.class, RealPayment.class));</pre>  * As a side-effect of this binding, Guice will inject the factory to initialize it for use. The  * factory cannot be used until the injector has been initialized.  *  *<h3>Using the factory</h3>  * Inject your factory into your application classes. When you use the factory, your arguments  * will be combined with values from the injector to construct an instance.  *<pre>public class PaymentAction {  *   {@literal @}Inject private PaymentFactory paymentFactory;  *  *   public void doPayment(Money amount) {  *     Payment payment = paymentFactory.create(new Date(), amount);  *     payment.apply();  *   }  * }</pre>  *  *<h3>Making parameter types distinct</h3>  * The types of the factory method's parameters must be distinct. To use multiple parameters of  * the same type, use a named {@literal @}{@link Assisted} annotation to disambiguate the  * parameters. The names must be applied to the factory method's parameters:  *  *<pre>public interface PaymentFactory {  *   Payment create(  *<strong>{@literal @}Assisted("startDate")</strong> Date startDate,  *<strong>{@literal @}Assisted("dueDate")</strong> Date dueDate,  *       Money amount);  * }</pre>  * ...and to the concrete type's constructor parameters:  *<pre>public class RealPayment implements Payment {  *   {@literal @}Inject  *   public RealPayment(  *      CreditService creditService,  *      AuthService authService,  *<strong>{@literal @}Assisted("startDate")</strong> Date startDate,  *<strong>{@literal @}Assisted("dueDate")</strong> Date dueDate,  *<strong>{@literal @}Assisted</strong> Money amount) {  *     ...  *   }  * }</pre>  *  *<h3>Values are created by Guice</h3>  * Returned factories use child injectors to create values. The values are eligible for method  * interception. In addition, {@literal @}{@literal Inject} members will be injected before they are  * returned.  *  *<h3>Backwards compatibility using {@literal @}AssistedInject</h3>  * Instead of the {@literal @}Inject annotation, you may annotate the constructed classes with  * {@literal @}{@link AssistedInject}. This triggers a limited backwards-compatability mode.  *  *<p>Instead of matching factory method arguments to constructor parameters using their names, the  *<strong>parameters are matched by their order</strong>. The first factory method argument is  * used for the first {@literal @}Assisted constructor parameter, etc.. Annotation names have no  * effect.  *  *<p>Returned values are<strong>not created by Guice</strong>. These types are not eligible for  * method interception. They do receive post-construction member injection.  *  * @param<F> The factory interface  *  * @author jmourits@google.com (Jerome Mourits)  * @author jessewilson@google.com (Jesse Wilson)  * @author dtm@google.com (Daniel Martin)  */
end_comment

begin_class
DECL|class|FactoryProvider
specifier|public
class|class
name|FactoryProvider
parameter_list|<
name|F
parameter_list|>
implements|implements
name|Provider
argument_list|<
name|F
argument_list|>
implements|,
name|HasDependencies
block|{
comment|/*    * This class implements the old @AssistedInject implementation that manually matches constructors    * to factory methods. The new child injector implementation lives in FactoryProvider2.    */
DECL|field|injector
specifier|private
name|Injector
name|injector
decl_stmt|;
DECL|field|factoryType
specifier|private
specifier|final
name|TypeLiteral
argument_list|<
name|F
argument_list|>
name|factoryType
decl_stmt|;
DECL|field|factoryMethodToConstructor
specifier|private
specifier|final
name|Map
argument_list|<
name|Method
argument_list|,
name|AssistedConstructor
argument_list|<
name|?
argument_list|>
argument_list|>
name|factoryMethodToConstructor
decl_stmt|;
DECL|method|newFactory
specifier|public
specifier|static
parameter_list|<
name|F
parameter_list|>
name|Provider
argument_list|<
name|F
argument_list|>
name|newFactory
parameter_list|(
name|Class
argument_list|<
name|F
argument_list|>
name|factoryType
parameter_list|,
name|Class
argument_list|<
name|?
argument_list|>
name|implementationType
parameter_list|)
block|{
return|return
name|newFactory
argument_list|(
name|TypeLiteral
operator|.
name|get
argument_list|(
name|factoryType
argument_list|)
argument_list|,
name|TypeLiteral
operator|.
name|get
argument_list|(
name|implementationType
argument_list|)
argument_list|)
return|;
block|}
DECL|method|newFactory
specifier|public
specifier|static
parameter_list|<
name|F
parameter_list|>
name|Provider
argument_list|<
name|F
argument_list|>
name|newFactory
parameter_list|(
name|TypeLiteral
argument_list|<
name|F
argument_list|>
name|factoryType
parameter_list|,
name|TypeLiteral
argument_list|<
name|?
argument_list|>
name|implementationType
parameter_list|)
block|{
name|Map
argument_list|<
name|Method
argument_list|,
name|AssistedConstructor
argument_list|<
name|?
argument_list|>
argument_list|>
name|factoryMethodToConstructor
init|=
name|createMethodMapping
argument_list|(
name|factoryType
argument_list|,
name|implementationType
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|factoryMethodToConstructor
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
operator|new
name|FactoryProvider
argument_list|<
name|F
argument_list|>
argument_list|(
name|factoryType
argument_list|,
name|factoryMethodToConstructor
argument_list|)
return|;
block|}
else|else
block|{
return|return
operator|new
name|FactoryProvider2
argument_list|<
name|F
argument_list|>
argument_list|(
name|factoryType
argument_list|,
name|Key
operator|.
name|get
argument_list|(
name|implementationType
argument_list|)
argument_list|)
return|;
block|}
block|}
DECL|method|FactoryProvider
specifier|private
name|FactoryProvider
parameter_list|(
name|TypeLiteral
argument_list|<
name|F
argument_list|>
name|factoryType
parameter_list|,
name|Map
argument_list|<
name|Method
argument_list|,
name|AssistedConstructor
argument_list|<
name|?
argument_list|>
argument_list|>
name|factoryMethodToConstructor
parameter_list|)
block|{
name|this
operator|.
name|factoryType
operator|=
name|factoryType
expr_stmt|;
name|this
operator|.
name|factoryMethodToConstructor
operator|=
name|factoryMethodToConstructor
expr_stmt|;
name|checkDeclaredExceptionsMatch
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Inject
DECL|method|setInjectorAndCheckUnboundParametersAreInjectable
name|void
name|setInjectorAndCheckUnboundParametersAreInjectable
parameter_list|(
name|Injector
name|injector
parameter_list|)
block|{
name|this
operator|.
name|injector
operator|=
name|injector
expr_stmt|;
for|for
control|(
name|AssistedConstructor
argument_list|<
name|?
argument_list|>
name|c
range|:
name|factoryMethodToConstructor
operator|.
name|values
argument_list|()
control|)
block|{
for|for
control|(
name|Parameter
name|p
range|:
name|c
operator|.
name|getAllParameters
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|p
operator|.
name|isProvidedByFactory
argument_list|()
operator|&&
operator|!
name|paramCanBeInjected
argument_list|(
name|p
argument_list|,
name|injector
argument_list|)
condition|)
block|{
comment|// this is lame - we're not using the proper mechanism to add an
comment|// error to the injector. Throughout this class we throw exceptions
comment|// to add errors, which isn't really the best way in Guice
throw|throw
name|newConfigurationException
argument_list|(
literal|"Parameter of type '%s' is not injectable or annotated "
operator|+
literal|"with @Assisted for Constructor '%s'"
argument_list|,
name|p
argument_list|,
name|c
argument_list|)
throw|;
block|}
block|}
block|}
block|}
DECL|method|checkDeclaredExceptionsMatch
specifier|private
name|void
name|checkDeclaredExceptionsMatch
parameter_list|()
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|Method
argument_list|,
name|AssistedConstructor
argument_list|<
name|?
argument_list|>
argument_list|>
name|entry
range|:
name|factoryMethodToConstructor
operator|.
name|entrySet
argument_list|()
control|)
block|{
for|for
control|(
name|Class
argument_list|<
name|?
argument_list|>
name|constructorException
range|:
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|getDeclaredExceptions
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|isConstructorExceptionCompatibleWithFactoryExeception
argument_list|(
name|constructorException
argument_list|,
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|getExceptionTypes
argument_list|()
argument_list|)
condition|)
block|{
throw|throw
name|newConfigurationException
argument_list|(
literal|"Constructor %s declares an exception, but no compatible "
operator|+
literal|"exception is thrown by the factory method %s"
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|,
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
throw|;
block|}
block|}
block|}
block|}
DECL|method|isConstructorExceptionCompatibleWithFactoryExeception
specifier|private
name|boolean
name|isConstructorExceptionCompatibleWithFactoryExeception
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|constructorException
parameter_list|,
name|Class
argument_list|<
name|?
argument_list|>
index|[]
name|factoryExceptions
parameter_list|)
block|{
for|for
control|(
name|Class
argument_list|<
name|?
argument_list|>
name|factoryException
range|:
name|factoryExceptions
control|)
block|{
if|if
condition|(
name|factoryException
operator|.
name|isAssignableFrom
argument_list|(
name|constructorException
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
DECL|method|paramCanBeInjected
specifier|private
name|boolean
name|paramCanBeInjected
parameter_list|(
name|Parameter
name|parameter
parameter_list|,
name|Injector
name|injector
parameter_list|)
block|{
return|return
name|parameter
operator|.
name|isBound
argument_list|(
name|injector
argument_list|)
return|;
block|}
DECL|method|createMethodMapping
specifier|private
specifier|static
name|Map
argument_list|<
name|Method
argument_list|,
name|AssistedConstructor
argument_list|<
name|?
argument_list|>
argument_list|>
name|createMethodMapping
parameter_list|(
name|TypeLiteral
argument_list|<
name|?
argument_list|>
name|factoryType
parameter_list|,
name|TypeLiteral
argument_list|<
name|?
argument_list|>
name|implementationType
parameter_list|)
block|{
name|List
argument_list|<
name|AssistedConstructor
argument_list|<
name|?
argument_list|>
argument_list|>
name|constructors
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
for|for
control|(
name|Constructor
argument_list|<
name|?
argument_list|>
name|constructor
range|:
name|implementationType
operator|.
name|getRawType
argument_list|()
operator|.
name|getDeclaredConstructors
argument_list|()
control|)
block|{
if|if
condition|(
name|constructor
operator|.
name|getAnnotation
argument_list|(
name|AssistedInject
operator|.
name|class
argument_list|)
operator|!=
literal|null
condition|)
block|{
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
comment|// the constructor type and implementation type agree
name|AssistedConstructor
name|assistedConstructor
init|=
operator|new
name|AssistedConstructor
argument_list|(
name|constructor
argument_list|,
name|implementationType
operator|.
name|getParameterTypes
argument_list|(
name|constructor
argument_list|)
argument_list|)
decl_stmt|;
name|constructors
operator|.
name|add
argument_list|(
name|assistedConstructor
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|constructors
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
name|ImmutableMap
operator|.
name|of
argument_list|()
return|;
block|}
name|Method
index|[]
name|factoryMethods
init|=
name|factoryType
operator|.
name|getRawType
argument_list|()
operator|.
name|getMethods
argument_list|()
decl_stmt|;
if|if
condition|(
name|constructors
operator|.
name|size
argument_list|()
operator|!=
name|factoryMethods
operator|.
name|length
condition|)
block|{
throw|throw
name|newConfigurationException
argument_list|(
literal|"Constructor mismatch: %s has %s @AssistedInject "
operator|+
literal|"constructors, factory %s has %s creation methods"
argument_list|,
name|implementationType
argument_list|,
name|constructors
operator|.
name|size
argument_list|()
argument_list|,
name|factoryType
argument_list|,
name|factoryMethods
operator|.
name|length
argument_list|)
throw|;
block|}
name|Map
argument_list|<
name|ParameterListKey
argument_list|,
name|AssistedConstructor
argument_list|>
name|paramsToConstructor
init|=
name|Maps
operator|.
name|newHashMap
argument_list|()
decl_stmt|;
for|for
control|(
name|AssistedConstructor
name|c
range|:
name|constructors
control|)
block|{
if|if
condition|(
name|paramsToConstructor
operator|.
name|containsKey
argument_list|(
name|c
operator|.
name|getAssistedParameters
argument_list|()
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Duplicate constructor, "
operator|+
name|c
argument_list|)
throw|;
block|}
name|paramsToConstructor
operator|.
name|put
argument_list|(
name|c
operator|.
name|getAssistedParameters
argument_list|()
argument_list|,
name|c
argument_list|)
expr_stmt|;
block|}
name|Map
argument_list|<
name|Method
argument_list|,
name|AssistedConstructor
argument_list|<
name|?
argument_list|>
argument_list|>
name|result
init|=
name|Maps
operator|.
name|newHashMap
argument_list|()
decl_stmt|;
for|for
control|(
name|Method
name|method
range|:
name|factoryMethods
control|)
block|{
if|if
condition|(
operator|!
name|method
operator|.
name|getReturnType
argument_list|()
operator|.
name|isAssignableFrom
argument_list|(
name|implementationType
operator|.
name|getRawType
argument_list|()
argument_list|)
condition|)
block|{
throw|throw
name|newConfigurationException
argument_list|(
literal|"Return type of method %s is not assignable from %s"
argument_list|,
name|method
argument_list|,
name|implementationType
argument_list|)
throw|;
block|}
name|List
argument_list|<
name|Type
argument_list|>
name|parameterTypes
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
for|for
control|(
name|TypeLiteral
argument_list|<
name|?
argument_list|>
name|parameterType
range|:
name|factoryType
operator|.
name|getParameterTypes
argument_list|(
name|method
argument_list|)
control|)
block|{
name|parameterTypes
operator|.
name|add
argument_list|(
name|parameterType
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|ParameterListKey
name|methodParams
init|=
operator|new
name|ParameterListKey
argument_list|(
name|parameterTypes
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|paramsToConstructor
operator|.
name|containsKey
argument_list|(
name|methodParams
argument_list|)
condition|)
block|{
throw|throw
name|newConfigurationException
argument_list|(
literal|"%s has no @AssistInject constructor that takes the "
operator|+
literal|"@Assisted parameters %s in that order. @AssistInject constructors are %s"
argument_list|,
name|implementationType
argument_list|,
name|methodParams
argument_list|,
name|paramsToConstructor
operator|.
name|values
argument_list|()
argument_list|)
throw|;
block|}
name|method
operator|.
name|getParameterAnnotations
argument_list|()
expr_stmt|;
for|for
control|(
name|Annotation
index|[]
name|parameterAnnotations
range|:
name|method
operator|.
name|getParameterAnnotations
argument_list|()
control|)
block|{
for|for
control|(
name|Annotation
name|parameterAnnotation
range|:
name|parameterAnnotations
control|)
block|{
if|if
condition|(
name|parameterAnnotation
operator|.
name|annotationType
argument_list|()
operator|==
name|Assisted
operator|.
name|class
condition|)
block|{
throw|throw
name|newConfigurationException
argument_list|(
literal|"Factory method %s has an @Assisted parameter, which "
operator|+
literal|"is incompatible with the deprecated @AssistedInject annotation. Please replace "
operator|+
literal|"@AssistedInject with @Inject on the %s constructor."
argument_list|,
name|method
argument_list|,
name|implementationType
argument_list|)
throw|;
block|}
block|}
block|}
name|AssistedConstructor
name|matchingConstructor
init|=
name|paramsToConstructor
operator|.
name|remove
argument_list|(
name|methodParams
argument_list|)
decl_stmt|;
name|result
operator|.
name|put
argument_list|(
name|method
argument_list|,
name|matchingConstructor
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
DECL|method|getDependencies
specifier|public
name|Set
argument_list|<
name|Dependency
argument_list|<
name|?
argument_list|>
argument_list|>
name|getDependencies
parameter_list|()
block|{
name|List
argument_list|<
name|Dependency
argument_list|<
name|?
argument_list|>
argument_list|>
name|dependencies
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
for|for
control|(
name|AssistedConstructor
argument_list|<
name|?
argument_list|>
name|constructor
range|:
name|factoryMethodToConstructor
operator|.
name|values
argument_list|()
control|)
block|{
for|for
control|(
name|Parameter
name|parameter
range|:
name|constructor
operator|.
name|getAllParameters
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|parameter
operator|.
name|isProvidedByFactory
argument_list|()
condition|)
block|{
name|dependencies
operator|.
name|add
argument_list|(
name|Dependency
operator|.
name|get
argument_list|(
name|parameter
operator|.
name|getPrimaryBindingKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|ImmutableSet
operator|.
name|copyOf
argument_list|(
name|dependencies
argument_list|)
return|;
block|}
DECL|method|get
specifier|public
name|F
name|get
parameter_list|()
block|{
name|InvocationHandler
name|invocationHandler
init|=
operator|new
name|InvocationHandler
argument_list|()
block|{
specifier|public
name|Object
name|invoke
parameter_list|(
name|Object
name|proxy
parameter_list|,
name|Method
name|method
parameter_list|,
name|Object
index|[]
name|creationArgs
parameter_list|)
throws|throws
name|Throwable
block|{
comment|// pass methods from Object.class to the proxy
if|if
condition|(
name|method
operator|.
name|getDeclaringClass
argument_list|()
operator|.
name|equals
argument_list|(
name|Object
operator|.
name|class
argument_list|)
condition|)
block|{
return|return
name|method
operator|.
name|invoke
argument_list|(
name|this
argument_list|,
name|creationArgs
argument_list|)
return|;
block|}
name|AssistedConstructor
argument_list|<
name|?
argument_list|>
name|constructor
init|=
name|factoryMethodToConstructor
operator|.
name|get
argument_list|(
name|method
argument_list|)
decl_stmt|;
name|Object
index|[]
name|constructorArgs
init|=
name|gatherArgsForConstructor
argument_list|(
name|constructor
argument_list|,
name|creationArgs
argument_list|)
decl_stmt|;
name|Object
name|objectToReturn
init|=
name|constructor
operator|.
name|newInstance
argument_list|(
name|constructorArgs
argument_list|)
decl_stmt|;
name|injector
operator|.
name|injectMembers
argument_list|(
name|objectToReturn
argument_list|)
expr_stmt|;
return|return
name|objectToReturn
return|;
block|}
specifier|public
name|Object
index|[]
name|gatherArgsForConstructor
parameter_list|(
name|AssistedConstructor
argument_list|<
name|?
argument_list|>
name|constructor
parameter_list|,
name|Object
index|[]
name|factoryArgs
parameter_list|)
block|{
name|int
name|numParams
init|=
name|constructor
operator|.
name|getAllParameters
argument_list|()
operator|.
name|size
argument_list|()
decl_stmt|;
name|int
name|argPosition
init|=
literal|0
decl_stmt|;
name|Object
index|[]
name|result
init|=
operator|new
name|Object
index|[
name|numParams
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
name|numParams
condition|;
name|i
operator|++
control|)
block|{
name|Parameter
name|parameter
init|=
name|constructor
operator|.
name|getAllParameters
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|parameter
operator|.
name|isProvidedByFactory
argument_list|()
condition|)
block|{
name|result
index|[
name|i
index|]
operator|=
name|factoryArgs
index|[
name|argPosition
index|]
expr_stmt|;
name|argPosition
operator|++
expr_stmt|;
block|}
else|else
block|{
name|result
index|[
name|i
index|]
operator|=
name|parameter
operator|.
name|getValue
argument_list|(
name|injector
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|result
return|;
block|}
block|}
decl_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
comment|// we imprecisely treat the class literal of T as a Class<T>
name|Class
argument_list|<
name|F
argument_list|>
name|factoryRawType
init|=
operator|(
name|Class
operator|)
name|factoryType
operator|.
name|getRawType
argument_list|()
decl_stmt|;
return|return
name|factoryRawType
operator|.
name|cast
argument_list|(
name|Proxy
operator|.
name|newProxyInstance
argument_list|(
name|factoryRawType
operator|.
name|getClassLoader
argument_list|()
argument_list|,
operator|new
name|Class
index|[]
block|{
name|factoryRawType
block|}
argument_list|,
name|invocationHandler
argument_list|)
argument_list|)
return|;
block|}
DECL|method|newConfigurationException
specifier|private
specifier|static
name|ConfigurationException
name|newConfigurationException
parameter_list|(
name|String
name|format
parameter_list|,
name|Object
modifier|...
name|args
parameter_list|)
block|{
return|return
operator|new
name|ConfigurationException
argument_list|(
name|ImmutableSet
operator|.
name|of
argument_list|(
operator|new
name|Message
argument_list|(
name|Errors
operator|.
name|format
argument_list|(
name|format
argument_list|,
name|args
argument_list|)
argument_list|)
argument_list|)
argument_list|)
return|;
block|}
block|}
end_class

end_unit

