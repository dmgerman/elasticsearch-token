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
name|io
operator|.
name|stream
operator|.
name|NamedWriteableRegistry
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
name|io
operator|.
name|stream
operator|.
name|StreamInput
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
name|settings
operator|.
name|Settings
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
name|XContentHelper
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
name|QueryParseContext
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
name|CompletionSuggesterBuilderTests
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
name|CompletionSuggestionBuilder
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
name|WritableTestCase
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
name|Laplace
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
name|LinearInterpolation
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
name|PhraseSuggestionBuilder
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
name|PhraseSuggestionBuilderTests
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
name|SmoothingModel
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
name|StupidBackoff
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
name|TermSuggestionBuilder
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
name|TermSuggestionBuilderTests
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|AfterClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|BeforeClass
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
name|Map
operator|.
name|Entry
import|;
end_import

begin_class
DECL|class|SuggestBuilderTests
specifier|public
class|class
name|SuggestBuilderTests
extends|extends
name|WritableTestCase
argument_list|<
name|SuggestBuilder
argument_list|>
block|{
DECL|field|namedWriteableRegistry
specifier|private
specifier|static
name|NamedWriteableRegistry
name|namedWriteableRegistry
decl_stmt|;
DECL|field|suggesters
specifier|private
specifier|static
name|Suggesters
name|suggesters
decl_stmt|;
comment|/**      * Setup for the whole base test class.      */
annotation|@
name|BeforeClass
DECL|method|init
specifier|public
specifier|static
name|void
name|init
parameter_list|()
block|{
name|namedWriteableRegistry
operator|=
operator|new
name|NamedWriteableRegistry
argument_list|()
expr_stmt|;
name|suggesters
operator|=
operator|new
name|Suggesters
argument_list|(
name|namedWriteableRegistry
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
DECL|method|afterClass
specifier|public
specifier|static
name|void
name|afterClass
parameter_list|()
block|{
name|namedWriteableRegistry
operator|=
literal|null
expr_stmt|;
name|suggesters
operator|=
literal|null
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|provideNamedWritableRegistry
specifier|protected
name|NamedWriteableRegistry
name|provideNamedWritableRegistry
parameter_list|()
block|{
return|return
name|namedWriteableRegistry
return|;
block|}
comment|/**      *  creates random suggestion builder, renders it to xContent and back to new instance that should be equal to original      */
DECL|method|testFromXContent
specifier|public
name|void
name|testFromXContent
parameter_list|()
throws|throws
name|IOException
block|{
name|QueryParseContext
name|context
init|=
operator|new
name|QueryParseContext
argument_list|(
literal|null
argument_list|)
decl_stmt|;
name|context
operator|.
name|parseFieldMatcher
argument_list|(
operator|new
name|ParseFieldMatcher
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|runs
init|=
literal|0
init|;
name|runs
operator|<
name|NUMBER_OF_RUNS
condition|;
name|runs
operator|++
control|)
block|{
name|SuggestBuilder
name|suggestBuilder
init|=
name|createTestModel
argument_list|()
decl_stmt|;
name|XContentBuilder
name|xContentBuilder
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
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|xContentBuilder
operator|.
name|prettyPrint
argument_list|()
expr_stmt|;
block|}
name|suggestBuilder
operator|.
name|toXContent
argument_list|(
name|xContentBuilder
argument_list|,
name|ToXContent
operator|.
name|EMPTY_PARAMS
argument_list|)
expr_stmt|;
name|XContentParser
name|parser
init|=
name|XContentHelper
operator|.
name|createParser
argument_list|(
name|xContentBuilder
operator|.
name|bytes
argument_list|()
argument_list|)
decl_stmt|;
name|context
operator|.
name|reset
argument_list|(
name|parser
argument_list|)
expr_stmt|;
name|SuggestBuilder
name|secondSuggestBuilder
init|=
name|SuggestBuilder
operator|.
name|fromXContent
argument_list|(
name|context
argument_list|,
name|suggesters
argument_list|)
decl_stmt|;
name|assertNotSame
argument_list|(
name|suggestBuilder
argument_list|,
name|secondSuggestBuilder
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|suggestBuilder
argument_list|,
name|secondSuggestBuilder
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|suggestBuilder
operator|.
name|hashCode
argument_list|()
argument_list|,
name|secondSuggestBuilder
operator|.
name|hashCode
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testIllegalSuggestionName
specifier|public
name|void
name|testIllegalSuggestionName
parameter_list|()
block|{
try|try
block|{
operator|new
name|SuggestBuilder
argument_list|()
operator|.
name|addSuggestion
argument_list|(
literal|null
argument_list|,
name|PhraseSuggestionBuilderTests
operator|.
name|randomPhraseSuggestionBuilder
argument_list|()
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"exception expected"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NullPointerException
name|e
parameter_list|)
block|{
name|assertEquals
argument_list|(
literal|"every suggestion needs a name"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
try|try
block|{
operator|new
name|SuggestBuilder
argument_list|()
operator|.
name|addSuggestion
argument_list|(
literal|"my-suggest"
argument_list|,
name|PhraseSuggestionBuilderTests
operator|.
name|randomPhraseSuggestionBuilder
argument_list|()
argument_list|)
operator|.
name|addSuggestion
argument_list|(
literal|"my-suggest"
argument_list|,
name|PhraseSuggestionBuilderTests
operator|.
name|randomPhraseSuggestionBuilder
argument_list|()
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"exception expected"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
name|assertEquals
argument_list|(
literal|"already added another suggestion with name [my-suggest]"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|createTestModel
specifier|protected
name|SuggestBuilder
name|createTestModel
parameter_list|()
block|{
return|return
name|randomSuggestBuilder
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|createMutation
specifier|protected
name|SuggestBuilder
name|createMutation
parameter_list|(
name|SuggestBuilder
name|original
parameter_list|)
throws|throws
name|IOException
block|{
name|SuggestBuilder
name|mutation
init|=
operator|new
name|SuggestBuilder
argument_list|()
operator|.
name|setGlobalText
argument_list|(
name|original
operator|.
name|getGlobalText
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|Entry
argument_list|<
name|String
argument_list|,
name|SuggestionBuilder
argument_list|<
name|?
argument_list|>
argument_list|>
name|suggestionBuilder
range|:
name|original
operator|.
name|getSuggestions
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|mutation
operator|.
name|addSuggestion
argument_list|(
name|suggestionBuilder
operator|.
name|getKey
argument_list|()
argument_list|,
name|suggestionBuilder
operator|.
name|getValue
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
name|mutation
operator|.
name|setGlobalText
argument_list|(
name|randomAsciiOfLengthBetween
argument_list|(
literal|5
argument_list|,
literal|60
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|mutation
operator|.
name|addSuggestion
argument_list|(
name|randomAsciiOfLength
argument_list|(
literal|10
argument_list|)
argument_list|,
name|PhraseSuggestionBuilderTests
operator|.
name|randomPhraseSuggestionBuilder
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|mutation
return|;
block|}
annotation|@
name|Override
DECL|method|readFrom
specifier|protected
name|SuggestBuilder
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|SuggestBuilder
operator|.
name|PROTOTYPE
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
return|;
block|}
DECL|method|randomSuggestBuilder
specifier|public
specifier|static
name|SuggestBuilder
name|randomSuggestBuilder
parameter_list|()
block|{
name|SuggestBuilder
name|builder
init|=
operator|new
name|SuggestBuilder
argument_list|()
decl_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|builder
operator|.
name|setGlobalText
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
specifier|final
name|int
name|numSuggestions
init|=
name|randomIntBetween
argument_list|(
literal|1
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
name|numSuggestions
condition|;
name|i
operator|++
control|)
block|{
name|builder
operator|.
name|addSuggestion
argument_list|(
name|randomAsciiOfLengthBetween
argument_list|(
literal|5
argument_list|,
literal|10
argument_list|)
argument_list|,
name|randomSuggestionBuilder
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|builder
return|;
block|}
DECL|method|randomSuggestionBuilder
specifier|private
specifier|static
name|SuggestionBuilder
argument_list|<
name|?
argument_list|>
name|randomSuggestionBuilder
parameter_list|()
block|{
switch|switch
condition|(
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|2
argument_list|)
condition|)
block|{
case|case
literal|0
case|:
return|return
name|TermSuggestionBuilderTests
operator|.
name|randomTermSuggestionBuilder
argument_list|()
return|;
case|case
literal|1
case|:
return|return
name|PhraseSuggestionBuilderTests
operator|.
name|randomPhraseSuggestionBuilder
argument_list|()
return|;
case|case
literal|2
case|:
return|return
name|CompletionSuggesterBuilderTests
operator|.
name|randomCompletionSuggestionBuilder
argument_list|()
return|;
default|default:
return|return
name|TermSuggestionBuilderTests
operator|.
name|randomTermSuggestionBuilder
argument_list|()
return|;
block|}
block|}
block|}
end_class

end_unit

