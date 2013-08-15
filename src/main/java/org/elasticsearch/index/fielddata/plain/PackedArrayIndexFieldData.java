begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.fielddata.plain
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|fielddata
operator|.
name|plain
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
name|Preconditions
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
name|AtomicReader
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
name|packed
operator|.
name|MonotonicAppendingLongBuffer
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
name|Index
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
name|*
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
name|fieldcomparator
operator|.
name|LongValuesComparatorSource
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
name|fieldcomparator
operator|.
name|SortMode
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
name|index
operator|.
name|fielddata
operator|.
name|ordinals
operator|.
name|Ordinals
operator|.
name|Docs
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
name|OrdinalsBuilder
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
name|mapper
operator|.
name|FieldMapper
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
name|settings
operator|.
name|IndexSettings
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|EnumSet
import|;
end_import

begin_comment
comment|/**  * Stores numeric data into bit-packed arrays for better memory efficiency.  */
end_comment

begin_class
DECL|class|PackedArrayIndexFieldData
specifier|public
class|class
name|PackedArrayIndexFieldData
extends|extends
name|AbstractIndexFieldData
argument_list|<
name|AtomicNumericFieldData
argument_list|>
implements|implements
name|IndexNumericFieldData
argument_list|<
name|AtomicNumericFieldData
argument_list|>
block|{
DECL|class|Builder
specifier|public
specifier|static
class|class
name|Builder
implements|implements
name|IndexFieldData
operator|.
name|Builder
block|{
DECL|field|numericType
specifier|private
name|NumericType
name|numericType
decl_stmt|;
DECL|method|setNumericType
specifier|public
name|Builder
name|setNumericType
parameter_list|(
name|NumericType
name|numericType
parameter_list|)
block|{
name|this
operator|.
name|numericType
operator|=
name|numericType
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
DECL|method|build
specifier|public
name|IndexFieldData
argument_list|<
name|AtomicNumericFieldData
argument_list|>
name|build
parameter_list|(
name|Index
name|index
parameter_list|,
annotation|@
name|IndexSettings
name|Settings
name|indexSettings
parameter_list|,
name|FieldMapper
operator|.
name|Names
name|fieldNames
parameter_list|,
name|FieldDataType
name|type
parameter_list|,
name|IndexFieldDataCache
name|cache
parameter_list|)
block|{
return|return
operator|new
name|PackedArrayIndexFieldData
argument_list|(
name|index
argument_list|,
name|indexSettings
argument_list|,
name|fieldNames
argument_list|,
name|type
argument_list|,
name|cache
argument_list|,
name|numericType
argument_list|)
return|;
block|}
block|}
DECL|field|numericType
specifier|private
specifier|final
name|NumericType
name|numericType
decl_stmt|;
DECL|method|PackedArrayIndexFieldData
specifier|public
name|PackedArrayIndexFieldData
parameter_list|(
name|Index
name|index
parameter_list|,
annotation|@
name|IndexSettings
name|Settings
name|indexSettings
parameter_list|,
name|FieldMapper
operator|.
name|Names
name|fieldNames
parameter_list|,
name|FieldDataType
name|fieldDataType
parameter_list|,
name|IndexFieldDataCache
name|cache
parameter_list|,
name|NumericType
name|numericType
parameter_list|)
block|{
name|super
argument_list|(
name|index
argument_list|,
name|indexSettings
argument_list|,
name|fieldNames
argument_list|,
name|fieldDataType
argument_list|,
name|cache
argument_list|)
expr_stmt|;
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|numericType
argument_list|)
expr_stmt|;
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|EnumSet
operator|.
name|of
argument_list|(
name|NumericType
operator|.
name|BYTE
argument_list|,
name|NumericType
operator|.
name|SHORT
argument_list|,
name|NumericType
operator|.
name|INT
argument_list|,
name|NumericType
operator|.
name|LONG
argument_list|)
operator|.
name|contains
argument_list|(
name|numericType
argument_list|)
argument_list|,
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|" only supports integer types, not "
operator|+
name|numericType
argument_list|)
expr_stmt|;
name|this
operator|.
name|numericType
operator|=
name|numericType
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getNumericType
specifier|public
name|NumericType
name|getNumericType
parameter_list|()
block|{
return|return
name|numericType
return|;
block|}
annotation|@
name|Override
DECL|method|valuesOrdered
specifier|public
name|boolean
name|valuesOrdered
parameter_list|()
block|{
comment|// because we might have single values? we can dynamically update a flag to reflect that
comment|// based on the atomic field data loaded
return|return
literal|false
return|;
block|}
annotation|@
name|Override
DECL|method|loadDirect
specifier|public
name|AtomicNumericFieldData
name|loadDirect
parameter_list|(
name|AtomicReaderContext
name|context
parameter_list|)
throws|throws
name|Exception
block|{
name|AtomicReader
name|reader
init|=
name|context
operator|.
name|reader
argument_list|()
decl_stmt|;
name|Terms
name|terms
init|=
name|reader
operator|.
name|terms
argument_list|(
name|getFieldNames
argument_list|()
operator|.
name|indexName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|terms
operator|==
literal|null
condition|)
block|{
return|return
name|PackedArrayAtomicFieldData
operator|.
name|empty
argument_list|(
name|reader
operator|.
name|maxDoc
argument_list|()
argument_list|)
return|;
block|}
comment|// TODO: how can we guess the number of terms? numerics end up creating more terms per value...
comment|// Lucene encodes numeric data so that the lexicographical (encoded) order matches the integer order so we know the sequence of
comment|// longs is going to be monotonically increasing
specifier|final
name|MonotonicAppendingLongBuffer
name|values
init|=
operator|new
name|MonotonicAppendingLongBuffer
argument_list|()
decl_stmt|;
specifier|final
name|float
name|acceptableTransientOverheadRatio
init|=
name|fieldDataType
operator|.
name|getSettings
argument_list|()
operator|.
name|getAsFloat
argument_list|(
literal|"acceptable_transient_overhead_ratio"
argument_list|,
name|OrdinalsBuilder
operator|.
name|DEFAULT_ACCEPTABLE_OVERHEAD_RATIO
argument_list|)
decl_stmt|;
name|OrdinalsBuilder
name|builder
init|=
operator|new
name|OrdinalsBuilder
argument_list|(
operator|-
literal|1
argument_list|,
name|reader
operator|.
name|maxDoc
argument_list|()
argument_list|,
name|acceptableTransientOverheadRatio
argument_list|)
decl_stmt|;
try|try
block|{
name|BytesRefIterator
name|iter
init|=
name|builder
operator|.
name|buildFromTerms
argument_list|(
name|getNumericType
argument_list|()
operator|.
name|wrapTermsEnum
argument_list|(
name|terms
operator|.
name|iterator
argument_list|(
literal|null
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|BytesRef
name|term
decl_stmt|;
assert|assert
operator|!
name|getNumericType
argument_list|()
operator|.
name|isFloatingPoint
argument_list|()
assert|;
specifier|final
name|boolean
name|indexedAsLong
init|=
name|getNumericType
argument_list|()
operator|.
name|requiredBits
argument_list|()
operator|>
literal|32
decl_stmt|;
while|while
condition|(
operator|(
name|term
operator|=
name|iter
operator|.
name|next
argument_list|()
operator|)
operator|!=
literal|null
condition|)
block|{
specifier|final
name|long
name|value
init|=
name|indexedAsLong
condition|?
name|NumericUtils
operator|.
name|prefixCodedToLong
argument_list|(
name|term
argument_list|)
else|:
name|NumericUtils
operator|.
name|prefixCodedToInt
argument_list|(
name|term
argument_list|)
decl_stmt|;
assert|assert
name|values
operator|.
name|size
argument_list|()
operator|==
literal|0
operator|||
name|value
operator|>
name|values
operator|.
name|get
argument_list|(
name|values
operator|.
name|size
argument_list|()
operator|-
literal|1
argument_list|)
assert|;
name|values
operator|.
name|add
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
name|Ordinals
name|build
init|=
name|builder
operator|.
name|build
argument_list|(
name|fieldDataType
operator|.
name|getSettings
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|build
operator|.
name|isMultiValued
argument_list|()
operator|&&
name|CommonSettings
operator|.
name|removeOrdsOnSingleValue
argument_list|(
name|fieldDataType
argument_list|)
condition|)
block|{
name|Docs
name|ordinals
init|=
name|build
operator|.
name|ordinals
argument_list|()
decl_stmt|;
specifier|final
name|FixedBitSet
name|set
init|=
name|builder
operator|.
name|buildDocsWithValuesSet
argument_list|()
decl_stmt|;
name|long
name|minValue
decl_stmt|,
name|maxValue
decl_stmt|;
name|minValue
operator|=
name|maxValue
operator|=
literal|0
expr_stmt|;
if|if
condition|(
name|values
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|minValue
operator|=
name|values
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|maxValue
operator|=
name|values
operator|.
name|get
argument_list|(
name|values
operator|.
name|size
argument_list|()
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
comment|// Encode document without a value with a special value
name|long
name|missingValue
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|set
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
operator|(
name|maxValue
operator|-
name|minValue
operator|+
literal|1
operator|)
operator|==
name|values
operator|.
name|size
argument_list|()
condition|)
block|{
comment|// values are dense
if|if
condition|(
name|minValue
operator|>
name|Long
operator|.
name|MIN_VALUE
condition|)
block|{
name|missingValue
operator|=
operator|--
name|minValue
expr_stmt|;
block|}
else|else
block|{
assert|assert
name|maxValue
operator|!=
name|Long
operator|.
name|MAX_VALUE
assert|;
name|missingValue
operator|=
operator|++
name|maxValue
expr_stmt|;
block|}
block|}
else|else
block|{
for|for
control|(
name|long
name|i
init|=
literal|1
init|;
name|i
operator|<
name|values
operator|.
name|size
argument_list|()
condition|;
operator|++
name|i
control|)
block|{
if|if
condition|(
name|values
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|>
name|values
operator|.
name|get
argument_list|(
name|i
operator|-
literal|1
argument_list|)
operator|+
literal|1
condition|)
block|{
name|missingValue
operator|=
name|values
operator|.
name|get
argument_list|(
name|i
operator|-
literal|1
argument_list|)
operator|+
literal|1
expr_stmt|;
break|break;
block|}
block|}
block|}
name|missingValue
operator|-=
name|minValue
expr_stmt|;
comment|// delta
block|}
specifier|final
name|long
name|delta
init|=
name|maxValue
operator|-
name|minValue
decl_stmt|;
specifier|final
name|int
name|bitsRequired
init|=
name|delta
operator|<
literal|0
condition|?
literal|64
else|:
name|PackedInts
operator|.
name|bitsRequired
argument_list|(
name|delta
argument_list|)
decl_stmt|;
specifier|final
name|float
name|acceptableOverheadRatio
init|=
name|fieldDataType
operator|.
name|getSettings
argument_list|()
operator|.
name|getAsFloat
argument_list|(
literal|"acceptable_overhead_ratio"
argument_list|,
name|PackedInts
operator|.
name|DEFAULT
argument_list|)
decl_stmt|;
specifier|final
name|PackedInts
operator|.
name|FormatAndBits
name|formatAndBits
init|=
name|PackedInts
operator|.
name|fastestFormatAndBits
argument_list|(
name|reader
operator|.
name|maxDoc
argument_list|()
argument_list|,
name|bitsRequired
argument_list|,
name|acceptableOverheadRatio
argument_list|)
decl_stmt|;
comment|// there's sweet spot where due to low unique value count, using ordinals will consume less memory
specifier|final
name|long
name|singleValuesSize
init|=
name|formatAndBits
operator|.
name|format
operator|.
name|longCount
argument_list|(
name|PackedInts
operator|.
name|VERSION_CURRENT
argument_list|,
name|reader
operator|.
name|maxDoc
argument_list|()
argument_list|,
name|formatAndBits
operator|.
name|bitsPerValue
argument_list|)
operator|*
literal|8L
decl_stmt|;
specifier|final
name|long
name|uniqueValuesSize
init|=
name|values
operator|.
name|ramBytesUsed
argument_list|()
decl_stmt|;
specifier|final
name|long
name|ordinalsSize
init|=
name|build
operator|.
name|getMemorySizeInBytes
argument_list|()
decl_stmt|;
if|if
condition|(
name|uniqueValuesSize
operator|+
name|ordinalsSize
operator|<
name|singleValuesSize
condition|)
block|{
return|return
operator|new
name|PackedArrayAtomicFieldData
operator|.
name|WithOrdinals
argument_list|(
name|values
argument_list|,
name|reader
operator|.
name|maxDoc
argument_list|()
argument_list|,
name|build
argument_list|)
return|;
block|}
specifier|final
name|PackedInts
operator|.
name|Mutable
name|sValues
init|=
name|PackedInts
operator|.
name|getMutable
argument_list|(
name|reader
operator|.
name|maxDoc
argument_list|()
argument_list|,
name|bitsRequired
argument_list|,
name|acceptableOverheadRatio
argument_list|)
decl_stmt|;
if|if
condition|(
name|missingValue
operator|!=
literal|0
condition|)
block|{
name|sValues
operator|.
name|fill
argument_list|(
literal|0
argument_list|,
name|sValues
operator|.
name|size
argument_list|()
argument_list|,
name|missingValue
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|reader
operator|.
name|maxDoc
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
specifier|final
name|long
name|ord
init|=
name|ordinals
operator|.
name|getOrd
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|ord
operator|>
literal|0
condition|)
block|{
name|sValues
operator|.
name|set
argument_list|(
name|i
argument_list|,
name|values
operator|.
name|get
argument_list|(
name|ord
operator|-
literal|1
argument_list|)
operator|-
name|minValue
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|set
operator|==
literal|null
condition|)
block|{
return|return
operator|new
name|PackedArrayAtomicFieldData
operator|.
name|Single
argument_list|(
name|sValues
argument_list|,
name|minValue
argument_list|,
name|reader
operator|.
name|maxDoc
argument_list|()
argument_list|,
name|ordinals
operator|.
name|getNumOrds
argument_list|()
argument_list|)
return|;
block|}
else|else
block|{
return|return
operator|new
name|PackedArrayAtomicFieldData
operator|.
name|SingleSparse
argument_list|(
name|sValues
argument_list|,
name|minValue
argument_list|,
name|reader
operator|.
name|maxDoc
argument_list|()
argument_list|,
name|missingValue
argument_list|,
name|ordinals
operator|.
name|getNumOrds
argument_list|()
argument_list|)
return|;
block|}
block|}
else|else
block|{
return|return
operator|new
name|PackedArrayAtomicFieldData
operator|.
name|WithOrdinals
argument_list|(
name|values
argument_list|,
name|reader
operator|.
name|maxDoc
argument_list|()
argument_list|,
name|build
argument_list|)
return|;
block|}
block|}
finally|finally
block|{
name|builder
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|comparatorSource
specifier|public
name|XFieldComparatorSource
name|comparatorSource
parameter_list|(
annotation|@
name|Nullable
name|Object
name|missingValue
parameter_list|,
name|SortMode
name|sortMode
parameter_list|)
block|{
return|return
operator|new
name|LongValuesComparatorSource
argument_list|(
name|this
argument_list|,
name|missingValue
argument_list|,
name|sortMode
argument_list|)
return|;
block|}
block|}
end_class

end_unit

