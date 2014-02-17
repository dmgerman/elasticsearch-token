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
name|carrotsearch
operator|.
name|hppc
operator|.
name|ObjectObjectOpenHashMap
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
name|ObjectObjectCursor
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
name|collect
operator|.
name|ImmutableOpenMap
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
name|BytesRefFieldComparatorSource
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
name|DocumentMapper
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
name|DocumentTypeListener
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
name|mapper
operator|.
name|internal
operator|.
name|ParentFieldMapper
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
name|internal
operator|.
name|UidFieldMapper
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
name|NavigableSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeSet
import|;
end_import

begin_comment
comment|/**  * ParentChildIndexFieldData is responsible for loading the id cache mapping  * needed for has_child and has_parent queries into memory.  */
end_comment

begin_class
DECL|class|ParentChildIndexFieldData
specifier|public
class|class
name|ParentChildIndexFieldData
extends|extends
name|AbstractIndexFieldData
argument_list|<
name|ParentChildAtomicFieldData
argument_list|>
implements|implements
name|DocumentTypeListener
block|{
DECL|field|parentTypes
specifier|private
specifier|final
name|NavigableSet
argument_list|<
name|BytesRef
argument_list|>
name|parentTypes
decl_stmt|;
DECL|field|breakerService
specifier|private
specifier|final
name|CircuitBreakerService
name|breakerService
decl_stmt|;
comment|// If child type (a type with _parent field) is added or removed, we want to make sure modifications don't happen
comment|// while loading.
DECL|field|lock
specifier|private
specifier|final
name|Object
name|lock
init|=
operator|new
name|Object
argument_list|()
decl_stmt|;
DECL|method|ParentChildIndexFieldData
specifier|public
name|ParentChildIndexFieldData
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
name|MapperService
name|mapperService
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
name|parentTypes
operator|=
operator|new
name|TreeSet
argument_list|<>
argument_list|(
name|BytesRef
operator|.
name|getUTF8SortedAsUnicodeComparator
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|breakerService
operator|=
name|breakerService
expr_stmt|;
for|for
control|(
name|DocumentMapper
name|documentMapper
range|:
name|mapperService
control|)
block|{
name|beforeCreate
argument_list|(
name|documentMapper
argument_list|)
expr_stmt|;
block|}
name|mapperService
operator|.
name|addTypeListener
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|valuesOrdered
specifier|public
name|boolean
name|valuesOrdered
parameter_list|()
block|{
return|return
literal|true
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
name|BytesRefFieldComparatorSource
argument_list|(
name|this
argument_list|,
name|missingValue
argument_list|,
name|sortMode
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|loadDirect
specifier|public
name|ParentChildAtomicFieldData
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
synchronized|synchronized
init|(
name|lock
init|)
block|{
name|boolean
name|success
init|=
literal|false
decl_stmt|;
name|ParentChildAtomicFieldData
name|data
init|=
literal|null
decl_stmt|;
name|ParentChildFilteredTermsEnum
name|termsEnum
init|=
operator|new
name|ParentChildFilteredTermsEnum
argument_list|(
operator|new
name|ParentChildIntersectTermsEnum
argument_list|(
name|reader
argument_list|,
name|UidFieldMapper
operator|.
name|NAME
argument_list|,
name|ParentFieldMapper
operator|.
name|NAME
argument_list|)
argument_list|,
name|parentTypes
argument_list|)
decl_stmt|;
name|ParentChildEstimator
name|estimator
init|=
operator|new
name|ParentChildEstimator
argument_list|(
name|breakerService
operator|.
name|getBreaker
argument_list|()
argument_list|,
name|termsEnum
argument_list|)
decl_stmt|;
name|TermsEnum
name|estimatedTermsEnum
init|=
name|estimator
operator|.
name|beforeLoad
argument_list|(
literal|null
argument_list|)
decl_stmt|;
name|ObjectObjectOpenHashMap
argument_list|<
name|String
argument_list|,
name|TypeBuilder
argument_list|>
name|typeBuilders
init|=
name|ObjectObjectOpenHashMap
operator|.
name|newInstance
argument_list|()
decl_stmt|;
try|try
block|{
try|try
block|{
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
name|estimatedTermsEnum
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
name|estimatedTermsEnum
operator|.
name|next
argument_list|()
control|)
block|{
comment|// Usually this would be estimatedTermsEnum, but the
comment|// abstract TermsEnum class does not support the .type()
comment|// and .id() methods, so we skip using the wrapped
comment|// TermsEnum and delegate directly to the
comment|// ParentChildFilteredTermsEnum that was originally wrapped
name|String
name|type
init|=
name|termsEnum
operator|.
name|type
argument_list|()
decl_stmt|;
name|TypeBuilder
name|typeBuilder
init|=
name|typeBuilders
operator|.
name|get
argument_list|(
name|type
argument_list|)
decl_stmt|;
if|if
condition|(
name|typeBuilder
operator|==
literal|null
condition|)
block|{
name|typeBuilders
operator|.
name|put
argument_list|(
name|type
argument_list|,
name|typeBuilder
operator|=
operator|new
name|TypeBuilder
argument_list|(
name|acceptableTransientOverheadRatio
argument_list|,
name|reader
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|BytesRef
name|id
init|=
name|termsEnum
operator|.
name|id
argument_list|()
decl_stmt|;
specifier|final
name|long
name|termOrd
init|=
name|typeBuilder
operator|.
name|builder
operator|.
name|nextOrdinal
argument_list|()
decl_stmt|;
assert|assert
name|termOrd
operator|==
name|typeBuilder
operator|.
name|termOrdToBytesOffset
operator|.
name|size
argument_list|()
assert|;
name|typeBuilder
operator|.
name|termOrdToBytesOffset
operator|.
name|add
argument_list|(
name|typeBuilder
operator|.
name|bytes
operator|.
name|copyUsingLengthPrefix
argument_list|(
name|id
argument_list|)
argument_list|)
expr_stmt|;
name|docsEnum
operator|=
name|estimatedTermsEnum
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
name|typeBuilder
operator|.
name|builder
operator|.
name|addDoc
argument_list|(
name|docId
argument_list|)
expr_stmt|;
block|}
block|}
name|ImmutableOpenMap
operator|.
name|Builder
argument_list|<
name|String
argument_list|,
name|PagedBytesAtomicFieldData
argument_list|>
name|typeToAtomicFieldData
init|=
name|ImmutableOpenMap
operator|.
name|builder
argument_list|(
name|typeBuilders
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|ObjectObjectCursor
argument_list|<
name|String
argument_list|,
name|TypeBuilder
argument_list|>
name|cursor
range|:
name|typeBuilders
control|)
block|{
specifier|final
name|long
name|sizePointer
init|=
name|cursor
operator|.
name|value
operator|.
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
name|cursor
operator|.
name|value
operator|.
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
name|cursor
operator|.
name|value
operator|.
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
name|typeToAtomicFieldData
operator|.
name|put
argument_list|(
name|cursor
operator|.
name|key
argument_list|,
operator|new
name|PagedBytesAtomicFieldData
argument_list|(
name|bytesReader
argument_list|,
name|sizePointer
argument_list|,
name|cursor
operator|.
name|value
operator|.
name|termOrdToBytesOffset
argument_list|,
name|ordinals
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|data
operator|=
operator|new
name|ParentChildAtomicFieldData
argument_list|(
name|typeToAtomicFieldData
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
for|for
control|(
name|ObjectObjectCursor
argument_list|<
name|String
argument_list|,
name|TypeBuilder
argument_list|>
name|cursor
range|:
name|typeBuilders
control|)
block|{
name|cursor
operator|.
name|value
operator|.
name|builder
operator|.
name|close
argument_list|()
expr_stmt|;
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
name|success
condition|)
block|{
name|estimator
operator|.
name|afterLoad
argument_list|(
name|estimatedTermsEnum
argument_list|,
name|data
operator|.
name|getMemorySizeInBytes
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|estimator
operator|.
name|afterLoad
argument_list|(
name|estimatedTermsEnum
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
annotation|@
name|Override
DECL|method|beforeCreate
specifier|public
name|void
name|beforeCreate
parameter_list|(
name|DocumentMapper
name|mapper
parameter_list|)
block|{
synchronized|synchronized
init|(
name|lock
init|)
block|{
name|ParentFieldMapper
name|parentFieldMapper
init|=
name|mapper
operator|.
name|parentFieldMapper
argument_list|()
decl_stmt|;
if|if
condition|(
name|parentFieldMapper
operator|.
name|active
argument_list|()
condition|)
block|{
comment|// A _parent field can never be added to an existing mapping, so a _parent field either exists on
comment|// a new created or doesn't exists. This is why we can update the known parent types via DocumentTypeListener
if|if
condition|(
name|parentTypes
operator|.
name|add
argument_list|(
operator|new
name|BytesRef
argument_list|(
name|parentFieldMapper
operator|.
name|type
argument_list|()
argument_list|)
argument_list|)
condition|)
block|{
name|clear
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
annotation|@
name|Override
DECL|method|afterRemove
specifier|public
name|void
name|afterRemove
parameter_list|(
name|DocumentMapper
name|mapper
parameter_list|)
block|{
synchronized|synchronized
init|(
name|lock
init|)
block|{
name|ParentFieldMapper
name|parentFieldMapper
init|=
name|mapper
operator|.
name|parentFieldMapper
argument_list|()
decl_stmt|;
if|if
condition|(
name|parentFieldMapper
operator|.
name|active
argument_list|()
condition|)
block|{
name|parentTypes
operator|.
name|remove
argument_list|(
operator|new
name|BytesRef
argument_list|(
name|parentFieldMapper
operator|.
name|type
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|class|TypeBuilder
class|class
name|TypeBuilder
block|{
DECL|field|bytes
specifier|final
name|PagedBytes
name|bytes
decl_stmt|;
DECL|field|termOrdToBytesOffset
specifier|final
name|MonotonicAppendingLongBuffer
name|termOrdToBytesOffset
decl_stmt|;
DECL|field|builder
specifier|final
name|OrdinalsBuilder
name|builder
decl_stmt|;
DECL|method|TypeBuilder
name|TypeBuilder
parameter_list|(
name|float
name|acceptableTransientOverheadRatio
parameter_list|,
name|AtomicReader
name|reader
parameter_list|)
throws|throws
name|IOException
block|{
name|bytes
operator|=
operator|new
name|PagedBytes
argument_list|(
literal|15
argument_list|)
expr_stmt|;
name|termOrdToBytesOffset
operator|=
operator|new
name|MonotonicAppendingLongBuffer
argument_list|()
expr_stmt|;
name|termOrdToBytesOffset
operator|.
name|add
argument_list|(
literal|0
argument_list|)
expr_stmt|;
comment|// first ord is reserved for missing values
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
name|builder
operator|=
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
expr_stmt|;
block|}
block|}
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
name|?
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
name|ParentChildIndexFieldData
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
name|mapperService
argument_list|,
name|breakerService
argument_list|)
return|;
block|}
block|}
comment|/**      * Estimator that wraps parent/child id field data by wrapping the data      * in a RamAccountingTermsEnum.      */
DECL|class|ParentChildEstimator
specifier|public
class|class
name|ParentChildEstimator
implements|implements
name|PerValueEstimator
block|{
DECL|field|breaker
specifier|private
specifier|final
name|MemoryCircuitBreaker
name|breaker
decl_stmt|;
DECL|field|filteredEnum
specifier|private
specifier|final
name|TermsEnum
name|filteredEnum
decl_stmt|;
comment|// The TermsEnum is passed in here instead of being generated in the
comment|// beforeLoad() function since it's filtered inside the previous
comment|// TermsEnum wrappers
DECL|method|ParentChildEstimator
specifier|public
name|ParentChildEstimator
parameter_list|(
name|MemoryCircuitBreaker
name|breaker
parameter_list|,
name|TermsEnum
name|filteredEnum
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
name|filteredEnum
operator|=
name|filteredEnum
expr_stmt|;
block|}
comment|/**          * General overhead for ids is 2 times the length of the ID          */
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
return|return
literal|2
operator|*
name|term
operator|.
name|length
return|;
block|}
comment|/**          * Wraps the already filtered {@link TermsEnum} in a          * {@link RamAccountingTermsEnum} and returns it          */
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
name|filteredEnum
argument_list|,
name|breaker
argument_list|,
name|this
argument_list|)
return|;
block|}
comment|/**          * Adjusts the breaker based on the difference between the actual usage          * and the aggregated estimations.          */
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
block|}
block|}
end_class

end_unit

