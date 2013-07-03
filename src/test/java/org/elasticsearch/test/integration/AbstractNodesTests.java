begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.test.integration
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|integration
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
name|ImmutableSet
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
name|block
operator|.
name|ClusterBlock
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
name|ClusterBlockLevel
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
name|NetworkUtils
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
name|node
operator|.
name|Node
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Maps
operator|.
name|newHashMap
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
name|Builder
operator|.
name|EMPTY_SETTINGS
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
name|node
operator|.
name|NodeBuilder
operator|.
name|nodeBuilder
import|;
end_import

begin_class
DECL|class|AbstractNodesTests
specifier|public
specifier|abstract
class|class
name|AbstractNodesTests
extends|extends
name|ElasticsearchTestCase
block|{
DECL|field|nodes
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|Node
argument_list|>
name|nodes
init|=
name|newHashMap
argument_list|()
decl_stmt|;
DECL|field|clients
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|Client
argument_list|>
name|clients
init|=
name|newHashMap
argument_list|()
decl_stmt|;
DECL|field|defaultSettings
specifier|private
name|Settings
name|defaultSettings
init|=
name|ImmutableSettings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"cluster.name"
argument_list|,
literal|"test-cluster-"
operator|+
name|NetworkUtils
operator|.
name|getLocalAddress
argument_list|()
operator|.
name|getHostName
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
DECL|method|putDefaultSettings
specifier|public
name|void
name|putDefaultSettings
parameter_list|(
name|Settings
operator|.
name|Builder
name|settings
parameter_list|)
block|{
name|putDefaultSettings
argument_list|(
name|settings
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|putDefaultSettings
specifier|public
name|void
name|putDefaultSettings
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|defaultSettings
operator|=
name|ImmutableSettings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
name|defaultSettings
argument_list|)
operator|.
name|put
argument_list|(
name|settings
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
block|}
DECL|method|startNode
specifier|public
name|Node
name|startNode
parameter_list|(
name|String
name|id
parameter_list|)
block|{
return|return
name|buildNode
argument_list|(
name|id
argument_list|)
operator|.
name|start
argument_list|()
return|;
block|}
DECL|method|startNode
specifier|public
name|Node
name|startNode
parameter_list|(
name|String
name|id
parameter_list|,
name|Settings
operator|.
name|Builder
name|settings
parameter_list|)
block|{
return|return
name|startNode
argument_list|(
name|id
argument_list|,
name|settings
operator|.
name|build
argument_list|()
argument_list|)
return|;
block|}
DECL|method|startNode
specifier|public
name|Node
name|startNode
parameter_list|(
name|String
name|id
parameter_list|,
name|Settings
name|settings
parameter_list|)
block|{
return|return
name|buildNode
argument_list|(
name|id
argument_list|,
name|settings
argument_list|)
operator|.
name|start
argument_list|()
return|;
block|}
DECL|method|buildNode
specifier|public
name|Node
name|buildNode
parameter_list|(
name|String
name|id
parameter_list|)
block|{
return|return
name|buildNode
argument_list|(
name|id
argument_list|,
name|EMPTY_SETTINGS
argument_list|)
return|;
block|}
DECL|method|buildNode
specifier|public
name|Node
name|buildNode
parameter_list|(
name|String
name|id
parameter_list|,
name|Settings
operator|.
name|Builder
name|settings
parameter_list|)
block|{
return|return
name|buildNode
argument_list|(
name|id
argument_list|,
name|settings
operator|.
name|build
argument_list|()
argument_list|)
return|;
block|}
DECL|method|buildNode
specifier|public
name|Node
name|buildNode
parameter_list|(
name|String
name|id
parameter_list|,
name|Settings
name|settings
parameter_list|)
block|{
name|String
name|settingsSource
init|=
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
operator|.
name|replace
argument_list|(
literal|'.'
argument_list|,
literal|'/'
argument_list|)
operator|+
literal|".yml"
decl_stmt|;
name|Settings
name|finalSettings
init|=
name|settingsBuilder
argument_list|()
operator|.
name|loadFromClasspath
argument_list|(
name|settingsSource
argument_list|)
operator|.
name|put
argument_list|(
name|defaultSettings
argument_list|)
operator|.
name|put
argument_list|(
name|settings
argument_list|)
operator|.
name|put
argument_list|(
literal|"name"
argument_list|,
name|id
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
if|if
condition|(
name|finalSettings
operator|.
name|get
argument_list|(
literal|"gateway.type"
argument_list|)
operator|==
literal|null
condition|)
block|{
comment|// default to non gateway
name|finalSettings
operator|=
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
name|finalSettings
argument_list|)
operator|.
name|put
argument_list|(
literal|"gateway.type"
argument_list|,
literal|"none"
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|finalSettings
operator|.
name|get
argument_list|(
literal|"cluster.routing.schedule"
argument_list|)
operator|!=
literal|null
condition|)
block|{
comment|// decrease the routing schedule so new nodes will be added quickly
name|finalSettings
operator|=
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
name|finalSettings
argument_list|)
operator|.
name|put
argument_list|(
literal|"cluster.routing.schedule"
argument_list|,
literal|"50ms"
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
block|}
name|Node
name|node
init|=
name|nodeBuilder
argument_list|()
operator|.
name|settings
argument_list|(
name|finalSettings
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|nodes
operator|.
name|put
argument_list|(
name|id
argument_list|,
name|node
argument_list|)
expr_stmt|;
name|clients
operator|.
name|put
argument_list|(
name|id
argument_list|,
name|node
operator|.
name|client
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|node
return|;
block|}
DECL|method|closeNode
specifier|public
name|void
name|closeNode
parameter_list|(
name|String
name|id
parameter_list|)
block|{
name|Client
name|client
init|=
name|clients
operator|.
name|remove
argument_list|(
name|id
argument_list|)
decl_stmt|;
if|if
condition|(
name|client
operator|!=
literal|null
condition|)
block|{
name|client
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|Node
name|node
init|=
name|nodes
operator|.
name|remove
argument_list|(
name|id
argument_list|)
decl_stmt|;
if|if
condition|(
name|node
operator|!=
literal|null
condition|)
block|{
name|node
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
DECL|method|node
specifier|public
name|Node
name|node
parameter_list|(
name|String
name|id
parameter_list|)
block|{
return|return
name|nodes
operator|.
name|get
argument_list|(
name|id
argument_list|)
return|;
block|}
DECL|method|client
specifier|public
name|Client
name|client
parameter_list|(
name|String
name|id
parameter_list|)
block|{
return|return
name|clients
operator|.
name|get
argument_list|(
name|id
argument_list|)
return|;
block|}
DECL|method|closeAllNodes
specifier|public
name|void
name|closeAllNodes
parameter_list|()
block|{
for|for
control|(
name|Client
name|client
range|:
name|clients
operator|.
name|values
argument_list|()
control|)
block|{
name|client
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|clients
operator|.
name|clear
argument_list|()
expr_stmt|;
for|for
control|(
name|Node
name|node
range|:
name|nodes
operator|.
name|values
argument_list|()
control|)
block|{
name|node
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|nodes
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
DECL|method|waitForNoBlocks
specifier|public
name|ImmutableSet
argument_list|<
name|ClusterBlock
argument_list|>
name|waitForNoBlocks
parameter_list|(
name|TimeValue
name|timeout
parameter_list|,
name|String
name|node
parameter_list|)
throws|throws
name|InterruptedException
block|{
name|long
name|start
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|ImmutableSet
argument_list|<
name|ClusterBlock
argument_list|>
name|blocks
decl_stmt|;
do|do
block|{
name|blocks
operator|=
name|client
argument_list|(
name|node
argument_list|)
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
name|setLocal
argument_list|(
literal|true
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
operator|.
name|getState
argument_list|()
operator|.
name|blocks
argument_list|()
operator|.
name|global
argument_list|(
name|ClusterBlockLevel
operator|.
name|METADATA
argument_list|)
expr_stmt|;
block|}
do|while
condition|(
operator|!
name|blocks
operator|.
name|isEmpty
argument_list|()
operator|&&
operator|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|start
operator|)
operator|<
name|timeout
operator|.
name|millis
argument_list|()
condition|)
do|;
return|return
name|blocks
return|;
block|}
DECL|method|createIndices
specifier|public
name|void
name|createIndices
parameter_list|(
name|Client
name|client
parameter_list|,
name|String
modifier|...
name|indices
parameter_list|)
block|{
for|for
control|(
name|String
name|index
range|:
name|indices
control|)
block|{
name|client
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
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
block|}
block|}
DECL|method|wipeIndices
specifier|public
name|void
name|wipeIndices
parameter_list|(
name|Client
name|client
parameter_list|,
name|String
modifier|...
name|names
parameter_list|)
block|{
try|try
block|{
name|client
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareDelete
argument_list|(
name|names
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
name|IndexMissingException
name|e
parameter_list|)
block|{
comment|// ignore
block|}
block|}
block|}
end_class

end_unit

