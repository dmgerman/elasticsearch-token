begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.script.groovy
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|groovy
package|;
end_package

begin_import
import|import
name|groovy
operator|.
name|lang
operator|.
name|Binding
import|;
end_import

begin_import
import|import
name|groovy
operator|.
name|lang
operator|.
name|GroovyClassLoader
import|;
end_import

begin_import
import|import
name|groovy
operator|.
name|lang
operator|.
name|Script
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
name|component
operator|.
name|AbstractComponent
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
name|settings
operator|.
name|Settings
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|ExecutableScript
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|ScriptEngineService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|ScriptException
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
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicLong
import|;
end_import

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|GroovyScriptEngineService
specifier|public
class|class
name|GroovyScriptEngineService
extends|extends
name|AbstractComponent
implements|implements
name|ScriptEngineService
block|{
DECL|field|counter
specifier|private
specifier|final
name|AtomicLong
name|counter
init|=
operator|new
name|AtomicLong
argument_list|()
decl_stmt|;
DECL|field|loader
specifier|private
specifier|final
name|GroovyClassLoader
name|loader
decl_stmt|;
DECL|method|GroovyScriptEngineService
annotation|@
name|Inject
specifier|public
name|GroovyScriptEngineService
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|loader
operator|=
operator|new
name|GroovyClassLoader
argument_list|(
name|settings
operator|.
name|getClassLoader
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|close
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
block|{
name|loader
operator|.
name|clearCache
argument_list|()
expr_stmt|;
block|}
DECL|method|types
annotation|@
name|Override
specifier|public
name|String
index|[]
name|types
parameter_list|()
block|{
return|return
operator|new
name|String
index|[]
block|{
literal|"groovy"
block|}
return|;
block|}
DECL|method|extensions
annotation|@
name|Override
specifier|public
name|String
index|[]
name|extensions
parameter_list|()
block|{
return|return
operator|new
name|String
index|[]
block|{
literal|"groovy"
block|}
return|;
block|}
DECL|method|compile
annotation|@
name|Override
specifier|public
name|Object
name|compile
parameter_list|(
name|String
name|script
parameter_list|)
block|{
return|return
name|loader
operator|.
name|parseClass
argument_list|(
name|script
argument_list|,
name|generateScriptName
argument_list|()
argument_list|)
return|;
block|}
DECL|method|executable
annotation|@
name|Override
specifier|public
name|ExecutableScript
name|executable
parameter_list|(
name|Object
name|compiledScript
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|vars
parameter_list|)
block|{
try|try
block|{
name|Class
name|scriptClass
init|=
operator|(
name|Class
operator|)
name|compiledScript
decl_stmt|;
name|Script
name|scriptObject
init|=
operator|(
name|Script
operator|)
name|scriptClass
operator|.
name|newInstance
argument_list|()
decl_stmt|;
name|Binding
name|binding
init|=
operator|new
name|Binding
argument_list|()
decl_stmt|;
if|if
condition|(
name|vars
operator|!=
literal|null
condition|)
block|{
name|binding
operator|.
name|getVariables
argument_list|()
operator|.
name|putAll
argument_list|(
name|vars
argument_list|)
expr_stmt|;
block|}
name|scriptObject
operator|.
name|setBinding
argument_list|(
name|binding
argument_list|)
expr_stmt|;
return|return
operator|new
name|GroovyExecutableScript
argument_list|(
name|scriptObject
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ScriptException
argument_list|(
literal|"failed to build executable script"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
DECL|method|execute
annotation|@
name|Override
specifier|public
name|Object
name|execute
parameter_list|(
name|Object
name|compiledScript
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|vars
parameter_list|)
block|{
try|try
block|{
name|Class
name|scriptClass
init|=
operator|(
name|Class
operator|)
name|compiledScript
decl_stmt|;
name|Script
name|scriptObject
init|=
operator|(
name|Script
operator|)
name|scriptClass
operator|.
name|newInstance
argument_list|()
decl_stmt|;
name|Binding
name|binding
init|=
operator|new
name|Binding
argument_list|(
name|vars
argument_list|)
decl_stmt|;
name|scriptObject
operator|.
name|setBinding
argument_list|(
name|binding
argument_list|)
expr_stmt|;
return|return
name|scriptObject
operator|.
name|run
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ScriptException
argument_list|(
literal|"failed to execute script"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
DECL|method|unwrap
annotation|@
name|Override
specifier|public
name|Object
name|unwrap
parameter_list|(
name|Object
name|value
parameter_list|)
block|{
return|return
name|value
return|;
block|}
DECL|method|generateScriptName
specifier|private
name|String
name|generateScriptName
parameter_list|()
block|{
return|return
literal|"Script"
operator|+
name|counter
operator|.
name|incrementAndGet
argument_list|()
operator|+
literal|".groovy"
return|;
block|}
DECL|class|GroovyExecutableScript
specifier|public
specifier|static
class|class
name|GroovyExecutableScript
implements|implements
name|ExecutableScript
block|{
DECL|field|script
specifier|private
specifier|final
name|Script
name|script
decl_stmt|;
DECL|method|GroovyExecutableScript
specifier|public
name|GroovyExecutableScript
parameter_list|(
name|Script
name|script
parameter_list|)
block|{
name|this
operator|.
name|script
operator|=
name|script
expr_stmt|;
block|}
DECL|method|run
annotation|@
name|Override
specifier|public
name|Object
name|run
parameter_list|()
block|{
return|return
name|script
operator|.
name|run
argument_list|()
return|;
block|}
DECL|method|run
annotation|@
name|Override
specifier|public
name|Object
name|run
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|vars
parameter_list|)
block|{
name|script
operator|.
name|getBinding
argument_list|()
operator|.
name|getVariables
argument_list|()
operator|.
name|putAll
argument_list|(
name|vars
argument_list|)
expr_stmt|;
return|return
name|script
operator|.
name|run
argument_list|()
return|;
block|}
DECL|method|unwrap
annotation|@
name|Override
specifier|public
name|Object
name|unwrap
parameter_list|(
name|Object
name|value
parameter_list|)
block|{
return|return
name|value
return|;
block|}
block|}
block|}
end_class

end_unit

