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
name|IndexInput
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
name|SingleInstanceLockFactory
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
name|SizeUnit
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
name|SizeValue
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
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Queue
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
name|ArrayBlockingQueue
import|;
end_import

begin_import
import|import static
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
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * @author kimchy (Shay Banon)  */
end_comment

begin_class
DECL|class|HeapDirectory
specifier|public
class|class
name|HeapDirectory
extends|extends
name|Directory
block|{
DECL|field|files
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|HeapRamFile
argument_list|>
name|files
init|=
name|newConcurrentMap
argument_list|()
decl_stmt|;
DECL|field|cache
specifier|private
specifier|final
name|Queue
argument_list|<
name|byte
index|[]
argument_list|>
name|cache
decl_stmt|;
DECL|field|bufferSizeInBytes
specifier|private
specifier|final
name|int
name|bufferSizeInBytes
decl_stmt|;
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
DECL|field|disableCache
specifier|private
specifier|final
name|boolean
name|disableCache
decl_stmt|;
DECL|method|HeapDirectory
specifier|public
name|HeapDirectory
parameter_list|()
block|{
name|this
argument_list|(
operator|new
name|SizeValue
argument_list|(
literal|1
argument_list|,
name|SizeUnit
operator|.
name|KB
argument_list|)
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
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
DECL|method|HeapDirectory
specifier|public
name|HeapDirectory
parameter_list|(
name|SizeValue
name|bufferSize
parameter_list|,
name|SizeValue
name|cacheSize
parameter_list|,
name|boolean
name|warmCache
parameter_list|)
block|{
name|disableCache
operator|=
name|cacheSize
operator|.
name|bytes
argument_list|()
operator|==
literal|0
expr_stmt|;
if|if
condition|(
operator|!
name|disableCache
operator|&&
name|cacheSize
operator|.
name|bytes
argument_list|()
operator|<
name|bufferSize
operator|.
name|bytes
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Cache size ["
operator|+
name|cacheSize
operator|+
literal|"] is smaller than buffer size ["
operator|+
name|bufferSize
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|this
operator|.
name|bufferSize
operator|=
name|bufferSize
expr_stmt|;
name|this
operator|.
name|bufferSizeInBytes
operator|=
operator|(
name|int
operator|)
name|bufferSize
operator|.
name|bytes
argument_list|()
expr_stmt|;
name|int
name|numberOfCacheEntries
init|=
call|(
name|int
call|)
argument_list|(
name|cacheSize
operator|.
name|bytes
argument_list|()
operator|/
name|bufferSize
operator|.
name|bytes
argument_list|()
argument_list|)
decl_stmt|;
name|this
operator|.
name|cache
operator|=
name|disableCache
condition|?
literal|null
else|:
operator|new
name|ArrayBlockingQueue
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|(
name|numberOfCacheEntries
argument_list|)
expr_stmt|;
name|this
operator|.
name|cacheSize
operator|=
name|disableCache
condition|?
operator|new
name|SizeValue
argument_list|(
literal|0
argument_list|,
name|SizeUnit
operator|.
name|BYTES
argument_list|)
else|:
operator|new
name|SizeValue
argument_list|(
name|numberOfCacheEntries
operator|*
name|bufferSize
operator|.
name|bytes
argument_list|()
argument_list|,
name|SizeUnit
operator|.
name|BYTES
argument_list|)
expr_stmt|;
name|setLockFactory
argument_list|(
operator|new
name|SingleInstanceLockFactory
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|disableCache
operator|&&
name|warmCache
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
name|numberOfCacheEntries
condition|;
name|i
operator|++
control|)
block|{
name|cache
operator|.
name|add
argument_list|(
name|createBuffer
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|bufferSize
specifier|public
name|SizeValue
name|bufferSize
parameter_list|()
block|{
return|return
name|this
operator|.
name|bufferSize
return|;
block|}
DECL|method|cacheSize
specifier|public
name|SizeValue
name|cacheSize
parameter_list|()
block|{
return|return
name|this
operator|.
name|cacheSize
return|;
block|}
DECL|method|bufferSizeInBytes
name|int
name|bufferSizeInBytes
parameter_list|()
block|{
return|return
name|bufferSizeInBytes
return|;
block|}
DECL|method|listAll
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
return|return
name|files
operator|.
name|keySet
argument_list|()
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
literal|0
index|]
argument_list|)
return|;
block|}
DECL|method|fileExists
annotation|@
name|Override
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
return|return
name|files
operator|.
name|containsKey
argument_list|(
name|name
argument_list|)
return|;
block|}
DECL|method|fileModified
annotation|@
name|Override
specifier|public
name|long
name|fileModified
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|IOException
block|{
name|HeapRamFile
name|file
init|=
name|files
operator|.
name|get
argument_list|(
name|name
argument_list|)
decl_stmt|;
if|if
condition|(
name|file
operator|==
literal|null
condition|)
throw|throw
operator|new
name|FileNotFoundException
argument_list|(
name|name
argument_list|)
throw|;
return|return
name|file
operator|.
name|lastModified
argument_list|()
return|;
block|}
DECL|method|touchFile
annotation|@
name|Override
specifier|public
name|void
name|touchFile
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|IOException
block|{
name|HeapRamFile
name|file
init|=
name|files
operator|.
name|get
argument_list|(
name|name
argument_list|)
decl_stmt|;
if|if
condition|(
name|file
operator|==
literal|null
condition|)
throw|throw
operator|new
name|FileNotFoundException
argument_list|(
name|name
argument_list|)
throw|;
name|long
name|ts2
decl_stmt|,
name|ts1
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
do|do
block|{
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|0
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ie
parameter_list|)
block|{
comment|// In 3.0 we will change this to throw
comment|// InterruptedException instead
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|interrupt
argument_list|()
expr_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|ie
argument_list|)
throw|;
block|}
name|ts2
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
block|}
do|while
condition|(
name|ts1
operator|==
name|ts2
condition|)
do|;
name|file
operator|.
name|lastModified
argument_list|(
name|ts2
argument_list|)
expr_stmt|;
block|}
DECL|method|deleteFile
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
name|HeapRamFile
name|file
init|=
name|files
operator|.
name|remove
argument_list|(
name|name
argument_list|)
decl_stmt|;
if|if
condition|(
name|file
operator|==
literal|null
condition|)
throw|throw
operator|new
name|FileNotFoundException
argument_list|(
name|name
argument_list|)
throw|;
name|file
operator|.
name|clean
argument_list|()
expr_stmt|;
block|}
DECL|method|fileLength
annotation|@
name|Override
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
name|HeapRamFile
name|file
init|=
name|files
operator|.
name|get
argument_list|(
name|name
argument_list|)
decl_stmt|;
if|if
condition|(
name|file
operator|==
literal|null
condition|)
throw|throw
operator|new
name|FileNotFoundException
argument_list|(
name|name
argument_list|)
throw|;
return|return
name|file
operator|.
name|length
argument_list|()
return|;
block|}
DECL|method|createOutput
annotation|@
name|Override
specifier|public
name|IndexOutput
name|createOutput
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|IOException
block|{
name|HeapRamFile
name|file
init|=
operator|new
name|HeapRamFile
argument_list|(
name|this
argument_list|)
decl_stmt|;
name|HeapRamFile
name|existing
init|=
name|files
operator|.
name|put
argument_list|(
name|name
argument_list|,
name|file
argument_list|)
decl_stmt|;
if|if
condition|(
name|existing
operator|!=
literal|null
condition|)
block|{
name|existing
operator|.
name|clean
argument_list|()
expr_stmt|;
block|}
return|return
operator|new
name|HeapIndexOutput
argument_list|(
name|this
argument_list|,
name|file
argument_list|)
return|;
block|}
DECL|method|openInput
annotation|@
name|Override
specifier|public
name|IndexInput
name|openInput
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|IOException
block|{
name|HeapRamFile
name|file
init|=
name|files
operator|.
name|get
argument_list|(
name|name
argument_list|)
decl_stmt|;
if|if
condition|(
name|file
operator|==
literal|null
condition|)
throw|throw
operator|new
name|FileNotFoundException
argument_list|(
name|name
argument_list|)
throw|;
return|return
operator|new
name|HeapIndexInput
argument_list|(
name|this
argument_list|,
name|file
argument_list|)
return|;
block|}
DECL|method|close
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
name|String
index|[]
name|files
init|=
name|listAll
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|file
range|:
name|files
control|)
block|{
name|deleteFile
argument_list|(
name|file
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|disableCache
condition|)
block|{
name|byte
index|[]
name|buffer
init|=
name|cache
operator|.
name|poll
argument_list|()
decl_stmt|;
while|while
condition|(
name|buffer
operator|!=
literal|null
condition|)
block|{
name|closeBuffer
argument_list|(
name|buffer
argument_list|)
expr_stmt|;
name|buffer
operator|=
name|cache
operator|.
name|poll
argument_list|()
expr_stmt|;
block|}
block|}
block|}
DECL|method|releaseBuffer
name|void
name|releaseBuffer
parameter_list|(
name|byte
index|[]
name|buffer
parameter_list|)
block|{
if|if
condition|(
name|disableCache
condition|)
block|{
name|closeBuffer
argument_list|(
name|buffer
argument_list|)
expr_stmt|;
return|return;
block|}
name|boolean
name|success
init|=
name|cache
operator|.
name|offer
argument_list|(
name|buffer
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|success
condition|)
block|{
name|closeBuffer
argument_list|(
name|buffer
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|acquireBuffer
name|byte
index|[]
name|acquireBuffer
parameter_list|()
block|{
if|if
condition|(
name|disableCache
condition|)
block|{
return|return
name|createBuffer
argument_list|()
return|;
block|}
name|byte
index|[]
name|buffer
init|=
name|cache
operator|.
name|poll
argument_list|()
decl_stmt|;
if|if
condition|(
name|buffer
operator|==
literal|null
condition|)
block|{
comment|// everything is taken, return a new one
return|return
name|createBuffer
argument_list|()
return|;
block|}
return|return
name|buffer
return|;
block|}
DECL|method|createBuffer
name|byte
index|[]
name|createBuffer
parameter_list|()
block|{
return|return
operator|new
name|byte
index|[
name|bufferSizeInBytes
index|]
return|;
block|}
DECL|method|closeBuffer
name|void
name|closeBuffer
parameter_list|(
name|byte
index|[]
name|buffer
parameter_list|)
block|{     }
block|}
end_class

end_unit

