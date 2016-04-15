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
name|support
operator|.
name|ToXContentToBytes
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
name|Nullable
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
name|io
operator|.
name|stream
operator|.
name|Writeable
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
name|Map
operator|.
name|Entry
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

begin_comment
comment|/**  * Defines how to perform suggesting. This builders allows a number of global options to be specified and  * an arbitrary number of {@link SuggestionBuilder} instances.  *<p>  * Suggesting works by suggesting terms/phrases that appear in the suggest text that are similar compared  * to the terms in provided text. These suggestions are based on several options described in this class.  */
end_comment

begin_class
DECL|class|SuggestBuilder
specifier|public
class|class
name|SuggestBuilder
extends|extends
name|ToXContentToBytes
implements|implements
name|Writeable
argument_list|<
name|SuggestBuilder
argument_list|>
block|{
DECL|field|GLOBAL_TEXT_FIELD
specifier|protected
specifier|static
specifier|final
name|ParseField
name|GLOBAL_TEXT_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"text"
argument_list|)
decl_stmt|;
DECL|field|globalText
specifier|private
name|String
name|globalText
decl_stmt|;
DECL|field|suggestions
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|SuggestionBuilder
argument_list|<
name|?
argument_list|>
argument_list|>
name|suggestions
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
comment|/**      * Build an empty SuggestBuilder.      */
DECL|method|SuggestBuilder
specifier|public
name|SuggestBuilder
parameter_list|()
block|{     }
comment|/**      * Read from a stream.      */
DECL|method|SuggestBuilder
specifier|public
name|SuggestBuilder
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|globalText
operator|=
name|in
operator|.
name|readOptionalString
argument_list|()
expr_stmt|;
specifier|final
name|int
name|size
init|=
name|in
operator|.
name|readVInt
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
name|size
condition|;
name|i
operator|++
control|)
block|{
name|suggestions
operator|.
name|put
argument_list|(
name|in
operator|.
name|readString
argument_list|()
argument_list|,
name|in
operator|.
name|readSuggestion
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|writeTo
specifier|public
name|void
name|writeTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeOptionalString
argument_list|(
name|globalText
argument_list|)
expr_stmt|;
specifier|final
name|int
name|size
init|=
name|suggestions
operator|.
name|size
argument_list|()
decl_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|size
argument_list|)
expr_stmt|;
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
name|suggestion
range|:
name|suggestions
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|out
operator|.
name|writeString
argument_list|(
name|suggestion
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeSuggestion
argument_list|(
name|suggestion
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      * Sets the text to provide suggestions for. The suggest text is a required option that needs      * to be set either via this setter or via the {@link org.elasticsearch.search.suggest.SuggestionBuilder#text(String)} method.      *<p>      * The suggest text gets analyzed by the suggest analyzer or the suggest field search analyzer.      * For each analyzed token, suggested terms are suggested if possible.      */
DECL|method|setGlobalText
specifier|public
name|SuggestBuilder
name|setGlobalText
parameter_list|(
annotation|@
name|Nullable
name|String
name|globalText
parameter_list|)
block|{
name|this
operator|.
name|globalText
operator|=
name|globalText
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Gets the global suggest text      */
annotation|@
name|Nullable
DECL|method|getGlobalText
specifier|public
name|String
name|getGlobalText
parameter_list|()
block|{
return|return
name|globalText
return|;
block|}
comment|/**      * Adds an {@link org.elasticsearch.search.suggest.SuggestionBuilder} instance under a user defined name.      * The order in which the<code>Suggestions</code> are added, is the same as in the response.      * @throws IllegalArgumentException if two suggestions added have the same name      */
DECL|method|addSuggestion
specifier|public
name|SuggestBuilder
name|addSuggestion
parameter_list|(
name|String
name|name
parameter_list|,
name|SuggestionBuilder
argument_list|<
name|?
argument_list|>
name|suggestion
parameter_list|)
block|{
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|name
argument_list|,
literal|"every suggestion needs a name"
argument_list|)
expr_stmt|;
if|if
condition|(
name|suggestions
operator|.
name|get
argument_list|(
name|name
argument_list|)
operator|==
literal|null
condition|)
block|{
name|suggestions
operator|.
name|put
argument_list|(
name|name
argument_list|,
name|suggestion
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"already added another suggestion with name ["
operator|+
name|name
operator|+
literal|"]"
argument_list|)
throw|;
block|}
return|return
name|this
return|;
block|}
comment|/**      * Get all the<code>Suggestions</code> that were added to the global {@link SuggestBuilder},      * together with their names      */
DECL|method|getSuggestions
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|SuggestionBuilder
argument_list|<
name|?
argument_list|>
argument_list|>
name|getSuggestions
parameter_list|()
block|{
return|return
name|suggestions
return|;
block|}
annotation|@
name|Override
DECL|method|toXContent
specifier|public
name|XContentBuilder
name|toXContent
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
name|startObject
argument_list|()
expr_stmt|;
if|if
condition|(
name|globalText
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"text"
argument_list|,
name|globalText
argument_list|)
expr_stmt|;
block|}
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
name|suggestion
range|:
name|suggestions
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|builder
operator|.
name|startObject
argument_list|(
name|suggestion
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
name|suggestion
operator|.
name|getValue
argument_list|()
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
return|return
name|builder
return|;
block|}
DECL|method|fromXContent
specifier|public
specifier|static
name|SuggestBuilder
name|fromXContent
parameter_list|(
name|QueryParseContext
name|parseContext
parameter_list|,
name|Suggesters
name|suggesters
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
name|SuggestBuilder
name|suggestBuilder
init|=
operator|new
name|SuggestBuilder
argument_list|()
decl_stmt|;
name|String
name|fieldName
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|parser
operator|.
name|currentToken
argument_list|()
operator|==
literal|null
condition|)
block|{
comment|// when we parse from RestSuggestAction the current token is null, advance the token
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
block|}
assert|assert
name|parser
operator|.
name|currentToken
argument_list|()
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|START_OBJECT
operator|:
literal|"current token must be a start object"
assert|;
name|XContentParser
operator|.
name|Token
name|token
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
name|fieldName
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
name|fieldName
argument_list|,
name|GLOBAL_TEXT_FIELD
argument_list|)
condition|)
block|{
name|suggestBuilder
operator|.
name|setGlobalText
argument_list|(
name|parser
operator|.
name|text
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"[suggest] does not support ["
operator|+
name|fieldName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
elseif|else
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|START_OBJECT
condition|)
block|{
name|String
name|suggestionName
init|=
name|fieldName
decl_stmt|;
if|if
condition|(
name|suggestionName
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"suggestion must have name"
argument_list|)
throw|;
block|}
name|suggestBuilder
operator|.
name|addSuggestion
argument_list|(
name|suggestionName
argument_list|,
name|SuggestionBuilder
operator|.
name|fromXContent
argument_list|(
name|parseContext
argument_list|,
name|suggesters
argument_list|)
argument_list|)
expr_stmt|;
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
literal|"unexpected token ["
operator|+
name|token
operator|+
literal|"] after ["
operator|+
name|fieldName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
return|return
name|suggestBuilder
return|;
block|}
DECL|method|build
specifier|public
name|SuggestionSearchContext
name|build
parameter_list|(
name|QueryShardContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|SuggestionSearchContext
name|suggestionSearchContext
init|=
operator|new
name|SuggestionSearchContext
argument_list|()
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
name|suggestion
range|:
name|suggestions
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|SuggestionContext
name|suggestionContext
init|=
name|suggestion
operator|.
name|getValue
argument_list|()
operator|.
name|build
argument_list|(
name|context
argument_list|)
decl_stmt|;
if|if
condition|(
name|suggestionContext
operator|.
name|getText
argument_list|()
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|globalText
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"The required text option is missing"
argument_list|)
throw|;
block|}
name|suggestionContext
operator|.
name|setText
argument_list|(
name|BytesRefs
operator|.
name|toBytesRef
argument_list|(
name|globalText
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|suggestionSearchContext
operator|.
name|addSuggestion
argument_list|(
name|suggestion
operator|.
name|getKey
argument_list|()
argument_list|,
name|suggestionContext
argument_list|)
expr_stmt|;
block|}
return|return
name|suggestionSearchContext
return|;
block|}
annotation|@
name|Override
DECL|method|equals
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|other
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|other
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
name|other
operator|==
literal|null
operator|||
name|getClass
argument_list|()
operator|!=
name|other
operator|.
name|getClass
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
name|SuggestBuilder
name|o
init|=
operator|(
name|SuggestBuilder
operator|)
name|other
decl_stmt|;
return|return
name|Objects
operator|.
name|equals
argument_list|(
name|globalText
argument_list|,
name|o
operator|.
name|globalText
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|suggestions
argument_list|,
name|o
operator|.
name|suggestions
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|hashCode
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|Objects
operator|.
name|hash
argument_list|(
name|globalText
argument_list|,
name|suggestions
argument_list|)
return|;
block|}
block|}
end_class

end_unit

