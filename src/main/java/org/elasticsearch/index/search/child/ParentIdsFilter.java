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
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_comment
comment|/**  * Advantages over using this filter over Lucene's TermsFilter in the parent child context:  * 1) Don't need to copy all values over to a list from the id cache and then  *    copy all the ids values over to one continuous byte array. Should save a lot of of object creations and gcs..  * 2) We filter docs by one field only.  * 3) We can directly reference to values that originate from the id cache.  */
end_comment

begin_class
DECL|class|ParentIdsFilter
specifier|final
class|class
name|ParentIdsFilter
extends|extends
name|Filter
block|{
DECL|field|parentTypeBr
specifier|private
specifier|final
name|BytesRef
name|parentTypeBr
decl_stmt|;
DECL|field|keys
specifier|private
specifier|final
name|Object
index|[]
name|keys
decl_stmt|;
DECL|field|allocated
specifier|private
specifier|final
name|boolean
index|[]
name|allocated
decl_stmt|;
DECL|method|ParentIdsFilter
specifier|public
name|ParentIdsFilter
parameter_list|(
name|String
name|parentType
parameter_list|,
name|Object
index|[]
name|keys
parameter_list|,
name|boolean
index|[]
name|allocated
parameter_list|)
block|{
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
name|keys
operator|=
name|keys
expr_stmt|;
name|this
operator|.
name|allocated
operator|=
name|allocated
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getDocIdSet
specifier|public
name|DocIdSet
name|getDocIdSet
parameter_list|(
name|AtomicReaderContext
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
name|BytesRef
name|uidSpare
init|=
operator|new
name|BytesRef
argument_list|()
decl_stmt|;
name|BytesRef
name|idSpare
init|=
operator|new
name|BytesRef
argument_list|()
decl_stmt|;
name|DocsEnum
name|docsEnum
init|=
literal|null
decl_stmt|;
name|FixedBitSet
name|result
init|=
literal|null
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
name|allocated
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
operator|!
name|allocated
index|[
name|i
index|]
condition|)
block|{
continue|continue;
block|}
name|idSpare
operator|.
name|bytes
operator|=
operator|(
operator|(
name|HashedBytesArray
operator|)
name|keys
index|[
name|i
index|]
operator|)
operator|.
name|toBytes
argument_list|()
expr_stmt|;
name|idSpare
operator|.
name|length
operator|=
name|idSpare
operator|.
name|bytes
operator|.
name|length
expr_stmt|;
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
expr_stmt|;
if|if
condition|(
name|termsEnum
operator|.
name|seekExact
argument_list|(
name|uidSpare
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
name|result
operator|.
name|set
argument_list|(
name|docId
argument_list|)
expr_stmt|;
block|}
else|else
block|{
continue|continue;
block|}
block|}
for|for
control|(
name|docId
operator|=
name|docsEnum
operator|.
name|nextDoc
argument_list|()
init|;
name|docId
operator|<
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
name|result
operator|.
name|set
argument_list|(
name|docId
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|result
return|;
block|}
block|}
end_class

end_unit

