begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.script
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|script
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
name|common
operator|.
name|Nullable
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
name|util
operator|.
name|Map
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
name|unmodifiableMap
import|;
end_import

begin_comment
comment|/**  * A native script engine service.  */
end_comment

begin_class
DECL|class|NativeScriptEngineService
specifier|public
class|class
name|NativeScriptEngineService
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
literal|"native"
decl_stmt|;
DECL|field|scripts
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|NativeScriptFactory
argument_list|>
name|scripts
decl_stmt|;
DECL|method|NativeScriptEngineService
specifier|public
name|NativeScriptEngineService
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|NativeScriptFactory
argument_list|>
name|scripts
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|scripts
operator|=
name|unmodifiableMap
argument_list|(
name|scripts
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getType
specifier|public
name|String
name|getType
parameter_list|()
block|{
return|return
name|NAME
return|;
block|}
annotation|@
name|Override
DECL|method|getExtension
specifier|public
name|String
name|getExtension
parameter_list|()
block|{
return|return
literal|""
return|;
comment|// Native scripts have no extensions
block|}
annotation|@
name|Override
DECL|method|compile
specifier|public
name|Object
name|compile
parameter_list|(
name|String
name|scriptName
parameter_list|,
name|String
name|scriptSource
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|params
parameter_list|)
block|{
name|NativeScriptFactory
name|scriptFactory
init|=
name|scripts
operator|.
name|get
argument_list|(
name|scriptSource
argument_list|)
decl_stmt|;
if|if
condition|(
name|scriptFactory
operator|!=
literal|null
condition|)
block|{
return|return
name|scriptFactory
return|;
block|}
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Native script ["
operator|+
name|scriptSource
operator|+
literal|"] not found"
argument_list|)
throw|;
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
annotation|@
name|Nullable
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|vars
parameter_list|)
block|{
name|NativeScriptFactory
name|scriptFactory
init|=
operator|(
name|NativeScriptFactory
operator|)
name|compiledScript
operator|.
name|compiled
argument_list|()
decl_stmt|;
return|return
name|scriptFactory
operator|.
name|newScript
argument_list|(
name|vars
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
specifier|final
name|SearchLookup
name|lookup
parameter_list|,
annotation|@
name|Nullable
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|vars
parameter_list|)
block|{
specifier|final
name|NativeScriptFactory
name|scriptFactory
init|=
operator|(
name|NativeScriptFactory
operator|)
name|compiledScript
operator|.
name|compiled
argument_list|()
decl_stmt|;
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
name|AbstractSearchScript
name|script
init|=
operator|(
name|AbstractSearchScript
operator|)
name|scriptFactory
operator|.
name|newScript
argument_list|(
name|vars
argument_list|)
decl_stmt|;
name|script
operator|.
name|setLookup
argument_list|(
name|lookup
operator|.
name|getLeafSearchLookup
argument_list|(
name|context
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|script
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
name|scriptFactory
operator|.
name|needsScores
argument_list|()
return|;
block|}
block|}
return|;
block|}
annotation|@
name|Override
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
block|{     }
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
comment|// Nothing to do here
block|}
annotation|@
name|Override
DECL|method|isInlineScriptEnabled
specifier|public
name|boolean
name|isInlineScriptEnabled
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
block|}
end_class

end_unit

