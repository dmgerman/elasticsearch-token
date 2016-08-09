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
name|document
operator|.
name|CompletionQuery
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
name|index
operator|.
name|mapper
operator|.
name|core
operator|.
name|CompletionFieldMapper2x
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
name|ContextMapping
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
name|ContextMappings
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
name|completion2x
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
DECL|class|CompletionSuggestionContext
specifier|public
class|class
name|CompletionSuggestionContext
extends|extends
name|SuggestionSearchContext
operator|.
name|SuggestionContext
block|{
DECL|method|CompletionSuggestionContext
specifier|protected
name|CompletionSuggestionContext
parameter_list|(
name|QueryShardContext
name|shardContext
parameter_list|)
block|{
name|super
argument_list|(
name|CompletionSuggester
operator|.
name|INSTANCE
argument_list|,
name|shardContext
argument_list|)
expr_stmt|;
block|}
DECL|field|fieldType
specifier|private
name|CompletionFieldMapper
operator|.
name|CompletionFieldType
name|fieldType
decl_stmt|;
DECL|field|fuzzyOptions
specifier|private
name|FuzzyOptions
name|fuzzyOptions
decl_stmt|;
DECL|field|regexOptions
specifier|private
name|RegexOptions
name|regexOptions
decl_stmt|;
DECL|field|queryContexts
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|ContextMapping
operator|.
name|InternalQueryContext
argument_list|>
argument_list|>
name|queryContexts
init|=
name|Collections
operator|.
name|emptyMap
argument_list|()
decl_stmt|;
DECL|field|fieldType2x
specifier|private
name|CompletionFieldMapper2x
operator|.
name|CompletionFieldType
name|fieldType2x
decl_stmt|;
DECL|field|contextQueries
specifier|private
name|List
argument_list|<
name|ContextQuery
argument_list|>
name|contextQueries
decl_stmt|;
DECL|method|getFieldType
name|CompletionFieldMapper
operator|.
name|CompletionFieldType
name|getFieldType
parameter_list|()
block|{
return|return
name|this
operator|.
name|fieldType
return|;
block|}
DECL|method|getFieldType2x
name|CompletionFieldMapper2x
operator|.
name|CompletionFieldType
name|getFieldType2x
parameter_list|()
block|{
return|return
name|this
operator|.
name|fieldType2x
return|;
block|}
DECL|method|setFieldType
name|void
name|setFieldType
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
DECL|method|setRegexOptions
name|void
name|setRegexOptions
parameter_list|(
name|RegexOptions
name|regexOptions
parameter_list|)
block|{
name|this
operator|.
name|regexOptions
operator|=
name|regexOptions
expr_stmt|;
block|}
DECL|method|setFuzzyOptions
name|void
name|setFuzzyOptions
parameter_list|(
name|FuzzyOptions
name|fuzzyOptions
parameter_list|)
block|{
name|this
operator|.
name|fuzzyOptions
operator|=
name|fuzzyOptions
expr_stmt|;
block|}
DECL|method|setQueryContexts
name|void
name|setQueryContexts
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|ContextMapping
operator|.
name|InternalQueryContext
argument_list|>
argument_list|>
name|queryContexts
parameter_list|)
block|{
name|this
operator|.
name|queryContexts
operator|=
name|queryContexts
expr_stmt|;
block|}
DECL|method|getFuzzyOptions
specifier|public
name|FuzzyOptions
name|getFuzzyOptions
parameter_list|()
block|{
return|return
name|fuzzyOptions
return|;
block|}
DECL|method|getRegexOptions
specifier|public
name|RegexOptions
name|getRegexOptions
parameter_list|()
block|{
return|return
name|regexOptions
return|;
block|}
DECL|method|getQueryContexts
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|ContextMapping
operator|.
name|InternalQueryContext
argument_list|>
argument_list|>
name|getQueryContexts
parameter_list|()
block|{
return|return
name|queryContexts
return|;
block|}
DECL|method|toQuery
name|CompletionQuery
name|toQuery
parameter_list|()
block|{
name|CompletionFieldMapper
operator|.
name|CompletionFieldType
name|fieldType
init|=
name|getFieldType
argument_list|()
decl_stmt|;
specifier|final
name|CompletionQuery
name|query
decl_stmt|;
if|if
condition|(
name|getPrefix
argument_list|()
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|fuzzyOptions
operator|!=
literal|null
condition|)
block|{
name|query
operator|=
name|fieldType
operator|.
name|fuzzyQuery
argument_list|(
name|getPrefix
argument_list|()
operator|.
name|utf8ToString
argument_list|()
argument_list|,
name|Fuzziness
operator|.
name|fromEdits
argument_list|(
name|fuzzyOptions
operator|.
name|getEditDistance
argument_list|()
argument_list|)
argument_list|,
name|fuzzyOptions
operator|.
name|getFuzzyPrefixLength
argument_list|()
argument_list|,
name|fuzzyOptions
operator|.
name|getFuzzyMinLength
argument_list|()
argument_list|,
name|fuzzyOptions
operator|.
name|getMaxDeterminizedStates
argument_list|()
argument_list|,
name|fuzzyOptions
operator|.
name|isTranspositions
argument_list|()
argument_list|,
name|fuzzyOptions
operator|.
name|isUnicodeAware
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|query
operator|=
name|fieldType
operator|.
name|prefixQuery
argument_list|(
name|getPrefix
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|getRegex
argument_list|()
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|fuzzyOptions
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"can not use 'fuzzy' options with 'regex"
argument_list|)
throw|;
block|}
if|if
condition|(
name|regexOptions
operator|==
literal|null
condition|)
block|{
name|regexOptions
operator|=
name|RegexOptions
operator|.
name|builder
argument_list|()
operator|.
name|build
argument_list|()
expr_stmt|;
block|}
name|query
operator|=
name|fieldType
operator|.
name|regexpQuery
argument_list|(
name|getRegex
argument_list|()
argument_list|,
name|regexOptions
operator|.
name|getFlagsValue
argument_list|()
argument_list|,
name|regexOptions
operator|.
name|getMaxDeterminizedStates
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
literal|"'prefix' or 'regex' must be defined"
argument_list|)
throw|;
block|}
if|if
condition|(
name|fieldType
operator|.
name|hasContextMappings
argument_list|()
condition|)
block|{
name|ContextMappings
name|contextMappings
init|=
name|fieldType
operator|.
name|getContextMappings
argument_list|()
decl_stmt|;
return|return
name|contextMappings
operator|.
name|toContextQuery
argument_list|(
name|query
argument_list|,
name|queryContexts
argument_list|)
return|;
block|}
return|return
name|query
return|;
block|}
DECL|method|setFieldType2x
specifier|public
name|void
name|setFieldType2x
parameter_list|(
name|CompletionFieldMapper2x
operator|.
name|CompletionFieldType
name|type
parameter_list|)
block|{
name|this
operator|.
name|fieldType2x
operator|=
name|type
expr_stmt|;
block|}
DECL|method|setContextQueries
specifier|public
name|void
name|setContextQueries
parameter_list|(
name|List
argument_list|<
name|ContextQuery
argument_list|>
name|contextQueries
parameter_list|)
block|{
name|this
operator|.
name|contextQueries
operator|=
name|contextQueries
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
name|contextQueries
return|;
block|}
block|}
end_class

end_unit

