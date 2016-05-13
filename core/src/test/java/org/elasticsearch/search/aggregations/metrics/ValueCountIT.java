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
name|apache
operator|.
name|lucene
operator|.
name|index
operator|.
name|LeafReaderContext
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
name|Scorer
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
name|CompiledScript
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
name|ExecutableScript
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
name|LeafSearchScript
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
name|ScriptEngineRegistry
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
name|ScriptEngineService
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
name|ScriptMode
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
name|ScriptModule
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
name|SearchScript
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
name|valuecount
operator|.
name|ValueCount
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
name|lookup
operator|.
name|LeafSearchLookup
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
name|lookup
operator|.
name|SearchLookup
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
name|List
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
name|count
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
annotation|@
name|ESIntegTestCase
operator|.
name|SuiteScopeTestCase
DECL|class|ValueCountIT
specifier|public
class|class
name|ValueCountIT
extends|extends
name|ESIntegTestCase
block|{
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
literal|"idx"
argument_list|)
expr_stmt|;
name|createIndex
argument_list|(
literal|"idx_unmapped"
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
literal|10
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
literal|"idx"
argument_list|,
literal|"type"
argument_list|,
literal|""
operator|+
name|i
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
literal|"value"
argument_list|,
name|i
operator|+
literal|1
argument_list|)
operator|.
name|startArray
argument_list|(
literal|"values"
argument_list|)
operator|.
name|value
argument_list|(
name|i
operator|+
literal|2
argument_list|)
operator|.
name|value
argument_list|(
name|i
operator|+
literal|3
argument_list|)
operator|.
name|endArray
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
name|prepareFlush
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
name|prepareRefresh
argument_list|()
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|ensureSearchable
argument_list|()
expr_stmt|;
block|}
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
name|singletonList
argument_list|(
name|FieldValueScriptPlugin
operator|.
name|class
argument_list|)
return|;
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
name|count
argument_list|(
literal|"count"
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
name|ValueCount
name|valueCount
init|=
name|searchResponse
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"count"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|valueCount
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|valueCount
operator|.
name|getName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"count"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|valueCount
operator|.
name|getValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0L
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
name|count
argument_list|(
literal|"count"
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
name|ValueCount
name|valueCount
init|=
name|searchResponse
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"count"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|valueCount
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|valueCount
operator|.
name|getName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"count"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|valueCount
operator|.
name|getValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|10L
argument_list|)
argument_list|)
expr_stmt|;
block|}
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
name|count
argument_list|(
literal|"count"
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
name|ValueCount
name|valueCount
init|=
name|global
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"count"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|valueCount
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|valueCount
operator|.
name|getName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"count"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|valueCount
operator|.
name|getValue
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
operator|(
name|ValueCount
operator|)
name|global
operator|.
name|getProperty
argument_list|(
literal|"count"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|valueCount
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
literal|"count.value"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|10d
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
name|double
operator|)
name|valueCount
operator|.
name|getProperty
argument_list|(
literal|"value"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|10d
argument_list|)
argument_list|)
expr_stmt|;
block|}
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
name|count
argument_list|(
literal|"count"
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
name|ValueCount
name|valueCount
init|=
name|searchResponse
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"count"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|valueCount
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|valueCount
operator|.
name|getName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"count"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|valueCount
operator|.
name|getValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|10L
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
name|count
argument_list|(
literal|"count"
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
name|ValueCount
name|valueCount
init|=
name|searchResponse
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"count"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|valueCount
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|valueCount
operator|.
name|getName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"count"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|valueCount
operator|.
name|getValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|20L
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testSingleValuedScript
specifier|public
name|void
name|testSingleValuedScript
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
name|count
argument_list|(
literal|"count"
argument_list|)
operator|.
name|script
argument_list|(
operator|new
name|Script
argument_list|(
literal|"value"
argument_list|,
name|ScriptType
operator|.
name|INLINE
argument_list|,
name|FieldValueScriptEngine
operator|.
name|NAME
argument_list|,
literal|null
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
name|ValueCount
name|valueCount
init|=
name|searchResponse
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"count"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|valueCount
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|valueCount
operator|.
name|getName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"count"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|valueCount
operator|.
name|getValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|10L
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testMultiValuedScript
specifier|public
name|void
name|testMultiValuedScript
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
name|count
argument_list|(
literal|"count"
argument_list|)
operator|.
name|script
argument_list|(
operator|new
name|Script
argument_list|(
literal|"values"
argument_list|,
name|ScriptType
operator|.
name|INLINE
argument_list|,
name|FieldValueScriptEngine
operator|.
name|NAME
argument_list|,
literal|null
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
name|ValueCount
name|valueCount
init|=
name|searchResponse
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"count"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|valueCount
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|valueCount
operator|.
name|getName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"count"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|valueCount
operator|.
name|getValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|20L
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testSingleValuedScriptWithParams
specifier|public
name|void
name|testSingleValuedScriptWithParams
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
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"s"
argument_list|,
literal|"value"
argument_list|)
decl_stmt|;
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
name|count
argument_list|(
literal|"count"
argument_list|)
operator|.
name|script
argument_list|(
operator|new
name|Script
argument_list|(
literal|""
argument_list|,
name|ScriptType
operator|.
name|INLINE
argument_list|,
name|FieldValueScriptEngine
operator|.
name|NAME
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
name|ValueCount
name|valueCount
init|=
name|searchResponse
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"count"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|valueCount
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|valueCount
operator|.
name|getName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"count"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|valueCount
operator|.
name|getValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|10L
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testMultiValuedScriptWithParams
specifier|public
name|void
name|testMultiValuedScriptWithParams
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
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"s"
argument_list|,
literal|"values"
argument_list|)
decl_stmt|;
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
name|count
argument_list|(
literal|"count"
argument_list|)
operator|.
name|script
argument_list|(
operator|new
name|Script
argument_list|(
literal|""
argument_list|,
name|ScriptType
operator|.
name|INLINE
argument_list|,
name|FieldValueScriptEngine
operator|.
name|NAME
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
name|ValueCount
name|valueCount
init|=
name|searchResponse
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"count"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|valueCount
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|valueCount
operator|.
name|getName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"count"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|valueCount
operator|.
name|getValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|20L
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**      * Mock plugin for the {@link FieldValueScriptEngine}      */
DECL|class|FieldValueScriptPlugin
specifier|public
specifier|static
class|class
name|FieldValueScriptPlugin
extends|extends
name|Plugin
block|{
annotation|@
name|Override
DECL|method|name
specifier|public
name|String
name|name
parameter_list|()
block|{
return|return
name|FieldValueScriptEngine
operator|.
name|NAME
return|;
block|}
annotation|@
name|Override
DECL|method|description
specifier|public
name|String
name|description
parameter_list|()
block|{
return|return
literal|"Mock script engine for "
operator|+
name|ValueCountIT
operator|.
name|class
return|;
block|}
DECL|method|onModule
specifier|public
name|void
name|onModule
parameter_list|(
name|ScriptModule
name|module
parameter_list|)
block|{
name|module
operator|.
name|addScriptEngine
argument_list|(
operator|new
name|ScriptEngineRegistry
operator|.
name|ScriptEngineRegistration
argument_list|(
name|FieldValueScriptEngine
operator|.
name|class
argument_list|,
name|FieldValueScriptEngine
operator|.
name|NAME
argument_list|,
name|ScriptMode
operator|.
name|ON
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      * This mock script returns the field value. If the parameter map contains a parameter "s", the corresponding is used as field name.      */
DECL|class|FieldValueScriptEngine
specifier|public
specifier|static
class|class
name|FieldValueScriptEngine
implements|implements
name|ScriptEngineService
block|{
DECL|field|NAME
specifier|public
specifier|static
specifier|final
name|String
name|NAME
init|=
literal|"field_value"
decl_stmt|;
annotation|@
name|Override
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{         }
annotation|@
name|Override
DECL|method|getType
specifier|public
name|String
name|getType
parameter_list|()
block|{
return|return
name|NAME
return|;
block|}
annotation|@
name|Override
DECL|method|getExtension
specifier|public
name|String
name|getExtension
parameter_list|()
block|{
return|return
name|NAME
return|;
block|}
annotation|@
name|Override
DECL|method|compile
specifier|public
name|Object
name|compile
parameter_list|(
name|String
name|scriptName
parameter_list|,
name|String
name|scriptSource
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|params
parameter_list|)
block|{
return|return
name|scriptSource
return|;
block|}
annotation|@
name|Override
DECL|method|executable
specifier|public
name|ExecutableScript
name|executable
parameter_list|(
name|CompiledScript
name|compiledScript
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|params
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
DECL|method|search
specifier|public
name|SearchScript
name|search
parameter_list|(
name|CompiledScript
name|compiledScript
parameter_list|,
name|SearchLookup
name|lookup
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|vars
parameter_list|)
block|{
specifier|final
name|String
name|fieldNameParam
decl_stmt|;
if|if
condition|(
name|vars
operator|==
literal|null
operator|||
name|vars
operator|.
name|containsKey
argument_list|(
literal|"s"
argument_list|)
operator|==
literal|false
condition|)
block|{
name|fieldNameParam
operator|=
literal|null
expr_stmt|;
block|}
else|else
block|{
name|fieldNameParam
operator|=
operator|(
name|String
operator|)
name|vars
operator|.
name|get
argument_list|(
literal|"s"
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|SearchScript
argument_list|()
block|{
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|vars
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|(
literal|2
argument_list|)
decl_stmt|;
annotation|@
name|Override
specifier|public
name|LeafSearchScript
name|getLeafSearchScript
parameter_list|(
name|LeafReaderContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|LeafSearchLookup
name|leafLookup
init|=
name|lookup
operator|.
name|getLeafSearchLookup
argument_list|(
name|context
argument_list|)
decl_stmt|;
return|return
operator|new
name|LeafSearchScript
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Object
name|unwrap
parameter_list|(
name|Object
name|value
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setNextVar
parameter_list|(
name|String
name|name
parameter_list|,
name|Object
name|value
parameter_list|)
block|{
name|vars
operator|.
name|put
argument_list|(
name|name
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|run
parameter_list|()
block|{
name|String
name|fieldName
init|=
operator|(
name|fieldNameParam
operator|!=
literal|null
operator|)
condition|?
name|fieldNameParam
else|:
operator|(
name|String
operator|)
name|compiledScript
operator|.
name|compiled
argument_list|()
decl_stmt|;
return|return
name|leafLookup
operator|.
name|doc
argument_list|()
operator|.
name|get
argument_list|(
name|fieldName
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setScorer
parameter_list|(
name|Scorer
name|scorer
parameter_list|)
block|{                         }
annotation|@
name|Override
specifier|public
name|void
name|setSource
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|source
parameter_list|)
block|{                         }
annotation|@
name|Override
specifier|public
name|void
name|setDocument
parameter_list|(
name|int
name|doc
parameter_list|)
block|{
if|if
condition|(
name|leafLookup
operator|!=
literal|null
condition|)
block|{
name|leafLookup
operator|.
name|setDocument
argument_list|(
name|doc
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|long
name|runAsLong
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
specifier|public
name|float
name|runAsFloat
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
specifier|public
name|double
name|runAsDouble
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
block|}
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|needsScores
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
block|}
return|;
block|}
annotation|@
name|Override
DECL|method|scriptRemoved
specifier|public
name|void
name|scriptRemoved
parameter_list|(
name|CompiledScript
name|script
parameter_list|)
block|{         }
block|}
block|}
end_class

end_unit

