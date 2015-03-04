begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.cluster.routing
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|routing
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
name|get
operator|.
name|GetIndexResponse
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
name|get
operator|.
name|GetResponse
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
name|search
operator|.
name|SearchResponse
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
name|metadata
operator|.
name|IndexMetaData
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
name|ImmutableSettings
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
name|node
operator|.
name|Node
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
name|SearchHit
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
name|java
operator|.
name|nio
operator|.
name|file
operator|.
name|Path
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|file
operator|.
name|Paths
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
name|assertHitCount
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
name|assertSearchResponse
import|;
end_import

begin_class
annotation|@
name|ElasticsearchIntegrationTest
operator|.
name|ClusterScope
argument_list|(
name|scope
operator|=
name|ElasticsearchIntegrationTest
operator|.
name|Scope
operator|.
name|TEST
argument_list|,
name|numDataNodes
operator|=
literal|0
argument_list|,
name|minNumDataNodes
operator|=
literal|0
argument_list|,
name|maxNumDataNodes
operator|=
literal|0
argument_list|)
DECL|class|RoutingBackwardCompatibilityUponUpgradeTests
specifier|public
class|class
name|RoutingBackwardCompatibilityUponUpgradeTests
extends|extends
name|ElasticsearchIntegrationTest
block|{
DECL|method|testDefaultRouting
specifier|public
name|void
name|testDefaultRouting
parameter_list|()
throws|throws
name|Exception
block|{
name|test
argument_list|(
literal|"default_routing_1_x"
argument_list|,
name|DjbHashFunction
operator|.
name|class
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
DECL|method|testCustomRouting
specifier|public
name|void
name|testCustomRouting
parameter_list|()
throws|throws
name|Exception
block|{
name|test
argument_list|(
literal|"custom_routing_1_x"
argument_list|,
name|SimpleHashFunction
operator|.
name|class
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
DECL|method|test
specifier|private
name|void
name|test
parameter_list|(
name|String
name|name
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|HashFunction
argument_list|>
name|expectedHashFunction
parameter_list|,
name|boolean
name|expectedUseType
parameter_list|)
throws|throws
name|Exception
block|{
name|Path
name|zippedIndexDir
init|=
name|Paths
operator|.
name|get
argument_list|(
name|getClass
argument_list|()
operator|.
name|getResource
argument_list|(
literal|"/org/elasticsearch/cluster/routing/"
operator|+
name|name
operator|+
literal|".zip"
argument_list|)
operator|.
name|toURI
argument_list|()
argument_list|)
decl_stmt|;
name|Settings
name|baseSettings
init|=
name|prepareBackwardsDataDir
argument_list|(
name|zippedIndexDir
argument_list|)
decl_stmt|;
name|internalCluster
argument_list|()
operator|.
name|startNode
argument_list|(
name|ImmutableSettings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|baseSettings
argument_list|)
operator|.
name|put
argument_list|(
name|Node
operator|.
name|HTTP_ENABLED
argument_list|,
literal|true
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|ensureYellow
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
name|GetIndexResponse
name|getIndexResponse
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
name|prepareGetIndex
argument_list|()
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertArrayEquals
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"test"
block|}
argument_list|,
name|getIndexResponse
operator|.
name|indices
argument_list|()
argument_list|)
expr_stmt|;
name|GetSettingsResponse
name|getSettingsResponse
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
name|prepareGetSettings
argument_list|(
literal|"test"
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|expectedHashFunction
operator|.
name|getName
argument_list|()
argument_list|,
name|getSettingsResponse
operator|.
name|getSetting
argument_list|(
literal|"test"
argument_list|,
name|IndexMetaData
operator|.
name|SETTING_LEGACY_ROUTING_HASH_FUNCTION
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Boolean
operator|.
name|valueOf
argument_list|(
name|expectedUseType
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|getSettingsResponse
operator|.
name|getSetting
argument_list|(
literal|"test"
argument_list|,
name|IndexMetaData
operator|.
name|SETTING_LEGACY_ROUTING_USE_TYPE
argument_list|)
argument_list|)
expr_stmt|;
name|SearchResponse
name|allDocs
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"test"
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertSearchResponse
argument_list|(
name|allDocs
argument_list|)
expr_stmt|;
name|assertHitCount
argument_list|(
name|allDocs
argument_list|,
literal|4
argument_list|)
expr_stmt|;
comment|// Make sure routing works
for|for
control|(
name|SearchHit
name|hit
range|:
name|allDocs
operator|.
name|getHits
argument_list|()
operator|.
name|hits
argument_list|()
control|)
block|{
name|GetResponse
name|get
init|=
name|client
argument_list|()
operator|.
name|prepareGet
argument_list|(
name|hit
operator|.
name|index
argument_list|()
argument_list|,
name|hit
operator|.
name|type
argument_list|()
argument_list|,
name|hit
operator|.
name|id
argument_list|()
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|get
operator|.
name|isExists
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

