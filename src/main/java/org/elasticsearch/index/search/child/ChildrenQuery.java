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
name|BytesRef
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
name|ApplyAcceptedDocsFilter
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
name|lucene
operator|.
name|search
operator|.
name|TermFilter
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
name|recycler
operator|.
name|Recycler
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
comment|/**  * A query implementation that executes the wrapped child query and connects all the matching child docs to the related  * parent documents using the {@link IdReaderTypeCache}.  *<p/>  * This query is executed in two rounds. The first round resolves all the matching child documents and groups these  * documents by parent uid value. Also the child scores are aggregated per parent uid value. During the second round  * all parent documents having the same uid value that is collected in the first phase are emitted as hit including  * a score based on the aggregated child scores and score type.  */
end_comment

begin_comment
comment|// TODO We use a score of 0 to indicate a doc was not scored in uidToScore, this means score of 0 can be problematic, if we move to HPCC, we can use lset/...
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
DECL|field|originalChildQuery
specifier|private
specifier|final
name|Query
name|originalChildQuery
decl_stmt|;
DECL|field|shortCircuitParentDocSet
specifier|private
specifier|final
name|int
name|shortCircuitParentDocSet
decl_stmt|;
DECL|field|rewrittenChildQuery
specifier|private
name|Query
name|rewrittenChildQuery
decl_stmt|;
DECL|field|uidToScore
specifier|private
name|Recycler
operator|.
name|V
argument_list|<
name|TObjectFloatHashMap
argument_list|<
name|HashedBytesArray
argument_list|>
argument_list|>
name|uidToScore
decl_stmt|;
DECL|field|uidToCount
specifier|private
name|Recycler
operator|.
name|V
argument_list|<
name|TObjectIntHashMap
argument_list|<
name|HashedBytesArray
argument_list|>
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
parameter_list|,
name|int
name|shortCircuitParentDocSet
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
operator|new
name|ApplyAcceptedDocsFilter
argument_list|(
name|parentFilter
argument_list|)
expr_stmt|;
name|this
operator|.
name|originalChildQuery
operator|=
name|childQuery
expr_stmt|;
name|this
operator|.
name|scoreType
operator|=
name|scoreType
expr_stmt|;
name|this
operator|.
name|shortCircuitParentDocSet
operator|=
name|shortCircuitParentDocSet
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
name|ChildrenQuery
name|that
init|=
operator|(
name|ChildrenQuery
operator|)
name|obj
decl_stmt|;
if|if
condition|(
operator|!
name|originalChildQuery
operator|.
name|equals
argument_list|(
name|that
operator|.
name|originalChildQuery
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
name|childType
operator|.
name|equals
argument_list|(
name|that
operator|.
name|childType
argument_list|)
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
name|originalChildQuery
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
name|childType
operator|.
name|hashCode
argument_list|()
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
name|originalChildQuery
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
name|rewrittenChildQuery
operator|==
literal|null
condition|)
block|{
name|rewrittenChildQuery
operator|=
name|originalChildQuery
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
name|rewrittenChildQuery
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
name|searchContext
operator|.
name|cacheRecycler
argument_list|()
operator|.
name|objectFloatMap
argument_list|(
operator|-
literal|1
argument_list|)
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
name|searchContext
operator|.
name|cacheRecycler
argument_list|()
operator|.
name|objectIntMap
argument_list|(
operator|-
literal|1
argument_list|)
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
operator|.
name|v
argument_list|()
argument_list|,
name|uidToCount
operator|.
name|v
argument_list|()
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
operator|.
name|v
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|Query
name|childQuery
decl_stmt|;
if|if
condition|(
name|rewrittenChildQuery
operator|==
literal|null
condition|)
block|{
name|childQuery
operator|=
name|rewrittenChildQuery
operator|=
name|searchContext
operator|.
name|searcher
argument_list|()
operator|.
name|rewrite
argument_list|(
name|originalChildQuery
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|childQuery
operator|=
name|rewrittenChildQuery
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
DECL|method|executionDone
specifier|public
name|void
name|executionDone
parameter_list|()
block|{
if|if
condition|(
name|uidToScore
operator|!=
literal|null
condition|)
block|{
name|uidToScore
operator|.
name|release
argument_list|()
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
name|uidToCount
operator|.
name|release
argument_list|()
expr_stmt|;
block|}
name|uidToCount
operator|=
literal|null
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|contextClear
specifier|public
name|void
name|contextClear
parameter_list|()
block|{     }
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
name|int
name|size
init|=
name|uidToScore
operator|.
name|v
argument_list|()
operator|.
name|size
argument_list|()
decl_stmt|;
if|if
condition|(
name|size
operator|==
literal|0
condition|)
block|{
return|return
name|Queries
operator|.
name|NO_MATCH_QUERY
operator|.
name|createWeight
argument_list|(
name|searcher
argument_list|)
return|;
block|}
name|Filter
name|parentFilter
decl_stmt|;
if|if
condition|(
name|size
operator|==
literal|1
condition|)
block|{
name|BytesRef
name|id
init|=
name|uidToScore
operator|.
name|v
argument_list|()
operator|.
name|keySet
argument_list|()
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
operator|.
name|toBytesRef
argument_list|()
decl_stmt|;
name|parentFilter
operator|=
operator|new
name|TermFilter
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
name|createUidAsBytes
argument_list|(
name|parentType
argument_list|,
name|id
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|size
operator|<=
name|shortCircuitParentDocSet
condition|)
block|{
name|parentFilter
operator|=
operator|new
name|ParentIdsFilter
argument_list|(
name|parentType
argument_list|,
name|uidToScore
operator|.
name|v
argument_list|()
operator|.
name|keySet
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|parentFilter
operator|=
name|this
operator|.
name|parentFilter
expr_stmt|;
block|}
return|return
operator|new
name|ParentWeight
argument_list|(
name|rewrittenChildQuery
operator|.
name|createWeight
argument_list|(
name|searcher
argument_list|)
argument_list|,
name|parentFilter
argument_list|,
name|size
argument_list|)
return|;
block|}
DECL|class|ParentWeight
specifier|final
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
DECL|field|parentFilter
specifier|final
name|Filter
name|parentFilter
decl_stmt|;
DECL|field|remaining
name|int
name|remaining
decl_stmt|;
DECL|method|ParentWeight
specifier|public
name|ParentWeight
parameter_list|(
name|Weight
name|childWeight
parameter_list|,
name|Filter
name|parentFilter
parameter_list|,
name|int
name|remaining
parameter_list|)
block|{
name|this
operator|.
name|childWeight
operator|=
name|childWeight
expr_stmt|;
name|this
operator|.
name|parentFilter
operator|=
name|parentFilter
expr_stmt|;
name|this
operator|.
name|remaining
operator|=
name|remaining
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
name|DocIdSets
operator|.
name|isEmpty
argument_list|(
name|parentsSet
argument_list|)
operator|||
name|remaining
operator|==
literal|0
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
operator|.
name|v
argument_list|()
argument_list|,
name|uidToCount
operator|.
name|v
argument_list|()
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
operator|.
name|v
argument_list|()
argument_list|,
name|parentsIterator
argument_list|)
return|;
block|}
block|}
DECL|class|ParentScorer
class|class
name|ParentScorer
extends|extends
name|Scorer
block|{
DECL|field|uidToScore
specifier|final
name|TObjectFloatHashMap
argument_list|<
name|HashedBytesArray
argument_list|>
name|uidToScore
decl_stmt|;
DECL|field|idTypeCache
specifier|final
name|IdReaderTypeCache
name|idTypeCache
decl_stmt|;
DECL|field|parentsIterator
specifier|final
name|DocIdSetIterator
name|parentsIterator
decl_stmt|;
DECL|field|remaining
name|int
name|remaining
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
name|TObjectFloatHashMap
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
name|parentsIterator
operator|=
name|parentsIterator
expr_stmt|;
name|this
operator|.
name|uidToScore
operator|=
name|uidToScore
expr_stmt|;
name|this
operator|.
name|remaining
operator|=
name|uidToScore
operator|.
name|size
argument_list|()
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
if|if
condition|(
name|remaining
operator|==
literal|0
condition|)
block|{
return|return
name|NO_MORE_DOCS
return|;
block|}
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
name|currentScore
operator|!=
literal|0
condition|)
block|{
name|remaining
operator|--
expr_stmt|;
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
if|if
condition|(
name|remaining
operator|==
literal|0
condition|)
block|{
return|return
name|NO_MORE_DOCS
return|;
block|}
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
name|currentScore
operator|!=
literal|0
condition|)
block|{
name|remaining
operator|--
expr_stmt|;
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
annotation|@
name|Override
DECL|method|cost
specifier|public
name|long
name|cost
parameter_list|()
block|{
return|return
name|parentsIterator
operator|.
name|cost
argument_list|()
return|;
block|}
block|}
DECL|class|AvgParentScorer
specifier|final
class|class
name|AvgParentScorer
extends|extends
name|ParentScorer
block|{
DECL|field|currentUid
name|HashedBytesArray
name|currentUid
decl_stmt|;
DECL|field|uidToCount
specifier|final
name|TObjectIntHashMap
argument_list|<
name|HashedBytesArray
argument_list|>
name|uidToCount
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
name|currentScore
operator|!=
literal|0
condition|)
block|{
name|remaining
operator|--
expr_stmt|;
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
name|currentScore
operator|!=
literal|0
condition|)
block|{
name|remaining
operator|--
expr_stmt|;
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
else|else
block|{
return|return
name|nextDoc
argument_list|()
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
name|ParentIdCollector
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
DECL|field|scorer
name|Scorer
name|scorer
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
name|super
argument_list|(
name|childType
argument_list|,
name|searchContext
argument_list|)
expr_stmt|;
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
DECL|method|collect
specifier|protected
name|void
name|collect
parameter_list|(
name|int
name|doc
parameter_list|,
name|HashedBytesArray
name|parentUid
parameter_list|)
throws|throws
name|IOException
block|{
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
name|previousScore
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
name|currentScore
operator|>
name|previousScore
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
case|case
name|AVG
case|:
assert|assert
literal|false
operator|:
literal|"AVG has it's own collector"
assert|;
default|default:
assert|assert
literal|false
operator|:
literal|"Are we missing a score type here? -- "
operator|+
name|scoreType
assert|;
break|break;
block|}
block|}
block|}
block|}
DECL|class|AvgChildUidCollector
specifier|final
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
specifier|protected
name|void
name|collect
parameter_list|(
name|int
name|doc
parameter_list|,
name|HashedBytesArray
name|parentUid
parameter_list|)
throws|throws
name|IOException
block|{
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
name|previousScore
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

