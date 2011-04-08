begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.lucene.docset
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|lucene
operator|.
name|docset
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
name|search
operator|.
name|DocIdSetIterator
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
name|BitUtil
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
name|RamUsage
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

begin_comment
comment|/**  * Derived from {@link org.apache.lucene.util.OpenBitSet} but works from a slice out of a provided long[] array.  * It does not expand, as it assumes that the slice is from a cached long[] array, so we can't really expand...  */
end_comment

begin_class
DECL|class|SlicedOpenBitSet
specifier|public
class|class
name|SlicedOpenBitSet
extends|extends
name|DocSet
block|{
DECL|field|bits
specifier|private
specifier|final
name|long
index|[]
name|bits
decl_stmt|;
DECL|field|wlen
specifier|private
specifier|final
name|int
name|wlen
decl_stmt|;
comment|// number of words (elements) used in the array
DECL|field|from
specifier|private
specifier|final
name|int
name|from
decl_stmt|;
comment|// the from index in the array
DECL|method|SlicedOpenBitSet
specifier|public
name|SlicedOpenBitSet
parameter_list|(
name|long
index|[]
name|bits
parameter_list|,
name|int
name|wlen
parameter_list|,
name|int
name|from
parameter_list|)
block|{
name|this
operator|.
name|bits
operator|=
name|bits
expr_stmt|;
name|this
operator|.
name|wlen
operator|=
name|wlen
expr_stmt|;
name|this
operator|.
name|from
operator|=
name|from
expr_stmt|;
block|}
DECL|method|isCacheable
annotation|@
name|Override
specifier|public
name|boolean
name|isCacheable
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
DECL|method|sizeInBytes
annotation|@
name|Override
specifier|public
name|long
name|sizeInBytes
parameter_list|()
block|{
return|return
name|wlen
operator|*
name|RamUsage
operator|.
name|NUM_BYTES_LONG
operator|+
name|RamUsage
operator|.
name|NUM_BYTES_ARRAY_HEADER
operator|+
name|RamUsage
operator|.
name|NUM_BYTES_INT
comment|/* wlen */
return|;
block|}
comment|/**      * Returns the current capacity in bits (1 greater than the index of the last bit)      */
DECL|method|capacity
specifier|public
name|long
name|capacity
parameter_list|()
block|{
return|return
operator|(
name|bits
operator|.
name|length
operator|-
name|from
operator|)
operator|<<
literal|6
return|;
block|}
comment|/**      * Returns the current capacity of this set.  Included for      * compatibility.  This is *not* equal to {@link #cardinality}      */
DECL|method|size
specifier|public
name|long
name|size
parameter_list|()
block|{
return|return
name|capacity
argument_list|()
return|;
block|}
comment|/**      * @return the number of set bits      */
DECL|method|cardinality
specifier|public
name|long
name|cardinality
parameter_list|()
block|{
return|return
name|BitUtil
operator|.
name|pop_array
argument_list|(
name|bits
argument_list|,
name|from
argument_list|,
name|wlen
argument_list|)
return|;
block|}
comment|/**      * Returns true or false for the specified bit index.      */
DECL|method|get
specifier|public
name|boolean
name|get
parameter_list|(
name|int
name|index
parameter_list|)
block|{
return|return
name|fastGet
argument_list|(
name|index
argument_list|)
return|;
comment|//        int i = index>> 6;               // div 64
comment|//        // signed shift will keep a negative index and force an
comment|//        // array-index-out-of-bounds-exception, removing the need for an explicit check.
comment|//        if (from + i>= wlen) return false;
comment|//
comment|//        int bit = index& 0x3f;           // mod 64
comment|//        long bitmask = 1L<< bit;
comment|//        return (bits[from + i]& bitmask) != 0;
block|}
comment|/**      * Returns true or false for the specified bit index.      * The index should be less than the OpenBitSet size      */
DECL|method|fastGet
specifier|public
name|boolean
name|fastGet
parameter_list|(
name|int
name|index
parameter_list|)
block|{
name|int
name|i
init|=
name|index
operator|>>
literal|6
decl_stmt|;
comment|// div 64
comment|// signed shift will keep a negative index and force an
comment|// array-index-out-of-bounds-exception, removing the need for an explicit check.
name|int
name|bit
init|=
name|index
operator|&
literal|0x3f
decl_stmt|;
comment|// mod 64
name|long
name|bitmask
init|=
literal|1L
operator|<<
name|bit
decl_stmt|;
return|return
operator|(
name|bits
index|[
name|from
operator|+
name|i
index|]
operator|&
name|bitmask
operator|)
operator|!=
literal|0
return|;
block|}
comment|/**      * Returns true or false for the specified bit index      */
DECL|method|get
specifier|public
name|boolean
name|get
parameter_list|(
name|long
name|index
parameter_list|)
block|{
name|int
name|i
init|=
call|(
name|int
call|)
argument_list|(
name|index
operator|>>
literal|6
argument_list|)
decl_stmt|;
comment|// div 64
if|if
condition|(
name|from
operator|+
name|i
operator|>=
name|wlen
condition|)
return|return
literal|false
return|;
name|int
name|bit
init|=
operator|(
name|int
operator|)
name|index
operator|&
literal|0x3f
decl_stmt|;
comment|// mod 64
name|long
name|bitmask
init|=
literal|1L
operator|<<
name|bit
decl_stmt|;
return|return
operator|(
name|bits
index|[
name|from
operator|+
name|i
index|]
operator|&
name|bitmask
operator|)
operator|!=
literal|0
return|;
block|}
comment|/**      * Returns true or false for the specified bit index.      * The index should be less than the OpenBitSet size.      */
DECL|method|fastGet
specifier|public
name|boolean
name|fastGet
parameter_list|(
name|long
name|index
parameter_list|)
block|{
name|int
name|i
init|=
call|(
name|int
call|)
argument_list|(
name|index
operator|>>
literal|6
argument_list|)
decl_stmt|;
comment|// div 64
name|int
name|bit
init|=
operator|(
name|int
operator|)
name|index
operator|&
literal|0x3f
decl_stmt|;
comment|// mod 64
name|long
name|bitmask
init|=
literal|1L
operator|<<
name|bit
decl_stmt|;
return|return
operator|(
name|bits
index|[
name|from
operator|+
name|i
index|]
operator|&
name|bitmask
operator|)
operator|!=
literal|0
return|;
block|}
comment|/**      * Sets the bit at the specified index.      * The index should be less than the OpenBitSet size.      */
DECL|method|fastSet
specifier|public
name|void
name|fastSet
parameter_list|(
name|int
name|index
parameter_list|)
block|{
name|int
name|wordNum
init|=
name|index
operator|>>
literal|6
decl_stmt|;
comment|// div 64
name|int
name|bit
init|=
name|index
operator|&
literal|0x3f
decl_stmt|;
comment|// mod 64
name|long
name|bitmask
init|=
literal|1L
operator|<<
name|bit
decl_stmt|;
name|bits
index|[
name|from
operator|+
name|wordNum
index|]
operator||=
name|bitmask
expr_stmt|;
block|}
comment|/**      * Sets the bit at the specified index.      * The index should be less than the OpenBitSet size.      */
DECL|method|fastSet
specifier|public
name|void
name|fastSet
parameter_list|(
name|long
name|index
parameter_list|)
block|{
name|int
name|wordNum
init|=
call|(
name|int
call|)
argument_list|(
name|index
operator|>>
literal|6
argument_list|)
decl_stmt|;
name|int
name|bit
init|=
operator|(
name|int
operator|)
name|index
operator|&
literal|0x3f
decl_stmt|;
name|long
name|bitmask
init|=
literal|1L
operator|<<
name|bit
decl_stmt|;
name|bits
index|[
name|from
operator|+
name|wordNum
index|]
operator||=
name|bitmask
expr_stmt|;
block|}
DECL|method|iterator
annotation|@
name|Override
specifier|public
name|DocIdSetIterator
name|iterator
parameter_list|()
throws|throws
name|IOException
block|{
return|return
operator|new
name|SlicedIterator
argument_list|(
name|this
argument_list|)
return|;
block|}
comment|/**      * An iterator to iterate over set bits in an OpenBitSet.      * This is faster than nextSetBit() for iterating over the complete set of bits,      * especially when the density of the bits set is high.      */
DECL|class|SlicedIterator
specifier|public
specifier|static
class|class
name|SlicedIterator
extends|extends
name|DocIdSetIterator
block|{
comment|// The General Idea: instead of having an array per byte that has
comment|// the offsets of the next set bit, that array could be
comment|// packed inside a 32 bit integer (8 4 bit numbers).  That
comment|// should be faster than accessing an array for each index, and
comment|// the total array size is kept smaller (256*sizeof(int))=1K
DECL|field|bitlist
specifier|protected
specifier|final
specifier|static
name|int
index|[]
name|bitlist
init|=
block|{
literal|0x0
block|,
literal|0x1
block|,
literal|0x2
block|,
literal|0x21
block|,
literal|0x3
block|,
literal|0x31
block|,
literal|0x32
block|,
literal|0x321
block|,
literal|0x4
block|,
literal|0x41
block|,
literal|0x42
block|,
literal|0x421
block|,
literal|0x43
block|,
literal|0x431
block|,
literal|0x432
block|,
literal|0x4321
block|,
literal|0x5
block|,
literal|0x51
block|,
literal|0x52
block|,
literal|0x521
block|,
literal|0x53
block|,
literal|0x531
block|,
literal|0x532
block|,
literal|0x5321
block|,
literal|0x54
block|,
literal|0x541
block|,
literal|0x542
block|,
literal|0x5421
block|,
literal|0x543
block|,
literal|0x5431
block|,
literal|0x5432
block|,
literal|0x54321
block|,
literal|0x6
block|,
literal|0x61
block|,
literal|0x62
block|,
literal|0x621
block|,
literal|0x63
block|,
literal|0x631
block|,
literal|0x632
block|,
literal|0x6321
block|,
literal|0x64
block|,
literal|0x641
block|,
literal|0x642
block|,
literal|0x6421
block|,
literal|0x643
block|,
literal|0x6431
block|,
literal|0x6432
block|,
literal|0x64321
block|,
literal|0x65
block|,
literal|0x651
block|,
literal|0x652
block|,
literal|0x6521
block|,
literal|0x653
block|,
literal|0x6531
block|,
literal|0x6532
block|,
literal|0x65321
block|,
literal|0x654
block|,
literal|0x6541
block|,
literal|0x6542
block|,
literal|0x65421
block|,
literal|0x6543
block|,
literal|0x65431
block|,
literal|0x65432
block|,
literal|0x654321
block|,
literal|0x7
block|,
literal|0x71
block|,
literal|0x72
block|,
literal|0x721
block|,
literal|0x73
block|,
literal|0x731
block|,
literal|0x732
block|,
literal|0x7321
block|,
literal|0x74
block|,
literal|0x741
block|,
literal|0x742
block|,
literal|0x7421
block|,
literal|0x743
block|,
literal|0x7431
block|,
literal|0x7432
block|,
literal|0x74321
block|,
literal|0x75
block|,
literal|0x751
block|,
literal|0x752
block|,
literal|0x7521
block|,
literal|0x753
block|,
literal|0x7531
block|,
literal|0x7532
block|,
literal|0x75321
block|,
literal|0x754
block|,
literal|0x7541
block|,
literal|0x7542
block|,
literal|0x75421
block|,
literal|0x7543
block|,
literal|0x75431
block|,
literal|0x75432
block|,
literal|0x754321
block|,
literal|0x76
block|,
literal|0x761
block|,
literal|0x762
block|,
literal|0x7621
block|,
literal|0x763
block|,
literal|0x7631
block|,
literal|0x7632
block|,
literal|0x76321
block|,
literal|0x764
block|,
literal|0x7641
block|,
literal|0x7642
block|,
literal|0x76421
block|,
literal|0x7643
block|,
literal|0x76431
block|,
literal|0x76432
block|,
literal|0x764321
block|,
literal|0x765
block|,
literal|0x7651
block|,
literal|0x7652
block|,
literal|0x76521
block|,
literal|0x7653
block|,
literal|0x76531
block|,
literal|0x76532
block|,
literal|0x765321
block|,
literal|0x7654
block|,
literal|0x76541
block|,
literal|0x76542
block|,
literal|0x765421
block|,
literal|0x76543
block|,
literal|0x765431
block|,
literal|0x765432
block|,
literal|0x7654321
block|,
literal|0x8
block|,
literal|0x81
block|,
literal|0x82
block|,
literal|0x821
block|,
literal|0x83
block|,
literal|0x831
block|,
literal|0x832
block|,
literal|0x8321
block|,
literal|0x84
block|,
literal|0x841
block|,
literal|0x842
block|,
literal|0x8421
block|,
literal|0x843
block|,
literal|0x8431
block|,
literal|0x8432
block|,
literal|0x84321
block|,
literal|0x85
block|,
literal|0x851
block|,
literal|0x852
block|,
literal|0x8521
block|,
literal|0x853
block|,
literal|0x8531
block|,
literal|0x8532
block|,
literal|0x85321
block|,
literal|0x854
block|,
literal|0x8541
block|,
literal|0x8542
block|,
literal|0x85421
block|,
literal|0x8543
block|,
literal|0x85431
block|,
literal|0x85432
block|,
literal|0x854321
block|,
literal|0x86
block|,
literal|0x861
block|,
literal|0x862
block|,
literal|0x8621
block|,
literal|0x863
block|,
literal|0x8631
block|,
literal|0x8632
block|,
literal|0x86321
block|,
literal|0x864
block|,
literal|0x8641
block|,
literal|0x8642
block|,
literal|0x86421
block|,
literal|0x8643
block|,
literal|0x86431
block|,
literal|0x86432
block|,
literal|0x864321
block|,
literal|0x865
block|,
literal|0x8651
block|,
literal|0x8652
block|,
literal|0x86521
block|,
literal|0x8653
block|,
literal|0x86531
block|,
literal|0x86532
block|,
literal|0x865321
block|,
literal|0x8654
block|,
literal|0x86541
block|,
literal|0x86542
block|,
literal|0x865421
block|,
literal|0x86543
block|,
literal|0x865431
block|,
literal|0x865432
block|,
literal|0x8654321
block|,
literal|0x87
block|,
literal|0x871
block|,
literal|0x872
block|,
literal|0x8721
block|,
literal|0x873
block|,
literal|0x8731
block|,
literal|0x8732
block|,
literal|0x87321
block|,
literal|0x874
block|,
literal|0x8741
block|,
literal|0x8742
block|,
literal|0x87421
block|,
literal|0x8743
block|,
literal|0x87431
block|,
literal|0x87432
block|,
literal|0x874321
block|,
literal|0x875
block|,
literal|0x8751
block|,
literal|0x8752
block|,
literal|0x87521
block|,
literal|0x8753
block|,
literal|0x87531
block|,
literal|0x87532
block|,
literal|0x875321
block|,
literal|0x8754
block|,
literal|0x87541
block|,
literal|0x87542
block|,
literal|0x875421
block|,
literal|0x87543
block|,
literal|0x875431
block|,
literal|0x875432
block|,
literal|0x8754321
block|,
literal|0x876
block|,
literal|0x8761
block|,
literal|0x8762
block|,
literal|0x87621
block|,
literal|0x8763
block|,
literal|0x87631
block|,
literal|0x87632
block|,
literal|0x876321
block|,
literal|0x8764
block|,
literal|0x87641
block|,
literal|0x87642
block|,
literal|0x876421
block|,
literal|0x87643
block|,
literal|0x876431
block|,
literal|0x876432
block|,
literal|0x8764321
block|,
literal|0x8765
block|,
literal|0x87651
block|,
literal|0x87652
block|,
literal|0x876521
block|,
literal|0x87653
block|,
literal|0x876531
block|,
literal|0x876532
block|,
literal|0x8765321
block|,
literal|0x87654
block|,
literal|0x876541
block|,
literal|0x876542
block|,
literal|0x8765421
block|,
literal|0x876543
block|,
literal|0x8765431
block|,
literal|0x8765432
block|,
literal|0x87654321
block|}
decl_stmt|;
comment|/**          * ** the python code that generated bitlist          * def bits2int(val):          * arr=0          * for shift in range(8,0,-1):          * if val& 0x80:          * arr = (arr<< 4) | shift          * val = val<< 1          * return arr          *          * def int_table():          * tbl = [ hex(bits2int(val)).strip('L') for val in range(256) ]          * return ','.join(tbl)          * ****          */
comment|// hmmm, what about an iterator that finds zeros though,
comment|// or a reverse iterator... should they be separate classes
comment|// for efficiency, or have a common root interface?  (or
comment|// maybe both?  could ask for a SetBitsIterator, etc...
DECL|field|arr
specifier|private
specifier|final
name|long
index|[]
name|arr
decl_stmt|;
DECL|field|words
specifier|private
specifier|final
name|int
name|words
decl_stmt|;
DECL|field|from
specifier|private
specifier|final
name|int
name|from
decl_stmt|;
DECL|field|i
specifier|private
name|int
name|i
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|word
specifier|private
name|long
name|word
decl_stmt|;
DECL|field|wordShift
specifier|private
name|int
name|wordShift
decl_stmt|;
DECL|field|indexArray
specifier|private
name|int
name|indexArray
decl_stmt|;
DECL|field|curDocId
specifier|private
name|int
name|curDocId
init|=
operator|-
literal|1
decl_stmt|;
DECL|method|SlicedIterator
specifier|public
name|SlicedIterator
parameter_list|(
name|SlicedOpenBitSet
name|obs
parameter_list|)
block|{
name|this
operator|.
name|arr
operator|=
name|obs
operator|.
name|bits
expr_stmt|;
name|this
operator|.
name|words
operator|=
name|obs
operator|.
name|wlen
expr_stmt|;
name|this
operator|.
name|from
operator|=
name|obs
operator|.
name|from
expr_stmt|;
block|}
comment|// 64 bit shifts
DECL|method|shift
specifier|private
name|void
name|shift
parameter_list|()
block|{
if|if
condition|(
operator|(
name|int
operator|)
name|word
operator|==
literal|0
condition|)
block|{
name|wordShift
operator|+=
literal|32
expr_stmt|;
name|word
operator|=
name|word
operator|>>>
literal|32
expr_stmt|;
block|}
if|if
condition|(
operator|(
name|word
operator|&
literal|0x0000FFFF
operator|)
operator|==
literal|0
condition|)
block|{
name|wordShift
operator|+=
literal|16
expr_stmt|;
name|word
operator|>>>=
literal|16
expr_stmt|;
block|}
if|if
condition|(
operator|(
name|word
operator|&
literal|0x000000FF
operator|)
operator|==
literal|0
condition|)
block|{
name|wordShift
operator|+=
literal|8
expr_stmt|;
name|word
operator|>>>=
literal|8
expr_stmt|;
block|}
name|indexArray
operator|=
name|bitlist
index|[
operator|(
name|int
operator|)
name|word
operator|&
literal|0xff
index|]
expr_stmt|;
block|}
comment|/**          * ** alternate shift implementations          * // 32 bit shifts, but a long shift needed at the end          * private void shift2() {          * int y = (int)word;          * if (y==0) {wordShift +=32; y = (int)(word>>>32); }          * if ((y& 0x0000FFFF) == 0) { wordShift +=16; y>>>=16; }          * if ((y& 0x000000FF) == 0) { wordShift +=8; y>>>=8; }          * indexArray = bitlist[y& 0xff];          * word>>>= (wordShift +1);          * }          *          * private void shift3() {          * int lower = (int)word;          * int lowByte = lower& 0xff;          * if (lowByte != 0) {          * indexArray=bitlist[lowByte];          * return;          * }          * shift();          * }          * ****          */
annotation|@
name|Override
DECL|method|nextDoc
specifier|public
name|int
name|nextDoc
parameter_list|()
block|{
if|if
condition|(
name|indexArray
operator|==
literal|0
condition|)
block|{
if|if
condition|(
name|word
operator|!=
literal|0
condition|)
block|{
name|word
operator|>>>=
literal|8
expr_stmt|;
name|wordShift
operator|+=
literal|8
expr_stmt|;
block|}
while|while
condition|(
name|word
operator|==
literal|0
condition|)
block|{
if|if
condition|(
operator|++
name|i
operator|>=
name|words
condition|)
block|{
return|return
name|curDocId
operator|=
name|NO_MORE_DOCS
return|;
block|}
name|word
operator|=
name|arr
index|[
name|from
operator|+
name|i
index|]
expr_stmt|;
name|wordShift
operator|=
operator|-
literal|1
expr_stmt|;
comment|// loop invariant code motion should move this
block|}
comment|// after the first time, should I go with a linear search, or
comment|// stick with the binary search in shift?
name|shift
argument_list|()
expr_stmt|;
block|}
name|int
name|bitIndex
init|=
operator|(
name|indexArray
operator|&
literal|0x0f
operator|)
operator|+
name|wordShift
decl_stmt|;
name|indexArray
operator|>>>=
literal|4
expr_stmt|;
comment|// should i<<6 be cached as a separate variable?
comment|// it would only save one cycle in the best circumstances.
return|return
name|curDocId
operator|=
operator|(
name|i
operator|<<
literal|6
operator|)
operator|+
name|bitIndex
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
block|{
name|indexArray
operator|=
literal|0
expr_stmt|;
name|i
operator|=
name|target
operator|>>
literal|6
expr_stmt|;
if|if
condition|(
name|i
operator|>=
name|words
condition|)
block|{
name|word
operator|=
literal|0
expr_stmt|;
comment|// setup so next() will also return -1
return|return
name|curDocId
operator|=
name|NO_MORE_DOCS
return|;
block|}
name|wordShift
operator|=
name|target
operator|&
literal|0x3f
expr_stmt|;
name|word
operator|=
name|arr
index|[
name|from
operator|+
name|i
index|]
operator|>>>
name|wordShift
expr_stmt|;
if|if
condition|(
name|word
operator|!=
literal|0
condition|)
block|{
name|wordShift
operator|--
expr_stmt|;
comment|// compensate for 1 based arrIndex
block|}
else|else
block|{
while|while
condition|(
name|word
operator|==
literal|0
condition|)
block|{
if|if
condition|(
operator|++
name|i
operator|>=
name|words
condition|)
block|{
return|return
name|curDocId
operator|=
name|NO_MORE_DOCS
return|;
block|}
name|word
operator|=
name|arr
index|[
name|from
operator|+
name|i
index|]
expr_stmt|;
block|}
name|wordShift
operator|=
operator|-
literal|1
expr_stmt|;
block|}
name|shift
argument_list|()
expr_stmt|;
name|int
name|bitIndex
init|=
operator|(
name|indexArray
operator|&
literal|0x0f
operator|)
operator|+
name|wordShift
decl_stmt|;
name|indexArray
operator|>>>=
literal|4
expr_stmt|;
comment|// should i<<6 be cached as a separate variable?
comment|// it would only save one cycle in the best circumstances.
return|return
name|curDocId
operator|=
operator|(
name|i
operator|<<
literal|6
operator|)
operator|+
name|bitIndex
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
name|curDocId
return|;
block|}
block|}
block|}
end_class

end_unit

