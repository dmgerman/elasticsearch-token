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
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
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
name|RandomAccessOrds
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
name|Accountables
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
name|Accountable
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
name|PackedLongValues
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
comment|/**  */
end_comment

begin_class
DECL|class|PagedBytesAtomicFieldData
specifier|public
class|class
name|PagedBytesAtomicFieldData
extends|extends
name|AbstractAtomicOrdinalsFieldData
block|{
DECL|field|bytes
specifier|private
specifier|final
name|PagedBytes
operator|.
name|Reader
name|bytes
decl_stmt|;
DECL|field|termOrdToBytesOffset
specifier|private
specifier|final
name|PackedLongValues
name|termOrdToBytesOffset
decl_stmt|;
DECL|field|ordinals
specifier|protected
specifier|final
name|Ordinals
name|ordinals
decl_stmt|;
DECL|method|PagedBytesAtomicFieldData
specifier|public
name|PagedBytesAtomicFieldData
parameter_list|(
name|PagedBytes
operator|.
name|Reader
name|bytes
parameter_list|,
name|PackedLongValues
name|termOrdToBytesOffset
parameter_list|,
name|Ordinals
name|ordinals
parameter_list|)
block|{
name|this
operator|.
name|bytes
operator|=
name|bytes
expr_stmt|;
name|this
operator|.
name|termOrdToBytesOffset
operator|=
name|termOrdToBytesOffset
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
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
block|{     }
annotation|@
name|Override
DECL|method|ramBytesUsed
specifier|public
name|long
name|ramBytesUsed
parameter_list|()
block|{
name|long
name|size
init|=
name|ordinals
operator|.
name|ramBytesUsed
argument_list|()
decl_stmt|;
comment|// PackedBytes
name|size
operator|+=
name|bytes
operator|.
name|ramBytesUsed
argument_list|()
expr_stmt|;
comment|// PackedInts
name|size
operator|+=
name|termOrdToBytesOffset
operator|.
name|ramBytesUsed
argument_list|()
expr_stmt|;
return|return
name|size
return|;
block|}
annotation|@
name|Override
DECL|method|getChildResources
specifier|public
name|Iterable
argument_list|<
name|Accountable
argument_list|>
name|getChildResources
parameter_list|()
block|{
name|List
argument_list|<
name|Accountable
argument_list|>
name|resources
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|resources
operator|.
name|add
argument_list|(
name|Accountables
operator|.
name|namedAccountable
argument_list|(
literal|"ordinals"
argument_list|,
name|ordinals
argument_list|)
argument_list|)
expr_stmt|;
name|resources
operator|.
name|add
argument_list|(
name|Accountables
operator|.
name|namedAccountable
argument_list|(
literal|"term bytes"
argument_list|,
name|bytes
argument_list|)
argument_list|)
expr_stmt|;
name|resources
operator|.
name|add
argument_list|(
name|Accountables
operator|.
name|namedAccountable
argument_list|(
literal|"term offsets"
argument_list|,
name|termOrdToBytesOffset
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|Collections
operator|.
name|unmodifiableList
argument_list|(
name|resources
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|getOrdinalsValues
specifier|public
name|RandomAccessOrds
name|getOrdinalsValues
parameter_list|()
block|{
return|return
name|ordinals
operator|.
name|ordinals
argument_list|(
operator|new
name|ValuesHolder
argument_list|(
name|bytes
argument_list|,
name|termOrdToBytesOffset
argument_list|)
argument_list|)
return|;
block|}
DECL|class|ValuesHolder
specifier|private
specifier|static
class|class
name|ValuesHolder
implements|implements
name|Ordinals
operator|.
name|ValuesHolder
block|{
DECL|field|scratch
specifier|private
specifier|final
name|BytesRef
name|scratch
init|=
operator|new
name|BytesRef
argument_list|()
decl_stmt|;
DECL|field|bytes
specifier|private
specifier|final
name|PagedBytes
operator|.
name|Reader
name|bytes
decl_stmt|;
DECL|field|termOrdToBytesOffset
specifier|private
specifier|final
name|PackedLongValues
name|termOrdToBytesOffset
decl_stmt|;
DECL|method|ValuesHolder
name|ValuesHolder
parameter_list|(
name|PagedBytes
operator|.
name|Reader
name|bytes
parameter_list|,
name|PackedLongValues
name|termOrdToBytesOffset
parameter_list|)
block|{
name|this
operator|.
name|bytes
operator|=
name|bytes
expr_stmt|;
name|this
operator|.
name|termOrdToBytesOffset
operator|=
name|termOrdToBytesOffset
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|lookupOrd
specifier|public
name|BytesRef
name|lookupOrd
parameter_list|(
name|long
name|ord
parameter_list|)
block|{
assert|assert
name|ord
operator|>=
literal|0
assert|;
name|bytes
operator|.
name|fill
argument_list|(
name|scratch
argument_list|,
name|termOrdToBytesOffset
operator|.
name|get
argument_list|(
name|ord
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|scratch
return|;
block|}
block|}
block|}
end_class

end_unit

