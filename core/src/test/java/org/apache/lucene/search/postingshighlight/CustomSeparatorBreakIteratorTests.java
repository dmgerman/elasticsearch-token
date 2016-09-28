begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|elasticsearch
operator|.
name|search
operator|.
name|fetch
operator|.
name|subphase
operator|.
name|highlight
operator|.
name|HighlightUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|ESTestCase
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
name|text
operator|.
name|CharacterIterator
import|;
end_import

begin_import
import|import
name|java
operator|.
name|text
operator|.
name|StringCharacterIterator
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
name|hamcrest
operator|.
name|CoreMatchers
operator|.
name|equalTo
import|;
end_import

begin_class
DECL|class|CustomSeparatorBreakIteratorTests
specifier|public
class|class
name|CustomSeparatorBreakIteratorTests
extends|extends
name|ESTestCase
block|{
DECL|method|testBreakOnCustomSeparator
specifier|public
name|void
name|testBreakOnCustomSeparator
parameter_list|()
throws|throws
name|Exception
block|{
name|Character
name|separator
init|=
name|randomSeparator
argument_list|()
decl_stmt|;
name|BreakIterator
name|bi
init|=
operator|new
name|CustomSeparatorBreakIterator
argument_list|(
name|separator
argument_list|)
decl_stmt|;
name|String
name|source
init|=
literal|"this"
operator|+
name|separator
operator|+
literal|"is"
operator|+
name|separator
operator|+
literal|"the"
operator|+
name|separator
operator|+
literal|"first"
operator|+
name|separator
operator|+
literal|"sentence"
decl_stmt|;
name|bi
operator|.
name|setText
argument_list|(
name|source
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|bi
operator|.
name|current
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|bi
operator|.
name|first
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|source
operator|.
name|substring
argument_list|(
name|bi
operator|.
name|current
argument_list|()
argument_list|,
name|bi
operator|.
name|next
argument_list|()
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"this"
operator|+
name|separator
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|source
operator|.
name|substring
argument_list|(
name|bi
operator|.
name|current
argument_list|()
argument_list|,
name|bi
operator|.
name|next
argument_list|()
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"is"
operator|+
name|separator
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|source
operator|.
name|substring
argument_list|(
name|bi
operator|.
name|current
argument_list|()
argument_list|,
name|bi
operator|.
name|next
argument_list|()
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"the"
operator|+
name|separator
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|source
operator|.
name|substring
argument_list|(
name|bi
operator|.
name|current
argument_list|()
argument_list|,
name|bi
operator|.
name|next
argument_list|()
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"first"
operator|+
name|separator
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|source
operator|.
name|substring
argument_list|(
name|bi
operator|.
name|current
argument_list|()
argument_list|,
name|bi
operator|.
name|next
argument_list|()
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"sentence"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|bi
operator|.
name|next
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|BreakIterator
operator|.
name|DONE
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|bi
operator|.
name|last
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|source
operator|.
name|length
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|int
name|current
init|=
name|bi
operator|.
name|current
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|source
operator|.
name|substring
argument_list|(
name|bi
operator|.
name|previous
argument_list|()
argument_list|,
name|current
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"sentence"
argument_list|)
argument_list|)
expr_stmt|;
name|current
operator|=
name|bi
operator|.
name|current
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|source
operator|.
name|substring
argument_list|(
name|bi
operator|.
name|previous
argument_list|()
argument_list|,
name|current
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"first"
operator|+
name|separator
argument_list|)
argument_list|)
expr_stmt|;
name|current
operator|=
name|bi
operator|.
name|current
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|source
operator|.
name|substring
argument_list|(
name|bi
operator|.
name|previous
argument_list|()
argument_list|,
name|current
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"the"
operator|+
name|separator
argument_list|)
argument_list|)
expr_stmt|;
name|current
operator|=
name|bi
operator|.
name|current
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|source
operator|.
name|substring
argument_list|(
name|bi
operator|.
name|previous
argument_list|()
argument_list|,
name|current
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"is"
operator|+
name|separator
argument_list|)
argument_list|)
expr_stmt|;
name|current
operator|=
name|bi
operator|.
name|current
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|source
operator|.
name|substring
argument_list|(
name|bi
operator|.
name|previous
argument_list|()
argument_list|,
name|current
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"this"
operator|+
name|separator
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|bi
operator|.
name|previous
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|BreakIterator
operator|.
name|DONE
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|bi
operator|.
name|current
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|source
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|bi
operator|.
name|following
argument_list|(
literal|9
argument_list|)
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"this"
operator|+
name|separator
operator|+
literal|"is"
operator|+
name|separator
operator|+
literal|"the"
operator|+
name|separator
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|source
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|bi
operator|.
name|preceding
argument_list|(
literal|9
argument_list|)
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"this"
operator|+
name|separator
operator|+
literal|"is"
operator|+
name|separator
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|bi
operator|.
name|first
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|source
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|bi
operator|.
name|next
argument_list|(
literal|3
argument_list|)
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"this"
operator|+
name|separator
operator|+
literal|"is"
operator|+
name|separator
operator|+
literal|"the"
operator|+
name|separator
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testSingleSentences
specifier|public
name|void
name|testSingleSentences
parameter_list|()
throws|throws
name|Exception
block|{
name|BreakIterator
name|expected
init|=
name|BreakIterator
operator|.
name|getSentenceInstance
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
decl_stmt|;
name|BreakIterator
name|actual
init|=
operator|new
name|CustomSeparatorBreakIterator
argument_list|(
name|randomSeparator
argument_list|()
argument_list|)
decl_stmt|;
name|assertSameBreaks
argument_list|(
literal|"a"
argument_list|,
name|expected
argument_list|,
name|actual
argument_list|)
expr_stmt|;
name|assertSameBreaks
argument_list|(
literal|"ab"
argument_list|,
name|expected
argument_list|,
name|actual
argument_list|)
expr_stmt|;
name|assertSameBreaks
argument_list|(
literal|"abc"
argument_list|,
name|expected
argument_list|,
name|actual
argument_list|)
expr_stmt|;
name|assertSameBreaks
argument_list|(
literal|""
argument_list|,
name|expected
argument_list|,
name|actual
argument_list|)
expr_stmt|;
block|}
DECL|method|testSliceEnd
specifier|public
name|void
name|testSliceEnd
parameter_list|()
throws|throws
name|Exception
block|{
name|BreakIterator
name|expected
init|=
name|BreakIterator
operator|.
name|getSentenceInstance
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
decl_stmt|;
name|BreakIterator
name|actual
init|=
operator|new
name|CustomSeparatorBreakIterator
argument_list|(
name|randomSeparator
argument_list|()
argument_list|)
decl_stmt|;
name|assertSameBreaks
argument_list|(
literal|"a000"
argument_list|,
literal|0
argument_list|,
literal|1
argument_list|,
name|expected
argument_list|,
name|actual
argument_list|)
expr_stmt|;
name|assertSameBreaks
argument_list|(
literal|"ab000"
argument_list|,
literal|0
argument_list|,
literal|1
argument_list|,
name|expected
argument_list|,
name|actual
argument_list|)
expr_stmt|;
name|assertSameBreaks
argument_list|(
literal|"abc000"
argument_list|,
literal|0
argument_list|,
literal|1
argument_list|,
name|expected
argument_list|,
name|actual
argument_list|)
expr_stmt|;
name|assertSameBreaks
argument_list|(
literal|"000"
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
name|expected
argument_list|,
name|actual
argument_list|)
expr_stmt|;
block|}
DECL|method|testSliceStart
specifier|public
name|void
name|testSliceStart
parameter_list|()
throws|throws
name|Exception
block|{
name|BreakIterator
name|expected
init|=
name|BreakIterator
operator|.
name|getSentenceInstance
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
decl_stmt|;
name|BreakIterator
name|actual
init|=
operator|new
name|CustomSeparatorBreakIterator
argument_list|(
name|randomSeparator
argument_list|()
argument_list|)
decl_stmt|;
name|assertSameBreaks
argument_list|(
literal|"000a"
argument_list|,
literal|3
argument_list|,
literal|1
argument_list|,
name|expected
argument_list|,
name|actual
argument_list|)
expr_stmt|;
name|assertSameBreaks
argument_list|(
literal|"000ab"
argument_list|,
literal|3
argument_list|,
literal|2
argument_list|,
name|expected
argument_list|,
name|actual
argument_list|)
expr_stmt|;
name|assertSameBreaks
argument_list|(
literal|"000abc"
argument_list|,
literal|3
argument_list|,
literal|3
argument_list|,
name|expected
argument_list|,
name|actual
argument_list|)
expr_stmt|;
name|assertSameBreaks
argument_list|(
literal|"000"
argument_list|,
literal|3
argument_list|,
literal|0
argument_list|,
name|expected
argument_list|,
name|actual
argument_list|)
expr_stmt|;
block|}
DECL|method|testSliceMiddle
specifier|public
name|void
name|testSliceMiddle
parameter_list|()
throws|throws
name|Exception
block|{
name|BreakIterator
name|expected
init|=
name|BreakIterator
operator|.
name|getSentenceInstance
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
decl_stmt|;
name|BreakIterator
name|actual
init|=
operator|new
name|CustomSeparatorBreakIterator
argument_list|(
name|randomSeparator
argument_list|()
argument_list|)
decl_stmt|;
name|assertSameBreaks
argument_list|(
literal|"000a000"
argument_list|,
literal|3
argument_list|,
literal|1
argument_list|,
name|expected
argument_list|,
name|actual
argument_list|)
expr_stmt|;
name|assertSameBreaks
argument_list|(
literal|"000ab000"
argument_list|,
literal|3
argument_list|,
literal|2
argument_list|,
name|expected
argument_list|,
name|actual
argument_list|)
expr_stmt|;
name|assertSameBreaks
argument_list|(
literal|"000abc000"
argument_list|,
literal|3
argument_list|,
literal|3
argument_list|,
name|expected
argument_list|,
name|actual
argument_list|)
expr_stmt|;
name|assertSameBreaks
argument_list|(
literal|"000000"
argument_list|,
literal|3
argument_list|,
literal|0
argument_list|,
name|expected
argument_list|,
name|actual
argument_list|)
expr_stmt|;
block|}
comment|/** the current position must be ignored, initial position is always first() */
DECL|method|testFirstPosition
specifier|public
name|void
name|testFirstPosition
parameter_list|()
throws|throws
name|Exception
block|{
name|BreakIterator
name|expected
init|=
name|BreakIterator
operator|.
name|getSentenceInstance
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
decl_stmt|;
name|BreakIterator
name|actual
init|=
operator|new
name|CustomSeparatorBreakIterator
argument_list|(
name|randomSeparator
argument_list|()
argument_list|)
decl_stmt|;
name|assertSameBreaks
argument_list|(
literal|"000ab000"
argument_list|,
literal|3
argument_list|,
literal|2
argument_list|,
literal|4
argument_list|,
name|expected
argument_list|,
name|actual
argument_list|)
expr_stmt|;
block|}
DECL|method|randomSeparator
specifier|private
specifier|static
name|char
name|randomSeparator
parameter_list|()
block|{
return|return
name|randomFrom
argument_list|(
literal|' '
argument_list|,
name|HighlightUtils
operator|.
name|NULL_SEPARATOR
argument_list|,
name|HighlightUtils
operator|.
name|PARAGRAPH_SEPARATOR
argument_list|)
return|;
block|}
DECL|method|assertSameBreaks
specifier|private
specifier|static
name|void
name|assertSameBreaks
parameter_list|(
name|String
name|text
parameter_list|,
name|BreakIterator
name|expected
parameter_list|,
name|BreakIterator
name|actual
parameter_list|)
block|{
name|assertSameBreaks
argument_list|(
operator|new
name|StringCharacterIterator
argument_list|(
name|text
argument_list|)
argument_list|,
operator|new
name|StringCharacterIterator
argument_list|(
name|text
argument_list|)
argument_list|,
name|expected
argument_list|,
name|actual
argument_list|)
expr_stmt|;
block|}
DECL|method|assertSameBreaks
specifier|private
specifier|static
name|void
name|assertSameBreaks
parameter_list|(
name|String
name|text
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|,
name|BreakIterator
name|expected
parameter_list|,
name|BreakIterator
name|actual
parameter_list|)
block|{
name|assertSameBreaks
argument_list|(
name|text
argument_list|,
name|offset
argument_list|,
name|length
argument_list|,
name|offset
argument_list|,
name|expected
argument_list|,
name|actual
argument_list|)
expr_stmt|;
block|}
DECL|method|assertSameBreaks
specifier|private
specifier|static
name|void
name|assertSameBreaks
parameter_list|(
name|String
name|text
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|,
name|int
name|current
parameter_list|,
name|BreakIterator
name|expected
parameter_list|,
name|BreakIterator
name|actual
parameter_list|)
block|{
name|assertSameBreaks
argument_list|(
operator|new
name|StringCharacterIterator
argument_list|(
name|text
argument_list|,
name|offset
argument_list|,
name|offset
operator|+
name|length
argument_list|,
name|current
argument_list|)
argument_list|,
operator|new
name|StringCharacterIterator
argument_list|(
name|text
argument_list|,
name|offset
argument_list|,
name|offset
operator|+
name|length
argument_list|,
name|current
argument_list|)
argument_list|,
name|expected
argument_list|,
name|actual
argument_list|)
expr_stmt|;
block|}
comment|/** Asserts that two breakiterators break the text the same way */
DECL|method|assertSameBreaks
specifier|private
specifier|static
name|void
name|assertSameBreaks
parameter_list|(
name|CharacterIterator
name|one
parameter_list|,
name|CharacterIterator
name|two
parameter_list|,
name|BreakIterator
name|expected
parameter_list|,
name|BreakIterator
name|actual
parameter_list|)
block|{
name|expected
operator|.
name|setText
argument_list|(
name|one
argument_list|)
expr_stmt|;
name|actual
operator|.
name|setText
argument_list|(
name|two
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
operator|.
name|current
argument_list|()
argument_list|,
name|actual
operator|.
name|current
argument_list|()
argument_list|)
expr_stmt|;
comment|// next()
name|int
name|v
init|=
name|expected
operator|.
name|current
argument_list|()
decl_stmt|;
while|while
condition|(
name|v
operator|!=
name|BreakIterator
operator|.
name|DONE
condition|)
block|{
name|assertEquals
argument_list|(
name|v
operator|=
name|expected
operator|.
name|next
argument_list|()
argument_list|,
name|actual
operator|.
name|next
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
operator|.
name|current
argument_list|()
argument_list|,
name|actual
operator|.
name|current
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// first()
name|assertEquals
argument_list|(
name|expected
operator|.
name|first
argument_list|()
argument_list|,
name|actual
operator|.
name|first
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
operator|.
name|current
argument_list|()
argument_list|,
name|actual
operator|.
name|current
argument_list|()
argument_list|)
expr_stmt|;
comment|// last()
name|assertEquals
argument_list|(
name|expected
operator|.
name|last
argument_list|()
argument_list|,
name|actual
operator|.
name|last
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
operator|.
name|current
argument_list|()
argument_list|,
name|actual
operator|.
name|current
argument_list|()
argument_list|)
expr_stmt|;
comment|// previous()
name|v
operator|=
name|expected
operator|.
name|current
argument_list|()
expr_stmt|;
while|while
condition|(
name|v
operator|!=
name|BreakIterator
operator|.
name|DONE
condition|)
block|{
name|assertEquals
argument_list|(
name|v
operator|=
name|expected
operator|.
name|previous
argument_list|()
argument_list|,
name|actual
operator|.
name|previous
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
operator|.
name|current
argument_list|()
argument_list|,
name|actual
operator|.
name|current
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// following()
for|for
control|(
name|int
name|i
init|=
name|one
operator|.
name|getBeginIndex
argument_list|()
init|;
name|i
operator|<=
name|one
operator|.
name|getEndIndex
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|expected
operator|.
name|first
argument_list|()
expr_stmt|;
name|actual
operator|.
name|first
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
operator|.
name|following
argument_list|(
name|i
argument_list|)
argument_list|,
name|actual
operator|.
name|following
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
operator|.
name|current
argument_list|()
argument_list|,
name|actual
operator|.
name|current
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// preceding()
for|for
control|(
name|int
name|i
init|=
name|one
operator|.
name|getBeginIndex
argument_list|()
init|;
name|i
operator|<=
name|one
operator|.
name|getEndIndex
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|expected
operator|.
name|last
argument_list|()
expr_stmt|;
name|actual
operator|.
name|last
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
operator|.
name|preceding
argument_list|(
name|i
argument_list|)
argument_list|,
name|actual
operator|.
name|preceding
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
operator|.
name|current
argument_list|()
argument_list|,
name|actual
operator|.
name|current
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

