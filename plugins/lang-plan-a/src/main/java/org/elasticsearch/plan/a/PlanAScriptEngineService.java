begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.plan.a
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|plan
operator|.
name|a
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|index
operator|.
name|LeafReaderContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|SpecialPermission
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
name|CompiledScript
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
name|LeafSearchScript
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
name|SearchScript
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|lookup
operator|.
name|SearchLookup
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|AccessControlContext
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
name|Permissions
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
name|security
operator|.
name|ProtectionDomain
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

begin_class
DECL|class|PlanAScriptEngineService
specifier|public
class|class
name|PlanAScriptEngineService
extends|extends
name|AbstractComponent
implements|implements
name|ScriptEngineService
block|{
DECL|field|NAME
specifier|public
specifier|static
specifier|final
name|String
name|NAME
init|=
literal|"plan-a"
decl_stmt|;
comment|// TODO: this should really be per-script since scripts do so many different things?
DECL|field|compilerSettings
specifier|private
specifier|static
specifier|final
name|CompilerSettings
name|compilerSettings
init|=
operator|new
name|CompilerSettings
argument_list|()
decl_stmt|;
DECL|field|NUMERIC_OVERFLOW
specifier|public
specifier|static
specifier|final
name|String
name|NUMERIC_OVERFLOW
init|=
literal|"plan-a.numeric_overflow"
decl_stmt|;
comment|// TODO: how should custom definitions be specified?
DECL|field|definition
specifier|private
name|Definition
name|definition
init|=
literal|null
decl_stmt|;
annotation|@
name|Inject
DECL|method|PlanAScriptEngineService
specifier|public
name|PlanAScriptEngineService
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
name|compilerSettings
operator|.
name|setNumericOverflow
argument_list|(
name|settings
operator|.
name|getAsBoolean
argument_list|(
name|NUMERIC_OVERFLOW
argument_list|,
name|compilerSettings
operator|.
name|getNumericOverflow
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|setDefinition
specifier|public
name|void
name|setDefinition
parameter_list|(
specifier|final
name|Definition
name|definition
parameter_list|)
block|{
name|this
operator|.
name|definition
operator|=
operator|new
name|Definition
argument_list|(
name|definition
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|types
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
name|NAME
block|}
return|;
block|}
annotation|@
name|Override
DECL|method|extensions
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
name|NAME
block|}
return|;
block|}
annotation|@
name|Override
DECL|method|sandboxed
specifier|public
name|boolean
name|sandboxed
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
comment|// context used during compilation
DECL|field|COMPILATION_CONTEXT
specifier|private
specifier|static
specifier|final
name|AccessControlContext
name|COMPILATION_CONTEXT
decl_stmt|;
static|static
block|{
name|Permissions
name|none
init|=
operator|new
name|Permissions
argument_list|()
decl_stmt|;
name|none
operator|.
name|setReadOnly
argument_list|()
expr_stmt|;
name|COMPILATION_CONTEXT
operator|=
operator|new
name|AccessControlContext
argument_list|(
operator|new
name|ProtectionDomain
index|[]
block|{
operator|new
name|ProtectionDomain
argument_list|(
literal|null
argument_list|,
name|none
argument_list|)
block|}
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|compile
specifier|public
name|Object
name|compile
parameter_list|(
name|String
name|script
parameter_list|)
block|{
comment|// check we ourselves are not being called by unprivileged code
name|SecurityManager
name|sm
init|=
name|System
operator|.
name|getSecurityManager
argument_list|()
decl_stmt|;
if|if
condition|(
name|sm
operator|!=
literal|null
condition|)
block|{
name|sm
operator|.
name|checkPermission
argument_list|(
operator|new
name|SpecialPermission
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// create our loader (which loads compiled code with no permissions)
name|Compiler
operator|.
name|Loader
name|loader
init|=
name|AccessController
operator|.
name|doPrivileged
argument_list|(
operator|new
name|PrivilegedAction
argument_list|<
name|Compiler
operator|.
name|Loader
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Compiler
operator|.
name|Loader
name|run
parameter_list|()
block|{
return|return
operator|new
name|Compiler
operator|.
name|Loader
argument_list|(
name|getClass
argument_list|()
operator|.
name|getClassLoader
argument_list|()
argument_list|)
return|;
block|}
block|}
argument_list|)
decl_stmt|;
comment|// drop all permissions to actually compile the code itself
return|return
name|AccessController
operator|.
name|doPrivileged
argument_list|(
operator|new
name|PrivilegedAction
argument_list|<
name|Executable
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Executable
name|run
parameter_list|()
block|{
return|return
name|Compiler
operator|.
name|compile
argument_list|(
name|loader
argument_list|,
literal|"something"
argument_list|,
name|script
argument_list|,
name|definition
argument_list|,
name|compilerSettings
argument_list|)
return|;
block|}
block|}
argument_list|,
name|COMPILATION_CONTEXT
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|executable
specifier|public
name|ExecutableScript
name|executable
parameter_list|(
name|CompiledScript
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
return|return
operator|new
name|ScriptImpl
argument_list|(
operator|(
name|Executable
operator|)
name|compiledScript
operator|.
name|compiled
argument_list|()
argument_list|,
name|vars
argument_list|,
literal|null
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|search
specifier|public
name|SearchScript
name|search
parameter_list|(
name|CompiledScript
name|compiledScript
parameter_list|,
name|SearchLookup
name|lookup
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
return|return
operator|new
name|SearchScript
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|LeafSearchScript
name|getLeafSearchScript
parameter_list|(
name|LeafReaderContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|ScriptImpl
argument_list|(
operator|(
name|Executable
operator|)
name|compiledScript
operator|.
name|compiled
argument_list|()
argument_list|,
name|vars
argument_list|,
name|lookup
operator|.
name|getLeafSearchLookup
argument_list|(
name|context
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|needsScores
parameter_list|()
block|{
return|return
literal|true
return|;
comment|// TODO: maybe even do these different and more like expressions.
block|}
block|}
return|;
block|}
annotation|@
name|Override
DECL|method|scriptRemoved
specifier|public
name|void
name|scriptRemoved
parameter_list|(
name|CompiledScript
name|script
parameter_list|)
block|{
comment|// nothing to do
block|}
annotation|@
name|Override
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
comment|// nothing to do
block|}
block|}
end_class

end_unit

