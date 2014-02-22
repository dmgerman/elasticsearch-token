begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.indices.warmer
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|warmer
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
name|warmer
operator|.
name|delete
operator|.
name|DeleteWarmerResponse
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|ClusterState
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
name|logging
operator|.
name|ESLogger
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
name|logging
operator|.
name|Loggers
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
name|index
operator|.
name|query
operator|.
name|QueryBuilders
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
name|warmer
operator|.
name|IndexWarmersMetaData
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
name|ElasticsearchIntegrationTest
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
name|ElasticsearchIntegrationTest
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
name|ElasticsearchIntegrationTest
operator|.
name|Scope
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
name|TestCluster
operator|.
name|RestartCallback
import|;
end_import

begin_import
import|import
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|settings
operator|.
name|ImmutableSettings
operator|.
name|settingsBuilder
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|hamcrest
operator|.
name|ElasticsearchAssertions
operator|.
name|assertAcked
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
name|equalTo
import|;
end_import

begin_comment
comment|/**  */
end_comment

begin_class
annotation|@
name|ClusterScope
argument_list|(
name|numNodes
operator|=
literal|0
argument_list|,
name|scope
operator|=
name|Scope
operator|.
name|TEST
argument_list|)
DECL|class|LocalGatewayIndicesWarmerTests
specifier|public
class|class
name|LocalGatewayIndicesWarmerTests
extends|extends
name|ElasticsearchIntegrationTest
block|{
DECL|field|logger
specifier|private
specifier|final
name|ESLogger
name|logger
init|=
name|Loggers
operator|.
name|getLogger
argument_list|(
name|LocalGatewayIndicesWarmerTests
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Test
DECL|method|testStatePersistence
specifier|public
name|void
name|testStatePersistence
parameter_list|()
throws|throws
name|Exception
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"--> starting 1 nodes"
argument_list|)
expr_stmt|;
name|cluster
argument_list|()
operator|.
name|startNode
argument_list|(
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"gateway.type"
argument_list|,
literal|"local"
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> putting two templates"
argument_list|)
expr_stmt|;
name|createIndex
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
name|ensureYellow
argument_list|()
expr_stmt|;
name|assertAcked
argument_list|(
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|preparePutWarmer
argument_list|(
literal|"warmer_1"
argument_list|)
operator|.
name|setSearchRequest
argument_list|(
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
name|termQuery
argument_list|(
literal|"field"
argument_list|,
literal|"value1"
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertAcked
argument_list|(
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|preparePutWarmer
argument_list|(
literal|"warmer_2"
argument_list|)
operator|.
name|setSearchRequest
argument_list|(
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
name|termQuery
argument_list|(
literal|"field"
argument_list|,
literal|"value2"
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> put template with warmer"
argument_list|)
expr_stmt|;
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|preparePutTemplate
argument_list|(
literal|"template_1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"{\n"
operator|+
literal|"    \"template\" : \"xxx\",\n"
operator|+
literal|"    \"warmers\" : {\n"
operator|+
literal|"        \"warmer_1\" : {\n"
operator|+
literal|"            \"types\" : [],\n"
operator|+
literal|"            \"source\" : {\n"
operator|+
literal|"                \"query\" : {\n"
operator|+
literal|"                    \"match_all\" : {}\n"
operator|+
literal|"                }\n"
operator|+
literal|"            }\n"
operator|+
literal|"        }\n"
operator|+
literal|"    }\n"
operator|+
literal|"}"
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> verify warmers are registered in cluster state"
argument_list|)
expr_stmt|;
name|ClusterState
name|clusterState
init|=
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareState
argument_list|()
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
operator|.
name|getState
argument_list|()
decl_stmt|;
name|IndexWarmersMetaData
name|warmersMetaData
init|=
name|clusterState
operator|.
name|metaData
argument_list|()
operator|.
name|index
argument_list|(
literal|"test"
argument_list|)
operator|.
name|custom
argument_list|(
name|IndexWarmersMetaData
operator|.
name|TYPE
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|warmersMetaData
argument_list|,
name|Matchers
operator|.
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|warmersMetaData
operator|.
name|entries
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|IndexWarmersMetaData
name|templateWarmers
init|=
name|clusterState
operator|.
name|metaData
argument_list|()
operator|.
name|templates
argument_list|()
operator|.
name|get
argument_list|(
literal|"template_1"
argument_list|)
operator|.
name|custom
argument_list|(
name|IndexWarmersMetaData
operator|.
name|TYPE
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|templateWarmers
argument_list|,
name|Matchers
operator|.
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|templateWarmers
operator|.
name|entries
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> restarting the node"
argument_list|)
expr_stmt|;
name|cluster
argument_list|()
operator|.
name|fullRestart
argument_list|(
operator|new
name|RestartCallback
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Settings
name|onNodeStopped
parameter_list|(
name|String
name|nodeName
parameter_list|)
throws|throws
name|Exception
block|{
return|return
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"gateway.type"
argument_list|,
literal|"local"
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|ensureYellow
argument_list|()
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> verify warmers are recovered"
argument_list|)
expr_stmt|;
name|clusterState
operator|=
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareState
argument_list|()
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
operator|.
name|getState
argument_list|()
expr_stmt|;
name|IndexWarmersMetaData
name|recoveredWarmersMetaData
init|=
name|clusterState
operator|.
name|metaData
argument_list|()
operator|.
name|index
argument_list|(
literal|"test"
argument_list|)
operator|.
name|custom
argument_list|(
name|IndexWarmersMetaData
operator|.
name|TYPE
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|recoveredWarmersMetaData
operator|.
name|entries
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|warmersMetaData
operator|.
name|entries
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|warmersMetaData
operator|.
name|entries
argument_list|()
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|assertThat
argument_list|(
name|recoveredWarmersMetaData
operator|.
name|entries
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|name
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|warmersMetaData
operator|.
name|entries
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|name
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|recoveredWarmersMetaData
operator|.
name|entries
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|source
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|warmersMetaData
operator|.
name|entries
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|source
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|logger
operator|.
name|info
argument_list|(
literal|"--> verify warmers in template are recovered"
argument_list|)
expr_stmt|;
name|IndexWarmersMetaData
name|recoveredTemplateWarmers
init|=
name|clusterState
operator|.
name|metaData
argument_list|()
operator|.
name|templates
argument_list|()
operator|.
name|get
argument_list|(
literal|"template_1"
argument_list|)
operator|.
name|custom
argument_list|(
name|IndexWarmersMetaData
operator|.
name|TYPE
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|recoveredTemplateWarmers
operator|.
name|entries
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|templateWarmers
operator|.
name|entries
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|templateWarmers
operator|.
name|entries
argument_list|()
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|assertThat
argument_list|(
name|recoveredTemplateWarmers
operator|.
name|entries
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|name
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|templateWarmers
operator|.
name|entries
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|name
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|recoveredTemplateWarmers
operator|.
name|entries
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|source
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|templateWarmers
operator|.
name|entries
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|source
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|logger
operator|.
name|info
argument_list|(
literal|"--> delete warmer warmer_1"
argument_list|)
expr_stmt|;
name|DeleteWarmerResponse
name|deleteWarmerResponse
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
name|prepareDeleteWarmer
argument_list|()
operator|.
name|setIndices
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setNames
argument_list|(
literal|"warmer_1"
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|deleteWarmerResponse
operator|.
name|isAcknowledged
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> verify warmers (delete) are registered in cluster state"
argument_list|)
expr_stmt|;
name|clusterState
operator|=
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareState
argument_list|()
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
operator|.
name|getState
argument_list|()
expr_stmt|;
name|warmersMetaData
operator|=
name|clusterState
operator|.
name|metaData
argument_list|()
operator|.
name|index
argument_list|(
literal|"test"
argument_list|)
operator|.
name|custom
argument_list|(
name|IndexWarmersMetaData
operator|.
name|TYPE
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|warmersMetaData
argument_list|,
name|Matchers
operator|.
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|warmersMetaData
operator|.
name|entries
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> restarting the node"
argument_list|)
expr_stmt|;
name|cluster
argument_list|()
operator|.
name|fullRestart
argument_list|(
operator|new
name|RestartCallback
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Settings
name|onNodeStopped
parameter_list|(
name|String
name|nodeName
parameter_list|)
throws|throws
name|Exception
block|{
return|return
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"gateway.type"
argument_list|,
literal|"local"
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|ensureYellow
argument_list|()
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> verify warmers are recovered"
argument_list|)
expr_stmt|;
name|clusterState
operator|=
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareState
argument_list|()
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
operator|.
name|getState
argument_list|()
expr_stmt|;
name|recoveredWarmersMetaData
operator|=
name|clusterState
operator|.
name|metaData
argument_list|()
operator|.
name|index
argument_list|(
literal|"test"
argument_list|)
operator|.
name|custom
argument_list|(
name|IndexWarmersMetaData
operator|.
name|TYPE
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|recoveredWarmersMetaData
operator|.
name|entries
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|warmersMetaData
operator|.
name|entries
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|warmersMetaData
operator|.
name|entries
argument_list|()
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|assertThat
argument_list|(
name|recoveredWarmersMetaData
operator|.
name|entries
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|name
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|warmersMetaData
operator|.
name|entries
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|name
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|recoveredWarmersMetaData
operator|.
name|entries
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|source
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|warmersMetaData
operator|.
name|entries
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|source
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

