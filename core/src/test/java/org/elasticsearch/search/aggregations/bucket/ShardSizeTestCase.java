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
name|List
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
name|is
import|;
end_import

begin_class
DECL|class|ShardSizeTestCase
specifier|public
specifier|abstract
class|class
name|ShardSizeTestCase
extends|extends
name|ESIntegTestCase
block|{
annotation|@
name|Override
DECL|method|numberOfShards
specifier|protected
name|int
name|numberOfShards
parameter_list|()
block|{
comment|// we need at least 2
return|return
name|randomIntBetween
argument_list|(
literal|2
argument_list|,
name|DEFAULT_MAX_NUM_SHARDS
argument_list|)
return|;
block|}
DECL|method|createIdx
specifier|protected
name|void
name|createIdx
parameter_list|(
name|String
name|keyFieldMapping
parameter_list|)
block|{
name|assertAcked
argument_list|(
name|prepareCreate
argument_list|(
literal|"idx"
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"type"
argument_list|,
literal|"key"
argument_list|,
name|keyFieldMapping
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|field|routing1
specifier|protected
specifier|static
name|String
name|routing1
decl_stmt|;
comment|// routing key to shard 1
DECL|field|routing2
specifier|protected
specifier|static
name|String
name|routing2
decl_stmt|;
comment|// routing key to shard 2
DECL|method|indexData
specifier|protected
name|void
name|indexData
parameter_list|()
throws|throws
name|Exception
block|{
comment|/*           ||          ||           size = 3, shard_size = 5               ||           shard_size = size = 3               ||         ||==========||==================================================||===============================================||         || shard 1: ||  "1" - 5 | "2" - 4 | "3" - 3 | "4" - 2 | "5" - 1 || "1" - 5 | "3" - 3 | "2" - 4                   ||         ||----------||--------------------------------------------------||-----------------------------------------------||         || shard 2: ||  "1" - 3 | "2" - 1 | "3" - 5 | "4" - 2 | "5" - 1 || "1" - 3 | "3" - 5 | "4" - 2                   ||         ||----------||--------------------------------------------------||-----------------------------------------------||         || reduced: ||  "1" - 8 | "2" - 5 | "3" - 8 | "4" - 4 | "5" - 2 ||                                               ||         ||          ||                                                  || "1" - 8, "3" - 8, "2" - 4<= WRONG         ||         ||          ||  "1" - 8 | "3" - 8 | "2" - 5<= CORRECT      ||                                               ||           */
name|List
argument_list|<
name|IndexRequestBuilder
argument_list|>
name|docs
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|routing1
operator|=
name|routingKeyForShard
argument_list|(
literal|"idx"
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|routing2
operator|=
name|routingKeyForShard
argument_list|(
literal|"idx"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|docs
operator|.
name|addAll
argument_list|(
name|indexDoc
argument_list|(
name|routing1
argument_list|,
literal|"1"
argument_list|,
literal|5
argument_list|)
argument_list|)
expr_stmt|;
name|docs
operator|.
name|addAll
argument_list|(
name|indexDoc
argument_list|(
name|routing1
argument_list|,
literal|"2"
argument_list|,
literal|4
argument_list|)
argument_list|)
expr_stmt|;
name|docs
operator|.
name|addAll
argument_list|(
name|indexDoc
argument_list|(
name|routing1
argument_list|,
literal|"3"
argument_list|,
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|docs
operator|.
name|addAll
argument_list|(
name|indexDoc
argument_list|(
name|routing1
argument_list|,
literal|"4"
argument_list|,
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|docs
operator|.
name|addAll
argument_list|(
name|indexDoc
argument_list|(
name|routing1
argument_list|,
literal|"5"
argument_list|,
literal|1
argument_list|)
argument_list|)
expr_stmt|;
comment|// total docs in shard "1" = 15
name|docs
operator|.
name|addAll
argument_list|(
name|indexDoc
argument_list|(
name|routing2
argument_list|,
literal|"1"
argument_list|,
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|docs
operator|.
name|addAll
argument_list|(
name|indexDoc
argument_list|(
name|routing2
argument_list|,
literal|"2"
argument_list|,
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|docs
operator|.
name|addAll
argument_list|(
name|indexDoc
argument_list|(
name|routing2
argument_list|,
literal|"3"
argument_list|,
literal|5
argument_list|)
argument_list|)
expr_stmt|;
name|docs
operator|.
name|addAll
argument_list|(
name|indexDoc
argument_list|(
name|routing2
argument_list|,
literal|"4"
argument_list|,
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|docs
operator|.
name|addAll
argument_list|(
name|indexDoc
argument_list|(
name|routing2
argument_list|,
literal|"5"
argument_list|,
literal|1
argument_list|)
argument_list|)
expr_stmt|;
comment|// total docs in shard "2"  = 12
name|indexRandom
argument_list|(
literal|true
argument_list|,
name|docs
argument_list|)
expr_stmt|;
name|SearchResponse
name|resp
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"idx"
argument_list|)
operator|.
name|setTypes
argument_list|(
literal|"type"
argument_list|)
operator|.
name|setRouting
argument_list|(
name|routing1
argument_list|)
operator|.
name|setQuery
argument_list|(
name|matchAllQuery
argument_list|()
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
name|resp
argument_list|)
expr_stmt|;
name|long
name|totalOnOne
init|=
name|resp
operator|.
name|getHits
argument_list|()
operator|.
name|getTotalHits
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|totalOnOne
argument_list|,
name|is
argument_list|(
literal|15L
argument_list|)
argument_list|)
expr_stmt|;
name|resp
operator|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"idx"
argument_list|)
operator|.
name|setTypes
argument_list|(
literal|"type"
argument_list|)
operator|.
name|setRouting
argument_list|(
name|routing2
argument_list|)
operator|.
name|setQuery
argument_list|(
name|matchAllQuery
argument_list|()
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
name|resp
argument_list|)
expr_stmt|;
name|long
name|totalOnTwo
init|=
name|resp
operator|.
name|getHits
argument_list|()
operator|.
name|getTotalHits
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|totalOnTwo
argument_list|,
name|is
argument_list|(
literal|12L
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|indexDoc
specifier|protected
name|List
argument_list|<
name|IndexRequestBuilder
argument_list|>
name|indexDoc
parameter_list|(
name|String
name|shard
parameter_list|,
name|String
name|key
parameter_list|,
name|int
name|times
parameter_list|)
throws|throws
name|Exception
block|{
name|IndexRequestBuilder
index|[]
name|builders
init|=
operator|new
name|IndexRequestBuilder
index|[
name|times
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
name|times
condition|;
name|i
operator|++
control|)
block|{
name|builders
index|[
name|i
index|]
operator|=
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
name|setRouting
argument_list|(
name|shard
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
literal|"key"
argument_list|,
name|key
argument_list|)
operator|.
name|field
argument_list|(
literal|"value"
argument_list|,
literal|1
argument_list|)
operator|.
name|endObject
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|Arrays
operator|.
name|asList
argument_list|(
name|builders
argument_list|)
return|;
block|}
block|}
end_class

end_unit

