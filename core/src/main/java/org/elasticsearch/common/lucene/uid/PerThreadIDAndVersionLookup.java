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
name|Numbers
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
name|Versions
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
name|index
operator|.
name|mapper
operator|.
name|internal
operator|.
name|VersionFieldMapper
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
DECL|class|PerThreadIDAndVersionLookup
specifier|final
class|class
name|PerThreadIDAndVersionLookup
block|{
comment|// TODO: do we really need to store all this stuff? some if it might not speed up anything.
comment|// we keep it around for now, to reduce the amount of e.g. hash lookups by field and stuff
comment|/** terms enum for uid field */
DECL|field|termsEnum
specifier|private
specifier|final
name|TermsEnum
name|termsEnum
decl_stmt|;
comment|/** _version data */
DECL|field|versions
specifier|private
specifier|final
name|NumericDocValues
name|versions
decl_stmt|;
comment|/** Only true when versions are indexed as payloads instead of docvalues */
DECL|field|hasPayloads
specifier|private
specifier|final
name|boolean
name|hasPayloads
decl_stmt|;
comment|/** Reused for iteration (when the term exists) */
DECL|field|docsEnum
specifier|private
name|PostingsEnum
name|docsEnum
decl_stmt|;
comment|/** Only used for back compat, to lookup a version from payload */
DECL|field|posEnum
specifier|private
name|PostingsEnum
name|posEnum
decl_stmt|;
comment|/**      * Initialize lookup for the provided segment      */
DECL|method|PerThreadIDAndVersionLookup
specifier|public
name|PerThreadIDAndVersionLookup
parameter_list|(
name|LeafReader
name|reader
parameter_list|)
throws|throws
name|IOException
block|{
name|TermsEnum
name|termsEnum
init|=
literal|null
decl_stmt|;
name|NumericDocValues
name|versions
init|=
literal|null
decl_stmt|;
name|boolean
name|hasPayloads
init|=
literal|false
decl_stmt|;
name|Fields
name|fields
init|=
name|reader
operator|.
name|fields
argument_list|()
decl_stmt|;
if|if
condition|(
name|fields
operator|!=
literal|null
condition|)
block|{
name|Terms
name|terms
init|=
name|fields
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
operator|!=
literal|null
condition|)
block|{
name|hasPayloads
operator|=
name|terms
operator|.
name|hasPayloads
argument_list|()
expr_stmt|;
name|termsEnum
operator|=
name|terms
operator|.
name|iterator
argument_list|()
expr_stmt|;
assert|assert
name|termsEnum
operator|!=
literal|null
assert|;
name|versions
operator|=
name|reader
operator|.
name|getNumericDocValues
argument_list|(
name|VersionFieldMapper
operator|.
name|NAME
argument_list|)
expr_stmt|;
block|}
block|}
name|this
operator|.
name|versions
operator|=
name|versions
expr_stmt|;
name|this
operator|.
name|termsEnum
operator|=
name|termsEnum
expr_stmt|;
name|this
operator|.
name|hasPayloads
operator|=
name|hasPayloads
expr_stmt|;
block|}
comment|/** Return null if id is not found. */
DECL|method|lookup
specifier|public
name|DocIdAndVersion
name|lookup
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
if|if
condition|(
name|versions
operator|!=
literal|null
operator|||
name|hasPayloads
operator|==
literal|false
condition|)
block|{
comment|// Use NDV to retrieve the version, in which case we only need PostingsEnum:
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
name|int
name|docID
init|=
name|DocIdSetIterator
operator|.
name|NO_MORE_DOCS
decl_stmt|;
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
if|if
condition|(
name|docID
operator|!=
name|DocIdSetIterator
operator|.
name|NO_MORE_DOCS
condition|)
block|{
if|if
condition|(
name|versions
operator|!=
literal|null
condition|)
block|{
return|return
operator|new
name|DocIdAndVersion
argument_list|(
name|docID
argument_list|,
name|versions
operator|.
name|get
argument_list|(
name|docID
argument_list|)
argument_list|,
name|context
argument_list|)
return|;
block|}
else|else
block|{
comment|// _uid found, but no doc values and no payloads
return|return
operator|new
name|DocIdAndVersion
argument_list|(
name|docID
argument_list|,
name|Versions
operator|.
name|NOT_SET
argument_list|,
name|context
argument_list|)
return|;
block|}
block|}
block|}
comment|// ... but used to be stored as payloads; in this case we must use PostingsEnum
name|posEnum
operator|=
name|termsEnum
operator|.
name|postings
argument_list|(
name|posEnum
argument_list|,
name|PostingsEnum
operator|.
name|PAYLOADS
argument_list|)
expr_stmt|;
assert|assert
name|posEnum
operator|!=
literal|null
assert|;
comment|// terms has payloads
for|for
control|(
name|int
name|d
init|=
name|posEnum
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
name|posEnum
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
name|posEnum
operator|.
name|nextPosition
argument_list|()
expr_stmt|;
specifier|final
name|BytesRef
name|payload
init|=
name|posEnum
operator|.
name|getPayload
argument_list|()
decl_stmt|;
if|if
condition|(
name|payload
operator|!=
literal|null
operator|&&
name|payload
operator|.
name|length
operator|==
literal|8
condition|)
block|{
comment|// TODO: does this break the nested docs case?  we are not returning the last matching docID here?
return|return
operator|new
name|DocIdAndVersion
argument_list|(
name|d
argument_list|,
name|Numbers
operator|.
name|bytesToLong
argument_list|(
name|payload
argument_list|)
argument_list|,
name|context
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
end_class

end_unit

