begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.update
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|update
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
name|Nullable
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|plugins
operator|.
name|Plugin
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
name|AbstractExecutableScript
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
name|NativeScriptEngineService
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
name|NativeScriptFactory
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
name|Script
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
name|ScriptModule
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
name|ScriptService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|ESIntegTestCase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|ESIntegTestCase
operator|.
name|ClusterScope
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|ESIntegTestCase
operator|.
name|Scope
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
name|HashMap
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
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|hasKey
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|is
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
annotation|@
name|ClusterScope
argument_list|(
name|scope
operator|=
name|Scope
operator|.
name|SUITE
argument_list|,
name|numDataNodes
operator|=
literal|1
argument_list|)
DECL|class|UpdateByNativeScriptIT
specifier|public
class|class
name|UpdateByNativeScriptIT
extends|extends
name|ESIntegTestCase
block|{
annotation|@
name|Override
DECL|method|nodePlugins
specifier|protected
name|Collection
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|Plugin
argument_list|>
argument_list|>
name|nodePlugins
parameter_list|()
block|{
return|return
name|pluginList
argument_list|(
name|CustomNativeScriptFactory
operator|.
name|TestPlugin
operator|.
name|class
argument_list|)
return|;
block|}
DECL|method|testThatUpdateUsingNativeScriptWorks
specifier|public
name|void
name|testThatUpdateUsingNativeScriptWorks
parameter_list|()
throws|throws
name|Exception
block|{
name|createIndex
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
name|ensureYellow
argument_list|()
expr_stmt|;
name|index
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
literal|"1"
argument_list|,
literal|"text"
argument_list|,
literal|"value"
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|params
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|params
operator|.
name|put
argument_list|(
literal|"foo"
argument_list|,
literal|"SETVALUE"
argument_list|)
expr_stmt|;
name|client
argument_list|()
operator|.
name|prepareUpdate
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setScript
argument_list|(
operator|new
name|Script
argument_list|(
literal|"custom"
argument_list|,
name|ScriptService
operator|.
name|ScriptType
operator|.
name|INLINE
argument_list|,
name|NativeScriptEngineService
operator|.
name|NAME
argument_list|,
name|params
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|data
init|=
name|client
argument_list|()
operator|.
name|prepareGet
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|get
argument_list|()
operator|.
name|getSource
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|data
argument_list|,
name|hasKey
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|data
operator|.
name|get
argument_list|(
literal|"foo"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"SETVALUE"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|class|CustomNativeScriptFactory
specifier|public
specifier|static
class|class
name|CustomNativeScriptFactory
implements|implements
name|NativeScriptFactory
block|{
DECL|class|TestPlugin
specifier|public
specifier|static
class|class
name|TestPlugin
extends|extends
name|Plugin
block|{
DECL|method|onModule
specifier|public
name|void
name|onModule
parameter_list|(
name|ScriptModule
name|scriptModule
parameter_list|)
block|{
name|scriptModule
operator|.
name|registerScript
argument_list|(
literal|"custom"
argument_list|,
name|CustomNativeScriptFactory
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|newScript
specifier|public
name|ExecutableScript
name|newScript
parameter_list|(
annotation|@
name|Nullable
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|params
parameter_list|)
block|{
return|return
operator|new
name|CustomScript
argument_list|(
name|params
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|needsScores
specifier|public
name|boolean
name|needsScores
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
block|}
DECL|class|CustomScript
specifier|static
class|class
name|CustomScript
extends|extends
name|AbstractExecutableScript
block|{
DECL|field|params
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|params
decl_stmt|;
DECL|field|vars
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|vars
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|(
literal|2
argument_list|)
decl_stmt|;
DECL|method|CustomScript
specifier|public
name|CustomScript
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|params
parameter_list|)
block|{
name|this
operator|.
name|params
operator|=
name|params
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|run
specifier|public
name|Object
name|run
parameter_list|()
block|{
if|if
condition|(
name|vars
operator|.
name|containsKey
argument_list|(
literal|"ctx"
argument_list|)
operator|&&
name|vars
operator|.
name|get
argument_list|(
literal|"ctx"
argument_list|)
operator|instanceof
name|Map
condition|)
block|{
name|Map
name|ctx
init|=
operator|(
name|Map
operator|)
name|vars
operator|.
name|get
argument_list|(
literal|"ctx"
argument_list|)
decl_stmt|;
if|if
condition|(
name|ctx
operator|.
name|containsKey
argument_list|(
literal|"_source"
argument_list|)
operator|&&
name|ctx
operator|.
name|get
argument_list|(
literal|"_source"
argument_list|)
operator|instanceof
name|Map
condition|)
block|{
name|Map
name|source
init|=
operator|(
name|Map
operator|)
name|ctx
operator|.
name|get
argument_list|(
literal|"_source"
argument_list|)
decl_stmt|;
name|source
operator|.
name|putAll
argument_list|(
name|params
argument_list|)
expr_stmt|;
block|}
block|}
comment|// return value does not matter, the UpdateHelper class
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|setNextVar
specifier|public
name|void
name|setNextVar
parameter_list|(
name|String
name|name
parameter_list|,
name|Object
name|value
parameter_list|)
block|{
name|vars
operator|.
name|put
argument_list|(
name|name
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

