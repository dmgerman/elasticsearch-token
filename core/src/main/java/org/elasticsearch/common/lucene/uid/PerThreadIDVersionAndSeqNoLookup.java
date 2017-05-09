begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_package
DECL|package|org.elasticsearch.common.lucene.uid
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|lucene
operator|.
name|uid
package|;
end_package

begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

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
name|Fields
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
name|LeafReader
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
name|NumericDocValues
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
name|common
operator|.
name|lucene
operator|.
name|uid
operator|.
name|VersionsAndSeqNoResolver
operator|.
name|DocIdAndSeqNo
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
name|uid
operator|.
name|VersionsAndSeqNoResolver
operator|.
name|DocIdAndVersion
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
name|SeqNoFieldMapper
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
name|VersionFieldMapper
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
name|seqno
operator|.
name|SequenceNumbersService
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
comment|/** Utility class to do efficient primary-key (only 1 doc contains the  *  given term) lookups by segment, re-using the enums.  This class is  *  not thread safe, so it is the caller's job to create and use one  *  instance of this per thread.  Do not use this if a term may appear  *  in more than one document!  It will only return the first one it  *  finds. */
end_comment

begin_class
DECL|class|PerThreadIDVersionAndSeqNoLookup
specifier|final
class|class
name|PerThreadIDVersionAndSeqNoLookup
block|{
comment|// TODO: do we really need to store all this stuff? some if it might not speed up anything.
comment|// we keep it around for now, to reduce the amount of e.g. hash lookups by field and stuff
comment|/** terms enum for uid field */
DECL|field|uidField
specifier|final
name|String
name|uidField
decl_stmt|;
DECL|field|termsEnum
specifier|private
specifier|final
name|TermsEnum
name|termsEnum
decl_stmt|;
comment|/** Reused for iteration (when the term exists) */
DECL|field|docsEnum
specifier|private
name|PostingsEnum
name|docsEnum
decl_stmt|;
comment|/** used for assertions to make sure class usage meets assumptions */
DECL|field|readerKey
specifier|private
specifier|final
name|Object
name|readerKey
decl_stmt|;
comment|/**      * Initialize lookup for the provided segment      */
DECL|method|PerThreadIDVersionAndSeqNoLookup
name|PerThreadIDVersionAndSeqNoLookup
parameter_list|(
name|LeafReader
name|reader
parameter_list|,
name|String
name|uidField
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|uidField
operator|=
name|uidField
expr_stmt|;
name|Fields
name|fields
init|=
name|reader
operator|.
name|fields
argument_list|()
decl_stmt|;
name|Terms
name|terms
init|=
name|fields
operator|.
name|terms
argument_list|(
name|uidField
argument_list|)
decl_stmt|;
if|if
condition|(
name|terms
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"reader misses the ["
operator|+
name|uidField
operator|+
literal|"] field"
argument_list|)
throw|;
block|}
name|termsEnum
operator|=
name|terms
operator|.
name|iterator
argument_list|()
expr_stmt|;
if|if
condition|(
name|reader
operator|.
name|getNumericDocValues
argument_list|(
name|VersionFieldMapper
operator|.
name|NAME
argument_list|)
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"reader misses the ["
operator|+
name|VersionFieldMapper
operator|.
name|NAME
operator|+
literal|"] field"
argument_list|)
throw|;
block|}
name|Object
name|readerKey
init|=
literal|null
decl_stmt|;
assert|assert
operator|(
name|readerKey
operator|=
name|reader
operator|.
name|getCoreCacheHelper
argument_list|()
operator|.
name|getKey
argument_list|()
operator|)
operator|!=
literal|null
assert|;
name|this
operator|.
name|readerKey
operator|=
name|readerKey
expr_stmt|;
block|}
comment|/** Return null if id is not found. */
DECL|method|lookupVersion
specifier|public
name|DocIdAndVersion
name|lookupVersion
parameter_list|(
name|BytesRef
name|id
parameter_list|,
name|Bits
name|liveDocs
parameter_list|,
name|LeafReaderContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
assert|assert
name|context
operator|.
name|reader
argument_list|()
operator|.
name|getCoreCacheHelper
argument_list|()
operator|.
name|getKey
argument_list|()
operator|.
name|equals
argument_list|(
name|readerKey
argument_list|)
operator|:
literal|"context's reader is not the same as the reader class was initialized on."
assert|;
name|int
name|docID
init|=
name|getDocID
argument_list|(
name|id
argument_list|,
name|liveDocs
argument_list|)
decl_stmt|;
if|if
condition|(
name|docID
operator|!=
name|DocIdSetIterator
operator|.
name|NO_MORE_DOCS
condition|)
block|{
specifier|final
name|NumericDocValues
name|versions
init|=
name|context
operator|.
name|reader
argument_list|()
operator|.
name|getNumericDocValues
argument_list|(
name|VersionFieldMapper
operator|.
name|NAME
argument_list|)
decl_stmt|;
if|if
condition|(
name|versions
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"reader misses the ["
operator|+
name|VersionFieldMapper
operator|.
name|NAME
operator|+
literal|"] field"
argument_list|)
throw|;
block|}
if|if
condition|(
name|versions
operator|.
name|advanceExact
argument_list|(
name|docID
argument_list|)
operator|==
literal|false
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Document ["
operator|+
name|docID
operator|+
literal|"] misses the ["
operator|+
name|VersionFieldMapper
operator|.
name|NAME
operator|+
literal|"] field"
argument_list|)
throw|;
block|}
return|return
operator|new
name|DocIdAndVersion
argument_list|(
name|docID
argument_list|,
name|versions
operator|.
name|longValue
argument_list|()
argument_list|,
name|context
argument_list|)
return|;
block|}
else|else
block|{
return|return
literal|null
return|;
block|}
block|}
comment|/**      * returns the internal lucene doc id for the given id bytes.      * {@link DocIdSetIterator#NO_MORE_DOCS} is returned if not found      * */
DECL|method|getDocID
specifier|private
name|int
name|getDocID
parameter_list|(
name|BytesRef
name|id
parameter_list|,
name|Bits
name|liveDocs
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|termsEnum
operator|.
name|seekExact
argument_list|(
name|id
argument_list|)
condition|)
block|{
name|int
name|docID
init|=
name|DocIdSetIterator
operator|.
name|NO_MORE_DOCS
decl_stmt|;
comment|// there may be more than one matching docID, in the case of nested docs, so we want the last one:
name|docsEnum
operator|=
name|termsEnum
operator|.
name|postings
argument_list|(
name|docsEnum
argument_list|,
literal|0
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|d
init|=
name|docsEnum
operator|.
name|nextDoc
argument_list|()
init|;
name|d
operator|!=
name|DocIdSetIterator
operator|.
name|NO_MORE_DOCS
condition|;
name|d
operator|=
name|docsEnum
operator|.
name|nextDoc
argument_list|()
control|)
block|{
if|if
condition|(
name|liveDocs
operator|!=
literal|null
operator|&&
name|liveDocs
operator|.
name|get
argument_list|(
name|d
argument_list|)
operator|==
literal|false
condition|)
block|{
continue|continue;
block|}
name|docID
operator|=
name|d
expr_stmt|;
block|}
return|return
name|docID
return|;
block|}
else|else
block|{
return|return
name|DocIdSetIterator
operator|.
name|NO_MORE_DOCS
return|;
block|}
block|}
comment|/** Return null if id is not found. */
DECL|method|lookupSeqNo
name|DocIdAndSeqNo
name|lookupSeqNo
parameter_list|(
name|BytesRef
name|id
parameter_list|,
name|Bits
name|liveDocs
parameter_list|,
name|LeafReaderContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
assert|assert
name|context
operator|.
name|reader
argument_list|()
operator|.
name|getCoreCacheHelper
argument_list|()
operator|.
name|getKey
argument_list|()
operator|.
name|equals
argument_list|(
name|readerKey
argument_list|)
operator|:
literal|"context's reader is not the same as the reader class was initialized on."
assert|;
name|int
name|docID
init|=
name|getDocID
argument_list|(
name|id
argument_list|,
name|liveDocs
argument_list|)
decl_stmt|;
if|if
condition|(
name|docID
operator|!=
name|DocIdSetIterator
operator|.
name|NO_MORE_DOCS
condition|)
block|{
name|NumericDocValues
name|seqNos
init|=
name|context
operator|.
name|reader
argument_list|()
operator|.
name|getNumericDocValues
argument_list|(
name|SeqNoFieldMapper
operator|.
name|NAME
argument_list|)
decl_stmt|;
name|long
name|seqNo
decl_stmt|;
if|if
condition|(
name|seqNos
operator|!=
literal|null
operator|&&
name|seqNos
operator|.
name|advanceExact
argument_list|(
name|docID
argument_list|)
condition|)
block|{
name|seqNo
operator|=
name|seqNos
operator|.
name|longValue
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|seqNo
operator|=
name|SequenceNumbersService
operator|.
name|UNASSIGNED_SEQ_NO
expr_stmt|;
block|}
return|return
operator|new
name|DocIdAndSeqNo
argument_list|(
name|docID
argument_list|,
name|seqNo
argument_list|,
name|context
argument_list|)
return|;
block|}
else|else
block|{
return|return
literal|null
return|;
block|}
block|}
comment|/**      * returns 0 if the primary term is not found.      *      * Note that 0 is an illegal primary term. See {@link org.elasticsearch.cluster.metadata.IndexMetaData#primaryTerm(int)}      **/
DECL|method|lookUpPrimaryTerm
name|long
name|lookUpPrimaryTerm
parameter_list|(
name|int
name|docID
parameter_list|,
name|LeafReader
name|reader
parameter_list|)
throws|throws
name|IOException
block|{
name|NumericDocValues
name|primaryTerms
init|=
name|reader
operator|.
name|getNumericDocValues
argument_list|(
name|SeqNoFieldMapper
operator|.
name|PRIMARY_TERM_NAME
argument_list|)
decl_stmt|;
if|if
condition|(
name|primaryTerms
operator|!=
literal|null
operator|&&
name|primaryTerms
operator|.
name|advanceExact
argument_list|(
name|docID
argument_list|)
condition|)
block|{
return|return
name|primaryTerms
operator|.
name|longValue
argument_list|()
return|;
block|}
else|else
block|{
return|return
literal|0
return|;
block|}
block|}
block|}
end_class

end_unit

