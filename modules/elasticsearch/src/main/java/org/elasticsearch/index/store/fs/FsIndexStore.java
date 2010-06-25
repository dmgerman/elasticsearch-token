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
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableMap
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
name|collect
operator|.
name|Maps
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
name|ByteSizeValue
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
name|service
operator|.
name|IndexService
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
name|AbstractIndexStore
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
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|FsIndexStore
specifier|public
specifier|abstract
class|class
name|FsIndexStore
extends|extends
name|AbstractIndexStore
block|{
DECL|field|location
specifier|private
specifier|final
name|File
name|location
decl_stmt|;
DECL|method|FsIndexStore
specifier|public
name|FsIndexStore
parameter_list|(
name|Index
name|index
parameter_list|,
annotation|@
name|IndexSettings
name|Settings
name|indexSettings
parameter_list|,
name|IndexService
name|indexService
parameter_list|,
name|NodeEnvironment
name|nodeEnv
parameter_list|)
block|{
name|super
argument_list|(
name|index
argument_list|,
name|indexSettings
argument_list|,
name|indexService
argument_list|)
expr_stmt|;
name|this
operator|.
name|location
operator|=
operator|new
name|File
argument_list|(
operator|new
name|File
argument_list|(
name|nodeEnv
operator|.
name|nodeFile
argument_list|()
argument_list|,
literal|"indices"
argument_list|)
argument_list|,
name|index
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|location
operator|.
name|exists
argument_list|()
condition|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|5
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|location
operator|.
name|mkdirs
argument_list|()
condition|)
block|{
break|break;
block|}
block|}
block|}
block|}
DECL|method|persistent
annotation|@
name|Override
specifier|public
name|boolean
name|persistent
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
DECL|method|backingStoreTotalSpace
annotation|@
name|Override
specifier|public
name|ByteSizeValue
name|backingStoreTotalSpace
parameter_list|()
block|{
name|long
name|totalSpace
init|=
name|location
operator|.
name|getTotalSpace
argument_list|()
decl_stmt|;
if|if
condition|(
name|totalSpace
operator|==
literal|0
condition|)
block|{
name|totalSpace
operator|=
operator|-
literal|1
expr_stmt|;
block|}
return|return
operator|new
name|ByteSizeValue
argument_list|(
name|totalSpace
argument_list|)
return|;
block|}
DECL|method|backingStoreFreeSpace
annotation|@
name|Override
specifier|public
name|ByteSizeValue
name|backingStoreFreeSpace
parameter_list|()
block|{
name|long
name|usableSpace
init|=
name|location
operator|.
name|getUsableSpace
argument_list|()
decl_stmt|;
if|if
condition|(
name|usableSpace
operator|==
literal|0
condition|)
block|{
name|usableSpace
operator|=
operator|-
literal|1
expr_stmt|;
block|}
return|return
operator|new
name|ByteSizeValue
argument_list|(
name|usableSpace
argument_list|)
return|;
block|}
DECL|method|listUnallocatedStoreMetaData
annotation|@
name|Override
specifier|protected
name|StoreFilesMetaData
name|listUnallocatedStoreMetaData
parameter_list|(
name|ShardId
name|shardId
parameter_list|)
throws|throws
name|IOException
block|{
name|File
name|shardIndexLocation
init|=
name|shardIndexLocation
argument_list|(
name|shardId
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|shardIndexLocation
operator|.
name|exists
argument_list|()
condition|)
block|{
return|return
operator|new
name|StoreFilesMetaData
argument_list|(
literal|false
argument_list|,
name|ImmutableMap
operator|.
expr|<
name|String
argument_list|,
name|StoreFileMetaData
operator|>
name|of
argument_list|()
argument_list|)
return|;
block|}
name|Map
argument_list|<
name|String
argument_list|,
name|StoreFileMetaData
argument_list|>
name|files
init|=
name|Maps
operator|.
name|newHashMap
argument_list|()
decl_stmt|;
for|for
control|(
name|File
name|file
range|:
name|shardIndexLocation
operator|.
name|listFiles
argument_list|()
control|)
block|{
name|files
operator|.
name|put
argument_list|(
name|file
operator|.
name|getName
argument_list|()
argument_list|,
operator|new
name|StoreFileMetaData
argument_list|(
name|file
operator|.
name|getName
argument_list|()
argument_list|,
name|file
operator|.
name|length
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|StoreFilesMetaData
argument_list|(
literal|false
argument_list|,
name|files
argument_list|)
return|;
block|}
DECL|method|location
specifier|public
name|File
name|location
parameter_list|()
block|{
return|return
name|location
return|;
block|}
DECL|method|shardIndexLocation
specifier|public
name|File
name|shardIndexLocation
parameter_list|(
name|ShardId
name|shardId
parameter_list|)
block|{
return|return
operator|new
name|File
argument_list|(
operator|new
name|File
argument_list|(
name|location
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|shardId
operator|.
name|id
argument_list|()
argument_list|)
argument_list|)
argument_list|,
literal|"index"
argument_list|)
return|;
block|}
block|}
end_class

end_unit

