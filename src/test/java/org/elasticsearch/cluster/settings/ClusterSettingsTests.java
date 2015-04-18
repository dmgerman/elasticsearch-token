begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.cluster.settings
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|settings
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
name|util
operator|.
name|LuceneTestCase
operator|.
name|Slow
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
name|cluster
operator|.
name|settings
operator|.
name|ClusterUpdateSettingsResponse
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
name|routing
operator|.
name|allocation
operator|.
name|decider
operator|.
name|DisableAllocationDecider
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
name|discovery
operator|.
name|DiscoverySettings
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
name|test
operator|.
name|ElasticsearchIntegrationTest
operator|.
name|ClusterScope
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
name|ElasticsearchIntegrationTest
operator|.
name|Scope
operator|.
name|TEST
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
name|*
import|;
end_import

begin_class
annotation|@
name|ClusterScope
argument_list|(
name|scope
operator|=
name|TEST
argument_list|)
annotation|@
name|Slow
DECL|class|ClusterSettingsTests
specifier|public
class|class
name|ClusterSettingsTests
extends|extends
name|ElasticsearchIntegrationTest
block|{
annotation|@
name|Test
DECL|method|clusterNonExistingSettingsUpdate
specifier|public
name|void
name|clusterNonExistingSettingsUpdate
parameter_list|()
block|{
name|String
name|key1
init|=
literal|"no_idea_what_you_are_talking_about"
decl_stmt|;
name|int
name|value1
init|=
literal|10
decl_stmt|;
name|ClusterUpdateSettingsResponse
name|response
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
name|prepareUpdateSettings
argument_list|()
operator|.
name|setTransientSettings
argument_list|(
name|ImmutableSettings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|key1
argument_list|,
name|value1
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertAcked
argument_list|(
name|response
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getTransientSettings
argument_list|()
operator|.
name|getAsMap
argument_list|()
operator|.
name|entrySet
argument_list|()
argument_list|,
name|Matchers
operator|.
name|emptyIterable
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|clusterSettingsUpdateResponse
specifier|public
name|void
name|clusterSettingsUpdateResponse
parameter_list|()
block|{
name|String
name|key1
init|=
literal|"indices.cache.filter.size"
decl_stmt|;
name|int
name|value1
init|=
literal|10
decl_stmt|;
name|String
name|key2
init|=
name|DisableAllocationDecider
operator|.
name|CLUSTER_ROUTING_ALLOCATION_DISABLE_ALLOCATION
decl_stmt|;
name|boolean
name|value2
init|=
literal|true
decl_stmt|;
name|Settings
name|transientSettings1
init|=
name|ImmutableSettings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|key1
argument_list|,
name|value1
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|Settings
name|persistentSettings1
init|=
name|ImmutableSettings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|key2
argument_list|,
name|value2
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|ClusterUpdateSettingsResponse
name|response1
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
name|prepareUpdateSettings
argument_list|()
operator|.
name|setTransientSettings
argument_list|(
name|transientSettings1
argument_list|)
operator|.
name|setPersistentSettings
argument_list|(
name|persistentSettings1
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|assertAcked
argument_list|(
name|response1
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response1
operator|.
name|getTransientSettings
argument_list|()
operator|.
name|get
argument_list|(
name|key1
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response1
operator|.
name|getTransientSettings
argument_list|()
operator|.
name|get
argument_list|(
name|key2
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response1
operator|.
name|getPersistentSettings
argument_list|()
operator|.
name|get
argument_list|(
name|key1
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response1
operator|.
name|getPersistentSettings
argument_list|()
operator|.
name|get
argument_list|(
name|key2
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|Settings
name|transientSettings2
init|=
name|ImmutableSettings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|key1
argument_list|,
name|value1
argument_list|)
operator|.
name|put
argument_list|(
name|key2
argument_list|,
name|value2
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|Settings
name|persistentSettings2
init|=
name|ImmutableSettings
operator|.
name|EMPTY
decl_stmt|;
name|ClusterUpdateSettingsResponse
name|response2
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
name|prepareUpdateSettings
argument_list|()
operator|.
name|setTransientSettings
argument_list|(
name|transientSettings2
argument_list|)
operator|.
name|setPersistentSettings
argument_list|(
name|persistentSettings2
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|assertAcked
argument_list|(
name|response2
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response2
operator|.
name|getTransientSettings
argument_list|()
operator|.
name|get
argument_list|(
name|key1
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response2
operator|.
name|getTransientSettings
argument_list|()
operator|.
name|get
argument_list|(
name|key2
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response2
operator|.
name|getPersistentSettings
argument_list|()
operator|.
name|get
argument_list|(
name|key1
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response2
operator|.
name|getPersistentSettings
argument_list|()
operator|.
name|get
argument_list|(
name|key2
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|Settings
name|transientSettings3
init|=
name|ImmutableSettings
operator|.
name|EMPTY
decl_stmt|;
name|Settings
name|persistentSettings3
init|=
name|ImmutableSettings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|key1
argument_list|,
name|value1
argument_list|)
operator|.
name|put
argument_list|(
name|key2
argument_list|,
name|value2
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|ClusterUpdateSettingsResponse
name|response3
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
name|prepareUpdateSettings
argument_list|()
operator|.
name|setTransientSettings
argument_list|(
name|transientSettings3
argument_list|)
operator|.
name|setPersistentSettings
argument_list|(
name|persistentSettings3
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|assertAcked
argument_list|(
name|response3
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response3
operator|.
name|getTransientSettings
argument_list|()
operator|.
name|get
argument_list|(
name|key1
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response3
operator|.
name|getTransientSettings
argument_list|()
operator|.
name|get
argument_list|(
name|key2
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response3
operator|.
name|getPersistentSettings
argument_list|()
operator|.
name|get
argument_list|(
name|key1
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response3
operator|.
name|getPersistentSettings
argument_list|()
operator|.
name|get
argument_list|(
name|key2
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testUpdateDiscoveryPublishTimeout
specifier|public
name|void
name|testUpdateDiscoveryPublishTimeout
parameter_list|()
block|{
name|DiscoverySettings
name|discoverySettings
init|=
name|internalCluster
argument_list|()
operator|.
name|getInstance
argument_list|(
name|DiscoverySettings
operator|.
name|class
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|discoverySettings
operator|.
name|getPublishTimeout
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|DiscoverySettings
operator|.
name|DEFAULT_PUBLISH_TIMEOUT
argument_list|)
argument_list|)
expr_stmt|;
name|ClusterUpdateSettingsResponse
name|response
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
name|prepareUpdateSettings
argument_list|()
operator|.
name|setTransientSettings
argument_list|(
name|ImmutableSettings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|DiscoverySettings
operator|.
name|PUBLISH_TIMEOUT
argument_list|,
literal|"1s"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertAcked
argument_list|(
name|response
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getTransientSettings
argument_list|()
operator|.
name|getAsMap
argument_list|()
operator|.
name|get
argument_list|(
name|DiscoverySettings
operator|.
name|PUBLISH_TIMEOUT
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"1s"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|discoverySettings
operator|.
name|getPublishTimeout
argument_list|()
operator|.
name|seconds
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1l
argument_list|)
argument_list|)
expr_stmt|;
name|response
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
name|prepareUpdateSettings
argument_list|()
operator|.
name|setTransientSettings
argument_list|(
name|ImmutableSettings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|DiscoverySettings
operator|.
name|PUBLISH_TIMEOUT
argument_list|,
literal|"whatever"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|assertAcked
argument_list|(
name|response
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getTransientSettings
argument_list|()
operator|.
name|getAsMap
argument_list|()
operator|.
name|entrySet
argument_list|()
argument_list|,
name|Matchers
operator|.
name|emptyIterable
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|discoverySettings
operator|.
name|getPublishTimeout
argument_list|()
operator|.
name|seconds
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1l
argument_list|)
argument_list|)
expr_stmt|;
name|response
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
name|prepareUpdateSettings
argument_list|()
operator|.
name|setTransientSettings
argument_list|(
name|ImmutableSettings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|DiscoverySettings
operator|.
name|PUBLISH_TIMEOUT
argument_list|,
operator|-
literal|1
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|assertAcked
argument_list|(
name|response
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getTransientSettings
argument_list|()
operator|.
name|getAsMap
argument_list|()
operator|.
name|entrySet
argument_list|()
argument_list|,
name|Matchers
operator|.
name|emptyIterable
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|discoverySettings
operator|.
name|getPublishTimeout
argument_list|()
operator|.
name|seconds
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1l
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

