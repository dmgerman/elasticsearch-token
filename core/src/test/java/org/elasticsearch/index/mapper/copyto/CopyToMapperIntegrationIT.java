begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.mapper.copyto
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
operator|.
name|copyto
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
name|Aggregator
operator|.
name|SubAggCollectionMode
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
name|terms
operator|.
name|Terms
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
name|hamcrest
operator|.
name|Matchers
operator|.
name|equalTo
import|;
end_import

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|CopyToMapperIntegrationIT
specifier|public
class|class
name|CopyToMapperIntegrationIT
extends|extends
name|ESIntegTestCase
block|{
DECL|method|testDynamicTemplateCopyTo
specifier|public
name|void
name|testDynamicTemplateCopyTo
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
literal|"test-idx"
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"doc"
argument_list|,
name|createDynamicTemplateMapping
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|int
name|recordCount
init|=
name|between
argument_list|(
literal|1
argument_list|,
literal|200
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
name|recordCount
operator|*
literal|2
condition|;
name|i
operator|++
control|)
block|{
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test-idx"
argument_list|,
literal|"doc"
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|i
argument_list|)
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"test_field"
argument_list|,
literal|"test "
operator|+
name|i
argument_list|,
literal|"even"
argument_list|,
name|i
operator|%
literal|2
operator|==
literal|0
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
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
argument_list|(
literal|"test-idx"
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|SubAggCollectionMode
name|aggCollectionMode
init|=
name|randomFrom
argument_list|(
name|SubAggCollectionMode
operator|.
name|values
argument_list|()
argument_list|)
decl_stmt|;
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
literal|"even"
argument_list|,
literal|true
argument_list|)
argument_list|)
operator|.
name|addAggregation
argument_list|(
name|AggregationBuilders
operator|.
name|terms
argument_list|(
literal|"test"
argument_list|)
operator|.
name|field
argument_list|(
literal|"test_field"
argument_list|)
operator|.
name|size
argument_list|(
name|recordCount
operator|*
literal|2
argument_list|)
operator|.
name|collectMode
argument_list|(
name|aggCollectionMode
argument_list|)
argument_list|)
operator|.
name|addAggregation
argument_list|(
name|AggregationBuilders
operator|.
name|terms
argument_list|(
literal|"test_raw"
argument_list|)
operator|.
name|field
argument_list|(
literal|"test_field_raw"
argument_list|)
operator|.
name|size
argument_list|(
name|recordCount
operator|*
literal|2
argument_list|)
operator|.
name|collectMode
argument_list|(
name|aggCollectionMode
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
name|totalHits
argument_list|()
argument_list|,
name|equalTo
argument_list|(
operator|(
name|long
operator|)
name|recordCount
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|Terms
operator|)
name|response
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"test"
argument_list|)
operator|)
operator|.
name|getBuckets
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|recordCount
operator|+
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|Terms
operator|)
name|response
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"test_raw"
argument_list|)
operator|)
operator|.
name|getBuckets
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|recordCount
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testDynamicObjectCopyTo
specifier|public
name|void
name|testDynamicObjectCopyTo
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|mapping
init|=
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
literal|"foo"
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
literal|"copy_to"
argument_list|,
literal|"root.top.child"
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
literal|"test-idx"
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"doc"
argument_list|,
name|mapping
argument_list|)
argument_list|)
expr_stmt|;
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test-idx"
argument_list|,
literal|"doc"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"foo"
argument_list|,
literal|"bar"
argument_list|)
operator|.
name|get
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
argument_list|(
literal|"test-idx"
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
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
literal|"root.top.child"
argument_list|,
literal|"bar"
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getHits
argument_list|()
operator|.
name|totalHits
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1L
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|createDynamicTemplateMapping
specifier|private
name|XContentBuilder
name|createDynamicTemplateMapping
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
literal|"doc"
argument_list|)
operator|.
name|startArray
argument_list|(
literal|"dynamic_templates"
argument_list|)
operator|.
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"template_raw"
argument_list|)
operator|.
name|field
argument_list|(
literal|"match"
argument_list|,
literal|"*_raw"
argument_list|)
operator|.
name|field
argument_list|(
literal|"match_mapping_type"
argument_list|,
literal|"string"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"mapping"
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
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"template_all"
argument_list|)
operator|.
name|field
argument_list|(
literal|"match"
argument_list|,
literal|"*"
argument_list|)
operator|.
name|field
argument_list|(
literal|"match_mapping_type"
argument_list|,
literal|"string"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"mapping"
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
literal|"fielddata"
argument_list|,
literal|true
argument_list|)
operator|.
name|field
argument_list|(
literal|"copy_to"
argument_list|,
literal|"{name}_raw"
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
name|endArray
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

