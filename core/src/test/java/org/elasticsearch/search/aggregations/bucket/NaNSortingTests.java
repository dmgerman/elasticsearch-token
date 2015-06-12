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
name|util
operator|.
name|Comparators
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
name|search
operator|.
name|aggregations
operator|.
name|Aggregation
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
name|search
operator|.
name|aggregations
operator|.
name|metrics
operator|.
name|MetricsAggregationBuilder
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
name|avg
operator|.
name|Avg
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
name|stats
operator|.
name|extended
operator|.
name|ExtendedStats
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
name|junit
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
name|avg
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
name|extendedStats
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
name|terms
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
name|core
operator|.
name|IsNull
operator|.
name|notNullValue
import|;
end_import

begin_class
annotation|@
name|ElasticsearchIntegrationTest
operator|.
name|SuiteScopeTest
DECL|class|NaNSortingTests
specifier|public
class|class
name|NaNSortingTests
extends|extends
name|ElasticsearchIntegrationTest
block|{
DECL|enum|SubAggregation
specifier|private
enum|enum
name|SubAggregation
block|{
DECL|enum constant|AVG
name|AVG
argument_list|(
literal|"avg"
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|MetricsAggregationBuilder
argument_list|<
name|?
argument_list|>
name|builder
parameter_list|()
block|{
return|return
name|avg
argument_list|(
name|name
argument_list|)
operator|.
name|field
argument_list|(
literal|"numeric_field"
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|double
name|getValue
parameter_list|(
name|Aggregation
name|aggregation
parameter_list|)
block|{
return|return
operator|(
operator|(
name|Avg
operator|)
name|aggregation
operator|)
operator|.
name|getValue
argument_list|()
return|;
block|}
block|}
block|,
DECL|enum constant|VARIANCE
name|VARIANCE
argument_list|(
literal|"variance"
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|MetricsAggregationBuilder
argument_list|<
name|?
argument_list|>
name|builder
parameter_list|()
block|{
return|return
name|extendedStats
argument_list|(
name|name
argument_list|)
operator|.
name|field
argument_list|(
literal|"numeric_field"
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|sortKey
parameter_list|()
block|{
return|return
name|name
operator|+
literal|".variance"
return|;
block|}
annotation|@
name|Override
specifier|public
name|double
name|getValue
parameter_list|(
name|Aggregation
name|aggregation
parameter_list|)
block|{
return|return
operator|(
operator|(
name|ExtendedStats
operator|)
name|aggregation
operator|)
operator|.
name|getVariance
argument_list|()
return|;
block|}
block|}
block|,
DECL|enum constant|STD_DEVIATION
name|STD_DEVIATION
argument_list|(
literal|"std_deviation"
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|MetricsAggregationBuilder
argument_list|<
name|?
argument_list|>
name|builder
parameter_list|()
block|{
return|return
name|extendedStats
argument_list|(
name|name
argument_list|)
operator|.
name|field
argument_list|(
literal|"numeric_field"
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|sortKey
parameter_list|()
block|{
return|return
name|name
operator|+
literal|".std_deviation"
return|;
block|}
annotation|@
name|Override
specifier|public
name|double
name|getValue
parameter_list|(
name|Aggregation
name|aggregation
parameter_list|)
block|{
return|return
operator|(
operator|(
name|ExtendedStats
operator|)
name|aggregation
operator|)
operator|.
name|getStdDeviation
argument_list|()
return|;
block|}
block|}
block|;
DECL|method|SubAggregation
name|SubAggregation
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
block|}
DECL|field|name
specifier|public
name|String
name|name
decl_stmt|;
DECL|method|builder
specifier|public
specifier|abstract
name|MetricsAggregationBuilder
argument_list|<
name|?
argument_list|>
name|builder
parameter_list|()
function_decl|;
DECL|method|sortKey
specifier|public
name|String
name|sortKey
parameter_list|()
block|{
return|return
name|name
return|;
block|}
DECL|method|getValue
specifier|public
specifier|abstract
name|double
name|getValue
parameter_list|(
name|Aggregation
name|aggregation
parameter_list|)
function_decl|;
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
literal|"idx"
argument_list|)
expr_stmt|;
specifier|final
name|int
name|numDocs
init|=
name|randomIntBetween
argument_list|(
literal|2
argument_list|,
literal|10
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
name|numDocs
condition|;
operator|++
name|i
control|)
block|{
specifier|final
name|long
name|value
init|=
name|randomInt
argument_list|(
literal|5
argument_list|)
decl_stmt|;
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
literal|"long_value"
argument_list|,
name|value
argument_list|)
operator|.
name|field
argument_list|(
literal|"double_value"
argument_list|,
name|value
operator|+
literal|0.05
argument_list|)
operator|.
name|field
argument_list|(
literal|"string_value"
argument_list|,
literal|"str_"
operator|+
name|value
argument_list|)
decl_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|source
operator|.
name|field
argument_list|(
literal|"numeric_value"
argument_list|,
name|randomDouble
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"idx"
argument_list|,
literal|"type"
argument_list|)
operator|.
name|setSource
argument_list|(
name|source
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
name|refresh
argument_list|()
expr_stmt|;
name|ensureSearchable
argument_list|()
expr_stmt|;
block|}
DECL|method|assertCorrectlySorted
specifier|private
name|void
name|assertCorrectlySorted
parameter_list|(
name|Terms
name|terms
parameter_list|,
name|boolean
name|asc
parameter_list|,
name|SubAggregation
name|agg
parameter_list|)
block|{
name|assertThat
argument_list|(
name|terms
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|double
name|previousValue
init|=
name|asc
condition|?
name|Double
operator|.
name|NEGATIVE_INFINITY
else|:
name|Double
operator|.
name|POSITIVE_INFINITY
decl_stmt|;
for|for
control|(
name|Terms
operator|.
name|Bucket
name|bucket
range|:
name|terms
operator|.
name|getBuckets
argument_list|()
control|)
block|{
name|Aggregation
name|sub
init|=
name|bucket
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
name|agg
operator|.
name|name
argument_list|)
decl_stmt|;
name|double
name|value
init|=
name|agg
operator|.
name|getValue
argument_list|(
name|sub
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|Comparators
operator|.
name|compareDiscardNaN
argument_list|(
name|previousValue
argument_list|,
name|value
argument_list|,
name|asc
argument_list|)
operator|<=
literal|0
argument_list|)
expr_stmt|;
name|previousValue
operator|=
name|value
expr_stmt|;
block|}
block|}
DECL|method|assertCorrectlySorted
specifier|private
name|void
name|assertCorrectlySorted
parameter_list|(
name|Histogram
name|histo
parameter_list|,
name|boolean
name|asc
parameter_list|,
name|SubAggregation
name|agg
parameter_list|)
block|{
name|assertThat
argument_list|(
name|histo
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|double
name|previousValue
init|=
name|asc
condition|?
name|Double
operator|.
name|NEGATIVE_INFINITY
else|:
name|Double
operator|.
name|POSITIVE_INFINITY
decl_stmt|;
for|for
control|(
name|Histogram
operator|.
name|Bucket
name|bucket
range|:
name|histo
operator|.
name|getBuckets
argument_list|()
control|)
block|{
name|Aggregation
name|sub
init|=
name|bucket
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
name|agg
operator|.
name|name
argument_list|)
decl_stmt|;
name|double
name|value
init|=
name|agg
operator|.
name|getValue
argument_list|(
name|sub
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|Comparators
operator|.
name|compareDiscardNaN
argument_list|(
name|previousValue
argument_list|,
name|value
argument_list|,
name|asc
argument_list|)
operator|<=
literal|0
argument_list|)
expr_stmt|;
name|previousValue
operator|=
name|value
expr_stmt|;
block|}
block|}
DECL|method|testTerms
specifier|public
name|void
name|testTerms
parameter_list|(
name|String
name|fieldName
parameter_list|)
block|{
specifier|final
name|boolean
name|asc
init|=
name|randomBoolean
argument_list|()
decl_stmt|;
name|SubAggregation
name|agg
init|=
name|randomFrom
argument_list|(
name|SubAggregation
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
literal|"idx"
argument_list|)
operator|.
name|addAggregation
argument_list|(
name|terms
argument_list|(
literal|"terms"
argument_list|)
operator|.
name|field
argument_list|(
name|fieldName
argument_list|)
operator|.
name|collectMode
argument_list|(
name|randomFrom
argument_list|(
name|SubAggCollectionMode
operator|.
name|values
argument_list|()
argument_list|)
argument_list|)
operator|.
name|subAggregation
argument_list|(
name|agg
operator|.
name|builder
argument_list|()
argument_list|)
operator|.
name|order
argument_list|(
name|Terms
operator|.
name|Order
operator|.
name|aggregation
argument_list|(
name|agg
operator|.
name|sortKey
argument_list|()
argument_list|,
name|asc
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
specifier|final
name|Terms
name|terms
init|=
name|response
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"terms"
argument_list|)
decl_stmt|;
name|assertCorrectlySorted
argument_list|(
name|terms
argument_list|,
name|asc
argument_list|,
name|agg
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|stringTerms
specifier|public
name|void
name|stringTerms
parameter_list|()
block|{
name|testTerms
argument_list|(
literal|"string_value"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|longTerms
specifier|public
name|void
name|longTerms
parameter_list|()
block|{
name|testTerms
argument_list|(
literal|"long_value"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|doubleTerms
specifier|public
name|void
name|doubleTerms
parameter_list|()
block|{
name|testTerms
argument_list|(
literal|"double_value"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|longHistogram
specifier|public
name|void
name|longHistogram
parameter_list|()
block|{
specifier|final
name|boolean
name|asc
init|=
name|randomBoolean
argument_list|()
decl_stmt|;
name|SubAggregation
name|agg
init|=
name|randomFrom
argument_list|(
name|SubAggregation
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
literal|"idx"
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
literal|"long_value"
argument_list|)
operator|.
name|interval
argument_list|(
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|2
argument_list|)
argument_list|)
operator|.
name|subAggregation
argument_list|(
name|agg
operator|.
name|builder
argument_list|()
argument_list|)
operator|.
name|order
argument_list|(
name|Histogram
operator|.
name|Order
operator|.
name|aggregation
argument_list|(
name|agg
operator|.
name|sortKey
argument_list|()
argument_list|,
name|asc
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
specifier|final
name|Histogram
name|histo
init|=
name|response
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"histo"
argument_list|)
decl_stmt|;
name|assertCorrectlySorted
argument_list|(
name|histo
argument_list|,
name|asc
argument_list|,
name|agg
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit
