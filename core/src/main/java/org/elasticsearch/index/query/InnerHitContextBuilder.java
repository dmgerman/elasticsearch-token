begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.query
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|ScriptContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|SearchScript
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
name|builder
operator|.
name|SearchSourceBuilder
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
name|fetch
operator|.
name|subphase
operator|.
name|DocValueFieldsContext
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
name|fetch
operator|.
name|subphase
operator|.
name|InnerHitsContext
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
name|sort
operator|.
name|SortAndFormats
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
name|sort
operator|.
name|SortBuilder
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
name|Optional
import|;
end_import

begin_comment
comment|/**  * A builder for {@link InnerHitsContext.InnerHitSubContext}  */
end_comment

begin_class
DECL|class|InnerHitContextBuilder
specifier|public
specifier|abstract
class|class
name|InnerHitContextBuilder
block|{
DECL|field|query
specifier|protected
specifier|final
name|QueryBuilder
name|query
decl_stmt|;
DECL|field|innerHitBuilder
specifier|protected
specifier|final
name|InnerHitBuilder
name|innerHitBuilder
decl_stmt|;
DECL|field|children
specifier|protected
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|InnerHitContextBuilder
argument_list|>
name|children
decl_stmt|;
DECL|method|InnerHitContextBuilder
specifier|protected
name|InnerHitContextBuilder
parameter_list|(
name|QueryBuilder
name|query
parameter_list|,
name|InnerHitBuilder
name|innerHitBuilder
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|InnerHitContextBuilder
argument_list|>
name|children
parameter_list|)
block|{
name|this
operator|.
name|innerHitBuilder
operator|=
name|innerHitBuilder
expr_stmt|;
name|this
operator|.
name|children
operator|=
name|children
expr_stmt|;
name|this
operator|.
name|query
operator|=
name|query
expr_stmt|;
block|}
DECL|method|build
specifier|public
specifier|abstract
name|void
name|build
parameter_list|(
name|SearchContext
name|parentSearchContext
parameter_list|,
name|InnerHitsContext
name|innerHitsContext
parameter_list|)
throws|throws
name|IOException
function_decl|;
DECL|method|extractInnerHits
specifier|public
specifier|static
name|void
name|extractInnerHits
parameter_list|(
name|QueryBuilder
name|query
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|InnerHitContextBuilder
argument_list|>
name|innerHitBuilders
parameter_list|)
block|{
if|if
condition|(
name|query
operator|instanceof
name|AbstractQueryBuilder
condition|)
block|{
operator|(
operator|(
name|AbstractQueryBuilder
operator|)
name|query
operator|)
operator|.
name|extractInnerHitBuilders
argument_list|(
name|innerHitBuilders
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"provided query builder ["
operator|+
name|query
operator|.
name|getClass
argument_list|()
operator|+
literal|"] class should inherit from AbstractQueryBuilder, but it doesn't"
argument_list|)
throw|;
block|}
block|}
DECL|method|setupInnerHitsContext
specifier|protected
name|void
name|setupInnerHitsContext
parameter_list|(
name|QueryShardContext
name|queryShardContext
parameter_list|,
name|InnerHitsContext
operator|.
name|InnerHitSubContext
name|innerHitsContext
parameter_list|)
throws|throws
name|IOException
block|{
name|innerHitsContext
operator|.
name|from
argument_list|(
name|innerHitBuilder
operator|.
name|getFrom
argument_list|()
argument_list|)
expr_stmt|;
name|innerHitsContext
operator|.
name|size
argument_list|(
name|innerHitBuilder
operator|.
name|getSize
argument_list|()
argument_list|)
expr_stmt|;
name|innerHitsContext
operator|.
name|explain
argument_list|(
name|innerHitBuilder
operator|.
name|isExplain
argument_list|()
argument_list|)
expr_stmt|;
name|innerHitsContext
operator|.
name|version
argument_list|(
name|innerHitBuilder
operator|.
name|isVersion
argument_list|()
argument_list|)
expr_stmt|;
name|innerHitsContext
operator|.
name|trackScores
argument_list|(
name|innerHitBuilder
operator|.
name|isTrackScores
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|innerHitBuilder
operator|.
name|getStoredFieldsContext
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|innerHitsContext
operator|.
name|storedFieldsContext
argument_list|(
name|innerHitBuilder
operator|.
name|getStoredFieldsContext
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|innerHitBuilder
operator|.
name|getDocValueFields
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|innerHitsContext
operator|.
name|docValueFieldsContext
argument_list|(
operator|new
name|DocValueFieldsContext
argument_list|(
name|innerHitBuilder
operator|.
name|getDocValueFields
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|innerHitBuilder
operator|.
name|getScriptFields
argument_list|()
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|SearchSourceBuilder
operator|.
name|ScriptField
name|field
range|:
name|innerHitBuilder
operator|.
name|getScriptFields
argument_list|()
control|)
block|{
name|SearchScript
operator|.
name|LeafFactory
name|searchScript
init|=
name|innerHitsContext
operator|.
name|getQueryShardContext
argument_list|()
operator|.
name|getSearchScript
argument_list|(
name|field
operator|.
name|script
argument_list|()
argument_list|,
name|SearchScript
operator|.
name|CONTEXT
argument_list|)
decl_stmt|;
name|innerHitsContext
operator|.
name|scriptFields
argument_list|()
operator|.
name|add
argument_list|(
operator|new
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|fetch
operator|.
name|subphase
operator|.
name|ScriptFieldsContext
operator|.
name|ScriptField
argument_list|(
name|field
operator|.
name|fieldName
argument_list|()
argument_list|,
name|searchScript
argument_list|,
name|field
operator|.
name|ignoreFailure
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|innerHitBuilder
operator|.
name|getFetchSourceContext
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|innerHitsContext
operator|.
name|fetchSourceContext
argument_list|(
name|innerHitBuilder
operator|.
name|getFetchSourceContext
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|innerHitBuilder
operator|.
name|getSorts
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|Optional
argument_list|<
name|SortAndFormats
argument_list|>
name|optionalSort
init|=
name|SortBuilder
operator|.
name|buildSort
argument_list|(
name|innerHitBuilder
operator|.
name|getSorts
argument_list|()
argument_list|,
name|queryShardContext
argument_list|)
decl_stmt|;
if|if
condition|(
name|optionalSort
operator|.
name|isPresent
argument_list|()
condition|)
block|{
name|innerHitsContext
operator|.
name|sort
argument_list|(
name|optionalSort
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|innerHitBuilder
operator|.
name|getHighlightBuilder
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|innerHitsContext
operator|.
name|highlight
argument_list|(
name|innerHitBuilder
operator|.
name|getHighlightBuilder
argument_list|()
operator|.
name|build
argument_list|(
name|queryShardContext
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|ParsedQuery
name|parsedQuery
init|=
operator|new
name|ParsedQuery
argument_list|(
name|query
operator|.
name|toQuery
argument_list|(
name|queryShardContext
argument_list|)
argument_list|,
name|queryShardContext
operator|.
name|copyNamedQueries
argument_list|()
argument_list|)
decl_stmt|;
name|innerHitsContext
operator|.
name|parsedQuery
argument_list|(
name|parsedQuery
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|InnerHitsContext
operator|.
name|InnerHitSubContext
argument_list|>
name|baseChildren
init|=
name|buildChildInnerHits
argument_list|(
name|innerHitsContext
operator|.
name|parentSearchContext
argument_list|()
argument_list|,
name|children
argument_list|)
decl_stmt|;
name|innerHitsContext
operator|.
name|setChildInnerHits
argument_list|(
name|baseChildren
argument_list|)
expr_stmt|;
block|}
DECL|method|buildChildInnerHits
specifier|private
specifier|static
name|Map
argument_list|<
name|String
argument_list|,
name|InnerHitsContext
operator|.
name|InnerHitSubContext
argument_list|>
name|buildChildInnerHits
parameter_list|(
name|SearchContext
name|parentSearchContext
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|InnerHitContextBuilder
argument_list|>
name|children
parameter_list|)
throws|throws
name|IOException
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|InnerHitsContext
operator|.
name|InnerHitSubContext
argument_list|>
name|childrenInnerHits
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|InnerHitContextBuilder
argument_list|>
name|entry
range|:
name|children
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|InnerHitsContext
name|childInnerHitsContext
init|=
operator|new
name|InnerHitsContext
argument_list|()
decl_stmt|;
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|build
argument_list|(
name|parentSearchContext
argument_list|,
name|childInnerHitsContext
argument_list|)
expr_stmt|;
if|if
condition|(
name|childInnerHitsContext
operator|.
name|getInnerHits
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|childrenInnerHits
operator|.
name|putAll
argument_list|(
name|childInnerHitsContext
operator|.
name|getInnerHits
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|childrenInnerHits
return|;
block|}
block|}
end_class

end_unit

