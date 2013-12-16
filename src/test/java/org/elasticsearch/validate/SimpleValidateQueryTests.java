begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.validate
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|validate
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Charsets
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|admin
operator|.
name|indices
operator|.
name|validate
operator|.
name|query
operator|.
name|ValidateQueryResponse
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|client
operator|.
name|Client
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
name|Priority
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
name|settings
operator|.
name|ImmutableSettings
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
name|XContentFactory
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
name|FilterBuilders
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
name|QueryBuilder
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
name|QueryBuilders
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
name|ElasticsearchIntegrationTest
import|;
end_import

begin_import
import|import
name|org
operator|.
name|hamcrest
operator|.
name|Matcher
import|;
end_import

begin_import
import|import
name|org
operator|.
name|joda
operator|.
name|time
operator|.
name|DateTime
import|;
end_import

begin_import
import|import
name|org
operator|.
name|joda
operator|.
name|time
operator|.
name|DateTimeZone
import|;
end_import

begin_import
import|import
name|org
operator|.
name|joda
operator|.
name|time
operator|.
name|format
operator|.
name|ISODateTimeFormat
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
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|QueryBuilders
operator|.
name|queryString
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|hamcrest
operator|.
name|ElasticsearchAssertions
operator|.
name|*
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

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|SimpleValidateQueryTests
specifier|public
class|class
name|SimpleValidateQueryTests
extends|extends
name|ElasticsearchIntegrationTest
block|{
annotation|@
name|Test
DECL|method|simpleValidateQuery
specifier|public
name|void
name|simpleValidateQuery
parameter_list|()
throws|throws
name|Exception
block|{
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareCreate
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setSettings
argument_list|(
name|ImmutableSettings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"index.number_of_shards"
argument_list|,
literal|1
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareHealth
argument_list|()
operator|.
name|setWaitForEvents
argument_list|(
name|Priority
operator|.
name|LANGUID
argument_list|)
operator|.
name|setWaitForGreenStatus
argument_list|()
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|preparePutMapping
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setType
argument_list|(
literal|"type1"
argument_list|)
operator|.
name|setSource
argument_list|(
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"type1"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"properties"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"foo"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"string"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"bar"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"integer"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareRefresh
argument_list|()
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareValidateQuery
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setQuery
argument_list|(
literal|"foo"
operator|.
name|getBytes
argument_list|(
name|Charsets
operator|.
name|UTF_8
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
operator|.
name|isValid
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareValidateQuery
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
name|queryString
argument_list|(
literal|"_id:1"
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
operator|.
name|isValid
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareValidateQuery
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
name|queryString
argument_list|(
literal|"_i:d:1"
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
operator|.
name|isValid
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareValidateQuery
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
name|queryString
argument_list|(
literal|"foo:1"
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
operator|.
name|isValid
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareValidateQuery
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
name|queryString
argument_list|(
literal|"bar:hey"
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
operator|.
name|isValid
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareValidateQuery
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
name|queryString
argument_list|(
literal|"nonexistent:hello"
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
operator|.
name|isValid
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareValidateQuery
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
name|queryString
argument_list|(
literal|"foo:1 AND"
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
operator|.
name|isValid
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|explainValidateQuery
specifier|public
name|void
name|explainValidateQuery
parameter_list|()
throws|throws
name|Exception
block|{
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareCreate
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setSettings
argument_list|(
name|ImmutableSettings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"index.number_of_shards"
argument_list|,
literal|1
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareHealth
argument_list|()
operator|.
name|setWaitForEvents
argument_list|(
name|Priority
operator|.
name|LANGUID
argument_list|)
operator|.
name|setWaitForGreenStatus
argument_list|()
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|preparePutMapping
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setType
argument_list|(
literal|"type1"
argument_list|)
operator|.
name|setSource
argument_list|(
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"type1"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"properties"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"foo"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"string"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"bar"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"integer"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"baz"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"string"
argument_list|)
operator|.
name|field
argument_list|(
literal|"analyzer"
argument_list|,
literal|"snowball"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"pin"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"properties"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"location"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"geo_point"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|preparePutMapping
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setType
argument_list|(
literal|"child-type"
argument_list|)
operator|.
name|setSource
argument_list|(
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"child-type"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"_parent"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"type1"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"properties"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"foo"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"string"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareRefresh
argument_list|()
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|ValidateQueryResponse
name|response
decl_stmt|;
name|response
operator|=
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareValidateQuery
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setQuery
argument_list|(
literal|"foo"
operator|.
name|getBytes
argument_list|(
name|Charsets
operator|.
name|UTF_8
argument_list|)
argument_list|)
operator|.
name|setExplain
argument_list|(
literal|true
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|isValid
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getQueryExplanation
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getQueryExplanation
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getError
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"Failed to parse"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getQueryExplanation
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getExplanation
argument_list|()
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertExplanation
argument_list|(
name|QueryBuilders
operator|.
name|queryString
argument_list|(
literal|"_id:1"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"ConstantScore(_uid:type1#1)"
argument_list|)
argument_list|)
expr_stmt|;
name|assertExplanation
argument_list|(
name|QueryBuilders
operator|.
name|idsQuery
argument_list|(
literal|"type1"
argument_list|)
operator|.
name|addIds
argument_list|(
literal|"1"
argument_list|)
operator|.
name|addIds
argument_list|(
literal|"2"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"ConstantScore(_uid:type1#1 _uid:type1#2)"
argument_list|)
argument_list|)
expr_stmt|;
name|assertExplanation
argument_list|(
name|QueryBuilders
operator|.
name|queryString
argument_list|(
literal|"foo"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"_all:foo"
argument_list|)
argument_list|)
expr_stmt|;
name|assertExplanation
argument_list|(
name|QueryBuilders
operator|.
name|filteredQuery
argument_list|(
name|QueryBuilders
operator|.
name|termQuery
argument_list|(
literal|"foo"
argument_list|,
literal|"1"
argument_list|)
argument_list|,
name|FilterBuilders
operator|.
name|orFilter
argument_list|(
name|FilterBuilders
operator|.
name|termFilter
argument_list|(
literal|"bar"
argument_list|,
literal|"2"
argument_list|)
argument_list|,
name|FilterBuilders
operator|.
name|termFilter
argument_list|(
literal|"baz"
argument_list|,
literal|"3"
argument_list|)
argument_list|)
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"filtered(foo:1)->cache(bar:[2 TO 2]) cache(baz:3)"
argument_list|)
argument_list|)
expr_stmt|;
name|assertExplanation
argument_list|(
name|QueryBuilders
operator|.
name|filteredQuery
argument_list|(
name|QueryBuilders
operator|.
name|termQuery
argument_list|(
literal|"foo"
argument_list|,
literal|"1"
argument_list|)
argument_list|,
name|FilterBuilders
operator|.
name|orFilter
argument_list|(
name|FilterBuilders
operator|.
name|termFilter
argument_list|(
literal|"bar"
argument_list|,
literal|"2"
argument_list|)
argument_list|)
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"filtered(foo:1)->cache(bar:[2 TO 2])"
argument_list|)
argument_list|)
expr_stmt|;
name|assertExplanation
argument_list|(
name|QueryBuilders
operator|.
name|filteredQuery
argument_list|(
name|QueryBuilders
operator|.
name|matchAllQuery
argument_list|()
argument_list|,
name|FilterBuilders
operator|.
name|geoPolygonFilter
argument_list|(
literal|"pin.location"
argument_list|)
operator|.
name|addPoint
argument_list|(
literal|40
argument_list|,
operator|-
literal|70
argument_list|)
operator|.
name|addPoint
argument_list|(
literal|30
argument_list|,
operator|-
literal|80
argument_list|)
operator|.
name|addPoint
argument_list|(
literal|20
argument_list|,
operator|-
literal|90
argument_list|)
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"ConstantScore(GeoPolygonFilter(pin.location, [[40.0, -70.0], [30.0, -80.0], [20.0, -90.0]]))"
argument_list|)
argument_list|)
expr_stmt|;
name|assertExplanation
argument_list|(
name|QueryBuilders
operator|.
name|constantScoreQuery
argument_list|(
name|FilterBuilders
operator|.
name|geoBoundingBoxFilter
argument_list|(
literal|"pin.location"
argument_list|)
operator|.
name|topLeft
argument_list|(
literal|40
argument_list|,
operator|-
literal|80
argument_list|)
operator|.
name|bottomRight
argument_list|(
literal|20
argument_list|,
operator|-
literal|70
argument_list|)
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"ConstantScore(GeoBoundingBoxFilter(pin.location, [40.0, -80.0], [20.0, -70.0]))"
argument_list|)
argument_list|)
expr_stmt|;
name|assertExplanation
argument_list|(
name|QueryBuilders
operator|.
name|constantScoreQuery
argument_list|(
name|FilterBuilders
operator|.
name|geoDistanceFilter
argument_list|(
literal|"pin.location"
argument_list|)
operator|.
name|lat
argument_list|(
literal|10
argument_list|)
operator|.
name|lon
argument_list|(
literal|20
argument_list|)
operator|.
name|distance
argument_list|(
literal|15
argument_list|,
name|DistanceUnit
operator|.
name|MILES
argument_list|)
operator|.
name|geoDistance
argument_list|(
name|GeoDistance
operator|.
name|PLANE
argument_list|)
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"ConstantScore(GeoDistanceFilter(pin.location, PLANE, 15.0, 10.0, 20.0))"
argument_list|)
argument_list|)
expr_stmt|;
name|assertExplanation
argument_list|(
name|QueryBuilders
operator|.
name|constantScoreQuery
argument_list|(
name|FilterBuilders
operator|.
name|geoDistanceFilter
argument_list|(
literal|"pin.location"
argument_list|)
operator|.
name|lat
argument_list|(
literal|10
argument_list|)
operator|.
name|lon
argument_list|(
literal|20
argument_list|)
operator|.
name|distance
argument_list|(
literal|15
argument_list|,
name|DistanceUnit
operator|.
name|MILES
argument_list|)
operator|.
name|geoDistance
argument_list|(
name|GeoDistance
operator|.
name|PLANE
argument_list|)
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"ConstantScore(GeoDistanceFilter(pin.location, PLANE, 15.0, 10.0, 20.0))"
argument_list|)
argument_list|)
expr_stmt|;
name|assertExplanation
argument_list|(
name|QueryBuilders
operator|.
name|constantScoreQuery
argument_list|(
name|FilterBuilders
operator|.
name|geoDistanceRangeFilter
argument_list|(
literal|"pin.location"
argument_list|)
operator|.
name|lat
argument_list|(
literal|10
argument_list|)
operator|.
name|lon
argument_list|(
literal|20
argument_list|)
operator|.
name|from
argument_list|(
literal|"15miles"
argument_list|)
operator|.
name|to
argument_list|(
literal|"25miles"
argument_list|)
operator|.
name|geoDistance
argument_list|(
name|GeoDistance
operator|.
name|PLANE
argument_list|)
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"ConstantScore(GeoDistanceRangeFilter(pin.location, PLANE, [15.0 - 25.0], 10.0, 20.0))"
argument_list|)
argument_list|)
expr_stmt|;
name|assertExplanation
argument_list|(
name|QueryBuilders
operator|.
name|filteredQuery
argument_list|(
name|QueryBuilders
operator|.
name|termQuery
argument_list|(
literal|"foo"
argument_list|,
literal|"1"
argument_list|)
argument_list|,
name|FilterBuilders
operator|.
name|andFilter
argument_list|(
name|FilterBuilders
operator|.
name|termFilter
argument_list|(
literal|"bar"
argument_list|,
literal|"2"
argument_list|)
argument_list|,
name|FilterBuilders
operator|.
name|termFilter
argument_list|(
literal|"baz"
argument_list|,
literal|"3"
argument_list|)
argument_list|)
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"filtered(foo:1)->+cache(bar:[2 TO 2]) +cache(baz:3)"
argument_list|)
argument_list|)
expr_stmt|;
name|assertExplanation
argument_list|(
name|QueryBuilders
operator|.
name|constantScoreQuery
argument_list|(
name|FilterBuilders
operator|.
name|termsFilter
argument_list|(
literal|"foo"
argument_list|,
literal|"1"
argument_list|,
literal|"2"
argument_list|,
literal|"3"
argument_list|)
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"ConstantScore(cache(foo:1 foo:2 foo:3))"
argument_list|)
argument_list|)
expr_stmt|;
name|assertExplanation
argument_list|(
name|QueryBuilders
operator|.
name|constantScoreQuery
argument_list|(
name|FilterBuilders
operator|.
name|notFilter
argument_list|(
name|FilterBuilders
operator|.
name|termFilter
argument_list|(
literal|"foo"
argument_list|,
literal|"bar"
argument_list|)
argument_list|)
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"ConstantScore(NotFilter(cache(foo:bar)))"
argument_list|)
argument_list|)
expr_stmt|;
name|assertExplanation
argument_list|(
name|QueryBuilders
operator|.
name|filteredQuery
argument_list|(
name|QueryBuilders
operator|.
name|termQuery
argument_list|(
literal|"foo"
argument_list|,
literal|"1"
argument_list|)
argument_list|,
name|FilterBuilders
operator|.
name|hasChildFilter
argument_list|(
literal|"child-type"
argument_list|,
name|QueryBuilders
operator|.
name|matchQuery
argument_list|(
literal|"foo"
argument_list|,
literal|"1"
argument_list|)
argument_list|)
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"filtered(foo:1)->CustomQueryWrappingFilter(child_filter[child-type/type1](filtered(foo:1)->cache(_type:child-type)))"
argument_list|)
argument_list|)
expr_stmt|;
name|assertExplanation
argument_list|(
name|QueryBuilders
operator|.
name|filteredQuery
argument_list|(
name|QueryBuilders
operator|.
name|termQuery
argument_list|(
literal|"foo"
argument_list|,
literal|"1"
argument_list|)
argument_list|,
name|FilterBuilders
operator|.
name|scriptFilter
argument_list|(
literal|"true"
argument_list|)
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"filtered(foo:1)->ScriptFilter(true)"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|explainValidateQueryTwoNodes
specifier|public
name|void
name|explainValidateQueryTwoNodes
parameter_list|()
throws|throws
name|IOException
block|{
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareCreate
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setSettings
argument_list|(
name|ImmutableSettings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"index.number_of_shards"
argument_list|,
literal|1
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.number_of_replicas"
argument_list|,
literal|0
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareHealth
argument_list|()
operator|.
name|setWaitForEvents
argument_list|(
name|Priority
operator|.
name|LANGUID
argument_list|)
operator|.
name|setWaitForGreenStatus
argument_list|()
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|preparePutMapping
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setType
argument_list|(
literal|"type1"
argument_list|)
operator|.
name|setSource
argument_list|(
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"type1"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"properties"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"foo"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"string"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"bar"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"integer"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"baz"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"string"
argument_list|)
operator|.
name|field
argument_list|(
literal|"analyzer"
argument_list|,
literal|"snowball"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"pin"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"properties"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"location"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"geo_point"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareRefresh
argument_list|()
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
for|for
control|(
name|Client
name|client
range|:
name|cluster
argument_list|()
control|)
block|{
name|ValidateQueryResponse
name|response
init|=
name|client
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareValidateQuery
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setQuery
argument_list|(
literal|"foo"
operator|.
name|getBytes
argument_list|(
name|Charsets
operator|.
name|UTF_8
argument_list|)
argument_list|)
operator|.
name|setExplain
argument_list|(
literal|true
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|isValid
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getQueryExplanation
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getQueryExplanation
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getError
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"Failed to parse"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getQueryExplanation
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getExplanation
argument_list|()
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|Client
name|client
range|:
name|cluster
argument_list|()
control|)
block|{
name|ValidateQueryResponse
name|response
init|=
name|client
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareValidateQuery
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
name|queryString
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
operator|.
name|setExplain
argument_list|(
literal|true
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|isValid
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getQueryExplanation
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getQueryExplanation
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getExplanation
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"_all:foo"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getQueryExplanation
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getError
argument_list|()
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
comment|//https://github.com/elasticsearch/elasticsearch/issues/3629
DECL|method|explainDateRangeInQueryString
specifier|public
name|void
name|explainDateRangeInQueryString
parameter_list|()
block|{
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareCreate
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setSettings
argument_list|(
name|ImmutableSettings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"index.number_of_shards"
argument_list|,
literal|1
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|String
name|aMonthAgo
init|=
name|ISODateTimeFormat
operator|.
name|yearMonthDay
argument_list|()
operator|.
name|print
argument_list|(
operator|new
name|DateTime
argument_list|(
name|DateTimeZone
operator|.
name|UTC
argument_list|)
operator|.
name|minusMonths
argument_list|(
literal|1
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|aMonthFromNow
init|=
name|ISODateTimeFormat
operator|.
name|yearMonthDay
argument_list|()
operator|.
name|print
argument_list|(
operator|new
name|DateTime
argument_list|(
name|DateTimeZone
operator|.
name|UTC
argument_list|)
operator|.
name|plusMonths
argument_list|(
literal|1
argument_list|)
argument_list|)
decl_stmt|;
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"past"
argument_list|,
name|aMonthAgo
argument_list|,
literal|"future"
argument_list|,
name|aMonthFromNow
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|refresh
argument_list|()
expr_stmt|;
name|ValidateQueryResponse
name|response
init|=
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareValidateQuery
argument_list|()
operator|.
name|setQuery
argument_list|(
name|queryString
argument_list|(
literal|"past:[now-2M/d TO now/d]"
argument_list|)
argument_list|)
operator|.
name|setExplain
argument_list|(
literal|true
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertNoFailures
argument_list|(
name|response
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getQueryExplanation
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getQueryExplanation
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getError
argument_list|()
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|DateTime
name|twoMonthsAgo
init|=
operator|new
name|DateTime
argument_list|(
name|DateTimeZone
operator|.
name|UTC
argument_list|)
operator|.
name|minusMonths
argument_list|(
literal|2
argument_list|)
operator|.
name|withTimeAtStartOfDay
argument_list|()
decl_stmt|;
name|DateTime
name|now
init|=
operator|new
name|DateTime
argument_list|(
name|DateTimeZone
operator|.
name|UTC
argument_list|)
operator|.
name|plusDays
argument_list|(
literal|1
argument_list|)
operator|.
name|withTimeAtStartOfDay
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getQueryExplanation
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getExplanation
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"past:["
operator|+
name|twoMonthsAgo
operator|.
name|getMillis
argument_list|()
operator|+
literal|" TO "
operator|+
name|now
operator|.
name|getMillis
argument_list|()
operator|+
literal|"]"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|isValid
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|assertExplanation
specifier|private
name|void
name|assertExplanation
parameter_list|(
name|QueryBuilder
name|queryBuilder
parameter_list|,
name|Matcher
argument_list|<
name|String
argument_list|>
name|matcher
parameter_list|)
block|{
name|ValidateQueryResponse
name|response
init|=
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareValidateQuery
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setTypes
argument_list|(
literal|"type1"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|queryBuilder
argument_list|)
operator|.
name|setExplain
argument_list|(
literal|true
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getQueryExplanation
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getQueryExplanation
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getError
argument_list|()
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getQueryExplanation
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getExplanation
argument_list|()
argument_list|,
name|matcher
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|isValid
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

