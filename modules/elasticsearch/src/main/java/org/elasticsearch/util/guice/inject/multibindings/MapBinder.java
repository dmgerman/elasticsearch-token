begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright (C) 2008 Google Inc.  *  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.util.guice.inject.multibindings
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
name|multibindings
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
name|guice
operator|.
name|inject
operator|.
name|*
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
name|ProviderWithDependencies
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
name|annotation
operator|.
name|Annotation
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
name|LinkedHashMap
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
name|Map
operator|.
name|Entry
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
name|util
operator|.
name|guice
operator|.
name|inject
operator|.
name|multibindings
operator|.
name|Multibinder
operator|.
name|*
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
name|util
operator|.
name|Types
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * An API to bind multiple map entries separately, only to later inject them as  * a complete map. MapBinder is intended for use in your application's module:  *<pre><code>  * public class SnacksModule extends AbstractModule {  *   protected void configure() {  *     MapBinder&lt;String, Snack&gt; mapbinder  *         = MapBinder.newMapBinder(binder(), String.class, Snack.class);  *     mapbinder.addBinding("twix").toInstance(new Twix());  *     mapbinder.addBinding("snickers").toProvider(SnickersProvider.class);  *     mapbinder.addBinding("skittles").to(Skittles.class);  *   }  * }</code></pre>  *  *<p>With this binding, a {@link Map}{@code<String, Snack>} can now be   * injected:  *<pre><code>  * class SnackMachine {  *   {@literal @}Inject  *   public SnackMachine(Map&lt;String, Snack&gt; snacks) { ... }  * }</code></pre>  *   *<p>In addition to binding {@code Map<K, V>}, a mapbinder will also bind  * {@code Map<K, Provider<V>>} for lazy value provision:  *<pre><code>  * class SnackMachine {  *   {@literal @}Inject  *   public SnackMachine(Map&lt;String, Provider&lt;Snack&gt;&gt; snackProviders) { ... }  * }</code></pre>  *  *<p>Creating mapbindings from different modules is supported. For example, it  * is okay to have both {@code CandyModule} and {@code ChipsModule} both  * create their own {@code MapBinder<String, Snack>}, and to each contribute   * bindings to the snacks map. When that map is injected, it will contain   * entries from both modules.  *  *<p>Values are resolved at map injection time. If a value is bound to a  * provider, that provider's get method will be called each time the map is  * injected (unless the binding is also scoped, or a map of providers is injected).  *  *<p>Annotations are used to create different maps of the same key/value  * type. Each distinct annotation gets its own independent map.  *  *<p><strong>Keys must be distinct.</strong> If the same key is bound more than  * once, map injection will fail.  *  *<p><strong>Keys must be non-null.</strong> {@code addBinding(null)} will   * throw an unchecked exception.  *  *<p><strong>Values must be non-null to use map injection.</strong> If any  * value is null, map injection will fail (although injecting a map of providers  * will not).  *  * @author dpb@google.com (David P. Baker)  */
end_comment

