begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
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

begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

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
name|ArrayList
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
name|Terms
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
name|BytesRefIterator
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
name|FixedBitSet
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
name|IntBlockPool
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
name|NumericUtils
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
name|IntBlockPool
operator|.
name|Allocator
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
name|IntBlockPool
operator|.
name|DirectAllocator
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticSearchIllegalArgumentException
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
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|fielddata
operator|.
name|util
operator|.
name|IntArrayRef
import|;
end_import

begin_comment
comment|/**  * Simple class to build document ID<-> ordinal mapping. Note: Ordinals are  *<tt>1</tt> based monotocially increasing positive integers.<tt>0</tt>  * donates the missing value in this context.  */
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
DECL|field|ords
specifier|private
specifier|final
name|int
index|[]
name|ords
decl_stmt|;
DECL|field|offsets
specifier|private
name|int
index|[]
name|offsets
decl_stmt|;
DECL|field|pool
specifier|private
specifier|final
name|IntBlockPool
name|pool
decl_stmt|;
DECL|field|writer
specifier|private
specifier|final
name|IntBlockPool
operator|.
name|SliceWriter
name|writer
decl_stmt|;
DECL|field|intsRef
specifier|private
specifier|final
name|IntArrayRef
name|intsRef
init|=
operator|new
name|IntArrayRef
argument_list|(
operator|new
name|int
index|[
literal|1
index|]
argument_list|)
decl_stmt|;
DECL|field|reader
specifier|private
specifier|final
name|IntBlockPool
operator|.
name|SliceReader
name|reader
decl_stmt|;
DECL|field|currentOrd
specifier|private
name|int
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
DECL|method|OrdinalsBuilder
specifier|public
name|OrdinalsBuilder
parameter_list|(
name|Terms
name|terms
parameter_list|,
name|int
name|maxDoc
parameter_list|,
name|Allocator
name|allocator
parameter_list|)
block|{
name|this
operator|.
name|ords
operator|=
operator|new
name|int
index|[
name|maxDoc
index|]
expr_stmt|;
name|pool
operator|=
operator|new
name|IntBlockPool
argument_list|(
name|allocator
argument_list|)
expr_stmt|;
name|reader
operator|=
operator|new
name|IntBlockPool
operator|.
name|SliceReader
argument_list|(
name|pool
argument_list|)
expr_stmt|;
name|writer
operator|=
operator|new
name|IntBlockPool
operator|.
name|SliceWriter
argument_list|(
name|pool
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
block|{
name|this
argument_list|(
literal|null
argument_list|,
name|maxDoc
argument_list|)
expr_stmt|;
block|}
DECL|method|OrdinalsBuilder
specifier|public
name|OrdinalsBuilder
parameter_list|(
name|Terms
name|terms
parameter_list|,
name|int
name|maxDoc
parameter_list|)
block|{
name|this
argument_list|(
name|terms
argument_list|,
name|maxDoc
argument_list|,
operator|new
name|DirectAllocator
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**      * Advances the {@link OrdinalsBuilder} to the next ordinal and      * return the current ordinal.      */
DECL|method|nextOrdinal
specifier|public
name|int
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
name|int
name|currentOrdinal
parameter_list|()
block|{
return|return
name|currentOrd
return|;
block|}
comment|/**      * Associates the given document id with the current ordinal.       */
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
name|int
name|docsOrd
init|=
name|ords
index|[
name|doc
index|]
decl_stmt|;
if|if
condition|(
name|docsOrd
operator|==
literal|0
condition|)
block|{
name|ords
index|[
name|doc
index|]
operator|=
name|currentOrd
expr_stmt|;
name|numDocsWithValue
operator|++
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|docsOrd
operator|>
literal|0
condition|)
block|{
name|numMultiValuedDocs
operator|++
expr_stmt|;
name|int
name|offset
init|=
name|writer
operator|.
name|startNewSlice
argument_list|()
decl_stmt|;
name|writer
operator|.
name|writeInt
argument_list|(
name|docsOrd
argument_list|)
expr_stmt|;
name|writer
operator|.
name|writeInt
argument_list|(
name|currentOrd
argument_list|)
expr_stmt|;
if|if
condition|(
name|offsets
operator|==
literal|null
condition|)
block|{
name|offsets
operator|=
operator|new
name|int
index|[
name|ords
operator|.
name|length
index|]
expr_stmt|;
block|}
name|offsets
index|[
name|doc
index|]
operator|=
name|writer
operator|.
name|getCurrentOffset
argument_list|()
expr_stmt|;
name|ords
index|[
name|doc
index|]
operator|=
operator|(
operator|-
literal|1
operator|*
name|offset
operator|)
operator|-
literal|1
expr_stmt|;
block|}
else|else
block|{
assert|assert
name|offsets
operator|!=
literal|null
assert|;
name|writer
operator|.
name|reset
argument_list|(
name|offsets
index|[
name|doc
index|]
argument_list|)
expr_stmt|;
name|writer
operator|.
name|writeInt
argument_list|(
name|currentOrd
argument_list|)
expr_stmt|;
name|offsets
index|[
name|doc
index|]
operator|=
name|writer
operator|.
name|getCurrentOffset
argument_list|()
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
name|offsets
operator|!=
literal|null
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
comment|/**      * Returns the number of distinct ordinals in this builder.        */
DECL|method|getNumOrds
specifier|public
name|int
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
name|this
operator|.
name|ords
operator|.
name|length
condition|)
return|return
literal|null
return|;
specifier|final
name|FixedBitSet
name|bitSet
init|=
operator|new
name|FixedBitSet
argument_list|(
name|this
operator|.
name|ords
operator|.
name|length
argument_list|)
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
name|ords
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|ords
index|[
name|i
index|]
operator|!=
literal|0
condition|)
block|{
name|bitSet
operator|.
name|set
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|bitSet
return|;
block|}
comment|/**      * Builds an {@link Ordinals} instance from the builders current state.       */
DECL|method|build
specifier|public
name|Ordinals
name|build
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
if|if
condition|(
name|numMultiValuedDocs
operator|==
literal|0
condition|)
block|{
return|return
operator|new
name|SingleArrayOrdinals
argument_list|(
name|ords
argument_list|,
name|getNumOrds
argument_list|()
argument_list|)
return|;
block|}
specifier|final
name|String
name|multiOrdinals
init|=
name|settings
operator|.
name|get
argument_list|(
literal|"multi_ordinals"
argument_list|,
literal|"sparse"
argument_list|)
decl_stmt|;
if|if
condition|(
literal|"flat"
operator|.
name|equals
argument_list|(
name|multiOrdinals
argument_list|)
condition|)
block|{
specifier|final
name|ArrayList
argument_list|<
name|int
index|[]
argument_list|>
name|ordinalBuffer
init|=
operator|new
name|ArrayList
argument_list|<
name|int
index|[]
argument_list|>
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
name|ords
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|IntArrayRef
name|docOrds
init|=
name|docOrds
argument_list|(
name|i
argument_list|)
decl_stmt|;
while|while
condition|(
name|ordinalBuffer
operator|.
name|size
argument_list|()
operator|<
name|docOrds
operator|.
name|size
argument_list|()
condition|)
block|{
name|ordinalBuffer
operator|.
name|add
argument_list|(
operator|new
name|int
index|[
name|ords
operator|.
name|length
index|]
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|j
init|=
name|docOrds
operator|.
name|start
init|;
name|j
operator|<
name|docOrds
operator|.
name|end
condition|;
name|j
operator|++
control|)
block|{
name|ordinalBuffer
operator|.
name|get
argument_list|(
name|j
argument_list|)
index|[
name|i
index|]
operator|=
name|docOrds
operator|.
name|values
index|[
name|j
index|]
expr_stmt|;
block|}
block|}
name|int
index|[]
index|[]
name|nativeOrdinals
init|=
operator|new
name|int
index|[
name|ordinalBuffer
operator|.
name|size
argument_list|()
index|]
index|[]
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
name|nativeOrdinals
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|nativeOrdinals
index|[
name|i
index|]
operator|=
name|ordinalBuffer
operator|.
name|get
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|MultiFlatArrayOrdinals
argument_list|(
name|nativeOrdinals
argument_list|,
name|getNumOrds
argument_list|()
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
literal|"sparse"
operator|.
name|equals
argument_list|(
name|multiOrdinals
argument_list|)
condition|)
block|{
name|int
name|multiOrdinalsMaxDocs
init|=
name|settings
operator|.
name|getAsInt
argument_list|(
literal|"multi_ordinals_max_docs"
argument_list|,
literal|16777216
comment|/* Equal to 64MB per storeage array */
argument_list|)
decl_stmt|;
return|return
operator|new
name|SparseMultiArrayOrdinals
argument_list|(
name|this
argument_list|,
name|multiOrdinalsMaxDocs
argument_list|)
return|;
block|}
else|else
block|{
throw|throw
operator|new
name|ElasticSearchIllegalArgumentException
argument_list|(
literal|"no applicable fielddata multi_ordinals value, got ["
operator|+
name|multiOrdinals
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
comment|/**      * Returns a shared {@link IntArrayRef} instance for the given doc ID holding all ordinals associated with it.      */
DECL|method|docOrds
specifier|public
name|IntArrayRef
name|docOrds
parameter_list|(
name|int
name|doc
parameter_list|)
block|{
name|int
name|docsOrd
init|=
name|ords
index|[
name|doc
index|]
decl_stmt|;
name|intsRef
operator|.
name|start
operator|=
literal|0
expr_stmt|;
if|if
condition|(
name|docsOrd
operator|==
literal|0
condition|)
block|{
name|intsRef
operator|.
name|end
operator|=
literal|0
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|docsOrd
operator|>
literal|0
condition|)
block|{
name|intsRef
operator|.
name|values
index|[
literal|0
index|]
operator|=
name|ords
index|[
name|doc
index|]
expr_stmt|;
name|intsRef
operator|.
name|end
operator|=
literal|1
expr_stmt|;
block|}
else|else
block|{
assert|assert
name|offsets
operator|!=
literal|null
assert|;
name|reader
operator|.
name|reset
argument_list|(
operator|-
literal|1
operator|*
operator|(
name|ords
index|[
name|doc
index|]
operator|+
literal|1
operator|)
argument_list|,
name|offsets
index|[
name|doc
index|]
argument_list|)
expr_stmt|;
name|int
name|pos
init|=
literal|0
decl_stmt|;
while|while
condition|(
operator|!
name|reader
operator|.
name|endOfSlice
argument_list|()
condition|)
block|{
if|if
condition|(
name|intsRef
operator|.
name|values
operator|.
name|length
operator|<=
name|pos
condition|)
block|{
name|intsRef
operator|.
name|values
operator|=
name|ArrayUtil
operator|.
name|grow
argument_list|(
name|intsRef
operator|.
name|values
argument_list|,
name|pos
operator|+
literal|1
argument_list|)
expr_stmt|;
block|}
name|intsRef
operator|.
name|values
index|[
name|pos
operator|++
index|]
operator|=
name|reader
operator|.
name|readInt
argument_list|()
expr_stmt|;
block|}
name|intsRef
operator|.
name|end
operator|=
name|pos
expr_stmt|;
block|}
return|return
name|intsRef
return|;
block|}
comment|/**      * Returns the maximum document ID this builder can associate with an ordinal      */
DECL|method|maxDoc
specifier|public
name|int
name|maxDoc
parameter_list|()
block|{
return|return
name|ords
operator|.
name|length
return|;
block|}
comment|/**      * A {@link TermsEnum} that iterates only full precision prefix coded 64 bit values.      * @see #buildFromTerms(TermsEnum, Bits)      */
DECL|method|wrapNumeric64Bit
specifier|public
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
comment|/**      * A {@link TermsEnum} that iterates only full precision prefix coded 32 bit values.      * @see #buildFromTerms(TermsEnum, Bits)      */
DECL|method|wrapNumeric32Bit
specifier|public
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
parameter_list|,
specifier|final
name|Bits
name|liveDocs
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
name|liveDocs
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
name|pool
operator|.
name|reset
argument_list|(
literal|true
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|offsets
operator|=
literal|null
expr_stmt|;
block|}
block|}
end_class

end_unit

