begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.suggest.completion
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|suggest
operator|.
name|completion
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
name|Collections
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
name|lessThanOrEqualTo
import|;
end_import

begin_class
DECL|class|CompletionSuggestionTests
specifier|public
class|class
name|CompletionSuggestionTests
extends|extends
name|ESTestCase
block|{
DECL|method|testToReduce
specifier|public
name|void
name|testToReduce
parameter_list|()
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|Suggest
operator|.
name|Suggestion
argument_list|<
name|CompletionSuggestion
operator|.
name|Entry
argument_list|>
argument_list|>
name|shardSuggestions
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|int
name|nShards
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|String
name|name
init|=
name|randomAsciiOfLength
argument_list|(
literal|10
argument_list|)
decl_stmt|;
name|int
name|size
init|=
name|randomIntBetween
argument_list|(
literal|3
argument_list|,
literal|5
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
name|nShards
condition|;
name|i
operator|++
control|)
block|{
name|CompletionSuggestion
name|suggestion
init|=
operator|new
name|CompletionSuggestion
argument_list|(
name|name
argument_list|,
name|size
argument_list|)
decl_stmt|;
name|suggestion
operator|.
name|addTerm
argument_list|(
operator|new
name|CompletionSuggestion
operator|.
name|Entry
argument_list|(
operator|new
name|Text
argument_list|(
literal|""
argument_list|)
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|shardSuggestions
operator|.
name|add
argument_list|(
name|suggestion
argument_list|)
expr_stmt|;
block|}
name|int
name|totalResults
init|=
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|5
argument_list|)
operator|*
name|nShards
decl_stmt|;
name|float
name|maxScore
init|=
name|randomIntBetween
argument_list|(
name|totalResults
argument_list|,
name|totalResults
operator|*
literal|2
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
name|totalResults
condition|;
name|i
operator|++
control|)
block|{
name|Suggest
operator|.
name|Suggestion
argument_list|<
name|CompletionSuggestion
operator|.
name|Entry
argument_list|>
name|suggestion
init|=
name|randomFrom
argument_list|(
name|shardSuggestions
argument_list|)
decl_stmt|;
name|suggestion
operator|.
name|getEntries
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|addOption
argument_list|(
operator|new
name|CompletionSuggestion
operator|.
name|Entry
operator|.
name|Option
argument_list|(
name|i
argument_list|,
operator|new
name|Text
argument_list|(
literal|""
argument_list|)
argument_list|,
name|maxScore
operator|-
name|i
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|CompletionSuggestion
name|reducedSuggestion
init|=
name|CompletionSuggestion
operator|.
name|reduceTo
argument_list|(
name|shardSuggestions
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|reducedSuggestion
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|reducedSuggestion
operator|.
name|getOptions
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|lessThanOrEqualTo
argument_list|(
name|size
argument_list|)
argument_list|)
expr_stmt|;
name|int
name|count
init|=
literal|0
decl_stmt|;
for|for
control|(
name|CompletionSuggestion
operator|.
name|Entry
operator|.
name|Option
name|option
range|:
name|reducedSuggestion
operator|.
name|getOptions
argument_list|()
control|)
block|{
name|assertThat
argument_list|(
name|option
operator|.
name|getDoc
argument_list|()
operator|.
name|doc
argument_list|,
name|equalTo
argument_list|(
name|count
argument_list|)
argument_list|)
expr_stmt|;
name|count
operator|++
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

