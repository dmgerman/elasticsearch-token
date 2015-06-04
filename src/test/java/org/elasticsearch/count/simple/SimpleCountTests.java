begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.count.simple
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|count
operator|.
name|simple
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
name|util
operator|.
name|Constants
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
name|count
operator|.
name|CountResponse
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
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ExecutionException
import|;
end_import

begin_import
import|import static
name|com
operator|.
name|carrotsearch
operator|.
name|randomizedtesting
operator|.
name|RandomizedTest
operator|.
name|systemPropertyAsBoolean
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
name|boolQuery
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
name|rangeQuery
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
name|is
import|;
end_import

begin_class
DECL|class|SimpleCountTests
specifier|public
class|class
name|SimpleCountTests
extends|extends
name|ElasticsearchIntegrationTest
block|{
annotation|@
name|Test
DECL|method|testCountRandomPreference
specifier|public
name|void
name|testCountRandomPreference
parameter_list|()
throws|throws
name|InterruptedException
throws|,
name|ExecutionException
block|{
name|createIndex
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
name|indexRandom
argument_list|(
literal|true
argument_list|,
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"field"
argument_list|,
literal|"value"
argument_list|)
argument_list|,
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
literal|"2"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"field"
argument_list|,
literal|"value"
argument_list|)
argument_list|,
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
literal|"3"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"field"
argument_list|,
literal|"value"
argument_list|)
argument_list|,
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
literal|"4"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"field"
argument_list|,
literal|"value"
argument_list|)
argument_list|,
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
literal|"5"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"field"
argument_list|,
literal|"value"
argument_list|)
argument_list|,
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
literal|"6"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"field"
argument_list|,
literal|"value"
argument_list|)
argument_list|)
expr_stmt|;
name|int
name|iters
init|=
name|scaledRandomIntBetween
argument_list|(
literal|10
argument_list|,
literal|100
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
name|iters
condition|;
name|i
operator|++
control|)
block|{
name|String
name|randomPreference
init|=
name|randomUnicodeOfLengthBetween
argument_list|(
literal|0
argument_list|,
literal|4
argument_list|)
decl_stmt|;
comment|// randomPreference should not start with '_' (reserved for known preference types (e.g. _shards, _primary)
while|while
condition|(
name|randomPreference
operator|.
name|startsWith
argument_list|(
literal|"_"
argument_list|)
condition|)
block|{
name|randomPreference
operator|=
name|randomUnicodeOfLengthBetween
argument_list|(
literal|0
argument_list|,
literal|4
argument_list|)
expr_stmt|;
block|}
comment|// id is not indexed, but lets see that we automatically convert to
name|CountResponse
name|countResponse
init|=
name|client
argument_list|()
operator|.
name|prepareCount
argument_list|()
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
name|matchAllQuery
argument_list|()
argument_list|)
operator|.
name|setPreference
argument_list|(
name|randomPreference
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertHitCount
argument_list|(
name|countResponse
argument_list|,
literal|6l
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
DECL|method|simpleIpTests
specifier|public
name|void
name|simpleIpTests
parameter_list|()
throws|throws
name|Exception
block|{
name|createIndex
argument_list|(
literal|"test"
argument_list|)
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
name|preparePutMapping
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setType
argument_list|(
literal|"type1"
argument_list|)
operator|.
name|setSource
argument_list|(
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
literal|"from"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"ip"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"to"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"ip"
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
name|actionGet
argument_list|()
expr_stmt|;
name|client
argument_list|()
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
literal|"from"
argument_list|,
literal|"192.168.0.5"
argument_list|,
literal|"to"
argument_list|,
literal|"192.168.0.10"
argument_list|)
operator|.
name|setRefresh
argument_list|(
literal|true
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|CountResponse
name|countResponse
init|=
name|client
argument_list|()
operator|.
name|prepareCount
argument_list|()
operator|.
name|setQuery
argument_list|(
name|boolQuery
argument_list|()
operator|.
name|must
argument_list|(
name|rangeQuery
argument_list|(
literal|"from"
argument_list|)
operator|.
name|lt
argument_list|(
literal|"192.168.0.7"
argument_list|)
argument_list|)
operator|.
name|must
argument_list|(
name|rangeQuery
argument_list|(
literal|"to"
argument_list|)
operator|.
name|gt
argument_list|(
literal|"192.168.0.7"
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
name|countResponse
argument_list|,
literal|1l
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|simpleIdTests
specifier|public
name|void
name|simpleIdTests
parameter_list|()
block|{
name|createIndex
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
literal|"XXX1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"field"
argument_list|,
literal|"value"
argument_list|)
operator|.
name|setRefresh
argument_list|(
literal|true
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
comment|// id is not indexed, but lets see that we automatically convert to
name|CountResponse
name|countResponse
init|=
name|client
argument_list|()
operator|.
name|prepareCount
argument_list|()
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
name|termQuery
argument_list|(
literal|"_id"
argument_list|,
literal|"XXX1"
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
name|countResponse
argument_list|,
literal|1l
argument_list|)
expr_stmt|;
name|countResponse
operator|=
name|client
argument_list|()
operator|.
name|prepareCount
argument_list|()
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
name|queryStringQuery
argument_list|(
literal|"_id:XXX1"
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|assertHitCount
argument_list|(
name|countResponse
argument_list|,
literal|1l
argument_list|)
expr_stmt|;
comment|// id is not index, but we can automatically support prefix as well
name|countResponse
operator|=
name|client
argument_list|()
operator|.
name|prepareCount
argument_list|()
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
name|prefixQuery
argument_list|(
literal|"_id"
argument_list|,
literal|"XXX"
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|assertHitCount
argument_list|(
name|countResponse
argument_list|,
literal|1l
argument_list|)
expr_stmt|;
name|countResponse
operator|=
name|client
argument_list|()
operator|.
name|prepareCount
argument_list|()
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
name|queryStringQuery
argument_list|(
literal|"_id:XXX*"
argument_list|)
operator|.
name|lowercaseExpandedTerms
argument_list|(
literal|false
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|assertHitCount
argument_list|(
name|countResponse
argument_list|,
literal|1l
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|simpleCountEarlyTerminationTests
specifier|public
name|void
name|simpleCountEarlyTerminationTests
parameter_list|()
throws|throws
name|Exception
block|{
comment|// set up one shard only to test early termination
name|prepareCreate
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setSettings
argument_list|(
name|SETTING_NUMBER_OF_SHARDS
argument_list|,
literal|1
argument_list|,
name|SETTING_NUMBER_OF_REPLICAS
argument_list|,
literal|0
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
name|int
name|max
init|=
name|randomIntBetween
argument_list|(
literal|3
argument_list|,
literal|29
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|IndexRequestBuilder
argument_list|>
name|docbuilders
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|max
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<=
name|max
condition|;
name|i
operator|++
control|)
block|{
name|String
name|id
init|=
name|String
operator|.
name|valueOf
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|docbuilders
operator|.
name|add
argument_list|(
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
name|id
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"field"
argument_list|,
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|indexRandom
argument_list|(
literal|true
argument_list|,
name|docbuilders
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
name|refresh
argument_list|()
expr_stmt|;
comment|// sanity check
name|CountResponse
name|countResponse
init|=
name|client
argument_list|()
operator|.
name|prepareCount
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
name|rangeQuery
argument_list|(
literal|"field"
argument_list|)
operator|.
name|gte
argument_list|(
literal|1
argument_list|)
operator|.
name|lte
argument_list|(
name|max
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
name|countResponse
argument_list|,
name|max
argument_list|)
expr_stmt|;
comment|// threshold<= actual count
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<=
name|max
condition|;
name|i
operator|++
control|)
block|{
name|countResponse
operator|=
name|client
argument_list|()
operator|.
name|prepareCount
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
name|rangeQuery
argument_list|(
literal|"field"
argument_list|)
operator|.
name|gte
argument_list|(
literal|1
argument_list|)
operator|.
name|lte
argument_list|(
name|max
argument_list|)
argument_list|)
operator|.
name|setTerminateAfter
argument_list|(
name|i
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|assertHitCount
argument_list|(
name|countResponse
argument_list|,
name|i
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|countResponse
operator|.
name|terminatedEarly
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// threshold> actual count
name|countResponse
operator|=
name|client
argument_list|()
operator|.
name|prepareCount
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
name|rangeQuery
argument_list|(
literal|"field"
argument_list|)
operator|.
name|gte
argument_list|(
literal|1
argument_list|)
operator|.
name|lte
argument_list|(
name|max
argument_list|)
argument_list|)
operator|.
name|setTerminateAfter
argument_list|(
name|max
operator|+
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
name|max
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|assertHitCount
argument_list|(
name|countResponse
argument_list|,
name|max
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|countResponse
operator|.
name|terminatedEarly
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|localDependentDateTests
specifier|public
name|void
name|localDependentDateTests
parameter_list|()
throws|throws
name|Exception
block|{
name|assumeFalse
argument_list|(
literal|"Locals are buggy on JDK9EA"
argument_list|,
name|Constants
operator|.
name|JRE_IS_MINIMUM_JAVA9
operator|&&
name|systemPropertyAsBoolean
argument_list|(
literal|"tests.security.manager"
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertAcked
argument_list|(
name|prepareCreate
argument_list|(
literal|"test"
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"type1"
argument_list|,
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
literal|"date_field"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"date"
argument_list|)
operator|.
name|field
argument_list|(
literal|"format"
argument_list|,
literal|"E, d MMM yyyy HH:mm:ss Z"
argument_list|)
operator|.
name|field
argument_list|(
literal|"locale"
argument_list|,
literal|"de"
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
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|()
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
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
literal|""
operator|+
name|i
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"date_field"
argument_list|,
literal|"Mi, 06 Dez 2000 02:55:00 -0800"
argument_list|)
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
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
literal|""
operator|+
operator|(
literal|10
operator|+
name|i
operator|)
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"date_field"
argument_list|,
literal|"Do, 07 Dez 2000 02:55:00 -0800"
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
name|CountResponse
name|countResponse
init|=
name|client
argument_list|()
operator|.
name|prepareCount
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
name|rangeQuery
argument_list|(
literal|"date_field"
argument_list|)
operator|.
name|gte
argument_list|(
literal|"Di, 05 Dez 2000 02:55:00 -0800"
argument_list|)
operator|.
name|lte
argument_list|(
literal|"Do, 07 Dez 2000 00:00:00 -0800"
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
name|countResponse
argument_list|,
literal|10l
argument_list|)
expr_stmt|;
name|countResponse
operator|=
name|client
argument_list|()
operator|.
name|prepareCount
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
name|rangeQuery
argument_list|(
literal|"date_field"
argument_list|)
operator|.
name|gte
argument_list|(
literal|"Di, 05 Dez 2000 02:55:00 -0800"
argument_list|)
operator|.
name|lte
argument_list|(
literal|"Fr, 08 Dez 2000 00:00:00 -0800"
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|assertHitCount
argument_list|(
name|countResponse
argument_list|,
literal|20l
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
DECL|method|testThatNonEpochDatesCanBeSearch
specifier|public
name|void
name|testThatNonEpochDatesCanBeSearch
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
name|addMapping
argument_list|(
literal|"type1"
argument_list|,
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
literal|"date_field"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"date"
argument_list|)
operator|.
name|field
argument_list|(
literal|"format"
argument_list|,
literal|"yyyyMMddHH"
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
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
name|XContentBuilder
name|document
init|=
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"date_field"
argument_list|,
literal|"2015060210"
argument_list|)
operator|.
name|endObject
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|)
operator|.
name|setSource
argument_list|(
name|document
argument_list|)
operator|.
name|get
argument_list|()
operator|.
name|isCreated
argument_list|()
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|document
operator|=
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"date_field"
argument_list|,
literal|"2014060210"
argument_list|)
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|)
operator|.
name|setSource
argument_list|(
name|document
argument_list|)
operator|.
name|get
argument_list|()
operator|.
name|isCreated
argument_list|()
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
comment|// this is a timestamp in 2015 and should not be returned in counting when filtering by year
name|document
operator|=
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"date_field"
argument_list|,
literal|"1433236702"
argument_list|)
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|)
operator|.
name|setSource
argument_list|(
name|document
argument_list|)
operator|.
name|get
argument_list|()
operator|.
name|isCreated
argument_list|()
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|refresh
argument_list|()
expr_stmt|;
name|assertHitCount
argument_list|(
name|client
argument_list|()
operator|.
name|prepareCount
argument_list|(
literal|"test"
argument_list|)
operator|.
name|get
argument_list|()
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|CountResponse
name|countResponse
init|=
name|client
argument_list|()
operator|.
name|prepareCount
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
name|rangeQuery
argument_list|(
literal|"date_field"
argument_list|)
operator|.
name|from
argument_list|(
literal|"2015010100"
argument_list|)
operator|.
name|to
argument_list|(
literal|"2015123123"
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertHitCount
argument_list|(
name|countResponse
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|countResponse
operator|=
name|client
argument_list|()
operator|.
name|prepareCount
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
name|rangeQuery
argument_list|(
literal|"date_field"
argument_list|)
operator|.
name|from
argument_list|(
literal|2015010100
argument_list|)
operator|.
name|to
argument_list|(
literal|2015123123
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|assertHitCount
argument_list|(
name|countResponse
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|countResponse
operator|=
name|client
argument_list|()
operator|.
name|prepareCount
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
name|rangeQuery
argument_list|(
literal|"date_field"
argument_list|)
operator|.
name|from
argument_list|(
literal|2015010100
argument_list|)
operator|.
name|to
argument_list|(
literal|2015123123
argument_list|)
operator|.
name|timeZone
argument_list|(
literal|"UTC"
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|assertHitCount
argument_list|(
name|countResponse
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

