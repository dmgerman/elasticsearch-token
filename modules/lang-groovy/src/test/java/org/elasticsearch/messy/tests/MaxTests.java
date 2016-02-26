begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.messy.tests
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|messy
operator|.
name|tests
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
name|script
operator|.
name|Script
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|ScriptService
operator|.
name|ScriptType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|groovy
operator|.
name|GroovyPlugin
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
name|bucket
operator|.
name|histogram
operator|.
name|Histogram
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
name|AbstractNumericTestCase
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
name|max
operator|.
name|Max
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
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
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
name|global
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
name|histogram
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
name|max
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
name|assertHitCount
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

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|MaxTests
specifier|public
class|class
name|MaxTests
extends|extends
name|AbstractNumericTestCase
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
name|Collections
operator|.
name|singleton
argument_list|(
name|GroovyPlugin
operator|.
name|class
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|testEmptyAggregation
specifier|public
name|void
name|testEmptyAggregation
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
literal|"empty_bucket_idx"
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
name|histogram
argument_list|(
literal|"histo"
argument_list|)
operator|.
name|field
argument_list|(
literal|"value"
argument_list|)
operator|.
name|interval
argument_list|(
literal|1L
argument_list|)
operator|.
name|minDocCount
argument_list|(
literal|0
argument_list|)
operator|.
name|subAggregation
argument_list|(
name|max
argument_list|(
literal|"max"
argument_list|)
operator|.
name|field
argument_list|(
literal|"value"
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
name|getHits
argument_list|()
operator|.
name|getTotalHits
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|2L
argument_list|)
argument_list|)
expr_stmt|;
name|Histogram
name|histo
init|=
name|searchResponse
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"histo"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|histo
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|Histogram
operator|.
name|Bucket
name|bucket
init|=
name|histo
operator|.
name|getBuckets
argument_list|()
operator|.
name|get
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|bucket
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|Max
name|max
init|=
name|bucket
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"max"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|max
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|max
operator|.
name|getName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"max"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|max
operator|.
name|getValue
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
block|}
annotation|@
name|Override
DECL|method|testUnmapped
specifier|public
name|void
name|testUnmapped
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
literal|"idx_unmapped"
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
name|max
argument_list|(
literal|"max"
argument_list|)
operator|.
name|field
argument_list|(
literal|"value"
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
name|Max
name|max
init|=
name|searchResponse
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"max"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|max
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|max
operator|.
name|getName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"max"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|max
operator|.
name|getValue
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
block|}
annotation|@
name|Override
DECL|method|testSingleValuedField
specifier|public
name|void
name|testSingleValuedField
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
literal|"idx"
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
name|max
argument_list|(
literal|"max"
argument_list|)
operator|.
name|field
argument_list|(
literal|"value"
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|assertHitCount
argument_list|(
name|searchResponse
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|Max
name|max
init|=
name|searchResponse
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"max"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|max
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|max
operator|.
name|getName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"max"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|max
operator|.
name|getValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|10.0
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testSingleValuedFieldWithFormatter
specifier|public
name|void
name|testSingleValuedFieldWithFormatter
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
literal|"idx"
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
name|max
argument_list|(
literal|"max"
argument_list|)
operator|.
name|format
argument_list|(
literal|"0000.0"
argument_list|)
operator|.
name|field
argument_list|(
literal|"value"
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|assertHitCount
argument_list|(
name|searchResponse
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|Max
name|max
init|=
name|searchResponse
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"max"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|max
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|max
operator|.
name|getName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"max"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|max
operator|.
name|getValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|10.0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|max
operator|.
name|getValueAsString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"0010.0"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|testSingleValuedFieldGetProperty
specifier|public
name|void
name|testSingleValuedFieldGetProperty
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
literal|"idx"
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
name|max
argument_list|(
literal|"max"
argument_list|)
operator|.
name|field
argument_list|(
literal|"value"
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
name|assertHitCount
argument_list|(
name|searchResponse
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|Global
name|global
init|=
name|searchResponse
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
literal|10L
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
name|Max
name|max
init|=
name|global
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"max"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|max
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|max
operator|.
name|getName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"max"
argument_list|)
argument_list|)
expr_stmt|;
name|double
name|expectedMaxValue
init|=
literal|10.0
decl_stmt|;
name|assertThat
argument_list|(
name|max
operator|.
name|getValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|expectedMaxValue
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
name|Max
operator|)
name|global
operator|.
name|getProperty
argument_list|(
literal|"max"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|max
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
name|double
operator|)
name|global
operator|.
name|getProperty
argument_list|(
literal|"max.value"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|expectedMaxValue
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
name|double
operator|)
name|max
operator|.
name|getProperty
argument_list|(
literal|"value"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|expectedMaxValue
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|testSingleValuedFieldPartiallyUnmapped
specifier|public
name|void
name|testSingleValuedFieldPartiallyUnmapped
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
literal|"idx"
argument_list|,
literal|"idx_unmapped"
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
name|max
argument_list|(
literal|"max"
argument_list|)
operator|.
name|field
argument_list|(
literal|"value"
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|assertHitCount
argument_list|(
name|searchResponse
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|Max
name|max
init|=
name|searchResponse
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"max"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|max
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|max
operator|.
name|getName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"max"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|max
operator|.
name|getValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|10.0
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|testSingleValuedFieldWithValueScript
specifier|public
name|void
name|testSingleValuedFieldWithValueScript
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
literal|"idx"
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
name|max
argument_list|(
literal|"max"
argument_list|)
operator|.
name|field
argument_list|(
literal|"value"
argument_list|)
operator|.
name|script
argument_list|(
operator|new
name|Script
argument_list|(
literal|"_value + 1"
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
name|assertHitCount
argument_list|(
name|searchResponse
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|Max
name|max
init|=
name|searchResponse
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"max"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|max
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|max
operator|.
name|getName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"max"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|max
operator|.
name|getValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|11.0
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|testSingleValuedFieldWithValueScriptWithParams
specifier|public
name|void
name|testSingleValuedFieldWithValueScriptWithParams
parameter_list|()
throws|throws
name|Exception
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|params
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|params
operator|.
name|put
argument_list|(
literal|"inc"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|SearchResponse
name|searchResponse
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"idx"
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
name|max
argument_list|(
literal|"max"
argument_list|)
operator|.
name|field
argument_list|(
literal|"value"
argument_list|)
operator|.
name|script
argument_list|(
operator|new
name|Script
argument_list|(
literal|"_value + inc"
argument_list|,
name|ScriptType
operator|.
name|INLINE
argument_list|,
literal|null
argument_list|,
name|params
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
name|assertHitCount
argument_list|(
name|searchResponse
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|Max
name|max
init|=
name|searchResponse
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"max"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|max
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|max
operator|.
name|getName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"max"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|max
operator|.
name|getValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|11.0
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
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
literal|"idx"
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
name|max
argument_list|(
literal|"max"
argument_list|)
operator|.
name|field
argument_list|(
literal|"values"
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|assertHitCount
argument_list|(
name|searchResponse
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|Max
name|max
init|=
name|searchResponse
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"max"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|max
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|max
operator|.
name|getName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"max"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|max
operator|.
name|getValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|12.0
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|testMultiValuedFieldWithValueScript
specifier|public
name|void
name|testMultiValuedFieldWithValueScript
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
literal|"idx"
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
name|max
argument_list|(
literal|"max"
argument_list|)
operator|.
name|field
argument_list|(
literal|"values"
argument_list|)
operator|.
name|script
argument_list|(
operator|new
name|Script
argument_list|(
literal|"_value + 1"
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
name|assertHitCount
argument_list|(
name|searchResponse
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|Max
name|max
init|=
name|searchResponse
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"max"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|max
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|max
operator|.
name|getName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"max"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|max
operator|.
name|getValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|13.0
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|testMultiValuedFieldWithValueScriptWithParams
specifier|public
name|void
name|testMultiValuedFieldWithValueScriptWithParams
parameter_list|()
throws|throws
name|Exception
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|params
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|params
operator|.
name|put
argument_list|(
literal|"inc"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|SearchResponse
name|searchResponse
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"idx"
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
name|max
argument_list|(
literal|"max"
argument_list|)
operator|.
name|field
argument_list|(
literal|"values"
argument_list|)
operator|.
name|script
argument_list|(
operator|new
name|Script
argument_list|(
literal|"_value + inc"
argument_list|,
name|ScriptType
operator|.
name|INLINE
argument_list|,
literal|null
argument_list|,
name|params
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
name|assertHitCount
argument_list|(
name|searchResponse
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|Max
name|max
init|=
name|searchResponse
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"max"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|max
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|max
operator|.
name|getName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"max"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|max
operator|.
name|getValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|13.0
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|testScriptSingleValued
specifier|public
name|void
name|testScriptSingleValued
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
literal|"idx"
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
name|max
argument_list|(
literal|"max"
argument_list|)
operator|.
name|script
argument_list|(
operator|new
name|Script
argument_list|(
literal|"doc['value'].value"
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
name|assertHitCount
argument_list|(
name|searchResponse
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|Max
name|max
init|=
name|searchResponse
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"max"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|max
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|max
operator|.
name|getName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"max"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|max
operator|.
name|getValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|10.0
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|testScriptSingleValuedWithParams
specifier|public
name|void
name|testScriptSingleValuedWithParams
parameter_list|()
throws|throws
name|Exception
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|params
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|params
operator|.
name|put
argument_list|(
literal|"inc"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|SearchResponse
name|searchResponse
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"idx"
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
name|max
argument_list|(
literal|"max"
argument_list|)
operator|.
name|script
argument_list|(
operator|new
name|Script
argument_list|(
literal|"doc['value'].value + inc"
argument_list|,
name|ScriptType
operator|.
name|INLINE
argument_list|,
literal|null
argument_list|,
name|params
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
name|assertHitCount
argument_list|(
name|searchResponse
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|Max
name|max
init|=
name|searchResponse
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"max"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|max
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|max
operator|.
name|getName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"max"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|max
operator|.
name|getValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|11.0
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|testScriptMultiValued
specifier|public
name|void
name|testScriptMultiValued
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
literal|"idx"
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
name|max
argument_list|(
literal|"max"
argument_list|)
operator|.
name|script
argument_list|(
operator|new
name|Script
argument_list|(
literal|"doc['values'].values"
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
name|assertHitCount
argument_list|(
name|searchResponse
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|Max
name|max
init|=
name|searchResponse
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"max"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|max
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|max
operator|.
name|getName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"max"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|max
operator|.
name|getValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|12.0
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|testScriptMultiValuedWithParams
specifier|public
name|void
name|testScriptMultiValuedWithParams
parameter_list|()
throws|throws
name|Exception
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|params
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|params
operator|.
name|put
argument_list|(
literal|"inc"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|SearchResponse
name|searchResponse
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"idx"
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
name|max
argument_list|(
literal|"max"
argument_list|)
operator|.
name|script
argument_list|(
operator|new
name|Script
argument_list|(
literal|"[ doc['value'].value, doc['value'].value + inc ]"
argument_list|,
name|ScriptType
operator|.
name|INLINE
argument_list|,
literal|null
argument_list|,
name|params
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
name|assertHitCount
argument_list|(
name|searchResponse
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|Max
name|max
init|=
name|searchResponse
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"max"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|max
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|max
operator|.
name|getName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"max"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|max
operator|.
name|getValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|11.0
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

