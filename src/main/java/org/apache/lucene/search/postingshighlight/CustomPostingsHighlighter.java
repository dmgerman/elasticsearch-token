begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements. See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the  * License for the specific language governing permissions and limitations under  * the License.  */
end_comment

begin_package
DECL|package|org.apache.lucene.search.postingshighlight
package|package
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|search
operator|.
name|postingshighlight
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
name|IndexReaderContext
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
name|IndexSearcher
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
name|Strings
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
name|highlight
operator|.
name|HighlightUtils
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
name|text
operator|.
name|BreakIterator
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
name|Map
import|;
end_import

begin_comment
comment|/**  * Subclass of the {@link XPostingsHighlighter} that works for a single field in a single document.  * It receives the field values as input and it performs discrete highlighting on each single value  * calling the highlightDoc method multiple times.  * It allows to pass in the query terms to avoid calling extract terms multiple times.  *  * The use that we make of the postings highlighter is not optimal. It would be much better to  * highlight multiple docs in a single call, as we actually lose its sequential IO.  But that would require:  * 1) to make our fork more complex and harder to maintain to perform discrete highlighting (needed to return  * a different snippet per value when number_of_fragments=0 and the field has multiple values)  * 2) refactoring of the elasticsearch highlight api which currently works per hit  *  */
end_comment

