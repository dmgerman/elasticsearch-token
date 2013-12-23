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
name|com
operator|.
name|carrotsearch
operator|.
name|hppc
operator|.
name|IntObjectOpenHashMap
import|;
end_import

begin_import
import|import
name|com
operator|.
name|carrotsearch
operator|.
name|hppc
operator|.
name|ObjectObjectOpenHashMap
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
name|ElasticSearchException
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
name|cache
operator|.
name|recycler
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
name|lucene
operator|.
name|search
operator|.
name|EmptyScorer
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
name|common
operator|.
name|recycler
operator|.
name|RecyclerUtils
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
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Comparator
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
comment|/**  * A query that evaluates the top matching child documents (based on the score) in order to determine what  * parent documents to return. This query tries to find just enough child documents to return the the requested  * number of parent documents (or less if no other child document can be found).  *<p/>  * This query executes several internal searches. In the first round it tries to find ((request offset + requested size) * factor)  * child documents. The resulting child documents are mapped into their parent documents including the aggragted child scores.  * If not enough parent documents could be resolved then a subsequent round is executed, requesting previous requested  * documents times incremental_factor. This logic repeats until enough parent documents are resolved or until no more  * child documents are available.  *<p/>  * This query is most of the times faster than the {@link ChildrenQuery}. Usually enough parent documents can be returned  * in the first child document query round.  */
end_comment

