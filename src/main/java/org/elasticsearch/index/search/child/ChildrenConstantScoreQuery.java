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
name|queries
operator|.
name|TermFilter
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
name|FixedBitSet
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
name|BytesRefHash
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
name|BytesValues
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
name|ordinals
operator|.
name|Ordinals
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
name|ParentChildIndexFieldData
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
name|Filter
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
name|Filter
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
name|ParentChildIndexFieldData
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
name|Filter
name|parentFilter
parameter_list|,
name|int
name|shortCircuitParentDocSet
parameter_list|,
name|Filter
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
name|searchContext
init|=
name|SearchContext
operator|.
name|current
argument_list|()
decl_stmt|;
name|BytesRefHash
name|parentIds
init|=
operator|new
name|BytesRefHash
argument_list|(
literal|512
argument_list|,
name|searchContext
operator|.
name|bigArrays
argument_list|()
argument_list|)
decl_stmt|;
name|ParentIdCollector
name|collector
init|=
operator|new
name|ParentIdCollector
argument_list|(
name|parentType
argument_list|,
name|parentChildIndexFieldData
argument_list|,
name|parentIds
argument_list|)
decl_stmt|;
specifier|final
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
name|searcher
operator|.
name|rewrite
argument_list|(
name|originalChildQuery
argument_list|)
expr_stmt|;
block|}
else|else
block|{
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
name|childQuery
operator|=
name|rewrittenChildQuery
expr_stmt|;
block|}
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
name|childQuery
argument_list|,
name|collector
argument_list|)
expr_stmt|;
name|long
name|remaining
init|=
name|parentIds
operator|.
name|size
argument_list|()
decl_stmt|;
if|if
condition|(
name|remaining
operator|==
literal|0
condition|)
block|{
name|Releasables
operator|.
name|release
argument_list|(
name|parentIds
argument_list|)
expr_stmt|;
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
operator|==
literal|1
condition|)
block|{
name|BytesRef
name|id
init|=
name|parentIds
operator|.
name|get
argument_list|(
literal|0
argument_list|,
operator|new
name|BytesRef
argument_list|()
argument_list|)
decl_stmt|;
name|shortCircuitFilter
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
name|remaining
operator|<=
name|shortCircuitParentDocSet
condition|)
block|{
name|shortCircuitFilter
operator|=
operator|new
name|ParentIdsFilter
argument_list|(
name|parentType
argument_list|,
name|nonNestedDocsFilter
argument_list|,
name|parentIds
argument_list|)
expr_stmt|;
block|}
name|ParentWeight
name|parentWeight
init|=
operator|new
name|ParentWeight
argument_list|(
name|parentFilter
argument_list|,
name|shortCircuitFilter
argument_list|,
name|parentIds
argument_list|)
decl_stmt|;
name|searchContext
operator|.
name|addReleasable
argument_list|(
name|parentWeight
argument_list|)
expr_stmt|;
return|return
name|parentWeight
return|;
block|}
DECL|class|ParentWeight
specifier|private
specifier|final
class|class
name|ParentWeight
extends|extends
name|Weight
implements|implements
name|Releasable
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
DECL|field|parentIds
specifier|private
specifier|final
name|BytesRefHash
name|parentIds
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
name|Filter
name|shortCircuitFilter
parameter_list|,
name|BytesRefHash
name|parentIds
parameter_list|)
block|{
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
name|shortCircuitFilter
operator|=
name|shortCircuitFilter
expr_stmt|;
name|this
operator|.
name|parentIds
operator|=
name|parentIds
expr_stmt|;
name|this
operator|.
name|remaining
operator|=
name|parentIds
operator|.
name|size
argument_list|()
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
name|BytesValues
name|bytesValues
init|=
name|parentChildIndexFieldData
operator|.
name|load
argument_list|(
name|context
argument_list|)
operator|.
name|getBytesValues
argument_list|(
name|parentType
argument_list|)
decl_stmt|;
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
if|if
condition|(
name|bytesValues
operator|!=
literal|null
condition|)
block|{
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
name|ParentDocIdIterator
name|parentDocIdIterator
init|=
operator|new
name|ParentDocIdIterator
argument_list|(
name|innerIterator
argument_list|,
name|parentIds
argument_list|,
name|bytesValues
argument_list|)
decl_stmt|;
return|return
name|ConstantScorer
operator|.
name|create
argument_list|(
name|parentDocIdIterator
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
annotation|@
name|Override
DECL|method|release
specifier|public
name|boolean
name|release
parameter_list|()
throws|throws
name|ElasticsearchException
block|{
name|Releasables
operator|.
name|release
argument_list|(
name|parentIds
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
DECL|class|ParentDocIdIterator
specifier|private
specifier|final
class|class
name|ParentDocIdIterator
extends|extends
name|FilteredDocIdSetIterator
block|{
DECL|field|parentIds
specifier|private
specifier|final
name|BytesRefHash
name|parentIds
decl_stmt|;
DECL|field|values
specifier|private
specifier|final
name|BytesValues
name|values
decl_stmt|;
DECL|method|ParentDocIdIterator
specifier|private
name|ParentDocIdIterator
parameter_list|(
name|DocIdSetIterator
name|innerIterator
parameter_list|,
name|BytesRefHash
name|parentIds
parameter_list|,
name|BytesValues
name|values
parameter_list|)
block|{
name|super
argument_list|(
name|innerIterator
argument_list|)
expr_stmt|;
name|this
operator|.
name|parentIds
operator|=
name|parentIds
expr_stmt|;
name|this
operator|.
name|values
operator|=
name|values
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
name|remaining
operator|==
literal|0
condition|)
block|{
try|try
block|{
name|advance
argument_list|(
name|DocIdSetIterator
operator|.
name|NO_MORE_DOCS
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
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
return|return
literal|false
return|;
block|}
name|values
operator|.
name|setDocument
argument_list|(
name|doc
argument_list|)
expr_stmt|;
name|BytesRef
name|parentId
init|=
name|values
operator|.
name|nextValue
argument_list|()
decl_stmt|;
name|int
name|hash
init|=
name|values
operator|.
name|currentValueHash
argument_list|()
decl_stmt|;
name|boolean
name|match
init|=
name|parentIds
operator|.
name|find
argument_list|(
name|parentId
argument_list|,
name|hash
argument_list|)
operator|>=
literal|0
decl_stmt|;
if|if
condition|(
name|match
condition|)
block|{
name|remaining
operator|--
expr_stmt|;
block|}
return|return
name|match
return|;
block|}
block|}
block|}
DECL|class|ParentIdCollector
specifier|private
specifier|final
specifier|static
class|class
name|ParentIdCollector
extends|extends
name|NoopCollector
block|{
DECL|field|parentIds
specifier|private
specifier|final
name|BytesRefHash
name|parentIds
decl_stmt|;
DECL|field|parentType
specifier|private
specifier|final
name|String
name|parentType
decl_stmt|;
DECL|field|indexFieldData
specifier|private
specifier|final
name|ParentChildIndexFieldData
name|indexFieldData
decl_stmt|;
DECL|field|values
specifier|protected
name|BytesValues
operator|.
name|WithOrdinals
name|values
decl_stmt|;
DECL|field|ordinals
specifier|private
name|Ordinals
operator|.
name|Docs
name|ordinals
decl_stmt|;
comment|// This remembers what ordinals have already been seen in the current segment
comment|// and prevents from fetch the actual id from FD and checking if it exists in parentIds
DECL|field|seenOrdinals
specifier|private
name|FixedBitSet
name|seenOrdinals
decl_stmt|;
DECL|method|ParentIdCollector
specifier|protected
name|ParentIdCollector
parameter_list|(
name|String
name|parentType
parameter_list|,
name|ParentChildIndexFieldData
name|indexFieldData
parameter_list|,
name|BytesRefHash
name|parentIds
parameter_list|)
block|{
name|this
operator|.
name|parentType
operator|=
name|parentType
expr_stmt|;
name|this
operator|.
name|indexFieldData
operator|=
name|indexFieldData
expr_stmt|;
name|this
operator|.
name|parentIds
operator|=
name|parentIds
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
name|ord
init|=
operator|(
name|int
operator|)
name|ordinals
operator|.
name|getOrd
argument_list|(
name|doc
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|seenOrdinals
operator|.
name|get
argument_list|(
name|ord
argument_list|)
condition|)
block|{
specifier|final
name|BytesRef
name|bytes
init|=
name|values
operator|.
name|getValueByOrd
argument_list|(
name|ord
argument_list|)
decl_stmt|;
specifier|final
name|int
name|hash
init|=
name|values
operator|.
name|currentValueHash
argument_list|()
decl_stmt|;
name|parentIds
operator|.
name|add
argument_list|(
name|bytes
argument_list|,
name|hash
argument_list|)
expr_stmt|;
name|seenOrdinals
operator|.
name|set
argument_list|(
name|ord
argument_list|)
expr_stmt|;
block|}
block|}
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
name|values
operator|=
name|indexFieldData
operator|.
name|load
argument_list|(
name|context
argument_list|)
operator|.
name|getBytesValues
argument_list|(
name|parentType
argument_list|)
expr_stmt|;
if|if
condition|(
name|values
operator|!=
literal|null
condition|)
block|{
name|ordinals
operator|=
name|values
operator|.
name|ordinals
argument_list|()
expr_stmt|;
specifier|final
name|int
name|maxOrd
init|=
operator|(
name|int
operator|)
name|ordinals
operator|.
name|getMaxOrd
argument_list|()
decl_stmt|;
if|if
condition|(
name|seenOrdinals
operator|==
literal|null
operator|||
name|seenOrdinals
operator|.
name|length
argument_list|()
operator|<
name|maxOrd
condition|)
block|{
name|seenOrdinals
operator|=
operator|new
name|FixedBitSet
argument_list|(
name|maxOrd
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|seenOrdinals
operator|.
name|clear
argument_list|(
literal|0
argument_list|,
name|maxOrd
argument_list|)
expr_stmt|;
block|}
block|}
block|}
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
literal|"child_filter["
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
argument_list|)
operator|.
name|append
argument_list|(
literal|')'
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
end_class

end_unit

