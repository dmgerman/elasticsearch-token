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
name|Collection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
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
name|HashMap
argument_list|<
name|String
argument_list|,
name|Directory
argument_list|>
name|nameDirMapping
init|=
operator|new
name|HashMap
argument_list|<>
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
name|lockFactory
operator|=
operator|new
name|DistributorLockFactoryWrapper
argument_list|(
name|distributor
operator|.
name|primary
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|listAll
specifier|public
specifier|synchronized
specifier|final
name|String
index|[]
name|listAll
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|nameDirMapping
operator|.
name|keySet
argument_list|()
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
name|nameDirMapping
operator|.
name|size
argument_list|()
index|]
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|deleteFile
specifier|public
specifier|synchronized
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
specifier|synchronized
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
specifier|synchronized
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
comment|// no need to sync this operation it could be long running too
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
DECL|method|renameFile
specifier|public
specifier|synchronized
name|void
name|renameFile
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
specifier|final
name|Directory
name|directory
init|=
name|getDirectory
argument_list|(
name|source
argument_list|)
decl_stmt|;
specifier|final
name|Directory
name|targetDir
init|=
name|nameDirMapping
operator|.
name|get
argument_list|(
name|dest
argument_list|)
decl_stmt|;
if|if
condition|(
name|targetDir
operator|!=
literal|null
operator|&&
name|targetDir
operator|!=
name|directory
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Can't rename file from "
operator|+
name|source
operator|+
literal|" to: "
operator|+
name|dest
operator|+
literal|": target file already exists in a different directory"
argument_list|)
throw|;
block|}
name|directory
operator|.
name|renameFile
argument_list|(
name|source
argument_list|,
name|dest
argument_list|)
expr_stmt|;
name|nameDirMapping
operator|.
name|remove
argument_list|(
name|source
argument_list|)
expr_stmt|;
name|nameDirMapping
operator|.
name|put
argument_list|(
name|dest
argument_list|,
name|directory
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|openInput
specifier|public
specifier|synchronized
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
specifier|synchronized
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
name|Directory
name|getDirectory
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|IOException
block|{
comment|// pkg private for testing
return|return
name|getDirectory
argument_list|(
name|name
argument_list|,
literal|true
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
specifier|final
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
assert|assert
name|nameDirMapping
operator|.
name|containsKey
argument_list|(
name|name
argument_list|)
operator|==
literal|false
assert|;
name|nameDirMapping
operator|.
name|put
argument_list|(
name|name
argument_list|,
name|dir
argument_list|)
expr_stmt|;
return|return
name|dir
return|;
block|}
return|return
name|directory
return|;
block|}
annotation|@
name|Override
DECL|method|setLockFactory
specifier|public
specifier|synchronized
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
name|super
operator|.
name|setLockFactory
argument_list|(
operator|new
name|DistributorLockFactoryWrapper
argument_list|(
name|distributor
operator|.
name|primary
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getLockID
specifier|public
specifier|synchronized
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
specifier|synchronized
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
DECL|method|getDistributor
name|Distributor
name|getDistributor
parameter_list|()
block|{
return|return
name|distributor
return|;
block|}
comment|/**      * Basic checks to ensure the internal mapping is consistent - should only be used in assertions      */
DECL|method|assertConsistency
specifier|static
name|boolean
name|assertConsistency
parameter_list|(
name|ESLogger
name|logger
parameter_list|,
name|DistributorDirectory
name|dir
parameter_list|)
throws|throws
name|IOException
block|{
synchronized|synchronized
init|(
name|dir
init|)
block|{
name|boolean
name|consistent
init|=
literal|true
decl_stmt|;
name|StringBuilder
name|builder
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|Directory
index|[]
name|all
init|=
name|dir
operator|.
name|distributor
operator|.
name|all
argument_list|()
decl_stmt|;
for|for
control|(
name|Directory
name|d
range|:
name|all
control|)
block|{
for|for
control|(
name|String
name|file
range|:
name|d
operator|.
name|listAll
argument_list|()
control|)
block|{
specifier|final
name|Directory
name|directory
init|=
name|dir
operator|.
name|nameDirMapping
operator|.
name|get
argument_list|(
name|file
argument_list|)
decl_stmt|;
if|if
condition|(
name|directory
operator|==
literal|null
condition|)
block|{
name|consistent
operator|=
literal|false
expr_stmt|;
name|builder
operator|.
name|append
argument_list|(
literal|"File "
argument_list|)
operator|.
name|append
argument_list|(
name|file
argument_list|)
operator|.
name|append
argument_list|(
literal|" was not mapped to a directory but exists in one of the distributors directories"
argument_list|)
operator|.
name|append
argument_list|(
name|System
operator|.
name|lineSeparator
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|directory
operator|!=
name|d
condition|)
block|{
name|consistent
operator|=
literal|false
expr_stmt|;
name|builder
operator|.
name|append
argument_list|(
literal|"File "
argument_list|)
operator|.
name|append
argument_list|(
name|file
argument_list|)
operator|.
name|append
argument_list|(
literal|" was  mapped to a directory "
argument_list|)
operator|.
name|append
argument_list|(
name|directory
argument_list|)
operator|.
name|append
argument_list|(
literal|" but exists in another distributor directory"
argument_list|)
operator|.
name|append
argument_list|(
name|d
argument_list|)
operator|.
name|append
argument_list|(
name|System
operator|.
name|lineSeparator
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|consistent
operator|==
literal|false
condition|)
block|{
name|logger
operator|.
name|info
argument_list|(
name|builder
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
assert|assert
name|consistent
operator|:
name|builder
operator|.
name|toString
argument_list|()
assert|;
return|return
name|consistent
return|;
comment|// return boolean so it can be easily be used in asserts
block|}
block|}
comment|/**      * This inner class is a simple wrapper around the original      * lock factory to track files written / created through the      * lock factory. For instance {@link NativeFSLockFactory} creates real      * files that we should expose for consistency reasons.      */
DECL|class|DistributorLockFactoryWrapper
specifier|private
class|class
name|DistributorLockFactoryWrapper
extends|extends
name|LockFactory
block|{
DECL|field|dir
specifier|private
specifier|final
name|Directory
name|dir
decl_stmt|;
DECL|field|delegate
specifier|private
specifier|final
name|LockFactory
name|delegate
decl_stmt|;
DECL|field|writesFiles
specifier|private
specifier|final
name|boolean
name|writesFiles
decl_stmt|;
DECL|method|DistributorLockFactoryWrapper
specifier|public
name|DistributorLockFactoryWrapper
parameter_list|(
name|Directory
name|dir
parameter_list|)
block|{
name|this
operator|.
name|dir
operator|=
name|dir
expr_stmt|;
specifier|final
name|FSDirectory
name|leaf
init|=
name|DirectoryUtils
operator|.
name|getLeaf
argument_list|(
name|dir
argument_list|,
name|FSDirectory
operator|.
name|class
argument_list|)
decl_stmt|;
if|if
condition|(
name|leaf
operator|!=
literal|null
condition|)
block|{
name|writesFiles
operator|=
name|leaf
operator|.
name|getLockFactory
argument_list|()
operator|instanceof
name|FSLockFactory
expr_stmt|;
block|}
else|else
block|{
name|writesFiles
operator|=
literal|false
expr_stmt|;
block|}
name|this
operator|.
name|delegate
operator|=
name|dir
operator|.
name|getLockFactory
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|setLockPrefix
specifier|public
name|void
name|setLockPrefix
parameter_list|(
name|String
name|lockPrefix
parameter_list|)
block|{
name|delegate
operator|.
name|setLockPrefix
argument_list|(
name|lockPrefix
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getLockPrefix
specifier|public
name|String
name|getLockPrefix
parameter_list|()
block|{
return|return
name|delegate
operator|.
name|getLockPrefix
argument_list|()
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
name|lockName
parameter_list|)
block|{
return|return
operator|new
name|DistributorLock
argument_list|(
name|delegate
operator|.
name|makeLock
argument_list|(
name|lockName
argument_list|)
argument_list|,
name|lockName
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
name|lockName
parameter_list|)
throws|throws
name|IOException
block|{
name|delegate
operator|.
name|clearLock
argument_list|(
name|lockName
argument_list|)
expr_stmt|;
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
literal|"DistributorLockFactoryWrapper("
operator|+
name|delegate
operator|.
name|toString
argument_list|()
operator|+
literal|")"
return|;
block|}
DECL|class|DistributorLock
specifier|private
class|class
name|DistributorLock
extends|extends
name|Lock
block|{
DECL|field|delegateLock
specifier|private
specifier|final
name|Lock
name|delegateLock
decl_stmt|;
DECL|field|name
specifier|private
specifier|final
name|String
name|name
decl_stmt|;
DECL|method|DistributorLock
name|DistributorLock
parameter_list|(
name|Lock
name|delegate
parameter_list|,
name|String
name|name
parameter_list|)
block|{
name|this
operator|.
name|delegateLock
operator|=
name|delegate
expr_stmt|;
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|obtain
specifier|public
name|boolean
name|obtain
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|delegateLock
operator|.
name|obtain
argument_list|()
condition|)
block|{
if|if
condition|(
name|writesFiles
condition|)
block|{
synchronized|synchronized
init|(
name|DistributorDirectory
operator|.
name|this
init|)
block|{
assert|assert
operator|(
name|nameDirMapping
operator|.
name|containsKey
argument_list|(
name|name
argument_list|)
operator|==
literal|false
operator|||
name|nameDirMapping
operator|.
name|get
argument_list|(
name|name
argument_list|)
operator|==
name|dir
operator|)
assert|;
if|if
condition|(
name|nameDirMapping
operator|.
name|get
argument_list|(
name|name
argument_list|)
operator|==
literal|null
condition|)
block|{
name|nameDirMapping
operator|.
name|put
argument_list|(
name|name
argument_list|,
name|dir
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
literal|true
return|;
block|}
else|else
block|{
return|return
literal|false
return|;
block|}
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
name|delegateLock
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|isLocked
specifier|public
name|boolean
name|isLocked
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|delegateLock
operator|.
name|isLocked
argument_list|()
return|;
block|}
block|}
block|}
block|}
end_class

end_unit

