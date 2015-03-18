begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.cluster.snapshots.restore
package|package
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
name|snapshots
operator|.
name|restore
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
name|MasterNodeOperationRequestBuilder
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
name|ClusterAdminClient
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
comment|/**  * Restore snapshot request builder  */
end_comment

begin_class
DECL|class|RestoreSnapshotRequestBuilder
specifier|public
class|class
name|RestoreSnapshotRequestBuilder
extends|extends
name|MasterNodeOperationRequestBuilder
argument_list|<
name|RestoreSnapshotRequest
argument_list|,
name|RestoreSnapshotResponse
argument_list|,
name|RestoreSnapshotRequestBuilder
argument_list|,
name|ClusterAdminClient
argument_list|>
block|{
comment|/**      * Constructs new restore snapshot request builder      *      * @param clusterAdminClient cluster admin client      */
DECL|method|RestoreSnapshotRequestBuilder
specifier|public
name|RestoreSnapshotRequestBuilder
parameter_list|(
name|ClusterAdminClient
name|clusterAdminClient
parameter_list|)
block|{
name|super
argument_list|(
name|clusterAdminClient
argument_list|,
operator|new
name|RestoreSnapshotRequest
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**      * Constructs new restore snapshot request builder with specified repository and snapshot names      *      * @param clusterAdminClient cluster admin client      * @param repository         reposiory name      * @param name               snapshot name      */
DECL|method|RestoreSnapshotRequestBuilder
specifier|public
name|RestoreSnapshotRequestBuilder
parameter_list|(
name|ClusterAdminClient
name|clusterAdminClient
parameter_list|,
name|String
name|repository
parameter_list|,
name|String
name|name
parameter_list|)
block|{
name|super
argument_list|(
name|clusterAdminClient
argument_list|,
operator|new
name|RestoreSnapshotRequest
argument_list|(
name|repository
argument_list|,
name|name
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**      * Sets snapshot name      *      * @param snapshot snapshot name      * @return this builder      */
DECL|method|setSnapshot
specifier|public
name|RestoreSnapshotRequestBuilder
name|setSnapshot
parameter_list|(
name|String
name|snapshot
parameter_list|)
block|{
name|request
operator|.
name|snapshot
argument_list|(
name|snapshot
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets repository name      *      * @param repository repository name      * @return this builder      */
DECL|method|setRepository
specifier|public
name|RestoreSnapshotRequestBuilder
name|setRepository
parameter_list|(
name|String
name|repository
parameter_list|)
block|{
name|request
operator|.
name|repository
argument_list|(
name|repository
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets the list of indices that should be restored from snapshot      *<p/>      * The list of indices supports multi-index syntax. For example: "+test*" ,"-test42" will index all indices with      * prefix "test" except index "test42". Aliases are not supported. An empty list or {"_all"} will restore all open      * indices in the snapshot.      *      * @param indices list of indices      * @return this builder      */
DECL|method|setIndices
specifier|public
name|RestoreSnapshotRequestBuilder
name|setIndices
parameter_list|(
name|String
modifier|...
name|indices
parameter_list|)
block|{
name|request
operator|.
name|indices
argument_list|(
name|indices
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Specifies what type of requested indices to ignore and how to deal with wildcard expressions.      * For example indices that don't exist.      *      * @param indicesOptions the desired behaviour regarding indices to ignore and wildcard indices expressions      * @return this builder      */
DECL|method|setIndicesOptions
specifier|public
name|RestoreSnapshotRequestBuilder
name|setIndicesOptions
parameter_list|(
name|IndicesOptions
name|indicesOptions
parameter_list|)
block|{
name|request
operator|.
name|indicesOptions
argument_list|(
name|indicesOptions
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets rename pattern that should be applied to restored indices.      *<p/>      * Indices that match the rename pattern will be renamed according to {@link #setRenameReplacement(String)}. The      * rename pattern is applied according to the {@link java.util.regex.Matcher#appendReplacement(StringBuffer, String)}      * The request will fail if two or more indices will be renamed into the same name.      *      * @param renamePattern rename pattern      * @return this builder      */
DECL|method|setRenamePattern
specifier|public
name|RestoreSnapshotRequestBuilder
name|setRenamePattern
parameter_list|(
name|String
name|renamePattern
parameter_list|)
block|{
name|request
operator|.
name|renamePattern
argument_list|(
name|renamePattern
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets rename replacement      *<p/>      * See {@link #setRenamePattern(String)} for more information.      *      * @param renameReplacement rename replacement      * @return this builder      */
DECL|method|setRenameReplacement
specifier|public
name|RestoreSnapshotRequestBuilder
name|setRenameReplacement
parameter_list|(
name|String
name|renameReplacement
parameter_list|)
block|{
name|request
operator|.
name|renameReplacement
argument_list|(
name|renameReplacement
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets repository-specific restore settings.      *<p/>      * See repository documentation for more information.      *      * @param settings repository-specific snapshot settings      * @return this builder      */
DECL|method|setSettings
specifier|public
name|RestoreSnapshotRequestBuilder
name|setSettings
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|request
operator|.
name|settings
argument_list|(
name|settings
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets repository-specific restore settings.      *<p/>      * See repository documentation for more information.      *      * @param settings repository-specific snapshot settings      * @return this builder      */
DECL|method|setSettings
specifier|public
name|RestoreSnapshotRequestBuilder
name|setSettings
parameter_list|(
name|Settings
operator|.
name|Builder
name|settings
parameter_list|)
block|{
name|request
operator|.
name|settings
argument_list|(
name|settings
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets repository-specific restore settings in JSON, YAML or properties format      *<p/>      * See repository documentation for more information.      *      * @param source repository-specific snapshot settings      * @return this builder      */
DECL|method|setSettings
specifier|public
name|RestoreSnapshotRequestBuilder
name|setSettings
parameter_list|(
name|String
name|source
parameter_list|)
block|{
name|request
operator|.
name|settings
argument_list|(
name|source
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets repository-specific restore settings      *<p/>      * See repository documentation for more information.      *      * @param source repository-specific snapshot settings      * @return this builder      */
DECL|method|setSettings
specifier|public
name|RestoreSnapshotRequestBuilder
name|setSettings
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|source
parameter_list|)
block|{
name|request
operator|.
name|settings
argument_list|(
name|source
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * If this parameter is set to true the operation will wait for completion of restore process before returning.      *      * @param waitForCompletion if true the operation will wait for completion      * @return this builder      */
DECL|method|setWaitForCompletion
specifier|public
name|RestoreSnapshotRequestBuilder
name|setWaitForCompletion
parameter_list|(
name|boolean
name|waitForCompletion
parameter_list|)
block|{
name|request
operator|.
name|waitForCompletion
argument_list|(
name|waitForCompletion
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * If set to true the restore procedure will restore global cluster state.      *<p/>      * The global cluster state includes persistent settings and index template definitions.      *      * @param restoreGlobalState true if global state should be restored from the snapshot      * @return this builder      */
DECL|method|setRestoreGlobalState
specifier|public
name|RestoreSnapshotRequestBuilder
name|setRestoreGlobalState
parameter_list|(
name|boolean
name|restoreGlobalState
parameter_list|)
block|{
name|request
operator|.
name|includeGlobalState
argument_list|(
name|restoreGlobalState
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * If set to true the restore procedure will restore partially snapshotted indices      *      * @param partial true if partially snapshotted indices should be restored      * @return this builder      */
DECL|method|setPartial
specifier|public
name|RestoreSnapshotRequestBuilder
name|setPartial
parameter_list|(
name|boolean
name|partial
parameter_list|)
block|{
name|request
operator|.
name|partial
argument_list|(
name|partial
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * If set to true the restore procedure will restore aliases      *      * @param restoreAliases true if aliases should be restored from the snapshot      * @return this builder      */
DECL|method|setIncludeAliases
specifier|public
name|RestoreSnapshotRequestBuilder
name|setIncludeAliases
parameter_list|(
name|boolean
name|restoreAliases
parameter_list|)
block|{
name|request
operator|.
name|includeAliases
argument_list|(
name|restoreAliases
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets index settings that should be added or replaced during restore      * @param settings index settings      * @return this builder      */
DECL|method|setIndexSettings
specifier|public
name|RestoreSnapshotRequestBuilder
name|setIndexSettings
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|request
operator|.
name|indexSettings
argument_list|(
name|settings
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets index settings that should be added or replaced during restore       * @param settings index settings      * @return this builder      */
DECL|method|setIndexSettings
specifier|public
name|RestoreSnapshotRequestBuilder
name|setIndexSettings
parameter_list|(
name|Settings
operator|.
name|Builder
name|settings
parameter_list|)
block|{
name|request
operator|.
name|indexSettings
argument_list|(
name|settings
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets index settings that should be added or replaced during restore       * @param source index settings      * @return this builder      */
DECL|method|setIndexSettings
specifier|public
name|RestoreSnapshotRequestBuilder
name|setIndexSettings
parameter_list|(
name|String
name|source
parameter_list|)
block|{
name|request
operator|.
name|indexSettings
argument_list|(
name|source
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets index settings that should be added or replaced during restore       * @param source index settings      * @return this builder      */
DECL|method|setIndexSettings
specifier|public
name|RestoreSnapshotRequestBuilder
name|setIndexSettings
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|source
parameter_list|)
block|{
name|request
operator|.
name|indexSettings
argument_list|(
name|source
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets the list of index settings and index settings groups that shouldn't be restored from snapshot      */
DECL|method|setIgnoreIndexSettings
specifier|public
name|RestoreSnapshotRequestBuilder
name|setIgnoreIndexSettings
parameter_list|(
name|String
modifier|...
name|ignoreIndexSettings
parameter_list|)
block|{
name|request
operator|.
name|ignoreIndexSettings
argument_list|(
name|ignoreIndexSettings
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets the list of index settings and index settings groups that shouldn't be restored from snapshot      */
DECL|method|setIgnoreIndexSettings
specifier|public
name|RestoreSnapshotRequestBuilder
name|setIgnoreIndexSettings
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|ignoreIndexSettings
parameter_list|)
block|{
name|request
operator|.
name|ignoreIndexSettings
argument_list|(
name|ignoreIndexSettings
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
DECL|method|doExecute
specifier|protected
name|void
name|doExecute
parameter_list|(
name|ActionListener
argument_list|<
name|RestoreSnapshotResponse
argument_list|>
name|listener
parameter_list|)
block|{
name|client
operator|.
name|restoreSnapshot
argument_list|(
name|request
argument_list|,
name|listener
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

