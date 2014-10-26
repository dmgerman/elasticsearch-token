begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.store
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
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
name|lucene
operator|.
name|index
operator|.
name|IndexFileNames
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
name|*
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
name|util
operator|.
name|IOUtils
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
name|math
operator|.
name|MathUtils
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
name|util
operator|.
name|concurrent
operator|.
name|ConcurrentCollections
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
name|distributor
operator|.
name|Distributor
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
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
name|ConcurrentMap
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
name|AtomicInteger
import|;
end_import

begin_comment
comment|/**  * A directory implementation that uses the Elasticsearch {@link Distributor} abstraction to distribute  * files across multiple data directories.  */
end_comment

begin_class
DECL|class|DistributorDirectory
specifier|public
specifier|final
class|class
name|DistributorDirectory
extends|extends
name|BaseDirectory
block|{
DECL|field|distributor
specifier|private
specifier|final
name|Distributor
name|distributor
decl_stmt|;
DECL|field|nameDirMapping
specifier|private
specifier|final
name|ConcurrentMap
argument_list|<
name|String
argument_list|,
name|Directory
argument_list|>
name|nameDirMapping
init|=
name|ConcurrentCollections
operator|.
name|newConcurrentMap
argument_list|()
decl_stmt|;
comment|/**      * Creates a new DistributorDirectory from multiple directories. Note: The first directory in the given array      * is used as the primary directory holding the file locks as well as the SEGMENTS_GEN file. All remaining      * directories are used in a round robin fashion.      */
DECL|method|DistributorDirectory
specifier|public
name|DistributorDirectory
parameter_list|(
specifier|final
name|Directory
modifier|...
name|dirs
parameter_list|)
throws|throws
name|IOException
block|{
name|this
argument_list|(
operator|new
name|Distributor
argument_list|()
block|{
specifier|final
name|AtomicInteger
name|count
init|=
operator|new
name|AtomicInteger
argument_list|()
decl_stmt|;
annotation|@
name|Override
specifier|public
name|Directory
name|primary
parameter_list|()
block|{
return|return
name|dirs
index|[
literal|0
index|]
return|;
block|}
annotation|@
name|Override
specifier|public
name|Directory
index|[]
name|all
parameter_list|()
block|{
return|return
name|dirs
return|;
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|Directory
name|any
parameter_list|()
block|{
return|return
name|dirs
index|[
name|MathUtils
operator|.
name|mod
argument_list|(
name|count
operator|.
name|incrementAndGet
argument_list|()
argument_list|,
name|dirs
operator|.
name|length
argument_list|)
index|]
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
comment|/**      * Creates a new DistributorDirectory form the given Distributor.      */
DECL|method|DistributorDirectory
specifier|public
name|DistributorDirectory
parameter_list|(
name|Distributor
name|distributor
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|distributor
operator|=
name|distributor
expr_stmt|;
for|for
control|(
name|Directory
name|dir
range|:
name|distributor
operator|.
name|all
argument_list|()
control|)
block|{
for|for
control|(
name|String
name|file
range|:
name|dir
operator|.
name|listAll
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|usePrimary
argument_list|(
name|file
argument_list|)
condition|)
block|{
name|nameDirMapping
operator|.
name|put
argument_list|(
name|file
argument_list|,
name|dir
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
annotation|@
name|Override
DECL|method|listAll
specifier|public
specifier|final
name|String
index|[]
name|listAll
parameter_list|()
throws|throws
name|IOException
block|{
specifier|final
name|ArrayList
argument_list|<
name|String
argument_list|>
name|files
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|Directory
name|dir
range|:
name|distributor
operator|.
name|all
argument_list|()
control|)
block|{
for|for
control|(
name|String
name|file
range|:
name|dir
operator|.
name|listAll
argument_list|()
control|)
block|{
name|files
operator|.
name|add
argument_list|(
name|file
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|files
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
name|files
operator|.
name|size
argument_list|()
index|]
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|fileExists
specifier|public
name|boolean
name|fileExists
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
return|return
name|getDirectory
argument_list|(
name|name
argument_list|)
operator|.
name|fileExists
argument_list|(
name|name
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|FileNotFoundException
name|ex
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
block|}
annotation|@
name|Override
DECL|method|deleteFile
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
name|getDirectory
argument_list|(
name|name
argument_list|,
literal|true
argument_list|)
operator|.
name|deleteFile
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|Directory
name|remove
init|=
name|nameDirMapping
operator|.
name|remove
argument_list|(
name|name
argument_list|)
decl_stmt|;
assert|assert
name|usePrimary
argument_list|(
name|name
argument_list|)
operator|||
name|remove
operator|!=
literal|null
operator|:
literal|"Tried to delete file "
operator|+
name|name
operator|+
literal|" but couldn't"
assert|;
block|}
annotation|@
name|Override
DECL|method|fileLength
specifier|public
name|long
name|fileLength
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|getDirectory
argument_list|(
name|name
argument_list|)
operator|.
name|fileLength
argument_list|(
name|name
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|createOutput
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
return|return
name|getDirectory
argument_list|(
name|name
argument_list|,
literal|false
argument_list|)
operator|.
name|createOutput
argument_list|(
name|name
argument_list|,
name|context
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|sync
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
for|for
control|(
name|Directory
name|dir
range|:
name|distributor
operator|.
name|all
argument_list|()
control|)
block|{
name|dir
operator|.
name|sync
argument_list|(
name|names
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|openInput
specifier|public
name|IndexInput
name|openInput
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
return|return
name|getDirectory
argument_list|(
name|name
argument_list|)
operator|.
name|openInput
argument_list|(
name|name
argument_list|,
name|context
argument_list|)
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
name|IOUtils
operator|.
name|close
argument_list|(
name|distributor
operator|.
name|all
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**      * Returns the directory that has previously been associated with this file name.      *      * @throws IOException if the name has not yet been associated with any directory ie. fi the file does not exists      */
DECL|method|getDirectory
specifier|private
name|Directory
name|getDirectory
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|getDirectory
argument_list|(
name|name
argument_list|,
literal|true
argument_list|)
return|;
block|}
comment|/**      * Returns true if the primary directory should be used for the given file.      */
DECL|method|usePrimary
specifier|private
name|boolean
name|usePrimary
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
name|IndexFileNames
operator|.
name|SEGMENTS_GEN
operator|.
name|equals
argument_list|(
name|name
argument_list|)
operator|||
name|Store
operator|.
name|isChecksum
argument_list|(
name|name
argument_list|)
return|;
block|}
comment|/**      * Returns the directory that has previously been associated with this file name or associates the name with a directory      * if failIfNotAssociated is set to false.      */
DECL|method|getDirectory
specifier|private
name|Directory
name|getDirectory
parameter_list|(
name|String
name|name
parameter_list|,
name|boolean
name|failIfNotAssociated
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|usePrimary
argument_list|(
name|name
argument_list|)
condition|)
block|{
return|return
name|distributor
operator|.
name|primary
argument_list|()
return|;
block|}
name|Directory
name|directory
init|=
name|nameDirMapping
operator|.
name|get
argument_list|(
name|name
argument_list|)
decl_stmt|;
if|if
condition|(
name|directory
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|failIfNotAssociated
condition|)
block|{
throw|throw
operator|new
name|FileNotFoundException
argument_list|(
literal|"No such file ["
operator|+
name|name
operator|+
literal|"]"
argument_list|)
throw|;
block|}
comment|// Pick a directory and associate this new file with it:
specifier|final
name|Directory
name|dir
init|=
name|distributor
operator|.
name|any
argument_list|()
decl_stmt|;
name|directory
operator|=
name|nameDirMapping
operator|.
name|putIfAbsent
argument_list|(
name|name
argument_list|,
name|dir
argument_list|)
expr_stmt|;
if|if
condition|(
name|directory
operator|==
literal|null
condition|)
block|{
comment|// putIfAbsent did in fact put dir:
name|directory
operator|=
name|dir
expr_stmt|;
block|}
block|}
return|return
name|directory
return|;
block|}
annotation|@
name|Override
DECL|method|makeLock
specifier|public
name|Lock
name|makeLock
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
name|distributor
operator|.
name|primary
argument_list|()
operator|.
name|makeLock
argument_list|(
name|name
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|clearLock
specifier|public
name|void
name|clearLock
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|IOException
block|{
name|distributor
operator|.
name|primary
argument_list|()
operator|.
name|clearLock
argument_list|(
name|name
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getLockFactory
specifier|public
name|LockFactory
name|getLockFactory
parameter_list|()
block|{
return|return
name|distributor
operator|.
name|primary
argument_list|()
operator|.
name|getLockFactory
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|setLockFactory
specifier|public
name|void
name|setLockFactory
parameter_list|(
name|LockFactory
name|lockFactory
parameter_list|)
throws|throws
name|IOException
block|{
name|distributor
operator|.
name|primary
argument_list|()
operator|.
name|setLockFactory
argument_list|(
name|lockFactory
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getLockID
specifier|public
name|String
name|getLockID
parameter_list|()
block|{
return|return
name|distributor
operator|.
name|primary
argument_list|()
operator|.
name|getLockID
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
name|distributor
operator|.
name|toString
argument_list|()
return|;
block|}
comment|/**      * Renames the given source file to the given target file unless the target already exists.      *      * @param directoryService the DirecotrySerivce to use.      * @param from the source file name.      * @param to the target file name      * @throws IOException if the target file already exists.      */
DECL|method|renameFile
specifier|public
name|void
name|renameFile
parameter_list|(
name|DirectoryService
name|directoryService
parameter_list|,
name|String
name|from
parameter_list|,
name|String
name|to
parameter_list|)
throws|throws
name|IOException
block|{
name|Directory
name|directory
init|=
name|getDirectory
argument_list|(
name|from
argument_list|)
decl_stmt|;
if|if
condition|(
name|nameDirMapping
operator|.
name|putIfAbsent
argument_list|(
name|to
argument_list|,
name|directory
argument_list|)
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Can't rename file from "
operator|+
name|from
operator|+
literal|" to: "
operator|+
name|to
operator|+
literal|": target file already exists"
argument_list|)
throw|;
block|}
name|boolean
name|success
init|=
literal|false
decl_stmt|;
try|try
block|{
name|directoryService
operator|.
name|renameFile
argument_list|(
name|directory
argument_list|,
name|from
argument_list|,
name|to
argument_list|)
expr_stmt|;
name|nameDirMapping
operator|.
name|remove
argument_list|(
name|from
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
operator|!
name|success
condition|)
block|{
name|nameDirMapping
operator|.
name|remove
argument_list|(
name|to
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

