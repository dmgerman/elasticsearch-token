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
name|apache
operator|.
name|lucene
operator|.
name|search
operator|.
name|suggest
operator|.
name|analyzing
operator|.
name|XFuzzySuggester
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
name|core
operator|.
name|CompletionFieldMapper
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
name|Suggester
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
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|suggest
operator|.
name|context
operator|.
name|ContextMapping
operator|.
name|ContextQuery
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

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|CompletionSuggestionContext
specifier|public
class|class
name|CompletionSuggestionContext
extends|extends
name|SuggestionSearchContext
operator|.
name|SuggestionContext
block|{
DECL|field|fieldType
specifier|private
name|CompletionFieldMapper
operator|.
name|CompletionFieldType
name|fieldType
decl_stmt|;
DECL|field|fuzzyEditDistance
specifier|private
name|int
name|fuzzyEditDistance
init|=
name|XFuzzySuggester
operator|.
name|DEFAULT_MAX_EDITS
decl_stmt|;
DECL|field|fuzzyTranspositions
specifier|private
name|boolean
name|fuzzyTranspositions
init|=
name|XFuzzySuggester
operator|.
name|DEFAULT_TRANSPOSITIONS
decl_stmt|;
DECL|field|fuzzyMinLength
specifier|private
name|int
name|fuzzyMinLength
init|=
name|XFuzzySuggester
operator|.
name|DEFAULT_MIN_FUZZY_LENGTH
decl_stmt|;
DECL|field|fuzzyPrefixLength
specifier|private
name|int
name|fuzzyPrefixLength
init|=
name|XFuzzySuggester
operator|.
name|DEFAULT_NON_FUZZY_PREFIX
decl_stmt|;
DECL|field|fuzzy
specifier|private
name|boolean
name|fuzzy
init|=
literal|false
decl_stmt|;
DECL|field|fuzzyUnicodeAware
specifier|private
name|boolean
name|fuzzyUnicodeAware
init|=
name|XFuzzySuggester
operator|.
name|DEFAULT_UNICODE_AWARE
decl_stmt|;
DECL|field|contextQueries
specifier|private
name|List
argument_list|<
name|ContextQuery
argument_list|>
name|contextQueries
init|=
name|Collections
operator|.
name|emptyList
argument_list|()
decl_stmt|;
DECL|method|CompletionSuggestionContext
specifier|public
name|CompletionSuggestionContext
parameter_list|(
name|Suggester
name|suggester
parameter_list|)
block|{
name|super
argument_list|(
name|suggester
argument_list|)
expr_stmt|;
block|}
DECL|method|fieldType
specifier|public
name|CompletionFieldMapper
operator|.
name|CompletionFieldType
name|fieldType
parameter_list|()
block|{
return|return
name|this
operator|.
name|fieldType
return|;
block|}
DECL|method|fieldType
specifier|public
name|void
name|fieldType
parameter_list|(
name|CompletionFieldMapper
operator|.
name|CompletionFieldType
name|fieldType
parameter_list|)
block|{
name|this
operator|.
name|fieldType
operator|=
name|fieldType
expr_stmt|;
block|}
DECL|method|setFuzzyEditDistance
specifier|public
name|void
name|setFuzzyEditDistance
parameter_list|(
name|int
name|fuzzyEditDistance
parameter_list|)
block|{
name|this
operator|.
name|fuzzyEditDistance
operator|=
name|fuzzyEditDistance
expr_stmt|;
block|}
DECL|method|getFuzzyEditDistance
specifier|public
name|int
name|getFuzzyEditDistance
parameter_list|()
block|{
return|return
name|fuzzyEditDistance
return|;
block|}
DECL|method|setFuzzyTranspositions
specifier|public
name|void
name|setFuzzyTranspositions
parameter_list|(
name|boolean
name|fuzzyTranspositions
parameter_list|)
block|{
name|this
operator|.
name|fuzzyTranspositions
operator|=
name|fuzzyTranspositions
expr_stmt|;
block|}
DECL|method|isFuzzyTranspositions
specifier|public
name|boolean
name|isFuzzyTranspositions
parameter_list|()
block|{
return|return
name|fuzzyTranspositions
return|;
block|}
DECL|method|setFuzzyMinLength
specifier|public
name|void
name|setFuzzyMinLength
parameter_list|(
name|int
name|fuzzyMinPrefixLength
parameter_list|)
block|{
name|this
operator|.
name|fuzzyMinLength
operator|=
name|fuzzyMinPrefixLength
expr_stmt|;
block|}
DECL|method|getFuzzyMinLength
specifier|public
name|int
name|getFuzzyMinLength
parameter_list|()
block|{
return|return
name|fuzzyMinLength
return|;
block|}
DECL|method|setFuzzyPrefixLength
specifier|public
name|void
name|setFuzzyPrefixLength
parameter_list|(
name|int
name|fuzzyNonPrefixLength
parameter_list|)
block|{
name|this
operator|.
name|fuzzyPrefixLength
operator|=
name|fuzzyNonPrefixLength
expr_stmt|;
block|}
DECL|method|getFuzzyPrefixLength
specifier|public
name|int
name|getFuzzyPrefixLength
parameter_list|()
block|{
return|return
name|fuzzyPrefixLength
return|;
block|}
DECL|method|setFuzzy
specifier|public
name|void
name|setFuzzy
parameter_list|(
name|boolean
name|fuzzy
parameter_list|)
block|{
name|this
operator|.
name|fuzzy
operator|=
name|fuzzy
expr_stmt|;
block|}
DECL|method|isFuzzy
specifier|public
name|boolean
name|isFuzzy
parameter_list|()
block|{
return|return
name|fuzzy
return|;
block|}
DECL|method|setFuzzyUnicodeAware
specifier|public
name|void
name|setFuzzyUnicodeAware
parameter_list|(
name|boolean
name|fuzzyUnicodeAware
parameter_list|)
block|{
name|this
operator|.
name|fuzzyUnicodeAware
operator|=
name|fuzzyUnicodeAware
expr_stmt|;
block|}
DECL|method|isFuzzyUnicodeAware
specifier|public
name|boolean
name|isFuzzyUnicodeAware
parameter_list|()
block|{
return|return
name|fuzzyUnicodeAware
return|;
block|}
DECL|method|setContextQuery
specifier|public
name|void
name|setContextQuery
parameter_list|(
name|List
argument_list|<
name|ContextQuery
argument_list|>
name|queries
parameter_list|)
block|{
name|this
operator|.
name|contextQueries
operator|=
name|queries
expr_stmt|;
block|}
DECL|method|getContextQueries
specifier|public
name|List
argument_list|<
name|ContextQuery
argument_list|>
name|getContextQueries
parameter_list|()
block|{
return|return
name|this
operator|.
name|contextQueries
return|;
block|}
block|}
end_class

end_unit
