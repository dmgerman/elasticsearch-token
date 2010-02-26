begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|FieldSelector
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
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
operator|.
name|*
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
name|SearchHit
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
name|Map
import|;
end_import

begin_comment
comment|/**  * @author kimchy (Shay Banon)  */
end_comment

begin_class
DECL|class|FetchPhase
specifier|public
class|class
name|FetchPhase
implements|implements
name|SearchPhase
block|{
DECL|method|parseElements
annotation|@
name|Override
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
literal|"explain"
argument_list|,
operator|new
name|ExplainParseElement
argument_list|()
argument_list|,
literal|"fields"
argument_list|,
operator|new
name|FieldsParseElement
argument_list|()
argument_list|)
return|;
block|}
DECL|method|preProcess
annotation|@
name|Override
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
name|FieldSelector
name|fieldSelector
init|=
name|buildFieldSelectors
argument_list|(
name|context
argument_list|)
decl_stmt|;
name|SearchHit
index|[]
name|hits
init|=
operator|new
name|SearchHit
index|[
name|context
operator|.
name|docIdsToLoad
argument_list|()
operator|.
name|length
index|]
decl_stmt|;
name|int
name|index
init|=
literal|0
decl_stmt|;
for|for
control|(
name|int
name|docId
range|:
name|context
operator|.
name|docIdsToLoad
argument_list|()
control|)
block|{
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
name|type
argument_list|(
name|uid
operator|.
name|type
argument_list|()
argument_list|)
decl_stmt|;
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
name|InternalSearchHit
name|searchHit
init|=
operator|new
name|InternalSearchHit
argument_list|(
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
name|source
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
name|name
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
name|field
operator|.
name|getBinaryValue
argument_list|()
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
name|fields
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
name|doExplanation
argument_list|(
name|context
argument_list|,
name|docId
argument_list|,
name|searchHit
argument_list|)
expr_stmt|;
name|index
operator|++
expr_stmt|;
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
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|doExplanation
specifier|private
name|void
name|doExplanation
parameter_list|(
name|SearchContext
name|context
parameter_list|,
name|int
name|docId
parameter_list|,
name|InternalSearchHit
name|searchHit
parameter_list|)
block|{
if|if
condition|(
name|context
operator|.
name|explain
argument_list|()
condition|)
block|{
try|try
block|{
name|searchHit
operator|.
name|explanation
argument_list|(
name|context
operator|.
name|searcher
argument_list|()
operator|.
name|explain
argument_list|(
name|context
operator|.
name|query
argument_list|()
argument_list|,
name|docId
argument_list|)
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
literal|"Failed to explain doc ["
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
name|byte
index|[]
name|source
init|=
literal|null
decl_stmt|;
name|Fieldable
name|sourceField
init|=
name|doc
operator|.
name|getFieldable
argument_list|(
name|documentMapper
operator|.
name|sourceMapper
argument_list|()
operator|.
name|names
argument_list|()
operator|.
name|indexName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|sourceField
operator|!=
literal|null
condition|)
block|{
name|source
operator|=
name|documentMapper
operator|.
name|sourceMapper
argument_list|()
operator|.
name|value
argument_list|(
name|sourceField
argument_list|)
expr_stmt|;
name|doc
operator|.
name|removeField
argument_list|(
name|documentMapper
operator|.
name|sourceMapper
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
return|return
name|source
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
parameter_list|)
block|{
name|Uid
name|uid
init|=
literal|null
decl_stmt|;
for|for
control|(
name|FieldMapper
name|fieldMapper
range|:
name|context
operator|.
name|mapperService
argument_list|()
operator|.
name|uidFieldMappers
argument_list|()
control|)
block|{
name|String
name|sUid
init|=
name|doc
operator|.
name|get
argument_list|(
name|fieldMapper
operator|.
name|names
argument_list|()
operator|.
name|indexName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|sUid
operator|!=
literal|null
condition|)
block|{
name|uid
operator|=
name|Uid
operator|.
name|createUid
argument_list|(
name|sUid
argument_list|)
expr_stmt|;
name|doc
operator|.
name|removeField
argument_list|(
name|fieldMapper
operator|.
name|names
argument_list|()
operator|.
name|indexName
argument_list|()
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
if|if
condition|(
name|uid
operator|==
literal|null
condition|)
block|{
comment|// no type, nothing to do (should not really happen
throw|throw
operator|new
name|FetchPhaseExecutionException
argument_list|(
name|context
argument_list|,
literal|"Failed to load uid from the index"
argument_list|)
throw|;
block|}
return|return
name|uid
return|;
block|}
DECL|method|loadDocument
specifier|private
name|Document
name|loadDocument
parameter_list|(
name|SearchContext
name|context
parameter_list|,
name|FieldSelector
name|fieldSelector
parameter_list|,
name|int
name|docId
parameter_list|)
block|{
name|Document
name|doc
decl_stmt|;
try|try
block|{
name|doc
operator|=
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
return|return
name|doc
return|;
block|}
DECL|method|buildFieldSelectors
specifier|private
name|FieldSelector
name|buildFieldSelectors
parameter_list|(
name|SearchContext
name|context
parameter_list|)
block|{
if|if
condition|(
name|context
operator|.
name|fieldNames
argument_list|()
operator|==
literal|null
operator|||
name|context
operator|.
name|fieldNames
argument_list|()
operator|.
name|length
operator|==
literal|0
condition|)
block|{
return|return
operator|new
name|UidAndSourceFieldSelector
argument_list|()
return|;
block|}
name|FieldMappersFieldSelector
name|fieldSelector
init|=
operator|new
name|FieldMappersFieldSelector
argument_list|()
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
name|FieldMappers
name|x
init|=
name|context
operator|.
name|mapperService
argument_list|()
operator|.
name|smartNameFieldMappers
argument_list|(
name|fieldName
argument_list|)
decl_stmt|;
if|if
condition|(
name|x
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|FetchPhaseExecutionException
argument_list|(
name|context
argument_list|,
literal|"No mapping for field ["
operator|+
name|fieldName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|fieldSelector
operator|.
name|add
argument_list|(
name|x
argument_list|)
expr_stmt|;
block|}
name|fieldSelector
operator|.
name|add
argument_list|(
name|context
operator|.
name|mapperService
argument_list|()
operator|.
name|uidFieldMappers
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|fieldSelector
return|;
block|}
block|}
end_class

end_unit

