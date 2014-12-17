begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.suggest.completion
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|suggest
operator|.
name|completion
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
name|ObjectLongOpenHashMap
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
name|codecs
operator|.
name|CodecUtil
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
name|codecs
operator|.
name|FieldsConsumer
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
name|suggest
operator|.
name|Lookup
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
name|suggest
operator|.
name|analyzing
operator|.
name|XAnalyzingSuggester
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
name|suggest
operator|.
name|analyzing
operator|.
name|XFuzzySuggester
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
name|store
operator|.
name|IndexInput
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
name|store
operator|.
name|IndexOutput
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
name|Accountable
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
name|Accountables
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
name|IOUtils
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
name|IntsRef
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
name|automaton
operator|.
name|Automaton
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
name|fst
operator|.
name|ByteSequenceOutputs
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
name|fst
operator|.
name|FST
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
name|fst
operator|.
name|PairOutputs
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
name|fst
operator|.
name|PairOutputs
operator|.
name|Pair
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
name|fst
operator|.
name|PositiveIntOutputs
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
name|regex
operator|.
name|Regex
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
name|core
operator|.
name|CompletionFieldMapper
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
name|suggest
operator|.
name|completion
operator|.
name|AnalyzingCompletionLookupProvider
operator|.
name|AnalyzingSuggestHolder
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
name|suggest
operator|.
name|completion
operator|.
name|Completion090PostingsFormat
operator|.
name|CompletionLookupProvider
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
name|suggest
operator|.
name|completion
operator|.
name|Completion090PostingsFormat
operator|.
name|LookupFactory
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
name|suggest
operator|.
name|context
operator|.
name|ContextMapping
operator|.
name|ContextQuery
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
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeMap
import|;
end_import

begin_comment
comment|/**  * This is an older implementation of the AnalyzingCompletionLookupProvider class  * We use this to test for backwards compatibility in our tests, namely  * CompletionPostingsFormatTest  * This ensures upgrades between versions work smoothly  */
end_comment

