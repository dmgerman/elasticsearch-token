begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.messy.tests
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|messy
operator|.
name|tests
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|admin
operator|.
name|indices
operator|.
name|delete
operator|.
name|DeleteIndexResponse
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|admin
operator|.
name|indices
operator|.
name|exists
operator|.
name|indices
operator|.
name|IndicesExistsRequest
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|admin
operator|.
name|indices
operator|.
name|exists
operator|.
name|indices
operator|.
name|IndicesExistsResponse
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|admin
operator|.
name|indices
operator|.
name|settings
operator|.
name|get
operator|.
name|GetSettingsRequest
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|admin
operator|.
name|indices
operator|.
name|settings
operator|.
name|get
operator|.
name|GetSettingsResponse
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|indexedscripts
operator|.
name|put
operator|.
name|PutIndexedScriptResponse
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|support
operator|.
name|IndicesOptions
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|IndexNotFoundException
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
name|ScriptService
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
name|groovy
operator|.
name|GroovyPlugin
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
name|Collections
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

begin_class
annotation|@
name|ESIntegTestCase
operator|.
name|ClusterScope
argument_list|(
name|scope
operator|=
name|ESIntegTestCase
operator|.
name|Scope
operator|.
name|TEST
argument_list|)
DECL|class|ScriptIndexSettingsTests
specifier|public
class|class
name|ScriptIndexSettingsTests
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
name|Collections
operator|.
name|singleton
argument_list|(
name|GroovyPlugin
operator|.
name|class
argument_list|)
return|;
block|}
DECL|method|testScriptIndexSettings
specifier|public
name|void
name|testScriptIndexSettings
parameter_list|()
block|{
name|PutIndexedScriptResponse
name|putIndexedScriptResponse
init|=
name|client
argument_list|()
operator|.
name|preparePutIndexedScript
argument_list|()
operator|.
name|setId
argument_list|(
literal|"foobar"
argument_list|)
operator|.
name|setScriptLang
argument_list|(
literal|"groovy"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"{ \"script\": 1 }"
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|putIndexedScriptResponse
operator|.
name|isCreated
argument_list|()
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
name|IndicesExistsRequest
name|existsRequest
init|=
operator|new
name|IndicesExistsRequest
argument_list|()
decl_stmt|;
name|String
index|[]
name|index
init|=
operator|new
name|String
index|[
literal|1
index|]
decl_stmt|;
name|index
index|[
literal|0
index|]
operator|=
name|ScriptService
operator|.
name|SCRIPT_INDEX
expr_stmt|;
name|existsRequest
operator|.
name|indices
argument_list|(
name|index
argument_list|)
expr_stmt|;
name|IndicesExistsResponse
name|existsResponse
init|=
name|cluster
argument_list|()
operator|.
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|exists
argument_list|(
name|existsRequest
argument_list|)
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|existsResponse
operator|.
name|isExists
argument_list|()
argument_list|)
expr_stmt|;
name|GetSettingsRequest
name|settingsRequest
init|=
operator|new
name|GetSettingsRequest
argument_list|()
decl_stmt|;
name|settingsRequest
operator|.
name|indices
argument_list|(
name|ScriptService
operator|.
name|SCRIPT_INDEX
argument_list|)
expr_stmt|;
name|settingsRequest
operator|.
name|indicesOptions
argument_list|(
name|IndicesOptions
operator|.
name|strictExpandOpen
argument_list|()
argument_list|)
expr_stmt|;
name|GetSettingsResponse
name|settingsResponse
init|=
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|getSettings
argument_list|(
name|settingsRequest
argument_list|)
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|String
name|numberOfShards
init|=
name|settingsResponse
operator|.
name|getSetting
argument_list|(
name|ScriptService
operator|.
name|SCRIPT_INDEX
argument_list|,
literal|"index.number_of_shards"
argument_list|)
decl_stmt|;
name|String
name|numberOfReplicas
init|=
name|settingsResponse
operator|.
name|getSetting
argument_list|(
name|ScriptService
operator|.
name|SCRIPT_INDEX
argument_list|,
literal|"index.auto_expand_replicas"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"Number of shards should be 1"
argument_list|,
literal|"1"
argument_list|,
name|numberOfShards
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Auto expand replicas should be 0-all"
argument_list|,
literal|"0-all"
argument_list|,
name|numberOfReplicas
argument_list|)
expr_stmt|;
block|}
DECL|method|testDeleteScriptIndex
specifier|public
name|void
name|testDeleteScriptIndex
parameter_list|()
block|{
name|PutIndexedScriptResponse
name|putIndexedScriptResponse
init|=
name|client
argument_list|()
operator|.
name|preparePutIndexedScript
argument_list|()
operator|.
name|setId
argument_list|(
literal|"foobar"
argument_list|)
operator|.
name|setScriptLang
argument_list|(
literal|"groovy"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"{ \"script\": 1 }"
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|putIndexedScriptResponse
operator|.
name|isCreated
argument_list|()
argument_list|)
expr_stmt|;
name|DeleteIndexResponse
name|deleteResponse
init|=
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareDelete
argument_list|(
name|ScriptService
operator|.
name|SCRIPT_INDEX
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|deleteResponse
operator|.
name|isAcknowledged
argument_list|()
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
try|try
block|{
name|client
argument_list|()
operator|.
name|prepareGetIndexedScript
argument_list|(
literal|"groovy"
argument_list|,
literal|"foobar"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|fail
argument_list|(
literal|"Expected IndexNotFoundException"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IndexNotFoundException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"no such index"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit
