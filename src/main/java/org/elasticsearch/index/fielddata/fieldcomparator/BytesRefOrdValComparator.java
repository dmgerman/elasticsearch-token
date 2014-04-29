begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.fielddata.fieldcomparator
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|fielddata
operator|.
name|fieldcomparator
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
name|AtomicReaderContext
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
name|search
operator|.
name|FieldComparator
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
name|elasticsearch
operator|.
name|index
operator|.
name|fielddata
operator|.
name|BytesValues
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
name|IndexFieldData
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
name|ordinals
operator|.
name|Ordinals
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|MultiValueMode
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
comment|/**  * Sorts by field's natural Term sort order, using  * ordinals.  This is functionally equivalent to {@link  * org.apache.lucene.search.FieldComparator.TermValComparator}, but it first resolves the string  * to their relative ordinal positions (using the index  * returned by {@link org.apache.lucene.search.FieldCache#getTermsIndex}), and  * does most comparisons using the ordinals.  For medium  * to large results, this comparator will be much faster  * than {@link org.apache.lucene.search.FieldComparator.TermValComparator}.  For very small  * result sets it may be slower.  *  * Internally this comparator multiplies ordinals by 4 so that virtual ordinals can be inserted in-between the original field data ordinals.  * Thanks to this, an ordinal for the missing value and the bottom value can be computed and all ordinals are directly comparable. For example,  * if the field data ordinals are (a,1), (b,2) and (c,3), they will be internally stored as (a,4), (b,8), (c,12). Then the ordinal for the  * missing value will be computed by binary searching. For example, if the missing value is 'ab', it will be assigned 6 as an ordinal (between  * 'a' and 'b'. And if the bottom value is 'ac', it will be assigned 7 as an ordinal (between 'ab' and 'b').  */
end_comment

