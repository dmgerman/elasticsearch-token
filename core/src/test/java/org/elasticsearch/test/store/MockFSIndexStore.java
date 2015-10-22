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
name|logging
operator|.
name|ESLogger
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
name|shard
operator|.
name|*
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
name|index
operator|.
name|store
operator|.
name|IndexStoreModule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|IndicesService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|store
operator|.
name|IndicesStore
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
name|*
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
DECL|field|CHECK_INDEX_ON_CLOSE
specifier|public
specifier|static
specifier|final
name|String
name|CHECK_INDEX_ON_CLOSE
init|=
literal|"index.store.mock.check_index_on_close"
decl_stmt|;
DECL|field|indicesService
specifier|private
specifier|final
name|IndicesService
name|indicesService
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
DECL|method|name
specifier|public
name|String
name|name
parameter_list|()
block|{
return|return
literal|"mock-index-store"
return|;
block|}
annotation|@
name|Override
DECL|method|description
specifier|public
name|String
name|description
parameter_list|()
block|{
return|return
literal|"a mock index store for testing"
return|;
block|}
DECL|method|onModule
specifier|public
name|void
name|onModule
parameter_list|(
name|IndexStoreModule
name|indexStoreModule
parameter_list|)
block|{
name|indexStoreModule
operator|.
name|addIndexStore
argument_list|(
literal|"mock"
argument_list|,
name|MockFSIndexStore
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
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
name|IndexStoreModule
operator|.
name|STORE_TYPE
argument_list|,
literal|"mock"
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
DECL|method|onModule
specifier|public
name|void
name|onModule
parameter_list|(
name|IndexModule
name|module
parameter_list|)
block|{
name|Settings
name|indexSettings
init|=
name|module
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
name|IndexStoreModule
operator|.
name|STORE_TYPE
argument_list|)
argument_list|)
condition|)
block|{
if|if
condition|(
name|indexSettings
operator|.
name|getAsBoolean
argument_list|(
name|CHECK_INDEX_ON_CLOSE
argument_list|,
literal|true
argument_list|)
condition|)
block|{
name|module
operator|.
name|addIndexEventListener
argument_list|(
operator|new
name|Listener
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
annotation|@
name|Inject
DECL|method|MockFSIndexStore
specifier|public
name|MockFSIndexStore
parameter_list|(
name|IndexSettings
name|indexSettings
parameter_list|,
name|IndicesStore
name|indicesStore
parameter_list|,
name|IndicesService
name|indicesService
parameter_list|)
block|{
name|super
argument_list|(
name|indexSettings
argument_list|,
name|indicesStore
argument_list|)
expr_stmt|;
name|this
operator|.
name|indicesService
operator|=
name|indicesService
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
name|indicesService
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
name|ESLogger
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
operator|&&
name|IndexMetaData
operator|.
name|isOnSharedFilesystem
argument_list|(
name|indexShard
operator|.
name|indexSettings
argument_list|()
argument_list|)
operator|==
literal|false
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

