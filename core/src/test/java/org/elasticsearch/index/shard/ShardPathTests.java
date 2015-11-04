begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.shard
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|shard
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
name|NodeEnvironment
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
name|Collections
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
name|is
import|;
end_import

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|ShardPathTests
specifier|public
class|class
name|ShardPathTests
extends|extends
name|ESTestCase
block|{
DECL|method|testLoadShardPath
specifier|public
name|void
name|testLoadShardPath
parameter_list|()
throws|throws
name|IOException
block|{
try|try
init|(
specifier|final
name|NodeEnvironment
name|env
init|=
name|newNodeEnvironment
argument_list|(
name|settingsBuilder
argument_list|()
operator|.
name|build
argument_list|()
argument_list|)
init|)
block|{
name|Settings
operator|.
name|Builder
name|builder
init|=
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_INDEX_UUID
argument_list|,
literal|"0xDEADBEEF"
argument_list|)
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_VERSION_CREATED
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
decl_stmt|;
name|Settings
name|settings
init|=
name|builder
operator|.
name|build
argument_list|()
decl_stmt|;
name|ShardId
name|shardId
init|=
operator|new
name|ShardId
argument_list|(
literal|"foo"
argument_list|,
literal|0
argument_list|)
decl_stmt|;
name|Path
index|[]
name|paths
init|=
name|env
operator|.
name|availableShardPaths
argument_list|(
name|shardId
argument_list|)
decl_stmt|;
name|Path
name|path
init|=
name|randomFrom
argument_list|(
name|paths
argument_list|)
decl_stmt|;
name|ShardStateMetaData
operator|.
name|FORMAT
operator|.
name|write
argument_list|(
operator|new
name|ShardStateMetaData
argument_list|(
literal|2
argument_list|,
literal|true
argument_list|,
literal|"0xDEADBEEF"
argument_list|)
argument_list|,
literal|2
argument_list|,
name|path
argument_list|)
expr_stmt|;
name|ShardPath
name|shardPath
init|=
name|ShardPath
operator|.
name|loadShardPath
argument_list|(
name|logger
argument_list|,
name|env
argument_list|,
name|shardId
argument_list|,
name|IndexSettingsModule
operator|.
name|newIndexSettings
argument_list|(
name|shardId
operator|.
name|index
argument_list|()
argument_list|,
name|settings
argument_list|,
name|Collections
operator|.
name|EMPTY_LIST
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|path
argument_list|,
name|shardPath
operator|.
name|getDataPath
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"0xDEADBEEF"
argument_list|,
name|shardPath
operator|.
name|getIndexUUID
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"foo"
argument_list|,
name|shardPath
operator|.
name|getShardId
argument_list|()
operator|.
name|getIndex
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|path
operator|.
name|resolve
argument_list|(
literal|"translog"
argument_list|)
argument_list|,
name|shardPath
operator|.
name|resolveTranslog
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|path
operator|.
name|resolve
argument_list|(
literal|"index"
argument_list|)
argument_list|,
name|shardPath
operator|.
name|resolveIndex
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testFailLoadShardPathOnMultiState
specifier|public
name|void
name|testFailLoadShardPathOnMultiState
parameter_list|()
throws|throws
name|IOException
block|{
try|try
init|(
specifier|final
name|NodeEnvironment
name|env
init|=
name|newNodeEnvironment
argument_list|(
name|settingsBuilder
argument_list|()
operator|.
name|build
argument_list|()
argument_list|)
init|)
block|{
name|Settings
operator|.
name|Builder
name|builder
init|=
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_INDEX_UUID
argument_list|,
literal|"0xDEADBEEF"
argument_list|)
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_VERSION_CREATED
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
decl_stmt|;
name|Settings
name|settings
init|=
name|builder
operator|.
name|build
argument_list|()
decl_stmt|;
name|ShardId
name|shardId
init|=
operator|new
name|ShardId
argument_list|(
literal|"foo"
argument_list|,
literal|0
argument_list|)
decl_stmt|;
name|Path
index|[]
name|paths
init|=
name|env
operator|.
name|availableShardPaths
argument_list|(
name|shardId
argument_list|)
decl_stmt|;
name|assumeTrue
argument_list|(
literal|"This test tests multi data.path but we only got one"
argument_list|,
name|paths
operator|.
name|length
operator|>
literal|1
argument_list|)
expr_stmt|;
name|int
name|id
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|ShardStateMetaData
operator|.
name|FORMAT
operator|.
name|write
argument_list|(
operator|new
name|ShardStateMetaData
argument_list|(
name|id
argument_list|,
literal|true
argument_list|,
literal|"0xDEADBEEF"
argument_list|)
argument_list|,
name|id
argument_list|,
name|paths
argument_list|)
expr_stmt|;
name|ShardPath
operator|.
name|loadShardPath
argument_list|(
name|logger
argument_list|,
name|env
argument_list|,
name|shardId
argument_list|,
name|IndexSettingsModule
operator|.
name|newIndexSettings
argument_list|(
name|shardId
operator|.
name|index
argument_list|()
argument_list|,
name|settings
argument_list|,
name|Collections
operator|.
name|EMPTY_LIST
argument_list|)
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Expected IllegalStateException"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalStateException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"more than one shard state found"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testFailLoadShardPathIndexUUIDMissmatch
specifier|public
name|void
name|testFailLoadShardPathIndexUUIDMissmatch
parameter_list|()
throws|throws
name|IOException
block|{
try|try
init|(
specifier|final
name|NodeEnvironment
name|env
init|=
name|newNodeEnvironment
argument_list|(
name|settingsBuilder
argument_list|()
operator|.
name|build
argument_list|()
argument_list|)
init|)
block|{
name|Settings
operator|.
name|Builder
name|builder
init|=
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_INDEX_UUID
argument_list|,
literal|"foobar"
argument_list|)
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_VERSION_CREATED
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
decl_stmt|;
name|Settings
name|settings
init|=
name|builder
operator|.
name|build
argument_list|()
decl_stmt|;
name|ShardId
name|shardId
init|=
operator|new
name|ShardId
argument_list|(
literal|"foo"
argument_list|,
literal|0
argument_list|)
decl_stmt|;
name|Path
index|[]
name|paths
init|=
name|env
operator|.
name|availableShardPaths
argument_list|(
name|shardId
argument_list|)
decl_stmt|;
name|Path
name|path
init|=
name|randomFrom
argument_list|(
name|paths
argument_list|)
decl_stmt|;
name|int
name|id
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|ShardStateMetaData
operator|.
name|FORMAT
operator|.
name|write
argument_list|(
operator|new
name|ShardStateMetaData
argument_list|(
name|id
argument_list|,
literal|true
argument_list|,
literal|"0xDEADBEEF"
argument_list|)
argument_list|,
name|id
argument_list|,
name|path
argument_list|)
expr_stmt|;
name|ShardPath
operator|.
name|loadShardPath
argument_list|(
name|logger
argument_list|,
name|env
argument_list|,
name|shardId
argument_list|,
name|IndexSettingsModule
operator|.
name|newIndexSettings
argument_list|(
name|shardId
operator|.
name|index
argument_list|()
argument_list|,
name|settings
argument_list|,
name|Collections
operator|.
name|EMPTY_LIST
argument_list|)
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Expected IllegalStateException"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalStateException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"expected: foobar on shard path"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testIllegalCustomDataPath
specifier|public
name|void
name|testIllegalCustomDataPath
parameter_list|()
block|{
specifier|final
name|Path
name|path
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
try|try
block|{
operator|new
name|ShardPath
argument_list|(
literal|true
argument_list|,
name|path
argument_list|,
name|path
argument_list|,
literal|"foo"
argument_list|,
operator|new
name|ShardId
argument_list|(
literal|"foo"
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Expected IllegalArgumentException"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"shard state path must be different to the data path when using custom data paths"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testValidCtor
specifier|public
name|void
name|testValidCtor
parameter_list|()
block|{
specifier|final
name|Path
name|path
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
name|ShardPath
name|shardPath
init|=
operator|new
name|ShardPath
argument_list|(
literal|false
argument_list|,
name|path
argument_list|,
name|path
argument_list|,
literal|"foo"
argument_list|,
operator|new
name|ShardId
argument_list|(
literal|"foo"
argument_list|,
literal|0
argument_list|)
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
name|shardPath
operator|.
name|isCustomDataPath
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|shardPath
operator|.
name|getDataPath
argument_list|()
argument_list|,
name|path
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|shardPath
operator|.
name|getShardStatePath
argument_list|()
argument_list|,
name|path
argument_list|)
expr_stmt|;
block|}
DECL|method|testGetRootPaths
specifier|public
name|void
name|testGetRootPaths
parameter_list|()
throws|throws
name|IOException
block|{
name|boolean
name|useCustomDataPath
init|=
name|randomBoolean
argument_list|()
decl_stmt|;
specifier|final
name|Settings
name|indexSetttings
decl_stmt|;
specifier|final
name|Settings
name|nodeSettings
decl_stmt|;
name|Settings
operator|.
name|Builder
name|indexSettingsBuilder
init|=
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_INDEX_UUID
argument_list|,
literal|"0xDEADBEEF"
argument_list|)
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_VERSION_CREATED
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
decl_stmt|;
specifier|final
name|Path
name|customPath
decl_stmt|;
if|if
condition|(
name|useCustomDataPath
condition|)
block|{
specifier|final
name|Path
name|path
init|=
name|createTempDir
argument_list|()
decl_stmt|;
specifier|final
name|boolean
name|includeNodeId
init|=
name|randomBoolean
argument_list|()
decl_stmt|;
name|indexSetttings
operator|=
name|indexSettingsBuilder
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_DATA_PATH
argument_list|,
literal|"custom"
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|nodeSettings
operator|=
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"path.shared_data"
argument_list|,
name|path
operator|.
name|toAbsolutePath
argument_list|()
operator|.
name|toAbsolutePath
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|NodeEnvironment
operator|.
name|ADD_NODE_ID_TO_CUSTOM_PATH
argument_list|,
name|includeNodeId
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
if|if
condition|(
name|includeNodeId
condition|)
block|{
name|customPath
operator|=
name|path
operator|.
name|resolve
argument_list|(
literal|"custom"
argument_list|)
operator|.
name|resolve
argument_list|(
literal|"0"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|customPath
operator|=
name|path
operator|.
name|resolve
argument_list|(
literal|"custom"
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|customPath
operator|=
literal|null
expr_stmt|;
name|indexSetttings
operator|=
name|indexSettingsBuilder
operator|.
name|build
argument_list|()
expr_stmt|;
name|nodeSettings
operator|=
name|Settings
operator|.
name|EMPTY
expr_stmt|;
block|}
try|try
init|(
specifier|final
name|NodeEnvironment
name|env
init|=
name|newNodeEnvironment
argument_list|(
name|nodeSettings
argument_list|)
init|)
block|{
name|ShardId
name|shardId
init|=
operator|new
name|ShardId
argument_list|(
literal|"foo"
argument_list|,
literal|0
argument_list|)
decl_stmt|;
name|Path
index|[]
name|paths
init|=
name|env
operator|.
name|availableShardPaths
argument_list|(
name|shardId
argument_list|)
decl_stmt|;
name|Path
name|path
init|=
name|randomFrom
argument_list|(
name|paths
argument_list|)
decl_stmt|;
name|ShardStateMetaData
operator|.
name|FORMAT
operator|.
name|write
argument_list|(
operator|new
name|ShardStateMetaData
argument_list|(
literal|2
argument_list|,
literal|true
argument_list|,
literal|"0xDEADBEEF"
argument_list|)
argument_list|,
literal|2
argument_list|,
name|path
argument_list|)
expr_stmt|;
name|ShardPath
name|shardPath
init|=
name|ShardPath
operator|.
name|loadShardPath
argument_list|(
name|logger
argument_list|,
name|env
argument_list|,
name|shardId
argument_list|,
name|IndexSettingsModule
operator|.
name|newIndexSettings
argument_list|(
name|shardId
operator|.
name|index
argument_list|()
argument_list|,
name|indexSetttings
argument_list|,
name|Collections
operator|.
name|EMPTY_LIST
argument_list|)
argument_list|)
decl_stmt|;
name|boolean
name|found
init|=
literal|false
decl_stmt|;
for|for
control|(
name|Path
name|p
range|:
name|env
operator|.
name|nodeDataPaths
argument_list|()
control|)
block|{
if|if
condition|(
name|p
operator|.
name|equals
argument_list|(
name|shardPath
operator|.
name|getRootStatePath
argument_list|()
argument_list|)
condition|)
block|{
name|found
operator|=
literal|true
expr_stmt|;
break|break;
block|}
block|}
name|assertTrue
argument_list|(
literal|"root state paths must be a node path but wasn't: "
operator|+
name|shardPath
operator|.
name|getRootStatePath
argument_list|()
argument_list|,
name|found
argument_list|)
expr_stmt|;
name|found
operator|=
literal|false
expr_stmt|;
if|if
condition|(
name|useCustomDataPath
condition|)
block|{
name|assertNotEquals
argument_list|(
name|shardPath
operator|.
name|getRootDataPath
argument_list|()
argument_list|,
name|shardPath
operator|.
name|getRootStatePath
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|customPath
argument_list|,
name|shardPath
operator|.
name|getRootDataPath
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertNull
argument_list|(
name|customPath
argument_list|)
expr_stmt|;
for|for
control|(
name|Path
name|p
range|:
name|env
operator|.
name|nodeDataPaths
argument_list|()
control|)
block|{
if|if
condition|(
name|p
operator|.
name|equals
argument_list|(
name|shardPath
operator|.
name|getRootDataPath
argument_list|()
argument_list|)
condition|)
block|{
name|found
operator|=
literal|true
expr_stmt|;
break|break;
block|}
block|}
name|assertTrue
argument_list|(
literal|"root state paths must be a node path but wasn't: "
operator|+
name|shardPath
operator|.
name|getRootDataPath
argument_list|()
argument_list|,
name|found
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

