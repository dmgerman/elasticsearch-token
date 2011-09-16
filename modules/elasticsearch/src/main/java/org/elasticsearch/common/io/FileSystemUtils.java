begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.io
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|io
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
name|logging
operator|.
name|ESLoggerFactory
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
name|TimeValue
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
name|FileInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|FileOutputStream
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
name|io
operator|.
name|InterruptedIOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|RandomAccessFile
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|channels
operator|.
name|FileChannel
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
name|List
import|;
end_import

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|FileSystemUtils
specifier|public
class|class
name|FileSystemUtils
block|{
DECL|field|logger
specifier|private
specifier|static
name|ESLogger
name|logger
init|=
name|ESLoggerFactory
operator|.
name|getLogger
argument_list|(
name|FileSystemUtils
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
DECL|field|mkdirsStallTimeout
specifier|private
specifier|static
specifier|final
name|long
name|mkdirsStallTimeout
init|=
name|TimeValue
operator|.
name|timeValueMinutes
argument_list|(
literal|5
argument_list|)
operator|.
name|millis
argument_list|()
decl_stmt|;
DECL|field|mkdirsMutex
specifier|private
specifier|static
specifier|final
name|Object
name|mkdirsMutex
init|=
operator|new
name|Object
argument_list|()
decl_stmt|;
DECL|field|mkdirsThread
specifier|private
specifier|static
specifier|volatile
name|Thread
name|mkdirsThread
decl_stmt|;
DECL|field|mkdirsStartTime
specifier|private
specifier|static
specifier|volatile
name|long
name|mkdirsStartTime
decl_stmt|;
DECL|method|mkdirs
specifier|public
specifier|static
name|boolean
name|mkdirs
parameter_list|(
name|File
name|dir
parameter_list|)
block|{
synchronized|synchronized
init|(
name|mkdirsMutex
init|)
block|{
try|try
block|{
name|mkdirsThread
operator|=
name|Thread
operator|.
name|currentThread
argument_list|()
expr_stmt|;
name|mkdirsStartTime
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
return|return
name|dir
operator|.
name|mkdirs
argument_list|()
return|;
block|}
finally|finally
block|{
name|mkdirsThread
operator|=
literal|null
expr_stmt|;
block|}
block|}
block|}
DECL|method|checkMkdirsStall
specifier|public
specifier|static
name|void
name|checkMkdirsStall
parameter_list|(
name|long
name|currentTime
parameter_list|)
block|{
name|Thread
name|mkdirsThread1
init|=
name|mkdirsThread
decl_stmt|;
name|long
name|stallTime
init|=
name|currentTime
operator|-
name|mkdirsStartTime
decl_stmt|;
if|if
condition|(
name|mkdirsThread1
operator|!=
literal|null
operator|&&
operator|(
name|stallTime
operator|>
name|mkdirsStallTimeout
operator|)
condition|)
block|{
name|logger
operator|.
name|error
argument_list|(
literal|"mkdirs stalled for {} on {}, trying to interrupt"
argument_list|,
operator|new
name|TimeValue
argument_list|(
name|stallTime
argument_list|)
argument_list|,
name|mkdirsThread1
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|mkdirsThread1
operator|.
name|interrupt
argument_list|()
expr_stmt|;
comment|// try and interrupt it...
block|}
block|}
DECL|method|maxOpenFiles
specifier|public
specifier|static
name|int
name|maxOpenFiles
parameter_list|(
name|File
name|testDir
parameter_list|)
block|{
name|boolean
name|dirCreated
init|=
literal|false
decl_stmt|;
if|if
condition|(
operator|!
name|testDir
operator|.
name|exists
argument_list|()
condition|)
block|{
name|dirCreated
operator|=
literal|true
expr_stmt|;
name|testDir
operator|.
name|mkdirs
argument_list|()
expr_stmt|;
block|}
name|List
argument_list|<
name|RandomAccessFile
argument_list|>
name|files
init|=
operator|new
name|ArrayList
argument_list|<
name|RandomAccessFile
argument_list|>
argument_list|()
decl_stmt|;
try|try
block|{
while|while
condition|(
literal|true
condition|)
block|{
name|files
operator|.
name|add
argument_list|(
operator|new
name|RandomAccessFile
argument_list|(
operator|new
name|File
argument_list|(
name|testDir
argument_list|,
literal|"tmp"
operator|+
name|files
operator|.
name|size
argument_list|()
argument_list|)
argument_list|,
literal|"rw"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
name|int
name|i
init|=
literal|0
decl_stmt|;
for|for
control|(
name|RandomAccessFile
name|raf
range|:
name|files
control|)
block|{
try|try
block|{
name|raf
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// ignore
block|}
operator|new
name|File
argument_list|(
name|testDir
argument_list|,
literal|"tmp"
operator|+
name|i
operator|++
argument_list|)
operator|.
name|delete
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|dirCreated
condition|)
block|{
name|deleteRecursively
argument_list|(
name|testDir
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|files
operator|.
name|size
argument_list|()
return|;
block|}
DECL|method|hasExtensions
specifier|public
specifier|static
name|boolean
name|hasExtensions
parameter_list|(
name|File
name|root
parameter_list|,
name|String
modifier|...
name|extensions
parameter_list|)
block|{
if|if
condition|(
name|root
operator|!=
literal|null
operator|&&
name|root
operator|.
name|exists
argument_list|()
condition|)
block|{
if|if
condition|(
name|root
operator|.
name|isDirectory
argument_list|()
condition|)
block|{
name|File
index|[]
name|children
init|=
name|root
operator|.
name|listFiles
argument_list|()
decl_stmt|;
if|if
condition|(
name|children
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|File
name|child
range|:
name|children
control|)
block|{
if|if
condition|(
name|child
operator|.
name|isDirectory
argument_list|()
condition|)
block|{
name|boolean
name|has
init|=
name|hasExtensions
argument_list|(
name|child
argument_list|,
name|extensions
argument_list|)
decl_stmt|;
if|if
condition|(
name|has
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
else|else
block|{
for|for
control|(
name|String
name|extension
range|:
name|extensions
control|)
block|{
if|if
condition|(
name|child
operator|.
name|getName
argument_list|()
operator|.
name|endsWith
argument_list|(
name|extension
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
block|}
block|}
block|}
block|}
block|}
return|return
literal|false
return|;
block|}
DECL|method|deleteRecursively
specifier|public
specifier|static
name|boolean
name|deleteRecursively
parameter_list|(
name|File
name|root
parameter_list|)
block|{
return|return
name|deleteRecursively
argument_list|(
name|root
argument_list|,
literal|true
argument_list|)
return|;
block|}
DECL|method|innerDeleteRecursively
specifier|private
specifier|static
name|boolean
name|innerDeleteRecursively
parameter_list|(
name|File
name|root
parameter_list|)
block|{
return|return
name|deleteRecursively
argument_list|(
name|root
argument_list|,
literal|true
argument_list|)
return|;
block|}
comment|/**      * Delete the supplied {@link java.io.File} - for directories,      * recursively delete any nested directories or files as well.      *      * @param root       the root<code>File</code> to delete      * @param deleteRoot whether or not to delete the root itself or just the content of the root.      * @return<code>true</code> if the<code>File</code> was deleted,      *         otherwise<code>false</code>      */
DECL|method|deleteRecursively
specifier|public
specifier|static
name|boolean
name|deleteRecursively
parameter_list|(
name|File
name|root
parameter_list|,
name|boolean
name|deleteRoot
parameter_list|)
block|{
if|if
condition|(
name|root
operator|!=
literal|null
operator|&&
name|root
operator|.
name|exists
argument_list|()
condition|)
block|{
if|if
condition|(
name|root
operator|.
name|isDirectory
argument_list|()
condition|)
block|{
name|File
index|[]
name|children
init|=
name|root
operator|.
name|listFiles
argument_list|()
decl_stmt|;
if|if
condition|(
name|children
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|File
name|aChildren
range|:
name|children
control|)
block|{
name|innerDeleteRecursively
argument_list|(
name|aChildren
argument_list|)
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|deleteRoot
condition|)
block|{
return|return
name|root
operator|.
name|delete
argument_list|()
return|;
block|}
else|else
block|{
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
DECL|method|syncFile
specifier|public
specifier|static
name|void
name|syncFile
parameter_list|(
name|File
name|fileToSync
parameter_list|)
throws|throws
name|IOException
block|{
name|boolean
name|success
init|=
literal|false
decl_stmt|;
name|int
name|retryCount
init|=
literal|0
decl_stmt|;
name|IOException
name|exc
init|=
literal|null
decl_stmt|;
while|while
condition|(
operator|!
name|success
operator|&&
name|retryCount
operator|<
literal|5
condition|)
block|{
name|retryCount
operator|++
expr_stmt|;
name|RandomAccessFile
name|file
init|=
literal|null
decl_stmt|;
try|try
block|{
try|try
block|{
name|file
operator|=
operator|new
name|RandomAccessFile
argument_list|(
name|fileToSync
argument_list|,
literal|"rw"
argument_list|)
expr_stmt|;
name|file
operator|.
name|getFD
argument_list|()
operator|.
name|sync
argument_list|()
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
name|file
operator|!=
literal|null
condition|)
name|file
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
if|if
condition|(
name|exc
operator|==
literal|null
condition|)
name|exc
operator|=
name|ioe
expr_stmt|;
try|try
block|{
comment|// Pause 5 msec
name|Thread
operator|.
name|sleep
argument_list|(
literal|5
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ie
parameter_list|)
block|{
throw|throw
operator|new
name|InterruptedIOException
argument_list|(
name|ie
operator|.
name|getMessage
argument_list|()
argument_list|)
throw|;
block|}
block|}
block|}
block|}
DECL|method|copyFile
specifier|public
specifier|static
name|void
name|copyFile
parameter_list|(
name|File
name|sourceFile
parameter_list|,
name|File
name|destinationFile
parameter_list|)
throws|throws
name|IOException
block|{
name|FileInputStream
name|sourceIs
init|=
literal|null
decl_stmt|;
name|FileChannel
name|source
init|=
literal|null
decl_stmt|;
name|FileOutputStream
name|destinationOs
init|=
literal|null
decl_stmt|;
name|FileChannel
name|destination
init|=
literal|null
decl_stmt|;
try|try
block|{
name|sourceIs
operator|=
operator|new
name|FileInputStream
argument_list|(
name|sourceFile
argument_list|)
expr_stmt|;
name|source
operator|=
name|sourceIs
operator|.
name|getChannel
argument_list|()
expr_stmt|;
name|destinationOs
operator|=
operator|new
name|FileOutputStream
argument_list|(
name|destinationFile
argument_list|)
expr_stmt|;
name|destination
operator|=
name|destinationOs
operator|.
name|getChannel
argument_list|()
expr_stmt|;
name|destination
operator|.
name|transferFrom
argument_list|(
name|source
argument_list|,
literal|0
argument_list|,
name|source
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|source
operator|!=
literal|null
condition|)
block|{
name|source
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|sourceIs
operator|!=
literal|null
condition|)
block|{
name|sourceIs
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|destination
operator|!=
literal|null
condition|)
block|{
name|destination
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|destinationOs
operator|!=
literal|null
condition|)
block|{
name|destinationOs
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
DECL|method|FileSystemUtils
specifier|private
name|FileSystemUtils
parameter_list|()
block|{      }
block|}
end_class

end_unit

