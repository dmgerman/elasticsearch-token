begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.cluster.action.index
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|action
operator|.
name|index
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
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchIllegalArgumentException
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
name|ActionRequestValidationException
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
name|ActionResponse
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
name|IndicesRequest
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
name|ActionFilters
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
name|action
operator|.
name|support
operator|.
name|master
operator|.
name|MasterNodeOperationRequest
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
name|master
operator|.
name|TransportMasterNodeOperationAction
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
name|ack
operator|.
name|ClusterStateUpdateResponse
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
name|MetaDataMappingService
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
name|io
operator|.
name|stream
operator|.
name|StreamInput
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
name|io
operator|.
name|stream
operator|.
name|StreamOutput
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
name|concurrent
operator|.
name|ConcurrentCollections
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
name|ToXContent
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
name|index
operator|.
name|mapper
operator|.
name|MapperService
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
name|mapper
operator|.
name|Mapping
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
name|settings
operator|.
name|NodeSettingsService
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
name|TransportService
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
name|concurrent
operator|.
name|BlockingQueue
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|TimeUnit
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
name|TimeoutException
import|;
end_import

begin_comment
comment|/**  * Called by shards in the cluster when their mapping was dynamically updated and it needs to be updated  * in the cluster state meta data (and broadcast to all members).  */
end_comment

