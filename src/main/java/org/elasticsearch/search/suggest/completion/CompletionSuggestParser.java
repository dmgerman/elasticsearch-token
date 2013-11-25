begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|ElasticSearchIllegalArgumentException
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
name|mapper
operator|.
name|MapperService
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
name|SuggestContextParser
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
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|suggest
operator|.
name|SuggestUtils
operator|.
name|parseSuggestContext
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|CompletionSuggestParser
specifier|public
class|class
name|CompletionSuggestParser
implements|implements
name|SuggestContextParser
block|{
DECL|field|completionSuggester
specifier|private
name|CompletionSuggester
name|completionSuggester
decl_stmt|;
DECL|method|CompletionSuggestParser
specifier|public
name|CompletionSuggestParser
parameter_list|(
name|CompletionSuggester
name|completionSuggester
parameter_list|)
block|{
name|this
operator|.
name|completionSuggester
operator|=
name|completionSuggester
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|parse
specifier|public
name|SuggestionSearchContext
operator|.
name|SuggestionContext
name|parse
parameter_list|(
name|XContentParser
name|parser
parameter_list|,
name|MapperService
name|mapperService
parameter_list|)
throws|throws
name|IOException
block|{
name|XContentParser
operator|.
name|Token
name|token
decl_stmt|;
name|String
name|fieldName
init|=
literal|null
decl_stmt|;
name|CompletionSuggestionContext
name|suggestion
init|=
operator|new
name|CompletionSuggestionContext
argument_list|(
name|completionSuggester
argument_list|)
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
operator|!
name|parseSuggestContext
argument_list|(
name|parser
argument_list|,
name|mapperService
argument_list|,
name|fieldName
argument_list|,
name|suggestion
argument_list|)
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
name|VALUE_BOOLEAN
operator|&&
literal|"fuzzy"
operator|.
name|equals
argument_list|(
name|fieldName
argument_list|)
condition|)
block|{
name|suggestion
operator|.
name|setFuzzy
argument_list|(
name|parser
operator|.
name|booleanValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
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
operator|&&
literal|"fuzzy"
operator|.
name|equals
argument_list|(
name|fieldName
argument_list|)
condition|)
block|{
name|suggestion
operator|.
name|setFuzzy
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|String
name|fuzzyConfigName
init|=
literal|null
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
name|fuzzyConfigName
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
literal|"edit_distance"
operator|.
name|equals
argument_list|(
name|fuzzyConfigName
argument_list|)
operator|||
literal|"editDistance"
operator|.
name|equals
argument_list|(
name|fuzzyConfigName
argument_list|)
condition|)
block|{
name|suggestion
operator|.
name|setFuzzyEditDistance
argument_list|(
name|parser
operator|.
name|intValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"transpositions"
operator|.
name|equals
argument_list|(
name|fuzzyConfigName
argument_list|)
condition|)
block|{
name|suggestion
operator|.
name|setFuzzyTranspositions
argument_list|(
name|parser
operator|.
name|booleanValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"min_length"
operator|.
name|equals
argument_list|(
name|fuzzyConfigName
argument_list|)
operator|||
literal|"minLength"
operator|.
name|equals
argument_list|(
name|fuzzyConfigName
argument_list|)
condition|)
block|{
name|suggestion
operator|.
name|setFuzzyMinLength
argument_list|(
name|parser
operator|.
name|intValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"prefix_length"
operator|.
name|equals
argument_list|(
name|fuzzyConfigName
argument_list|)
operator|||
literal|"prefixLength"
operator|.
name|equals
argument_list|(
name|fuzzyConfigName
argument_list|)
condition|)
block|{
name|suggestion
operator|.
name|setFuzzyPrefixLength
argument_list|(
name|parser
operator|.
name|intValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"unicode_aware"
operator|.
name|equals
argument_list|(
name|fuzzyConfigName
argument_list|)
operator|||
literal|"unicodeAware"
operator|.
name|equals
argument_list|(
name|fuzzyConfigName
argument_list|)
condition|)
block|{
name|suggestion
operator|.
name|setFuzzyUnicodeAware
argument_list|(
name|parser
operator|.
name|booleanValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
else|else
block|{
throw|throw
operator|new
name|ElasticSearchIllegalArgumentException
argument_list|(
literal|"suggester[completion]  doesn't support field ["
operator|+
name|fieldName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
name|suggestion
operator|.
name|mapper
argument_list|(
name|mapperService
operator|.
name|smartNameFieldMapper
argument_list|(
name|suggestion
operator|.
name|getField
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|suggestion
return|;
block|}
block|}
end_class

end_unit

