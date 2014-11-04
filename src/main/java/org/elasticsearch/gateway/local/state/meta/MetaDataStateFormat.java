begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.gateway.local.state.meta
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|gateway
operator|.
name|local
operator|.
name|state
operator|.
name|meta
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
name|base
operator|.
name|Predicate
import|;
end_import

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
name|Collections2
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
name|codecs
operator|.
name|CodecUtil
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
name|index
operator|.
name|CorruptIndexException
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
name|OutputStreamIndexOutput
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
name|SimpleFSDirectory
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
name|ElasticsearchIllegalStateException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ExceptionsHelper
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
name|Preconditions
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
name|Streams
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
name|lucene
operator|.
name|store
operator|.
name|InputStreamIndexInput
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
name|xcontent
operator|.
name|XContentBuilder
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
name|xcontent
operator|.
name|XContentFactory
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
name|xcontent
operator|.
name|XContentHelper
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
name|xcontent
operator|.
name|XContentParser
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
name|xcontent
operator|.
name|XContentType
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
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|OutputStream
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
name|Paths
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
name|StandardCopyOption
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Matcher
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
import|;
end_import

begin_comment
comment|/**  * MetaDataStateFormat is a base class to write checksummed  * XContent based files to one or more directories in a standardized directory structure.  * @param<T> the type of the XContent base data-structure  */
end_comment

