begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.fielddata.ordinals
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|fielddata
operator|.
name|ordinals
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
name|index
operator|.
name|DocsEnum
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
name|FilteredTermsEnum
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
name|TermsEnum
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
name|packed
operator|.
name|GrowableWriter
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
name|packed
operator|.
name|PackedInts
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
name|packed
operator|.
name|PagedGrowableWriter
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
name|settings
operator|.
name|Settings
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Closeable
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
name|Arrays
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

begin_comment
comment|/**  * Simple class to build document ID<-> ordinal mapping. Note: Ordinals are  *<tt>1</tt> based monotonically increasing positive integers.<tt>0</tt>  * donates the missing value in this context.  */
end_comment

begin_class
DECL|class|OrdinalsBuilder
specifier|public
specifier|final
class|class
name|OrdinalsBuilder
implements|implements
name|Closeable
block|{
comment|/**      * Default acceptable overhead ratio. {@link OrdinalsBuilder} memory usage is mostly transient so it is likely a better trade-off to      * trade memory for speed in order to resize less often.      */
DECL|field|DEFAULT_ACCEPTABLE_OVERHEAD_RATIO
specifier|public
specifier|static
specifier|final
name|float
name|DEFAULT_ACCEPTABLE_OVERHEAD_RATIO
init|=
name|PackedInts
operator|.
name|FAST
decl_stmt|;
comment|/**      * The following structure is used to store ordinals. The idea is to store ords on levels of increasing sizes. Level 0 stores      * 1 value and 1 pointer to level 1. Level 1 stores 2 values and 1 pointer to level 2, ..., Level n stores 2**n values and      * 1 pointer to level n+1. If at some point an ordinal or a pointer has 0 as a value, this means that there are no remaining      * values. On the first level, ordinals.get(docId) is the first ordinal for docId or 0 if the document has no ordinals. On      * subsequent levels, the first 2^level slots are reserved and all have 0 as a value.      *<pre>      * Example for an index of 3 docs (O=ordinal, P = pointer)      * Level 0:      *   ordinals           [1] [4] [2]      *   nextLevelSlices    2  0  1      * Level 1:      *   ordinals           [0  0] [2  0] [3  4]      *   nextLevelSlices    0  0  1      * Level 2:      *   ordinals           [0  0  0  0] [5  0  0  0]      *   nextLevelSlices    0  0      *</pre>      * On level 0, all documents have an ordinal: 0 has 1, 1 has 4 and 2 has 2 as a first ordinal, this means that we need to read      * nextLevelEntries to get the index of their ordinals on the next level. The entry for document 1 is 0, meaning that we have      * already read all its ordinals. On the contrary 0 and 2 have more ordinals which are stored at indices 2 and 1. Let's continue      * with document 2: it has 2 more ordinals on level 1: 3 and 4 and its next level index is 1 meaning that there are remaining      * ordinals on the next level. On level 2 at index 1, we can read [5  0  0  0] meaning that 5 is an ordinal as well, but the      * fact that it is followed by zeros means that there are no more ordinals. In the end, document 2 has 2, 3, 4 and 5 as ordinals.      *<p/>      * In addition to these structures, there is another array which stores the current position (level + slice + offset in the slice)      * in order to be able to append data in constant time.      */
DECL|class|OrdinalsStore
specifier|private
specifier|static
class|class
name|OrdinalsStore
block|{
DECL|field|PAGE_SIZE
specifier|private
specifier|static
specifier|final
name|int
name|PAGE_SIZE
init|=
literal|1
operator|<<
literal|12
decl_stmt|;
comment|/**          * Number of slots at<code>level</code>          */
DECL|method|numSlots
specifier|private
specifier|static
name|int
name|numSlots
parameter_list|(
name|int
name|level
parameter_list|)
block|{
return|return
literal|1
operator|<<
name|level
return|;
block|}
DECL|method|slotsMask
specifier|private
specifier|static
name|int
name|slotsMask
parameter_list|(
name|int
name|level
parameter_list|)
block|{
return|return
name|numSlots
argument_list|(
name|level
argument_list|)
operator|-
literal|1
return|;
block|}
comment|/**          * Encode the position for the given level and offset. The idea is to encode the level using unary coding in the lower bits and          * then the offset in the higher bits.          */
DECL|method|position
specifier|private
specifier|static
name|long
name|position
parameter_list|(
name|int
name|level
parameter_list|,
name|long
name|offset
parameter_list|)
block|{
assert|assert
name|level
operator|>=
literal|1
assert|;
return|return
operator|(
literal|1
operator|<<
operator|(
name|level
operator|-
literal|1
operator|)
operator|)
operator||
operator|(
name|offset
operator|<<
name|level
operator|)
return|;
block|}
comment|/**          * Decode the level from an encoded position.          */
DECL|method|level
specifier|private
specifier|static
name|int
name|level
parameter_list|(
name|long
name|position
parameter_list|)
block|{
return|return
literal|1
operator|+
name|Long
operator|.
name|numberOfTrailingZeros
argument_list|(
name|position
argument_list|)
return|;
block|}
comment|/**          * Decode the offset from the position.          */
DECL|method|offset
specifier|private
specifier|static
name|long
name|offset
parameter_list|(
name|long
name|position
parameter_list|,
name|int
name|level
parameter_list|)
block|{
return|return
name|position
operator|>>>
name|level
return|;
block|}
comment|/**          * Get the ID of the slice given an offset.          */
DECL|method|sliceID
specifier|private
specifier|static
name|long
name|sliceID
parameter_list|(
name|int
name|level
parameter_list|,
name|long
name|offset
parameter_list|)
block|{
return|return
name|offset
operator|>>>
name|level
return|;
block|}
comment|/**          * Compute the first offset of the given slice.          */
DECL|method|startOffset
specifier|private
specifier|static
name|long
name|startOffset
parameter_list|(
name|int
name|level
parameter_list|,
name|long
name|slice
parameter_list|)
block|{
return|return
name|slice
operator|<<
name|level
return|;
block|}
comment|/**          * Compute the number of ordinals stored for a value given its current position.          */
DECL|method|numOrdinals
specifier|private
specifier|static
name|int
name|numOrdinals
parameter_list|(
name|int
name|level
parameter_list|,
name|long
name|offset
parameter_list|)
block|{
return|return
operator|(
literal|1
operator|<<
name|level
operator|)
operator|+
call|(
name|int
call|)
argument_list|(
name|offset
operator|&
name|slotsMask
argument_list|(
name|level
argument_list|)
argument_list|)
return|;
block|}
comment|// Current position
DECL|field|positions
specifier|private
name|PagedGrowableWriter
name|positions
decl_stmt|;
comment|// First level (0) of ordinals and pointers to the next level
DECL|field|firstOrdinals
specifier|private
specifier|final
name|GrowableWriter
name|firstOrdinals
decl_stmt|;
DECL|field|firstNextLevelSlices
specifier|private
name|PagedGrowableWriter
name|firstNextLevelSlices
decl_stmt|;
comment|// Ordinals and pointers for other levels, starting at 1
DECL|field|ordinals
specifier|private
specifier|final
name|PagedGrowableWriter
index|[]
name|ordinals
decl_stmt|;
DECL|field|nextLevelSlices
specifier|private
specifier|final
name|PagedGrowableWriter
index|[]
name|nextLevelSlices
decl_stmt|;
DECL|field|sizes
specifier|private
specifier|final
name|int
index|[]
name|sizes
decl_stmt|;
DECL|field|startBitsPerValue
specifier|private
specifier|final
name|int
name|startBitsPerValue
decl_stmt|;
DECL|field|acceptableOverheadRatio
specifier|private
specifier|final
name|float
name|acceptableOverheadRatio
decl_stmt|;
DECL|method|OrdinalsStore
name|OrdinalsStore
parameter_list|(
name|int
name|maxDoc
parameter_list|,
name|int
name|startBitsPerValue
parameter_list|,
name|float
name|acceptableOverheadRatio
parameter_list|)
block|{
name|this
operator|.
name|startBitsPerValue
operator|=
name|startBitsPerValue
expr_stmt|;
name|this
operator|.
name|acceptableOverheadRatio
operator|=
name|acceptableOverheadRatio
expr_stmt|;
name|positions
operator|=
operator|new
name|PagedGrowableWriter
argument_list|(
name|maxDoc
argument_list|,
name|PAGE_SIZE
argument_list|,
name|startBitsPerValue
argument_list|,
name|acceptableOverheadRatio
argument_list|)
expr_stmt|;
name|firstOrdinals
operator|=
operator|new
name|GrowableWriter
argument_list|(
name|startBitsPerValue
argument_list|,
name|maxDoc
argument_list|,
name|acceptableOverheadRatio
argument_list|)
expr_stmt|;
comment|// over allocate in order to never worry about the array sizes, 24 entries would allow to store several millions of ordinals per doc...
name|ordinals
operator|=
operator|new
name|PagedGrowableWriter
index|[
literal|24
index|]
expr_stmt|;
name|nextLevelSlices
operator|=
operator|new
name|PagedGrowableWriter
index|[
literal|24
index|]
expr_stmt|;
name|sizes
operator|=
operator|new
name|int
index|[
literal|24
index|]
expr_stmt|;
name|Arrays
operator|.
name|fill
argument_list|(
name|sizes
argument_list|,
literal|1
argument_list|)
expr_stmt|;
comment|// reserve the 1st slice on every level
block|}
comment|/**          * Allocate a new slice and return its ID.          */
DECL|method|newSlice
specifier|private
name|long
name|newSlice
parameter_list|(
name|int
name|level
parameter_list|)
block|{
specifier|final
name|long
name|newSlice
init|=
name|sizes
index|[
name|level
index|]
operator|++
decl_stmt|;
comment|// Lazily allocate ordinals
if|if
condition|(
name|ordinals
index|[
name|level
index|]
operator|==
literal|null
condition|)
block|{
name|ordinals
index|[
name|level
index|]
operator|=
operator|new
name|PagedGrowableWriter
argument_list|(
literal|8L
operator|*
name|numSlots
argument_list|(
name|level
argument_list|)
argument_list|,
name|PAGE_SIZE
argument_list|,
name|startBitsPerValue
argument_list|,
name|acceptableOverheadRatio
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|ordinals
index|[
name|level
index|]
operator|=
name|ordinals
index|[
name|level
index|]
operator|.
name|grow
argument_list|(
name|sizes
index|[
name|level
index|]
operator|*
name|numSlots
argument_list|(
name|level
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|nextLevelSlices
index|[
name|level
index|]
operator|!=
literal|null
condition|)
block|{
name|nextLevelSlices
index|[
name|level
index|]
operator|=
name|nextLevelSlices
index|[
name|level
index|]
operator|.
name|grow
argument_list|(
name|sizes
index|[
name|level
index|]
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|newSlice
return|;
block|}
DECL|method|addOrdinal
specifier|public
name|int
name|addOrdinal
parameter_list|(
name|int
name|docID
parameter_list|,
name|long
name|ordinal
parameter_list|)
block|{
specifier|final
name|long
name|position
init|=
name|positions
operator|.
name|get
argument_list|(
name|docID
argument_list|)
decl_stmt|;
if|if
condition|(
name|position
operator|==
literal|0L
condition|)
block|{
comment|// on the first level
comment|// 0 or 1 ordinal
if|if
condition|(
name|firstOrdinals
operator|.
name|get
argument_list|(
name|docID
argument_list|)
operator|==
literal|0L
condition|)
block|{
name|firstOrdinals
operator|.
name|set
argument_list|(
name|docID
argument_list|,
name|ordinal
argument_list|)
expr_stmt|;
return|return
literal|1
return|;
block|}
else|else
block|{
specifier|final
name|long
name|newSlice
init|=
name|newSlice
argument_list|(
literal|1
argument_list|)
decl_stmt|;
if|if
condition|(
name|firstNextLevelSlices
operator|==
literal|null
condition|)
block|{
name|firstNextLevelSlices
operator|=
operator|new
name|PagedGrowableWriter
argument_list|(
name|firstOrdinals
operator|.
name|size
argument_list|()
argument_list|,
name|PAGE_SIZE
argument_list|,
literal|3
argument_list|,
name|acceptableOverheadRatio
argument_list|)
expr_stmt|;
block|}
name|firstNextLevelSlices
operator|.
name|set
argument_list|(
name|docID
argument_list|,
name|newSlice
argument_list|)
expr_stmt|;
specifier|final
name|long
name|offset
init|=
name|startOffset
argument_list|(
literal|1
argument_list|,
name|newSlice
argument_list|)
decl_stmt|;
name|ordinals
index|[
literal|1
index|]
operator|.
name|set
argument_list|(
name|offset
argument_list|,
name|ordinal
argument_list|)
expr_stmt|;
name|positions
operator|.
name|set
argument_list|(
name|docID
argument_list|,
name|position
argument_list|(
literal|1
argument_list|,
name|offset
argument_list|)
argument_list|)
expr_stmt|;
comment|// current position is on the 1st level and not allocated yet
return|return
literal|2
return|;
block|}
block|}
else|else
block|{
name|int
name|level
init|=
name|level
argument_list|(
name|position
argument_list|)
decl_stmt|;
name|long
name|offset
init|=
name|offset
argument_list|(
name|position
argument_list|,
name|level
argument_list|)
decl_stmt|;
assert|assert
name|offset
operator|!=
literal|0L
assert|;
if|if
condition|(
operator|(
operator|(
name|offset
operator|+
literal|1
operator|)
operator|&
name|slotsMask
argument_list|(
name|level
argument_list|)
operator|)
operator|==
literal|0L
condition|)
block|{
comment|// reached the end of the slice, allocate a new one on the next level
specifier|final
name|long
name|newSlice
init|=
name|newSlice
argument_list|(
name|level
operator|+
literal|1
argument_list|)
decl_stmt|;
if|if
condition|(
name|nextLevelSlices
index|[
name|level
index|]
operator|==
literal|null
condition|)
block|{
name|nextLevelSlices
index|[
name|level
index|]
operator|=
operator|new
name|PagedGrowableWriter
argument_list|(
name|sizes
index|[
name|level
index|]
argument_list|,
name|PAGE_SIZE
argument_list|,
literal|1
argument_list|,
name|acceptableOverheadRatio
argument_list|)
expr_stmt|;
block|}
name|nextLevelSlices
index|[
name|level
index|]
operator|.
name|set
argument_list|(
name|sliceID
argument_list|(
name|level
argument_list|,
name|offset
argument_list|)
argument_list|,
name|newSlice
argument_list|)
expr_stmt|;
operator|++
name|level
expr_stmt|;
name|offset
operator|=
name|startOffset
argument_list|(
name|level
argument_list|,
name|newSlice
argument_list|)
expr_stmt|;
assert|assert
operator|(
name|offset
operator|&
name|slotsMask
argument_list|(
name|level
argument_list|)
operator|)
operator|==
literal|0L
assert|;
block|}
else|else
block|{
comment|// just go to the next slot
operator|++
name|offset
expr_stmt|;
block|}
name|ordinals
index|[
name|level
index|]
operator|.
name|set
argument_list|(
name|offset
argument_list|,
name|ordinal
argument_list|)
expr_stmt|;
specifier|final
name|long
name|newPosition
init|=
name|position
argument_list|(
name|level
argument_list|,
name|offset
argument_list|)
decl_stmt|;
name|positions
operator|.
name|set
argument_list|(
name|docID
argument_list|,
name|newPosition
argument_list|)
expr_stmt|;
return|return
name|numOrdinals
argument_list|(
name|level
argument_list|,
name|offset
argument_list|)
return|;
block|}
block|}
DECL|method|appendOrdinals
specifier|public
name|void
name|appendOrdinals
parameter_list|(
name|int
name|docID
parameter_list|,
name|LongsRef
name|ords
parameter_list|)
block|{
comment|// First level
specifier|final
name|long
name|firstOrd
init|=
name|firstOrdinals
operator|.
name|get
argument_list|(
name|docID
argument_list|)
decl_stmt|;
if|if
condition|(
name|firstOrd
operator|==
literal|0L
condition|)
block|{
return|return;
block|}
name|ords
operator|.
name|longs
operator|=
name|ArrayUtil
operator|.
name|grow
argument_list|(
name|ords
operator|.
name|longs
argument_list|,
name|ords
operator|.
name|offset
operator|+
name|ords
operator|.
name|length
operator|+
literal|1
argument_list|)
expr_stmt|;
name|ords
operator|.
name|longs
index|[
name|ords
operator|.
name|offset
operator|+
name|ords
operator|.
name|length
operator|++
index|]
operator|=
name|firstOrd
expr_stmt|;
if|if
condition|(
name|firstNextLevelSlices
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|long
name|sliceID
init|=
name|firstNextLevelSlices
operator|.
name|get
argument_list|(
name|docID
argument_list|)
decl_stmt|;
if|if
condition|(
name|sliceID
operator|==
literal|0L
condition|)
block|{
return|return;
block|}
comment|// Other levels
for|for
control|(
name|int
name|level
init|=
literal|1
init|;
condition|;
operator|++
name|level
control|)
block|{
specifier|final
name|int
name|numSlots
init|=
name|numSlots
argument_list|(
name|level
argument_list|)
decl_stmt|;
name|ords
operator|.
name|longs
operator|=
name|ArrayUtil
operator|.
name|grow
argument_list|(
name|ords
operator|.
name|longs
argument_list|,
name|ords
operator|.
name|offset
operator|+
name|ords
operator|.
name|length
operator|+
name|numSlots
argument_list|)
expr_stmt|;
specifier|final
name|long
name|offset
init|=
name|startOffset
argument_list|(
name|level
argument_list|,
name|sliceID
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|numSlots
condition|;
operator|++
name|j
control|)
block|{
specifier|final
name|long
name|ord
init|=
name|ordinals
index|[
name|level
index|]
operator|.
name|get
argument_list|(
name|offset
operator|+
name|j
argument_list|)
decl_stmt|;
if|if
condition|(
name|ord
operator|==
literal|0L
condition|)
block|{
return|return;
block|}
name|ords
operator|.
name|longs
index|[
name|ords
operator|.
name|offset
operator|+
name|ords
operator|.
name|length
operator|++
index|]
operator|=
name|ord
expr_stmt|;
block|}
if|if
condition|(
name|nextLevelSlices
index|[
name|level
index|]
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|sliceID
operator|=
name|nextLevelSlices
index|[
name|level
index|]
operator|.
name|get
argument_list|(
name|sliceID
argument_list|)
expr_stmt|;
if|if
condition|(
name|sliceID
operator|==
literal|0L
condition|)
block|{
return|return;
block|}
block|}
block|}
block|}
DECL|field|maxDoc
specifier|private
specifier|final
name|int
name|maxDoc
decl_stmt|;
DECL|field|currentOrd
specifier|private
name|long
name|currentOrd
init|=
literal|0
decl_stmt|;
DECL|field|numDocsWithValue
specifier|private
name|int
name|numDocsWithValue
init|=
literal|0
decl_stmt|;
DECL|field|numMultiValuedDocs
specifier|private
name|int
name|numMultiValuedDocs
init|=
literal|0
decl_stmt|;
DECL|field|totalNumOrds
specifier|private
name|int
name|totalNumOrds
init|=
literal|0
decl_stmt|;
DECL|field|ordinals
specifier|private
name|OrdinalsStore
name|ordinals
decl_stmt|;
DECL|field|spare
specifier|private
specifier|final
name|LongsRef
name|spare
decl_stmt|;
DECL|method|OrdinalsBuilder
specifier|public
name|OrdinalsBuilder
parameter_list|(
name|long
name|numTerms
parameter_list|,
name|int
name|maxDoc
parameter_list|,
name|float
name|acceptableOverheadRatio
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|maxDoc
operator|=
name|maxDoc
expr_stmt|;
name|int
name|startBitsPerValue
init|=
literal|8
decl_stmt|;
if|if
condition|(
name|numTerms
operator|>=
literal|0
condition|)
block|{
name|startBitsPerValue
operator|=
name|PackedInts
operator|.
name|bitsRequired
argument_list|(
name|numTerms
argument_list|)
expr_stmt|;
block|}
name|ordinals
operator|=
operator|new
name|OrdinalsStore
argument_list|(
name|maxDoc
argument_list|,
name|startBitsPerValue
argument_list|,
name|acceptableOverheadRatio
argument_list|)
expr_stmt|;
name|spare
operator|=
operator|new
name|LongsRef
argument_list|()
expr_stmt|;
block|}
DECL|method|OrdinalsBuilder
specifier|public
name|OrdinalsBuilder
parameter_list|(
name|int
name|maxDoc
parameter_list|,
name|float
name|acceptableOverheadRatio
parameter_list|)
throws|throws
name|IOException
block|{
name|this
argument_list|(
operator|-
literal|1
argument_list|,
name|maxDoc
argument_list|,
name|acceptableOverheadRatio
argument_list|)
expr_stmt|;
block|}
DECL|method|OrdinalsBuilder
specifier|public
name|OrdinalsBuilder
parameter_list|(
name|int
name|maxDoc
parameter_list|)
throws|throws
name|IOException
block|{
name|this
argument_list|(
name|maxDoc
argument_list|,
name|DEFAULT_ACCEPTABLE_OVERHEAD_RATIO
argument_list|)
expr_stmt|;
block|}
comment|/**      * Returns a shared {@link LongsRef} instance for the given doc ID holding all ordinals associated with it.      */
DECL|method|docOrds
specifier|public
name|LongsRef
name|docOrds
parameter_list|(
name|int
name|docID
parameter_list|)
block|{
name|spare
operator|.
name|offset
operator|=
name|spare
operator|.
name|length
operator|=
literal|0
expr_stmt|;
name|ordinals
operator|.
name|appendOrdinals
argument_list|(
name|docID
argument_list|,
name|spare
argument_list|)
expr_stmt|;
return|return
name|spare
return|;
block|}
comment|/**      * Return a {@link PackedInts.Reader} instance mapping every doc ID to its first ordinal if it exists and 0 otherwise.      */
DECL|method|getFirstOrdinals
specifier|public
name|PackedInts
operator|.
name|Reader
name|getFirstOrdinals
parameter_list|()
block|{
return|return
name|ordinals
operator|.
name|firstOrdinals
return|;
block|}
comment|/**      * Advances the {@link OrdinalsBuilder} to the next ordinal and      * return the current ordinal.      */
DECL|method|nextOrdinal
specifier|public
name|long
name|nextOrdinal
parameter_list|()
block|{
return|return
operator|++
name|currentOrd
return|;
block|}
comment|/**      * Retruns the current ordinal or<tt>0</tt> if this build has not been advanced via      * {@link #nextOrdinal()}.      */
DECL|method|currentOrdinal
specifier|public
name|long
name|currentOrdinal
parameter_list|()
block|{
return|return
name|currentOrd
return|;
block|}
comment|/**      * Associates the given document id with the current ordinal.      */
DECL|method|addDoc
specifier|public
name|OrdinalsBuilder
name|addDoc
parameter_list|(
name|int
name|doc
parameter_list|)
block|{
name|totalNumOrds
operator|++
expr_stmt|;
specifier|final
name|int
name|numValues
init|=
name|ordinals
operator|.
name|addOrdinal
argument_list|(
name|doc
argument_list|,
name|currentOrd
argument_list|)
decl_stmt|;
if|if
condition|(
name|numValues
operator|==
literal|1
condition|)
block|{
operator|++
name|numDocsWithValue
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|numValues
operator|==
literal|2
condition|)
block|{
operator|++
name|numMultiValuedDocs
expr_stmt|;
block|}
return|return
name|this
return|;
block|}
comment|/**      * Returns<code>true</code> iff this builder contains a document ID that is associated with more than one ordinal. Otherwise<code>false</code>;      */
DECL|method|isMultiValued
specifier|public
name|boolean
name|isMultiValued
parameter_list|()
block|{
return|return
name|numMultiValuedDocs
operator|>
literal|0
return|;
block|}
comment|/**      * Returns the number distinct of document IDs with one or more values.      */
DECL|method|getNumDocsWithValue
specifier|public
name|int
name|getNumDocsWithValue
parameter_list|()
block|{
return|return
name|numDocsWithValue
return|;
block|}
comment|/**      * Returns the number distinct of document IDs associated with exactly one value.      */
DECL|method|getNumSingleValuedDocs
specifier|public
name|int
name|getNumSingleValuedDocs
parameter_list|()
block|{
return|return
name|numDocsWithValue
operator|-
name|numMultiValuedDocs
return|;
block|}
comment|/**      * Returns the number distinct of document IDs associated with two or more values.      */
DECL|method|getNumMultiValuesDocs
specifier|public
name|int
name|getNumMultiValuesDocs
parameter_list|()
block|{
return|return
name|numMultiValuedDocs
return|;
block|}
comment|/**      * Returns the number of document ID to ordinal pairs in this builder.      */
DECL|method|getTotalNumOrds
specifier|public
name|int
name|getTotalNumOrds
parameter_list|()
block|{
return|return
name|totalNumOrds
return|;
block|}
comment|/**      * Returns the number of distinct ordinals in this builder.      */
DECL|method|getNumOrds
specifier|public
name|long
name|getNumOrds
parameter_list|()
block|{
return|return
name|currentOrd
return|;
block|}
comment|/**      * Builds a {@link FixedBitSet} where each documents bit is that that has one or more ordinals associated with it.      * if every document has an ordinal associated with it this method returns<code>null</code>      */
DECL|method|buildDocsWithValuesSet
specifier|public
name|FixedBitSet
name|buildDocsWithValuesSet
parameter_list|()
block|{
if|if
condition|(
name|numDocsWithValue
operator|==
name|maxDoc
condition|)
block|{
return|return
literal|null
return|;
block|}
specifier|final
name|FixedBitSet
name|bitSet
init|=
operator|new
name|FixedBitSet
argument_list|(
name|maxDoc
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|docID
init|=
literal|0
init|;
name|docID
operator|<
name|maxDoc
condition|;
operator|++
name|docID
control|)
block|{
if|if
condition|(
name|ordinals
operator|.
name|firstOrdinals
operator|.
name|get
argument_list|(
name|docID
argument_list|)
operator|!=
literal|0
condition|)
block|{
name|bitSet
operator|.
name|set
argument_list|(
name|docID
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|bitSet
return|;
block|}
comment|/**      * Builds an {@link Ordinals} instance from the builders current state.      */
DECL|method|build
specifier|public
name|Ordinals
name|build
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
specifier|final
name|float
name|acceptableOverheadRatio
init|=
name|settings
operator|.
name|getAsFloat
argument_list|(
literal|"acceptable_overhead_ratio"
argument_list|,
name|PackedInts
operator|.
name|FASTEST
argument_list|)
decl_stmt|;
if|if
condition|(
name|numMultiValuedDocs
operator|>
literal|0
operator|||
name|MultiOrdinals
operator|.
name|significantlySmallerThanSinglePackedOrdinals
argument_list|(
name|maxDoc
argument_list|,
name|numDocsWithValue
argument_list|,
name|getNumOrds
argument_list|()
argument_list|,
name|acceptableOverheadRatio
argument_list|)
condition|)
block|{
comment|// MultiOrdinals can be smaller than SinglePackedOrdinals for sparse fields
return|return
operator|new
name|MultiOrdinals
argument_list|(
name|this
argument_list|,
name|acceptableOverheadRatio
argument_list|)
return|;
block|}
else|else
block|{
return|return
operator|new
name|SinglePackedOrdinals
argument_list|(
name|this
argument_list|,
name|acceptableOverheadRatio
argument_list|)
return|;
block|}
block|}
comment|/**      * Returns the maximum document ID this builder can associate with an ordinal      */
DECL|method|maxDoc
specifier|public
name|int
name|maxDoc
parameter_list|()
block|{
return|return
name|maxDoc
return|;
block|}
comment|/**      * A {@link TermsEnum} that iterates only full precision prefix coded 64 bit values.      *      * @see #buildFromTerms(TermsEnum, Bits)      */
DECL|method|wrapNumeric64Bit
specifier|public
specifier|static
name|TermsEnum
name|wrapNumeric64Bit
parameter_list|(
name|TermsEnum
name|termsEnum
parameter_list|)
block|{
return|return
operator|new
name|FilteredTermsEnum
argument_list|(
name|termsEnum
argument_list|,
literal|false
argument_list|)
block|{
annotation|@
name|Override
specifier|protected
name|AcceptStatus
name|accept
parameter_list|(
name|BytesRef
name|term
parameter_list|)
throws|throws
name|IOException
block|{
comment|// we stop accepting terms once we moved across the prefix codec terms - redundant values!
return|return
name|NumericUtils
operator|.
name|getPrefixCodedLongShift
argument_list|(
name|term
argument_list|)
operator|==
literal|0
condition|?
name|AcceptStatus
operator|.
name|YES
else|:
name|AcceptStatus
operator|.
name|END
return|;
block|}
block|}
return|;
block|}
comment|/**      * A {@link TermsEnum} that iterates only full precision prefix coded 32 bit values.      *      * @see #buildFromTerms(TermsEnum, Bits)      */
DECL|method|wrapNumeric32Bit
specifier|public
specifier|static
name|TermsEnum
name|wrapNumeric32Bit
parameter_list|(
name|TermsEnum
name|termsEnum
parameter_list|)
block|{
return|return
operator|new
name|FilteredTermsEnum
argument_list|(
name|termsEnum
argument_list|,
literal|false
argument_list|)
block|{
annotation|@
name|Override
specifier|protected
name|AcceptStatus
name|accept
parameter_list|(
name|BytesRef
name|term
parameter_list|)
throws|throws
name|IOException
block|{
comment|// we stop accepting terms once we moved across the prefix codec terms - redundant values!
return|return
name|NumericUtils
operator|.
name|getPrefixCodedIntShift
argument_list|(
name|term
argument_list|)
operator|==
literal|0
condition|?
name|AcceptStatus
operator|.
name|YES
else|:
name|AcceptStatus
operator|.
name|END
return|;
block|}
block|}
return|;
block|}
comment|/**      * This method iterates all terms in the given {@link TermsEnum} and      * associates each terms ordinal with the terms documents. The caller must      * exhaust the returned {@link BytesRefIterator} which returns all values      * where the first returned value is associted with the ordinal<tt>1</tt>      * etc.      *<p>      * If the {@link TermsEnum} contains prefix coded numerical values the terms      * enum should be wrapped with either {@link #wrapNumeric32Bit(TermsEnum)}      * or {@link #wrapNumeric64Bit(TermsEnum)} depending on its precision. If      * the {@link TermsEnum} is not wrapped the returned      * {@link BytesRefIterator} will contain partial precision terms rather than      * only full-precision terms.      *</p>      */
DECL|method|buildFromTerms
specifier|public
name|BytesRefIterator
name|buildFromTerms
parameter_list|(
specifier|final
name|TermsEnum
name|termsEnum
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|BytesRefIterator
argument_list|()
block|{
specifier|private
name|DocsEnum
name|docsEnum
init|=
literal|null
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
name|BytesRef
name|ref
decl_stmt|;
if|if
condition|(
operator|(
name|ref
operator|=
name|termsEnum
operator|.
name|next
argument_list|()
operator|)
operator|!=
literal|null
condition|)
block|{
name|docsEnum
operator|=
name|termsEnum
operator|.
name|docs
argument_list|(
literal|null
argument_list|,
name|docsEnum
argument_list|,
name|DocsEnum
operator|.
name|FLAG_NONE
argument_list|)
expr_stmt|;
name|nextOrdinal
argument_list|()
expr_stmt|;
name|int
name|docId
decl_stmt|;
while|while
condition|(
operator|(
name|docId
operator|=
name|docsEnum
operator|.
name|nextDoc
argument_list|()
operator|)
operator|!=
name|DocsEnum
operator|.
name|NO_MORE_DOCS
condition|)
block|{
name|addDoc
argument_list|(
name|docId
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|ref
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
name|termsEnum
operator|.
name|getComparator
argument_list|()
return|;
block|}
block|}
return|;
block|}
comment|/**      * Closes this builder and release all resources.      */
annotation|@
name|Override
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
name|ordinals
operator|=
literal|null
expr_stmt|;
block|}
block|}
end_class

end_unit

