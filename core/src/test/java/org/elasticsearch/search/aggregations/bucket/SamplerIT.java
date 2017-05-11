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
name|admin
operator|.
name|indices
operator|.
name|refresh
operator|.
name|RefreshRequest
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
name|action
operator|.
name|search
operator|.
name|SearchType
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
name|TermQueryBuilder
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
name|sampler
operator|.
name|Sampler
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
name|sampler
operator|.
name|SamplerAggregator
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
name|sampler
operator|.
name|SamplerAggregationBuilder
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
name|bucket
operator|.
name|terms
operator|.
name|Terms
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
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|BucketOrder
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
name|Collection
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
name|cluster
operator|.
name|metadata
operator|.
name|IndexMetaData
operator|.
name|SETTING_NUMBER_OF_REPLICAS
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|metadata
operator|.
name|IndexMetaData
operator|.
name|SETTING_NUMBER_OF_SHARDS
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
name|search
operator|.
name|aggregations
operator|.
name|AggregationBuilders
operator|.
name|sampler
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

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|greaterThan
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
name|greaterThanOrEqualTo
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
name|lessThanOrEqualTo
import|;
end_import

begin_comment
comment|/**  * Tests the Sampler aggregation  */
end_comment

begin_class
annotation|@
name|ESIntegTestCase
operator|.
name|SuiteScopeTestCase
DECL|class|SamplerIT
specifier|public
class|class
name|SamplerIT
extends|extends
name|ESIntegTestCase
block|{
DECL|field|NUM_SHARDS
specifier|public
specifier|static
specifier|final
name|int
name|NUM_SHARDS
init|=
literal|2
decl_stmt|;
DECL|method|randomExecutionHint
specifier|public
name|String
name|randomExecutionHint
parameter_list|()
block|{
return|return
name|randomBoolean
argument_list|()
condition|?
literal|null
else|:
name|randomFrom
argument_list|(
name|SamplerAggregator
operator|.
name|ExecutionMode
operator|.
name|values
argument_list|()
argument_list|)
operator|.
name|toString
argument_list|()
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
name|assertAcked
argument_list|(
name|prepareCreate
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setSettings
argument_list|(
name|SETTING_NUMBER_OF_SHARDS
argument_list|,
name|NUM_SHARDS
argument_list|,
name|SETTING_NUMBER_OF_REPLICAS
argument_list|,
literal|0
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"book"
argument_list|,
literal|"author"
argument_list|,
literal|"type=keyword"
argument_list|,
literal|"name"
argument_list|,
literal|"type=text"
argument_list|,
literal|"genre"
argument_list|,
literal|"type=keyword"
argument_list|,
literal|"price"
argument_list|,
literal|"type=float"
argument_list|)
argument_list|)
expr_stmt|;
name|createIndex
argument_list|(
literal|"idx_unmapped"
argument_list|)
expr_stmt|;
comment|// idx_unmapped_author is same as main index but missing author field
name|assertAcked
argument_list|(
name|prepareCreate
argument_list|(
literal|"idx_unmapped_author"
argument_list|)
operator|.
name|setSettings
argument_list|(
name|SETTING_NUMBER_OF_SHARDS
argument_list|,
name|NUM_SHARDS
argument_list|,
name|SETTING_NUMBER_OF_REPLICAS
argument_list|,
literal|0
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"book"
argument_list|,
literal|"name"
argument_list|,
literal|"type=text"
argument_list|,
literal|"genre"
argument_list|,
literal|"type=keyword"
argument_list|,
literal|"price"
argument_list|,
literal|"type=float"
argument_list|)
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
name|String
name|data
index|[]
init|=
block|{
comment|// "id,cat,name,price,inStock,author_t,series_t,sequence_i,genre_s",
literal|"0553573403,book,A Game of Thrones,7.99,true,George R.R. Martin,A Song of Ice and Fire,1,fantasy"
block|,
literal|"0553579908,book,A Clash of Kings,7.99,true,George R.R. Martin,A Song of Ice and Fire,2,fantasy"
block|,
literal|"055357342X,book,A Storm of Swords,7.99,true,George R.R. Martin,A Song of Ice and Fire,3,fantasy"
block|,
literal|"0553293354,book,Foundation,17.99,true,Isaac Asimov,Foundation Novels,1,scifi"
block|,
literal|"0812521390,book,The Black Company,6.99,false,Glen Cook,The Chronicles of The Black Company,1,fantasy"
block|,
literal|"0812550706,book,Ender's Game,6.99,true,Orson Scott Card,Ender,1,scifi"
block|,
literal|"0441385532,book,Jhereg,7.95,false,Steven Brust,Vlad Taltos,1,fantasy"
block|,
literal|"0380014300,book,Nine Princes In Amber,6.99,true,Roger Zelazny,the Chronicles of Amber,1,fantasy"
block|,
literal|"0805080481,book,The Book of Three,5.99,true,Lloyd Alexander,The Chronicles of Prydain,1,fantasy"
block|,
literal|"080508049X,book,The Black Cauldron,5.99,true,Lloyd Alexander,The Chronicles of Prydain,2,fantasy"
block|}
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
name|data
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|String
index|[]
name|parts
init|=
name|data
index|[
name|i
index|]
operator|.
name|split
argument_list|(
literal|","
argument_list|)
decl_stmt|;
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"book"
argument_list|,
literal|""
operator|+
name|i
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"author"
argument_list|,
name|parts
index|[
literal|5
index|]
argument_list|,
literal|"name"
argument_list|,
name|parts
index|[
literal|2
index|]
argument_list|,
literal|"genre"
argument_list|,
name|parts
index|[
literal|8
index|]
argument_list|,
literal|"price"
argument_list|,
name|Float
operator|.
name|parseFloat
argument_list|(
name|parts
index|[
literal|3
index|]
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"idx_unmapped_author"
argument_list|,
literal|"book"
argument_list|,
literal|""
operator|+
name|i
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"name"
argument_list|,
name|parts
index|[
literal|2
index|]
argument_list|,
literal|"genre"
argument_list|,
name|parts
index|[
literal|8
index|]
argument_list|,
literal|"price"
argument_list|,
name|Float
operator|.
name|parseFloat
argument_list|(
name|parts
index|[
literal|3
index|]
argument_list|)
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
name|refresh
argument_list|(
operator|new
name|RefreshRequest
argument_list|(
literal|"test"
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
DECL|method|testIssue10719
specifier|public
name|void
name|testIssue10719
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Tests that we can refer to nested elements under a sample in a path
comment|// statement
name|boolean
name|asc
init|=
name|randomBoolean
argument_list|()
decl_stmt|;
name|SearchResponse
name|response
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setTypes
argument_list|(
literal|"book"
argument_list|)
operator|.
name|setSearchType
argument_list|(
name|SearchType
operator|.
name|QUERY_THEN_FETCH
argument_list|)
operator|.
name|addAggregation
argument_list|(
name|terms
argument_list|(
literal|"genres"
argument_list|)
operator|.
name|field
argument_list|(
literal|"genre"
argument_list|)
operator|.
name|order
argument_list|(
name|BucketOrder
operator|.
name|aggregation
argument_list|(
literal|"sample>max_price.value"
argument_list|,
name|asc
argument_list|)
argument_list|)
operator|.
name|subAggregation
argument_list|(
name|sampler
argument_list|(
literal|"sample"
argument_list|)
operator|.
name|shardSize
argument_list|(
literal|100
argument_list|)
operator|.
name|subAggregation
argument_list|(
name|max
argument_list|(
literal|"max_price"
argument_list|)
operator|.
name|field
argument_list|(
literal|"price"
argument_list|)
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
name|Terms
name|genres
init|=
name|response
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"genres"
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|?
extends|extends
name|Bucket
argument_list|>
name|genreBuckets
init|=
name|genres
operator|.
name|getBuckets
argument_list|()
decl_stmt|;
comment|// For this test to be useful we need>1 genre bucket to compare
name|assertThat
argument_list|(
name|genreBuckets
operator|.
name|size
argument_list|()
argument_list|,
name|greaterThan
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|double
name|lastMaxPrice
init|=
name|asc
condition|?
name|Double
operator|.
name|MIN_VALUE
else|:
name|Double
operator|.
name|MAX_VALUE
decl_stmt|;
for|for
control|(
name|Terms
operator|.
name|Bucket
name|genreBucket
range|:
name|genres
operator|.
name|getBuckets
argument_list|()
control|)
block|{
name|Sampler
name|sample
init|=
name|genreBucket
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"sample"
argument_list|)
decl_stmt|;
name|Max
name|maxPriceInGenre
init|=
name|sample
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"max_price"
argument_list|)
decl_stmt|;
name|double
name|price
init|=
name|maxPriceInGenre
operator|.
name|getValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|asc
condition|)
block|{
name|assertThat
argument_list|(
name|price
argument_list|,
name|greaterThanOrEqualTo
argument_list|(
name|lastMaxPrice
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertThat
argument_list|(
name|price
argument_list|,
name|lessThanOrEqualTo
argument_list|(
name|lastMaxPrice
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|lastMaxPrice
operator|=
name|price
expr_stmt|;
block|}
block|}
DECL|method|testSimpleSampler
specifier|public
name|void
name|testSimpleSampler
parameter_list|()
throws|throws
name|Exception
block|{
name|SamplerAggregationBuilder
name|sampleAgg
init|=
name|sampler
argument_list|(
literal|"sample"
argument_list|)
operator|.
name|shardSize
argument_list|(
literal|100
argument_list|)
decl_stmt|;
name|sampleAgg
operator|.
name|subAggregation
argument_list|(
name|terms
argument_list|(
literal|"authors"
argument_list|)
operator|.
name|field
argument_list|(
literal|"author"
argument_list|)
argument_list|)
expr_stmt|;
name|SearchResponse
name|response
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setSearchType
argument_list|(
name|SearchType
operator|.
name|QUERY_THEN_FETCH
argument_list|)
operator|.
name|setQuery
argument_list|(
operator|new
name|TermQueryBuilder
argument_list|(
literal|"genre"
argument_list|,
literal|"fantasy"
argument_list|)
argument_list|)
operator|.
name|setFrom
argument_list|(
literal|0
argument_list|)
operator|.
name|setSize
argument_list|(
literal|60
argument_list|)
operator|.
name|addAggregation
argument_list|(
name|sampleAgg
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
name|Sampler
name|sample
init|=
name|response
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"sample"
argument_list|)
decl_stmt|;
name|Terms
name|authors
init|=
name|sample
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"authors"
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|?
extends|extends
name|Bucket
argument_list|>
name|testBuckets
init|=
name|authors
operator|.
name|getBuckets
argument_list|()
decl_stmt|;
name|long
name|maxBooksPerAuthor
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Terms
operator|.
name|Bucket
name|testBucket
range|:
name|testBuckets
control|)
block|{
name|maxBooksPerAuthor
operator|=
name|Math
operator|.
name|max
argument_list|(
name|testBucket
operator|.
name|getDocCount
argument_list|()
argument_list|,
name|maxBooksPerAuthor
argument_list|)
expr_stmt|;
block|}
name|assertThat
argument_list|(
name|maxBooksPerAuthor
argument_list|,
name|equalTo
argument_list|(
literal|3L
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testUnmappedChildAggNoDiversity
specifier|public
name|void
name|testUnmappedChildAggNoDiversity
parameter_list|()
throws|throws
name|Exception
block|{
name|SamplerAggregationBuilder
name|sampleAgg
init|=
name|sampler
argument_list|(
literal|"sample"
argument_list|)
operator|.
name|shardSize
argument_list|(
literal|100
argument_list|)
decl_stmt|;
name|sampleAgg
operator|.
name|subAggregation
argument_list|(
name|terms
argument_list|(
literal|"authors"
argument_list|)
operator|.
name|field
argument_list|(
literal|"author"
argument_list|)
argument_list|)
expr_stmt|;
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
name|setSearchType
argument_list|(
name|SearchType
operator|.
name|QUERY_THEN_FETCH
argument_list|)
operator|.
name|setQuery
argument_list|(
operator|new
name|TermQueryBuilder
argument_list|(
literal|"genre"
argument_list|,
literal|"fantasy"
argument_list|)
argument_list|)
operator|.
name|setFrom
argument_list|(
literal|0
argument_list|)
operator|.
name|setSize
argument_list|(
literal|60
argument_list|)
operator|.
name|addAggregation
argument_list|(
name|sampleAgg
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
name|Sampler
name|sample
init|=
name|response
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"sample"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|sample
operator|.
name|getDocCount
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
name|Terms
name|authors
init|=
name|sample
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"authors"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|authors
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
DECL|method|testPartiallyUnmappedChildAggNoDiversity
specifier|public
name|void
name|testPartiallyUnmappedChildAggNoDiversity
parameter_list|()
throws|throws
name|Exception
block|{
name|SamplerAggregationBuilder
name|sampleAgg
init|=
name|sampler
argument_list|(
literal|"sample"
argument_list|)
operator|.
name|shardSize
argument_list|(
literal|100
argument_list|)
decl_stmt|;
name|sampleAgg
operator|.
name|subAggregation
argument_list|(
name|terms
argument_list|(
literal|"authors"
argument_list|)
operator|.
name|field
argument_list|(
literal|"author"
argument_list|)
argument_list|)
expr_stmt|;
name|SearchResponse
name|response
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"idx_unmapped"
argument_list|,
literal|"test"
argument_list|)
operator|.
name|setSearchType
argument_list|(
name|SearchType
operator|.
name|QUERY_THEN_FETCH
argument_list|)
operator|.
name|setQuery
argument_list|(
operator|new
name|TermQueryBuilder
argument_list|(
literal|"genre"
argument_list|,
literal|"fantasy"
argument_list|)
argument_list|)
operator|.
name|setFrom
argument_list|(
literal|0
argument_list|)
operator|.
name|setSize
argument_list|(
literal|60
argument_list|)
operator|.
name|setExplain
argument_list|(
literal|true
argument_list|)
operator|.
name|addAggregation
argument_list|(
name|sampleAgg
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
name|Sampler
name|sample
init|=
name|response
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"sample"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|sample
operator|.
name|getDocCount
argument_list|()
argument_list|,
name|greaterThan
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
name|Terms
name|authors
init|=
name|sample
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"authors"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|authors
operator|.
name|getBuckets
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|greaterThan
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

