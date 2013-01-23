begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.sort
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|sort
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
name|search
operator|.
name|SortField
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
name|geo
operator|.
name|GeoDistance
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
name|GeoHashUtils
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
name|common
operator|.
name|geo
operator|.
name|GeoUtils
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
name|common
operator|.
name|xcontent
operator|.
name|XContentParser
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
name|IndexGeoPointFieldData
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
name|GeoDistanceComparatorSource
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
name|geo
operator|.
name|GeoPointFieldMapper
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|internal
operator|.
name|SearchContext
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|GeoDistanceSortParser
specifier|public
class|class
name|GeoDistanceSortParser
implements|implements
name|SortParser
block|{
annotation|@
name|Override
DECL|method|names
specifier|public
name|String
index|[]
name|names
parameter_list|()
block|{
return|return
operator|new
name|String
index|[]
block|{
literal|"_geo_distance"
block|,
literal|"_geoDistance"
block|}
return|;
block|}
annotation|@
name|Override
DECL|method|parse
specifier|public
name|SortField
name|parse
parameter_list|(
name|XContentParser
name|parser
parameter_list|,
name|SearchContext
name|context
parameter_list|)
throws|throws
name|Exception
block|{
name|String
name|fieldName
init|=
literal|null
decl_stmt|;
name|GeoPoint
name|point
init|=
operator|new
name|GeoPoint
argument_list|()
decl_stmt|;
name|DistanceUnit
name|unit
init|=
name|DistanceUnit
operator|.
name|KILOMETERS
decl_stmt|;
name|GeoDistance
name|geoDistance
init|=
name|GeoDistance
operator|.
name|ARC
decl_stmt|;
name|boolean
name|reverse
init|=
literal|false
decl_stmt|;
name|boolean
name|normalizeLon
init|=
literal|true
decl_stmt|;
name|boolean
name|normalizeLat
init|=
literal|true
decl_stmt|;
name|XContentParser
operator|.
name|Token
name|token
decl_stmt|;
name|String
name|currentName
init|=
name|parser
operator|.
name|currentName
argument_list|()
decl_stmt|;
while|while
condition|(
operator|(
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
operator|)
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|END_OBJECT
condition|)
block|{
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|FIELD_NAME
condition|)
block|{
name|currentName
operator|=
name|parser
operator|.
name|currentName
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|START_ARRAY
condition|)
block|{
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
name|point
operator|.
name|resetLon
argument_list|(
name|parser
operator|.
name|doubleValue
argument_list|()
argument_list|)
expr_stmt|;
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
name|point
operator|.
name|resetLat
argument_list|(
name|parser
operator|.
name|doubleValue
argument_list|()
argument_list|)
expr_stmt|;
while|while
condition|(
operator|(
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
operator|)
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|END_ARRAY
condition|)
block|{                  }
name|fieldName
operator|=
name|currentName
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|START_OBJECT
condition|)
block|{
comment|// the json in the format of -> field : { lat : 30, lon : 12 }
name|fieldName
operator|=
name|currentName
expr_stmt|;
while|while
condition|(
operator|(
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
operator|)
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|END_OBJECT
condition|)
block|{
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|FIELD_NAME
condition|)
block|{
name|currentName
operator|=
name|parser
operator|.
name|currentName
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|token
operator|.
name|isValue
argument_list|()
condition|)
block|{
if|if
condition|(
name|currentName
operator|.
name|equals
argument_list|(
name|GeoPointFieldMapper
operator|.
name|Names
operator|.
name|LAT
argument_list|)
condition|)
block|{
name|point
operator|.
name|resetLat
argument_list|(
name|parser
operator|.
name|doubleValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|currentName
operator|.
name|equals
argument_list|(
name|GeoPointFieldMapper
operator|.
name|Names
operator|.
name|LON
argument_list|)
condition|)
block|{
name|point
operator|.
name|resetLon
argument_list|(
name|parser
operator|.
name|doubleValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|currentName
operator|.
name|equals
argument_list|(
name|GeoPointFieldMapper
operator|.
name|Names
operator|.
name|GEOHASH
argument_list|)
condition|)
block|{
name|GeoHashUtils
operator|.
name|decode
argument_list|(
name|parser
operator|.
name|text
argument_list|()
argument_list|,
name|point
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
elseif|else
if|if
condition|(
name|token
operator|.
name|isValue
argument_list|()
condition|)
block|{
if|if
condition|(
literal|"reverse"
operator|.
name|equals
argument_list|(
name|currentName
argument_list|)
condition|)
block|{
name|reverse
operator|=
name|parser
operator|.
name|booleanValue
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"order"
operator|.
name|equals
argument_list|(
name|currentName
argument_list|)
condition|)
block|{
name|reverse
operator|=
literal|"desc"
operator|.
name|equals
argument_list|(
name|parser
operator|.
name|text
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|currentName
operator|.
name|equals
argument_list|(
literal|"unit"
argument_list|)
condition|)
block|{
name|unit
operator|=
name|DistanceUnit
operator|.
name|fromString
argument_list|(
name|parser
operator|.
name|text
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|currentName
operator|.
name|equals
argument_list|(
literal|"distance_type"
argument_list|)
operator|||
name|currentName
operator|.
name|equals
argument_list|(
literal|"distanceType"
argument_list|)
condition|)
block|{
name|geoDistance
operator|=
name|GeoDistance
operator|.
name|fromString
argument_list|(
name|parser
operator|.
name|text
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"normalize"
operator|.
name|equals
argument_list|(
name|currentName
argument_list|)
condition|)
block|{
name|normalizeLat
operator|=
name|parser
operator|.
name|booleanValue
argument_list|()
expr_stmt|;
name|normalizeLon
operator|=
name|parser
operator|.
name|booleanValue
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|point
operator|.
name|resetFromString
argument_list|(
name|parser
operator|.
name|text
argument_list|()
argument_list|)
expr_stmt|;
name|fieldName
operator|=
name|currentName
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|normalizeLat
operator|||
name|normalizeLon
condition|)
block|{
name|GeoUtils
operator|.
name|normalizePoint
argument_list|(
name|point
argument_list|,
name|normalizeLat
argument_list|,
name|normalizeLon
argument_list|)
expr_stmt|;
block|}
name|FieldMapper
name|mapper
init|=
name|context
operator|.
name|smartNameFieldMapper
argument_list|(
name|fieldName
argument_list|)
decl_stmt|;
if|if
condition|(
name|mapper
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ElasticSearchIllegalArgumentException
argument_list|(
literal|"failed to find mapper for ["
operator|+
name|fieldName
operator|+
literal|"] for geo distance based sort"
argument_list|)
throw|;
block|}
name|IndexGeoPointFieldData
name|indexFieldData
init|=
name|context
operator|.
name|fieldData
argument_list|()
operator|.
name|getForField
argument_list|(
name|mapper
argument_list|)
decl_stmt|;
return|return
operator|new
name|SortField
argument_list|(
name|fieldName
argument_list|,
operator|new
name|GeoDistanceComparatorSource
argument_list|(
name|indexFieldData
argument_list|,
name|point
operator|.
name|lat
argument_list|()
argument_list|,
name|point
operator|.
name|lon
argument_list|()
argument_list|,
name|unit
argument_list|,
name|geoDistance
argument_list|)
argument_list|,
name|reverse
argument_list|)
return|;
block|}
block|}
end_class

end_unit

