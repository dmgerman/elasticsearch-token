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
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|index
operator|.
name|AtomicReaderContext
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
name|ReaderUtil
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
name|text
operator|.
name|StringAndBytesText
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
name|fieldvisitor
operator|.
name|CustomFieldsVisitor
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
name|fieldvisitor
operator|.
name|FieldsVisitor
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
name|fieldvisitor
operator|.
name|JustUidFieldsVisitor
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
name|fieldvisitor
operator|.
name|UidAndSourceFieldsVisitor
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
name|*
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
name|Lists
operator|.
name|newArrayList
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
name|FieldsVisitor
name|fieldsVisitor
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
name|fieldsVisitor
operator|=
operator|new
name|UidAndSourceFieldsVisitor
argument_list|()
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
name|fieldsVisitor
operator|=
operator|new
name|JustUidFieldsVisitor
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|sourceRequested
operator|=
literal|true
expr_stmt|;
name|fieldsVisitor
operator|=
operator|new
name|UidAndSourceFieldsVisitor
argument_list|()
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
name|fieldsVisitor
operator|=
operator|new
name|JustUidFieldsVisitor
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|boolean
name|loadAllStored
init|=
literal|false
decl_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|fieldNames
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
name|fieldType
argument_list|()
operator|.
name|stored
argument_list|()
condition|)
block|{
if|if
condition|(
name|fieldNames
operator|==
literal|null
condition|)
block|{
name|fieldNames
operator|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|()
expr_stmt|;
block|}
name|fieldNames
operator|.
name|add
argument_list|(
name|x
operator|.
name|mapper
argument_list|()
operator|.
name|names
argument_list|()
operator|.
name|indexName
argument_list|()
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
name|fieldsVisitor
operator|=
operator|new
name|CustomFieldsVisitor
argument_list|(
literal|true
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|// load everything, including _source
block|}
else|else
block|{
name|fieldsVisitor
operator|=
operator|new
name|CustomFieldsVisitor
argument_list|(
literal|true
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|fieldNames
operator|!=
literal|null
condition|)
block|{
name|boolean
name|loadSource
init|=
name|extractFieldNames
operator|!=
literal|null
operator|||
name|sourceRequested
decl_stmt|;
name|fieldsVisitor
operator|=
operator|new
name|CustomFieldsVisitor
argument_list|(
name|fieldNames
argument_list|,
name|loadSource
argument_list|)
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
name|fieldsVisitor
operator|=
operator|new
name|UidAndSourceFieldsVisitor
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|fieldsVisitor
operator|=
operator|new
name|JustUidFieldsVisitor
argument_list|()
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
name|loadStoredFields
argument_list|(
name|context
argument_list|,
name|fieldsVisitor
argument_list|,
name|docId
argument_list|)
expr_stmt|;
name|fieldsVisitor
operator|.
name|postProcess
argument_list|(
name|context
operator|.
name|mapperService
argument_list|()
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|SearchHitField
argument_list|>
name|searchFields
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|fieldsVisitor
operator|.
name|fields
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|searchFields
operator|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|SearchHitField
argument_list|>
argument_list|(
name|fieldsVisitor
operator|.
name|fields
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|Object
argument_list|>
argument_list|>
name|entry
range|:
name|fieldsVisitor
operator|.
name|fields
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|searchFields
operator|.
name|put
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
operator|new
name|InternalSearchHitField
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
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
name|fieldsVisitor
operator|.
name|uid
argument_list|()
operator|.
name|type
argument_list|()
argument_list|)
decl_stmt|;
name|Text
name|typeText
decl_stmt|;
if|if
condition|(
name|documentMapper
operator|==
literal|null
condition|)
block|{
name|typeText
operator|=
operator|new
name|StringAndBytesText
argument_list|(
name|fieldsVisitor
operator|.
name|uid
argument_list|()
operator|.
name|type
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|typeText
operator|=
name|documentMapper
operator|.
name|typeText
argument_list|()
expr_stmt|;
block|}
name|InternalSearchHit
name|searchHit
init|=
operator|new
name|InternalSearchHit
argument_list|(
name|docId
argument_list|,
name|fieldsVisitor
operator|.
name|uid
argument_list|()
operator|.
name|id
argument_list|()
argument_list|,
name|typeText
argument_list|,
name|sourceRequested
condition|?
name|fieldsVisitor
operator|.
name|source
argument_list|()
else|:
literal|null
argument_list|,
name|searchFields
argument_list|)
decl_stmt|;
name|hits
index|[
name|index
index|]
operator|=
name|searchHit
expr_stmt|;
name|int
name|readerIndex
init|=
name|ReaderUtil
operator|.
name|subIndex
argument_list|(
name|docId
argument_list|,
name|context
operator|.
name|searcher
argument_list|()
operator|.
name|getIndexReader
argument_list|()
operator|.
name|leaves
argument_list|()
argument_list|)
decl_stmt|;
name|AtomicReaderContext
name|subReaderContext
init|=
name|context
operator|.
name|searcher
argument_list|()
operator|.
name|getIndexReader
argument_list|()
operator|.
name|leaves
argument_list|()
operator|.
name|get
argument_list|(
name|readerIndex
argument_list|)
decl_stmt|;
name|int
name|subDoc
init|=
name|docId
operator|-
name|subReaderContext
operator|.
name|docBase
decl_stmt|;
comment|// go over and extract fields that are not mapped / stored
name|context
operator|.
name|lookup
argument_list|()
operator|.
name|setNextReader
argument_list|(
name|subReaderContext
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
name|searchHit
operator|.
name|source
argument_list|()
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
name|searchHit
operator|.
name|source
argument_list|()
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
name|subReaderContext
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
name|fieldsVisitor
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
DECL|method|loadStoredFields
specifier|private
name|void
name|loadStoredFields
parameter_list|(
name|SearchContext
name|context
parameter_list|,
name|FieldsVisitor
name|fieldVisitor
parameter_list|,
name|int
name|docId
parameter_list|)
block|{
name|fieldVisitor
operator|.
name|reset
argument_list|()
expr_stmt|;
try|try
block|{
name|context
operator|.
name|searcher
argument_list|()
operator|.
name|doc
argument_list|(
name|docId
argument_list|,
name|fieldVisitor
argument_list|)
expr_stmt|;
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