begin_class
DECL|class|MapBinder
specifier|public
specifier|abstract
class|class
name|MapBinder
parameter_list|<
name|K
parameter_list|,
name|V
parameter_list|>
block|{
DECL|method|MapBinder
specifier|private
name|MapBinder
parameter_list|()
block|{}
comment|/**    * Returns a new mapbinder that collects entries of {@code keyType}/{@code valueType} in a    * {@link Map} that is itself bound with no binding annotation.    */
DECL|method|newMapBinder
specifier|public
specifier|static
parameter_list|<
name|K
parameter_list|,
name|V
parameter_list|>
name|MapBinder
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
name|newMapBinder
parameter_list|(
name|Binder
name|binder
parameter_list|,
name|TypeLiteral
argument_list|<
name|K
argument_list|>
name|keyType
parameter_list|,
name|TypeLiteral
argument_list|<
name|V
argument_list|>
name|valueType
parameter_list|)
block|{
name|binder
operator|=
name|binder
operator|.
name|skipSources
argument_list|(
name|MapBinder
operator|.
name|class
argument_list|,
name|RealMapBinder
operator|.
name|class
argument_list|)
expr_stmt|;
return|return
name|newMapBinder
argument_list|(
name|binder
argument_list|,
name|valueType
argument_list|,
name|Key
operator|.
name|get
argument_list|(
name|mapOf
argument_list|(
name|keyType
argument_list|,
name|valueType
argument_list|)
argument_list|)
argument_list|,
name|Key
operator|.
name|get
argument_list|(
name|mapOfProviderOf
argument_list|(
name|keyType
argument_list|,
name|valueType
argument_list|)
argument_list|)
argument_list|,
name|Multibinder
operator|.
name|newSetBinder
argument_list|(
name|binder
argument_list|,
name|entryOfProviderOf
argument_list|(
name|keyType
argument_list|,
name|valueType
argument_list|)
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * Returns a new mapbinder that collects entries of {@code keyType}/{@code valueType} in a    * {@link Map} that is itself bound with no binding annotation.    */
DECL|method|newMapBinder
specifier|public
specifier|static
parameter_list|<
name|K
parameter_list|,
name|V
parameter_list|>
name|MapBinder
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
name|newMapBinder
parameter_list|(
name|Binder
name|binder
parameter_list|,
name|Class
argument_list|<
name|K
argument_list|>
name|keyType
parameter_list|,
name|Class
argument_list|<
name|V
argument_list|>
name|valueType
parameter_list|)
block|{
return|return
name|newMapBinder
argument_list|(
name|binder
argument_list|,
name|TypeLiteral
operator|.
name|get
argument_list|(
name|keyType
argument_list|)
argument_list|,
name|TypeLiteral
operator|.
name|get
argument_list|(
name|valueType
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * Returns a new mapbinder that collects entries of {@code keyType}/{@code valueType} in a    * {@link Map} that is itself bound with {@code annotation}.    */
DECL|method|newMapBinder
specifier|public
specifier|static
parameter_list|<
name|K
parameter_list|,
name|V
parameter_list|>
name|MapBinder
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
name|newMapBinder
parameter_list|(
name|Binder
name|binder
parameter_list|,
name|TypeLiteral
argument_list|<
name|K
argument_list|>
name|keyType
parameter_list|,
name|TypeLiteral
argument_list|<
name|V
argument_list|>
name|valueType
parameter_list|,
name|Annotation
name|annotation
parameter_list|)
block|{
name|binder
operator|=
name|binder
operator|.
name|skipSources
argument_list|(
name|MapBinder
operator|.
name|class
argument_list|,
name|RealMapBinder
operator|.
name|class
argument_list|)
expr_stmt|;
return|return
name|newMapBinder
argument_list|(
name|binder
argument_list|,
name|valueType
argument_list|,
name|Key
operator|.
name|get
argument_list|(
name|mapOf
argument_list|(
name|keyType
argument_list|,
name|valueType
argument_list|)
argument_list|,
name|annotation
argument_list|)
argument_list|,
name|Key
operator|.
name|get
argument_list|(
name|mapOfProviderOf
argument_list|(
name|keyType
argument_list|,
name|valueType
argument_list|)
argument_list|,
name|annotation
argument_list|)
argument_list|,
name|Multibinder
operator|.
name|newSetBinder
argument_list|(
name|binder
argument_list|,
name|entryOfProviderOf
argument_list|(
name|keyType
argument_list|,
name|valueType
argument_list|)
argument_list|,
name|annotation
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * Returns a new mapbinder that collects entries of {@code keyType}/{@code valueType} in a    * {@link Map} that is itself bound with {@code annotation}.    */
DECL|method|newMapBinder
specifier|public
specifier|static
parameter_list|<
name|K
parameter_list|,
name|V
parameter_list|>
name|MapBinder
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
name|newMapBinder
parameter_list|(
name|Binder
name|binder
parameter_list|,
name|Class
argument_list|<
name|K
argument_list|>
name|keyType
parameter_list|,
name|Class
argument_list|<
name|V
argument_list|>
name|valueType
parameter_list|,
name|Annotation
name|annotation
parameter_list|)
block|{
return|return
name|newMapBinder
argument_list|(
name|binder
argument_list|,
name|TypeLiteral
operator|.
name|get
argument_list|(
name|keyType
argument_list|)
argument_list|,
name|TypeLiteral
operator|.
name|get
argument_list|(
name|valueType
argument_list|)
argument_list|,
name|annotation
argument_list|)
return|;
block|}
comment|/**    * Returns a new mapbinder that collects entries of {@code keyType}/{@code valueType} in a    * {@link Map} that is itself bound with {@code annotationType}.    */
DECL|method|newMapBinder
specifier|public
specifier|static
parameter_list|<
name|K
parameter_list|,
name|V
parameter_list|>
name|MapBinder
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
name|newMapBinder
parameter_list|(
name|Binder
name|binder
parameter_list|,
name|TypeLiteral
argument_list|<
name|K
argument_list|>
name|keyType
parameter_list|,
name|TypeLiteral
argument_list|<
name|V
argument_list|>
name|valueType
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|Annotation
argument_list|>
name|annotationType
parameter_list|)
block|{
name|binder
operator|=
name|binder
operator|.
name|skipSources
argument_list|(
name|MapBinder
operator|.
name|class
argument_list|,
name|RealMapBinder
operator|.
name|class
argument_list|)
expr_stmt|;
return|return
name|newMapBinder
argument_list|(
name|binder
argument_list|,
name|valueType
argument_list|,
name|Key
operator|.
name|get
argument_list|(
name|mapOf
argument_list|(
name|keyType
argument_list|,
name|valueType
argument_list|)
argument_list|,
name|annotationType
argument_list|)
argument_list|,
name|Key
operator|.
name|get
argument_list|(
name|mapOfProviderOf
argument_list|(
name|keyType
argument_list|,
name|valueType
argument_list|)
argument_list|,
name|annotationType
argument_list|)
argument_list|,
name|Multibinder
operator|.
name|newSetBinder
argument_list|(
name|binder
argument_list|,
name|entryOfProviderOf
argument_list|(
name|keyType
argument_list|,
name|valueType
argument_list|)
argument_list|,
name|annotationType
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * Returns a new mapbinder that collects entries of {@code keyType}/{@code valueType} in a    * {@link Map} that is itself bound with {@code annotationType}.    */
DECL|method|newMapBinder
specifier|public
specifier|static
parameter_list|<
name|K
parameter_list|,
name|V
parameter_list|>
name|MapBinder
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
name|newMapBinder
parameter_list|(
name|Binder
name|binder
parameter_list|,
name|Class
argument_list|<
name|K
argument_list|>
name|keyType
parameter_list|,
name|Class
argument_list|<
name|V
argument_list|>
name|valueType
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|Annotation
argument_list|>
name|annotationType
parameter_list|)
block|{
return|return
name|newMapBinder
argument_list|(
name|binder
argument_list|,
name|TypeLiteral
operator|.
name|get
argument_list|(
name|keyType
argument_list|)
argument_list|,
name|TypeLiteral
operator|.
name|get
argument_list|(
name|valueType
argument_list|)
argument_list|,
name|annotationType
argument_list|)
return|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
comment|// a map of<K, V> is safely a Map<K, V>
DECL|method|mapOf
specifier|private
specifier|static
parameter_list|<
name|K
parameter_list|,
name|V
parameter_list|>
name|TypeLiteral
argument_list|<
name|Map
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
argument_list|>
name|mapOf
parameter_list|(
name|TypeLiteral
argument_list|<
name|K
argument_list|>
name|keyType
parameter_list|,
name|TypeLiteral
argument_list|<
name|V
argument_list|>
name|valueType
parameter_list|)
block|{
return|return
operator|(
name|TypeLiteral
argument_list|<
name|Map
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
argument_list|>
operator|)
name|TypeLiteral
operator|.
name|get
argument_list|(
name|Types
operator|.
name|mapOf
argument_list|(
name|keyType
operator|.
name|getType
argument_list|()
argument_list|,
name|valueType
operator|.
name|getType
argument_list|()
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
comment|// a provider map<K, V> is safely a Map<K, Provider<V>>
DECL|method|mapOfProviderOf
specifier|private
specifier|static
parameter_list|<
name|K
parameter_list|,
name|V
parameter_list|>
name|TypeLiteral
argument_list|<
name|Map
argument_list|<
name|K
argument_list|,
name|Provider
argument_list|<
name|V
argument_list|>
argument_list|>
argument_list|>
name|mapOfProviderOf
parameter_list|(
name|TypeLiteral
argument_list|<
name|K
argument_list|>
name|keyType
parameter_list|,
name|TypeLiteral
argument_list|<
name|V
argument_list|>
name|valueType
parameter_list|)
block|{
return|return
operator|(
name|TypeLiteral
argument_list|<
name|Map
argument_list|<
name|K
argument_list|,
name|Provider
argument_list|<
name|V
argument_list|>
argument_list|>
argument_list|>
operator|)
name|TypeLiteral
operator|.
name|get
argument_list|(
name|Types
operator|.
name|mapOf
argument_list|(
name|keyType
operator|.
name|getType
argument_list|()
argument_list|,
name|newParameterizedType
argument_list|(
name|Provider
operator|.
name|class
argument_list|,
name|valueType
operator|.
name|getType
argument_list|()
argument_list|)
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
comment|// a provider entry<K, V> is safely a Map.Entry<K, Provider<V>>
DECL|method|entryOfProviderOf
specifier|private
specifier|static
parameter_list|<
name|K
parameter_list|,
name|V
parameter_list|>
name|TypeLiteral
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|K
argument_list|,
name|Provider
argument_list|<
name|V
argument_list|>
argument_list|>
argument_list|>
name|entryOfProviderOf
parameter_list|(
name|TypeLiteral
argument_list|<
name|K
argument_list|>
name|keyType
parameter_list|,
name|TypeLiteral
argument_list|<
name|V
argument_list|>
name|valueType
parameter_list|)
block|{
return|return
operator|(
name|TypeLiteral
argument_list|<
name|Entry
argument_list|<
name|K
argument_list|,
name|Provider
argument_list|<
name|V
argument_list|>
argument_list|>
argument_list|>
operator|)
name|TypeLiteral
operator|.
name|get
argument_list|(
name|newParameterizedTypeWithOwner
argument_list|(
name|Map
operator|.
name|class
argument_list|,
name|Entry
operator|.
name|class
argument_list|,
name|keyType
operator|.
name|getType
argument_list|()
argument_list|,
name|Types
operator|.
name|providerOf
argument_list|(
name|valueType
operator|.
name|getType
argument_list|()
argument_list|)
argument_list|)
argument_list|)
return|;
block|}
DECL|method|newMapBinder
specifier|private
specifier|static
parameter_list|<
name|K
parameter_list|,
name|V
parameter_list|>
name|MapBinder
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
name|newMapBinder
parameter_list|(
name|Binder
name|binder
parameter_list|,
name|TypeLiteral
argument_list|<
name|V
argument_list|>
name|valueType
parameter_list|,
name|Key
argument_list|<
name|Map
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
argument_list|>
name|mapKey
parameter_list|,
name|Key
argument_list|<
name|Map
argument_list|<
name|K
argument_list|,
name|Provider
argument_list|<
name|V
argument_list|>
argument_list|>
argument_list|>
name|providerMapKey
parameter_list|,
name|Multibinder
argument_list|<
name|Entry
argument_list|<
name|K
argument_list|,
name|Provider
argument_list|<
name|V
argument_list|>
argument_list|>
argument_list|>
name|entrySetBinder
parameter_list|)
block|{
name|RealMapBinder
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
name|mapBinder
init|=
operator|new
name|RealMapBinder
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
argument_list|(
name|binder
argument_list|,
name|valueType
argument_list|,
name|mapKey
argument_list|,
name|providerMapKey
argument_list|,
name|entrySetBinder
argument_list|)
decl_stmt|;
name|binder
operator|.
name|install
argument_list|(
name|mapBinder
argument_list|)
expr_stmt|;
return|return
name|mapBinder
return|;
block|}
comment|/**    * Returns a binding builder used to add a new entry in the map. Each    * key must be distinct (and non-null). Bound providers will be evaluated each    * time the map is injected.    *    *<p>It is an error to call this method without also calling one of the    * {@code to} methods on the returned binding builder.    *    *<p>Scoping elements independently is supported. Use the {@code in} method    * to specify a binding scope.    */
DECL|method|addBinding
specifier|public
specifier|abstract
name|LinkedBindingBuilder
argument_list|<
name|V
argument_list|>
name|addBinding
parameter_list|(
name|K
name|key
parameter_list|)
function_decl|;
comment|/**    * The actual mapbinder plays several roles:    *    *<p>As a MapBinder, it acts as a factory for LinkedBindingBuilders for    * each of the map's values. It delegates to a {@link Multibinder} of    * entries (keys to value providers).    *    *<p>As a Module, it installs the binding to the map itself, as well as to    * a corresponding map whose values are providers. It uses the entry set     * multibinder to construct the map and the provider map.    *     *<p>As a module, this implements equals() and hashcode() in order to trick     * Guice into executing its configure() method only once. That makes it so     * that multiple mapbinders can be created for the same target map, but    * only one is bound. Since the list of bindings is retrieved from the    * injector itself (and not the mapbinder), each mapbinder has access to    * all contributions from all equivalent mapbinders.    *    *<p>Rather than binding a single Map.Entry&lt;K, V&gt;, the map binder    * binds keys and values independently. This allows the values to be properly    * scoped.    *    *<p>We use a subclass to hide 'implements Module' from the public API.    */
DECL|class|RealMapBinder
specifier|private
specifier|static
specifier|final
class|class
name|RealMapBinder
parameter_list|<
name|K
parameter_list|,
name|V
parameter_list|>
extends|extends
name|MapBinder
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
implements|implements
name|Module
block|{
DECL|field|valueType
specifier|private
specifier|final
name|TypeLiteral
argument_list|<
name|V
argument_list|>
name|valueType
decl_stmt|;
DECL|field|mapKey
specifier|private
specifier|final
name|Key
argument_list|<
name|Map
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
argument_list|>
name|mapKey
decl_stmt|;
DECL|field|providerMapKey
specifier|private
specifier|final
name|Key
argument_list|<
name|Map
argument_list|<
name|K
argument_list|,
name|Provider
argument_list|<
name|V
argument_list|>
argument_list|>
argument_list|>
name|providerMapKey
decl_stmt|;
DECL|field|entrySetBinder
specifier|private
specifier|final
name|RealMultibinder
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|K
argument_list|,
name|Provider
argument_list|<
name|V
argument_list|>
argument_list|>
argument_list|>
name|entrySetBinder
decl_stmt|;
comment|/* the target injector's binder. non-null until initialization, null afterwards */
DECL|field|binder
specifier|private
name|Binder
name|binder
decl_stmt|;
DECL|method|RealMapBinder
specifier|private
name|RealMapBinder
parameter_list|(
name|Binder
name|binder
parameter_list|,
name|TypeLiteral
argument_list|<
name|V
argument_list|>
name|valueType
parameter_list|,
name|Key
argument_list|<
name|Map
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
argument_list|>
name|mapKey
parameter_list|,
name|Key
argument_list|<
name|Map
argument_list|<
name|K
argument_list|,
name|Provider
argument_list|<
name|V
argument_list|>
argument_list|>
argument_list|>
name|providerMapKey
parameter_list|,
name|Multibinder
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|K
argument_list|,
name|Provider
argument_list|<
name|V
argument_list|>
argument_list|>
argument_list|>
name|entrySetBinder
parameter_list|)
block|{
name|this
operator|.
name|valueType
operator|=
name|valueType
expr_stmt|;
name|this
operator|.
name|mapKey
operator|=
name|mapKey
expr_stmt|;
name|this
operator|.
name|providerMapKey
operator|=
name|providerMapKey
expr_stmt|;
name|this
operator|.
name|entrySetBinder
operator|=
operator|(
name|RealMultibinder
argument_list|<
name|Entry
argument_list|<
name|K
argument_list|,
name|Provider
argument_list|<
name|V
argument_list|>
argument_list|>
argument_list|>
operator|)
name|entrySetBinder
expr_stmt|;
name|this
operator|.
name|binder
operator|=
name|binder
expr_stmt|;
block|}
comment|/**      * This creates two bindings. One for the {@code Map.Entry<K, Provider<V>>}      * and another for {@code V}.      */
DECL|method|addBinding
annotation|@
name|Override
specifier|public
name|LinkedBindingBuilder
argument_list|<
name|V
argument_list|>
name|addBinding
parameter_list|(
name|K
name|key
parameter_list|)
block|{
name|checkNotNull
argument_list|(
name|key
argument_list|,
literal|"key"
argument_list|)
expr_stmt|;
name|checkConfiguration
argument_list|(
operator|!
name|isInitialized
argument_list|()
argument_list|,
literal|"MapBinder was already initialized"
argument_list|)
expr_stmt|;
name|Key
argument_list|<
name|V
argument_list|>
name|valueKey
init|=
name|Key
operator|.
name|get
argument_list|(
name|valueType
argument_list|,
operator|new
name|RealElement
argument_list|(
name|entrySetBinder
operator|.
name|getSetName
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|entrySetBinder
operator|.
name|addBinding
argument_list|()
operator|.
name|toInstance
argument_list|(
operator|new
name|MapEntry
argument_list|<
name|K
argument_list|,
name|Provider
argument_list|<
name|V
argument_list|>
argument_list|>
argument_list|(
name|key
argument_list|,
name|binder
operator|.
name|getProvider
argument_list|(
name|valueKey
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|binder
operator|.
name|bind
argument_list|(
name|valueKey
argument_list|)
return|;
block|}
DECL|method|configure
specifier|public
name|void
name|configure
parameter_list|(
name|Binder
name|binder
parameter_list|)
block|{
name|checkConfiguration
argument_list|(
operator|!
name|isInitialized
argument_list|()
argument_list|,
literal|"MapBinder was already initialized"
argument_list|)
expr_stmt|;
specifier|final
name|ImmutableSet
argument_list|<
name|Dependency
argument_list|<
name|?
argument_list|>
argument_list|>
name|dependencies
init|=
name|ImmutableSet
operator|.
expr|<
name|Dependency
argument_list|<
name|?
argument_list|>
operator|>
name|of
argument_list|(
name|Dependency
operator|.
name|get
argument_list|(
name|entrySetBinder
operator|.
name|getSetKey
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
comment|// binds a Map<K, Provider<V>> from a collection of Map<Entry<K, Provider<V>>
specifier|final
name|Provider
argument_list|<
name|Set
argument_list|<
name|Entry
argument_list|<
name|K
argument_list|,
name|Provider
argument_list|<
name|V
argument_list|>
argument_list|>
argument_list|>
argument_list|>
name|entrySetProvider
init|=
name|binder
operator|.
name|getProvider
argument_list|(
name|entrySetBinder
operator|.
name|getSetKey
argument_list|()
argument_list|)
decl_stmt|;
name|binder
operator|.
name|bind
argument_list|(
name|providerMapKey
argument_list|)
operator|.
name|toProvider
argument_list|(
operator|new
name|ProviderWithDependencies
argument_list|<
name|Map
argument_list|<
name|K
argument_list|,
name|Provider
argument_list|<
name|V
argument_list|>
argument_list|>
argument_list|>
argument_list|()
block|{
specifier|private
name|Map
argument_list|<
name|K
argument_list|,
name|Provider
argument_list|<
name|V
argument_list|>
argument_list|>
name|providerMap
decl_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
annotation|@
name|Inject
name|void
name|initialize
parameter_list|()
block|{
name|RealMapBinder
operator|.
name|this
operator|.
name|binder
operator|=
literal|null
expr_stmt|;
name|Map
argument_list|<
name|K
argument_list|,
name|Provider
argument_list|<
name|V
argument_list|>
argument_list|>
name|providerMapMutable
init|=
operator|new
name|LinkedHashMap
argument_list|<
name|K
argument_list|,
name|Provider
argument_list|<
name|V
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Entry
argument_list|<
name|K
argument_list|,
name|Provider
argument_list|<
name|V
argument_list|>
argument_list|>
name|entry
range|:
name|entrySetProvider
operator|.
name|get
argument_list|()
control|)
block|{
name|checkConfiguration
argument_list|(
name|providerMapMutable
operator|.
name|put
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
operator|==
literal|null
argument_list|,
literal|"Map injection failed due to duplicated key \"%s\""
argument_list|,
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|providerMap
operator|=
name|Collections
operator|.
name|unmodifiableMap
argument_list|(
name|providerMapMutable
argument_list|)
expr_stmt|;
block|}
specifier|public
name|Map
argument_list|<
name|K
argument_list|,
name|Provider
argument_list|<
name|V
argument_list|>
argument_list|>
name|get
parameter_list|()
block|{
return|return
name|providerMap
return|;
block|}
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
return|return
name|dependencies
return|;
block|}
block|}
argument_list|)
expr_stmt|;
specifier|final
name|Provider
argument_list|<
name|Map
argument_list|<
name|K
argument_list|,
name|Provider
argument_list|<
name|V
argument_list|>
argument_list|>
argument_list|>
name|mapProvider
init|=
name|binder
operator|.
name|getProvider
argument_list|(
name|providerMapKey
argument_list|)
decl_stmt|;
name|binder
operator|.
name|bind
argument_list|(
name|mapKey
argument_list|)
operator|.
name|toProvider
argument_list|(
operator|new
name|ProviderWithDependencies
argument_list|<
name|Map
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
argument_list|>
argument_list|()
block|{
specifier|public
name|Map
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
name|get
parameter_list|()
block|{
name|Map
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
name|map
init|=
operator|new
name|LinkedHashMap
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Entry
argument_list|<
name|K
argument_list|,
name|Provider
argument_list|<
name|V
argument_list|>
argument_list|>
name|entry
range|:
name|mapProvider
operator|.
name|get
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|V
name|value
init|=
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|get
argument_list|()
decl_stmt|;
name|K
name|key
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|checkConfiguration
argument_list|(
name|value
operator|!=
literal|null
argument_list|,
literal|"Map injection failed due to null value for key \"%s\""
argument_list|,
name|key
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
return|return
name|Collections
operator|.
name|unmodifiableMap
argument_list|(
name|map
argument_list|)
return|;
block|}
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
return|return
name|dependencies
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
DECL|method|isInitialized
specifier|private
name|boolean
name|isInitialized
parameter_list|()
block|{
return|return
name|binder
operator|==
literal|null
return|;
block|}
DECL|method|equals
annotation|@
name|Override
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
name|RealMapBinder
operator|&&
operator|(
operator|(
name|RealMapBinder
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
operator|)
name|o
operator|)
operator|.
name|mapKey
operator|.
name|equals
argument_list|(
name|mapKey
argument_list|)
return|;
block|}
DECL|method|hashCode
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|mapKey
operator|.
name|hashCode
argument_list|()
return|;
block|}
DECL|class|MapEntry
specifier|private
specifier|static
specifier|final
class|class
name|MapEntry
parameter_list|<
name|K
parameter_list|,
name|V
parameter_list|>
implements|implements
name|Map
operator|.
name|Entry
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
block|{
DECL|field|key
specifier|private
specifier|final
name|K
name|key
decl_stmt|;
DECL|field|value
specifier|private
specifier|final
name|V
name|value
decl_stmt|;
DECL|method|MapEntry
specifier|private
name|MapEntry
parameter_list|(
name|K
name|key
parameter_list|,
name|V
name|value
parameter_list|)
block|{
name|this
operator|.
name|key
operator|=
name|key
expr_stmt|;
name|this
operator|.
name|value
operator|=
name|value
expr_stmt|;
block|}
DECL|method|getKey
specifier|public
name|K
name|getKey
parameter_list|()
block|{
return|return
name|key
return|;
block|}
DECL|method|getValue
specifier|public
name|V
name|getValue
parameter_list|()
block|{
return|return
name|value
return|;
block|}
DECL|method|setValue
specifier|public
name|V
name|setValue
parameter_list|(
name|V
name|value
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
DECL|method|equals
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
return|return
name|obj
operator|instanceof
name|Map
operator|.
name|Entry
operator|&&
name|key
operator|.
name|equals
argument_list|(
operator|(
operator|(
name|Map
operator|.
name|Entry
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
operator|)
name|obj
operator|)
operator|.
name|getKey
argument_list|()
argument_list|)
operator|&&
name|value
operator|.
name|equals
argument_list|(
operator|(
operator|(
name|Map
operator|.
name|Entry
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
operator|)
name|obj
operator|)
operator|.
name|getValue
argument_list|()
argument_list|)
return|;
block|}
DECL|method|hashCode
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
literal|127
operator|*
operator|(
literal|"key"
operator|.
name|hashCode
argument_list|()
operator|^
name|key
operator|.
name|hashCode
argument_list|()
operator|)
operator|+
literal|127
operator|*
operator|(
literal|"value"
operator|.
name|hashCode
argument_list|()
operator|^
name|value
operator|.
name|hashCode
argument_list|()
operator|)
return|;
block|}
DECL|method|toString
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"MapEntry("
operator|+
name|key
operator|+
literal|", "
operator|+
name|value
operator|+
literal|")"
return|;
block|}
block|}
block|}
block|}
end_class

end_unit

