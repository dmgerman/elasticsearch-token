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
name|com
operator|.
name|carrotsearch
operator|.
name|randomizedtesting
operator|.
name|RandomizedContext
import|;
end_import

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
name|IOUtils
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
name|network
operator|.
name|NetworkModule
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
name|NamedXContentRegistry
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
name|common
operator|.
name|xcontent
operator|.
name|XContentType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|env
operator|.
name|Environment
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|env
operator|.
name|NodeEnvironment
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
name|Index
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
name|MockNode
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
name|NodeValidationException
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
name|test
operator|.
name|discovery
operator|.
name|TestZenDiscovery
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
name|elasticsearch
operator|.
name|transport
operator|.
name|MockTcpTransportPlugin
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|AfterClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|BeforeClass
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
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
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

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|lessThanOrEqualTo
import|;
end_import

begin_comment
comment|/**  * A test that keep a singleton node started for all tests that can be used to get  * references to Guice injectors in unit tests.  */
end_comment

begin_class
DECL|class|ESSingleNodeTestCase
specifier|public
specifier|abstract
class|class
name|ESSingleNodeTestCase
extends|extends
name|ESTestCase
block|{
DECL|field|NODE
specifier|private
specifier|static
name|Node
name|NODE
init|=
literal|null
decl_stmt|;
DECL|method|startNode
specifier|protected
name|void
name|startNode
parameter_list|(
name|long
name|seed
parameter_list|)
throws|throws
name|Exception
block|{
assert|assert
name|NODE
operator|==
literal|null
assert|;
name|NODE
operator|=
name|RandomizedContext
operator|.
name|current
argument_list|()
operator|.
name|runWithPrivateRandomness
argument_list|(
name|seed
argument_list|,
name|this
operator|::
name|newNode
argument_list|)
expr_stmt|;
comment|// we must wait for the node to actually be up and running. otherwise the node might have started,
comment|// elected itself master but might not yet have removed the
comment|// SERVICE_UNAVAILABLE/1/state not recovered / initialized block
name|ClusterHealthResponse
name|clusterHealthResponse
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
name|prepareHealth
argument_list|()
operator|.
name|setWaitForGreenStatus
argument_list|()
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertFalse
argument_list|(
name|clusterHealthResponse
operator|.
name|isTimedOut
argument_list|()
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
literal|"one_shard_index_template"
argument_list|)
operator|.
name|setPatterns
argument_list|(
name|Collections
operator|.
name|singletonList
argument_list|(
literal|"*"
argument_list|)
argument_list|)
operator|.
name|setOrder
argument_list|(
literal|0
argument_list|)
operator|.
name|setSettings
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
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
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
DECL|method|stopNode
specifier|private
specifier|static
name|void
name|stopNode
parameter_list|()
throws|throws
name|IOException
block|{
name|Node
name|node
init|=
name|NODE
decl_stmt|;
name|NODE
operator|=
literal|null
expr_stmt|;
name|IOUtils
operator|.
name|close
argument_list|(
name|node
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|setUp
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|setUp
argument_list|()
expr_stmt|;
comment|//the seed has to be created regardless of whether it will be used or not, for repeatability
name|long
name|seed
init|=
name|random
argument_list|()
operator|.
name|nextLong
argument_list|()
decl_stmt|;
comment|// Create the node lazily, on the first test. This is ok because we do not randomize any settings,
comment|// only the cluster name. This allows us to have overridden properties for plugins and the version to use.
if|if
condition|(
name|NODE
operator|==
literal|null
condition|)
block|{
name|startNode
argument_list|(
name|seed
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|tearDown
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"[{}#{}]: cleaning up after test"
argument_list|,
name|getTestClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
argument_list|,
name|getTestName
argument_list|()
argument_list|)
expr_stmt|;
name|super
operator|.
name|tearDown
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
name|prepareDelete
argument_list|(
literal|"*"
argument_list|)
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
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
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|resetNodeAfterTest
argument_list|()
condition|)
block|{
assert|assert
name|NODE
operator|!=
literal|null
assert|;
name|stopNode
argument_list|()
expr_stmt|;
comment|//the seed can be created within this if as it will either be executed before every test method or will never be.
name|startNode
argument_list|(
name|random
argument_list|()
operator|.
name|nextLong
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|BeforeClass
DECL|method|setUpClass
specifier|public
specifier|static
name|void
name|setUpClass
parameter_list|()
throws|throws
name|Exception
block|{
name|stopNode
argument_list|()
expr_stmt|;
block|}
annotation|@
name|AfterClass
DECL|method|tearDownClass
specifier|public
specifier|static
name|void
name|tearDownClass
parameter_list|()
throws|throws
name|IOException
block|{
name|stopNode
argument_list|()
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
comment|/** The plugin classes that should be added to the node. */
DECL|method|getPlugins
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
name|getPlugins
parameter_list|()
block|{
return|return
name|Collections
operator|.
name|emptyList
argument_list|()
return|;
block|}
comment|/** Helper method to create list of plugins without specifying generic types. */
annotation|@
name|SafeVarargs
annotation|@
name|SuppressWarnings
argument_list|(
literal|"varargs"
argument_list|)
comment|// due to type erasure, the varargs type is non-reifiable, which causes this warning
DECL|method|pluginList
specifier|protected
specifier|final
name|Collection
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|Plugin
argument_list|>
argument_list|>
name|pluginList
parameter_list|(
name|Class
argument_list|<
name|?
extends|extends
name|Plugin
argument_list|>
modifier|...
name|plugins
parameter_list|)
block|{
return|return
name|Arrays
operator|.
name|asList
argument_list|(
name|plugins
argument_list|)
return|;
block|}
comment|/** Additional settings to add when creating the node. Also allows overriding the default settings. */
DECL|method|nodeSettings
specifier|protected
name|Settings
name|nodeSettings
parameter_list|()
block|{
return|return
name|Settings
operator|.
name|EMPTY
return|;
block|}
DECL|method|newNode
specifier|private
name|Node
name|newNode
parameter_list|()
block|{
specifier|final
name|Path
name|tempDir
init|=
name|createTempDir
argument_list|()
decl_stmt|;
name|Settings
name|settings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|ClusterName
operator|.
name|CLUSTER_NAME_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
name|InternalTestCluster
operator|.
name|clusterName
argument_list|(
literal|"single-node-cluster"
argument_list|,
name|random
argument_list|()
operator|.
name|nextLong
argument_list|()
argument_list|)
argument_list|)
operator|.
name|put
argument_list|(
name|Environment
operator|.
name|PATH_HOME_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
name|tempDir
argument_list|)
operator|.
name|put
argument_list|(
name|Environment
operator|.
name|PATH_REPO_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
name|tempDir
operator|.
name|resolve
argument_list|(
literal|"repo"
argument_list|)
argument_list|)
comment|// TODO: use a consistent data path for custom paths
comment|// This needs to tie into the ESIntegTestCase#indexSettings() method
operator|.
name|put
argument_list|(
name|Environment
operator|.
name|PATH_SHARED_DATA_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
name|createTempDir
argument_list|()
operator|.
name|getParent
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
literal|"node.name"
argument_list|,
literal|"node_s_0"
argument_list|)
operator|.
name|put
argument_list|(
name|ScriptService
operator|.
name|SCRIPT_MAX_COMPILATIONS_PER_MINUTE
operator|.
name|getKey
argument_list|()
argument_list|,
literal|1000
argument_list|)
operator|.
name|put
argument_list|(
name|EsExecutors
operator|.
name|PROCESSORS_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
literal|1
argument_list|)
comment|// limit the number of threads created
operator|.
name|put
argument_list|(
name|NetworkModule
operator|.
name|HTTP_ENABLED
operator|.
name|getKey
argument_list|()
argument_list|,
literal|false
argument_list|)
operator|.
name|put
argument_list|(
literal|"transport.type"
argument_list|,
name|MockTcpTransportPlugin
operator|.
name|MOCK_TCP_TRANSPORT_NAME
argument_list|)
operator|.
name|put
argument_list|(
name|Node
operator|.
name|NODE_DATA_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
literal|true
argument_list|)
operator|.
name|put
argument_list|(
name|NodeEnvironment
operator|.
name|NODE_ID_SEED_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
name|random
argument_list|()
operator|.
name|nextLong
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|nodeSettings
argument_list|()
argument_list|)
comment|// allow test cases to provide their own settings or override these
operator|.
name|build
argument_list|()
decl_stmt|;
name|Collection
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|Plugin
argument_list|>
argument_list|>
name|plugins
init|=
name|getPlugins
argument_list|()
decl_stmt|;
if|if
condition|(
name|plugins
operator|.
name|contains
argument_list|(
name|MockTcpTransportPlugin
operator|.
name|class
argument_list|)
operator|==
literal|false
condition|)
block|{
name|plugins
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|plugins
argument_list|)
expr_stmt|;
name|plugins
operator|.
name|add
argument_list|(
name|MockTcpTransportPlugin
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|plugins
operator|.
name|contains
argument_list|(
name|TestZenDiscovery
operator|.
name|TestPlugin
operator|.
name|class
argument_list|)
operator|==
literal|false
condition|)
block|{
name|plugins
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|plugins
argument_list|)
expr_stmt|;
name|plugins
operator|.
name|add
argument_list|(
name|TestZenDiscovery
operator|.
name|TestPlugin
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
name|Node
name|build
init|=
operator|new
name|MockNode
argument_list|(
name|settings
argument_list|,
name|plugins
argument_list|)
decl_stmt|;
try|try
block|{
name|build
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NodeValidationException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
return|return
name|build
return|;
block|}
comment|/**      * Returns a client to the single-node cluster.      */
DECL|method|client
specifier|public
name|Client
name|client
parameter_list|()
block|{
return|return
name|NODE
operator|.
name|client
argument_list|()
return|;
block|}
comment|/**      * Return a reference to the singleton node.      */
DECL|method|node
specifier|protected
name|Node
name|node
parameter_list|()
block|{
return|return
name|NODE
return|;
block|}
comment|/**      * Get an instance for a particular class using the injector of the singleton node.      */
DECL|method|getInstanceFromNode
specifier|protected
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
name|NODE
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
name|Settings
operator|.
name|EMPTY
argument_list|)
return|;
block|}
comment|/**      * Create a new index on the singleton node with the provided index settings.      */
DECL|method|createIndex
specifier|protected
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
name|waitForNoRelocatingShards
argument_list|(
literal|true
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
name|resolveIndex
argument_list|(
name|index
argument_list|)
argument_list|)
return|;
block|}
DECL|method|resolveIndex
specifier|public
name|Index
name|resolveIndex
parameter_list|(
name|String
name|index
parameter_list|)
block|{
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
name|setIndices
argument_list|(
name|index
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
literal|"index "
operator|+
name|index
operator|+
literal|" not found"
argument_list|,
name|getIndexResponse
operator|.
name|getSettings
argument_list|()
operator|.
name|containsKey
argument_list|(
name|index
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|uuid
init|=
name|getIndexResponse
operator|.
name|getSettings
argument_list|()
operator|.
name|get
argument_list|(
name|index
argument_list|)
operator|.
name|get
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_INDEX_UUID
argument_list|)
decl_stmt|;
return|return
operator|new
name|Index
argument_list|(
name|index
argument_list|,
name|uuid
argument_list|)
return|;
block|}
comment|/**      * Create a new search context.      */
DECL|method|createSearchContext
specifier|protected
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
name|getBigArrays
argument_list|()
decl_stmt|;
name|ThreadPool
name|threadPool
init|=
name|indexService
operator|.
name|getThreadPool
argument_list|()
decl_stmt|;
return|return
operator|new
name|TestSearchContext
argument_list|(
name|threadPool
argument_list|,
name|bigArrays
argument_list|,
name|indexService
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
name|waitForNoRelocatingShards
argument_list|(
literal|true
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
annotation|@
name|Override
DECL|method|xContentRegistry
specifier|protected
name|NamedXContentRegistry
name|xContentRegistry
parameter_list|()
block|{
return|return
name|getInstanceFromNode
argument_list|(
name|NamedXContentRegistry
operator|.
name|class
argument_list|)
return|;
block|}
block|}
end_class

end_unit

