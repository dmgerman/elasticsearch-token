begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|index
operator|.
name|search
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
name|index
operator|.
name|search
operator|.
name|geo
operator|.
name|GeoDistanceDataComparator
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
name|search
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
name|search
operator|.
name|internal
operator|.
name|SearchContext
import|;
end_import

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|GeoDistanceSortParser
specifier|public
class|class
name|GeoDistanceSortParser
implements|implements
name|SortParser
block|{
DECL|method|names
annotation|@
name|Override
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
DECL|method|parse
annotation|@
name|Override
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
name|double
name|lat
init|=
name|Double
operator|.
name|NaN
decl_stmt|;
name|double
name|lon
init|=
name|Double
operator|.
name|NaN
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
name|lon
operator|=
name|parser
operator|.
name|doubleValue
argument_list|()
expr_stmt|;
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
name|lat
operator|=
name|parser
operator|.
name|doubleValue
argument_list|()
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
name|lat
operator|=
name|parser
operator|.
name|doubleValue
argument_list|()
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
name|lon
operator|=
name|parser
operator|.
name|doubleValue
argument_list|()
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
name|double
index|[]
name|values
init|=
name|GeoHashUtils
operator|.
name|decode
argument_list|(
name|parser
operator|.
name|text
argument_list|()
argument_list|)
decl_stmt|;
name|lat
operator|=
name|values
index|[
literal|0
index|]
expr_stmt|;
name|lon
operator|=
name|values
index|[
literal|1
index|]
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
else|else
block|{
comment|// assume the value is the actual value
name|String
name|value
init|=
name|parser
operator|.
name|text
argument_list|()
decl_stmt|;
name|int
name|comma
init|=
name|value
operator|.
name|indexOf
argument_list|(
literal|','
argument_list|)
decl_stmt|;
if|if
condition|(
name|comma
operator|!=
operator|-
literal|1
condition|)
block|{
name|lat
operator|=
name|Double
operator|.
name|parseDouble
argument_list|(
name|value
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|comma
argument_list|)
operator|.
name|trim
argument_list|()
argument_list|)
expr_stmt|;
name|lon
operator|=
name|Double
operator|.
name|parseDouble
argument_list|(
name|value
operator|.
name|substring
argument_list|(
name|comma
operator|+
literal|1
argument_list|)
operator|.
name|trim
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|double
index|[]
name|values
init|=
name|GeoHashUtils
operator|.
name|decode
argument_list|(
name|value
argument_list|)
decl_stmt|;
name|lat
operator|=
name|values
index|[
literal|0
index|]
expr_stmt|;
name|lon
operator|=
name|values
index|[
literal|1
index|]
expr_stmt|;
block|}
name|fieldName
operator|=
name|currentName
expr_stmt|;
block|}
block|}
block|}
return|return
operator|new
name|SortField
argument_list|(
name|fieldName
argument_list|,
name|GeoDistanceDataComparator
operator|.
name|comparatorSource
argument_list|(
name|fieldName
argument_list|,
name|lat
argument_list|,
name|lon
argument_list|,
name|unit
argument_list|,
name|geoDistance
argument_list|,
name|context
operator|.
name|fieldDataCache
argument_list|()
argument_list|,
name|context
operator|.
name|mapperService
argument_list|()
argument_list|)
argument_list|,
name|reverse
argument_list|)
return|;
block|}
block|}
end_class

end_unit

