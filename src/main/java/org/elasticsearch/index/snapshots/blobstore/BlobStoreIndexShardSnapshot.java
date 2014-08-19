begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.snapshots.blobstore
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|snapshots
operator|.
name|blobstore
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
name|ImmutableList
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
name|BytesRef
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
name|Version
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchParseException
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
name|Nullable
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
name|ParseField
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
name|Lucene
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
name|ByteSizeValue
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
name|ToXContent
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
name|XContentBuilderString
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
name|index
operator|.
name|store
operator|.
name|StoreFileMetaData
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
name|List
import|;
end_import

begin_import
import|import static
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
operator|.
name|newArrayList
import|;
end_import

begin_comment
comment|/**  * Shard snapshot metadata  */
end_comment

begin_class
DECL|class|BlobStoreIndexShardSnapshot
specifier|public
class|class
name|BlobStoreIndexShardSnapshot
block|{
comment|/**      * Information about snapshotted file      */
DECL|class|FileInfo
specifier|public
specifier|static
class|class
name|FileInfo
block|{
DECL|field|name
specifier|private
specifier|final
name|String
name|name
decl_stmt|;
DECL|field|partSize
specifier|private
specifier|final
name|ByteSizeValue
name|partSize
decl_stmt|;
DECL|field|partBytes
specifier|private
specifier|final
name|long
name|partBytes
decl_stmt|;
DECL|field|numberOfParts
specifier|private
specifier|final
name|long
name|numberOfParts
decl_stmt|;
DECL|field|metadata
specifier|private
specifier|final
name|StoreFileMetaData
name|metadata
decl_stmt|;
comment|/**          * Constructs a new instance of file info          *          * @param name         file name as stored in the blob store          * @param metaData  the files meta data          * @param partSize     size of the single chunk          */
DECL|method|FileInfo
specifier|public
name|FileInfo
parameter_list|(
name|String
name|name
parameter_list|,
name|StoreFileMetaData
name|metaData
parameter_list|,
name|ByteSizeValue
name|partSize
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
name|this
operator|.
name|metadata
operator|=
name|metaData
expr_stmt|;
name|long
name|partBytes
init|=
name|Long
operator|.
name|MAX_VALUE
decl_stmt|;
if|if
condition|(
name|partSize
operator|!=
literal|null
condition|)
block|{
name|partBytes
operator|=
name|partSize
operator|.
name|bytes
argument_list|()
expr_stmt|;
block|}
name|long
name|totalLength
init|=
name|metaData
operator|.
name|length
argument_list|()
decl_stmt|;
name|long
name|numberOfParts
init|=
name|totalLength
operator|/
name|partBytes
decl_stmt|;
if|if
condition|(
name|totalLength
operator|%
name|partBytes
operator|>
literal|0
condition|)
block|{
name|numberOfParts
operator|++
expr_stmt|;
block|}
if|if
condition|(
name|numberOfParts
operator|==
literal|0
condition|)
block|{
name|numberOfParts
operator|++
expr_stmt|;
block|}
name|this
operator|.
name|numberOfParts
operator|=
name|numberOfParts
expr_stmt|;
name|this
operator|.
name|partSize
operator|=
name|partSize
expr_stmt|;
name|this
operator|.
name|partBytes
operator|=
name|partBytes
expr_stmt|;
block|}
comment|/**          * Returns the base file name          *          * @return file name          */
DECL|method|name
specifier|public
name|String
name|name
parameter_list|()
block|{
return|return
name|name
return|;
block|}
comment|/**          * Returns part name if file is stored as multiple parts          *          * @param part part number          * @return part name          */
DECL|method|partName
specifier|public
name|String
name|partName
parameter_list|(
name|long
name|part
parameter_list|)
block|{
if|if
condition|(
name|numberOfParts
operator|>
literal|1
condition|)
block|{
return|return
name|name
operator|+
literal|".part"
operator|+
name|part
return|;
block|}
else|else
block|{
return|return
name|name
return|;
block|}
block|}
comment|/**          * Returns base file name from part name          *          * @param blobName part name          * @return base file name          */
DECL|method|canonicalName
specifier|public
specifier|static
name|String
name|canonicalName
parameter_list|(
name|String
name|blobName
parameter_list|)
block|{
if|if
condition|(
name|blobName
operator|.
name|contains
argument_list|(
literal|".part"
argument_list|)
condition|)
block|{
return|return
name|blobName
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|blobName
operator|.
name|indexOf
argument_list|(
literal|".part"
argument_list|)
argument_list|)
return|;
block|}
return|return
name|blobName
return|;
block|}
comment|/**          * Returns original file name          *          * @return original file name          */
DECL|method|physicalName
specifier|public
name|String
name|physicalName
parameter_list|()
block|{
return|return
name|metadata
operator|.
name|name
argument_list|()
return|;
block|}
comment|/**          * File length          *          * @return file length          */
DECL|method|length
specifier|public
name|long
name|length
parameter_list|()
block|{
return|return
name|metadata
operator|.
name|length
argument_list|()
return|;
block|}
comment|/**          * Returns part size          *          * @return part size          */
DECL|method|partSize
specifier|public
name|ByteSizeValue
name|partSize
parameter_list|()
block|{
return|return
name|partSize
return|;
block|}
comment|/**          * Return maximum number of bytes in a part          *          * @return maximum number of bytes in a part          */
DECL|method|partBytes
specifier|public
name|long
name|partBytes
parameter_list|()
block|{
return|return
name|partBytes
return|;
block|}
comment|/**          * Returns number of parts          *          * @return number of parts          */
DECL|method|numberOfParts
specifier|public
name|long
name|numberOfParts
parameter_list|()
block|{
return|return
name|numberOfParts
return|;
block|}
comment|/**          * Returns file md5 checksum provided by {@link org.elasticsearch.index.store.Store}          *          * @return file checksum          */
annotation|@
name|Nullable
DECL|method|checksum
specifier|public
name|String
name|checksum
parameter_list|()
block|{
return|return
name|metadata
operator|.
name|checksum
argument_list|()
return|;
block|}
comment|/**          * Returns the StoreFileMetaData for this file info.          */
DECL|method|metadata
specifier|public
name|StoreFileMetaData
name|metadata
parameter_list|()
block|{
return|return
name|metadata
return|;
block|}
comment|/**          * Checks if a file in a store is the same file          *          * @param md file in a store          * @return true if file in a store this this file have the same checksum and length          */
DECL|method|isSame
specifier|public
name|boolean
name|isSame
parameter_list|(
name|StoreFileMetaData
name|md
parameter_list|)
block|{
return|return
name|metadata
operator|.
name|isSame
argument_list|(
name|md
argument_list|)
return|;
block|}
DECL|class|Fields
specifier|static
specifier|final
class|class
name|Fields
block|{
DECL|field|NAME
specifier|static
specifier|final
name|XContentBuilderString
name|NAME
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"name"
argument_list|)
decl_stmt|;
DECL|field|PHYSICAL_NAME
specifier|static
specifier|final
name|XContentBuilderString
name|PHYSICAL_NAME
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"physical_name"
argument_list|)
decl_stmt|;
DECL|field|LENGTH
specifier|static
specifier|final
name|XContentBuilderString
name|LENGTH
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"length"
argument_list|)
decl_stmt|;
DECL|field|CHECKSUM
specifier|static
specifier|final
name|XContentBuilderString
name|CHECKSUM
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"checksum"
argument_list|)
decl_stmt|;
DECL|field|PART_SIZE
specifier|static
specifier|final
name|XContentBuilderString
name|PART_SIZE
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"part_size"
argument_list|)
decl_stmt|;
DECL|field|WRITTEN_BY
specifier|static
specifier|final
name|XContentBuilderString
name|WRITTEN_BY
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"written_by"
argument_list|)
decl_stmt|;
DECL|field|META_HASH
specifier|static
specifier|final
name|XContentBuilderString
name|META_HASH
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"meta_hash"
argument_list|)
decl_stmt|;
block|}
comment|/**          * Serializes file info into JSON          *          * @param file    file info          * @param builder XContent builder          * @param params  parameters          * @throws IOException          */
DECL|method|toXContent
specifier|public
specifier|static
name|void
name|toXContent
parameter_list|(
name|FileInfo
name|file
parameter_list|,
name|XContentBuilder
name|builder
parameter_list|,
name|ToXContent
operator|.
name|Params
name|params
parameter_list|)
throws|throws
name|IOException
block|{
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|NAME
argument_list|,
name|file
operator|.
name|name
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|PHYSICAL_NAME
argument_list|,
name|file
operator|.
name|metadata
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|LENGTH
argument_list|,
name|file
operator|.
name|metadata
operator|.
name|length
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|file
operator|.
name|metadata
operator|.
name|checksum
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|CHECKSUM
argument_list|,
name|file
operator|.
name|metadata
operator|.
name|checksum
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|file
operator|.
name|partSize
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|PART_SIZE
argument_list|,
name|file
operator|.
name|partSize
operator|.
name|bytes
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|file
operator|.
name|metadata
operator|.
name|writtenBy
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|WRITTEN_BY
argument_list|,
name|file
operator|.
name|metadata
operator|.
name|writtenBy
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|file
operator|.
name|metadata
operator|.
name|hash
argument_list|()
operator|!=
literal|null
operator|&&
name|file
operator|.
name|metadata
argument_list|()
operator|.
name|hash
argument_list|()
operator|.
name|length
operator|>
literal|0
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|META_HASH
argument_list|,
name|file
operator|.
name|metadata
operator|.
name|hash
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
comment|/**          * Parses JSON that represents file info          *          * @param parser parser          * @return file info          * @throws IOException          */
DECL|method|fromXContent
specifier|public
specifier|static
name|FileInfo
name|fromXContent
parameter_list|(
name|XContentParser
name|parser
parameter_list|)
throws|throws
name|IOException
block|{
name|XContentParser
operator|.
name|Token
name|token
init|=
name|parser
operator|.
name|currentToken
argument_list|()
decl_stmt|;
name|String
name|name
init|=
literal|null
decl_stmt|;
name|String
name|physicalName
init|=
literal|null
decl_stmt|;
name|long
name|length
init|=
operator|-
literal|1
decl_stmt|;
name|String
name|checksum
init|=
literal|null
decl_stmt|;
name|ByteSizeValue
name|partSize
init|=
literal|null
decl_stmt|;
name|Version
name|writtenBy
init|=
literal|null
decl_stmt|;
name|BytesRef
name|metaHash
init|=
operator|new
name|BytesRef
argument_list|()
decl_stmt|;
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|START_OBJECT
condition|)
block|{
while|while
condition|(
operator|(
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
operator|)
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|END_OBJECT
condition|)
block|{
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|FIELD_NAME
condition|)
block|{
name|String
name|currentFieldName
init|=
name|parser
operator|.
name|currentName
argument_list|()
decl_stmt|;
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
if|if
condition|(
name|token
operator|.
name|isValue
argument_list|()
condition|)
block|{
if|if
condition|(
literal|"name"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|name
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"physical_name"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|physicalName
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"length"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|length
operator|=
name|parser
operator|.
name|longValue
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"checksum"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|checksum
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"part_size"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|partSize
operator|=
operator|new
name|ByteSizeValue
argument_list|(
name|parser
operator|.
name|longValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"written_by"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|writtenBy
operator|=
name|Lucene
operator|.
name|parseVersionLenient
argument_list|(
name|parser
operator|.
name|text
argument_list|()
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"meta_hash"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|metaHash
operator|.
name|bytes
operator|=
name|parser
operator|.
name|binaryValue
argument_list|()
expr_stmt|;
name|metaHash
operator|.
name|offset
operator|=
literal|0
expr_stmt|;
name|metaHash
operator|.
name|length
operator|=
name|metaHash
operator|.
name|bytes
operator|.
name|length
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|ElasticsearchParseException
argument_list|(
literal|"unknown parameter ["
operator|+
name|currentFieldName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
else|else
block|{
throw|throw
operator|new
name|ElasticsearchParseException
argument_list|(
literal|"unexpected token  ["
operator|+
name|token
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
else|else
block|{
throw|throw
operator|new
name|ElasticsearchParseException
argument_list|(
literal|"unexpected token  ["
operator|+
name|token
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
block|}
comment|// TODO: Verify???
return|return
operator|new
name|FileInfo
argument_list|(
name|name
argument_list|,
operator|new
name|StoreFileMetaData
argument_list|(
name|physicalName
argument_list|,
name|length
argument_list|,
name|checksum
argument_list|,
name|writtenBy
argument_list|,
name|metaHash
argument_list|)
argument_list|,
name|partSize
argument_list|)
return|;
block|}
block|}
DECL|field|snapshot
specifier|private
specifier|final
name|String
name|snapshot
decl_stmt|;
DECL|field|indexVersion
specifier|private
specifier|final
name|long
name|indexVersion
decl_stmt|;
DECL|field|startTime
specifier|private
specifier|final
name|long
name|startTime
decl_stmt|;
DECL|field|time
specifier|private
specifier|final
name|long
name|time
decl_stmt|;
DECL|field|numberOfFiles
specifier|private
specifier|final
name|int
name|numberOfFiles
decl_stmt|;
DECL|field|totalSize
specifier|private
specifier|final
name|long
name|totalSize
decl_stmt|;
DECL|field|indexFiles
specifier|private
specifier|final
name|ImmutableList
argument_list|<
name|FileInfo
argument_list|>
name|indexFiles
decl_stmt|;
comment|/**      * Constructs new shard snapshot metadata from snapshot metadata      *      * @param snapshot      snapshot id      * @param indexVersion  index version      * @param indexFiles    list of files in the shard      * @param startTime     snapshot start time      * @param time          snapshot running time      * @param numberOfFiles number of files that where snapshotted      * @param totalSize     total size of all files snapshotted      */
DECL|method|BlobStoreIndexShardSnapshot
specifier|public
name|BlobStoreIndexShardSnapshot
parameter_list|(
name|String
name|snapshot
parameter_list|,
name|long
name|indexVersion
parameter_list|,
name|List
argument_list|<
name|FileInfo
argument_list|>
name|indexFiles
parameter_list|,
name|long
name|startTime
parameter_list|,
name|long
name|time
parameter_list|,
name|int
name|numberOfFiles
parameter_list|,
name|long
name|totalSize
parameter_list|)
block|{
assert|assert
name|snapshot
operator|!=
literal|null
assert|;
assert|assert
name|indexVersion
operator|>=
literal|0
assert|;
name|this
operator|.
name|snapshot
operator|=
name|snapshot
expr_stmt|;
name|this
operator|.
name|indexVersion
operator|=
name|indexVersion
expr_stmt|;
name|this
operator|.
name|indexFiles
operator|=
name|ImmutableList
operator|.
name|copyOf
argument_list|(
name|indexFiles
argument_list|)
expr_stmt|;
name|this
operator|.
name|startTime
operator|=
name|startTime
expr_stmt|;
name|this
operator|.
name|time
operator|=
name|time
expr_stmt|;
name|this
operator|.
name|numberOfFiles
operator|=
name|numberOfFiles
expr_stmt|;
name|this
operator|.
name|totalSize
operator|=
name|totalSize
expr_stmt|;
block|}
comment|/**      * Returns index version      *      * @return index version      */
DECL|method|indexVersion
specifier|public
name|long
name|indexVersion
parameter_list|()
block|{
return|return
name|indexVersion
return|;
block|}
comment|/**      * Returns snapshot id      *      * @return snapshot id      */
DECL|method|snapshot
specifier|public
name|String
name|snapshot
parameter_list|()
block|{
return|return
name|snapshot
return|;
block|}
comment|/**      * Returns list of files in the shard      *      * @return list of files      */
DECL|method|indexFiles
specifier|public
name|ImmutableList
argument_list|<
name|FileInfo
argument_list|>
name|indexFiles
parameter_list|()
block|{
return|return
name|indexFiles
return|;
block|}
comment|/**      * Returns snapshot start time      */
DECL|method|startTime
specifier|public
name|long
name|startTime
parameter_list|()
block|{
return|return
name|startTime
return|;
block|}
comment|/**      * Returns snapshot running time      */
DECL|method|time
specifier|public
name|long
name|time
parameter_list|()
block|{
return|return
name|time
return|;
block|}
comment|/**      * Returns number of files that where snapshotted      */
DECL|method|numberOfFiles
specifier|public
name|int
name|numberOfFiles
parameter_list|()
block|{
return|return
name|numberOfFiles
return|;
block|}
comment|/**      * Returns total size of all files that where snapshotted      */
DECL|method|totalSize
specifier|public
name|long
name|totalSize
parameter_list|()
block|{
return|return
name|totalSize
return|;
block|}
DECL|class|Fields
specifier|static
specifier|final
class|class
name|Fields
block|{
DECL|field|NAME
specifier|static
specifier|final
name|XContentBuilderString
name|NAME
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"name"
argument_list|)
decl_stmt|;
DECL|field|INDEX_VERSION
specifier|static
specifier|final
name|XContentBuilderString
name|INDEX_VERSION
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"index_version"
argument_list|)
decl_stmt|;
DECL|field|START_TIME
specifier|static
specifier|final
name|XContentBuilderString
name|START_TIME
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"start_time"
argument_list|)
decl_stmt|;
DECL|field|TIME
specifier|static
specifier|final
name|XContentBuilderString
name|TIME
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"time"
argument_list|)
decl_stmt|;
DECL|field|NUMBER_OF_FILES
specifier|static
specifier|final
name|XContentBuilderString
name|NUMBER_OF_FILES
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"number_of_files"
argument_list|)
decl_stmt|;
DECL|field|TOTAL_SIZE
specifier|static
specifier|final
name|XContentBuilderString
name|TOTAL_SIZE
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"total_size"
argument_list|)
decl_stmt|;
DECL|field|FILES
specifier|static
specifier|final
name|XContentBuilderString
name|FILES
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"files"
argument_list|)
decl_stmt|;
block|}
DECL|class|ParseFields
specifier|static
specifier|final
class|class
name|ParseFields
block|{
DECL|field|NAME
specifier|static
specifier|final
name|ParseField
name|NAME
init|=
operator|new
name|ParseField
argument_list|(
literal|"name"
argument_list|)
decl_stmt|;
DECL|field|INDEX_VERSION
specifier|static
specifier|final
name|ParseField
name|INDEX_VERSION
init|=
operator|new
name|ParseField
argument_list|(
literal|"index_version"
argument_list|,
literal|"index-version"
argument_list|)
decl_stmt|;
DECL|field|START_TIME
specifier|static
specifier|final
name|ParseField
name|START_TIME
init|=
operator|new
name|ParseField
argument_list|(
literal|"start_time"
argument_list|)
decl_stmt|;
DECL|field|TIME
specifier|static
specifier|final
name|ParseField
name|TIME
init|=
operator|new
name|ParseField
argument_list|(
literal|"time"
argument_list|)
decl_stmt|;
DECL|field|NUMBER_OF_FILES
specifier|static
specifier|final
name|ParseField
name|NUMBER_OF_FILES
init|=
operator|new
name|ParseField
argument_list|(
literal|"number_of_files"
argument_list|)
decl_stmt|;
DECL|field|TOTAL_SIZE
specifier|static
specifier|final
name|ParseField
name|TOTAL_SIZE
init|=
operator|new
name|ParseField
argument_list|(
literal|"total_size"
argument_list|)
decl_stmt|;
DECL|field|FILES
specifier|static
specifier|final
name|ParseField
name|FILES
init|=
operator|new
name|ParseField
argument_list|(
literal|"files"
argument_list|)
decl_stmt|;
block|}
comment|/**      * Serializes shard snapshot metadata info into JSON      *      * @param snapshot shard snapshot metadata      * @param builder  XContent builder      * @param params   parameters      * @throws IOException      */
DECL|method|toXContent
specifier|public
specifier|static
name|void
name|toXContent
parameter_list|(
name|BlobStoreIndexShardSnapshot
name|snapshot
parameter_list|,
name|XContentBuilder
name|builder
parameter_list|,
name|ToXContent
operator|.
name|Params
name|params
parameter_list|)
throws|throws
name|IOException
block|{
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|NAME
argument_list|,
name|snapshot
operator|.
name|snapshot
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|INDEX_VERSION
argument_list|,
name|snapshot
operator|.
name|indexVersion
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|START_TIME
argument_list|,
name|snapshot
operator|.
name|startTime
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|TIME
argument_list|,
name|snapshot
operator|.
name|time
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|NUMBER_OF_FILES
argument_list|,
name|snapshot
operator|.
name|numberOfFiles
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|TOTAL_SIZE
argument_list|,
name|snapshot
operator|.
name|totalSize
argument_list|)
expr_stmt|;
name|builder
operator|.
name|startArray
argument_list|(
name|Fields
operator|.
name|FILES
argument_list|)
expr_stmt|;
for|for
control|(
name|FileInfo
name|fileInfo
range|:
name|snapshot
operator|.
name|indexFiles
control|)
block|{
name|FileInfo
operator|.
name|toXContent
argument_list|(
name|fileInfo
argument_list|,
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endArray
argument_list|()
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
comment|/**      * Parses shard snapshot metadata      *      * @param parser parser      * @return shard snapshot metadata      * @throws IOException      */
DECL|method|fromXContent
specifier|public
specifier|static
name|BlobStoreIndexShardSnapshot
name|fromXContent
parameter_list|(
name|XContentParser
name|parser
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|snapshot
init|=
literal|null
decl_stmt|;
name|long
name|indexVersion
init|=
operator|-
literal|1
decl_stmt|;
name|long
name|startTime
init|=
literal|0
decl_stmt|;
name|long
name|time
init|=
literal|0
decl_stmt|;
name|int
name|numberOfFiles
init|=
literal|0
decl_stmt|;
name|long
name|totalSize
init|=
literal|0
decl_stmt|;
name|List
argument_list|<
name|FileInfo
argument_list|>
name|indexFiles
init|=
name|newArrayList
argument_list|()
decl_stmt|;
name|XContentParser
operator|.
name|Token
name|token
init|=
name|parser
operator|.
name|currentToken
argument_list|()
decl_stmt|;
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|START_OBJECT
condition|)
block|{
while|while
condition|(
operator|(
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
operator|)
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|END_OBJECT
condition|)
block|{
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|FIELD_NAME
condition|)
block|{
name|String
name|currentFieldName
init|=
name|parser
operator|.
name|currentName
argument_list|()
decl_stmt|;
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
if|if
condition|(
name|token
operator|.
name|isValue
argument_list|()
condition|)
block|{
if|if
condition|(
name|ParseFields
operator|.
name|NAME
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|snapshot
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|ParseFields
operator|.
name|INDEX_VERSION
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
comment|// The index-version is needed for backward compatibility with v 1.0
name|indexVersion
operator|=
name|parser
operator|.
name|longValue
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|ParseFields
operator|.
name|START_TIME
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|startTime
operator|=
name|parser
operator|.
name|longValue
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|ParseFields
operator|.
name|TIME
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|time
operator|=
name|parser
operator|.
name|longValue
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|ParseFields
operator|.
name|NUMBER_OF_FILES
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|numberOfFiles
operator|=
name|parser
operator|.
name|intValue
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|ParseFields
operator|.
name|TOTAL_SIZE
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|totalSize
operator|=
name|parser
operator|.
name|longValue
argument_list|()
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|ElasticsearchParseException
argument_list|(
literal|"unknown parameter ["
operator|+
name|currentFieldName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
elseif|else
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|START_ARRAY
condition|)
block|{
if|if
condition|(
name|ParseFields
operator|.
name|FILES
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
while|while
condition|(
operator|(
name|parser
operator|.
name|nextToken
argument_list|()
operator|)
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|END_ARRAY
condition|)
block|{
name|indexFiles
operator|.
name|add
argument_list|(
name|FileInfo
operator|.
name|fromXContent
argument_list|(
name|parser
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
throw|throw
operator|new
name|ElasticsearchParseException
argument_list|(
literal|"unknown parameter ["
operator|+
name|currentFieldName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
else|else
block|{
throw|throw
operator|new
name|ElasticsearchParseException
argument_list|(
literal|"unexpected token  ["
operator|+
name|token
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
else|else
block|{
throw|throw
operator|new
name|ElasticsearchParseException
argument_list|(
literal|"unexpected token  ["
operator|+
name|token
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
block|}
return|return
operator|new
name|BlobStoreIndexShardSnapshot
argument_list|(
name|snapshot
argument_list|,
name|indexVersion
argument_list|,
name|ImmutableList
operator|.
expr|<
name|FileInfo
operator|>
name|copyOf
argument_list|(
name|indexFiles
argument_list|)
argument_list|,
name|startTime
argument_list|,
name|time
argument_list|,
name|numberOfFiles
argument_list|,
name|totalSize
argument_list|)
return|;
block|}
comment|/**      * Returns true if this snapshot contains a file with a given original name      *      * @param physicalName original file name      * @return true if the file was found, false otherwise      */
DECL|method|containPhysicalIndexFile
specifier|public
name|boolean
name|containPhysicalIndexFile
parameter_list|(
name|String
name|physicalName
parameter_list|)
block|{
return|return
name|findPhysicalIndexFile
argument_list|(
name|physicalName
argument_list|)
operator|!=
literal|null
return|;
block|}
DECL|method|findPhysicalIndexFile
specifier|public
name|FileInfo
name|findPhysicalIndexFile
parameter_list|(
name|String
name|physicalName
parameter_list|)
block|{
for|for
control|(
name|FileInfo
name|file
range|:
name|indexFiles
control|)
block|{
if|if
condition|(
name|file
operator|.
name|physicalName
argument_list|()
operator|.
name|equals
argument_list|(
name|physicalName
argument_list|)
condition|)
block|{
return|return
name|file
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
comment|/**      * Returns true if this snapshot contains a file with a given name      *      * @param name file name      * @return true if file was found, false otherwise      */
DECL|method|findNameFile
specifier|public
name|FileInfo
name|findNameFile
parameter_list|(
name|String
name|name
parameter_list|)
block|{
for|for
control|(
name|FileInfo
name|file
range|:
name|indexFiles
control|)
block|{
if|if
condition|(
name|file
operator|.
name|name
argument_list|()
operator|.
name|equals
argument_list|(
name|name
argument_list|)
condition|)
block|{
return|return
name|file
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
block|}
end_class

end_unit

