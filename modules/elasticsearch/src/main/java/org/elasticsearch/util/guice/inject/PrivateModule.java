begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright (C) 2008 Google Inc.  *  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.util.guice.inject
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
name|util
operator|.
name|guice
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
name|util
operator|.
name|guice
operator|.
name|inject
operator|.
name|binder
operator|.
name|AnnotatedElementBuilder
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
name|binder
operator|.
name|LinkedBindingBuilder
import|;
end_import

begin_import
import|import static
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
name|Preconditions
operator|.
name|checkState
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
name|guice
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
name|util
operator|.
name|guice
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
comment|/**  * A module whose configuration information is hidden from its environment by default. Only bindings  * that are explicitly exposed will be available to other modules and to the users of the injector.  * This module may expose the bindings it creates and the bindings of the modules it installs.  *  *<p>A private module can be nested within a regular module or within another private module using  * {@link Binder#install install()}.  Its bindings live in a new environment that inherits bindings,  * type converters, scopes, and interceptors from the surrounding ("parent") environment.  When you  * nest multiple private modules, the result is a tree of environments where the injector's  * environment is the root.  *  *<p>Guice EDSL bindings can be exposed with {@link #expose(Class) expose()}. {@literal @}{@link  * org.elasticsearch.util.guice.inject.Provides Provides} bindings can be exposed with the {@literal @}{@link  * Exposed} annotation:  *  *<pre>  * public class FooBarBazModule extends PrivateModule {  *   protected void configure() {  *     bind(Foo.class).to(RealFoo.class);  *     expose(Foo.class);  *  *     install(new TransactionalBarModule());  *     expose(Bar.class).annotatedWith(Transactional.class);  *  *     bind(SomeImplementationDetail.class);  *     install(new MoreImplementationDetailsModule());  *   }  *  *   {@literal @}Provides {@literal @}Exposed  *   public Baz provideBaz() {  *     return new SuperBaz();  *   }  * }  *</pre>  *  *<p>Private modules are implemented using {@link Injector#createChildInjector(Module[]) parent  * injectors}. When it can satisfy their dependencies, just-in-time bindings will be created in the  * root environment. Such bindings are shared among all environments in the tree.  *   *<p>The scope of a binding is constrained to its environment. A singleton bound in a private  * module will be unique to its environment. But a binding for the same type in a different private  * module will yield a different instance.  *  *<p>A shared binding that injects the {@code Injector} gets the root injector, which only has  * access to bindings in the root environment. An explicit binding that injects the {@code Injector}  * gets access to all bindings in the child environment.  *  *<p>To promote a just-in-time binding to an explicit binding, bind it:  *<pre>  *   bind(FooImpl.class);  *</pre>  *  * @author jessewilson@google.com (Jesse Wilson)  * @since 2.0  */
end_comment

