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
name|com
operator|.
name|carrotsearch
operator|.
name|randomizedtesting
operator|.
name|generators
operator|.
name|RandomStrings
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
name|ParsingException
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
name|unit
operator|.
name|Fuzziness
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
name|search
operator|.
name|suggest
operator|.
name|AbstractSuggestionBuilderTestCase
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
name|SuggestBuilder
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
name|context
operator|.
name|CategoryQueryContext
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
name|context
operator|.
name|GeoQueryContext
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
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Locale
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
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|containsString
import|;
end_import

begin_class
DECL|class|CompletionSuggesterBuilderTests
specifier|public
class|class
name|CompletionSuggesterBuilderTests
extends|extends
name|AbstractSuggestionBuilderTestCase
argument_list|<
name|CompletionSuggestionBuilder
argument_list|>
block|{
annotation|@
name|Override
DECL|method|randomSuggestionBuilder
specifier|protected
name|CompletionSuggestionBuilder
name|randomSuggestionBuilder
parameter_list|()
block|{
return|return
name|randomCompletionSuggestionBuilder
argument_list|()
return|;
block|}
DECL|method|randomCompletionSuggestionBuilder
specifier|public
specifier|static
name|CompletionSuggestionBuilder
name|randomCompletionSuggestionBuilder
parameter_list|()
block|{
return|return
name|randomSuggestionBuilderWithContextInfo
argument_list|()
operator|.
name|builder
return|;
block|}
DECL|class|BuilderAndInfo
specifier|private
specifier|static
class|class
name|BuilderAndInfo
block|{
DECL|field|builder
name|CompletionSuggestionBuilder
name|builder
decl_stmt|;
DECL|field|catContexts
name|List
argument_list|<
name|String
argument_list|>
name|catContexts
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
DECL|field|geoContexts
name|List
argument_list|<
name|String
argument_list|>
name|geoContexts
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
block|}
DECL|method|randomSuggestionBuilderWithContextInfo
specifier|private
specifier|static
name|BuilderAndInfo
name|randomSuggestionBuilderWithContextInfo
parameter_list|()
block|{
specifier|final
name|BuilderAndInfo
name|builderAndInfo
init|=
operator|new
name|BuilderAndInfo
argument_list|()
decl_stmt|;
name|CompletionSuggestionBuilder
name|testBuilder
init|=
operator|new
name|CompletionSuggestionBuilder
argument_list|(
name|randomAsciiOfLengthBetween
argument_list|(
literal|2
argument_list|,
literal|20
argument_list|)
argument_list|)
decl_stmt|;
name|setCommonPropertiesOnRandomBuilder
argument_list|(
name|testBuilder
argument_list|)
expr_stmt|;
switch|switch
condition|(
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|3
argument_list|)
condition|)
block|{
case|case
literal|0
case|:
name|testBuilder
operator|.
name|prefix
argument_list|(
name|randomAsciiOfLength
argument_list|(
literal|10
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
literal|1
case|:
name|testBuilder
operator|.
name|prefix
argument_list|(
name|randomAsciiOfLength
argument_list|(
literal|10
argument_list|)
argument_list|,
name|FuzzyOptionsTests
operator|.
name|randomFuzzyOptions
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
literal|2
case|:
name|testBuilder
operator|.
name|prefix
argument_list|(
name|randomAsciiOfLength
argument_list|(
literal|10
argument_list|)
argument_list|,
name|randomFrom
argument_list|(
name|Fuzziness
operator|.
name|ZERO
argument_list|,
name|Fuzziness
operator|.
name|ONE
argument_list|,
name|Fuzziness
operator|.
name|TWO
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
literal|3
case|:
name|testBuilder
operator|.
name|regex
argument_list|(
name|randomAsciiOfLength
argument_list|(
literal|10
argument_list|)
argument_list|,
name|RegexOptionsTests
operator|.
name|randomRegexOptions
argument_list|()
argument_list|)
expr_stmt|;
break|break;
block|}
name|List
argument_list|<
name|String
argument_list|>
name|payloads
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|Collections
operator|.
name|addAll
argument_list|(
name|payloads
argument_list|,
name|generateRandomStringArray
argument_list|(
literal|5
argument_list|,
literal|10
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|maybeSet
argument_list|(
name|testBuilder
operator|::
name|payload
argument_list|,
name|payloads
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|?
extends|extends
name|ToXContent
argument_list|>
argument_list|>
name|contextMap
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|int
name|numContext
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|5
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|CategoryQueryContext
argument_list|>
name|contexts
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|numContext
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
name|numContext
condition|;
name|i
operator|++
control|)
block|{
name|contexts
operator|.
name|add
argument_list|(
name|CategoryQueryContextTests
operator|.
name|randomCategoryQueryContext
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|String
name|name
init|=
name|randomAsciiOfLength
argument_list|(
literal|10
argument_list|)
decl_stmt|;
name|contextMap
operator|.
name|put
argument_list|(
name|name
argument_list|,
name|contexts
argument_list|)
expr_stmt|;
name|builderAndInfo
operator|.
name|catContexts
operator|.
name|add
argument_list|(
name|name
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|int
name|numContext
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|5
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|GeoQueryContext
argument_list|>
name|contexts
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|numContext
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
name|numContext
condition|;
name|i
operator|++
control|)
block|{
name|contexts
operator|.
name|add
argument_list|(
name|GeoQueryContextTests
operator|.
name|randomGeoQueryContext
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|String
name|name
init|=
name|randomAsciiOfLength
argument_list|(
literal|10
argument_list|)
decl_stmt|;
name|contextMap
operator|.
name|put
argument_list|(
name|name
argument_list|,
name|contexts
argument_list|)
expr_stmt|;
name|builderAndInfo
operator|.
name|geoContexts
operator|.
name|add
argument_list|(
name|name
argument_list|)
expr_stmt|;
block|}
name|testBuilder
operator|.
name|contexts
argument_list|(
name|contextMap
argument_list|)
expr_stmt|;
name|builderAndInfo
operator|.
name|builder
operator|=
name|testBuilder
expr_stmt|;
return|return
name|builderAndInfo
return|;
block|}
annotation|@
name|Override
DECL|method|mutateSpecificParameters
specifier|protected
name|void
name|mutateSpecificParameters
parameter_list|(
name|CompletionSuggestionBuilder
name|builder
parameter_list|)
throws|throws
name|IOException
block|{
switch|switch
condition|(
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|5
argument_list|)
condition|)
block|{
case|case
literal|0
case|:
name|List
argument_list|<
name|String
argument_list|>
name|payloads
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|Collections
operator|.
name|addAll
argument_list|(
name|payloads
argument_list|,
name|generateRandomStringArray
argument_list|(
literal|5
argument_list|,
literal|10
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|.
name|payload
argument_list|(
name|payloads
argument_list|)
expr_stmt|;
break|break;
case|case
literal|1
case|:
name|int
name|nCatContext
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|5
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|CategoryQueryContext
argument_list|>
name|contexts
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|nCatContext
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
name|nCatContext
condition|;
name|i
operator|++
control|)
block|{
name|contexts
operator|.
name|add
argument_list|(
name|CategoryQueryContextTests
operator|.
name|randomCategoryQueryContext
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|contexts
argument_list|(
name|Collections
operator|.
name|singletonMap
argument_list|(
name|randomAsciiOfLength
argument_list|(
literal|10
argument_list|)
argument_list|,
name|contexts
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
literal|2
case|:
name|int
name|nGeoContext
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|5
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|GeoQueryContext
argument_list|>
name|geoContexts
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|nGeoContext
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
name|nGeoContext
condition|;
name|i
operator|++
control|)
block|{
name|geoContexts
operator|.
name|add
argument_list|(
name|GeoQueryContextTests
operator|.
name|randomGeoQueryContext
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|contexts
argument_list|(
name|Collections
operator|.
name|singletonMap
argument_list|(
name|randomAsciiOfLength
argument_list|(
literal|10
argument_list|)
argument_list|,
name|geoContexts
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
literal|3
case|:
name|builder
operator|.
name|prefix
argument_list|(
name|randomAsciiOfLength
argument_list|(
literal|10
argument_list|)
argument_list|,
name|FuzzyOptionsTests
operator|.
name|randomFuzzyOptions
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
literal|4
case|:
name|builder
operator|.
name|prefix
argument_list|(
name|randomAsciiOfLength
argument_list|(
literal|10
argument_list|)
argument_list|,
name|randomFrom
argument_list|(
name|Fuzziness
operator|.
name|ZERO
argument_list|,
name|Fuzziness
operator|.
name|ONE
argument_list|,
name|Fuzziness
operator|.
name|TWO
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
literal|5
case|:
name|builder
operator|.
name|regex
argument_list|(
name|randomAsciiOfLength
argument_list|(
literal|10
argument_list|)
argument_list|,
name|RegexOptionsTests
operator|.
name|randomRegexOptions
argument_list|()
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"should not through"
argument_list|)
throw|;
block|}
block|}
comment|/**      * Test that a malformed JSON suggestion request fails.      */
DECL|method|testMalformedJsonRequestPayload
specifier|public
name|void
name|testMalformedJsonRequestPayload
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|String
name|field
init|=
name|RandomStrings
operator|.
name|randomAsciiOfLength
argument_list|(
name|getRandom
argument_list|()
argument_list|,
literal|10
argument_list|)
operator|.
name|toLowerCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
decl_stmt|;
specifier|final
name|String
name|payload
init|=
literal|"{\n"
operator|+
literal|"  \"bad-payload\" : { \n"
operator|+
literal|"    \"prefix\" : \"sug\",\n"
operator|+
literal|"    \"completion\" : { \n"
operator|+
literal|"      \"field\" : \""
operator|+
name|field
operator|+
literal|"\",\n "
operator|+
literal|"      \"payload\" : [ {\"payload\":\"field\"} ]\n"
operator|+
literal|"    }\n"
operator|+
literal|"  }\n"
operator|+
literal|"}\n"
decl_stmt|;
try|try
block|{
specifier|final
name|SuggestBuilder
name|suggestBuilder
init|=
name|SuggestBuilder
operator|.
name|fromXContent
argument_list|(
name|newParseContext
argument_list|(
name|payload
argument_list|)
argument_list|,
name|suggesters
argument_list|)
decl_stmt|;
name|fail
argument_list|(
literal|"Should not have been able to create SuggestBuilder from malformed JSON: "
operator|+
name|suggestBuilder
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ParsingException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"failed to parse field [payload]"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

