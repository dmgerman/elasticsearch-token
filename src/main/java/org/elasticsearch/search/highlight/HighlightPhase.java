begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.highlight
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|highlight
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableMap
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableSet
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
name|index
operator|.
name|FieldInfo
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticSearchException
import|;
end_import

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
name|regex
operator|.
name|Regex
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
name|index
operator|.
name|mapper
operator|.
name|DocumentMapper
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
name|FieldMapper
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
name|fetch
operator|.
name|FetchSubPhase
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
name|InternalSearchHit
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
name|Set
import|;
end_import

begin_import
import|import static
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Maps
operator|.
name|newHashMap
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|HighlightPhase
specifier|public
class|class
name|HighlightPhase
extends|extends
name|AbstractComponent
implements|implements
name|FetchSubPhase
block|{
DECL|field|highlighters
specifier|private
specifier|final
name|Highlighters
name|highlighters
decl_stmt|;
annotation|@
name|Inject
DECL|method|HighlightPhase
specifier|public
name|HighlightPhase
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|Highlighters
name|highlighters
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|highlighters
operator|=
name|highlighters
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
name|ImmutableMap
operator|.
name|of
argument_list|(
literal|"highlight"
argument_list|,
operator|new
name|HighlighterParseElement
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|hitsExecutionNeeded
specifier|public
name|boolean
name|hitsExecutionNeeded
parameter_list|(
name|SearchContext
name|context
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
DECL|method|hitsExecute
specifier|public
name|void
name|hitsExecute
parameter_list|(
name|SearchContext
name|context
parameter_list|,
name|InternalSearchHit
index|[]
name|hits
parameter_list|)
throws|throws
name|ElasticSearchException
block|{     }
annotation|@
name|Override
DECL|method|hitExecutionNeeded
specifier|public
name|boolean
name|hitExecutionNeeded
parameter_list|(
name|SearchContext
name|context
parameter_list|)
block|{
return|return
name|context
operator|.
name|highlight
argument_list|()
operator|!=
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|hitExecute
specifier|public
name|void
name|hitExecute
parameter_list|(
name|SearchContext
name|context
parameter_list|,
name|HitContext
name|hitContext
parameter_list|)
throws|throws
name|ElasticSearchException
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|HighlightField
argument_list|>
name|highlightFields
init|=
name|newHashMap
argument_list|()
decl_stmt|;
for|for
control|(
name|SearchContextHighlight
operator|.
name|Field
name|field
range|:
name|context
operator|.
name|highlight
argument_list|()
operator|.
name|fields
argument_list|()
control|)
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|fieldNamesToHighlight
decl_stmt|;
if|if
condition|(
name|Regex
operator|.
name|isSimpleMatchPattern
argument_list|(
name|field
operator|.
name|field
argument_list|()
argument_list|)
condition|)
block|{
name|DocumentMapper
name|documentMapper
init|=
name|context
operator|.
name|mapperService
argument_list|()
operator|.
name|documentMapper
argument_list|(
name|hitContext
operator|.
name|hit
argument_list|()
operator|.
name|type
argument_list|()
argument_list|)
decl_stmt|;
name|fieldNamesToHighlight
operator|=
name|documentMapper
operator|.
name|mappers
argument_list|()
operator|.
name|simpleMatchToFullName
argument_list|(
name|field
operator|.
name|field
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|fieldNamesToHighlight
operator|=
name|ImmutableSet
operator|.
name|of
argument_list|(
name|field
operator|.
name|field
argument_list|()
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|String
name|fieldName
range|:
name|fieldNamesToHighlight
control|)
block|{
name|FieldMapper
argument_list|<
name|?
argument_list|>
name|fieldMapper
init|=
name|getMapperForField
argument_list|(
name|fieldName
argument_list|,
name|context
argument_list|,
name|hitContext
argument_list|)
decl_stmt|;
if|if
condition|(
name|fieldMapper
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
if|if
condition|(
name|field
operator|.
name|highlighterType
argument_list|()
operator|==
literal|null
condition|)
block|{
name|boolean
name|useFastVectorHighlighter
init|=
name|fieldMapper
operator|.
name|fieldType
argument_list|()
operator|.
name|storeTermVectors
argument_list|()
operator|&&
name|fieldMapper
operator|.
name|fieldType
argument_list|()
operator|.
name|storeTermVectorOffsets
argument_list|()
operator|&&
name|fieldMapper
operator|.
name|fieldType
argument_list|()
operator|.
name|storeTermVectorPositions
argument_list|()
decl_stmt|;
if|if
condition|(
name|useFastVectorHighlighter
condition|)
block|{
name|field
operator|.
name|highlighterType
argument_list|(
literal|"fvh"
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|fieldMapper
operator|.
name|fieldType
argument_list|()
operator|.
name|indexOptions
argument_list|()
operator|==
name|FieldInfo
operator|.
name|IndexOptions
operator|.
name|DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS
condition|)
block|{
name|field
operator|.
name|highlighterType
argument_list|(
literal|"postings"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|field
operator|.
name|highlighterType
argument_list|(
literal|"plain"
argument_list|)
expr_stmt|;
block|}
block|}
name|Highlighter
name|highlighter
init|=
name|highlighters
operator|.
name|get
argument_list|(
name|field
operator|.
name|highlighterType
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|highlighter
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ElasticSearchIllegalArgumentException
argument_list|(
literal|"unknown highlighter type ["
operator|+
name|field
operator|.
name|highlighterType
argument_list|()
operator|+
literal|"] for the field ["
operator|+
name|fieldName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|HighlighterContext
operator|.
name|HighlightQuery
name|highlightQuery
decl_stmt|;
if|if
condition|(
name|field
operator|.
name|highlightQuery
argument_list|()
operator|==
literal|null
condition|)
block|{
name|highlightQuery
operator|=
operator|new
name|HighlighterContext
operator|.
name|HighlightQuery
argument_list|(
name|context
operator|.
name|parsedQuery
argument_list|()
operator|.
name|query
argument_list|()
argument_list|,
name|context
operator|.
name|query
argument_list|()
argument_list|,
name|context
operator|.
name|queryRewritten
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|highlightQuery
operator|=
operator|new
name|HighlighterContext
operator|.
name|HighlightQuery
argument_list|(
name|field
operator|.
name|highlightQuery
argument_list|()
argument_list|,
name|field
operator|.
name|highlightQuery
argument_list|()
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
name|HighlighterContext
name|highlighterContext
init|=
operator|new
name|HighlighterContext
argument_list|(
name|fieldName
argument_list|,
name|field
argument_list|,
name|fieldMapper
argument_list|,
name|context
argument_list|,
name|hitContext
argument_list|,
name|highlightQuery
argument_list|)
decl_stmt|;
name|HighlightField
name|highlightField
init|=
name|highlighter
operator|.
name|highlight
argument_list|(
name|highlighterContext
argument_list|)
decl_stmt|;
if|if
condition|(
name|highlightField
operator|!=
literal|null
condition|)
block|{
name|highlightFields
operator|.
name|put
argument_list|(
name|highlightField
operator|.
name|name
argument_list|()
argument_list|,
name|highlightField
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|hitContext
operator|.
name|hit
argument_list|()
operator|.
name|highlightFields
argument_list|(
name|highlightFields
argument_list|)
expr_stmt|;
block|}
DECL|method|getMapperForField
specifier|private
name|FieldMapper
argument_list|<
name|?
argument_list|>
name|getMapperForField
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|SearchContext
name|searchContext
parameter_list|,
name|HitContext
name|hitContext
parameter_list|)
block|{
name|DocumentMapper
name|documentMapper
init|=
name|searchContext
operator|.
name|mapperService
argument_list|()
operator|.
name|documentMapper
argument_list|(
name|hitContext
operator|.
name|hit
argument_list|()
operator|.
name|type
argument_list|()
argument_list|)
decl_stmt|;
name|FieldMapper
argument_list|<
name|?
argument_list|>
name|mapper
init|=
name|documentMapper
operator|.
name|mappers
argument_list|()
operator|.
name|smartNameFieldMapper
argument_list|(
name|fieldName
argument_list|)
decl_stmt|;
if|if
condition|(
name|mapper
operator|==
literal|null
condition|)
block|{
name|MapperService
operator|.
name|SmartNameFieldMappers
name|fullMapper
init|=
name|searchContext
operator|.
name|mapperService
argument_list|()
operator|.
name|smartName
argument_list|(
name|fieldName
argument_list|)
decl_stmt|;
if|if
condition|(
name|fullMapper
operator|==
literal|null
operator|||
operator|!
name|fullMapper
operator|.
name|hasDocMapper
argument_list|()
operator|||
name|fullMapper
operator|.
name|docMapper
argument_list|()
operator|.
name|type
argument_list|()
operator|.
name|equals
argument_list|(
name|hitContext
operator|.
name|hit
argument_list|()
operator|.
name|type
argument_list|()
argument_list|)
condition|)
block|{
return|return
literal|null
return|;
block|}
name|mapper
operator|=
name|fullMapper
operator|.
name|mapper
argument_list|()
expr_stmt|;
block|}
return|return
name|mapper
return|;
block|}
block|}
end_class

end_unit

