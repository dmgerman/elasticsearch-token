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
comment|/**  * A query that only return child documents that are linked to the parent documents that matched with the inner query.  */
end_comment

begin_class
DECL|class|ParentConstantScoreQuery
specifier|public
class|class
name|ParentConstantScoreQuery
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
DECL|method|ParentConstantScoreQuery
specifier|public
name|ParentConstantScoreQuery
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
name|rewrittenParentQuery
operator|=
name|originalParentQuery
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
DECL|method|clone
specifier|public
name|Query
name|clone
parameter_list|()
block|{
name|ParentConstantScoreQuery
name|q
init|=
operator|(
name|ParentConstantScoreQuery
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
parameter_list|,
name|boolean
name|needsScores
parameter_list|)
throws|throws
name|IOException
block|{
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
name|long
name|maxOrd
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
argument_list|,
name|needsScores
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
name|maxOrd
operator|=
name|globalValues
operator|.
name|getValueCount
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|maxOrd
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
argument_list|,
name|needsScores
argument_list|)
return|;
block|}
specifier|final
name|Query
name|parentQuery
init|=
name|rewrittenParentQuery
decl_stmt|;
name|ParentOrdsCollector
name|collector
init|=
operator|new
name|ParentOrdsCollector
argument_list|(
name|globalIfd
argument_list|,
name|maxOrd
argument_list|,
name|parentType
argument_list|)
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
argument_list|,
name|needsScores
argument_list|)
return|;
block|}
return|return
operator|new
name|ChildrenWeight
argument_list|(
name|this
argument_list|,
name|childrenFilter
argument_list|,
name|collector
argument_list|,
name|globalIfd
argument_list|)
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
name|ParentConstantScoreQuery
name|that
init|=
operator|(
name|ParentConstantScoreQuery
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
name|this
operator|.
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
literal|"parent_filter["
operator|+
name|parentType
operator|+
literal|"]("
operator|+
name|originalParentQuery
operator|+
literal|')'
return|;
block|}
DECL|class|ChildrenWeight
specifier|private
specifier|final
class|class
name|ChildrenWeight
extends|extends
name|Weight
block|{
DECL|field|globalIfd
specifier|private
specifier|final
name|IndexParentChildFieldData
name|globalIfd
decl_stmt|;
DECL|field|childrenFilter
specifier|private
specifier|final
name|Filter
name|childrenFilter
decl_stmt|;
DECL|field|parentOrds
specifier|private
specifier|final
name|LongBitSet
name|parentOrds
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
DECL|method|ChildrenWeight
specifier|private
name|ChildrenWeight
parameter_list|(
name|Query
name|query
parameter_list|,
name|Filter
name|childrenFilter
parameter_list|,
name|ParentOrdsCollector
name|collector
parameter_list|,
name|IndexParentChildFieldData
name|globalIfd
parameter_list|)
block|{
name|super
argument_list|(
name|query
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
name|childrenFilter
operator|=
name|childrenFilter
expr_stmt|;
name|this
operator|.
name|parentOrds
operator|=
name|collector
operator|.
name|parentOrds
expr_stmt|;
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
block|{         }
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
name|DocIdSet
name|childrenDocIdSet
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
name|childrenDocIdSet
argument_list|)
condition|)
block|{
return|return
literal|null
return|;
block|}
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
name|innerIterator
init|=
name|childrenDocIdSet
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
name|ChildrenDocIdIterator
name|childrenDocIdIterator
init|=
operator|new
name|ChildrenDocIdIterator
argument_list|(
name|innerIterator
argument_list|,
name|parentOrds
argument_list|,
name|globalValues
argument_list|)
decl_stmt|;
return|return
name|ConstantScorer
operator|.
name|create
argument_list|(
name|childrenDocIdIterator
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
block|}
DECL|class|ChildrenDocIdIterator
specifier|private
specifier|final
class|class
name|ChildrenDocIdIterator
extends|extends
name|FilteredDocIdSetIterator
block|{
DECL|field|parentOrds
specifier|private
specifier|final
name|LongBitSet
name|parentOrds
decl_stmt|;
DECL|field|globalOrdinals
specifier|private
specifier|final
name|SortedDocValues
name|globalOrdinals
decl_stmt|;
DECL|method|ChildrenDocIdIterator
name|ChildrenDocIdIterator
parameter_list|(
name|DocIdSetIterator
name|innerIterator
parameter_list|,
name|LongBitSet
name|parentOrds
parameter_list|,
name|SortedDocValues
name|globalOrdinals
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
name|globalOrdinals
operator|=
name|globalOrdinals
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
name|docId
parameter_list|)
block|{
name|int
name|globalOrd
init|=
name|globalOrdinals
operator|.
name|getOrd
argument_list|(
name|docId
argument_list|)
decl_stmt|;
if|if
condition|(
name|globalOrd
operator|>=
literal|0
condition|)
block|{
return|return
name|parentOrds
operator|.
name|get
argument_list|(
name|globalOrd
argument_list|)
return|;
block|}
else|else
block|{
return|return
literal|false
return|;
block|}
block|}
block|}
DECL|class|ParentOrdsCollector
specifier|private
specifier|final
specifier|static
class|class
name|ParentOrdsCollector
extends|extends
name|NoopCollector
block|{
DECL|field|parentOrds
specifier|private
specifier|final
name|LongBitSet
name|parentOrds
decl_stmt|;
DECL|field|globalIfd
specifier|private
specifier|final
name|IndexParentChildFieldData
name|globalIfd
decl_stmt|;
DECL|field|parentType
specifier|private
specifier|final
name|String
name|parentType
decl_stmt|;
DECL|field|globalOrdinals
specifier|private
name|SortedDocValues
name|globalOrdinals
decl_stmt|;
DECL|method|ParentOrdsCollector
name|ParentOrdsCollector
parameter_list|(
name|IndexParentChildFieldData
name|globalIfd
parameter_list|,
name|long
name|maxOrd
parameter_list|,
name|String
name|parentType
parameter_list|)
block|{
name|this
operator|.
name|parentOrds
operator|=
operator|new
name|LongBitSet
argument_list|(
name|maxOrd
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
name|globalOrdinals
operator|!=
literal|null
condition|)
block|{
name|long
name|globalOrd
init|=
name|globalOrdinals
operator|.
name|getOrd
argument_list|(
name|doc
argument_list|)
decl_stmt|;
if|if
condition|(
name|globalOrd
operator|>=
literal|0
condition|)
block|{
name|parentOrds
operator|.
name|set
argument_list|(
name|globalOrd
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
DECL|method|doSetNextReader
specifier|public
name|void
name|doSetNextReader
parameter_list|(
name|LeafReaderContext
name|readerContext
parameter_list|)
throws|throws
name|IOException
block|{
name|globalOrdinals
operator|=
name|globalIfd
operator|.
name|load
argument_list|(
name|readerContext
argument_list|)
operator|.
name|getOrdinalsValues
argument_list|(
name|parentType
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
name|parentOrds
operator|.
name|cardinality
argument_list|()
return|;
block|}
block|}
block|}
end_class

end_unit

