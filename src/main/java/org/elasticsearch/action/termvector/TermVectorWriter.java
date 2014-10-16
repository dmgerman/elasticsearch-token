begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.termvector
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|termvector
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
name|CollectionStatistics
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
name|TermStatistics
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
name|action
operator|.
name|termvector
operator|.
name|TermVectorRequest
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
name|ArrayList
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
name|List
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
comment|// package only - this is an internal class!
end_comment

begin_class
DECL|class|TermVectorWriter
specifier|final
class|class
name|TermVectorWriter
block|{
DECL|field|fields
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|fields
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
DECL|field|fieldOffset
specifier|final
name|List
argument_list|<
name|Long
argument_list|>
name|fieldOffset
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
DECL|field|output
specifier|final
name|BytesStreamOutput
name|output
init|=
operator|new
name|BytesStreamOutput
argument_list|(
literal|1
argument_list|)
decl_stmt|;
comment|// can we somehow
comment|// predict the
comment|// size here?
DECL|field|HEADER
specifier|private
specifier|static
specifier|final
name|String
name|HEADER
init|=
literal|"TV"
decl_stmt|;
DECL|field|CURRENT_VERSION
specifier|private
specifier|static
specifier|final
name|int
name|CURRENT_VERSION
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|response
name|TermVectorResponse
name|response
init|=
literal|null
decl_stmt|;
DECL|method|TermVectorWriter
name|TermVectorWriter
parameter_list|(
name|TermVectorResponse
name|termVectorResponse
parameter_list|)
throws|throws
name|IOException
block|{
name|response
operator|=
name|termVectorResponse
expr_stmt|;
block|}
DECL|method|setFields
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
name|int
name|numFieldsWritten
init|=
literal|0
decl_stmt|;
name|TermsEnum
name|iterator
init|=
literal|null
decl_stmt|;
name|DocsAndPositionsEnum
name|docsAndPosEnum
init|=
literal|null
decl_stmt|;
name|DocsEnum
name|docsEnum
init|=
literal|null
decl_stmt|;
name|TermsEnum
name|topLevelIterator
init|=
literal|null
decl_stmt|;
for|for
control|(
name|String
name|field
range|:
name|termVectorsByField
control|)
block|{
if|if
condition|(
operator|(
name|selectedFields
operator|!=
literal|null
operator|)
operator|&&
operator|(
operator|!
name|selectedFields
operator|.
name|contains
argument_list|(
name|field
argument_list|)
operator|)
condition|)
block|{
continue|continue;
block|}
name|Terms
name|fieldTermVector
init|=
name|termVectorsByField
operator|.
name|terms
argument_list|(
name|field
argument_list|)
decl_stmt|;
name|Terms
name|topLevelTerms
init|=
name|topLevelFields
operator|.
name|terms
argument_list|(
name|field
argument_list|)
decl_stmt|;
comment|// if no terms found, take the retrieved term vector fields for stats
if|if
condition|(
name|topLevelTerms
operator|==
literal|null
condition|)
block|{
name|topLevelTerms
operator|=
name|fieldTermVector
expr_stmt|;
block|}
name|topLevelIterator
operator|=
name|topLevelTerms
operator|.
name|iterator
argument_list|(
name|topLevelIterator
argument_list|)
expr_stmt|;
name|boolean
name|positions
init|=
name|flags
operator|.
name|contains
argument_list|(
name|Flag
operator|.
name|Positions
argument_list|)
operator|&&
name|fieldTermVector
operator|.
name|hasPositions
argument_list|()
decl_stmt|;
name|boolean
name|offsets
init|=
name|flags
operator|.
name|contains
argument_list|(
name|Flag
operator|.
name|Offsets
argument_list|)
operator|&&
name|fieldTermVector
operator|.
name|hasOffsets
argument_list|()
decl_stmt|;
name|boolean
name|payloads
init|=
name|flags
operator|.
name|contains
argument_list|(
name|Flag
operator|.
name|Payloads
argument_list|)
operator|&&
name|fieldTermVector
operator|.
name|hasPayloads
argument_list|()
decl_stmt|;
name|startField
argument_list|(
name|field
argument_list|,
name|fieldTermVector
operator|.
name|size
argument_list|()
argument_list|,
name|positions
argument_list|,
name|offsets
argument_list|,
name|payloads
argument_list|)
expr_stmt|;
if|if
condition|(
name|flags
operator|.
name|contains
argument_list|(
name|Flag
operator|.
name|FieldStatistics
argument_list|)
condition|)
block|{
if|if
condition|(
name|dfs
operator|!=
literal|null
condition|)
block|{
name|writeFieldStatistics
argument_list|(
name|dfs
operator|.
name|fieldStatistics
argument_list|()
operator|.
name|get
argument_list|(
name|field
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|writeFieldStatistics
argument_list|(
name|topLevelTerms
argument_list|)
expr_stmt|;
block|}
block|}
name|iterator
operator|=
name|fieldTermVector
operator|.
name|iterator
argument_list|(
name|iterator
argument_list|)
expr_stmt|;
specifier|final
name|boolean
name|useDocsAndPos
init|=
name|positions
operator|||
name|offsets
operator|||
name|payloads
decl_stmt|;
while|while
condition|(
name|iterator
operator|.
name|next
argument_list|()
operator|!=
literal|null
condition|)
block|{
comment|// iterate all terms of the
comment|// current field
comment|// get the doc frequency
name|BytesRef
name|term
init|=
name|iterator
operator|.
name|term
argument_list|()
decl_stmt|;
name|boolean
name|foundTerm
init|=
name|topLevelIterator
operator|.
name|seekExact
argument_list|(
name|term
argument_list|)
decl_stmt|;
name|startTerm
argument_list|(
name|term
argument_list|)
expr_stmt|;
if|if
condition|(
name|flags
operator|.
name|contains
argument_list|(
name|Flag
operator|.
name|TermStatistics
argument_list|)
condition|)
block|{
if|if
condition|(
name|dfs
operator|!=
literal|null
condition|)
block|{
name|writeTermStatistics
argument_list|(
name|dfs
operator|.
name|termStatistics
argument_list|()
operator|.
name|get
argument_list|(
operator|new
name|Term
argument_list|(
name|field
argument_list|,
name|term
operator|.
name|utf8ToString
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|writeTermStatistics
argument_list|(
name|topLevelIterator
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|useDocsAndPos
condition|)
block|{
comment|// given we have pos or offsets
name|docsAndPosEnum
operator|=
name|writeTermWithDocsAndPos
argument_list|(
name|iterator
argument_list|,
name|docsAndPosEnum
argument_list|,
name|positions
argument_list|,
name|offsets
argument_list|,
name|payloads
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// if we do not have the positions stored, we need to
comment|// get the frequency from a DocsEnum.
name|docsEnum
operator|=
name|writeTermWithDocsOnly
argument_list|(
name|iterator
argument_list|,
name|docsEnum
argument_list|)
expr_stmt|;
block|}
block|}
name|numFieldsWritten
operator|++
expr_stmt|;
block|}
name|response
operator|.
name|setTermVectorField
argument_list|(
name|output
argument_list|)
expr_stmt|;
name|response
operator|.
name|setHeader
argument_list|(
name|writeHeader
argument_list|(
name|numFieldsWritten
argument_list|,
name|flags
operator|.
name|contains
argument_list|(
name|Flag
operator|.
name|TermStatistics
argument_list|)
argument_list|,
name|flags
operator|.
name|contains
argument_list|(
name|Flag
operator|.
name|FieldStatistics
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|writeHeader
specifier|private
name|BytesReference
name|writeHeader
parameter_list|(
name|int
name|numFieldsWritten
parameter_list|,
name|boolean
name|getTermStatistics
parameter_list|,
name|boolean
name|getFieldStatistics
parameter_list|)
throws|throws
name|IOException
block|{
comment|// now, write the information about offset of the terms in the
comment|// termVectors field
name|BytesStreamOutput
name|header
init|=
operator|new
name|BytesStreamOutput
argument_list|()
decl_stmt|;
name|header
operator|.
name|writeString
argument_list|(
name|HEADER
argument_list|)
expr_stmt|;
name|header
operator|.
name|writeInt
argument_list|(
name|CURRENT_VERSION
argument_list|)
expr_stmt|;
name|header
operator|.
name|writeBoolean
argument_list|(
name|getTermStatistics
argument_list|)
expr_stmt|;
name|header
operator|.
name|writeBoolean
argument_list|(
name|getFieldStatistics
argument_list|)
expr_stmt|;
name|header
operator|.
name|writeVInt
argument_list|(
name|numFieldsWritten
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
name|fields
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|header
operator|.
name|writeString
argument_list|(
name|fields
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|header
operator|.
name|writeVLong
argument_list|(
name|fieldOffset
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|longValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|header
operator|.
name|close
argument_list|()
expr_stmt|;
return|return
name|header
operator|.
name|bytes
argument_list|()
return|;
block|}
DECL|method|writeTermWithDocsOnly
specifier|private
name|DocsEnum
name|writeTermWithDocsOnly
parameter_list|(
name|TermsEnum
name|iterator
parameter_list|,
name|DocsEnum
name|docsEnum
parameter_list|)
throws|throws
name|IOException
block|{
name|docsEnum
operator|=
name|iterator
operator|.
name|docs
argument_list|(
literal|null
argument_list|,
name|docsEnum
argument_list|)
expr_stmt|;
name|int
name|nextDoc
init|=
name|docsEnum
operator|.
name|nextDoc
argument_list|()
decl_stmt|;
assert|assert
name|nextDoc
operator|!=
name|DocsEnum
operator|.
name|NO_MORE_DOCS
assert|;
name|writeFreq
argument_list|(
name|docsEnum
operator|.
name|freq
argument_list|()
argument_list|)
expr_stmt|;
name|nextDoc
operator|=
name|docsEnum
operator|.
name|nextDoc
argument_list|()
expr_stmt|;
assert|assert
name|nextDoc
operator|==
name|DocsEnum
operator|.
name|NO_MORE_DOCS
assert|;
return|return
name|docsEnum
return|;
block|}
DECL|method|writeTermWithDocsAndPos
specifier|private
name|DocsAndPositionsEnum
name|writeTermWithDocsAndPos
parameter_list|(
name|TermsEnum
name|iterator
parameter_list|,
name|DocsAndPositionsEnum
name|docsAndPosEnum
parameter_list|,
name|boolean
name|positions
parameter_list|,
name|boolean
name|offsets
parameter_list|,
name|boolean
name|payloads
parameter_list|)
throws|throws
name|IOException
block|{
name|docsAndPosEnum
operator|=
name|iterator
operator|.
name|docsAndPositions
argument_list|(
literal|null
argument_list|,
name|docsAndPosEnum
argument_list|)
expr_stmt|;
comment|// for each term (iterator next) in this field (field)
comment|// iterate over the docs (should only be one)
name|int
name|nextDoc
init|=
name|docsAndPosEnum
operator|.
name|nextDoc
argument_list|()
decl_stmt|;
assert|assert
name|nextDoc
operator|!=
name|DocsEnum
operator|.
name|NO_MORE_DOCS
assert|;
specifier|final
name|int
name|freq
init|=
name|docsAndPosEnum
operator|.
name|freq
argument_list|()
decl_stmt|;
name|writeFreq
argument_list|(
name|freq
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|freq
condition|;
name|j
operator|++
control|)
block|{
name|int
name|curPos
init|=
name|docsAndPosEnum
operator|.
name|nextPosition
argument_list|()
decl_stmt|;
if|if
condition|(
name|positions
condition|)
block|{
name|writePosition
argument_list|(
name|curPos
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|offsets
condition|)
block|{
name|writeOffsets
argument_list|(
name|docsAndPosEnum
operator|.
name|startOffset
argument_list|()
argument_list|,
name|docsAndPosEnum
operator|.
name|endOffset
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|payloads
condition|)
block|{
name|writePayload
argument_list|(
name|docsAndPosEnum
operator|.
name|getPayload
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|nextDoc
operator|=
name|docsAndPosEnum
operator|.
name|nextDoc
argument_list|()
expr_stmt|;
assert|assert
name|nextDoc
operator|==
name|DocsEnum
operator|.
name|NO_MORE_DOCS
assert|;
return|return
name|docsAndPosEnum
return|;
block|}
DECL|method|writePayload
specifier|private
name|void
name|writePayload
parameter_list|(
name|BytesRef
name|payload
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|payload
operator|!=
literal|null
condition|)
block|{
name|output
operator|.
name|writeVInt
argument_list|(
name|payload
operator|.
name|length
argument_list|)
expr_stmt|;
name|output
operator|.
name|writeBytes
argument_list|(
name|payload
operator|.
name|bytes
argument_list|,
name|payload
operator|.
name|offset
argument_list|,
name|payload
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|output
operator|.
name|writeVInt
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|writeFreq
specifier|private
name|void
name|writeFreq
parameter_list|(
name|int
name|termFreq
parameter_list|)
throws|throws
name|IOException
block|{
name|writePotentiallyNegativeVInt
argument_list|(
name|termFreq
argument_list|)
expr_stmt|;
block|}
DECL|method|writeOffsets
specifier|private
name|void
name|writeOffsets
parameter_list|(
name|int
name|startOffset
parameter_list|,
name|int
name|endOffset
parameter_list|)
throws|throws
name|IOException
block|{
assert|assert
operator|(
name|startOffset
operator|>=
literal|0
operator|)
assert|;
assert|assert
operator|(
name|endOffset
operator|>=
literal|0
operator|)
assert|;
if|if
condition|(
operator|(
name|startOffset
operator|>=
literal|0
operator|)
operator|&&
operator|(
name|endOffset
operator|>=
literal|0
operator|)
condition|)
block|{
name|output
operator|.
name|writeVInt
argument_list|(
name|startOffset
argument_list|)
expr_stmt|;
name|output
operator|.
name|writeVInt
argument_list|(
name|endOffset
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|writePosition
specifier|private
name|void
name|writePosition
parameter_list|(
name|int
name|pos
parameter_list|)
throws|throws
name|IOException
block|{
assert|assert
operator|(
name|pos
operator|>=
literal|0
operator|)
assert|;
if|if
condition|(
name|pos
operator|>=
literal|0
condition|)
block|{
name|output
operator|.
name|writeVInt
argument_list|(
name|pos
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|startField
specifier|private
name|void
name|startField
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|long
name|termsSize
parameter_list|,
name|boolean
name|writePositions
parameter_list|,
name|boolean
name|writeOffsets
parameter_list|,
name|boolean
name|writePayloads
parameter_list|)
throws|throws
name|IOException
block|{
name|fields
operator|.
name|add
argument_list|(
name|fieldName
argument_list|)
expr_stmt|;
name|fieldOffset
operator|.
name|add
argument_list|(
name|output
operator|.
name|position
argument_list|()
argument_list|)
expr_stmt|;
name|output
operator|.
name|writeVLong
argument_list|(
name|termsSize
argument_list|)
expr_stmt|;
comment|// add information on if positions etc. are written
name|output
operator|.
name|writeBoolean
argument_list|(
name|writePositions
argument_list|)
expr_stmt|;
name|output
operator|.
name|writeBoolean
argument_list|(
name|writeOffsets
argument_list|)
expr_stmt|;
name|output
operator|.
name|writeBoolean
argument_list|(
name|writePayloads
argument_list|)
expr_stmt|;
block|}
DECL|method|startTerm
specifier|private
name|void
name|startTerm
parameter_list|(
name|BytesRef
name|term
parameter_list|)
throws|throws
name|IOException
block|{
name|output
operator|.
name|writeVInt
argument_list|(
name|term
operator|.
name|length
argument_list|)
expr_stmt|;
name|output
operator|.
name|writeBytes
argument_list|(
name|term
operator|.
name|bytes
argument_list|,
name|term
operator|.
name|offset
argument_list|,
name|term
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
DECL|method|writeTermStatistics
specifier|private
name|void
name|writeTermStatistics
parameter_list|(
name|TermsEnum
name|topLevelIterator
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|docFreq
init|=
name|topLevelIterator
operator|.
name|docFreq
argument_list|()
decl_stmt|;
assert|assert
operator|(
name|docFreq
operator|>=
operator|-
literal|1
operator|)
assert|;
name|writePotentiallyNegativeVInt
argument_list|(
name|docFreq
argument_list|)
expr_stmt|;
name|long
name|ttf
init|=
name|topLevelIterator
operator|.
name|totalTermFreq
argument_list|()
decl_stmt|;
assert|assert
operator|(
name|ttf
operator|>=
operator|-
literal|1
operator|)
assert|;
name|writePotentiallyNegativeVLong
argument_list|(
name|ttf
argument_list|)
expr_stmt|;
block|}
DECL|method|writeTermStatistics
specifier|private
name|void
name|writeTermStatistics
parameter_list|(
name|TermStatistics
name|termStatistics
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|docFreq
init|=
operator|(
name|int
operator|)
name|termStatistics
operator|.
name|docFreq
argument_list|()
decl_stmt|;
assert|assert
operator|(
name|docFreq
operator|>=
operator|-
literal|1
operator|)
assert|;
name|writePotentiallyNegativeVInt
argument_list|(
name|docFreq
argument_list|)
expr_stmt|;
name|long
name|ttf
init|=
name|termStatistics
operator|.
name|totalTermFreq
argument_list|()
decl_stmt|;
assert|assert
operator|(
name|ttf
operator|>=
operator|-
literal|1
operator|)
assert|;
name|writePotentiallyNegativeVLong
argument_list|(
name|ttf
argument_list|)
expr_stmt|;
block|}
DECL|method|writeFieldStatistics
specifier|private
name|void
name|writeFieldStatistics
parameter_list|(
name|Terms
name|topLevelTerms
parameter_list|)
throws|throws
name|IOException
block|{
name|long
name|sttf
init|=
name|topLevelTerms
operator|.
name|getSumTotalTermFreq
argument_list|()
decl_stmt|;
assert|assert
operator|(
name|sttf
operator|>=
operator|-
literal|1
operator|)
assert|;
name|writePotentiallyNegativeVLong
argument_list|(
name|sttf
argument_list|)
expr_stmt|;
name|long
name|sdf
init|=
name|topLevelTerms
operator|.
name|getSumDocFreq
argument_list|()
decl_stmt|;
assert|assert
operator|(
name|sdf
operator|>=
operator|-
literal|1
operator|)
assert|;
name|writePotentiallyNegativeVLong
argument_list|(
name|sdf
argument_list|)
expr_stmt|;
name|int
name|dc
init|=
name|topLevelTerms
operator|.
name|getDocCount
argument_list|()
decl_stmt|;
assert|assert
operator|(
name|dc
operator|>=
operator|-
literal|1
operator|)
assert|;
name|writePotentiallyNegativeVInt
argument_list|(
name|dc
argument_list|)
expr_stmt|;
block|}
DECL|method|writeFieldStatistics
specifier|private
name|void
name|writeFieldStatistics
parameter_list|(
name|CollectionStatistics
name|fieldStats
parameter_list|)
throws|throws
name|IOException
block|{
name|long
name|sttf
init|=
name|fieldStats
operator|.
name|sumTotalTermFreq
argument_list|()
decl_stmt|;
assert|assert
operator|(
name|sttf
operator|>=
operator|-
literal|1
operator|)
assert|;
name|writePotentiallyNegativeVLong
argument_list|(
name|sttf
argument_list|)
expr_stmt|;
name|long
name|sdf
init|=
name|fieldStats
operator|.
name|sumDocFreq
argument_list|()
decl_stmt|;
assert|assert
operator|(
name|sdf
operator|>=
operator|-
literal|1
operator|)
assert|;
name|writePotentiallyNegativeVLong
argument_list|(
name|sdf
argument_list|)
expr_stmt|;
name|int
name|dc
init|=
operator|(
name|int
operator|)
name|fieldStats
operator|.
name|docCount
argument_list|()
decl_stmt|;
assert|assert
operator|(
name|dc
operator|>=
operator|-
literal|1
operator|)
assert|;
name|writePotentiallyNegativeVInt
argument_list|(
name|dc
argument_list|)
expr_stmt|;
block|}
DECL|method|writePotentiallyNegativeVInt
specifier|private
name|void
name|writePotentiallyNegativeVInt
parameter_list|(
name|int
name|value
parameter_list|)
throws|throws
name|IOException
block|{
comment|// term freq etc. can be negative if not present... we transport that
comment|// further...
name|output
operator|.
name|writeVInt
argument_list|(
name|Math
operator|.
name|max
argument_list|(
literal|0
argument_list|,
name|value
operator|+
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|writePotentiallyNegativeVLong
specifier|private
name|void
name|writePotentiallyNegativeVLong
parameter_list|(
name|long
name|value
parameter_list|)
throws|throws
name|IOException
block|{
comment|// term freq etc. can be negative if not present... we transport that
comment|// further...
name|output
operator|.
name|writeVLong
argument_list|(
name|Math
operator|.
name|max
argument_list|(
literal|0
argument_list|,
name|value
operator|+
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

