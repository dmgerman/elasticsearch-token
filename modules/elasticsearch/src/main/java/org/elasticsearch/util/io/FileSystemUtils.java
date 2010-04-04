begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.util.io
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|io
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|*
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

begin_comment
comment|/**  * @author kimchy (Shay Banon)  */
end_comment

begin_class
DECL|class|FileSystemUtils
specifier|public
class|class
name|FileSystemUtils
block|{
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
name|deleteRecursively
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

