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
name|ElasticsearchException
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
name|component
operator|.
name|AbstractComponent
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
name|inject
operator|.
name|Inject
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
name|search
operator|.
name|SearchParseElement
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
name|SearchPhase
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
name|internal
operator|.
name|SearchContext
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
name|ArrayList
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

begin_import
import|import static
name|java
operator|.
name|util
operator|.
name|Collections
operator|.
name|singletonMap
import|;
end_import

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|SuggestPhase
specifier|public
class|class
name|SuggestPhase
extends|extends
name|AbstractComponent
implements|implements
name|SearchPhase
block|{
DECL|field|parseElements
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|SearchParseElement
argument_list|>
name|parseElements
decl_stmt|;
DECL|field|parseElement
specifier|private
specifier|final
name|SuggestParseElement
name|parseElement
decl_stmt|;
annotation|@
name|Inject
DECL|method|SuggestPhase
specifier|public
name|SuggestPhase
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|SuggestParseElement
name|suggestParseElement
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|parseElement
operator|=
name|suggestParseElement
expr_stmt|;
name|parseElements
operator|=
name|singletonMap
argument_list|(
literal|"suggest"
argument_list|,
name|parseElement
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|parseElements
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|?
extends|extends
name|SearchParseElement
argument_list|>
name|parseElements
parameter_list|()
block|{
return|return
name|parseElements
return|;
block|}
DECL|method|parseElement
specifier|public
name|SuggestParseElement
name|parseElement
parameter_list|()
block|{
return|return
name|parseElement
return|;
block|}
annotation|@
name|Override
DECL|method|preProcess
specifier|public
name|void
name|preProcess
parameter_list|(
name|SearchContext
name|context
parameter_list|)
block|{     }
annotation|@
name|Override
DECL|method|execute
specifier|public
name|void
name|execute
parameter_list|(
name|SearchContext
name|context
parameter_list|)
block|{
specifier|final
name|SuggestionSearchContext
name|suggest
init|=
name|context
operator|.
name|suggest
argument_list|()
decl_stmt|;
if|if
condition|(
name|suggest
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|context
operator|.
name|queryResult
argument_list|()
operator|.
name|suggest
argument_list|(
name|execute
argument_list|(
name|suggest
argument_list|,
name|context
operator|.
name|searcher
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|execute
specifier|public
name|Suggest
name|execute
parameter_list|(
name|SuggestionSearchContext
name|suggest
parameter_list|,
name|IndexSearcher
name|searcher
parameter_list|)
block|{
try|try
block|{
name|CharsRefBuilder
name|spare
init|=
operator|new
name|CharsRefBuilder
argument_list|()
decl_stmt|;
specifier|final
name|List
argument_list|<
name|Suggestion
argument_list|<
name|?
extends|extends
name|Entry
argument_list|<
name|?
extends|extends
name|Option
argument_list|>
argument_list|>
argument_list|>
name|suggestions
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|suggest
operator|.
name|suggestions
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|SuggestionSearchContext
operator|.
name|SuggestionContext
argument_list|>
name|entry
range|:
name|suggest
operator|.
name|suggestions
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|SuggestionSearchContext
operator|.
name|SuggestionContext
name|suggestion
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|Suggester
argument_list|<
name|SuggestionContext
argument_list|>
name|suggester
init|=
name|suggestion
operator|.
name|getSuggester
argument_list|()
decl_stmt|;
name|Suggestion
argument_list|<
name|?
extends|extends
name|Entry
argument_list|<
name|?
extends|extends
name|Option
argument_list|>
argument_list|>
name|result
init|=
name|suggester
operator|.
name|execute
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|suggestion
argument_list|,
name|searcher
argument_list|,
name|spare
argument_list|)
decl_stmt|;
if|if
condition|(
name|result
operator|!=
literal|null
condition|)
block|{
assert|assert
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|equals
argument_list|(
name|result
operator|.
name|name
argument_list|)
assert|;
name|suggestions
operator|.
name|add
argument_list|(
name|result
argument_list|)
expr_stmt|;
block|}
block|}
return|return
operator|new
name|Suggest
argument_list|(
name|Suggest
operator|.
name|Fields
operator|.
name|SUGGEST
argument_list|,
name|suggestions
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticsearchException
argument_list|(
literal|"I/O exception during suggest phase"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

