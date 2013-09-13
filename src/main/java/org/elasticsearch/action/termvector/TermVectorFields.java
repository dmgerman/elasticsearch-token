begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.termvector
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|termvector
package|;
end_package

begin_import
import|import
name|com
operator|.
name|carrotsearch
operator|.
name|hppc
operator|.
name|ObjectLongOpenHashMap
import|;
end_import

begin_import
import|import
name|com
operator|.
name|carrotsearch
operator|.
name|hppc
operator|.
name|cursors
operator|.
name|ObjectLongCursor
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
name|ArrayUtil
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
name|Bits
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
name|RamUsageEstimator
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
name|hppc
operator|.
name|HppcMaps
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
name|BytesStreamInput
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
name|Comparator
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|ArrayUtil
operator|.
name|grow
import|;
end_import

begin_comment
comment|/**  * This class represents the result of a {@link TermVectorRequest}. It works  * exactly like the {@link Fields} class except for one thing: It can return  * offsets and payloads even if positions are not present. You must call  * nextPosition() anyway to move the counter although this method only returns  *<tt>-1,</tt>, if no positions were returned by the {@link TermVectorRequest}.  *<p/>  * The data is stored in two byte arrays ({@code headerRef} and  * {@code termVectors}, both {@link ByteRef}) that have the following format:  *<p/>  * {@code headerRef}: Stores offsets per field in the {@code termVectors} array  * and some header information as {@link BytesRef}. Format is  *<ul>  *<li>String : "TV"</li>  *<li>vint: version (=-1)</li>  *<li>boolean: hasTermStatistics (are the term statistics stored?)</li>  *<li>boolean: hasFieldStatitsics (are the field statistics stored?)</li>  *<li>vint: number of fields</li>  *<ul>  *<li>String: field name 1</li>  *<li>vint: offset in {@code termVectors} for field 1</li>  *<li>...</li>  *<li>String: field name last field</li>  *<li>vint: offset in {@code termVectors} for last field</li>  *</ul>  *</ul>  *<p/>  * termVectors: Stores the actual term vectors as a {@link BytesRef}.  *<p/>  * Term vectors for each fields are stored in blocks, one for each field. The  * offsets in {@code headerRef} are used to find where the block for a field  * starts. Each block begins with a  *<ul>  *<li>vint: number of terms</li>  *<li>boolean: positions (has it positions stored?)</li>  *<li>boolean: offsets (has it offsets stored?)</li>  *<li>boolean: payloads (has it payloads stored?)</li>  *</ul>  * If the field statistics were requested ({@code hasFieldStatistics} is true,  * see {@code headerRef}), the following numbers are stored:  *<ul>  *<li>vlong: sum of total term freqencies of the field (sumTotalTermFreq)</li>  *<li>vlong: sum of document frequencies for each term (sumDocFreq)</li>  *<li>vint: number of documents in the shard that has an entry for this field  * (docCount)</li>  *</ul>  *<p/>  * After that, for each term it stores  *<ul>  *<ul>  *<li>vint: term lengths</li>  *<li>BytesRef: term name</li>  *</ul>  *<p/>  * If term statistics are requested ({@code hasTermStatistics} is true, see  * {@code headerRef}):  *<ul>  *<li>vint: document frequency, how often does this term appear in documents?</li>  *<li>vlong: total term frequency. Sum of terms in this field.</li>  *</ul>  * After that  *<ul>  *<li>vint: frequency (always returned)</li>  *<ul>  *<li>vint: position_1 (if positions == true)</li>  *<li>vint: startOffset_1 (if offset == true)</li>  *<li>vint: endOffset_1 (if offset == true)</li>  *<li>BytesRef: payload_1 (if payloads == true)</li>  *<li>...</li>  *<li>vint: endOffset_freqency (if offset == true)</li>  *<li>BytesRef: payload_freqency (if payloads == true)</li>  *<ul>  *</ul></ul>  */
end_comment

