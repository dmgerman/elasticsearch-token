begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.search.child
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|search
operator|.
name|child
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
name|*
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
name|*
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
name|Bits
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
name|ToStringUtils
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
name|lease
operator|.
name|Releasable
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
name|lease
operator|.
name|Releasables
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
name|docset
operator|.
name|DocIdSets
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
name|NoopCollector
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
name|common
operator|.
name|util
operator|.
name|BigArrays
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
name|util
operator|.
name|FloatArray
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
name|util
operator|.
name|LongHash
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
name|fielddata
operator|.
name|IndexParentChildFieldData
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
name|fielddata
operator|.
name|plain
operator|.
name|ParentChildIndexFieldData
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
name|SearchContext
operator|.
name|Lifetime
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
name|Set
import|;
end_import

begin_comment
comment|/**  * A query implementation that executes the wrapped parent query and  * connects the matching parent docs to the related child documents  * using the {@link ParentChildIndexFieldData}.  */
end_comment

begin_class
DECL|class|ParentQuery
specifier|public
class|class
name|ParentQuery
extends|extends
name|Query
block|{
DECL|field|parentChildIndexFieldData
specifier|private
specifier|final
name|ParentChildIndexFieldData
name|parentChildIndexFieldData
decl_stmt|;
DECL|field|originalParentQuery
specifier|private
name|Query
name|originalParentQuery
decl_stmt|;
DECL|field|parentType
specifier|private
specifier|final
name|String
name|parentType
decl_stmt|;
DECL|field|childrenFilter
specifier|private
specifier|final
name|Filter
name|childrenFilter
decl_stmt|;
DECL|field|rewrittenParentQuery
specifier|private
name|Query
name|rewrittenParentQuery
decl_stmt|;
DECL|field|rewriteIndexReader
specifier|private
name|IndexReader
name|rewriteIndexReader
decl_stmt|;
DECL|method|ParentQuery
specifier|public
name|ParentQuery
parameter_list|(
name|ParentChildIndexFieldData
name|parentChildIndexFieldData
parameter_list|,
name|Query
name|parentQuery
parameter_list|,
name|String
name|parentType
parameter_list|,
name|Filter
name|childrenFilter
parameter_list|)
block|{
name|this
operator|.
name|parentChildIndexFieldData
operator|=
name|parentChildIndexFieldData
expr_stmt|;
name|this
operator|.
name|originalParentQuery
operator|=
name|parentQuery
expr_stmt|;
name|this
operator|.
name|parentType
operator|=
name|parentType
expr_stmt|;
name|this
operator|.
name|childrenFilter
operator|=
name|childrenFilter
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|equals
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|obj
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
name|obj
operator|==
literal|null
operator|||
name|obj
operator|.
name|getClass
argument_list|()
operator|!=
name|this
operator|.
name|getClass
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
name|ParentQuery
name|that
init|=
operator|(
name|ParentQuery
operator|)
name|obj
decl_stmt|;
if|if
condition|(
operator|!
name|originalParentQuery
operator|.
name|equals
argument_list|(
name|that
operator|.
name|originalParentQuery
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
operator|!
name|parentType
operator|.
name|equals
argument_list|(
name|that
operator|.
name|parentType
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|getBoost
argument_list|()
operator|!=
name|that
operator|.
name|getBoost
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
annotation|@
name|Override
DECL|method|hashCode
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
name|int
name|result
init|=
name|originalParentQuery
operator|.
name|hashCode
argument_list|()
decl_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
name|parentType
operator|.
name|hashCode
argument_list|()
expr_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
name|Float
operator|.
name|floatToIntBits
argument_list|(
name|getBoost
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|(
name|String
name|field
parameter_list|)
block|{
return|return
literal|"ParentQuery["
operator|+
name|parentType
operator|+
literal|"]("
operator|+
name|originalParentQuery
operator|.
name|toString
argument_list|(
name|field
argument_list|)
operator|+
literal|')'
operator|+
name|ToStringUtils
operator|.
name|boost
argument_list|(
name|getBoost
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
comment|// See TopChildrenQuery#rewrite
DECL|method|rewrite
specifier|public
name|Query
name|rewrite
parameter_list|(
name|IndexReader
name|reader
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|rewrittenParentQuery
operator|==
literal|null
condition|)
block|{
name|rewriteIndexReader
operator|=
name|reader
expr_stmt|;
name|rewrittenParentQuery
operator|=
name|originalParentQuery
operator|.
name|rewrite
argument_list|(
name|reader
argument_list|)
expr_stmt|;
block|}
return|return
name|this
return|;
block|}
annotation|@
name|Override
DECL|method|extractTerms
specifier|public
name|void
name|extractTerms
parameter_list|(
name|Set
argument_list|<
name|Term
argument_list|>
name|terms
parameter_list|)
block|{
name|rewrittenParentQuery
operator|.
name|extractTerms
argument_list|(
name|terms
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|clone
specifier|public
name|Query
name|clone
parameter_list|()
block|{
name|ParentQuery
name|q
init|=
operator|(
name|ParentQuery
operator|)
name|super
operator|.
name|clone
argument_list|()
decl_stmt|;
name|q
operator|.
name|originalParentQuery
operator|=
name|originalParentQuery
operator|.
name|clone
argument_list|()
expr_stmt|;
if|if
condition|(
name|q
operator|.
name|rewrittenParentQuery
operator|!=
literal|null
condition|)
block|{
name|q
operator|.
name|rewrittenParentQuery
operator|=
name|rewrittenParentQuery
operator|.
name|clone
argument_list|()
expr_stmt|;
block|}
return|return
name|q
return|;
block|}
annotation|@
name|Override
DECL|method|createWeight
specifier|public
name|Weight
name|createWeight
parameter_list|(
name|IndexSearcher
name|searcher
parameter_list|)
throws|throws
name|IOException
block|{
name|SearchContext
name|sc
init|=
name|SearchContext
operator|.
name|current
argument_list|()
decl_stmt|;
name|ChildWeight
name|childWeight
decl_stmt|;
name|boolean
name|releaseCollectorResource
init|=
literal|true
decl_stmt|;
name|ParentOrdAndScoreCollector
name|collector
init|=
literal|null
decl_stmt|;
name|IndexParentChildFieldData
name|globalIfd
init|=
name|parentChildIndexFieldData
operator|.
name|loadGlobal
argument_list|(
name|searcher
operator|.
name|getIndexReader
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|globalIfd
operator|==
literal|null
condition|)
block|{
comment|// No docs of the specified type don't exist on this shard
return|return
name|Queries
operator|.
name|newMatchNoDocsQuery
argument_list|()
operator|.
name|createWeight
argument_list|(
name|searcher
argument_list|)
return|;
block|}
try|try
block|{
assert|assert
name|rewrittenParentQuery
operator|!=
literal|null
assert|;
assert|assert
name|rewriteIndexReader
operator|==
name|searcher
operator|.
name|getIndexReader
argument_list|()
operator|:
literal|"not equal, rewriteIndexReader="
operator|+
name|rewriteIndexReader
operator|+
literal|" searcher.getIndexReader()="
operator|+
name|searcher
operator|.
name|getIndexReader
argument_list|()
assert|;
specifier|final
name|Query
name|parentQuery
init|=
name|rewrittenParentQuery
decl_stmt|;
name|collector
operator|=
operator|new
name|ParentOrdAndScoreCollector
argument_list|(
name|sc
argument_list|,
name|globalIfd
argument_list|,
name|parentType
argument_list|)
expr_stmt|;
name|IndexSearcher
name|indexSearcher
init|=
operator|new
name|IndexSearcher
argument_list|(
name|sc
operator|.
name|searcher
argument_list|()
operator|.
name|getIndexReader
argument_list|()
argument_list|)
decl_stmt|;
name|indexSearcher
operator|.
name|setSimilarity
argument_list|(
name|searcher
operator|.
name|getSimilarity
argument_list|()
argument_list|)
expr_stmt|;
name|indexSearcher
operator|.
name|search
argument_list|(
name|parentQuery
argument_list|,
name|collector
argument_list|)
expr_stmt|;
if|if
condition|(
name|collector
operator|.
name|parentCount
argument_list|()
operator|==
literal|0
condition|)
block|{
return|return
name|Queries
operator|.
name|newMatchNoDocsQuery
argument_list|()
operator|.
name|createWeight
argument_list|(
name|searcher
argument_list|)
return|;
block|}
name|childWeight
operator|=
operator|new
name|ChildWeight
argument_list|(
name|parentQuery
operator|.
name|createWeight
argument_list|(
name|searcher
argument_list|)
argument_list|,
name|childrenFilter
argument_list|,
name|collector
argument_list|,
name|globalIfd
argument_list|)
expr_stmt|;
name|releaseCollectorResource
operator|=
literal|false
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|releaseCollectorResource
condition|)
block|{
comment|// either if we run into an exception or if we return early
name|Releasables
operator|.
name|close
argument_list|(
name|collector
argument_list|)
expr_stmt|;
block|}
block|}
name|sc
operator|.
name|addReleasable
argument_list|(
name|collector
argument_list|,
name|Lifetime
operator|.
name|COLLECTION
argument_list|)
expr_stmt|;
return|return
name|childWeight
return|;
block|}
DECL|class|ParentOrdAndScoreCollector
specifier|private
specifier|static
class|class
name|ParentOrdAndScoreCollector
extends|extends
name|NoopCollector
implements|implements
name|Releasable
block|{
DECL|field|parentIdxs
specifier|private
specifier|final
name|LongHash
name|parentIdxs
decl_stmt|;
DECL|field|scores
specifier|private
name|FloatArray
name|scores
decl_stmt|;
DECL|field|globalIfd
specifier|private
specifier|final
name|IndexParentChildFieldData
name|globalIfd
decl_stmt|;
DECL|field|bigArrays
specifier|private
specifier|final
name|BigArrays
name|bigArrays
decl_stmt|;
DECL|field|parentType
specifier|private
specifier|final
name|String
name|parentType
decl_stmt|;
DECL|field|scorer
specifier|private
name|Scorer
name|scorer
decl_stmt|;
DECL|field|values
specifier|private
name|SortedDocValues
name|values
decl_stmt|;
DECL|method|ParentOrdAndScoreCollector
name|ParentOrdAndScoreCollector
parameter_list|(
name|SearchContext
name|searchContext
parameter_list|,
name|IndexParentChildFieldData
name|globalIfd
parameter_list|,
name|String
name|parentType
parameter_list|)
block|{
name|this
operator|.
name|bigArrays
operator|=
name|searchContext
operator|.
name|bigArrays
argument_list|()
expr_stmt|;
name|this
operator|.
name|parentIdxs
operator|=
operator|new
name|LongHash
argument_list|(
literal|512
argument_list|,
name|bigArrays
argument_list|)
expr_stmt|;
name|this
operator|.
name|scores
operator|=
name|bigArrays
operator|.
name|newFloatArray
argument_list|(
literal|512
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|this
operator|.
name|globalIfd
operator|=
name|globalIfd
expr_stmt|;
name|this
operator|.
name|parentType
operator|=
name|parentType
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|collect
specifier|public
name|void
name|collect
parameter_list|(
name|int
name|doc
parameter_list|)
throws|throws
name|IOException
block|{
comment|// It can happen that for particular segment no document exist for an specific type. This prevents NPE
if|if
condition|(
name|values
operator|!=
literal|null
condition|)
block|{
name|long
name|globalOrdinal
init|=
name|values
operator|.
name|getOrd
argument_list|(
name|doc
argument_list|)
decl_stmt|;
if|if
condition|(
name|globalOrdinal
operator|!=
name|SortedSetDocValues
operator|.
name|NO_MORE_ORDS
condition|)
block|{
name|long
name|parentIdx
init|=
name|parentIdxs
operator|.
name|add
argument_list|(
name|globalOrdinal
argument_list|)
decl_stmt|;
if|if
condition|(
name|parentIdx
operator|>=
literal|0
condition|)
block|{
name|scores
operator|=
name|bigArrays
operator|.
name|grow
argument_list|(
name|scores
argument_list|,
name|parentIdx
operator|+
literal|1
argument_list|)
expr_stmt|;
name|scores
operator|.
name|set
argument_list|(
name|parentIdx
argument_list|,
name|scorer
operator|.
name|score
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
assert|assert
literal|false
operator|:
literal|"parent id should only match once, since there can only be one parent doc"
assert|;
block|}
block|}
block|}
block|}
annotation|@
name|Override
DECL|method|setScorer
specifier|public
name|void
name|setScorer
parameter_list|(
name|Scorer
name|scorer
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|scorer
operator|=
name|scorer
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doSetNextReader
specifier|protected
name|void
name|doSetNextReader
parameter_list|(
name|LeafReaderContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|values
operator|=
name|globalIfd
operator|.
name|load
argument_list|(
name|context
argument_list|)
operator|.
name|getOrdinalsValues
argument_list|(
name|parentType
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|ElasticsearchException
block|{
name|Releasables
operator|.
name|close
argument_list|(
name|parentIdxs
argument_list|,
name|scores
argument_list|)
expr_stmt|;
block|}
DECL|method|parentCount
specifier|public
name|long
name|parentCount
parameter_list|()
block|{
return|return
name|parentIdxs
operator|.
name|size
argument_list|()
return|;
block|}
block|}
DECL|class|ChildWeight
specifier|private
class|class
name|ChildWeight
extends|extends
name|Weight
block|{
DECL|field|parentWeight
specifier|private
specifier|final
name|Weight
name|parentWeight
decl_stmt|;
DECL|field|childrenFilter
specifier|private
specifier|final
name|Filter
name|childrenFilter
decl_stmt|;
DECL|field|parentIdxs
specifier|private
specifier|final
name|LongHash
name|parentIdxs
decl_stmt|;
DECL|field|scores
specifier|private
specifier|final
name|FloatArray
name|scores
decl_stmt|;
DECL|field|globalIfd
specifier|private
specifier|final
name|IndexParentChildFieldData
name|globalIfd
decl_stmt|;
DECL|method|ChildWeight
specifier|private
name|ChildWeight
parameter_list|(
name|Weight
name|parentWeight
parameter_list|,
name|Filter
name|childrenFilter
parameter_list|,
name|ParentOrdAndScoreCollector
name|collector
parameter_list|,
name|IndexParentChildFieldData
name|globalIfd
parameter_list|)
block|{
name|this
operator|.
name|parentWeight
operator|=
name|parentWeight
expr_stmt|;
name|this
operator|.
name|childrenFilter
operator|=
name|childrenFilter
expr_stmt|;
name|this
operator|.
name|parentIdxs
operator|=
name|collector
operator|.
name|parentIdxs
expr_stmt|;
name|this
operator|.
name|scores
operator|=
name|collector
operator|.
name|scores
expr_stmt|;
name|this
operator|.
name|globalIfd
operator|=
name|globalIfd
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|explain
specifier|public
name|Explanation
name|explain
parameter_list|(
name|LeafReaderContext
name|context
parameter_list|,
name|int
name|doc
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|Explanation
argument_list|(
name|getBoost
argument_list|()
argument_list|,
literal|"not implemented yet..."
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|getQuery
specifier|public
name|Query
name|getQuery
parameter_list|()
block|{
return|return
name|ParentQuery
operator|.
name|this
return|;
block|}
annotation|@
name|Override
DECL|method|getValueForNormalization
specifier|public
name|float
name|getValueForNormalization
parameter_list|()
throws|throws
name|IOException
block|{
name|float
name|sum
init|=
name|parentWeight
operator|.
name|getValueForNormalization
argument_list|()
decl_stmt|;
name|sum
operator|*=
name|getBoost
argument_list|()
operator|*
name|getBoost
argument_list|()
expr_stmt|;
return|return
name|sum
return|;
block|}
annotation|@
name|Override
DECL|method|normalize
specifier|public
name|void
name|normalize
parameter_list|(
name|float
name|norm
parameter_list|,
name|float
name|topLevelBoost
parameter_list|)
block|{         }
annotation|@
name|Override
DECL|method|scorer
specifier|public
name|Scorer
name|scorer
parameter_list|(
name|LeafReaderContext
name|context
parameter_list|,
name|Bits
name|acceptDocs
parameter_list|)
throws|throws
name|IOException
block|{
name|DocIdSet
name|childrenDocSet
init|=
name|childrenFilter
operator|.
name|getDocIdSet
argument_list|(
name|context
argument_list|,
name|acceptDocs
argument_list|)
decl_stmt|;
if|if
condition|(
name|DocIdSets
operator|.
name|isEmpty
argument_list|(
name|childrenDocSet
argument_list|)
condition|)
block|{
return|return
literal|null
return|;
block|}
name|SortedDocValues
name|bytesValues
init|=
name|globalIfd
operator|.
name|load
argument_list|(
name|context
argument_list|)
operator|.
name|getOrdinalsValues
argument_list|(
name|parentType
argument_list|)
decl_stmt|;
if|if
condition|(
name|bytesValues
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
operator|new
name|ChildScorer
argument_list|(
name|this
argument_list|,
name|parentIdxs
argument_list|,
name|scores
argument_list|,
name|childrenDocSet
operator|.
name|iterator
argument_list|()
argument_list|,
name|bytesValues
argument_list|)
return|;
block|}
block|}
DECL|class|ChildScorer
specifier|private
specifier|static
class|class
name|ChildScorer
extends|extends
name|Scorer
block|{
DECL|field|parentIdxs
specifier|private
specifier|final
name|LongHash
name|parentIdxs
decl_stmt|;
DECL|field|scores
specifier|private
specifier|final
name|FloatArray
name|scores
decl_stmt|;
DECL|field|childrenIterator
specifier|private
specifier|final
name|DocIdSetIterator
name|childrenIterator
decl_stmt|;
DECL|field|ordinals
specifier|private
specifier|final
name|SortedDocValues
name|ordinals
decl_stmt|;
DECL|field|currentChildDoc
specifier|private
name|int
name|currentChildDoc
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|currentScore
specifier|private
name|float
name|currentScore
decl_stmt|;
DECL|method|ChildScorer
name|ChildScorer
parameter_list|(
name|Weight
name|weight
parameter_list|,
name|LongHash
name|parentIdxs
parameter_list|,
name|FloatArray
name|scores
parameter_list|,
name|DocIdSetIterator
name|childrenIterator
parameter_list|,
name|SortedDocValues
name|ordinals
parameter_list|)
block|{
name|super
argument_list|(
name|weight
argument_list|)
expr_stmt|;
name|this
operator|.
name|parentIdxs
operator|=
name|parentIdxs
expr_stmt|;
name|this
operator|.
name|scores
operator|=
name|scores
expr_stmt|;
name|this
operator|.
name|childrenIterator
operator|=
name|childrenIterator
expr_stmt|;
name|this
operator|.
name|ordinals
operator|=
name|ordinals
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|score
specifier|public
name|float
name|score
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|currentScore
return|;
block|}
annotation|@
name|Override
DECL|method|freq
specifier|public
name|int
name|freq
parameter_list|()
throws|throws
name|IOException
block|{
comment|// We don't have the original child query hit info here...
comment|// But the freq of the children could be collector and returned here, but makes this Scorer more expensive.
return|return
literal|1
return|;
block|}
annotation|@
name|Override
DECL|method|docID
specifier|public
name|int
name|docID
parameter_list|()
block|{
return|return
name|currentChildDoc
return|;
block|}
annotation|@
name|Override
DECL|method|nextDoc
specifier|public
name|int
name|nextDoc
parameter_list|()
throws|throws
name|IOException
block|{
while|while
condition|(
literal|true
condition|)
block|{
name|currentChildDoc
operator|=
name|childrenIterator
operator|.
name|nextDoc
argument_list|()
expr_stmt|;
if|if
condition|(
name|currentChildDoc
operator|==
name|DocIdSetIterator
operator|.
name|NO_MORE_DOCS
condition|)
block|{
return|return
name|currentChildDoc
return|;
block|}
name|int
name|globalOrdinal
init|=
operator|(
name|int
operator|)
name|ordinals
operator|.
name|getOrd
argument_list|(
name|currentChildDoc
argument_list|)
decl_stmt|;
if|if
condition|(
name|globalOrdinal
operator|<
literal|0
condition|)
block|{
continue|continue;
block|}
specifier|final
name|long
name|parentIdx
init|=
name|parentIdxs
operator|.
name|find
argument_list|(
name|globalOrdinal
argument_list|)
decl_stmt|;
if|if
condition|(
name|parentIdx
operator|!=
operator|-
literal|1
condition|)
block|{
name|currentScore
operator|=
name|scores
operator|.
name|get
argument_list|(
name|parentIdx
argument_list|)
expr_stmt|;
return|return
name|currentChildDoc
return|;
block|}
block|}
block|}
annotation|@
name|Override
DECL|method|advance
specifier|public
name|int
name|advance
parameter_list|(
name|int
name|target
parameter_list|)
throws|throws
name|IOException
block|{
name|currentChildDoc
operator|=
name|childrenIterator
operator|.
name|advance
argument_list|(
name|target
argument_list|)
expr_stmt|;
if|if
condition|(
name|currentChildDoc
operator|==
name|DocIdSetIterator
operator|.
name|NO_MORE_DOCS
condition|)
block|{
return|return
name|currentChildDoc
return|;
block|}
name|int
name|globalOrdinal
init|=
operator|(
name|int
operator|)
name|ordinals
operator|.
name|getOrd
argument_list|(
name|currentChildDoc
argument_list|)
decl_stmt|;
if|if
condition|(
name|globalOrdinal
operator|<
literal|0
condition|)
block|{
return|return
name|nextDoc
argument_list|()
return|;
block|}
specifier|final
name|long
name|parentIdx
init|=
name|parentIdxs
operator|.
name|find
argument_list|(
name|globalOrdinal
argument_list|)
decl_stmt|;
if|if
condition|(
name|parentIdx
operator|!=
operator|-
literal|1
condition|)
block|{
name|currentScore
operator|=
name|scores
operator|.
name|get
argument_list|(
name|parentIdx
argument_list|)
expr_stmt|;
return|return
name|currentChildDoc
return|;
block|}
else|else
block|{
return|return
name|nextDoc
argument_list|()
return|;
block|}
block|}
annotation|@
name|Override
DECL|method|cost
specifier|public
name|long
name|cost
parameter_list|()
block|{
return|return
name|childrenIterator
operator|.
name|cost
argument_list|()
return|;
block|}
block|}
block|}
end_class

end_unit

