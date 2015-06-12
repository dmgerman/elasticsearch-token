begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright (C) 2007 Google Inc.  *  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|binder
operator|.
name|AnnotatedBindingBuilder
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
name|binder
operator|.
name|AnnotatedConstantBindingBuilder
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
name|binder
operator|.
name|LinkedBindingBuilder
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
name|matcher
operator|.
name|Matcher
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
name|common
operator|.
name|inject
operator|.
name|spi
operator|.
name|TypeConverter
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
name|spi
operator|.
name|TypeListener
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

begin_comment
comment|/**  * Collects configuration information (primarily<i>bindings</i>) which will be  * used to create an {@link Injector}. Guice provides this object to your  * application's {@link Module} implementors so they may each contribute  * their own bindings and other registrations.  *<p/>  *<h3>The Guice Binding EDSL</h3>  *<p/>  * Guice uses an<i>embedded domain-specific language</i>, or EDSL, to help you  * create bindings simply and readably.  This approach is great for overall  * usability, but it does come with a small cost:<b>it is difficult to  * learn how to use the Binding EDSL by reading  * method-level javadocs</b>.  Instead, you should consult the series of  * examples below.  To save space, these examples omit the opening  * {@code binder}, just as you will if your module extends  * {@link AbstractModule}.  *<p/>  *<pre>  *     bind(ServiceImpl.class);</pre>  *  * This statement does essentially nothing; it "binds the {@code ServiceImpl}  * class to itself" and does not change Guice's default behavior.  You may still  * want to use this if you prefer your {@link Module} class to serve as an  * explicit<i>manifest</i> for the services it provides.  Also, in rare cases,  * Guice may be unable to validate a binding at injector creation time unless it  * is given explicitly.  *  *<pre>  *     bind(Service.class).to(ServiceImpl.class);</pre>  *  * Specifies that a request for a {@code Service} instance with no binding  * annotations should be treated as if it were a request for a  * {@code ServiceImpl} instance. This<i>overrides</i> the function of any  * {@link ImplementedBy @ImplementedBy} or {@link ProvidedBy @ProvidedBy}  * annotations found on {@code Service}, since Guice will have already  * "moved on" to {@code ServiceImpl} before it reaches the point when it starts  * looking for these annotations.  *  *<pre>  *     bind(Service.class).toProvider(ServiceProvider.class);</pre>  *  * In this example, {@code ServiceProvider} must extend or implement  * {@code Provider<Service>}. This binding specifies that Guice should resolve  * an unannotated injection request for {@code Service} by first resolving an  * instance of {@code ServiceProvider} in the regular way, then calling  * {@link Provider#get get()} on the resulting Provider instance to obtain the  * {@code Service} instance.  *  *<p>The {@link Provider} you use here does not have to be a "factory"; that  * is, a provider which always<i>creates</i> each instance it provides.  * However, this is generally a good practice to follow.  You can then use  * Guice's concept of {@link Scope scopes} to guide when creation should happen  * -- "letting Guice work for you".  *  *<pre>  *     bind(Service.class).annotatedWith(Red.class).to(ServiceImpl.class);</pre>  *  * Like the previous example, but only applies to injection requests that use  * the binding annotation {@code @Red}.  If your module also includes bindings  * for particular<i>values</i> of the {@code @Red} annotation (see below),  * then this binding will serve as a "catch-all" for any values of {@code @Red}  * that have no exact match in the bindings.  *  *<pre>  *     bind(ServiceImpl.class).in(Singleton.class);  *     // or, alternatively  *     bind(ServiceImpl.class).in(Scopes.SINGLETON);</pre>  *  * Either of these statements places the {@code ServiceImpl} class into  * singleton scope.  Guice will create only one instance of {@code ServiceImpl}  * and will reuse it for all injection requests of this type.  Note that it is  * still possible to bind another instance of {@code ServiceImpl} if the second  * binding is qualified by an annotation as in the previous example.  Guice is  * not overly concerned with<i>preventing</i> you from creating multiple  * instances of your "singletons", only with<i>enabling</i> your application to  * share only one instance if that's all you tell Guice you need.  *  *<p><b>Note:</b> a scope specified in this way<i>overrides</i> any scope that  * was specified with an annotation on the {@code ServiceImpl} class.  *  *<p>Besides {@link Singleton}/{@link Scopes#SINGLETON}, there are  * servlet-specific scopes available in  * {@code com.google.inject.servlet.ServletScopes}, and your Modules can  * contribute their own custom scopes for use here as well.  *  *<pre>  *     bind(new TypeLiteral&lt;PaymentService&lt;CreditCard>>() {})  *         .to(CreditCardPaymentService.class);</pre>  *  * This admittedly odd construct is the way to bind a parameterized type. It  * tells Guice how to honor an injection request for an element of type  * {@code PaymentService<CreditCard>}. The class  * {@code CreditCardPaymentService} must implement the  * {@code PaymentService<CreditCard>} interface.  Guice cannot currently bind or  * inject a generic type, such as {@code Set<E>}; all type parameters must be  * fully specified.  *  *<pre>  *     bind(Service.class).toInstance(new ServiceImpl());  *     // or, alternatively  *     bind(Service.class).toInstance(SomeLegacyRegistry.getService());</pre>  *  * In this example, your module itself,<i>not Guice</i>, takes responsibility  * for obtaining a {@code ServiceImpl} instance, then asks Guice to always use  * this single instance to fulfill all {@code Service} injection requests.  When  * the {@link Injector} is created, it will automatically perform field  * and method injection for this instance, but any injectable constructor on  * {@code ServiceImpl} is simply ignored.  Note that using this approach results  * in "eager loading" behavior that you can't control.  *  *<pre>  *     bindConstant().annotatedWith(ServerHost.class).to(args[0]);</pre>  *  * Sets up a constant binding. Constant injections must always be annotated.  * When a constant binding's value is a string, it is eligile for conversion to  * all primitive types, to {@link Enum#valueOf(Class, String) all enums}, and to  * {@link Class#forName class literals}. Conversions for other types can be  * configured using {@link #convertToTypes(Matcher, TypeConverter)  * convertToTypes()}.  *  *<pre>  *   {@literal @}Color("red") Color red; // A member variable (field)  *    . . .  *     red = MyModule.class.getDeclaredField("red").getAnnotation(Color.class);  *     bind(Service.class).annotatedWith(red).to(RedService.class);</pre>  *  * If your binding annotation has parameters you can apply different bindings to  * different specific values of your annotation.  Getting your hands on the  * right instance of the annotation is a bit of a pain -- one approach, shown  * above, is to apply a prototype annotation to a field in your module class, so  * that you can read this annotation instance and give it to Guice.  *  *<pre>  *     bind(Service.class)  *         .annotatedWith(Names.named("blue"))  *         .to(BlueService.class);</pre>  *  * Differentiating by names is a common enough use case that we provided a  * standard annotation, {@link org.elasticsearch.common.inject.name.Named @Named}.  Because of  * Guice's library support, binding by name is quite easier than in the  * arbitrary binding annotation case we just saw.  However, remember that these  * names will live in a single flat namespace with all the other names used in  * your application.  *  *<p>The above list of examples is far from exhaustive.  If you can think of  * how the concepts of one example might coexist with the concepts from another,  * you can most likely weave the two together.  If the two concepts make no  * sense with each other, you most likely won't be able to do it.  In a few  * cases Guice will let something bogus slip by, and will then inform you of  * the problems at runtime, as soon as you try to create your Injector.  *  *<p>The other methods of Binder such as {@link #bindScope},  * {@link #bindInterceptor}, {@link #install}, {@link #requestStaticInjection},  * {@link #addError} and {@link #currentStage} are not part of the Binding EDSL;  * you can learn how to use these in the usual way, from the method  * documentation.  *  * @author crazybob@google.com (Bob Lee)  * @author jessewilson@google.com (Jesse Wilson)  * @author kevinb@google.com (Kevin Bourrillion)  */
end_comment

