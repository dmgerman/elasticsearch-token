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
name|ShapeRelation
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
name|builders
operator|.
name|ShapeBuilders
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
name|QueryBuilders
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
name|fetch
operator|.
name|subphase
operator|.
name|highlight
operator|.
name|HighlightBuilder
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
DECL|class|ExternalValuesMapperIntegrationIT
specifier|public
class|class
name|ExternalValuesMapperIntegrationIT
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
name|ExternalMapperPlugin
operator|.
name|class
argument_list|)
return|;
block|}
DECL|method|testHighlightingOnCustomString
specifier|public
name|void
name|testHighlightingOnCustomString
parameter_list|()
throws|throws
name|Exception
block|{
name|prepareCreate
argument_list|(
literal|"test-idx"
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"type"
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
literal|"type"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"properties"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"field"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
name|FakeStringFieldMapper
operator|.
name|CONTENT_TYPE
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
name|get
argument_list|()
expr_stmt|;
name|index
argument_list|(
literal|"test-idx"
argument_list|,
literal|"type"
argument_list|,
literal|"1"
argument_list|,
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"field"
argument_list|,
literal|"Every day is exactly the same"
argument_list|)
operator|.
name|endObject
argument_list|()
argument_list|)
expr_stmt|;
name|refresh
argument_list|()
expr_stmt|;
name|SearchResponse
name|response
decl_stmt|;
comment|// test if the highlighting is excluded when we use wildcards
name|response
operator|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"test-idx"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
name|matchQuery
argument_list|(
literal|"field"
argument_list|,
literal|"exactly the same"
argument_list|)
argument_list|)
operator|.
name|highlighter
argument_list|(
operator|new
name|HighlightBuilder
argument_list|()
operator|.
name|field
argument_list|(
literal|"*"
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|assertSearchResponse
argument_list|(
name|response
argument_list|)
expr_stmt|;
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
literal|1L
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getHits
argument_list|()
operator|.
name|getAt
argument_list|(
literal|0
argument_list|)
operator|.
name|getHighlightFields
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
comment|// make sure it is not excluded when we explicitly provide the fieldname
name|response
operator|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"test-idx"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
name|matchQuery
argument_list|(
literal|"field"
argument_list|,
literal|"exactly the same"
argument_list|)
argument_list|)
operator|.
name|highlighter
argument_list|(
operator|new
name|HighlightBuilder
argument_list|()
operator|.
name|field
argument_list|(
literal|"field"
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|assertSearchResponse
argument_list|(
name|response
argument_list|)
expr_stmt|;
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
literal|1L
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getHits
argument_list|()
operator|.
name|getAt
argument_list|(
literal|0
argument_list|)
operator|.
name|getHighlightFields
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
name|getHits
argument_list|()
operator|.
name|getAt
argument_list|(
literal|0
argument_list|)
operator|.
name|getHighlightFields
argument_list|()
operator|.
name|get
argument_list|(
literal|"field"
argument_list|)
operator|.
name|fragments
argument_list|()
index|[
literal|0
index|]
operator|.
name|string
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"Every day is "
operator|+
literal|"<em>exactly</em><em>the</em><em>same</em>"
argument_list|)
argument_list|)
expr_stmt|;
comment|// make sure it is not excluded when we explicitly provide the fieldname and a wildcard
name|response
operator|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"test-idx"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
name|matchQuery
argument_list|(
literal|"field"
argument_list|,
literal|"exactly the same"
argument_list|)
argument_list|)
operator|.
name|highlighter
argument_list|(
operator|new
name|HighlightBuilder
argument_list|()
operator|.
name|field
argument_list|(
literal|"*"
argument_list|)
operator|.
name|field
argument_list|(
literal|"field"
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|assertSearchResponse
argument_list|(
name|response
argument_list|)
expr_stmt|;
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
literal|1L
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getHits
argument_list|()
operator|.
name|getAt
argument_list|(
literal|0
argument_list|)
operator|.
name|getHighlightFields
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
name|getHits
argument_list|()
operator|.
name|getAt
argument_list|(
literal|0
argument_list|)
operator|.
name|getHighlightFields
argument_list|()
operator|.
name|get
argument_list|(
literal|"field"
argument_list|)
operator|.
name|fragments
argument_list|()
index|[
literal|0
index|]
operator|.
name|string
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"Every day is "
operator|+
literal|"<em>exactly</em><em>the</em><em>same</em>"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testExternalValues
specifier|public
name|void
name|testExternalValues
parameter_list|()
throws|throws
name|Exception
block|{
name|prepareCreate
argument_list|(
literal|"test-idx"
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"type"
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
literal|"type"
argument_list|)
operator|.
name|startObject
argument_list|(
name|ExternalMetadataMapper
operator|.
name|CONTENT_TYPE
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
literal|"field"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
name|ExternalMapperPlugin
operator|.
name|EXTERNAL
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
name|get
argument_list|()
expr_stmt|;
name|index
argument_list|(
literal|"test-idx"
argument_list|,
literal|"type"
argument_list|,
literal|"1"
argument_list|,
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"field"
argument_list|,
literal|"1234"
argument_list|)
operator|.
name|endObject
argument_list|()
argument_list|)
expr_stmt|;
name|refresh
argument_list|()
expr_stmt|;
name|SearchResponse
name|response
decl_stmt|;
name|response
operator|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"test-idx"
argument_list|)
operator|.
name|setPostFilter
argument_list|(
name|QueryBuilders
operator|.
name|termQuery
argument_list|(
literal|"field.bool"
argument_list|,
literal|"true"
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
operator|(
name|long
operator|)
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|response
operator|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"test-idx"
argument_list|)
operator|.
name|setPostFilter
argument_list|(
name|QueryBuilders
operator|.
name|geoDistanceQuery
argument_list|(
literal|"field.point"
argument_list|)
operator|.
name|point
argument_list|(
literal|42.0
argument_list|,
literal|51.0
argument_list|)
operator|.
name|distance
argument_list|(
literal|"1km"
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
operator|(
name|long
operator|)
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|response
operator|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"test-idx"
argument_list|)
operator|.
name|setPostFilter
argument_list|(
name|QueryBuilders
operator|.
name|geoShapeQuery
argument_list|(
literal|"field.shape"
argument_list|,
name|ShapeBuilders
operator|.
name|newPoint
argument_list|(
operator|-
literal|100
argument_list|,
literal|45
argument_list|)
argument_list|)
operator|.
name|relation
argument_list|(
name|ShapeRelation
operator|.
name|WITHIN
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
operator|(
name|long
operator|)
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|response
operator|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"test-idx"
argument_list|)
operator|.
name|setPostFilter
argument_list|(
name|QueryBuilders
operator|.
name|termQuery
argument_list|(
literal|"field.field"
argument_list|,
literal|"foo"
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
operator|(
name|long
operator|)
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testExternalValuesWithMultifield
specifier|public
name|void
name|testExternalValuesWithMultifield
parameter_list|()
throws|throws
name|Exception
block|{
name|prepareCreate
argument_list|(
literal|"test-idx"
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"doc"
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
literal|"doc"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"properties"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"f"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
name|ExternalMapperPlugin
operator|.
name|EXTERNAL_UPPER
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"fields"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"g"
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
literal|"store"
argument_list|,
literal|true
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"fields"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"raw"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"keyword"
argument_list|)
operator|.
name|field
argument_list|(
literal|"store"
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
name|get
argument_list|()
expr_stmt|;
name|index
argument_list|(
literal|"test-idx"
argument_list|,
literal|"doc"
argument_list|,
literal|"1"
argument_list|,
literal|"f"
argument_list|,
literal|"This is my text"
argument_list|)
expr_stmt|;
name|refresh
argument_list|()
expr_stmt|;
name|SearchResponse
name|response
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"test-idx"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
name|termQuery
argument_list|(
literal|"f.g.raw"
argument_list|,
literal|"FOO BAR"
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
operator|(
name|long
operator|)
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

