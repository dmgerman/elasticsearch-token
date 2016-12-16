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
name|common
operator|.
name|ParseFieldMatcher
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
name|XContentParser
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
name|XContentType
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
name|MatchNoneQueryBuilder
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
name|QueryBuilder
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
name|index
operator|.
name|query
operator|.
name|QueryParseContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|query
operator|.
name|IndicesQueriesRegistry
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
name|BaseAggregationTestCase
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
name|filters
operator|.
name|FiltersAggregationBuilder
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
name|filters
operator|.
name|FiltersAggregator
operator|.
name|KeyedFilter
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

begin_class
DECL|class|FiltersTests
specifier|public
class|class
name|FiltersTests
extends|extends
name|BaseAggregationTestCase
argument_list|<
name|FiltersAggregationBuilder
argument_list|>
block|{
annotation|@
name|Override
DECL|method|createTestAggregatorBuilder
specifier|protected
name|FiltersAggregationBuilder
name|createTestAggregatorBuilder
parameter_list|()
block|{
name|int
name|size
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|20
argument_list|)
decl_stmt|;
name|FiltersAggregationBuilder
name|factory
decl_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|KeyedFilter
index|[]
name|filters
init|=
operator|new
name|KeyedFilter
index|[
name|size
index|]
decl_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
for|for
control|(
name|String
name|key
range|:
name|randomUnique
argument_list|(
parameter_list|()
lambda|->
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|20
argument_list|)
argument_list|,
name|size
argument_list|)
control|)
block|{
name|filters
index|[
name|i
operator|++
index|]
operator|=
operator|new
name|KeyedFilter
argument_list|(
name|key
argument_list|,
name|QueryBuilders
operator|.
name|termQuery
argument_list|(
name|randomAsciiOfLengthBetween
argument_list|(
literal|5
argument_list|,
literal|20
argument_list|)
argument_list|,
name|randomAsciiOfLengthBetween
argument_list|(
literal|5
argument_list|,
literal|20
argument_list|)
argument_list|)
argument_list|)
block|;             }
name|factory
operator|=
operator|new
name|FiltersAggregationBuilder
argument_list|(
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|20
argument_list|)
argument_list|,
name|filters
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|QueryBuilder
index|[]
name|filters
init|=
operator|new
name|QueryBuilder
index|[
name|size
index|]
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
name|size
condition|;
name|i
operator|++
control|)
block|{
name|filters
index|[
name|i
index|]
operator|=
name|QueryBuilders
operator|.
name|termQuery
argument_list|(
name|randomAsciiOfLengthBetween
argument_list|(
literal|5
argument_list|,
literal|20
argument_list|)
argument_list|,
name|randomAsciiOfLengthBetween
argument_list|(
literal|5
argument_list|,
literal|20
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|factory
operator|=
operator|new
name|FiltersAggregationBuilder
argument_list|(
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|20
argument_list|)
argument_list|,
name|filters
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|factory
operator|.
name|otherBucket
argument_list|(
name|randomBoolean
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|factory
operator|.
name|otherBucketKey
argument_list|(
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|20
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|factory
return|;
block|}
comment|/**      * Test that when passing in keyed filters as list or array, the list stored internally is sorted by key      * Also check the list passed in is not modified by this but rather copied      */
DECL|method|testFiltersSortedByKey
specifier|public
name|void
name|testFiltersSortedByKey
parameter_list|()
block|{
name|KeyedFilter
index|[]
name|original
init|=
operator|new
name|KeyedFilter
index|[]
block|{
operator|new
name|KeyedFilter
argument_list|(
literal|"bbb"
argument_list|,
operator|new
name|MatchNoneQueryBuilder
argument_list|()
argument_list|)
block|,
operator|new
name|KeyedFilter
argument_list|(
literal|"aaa"
argument_list|,
operator|new
name|MatchNoneQueryBuilder
argument_list|()
argument_list|)
block|}
decl_stmt|;
name|FiltersAggregationBuilder
name|builder
decl_stmt|;
name|builder
operator|=
operator|new
name|FiltersAggregationBuilder
argument_list|(
literal|"my-agg"
argument_list|,
name|original
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"aaa"
argument_list|,
name|builder
operator|.
name|filters
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|key
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"bbb"
argument_list|,
name|builder
operator|.
name|filters
argument_list|()
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|key
argument_list|()
argument_list|)
expr_stmt|;
comment|// original should be unchanged
name|assertEquals
argument_list|(
literal|"bbb"
argument_list|,
name|original
index|[
literal|0
index|]
operator|.
name|key
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"aaa"
argument_list|,
name|original
index|[
literal|1
index|]
operator|.
name|key
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testOtherBucket
specifier|public
name|void
name|testOtherBucket
parameter_list|()
throws|throws
name|IOException
block|{
name|XContentBuilder
name|builder
init|=
name|XContentFactory
operator|.
name|contentBuilder
argument_list|(
name|randomFrom
argument_list|(
name|XContentType
operator|.
name|values
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|startArray
argument_list|(
literal|"filters"
argument_list|)
operator|.
name|endArray
argument_list|()
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|XContentParser
name|parser
init|=
name|createParser
argument_list|(
name|shuffleXContent
argument_list|(
name|builder
argument_list|)
argument_list|)
decl_stmt|;
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
name|QueryParseContext
name|context
init|=
operator|new
name|QueryParseContext
argument_list|(
operator|new
name|IndicesQueriesRegistry
argument_list|()
argument_list|,
name|parser
argument_list|,
name|ParseFieldMatcher
operator|.
name|STRICT
argument_list|)
decl_stmt|;
name|FiltersAggregationBuilder
name|filters
init|=
name|FiltersAggregationBuilder
operator|.
name|parse
argument_list|(
literal|"agg_name"
argument_list|,
name|context
argument_list|)
decl_stmt|;
comment|// The other bucket is disabled by default
name|assertFalse
argument_list|(
name|filters
operator|.
name|otherBucket
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|=
name|XContentFactory
operator|.
name|contentBuilder
argument_list|(
name|randomFrom
argument_list|(
name|XContentType
operator|.
name|values
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|startArray
argument_list|(
literal|"filters"
argument_list|)
operator|.
name|endArray
argument_list|()
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"other_bucket_key"
argument_list|,
literal|"some_key"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|parser
operator|=
name|createParser
argument_list|(
name|shuffleXContent
argument_list|(
name|builder
argument_list|)
argument_list|)
expr_stmt|;
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
name|context
operator|=
operator|new
name|QueryParseContext
argument_list|(
operator|new
name|IndicesQueriesRegistry
argument_list|()
argument_list|,
name|parser
argument_list|,
name|ParseFieldMatcher
operator|.
name|STRICT
argument_list|)
expr_stmt|;
name|filters
operator|=
name|FiltersAggregationBuilder
operator|.
name|parse
argument_list|(
literal|"agg_name"
argument_list|,
name|context
argument_list|)
expr_stmt|;
comment|// but setting a key enables it automatically
name|assertTrue
argument_list|(
name|filters
operator|.
name|otherBucket
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|=
name|XContentFactory
operator|.
name|contentBuilder
argument_list|(
name|randomFrom
argument_list|(
name|XContentType
operator|.
name|values
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|startArray
argument_list|(
literal|"filters"
argument_list|)
operator|.
name|endArray
argument_list|()
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"other_bucket"
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"other_bucket_key"
argument_list|,
literal|"some_key"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|parser
operator|=
name|createParser
argument_list|(
name|shuffleXContent
argument_list|(
name|builder
argument_list|)
argument_list|)
expr_stmt|;
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
name|context
operator|=
operator|new
name|QueryParseContext
argument_list|(
operator|new
name|IndicesQueriesRegistry
argument_list|()
argument_list|,
name|parser
argument_list|,
name|ParseFieldMatcher
operator|.
name|STRICT
argument_list|)
expr_stmt|;
name|filters
operator|=
name|FiltersAggregationBuilder
operator|.
name|parse
argument_list|(
literal|"agg_name"
argument_list|,
name|context
argument_list|)
expr_stmt|;
comment|// unless the other bucket is explicitly disabled
name|assertFalse
argument_list|(
name|filters
operator|.
name|otherBucket
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

