begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.mapper.geo
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
operator|.
name|geo
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|unit
operator|.
name|DistanceUnit
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
name|field
operator|.
name|data
operator|.
name|DocFieldData
import|;
end_import

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|GeoPointDocFieldData
specifier|public
class|class
name|GeoPointDocFieldData
extends|extends
name|DocFieldData
argument_list|<
name|GeoPointFieldData
argument_list|>
block|{
DECL|method|GeoPointDocFieldData
specifier|public
name|GeoPointDocFieldData
parameter_list|(
name|GeoPointFieldData
name|fieldData
parameter_list|)
block|{
name|super
argument_list|(
name|fieldData
argument_list|)
expr_stmt|;
block|}
DECL|method|getValue
specifier|public
name|GeoPoint
name|getValue
parameter_list|()
block|{
return|return
name|fieldData
operator|.
name|value
argument_list|(
name|docId
argument_list|)
return|;
block|}
DECL|method|getValues
specifier|public
name|GeoPoint
index|[]
name|getValues
parameter_list|()
block|{
return|return
name|fieldData
operator|.
name|values
argument_list|(
name|docId
argument_list|)
return|;
block|}
DECL|method|factorDistance
specifier|public
name|double
name|factorDistance
parameter_list|(
name|double
name|lat
parameter_list|,
name|double
name|lon
parameter_list|)
block|{
return|return
name|fieldData
operator|.
name|factorDistance
argument_list|(
name|docId
argument_list|,
name|DistanceUnit
operator|.
name|MILES
argument_list|,
name|lat
argument_list|,
name|lon
argument_list|)
return|;
block|}
DECL|method|arcDistance
specifier|public
name|double
name|arcDistance
parameter_list|(
name|double
name|lat
parameter_list|,
name|double
name|lon
parameter_list|)
block|{
return|return
name|fieldData
operator|.
name|arcDistance
argument_list|(
name|docId
argument_list|,
name|DistanceUnit
operator|.
name|MILES
argument_list|,
name|lat
argument_list|,
name|lon
argument_list|)
return|;
block|}
DECL|method|arcDistanceInKm
specifier|public
name|double
name|arcDistanceInKm
parameter_list|(
name|double
name|lat
parameter_list|,
name|double
name|lon
parameter_list|)
block|{
return|return
name|fieldData
operator|.
name|arcDistance
argument_list|(
name|docId
argument_list|,
name|DistanceUnit
operator|.
name|KILOMETERS
argument_list|,
name|lat
argument_list|,
name|lon
argument_list|)
return|;
block|}
DECL|method|distance
specifier|public
name|double
name|distance
parameter_list|(
name|double
name|lat
parameter_list|,
name|double
name|lon
parameter_list|)
block|{
return|return
name|fieldData
operator|.
name|distance
argument_list|(
name|docId
argument_list|,
name|DistanceUnit
operator|.
name|MILES
argument_list|,
name|lat
argument_list|,
name|lon
argument_list|)
return|;
block|}
DECL|method|distanceInKm
specifier|public
name|double
name|distanceInKm
parameter_list|(
name|double
name|lat
parameter_list|,
name|double
name|lon
parameter_list|)
block|{
return|return
name|fieldData
operator|.
name|distance
argument_list|(
name|docId
argument_list|,
name|DistanceUnit
operator|.
name|KILOMETERS
argument_list|,
name|lat
argument_list|,
name|lon
argument_list|)
return|;
block|}
DECL|method|geohashDistance
specifier|public
name|double
name|geohashDistance
parameter_list|(
name|String
name|geohash
parameter_list|)
block|{
return|return
name|fieldData
operator|.
name|distanceGeohash
argument_list|(
name|docId
argument_list|,
name|DistanceUnit
operator|.
name|MILES
argument_list|,
name|geohash
argument_list|)
return|;
block|}
DECL|method|geohashDistanceInKm
specifier|public
name|double
name|geohashDistanceInKm
parameter_list|(
name|String
name|geohash
parameter_list|)
block|{
return|return
name|fieldData
operator|.
name|distanceGeohash
argument_list|(
name|docId
argument_list|,
name|DistanceUnit
operator|.
name|KILOMETERS
argument_list|,
name|geohash
argument_list|)
return|;
block|}
DECL|method|getLat
specifier|public
name|double
name|getLat
parameter_list|()
block|{
return|return
name|fieldData
operator|.
name|latValue
argument_list|(
name|docId
argument_list|)
return|;
block|}
DECL|method|getLon
specifier|public
name|double
name|getLon
parameter_list|()
block|{
return|return
name|fieldData
operator|.
name|lonValue
argument_list|(
name|docId
argument_list|)
return|;
block|}
DECL|method|getLats
specifier|public
name|double
index|[]
name|getLats
parameter_list|()
block|{
return|return
name|fieldData
operator|.
name|latValues
argument_list|(
name|docId
argument_list|)
return|;
block|}
DECL|method|getLons
specifier|public
name|double
index|[]
name|getLons
parameter_list|()
block|{
return|return
name|fieldData
operator|.
name|lonValues
argument_list|(
name|docId
argument_list|)
return|;
block|}
block|}
end_class

end_unit

