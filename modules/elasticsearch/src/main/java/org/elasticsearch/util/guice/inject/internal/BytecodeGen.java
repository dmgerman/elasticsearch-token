begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright (C) 2008 Google Inc.  *  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.util.guice.inject.internal
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
name|internal
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
name|gcommon
operator|.
name|base
operator|.
name|Function
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
name|MapMaker
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
name|checkNotNull
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
name|security
operator|.
name|AccessController
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|PrivilegedAction
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
name|logging
operator|.
name|Logger
import|;
end_import

begin_comment
comment|/**  * Utility methods for runtime code generation and class loading. We use this stuff for {@link  * net.sf.cglib.reflect.FastClass faster reflection}, {@link net.sf.cglib.proxy.Enhancer method  * interceptors} and to proxy circular dependencies.  *  *<p>When loading classes, we need to be careful of:  *<ul>  *<li><strong>Memory leaks.</strong> Generated classes need to be garbage collected in long-lived  *       applications. Once an injector and any instances it created can be garbage collected, the  *       corresponding generated classes should be collectable.  *<li><strong>Visibility.</strong> Containers like<code>OSGi</code> use class loader boundaries  *       to enforce modularity at runtime.  *</ul>  *  *<p>For each generated class, there's multiple class loaders involved:  *<ul>  *<li><strong>The related class's class loader.</strong> Every generated class services exactly  *        one user-supplied class. This class loader must be used to access members with private and  *        package visibility.  *<li><strong>Guice's class loader.</strong>  *<li><strong>Our bridge class loader.</strong> This is a child of the user's class loader. It  *        selectively delegates to either the user's class loader (for user classes) or the Guice  *        class loader (for internal classes that are used by the generated classes). This class  *        loader that owns the classes generated by Guice.  *</ul>  *  * @author mcculls@gmail.com (Stuart McCulloch)  * @author jessewilson@google.com (Jesse Wilson)  */
end_comment

