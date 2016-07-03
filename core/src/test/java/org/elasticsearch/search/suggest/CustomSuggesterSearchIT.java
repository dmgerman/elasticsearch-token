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
name|common
operator|.
name|ParseField
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
name|io
operator|.
name|stream
operator|.
name|StreamOutput
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
name|lucene
operator|.
name|BytesRefs
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
name|util
operator|.
name|CollectionUtils
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
name|XContentParser
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
name|index
operator|.
name|query
operator|.
name|QueryShardContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|plugins
operator|.
name|Plugin
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
name|SuggestionSearchContext
operator|.
name|SuggestionContext
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
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|ESIntegTestCase
operator|.
name|ClusterScope
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
operator|.
name|Scope
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
name|Collection
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
import|import
name|java
operator|.
name|util
operator|.
name|Objects
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|support
operator|.
name|WriteRequest
operator|.
name|RefreshPolicy
operator|.
name|IMMEDIATE
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
name|hasSize
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

begin_comment
comment|/**  * Integration test for registering a custom suggester.  */
end_comment

begin_class
annotation|@
name|ClusterScope
argument_list|(
name|scope
operator|=
name|Scope
operator|.
name|SUITE
argument_list|,
name|numDataNodes
operator|=
literal|1
argument_list|)
DECL|class|CustomSuggesterSearchIT
specifier|public
class|class
name|CustomSuggesterSearchIT
extends|extends
name|ESIntegTestCase
block|{
annotation|@
name|Override
DECL|method|nodePlugins
specifier|protected
name|Collection
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|Plugin
argument_list|>
argument_list|>
name|nodePlugins
parameter_list|()
block|{
return|return
name|pluginList
argument_list|(
name|CustomSuggesterPlugin
operator|.
name|class
argument_list|)
return|;
block|}
DECL|method|testThatCustomSuggestersCanBeRegisteredAndWork
specifier|public
name|void
name|testThatCustomSuggestersCanBeRegisteredAndWork
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
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"test"
argument_list|,
literal|"1"
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
literal|"name"
argument_list|,
literal|"arbitrary content"
argument_list|)
operator|.
name|endObject
argument_list|()
argument_list|)
operator|.
name|setRefreshPolicy
argument_list|(
name|IMMEDIATE
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|ensureYellow
argument_list|()
expr_stmt|;
name|String
name|randomText
init|=
name|randomAsciiOfLength
argument_list|(
literal|10
argument_list|)
decl_stmt|;
name|String
name|randomField
init|=
name|randomAsciiOfLength
argument_list|(
literal|10
argument_list|)
decl_stmt|;
name|String
name|randomSuffix
init|=
name|randomAsciiOfLength
argument_list|(
literal|10
argument_list|)
decl_stmt|;
name|SuggestBuilder
name|suggestBuilder
init|=
operator|new
name|SuggestBuilder
argument_list|()
decl_stmt|;
name|suggestBuilder
operator|.
name|addSuggestion
argument_list|(
literal|"someName"
argument_list|,
operator|new
name|CustomSuggestionBuilder
argument_list|(
name|randomField
argument_list|,
name|randomSuffix
argument_list|)
operator|.
name|text
argument_list|(
name|randomText
argument_list|)
argument_list|)
expr_stmt|;
name|SearchRequestBuilder
name|searchRequestBuilder
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
operator|.
name|setFrom
argument_list|(
literal|0
argument_list|)
operator|.
name|setSize
argument_list|(
literal|1
argument_list|)
operator|.
name|suggest
argument_list|(
name|suggestBuilder
argument_list|)
decl_stmt|;
name|SearchResponse
name|searchResponse
init|=
name|searchRequestBuilder
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
comment|// TODO: infer type once JI-9019884 is fixed
comment|// TODO: see also JDK-8039214
name|List
argument_list|<
name|Suggest
operator|.
name|Suggestion
operator|.
name|Entry
argument_list|<
name|?
extends|extends
name|Suggest
operator|.
name|Suggestion
operator|.
name|Entry
operator|.
name|Option
argument_list|>
argument_list|>
name|suggestions
init|=
name|CollectionUtils
operator|.
expr|<
name|Suggest
operator|.
name|Suggestion
operator|.
name|Entry
argument_list|<
name|?
extends|extends
name|Suggest
operator|.
name|Suggestion
operator|.
name|Entry
operator|.
name|Option
argument_list|>
operator|>
name|iterableAsArrayList
argument_list|(
name|searchResponse
operator|.
name|getSuggest
argument_list|()
operator|.
name|getSuggestion
argument_list|(
literal|"someName"
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|suggestions
argument_list|,
name|hasSize
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|suggestions
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getText
argument_list|()
operator|.
name|string
argument_list|()
argument_list|,
name|is
argument_list|(
name|String
operator|.
name|format
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|,
literal|"%s-%s-%s-12"
argument_list|,
name|randomText
argument_list|,
name|randomField
argument_list|,
name|randomSuffix
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|suggestions
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|getText
argument_list|()
operator|.
name|string
argument_list|()
argument_list|,
name|is
argument_list|(
name|String
operator|.
name|format
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|,
literal|"%s-%s-%s-123"
argument_list|,
name|randomText
argument_list|,
name|randomField
argument_list|,
name|randomSuffix
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|class|CustomSuggestionBuilder
specifier|public
specifier|static
class|class
name|CustomSuggestionBuilder
extends|extends
name|SuggestionBuilder
argument_list|<
name|CustomSuggestionBuilder
argument_list|>
block|{
DECL|field|RANDOM_SUFFIX_FIELD
specifier|protected
specifier|static
specifier|final
name|ParseField
name|RANDOM_SUFFIX_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"suffix"
argument_list|)
decl_stmt|;
DECL|field|randomSuffix
specifier|private
name|String
name|randomSuffix
decl_stmt|;
DECL|method|CustomSuggestionBuilder
specifier|public
name|CustomSuggestionBuilder
parameter_list|(
name|String
name|randomField
parameter_list|,
name|String
name|randomSuffix
parameter_list|)
block|{
name|super
argument_list|(
name|randomField
argument_list|)
expr_stmt|;
name|this
operator|.
name|randomSuffix
operator|=
name|randomSuffix
expr_stmt|;
block|}
comment|/**          * Read from a stream.          */
DECL|method|CustomSuggestionBuilder
specifier|public
name|CustomSuggestionBuilder
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|this
operator|.
name|randomSuffix
operator|=
name|in
operator|.
name|readString
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doWriteTo
specifier|public
name|void
name|doWriteTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeString
argument_list|(
name|randomSuffix
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|innerToXContent
specifier|protected
name|XContentBuilder
name|innerToXContent
parameter_list|(
name|XContentBuilder
name|builder
parameter_list|,
name|Params
name|params
parameter_list|)
throws|throws
name|IOException
block|{
name|builder
operator|.
name|field
argument_list|(
name|RANDOM_SUFFIX_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|randomSuffix
argument_list|)
expr_stmt|;
return|return
name|builder
return|;
block|}
annotation|@
name|Override
DECL|method|getWriteableName
specifier|public
name|String
name|getWriteableName
parameter_list|()
block|{
return|return
literal|"custom"
return|;
block|}
annotation|@
name|Override
DECL|method|doEquals
specifier|protected
name|boolean
name|doEquals
parameter_list|(
name|CustomSuggestionBuilder
name|other
parameter_list|)
block|{
return|return
name|Objects
operator|.
name|equals
argument_list|(
name|randomSuffix
argument_list|,
name|other
operator|.
name|randomSuffix
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|doHashCode
specifier|protected
name|int
name|doHashCode
parameter_list|()
block|{
return|return
name|Objects
operator|.
name|hash
argument_list|(
name|randomSuffix
argument_list|)
return|;
block|}
DECL|method|innerFromXContent
specifier|static
name|CustomSuggestionBuilder
name|innerFromXContent
parameter_list|(
name|QueryParseContext
name|parseContext
parameter_list|)
throws|throws
name|IOException
block|{
name|XContentParser
name|parser
init|=
name|parseContext
operator|.
name|parser
argument_list|()
decl_stmt|;
name|ParseFieldMatcher
name|parseFieldMatcher
init|=
name|parseContext
operator|.
name|getParseFieldMatcher
argument_list|()
decl_stmt|;
name|XContentParser
operator|.
name|Token
name|token
decl_stmt|;
name|String
name|currentFieldName
init|=
literal|null
decl_stmt|;
name|String
name|fieldname
init|=
literal|null
decl_stmt|;
name|String
name|suffix
init|=
literal|null
decl_stmt|;
name|String
name|analyzer
init|=
literal|null
decl_stmt|;
name|int
name|sizeField
init|=
operator|-
literal|1
decl_stmt|;
name|int
name|shardSize
init|=
operator|-
literal|1
decl_stmt|;
while|while
condition|(
operator|(
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
operator|)
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|END_OBJECT
condition|)
block|{
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|FIELD_NAME
condition|)
block|{
name|currentFieldName
operator|=
name|parser
operator|.
name|currentName
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|token
operator|.
name|isValue
argument_list|()
condition|)
block|{
if|if
condition|(
name|parseFieldMatcher
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|SuggestionBuilder
operator|.
name|ANALYZER_FIELD
argument_list|)
condition|)
block|{
name|analyzer
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|parseFieldMatcher
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|SuggestionBuilder
operator|.
name|FIELDNAME_FIELD
argument_list|)
condition|)
block|{
name|fieldname
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|parseFieldMatcher
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|SuggestionBuilder
operator|.
name|SIZE_FIELD
argument_list|)
condition|)
block|{
name|sizeField
operator|=
name|parser
operator|.
name|intValue
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|parseFieldMatcher
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|SuggestionBuilder
operator|.
name|SHARDSIZE_FIELD
argument_list|)
condition|)
block|{
name|shardSize
operator|=
name|parser
operator|.
name|intValue
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|parseFieldMatcher
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|RANDOM_SUFFIX_FIELD
argument_list|)
condition|)
block|{
name|suffix
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
block|}
block|}
else|else
block|{
throw|throw
operator|new
name|ParsingException
argument_list|(
name|parser
operator|.
name|getTokenLocation
argument_list|()
argument_list|,
literal|"suggester[custom] doesn't support field ["
operator|+
name|currentFieldName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
comment|// now we should have field name, check and copy fields over to the suggestion builder we return
if|if
condition|(
name|fieldname
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ParsingException
argument_list|(
name|parser
operator|.
name|getTokenLocation
argument_list|()
argument_list|,
literal|"the required field option is missing"
argument_list|)
throw|;
block|}
name|CustomSuggestionBuilder
name|builder
init|=
operator|new
name|CustomSuggestionBuilder
argument_list|(
name|fieldname
argument_list|,
name|suffix
argument_list|)
decl_stmt|;
if|if
condition|(
name|analyzer
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|analyzer
argument_list|(
name|analyzer
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|sizeField
operator|!=
operator|-
literal|1
condition|)
block|{
name|builder
operator|.
name|size
argument_list|(
name|sizeField
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|shardSize
operator|!=
operator|-
literal|1
condition|)
block|{
name|builder
operator|.
name|shardSize
argument_list|(
name|shardSize
argument_list|)
expr_stmt|;
block|}
return|return
name|builder
return|;
block|}
annotation|@
name|Override
DECL|method|build
specifier|public
name|SuggestionContext
name|build
parameter_list|(
name|QueryShardContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|options
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|options
operator|.
name|put
argument_list|(
name|FIELDNAME_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|field
argument_list|()
argument_list|)
expr_stmt|;
name|options
operator|.
name|put
argument_list|(
name|RANDOM_SUFFIX_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|randomSuffix
argument_list|)
expr_stmt|;
name|CustomSuggester
operator|.
name|CustomSuggestionsContext
name|customSuggestionsContext
init|=
operator|new
name|CustomSuggester
operator|.
name|CustomSuggestionsContext
argument_list|(
name|context
argument_list|,
name|options
argument_list|)
decl_stmt|;
name|customSuggestionsContext
operator|.
name|setField
argument_list|(
name|field
argument_list|()
argument_list|)
expr_stmt|;
assert|assert
name|text
operator|!=
literal|null
assert|;
name|customSuggestionsContext
operator|.
name|setText
argument_list|(
name|BytesRefs
operator|.
name|toBytesRef
argument_list|(
name|text
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|customSuggestionsContext
return|;
block|}
block|}
block|}
end_class

end_unit

