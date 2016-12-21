begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.query
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
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
name|MatchNoDocsQuery
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
name|Query
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
name|ParsingException
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
name|search
operator|.
name|internal
operator|.
name|SearchContext
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
name|AbstractQueryTestCase
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
name|RandomShapeGenerator
import|;
end_import

begin_import
import|import
name|org
operator|.
name|locationtech
operator|.
name|spatial4j
operator|.
name|shape
operator|.
name|Point
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
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|CoreMatchers
operator|.
name|containsString
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|CoreMatchers
operator|.
name|instanceOf
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|CoreMatchers
operator|.
name|notNullValue
import|;
end_import

begin_class
DECL|class|GeoDistanceQueryBuilderTests
specifier|public
class|class
name|GeoDistanceQueryBuilderTests
extends|extends
name|AbstractQueryTestCase
argument_list|<
name|GeoDistanceQueryBuilder
argument_list|>
block|{
annotation|@
name|Override
DECL|method|doCreateTestQueryBuilder
specifier|protected
name|GeoDistanceQueryBuilder
name|doCreateTestQueryBuilder
parameter_list|()
block|{
name|GeoDistanceQueryBuilder
name|qb
init|=
operator|new
name|GeoDistanceQueryBuilder
argument_list|(
name|GEO_POINT_FIELD_NAME
argument_list|)
decl_stmt|;
name|String
name|distance
init|=
literal|""
operator|+
name|randomDouble
argument_list|()
decl_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|DistanceUnit
name|unit
init|=
name|randomFrom
argument_list|(
name|DistanceUnit
operator|.
name|values
argument_list|()
argument_list|)
decl_stmt|;
name|distance
operator|=
name|distance
operator|+
name|unit
operator|.
name|toString
argument_list|()
expr_stmt|;
block|}
name|int
name|selector
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
name|selector
condition|)
block|{
case|case
literal|0
case|:
name|qb
operator|.
name|distance
argument_list|(
name|randomDouble
argument_list|()
argument_list|,
name|randomFrom
argument_list|(
name|DistanceUnit
operator|.
name|values
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
literal|1
case|:
name|qb
operator|.
name|distance
argument_list|(
name|distance
argument_list|,
name|randomFrom
argument_list|(
name|DistanceUnit
operator|.
name|values
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
literal|2
case|:
name|qb
operator|.
name|distance
argument_list|(
name|distance
argument_list|)
expr_stmt|;
break|break;
block|}
name|Point
name|p
init|=
name|RandomShapeGenerator
operator|.
name|xRandomPoint
argument_list|(
name|random
argument_list|()
argument_list|)
decl_stmt|;
name|qb
operator|.
name|point
argument_list|(
operator|new
name|GeoPoint
argument_list|(
name|p
operator|.
name|getY
argument_list|()
argument_list|,
name|p
operator|.
name|getX
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|qb
operator|.
name|setValidationMethod
argument_list|(
name|randomFrom
argument_list|(
name|GeoValidationMethod
operator|.
name|values
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
name|qb
operator|.
name|geoDistance
argument_list|(
name|randomFrom
argument_list|(
name|GeoDistance
operator|.
name|values
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
name|qb
operator|.
name|ignoreUnmapped
argument_list|(
name|randomBoolean
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|qb
return|;
block|}
DECL|method|testIllegalValues
specifier|public
name|void
name|testIllegalValues
parameter_list|()
block|{
name|IllegalArgumentException
name|e
init|=
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
operator|new
name|GeoDistanceQueryBuilder
argument_list|(
literal|""
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"fieldName must not be null or empty"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|e
operator|=
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
operator|new
name|GeoDistanceQueryBuilder
argument_list|(
operator|(
name|String
operator|)
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"fieldName must not be null or empty"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|GeoDistanceQueryBuilder
name|query
init|=
operator|new
name|GeoDistanceQueryBuilder
argument_list|(
literal|"fieldName"
argument_list|)
decl_stmt|;
name|e
operator|=
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|query
operator|.
name|distance
argument_list|(
literal|""
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"distance must not be null or empty"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|e
operator|=
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|query
operator|.
name|distance
argument_list|(
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"distance must not be null or empty"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|e
operator|=
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|query
operator|.
name|distance
argument_list|(
literal|""
argument_list|,
name|DistanceUnit
operator|.
name|DEFAULT
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"distance must not be null or empty"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|e
operator|=
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|query
operator|.
name|distance
argument_list|(
literal|null
argument_list|,
name|DistanceUnit
operator|.
name|DEFAULT
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"distance must not be null or empty"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|e
operator|=
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|query
operator|.
name|distance
argument_list|(
literal|"1"
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"distance unit must not be null"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|e
operator|=
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|query
operator|.
name|distance
argument_list|(
literal|1
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"distance unit must not be null"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|e
operator|=
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|query
operator|.
name|distance
argument_list|(
name|randomIntBetween
argument_list|(
name|Integer
operator|.
name|MIN_VALUE
argument_list|,
literal|0
argument_list|)
argument_list|,
name|DistanceUnit
operator|.
name|DEFAULT
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"distance must be greater than zero"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|e
operator|=
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|query
operator|.
name|geohash
argument_list|(
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"geohash must not be null or empty"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|e
operator|=
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|query
operator|.
name|geohash
argument_list|(
literal|""
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"geohash must not be null or empty"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|e
operator|=
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|query
operator|.
name|geoDistance
argument_list|(
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"geoDistance must not be null"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**      * Overridden here to ensure the test is only run if at least one type is      * present in the mappings. Geo queries do not execute if the field is not      * explicitly mapped      */
annotation|@
name|Override
DECL|method|testToQuery
specifier|public
name|void
name|testToQuery
parameter_list|()
throws|throws
name|IOException
block|{
name|assumeTrue
argument_list|(
literal|"test runs only when at least a type is registered"
argument_list|,
name|getCurrentTypes
argument_list|()
operator|.
name|length
operator|>
literal|0
argument_list|)
expr_stmt|;
name|super
operator|.
name|testToQuery
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doAssertLuceneQuery
specifier|protected
name|void
name|doAssertLuceneQuery
parameter_list|(
name|GeoDistanceQueryBuilder
name|queryBuilder
parameter_list|,
name|Query
name|query
parameter_list|,
name|SearchContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
comment|// TODO: what can we check
block|}
DECL|method|testParsingAndToQuery1
specifier|public
name|void
name|testParsingAndToQuery1
parameter_list|()
throws|throws
name|IOException
block|{
name|assumeTrue
argument_list|(
literal|"test runs only when at least a type is registered"
argument_list|,
name|getCurrentTypes
argument_list|()
operator|.
name|length
operator|>
literal|0
argument_list|)
expr_stmt|;
name|String
name|query
init|=
literal|"{\n"
operator|+
literal|"    \"geo_distance\":{\n"
operator|+
literal|"        \"distance\":\"12mi\",\n"
operator|+
literal|"        \""
operator|+
name|GEO_POINT_FIELD_NAME
operator|+
literal|"\":{\n"
operator|+
literal|"            \"lat\":40,\n"
operator|+
literal|"            \"lon\":-70\n"
operator|+
literal|"        }\n"
operator|+
literal|"    }\n"
operator|+
literal|"}\n"
decl_stmt|;
name|assertGeoDistanceRangeQuery
argument_list|(
name|query
argument_list|,
literal|40
argument_list|,
operator|-
literal|70
argument_list|,
literal|12
argument_list|,
name|DistanceUnit
operator|.
name|DEFAULT
argument_list|)
expr_stmt|;
block|}
DECL|method|testParsingAndToQuery2
specifier|public
name|void
name|testParsingAndToQuery2
parameter_list|()
throws|throws
name|IOException
block|{
name|assumeTrue
argument_list|(
literal|"test runs only when at least a type is registered"
argument_list|,
name|getCurrentTypes
argument_list|()
operator|.
name|length
operator|>
literal|0
argument_list|)
expr_stmt|;
name|String
name|query
init|=
literal|"{\n"
operator|+
literal|"    \"geo_distance\":{\n"
operator|+
literal|"        \"distance\":\"12mi\",\n"
operator|+
literal|"        \""
operator|+
name|GEO_POINT_FIELD_NAME
operator|+
literal|"\":[-70, 40]\n"
operator|+
literal|"    }\n"
operator|+
literal|"}\n"
decl_stmt|;
name|assertGeoDistanceRangeQuery
argument_list|(
name|query
argument_list|,
literal|40
argument_list|,
operator|-
literal|70
argument_list|,
literal|12
argument_list|,
name|DistanceUnit
operator|.
name|DEFAULT
argument_list|)
expr_stmt|;
block|}
DECL|method|testParsingAndToQuery3
specifier|public
name|void
name|testParsingAndToQuery3
parameter_list|()
throws|throws
name|IOException
block|{
name|assumeTrue
argument_list|(
literal|"test runs only when at least a type is registered"
argument_list|,
name|getCurrentTypes
argument_list|()
operator|.
name|length
operator|>
literal|0
argument_list|)
expr_stmt|;
name|String
name|query
init|=
literal|"{\n"
operator|+
literal|"    \"geo_distance\":{\n"
operator|+
literal|"        \"distance\":\"12mi\",\n"
operator|+
literal|"        \""
operator|+
name|GEO_POINT_FIELD_NAME
operator|+
literal|"\":\"40, -70\"\n"
operator|+
literal|"    }\n"
operator|+
literal|"}\n"
decl_stmt|;
name|assertGeoDistanceRangeQuery
argument_list|(
name|query
argument_list|,
literal|40
argument_list|,
operator|-
literal|70
argument_list|,
literal|12
argument_list|,
name|DistanceUnit
operator|.
name|DEFAULT
argument_list|)
expr_stmt|;
block|}
DECL|method|testParsingAndToQuery4
specifier|public
name|void
name|testParsingAndToQuery4
parameter_list|()
throws|throws
name|IOException
block|{
name|assumeTrue
argument_list|(
literal|"test runs only when at least a type is registered"
argument_list|,
name|getCurrentTypes
argument_list|()
operator|.
name|length
operator|>
literal|0
argument_list|)
expr_stmt|;
name|String
name|query
init|=
literal|"{\n"
operator|+
literal|"    \"geo_distance\":{\n"
operator|+
literal|"        \"distance\":\"12mi\",\n"
operator|+
literal|"        \""
operator|+
name|GEO_POINT_FIELD_NAME
operator|+
literal|"\":\"drn5x1g8cu2y\"\n"
operator|+
literal|"    }\n"
operator|+
literal|"}\n"
decl_stmt|;
name|assertGeoDistanceRangeQuery
argument_list|(
name|query
argument_list|,
literal|40
argument_list|,
operator|-
literal|70
argument_list|,
literal|12
argument_list|,
name|DistanceUnit
operator|.
name|DEFAULT
argument_list|)
expr_stmt|;
block|}
DECL|method|testParsingAndToQuery5
specifier|public
name|void
name|testParsingAndToQuery5
parameter_list|()
throws|throws
name|IOException
block|{
name|assumeTrue
argument_list|(
literal|"test runs only when at least a type is registered"
argument_list|,
name|getCurrentTypes
argument_list|()
operator|.
name|length
operator|>
literal|0
argument_list|)
expr_stmt|;
name|String
name|query
init|=
literal|"{\n"
operator|+
literal|"    \"geo_distance\":{\n"
operator|+
literal|"        \"distance\":12,\n"
operator|+
literal|"        \"unit\":\"mi\",\n"
operator|+
literal|"        \""
operator|+
name|GEO_POINT_FIELD_NAME
operator|+
literal|"\":{\n"
operator|+
literal|"            \"lat\":40,\n"
operator|+
literal|"            \"lon\":-70\n"
operator|+
literal|"        }\n"
operator|+
literal|"    }\n"
operator|+
literal|"}\n"
decl_stmt|;
name|assertGeoDistanceRangeQuery
argument_list|(
name|query
argument_list|,
literal|40
argument_list|,
operator|-
literal|70
argument_list|,
literal|12
argument_list|,
name|DistanceUnit
operator|.
name|DEFAULT
argument_list|)
expr_stmt|;
block|}
DECL|method|testParsingAndToQuery6
specifier|public
name|void
name|testParsingAndToQuery6
parameter_list|()
throws|throws
name|IOException
block|{
name|assumeTrue
argument_list|(
literal|"test runs only when at least a type is registered"
argument_list|,
name|getCurrentTypes
argument_list|()
operator|.
name|length
operator|>
literal|0
argument_list|)
expr_stmt|;
name|String
name|query
init|=
literal|"{\n"
operator|+
literal|"    \"geo_distance\":{\n"
operator|+
literal|"        \"distance\":\"12\",\n"
operator|+
literal|"        \"unit\":\"mi\",\n"
operator|+
literal|"        \""
operator|+
name|GEO_POINT_FIELD_NAME
operator|+
literal|"\":{\n"
operator|+
literal|"            \"lat\":40,\n"
operator|+
literal|"            \"lon\":-70\n"
operator|+
literal|"        }\n"
operator|+
literal|"    }\n"
operator|+
literal|"}\n"
decl_stmt|;
name|assertGeoDistanceRangeQuery
argument_list|(
name|query
argument_list|,
literal|40
argument_list|,
operator|-
literal|70
argument_list|,
literal|12
argument_list|,
name|DistanceUnit
operator|.
name|DEFAULT
argument_list|)
expr_stmt|;
block|}
DECL|method|testParsingAndToQuery7
specifier|public
name|void
name|testParsingAndToQuery7
parameter_list|()
throws|throws
name|IOException
block|{
name|assumeTrue
argument_list|(
literal|"test runs only when at least a type is registered"
argument_list|,
name|getCurrentTypes
argument_list|()
operator|.
name|length
operator|>
literal|0
argument_list|)
expr_stmt|;
name|String
name|query
init|=
literal|"{\n"
operator|+
literal|"  \"geo_distance\":{\n"
operator|+
literal|"      \"distance\":\"19.312128\",\n"
operator|+
literal|"      \""
operator|+
name|GEO_POINT_FIELD_NAME
operator|+
literal|"\":{\n"
operator|+
literal|"          \"lat\":40,\n"
operator|+
literal|"          \"lon\":-70\n"
operator|+
literal|"      }\n"
operator|+
literal|"  }\n"
operator|+
literal|"}\n"
decl_stmt|;
name|assertGeoDistanceRangeQuery
argument_list|(
name|query
argument_list|,
literal|40
argument_list|,
operator|-
literal|70
argument_list|,
literal|0.012
argument_list|,
name|DistanceUnit
operator|.
name|DEFAULT
argument_list|)
expr_stmt|;
block|}
DECL|method|testParsingAndToQuery8
specifier|public
name|void
name|testParsingAndToQuery8
parameter_list|()
throws|throws
name|IOException
block|{
name|assumeTrue
argument_list|(
literal|"test runs only when at least a type is registered"
argument_list|,
name|getCurrentTypes
argument_list|()
operator|.
name|length
operator|>
literal|0
argument_list|)
expr_stmt|;
name|String
name|query
init|=
literal|"{\n"
operator|+
literal|"    \"geo_distance\":{\n"
operator|+
literal|"        \"distance\":19.312128,\n"
operator|+
literal|"        \""
operator|+
name|GEO_POINT_FIELD_NAME
operator|+
literal|"\":{\n"
operator|+
literal|"            \"lat\":40,\n"
operator|+
literal|"            \"lon\":-70\n"
operator|+
literal|"        }\n"
operator|+
literal|"    }\n"
operator|+
literal|"}\n"
decl_stmt|;
name|assertGeoDistanceRangeQuery
argument_list|(
name|query
argument_list|,
literal|40
argument_list|,
operator|-
literal|70
argument_list|,
literal|12
argument_list|,
name|DistanceUnit
operator|.
name|KILOMETERS
argument_list|)
expr_stmt|;
block|}
DECL|method|testParsingAndToQuery9
specifier|public
name|void
name|testParsingAndToQuery9
parameter_list|()
throws|throws
name|IOException
block|{
name|assumeTrue
argument_list|(
literal|"test runs only when at least a type is registered"
argument_list|,
name|getCurrentTypes
argument_list|()
operator|.
name|length
operator|>
literal|0
argument_list|)
expr_stmt|;
name|String
name|query
init|=
literal|"{\n"
operator|+
literal|"    \"geo_distance\":{\n"
operator|+
literal|"        \"distance\":\"19.312128\",\n"
operator|+
literal|"        \"unit\":\"km\",\n"
operator|+
literal|"        \""
operator|+
name|GEO_POINT_FIELD_NAME
operator|+
literal|"\":{\n"
operator|+
literal|"            \"lat\":40,\n"
operator|+
literal|"            \"lon\":-70\n"
operator|+
literal|"        }\n"
operator|+
literal|"    }\n"
operator|+
literal|"}\n"
decl_stmt|;
name|assertGeoDistanceRangeQuery
argument_list|(
name|query
argument_list|,
literal|40
argument_list|,
operator|-
literal|70
argument_list|,
literal|12
argument_list|,
name|DistanceUnit
operator|.
name|DEFAULT
argument_list|)
expr_stmt|;
block|}
DECL|method|testParsingAndToQuery10
specifier|public
name|void
name|testParsingAndToQuery10
parameter_list|()
throws|throws
name|IOException
block|{
name|assumeTrue
argument_list|(
literal|"test runs only when at least a type is registered"
argument_list|,
name|getCurrentTypes
argument_list|()
operator|.
name|length
operator|>
literal|0
argument_list|)
expr_stmt|;
name|String
name|query
init|=
literal|"{\n"
operator|+
literal|"    \"geo_distance\":{\n"
operator|+
literal|"        \"distance\":19.312128,\n"
operator|+
literal|"        \"unit\":\"km\",\n"
operator|+
literal|"        \""
operator|+
name|GEO_POINT_FIELD_NAME
operator|+
literal|"\":{\n"
operator|+
literal|"            \"lat\":40,\n"
operator|+
literal|"            \"lon\":-70\n"
operator|+
literal|"        }\n"
operator|+
literal|"    }\n"
operator|+
literal|"}\n"
decl_stmt|;
name|assertGeoDistanceRangeQuery
argument_list|(
name|query
argument_list|,
literal|40
argument_list|,
operator|-
literal|70
argument_list|,
literal|12
argument_list|,
name|DistanceUnit
operator|.
name|DEFAULT
argument_list|)
expr_stmt|;
block|}
DECL|method|testParsingAndToQuery11
specifier|public
name|void
name|testParsingAndToQuery11
parameter_list|()
throws|throws
name|IOException
block|{
name|assumeTrue
argument_list|(
literal|"test runs only when at least a type is registered"
argument_list|,
name|getCurrentTypes
argument_list|()
operator|.
name|length
operator|>
literal|0
argument_list|)
expr_stmt|;
name|String
name|query
init|=
literal|"{\n"
operator|+
literal|"    \"geo_distance\":{\n"
operator|+
literal|"        \"distance\":\"19.312128km\",\n"
operator|+
literal|"        \""
operator|+
name|GEO_POINT_FIELD_NAME
operator|+
literal|"\":{\n"
operator|+
literal|"            \"lat\":40,\n"
operator|+
literal|"            \"lon\":-70\n"
operator|+
literal|"        }\n"
operator|+
literal|"    }\n"
operator|+
literal|"}\n"
decl_stmt|;
name|assertGeoDistanceRangeQuery
argument_list|(
name|query
argument_list|,
literal|40
argument_list|,
operator|-
literal|70
argument_list|,
literal|12
argument_list|,
name|DistanceUnit
operator|.
name|DEFAULT
argument_list|)
expr_stmt|;
block|}
DECL|method|testParsingAndToQuery12
specifier|public
name|void
name|testParsingAndToQuery12
parameter_list|()
throws|throws
name|IOException
block|{
name|assumeTrue
argument_list|(
literal|"test runs only when at least a type is registered"
argument_list|,
name|getCurrentTypes
argument_list|()
operator|.
name|length
operator|>
literal|0
argument_list|)
expr_stmt|;
name|String
name|query
init|=
literal|"{\n"
operator|+
literal|"    \"geo_distance\":{\n"
operator|+
literal|"        \"distance\":\"12mi\",\n"
operator|+
literal|"        \"unit\":\"km\",\n"
operator|+
literal|"        \""
operator|+
name|GEO_POINT_FIELD_NAME
operator|+
literal|"\":{\n"
operator|+
literal|"            \"lat\":40,\n"
operator|+
literal|"            \"lon\":-70\n"
operator|+
literal|"        }\n"
operator|+
literal|"    }\n"
operator|+
literal|"}\n"
decl_stmt|;
name|assertGeoDistanceRangeQuery
argument_list|(
name|query
argument_list|,
literal|40
argument_list|,
operator|-
literal|70
argument_list|,
literal|12
argument_list|,
name|DistanceUnit
operator|.
name|DEFAULT
argument_list|)
expr_stmt|;
block|}
DECL|method|assertGeoDistanceRangeQuery
specifier|private
name|void
name|assertGeoDistanceRangeQuery
parameter_list|(
name|String
name|query
parameter_list|,
name|double
name|lat
parameter_list|,
name|double
name|lon
parameter_list|,
name|double
name|distance
parameter_list|,
name|DistanceUnit
name|distanceUnit
parameter_list|)
throws|throws
name|IOException
block|{
name|assumeTrue
argument_list|(
literal|"test runs only when at least a type is registered"
argument_list|,
name|getCurrentTypes
argument_list|()
operator|.
name|length
operator|>
literal|0
argument_list|)
expr_stmt|;
name|Query
name|parsedQuery
init|=
name|parseQuery
argument_list|(
name|query
argument_list|)
operator|.
name|toQuery
argument_list|(
name|createShardContext
argument_list|()
argument_list|)
decl_stmt|;
comment|// TODO: what can we check?
block|}
DECL|method|testFromJson
specifier|public
name|void
name|testFromJson
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|json
init|=
literal|"{\n"
operator|+
literal|"  \"geo_distance\" : {\n"
operator|+
literal|"    \"pin.location\" : [ -70.0, 40.0 ],\n"
operator|+
literal|"    \"distance\" : 12000.0,\n"
operator|+
literal|"    \"distance_type\" : \"sloppy_arc\",\n"
operator|+
literal|"    \"validation_method\" : \"STRICT\",\n"
operator|+
literal|"    \"ignore_unmapped\" : false,\n"
operator|+
literal|"    \"boost\" : 1.0\n"
operator|+
literal|"  }\n"
operator|+
literal|"}"
decl_stmt|;
name|GeoDistanceQueryBuilder
name|parsed
init|=
operator|(
name|GeoDistanceQueryBuilder
operator|)
name|parseQuery
argument_list|(
name|json
argument_list|)
decl_stmt|;
name|checkGeneratedJson
argument_list|(
name|json
argument_list|,
name|parsed
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|json
argument_list|,
operator|-
literal|70.0
argument_list|,
name|parsed
operator|.
name|point
argument_list|()
operator|.
name|getLon
argument_list|()
argument_list|,
literal|0.0001
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|json
argument_list|,
literal|40.0
argument_list|,
name|parsed
operator|.
name|point
argument_list|()
operator|.
name|getLat
argument_list|()
argument_list|,
literal|0.0001
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|json
argument_list|,
literal|12000.0
argument_list|,
name|parsed
operator|.
name|distance
argument_list|()
argument_list|,
literal|0.0001
argument_list|)
expr_stmt|;
block|}
DECL|method|testOptimizeBboxIsDeprecated
specifier|public
name|void
name|testOptimizeBboxIsDeprecated
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|json
init|=
literal|"{\n"
operator|+
literal|"  \"geo_distance\" : {\n"
operator|+
literal|"    \"pin.location\" : [ -70.0, 40.0 ],\n"
operator|+
literal|"    \"distance\" : 12000.0,\n"
operator|+
literal|"    \"distance_type\" : \"sloppy_arc\",\n"
operator|+
literal|"    \"optimize_bbox\" : \"memory\",\n"
operator|+
literal|"    \"validation_method\" : \"STRICT\",\n"
operator|+
literal|"    \"ignore_unmapped\" : false,\n"
operator|+
literal|"    \"boost\" : 1.0\n"
operator|+
literal|"  }\n"
operator|+
literal|"}"
decl_stmt|;
name|parseQuery
argument_list|(
name|json
argument_list|)
expr_stmt|;
name|assertWarnings
argument_list|(
literal|"Deprecated field [optimize_bbox] used, replaced by [no replacement: "
operator|+
literal|"`optimize_bbox` is no longer supported due to recent improvements]"
argument_list|)
expr_stmt|;
block|}
DECL|method|testFromCoerceIsDeprecated
specifier|public
name|void
name|testFromCoerceIsDeprecated
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|json
init|=
literal|"{\n"
operator|+
literal|"  \"geo_distance\" : {\n"
operator|+
literal|"    \"pin.location\" : [ -70.0, 40.0 ],\n"
operator|+
literal|"    \"distance\" : 12000.0,\n"
operator|+
literal|"    \"distance_type\" : \"sloppy_arc\",\n"
operator|+
literal|"    \"coerce\" : true,\n"
operator|+
literal|"    \"ignore_unmapped\" : false,\n"
operator|+
literal|"    \"boost\" : 1.0\n"
operator|+
literal|"  }\n"
operator|+
literal|"}"
decl_stmt|;
name|parseQuery
argument_list|(
name|json
argument_list|)
expr_stmt|;
name|assertWarnings
argument_list|(
literal|"Deprecated field [coerce] used, replaced by [validation_method]"
argument_list|)
expr_stmt|;
block|}
DECL|method|testFromJsonIgnoreMalformedIsDeprecated
specifier|public
name|void
name|testFromJsonIgnoreMalformedIsDeprecated
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|json
init|=
literal|"{\n"
operator|+
literal|"  \"geo_distance\" : {\n"
operator|+
literal|"    \"pin.location\" : [ -70.0, 40.0 ],\n"
operator|+
literal|"    \"distance\" : 12000.0,\n"
operator|+
literal|"    \"distance_type\" : \"sloppy_arc\",\n"
operator|+
literal|"    \"ignore_malformed\" : true,\n"
operator|+
literal|"    \"ignore_unmapped\" : false,\n"
operator|+
literal|"    \"boost\" : 1.0\n"
operator|+
literal|"  }\n"
operator|+
literal|"}"
decl_stmt|;
name|parseQuery
argument_list|(
name|json
argument_list|)
expr_stmt|;
name|assertWarnings
argument_list|(
literal|"Deprecated field [ignore_malformed] used, replaced by [validation_method]"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|testMustRewrite
specifier|public
name|void
name|testMustRewrite
parameter_list|()
throws|throws
name|IOException
block|{
name|assumeTrue
argument_list|(
literal|"test runs only when at least a type is registered"
argument_list|,
name|getCurrentTypes
argument_list|()
operator|.
name|length
operator|>
literal|0
argument_list|)
expr_stmt|;
name|super
operator|.
name|testMustRewrite
argument_list|()
expr_stmt|;
block|}
DECL|method|testIgnoreUnmapped
specifier|public
name|void
name|testIgnoreUnmapped
parameter_list|()
throws|throws
name|IOException
block|{
specifier|final
name|GeoDistanceQueryBuilder
name|queryBuilder
init|=
operator|new
name|GeoDistanceQueryBuilder
argument_list|(
literal|"unmapped"
argument_list|)
operator|.
name|point
argument_list|(
literal|0.0
argument_list|,
literal|0.0
argument_list|)
operator|.
name|distance
argument_list|(
literal|"20m"
argument_list|)
decl_stmt|;
name|queryBuilder
operator|.
name|ignoreUnmapped
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|QueryShardContext
name|shardContext
init|=
name|createShardContext
argument_list|()
decl_stmt|;
name|Query
name|query
init|=
name|queryBuilder
operator|.
name|toQuery
argument_list|(
name|shardContext
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|query
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|query
argument_list|,
name|instanceOf
argument_list|(
name|MatchNoDocsQuery
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
specifier|final
name|GeoDistanceQueryBuilder
name|failingQueryBuilder
init|=
operator|new
name|GeoDistanceQueryBuilder
argument_list|(
literal|"unmapped"
argument_list|)
operator|.
name|point
argument_list|(
literal|0.0
argument_list|,
literal|0.0
argument_list|)
operator|.
name|distance
argument_list|(
literal|"20m"
argument_list|)
decl_stmt|;
name|failingQueryBuilder
operator|.
name|ignoreUnmapped
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|QueryShardException
name|e
init|=
name|expectThrows
argument_list|(
name|QueryShardException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|failingQueryBuilder
operator|.
name|toQuery
argument_list|(
name|shardContext
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"failed to find geo_point field [unmapped]"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testParseFailsWithMultipleFields
specifier|public
name|void
name|testParseFailsWithMultipleFields
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|json
init|=
literal|"{\n"
operator|+
literal|"  \"geo_distance\" : {\n"
operator|+
literal|"    \"point1\" : {\n"
operator|+
literal|"      \"lat\" : 30, \"lon\" : 12\n"
operator|+
literal|"    },\n"
operator|+
literal|"    \"point2\" : {\n"
operator|+
literal|"      \"lat\" : 30, \"lon\" : 12\n"
operator|+
literal|"    }\n"
operator|+
literal|"  }\n"
operator|+
literal|"}"
decl_stmt|;
name|ParsingException
name|e
init|=
name|expectThrows
argument_list|(
name|ParsingException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|parseQuery
argument_list|(
name|json
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"[geo_distance] query doesn't support multiple fields, found [point1] and [point2]"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

