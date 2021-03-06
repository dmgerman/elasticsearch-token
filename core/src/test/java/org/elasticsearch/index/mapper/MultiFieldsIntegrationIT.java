begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.mapper
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
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
name|admin
operator|.
name|indices
operator|.
name|mapping
operator|.
name|get
operator|.
name|GetMappingsResponse
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
name|MappingMetaData
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
name|common
operator|.
name|xcontent
operator|.
name|support
operator|.
name|XContentMapValues
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
name|Map
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|support
operator|.
name|WriteRequest
operator|.
name|RefreshPolicy
operator|.
name|IMMEDIATE
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
name|constantScoreQuery
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
name|geoDistanceQuery
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
name|matchQuery
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
name|not
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
name|nullValue
import|;
end_import

begin_class
DECL|class|MultiFieldsIntegrationIT
specifier|public
class|class
name|MultiFieldsIntegrationIT
extends|extends
name|ESIntegTestCase
block|{
DECL|method|testMultiFields
specifier|public
name|void
name|testMultiFields
parameter_list|()
throws|throws
name|Exception
block|{
name|assertAcked
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
name|prepareCreate
argument_list|(
literal|"my-index"
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"my-type"
argument_list|,
name|createTypeSource
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|GetMappingsResponse
name|getMappingsResponse
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
name|prepareGetMappings
argument_list|(
literal|"my-index"
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|MappingMetaData
name|mappingMetaData
init|=
name|getMappingsResponse
operator|.
name|mappings
argument_list|()
operator|.
name|get
argument_list|(
literal|"my-index"
argument_list|)
operator|.
name|get
argument_list|(
literal|"my-type"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|mappingMetaData
argument_list|,
name|not
argument_list|(
name|nullValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|mappingSource
init|=
name|mappingMetaData
operator|.
name|sourceAsMap
argument_list|()
decl_stmt|;
name|Map
name|titleFields
init|=
operator|(
operator|(
name|Map
operator|)
name|XContentMapValues
operator|.
name|extractValue
argument_list|(
literal|"properties.title.fields"
argument_list|,
name|mappingSource
argument_list|)
operator|)
decl_stmt|;
name|assertThat
argument_list|(
name|titleFields
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
name|titleFields
operator|.
name|get
argument_list|(
literal|"not_analyzed"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|Map
operator|)
name|titleFields
operator|.
name|get
argument_list|(
literal|"not_analyzed"
argument_list|)
operator|)
operator|.
name|get
argument_list|(
literal|"type"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"keyword"
argument_list|)
argument_list|)
expr_stmt|;
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"my-index"
argument_list|,
literal|"my-type"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"title"
argument_list|,
literal|"Multi fields"
argument_list|)
operator|.
name|setRefreshPolicy
argument_list|(
name|IMMEDIATE
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|SearchResponse
name|searchResponse
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"my-index"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|matchQuery
argument_list|(
literal|"title"
argument_list|,
literal|"multi"
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|searchResponse
operator|.
name|getHits
argument_list|()
operator|.
name|getTotalHits
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1L
argument_list|)
argument_list|)
expr_stmt|;
name|searchResponse
operator|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"my-index"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|matchQuery
argument_list|(
literal|"title.not_analyzed"
argument_list|,
literal|"Multi fields"
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|searchResponse
operator|.
name|getHits
argument_list|()
operator|.
name|getTotalHits
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1L
argument_list|)
argument_list|)
expr_stmt|;
name|assertAcked
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
name|preparePutMapping
argument_list|(
literal|"my-index"
argument_list|)
operator|.
name|setType
argument_list|(
literal|"my-type"
argument_list|)
operator|.
name|setSource
argument_list|(
name|createPutMappingSource
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|getMappingsResponse
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
name|prepareGetMappings
argument_list|(
literal|"my-index"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|mappingMetaData
operator|=
name|getMappingsResponse
operator|.
name|mappings
argument_list|()
operator|.
name|get
argument_list|(
literal|"my-index"
argument_list|)
operator|.
name|get
argument_list|(
literal|"my-type"
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|mappingMetaData
argument_list|,
name|not
argument_list|(
name|nullValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|mappingSource
operator|=
name|mappingMetaData
operator|.
name|sourceAsMap
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|Map
operator|)
name|XContentMapValues
operator|.
name|extractValue
argument_list|(
literal|"properties.title"
argument_list|,
name|mappingSource
argument_list|)
operator|)
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|titleFields
operator|=
operator|(
operator|(
name|Map
operator|)
name|XContentMapValues
operator|.
name|extractValue
argument_list|(
literal|"properties.title.fields"
argument_list|,
name|mappingSource
argument_list|)
operator|)
expr_stmt|;
name|assertThat
argument_list|(
name|titleFields
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|titleFields
operator|.
name|get
argument_list|(
literal|"not_analyzed"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|Map
operator|)
name|titleFields
operator|.
name|get
argument_list|(
literal|"not_analyzed"
argument_list|)
operator|)
operator|.
name|get
argument_list|(
literal|"type"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"keyword"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|titleFields
operator|.
name|get
argument_list|(
literal|"uncased"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|Map
operator|)
name|titleFields
operator|.
name|get
argument_list|(
literal|"uncased"
argument_list|)
operator|)
operator|.
name|get
argument_list|(
literal|"analyzer"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"whitespace"
argument_list|)
argument_list|)
expr_stmt|;
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"my-index"
argument_list|,
literal|"my-type"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"title"
argument_list|,
literal|"Multi fields"
argument_list|)
operator|.
name|setRefreshPolicy
argument_list|(
name|IMMEDIATE
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|searchResponse
operator|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"my-index"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|matchQuery
argument_list|(
literal|"title.uncased"
argument_list|,
literal|"Multi"
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|searchResponse
operator|.
name|getHits
argument_list|()
operator|.
name|getTotalHits
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1L
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testGeoPointMultiField
specifier|public
name|void
name|testGeoPointMultiField
parameter_list|()
throws|throws
name|Exception
block|{
name|assertAcked
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
name|prepareCreate
argument_list|(
literal|"my-index"
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"my-type"
argument_list|,
name|createMappingSource
argument_list|(
literal|"geo_point"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|GetMappingsResponse
name|getMappingsResponse
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
name|prepareGetMappings
argument_list|(
literal|"my-index"
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|MappingMetaData
name|mappingMetaData
init|=
name|getMappingsResponse
operator|.
name|mappings
argument_list|()
operator|.
name|get
argument_list|(
literal|"my-index"
argument_list|)
operator|.
name|get
argument_list|(
literal|"my-type"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|mappingMetaData
argument_list|,
name|not
argument_list|(
name|nullValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|mappingSource
init|=
name|mappingMetaData
operator|.
name|sourceAsMap
argument_list|()
decl_stmt|;
name|Map
name|aField
init|=
operator|(
operator|(
name|Map
operator|)
name|XContentMapValues
operator|.
name|extractValue
argument_list|(
literal|"properties.a"
argument_list|,
name|mappingSource
argument_list|)
operator|)
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"Keys: {}"
argument_list|,
name|aField
operator|.
name|keySet
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|aField
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|aField
operator|.
name|get
argument_list|(
literal|"type"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"geo_point"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|aField
operator|.
name|get
argument_list|(
literal|"fields"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|Map
name|bField
init|=
operator|(
operator|(
name|Map
operator|)
name|XContentMapValues
operator|.
name|extractValue
argument_list|(
literal|"properties.a.fields.b"
argument_list|,
name|mappingSource
argument_list|)
operator|)
decl_stmt|;
name|assertThat
argument_list|(
name|bField
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
name|bField
operator|.
name|get
argument_list|(
literal|"type"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"keyword"
argument_list|)
argument_list|)
expr_stmt|;
name|GeoPoint
name|point
init|=
operator|new
name|GeoPoint
argument_list|(
literal|51
argument_list|,
literal|19
argument_list|)
decl_stmt|;
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"my-index"
argument_list|,
literal|"my-type"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"a"
argument_list|,
name|point
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|setRefreshPolicy
argument_list|(
name|IMMEDIATE
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|SearchResponse
name|countResponse
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"my-index"
argument_list|)
operator|.
name|setSize
argument_list|(
literal|0
argument_list|)
operator|.
name|setQuery
argument_list|(
name|constantScoreQuery
argument_list|(
name|geoDistanceQuery
argument_list|(
literal|"a"
argument_list|)
operator|.
name|point
argument_list|(
literal|51
argument_list|,
literal|19
argument_list|)
operator|.
name|distance
argument_list|(
literal|50
argument_list|,
name|DistanceUnit
operator|.
name|KILOMETERS
argument_list|)
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|countResponse
operator|.
name|getHits
argument_list|()
operator|.
name|getTotalHits
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1L
argument_list|)
argument_list|)
expr_stmt|;
name|countResponse
operator|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"my-index"
argument_list|)
operator|.
name|setSize
argument_list|(
literal|0
argument_list|)
operator|.
name|setQuery
argument_list|(
name|matchQuery
argument_list|(
literal|"a.b"
argument_list|,
name|point
operator|.
name|geohash
argument_list|()
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|countResponse
operator|.
name|getHits
argument_list|()
operator|.
name|getTotalHits
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1L
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testTokenCountMultiField
specifier|public
name|void
name|testTokenCountMultiField
parameter_list|()
throws|throws
name|Exception
block|{
name|assertAcked
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
name|prepareCreate
argument_list|(
literal|"my-index"
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"my-type"
argument_list|,
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
literal|"my-type"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"properties"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"a"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"token_count"
argument_list|)
operator|.
name|field
argument_list|(
literal|"analyzer"
argument_list|,
literal|"simple"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"fields"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"b"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"keyword"
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
argument_list|)
expr_stmt|;
name|GetMappingsResponse
name|getMappingsResponse
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
name|prepareGetMappings
argument_list|(
literal|"my-index"
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|MappingMetaData
name|mappingMetaData
init|=
name|getMappingsResponse
operator|.
name|mappings
argument_list|()
operator|.
name|get
argument_list|(
literal|"my-index"
argument_list|)
operator|.
name|get
argument_list|(
literal|"my-type"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|mappingMetaData
argument_list|,
name|not
argument_list|(
name|nullValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|mappingSource
init|=
name|mappingMetaData
operator|.
name|sourceAsMap
argument_list|()
decl_stmt|;
name|Map
name|aField
init|=
operator|(
operator|(
name|Map
operator|)
name|XContentMapValues
operator|.
name|extractValue
argument_list|(
literal|"properties.a"
argument_list|,
name|mappingSource
argument_list|)
operator|)
decl_stmt|;
name|assertThat
argument_list|(
name|aField
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|aField
operator|.
name|get
argument_list|(
literal|"type"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"token_count"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|aField
operator|.
name|get
argument_list|(
literal|"fields"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|Map
name|bField
init|=
operator|(
operator|(
name|Map
operator|)
name|XContentMapValues
operator|.
name|extractValue
argument_list|(
literal|"properties.a.fields.b"
argument_list|,
name|mappingSource
argument_list|)
operator|)
decl_stmt|;
name|assertThat
argument_list|(
name|bField
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
name|bField
operator|.
name|get
argument_list|(
literal|"type"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"keyword"
argument_list|)
argument_list|)
expr_stmt|;
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"my-index"
argument_list|,
literal|"my-type"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"a"
argument_list|,
literal|"my tokens"
argument_list|)
operator|.
name|setRefreshPolicy
argument_list|(
name|IMMEDIATE
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|SearchResponse
name|countResponse
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"my-index"
argument_list|)
operator|.
name|setSize
argument_list|(
literal|0
argument_list|)
operator|.
name|setQuery
argument_list|(
name|matchQuery
argument_list|(
literal|"a.b"
argument_list|,
literal|"my tokens"
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|countResponse
operator|.
name|getHits
argument_list|()
operator|.
name|getTotalHits
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1L
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testCompletionMultiField
specifier|public
name|void
name|testCompletionMultiField
parameter_list|()
throws|throws
name|Exception
block|{
name|assertAcked
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
name|prepareCreate
argument_list|(
literal|"my-index"
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"my-type"
argument_list|,
name|createMappingSource
argument_list|(
literal|"completion"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|GetMappingsResponse
name|getMappingsResponse
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
name|prepareGetMappings
argument_list|(
literal|"my-index"
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|MappingMetaData
name|mappingMetaData
init|=
name|getMappingsResponse
operator|.
name|mappings
argument_list|()
operator|.
name|get
argument_list|(
literal|"my-index"
argument_list|)
operator|.
name|get
argument_list|(
literal|"my-type"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|mappingMetaData
argument_list|,
name|not
argument_list|(
name|nullValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|mappingSource
init|=
name|mappingMetaData
operator|.
name|sourceAsMap
argument_list|()
decl_stmt|;
name|Map
name|aField
init|=
operator|(
operator|(
name|Map
operator|)
name|XContentMapValues
operator|.
name|extractValue
argument_list|(
literal|"properties.a"
argument_list|,
name|mappingSource
argument_list|)
operator|)
decl_stmt|;
name|assertThat
argument_list|(
name|aField
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|6
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|aField
operator|.
name|get
argument_list|(
literal|"type"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"completion"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|aField
operator|.
name|get
argument_list|(
literal|"fields"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|Map
name|bField
init|=
operator|(
operator|(
name|Map
operator|)
name|XContentMapValues
operator|.
name|extractValue
argument_list|(
literal|"properties.a.fields.b"
argument_list|,
name|mappingSource
argument_list|)
operator|)
decl_stmt|;
name|assertThat
argument_list|(
name|bField
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
name|bField
operator|.
name|get
argument_list|(
literal|"type"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"keyword"
argument_list|)
argument_list|)
expr_stmt|;
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"my-index"
argument_list|,
literal|"my-type"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"a"
argument_list|,
literal|"complete me"
argument_list|)
operator|.
name|setRefreshPolicy
argument_list|(
name|IMMEDIATE
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|SearchResponse
name|countResponse
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"my-index"
argument_list|)
operator|.
name|setSize
argument_list|(
literal|0
argument_list|)
operator|.
name|setQuery
argument_list|(
name|matchQuery
argument_list|(
literal|"a.b"
argument_list|,
literal|"complete me"
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|countResponse
operator|.
name|getHits
argument_list|()
operator|.
name|getTotalHits
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1L
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testIpMultiField
specifier|public
name|void
name|testIpMultiField
parameter_list|()
throws|throws
name|Exception
block|{
name|assertAcked
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
name|prepareCreate
argument_list|(
literal|"my-index"
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"my-type"
argument_list|,
name|createMappingSource
argument_list|(
literal|"ip"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|GetMappingsResponse
name|getMappingsResponse
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
name|prepareGetMappings
argument_list|(
literal|"my-index"
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|MappingMetaData
name|mappingMetaData
init|=
name|getMappingsResponse
operator|.
name|mappings
argument_list|()
operator|.
name|get
argument_list|(
literal|"my-index"
argument_list|)
operator|.
name|get
argument_list|(
literal|"my-type"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|mappingMetaData
argument_list|,
name|not
argument_list|(
name|nullValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|mappingSource
init|=
name|mappingMetaData
operator|.
name|sourceAsMap
argument_list|()
decl_stmt|;
name|Map
name|aField
init|=
operator|(
operator|(
name|Map
operator|)
name|XContentMapValues
operator|.
name|extractValue
argument_list|(
literal|"properties.a"
argument_list|,
name|mappingSource
argument_list|)
operator|)
decl_stmt|;
name|assertThat
argument_list|(
name|aField
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|aField
operator|.
name|get
argument_list|(
literal|"type"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"ip"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|aField
operator|.
name|get
argument_list|(
literal|"fields"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|Map
name|bField
init|=
operator|(
operator|(
name|Map
operator|)
name|XContentMapValues
operator|.
name|extractValue
argument_list|(
literal|"properties.a.fields.b"
argument_list|,
name|mappingSource
argument_list|)
operator|)
decl_stmt|;
name|assertThat
argument_list|(
name|bField
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
name|bField
operator|.
name|get
argument_list|(
literal|"type"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"keyword"
argument_list|)
argument_list|)
expr_stmt|;
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"my-index"
argument_list|,
literal|"my-type"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"a"
argument_list|,
literal|"127.0.0.1"
argument_list|)
operator|.
name|setRefreshPolicy
argument_list|(
name|IMMEDIATE
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|SearchResponse
name|countResponse
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"my-index"
argument_list|)
operator|.
name|setSize
argument_list|(
literal|0
argument_list|)
operator|.
name|setQuery
argument_list|(
name|matchQuery
argument_list|(
literal|"a.b"
argument_list|,
literal|"127.0.0.1"
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|countResponse
operator|.
name|getHits
argument_list|()
operator|.
name|getTotalHits
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1L
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|createMappingSource
specifier|private
name|XContentBuilder
name|createMappingSource
parameter_list|(
name|String
name|fieldType
parameter_list|)
throws|throws
name|IOException
block|{
return|return
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
literal|"my-type"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"properties"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"a"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
name|fieldType
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"fields"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"b"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"keyword"
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
return|;
block|}
DECL|method|createTypeSource
specifier|private
name|XContentBuilder
name|createTypeSource
parameter_list|()
throws|throws
name|IOException
block|{
return|return
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
literal|"my-type"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"properties"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"title"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"text"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"fields"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"not_analyzed"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"keyword"
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
return|;
block|}
DECL|method|createPutMappingSource
specifier|private
name|XContentBuilder
name|createPutMappingSource
parameter_list|()
throws|throws
name|IOException
block|{
return|return
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
literal|"my-type"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"properties"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"title"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"text"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"fields"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"uncased"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"text"
argument_list|)
operator|.
name|field
argument_list|(
literal|"analyzer"
argument_list|,
literal|"whitespace"
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
return|;
block|}
block|}
end_class

end_unit

