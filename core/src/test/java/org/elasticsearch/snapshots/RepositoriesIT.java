begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.snapshots
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|snapshots
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
name|admin
operator|.
name|cluster
operator|.
name|repositories
operator|.
name|delete
operator|.
name|DeleteRepositoryResponse
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
name|repositories
operator|.
name|get
operator|.
name|GetRepositoriesResponse
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
name|repositories
operator|.
name|put
operator|.
name|PutRepositoryResponse
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
name|repositories
operator|.
name|verify
operator|.
name|VerifyRepositoryResponse
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
name|metadata
operator|.
name|RepositoriesMetaData
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
name|RepositoryMetaData
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
name|FileSystemUtils
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
name|ByteSizeUnit
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
name|RepositoryException
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
name|RepositoryVerificationException
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
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
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
name|List
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
name|elasticsearch
operator|.
name|test
operator|.
name|hamcrest
operator|.
name|ElasticsearchAssertions
operator|.
name|assertThrows
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
name|notNullValue
import|;
end_import

begin_comment
comment|/**  */
end_comment

begin_class
annotation|@
name|ESIntegTestCase
operator|.
name|ClusterScope
argument_list|(
name|minNumDataNodes
operator|=
literal|2
argument_list|)
DECL|class|RepositoriesIT
specifier|public
class|class
name|RepositoriesIT
extends|extends
name|AbstractSnapshotIntegTestCase
block|{
annotation|@
name|Test
DECL|method|testRepositoryCreation
specifier|public
name|void
name|testRepositoryCreation
parameter_list|()
throws|throws
name|Exception
block|{
name|Client
name|client
init|=
name|client
argument_list|()
decl_stmt|;
name|Path
name|location
init|=
name|randomRepoPath
argument_list|()
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"-->  creating repository"
argument_list|)
expr_stmt|;
name|PutRepositoryResponse
name|putRepositoryResponse
init|=
name|client
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|preparePutRepository
argument_list|(
literal|"test-repo-1"
argument_list|)
operator|.
name|setType
argument_list|(
literal|"fs"
argument_list|)
operator|.
name|setSettings
argument_list|(
name|Settings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"location"
argument_list|,
name|location
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|putRepositoryResponse
operator|.
name|isAcknowledged
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> verify the repository"
argument_list|)
expr_stmt|;
name|int
name|numberOfFiles
init|=
name|FileSystemUtils
operator|.
name|files
argument_list|(
name|location
argument_list|)
operator|.
name|length
decl_stmt|;
name|VerifyRepositoryResponse
name|verifyRepositoryResponse
init|=
name|client
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareVerifyRepository
argument_list|(
literal|"test-repo-1"
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|verifyRepositoryResponse
operator|.
name|getNodes
argument_list|()
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
name|cluster
argument_list|()
operator|.
name|numDataAndMasterNodes
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> verify that we didn't leave any files as a result of verification"
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|FileSystemUtils
operator|.
name|files
argument_list|(
name|location
argument_list|)
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
name|numberOfFiles
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> check that repository is really there"
argument_list|)
expr_stmt|;
name|ClusterStateResponse
name|clusterStateResponse
init|=
name|client
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
name|clear
argument_list|()
operator|.
name|setMetaData
argument_list|(
literal|true
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|MetaData
name|metaData
init|=
name|clusterStateResponse
operator|.
name|getState
argument_list|()
operator|.
name|getMetaData
argument_list|()
decl_stmt|;
name|RepositoriesMetaData
name|repositoriesMetaData
init|=
name|metaData
operator|.
name|custom
argument_list|(
name|RepositoriesMetaData
operator|.
name|TYPE
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|repositoriesMetaData
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|repositoriesMetaData
operator|.
name|repository
argument_list|(
literal|"test-repo-1"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|repositoriesMetaData
operator|.
name|repository
argument_list|(
literal|"test-repo-1"
argument_list|)
operator|.
name|type
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"fs"
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"-->  creating another repository"
argument_list|)
expr_stmt|;
name|putRepositoryResponse
operator|=
name|client
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|preparePutRepository
argument_list|(
literal|"test-repo-2"
argument_list|)
operator|.
name|setType
argument_list|(
literal|"fs"
argument_list|)
operator|.
name|setSettings
argument_list|(
name|Settings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"location"
argument_list|,
name|randomRepoPath
argument_list|()
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|putRepositoryResponse
operator|.
name|isAcknowledged
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> check that both repositories are in cluster state"
argument_list|)
expr_stmt|;
name|clusterStateResponse
operator|=
name|client
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
name|clear
argument_list|()
operator|.
name|setMetaData
argument_list|(
literal|true
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|metaData
operator|=
name|clusterStateResponse
operator|.
name|getState
argument_list|()
operator|.
name|getMetaData
argument_list|()
expr_stmt|;
name|repositoriesMetaData
operator|=
name|metaData
operator|.
name|custom
argument_list|(
name|RepositoriesMetaData
operator|.
name|TYPE
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|repositoriesMetaData
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|repositoriesMetaData
operator|.
name|repositories
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|repositoriesMetaData
operator|.
name|repository
argument_list|(
literal|"test-repo-1"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|repositoriesMetaData
operator|.
name|repository
argument_list|(
literal|"test-repo-1"
argument_list|)
operator|.
name|type
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"fs"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|repositoriesMetaData
operator|.
name|repository
argument_list|(
literal|"test-repo-2"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|repositoriesMetaData
operator|.
name|repository
argument_list|(
literal|"test-repo-2"
argument_list|)
operator|.
name|type
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"fs"
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> check that both repositories can be retrieved by getRepositories query"
argument_list|)
expr_stmt|;
name|GetRepositoriesResponse
name|repositoriesResponse
init|=
name|client
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareGetRepositories
argument_list|()
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|repositoriesResponse
operator|.
name|repositories
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|findRepository
argument_list|(
name|repositoriesResponse
operator|.
name|repositories
argument_list|()
argument_list|,
literal|"test-repo-1"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|findRepository
argument_list|(
name|repositoriesResponse
operator|.
name|repositories
argument_list|()
argument_list|,
literal|"test-repo-2"
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
literal|"--> delete repository test-repo-1"
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
name|prepareDeleteRepository
argument_list|(
literal|"test-repo-1"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|repositoriesResponse
operator|=
name|client
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareGetRepositories
argument_list|()
operator|.
name|get
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|repositoriesResponse
operator|.
name|repositories
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
name|findRepository
argument_list|(
name|repositoriesResponse
operator|.
name|repositories
argument_list|()
argument_list|,
literal|"test-repo-2"
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
literal|"--> delete repository test-repo-2"
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
name|prepareDeleteRepository
argument_list|(
literal|"test-repo-2"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|repositoriesResponse
operator|=
name|client
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareGetRepositories
argument_list|()
operator|.
name|get
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|repositoriesResponse
operator|.
name|repositories
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
block|}
DECL|method|findRepository
specifier|private
name|RepositoryMetaData
name|findRepository
parameter_list|(
name|List
argument_list|<
name|RepositoryMetaData
argument_list|>
name|repositories
parameter_list|,
name|String
name|name
parameter_list|)
block|{
for|for
control|(
name|RepositoryMetaData
name|repository
range|:
name|repositories
control|)
block|{
if|if
condition|(
name|repository
operator|.
name|name
argument_list|()
operator|.
name|equals
argument_list|(
name|name
argument_list|)
condition|)
block|{
return|return
name|repository
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
annotation|@
name|Test
DECL|method|testMisconfiguredRepository
specifier|public
name|void
name|testMisconfiguredRepository
parameter_list|()
throws|throws
name|Exception
block|{
name|Client
name|client
init|=
name|client
argument_list|()
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> trying creating repository with incorrect settings"
argument_list|)
expr_stmt|;
try|try
block|{
name|client
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|preparePutRepository
argument_list|(
literal|"test-repo"
argument_list|)
operator|.
name|setType
argument_list|(
literal|"fs"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|fail
argument_list|(
literal|"Shouldn't be here"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RepositoryException
name|ex
parameter_list|)
block|{
name|assertThat
argument_list|(
name|ex
operator|.
name|toString
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"missing location"
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|logger
operator|.
name|info
argument_list|(
literal|"--> trying creating fs repository with location that is not registered in path.repo setting"
argument_list|)
expr_stmt|;
name|Path
name|invalidRepoPath
init|=
name|createTempDir
argument_list|()
operator|.
name|toAbsolutePath
argument_list|()
decl_stmt|;
name|String
name|location
init|=
name|invalidRepoPath
operator|.
name|toString
argument_list|()
decl_stmt|;
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
name|preparePutRepository
argument_list|(
literal|"test-repo"
argument_list|)
operator|.
name|setType
argument_list|(
literal|"fs"
argument_list|)
operator|.
name|setSettings
argument_list|(
name|Settings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"location"
argument_list|,
name|location
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|fail
argument_list|(
literal|"Shouldn't be here"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RepositoryException
name|ex
parameter_list|)
block|{
name|assertThat
argument_list|(
name|ex
operator|.
name|toString
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"location ["
operator|+
name|location
operator|+
literal|"] doesn't match any of the locations specified by path.repo"
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|String
name|repoUrl
init|=
name|invalidRepoPath
operator|.
name|toAbsolutePath
argument_list|()
operator|.
name|toUri
argument_list|()
operator|.
name|toURL
argument_list|()
operator|.
name|toString
argument_list|()
decl_stmt|;
name|String
name|unsupportedUrl
init|=
name|repoUrl
operator|.
name|replace
argument_list|(
literal|"file:/"
argument_list|,
literal|"netdoc:/"
argument_list|)
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> trying creating url repository with unsupported url protocol"
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
name|preparePutRepository
argument_list|(
literal|"test-repo"
argument_list|)
operator|.
name|setType
argument_list|(
literal|"url"
argument_list|)
operator|.
name|setSettings
argument_list|(
name|Settings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"url"
argument_list|,
name|unsupportedUrl
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|fail
argument_list|(
literal|"Shouldn't be here"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RepositoryException
name|ex
parameter_list|)
block|{
name|assertThat
argument_list|(
name|ex
operator|.
name|toString
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"unsupported url protocol [netdoc]"
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|logger
operator|.
name|info
argument_list|(
literal|"--> trying creating url repository with location that is not registered in path.repo setting"
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
name|preparePutRepository
argument_list|(
literal|"test-repo"
argument_list|)
operator|.
name|setType
argument_list|(
literal|"url"
argument_list|)
operator|.
name|setSettings
argument_list|(
name|Settings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"url"
argument_list|,
name|invalidRepoPath
operator|.
name|toUri
argument_list|()
operator|.
name|toURL
argument_list|()
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|fail
argument_list|(
literal|"Shouldn't be here"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RepositoryException
name|ex
parameter_list|)
block|{
name|assertThat
argument_list|(
name|ex
operator|.
name|toString
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"doesn't match any of the locations specified by path.repo"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
DECL|method|repositoryAckTimeoutTest
specifier|public
name|void
name|repositoryAckTimeoutTest
parameter_list|()
throws|throws
name|Exception
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"-->  creating repository test-repo-1 with 0s timeout - shouldn't ack"
argument_list|)
expr_stmt|;
name|PutRepositoryResponse
name|putRepositoryResponse
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
name|preparePutRepository
argument_list|(
literal|"test-repo-1"
argument_list|)
operator|.
name|setType
argument_list|(
literal|"fs"
argument_list|)
operator|.
name|setSettings
argument_list|(
name|Settings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"location"
argument_list|,
name|randomRepoPath
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
literal|"compress"
argument_list|,
name|randomBoolean
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
literal|"chunk_size"
argument_list|,
name|randomIntBetween
argument_list|(
literal|5
argument_list|,
literal|100
argument_list|)
argument_list|,
name|ByteSizeUnit
operator|.
name|BYTES
argument_list|)
argument_list|)
operator|.
name|setTimeout
argument_list|(
literal|"0s"
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|putRepositoryResponse
operator|.
name|isAcknowledged
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"-->  creating repository test-repo-2 with standard timeout - should ack"
argument_list|)
expr_stmt|;
name|putRepositoryResponse
operator|=
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
literal|"test-repo-2"
argument_list|)
operator|.
name|setType
argument_list|(
literal|"fs"
argument_list|)
operator|.
name|setSettings
argument_list|(
name|Settings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"location"
argument_list|,
name|randomRepoPath
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
literal|"compress"
argument_list|,
name|randomBoolean
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
literal|"chunk_size"
argument_list|,
name|randomIntBetween
argument_list|(
literal|5
argument_list|,
literal|100
argument_list|)
argument_list|,
name|ByteSizeUnit
operator|.
name|BYTES
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|putRepositoryResponse
operator|.
name|isAcknowledged
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"-->  deleting repository test-repo-2 with 0s timeout - shouldn't ack"
argument_list|)
expr_stmt|;
name|DeleteRepositoryResponse
name|deleteRepositoryResponse
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
name|prepareDeleteRepository
argument_list|(
literal|"test-repo-2"
argument_list|)
operator|.
name|setTimeout
argument_list|(
literal|"0s"
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|deleteRepositoryResponse
operator|.
name|isAcknowledged
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"-->  deleting repository test-repo-1 with standard timeout - should ack"
argument_list|)
expr_stmt|;
name|deleteRepositoryResponse
operator|=
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareDeleteRepository
argument_list|(
literal|"test-repo-1"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|deleteRepositoryResponse
operator|.
name|isAcknowledged
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|repositoryVerificationTest
specifier|public
name|void
name|repositoryVerificationTest
parameter_list|()
throws|throws
name|Exception
block|{
name|Client
name|client
init|=
name|client
argument_list|()
decl_stmt|;
name|Settings
name|settings
init|=
name|Settings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"location"
argument_list|,
name|randomRepoPath
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
literal|"random_control_io_exception_rate"
argument_list|,
literal|1.0
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"-->  creating repository that cannot write any files - should fail"
argument_list|)
expr_stmt|;
name|assertThrows
argument_list|(
name|client
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|preparePutRepository
argument_list|(
literal|"test-repo-1"
argument_list|)
operator|.
name|setType
argument_list|(
literal|"mock"
argument_list|)
operator|.
name|setSettings
argument_list|(
name|settings
argument_list|)
argument_list|,
name|RepositoryVerificationException
operator|.
name|class
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"-->  creating repository that cannot write any files, but suppress verification - should be acked"
argument_list|)
expr_stmt|;
name|assertAcked
argument_list|(
name|client
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|preparePutRepository
argument_list|(
literal|"test-repo-1"
argument_list|)
operator|.
name|setType
argument_list|(
literal|"mock"
argument_list|)
operator|.
name|setSettings
argument_list|(
name|settings
argument_list|)
operator|.
name|setVerify
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"-->  verifying repository"
argument_list|)
expr_stmt|;
name|assertThrows
argument_list|(
name|client
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareVerifyRepository
argument_list|(
literal|"test-repo-1"
argument_list|)
argument_list|,
name|RepositoryVerificationException
operator|.
name|class
argument_list|)
expr_stmt|;
name|Path
name|location
init|=
name|randomRepoPath
argument_list|()
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"-->  creating repository"
argument_list|)
expr_stmt|;
try|try
block|{
name|client
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|preparePutRepository
argument_list|(
literal|"test-repo-1"
argument_list|)
operator|.
name|setType
argument_list|(
literal|"mock"
argument_list|)
operator|.
name|setSettings
argument_list|(
name|Settings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"location"
argument_list|,
name|location
argument_list|)
operator|.
name|put
argument_list|(
literal|"localize_location"
argument_list|,
literal|true
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|fail
argument_list|(
literal|"RepositoryVerificationException wasn't generated"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RepositoryVerificationException
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
literal|"is not shared"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
DECL|method|repositoryVerificationTimeoutTest
specifier|public
name|void
name|repositoryVerificationTimeoutTest
parameter_list|()
throws|throws
name|Exception
block|{
name|Client
name|client
init|=
name|client
argument_list|()
decl_stmt|;
name|Settings
name|settings
init|=
name|Settings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"location"
argument_list|,
name|randomRepoPath
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
literal|"random_control_io_exception_rate"
argument_list|,
literal|1.0
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"-->  creating repository that cannot write any files - should fail"
argument_list|)
expr_stmt|;
name|assertThrows
argument_list|(
name|client
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|preparePutRepository
argument_list|(
literal|"test-repo-1"
argument_list|)
operator|.
name|setType
argument_list|(
literal|"mock"
argument_list|)
operator|.
name|setSettings
argument_list|(
name|settings
argument_list|)
argument_list|,
name|RepositoryVerificationException
operator|.
name|class
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"-->  creating repository that cannot write any files, but suppress verification - should be acked"
argument_list|)
expr_stmt|;
name|assertAcked
argument_list|(
name|client
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|preparePutRepository
argument_list|(
literal|"test-repo-1"
argument_list|)
operator|.
name|setType
argument_list|(
literal|"mock"
argument_list|)
operator|.
name|setSettings
argument_list|(
name|settings
argument_list|)
operator|.
name|setVerify
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"-->  verifying repository"
argument_list|)
expr_stmt|;
name|assertThrows
argument_list|(
name|client
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareVerifyRepository
argument_list|(
literal|"test-repo-1"
argument_list|)
argument_list|,
name|RepositoryVerificationException
operator|.
name|class
argument_list|)
expr_stmt|;
name|Path
name|location
init|=
name|randomRepoPath
argument_list|()
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"-->  creating repository"
argument_list|)
expr_stmt|;
try|try
block|{
name|client
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|preparePutRepository
argument_list|(
literal|"test-repo-1"
argument_list|)
operator|.
name|setType
argument_list|(
literal|"mock"
argument_list|)
operator|.
name|setSettings
argument_list|(
name|Settings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"location"
argument_list|,
name|location
argument_list|)
operator|.
name|put
argument_list|(
literal|"localize_location"
argument_list|,
literal|true
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|fail
argument_list|(
literal|"RepositoryVerificationException wasn't generated"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RepositoryVerificationException
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
literal|"is not shared"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

