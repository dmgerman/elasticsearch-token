begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|elasticsearch
operator|.
name|common
operator|.
name|bytes
operator|.
name|BytesArray
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
name|XContentHelper
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
name|MappedFieldType
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
name|query
operator|.
name|QueryParseContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|geo
operator|.
name|RandomGeoGenerator
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
name|Arrays
import|;
end_import

begin_class
DECL|class|GeoDistanceSortBuilderTests
specifier|public
class|class
name|GeoDistanceSortBuilderTests
extends|extends
name|AbstractSortTestCase
argument_list|<
name|GeoDistanceSortBuilder
argument_list|>
block|{
annotation|@
name|Override
DECL|method|createTestItem
specifier|protected
name|GeoDistanceSortBuilder
name|createTestItem
parameter_list|()
block|{
return|return
name|randomGeoDistanceSortBuilder
argument_list|()
return|;
block|}
DECL|method|randomGeoDistanceSortBuilder
specifier|public
specifier|static
name|GeoDistanceSortBuilder
name|randomGeoDistanceSortBuilder
parameter_list|()
block|{
name|String
name|fieldName
init|=
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|GeoDistanceSortBuilder
name|result
init|=
literal|null
decl_stmt|;
name|int
name|id
init|=
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|2
argument_list|)
decl_stmt|;
switch|switch
condition|(
name|id
condition|)
block|{
case|case
literal|0
case|:
name|int
name|count
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|String
index|[]
name|geohashes
init|=
operator|new
name|String
index|[
name|count
index|]
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
name|i
operator|++
control|)
block|{
name|geohashes
index|[
name|i
index|]
operator|=
name|RandomGeoGenerator
operator|.
name|randomPoint
argument_list|(
name|getRandom
argument_list|()
argument_list|)
operator|.
name|geohash
argument_list|()
expr_stmt|;
block|}
name|result
operator|=
operator|new
name|GeoDistanceSortBuilder
argument_list|(
name|fieldName
argument_list|,
name|geohashes
argument_list|)
expr_stmt|;
break|break;
case|case
literal|1
case|:
name|GeoPoint
name|pt
init|=
name|RandomGeoGenerator
operator|.
name|randomPoint
argument_list|(
name|getRandom
argument_list|()
argument_list|)
decl_stmt|;
name|result
operator|=
operator|new
name|GeoDistanceSortBuilder
argument_list|(
name|fieldName
argument_list|,
name|pt
operator|.
name|getLat
argument_list|()
argument_list|,
name|pt
operator|.
name|getLon
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
literal|2
case|:
name|result
operator|=
operator|new
name|GeoDistanceSortBuilder
argument_list|(
name|fieldName
argument_list|,
name|points
argument_list|(
operator|new
name|GeoPoint
index|[
literal|0
index|]
argument_list|)
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"one of three geo initialisation strategies must be used"
argument_list|)
throw|;
block|}
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|result
operator|.
name|geoDistance
argument_list|(
name|geoDistance
argument_list|(
name|result
operator|.
name|geoDistance
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|result
operator|.
name|unit
argument_list|(
name|unit
argument_list|(
name|result
operator|.
name|unit
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|result
operator|.
name|order
argument_list|(
name|RandomSortDataGenerator
operator|.
name|order
argument_list|(
literal|null
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|result
operator|.
name|sortMode
argument_list|(
name|mode
argument_list|(
name|result
operator|.
name|sortMode
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|result
operator|.
name|setNestedFilter
argument_list|(
name|RandomSortDataGenerator
operator|.
name|nestedFilter
argument_list|(
name|result
operator|.
name|getNestedFilter
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|result
operator|.
name|setNestedPath
argument_list|(
name|RandomSortDataGenerator
operator|.
name|randomAscii
argument_list|(
name|result
operator|.
name|getNestedPath
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|result
operator|.
name|coerce
argument_list|(
operator|!
name|result
operator|.
name|coerce
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|result
operator|.
name|ignoreMalformed
argument_list|(
operator|!
name|result
operator|.
name|ignoreMalformed
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
annotation|@
name|Override
DECL|method|provideMappedFieldType
specifier|protected
name|MappedFieldType
name|provideMappedFieldType
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|MappedFieldType
name|clone
init|=
name|GeoPointFieldMapper
operator|.
name|Defaults
operator|.
name|FIELD_TYPE
operator|.
name|clone
argument_list|()
decl_stmt|;
name|clone
operator|.
name|setName
argument_list|(
name|name
argument_list|)
expr_stmt|;
return|return
name|clone
return|;
block|}
DECL|method|mode
specifier|private
specifier|static
name|SortMode
name|mode
parameter_list|(
name|SortMode
name|original
parameter_list|)
block|{
name|SortMode
name|result
decl_stmt|;
do|do
block|{
name|result
operator|=
name|randomFrom
argument_list|(
name|SortMode
operator|.
name|values
argument_list|()
argument_list|)
expr_stmt|;
block|}
do|while
condition|(
name|result
operator|==
name|SortMode
operator|.
name|SUM
operator|||
name|result
operator|==
name|original
condition|)
do|;
return|return
name|result
return|;
block|}
DECL|method|unit
specifier|private
specifier|static
name|DistanceUnit
name|unit
parameter_list|(
name|DistanceUnit
name|original
parameter_list|)
block|{
name|int
name|id
init|=
operator|-
literal|1
decl_stmt|;
while|while
condition|(
name|id
operator|==
operator|-
literal|1
operator|||
operator|(
name|original
operator|!=
literal|null
operator|&&
name|original
operator|.
name|ordinal
argument_list|()
operator|==
name|id
operator|)
condition|)
block|{
name|id
operator|=
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
name|DistanceUnit
operator|.
name|values
argument_list|()
operator|.
name|length
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
return|return
name|DistanceUnit
operator|.
name|values
argument_list|()
index|[
name|id
index|]
return|;
block|}
DECL|method|points
specifier|private
specifier|static
name|GeoPoint
index|[]
name|points
parameter_list|(
name|GeoPoint
index|[]
name|original
parameter_list|)
block|{
name|GeoPoint
index|[]
name|result
init|=
literal|null
decl_stmt|;
while|while
condition|(
name|result
operator|==
literal|null
operator|||
name|Arrays
operator|.
name|deepEquals
argument_list|(
name|original
argument_list|,
name|result
argument_list|)
condition|)
block|{
name|int
name|count
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|result
operator|=
operator|new
name|GeoPoint
index|[
name|count
index|]
expr_stmt|;
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
name|i
operator|++
control|)
block|{
name|result
index|[
name|i
index|]
operator|=
name|RandomGeoGenerator
operator|.
name|randomPoint
argument_list|(
name|getRandom
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|result
return|;
block|}
DECL|method|geoDistance
specifier|private
specifier|static
name|GeoDistance
name|geoDistance
parameter_list|(
name|GeoDistance
name|original
parameter_list|)
block|{
name|int
name|id
init|=
operator|-
literal|1
decl_stmt|;
while|while
condition|(
name|id
operator|==
operator|-
literal|1
operator|||
operator|(
name|original
operator|!=
literal|null
operator|&&
name|original
operator|.
name|ordinal
argument_list|()
operator|==
name|id
operator|)
condition|)
block|{
name|id
operator|=
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
name|GeoDistance
operator|.
name|values
argument_list|()
operator|.
name|length
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
return|return
name|GeoDistance
operator|.
name|values
argument_list|()
index|[
name|id
index|]
return|;
block|}
annotation|@
name|Override
DECL|method|mutate
specifier|protected
name|GeoDistanceSortBuilder
name|mutate
parameter_list|(
name|GeoDistanceSortBuilder
name|original
parameter_list|)
throws|throws
name|IOException
block|{
name|GeoDistanceSortBuilder
name|result
init|=
operator|new
name|GeoDistanceSortBuilder
argument_list|(
name|original
argument_list|)
decl_stmt|;
name|int
name|parameter
init|=
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|9
argument_list|)
decl_stmt|;
switch|switch
condition|(
name|parameter
condition|)
block|{
case|case
literal|0
case|:
while|while
condition|(
name|Arrays
operator|.
name|deepEquals
argument_list|(
name|original
operator|.
name|points
argument_list|()
argument_list|,
name|result
operator|.
name|points
argument_list|()
argument_list|)
condition|)
block|{
name|GeoPoint
name|pt
init|=
name|RandomGeoGenerator
operator|.
name|randomPoint
argument_list|(
name|getRandom
argument_list|()
argument_list|)
decl_stmt|;
name|result
operator|.
name|point
argument_list|(
name|pt
operator|.
name|getLat
argument_list|()
argument_list|,
name|pt
operator|.
name|getLon
argument_list|()
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
literal|1
case|:
name|result
operator|.
name|points
argument_list|(
name|points
argument_list|(
name|original
operator|.
name|points
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
literal|2
case|:
name|result
operator|.
name|geoDistance
argument_list|(
name|geoDistance
argument_list|(
name|original
operator|.
name|geoDistance
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
literal|3
case|:
name|result
operator|.
name|unit
argument_list|(
name|unit
argument_list|(
name|original
operator|.
name|unit
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
literal|4
case|:
name|result
operator|.
name|order
argument_list|(
name|RandomSortDataGenerator
operator|.
name|order
argument_list|(
name|original
operator|.
name|order
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
literal|5
case|:
name|result
operator|.
name|sortMode
argument_list|(
name|mode
argument_list|(
name|original
operator|.
name|sortMode
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
literal|6
case|:
name|result
operator|.
name|setNestedFilter
argument_list|(
name|RandomSortDataGenerator
operator|.
name|nestedFilter
argument_list|(
name|original
operator|.
name|getNestedFilter
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
literal|7
case|:
name|result
operator|.
name|setNestedPath
argument_list|(
name|RandomSortDataGenerator
operator|.
name|randomAscii
argument_list|(
name|original
operator|.
name|getNestedPath
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
literal|8
case|:
name|result
operator|.
name|coerce
argument_list|(
operator|!
name|original
operator|.
name|coerce
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
literal|9
case|:
comment|// ignore malformed will only be set if coerce is set to true
name|result
operator|.
name|coerce
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|result
operator|.
name|ignoreMalformed
argument_list|(
operator|!
name|original
operator|.
name|ignoreMalformed
argument_list|()
argument_list|)
expr_stmt|;
break|break;
block|}
return|return
name|result
return|;
block|}
DECL|method|testSortModeSumIsRejectedInSetter
specifier|public
name|void
name|testSortModeSumIsRejectedInSetter
parameter_list|()
block|{
name|GeoDistanceSortBuilder
name|builder
init|=
operator|new
name|GeoDistanceSortBuilder
argument_list|(
literal|"testname"
argument_list|,
operator|-
literal|1
argument_list|,
operator|-
literal|1
argument_list|)
decl_stmt|;
name|GeoPoint
name|point
init|=
name|RandomGeoGenerator
operator|.
name|randomPoint
argument_list|(
name|getRandom
argument_list|()
argument_list|)
decl_stmt|;
name|builder
operator|.
name|point
argument_list|(
name|point
operator|.
name|getLat
argument_list|()
argument_list|,
name|point
operator|.
name|getLon
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
name|builder
operator|.
name|sortMode
argument_list|(
name|SortMode
operator|.
name|SUM
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"sort mode sum should not be supported"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
comment|// all good
block|}
block|}
DECL|method|testSortModeSumIsRejectedInJSON
specifier|public
name|void
name|testSortModeSumIsRejectedInJSON
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|json
init|=
literal|"{\n"
operator|+
literal|"  \"testname\" : [ {\n"
operator|+
literal|"    \"lat\" : -6.046997540714173,\n"
operator|+
literal|"    \"lon\" : -51.94128329747579\n"
operator|+
literal|"  } ],\n"
operator|+
literal|"  \"unit\" : \"m\",\n"
operator|+
literal|"  \"distance_type\" : \"sloppy_arc\",\n"
operator|+
literal|"  \"reverse\" : true,\n"
operator|+
literal|"  \"mode\" : \"SUM\",\n"
operator|+
literal|"  \"coerce\" : false,\n"
operator|+
literal|"  \"ignore_malformed\" : false\n"
operator|+
literal|"}"
decl_stmt|;
name|XContentParser
name|itemParser
init|=
name|XContentHelper
operator|.
name|createParser
argument_list|(
operator|new
name|BytesArray
argument_list|(
name|json
argument_list|)
argument_list|)
decl_stmt|;
name|itemParser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
name|QueryParseContext
name|context
init|=
operator|new
name|QueryParseContext
argument_list|(
name|indicesQueriesRegistry
argument_list|)
decl_stmt|;
name|context
operator|.
name|reset
argument_list|(
name|itemParser
argument_list|)
expr_stmt|;
try|try
block|{
name|GeoDistanceSortBuilder
operator|.
name|PROTOTYPE
operator|.
name|fromXContent
argument_list|(
name|context
argument_list|,
literal|""
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"sort mode sum should not be supported"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
comment|// all good
block|}
block|}
DECL|method|testGeoDistanceSortCanBeParsedFromGeoHash
specifier|public
name|void
name|testGeoDistanceSortCanBeParsedFromGeoHash
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|json
init|=
literal|"{\n"
operator|+
literal|"    \"VDcvDuFjE\" : [ \"7umzzv8eychg\", \"dmdgmt5z13uw\", "
operator|+
literal|"    \"ezu09wxw6v4c\", \"kc7s3515p6k6\", \"jgeuvjwrmfzn\", \"kcpcfj7ruyf8\" ],\n"
operator|+
literal|"    \"unit\" : \"m\",\n"
operator|+
literal|"    \"distance_type\" : \"sloppy_arc\",\n"
operator|+
literal|"    \"mode\" : \"MAX\",\n"
operator|+
literal|"    \"nested_filter\" : {\n"
operator|+
literal|"      \"ids\" : {\n"
operator|+
literal|"        \"type\" : [ ],\n"
operator|+
literal|"        \"values\" : [ ],\n"
operator|+
literal|"        \"boost\" : 5.711116\n"
operator|+
literal|"      }\n"
operator|+
literal|"    },\n"
operator|+
literal|"    \"coerce\" : false,\n"
operator|+
literal|"    \"ignore_malformed\" : true\n"
operator|+
literal|"  }"
decl_stmt|;
name|XContentParser
name|itemParser
init|=
name|XContentHelper
operator|.
name|createParser
argument_list|(
operator|new
name|BytesArray
argument_list|(
name|json
argument_list|)
argument_list|)
decl_stmt|;
name|itemParser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
name|QueryParseContext
name|context
init|=
operator|new
name|QueryParseContext
argument_list|(
name|indicesQueriesRegistry
argument_list|)
decl_stmt|;
name|context
operator|.
name|reset
argument_list|(
name|itemParser
argument_list|)
expr_stmt|;
name|GeoDistanceSortBuilder
name|result
init|=
name|GeoDistanceSortBuilder
operator|.
name|PROTOTYPE
operator|.
name|fromXContent
argument_list|(
name|context
argument_list|,
name|json
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"[-19.700583312660456, -2.8225036337971687, "
operator|+
literal|"31.537466906011105, -74.63590376079082, "
operator|+
literal|"43.71844606474042, -5.548660643398762, "
operator|+
literal|"-37.20467280596495, 38.71751043945551, "
operator|+
literal|"-69.44606635719538, 84.25200328230858, "
operator|+
literal|"-39.03717711567879, 44.74099852144718]"
argument_list|,
name|Arrays
operator|.
name|toString
argument_list|(
name|result
operator|.
name|points
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

