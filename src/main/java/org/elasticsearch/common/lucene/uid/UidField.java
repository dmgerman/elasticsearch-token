begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|analysis
operator|.
name|TokenStream
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
name|analysis
operator|.
name|tokenattributes
operator|.
name|CharTermAttribute
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
name|analysis
operator|.
name|tokenattributes
operator|.
name|PayloadAttribute
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
name|document
operator|.
name|AbstractField
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
name|document
operator|.
name|Field
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
name|Lucene
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
name|io
operator|.
name|Reader
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|UidField
specifier|public
class|class
name|UidField
extends|extends
name|AbstractField
block|{
DECL|class|DocIdAndVersion
specifier|public
specifier|static
class|class
name|DocIdAndVersion
block|{
DECL|field|docId
specifier|public
specifier|final
name|int
name|docId
decl_stmt|;
DECL|field|version
specifier|public
specifier|final
name|long
name|version
decl_stmt|;
DECL|field|reader
specifier|public
specifier|final
name|IndexReader
name|reader
decl_stmt|;
DECL|method|DocIdAndVersion
specifier|public
name|DocIdAndVersion
parameter_list|(
name|int
name|docId
parameter_list|,
name|long
name|version
parameter_list|,
name|IndexReader
name|reader
parameter_list|)
block|{
name|this
operator|.
name|docId
operator|=
name|docId
expr_stmt|;
name|this
operator|.
name|version
operator|=
name|version
expr_stmt|;
name|this
operator|.
name|reader
operator|=
name|reader
expr_stmt|;
block|}
block|}
comment|// this works fine for nested docs since they don't have the payload which has the version
comment|// so we iterate till we find the one with the payload
DECL|method|loadDocIdAndVersion
specifier|public
specifier|static
name|DocIdAndVersion
name|loadDocIdAndVersion
parameter_list|(
name|IndexReader
name|reader
parameter_list|,
name|Term
name|term
parameter_list|)
block|{
name|int
name|docId
init|=
name|Lucene
operator|.
name|NO_DOC
decl_stmt|;
name|TermPositions
name|uid
init|=
literal|null
decl_stmt|;
try|try
block|{
name|uid
operator|=
name|reader
operator|.
name|termPositions
argument_list|(
name|term
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|uid
operator|.
name|next
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
comment|// no doc
block|}
comment|// Note, only master docs uid have version payload, so we can use that info to not
comment|// take them into account
do|do
block|{
name|docId
operator|=
name|uid
operator|.
name|doc
argument_list|()
expr_stmt|;
name|uid
operator|.
name|nextPosition
argument_list|()
expr_stmt|;
if|if
condition|(
operator|!
name|uid
operator|.
name|isPayloadAvailable
argument_list|()
condition|)
block|{
continue|continue;
block|}
if|if
condition|(
name|uid
operator|.
name|getPayloadLength
argument_list|()
operator|<
literal|8
condition|)
block|{
continue|continue;
block|}
name|byte
index|[]
name|payload
init|=
name|uid
operator|.
name|getPayload
argument_list|(
operator|new
name|byte
index|[
literal|8
index|]
argument_list|,
literal|0
argument_list|)
decl_stmt|;
return|return
operator|new
name|DocIdAndVersion
argument_list|(
name|docId
argument_list|,
name|Numbers
operator|.
name|bytesToLong
argument_list|(
name|payload
argument_list|)
argument_list|,
name|reader
argument_list|)
return|;
block|}
do|while
condition|(
name|uid
operator|.
name|next
argument_list|()
condition|)
do|;
return|return
operator|new
name|DocIdAndVersion
argument_list|(
name|docId
argument_list|,
operator|-
literal|2
argument_list|,
name|reader
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
return|return
operator|new
name|DocIdAndVersion
argument_list|(
name|docId
argument_list|,
operator|-
literal|2
argument_list|,
name|reader
argument_list|)
return|;
block|}
finally|finally
block|{
if|if
condition|(
name|uid
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|uid
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// nothing to do here...
block|}
block|}
block|}
block|}
comment|/**      * Load the version for the uid from the reader, returning -1 if no doc exists, or -2 if      * no version is available (for backward comp.)      */
DECL|method|loadVersion
specifier|public
specifier|static
name|long
name|loadVersion
parameter_list|(
name|IndexReader
name|reader
parameter_list|,
name|Term
name|term
parameter_list|)
block|{
name|TermPositions
name|uid
init|=
literal|null
decl_stmt|;
try|try
block|{
name|uid
operator|=
name|reader
operator|.
name|termPositions
argument_list|(
name|term
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|uid
operator|.
name|next
argument_list|()
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
comment|// Note, only master docs uid have version payload, so we can use that info to not
comment|// take them into account
do|do
block|{
name|uid
operator|.
name|nextPosition
argument_list|()
expr_stmt|;
if|if
condition|(
operator|!
name|uid
operator|.
name|isPayloadAvailable
argument_list|()
condition|)
block|{
continue|continue;
block|}
if|if
condition|(
name|uid
operator|.
name|getPayloadLength
argument_list|()
operator|<
literal|8
condition|)
block|{
continue|continue;
block|}
name|byte
index|[]
name|payload
init|=
name|uid
operator|.
name|getPayload
argument_list|(
operator|new
name|byte
index|[
literal|8
index|]
argument_list|,
literal|0
argument_list|)
decl_stmt|;
return|return
name|Numbers
operator|.
name|bytesToLong
argument_list|(
name|payload
argument_list|)
return|;
block|}
do|while
condition|(
name|uid
operator|.
name|next
argument_list|()
condition|)
do|;
return|return
operator|-
literal|2
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
return|return
operator|-
literal|2
return|;
block|}
finally|finally
block|{
if|if
condition|(
name|uid
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|uid
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// nothing to do here...
block|}
block|}
block|}
block|}
DECL|field|uid
specifier|private
name|String
name|uid
decl_stmt|;
DECL|field|version
specifier|private
name|long
name|version
decl_stmt|;
DECL|field|tokenStream
specifier|private
specifier|final
name|UidPayloadTokenStream
name|tokenStream
decl_stmt|;
DECL|method|UidField
specifier|public
name|UidField
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|uid
parameter_list|,
name|long
name|version
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|YES
argument_list|,
name|Field
operator|.
name|Index
operator|.
name|ANALYZED
argument_list|,
name|Field
operator|.
name|TermVector
operator|.
name|NO
argument_list|)
expr_stmt|;
name|this
operator|.
name|uid
operator|=
name|uid
expr_stmt|;
name|this
operator|.
name|version
operator|=
name|version
expr_stmt|;
name|this
operator|.
name|indexOptions
operator|=
name|FieldInfo
operator|.
name|IndexOptions
operator|.
name|DOCS_AND_FREQS_AND_POSITIONS
expr_stmt|;
name|this
operator|.
name|tokenStream
operator|=
operator|new
name|UidPayloadTokenStream
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|setIndexOptions
specifier|public
name|void
name|setIndexOptions
parameter_list|(
name|FieldInfo
operator|.
name|IndexOptions
name|indexOptions
parameter_list|)
block|{
comment|// never allow to set this, since we want payload!
block|}
annotation|@
name|Override
DECL|method|setOmitTermFreqAndPositions
specifier|public
name|void
name|setOmitTermFreqAndPositions
parameter_list|(
name|boolean
name|omitTermFreqAndPositions
parameter_list|)
block|{
comment|// never allow to set this, since we want payload!
block|}
DECL|method|uid
specifier|public
name|String
name|uid
parameter_list|()
block|{
return|return
name|this
operator|.
name|uid
return|;
block|}
DECL|method|setUid
specifier|public
name|void
name|setUid
parameter_list|(
name|String
name|uid
parameter_list|)
block|{
name|this
operator|.
name|uid
operator|=
name|uid
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|stringValue
specifier|public
name|String
name|stringValue
parameter_list|()
block|{
return|return
name|uid
return|;
block|}
annotation|@
name|Override
DECL|method|readerValue
specifier|public
name|Reader
name|readerValue
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
DECL|method|version
specifier|public
name|long
name|version
parameter_list|()
block|{
return|return
name|this
operator|.
name|version
return|;
block|}
DECL|method|version
specifier|public
name|void
name|version
parameter_list|(
name|long
name|version
parameter_list|)
block|{
name|this
operator|.
name|version
operator|=
name|version
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|tokenStreamValue
specifier|public
name|TokenStream
name|tokenStreamValue
parameter_list|()
block|{
return|return
name|tokenStream
return|;
block|}
DECL|class|UidPayloadTokenStream
specifier|public
specifier|static
specifier|final
class|class
name|UidPayloadTokenStream
extends|extends
name|TokenStream
block|{
DECL|field|payloadAttribute
specifier|private
specifier|final
name|PayloadAttribute
name|payloadAttribute
init|=
name|addAttribute
argument_list|(
name|PayloadAttribute
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|termAtt
specifier|private
specifier|final
name|CharTermAttribute
name|termAtt
init|=
name|addAttribute
argument_list|(
name|CharTermAttribute
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|field
specifier|private
specifier|final
name|UidField
name|field
decl_stmt|;
DECL|field|added
specifier|private
name|boolean
name|added
init|=
literal|false
decl_stmt|;
DECL|method|UidPayloadTokenStream
specifier|public
name|UidPayloadTokenStream
parameter_list|(
name|UidField
name|field
parameter_list|)
block|{
name|this
operator|.
name|field
operator|=
name|field
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|reset
specifier|public
name|void
name|reset
parameter_list|()
throws|throws
name|IOException
block|{
name|added
operator|=
literal|false
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|incrementToken
specifier|public
specifier|final
name|boolean
name|incrementToken
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|added
condition|)
block|{
return|return
literal|false
return|;
block|}
name|termAtt
operator|.
name|setLength
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|termAtt
operator|.
name|append
argument_list|(
name|field
operator|.
name|uid
argument_list|)
expr_stmt|;
name|payloadAttribute
operator|.
name|setPayload
argument_list|(
operator|new
name|Payload
argument_list|(
name|Numbers
operator|.
name|longToBytes
argument_list|(
name|field
operator|.
name|version
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|added
operator|=
literal|true
expr_stmt|;
return|return
literal|true
return|;
block|}
block|}
block|}
end_class

end_unit

