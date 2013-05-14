begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *      http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.search.nested
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|search
operator|.
name|nested
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
name|DocIdSet
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
name|search
operator|.
name|Filter
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
name|SortField
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
name|lucene
operator|.
name|docset
operator|.
name|DocIdSets
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
name|fieldcomparator
operator|.
name|NumberComparatorBase
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
DECL|class|NestedFieldComparatorSource
specifier|public
class|class
name|NestedFieldComparatorSource
extends|extends
name|IndexFieldData
operator|.
name|XFieldComparatorSource
block|{
DECL|field|sortMode
specifier|private
specifier|final
name|SortMode
name|sortMode
decl_stmt|;
DECL|field|wrappedSource
specifier|private
specifier|final
name|IndexFieldData
operator|.
name|XFieldComparatorSource
name|wrappedSource
decl_stmt|;
DECL|field|rootDocumentsFilter
specifier|private
specifier|final
name|Filter
name|rootDocumentsFilter
decl_stmt|;
DECL|field|innerDocumentsFilter
specifier|private
specifier|final
name|Filter
name|innerDocumentsFilter
decl_stmt|;
DECL|method|NestedFieldComparatorSource
specifier|public
name|NestedFieldComparatorSource
parameter_list|(
name|SortMode
name|sortMode
parameter_list|,
name|IndexFieldData
operator|.
name|XFieldComparatorSource
name|wrappedSource
parameter_list|,
name|Filter
name|rootDocumentsFilter
parameter_list|,
name|Filter
name|innerDocumentsFilter
parameter_list|)
block|{
name|this
operator|.
name|sortMode
operator|=
name|sortMode
expr_stmt|;
name|this
operator|.
name|wrappedSource
operator|=
name|wrappedSource
expr_stmt|;
name|this
operator|.
name|rootDocumentsFilter
operator|=
name|rootDocumentsFilter
expr_stmt|;
name|this
operator|.
name|innerDocumentsFilter
operator|=
name|innerDocumentsFilter
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|newComparator
specifier|public
name|FieldComparator
argument_list|<
name|?
argument_list|>
name|newComparator
parameter_list|(
name|String
name|fieldname
parameter_list|,
name|int
name|numHits
parameter_list|,
name|int
name|sortPos
parameter_list|,
name|boolean
name|reversed
parameter_list|)
throws|throws
name|IOException
block|{
comment|// +1: have one spare slot for value comparison between inner documents.
name|FieldComparator
name|wrappedComparator
init|=
name|wrappedSource
operator|.
name|newComparator
argument_list|(
name|fieldname
argument_list|,
name|numHits
operator|+
literal|1
argument_list|,
name|sortPos
argument_list|,
name|reversed
argument_list|)
decl_stmt|;
switch|switch
condition|(
name|sortMode
condition|)
block|{
case|case
name|MAX
case|:
return|return
operator|new
name|NestedFieldComparator
operator|.
name|Highest
argument_list|(
name|wrappedComparator
argument_list|,
name|rootDocumentsFilter
argument_list|,
name|innerDocumentsFilter
argument_list|,
name|numHits
argument_list|)
return|;
case|case
name|MIN
case|:
return|return
operator|new
name|NestedFieldComparator
operator|.
name|Lowest
argument_list|(
name|wrappedComparator
argument_list|,
name|rootDocumentsFilter
argument_list|,
name|innerDocumentsFilter
argument_list|,
name|numHits
argument_list|)
return|;
case|case
name|SUM
case|:
return|return
operator|new
name|NestedFieldComparator
operator|.
name|Sum
argument_list|(
operator|(
name|NumberComparatorBase
operator|)
name|wrappedComparator
argument_list|,
name|rootDocumentsFilter
argument_list|,
name|innerDocumentsFilter
argument_list|,
name|numHits
argument_list|)
return|;
case|case
name|AVG
case|:
return|return
operator|new
name|NestedFieldComparator
operator|.
name|Avg
argument_list|(
operator|(
name|NumberComparatorBase
operator|)
name|wrappedComparator
argument_list|,
name|rootDocumentsFilter
argument_list|,
name|innerDocumentsFilter
argument_list|,
name|numHits
argument_list|)
return|;
default|default:
throw|throw
operator|new
name|ElasticSearchIllegalArgumentException
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"Unsupported sort_mode[%s] for nested type"
argument_list|,
name|sortMode
argument_list|)
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
DECL|method|reducedType
specifier|public
name|SortField
operator|.
name|Type
name|reducedType
parameter_list|()
block|{
return|return
name|wrappedSource
operator|.
name|reducedType
argument_list|()
return|;
block|}
block|}
end_class

begin_class
DECL|class|NestedFieldComparator
specifier|abstract
class|class
name|NestedFieldComparator
extends|extends
name|FieldComparator
block|{
DECL|field|rootDocumentsFilter
specifier|final
name|Filter
name|rootDocumentsFilter
decl_stmt|;
DECL|field|innerDocumentsFilter
specifier|final
name|Filter
name|innerDocumentsFilter
decl_stmt|;
DECL|field|spareSlot
specifier|final
name|int
name|spareSlot
decl_stmt|;
DECL|field|wrappedComparator
name|FieldComparator
name|wrappedComparator
decl_stmt|;
DECL|field|rootDocuments
name|FixedBitSet
name|rootDocuments
decl_stmt|;
DECL|field|innerDocuments
name|FixedBitSet
name|innerDocuments
decl_stmt|;
DECL|field|bottomSlot
name|int
name|bottomSlot
decl_stmt|;
DECL|method|NestedFieldComparator
name|NestedFieldComparator
parameter_list|(
name|FieldComparator
name|wrappedComparator
parameter_list|,
name|Filter
name|rootDocumentsFilter
parameter_list|,
name|Filter
name|innerDocumentsFilter
parameter_list|,
name|int
name|spareSlot
parameter_list|)
block|{
name|this
operator|.
name|wrappedComparator
operator|=
name|wrappedComparator
expr_stmt|;
name|this
operator|.
name|rootDocumentsFilter
operator|=
name|rootDocumentsFilter
expr_stmt|;
name|this
operator|.
name|innerDocumentsFilter
operator|=
name|innerDocumentsFilter
expr_stmt|;
name|this
operator|.
name|spareSlot
operator|=
name|spareSlot
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|compare
specifier|public
specifier|final
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
name|wrappedComparator
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
specifier|final
name|void
name|setBottom
parameter_list|(
name|int
name|slot
parameter_list|)
block|{
name|wrappedComparator
operator|.
name|setBottom
argument_list|(
name|slot
argument_list|)
expr_stmt|;
name|this
operator|.
name|bottomSlot
operator|=
name|slot
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|setNextReader
specifier|public
name|FieldComparator
name|setNextReader
parameter_list|(
name|AtomicReaderContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|DocIdSet
name|innerDocuments
init|=
name|innerDocumentsFilter
operator|.
name|getDocIdSet
argument_list|(
name|context
argument_list|,
literal|null
argument_list|)
decl_stmt|;
if|if
condition|(
name|DocIdSets
operator|.
name|isEmpty
argument_list|(
name|innerDocuments
argument_list|)
condition|)
block|{
name|this
operator|.
name|innerDocuments
operator|=
literal|null
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|innerDocuments
operator|instanceof
name|FixedBitSet
condition|)
block|{
name|this
operator|.
name|innerDocuments
operator|=
operator|(
name|FixedBitSet
operator|)
name|innerDocuments
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|innerDocuments
operator|=
name|DocIdSets
operator|.
name|toFixedBitSet
argument_list|(
name|innerDocuments
operator|.
name|iterator
argument_list|()
argument_list|,
name|context
operator|.
name|reader
argument_list|()
operator|.
name|maxDoc
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|DocIdSet
name|rootDocuments
init|=
name|rootDocumentsFilter
operator|.
name|getDocIdSet
argument_list|(
name|context
argument_list|,
literal|null
argument_list|)
decl_stmt|;
if|if
condition|(
name|DocIdSets
operator|.
name|isEmpty
argument_list|(
name|rootDocuments
argument_list|)
condition|)
block|{
name|this
operator|.
name|rootDocuments
operator|=
literal|null
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|rootDocuments
operator|instanceof
name|FixedBitSet
condition|)
block|{
name|this
operator|.
name|rootDocuments
operator|=
operator|(
name|FixedBitSet
operator|)
name|rootDocuments
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|rootDocuments
operator|=
name|DocIdSets
operator|.
name|toFixedBitSet
argument_list|(
name|rootDocuments
operator|.
name|iterator
argument_list|()
argument_list|,
name|context
operator|.
name|reader
argument_list|()
operator|.
name|maxDoc
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|wrappedComparator
operator|=
name|wrappedComparator
operator|.
name|setNextReader
argument_list|(
name|context
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
DECL|method|value
specifier|public
specifier|final
name|Object
name|value
parameter_list|(
name|int
name|slot
parameter_list|)
block|{
return|return
name|wrappedComparator
operator|.
name|value
argument_list|(
name|slot
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
name|rootDoc
parameter_list|,
name|Object
name|value
parameter_list|)
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"compareDocToValue() not used for sorting in ES"
argument_list|)
throw|;
block|}
DECL|class|Lowest
specifier|final
specifier|static
class|class
name|Lowest
extends|extends
name|NestedFieldComparator
block|{
DECL|method|Lowest
name|Lowest
parameter_list|(
name|FieldComparator
name|wrappedComparator
parameter_list|,
name|Filter
name|parentFilter
parameter_list|,
name|Filter
name|childFilter
parameter_list|,
name|int
name|spareSlot
parameter_list|)
block|{
name|super
argument_list|(
name|wrappedComparator
argument_list|,
name|parentFilter
argument_list|,
name|childFilter
argument_list|,
name|spareSlot
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|compareBottom
specifier|public
name|int
name|compareBottom
parameter_list|(
name|int
name|rootDoc
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|rootDoc
operator|==
literal|0
operator|||
name|rootDocuments
operator|==
literal|null
operator|||
name|innerDocuments
operator|==
literal|null
condition|)
block|{
return|return
name|compareBottomMissing
argument_list|(
name|wrappedComparator
argument_list|)
return|;
block|}
comment|// We need to copy the lowest value from all nested docs into slot.
name|int
name|prevRootDoc
init|=
name|rootDocuments
operator|.
name|prevSetBit
argument_list|(
name|rootDoc
operator|-
literal|1
argument_list|)
decl_stmt|;
name|int
name|nestedDoc
init|=
name|innerDocuments
operator|.
name|nextSetBit
argument_list|(
name|prevRootDoc
operator|+
literal|1
argument_list|)
decl_stmt|;
if|if
condition|(
name|nestedDoc
operator|>=
name|rootDoc
operator|||
name|nestedDoc
operator|==
operator|-
literal|1
condition|)
block|{
return|return
name|compareBottomMissing
argument_list|(
name|wrappedComparator
argument_list|)
return|;
block|}
comment|// We only need to emit a single cmp value for any matching nested doc
name|int
name|cmp
init|=
name|wrappedComparator
operator|.
name|compareBottom
argument_list|(
name|nestedDoc
argument_list|)
decl_stmt|;
if|if
condition|(
name|cmp
operator|>
literal|0
condition|)
block|{
return|return
name|cmp
return|;
block|}
while|while
condition|(
literal|true
condition|)
block|{
name|nestedDoc
operator|=
name|innerDocuments
operator|.
name|nextSetBit
argument_list|(
name|nestedDoc
operator|+
literal|1
argument_list|)
expr_stmt|;
if|if
condition|(
name|nestedDoc
operator|>=
name|rootDoc
operator|||
name|nestedDoc
operator|==
operator|-
literal|1
condition|)
block|{
return|return
name|cmp
return|;
block|}
name|int
name|cmp1
init|=
name|wrappedComparator
operator|.
name|compareBottom
argument_list|(
name|nestedDoc
argument_list|)
decl_stmt|;
if|if
condition|(
name|cmp1
operator|>
literal|0
condition|)
block|{
return|return
name|cmp1
return|;
block|}
else|else
block|{
if|if
condition|(
name|cmp1
operator|==
literal|0
condition|)
block|{
name|cmp
operator|=
literal|0
expr_stmt|;
block|}
block|}
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
name|rootDoc
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|rootDoc
operator|==
literal|0
operator|||
name|rootDocuments
operator|==
literal|null
operator|||
name|innerDocuments
operator|==
literal|null
condition|)
block|{
name|copyMissing
argument_list|(
name|wrappedComparator
argument_list|,
name|slot
argument_list|)
expr_stmt|;
return|return;
block|}
comment|// We need to copy the lowest value from all nested docs into slot.
name|int
name|prevRootDoc
init|=
name|rootDocuments
operator|.
name|prevSetBit
argument_list|(
name|rootDoc
operator|-
literal|1
argument_list|)
decl_stmt|;
name|int
name|nestedDoc
init|=
name|innerDocuments
operator|.
name|nextSetBit
argument_list|(
name|prevRootDoc
operator|+
literal|1
argument_list|)
decl_stmt|;
if|if
condition|(
name|nestedDoc
operator|>=
name|rootDoc
operator|||
name|nestedDoc
operator|==
operator|-
literal|1
condition|)
block|{
name|copyMissing
argument_list|(
name|wrappedComparator
argument_list|,
name|slot
argument_list|)
expr_stmt|;
return|return;
block|}
name|wrappedComparator
operator|.
name|copy
argument_list|(
name|spareSlot
argument_list|,
name|nestedDoc
argument_list|)
expr_stmt|;
name|wrappedComparator
operator|.
name|copy
argument_list|(
name|slot
argument_list|,
name|nestedDoc
argument_list|)
expr_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
name|nestedDoc
operator|=
name|innerDocuments
operator|.
name|nextSetBit
argument_list|(
name|nestedDoc
operator|+
literal|1
argument_list|)
expr_stmt|;
if|if
condition|(
name|nestedDoc
operator|>=
name|rootDoc
operator|||
name|nestedDoc
operator|==
operator|-
literal|1
condition|)
block|{
return|return;
block|}
name|wrappedComparator
operator|.
name|copy
argument_list|(
name|spareSlot
argument_list|,
name|nestedDoc
argument_list|)
expr_stmt|;
if|if
condition|(
name|wrappedComparator
operator|.
name|compare
argument_list|(
name|spareSlot
argument_list|,
name|slot
argument_list|)
operator|<
literal|0
condition|)
block|{
name|wrappedComparator
operator|.
name|copy
argument_list|(
name|slot
argument_list|,
name|nestedDoc
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
DECL|class|Highest
specifier|final
specifier|static
class|class
name|Highest
extends|extends
name|NestedFieldComparator
block|{
DECL|method|Highest
name|Highest
parameter_list|(
name|FieldComparator
name|wrappedComparator
parameter_list|,
name|Filter
name|parentFilter
parameter_list|,
name|Filter
name|childFilter
parameter_list|,
name|int
name|spareSlot
parameter_list|)
block|{
name|super
argument_list|(
name|wrappedComparator
argument_list|,
name|parentFilter
argument_list|,
name|childFilter
argument_list|,
name|spareSlot
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|compareBottom
specifier|public
name|int
name|compareBottom
parameter_list|(
name|int
name|rootDoc
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|rootDoc
operator|==
literal|0
operator|||
name|rootDocuments
operator|==
literal|null
operator|||
name|innerDocuments
operator|==
literal|null
condition|)
block|{
return|return
name|compareBottomMissing
argument_list|(
name|wrappedComparator
argument_list|)
return|;
block|}
name|int
name|prevRootDoc
init|=
name|rootDocuments
operator|.
name|prevSetBit
argument_list|(
name|rootDoc
operator|-
literal|1
argument_list|)
decl_stmt|;
name|int
name|nestedDoc
init|=
name|innerDocuments
operator|.
name|nextSetBit
argument_list|(
name|prevRootDoc
operator|+
literal|1
argument_list|)
decl_stmt|;
if|if
condition|(
name|nestedDoc
operator|>=
name|rootDoc
operator|||
name|nestedDoc
operator|==
operator|-
literal|1
condition|)
block|{
return|return
name|compareBottomMissing
argument_list|(
name|wrappedComparator
argument_list|)
return|;
block|}
name|int
name|cmp
init|=
name|wrappedComparator
operator|.
name|compareBottom
argument_list|(
name|nestedDoc
argument_list|)
decl_stmt|;
if|if
condition|(
name|cmp
operator|<
literal|0
condition|)
block|{
return|return
name|cmp
return|;
block|}
while|while
condition|(
literal|true
condition|)
block|{
name|nestedDoc
operator|=
name|innerDocuments
operator|.
name|nextSetBit
argument_list|(
name|nestedDoc
operator|+
literal|1
argument_list|)
expr_stmt|;
if|if
condition|(
name|nestedDoc
operator|>=
name|rootDoc
operator|||
name|nestedDoc
operator|==
operator|-
literal|1
condition|)
block|{
return|return
name|cmp
return|;
block|}
name|int
name|cmp1
init|=
name|wrappedComparator
operator|.
name|compareBottom
argument_list|(
name|nestedDoc
argument_list|)
decl_stmt|;
if|if
condition|(
name|cmp1
operator|<
literal|0
condition|)
block|{
return|return
name|cmp1
return|;
block|}
else|else
block|{
if|if
condition|(
name|cmp1
operator|==
literal|0
condition|)
block|{
name|cmp
operator|=
literal|0
expr_stmt|;
block|}
block|}
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
name|rootDoc
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|rootDoc
operator|==
literal|0
operator|||
name|rootDocuments
operator|==
literal|null
operator|||
name|innerDocuments
operator|==
literal|null
condition|)
block|{
name|copyMissing
argument_list|(
name|wrappedComparator
argument_list|,
name|slot
argument_list|)
expr_stmt|;
return|return;
block|}
name|int
name|prevRootDoc
init|=
name|rootDocuments
operator|.
name|prevSetBit
argument_list|(
name|rootDoc
operator|-
literal|1
argument_list|)
decl_stmt|;
name|int
name|nestedDoc
init|=
name|innerDocuments
operator|.
name|nextSetBit
argument_list|(
name|prevRootDoc
operator|+
literal|1
argument_list|)
decl_stmt|;
if|if
condition|(
name|nestedDoc
operator|>=
name|rootDoc
operator|||
name|nestedDoc
operator|==
operator|-
literal|1
condition|)
block|{
name|copyMissing
argument_list|(
name|wrappedComparator
argument_list|,
name|slot
argument_list|)
expr_stmt|;
return|return;
block|}
name|wrappedComparator
operator|.
name|copy
argument_list|(
name|spareSlot
argument_list|,
name|nestedDoc
argument_list|)
expr_stmt|;
name|wrappedComparator
operator|.
name|copy
argument_list|(
name|slot
argument_list|,
name|nestedDoc
argument_list|)
expr_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
name|nestedDoc
operator|=
name|innerDocuments
operator|.
name|nextSetBit
argument_list|(
name|nestedDoc
operator|+
literal|1
argument_list|)
expr_stmt|;
if|if
condition|(
name|nestedDoc
operator|>=
name|rootDoc
operator|||
name|nestedDoc
operator|==
operator|-
literal|1
condition|)
block|{
return|return;
block|}
name|wrappedComparator
operator|.
name|copy
argument_list|(
name|spareSlot
argument_list|,
name|nestedDoc
argument_list|)
expr_stmt|;
if|if
condition|(
name|wrappedComparator
operator|.
name|compare
argument_list|(
name|spareSlot
argument_list|,
name|slot
argument_list|)
operator|>
literal|0
condition|)
block|{
name|wrappedComparator
operator|.
name|copy
argument_list|(
name|slot
argument_list|,
name|nestedDoc
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
DECL|class|Sum
specifier|final
specifier|static
class|class
name|Sum
extends|extends
name|NestedFieldComparator
block|{
DECL|field|wrappedComparator
name|NumberComparatorBase
name|wrappedComparator
decl_stmt|;
DECL|method|Sum
name|Sum
parameter_list|(
name|NumberComparatorBase
name|wrappedComparator
parameter_list|,
name|Filter
name|rootDocumentsFilter
parameter_list|,
name|Filter
name|innerDocumentsFilter
parameter_list|,
name|int
name|spareSlot
parameter_list|)
block|{
name|super
argument_list|(
name|wrappedComparator
argument_list|,
name|rootDocumentsFilter
argument_list|,
name|innerDocumentsFilter
argument_list|,
name|spareSlot
argument_list|)
expr_stmt|;
name|this
operator|.
name|wrappedComparator
operator|=
name|wrappedComparator
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|compareBottom
specifier|public
name|int
name|compareBottom
parameter_list|(
name|int
name|rootDoc
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|rootDoc
operator|==
literal|0
operator|||
name|rootDocuments
operator|==
literal|null
operator|||
name|innerDocuments
operator|==
literal|null
condition|)
block|{
return|return
name|compareBottomMissing
argument_list|(
name|wrappedComparator
argument_list|)
return|;
block|}
name|int
name|prevRootDoc
init|=
name|rootDocuments
operator|.
name|prevSetBit
argument_list|(
name|rootDoc
operator|-
literal|1
argument_list|)
decl_stmt|;
name|int
name|nestedDoc
init|=
name|innerDocuments
operator|.
name|nextSetBit
argument_list|(
name|prevRootDoc
operator|+
literal|1
argument_list|)
decl_stmt|;
if|if
condition|(
name|nestedDoc
operator|>=
name|rootDoc
operator|||
name|nestedDoc
operator|==
operator|-
literal|1
condition|)
block|{
return|return
name|compareBottomMissing
argument_list|(
name|wrappedComparator
argument_list|)
return|;
block|}
name|wrappedComparator
operator|.
name|copy
argument_list|(
name|spareSlot
argument_list|,
name|nestedDoc
argument_list|)
expr_stmt|;
name|nestedDoc
operator|=
name|innerDocuments
operator|.
name|nextSetBit
argument_list|(
name|nestedDoc
operator|+
literal|1
argument_list|)
expr_stmt|;
while|while
condition|(
name|nestedDoc
operator|>
name|prevRootDoc
operator|&&
name|nestedDoc
operator|<
name|rootDoc
condition|)
block|{
name|wrappedComparator
operator|.
name|add
argument_list|(
name|spareSlot
argument_list|,
name|nestedDoc
argument_list|)
expr_stmt|;
name|nestedDoc
operator|=
name|innerDocuments
operator|.
name|nextSetBit
argument_list|(
name|nestedDoc
operator|+
literal|1
argument_list|)
expr_stmt|;
block|}
return|return
name|compare
argument_list|(
name|bottomSlot
argument_list|,
name|spareSlot
argument_list|)
return|;
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
name|rootDoc
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|rootDoc
operator|==
literal|0
operator|||
name|rootDocuments
operator|==
literal|null
operator|||
name|innerDocuments
operator|==
literal|null
condition|)
block|{
name|copyMissing
argument_list|(
name|wrappedComparator
argument_list|,
name|slot
argument_list|)
expr_stmt|;
return|return;
block|}
name|int
name|prevRootDoc
init|=
name|rootDocuments
operator|.
name|prevSetBit
argument_list|(
name|rootDoc
operator|-
literal|1
argument_list|)
decl_stmt|;
name|int
name|nestedDoc
init|=
name|innerDocuments
operator|.
name|nextSetBit
argument_list|(
name|prevRootDoc
operator|+
literal|1
argument_list|)
decl_stmt|;
if|if
condition|(
name|nestedDoc
operator|>=
name|rootDoc
operator|||
name|nestedDoc
operator|==
operator|-
literal|1
condition|)
block|{
name|copyMissing
argument_list|(
name|wrappedComparator
argument_list|,
name|slot
argument_list|)
expr_stmt|;
return|return;
block|}
name|wrappedComparator
operator|.
name|copy
argument_list|(
name|slot
argument_list|,
name|nestedDoc
argument_list|)
expr_stmt|;
name|nestedDoc
operator|=
name|innerDocuments
operator|.
name|nextSetBit
argument_list|(
name|nestedDoc
operator|+
literal|1
argument_list|)
expr_stmt|;
while|while
condition|(
name|nestedDoc
operator|>
name|prevRootDoc
operator|&&
name|nestedDoc
operator|<
name|rootDoc
condition|)
block|{
name|wrappedComparator
operator|.
name|add
argument_list|(
name|slot
argument_list|,
name|nestedDoc
argument_list|)
expr_stmt|;
name|nestedDoc
operator|=
name|innerDocuments
operator|.
name|nextSetBit
argument_list|(
name|nestedDoc
operator|+
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|setNextReader
specifier|public
name|FieldComparator
name|setNextReader
parameter_list|(
name|AtomicReaderContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|setNextReader
argument_list|(
name|context
argument_list|)
expr_stmt|;
name|wrappedComparator
operator|=
operator|(
name|NumberComparatorBase
operator|)
name|super
operator|.
name|wrappedComparator
expr_stmt|;
return|return
name|this
return|;
block|}
block|}
DECL|class|Avg
specifier|final
specifier|static
class|class
name|Avg
extends|extends
name|NestedFieldComparator
block|{
DECL|field|wrappedComparator
name|NumberComparatorBase
name|wrappedComparator
decl_stmt|;
DECL|method|Avg
name|Avg
parameter_list|(
name|NumberComparatorBase
name|wrappedComparator
parameter_list|,
name|Filter
name|rootDocumentsFilter
parameter_list|,
name|Filter
name|innerDocumentsFilter
parameter_list|,
name|int
name|spareSlot
parameter_list|)
block|{
name|super
argument_list|(
name|wrappedComparator
argument_list|,
name|rootDocumentsFilter
argument_list|,
name|innerDocumentsFilter
argument_list|,
name|spareSlot
argument_list|)
expr_stmt|;
name|this
operator|.
name|wrappedComparator
operator|=
name|wrappedComparator
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|compareBottom
specifier|public
name|int
name|compareBottom
parameter_list|(
name|int
name|rootDoc
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|rootDoc
operator|==
literal|0
operator|||
name|rootDocuments
operator|==
literal|null
operator|||
name|innerDocuments
operator|==
literal|null
condition|)
block|{
return|return
literal|0
return|;
block|}
name|int
name|prevRootDoc
init|=
name|rootDocuments
operator|.
name|prevSetBit
argument_list|(
name|rootDoc
operator|-
literal|1
argument_list|)
decl_stmt|;
name|int
name|nestedDoc
init|=
name|innerDocuments
operator|.
name|nextSetBit
argument_list|(
name|prevRootDoc
operator|+
literal|1
argument_list|)
decl_stmt|;
if|if
condition|(
name|nestedDoc
operator|>=
name|rootDoc
operator|||
name|nestedDoc
operator|==
operator|-
literal|1
condition|)
block|{
return|return
name|compareBottomMissing
argument_list|(
name|wrappedComparator
argument_list|)
return|;
block|}
name|int
name|counter
init|=
literal|1
decl_stmt|;
name|wrappedComparator
operator|.
name|copy
argument_list|(
name|spareSlot
argument_list|,
name|nestedDoc
argument_list|)
expr_stmt|;
name|nestedDoc
operator|=
name|innerDocuments
operator|.
name|nextSetBit
argument_list|(
name|nestedDoc
operator|+
literal|1
argument_list|)
expr_stmt|;
while|while
condition|(
name|nestedDoc
operator|>
name|prevRootDoc
operator|&&
name|nestedDoc
operator|<
name|rootDoc
condition|)
block|{
name|wrappedComparator
operator|.
name|add
argument_list|(
name|spareSlot
argument_list|,
name|nestedDoc
argument_list|)
expr_stmt|;
name|nestedDoc
operator|=
name|innerDocuments
operator|.
name|nextSetBit
argument_list|(
name|nestedDoc
operator|+
literal|1
argument_list|)
expr_stmt|;
name|counter
operator|++
expr_stmt|;
block|}
name|wrappedComparator
operator|.
name|divide
argument_list|(
name|spareSlot
argument_list|,
name|counter
argument_list|)
expr_stmt|;
return|return
name|compare
argument_list|(
name|bottomSlot
argument_list|,
name|spareSlot
argument_list|)
return|;
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
name|rootDoc
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|rootDoc
operator|==
literal|0
operator|||
name|rootDocuments
operator|==
literal|null
operator|||
name|innerDocuments
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|int
name|prevRootDoc
init|=
name|rootDocuments
operator|.
name|prevSetBit
argument_list|(
name|rootDoc
operator|-
literal|1
argument_list|)
decl_stmt|;
name|int
name|nestedDoc
init|=
name|innerDocuments
operator|.
name|nextSetBit
argument_list|(
name|prevRootDoc
operator|+
literal|1
argument_list|)
decl_stmt|;
if|if
condition|(
name|nestedDoc
operator|>=
name|rootDoc
operator|||
name|nestedDoc
operator|==
operator|-
literal|1
condition|)
block|{
name|copyMissing
argument_list|(
name|wrappedComparator
argument_list|,
name|slot
argument_list|)
expr_stmt|;
return|return;
block|}
name|int
name|counter
init|=
literal|1
decl_stmt|;
name|wrappedComparator
operator|.
name|copy
argument_list|(
name|slot
argument_list|,
name|nestedDoc
argument_list|)
expr_stmt|;
name|nestedDoc
operator|=
name|innerDocuments
operator|.
name|nextSetBit
argument_list|(
name|nestedDoc
operator|+
literal|1
argument_list|)
expr_stmt|;
while|while
condition|(
name|nestedDoc
operator|>
name|prevRootDoc
operator|&&
name|nestedDoc
operator|<
name|rootDoc
condition|)
block|{
name|wrappedComparator
operator|.
name|add
argument_list|(
name|slot
argument_list|,
name|nestedDoc
argument_list|)
expr_stmt|;
name|nestedDoc
operator|=
name|innerDocuments
operator|.
name|nextSetBit
argument_list|(
name|nestedDoc
operator|+
literal|1
argument_list|)
expr_stmt|;
name|counter
operator|++
expr_stmt|;
block|}
name|wrappedComparator
operator|.
name|divide
argument_list|(
name|slot
argument_list|,
name|counter
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|setNextReader
specifier|public
name|FieldComparator
name|setNextReader
parameter_list|(
name|AtomicReaderContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|setNextReader
argument_list|(
name|context
argument_list|)
expr_stmt|;
name|wrappedComparator
operator|=
operator|(
name|NumberComparatorBase
operator|)
name|super
operator|.
name|wrappedComparator
expr_stmt|;
return|return
name|this
return|;
block|}
block|}
DECL|method|copyMissing
specifier|static
specifier|final
name|void
name|copyMissing
parameter_list|(
name|FieldComparator
name|comparator
parameter_list|,
name|int
name|slot
parameter_list|)
block|{
if|if
condition|(
name|comparator
operator|instanceof
name|NumberComparatorBase
condition|)
block|{
operator|(
operator|(
name|NumberComparatorBase
operator|)
name|comparator
operator|)
operator|.
name|missing
argument_list|(
name|slot
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|compareBottomMissing
specifier|static
specifier|final
name|int
name|compareBottomMissing
parameter_list|(
name|FieldComparator
name|comparator
parameter_list|)
block|{
if|if
condition|(
name|comparator
operator|instanceof
name|NumberComparatorBase
condition|)
block|{
return|return
operator|(
operator|(
name|NumberComparatorBase
operator|)
name|comparator
operator|)
operator|.
name|compareBottomMissing
argument_list|()
return|;
block|}
else|else
block|{
return|return
literal|0
return|;
block|}
block|}
block|}
end_class

end_unit

