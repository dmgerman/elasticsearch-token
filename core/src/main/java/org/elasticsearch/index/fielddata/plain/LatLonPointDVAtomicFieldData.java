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
name|geo
operator|.
name|GeoEncodingUtils
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
name|SortedNumericDocValues
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
name|ArrayUtil
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
name|MultiGeoPointValues
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
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

begin_comment
comment|/**  * Created by nknize on 8/23/16.  */
end_comment

begin_class
DECL|class|LatLonPointDVAtomicFieldData
specifier|final
class|class
name|LatLonPointDVAtomicFieldData
extends|extends
name|AbstractAtomicGeoPointFieldData
block|{
DECL|field|values
specifier|private
specifier|final
name|SortedNumericDocValues
name|values
decl_stmt|;
DECL|method|LatLonPointDVAtomicFieldData
name|LatLonPointDVAtomicFieldData
parameter_list|(
name|SortedNumericDocValues
name|values
parameter_list|)
block|{
name|super
argument_list|()
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
DECL|method|ramBytesUsed
specifier|public
name|long
name|ramBytesUsed
parameter_list|()
block|{
return|return
literal|0
return|;
comment|// not exposed by lucene
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
return|return
name|Collections
operator|.
name|emptyList
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
block|{
comment|// noop
block|}
annotation|@
name|Override
DECL|method|getGeoPointValues
specifier|public
name|MultiGeoPointValues
name|getGeoPointValues
parameter_list|()
block|{
return|return
operator|new
name|MultiGeoPointValues
argument_list|()
block|{
name|GeoPoint
index|[]
name|points
init|=
operator|new
name|GeoPoint
index|[
literal|0
index|]
decl_stmt|;
specifier|private
name|int
name|count
init|=
literal|0
decl_stmt|;
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
name|values
operator|.
name|setDocument
argument_list|(
name|docId
argument_list|)
expr_stmt|;
name|count
operator|=
name|values
operator|.
name|count
argument_list|()
expr_stmt|;
if|if
condition|(
name|count
operator|>
name|points
operator|.
name|length
condition|)
block|{
specifier|final
name|int
name|previousLength
init|=
name|points
operator|.
name|length
decl_stmt|;
name|points
operator|=
name|Arrays
operator|.
name|copyOf
argument_list|(
name|points
argument_list|,
name|ArrayUtil
operator|.
name|oversize
argument_list|(
name|count
argument_list|,
name|RamUsageEstimator
operator|.
name|NUM_BYTES_OBJECT_REF
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
name|previousLength
init|;
name|i
operator|<
name|points
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|points
index|[
name|i
index|]
operator|=
operator|new
name|GeoPoint
argument_list|(
name|Double
operator|.
name|NaN
argument_list|,
name|Double
operator|.
name|NaN
argument_list|)
expr_stmt|;
block|}
block|}
name|long
name|encoded
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
name|count
condition|;
operator|++
name|i
control|)
block|{
name|encoded
operator|=
name|values
operator|.
name|valueAt
argument_list|(
name|i
argument_list|)
expr_stmt|;
name|points
index|[
name|i
index|]
operator|.
name|reset
argument_list|(
name|GeoEncodingUtils
operator|.
name|decodeLatitude
argument_list|(
call|(
name|int
call|)
argument_list|(
name|encoded
operator|>>>
literal|32
argument_list|)
argument_list|)
argument_list|,
name|GeoEncodingUtils
operator|.
name|decodeLongitude
argument_list|(
operator|(
name|int
operator|)
name|encoded
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|int
name|count
parameter_list|()
block|{
return|return
name|count
return|;
block|}
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
return|return
name|points
index|[
name|index
index|]
return|;
block|}
block|}
return|;
block|}
block|}
end_class

end_unit

