begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.lucene.index
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|lucene
operator|.
name|index
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
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
name|PostingsEnum
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
name|elasticsearch
operator|.
name|ElasticsearchIllegalArgumentException
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
name|Nullable
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

begin_comment
comment|/**  * A frequency TermsEnum that returns frequencies derived from a collection of  * cached leaf termEnums. It also allows to provide a filter to explicitly  * compute frequencies only for docs that match the filter (heavier!).  */
end_comment

begin_class
DECL|class|FilterableTermsEnum
specifier|public
class|class
name|FilterableTermsEnum
extends|extends
name|TermsEnum
block|{
DECL|class|Holder
specifier|static
class|class
name|Holder
block|{
DECL|field|termsEnum
specifier|final
name|TermsEnum
name|termsEnum
decl_stmt|;
annotation|@
name|Nullable
DECL|field|docsEnum
name|PostingsEnum
name|docsEnum
decl_stmt|;
annotation|@
name|Nullable
DECL|field|bits
specifier|final
name|Bits
name|bits
decl_stmt|;
DECL|method|Holder
name|Holder
parameter_list|(
name|TermsEnum
name|termsEnum
parameter_list|,
name|Bits
name|bits
parameter_list|)
block|{
name|this
operator|.
name|termsEnum
operator|=
name|termsEnum
expr_stmt|;
name|this
operator|.
name|bits
operator|=
name|bits
expr_stmt|;
block|}
block|}
DECL|field|UNSUPPORTED_MESSAGE
specifier|static
specifier|final
name|String
name|UNSUPPORTED_MESSAGE
init|=
literal|"This TermsEnum only supports #seekExact(BytesRef) as well as #docFreq() and #totalTermFreq()"
decl_stmt|;
DECL|field|NOT_FOUND
specifier|protected
specifier|final
specifier|static
name|int
name|NOT_FOUND
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|enums
specifier|private
specifier|final
name|Holder
index|[]
name|enums
decl_stmt|;
DECL|field|currentDocFreq
specifier|protected
name|int
name|currentDocFreq
init|=
literal|0
decl_stmt|;
DECL|field|currentTotalTermFreq
specifier|protected
name|long
name|currentTotalTermFreq
init|=
literal|0
decl_stmt|;
DECL|field|current
specifier|protected
name|BytesRef
name|current
decl_stmt|;
DECL|field|docsEnumFlag
specifier|protected
specifier|final
name|int
name|docsEnumFlag
decl_stmt|;
DECL|field|numDocs
specifier|protected
name|int
name|numDocs
decl_stmt|;
DECL|method|FilterableTermsEnum
specifier|public
name|FilterableTermsEnum
parameter_list|(
name|IndexReader
name|reader
parameter_list|,
name|String
name|field
parameter_list|,
name|int
name|docsEnumFlag
parameter_list|,
annotation|@
name|Nullable
specifier|final
name|Filter
name|filter
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
operator|(
name|docsEnumFlag
operator|!=
name|PostingsEnum
operator|.
name|FREQS
operator|)
operator|&&
operator|(
name|docsEnumFlag
operator|!=
name|PostingsEnum
operator|.
name|NONE
operator|)
condition|)
block|{
throw|throw
operator|new
name|ElasticsearchIllegalArgumentException
argument_list|(
literal|"invalid docsEnumFlag of "
operator|+
name|docsEnumFlag
argument_list|)
throw|;
block|}
name|this
operator|.
name|docsEnumFlag
operator|=
name|docsEnumFlag
expr_stmt|;
if|if
condition|(
name|filter
operator|==
literal|null
condition|)
block|{
comment|// Important - need to use the doc count that includes deleted docs
comment|// or we have this issue: https://github.com/elasticsearch/elasticsearch/issues/7951
name|numDocs
operator|=
name|reader
operator|.
name|maxDoc
argument_list|()
expr_stmt|;
block|}
name|List
argument_list|<
name|LeafReaderContext
argument_list|>
name|leaves
init|=
name|reader
operator|.
name|leaves
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Holder
argument_list|>
name|enums
init|=
name|Lists
operator|.
name|newArrayListWithExpectedSize
argument_list|(
name|leaves
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|LeafReaderContext
name|context
range|:
name|leaves
control|)
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
name|field
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
name|termsEnum
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
name|Bits
name|bits
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|filter
operator|!=
literal|null
condition|)
block|{
comment|// we want to force apply deleted docs
name|DocIdSet
name|docIdSet
init|=
name|filter
operator|.
name|getDocIdSet
argument_list|(
name|context
argument_list|,
name|context
operator|.
name|reader
argument_list|()
operator|.
name|getLiveDocs
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|DocIdSets
operator|.
name|isEmpty
argument_list|(
name|docIdSet
argument_list|)
condition|)
block|{
comment|// fully filtered, none matching, no need to iterate on this
continue|continue;
block|}
name|bits
operator|=
name|DocIdSets
operator|.
name|toSafeBits
argument_list|(
name|context
operator|.
name|reader
argument_list|()
operator|.
name|maxDoc
argument_list|()
argument_list|,
name|docIdSet
argument_list|)
expr_stmt|;
comment|// Count how many docs are in our filtered set
comment|// TODO make this lazy-loaded only for those that need it?
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
while|while
condition|(
name|iterator
operator|.
name|nextDoc
argument_list|()
operator|!=
name|DocIdSetIterator
operator|.
name|NO_MORE_DOCS
condition|)
block|{
name|numDocs
operator|++
expr_stmt|;
block|}
block|}
block|}
name|enums
operator|.
name|add
argument_list|(
operator|new
name|Holder
argument_list|(
name|termsEnum
argument_list|,
name|bits
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|enums
operator|=
name|enums
operator|.
name|toArray
argument_list|(
operator|new
name|Holder
index|[
name|enums
operator|.
name|size
argument_list|()
index|]
argument_list|)
expr_stmt|;
block|}
DECL|method|getNumDocs
specifier|public
name|int
name|getNumDocs
parameter_list|()
block|{
return|return
name|numDocs
return|;
block|}
annotation|@
name|Override
DECL|method|term
specifier|public
name|BytesRef
name|term
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|current
return|;
block|}
annotation|@
name|Override
DECL|method|seekExact
specifier|public
name|boolean
name|seekExact
parameter_list|(
name|BytesRef
name|text
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|docFreq
init|=
literal|0
decl_stmt|;
name|long
name|totalTermFreq
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Holder
name|anEnum
range|:
name|enums
control|)
block|{
if|if
condition|(
name|anEnum
operator|.
name|termsEnum
operator|.
name|seekExact
argument_list|(
name|text
argument_list|)
condition|)
block|{
if|if
condition|(
name|anEnum
operator|.
name|bits
operator|==
literal|null
condition|)
block|{
name|docFreq
operator|+=
name|anEnum
operator|.
name|termsEnum
operator|.
name|docFreq
argument_list|()
expr_stmt|;
if|if
condition|(
name|docsEnumFlag
operator|==
name|PostingsEnum
operator|.
name|FREQS
condition|)
block|{
name|long
name|leafTotalTermFreq
init|=
name|anEnum
operator|.
name|termsEnum
operator|.
name|totalTermFreq
argument_list|()
decl_stmt|;
if|if
condition|(
name|totalTermFreq
operator|==
operator|-
literal|1
operator|||
name|leafTotalTermFreq
operator|==
operator|-
literal|1
condition|)
block|{
name|totalTermFreq
operator|=
operator|-
literal|1
expr_stmt|;
continue|continue;
block|}
name|totalTermFreq
operator|+=
name|leafTotalTermFreq
expr_stmt|;
block|}
block|}
else|else
block|{
specifier|final
name|PostingsEnum
name|docsEnum
init|=
name|anEnum
operator|.
name|docsEnum
operator|=
name|anEnum
operator|.
name|termsEnum
operator|.
name|postings
argument_list|(
name|anEnum
operator|.
name|bits
argument_list|,
name|anEnum
operator|.
name|docsEnum
argument_list|,
name|docsEnumFlag
argument_list|)
decl_stmt|;
comment|// 2 choices for performing same heavy loop - one attempts to calculate totalTermFreq and other does not
if|if
condition|(
name|docsEnumFlag
operator|==
name|PostingsEnum
operator|.
name|FREQS
condition|)
block|{
for|for
control|(
name|int
name|docId
init|=
name|docsEnum
operator|.
name|nextDoc
argument_list|()
init|;
name|docId
operator|!=
name|DocIdSetIterator
operator|.
name|NO_MORE_DOCS
condition|;
name|docId
operator|=
name|docsEnum
operator|.
name|nextDoc
argument_list|()
control|)
block|{
name|docFreq
operator|++
expr_stmt|;
comment|// docsEnum.freq() returns 1 if doc indexed with IndexOptions.DOCS_ONLY so no way of knowing if value
comment|// is really 1 or unrecorded when filtering like this
name|totalTermFreq
operator|+=
name|docsEnum
operator|.
name|freq
argument_list|()
expr_stmt|;
block|}
block|}
else|else
block|{
for|for
control|(
name|int
name|docId
init|=
name|docsEnum
operator|.
name|nextDoc
argument_list|()
init|;
name|docId
operator|!=
name|DocIdSetIterator
operator|.
name|NO_MORE_DOCS
condition|;
name|docId
operator|=
name|docsEnum
operator|.
name|nextDoc
argument_list|()
control|)
block|{
comment|// docsEnum.freq() behaviour is undefined if docsEnumFlag==PostingsEnum.FLAG_NONE so don't bother with call
name|docFreq
operator|++
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
if|if
condition|(
name|docFreq
operator|>
literal|0
condition|)
block|{
name|currentDocFreq
operator|=
name|docFreq
expr_stmt|;
name|currentTotalTermFreq
operator|=
name|totalTermFreq
expr_stmt|;
name|current
operator|=
name|text
expr_stmt|;
return|return
literal|true
return|;
block|}
else|else
block|{
name|currentDocFreq
operator|=
name|NOT_FOUND
expr_stmt|;
name|currentTotalTermFreq
operator|=
name|NOT_FOUND
expr_stmt|;
name|current
operator|=
literal|null
expr_stmt|;
return|return
literal|false
return|;
block|}
block|}
annotation|@
name|Override
DECL|method|docFreq
specifier|public
name|int
name|docFreq
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|currentDocFreq
return|;
block|}
annotation|@
name|Override
DECL|method|totalTermFreq
specifier|public
name|long
name|totalTermFreq
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|currentTotalTermFreq
return|;
block|}
annotation|@
name|Override
DECL|method|seekExact
specifier|public
name|void
name|seekExact
parameter_list|(
name|long
name|ord
parameter_list|)
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
name|UNSUPPORTED_MESSAGE
argument_list|)
throw|;
block|}
annotation|@
name|Override
DECL|method|seekCeil
specifier|public
name|SeekStatus
name|seekCeil
parameter_list|(
name|BytesRef
name|text
parameter_list|)
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
name|UNSUPPORTED_MESSAGE
argument_list|)
throw|;
block|}
annotation|@
name|Override
DECL|method|ord
specifier|public
name|long
name|ord
parameter_list|()
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
name|UNSUPPORTED_MESSAGE
argument_list|)
throw|;
block|}
annotation|@
name|Override
DECL|method|postings
specifier|public
name|PostingsEnum
name|postings
parameter_list|(
name|Bits
name|liveDocs
parameter_list|,
name|PostingsEnum
name|reuse
parameter_list|,
name|int
name|flags
parameter_list|)
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
name|UNSUPPORTED_MESSAGE
argument_list|)
throw|;
block|}
annotation|@
name|Override
DECL|method|next
specifier|public
name|BytesRef
name|next
parameter_list|()
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
name|UNSUPPORTED_MESSAGE
argument_list|)
throw|;
block|}
block|}
end_class

end_unit

