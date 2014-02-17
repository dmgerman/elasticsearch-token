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
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|codecs
operator|.
name|BlockTreeTermsReader
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
name|PagedBytes
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
name|RamAccountingTermsEnum
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

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|PagedBytesIndexFieldData
specifier|public
class|class
name|PagedBytesIndexFieldData
extends|extends
name|AbstractBytesIndexFieldData
argument_list|<
name|PagedBytesAtomicFieldData
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
annotation|@
name|Override
DECL|method|build
specifier|public
name|IndexFieldData
argument_list|<
name|PagedBytesAtomicFieldData
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
name|PagedBytesIndexFieldData
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
name|breakerService
argument_list|,
name|globalOrdinalBuilder
argument_list|)
return|;
block|}
block|}
DECL|method|PagedBytesIndexFieldData
specifier|public
name|PagedBytesIndexFieldData
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
name|CircuitBreakerService
name|breakerService
parameter_list|,
name|GlobalOrdinalsBuilder
name|globalOrdinalsBuilder
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
argument_list|,
name|globalOrdinalsBuilder
argument_list|,
name|breakerService
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|loadDirect
specifier|public
name|PagedBytesAtomicFieldData
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
name|PagedBytesEstimator
name|estimator
init|=
operator|new
name|PagedBytesEstimator
argument_list|(
name|context
argument_list|,
name|breakerService
operator|.
name|getBreaker
argument_list|()
argument_list|)
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
name|PagedBytesAtomicFieldData
name|emptyData
init|=
name|PagedBytesAtomicFieldData
operator|.
name|empty
argument_list|(
name|reader
operator|.
name|maxDoc
argument_list|()
argument_list|)
decl_stmt|;
name|estimator
operator|.
name|adjustForNoTerms
argument_list|(
name|emptyData
operator|.
name|getMemorySizeInBytes
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|emptyData
return|;
block|}
specifier|final
name|PagedBytes
name|bytes
init|=
operator|new
name|PagedBytes
argument_list|(
literal|15
argument_list|)
decl_stmt|;
specifier|final
name|MonotonicAppendingLongBuffer
name|termOrdToBytesOffset
init|=
operator|new
name|MonotonicAppendingLongBuffer
argument_list|()
decl_stmt|;
name|termOrdToBytesOffset
operator|.
name|add
argument_list|(
literal|0
argument_list|)
expr_stmt|;
comment|// first ord is reserved for missing values
specifier|final
name|long
name|numTerms
decl_stmt|;
if|if
condition|(
name|regex
operator|==
literal|null
operator|&&
name|frequency
operator|==
literal|null
condition|)
block|{
name|numTerms
operator|=
name|terms
operator|.
name|size
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|numTerms
operator|=
operator|-
literal|1
expr_stmt|;
block|}
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
name|FilterSettingFields
operator|.
name|ACCEPTABLE_TRANSIENT_OVERHEAD_RATIO
argument_list|,
name|OrdinalsBuilder
operator|.
name|DEFAULT_ACCEPTABLE_OVERHEAD_RATIO
argument_list|)
decl_stmt|;
comment|// Wrap the context in an estimator and use it to either estimate
comment|// the entire set, or wrap the TermsEnum so it can be calculated
comment|// per-term
name|PagedBytesAtomicFieldData
name|data
init|=
literal|null
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
name|numTerms
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
comment|// 0 is reserved for "unset"
name|bytes
operator|.
name|copyUsingLengthPrefix
argument_list|(
operator|new
name|BytesRef
argument_list|()
argument_list|)
expr_stmt|;
name|DocsEnum
name|docsEnum
init|=
literal|null
decl_stmt|;
for|for
control|(
name|BytesRef
name|term
init|=
name|termsEnum
operator|.
name|next
argument_list|()
init|;
name|term
operator|!=
literal|null
condition|;
name|term
operator|=
name|termsEnum
operator|.
name|next
argument_list|()
control|)
block|{
specifier|final
name|long
name|termOrd
init|=
name|builder
operator|.
name|nextOrdinal
argument_list|()
decl_stmt|;
assert|assert
name|termOrd
operator|==
name|termOrdToBytesOffset
operator|.
name|size
argument_list|()
assert|;
name|termOrdToBytesOffset
operator|.
name|add
argument_list|(
name|bytes
operator|.
name|copyUsingLengthPrefix
argument_list|(
name|term
argument_list|)
argument_list|)
expr_stmt|;
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
for|for
control|(
name|int
name|docId
init|=
name|docsEnum
operator|.
name|nextDoc
argument_list|()
init|;
name|docId
operator|!=
name|DocsEnum
operator|.
name|NO_MORE_DOCS
condition|;
name|docId
operator|=
name|docsEnum
operator|.
name|nextDoc
argument_list|()
control|)
block|{
name|builder
operator|.
name|addDoc
argument_list|(
name|docId
argument_list|)
expr_stmt|;
block|}
block|}
specifier|final
name|long
name|sizePointer
init|=
name|bytes
operator|.
name|getPointer
argument_list|()
decl_stmt|;
name|PagedBytes
operator|.
name|Reader
name|bytesReader
init|=
name|bytes
operator|.
name|freeze
argument_list|(
literal|true
argument_list|)
decl_stmt|;
specifier|final
name|Ordinals
name|ordinals
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
name|data
operator|=
operator|new
name|PagedBytesAtomicFieldData
argument_list|(
name|bytesReader
argument_list|,
name|sizePointer
argument_list|,
name|termOrdToBytesOffset
argument_list|,
name|ordinals
argument_list|)
expr_stmt|;
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
comment|// Call .afterLoad() to adjust the breaker now that we have an exact size
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
comment|/**      * Estimator that wraps string field data by either using      * BlockTreeTermsReader, or wrapping the data in a RamAccountingTermsEnum      * if the BlockTreeTermsReader cannot be used.      */
DECL|class|PagedBytesEstimator
specifier|public
class|class
name|PagedBytesEstimator
implements|implements
name|PerValueEstimator
block|{
DECL|field|context
specifier|private
specifier|final
name|AtomicReaderContext
name|context
decl_stmt|;
DECL|field|breaker
specifier|private
specifier|final
name|MemoryCircuitBreaker
name|breaker
decl_stmt|;
DECL|field|estimatedBytes
specifier|private
name|long
name|estimatedBytes
decl_stmt|;
DECL|method|PagedBytesEstimator
name|PagedBytesEstimator
parameter_list|(
name|AtomicReaderContext
name|context
parameter_list|,
name|MemoryCircuitBreaker
name|breaker
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
name|context
operator|=
name|context
expr_stmt|;
block|}
comment|/**          * @return the number of bytes for the term based on the length and ordinal overhead          */
DECL|method|bytesPerValue
specifier|public
name|long
name|bytesPerValue
parameter_list|(
name|BytesRef
name|term
parameter_list|)
block|{
if|if
condition|(
name|term
operator|==
literal|null
condition|)
block|{
return|return
literal|0
return|;
block|}
name|long
name|bytes
init|=
name|term
operator|.
name|length
decl_stmt|;
comment|// 64 bytes for miscellaneous overhead
name|bytes
operator|+=
literal|64
expr_stmt|;
comment|// Seems to be about a 1.5x compression per term/ord, plus 1 for some wiggle room
name|bytes
operator|=
call|(
name|long
call|)
argument_list|(
operator|(
name|double
operator|)
name|bytes
operator|/
literal|1.5
argument_list|)
operator|+
literal|1
expr_stmt|;
return|return
name|bytes
return|;
block|}
comment|/**          * @return the estimate for loading the entire term set into field data, or 0 if unavailable          */
DECL|method|estimateStringFieldData
specifier|public
name|long
name|estimateStringFieldData
parameter_list|()
block|{
try|try
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
name|Fields
name|fields
init|=
name|reader
operator|.
name|fields
argument_list|()
decl_stmt|;
specifier|final
name|Terms
name|fieldTerms
init|=
name|fields
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
name|fieldTerms
operator|instanceof
name|BlockTreeTermsReader
operator|.
name|FieldReader
condition|)
block|{
specifier|final
name|BlockTreeTermsReader
operator|.
name|Stats
name|stats
init|=
operator|(
operator|(
name|BlockTreeTermsReader
operator|.
name|FieldReader
operator|)
name|fieldTerms
operator|)
operator|.
name|computeStats
argument_list|()
decl_stmt|;
name|long
name|totalTermBytes
init|=
name|stats
operator|.
name|totalTermBytes
decl_stmt|;
if|if
condition|(
name|logger
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"totalTermBytes: {}, terms.size(): {}, terms.getSumDocFreq(): {}"
argument_list|,
name|totalTermBytes
argument_list|,
name|terms
operator|.
name|size
argument_list|()
argument_list|,
name|terms
operator|.
name|getSumDocFreq
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|long
name|totalBytes
init|=
name|totalTermBytes
operator|+
operator|(
literal|2
operator|*
name|terms
operator|.
name|size
argument_list|()
operator|)
operator|+
operator|(
literal|4
operator|*
name|terms
operator|.
name|getSumDocFreq
argument_list|()
operator|)
decl_stmt|;
return|return
name|totalBytes
return|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"Unable to estimate memory overhead"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
return|return
literal|0
return|;
block|}
comment|/**          * Determine whether the BlockTreeTermsReader.FieldReader can be used          * for estimating the field data, adding the estimate to the circuit          * breaker if it can, otherwise wrapping the terms in a          * RamAccountingTermsEnum to be estimated on a per-term basis.          *          * @param terms terms to be estimated          * @return A possibly wrapped TermsEnum for the terms          * @throws IOException          */
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
name|FilterSettingFields
operator|.
name|ACCEPTABLE_TRANSIENT_OVERHEAD_RATIO
argument_list|,
name|OrdinalsBuilder
operator|.
name|DEFAULT_ACCEPTABLE_OVERHEAD_RATIO
argument_list|)
decl_stmt|;
name|AtomicReader
name|reader
init|=
name|context
operator|.
name|reader
argument_list|()
decl_stmt|;
comment|// Check if one of the following is present:
comment|// - The OrdinalsBuilder overhead has been tweaked away from the default
comment|// - A field data filter is present
comment|// - A regex filter is present
if|if
condition|(
name|acceptableTransientOverheadRatio
operator|!=
name|OrdinalsBuilder
operator|.
name|DEFAULT_ACCEPTABLE_OVERHEAD_RATIO
operator|||
name|fieldDataType
operator|.
name|getSettings
argument_list|()
operator|.
name|getAsDouble
argument_list|(
name|FilterSettingFields
operator|.
name|FREQUENCY_MIN
argument_list|,
literal|0d
argument_list|)
operator|!=
literal|0d
operator|||
name|fieldDataType
operator|.
name|getSettings
argument_list|()
operator|.
name|getAsDouble
argument_list|(
name|FilterSettingFields
operator|.
name|FREQUENCY_MAX
argument_list|,
literal|0d
argument_list|)
operator|!=
literal|0d
operator|||
name|fieldDataType
operator|.
name|getSettings
argument_list|()
operator|.
name|getAsDouble
argument_list|(
name|FilterSettingFields
operator|.
name|FREQUENCY_MIN_SEGMENT_SIZE
argument_list|,
literal|0d
argument_list|)
operator|!=
literal|0d
operator|||
name|fieldDataType
operator|.
name|getSettings
argument_list|()
operator|.
name|get
argument_list|(
name|FilterSettingFields
operator|.
name|REGEX_PATTERN
argument_list|)
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|logger
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"Filter exists, can't circuit break normally, using RamAccountingTermsEnum"
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|RamAccountingTermsEnum
argument_list|(
name|filter
argument_list|(
name|terms
argument_list|,
name|reader
argument_list|)
argument_list|,
name|breaker
argument_list|,
name|this
argument_list|)
return|;
block|}
else|else
block|{
name|estimatedBytes
operator|=
name|this
operator|.
name|estimateStringFieldData
argument_list|()
expr_stmt|;
comment|// If we weren't able to estimate, wrap in the RamAccountingTermsEnum
if|if
condition|(
name|estimatedBytes
operator|==
literal|0
condition|)
block|{
return|return
operator|new
name|RamAccountingTermsEnum
argument_list|(
name|filter
argument_list|(
name|terms
argument_list|,
name|reader
argument_list|)
argument_list|,
name|breaker
argument_list|,
name|this
argument_list|)
return|;
block|}
name|breaker
operator|.
name|addEstimateBytesAndMaybeBreak
argument_list|(
name|estimatedBytes
argument_list|)
expr_stmt|;
return|return
name|filter
argument_list|(
name|terms
argument_list|,
name|reader
argument_list|)
return|;
block|}
block|}
comment|/**          * Adjust the circuit breaker now that terms have been loaded, getting          * the actual used either from the parameter (if estimation worked for          * the entire set), or from the TermsEnum if it has been wrapped in a          * RamAccountingTermsEnum.          *          * @param termsEnum  terms that were loaded          * @param actualUsed actual field data memory usage          */
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
if|if
condition|(
name|termsEnum
operator|instanceof
name|RamAccountingTermsEnum
condition|)
block|{
name|estimatedBytes
operator|=
operator|(
operator|(
name|RamAccountingTermsEnum
operator|)
name|termsEnum
operator|)
operator|.
name|getTotalBytes
argument_list|()
expr_stmt|;
block|}
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
comment|/**          * Adjust the breaker when no terms were actually loaded, but the field          * data takes up space regardless. For instance, when ordinals are          * used.          * @param actualUsed bytes actually used          */
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
DECL|class|FilterSettingFields
specifier|static
specifier|final
class|class
name|FilterSettingFields
block|{
DECL|field|ACCEPTABLE_TRANSIENT_OVERHEAD_RATIO
specifier|static
specifier|final
name|String
name|ACCEPTABLE_TRANSIENT_OVERHEAD_RATIO
init|=
literal|"acceptable_transient_overhead_ratio"
decl_stmt|;
DECL|field|FREQUENCY_MIN
specifier|static
specifier|final
name|String
name|FREQUENCY_MIN
init|=
literal|"filter.frequency.min"
decl_stmt|;
DECL|field|FREQUENCY_MAX
specifier|static
specifier|final
name|String
name|FREQUENCY_MAX
init|=
literal|"filter.frequency.max"
decl_stmt|;
DECL|field|FREQUENCY_MIN_SEGMENT_SIZE
specifier|static
specifier|final
name|String
name|FREQUENCY_MIN_SEGMENT_SIZE
init|=
literal|"filter.frequency.min_segment_size"
decl_stmt|;
DECL|field|REGEX_PATTERN
specifier|static
specifier|final
name|String
name|REGEX_PATTERN
init|=
literal|"filter.regex.pattern"
decl_stmt|;
block|}
block|}
end_class

end_unit

