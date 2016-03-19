begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/* /*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.bwcompat
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|bwcompat
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
name|admin
operator|.
name|cluster
operator|.
name|snapshots
operator|.
name|get
operator|.
name|GetSnapshotsResponse
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
name|snapshots
operator|.
name|restore
operator|.
name|RestoreSnapshotResponse
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
name|search
operator|.
name|SearchResponse
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
name|routing
operator|.
name|allocation
operator|.
name|decider
operator|.
name|FilterAllocationDecider
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
name|repositories
operator|.
name|uri
operator|.
name|URLRepository
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
name|RestStatus
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|snapshots
operator|.
name|AbstractSnapshotIntegTestCase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|snapshots
operator|.
name|RestoreInfo
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|snapshots
operator|.
name|SnapshotInfo
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|snapshots
operator|.
name|SnapshotRestoreException
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
name|ESIntegTestCase
operator|.
name|ClusterScope
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
name|ESIntegTestCase
operator|.
name|Scope
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
name|VersionUtils
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
name|lang
operator|.
name|reflect
operator|.
name|Modifier
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URI
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URISyntaxException
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
name|DirectoryStream
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
name|Files
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
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Locale
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|SortedSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeSet
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
name|Settings
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
name|containsString
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
name|greaterThan
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
name|notNullValue
import|;
end_import

begin_class
annotation|@
name|ClusterScope
argument_list|(
name|scope
operator|=
name|Scope
operator|.
name|TEST
argument_list|)
DECL|class|RestoreBackwardsCompatIT
specifier|public
class|class
name|RestoreBackwardsCompatIT
extends|extends
name|AbstractSnapshotIntegTestCase
block|{
annotation|@
name|Override
DECL|method|nodeSettings
specifier|protected
name|Settings
name|nodeSettings
parameter_list|(
name|int
name|nodeOrdinal
parameter_list|)
block|{
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
comment|// Configure using path.repo
return|return
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
name|super
operator|.
name|nodeSettings
argument_list|(
name|nodeOrdinal
argument_list|)
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
name|getBwcIndicesPath
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
else|else
block|{
comment|// Configure using url white list
try|try
block|{
name|URI
name|repoJarPatternUri
init|=
operator|new
name|URI
argument_list|(
literal|"jar:"
operator|+
name|getBwcIndicesPath
argument_list|()
operator|.
name|toUri
argument_list|()
operator|.
name|toString
argument_list|()
operator|+
literal|"*.zip!/repo/"
argument_list|)
decl_stmt|;
return|return
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
name|super
operator|.
name|nodeSettings
argument_list|(
name|nodeOrdinal
argument_list|)
argument_list|)
operator|.
name|putArray
argument_list|(
name|URLRepository
operator|.
name|ALLOWED_URLS_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
name|repoJarPatternUri
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|URISyntaxException
name|ex
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|ex
argument_list|)
throw|;
block|}
block|}
block|}
DECL|method|testRestoreOldSnapshots
specifier|public
name|void
name|testRestoreOldSnapshots
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|repo
init|=
literal|"test_repo"
decl_stmt|;
name|String
name|snapshot
init|=
literal|"test_1"
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|repoVersions
init|=
name|repoVersions
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|repoVersions
operator|.
name|size
argument_list|()
argument_list|,
name|greaterThan
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|version
range|:
name|repoVersions
control|)
block|{
name|createRepo
argument_list|(
literal|"repo"
argument_list|,
name|version
argument_list|,
name|repo
argument_list|)
expr_stmt|;
name|testOldSnapshot
argument_list|(
name|version
argument_list|,
name|repo
argument_list|,
name|snapshot
argument_list|)
expr_stmt|;
block|}
name|SortedSet
argument_list|<
name|String
argument_list|>
name|expectedVersions
init|=
operator|new
name|TreeSet
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Field
name|field
range|:
name|Version
operator|.
name|class
operator|.
name|getFields
argument_list|()
control|)
block|{
if|if
condition|(
name|Modifier
operator|.
name|isStatic
argument_list|(
name|field
operator|.
name|getModifiers
argument_list|()
argument_list|)
operator|&&
name|field
operator|.
name|getType
argument_list|()
operator|==
name|Version
operator|.
name|class
condition|)
block|{
name|Version
name|v
init|=
operator|(
name|Version
operator|)
name|field
operator|.
name|get
argument_list|(
name|Version
operator|.
name|class
argument_list|)
decl_stmt|;
if|if
condition|(
name|VersionUtils
operator|.
name|isSnapshot
argument_list|(
name|v
argument_list|)
condition|)
continue|continue;
if|if
condition|(
name|v
operator|.
name|onOrBefore
argument_list|(
name|Version
operator|.
name|V_2_0_0_beta1
argument_list|)
condition|)
continue|continue;
if|if
condition|(
name|v
operator|.
name|equals
argument_list|(
name|Version
operator|.
name|CURRENT
argument_list|)
condition|)
continue|continue;
name|expectedVersions
operator|.
name|add
argument_list|(
name|v
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
for|for
control|(
name|String
name|repoVersion
range|:
name|repoVersions
control|)
block|{
if|if
condition|(
name|expectedVersions
operator|.
name|remove
argument_list|(
name|repoVersion
argument_list|)
operator|==
literal|false
condition|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"Old repositories tests contain extra repo: {}"
argument_list|,
name|repoVersion
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|expectedVersions
operator|.
name|isEmpty
argument_list|()
operator|==
literal|false
condition|)
block|{
name|StringBuilder
name|msg
init|=
operator|new
name|StringBuilder
argument_list|(
literal|"Old repositories tests are missing versions:"
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|expected
range|:
name|expectedVersions
control|)
block|{
name|msg
operator|.
name|append
argument_list|(
literal|"\n"
operator|+
name|expected
argument_list|)
expr_stmt|;
block|}
name|fail
argument_list|(
name|msg
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testRestoreUnsupportedSnapshots
specifier|public
name|void
name|testRestoreUnsupportedSnapshots
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|repo
init|=
literal|"test_repo"
decl_stmt|;
name|String
name|snapshot
init|=
literal|"test_1"
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|repoVersions
init|=
name|unsupportedRepoVersions
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|repoVersions
operator|.
name|size
argument_list|()
argument_list|,
name|greaterThan
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|version
range|:
name|repoVersions
control|)
block|{
name|createRepo
argument_list|(
literal|"unsupportedrepo"
argument_list|,
name|version
argument_list|,
name|repo
argument_list|)
expr_stmt|;
name|assertUnsupportedIndexFailsToRestore
argument_list|(
name|repo
argument_list|,
name|snapshot
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|repoVersions
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|repoVersions
parameter_list|()
throws|throws
name|Exception
block|{
return|return
name|listRepoVersions
argument_list|(
literal|"repo"
argument_list|)
return|;
block|}
DECL|method|unsupportedRepoVersions
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|unsupportedRepoVersions
parameter_list|()
throws|throws
name|Exception
block|{
return|return
name|listRepoVersions
argument_list|(
literal|"unsupportedrepo"
argument_list|)
return|;
block|}
DECL|method|listRepoVersions
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|listRepoVersions
parameter_list|(
name|String
name|prefix
parameter_list|)
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|String
argument_list|>
name|repoVersions
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|Path
name|repoFiles
init|=
name|getBwcIndicesPath
argument_list|()
decl_stmt|;
try|try
init|(
name|DirectoryStream
argument_list|<
name|Path
argument_list|>
name|stream
init|=
name|Files
operator|.
name|newDirectoryStream
argument_list|(
name|repoFiles
argument_list|,
name|prefix
operator|+
literal|"-*.zip"
argument_list|)
init|)
block|{
for|for
control|(
name|Path
name|entry
range|:
name|stream
control|)
block|{
name|String
name|fileName
init|=
name|entry
operator|.
name|getFileName
argument_list|()
operator|.
name|toString
argument_list|()
decl_stmt|;
name|String
name|version
init|=
name|fileName
operator|.
name|substring
argument_list|(
name|prefix
operator|.
name|length
argument_list|()
operator|+
literal|1
argument_list|)
decl_stmt|;
name|version
operator|=
name|version
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|version
operator|.
name|length
argument_list|()
operator|-
literal|".zip"
operator|.
name|length
argument_list|()
argument_list|)
expr_stmt|;
name|repoVersions
operator|.
name|add
argument_list|(
name|version
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|repoVersions
return|;
block|}
DECL|method|createRepo
specifier|private
name|void
name|createRepo
parameter_list|(
name|String
name|prefix
parameter_list|,
name|String
name|version
parameter_list|,
name|String
name|repo
parameter_list|)
throws|throws
name|Exception
block|{
name|Path
name|repoFile
init|=
name|getBwcIndicesPath
argument_list|()
operator|.
name|resolve
argument_list|(
name|prefix
operator|+
literal|"-"
operator|+
name|version
operator|+
literal|".zip"
argument_list|)
decl_stmt|;
name|URI
name|repoFileUri
init|=
name|repoFile
operator|.
name|toUri
argument_list|()
decl_stmt|;
name|URI
name|repoJarUri
init|=
operator|new
name|URI
argument_list|(
literal|"jar:"
operator|+
name|repoFileUri
operator|.
name|toString
argument_list|()
operator|+
literal|"!/repo/"
argument_list|)
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"-->  creating repository [{}] for version [{}]"
argument_list|,
name|repo
argument_list|,
name|version
argument_list|)
expr_stmt|;
name|assertAcked
argument_list|(
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|preparePutRepository
argument_list|(
name|repo
argument_list|)
operator|.
name|setType
argument_list|(
literal|"url"
argument_list|)
operator|.
name|setSettings
argument_list|(
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"url"
argument_list|,
name|repoJarUri
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testOldSnapshot
specifier|private
name|void
name|testOldSnapshot
parameter_list|(
name|String
name|version
parameter_list|,
name|String
name|repo
parameter_list|,
name|String
name|snapshot
parameter_list|)
throws|throws
name|IOException
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"--> get snapshot and check its version"
argument_list|)
expr_stmt|;
name|GetSnapshotsResponse
name|getSnapshotsResponse
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
name|prepareGetSnapshots
argument_list|(
name|repo
argument_list|)
operator|.
name|setSnapshots
argument_list|(
name|snapshot
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|getSnapshotsResponse
operator|.
name|getSnapshots
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|SnapshotInfo
name|snapshotInfo
init|=
name|getSnapshotsResponse
operator|.
name|getSnapshots
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|snapshotInfo
operator|.
name|version
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|version
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> restoring snapshot"
argument_list|)
expr_stmt|;
name|RestoreSnapshotResponse
name|response
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
name|prepareRestoreSnapshot
argument_list|(
name|repo
argument_list|,
name|snapshot
argument_list|)
operator|.
name|setRestoreGlobalState
argument_list|(
literal|true
argument_list|)
operator|.
name|setWaitForCompletion
argument_list|(
literal|true
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|status
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|RestStatus
operator|.
name|OK
argument_list|)
argument_list|)
expr_stmt|;
name|RestoreInfo
name|restoreInfo
init|=
name|response
operator|.
name|getRestoreInfo
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|restoreInfo
operator|.
name|successfulShards
argument_list|()
argument_list|,
name|greaterThan
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|restoreInfo
operator|.
name|successfulShards
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|restoreInfo
operator|.
name|totalShards
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|restoreInfo
operator|.
name|failedShards
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|index
init|=
name|restoreInfo
operator|.
name|indices
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> check search"
argument_list|)
expr_stmt|;
name|SearchResponse
name|searchResponse
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
name|index
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|searchResponse
operator|.
name|getHits
argument_list|()
operator|.
name|totalHits
argument_list|()
argument_list|,
name|greaterThan
argument_list|(
literal|1L
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> check settings"
argument_list|)
expr_stmt|;
name|ClusterState
name|clusterState
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
decl_stmt|;
name|assertThat
argument_list|(
name|clusterState
operator|.
name|metaData
argument_list|()
operator|.
name|persistentSettings
argument_list|()
operator|.
name|get
argument_list|(
name|FilterAllocationDecider
operator|.
name|CLUSTER_ROUTING_EXCLUDE_GROUP_SETTING
operator|.
name|getKey
argument_list|()
operator|+
literal|"version_attr"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|version
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> check templates"
argument_list|)
expr_stmt|;
name|IndexTemplateMetaData
name|template
init|=
name|clusterState
operator|.
name|getMetaData
argument_list|()
operator|.
name|templates
argument_list|()
operator|.
name|get
argument_list|(
literal|"template_"
operator|+
name|version
operator|.
name|toLowerCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|template
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|template
operator|.
name|template
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"te*"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|template
operator|.
name|settings
argument_list|()
operator|.
name|getAsInt
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_NUMBER_OF_SHARDS
argument_list|,
operator|-
literal|1
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|template
operator|.
name|mappings
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|template
operator|.
name|mappings
argument_list|()
operator|.
name|get
argument_list|(
literal|"type1"
argument_list|)
operator|.
name|string
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"{\"type1\":{\"_source\":{\"enabled\":false}}}"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|template
operator|.
name|aliases
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|template
operator|.
name|aliases
argument_list|()
operator|.
name|get
argument_list|(
literal|"alias1"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|template
operator|.
name|aliases
argument_list|()
operator|.
name|get
argument_list|(
literal|"alias2"
argument_list|)
operator|.
name|filter
argument_list|()
operator|.
name|string
argument_list|()
argument_list|,
name|containsString
argument_list|(
name|version
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|template
operator|.
name|aliases
argument_list|()
operator|.
name|get
argument_list|(
literal|"alias2"
argument_list|)
operator|.
name|indexRouting
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"kimchy"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|template
operator|.
name|aliases
argument_list|()
operator|.
name|get
argument_list|(
literal|"{index}-alias"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> cleanup"
argument_list|)
expr_stmt|;
name|cluster
argument_list|()
operator|.
name|wipeIndices
argument_list|(
name|restoreInfo
operator|.
name|indices
argument_list|()
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
name|restoreInfo
operator|.
name|indices
argument_list|()
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|cluster
argument_list|()
operator|.
name|wipeTemplates
argument_list|()
expr_stmt|;
block|}
DECL|method|assertUnsupportedIndexFailsToRestore
specifier|private
name|void
name|assertUnsupportedIndexFailsToRestore
parameter_list|(
name|String
name|repo
parameter_list|,
name|String
name|snapshot
parameter_list|)
throws|throws
name|IOException
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"--> restoring unsupported snapshot"
argument_list|)
expr_stmt|;
try|try
block|{
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareRestoreSnapshot
argument_list|(
name|repo
argument_list|,
name|snapshot
argument_list|)
operator|.
name|setRestoreGlobalState
argument_list|(
literal|true
argument_list|)
operator|.
name|setWaitForCompletion
argument_list|(
literal|true
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|fail
argument_list|(
literal|"should have failed to restore"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SnapshotRestoreException
name|ex
parameter_list|)
block|{
name|assertThat
argument_list|(
name|ex
operator|.
name|getMessage
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"cannot restore index"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ex
operator|.
name|getMessage
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"because it cannot be upgraded"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

