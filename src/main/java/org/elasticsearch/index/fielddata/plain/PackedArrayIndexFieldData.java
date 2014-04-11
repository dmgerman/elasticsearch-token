begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|AppendingDeltaPackedLongBuffer
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
name|ElasticsearchException
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
name|breaker
operator|.
name|MemoryCircuitBreaker
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
name|GlobalOrdinalsBuilder
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
name|mapper
operator|.
name|MapperService
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
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|fielddata
operator|.
name|breaker
operator|.
name|CircuitBreakerService
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
argument_list|<
name|?
argument_list|>
name|mapper
parameter_list|,
name|IndexFieldDataCache
name|cache
parameter_list|,
name|CircuitBreakerService
name|breakerService
parameter_list|,
name|MapperService
name|mapperService
parameter_list|,
name|GlobalOrdinalsBuilder
name|globalOrdinalBuilder
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
name|mapper
operator|.
name|names
argument_list|()
argument_list|,
name|mapper
operator|.
name|fieldDataType
argument_list|()
argument_list|,
name|cache
argument_list|,
name|numericType
argument_list|,
name|breakerService
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
DECL|field|breakerService
specifier|private
specifier|final
name|CircuitBreakerService
name|breakerService
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
parameter_list|,
name|CircuitBreakerService
name|breakerService
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
name|this
operator|.
name|breakerService
operator|=
name|breakerService
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
name|PackedArrayAtomicFieldData
name|data
init|=
literal|null
decl_stmt|;
name|PackedArrayEstimator
name|estimator
init|=
operator|new
name|PackedArrayEstimator
argument_list|(
name|breakerService
operator|.
name|getBreaker
argument_list|()
argument_list|,
name|getNumericType
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
name|data
operator|=
name|PackedArrayAtomicFieldData
operator|.
name|empty
argument_list|(
name|reader
operator|.
name|maxDoc
argument_list|()
argument_list|)
expr_stmt|;
name|estimator
operator|.
name|adjustForNoTerms
argument_list|(
name|data
operator|.
name|getMemorySizeInBytes
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|data
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
name|TermsEnum
name|termsEnum
init|=
name|estimator
operator|.
name|beforeLoad
argument_list|(
name|terms
argument_list|)
decl_stmt|;
name|boolean
name|success
init|=
literal|false
decl_stmt|;
try|try
init|(
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
init|)
block|{
name|BytesRefIterator
name|iter
init|=
name|builder
operator|.
name|buildFromTerms
argument_list|(
name|termsEnum
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
name|CommonSettings
operator|.
name|MemoryStorageFormat
name|formatHint
init|=
name|CommonSettings
operator|.
name|getMemoryStorageHint
argument_list|(
name|fieldDataType
argument_list|)
decl_stmt|;
if|if
condition|(
name|build
operator|.
name|isMultiValued
argument_list|()
operator|||
name|formatHint
operator|==
name|CommonSettings
operator|.
name|MemoryStorageFormat
operator|.
name|ORDINALS
condition|)
block|{
name|data
operator|=
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
expr_stmt|;
block|}
else|else
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
name|docsWithValues
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
name|int
name|pageSize
init|=
name|fieldDataType
operator|.
name|getSettings
argument_list|()
operator|.
name|getAsInt
argument_list|(
literal|"single_value_page_size"
argument_list|,
literal|1024
argument_list|)
decl_stmt|;
if|if
condition|(
name|formatHint
operator|==
literal|null
condition|)
block|{
name|formatHint
operator|=
name|chooseStorageFormat
argument_list|(
name|reader
argument_list|,
name|values
argument_list|,
name|build
argument_list|,
name|ordinals
argument_list|,
name|minValue
argument_list|,
name|maxValue
argument_list|,
name|acceptableOverheadRatio
argument_list|,
name|pageSize
argument_list|)
expr_stmt|;
block|}
name|logger
operator|.
name|trace
argument_list|(
literal|"single value format for field [{}] set to [{}]"
argument_list|,
name|getFieldNames
argument_list|()
operator|.
name|fullName
argument_list|()
argument_list|,
name|formatHint
argument_list|)
expr_stmt|;
switch|switch
condition|(
name|formatHint
condition|)
block|{
case|case
name|PACKED
case|:
comment|// Encode document without a value with a special value
name|long
name|missingValue
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|docsWithValues
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
block|}
specifier|final
name|long
name|valuesDelta
init|=
name|maxValue
operator|-
name|minValue
decl_stmt|;
name|int
name|bitsRequired
init|=
name|valuesDelta
operator|<
literal|0
condition|?
literal|64
else|:
name|PackedInts
operator|.
name|bitsRequired
argument_list|(
name|valuesDelta
argument_list|)
decl_stmt|;
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
name|docsWithValues
operator|!=
literal|null
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
operator|!=
name|Ordinals
operator|.
name|MISSING_ORDINAL
condition|)
block|{
name|long
name|value
init|=
name|values
operator|.
name|get
argument_list|(
name|ord
operator|-
literal|1
argument_list|)
decl_stmt|;
name|sValues
operator|.
name|set
argument_list|(
name|i
argument_list|,
name|value
operator|-
name|minValue
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|docsWithValues
operator|==
literal|null
condition|)
block|{
name|data
operator|=
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
expr_stmt|;
block|}
else|else
block|{
name|data
operator|=
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
expr_stmt|;
block|}
break|break;
case|case
name|PAGED
case|:
specifier|final
name|AppendingDeltaPackedLongBuffer
name|dpValues
init|=
operator|new
name|AppendingDeltaPackedLongBuffer
argument_list|(
name|reader
operator|.
name|maxDoc
argument_list|()
operator|/
name|pageSize
operator|+
literal|1
argument_list|,
name|pageSize
argument_list|,
name|acceptableOverheadRatio
argument_list|)
decl_stmt|;
name|long
name|lastValue
init|=
literal|0
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
operator|!=
name|Ordinals
operator|.
name|MISSING_ORDINAL
condition|)
block|{
name|lastValue
operator|=
name|values
operator|.
name|get
argument_list|(
name|ord
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
name|dpValues
operator|.
name|add
argument_list|(
name|lastValue
argument_list|)
expr_stmt|;
block|}
name|dpValues
operator|.
name|freeze
argument_list|()
expr_stmt|;
if|if
condition|(
name|docsWithValues
operator|==
literal|null
condition|)
block|{
name|data
operator|=
operator|new
name|PackedArrayAtomicFieldData
operator|.
name|PagedSingle
argument_list|(
name|dpValues
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
expr_stmt|;
block|}
else|else
block|{
name|data
operator|=
operator|new
name|PackedArrayAtomicFieldData
operator|.
name|PagedSingleSparse
argument_list|(
name|dpValues
argument_list|,
name|reader
operator|.
name|maxDoc
argument_list|()
argument_list|,
name|docsWithValues
argument_list|,
name|ordinals
operator|.
name|getNumOrds
argument_list|()
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
name|ORDINALS
case|:
name|data
operator|=
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
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|ElasticsearchException
argument_list|(
literal|"unknown memory format: "
operator|+
name|formatHint
argument_list|)
throw|;
block|}
block|}
name|success
operator|=
literal|true
expr_stmt|;
return|return
name|data
return|;
block|}
finally|finally
block|{
if|if
condition|(
operator|!
name|success
condition|)
block|{
comment|// If something went wrong, unwind any current estimations we've made
name|estimator
operator|.
name|afterLoad
argument_list|(
name|termsEnum
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// Adjust as usual, based on the actual size of the field data
name|estimator
operator|.
name|afterLoad
argument_list|(
name|termsEnum
argument_list|,
name|data
operator|.
name|getMemorySizeInBytes
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|chooseStorageFormat
specifier|protected
name|CommonSettings
operator|.
name|MemoryStorageFormat
name|chooseStorageFormat
parameter_list|(
name|AtomicReader
name|reader
parameter_list|,
name|MonotonicAppendingLongBuffer
name|values
parameter_list|,
name|Ordinals
name|build
parameter_list|,
name|Docs
name|ordinals
parameter_list|,
name|long
name|minValue
parameter_list|,
name|long
name|maxValue
parameter_list|,
name|float
name|acceptableOverheadRatio
parameter_list|,
name|int
name|pageSize
parameter_list|)
block|{
name|CommonSettings
operator|.
name|MemoryStorageFormat
name|format
decl_stmt|;
comment|// estimate memory usage for a single packed array
name|long
name|packedDelta
init|=
name|maxValue
operator|-
name|minValue
operator|+
literal|1
decl_stmt|;
comment|// allow for a missing value
comment|// valuesDelta can be negative if the difference between max and min values overflows the positive side of longs.
name|int
name|bitsRequired
init|=
name|packedDelta
operator|<
literal|0
condition|?
literal|64
else|:
name|PackedInts
operator|.
name|bitsRequired
argument_list|(
name|packedDelta
argument_list|)
decl_stmt|;
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
comment|// ordinal memory usage
specifier|final
name|long
name|ordinalsSize
init|=
name|build
operator|.
name|getMemorySizeInBytes
argument_list|()
operator|+
name|values
operator|.
name|ramBytesUsed
argument_list|()
decl_stmt|;
comment|// estimate the memory signature of paged packing
name|long
name|pagedSingleValuesSize
init|=
operator|(
name|reader
operator|.
name|maxDoc
argument_list|()
operator|/
name|pageSize
operator|+
literal|1
operator|)
operator|*
name|RamUsageEstimator
operator|.
name|NUM_BYTES_OBJECT_REF
decl_stmt|;
comment|// array of pages
name|int
name|pageIndex
init|=
literal|0
decl_stmt|;
name|long
name|pageMinOrdinal
init|=
name|Long
operator|.
name|MAX_VALUE
decl_stmt|;
name|long
name|pageMaxOrdinal
init|=
name|Long
operator|.
name|MIN_VALUE
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|reader
operator|.
name|maxDoc
argument_list|()
condition|;
operator|++
name|i
operator|,
name|pageIndex
operator|=
operator|(
name|pageIndex
operator|+
literal|1
operator|)
operator|%
name|pageSize
control|)
block|{
name|long
name|ordinal
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
name|ordinal
operator|!=
name|Ordinals
operator|.
name|MISSING_ORDINAL
condition|)
block|{
name|pageMaxOrdinal
operator|=
name|Math
operator|.
name|max
argument_list|(
name|ordinal
argument_list|,
name|pageMaxOrdinal
argument_list|)
expr_stmt|;
name|pageMinOrdinal
operator|=
name|Math
operator|.
name|min
argument_list|(
name|ordinal
argument_list|,
name|pageMinOrdinal
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|pageIndex
operator|==
name|pageSize
operator|-
literal|1
condition|)
block|{
comment|// end of page, we now know enough to estimate memory usage
name|pagedSingleValuesSize
operator|+=
name|getPageMemoryUsage
argument_list|(
name|values
argument_list|,
name|acceptableOverheadRatio
argument_list|,
name|pageSize
argument_list|,
name|pageMinOrdinal
argument_list|,
name|pageMaxOrdinal
argument_list|)
expr_stmt|;
name|pageMinOrdinal
operator|=
name|Long
operator|.
name|MAX_VALUE
expr_stmt|;
name|pageMaxOrdinal
operator|=
name|Long
operator|.
name|MIN_VALUE
expr_stmt|;
block|}
block|}
if|if
condition|(
name|pageIndex
operator|>
literal|0
condition|)
block|{
comment|// last page estimation
name|pageIndex
operator|++
expr_stmt|;
name|pagedSingleValuesSize
operator|+=
name|getPageMemoryUsage
argument_list|(
name|values
argument_list|,
name|acceptableOverheadRatio
argument_list|,
name|pageSize
argument_list|,
name|pageMinOrdinal
argument_list|,
name|pageMaxOrdinal
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|ordinalsSize
operator|<
name|singleValuesSize
condition|)
block|{
if|if
condition|(
name|ordinalsSize
operator|<
name|pagedSingleValuesSize
condition|)
block|{
name|format
operator|=
name|CommonSettings
operator|.
name|MemoryStorageFormat
operator|.
name|ORDINALS
expr_stmt|;
block|}
else|else
block|{
name|format
operator|=
name|CommonSettings
operator|.
name|MemoryStorageFormat
operator|.
name|PAGED
expr_stmt|;
block|}
block|}
else|else
block|{
if|if
condition|(
name|pagedSingleValuesSize
operator|<
name|singleValuesSize
condition|)
block|{
name|format
operator|=
name|CommonSettings
operator|.
name|MemoryStorageFormat
operator|.
name|PAGED
expr_stmt|;
block|}
else|else
block|{
name|format
operator|=
name|CommonSettings
operator|.
name|MemoryStorageFormat
operator|.
name|PACKED
expr_stmt|;
block|}
block|}
return|return
name|format
return|;
block|}
DECL|method|getPageMemoryUsage
specifier|private
name|long
name|getPageMemoryUsage
parameter_list|(
name|MonotonicAppendingLongBuffer
name|values
parameter_list|,
name|float
name|acceptableOverheadRatio
parameter_list|,
name|int
name|pageSize
parameter_list|,
name|long
name|pageMinOrdinal
parameter_list|,
name|long
name|pageMaxOrdinal
parameter_list|)
block|{
name|int
name|bitsRequired
decl_stmt|;
name|long
name|pageMemorySize
init|=
literal|0
decl_stmt|;
name|PackedInts
operator|.
name|FormatAndBits
name|formatAndBits
decl_stmt|;
if|if
condition|(
name|pageMaxOrdinal
operator|==
name|Long
operator|.
name|MIN_VALUE
condition|)
block|{
comment|// empty page - will use the null reader which just stores size
name|pageMemorySize
operator|+=
name|RamUsageEstimator
operator|.
name|alignObjectSize
argument_list|(
name|RamUsageEstimator
operator|.
name|NUM_BYTES_OBJECT_HEADER
operator|+
name|RamUsageEstimator
operator|.
name|NUM_BYTES_INT
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|long
name|pageMinValue
init|=
name|values
operator|.
name|get
argument_list|(
name|pageMinOrdinal
operator|-
literal|1
argument_list|)
decl_stmt|;
name|long
name|pageMaxValue
init|=
name|values
operator|.
name|get
argument_list|(
name|pageMaxOrdinal
operator|-
literal|1
argument_list|)
decl_stmt|;
name|long
name|pageDelta
init|=
name|pageMaxValue
operator|-
name|pageMinValue
decl_stmt|;
if|if
condition|(
name|pageDelta
operator|!=
literal|0
condition|)
block|{
name|bitsRequired
operator|=
name|pageDelta
operator|<
literal|0
condition|?
literal|64
else|:
name|PackedInts
operator|.
name|bitsRequired
argument_list|(
name|pageDelta
argument_list|)
expr_stmt|;
name|formatAndBits
operator|=
name|PackedInts
operator|.
name|fastestFormatAndBits
argument_list|(
name|pageSize
argument_list|,
name|bitsRequired
argument_list|,
name|acceptableOverheadRatio
argument_list|)
expr_stmt|;
name|pageMemorySize
operator|+=
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
name|pageSize
argument_list|,
name|formatAndBits
operator|.
name|bitsPerValue
argument_list|)
operator|*
name|RamUsageEstimator
operator|.
name|NUM_BYTES_LONG
expr_stmt|;
name|pageMemorySize
operator|+=
name|RamUsageEstimator
operator|.
name|NUM_BYTES_LONG
expr_stmt|;
comment|// min value per page storage
block|}
else|else
block|{
comment|// empty page
name|pageMemorySize
operator|+=
name|RamUsageEstimator
operator|.
name|alignObjectSize
argument_list|(
name|RamUsageEstimator
operator|.
name|NUM_BYTES_OBJECT_HEADER
operator|+
name|RamUsageEstimator
operator|.
name|NUM_BYTES_INT
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|pageMemorySize
return|;
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
comment|/**      * Estimator that wraps numeric field data loading in a      * RamAccountingTermsEnum, adjusting the breaker after data has been      * loaded      */
DECL|class|PackedArrayEstimator
specifier|public
class|class
name|PackedArrayEstimator
implements|implements
name|PerValueEstimator
block|{
DECL|field|breaker
specifier|private
specifier|final
name|MemoryCircuitBreaker
name|breaker
decl_stmt|;
DECL|field|type
specifier|private
specifier|final
name|NumericType
name|type
decl_stmt|;
DECL|method|PackedArrayEstimator
specifier|public
name|PackedArrayEstimator
parameter_list|(
name|MemoryCircuitBreaker
name|breaker
parameter_list|,
name|NumericType
name|type
parameter_list|)
block|{
name|this
operator|.
name|breaker
operator|=
name|breaker
expr_stmt|;
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
block|}
comment|/**          * @return number of bytes per term, based on the NumericValue.requiredBits()          */
annotation|@
name|Override
DECL|method|bytesPerValue
specifier|public
name|long
name|bytesPerValue
parameter_list|(
name|BytesRef
name|term
parameter_list|)
block|{
comment|// Estimate about  about 0.8 (8 / 10) compression ratio for
comment|// numbers, but at least 4 bytes
return|return
name|Math
operator|.
name|max
argument_list|(
name|type
operator|.
name|requiredBits
argument_list|()
operator|/
literal|10
argument_list|,
literal|4
argument_list|)
return|;
block|}
comment|/**          * @return A TermsEnum wrapped in a RamAccountingTermsEnum          * @throws IOException          */
annotation|@
name|Override
DECL|method|beforeLoad
specifier|public
name|TermsEnum
name|beforeLoad
parameter_list|(
name|Terms
name|terms
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|RamAccountingTermsEnum
argument_list|(
name|type
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
argument_list|,
name|breaker
argument_list|,
name|this
argument_list|)
return|;
block|}
comment|/**          * Adjusts the breaker based on the aggregated value from the RamAccountingTermsEnum          *          * @param termsEnum  terms that were wrapped and loaded          * @param actualUsed actual field data memory usage          */
annotation|@
name|Override
DECL|method|afterLoad
specifier|public
name|void
name|afterLoad
parameter_list|(
name|TermsEnum
name|termsEnum
parameter_list|,
name|long
name|actualUsed
parameter_list|)
block|{
assert|assert
name|termsEnum
operator|instanceof
name|RamAccountingTermsEnum
assert|;
name|long
name|estimatedBytes
init|=
operator|(
operator|(
name|RamAccountingTermsEnum
operator|)
name|termsEnum
operator|)
operator|.
name|getTotalBytes
argument_list|()
decl_stmt|;
name|breaker
operator|.
name|addWithoutBreaking
argument_list|(
operator|-
operator|(
name|estimatedBytes
operator|-
name|actualUsed
operator|)
argument_list|)
expr_stmt|;
block|}
comment|/**          * Adjust the breaker when no terms were actually loaded, but the field          * data takes up space regardless. For instance, when ordinals are          * used.          *          * @param actualUsed bytes actually used          */
DECL|method|adjustForNoTerms
specifier|public
name|void
name|adjustForNoTerms
parameter_list|(
name|long
name|actualUsed
parameter_list|)
block|{
name|breaker
operator|.
name|addWithoutBreaking
argument_list|(
name|actualUsed
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