begin_class
DECL|class|MetaDataStateFormat
specifier|public
specifier|abstract
class|class
name|MetaDataStateFormat
parameter_list|<
name|T
parameter_list|>
block|{
DECL|field|STATE_DIR_NAME
specifier|public
specifier|static
specifier|final
name|String
name|STATE_DIR_NAME
init|=
literal|"_state"
decl_stmt|;
DECL|field|STATE_FILE_EXTENSION
specifier|public
specifier|static
specifier|final
name|String
name|STATE_FILE_EXTENSION
init|=
literal|".st"
decl_stmt|;
DECL|field|STATE_FILE_CODEC
specifier|private
specifier|static
specifier|final
name|String
name|STATE_FILE_CODEC
init|=
literal|"state"
decl_stmt|;
DECL|field|STATE_FILE_VERSION
specifier|private
specifier|static
specifier|final
name|int
name|STATE_FILE_VERSION
init|=
literal|0
decl_stmt|;
DECL|field|BUFFER_SIZE
specifier|private
specifier|static
specifier|final
name|int
name|BUFFER_SIZE
init|=
literal|4096
decl_stmt|;
DECL|field|format
specifier|private
specifier|final
name|XContentType
name|format
decl_stmt|;
DECL|field|deleteOldFiles
specifier|private
specifier|final
name|boolean
name|deleteOldFiles
decl_stmt|;
comment|/**      * Creates a new {@link MetaDataStateFormat} instance      * @param format the format of the x-content      * @param deleteOldFiles if<code>true</code> write operations will      *                       clean up old files written with this format.      */
DECL|method|MetaDataStateFormat
specifier|protected
name|MetaDataStateFormat
parameter_list|(
name|XContentType
name|format
parameter_list|,
name|boolean
name|deleteOldFiles
parameter_list|)
block|{
name|this
operator|.
name|format
operator|=
name|format
expr_stmt|;
name|this
operator|.
name|deleteOldFiles
operator|=
name|deleteOldFiles
expr_stmt|;
block|}
comment|/**      * Returns the {@link XContentType} used to serialize xcontent on write.      */
DECL|method|format
specifier|public
name|XContentType
name|format
parameter_list|()
block|{
return|return
name|format
return|;
block|}
comment|/**      * Writes the given state to the given directories. The state is written to a      * state directory ({@value #STATE_DIR_NAME}) underneath each of the given file locations and is created if it      * doesn't exist. The state is serialized to a temporary file in that directory and is then atomically moved to      * it's target filename of the pattern<tt>{prefix}{version}.st</tt>.      *      * @param state the state object to write      * @param prefix the state names prefix used to compose the file name.      * @param version the version of the state      * @param locations the locations where the state should be written to.      * @throws IOException if an IOException occurs      */
DECL|method|write
specifier|public
specifier|final
name|void
name|write
parameter_list|(
specifier|final
name|T
name|state
parameter_list|,
specifier|final
name|String
name|prefix
parameter_list|,
specifier|final
name|long
name|version
parameter_list|,
specifier|final
name|File
modifier|...
name|locations
parameter_list|)
throws|throws
name|IOException
block|{
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|locations
operator|!=
literal|null
argument_list|,
literal|"Locations must not be null"
argument_list|)
expr_stmt|;
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|locations
operator|.
name|length
operator|>
literal|0
argument_list|,
literal|"One or more locations required"
argument_list|)
expr_stmt|;
name|String
name|fileName
init|=
name|prefix
operator|+
name|version
operator|+
name|STATE_FILE_EXTENSION
decl_stmt|;
name|Path
name|stateLocation
init|=
name|Paths
operator|.
name|get
argument_list|(
name|locations
index|[
literal|0
index|]
operator|.
name|getPath
argument_list|()
argument_list|,
name|STATE_DIR_NAME
argument_list|)
decl_stmt|;
name|Files
operator|.
name|createDirectories
argument_list|(
name|stateLocation
argument_list|)
expr_stmt|;
specifier|final
name|Path
name|tmpStatePath
init|=
name|stateLocation
operator|.
name|resolve
argument_list|(
name|fileName
operator|+
literal|".tmp"
argument_list|)
decl_stmt|;
specifier|final
name|Path
name|finalStatePath
init|=
name|stateLocation
operator|.
name|resolve
argument_list|(
name|fileName
argument_list|)
decl_stmt|;
try|try
block|{
try|try
init|(
name|OutputStreamIndexOutput
name|out
init|=
operator|new
name|OutputStreamIndexOutput
argument_list|(
name|Files
operator|.
name|newOutputStream
argument_list|(
name|tmpStatePath
argument_list|)
argument_list|,
name|BUFFER_SIZE
argument_list|)
init|)
block|{
name|CodecUtil
operator|.
name|writeHeader
argument_list|(
name|out
argument_list|,
name|STATE_FILE_CODEC
argument_list|,
name|STATE_FILE_VERSION
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|format
operator|.
name|index
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeLong
argument_list|(
name|version
argument_list|)
expr_stmt|;
try|try
init|(
name|XContentBuilder
name|builder
init|=
name|newXContentBuilder
argument_list|(
name|format
argument_list|,
operator|new
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|lucene
operator|.
name|store
operator|.
name|OutputStreamIndexOutput
argument_list|(
name|out
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
comment|// this is important since some of the XContentBuilders write bytes on close.
comment|// in order to write the footer we need to prevent closing the actual index input.
block|}
block|}
argument_list|)
init|)
block|{
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
block|{
name|toXContent
argument_list|(
name|builder
argument_list|,
name|state
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
name|CodecUtil
operator|.
name|writeFooter
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
name|IOUtils
operator|.
name|fsync
argument_list|(
name|tmpStatePath
operator|.
name|toFile
argument_list|()
argument_list|,
literal|false
argument_list|)
expr_stmt|;
comment|// fsync the state file
name|Files
operator|.
name|move
argument_list|(
name|tmpStatePath
argument_list|,
name|finalStatePath
argument_list|,
name|StandardCopyOption
operator|.
name|ATOMIC_MOVE
argument_list|)
expr_stmt|;
name|IOUtils
operator|.
name|fsync
argument_list|(
name|stateLocation
operator|.
name|toFile
argument_list|()
argument_list|,
literal|true
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|locations
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|stateLocation
operator|=
name|Paths
operator|.
name|get
argument_list|(
name|locations
index|[
name|i
index|]
operator|.
name|getPath
argument_list|()
argument_list|,
name|STATE_DIR_NAME
argument_list|)
expr_stmt|;
name|Files
operator|.
name|createDirectories
argument_list|(
name|stateLocation
argument_list|)
expr_stmt|;
name|Path
name|tmpPath
init|=
name|stateLocation
operator|.
name|resolve
argument_list|(
name|fileName
operator|+
literal|".tmp"
argument_list|)
decl_stmt|;
name|Path
name|finalPath
init|=
name|stateLocation
operator|.
name|resolve
argument_list|(
name|fileName
argument_list|)
decl_stmt|;
try|try
block|{
name|Files
operator|.
name|copy
argument_list|(
name|finalStatePath
argument_list|,
name|tmpPath
argument_list|)
expr_stmt|;
name|Files
operator|.
name|move
argument_list|(
name|tmpPath
argument_list|,
name|finalPath
argument_list|,
name|StandardCopyOption
operator|.
name|ATOMIC_MOVE
argument_list|)
expr_stmt|;
comment|// we are on the same FileSystem / Partition here we can do an atomic move
name|IOUtils
operator|.
name|fsync
argument_list|(
name|stateLocation
operator|.
name|toFile
argument_list|()
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|// we just fsync the dir here..
block|}
finally|finally
block|{
name|Files
operator|.
name|deleteIfExists
argument_list|(
name|tmpPath
argument_list|)
expr_stmt|;
block|}
block|}
block|}
finally|finally
block|{
name|Files
operator|.
name|deleteIfExists
argument_list|(
name|tmpStatePath
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|deleteOldFiles
condition|)
block|{
name|cleanupOldFiles
argument_list|(
name|prefix
argument_list|,
name|fileName
argument_list|,
name|locations
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|newXContentBuilder
specifier|protected
name|XContentBuilder
name|newXContentBuilder
parameter_list|(
name|XContentType
name|type
parameter_list|,
name|OutputStream
name|stream
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|XContentFactory
operator|.
name|contentBuilder
argument_list|(
name|type
argument_list|,
name|stream
argument_list|)
return|;
block|}
comment|/**      * Writes the given state to the given XContentBuilder      * Subclasses need to implement this class for theirs specific state.      */
DECL|method|toXContent
specifier|public
specifier|abstract
name|void
name|toXContent
parameter_list|(
name|XContentBuilder
name|builder
parameter_list|,
name|T
name|state
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**      * Reads a new instance of the state from the given XContentParser      * Subclasses need to implement this class for theirs specific state.      */
DECL|method|fromXContent
specifier|public
specifier|abstract
name|T
name|fromXContent
parameter_list|(
name|XContentParser
name|parser
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**      * Reads the state from a given file and compares the expected version against the actual version of      * the state.      */
DECL|method|read
specifier|public
specifier|final
name|T
name|read
parameter_list|(
name|File
name|file
parameter_list|,
name|long
name|expectedVersion
parameter_list|)
throws|throws
name|IOException
block|{
try|try
init|(
name|Directory
name|dir
init|=
name|newDirectory
argument_list|(
name|file
operator|.
name|getParentFile
argument_list|()
argument_list|)
init|)
block|{
try|try
init|(
specifier|final
name|IndexInput
name|indexInput
init|=
name|dir
operator|.
name|openInput
argument_list|(
name|file
operator|.
name|getName
argument_list|()
argument_list|,
name|IOContext
operator|.
name|DEFAULT
argument_list|)
init|)
block|{
comment|// We checksum the entire file before we even go and parse it. If it's corrupted we barf right here.
name|CodecUtil
operator|.
name|checksumEntireFile
argument_list|(
name|indexInput
argument_list|)
expr_stmt|;
name|CodecUtil
operator|.
name|checkHeader
argument_list|(
name|indexInput
argument_list|,
name|STATE_FILE_CODEC
argument_list|,
name|STATE_FILE_VERSION
argument_list|,
name|STATE_FILE_VERSION
argument_list|)
expr_stmt|;
specifier|final
name|XContentType
name|xContentType
init|=
name|XContentType
operator|.
name|values
argument_list|()
index|[
name|indexInput
operator|.
name|readInt
argument_list|()
index|]
decl_stmt|;
specifier|final
name|long
name|version
init|=
name|indexInput
operator|.
name|readLong
argument_list|()
decl_stmt|;
if|if
condition|(
name|version
operator|!=
name|expectedVersion
condition|)
block|{
throw|throw
operator|new
name|CorruptStateException
argument_list|(
literal|"State version mismatch expected: "
operator|+
name|expectedVersion
operator|+
literal|" but was: "
operator|+
name|version
argument_list|)
throw|;
block|}
name|long
name|filePointer
init|=
name|indexInput
operator|.
name|getFilePointer
argument_list|()
decl_stmt|;
name|long
name|contentSize
init|=
name|indexInput
operator|.
name|length
argument_list|()
operator|-
name|CodecUtil
operator|.
name|footerLength
argument_list|()
operator|-
name|filePointer
decl_stmt|;
try|try
init|(
name|IndexInput
name|slice
init|=
name|indexInput
operator|.
name|slice
argument_list|(
literal|"state_xcontent"
argument_list|,
name|filePointer
argument_list|,
name|contentSize
argument_list|)
init|)
block|{
try|try
init|(
name|XContentParser
name|parser
init|=
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|xContentType
argument_list|)
operator|.
name|createParser
argument_list|(
operator|new
name|InputStreamIndexInput
argument_list|(
name|slice
argument_list|,
name|contentSize
argument_list|)
argument_list|)
init|)
block|{
return|return
name|fromXContent
argument_list|(
name|parser
argument_list|)
return|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|CorruptIndexException
name|ex
parameter_list|)
block|{
comment|// we trick this into a dedicated exception with the original stacktrace
throw|throw
operator|new
name|CorruptStateException
argument_list|(
name|ex
argument_list|)
throw|;
block|}
block|}
block|}
DECL|method|newDirectory
specifier|protected
name|Directory
name|newDirectory
parameter_list|(
name|File
name|dir
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|SimpleFSDirectory
argument_list|(
name|dir
argument_list|)
return|;
block|}
DECL|method|cleanupOldFiles
specifier|private
name|void
name|cleanupOldFiles
parameter_list|(
name|String
name|prefix
parameter_list|,
name|String
name|fileName
parameter_list|,
name|File
index|[]
name|locations
parameter_list|)
throws|throws
name|IOException
block|{
comment|// now clean up the old files
for|for
control|(
name|File
name|dataLocation
range|:
name|locations
control|)
block|{
specifier|final
name|File
index|[]
name|files
init|=
operator|new
name|File
argument_list|(
name|dataLocation
argument_list|,
name|STATE_DIR_NAME
argument_list|)
operator|.
name|listFiles
argument_list|()
decl_stmt|;
if|if
condition|(
name|files
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|File
name|file
range|:
name|files
control|)
block|{
if|if
condition|(
operator|!
name|file
operator|.
name|getName
argument_list|()
operator|.
name|startsWith
argument_list|(
name|prefix
argument_list|)
condition|)
block|{
continue|continue;
block|}
if|if
condition|(
name|file
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
name|fileName
argument_list|)
condition|)
block|{
continue|continue;
block|}
name|Files
operator|.
name|delete
argument_list|(
name|file
operator|.
name|toPath
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
comment|/**      * Tries to load the latest state from the given data-locations. It tries to load the latest state determined by      * the states version from one or more data directories and if none of the latest states can be loaded an exception      * is thrown to prevent accidentally loading a previous state and silently omitting the latest state.      *      * @param logger an elasticsearch logger instance      * @param format the actual metastate format to use      * @param pattern the file name pattern to identify files belonging to this pattern and to read the version from.      *                The first capture group should return the version of the file. If the second capture group is has a      *                null value the files is considered a legacy file and will be treated as if the file contains a plain      *                x-content payload.      * @param stateType the state type we are loading. used for logging contenxt only.      * @param dataLocations the data-locations to try.      * @return the latest state or<code>null</code> if no state was found.      */
DECL|method|loadLatestState
specifier|public
specifier|static
parameter_list|<
name|T
parameter_list|>
name|T
name|loadLatestState
parameter_list|(
name|ESLogger
name|logger
parameter_list|,
name|MetaDataStateFormat
argument_list|<
name|T
argument_list|>
name|format
parameter_list|,
name|Pattern
name|pattern
parameter_list|,
name|String
name|stateType
parameter_list|,
name|File
modifier|...
name|dataLocations
parameter_list|)
block|{
name|List
argument_list|<
name|FileAndVersion
argument_list|>
name|files
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|long
name|maxVersion
init|=
operator|-
literal|1
decl_stmt|;
name|boolean
name|maxVersionIsLegacy
init|=
literal|true
decl_stmt|;
if|if
condition|(
name|dataLocations
operator|!=
literal|null
condition|)
block|{
comment|// select all eligable files first
for|for
control|(
name|File
name|dataLocation
range|:
name|dataLocations
control|)
block|{
name|File
name|stateDir
init|=
operator|new
name|File
argument_list|(
name|dataLocation
argument_list|,
name|MetaDataStateFormat
operator|.
name|STATE_DIR_NAME
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|stateDir
operator|.
name|exists
argument_list|()
operator|||
operator|!
name|stateDir
operator|.
name|isDirectory
argument_list|()
condition|)
block|{
continue|continue;
block|}
comment|// now, iterate over the current versions, and find latest one
name|File
index|[]
name|stateFiles
init|=
name|stateDir
operator|.
name|listFiles
argument_list|()
decl_stmt|;
if|if
condition|(
name|stateFiles
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
for|for
control|(
name|File
name|stateFile
range|:
name|stateFiles
control|)
block|{
specifier|final
name|Matcher
name|matcher
init|=
name|pattern
operator|.
name|matcher
argument_list|(
name|stateFile
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|matcher
operator|.
name|matches
argument_list|()
condition|)
block|{
specifier|final
name|long
name|version
init|=
name|Long
operator|.
name|parseLong
argument_list|(
name|matcher
operator|.
name|group
argument_list|(
literal|1
argument_list|)
argument_list|)
decl_stmt|;
name|maxVersion
operator|=
name|Math
operator|.
name|max
argument_list|(
name|maxVersion
argument_list|,
name|version
argument_list|)
expr_stmt|;
specifier|final
name|boolean
name|legacy
init|=
name|MetaDataStateFormat
operator|.
name|STATE_FILE_EXTENSION
operator|.
name|equals
argument_list|(
name|matcher
operator|.
name|group
argument_list|(
literal|2
argument_list|)
argument_list|)
operator|==
literal|false
decl_stmt|;
name|maxVersionIsLegacy
operator|&=
name|legacy
expr_stmt|;
comment|// on purpose, see NOTE below
name|files
operator|.
name|add
argument_list|(
operator|new
name|FileAndVersion
argument_list|(
name|stateFile
argument_list|,
name|version
argument_list|,
name|legacy
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
specifier|final
name|List
argument_list|<
name|Throwable
argument_list|>
name|exceptions
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|T
name|state
init|=
literal|null
decl_stmt|;
comment|// NOTE: we might have multiple version of the latest state if there are multiple data dirs.. for this case
comment|//       we iterate only over the ones with the max version. If we have at least one state file that uses the
comment|//       new format (ie. legacy == false) then we know that the latest version state ought to use this new format.
comment|//       In case the state file with the latest version does not use the new format while older state files do,
comment|//       the list below will be empty and loading the state will fail
for|for
control|(
name|FileAndVersion
name|fileAndVersion
range|:
name|Collections2
operator|.
name|filter
argument_list|(
name|files
argument_list|,
operator|new
name|VersionAndLegacyPredicate
argument_list|(
name|maxVersion
argument_list|,
name|maxVersionIsLegacy
argument_list|)
argument_list|)
control|)
block|{
try|try
block|{
specifier|final
name|File
name|stateFile
init|=
name|fileAndVersion
operator|.
name|file
decl_stmt|;
specifier|final
name|long
name|version
init|=
name|fileAndVersion
operator|.
name|version
decl_stmt|;
specifier|final
name|XContentParser
name|parser
decl_stmt|;
if|if
condition|(
name|fileAndVersion
operator|.
name|legacy
condition|)
block|{
comment|// read the legacy format -- plain XContent
try|try
init|(
name|FileInputStream
name|stream
init|=
operator|new
name|FileInputStream
argument_list|(
name|stateFile
argument_list|)
init|)
block|{
specifier|final
name|byte
index|[]
name|data
init|=
name|Streams
operator|.
name|copyToByteArray
argument_list|(
name|stream
argument_list|)
decl_stmt|;
if|if
condition|(
name|data
operator|.
name|length
operator|==
literal|0
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"{}: no data for [{}], ignoring..."
argument_list|,
name|stateType
argument_list|,
name|stateFile
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
expr_stmt|;
continue|continue;
block|}
name|parser
operator|=
name|XContentHelper
operator|.
name|createParser
argument_list|(
name|data
argument_list|,
literal|0
argument_list|,
name|data
operator|.
name|length
argument_list|)
expr_stmt|;
name|state
operator|=
name|format
operator|.
name|fromXContent
argument_list|(
name|parser
argument_list|)
expr_stmt|;
if|if
condition|(
name|state
operator|==
literal|null
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"{}: no data for [{}], ignoring..."
argument_list|,
name|stateType
argument_list|,
name|stateFile
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
name|state
operator|=
name|format
operator|.
name|read
argument_list|(
name|stateFile
argument_list|,
name|version
argument_list|)
expr_stmt|;
block|}
return|return
name|state
return|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|exceptions
operator|.
name|add
argument_list|(
name|e
argument_list|)
expr_stmt|;
name|logger
operator|.
name|debug
argument_list|(
literal|"{}: failed to read [{}], ignoring..."
argument_list|,
name|e
argument_list|,
name|fileAndVersion
operator|.
name|file
operator|.
name|getAbsolutePath
argument_list|()
argument_list|,
name|stateType
argument_list|)
expr_stmt|;
block|}
block|}
comment|// if we reach this something went wrong
name|ExceptionsHelper
operator|.
name|maybeThrowRuntimeAndSuppress
argument_list|(
name|exceptions
argument_list|)
expr_stmt|;
if|if
condition|(
name|files
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
comment|// We have some state files but none of them gave us a usable state
throw|throw
operator|new
name|ElasticsearchIllegalStateException
argument_list|(
literal|"Could not find a state file to recover from among "
operator|+
name|files
argument_list|)
throw|;
block|}
return|return
name|state
return|;
block|}
comment|/**      * Filters out all {@link FileAndVersion} instances with a different version than      * the given one.      */
DECL|class|VersionAndLegacyPredicate
specifier|private
specifier|static
specifier|final
class|class
name|VersionAndLegacyPredicate
implements|implements
name|Predicate
argument_list|<
name|FileAndVersion
argument_list|>
block|{
DECL|field|version
specifier|private
specifier|final
name|long
name|version
decl_stmt|;
DECL|field|legacy
specifier|private
specifier|final
name|boolean
name|legacy
decl_stmt|;
DECL|method|VersionAndLegacyPredicate
name|VersionAndLegacyPredicate
parameter_list|(
name|long
name|version
parameter_list|,
name|boolean
name|legacy
parameter_list|)
block|{
name|this
operator|.
name|version
operator|=
name|version
expr_stmt|;
name|this
operator|.
name|legacy
operator|=
name|legacy
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|apply
specifier|public
name|boolean
name|apply
parameter_list|(
name|FileAndVersion
name|input
parameter_list|)
block|{
return|return
name|input
operator|.
name|version
operator|==
name|version
operator|&&
name|input
operator|.
name|legacy
operator|==
name|legacy
return|;
block|}
block|}
comment|/**      * Internal struct-like class that holds the parsed state version, the file      * and a flag if the file is a legacy state ie. pre 1.5      */
DECL|class|FileAndVersion
specifier|private
specifier|static
class|class
name|FileAndVersion
block|{
DECL|field|file
specifier|final
name|File
name|file
decl_stmt|;
DECL|field|version
specifier|final
name|long
name|version
decl_stmt|;
DECL|field|legacy
specifier|final
name|boolean
name|legacy
decl_stmt|;
DECL|method|FileAndVersion
specifier|private
name|FileAndVersion
parameter_list|(
name|File
name|file
parameter_list|,
name|long
name|version
parameter_list|,
name|boolean
name|legacy
parameter_list|)
block|{
name|this
operator|.
name|file
operator|=
name|file
expr_stmt|;
name|this
operator|.
name|version
operator|=
name|version
expr_stmt|;
name|this
operator|.
name|legacy
operator|=
name|legacy
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

