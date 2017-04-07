begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.metrics
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|metrics
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|search
operator|.
name|SearchResponse
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
name|search
operator|.
name|aggregations
operator|.
name|InternalAggregation
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
name|aggregations
operator|.
name|bucket
operator|.
name|geogrid
operator|.
name|GeoHashGrid
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
name|aggregations
operator|.
name|bucket
operator|.
name|global
operator|.
name|Global
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
name|aggregations
operator|.
name|metrics
operator|.
name|geocentroid
operator|.
name|GeoCentroid
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
name|ESIntegTestCase
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
name|matchAllQuery
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|AggregationBuilders
operator|.
name|geoCentroid
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|AggregationBuilders
operator|.
name|geohashGrid
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|AggregationBuilders
operator|.
name|global
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
name|assertSearchResponse
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
name|closeTo
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
name|equalTo
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
name|notNullValue
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
name|sameInstance
import|;
end_import

begin_comment
comment|/**  * Integration Test for GeoCentroid metric aggregator  */
end_comment

begin_class
annotation|@
name|ESIntegTestCase
operator|.
name|SuiteScopeTestCase
DECL|class|GeoCentroidIT
specifier|public
class|class
name|GeoCentroidIT
extends|extends
name|AbstractGeoTestCase
block|{
DECL|field|aggName
specifier|private
specifier|static
specifier|final
name|String
name|aggName
init|=
literal|"geoCentroid"
decl_stmt|;
DECL|method|testEmptyAggregation
specifier|public
name|void
name|testEmptyAggregation
parameter_list|()
throws|throws
name|Exception
block|{
name|SearchResponse
name|response
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
name|EMPTY_IDX_NAME
argument_list|)
operator|.
name|setQuery
argument_list|(
name|matchAllQuery
argument_list|()
argument_list|)
operator|.
name|addAggregation
argument_list|(
name|geoCentroid
argument_list|(
name|aggName
argument_list|)
operator|.
name|field
argument_list|(
name|SINGLE_VALUED_FIELD_NAME
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|assertSearchResponse
argument_list|(
name|response
argument_list|)
expr_stmt|;
name|GeoCentroid
name|geoCentroid
init|=
name|response
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
name|aggName
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getHits
argument_list|()
operator|.
name|getTotalHits
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|geoCentroid
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|geoCentroid
operator|.
name|getName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|aggName
argument_list|)
argument_list|)
expr_stmt|;
name|GeoPoint
name|centroid
init|=
name|geoCentroid
operator|.
name|centroid
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|centroid
argument_list|,
name|equalTo
argument_list|(
literal|null
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testUnmapped
specifier|public
name|void
name|testUnmapped
parameter_list|()
throws|throws
name|Exception
block|{
name|SearchResponse
name|response
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
name|UNMAPPED_IDX_NAME
argument_list|)
operator|.
name|addAggregation
argument_list|(
name|geoCentroid
argument_list|(
name|aggName
argument_list|)
operator|.
name|field
argument_list|(
name|SINGLE_VALUED_FIELD_NAME
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|assertSearchResponse
argument_list|(
name|response
argument_list|)
expr_stmt|;
name|GeoCentroid
name|geoCentroid
init|=
name|response
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
name|aggName
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|geoCentroid
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|geoCentroid
operator|.
name|getName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|aggName
argument_list|)
argument_list|)
expr_stmt|;
name|GeoPoint
name|centroid
init|=
name|geoCentroid
operator|.
name|centroid
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|centroid
argument_list|,
name|equalTo
argument_list|(
literal|null
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testPartiallyUnmapped
specifier|public
name|void
name|testPartiallyUnmapped
parameter_list|()
throws|throws
name|Exception
block|{
name|SearchResponse
name|response
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
name|IDX_NAME
argument_list|,
name|UNMAPPED_IDX_NAME
argument_list|)
operator|.
name|addAggregation
argument_list|(
name|geoCentroid
argument_list|(
name|aggName
argument_list|)
operator|.
name|field
argument_list|(
name|SINGLE_VALUED_FIELD_NAME
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|assertSearchResponse
argument_list|(
name|response
argument_list|)
expr_stmt|;
name|GeoCentroid
name|geoCentroid
init|=
name|response
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
name|aggName
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|geoCentroid
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|geoCentroid
operator|.
name|getName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|aggName
argument_list|)
argument_list|)
expr_stmt|;
name|GeoPoint
name|centroid
init|=
name|geoCentroid
operator|.
name|centroid
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|centroid
operator|.
name|lat
argument_list|()
argument_list|,
name|closeTo
argument_list|(
name|singleCentroid
operator|.
name|lat
argument_list|()
argument_list|,
name|GEOHASH_TOLERANCE
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|centroid
operator|.
name|lon
argument_list|()
argument_list|,
name|closeTo
argument_list|(
name|singleCentroid
operator|.
name|lon
argument_list|()
argument_list|,
name|GEOHASH_TOLERANCE
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testSingleValuedField
specifier|public
name|void
name|testSingleValuedField
parameter_list|()
throws|throws
name|Exception
block|{
name|SearchResponse
name|response
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
name|IDX_NAME
argument_list|)
operator|.
name|setQuery
argument_list|(
name|matchAllQuery
argument_list|()
argument_list|)
operator|.
name|addAggregation
argument_list|(
name|geoCentroid
argument_list|(
name|aggName
argument_list|)
operator|.
name|field
argument_list|(
name|SINGLE_VALUED_FIELD_NAME
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|assertSearchResponse
argument_list|(
name|response
argument_list|)
expr_stmt|;
name|GeoCentroid
name|geoCentroid
init|=
name|response
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
name|aggName
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|geoCentroid
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|geoCentroid
operator|.
name|getName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|aggName
argument_list|)
argument_list|)
expr_stmt|;
name|GeoPoint
name|centroid
init|=
name|geoCentroid
operator|.
name|centroid
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|centroid
operator|.
name|lat
argument_list|()
argument_list|,
name|closeTo
argument_list|(
name|singleCentroid
operator|.
name|lat
argument_list|()
argument_list|,
name|GEOHASH_TOLERANCE
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|centroid
operator|.
name|lon
argument_list|()
argument_list|,
name|closeTo
argument_list|(
name|singleCentroid
operator|.
name|lon
argument_list|()
argument_list|,
name|GEOHASH_TOLERANCE
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testSingleValueFieldGetProperty
specifier|public
name|void
name|testSingleValueFieldGetProperty
parameter_list|()
throws|throws
name|Exception
block|{
name|SearchResponse
name|response
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
name|IDX_NAME
argument_list|)
operator|.
name|setQuery
argument_list|(
name|matchAllQuery
argument_list|()
argument_list|)
operator|.
name|addAggregation
argument_list|(
name|global
argument_list|(
literal|"global"
argument_list|)
operator|.
name|subAggregation
argument_list|(
name|geoCentroid
argument_list|(
name|aggName
argument_list|)
operator|.
name|field
argument_list|(
name|SINGLE_VALUED_FIELD_NAME
argument_list|)
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|assertSearchResponse
argument_list|(
name|response
argument_list|)
expr_stmt|;
name|Global
name|global
init|=
name|response
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"global"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|global
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|global
operator|.
name|getName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"global"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|global
operator|.
name|getDocCount
argument_list|()
argument_list|,
name|equalTo
argument_list|(
operator|(
name|long
operator|)
name|numDocs
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|global
operator|.
name|getAggregations
argument_list|()
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|global
operator|.
name|getAggregations
argument_list|()
operator|.
name|asMap
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
name|GeoCentroid
name|geoCentroid
init|=
name|global
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
name|aggName
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|geoCentroid
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|geoCentroid
operator|.
name|getName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|aggName
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
call|(
name|GeoCentroid
call|)
argument_list|(
operator|(
name|InternalAggregation
operator|)
name|global
argument_list|)
operator|.
name|getProperty
argument_list|(
name|aggName
argument_list|)
argument_list|,
name|sameInstance
argument_list|(
name|geoCentroid
argument_list|)
argument_list|)
expr_stmt|;
name|GeoPoint
name|centroid
init|=
name|geoCentroid
operator|.
name|centroid
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|centroid
operator|.
name|lat
argument_list|()
argument_list|,
name|closeTo
argument_list|(
name|singleCentroid
operator|.
name|lat
argument_list|()
argument_list|,
name|GEOHASH_TOLERANCE
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|centroid
operator|.
name|lon
argument_list|()
argument_list|,
name|closeTo
argument_list|(
name|singleCentroid
operator|.
name|lon
argument_list|()
argument_list|,
name|GEOHASH_TOLERANCE
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
call|(
name|GeoPoint
call|)
argument_list|(
operator|(
name|InternalAggregation
operator|)
name|global
argument_list|)
operator|.
name|getProperty
argument_list|(
name|aggName
operator|+
literal|".value"
argument_list|)
operator|)
operator|.
name|lat
argument_list|()
argument_list|,
name|closeTo
argument_list|(
name|singleCentroid
operator|.
name|lat
argument_list|()
argument_list|,
name|GEOHASH_TOLERANCE
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
call|(
name|GeoPoint
call|)
argument_list|(
operator|(
name|InternalAggregation
operator|)
name|global
argument_list|)
operator|.
name|getProperty
argument_list|(
name|aggName
operator|+
literal|".value"
argument_list|)
operator|)
operator|.
name|lon
argument_list|()
argument_list|,
name|closeTo
argument_list|(
name|singleCentroid
operator|.
name|lon
argument_list|()
argument_list|,
name|GEOHASH_TOLERANCE
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
call|(
name|double
call|)
argument_list|(
operator|(
name|InternalAggregation
operator|)
name|global
argument_list|)
operator|.
name|getProperty
argument_list|(
name|aggName
operator|+
literal|".lat"
argument_list|)
argument_list|,
name|closeTo
argument_list|(
name|singleCentroid
operator|.
name|lat
argument_list|()
argument_list|,
name|GEOHASH_TOLERANCE
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
call|(
name|double
call|)
argument_list|(
operator|(
name|InternalAggregation
operator|)
name|global
argument_list|)
operator|.
name|getProperty
argument_list|(
name|aggName
operator|+
literal|".lon"
argument_list|)
argument_list|,
name|closeTo
argument_list|(
name|singleCentroid
operator|.
name|lon
argument_list|()
argument_list|,
name|GEOHASH_TOLERANCE
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testMultiValuedField
specifier|public
name|void
name|testMultiValuedField
parameter_list|()
throws|throws
name|Exception
block|{
name|SearchResponse
name|searchResponse
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
name|IDX_NAME
argument_list|)
operator|.
name|setQuery
argument_list|(
name|matchAllQuery
argument_list|()
argument_list|)
operator|.
name|addAggregation
argument_list|(
name|geoCentroid
argument_list|(
name|aggName
argument_list|)
operator|.
name|field
argument_list|(
name|MULTI_VALUED_FIELD_NAME
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|assertSearchResponse
argument_list|(
name|searchResponse
argument_list|)
expr_stmt|;
name|GeoCentroid
name|geoCentroid
init|=
name|searchResponse
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
name|aggName
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|geoCentroid
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|geoCentroid
operator|.
name|getName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|aggName
argument_list|)
argument_list|)
expr_stmt|;
name|GeoPoint
name|centroid
init|=
name|geoCentroid
operator|.
name|centroid
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|centroid
operator|.
name|lat
argument_list|()
argument_list|,
name|closeTo
argument_list|(
name|multiCentroid
operator|.
name|lat
argument_list|()
argument_list|,
name|GEOHASH_TOLERANCE
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|centroid
operator|.
name|lon
argument_list|()
argument_list|,
name|closeTo
argument_list|(
name|multiCentroid
operator|.
name|lon
argument_list|()
argument_list|,
name|GEOHASH_TOLERANCE
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testSingleValueFieldAsSubAggToGeohashGrid
specifier|public
name|void
name|testSingleValueFieldAsSubAggToGeohashGrid
parameter_list|()
throws|throws
name|Exception
block|{
name|SearchResponse
name|response
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
name|HIGH_CARD_IDX_NAME
argument_list|)
operator|.
name|addAggregation
argument_list|(
name|geohashGrid
argument_list|(
literal|"geoGrid"
argument_list|)
operator|.
name|field
argument_list|(
name|SINGLE_VALUED_FIELD_NAME
argument_list|)
operator|.
name|subAggregation
argument_list|(
name|geoCentroid
argument_list|(
name|aggName
argument_list|)
operator|.
name|field
argument_list|(
name|SINGLE_VALUED_FIELD_NAME
argument_list|)
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|assertSearchResponse
argument_list|(
name|response
argument_list|)
expr_stmt|;
name|GeoHashGrid
name|grid
init|=
name|response
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"geoGrid"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|grid
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|grid
operator|.
name|getName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"geoGrid"
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|GeoHashGrid
operator|.
name|Bucket
argument_list|>
name|buckets
init|=
name|grid
operator|.
name|getBuckets
argument_list|()
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
name|buckets
operator|.
name|size
argument_list|()
condition|;
operator|++
name|i
control|)
block|{
name|GeoHashGrid
operator|.
name|Bucket
name|cell
init|=
name|buckets
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|String
name|geohash
init|=
name|cell
operator|.
name|getKeyAsString
argument_list|()
decl_stmt|;
name|GeoPoint
name|expectedCentroid
init|=
name|expectedCentroidsForGeoHash
operator|.
name|get
argument_list|(
name|geohash
argument_list|)
decl_stmt|;
name|GeoCentroid
name|centroidAgg
init|=
name|cell
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
name|aggName
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
literal|"Geohash "
operator|+
name|geohash
operator|+
literal|" has wrong centroid latitude "
argument_list|,
name|expectedCentroid
operator|.
name|lat
argument_list|()
argument_list|,
name|closeTo
argument_list|(
name|centroidAgg
operator|.
name|centroid
argument_list|()
operator|.
name|lat
argument_list|()
argument_list|,
name|GEOHASH_TOLERANCE
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"Geohash "
operator|+
name|geohash
operator|+
literal|" has wrong centroid longitude"
argument_list|,
name|expectedCentroid
operator|.
name|lon
argument_list|()
argument_list|,
name|closeTo
argument_list|(
name|centroidAgg
operator|.
name|centroid
argument_list|()
operator|.
name|lon
argument_list|()
argument_list|,
name|GEOHASH_TOLERANCE
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

