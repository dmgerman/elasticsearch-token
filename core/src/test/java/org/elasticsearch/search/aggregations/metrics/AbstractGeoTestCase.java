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
name|com
operator|.
name|carrotsearch
operator|.
name|hppc
operator|.
name|ObjectIntHashMap
import|;
end_import

begin_import
import|import
name|com
operator|.
name|carrotsearch
operator|.
name|hppc
operator|.
name|ObjectIntMap
import|;
end_import

begin_import
import|import
name|com
operator|.
name|carrotsearch
operator|.
name|hppc
operator|.
name|ObjectObjectHashMap
import|;
end_import

begin_import
import|import
name|com
operator|.
name|carrotsearch
operator|.
name|hppc
operator|.
name|ObjectObjectMap
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
name|index
operator|.
name|IndexRequestBuilder
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
name|settings
operator|.
name|Settings
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
name|ToXContent
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
name|XContentBuilder
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
name|search
operator|.
name|SearchHit
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
name|SearchHitField
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
name|sort
operator|.
name|SortBuilders
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
name|sort
operator|.
name|SortOrder
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
name|List
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|xcontent
operator|.
name|XContentFactory
operator|.
name|jsonBuilder
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
name|assertAcked
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
name|equalTo
import|;
end_import

begin_class
annotation|@
name|ESIntegTestCase
operator|.
name|SuiteScopeTestCase
DECL|class|AbstractGeoTestCase
specifier|public
specifier|abstract
class|class
name|AbstractGeoTestCase
extends|extends
name|ESIntegTestCase
block|{
DECL|field|SINGLE_VALUED_FIELD_NAME
specifier|protected
specifier|static
specifier|final
name|String
name|SINGLE_VALUED_FIELD_NAME
init|=
literal|"geo_value"
decl_stmt|;
DECL|field|MULTI_VALUED_FIELD_NAME
specifier|protected
specifier|static
specifier|final
name|String
name|MULTI_VALUED_FIELD_NAME
init|=
literal|"geo_values"
decl_stmt|;
DECL|field|NUMBER_FIELD_NAME
specifier|protected
specifier|static
specifier|final
name|String
name|NUMBER_FIELD_NAME
init|=
literal|"l_values"
decl_stmt|;
DECL|field|UNMAPPED_IDX_NAME
specifier|protected
specifier|static
specifier|final
name|String
name|UNMAPPED_IDX_NAME
init|=
literal|"idx_unmapped"
decl_stmt|;
DECL|field|IDX_NAME
specifier|protected
specifier|static
specifier|final
name|String
name|IDX_NAME
init|=
literal|"idx"
decl_stmt|;
DECL|field|EMPTY_IDX_NAME
specifier|protected
specifier|static
specifier|final
name|String
name|EMPTY_IDX_NAME
init|=
literal|"empty_idx"
decl_stmt|;
DECL|field|DATELINE_IDX_NAME
specifier|protected
specifier|static
specifier|final
name|String
name|DATELINE_IDX_NAME
init|=
literal|"dateline_idx"
decl_stmt|;
DECL|field|HIGH_CARD_IDX_NAME
specifier|protected
specifier|static
specifier|final
name|String
name|HIGH_CARD_IDX_NAME
init|=
literal|"high_card_idx"
decl_stmt|;
DECL|field|IDX_ZERO_NAME
specifier|protected
specifier|static
specifier|final
name|String
name|IDX_ZERO_NAME
init|=
literal|"idx_zero"
decl_stmt|;
DECL|field|numDocs
specifier|protected
specifier|static
name|int
name|numDocs
decl_stmt|;
DECL|field|numUniqueGeoPoints
specifier|protected
specifier|static
name|int
name|numUniqueGeoPoints
decl_stmt|;
DECL|field|singleValues
DECL|field|multiValues
specifier|protected
specifier|static
name|GeoPoint
index|[]
name|singleValues
decl_stmt|,
name|multiValues
decl_stmt|;
DECL|field|singleTopLeft
DECL|field|singleBottomRight
DECL|field|multiTopLeft
DECL|field|multiBottomRight
DECL|field|singleCentroid
DECL|field|multiCentroid
DECL|field|unmappedCentroid
specifier|protected
specifier|static
name|GeoPoint
name|singleTopLeft
decl_stmt|,
name|singleBottomRight
decl_stmt|,
name|multiTopLeft
decl_stmt|,
name|multiBottomRight
decl_stmt|,
name|singleCentroid
decl_stmt|,
name|multiCentroid
decl_stmt|,
name|unmappedCentroid
decl_stmt|;
DECL|field|expectedDocCountsForGeoHash
specifier|protected
specifier|static
name|ObjectIntMap
argument_list|<
name|String
argument_list|>
name|expectedDocCountsForGeoHash
init|=
literal|null
decl_stmt|;
DECL|field|expectedCentroidsForGeoHash
specifier|protected
specifier|static
name|ObjectObjectMap
argument_list|<
name|String
argument_list|,
name|GeoPoint
argument_list|>
name|expectedCentroidsForGeoHash
init|=
literal|null
decl_stmt|;
DECL|field|GEOHASH_TOLERANCE
specifier|protected
specifier|static
specifier|final
name|double
name|GEOHASH_TOLERANCE
init|=
literal|1E
operator|-
literal|5D
decl_stmt|;
annotation|@
name|Override
DECL|method|setupSuiteScopeCluster
specifier|public
name|void
name|setupSuiteScopeCluster
parameter_list|()
throws|throws
name|Exception
block|{
name|createIndex
argument_list|(
name|UNMAPPED_IDX_NAME
argument_list|)
expr_stmt|;
name|assertAcked
argument_list|(
name|prepareCreate
argument_list|(
name|IDX_NAME
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"type"
argument_list|,
name|SINGLE_VALUED_FIELD_NAME
argument_list|,
literal|"type=geo_point"
argument_list|,
name|MULTI_VALUED_FIELD_NAME
argument_list|,
literal|"type=geo_point"
argument_list|,
name|NUMBER_FIELD_NAME
argument_list|,
literal|"type=long"
argument_list|,
literal|"tag"
argument_list|,
literal|"type=keyword"
argument_list|)
argument_list|)
expr_stmt|;
name|singleTopLeft
operator|=
operator|new
name|GeoPoint
argument_list|(
name|Double
operator|.
name|NEGATIVE_INFINITY
argument_list|,
name|Double
operator|.
name|POSITIVE_INFINITY
argument_list|)
expr_stmt|;
name|singleBottomRight
operator|=
operator|new
name|GeoPoint
argument_list|(
name|Double
operator|.
name|POSITIVE_INFINITY
argument_list|,
name|Double
operator|.
name|NEGATIVE_INFINITY
argument_list|)
expr_stmt|;
name|multiTopLeft
operator|=
operator|new
name|GeoPoint
argument_list|(
name|Double
operator|.
name|NEGATIVE_INFINITY
argument_list|,
name|Double
operator|.
name|POSITIVE_INFINITY
argument_list|)
expr_stmt|;
name|multiBottomRight
operator|=
operator|new
name|GeoPoint
argument_list|(
name|Double
operator|.
name|POSITIVE_INFINITY
argument_list|,
name|Double
operator|.
name|NEGATIVE_INFINITY
argument_list|)
expr_stmt|;
name|singleCentroid
operator|=
operator|new
name|GeoPoint
argument_list|(
literal|0
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|multiCentroid
operator|=
operator|new
name|GeoPoint
argument_list|(
literal|0
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|unmappedCentroid
operator|=
operator|new
name|GeoPoint
argument_list|(
literal|0
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|numDocs
operator|=
name|randomIntBetween
argument_list|(
literal|6
argument_list|,
literal|20
argument_list|)
expr_stmt|;
name|numUniqueGeoPoints
operator|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
name|numDocs
argument_list|)
expr_stmt|;
name|expectedDocCountsForGeoHash
operator|=
operator|new
name|ObjectIntHashMap
argument_list|<>
argument_list|(
name|numDocs
operator|*
literal|2
argument_list|)
expr_stmt|;
name|expectedCentroidsForGeoHash
operator|=
operator|new
name|ObjectObjectHashMap
argument_list|<>
argument_list|(
name|numDocs
operator|*
literal|2
argument_list|)
expr_stmt|;
name|singleValues
operator|=
operator|new
name|GeoPoint
index|[
name|numUniqueGeoPoints
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
name|singleValues
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|singleValues
index|[
name|i
index|]
operator|=
name|RandomGeoGenerator
operator|.
name|randomPoint
argument_list|(
name|random
argument_list|()
argument_list|)
expr_stmt|;
name|updateBoundsTopLeft
argument_list|(
name|singleValues
index|[
name|i
index|]
argument_list|,
name|singleTopLeft
argument_list|)
expr_stmt|;
name|updateBoundsBottomRight
argument_list|(
name|singleValues
index|[
name|i
index|]
argument_list|,
name|singleBottomRight
argument_list|)
expr_stmt|;
block|}
name|multiValues
operator|=
operator|new
name|GeoPoint
index|[
name|numUniqueGeoPoints
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
name|multiValues
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|multiValues
index|[
name|i
index|]
operator|=
name|RandomGeoGenerator
operator|.
name|randomPoint
argument_list|(
name|random
argument_list|()
argument_list|)
expr_stmt|;
name|updateBoundsTopLeft
argument_list|(
name|multiValues
index|[
name|i
index|]
argument_list|,
name|multiTopLeft
argument_list|)
expr_stmt|;
name|updateBoundsBottomRight
argument_list|(
name|multiValues
index|[
name|i
index|]
argument_list|,
name|multiBottomRight
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|IndexRequestBuilder
argument_list|>
name|builders
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|GeoPoint
name|singleVal
decl_stmt|;
specifier|final
name|GeoPoint
index|[]
name|multiVal
init|=
operator|new
name|GeoPoint
index|[
literal|2
index|]
decl_stmt|;
name|double
name|newMVLat
decl_stmt|,
name|newMVLon
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
name|numDocs
condition|;
name|i
operator|++
control|)
block|{
name|singleVal
operator|=
name|singleValues
index|[
name|i
operator|%
name|numUniqueGeoPoints
index|]
expr_stmt|;
name|multiVal
index|[
literal|0
index|]
operator|=
name|multiValues
index|[
name|i
operator|%
name|numUniqueGeoPoints
index|]
expr_stmt|;
name|multiVal
index|[
literal|1
index|]
operator|=
name|multiValues
index|[
operator|(
name|i
operator|+
literal|1
operator|)
operator|%
name|numUniqueGeoPoints
index|]
expr_stmt|;
name|builders
operator|.
name|add
argument_list|(
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
name|IDX_NAME
argument_list|,
literal|"type"
argument_list|)
operator|.
name|setSource
argument_list|(
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|array
argument_list|(
name|SINGLE_VALUED_FIELD_NAME
argument_list|,
name|singleVal
operator|.
name|lon
argument_list|()
argument_list|,
name|singleVal
operator|.
name|lat
argument_list|()
argument_list|)
operator|.
name|startArray
argument_list|(
name|MULTI_VALUED_FIELD_NAME
argument_list|)
operator|.
name|startArray
argument_list|()
operator|.
name|value
argument_list|(
name|multiVal
index|[
literal|0
index|]
operator|.
name|lon
argument_list|()
argument_list|)
operator|.
name|value
argument_list|(
name|multiVal
index|[
literal|0
index|]
operator|.
name|lat
argument_list|()
argument_list|)
operator|.
name|endArray
argument_list|()
operator|.
name|startArray
argument_list|()
operator|.
name|value
argument_list|(
name|multiVal
index|[
literal|1
index|]
operator|.
name|lon
argument_list|()
argument_list|)
operator|.
name|value
argument_list|(
name|multiVal
index|[
literal|1
index|]
operator|.
name|lat
argument_list|()
argument_list|)
operator|.
name|endArray
argument_list|()
operator|.
name|endArray
argument_list|()
operator|.
name|field
argument_list|(
name|NUMBER_FIELD_NAME
argument_list|,
name|i
argument_list|)
operator|.
name|field
argument_list|(
literal|"tag"
argument_list|,
literal|"tag"
operator|+
name|i
argument_list|)
operator|.
name|endObject
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|singleCentroid
operator|=
name|singleCentroid
operator|.
name|reset
argument_list|(
name|singleCentroid
operator|.
name|lat
argument_list|()
operator|+
operator|(
name|singleVal
operator|.
name|lat
argument_list|()
operator|-
name|singleCentroid
operator|.
name|lat
argument_list|()
operator|)
operator|/
operator|(
name|i
operator|+
literal|1
operator|)
argument_list|,
name|singleCentroid
operator|.
name|lon
argument_list|()
operator|+
operator|(
name|singleVal
operator|.
name|lon
argument_list|()
operator|-
name|singleCentroid
operator|.
name|lon
argument_list|()
operator|)
operator|/
operator|(
name|i
operator|+
literal|1
operator|)
argument_list|)
expr_stmt|;
name|newMVLat
operator|=
operator|(
name|multiVal
index|[
literal|0
index|]
operator|.
name|lat
argument_list|()
operator|+
name|multiVal
index|[
literal|1
index|]
operator|.
name|lat
argument_list|()
operator|)
operator|/
literal|2d
expr_stmt|;
name|newMVLon
operator|=
operator|(
name|multiVal
index|[
literal|0
index|]
operator|.
name|lon
argument_list|()
operator|+
name|multiVal
index|[
literal|1
index|]
operator|.
name|lon
argument_list|()
operator|)
operator|/
literal|2d
expr_stmt|;
name|multiCentroid
operator|=
name|multiCentroid
operator|.
name|reset
argument_list|(
name|multiCentroid
operator|.
name|lat
argument_list|()
operator|+
operator|(
name|newMVLat
operator|-
name|multiCentroid
operator|.
name|lat
argument_list|()
operator|)
operator|/
operator|(
name|i
operator|+
literal|1
operator|)
argument_list|,
name|multiCentroid
operator|.
name|lon
argument_list|()
operator|+
operator|(
name|newMVLon
operator|-
name|multiCentroid
operator|.
name|lon
argument_list|()
operator|)
operator|/
operator|(
name|i
operator|+
literal|1
operator|)
argument_list|)
expr_stmt|;
block|}
name|assertAcked
argument_list|(
name|prepareCreate
argument_list|(
name|EMPTY_IDX_NAME
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"type"
argument_list|,
name|SINGLE_VALUED_FIELD_NAME
argument_list|,
literal|"type=geo_point"
argument_list|)
argument_list|)
expr_stmt|;
name|assertAcked
argument_list|(
name|prepareCreate
argument_list|(
name|DATELINE_IDX_NAME
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"type"
argument_list|,
name|SINGLE_VALUED_FIELD_NAME
argument_list|,
literal|"type=geo_point"
argument_list|,
name|MULTI_VALUED_FIELD_NAME
argument_list|,
literal|"type=geo_point"
argument_list|,
name|NUMBER_FIELD_NAME
argument_list|,
literal|"type=long"
argument_list|,
literal|"tag"
argument_list|,
literal|"type=keyword"
argument_list|)
argument_list|)
expr_stmt|;
name|GeoPoint
index|[]
name|geoValues
init|=
operator|new
name|GeoPoint
index|[
literal|5
index|]
decl_stmt|;
name|geoValues
index|[
literal|0
index|]
operator|=
operator|new
name|GeoPoint
argument_list|(
literal|38
argument_list|,
literal|178
argument_list|)
expr_stmt|;
name|geoValues
index|[
literal|1
index|]
operator|=
operator|new
name|GeoPoint
argument_list|(
literal|12
argument_list|,
operator|-
literal|179
argument_list|)
expr_stmt|;
name|geoValues
index|[
literal|2
index|]
operator|=
operator|new
name|GeoPoint
argument_list|(
operator|-
literal|24
argument_list|,
literal|170
argument_list|)
expr_stmt|;
name|geoValues
index|[
literal|3
index|]
operator|=
operator|new
name|GeoPoint
argument_list|(
literal|32
argument_list|,
operator|-
literal|175
argument_list|)
expr_stmt|;
name|geoValues
index|[
literal|4
index|]
operator|=
operator|new
name|GeoPoint
argument_list|(
operator|-
literal|11
argument_list|,
literal|178
argument_list|)
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
literal|5
condition|;
name|i
operator|++
control|)
block|{
name|builders
operator|.
name|add
argument_list|(
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
name|DATELINE_IDX_NAME
argument_list|,
literal|"type"
argument_list|)
operator|.
name|setSource
argument_list|(
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|array
argument_list|(
name|SINGLE_VALUED_FIELD_NAME
argument_list|,
name|geoValues
index|[
name|i
index|]
operator|.
name|lon
argument_list|()
argument_list|,
name|geoValues
index|[
name|i
index|]
operator|.
name|lat
argument_list|()
argument_list|)
operator|.
name|field
argument_list|(
name|NUMBER_FIELD_NAME
argument_list|,
name|i
argument_list|)
operator|.
name|field
argument_list|(
literal|"tag"
argument_list|,
literal|"tag"
operator|+
name|i
argument_list|)
operator|.
name|endObject
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|assertAcked
argument_list|(
name|prepareCreate
argument_list|(
name|HIGH_CARD_IDX_NAME
argument_list|)
operator|.
name|setSettings
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"number_of_shards"
argument_list|,
literal|2
argument_list|)
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"type"
argument_list|,
name|SINGLE_VALUED_FIELD_NAME
argument_list|,
literal|"type=geo_point"
argument_list|,
name|MULTI_VALUED_FIELD_NAME
argument_list|,
literal|"type=geo_point"
argument_list|,
name|NUMBER_FIELD_NAME
argument_list|,
literal|"type=long,store=true"
argument_list|,
literal|"tag"
argument_list|,
literal|"type=keyword"
argument_list|)
argument_list|)
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
literal|2000
condition|;
name|i
operator|++
control|)
block|{
name|singleVal
operator|=
name|singleValues
index|[
name|i
operator|%
name|numUniqueGeoPoints
index|]
expr_stmt|;
name|builders
operator|.
name|add
argument_list|(
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
name|HIGH_CARD_IDX_NAME
argument_list|,
literal|"type"
argument_list|)
operator|.
name|setSource
argument_list|(
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|array
argument_list|(
name|SINGLE_VALUED_FIELD_NAME
argument_list|,
name|singleVal
operator|.
name|lon
argument_list|()
argument_list|,
name|singleVal
operator|.
name|lat
argument_list|()
argument_list|)
operator|.
name|startArray
argument_list|(
name|MULTI_VALUED_FIELD_NAME
argument_list|)
operator|.
name|startArray
argument_list|()
operator|.
name|value
argument_list|(
name|multiValues
index|[
name|i
operator|%
name|numUniqueGeoPoints
index|]
operator|.
name|lon
argument_list|()
argument_list|)
operator|.
name|value
argument_list|(
name|multiValues
index|[
name|i
operator|%
name|numUniqueGeoPoints
index|]
operator|.
name|lat
argument_list|()
argument_list|)
operator|.
name|endArray
argument_list|()
operator|.
name|startArray
argument_list|()
operator|.
name|value
argument_list|(
name|multiValues
index|[
operator|(
name|i
operator|+
literal|1
operator|)
operator|%
name|numUniqueGeoPoints
index|]
operator|.
name|lon
argument_list|()
argument_list|)
operator|.
name|value
argument_list|(
name|multiValues
index|[
operator|(
name|i
operator|+
literal|1
operator|)
operator|%
name|numUniqueGeoPoints
index|]
operator|.
name|lat
argument_list|()
argument_list|)
operator|.
name|endArray
argument_list|()
operator|.
name|endArray
argument_list|()
operator|.
name|field
argument_list|(
name|NUMBER_FIELD_NAME
argument_list|,
name|i
argument_list|)
operator|.
name|field
argument_list|(
literal|"tag"
argument_list|,
literal|"tag"
operator|+
name|i
argument_list|)
operator|.
name|endObject
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|updateGeohashBucketsCentroid
argument_list|(
name|singleVal
argument_list|)
expr_stmt|;
block|}
name|builders
operator|.
name|add
argument_list|(
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
name|IDX_ZERO_NAME
argument_list|,
literal|"type"
argument_list|)
operator|.
name|setSource
argument_list|(
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|array
argument_list|(
name|SINGLE_VALUED_FIELD_NAME
argument_list|,
literal|0.0
argument_list|,
literal|1.0
argument_list|)
operator|.
name|endObject
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertAcked
argument_list|(
name|prepareCreate
argument_list|(
name|IDX_ZERO_NAME
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"type"
argument_list|,
name|SINGLE_VALUED_FIELD_NAME
argument_list|,
literal|"type=geo_point"
argument_list|)
argument_list|)
expr_stmt|;
name|indexRandom
argument_list|(
literal|true
argument_list|,
name|builders
argument_list|)
expr_stmt|;
name|ensureSearchable
argument_list|()
expr_stmt|;
comment|// Added to debug a test failure where the terms aggregation seems to be reporting two documents with the same value for NUMBER_FIELD_NAME.  This will check that after
comment|// random indexing each document only has 1 value for NUMBER_FIELD_NAME and it is the correct value. Following this initial change its seems that this call was getting
comment|// more that 2000 hits (actual value was 2059) so now it will also check to ensure all hits have the correct index and type
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
name|addStoredField
argument_list|(
name|NUMBER_FIELD_NAME
argument_list|)
operator|.
name|addSort
argument_list|(
name|SortBuilders
operator|.
name|fieldSort
argument_list|(
name|NUMBER_FIELD_NAME
argument_list|)
operator|.
name|order
argument_list|(
name|SortOrder
operator|.
name|ASC
argument_list|)
argument_list|)
operator|.
name|setSize
argument_list|(
literal|5000
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertSearchResponse
argument_list|(
name|response
argument_list|)
expr_stmt|;
name|long
name|totalHits
init|=
name|response
operator|.
name|getHits
argument_list|()
operator|.
name|totalHits
argument_list|()
decl_stmt|;
name|XContentBuilder
name|builder
init|=
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
decl_stmt|;
name|response
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|ToXContent
operator|.
name|EMPTY_PARAMS
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"Full high_card_idx Response Content:\n{ {} }"
argument_list|,
name|builder
operator|.
name|string
argument_list|()
argument_list|)
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
name|totalHits
condition|;
name|i
operator|++
control|)
block|{
name|SearchHit
name|searchHit
init|=
name|response
operator|.
name|getHits
argument_list|()
operator|.
name|getAt
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
literal|"Hit "
operator|+
name|i
operator|+
literal|" with id: "
operator|+
name|searchHit
operator|.
name|getId
argument_list|()
argument_list|,
name|searchHit
operator|.
name|getIndex
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"high_card_idx"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"Hit "
operator|+
name|i
operator|+
literal|" with id: "
operator|+
name|searchHit
operator|.
name|getId
argument_list|()
argument_list|,
name|searchHit
operator|.
name|getType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"type"
argument_list|)
argument_list|)
expr_stmt|;
name|SearchHitField
name|hitField
init|=
name|searchHit
operator|.
name|field
argument_list|(
name|NUMBER_FIELD_NAME
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
literal|"Hit "
operator|+
name|i
operator|+
literal|" has wrong number of values"
argument_list|,
name|hitField
operator|.
name|getValues
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
name|Long
name|value
init|=
name|hitField
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
literal|"Hit "
operator|+
name|i
operator|+
literal|" has wrong value"
argument_list|,
name|value
operator|.
name|intValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|assertThat
argument_list|(
name|totalHits
argument_list|,
name|equalTo
argument_list|(
literal|2000L
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|updateGeohashBucketsCentroid
specifier|private
name|void
name|updateGeohashBucketsCentroid
parameter_list|(
specifier|final
name|GeoPoint
name|location
parameter_list|)
block|{
name|String
name|hash
init|=
name|GeoHashUtils
operator|.
name|stringEncode
argument_list|(
name|location
operator|.
name|lon
argument_list|()
argument_list|,
name|location
operator|.
name|lat
argument_list|()
argument_list|,
name|GeoHashUtils
operator|.
name|PRECISION
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|precision
init|=
name|GeoHashUtils
operator|.
name|PRECISION
init|;
name|precision
operator|>
literal|0
condition|;
operator|--
name|precision
control|)
block|{
specifier|final
name|String
name|h
init|=
name|hash
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|precision
argument_list|)
decl_stmt|;
name|expectedDocCountsForGeoHash
operator|.
name|put
argument_list|(
name|h
argument_list|,
name|expectedDocCountsForGeoHash
operator|.
name|getOrDefault
argument_list|(
name|h
argument_list|,
literal|0
argument_list|)
operator|+
literal|1
argument_list|)
expr_stmt|;
name|expectedCentroidsForGeoHash
operator|.
name|put
argument_list|(
name|h
argument_list|,
name|updateHashCentroid
argument_list|(
name|h
argument_list|,
name|location
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|updateHashCentroid
specifier|private
name|GeoPoint
name|updateHashCentroid
parameter_list|(
name|String
name|hash
parameter_list|,
specifier|final
name|GeoPoint
name|location
parameter_list|)
block|{
name|GeoPoint
name|centroid
init|=
name|expectedCentroidsForGeoHash
operator|.
name|getOrDefault
argument_list|(
name|hash
argument_list|,
literal|null
argument_list|)
decl_stmt|;
if|if
condition|(
name|centroid
operator|==
literal|null
condition|)
block|{
return|return
operator|new
name|GeoPoint
argument_list|(
name|location
operator|.
name|lat
argument_list|()
argument_list|,
name|location
operator|.
name|lon
argument_list|()
argument_list|)
return|;
block|}
specifier|final
name|int
name|docCount
init|=
name|expectedDocCountsForGeoHash
operator|.
name|get
argument_list|(
name|hash
argument_list|)
decl_stmt|;
specifier|final
name|double
name|newLon
init|=
name|centroid
operator|.
name|lon
argument_list|()
operator|+
operator|(
name|location
operator|.
name|lon
argument_list|()
operator|-
name|centroid
operator|.
name|lon
argument_list|()
operator|)
operator|/
name|docCount
decl_stmt|;
specifier|final
name|double
name|newLat
init|=
name|centroid
operator|.
name|lat
argument_list|()
operator|+
operator|(
name|location
operator|.
name|lat
argument_list|()
operator|-
name|centroid
operator|.
name|lat
argument_list|()
operator|)
operator|/
name|docCount
decl_stmt|;
return|return
name|centroid
operator|.
name|reset
argument_list|(
name|newLat
argument_list|,
name|newLon
argument_list|)
return|;
block|}
DECL|method|updateBoundsBottomRight
specifier|private
name|void
name|updateBoundsBottomRight
parameter_list|(
name|GeoPoint
name|geoPoint
parameter_list|,
name|GeoPoint
name|currentBound
parameter_list|)
block|{
if|if
condition|(
name|geoPoint
operator|.
name|lat
argument_list|()
operator|<
name|currentBound
operator|.
name|lat
argument_list|()
condition|)
block|{
name|currentBound
operator|.
name|resetLat
argument_list|(
name|geoPoint
operator|.
name|lat
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|geoPoint
operator|.
name|lon
argument_list|()
operator|>
name|currentBound
operator|.
name|lon
argument_list|()
condition|)
block|{
name|currentBound
operator|.
name|resetLon
argument_list|(
name|geoPoint
operator|.
name|lon
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|updateBoundsTopLeft
specifier|private
name|void
name|updateBoundsTopLeft
parameter_list|(
name|GeoPoint
name|geoPoint
parameter_list|,
name|GeoPoint
name|currentBound
parameter_list|)
block|{
if|if
condition|(
name|geoPoint
operator|.
name|lat
argument_list|()
operator|>
name|currentBound
operator|.
name|lat
argument_list|()
condition|)
block|{
name|currentBound
operator|.
name|resetLat
argument_list|(
name|geoPoint
operator|.
name|lat
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|geoPoint
operator|.
name|lon
argument_list|()
operator|<
name|currentBound
operator|.
name|lon
argument_list|()
condition|)
block|{
name|currentBound
operator|.
name|resetLon
argument_list|(
name|geoPoint
operator|.
name|lon
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

