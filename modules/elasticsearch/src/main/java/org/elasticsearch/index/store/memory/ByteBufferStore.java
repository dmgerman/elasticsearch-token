begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.store.memory
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|store
operator|.
name|memory
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|guice
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
name|AbstractStore
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
name|SizeUnit
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
name|SizeValue
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

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|ByteBufferStore
specifier|public
class|class
name|ByteBufferStore
extends|extends
name|AbstractStore
argument_list|<
name|ByteBufferDirectory
argument_list|>
block|{
DECL|field|bufferSize
specifier|private
specifier|final
name|SizeValue
name|bufferSize
decl_stmt|;
DECL|field|cacheSize
specifier|private
specifier|final
name|SizeValue
name|cacheSize
decl_stmt|;
DECL|field|direct
specifier|private
specifier|final
name|boolean
name|direct
decl_stmt|;
DECL|field|warmCache
specifier|private
specifier|final
name|boolean
name|warmCache
decl_stmt|;
DECL|field|directory
specifier|private
specifier|final
name|ByteBufferDirectory
name|directory
decl_stmt|;
DECL|method|ByteBufferStore
annotation|@
name|Inject
specifier|public
name|ByteBufferStore
parameter_list|(
name|ShardId
name|shardId
parameter_list|,
annotation|@
name|IndexSettings
name|Settings
name|indexSettings
parameter_list|)
block|{
name|super
argument_list|(
name|shardId
argument_list|,
name|indexSettings
argument_list|)
expr_stmt|;
name|this
operator|.
name|bufferSize
operator|=
name|componentSettings
operator|.
name|getAsSize
argument_list|(
literal|"buffer_size"
argument_list|,
operator|new
name|SizeValue
argument_list|(
literal|100
argument_list|,
name|SizeUnit
operator|.
name|KB
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|cacheSize
operator|=
name|componentSettings
operator|.
name|getAsSize
argument_list|(
literal|"cache_size"
argument_list|,
operator|new
name|SizeValue
argument_list|(
literal|20
argument_list|,
name|SizeUnit
operator|.
name|MB
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|direct
operator|=
name|componentSettings
operator|.
name|getAsBoolean
argument_list|(
literal|"direct"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|this
operator|.
name|warmCache
operator|=
name|componentSettings
operator|.
name|getAsBoolean
argument_list|(
literal|"warm_cache"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|this
operator|.
name|directory
operator|=
operator|new
name|ByteBufferDirectory
argument_list|(
operator|(
name|int
operator|)
name|bufferSize
operator|.
name|bytes
argument_list|()
argument_list|,
operator|(
name|int
operator|)
name|cacheSize
operator|.
name|bytes
argument_list|()
argument_list|,
name|direct
argument_list|,
name|warmCache
argument_list|)
expr_stmt|;
name|logger
operator|.
name|debug
argument_list|(
literal|"Using [byte_buffer] store with buffer_size[{}], cache_size[{}], direct[{}], warm_cache[{}]"
argument_list|,
operator|new
name|Object
index|[]
block|{
name|bufferSize
block|,
name|cacheSize
block|,
name|directory
operator|.
name|isDirect
argument_list|()
block|,
name|warmCache
block|}
argument_list|)
expr_stmt|;
block|}
DECL|method|directory
annotation|@
name|Override
specifier|public
name|ByteBufferDirectory
name|directory
parameter_list|()
block|{
return|return
name|directory
return|;
block|}
comment|/**      * Its better to not use the compound format when using the Ram store.      */
DECL|method|suggestUseCompoundFile
annotation|@
name|Override
specifier|public
name|boolean
name|suggestUseCompoundFile
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
block|}
end_class

end_unit