begin_class
DECL|class|BytecodeGen
specifier|public
specifier|final
class|class
name|BytecodeGen
block|{
DECL|field|logger
specifier|private
specifier|static
specifier|final
name|Logger
name|logger
init|=
name|Logger
operator|.
name|getLogger
argument_list|(
name|BytecodeGen
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
DECL|field|GUICE_CLASS_LOADER
specifier|static
specifier|final
name|ClassLoader
name|GUICE_CLASS_LOADER
init|=
name|BytecodeGen
operator|.
name|class
operator|.
name|getClassLoader
argument_list|()
decl_stmt|;
comment|/** ie. "com.google.inject.internal" */
DECL|field|GUICE_INTERNAL_PACKAGE
specifier|private
specifier|static
specifier|final
name|String
name|GUICE_INTERNAL_PACKAGE
init|=
name|BytecodeGen
operator|.
name|class
operator|.
name|getName
argument_list|()
operator|.
name|replaceFirst
argument_list|(
literal|"\\.internal\\..*$"
argument_list|,
literal|".internal"
argument_list|)
decl_stmt|;
DECL|field|CGLIB_PACKAGE
specifier|private
specifier|static
specifier|final
name|String
name|CGLIB_PACKAGE
init|=
literal|" "
decl_stmt|;
comment|// any string that's illegal in a package name
comment|/** Use "-Dguice.custom.loader=false" to disable custom classloading. */
DECL|field|HOOK_ENABLED
specifier|static
specifier|final
name|boolean
name|HOOK_ENABLED
init|=
literal|"true"
operator|.
name|equals
argument_list|(
name|System
operator|.
name|getProperty
argument_list|(
literal|"guice.custom.loader"
argument_list|,
literal|"true"
argument_list|)
argument_list|)
decl_stmt|;
comment|/**    * Weak cache of bridge class loaders that make the Guice implementation    * classes visible to various code-generated proxies of client classes.    */
DECL|field|CLASS_LOADER_CACHE
specifier|private
specifier|static
specifier|final
name|Map
argument_list|<
name|ClassLoader
argument_list|,
name|ClassLoader
argument_list|>
name|CLASS_LOADER_CACHE
init|=
operator|new
name|MapMaker
argument_list|()
operator|.
name|weakKeys
argument_list|()
operator|.
name|weakValues
argument_list|()
operator|.
name|makeComputingMap
argument_list|(
operator|new
name|Function
argument_list|<
name|ClassLoader
argument_list|,
name|ClassLoader
argument_list|>
argument_list|()
block|{
specifier|public
name|ClassLoader
name|apply
parameter_list|(
specifier|final
annotation|@
name|Nullable
name|ClassLoader
name|typeClassLoader
parameter_list|)
block|{
name|logger
operator|.
name|fine
argument_list|(
literal|"Creating a bridge ClassLoader for "
operator|+
name|typeClassLoader
argument_list|)
expr_stmt|;
return|return
name|AccessController
operator|.
name|doPrivileged
argument_list|(
operator|new
name|PrivilegedAction
argument_list|<
name|ClassLoader
argument_list|>
argument_list|()
block|{
specifier|public
name|ClassLoader
name|run
parameter_list|()
block|{
return|return
operator|new
name|BridgeClassLoader
argument_list|(
name|typeClassLoader
argument_list|)
return|;
block|}
block|}
argument_list|)
return|;
block|}
block|}
argument_list|)
decl_stmt|;
comment|/**    * For class loaders, {@code null}, is always an alias to the    * {@link ClassLoader#getSystemClassLoader() system class loader}. This method    * will not return null.    */
DECL|method|canonicalize
specifier|private
specifier|static
name|ClassLoader
name|canonicalize
parameter_list|(
name|ClassLoader
name|classLoader
parameter_list|)
block|{
return|return
name|classLoader
operator|!=
literal|null
condition|?
name|classLoader
else|:
name|checkNotNull
argument_list|(
name|getSystemClassLoaderOrNull
argument_list|()
argument_list|,
literal|"Couldn't get a ClassLoader"
argument_list|)
return|;
block|}
comment|/**    * Returns the system classloader, or {@code null} if we don't have    * permission.    */
DECL|method|getSystemClassLoaderOrNull
specifier|private
specifier|static
name|ClassLoader
name|getSystemClassLoaderOrNull
parameter_list|()
block|{
try|try
block|{
return|return
name|ClassLoader
operator|.
name|getSystemClassLoader
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|SecurityException
name|e
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
block|}
comment|/**    * Returns the class loader to host generated classes for {@code type}.    */
DECL|method|getClassLoader
specifier|public
specifier|static
name|ClassLoader
name|getClassLoader
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|type
parameter_list|)
block|{
return|return
name|getClassLoader
argument_list|(
name|type
argument_list|,
name|type
operator|.
name|getClassLoader
argument_list|()
argument_list|)
return|;
block|}
DECL|method|getClassLoader
specifier|private
specifier|static
name|ClassLoader
name|getClassLoader
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|type
parameter_list|,
name|ClassLoader
name|delegate
parameter_list|)
block|{
name|delegate
operator|=
name|canonicalize
argument_list|(
name|delegate
argument_list|)
expr_stmt|;
comment|// if the application is running in the System classloader, assume we can run there too
if|if
condition|(
name|delegate
operator|==
name|getSystemClassLoaderOrNull
argument_list|()
condition|)
block|{
return|return
name|delegate
return|;
block|}
comment|// Don't bother bridging existing bridge classloaders
if|if
condition|(
name|delegate
operator|instanceof
name|BridgeClassLoader
condition|)
block|{
return|return
name|delegate
return|;
block|}
if|if
condition|(
name|HOOK_ENABLED
operator|&&
name|Visibility
operator|.
name|forType
argument_list|(
name|type
argument_list|)
operator|==
name|Visibility
operator|.
name|PUBLIC
condition|)
block|{
return|return
name|CLASS_LOADER_CACHE
operator|.
name|get
argument_list|(
name|delegate
argument_list|)
return|;
block|}
return|return
name|delegate
return|;
block|}
comment|/**    * The required visibility of a user's class from a Guice-generated class. Visibility of    * package-private members depends on the loading classloader: only if two classes were loaded by    * the same classloader can they see each other's package-private members. We need to be careful    * when choosing which classloader to use for generated classes. We prefer our bridge classloader,    * since it's OSGi-safe and doesn't leak permgen space. But often we cannot due to visibility.    */
DECL|enum|Visibility
specifier|public
enum|enum
name|Visibility
block|{
comment|/**      * Indicates that Guice-generated classes only need to call and override public members of the      * target class. These generated classes may be loaded by our bridge classloader.      */
DECL|enum constant|PUBLIC
name|PUBLIC
block|{
specifier|public
name|Visibility
name|and
parameter_list|(
name|Visibility
name|that
parameter_list|)
block|{
return|return
name|that
return|;
block|}
block|}
block|,
comment|/**      * Indicates that Guice-generated classes need to call or override package-private members.      * These generated classes must be loaded in the same classloader as the target class. They      * won't work with OSGi, and won't get garbage collected until the target class' classloader is      * garbage collected.      */
DECL|enum constant|SAME_PACKAGE
name|SAME_PACKAGE
block|{
specifier|public
name|Visibility
name|and
parameter_list|(
name|Visibility
name|that
parameter_list|)
block|{
return|return
name|this
return|;
block|}
block|}
block|;
DECL|method|forMember
specifier|public
specifier|static
name|Visibility
name|forMember
parameter_list|(
name|Member
name|member
parameter_list|)
block|{
if|if
condition|(
operator|(
name|member
operator|.
name|getModifiers
argument_list|()
operator|&
operator|(
name|Modifier
operator|.
name|PROTECTED
operator||
name|Modifier
operator|.
name|PUBLIC
operator|)
operator|)
operator|==
literal|0
condition|)
block|{
return|return
name|SAME_PACKAGE
return|;
block|}
name|Class
index|[]
name|parameterTypes
init|=
name|member
operator|instanceof
name|Constructor
condition|?
operator|(
operator|(
name|Constructor
operator|)
name|member
operator|)
operator|.
name|getParameterTypes
argument_list|()
else|:
operator|(
operator|(
name|Method
operator|)
name|member
operator|)
operator|.
name|getParameterTypes
argument_list|()
decl_stmt|;
for|for
control|(
name|Class
argument_list|<
name|?
argument_list|>
name|type
range|:
name|parameterTypes
control|)
block|{
if|if
condition|(
name|forType
argument_list|(
name|type
argument_list|)
operator|==
name|SAME_PACKAGE
condition|)
block|{
return|return
name|SAME_PACKAGE
return|;
block|}
block|}
return|return
name|PUBLIC
return|;
block|}
DECL|method|forType
specifier|public
specifier|static
name|Visibility
name|forType
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|type
parameter_list|)
block|{
return|return
operator|(
name|type
operator|.
name|getModifiers
argument_list|()
operator|&
operator|(
name|Modifier
operator|.
name|PROTECTED
operator||
name|Modifier
operator|.
name|PUBLIC
operator|)
operator|)
operator|!=
literal|0
condition|?
name|PUBLIC
else|:
name|SAME_PACKAGE
return|;
block|}
DECL|method|and
specifier|public
specifier|abstract
name|Visibility
name|and
parameter_list|(
name|Visibility
name|that
parameter_list|)
function_decl|;
block|}
comment|/**    * Loader for Guice-generated classes. For referenced classes, this delegates to either either the    * user's classloader (which is the parent of this classloader) or Guice's class loader.    */
DECL|class|BridgeClassLoader
specifier|private
specifier|static
class|class
name|BridgeClassLoader
extends|extends
name|ClassLoader
block|{
DECL|method|BridgeClassLoader
specifier|public
name|BridgeClassLoader
parameter_list|(
name|ClassLoader
name|usersClassLoader
parameter_list|)
block|{
name|super
argument_list|(
name|usersClassLoader
argument_list|)
expr_stmt|;
block|}
DECL|method|loadClass
annotation|@
name|Override
specifier|protected
name|Class
argument_list|<
name|?
argument_list|>
name|loadClass
parameter_list|(
name|String
name|name
parameter_list|,
name|boolean
name|resolve
parameter_list|)
throws|throws
name|ClassNotFoundException
block|{
comment|// delegate internal requests to Guice class space
if|if
condition|(
name|name
operator|.
name|startsWith
argument_list|(
name|GUICE_INTERNAL_PACKAGE
argument_list|)
operator|||
name|name
operator|.
name|startsWith
argument_list|(
name|CGLIB_PACKAGE
argument_list|)
condition|)
block|{
try|try
block|{
name|Class
argument_list|<
name|?
argument_list|>
name|clazz
init|=
name|GUICE_CLASS_LOADER
operator|.
name|loadClass
argument_list|(
name|name
argument_list|)
decl_stmt|;
if|if
condition|(
name|resolve
condition|)
block|{
name|resolveClass
argument_list|(
name|clazz
argument_list|)
expr_stmt|;
block|}
return|return
name|clazz
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// fall back to classic delegation
block|}
block|}
return|return
name|super
operator|.
name|loadClass
argument_list|(
name|name
argument_list|,
name|resolve
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

