begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.test
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|test
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
name|cluster
operator|.
name|health
operator|.
name|ClusterHealthResponse
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
name|health
operator|.
name|ClusterHealthStatus
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
name|create
operator|.
name|CreateIndexRequestBuilder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cache
operator|.
name|recycler
operator|.
name|PageCacheRecycler
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|client
operator|.
name|Client
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|client
operator|.
name|Requests
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
name|ClusterName
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
name|cluster
operator|.
name|metadata
operator|.
name|MetaData
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
name|node
operator|.
name|DiscoveryNode
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
name|Priority
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
name|common
operator|.
name|unit
operator|.
name|TimeValue
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
name|util
operator|.
name|BigArrays
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
name|util
operator|.
name|concurrent
operator|.
name|EsExecutors
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
name|xcontent
operator|.
name|XContentBuilder
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
name|IndexService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|IndicesService
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
name|node
operator|.
name|NodeBuilder
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
name|internal
operator|.
name|InternalNode
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
name|internal
operator|.
name|SearchContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|threadpool
operator|.
name|ThreadPool
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|After
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Ignore
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

begin_comment
comment|/**  * A test that keep a singleton node started for all tests that can be used to get  * references to Guice injectors in unit tests.  */
end_comment

begin_class
annotation|@
name|Ignore
DECL|class|ElasticsearchSingleNodeTest
specifier|public
specifier|abstract
class|class
name|ElasticsearchSingleNodeTest
extends|extends
name|ElasticsearchTestCase
block|{
DECL|class|Holder
specifier|private
specifier|static
class|class
name|Holder
block|{
comment|// lazy init on first access
DECL|field|NODE
specifier|private
specifier|static
name|Node
name|NODE
init|=
name|newNode
argument_list|()
decl_stmt|;
DECL|method|reset
specifier|private
specifier|static
name|void
name|reset
parameter_list|()
block|{
assert|assert
name|NODE
operator|!=
literal|null
assert|;
name|node
argument_list|()
operator|.
name|stop
argument_list|()
expr_stmt|;
name|Holder
operator|.
name|NODE
operator|=
name|newNode
argument_list|()
expr_stmt|;
block|}
block|}
DECL|method|cleanup
specifier|static
name|void
name|cleanup
parameter_list|(
name|boolean
name|resetNode
parameter_list|)
block|{
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
name|prepareDelete
argument_list|(
literal|"*"
argument_list|)
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|resetNode
condition|)
block|{
name|Holder
operator|.
name|reset
argument_list|()
expr_stmt|;
block|}
name|MetaData
name|metaData
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
name|get
argument_list|()
operator|.
name|getState
argument_list|()
operator|.
name|getMetaData
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
literal|"test leaves persistent cluster metadata behind: "
operator|+
name|metaData
operator|.
name|persistentSettings
argument_list|()
operator|.
name|getAsMap
argument_list|()
argument_list|,
name|metaData
operator|.
name|persistentSettings
argument_list|()
operator|.
name|getAsMap
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"test leaves transient cluster metadata behind: "
operator|+
name|metaData
operator|.
name|transientSettings
argument_list|()
operator|.
name|getAsMap
argument_list|()
argument_list|,
name|metaData
operator|.
name|transientSettings
argument_list|()
operator|.
name|getAsMap
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|After
DECL|method|after
specifier|public
name|void
name|after
parameter_list|()
block|{
name|cleanup
argument_list|(
name|resetNodeAfterTest
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**      * This method returns<code>true</code> if the node that is used in the background should be reset      * after each test. This is useful if the test changes the cluster state metadata etc. The default is      *<code>false</code>.      */
DECL|method|resetNodeAfterTest
specifier|protected
name|boolean
name|resetNodeAfterTest
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
DECL|method|newNode
specifier|private
specifier|static
name|Node
name|newNode
parameter_list|()
block|{
name|Node
name|build
init|=
name|NodeBuilder
operator|.
name|nodeBuilder
argument_list|()
operator|.
name|local
argument_list|(
literal|true
argument_list|)
operator|.
name|data
argument_list|(
literal|true
argument_list|)
operator|.
name|settings
argument_list|(
name|ImmutableSettings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|ClusterName
operator|.
name|SETTING
argument_list|,
name|nodeName
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
literal|"node.name"
argument_list|,
name|nodeName
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_NUMBER_OF_SHARDS
argument_list|,
literal|1
argument_list|)
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_NUMBER_OF_REPLICAS
argument_list|,
literal|0
argument_list|)
operator|.
name|put
argument_list|(
literal|"script.disable_dynamic"
argument_list|,
literal|false
argument_list|)
operator|.
name|put
argument_list|(
name|EsExecutors
operator|.
name|PROCESSORS
argument_list|,
literal|1
argument_list|)
comment|// limit the number of threads created
operator|.
name|put
argument_list|(
literal|"http.enabled"
argument_list|,
literal|false
argument_list|)
operator|.
name|put
argument_list|(
literal|"config.ignore_system_properties"
argument_list|,
literal|true
argument_list|)
comment|// make sure we get what we set :)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|build
operator|.
name|start
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|DiscoveryNode
operator|.
name|localNode
argument_list|(
name|build
operator|.
name|settings
argument_list|()
argument_list|)
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|build
return|;
block|}
comment|/**      * Returns a client to the single-node cluster.      */
DECL|method|client
specifier|public
specifier|static
name|Client
name|client
parameter_list|()
block|{
return|return
name|Holder
operator|.
name|NODE
operator|.
name|client
argument_list|()
return|;
block|}
comment|/**      * Returns the single test nodes name.      */
DECL|method|nodeName
specifier|public
specifier|static
name|String
name|nodeName
parameter_list|()
block|{
return|return
name|ElasticsearchSingleNodeTest
operator|.
name|class
operator|.
name|getName
argument_list|()
return|;
block|}
comment|/**      * Returns the name of the cluster used for the single test node.      */
DECL|method|clusterName
specifier|public
specifier|static
name|String
name|clusterName
parameter_list|()
block|{
return|return
name|ElasticsearchSingleNodeTest
operator|.
name|class
operator|.
name|getName
argument_list|()
return|;
block|}
comment|/**      * Return a reference to the singleton node.      */
DECL|method|node
specifier|protected
specifier|static
name|Node
name|node
parameter_list|()
block|{
return|return
name|Holder
operator|.
name|NODE
return|;
block|}
comment|/**      * Get an instance for a particular class using the injector of the singleton node.      */
DECL|method|getInstanceFromNode
specifier|protected
specifier|static
parameter_list|<
name|T
parameter_list|>
name|T
name|getInstanceFromNode
parameter_list|(
name|Class
argument_list|<
name|T
argument_list|>
name|clazz
parameter_list|)
block|{
return|return
operator|(
operator|(
name|InternalNode
operator|)
name|Holder
operator|.
name|NODE
operator|)
operator|.
name|injector
argument_list|()
operator|.
name|getInstance
argument_list|(
name|clazz
argument_list|)
return|;
block|}
comment|/**      * Create a new index on the singleton node with empty index settings.      */
DECL|method|createIndex
specifier|protected
specifier|static
name|IndexService
name|createIndex
parameter_list|(
name|String
name|index
parameter_list|)
block|{
return|return
name|createIndex
argument_list|(
name|index
argument_list|,
name|ImmutableSettings
operator|.
name|EMPTY
argument_list|)
return|;
block|}
comment|/**      * Create a new index on the singleton node with the provided index settings.      */
DECL|method|createIndex
specifier|protected
specifier|static
name|IndexService
name|createIndex
parameter_list|(
name|String
name|index
parameter_list|,
name|Settings
name|settings
parameter_list|)
block|{
return|return
name|createIndex
argument_list|(
name|index
argument_list|,
name|settings
argument_list|,
literal|null
argument_list|,
operator|(
name|XContentBuilder
operator|)
literal|null
argument_list|)
return|;
block|}
comment|/**      * Create a new index on the singleton node with the provided index settings.      */
DECL|method|createIndex
specifier|protected
specifier|static
name|IndexService
name|createIndex
parameter_list|(
name|String
name|index
parameter_list|,
name|Settings
name|settings
parameter_list|,
name|String
name|type
parameter_list|,
name|XContentBuilder
name|mappings
parameter_list|)
block|{
name|CreateIndexRequestBuilder
name|createIndexRequestBuilder
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
name|prepareCreate
argument_list|(
name|index
argument_list|)
operator|.
name|setSettings
argument_list|(
name|settings
argument_list|)
decl_stmt|;
if|if
condition|(
name|type
operator|!=
literal|null
operator|&&
name|mappings
operator|!=
literal|null
condition|)
block|{
name|createIndexRequestBuilder
operator|.
name|addMapping
argument_list|(
name|type
argument_list|,
name|mappings
argument_list|)
expr_stmt|;
block|}
return|return
name|createIndex
argument_list|(
name|index
argument_list|,
name|createIndexRequestBuilder
argument_list|)
return|;
block|}
comment|/**      * Create a new index on the singleton node with the provided index settings.      */
DECL|method|createIndex
specifier|protected
specifier|static
name|IndexService
name|createIndex
parameter_list|(
name|String
name|index
parameter_list|,
name|Settings
name|settings
parameter_list|,
name|String
name|type
parameter_list|,
name|Object
modifier|...
name|mappings
parameter_list|)
block|{
name|CreateIndexRequestBuilder
name|createIndexRequestBuilder
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
name|prepareCreate
argument_list|(
name|index
argument_list|)
operator|.
name|setSettings
argument_list|(
name|settings
argument_list|)
decl_stmt|;
if|if
condition|(
name|type
operator|!=
literal|null
operator|&&
name|mappings
operator|!=
literal|null
condition|)
block|{
name|createIndexRequestBuilder
operator|.
name|addMapping
argument_list|(
name|type
argument_list|,
name|mappings
argument_list|)
expr_stmt|;
block|}
return|return
name|createIndex
argument_list|(
name|index
argument_list|,
name|createIndexRequestBuilder
argument_list|)
return|;
block|}
DECL|method|createIndex
specifier|protected
specifier|static
name|IndexService
name|createIndex
parameter_list|(
name|String
name|index
parameter_list|,
name|CreateIndexRequestBuilder
name|createIndexRequestBuilder
parameter_list|)
block|{
name|assertAcked
argument_list|(
name|createIndexRequestBuilder
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
comment|// Wait for the index to be allocated so that cluster state updates don't override
comment|// changes that would have been done locally
name|ClusterHealthResponse
name|health
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
name|health
argument_list|(
name|Requests
operator|.
name|clusterHealthRequest
argument_list|(
name|index
argument_list|)
operator|.
name|waitForYellowStatus
argument_list|()
operator|.
name|waitForEvents
argument_list|(
name|Priority
operator|.
name|LANGUID
argument_list|)
operator|.
name|waitForRelocatingShards
argument_list|(
literal|0
argument_list|)
argument_list|)
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|health
operator|.
name|getStatus
argument_list|()
argument_list|,
name|lessThanOrEqualTo
argument_list|(
name|ClusterHealthStatus
operator|.
name|YELLOW
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"Cluster must be a single node cluster"
argument_list|,
name|health
operator|.
name|getNumberOfDataNodes
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|IndicesService
name|instanceFromNode
init|=
name|getInstanceFromNode
argument_list|(
name|IndicesService
operator|.
name|class
argument_list|)
decl_stmt|;
return|return
name|instanceFromNode
operator|.
name|indexServiceSafe
argument_list|(
name|index
argument_list|)
return|;
block|}
DECL|method|engine
specifier|protected
specifier|static
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|engine
operator|.
name|Engine
name|engine
parameter_list|(
name|IndexService
name|service
parameter_list|)
block|{
return|return
name|service
operator|.
name|shard
argument_list|(
literal|0
argument_list|)
operator|.
name|engine
argument_list|()
return|;
block|}
comment|/**      * Create a new search context.      */
DECL|method|createSearchContext
specifier|protected
specifier|static
name|SearchContext
name|createSearchContext
parameter_list|(
name|IndexService
name|indexService
parameter_list|)
block|{
name|BigArrays
name|bigArrays
init|=
name|indexService
operator|.
name|injector
argument_list|()
operator|.
name|getInstance
argument_list|(
name|BigArrays
operator|.
name|class
argument_list|)
decl_stmt|;
name|ThreadPool
name|threadPool
init|=
name|indexService
operator|.
name|injector
argument_list|()
operator|.
name|getInstance
argument_list|(
name|ThreadPool
operator|.
name|class
argument_list|)
decl_stmt|;
name|PageCacheRecycler
name|pageCacheRecycler
init|=
name|indexService
operator|.
name|injector
argument_list|()
operator|.
name|getInstance
argument_list|(
name|PageCacheRecycler
operator|.
name|class
argument_list|)
decl_stmt|;
return|return
operator|new
name|TestSearchContext
argument_list|(
name|threadPool
argument_list|,
name|pageCacheRecycler
argument_list|,
name|bigArrays
argument_list|,
name|indexService
argument_list|,
name|indexService
operator|.
name|cache
argument_list|()
operator|.
name|filter
argument_list|()
argument_list|,
name|indexService
operator|.
name|fieldData
argument_list|()
argument_list|)
return|;
block|}
comment|/**      * Ensures the cluster has a green state via the cluster health API. This method will also wait for relocations.      * It is useful to ensure that all action on the cluster have finished and all shards that were currently relocating      * are now allocated and started.      */
DECL|method|ensureGreen
specifier|public
name|ClusterHealthStatus
name|ensureGreen
parameter_list|(
name|String
modifier|...
name|indices
parameter_list|)
block|{
return|return
name|ensureGreen
argument_list|(
name|TimeValue
operator|.
name|timeValueSeconds
argument_list|(
literal|30
argument_list|)
argument_list|,
name|indices
argument_list|)
return|;
block|}
comment|/**      * Ensures the cluster has a green state via the cluster health API. This method will also wait for relocations.      * It is useful to ensure that all action on the cluster have finished and all shards that were currently relocating      * are now allocated and started.      *      * @param timeout time out value to set on {@link org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest}      */
DECL|method|ensureGreen
specifier|public
name|ClusterHealthStatus
name|ensureGreen
parameter_list|(
name|TimeValue
name|timeout
parameter_list|,
name|String
modifier|...
name|indices
parameter_list|)
block|{
name|ClusterHealthResponse
name|actionGet
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
name|health
argument_list|(
name|Requests
operator|.
name|clusterHealthRequest
argument_list|(
name|indices
argument_list|)
operator|.
name|timeout
argument_list|(
name|timeout
argument_list|)
operator|.
name|waitForGreenStatus
argument_list|()
operator|.
name|waitForEvents
argument_list|(
name|Priority
operator|.
name|LANGUID
argument_list|)
operator|.
name|waitForRelocatingShards
argument_list|(
literal|0
argument_list|)
argument_list|)
operator|.
name|actionGet
argument_list|()
decl_stmt|;
if|if
condition|(
name|actionGet
operator|.
name|isTimedOut
argument_list|()
condition|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"ensureGreen timed out, cluster state:\n{}\n{}"
argument_list|,
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
name|get
argument_list|()
operator|.
name|getState
argument_list|()
operator|.
name|prettyPrint
argument_list|()
argument_list|,
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|preparePendingClusterTasks
argument_list|()
operator|.
name|get
argument_list|()
operator|.
name|prettyPrint
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"timed out waiting for green state"
argument_list|,
name|actionGet
operator|.
name|isTimedOut
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|assertThat
argument_list|(
name|actionGet
operator|.
name|getStatus
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|ClusterHealthStatus
operator|.
name|GREEN
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|debug
argument_list|(
literal|"indices {} are green"
argument_list|,
name|indices
operator|.
name|length
operator|==
literal|0
condition|?
literal|"[_all]"
else|:
name|indices
argument_list|)
expr_stmt|;
return|return
name|actionGet
operator|.
name|getStatus
argument_list|()
return|;
block|}
block|}
end_class

end_unit

