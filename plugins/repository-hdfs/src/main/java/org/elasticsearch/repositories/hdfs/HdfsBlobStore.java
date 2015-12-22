begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.repositories.hdfs
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|repositories
operator|.
name|hdfs
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|fs
operator|.
name|FileAlreadyExistsException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|fs
operator|.
name|FileContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|fs
operator|.
name|Path
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
name|AlreadyClosedException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|SpecialPermission
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
name|blobstore
operator|.
name|BlobContainer
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
name|blobstore
operator|.
name|BlobPath
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
name|blobstore
operator|.
name|BlobStore
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
name|security
operator|.
name|AccessController
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|PrivilegedActionException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|PrivilegedExceptionAction
import|;
end_import

begin_class
DECL|class|HdfsBlobStore
specifier|final
class|class
name|HdfsBlobStore
implements|implements
name|BlobStore
block|{
DECL|field|root
specifier|private
specifier|final
name|Path
name|root
decl_stmt|;
DECL|field|fileContext
specifier|private
specifier|final
name|FileContext
name|fileContext
decl_stmt|;
DECL|field|bufferSize
specifier|private
specifier|final
name|int
name|bufferSize
decl_stmt|;
DECL|field|closed
specifier|private
specifier|volatile
name|boolean
name|closed
decl_stmt|;
DECL|method|HdfsBlobStore
name|HdfsBlobStore
parameter_list|(
name|FileContext
name|fileContext
parameter_list|,
name|String
name|path
parameter_list|,
name|int
name|bufferSize
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|fileContext
operator|=
name|fileContext
expr_stmt|;
name|this
operator|.
name|bufferSize
operator|=
name|bufferSize
expr_stmt|;
name|this
operator|.
name|root
operator|=
name|execute
argument_list|(
operator|new
name|Operation
argument_list|<
name|Path
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Path
name|run
parameter_list|(
name|FileContext
name|fileContext
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|fileContext
operator|.
name|makeQualified
argument_list|(
operator|new
name|Path
argument_list|(
name|path
argument_list|)
argument_list|)
return|;
block|}
block|}
argument_list|)
expr_stmt|;
try|try
block|{
name|mkdirs
argument_list|(
name|root
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|FileAlreadyExistsException
name|ok
parameter_list|)
block|{
comment|// behaves like Files.createDirectories
block|}
block|}
DECL|method|mkdirs
specifier|private
name|void
name|mkdirs
parameter_list|(
name|Path
name|path
parameter_list|)
throws|throws
name|IOException
block|{
name|execute
argument_list|(
operator|new
name|Operation
argument_list|<
name|Void
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Void
name|run
parameter_list|(
name|FileContext
name|fileContext
parameter_list|)
throws|throws
name|IOException
block|{
name|fileContext
operator|.
name|mkdir
argument_list|(
name|path
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|delete
specifier|public
name|void
name|delete
parameter_list|(
name|BlobPath
name|path
parameter_list|)
throws|throws
name|IOException
block|{
name|execute
argument_list|(
operator|new
name|Operation
argument_list|<
name|Void
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Void
name|run
parameter_list|(
name|FileContext
name|fc
parameter_list|)
throws|throws
name|IOException
block|{
name|fc
operator|.
name|delete
argument_list|(
name|translateToHdfsPath
argument_list|(
name|path
argument_list|)
argument_list|,
literal|true
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
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
name|root
operator|.
name|toUri
argument_list|()
operator|.
name|toString
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|blobContainer
specifier|public
name|BlobContainer
name|blobContainer
parameter_list|(
name|BlobPath
name|path
parameter_list|)
block|{
return|return
operator|new
name|HdfsBlobContainer
argument_list|(
name|path
argument_list|,
name|this
argument_list|,
name|buildHdfsPath
argument_list|(
name|path
argument_list|)
argument_list|,
name|bufferSize
argument_list|)
return|;
block|}
DECL|method|buildHdfsPath
specifier|private
name|Path
name|buildHdfsPath
parameter_list|(
name|BlobPath
name|blobPath
parameter_list|)
block|{
specifier|final
name|Path
name|path
init|=
name|translateToHdfsPath
argument_list|(
name|blobPath
argument_list|)
decl_stmt|;
try|try
block|{
name|mkdirs
argument_list|(
name|path
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|FileAlreadyExistsException
name|ok
parameter_list|)
block|{
comment|// behaves like Files.createDirectories
block|}
catch|catch
parameter_list|(
name|IOException
name|ex
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticsearchException
argument_list|(
literal|"failed to create blob container"
argument_list|,
name|ex
argument_list|)
throw|;
block|}
return|return
name|path
return|;
block|}
DECL|method|translateToHdfsPath
specifier|private
name|Path
name|translateToHdfsPath
parameter_list|(
name|BlobPath
name|blobPath
parameter_list|)
block|{
name|Path
name|path
init|=
name|root
decl_stmt|;
for|for
control|(
name|String
name|p
range|:
name|blobPath
control|)
block|{
name|path
operator|=
operator|new
name|Path
argument_list|(
name|path
argument_list|,
name|p
argument_list|)
expr_stmt|;
block|}
return|return
name|path
return|;
block|}
DECL|interface|Operation
interface|interface
name|Operation
parameter_list|<
name|V
parameter_list|>
block|{
DECL|method|run
name|V
name|run
parameter_list|(
name|FileContext
name|fileContext
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
comment|/**      * Executes the provided operation against this store      */
DECL|method|execute
parameter_list|<
name|V
parameter_list|>
name|V
name|execute
parameter_list|(
name|Operation
argument_list|<
name|V
argument_list|>
name|operation
parameter_list|)
throws|throws
name|IOException
block|{
name|SecurityManager
name|sm
init|=
name|System
operator|.
name|getSecurityManager
argument_list|()
decl_stmt|;
if|if
condition|(
name|sm
operator|!=
literal|null
condition|)
block|{
comment|// unprivileged code such as scripts do not have SpecialPermission
name|sm
operator|.
name|checkPermission
argument_list|(
operator|new
name|SpecialPermission
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|closed
condition|)
block|{
throw|throw
operator|new
name|AlreadyClosedException
argument_list|(
literal|"HdfsBlobStore is closed: "
operator|+
name|this
argument_list|)
throw|;
block|}
try|try
block|{
return|return
name|AccessController
operator|.
name|doPrivileged
argument_list|(
operator|new
name|PrivilegedExceptionAction
argument_list|<
name|V
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|V
name|run
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|operation
operator|.
name|run
argument_list|(
name|fileContext
argument_list|)
return|;
block|}
block|}
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|PrivilegedActionException
name|pae
parameter_list|)
block|{
throw|throw
operator|(
name|IOException
operator|)
name|pae
operator|.
name|getException
argument_list|()
throw|;
block|}
block|}
annotation|@
name|Override
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
block|{
name|closed
operator|=
literal|true
expr_stmt|;
block|}
block|}
end_class

end_unit

