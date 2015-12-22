begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.repositories.hdfs
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|repositories
operator|.
name|hdfs
package|;
end_package

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
import|import
name|java
operator|.
name|util
operator|.
name|Collection
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
name|snapshots
operator|.
name|create
operator|.
name|CreateSnapshotResponse
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
name|ClusterState
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
name|plugins
operator|.
name|Plugin
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
name|hdfs
operator|.
name|HdfsPlugin
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
name|SnapshotState
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
name|ESSingleNodeTestCase
import|;
end_import

begin_class
DECL|class|HdfsTests
specifier|public
class|class
name|HdfsTests
extends|extends
name|ESSingleNodeTestCase
block|{
annotation|@
name|Override
DECL|method|getPlugins
specifier|protected
name|Collection
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|Plugin
argument_list|>
argument_list|>
name|getPlugins
parameter_list|()
block|{
return|return
name|pluginList
argument_list|(
name|HdfsPlugin
operator|.
name|class
argument_list|)
return|;
block|}
DECL|method|testSimpleWorkflow
specifier|public
name|void
name|testSimpleWorkflow
parameter_list|()
block|{
name|Client
name|client
init|=
name|client
argument_list|()
decl_stmt|;
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
literal|"test-repo"
argument_list|)
operator|.
name|setType
argument_list|(
literal|"hdfs"
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
literal|"uri"
argument_list|,
literal|"hdfs:///"
argument_list|)
operator|.
name|put
argument_list|(
literal|"conf.fs.AbstractFileSystem.hdfs.impl"
argument_list|,
name|TestingFs
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
literal|"path"
argument_list|,
literal|"foo"
argument_list|)
operator|.
name|put
argument_list|(
literal|"chunk_size"
argument_list|,
name|randomIntBetween
argument_list|(
literal|100
argument_list|,
literal|1000
argument_list|)
operator|+
literal|"k"
argument_list|)
operator|.
name|put
argument_list|(
literal|"compress"
argument_list|,
name|randomBoolean
argument_list|()
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
name|createIndex
argument_list|(
literal|"test-idx-1"
argument_list|)
expr_stmt|;
name|createIndex
argument_list|(
literal|"test-idx-2"
argument_list|)
expr_stmt|;
name|createIndex
argument_list|(
literal|"test-idx-3"
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> indexing some data"
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|100
condition|;
name|i
operator|++
control|)
block|{
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test-idx-1"
argument_list|,
literal|"doc"
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|i
argument_list|)
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"foo"
argument_list|,
literal|"bar"
operator|+
name|i
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test-idx-2"
argument_list|,
literal|"doc"
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|i
argument_list|)
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"foo"
argument_list|,
literal|"bar"
operator|+
name|i
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test-idx-3"
argument_list|,
literal|"doc"
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|i
argument_list|)
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"foo"
argument_list|,
literal|"bar"
operator|+
name|i
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareRefresh
argument_list|()
operator|.
name|get
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|count
argument_list|(
name|client
argument_list|,
literal|"test-idx-1"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|100L
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|count
argument_list|(
name|client
argument_list|,
literal|"test-idx-2"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|100L
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|count
argument_list|(
name|client
argument_list|,
literal|"test-idx-3"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|100L
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> snapshot"
argument_list|)
expr_stmt|;
name|CreateSnapshotResponse
name|createSnapshotResponse
init|=
name|client
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareCreateSnapshot
argument_list|(
literal|"test-repo"
argument_list|,
literal|"test-snap"
argument_list|)
operator|.
name|setWaitForCompletion
argument_list|(
literal|true
argument_list|)
operator|.
name|setIndices
argument_list|(
literal|"test-idx-*"
argument_list|,
literal|"-test-idx-3"
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|createSnapshotResponse
operator|.
name|getSnapshotInfo
argument_list|()
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
name|createSnapshotResponse
operator|.
name|getSnapshotInfo
argument_list|()
operator|.
name|successfulShards
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|createSnapshotResponse
operator|.
name|getSnapshotInfo
argument_list|()
operator|.
name|totalShards
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|client
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareGetSnapshots
argument_list|(
literal|"test-repo"
argument_list|)
operator|.
name|setSnapshots
argument_list|(
literal|"test-snap"
argument_list|)
operator|.
name|get
argument_list|()
operator|.
name|getSnapshots
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|state
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|SnapshotState
operator|.
name|SUCCESS
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> delete some data"
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|50
condition|;
name|i
operator|++
control|)
block|{
name|client
operator|.
name|prepareDelete
argument_list|(
literal|"test-idx-1"
argument_list|,
literal|"doc"
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|i
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|50
init|;
name|i
operator|<
literal|100
condition|;
name|i
operator|++
control|)
block|{
name|client
operator|.
name|prepareDelete
argument_list|(
literal|"test-idx-2"
argument_list|,
literal|"doc"
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|i
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|100
condition|;
name|i
operator|+=
literal|2
control|)
block|{
name|client
operator|.
name|prepareDelete
argument_list|(
literal|"test-idx-3"
argument_list|,
literal|"doc"
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|i
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareRefresh
argument_list|()
operator|.
name|get
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|count
argument_list|(
name|client
argument_list|,
literal|"test-idx-1"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|50L
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|count
argument_list|(
name|client
argument_list|,
literal|"test-idx-2"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|50L
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|count
argument_list|(
name|client
argument_list|,
literal|"test-idx-3"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|50L
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> close indices"
argument_list|)
expr_stmt|;
name|client
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareClose
argument_list|(
literal|"test-idx-1"
argument_list|,
literal|"test-idx-2"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> restore all indices from the snapshot"
argument_list|)
expr_stmt|;
name|RestoreSnapshotResponse
name|restoreSnapshotResponse
init|=
name|client
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareRestoreSnapshot
argument_list|(
literal|"test-repo"
argument_list|,
literal|"test-snap"
argument_list|)
operator|.
name|setWaitForCompletion
argument_list|(
literal|true
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|restoreSnapshotResponse
operator|.
name|getRestoreInfo
argument_list|()
operator|.
name|totalShards
argument_list|()
argument_list|,
name|greaterThan
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|count
argument_list|(
name|client
argument_list|,
literal|"test-idx-1"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|100L
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|count
argument_list|(
name|client
argument_list|,
literal|"test-idx-2"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|100L
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|count
argument_list|(
name|client
argument_list|,
literal|"test-idx-3"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|50L
argument_list|)
argument_list|)
expr_stmt|;
comment|// Test restore after index deletion
name|logger
operator|.
name|info
argument_list|(
literal|"--> delete indices"
argument_list|)
expr_stmt|;
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareDelete
argument_list|(
literal|"test-idx-1"
argument_list|,
literal|"test-idx-2"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> restore one index after deletion"
argument_list|)
expr_stmt|;
name|restoreSnapshotResponse
operator|=
name|client
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareRestoreSnapshot
argument_list|(
literal|"test-repo"
argument_list|,
literal|"test-snap"
argument_list|)
operator|.
name|setWaitForCompletion
argument_list|(
literal|true
argument_list|)
operator|.
name|setIndices
argument_list|(
literal|"test-idx-*"
argument_list|,
literal|"-test-idx-2"
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|restoreSnapshotResponse
operator|.
name|getRestoreInfo
argument_list|()
operator|.
name|totalShards
argument_list|()
argument_list|,
name|greaterThan
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|count
argument_list|(
name|client
argument_list|,
literal|"test-idx-1"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|100L
argument_list|)
argument_list|)
expr_stmt|;
name|ClusterState
name|clusterState
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
name|getMetaData
argument_list|()
operator|.
name|hasIndex
argument_list|(
literal|"test-idx-1"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|clusterState
operator|.
name|getMetaData
argument_list|()
operator|.
name|hasIndex
argument_list|(
literal|"test-idx-2"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testMissingUri
specifier|public
name|void
name|testMissingUri
parameter_list|()
block|{
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
literal|"hdfs"
argument_list|)
operator|.
name|setSettings
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RepositoryException
name|e
parameter_list|)
block|{
name|assertTrue
argument_list|(
name|e
operator|.
name|getCause
argument_list|()
operator|instanceof
name|IllegalArgumentException
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|e
operator|.
name|getCause
argument_list|()
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"No 'uri' defined for hdfs"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testEmptyUri
specifier|public
name|void
name|testEmptyUri
parameter_list|()
block|{
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
literal|"hdfs"
argument_list|)
operator|.
name|setSettings
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"uri"
argument_list|,
literal|"/path"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RepositoryException
name|e
parameter_list|)
block|{
name|assertTrue
argument_list|(
name|e
operator|.
name|getCause
argument_list|()
operator|instanceof
name|IllegalArgumentException
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|e
operator|.
name|getCause
argument_list|()
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
operator|.
name|getCause
argument_list|()
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"Invalid scheme [null] specified in uri [/path]"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testNonHdfsUri
specifier|public
name|void
name|testNonHdfsUri
parameter_list|()
block|{
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
literal|"hdfs"
argument_list|)
operator|.
name|setSettings
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"uri"
argument_list|,
literal|"file:///"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RepositoryException
name|e
parameter_list|)
block|{
name|assertTrue
argument_list|(
name|e
operator|.
name|getCause
argument_list|()
operator|instanceof
name|IllegalArgumentException
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|e
operator|.
name|getCause
argument_list|()
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"Invalid scheme [file] specified in uri [file:///]"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testPathSpecifiedInHdfs
specifier|public
name|void
name|testPathSpecifiedInHdfs
parameter_list|()
block|{
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
literal|"hdfs"
argument_list|)
operator|.
name|setSettings
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"uri"
argument_list|,
literal|"hdfs:///some/path"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RepositoryException
name|e
parameter_list|)
block|{
name|assertTrue
argument_list|(
name|e
operator|.
name|getCause
argument_list|()
operator|instanceof
name|IllegalArgumentException
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|e
operator|.
name|getCause
argument_list|()
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"Use 'path' option to specify a path [/some/path]"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testMissingPath
specifier|public
name|void
name|testMissingPath
parameter_list|()
block|{
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
literal|"hdfs"
argument_list|)
operator|.
name|setSettings
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"uri"
argument_list|,
literal|"hdfs:///"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RepositoryException
name|e
parameter_list|)
block|{
name|assertTrue
argument_list|(
name|e
operator|.
name|getCause
argument_list|()
operator|instanceof
name|IllegalArgumentException
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|e
operator|.
name|getCause
argument_list|()
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"No 'path' defined for hdfs"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|count
specifier|private
name|long
name|count
parameter_list|(
name|Client
name|client
parameter_list|,
name|String
name|index
parameter_list|)
block|{
return|return
name|client
operator|.
name|prepareSearch
argument_list|(
name|index
argument_list|)
operator|.
name|setSize
argument_list|(
literal|0
argument_list|)
operator|.
name|get
argument_list|()
operator|.
name|getHits
argument_list|()
operator|.
name|totalHits
argument_list|()
return|;
block|}
block|}
end_class

end_unit

