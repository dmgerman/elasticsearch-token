begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.test.integration.search.geo
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|integration
operator|.
name|search
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
name|integration
operator|.
name|AbstractNodesTests
import|;
end_import

begin_import
import|import
name|org
operator|.
name|testng
operator|.
name|annotations
operator|.
name|AfterClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|testng
operator|.
name|annotations
operator|.
name|BeforeClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|testng
operator|.
name|annotations
operator|.
name|Test
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
name|*
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
name|FilterBuilders
operator|.
name|*
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
name|*
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|MatcherAssert
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
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|GeoDistanceTests
specifier|public
class|class
name|GeoDistanceTests
extends|extends
name|AbstractNodesTests
block|{
DECL|field|client
specifier|private
name|Client
name|client
decl_stmt|;
DECL|method|createNodes
annotation|@
name|BeforeClass
specifier|public
name|void
name|createNodes
parameter_list|()
throws|throws
name|Exception
block|{
name|startNode
argument_list|(
literal|"server1"
argument_list|)
expr_stmt|;
name|startNode
argument_list|(
literal|"server2"
argument_list|)
expr_stmt|;
name|client
operator|=
name|getClient
argument_list|()
expr_stmt|;
block|}
DECL|method|closeNodes
annotation|@
name|AfterClass
specifier|public
name|void
name|closeNodes
parameter_list|()
block|{
name|client
operator|.
name|close
argument_list|()
expr_stmt|;
name|closeAllNodes
argument_list|()
expr_stmt|;
block|}
DECL|method|getClient
specifier|protected
name|Client
name|getClient
parameter_list|()
block|{
return|return
name|client
argument_list|(
literal|"server1"
argument_list|)
return|;
block|}
DECL|method|simpleDistanceTests
annotation|@
name|Test
specifier|public
name|void
name|simpleDistanceTests
parameter_list|()
throws|throws
name|Exception
block|{
try|try
block|{
name|client
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareDelete
argument_list|(
literal|"test"
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// ignore
block|}
name|String
name|mapping
init|=
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
name|field
argument_list|(
literal|"lat_lon"
argument_list|,
literal|true
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
name|string
argument_list|()
decl_stmt|;
name|client
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
name|addMapping
argument_list|(
literal|"type1"
argument_list|,
name|mapping
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|client
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
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
literal|"1"
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
name|field
argument_list|(
literal|"name"
argument_list|,
literal|"New York"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"location"
argument_list|)
operator|.
name|field
argument_list|(
literal|"lat"
argument_list|,
literal|40.7143528
argument_list|)
operator|.
name|field
argument_list|(
literal|"lon"
argument_list|,
operator|-
literal|74.0059731
argument_list|)
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
comment|// to NY: 5.286 km
name|client
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
literal|"2"
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
name|field
argument_list|(
literal|"name"
argument_list|,
literal|"Times Square"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"location"
argument_list|)
operator|.
name|field
argument_list|(
literal|"lat"
argument_list|,
literal|40.759011
argument_list|)
operator|.
name|field
argument_list|(
literal|"lon"
argument_list|,
operator|-
literal|73.9844722
argument_list|)
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
comment|// to NY: 0.4621 km
name|client
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
literal|"3"
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
name|field
argument_list|(
literal|"name"
argument_list|,
literal|"Tribeca"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"location"
argument_list|)
operator|.
name|field
argument_list|(
literal|"lat"
argument_list|,
literal|40.718266
argument_list|)
operator|.
name|field
argument_list|(
literal|"lon"
argument_list|,
operator|-
literal|74.007819
argument_list|)
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
comment|// to NY: 1.055 km
name|client
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
literal|"4"
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
name|field
argument_list|(
literal|"name"
argument_list|,
literal|"Wall Street"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"location"
argument_list|)
operator|.
name|field
argument_list|(
literal|"lat"
argument_list|,
literal|40.7051157
argument_list|)
operator|.
name|field
argument_list|(
literal|"lon"
argument_list|,
operator|-
literal|74.0088305
argument_list|)
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
comment|// to NY: 1.258 km
name|client
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
literal|"5"
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
name|field
argument_list|(
literal|"name"
argument_list|,
literal|"Soho"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"location"
argument_list|)
operator|.
name|field
argument_list|(
literal|"lat"
argument_list|,
literal|40.7247222
argument_list|)
operator|.
name|field
argument_list|(
literal|"lon"
argument_list|,
operator|-
literal|74
argument_list|)
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
comment|// to NY: 2.029 km
name|client
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
literal|"6"
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
name|field
argument_list|(
literal|"name"
argument_list|,
literal|"Greenwich Village"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"location"
argument_list|)
operator|.
name|field
argument_list|(
literal|"lat"
argument_list|,
literal|40.731033
argument_list|)
operator|.
name|field
argument_list|(
literal|"lon"
argument_list|,
operator|-
literal|73.9962255
argument_list|)
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
comment|// to NY: 8.572 km
name|client
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
literal|"7"
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
name|field
argument_list|(
literal|"name"
argument_list|,
literal|"Brooklyn"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"location"
argument_list|)
operator|.
name|field
argument_list|(
literal|"lat"
argument_list|,
literal|40.65
argument_list|)
operator|.
name|field
argument_list|(
literal|"lon"
argument_list|,
operator|-
literal|73.95
argument_list|)
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
name|SearchResponse
name|searchResponse
init|=
name|client
operator|.
name|prepareSearch
argument_list|()
comment|// from NY
operator|.
name|setQuery
argument_list|(
name|filteredQuery
argument_list|(
name|matchAllQuery
argument_list|()
argument_list|,
name|geoDistanceFilter
argument_list|(
literal|"location"
argument_list|)
operator|.
name|distance
argument_list|(
literal|"3km"
argument_list|)
operator|.
name|point
argument_list|(
literal|40.7143528
argument_list|,
operator|-
literal|74.0059731
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
name|assertThat
argument_list|(
name|searchResponse
operator|.
name|hits
argument_list|()
operator|.
name|getTotalHits
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|5l
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|searchResponse
operator|.
name|hits
argument_list|()
operator|.
name|hits
argument_list|()
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
literal|5
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|SearchHit
name|hit
range|:
name|searchResponse
operator|.
name|hits
argument_list|()
control|)
block|{
name|assertThat
argument_list|(
name|hit
operator|.
name|id
argument_list|()
argument_list|,
name|anyOf
argument_list|(
name|equalTo
argument_list|(
literal|"1"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"3"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"4"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"5"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"6"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|searchResponse
operator|=
name|client
operator|.
name|prepareSearch
argument_list|()
comment|// from NY
operator|.
name|setQuery
argument_list|(
name|filteredQuery
argument_list|(
name|matchAllQuery
argument_list|()
argument_list|,
name|geoDistanceFilter
argument_list|(
literal|"location"
argument_list|)
operator|.
name|distance
argument_list|(
literal|"2km"
argument_list|)
operator|.
name|point
argument_list|(
literal|40.7143528
argument_list|,
operator|-
literal|74.0059731
argument_list|)
argument_list|)
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
name|searchResponse
operator|.
name|hits
argument_list|()
operator|.
name|getTotalHits
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|4l
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|searchResponse
operator|.
name|hits
argument_list|()
operator|.
name|hits
argument_list|()
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
literal|4
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|SearchHit
name|hit
range|:
name|searchResponse
operator|.
name|hits
argument_list|()
control|)
block|{
name|assertThat
argument_list|(
name|hit
operator|.
name|id
argument_list|()
argument_list|,
name|anyOf
argument_list|(
name|equalTo
argument_list|(
literal|"1"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"3"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"4"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"5"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|searchResponse
operator|=
name|client
operator|.
name|prepareSearch
argument_list|()
comment|// from NY
operator|.
name|setQuery
argument_list|(
name|filteredQuery
argument_list|(
name|matchAllQuery
argument_list|()
argument_list|,
name|geoDistanceFilter
argument_list|(
literal|"location"
argument_list|)
operator|.
name|distance
argument_list|(
literal|"1.242mi"
argument_list|)
operator|.
name|point
argument_list|(
literal|40.7143528
argument_list|,
operator|-
literal|74.0059731
argument_list|)
argument_list|)
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
name|searchResponse
operator|.
name|hits
argument_list|()
operator|.
name|getTotalHits
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|4l
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|searchResponse
operator|.
name|hits
argument_list|()
operator|.
name|hits
argument_list|()
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
literal|4
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|SearchHit
name|hit
range|:
name|searchResponse
operator|.
name|hits
argument_list|()
control|)
block|{
name|assertThat
argument_list|(
name|hit
operator|.
name|id
argument_list|()
argument_list|,
name|anyOf
argument_list|(
name|equalTo
argument_list|(
literal|"1"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"3"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"4"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"5"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|searchResponse
operator|=
name|client
operator|.
name|prepareSearch
argument_list|()
comment|// from NY
operator|.
name|setQuery
argument_list|(
name|filteredQuery
argument_list|(
name|matchAllQuery
argument_list|()
argument_list|,
name|geoDistanceRangeFilter
argument_list|(
literal|"location"
argument_list|)
operator|.
name|from
argument_list|(
literal|"1.0km"
argument_list|)
operator|.
name|to
argument_list|(
literal|"2.0km"
argument_list|)
operator|.
name|point
argument_list|(
literal|40.7143528
argument_list|,
operator|-
literal|74.0059731
argument_list|)
argument_list|)
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
name|searchResponse
operator|.
name|hits
argument_list|()
operator|.
name|getTotalHits
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|2l
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|searchResponse
operator|.
name|hits
argument_list|()
operator|.
name|hits
argument_list|()
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|SearchHit
name|hit
range|:
name|searchResponse
operator|.
name|hits
argument_list|()
control|)
block|{
name|assertThat
argument_list|(
name|hit
operator|.
name|id
argument_list|()
argument_list|,
name|anyOf
argument_list|(
name|equalTo
argument_list|(
literal|"4"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"5"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// SORTING
name|searchResponse
operator|=
name|client
operator|.
name|prepareSearch
argument_list|()
operator|.
name|setQuery
argument_list|(
name|matchAllQuery
argument_list|()
argument_list|)
operator|.
name|addSort
argument_list|(
name|SortBuilders
operator|.
name|geoDistanceSort
argument_list|(
literal|"location"
argument_list|)
operator|.
name|point
argument_list|(
literal|40.7143528
argument_list|,
operator|-
literal|74.0059731
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
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|searchResponse
operator|.
name|hits
argument_list|()
operator|.
name|getTotalHits
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|7l
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|searchResponse
operator|.
name|hits
argument_list|()
operator|.
name|hits
argument_list|()
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
literal|7
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|searchResponse
operator|.
name|hits
argument_list|()
operator|.
name|getAt
argument_list|(
literal|0
argument_list|)
operator|.
name|id
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|searchResponse
operator|.
name|hits
argument_list|()
operator|.
name|getAt
argument_list|(
literal|1
argument_list|)
operator|.
name|id
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"3"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|searchResponse
operator|.
name|hits
argument_list|()
operator|.
name|getAt
argument_list|(
literal|2
argument_list|)
operator|.
name|id
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"4"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|searchResponse
operator|.
name|hits
argument_list|()
operator|.
name|getAt
argument_list|(
literal|3
argument_list|)
operator|.
name|id
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"5"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|searchResponse
operator|.
name|hits
argument_list|()
operator|.
name|getAt
argument_list|(
literal|4
argument_list|)
operator|.
name|id
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"6"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|searchResponse
operator|.
name|hits
argument_list|()
operator|.
name|getAt
argument_list|(
literal|5
argument_list|)
operator|.
name|id
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"2"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|searchResponse
operator|.
name|hits
argument_list|()
operator|.
name|getAt
argument_list|(
literal|6
argument_list|)
operator|.
name|id
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"7"
argument_list|)
argument_list|)
expr_stmt|;
name|searchResponse
operator|=
name|client
operator|.
name|prepareSearch
argument_list|()
operator|.
name|setQuery
argument_list|(
name|matchAllQuery
argument_list|()
argument_list|)
operator|.
name|addSort
argument_list|(
name|SortBuilders
operator|.
name|geoDistanceSort
argument_list|(
literal|"location"
argument_list|)
operator|.
name|point
argument_list|(
literal|40.7143528
argument_list|,
operator|-
literal|74.0059731
argument_list|)
operator|.
name|order
argument_list|(
name|SortOrder
operator|.
name|DESC
argument_list|)
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
name|searchResponse
operator|.
name|hits
argument_list|()
operator|.
name|getTotalHits
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|7l
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|searchResponse
operator|.
name|hits
argument_list|()
operator|.
name|hits
argument_list|()
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
literal|7
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|searchResponse
operator|.
name|hits
argument_list|()
operator|.
name|getAt
argument_list|(
literal|6
argument_list|)
operator|.
name|id
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|searchResponse
operator|.
name|hits
argument_list|()
operator|.
name|getAt
argument_list|(
literal|5
argument_list|)
operator|.
name|id
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"3"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|searchResponse
operator|.
name|hits
argument_list|()
operator|.
name|getAt
argument_list|(
literal|4
argument_list|)
operator|.
name|id
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"4"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|searchResponse
operator|.
name|hits
argument_list|()
operator|.
name|getAt
argument_list|(
literal|3
argument_list|)
operator|.
name|id
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"5"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|searchResponse
operator|.
name|hits
argument_list|()
operator|.
name|getAt
argument_list|(
literal|2
argument_list|)
operator|.
name|id
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"6"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|searchResponse
operator|.
name|hits
argument_list|()
operator|.
name|getAt
argument_list|(
literal|1
argument_list|)
operator|.
name|id
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"2"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|searchResponse
operator|.
name|hits
argument_list|()
operator|.
name|getAt
argument_list|(
literal|0
argument_list|)
operator|.
name|id
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"7"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

