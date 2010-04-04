begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.store.fs
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|store
operator|.
name|fs
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
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
name|FSDirectory
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
name|LocalNodeId
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
name|settings
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
name|support
operator|.
name|ForceSyncDirectory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|lucene
operator|.
name|store
operator|.
name|SwitchDirectory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
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
name|io
operator|.
name|File
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
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|store
operator|.
name|fs
operator|.
name|FsStores
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * @author kimchy (Shay Banon)  */
end_comment

begin_class
DECL|class|SimpleFsStore
specifier|public
class|class
name|SimpleFsStore
extends|extends
name|AbstractFsStore
argument_list|<
name|Directory
argument_list|>
block|{
DECL|field|syncToDisk
specifier|private
specifier|final
name|boolean
name|syncToDisk
decl_stmt|;
DECL|field|fsDirectory
specifier|private
name|SimpleFSDirectory
name|fsDirectory
decl_stmt|;
DECL|field|directory
specifier|private
specifier|final
name|Directory
name|directory
decl_stmt|;
DECL|field|suggestUseCompoundFile
specifier|private
specifier|final
name|boolean
name|suggestUseCompoundFile
decl_stmt|;
DECL|method|SimpleFsStore
annotation|@
name|Inject
specifier|public
name|SimpleFsStore
parameter_list|(
name|ShardId
name|shardId
parameter_list|,
annotation|@
name|IndexSettings
name|Settings
name|indexSettings
parameter_list|,
name|Environment
name|environment
parameter_list|,
annotation|@
name|LocalNodeId
name|String
name|localNodeId
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|shardId
argument_list|,
name|indexSettings
argument_list|)
expr_stmt|;
comment|// by default, we don't need to sync to disk, since we use the gateway
name|this
operator|.
name|syncToDisk
operator|=
name|componentSettings
operator|.
name|getAsBoolean
argument_list|(
literal|"sync_to_disk"
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|this
operator|.
name|fsDirectory
operator|=
operator|new
name|CustomSimpleFSDirectory
argument_list|(
name|createStoreFilePath
argument_list|(
name|environment
operator|.
name|workWithClusterFile
argument_list|()
argument_list|,
name|localNodeId
argument_list|,
name|shardId
argument_list|,
name|MAIN_INDEX_SUFFIX
argument_list|)
argument_list|,
name|syncToDisk
argument_list|)
expr_stmt|;
name|SwitchDirectory
name|switchDirectory
init|=
name|buildSwitchDirectoryIfNeeded
argument_list|(
name|fsDirectory
argument_list|)
decl_stmt|;
if|if
condition|(
name|switchDirectory
operator|!=
literal|null
condition|)
block|{
name|suggestUseCompoundFile
operator|=
literal|false
expr_stmt|;
name|logger
operator|.
name|debug
argument_list|(
literal|"Using [SimpleFs] Store with path [{}], cache [true] with extensions [{}]"
argument_list|,
operator|new
name|Object
index|[]
block|{
name|fsDirectory
operator|.
name|getFile
argument_list|()
block|,
name|switchDirectory
operator|.
name|primaryExtensions
argument_list|()
block|}
argument_list|)
expr_stmt|;
name|directory
operator|=
name|switchDirectory
expr_stmt|;
block|}
else|else
block|{
name|suggestUseCompoundFile
operator|=
literal|true
expr_stmt|;
name|directory
operator|=
name|fsDirectory
expr_stmt|;
name|logger
operator|.
name|debug
argument_list|(
literal|"Using [SimpleFs] Store with path [{}]"
argument_list|,
name|fsDirectory
operator|.
name|getFile
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|fsDirectory
annotation|@
name|Override
specifier|public
name|FSDirectory
name|fsDirectory
parameter_list|()
block|{
return|return
name|fsDirectory
return|;
block|}
DECL|method|directory
annotation|@
name|Override
specifier|public
name|Directory
name|directory
parameter_list|()
block|{
return|return
name|directory
return|;
block|}
DECL|method|suggestUseCompoundFile
annotation|@
name|Override
specifier|public
name|boolean
name|suggestUseCompoundFile
parameter_list|()
block|{
return|return
name|suggestUseCompoundFile
return|;
block|}
DECL|class|CustomSimpleFSDirectory
specifier|private
specifier|static
class|class
name|CustomSimpleFSDirectory
extends|extends
name|SimpleFSDirectory
implements|implements
name|ForceSyncDirectory
block|{
DECL|field|syncToDisk
specifier|private
specifier|final
name|boolean
name|syncToDisk
decl_stmt|;
DECL|method|CustomSimpleFSDirectory
specifier|private
name|CustomSimpleFSDirectory
parameter_list|(
name|File
name|path
parameter_list|,
name|boolean
name|syncToDisk
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|path
argument_list|)
expr_stmt|;
name|this
operator|.
name|syncToDisk
operator|=
name|syncToDisk
expr_stmt|;
block|}
DECL|method|sync
annotation|@
name|Override
specifier|public
name|void
name|sync
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|syncToDisk
condition|)
block|{
return|return;
block|}
name|super
operator|.
name|sync
argument_list|(
name|name
argument_list|)
expr_stmt|;
block|}
DECL|method|forceSync
annotation|@
name|Override
specifier|public
name|void
name|forceSync
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|sync
argument_list|(
name|name
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

