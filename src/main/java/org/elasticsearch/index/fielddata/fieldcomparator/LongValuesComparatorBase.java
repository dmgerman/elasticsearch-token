begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
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

begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *      http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

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
name|elasticsearch
operator|.
name|index
operator|.
name|fielddata
operator|.
name|IndexNumericFieldData
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
name|LongValues
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

begin_class
DECL|class|LongValuesComparatorBase
specifier|abstract
class|class
name|LongValuesComparatorBase
parameter_list|<
name|T
extends|extends
name|Number
parameter_list|>
extends|extends
name|FieldComparator
argument_list|<
name|T
argument_list|>
block|{
DECL|field|indexFieldData
specifier|protected
specifier|final
name|IndexNumericFieldData
argument_list|<
name|?
argument_list|>
name|indexFieldData
decl_stmt|;
DECL|field|missingValue
specifier|protected
specifier|final
name|long
name|missingValue
decl_stmt|;
DECL|field|bottom
specifier|protected
name|long
name|bottom
decl_stmt|;
DECL|field|readerValues
specifier|protected
name|LongValues
name|readerValues
decl_stmt|;
DECL|field|sortMode
specifier|private
specifier|final
name|SortMode
name|sortMode
decl_stmt|;
DECL|method|LongValuesComparatorBase
specifier|public
name|LongValuesComparatorBase
parameter_list|(
name|IndexNumericFieldData
argument_list|<
name|?
argument_list|>
name|indexFieldData
parameter_list|,
name|long
name|missingValue
parameter_list|,
name|SortMode
name|sortMode
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
name|missingValue
operator|=
name|missingValue
expr_stmt|;
name|this
operator|.
name|sortMode
operator|=
name|sortMode
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|compareBottom
specifier|public
specifier|final
name|int
name|compareBottom
parameter_list|(
name|int
name|doc
parameter_list|)
throws|throws
name|IOException
block|{
name|long
name|v2
init|=
name|readerValues
operator|.
name|getValueMissing
argument_list|(
name|doc
argument_list|,
name|missingValue
argument_list|)
decl_stmt|;
return|return
name|compare
argument_list|(
name|bottom
argument_list|,
name|v2
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|compareDocToValue
specifier|public
specifier|final
name|int
name|compareDocToValue
parameter_list|(
name|int
name|doc
parameter_list|,
name|T
name|valueObj
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|long
name|value
init|=
name|valueObj
operator|.
name|longValue
argument_list|()
decl_stmt|;
name|long
name|docValue
init|=
name|readerValues
operator|.
name|getValueMissing
argument_list|(
name|doc
argument_list|,
name|missingValue
argument_list|)
decl_stmt|;
return|return
name|compare
argument_list|(
name|docValue
argument_list|,
name|value
argument_list|)
return|;
block|}
DECL|method|compare
specifier|static
specifier|final
name|int
name|compare
parameter_list|(
name|long
name|left
parameter_list|,
name|long
name|right
parameter_list|)
block|{
if|if
condition|(
name|left
operator|>
name|right
condition|)
block|{
return|return
literal|1
return|;
block|}
elseif|else
if|if
condition|(
name|left
operator|<
name|right
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
else|else
block|{
return|return
literal|0
return|;
block|}
block|}
annotation|@
name|Override
DECL|method|setNextReader
specifier|public
specifier|final
name|FieldComparator
argument_list|<
name|T
argument_list|>
name|setNextReader
parameter_list|(
name|AtomicReaderContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|readerValues
operator|=
name|indexFieldData
operator|.
name|load
argument_list|(
name|context
argument_list|)
operator|.
name|getLongValues
argument_list|()
expr_stmt|;
if|if
condition|(
name|readerValues
operator|.
name|isMultiValued
argument_list|()
condition|)
block|{
name|readerValues
operator|=
operator|new
name|MultiValuedBytesWrapper
argument_list|(
name|readerValues
argument_list|,
name|sortMode
argument_list|)
expr_stmt|;
block|}
return|return
name|this
return|;
block|}
DECL|class|MultiValuedBytesWrapper
specifier|private
specifier|static
specifier|final
class|class
name|MultiValuedBytesWrapper
extends|extends
name|LongValues
operator|.
name|FilteredLongValues
block|{
DECL|field|sortMode
specifier|private
specifier|final
name|SortMode
name|sortMode
decl_stmt|;
DECL|method|MultiValuedBytesWrapper
specifier|public
name|MultiValuedBytesWrapper
parameter_list|(
name|LongValues
name|delegate
parameter_list|,
name|SortMode
name|sortMode
parameter_list|)
block|{
name|super
argument_list|(
name|delegate
argument_list|)
expr_stmt|;
name|this
operator|.
name|sortMode
operator|=
name|sortMode
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getValueMissing
specifier|public
name|long
name|getValueMissing
parameter_list|(
name|int
name|docId
parameter_list|,
name|long
name|missing
parameter_list|)
block|{
name|LongValues
operator|.
name|Iter
name|iter
init|=
name|delegate
operator|.
name|getIter
argument_list|(
name|docId
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|iter
operator|.
name|hasNext
argument_list|()
condition|)
block|{
return|return
name|missing
return|;
block|}
name|long
name|currentVal
init|=
name|iter
operator|.
name|next
argument_list|()
decl_stmt|;
name|long
name|relevantVal
init|=
name|currentVal
decl_stmt|;
name|int
name|counter
init|=
literal|1
decl_stmt|;
while|while
condition|(
name|iter
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|currentVal
operator|=
name|iter
operator|.
name|next
argument_list|()
expr_stmt|;
switch|switch
condition|(
name|sortMode
condition|)
block|{
case|case
name|SUM
case|:
name|relevantVal
operator|+=
name|currentVal
expr_stmt|;
break|break;
case|case
name|AVG
case|:
name|relevantVal
operator|+=
name|currentVal
expr_stmt|;
name|counter
operator|++
expr_stmt|;
break|break;
case|case
name|MAX
case|:
if|if
condition|(
name|currentVal
operator|>
name|relevantVal
condition|)
block|{
name|relevantVal
operator|=
name|currentVal
expr_stmt|;
block|}
break|break;
case|case
name|MIN
case|:
if|if
condition|(
name|currentVal
operator|<
name|relevantVal
condition|)
block|{
name|relevantVal
operator|=
name|currentVal
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|sortMode
operator|==
name|SortMode
operator|.
name|AVG
condition|)
block|{
return|return
name|relevantVal
operator|/
name|counter
return|;
block|}
else|else
block|{
return|return
name|relevantVal
return|;
block|}
comment|// If we have a method on readerValues that tells if the values emitted by Iter or ArrayRef are sorted per
comment|// document that we can do this or something similar:
comment|// (This is already possible, if values are loaded from index, but we just need a method that tells us this
comment|// For example a impl that read values from the _source field might not read values in order)
comment|/*if (reversed) {                 // Would be nice if there is a way to get highest value from LongValues. The values are sorted anyway.                 LongArrayRef ref = readerValues.getValues(doc);                 if (ref.isEmpty()) {                     return missing;                 } else {                     return ref.values[ref.end - 1]; // last element is the highest value.                 }             } else {                 return readerValues.getValueMissing(doc, missing); // returns lowest             }*/
block|}
block|}
block|}
end_class

end_unit