begin_class
DECL|class|PrivateModule
specifier|public
specifier|abstract
class|class
name|PrivateModule
implements|implements
name|Module
block|{
comment|/** Like abstract module, the binder of the current private module */
DECL|field|binder
specifier|private
name|PrivateBinder
name|binder
decl_stmt|;
DECL|method|configure
specifier|public
specifier|final
specifier|synchronized
name|void
name|configure
parameter_list|(
name|Binder
name|binder
parameter_list|)
block|{
name|checkState
argument_list|(
name|this
operator|.
name|binder
operator|==
literal|null
argument_list|,
literal|"Re-entry is not allowed."
argument_list|)
expr_stmt|;
comment|// Guice treats PrivateModules specially and passes in a PrivateBinder automatically.
name|this
operator|.
name|binder
operator|=
operator|(
name|PrivateBinder
operator|)
name|binder
operator|.
name|skipSources
argument_list|(
name|PrivateModule
operator|.
name|class
argument_list|)
expr_stmt|;
try|try
block|{
name|configure
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
name|this
operator|.
name|binder
operator|=
literal|null
expr_stmt|;
block|}
block|}
comment|/**    * Creates bindings and other configurations private to this module. Use {@link #expose(Class)    * expose()} to make the bindings in this module available externally.    */
DECL|method|configure
specifier|protected
specifier|abstract
name|void
name|configure
parameter_list|()
function_decl|;
comment|/** Makes the binding for {@code key} available to other modules and the injector. */
DECL|method|expose
specifier|protected
specifier|final
parameter_list|<
name|T
parameter_list|>
name|void
name|expose
parameter_list|(
name|Key
argument_list|<
name|T
argument_list|>
name|key
parameter_list|)
block|{
name|binder
operator|.
name|expose
argument_list|(
name|key
argument_list|)
expr_stmt|;
block|}
comment|/**    * Makes a binding for {@code type} available to other modules and the injector. Use {@link    * AnnotatedElementBuilder#annotatedWith(Class) annotatedWith()} to expose {@code type} with a    * binding annotation.    */
DECL|method|expose
specifier|protected
specifier|final
name|AnnotatedElementBuilder
name|expose
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|type
parameter_list|)
block|{
return|return
name|binder
operator|.
name|expose
argument_list|(
name|type
argument_list|)
return|;
block|}
comment|/**    * Makes a binding for {@code type} available to other modules and the injector. Use {@link    * AnnotatedElementBuilder#annotatedWith(Class) annotatedWith()} to expose {@code type} with a    * binding annotation.    */
DECL|method|expose
specifier|protected
specifier|final
name|AnnotatedElementBuilder
name|expose
parameter_list|(
name|TypeLiteral
argument_list|<
name|?
argument_list|>
name|type
parameter_list|)
block|{
return|return
name|binder
operator|.
name|expose
argument_list|(
name|type
argument_list|)
return|;
block|}
comment|// everything below is copied from AbstractModule
comment|/**    * Returns the current binder.    */
DECL|method|binder
specifier|protected
specifier|final
name|PrivateBinder
name|binder
parameter_list|()
block|{
return|return
name|binder
return|;
block|}
comment|/**    * @see Binder#bindScope(Class, Scope)    */
DECL|method|bindScope
specifier|protected
specifier|final
name|void
name|bindScope
parameter_list|(
name|Class
argument_list|<
name|?
extends|extends
name|Annotation
argument_list|>
name|scopeAnnotation
parameter_list|,
name|Scope
name|scope
parameter_list|)
block|{
name|binder
operator|.
name|bindScope
argument_list|(
name|scopeAnnotation
argument_list|,
name|scope
argument_list|)
expr_stmt|;
block|}
comment|/**    * @see Binder#bind(Key)    */
DECL|method|bind
specifier|protected
specifier|final
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
block|{
return|return
name|binder
operator|.
name|bind
argument_list|(
name|key
argument_list|)
return|;
block|}
comment|/**    * @see Binder#bind(TypeLiteral)    */
DECL|method|bind
specifier|protected
specifier|final
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
block|{
return|return
name|binder
operator|.
name|bind
argument_list|(
name|typeLiteral
argument_list|)
return|;
block|}
comment|/**    * @see Binder#bind(Class)      */
DECL|method|bind
specifier|protected
specifier|final
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
name|clazz
parameter_list|)
block|{
return|return
name|binder
operator|.
name|bind
argument_list|(
name|clazz
argument_list|)
return|;
block|}
comment|/**    * @see Binder#bindConstant()    */
DECL|method|bindConstant
specifier|protected
specifier|final
name|AnnotatedConstantBindingBuilder
name|bindConstant
parameter_list|()
block|{
return|return
name|binder
operator|.
name|bindConstant
argument_list|()
return|;
block|}
comment|/**    * @see Binder#install(Module)    */
DECL|method|install
specifier|protected
specifier|final
name|void
name|install
parameter_list|(
name|Module
name|module
parameter_list|)
block|{
name|binder
operator|.
name|install
argument_list|(
name|module
argument_list|)
expr_stmt|;
block|}
comment|/**    * @see Binder#addError(String, Object[])    */
DECL|method|addError
specifier|protected
specifier|final
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
block|{
name|binder
operator|.
name|addError
argument_list|(
name|message
argument_list|,
name|arguments
argument_list|)
expr_stmt|;
block|}
comment|/**    * @see Binder#addError(Throwable)    */
DECL|method|addError
specifier|protected
specifier|final
name|void
name|addError
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|binder
operator|.
name|addError
argument_list|(
name|t
argument_list|)
expr_stmt|;
block|}
comment|/**    * @see Binder#addError(Message)    */
DECL|method|addError
specifier|protected
specifier|final
name|void
name|addError
parameter_list|(
name|Message
name|message
parameter_list|)
block|{
name|binder
operator|.
name|addError
argument_list|(
name|message
argument_list|)
expr_stmt|;
block|}
comment|/**    * @see Binder#requestInjection(Object)    */
DECL|method|requestInjection
specifier|protected
specifier|final
name|void
name|requestInjection
parameter_list|(
name|Object
name|instance
parameter_list|)
block|{
name|binder
operator|.
name|requestInjection
argument_list|(
name|instance
argument_list|)
expr_stmt|;
block|}
comment|/**    * @see Binder#requestStaticInjection(Class[])    */
DECL|method|requestStaticInjection
specifier|protected
specifier|final
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
block|{
name|binder
operator|.
name|requestStaticInjection
argument_list|(
name|types
argument_list|)
expr_stmt|;
block|}
comment|/**    * Instructs Guice to require a binding to the given key.    */
DECL|method|requireBinding
specifier|protected
specifier|final
name|void
name|requireBinding
parameter_list|(
name|Key
argument_list|<
name|?
argument_list|>
name|key
parameter_list|)
block|{
name|binder
operator|.
name|getProvider
argument_list|(
name|key
argument_list|)
expr_stmt|;
block|}
comment|/**    * Instructs Guice to require a binding to the given type.    */
DECL|method|requireBinding
specifier|protected
specifier|final
name|void
name|requireBinding
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|type
parameter_list|)
block|{
name|binder
operator|.
name|getProvider
argument_list|(
name|type
argument_list|)
expr_stmt|;
block|}
comment|/**    * @see Binder#getProvider(Key)    */
DECL|method|getProvider
specifier|protected
specifier|final
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
block|{
return|return
name|binder
operator|.
name|getProvider
argument_list|(
name|key
argument_list|)
return|;
block|}
comment|/**    * @see Binder#getProvider(Class)    */
DECL|method|getProvider
specifier|protected
specifier|final
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
block|{
return|return
name|binder
operator|.
name|getProvider
argument_list|(
name|type
argument_list|)
return|;
block|}
comment|/**    * @see Binder#convertToTypes(org.elasticsearch.util.guice.inject.matcher.Matcher, org.elasticsearch.util.guice.inject.spi.TypeConverter)    */
DECL|method|convertToTypes
specifier|protected
specifier|final
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
block|{
name|binder
operator|.
name|convertToTypes
argument_list|(
name|typeMatcher
argument_list|,
name|converter
argument_list|)
expr_stmt|;
block|}
comment|/**    * @see Binder#currentStage()    */
DECL|method|currentStage
specifier|protected
specifier|final
name|Stage
name|currentStage
parameter_list|()
block|{
return|return
name|binder
operator|.
name|currentStage
argument_list|()
return|;
block|}
comment|/**    * @see Binder#getMembersInjector(Class)    */
DECL|method|getMembersInjector
specifier|protected
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
block|{
return|return
name|binder
operator|.
name|getMembersInjector
argument_list|(
name|type
argument_list|)
return|;
block|}
comment|/**    * @see Binder#getMembersInjector(TypeLiteral)    */
DECL|method|getMembersInjector
specifier|protected
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
name|type
parameter_list|)
block|{
return|return
name|binder
operator|.
name|getMembersInjector
argument_list|(
name|type
argument_list|)
return|;
block|}
comment|/**    * @see Binder#bindListener(org.elasticsearch.util.guice.inject.matcher.Matcher, org.elasticsearch.util.guice.inject.spi.TypeListener)    */
DECL|method|bindListener
specifier|protected
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
block|{
name|binder
operator|.
name|bindListener
argument_list|(
name|typeMatcher
argument_list|,
name|listener
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

