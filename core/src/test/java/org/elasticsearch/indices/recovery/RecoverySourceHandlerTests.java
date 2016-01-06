begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.indices.recovery
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|recovery
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|document
operator|.
name|Document
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
name|document
operator|.
name|Field
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
name|document
operator|.
name|StringField
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
name|document
operator|.
name|TextField
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
name|index
operator|.
name|CorruptIndexException
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
name|index
operator|.
name|DirectoryReader
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
name|index
operator|.
name|IndexReader
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
name|index
operator|.
name|RandomIndexWriter
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
name|store
operator|.
name|BaseDirectoryWrapper
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
name|store
operator|.
name|Directory
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
name|store
operator|.
name|IOContext
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
name|util
operator|.
name|IOUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ExceptionsHelper
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
name|lucene
operator|.
name|store
operator|.
name|IndexOutputOutputStream
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
name|ClusterSettings
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
name|transport
operator|.
name|DummyTransportAddress
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
name|index
operator|.
name|store
operator|.
name|DirectoryService
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
name|store
operator|.
name|Store
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
name|store
operator|.
name|StoreFileMetaData
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
name|CorruptionUtils
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
name|DummyShardLock
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
name|ESTestCase
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
name|IndexSettingsModule
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
name|Collections
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
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicBoolean
import|;
end_import

