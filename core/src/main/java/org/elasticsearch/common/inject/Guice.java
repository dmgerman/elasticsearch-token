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
name|java
operator|.
name|util
operator|.
name|Arrays
import|;
end_import

begin_comment
comment|/**  * The entry point to the Guice framework. Creates {@link Injector}s from  * {@link Module}s.  *<p/>  *<p>Guice supports a model of development that draws clear boundaries between  * APIs, Implementations of these APIs, Modules which configure these  * implementations, and finally Applications which consist of a collection of  * Modules. It is the Application, which typically defines your {@code main()}  * method, that bootstraps the Guice Injector using the {@code Guice} class, as  * in this example:  *<pre>  *     public class FooApplication {  *       public static void main(String[] args) {  *         Injector injector = Guice.createInjector(  *             new ModuleA(),  *             new ModuleB(),  *             . . .  *             new FooApplicationFlagsModule(args)  *         );  *  *         // Now just bootstrap the application and you're done  *         FooStarter starter = injector.getInstance(FooStarter.class);  *         starter.runApplication();  *       }  *     }  *</pre>  */
end_comment

begin_class
DECL|class|Guice
specifier|public
specifier|final
class|class
name|Guice
block|{
DECL|method|Guice
specifier|private
name|Guice
parameter_list|()
block|{     }
comment|/**      * Creates an injector for the given set of modules.      *      * @throws CreationException if one or more errors occur during Injector      *                           construction      */
DECL|method|createInjector
specifier|public
specifier|static
name|Injector
name|createInjector
parameter_list|(
name|Module
modifier|...
name|modules
parameter_list|)
block|{
return|return
name|createInjector
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|modules
argument_list|)
argument_list|)
return|;
block|}
comment|/**      * Creates an injector for the given set of modules.      *      * @throws CreationException if one or more errors occur during Injector      *                           creation      */
DECL|method|createInjector
specifier|public
specifier|static
name|Injector
name|createInjector
parameter_list|(
name|Iterable
argument_list|<
name|?
extends|extends
name|Module
argument_list|>
name|modules
parameter_list|)
block|{
return|return
name|createInjector
argument_list|(
name|Stage
operator|.
name|DEVELOPMENT
argument_list|,
name|modules
argument_list|)
return|;
block|}
comment|/**      * Creates an injector for the given set of modules, in a given development      * stage.      *      * @throws CreationException if one or more errors occur during Injector      *                           creation      */
DECL|method|createInjector
specifier|public
specifier|static
name|Injector
name|createInjector
parameter_list|(
name|Stage
name|stage
parameter_list|,
name|Module
modifier|...
name|modules
parameter_list|)
block|{
return|return
name|createInjector
argument_list|(
name|stage
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|modules
argument_list|)
argument_list|)
return|;
block|}
comment|/**      * Creates an injector for the given set of modules, in a given development      * stage.      *      * @throws CreationException if one or more errors occur during Injector      *                           construction      */
DECL|method|createInjector
specifier|public
specifier|static
name|Injector
name|createInjector
parameter_list|(
name|Stage
name|stage
parameter_list|,
name|Iterable
argument_list|<
name|?
extends|extends
name|Module
argument_list|>
name|modules
parameter_list|)
block|{
return|return
operator|new
name|InjectorBuilder
argument_list|()
operator|.
name|stage
argument_list|(
name|stage
argument_list|)
operator|.
name|addModules
argument_list|(
name|modules
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
block|}
end_class

end_unit
