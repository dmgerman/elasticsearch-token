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
name|com
operator|.
name|spatial4j
operator|.
name|core
operator|.
name|shape
operator|.
name|Point
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
name|index
operator|.
name|search
operator|.
name|geo
operator|.
name|GeoDistanceRangeQuery
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
name|junit
operator|.
name|Test
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
name|Matchers
operator|.
name|*
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
name|optimizeBbox
argument_list|(
name|randomFrom
argument_list|(
literal|"none"
argument_list|,
literal|"memory"
argument_list|,
literal|"indexed"
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
try|try
block|{
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
operator|new
name|GeoDistanceQueryBuilder
argument_list|(
literal|""
argument_list|)
expr_stmt|;
block|}
else|else
block|{
operator|new
name|GeoDistanceQueryBuilder
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
name|fail
argument_list|(
literal|"must not be null or empty"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|ex
parameter_list|)
block|{
comment|// expected
block|}
name|GeoDistanceQueryBuilder
name|query
init|=
operator|new
name|GeoDistanceQueryBuilder
argument_list|(
literal|"fieldName"
argument_list|)
decl_stmt|;
try|try
block|{
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|query
operator|.
name|distance
argument_list|(
literal|""
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|query
operator|.
name|distance
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
name|fail
argument_list|(
literal|"must not be null or empty"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|ex
parameter_list|)
block|{
comment|// expected
block|}
try|try
block|{
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
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
expr_stmt|;
block|}
else|else
block|{
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
expr_stmt|;
block|}
name|fail
argument_list|(
literal|"must not be null or empty"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|ex
parameter_list|)
block|{
comment|// expected
block|}
try|try
block|{
name|query
operator|.
name|distance
argument_list|(
literal|"1"
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"unit must not be null"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|ex
parameter_list|)
block|{
comment|// expected
block|}
try|try
block|{
name|query
operator|.
name|distance
argument_list|(
literal|1
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"unit must not be null"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|ex
parameter_list|)
block|{
comment|// expected
block|}
try|try
block|{
name|query
operator|.
name|geohash
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"geohash must not be null"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|ex
parameter_list|)
block|{
comment|// expected
block|}
try|try
block|{
name|query
operator|.
name|geoDistance
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"geodistance must not be null"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|ex
parameter_list|)
block|{
comment|// expected
block|}
try|try
block|{
name|query
operator|.
name|optimizeBbox
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"optimizeBbox must not be null"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|ex
parameter_list|)
block|{
comment|// expected
block|}
block|}
comment|/**      * Overridden here to ensure the test is only run if at least one type is      * present in the mappings. Geo queries do not execute if the field is not      * explicitly mapped      */
annotation|@
name|Override
annotation|@
name|Test
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
name|QueryShardContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|assertThat
argument_list|(
name|query
argument_list|,
name|instanceOf
argument_list|(
name|GeoDistanceRangeQuery
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|GeoDistanceRangeQuery
name|geoQuery
init|=
operator|(
name|GeoDistanceRangeQuery
operator|)
name|query
decl_stmt|;
name|assertThat
argument_list|(
name|geoQuery
operator|.
name|fieldName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|queryBuilder
operator|.
name|fieldName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|queryBuilder
operator|.
name|point
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|assertThat
argument_list|(
name|geoQuery
operator|.
name|lat
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|queryBuilder
operator|.
name|point
argument_list|()
operator|.
name|lat
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|geoQuery
operator|.
name|lon
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|queryBuilder
operator|.
name|point
argument_list|()
operator|.
name|lon
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|assertThat
argument_list|(
name|geoQuery
operator|.
name|geoDistance
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|queryBuilder
operator|.
name|geoDistance
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|geoQuery
operator|.
name|minInclusiveDistance
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|Double
operator|.
name|NEGATIVE_INFINITY
argument_list|)
argument_list|)
expr_stmt|;
name|double
name|distance
init|=
name|queryBuilder
operator|.
name|distance
argument_list|()
decl_stmt|;
if|if
condition|(
name|queryBuilder
operator|.
name|geoDistance
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|distance
operator|=
name|queryBuilder
operator|.
name|geoDistance
argument_list|()
operator|.
name|normalize
argument_list|(
name|distance
argument_list|,
name|DistanceUnit
operator|.
name|DEFAULT
argument_list|)
expr_stmt|;
block|}
name|assertThat
argument_list|(
name|geoQuery
operator|.
name|maxInclusiveDistance
argument_list|()
argument_list|,
name|closeTo
argument_list|(
name|distance
argument_list|,
name|Math
operator|.
name|abs
argument_list|(
name|distance
argument_list|)
operator|/
literal|1000
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
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
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
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
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
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
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
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
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
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
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
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
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
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
name|GeoDistanceRangeQuery
name|filter
init|=
operator|(
name|GeoDistanceRangeQuery
operator|)
name|parsedQuery
decl_stmt|;
name|assertThat
argument_list|(
name|filter
operator|.
name|fieldName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|GEO_POINT_FIELD_NAME
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filter
operator|.
name|lat
argument_list|()
argument_list|,
name|closeTo
argument_list|(
literal|40
argument_list|,
literal|0.00001
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filter
operator|.
name|lon
argument_list|()
argument_list|,
name|closeTo
argument_list|(
operator|-
literal|70
argument_list|,
literal|0.00001
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filter
operator|.
name|minInclusiveDistance
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|Double
operator|.
name|NEGATIVE_INFINITY
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filter
operator|.
name|maxInclusiveDistance
argument_list|()
argument_list|,
name|closeTo
argument_list|(
name|DistanceUnit
operator|.
name|DEFAULT
operator|.
name|convert
argument_list|(
literal|0.012
argument_list|,
name|DistanceUnit
operator|.
name|MILES
argument_list|)
argument_list|,
literal|0.00001
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
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
name|GeoDistanceRangeQuery
name|filter
init|=
operator|(
name|GeoDistanceRangeQuery
operator|)
name|parsedQuery
decl_stmt|;
name|assertThat
argument_list|(
name|filter
operator|.
name|fieldName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|GEO_POINT_FIELD_NAME
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filter
operator|.
name|lat
argument_list|()
argument_list|,
name|closeTo
argument_list|(
literal|40
argument_list|,
literal|0.00001
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filter
operator|.
name|lon
argument_list|()
argument_list|,
name|closeTo
argument_list|(
operator|-
literal|70
argument_list|,
literal|0.00001
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filter
operator|.
name|minInclusiveDistance
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|Double
operator|.
name|NEGATIVE_INFINITY
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filter
operator|.
name|maxInclusiveDistance
argument_list|()
argument_list|,
name|closeTo
argument_list|(
name|DistanceUnit
operator|.
name|KILOMETERS
operator|.
name|convert
argument_list|(
literal|12
argument_list|,
name|DistanceUnit
operator|.
name|MILES
argument_list|)
argument_list|,
literal|0.00001
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
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
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
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
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
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
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
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
name|GeoDistanceRangeQuery
name|filter
init|=
operator|(
name|GeoDistanceRangeQuery
operator|)
name|parsedQuery
decl_stmt|;
name|assertThat
argument_list|(
name|filter
operator|.
name|fieldName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|GEO_POINT_FIELD_NAME
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filter
operator|.
name|lat
argument_list|()
argument_list|,
name|closeTo
argument_list|(
literal|40
argument_list|,
literal|0.00001
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filter
operator|.
name|lon
argument_list|()
argument_list|,
name|closeTo
argument_list|(
operator|-
literal|70
argument_list|,
literal|0.00001
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filter
operator|.
name|minInclusiveDistance
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|Double
operator|.
name|NEGATIVE_INFINITY
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filter
operator|.
name|maxInclusiveDistance
argument_list|()
argument_list|,
name|closeTo
argument_list|(
name|DistanceUnit
operator|.
name|DEFAULT
operator|.
name|convert
argument_list|(
literal|12
argument_list|,
name|DistanceUnit
operator|.
name|MILES
argument_list|)
argument_list|,
literal|0.00001
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

