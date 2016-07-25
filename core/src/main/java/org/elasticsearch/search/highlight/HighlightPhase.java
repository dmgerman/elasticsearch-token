begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|search
operator|.
name|Query
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
name|core
operator|.
name|KeywordFieldMapper
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
name|StringFieldMapper
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
name|TextFieldMapper
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
name|internal
operator|.
name|SourceFieldMapper
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
name|SearchContext
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
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
name|Map
import|;
end_import

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
DECL|field|STANDARD_HIGHLIGHTERS_BY_PRECEDENCE
specifier|private
specifier|static
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|STANDARD_HIGHLIGHTERS_BY_PRECEDENCE
init|=
name|Arrays
operator|.
name|asList
argument_list|(
literal|"fvh"
argument_list|,
literal|"postings"
argument_list|,
literal|"plain"
argument_list|)
decl_stmt|;
DECL|field|highlighters
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Highlighter
argument_list|>
name|highlighters
decl_stmt|;
DECL|method|HighlightPhase
specifier|public
name|HighlightPhase
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Highlighter
argument_list|>
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
block|{
if|if
condition|(
name|context
operator|.
name|highlight
argument_list|()
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|Map
argument_list|<
name|String
argument_list|,
name|HighlightField
argument_list|>
name|highlightFields
init|=
operator|new
name|HashMap
argument_list|<>
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
name|Collection
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
name|Collections
operator|.
name|singletonList
argument_list|(
name|field
operator|.
name|field
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|context
operator|.
name|highlight
argument_list|()
operator|.
name|forceSource
argument_list|(
name|field
argument_list|)
condition|)
block|{
name|SourceFieldMapper
name|sourceFieldMapper
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
operator|.
name|sourceMapper
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|sourceFieldMapper
operator|.
name|enabled
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"source is forced for fields "
operator|+
name|fieldNamesToHighlight
operator|+
literal|" but type ["
operator|+
name|hitContext
operator|.
name|hit
argument_list|()
operator|.
name|type
argument_list|()
operator|+
literal|"] has disabled _source"
argument_list|)
throw|;
block|}
block|}
name|boolean
name|fieldNameContainsWildcards
init|=
name|field
operator|.
name|field
argument_list|()
operator|.
name|contains
argument_list|(
literal|"*"
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|fieldName
range|:
name|fieldNamesToHighlight
control|)
block|{
name|FieldMapper
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
comment|// We should prevent highlighting if a field is anything but a text or keyword field.
comment|// However, someone might implement a custom field type that has text and still want to
comment|// highlight on that. We cannot know in advance if the highlighter will be able to
comment|// highlight such a field and so we do the following:
comment|// If the field is only highlighted because the field matches a wildcard we assume
comment|// it was a mistake and do not process it.
comment|// If the field was explicitly given we assume that whoever issued the query knew
comment|// what they were doing and try to highlight anyway.
if|if
condition|(
name|fieldNameContainsWildcards
condition|)
block|{
if|if
condition|(
name|fieldMapper
operator|.
name|fieldType
argument_list|()
operator|.
name|typeName
argument_list|()
operator|.
name|equals
argument_list|(
name|TextFieldMapper
operator|.
name|CONTENT_TYPE
argument_list|)
operator|==
literal|false
operator|&&
name|fieldMapper
operator|.
name|fieldType
argument_list|()
operator|.
name|typeName
argument_list|()
operator|.
name|equals
argument_list|(
name|KeywordFieldMapper
operator|.
name|CONTENT_TYPE
argument_list|)
operator|==
literal|false
operator|&&
name|fieldMapper
operator|.
name|fieldType
argument_list|()
operator|.
name|typeName
argument_list|()
operator|.
name|equals
argument_list|(
name|StringFieldMapper
operator|.
name|CONTENT_TYPE
argument_list|)
operator|==
literal|false
condition|)
block|{
continue|continue;
block|}
block|}
name|String
name|highlighterType
init|=
name|field
operator|.
name|fieldOptions
argument_list|()
operator|.
name|highlighterType
argument_list|()
decl_stmt|;
if|if
condition|(
name|highlighterType
operator|==
literal|null
condition|)
block|{
for|for
control|(
name|String
name|highlighterCandidate
range|:
name|STANDARD_HIGHLIGHTERS_BY_PRECEDENCE
control|)
block|{
if|if
condition|(
name|highlighters
operator|.
name|get
argument_list|(
name|highlighterCandidate
argument_list|)
operator|.
name|canHighlight
argument_list|(
name|fieldMapper
argument_list|)
condition|)
block|{
name|highlighterType
operator|=
name|highlighterCandidate
expr_stmt|;
break|break;
block|}
block|}
assert|assert
name|highlighterType
operator|!=
literal|null
assert|;
block|}
name|Highlighter
name|highlighter
init|=
name|highlighters
operator|.
name|get
argument_list|(
name|highlighterType
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
name|IllegalArgumentException
argument_list|(
literal|"unknown highlighter type ["
operator|+
name|highlighterType
operator|+
literal|"] for the field ["
operator|+
name|fieldName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|Query
name|highlightQuery
init|=
name|field
operator|.
name|fieldOptions
argument_list|()
operator|.
name|highlightQuery
argument_list|()
operator|==
literal|null
condition|?
name|context
operator|.
name|parsedQuery
argument_list|()
operator|.
name|query
argument_list|()
else|:
name|field
operator|.
name|fieldOptions
argument_list|()
operator|.
name|highlightQuery
argument_list|()
decl_stmt|;
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
if|if
condition|(
operator|(
name|highlighter
operator|.
name|canHighlight
argument_list|(
name|fieldMapper
argument_list|)
operator|==
literal|false
operator|)
operator|&&
name|fieldNameContainsWildcards
condition|)
block|{
comment|// if several fieldnames matched the wildcard then we want to skip those that we cannot highlight
continue|continue;
block|}
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
comment|// TODO: no need to lookup the doc mapper with unambiguous field names? just look at the mapper service
return|return
name|documentMapper
operator|.
name|mappers
argument_list|()
operator|.
name|smartNameFieldMapper
argument_list|(
name|fieldName
argument_list|)
return|;
block|}
block|}
end_class

end_unit