begin_class
DECL|class|CustomPostingsHighlighter
specifier|public
specifier|final
class|class
name|CustomPostingsHighlighter
extends|extends
name|XPostingsHighlighter
block|{
DECL|field|EMPTY_SNIPPET
specifier|private
specifier|static
specifier|final
name|Snippet
index|[]
name|EMPTY_SNIPPET
init|=
operator|new
name|Snippet
index|[
literal|0
index|]
decl_stmt|;
DECL|field|EMPTY_PASSAGE
specifier|private
specifier|static
specifier|final
name|Passage
index|[]
name|EMPTY_PASSAGE
init|=
operator|new
name|Passage
index|[
literal|0
index|]
decl_stmt|;
DECL|field|passageFormatter
specifier|private
specifier|final
name|CustomPassageFormatter
name|passageFormatter
decl_stmt|;
DECL|field|noMatchSize
specifier|private
specifier|final
name|int
name|noMatchSize
decl_stmt|;
DECL|field|totalContentLength
specifier|private
specifier|final
name|int
name|totalContentLength
decl_stmt|;
DECL|field|fieldValues
specifier|private
specifier|final
name|String
index|[]
name|fieldValues
decl_stmt|;
DECL|field|fieldValuesOffsets
specifier|private
specifier|final
name|int
index|[]
name|fieldValuesOffsets
decl_stmt|;
DECL|field|currentValueIndex
specifier|private
name|int
name|currentValueIndex
init|=
literal|0
decl_stmt|;
DECL|field|breakIterator
specifier|private
name|BreakIterator
name|breakIterator
decl_stmt|;
DECL|method|CustomPostingsHighlighter
specifier|public
name|CustomPostingsHighlighter
parameter_list|(
name|CustomPassageFormatter
name|passageFormatter
parameter_list|,
name|List
argument_list|<
name|Object
argument_list|>
name|fieldValues
parameter_list|,
name|boolean
name|mergeValues
parameter_list|,
name|int
name|maxLength
parameter_list|,
name|int
name|noMatchSize
parameter_list|)
block|{
name|super
argument_list|(
name|maxLength
argument_list|)
expr_stmt|;
name|this
operator|.
name|passageFormatter
operator|=
name|passageFormatter
expr_stmt|;
name|this
operator|.
name|noMatchSize
operator|=
name|noMatchSize
expr_stmt|;
if|if
condition|(
name|mergeValues
condition|)
block|{
name|String
name|rawValue
init|=
name|Strings
operator|.
name|collectionToDelimitedString
argument_list|(
name|fieldValues
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|getMultiValuedSeparator
argument_list|(
literal|""
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|fieldValue
init|=
name|rawValue
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|Math
operator|.
name|min
argument_list|(
name|rawValue
operator|.
name|length
argument_list|()
argument_list|,
name|maxLength
argument_list|)
argument_list|)
decl_stmt|;
name|this
operator|.
name|fieldValues
operator|=
operator|new
name|String
index|[]
block|{
name|fieldValue
block|}
expr_stmt|;
name|this
operator|.
name|fieldValuesOffsets
operator|=
operator|new
name|int
index|[]
block|{
literal|0
block|}
expr_stmt|;
name|this
operator|.
name|totalContentLength
operator|=
name|fieldValue
operator|.
name|length
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|fieldValues
operator|=
operator|new
name|String
index|[
name|fieldValues
operator|.
name|size
argument_list|()
index|]
expr_stmt|;
name|this
operator|.
name|fieldValuesOffsets
operator|=
operator|new
name|int
index|[
name|fieldValues
operator|.
name|size
argument_list|()
index|]
expr_stmt|;
name|int
name|contentLength
init|=
literal|0
decl_stmt|;
name|int
name|offset
init|=
literal|0
decl_stmt|;
name|int
name|previousLength
init|=
operator|-
literal|1
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
name|fieldValues
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|String
name|rawValue
init|=
name|fieldValues
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|toString
argument_list|()
decl_stmt|;
name|String
name|fieldValue
init|=
name|rawValue
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|Math
operator|.
name|min
argument_list|(
name|rawValue
operator|.
name|length
argument_list|()
argument_list|,
name|maxLength
argument_list|)
argument_list|)
decl_stmt|;
name|this
operator|.
name|fieldValues
index|[
name|i
index|]
operator|=
name|fieldValue
expr_stmt|;
name|contentLength
operator|+=
name|fieldValue
operator|.
name|length
argument_list|()
expr_stmt|;
name|offset
operator|+=
name|previousLength
operator|+
literal|1
expr_stmt|;
name|this
operator|.
name|fieldValuesOffsets
index|[
name|i
index|]
operator|=
name|offset
expr_stmt|;
name|previousLength
operator|=
name|fieldValue
operator|.
name|length
argument_list|()
expr_stmt|;
block|}
name|this
operator|.
name|totalContentLength
operator|=
name|contentLength
expr_stmt|;
block|}
block|}
comment|/*     Our own api to highlight a single document field, passing in the query terms, and get back our own Snippet object      */
DECL|method|highlightDoc
specifier|public
name|Snippet
index|[]
name|highlightDoc
parameter_list|(
name|String
name|field
parameter_list|,
name|BytesRef
index|[]
name|terms
parameter_list|,
name|IndexSearcher
name|searcher
parameter_list|,
name|int
name|docId
parameter_list|,
name|int
name|maxPassages
parameter_list|)
throws|throws
name|IOException
block|{
name|IndexReader
name|reader
init|=
name|searcher
operator|.
name|getIndexReader
argument_list|()
decl_stmt|;
name|IndexReaderContext
name|readerContext
init|=
name|reader
operator|.
name|getContext
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|AtomicReaderContext
argument_list|>
name|leaves
init|=
name|readerContext
operator|.
name|leaves
argument_list|()
decl_stmt|;
name|String
index|[]
name|contents
init|=
operator|new
name|String
index|[]
block|{
name|loadCurrentFieldValue
argument_list|()
block|}
decl_stmt|;
name|Map
argument_list|<
name|Integer
argument_list|,
name|Object
argument_list|>
name|snippetsMap
init|=
name|highlightField
argument_list|(
name|field
argument_list|,
name|contents
argument_list|,
name|getBreakIterator
argument_list|(
name|field
argument_list|)
argument_list|,
name|terms
argument_list|,
operator|new
name|int
index|[]
block|{
name|docId
block|}
argument_list|,
name|leaves
argument_list|,
name|maxPassages
argument_list|)
decl_stmt|;
comment|//increment the current value index so that next time we'll highlight the next value if available
name|currentValueIndex
operator|++
expr_stmt|;
name|Object
name|snippetObject
init|=
name|snippetsMap
operator|.
name|get
argument_list|(
name|docId
argument_list|)
decl_stmt|;
if|if
condition|(
name|snippetObject
operator|!=
literal|null
operator|&&
name|snippetObject
operator|instanceof
name|Snippet
index|[]
condition|)
block|{
return|return
operator|(
name|Snippet
index|[]
operator|)
name|snippetObject
return|;
block|}
return|return
name|EMPTY_SNIPPET
return|;
block|}
comment|/*     Method provided through our own fork: allows to do proper scoring when doing per value discrete highlighting.     Used to provide the total length of the field (all values) for proper scoring.      */
annotation|@
name|Override
DECL|method|getContentLength
specifier|protected
name|int
name|getContentLength
parameter_list|(
name|String
name|field
parameter_list|,
name|int
name|docId
parameter_list|)
block|{
return|return
name|totalContentLength
return|;
block|}
comment|/*     Method provided through our own fork: allows to perform proper per value discrete highlighting.     Used to provide the offset for the current value.      */
annotation|@
name|Override
DECL|method|getOffsetForCurrentValue
specifier|protected
name|int
name|getOffsetForCurrentValue
parameter_list|(
name|String
name|field
parameter_list|,
name|int
name|docId
parameter_list|)
block|{
if|if
condition|(
name|currentValueIndex
operator|<
name|fieldValuesOffsets
operator|.
name|length
condition|)
block|{
return|return
name|fieldValuesOffsets
index|[
name|currentValueIndex
index|]
return|;
block|}
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"No more values offsets to return"
argument_list|)
throw|;
block|}
DECL|method|setBreakIterator
specifier|public
name|void
name|setBreakIterator
parameter_list|(
name|BreakIterator
name|breakIterator
parameter_list|)
block|{
name|this
operator|.
name|breakIterator
operator|=
name|breakIterator
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getFormatter
specifier|protected
name|XPassageFormatter
name|getFormatter
parameter_list|(
name|String
name|field
parameter_list|)
block|{
return|return
name|passageFormatter
return|;
block|}
annotation|@
name|Override
DECL|method|getBreakIterator
specifier|protected
name|BreakIterator
name|getBreakIterator
parameter_list|(
name|String
name|field
parameter_list|)
block|{
if|if
condition|(
name|breakIterator
operator|==
literal|null
condition|)
block|{
return|return
name|super
operator|.
name|getBreakIterator
argument_list|(
name|field
argument_list|)
return|;
block|}
return|return
name|breakIterator
return|;
block|}
annotation|@
name|Override
DECL|method|getMultiValuedSeparator
specifier|protected
name|char
name|getMultiValuedSeparator
parameter_list|(
name|String
name|field
parameter_list|)
block|{
comment|//U+2029 PARAGRAPH SEPARATOR (PS): each value holds a discrete passage for highlighting
return|return
name|HighlightUtils
operator|.
name|PARAGRAPH_SEPARATOR
return|;
block|}
comment|/*     By default the postings highlighter returns non highlighted snippet when there are no matches.     We want to return no snippets by default, unless no_match_size is greater than 0      */
annotation|@
name|Override
DECL|method|getEmptyHighlight
specifier|protected
name|Passage
index|[]
name|getEmptyHighlight
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|BreakIterator
name|bi
parameter_list|,
name|int
name|maxPassages
parameter_list|)
block|{
if|if
condition|(
name|noMatchSize
operator|>
literal|0
condition|)
block|{
comment|//we want to return the first sentence of the first snippet only
return|return
name|super
operator|.
name|getEmptyHighlight
argument_list|(
name|fieldName
argument_list|,
name|bi
argument_list|,
literal|1
argument_list|)
return|;
block|}
return|return
name|EMPTY_PASSAGE
return|;
block|}
comment|/*     Not needed since we call our own loadCurrentFieldValue explicitly, but we override it anyway for consistency.      */
annotation|@
name|Override
DECL|method|loadFieldValues
specifier|protected
name|String
index|[]
index|[]
name|loadFieldValues
parameter_list|(
name|IndexSearcher
name|searcher
parameter_list|,
name|String
index|[]
name|fields
parameter_list|,
name|int
index|[]
name|docids
parameter_list|,
name|int
name|maxLength
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|String
index|[]
index|[]
block|{
operator|new
name|String
index|[]
block|{
name|loadCurrentFieldValue
argument_list|()
block|}
block|}
return|;
block|}
comment|/*      Our own method that returns the field values, which relies on the content that was provided when creating the highlighter.      Supports per value discrete highlighting calling the highlightDoc method multiple times, one per value.     */
DECL|method|loadCurrentFieldValue
specifier|protected
name|String
name|loadCurrentFieldValue
parameter_list|()
block|{
if|if
condition|(
name|currentValueIndex
operator|<
name|fieldValues
operator|.
name|length
condition|)
block|{
return|return
name|fieldValues
index|[
name|currentValueIndex
index|]
return|;
block|}
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"No more values to return"
argument_list|)
throw|;
block|}
block|}
end_class

end_unit