begin_class
DECL|class|MappingUpdatedAction
specifier|public
class|class
name|MappingUpdatedAction
extends|extends
name|TransportMasterNodeOperationAction
argument_list|<
name|MappingUpdatedAction
operator|.
name|MappingUpdatedRequest
argument_list|,
name|MappingUpdatedAction
operator|.
name|MappingUpdatedResponse
argument_list|>
block|{
DECL|field|INDICES_MAPPING_DYNAMIC_TIMEOUT
specifier|public
specifier|static
specifier|final
name|String
name|INDICES_MAPPING_DYNAMIC_TIMEOUT
init|=
literal|"indices.mapping.dynamic_timeout"
decl_stmt|;
DECL|field|ACTION_NAME
specifier|public
specifier|static
specifier|final
name|String
name|ACTION_NAME
init|=
literal|"internal:cluster/mapping_updated"
decl_stmt|;
DECL|field|metaDataMappingService
specifier|private
specifier|final
name|MetaDataMappingService
name|metaDataMappingService
decl_stmt|;
DECL|field|masterMappingUpdater
specifier|private
specifier|volatile
name|MasterMappingUpdater
name|masterMappingUpdater
decl_stmt|;
DECL|field|dynamicMappingUpdateTimeout
specifier|private
specifier|volatile
name|TimeValue
name|dynamicMappingUpdateTimeout
decl_stmt|;
DECL|class|ApplySettings
class|class
name|ApplySettings
implements|implements
name|NodeSettingsService
operator|.
name|Listener
block|{
annotation|@
name|Override
DECL|method|onRefreshSettings
specifier|public
name|void
name|onRefreshSettings
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|TimeValue
name|current
init|=
name|MappingUpdatedAction
operator|.
name|this
operator|.
name|dynamicMappingUpdateTimeout
decl_stmt|;
name|TimeValue
name|newValue
init|=
name|settings
operator|.
name|getAsTime
argument_list|(
name|INDICES_MAPPING_DYNAMIC_TIMEOUT
argument_list|,
name|current
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|current
operator|.
name|equals
argument_list|(
name|newValue
argument_list|)
condition|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"updating "
operator|+
name|INDICES_MAPPING_DYNAMIC_TIMEOUT
operator|+
literal|" from [{}] to [{}]"
argument_list|,
name|current
argument_list|,
name|newValue
argument_list|)
expr_stmt|;
name|MappingUpdatedAction
operator|.
name|this
operator|.
name|dynamicMappingUpdateTimeout
operator|=
name|newValue
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Inject
DECL|method|MappingUpdatedAction
specifier|public
name|MappingUpdatedAction
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|TransportService
name|transportService
parameter_list|,
name|ClusterService
name|clusterService
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|,
name|MetaDataMappingService
name|metaDataMappingService
parameter_list|,
name|NodeSettingsService
name|nodeSettingsService
parameter_list|,
name|ActionFilters
name|actionFilters
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|ACTION_NAME
argument_list|,
name|transportService
argument_list|,
name|clusterService
argument_list|,
name|threadPool
argument_list|,
name|actionFilters
argument_list|)
expr_stmt|;
name|this
operator|.
name|metaDataMappingService
operator|=
name|metaDataMappingService
expr_stmt|;
name|this
operator|.
name|dynamicMappingUpdateTimeout
operator|=
name|settings
operator|.
name|getAsTime
argument_list|(
name|INDICES_MAPPING_DYNAMIC_TIMEOUT
argument_list|,
name|TimeValue
operator|.
name|timeValueSeconds
argument_list|(
literal|30
argument_list|)
argument_list|)
expr_stmt|;
name|nodeSettingsService
operator|.
name|addListener
argument_list|(
operator|new
name|ApplySettings
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|start
specifier|public
name|void
name|start
parameter_list|()
block|{
name|this
operator|.
name|masterMappingUpdater
operator|=
operator|new
name|MasterMappingUpdater
argument_list|(
name|EsExecutors
operator|.
name|threadName
argument_list|(
name|settings
argument_list|,
literal|"master_mapping_updater"
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|masterMappingUpdater
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
DECL|method|stop
specifier|public
name|void
name|stop
parameter_list|()
block|{
name|this
operator|.
name|masterMappingUpdater
operator|.
name|close
argument_list|()
expr_stmt|;
name|this
operator|.
name|masterMappingUpdater
operator|=
literal|null
expr_stmt|;
block|}
DECL|method|updateMappingOnMaster
specifier|public
name|void
name|updateMappingOnMaster
parameter_list|(
name|String
name|index
parameter_list|,
name|String
name|indexUUID
parameter_list|,
name|String
name|type
parameter_list|,
name|Mapping
name|mappingUpdate
parameter_list|,
name|MappingUpdateListener
name|listener
parameter_list|)
block|{
if|if
condition|(
name|type
operator|.
name|equals
argument_list|(
name|MapperService
operator|.
name|DEFAULT_MAPPING
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|ElasticsearchIllegalArgumentException
argument_list|(
literal|"_default_ mapping should not be updated"
argument_list|)
throw|;
block|}
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
name|mappingUpdate
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
operator|new
name|ToXContent
operator|.
name|MapParams
argument_list|(
name|ImmutableMap
operator|.
expr|<
name|String
argument_list|,
name|String
operator|>
name|of
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
specifier|final
name|CompressedString
name|mappingSource
init|=
operator|new
name|CompressedString
argument_list|(
name|builder
operator|.
name|endObject
argument_list|()
operator|.
name|bytes
argument_list|()
argument_list|)
decl_stmt|;
name|masterMappingUpdater
operator|.
name|add
argument_list|(
operator|new
name|MappingChange
argument_list|(
name|index
argument_list|,
name|indexUUID
argument_list|,
name|type
argument_list|,
name|mappingSource
argument_list|,
name|listener
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|bogus
parameter_list|)
block|{
throw|throw
operator|new
name|AssertionError
argument_list|(
literal|"Cannot happen"
argument_list|,
name|bogus
argument_list|)
throw|;
block|}
block|}
comment|/**      * Same as {@link #updateMappingOnMasterSynchronously(String, String, String, Mapping, TimeValue)}      * using the default timeout.      */
DECL|method|updateMappingOnMasterSynchronously
specifier|public
name|void
name|updateMappingOnMasterSynchronously
parameter_list|(
name|String
name|index
parameter_list|,
name|String
name|indexUUID
parameter_list|,
name|String
name|type
parameter_list|,
name|Mapping
name|mappingUpdate
parameter_list|)
throws|throws
name|Throwable
block|{
name|updateMappingOnMasterSynchronously
argument_list|(
name|index
argument_list|,
name|indexUUID
argument_list|,
name|type
argument_list|,
name|mappingUpdate
argument_list|,
name|dynamicMappingUpdateTimeout
argument_list|)
expr_stmt|;
block|}
comment|/**      * Update mappings synchronously on the master node, waiting for at most      * {@code timeout}. When this method returns successfully mappings have      * been applied to the master node and propagated to data nodes.      */
DECL|method|updateMappingOnMasterSynchronously
specifier|public
name|void
name|updateMappingOnMasterSynchronously
parameter_list|(
name|String
name|index
parameter_list|,
name|String
name|indexUUID
parameter_list|,
name|String
name|type
parameter_list|,
name|Mapping
name|mappingUpdate
parameter_list|,
name|TimeValue
name|timeout
parameter_list|)
throws|throws
name|Throwable
block|{
specifier|final
name|CountDownLatch
name|latch
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
specifier|final
name|Throwable
index|[]
name|cause
init|=
operator|new
name|Throwable
index|[
literal|1
index|]
decl_stmt|;
specifier|final
name|MappingUpdateListener
name|listener
init|=
operator|new
name|MappingUpdateListener
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onMappingUpdate
parameter_list|()
block|{
name|latch
operator|.
name|countDown
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
name|t
parameter_list|)
block|{
name|cause
index|[
literal|0
index|]
operator|=
name|t
expr_stmt|;
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
block|}
decl_stmt|;
name|updateMappingOnMaster
argument_list|(
name|index
argument_list|,
name|indexUUID
argument_list|,
name|type
argument_list|,
name|mappingUpdate
argument_list|,
name|listener
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|latch
operator|.
name|await
argument_list|(
name|timeout
operator|.
name|getMillis
argument_list|()
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|TimeoutException
argument_list|(
literal|"Time out while waiting for the master node to validate a mapping update for type ["
operator|+
name|type
operator|+
literal|"]"
argument_list|)
throw|;
block|}
if|if
condition|(
name|cause
index|[
literal|0
index|]
operator|!=
literal|null
condition|)
block|{
throw|throw
name|cause
index|[
literal|0
index|]
throw|;
block|}
block|}
annotation|@
name|Override
DECL|method|checkBlock
specifier|protected
name|ClusterBlockException
name|checkBlock
parameter_list|(
name|MappingUpdatedRequest
name|request
parameter_list|,
name|ClusterState
name|state
parameter_list|)
block|{
comment|// internal call by other nodes, no need to check for blocks
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|executor
specifier|protected
name|String
name|executor
parameter_list|()
block|{
comment|// we go async right away
return|return
name|ThreadPool
operator|.
name|Names
operator|.
name|SAME
return|;
block|}
annotation|@
name|Override
DECL|method|newRequest
specifier|protected
name|MappingUpdatedRequest
name|newRequest
parameter_list|()
block|{
return|return
operator|new
name|MappingUpdatedRequest
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|newResponse
specifier|protected
name|MappingUpdatedResponse
name|newResponse
parameter_list|()
block|{
return|return
operator|new
name|MappingUpdatedResponse
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|masterOperation
specifier|protected
name|void
name|masterOperation
parameter_list|(
specifier|final
name|MappingUpdatedRequest
name|request
parameter_list|,
specifier|final
name|ClusterState
name|state
parameter_list|,
specifier|final
name|ActionListener
argument_list|<
name|MappingUpdatedResponse
argument_list|>
name|listener
parameter_list|)
throws|throws
name|ElasticsearchException
block|{
name|metaDataMappingService
operator|.
name|updateMapping
argument_list|(
name|request
operator|.
name|index
argument_list|()
argument_list|,
name|request
operator|.
name|indexUUID
argument_list|()
argument_list|,
name|request
operator|.
name|type
argument_list|()
argument_list|,
name|request
operator|.
name|mappingSource
argument_list|()
argument_list|,
name|request
operator|.
name|nodeId
argument_list|,
operator|new
name|ActionListener
argument_list|<
name|ClusterStateUpdateResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onResponse
parameter_list|(
name|ClusterStateUpdateResponse
name|response
parameter_list|)
block|{
name|listener
operator|.
name|onResponse
argument_list|(
operator|new
name|MappingUpdatedResponse
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|onFailure
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"[{}] update-mapping [{}] failed to dynamically update the mapping in cluster_state from shard"
argument_list|,
name|t
argument_list|,
name|request
operator|.
name|index
argument_list|()
argument_list|,
name|request
operator|.
name|type
argument_list|()
argument_list|)
expr_stmt|;
name|listener
operator|.
name|onFailure
argument_list|(
name|t
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
DECL|class|MappingUpdatedResponse
specifier|public
specifier|static
class|class
name|MappingUpdatedResponse
extends|extends
name|ActionResponse
block|{
annotation|@
name|Override
DECL|method|readFrom
specifier|public
name|void
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|writeTo
specifier|public
name|void
name|writeTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|MappingUpdatedRequest
specifier|public
specifier|static
class|class
name|MappingUpdatedRequest
extends|extends
name|MasterNodeOperationRequest
argument_list|<
name|MappingUpdatedRequest
argument_list|>
implements|implements
name|IndicesRequest
block|{
DECL|field|index
specifier|private
name|String
name|index
decl_stmt|;
DECL|field|indexUUID
specifier|private
name|String
name|indexUUID
init|=
name|IndexMetaData
operator|.
name|INDEX_UUID_NA_VALUE
decl_stmt|;
DECL|field|type
specifier|private
name|String
name|type
decl_stmt|;
DECL|field|mappingSource
specifier|private
name|CompressedString
name|mappingSource
decl_stmt|;
DECL|field|nodeId
specifier|private
name|String
name|nodeId
init|=
literal|null
decl_stmt|;
comment|// null means not set
DECL|method|MappingUpdatedRequest
name|MappingUpdatedRequest
parameter_list|()
block|{         }
DECL|method|MappingUpdatedRequest
specifier|public
name|MappingUpdatedRequest
parameter_list|(
name|String
name|index
parameter_list|,
name|String
name|indexUUID
parameter_list|,
name|String
name|type
parameter_list|,
name|CompressedString
name|mappingSource
parameter_list|,
name|String
name|nodeId
parameter_list|)
block|{
name|this
operator|.
name|index
operator|=
name|index
expr_stmt|;
name|this
operator|.
name|indexUUID
operator|=
name|indexUUID
expr_stmt|;
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
name|this
operator|.
name|mappingSource
operator|=
name|mappingSource
expr_stmt|;
name|this
operator|.
name|nodeId
operator|=
name|nodeId
expr_stmt|;
block|}
DECL|method|index
specifier|public
name|String
name|index
parameter_list|()
block|{
return|return
name|index
return|;
block|}
annotation|@
name|Override
DECL|method|indicesOptions
specifier|public
name|IndicesOptions
name|indicesOptions
parameter_list|()
block|{
return|return
name|IndicesOptions
operator|.
name|strictSingleIndexNoExpandForbidClosed
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|indices
specifier|public
name|String
index|[]
name|indices
parameter_list|()
block|{
return|return
operator|new
name|String
index|[]
block|{
name|index
block|}
return|;
block|}
DECL|method|indexUUID
specifier|public
name|String
name|indexUUID
parameter_list|()
block|{
return|return
name|indexUUID
return|;
block|}
DECL|method|type
specifier|public
name|String
name|type
parameter_list|()
block|{
return|return
name|type
return|;
block|}
DECL|method|mappingSource
specifier|public
name|CompressedString
name|mappingSource
parameter_list|()
block|{
return|return
name|mappingSource
return|;
block|}
comment|/**          * Returns null for not set.          */
DECL|method|nodeId
specifier|public
name|String
name|nodeId
parameter_list|()
block|{
return|return
name|this
operator|.
name|nodeId
return|;
block|}
annotation|@
name|Override
DECL|method|validate
specifier|public
name|ActionRequestValidationException
name|validate
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|readFrom
specifier|public
name|void
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|index
operator|=
name|in
operator|.
name|readString
argument_list|()
expr_stmt|;
name|type
operator|=
name|in
operator|.
name|readString
argument_list|()
expr_stmt|;
name|mappingSource
operator|=
name|CompressedString
operator|.
name|readCompressedString
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|indexUUID
operator|=
name|in
operator|.
name|readString
argument_list|()
expr_stmt|;
name|nodeId
operator|=
name|in
operator|.
name|readOptionalString
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|writeTo
specifier|public
name|void
name|writeTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeString
argument_list|(
name|index
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeString
argument_list|(
name|type
argument_list|)
expr_stmt|;
name|mappingSource
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeString
argument_list|(
name|indexUUID
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeOptionalString
argument_list|(
name|nodeId
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"index ["
operator|+
name|index
operator|+
literal|"], indexUUID ["
operator|+
name|indexUUID
operator|+
literal|"], type ["
operator|+
name|type
operator|+
literal|"] and source ["
operator|+
name|mappingSource
operator|+
literal|"]"
return|;
block|}
block|}
DECL|class|MappingChange
specifier|private
specifier|static
class|class
name|MappingChange
block|{
DECL|field|index
specifier|public
specifier|final
name|String
name|index
decl_stmt|;
DECL|field|indexUUID
specifier|public
specifier|final
name|String
name|indexUUID
decl_stmt|;
DECL|field|type
specifier|public
specifier|final
name|String
name|type
decl_stmt|;
DECL|field|mappingSource
specifier|public
specifier|final
name|CompressedString
name|mappingSource
decl_stmt|;
DECL|field|listener
specifier|public
specifier|final
name|MappingUpdateListener
name|listener
decl_stmt|;
DECL|method|MappingChange
name|MappingChange
parameter_list|(
name|String
name|index
parameter_list|,
name|String
name|indexUUID
parameter_list|,
name|String
name|type
parameter_list|,
name|CompressedString
name|mappingSource
parameter_list|,
name|MappingUpdateListener
name|listener
parameter_list|)
block|{
name|this
operator|.
name|index
operator|=
name|index
expr_stmt|;
name|this
operator|.
name|indexUUID
operator|=
name|indexUUID
expr_stmt|;
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
name|this
operator|.
name|mappingSource
operator|=
name|mappingSource
expr_stmt|;
name|this
operator|.
name|listener
operator|=
name|listener
expr_stmt|;
block|}
block|}
comment|/**      * A listener to be notified when the mappings were updated      */
DECL|interface|MappingUpdateListener
specifier|public
specifier|static
interface|interface
name|MappingUpdateListener
block|{
DECL|method|onMappingUpdate
name|void
name|onMappingUpdate
parameter_list|()
function_decl|;
DECL|method|onFailure
name|void
name|onFailure
parameter_list|(
name|Throwable
name|t
parameter_list|)
function_decl|;
block|}
comment|/**      * The master mapping updater removes the overhead of refreshing the mapping (refreshSource) on the      * indexing thread.      *<p/>      * It also allows to reduce multiple mapping updates on the same index(UUID) and type into one update      * (refreshSource + sending to master), which allows to offload the number of times mappings are updated      * and sent to master for heavy single index requests that each introduce a new mapping, and when      * multiple shards exists on the same nodes, allowing to work on the index level in this case.      */
DECL|class|MasterMappingUpdater
specifier|private
class|class
name|MasterMappingUpdater
extends|extends
name|Thread
block|{
DECL|field|running
specifier|private
specifier|volatile
name|boolean
name|running
init|=
literal|true
decl_stmt|;
DECL|field|queue
specifier|private
specifier|final
name|BlockingQueue
argument_list|<
name|MappingChange
argument_list|>
name|queue
init|=
name|ConcurrentCollections
operator|.
name|newBlockingQueue
argument_list|()
decl_stmt|;
DECL|method|MasterMappingUpdater
specifier|public
name|MasterMappingUpdater
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|)
expr_stmt|;
block|}
DECL|method|add
specifier|public
name|void
name|add
parameter_list|(
name|MappingChange
name|change
parameter_list|)
block|{
name|queue
operator|.
name|add
argument_list|(
name|change
argument_list|)
expr_stmt|;
block|}
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
block|{
name|running
operator|=
literal|false
expr_stmt|;
name|this
operator|.
name|interrupt
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|run
specifier|public
name|void
name|run
parameter_list|()
block|{
while|while
condition|(
name|running
condition|)
block|{
name|MappingUpdateListener
name|listener
init|=
literal|null
decl_stmt|;
try|try
block|{
specifier|final
name|MappingChange
name|change
init|=
name|queue
operator|.
name|poll
argument_list|(
literal|10
argument_list|,
name|TimeUnit
operator|.
name|MINUTES
argument_list|)
decl_stmt|;
if|if
condition|(
name|change
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
name|listener
operator|=
name|change
operator|.
name|listener
expr_stmt|;
specifier|final
name|MappingUpdatedAction
operator|.
name|MappingUpdatedRequest
name|mappingRequest
decl_stmt|;
try|try
block|{
name|DiscoveryNode
name|node
init|=
name|clusterService
operator|.
name|localNode
argument_list|()
decl_stmt|;
name|mappingRequest
operator|=
operator|new
name|MappingUpdatedAction
operator|.
name|MappingUpdatedRequest
argument_list|(
name|change
operator|.
name|index
argument_list|,
name|change
operator|.
name|indexUUID
argument_list|,
name|change
operator|.
name|type
argument_list|,
name|change
operator|.
name|mappingSource
argument_list|,
name|node
operator|!=
literal|null
condition|?
name|node
operator|.
name|id
argument_list|()
else|:
literal|null
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"Failed to update master on updated mapping for index ["
operator|+
name|change
operator|.
name|index
operator|+
literal|"], type ["
operator|+
name|change
operator|.
name|type
operator|+
literal|"]"
argument_list|,
name|t
argument_list|)
expr_stmt|;
if|if
condition|(
name|change
operator|.
name|listener
operator|!=
literal|null
condition|)
block|{
name|change
operator|.
name|listener
operator|.
name|onFailure
argument_list|(
name|t
argument_list|)
expr_stmt|;
block|}
continue|continue;
block|}
name|logger
operator|.
name|trace
argument_list|(
literal|"sending mapping updated to master: {}"
argument_list|,
name|mappingRequest
argument_list|)
expr_stmt|;
name|execute
argument_list|(
name|mappingRequest
argument_list|,
operator|new
name|ActionListener
argument_list|<
name|MappingUpdatedAction
operator|.
name|MappingUpdatedResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onResponse
parameter_list|(
name|MappingUpdatedAction
operator|.
name|MappingUpdatedResponse
name|mappingUpdatedResponse
parameter_list|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"successfully updated master with mapping update: {}"
argument_list|,
name|mappingRequest
argument_list|)
expr_stmt|;
if|if
condition|(
name|change
operator|.
name|listener
operator|!=
literal|null
condition|)
block|{
name|change
operator|.
name|listener
operator|.
name|onMappingUpdate
argument_list|()
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
name|warn
argument_list|(
literal|"failed to update master on updated mapping for {}"
argument_list|,
name|e
argument_list|,
name|mappingRequest
argument_list|)
expr_stmt|;
if|if
condition|(
name|change
operator|.
name|listener
operator|!=
literal|null
condition|)
block|{
name|change
operator|.
name|listener
operator|.
name|onFailure
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
if|if
condition|(
name|listener
operator|!=
literal|null
condition|)
block|{
comment|// even if the failure is expected, eg. if we got interrupted,
comment|// we need to notify the listener as there might be a latch
comment|// waiting for it to be called
name|listener
operator|.
name|onFailure
argument_list|(
name|t
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|t
operator|instanceof
name|InterruptedException
operator|&&
operator|!
name|running
condition|)
block|{
comment|// all is well, we are shutting down
block|}
else|else
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"failed to process mapping update"
argument_list|,
name|t
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
block|}
end_class

end_unit

