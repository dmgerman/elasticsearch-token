begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.suggest
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|suggest
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
name|bytes
operator|.
name|BytesReference
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
name|text
operator|.
name|Text
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
name|ToXContent
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
name|SearchHitTests
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
name|completion
operator|.
name|CompletionSuggestion
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
name|completion
operator|.
name|CompletionSuggestion
operator|.
name|Entry
operator|.
name|Option
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
name|ESTestCase
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
name|HashSet
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
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|function
operator|.
name|Predicate
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
name|XContentHelper
operator|.
name|toXContent
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
name|XContentTestUtils
operator|.
name|insertRandomFields
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
name|assertToXContentEquivalent
import|;
end_import

begin_class
DECL|class|CompletionSuggestionOptionTests
specifier|public
class|class
name|CompletionSuggestionOptionTests
extends|extends
name|ESTestCase
block|{
DECL|method|createTestItem
specifier|public
specifier|static
name|Option
name|createTestItem
parameter_list|()
block|{
name|Text
name|text
init|=
operator|new
name|Text
argument_list|(
name|randomAlphaOfLengthBetween
argument_list|(
literal|5
argument_list|,
literal|15
argument_list|)
argument_list|)
decl_stmt|;
name|int
name|docId
init|=
name|randomInt
argument_list|()
decl_stmt|;
name|int
name|numberOfContexts
init|=
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|3
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Set
argument_list|<
name|CharSequence
argument_list|>
argument_list|>
name|contexts
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
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
name|numberOfContexts
condition|;
name|i
operator|++
control|)
block|{
name|int
name|numberOfValues
init|=
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|3
argument_list|)
decl_stmt|;
name|Set
argument_list|<
name|CharSequence
argument_list|>
name|values
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|v
init|=
literal|0
init|;
name|v
operator|<
name|numberOfValues
condition|;
name|v
operator|++
control|)
block|{
name|values
operator|.
name|add
argument_list|(
name|randomAlphaOfLengthBetween
argument_list|(
literal|5
argument_list|,
literal|15
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|contexts
operator|.
name|put
argument_list|(
name|randomAlphaOfLengthBetween
argument_list|(
literal|5
argument_list|,
literal|15
argument_list|)
argument_list|,
name|values
argument_list|)
expr_stmt|;
block|}
name|SearchHit
name|hit
init|=
literal|null
decl_stmt|;
name|float
name|score
init|=
name|randomFloat
argument_list|()
decl_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|hit
operator|=
name|SearchHitTests
operator|.
name|createTestItem
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|score
operator|=
name|hit
operator|.
name|getScore
argument_list|()
expr_stmt|;
block|}
name|Option
name|option
init|=
operator|new
name|CompletionSuggestion
operator|.
name|Entry
operator|.
name|Option
argument_list|(
name|docId
argument_list|,
name|text
argument_list|,
name|score
argument_list|,
name|contexts
argument_list|)
decl_stmt|;
name|option
operator|.
name|setHit
argument_list|(
name|hit
argument_list|)
expr_stmt|;
return|return
name|option
return|;
block|}
DECL|method|testFromXContent
specifier|public
name|void
name|testFromXContent
parameter_list|()
throws|throws
name|IOException
block|{
name|doTestFromXContent
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
DECL|method|testFromXContentWithRandomFields
specifier|public
name|void
name|testFromXContentWithRandomFields
parameter_list|()
throws|throws
name|IOException
block|{
name|doTestFromXContent
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
DECL|method|doTestFromXContent
specifier|private
name|void
name|doTestFromXContent
parameter_list|(
name|boolean
name|addRandomFields
parameter_list|)
throws|throws
name|IOException
block|{
name|Option
name|option
init|=
name|createTestItem
argument_list|()
decl_stmt|;
name|XContentType
name|xContentType
init|=
name|randomFrom
argument_list|(
name|XContentType
operator|.
name|values
argument_list|()
argument_list|)
decl_stmt|;
name|boolean
name|humanReadable
init|=
name|randomBoolean
argument_list|()
decl_stmt|;
name|BytesReference
name|originalBytes
init|=
name|toShuffledXContent
argument_list|(
name|option
argument_list|,
name|xContentType
argument_list|,
name|ToXContent
operator|.
name|EMPTY_PARAMS
argument_list|,
name|humanReadable
argument_list|)
decl_stmt|;
name|BytesReference
name|mutated
decl_stmt|;
if|if
condition|(
name|addRandomFields
condition|)
block|{
comment|// "contexts" is an object consisting of key/array pairs, we shouldn't add anything random there
comment|// also there can be inner search hits fields inside this option, we need to exclude another couple of paths
comment|// where we cannot add random stuff
name|Predicate
argument_list|<
name|String
argument_list|>
name|excludeFilter
init|=
parameter_list|(
name|path
parameter_list|)
lambda|->
operator|(
name|path
operator|.
name|endsWith
argument_list|(
name|CompletionSuggestion
operator|.
name|Entry
operator|.
name|Option
operator|.
name|CONTEXTS
operator|.
name|getPreferredName
argument_list|()
argument_list|)
operator|||
name|path
operator|.
name|endsWith
argument_list|(
literal|"highlight"
argument_list|)
operator|||
name|path
operator|.
name|endsWith
argument_list|(
literal|"fields"
argument_list|)
operator|||
name|path
operator|.
name|contains
argument_list|(
literal|"_source"
argument_list|)
operator|||
name|path
operator|.
name|contains
argument_list|(
literal|"inner_hits"
argument_list|)
operator|)
decl_stmt|;
name|mutated
operator|=
name|insertRandomFields
argument_list|(
name|xContentType
argument_list|,
name|originalBytes
argument_list|,
name|excludeFilter
argument_list|,
name|random
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|mutated
operator|=
name|originalBytes
expr_stmt|;
block|}
name|Option
name|parsed
decl_stmt|;
try|try
init|(
name|XContentParser
name|parser
init|=
name|createParser
argument_list|(
name|xContentType
operator|.
name|xContent
argument_list|()
argument_list|,
name|mutated
argument_list|)
init|)
block|{
name|parsed
operator|=
name|Option
operator|.
name|fromXContent
argument_list|(
name|parser
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|parser
operator|.
name|nextToken
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|option
operator|.
name|getText
argument_list|()
argument_list|,
name|parsed
operator|.
name|getText
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|option
operator|.
name|getHighlighted
argument_list|()
argument_list|,
name|parsed
operator|.
name|getHighlighted
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|option
operator|.
name|getScore
argument_list|()
argument_list|,
name|parsed
operator|.
name|getScore
argument_list|()
argument_list|,
name|Float
operator|.
name|MIN_VALUE
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|option
operator|.
name|collateMatch
argument_list|()
argument_list|,
name|parsed
operator|.
name|collateMatch
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|option
operator|.
name|getContexts
argument_list|()
argument_list|,
name|parsed
operator|.
name|getContexts
argument_list|()
argument_list|)
expr_stmt|;
name|assertToXContentEquivalent
argument_list|(
name|originalBytes
argument_list|,
name|toXContent
argument_list|(
name|parsed
argument_list|,
name|xContentType
argument_list|,
name|humanReadable
argument_list|)
argument_list|,
name|xContentType
argument_list|)
expr_stmt|;
block|}
DECL|method|testToXContent
specifier|public
name|void
name|testToXContent
parameter_list|()
throws|throws
name|IOException
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Set
argument_list|<
name|CharSequence
argument_list|>
argument_list|>
name|contexts
init|=
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"key"
argument_list|,
name|Collections
operator|.
name|singleton
argument_list|(
literal|"value"
argument_list|)
argument_list|)
decl_stmt|;
name|CompletionSuggestion
operator|.
name|Entry
operator|.
name|Option
name|option
init|=
operator|new
name|CompletionSuggestion
operator|.
name|Entry
operator|.
name|Option
argument_list|(
literal|1
argument_list|,
operator|new
name|Text
argument_list|(
literal|"someText"
argument_list|)
argument_list|,
literal|1.3f
argument_list|,
name|contexts
argument_list|)
decl_stmt|;
name|BytesReference
name|xContent
init|=
name|toXContent
argument_list|(
name|option
argument_list|,
name|XContentType
operator|.
name|JSON
argument_list|,
name|randomBoolean
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"{\"text\":\"someText\",\"score\":1.3,\"contexts\":{\"key\":[\"value\"]}}"
argument_list|,
name|xContent
operator|.
name|utf8ToString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

