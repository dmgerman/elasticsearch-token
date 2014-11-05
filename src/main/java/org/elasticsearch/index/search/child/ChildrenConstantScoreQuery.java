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
name|LeafReaderContext
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
name|SortedDocValues
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
name|BitsFilteredDocIdSet
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
name|CollectionTerminatedException
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
name|DocIdSet
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
name|DocIdSetIterator
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
name|Explanation
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
name|Filter
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
name|IndexSearcher
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
name|Scorer
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
name|Weight
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
name|XFilteredDocIdSetIterator
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
name|BitDocIdSetFilter
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
name|LongBitSet
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
name|index
operator|.
name|fielddata
operator|.
name|AtomicParentChildFieldData
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
name|List
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
comment|/**  *  */
end_comment

begin_class
DECL|class|ChildrenConstantScoreQuery
specifier|public
class|class
name|ChildrenConstantScoreQuery
extends|extends
name|Query
block|{
DECL|field|parentChildIndexFieldData
specifier|private
specifier|final
name|IndexParentChildFieldData
name|parentChildIndexFieldData
decl_stmt|;
DECL|field|originalChildQuery
specifier|private
name|Query
name|originalChildQuery
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
name|BitDocIdSetFilter
name|parentFilter
decl_stmt|;
DECL|field|shortCircuitParentDocSet
specifier|private
specifier|final
name|int
name|shortCircuitParentDocSet
decl_stmt|;
DECL|field|nonNestedDocsFilter
specifier|private
specifier|final
name|BitDocIdSetFilter
name|nonNestedDocsFilter
decl_stmt|;
DECL|field|rewrittenChildQuery
specifier|private
name|Query
name|rewrittenChildQuery
decl_stmt|;
DECL|field|rewriteIndexReader
specifier|private
name|IndexReader
name|rewriteIndexReader
decl_stmt|;
DECL|method|ChildrenConstantScoreQuery
specifier|public
name|ChildrenConstantScoreQuery
parameter_list|(
name|IndexParentChildFieldData
name|parentChildIndexFieldData
parameter_list|,
name|Query
name|childQuery
parameter_list|,
name|String
name|parentType
parameter_list|,
name|String
name|childType
parameter_list|,
name|BitDocIdSetFilter
name|parentFilter
parameter_list|,
name|int
name|shortCircuitParentDocSet
parameter_list|,
name|BitDocIdSetFilter
name|nonNestedDocsFilter
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
name|parentFilter
operator|=
name|parentFilter
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
name|originalChildQuery
operator|=
name|childQuery
expr_stmt|;
name|this
operator|.
name|shortCircuitParentDocSet
operator|=
name|shortCircuitParentDocSet
expr_stmt|;
name|this
operator|.
name|nonNestedDocsFilter
operator|=
name|nonNestedDocsFilter
expr_stmt|;
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
name|rewriteIndexReader
operator|=
name|reader
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
DECL|method|clone
specifier|public
name|Query
name|clone
parameter_list|()
block|{
name|ChildrenConstantScoreQuery
name|q
init|=
operator|(
name|ChildrenConstantScoreQuery
operator|)
name|super
operator|.
name|clone
argument_list|()
decl_stmt|;
name|q
operator|.
name|originalChildQuery
operator|=
name|originalChildQuery
operator|.
name|clone
argument_list|()
expr_stmt|;
if|if
condition|(
name|q
operator|.
name|rewrittenChildQuery
operator|!=
literal|null
condition|)
block|{
name|q
operator|.
name|rewrittenChildQuery
operator|=
name|rewrittenChildQuery
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
assert|assert
name|rewrittenChildQuery
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
name|long
name|valueCount
decl_stmt|;
name|List
argument_list|<
name|LeafReaderContext
argument_list|>
name|leaves
init|=
name|searcher
operator|.
name|getIndexReader
argument_list|()
operator|.
name|leaves
argument_list|()
decl_stmt|;
if|if
condition|(
name|globalIfd
operator|==
literal|null
operator|||
name|leaves
operator|.
name|isEmpty
argument_list|()
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
else|else
block|{
name|AtomicParentChildFieldData
name|afd
init|=
name|globalIfd
operator|.
name|load
argument_list|(
name|leaves
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
decl_stmt|;
name|SortedDocValues
name|globalValues
init|=
name|afd
operator|.
name|getOrdinalsValues
argument_list|(
name|parentType
argument_list|)
decl_stmt|;
name|valueCount
operator|=
name|globalValues
operator|.
name|getValueCount
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|valueCount
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
name|Query
name|childQuery
init|=
name|rewrittenChildQuery
decl_stmt|;
name|IndexSearcher
name|indexSearcher
init|=
operator|new
name|IndexSearcher
argument_list|(
name|searcher
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
name|ParentOrdCollector
name|collector
init|=
operator|new
name|ParentOrdCollector
argument_list|(
name|globalIfd
argument_list|,
name|valueCount
argument_list|,
name|parentType
argument_list|)
decl_stmt|;
name|indexSearcher
operator|.
name|search
argument_list|(
name|childQuery
argument_list|,
name|collector
argument_list|)
expr_stmt|;
specifier|final
name|long
name|remaining
init|=
name|collector
operator|.
name|foundParents
argument_list|()
decl_stmt|;
if|if
condition|(
name|remaining
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
name|Filter
name|shortCircuitFilter
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|remaining
operator|<=
name|shortCircuitParentDocSet
condition|)
block|{
name|shortCircuitFilter
operator|=
name|ParentIdsFilter
operator|.
name|createShortCircuitFilter
argument_list|(
name|nonNestedDocsFilter
argument_list|,
name|sc
argument_list|,
name|parentType
argument_list|,
name|collector
operator|.
name|values
argument_list|,
name|collector
operator|.
name|parentOrds
argument_list|,
name|remaining
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|ParentWeight
argument_list|(
name|parentFilter
argument_list|,
name|globalIfd
argument_list|,
name|shortCircuitFilter
argument_list|,
name|collector
argument_list|,
name|remaining
argument_list|)
return|;
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
name|ChildrenConstantScoreQuery
name|that
init|=
operator|(
name|ChildrenConstantScoreQuery
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
if|if
condition|(
name|shortCircuitParentDocSet
operator|!=
name|that
operator|.
name|shortCircuitParentDocSet
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
name|result
operator|=
literal|31
operator|*
name|result
operator|+
name|shortCircuitParentDocSet
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
literal|"child_filter["
operator|+
name|childType
operator|+
literal|"/"
operator|+
name|parentType
operator|+
literal|"]("
operator|+
name|originalChildQuery
operator|+
literal|')'
return|;
block|}
DECL|class|ParentWeight
specifier|private
specifier|final
class|class
name|ParentWeight
extends|extends
name|Weight
block|{
DECL|field|parentFilter
specifier|private
specifier|final
name|Filter
name|parentFilter
decl_stmt|;
DECL|field|shortCircuitFilter
specifier|private
specifier|final
name|Filter
name|shortCircuitFilter
decl_stmt|;
DECL|field|collector
specifier|private
specifier|final
name|ParentOrdCollector
name|collector
decl_stmt|;
DECL|field|globalIfd
specifier|private
specifier|final
name|IndexParentChildFieldData
name|globalIfd
decl_stmt|;
DECL|field|remaining
specifier|private
name|long
name|remaining
decl_stmt|;
DECL|field|queryNorm
specifier|private
name|float
name|queryNorm
decl_stmt|;
DECL|field|queryWeight
specifier|private
name|float
name|queryWeight
decl_stmt|;
DECL|method|ParentWeight
specifier|public
name|ParentWeight
parameter_list|(
name|Filter
name|parentFilter
parameter_list|,
name|IndexParentChildFieldData
name|globalIfd
parameter_list|,
name|Filter
name|shortCircuitFilter
parameter_list|,
name|ParentOrdCollector
name|collector
parameter_list|,
name|long
name|remaining
parameter_list|)
block|{
name|this
operator|.
name|parentFilter
operator|=
name|parentFilter
expr_stmt|;
name|this
operator|.
name|globalIfd
operator|=
name|globalIfd
expr_stmt|;
name|this
operator|.
name|shortCircuitFilter
operator|=
name|shortCircuitFilter
expr_stmt|;
name|this
operator|.
name|collector
operator|=
name|collector
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
name|ChildrenConstantScoreQuery
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
name|queryWeight
operator|=
name|getBoost
argument_list|()
expr_stmt|;
return|return
name|queryWeight
operator|*
name|queryWeight
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
block|{
name|this
operator|.
name|queryNorm
operator|=
name|norm
operator|*
name|topLevelBoost
expr_stmt|;
name|queryWeight
operator|*=
name|this
operator|.
name|queryNorm
expr_stmt|;
block|}
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
if|if
condition|(
name|remaining
operator|==
literal|0
condition|)
block|{
return|return
literal|null
return|;
block|}
if|if
condition|(
name|shortCircuitFilter
operator|!=
literal|null
condition|)
block|{
name|DocIdSet
name|docIdSet
init|=
name|shortCircuitFilter
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
operator|!
name|DocIdSets
operator|.
name|isEmpty
argument_list|(
name|docIdSet
argument_list|)
condition|)
block|{
name|DocIdSetIterator
name|iterator
init|=
name|docIdSet
operator|.
name|iterator
argument_list|()
decl_stmt|;
if|if
condition|(
name|iterator
operator|!=
literal|null
condition|)
block|{
return|return
name|ConstantScorer
operator|.
name|create
argument_list|(
name|iterator
argument_list|,
name|this
argument_list|,
name|queryWeight
argument_list|)
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
name|DocIdSet
name|parentDocIdSet
init|=
name|this
operator|.
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
operator|!
name|DocIdSets
operator|.
name|isEmpty
argument_list|(
name|parentDocIdSet
argument_list|)
condition|)
block|{
comment|// We can't be sure of the fact that liveDocs have been applied, so we apply it here. The "remaining"
comment|// count down (short circuit) logic will then work as expected.
name|parentDocIdSet
operator|=
name|BitsFilteredDocIdSet
operator|.
name|wrap
argument_list|(
name|parentDocIdSet
argument_list|,
name|context
operator|.
name|reader
argument_list|()
operator|.
name|getLiveDocs
argument_list|()
argument_list|)
expr_stmt|;
name|DocIdSetIterator
name|innerIterator
init|=
name|parentDocIdSet
operator|.
name|iterator
argument_list|()
decl_stmt|;
if|if
condition|(
name|innerIterator
operator|!=
literal|null
condition|)
block|{
name|LongBitSet
name|parentOrds
init|=
name|collector
operator|.
name|parentOrds
decl_stmt|;
name|SortedDocValues
name|globalValues
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
name|globalValues
operator|!=
literal|null
condition|)
block|{
name|DocIdSetIterator
name|parentIdIterator
init|=
operator|new
name|ParentOrdIterator
argument_list|(
name|innerIterator
argument_list|,
name|parentOrds
argument_list|,
name|globalValues
argument_list|,
name|this
argument_list|)
decl_stmt|;
return|return
name|ConstantScorer
operator|.
name|create
argument_list|(
name|parentIdIterator
argument_list|,
name|this
argument_list|,
name|queryWeight
argument_list|)
return|;
block|}
block|}
block|}
return|return
literal|null
return|;
block|}
block|}
DECL|class|ParentOrdCollector
specifier|private
specifier|final
specifier|static
class|class
name|ParentOrdCollector
extends|extends
name|NoopCollector
block|{
DECL|field|parentOrds
specifier|private
specifier|final
name|LongBitSet
name|parentOrds
decl_stmt|;
DECL|field|indexFieldData
specifier|private
specifier|final
name|IndexParentChildFieldData
name|indexFieldData
decl_stmt|;
DECL|field|parentType
specifier|private
specifier|final
name|String
name|parentType
decl_stmt|;
DECL|field|values
specifier|private
name|SortedDocValues
name|values
decl_stmt|;
DECL|method|ParentOrdCollector
specifier|private
name|ParentOrdCollector
parameter_list|(
name|IndexParentChildFieldData
name|indexFieldData
parameter_list|,
name|long
name|maxOrd
parameter_list|,
name|String
name|parentType
parameter_list|)
block|{
comment|// TODO: look into reusing LongBitSet#bits array
name|this
operator|.
name|parentOrds
operator|=
operator|new
name|LongBitSet
argument_list|(
name|maxOrd
operator|+
literal|1
argument_list|)
expr_stmt|;
name|this
operator|.
name|indexFieldData
operator|=
name|indexFieldData
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
if|if
condition|(
name|values
operator|!=
literal|null
condition|)
block|{
name|int
name|globalOrdinal
init|=
name|values
operator|.
name|getOrd
argument_list|(
name|doc
argument_list|)
decl_stmt|;
comment|// TODO: oversize the long bitset and remove the branch
if|if
condition|(
name|globalOrdinal
operator|>=
literal|0
condition|)
block|{
name|parentOrds
operator|.
name|set
argument_list|(
name|globalOrdinal
argument_list|)
expr_stmt|;
block|}
block|}
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
name|indexFieldData
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
DECL|method|foundParents
name|long
name|foundParents
parameter_list|()
block|{
return|return
name|parentOrds
operator|.
name|cardinality
argument_list|()
return|;
block|}
block|}
DECL|class|ParentOrdIterator
specifier|private
specifier|final
specifier|static
class|class
name|ParentOrdIterator
extends|extends
name|XFilteredDocIdSetIterator
block|{
DECL|field|parentOrds
specifier|private
specifier|final
name|LongBitSet
name|parentOrds
decl_stmt|;
DECL|field|ordinals
specifier|private
specifier|final
name|SortedDocValues
name|ordinals
decl_stmt|;
DECL|field|parentWeight
specifier|private
specifier|final
name|ParentWeight
name|parentWeight
decl_stmt|;
DECL|method|ParentOrdIterator
specifier|private
name|ParentOrdIterator
parameter_list|(
name|DocIdSetIterator
name|innerIterator
parameter_list|,
name|LongBitSet
name|parentOrds
parameter_list|,
name|SortedDocValues
name|ordinals
parameter_list|,
name|ParentWeight
name|parentWeight
parameter_list|)
block|{
name|super
argument_list|(
name|innerIterator
argument_list|)
expr_stmt|;
name|this
operator|.
name|parentOrds
operator|=
name|parentOrds
expr_stmt|;
name|this
operator|.
name|ordinals
operator|=
name|ordinals
expr_stmt|;
name|this
operator|.
name|parentWeight
operator|=
name|parentWeight
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|match
specifier|protected
name|boolean
name|match
parameter_list|(
name|int
name|doc
parameter_list|)
block|{
if|if
condition|(
name|parentWeight
operator|.
name|remaining
operator|==
literal|0
condition|)
block|{
throw|throw
operator|new
name|CollectionTerminatedException
argument_list|()
throw|;
block|}
name|long
name|parentOrd
init|=
name|ordinals
operator|.
name|getOrd
argument_list|(
name|doc
argument_list|)
decl_stmt|;
if|if
condition|(
name|parentOrd
operator|>=
literal|0
condition|)
block|{
name|boolean
name|match
init|=
name|parentOrds
operator|.
name|get
argument_list|(
name|parentOrd
argument_list|)
decl_stmt|;
if|if
condition|(
name|match
condition|)
block|{
name|parentWeight
operator|.
name|remaining
operator|--
expr_stmt|;
block|}
return|return
name|match
return|;
block|}
return|return
literal|false
return|;
block|}
block|}
block|}
end_class

end_unit

