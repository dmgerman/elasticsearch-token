begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|gnu
operator|.
name|trove
operator|.
name|map
operator|.
name|TObjectFloatMap
import|;
end_import

begin_import
import|import
name|gnu
operator|.
name|trove
operator|.
name|map
operator|.
name|TObjectIntMap
import|;
end_import

begin_import
import|import
name|gnu
operator|.
name|trove
operator|.
name|map
operator|.
name|hash
operator|.
name|TObjectFloatHashMap
import|;
end_import

begin_import
import|import
name|gnu
operator|.
name|trove
operator|.
name|map
operator|.
name|hash
operator|.
name|TObjectIntHashMap
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
name|IndexReader
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
name|ElasticSearchIllegalStateException
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
name|CacheRecycler
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
name|HashedBytesArray
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
name|index
operator|.
name|cache
operator|.
name|id
operator|.
name|IdReaderTypeCache
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
name|Set
import|;
end_import

begin_comment
comment|/**  * A query implementation that executes the wrapped child query and  * connects the matching child docs to the related parent documents  * using the {@link IdReaderTypeCache}.  */
end_comment

begin_class
DECL|class|ChildrenQuery
specifier|public
class|class
name|ChildrenQuery
extends|extends
name|Query
implements|implements
name|SearchContext
operator|.
name|Rewrite
block|{
DECL|field|searchContext
specifier|private
specifier|final
name|SearchContext
name|searchContext
decl_stmt|;
DECL|field|parentType
specifier|private
specifier|final
name|String
name|parentType
decl_stmt|;
DECL|field|childType
specifier|private
specifier|final
name|String
name|childType
decl_stmt|;
DECL|field|parentFilter
specifier|private
specifier|final
name|Filter
name|parentFilter
decl_stmt|;
DECL|field|scoreType
specifier|private
specifier|final
name|ScoreType
name|scoreType
decl_stmt|;
DECL|field|childQuery
specifier|private
specifier|final
name|Query
name|childQuery
decl_stmt|;
DECL|field|uidToScore
specifier|private
name|TObjectFloatHashMap
argument_list|<
name|HashedBytesArray
argument_list|>
name|uidToScore
decl_stmt|;
DECL|field|uidToCount
specifier|private
name|TObjectIntHashMap
argument_list|<
name|HashedBytesArray
argument_list|>
name|uidToCount
decl_stmt|;
DECL|method|ChildrenQuery
specifier|public
name|ChildrenQuery
parameter_list|(
name|SearchContext
name|searchContext
parameter_list|,
name|String
name|parentType
parameter_list|,
name|String
name|childType
parameter_list|,
name|Filter
name|parentFilter
parameter_list|,
name|Query
name|childQuery
parameter_list|,
name|ScoreType
name|scoreType
parameter_list|)
block|{
name|this
operator|.
name|searchContext
operator|=
name|searchContext
expr_stmt|;
name|this
operator|.
name|parentType
operator|=
name|parentType
expr_stmt|;
name|this
operator|.
name|childType
operator|=
name|childType
expr_stmt|;
name|this
operator|.
name|parentFilter
operator|=
name|parentFilter
expr_stmt|;
name|this
operator|.
name|childQuery
operator|=
name|childQuery
expr_stmt|;
name|this
operator|.
name|scoreType
operator|=
name|scoreType
expr_stmt|;
block|}
DECL|method|ChildrenQuery
specifier|private
name|ChildrenQuery
parameter_list|(
name|ChildrenQuery
name|unProcessedQuery
parameter_list|,
name|Query
name|rewrittenChildQuery
parameter_list|)
block|{
name|this
operator|.
name|searchContext
operator|=
name|unProcessedQuery
operator|.
name|searchContext
expr_stmt|;
name|this
operator|.
name|parentType
operator|=
name|unProcessedQuery
operator|.
name|parentType
expr_stmt|;
name|this
operator|.
name|childType
operator|=
name|unProcessedQuery
operator|.
name|childType
expr_stmt|;
name|this
operator|.
name|parentFilter
operator|=
name|unProcessedQuery
operator|.
name|parentFilter
expr_stmt|;
name|this
operator|.
name|scoreType
operator|=
name|unProcessedQuery
operator|.
name|scoreType
expr_stmt|;
name|this
operator|.
name|childQuery
operator|=
name|rewrittenChildQuery
expr_stmt|;
name|this
operator|.
name|uidToScore
operator|=
name|unProcessedQuery
operator|.
name|uidToScore
expr_stmt|;
name|this
operator|.
name|uidToCount
operator|=
name|unProcessedQuery
operator|.
name|uidToCount
expr_stmt|;
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
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"ChildrenQuery["
argument_list|)
operator|.
name|append
argument_list|(
name|childType
argument_list|)
operator|.
name|append
argument_list|(
literal|"/"
argument_list|)
operator|.
name|append
argument_list|(
name|parentType
argument_list|)
operator|.
name|append
argument_list|(
literal|"]("
argument_list|)
operator|.
name|append
argument_list|(
name|childQuery
operator|.
name|toString
argument_list|(
name|field
argument_list|)
argument_list|)
operator|.
name|append
argument_list|(
literal|')'
argument_list|)
operator|.
name|append
argument_list|(
name|ToStringUtils
operator|.
name|boost
argument_list|(
name|getBoost
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
annotation|@
name|Override
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
name|Query
name|rewrittenChildQuery
init|=
name|childQuery
operator|.
name|rewrite
argument_list|(
name|reader
argument_list|)
decl_stmt|;
if|if
condition|(
name|rewrittenChildQuery
operator|==
name|childQuery
condition|)
block|{
return|return
name|this
return|;
block|}
name|int
name|index
init|=
name|searchContext
operator|.
name|rewrites
argument_list|()
operator|.
name|indexOf
argument_list|(
name|this
argument_list|)
decl_stmt|;
name|ChildrenQuery
name|rewrite
init|=
operator|new
name|ChildrenQuery
argument_list|(
name|this
argument_list|,
name|rewrittenChildQuery
argument_list|)
decl_stmt|;
name|searchContext
operator|.
name|rewrites
argument_list|()
operator|.
name|set
argument_list|(
name|index
argument_list|,
name|rewrite
argument_list|)
expr_stmt|;
return|return
name|rewrite
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
name|childQuery
operator|.
name|extractTerms
argument_list|(
name|terms
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|contextRewrite
specifier|public
name|void
name|contextRewrite
parameter_list|(
name|SearchContext
name|searchContext
parameter_list|)
throws|throws
name|Exception
block|{
name|searchContext
operator|.
name|idCache
argument_list|()
operator|.
name|refresh
argument_list|(
name|searchContext
operator|.
name|searcher
argument_list|()
operator|.
name|getTopReaderContext
argument_list|()
operator|.
name|leaves
argument_list|()
argument_list|)
expr_stmt|;
name|uidToScore
operator|=
name|CacheRecycler
operator|.
name|popObjectFloatMap
argument_list|()
expr_stmt|;
name|Collector
name|collector
decl_stmt|;
switch|switch
condition|(
name|scoreType
condition|)
block|{
case|case
name|AVG
case|:
name|uidToCount
operator|=
name|CacheRecycler
operator|.
name|popObjectIntMap
argument_list|()
expr_stmt|;
name|collector
operator|=
operator|new
name|AvgChildUidCollector
argument_list|(
name|scoreType
argument_list|,
name|searchContext
argument_list|,
name|parentType
argument_list|,
name|uidToScore
argument_list|,
name|uidToCount
argument_list|)
expr_stmt|;
break|break;
default|default:
name|collector
operator|=
operator|new
name|ChildUidCollector
argument_list|(
name|scoreType
argument_list|,
name|searchContext
argument_list|,
name|parentType
argument_list|,
name|uidToScore
argument_list|)
expr_stmt|;
block|}
name|searchContext
operator|.
name|searcher
argument_list|()
operator|.
name|search
argument_list|(
name|childQuery
argument_list|,
name|collector
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|contextClear
specifier|public
name|void
name|contextClear
parameter_list|()
block|{
if|if
condition|(
name|uidToScore
operator|!=
literal|null
condition|)
block|{
name|CacheRecycler
operator|.
name|pushObjectFloatMap
argument_list|(
name|uidToScore
argument_list|)
expr_stmt|;
block|}
name|uidToScore
operator|=
literal|null
expr_stmt|;
if|if
condition|(
name|uidToCount
operator|!=
literal|null
condition|)
block|{
name|CacheRecycler
operator|.
name|pushObjectIntMap
argument_list|(
name|uidToCount
argument_list|)
expr_stmt|;
block|}
name|uidToCount
operator|=
literal|null
expr_stmt|;
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
if|if
condition|(
name|uidToScore
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ElasticSearchIllegalStateException
argument_list|(
literal|"has_child query hasn't executed properly"
argument_list|)
throw|;
block|}
return|return
operator|new
name|ParentWeight
argument_list|(
name|childQuery
operator|.
name|createWeight
argument_list|(
name|searcher
argument_list|)
argument_list|)
return|;
block|}
DECL|class|ParentWeight
class|class
name|ParentWeight
extends|extends
name|Weight
block|{
DECL|field|childWeight
specifier|final
name|Weight
name|childWeight
decl_stmt|;
DECL|method|ParentWeight
specifier|public
name|ParentWeight
parameter_list|(
name|Weight
name|childWeight
parameter_list|)
block|{
name|this
operator|.
name|childWeight
operator|=
name|childWeight
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|explain
specifier|public
name|Explanation
name|explain
parameter_list|(
name|AtomicReaderContext
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
name|ChildrenQuery
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
name|childWeight
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
name|AtomicReaderContext
name|context
parameter_list|,
name|boolean
name|scoreDocsInOrder
parameter_list|,
name|boolean
name|topScorer
parameter_list|,
name|Bits
name|acceptDocs
parameter_list|)
throws|throws
name|IOException
block|{
name|DocIdSet
name|parentsSet
init|=
name|parentFilter
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
name|parentsSet
operator|==
literal|null
operator|||
name|parentsSet
operator|==
name|DocIdSet
operator|.
name|EMPTY_DOCIDSET
condition|)
block|{
return|return
literal|null
return|;
block|}
name|IdReaderTypeCache
name|idTypeCache
init|=
name|searchContext
operator|.
name|idCache
argument_list|()
operator|.
name|reader
argument_list|(
name|context
operator|.
name|reader
argument_list|()
argument_list|)
operator|.
name|type
argument_list|(
name|parentType
argument_list|)
decl_stmt|;
name|DocIdSetIterator
name|parentsIterator
init|=
name|parentsSet
operator|.
name|iterator
argument_list|()
decl_stmt|;
switch|switch
condition|(
name|scoreType
condition|)
block|{
case|case
name|AVG
case|:
return|return
operator|new
name|AvgParentScorer
argument_list|(
name|this
argument_list|,
name|idTypeCache
argument_list|,
name|uidToScore
argument_list|,
name|uidToCount
argument_list|,
name|parentsIterator
argument_list|)
return|;
default|default:
return|return
operator|new
name|ParentScorer
argument_list|(
name|this
argument_list|,
name|idTypeCache
argument_list|,
name|uidToScore
argument_list|,
name|parentsIterator
argument_list|)
return|;
block|}
block|}
block|}
DECL|class|ParentScorer
specifier|static
class|class
name|ParentScorer
extends|extends
name|Scorer
block|{
DECL|field|idTypeCache
specifier|final
name|IdReaderTypeCache
name|idTypeCache
decl_stmt|;
DECL|field|uidToScore
specifier|final
name|TObjectFloatMap
argument_list|<
name|HashedBytesArray
argument_list|>
name|uidToScore
decl_stmt|;
DECL|field|parentsIterator
specifier|final
name|DocIdSetIterator
name|parentsIterator
decl_stmt|;
DECL|field|currentDocId
name|int
name|currentDocId
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|currentScore
name|float
name|currentScore
decl_stmt|;
DECL|method|ParentScorer
name|ParentScorer
parameter_list|(
name|Weight
name|weight
parameter_list|,
name|IdReaderTypeCache
name|idTypeCache
parameter_list|,
name|TObjectFloatMap
argument_list|<
name|HashedBytesArray
argument_list|>
name|uidToScore
parameter_list|,
name|DocIdSetIterator
name|parentsIterator
parameter_list|)
block|{
name|super
argument_list|(
name|weight
argument_list|)
expr_stmt|;
name|this
operator|.
name|idTypeCache
operator|=
name|idTypeCache
expr_stmt|;
name|this
operator|.
name|uidToScore
operator|=
name|uidToScore
expr_stmt|;
name|this
operator|.
name|parentsIterator
operator|=
name|parentsIterator
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
name|currentDocId
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
name|currentDocId
operator|=
name|parentsIterator
operator|.
name|nextDoc
argument_list|()
expr_stmt|;
if|if
condition|(
name|currentDocId
operator|==
name|DocIdSetIterator
operator|.
name|NO_MORE_DOCS
condition|)
block|{
return|return
name|currentDocId
return|;
block|}
name|HashedBytesArray
name|uid
init|=
name|idTypeCache
operator|.
name|idByDoc
argument_list|(
name|currentDocId
argument_list|)
decl_stmt|;
name|currentScore
operator|=
name|uidToScore
operator|.
name|get
argument_list|(
name|uid
argument_list|)
expr_stmt|;
if|if
condition|(
name|Float
operator|.
name|compare
argument_list|(
name|currentScore
argument_list|,
literal|0
argument_list|)
operator|>
literal|0
condition|)
block|{
return|return
name|currentDocId
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
name|currentDocId
operator|=
name|parentsIterator
operator|.
name|advance
argument_list|(
name|target
argument_list|)
expr_stmt|;
if|if
condition|(
name|currentDocId
operator|==
name|DocIdSetIterator
operator|.
name|NO_MORE_DOCS
condition|)
block|{
return|return
name|currentDocId
return|;
block|}
name|HashedBytesArray
name|uid
init|=
name|idTypeCache
operator|.
name|idByDoc
argument_list|(
name|currentDocId
argument_list|)
decl_stmt|;
name|currentScore
operator|=
name|uidToScore
operator|.
name|get
argument_list|(
name|uid
argument_list|)
expr_stmt|;
if|if
condition|(
name|Float
operator|.
name|compare
argument_list|(
name|currentScore
argument_list|,
literal|0
argument_list|)
operator|>
literal|0
condition|)
block|{
return|return
name|currentDocId
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
block|}
DECL|class|AvgParentScorer
specifier|static
class|class
name|AvgParentScorer
extends|extends
name|ParentScorer
block|{
DECL|field|uidToCount
specifier|final
name|TObjectIntMap
argument_list|<
name|HashedBytesArray
argument_list|>
name|uidToCount
decl_stmt|;
DECL|field|currentUid
name|HashedBytesArray
name|currentUid
decl_stmt|;
DECL|method|AvgParentScorer
name|AvgParentScorer
parameter_list|(
name|Weight
name|weight
parameter_list|,
name|IdReaderTypeCache
name|idTypeCache
parameter_list|,
name|TObjectFloatMap
argument_list|<
name|HashedBytesArray
argument_list|>
name|uidToScore
parameter_list|,
name|TObjectIntMap
argument_list|<
name|HashedBytesArray
argument_list|>
name|uidToCount
parameter_list|,
name|DocIdSetIterator
name|parentsIterator
parameter_list|)
block|{
name|super
argument_list|(
name|weight
argument_list|,
name|idTypeCache
argument_list|,
name|uidToScore
argument_list|,
name|parentsIterator
argument_list|)
expr_stmt|;
name|this
operator|.
name|uidToCount
operator|=
name|uidToCount
expr_stmt|;
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
name|currentDocId
operator|=
name|parentsIterator
operator|.
name|nextDoc
argument_list|()
expr_stmt|;
if|if
condition|(
name|currentDocId
operator|==
name|DocIdSetIterator
operator|.
name|NO_MORE_DOCS
condition|)
block|{
return|return
name|currentDocId
return|;
block|}
name|currentUid
operator|=
name|idTypeCache
operator|.
name|idByDoc
argument_list|(
name|currentDocId
argument_list|)
expr_stmt|;
name|currentScore
operator|=
name|uidToScore
operator|.
name|get
argument_list|(
name|currentUid
argument_list|)
expr_stmt|;
if|if
condition|(
name|Float
operator|.
name|compare
argument_list|(
name|currentScore
argument_list|,
literal|0
argument_list|)
operator|>
literal|0
condition|)
block|{
name|currentScore
operator|/=
name|uidToCount
operator|.
name|get
argument_list|(
name|currentUid
argument_list|)
expr_stmt|;
return|return
name|currentDocId
return|;
block|}
block|}
block|}
block|}
DECL|class|ChildUidCollector
specifier|static
class|class
name|ChildUidCollector
extends|extends
name|NoopCollector
block|{
DECL|field|uidToScore
specifier|final
name|TObjectFloatHashMap
argument_list|<
name|HashedBytesArray
argument_list|>
name|uidToScore
decl_stmt|;
DECL|field|scoreType
specifier|final
name|ScoreType
name|scoreType
decl_stmt|;
DECL|field|searchContext
specifier|final
name|SearchContext
name|searchContext
decl_stmt|;
DECL|field|childType
specifier|final
name|String
name|childType
decl_stmt|;
DECL|field|scorer
name|Scorer
name|scorer
decl_stmt|;
DECL|field|typeCache
name|IdReaderTypeCache
name|typeCache
decl_stmt|;
DECL|method|ChildUidCollector
name|ChildUidCollector
parameter_list|(
name|ScoreType
name|scoreType
parameter_list|,
name|SearchContext
name|searchContext
parameter_list|,
name|String
name|childType
parameter_list|,
name|TObjectFloatHashMap
argument_list|<
name|HashedBytesArray
argument_list|>
name|uidToScore
parameter_list|)
block|{
name|this
operator|.
name|uidToScore
operator|=
name|uidToScore
expr_stmt|;
name|this
operator|.
name|scoreType
operator|=
name|scoreType
expr_stmt|;
name|this
operator|.
name|searchContext
operator|=
name|searchContext
expr_stmt|;
name|this
operator|.
name|childType
operator|=
name|childType
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
if|if
condition|(
name|typeCache
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|HashedBytesArray
name|parentUid
init|=
name|typeCache
operator|.
name|parentIdByDoc
argument_list|(
name|doc
argument_list|)
decl_stmt|;
name|float
name|previousScore
init|=
name|uidToScore
operator|.
name|get
argument_list|(
name|parentUid
argument_list|)
decl_stmt|;
name|float
name|currentScore
init|=
name|scorer
operator|.
name|score
argument_list|()
decl_stmt|;
if|if
condition|(
name|Float
operator|.
name|compare
argument_list|(
name|previousScore
argument_list|,
literal|0
argument_list|)
operator|==
literal|0
condition|)
block|{
name|uidToScore
operator|.
name|put
argument_list|(
name|parentUid
argument_list|,
name|currentScore
argument_list|)
expr_stmt|;
block|}
else|else
block|{
switch|switch
condition|(
name|scoreType
condition|)
block|{
case|case
name|SUM
case|:
name|uidToScore
operator|.
name|adjustValue
argument_list|(
name|parentUid
argument_list|,
name|currentScore
argument_list|)
expr_stmt|;
break|break;
case|case
name|MAX
case|:
if|if
condition|(
name|Float
operator|.
name|compare
argument_list|(
name|previousScore
argument_list|,
name|currentScore
argument_list|)
operator|<
literal|0
condition|)
block|{
name|uidToScore
operator|.
name|put
argument_list|(
name|parentUid
argument_list|,
name|currentScore
argument_list|)
expr_stmt|;
block|}
break|break;
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
DECL|method|setNextReader
specifier|public
name|void
name|setNextReader
parameter_list|(
name|AtomicReaderContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|typeCache
operator|=
name|searchContext
operator|.
name|idCache
argument_list|()
operator|.
name|reader
argument_list|(
name|context
operator|.
name|reader
argument_list|()
argument_list|)
operator|.
name|type
argument_list|(
name|childType
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|AvgChildUidCollector
specifier|static
class|class
name|AvgChildUidCollector
extends|extends
name|ChildUidCollector
block|{
DECL|field|uidToCount
specifier|final
name|TObjectIntHashMap
argument_list|<
name|HashedBytesArray
argument_list|>
name|uidToCount
decl_stmt|;
DECL|method|AvgChildUidCollector
name|AvgChildUidCollector
parameter_list|(
name|ScoreType
name|scoreType
parameter_list|,
name|SearchContext
name|searchContext
parameter_list|,
name|String
name|childType
parameter_list|,
name|TObjectFloatHashMap
argument_list|<
name|HashedBytesArray
argument_list|>
name|uidToScore
parameter_list|,
name|TObjectIntHashMap
argument_list|<
name|HashedBytesArray
argument_list|>
name|uidToCount
parameter_list|)
block|{
name|super
argument_list|(
name|scoreType
argument_list|,
name|searchContext
argument_list|,
name|childType
argument_list|,
name|uidToScore
argument_list|)
expr_stmt|;
name|this
operator|.
name|uidToCount
operator|=
name|uidToCount
expr_stmt|;
assert|assert
name|scoreType
operator|==
name|ScoreType
operator|.
name|AVG
assert|;
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
if|if
condition|(
name|typeCache
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|HashedBytesArray
name|parentUid
init|=
name|typeCache
operator|.
name|parentIdByDoc
argument_list|(
name|doc
argument_list|)
decl_stmt|;
name|float
name|previousScore
init|=
name|uidToScore
operator|.
name|get
argument_list|(
name|parentUid
argument_list|)
decl_stmt|;
name|float
name|currentScore
init|=
name|scorer
operator|.
name|score
argument_list|()
decl_stmt|;
if|if
condition|(
name|Float
operator|.
name|compare
argument_list|(
name|previousScore
argument_list|,
literal|0
argument_list|)
operator|==
literal|0
condition|)
block|{
name|uidToScore
operator|.
name|put
argument_list|(
name|parentUid
argument_list|,
name|currentScore
argument_list|)
expr_stmt|;
name|uidToCount
operator|.
name|put
argument_list|(
name|parentUid
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|uidToScore
operator|.
name|adjustValue
argument_list|(
name|parentUid
argument_list|,
name|currentScore
argument_list|)
expr_stmt|;
name|uidToCount
operator|.
name|increment
argument_list|(
name|parentUid
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