begin_interface
DECL|interface|Binder
specifier|public
interface|interface
name|Binder
block|{
comment|/**      * Binds a scope to an annotation.      */
DECL|method|bindScope
name|void
name|bindScope
parameter_list|(
name|Class
argument_list|<
name|?
extends|extends
name|Annotation
argument_list|>
name|annotationType
parameter_list|,
name|Scope
name|scope
parameter_list|)
function_decl|;
comment|/**      * See the EDSL examples at {@link Binder}.      */
DECL|method|bind
parameter_list|<
name|T
parameter_list|>
name|LinkedBindingBuilder
argument_list|<
name|T
argument_list|>
name|bind
parameter_list|(
name|Key
argument_list|<
name|T
argument_list|>
name|key
parameter_list|)
function_decl|;
comment|/**      * See the EDSL examples at {@link Binder}.      */
DECL|method|bind
parameter_list|<
name|T
parameter_list|>
name|AnnotatedBindingBuilder
argument_list|<
name|T
argument_list|>
name|bind
parameter_list|(
name|TypeLiteral
argument_list|<
name|T
argument_list|>
name|typeLiteral
parameter_list|)
function_decl|;
comment|/**      * See the EDSL examples at {@link Binder}.      */
DECL|method|bind
parameter_list|<
name|T
parameter_list|>
name|AnnotatedBindingBuilder
argument_list|<
name|T
argument_list|>
name|bind
parameter_list|(
name|Class
argument_list|<
name|T
argument_list|>
name|type
parameter_list|)
function_decl|;
comment|/**      * See the EDSL examples at {@link Binder}.      */
DECL|method|bindConstant
name|AnnotatedConstantBindingBuilder
name|bindConstant
parameter_list|()
function_decl|;
comment|/**      * Upon successful creation, the {@link Injector} will inject instance fields      * and methods of the given object.      *      * @param type     of instance      * @param instance for which members will be injected      * @since 2.0      */
DECL|method|requestInjection
parameter_list|<
name|T
parameter_list|>
name|void
name|requestInjection
parameter_list|(
name|TypeLiteral
argument_list|<
name|T
argument_list|>
name|type
parameter_list|,
name|T
name|instance
parameter_list|)
function_decl|;
comment|/**      * Upon successful creation, the {@link Injector} will inject instance fields      * and methods of the given object.      *      * @param instance for which members will be injected      * @since 2.0      */
DECL|method|requestInjection
name|void
name|requestInjection
parameter_list|(
name|Object
name|instance
parameter_list|)
function_decl|;
comment|/**      * Upon successful creation, the {@link Injector} will inject static fields      * and methods in the given classes.      *      * @param types for which static members will be injected      */
DECL|method|requestStaticInjection
name|void
name|requestStaticInjection
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
modifier|...
name|types
parameter_list|)
function_decl|;
comment|/**      * Uses the given module to configure more bindings.      */
DECL|method|install
name|void
name|install
parameter_list|(
name|Module
name|module
parameter_list|)
function_decl|;
comment|/**      * Gets the current stage.      */
DECL|method|currentStage
name|Stage
name|currentStage
parameter_list|()
function_decl|;
comment|/**      * Records an error message which will be presented to the user at a later      * time. Unlike throwing an exception, this enable us to continue      * configuring the Injector and discover more errors. Uses {@link      * String#format(String, Object[])} to insert the arguments into the      * message.      */
DECL|method|addError
name|void
name|addError
parameter_list|(
name|String
name|message
parameter_list|,
name|Object
modifier|...
name|arguments
parameter_list|)
function_decl|;
comment|/**      * Records an exception, the full details of which will be logged, and the      * message of which will be presented to the user at a later      * time. If your Module calls something that you worry may fail, you should      * catch the exception and pass it into this.      */
DECL|method|addError
name|void
name|addError
parameter_list|(
name|Throwable
name|t
parameter_list|)
function_decl|;
comment|/**      * Records an error message to be presented to the user at a later time.      *      * @since 2.0      */
DECL|method|addError
name|void
name|addError
parameter_list|(
name|Message
name|message
parameter_list|)
function_decl|;
comment|/**      * Returns the provider used to obtain instances for the given injection key.      * The returned will not be valid until the {@link Injector} has been      * created. The provider will throw an {@code IllegalStateException} if you      * try to use it beforehand.      *      * @since 2.0      */
DECL|method|getProvider
parameter_list|<
name|T
parameter_list|>
name|Provider
argument_list|<
name|T
argument_list|>
name|getProvider
parameter_list|(
name|Key
argument_list|<
name|T
argument_list|>
name|key
parameter_list|)
function_decl|;
comment|/**      * Returns the provider used to obtain instances for the given injection type.      * The returned provider will not be valid until the {@link Injector} has been      * created. The provider will throw an {@code IllegalStateException} if you      * try to use it beforehand.      *      * @since 2.0      */
DECL|method|getProvider
parameter_list|<
name|T
parameter_list|>
name|Provider
argument_list|<
name|T
argument_list|>
name|getProvider
parameter_list|(
name|Class
argument_list|<
name|T
argument_list|>
name|type
parameter_list|)
function_decl|;
comment|/**      * Returns the members injector used to inject dependencies into methods and fields on instances      * of the given type {@code T}. The returned members injector will not be valid until the main      * {@link Injector} has been created. The members injector will throw an {@code      * IllegalStateException} if you try to use it beforehand.      *      * @param typeLiteral type to get members injector for      * @since 2.0      */
DECL|method|getMembersInjector
parameter_list|<
name|T
parameter_list|>
name|MembersInjector
argument_list|<
name|T
argument_list|>
name|getMembersInjector
parameter_list|(
name|TypeLiteral
argument_list|<
name|T
argument_list|>
name|typeLiteral
parameter_list|)
function_decl|;
comment|/**      * Returns the members injector used to inject dependencies into methods and fields on instances      * of the given type {@code T}. The returned members injector will not be valid until the main      * {@link Injector} has been created. The members injector will throw an {@code      * IllegalStateException} if you try to use it beforehand.      *      * @param type type to get members injector for      * @since 2.0      */
DECL|method|getMembersInjector
parameter_list|<
name|T
parameter_list|>
name|MembersInjector
argument_list|<
name|T
argument_list|>
name|getMembersInjector
parameter_list|(
name|Class
argument_list|<
name|T
argument_list|>
name|type
parameter_list|)
function_decl|;
comment|/**      * Binds a type converter. The injector will use the given converter to      * convert string constants to matching types as needed.      *      * @param typeMatcher matches types the converter can handle      * @param converter   converts values      * @since 2.0      */
DECL|method|convertToTypes
name|void
name|convertToTypes
parameter_list|(
name|Matcher
argument_list|<
name|?
super|super
name|TypeLiteral
argument_list|<
name|?
argument_list|>
argument_list|>
name|typeMatcher
parameter_list|,
name|TypeConverter
name|converter
parameter_list|)
function_decl|;
comment|/**      * Registers a listener for injectable types. Guice will notify the listener when it encounters      * injectable types matched by the given type matcher.      *      * @param typeMatcher that matches injectable types the listener should be notified of      * @param listener    for injectable types matched by typeMatcher      * @since 2.0      */
DECL|method|bindListener
name|void
name|bindListener
parameter_list|(
name|Matcher
argument_list|<
name|?
super|super
name|TypeLiteral
argument_list|<
name|?
argument_list|>
argument_list|>
name|typeMatcher
parameter_list|,
name|TypeListener
name|listener
parameter_list|)
function_decl|;
comment|/**      * Returns a binder that uses {@code source} as the reference location for      * configuration errors. This is typically a {@link StackTraceElement}      * for {@code .java} source but it could any binding source, such as the      * path to a {@code .properties} file.      *      * @param source any object representing the source location and has a      *               concise {@link Object#toString() toString()} value      * @return a binder that shares its configuration with this binder      * @since 2.0      */
DECL|method|withSource
name|Binder
name|withSource
parameter_list|(
name|Object
name|source
parameter_list|)
function_decl|;
comment|/**      * Returns a binder that skips {@code classesToSkip} when identify the      * calling code. The caller's {@link StackTraceElement} is used to locate      * the source of configuration errors.      *      * @param classesToSkip library classes that create bindings on behalf of      *                      their clients.      * @return a binder that shares its configuration with this binder.      * @since 2.0      */
DECL|method|skipSources
name|Binder
name|skipSources
parameter_list|(
name|Class
modifier|...
name|classesToSkip
parameter_list|)
function_decl|;
comment|/**      * Creates a new private child environment for bindings and other configuration. The returned      * binder can be used to add and configuration information in this environment. See {@link      * PrivateModule} for details.      *      * @return a binder that inherits configuration from this binder. Only exposed configuration on      *         the returned binder will be visible to this binder.      * @since 2.0      */
DECL|method|newPrivateBinder
name|PrivateBinder
name|newPrivateBinder
parameter_list|()
function_decl|;
block|}
end_interface

end_unit
