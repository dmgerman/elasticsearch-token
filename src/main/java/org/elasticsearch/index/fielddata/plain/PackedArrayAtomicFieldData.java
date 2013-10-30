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
name|Ordinals
import|;
end_import

begin_comment
comment|/**  * {@link AtomicNumericFieldData} implementation which stores data in packed arrays to save memory.  */
end_comment

begin_class
DECL|class|PackedArrayAtomicFieldData
specifier|public
specifier|abstract
class|class
name|PackedArrayAtomicFieldData
extends|extends
name|AbstractAtomicNumericFieldData
block|{
DECL|method|empty
specifier|public
specifier|static
name|PackedArrayAtomicFieldData
name|empty
parameter_list|(
name|int
name|numDocs
parameter_list|)
block|{
return|return
operator|new
name|Empty
argument_list|(
name|numDocs
argument_list|)
return|;
block|}
DECL|field|numDocs
specifier|private
specifier|final
name|int
name|numDocs
decl_stmt|;
DECL|field|size
specifier|protected
name|long
name|size
init|=
operator|-
literal|1
decl_stmt|;
DECL|method|PackedArrayAtomicFieldData
specifier|public
name|PackedArrayAtomicFieldData
parameter_list|(
name|int
name|numDocs
parameter_list|)
block|{
name|super
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|this
operator|.
name|numDocs
operator|=
name|numDocs
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
block|{     }
annotation|@
name|Override
DECL|method|getNumDocs
specifier|public
name|int
name|getNumDocs
parameter_list|()
block|{
return|return
name|numDocs
return|;
block|}
DECL|class|Empty
specifier|static
class|class
name|Empty
extends|extends
name|PackedArrayAtomicFieldData
block|{
DECL|method|Empty
name|Empty
parameter_list|(
name|int
name|numDocs
parameter_list|)
block|{
name|super
argument_list|(
name|numDocs
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getLongValues
specifier|public
name|LongValues
name|getLongValues
parameter_list|()
block|{
return|return
name|LongValues
operator|.
name|EMPTY
return|;
block|}
annotation|@
name|Override
DECL|method|getDoubleValues
specifier|public
name|DoubleValues
name|getDoubleValues
parameter_list|()
block|{
return|return
name|DoubleValues
operator|.
name|EMPTY
return|;
block|}
annotation|@
name|Override
DECL|method|isMultiValued
specifier|public
name|boolean
name|isMultiValued
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
DECL|method|isValuesOrdered
specifier|public
name|boolean
name|isValuesOrdered
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
DECL|method|getMemorySizeInBytes
specifier|public
name|long
name|getMemorySizeInBytes
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
DECL|method|getNumberUniqueValues
specifier|public
name|long
name|getNumberUniqueValues
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
DECL|method|getBytesValues
specifier|public
name|BytesValues
name|getBytesValues
parameter_list|(
name|boolean
name|needsHashes
parameter_list|)
block|{
return|return
name|BytesValues
operator|.
name|EMPTY
return|;
block|}
annotation|@
name|Override
DECL|method|getScriptValues
specifier|public
name|ScriptDocValues
name|getScriptValues
parameter_list|()
block|{
return|return
name|ScriptDocValues
operator|.
name|EMPTY
return|;
block|}
block|}
DECL|class|WithOrdinals
specifier|public
specifier|static
class|class
name|WithOrdinals
extends|extends
name|PackedArrayAtomicFieldData
block|{
DECL|field|values
specifier|private
specifier|final
name|MonotonicAppendingLongBuffer
name|values
decl_stmt|;
DECL|field|ordinals
specifier|private
specifier|final
name|Ordinals
name|ordinals
decl_stmt|;
DECL|method|WithOrdinals
specifier|public
name|WithOrdinals
parameter_list|(
name|MonotonicAppendingLongBuffer
name|values
parameter_list|,
name|int
name|numDocs
parameter_list|,
name|Ordinals
name|ordinals
parameter_list|)
block|{
name|super
argument_list|(
name|numDocs
argument_list|)
expr_stmt|;
name|this
operator|.
name|values
operator|=
name|values
expr_stmt|;
name|this
operator|.
name|ordinals
operator|=
name|ordinals
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|isMultiValued
specifier|public
name|boolean
name|isMultiValued
parameter_list|()
block|{
return|return
name|ordinals
operator|.
name|isMultiValued
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|isValuesOrdered
specifier|public
name|boolean
name|isValuesOrdered
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
DECL|method|getMemorySizeInBytes
specifier|public
name|long
name|getMemorySizeInBytes
parameter_list|()
block|{
if|if
condition|(
name|size
operator|==
operator|-
literal|1
condition|)
block|{
name|size
operator|=
name|RamUsageEstimator
operator|.
name|NUM_BYTES_INT
comment|/*size*/
operator|+
name|RamUsageEstimator
operator|.
name|NUM_BYTES_INT
comment|/*numDocs*/
operator|+
name|values
operator|.
name|ramBytesUsed
argument_list|()
operator|+
name|ordinals
operator|.
name|getMemorySizeInBytes
argument_list|()
expr_stmt|;
block|}
return|return
name|size
return|;
block|}
annotation|@
name|Override
DECL|method|getNumberUniqueValues
specifier|public
name|long
name|getNumberUniqueValues
parameter_list|()
block|{
return|return
name|ordinals
operator|.
name|getNumOrds
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|getLongValues
specifier|public
name|LongValues
name|getLongValues
parameter_list|()
block|{
return|return
operator|new
name|LongValues
argument_list|(
name|values
argument_list|,
name|ordinals
operator|.
name|ordinals
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|getDoubleValues
specifier|public
name|DoubleValues
name|getDoubleValues
parameter_list|()
block|{
return|return
operator|new
name|DoubleValues
argument_list|(
name|values
argument_list|,
name|ordinals
operator|.
name|ordinals
argument_list|()
argument_list|)
return|;
block|}
DECL|class|LongValues
specifier|static
class|class
name|LongValues
extends|extends
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|fielddata
operator|.
name|LongValues
operator|.
name|WithOrdinals
block|{
DECL|field|values
specifier|private
specifier|final
name|MonotonicAppendingLongBuffer
name|values
decl_stmt|;
DECL|method|LongValues
name|LongValues
parameter_list|(
name|MonotonicAppendingLongBuffer
name|values
parameter_list|,
name|Ordinals
operator|.
name|Docs
name|ordinals
parameter_list|)
block|{
name|super
argument_list|(
name|ordinals
argument_list|)
expr_stmt|;
name|this
operator|.
name|values
operator|=
name|values
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getValueByOrd
specifier|public
name|long
name|getValueByOrd
parameter_list|(
name|long
name|ord
parameter_list|)
block|{
assert|assert
name|ord
operator|!=
name|Ordinals
operator|.
name|MISSING_ORDINAL
assert|;
return|return
name|values
operator|.
name|get
argument_list|(
name|ord
operator|-
literal|1
argument_list|)
return|;
block|}
block|}
DECL|class|DoubleValues
specifier|static
class|class
name|DoubleValues
extends|extends
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|fielddata
operator|.
name|DoubleValues
operator|.
name|WithOrdinals
block|{
DECL|field|values
specifier|private
specifier|final
name|MonotonicAppendingLongBuffer
name|values
decl_stmt|;
DECL|method|DoubleValues
name|DoubleValues
parameter_list|(
name|MonotonicAppendingLongBuffer
name|values
parameter_list|,
name|Ordinals
operator|.
name|Docs
name|ordinals
parameter_list|)
block|{
name|super
argument_list|(
name|ordinals
argument_list|)
expr_stmt|;
name|this
operator|.
name|values
operator|=
name|values
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getValueByOrd
specifier|public
name|double
name|getValueByOrd
parameter_list|(
name|long
name|ord
parameter_list|)
block|{
assert|assert
name|ord
operator|!=
name|Ordinals
operator|.
name|MISSING_ORDINAL
assert|;
return|return
name|values
operator|.
name|get
argument_list|(
name|ord
operator|-
literal|1
argument_list|)
return|;
block|}
block|}
block|}
comment|/**      * A single valued case, where not all values are "set", so we have a special      * value which encodes the fact that the document has no value.      */
DECL|class|SingleSparse
specifier|public
specifier|static
class|class
name|SingleSparse
extends|extends
name|PackedArrayAtomicFieldData
block|{
DECL|field|values
specifier|private
specifier|final
name|PackedInts
operator|.
name|Mutable
name|values
decl_stmt|;
DECL|field|minValue
specifier|private
specifier|final
name|long
name|minValue
decl_stmt|;
DECL|field|missingValue
specifier|private
specifier|final
name|long
name|missingValue
decl_stmt|;
DECL|field|numOrds
specifier|private
specifier|final
name|long
name|numOrds
decl_stmt|;
DECL|method|SingleSparse
specifier|public
name|SingleSparse
parameter_list|(
name|PackedInts
operator|.
name|Mutable
name|values
parameter_list|,
name|long
name|minValue
parameter_list|,
name|int
name|numDocs
parameter_list|,
name|long
name|missingValue
parameter_list|,
name|long
name|numOrds
parameter_list|)
block|{
name|super
argument_list|(
name|numDocs
argument_list|)
expr_stmt|;
name|this
operator|.
name|values
operator|=
name|values
expr_stmt|;
name|this
operator|.
name|minValue
operator|=
name|minValue
expr_stmt|;
name|this
operator|.
name|missingValue
operator|=
name|missingValue
expr_stmt|;
name|this
operator|.
name|numOrds
operator|=
name|numOrds
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|isMultiValued
specifier|public
name|boolean
name|isMultiValued
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
DECL|method|isValuesOrdered
specifier|public
name|boolean
name|isValuesOrdered
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
DECL|method|getNumberUniqueValues
specifier|public
name|long
name|getNumberUniqueValues
parameter_list|()
block|{
return|return
name|numOrds
return|;
block|}
annotation|@
name|Override
DECL|method|getMemorySizeInBytes
specifier|public
name|long
name|getMemorySizeInBytes
parameter_list|()
block|{
if|if
condition|(
name|size
operator|==
operator|-
literal|1
condition|)
block|{
name|size
operator|=
name|values
operator|.
name|ramBytesUsed
argument_list|()
operator|+
literal|2
operator|*
name|RamUsageEstimator
operator|.
name|NUM_BYTES_LONG
expr_stmt|;
block|}
return|return
name|size
return|;
block|}
annotation|@
name|Override
DECL|method|getLongValues
specifier|public
name|LongValues
name|getLongValues
parameter_list|()
block|{
return|return
operator|new
name|LongValues
argument_list|(
name|values
argument_list|,
name|minValue
argument_list|,
name|missingValue
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|getDoubleValues
specifier|public
name|DoubleValues
name|getDoubleValues
parameter_list|()
block|{
return|return
operator|new
name|DoubleValues
argument_list|(
name|values
argument_list|,
name|minValue
argument_list|,
name|missingValue
argument_list|)
return|;
block|}
DECL|class|LongValues
specifier|static
class|class
name|LongValues
extends|extends
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|fielddata
operator|.
name|LongValues
block|{
DECL|field|values
specifier|private
specifier|final
name|PackedInts
operator|.
name|Mutable
name|values
decl_stmt|;
DECL|field|minValue
specifier|private
specifier|final
name|long
name|minValue
decl_stmt|;
DECL|field|missingValue
specifier|private
specifier|final
name|long
name|missingValue
decl_stmt|;
DECL|method|LongValues
name|LongValues
parameter_list|(
name|PackedInts
operator|.
name|Mutable
name|values
parameter_list|,
name|long
name|minValue
parameter_list|,
name|long
name|missingValue
parameter_list|)
block|{
name|super
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|this
operator|.
name|values
operator|=
name|values
expr_stmt|;
name|this
operator|.
name|minValue
operator|=
name|minValue
expr_stmt|;
name|this
operator|.
name|missingValue
operator|=
name|missingValue
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|hasValue
specifier|public
name|boolean
name|hasValue
parameter_list|(
name|int
name|docId
parameter_list|)
block|{
return|return
name|values
operator|.
name|get
argument_list|(
name|docId
argument_list|)
operator|!=
name|missingValue
return|;
block|}
annotation|@
name|Override
DECL|method|getValue
specifier|public
name|long
name|getValue
parameter_list|(
name|int
name|docId
parameter_list|)
block|{
specifier|final
name|long
name|value
init|=
name|values
operator|.
name|get
argument_list|(
name|docId
argument_list|)
decl_stmt|;
return|return
name|value
operator|==
name|missingValue
condition|?
literal|0L
else|:
name|minValue
operator|+
name|value
return|;
block|}
block|}
DECL|class|DoubleValues
specifier|static
class|class
name|DoubleValues
extends|extends
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|fielddata
operator|.
name|DoubleValues
block|{
DECL|field|values
specifier|private
specifier|final
name|PackedInts
operator|.
name|Mutable
name|values
decl_stmt|;
DECL|field|minValue
specifier|private
specifier|final
name|long
name|minValue
decl_stmt|;
DECL|field|missingValue
specifier|private
specifier|final
name|long
name|missingValue
decl_stmt|;
DECL|method|DoubleValues
name|DoubleValues
parameter_list|(
name|PackedInts
operator|.
name|Mutable
name|values
parameter_list|,
name|long
name|minValue
parameter_list|,
name|long
name|missingValue
parameter_list|)
block|{
name|super
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|this
operator|.
name|values
operator|=
name|values
expr_stmt|;
name|this
operator|.
name|minValue
operator|=
name|minValue
expr_stmt|;
name|this
operator|.
name|missingValue
operator|=
name|missingValue
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|hasValue
specifier|public
name|boolean
name|hasValue
parameter_list|(
name|int
name|docId
parameter_list|)
block|{
return|return
name|values
operator|.
name|get
argument_list|(
name|docId
argument_list|)
operator|!=
name|missingValue
return|;
block|}
annotation|@
name|Override
DECL|method|getValue
specifier|public
name|double
name|getValue
parameter_list|(
name|int
name|docId
parameter_list|)
block|{
specifier|final
name|long
name|value
init|=
name|values
operator|.
name|get
argument_list|(
name|docId
argument_list|)
decl_stmt|;
return|return
name|value
operator|==
name|missingValue
condition|?
literal|0L
else|:
name|minValue
operator|+
name|value
return|;
block|}
block|}
block|}
comment|/**      * Assumes all the values are "set", and docId is used as the index to the value array.      */
DECL|class|Single
specifier|public
specifier|static
class|class
name|Single
extends|extends
name|PackedArrayAtomicFieldData
block|{
DECL|field|values
specifier|private
specifier|final
name|PackedInts
operator|.
name|Mutable
name|values
decl_stmt|;
DECL|field|minValue
specifier|private
specifier|final
name|long
name|minValue
decl_stmt|;
DECL|field|numOrds
specifier|private
specifier|final
name|long
name|numOrds
decl_stmt|;
comment|/**          * Note, here, we assume that there is no offset by 1 from docId, so position 0          * is the value for docId 0.          */
DECL|method|Single
specifier|public
name|Single
parameter_list|(
name|PackedInts
operator|.
name|Mutable
name|values
parameter_list|,
name|long
name|minValue
parameter_list|,
name|int
name|numDocs
parameter_list|,
name|long
name|numOrds
parameter_list|)
block|{
name|super
argument_list|(
name|numDocs
argument_list|)
expr_stmt|;
name|this
operator|.
name|values
operator|=
name|values
expr_stmt|;
name|this
operator|.
name|minValue
operator|=
name|minValue
expr_stmt|;
name|this
operator|.
name|numOrds
operator|=
name|numOrds
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|isMultiValued
specifier|public
name|boolean
name|isMultiValued
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
DECL|method|isValuesOrdered
specifier|public
name|boolean
name|isValuesOrdered
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
DECL|method|getNumberUniqueValues
specifier|public
name|long
name|getNumberUniqueValues
parameter_list|()
block|{
return|return
name|numOrds
return|;
block|}
annotation|@
name|Override
DECL|method|getMemorySizeInBytes
specifier|public
name|long
name|getMemorySizeInBytes
parameter_list|()
block|{
if|if
condition|(
name|size
operator|==
operator|-
literal|1
condition|)
block|{
name|size
operator|=
name|values
operator|.
name|ramBytesUsed
argument_list|()
expr_stmt|;
block|}
return|return
name|size
return|;
block|}
annotation|@
name|Override
DECL|method|getLongValues
specifier|public
name|LongValues
name|getLongValues
parameter_list|()
block|{
return|return
operator|new
name|LongValues
argument_list|(
name|values
argument_list|,
name|minValue
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|getDoubleValues
specifier|public
name|DoubleValues
name|getDoubleValues
parameter_list|()
block|{
return|return
operator|new
name|DoubleValues
argument_list|(
name|values
argument_list|,
name|minValue
argument_list|)
return|;
block|}
DECL|class|LongValues
specifier|static
class|class
name|LongValues
extends|extends
name|DenseLongValues
block|{
DECL|field|values
specifier|private
specifier|final
name|PackedInts
operator|.
name|Mutable
name|values
decl_stmt|;
DECL|field|minValue
specifier|private
specifier|final
name|long
name|minValue
decl_stmt|;
DECL|method|LongValues
name|LongValues
parameter_list|(
name|PackedInts
operator|.
name|Mutable
name|values
parameter_list|,
name|long
name|minValue
parameter_list|)
block|{
name|super
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|this
operator|.
name|values
operator|=
name|values
expr_stmt|;
name|this
operator|.
name|minValue
operator|=
name|minValue
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getValue
specifier|public
name|long
name|getValue
parameter_list|(
name|int
name|docId
parameter_list|)
block|{
return|return
name|minValue
operator|+
name|values
operator|.
name|get
argument_list|(
name|docId
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|nextValue
specifier|public
name|long
name|nextValue
parameter_list|()
block|{
return|return
name|minValue
operator|+
name|values
operator|.
name|get
argument_list|(
name|docId
argument_list|)
return|;
block|}
block|}
DECL|class|DoubleValues
specifier|static
class|class
name|DoubleValues
extends|extends
name|DenseDoubleValues
block|{
DECL|field|values
specifier|private
specifier|final
name|PackedInts
operator|.
name|Mutable
name|values
decl_stmt|;
DECL|field|minValue
specifier|private
specifier|final
name|long
name|minValue
decl_stmt|;
DECL|method|DoubleValues
name|DoubleValues
parameter_list|(
name|PackedInts
operator|.
name|Mutable
name|values
parameter_list|,
name|long
name|minValue
parameter_list|)
block|{
name|super
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|this
operator|.
name|values
operator|=
name|values
expr_stmt|;
name|this
operator|.
name|minValue
operator|=
name|minValue
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getValue
specifier|public
name|double
name|getValue
parameter_list|(
name|int
name|docId
parameter_list|)
block|{
return|return
name|minValue
operator|+
name|values
operator|.
name|get
argument_list|(
name|docId
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|nextValue
specifier|public
name|double
name|nextValue
parameter_list|()
block|{
return|return
name|minValue
operator|+
name|values
operator|.
name|get
argument_list|(
name|docId
argument_list|)
return|;
block|}
block|}
block|}
block|}
end_class

end_unit

