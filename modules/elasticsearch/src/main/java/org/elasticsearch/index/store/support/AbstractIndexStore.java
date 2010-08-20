begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.store.support
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|store
operator|.
name|support
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
name|AbstractIndexComponent
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
name|shard
operator|.
name|service
operator|.
name|InternalIndexShard
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
name|StoreFileMetaData
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

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|AbstractIndexStore
specifier|public
specifier|abstract
class|class
name|AbstractIndexStore
extends|extends
name|AbstractIndexComponent
implements|implements
name|IndexStore
block|{
DECL|field|indexService
specifier|protected
specifier|final
name|IndexService
name|indexService
decl_stmt|;
DECL|method|AbstractIndexStore
specifier|protected
name|AbstractIndexStore
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
parameter_list|)
block|{
name|super
argument_list|(
name|index
argument_list|,
name|indexSettings
argument_list|)
expr_stmt|;
name|this
operator|.
name|indexService
operator|=
name|indexService
expr_stmt|;
block|}
DECL|method|deleteUnallocated
annotation|@
name|Override
specifier|public
name|void
name|deleteUnallocated
parameter_list|(
name|ShardId
name|shardId
parameter_list|)
throws|throws
name|IOException
block|{
comment|// do nothing here...
block|}
DECL|method|listStoreMetaData
annotation|@
name|Override
specifier|public
name|StoreFilesMetaData
name|listStoreMetaData
parameter_list|(
name|ShardId
name|shardId
parameter_list|)
throws|throws
name|IOException
block|{
name|InternalIndexShard
name|indexShard
init|=
operator|(
name|InternalIndexShard
operator|)
name|indexService
operator|.
name|shard
argument_list|(
name|shardId
operator|.
name|id
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|indexShard
operator|==
literal|null
condition|)
block|{
return|return
name|listUnallocatedStoreMetaData
argument_list|(
name|shardId
argument_list|)
return|;
block|}
else|else
block|{
return|return
operator|new
name|StoreFilesMetaData
argument_list|(
literal|true
argument_list|,
name|shardId
argument_list|,
name|indexShard
operator|.
name|store
argument_list|()
operator|.
name|listWithMd5
argument_list|()
argument_list|)
return|;
block|}
block|}
DECL|method|listUnallocatedStoreMetaData
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
return|return
operator|new
name|StoreFilesMetaData
argument_list|(
literal|false
argument_list|,
name|shardId
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
block|}
end_class

end_unit

