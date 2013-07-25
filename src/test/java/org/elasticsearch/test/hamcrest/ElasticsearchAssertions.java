begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.test.hamcrest
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|hamcrest
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
name|search
operator|.
name|BooleanQuery
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
name|Query
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticSearchException
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
name|ActionFuture
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
name|ActionRequestBuilder
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
name|support
operator|.
name|broadcast
operator|.
name|BroadcastOperationResponse
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
name|suggest
operator|.
name|Suggest
import|;
end_import

begin_import
import|import
name|org
operator|.
name|hamcrest
operator|.
name|Matcher
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
name|HashSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|MatcherAssert
operator|.
name|assertThat
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

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|ElasticsearchAssertions
specifier|public
class|class
name|ElasticsearchAssertions
block|{
comment|/*      * assertions      */
DECL|method|assertHitCount
specifier|public
specifier|static
name|void
name|assertHitCount
parameter_list|(
name|SearchResponse
name|searchResponse
parameter_list|,
name|long
name|expectedHitCount
parameter_list|)
block|{
name|assertThat
argument_list|(
name|searchResponse
operator|.
name|getHits
argument_list|()
operator|.
name|totalHits
argument_list|()
argument_list|,
name|is
argument_list|(
name|expectedHitCount
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|assertSearchHits
specifier|public
specifier|static
name|void
name|assertSearchHits
parameter_list|(
name|SearchResponse
name|searchResponse
parameter_list|,
name|String
modifier|...
name|ids
parameter_list|)
block|{
name|assertThat
argument_list|(
literal|"Expected different hit count"
argument_list|,
name|searchResponse
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
name|Set
argument_list|<
name|String
argument_list|>
name|idsSet
init|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|ids
argument_list|)
argument_list|)
decl_stmt|;
for|for
control|(
name|SearchHit
name|hit
range|:
name|searchResponse
operator|.
name|getHits
argument_list|()
control|)
block|{
name|assertThat
argument_list|(
literal|"Expected id: "
operator|+
name|hit
operator|.
name|getId
argument_list|()
operator|+
literal|" in the result but wasn't"
argument_list|,
name|idsSet
operator|.
name|remove
argument_list|(
name|hit
operator|.
name|getId
argument_list|()
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|assertThat
argument_list|(
literal|"Expected ids: "
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|idsSet
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
literal|0
index|]
argument_list|)
argument_list|)
operator|+
literal|" in the result - result size differs"
argument_list|,
name|idsSet
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
DECL|method|assertHitCount
specifier|public
specifier|static
name|void
name|assertHitCount
parameter_list|(
name|CountResponse
name|countResponse
parameter_list|,
name|long
name|expectedHitCount
parameter_list|)
block|{
name|assertThat
argument_list|(
name|countResponse
operator|.
name|getCount
argument_list|()
argument_list|,
name|is
argument_list|(
name|expectedHitCount
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|assertFirstHit
specifier|public
specifier|static
name|void
name|assertFirstHit
parameter_list|(
name|SearchResponse
name|searchResponse
parameter_list|,
name|Matcher
argument_list|<
name|SearchHit
argument_list|>
name|matcher
parameter_list|)
block|{
name|assertSearchHit
argument_list|(
name|searchResponse
argument_list|,
literal|1
argument_list|,
name|matcher
argument_list|)
expr_stmt|;
block|}
DECL|method|assertSecondHit
specifier|public
specifier|static
name|void
name|assertSecondHit
parameter_list|(
name|SearchResponse
name|searchResponse
parameter_list|,
name|Matcher
argument_list|<
name|SearchHit
argument_list|>
name|matcher
parameter_list|)
block|{
name|assertSearchHit
argument_list|(
name|searchResponse
argument_list|,
literal|2
argument_list|,
name|matcher
argument_list|)
expr_stmt|;
block|}
DECL|method|assertThirdHit
specifier|public
specifier|static
name|void
name|assertThirdHit
parameter_list|(
name|SearchResponse
name|searchResponse
parameter_list|,
name|Matcher
argument_list|<
name|SearchHit
argument_list|>
name|matcher
parameter_list|)
block|{
name|assertSearchHit
argument_list|(
name|searchResponse
argument_list|,
literal|3
argument_list|,
name|matcher
argument_list|)
expr_stmt|;
block|}
DECL|method|assertSearchHit
specifier|public
specifier|static
name|void
name|assertSearchHit
parameter_list|(
name|SearchResponse
name|searchResponse
parameter_list|,
name|int
name|number
parameter_list|,
name|Matcher
argument_list|<
name|SearchHit
argument_list|>
name|matcher
parameter_list|)
block|{
assert|assert
name|number
operator|>
literal|0
assert|;
name|assertThat
argument_list|(
literal|"SearchHit number must be greater than 0"
argument_list|,
name|number
argument_list|,
name|greaterThan
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|searchResponse
operator|.
name|getHits
argument_list|()
operator|.
name|totalHits
argument_list|()
argument_list|,
name|greaterThanOrEqualTo
argument_list|(
operator|(
name|long
operator|)
name|number
argument_list|)
argument_list|)
expr_stmt|;
name|assertSearchHit
argument_list|(
name|searchResponse
operator|.
name|getHits
argument_list|()
operator|.
name|getAt
argument_list|(
name|number
operator|-
literal|1
argument_list|)
argument_list|,
name|matcher
argument_list|)
expr_stmt|;
block|}
DECL|method|assertNoFailures
specifier|public
specifier|static
name|void
name|assertNoFailures
parameter_list|(
name|SearchResponse
name|searchResponse
parameter_list|)
block|{
name|assertThat
argument_list|(
literal|"Unexpectd ShardFailures: "
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|searchResponse
operator|.
name|getShardFailures
argument_list|()
argument_list|)
argument_list|,
name|searchResponse
operator|.
name|getShardFailures
argument_list|()
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|assertNoFailures
specifier|public
specifier|static
name|void
name|assertNoFailures
parameter_list|(
name|BroadcastOperationResponse
name|response
parameter_list|)
block|{
name|assertThat
argument_list|(
literal|"Unexpectd ShardFailures: "
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|response
operator|.
name|getShardFailures
argument_list|()
argument_list|)
argument_list|,
name|response
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
DECL|method|assertSearchHit
specifier|public
specifier|static
name|void
name|assertSearchHit
parameter_list|(
name|SearchHit
name|searchHit
parameter_list|,
name|Matcher
argument_list|<
name|SearchHit
argument_list|>
name|matcher
parameter_list|)
block|{
name|assertThat
argument_list|(
name|searchHit
argument_list|,
name|matcher
argument_list|)
expr_stmt|;
block|}
DECL|method|assertHighlight
specifier|public
specifier|static
name|void
name|assertHighlight
parameter_list|(
name|SearchResponse
name|resp
parameter_list|,
name|int
name|hit
parameter_list|,
name|String
name|field
parameter_list|,
name|int
name|fragment
parameter_list|,
name|Matcher
argument_list|<
name|String
argument_list|>
name|matcher
parameter_list|)
block|{
name|assertNoFailures
argument_list|(
name|resp
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"not enough hits"
argument_list|,
name|resp
operator|.
name|getHits
argument_list|()
operator|.
name|hits
argument_list|()
operator|.
name|length
argument_list|,
name|greaterThan
argument_list|(
name|hit
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|resp
operator|.
name|getHits
argument_list|()
operator|.
name|hits
argument_list|()
index|[
name|hit
index|]
operator|.
name|getHighlightFields
argument_list|()
operator|.
name|get
argument_list|(
name|field
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|resp
operator|.
name|getHits
argument_list|()
operator|.
name|hits
argument_list|()
index|[
name|hit
index|]
operator|.
name|getHighlightFields
argument_list|()
operator|.
name|get
argument_list|(
name|field
argument_list|)
operator|.
name|fragments
argument_list|()
operator|.
name|length
argument_list|,
name|greaterThan
argument_list|(
name|fragment
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|resp
operator|.
name|getHits
argument_list|()
operator|.
name|hits
argument_list|()
index|[
name|hit
index|]
operator|.
name|highlightFields
argument_list|()
operator|.
name|get
argument_list|(
name|field
argument_list|)
operator|.
name|fragments
argument_list|()
index|[
name|fragment
index|]
operator|.
name|string
argument_list|()
argument_list|,
name|matcher
argument_list|)
expr_stmt|;
block|}
DECL|method|assertSuggestionSize
specifier|public
specifier|static
name|void
name|assertSuggestionSize
parameter_list|(
name|Suggest
name|searchSuggest
parameter_list|,
name|int
name|entry
parameter_list|,
name|int
name|size
parameter_list|,
name|String
name|key
parameter_list|)
block|{
name|assertThat
argument_list|(
name|searchSuggest
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|searchSuggest
operator|.
name|size
argument_list|()
argument_list|,
name|greaterThanOrEqualTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|searchSuggest
operator|.
name|getSuggestion
argument_list|(
name|key
argument_list|)
operator|.
name|getName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|key
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|searchSuggest
operator|.
name|getSuggestion
argument_list|(
name|key
argument_list|)
operator|.
name|getEntries
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|greaterThanOrEqualTo
argument_list|(
name|entry
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|searchSuggest
operator|.
name|getSuggestion
argument_list|(
name|key
argument_list|)
operator|.
name|getEntries
argument_list|()
operator|.
name|get
argument_list|(
name|entry
argument_list|)
operator|.
name|getOptions
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|size
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|assertSuggestion
specifier|public
specifier|static
name|void
name|assertSuggestion
parameter_list|(
name|Suggest
name|searchSuggest
parameter_list|,
name|int
name|entry
parameter_list|,
name|int
name|ord
parameter_list|,
name|String
name|key
parameter_list|,
name|String
name|text
parameter_list|)
block|{
name|assertThat
argument_list|(
name|searchSuggest
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|searchSuggest
operator|.
name|size
argument_list|()
argument_list|,
name|greaterThanOrEqualTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|searchSuggest
operator|.
name|getSuggestion
argument_list|(
name|key
argument_list|)
operator|.
name|getName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|key
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|searchSuggest
operator|.
name|getSuggestion
argument_list|(
name|key
argument_list|)
operator|.
name|getEntries
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|greaterThanOrEqualTo
argument_list|(
name|entry
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|searchSuggest
operator|.
name|getSuggestion
argument_list|(
name|key
argument_list|)
operator|.
name|getEntries
argument_list|()
operator|.
name|get
argument_list|(
name|entry
argument_list|)
operator|.
name|getOptions
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|greaterThan
argument_list|(
name|ord
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|searchSuggest
operator|.
name|getSuggestion
argument_list|(
name|key
argument_list|)
operator|.
name|getEntries
argument_list|()
operator|.
name|get
argument_list|(
name|entry
argument_list|)
operator|.
name|getOptions
argument_list|()
operator|.
name|get
argument_list|(
name|ord
argument_list|)
operator|.
name|getText
argument_list|()
operator|.
name|string
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|text
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/*      * matchers      */
DECL|method|hasId
specifier|public
specifier|static
name|Matcher
argument_list|<
name|SearchHit
argument_list|>
name|hasId
parameter_list|(
specifier|final
name|String
name|id
parameter_list|)
block|{
return|return
operator|new
name|ElasticsearchMatchers
operator|.
name|SearchHitHasIdMatcher
argument_list|(
name|id
argument_list|)
return|;
block|}
DECL|method|hasType
specifier|public
specifier|static
name|Matcher
argument_list|<
name|SearchHit
argument_list|>
name|hasType
parameter_list|(
specifier|final
name|String
name|type
parameter_list|)
block|{
return|return
operator|new
name|ElasticsearchMatchers
operator|.
name|SearchHitHasTypeMatcher
argument_list|(
name|type
argument_list|)
return|;
block|}
DECL|method|hasIndex
specifier|public
specifier|static
name|Matcher
argument_list|<
name|SearchHit
argument_list|>
name|hasIndex
parameter_list|(
specifier|final
name|String
name|index
parameter_list|)
block|{
return|return
operator|new
name|ElasticsearchMatchers
operator|.
name|SearchHitHasIndexMatcher
argument_list|(
name|index
argument_list|)
return|;
block|}
DECL|method|assertBooleanSubQuery
specifier|public
specifier|static
parameter_list|<
name|T
extends|extends
name|Query
parameter_list|>
name|T
name|assertBooleanSubQuery
parameter_list|(
name|Query
name|query
parameter_list|,
name|Class
argument_list|<
name|T
argument_list|>
name|subqueryType
parameter_list|,
name|int
name|i
parameter_list|)
block|{
name|assertThat
argument_list|(
name|query
argument_list|,
name|instanceOf
argument_list|(
name|BooleanQuery
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|BooleanQuery
name|q
init|=
operator|(
name|BooleanQuery
operator|)
name|query
decl_stmt|;
name|assertThat
argument_list|(
name|q
operator|.
name|getClauses
argument_list|()
operator|.
name|length
argument_list|,
name|greaterThan
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|q
operator|.
name|getClauses
argument_list|()
index|[
name|i
index|]
operator|.
name|getQuery
argument_list|()
argument_list|,
name|instanceOf
argument_list|(
name|subqueryType
argument_list|)
argument_list|)
expr_stmt|;
return|return
operator|(
name|T
operator|)
name|q
operator|.
name|getClauses
argument_list|()
index|[
name|i
index|]
operator|.
name|getQuery
argument_list|()
return|;
block|}
DECL|method|assertThrows
specifier|public
specifier|static
parameter_list|<
name|E
extends|extends
name|Throwable
parameter_list|>
name|void
name|assertThrows
parameter_list|(
name|ActionRequestBuilder
argument_list|<
name|?
argument_list|,
name|?
argument_list|,
name|?
argument_list|>
name|builder
parameter_list|,
name|Class
argument_list|<
name|E
argument_list|>
name|exceptionClass
parameter_list|)
block|{
name|assertThrows
argument_list|(
name|builder
operator|.
name|execute
argument_list|()
argument_list|,
name|exceptionClass
argument_list|)
expr_stmt|;
block|}
DECL|method|assertThrows
specifier|public
specifier|static
parameter_list|<
name|E
extends|extends
name|Throwable
parameter_list|>
name|void
name|assertThrows
parameter_list|(
name|ActionFuture
name|future
parameter_list|,
name|Class
argument_list|<
name|E
argument_list|>
name|exceptionClass
parameter_list|)
block|{
name|boolean
name|fail
init|=
literal|false
decl_stmt|;
try|try
block|{
name|future
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|fail
operator|=
literal|true
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ElasticSearchException
name|esException
parameter_list|)
block|{
name|assertThat
argument_list|(
name|esException
operator|.
name|unwrapCause
argument_list|()
argument_list|,
name|instanceOf
argument_list|(
name|exceptionClass
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|e
argument_list|,
name|instanceOf
argument_list|(
name|exceptionClass
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// has to be outside catch clause to get a proper message
if|if
condition|(
name|fail
condition|)
block|{
throw|throw
operator|new
name|AssertionError
argument_list|(
literal|"Expected a "
operator|+
name|exceptionClass
operator|+
literal|" exception to be thrown"
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

