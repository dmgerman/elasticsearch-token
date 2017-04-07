begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.bucket
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|bucket
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
name|cursors
operator|.
name|ObjectIntCursor
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|Version
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
name|cluster
operator|.
name|metadata
operator|.
name|IndexMetaData
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
name|XContentBuilder
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
name|GeoBoundingBoxQueryBuilder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|plugins
operator|.
name|Plugin
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
name|AggregationBuilders
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
name|filter
operator|.
name|Filter
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
name|geogrid
operator|.
name|GeoHashGrid
operator|.
name|Bucket
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
name|InternalSettingsPlugin
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
name|VersionUtils
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
name|HashSet
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
import|import
name|java
operator|.
name|util
operator|.
name|Random
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
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
name|geo
operator|.
name|GeoHashUtils
operator|.
name|PRECISION
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
name|geo
operator|.
name|GeoHashUtils
operator|.
name|stringEncode
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
name|containsString
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
DECL|class|GeoHashGridIT
specifier|public
class|class
name|GeoHashGridIT
extends|extends
name|ESIntegTestCase
block|{
annotation|@
name|Override
DECL|method|nodePlugins
specifier|protected
name|Collection
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|Plugin
argument_list|>
argument_list|>
name|nodePlugins
parameter_list|()
block|{
return|return
name|Arrays
operator|.
name|asList
argument_list|(
name|InternalSettingsPlugin
operator|.
name|class
argument_list|)
return|;
comment|// uses index.version.created
block|}
DECL|field|version
specifier|private
name|Version
name|version
init|=
name|VersionUtils
operator|.
name|randomVersionBetween
argument_list|(
name|random
argument_list|()
argument_list|,
name|Version
operator|.
name|V_2_0_0
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
decl_stmt|;
DECL|field|expectedDocCountsForGeoHash
specifier|static
name|ObjectIntMap
argument_list|<
name|String
argument_list|>
name|expectedDocCountsForGeoHash
init|=
literal|null
decl_stmt|;
DECL|field|multiValuedExpectedDocCountsForGeoHash
specifier|static
name|ObjectIntMap
argument_list|<
name|String
argument_list|>
name|multiValuedExpectedDocCountsForGeoHash
init|=
literal|null
decl_stmt|;
DECL|field|numDocs
specifier|static
name|int
name|numDocs
init|=
literal|100
decl_stmt|;
DECL|field|smallestGeoHash
specifier|static
name|String
name|smallestGeoHash
init|=
literal|null
decl_stmt|;
DECL|method|indexCity
specifier|private
specifier|static
name|IndexRequestBuilder
name|indexCity
parameter_list|(
name|String
name|index
parameter_list|,
name|String
name|name
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|latLon
parameter_list|)
throws|throws
name|Exception
block|{
name|XContentBuilder
name|source
init|=
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"city"
argument_list|,
name|name
argument_list|)
decl_stmt|;
if|if
condition|(
name|latLon
operator|!=
literal|null
condition|)
block|{
name|source
operator|=
name|source
operator|.
name|field
argument_list|(
literal|"location"
argument_list|,
name|latLon
argument_list|)
expr_stmt|;
block|}
name|source
operator|=
name|source
operator|.
name|endObject
argument_list|()
expr_stmt|;
return|return
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
name|index
argument_list|,
literal|"type"
argument_list|)
operator|.
name|setSource
argument_list|(
name|source
argument_list|)
return|;
block|}
DECL|method|indexCity
specifier|private
specifier|static
name|IndexRequestBuilder
name|indexCity
parameter_list|(
name|String
name|index
parameter_list|,
name|String
name|name
parameter_list|,
name|String
name|latLon
parameter_list|)
throws|throws
name|Exception
block|{
return|return
name|indexCity
argument_list|(
name|index
argument_list|,
name|name
argument_list|,
name|Arrays
operator|.
expr|<
name|String
operator|>
name|asList
argument_list|(
name|latLon
argument_list|)
argument_list|)
return|;
block|}
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
literal|"idx_unmapped"
argument_list|)
expr_stmt|;
name|Settings
name|settings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_VERSION_CREATED
argument_list|,
name|version
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|assertAcked
argument_list|(
name|prepareCreate
argument_list|(
literal|"idx"
argument_list|)
operator|.
name|setSettings
argument_list|(
name|settings
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"type"
argument_list|,
literal|"location"
argument_list|,
literal|"type=geo_point"
argument_list|,
literal|"city"
argument_list|,
literal|"type=keyword"
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|IndexRequestBuilder
argument_list|>
name|cities
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|Random
name|random
init|=
name|random
argument_list|()
decl_stmt|;
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
comment|//generate random point
name|double
name|lat
init|=
operator|(
literal|180d
operator|*
name|random
operator|.
name|nextDouble
argument_list|()
operator|)
operator|-
literal|90d
decl_stmt|;
name|double
name|lng
init|=
operator|(
literal|360d
operator|*
name|random
operator|.
name|nextDouble
argument_list|()
operator|)
operator|-
literal|180d
decl_stmt|;
name|String
name|randomGeoHash
init|=
name|stringEncode
argument_list|(
name|lng
argument_list|,
name|lat
argument_list|,
name|PRECISION
argument_list|)
decl_stmt|;
comment|//Index at the highest resolution
name|cities
operator|.
name|add
argument_list|(
name|indexCity
argument_list|(
literal|"idx"
argument_list|,
name|randomGeoHash
argument_list|,
name|lat
operator|+
literal|", "
operator|+
name|lng
argument_list|)
argument_list|)
expr_stmt|;
name|expectedDocCountsForGeoHash
operator|.
name|put
argument_list|(
name|randomGeoHash
argument_list|,
name|expectedDocCountsForGeoHash
operator|.
name|getOrDefault
argument_list|(
name|randomGeoHash
argument_list|,
literal|0
argument_list|)
operator|+
literal|1
argument_list|)
expr_stmt|;
comment|//Update expected doc counts for all resolutions..
for|for
control|(
name|int
name|precision
init|=
name|PRECISION
operator|-
literal|1
init|;
name|precision
operator|>
literal|0
condition|;
name|precision
operator|--
control|)
block|{
name|String
name|hash
init|=
name|stringEncode
argument_list|(
name|lng
argument_list|,
name|lat
argument_list|,
name|precision
argument_list|)
decl_stmt|;
if|if
condition|(
operator|(
name|smallestGeoHash
operator|==
literal|null
operator|)
operator|||
operator|(
name|hash
operator|.
name|length
argument_list|()
operator|<
name|smallestGeoHash
operator|.
name|length
argument_list|()
operator|)
condition|)
block|{
name|smallestGeoHash
operator|=
name|hash
expr_stmt|;
block|}
name|expectedDocCountsForGeoHash
operator|.
name|put
argument_list|(
name|hash
argument_list|,
name|expectedDocCountsForGeoHash
operator|.
name|getOrDefault
argument_list|(
name|hash
argument_list|,
literal|0
argument_list|)
operator|+
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
name|indexRandom
argument_list|(
literal|true
argument_list|,
name|cities
argument_list|)
expr_stmt|;
name|assertAcked
argument_list|(
name|prepareCreate
argument_list|(
literal|"multi_valued_idx"
argument_list|)
operator|.
name|setSettings
argument_list|(
name|settings
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"type"
argument_list|,
literal|"location"
argument_list|,
literal|"type=geo_point"
argument_list|,
literal|"city"
argument_list|,
literal|"type=keyword"
argument_list|)
argument_list|)
expr_stmt|;
name|cities
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
name|multiValuedExpectedDocCountsForGeoHash
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
specifier|final
name|int
name|numPoints
init|=
name|random
operator|.
name|nextInt
argument_list|(
literal|4
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|points
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|geoHashes
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|numPoints
condition|;
operator|++
name|j
control|)
block|{
name|double
name|lat
init|=
operator|(
literal|180d
operator|*
name|random
operator|.
name|nextDouble
argument_list|()
operator|)
operator|-
literal|90d
decl_stmt|;
name|double
name|lng
init|=
operator|(
literal|360d
operator|*
name|random
operator|.
name|nextDouble
argument_list|()
operator|)
operator|-
literal|180d
decl_stmt|;
name|points
operator|.
name|add
argument_list|(
name|lat
operator|+
literal|","
operator|+
name|lng
argument_list|)
expr_stmt|;
comment|// Update expected doc counts for all resolutions..
for|for
control|(
name|int
name|precision
init|=
name|PRECISION
init|;
name|precision
operator|>
literal|0
condition|;
name|precision
operator|--
control|)
block|{
specifier|final
name|String
name|geoHash
init|=
name|stringEncode
argument_list|(
name|lng
argument_list|,
name|lat
argument_list|,
name|precision
argument_list|)
decl_stmt|;
name|geoHashes
operator|.
name|add
argument_list|(
name|geoHash
argument_list|)
expr_stmt|;
block|}
block|}
name|cities
operator|.
name|add
argument_list|(
name|indexCity
argument_list|(
literal|"multi_valued_idx"
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|i
argument_list|)
argument_list|,
name|points
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|hash
range|:
name|geoHashes
control|)
block|{
name|multiValuedExpectedDocCountsForGeoHash
operator|.
name|put
argument_list|(
name|hash
argument_list|,
name|multiValuedExpectedDocCountsForGeoHash
operator|.
name|getOrDefault
argument_list|(
name|hash
argument_list|,
literal|0
argument_list|)
operator|+
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
name|indexRandom
argument_list|(
literal|true
argument_list|,
name|cities
argument_list|)
expr_stmt|;
name|ensureSearchable
argument_list|()
expr_stmt|;
block|}
DECL|method|testSimple
specifier|public
name|void
name|testSimple
parameter_list|()
throws|throws
name|Exception
block|{
for|for
control|(
name|int
name|precision
init|=
literal|1
init|;
name|precision
operator|<=
name|PRECISION
condition|;
name|precision
operator|++
control|)
block|{
name|SearchResponse
name|response
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"idx"
argument_list|)
operator|.
name|addAggregation
argument_list|(
name|geohashGrid
argument_list|(
literal|"geohashgrid"
argument_list|)
operator|.
name|field
argument_list|(
literal|"location"
argument_list|)
operator|.
name|precision
argument_list|(
name|precision
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
name|geoGrid
init|=
name|response
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"geohashgrid"
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Bucket
argument_list|>
name|buckets
init|=
name|geoGrid
operator|.
name|getBuckets
argument_list|()
decl_stmt|;
name|Object
index|[]
name|propertiesKeys
init|=
operator|(
name|Object
index|[]
operator|)
operator|(
operator|(
name|InternalAggregation
operator|)
name|geoGrid
operator|)
operator|.
name|getProperty
argument_list|(
literal|"_key"
argument_list|)
decl_stmt|;
name|Object
index|[]
name|propertiesDocCounts
init|=
operator|(
name|Object
index|[]
operator|)
operator|(
operator|(
name|InternalAggregation
operator|)
name|geoGrid
operator|)
operator|.
name|getProperty
argument_list|(
literal|"_count"
argument_list|)
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
name|i
operator|++
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
name|long
name|bucketCount
init|=
name|cell
operator|.
name|getDocCount
argument_list|()
decl_stmt|;
name|int
name|expectedBucketCount
init|=
name|expectedDocCountsForGeoHash
operator|.
name|get
argument_list|(
name|geohash
argument_list|)
decl_stmt|;
name|assertNotSame
argument_list|(
name|bucketCount
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Geohash "
operator|+
name|geohash
operator|+
literal|" has wrong doc count "
argument_list|,
name|expectedBucketCount
argument_list|,
name|bucketCount
argument_list|)
expr_stmt|;
name|GeoPoint
name|geoPoint
init|=
operator|(
name|GeoPoint
operator|)
name|propertiesKeys
index|[
name|i
index|]
decl_stmt|;
name|assertThat
argument_list|(
name|stringEncode
argument_list|(
name|geoPoint
operator|.
name|lon
argument_list|()
argument_list|,
name|geoPoint
operator|.
name|lat
argument_list|()
argument_list|,
name|precision
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|geohash
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
name|long
operator|)
name|propertiesDocCounts
index|[
name|i
index|]
argument_list|,
name|equalTo
argument_list|(
name|bucketCount
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|testMultivalued
specifier|public
name|void
name|testMultivalued
parameter_list|()
throws|throws
name|Exception
block|{
for|for
control|(
name|int
name|precision
init|=
literal|1
init|;
name|precision
operator|<=
name|PRECISION
condition|;
name|precision
operator|++
control|)
block|{
name|SearchResponse
name|response
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"multi_valued_idx"
argument_list|)
operator|.
name|addAggregation
argument_list|(
name|geohashGrid
argument_list|(
literal|"geohashgrid"
argument_list|)
operator|.
name|field
argument_list|(
literal|"location"
argument_list|)
operator|.
name|precision
argument_list|(
name|precision
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
name|geoGrid
init|=
name|response
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"geohashgrid"
argument_list|)
decl_stmt|;
for|for
control|(
name|GeoHashGrid
operator|.
name|Bucket
name|cell
range|:
name|geoGrid
operator|.
name|getBuckets
argument_list|()
control|)
block|{
name|String
name|geohash
init|=
name|cell
operator|.
name|getKeyAsString
argument_list|()
decl_stmt|;
name|long
name|bucketCount
init|=
name|cell
operator|.
name|getDocCount
argument_list|()
decl_stmt|;
name|int
name|expectedBucketCount
init|=
name|multiValuedExpectedDocCountsForGeoHash
operator|.
name|get
argument_list|(
name|geohash
argument_list|)
decl_stmt|;
name|assertNotSame
argument_list|(
name|bucketCount
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Geohash "
operator|+
name|geohash
operator|+
literal|" has wrong doc count "
argument_list|,
name|expectedBucketCount
argument_list|,
name|bucketCount
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|testFiltered
specifier|public
name|void
name|testFiltered
parameter_list|()
throws|throws
name|Exception
block|{
name|GeoBoundingBoxQueryBuilder
name|bbox
init|=
operator|new
name|GeoBoundingBoxQueryBuilder
argument_list|(
literal|"location"
argument_list|)
decl_stmt|;
name|bbox
operator|.
name|setCorners
argument_list|(
name|smallestGeoHash
argument_list|)
operator|.
name|queryName
argument_list|(
literal|"bbox"
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|precision
init|=
literal|1
init|;
name|precision
operator|<=
name|PRECISION
condition|;
name|precision
operator|++
control|)
block|{
name|SearchResponse
name|response
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"idx"
argument_list|)
operator|.
name|addAggregation
argument_list|(
name|AggregationBuilders
operator|.
name|filter
argument_list|(
literal|"filtered"
argument_list|,
name|bbox
argument_list|)
operator|.
name|subAggregation
argument_list|(
name|geohashGrid
argument_list|(
literal|"geohashgrid"
argument_list|)
operator|.
name|field
argument_list|(
literal|"location"
argument_list|)
operator|.
name|precision
argument_list|(
name|precision
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
name|Filter
name|filter
init|=
name|response
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"filtered"
argument_list|)
decl_stmt|;
name|GeoHashGrid
name|geoGrid
init|=
name|filter
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"geohashgrid"
argument_list|)
decl_stmt|;
for|for
control|(
name|GeoHashGrid
operator|.
name|Bucket
name|cell
range|:
name|geoGrid
operator|.
name|getBuckets
argument_list|()
control|)
block|{
name|String
name|geohash
init|=
name|cell
operator|.
name|getKeyAsString
argument_list|()
decl_stmt|;
name|long
name|bucketCount
init|=
name|cell
operator|.
name|getDocCount
argument_list|()
decl_stmt|;
name|int
name|expectedBucketCount
init|=
name|expectedDocCountsForGeoHash
operator|.
name|get
argument_list|(
name|geohash
argument_list|)
decl_stmt|;
name|assertNotSame
argument_list|(
name|bucketCount
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Buckets must be filtered"
argument_list|,
name|geohash
operator|.
name|startsWith
argument_list|(
name|smallestGeoHash
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Geohash "
operator|+
name|geohash
operator|+
literal|" has wrong doc count "
argument_list|,
name|expectedBucketCount
argument_list|,
name|bucketCount
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|testUnmapped
specifier|public
name|void
name|testUnmapped
parameter_list|()
throws|throws
name|Exception
block|{
for|for
control|(
name|int
name|precision
init|=
literal|1
init|;
name|precision
operator|<=
name|PRECISION
condition|;
name|precision
operator|++
control|)
block|{
name|SearchResponse
name|response
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"idx_unmapped"
argument_list|)
operator|.
name|addAggregation
argument_list|(
name|geohashGrid
argument_list|(
literal|"geohashgrid"
argument_list|)
operator|.
name|field
argument_list|(
literal|"location"
argument_list|)
operator|.
name|precision
argument_list|(
name|precision
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
name|geoGrid
init|=
name|response
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"geohashgrid"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|geoGrid
operator|.
name|getBuckets
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testPartiallyUnmapped
specifier|public
name|void
name|testPartiallyUnmapped
parameter_list|()
throws|throws
name|Exception
block|{
for|for
control|(
name|int
name|precision
init|=
literal|1
init|;
name|precision
operator|<=
name|PRECISION
condition|;
name|precision
operator|++
control|)
block|{
name|SearchResponse
name|response
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"idx"
argument_list|,
literal|"idx_unmapped"
argument_list|)
operator|.
name|addAggregation
argument_list|(
name|geohashGrid
argument_list|(
literal|"geohashgrid"
argument_list|)
operator|.
name|field
argument_list|(
literal|"location"
argument_list|)
operator|.
name|precision
argument_list|(
name|precision
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
name|geoGrid
init|=
name|response
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"geohashgrid"
argument_list|)
decl_stmt|;
for|for
control|(
name|GeoHashGrid
operator|.
name|Bucket
name|cell
range|:
name|geoGrid
operator|.
name|getBuckets
argument_list|()
control|)
block|{
name|String
name|geohash
init|=
name|cell
operator|.
name|getKeyAsString
argument_list|()
decl_stmt|;
name|long
name|bucketCount
init|=
name|cell
operator|.
name|getDocCount
argument_list|()
decl_stmt|;
name|int
name|expectedBucketCount
init|=
name|expectedDocCountsForGeoHash
operator|.
name|get
argument_list|(
name|geohash
argument_list|)
decl_stmt|;
name|assertNotSame
argument_list|(
name|bucketCount
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Geohash "
operator|+
name|geohash
operator|+
literal|" has wrong doc count "
argument_list|,
name|expectedBucketCount
argument_list|,
name|bucketCount
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|testTopMatch
specifier|public
name|void
name|testTopMatch
parameter_list|()
throws|throws
name|Exception
block|{
for|for
control|(
name|int
name|precision
init|=
literal|1
init|;
name|precision
operator|<=
name|PRECISION
condition|;
name|precision
operator|++
control|)
block|{
name|SearchResponse
name|response
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"idx"
argument_list|)
operator|.
name|addAggregation
argument_list|(
name|geohashGrid
argument_list|(
literal|"geohashgrid"
argument_list|)
operator|.
name|field
argument_list|(
literal|"location"
argument_list|)
operator|.
name|size
argument_list|(
literal|1
argument_list|)
operator|.
name|shardSize
argument_list|(
literal|100
argument_list|)
operator|.
name|precision
argument_list|(
name|precision
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
name|geoGrid
init|=
name|response
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"geohashgrid"
argument_list|)
decl_stmt|;
comment|//Check we only have one bucket with the best match for that resolution
name|assertThat
argument_list|(
name|geoGrid
operator|.
name|getBuckets
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
for|for
control|(
name|GeoHashGrid
operator|.
name|Bucket
name|cell
range|:
name|geoGrid
operator|.
name|getBuckets
argument_list|()
control|)
block|{
name|String
name|geohash
init|=
name|cell
operator|.
name|getKeyAsString
argument_list|()
decl_stmt|;
name|long
name|bucketCount
init|=
name|cell
operator|.
name|getDocCount
argument_list|()
decl_stmt|;
name|int
name|expectedBucketCount
init|=
literal|0
decl_stmt|;
for|for
control|(
name|ObjectIntCursor
argument_list|<
name|String
argument_list|>
name|cursor
range|:
name|expectedDocCountsForGeoHash
control|)
block|{
if|if
condition|(
name|cursor
operator|.
name|key
operator|.
name|length
argument_list|()
operator|==
name|precision
condition|)
block|{
name|expectedBucketCount
operator|=
name|Math
operator|.
name|max
argument_list|(
name|expectedBucketCount
argument_list|,
name|cursor
operator|.
name|value
argument_list|)
expr_stmt|;
block|}
block|}
name|assertNotSame
argument_list|(
name|bucketCount
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Geohash "
operator|+
name|geohash
operator|+
literal|" has wrong doc count "
argument_list|,
name|expectedBucketCount
argument_list|,
name|bucketCount
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|testSizeIsZero
specifier|public
name|void
name|testSizeIsZero
parameter_list|()
block|{
specifier|final
name|int
name|size
init|=
literal|0
decl_stmt|;
specifier|final
name|int
name|shardSize
init|=
literal|10000
decl_stmt|;
name|IllegalArgumentException
name|exception
init|=
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"idx"
argument_list|)
operator|.
name|addAggregation
argument_list|(
name|geohashGrid
argument_list|(
literal|"geohashgrid"
argument_list|)
operator|.
name|field
argument_list|(
literal|"location"
argument_list|)
operator|.
name|size
argument_list|(
name|size
argument_list|)
operator|.
name|shardSize
argument_list|(
name|shardSize
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|exception
operator|.
name|getMessage
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"[size] must be greater than 0. Found [0] in [geohashgrid]"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testShardSizeIsZero
specifier|public
name|void
name|testShardSizeIsZero
parameter_list|()
block|{
specifier|final
name|int
name|size
init|=
literal|100
decl_stmt|;
specifier|final
name|int
name|shardSize
init|=
literal|0
decl_stmt|;
name|IllegalArgumentException
name|exception
init|=
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"idx"
argument_list|)
operator|.
name|addAggregation
argument_list|(
name|geohashGrid
argument_list|(
literal|"geohashgrid"
argument_list|)
operator|.
name|field
argument_list|(
literal|"location"
argument_list|)
operator|.
name|size
argument_list|(
name|size
argument_list|)
operator|.
name|shardSize
argument_list|(
name|shardSize
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|exception
operator|.
name|getMessage
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"[shardSize] must be greater than 0. Found [0] in [geohashgrid]"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