begin_class
DECL|class|TermVectorFields
specifier|public
specifier|final
class|class
name|TermVectorFields
extends|extends
name|Fields
block|{
DECL|field|fieldMap
specifier|final
specifier|private
name|ObjectLongOpenHashMap
argument_list|<
name|String
argument_list|>
name|fieldMap
decl_stmt|;
DECL|field|termVectors
specifier|final
specifier|private
name|BytesReference
name|termVectors
decl_stmt|;
DECL|field|hasTermStatistic
specifier|final
name|boolean
name|hasTermStatistic
decl_stmt|;
DECL|field|hasFieldStatistic
specifier|final
name|boolean
name|hasFieldStatistic
decl_stmt|;
comment|/**      * @param headerRef   Stores offsets per field in the {@code termVectors} and some      *                    header information as {@link BytesRef}.      * @param termVectors Stores the actual term vectors as a {@link BytesRef}.      */
DECL|method|TermVectorFields
specifier|public
name|TermVectorFields
parameter_list|(
name|BytesReference
name|headerRef
parameter_list|,
name|BytesReference
name|termVectors
parameter_list|)
throws|throws
name|IOException
block|{
name|BytesStreamInput
name|header
init|=
operator|new
name|BytesStreamInput
argument_list|(
name|headerRef
argument_list|)
decl_stmt|;
name|fieldMap
operator|=
operator|new
name|ObjectLongOpenHashMap
argument_list|<
name|String
argument_list|>
argument_list|()
expr_stmt|;
comment|// here we read the header to fill the field offset map
name|String
name|headerString
init|=
name|header
operator|.
name|readString
argument_list|()
decl_stmt|;
assert|assert
name|headerString
operator|.
name|equals
argument_list|(
literal|"TV"
argument_list|)
assert|;
name|int
name|version
init|=
name|header
operator|.
name|readInt
argument_list|()
decl_stmt|;
assert|assert
name|version
operator|==
operator|-
literal|1
assert|;
name|hasTermStatistic
operator|=
name|header
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
name|hasFieldStatistic
operator|=
name|header
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
specifier|final
name|int
name|numFields
init|=
name|header
operator|.
name|readVInt
argument_list|()
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
name|numFields
condition|;
name|i
operator|++
control|)
block|{
name|fieldMap
operator|.
name|put
argument_list|(
operator|(
name|header
operator|.
name|readString
argument_list|()
operator|)
argument_list|,
name|header
operator|.
name|readVLong
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|header
operator|.
name|close
argument_list|()
expr_stmt|;
comment|// reference to the term vector data
name|this
operator|.
name|termVectors
operator|=
name|termVectors
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|iterator
specifier|public
name|Iterator
argument_list|<
name|String
argument_list|>
name|iterator
parameter_list|()
block|{
specifier|final
name|Iterator
argument_list|<
name|ObjectLongCursor
argument_list|<
name|String
argument_list|>
argument_list|>
name|iterator
init|=
name|fieldMap
operator|.
name|iterator
argument_list|()
decl_stmt|;
return|return
operator|new
name|Iterator
argument_list|<
name|String
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|hasNext
parameter_list|()
block|{
return|return
name|iterator
operator|.
name|hasNext
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|next
parameter_list|()
block|{
return|return
name|iterator
operator|.
name|next
argument_list|()
operator|.
name|key
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|remove
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
block|}
return|;
block|}
annotation|@
name|Override
DECL|method|terms
specifier|public
name|Terms
name|terms
parameter_list|(
name|String
name|field
parameter_list|)
throws|throws
name|IOException
block|{
comment|// first, find where in the termVectors bytes the actual term vector for
comment|// this field is stored
if|if
condition|(
operator|!
name|fieldMap
operator|.
name|containsKey
argument_list|(
name|field
argument_list|)
condition|)
block|{
return|return
literal|null
return|;
comment|// we don't have it.
block|}
name|long
name|offset
init|=
name|fieldMap
operator|.
name|lget
argument_list|()
decl_stmt|;
specifier|final
name|BytesStreamInput
name|perFieldTermVectorInput
init|=
operator|new
name|BytesStreamInput
argument_list|(
name|this
operator|.
name|termVectors
argument_list|)
decl_stmt|;
name|perFieldTermVectorInput
operator|.
name|reset
argument_list|()
expr_stmt|;
name|perFieldTermVectorInput
operator|.
name|skip
argument_list|(
name|offset
argument_list|)
expr_stmt|;
comment|// read how many terms....
specifier|final
name|long
name|numTerms
init|=
name|perFieldTermVectorInput
operator|.
name|readVLong
argument_list|()
decl_stmt|;
comment|// ...if positions etc. were stored....
specifier|final
name|boolean
name|hasPositions
init|=
name|perFieldTermVectorInput
operator|.
name|readBoolean
argument_list|()
decl_stmt|;
specifier|final
name|boolean
name|hasOffsets
init|=
name|perFieldTermVectorInput
operator|.
name|readBoolean
argument_list|()
decl_stmt|;
specifier|final
name|boolean
name|hasPayloads
init|=
name|perFieldTermVectorInput
operator|.
name|readBoolean
argument_list|()
decl_stmt|;
comment|// read the field statistics
specifier|final
name|long
name|sumTotalTermFreq
init|=
name|hasFieldStatistic
condition|?
name|readPotentiallyNegativeVLong
argument_list|(
name|perFieldTermVectorInput
argument_list|)
else|:
operator|-
literal|1
decl_stmt|;
specifier|final
name|long
name|sumDocFreq
init|=
name|hasFieldStatistic
condition|?
name|readPotentiallyNegativeVLong
argument_list|(
name|perFieldTermVectorInput
argument_list|)
else|:
operator|-
literal|1
decl_stmt|;
specifier|final
name|int
name|docCount
init|=
name|hasFieldStatistic
condition|?
name|readPotentiallyNegativeVInt
argument_list|(
name|perFieldTermVectorInput
argument_list|)
else|:
operator|-
literal|1
decl_stmt|;
return|return
operator|new
name|Terms
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|TermsEnum
name|iterator
parameter_list|(
name|TermsEnum
name|reuse
parameter_list|)
throws|throws
name|IOException
block|{
comment|// convert bytes ref for the terms to actual data
return|return
operator|new
name|TermsEnum
argument_list|()
block|{
name|int
name|currentTerm
init|=
literal|0
decl_stmt|;
name|int
name|freq
init|=
literal|0
decl_stmt|;
name|int
name|docFreq
init|=
operator|-
literal|1
decl_stmt|;
name|long
name|totalTermFrequency
init|=
operator|-
literal|1
decl_stmt|;
name|int
index|[]
name|positions
init|=
operator|new
name|int
index|[
literal|1
index|]
decl_stmt|;
name|int
index|[]
name|startOffsets
init|=
operator|new
name|int
index|[
literal|1
index|]
decl_stmt|;
name|int
index|[]
name|endOffsets
init|=
operator|new
name|int
index|[
literal|1
index|]
decl_stmt|;
name|BytesRef
index|[]
name|payloads
init|=
operator|new
name|BytesRef
index|[
literal|1
index|]
decl_stmt|;
specifier|final
name|BytesRef
name|spare
init|=
operator|new
name|BytesRef
argument_list|()
decl_stmt|;
annotation|@
name|Override
specifier|public
name|BytesRef
name|next
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|currentTerm
operator|++
operator|<
name|numTerms
condition|)
block|{
comment|// term string. first the size...
name|int
name|termVectorSize
init|=
name|perFieldTermVectorInput
operator|.
name|readVInt
argument_list|()
decl_stmt|;
name|spare
operator|.
name|grow
argument_list|(
name|termVectorSize
argument_list|)
expr_stmt|;
comment|// ...then the value.
name|perFieldTermVectorInput
operator|.
name|readBytes
argument_list|(
name|spare
operator|.
name|bytes
argument_list|,
literal|0
argument_list|,
name|termVectorSize
argument_list|)
expr_stmt|;
name|spare
operator|.
name|length
operator|=
name|termVectorSize
expr_stmt|;
if|if
condition|(
name|hasTermStatistic
condition|)
block|{
name|docFreq
operator|=
name|readPotentiallyNegativeVInt
argument_list|(
name|perFieldTermVectorInput
argument_list|)
expr_stmt|;
name|totalTermFrequency
operator|=
name|readPotentiallyNegativeVLong
argument_list|(
name|perFieldTermVectorInput
argument_list|)
expr_stmt|;
block|}
name|freq
operator|=
name|readPotentiallyNegativeVInt
argument_list|(
name|perFieldTermVectorInput
argument_list|)
expr_stmt|;
comment|// grow the arrays to read the values. this is just
comment|// for performance reasons. Re-use memory instead of
comment|// realloc.
name|growBuffers
argument_list|()
expr_stmt|;
comment|// finally, read the values into the arrays
comment|// curentPosition etc. so that we can just iterate
comment|// later
name|writeInfos
argument_list|(
name|perFieldTermVectorInput
argument_list|)
expr_stmt|;
return|return
name|spare
return|;
block|}
else|else
block|{
return|return
literal|null
return|;
block|}
block|}
specifier|private
name|void
name|writeInfos
parameter_list|(
specifier|final
name|BytesStreamInput
name|input
parameter_list|)
throws|throws
name|IOException
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
name|freq
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|hasPositions
condition|)
block|{
name|positions
index|[
name|i
index|]
operator|=
name|input
operator|.
name|readVInt
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|hasOffsets
condition|)
block|{
name|startOffsets
index|[
name|i
index|]
operator|=
name|input
operator|.
name|readVInt
argument_list|()
expr_stmt|;
name|endOffsets
index|[
name|i
index|]
operator|=
name|input
operator|.
name|readVInt
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|hasPayloads
condition|)
block|{
name|int
name|payloadLength
init|=
name|input
operator|.
name|readVInt
argument_list|()
decl_stmt|;
if|if
condition|(
name|payloadLength
operator|>
literal|0
condition|)
block|{
if|if
condition|(
name|payloads
index|[
name|i
index|]
operator|==
literal|null
condition|)
block|{
name|payloads
index|[
name|i
index|]
operator|=
operator|new
name|BytesRef
argument_list|(
name|payloadLength
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|payloads
index|[
name|i
index|]
operator|.
name|grow
argument_list|(
name|payloadLength
argument_list|)
expr_stmt|;
block|}
name|input
operator|.
name|readBytes
argument_list|(
name|payloads
index|[
name|i
index|]
operator|.
name|bytes
argument_list|,
literal|0
argument_list|,
name|payloadLength
argument_list|)
expr_stmt|;
name|payloads
index|[
name|i
index|]
operator|.
name|length
operator|=
name|payloadLength
expr_stmt|;
name|payloads
index|[
name|i
index|]
operator|.
name|offset
operator|=
literal|0
expr_stmt|;
block|}
block|}
block|}
block|}
specifier|private
name|void
name|growBuffers
parameter_list|()
block|{
if|if
condition|(
name|hasPositions
condition|)
block|{
name|positions
operator|=
name|grow
argument_list|(
name|positions
argument_list|,
name|freq
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|hasOffsets
condition|)
block|{
name|startOffsets
operator|=
name|grow
argument_list|(
name|startOffsets
argument_list|,
name|freq
argument_list|)
expr_stmt|;
name|endOffsets
operator|=
name|grow
argument_list|(
name|endOffsets
argument_list|,
name|freq
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|hasPayloads
condition|)
block|{
if|if
condition|(
name|payloads
operator|.
name|length
operator|<
name|freq
condition|)
block|{
specifier|final
name|BytesRef
index|[]
name|newArray
init|=
operator|new
name|BytesRef
index|[
name|ArrayUtil
operator|.
name|oversize
argument_list|(
name|freq
argument_list|,
name|RamUsageEstimator
operator|.
name|NUM_BYTES_OBJECT_REF
argument_list|)
index|]
decl_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|payloads
argument_list|,
literal|0
argument_list|,
name|newArray
argument_list|,
literal|0
argument_list|,
name|payloads
operator|.
name|length
argument_list|)
expr_stmt|;
name|payloads
operator|=
name|newArray
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|Comparator
argument_list|<
name|BytesRef
argument_list|>
name|getComparator
parameter_list|()
block|{
return|return
name|BytesRef
operator|.
name|getUTF8SortedAsUnicodeComparator
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|SeekStatus
name|seekCeil
parameter_list|(
name|BytesRef
name|text
parameter_list|,
name|boolean
name|useCache
parameter_list|)
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|seekExact
parameter_list|(
name|long
name|ord
parameter_list|)
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Seek is not supported"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|BytesRef
name|term
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|spare
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|ord
parameter_list|()
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"ordinals are not supported"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|docFreq
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|docFreq
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|totalTermFreq
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|totalTermFrequency
return|;
block|}
annotation|@
name|Override
specifier|public
name|DocsEnum
name|docs
parameter_list|(
name|Bits
name|liveDocs
parameter_list|,
name|DocsEnum
name|reuse
parameter_list|,
name|int
name|flags
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|docsAndPositions
argument_list|(
name|liveDocs
argument_list|,
name|reuse
operator|instanceof
name|DocsAndPositionsEnum
condition|?
operator|(
name|DocsAndPositionsEnum
operator|)
name|reuse
else|:
literal|null
argument_list|,
literal|0
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|DocsAndPositionsEnum
name|docsAndPositions
parameter_list|(
name|Bits
name|liveDocs
parameter_list|,
name|DocsAndPositionsEnum
name|reuse
parameter_list|,
name|int
name|flags
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|TermVectorsDocsAndPosEnum
name|retVal
init|=
operator|(
name|reuse
operator|instanceof
name|TermVectorsDocsAndPosEnum
condition|?
operator|(
name|TermVectorsDocsAndPosEnum
operator|)
name|reuse
else|:
operator|new
name|TermVectorsDocsAndPosEnum
argument_list|()
operator|)
decl_stmt|;
return|return
name|retVal
operator|.
name|reset
argument_list|(
name|hasPositions
condition|?
name|positions
else|:
literal|null
argument_list|,
name|hasOffsets
condition|?
name|startOffsets
else|:
literal|null
argument_list|,
name|hasOffsets
condition|?
name|endOffsets
else|:
literal|null
argument_list|,
name|hasPayloads
condition|?
name|payloads
else|:
literal|null
argument_list|,
name|freq
argument_list|)
return|;
block|}
block|}
return|;
block|}
annotation|@
name|Override
specifier|public
name|Comparator
argument_list|<
name|BytesRef
argument_list|>
name|getComparator
parameter_list|()
block|{
return|return
name|BytesRef
operator|.
name|getUTF8SortedAsUnicodeComparator
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|size
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|numTerms
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getSumTotalTermFreq
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|sumTotalTermFreq
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getSumDocFreq
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|sumDocFreq
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getDocCount
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|docCount
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|hasOffsets
parameter_list|()
block|{
return|return
name|hasOffsets
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|hasPositions
parameter_list|()
block|{
return|return
name|hasPositions
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|hasPayloads
parameter_list|()
block|{
return|return
name|hasPayloads
return|;
block|}
block|}
return|;
block|}
annotation|@
name|Override
DECL|method|size
specifier|public
name|int
name|size
parameter_list|()
block|{
return|return
name|fieldMap
operator|.
name|size
argument_list|()
return|;
block|}
DECL|class|TermVectorsDocsAndPosEnum
specifier|private
specifier|final
class|class
name|TermVectorsDocsAndPosEnum
extends|extends
name|DocsAndPositionsEnum
block|{
DECL|field|hasPositions
specifier|private
name|boolean
name|hasPositions
decl_stmt|;
DECL|field|hasOffsets
specifier|private
name|boolean
name|hasOffsets
decl_stmt|;
DECL|field|hasPayloads
specifier|private
name|boolean
name|hasPayloads
decl_stmt|;
DECL|field|curPos
name|int
name|curPos
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|doc
name|int
name|doc
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|freq
specifier|private
name|int
name|freq
decl_stmt|;
DECL|field|startOffsets
specifier|private
name|int
index|[]
name|startOffsets
decl_stmt|;
DECL|field|positions
specifier|private
name|int
index|[]
name|positions
decl_stmt|;
DECL|field|payloads
specifier|private
name|BytesRef
index|[]
name|payloads
decl_stmt|;
DECL|field|endOffsets
specifier|private
name|int
index|[]
name|endOffsets
decl_stmt|;
DECL|method|reset
specifier|private
name|DocsAndPositionsEnum
name|reset
parameter_list|(
name|int
index|[]
name|positions
parameter_list|,
name|int
index|[]
name|startOffsets
parameter_list|,
name|int
index|[]
name|endOffsets
parameter_list|,
name|BytesRef
index|[]
name|payloads
parameter_list|,
name|int
name|freq
parameter_list|)
block|{
name|curPos
operator|=
operator|-
literal|1
expr_stmt|;
name|doc
operator|=
operator|-
literal|1
expr_stmt|;
name|this
operator|.
name|hasPositions
operator|=
name|positions
operator|!=
literal|null
expr_stmt|;
name|this
operator|.
name|hasOffsets
operator|=
name|startOffsets
operator|!=
literal|null
expr_stmt|;
name|this
operator|.
name|hasPayloads
operator|=
name|payloads
operator|!=
literal|null
expr_stmt|;
name|this
operator|.
name|freq
operator|=
name|freq
expr_stmt|;
name|this
operator|.
name|startOffsets
operator|=
name|startOffsets
expr_stmt|;
name|this
operator|.
name|endOffsets
operator|=
name|endOffsets
expr_stmt|;
name|this
operator|.
name|payloads
operator|=
name|payloads
expr_stmt|;
name|this
operator|.
name|positions
operator|=
name|positions
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
DECL|method|nextDoc
specifier|public
name|int
name|nextDoc
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|doc
operator|=
operator|(
name|doc
operator|==
operator|-
literal|1
condition|?
literal|0
else|:
name|NO_MORE_DOCS
operator|)
return|;
block|}
annotation|@
name|Override
DECL|method|docID
specifier|public
name|int
name|docID
parameter_list|()
block|{
return|return
name|doc
return|;
block|}
annotation|@
name|Override
DECL|method|advance
specifier|public
name|int
name|advance
parameter_list|(
name|int
name|target
parameter_list|)
throws|throws
name|IOException
block|{
while|while
condition|(
name|nextDoc
argument_list|()
operator|<
name|target
operator|&&
name|doc
operator|!=
name|NO_MORE_DOCS
condition|)
block|{             }
return|return
name|doc
return|;
block|}
annotation|@
name|Override
DECL|method|freq
specifier|public
name|int
name|freq
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|freq
return|;
block|}
comment|// call nextPosition once before calling this one
comment|// because else counter is not advanced
annotation|@
name|Override
DECL|method|startOffset
specifier|public
name|int
name|startOffset
parameter_list|()
throws|throws
name|IOException
block|{
assert|assert
name|curPos
operator|<
name|freq
operator|&&
name|curPos
operator|>=
literal|0
assert|;
return|return
name|hasOffsets
condition|?
name|startOffsets
index|[
name|curPos
index|]
else|:
operator|-
literal|1
return|;
block|}
annotation|@
name|Override
comment|// can return -1 if posistions were not requested or
comment|// stored but offsets were stored and requested
DECL|method|nextPosition
specifier|public
name|int
name|nextPosition
parameter_list|()
throws|throws
name|IOException
block|{
assert|assert
name|curPos
operator|+
literal|1
operator|<
name|freq
assert|;
operator|++
name|curPos
expr_stmt|;
comment|// this is kind of cheating but if you don't need positions
comment|// we safe lots fo space on the wire
return|return
name|hasPositions
condition|?
name|positions
index|[
name|curPos
index|]
else|:
operator|-
literal|1
return|;
block|}
annotation|@
name|Override
DECL|method|getPayload
specifier|public
name|BytesRef
name|getPayload
parameter_list|()
throws|throws
name|IOException
block|{
assert|assert
name|curPos
operator|<
name|freq
operator|&&
name|curPos
operator|>=
literal|0
assert|;
return|return
name|hasPayloads
condition|?
name|payloads
index|[
name|curPos
index|]
else|:
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|endOffset
specifier|public
name|int
name|endOffset
parameter_list|()
throws|throws
name|IOException
block|{
assert|assert
name|curPos
operator|<
name|freq
operator|&&
name|curPos
operator|>=
literal|0
assert|;
return|return
name|hasOffsets
condition|?
name|endOffsets
index|[
name|curPos
index|]
else|:
operator|-
literal|1
return|;
block|}
annotation|@
name|Override
DECL|method|cost
specifier|public
name|long
name|cost
parameter_list|()
block|{
return|return
literal|1
return|;
block|}
block|}
comment|// read a vInt. this is used if the integer might be negative. In this case,
comment|// the writer writes a 0 for -1 or value +1 and accordingly we have to
comment|// substract 1 again
comment|// adds one to mock not existing term freq
DECL|method|readPotentiallyNegativeVInt
name|int
name|readPotentiallyNegativeVInt
parameter_list|(
name|BytesStreamInput
name|stream
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|stream
operator|.
name|readVInt
argument_list|()
operator|-
literal|1
return|;
block|}
comment|// read a vLong. this is used if the integer might be negative. In this
comment|// case, the writer writes a 0 for -1 or value +1 and accordingly we have to
comment|// substract 1 again
comment|// adds one to mock not existing term freq
DECL|method|readPotentiallyNegativeVLong
name|long
name|readPotentiallyNegativeVLong
parameter_list|(
name|BytesStreamInput
name|stream
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|stream
operator|.
name|readVLong
argument_list|()
operator|-
literal|1
return|;
block|}
block|}
end_class

end_unit

