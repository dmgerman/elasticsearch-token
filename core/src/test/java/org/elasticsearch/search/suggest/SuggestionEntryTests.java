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
name|suggest
operator|.
name|Suggest
operator|.
name|Suggestion
operator|.
name|Entry
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
operator|.
name|Suggestion
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
name|phrase
operator|.
name|PhraseSuggestion
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
name|term
operator|.
name|TermSuggestion
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
name|Map
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
name|Function
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
import|import
name|java
operator|.
name|util
operator|.
name|function
operator|.
name|Supplier
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
name|common
operator|.
name|xcontent
operator|.
name|XContentParserUtils
operator|.
name|ensureExpectedToken
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
DECL|class|SuggestionEntryTests
specifier|public
class|class
name|SuggestionEntryTests
extends|extends
name|ESTestCase
block|{
DECL|field|ENTRY_PARSERS
specifier|private
specifier|static
specifier|final
name|Map
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|Entry
argument_list|>
argument_list|,
name|Function
argument_list|<
name|XContentParser
argument_list|,
name|?
extends|extends
name|Entry
argument_list|>
argument_list|>
name|ENTRY_PARSERS
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
static|static
block|{
name|ENTRY_PARSERS
operator|.
name|put
argument_list|(
name|TermSuggestion
operator|.
name|Entry
operator|.
name|class
argument_list|,
name|TermSuggestion
operator|.
name|Entry
operator|::
name|fromXContent
argument_list|)
expr_stmt|;
name|ENTRY_PARSERS
operator|.
name|put
argument_list|(
name|PhraseSuggestion
operator|.
name|Entry
operator|.
name|class
argument_list|,
name|PhraseSuggestion
operator|.
name|Entry
operator|::
name|fromXContent
argument_list|)
expr_stmt|;
name|ENTRY_PARSERS
operator|.
name|put
argument_list|(
name|CompletionSuggestion
operator|.
name|Entry
operator|.
name|class
argument_list|,
name|CompletionSuggestion
operator|.
name|Entry
operator|::
name|fromXContent
argument_list|)
expr_stmt|;
block|}
comment|/**      * Create a randomized Suggestion.Entry      */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|method|createTestItem
specifier|public
specifier|static
parameter_list|<
name|O
extends|extends
name|Option
parameter_list|>
name|Entry
argument_list|<
name|O
argument_list|>
name|createTestItem
parameter_list|(
name|Class
argument_list|<
name|?
extends|extends
name|Entry
argument_list|>
name|entryType
parameter_list|)
block|{
name|Text
name|entryText
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
name|offset
init|=
name|randomInt
argument_list|()
decl_stmt|;
name|int
name|length
init|=
name|randomInt
argument_list|()
decl_stmt|;
name|Entry
name|entry
decl_stmt|;
name|Supplier
argument_list|<
name|Option
argument_list|>
name|supplier
decl_stmt|;
if|if
condition|(
name|entryType
operator|==
name|TermSuggestion
operator|.
name|Entry
operator|.
name|class
condition|)
block|{
name|entry
operator|=
operator|new
name|TermSuggestion
operator|.
name|Entry
argument_list|(
name|entryText
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
expr_stmt|;
name|supplier
operator|=
name|TermSuggestionOptionTests
operator|::
name|createTestItem
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|entryType
operator|==
name|PhraseSuggestion
operator|.
name|Entry
operator|.
name|class
condition|)
block|{
name|entry
operator|=
operator|new
name|PhraseSuggestion
operator|.
name|Entry
argument_list|(
name|entryText
argument_list|,
name|offset
argument_list|,
name|length
argument_list|,
name|randomDouble
argument_list|()
argument_list|)
expr_stmt|;
name|supplier
operator|=
name|SuggestionOptionTests
operator|::
name|createTestItem
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|entryType
operator|==
name|CompletionSuggestion
operator|.
name|Entry
operator|.
name|class
condition|)
block|{
name|entry
operator|=
operator|new
name|CompletionSuggestion
operator|.
name|Entry
argument_list|(
name|entryText
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
expr_stmt|;
name|supplier
operator|=
name|CompletionSuggestionOptionTests
operator|::
name|createTestItem
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"entryType not supported ["
operator|+
name|entryType
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|int
name|numOptions
init|=
name|randomIntBetween
argument_list|(
literal|0
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
name|numOptions
condition|;
name|i
operator|++
control|)
block|{
name|entry
operator|.
name|addOption
argument_list|(
name|supplier
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|entry
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
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
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
for|for
control|(
name|Class
argument_list|<
name|?
extends|extends
name|Entry
argument_list|>
name|entryType
range|:
name|ENTRY_PARSERS
operator|.
name|keySet
argument_list|()
control|)
block|{
name|Entry
argument_list|<
name|Option
argument_list|>
name|entry
init|=
name|createTestItem
argument_list|(
name|entryType
argument_list|)
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
name|entry
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
name|Entry
argument_list|<
name|Option
argument_list|>
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
name|ensureExpectedToken
argument_list|(
name|XContentParser
operator|.
name|Token
operator|.
name|START_OBJECT
argument_list|,
name|parser
operator|.
name|nextToken
argument_list|()
argument_list|,
name|parser
operator|::
name|getTokenLocation
argument_list|)
expr_stmt|;
name|parsed
operator|=
name|ENTRY_PARSERS
operator|.
name|get
argument_list|(
name|entry
operator|.
name|getClass
argument_list|()
argument_list|)
operator|.
name|apply
argument_list|(
name|parser
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|XContentParser
operator|.
name|Token
operator|.
name|END_OBJECT
argument_list|,
name|parser
operator|.
name|currentToken
argument_list|()
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
name|entry
operator|.
name|getClass
argument_list|()
argument_list|,
name|parsed
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|entry
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
name|entry
operator|.
name|getLength
argument_list|()
argument_list|,
name|parsed
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|entry
operator|.
name|getOffset
argument_list|()
argument_list|,
name|parsed
operator|.
name|getOffset
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|entry
operator|.
name|getOptions
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|parsed
operator|.
name|getOptions
argument_list|()
operator|.
name|size
argument_list|()
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
name|entry
operator|.
name|getOptions
argument_list|()
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|assertEquals
argument_list|(
name|entry
operator|.
name|getOptions
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getClass
argument_list|()
argument_list|,
name|parsed
operator|.
name|getOptions
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
block|}
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
block|}
DECL|method|testToXContent
specifier|public
name|void
name|testToXContent
parameter_list|()
throws|throws
name|IOException
block|{
name|Option
name|option
init|=
operator|new
name|Option
argument_list|(
operator|new
name|Text
argument_list|(
literal|"someText"
argument_list|)
argument_list|,
operator|new
name|Text
argument_list|(
literal|"somethingHighlighted"
argument_list|)
argument_list|,
literal|1.3f
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|Entry
argument_list|<
name|Option
argument_list|>
name|entry
init|=
operator|new
name|Entry
argument_list|<>
argument_list|(
operator|new
name|Text
argument_list|(
literal|"entryText"
argument_list|)
argument_list|,
literal|42
argument_list|,
literal|313
argument_list|)
decl_stmt|;
name|entry
operator|.
name|addOption
argument_list|(
name|option
argument_list|)
expr_stmt|;
name|BytesReference
name|xContent
init|=
name|toXContent
argument_list|(
name|entry
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
literal|"{\"text\":\"entryText\","
operator|+
literal|"\"offset\":42,"
operator|+
literal|"\"length\":313,"
operator|+
literal|"\"options\":["
operator|+
literal|"{\"text\":\"someText\","
operator|+
literal|"\"highlighted\":\"somethingHighlighted\","
operator|+
literal|"\"score\":1.3,"
operator|+
literal|"\"collate_match\":true}"
operator|+
literal|"]}"
argument_list|,
name|xContent
operator|.
name|utf8ToString
argument_list|()
argument_list|)
expr_stmt|;
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|suggest
operator|.
name|term
operator|.
name|TermSuggestion
operator|.
name|Entry
operator|.
name|Option
name|termOption
init|=
operator|new
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|suggest
operator|.
name|term
operator|.
name|TermSuggestion
operator|.
name|Entry
operator|.
name|Option
argument_list|(
operator|new
name|Text
argument_list|(
literal|"termSuggestOption"
argument_list|)
argument_list|,
literal|42
argument_list|,
literal|3.13f
argument_list|)
decl_stmt|;
name|entry
operator|=
operator|new
name|Entry
argument_list|<>
argument_list|(
operator|new
name|Text
argument_list|(
literal|"entryText"
argument_list|)
argument_list|,
literal|42
argument_list|,
literal|313
argument_list|)
expr_stmt|;
name|entry
operator|.
name|addOption
argument_list|(
name|termOption
argument_list|)
expr_stmt|;
name|xContent
operator|=
name|toXContent
argument_list|(
name|entry
argument_list|,
name|XContentType
operator|.
name|JSON
argument_list|,
name|randomBoolean
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"{\"text\":\"entryText\","
operator|+
literal|"\"offset\":42,"
operator|+
literal|"\"length\":313,"
operator|+
literal|"\"options\":["
operator|+
literal|"{\"text\":\"termSuggestOption\","
operator|+
literal|"\"score\":3.13,"
operator|+
literal|"\"freq\":42}"
operator|+
literal|"]}"
argument_list|,
name|xContent
operator|.
name|utf8ToString
argument_list|()
argument_list|)
expr_stmt|;
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
name|completionOption
init|=
operator|new
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
argument_list|(
operator|-
literal|1
argument_list|,
operator|new
name|Text
argument_list|(
literal|"completionOption"
argument_list|)
argument_list|,
literal|3.13f
argument_list|,
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
argument_list|)
decl_stmt|;
name|entry
operator|=
operator|new
name|Entry
argument_list|<>
argument_list|(
operator|new
name|Text
argument_list|(
literal|"entryText"
argument_list|)
argument_list|,
literal|42
argument_list|,
literal|313
argument_list|)
expr_stmt|;
name|entry
operator|.
name|addOption
argument_list|(
name|completionOption
argument_list|)
expr_stmt|;
name|xContent
operator|=
name|toXContent
argument_list|(
name|entry
argument_list|,
name|XContentType
operator|.
name|JSON
argument_list|,
name|randomBoolean
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"{\"text\":\"entryText\","
operator|+
literal|"\"offset\":42,"
operator|+
literal|"\"length\":313,"
operator|+
literal|"\"options\":["
operator|+
literal|"{\"text\":\"completionOption\","
operator|+
literal|"\"score\":3.13,"
operator|+
literal|"\"contexts\":{\"key\":[\"value\"]}"
operator|+
literal|"}"
operator|+
literal|"]}"
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

