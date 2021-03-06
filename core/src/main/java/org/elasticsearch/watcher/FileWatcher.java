begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.watcher
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|watcher
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|logging
operator|.
name|log4j
operator|.
name|Logger
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
name|common
operator|.
name|logging
operator|.
name|Loggers
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
name|nio
operator|.
name|file
operator|.
name|Files
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|file
operator|.
name|Path
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|file
operator|.
name|attribute
operator|.
name|BasicFileAttributes
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
import|;
end_import

begin_comment
comment|/**  * File resources watcher  *  * The file watcher checks directory and all its subdirectories for file changes and notifies its listeners accordingly  */
end_comment

begin_class
DECL|class|FileWatcher
specifier|public
class|class
name|FileWatcher
extends|extends
name|AbstractResourceWatcher
argument_list|<
name|FileChangesListener
argument_list|>
block|{
DECL|field|rootFileObserver
specifier|private
name|FileObserver
name|rootFileObserver
decl_stmt|;
DECL|field|file
specifier|private
name|Path
name|file
decl_stmt|;
DECL|field|logger
specifier|private
specifier|static
specifier|final
name|Logger
name|logger
init|=
name|Loggers
operator|.
name|getLogger
argument_list|(
name|FileWatcher
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**      * Creates new file watcher on the given directory      */
DECL|method|FileWatcher
specifier|public
name|FileWatcher
parameter_list|(
name|Path
name|file
parameter_list|)
block|{
name|this
operator|.
name|file
operator|=
name|file
expr_stmt|;
name|rootFileObserver
operator|=
operator|new
name|FileObserver
argument_list|(
name|file
argument_list|)
expr_stmt|;
block|}
comment|/**      * Clears any state with the FileWatcher, making all files show up as new      */
DECL|method|clearState
specifier|public
name|void
name|clearState
parameter_list|()
block|{
name|rootFileObserver
operator|=
operator|new
name|FileObserver
argument_list|(
name|file
argument_list|)
expr_stmt|;
try|try
block|{
name|rootFileObserver
operator|.
name|init
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// ignore IOException
block|}
block|}
annotation|@
name|Override
DECL|method|doInit
specifier|protected
name|void
name|doInit
parameter_list|()
throws|throws
name|IOException
block|{
name|rootFileObserver
operator|.
name|init
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doCheckAndNotify
specifier|protected
name|void
name|doCheckAndNotify
parameter_list|()
throws|throws
name|IOException
block|{
name|rootFileObserver
operator|.
name|checkAndNotify
argument_list|()
expr_stmt|;
block|}
DECL|field|EMPTY_DIRECTORY
specifier|private
specifier|static
name|FileObserver
index|[]
name|EMPTY_DIRECTORY
init|=
operator|new
name|FileObserver
index|[
literal|0
index|]
decl_stmt|;
DECL|class|FileObserver
specifier|private
class|class
name|FileObserver
block|{
DECL|field|file
specifier|private
name|Path
name|file
decl_stmt|;
DECL|field|exists
specifier|private
name|boolean
name|exists
decl_stmt|;
DECL|field|length
specifier|private
name|long
name|length
decl_stmt|;
DECL|field|lastModified
specifier|private
name|long
name|lastModified
decl_stmt|;
DECL|field|isDirectory
specifier|private
name|boolean
name|isDirectory
decl_stmt|;
DECL|field|children
specifier|private
name|FileObserver
index|[]
name|children
decl_stmt|;
DECL|method|FileObserver
name|FileObserver
parameter_list|(
name|Path
name|file
parameter_list|)
block|{
name|this
operator|.
name|file
operator|=
name|file
expr_stmt|;
block|}
DECL|method|checkAndNotify
specifier|public
name|void
name|checkAndNotify
parameter_list|()
throws|throws
name|IOException
block|{
name|boolean
name|prevExists
init|=
name|exists
decl_stmt|;
name|boolean
name|prevIsDirectory
init|=
name|isDirectory
decl_stmt|;
name|long
name|prevLength
init|=
name|length
decl_stmt|;
name|long
name|prevLastModified
init|=
name|lastModified
decl_stmt|;
name|exists
operator|=
name|Files
operator|.
name|exists
argument_list|(
name|file
argument_list|)
expr_stmt|;
comment|// TODO we might use the new NIO2 API to get real notification?
if|if
condition|(
name|exists
condition|)
block|{
name|BasicFileAttributes
name|attributes
init|=
name|Files
operator|.
name|readAttributes
argument_list|(
name|file
argument_list|,
name|BasicFileAttributes
operator|.
name|class
argument_list|)
decl_stmt|;
name|isDirectory
operator|=
name|attributes
operator|.
name|isDirectory
argument_list|()
expr_stmt|;
if|if
condition|(
name|isDirectory
condition|)
block|{
name|length
operator|=
literal|0
expr_stmt|;
name|lastModified
operator|=
literal|0
expr_stmt|;
block|}
else|else
block|{
name|length
operator|=
name|attributes
operator|.
name|size
argument_list|()
expr_stmt|;
name|lastModified
operator|=
name|attributes
operator|.
name|lastModifiedTime
argument_list|()
operator|.
name|toMillis
argument_list|()
expr_stmt|;
block|}
block|}
else|else
block|{
name|isDirectory
operator|=
literal|false
expr_stmt|;
name|length
operator|=
literal|0
expr_stmt|;
name|lastModified
operator|=
literal|0
expr_stmt|;
block|}
comment|// Perform notifications and update children for the current file
if|if
condition|(
name|prevExists
condition|)
block|{
if|if
condition|(
name|exists
condition|)
block|{
if|if
condition|(
name|isDirectory
condition|)
block|{
if|if
condition|(
name|prevIsDirectory
condition|)
block|{
comment|// Remained a directory
name|updateChildren
argument_list|()
expr_stmt|;
block|}
else|else
block|{
comment|// File replaced by directory
name|onFileDeleted
argument_list|()
expr_stmt|;
name|onDirectoryCreated
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
if|if
condition|(
name|prevIsDirectory
condition|)
block|{
comment|// Directory replaced by file
name|onDirectoryDeleted
argument_list|()
expr_stmt|;
name|onFileCreated
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// Remained file
if|if
condition|(
name|prevLastModified
operator|!=
name|lastModified
operator|||
name|prevLength
operator|!=
name|length
condition|)
block|{
name|onFileChanged
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
else|else
block|{
comment|// Deleted
if|if
condition|(
name|prevIsDirectory
condition|)
block|{
name|onDirectoryDeleted
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|onFileDeleted
argument_list|()
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
comment|// Created
if|if
condition|(
name|exists
condition|)
block|{
if|if
condition|(
name|isDirectory
condition|)
block|{
name|onDirectoryCreated
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|onFileCreated
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
DECL|method|init
specifier|private
name|void
name|init
parameter_list|(
name|boolean
name|initial
parameter_list|)
throws|throws
name|IOException
block|{
name|exists
operator|=
name|Files
operator|.
name|exists
argument_list|(
name|file
argument_list|)
expr_stmt|;
if|if
condition|(
name|exists
condition|)
block|{
name|BasicFileAttributes
name|attributes
init|=
name|Files
operator|.
name|readAttributes
argument_list|(
name|file
argument_list|,
name|BasicFileAttributes
operator|.
name|class
argument_list|)
decl_stmt|;
name|isDirectory
operator|=
name|attributes
operator|.
name|isDirectory
argument_list|()
expr_stmt|;
if|if
condition|(
name|isDirectory
condition|)
block|{
name|onDirectoryCreated
argument_list|(
name|initial
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|length
operator|=
name|attributes
operator|.
name|size
argument_list|()
expr_stmt|;
name|lastModified
operator|=
name|attributes
operator|.
name|lastModifiedTime
argument_list|()
operator|.
name|toMillis
argument_list|()
expr_stmt|;
name|onFileCreated
argument_list|(
name|initial
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|createChild
specifier|private
name|FileObserver
name|createChild
parameter_list|(
name|Path
name|file
parameter_list|,
name|boolean
name|initial
parameter_list|)
throws|throws
name|IOException
block|{
name|FileObserver
name|child
init|=
operator|new
name|FileObserver
argument_list|(
name|file
argument_list|)
decl_stmt|;
name|child
operator|.
name|init
argument_list|(
name|initial
argument_list|)
expr_stmt|;
return|return
name|child
return|;
block|}
DECL|method|listFiles
specifier|private
name|Path
index|[]
name|listFiles
parameter_list|()
throws|throws
name|IOException
block|{
specifier|final
name|Path
index|[]
name|files
init|=
name|FileSystemUtils
operator|.
name|files
argument_list|(
name|file
argument_list|)
decl_stmt|;
name|Arrays
operator|.
name|sort
argument_list|(
name|files
argument_list|)
expr_stmt|;
return|return
name|files
return|;
block|}
DECL|method|listChildren
specifier|private
name|FileObserver
index|[]
name|listChildren
parameter_list|(
name|boolean
name|initial
parameter_list|)
throws|throws
name|IOException
block|{
name|Path
index|[]
name|files
init|=
name|listFiles
argument_list|()
decl_stmt|;
if|if
condition|(
name|files
operator|!=
literal|null
operator|&&
name|files
operator|.
name|length
operator|>
literal|0
condition|)
block|{
name|FileObserver
index|[]
name|children
init|=
operator|new
name|FileObserver
index|[
name|files
operator|.
name|length
index|]
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|files
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|children
index|[
name|i
index|]
operator|=
name|createChild
argument_list|(
name|files
index|[
name|i
index|]
argument_list|,
name|initial
argument_list|)
expr_stmt|;
block|}
return|return
name|children
return|;
block|}
else|else
block|{
return|return
name|EMPTY_DIRECTORY
return|;
block|}
block|}
DECL|method|updateChildren
specifier|private
name|void
name|updateChildren
parameter_list|()
throws|throws
name|IOException
block|{
name|Path
index|[]
name|files
init|=
name|listFiles
argument_list|()
decl_stmt|;
if|if
condition|(
name|files
operator|!=
literal|null
operator|&&
name|files
operator|.
name|length
operator|>
literal|0
condition|)
block|{
name|FileObserver
index|[]
name|newChildren
init|=
operator|new
name|FileObserver
index|[
name|files
operator|.
name|length
index|]
decl_stmt|;
name|int
name|child
init|=
literal|0
decl_stmt|;
name|int
name|file
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|file
operator|<
name|files
operator|.
name|length
operator|||
name|child
operator|<
name|children
operator|.
name|length
condition|)
block|{
name|int
name|compare
decl_stmt|;
if|if
condition|(
name|file
operator|>=
name|files
operator|.
name|length
condition|)
block|{
name|compare
operator|=
operator|-
literal|1
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|child
operator|>=
name|children
operator|.
name|length
condition|)
block|{
name|compare
operator|=
literal|1
expr_stmt|;
block|}
else|else
block|{
name|compare
operator|=
name|children
index|[
name|child
index|]
operator|.
name|file
operator|.
name|compareTo
argument_list|(
name|files
index|[
name|file
index|]
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|compare
operator|==
literal|0
condition|)
block|{
comment|// Same file copy it and update
name|children
index|[
name|child
index|]
operator|.
name|checkAndNotify
argument_list|()
expr_stmt|;
name|newChildren
index|[
name|file
index|]
operator|=
name|children
index|[
name|child
index|]
expr_stmt|;
name|file
operator|++
expr_stmt|;
name|child
operator|++
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|compare
operator|>
literal|0
condition|)
block|{
comment|// This child doesn't appear in the old list - init it
name|newChildren
index|[
name|file
index|]
operator|=
name|createChild
argument_list|(
name|files
index|[
name|file
index|]
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|file
operator|++
expr_stmt|;
block|}
else|else
block|{
comment|// The child from the old list is missing in the new list
comment|// Delete it
name|deleteChild
argument_list|(
name|child
argument_list|)
expr_stmt|;
name|child
operator|++
expr_stmt|;
block|}
block|}
block|}
name|children
operator|=
name|newChildren
expr_stmt|;
block|}
else|else
block|{
comment|// No files - delete all children
for|for
control|(
name|int
name|child
init|=
literal|0
init|;
name|child
operator|<
name|children
operator|.
name|length
condition|;
name|child
operator|++
control|)
block|{
name|deleteChild
argument_list|(
name|child
argument_list|)
expr_stmt|;
block|}
name|children
operator|=
name|EMPTY_DIRECTORY
expr_stmt|;
block|}
block|}
DECL|method|deleteChild
specifier|private
name|void
name|deleteChild
parameter_list|(
name|int
name|child
parameter_list|)
block|{
if|if
condition|(
name|children
index|[
name|child
index|]
operator|.
name|exists
condition|)
block|{
if|if
condition|(
name|children
index|[
name|child
index|]
operator|.
name|isDirectory
condition|)
block|{
name|children
index|[
name|child
index|]
operator|.
name|onDirectoryDeleted
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|children
index|[
name|child
index|]
operator|.
name|onFileDeleted
argument_list|()
expr_stmt|;
block|}
block|}
block|}
DECL|method|onFileCreated
specifier|private
name|void
name|onFileCreated
parameter_list|(
name|boolean
name|initial
parameter_list|)
block|{
for|for
control|(
name|FileChangesListener
name|listener
range|:
name|listeners
argument_list|()
control|)
block|{
try|try
block|{
if|if
condition|(
name|initial
condition|)
block|{
name|listener
operator|.
name|onFileInit
argument_list|(
name|file
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|listener
operator|.
name|onFileCreated
argument_list|(
name|file
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"cannot notify file changes listener"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|onFileDeleted
specifier|private
name|void
name|onFileDeleted
parameter_list|()
block|{
for|for
control|(
name|FileChangesListener
name|listener
range|:
name|listeners
argument_list|()
control|)
block|{
try|try
block|{
name|listener
operator|.
name|onFileDeleted
argument_list|(
name|file
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"cannot notify file changes listener"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|onFileChanged
specifier|private
name|void
name|onFileChanged
parameter_list|()
block|{
for|for
control|(
name|FileChangesListener
name|listener
range|:
name|listeners
argument_list|()
control|)
block|{
try|try
block|{
name|listener
operator|.
name|onFileChanged
argument_list|(
name|file
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"cannot notify file changes listener"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|onDirectoryCreated
specifier|private
name|void
name|onDirectoryCreated
parameter_list|(
name|boolean
name|initial
parameter_list|)
throws|throws
name|IOException
block|{
for|for
control|(
name|FileChangesListener
name|listener
range|:
name|listeners
argument_list|()
control|)
block|{
try|try
block|{
if|if
condition|(
name|initial
condition|)
block|{
name|listener
operator|.
name|onDirectoryInit
argument_list|(
name|file
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|listener
operator|.
name|onDirectoryCreated
argument_list|(
name|file
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"cannot notify file changes listener"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
name|children
operator|=
name|listChildren
argument_list|(
name|initial
argument_list|)
expr_stmt|;
block|}
DECL|method|onDirectoryDeleted
specifier|private
name|void
name|onDirectoryDeleted
parameter_list|()
block|{
comment|// First delete all children
for|for
control|(
name|int
name|child
init|=
literal|0
init|;
name|child
operator|<
name|children
operator|.
name|length
condition|;
name|child
operator|++
control|)
block|{
name|deleteChild
argument_list|(
name|child
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|FileChangesListener
name|listener
range|:
name|listeners
argument_list|()
control|)
block|{
try|try
block|{
name|listener
operator|.
name|onDirectoryDeleted
argument_list|(
name|file
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"cannot notify file changes listener"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
end_class

end_unit

