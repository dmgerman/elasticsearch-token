begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.repositories.blobstore
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|repositories
operator|.
name|blobstore
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
name|index
operator|.
name|IndexFormatTooNewException
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
name|IndexFormatTooOldException
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
name|elasticsearch
operator|.
name|common
operator|.
name|ParseFieldMatcher
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
name|bytes
operator|.
name|BytesArray
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
name|bytes
operator|.
name|BytesReference
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
name|compress
operator|.
name|CompressorFactory
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
name|io
operator|.
name|stream
operator|.
name|BytesStreamOutput
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
name|stream
operator|.
name|StreamOutput
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
name|ByteArrayIndexInput
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
name|IndexOutputOutputStream
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
name|*
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|gateway
operator|.
name|CorruptStateException
import|;
end_import

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
name|util
operator|.
name|Locale
import|;
end_import

begin_comment
comment|/**  * Snapshot metadata file format used in v2.0 and above  */
end_comment

begin_class
DECL|class|ChecksumBlobStoreFormat
specifier|public
class|class
name|ChecksumBlobStoreFormat
parameter_list|<
name|T
extends|extends
name|ToXContent
parameter_list|>
extends|extends
name|BlobStoreFormat
argument_list|<
name|T
argument_list|>
block|{
DECL|field|TEMP_FILE_PREFIX
specifier|private
specifier|static
specifier|final
name|String
name|TEMP_FILE_PREFIX
init|=
literal|"pending-"
decl_stmt|;
DECL|field|DEFAULT_X_CONTENT_TYPE
specifier|private
specifier|static
specifier|final
name|XContentType
name|DEFAULT_X_CONTENT_TYPE
init|=
name|XContentType
operator|.
name|SMILE
decl_stmt|;
comment|// The format version
DECL|field|VERSION
specifier|public
specifier|static
specifier|final
name|int
name|VERSION
init|=
literal|1
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
DECL|field|xContentType
specifier|protected
specifier|final
name|XContentType
name|xContentType
decl_stmt|;
DECL|field|compress
specifier|protected
specifier|final
name|boolean
name|compress
decl_stmt|;
DECL|field|codec
specifier|private
specifier|final
name|String
name|codec
decl_stmt|;
comment|/**      * @param codec          codec name      * @param blobNameFormat format of the blobname in {@link String#format} format      * @param reader         prototype object that can deserialize T from XContent      * @param compress       true if the content should be compressed      * @param xContentType   content type that should be used for write operations      */
DECL|method|ChecksumBlobStoreFormat
specifier|public
name|ChecksumBlobStoreFormat
parameter_list|(
name|String
name|codec
parameter_list|,
name|String
name|blobNameFormat
parameter_list|,
name|FromXContentBuilder
argument_list|<
name|T
argument_list|>
name|reader
parameter_list|,
name|ParseFieldMatcher
name|parseFieldMatcher
parameter_list|,
name|boolean
name|compress
parameter_list|,
name|XContentType
name|xContentType
parameter_list|)
block|{
name|super
argument_list|(
name|blobNameFormat
argument_list|,
name|reader
argument_list|,
name|parseFieldMatcher
argument_list|)
expr_stmt|;
name|this
operator|.
name|xContentType
operator|=
name|xContentType
expr_stmt|;
name|this
operator|.
name|compress
operator|=
name|compress
expr_stmt|;
name|this
operator|.
name|codec
operator|=
name|codec
expr_stmt|;
block|}
comment|/**      * @param codec          codec name      * @param blobNameFormat format of the blobname in {@link String#format} format      * @param reader         prototype object that can deserialize T from XContent      * @param compress       true if the content should be compressed      */
DECL|method|ChecksumBlobStoreFormat
specifier|public
name|ChecksumBlobStoreFormat
parameter_list|(
name|String
name|codec
parameter_list|,
name|String
name|blobNameFormat
parameter_list|,
name|FromXContentBuilder
argument_list|<
name|T
argument_list|>
name|reader
parameter_list|,
name|ParseFieldMatcher
name|parseFieldMatcher
parameter_list|,
name|boolean
name|compress
parameter_list|)
block|{
name|this
argument_list|(
name|codec
argument_list|,
name|blobNameFormat
argument_list|,
name|reader
argument_list|,
name|parseFieldMatcher
argument_list|,
name|compress
argument_list|,
name|DEFAULT_X_CONTENT_TYPE
argument_list|)
expr_stmt|;
block|}
comment|/**      * Reads blob with specified name without resolving the blobName using using {@link #blobName} method.      *      * @param blobContainer blob container      * @param blobName blob name      */
DECL|method|readBlob
specifier|public
name|T
name|readBlob
parameter_list|(
name|BlobContainer
name|blobContainer
parameter_list|,
name|String
name|blobName
parameter_list|)
throws|throws
name|IOException
block|{
try|try
init|(
name|InputStream
name|inputStream
init|=
name|blobContainer
operator|.
name|readBlob
argument_list|(
name|blobName
argument_list|)
init|)
block|{
name|ByteArrayOutputStream
name|out
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|Streams
operator|.
name|copy
argument_list|(
name|inputStream
argument_list|,
name|out
argument_list|)
expr_stmt|;
specifier|final
name|byte
index|[]
name|bytes
init|=
name|out
operator|.
name|toByteArray
argument_list|()
decl_stmt|;
specifier|final
name|String
name|resourceDesc
init|=
literal|"ChecksumBlobStoreFormat.readBlob(blob=\""
operator|+
name|blobName
operator|+
literal|"\")"
decl_stmt|;
try|try
init|(
name|ByteArrayIndexInput
name|indexInput
init|=
operator|new
name|ByteArrayIndexInput
argument_list|(
name|resourceDesc
argument_list|,
name|bytes
argument_list|)
init|)
block|{
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
name|codec
argument_list|,
name|VERSION
argument_list|,
name|VERSION
argument_list|)
expr_stmt|;
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
name|BytesReference
name|bytesReference
init|=
operator|new
name|BytesArray
argument_list|(
name|bytes
argument_list|,
operator|(
name|int
operator|)
name|filePointer
argument_list|,
operator|(
name|int
operator|)
name|contentSize
argument_list|)
decl_stmt|;
return|return
name|read
argument_list|(
name|bytesReference
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|CorruptIndexException
decl||
name|IndexFormatTooOldException
decl||
name|IndexFormatTooNewException
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
comment|/**      * Writes blob in atomic manner with resolving the blob name using {@link #blobName} and {@link #tempBlobName} methods.      *<p>      * The blob will be compressed and checksum will be written if required.      *      * Atomic move might be very inefficient on some repositories. It also cannot override existing files.      *      * @param obj           object to be serialized      * @param blobContainer blob container      * @param name          blob name      */
DECL|method|writeAtomic
specifier|public
name|void
name|writeAtomic
parameter_list|(
name|T
name|obj
parameter_list|,
name|BlobContainer
name|blobContainer
parameter_list|,
name|String
name|name
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|blobName
init|=
name|blobName
argument_list|(
name|name
argument_list|)
decl_stmt|;
name|String
name|tempBlobName
init|=
name|tempBlobName
argument_list|(
name|name
argument_list|)
decl_stmt|;
name|writeBlob
argument_list|(
name|obj
argument_list|,
name|blobContainer
argument_list|,
name|tempBlobName
argument_list|)
expr_stmt|;
try|try
block|{
name|blobContainer
operator|.
name|move
argument_list|(
name|tempBlobName
argument_list|,
name|blobName
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ex
parameter_list|)
block|{
comment|// Move failed - try cleaning up
name|blobContainer
operator|.
name|deleteBlob
argument_list|(
name|tempBlobName
argument_list|)
expr_stmt|;
throw|throw
name|ex
throw|;
block|}
block|}
comment|/**      * Writes blob with resolving the blob name using {@link #blobName} method.      *<p>      * The blob will be compressed and checksum will be written if required.      *      * @param obj           object to be serialized      * @param blobContainer blob container      * @param name          blob name      */
DECL|method|write
specifier|public
name|void
name|write
parameter_list|(
name|T
name|obj
parameter_list|,
name|BlobContainer
name|blobContainer
parameter_list|,
name|String
name|name
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|blobName
init|=
name|blobName
argument_list|(
name|name
argument_list|)
decl_stmt|;
name|writeBlob
argument_list|(
name|obj
argument_list|,
name|blobContainer
argument_list|,
name|blobName
argument_list|)
expr_stmt|;
block|}
comment|/**      * Writes blob in atomic manner without resolving the blobName using using {@link #blobName} method.      *<p>      * The blob will be compressed and checksum will be written if required.      *      * @param obj           object to be serialized      * @param blobContainer blob container      * @param blobName          blob name      */
DECL|method|writeBlob
specifier|protected
name|void
name|writeBlob
parameter_list|(
name|T
name|obj
parameter_list|,
name|BlobContainer
name|blobContainer
parameter_list|,
name|String
name|blobName
parameter_list|)
throws|throws
name|IOException
block|{
name|BytesReference
name|bytes
init|=
name|write
argument_list|(
name|obj
argument_list|)
decl_stmt|;
try|try
init|(
name|ByteArrayOutputStream
name|byteArrayOutputStream
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
init|)
block|{
specifier|final
name|String
name|resourceDesc
init|=
literal|"ChecksumBlobStoreFormat.writeBlob(blob=\""
operator|+
name|blobName
operator|+
literal|"\")"
decl_stmt|;
try|try
init|(
name|OutputStreamIndexOutput
name|indexOutput
init|=
operator|new
name|OutputStreamIndexOutput
argument_list|(
name|resourceDesc
argument_list|,
name|byteArrayOutputStream
argument_list|,
name|BUFFER_SIZE
argument_list|)
init|)
block|{
name|CodecUtil
operator|.
name|writeHeader
argument_list|(
name|indexOutput
argument_list|,
name|codec
argument_list|,
name|VERSION
argument_list|)
expr_stmt|;
try|try
init|(
name|OutputStream
name|indexOutputOutputStream
init|=
operator|new
name|IndexOutputOutputStream
argument_list|(
name|indexOutput
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
init|)
block|{
name|bytes
operator|.
name|writeTo
argument_list|(
name|indexOutputOutputStream
argument_list|)
expr_stmt|;
block|}
name|CodecUtil
operator|.
name|writeFooter
argument_list|(
name|indexOutput
argument_list|)
expr_stmt|;
block|}
name|blobContainer
operator|.
name|writeBlob
argument_list|(
name|blobName
argument_list|,
operator|new
name|BytesArray
argument_list|(
name|byteArrayOutputStream
operator|.
name|toByteArray
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      * Returns true if the blob is a leftover temporary blob.      *      * The temporary blobs might be left after failed atomic write operation.      */
DECL|method|isTempBlobName
specifier|public
name|boolean
name|isTempBlobName
parameter_list|(
name|String
name|blobName
parameter_list|)
block|{
return|return
name|blobName
operator|.
name|startsWith
argument_list|(
name|ChecksumBlobStoreFormat
operator|.
name|TEMP_FILE_PREFIX
argument_list|)
return|;
block|}
DECL|method|write
specifier|protected
name|BytesReference
name|write
parameter_list|(
name|T
name|obj
parameter_list|)
throws|throws
name|IOException
block|{
try|try
init|(
name|BytesStreamOutput
name|bytesStreamOutput
init|=
operator|new
name|BytesStreamOutput
argument_list|()
init|)
block|{
if|if
condition|(
name|compress
condition|)
block|{
try|try
init|(
name|StreamOutput
name|compressedStreamOutput
init|=
name|CompressorFactory
operator|.
name|defaultCompressor
argument_list|()
operator|.
name|streamOutput
argument_list|(
name|bytesStreamOutput
argument_list|)
init|)
block|{
name|write
argument_list|(
name|obj
argument_list|,
name|compressedStreamOutput
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|write
argument_list|(
name|obj
argument_list|,
name|bytesStreamOutput
argument_list|)
expr_stmt|;
block|}
return|return
name|bytesStreamOutput
operator|.
name|bytes
argument_list|()
return|;
block|}
block|}
DECL|method|write
specifier|protected
name|void
name|write
parameter_list|(
name|T
name|obj
parameter_list|,
name|StreamOutput
name|streamOutput
parameter_list|)
throws|throws
name|IOException
block|{
try|try
init|(
name|XContentBuilder
name|builder
init|=
name|XContentFactory
operator|.
name|contentBuilder
argument_list|(
name|xContentType
argument_list|,
name|streamOutput
argument_list|)
init|)
block|{
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|obj
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|SNAPSHOT_ONLY_FORMAT_PARAMS
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
block|}
DECL|method|tempBlobName
specifier|protected
name|String
name|tempBlobName
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
name|TEMP_FILE_PREFIX
operator|+
name|String
operator|.
name|format
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|,
name|blobNameFormat
argument_list|,
name|name
argument_list|)
return|;
block|}
block|}
end_class

end_unit