begin_class
DECL|class|RecoverySourceHandlerTests
specifier|public
class|class
name|RecoverySourceHandlerTests
extends|extends
name|ESTestCase
block|{
DECL|field|INDEX_SETTINGS
specifier|private
specifier|static
specifier|final
name|IndexSettings
name|INDEX_SETTINGS
init|=
name|IndexSettingsModule
operator|.
name|newIndexSettings
argument_list|(
operator|new
name|Index
argument_list|(
literal|"index"
argument_list|)
argument_list|,
name|Settings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_VERSION_CREATED
argument_list|,
name|org
operator|.
name|elasticsearch
operator|.
name|Version
operator|.
name|CURRENT
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
DECL|field|shardId
specifier|private
specifier|final
name|ShardId
name|shardId
init|=
operator|new
name|ShardId
argument_list|(
name|INDEX_SETTINGS
operator|.
name|getIndex
argument_list|()
argument_list|,
literal|1
argument_list|)
decl_stmt|;
DECL|field|service
specifier|private
specifier|final
name|ClusterSettings
name|service
init|=
operator|new
name|ClusterSettings
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
name|ClusterSettings
operator|.
name|BUILT_IN_CLUSTER_SETTINGS
argument_list|)
decl_stmt|;
DECL|method|testSendFiles
specifier|public
name|void
name|testSendFiles
parameter_list|()
throws|throws
name|Throwable
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
literal|"indices.recovery.concurrent_streams"
argument_list|,
literal|1
argument_list|)
operator|.
name|put
argument_list|(
literal|"indices.recovery.concurrent_small_file_streams"
argument_list|,
literal|1
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
specifier|final
name|RecoverySettings
name|recoverySettings
init|=
operator|new
name|RecoverySettings
argument_list|(
name|settings
argument_list|,
name|service
argument_list|)
decl_stmt|;
name|StartRecoveryRequest
name|request
init|=
operator|new
name|StartRecoveryRequest
argument_list|(
name|shardId
argument_list|,
operator|new
name|DiscoveryNode
argument_list|(
literal|"b"
argument_list|,
name|DummyTransportAddress
operator|.
name|INSTANCE
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
argument_list|,
operator|new
name|DiscoveryNode
argument_list|(
literal|"b"
argument_list|,
name|DummyTransportAddress
operator|.
name|INSTANCE
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
argument_list|,
name|randomBoolean
argument_list|()
argument_list|,
literal|null
argument_list|,
name|RecoveryState
operator|.
name|Type
operator|.
name|STORE
argument_list|,
name|randomLong
argument_list|()
argument_list|)
decl_stmt|;
name|Store
name|store
init|=
name|newStore
argument_list|(
name|createTempDir
argument_list|()
argument_list|)
decl_stmt|;
name|RecoverySourceHandler
name|handler
init|=
operator|new
name|RecoverySourceHandler
argument_list|(
literal|null
argument_list|,
name|request
argument_list|,
name|recoverySettings
argument_list|,
literal|null
argument_list|,
name|logger
argument_list|)
decl_stmt|;
name|Directory
name|dir
init|=
name|store
operator|.
name|directory
argument_list|()
decl_stmt|;
name|RandomIndexWriter
name|writer
init|=
operator|new
name|RandomIndexWriter
argument_list|(
name|random
argument_list|()
argument_list|,
name|dir
argument_list|,
name|newIndexWriterConfig
argument_list|()
argument_list|)
decl_stmt|;
name|int
name|numDocs
init|=
name|randomIntBetween
argument_list|(
literal|10
argument_list|,
literal|100
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numDocs
condition|;
name|i
operator|++
control|)
block|{
name|Document
name|document
init|=
operator|new
name|Document
argument_list|()
decl_stmt|;
name|document
operator|.
name|add
argument_list|(
operator|new
name|StringField
argument_list|(
literal|"id"
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|i
argument_list|)
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|YES
argument_list|)
argument_list|)
expr_stmt|;
name|document
operator|.
name|add
argument_list|(
name|newField
argument_list|(
literal|"field"
argument_list|,
name|randomUnicodeOfCodepointLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
argument_list|,
name|TextField
operator|.
name|TYPE_STORED
argument_list|)
argument_list|)
expr_stmt|;
name|writer
operator|.
name|addDocument
argument_list|(
name|document
argument_list|)
expr_stmt|;
block|}
name|writer
operator|.
name|commit
argument_list|()
expr_stmt|;
name|Store
operator|.
name|MetadataSnapshot
name|metadata
init|=
name|store
operator|.
name|getMetadata
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|StoreFileMetaData
argument_list|>
name|metas
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|StoreFileMetaData
name|md
range|:
name|metadata
control|)
block|{
name|metas
operator|.
name|add
argument_list|(
name|md
argument_list|)
expr_stmt|;
block|}
name|Store
name|targetStore
init|=
name|newStore
argument_list|(
name|createTempDir
argument_list|()
argument_list|)
decl_stmt|;
name|handler
operator|.
name|sendFiles
argument_list|(
name|store
argument_list|,
name|metas
operator|.
name|toArray
argument_list|(
operator|new
name|StoreFileMetaData
index|[
literal|0
index|]
argument_list|)
argument_list|,
parameter_list|(
name|md
parameter_list|)
lambda|->
block|{
try|try
block|{
return|return
operator|new
name|IndexOutputOutputStream
argument_list|(
name|targetStore
operator|.
name|createVerifyingOutput
argument_list|(
name|md
operator|.
name|name
argument_list|()
argument_list|,
name|md
argument_list|,
name|IOContext
operator|.
name|DEFAULT
argument_list|)
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
name|super
operator|.
name|close
argument_list|()
expr_stmt|;
name|store
operator|.
name|directory
argument_list|()
operator|.
name|sync
argument_list|(
name|Collections
operator|.
name|singleton
argument_list|(
name|md
operator|.
name|name
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// sync otherwise MDW will mess with it
block|}
block|}
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
argument_list|)
expr_stmt|;
name|Store
operator|.
name|MetadataSnapshot
name|targetStoreMetadata
init|=
name|targetStore
operator|.
name|getMetadata
argument_list|()
decl_stmt|;
name|Store
operator|.
name|RecoveryDiff
name|recoveryDiff
init|=
name|targetStoreMetadata
operator|.
name|recoveryDiff
argument_list|(
name|metadata
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|metas
operator|.
name|size
argument_list|()
argument_list|,
name|recoveryDiff
operator|.
name|identical
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|recoveryDiff
operator|.
name|different
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|recoveryDiff
operator|.
name|missing
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|IndexReader
name|reader
init|=
name|DirectoryReader
operator|.
name|open
argument_list|(
name|targetStore
operator|.
name|directory
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|numDocs
argument_list|,
name|reader
operator|.
name|maxDoc
argument_list|()
argument_list|)
expr_stmt|;
name|IOUtils
operator|.
name|close
argument_list|(
name|reader
argument_list|,
name|writer
argument_list|,
name|store
argument_list|,
name|targetStore
argument_list|)
expr_stmt|;
block|}
DECL|method|testHandleCorruptedIndexOnSendSendFiles
specifier|public
name|void
name|testHandleCorruptedIndexOnSendSendFiles
parameter_list|()
throws|throws
name|Throwable
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
literal|"indices.recovery.concurrent_streams"
argument_list|,
literal|1
argument_list|)
operator|.
name|put
argument_list|(
literal|"indices.recovery.concurrent_small_file_streams"
argument_list|,
literal|1
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
specifier|final
name|RecoverySettings
name|recoverySettings
init|=
operator|new
name|RecoverySettings
argument_list|(
name|settings
argument_list|,
name|service
argument_list|)
decl_stmt|;
name|StartRecoveryRequest
name|request
init|=
operator|new
name|StartRecoveryRequest
argument_list|(
name|shardId
argument_list|,
operator|new
name|DiscoveryNode
argument_list|(
literal|"b"
argument_list|,
name|DummyTransportAddress
operator|.
name|INSTANCE
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
argument_list|,
operator|new
name|DiscoveryNode
argument_list|(
literal|"b"
argument_list|,
name|DummyTransportAddress
operator|.
name|INSTANCE
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
argument_list|,
name|randomBoolean
argument_list|()
argument_list|,
literal|null
argument_list|,
name|RecoveryState
operator|.
name|Type
operator|.
name|STORE
argument_list|,
name|randomLong
argument_list|()
argument_list|)
decl_stmt|;
name|Path
name|tempDir
init|=
name|createTempDir
argument_list|()
decl_stmt|;
name|Store
name|store
init|=
name|newStore
argument_list|(
name|tempDir
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|AtomicBoolean
name|failedEngine
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
name|RecoverySourceHandler
name|handler
init|=
operator|new
name|RecoverySourceHandler
argument_list|(
literal|null
argument_list|,
name|request
argument_list|,
name|recoverySettings
argument_list|,
literal|null
argument_list|,
name|logger
argument_list|)
block|{
annotation|@
name|Override
specifier|protected
name|void
name|failEngine
parameter_list|(
name|IOException
name|cause
parameter_list|)
block|{
name|assertFalse
argument_list|(
name|failedEngine
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|failedEngine
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
decl_stmt|;
name|Directory
name|dir
init|=
name|store
operator|.
name|directory
argument_list|()
decl_stmt|;
name|RandomIndexWriter
name|writer
init|=
operator|new
name|RandomIndexWriter
argument_list|(
name|random
argument_list|()
argument_list|,
name|dir
argument_list|,
name|newIndexWriterConfig
argument_list|()
argument_list|)
decl_stmt|;
name|int
name|numDocs
init|=
name|randomIntBetween
argument_list|(
literal|10
argument_list|,
literal|100
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numDocs
condition|;
name|i
operator|++
control|)
block|{
name|Document
name|document
init|=
operator|new
name|Document
argument_list|()
decl_stmt|;
name|document
operator|.
name|add
argument_list|(
operator|new
name|StringField
argument_list|(
literal|"id"
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|i
argument_list|)
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|YES
argument_list|)
argument_list|)
expr_stmt|;
name|document
operator|.
name|add
argument_list|(
name|newField
argument_list|(
literal|"field"
argument_list|,
name|randomUnicodeOfCodepointLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
argument_list|,
name|TextField
operator|.
name|TYPE_STORED
argument_list|)
argument_list|)
expr_stmt|;
name|writer
operator|.
name|addDocument
argument_list|(
name|document
argument_list|)
expr_stmt|;
block|}
name|writer
operator|.
name|commit
argument_list|()
expr_stmt|;
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
name|Store
operator|.
name|MetadataSnapshot
name|metadata
init|=
name|store
operator|.
name|getMetadata
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|StoreFileMetaData
argument_list|>
name|metas
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|StoreFileMetaData
name|md
range|:
name|metadata
control|)
block|{
name|metas
operator|.
name|add
argument_list|(
name|md
argument_list|)
expr_stmt|;
block|}
name|CorruptionUtils
operator|.
name|corruptFile
argument_list|(
name|getRandom
argument_list|()
argument_list|,
name|FileSystemUtils
operator|.
name|files
argument_list|(
name|tempDir
argument_list|,
parameter_list|(
name|p
parameter_list|)
lambda|->
operator|(
name|p
operator|.
name|getFileName
argument_list|()
operator|.
name|toString
argument_list|()
operator|.
name|equals
argument_list|(
literal|"write.lock"
argument_list|)
operator|||
name|p
operator|.
name|getFileName
argument_list|()
operator|.
name|toString
argument_list|()
operator|.
name|startsWith
argument_list|(
literal|"extra"
argument_list|)
operator|)
operator|==
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|Store
name|targetStore
init|=
name|newStore
argument_list|(
name|createTempDir
argument_list|()
argument_list|,
literal|false
argument_list|)
decl_stmt|;
try|try
block|{
name|handler
operator|.
name|sendFiles
argument_list|(
name|store
argument_list|,
name|metas
operator|.
name|toArray
argument_list|(
operator|new
name|StoreFileMetaData
index|[
literal|0
index|]
argument_list|)
argument_list|,
parameter_list|(
name|md
parameter_list|)
lambda|->
block|{
try|try
block|{
return|return
operator|new
name|IndexOutputOutputStream
argument_list|(
name|targetStore
operator|.
name|createVerifyingOutput
argument_list|(
name|md
operator|.
name|name
argument_list|()
argument_list|,
name|md
argument_list|,
name|IOContext
operator|.
name|DEFAULT
argument_list|)
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
name|super
operator|.
name|close
argument_list|()
expr_stmt|;
name|store
operator|.
name|directory
argument_list|()
operator|.
name|sync
argument_list|(
name|Collections
operator|.
name|singleton
argument_list|(
name|md
operator|.
name|name
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// sync otherwise MDW will mess with it
block|}
block|}
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"corrupted index"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ex
parameter_list|)
block|{
name|assertNotNull
argument_list|(
name|ExceptionsHelper
operator|.
name|unwrapCorruption
argument_list|(
name|ex
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|assertTrue
argument_list|(
name|failedEngine
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|IOUtils
operator|.
name|close
argument_list|(
name|store
argument_list|,
name|targetStore
argument_list|)
expr_stmt|;
block|}
DECL|method|testHandleExceptinoOnSendSendFiles
specifier|public
name|void
name|testHandleExceptinoOnSendSendFiles
parameter_list|()
throws|throws
name|Throwable
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
literal|"indices.recovery.concurrent_streams"
argument_list|,
literal|1
argument_list|)
operator|.
name|put
argument_list|(
literal|"indices.recovery.concurrent_small_file_streams"
argument_list|,
literal|1
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
specifier|final
name|RecoverySettings
name|recoverySettings
init|=
operator|new
name|RecoverySettings
argument_list|(
name|settings
argument_list|,
name|service
argument_list|)
decl_stmt|;
name|StartRecoveryRequest
name|request
init|=
operator|new
name|StartRecoveryRequest
argument_list|(
name|shardId
argument_list|,
operator|new
name|DiscoveryNode
argument_list|(
literal|"b"
argument_list|,
name|DummyTransportAddress
operator|.
name|INSTANCE
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
argument_list|,
operator|new
name|DiscoveryNode
argument_list|(
literal|"b"
argument_list|,
name|DummyTransportAddress
operator|.
name|INSTANCE
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
argument_list|,
name|randomBoolean
argument_list|()
argument_list|,
literal|null
argument_list|,
name|RecoveryState
operator|.
name|Type
operator|.
name|STORE
argument_list|,
name|randomLong
argument_list|()
argument_list|)
decl_stmt|;
name|Path
name|tempDir
init|=
name|createTempDir
argument_list|()
decl_stmt|;
name|Store
name|store
init|=
name|newStore
argument_list|(
name|tempDir
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|AtomicBoolean
name|failedEngine
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
name|RecoverySourceHandler
name|handler
init|=
operator|new
name|RecoverySourceHandler
argument_list|(
literal|null
argument_list|,
name|request
argument_list|,
name|recoverySettings
argument_list|,
literal|null
argument_list|,
name|logger
argument_list|)
block|{
annotation|@
name|Override
specifier|protected
name|void
name|failEngine
parameter_list|(
name|IOException
name|cause
parameter_list|)
block|{
name|assertFalse
argument_list|(
name|failedEngine
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|failedEngine
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
decl_stmt|;
name|Directory
name|dir
init|=
name|store
operator|.
name|directory
argument_list|()
decl_stmt|;
name|RandomIndexWriter
name|writer
init|=
operator|new
name|RandomIndexWriter
argument_list|(
name|random
argument_list|()
argument_list|,
name|dir
argument_list|,
name|newIndexWriterConfig
argument_list|()
argument_list|)
decl_stmt|;
name|int
name|numDocs
init|=
name|randomIntBetween
argument_list|(
literal|10
argument_list|,
literal|100
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numDocs
condition|;
name|i
operator|++
control|)
block|{
name|Document
name|document
init|=
operator|new
name|Document
argument_list|()
decl_stmt|;
name|document
operator|.
name|add
argument_list|(
operator|new
name|StringField
argument_list|(
literal|"id"
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|i
argument_list|)
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|YES
argument_list|)
argument_list|)
expr_stmt|;
name|document
operator|.
name|add
argument_list|(
name|newField
argument_list|(
literal|"field"
argument_list|,
name|randomUnicodeOfCodepointLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
argument_list|,
name|TextField
operator|.
name|TYPE_STORED
argument_list|)
argument_list|)
expr_stmt|;
name|writer
operator|.
name|addDocument
argument_list|(
name|document
argument_list|)
expr_stmt|;
block|}
name|writer
operator|.
name|commit
argument_list|()
expr_stmt|;
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
name|Store
operator|.
name|MetadataSnapshot
name|metadata
init|=
name|store
operator|.
name|getMetadata
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|StoreFileMetaData
argument_list|>
name|metas
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|StoreFileMetaData
name|md
range|:
name|metadata
control|)
block|{
name|metas
operator|.
name|add
argument_list|(
name|md
argument_list|)
expr_stmt|;
block|}
specifier|final
name|boolean
name|throwCorruptedIndexException
init|=
name|randomBoolean
argument_list|()
decl_stmt|;
name|Store
name|targetStore
init|=
name|newStore
argument_list|(
name|createTempDir
argument_list|()
argument_list|,
literal|false
argument_list|)
decl_stmt|;
try|try
block|{
name|handler
operator|.
name|sendFiles
argument_list|(
name|store
argument_list|,
name|metas
operator|.
name|toArray
argument_list|(
operator|new
name|StoreFileMetaData
index|[
literal|0
index|]
argument_list|)
argument_list|,
parameter_list|(
name|md
parameter_list|)
lambda|->
block|{
if|if
condition|(
name|throwCorruptedIndexException
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
operator|new
name|CorruptIndexException
argument_list|(
literal|"foo"
argument_list|,
literal|"bar"
argument_list|)
argument_list|)
throw|;
block|}
else|else
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"boom"
argument_list|)
throw|;
block|}
block|}
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"exception index"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RuntimeException
name|ex
parameter_list|)
block|{
name|assertNull
argument_list|(
name|ExceptionsHelper
operator|.
name|unwrapCorruption
argument_list|(
name|ex
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|throwCorruptedIndexException
condition|)
block|{
name|assertEquals
argument_list|(
name|ex
operator|.
name|getMessage
argument_list|()
argument_list|,
literal|"[File corruption occurred on recovery but checksums are ok]"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertEquals
argument_list|(
name|ex
operator|.
name|getMessage
argument_list|()
argument_list|,
literal|"boom"
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|CorruptIndexException
name|ex
parameter_list|)
block|{
name|fail
argument_list|(
literal|"not expected here"
argument_list|)
expr_stmt|;
block|}
name|assertFalse
argument_list|(
name|failedEngine
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|IOUtils
operator|.
name|close
argument_list|(
name|store
argument_list|,
name|targetStore
argument_list|)
expr_stmt|;
block|}
DECL|method|newStore
specifier|private
name|Store
name|newStore
parameter_list|(
name|Path
name|path
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|newStore
argument_list|(
name|path
argument_list|,
literal|true
argument_list|)
return|;
block|}
DECL|method|newStore
specifier|private
name|Store
name|newStore
parameter_list|(
name|Path
name|path
parameter_list|,
name|boolean
name|checkIndex
parameter_list|)
throws|throws
name|IOException
block|{
name|DirectoryService
name|directoryService
init|=
operator|new
name|DirectoryService
argument_list|(
name|shardId
argument_list|,
name|INDEX_SETTINGS
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|long
name|throttleTimeInNanos
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|Directory
name|newDirectory
parameter_list|()
throws|throws
name|IOException
block|{
name|BaseDirectoryWrapper
name|baseDirectoryWrapper
init|=
name|RecoverySourceHandlerTests
operator|.
name|newFSDirectory
argument_list|(
name|path
argument_list|)
decl_stmt|;
if|if
condition|(
name|checkIndex
operator|==
literal|false
condition|)
block|{
name|baseDirectoryWrapper
operator|.
name|setCheckIndexOnClose
argument_list|(
literal|false
argument_list|)
expr_stmt|;
comment|// don't run checkindex we might corrupt the index in these tests
block|}
return|return
name|baseDirectoryWrapper
return|;
block|}
block|}
decl_stmt|;
return|return
operator|new
name|Store
argument_list|(
name|shardId
argument_list|,
name|INDEX_SETTINGS
argument_list|,
name|directoryService
argument_list|,
operator|new
name|DummyShardLock
argument_list|(
name|shardId
argument_list|)
argument_list|)
return|;
block|}
block|}
end_class

end_unit

