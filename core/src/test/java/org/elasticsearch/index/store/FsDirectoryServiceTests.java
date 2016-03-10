begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.store
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|store
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
name|FilterDirectory
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
name|RateLimitedFSDirectory
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
name|SimpleFSDirectory
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
name|SleepingLockWrapper
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
name|index
operator|.
name|IndexModule
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
name|shard
operator|.
name|ShardPath
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

begin_class
DECL|class|FsDirectoryServiceTests
specifier|public
class|class
name|FsDirectoryServiceTests
extends|extends
name|ESTestCase
block|{
DECL|method|testHasSleepWrapperOnSharedFS
specifier|public
name|void
name|testHasSleepWrapperOnSharedFS
parameter_list|()
throws|throws
name|IOException
block|{
name|Settings
name|build
init|=
name|randomBoolean
argument_list|()
condition|?
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_SHARED_FILESYSTEM
argument_list|,
literal|true
argument_list|)
operator|.
name|build
argument_list|()
else|:
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_SHADOW_REPLICAS
argument_list|,
literal|true
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
empty_stmt|;
name|IndexSettings
name|settings
init|=
name|IndexSettingsModule
operator|.
name|newIndexSettings
argument_list|(
literal|"foo"
argument_list|,
name|build
argument_list|)
decl_stmt|;
name|IndexStoreConfig
name|config
init|=
operator|new
name|IndexStoreConfig
argument_list|(
name|build
argument_list|)
decl_stmt|;
name|IndexStore
name|store
init|=
operator|new
name|IndexStore
argument_list|(
name|settings
argument_list|,
name|config
argument_list|)
decl_stmt|;
name|Path
name|tempDir
init|=
name|createTempDir
argument_list|()
operator|.
name|resolve
argument_list|(
literal|"foo"
argument_list|)
operator|.
name|resolve
argument_list|(
literal|"0"
argument_list|)
decl_stmt|;
name|Files
operator|.
name|createDirectories
argument_list|(
name|tempDir
argument_list|)
expr_stmt|;
name|ShardPath
name|path
init|=
operator|new
name|ShardPath
argument_list|(
literal|false
argument_list|,
name|tempDir
argument_list|,
name|tempDir
argument_list|,
name|settings
operator|.
name|getUUID
argument_list|()
argument_list|,
operator|new
name|ShardId
argument_list|(
name|settings
operator|.
name|getIndex
argument_list|()
argument_list|,
literal|0
argument_list|)
argument_list|)
decl_stmt|;
name|FsDirectoryService
name|fsDirectoryService
init|=
operator|new
name|FsDirectoryService
argument_list|(
name|settings
argument_list|,
name|store
argument_list|,
name|path
argument_list|)
decl_stmt|;
name|Directory
name|directory
init|=
name|fsDirectoryService
operator|.
name|newDirectory
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|directory
operator|instanceof
name|RateLimitedFSDirectory
argument_list|)
expr_stmt|;
name|RateLimitedFSDirectory
name|rateLimitingDirectory
init|=
operator|(
name|RateLimitedFSDirectory
operator|)
name|directory
decl_stmt|;
name|Directory
name|delegate
init|=
name|rateLimitingDirectory
operator|.
name|getDelegate
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|delegate
operator|.
name|getClass
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
name|delegate
operator|instanceof
name|SleepingLockWrapper
argument_list|)
expr_stmt|;
block|}
DECL|method|testHasNoSleepWrapperOnNormalFS
specifier|public
name|void
name|testHasNoSleepWrapperOnNormalFS
parameter_list|()
throws|throws
name|IOException
block|{
name|Settings
name|build
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|IndexModule
operator|.
name|INDEX_STORE_TYPE_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
literal|"simplefs"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|IndexSettings
name|settings
init|=
name|IndexSettingsModule
operator|.
name|newIndexSettings
argument_list|(
literal|"foo"
argument_list|,
name|build
argument_list|)
decl_stmt|;
name|IndexStoreConfig
name|config
init|=
operator|new
name|IndexStoreConfig
argument_list|(
name|build
argument_list|)
decl_stmt|;
name|IndexStore
name|store
init|=
operator|new
name|IndexStore
argument_list|(
name|settings
argument_list|,
name|config
argument_list|)
decl_stmt|;
name|Path
name|tempDir
init|=
name|createTempDir
argument_list|()
operator|.
name|resolve
argument_list|(
literal|"foo"
argument_list|)
operator|.
name|resolve
argument_list|(
literal|"0"
argument_list|)
decl_stmt|;
name|Files
operator|.
name|createDirectories
argument_list|(
name|tempDir
argument_list|)
expr_stmt|;
name|ShardPath
name|path
init|=
operator|new
name|ShardPath
argument_list|(
literal|false
argument_list|,
name|tempDir
argument_list|,
name|tempDir
argument_list|,
name|settings
operator|.
name|getUUID
argument_list|()
argument_list|,
operator|new
name|ShardId
argument_list|(
name|settings
operator|.
name|getIndex
argument_list|()
argument_list|,
literal|0
argument_list|)
argument_list|)
decl_stmt|;
name|FsDirectoryService
name|fsDirectoryService
init|=
operator|new
name|FsDirectoryService
argument_list|(
name|settings
argument_list|,
name|store
argument_list|,
name|path
argument_list|)
decl_stmt|;
name|Directory
name|directory
init|=
name|fsDirectoryService
operator|.
name|newDirectory
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|directory
operator|instanceof
name|RateLimitedFSDirectory
argument_list|)
expr_stmt|;
name|RateLimitedFSDirectory
name|rateLimitingDirectory
init|=
operator|(
name|RateLimitedFSDirectory
operator|)
name|directory
decl_stmt|;
name|Directory
name|delegate
init|=
name|rateLimitingDirectory
operator|.
name|getDelegate
argument_list|()
decl_stmt|;
name|assertFalse
argument_list|(
name|delegate
operator|instanceof
name|SleepingLockWrapper
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|delegate
operator|instanceof
name|SimpleFSDirectory
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit
