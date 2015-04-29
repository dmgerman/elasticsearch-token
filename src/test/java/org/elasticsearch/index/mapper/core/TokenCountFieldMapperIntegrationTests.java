begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.mapper.core
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
operator|.
name|core
package|;
end_package

begin_import
import|import
name|com
operator|.
name|carrotsearch
operator|.
name|randomizedtesting
operator|.
name|annotations
operator|.
name|Name
import|;
end_import

begin_import
import|import
name|com
operator|.
name|carrotsearch
operator|.
name|randomizedtesting
operator|.
name|annotations
operator|.
name|ParametersFactory
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableList
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
name|util
operator|.
name|LuceneTestCase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchException
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
name|bulk
operator|.
name|BulkResponse
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
name|action
operator|.
name|search
operator|.
name|SearchRequestBuilder
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
name|SearchHit
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
name|hamcrest
operator|.
name|Matchers
operator|.
name|*
import|;
end_import

begin_class
DECL|class|TokenCountFieldMapperIntegrationTests
specifier|public
class|class
name|TokenCountFieldMapperIntegrationTests
extends|extends
name|ElasticsearchIntegrationTest
block|{
annotation|@
name|ParametersFactory
DECL|method|buildParameters
specifier|public
specifier|static
name|Iterable
argument_list|<
name|Object
index|[]
argument_list|>
name|buildParameters
parameter_list|()
block|{
name|List
argument_list|<
name|Object
index|[]
argument_list|>
name|parameters
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|boolean
name|storeCountedFields
range|:
operator|new
name|boolean
index|[]
block|{
literal|true
block|,
literal|false
block|}
control|)
block|{
for|for
control|(
name|boolean
name|loadCountedFields
range|:
operator|new
name|boolean
index|[]
block|{
literal|true
block|,
literal|false
block|}
control|)
block|{
name|parameters
operator|.
name|add
argument_list|(
operator|new
name|Object
index|[]
block|{
name|storeCountedFields
block|,
name|loadCountedFields
block|}
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|parameters
return|;
block|}
DECL|field|storeCountedFields
specifier|private
specifier|final
name|boolean
name|storeCountedFields
decl_stmt|;
DECL|field|loadCountedFields
specifier|private
specifier|final
name|boolean
name|loadCountedFields
decl_stmt|;
DECL|method|TokenCountFieldMapperIntegrationTests
specifier|public
name|TokenCountFieldMapperIntegrationTests
parameter_list|(
annotation|@
name|Name
argument_list|(
literal|"storeCountedFields"
argument_list|)
name|boolean
name|storeCountedFields
parameter_list|,
annotation|@
name|Name
argument_list|(
literal|"loadCountedFields"
argument_list|)
name|boolean
name|loadCountedFields
parameter_list|)
block|{
name|this
operator|.
name|storeCountedFields
operator|=
name|storeCountedFields
expr_stmt|;
name|this
operator|.
name|loadCountedFields
operator|=
name|loadCountedFields
expr_stmt|;
block|}
comment|/**      * It is possible to get the token count in a search response.      */
annotation|@
name|Test
DECL|method|searchReturnsTokenCount
specifier|public
name|void
name|searchReturnsTokenCount
parameter_list|()
throws|throws
name|IOException
block|{
name|init
argument_list|()
expr_stmt|;
name|assertSearchReturns
argument_list|(
name|searchById
argument_list|(
literal|"single"
argument_list|)
argument_list|,
literal|"single"
argument_list|)
expr_stmt|;
name|assertSearchReturns
argument_list|(
name|searchById
argument_list|(
literal|"bulk1"
argument_list|)
argument_list|,
literal|"bulk1"
argument_list|)
expr_stmt|;
name|assertSearchReturns
argument_list|(
name|searchById
argument_list|(
literal|"bulk2"
argument_list|)
argument_list|,
literal|"bulk2"
argument_list|)
expr_stmt|;
name|assertSearchReturns
argument_list|(
name|searchById
argument_list|(
literal|"multi"
argument_list|)
argument_list|,
literal|"multi"
argument_list|)
expr_stmt|;
name|assertSearchReturns
argument_list|(
name|searchById
argument_list|(
literal|"multibulk1"
argument_list|)
argument_list|,
literal|"multibulk1"
argument_list|)
expr_stmt|;
name|assertSearchReturns
argument_list|(
name|searchById
argument_list|(
literal|"multibulk2"
argument_list|)
argument_list|,
literal|"multibulk2"
argument_list|)
expr_stmt|;
block|}
comment|/**      * It is possible to search by token count.      */
annotation|@
name|Test
DECL|method|searchByTokenCount
specifier|public
name|void
name|searchByTokenCount
parameter_list|()
throws|throws
name|IOException
block|{
name|init
argument_list|()
expr_stmt|;
name|assertSearchReturns
argument_list|(
name|searchByNumericRange
argument_list|(
literal|4
argument_list|,
literal|4
argument_list|)
operator|.
name|get
argument_list|()
argument_list|,
literal|"single"
argument_list|)
expr_stmt|;
name|assertSearchReturns
argument_list|(
name|searchByNumericRange
argument_list|(
literal|10
argument_list|,
literal|10
argument_list|)
operator|.
name|get
argument_list|()
argument_list|,
literal|"multibulk2"
argument_list|)
expr_stmt|;
name|assertSearchReturns
argument_list|(
name|searchByNumericRange
argument_list|(
literal|7
argument_list|,
literal|10
argument_list|)
operator|.
name|get
argument_list|()
argument_list|,
literal|"multi"
argument_list|,
literal|"multibulk1"
argument_list|,
literal|"multibulk2"
argument_list|)
expr_stmt|;
name|assertSearchReturns
argument_list|(
name|searchByNumericRange
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
operator|.
name|get
argument_list|()
argument_list|,
literal|"single"
argument_list|,
literal|"bulk1"
argument_list|,
literal|"bulk2"
argument_list|,
literal|"multi"
argument_list|,
literal|"multibulk1"
argument_list|,
literal|"multibulk2"
argument_list|)
expr_stmt|;
name|assertSearchReturns
argument_list|(
name|searchByNumericRange
argument_list|(
literal|12
argument_list|,
literal|12
argument_list|)
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**      * It is possible to search by token count.      */
annotation|@
name|Test
DECL|method|facetByTokenCount
specifier|public
name|void
name|facetByTokenCount
parameter_list|()
throws|throws
name|IOException
block|{
name|init
argument_list|()
expr_stmt|;
name|String
name|facetField
init|=
name|randomFrom
argument_list|(
name|ImmutableList
operator|.
name|of
argument_list|(
literal|"foo.token_count"
argument_list|,
literal|"foo.token_count_unstored"
argument_list|,
literal|"foo.token_count_with_doc_values"
argument_list|)
argument_list|)
decl_stmt|;
name|SearchResponse
name|result
init|=
name|searchByNumericRange
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
operator|.
name|addAggregation
argument_list|(
name|AggregationBuilders
operator|.
name|terms
argument_list|(
literal|"facet"
argument_list|)
operator|.
name|field
argument_list|(
name|facetField
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertSearchReturns
argument_list|(
name|result
argument_list|,
literal|"single"
argument_list|,
literal|"bulk1"
argument_list|,
literal|"bulk2"
argument_list|,
literal|"multi"
argument_list|,
literal|"multibulk1"
argument_list|,
literal|"multibulk2"
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|result
operator|.
name|getAggregations
argument_list|()
operator|.
name|asList
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
name|Terms
name|terms
init|=
operator|(
name|Terms
operator|)
name|result
operator|.
name|getAggregations
argument_list|()
operator|.
name|asList
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|terms
operator|.
name|getBuckets
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|9
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|init
specifier|private
name|void
name|init
parameter_list|()
throws|throws
name|IOException
block|{
name|prepareCreate
argument_list|(
literal|"test"
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"test"
argument_list|,
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"test"
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
literal|"multi_field"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"fields"
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
literal|"string"
argument_list|)
operator|.
name|field
argument_list|(
literal|"store"
argument_list|,
name|storeCountedFields
argument_list|)
operator|.
name|field
argument_list|(
literal|"analyzer"
argument_list|,
literal|"simple"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"token_count"
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
literal|"standard"
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
name|startObject
argument_list|(
literal|"token_count_unstored"
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
literal|"standard"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"token_count_with_doc_values"
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
literal|"standard"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"fielddata"
argument_list|)
operator|.
name|field
argument_list|(
literal|"format"
argument_list|,
literal|"doc_values"
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
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
name|prepareIndex
argument_list|(
literal|"single"
argument_list|,
literal|"I have four terms"
argument_list|)
operator|.
name|get
argument_list|()
operator|.
name|isCreated
argument_list|()
argument_list|)
expr_stmt|;
name|BulkResponse
name|bulk
init|=
name|client
argument_list|()
operator|.
name|prepareBulk
argument_list|()
operator|.
name|add
argument_list|(
name|prepareIndex
argument_list|(
literal|"bulk1"
argument_list|,
literal|"bulk three terms"
argument_list|)
argument_list|)
operator|.
name|add
argument_list|(
name|prepareIndex
argument_list|(
literal|"bulk2"
argument_list|,
literal|"this has five bulk terms"
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertFalse
argument_list|(
name|bulk
operator|.
name|buildFailureMessage
argument_list|()
argument_list|,
name|bulk
operator|.
name|hasFailures
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|prepareIndex
argument_list|(
literal|"multi"
argument_list|,
literal|"two terms"
argument_list|,
literal|"wow now I have seven lucky terms"
argument_list|)
operator|.
name|get
argument_list|()
operator|.
name|isCreated
argument_list|()
argument_list|)
expr_stmt|;
name|bulk
operator|=
name|client
argument_list|()
operator|.
name|prepareBulk
argument_list|()
operator|.
name|add
argument_list|(
name|prepareIndex
argument_list|(
literal|"multibulk1"
argument_list|,
literal|"one"
argument_list|,
literal|"oh wow now I have eight unlucky terms"
argument_list|)
argument_list|)
operator|.
name|add
argument_list|(
name|prepareIndex
argument_list|(
literal|"multibulk2"
argument_list|,
literal|"six is a bunch of terms"
argument_list|,
literal|"ten!  ten terms is just crazy!  too many too count!"
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|assertFalse
argument_list|(
name|bulk
operator|.
name|buildFailureMessage
argument_list|()
argument_list|,
name|bulk
operator|.
name|hasFailures
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|refresh
argument_list|()
operator|.
name|getFailedShards
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|prepareIndex
specifier|private
name|IndexRequestBuilder
name|prepareIndex
parameter_list|(
name|String
name|id
parameter_list|,
name|String
modifier|...
name|texts
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"test"
argument_list|,
name|id
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"foo"
argument_list|,
name|texts
argument_list|)
return|;
block|}
DECL|method|searchById
specifier|private
name|SearchResponse
name|searchById
parameter_list|(
name|String
name|id
parameter_list|)
block|{
return|return
name|prepareSearch
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
name|id
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
return|;
block|}
DECL|method|searchByNumericRange
specifier|private
name|SearchRequestBuilder
name|searchByNumericRange
parameter_list|(
name|int
name|low
parameter_list|,
name|int
name|high
parameter_list|)
block|{
return|return
name|prepareSearch
argument_list|()
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
name|rangeQuery
argument_list|(
name|randomFrom
argument_list|(
name|ImmutableList
operator|.
name|of
argument_list|(
literal|"foo.token_count"
argument_list|,
literal|"foo.token_count_unstored"
argument_list|,
literal|"foo.token_count_with_doc_values"
argument_list|)
argument_list|)
argument_list|)
operator|.
name|gte
argument_list|(
name|low
argument_list|)
operator|.
name|lte
argument_list|(
name|high
argument_list|)
argument_list|)
return|;
block|}
DECL|method|prepareSearch
specifier|private
name|SearchRequestBuilder
name|prepareSearch
parameter_list|()
block|{
name|SearchRequestBuilder
name|request
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
literal|"test"
argument_list|)
decl_stmt|;
name|request
operator|.
name|addField
argument_list|(
literal|"foo.token_count"
argument_list|)
expr_stmt|;
if|if
condition|(
name|loadCountedFields
condition|)
block|{
name|request
operator|.
name|addField
argument_list|(
literal|"foo"
argument_list|)
expr_stmt|;
block|}
return|return
name|request
return|;
block|}
DECL|method|assertSearchReturns
specifier|private
name|void
name|assertSearchReturns
parameter_list|(
name|SearchResponse
name|result
parameter_list|,
name|String
modifier|...
name|ids
parameter_list|)
block|{
name|assertThat
argument_list|(
name|result
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
name|ids
operator|.
name|length
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|result
operator|.
name|getHits
argument_list|()
operator|.
name|hits
argument_list|()
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
name|ids
operator|.
name|length
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|foundIds
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|SearchHit
name|hit
range|:
name|result
operator|.
name|getHits
argument_list|()
control|)
block|{
name|foundIds
operator|.
name|add
argument_list|(
name|hit
operator|.
name|id
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|assertThat
argument_list|(
name|foundIds
argument_list|,
name|containsInAnyOrder
argument_list|(
name|ids
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|SearchHit
name|hit
range|:
name|result
operator|.
name|getHits
argument_list|()
control|)
block|{
name|String
name|id
init|=
name|hit
operator|.
name|id
argument_list|()
decl_stmt|;
if|if
condition|(
name|id
operator|.
name|equals
argument_list|(
literal|"single"
argument_list|)
condition|)
block|{
name|assertSearchHit
argument_list|(
name|hit
argument_list|,
literal|4
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|id
operator|.
name|equals
argument_list|(
literal|"bulk1"
argument_list|)
condition|)
block|{
name|assertSearchHit
argument_list|(
name|hit
argument_list|,
literal|3
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|id
operator|.
name|equals
argument_list|(
literal|"bulk2"
argument_list|)
condition|)
block|{
name|assertSearchHit
argument_list|(
name|hit
argument_list|,
literal|5
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|id
operator|.
name|equals
argument_list|(
literal|"multi"
argument_list|)
condition|)
block|{
name|assertSearchHit
argument_list|(
name|hit
argument_list|,
literal|2
argument_list|,
literal|7
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|id
operator|.
name|equals
argument_list|(
literal|"multibulk1"
argument_list|)
condition|)
block|{
name|assertSearchHit
argument_list|(
name|hit
argument_list|,
literal|1
argument_list|,
literal|8
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|id
operator|.
name|equals
argument_list|(
literal|"multibulk2"
argument_list|)
condition|)
block|{
name|assertSearchHit
argument_list|(
name|hit
argument_list|,
literal|6
argument_list|,
literal|10
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|ElasticsearchException
argument_list|(
literal|"Unexpected response!"
argument_list|)
throw|;
block|}
block|}
block|}
DECL|method|assertSearchHit
specifier|private
name|void
name|assertSearchHit
parameter_list|(
name|SearchHit
name|hit
parameter_list|,
name|int
modifier|...
name|termCounts
parameter_list|)
block|{
name|assertThat
argument_list|(
name|hit
operator|.
name|field
argument_list|(
literal|"foo.token_count"
argument_list|)
argument_list|,
name|not
argument_list|(
name|nullValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|hit
operator|.
name|field
argument_list|(
literal|"foo.token_count"
argument_list|)
operator|.
name|values
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|termCounts
operator|.
name|length
argument_list|)
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
name|termCounts
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|assertThat
argument_list|(
operator|(
name|Integer
operator|)
name|hit
operator|.
name|field
argument_list|(
literal|"foo.token_count"
argument_list|)
operator|.
name|values
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|termCounts
index|[
name|i
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|loadCountedFields
operator|&&
name|storeCountedFields
condition|)
block|{
name|assertThat
argument_list|(
name|hit
operator|.
name|field
argument_list|(
literal|"foo"
argument_list|)
operator|.
name|values
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|termCounts
operator|.
name|length
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