begin_class
DECL|class|BytesRefOrdValComparator
specifier|public
specifier|final
class|class
name|BytesRefOrdValComparator
extends|extends
name|NestedWrappableComparator
argument_list|<
name|BytesRef
argument_list|>
block|{
DECL|field|indexFieldData
specifier|final
name|IndexFieldData
operator|.
name|WithOrdinals
argument_list|<
name|?
argument_list|>
name|indexFieldData
decl_stmt|;
DECL|field|missingValue
specifier|final
name|BytesRef
name|missingValue
decl_stmt|;
comment|/* Ords for each slot, times 4.        @lucene.internal */
DECL|field|ords
specifier|final
name|long
index|[]
name|ords
decl_stmt|;
DECL|field|sortMode
specifier|final
name|MultiValueMode
name|sortMode
decl_stmt|;
comment|/* Values for each slot.        @lucene.internal */
DECL|field|values
specifier|final
name|BytesRef
index|[]
name|values
decl_stmt|;
comment|/* Which reader last copied a value into the slot. When        we compare two slots, we just compare-by-ord if the        readerGen is the same; else we must compare the        values (slower).        @lucene.internal */
DECL|field|readerGen
specifier|final
name|int
index|[]
name|readerGen
decl_stmt|;
comment|/* Gen of current reader we are on.        @lucene.internal */
DECL|field|currentReaderGen
name|int
name|currentReaderGen
init|=
operator|-
literal|1
decl_stmt|;
comment|/* Current reader's doc ord/values.        @lucene.internal */
DECL|field|termsIndex
name|BytesValues
operator|.
name|WithOrdinals
name|termsIndex
decl_stmt|;
DECL|field|missingOrd
name|long
name|missingOrd
decl_stmt|;
comment|/* Bottom slot, or -1 if queue isn't full yet        @lucene.internal */
DECL|field|bottomSlot
name|int
name|bottomSlot
init|=
operator|-
literal|1
decl_stmt|;
comment|/* Bottom ord (same as ords[bottomSlot] once bottomSlot        is set).  Cached for faster compares.        @lucene.internal */
DECL|field|bottomOrd
name|long
name|bottomOrd
decl_stmt|;
DECL|field|top
name|BytesRef
name|top
decl_stmt|;
DECL|field|topOrd
name|long
name|topOrd
decl_stmt|;
DECL|method|BytesRefOrdValComparator
specifier|public
name|BytesRefOrdValComparator
parameter_list|(
name|IndexFieldData
operator|.
name|WithOrdinals
argument_list|<
name|?
argument_list|>
name|indexFieldData
parameter_list|,
name|int
name|numHits
parameter_list|,
name|MultiValueMode
name|sortMode
parameter_list|,
name|BytesRef
name|missingValue
parameter_list|)
block|{
name|this
operator|.
name|indexFieldData
operator|=
name|indexFieldData
expr_stmt|;
name|this
operator|.
name|sortMode
operator|=
name|sortMode
expr_stmt|;
name|this
operator|.
name|missingValue
operator|=
name|missingValue
expr_stmt|;
name|ords
operator|=
operator|new
name|long
index|[
name|numHits
index|]
expr_stmt|;
name|values
operator|=
operator|new
name|BytesRef
index|[
name|numHits
index|]
expr_stmt|;
name|readerGen
operator|=
operator|new
name|int
index|[
name|numHits
index|]
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|compare
specifier|public
name|int
name|compare
parameter_list|(
name|int
name|slot1
parameter_list|,
name|int
name|slot2
parameter_list|)
block|{
if|if
condition|(
name|readerGen
index|[
name|slot1
index|]
operator|==
name|readerGen
index|[
name|slot2
index|]
condition|)
block|{
specifier|final
name|int
name|res
init|=
name|Long
operator|.
name|compare
argument_list|(
name|ords
index|[
name|slot1
index|]
argument_list|,
name|ords
index|[
name|slot2
index|]
argument_list|)
decl_stmt|;
assert|assert
name|Integer
operator|.
name|signum
argument_list|(
name|res
argument_list|)
operator|==
name|Integer
operator|.
name|signum
argument_list|(
name|compareValues
argument_list|(
name|values
index|[
name|slot1
index|]
argument_list|,
name|values
index|[
name|slot2
index|]
argument_list|)
argument_list|)
operator|:
name|values
index|[
name|slot1
index|]
operator|+
literal|" "
operator|+
name|values
index|[
name|slot2
index|]
operator|+
literal|" "
operator|+
name|ords
index|[
name|slot1
index|]
operator|+
literal|" "
operator|+
name|ords
index|[
name|slot2
index|]
assert|;
return|return
name|res
return|;
block|}
specifier|final
name|BytesRef
name|val1
init|=
name|values
index|[
name|slot1
index|]
decl_stmt|;
specifier|final
name|BytesRef
name|val2
init|=
name|values
index|[
name|slot2
index|]
decl_stmt|;
return|return
name|compareValues
argument_list|(
name|val1
argument_list|,
name|val2
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|compareBottom
specifier|public
name|int
name|compareBottom
parameter_list|(
name|int
name|doc
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
DECL|method|compareTop
specifier|public
name|int
name|compareTop
parameter_list|(
name|int
name|doc
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
DECL|method|compareBottomMissing
specifier|public
name|int
name|compareBottomMissing
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
DECL|method|copy
specifier|public
name|void
name|copy
parameter_list|(
name|int
name|slot
parameter_list|,
name|int
name|doc
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
DECL|method|missing
specifier|public
name|void
name|missing
parameter_list|(
name|int
name|slot
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
DECL|method|compareTopMissing
specifier|public
name|int
name|compareTopMissing
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
DECL|class|PerSegmentComparator
class|class
name|PerSegmentComparator
extends|extends
name|NestedWrappableComparator
argument_list|<
name|BytesRef
argument_list|>
block|{
DECL|field|readerOrds
specifier|final
name|Ordinals
operator|.
name|Docs
name|readerOrds
decl_stmt|;
DECL|field|termsIndex
specifier|final
name|BytesValues
operator|.
name|WithOrdinals
name|termsIndex
decl_stmt|;
DECL|method|PerSegmentComparator
specifier|public
name|PerSegmentComparator
parameter_list|(
name|BytesValues
operator|.
name|WithOrdinals
name|termsIndex
parameter_list|)
block|{
name|this
operator|.
name|readerOrds
operator|=
name|termsIndex
operator|.
name|ordinals
argument_list|()
expr_stmt|;
name|this
operator|.
name|termsIndex
operator|=
name|termsIndex
expr_stmt|;
if|if
condition|(
name|readerOrds
operator|.
name|getMaxOrd
argument_list|()
operator|>
name|Long
operator|.
name|MAX_VALUE
operator|/
literal|4
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Current terms index pretends it has more than "
operator|+
operator|(
name|Long
operator|.
name|MAX_VALUE
operator|/
literal|4
operator|)
operator|+
literal|" ordinals, which is unsupported by this impl"
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
DECL|method|setNextReader
specifier|public
name|FieldComparator
argument_list|<
name|BytesRef
argument_list|>
name|setNextReader
parameter_list|(
name|AtomicReaderContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|BytesRefOrdValComparator
operator|.
name|this
operator|.
name|setNextReader
argument_list|(
name|context
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|compare
specifier|public
name|int
name|compare
parameter_list|(
name|int
name|slot1
parameter_list|,
name|int
name|slot2
parameter_list|)
block|{
return|return
name|BytesRefOrdValComparator
operator|.
name|this
operator|.
name|compare
argument_list|(
name|slot1
argument_list|,
name|slot2
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|setBottom
specifier|public
name|void
name|setBottom
parameter_list|(
specifier|final
name|int
name|bottom
parameter_list|)
block|{
name|BytesRefOrdValComparator
operator|.
name|this
operator|.
name|setBottom
argument_list|(
name|bottom
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|setTopValue
specifier|public
name|void
name|setTopValue
parameter_list|(
name|BytesRef
name|value
parameter_list|)
block|{
name|BytesRefOrdValComparator
operator|.
name|this
operator|.
name|setTopValue
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|value
specifier|public
name|BytesRef
name|value
parameter_list|(
name|int
name|slot
parameter_list|)
block|{
return|return
name|BytesRefOrdValComparator
operator|.
name|this
operator|.
name|value
argument_list|(
name|slot
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|compareValues
specifier|public
name|int
name|compareValues
parameter_list|(
name|BytesRef
name|val1
parameter_list|,
name|BytesRef
name|val2
parameter_list|)
block|{
if|if
condition|(
name|val1
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|val2
operator|==
literal|null
condition|)
block|{
return|return
literal|0
return|;
block|}
return|return
operator|-
literal|1
return|;
block|}
elseif|else
if|if
condition|(
name|val2
operator|==
literal|null
condition|)
block|{
return|return
literal|1
return|;
block|}
return|return
name|val1
operator|.
name|compareTo
argument_list|(
name|val2
argument_list|)
return|;
block|}
DECL|method|getOrd
specifier|protected
name|long
name|getOrd
parameter_list|(
name|int
name|doc
parameter_list|)
block|{
return|return
name|readerOrds
operator|.
name|getOrd
argument_list|(
name|doc
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|compareBottom
specifier|public
name|int
name|compareBottom
parameter_list|(
name|int
name|doc
parameter_list|)
block|{
assert|assert
name|bottomSlot
operator|!=
operator|-
literal|1
assert|;
specifier|final
name|long
name|docOrd
init|=
name|getOrd
argument_list|(
name|doc
argument_list|)
decl_stmt|;
specifier|final
name|long
name|comparableOrd
init|=
name|docOrd
operator|==
name|Ordinals
operator|.
name|MISSING_ORDINAL
condition|?
name|missingOrd
else|:
name|docOrd
operator|<<
literal|2
decl_stmt|;
return|return
name|Long
operator|.
name|compare
argument_list|(
name|bottomOrd
argument_list|,
name|comparableOrd
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|compareTop
specifier|public
name|int
name|compareTop
parameter_list|(
name|int
name|doc
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|long
name|ord
init|=
name|getOrd
argument_list|(
name|doc
argument_list|)
decl_stmt|;
if|if
condition|(
name|ord
operator|==
name|Ordinals
operator|.
name|MISSING_ORDINAL
condition|)
block|{
return|return
name|compareTopMissing
argument_list|()
return|;
block|}
else|else
block|{
specifier|final
name|long
name|comparableOrd
init|=
name|ord
operator|<<
literal|2
decl_stmt|;
return|return
name|Long
operator|.
name|compare
argument_list|(
name|topOrd
argument_list|,
name|comparableOrd
argument_list|)
return|;
block|}
block|}
annotation|@
name|Override
DECL|method|compareBottomMissing
specifier|public
name|int
name|compareBottomMissing
parameter_list|()
block|{
assert|assert
name|bottomSlot
operator|!=
operator|-
literal|1
assert|;
return|return
name|Long
operator|.
name|compare
argument_list|(
name|bottomOrd
argument_list|,
name|missingOrd
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|compareTopMissing
specifier|public
name|int
name|compareTopMissing
parameter_list|()
block|{
name|int
name|cmp
init|=
name|Long
operator|.
name|compare
argument_list|(
name|topOrd
argument_list|,
name|missingOrd
argument_list|)
decl_stmt|;
if|if
condition|(
name|cmp
operator|==
literal|0
condition|)
block|{
return|return
name|compareValues
argument_list|(
name|top
argument_list|,
name|missingValue
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|cmp
return|;
block|}
block|}
annotation|@
name|Override
DECL|method|copy
specifier|public
name|void
name|copy
parameter_list|(
name|int
name|slot
parameter_list|,
name|int
name|doc
parameter_list|)
block|{
specifier|final
name|long
name|ord
init|=
name|getOrd
argument_list|(
name|doc
argument_list|)
decl_stmt|;
if|if
condition|(
name|ord
operator|==
name|Ordinals
operator|.
name|MISSING_ORDINAL
condition|)
block|{
name|ords
index|[
name|slot
index|]
operator|=
name|missingOrd
expr_stmt|;
name|values
index|[
name|slot
index|]
operator|=
name|missingValue
expr_stmt|;
block|}
else|else
block|{
assert|assert
name|ord
operator|>=
literal|0
assert|;
name|ords
index|[
name|slot
index|]
operator|=
name|ord
operator|<<
literal|2
expr_stmt|;
if|if
condition|(
name|values
index|[
name|slot
index|]
operator|==
literal|null
operator|||
name|values
index|[
name|slot
index|]
operator|==
name|missingValue
condition|)
block|{
name|values
index|[
name|slot
index|]
operator|=
operator|new
name|BytesRef
argument_list|()
expr_stmt|;
block|}
name|values
index|[
name|slot
index|]
operator|.
name|copyBytes
argument_list|(
name|termsIndex
operator|.
name|getValueByOrd
argument_list|(
name|ord
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|readerGen
index|[
name|slot
index|]
operator|=
name|currentReaderGen
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|missing
specifier|public
name|void
name|missing
parameter_list|(
name|int
name|slot
parameter_list|)
block|{
name|ords
index|[
name|slot
index|]
operator|=
name|missingOrd
expr_stmt|;
name|values
index|[
name|slot
index|]
operator|=
name|missingValue
expr_stmt|;
name|readerGen
index|[
name|slot
index|]
operator|=
name|currentReaderGen
expr_stmt|;
block|}
block|}
comment|// for assertions
DECL|method|consistentInsertedOrd
specifier|private
name|boolean
name|consistentInsertedOrd
parameter_list|(
name|BytesValues
operator|.
name|WithOrdinals
name|termsIndex
parameter_list|,
name|long
name|ord
parameter_list|,
name|BytesRef
name|value
parameter_list|)
block|{
specifier|final
name|long
name|previousOrd
init|=
name|ord
operator|>>
literal|2
decl_stmt|;
specifier|final
name|long
name|nextOrd
init|=
name|previousOrd
operator|+
literal|1
decl_stmt|;
specifier|final
name|BytesRef
name|previous
init|=
name|previousOrd
operator|==
name|Ordinals
operator|.
name|MISSING_ORDINAL
condition|?
literal|null
else|:
name|termsIndex
operator|.
name|getValueByOrd
argument_list|(
name|previousOrd
argument_list|)
decl_stmt|;
if|if
condition|(
operator|(
name|ord
operator|&
literal|3
operator|)
operator|==
literal|0
condition|)
block|{
comment|// there was an existing ord with the inserted value
assert|assert
name|compareValues
argument_list|(
name|previous
argument_list|,
name|value
argument_list|)
operator|==
literal|0
assert|;
block|}
else|else
block|{
assert|assert
name|compareValues
argument_list|(
name|previous
argument_list|,
name|value
argument_list|)
operator|<
literal|0
assert|;
block|}
if|if
condition|(
name|nextOrd
operator|<
name|termsIndex
operator|.
name|ordinals
argument_list|()
operator|.
name|getMaxOrd
argument_list|()
condition|)
block|{
specifier|final
name|BytesRef
name|next
init|=
name|termsIndex
operator|.
name|getValueByOrd
argument_list|(
name|nextOrd
argument_list|)
decl_stmt|;
assert|assert
name|compareValues
argument_list|(
name|value
argument_list|,
name|next
argument_list|)
operator|<
literal|0
assert|;
block|}
return|return
literal|true
return|;
block|}
comment|// find where to insert an ord in the current terms index
DECL|method|ordInCurrentReader
specifier|private
name|long
name|ordInCurrentReader
parameter_list|(
name|BytesValues
operator|.
name|WithOrdinals
name|termsIndex
parameter_list|,
name|BytesRef
name|value
parameter_list|)
block|{
specifier|final
name|long
name|ord
decl_stmt|;
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
name|ord
operator|=
name|Ordinals
operator|.
name|MISSING_ORDINAL
operator|<<
literal|2
expr_stmt|;
block|}
else|else
block|{
specifier|final
name|long
name|docOrd
init|=
name|binarySearch
argument_list|(
name|termsIndex
argument_list|,
name|value
argument_list|)
decl_stmt|;
if|if
condition|(
name|docOrd
operator|>=
name|Ordinals
operator|.
name|MIN_ORDINAL
condition|)
block|{
comment|// value exists in the current segment
name|ord
operator|=
name|docOrd
operator|<<
literal|2
expr_stmt|;
block|}
else|else
block|{
comment|// value doesn't exist, use the ord between the previous and the next term
name|ord
operator|=
operator|(
operator|(
operator|-
literal|2
operator|-
name|docOrd
operator|)
operator|<<
literal|2
operator|)
operator|+
literal|2
expr_stmt|;
block|}
block|}
assert|assert
operator|(
name|ord
operator|&
literal|1
operator|)
operator|==
literal|0
assert|;
return|return
name|ord
return|;
block|}
annotation|@
name|Override
DECL|method|setNextReader
specifier|public
name|FieldComparator
argument_list|<
name|BytesRef
argument_list|>
name|setNextReader
parameter_list|(
name|AtomicReaderContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|termsIndex
operator|=
name|indexFieldData
operator|.
name|load
argument_list|(
name|context
argument_list|)
operator|.
name|getBytesValues
argument_list|(
literal|false
argument_list|)
expr_stmt|;
assert|assert
name|termsIndex
operator|.
name|ordinals
argument_list|()
operator|!=
literal|null
assert|;
name|missingOrd
operator|=
name|ordInCurrentReader
argument_list|(
name|termsIndex
argument_list|,
name|missingValue
argument_list|)
expr_stmt|;
assert|assert
name|consistentInsertedOrd
argument_list|(
name|termsIndex
argument_list|,
name|missingOrd
argument_list|,
name|missingValue
argument_list|)
assert|;
name|FieldComparator
argument_list|<
name|BytesRef
argument_list|>
name|perSegComp
init|=
literal|null
decl_stmt|;
assert|assert
name|termsIndex
operator|.
name|ordinals
argument_list|()
operator|!=
literal|null
assert|;
if|if
condition|(
name|termsIndex
operator|.
name|isMultiValued
argument_list|()
condition|)
block|{
name|perSegComp
operator|=
operator|new
name|PerSegmentComparator
argument_list|(
name|termsIndex
argument_list|)
block|{
annotation|@
name|Override
specifier|protected
name|long
name|getOrd
parameter_list|(
name|int
name|doc
parameter_list|)
block|{
return|return
name|getRelevantOrd
argument_list|(
name|readerOrds
argument_list|,
name|doc
argument_list|,
name|sortMode
argument_list|)
return|;
block|}
block|}
expr_stmt|;
block|}
else|else
block|{
name|perSegComp
operator|=
operator|new
name|PerSegmentComparator
argument_list|(
name|termsIndex
argument_list|)
expr_stmt|;
block|}
name|currentReaderGen
operator|++
expr_stmt|;
if|if
condition|(
name|bottomSlot
operator|!=
operator|-
literal|1
condition|)
block|{
name|perSegComp
operator|.
name|setBottom
argument_list|(
name|bottomSlot
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|top
operator|!=
literal|null
condition|)
block|{
name|perSegComp
operator|.
name|setTopValue
argument_list|(
name|top
argument_list|)
expr_stmt|;
name|topOrd
operator|=
name|ordInCurrentReader
argument_list|(
name|termsIndex
argument_list|,
name|top
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|topOrd
operator|=
name|missingOrd
expr_stmt|;
block|}
return|return
name|perSegComp
return|;
block|}
annotation|@
name|Override
DECL|method|setBottom
specifier|public
name|void
name|setBottom
parameter_list|(
specifier|final
name|int
name|bottom
parameter_list|)
block|{
name|bottomSlot
operator|=
name|bottom
expr_stmt|;
specifier|final
name|BytesRef
name|bottomValue
init|=
name|values
index|[
name|bottomSlot
index|]
decl_stmt|;
if|if
condition|(
name|currentReaderGen
operator|==
name|readerGen
index|[
name|bottomSlot
index|]
condition|)
block|{
name|bottomOrd
operator|=
name|ords
index|[
name|bottomSlot
index|]
expr_stmt|;
block|}
else|else
block|{
comment|// insert an ord
name|bottomOrd
operator|=
name|ordInCurrentReader
argument_list|(
name|termsIndex
argument_list|,
name|bottomValue
argument_list|)
expr_stmt|;
if|if
condition|(
name|bottomOrd
operator|==
name|missingOrd
operator|&&
name|bottomValue
operator|!=
literal|null
condition|)
block|{
comment|// bottomValue and missingValue and in-between the same field data values -> tie-break
comment|// this is why we multiply ords by 4
assert|assert
name|missingValue
operator|!=
literal|null
assert|;
specifier|final
name|int
name|cmp
init|=
name|bottomValue
operator|.
name|compareTo
argument_list|(
name|missingValue
argument_list|)
decl_stmt|;
if|if
condition|(
name|cmp
operator|<
literal|0
condition|)
block|{
operator|--
name|bottomOrd
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|cmp
operator|>
literal|0
condition|)
block|{
operator|++
name|bottomOrd
expr_stmt|;
block|}
block|}
assert|assert
name|consistentInsertedOrd
argument_list|(
name|termsIndex
argument_list|,
name|bottomOrd
argument_list|,
name|bottomValue
argument_list|)
assert|;
block|}
block|}
annotation|@
name|Override
DECL|method|setTopValue
specifier|public
name|void
name|setTopValue
parameter_list|(
name|BytesRef
name|value
parameter_list|)
block|{
name|this
operator|.
name|top
operator|=
name|value
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|value
specifier|public
name|BytesRef
name|value
parameter_list|(
name|int
name|slot
parameter_list|)
block|{
return|return
name|values
index|[
name|slot
index|]
return|;
block|}
DECL|method|binarySearch
specifier|final
specifier|protected
specifier|static
name|long
name|binarySearch
parameter_list|(
name|BytesValues
operator|.
name|WithOrdinals
name|a
parameter_list|,
name|BytesRef
name|key
parameter_list|)
block|{
return|return
name|binarySearch
argument_list|(
name|a
argument_list|,
name|key
argument_list|,
name|Ordinals
operator|.
name|MIN_ORDINAL
argument_list|,
name|a
operator|.
name|ordinals
argument_list|()
operator|.
name|getMaxOrd
argument_list|()
operator|-
literal|1
argument_list|)
return|;
block|}
DECL|method|binarySearch
specifier|final
specifier|protected
specifier|static
name|long
name|binarySearch
parameter_list|(
name|BytesValues
operator|.
name|WithOrdinals
name|a
parameter_list|,
name|BytesRef
name|key
parameter_list|,
name|long
name|low
parameter_list|,
name|long
name|high
parameter_list|)
block|{
assert|assert
name|low
operator|!=
name|Ordinals
operator|.
name|MISSING_ORDINAL
assert|;
assert|assert
name|high
operator|==
name|Ordinals
operator|.
name|MISSING_ORDINAL
operator|||
operator|(
name|a
operator|.
name|getValueByOrd
argument_list|(
name|high
argument_list|)
operator|==
literal|null
operator||
name|a
operator|.
name|getValueByOrd
argument_list|(
name|high
argument_list|)
operator|!=
literal|null
operator|)
assert|;
comment|// make sure we actually can get these values
assert|assert
name|low
operator|==
name|high
operator|+
literal|1
operator|||
name|a
operator|.
name|getValueByOrd
argument_list|(
name|low
argument_list|)
operator|==
literal|null
operator||
name|a
operator|.
name|getValueByOrd
argument_list|(
name|low
argument_list|)
operator|!=
literal|null
assert|;
while|while
condition|(
name|low
operator|<=
name|high
condition|)
block|{
name|long
name|mid
init|=
operator|(
name|low
operator|+
name|high
operator|)
operator|>>>
literal|1
decl_stmt|;
name|BytesRef
name|midVal
init|=
name|a
operator|.
name|getValueByOrd
argument_list|(
name|mid
argument_list|)
decl_stmt|;
name|int
name|cmp
decl_stmt|;
if|if
condition|(
name|midVal
operator|!=
literal|null
condition|)
block|{
name|cmp
operator|=
name|midVal
operator|.
name|compareTo
argument_list|(
name|key
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|cmp
operator|=
operator|-
literal|1
expr_stmt|;
block|}
if|if
condition|(
name|cmp
operator|<
literal|0
condition|)
name|low
operator|=
name|mid
operator|+
literal|1
expr_stmt|;
elseif|else
if|if
condition|(
name|cmp
operator|>
literal|0
condition|)
name|high
operator|=
name|mid
operator|-
literal|1
expr_stmt|;
else|else
return|return
name|mid
return|;
block|}
return|return
operator|-
operator|(
name|low
operator|+
literal|1
operator|)
return|;
block|}
DECL|method|getRelevantOrd
specifier|static
name|long
name|getRelevantOrd
parameter_list|(
name|Ordinals
operator|.
name|Docs
name|readerOrds
parameter_list|,
name|int
name|docId
parameter_list|,
name|MultiValueMode
name|sortMode
parameter_list|)
block|{
name|int
name|length
init|=
name|readerOrds
operator|.
name|setDocument
argument_list|(
name|docId
argument_list|)
decl_stmt|;
name|long
name|relevantVal
init|=
name|sortMode
operator|.
name|startLong
argument_list|()
decl_stmt|;
name|long
name|result
init|=
name|Ordinals
operator|.
name|MISSING_ORDINAL
decl_stmt|;
assert|assert
name|sortMode
operator|==
name|MultiValueMode
operator|.
name|MAX
operator|||
name|sortMode
operator|==
name|MultiValueMode
operator|.
name|MIN
assert|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|length
condition|;
name|i
operator|++
control|)
block|{
name|result
operator|=
name|relevantVal
operator|=
name|sortMode
operator|.
name|apply
argument_list|(
name|readerOrds
operator|.
name|nextOrd
argument_list|()
argument_list|,
name|relevantVal
argument_list|)
expr_stmt|;
block|}
assert|assert
name|result
operator|>=
name|Ordinals
operator|.
name|MISSING_ORDINAL
assert|;
assert|assert
name|result
operator|<
name|readerOrds
operator|.
name|getMaxOrd
argument_list|()
assert|;
return|return
name|result
return|;
comment|// Enable this when the api can tell us that the ords per doc are ordered
comment|/*if (reversed) {             IntArrayRef ref = readerOrds.getOrds(docId);             if (ref.isEmpty()) {                 return 0;             } else {                 return ref.values[ref.end - 1]; // last element is the highest value.             }         } else {             return readerOrds.getOrd(docId); // returns the lowest value         }*/
block|}
block|}
end_class

end_unit

