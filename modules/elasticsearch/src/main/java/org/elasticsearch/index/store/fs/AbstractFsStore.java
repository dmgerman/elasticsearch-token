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
name|util
operator|.
name|gcommon
operator|.
name|collect
operator|.
name|ImmutableSet
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
name|memory
operator|.
name|ByteBufferDirectory
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
name|memory
operator|.
name|HeapDirectory
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
name|IOException
import|;
end_import

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|AbstractFsStore
specifier|public
specifier|abstract
class|class
name|AbstractFsStore
parameter_list|<
name|T
extends|extends
name|Directory
parameter_list|>
extends|extends
name|AbstractStore
argument_list|<
name|T
argument_list|>
block|{
DECL|method|AbstractFsStore
specifier|public
name|AbstractFsStore
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
block|}
DECL|method|fullDelete
annotation|@
name|Override
specifier|public
name|void
name|fullDelete
parameter_list|()
throws|throws
name|IOException
block|{
name|FileSystemUtils
operator|.
name|deleteRecursively
argument_list|(
name|fsDirectory
argument_list|()
operator|.
name|getFile
argument_list|()
argument_list|)
expr_stmt|;
comment|// if we are the last ones, delete also the actual index
name|String
index|[]
name|list
init|=
name|fsDirectory
argument_list|()
operator|.
name|getFile
argument_list|()
operator|.
name|getParentFile
argument_list|()
operator|.
name|list
argument_list|()
decl_stmt|;
if|if
condition|(
name|list
operator|==
literal|null
operator|||
name|list
operator|.
name|length
operator|==
literal|0
condition|)
block|{
name|FileSystemUtils
operator|.
name|deleteRecursively
argument_list|(
name|fsDirectory
argument_list|()
operator|.
name|getFile
argument_list|()
operator|.
name|getParentFile
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|fsDirectory
specifier|public
specifier|abstract
name|FSDirectory
name|fsDirectory
parameter_list|()
function_decl|;
DECL|method|buildSwitchDirectoryIfNeeded
specifier|protected
name|SwitchDirectory
name|buildSwitchDirectoryIfNeeded
parameter_list|(
name|Directory
name|fsDirectory
parameter_list|)
block|{
name|boolean
name|cache
init|=
name|componentSettings
operator|.
name|getAsBoolean
argument_list|(
literal|"memory.enabled"
argument_list|,
literal|false
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|cache
condition|)
block|{
return|return
literal|null
return|;
block|}
name|SizeValue
name|bufferSize
init|=
name|componentSettings
operator|.
name|getAsSize
argument_list|(
literal|"memory.buffer_size"
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
decl_stmt|;
name|SizeValue
name|cacheSize
init|=
name|componentSettings
operator|.
name|getAsSize
argument_list|(
literal|"memory.cache_size"
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
decl_stmt|;
name|boolean
name|direct
init|=
name|componentSettings
operator|.
name|getAsBoolean
argument_list|(
literal|"memory.direct"
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|boolean
name|warmCache
init|=
name|componentSettings
operator|.
name|getAsBoolean
argument_list|(
literal|"memory.warm_cache"
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|Directory
name|memDir
decl_stmt|;
if|if
condition|(
name|direct
condition|)
block|{
name|memDir
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
literal|true
argument_list|,
name|warmCache
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|memDir
operator|=
operator|new
name|HeapDirectory
argument_list|(
name|bufferSize
argument_list|,
name|cacheSize
argument_list|,
name|warmCache
argument_list|)
expr_stmt|;
block|}
comment|// see http://lucene.apache.org/java/3_0_1/fileformats.html
name|String
index|[]
name|primaryExtensions
init|=
name|componentSettings
operator|.
name|getAsArray
argument_list|(
literal|"memory.extensions"
argument_list|,
operator|new
name|String
index|[]
block|{
literal|""
block|,
literal|"del"
block|,
literal|"gen"
block|}
argument_list|)
decl_stmt|;
return|return
operator|new
name|SwitchDirectory
argument_list|(
name|ImmutableSet
operator|.
name|of
argument_list|(
name|primaryExtensions
argument_list|)
argument_list|,
name|memDir
argument_list|,
name|fsDirectory
argument_list|,
literal|true
argument_list|)
return|;
block|}
block|}
end_class

end_unit

