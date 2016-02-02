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
name|apache
operator|.
name|lucene
operator|.
name|search
operator|.
name|IndexSearcher
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
name|util
operator|.
name|CharsRefBuilder
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
name|index
operator|.
name|query
operator|.
name|QueryShardContext
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

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|CustomSuggester
specifier|public
class|class
name|CustomSuggester
extends|extends
name|Suggester
argument_list|<
name|CustomSuggester
operator|.
name|CustomSuggestionsContext
argument_list|>
block|{
comment|// This is a pretty dumb implementation which returns the original text + fieldName + custom config option + 12 or 123
annotation|@
name|Override
DECL|method|innerExecute
specifier|public
name|Suggest
operator|.
name|Suggestion
argument_list|<
name|?
extends|extends
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
name|innerExecute
parameter_list|(
name|String
name|name
parameter_list|,
name|CustomSuggestionsContext
name|suggestion
parameter_list|,
name|IndexSearcher
name|searcher
parameter_list|,
name|CharsRefBuilder
name|spare
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Get the suggestion context
name|String
name|text
init|=
name|suggestion
operator|.
name|getText
argument_list|()
operator|.
name|utf8ToString
argument_list|()
decl_stmt|;
comment|// create two suggestions with 12 and 123 appended
name|Suggest
operator|.
name|Suggestion
argument_list|<
name|Suggest
operator|.
name|Suggestion
operator|.
name|Entry
argument_list|<
name|Suggest
operator|.
name|Suggestion
operator|.
name|Entry
operator|.
name|Option
argument_list|>
argument_list|>
name|response
init|=
operator|new
name|Suggest
operator|.
name|Suggestion
argument_list|<>
argument_list|(
name|name
argument_list|,
name|suggestion
operator|.
name|getSize
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|firstSuggestion
init|=
name|String
operator|.
name|format
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|,
literal|"%s-%s-%s-%s"
argument_list|,
name|text
argument_list|,
name|suggestion
operator|.
name|getField
argument_list|()
argument_list|,
name|suggestion
operator|.
name|options
operator|.
name|get
argument_list|(
literal|"suffix"
argument_list|)
argument_list|,
literal|"12"
argument_list|)
decl_stmt|;
name|Suggest
operator|.
name|Suggestion
operator|.
name|Entry
argument_list|<
name|Suggest
operator|.
name|Suggestion
operator|.
name|Entry
operator|.
name|Option
argument_list|>
name|resultEntry12
init|=
operator|new
name|Suggest
operator|.
name|Suggestion
operator|.
name|Entry
argument_list|<>
argument_list|(
operator|new
name|Text
argument_list|(
name|firstSuggestion
argument_list|)
argument_list|,
literal|0
argument_list|,
name|text
operator|.
name|length
argument_list|()
operator|+
literal|2
argument_list|)
decl_stmt|;
name|response
operator|.
name|addTerm
argument_list|(
name|resultEntry12
argument_list|)
expr_stmt|;
name|String
name|secondSuggestion
init|=
name|String
operator|.
name|format
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|,
literal|"%s-%s-%s-%s"
argument_list|,
name|text
argument_list|,
name|suggestion
operator|.
name|getField
argument_list|()
argument_list|,
name|suggestion
operator|.
name|options
operator|.
name|get
argument_list|(
literal|"suffix"
argument_list|)
argument_list|,
literal|"123"
argument_list|)
decl_stmt|;
name|Suggest
operator|.
name|Suggestion
operator|.
name|Entry
argument_list|<
name|Suggest
operator|.
name|Suggestion
operator|.
name|Entry
operator|.
name|Option
argument_list|>
name|resultEntry123
init|=
operator|new
name|Suggest
operator|.
name|Suggestion
operator|.
name|Entry
argument_list|<>
argument_list|(
operator|new
name|Text
argument_list|(
name|secondSuggestion
argument_list|)
argument_list|,
literal|0
argument_list|,
name|text
operator|.
name|length
argument_list|()
operator|+
literal|3
argument_list|)
decl_stmt|;
name|response
operator|.
name|addTerm
argument_list|(
name|resultEntry123
argument_list|)
expr_stmt|;
return|return
name|response
return|;
block|}
annotation|@
name|Override
DECL|method|getContextParser
specifier|public
name|SuggestContextParser
name|getContextParser
parameter_list|()
block|{
return|return
parameter_list|(
name|parser
parameter_list|,
name|shardContext
parameter_list|)
lambda|->
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|options
init|=
name|parser
operator|.
name|map
argument_list|()
decl_stmt|;
name|CustomSuggestionsContext
name|suggestionContext
init|=
operator|new
name|CustomSuggestionsContext
argument_list|(
name|shardContext
argument_list|,
name|options
argument_list|)
decl_stmt|;
name|suggestionContext
operator|.
name|setField
argument_list|(
operator|(
name|String
operator|)
name|options
operator|.
name|get
argument_list|(
literal|"field"
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|suggestionContext
return|;
block|}
return|;
block|}
DECL|class|CustomSuggestionsContext
specifier|public
specifier|static
class|class
name|CustomSuggestionsContext
extends|extends
name|SuggestionSearchContext
operator|.
name|SuggestionContext
block|{
DECL|field|options
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|options
decl_stmt|;
DECL|method|CustomSuggestionsContext
specifier|public
name|CustomSuggestionsContext
parameter_list|(
name|QueryShardContext
name|context
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|options
parameter_list|)
block|{
name|super
argument_list|(
operator|new
name|CustomSuggester
argument_list|()
argument_list|,
name|context
argument_list|)
expr_stmt|;
name|this
operator|.
name|options
operator|=
name|options
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|getBuilderPrototype
specifier|public
name|SuggestionBuilder
argument_list|<
name|?
argument_list|>
name|getBuilderPrototype
parameter_list|()
block|{
return|return
name|CustomSuggesterSearchIT
operator|.
name|CustomSuggestionBuilder
operator|.
name|PROTOTYPE
return|;
block|}
block|}
end_class

end_unit

