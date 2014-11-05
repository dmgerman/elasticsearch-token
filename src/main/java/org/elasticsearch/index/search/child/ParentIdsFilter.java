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
name|DocsEnum
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
name|index
operator|.
name|Terms
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
name|TermsEnum
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
name|BitDocIdSet
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
name|BitSet
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
name|BytesRefBuilder
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
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|SparseFixedBitSet
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
name|search
operator|.
name|AndFilter
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
name|Arrays
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

begin_comment
comment|/**  * Advantages over using this filter over Lucene's TermsFilter in the parent child context:  * 1) Don't need to copy all values over to a list from the id cache and then  * copy all the ids values over to one continuous byte array. Should save a lot of of object creations and gcs..  * 2) We filter docs by one field only.  */
end_comment

begin_class
DECL|class|ParentIdsFilter
specifier|final
class|class
name|ParentIdsFilter
extends|extends
name|Filter
block|{
DECL|method|createShortCircuitFilter
specifier|static
name|Filter
name|createShortCircuitFilter
parameter_list|(
name|BitDocIdSetFilter
name|nonNestedDocsFilter
parameter_list|,
name|SearchContext
name|searchContext
parameter_list|,
name|String
name|parentType
parameter_list|,
name|SortedDocValues
name|globalValues
parameter_list|,
name|LongBitSet
name|parentOrds
parameter_list|,
name|long
name|numFoundParents
parameter_list|)
block|{
if|if
condition|(
name|numFoundParents
operator|==
literal|1
condition|)
block|{
name|BytesRef
name|id
init|=
name|globalValues
operator|.
name|lookupOrd
argument_list|(
operator|(
name|int
operator|)
name|parentOrds
operator|.
name|nextSetBit
argument_list|(
literal|0
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|nonNestedDocsFilter
operator|!=
literal|null
condition|)
block|{
name|List
argument_list|<
name|Filter
argument_list|>
name|filters
init|=
name|Arrays
operator|.
name|asList
argument_list|(
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
argument_list|,
name|nonNestedDocsFilter
argument_list|)
decl_stmt|;
return|return
operator|new
name|AndFilter
argument_list|(
name|filters
argument_list|)
return|;
block|}
else|else
block|{
return|return
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
return|;
block|}
block|}
else|else
block|{
name|BytesRefHash
name|parentIds
init|=
literal|null
decl_stmt|;
name|boolean
name|constructed
init|=
literal|false
decl_stmt|;
try|try
block|{
name|parentIds
operator|=
operator|new
name|BytesRefHash
argument_list|(
name|numFoundParents
argument_list|,
name|searchContext
operator|.
name|bigArrays
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|long
name|parentOrd
init|=
name|parentOrds
operator|.
name|nextSetBit
argument_list|(
literal|0
argument_list|)
init|;
name|parentOrd
operator|!=
operator|-
literal|1
condition|;
name|parentOrd
operator|=
name|parentOrds
operator|.
name|nextSetBit
argument_list|(
name|parentOrd
operator|+
literal|1
argument_list|)
control|)
block|{
name|parentIds
operator|.
name|add
argument_list|(
name|globalValues
operator|.
name|lookupOrd
argument_list|(
operator|(
name|int
operator|)
name|parentOrd
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|constructed
operator|=
literal|true
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
operator|!
name|constructed
condition|)
block|{
name|Releasables
operator|.
name|close
argument_list|(
name|parentIds
argument_list|)
expr_stmt|;
block|}
block|}
name|searchContext
operator|.
name|addReleasable
argument_list|(
name|parentIds
argument_list|,
name|SearchContext
operator|.
name|Lifetime
operator|.
name|COLLECTION
argument_list|)
expr_stmt|;
return|return
operator|new
name|ParentIdsFilter
argument_list|(
name|parentType
argument_list|,
name|nonNestedDocsFilter
argument_list|,
name|parentIds
argument_list|)
return|;
block|}
block|}
DECL|method|createShortCircuitFilter
specifier|static
name|Filter
name|createShortCircuitFilter
parameter_list|(
name|BitDocIdSetFilter
name|nonNestedDocsFilter
parameter_list|,
name|SearchContext
name|searchContext
parameter_list|,
name|String
name|parentType
parameter_list|,
name|SortedDocValues
name|globalValues
parameter_list|,
name|LongHash
name|parentIdxs
parameter_list|,
name|long
name|numFoundParents
parameter_list|)
block|{
if|if
condition|(
name|numFoundParents
operator|==
literal|1
condition|)
block|{
name|BytesRef
name|id
init|=
name|globalValues
operator|.
name|lookupOrd
argument_list|(
operator|(
name|int
operator|)
name|parentIdxs
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|nonNestedDocsFilter
operator|!=
literal|null
condition|)
block|{
name|List
argument_list|<
name|Filter
argument_list|>
name|filters
init|=
name|Arrays
operator|.
name|asList
argument_list|(
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
argument_list|,
name|nonNestedDocsFilter
argument_list|)
decl_stmt|;
return|return
operator|new
name|AndFilter
argument_list|(
name|filters
argument_list|)
return|;
block|}
else|else
block|{
return|return
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
return|;
block|}
block|}
else|else
block|{
name|BytesRefHash
name|parentIds
init|=
literal|null
decl_stmt|;
name|boolean
name|constructed
init|=
literal|false
decl_stmt|;
try|try
block|{
name|parentIds
operator|=
operator|new
name|BytesRefHash
argument_list|(
name|numFoundParents
argument_list|,
name|searchContext
operator|.
name|bigArrays
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|id
init|=
literal|0
init|;
name|id
operator|<
name|parentIdxs
operator|.
name|size
argument_list|()
condition|;
name|id
operator|++
control|)
block|{
name|parentIds
operator|.
name|add
argument_list|(
name|globalValues
operator|.
name|lookupOrd
argument_list|(
operator|(
name|int
operator|)
name|parentIdxs
operator|.
name|get
argument_list|(
name|id
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|constructed
operator|=
literal|true
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
operator|!
name|constructed
condition|)
block|{
name|Releasables
operator|.
name|close
argument_list|(
name|parentIds
argument_list|)
expr_stmt|;
block|}
block|}
name|searchContext
operator|.
name|addReleasable
argument_list|(
name|parentIds
argument_list|,
name|SearchContext
operator|.
name|Lifetime
operator|.
name|COLLECTION
argument_list|)
expr_stmt|;
return|return
operator|new
name|ParentIdsFilter
argument_list|(
name|parentType
argument_list|,
name|nonNestedDocsFilter
argument_list|,
name|parentIds
argument_list|)
return|;
block|}
block|}
DECL|field|parentTypeBr
specifier|private
specifier|final
name|BytesRef
name|parentTypeBr
decl_stmt|;
DECL|field|nonNestedDocsFilter
specifier|private
specifier|final
name|BitDocIdSetFilter
name|nonNestedDocsFilter
decl_stmt|;
DECL|field|parentIds
specifier|private
specifier|final
name|BytesRefHash
name|parentIds
decl_stmt|;
DECL|method|ParentIdsFilter
specifier|private
name|ParentIdsFilter
parameter_list|(
name|String
name|parentType
parameter_list|,
name|BitDocIdSetFilter
name|nonNestedDocsFilter
parameter_list|,
name|BytesRefHash
name|parentIds
parameter_list|)
block|{
name|this
operator|.
name|nonNestedDocsFilter
operator|=
name|nonNestedDocsFilter
expr_stmt|;
name|this
operator|.
name|parentTypeBr
operator|=
operator|new
name|BytesRef
argument_list|(
name|parentType
argument_list|)
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
DECL|method|getDocIdSet
specifier|public
name|DocIdSet
name|getDocIdSet
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
name|Terms
name|terms
init|=
name|context
operator|.
name|reader
argument_list|()
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
return|return
literal|null
return|;
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
name|BytesRefBuilder
name|uidSpare
init|=
operator|new
name|BytesRefBuilder
argument_list|()
decl_stmt|;
name|BytesRef
name|idSpare
init|=
operator|new
name|BytesRef
argument_list|()
decl_stmt|;
if|if
condition|(
name|acceptDocs
operator|==
literal|null
condition|)
block|{
name|acceptDocs
operator|=
name|context
operator|.
name|reader
argument_list|()
operator|.
name|getLiveDocs
argument_list|()
expr_stmt|;
block|}
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
name|nonNestedDocs
operator|=
name|nonNestedDocsFilter
operator|.
name|getDocIdSet
argument_list|(
name|context
argument_list|)
operator|.
name|bits
argument_list|()
expr_stmt|;
block|}
name|DocsEnum
name|docsEnum
init|=
literal|null
decl_stmt|;
name|BitSet
name|result
init|=
literal|null
decl_stmt|;
name|int
name|size
init|=
operator|(
name|int
operator|)
name|parentIds
operator|.
name|size
argument_list|()
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
name|size
condition|;
name|i
operator|++
control|)
block|{
name|parentIds
operator|.
name|get
argument_list|(
name|i
argument_list|,
name|idSpare
argument_list|)
expr_stmt|;
name|BytesRef
name|uid
init|=
name|Uid
operator|.
name|createUidAsBytes
argument_list|(
name|parentTypeBr
argument_list|,
name|idSpare
argument_list|,
name|uidSpare
argument_list|)
decl_stmt|;
if|if
condition|(
name|termsEnum
operator|.
name|seekExact
argument_list|(
name|uid
argument_list|)
condition|)
block|{
name|int
name|docId
decl_stmt|;
name|docsEnum
operator|=
name|termsEnum
operator|.
name|docs
argument_list|(
name|acceptDocs
argument_list|,
name|docsEnum
argument_list|,
name|DocsEnum
operator|.
name|FLAG_NONE
argument_list|)
expr_stmt|;
if|if
condition|(
name|result
operator|==
literal|null
condition|)
block|{
name|docId
operator|=
name|docsEnum
operator|.
name|nextDoc
argument_list|()
expr_stmt|;
if|if
condition|(
name|docId
operator|!=
name|DocIdSetIterator
operator|.
name|NO_MORE_DOCS
condition|)
block|{
comment|// very rough heuristic that tries to get an idea of the number of documents
comment|// in the set based on the number of parent ids that we didn't find in this segment
specifier|final
name|int
name|expectedCardinality
init|=
name|size
operator|/
operator|(
name|i
operator|+
literal|1
operator|)
decl_stmt|;
comment|// similar heuristic to BitDocIdSet.Builder
if|if
condition|(
name|expectedCardinality
operator|>=
operator|(
name|context
operator|.
name|reader
argument_list|()
operator|.
name|maxDoc
argument_list|()
operator|>>>
literal|10
operator|)
condition|)
block|{
name|result
operator|=
operator|new
name|FixedBitSet
argument_list|(
name|context
operator|.
name|reader
argument_list|()
operator|.
name|maxDoc
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|result
operator|=
operator|new
name|SparseFixedBitSet
argument_list|(
name|context
operator|.
name|reader
argument_list|()
operator|.
name|maxDoc
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
continue|continue;
block|}
block|}
else|else
block|{
name|docId
operator|=
name|docsEnum
operator|.
name|nextDoc
argument_list|()
expr_stmt|;
if|if
condition|(
name|docId
operator|==
name|DocIdSetIterator
operator|.
name|NO_MORE_DOCS
condition|)
block|{
continue|continue;
block|}
block|}
if|if
condition|(
name|nonNestedDocs
operator|!=
literal|null
condition|)
block|{
name|docId
operator|=
name|nonNestedDocs
operator|.
name|nextSetBit
argument_list|(
name|docId
argument_list|)
expr_stmt|;
block|}
name|result
operator|.
name|set
argument_list|(
name|docId
argument_list|)
expr_stmt|;
assert|assert
name|docsEnum
operator|.
name|advance
argument_list|(
name|docId
operator|+
literal|1
argument_list|)
operator|==
name|DocIdSetIterator
operator|.
name|NO_MORE_DOCS
operator|:
literal|"DocId "
operator|+
name|docId
operator|+
literal|" should have been the last one but docId "
operator|+
name|docsEnum
operator|.
name|docID
argument_list|()
operator|+
literal|" exists."
assert|;
block|}
block|}
return|return
name|result
operator|==
literal|null
condition|?
literal|null
else|:
operator|new
name|BitDocIdSet
argument_list|(
name|result
argument_list|)
return|;
block|}
block|}
end_class

end_unit

