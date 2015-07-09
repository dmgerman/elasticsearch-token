begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.cluster.metadata
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|metadata
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
name|cluster
operator|.
name|routing
operator|.
name|DjbHashFunction
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
name|HashFunction
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
name|SimpleHashFunction
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
name|UnassignedInfo
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
name|AbstractComponent
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
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_comment
comment|/**  * This service is responsible for upgrading legacy index metadata to the current version  *<p/>  * Every time an existing index is introduced into cluster this service should be used  * to upgrade the existing index metadata to the latest version of the cluster. It typically  * occurs during cluster upgrade, when dangling indices are imported into the cluster or indices  * are restored from a repository.  */
end_comment

begin_class
DECL|class|MetaDataIndexUpgradeService
specifier|public
class|class
name|MetaDataIndexUpgradeService
extends|extends
name|AbstractComponent
block|{
DECL|field|DEPRECATED_SETTING_ROUTING_HASH_FUNCTION
specifier|private
specifier|static
specifier|final
name|String
name|DEPRECATED_SETTING_ROUTING_HASH_FUNCTION
init|=
literal|"cluster.routing.operation.hash.type"
decl_stmt|;
DECL|field|DEPRECATED_SETTING_ROUTING_USE_TYPE
specifier|private
specifier|static
specifier|final
name|String
name|DEPRECATED_SETTING_ROUTING_USE_TYPE
init|=
literal|"cluster.routing.operation.use_type"
decl_stmt|;
DECL|field|pre20HashFunction
specifier|private
specifier|final
name|Class
argument_list|<
name|?
extends|extends
name|HashFunction
argument_list|>
name|pre20HashFunction
decl_stmt|;
DECL|field|pre20UseType
specifier|private
specifier|final
name|Boolean
name|pre20UseType
decl_stmt|;
annotation|@
name|Inject
DECL|method|MetaDataIndexUpgradeService
specifier|public
name|MetaDataIndexUpgradeService
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
specifier|final
name|String
name|pre20HashFunctionName
init|=
name|settings
operator|.
name|get
argument_list|(
name|DEPRECATED_SETTING_ROUTING_HASH_FUNCTION
argument_list|,
literal|null
argument_list|)
decl_stmt|;
specifier|final
name|boolean
name|hasCustomPre20HashFunction
init|=
name|pre20HashFunctionName
operator|!=
literal|null
decl_stmt|;
comment|// the hash function package has changed we replace the two hash functions if their fully qualified name is used.
if|if
condition|(
name|hasCustomPre20HashFunction
condition|)
block|{
switch|switch
condition|(
name|pre20HashFunctionName
condition|)
block|{
case|case
literal|"org.elasticsearch.cluster.routing.operation.hash.simple.SimpleHashFunction"
case|:
name|pre20HashFunction
operator|=
name|SimpleHashFunction
operator|.
name|class
expr_stmt|;
break|break;
case|case
literal|"org.elasticsearch.cluster.routing.operation.hash.djb.DjbHashFunction"
case|:
name|pre20HashFunction
operator|=
name|DjbHashFunction
operator|.
name|class
expr_stmt|;
break|break;
default|default:
name|pre20HashFunction
operator|=
name|settings
operator|.
name|getAsClass
argument_list|(
name|DEPRECATED_SETTING_ROUTING_HASH_FUNCTION
argument_list|,
name|DjbHashFunction
operator|.
name|class
argument_list|,
literal|"org.elasticsearch.cluster.routing."
argument_list|,
literal|"HashFunction"
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|pre20HashFunction
operator|=
name|DjbHashFunction
operator|.
name|class
expr_stmt|;
block|}
name|pre20UseType
operator|=
name|settings
operator|.
name|getAsBoolean
argument_list|(
name|DEPRECATED_SETTING_ROUTING_USE_TYPE
argument_list|,
literal|null
argument_list|)
expr_stmt|;
if|if
condition|(
name|hasCustomPre20HashFunction
operator|||
name|pre20UseType
operator|!=
literal|null
condition|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"Settings [{}] and [{}] are deprecated. Index settings from your old indices have been updated to record the fact that they "
operator|+
literal|"used some custom routing logic, you can now remove these settings from your `elasticsearch.yml` file"
argument_list|,
name|DEPRECATED_SETTING_ROUTING_HASH_FUNCTION
argument_list|,
name|DEPRECATED_SETTING_ROUTING_USE_TYPE
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      * Checks that the index can be upgraded to the current version of the master node.      *      *<p/>      * If the index does not need upgrade it returns the index metadata unchanged, otherwise it returns a modified index metadata. If index      * cannot be updated the method throws an exception.      */
DECL|method|upgradeIndexMetaData
specifier|public
name|IndexMetaData
name|upgradeIndexMetaData
parameter_list|(
name|IndexMetaData
name|indexMetaData
parameter_list|)
throws|throws
name|Exception
block|{
comment|// Throws an exception if there are too-old segments:
name|checkSupportedVersion
argument_list|(
name|indexMetaData
argument_list|)
expr_stmt|;
name|IndexMetaData
name|newMetaData
init|=
name|upgradeLegacyRoutingSettings
argument_list|(
name|indexMetaData
argument_list|)
decl_stmt|;
name|newMetaData
operator|=
name|addDefaultUnitsIfNeeded
argument_list|(
name|newMetaData
argument_list|)
expr_stmt|;
return|return
name|newMetaData
return|;
block|}
comment|/**      * Elasticsearch 2.0 no longer supports indices with pre Lucene v4.0 (Elasticsearch v 0.90.0) segments. All indices      * that were created before Elasticsearch v0.90.0 should be upgraded using upgrade plugin before they can      * be open by this version of elasticsearch.      */
DECL|method|checkSupportedVersion
specifier|private
name|void
name|checkSupportedVersion
parameter_list|(
name|IndexMetaData
name|indexMetaData
parameter_list|)
throws|throws
name|Exception
block|{
if|if
condition|(
name|indexMetaData
operator|.
name|getState
argument_list|()
operator|==
name|IndexMetaData
operator|.
name|State
operator|.
name|OPEN
operator|&&
name|isSupportedVersion
argument_list|(
name|indexMetaData
argument_list|)
operator|==
literal|false
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"The index ["
operator|+
name|indexMetaData
operator|.
name|getIndex
argument_list|()
operator|+
literal|"] was created before v0.90.0 and wasn't upgraded."
operator|+
literal|" This index should be open using a version before "
operator|+
name|Version
operator|.
name|CURRENT
operator|.
name|minimumCompatibilityVersion
argument_list|()
operator|+
literal|" and upgraded using the upgrade API."
argument_list|)
throw|;
block|}
block|}
comment|/*      * Returns true if this index can be supported by the current version of elasticsearch      */
DECL|method|isSupportedVersion
specifier|private
specifier|static
name|boolean
name|isSupportedVersion
parameter_list|(
name|IndexMetaData
name|indexMetaData
parameter_list|)
block|{
if|if
condition|(
name|indexMetaData
operator|.
name|creationVersion
argument_list|()
operator|.
name|onOrAfter
argument_list|(
name|Version
operator|.
name|V_0_90_0_Beta1
argument_list|)
condition|)
block|{
comment|// The index was created with elasticsearch that was using Lucene 4.0
return|return
literal|true
return|;
block|}
if|if
condition|(
name|indexMetaData
operator|.
name|getMinimumCompatibleVersion
argument_list|()
operator|!=
literal|null
operator|&&
name|indexMetaData
operator|.
name|getMinimumCompatibleVersion
argument_list|()
operator|.
name|onOrAfter
argument_list|(
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|Version
operator|.
name|LUCENE_4_0_0
argument_list|)
condition|)
block|{
comment|//The index was upgraded we can work with it
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
comment|/**      * Elasticsearch 2.0 deprecated custom routing hash functions. So what we do here is that for old indices, we      * move this old and deprecated node setting to an index setting so that we can keep things backward compatible.      */
DECL|method|upgradeLegacyRoutingSettings
specifier|private
name|IndexMetaData
name|upgradeLegacyRoutingSettings
parameter_list|(
name|IndexMetaData
name|indexMetaData
parameter_list|)
throws|throws
name|Exception
block|{
if|if
condition|(
name|indexMetaData
operator|.
name|settings
argument_list|()
operator|.
name|get
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_LEGACY_ROUTING_HASH_FUNCTION
argument_list|)
operator|==
literal|null
operator|&&
name|indexMetaData
operator|.
name|getCreationVersion
argument_list|()
operator|.
name|before
argument_list|(
name|Version
operator|.
name|V_2_0_0_beta1
argument_list|)
condition|)
block|{
comment|// these settings need an upgrade
name|Settings
name|indexSettings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|indexMetaData
operator|.
name|settings
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_LEGACY_ROUTING_HASH_FUNCTION
argument_list|,
name|pre20HashFunction
argument_list|)
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_LEGACY_ROUTING_USE_TYPE
argument_list|,
name|pre20UseType
operator|==
literal|null
condition|?
literal|false
else|:
name|pre20UseType
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
return|return
name|IndexMetaData
operator|.
name|builder
argument_list|(
name|indexMetaData
argument_list|)
operator|.
name|version
argument_list|(
name|indexMetaData
operator|.
name|version
argument_list|()
argument_list|)
operator|.
name|settings
argument_list|(
name|indexSettings
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
elseif|else
if|if
condition|(
name|indexMetaData
operator|.
name|getCreationVersion
argument_list|()
operator|.
name|onOrAfter
argument_list|(
name|Version
operator|.
name|V_2_0_0_beta1
argument_list|)
condition|)
block|{
if|if
condition|(
name|indexMetaData
operator|.
name|getSettings
argument_list|()
operator|.
name|get
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_LEGACY_ROUTING_HASH_FUNCTION
argument_list|)
operator|!=
literal|null
operator|||
name|indexMetaData
operator|.
name|getSettings
argument_list|()
operator|.
name|get
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_LEGACY_ROUTING_USE_TYPE
argument_list|)
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Indices created on or after 2.0 should NOT contain ["
operator|+
name|IndexMetaData
operator|.
name|SETTING_LEGACY_ROUTING_HASH_FUNCTION
operator|+
literal|"] + or ["
operator|+
name|IndexMetaData
operator|.
name|SETTING_LEGACY_ROUTING_USE_TYPE
operator|+
literal|"] in their index settings"
argument_list|)
throw|;
block|}
block|}
return|return
name|indexMetaData
return|;
block|}
comment|/** All known byte-sized settings for an index. */
DECL|field|INDEX_BYTES_SIZE_SETTINGS
specifier|public
specifier|static
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|INDEX_BYTES_SIZE_SETTINGS
init|=
name|ImmutableSet
operator|.
name|of
argument_list|(
literal|"index.buffer_size"
argument_list|,
literal|"index.merge.policy.floor_segment"
argument_list|,
literal|"index.merge.policy.max_merged_segment"
argument_list|,
literal|"index.merge.policy.max_merge_size"
argument_list|,
literal|"index.merge.policy.min_merge_size"
argument_list|,
literal|"index.shard.recovery.file_chunk_size"
argument_list|,
literal|"index.shard.recovery.translog_size"
argument_list|,
literal|"index.store.throttle.max_bytes_per_sec"
argument_list|,
literal|"index.translog.flush_threshold_size"
argument_list|,
literal|"index.translog.fs.buffer_size"
argument_list|,
literal|"index.version_map_size"
argument_list|)
decl_stmt|;
comment|/** All known time settings for an index. */
DECL|field|INDEX_TIME_SETTINGS
specifier|public
specifier|static
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|INDEX_TIME_SETTINGS
init|=
name|ImmutableSet
operator|.
name|of
argument_list|(
literal|"index.gateway.wait_for_mapping_update_post_recovery"
argument_list|,
literal|"index.shard.wait_for_mapping_update_post_recovery"
argument_list|,
literal|"index.gc_deletes"
argument_list|,
literal|"index.indexing.slowlog.threshold.index.debug"
argument_list|,
literal|"index.indexing.slowlog.threshold.index.info"
argument_list|,
literal|"index.indexing.slowlog.threshold.index.trace"
argument_list|,
literal|"index.indexing.slowlog.threshold.index.warn"
argument_list|,
literal|"index.refresh_interval"
argument_list|,
literal|"index.search.slowlog.threshold.fetch.debug"
argument_list|,
literal|"index.search.slowlog.threshold.fetch.info"
argument_list|,
literal|"index.search.slowlog.threshold.fetch.trace"
argument_list|,
literal|"index.search.slowlog.threshold.fetch.warn"
argument_list|,
literal|"index.search.slowlog.threshold.query.debug"
argument_list|,
literal|"index.search.slowlog.threshold.query.info"
argument_list|,
literal|"index.search.slowlog.threshold.query.trace"
argument_list|,
literal|"index.search.slowlog.threshold.query.warn"
argument_list|,
literal|"index.shadow.wait_for_initial_commit"
argument_list|,
literal|"index.store.stats_refresh_interval"
argument_list|,
literal|"index.translog.flush_threshold_period"
argument_list|,
literal|"index.translog.interval"
argument_list|,
literal|"index.translog.sync_interval"
argument_list|,
name|UnassignedInfo
operator|.
name|INDEX_DELAYED_NODE_LEFT_TIMEOUT_SETTING
argument_list|)
decl_stmt|;
comment|/**      * Elasticsearch 2.0 requires units on byte/memory and time settings; this method adds the default unit to any such settings that are      * missing units.      */
DECL|method|addDefaultUnitsIfNeeded
specifier|private
name|IndexMetaData
name|addDefaultUnitsIfNeeded
parameter_list|(
name|IndexMetaData
name|indexMetaData
parameter_list|)
block|{
if|if
condition|(
name|indexMetaData
operator|.
name|getCreationVersion
argument_list|()
operator|.
name|before
argument_list|(
name|Version
operator|.
name|V_2_0_0_beta1
argument_list|)
condition|)
block|{
comment|// TODO: can we somehow only do this *once* for a pre-2.0 index?  Maybe we could stuff a "fake marker setting" here?  Seems hackish...
comment|// Created lazily if we find any settings that are missing units:
name|Settings
name|settings
init|=
name|indexMetaData
operator|.
name|settings
argument_list|()
decl_stmt|;
name|Settings
operator|.
name|Builder
name|newSettings
init|=
literal|null
decl_stmt|;
for|for
control|(
name|String
name|byteSizeSetting
range|:
name|INDEX_BYTES_SIZE_SETTINGS
control|)
block|{
name|String
name|value
init|=
name|settings
operator|.
name|get
argument_list|(
name|byteSizeSetting
argument_list|)
decl_stmt|;
if|if
condition|(
name|value
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|Long
operator|.
name|parseLong
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|nfe
parameter_list|)
block|{
continue|continue;
block|}
comment|// It's a naked number that previously would be interpreted as default unit (bytes); now we add it:
name|logger
operator|.
name|warn
argument_list|(
literal|"byte-sized index setting [{}] with value [{}] is missing units; assuming default units (b) but in future versions this will be a hard error"
argument_list|,
name|byteSizeSetting
argument_list|,
name|value
argument_list|)
expr_stmt|;
if|if
condition|(
name|newSettings
operator|==
literal|null
condition|)
block|{
name|newSettings
operator|=
name|Settings
operator|.
name|builder
argument_list|()
expr_stmt|;
name|newSettings
operator|.
name|put
argument_list|(
name|settings
argument_list|)
expr_stmt|;
block|}
name|newSettings
operator|.
name|put
argument_list|(
name|byteSizeSetting
argument_list|,
name|value
operator|+
literal|"b"
argument_list|)
expr_stmt|;
block|}
block|}
for|for
control|(
name|String
name|timeSetting
range|:
name|INDEX_TIME_SETTINGS
control|)
block|{
name|String
name|value
init|=
name|settings
operator|.
name|get
argument_list|(
name|timeSetting
argument_list|)
decl_stmt|;
if|if
condition|(
name|value
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|Long
operator|.
name|parseLong
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|nfe
parameter_list|)
block|{
continue|continue;
block|}
comment|// It's a naked number that previously would be interpreted as default unit (ms); now we add it:
name|logger
operator|.
name|warn
argument_list|(
literal|"time index setting [{}] with value [{}] is missing units; assuming default units (ms) but in future versions this will be a hard error"
argument_list|,
name|timeSetting
argument_list|,
name|value
argument_list|)
expr_stmt|;
if|if
condition|(
name|newSettings
operator|==
literal|null
condition|)
block|{
name|newSettings
operator|=
name|Settings
operator|.
name|builder
argument_list|()
expr_stmt|;
name|newSettings
operator|.
name|put
argument_list|(
name|settings
argument_list|)
expr_stmt|;
block|}
name|newSettings
operator|.
name|put
argument_list|(
name|timeSetting
argument_list|,
name|value
operator|+
literal|"ms"
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|newSettings
operator|!=
literal|null
condition|)
block|{
comment|// At least one setting was changed:
return|return
name|IndexMetaData
operator|.
name|builder
argument_list|(
name|indexMetaData
argument_list|)
operator|.
name|version
argument_list|(
name|indexMetaData
operator|.
name|version
argument_list|()
argument_list|)
operator|.
name|settings
argument_list|(
name|newSettings
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
block|}
comment|// No changes:
return|return
name|indexMetaData
return|;
block|}
block|}
end_class

end_unit

