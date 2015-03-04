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
name|*
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
name|ElasticsearchIllegalStateException
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
DECL|field|parentChildIndexFieldData
specifier|private
specifier|final
name|IndexParentChildFieldData
name|parentChildIndexFieldData
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
name|Query
name|originalChildQuery
decl_stmt|;
DECL|field|nonNestedDocsFilter
specifier|private
specifier|final
name|BitDocIdSetFilter
name|nonNestedDocsFilter
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
name|IndexParentChildFieldData
name|parentChildIndexFieldData
parameter_list|,
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
name|nonNestedDocsFilter
operator|=
name|nonNestedDocsFilter
expr_stmt|;
block|}
comment|// Rewrite invocation logic:
comment|// 1) query_then|and_fetch (default): Rewrite is execute as part of the createWeight invocation, when search child docs.
comment|// 2) dfs_query_then|and_fetch:: First rewrite and then createWeight is executed. During query phase rewrite isn't
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
DECL|method|clone
specifier|public
name|Query
name|clone
parameter_list|()
block|{
name|TopChildrenQuery
name|q
init|=
operator|(
name|TopChildrenQuery
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
parameter_list|,
name|boolean
name|needsScores
parameter_list|)
throws|throws
name|IOException
block|{
name|ObjectObjectOpenHashMap
argument_list|<
name|Object
argument_list|,
name|ParentDoc
index|[]
argument_list|>
name|parentDocs
init|=
operator|new
name|ObjectObjectOpenHashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|SearchContext
name|searchContext
init|=
name|SearchContext
operator|.
name|current
argument_list|()
decl_stmt|;
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
while|while
condition|(
literal|true
condition|)
block|{
name|parentDocs
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
try|try
block|{
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
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
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
name|this
argument_list|,
name|rewrittenChildQuery
operator|.
name|createWeight
argument_list|(
name|searcher
argument_list|,
name|needsScores
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
argument_list|,
name|Lifetime
operator|.
name|COLLECTION
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
name|ObjectObjectOpenHashMap
argument_list|<
name|Object
argument_list|,
name|ParentDoc
index|[]
argument_list|>
name|parentDocs
parameter_list|)
throws|throws
name|Exception
block|{
name|int
name|parentHitsResolved
init|=
literal|0
decl_stmt|;
name|ObjectObjectOpenHashMap
argument_list|<
name|Object
argument_list|,
name|IntObjectOpenHashMap
argument_list|<
name|ParentDoc
argument_list|>
argument_list|>
name|parentDocsPerReader
init|=
operator|new
name|ObjectObjectOpenHashMap
argument_list|<>
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
name|child_hits
label|:
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
name|LeafReaderContext
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
name|SortedDocValues
name|parentValues
init|=
name|parentChildIndexFieldData
operator|.
name|load
argument_list|(
name|subContext
argument_list|)
operator|.
name|getOrdinalsValues
argument_list|(
name|parentType
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
name|BytesRef
name|parentId
init|=
name|parentValues
operator|.
name|get
argument_list|(
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
name|LeafReaderContext
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
name|LeafReader
name|indexReader
init|=
name|atomicReaderContext
operator|.
name|reader
argument_list|()
decl_stmt|;
name|BitSet
name|nonNestedDocs
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|nonNestedDocsFilter
operator|!=
literal|null
condition|)
block|{
name|BitDocIdSet
name|nonNestedDocIdSet
init|=
name|nonNestedDocsFilter
operator|.
name|getDocIdSet
argument_list|(
name|atomicReaderContext
argument_list|)
decl_stmt|;
if|if
condition|(
name|nonNestedDocIdSet
operator|!=
literal|null
condition|)
block|{
name|nonNestedDocs
operator|=
name|nonNestedDocIdSet
operator|.
name|bits
argument_list|()
expr_stmt|;
block|}
block|}
name|Terms
name|terms
init|=
name|indexReader
operator|.
name|terms
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|)
decl_stmt|;
if|if
condition|(
name|terms
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
name|TermsEnum
name|termsEnum
init|=
name|terms
operator|.
name|iterator
argument_list|(
literal|null
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|termsEnum
operator|.
name|seekExact
argument_list|(
name|Uid
operator|.
name|createUidAsBytes
argument_list|(
name|parentType
argument_list|,
name|parentId
argument_list|)
argument_list|)
condition|)
block|{
continue|continue;
block|}
name|PostingsEnum
name|docsEnum
init|=
name|termsEnum
operator|.
name|postings
argument_list|(
name|indexReader
operator|.
name|getLiveDocs
argument_list|()
argument_list|,
literal|null
argument_list|,
name|PostingsEnum
operator|.
name|NONE
argument_list|)
decl_stmt|;
name|int
name|parentDocId
init|=
name|docsEnum
operator|.
name|nextDoc
argument_list|()
decl_stmt|;
if|if
condition|(
name|nonNestedDocs
operator|!=
literal|null
operator|&&
operator|!
name|nonNestedDocs
operator|.
name|get
argument_list|(
name|parentDocId
argument_list|)
condition|)
block|{
name|parentDocId
operator|=
name|nonNestedDocs
operator|.
name|nextSetBit
argument_list|(
name|parentDocId
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|parentDocId
operator|!=
name|DocIdSetIterator
operator|.
name|NO_MORE_DOCS
condition|)
block|{
comment|// we found a match, add it and break
name|IntObjectOpenHashMap
argument_list|<
name|ParentDoc
argument_list|>
name|readerParentDocs
init|=
name|parentDocsPerReader
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
comment|//The number of docs in the reader and in the query both upper bound the size of parentDocsPerReader
name|int
name|mapSize
init|=
name|Math
operator|.
name|min
argument_list|(
name|indexReader
operator|.
name|maxDoc
argument_list|()
argument_list|,
name|context
operator|.
name|from
argument_list|()
operator|+
name|context
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
name|readerParentDocs
operator|=
operator|new
name|IntObjectOpenHashMap
argument_list|<>
argument_list|(
name|mapSize
argument_list|)
expr_stmt|;
name|parentDocsPerReader
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
name|minScore
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
operator|<
name|parentDoc
operator|.
name|minScore
condition|)
block|{
name|parentDoc
operator|.
name|minScore
operator|=
name|scoreDoc
operator|.
name|score
expr_stmt|;
block|}
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
continue|continue
name|child_hits
continue|;
block|}
block|}
block|}
name|boolean
index|[]
name|states
init|=
name|parentDocsPerReader
operator|.
name|allocated
decl_stmt|;
name|Object
index|[]
name|keys
init|=
name|parentDocsPerReader
operator|.
name|keys
decl_stmt|;
name|Object
index|[]
name|values
init|=
name|parentDocsPerReader
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
name|IntObjectOpenHashMap
argument_list|<
name|ParentDoc
argument_list|>
name|value
init|=
operator|(
name|IntObjectOpenHashMap
argument_list|<
name|ParentDoc
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
block|}
block|}
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
name|ObjectObjectOpenHashMap
argument_list|<
name|Object
argument_list|,
name|ParentDoc
index|[]
argument_list|>
name|parentDocs
decl_stmt|;
DECL|method|ParentWeight
specifier|public
name|ParentWeight
parameter_list|(
name|Query
name|query
parameter_list|,
name|Weight
name|queryWeight
parameter_list|,
name|ObjectObjectOpenHashMap
argument_list|<
name|Object
argument_list|,
name|ParentDoc
index|[]
argument_list|>
name|parentDocs
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|query
argument_list|)
expr_stmt|;
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
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|ElasticsearchException
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
name|ParentDoc
index|[]
name|readerParentDocs
init|=
name|parentDocs
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
comment|// We ignore the needsScores parameter here because there isn't really anything that we
comment|// can improve by ignoring scores. Actually this query does not really make sense
comment|// with needsScores=false...
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
name|MIN
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
operator|&&
name|doc
operator|.
name|docId
operator|!=
name|NO_MORE_DOCS
assert|;
return|return
name|doc
operator|.
name|minScore
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
operator|&&
name|doc
operator|.
name|docId
operator|!=
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
operator|&&
name|doc
operator|.
name|docId
operator|!=
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
operator|&&
name|doc
operator|.
name|docId
operator|!=
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
name|ElasticsearchIllegalStateException
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
DECL|field|minScore
specifier|public
name|float
name|minScore
init|=
name|Float
operator|.
name|NaN
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

