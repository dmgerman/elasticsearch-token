begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.river
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|river
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableMap
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableSet
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Maps
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticSearchException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ExceptionsHelper
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
name|ActionListener
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
name|NoShardAvailableActionException
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
name|WriteConsistencyLevel
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
name|mapping
operator|.
name|delete
operator|.
name|DeleteMappingResponse
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
name|cluster
operator|.
name|ClusterService
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
name|block
operator|.
name|ClusterBlockException
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
name|collect
operator|.
name|MapBuilder
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
name|AbstractLifecycleComponent
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
name|inject
operator|.
name|Injector
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
name|Injectors
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
name|ModulesBuilder
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
name|XContentFactory
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
name|IndexMissingException
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
name|PluginsService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|river
operator|.
name|cluster
operator|.
name|RiverClusterChangedEvent
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|river
operator|.
name|cluster
operator|.
name|RiverClusterService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|river
operator|.
name|cluster
operator|.
name|RiverClusterState
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|river
operator|.
name|cluster
operator|.
name|RiverClusterStateListener
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|river
operator|.
name|routing
operator|.
name|RiverRouting
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
name|CountDownLatch
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|RiversService
specifier|public
class|class
name|RiversService
extends|extends
name|AbstractLifecycleComponent
argument_list|<
name|RiversService
argument_list|>
block|{
DECL|field|riverIndexName
specifier|private
specifier|final
name|String
name|riverIndexName
decl_stmt|;
DECL|field|client
specifier|private
name|Client
name|client
decl_stmt|;
DECL|field|threadPool
specifier|private
specifier|final
name|ThreadPool
name|threadPool
decl_stmt|;
DECL|field|clusterService
specifier|private
specifier|final
name|ClusterService
name|clusterService
decl_stmt|;
DECL|field|typesRegistry
specifier|private
specifier|final
name|RiversTypesRegistry
name|typesRegistry
decl_stmt|;
DECL|field|injector
specifier|private
specifier|final
name|Injector
name|injector
decl_stmt|;
DECL|field|riversInjectors
specifier|private
specifier|final
name|Map
argument_list|<
name|RiverName
argument_list|,
name|Injector
argument_list|>
name|riversInjectors
init|=
name|Maps
operator|.
name|newHashMap
argument_list|()
decl_stmt|;
DECL|field|rivers
specifier|private
specifier|volatile
name|ImmutableMap
argument_list|<
name|RiverName
argument_list|,
name|River
argument_list|>
name|rivers
init|=
name|ImmutableMap
operator|.
name|of
argument_list|()
decl_stmt|;
annotation|@
name|Inject
DECL|method|RiversService
specifier|public
name|RiversService
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|Client
name|client
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|,
name|ClusterService
name|clusterService
parameter_list|,
name|RiversTypesRegistry
name|typesRegistry
parameter_list|,
name|RiverClusterService
name|riverClusterService
parameter_list|,
name|Injector
name|injector
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|riverIndexName
operator|=
name|RiverIndexName
operator|.
name|Conf
operator|.
name|indexName
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|client
operator|=
name|client
expr_stmt|;
name|this
operator|.
name|threadPool
operator|=
name|threadPool
expr_stmt|;
name|this
operator|.
name|clusterService
operator|=
name|clusterService
expr_stmt|;
name|this
operator|.
name|typesRegistry
operator|=
name|typesRegistry
expr_stmt|;
name|this
operator|.
name|injector
operator|=
name|injector
expr_stmt|;
name|riverClusterService
operator|.
name|add
argument_list|(
operator|new
name|ApplyRivers
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doStart
specifier|protected
name|void
name|doStart
parameter_list|()
throws|throws
name|ElasticSearchException
block|{     }
annotation|@
name|Override
DECL|method|doStop
specifier|protected
name|void
name|doStop
parameter_list|()
throws|throws
name|ElasticSearchException
block|{
name|ImmutableSet
argument_list|<
name|RiverName
argument_list|>
name|indices
init|=
name|ImmutableSet
operator|.
name|copyOf
argument_list|(
name|this
operator|.
name|rivers
operator|.
name|keySet
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|CountDownLatch
name|latch
init|=
operator|new
name|CountDownLatch
argument_list|(
name|indices
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
specifier|final
name|RiverName
name|riverName
range|:
name|indices
control|)
block|{
name|threadPool
operator|.
name|cached
argument_list|()
operator|.
name|execute
argument_list|(
operator|new
name|Runnable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
name|closeRiver
argument_list|(
name|riverName
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"failed to delete river on stop [{}]/[{}]"
argument_list|,
name|e
argument_list|,
name|riverName
operator|.
name|type
argument_list|()
argument_list|,
name|riverName
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|latch
operator|.
name|await
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
comment|// ignore
block|}
block|}
annotation|@
name|Override
DECL|method|doClose
specifier|protected
name|void
name|doClose
parameter_list|()
throws|throws
name|ElasticSearchException
block|{     }
DECL|method|createRiver
specifier|public
specifier|synchronized
name|void
name|createRiver
parameter_list|(
name|RiverName
name|riverName
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|settings
parameter_list|)
throws|throws
name|ElasticSearchException
block|{
if|if
condition|(
name|riversInjectors
operator|.
name|containsKey
argument_list|(
name|riverName
argument_list|)
condition|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"ignoring river [{}][{}] creation, already exists"
argument_list|,
name|riverName
operator|.
name|type
argument_list|()
argument_list|,
name|riverName
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
return|return;
block|}
name|logger
operator|.
name|debug
argument_list|(
literal|"creating river [{}][{}]"
argument_list|,
name|riverName
operator|.
name|type
argument_list|()
argument_list|,
name|riverName
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
name|ModulesBuilder
name|modules
init|=
operator|new
name|ModulesBuilder
argument_list|()
decl_stmt|;
name|modules
operator|.
name|add
argument_list|(
operator|new
name|RiverNameModule
argument_list|(
name|riverName
argument_list|)
argument_list|)
expr_stmt|;
name|modules
operator|.
name|add
argument_list|(
operator|new
name|RiverModule
argument_list|(
name|riverName
argument_list|,
name|settings
argument_list|,
name|this
operator|.
name|settings
argument_list|,
name|typesRegistry
argument_list|)
argument_list|)
expr_stmt|;
name|modules
operator|.
name|add
argument_list|(
operator|new
name|RiversPluginsModule
argument_list|(
name|this
operator|.
name|settings
argument_list|,
name|injector
operator|.
name|getInstance
argument_list|(
name|PluginsService
operator|.
name|class
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|Injector
name|indexInjector
init|=
name|modules
operator|.
name|createChildInjector
argument_list|(
name|injector
argument_list|)
decl_stmt|;
name|riversInjectors
operator|.
name|put
argument_list|(
name|riverName
argument_list|,
name|indexInjector
argument_list|)
expr_stmt|;
name|River
name|river
init|=
name|indexInjector
operator|.
name|getInstance
argument_list|(
name|River
operator|.
name|class
argument_list|)
decl_stmt|;
name|rivers
operator|=
name|MapBuilder
operator|.
name|newMapBuilder
argument_list|(
name|rivers
argument_list|)
operator|.
name|put
argument_list|(
name|riverName
argument_list|,
name|river
argument_list|)
operator|.
name|immutableMap
argument_list|()
expr_stmt|;
comment|// we need this start so there can be operations done (like creating an index) which can't be
comment|// done on create since Guice can't create two concurrent child injectors
name|river
operator|.
name|start
argument_list|()
expr_stmt|;
name|XContentBuilder
name|builder
init|=
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
decl_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"ok"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|builder
operator|.
name|startObject
argument_list|(
literal|"node"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"id"
argument_list|,
name|clusterService
operator|.
name|localNode
argument_list|()
operator|.
name|id
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"name"
argument_list|,
name|clusterService
operator|.
name|localNode
argument_list|()
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"transport_address"
argument_list|,
name|clusterService
operator|.
name|localNode
argument_list|()
operator|.
name|address
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|client
operator|.
name|prepareIndex
argument_list|(
name|riverIndexName
argument_list|,
name|riverName
operator|.
name|name
argument_list|()
argument_list|,
literal|"_status"
argument_list|)
operator|.
name|setConsistencyLevel
argument_list|(
name|WriteConsistencyLevel
operator|.
name|ONE
argument_list|)
operator|.
name|setSource
argument_list|(
name|builder
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"failed to create river [{}][{}]"
argument_list|,
name|e
argument_list|,
name|riverName
operator|.
name|type
argument_list|()
argument_list|,
name|riverName
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
name|XContentBuilder
name|builder
init|=
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
decl_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"error"
argument_list|,
name|ExceptionsHelper
operator|.
name|detailedMessage
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|.
name|startObject
argument_list|(
literal|"node"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"id"
argument_list|,
name|clusterService
operator|.
name|localNode
argument_list|()
operator|.
name|id
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"name"
argument_list|,
name|clusterService
operator|.
name|localNode
argument_list|()
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"transport_address"
argument_list|,
name|clusterService
operator|.
name|localNode
argument_list|()
operator|.
name|address
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|client
operator|.
name|prepareIndex
argument_list|(
name|riverIndexName
argument_list|,
name|riverName
operator|.
name|name
argument_list|()
argument_list|,
literal|"_status"
argument_list|)
operator|.
name|setConsistencyLevel
argument_list|(
name|WriteConsistencyLevel
operator|.
name|ONE
argument_list|)
operator|.
name|setSource
argument_list|(
name|builder
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e1
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"failed to write failed status for river creation"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|closeRiver
specifier|public
specifier|synchronized
name|void
name|closeRiver
parameter_list|(
name|RiverName
name|riverName
parameter_list|)
throws|throws
name|ElasticSearchException
block|{
name|Injector
name|riverInjector
decl_stmt|;
name|River
name|river
decl_stmt|;
synchronized|synchronized
init|(
name|this
init|)
block|{
name|riverInjector
operator|=
name|riversInjectors
operator|.
name|remove
argument_list|(
name|riverName
argument_list|)
expr_stmt|;
if|if
condition|(
name|riverInjector
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|RiverException
argument_list|(
name|riverName
argument_list|,
literal|"missing"
argument_list|)
throw|;
block|}
name|logger
operator|.
name|debug
argument_list|(
literal|"closing river [{}][{}]"
argument_list|,
name|riverName
operator|.
name|type
argument_list|()
argument_list|,
name|riverName
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|RiverName
argument_list|,
name|River
argument_list|>
name|tmpMap
init|=
name|Maps
operator|.
name|newHashMap
argument_list|(
name|rivers
argument_list|)
decl_stmt|;
name|river
operator|=
name|tmpMap
operator|.
name|remove
argument_list|(
name|riverName
argument_list|)
expr_stmt|;
name|rivers
operator|=
name|ImmutableMap
operator|.
name|copyOf
argument_list|(
name|tmpMap
argument_list|)
expr_stmt|;
block|}
name|river
operator|.
name|close
argument_list|()
expr_stmt|;
name|Injectors
operator|.
name|close
argument_list|(
name|injector
argument_list|)
expr_stmt|;
block|}
DECL|class|ApplyRivers
specifier|private
class|class
name|ApplyRivers
implements|implements
name|RiverClusterStateListener
block|{
annotation|@
name|Override
DECL|method|riverClusterChanged
specifier|public
name|void
name|riverClusterChanged
parameter_list|(
name|RiverClusterChangedEvent
name|event
parameter_list|)
block|{
name|DiscoveryNode
name|localNode
init|=
name|clusterService
operator|.
name|localNode
argument_list|()
decl_stmt|;
name|RiverClusterState
name|state
init|=
name|event
operator|.
name|state
argument_list|()
decl_stmt|;
comment|// first, go over and delete ones that either don't exists or are not allocated
for|for
control|(
specifier|final
name|RiverName
name|riverName
range|:
name|rivers
operator|.
name|keySet
argument_list|()
control|)
block|{
name|RiverRouting
name|routing
init|=
name|state
operator|.
name|routing
argument_list|()
operator|.
name|routing
argument_list|(
name|riverName
argument_list|)
decl_stmt|;
if|if
condition|(
name|routing
operator|==
literal|null
operator|||
operator|!
name|localNode
operator|.
name|equals
argument_list|(
name|routing
operator|.
name|node
argument_list|()
argument_list|)
condition|)
block|{
comment|// not routed at all, and not allocated here, clean it (we delete the relevant ones before)
name|closeRiver
argument_list|(
name|riverName
argument_list|)
expr_stmt|;
comment|// also, double check and delete the river content if it was deleted (_meta does not exists)
try|try
block|{
name|client
operator|.
name|prepareGet
argument_list|(
name|riverIndexName
argument_list|,
name|riverName
operator|.
name|name
argument_list|()
argument_list|,
literal|"_meta"
argument_list|)
operator|.
name|setListenerThreaded
argument_list|(
literal|true
argument_list|)
operator|.
name|execute
argument_list|(
operator|new
name|ActionListener
argument_list|<
name|GetResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onResponse
parameter_list|(
name|GetResponse
name|getResponse
parameter_list|)
block|{
if|if
condition|(
operator|!
name|getResponse
operator|.
name|exists
argument_list|()
condition|)
block|{
comment|// verify the river is deleted
name|client
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareDeleteMapping
argument_list|(
name|riverIndexName
argument_list|)
operator|.
name|setType
argument_list|(
name|riverName
operator|.
name|name
argument_list|()
argument_list|)
operator|.
name|execute
argument_list|(
operator|new
name|ActionListener
argument_list|<
name|DeleteMappingResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onResponse
parameter_list|(
name|DeleteMappingResponse
name|deleteMappingResponse
parameter_list|)
block|{
comment|// all is well...
block|}
annotation|@
name|Override
specifier|public
name|void
name|onFailure
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"failed to (double) delete river [{}] content"
argument_list|,
name|e
argument_list|,
name|riverName
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|onFailure
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"failed to (double) delete river [{}] content"
argument_list|,
name|e
argument_list|,
name|riverName
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IndexMissingException
name|e
parameter_list|)
block|{
comment|// all is well, the _river index was deleted
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"unexpected failure when trying to verify river [{}] deleted"
argument_list|,
name|e
argument_list|,
name|riverName
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
for|for
control|(
specifier|final
name|RiverRouting
name|routing
range|:
name|state
operator|.
name|routing
argument_list|()
control|)
block|{
comment|// not allocated
if|if
condition|(
name|routing
operator|.
name|node
argument_list|()
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
comment|// only apply changes to the local node
if|if
condition|(
operator|!
name|routing
operator|.
name|node
argument_list|()
operator|.
name|equals
argument_list|(
name|localNode
argument_list|)
condition|)
block|{
continue|continue;
block|}
comment|// if its already created, ignore it
if|if
condition|(
name|rivers
operator|.
name|containsKey
argument_list|(
name|routing
operator|.
name|riverName
argument_list|()
argument_list|)
condition|)
block|{
continue|continue;
block|}
name|client
operator|.
name|prepareGet
argument_list|(
name|riverIndexName
argument_list|,
name|routing
operator|.
name|riverName
argument_list|()
operator|.
name|name
argument_list|()
argument_list|,
literal|"_meta"
argument_list|)
operator|.
name|setListenerThreaded
argument_list|(
literal|true
argument_list|)
operator|.
name|execute
argument_list|(
operator|new
name|ActionListener
argument_list|<
name|GetResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onResponse
parameter_list|(
name|GetResponse
name|getResponse
parameter_list|)
block|{
if|if
condition|(
operator|!
name|rivers
operator|.
name|containsKey
argument_list|(
name|routing
operator|.
name|riverName
argument_list|()
argument_list|)
condition|)
block|{
if|if
condition|(
name|getResponse
operator|.
name|exists
argument_list|()
condition|)
block|{
comment|// only create the river if it exists, otherwise, the indexing meta data has not been visible yet...
name|createRiver
argument_list|(
name|routing
operator|.
name|riverName
argument_list|()
argument_list|,
name|getResponse
operator|.
name|sourceAsMap
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|onFailure
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
comment|// if its this is a failure that need to be retried, then do it
comment|// this might happen if the state of the river index has not been propagated yet to this node, which
comment|// should happen pretty fast since we managed to get the _meta in the RiversRouter
name|Throwable
name|failure
init|=
name|ExceptionsHelper
operator|.
name|unwrapCause
argument_list|(
name|e
argument_list|)
decl_stmt|;
if|if
condition|(
operator|(
name|failure
operator|instanceof
name|NoShardAvailableActionException
operator|)
operator|||
operator|(
name|failure
operator|instanceof
name|ClusterBlockException
operator|)
operator|||
operator|(
name|failure
operator|instanceof
name|IndexMissingException
operator|)
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"failed to get _meta from [{}]/[{}], retrying..."
argument_list|,
name|e
argument_list|,
name|routing
operator|.
name|riverName
argument_list|()
operator|.
name|type
argument_list|()
argument_list|,
name|routing
operator|.
name|riverName
argument_list|()
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
specifier|final
name|ActionListener
argument_list|<
name|GetResponse
argument_list|>
name|listener
init|=
name|this
decl_stmt|;
name|threadPool
operator|.
name|schedule
argument_list|(
name|TimeValue
operator|.
name|timeValueSeconds
argument_list|(
literal|5
argument_list|)
argument_list|,
name|ThreadPool
operator|.
name|Names
operator|.
name|SAME
argument_list|,
operator|new
name|Runnable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|client
operator|.
name|prepareGet
argument_list|(
name|riverIndexName
argument_list|,
name|routing
operator|.
name|riverName
argument_list|()
operator|.
name|name
argument_list|()
argument_list|,
literal|"_meta"
argument_list|)
operator|.
name|setListenerThreaded
argument_list|(
literal|true
argument_list|)
operator|.
name|execute
argument_list|(
name|listener
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"failed to get _meta from [{}]/[{}]"
argument_list|,
name|e
argument_list|,
name|routing
operator|.
name|riverName
argument_list|()
operator|.
name|type
argument_list|()
argument_list|,
name|routing
operator|.
name|riverName
argument_list|()
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

