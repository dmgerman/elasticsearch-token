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
name|index
operator|.
name|DocValues
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
name|index
operator|.
name|SortedDocValues
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
name|BitSet
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
name|PagedMutable
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
name|geo
operator|.
name|GeoPoint
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
name|FieldData
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
name|GeoPointValues
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
name|MultiGeoPointValues
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
name|mapper
operator|.
name|geo
operator|.
name|GeoPointFieldMapper
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
name|Collection
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

begin_comment
comment|/**  * Field data atomic impl for geo points with lossy compression.  */
end_comment

begin_class
DECL|class|GeoPointCompressedAtomicFieldData
specifier|public
specifier|abstract
class|class
name|GeoPointCompressedAtomicFieldData
extends|extends
name|AbstractAtomicGeoPointFieldData
block|{
annotation|@
name|Override
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
block|{     }
DECL|class|WithOrdinals
specifier|static
class|class
name|WithOrdinals
extends|extends
name|GeoPointCompressedAtomicFieldData
block|{
DECL|field|encoding
specifier|private
specifier|final
name|GeoPointFieldMapper
operator|.
name|Encoding
name|encoding
decl_stmt|;
DECL|field|lon
DECL|field|lat
specifier|private
specifier|final
name|PagedMutable
name|lon
decl_stmt|,
name|lat
decl_stmt|;
DECL|field|ordinals
specifier|private
specifier|final
name|Ordinals
name|ordinals
decl_stmt|;
DECL|field|maxDoc
specifier|private
specifier|final
name|int
name|maxDoc
decl_stmt|;
DECL|method|WithOrdinals
specifier|public
name|WithOrdinals
parameter_list|(
name|GeoPointFieldMapper
operator|.
name|Encoding
name|encoding
parameter_list|,
name|PagedMutable
name|lon
parameter_list|,
name|PagedMutable
name|lat
parameter_list|,
name|Ordinals
name|ordinals
parameter_list|,
name|int
name|maxDoc
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|encoding
operator|=
name|encoding
expr_stmt|;
name|this
operator|.
name|lon
operator|=
name|lon
expr_stmt|;
name|this
operator|.
name|lat
operator|=
name|lat
expr_stmt|;
name|this
operator|.
name|ordinals
operator|=
name|ordinals
expr_stmt|;
name|this
operator|.
name|maxDoc
operator|=
name|maxDoc
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|ramBytesUsed
specifier|public
name|long
name|ramBytesUsed
parameter_list|()
block|{
return|return
name|RamUsageEstimator
operator|.
name|NUM_BYTES_INT
comment|/*size*/
operator|+
name|lon
operator|.
name|ramBytesUsed
argument_list|()
operator|+
name|lat
operator|.
name|ramBytesUsed
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|getChildResources
specifier|public
name|Collection
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
literal|"latitude"
argument_list|,
name|lat
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
literal|"longitude"
argument_list|,
name|lon
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
DECL|method|getGeoPointValues
specifier|public
name|MultiGeoPointValues
name|getGeoPointValues
parameter_list|()
block|{
specifier|final
name|RandomAccessOrds
name|ords
init|=
name|ordinals
operator|.
name|ordinals
argument_list|()
decl_stmt|;
specifier|final
name|SortedDocValues
name|singleOrds
init|=
name|DocValues
operator|.
name|unwrapSingleton
argument_list|(
name|ords
argument_list|)
decl_stmt|;
if|if
condition|(
name|singleOrds
operator|!=
literal|null
condition|)
block|{
specifier|final
name|GeoPoint
name|point
init|=
operator|new
name|GeoPoint
argument_list|()
decl_stmt|;
specifier|final
name|GeoPointValues
name|values
init|=
operator|new
name|GeoPointValues
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|GeoPoint
name|get
parameter_list|(
name|int
name|docID
parameter_list|)
block|{
specifier|final
name|int
name|ord
init|=
name|singleOrds
operator|.
name|getOrd
argument_list|(
name|docID
argument_list|)
decl_stmt|;
if|if
condition|(
name|ord
operator|>=
literal|0
condition|)
block|{
name|encoding
operator|.
name|decode
argument_list|(
name|lat
operator|.
name|get
argument_list|(
name|ord
argument_list|)
argument_list|,
name|lon
operator|.
name|get
argument_list|(
name|ord
argument_list|)
argument_list|,
name|point
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|point
operator|.
name|reset
argument_list|(
literal|0
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
return|return
name|point
return|;
block|}
block|}
decl_stmt|;
return|return
name|FieldData
operator|.
name|singleton
argument_list|(
name|values
argument_list|,
name|DocValues
operator|.
name|docsWithValue
argument_list|(
name|singleOrds
argument_list|,
name|maxDoc
argument_list|)
argument_list|)
return|;
block|}
else|else
block|{
specifier|final
name|GeoPoint
name|point
init|=
operator|new
name|GeoPoint
argument_list|()
decl_stmt|;
return|return
operator|new
name|MultiGeoPointValues
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|GeoPoint
name|valueAt
parameter_list|(
name|int
name|index
parameter_list|)
block|{
specifier|final
name|long
name|ord
init|=
name|ords
operator|.
name|ordAt
argument_list|(
name|index
argument_list|)
decl_stmt|;
name|encoding
operator|.
name|decode
argument_list|(
name|lat
operator|.
name|get
argument_list|(
name|ord
argument_list|)
argument_list|,
name|lon
operator|.
name|get
argument_list|(
name|ord
argument_list|)
argument_list|,
name|point
argument_list|)
expr_stmt|;
return|return
name|point
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setDocument
parameter_list|(
name|int
name|docId
parameter_list|)
block|{
name|ords
operator|.
name|setDocument
argument_list|(
name|docId
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|count
parameter_list|()
block|{
return|return
name|ords
operator|.
name|cardinality
argument_list|()
return|;
block|}
block|}
return|;
block|}
block|}
block|}
comment|/**      * Assumes unset values are marked in bitset, and docId is used as the index to the value array.      */
DECL|class|Single
specifier|public
specifier|static
class|class
name|Single
extends|extends
name|GeoPointCompressedAtomicFieldData
block|{
DECL|field|encoding
specifier|private
specifier|final
name|GeoPointFieldMapper
operator|.
name|Encoding
name|encoding
decl_stmt|;
DECL|field|lon
DECL|field|lat
specifier|private
specifier|final
name|PagedMutable
name|lon
decl_stmt|,
name|lat
decl_stmt|;
DECL|field|set
specifier|private
specifier|final
name|BitSet
name|set
decl_stmt|;
DECL|method|Single
specifier|public
name|Single
parameter_list|(
name|GeoPointFieldMapper
operator|.
name|Encoding
name|encoding
parameter_list|,
name|PagedMutable
name|lon
parameter_list|,
name|PagedMutable
name|lat
parameter_list|,
name|BitSet
name|set
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|encoding
operator|=
name|encoding
expr_stmt|;
name|this
operator|.
name|lon
operator|=
name|lon
expr_stmt|;
name|this
operator|.
name|lat
operator|=
name|lat
expr_stmt|;
name|this
operator|.
name|set
operator|=
name|set
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|ramBytesUsed
specifier|public
name|long
name|ramBytesUsed
parameter_list|()
block|{
return|return
name|RamUsageEstimator
operator|.
name|NUM_BYTES_INT
comment|/*size*/
operator|+
name|lon
operator|.
name|ramBytesUsed
argument_list|()
operator|+
name|lat
operator|.
name|ramBytesUsed
argument_list|()
operator|+
operator|(
name|set
operator|==
literal|null
condition|?
literal|0
else|:
name|set
operator|.
name|ramBytesUsed
argument_list|()
operator|)
return|;
block|}
annotation|@
name|Override
DECL|method|getChildResources
specifier|public
name|Collection
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
literal|"latitude"
argument_list|,
name|lat
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
literal|"longitude"
argument_list|,
name|lon
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|set
operator|!=
literal|null
condition|)
block|{
name|resources
operator|.
name|add
argument_list|(
name|Accountables
operator|.
name|namedAccountable
argument_list|(
literal|"missing bitset"
argument_list|,
name|set
argument_list|)
argument_list|)
expr_stmt|;
block|}
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
DECL|method|getGeoPointValues
specifier|public
name|MultiGeoPointValues
name|getGeoPointValues
parameter_list|()
block|{
specifier|final
name|GeoPoint
name|point
init|=
operator|new
name|GeoPoint
argument_list|()
decl_stmt|;
specifier|final
name|GeoPointValues
name|values
init|=
operator|new
name|GeoPointValues
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|GeoPoint
name|get
parameter_list|(
name|int
name|docID
parameter_list|)
block|{
name|encoding
operator|.
name|decode
argument_list|(
name|lat
operator|.
name|get
argument_list|(
name|docID
argument_list|)
argument_list|,
name|lon
operator|.
name|get
argument_list|(
name|docID
argument_list|)
argument_list|,
name|point
argument_list|)
expr_stmt|;
return|return
name|point
return|;
block|}
block|}
decl_stmt|;
return|return
name|FieldData
operator|.
name|singleton
argument_list|(
name|values
argument_list|,
name|set
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit
