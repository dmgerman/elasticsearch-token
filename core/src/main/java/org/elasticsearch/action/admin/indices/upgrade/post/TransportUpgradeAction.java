begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.indices.upgrade.post
package|package
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
name|upgrade
operator|.
name|post
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|Version
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
name|PrimaryMissingActionException
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
name|ShardOperationFailedException
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
name|broadcast
operator|.
name|node
operator|.
name|TransportBroadcastByNodeAction
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
name|cluster
operator|.
name|metadata
operator|.
name|IndexNameExpressionResolver
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
name|RoutingTable
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
name|ShardsIterator
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
name|Tuple
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
name|shard
operator|.
name|IndexShard
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
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_comment
comment|/**  * Upgrade index/indices action.  */
end_comment

begin_class
DECL|class|TransportUpgradeAction
specifier|public
class|class
name|TransportUpgradeAction
extends|extends
name|TransportBroadcastByNodeAction
argument_list|<
name|UpgradeRequest
argument_list|,
name|UpgradeResponse
argument_list|,
name|ShardUpgradeResult
argument_list|>
block|{
DECL|field|indicesService
specifier|private
specifier|final
name|IndicesService
name|indicesService
decl_stmt|;
DECL|field|upgradeSettingsAction
specifier|private
specifier|final
name|TransportUpgradeSettingsAction
name|upgradeSettingsAction
decl_stmt|;
annotation|@
name|Inject
DECL|method|TransportUpgradeAction
specifier|public
name|TransportUpgradeAction
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|,
name|ClusterService
name|clusterService
parameter_list|,
name|TransportService
name|transportService
parameter_list|,
name|IndicesService
name|indicesService
parameter_list|,
name|ActionFilters
name|actionFilters
parameter_list|,
name|IndexNameExpressionResolver
name|indexNameExpressionResolver
parameter_list|,
name|TransportUpgradeSettingsAction
name|upgradeSettingsAction
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|UpgradeAction
operator|.
name|NAME
argument_list|,
name|threadPool
argument_list|,
name|clusterService
argument_list|,
name|transportService
argument_list|,
name|actionFilters
argument_list|,
name|indexNameExpressionResolver
argument_list|,
name|UpgradeRequest
operator|.
name|class
argument_list|,
name|ThreadPool
operator|.
name|Names
operator|.
name|OPTIMIZE
argument_list|)
expr_stmt|;
name|this
operator|.
name|indicesService
operator|=
name|indicesService
expr_stmt|;
name|this
operator|.
name|upgradeSettingsAction
operator|=
name|upgradeSettingsAction
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|newResponse
specifier|protected
name|UpgradeResponse
name|newResponse
parameter_list|(
name|UpgradeRequest
name|request
parameter_list|,
name|int
name|totalShards
parameter_list|,
name|int
name|successfulShards
parameter_list|,
name|int
name|failedShards
parameter_list|,
name|List
argument_list|<
name|ShardUpgradeResult
argument_list|>
name|shardUpgradeResults
parameter_list|,
name|List
argument_list|<
name|ShardOperationFailedException
argument_list|>
name|shardFailures
parameter_list|,
name|ClusterState
name|clusterState
parameter_list|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
name|successfulPrimaryShards
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Tuple
argument_list|<
name|Version
argument_list|,
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|Version
argument_list|>
argument_list|>
name|versions
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|ShardUpgradeResult
name|result
range|:
name|shardUpgradeResults
control|)
block|{
name|successfulShards
operator|++
expr_stmt|;
name|String
name|index
init|=
name|result
operator|.
name|getShardId
argument_list|()
operator|.
name|getIndex
argument_list|()
decl_stmt|;
if|if
condition|(
name|result
operator|.
name|primary
argument_list|()
condition|)
block|{
name|Integer
name|count
init|=
name|successfulPrimaryShards
operator|.
name|get
argument_list|(
name|index
argument_list|)
decl_stmt|;
name|successfulPrimaryShards
operator|.
name|put
argument_list|(
name|index
argument_list|,
name|count
operator|==
literal|null
condition|?
literal|1
else|:
name|count
operator|+
literal|1
argument_list|)
expr_stmt|;
block|}
name|Tuple
argument_list|<
name|Version
argument_list|,
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|Version
argument_list|>
name|versionTuple
init|=
name|versions
operator|.
name|get
argument_list|(
name|index
argument_list|)
decl_stmt|;
if|if
condition|(
name|versionTuple
operator|==
literal|null
condition|)
block|{
name|versions
operator|.
name|put
argument_list|(
name|index
argument_list|,
operator|new
name|Tuple
argument_list|<>
argument_list|(
name|result
operator|.
name|upgradeVersion
argument_list|()
argument_list|,
name|result
operator|.
name|oldestLuceneSegment
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// We already have versions for this index - let's see if we need to update them based on the current shard
name|Version
name|version
init|=
name|versionTuple
operator|.
name|v1
argument_list|()
decl_stmt|;
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|Version
name|luceneVersion
init|=
name|versionTuple
operator|.
name|v2
argument_list|()
decl_stmt|;
comment|// For the metadata we are interested in the _latest_ Elasticsearch version that was processing the metadata
comment|// Since we rewrite the mapping during upgrade the metadata is always rewritten by the latest version
if|if
condition|(
name|result
operator|.
name|upgradeVersion
argument_list|()
operator|.
name|after
argument_list|(
name|versionTuple
operator|.
name|v1
argument_list|()
argument_list|)
condition|)
block|{
name|version
operator|=
name|result
operator|.
name|upgradeVersion
argument_list|()
expr_stmt|;
block|}
comment|// For the lucene version we are interested in the _oldest_ lucene version since it determines the
comment|// oldest version that we need to support
if|if
condition|(
name|result
operator|.
name|oldestLuceneSegment
argument_list|()
operator|.
name|onOrAfter
argument_list|(
name|versionTuple
operator|.
name|v2
argument_list|()
argument_list|)
operator|==
literal|false
condition|)
block|{
name|luceneVersion
operator|=
name|result
operator|.
name|oldestLuceneSegment
argument_list|()
expr_stmt|;
block|}
name|versions
operator|.
name|put
argument_list|(
name|index
argument_list|,
operator|new
name|Tuple
argument_list|<>
argument_list|(
name|version
argument_list|,
name|luceneVersion
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|Map
argument_list|<
name|String
argument_list|,
name|Tuple
argument_list|<
name|org
operator|.
name|elasticsearch
operator|.
name|Version
argument_list|,
name|String
argument_list|>
argument_list|>
name|updatedVersions
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|MetaData
name|metaData
init|=
name|clusterState
operator|.
name|metaData
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Tuple
argument_list|<
name|Version
argument_list|,
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|Version
argument_list|>
argument_list|>
name|versionEntry
range|:
name|versions
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|index
init|=
name|versionEntry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|Integer
name|primaryCount
init|=
name|successfulPrimaryShards
operator|.
name|get
argument_list|(
name|index
argument_list|)
decl_stmt|;
name|int
name|expectedPrimaryCount
init|=
name|metaData
operator|.
name|index
argument_list|(
name|index
argument_list|)
operator|.
name|getNumberOfShards
argument_list|()
decl_stmt|;
if|if
condition|(
name|primaryCount
operator|==
name|metaData
operator|.
name|index
argument_list|(
name|index
argument_list|)
operator|.
name|getNumberOfShards
argument_list|()
condition|)
block|{
name|updatedVersions
operator|.
name|put
argument_list|(
name|index
argument_list|,
operator|new
name|Tuple
argument_list|<>
argument_list|(
name|versionEntry
operator|.
name|getValue
argument_list|()
operator|.
name|v1
argument_list|()
argument_list|,
name|versionEntry
operator|.
name|getValue
argument_list|()
operator|.
name|v2
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"Not updating settings for the index [{}] because upgraded of some primary shards failed - expected[{}], received[{}]"
argument_list|,
name|index
argument_list|,
name|expectedPrimaryCount
argument_list|,
name|primaryCount
operator|==
literal|null
condition|?
literal|0
else|:
name|primaryCount
argument_list|)
expr_stmt|;
block|}
block|}
return|return
operator|new
name|UpgradeResponse
argument_list|(
name|updatedVersions
argument_list|,
name|totalShards
argument_list|,
name|successfulShards
argument_list|,
name|failedShards
argument_list|,
name|shardFailures
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|shardOperation
specifier|protected
name|ShardUpgradeResult
name|shardOperation
parameter_list|(
name|UpgradeRequest
name|request
parameter_list|,
name|ShardRouting
name|shardRouting
parameter_list|)
throws|throws
name|IOException
block|{
name|IndexShard
name|indexShard
init|=
name|indicesService
operator|.
name|indexServiceSafe
argument_list|(
name|shardRouting
operator|.
name|shardId
argument_list|()
operator|.
name|getIndex
argument_list|()
argument_list|)
operator|.
name|shardSafe
argument_list|(
name|shardRouting
operator|.
name|shardId
argument_list|()
operator|.
name|id
argument_list|()
argument_list|)
decl_stmt|;
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|Version
name|oldestLuceneSegment
init|=
name|indexShard
operator|.
name|upgrade
argument_list|(
name|request
argument_list|)
decl_stmt|;
comment|// We are using the current version of Elasticsearch as upgrade version since we update mapping to match the current version
return|return
operator|new
name|ShardUpgradeResult
argument_list|(
name|shardRouting
operator|.
name|shardId
argument_list|()
argument_list|,
name|indexShard
operator|.
name|routingEntry
argument_list|()
operator|.
name|primary
argument_list|()
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|,
name|oldestLuceneSegment
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|readShardResult
specifier|protected
name|ShardUpgradeResult
name|readShardResult
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|ShardUpgradeResult
name|result
init|=
operator|new
name|ShardUpgradeResult
argument_list|()
decl_stmt|;
name|result
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
annotation|@
name|Override
DECL|method|readRequestFrom
specifier|protected
name|UpgradeRequest
name|readRequestFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|UpgradeRequest
name|request
init|=
operator|new
name|UpgradeRequest
argument_list|()
decl_stmt|;
name|request
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
return|return
name|request
return|;
block|}
comment|/**      * The upgrade request works against *all* shards.      */
annotation|@
name|Override
DECL|method|shards
specifier|protected
name|ShardsIterator
name|shards
parameter_list|(
name|ClusterState
name|clusterState
parameter_list|,
name|UpgradeRequest
name|request
parameter_list|,
name|String
index|[]
name|concreteIndices
parameter_list|)
block|{
name|ShardsIterator
name|iterator
init|=
name|clusterState
operator|.
name|routingTable
argument_list|()
operator|.
name|allShards
argument_list|(
name|concreteIndices
argument_list|)
decl_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|indicesWithMissingPrimaries
init|=
name|indicesWithMissingPrimaries
argument_list|(
name|clusterState
argument_list|,
name|concreteIndices
argument_list|)
decl_stmt|;
if|if
condition|(
name|indicesWithMissingPrimaries
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
name|iterator
return|;
block|}
comment|// If some primary shards are not available the request should fail.
throw|throw
operator|new
name|PrimaryMissingActionException
argument_list|(
literal|"Cannot upgrade indices because the following indices are missing primary shards "
operator|+
name|indicesWithMissingPrimaries
argument_list|)
throw|;
block|}
comment|/**      * Finds all indices that have not all primaries available      */
DECL|method|indicesWithMissingPrimaries
specifier|private
name|Set
argument_list|<
name|String
argument_list|>
name|indicesWithMissingPrimaries
parameter_list|(
name|ClusterState
name|clusterState
parameter_list|,
name|String
index|[]
name|concreteIndices
parameter_list|)
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|indices
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
name|RoutingTable
name|routingTable
init|=
name|clusterState
operator|.
name|routingTable
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|index
range|:
name|concreteIndices
control|)
block|{
name|IndexRoutingTable
name|indexRoutingTable
init|=
name|routingTable
operator|.
name|index
argument_list|(
name|index
argument_list|)
decl_stmt|;
if|if
condition|(
name|indexRoutingTable
operator|.
name|allPrimaryShardsActive
argument_list|()
operator|==
literal|false
condition|)
block|{
name|indices
operator|.
name|add
argument_list|(
name|index
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|indices
return|;
block|}
annotation|@
name|Override
DECL|method|checkGlobalBlock
specifier|protected
name|ClusterBlockException
name|checkGlobalBlock
parameter_list|(
name|ClusterState
name|state
parameter_list|,
name|UpgradeRequest
name|request
parameter_list|)
block|{
return|return
name|state
operator|.
name|blocks
argument_list|()
operator|.
name|globalBlockedException
argument_list|(
name|ClusterBlockLevel
operator|.
name|METADATA_WRITE
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|checkRequestBlock
specifier|protected
name|ClusterBlockException
name|checkRequestBlock
parameter_list|(
name|ClusterState
name|state
parameter_list|,
name|UpgradeRequest
name|request
parameter_list|,
name|String
index|[]
name|concreteIndices
parameter_list|)
block|{
return|return
name|state
operator|.
name|blocks
argument_list|()
operator|.
name|indicesBlockedException
argument_list|(
name|ClusterBlockLevel
operator|.
name|METADATA_WRITE
argument_list|,
name|concreteIndices
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|doExecute
specifier|protected
name|void
name|doExecute
parameter_list|(
name|UpgradeRequest
name|request
parameter_list|,
specifier|final
name|ActionListener
argument_list|<
name|UpgradeResponse
argument_list|>
name|listener
parameter_list|)
block|{
name|ActionListener
argument_list|<
name|UpgradeResponse
argument_list|>
name|settingsUpdateListener
init|=
operator|new
name|ActionListener
argument_list|<
name|UpgradeResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onResponse
parameter_list|(
name|UpgradeResponse
name|upgradeResponse
parameter_list|)
block|{
try|try
block|{
if|if
condition|(
name|upgradeResponse
operator|.
name|versions
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|listener
operator|.
name|onResponse
argument_list|(
name|upgradeResponse
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|updateSettings
argument_list|(
name|upgradeResponse
argument_list|,
name|listener
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|listener
operator|.
name|onFailure
argument_list|(
name|t
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
name|listener
operator|.
name|onFailure
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
decl_stmt|;
name|super
operator|.
name|doExecute
argument_list|(
name|request
argument_list|,
name|settingsUpdateListener
argument_list|)
expr_stmt|;
block|}
DECL|method|updateSettings
specifier|private
name|void
name|updateSettings
parameter_list|(
specifier|final
name|UpgradeResponse
name|upgradeResponse
parameter_list|,
specifier|final
name|ActionListener
argument_list|<
name|UpgradeResponse
argument_list|>
name|listener
parameter_list|)
block|{
name|UpgradeSettingsRequest
name|upgradeSettingsRequest
init|=
operator|new
name|UpgradeSettingsRequest
argument_list|(
name|upgradeResponse
operator|.
name|versions
argument_list|()
argument_list|)
decl_stmt|;
name|upgradeSettingsAction
operator|.
name|execute
argument_list|(
name|upgradeSettingsRequest
argument_list|,
operator|new
name|ActionListener
argument_list|<
name|UpgradeSettingsResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onResponse
parameter_list|(
name|UpgradeSettingsResponse
name|updateSettingsResponse
parameter_list|)
block|{
name|listener
operator|.
name|onResponse
argument_list|(
name|upgradeResponse
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
name|e
parameter_list|)
block|{
name|listener
operator|.
name|onFailure
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

