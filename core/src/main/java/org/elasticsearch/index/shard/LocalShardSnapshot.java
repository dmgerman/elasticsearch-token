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
name|IOContext
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
name|IndexOutput
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
name|Lock
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
name|NoLockFactory
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
name|engine
operator|.
name|Engine
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
name|Store
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Closeable
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
name|Collection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicBoolean
import|;
end_import

begin_class
DECL|class|LocalShardSnapshot
specifier|final
class|class
name|LocalShardSnapshot
implements|implements
name|Closeable
block|{
DECL|field|shard
specifier|private
specifier|final
name|IndexShard
name|shard
decl_stmt|;
DECL|field|store
specifier|private
specifier|final
name|Store
name|store
decl_stmt|;
DECL|field|indexCommit
specifier|private
specifier|final
name|Engine
operator|.
name|IndexCommitRef
name|indexCommit
decl_stmt|;
DECL|field|closed
specifier|private
specifier|final
name|AtomicBoolean
name|closed
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
DECL|method|LocalShardSnapshot
name|LocalShardSnapshot
parameter_list|(
name|IndexShard
name|shard
parameter_list|)
block|{
name|this
operator|.
name|shard
operator|=
name|shard
expr_stmt|;
name|store
operator|=
name|shard
operator|.
name|store
argument_list|()
expr_stmt|;
name|store
operator|.
name|incRef
argument_list|()
expr_stmt|;
name|boolean
name|success
init|=
literal|false
decl_stmt|;
try|try
block|{
name|indexCommit
operator|=
name|shard
operator|.
name|acquireIndexCommit
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|success
operator|=
literal|true
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|success
operator|==
literal|false
condition|)
block|{
name|store
operator|.
name|decRef
argument_list|()
expr_stmt|;
block|}
block|}
block|}
DECL|method|getIndex
name|Index
name|getIndex
parameter_list|()
block|{
return|return
name|shard
operator|.
name|indexSettings
argument_list|()
operator|.
name|getIndex
argument_list|()
return|;
block|}
DECL|method|getSnapshotDirectory
name|Directory
name|getSnapshotDirectory
parameter_list|()
block|{
comment|/* this directory will not be used for anything else but reading / copying files to another directory          * we prevent all write operations on this directory with UOE - nobody should close it either. */
return|return
operator|new
name|FilterDirectory
argument_list|(
name|store
operator|.
name|directory
argument_list|()
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|String
index|[]
name|listAll
parameter_list|()
throws|throws
name|IOException
block|{
name|Collection
argument_list|<
name|String
argument_list|>
name|fileNames
init|=
name|indexCommit
operator|.
name|getIndexCommit
argument_list|()
operator|.
name|getFileNames
argument_list|()
decl_stmt|;
specifier|final
name|String
index|[]
name|fileNameArray
init|=
name|fileNames
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
name|fileNames
operator|.
name|size
argument_list|()
index|]
argument_list|)
decl_stmt|;
return|return
name|fileNameArray
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|deleteFile
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"this directory is read-only"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|sync
parameter_list|(
name|Collection
argument_list|<
name|String
argument_list|>
name|names
parameter_list|)
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"this directory is read-only"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|rename
parameter_list|(
name|String
name|source
parameter_list|,
name|String
name|dest
parameter_list|)
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"this directory is read-only"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|IndexOutput
name|createOutput
parameter_list|(
name|String
name|name
parameter_list|,
name|IOContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"this directory is read-only"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|IndexOutput
name|createTempOutput
parameter_list|(
name|String
name|prefix
parameter_list|,
name|String
name|suffix
parameter_list|,
name|IOContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"this directory is read-only"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|Lock
name|obtainLock
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|IOException
block|{
comment|/* we do explicitly a no-lock instance since we hold an index commit from a SnapshotDeletionPolicy so we                      * can we certain that nobody messes with the files on disk. We also hold a ref on the store which means                      * no external source will delete files either.*/
return|return
name|NoLockFactory
operator|.
name|INSTANCE
operator|.
name|obtainLock
argument_list|(
name|in
argument_list|,
name|name
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"nobody should close this directory wrapper"
argument_list|)
throw|;
block|}
block|}
return|;
block|}
annotation|@
name|Override
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|closed
operator|.
name|compareAndSet
argument_list|(
literal|false
argument_list|,
literal|true
argument_list|)
condition|)
block|{
try|try
block|{
name|indexCommit
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
name|store
operator|.
name|decRef
argument_list|()
expr_stmt|;
block|}
block|}
block|}
DECL|method|getIndexMetaData
name|IndexMetaData
name|getIndexMetaData
parameter_list|()
block|{
return|return
name|shard
operator|.
name|indexSettings
operator|.
name|getIndexMetaData
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"local_shard_snapshot:["
operator|+
name|shard
operator|.
name|shardId
argument_list|()
operator|+
literal|" indexCommit: "
operator|+
name|indexCommit
operator|+
literal|"]"
return|;
block|}
block|}
end_class

end_unit

