begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Iterators
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
name|java
operator|.
name|io
operator|.
name|BufferedReader
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
name|InputStreamReader
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Reader
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URL
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|charset
operator|.
name|Charset
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|charset
operator|.
name|CharsetDecoder
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
name|*
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

begin_import
import|import static
name|java
operator|.
name|nio
operator|.
name|file
operator|.
name|FileVisitResult
operator|.
name|CONTINUE
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|nio
operator|.
name|file
operator|.
name|FileVisitResult
operator|.
name|SKIP_SUBTREE
import|;
end_import

begin_comment
comment|/**  * Elasticsearch utils to work with {@link java.nio.file.Path}  */
end_comment

begin_class
DECL|class|FileSystemUtils
specifier|public
specifier|final
class|class
name|FileSystemUtils
block|{
DECL|method|FileSystemUtils
specifier|private
name|FileSystemUtils
parameter_list|()
block|{}
comment|// only static methods
comment|/**      * Returns<code>true</code> iff a file under the given root has one of the given extensions. This method      * will travers directories recursively and will terminate once any of the extensions was found. This      * methods will not follow any links.      *      * @param root the root directory to travers. Must be a directory      * @param extensions the file extensions to look for      * @return<code>true</code> iff a file under the given root has one of the given extensions, otherwise<code>false</code>      * @throws IOException if an IOException occurs or if the given root path is not a directory.      */
DECL|method|hasExtensions
specifier|public
specifier|static
name|boolean
name|hasExtensions
parameter_list|(
name|Path
name|root
parameter_list|,
specifier|final
name|String
modifier|...
name|extensions
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|AtomicBoolean
name|retVal
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
name|Files
operator|.
name|walkFileTree
argument_list|(
name|root
argument_list|,
operator|new
name|SimpleFileVisitor
argument_list|<
name|Path
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|FileVisitResult
name|visitFile
parameter_list|(
name|Path
name|file
parameter_list|,
name|BasicFileAttributes
name|attrs
parameter_list|)
throws|throws
name|IOException
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
name|file
operator|.
name|getFileName
argument_list|()
operator|.
name|toString
argument_list|()
operator|.
name|endsWith
argument_list|(
name|extension
argument_list|)
condition|)
block|{
name|retVal
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
return|return
name|FileVisitResult
operator|.
name|TERMINATE
return|;
block|}
block|}
return|return
name|super
operator|.
name|visitFile
argument_list|(
name|file
argument_list|,
name|attrs
argument_list|)
return|;
block|}
block|}
argument_list|)
expr_stmt|;
return|return
name|retVal
operator|.
name|get
argument_list|()
return|;
block|}
comment|/**      * Returns<code>true</code> iff one of the files exists otherwise<code>false</code>      */
DECL|method|exists
specifier|public
specifier|static
name|boolean
name|exists
parameter_list|(
name|Path
modifier|...
name|files
parameter_list|)
block|{
for|for
control|(
name|Path
name|file
range|:
name|files
control|)
block|{
if|if
condition|(
name|Files
operator|.
name|exists
argument_list|(
name|file
argument_list|)
condition|)
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
comment|/**      * Appends the path to the given base and strips N elements off the path if strip is> 0.      */
DECL|method|append
specifier|public
specifier|static
name|Path
name|append
parameter_list|(
name|Path
name|base
parameter_list|,
name|Path
name|path
parameter_list|,
name|int
name|strip
parameter_list|)
block|{
for|for
control|(
name|Path
name|subPath
range|:
name|path
control|)
block|{
if|if
condition|(
name|strip
operator|--
operator|>
literal|0
condition|)
block|{
continue|continue;
block|}
name|base
operator|=
name|base
operator|.
name|resolve
argument_list|(
name|subPath
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|base
return|;
block|}
comment|/**      * Deletes all subdirectories in the given path recursively      * @throws java.lang.IllegalArgumentException if the given path is not a directory      */
DECL|method|deleteSubDirectories
specifier|public
specifier|static
name|void
name|deleteSubDirectories
parameter_list|(
name|Path
modifier|...
name|paths
parameter_list|)
throws|throws
name|IOException
block|{
for|for
control|(
name|Path
name|path
range|:
name|paths
control|)
block|{
try|try
init|(
name|DirectoryStream
argument_list|<
name|Path
argument_list|>
name|stream
init|=
name|Files
operator|.
name|newDirectoryStream
argument_list|(
name|path
argument_list|)
init|)
block|{
for|for
control|(
name|Path
name|subPath
range|:
name|stream
control|)
block|{
if|if
condition|(
name|Files
operator|.
name|isDirectory
argument_list|(
name|subPath
argument_list|)
condition|)
block|{
name|IOUtils
operator|.
name|rm
argument_list|(
name|subPath
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
comment|/**      * Check that a directory exists, is a directory and is readable      * by the current user      */
DECL|method|isAccessibleDirectory
specifier|public
specifier|static
name|boolean
name|isAccessibleDirectory
parameter_list|(
name|Path
name|directory
parameter_list|,
name|ESLogger
name|logger
parameter_list|)
block|{
assert|assert
name|directory
operator|!=
literal|null
operator|&&
name|logger
operator|!=
literal|null
assert|;
if|if
condition|(
operator|!
name|Files
operator|.
name|exists
argument_list|(
name|directory
argument_list|)
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"[{}] directory does not exist."
argument_list|,
name|directory
operator|.
name|toAbsolutePath
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
if|if
condition|(
operator|!
name|Files
operator|.
name|isDirectory
argument_list|(
name|directory
argument_list|)
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"[{}] should be a directory but is not."
argument_list|,
name|directory
operator|.
name|toAbsolutePath
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
if|if
condition|(
operator|!
name|Files
operator|.
name|isReadable
argument_list|(
name|directory
argument_list|)
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"[{}] directory is not readable."
argument_list|,
name|directory
operator|.
name|toAbsolutePath
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
comment|/**      * Opens the given url for reading returning a {@code BufferedReader} that may be      * used to read text from the URL in an efficient manner. Bytes from the      * file are decoded into characters using the specified charset.      */
DECL|method|newBufferedReader
specifier|public
specifier|static
name|BufferedReader
name|newBufferedReader
parameter_list|(
name|URL
name|url
parameter_list|,
name|Charset
name|cs
parameter_list|)
throws|throws
name|IOException
block|{
name|CharsetDecoder
name|decoder
init|=
name|cs
operator|.
name|newDecoder
argument_list|()
decl_stmt|;
name|Reader
name|reader
init|=
operator|new
name|InputStreamReader
argument_list|(
name|url
operator|.
name|openStream
argument_list|()
argument_list|,
name|decoder
argument_list|)
decl_stmt|;
return|return
operator|new
name|BufferedReader
argument_list|(
name|reader
argument_list|)
return|;
block|}
comment|/**      * This utility copy a full directory content (excluded) under      * a new directory but without overwriting existing files.      *      * When a file already exists in destination dir, the source file is copied under      * destination directory but with a suffix appended if set or source file is ignored      * if suffix is not set (null).      * @param source Source directory (for example /tmp/es/src)      * @param destination Destination directory (destination directory /tmp/es/dst)      * @param suffix When not null, files are copied with a suffix appended to the original name (eg: ".new")      *               When null, files are ignored      */
DECL|method|moveFilesWithoutOverwriting
specifier|public
specifier|static
name|void
name|moveFilesWithoutOverwriting
parameter_list|(
name|Path
name|source
parameter_list|,
specifier|final
name|Path
name|destination
parameter_list|,
specifier|final
name|String
name|suffix
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Create destination dir
name|Files
operator|.
name|createDirectories
argument_list|(
name|destination
argument_list|)
expr_stmt|;
specifier|final
name|int
name|configPathRootLevel
init|=
name|source
operator|.
name|getNameCount
argument_list|()
decl_stmt|;
comment|// We walk through the file tree from
name|Files
operator|.
name|walkFileTree
argument_list|(
name|source
argument_list|,
operator|new
name|SimpleFileVisitor
argument_list|<
name|Path
argument_list|>
argument_list|()
block|{
specifier|private
name|Path
name|buildPath
parameter_list|(
name|Path
name|path
parameter_list|)
block|{
return|return
name|destination
operator|.
name|resolve
argument_list|(
name|path
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|FileVisitResult
name|preVisitDirectory
parameter_list|(
name|Path
name|dir
parameter_list|,
name|BasicFileAttributes
name|attrs
parameter_list|)
throws|throws
name|IOException
block|{
comment|// We are now in dir. We need to remove root of config files to have a relative path
comment|// If we are not walking in root dir, we might be able to copy its content
comment|// if it does not already exist
if|if
condition|(
name|configPathRootLevel
operator|!=
name|dir
operator|.
name|getNameCount
argument_list|()
condition|)
block|{
name|Path
name|subpath
init|=
name|dir
operator|.
name|subpath
argument_list|(
name|configPathRootLevel
argument_list|,
name|dir
operator|.
name|getNameCount
argument_list|()
argument_list|)
decl_stmt|;
name|Path
name|path
init|=
name|buildPath
argument_list|(
name|subpath
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|Files
operator|.
name|exists
argument_list|(
name|path
argument_list|)
condition|)
block|{
comment|// We just move the structure to new dir
comment|// we can't do atomic move here since src / dest might be on different mounts?
name|move
argument_list|(
name|dir
argument_list|,
name|path
argument_list|)
expr_stmt|;
comment|// We just ignore sub files from here
return|return
name|FileVisitResult
operator|.
name|SKIP_SUBTREE
return|;
block|}
block|}
return|return
name|FileVisitResult
operator|.
name|CONTINUE
return|;
block|}
annotation|@
name|Override
specifier|public
name|FileVisitResult
name|visitFile
parameter_list|(
name|Path
name|file
parameter_list|,
name|BasicFileAttributes
name|attrs
parameter_list|)
throws|throws
name|IOException
block|{
name|Path
name|subpath
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|configPathRootLevel
operator|!=
name|file
operator|.
name|getNameCount
argument_list|()
condition|)
block|{
name|subpath
operator|=
name|file
operator|.
name|subpath
argument_list|(
name|configPathRootLevel
argument_list|,
name|file
operator|.
name|getNameCount
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|Path
name|path
init|=
name|buildPath
argument_list|(
name|subpath
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|Files
operator|.
name|exists
argument_list|(
name|path
argument_list|)
condition|)
block|{
comment|// We just move the new file to new dir
name|move
argument_list|(
name|file
argument_list|,
name|path
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|suffix
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
operator|!
name|isSameFile
argument_list|(
name|file
argument_list|,
name|path
argument_list|)
condition|)
block|{
comment|// If it already exists we try to copy this new version appending suffix to its name
name|path
operator|=
name|Paths
operator|.
name|get
argument_list|(
name|path
operator|.
name|toString
argument_list|()
operator|.
name|concat
argument_list|(
name|suffix
argument_list|)
argument_list|)
expr_stmt|;
comment|// We just move the file to new dir but with a new name (appended with suffix)
name|Files
operator|.
name|move
argument_list|(
name|file
argument_list|,
name|path
argument_list|,
name|StandardCopyOption
operator|.
name|REPLACE_EXISTING
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|FileVisitResult
operator|.
name|CONTINUE
return|;
block|}
comment|/**              * Compares the content of two paths by comparing them              */
specifier|private
name|boolean
name|isSameFile
parameter_list|(
name|Path
name|first
parameter_list|,
name|Path
name|second
parameter_list|)
throws|throws
name|IOException
block|{
comment|// do quick file size comparison before hashing
name|boolean
name|sameFileSize
init|=
name|Files
operator|.
name|size
argument_list|(
name|first
argument_list|)
operator|==
name|Files
operator|.
name|size
argument_list|(
name|second
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|sameFileSize
condition|)
block|{
return|return
literal|false
return|;
block|}
name|byte
index|[]
name|firstBytes
init|=
name|Files
operator|.
name|readAllBytes
argument_list|(
name|first
argument_list|)
decl_stmt|;
name|byte
index|[]
name|secondBytes
init|=
name|Files
operator|.
name|readAllBytes
argument_list|(
name|second
argument_list|)
decl_stmt|;
return|return
name|Arrays
operator|.
name|equals
argument_list|(
name|firstBytes
argument_list|,
name|secondBytes
argument_list|)
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
comment|/**      * Copy recursively a dir to a new location      * @param source source dir      * @param destination destination dir      */
DECL|method|copyDirectoryRecursively
specifier|public
specifier|static
name|void
name|copyDirectoryRecursively
parameter_list|(
name|Path
name|source
parameter_list|,
name|Path
name|destination
parameter_list|)
throws|throws
name|IOException
block|{
name|Files
operator|.
name|walkFileTree
argument_list|(
name|source
argument_list|,
operator|new
name|TreeCopier
argument_list|(
name|source
argument_list|,
name|destination
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**      * Move or rename a file to a target file. This method supports moving a file from      * different filesystems (not supported by Files.move()).      *      * @param source source file      * @param destination destination file      */
DECL|method|move
specifier|public
specifier|static
name|void
name|move
parameter_list|(
name|Path
name|source
parameter_list|,
name|Path
name|destination
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
comment|// We can't use atomic move here since source& target can be on different filesystems.
name|Files
operator|.
name|move
argument_list|(
name|source
argument_list|,
name|destination
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|DirectoryNotEmptyException
name|e
parameter_list|)
block|{
name|Files
operator|.
name|walkFileTree
argument_list|(
name|source
argument_list|,
operator|new
name|TreeCopier
argument_list|(
name|source
argument_list|,
name|destination
argument_list|,
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|TreeCopier
specifier|static
class|class
name|TreeCopier
extends|extends
name|SimpleFileVisitor
argument_list|<
name|Path
argument_list|>
block|{
DECL|field|source
specifier|private
specifier|final
name|Path
name|source
decl_stmt|;
DECL|field|target
specifier|private
specifier|final
name|Path
name|target
decl_stmt|;
DECL|field|delete
specifier|private
specifier|final
name|boolean
name|delete
decl_stmt|;
DECL|method|TreeCopier
name|TreeCopier
parameter_list|(
name|Path
name|source
parameter_list|,
name|Path
name|target
parameter_list|,
name|boolean
name|delete
parameter_list|)
block|{
name|this
operator|.
name|source
operator|=
name|source
expr_stmt|;
name|this
operator|.
name|target
operator|=
name|target
expr_stmt|;
name|this
operator|.
name|delete
operator|=
name|delete
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|preVisitDirectory
specifier|public
name|FileVisitResult
name|preVisitDirectory
parameter_list|(
name|Path
name|dir
parameter_list|,
name|BasicFileAttributes
name|attrs
parameter_list|)
block|{
name|Path
name|newDir
init|=
name|target
operator|.
name|resolve
argument_list|(
name|source
operator|.
name|relativize
argument_list|(
name|dir
argument_list|)
argument_list|)
decl_stmt|;
try|try
block|{
name|Files
operator|.
name|copy
argument_list|(
name|dir
argument_list|,
name|newDir
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|FileAlreadyExistsException
name|x
parameter_list|)
block|{
comment|// We ignore this
block|}
catch|catch
parameter_list|(
name|IOException
name|x
parameter_list|)
block|{
return|return
name|SKIP_SUBTREE
return|;
block|}
return|return
name|CONTINUE
return|;
block|}
annotation|@
name|Override
DECL|method|postVisitDirectory
specifier|public
name|FileVisitResult
name|postVisitDirectory
parameter_list|(
name|Path
name|dir
parameter_list|,
name|IOException
name|exc
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|delete
condition|)
block|{
name|IOUtils
operator|.
name|rm
argument_list|(
name|dir
argument_list|)
expr_stmt|;
block|}
return|return
name|CONTINUE
return|;
block|}
annotation|@
name|Override
DECL|method|visitFile
specifier|public
name|FileVisitResult
name|visitFile
parameter_list|(
name|Path
name|file
parameter_list|,
name|BasicFileAttributes
name|attrs
parameter_list|)
throws|throws
name|IOException
block|{
name|Path
name|newFile
init|=
name|target
operator|.
name|resolve
argument_list|(
name|source
operator|.
name|relativize
argument_list|(
name|file
argument_list|)
argument_list|)
decl_stmt|;
try|try
block|{
name|Files
operator|.
name|copy
argument_list|(
name|file
argument_list|,
name|newFile
argument_list|)
expr_stmt|;
if|if
condition|(
name|delete
condition|)
block|{
name|Files
operator|.
name|deleteIfExists
argument_list|(
name|file
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|x
parameter_list|)
block|{
comment|// We ignore this
block|}
return|return
name|CONTINUE
return|;
block|}
block|}
comment|/**      * Returns an array of all files in the given directory matching.      */
DECL|method|files
specifier|public
specifier|static
name|Path
index|[]
name|files
parameter_list|(
name|Path
name|from
parameter_list|,
name|DirectoryStream
operator|.
name|Filter
argument_list|<
name|Path
argument_list|>
name|filter
parameter_list|)
throws|throws
name|IOException
block|{
try|try
init|(
name|DirectoryStream
argument_list|<
name|Path
argument_list|>
name|stream
init|=
name|Files
operator|.
name|newDirectoryStream
argument_list|(
name|from
argument_list|,
name|filter
argument_list|)
init|)
block|{
return|return
name|Iterators
operator|.
name|toArray
argument_list|(
name|stream
operator|.
name|iterator
argument_list|()
argument_list|,
name|Path
operator|.
name|class
argument_list|)
return|;
block|}
block|}
comment|/**      * Returns an array of all files in the given directory.      */
DECL|method|files
specifier|public
specifier|static
name|Path
index|[]
name|files
parameter_list|(
name|Path
name|directory
parameter_list|)
throws|throws
name|IOException
block|{
try|try
init|(
name|DirectoryStream
argument_list|<
name|Path
argument_list|>
name|stream
init|=
name|Files
operator|.
name|newDirectoryStream
argument_list|(
name|directory
argument_list|)
init|)
block|{
return|return
name|Iterators
operator|.
name|toArray
argument_list|(
name|stream
operator|.
name|iterator
argument_list|()
argument_list|,
name|Path
operator|.
name|class
argument_list|)
return|;
block|}
block|}
comment|/**      * Returns an array of all files in the given directory matching the glob.      */
DECL|method|files
specifier|public
specifier|static
name|Path
index|[]
name|files
parameter_list|(
name|Path
name|directory
parameter_list|,
name|String
name|glob
parameter_list|)
throws|throws
name|IOException
block|{
try|try
init|(
name|DirectoryStream
argument_list|<
name|Path
argument_list|>
name|stream
init|=
name|Files
operator|.
name|newDirectoryStream
argument_list|(
name|directory
argument_list|,
name|glob
argument_list|)
init|)
block|{
return|return
name|Iterators
operator|.
name|toArray
argument_list|(
name|stream
operator|.
name|iterator
argument_list|()
argument_list|,
name|Path
operator|.
name|class
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

