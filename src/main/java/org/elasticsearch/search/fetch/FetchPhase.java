begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.fetch
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|fetch
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
name|Lists
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
name|document
operator|.
name|Document
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
name|document
operator|.
name|Fieldable
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
name|IndexReader
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
name|bytes
operator|.
name|BytesArray
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
name|lucene
operator|.
name|document
operator|.
name|ResetFieldSelector
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
name|Index
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
name|FieldMappers
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
name|Uid
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
name|index
operator|.
name|mapper
operator|.
name|internal
operator|.
name|UidFieldMapper
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
name|selector
operator|.
name|AllButSourceFieldSelector
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
name|selector
operator|.
name|FieldMappersFieldSelector
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
name|selector
operator|.
name|UidAndSourceFieldSelector
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
name|selector
operator|.
name|UidFieldSelector
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|TypeMissingException
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
name|SearchHitField
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
name|fetch
operator|.
name|explain
operator|.
name|ExplainFetchSubPhase
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
name|matchedfilters
operator|.
name|MatchedFiltersFetchSubPhase
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
name|partial
operator|.
name|PartialFieldsFetchSubPhase
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
name|script
operator|.
name|ScriptFieldsFetchSubPhase
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
name|version
operator|.
name|VersionFetchSubPhase
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
name|highlight
operator|.
name|HighlightPhase
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
name|InternalSearchHitField
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
name|InternalSearchHits
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

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|FetchPhase
specifier|public
class|class
name|FetchPhase
implements|implements
name|SearchPhase
block|{
DECL|field|fetchSubPhases
specifier|private
specifier|final
name|FetchSubPhase
index|[]
name|fetchSubPhases
decl_stmt|;
annotation|@
name|Inject
DECL|method|FetchPhase
specifier|public
name|FetchPhase
parameter_list|(
name|HighlightPhase
name|highlightPhase
parameter_list|,
name|ScriptFieldsFetchSubPhase
name|scriptFieldsPhase
parameter_list|,
name|PartialFieldsFetchSubPhase
name|partialFieldsPhase
parameter_list|,
name|MatchedFiltersFetchSubPhase
name|matchFiltersPhase
parameter_list|,
name|ExplainFetchSubPhase
name|explainPhase
parameter_list|,
name|VersionFetchSubPhase
name|versionPhase
parameter_list|)
block|{
name|this
operator|.
name|fetchSubPhases
operator|=
operator|new
name|FetchSubPhase
index|[]
block|{
name|scriptFieldsPhase
block|,
name|partialFieldsPhase
block|,
name|matchFiltersPhase
block|,
name|explainPhase
block|,
name|highlightPhase
block|,
name|versionPhase
block|}
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
name|ImmutableMap
operator|.
name|Builder
argument_list|<
name|String
argument_list|,
name|SearchParseElement
argument_list|>
name|parseElements
init|=
name|ImmutableMap
operator|.
name|builder
argument_list|()
decl_stmt|;
name|parseElements
operator|.
name|put
argument_list|(
literal|"fields"
argument_list|,
operator|new
name|FieldsParseElement
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|FetchSubPhase
name|fetchSubPhase
range|:
name|fetchSubPhases
control|)
block|{
name|parseElements
operator|.
name|putAll
argument_list|(
name|fetchSubPhase
operator|.
name|parseElements
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|parseElements
operator|.
name|build
argument_list|()
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
DECL|method|execute
specifier|public
name|void
name|execute
parameter_list|(
name|SearchContext
name|context
parameter_list|)
block|{
name|ResetFieldSelector
name|fieldSelector
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|extractFieldNames
init|=
literal|null
decl_stmt|;
name|boolean
name|sourceRequested
init|=
literal|false
decl_stmt|;
if|if
condition|(
operator|!
name|context
operator|.
name|hasFieldNames
argument_list|()
condition|)
block|{
if|if
condition|(
name|context
operator|.
name|hasPartialFields
argument_list|()
condition|)
block|{
comment|// partial fields need the source, so fetch it, but don't return it
name|fieldSelector
operator|=
operator|new
name|UidAndSourceFieldSelector
argument_list|()
expr_stmt|;
name|sourceRequested
operator|=
literal|false
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|context
operator|.
name|hasScriptFields
argument_list|()
condition|)
block|{
comment|// we ask for script fields, and no field names, don't load the source
name|fieldSelector
operator|=
name|UidFieldSelector
operator|.
name|INSTANCE
expr_stmt|;
name|sourceRequested
operator|=
literal|false
expr_stmt|;
block|}
else|else
block|{
name|fieldSelector
operator|=
operator|new
name|UidAndSourceFieldSelector
argument_list|()
expr_stmt|;
name|sourceRequested
operator|=
literal|true
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|context
operator|.
name|fieldNames
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|fieldSelector
operator|=
name|UidFieldSelector
operator|.
name|INSTANCE
expr_stmt|;
name|sourceRequested
operator|=
literal|false
expr_stmt|;
block|}
else|else
block|{
name|boolean
name|loadAllStored
init|=
literal|false
decl_stmt|;
name|FieldMappersFieldSelector
name|fieldSelectorMapper
init|=
literal|null
decl_stmt|;
for|for
control|(
name|String
name|fieldName
range|:
name|context
operator|.
name|fieldNames
argument_list|()
control|)
block|{
if|if
condition|(
name|fieldName
operator|.
name|equals
argument_list|(
literal|"*"
argument_list|)
condition|)
block|{
name|loadAllStored
operator|=
literal|true
expr_stmt|;
continue|continue;
block|}
if|if
condition|(
name|fieldName
operator|.
name|equals
argument_list|(
name|SourceFieldMapper
operator|.
name|NAME
argument_list|)
condition|)
block|{
name|sourceRequested
operator|=
literal|true
expr_stmt|;
continue|continue;
block|}
name|FieldMappers
name|x
init|=
name|context
operator|.
name|smartNameFieldMappers
argument_list|(
name|fieldName
argument_list|)
decl_stmt|;
if|if
condition|(
name|x
operator|!=
literal|null
operator|&&
name|x
operator|.
name|mapper
argument_list|()
operator|.
name|stored
argument_list|()
condition|)
block|{
if|if
condition|(
name|fieldSelectorMapper
operator|==
literal|null
condition|)
block|{
name|fieldSelectorMapper
operator|=
operator|new
name|FieldMappersFieldSelector
argument_list|()
expr_stmt|;
block|}
name|fieldSelectorMapper
operator|.
name|add
argument_list|(
name|x
argument_list|)
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|extractFieldNames
operator|==
literal|null
condition|)
block|{
name|extractFieldNames
operator|=
name|Lists
operator|.
name|newArrayList
argument_list|()
expr_stmt|;
block|}
name|extractFieldNames
operator|.
name|add
argument_list|(
name|fieldName
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|loadAllStored
condition|)
block|{
if|if
condition|(
name|sourceRequested
operator|||
name|extractFieldNames
operator|!=
literal|null
condition|)
block|{
name|fieldSelector
operator|=
literal|null
expr_stmt|;
comment|// load everything, including _source
block|}
else|else
block|{
name|fieldSelector
operator|=
name|AllButSourceFieldSelector
operator|.
name|INSTANCE
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|fieldSelectorMapper
operator|!=
literal|null
condition|)
block|{
comment|// we are asking specific stored fields, just add the UID and be done
name|fieldSelectorMapper
operator|.
name|add
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|)
expr_stmt|;
if|if
condition|(
name|extractFieldNames
operator|!=
literal|null
operator|||
name|sourceRequested
condition|)
block|{
name|fieldSelectorMapper
operator|.
name|add
argument_list|(
name|SourceFieldMapper
operator|.
name|NAME
argument_list|)
expr_stmt|;
block|}
name|fieldSelector
operator|=
name|fieldSelectorMapper
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|extractFieldNames
operator|!=
literal|null
operator|||
name|sourceRequested
condition|)
block|{
name|fieldSelector
operator|=
operator|new
name|UidAndSourceFieldSelector
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|fieldSelector
operator|=
name|UidFieldSelector
operator|.
name|INSTANCE
expr_stmt|;
block|}
block|}
name|InternalSearchHit
index|[]
name|hits
init|=
operator|new
name|InternalSearchHit
index|[
name|context
operator|.
name|docIdsToLoadSize
argument_list|()
index|]
decl_stmt|;
for|for
control|(
name|int
name|index
init|=
literal|0
init|;
name|index
operator|<
name|context
operator|.
name|docIdsToLoadSize
argument_list|()
condition|;
name|index
operator|++
control|)
block|{
name|int
name|docId
init|=
name|context
operator|.
name|docIdsToLoad
argument_list|()
index|[
name|context
operator|.
name|docIdsToLoadFrom
argument_list|()
operator|+
name|index
index|]
decl_stmt|;
name|Document
name|doc
init|=
name|loadDocument
argument_list|(
name|context
argument_list|,
name|fieldSelector
argument_list|,
name|docId
argument_list|)
decl_stmt|;
name|Uid
name|uid
init|=
name|extractUid
argument_list|(
name|context
argument_list|,
name|doc
argument_list|,
name|fieldSelector
argument_list|)
decl_stmt|;
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
name|uid
operator|.
name|type
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|documentMapper
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|TypeMissingException
argument_list|(
operator|new
name|Index
argument_list|(
name|context
operator|.
name|shardTarget
argument_list|()
operator|.
name|index
argument_list|()
argument_list|)
argument_list|,
name|uid
operator|.
name|type
argument_list|()
argument_list|,
literal|"failed to find type loaded for doc ["
operator|+
name|uid
operator|.
name|id
argument_list|()
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|byte
index|[]
name|source
init|=
name|extractSource
argument_list|(
name|doc
argument_list|,
name|documentMapper
argument_list|)
decl_stmt|;
comment|// get the version
name|InternalSearchHit
name|searchHit
init|=
operator|new
name|InternalSearchHit
argument_list|(
name|docId
argument_list|,
name|uid
operator|.
name|id
argument_list|()
argument_list|,
name|uid
operator|.
name|type
argument_list|()
argument_list|,
name|sourceRequested
condition|?
name|source
else|:
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|hits
index|[
name|index
index|]
operator|=
name|searchHit
expr_stmt|;
for|for
control|(
name|Object
name|oField
range|:
name|doc
operator|.
name|getFields
argument_list|()
control|)
block|{
name|Fieldable
name|field
init|=
operator|(
name|Fieldable
operator|)
name|oField
decl_stmt|;
name|String
name|name
init|=
name|field
operator|.
name|name
argument_list|()
decl_stmt|;
comment|// ignore UID, we handled it above
if|if
condition|(
name|name
operator|.
name|equals
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|)
condition|)
block|{
continue|continue;
block|}
comment|// ignore source, we handled it above
if|if
condition|(
name|name
operator|.
name|equals
argument_list|(
name|SourceFieldMapper
operator|.
name|NAME
argument_list|)
condition|)
block|{
continue|continue;
block|}
name|Object
name|value
init|=
literal|null
decl_stmt|;
name|FieldMappers
name|fieldMappers
init|=
name|documentMapper
operator|.
name|mappers
argument_list|()
operator|.
name|indexName
argument_list|(
name|field
operator|.
name|name
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|fieldMappers
operator|!=
literal|null
condition|)
block|{
name|FieldMapper
name|mapper
init|=
name|fieldMappers
operator|.
name|mapper
argument_list|()
decl_stmt|;
if|if
condition|(
name|mapper
operator|!=
literal|null
condition|)
block|{
name|name
operator|=
name|mapper
operator|.
name|names
argument_list|()
operator|.
name|fullName
argument_list|()
expr_stmt|;
name|value
operator|=
name|mapper
operator|.
name|valueForSearch
argument_list|(
name|field
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|field
operator|.
name|isBinary
argument_list|()
condition|)
block|{
name|value
operator|=
operator|new
name|BytesArray
argument_list|(
name|field
operator|.
name|getBinaryValue
argument_list|()
argument_list|,
name|field
operator|.
name|getBinaryOffset
argument_list|()
argument_list|,
name|field
operator|.
name|getBinaryLength
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|value
operator|=
name|field
operator|.
name|stringValue
argument_list|()
expr_stmt|;
block|}
block|}
if|if
condition|(
name|searchHit
operator|.
name|fieldsOrNull
argument_list|()
operator|==
literal|null
condition|)
block|{
name|searchHit
operator|.
name|fields
argument_list|(
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|SearchHitField
argument_list|>
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|SearchHitField
name|hitField
init|=
name|searchHit
operator|.
name|fields
argument_list|()
operator|.
name|get
argument_list|(
name|name
argument_list|)
decl_stmt|;
if|if
condition|(
name|hitField
operator|==
literal|null
condition|)
block|{
name|hitField
operator|=
operator|new
name|InternalSearchHitField
argument_list|(
name|name
argument_list|,
operator|new
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|searchHit
operator|.
name|fields
argument_list|()
operator|.
name|put
argument_list|(
name|name
argument_list|,
name|hitField
argument_list|)
expr_stmt|;
block|}
name|hitField
operator|.
name|values
argument_list|()
operator|.
name|add
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
name|int
name|readerIndex
init|=
name|context
operator|.
name|searcher
argument_list|()
operator|.
name|readerIndex
argument_list|(
name|docId
argument_list|)
decl_stmt|;
name|IndexReader
name|subReader
init|=
name|context
operator|.
name|searcher
argument_list|()
operator|.
name|subReaders
argument_list|()
index|[
name|readerIndex
index|]
decl_stmt|;
name|int
name|subDoc
init|=
name|docId
operator|-
name|context
operator|.
name|searcher
argument_list|()
operator|.
name|docStarts
argument_list|()
index|[
name|readerIndex
index|]
decl_stmt|;
comment|// go over and extract fields that are not mapped / stored
name|context
operator|.
name|lookup
argument_list|()
operator|.
name|setNextReader
argument_list|(
name|subReader
argument_list|)
expr_stmt|;
name|context
operator|.
name|lookup
argument_list|()
operator|.
name|setNextDocId
argument_list|(
name|subDoc
argument_list|)
expr_stmt|;
if|if
condition|(
name|source
operator|!=
literal|null
condition|)
block|{
name|context
operator|.
name|lookup
argument_list|()
operator|.
name|source
argument_list|()
operator|.
name|setNextSource
argument_list|(
operator|new
name|BytesArray
argument_list|(
name|source
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|extractFieldNames
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|String
name|extractFieldName
range|:
name|extractFieldNames
control|)
block|{
name|Object
name|value
init|=
name|context
operator|.
name|lookup
argument_list|()
operator|.
name|source
argument_list|()
operator|.
name|extractValue
argument_list|(
name|extractFieldName
argument_list|)
decl_stmt|;
if|if
condition|(
name|value
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|searchHit
operator|.
name|fieldsOrNull
argument_list|()
operator|==
literal|null
condition|)
block|{
name|searchHit
operator|.
name|fields
argument_list|(
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|SearchHitField
argument_list|>
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|SearchHitField
name|hitField
init|=
name|searchHit
operator|.
name|fields
argument_list|()
operator|.
name|get
argument_list|(
name|extractFieldName
argument_list|)
decl_stmt|;
if|if
condition|(
name|hitField
operator|==
literal|null
condition|)
block|{
name|hitField
operator|=
operator|new
name|InternalSearchHitField
argument_list|(
name|extractFieldName
argument_list|,
operator|new
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|searchHit
operator|.
name|fields
argument_list|()
operator|.
name|put
argument_list|(
name|extractFieldName
argument_list|,
name|hitField
argument_list|)
expr_stmt|;
block|}
name|hitField
operator|.
name|values
argument_list|()
operator|.
name|add
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
block|}
block|}
for|for
control|(
name|FetchSubPhase
name|fetchSubPhase
range|:
name|fetchSubPhases
control|)
block|{
name|FetchSubPhase
operator|.
name|HitContext
name|hitContext
init|=
operator|new
name|FetchSubPhase
operator|.
name|HitContext
argument_list|()
decl_stmt|;
if|if
condition|(
name|fetchSubPhase
operator|.
name|hitExecutionNeeded
argument_list|(
name|context
argument_list|)
condition|)
block|{
name|hitContext
operator|.
name|reset
argument_list|(
name|searchHit
argument_list|,
name|subReader
argument_list|,
name|subDoc
argument_list|,
name|context
operator|.
name|searcher
argument_list|()
operator|.
name|getIndexReader
argument_list|()
argument_list|,
name|docId
argument_list|,
name|doc
argument_list|)
expr_stmt|;
name|fetchSubPhase
operator|.
name|hitExecute
argument_list|(
name|context
argument_list|,
name|hitContext
argument_list|)
expr_stmt|;
block|}
block|}
block|}
for|for
control|(
name|FetchSubPhase
name|fetchSubPhase
range|:
name|fetchSubPhases
control|)
block|{
if|if
condition|(
name|fetchSubPhase
operator|.
name|hitsExecutionNeeded
argument_list|(
name|context
argument_list|)
condition|)
block|{
name|fetchSubPhase
operator|.
name|hitsExecute
argument_list|(
name|context
argument_list|,
name|hits
argument_list|)
expr_stmt|;
block|}
block|}
name|context
operator|.
name|fetchResult
argument_list|()
operator|.
name|hits
argument_list|(
operator|new
name|InternalSearchHits
argument_list|(
name|hits
argument_list|,
name|context
operator|.
name|queryResult
argument_list|()
operator|.
name|topDocs
argument_list|()
operator|.
name|totalHits
argument_list|,
name|context
operator|.
name|queryResult
argument_list|()
operator|.
name|topDocs
argument_list|()
operator|.
name|getMaxScore
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|extractSource
specifier|private
name|byte
index|[]
name|extractSource
parameter_list|(
name|Document
name|doc
parameter_list|,
name|DocumentMapper
name|documentMapper
parameter_list|)
block|{
name|Fieldable
name|sourceField
init|=
name|doc
operator|.
name|getFieldable
argument_list|(
name|SourceFieldMapper
operator|.
name|NAME
argument_list|)
decl_stmt|;
if|if
condition|(
name|sourceField
operator|!=
literal|null
condition|)
block|{
return|return
name|documentMapper
operator|.
name|sourceMapper
argument_list|()
operator|.
name|nativeValue
argument_list|(
name|sourceField
argument_list|)
return|;
block|}
return|return
literal|null
return|;
block|}
DECL|method|extractUid
specifier|private
name|Uid
name|extractUid
parameter_list|(
name|SearchContext
name|context
parameter_list|,
name|Document
name|doc
parameter_list|,
annotation|@
name|Nullable
name|ResetFieldSelector
name|fieldSelector
parameter_list|)
block|{
name|String
name|sUid
init|=
name|doc
operator|.
name|get
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|)
decl_stmt|;
if|if
condition|(
name|sUid
operator|!=
literal|null
condition|)
block|{
return|return
name|Uid
operator|.
name|createUid
argument_list|(
name|sUid
argument_list|)
return|;
block|}
comment|// no type, nothing to do (should not really happen)
name|List
argument_list|<
name|String
argument_list|>
name|fieldNames
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Fieldable
name|field
range|:
name|doc
operator|.
name|getFields
argument_list|()
control|)
block|{
name|fieldNames
operator|.
name|add
argument_list|(
name|field
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
block|}
throw|throw
operator|new
name|FetchPhaseExecutionException
argument_list|(
name|context
argument_list|,
literal|"Failed to load uid from the index, missing internal _uid field, current fields in the doc ["
operator|+
name|fieldNames
operator|+
literal|"], selector ["
operator|+
name|fieldSelector
operator|+
literal|"]"
argument_list|)
throw|;
block|}
DECL|method|loadDocument
specifier|private
name|Document
name|loadDocument
parameter_list|(
name|SearchContext
name|context
parameter_list|,
annotation|@
name|Nullable
name|ResetFieldSelector
name|fieldSelector
parameter_list|,
name|int
name|docId
parameter_list|)
block|{
try|try
block|{
if|if
condition|(
name|fieldSelector
operator|!=
literal|null
condition|)
name|fieldSelector
operator|.
name|reset
argument_list|()
expr_stmt|;
return|return
name|context
operator|.
name|searcher
argument_list|()
operator|.
name|doc
argument_list|(
name|docId
argument_list|,
name|fieldSelector
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
name|FetchPhaseExecutionException
argument_list|(
name|context
argument_list|,
literal|"Failed to fetch doc id ["
operator|+
name|docId
operator|+
literal|"]"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

