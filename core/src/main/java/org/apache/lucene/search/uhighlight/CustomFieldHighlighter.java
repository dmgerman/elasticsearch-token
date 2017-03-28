begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.apache.lucene.search.uhighlight
package|package
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|search
operator|.
name|uhighlight
package|;
end_package

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
name|Locale
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|search
operator|.
name|uhighlight
operator|.
name|CustomUnifiedHighlighter
operator|.
name|MULTIVAL_SEP_CHAR
import|;
end_import

begin_comment
comment|/**  * Custom {@link FieldHighlighter} that creates a single passage bounded to {@code noMatchSize} when  * no highlights were found.  */
end_comment

begin_class
DECL|class|CustomFieldHighlighter
class|class
name|CustomFieldHighlighter
extends|extends
name|FieldHighlighter
block|{
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
DECL|field|breakIteratorLocale
specifier|private
specifier|final
name|Locale
name|breakIteratorLocale
decl_stmt|;
DECL|field|noMatchSize
specifier|private
specifier|final
name|int
name|noMatchSize
decl_stmt|;
DECL|field|fieldValue
specifier|private
specifier|final
name|String
name|fieldValue
decl_stmt|;
DECL|method|CustomFieldHighlighter
name|CustomFieldHighlighter
parameter_list|(
name|String
name|field
parameter_list|,
name|FieldOffsetStrategy
name|fieldOffsetStrategy
parameter_list|,
name|Locale
name|breakIteratorLocale
parameter_list|,
name|BreakIterator
name|breakIterator
parameter_list|,
name|PassageScorer
name|passageScorer
parameter_list|,
name|int
name|maxPassages
parameter_list|,
name|int
name|maxNoHighlightPassages
parameter_list|,
name|PassageFormatter
name|passageFormatter
parameter_list|,
name|int
name|noMatchSize
parameter_list|,
name|String
name|fieldValue
parameter_list|)
block|{
name|super
argument_list|(
name|field
argument_list|,
name|fieldOffsetStrategy
argument_list|,
name|breakIterator
argument_list|,
name|passageScorer
argument_list|,
name|maxPassages
argument_list|,
name|maxNoHighlightPassages
argument_list|,
name|passageFormatter
argument_list|)
expr_stmt|;
name|this
operator|.
name|breakIteratorLocale
operator|=
name|breakIteratorLocale
expr_stmt|;
name|this
operator|.
name|noMatchSize
operator|=
name|noMatchSize
expr_stmt|;
name|this
operator|.
name|fieldValue
operator|=
name|fieldValue
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getSummaryPassagesNoHighlight
specifier|protected
name|Passage
index|[]
name|getSummaryPassagesNoHighlight
parameter_list|(
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
name|int
name|pos
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|pos
operator|<
name|fieldValue
operator|.
name|length
argument_list|()
operator|&&
name|fieldValue
operator|.
name|charAt
argument_list|(
name|pos
argument_list|)
operator|==
name|MULTIVAL_SEP_CHAR
condition|)
block|{
name|pos
operator|++
expr_stmt|;
block|}
if|if
condition|(
name|pos
operator|<
name|fieldValue
operator|.
name|length
argument_list|()
condition|)
block|{
name|int
name|end
init|=
name|fieldValue
operator|.
name|indexOf
argument_list|(
name|MULTIVAL_SEP_CHAR
argument_list|,
name|pos
argument_list|)
decl_stmt|;
if|if
condition|(
name|end
operator|==
operator|-
literal|1
condition|)
block|{
name|end
operator|=
name|fieldValue
operator|.
name|length
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|noMatchSize
operator|+
name|pos
operator|<
name|end
condition|)
block|{
name|BreakIterator
name|bi
init|=
name|BreakIterator
operator|.
name|getWordInstance
argument_list|(
name|breakIteratorLocale
argument_list|)
decl_stmt|;
name|bi
operator|.
name|setText
argument_list|(
name|fieldValue
argument_list|)
expr_stmt|;
comment|// Finds the next word boundary **after** noMatchSize.
name|end
operator|=
name|bi
operator|.
name|following
argument_list|(
name|noMatchSize
operator|+
name|pos
argument_list|)
expr_stmt|;
if|if
condition|(
name|end
operator|==
name|BreakIterator
operator|.
name|DONE
condition|)
block|{
name|end
operator|=
name|fieldValue
operator|.
name|length
argument_list|()
expr_stmt|;
block|}
block|}
name|Passage
name|passage
init|=
operator|new
name|Passage
argument_list|()
decl_stmt|;
name|passage
operator|.
name|setScore
argument_list|(
name|Float
operator|.
name|NaN
argument_list|)
expr_stmt|;
name|passage
operator|.
name|setStartOffset
argument_list|(
name|pos
argument_list|)
expr_stmt|;
name|passage
operator|.
name|setEndOffset
argument_list|(
name|end
argument_list|)
expr_stmt|;
return|return
operator|new
name|Passage
index|[]
block|{
name|passage
block|}
return|;
block|}
block|}
return|return
name|EMPTY_PASSAGE
return|;
block|}
block|}
end_class

end_unit
