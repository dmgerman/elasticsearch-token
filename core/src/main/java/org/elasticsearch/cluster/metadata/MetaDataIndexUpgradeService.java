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
name|com
operator|.
name|carrotsearch
operator|.
name|hppc
operator|.
name|cursors
operator|.
name|ObjectCursor
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
name|analysis
operator|.
name|Analyzer
import|;
end_import

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
name|IndexSettings
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
name|analysis
operator|.
name|AnalysisService
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
name|analysis
operator|.
name|NamedAnalyzer
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
name|similarity
operator|.
name|SimilarityService
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
name|java
operator|.
name|util
operator|.
name|Collections
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

begin_import
import|import static
name|java
operator|.
name|util
operator|.
name|Collections
operator|.
name|unmodifiableSet
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
name|util
operator|.
name|set
operator|.
name|Sets
operator|.
name|newHashSet
import|;
end_import

begin_comment
comment|/**  * This service is responsible for upgrading legacy index metadata to the current version  *<p>  * Every time an existing index is introduced into cluster this service should be used  * to upgrade the existing index metadata to the latest version of the cluster. It typically  * occurs during cluster upgrade, when dangling indices are imported into the cluster or indices  * are restored from a repository.  */
end_comment

begin_class
DECL|class|MetaDataIndexUpgradeService
specifier|public
class|class
name|MetaDataIndexUpgradeService
extends|extends
name|AbstractComponent
block|{
DECL|field|scriptService
specifier|private
specifier|final
name|ScriptService
name|scriptService
decl_stmt|;
annotation|@
name|Inject
DECL|method|MetaDataIndexUpgradeService
specifier|public
name|MetaDataIndexUpgradeService
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ScriptService
name|scriptService
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|scriptService
operator|=
name|scriptService
expr_stmt|;
block|}
comment|/**      * Checks that the index can be upgraded to the current version of the master node.      *      *<p>      * If the index does not need upgrade it returns the index metadata unchanged, otherwise it returns a modified index metadata. If index      * cannot be updated the method throws an exception.      */
DECL|method|upgradeIndexMetaData
specifier|public
name|IndexMetaData
name|upgradeIndexMetaData
parameter_list|(
name|IndexMetaData
name|indexMetaData
parameter_list|)
block|{
comment|// Throws an exception if there are too-old segments:
if|if
condition|(
name|isUpgraded
argument_list|(
name|indexMetaData
argument_list|)
condition|)
block|{
return|return
name|indexMetaData
return|;
block|}
name|checkSupportedVersion
argument_list|(
name|indexMetaData
argument_list|)
expr_stmt|;
name|IndexMetaData
name|newMetaData
init|=
name|indexMetaData
decl_stmt|;
name|newMetaData
operator|=
name|addDefaultUnitsIfNeeded
argument_list|(
name|newMetaData
argument_list|)
expr_stmt|;
name|checkMappingsCompatibility
argument_list|(
name|newMetaData
argument_list|)
expr_stmt|;
name|newMetaData
operator|=
name|markAsUpgraded
argument_list|(
name|newMetaData
argument_list|)
expr_stmt|;
return|return
name|newMetaData
return|;
block|}
comment|/**      * Checks if the index was already opened by this version of Elasticsearch and doesn't require any additional checks.      */
DECL|method|isUpgraded
specifier|private
name|boolean
name|isUpgraded
parameter_list|(
name|IndexMetaData
name|indexMetaData
parameter_list|)
block|{
return|return
name|indexMetaData
operator|.
name|getUpgradedVersion
argument_list|()
operator|.
name|onOrAfter
argument_list|(
name|Version
operator|.
name|V_3_0_0
argument_list|)
return|;
block|}
comment|/**      * Elasticsearch 3.0 no longer supports indices with pre Lucene v5.0 (Elasticsearch v2.0.0.beta1) segments. All indices      * that were created before Elasticsearch v2.0.0.beta1 should be upgraded using upgrade API before they can      * be open by this version of elasticsearch.      */
DECL|method|checkSupportedVersion
specifier|private
name|void
name|checkSupportedVersion
parameter_list|(
name|IndexMetaData
name|indexMetaData
parameter_list|)
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
literal|"] was created before v2.0.0.beta1 and wasn't upgraded."
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
comment|// The index was created with elasticsearch that was using Lucene 5.2.1
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
name|LUCENE_5_0_0
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
name|unmodifiableSet
argument_list|(
name|newHashSet
argument_list|(
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
name|unmodifiableSet
argument_list|(
name|newHashSet
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
name|getSettings
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
name|getVersion
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
comment|/**      * Checks the mappings for compatibility with the current version      */
DECL|method|checkMappingsCompatibility
specifier|private
name|void
name|checkMappingsCompatibility
parameter_list|(
name|IndexMetaData
name|indexMetaData
parameter_list|)
block|{
try|try
block|{
comment|// We cannot instantiate real analysis server at this point because the node might not have
comment|// been started yet. However, we don't really need real analyzers at this stage - so we can fake it
name|IndexSettings
name|indexSettings
init|=
operator|new
name|IndexSettings
argument_list|(
name|indexMetaData
argument_list|,
name|this
operator|.
name|settings
argument_list|,
name|Collections
operator|.
name|EMPTY_LIST
argument_list|)
decl_stmt|;
name|SimilarityService
name|similarityService
init|=
operator|new
name|SimilarityService
argument_list|(
name|indexSettings
argument_list|,
name|Collections
operator|.
name|EMPTY_MAP
argument_list|)
decl_stmt|;
try|try
init|(
name|AnalysisService
name|analysisService
init|=
operator|new
name|FakeAnalysisService
argument_list|(
name|indexSettings
argument_list|)
init|)
block|{
try|try
init|(
name|MapperService
name|mapperService
init|=
operator|new
name|MapperService
argument_list|(
name|indexSettings
argument_list|,
name|analysisService
argument_list|,
name|similarityService
argument_list|,
name|scriptService
argument_list|)
init|)
block|{
for|for
control|(
name|ObjectCursor
argument_list|<
name|MappingMetaData
argument_list|>
name|cursor
range|:
name|indexMetaData
operator|.
name|getMappings
argument_list|()
operator|.
name|values
argument_list|()
control|)
block|{
name|MappingMetaData
name|mappingMetaData
init|=
name|cursor
operator|.
name|value
decl_stmt|;
name|mapperService
operator|.
name|merge
argument_list|(
name|mappingMetaData
operator|.
name|type
argument_list|()
argument_list|,
name|mappingMetaData
operator|.
name|source
argument_list|()
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
comment|// Wrap the inner exception so we have the index name in the exception message
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"unable to upgrade the mappings for the index ["
operator|+
name|indexMetaData
operator|.
name|getIndex
argument_list|()
operator|+
literal|"], reason: ["
operator|+
name|ex
operator|.
name|getMessage
argument_list|()
operator|+
literal|"]"
argument_list|,
name|ex
argument_list|)
throw|;
block|}
block|}
comment|/**      * Marks index as upgraded so we don't have to test it again      */
DECL|method|markAsUpgraded
specifier|private
name|IndexMetaData
name|markAsUpgraded
parameter_list|(
name|IndexMetaData
name|indexMetaData
parameter_list|)
block|{
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
name|indexMetaData
operator|.
name|getSettings
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_VERSION_UPGRADED
argument_list|,
name|Version
operator|.
name|CURRENT
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
name|settings
argument_list|(
name|settings
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
comment|/**      * A fake analysis server that returns the same keyword analyzer for all requests      */
DECL|class|FakeAnalysisService
specifier|private
specifier|static
class|class
name|FakeAnalysisService
extends|extends
name|AnalysisService
block|{
DECL|field|fakeAnalyzer
specifier|private
name|Analyzer
name|fakeAnalyzer
init|=
operator|new
name|Analyzer
argument_list|()
block|{
annotation|@
name|Override
specifier|protected
name|TokenStreamComponents
name|createComponents
parameter_list|(
name|String
name|fieldName
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"shouldn't be here"
argument_list|)
throw|;
block|}
block|}
decl_stmt|;
DECL|method|FakeAnalysisService
specifier|public
name|FakeAnalysisService
parameter_list|(
name|IndexSettings
name|indexSettings
parameter_list|)
block|{
name|super
argument_list|(
name|indexSettings
argument_list|,
name|Collections
operator|.
name|EMPTY_MAP
argument_list|,
name|Collections
operator|.
name|EMPTY_MAP
argument_list|,
name|Collections
operator|.
name|EMPTY_MAP
argument_list|,
name|Collections
operator|.
name|EMPTY_MAP
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|analyzer
specifier|public
name|NamedAnalyzer
name|analyzer
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
operator|new
name|NamedAnalyzer
argument_list|(
name|name
argument_list|,
name|fakeAnalyzer
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
block|{
name|fakeAnalyzer
operator|.
name|close
argument_list|()
expr_stmt|;
name|super
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

