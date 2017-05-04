begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.fetch.subphase
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|fetch
operator|.
name|subphase
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
name|index
operator|.
name|Term
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
name|search
operator|.
name|BooleanClause
operator|.
name|Occur
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
name|search
operator|.
name|BooleanQuery
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
name|search
operator|.
name|DocValuesTermsQuery
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
name|search
operator|.
name|Query
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
name|search
operator|.
name|TermQuery
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
name|search
operator|.
name|TopDocs
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
name|search
operator|.
name|TopDocsCollector
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
name|search
operator|.
name|TopFieldCollector
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
name|search
operator|.
name|TopScoreDocCollector
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
name|search
operator|.
name|join
operator|.
name|BitSetProducer
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
name|search
operator|.
name|join
operator|.
name|ParentChildrenBlockJoinQuery
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ExceptionsHelper
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
name|Lucene
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
name|search
operator|.
name|Queries
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
name|MapperService
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
name|ObjectMapper
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
name|ParentFieldMapper
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
name|UidFieldMapper
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
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|internal
operator|.
name|SubSearchContext
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
name|Objects
import|;
end_import

begin_class
DECL|class|InnerHitsContext
specifier|public
specifier|final
class|class
name|InnerHitsContext
block|{
DECL|field|innerHits
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|BaseInnerHits
argument_list|>
name|innerHits
decl_stmt|;
DECL|method|InnerHitsContext
specifier|public
name|InnerHitsContext
parameter_list|()
block|{
name|this
operator|.
name|innerHits
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
expr_stmt|;
block|}
DECL|method|InnerHitsContext
specifier|public
name|InnerHitsContext
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|BaseInnerHits
argument_list|>
name|innerHits
parameter_list|)
block|{
name|this
operator|.
name|innerHits
operator|=
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|innerHits
argument_list|)
expr_stmt|;
block|}
DECL|method|getInnerHits
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|BaseInnerHits
argument_list|>
name|getInnerHits
parameter_list|()
block|{
return|return
name|innerHits
return|;
block|}
DECL|method|addInnerHitDefinition
specifier|public
name|void
name|addInnerHitDefinition
parameter_list|(
name|BaseInnerHits
name|innerHit
parameter_list|)
block|{
if|if
condition|(
name|innerHits
operator|.
name|containsKey
argument_list|(
name|innerHit
operator|.
name|getName
argument_list|()
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"inner_hit definition with the name ["
operator|+
name|innerHit
operator|.
name|getName
argument_list|()
operator|+
literal|"] already exists. Use a different inner_hit name or define one explicitly"
argument_list|)
throw|;
block|}
name|innerHits
operator|.
name|put
argument_list|(
name|innerHit
operator|.
name|getName
argument_list|()
argument_list|,
name|innerHit
argument_list|)
expr_stmt|;
block|}
DECL|class|BaseInnerHits
specifier|public
specifier|abstract
specifier|static
class|class
name|BaseInnerHits
extends|extends
name|SubSearchContext
block|{
DECL|field|name
specifier|private
specifier|final
name|String
name|name
decl_stmt|;
DECL|field|childInnerHits
specifier|private
name|InnerHitsContext
name|childInnerHits
decl_stmt|;
DECL|method|BaseInnerHits
specifier|protected
name|BaseInnerHits
parameter_list|(
name|String
name|name
parameter_list|,
name|SearchContext
name|context
parameter_list|)
block|{
name|super
argument_list|(
name|context
argument_list|)
expr_stmt|;
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
block|}
DECL|method|topDocs
specifier|public
specifier|abstract
name|TopDocs
name|topDocs
parameter_list|(
name|SearchContext
name|context
parameter_list|,
name|FetchSubPhase
operator|.
name|HitContext
name|hitContext
parameter_list|)
throws|throws
name|IOException
function_decl|;
DECL|method|getName
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|name
return|;
block|}
annotation|@
name|Override
DECL|method|innerHits
specifier|public
name|InnerHitsContext
name|innerHits
parameter_list|()
block|{
return|return
name|childInnerHits
return|;
block|}
DECL|method|setChildInnerHits
specifier|public
name|void
name|setChildInnerHits
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|InnerHitsContext
operator|.
name|BaseInnerHits
argument_list|>
name|childInnerHits
parameter_list|)
block|{
name|this
operator|.
name|childInnerHits
operator|=
operator|new
name|InnerHitsContext
argument_list|(
name|childInnerHits
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|NestedInnerHits
specifier|public
specifier|static
specifier|final
class|class
name|NestedInnerHits
extends|extends
name|BaseInnerHits
block|{
DECL|field|parentObjectMapper
specifier|private
specifier|final
name|ObjectMapper
name|parentObjectMapper
decl_stmt|;
DECL|field|childObjectMapper
specifier|private
specifier|final
name|ObjectMapper
name|childObjectMapper
decl_stmt|;
DECL|method|NestedInnerHits
specifier|public
name|NestedInnerHits
parameter_list|(
name|String
name|name
parameter_list|,
name|SearchContext
name|context
parameter_list|,
name|ObjectMapper
name|parentObjectMapper
parameter_list|,
name|ObjectMapper
name|childObjectMapper
parameter_list|)
block|{
name|super
argument_list|(
name|name
operator|!=
literal|null
condition|?
name|name
else|:
name|childObjectMapper
operator|.
name|fullPath
argument_list|()
argument_list|,
name|context
argument_list|)
expr_stmt|;
name|this
operator|.
name|parentObjectMapper
operator|=
name|parentObjectMapper
expr_stmt|;
name|this
operator|.
name|childObjectMapper
operator|=
name|childObjectMapper
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|topDocs
specifier|public
name|TopDocs
name|topDocs
parameter_list|(
name|SearchContext
name|context
parameter_list|,
name|FetchSubPhase
operator|.
name|HitContext
name|hitContext
parameter_list|)
throws|throws
name|IOException
block|{
name|Query
name|rawParentFilter
decl_stmt|;
if|if
condition|(
name|parentObjectMapper
operator|==
literal|null
condition|)
block|{
name|rawParentFilter
operator|=
name|Queries
operator|.
name|newNonNestedFilter
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|rawParentFilter
operator|=
name|parentObjectMapper
operator|.
name|nestedTypeFilter
argument_list|()
expr_stmt|;
block|}
name|BitSetProducer
name|parentFilter
init|=
name|context
operator|.
name|bitsetFilterCache
argument_list|()
operator|.
name|getBitSetProducer
argument_list|(
name|rawParentFilter
argument_list|)
decl_stmt|;
name|Query
name|childFilter
init|=
name|childObjectMapper
operator|.
name|nestedTypeFilter
argument_list|()
decl_stmt|;
name|int
name|parentDocId
init|=
name|hitContext
operator|.
name|readerContext
argument_list|()
operator|.
name|docBase
operator|+
name|hitContext
operator|.
name|docId
argument_list|()
decl_stmt|;
name|Query
name|q
init|=
name|Queries
operator|.
name|filtered
argument_list|(
name|query
argument_list|()
argument_list|,
operator|new
name|ParentChildrenBlockJoinQuery
argument_list|(
name|parentFilter
argument_list|,
name|childFilter
argument_list|,
name|parentDocId
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
return|return
operator|new
name|TopDocs
argument_list|(
name|context
operator|.
name|searcher
argument_list|()
operator|.
name|count
argument_list|(
name|q
argument_list|)
argument_list|,
name|Lucene
operator|.
name|EMPTY_SCORE_DOCS
argument_list|,
literal|0
argument_list|)
return|;
block|}
else|else
block|{
name|int
name|topN
init|=
name|Math
operator|.
name|min
argument_list|(
name|from
argument_list|()
operator|+
name|size
argument_list|()
argument_list|,
name|context
operator|.
name|searcher
argument_list|()
operator|.
name|getIndexReader
argument_list|()
operator|.
name|maxDoc
argument_list|()
argument_list|)
decl_stmt|;
name|TopDocsCollector
argument_list|<
name|?
argument_list|>
name|topDocsCollector
decl_stmt|;
if|if
condition|(
name|sort
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|topDocsCollector
operator|=
name|TopFieldCollector
operator|.
name|create
argument_list|(
name|sort
argument_list|()
operator|.
name|sort
argument_list|,
name|topN
argument_list|,
literal|true
argument_list|,
name|trackScores
argument_list|()
argument_list|,
name|trackScores
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|topDocsCollector
operator|=
name|TopScoreDocCollector
operator|.
name|create
argument_list|(
name|topN
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|context
operator|.
name|searcher
argument_list|()
operator|.
name|search
argument_list|(
name|q
argument_list|,
name|topDocsCollector
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|clearReleasables
argument_list|(
name|Lifetime
operator|.
name|COLLECTION
argument_list|)
expr_stmt|;
block|}
return|return
name|topDocsCollector
operator|.
name|topDocs
argument_list|(
name|from
argument_list|()
argument_list|,
name|size
argument_list|()
argument_list|)
return|;
block|}
block|}
block|}
DECL|class|ParentChildInnerHits
specifier|public
specifier|static
specifier|final
class|class
name|ParentChildInnerHits
extends|extends
name|BaseInnerHits
block|{
DECL|field|mapperService
specifier|private
specifier|final
name|MapperService
name|mapperService
decl_stmt|;
DECL|field|documentMapper
specifier|private
specifier|final
name|DocumentMapper
name|documentMapper
decl_stmt|;
DECL|method|ParentChildInnerHits
specifier|public
name|ParentChildInnerHits
parameter_list|(
name|String
name|name
parameter_list|,
name|SearchContext
name|context
parameter_list|,
name|MapperService
name|mapperService
parameter_list|,
name|DocumentMapper
name|documentMapper
parameter_list|)
block|{
name|super
argument_list|(
name|name
operator|!=
literal|null
condition|?
name|name
else|:
name|documentMapper
operator|.
name|type
argument_list|()
argument_list|,
name|context
argument_list|)
expr_stmt|;
name|this
operator|.
name|mapperService
operator|=
name|mapperService
expr_stmt|;
name|this
operator|.
name|documentMapper
operator|=
name|documentMapper
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|topDocs
specifier|public
name|TopDocs
name|topDocs
parameter_list|(
name|SearchContext
name|context
parameter_list|,
name|FetchSubPhase
operator|.
name|HitContext
name|hitContext
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|Query
name|hitQuery
decl_stmt|;
if|if
condition|(
name|isParentHit
argument_list|(
name|hitContext
operator|.
name|hit
argument_list|()
argument_list|)
condition|)
block|{
name|String
name|field
init|=
name|ParentFieldMapper
operator|.
name|joinField
argument_list|(
name|hitContext
operator|.
name|hit
argument_list|()
operator|.
name|getType
argument_list|()
argument_list|)
decl_stmt|;
name|hitQuery
operator|=
operator|new
name|DocValuesTermsQuery
argument_list|(
name|field
argument_list|,
name|hitContext
operator|.
name|hit
argument_list|()
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|isChildHit
argument_list|(
name|hitContext
operator|.
name|hit
argument_list|()
argument_list|)
condition|)
block|{
name|DocumentMapper
name|hitDocumentMapper
init|=
name|mapperService
operator|.
name|documentMapper
argument_list|(
name|hitContext
operator|.
name|hit
argument_list|()
operator|.
name|getType
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|String
name|parentType
init|=
name|hitDocumentMapper
operator|.
name|parentFieldMapper
argument_list|()
operator|.
name|type
argument_list|()
decl_stmt|;
name|SearchHitField
name|parentField
init|=
name|hitContext
operator|.
name|hit
argument_list|()
operator|.
name|field
argument_list|(
name|ParentFieldMapper
operator|.
name|NAME
argument_list|)
decl_stmt|;
if|if
condition|(
name|parentField
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"All children must have a _parent"
argument_list|)
throw|;
block|}
name|hitQuery
operator|=
operator|new
name|TermQuery
argument_list|(
operator|new
name|Term
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|,
name|Uid
operator|.
name|createUid
argument_list|(
name|parentType
argument_list|,
name|parentField
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
return|return
name|Lucene
operator|.
name|EMPTY_TOP_DOCS
return|;
block|}
name|BooleanQuery
name|q
init|=
operator|new
name|BooleanQuery
operator|.
name|Builder
argument_list|()
operator|.
name|add
argument_list|(
name|query
argument_list|()
argument_list|,
name|Occur
operator|.
name|MUST
argument_list|)
comment|// Only include docs that have the current hit as parent
operator|.
name|add
argument_list|(
name|hitQuery
argument_list|,
name|Occur
operator|.
name|FILTER
argument_list|)
comment|// Only include docs that have this inner hits type
operator|.
name|add
argument_list|(
name|documentMapper
operator|.
name|typeFilter
argument_list|(
name|context
operator|.
name|getQueryShardContext
argument_list|()
argument_list|)
argument_list|,
name|Occur
operator|.
name|FILTER
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
if|if
condition|(
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
specifier|final
name|int
name|count
init|=
name|context
operator|.
name|searcher
argument_list|()
operator|.
name|count
argument_list|(
name|q
argument_list|)
decl_stmt|;
return|return
operator|new
name|TopDocs
argument_list|(
name|count
argument_list|,
name|Lucene
operator|.
name|EMPTY_SCORE_DOCS
argument_list|,
literal|0
argument_list|)
return|;
block|}
else|else
block|{
name|int
name|topN
init|=
name|Math
operator|.
name|min
argument_list|(
name|from
argument_list|()
operator|+
name|size
argument_list|()
argument_list|,
name|context
operator|.
name|searcher
argument_list|()
operator|.
name|getIndexReader
argument_list|()
operator|.
name|maxDoc
argument_list|()
argument_list|)
decl_stmt|;
name|TopDocsCollector
name|topDocsCollector
decl_stmt|;
if|if
condition|(
name|sort
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|topDocsCollector
operator|=
name|TopFieldCollector
operator|.
name|create
argument_list|(
name|sort
argument_list|()
operator|.
name|sort
argument_list|,
name|topN
argument_list|,
literal|true
argument_list|,
name|trackScores
argument_list|()
argument_list|,
name|trackScores
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|topDocsCollector
operator|=
name|TopScoreDocCollector
operator|.
name|create
argument_list|(
name|topN
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|context
operator|.
name|searcher
argument_list|()
operator|.
name|search
argument_list|(
name|q
argument_list|,
name|topDocsCollector
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|clearReleasables
argument_list|(
name|Lifetime
operator|.
name|COLLECTION
argument_list|)
expr_stmt|;
block|}
return|return
name|topDocsCollector
operator|.
name|topDocs
argument_list|(
name|from
argument_list|()
argument_list|,
name|size
argument_list|()
argument_list|)
return|;
block|}
block|}
DECL|method|isParentHit
specifier|private
name|boolean
name|isParentHit
parameter_list|(
name|SearchHit
name|hit
parameter_list|)
block|{
return|return
name|hit
operator|.
name|getType
argument_list|()
operator|.
name|equals
argument_list|(
name|documentMapper
operator|.
name|parentFieldMapper
argument_list|()
operator|.
name|type
argument_list|()
argument_list|)
return|;
block|}
DECL|method|isChildHit
specifier|private
name|boolean
name|isChildHit
parameter_list|(
name|SearchHit
name|hit
parameter_list|)
block|{
name|DocumentMapper
name|hitDocumentMapper
init|=
name|mapperService
operator|.
name|documentMapper
argument_list|(
name|hit
operator|.
name|getType
argument_list|()
argument_list|)
decl_stmt|;
return|return
name|documentMapper
operator|.
name|type
argument_list|()
operator|.
name|equals
argument_list|(
name|hitDocumentMapper
operator|.
name|parentFieldMapper
argument_list|()
operator|.
name|type
argument_list|()
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

