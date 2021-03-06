begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.test.store
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|test
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
name|logging
operator|.
name|log4j
operator|.
name|Logger
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
name|Nullable
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
name|logging
operator|.
name|Loggers
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
name|Setting
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
name|Setting
operator|.
name|Property
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
name|IndexEventListener
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
name|index
operator|.
name|shard
operator|.
name|IndexShardState
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
name|IndexStore
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
name|java
operator|.
name|util
operator|.
name|Arrays
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
name|EnumSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|IdentityHashMap
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

begin_class
DECL|class|MockFSIndexStore
specifier|public
class|class
name|MockFSIndexStore
extends|extends
name|IndexStore
block|{
DECL|field|INDEX_CHECK_INDEX_ON_CLOSE_SETTING
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|Boolean
argument_list|>
name|INDEX_CHECK_INDEX_ON_CLOSE_SETTING
init|=
name|Setting
operator|.
name|boolSetting
argument_list|(
literal|"index.store.mock.check_index_on_close"
argument_list|,
literal|true
argument_list|,
name|Property
operator|.
name|IndexScope
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|)
decl_stmt|;
DECL|class|TestPlugin
specifier|public
specifier|static
class|class
name|TestPlugin
extends|extends
name|Plugin
block|{
annotation|@
name|Override
DECL|method|additionalSettings
specifier|public
name|Settings
name|additionalSettings
parameter_list|()
block|{
return|return
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
literal|"mock"
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|getSettings
specifier|public
name|List
argument_list|<
name|Setting
argument_list|<
name|?
argument_list|>
argument_list|>
name|getSettings
parameter_list|()
block|{
return|return
name|Arrays
operator|.
name|asList
argument_list|(
name|INDEX_CHECK_INDEX_ON_CLOSE_SETTING
argument_list|,
name|MockFSDirectoryService
operator|.
name|CRASH_INDEX_SETTING
argument_list|,
name|MockFSDirectoryService
operator|.
name|RANDOM_IO_EXCEPTION_RATE_SETTING
argument_list|,
name|MockFSDirectoryService
operator|.
name|RANDOM_PREVENT_DOUBLE_WRITE_SETTING
argument_list|,
name|MockFSDirectoryService
operator|.
name|RANDOM_NO_DELETE_OPEN_FILE_SETTING
argument_list|,
name|MockFSDirectoryService
operator|.
name|RANDOM_IO_EXCEPTION_RATE_ON_OPEN_SETTING
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|onIndexModule
specifier|public
name|void
name|onIndexModule
parameter_list|(
name|IndexModule
name|indexModule
parameter_list|)
block|{
name|Settings
name|indexSettings
init|=
name|indexModule
operator|.
name|getSettings
argument_list|()
decl_stmt|;
if|if
condition|(
literal|"mock"
operator|.
name|equals
argument_list|(
name|indexSettings
operator|.
name|get
argument_list|(
name|IndexModule
operator|.
name|INDEX_STORE_TYPE_SETTING
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
condition|)
block|{
if|if
condition|(
name|INDEX_CHECK_INDEX_ON_CLOSE_SETTING
operator|.
name|get
argument_list|(
name|indexSettings
argument_list|)
condition|)
block|{
name|indexModule
operator|.
name|addIndexEventListener
argument_list|(
operator|new
name|Listener
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|indexModule
operator|.
name|addIndexStore
argument_list|(
literal|"mock"
argument_list|,
name|MockFSIndexStore
operator|::
operator|new
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|MockFSIndexStore
name|MockFSIndexStore
parameter_list|(
name|IndexSettings
name|indexSettings
parameter_list|)
block|{
name|super
argument_list|(
name|indexSettings
argument_list|)
expr_stmt|;
block|}
DECL|method|newDirectoryService
specifier|public
name|DirectoryService
name|newDirectoryService
parameter_list|(
name|ShardPath
name|path
parameter_list|)
block|{
return|return
operator|new
name|MockFSDirectoryService
argument_list|(
name|indexSettings
argument_list|,
name|this
argument_list|,
name|path
argument_list|)
return|;
block|}
DECL|field|validCheckIndexStates
specifier|private
specifier|static
specifier|final
name|EnumSet
argument_list|<
name|IndexShardState
argument_list|>
name|validCheckIndexStates
init|=
name|EnumSet
operator|.
name|of
argument_list|(
name|IndexShardState
operator|.
name|STARTED
argument_list|,
name|IndexShardState
operator|.
name|RELOCATED
argument_list|,
name|IndexShardState
operator|.
name|POST_RECOVERY
argument_list|)
decl_stmt|;
DECL|class|Listener
specifier|private
specifier|static
specifier|final
class|class
name|Listener
implements|implements
name|IndexEventListener
block|{
DECL|field|shardSet
specifier|private
specifier|final
name|Map
argument_list|<
name|IndexShard
argument_list|,
name|Boolean
argument_list|>
name|shardSet
init|=
name|Collections
operator|.
name|synchronizedMap
argument_list|(
operator|new
name|IdentityHashMap
argument_list|<>
argument_list|()
argument_list|)
decl_stmt|;
annotation|@
name|Override
DECL|method|afterIndexShardClosed
specifier|public
name|void
name|afterIndexShardClosed
parameter_list|(
name|ShardId
name|shardId
parameter_list|,
annotation|@
name|Nullable
name|IndexShard
name|indexShard
parameter_list|,
name|Settings
name|indexSettings
parameter_list|)
block|{
if|if
condition|(
name|indexShard
operator|!=
literal|null
condition|)
block|{
name|Boolean
name|remove
init|=
name|shardSet
operator|.
name|remove
argument_list|(
name|indexShard
argument_list|)
decl_stmt|;
if|if
condition|(
name|remove
operator|==
name|Boolean
operator|.
name|TRUE
condition|)
block|{
name|Logger
name|logger
init|=
name|Loggers
operator|.
name|getLogger
argument_list|(
name|getClass
argument_list|()
argument_list|,
name|indexShard
operator|.
name|indexSettings
argument_list|()
operator|.
name|getSettings
argument_list|()
argument_list|,
name|indexShard
operator|.
name|shardId
argument_list|()
argument_list|)
decl_stmt|;
name|MockFSDirectoryService
operator|.
name|checkIndex
argument_list|(
name|logger
argument_list|,
name|indexShard
operator|.
name|store
argument_list|()
argument_list|,
name|indexShard
operator|.
name|shardId
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
DECL|method|indexShardStateChanged
specifier|public
name|void
name|indexShardStateChanged
parameter_list|(
name|IndexShard
name|indexShard
parameter_list|,
annotation|@
name|Nullable
name|IndexShardState
name|previousState
parameter_list|,
name|IndexShardState
name|currentState
parameter_list|,
annotation|@
name|Nullable
name|String
name|reason
parameter_list|)
block|{
if|if
condition|(
name|currentState
operator|==
name|IndexShardState
operator|.
name|CLOSED
operator|&&
name|validCheckIndexStates
operator|.
name|contains
argument_list|(
name|previousState
argument_list|)
condition|)
block|{
name|shardSet
operator|.
name|put
argument_list|(
name|indexShard
argument_list|,
name|Boolean
operator|.
name|TRUE
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