begin_class
DECL|class|AnalyzingCompletionLookupProviderV1
specifier|public
class|class
name|AnalyzingCompletionLookupProviderV1
extends|extends
name|CompletionLookupProvider
block|{
comment|// for serialization
DECL|field|SERIALIZE_PRESERVE_SEPARATORS
specifier|public
specifier|static
specifier|final
name|int
name|SERIALIZE_PRESERVE_SEPARATORS
init|=
literal|1
decl_stmt|;
DECL|field|SERIALIZE_HAS_PAYLOADS
specifier|public
specifier|static
specifier|final
name|int
name|SERIALIZE_HAS_PAYLOADS
init|=
literal|2
decl_stmt|;
DECL|field|SERIALIZE_PRESERVE_POSITION_INCREMENTS
specifier|public
specifier|static
specifier|final
name|int
name|SERIALIZE_PRESERVE_POSITION_INCREMENTS
init|=
literal|4
decl_stmt|;
DECL|field|MAX_SURFACE_FORMS_PER_ANALYZED_FORM
specifier|private
specifier|static
specifier|final
name|int
name|MAX_SURFACE_FORMS_PER_ANALYZED_FORM
init|=
literal|256
decl_stmt|;
DECL|field|MAX_GRAPH_EXPANSIONS
specifier|private
specifier|static
specifier|final
name|int
name|MAX_GRAPH_EXPANSIONS
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|CODEC_NAME
specifier|public
specifier|static
specifier|final
name|String
name|CODEC_NAME
init|=
literal|"analyzing"
decl_stmt|;
DECL|field|CODEC_VERSION
specifier|public
specifier|static
specifier|final
name|int
name|CODEC_VERSION
init|=
literal|1
decl_stmt|;
DECL|field|preserveSep
specifier|private
name|boolean
name|preserveSep
decl_stmt|;
DECL|field|preservePositionIncrements
specifier|private
name|boolean
name|preservePositionIncrements
decl_stmt|;
DECL|field|maxSurfaceFormsPerAnalyzedForm
specifier|private
name|int
name|maxSurfaceFormsPerAnalyzedForm
decl_stmt|;
DECL|field|maxGraphExpansions
specifier|private
name|int
name|maxGraphExpansions
decl_stmt|;
DECL|field|hasPayloads
specifier|private
name|boolean
name|hasPayloads
decl_stmt|;
DECL|field|prototype
specifier|private
specifier|final
name|XAnalyzingSuggester
name|prototype
decl_stmt|;
comment|// important, these are the settings from the old xanalyzingsuggester
DECL|field|SEP_LABEL
specifier|public
specifier|static
specifier|final
name|int
name|SEP_LABEL
init|=
literal|0xFF
decl_stmt|;
DECL|field|END_BYTE
specifier|public
specifier|static
specifier|final
name|int
name|END_BYTE
init|=
literal|0x0
decl_stmt|;
DECL|field|PAYLOAD_SEP
specifier|public
specifier|static
specifier|final
name|int
name|PAYLOAD_SEP
init|=
literal|'\u001f'
decl_stmt|;
DECL|method|AnalyzingCompletionLookupProviderV1
specifier|public
name|AnalyzingCompletionLookupProviderV1
parameter_list|(
name|boolean
name|preserveSep
parameter_list|,
name|boolean
name|exactFirst
parameter_list|,
name|boolean
name|preservePositionIncrements
parameter_list|,
name|boolean
name|hasPayloads
parameter_list|)
block|{
name|this
operator|.
name|preserveSep
operator|=
name|preserveSep
expr_stmt|;
name|this
operator|.
name|preservePositionIncrements
operator|=
name|preservePositionIncrements
expr_stmt|;
name|this
operator|.
name|hasPayloads
operator|=
name|hasPayloads
expr_stmt|;
name|this
operator|.
name|maxSurfaceFormsPerAnalyzedForm
operator|=
name|MAX_SURFACE_FORMS_PER_ANALYZED_FORM
expr_stmt|;
name|this
operator|.
name|maxGraphExpansions
operator|=
name|MAX_GRAPH_EXPANSIONS
expr_stmt|;
name|int
name|options
init|=
name|preserveSep
condition|?
name|XAnalyzingSuggester
operator|.
name|PRESERVE_SEP
else|:
literal|0
decl_stmt|;
comment|// needs to fixed in the suggester first before it can be supported
comment|//options |= exactFirst ? XAnalyzingSuggester.EXACT_FIRST : 0;
name|prototype
operator|=
operator|new
name|XAnalyzingSuggester
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|options
argument_list|,
name|maxSurfaceFormsPerAnalyzedForm
argument_list|,
name|maxGraphExpansions
argument_list|,
name|preservePositionIncrements
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|,
literal|1
argument_list|,
name|SEP_LABEL
argument_list|,
name|PAYLOAD_SEP
argument_list|,
name|END_BYTE
argument_list|,
name|XAnalyzingSuggester
operator|.
name|HOLE_CHARACTER
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getName
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
literal|"analyzing"
return|;
block|}
annotation|@
name|Override
DECL|method|consumer
specifier|public
name|FieldsConsumer
name|consumer
parameter_list|(
specifier|final
name|IndexOutput
name|output
parameter_list|)
throws|throws
name|IOException
block|{
comment|// TODO write index header?
name|CodecUtil
operator|.
name|writeHeader
argument_list|(
name|output
argument_list|,
name|CODEC_NAME
argument_list|,
name|CODEC_VERSION
argument_list|)
expr_stmt|;
return|return
operator|new
name|FieldsConsumer
argument_list|()
block|{
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|fieldOffsets
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
try|try
block|{
comment|/*                        * write the offsets per field such that we know where                        * we need to load the FSTs from                        */
name|long
name|pointer
init|=
name|output
operator|.
name|getFilePointer
argument_list|()
decl_stmt|;
name|output
operator|.
name|writeVInt
argument_list|(
name|fieldOffsets
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|entry
range|:
name|fieldOffsets
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|output
operator|.
name|writeString
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
name|output
operator|.
name|writeVLong
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|output
operator|.
name|writeLong
argument_list|(
name|pointer
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|IOUtils
operator|.
name|close
argument_list|(
name|output
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|Fields
name|fields
parameter_list|)
throws|throws
name|IOException
block|{
for|for
control|(
name|String
name|field
range|:
name|fields
control|)
block|{
name|Terms
name|terms
init|=
name|fields
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
name|DocsAndPositionsEnum
name|docsEnum
init|=
literal|null
decl_stmt|;
specifier|final
name|SuggestPayload
name|spare
init|=
operator|new
name|SuggestPayload
argument_list|()
decl_stmt|;
name|int
name|maxAnalyzedPathsForOneInput
init|=
literal|0
decl_stmt|;
specifier|final
name|XAnalyzingSuggester
operator|.
name|XBuilder
name|builder
init|=
operator|new
name|XAnalyzingSuggester
operator|.
name|XBuilder
argument_list|(
name|maxSurfaceFormsPerAnalyzedForm
argument_list|,
name|hasPayloads
argument_list|,
name|XAnalyzingSuggester
operator|.
name|PAYLOAD_SEP
argument_list|)
decl_stmt|;
name|int
name|docCount
init|=
literal|0
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
name|BytesRef
name|term
init|=
name|termsEnum
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|term
operator|==
literal|null
condition|)
block|{
break|break;
block|}
name|docsEnum
operator|=
name|termsEnum
operator|.
name|docsAndPositions
argument_list|(
literal|null
argument_list|,
name|docsEnum
argument_list|,
name|DocsAndPositionsEnum
operator|.
name|FLAG_PAYLOADS
argument_list|)
expr_stmt|;
name|builder
operator|.
name|startTerm
argument_list|(
name|term
argument_list|)
expr_stmt|;
name|int
name|docFreq
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|docsEnum
operator|.
name|nextDoc
argument_list|()
operator|!=
name|DocIdSetIterator
operator|.
name|NO_MORE_DOCS
condition|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|docsEnum
operator|.
name|freq
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
specifier|final
name|int
name|position
init|=
name|docsEnum
operator|.
name|nextPosition
argument_list|()
decl_stmt|;
name|AnalyzingCompletionLookupProviderV1
operator|.
name|this
operator|.
name|parsePayload
argument_list|(
name|docsEnum
operator|.
name|getPayload
argument_list|()
argument_list|,
name|spare
argument_list|)
expr_stmt|;
name|builder
operator|.
name|addSurface
argument_list|(
name|spare
operator|.
name|surfaceForm
operator|.
name|get
argument_list|()
argument_list|,
name|spare
operator|.
name|payload
operator|.
name|get
argument_list|()
argument_list|,
name|spare
operator|.
name|weight
argument_list|)
expr_stmt|;
comment|// multi fields have the same surface form so we sum up here
name|maxAnalyzedPathsForOneInput
operator|=
name|Math
operator|.
name|max
argument_list|(
name|maxAnalyzedPathsForOneInput
argument_list|,
name|position
operator|+
literal|1
argument_list|)
expr_stmt|;
block|}
name|docFreq
operator|++
expr_stmt|;
name|docCount
operator|=
name|Math
operator|.
name|max
argument_list|(
name|docCount
argument_list|,
name|docsEnum
operator|.
name|docID
argument_list|()
operator|+
literal|1
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|finishTerm
argument_list|(
name|docFreq
argument_list|)
expr_stmt|;
block|}
comment|/*                      * Here we are done processing the field and we can                      * buid the FST and write it to disk.                      */
name|FST
argument_list|<
name|Pair
argument_list|<
name|Long
argument_list|,
name|BytesRef
argument_list|>
argument_list|>
name|build
init|=
name|builder
operator|.
name|build
argument_list|()
decl_stmt|;
assert|assert
name|build
operator|!=
literal|null
operator|||
name|docCount
operator|==
literal|0
operator|:
literal|"the FST is null but docCount is != 0 actual value: ["
operator|+
name|docCount
operator|+
literal|"]"
assert|;
comment|/*                          * it's possible that the FST is null if we have 2 segments that get merged                          * and all docs that have a value in this field are deleted. This will cause                          * a consumer to be created but it doesn't consume any values causing the FSTBuilder                          * to return null.                          */
if|if
condition|(
name|build
operator|!=
literal|null
condition|)
block|{
name|fieldOffsets
operator|.
name|put
argument_list|(
name|field
argument_list|,
name|output
operator|.
name|getFilePointer
argument_list|()
argument_list|)
expr_stmt|;
name|build
operator|.
name|save
argument_list|(
name|output
argument_list|)
expr_stmt|;
comment|/* write some more meta-info */
name|output
operator|.
name|writeVInt
argument_list|(
name|maxAnalyzedPathsForOneInput
argument_list|)
expr_stmt|;
name|output
operator|.
name|writeVInt
argument_list|(
name|maxSurfaceFormsPerAnalyzedForm
argument_list|)
expr_stmt|;
name|output
operator|.
name|writeInt
argument_list|(
name|maxGraphExpansions
argument_list|)
expr_stmt|;
comment|// can be negative
name|int
name|options
init|=
literal|0
decl_stmt|;
name|options
operator||=
name|preserveSep
condition|?
name|SERIALIZE_PRESERVE_SEPARATORS
else|:
literal|0
expr_stmt|;
name|options
operator||=
name|hasPayloads
condition|?
name|SERIALIZE_HAS_PAYLOADS
else|:
literal|0
expr_stmt|;
name|options
operator||=
name|preservePositionIncrements
condition|?
name|SERIALIZE_PRESERVE_POSITION_INCREMENTS
else|:
literal|0
expr_stmt|;
name|output
operator|.
name|writeVInt
argument_list|(
name|options
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
return|;
block|}
annotation|@
name|Override
DECL|method|load
specifier|public
name|LookupFactory
name|load
parameter_list|(
name|IndexInput
name|input
parameter_list|)
throws|throws
name|IOException
block|{
name|CodecUtil
operator|.
name|checkHeader
argument_list|(
name|input
argument_list|,
name|CODEC_NAME
argument_list|,
name|CODEC_VERSION
argument_list|,
name|CODEC_VERSION
argument_list|)
expr_stmt|;
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|AnalyzingSuggestHolder
argument_list|>
name|lookupMap
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|input
operator|.
name|seek
argument_list|(
name|input
operator|.
name|length
argument_list|()
operator|-
literal|8
argument_list|)
expr_stmt|;
name|long
name|metaPointer
init|=
name|input
operator|.
name|readLong
argument_list|()
decl_stmt|;
name|input
operator|.
name|seek
argument_list|(
name|metaPointer
argument_list|)
expr_stmt|;
name|int
name|numFields
init|=
name|input
operator|.
name|readVInt
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|Long
argument_list|,
name|String
argument_list|>
name|meta
init|=
operator|new
name|TreeMap
argument_list|<>
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
name|numFields
condition|;
name|i
operator|++
control|)
block|{
name|String
name|name
init|=
name|input
operator|.
name|readString
argument_list|()
decl_stmt|;
name|long
name|offset
init|=
name|input
operator|.
name|readVLong
argument_list|()
decl_stmt|;
name|meta
operator|.
name|put
argument_list|(
name|offset
argument_list|,
name|name
argument_list|)
expr_stmt|;
block|}
name|long
name|sizeInBytes
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|Long
argument_list|,
name|String
argument_list|>
name|entry
range|:
name|meta
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|input
operator|.
name|seek
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
name|FST
argument_list|<
name|Pair
argument_list|<
name|Long
argument_list|,
name|BytesRef
argument_list|>
argument_list|>
name|fst
init|=
operator|new
name|FST
argument_list|<>
argument_list|(
name|input
argument_list|,
operator|new
name|PairOutputs
argument_list|<>
argument_list|(
name|PositiveIntOutputs
operator|.
name|getSingleton
argument_list|()
argument_list|,
name|ByteSequenceOutputs
operator|.
name|getSingleton
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|int
name|maxAnalyzedPathsForOneInput
init|=
name|input
operator|.
name|readVInt
argument_list|()
decl_stmt|;
name|int
name|maxSurfaceFormsPerAnalyzedForm
init|=
name|input
operator|.
name|readVInt
argument_list|()
decl_stmt|;
name|int
name|maxGraphExpansions
init|=
name|input
operator|.
name|readInt
argument_list|()
decl_stmt|;
name|int
name|options
init|=
name|input
operator|.
name|readVInt
argument_list|()
decl_stmt|;
name|boolean
name|preserveSep
init|=
operator|(
name|options
operator|&
name|SERIALIZE_PRESERVE_SEPARATORS
operator|)
operator|!=
literal|0
decl_stmt|;
name|boolean
name|hasPayloads
init|=
operator|(
name|options
operator|&
name|SERIALIZE_HAS_PAYLOADS
operator|)
operator|!=
literal|0
decl_stmt|;
name|boolean
name|preservePositionIncrements
init|=
operator|(
name|options
operator|&
name|SERIALIZE_PRESERVE_POSITION_INCREMENTS
operator|)
operator|!=
literal|0
decl_stmt|;
name|sizeInBytes
operator|+=
name|fst
operator|.
name|ramBytesUsed
argument_list|()
expr_stmt|;
name|lookupMap
operator|.
name|put
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|,
operator|new
name|AnalyzingSuggestHolder
argument_list|(
name|preserveSep
argument_list|,
name|preservePositionIncrements
argument_list|,
name|maxSurfaceFormsPerAnalyzedForm
argument_list|,
name|maxGraphExpansions
argument_list|,
name|hasPayloads
argument_list|,
name|maxAnalyzedPathsForOneInput
argument_list|,
name|fst
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|final
name|long
name|ramBytesUsed
init|=
name|sizeInBytes
decl_stmt|;
return|return
operator|new
name|LookupFactory
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Lookup
name|getLookup
parameter_list|(
name|CompletionFieldMapper
name|mapper
parameter_list|,
name|CompletionSuggestionContext
name|suggestionContext
parameter_list|)
block|{
name|AnalyzingSuggestHolder
name|analyzingSuggestHolder
init|=
name|lookupMap
operator|.
name|get
argument_list|(
name|mapper
operator|.
name|names
argument_list|()
operator|.
name|indexName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|analyzingSuggestHolder
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|int
name|flags
init|=
name|analyzingSuggestHolder
operator|.
name|getPreserveSeparator
argument_list|()
condition|?
name|XAnalyzingSuggester
operator|.
name|PRESERVE_SEP
else|:
literal|0
decl_stmt|;
specifier|final
name|Automaton
name|queryPrefix
init|=
name|mapper
operator|.
name|requiresContext
argument_list|()
condition|?
name|ContextQuery
operator|.
name|toAutomaton
argument_list|(
name|analyzingSuggestHolder
operator|.
name|getPreserveSeparator
argument_list|()
argument_list|,
name|suggestionContext
operator|.
name|getContextQueries
argument_list|()
argument_list|)
else|:
literal|null
decl_stmt|;
name|XAnalyzingSuggester
name|suggester
decl_stmt|;
if|if
condition|(
name|suggestionContext
operator|.
name|isFuzzy
argument_list|()
condition|)
block|{
name|suggester
operator|=
operator|new
name|XFuzzySuggester
argument_list|(
name|mapper
operator|.
name|indexAnalyzer
argument_list|()
argument_list|,
name|queryPrefix
argument_list|,
name|mapper
operator|.
name|searchAnalyzer
argument_list|()
argument_list|,
name|flags
argument_list|,
name|analyzingSuggestHolder
operator|.
name|maxSurfaceFormsPerAnalyzedForm
argument_list|,
name|analyzingSuggestHolder
operator|.
name|maxGraphExpansions
argument_list|,
name|suggestionContext
operator|.
name|getFuzzyEditDistance
argument_list|()
argument_list|,
name|suggestionContext
operator|.
name|isFuzzyTranspositions
argument_list|()
argument_list|,
name|suggestionContext
operator|.
name|getFuzzyPrefixLength
argument_list|()
argument_list|,
name|suggestionContext
operator|.
name|getFuzzyMinLength
argument_list|()
argument_list|,
literal|false
argument_list|,
name|analyzingSuggestHolder
operator|.
name|fst
argument_list|,
name|analyzingSuggestHolder
operator|.
name|hasPayloads
argument_list|,
name|analyzingSuggestHolder
operator|.
name|maxAnalyzedPathsForOneInput
argument_list|,
name|SEP_LABEL
argument_list|,
name|PAYLOAD_SEP
argument_list|,
name|END_BYTE
argument_list|,
name|XAnalyzingSuggester
operator|.
name|HOLE_CHARACTER
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|suggester
operator|=
operator|new
name|XAnalyzingSuggester
argument_list|(
name|mapper
operator|.
name|indexAnalyzer
argument_list|()
argument_list|,
name|queryPrefix
argument_list|,
name|mapper
operator|.
name|searchAnalyzer
argument_list|()
argument_list|,
name|flags
argument_list|,
name|analyzingSuggestHolder
operator|.
name|maxSurfaceFormsPerAnalyzedForm
argument_list|,
name|analyzingSuggestHolder
operator|.
name|maxGraphExpansions
argument_list|,
name|analyzingSuggestHolder
operator|.
name|preservePositionIncrements
argument_list|,
name|analyzingSuggestHolder
operator|.
name|fst
argument_list|,
name|analyzingSuggestHolder
operator|.
name|hasPayloads
argument_list|,
name|analyzingSuggestHolder
operator|.
name|maxAnalyzedPathsForOneInput
argument_list|,
name|SEP_LABEL
argument_list|,
name|PAYLOAD_SEP
argument_list|,
name|END_BYTE
argument_list|,
name|XAnalyzingSuggester
operator|.
name|HOLE_CHARACTER
argument_list|)
expr_stmt|;
block|}
return|return
name|suggester
return|;
block|}
annotation|@
name|Override
specifier|public
name|CompletionStats
name|stats
parameter_list|(
name|String
modifier|...
name|fields
parameter_list|)
block|{
name|long
name|sizeInBytes
init|=
literal|0
decl_stmt|;
name|ObjectLongOpenHashMap
argument_list|<
name|String
argument_list|>
name|completionFields
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|fields
operator|!=
literal|null
operator|&&
name|fields
operator|.
name|length
operator|>
literal|0
condition|)
block|{
name|completionFields
operator|=
operator|new
name|ObjectLongOpenHashMap
argument_list|<>
argument_list|(
name|fields
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|AnalyzingSuggestHolder
argument_list|>
name|entry
range|:
name|lookupMap
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|sizeInBytes
operator|+=
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|fst
operator|.
name|ramBytesUsed
argument_list|()
expr_stmt|;
if|if
condition|(
name|fields
operator|==
literal|null
operator|||
name|fields
operator|.
name|length
operator|==
literal|0
condition|)
block|{
continue|continue;
block|}
for|for
control|(
name|String
name|field
range|:
name|fields
control|)
block|{
comment|// support for getting fields by regex as in fielddata
if|if
condition|(
name|Regex
operator|.
name|simpleMatch
argument_list|(
name|field
argument_list|,
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
condition|)
block|{
name|long
name|fstSize
init|=
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|fst
operator|.
name|ramBytesUsed
argument_list|()
decl_stmt|;
name|completionFields
operator|.
name|addTo
argument_list|(
name|field
argument_list|,
name|fstSize
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
operator|new
name|CompletionStats
argument_list|(
name|sizeInBytes
argument_list|,
name|completionFields
argument_list|)
return|;
block|}
annotation|@
name|Override
name|AnalyzingSuggestHolder
name|getAnalyzingSuggestHolder
parameter_list|(
name|CompletionFieldMapper
name|mapper
parameter_list|)
block|{
return|return
name|lookupMap
operator|.
name|get
argument_list|(
name|mapper
operator|.
name|names
argument_list|()
operator|.
name|indexName
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|ramBytesUsed
parameter_list|()
block|{
return|return
name|ramBytesUsed
return|;
block|}
annotation|@
name|Override
specifier|public
name|Iterable
argument_list|<
name|Accountable
argument_list|>
name|getChildResources
parameter_list|()
block|{
return|return
name|Accountables
operator|.
name|namedAccountables
argument_list|(
literal|"field"
argument_list|,
name|lookupMap
argument_list|)
return|;
block|}
block|}
return|;
block|}
comment|/*     // might be readded when we change the current impl, right now not needed     static class AnalyzingSuggestHolder {         final boolean preserveSep;         final boolean preservePositionIncrements;         final int maxSurfaceFormsPerAnalyzedForm;         final int maxGraphExpansions;         final boolean hasPayloads;         final int maxAnalyzedPathsForOneInput;         final FST<Pair<Long, BytesRef>> fst;          public AnalyzingSuggestHolder(boolean preserveSep, boolean preservePositionIncrements, int maxSurfaceFormsPerAnalyzedForm, int maxGraphExpansions,                                       boolean hasPayloads, int maxAnalyzedPathsForOneInput, FST<Pair<Long, BytesRef>> fst) {             this.preserveSep = preserveSep;             this.preservePositionIncrements = preservePositionIncrements;             this.maxSurfaceFormsPerAnalyzedForm = maxSurfaceFormsPerAnalyzedForm;             this.maxGraphExpansions = maxGraphExpansions;             this.hasPayloads = hasPayloads;             this.maxAnalyzedPathsForOneInput = maxAnalyzedPathsForOneInput;             this.fst = fst;         }      }     */
annotation|@
name|Override
DECL|method|toFiniteStrings
specifier|public
name|Set
argument_list|<
name|IntsRef
argument_list|>
name|toFiniteStrings
parameter_list|(
name|TokenStream
name|stream
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|prototype
operator|.
name|toFiniteStrings
argument_list|(
name|prototype
operator|.
name|getTokenStreamToAutomaton
argument_list|()
argument_list|,
name|stream
argument_list|)
return|;
block|}
block|}
end_class

end_unit