begin_class
DECL|class|TopChildrenQuery
specifier|public
class|class
name|TopChildrenQuery
extends|extends
name|Query
block|{
DECL|field|PARENT_DOC_COMP
specifier|private
specifier|static
specifier|final
name|ParentDocComparator
name|PARENT_DOC_COMP
init|=
operator|new
name|ParentDocComparator
argument_list|()
decl_stmt|;
DECL|field|cacheRecycler
specifier|private
specifier|final
name|CacheRecycler
name|cacheRecycler
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
DECL|field|scoreType
specifier|private
specifier|final
name|ScoreType
name|scoreType
decl_stmt|;
DECL|field|factor
specifier|private
specifier|final
name|int
name|factor
decl_stmt|;
DECL|field|incrementalFactor
specifier|private
specifier|final
name|int
name|incrementalFactor
decl_stmt|;
DECL|field|originalChildQuery
specifier|private
specifier|final
name|Query
name|originalChildQuery
decl_stmt|;
comment|// This field will hold the rewritten form of originalChildQuery, so that we can reuse it
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
comment|// Note, the query is expected to already be filtered to only child type docs
DECL|method|TopChildrenQuery
specifier|public
name|TopChildrenQuery
parameter_list|(
name|Query
name|childQuery
parameter_list|,
name|String
name|childType
parameter_list|,
name|String
name|parentType
parameter_list|,
name|ScoreType
name|scoreType
parameter_list|,
name|int
name|factor
parameter_list|,
name|int
name|incrementalFactor
parameter_list|,
name|CacheRecycler
name|cacheRecycler
parameter_list|)
block|{
name|this
operator|.
name|originalChildQuery
operator|=
name|childQuery
expr_stmt|;
name|this
operator|.
name|childType
operator|=
name|childType
expr_stmt|;
name|this
operator|.
name|parentType
operator|=
name|parentType
expr_stmt|;
name|this
operator|.
name|scoreType
operator|=
name|scoreType
expr_stmt|;
name|this
operator|.
name|factor
operator|=
name|factor
expr_stmt|;
name|this
operator|.
name|incrementalFactor
operator|=
name|incrementalFactor
expr_stmt|;
name|this
operator|.
name|cacheRecycler
operator|=
name|cacheRecycler
expr_stmt|;
block|}
comment|// Rewrite invocation logic:
comment|// 1) query_then_fetch (default): Rewrite is execute as part of the createWeight invocation, when search child docs.
comment|// 2) dfs_query_then_fetch:: First rewrite and then createWeight is executed. During query phase rewrite isn't
comment|// executed any more because searchContext#queryRewritten() returns true.
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
comment|// We can always return the current instance, and we can do this b/c the child query is executed separately
comment|// before the main query (other scope) in a different IS#search() invocation than the main query.
comment|// In fact we only need override the rewrite method because for the dfs phase, to get also global document
comment|// frequency for the child query.
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
name|Recycler
operator|.
name|V
argument_list|<
name|ObjectObjectOpenHashMap
argument_list|<
name|Object
argument_list|,
name|ParentDoc
index|[]
argument_list|>
argument_list|>
name|parentDocs
init|=
name|cacheRecycler
operator|.
name|hashMap
argument_list|(
operator|-
literal|1
argument_list|)
decl_stmt|;
name|SearchContext
name|searchContext
init|=
name|SearchContext
operator|.
name|current
argument_list|()
decl_stmt|;
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
name|int
name|parentHitsResolved
decl_stmt|;
name|int
name|requestedDocs
init|=
operator|(
name|searchContext
operator|.
name|from
argument_list|()
operator|+
name|searchContext
operator|.
name|size
argument_list|()
operator|)
decl_stmt|;
if|if
condition|(
name|requestedDocs
operator|<=
literal|0
condition|)
block|{
name|requestedDocs
operator|=
literal|1
expr_stmt|;
block|}
name|int
name|numChildDocs
init|=
name|requestedDocs
operator|*
name|factor
decl_stmt|;
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
while|while
condition|(
literal|true
condition|)
block|{
name|parentDocs
operator|.
name|v
argument_list|()
operator|.
name|clear
argument_list|()
expr_stmt|;
name|TopDocs
name|topChildDocs
init|=
name|indexSearcher
operator|.
name|search
argument_list|(
name|childQuery
argument_list|,
name|numChildDocs
argument_list|)
decl_stmt|;
name|parentHitsResolved
operator|=
name|resolveParentDocuments
argument_list|(
name|topChildDocs
argument_list|,
name|searchContext
argument_list|,
name|parentDocs
argument_list|)
expr_stmt|;
comment|// check if we found enough docs, if so, break
if|if
condition|(
name|parentHitsResolved
operator|>=
name|requestedDocs
condition|)
block|{
break|break;
block|}
comment|// if we did not find enough docs, check if it make sense to search further
if|if
condition|(
name|topChildDocs
operator|.
name|totalHits
operator|<=
name|numChildDocs
condition|)
block|{
break|break;
block|}
comment|// if not, update numDocs, and search again
name|numChildDocs
operator|*=
name|incrementalFactor
expr_stmt|;
if|if
condition|(
name|numChildDocs
operator|>
name|topChildDocs
operator|.
name|totalHits
condition|)
block|{
name|numChildDocs
operator|=
name|topChildDocs
operator|.
name|totalHits
expr_stmt|;
block|}
block|}
name|ParentWeight
name|parentWeight
init|=
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
name|parentDocs
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
DECL|method|resolveParentDocuments
name|int
name|resolveParentDocuments
parameter_list|(
name|TopDocs
name|topDocs
parameter_list|,
name|SearchContext
name|context
parameter_list|,
name|Recycler
operator|.
name|V
argument_list|<
name|ObjectObjectOpenHashMap
argument_list|<
name|Object
argument_list|,
name|ParentDoc
index|[]
argument_list|>
argument_list|>
name|parentDocs
parameter_list|)
block|{
name|int
name|parentHitsResolved
init|=
literal|0
decl_stmt|;
name|Recycler
operator|.
name|V
argument_list|<
name|ObjectObjectOpenHashMap
argument_list|<
name|Object
argument_list|,
name|Recycler
operator|.
name|V
argument_list|<
name|IntObjectOpenHashMap
argument_list|<
name|ParentDoc
argument_list|>
argument_list|>
argument_list|>
argument_list|>
name|parentDocsPerReader
init|=
name|cacheRecycler
operator|.
name|hashMap
argument_list|(
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
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|ScoreDoc
name|scoreDoc
range|:
name|topDocs
operator|.
name|scoreDocs
control|)
block|{
name|int
name|readerIndex
init|=
name|ReaderUtil
operator|.
name|subIndex
argument_list|(
name|scoreDoc
operator|.
name|doc
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
name|subContext
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
name|scoreDoc
operator|.
name|doc
operator|-
name|subContext
operator|.
name|docBase
decl_stmt|;
comment|// find the parent id
name|HashedBytesArray
name|parentId
init|=
name|context
operator|.
name|idCache
argument_list|()
operator|.
name|reader
argument_list|(
name|subContext
operator|.
name|reader
argument_list|()
argument_list|)
operator|.
name|parentIdByDoc
argument_list|(
name|parentType
argument_list|,
name|subDoc
argument_list|)
decl_stmt|;
if|if
condition|(
name|parentId
operator|==
literal|null
condition|)
block|{
comment|// no parent found
continue|continue;
block|}
comment|// now go over and find the parent doc Id and reader tuple
for|for
control|(
name|AtomicReaderContext
name|atomicReaderContext
range|:
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
control|)
block|{
name|AtomicReader
name|indexReader
init|=
name|atomicReaderContext
operator|.
name|reader
argument_list|()
decl_stmt|;
name|int
name|parentDocId
init|=
name|context
operator|.
name|idCache
argument_list|()
operator|.
name|reader
argument_list|(
name|indexReader
argument_list|)
operator|.
name|docById
argument_list|(
name|parentType
argument_list|,
name|parentId
argument_list|)
decl_stmt|;
name|Bits
name|liveDocs
init|=
name|indexReader
operator|.
name|getLiveDocs
argument_list|()
decl_stmt|;
if|if
condition|(
name|parentDocId
operator|!=
operator|-
literal|1
operator|&&
operator|(
name|liveDocs
operator|==
literal|null
operator|||
name|liveDocs
operator|.
name|get
argument_list|(
name|parentDocId
argument_list|)
operator|)
condition|)
block|{
comment|// we found a match, add it and break
name|Recycler
operator|.
name|V
argument_list|<
name|IntObjectOpenHashMap
argument_list|<
name|ParentDoc
argument_list|>
argument_list|>
name|readerParentDocs
init|=
name|parentDocsPerReader
operator|.
name|v
argument_list|()
operator|.
name|get
argument_list|(
name|indexReader
operator|.
name|getCoreCacheKey
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|readerParentDocs
operator|==
literal|null
condition|)
block|{
name|readerParentDocs
operator|=
name|cacheRecycler
operator|.
name|intObjectMap
argument_list|(
name|indexReader
operator|.
name|maxDoc
argument_list|()
argument_list|)
expr_stmt|;
name|parentDocsPerReader
operator|.
name|v
argument_list|()
operator|.
name|put
argument_list|(
name|indexReader
operator|.
name|getCoreCacheKey
argument_list|()
argument_list|,
name|readerParentDocs
argument_list|)
expr_stmt|;
block|}
name|ParentDoc
name|parentDoc
init|=
name|readerParentDocs
operator|.
name|v
argument_list|()
operator|.
name|get
argument_list|(
name|parentDocId
argument_list|)
decl_stmt|;
if|if
condition|(
name|parentDoc
operator|==
literal|null
condition|)
block|{
name|parentHitsResolved
operator|++
expr_stmt|;
comment|// we have a hit on a parent
name|parentDoc
operator|=
operator|new
name|ParentDoc
argument_list|()
expr_stmt|;
name|parentDoc
operator|.
name|docId
operator|=
name|parentDocId
expr_stmt|;
name|parentDoc
operator|.
name|count
operator|=
literal|1
expr_stmt|;
name|parentDoc
operator|.
name|maxScore
operator|=
name|scoreDoc
operator|.
name|score
expr_stmt|;
name|parentDoc
operator|.
name|sumScores
operator|=
name|scoreDoc
operator|.
name|score
expr_stmt|;
name|readerParentDocs
operator|.
name|v
argument_list|()
operator|.
name|put
argument_list|(
name|parentDocId
argument_list|,
name|parentDoc
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|parentDoc
operator|.
name|count
operator|++
expr_stmt|;
name|parentDoc
operator|.
name|sumScores
operator|+=
name|scoreDoc
operator|.
name|score
expr_stmt|;
if|if
condition|(
name|scoreDoc
operator|.
name|score
operator|>
name|parentDoc
operator|.
name|maxScore
condition|)
block|{
name|parentDoc
operator|.
name|maxScore
operator|=
name|scoreDoc
operator|.
name|score
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
name|boolean
index|[]
name|states
init|=
name|parentDocsPerReader
operator|.
name|v
argument_list|()
operator|.
name|allocated
decl_stmt|;
name|Object
index|[]
name|keys
init|=
name|parentDocsPerReader
operator|.
name|v
argument_list|()
operator|.
name|keys
decl_stmt|;
name|Object
index|[]
name|values
init|=
name|parentDocsPerReader
operator|.
name|v
argument_list|()
operator|.
name|values
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|states
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|states
index|[
name|i
index|]
condition|)
block|{
name|Recycler
operator|.
name|V
argument_list|<
name|IntObjectOpenHashMap
argument_list|<
name|ParentDoc
argument_list|>
argument_list|>
name|value
init|=
operator|(
name|Recycler
operator|.
name|V
argument_list|<
name|IntObjectOpenHashMap
argument_list|<
name|ParentDoc
argument_list|>
argument_list|>
operator|)
name|values
index|[
name|i
index|]
decl_stmt|;
name|ParentDoc
index|[]
name|_parentDocs
init|=
name|value
operator|.
name|v
argument_list|()
operator|.
name|values
argument_list|()
operator|.
name|toArray
argument_list|(
name|ParentDoc
operator|.
name|class
argument_list|)
decl_stmt|;
name|Arrays
operator|.
name|sort
argument_list|(
name|_parentDocs
argument_list|,
name|PARENT_DOC_COMP
argument_list|)
expr_stmt|;
name|parentDocs
operator|.
name|v
argument_list|()
operator|.
name|put
argument_list|(
name|keys
index|[
name|i
index|]
argument_list|,
name|_parentDocs
argument_list|)
expr_stmt|;
name|value
operator|.
name|release
argument_list|()
expr_stmt|;
block|}
block|}
name|parentDocsPerReader
operator|.
name|release
argument_list|()
expr_stmt|;
return|return
name|parentHitsResolved
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
name|TopChildrenQuery
name|that
init|=
operator|(
name|TopChildrenQuery
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
name|incrementalFactor
operator|!=
name|that
operator|.
name|incrementalFactor
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
name|incrementalFactor
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
literal|"score_child["
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
expr_stmt|;
name|sb
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
DECL|class|ParentWeight
specifier|private
class|class
name|ParentWeight
extends|extends
name|Weight
implements|implements
name|Releasable
block|{
DECL|field|queryWeight
specifier|private
specifier|final
name|Weight
name|queryWeight
decl_stmt|;
DECL|field|parentDocs
specifier|private
specifier|final
name|Recycler
operator|.
name|V
argument_list|<
name|ObjectObjectOpenHashMap
argument_list|<
name|Object
argument_list|,
name|ParentDoc
index|[]
argument_list|>
argument_list|>
name|parentDocs
decl_stmt|;
DECL|method|ParentWeight
specifier|public
name|ParentWeight
parameter_list|(
name|Weight
name|queryWeight
parameter_list|,
name|Recycler
operator|.
name|V
argument_list|<
name|ObjectObjectOpenHashMap
argument_list|<
name|Object
argument_list|,
name|ParentDoc
index|[]
argument_list|>
argument_list|>
name|parentDocs
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|queryWeight
operator|=
name|queryWeight
expr_stmt|;
name|this
operator|.
name|parentDocs
operator|=
name|parentDocs
expr_stmt|;
block|}
DECL|method|getQuery
specifier|public
name|Query
name|getQuery
parameter_list|()
block|{
return|return
name|TopChildrenQuery
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
name|queryWeight
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
block|{
comment|// Nothing to normalize
block|}
annotation|@
name|Override
DECL|method|release
specifier|public
name|boolean
name|release
parameter_list|()
throws|throws
name|ElasticSearchException
block|{
name|RecyclerUtils
operator|.
name|release
argument_list|(
name|parentDocs
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
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
name|ParentDoc
index|[]
name|readerParentDocs
init|=
name|parentDocs
operator|.
name|v
argument_list|()
operator|.
name|get
argument_list|(
name|context
operator|.
name|reader
argument_list|()
operator|.
name|getCoreCacheKey
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|readerParentDocs
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|scoreType
operator|==
name|ScoreType
operator|.
name|MAX
condition|)
block|{
return|return
operator|new
name|ParentScorer
argument_list|(
name|this
argument_list|,
name|readerParentDocs
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|float
name|score
parameter_list|()
throws|throws
name|IOException
block|{
assert|assert
name|doc
operator|.
name|docId
operator|>=
literal|0
operator|||
name|doc
operator|.
name|docId
operator|<
name|NO_MORE_DOCS
assert|;
return|return
name|doc
operator|.
name|maxScore
return|;
block|}
block|}
return|;
block|}
elseif|else
if|if
condition|(
name|scoreType
operator|==
name|ScoreType
operator|.
name|AVG
condition|)
block|{
return|return
operator|new
name|ParentScorer
argument_list|(
name|this
argument_list|,
name|readerParentDocs
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|float
name|score
parameter_list|()
throws|throws
name|IOException
block|{
assert|assert
name|doc
operator|.
name|docId
operator|>=
literal|0
operator|||
name|doc
operator|.
name|docId
operator|<
name|NO_MORE_DOCS
assert|;
return|return
name|doc
operator|.
name|sumScores
operator|/
name|doc
operator|.
name|count
return|;
block|}
block|}
return|;
block|}
elseif|else
if|if
condition|(
name|scoreType
operator|==
name|ScoreType
operator|.
name|SUM
condition|)
block|{
return|return
operator|new
name|ParentScorer
argument_list|(
name|this
argument_list|,
name|readerParentDocs
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|float
name|score
parameter_list|()
throws|throws
name|IOException
block|{
assert|assert
name|doc
operator|.
name|docId
operator|>=
literal|0
operator|||
name|doc
operator|.
name|docId
operator|<
name|NO_MORE_DOCS
assert|;
return|return
name|doc
operator|.
name|sumScores
return|;
block|}
block|}
return|;
block|}
throw|throw
operator|new
name|ElasticSearchIllegalStateException
argument_list|(
literal|"No support for score type ["
operator|+
name|scoreType
operator|+
literal|"]"
argument_list|)
throw|;
block|}
return|return
operator|new
name|EmptyScorer
argument_list|(
name|this
argument_list|)
return|;
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
block|}
DECL|class|ParentScorer
specifier|private
specifier|static
specifier|abstract
class|class
name|ParentScorer
extends|extends
name|Scorer
block|{
DECL|field|spare
specifier|private
specifier|final
name|ParentDoc
name|spare
init|=
operator|new
name|ParentDoc
argument_list|()
decl_stmt|;
DECL|field|docs
specifier|protected
specifier|final
name|ParentDoc
index|[]
name|docs
decl_stmt|;
DECL|field|doc
specifier|protected
name|ParentDoc
name|doc
init|=
name|spare
decl_stmt|;
DECL|field|index
specifier|private
name|int
name|index
init|=
operator|-
literal|1
decl_stmt|;
DECL|method|ParentScorer
name|ParentScorer
parameter_list|(
name|ParentWeight
name|weight
parameter_list|,
name|ParentDoc
index|[]
name|docs
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|weight
argument_list|)
expr_stmt|;
name|this
operator|.
name|docs
operator|=
name|docs
expr_stmt|;
name|spare
operator|.
name|docId
operator|=
operator|-
literal|1
expr_stmt|;
name|spare
operator|.
name|count
operator|=
operator|-
literal|1
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|docID
specifier|public
specifier|final
name|int
name|docID
parameter_list|()
block|{
return|return
name|doc
operator|.
name|docId
return|;
block|}
annotation|@
name|Override
DECL|method|advance
specifier|public
specifier|final
name|int
name|advance
parameter_list|(
name|int
name|target
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|slowAdvance
argument_list|(
name|target
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|nextDoc
specifier|public
specifier|final
name|int
name|nextDoc
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
operator|++
name|index
operator|>=
name|docs
operator|.
name|length
condition|)
block|{
name|doc
operator|=
name|spare
expr_stmt|;
name|doc
operator|.
name|count
operator|=
literal|0
expr_stmt|;
return|return
operator|(
name|doc
operator|.
name|docId
operator|=
name|NO_MORE_DOCS
operator|)
return|;
block|}
return|return
operator|(
name|doc
operator|=
name|docs
index|[
name|index
index|]
operator|)
operator|.
name|docId
return|;
block|}
annotation|@
name|Override
DECL|method|freq
specifier|public
specifier|final
name|int
name|freq
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|doc
operator|.
name|count
return|;
comment|// The number of matches in the child doc, which is propagated to parent
block|}
annotation|@
name|Override
DECL|method|cost
specifier|public
specifier|final
name|long
name|cost
parameter_list|()
block|{
return|return
name|docs
operator|.
name|length
return|;
block|}
block|}
DECL|class|ParentDocComparator
specifier|private
specifier|static
class|class
name|ParentDocComparator
implements|implements
name|Comparator
argument_list|<
name|ParentDoc
argument_list|>
block|{
annotation|@
name|Override
DECL|method|compare
specifier|public
name|int
name|compare
parameter_list|(
name|ParentDoc
name|o1
parameter_list|,
name|ParentDoc
name|o2
parameter_list|)
block|{
return|return
name|o1
operator|.
name|docId
operator|-
name|o2
operator|.
name|docId
return|;
block|}
block|}
DECL|class|ParentDoc
specifier|private
specifier|static
class|class
name|ParentDoc
block|{
DECL|field|docId
specifier|public
name|int
name|docId
decl_stmt|;
DECL|field|count
specifier|public
name|int
name|count
decl_stmt|;
DECL|field|maxScore
specifier|public
name|float
name|maxScore
init|=
name|Float
operator|.
name|NaN
decl_stmt|;
DECL|field|sumScores
specifier|public
name|float
name|sumScores
init|=
literal|0
decl_stmt|;
block|}
block|}
end_class

end_unit

