begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.termvectors
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|termvectors
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
name|Iterators
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
name|util
operator|.
name|ArrayUtil
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
name|CharsRefBuilder
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
name|action
operator|.
name|ActionResponse
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|termvectors
operator|.
name|TermVectorsRequest
operator|.
name|Flag
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
name|bytes
operator|.
name|BytesArray
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
name|BytesReference
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
name|io
operator|.
name|stream
operator|.
name|BytesStreamOutput
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
name|io
operator|.
name|stream
operator|.
name|StreamInput
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
name|io
operator|.
name|stream
operator|.
name|StreamOutput
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
name|unit
operator|.
name|TimeValue
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
name|xcontent
operator|.
name|ToXContent
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
name|xcontent
operator|.
name|XContentBuilder
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
name|xcontent
operator|.
name|XContentBuilderString
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
name|dfs
operator|.
name|AggregatedDfs
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
name|EnumSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
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

begin_class
DECL|class|TermVectorsResponse
specifier|public
class|class
name|TermVectorsResponse
extends|extends
name|ActionResponse
implements|implements
name|ToXContent
block|{
DECL|class|FieldStrings
specifier|private
specifier|static
class|class
name|FieldStrings
block|{
comment|// term statistics strings
DECL|field|TTF
specifier|public
specifier|static
specifier|final
name|XContentBuilderString
name|TTF
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"ttf"
argument_list|)
decl_stmt|;
DECL|field|DOC_FREQ
specifier|public
specifier|static
specifier|final
name|XContentBuilderString
name|DOC_FREQ
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"doc_freq"
argument_list|)
decl_stmt|;
DECL|field|TERM_FREQ
specifier|public
specifier|static
specifier|final
name|XContentBuilderString
name|TERM_FREQ
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"term_freq"
argument_list|)
decl_stmt|;
comment|// field statistics strings
DECL|field|FIELD_STATISTICS
specifier|public
specifier|static
specifier|final
name|XContentBuilderString
name|FIELD_STATISTICS
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"field_statistics"
argument_list|)
decl_stmt|;
DECL|field|DOC_COUNT
specifier|public
specifier|static
specifier|final
name|XContentBuilderString
name|DOC_COUNT
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"doc_count"
argument_list|)
decl_stmt|;
DECL|field|SUM_DOC_FREQ
specifier|public
specifier|static
specifier|final
name|XContentBuilderString
name|SUM_DOC_FREQ
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"sum_doc_freq"
argument_list|)
decl_stmt|;
DECL|field|SUM_TTF
specifier|public
specifier|static
specifier|final
name|XContentBuilderString
name|SUM_TTF
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"sum_ttf"
argument_list|)
decl_stmt|;
DECL|field|TOKENS
specifier|public
specifier|static
specifier|final
name|XContentBuilderString
name|TOKENS
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"tokens"
argument_list|)
decl_stmt|;
DECL|field|POS
specifier|public
specifier|static
specifier|final
name|XContentBuilderString
name|POS
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"position"
argument_list|)
decl_stmt|;
DECL|field|START_OFFSET
specifier|public
specifier|static
specifier|final
name|XContentBuilderString
name|START_OFFSET
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"start_offset"
argument_list|)
decl_stmt|;
DECL|field|END_OFFSET
specifier|public
specifier|static
specifier|final
name|XContentBuilderString
name|END_OFFSET
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"end_offset"
argument_list|)
decl_stmt|;
DECL|field|PAYLOAD
specifier|public
specifier|static
specifier|final
name|XContentBuilderString
name|PAYLOAD
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"payload"
argument_list|)
decl_stmt|;
DECL|field|_INDEX
specifier|public
specifier|static
specifier|final
name|XContentBuilderString
name|_INDEX
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"_index"
argument_list|)
decl_stmt|;
DECL|field|_TYPE
specifier|public
specifier|static
specifier|final
name|XContentBuilderString
name|_TYPE
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"_type"
argument_list|)
decl_stmt|;
DECL|field|_ID
specifier|public
specifier|static
specifier|final
name|XContentBuilderString
name|_ID
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"_id"
argument_list|)
decl_stmt|;
DECL|field|_VERSION
specifier|public
specifier|static
specifier|final
name|XContentBuilderString
name|_VERSION
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"_version"
argument_list|)
decl_stmt|;
DECL|field|FOUND
specifier|public
specifier|static
specifier|final
name|XContentBuilderString
name|FOUND
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"found"
argument_list|)
decl_stmt|;
DECL|field|TOOK
specifier|public
specifier|static
specifier|final
name|XContentBuilderString
name|TOOK
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"took"
argument_list|)
decl_stmt|;
DECL|field|TERMS
specifier|public
specifier|static
specifier|final
name|XContentBuilderString
name|TERMS
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"terms"
argument_list|)
decl_stmt|;
DECL|field|TERM_VECTORS
specifier|public
specifier|static
specifier|final
name|XContentBuilderString
name|TERM_VECTORS
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"term_vectors"
argument_list|)
decl_stmt|;
block|}
DECL|field|termVectors
specifier|private
name|BytesReference
name|termVectors
decl_stmt|;
DECL|field|headerRef
specifier|private
name|BytesReference
name|headerRef
decl_stmt|;
DECL|field|index
specifier|private
name|String
name|index
decl_stmt|;
DECL|field|type
specifier|private
name|String
name|type
decl_stmt|;
DECL|field|id
specifier|private
name|String
name|id
decl_stmt|;
DECL|field|docVersion
specifier|private
name|long
name|docVersion
decl_stmt|;
DECL|field|exists
specifier|private
name|boolean
name|exists
init|=
literal|false
decl_stmt|;
DECL|field|artificial
specifier|private
name|boolean
name|artificial
init|=
literal|false
decl_stmt|;
DECL|field|tookInMillis
specifier|private
name|long
name|tookInMillis
decl_stmt|;
DECL|field|sourceCopied
specifier|private
name|boolean
name|sourceCopied
init|=
literal|false
decl_stmt|;
DECL|field|currentPositions
name|int
index|[]
name|currentPositions
init|=
operator|new
name|int
index|[
literal|0
index|]
decl_stmt|;
DECL|field|currentStartOffset
name|int
index|[]
name|currentStartOffset
init|=
operator|new
name|int
index|[
literal|0
index|]
decl_stmt|;
DECL|field|currentEndOffset
name|int
index|[]
name|currentEndOffset
init|=
operator|new
name|int
index|[
literal|0
index|]
decl_stmt|;
DECL|field|currentPayloads
name|BytesReference
index|[]
name|currentPayloads
init|=
operator|new
name|BytesReference
index|[
literal|0
index|]
decl_stmt|;
DECL|method|TermVectorsResponse
specifier|public
name|TermVectorsResponse
parameter_list|(
name|String
name|index
parameter_list|,
name|String
name|type
parameter_list|,
name|String
name|id
parameter_list|)
block|{
name|this
operator|.
name|index
operator|=
name|index
expr_stmt|;
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
name|this
operator|.
name|id
operator|=
name|id
expr_stmt|;
block|}
DECL|method|TermVectorsResponse
name|TermVectorsResponse
parameter_list|()
block|{     }
annotation|@
name|Override
DECL|method|writeTo
specifier|public
name|void
name|writeTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeString
argument_list|(
name|index
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeString
argument_list|(
name|type
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeString
argument_list|(
name|id
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVLong
argument_list|(
name|docVersion
argument_list|)
expr_stmt|;
specifier|final
name|boolean
name|docExists
init|=
name|isExists
argument_list|()
decl_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|docExists
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|artificial
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVLong
argument_list|(
name|tookInMillis
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|hasTermVectors
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|hasTermVectors
argument_list|()
condition|)
block|{
name|out
operator|.
name|writeBytesReference
argument_list|(
name|headerRef
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBytesReference
argument_list|(
name|termVectors
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|hasTermVectors
specifier|private
name|boolean
name|hasTermVectors
parameter_list|()
block|{
assert|assert
operator|(
name|headerRef
operator|==
literal|null
operator|&&
name|termVectors
operator|==
literal|null
operator|)
operator|||
operator|(
name|headerRef
operator|!=
literal|null
operator|&&
name|termVectors
operator|!=
literal|null
operator|)
assert|;
return|return
name|headerRef
operator|!=
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|readFrom
specifier|public
name|void
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|index
operator|=
name|in
operator|.
name|readString
argument_list|()
expr_stmt|;
name|type
operator|=
name|in
operator|.
name|readString
argument_list|()
expr_stmt|;
name|id
operator|=
name|in
operator|.
name|readString
argument_list|()
expr_stmt|;
name|docVersion
operator|=
name|in
operator|.
name|readVLong
argument_list|()
expr_stmt|;
name|exists
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
name|artificial
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
name|tookInMillis
operator|=
name|in
operator|.
name|readVLong
argument_list|()
expr_stmt|;
if|if
condition|(
name|in
operator|.
name|readBoolean
argument_list|()
condition|)
block|{
name|headerRef
operator|=
name|in
operator|.
name|readBytesReference
argument_list|()
expr_stmt|;
name|termVectors
operator|=
name|in
operator|.
name|readBytesReference
argument_list|()
expr_stmt|;
block|}
block|}
DECL|method|getFields
specifier|public
name|Fields
name|getFields
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|hasTermVectors
argument_list|()
operator|&&
name|isExists
argument_list|()
condition|)
block|{
if|if
condition|(
operator|!
name|sourceCopied
condition|)
block|{
comment|// make the bytes safe
name|headerRef
operator|=
name|headerRef
operator|.
name|copyBytesArray
argument_list|()
expr_stmt|;
name|termVectors
operator|=
name|termVectors
operator|.
name|copyBytesArray
argument_list|()
expr_stmt|;
block|}
return|return
operator|new
name|TermVectorsFields
argument_list|(
name|headerRef
argument_list|,
name|termVectors
argument_list|)
return|;
block|}
else|else
block|{
return|return
operator|new
name|Fields
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Iterator
argument_list|<
name|String
argument_list|>
name|iterator
parameter_list|()
block|{
return|return
name|Iterators
operator|.
name|emptyIterator
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|Terms
name|terms
parameter_list|(
name|String
name|field
parameter_list|)
throws|throws
name|IOException
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|size
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
block|}
return|;
block|}
block|}
annotation|@
name|Override
DECL|method|toXContent
specifier|public
name|XContentBuilder
name|toXContent
parameter_list|(
name|XContentBuilder
name|builder
parameter_list|,
name|Params
name|params
parameter_list|)
throws|throws
name|IOException
block|{
assert|assert
name|index
operator|!=
literal|null
assert|;
assert|assert
name|type
operator|!=
literal|null
assert|;
assert|assert
name|id
operator|!=
literal|null
assert|;
name|builder
operator|.
name|field
argument_list|(
name|FieldStrings
operator|.
name|_INDEX
argument_list|,
name|index
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|FieldStrings
operator|.
name|_TYPE
argument_list|,
name|type
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|isArtificial
argument_list|()
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|FieldStrings
operator|.
name|_ID
argument_list|,
name|id
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|field
argument_list|(
name|FieldStrings
operator|.
name|_VERSION
argument_list|,
name|docVersion
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|FieldStrings
operator|.
name|FOUND
argument_list|,
name|isExists
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|FieldStrings
operator|.
name|TOOK
argument_list|,
name|tookInMillis
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|isExists
argument_list|()
condition|)
block|{
return|return
name|builder
return|;
block|}
name|builder
operator|.
name|startObject
argument_list|(
name|FieldStrings
operator|.
name|TERM_VECTORS
argument_list|)
expr_stmt|;
specifier|final
name|CharsRefBuilder
name|spare
init|=
operator|new
name|CharsRefBuilder
argument_list|()
decl_stmt|;
name|Fields
name|theFields
init|=
name|getFields
argument_list|()
decl_stmt|;
name|Iterator
argument_list|<
name|String
argument_list|>
name|fieldIter
init|=
name|theFields
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|fieldIter
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|buildField
argument_list|(
name|builder
argument_list|,
name|spare
argument_list|,
name|theFields
argument_list|,
name|fieldIter
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
return|return
name|builder
return|;
block|}
DECL|method|buildField
specifier|private
name|void
name|buildField
parameter_list|(
name|XContentBuilder
name|builder
parameter_list|,
specifier|final
name|CharsRefBuilder
name|spare
parameter_list|,
name|Fields
name|theFields
parameter_list|,
name|Iterator
argument_list|<
name|String
argument_list|>
name|fieldIter
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|fieldName
init|=
name|fieldIter
operator|.
name|next
argument_list|()
decl_stmt|;
name|builder
operator|.
name|startObject
argument_list|(
name|fieldName
argument_list|)
expr_stmt|;
name|Terms
name|curTerms
init|=
name|theFields
operator|.
name|terms
argument_list|(
name|fieldName
argument_list|)
decl_stmt|;
comment|// write field statistics
name|buildFieldStatistics
argument_list|(
name|builder
argument_list|,
name|curTerms
argument_list|)
expr_stmt|;
name|builder
operator|.
name|startObject
argument_list|(
name|FieldStrings
operator|.
name|TERMS
argument_list|)
expr_stmt|;
name|TermsEnum
name|termIter
init|=
name|curTerms
operator|.
name|iterator
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
name|curTerms
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|buildTerm
argument_list|(
name|builder
argument_list|,
name|spare
argument_list|,
name|curTerms
argument_list|,
name|termIter
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
DECL|method|buildTerm
specifier|private
name|void
name|buildTerm
parameter_list|(
name|XContentBuilder
name|builder
parameter_list|,
specifier|final
name|CharsRefBuilder
name|spare
parameter_list|,
name|Terms
name|curTerms
parameter_list|,
name|TermsEnum
name|termIter
parameter_list|)
throws|throws
name|IOException
block|{
comment|// start term, optimized writing
name|BytesRef
name|term
init|=
name|termIter
operator|.
name|next
argument_list|()
decl_stmt|;
name|spare
operator|.
name|copyUTF8Bytes
argument_list|(
name|term
argument_list|)
expr_stmt|;
name|builder
operator|.
name|startObject
argument_list|(
name|spare
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|buildTermStatistics
argument_list|(
name|builder
argument_list|,
name|termIter
argument_list|)
expr_stmt|;
comment|// finally write the term vectors
name|PostingsEnum
name|posEnum
init|=
name|termIter
operator|.
name|postings
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
name|PostingsEnum
operator|.
name|ALL
argument_list|)
decl_stmt|;
name|int
name|termFreq
init|=
name|posEnum
operator|.
name|freq
argument_list|()
decl_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|FieldStrings
operator|.
name|TERM_FREQ
argument_list|,
name|termFreq
argument_list|)
expr_stmt|;
name|initMemory
argument_list|(
name|curTerms
argument_list|,
name|termFreq
argument_list|)
expr_stmt|;
name|initValues
argument_list|(
name|curTerms
argument_list|,
name|posEnum
argument_list|,
name|termFreq
argument_list|)
expr_stmt|;
name|buildValues
argument_list|(
name|builder
argument_list|,
name|curTerms
argument_list|,
name|termFreq
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
DECL|method|buildTermStatistics
specifier|private
name|void
name|buildTermStatistics
parameter_list|(
name|XContentBuilder
name|builder
parameter_list|,
name|TermsEnum
name|termIter
parameter_list|)
throws|throws
name|IOException
block|{
comment|// write term statistics. At this point we do not naturally have a
comment|// boolean that says if these values actually were requested.
comment|// However, we can assume that they were not if the statistic values are
comment|//<= 0.
assert|assert
operator|(
operator|(
operator|(
name|termIter
operator|.
name|docFreq
argument_list|()
operator|>
literal|0
operator|)
operator|&&
operator|(
name|termIter
operator|.
name|totalTermFreq
argument_list|()
operator|>
literal|0
operator|)
operator|)
operator|||
operator|(
operator|(
name|termIter
operator|.
name|docFreq
argument_list|()
operator|==
operator|-
literal|1
operator|)
operator|&&
operator|(
name|termIter
operator|.
name|totalTermFreq
argument_list|()
operator|==
operator|-
literal|1
operator|)
operator|)
operator|)
assert|;
name|int
name|docFreq
init|=
name|termIter
operator|.
name|docFreq
argument_list|()
decl_stmt|;
if|if
condition|(
name|docFreq
operator|>
literal|0
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|FieldStrings
operator|.
name|DOC_FREQ
argument_list|,
name|docFreq
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|FieldStrings
operator|.
name|TTF
argument_list|,
name|termIter
operator|.
name|totalTermFreq
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|buildValues
specifier|private
name|void
name|buildValues
parameter_list|(
name|XContentBuilder
name|builder
parameter_list|,
name|Terms
name|curTerms
parameter_list|,
name|int
name|termFreq
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
operator|(
name|curTerms
operator|.
name|hasPayloads
argument_list|()
operator|||
name|curTerms
operator|.
name|hasOffsets
argument_list|()
operator|||
name|curTerms
operator|.
name|hasPositions
argument_list|()
operator|)
condition|)
block|{
return|return;
block|}
name|builder
operator|.
name|startArray
argument_list|(
name|FieldStrings
operator|.
name|TOKENS
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|termFreq
condition|;
name|i
operator|++
control|)
block|{
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
if|if
condition|(
name|curTerms
operator|.
name|hasPositions
argument_list|()
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|FieldStrings
operator|.
name|POS
argument_list|,
name|currentPositions
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|curTerms
operator|.
name|hasOffsets
argument_list|()
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|FieldStrings
operator|.
name|START_OFFSET
argument_list|,
name|currentStartOffset
index|[
name|i
index|]
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|FieldStrings
operator|.
name|END_OFFSET
argument_list|,
name|currentEndOffset
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|curTerms
operator|.
name|hasPayloads
argument_list|()
operator|&&
operator|(
name|currentPayloads
index|[
name|i
index|]
operator|.
name|length
argument_list|()
operator|>
literal|0
operator|)
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|FieldStrings
operator|.
name|PAYLOAD
argument_list|,
name|currentPayloads
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
name|builder
operator|.
name|endArray
argument_list|()
expr_stmt|;
block|}
DECL|method|initValues
specifier|private
name|void
name|initValues
parameter_list|(
name|Terms
name|curTerms
parameter_list|,
name|PostingsEnum
name|posEnum
parameter_list|,
name|int
name|termFreq
parameter_list|)
throws|throws
name|IOException
block|{
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|termFreq
condition|;
name|j
operator|++
control|)
block|{
name|int
name|nextPos
init|=
name|posEnum
operator|.
name|nextPosition
argument_list|()
decl_stmt|;
if|if
condition|(
name|curTerms
operator|.
name|hasPositions
argument_list|()
condition|)
block|{
name|currentPositions
index|[
name|j
index|]
operator|=
name|nextPos
expr_stmt|;
block|}
if|if
condition|(
name|curTerms
operator|.
name|hasOffsets
argument_list|()
condition|)
block|{
name|currentStartOffset
index|[
name|j
index|]
operator|=
name|posEnum
operator|.
name|startOffset
argument_list|()
expr_stmt|;
name|currentEndOffset
index|[
name|j
index|]
operator|=
name|posEnum
operator|.
name|endOffset
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|curTerms
operator|.
name|hasPayloads
argument_list|()
condition|)
block|{
name|BytesRef
name|curPayload
init|=
name|posEnum
operator|.
name|getPayload
argument_list|()
decl_stmt|;
if|if
condition|(
name|curPayload
operator|!=
literal|null
condition|)
block|{
name|currentPayloads
index|[
name|j
index|]
operator|=
operator|new
name|BytesArray
argument_list|(
name|curPayload
operator|.
name|bytes
argument_list|,
literal|0
argument_list|,
name|curPayload
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|currentPayloads
index|[
name|j
index|]
operator|=
literal|null
expr_stmt|;
block|}
block|}
block|}
block|}
DECL|method|initMemory
specifier|private
name|void
name|initMemory
parameter_list|(
name|Terms
name|curTerms
parameter_list|,
name|int
name|termFreq
parameter_list|)
block|{
comment|// init memory for performance reasons
if|if
condition|(
name|curTerms
operator|.
name|hasPositions
argument_list|()
condition|)
block|{
name|currentPositions
operator|=
name|ArrayUtil
operator|.
name|grow
argument_list|(
name|currentPositions
argument_list|,
name|termFreq
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|curTerms
operator|.
name|hasOffsets
argument_list|()
condition|)
block|{
name|currentStartOffset
operator|=
name|ArrayUtil
operator|.
name|grow
argument_list|(
name|currentStartOffset
argument_list|,
name|termFreq
argument_list|)
expr_stmt|;
name|currentEndOffset
operator|=
name|ArrayUtil
operator|.
name|grow
argument_list|(
name|currentEndOffset
argument_list|,
name|termFreq
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|curTerms
operator|.
name|hasPayloads
argument_list|()
condition|)
block|{
name|currentPayloads
operator|=
operator|new
name|BytesArray
index|[
name|termFreq
index|]
expr_stmt|;
block|}
block|}
DECL|method|buildFieldStatistics
specifier|private
name|void
name|buildFieldStatistics
parameter_list|(
name|XContentBuilder
name|builder
parameter_list|,
name|Terms
name|curTerms
parameter_list|)
throws|throws
name|IOException
block|{
name|long
name|sumDocFreq
init|=
name|curTerms
operator|.
name|getSumDocFreq
argument_list|()
decl_stmt|;
name|int
name|docCount
init|=
name|curTerms
operator|.
name|getDocCount
argument_list|()
decl_stmt|;
name|long
name|sumTotalTermFrequencies
init|=
name|curTerms
operator|.
name|getSumTotalTermFreq
argument_list|()
decl_stmt|;
if|if
condition|(
name|docCount
operator|>
literal|0
condition|)
block|{
assert|assert
operator|(
operator|(
name|sumDocFreq
operator|>
literal|0
operator|)
operator|)
operator|:
literal|"docCount>= 0 but sumDocFreq ain't!"
assert|;
assert|assert
operator|(
operator|(
name|sumTotalTermFrequencies
operator|>
literal|0
operator|)
operator|)
operator|:
literal|"docCount>= 0 but sumTotalTermFrequencies ain't!"
assert|;
name|builder
operator|.
name|startObject
argument_list|(
name|FieldStrings
operator|.
name|FIELD_STATISTICS
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|FieldStrings
operator|.
name|SUM_DOC_FREQ
argument_list|,
name|sumDocFreq
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|FieldStrings
operator|.
name|DOC_COUNT
argument_list|,
name|docCount
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|FieldStrings
operator|.
name|SUM_TTF
argument_list|,
name|sumTotalTermFrequencies
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|docCount
operator|==
operator|-
literal|1
condition|)
block|{
comment|// this should only be -1 if the field
comment|// statistics were not requested at all. In
comment|// this case all 3 values should be -1
assert|assert
operator|(
operator|(
name|sumDocFreq
operator|==
operator|-
literal|1
operator|)
operator|)
operator|:
literal|"docCount was -1 but sumDocFreq ain't!"
assert|;
assert|assert
operator|(
operator|(
name|sumTotalTermFrequencies
operator|==
operator|-
literal|1
operator|)
operator|)
operator|:
literal|"docCount was -1 but sumTotalTermFrequencies ain't!"
assert|;
block|}
else|else
block|{
throw|throw
operator|new
name|ElasticsearchIllegalStateException
argument_list|(
literal|"Something is wrong with the field statistics of the term vector request: Values are "
operator|+
literal|"\n"
operator|+
name|FieldStrings
operator|.
name|SUM_DOC_FREQ
operator|+
literal|" "
operator|+
name|sumDocFreq
operator|+
literal|"\n"
operator|+
name|FieldStrings
operator|.
name|DOC_COUNT
operator|+
literal|" "
operator|+
name|docCount
operator|+
literal|"\n"
operator|+
name|FieldStrings
operator|.
name|SUM_TTF
operator|+
literal|" "
operator|+
name|sumTotalTermFrequencies
argument_list|)
throw|;
block|}
block|}
DECL|method|updateTookInMillis
specifier|public
name|void
name|updateTookInMillis
parameter_list|(
name|long
name|startTime
parameter_list|)
block|{
name|this
operator|.
name|tookInMillis
operator|=
name|Math
operator|.
name|max
argument_list|(
literal|1
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|startTime
argument_list|)
expr_stmt|;
block|}
DECL|method|getTook
specifier|public
name|TimeValue
name|getTook
parameter_list|()
block|{
return|return
operator|new
name|TimeValue
argument_list|(
name|tookInMillis
argument_list|)
return|;
block|}
DECL|method|getTookInMillis
specifier|public
name|long
name|getTookInMillis
parameter_list|()
block|{
return|return
name|tookInMillis
return|;
block|}
DECL|method|isExists
specifier|public
name|boolean
name|isExists
parameter_list|()
block|{
return|return
name|exists
return|;
block|}
DECL|method|setExists
specifier|public
name|void
name|setExists
parameter_list|(
name|boolean
name|exists
parameter_list|)
block|{
name|this
operator|.
name|exists
operator|=
name|exists
expr_stmt|;
block|}
DECL|method|setFields
specifier|public
name|void
name|setFields
parameter_list|(
name|Fields
name|termVectorsByField
parameter_list|,
name|Set
argument_list|<
name|String
argument_list|>
name|selectedFields
parameter_list|,
name|EnumSet
argument_list|<
name|Flag
argument_list|>
name|flags
parameter_list|,
name|Fields
name|topLevelFields
parameter_list|)
throws|throws
name|IOException
block|{
name|setFields
argument_list|(
name|termVectorsByField
argument_list|,
name|selectedFields
argument_list|,
name|flags
argument_list|,
name|topLevelFields
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
DECL|method|setFields
specifier|public
name|void
name|setFields
parameter_list|(
name|Fields
name|termVectorsByField
parameter_list|,
name|Set
argument_list|<
name|String
argument_list|>
name|selectedFields
parameter_list|,
name|EnumSet
argument_list|<
name|Flag
argument_list|>
name|flags
parameter_list|,
name|Fields
name|topLevelFields
parameter_list|,
annotation|@
name|Nullable
name|AggregatedDfs
name|dfs
parameter_list|)
throws|throws
name|IOException
block|{
name|TermVectorsWriter
name|tvw
init|=
operator|new
name|TermVectorsWriter
argument_list|(
name|this
argument_list|)
decl_stmt|;
if|if
condition|(
name|termVectorsByField
operator|!=
literal|null
condition|)
block|{
name|tvw
operator|.
name|setFields
argument_list|(
name|termVectorsByField
argument_list|,
name|selectedFields
argument_list|,
name|flags
argument_list|,
name|topLevelFields
argument_list|,
name|dfs
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|setTermVectorsField
specifier|public
name|void
name|setTermVectorsField
parameter_list|(
name|BytesStreamOutput
name|output
parameter_list|)
block|{
name|termVectors
operator|=
name|output
operator|.
name|bytes
argument_list|()
expr_stmt|;
block|}
DECL|method|setHeader
specifier|public
name|void
name|setHeader
parameter_list|(
name|BytesReference
name|header
parameter_list|)
block|{
name|headerRef
operator|=
name|header
expr_stmt|;
block|}
DECL|method|setDocVersion
specifier|public
name|void
name|setDocVersion
parameter_list|(
name|long
name|version
parameter_list|)
block|{
name|this
operator|.
name|docVersion
operator|=
name|version
expr_stmt|;
block|}
DECL|method|getVersion
specifier|public
name|Long
name|getVersion
parameter_list|()
block|{
return|return
name|docVersion
return|;
block|}
DECL|method|getIndex
specifier|public
name|String
name|getIndex
parameter_list|()
block|{
return|return
name|index
return|;
block|}
DECL|method|getType
specifier|public
name|String
name|getType
parameter_list|()
block|{
return|return
name|type
return|;
block|}
DECL|method|getId
specifier|public
name|String
name|getId
parameter_list|()
block|{
return|return
name|id
return|;
block|}
DECL|method|isArtificial
specifier|public
name|boolean
name|isArtificial
parameter_list|()
block|{
return|return
name|artificial
return|;
block|}
DECL|method|setArtificial
specifier|public
name|void
name|setArtificial
parameter_list|(
name|boolean
name|artificial
parameter_list|)
block|{
name|this
operator|.
name|artificial
operator|=
name|artificial
expr_stmt|;
block|}
block|}
end_class

end_unit

