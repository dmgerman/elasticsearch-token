begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.functionscore
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|functionscore
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
name|ArrayUtil
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
name|functionscore
operator|.
name|random
operator|.
name|RandomScoreFunctionBuilder
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
name|test
operator|.
name|ElasticsearchIntegrationTest
import|;
end_import

begin_import
import|import
name|org
operator|.
name|hamcrest
operator|.
name|CoreMatchers
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Ignore
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
name|Comparator
import|;
end_import

begin_import
import|import static
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
operator|.
name|newArrayList
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
name|*
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
name|functionscore
operator|.
name|ScoreFunctionBuilders
operator|.
name|*
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
name|assertNoFailures
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
DECL|class|RandomScoreFunctionTests
specifier|public
class|class
name|RandomScoreFunctionTests
extends|extends
name|ElasticsearchIntegrationTest
block|{
annotation|@
name|Slow
DECL|method|testConsistentHitsWithSameSeed
specifier|public
name|void
name|testConsistentHitsWithSameSeed
parameter_list|()
throws|throws
name|Exception
block|{
name|createIndex
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
comment|// make sure we are done otherwise preference could change?
name|int
name|docCount
init|=
name|randomIntBetween
argument_list|(
literal|100
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
name|docCount
condition|;
name|i
operator|++
control|)
block|{
name|index
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
literal|""
operator|+
name|i
argument_list|,
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|endObject
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|flush
argument_list|()
expr_stmt|;
name|refresh
argument_list|()
expr_stmt|;
name|int
name|outerIters
init|=
name|scaledRandomIntBetween
argument_list|(
literal|10
argument_list|,
literal|20
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|o
init|=
literal|0
init|;
name|o
operator|<
name|outerIters
condition|;
name|o
operator|++
control|)
block|{
specifier|final
name|int
name|seed
init|=
name|randomInt
argument_list|()
decl_stmt|;
name|String
name|preference
init|=
name|randomRealisticUnicodeOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
decl_stmt|;
comment|// at least one char!!
comment|// randomPreference should not start with '_' (reserved for known preference types (e.g. _shards, _primary)
while|while
condition|(
name|preference
operator|.
name|startsWith
argument_list|(
literal|"_"
argument_list|)
condition|)
block|{
name|preference
operator|=
name|randomRealisticUnicodeOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
expr_stmt|;
block|}
name|int
name|innerIters
init|=
name|scaledRandomIntBetween
argument_list|(
literal|2
argument_list|,
literal|5
argument_list|)
decl_stmt|;
name|SearchHit
index|[]
name|hits
init|=
literal|null
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
name|innerIters
condition|;
name|i
operator|++
control|)
block|{
name|SearchResponse
name|searchResponse
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|()
operator|.
name|setSize
argument_list|(
name|docCount
argument_list|)
comment|// get all docs otherwise we are prone to tie-breaking
operator|.
name|setPreference
argument_list|(
name|preference
argument_list|)
operator|.
name|setQuery
argument_list|(
name|functionScoreQuery
argument_list|(
name|matchAllQuery
argument_list|()
argument_list|,
name|randomFunction
argument_list|(
name|seed
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
literal|"Failures "
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
name|CoreMatchers
operator|.
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
specifier|final
name|int
name|hitCount
init|=
name|searchResponse
operator|.
name|getHits
argument_list|()
operator|.
name|getHits
argument_list|()
operator|.
name|length
decl_stmt|;
specifier|final
name|SearchHit
index|[]
name|currentHits
init|=
name|searchResponse
operator|.
name|getHits
argument_list|()
operator|.
name|getHits
argument_list|()
decl_stmt|;
name|ArrayUtil
operator|.
name|timSort
argument_list|(
name|currentHits
argument_list|,
operator|new
name|Comparator
argument_list|<
name|SearchHit
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|int
name|compare
parameter_list|(
name|SearchHit
name|o1
parameter_list|,
name|SearchHit
name|o2
parameter_list|)
block|{
comment|// for tie-breaking we have to resort here since if the score is
comment|// identical we rely on collection order which might change.
name|int
name|cmp
init|=
name|Float
operator|.
name|compare
argument_list|(
name|o1
operator|.
name|getScore
argument_list|()
argument_list|,
name|o2
operator|.
name|getScore
argument_list|()
argument_list|)
decl_stmt|;
return|return
name|cmp
operator|==
literal|0
condition|?
name|o1
operator|.
name|getId
argument_list|()
operator|.
name|compareTo
argument_list|(
name|o2
operator|.
name|getId
argument_list|()
argument_list|)
else|:
name|cmp
return|;
block|}
block|}
argument_list|)
expr_stmt|;
if|if
condition|(
name|i
operator|==
literal|0
condition|)
block|{
name|assertThat
argument_list|(
name|hits
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|hits
operator|=
name|currentHits
expr_stmt|;
block|}
else|else
block|{
name|assertThat
argument_list|(
name|hits
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
name|searchResponse
operator|.
name|getHits
argument_list|()
operator|.
name|getHits
argument_list|()
operator|.
name|length
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|hitCount
condition|;
name|j
operator|++
control|)
block|{
name|assertThat
argument_list|(
literal|""
operator|+
name|j
argument_list|,
name|currentHits
index|[
name|j
index|]
operator|.
name|score
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|hits
index|[
name|j
index|]
operator|.
name|score
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|""
operator|+
name|j
argument_list|,
name|currentHits
index|[
name|j
index|]
operator|.
name|id
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|hits
index|[
name|j
index|]
operator|.
name|id
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|// randomly change some docs to get them in different segments
name|int
name|numDocsToChange
init|=
name|randomIntBetween
argument_list|(
literal|20
argument_list|,
literal|50
argument_list|)
decl_stmt|;
while|while
condition|(
name|numDocsToChange
operator|>
literal|0
condition|)
block|{
name|int
name|doc
init|=
name|randomInt
argument_list|(
name|docCount
operator|-
literal|1
argument_list|)
decl_stmt|;
comment|// watch out this is inclusive the max values!
name|index
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
literal|""
operator|+
name|doc
argument_list|,
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|endObject
argument_list|()
argument_list|)
expr_stmt|;
operator|--
name|numDocsToChange
expr_stmt|;
block|}
name|flush
argument_list|()
expr_stmt|;
name|refresh
argument_list|()
expr_stmt|;
block|}
block|}
block|}
DECL|method|testScoreAccessWithinScript
specifier|public
name|void
name|testScoreAccessWithinScript
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
literal|"type"
argument_list|,
literal|"body"
argument_list|,
literal|"type=string"
argument_list|,
literal|"index"
argument_list|,
literal|"type="
operator|+
name|randomFrom
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"short"
block|,
literal|"float"
block|,
literal|"long"
block|,
literal|"integer"
block|,
literal|"double"
block|}
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|ensureYellow
argument_list|()
expr_stmt|;
name|int
name|docCount
init|=
name|randomIntBetween
argument_list|(
literal|100
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
name|docCount
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
literal|"type"
argument_list|,
literal|""
operator|+
name|i
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"body"
argument_list|,
name|randomFrom
argument_list|(
name|newArrayList
argument_list|(
literal|"foo"
argument_list|,
literal|"bar"
argument_list|,
literal|"baz"
argument_list|)
argument_list|)
argument_list|,
literal|"index"
argument_list|,
name|i
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
name|refresh
argument_list|()
expr_stmt|;
comment|// Test for accessing _score
name|SearchResponse
name|resp
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|functionScoreQuery
argument_list|(
name|matchQuery
argument_list|(
literal|"body"
argument_list|,
literal|"foo"
argument_list|)
argument_list|)
operator|.
name|add
argument_list|(
name|fieldValueFactorFunction
argument_list|(
literal|"index"
argument_list|)
operator|.
name|factor
argument_list|(
literal|2
argument_list|)
argument_list|)
operator|.
name|add
argument_list|(
name|scriptFunction
argument_list|(
literal|"log(doc['index'].value + (factor * _score))"
argument_list|)
operator|.
name|param
argument_list|(
literal|"factor"
argument_list|,
name|randomIntBetween
argument_list|(
literal|2
argument_list|,
literal|4
argument_list|)
argument_list|)
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertNoFailures
argument_list|(
name|resp
argument_list|)
expr_stmt|;
name|SearchHit
name|firstHit
init|=
name|resp
operator|.
name|getHits
argument_list|()
operator|.
name|getAt
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|firstHit
operator|.
name|getScore
argument_list|()
argument_list|,
name|greaterThan
argument_list|(
literal|1f
argument_list|)
argument_list|)
expr_stmt|;
comment|// Test for accessing _score.intValue()
name|resp
operator|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|functionScoreQuery
argument_list|(
name|matchQuery
argument_list|(
literal|"body"
argument_list|,
literal|"foo"
argument_list|)
argument_list|)
operator|.
name|add
argument_list|(
name|fieldValueFactorFunction
argument_list|(
literal|"index"
argument_list|)
operator|.
name|factor
argument_list|(
literal|2
argument_list|)
argument_list|)
operator|.
name|add
argument_list|(
name|scriptFunction
argument_list|(
literal|"log(doc['index'].value + (factor * _score.intValue()))"
argument_list|)
operator|.
name|param
argument_list|(
literal|"factor"
argument_list|,
name|randomIntBetween
argument_list|(
literal|2
argument_list|,
literal|4
argument_list|)
argument_list|)
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|assertNoFailures
argument_list|(
name|resp
argument_list|)
expr_stmt|;
name|firstHit
operator|=
name|resp
operator|.
name|getHits
argument_list|()
operator|.
name|getAt
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|firstHit
operator|.
name|getScore
argument_list|()
argument_list|,
name|greaterThan
argument_list|(
literal|1f
argument_list|)
argument_list|)
expr_stmt|;
comment|// Test for accessing _score.longValue()
name|resp
operator|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|functionScoreQuery
argument_list|(
name|matchQuery
argument_list|(
literal|"body"
argument_list|,
literal|"foo"
argument_list|)
argument_list|)
operator|.
name|add
argument_list|(
name|fieldValueFactorFunction
argument_list|(
literal|"index"
argument_list|)
operator|.
name|factor
argument_list|(
literal|2
argument_list|)
argument_list|)
operator|.
name|add
argument_list|(
name|scriptFunction
argument_list|(
literal|"log(doc['index'].value + (factor * _score.longValue()))"
argument_list|)
operator|.
name|param
argument_list|(
literal|"factor"
argument_list|,
name|randomIntBetween
argument_list|(
literal|2
argument_list|,
literal|4
argument_list|)
argument_list|)
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|assertNoFailures
argument_list|(
name|resp
argument_list|)
expr_stmt|;
name|firstHit
operator|=
name|resp
operator|.
name|getHits
argument_list|()
operator|.
name|getAt
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|firstHit
operator|.
name|getScore
argument_list|()
argument_list|,
name|greaterThan
argument_list|(
literal|1f
argument_list|)
argument_list|)
expr_stmt|;
comment|// Test for accessing _score.floatValue()
name|resp
operator|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|functionScoreQuery
argument_list|(
name|matchQuery
argument_list|(
literal|"body"
argument_list|,
literal|"foo"
argument_list|)
argument_list|)
operator|.
name|add
argument_list|(
name|fieldValueFactorFunction
argument_list|(
literal|"index"
argument_list|)
operator|.
name|factor
argument_list|(
literal|2
argument_list|)
argument_list|)
operator|.
name|add
argument_list|(
name|scriptFunction
argument_list|(
literal|"log(doc['index'].value + (factor * _score.floatValue()))"
argument_list|)
operator|.
name|param
argument_list|(
literal|"factor"
argument_list|,
name|randomIntBetween
argument_list|(
literal|2
argument_list|,
literal|4
argument_list|)
argument_list|)
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|assertNoFailures
argument_list|(
name|resp
argument_list|)
expr_stmt|;
name|firstHit
operator|=
name|resp
operator|.
name|getHits
argument_list|()
operator|.
name|getAt
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|firstHit
operator|.
name|getScore
argument_list|()
argument_list|,
name|greaterThan
argument_list|(
literal|1f
argument_list|)
argument_list|)
expr_stmt|;
comment|// Test for accessing _score.doubleValue()
name|resp
operator|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|functionScoreQuery
argument_list|(
name|matchQuery
argument_list|(
literal|"body"
argument_list|,
literal|"foo"
argument_list|)
argument_list|)
operator|.
name|add
argument_list|(
name|fieldValueFactorFunction
argument_list|(
literal|"index"
argument_list|)
operator|.
name|factor
argument_list|(
literal|2
argument_list|)
argument_list|)
operator|.
name|add
argument_list|(
name|scriptFunction
argument_list|(
literal|"log(doc['index'].value + (factor * _score.doubleValue()))"
argument_list|)
operator|.
name|param
argument_list|(
literal|"factor"
argument_list|,
name|randomIntBetween
argument_list|(
literal|2
argument_list|,
literal|4
argument_list|)
argument_list|)
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|assertNoFailures
argument_list|(
name|resp
argument_list|)
expr_stmt|;
name|firstHit
operator|=
name|resp
operator|.
name|getHits
argument_list|()
operator|.
name|getAt
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|firstHit
operator|.
name|getScore
argument_list|()
argument_list|,
name|greaterThan
argument_list|(
literal|1f
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testSeedReportedInExplain
specifier|public
name|void
name|testSeedReportedInExplain
parameter_list|()
throws|throws
name|Exception
block|{
name|createIndex
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
name|index
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
literal|"1"
argument_list|,
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|endObject
argument_list|()
argument_list|)
expr_stmt|;
name|flush
argument_list|()
expr_stmt|;
name|refresh
argument_list|()
expr_stmt|;
name|int
name|seed
init|=
literal|12345678
decl_stmt|;
name|SearchResponse
name|resp
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|functionScoreQuery
argument_list|(
name|matchAllQuery
argument_list|()
argument_list|,
name|randomFunction
argument_list|(
name|seed
argument_list|)
argument_list|)
argument_list|)
operator|.
name|setExplain
argument_list|(
literal|true
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertNoFailures
argument_list|(
name|resp
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|resp
operator|.
name|getHits
argument_list|()
operator|.
name|totalHits
argument_list|()
argument_list|)
expr_stmt|;
name|SearchHit
name|firstHit
init|=
name|resp
operator|.
name|getHits
argument_list|()
operator|.
name|getAt
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|firstHit
operator|.
name|explanation
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|""
operator|+
name|seed
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testNoDocs
specifier|public
name|void
name|testNoDocs
parameter_list|()
throws|throws
name|Exception
block|{
name|createIndex
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
name|SearchResponse
name|resp
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|functionScoreQuery
argument_list|(
name|matchAllQuery
argument_list|()
argument_list|,
name|randomFunction
argument_list|(
literal|1234
argument_list|)
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertNoFailures
argument_list|(
name|resp
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|resp
operator|.
name|getHits
argument_list|()
operator|.
name|totalHits
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testScoreRange
specifier|public
name|void
name|testScoreRange
parameter_list|()
throws|throws
name|Exception
block|{
comment|// all random scores should be in range [0.0, 1.0]
name|createIndex
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
name|int
name|docCount
init|=
name|randomIntBetween
argument_list|(
literal|100
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
name|docCount
condition|;
name|i
operator|++
control|)
block|{
name|String
name|id
init|=
name|randomRealisticUnicodeOfCodepointLengthBetween
argument_list|(
literal|1
argument_list|,
literal|50
argument_list|)
decl_stmt|;
name|index
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
name|id
argument_list|,
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|endObject
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|flush
argument_list|()
expr_stmt|;
name|refresh
argument_list|()
expr_stmt|;
name|int
name|iters
init|=
name|scaledRandomIntBetween
argument_list|(
literal|10
argument_list|,
literal|20
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
operator|++
name|i
control|)
block|{
name|int
name|seed
init|=
name|randomInt
argument_list|()
decl_stmt|;
name|SearchResponse
name|searchResponse
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|()
operator|.
name|setQuery
argument_list|(
name|functionScoreQuery
argument_list|(
name|matchAllQuery
argument_list|()
argument_list|,
name|randomFunction
argument_list|(
name|seed
argument_list|)
argument_list|)
argument_list|)
operator|.
name|setSize
argument_list|(
name|docCount
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|assertNoFailures
argument_list|(
name|searchResponse
argument_list|)
expr_stmt|;
for|for
control|(
name|SearchHit
name|hit
range|:
name|searchResponse
operator|.
name|getHits
argument_list|()
operator|.
name|getHits
argument_list|()
control|)
block|{
name|assertThat
argument_list|(
name|hit
operator|.
name|score
argument_list|()
argument_list|,
name|allOf
argument_list|(
name|greaterThanOrEqualTo
argument_list|(
literal|0.0f
argument_list|)
argument_list|,
name|lessThanOrEqualTo
argument_list|(
literal|1.0f
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|testSeeds
specifier|public
name|void
name|testSeeds
parameter_list|()
throws|throws
name|Exception
block|{
name|createIndex
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
specifier|final
name|int
name|docCount
init|=
name|randomIntBetween
argument_list|(
literal|100
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
name|docCount
condition|;
name|i
operator|++
control|)
block|{
name|index
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
literal|""
operator|+
name|i
argument_list|,
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|endObject
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|flushAndRefresh
argument_list|()
expr_stmt|;
name|assertNoFailures
argument_list|(
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|()
operator|.
name|setSize
argument_list|(
name|docCount
argument_list|)
comment|// get all docs otherwise we are prone to tie-breaking
operator|.
name|setQuery
argument_list|(
name|functionScoreQuery
argument_list|(
name|matchAllQuery
argument_list|()
argument_list|,
name|randomFunction
argument_list|(
name|randomInt
argument_list|()
argument_list|)
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
argument_list|)
expr_stmt|;
name|assertNoFailures
argument_list|(
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|()
operator|.
name|setSize
argument_list|(
name|docCount
argument_list|)
comment|// get all docs otherwise we are prone to tie-breaking
operator|.
name|setQuery
argument_list|(
name|functionScoreQuery
argument_list|(
name|matchAllQuery
argument_list|()
argument_list|,
name|randomFunction
argument_list|(
name|randomLong
argument_list|()
argument_list|)
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
argument_list|)
expr_stmt|;
name|assertNoFailures
argument_list|(
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|()
operator|.
name|setSize
argument_list|(
name|docCount
argument_list|)
comment|// get all docs otherwise we are prone to tie-breaking
operator|.
name|setQuery
argument_list|(
name|functionScoreQuery
argument_list|(
name|matchAllQuery
argument_list|()
argument_list|,
name|randomFunction
argument_list|(
name|randomRealisticUnicodeOfLengthBetween
argument_list|(
literal|10
argument_list|,
literal|20
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
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Ignore
DECL|method|checkDistribution
specifier|public
name|void
name|checkDistribution
parameter_list|()
throws|throws
name|Exception
block|{
name|int
name|count
init|=
literal|10000
decl_stmt|;
name|assertAcked
argument_list|(
name|prepareCreate
argument_list|(
literal|"test"
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
name|count
condition|;
name|i
operator|++
control|)
block|{
name|index
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
literal|""
operator|+
name|i
argument_list|,
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|endObject
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|flush
argument_list|()
expr_stmt|;
name|refresh
argument_list|()
expr_stmt|;
name|int
index|[]
name|matrix
init|=
operator|new
name|int
index|[
name|count
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
name|count
condition|;
name|i
operator|++
control|)
block|{
name|SearchResponse
name|searchResponse
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|()
operator|.
name|setQuery
argument_list|(
name|functionScoreQuery
argument_list|(
name|matchAllQuery
argument_list|()
argument_list|,
operator|new
name|RandomScoreFunctionBuilder
argument_list|()
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|matrix
index|[
name|Integer
operator|.
name|valueOf
argument_list|(
name|searchResponse
operator|.
name|getHits
argument_list|()
operator|.
name|getAt
argument_list|(
literal|0
argument_list|)
operator|.
name|id
argument_list|()
argument_list|)
index|]
operator|++
expr_stmt|;
block|}
name|int
name|filled
init|=
literal|0
decl_stmt|;
name|int
name|maxRepeat
init|=
literal|0
decl_stmt|;
name|int
name|sumRepeat
init|=
literal|0
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
name|matrix
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|int
name|value
init|=
name|matrix
index|[
name|i
index|]
decl_stmt|;
name|sumRepeat
operator|+=
name|value
expr_stmt|;
name|maxRepeat
operator|=
name|Math
operator|.
name|max
argument_list|(
name|maxRepeat
argument_list|,
name|value
argument_list|)
expr_stmt|;
if|if
condition|(
name|value
operator|>
literal|0
condition|)
block|{
name|filled
operator|++
expr_stmt|;
block|}
block|}
name|System
operator|.
name|out
operator|.
name|println
argument_list|()
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"max repeat: "
operator|+
name|maxRepeat
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"avg repeat: "
operator|+
name|sumRepeat
operator|/
operator|(
name|double
operator|)
name|filled
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"distribution: "
operator|+
name|filled
operator|/
operator|(
name|double
operator|)
name|count
argument_list|)
expr_stmt|;
name|int
name|percentile50
init|=
name|filled
operator|/
literal|2
decl_stmt|;
name|int
name|percentile25
init|=
operator|(
name|filled
operator|/
literal|4
operator|)
decl_stmt|;
name|int
name|percentile75
init|=
name|percentile50
operator|+
name|percentile25
decl_stmt|;
name|int
name|sum
init|=
literal|0
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
name|matrix
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|matrix
index|[
name|i
index|]
operator|==
literal|0
condition|)
block|{
continue|continue;
block|}
name|sum
operator|+=
name|i
operator|*
name|matrix
index|[
name|i
index|]
expr_stmt|;
if|if
condition|(
name|percentile50
operator|==
literal|0
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"median: "
operator|+
name|i
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|percentile25
operator|==
literal|0
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"percentile_25: "
operator|+
name|i
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|percentile75
operator|==
literal|0
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"percentile_75: "
operator|+
name|i
argument_list|)
expr_stmt|;
block|}
name|percentile50
operator|--
expr_stmt|;
name|percentile25
operator|--
expr_stmt|;
name|percentile75
operator|--
expr_stmt|;
block|}
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"mean: "
operator|+
name|sum
operator|/
operator|(
name|double
operator|)
name|count
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

