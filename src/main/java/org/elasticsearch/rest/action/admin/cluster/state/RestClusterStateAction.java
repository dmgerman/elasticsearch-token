begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.rest.action.admin.cluster.state
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|action
operator|.
name|admin
operator|.
name|cluster
operator|.
name|state
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
name|admin
operator|.
name|cluster
operator|.
name|state
operator|.
name|ClusterStateRequest
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
name|state
operator|.
name|ClusterStateResponse
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
name|ClusterState
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
name|IndexTemplateMetaData
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
name|MappingMetaData
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
name|cluster
operator|.
name|routing
operator|.
name|IndexRoutingTable
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
name|IndexShardRoutingTable
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
name|RoutingNode
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
name|ShardRouting
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
name|AllocationExplanation
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
name|Strings
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
name|compress
operator|.
name|CompressedString
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
name|settings
operator|.
name|SettingsFilter
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
name|common
operator|.
name|xcontent
operator|.
name|XContentParser
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
name|shard
operator|.
name|ShardId
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|*
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|action
operator|.
name|support
operator|.
name|RestActions
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|action
operator|.
name|support
operator|.
name|RestXContentBuilder
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
name|List
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

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|RestClusterStateAction
specifier|public
class|class
name|RestClusterStateAction
extends|extends
name|BaseRestHandler
block|{
DECL|field|settingsFilter
specifier|private
specifier|final
name|SettingsFilter
name|settingsFilter
decl_stmt|;
annotation|@
name|Inject
DECL|method|RestClusterStateAction
specifier|public
name|RestClusterStateAction
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|Client
name|client
parameter_list|,
name|RestController
name|controller
parameter_list|,
name|SettingsFilter
name|settingsFilter
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|client
argument_list|)
expr_stmt|;
name|controller
operator|.
name|registerHandler
argument_list|(
name|RestRequest
operator|.
name|Method
operator|.
name|GET
argument_list|,
literal|"/_cluster/state"
argument_list|,
name|this
argument_list|)
expr_stmt|;
name|this
operator|.
name|settingsFilter
operator|=
name|settingsFilter
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|handleRequest
specifier|public
name|void
name|handleRequest
parameter_list|(
specifier|final
name|RestRequest
name|request
parameter_list|,
specifier|final
name|RestChannel
name|channel
parameter_list|)
block|{
specifier|final
name|ClusterStateRequest
name|clusterStateRequest
init|=
name|Requests
operator|.
name|clusterStateRequest
argument_list|()
decl_stmt|;
name|clusterStateRequest
operator|.
name|listenerThreaded
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|clusterStateRequest
operator|.
name|masterNodeTimeout
argument_list|(
name|request
operator|.
name|paramAsTime
argument_list|(
literal|"master_timeout"
argument_list|,
name|clusterStateRequest
operator|.
name|masterNodeTimeout
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|clusterStateRequest
operator|.
name|filterNodes
argument_list|(
name|request
operator|.
name|paramAsBoolean
argument_list|(
literal|"filter_nodes"
argument_list|,
name|clusterStateRequest
operator|.
name|filterNodes
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|clusterStateRequest
operator|.
name|filterRoutingTable
argument_list|(
name|request
operator|.
name|paramAsBoolean
argument_list|(
literal|"filter_routing_table"
argument_list|,
name|clusterStateRequest
operator|.
name|filterRoutingTable
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|clusterStateRequest
operator|.
name|filterMetaData
argument_list|(
name|request
operator|.
name|paramAsBoolean
argument_list|(
literal|"filter_metadata"
argument_list|,
name|clusterStateRequest
operator|.
name|filterMetaData
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|clusterStateRequest
operator|.
name|filterBlocks
argument_list|(
name|request
operator|.
name|paramAsBoolean
argument_list|(
literal|"filter_blocks"
argument_list|,
name|clusterStateRequest
operator|.
name|filterBlocks
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|clusterStateRequest
operator|.
name|filteredIndices
argument_list|(
name|RestActions
operator|.
name|splitIndices
argument_list|(
name|request
operator|.
name|param
argument_list|(
literal|"filter_indices"
argument_list|,
literal|null
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|clusterStateRequest
operator|.
name|filteredIndexTemplates
argument_list|(
name|request
operator|.
name|paramAsStringArray
argument_list|(
literal|"filter_index_templates"
argument_list|,
name|Strings
operator|.
name|EMPTY_ARRAY
argument_list|)
argument_list|)
expr_stmt|;
name|clusterStateRequest
operator|.
name|local
argument_list|(
name|request
operator|.
name|paramAsBoolean
argument_list|(
literal|"local"
argument_list|,
name|clusterStateRequest
operator|.
name|local
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|client
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|state
argument_list|(
name|clusterStateRequest
argument_list|,
operator|new
name|ActionListener
argument_list|<
name|ClusterStateResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onResponse
parameter_list|(
name|ClusterStateResponse
name|response
parameter_list|)
block|{
try|try
block|{
name|ClusterState
name|state
init|=
name|response
operator|.
name|state
argument_list|()
decl_stmt|;
name|XContentBuilder
name|builder
init|=
name|RestXContentBuilder
operator|.
name|restContentBuilder
argument_list|(
name|request
argument_list|)
decl_stmt|;
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"cluster_name"
argument_list|,
name|response
operator|.
name|clusterName
argument_list|()
operator|.
name|value
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|clusterStateRequest
operator|.
name|filterNodes
argument_list|()
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"master_node"
argument_list|,
name|state
operator|.
name|nodes
argument_list|()
operator|.
name|masterNodeId
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// blocks
if|if
condition|(
operator|!
name|clusterStateRequest
operator|.
name|filterBlocks
argument_list|()
condition|)
block|{
name|builder
operator|.
name|startObject
argument_list|(
literal|"blocks"
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|state
operator|.
name|blocks
argument_list|()
operator|.
name|global
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|builder
operator|.
name|startObject
argument_list|(
literal|"global"
argument_list|)
expr_stmt|;
for|for
control|(
name|ClusterBlock
name|block
range|:
name|state
operator|.
name|blocks
argument_list|()
operator|.
name|global
argument_list|()
control|)
block|{
name|block
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|request
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|state
operator|.
name|blocks
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|builder
operator|.
name|startObject
argument_list|(
literal|"indices"
argument_list|)
expr_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|ImmutableSet
argument_list|<
name|ClusterBlock
argument_list|>
argument_list|>
name|entry
range|:
name|state
operator|.
name|blocks
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|builder
operator|.
name|startObject
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|ClusterBlock
name|block
range|:
name|entry
operator|.
name|getValue
argument_list|()
control|)
block|{
name|block
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|request
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
comment|// nodes
if|if
condition|(
operator|!
name|clusterStateRequest
operator|.
name|filterNodes
argument_list|()
condition|)
block|{
name|builder
operator|.
name|startObject
argument_list|(
literal|"nodes"
argument_list|)
expr_stmt|;
for|for
control|(
name|DiscoveryNode
name|node
range|:
name|state
operator|.
name|nodes
argument_list|()
control|)
block|{
name|builder
operator|.
name|startObject
argument_list|(
name|node
operator|.
name|id
argument_list|()
argument_list|,
name|XContentBuilder
operator|.
name|FieldCaseConversion
operator|.
name|NONE
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"name"
argument_list|,
name|node
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
name|node
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
name|startObject
argument_list|(
literal|"attributes"
argument_list|)
expr_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|attr
range|:
name|node
operator|.
name|attributes
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|attr
operator|.
name|getKey
argument_list|()
argument_list|,
name|attr
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
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
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
comment|// meta data
if|if
condition|(
operator|!
name|clusterStateRequest
operator|.
name|filterMetaData
argument_list|()
condition|)
block|{
name|builder
operator|.
name|startObject
argument_list|(
literal|"metadata"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|startObject
argument_list|(
literal|"templates"
argument_list|)
expr_stmt|;
for|for
control|(
name|IndexTemplateMetaData
name|templateMetaData
range|:
name|state
operator|.
name|metaData
argument_list|()
operator|.
name|templates
argument_list|()
operator|.
name|values
argument_list|()
control|)
block|{
name|builder
operator|.
name|startObject
argument_list|(
name|templateMetaData
operator|.
name|name
argument_list|()
argument_list|,
name|XContentBuilder
operator|.
name|FieldCaseConversion
operator|.
name|NONE
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"template"
argument_list|,
name|templateMetaData
operator|.
name|template
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"order"
argument_list|,
name|templateMetaData
operator|.
name|order
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|startObject
argument_list|(
literal|"settings"
argument_list|)
expr_stmt|;
name|Settings
name|settings
init|=
name|settingsFilter
operator|.
name|filterSettings
argument_list|(
name|templateMetaData
operator|.
name|settings
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|entry
range|:
name|settings
operator|.
name|getAsMap
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|startObject
argument_list|(
literal|"mappings"
argument_list|)
expr_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|CompressedString
argument_list|>
name|entry
range|:
name|templateMetaData
operator|.
name|mappings
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|byte
index|[]
name|mappingSource
init|=
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|uncompressed
argument_list|()
decl_stmt|;
name|XContentParser
name|parser
init|=
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|mappingSource
argument_list|)
operator|.
name|createParser
argument_list|(
name|mappingSource
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|mapping
init|=
name|parser
operator|.
name|map
argument_list|()
decl_stmt|;
if|if
condition|(
name|mapping
operator|.
name|size
argument_list|()
operator|==
literal|1
operator|&&
name|mapping
operator|.
name|containsKey
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
condition|)
block|{
comment|// the type name is the root value, reduce it
name|mapping
operator|=
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|mapping
operator|.
name|get
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|field
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|map
argument_list|(
name|mapping
argument_list|)
expr_stmt|;
block|}
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
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|startObject
argument_list|(
literal|"indices"
argument_list|)
expr_stmt|;
for|for
control|(
name|IndexMetaData
name|indexMetaData
range|:
name|state
operator|.
name|metaData
argument_list|()
control|)
block|{
name|builder
operator|.
name|startObject
argument_list|(
name|indexMetaData
operator|.
name|index
argument_list|()
argument_list|,
name|XContentBuilder
operator|.
name|FieldCaseConversion
operator|.
name|NONE
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"state"
argument_list|,
name|indexMetaData
operator|.
name|state
argument_list|()
operator|.
name|toString
argument_list|()
operator|.
name|toLowerCase
argument_list|(
name|Locale
operator|.
name|ENGLISH
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|.
name|startObject
argument_list|(
literal|"settings"
argument_list|)
expr_stmt|;
name|Settings
name|settings
init|=
name|settingsFilter
operator|.
name|filterSettings
argument_list|(
name|indexMetaData
operator|.
name|settings
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|entry
range|:
name|settings
operator|.
name|getAsMap
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|startObject
argument_list|(
literal|"mappings"
argument_list|)
expr_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|MappingMetaData
argument_list|>
name|entry
range|:
name|indexMetaData
operator|.
name|mappings
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|byte
index|[]
name|mappingSource
init|=
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|source
argument_list|()
operator|.
name|uncompressed
argument_list|()
decl_stmt|;
name|XContentParser
name|parser
init|=
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|mappingSource
argument_list|)
operator|.
name|createParser
argument_list|(
name|mappingSource
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|mapping
init|=
name|parser
operator|.
name|map
argument_list|()
decl_stmt|;
if|if
condition|(
name|mapping
operator|.
name|size
argument_list|()
operator|==
literal|1
operator|&&
name|mapping
operator|.
name|containsKey
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
condition|)
block|{
comment|// the type name is the root value, reduce it
name|mapping
operator|=
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|mapping
operator|.
name|get
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|field
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|map
argument_list|(
name|mapping
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|startArray
argument_list|(
literal|"aliases"
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|alias
range|:
name|indexMetaData
operator|.
name|aliases
argument_list|()
operator|.
name|keySet
argument_list|()
control|)
block|{
name|builder
operator|.
name|value
argument_list|(
name|alias
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endArray
argument_list|()
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
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
block|}
comment|// routing table
if|if
condition|(
operator|!
name|clusterStateRequest
operator|.
name|filterRoutingTable
argument_list|()
condition|)
block|{
name|builder
operator|.
name|startObject
argument_list|(
literal|"routing_table"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|startObject
argument_list|(
literal|"indices"
argument_list|)
expr_stmt|;
for|for
control|(
name|IndexRoutingTable
name|indexRoutingTable
range|:
name|state
operator|.
name|routingTable
argument_list|()
control|)
block|{
name|builder
operator|.
name|startObject
argument_list|(
name|indexRoutingTable
operator|.
name|index
argument_list|()
argument_list|,
name|XContentBuilder
operator|.
name|FieldCaseConversion
operator|.
name|NONE
argument_list|)
expr_stmt|;
name|builder
operator|.
name|startObject
argument_list|(
literal|"shards"
argument_list|)
expr_stmt|;
for|for
control|(
name|IndexShardRoutingTable
name|indexShardRoutingTable
range|:
name|indexRoutingTable
control|)
block|{
name|builder
operator|.
name|startArray
argument_list|(
name|Integer
operator|.
name|toString
argument_list|(
name|indexShardRoutingTable
operator|.
name|shardId
argument_list|()
operator|.
name|id
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|ShardRouting
name|shardRouting
range|:
name|indexShardRoutingTable
control|)
block|{
name|jsonShardRouting
argument_list|(
name|builder
argument_list|,
name|shardRouting
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endArray
argument_list|()
expr_stmt|;
block|}
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
block|}
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
block|}
comment|// routing nodes
if|if
condition|(
operator|!
name|clusterStateRequest
operator|.
name|filterRoutingTable
argument_list|()
condition|)
block|{
name|builder
operator|.
name|startObject
argument_list|(
literal|"routing_nodes"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|startArray
argument_list|(
literal|"unassigned"
argument_list|)
expr_stmt|;
for|for
control|(
name|ShardRouting
name|shardRouting
range|:
name|state
operator|.
name|readOnlyRoutingNodes
argument_list|()
operator|.
name|unassigned
argument_list|()
control|)
block|{
name|jsonShardRouting
argument_list|(
name|builder
argument_list|,
name|shardRouting
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endArray
argument_list|()
expr_stmt|;
name|builder
operator|.
name|startObject
argument_list|(
literal|"nodes"
argument_list|)
expr_stmt|;
for|for
control|(
name|RoutingNode
name|routingNode
range|:
name|state
operator|.
name|readOnlyRoutingNodes
argument_list|()
control|)
block|{
name|builder
operator|.
name|startArray
argument_list|(
name|routingNode
operator|.
name|nodeId
argument_list|()
argument_list|,
name|XContentBuilder
operator|.
name|FieldCaseConversion
operator|.
name|NONE
argument_list|)
expr_stmt|;
for|for
control|(
name|ShardRouting
name|shardRouting
range|:
name|routingNode
control|)
block|{
name|jsonShardRouting
argument_list|(
name|builder
argument_list|,
name|shardRouting
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endArray
argument_list|()
expr_stmt|;
block|}
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
block|}
if|if
condition|(
operator|!
name|clusterStateRequest
operator|.
name|filterRoutingTable
argument_list|()
condition|)
block|{
name|builder
operator|.
name|startArray
argument_list|(
literal|"allocations"
argument_list|)
expr_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|ShardId
argument_list|,
name|List
argument_list|<
name|AllocationExplanation
operator|.
name|NodeExplanation
argument_list|>
argument_list|>
name|entry
range|:
name|state
operator|.
name|allocationExplanation
argument_list|()
operator|.
name|explanations
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"index"
argument_list|,
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|index
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
literal|"shard"
argument_list|,
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|id
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|startArray
argument_list|(
literal|"explanations"
argument_list|)
expr_stmt|;
for|for
control|(
name|AllocationExplanation
operator|.
name|NodeExplanation
name|nodeExplanation
range|:
name|entry
operator|.
name|getValue
argument_list|()
control|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"desc"
argument_list|,
name|nodeExplanation
operator|.
name|description
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|nodeExplanation
operator|.
name|node
argument_list|()
operator|!=
literal|null
condition|)
block|{
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
name|nodeExplanation
operator|.
name|node
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
name|nodeExplanation
operator|.
name|node
argument_list|()
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
block|}
name|builder
operator|.
name|endArray
argument_list|()
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
name|builder
operator|.
name|endArray
argument_list|()
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|channel
operator|.
name|sendResponse
argument_list|(
operator|new
name|XContentRestResponse
argument_list|(
name|request
argument_list|,
name|RestStatus
operator|.
name|OK
argument_list|,
name|builder
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|onFailure
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|jsonShardRouting
parameter_list|(
name|XContentBuilder
name|builder
parameter_list|,
name|ShardRouting
name|shardRouting
parameter_list|)
throws|throws
name|IOException
block|{
name|builder
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"state"
argument_list|,
name|shardRouting
operator|.
name|state
argument_list|()
argument_list|)
operator|.
name|field
argument_list|(
literal|"primary"
argument_list|,
name|shardRouting
operator|.
name|primary
argument_list|()
argument_list|)
operator|.
name|field
argument_list|(
literal|"node"
argument_list|,
name|shardRouting
operator|.
name|currentNodeId
argument_list|()
argument_list|)
operator|.
name|field
argument_list|(
literal|"relocating_node"
argument_list|,
name|shardRouting
operator|.
name|relocatingNodeId
argument_list|()
argument_list|)
operator|.
name|field
argument_list|(
literal|"shard"
argument_list|,
name|shardRouting
operator|.
name|shardId
argument_list|()
operator|.
name|id
argument_list|()
argument_list|)
operator|.
name|field
argument_list|(
literal|"index"
argument_list|,
name|shardRouting
operator|.
name|shardId
argument_list|()
operator|.
name|index
argument_list|()
operator|.
name|name
argument_list|()
argument_list|)
operator|.
name|endObject
argument_list|()
expr_stmt|;
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
if|if
condition|(
name|logger
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"failed to handle cluster state"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|channel
operator|.
name|sendResponse
argument_list|(
operator|new
name|XContentThrowableRestResponse
argument_list|(
name|request
argument_list|,
name|e
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e1
parameter_list|)
block|{
name|logger
operator|.
name|error
argument_list|(
literal|"Failed to send failure response"
argument_list|,
name|e1
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

