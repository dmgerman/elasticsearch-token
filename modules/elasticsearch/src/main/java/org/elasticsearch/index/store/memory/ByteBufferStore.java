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
name|bytebuffer
operator|.
name|ByteBufferAllocator
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
name|bytebuffer
operator|.
name|ByteBufferDirectory
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
name|bytebuffer
operator|.
name|ByteBufferFile
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cache
operator|.
name|memory
operator|.
name|ByteBufferCache
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
name|support
operator|.
name|AbstractStore
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|FileNotFoundException
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
DECL|class|ByteBufferStore
specifier|public
class|class
name|ByteBufferStore
extends|extends
name|AbstractStore
block|{
DECL|field|bbDirectory
specifier|private
specifier|final
name|CustomByteBufferDirectory
name|bbDirectory
decl_stmt|;
DECL|field|directory
specifier|private
specifier|final
name|Directory
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
parameter_list|,
name|IndexStore
name|indexStore
parameter_list|,
name|ByteBufferCache
name|byteBufferCache
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|shardId
argument_list|,
name|indexSettings
argument_list|,
name|indexStore
argument_list|)
expr_stmt|;
name|this
operator|.
name|bbDirectory
operator|=
operator|new
name|CustomByteBufferDirectory
argument_list|(
name|byteBufferCache
argument_list|)
expr_stmt|;
name|this
operator|.
name|directory
operator|=
name|wrapDirectory
argument_list|(
name|bbDirectory
argument_list|)
expr_stmt|;
name|logger
operator|.
name|debug
argument_list|(
literal|"Using [byte_buffer] store"
argument_list|)
expr_stmt|;
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
DECL|method|doRenameFile
annotation|@
name|Override
specifier|protected
name|void
name|doRenameFile
parameter_list|(
name|String
name|from
parameter_list|,
name|String
name|to
parameter_list|)
throws|throws
name|IOException
block|{
name|bbDirectory
operator|.
name|renameTo
argument_list|(
name|from
argument_list|,
name|to
argument_list|)
expr_stmt|;
block|}
DECL|class|CustomByteBufferDirectory
specifier|static
class|class
name|CustomByteBufferDirectory
extends|extends
name|ByteBufferDirectory
block|{
DECL|method|CustomByteBufferDirectory
name|CustomByteBufferDirectory
parameter_list|()
block|{         }
DECL|method|CustomByteBufferDirectory
name|CustomByteBufferDirectory
parameter_list|(
name|ByteBufferAllocator
name|allocator
parameter_list|)
block|{
name|super
argument_list|(
name|allocator
argument_list|)
expr_stmt|;
block|}
DECL|method|renameTo
specifier|public
name|void
name|renameTo
parameter_list|(
name|String
name|from
parameter_list|,
name|String
name|to
parameter_list|)
throws|throws
name|IOException
block|{
name|ByteBufferFile
name|fromFile
init|=
name|files
operator|.
name|get
argument_list|(
name|from
argument_list|)
decl_stmt|;
if|if
condition|(
name|fromFile
operator|==
literal|null
condition|)
throw|throw
operator|new
name|FileNotFoundException
argument_list|(
name|from
argument_list|)
throw|;
name|ByteBufferFile
name|toFile
init|=
name|files
operator|.
name|get
argument_list|(
name|to
argument_list|)
decl_stmt|;
if|if
condition|(
name|toFile
operator|!=
literal|null
condition|)
block|{
name|files
operator|.
name|remove
argument_list|(
name|from
argument_list|)
expr_stmt|;
block|}
name|files
operator|.
name|put
argument_list|(
name|to
argument_list|,
name|fromFile
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

