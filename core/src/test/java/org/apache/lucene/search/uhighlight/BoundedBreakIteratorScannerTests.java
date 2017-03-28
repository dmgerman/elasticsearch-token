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
name|Arrays
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
name|Locale
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|greaterThan
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|greaterThanOrEqualTo
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|lessThanOrEqualTo
import|;
end_import

begin_class
DECL|class|BoundedBreakIteratorScannerTests
specifier|public
class|class
name|BoundedBreakIteratorScannerTests
extends|extends
name|ESTestCase
block|{
DECL|field|WORD_BOUNDARIES
specifier|private
specifier|static
specifier|final
name|String
index|[]
name|WORD_BOUNDARIES
init|=
operator|new
name|String
index|[]
block|{
literal|" "
block|,
literal|"  "
block|,
literal|"\t"
block|,
literal|"#"
block|,
literal|"\n"
block|}
decl_stmt|;
DECL|field|SENTENCE_BOUNDARIES
specifier|private
specifier|static
specifier|final
name|String
index|[]
name|SENTENCE_BOUNDARIES
init|=
operator|new
name|String
index|[]
block|{
literal|"! "
block|,
literal|"? "
block|,
literal|". "
block|,
literal|".\n"
block|,
literal|".\n\n"
block|}
decl_stmt|;
DECL|method|testRandomAsciiTextCase
specifier|private
name|void
name|testRandomAsciiTextCase
parameter_list|(
name|BreakIterator
name|bi
parameter_list|,
name|int
name|maxLen
parameter_list|)
block|{
comment|// Generate a random set of unique terms with ascii character
name|int
name|maxSize
init|=
name|randomIntBetween
argument_list|(
literal|5
argument_list|,
literal|100
argument_list|)
decl_stmt|;
name|String
index|[]
name|vocabulary
init|=
operator|new
name|String
index|[
name|maxSize
index|]
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
name|maxSize
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|rarely
argument_list|()
condition|)
block|{
name|vocabulary
index|[
name|i
index|]
operator|=
name|randomAsciiOfLengthBetween
argument_list|(
literal|50
argument_list|,
literal|200
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|vocabulary
index|[
name|i
index|]
operator|=
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|30
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Generate a random text made of random terms separated with word-boundaries
comment|// and sentence-boundaries.
name|StringBuilder
name|text
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Integer
argument_list|>
name|offsetList
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Integer
argument_list|>
name|sizeList
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
comment|// the number of sentences to generate
name|int
name|numSentences
init|=
name|randomIntBetween
argument_list|(
literal|10
argument_list|,
literal|100
argument_list|)
decl_stmt|;
name|int
name|maxTermLen
init|=
literal|0
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
name|numSentences
condition|;
name|i
operator|++
control|)
block|{
comment|// the number of terms in the sentence
name|int
name|numTerms
init|=
name|randomIntBetween
argument_list|(
literal|5
argument_list|,
literal|10
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|numTerms
condition|;
name|j
operator|++
control|)
block|{
name|int
name|termId
init|=
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
name|vocabulary
operator|.
name|length
operator|-
literal|1
argument_list|)
decl_stmt|;
name|String
name|term
init|=
name|vocabulary
index|[
name|termId
index|]
operator|.
name|toLowerCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
decl_stmt|;
if|if
condition|(
name|j
operator|==
literal|0
condition|)
block|{
comment|// capitalize the first letter of the first term in the sentence
name|term
operator|=
name|term
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
literal|1
argument_list|)
operator|.
name|toUpperCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
operator|+
name|term
operator|.
name|substring
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|String
name|sep
init|=
name|randomFrom
argument_list|(
name|WORD_BOUNDARIES
argument_list|)
decl_stmt|;
name|text
operator|.
name|append
argument_list|(
name|sep
argument_list|)
expr_stmt|;
block|}
name|maxTermLen
operator|=
name|Math
operator|.
name|max
argument_list|(
name|term
operator|.
name|length
argument_list|()
argument_list|,
name|maxTermLen
argument_list|)
expr_stmt|;
name|offsetList
operator|.
name|add
argument_list|(
name|text
operator|.
name|length
argument_list|()
argument_list|)
expr_stmt|;
name|sizeList
operator|.
name|add
argument_list|(
name|term
operator|.
name|length
argument_list|()
argument_list|)
expr_stmt|;
name|text
operator|.
name|append
argument_list|(
name|term
argument_list|)
expr_stmt|;
block|}
name|String
name|boundary
init|=
name|randomFrom
argument_list|(
name|SENTENCE_BOUNDARIES
argument_list|)
decl_stmt|;
name|text
operator|.
name|append
argument_list|(
name|boundary
argument_list|)
expr_stmt|;
block|}
name|int
index|[]
name|sizes
init|=
name|sizeList
operator|.
name|stream
argument_list|()
operator|.
name|mapToInt
argument_list|(
name|i
lambda|->
name|i
argument_list|)
operator|.
name|toArray
argument_list|()
decl_stmt|;
name|int
index|[]
name|offsets
init|=
name|offsetList
operator|.
name|stream
argument_list|()
operator|.
name|mapToInt
argument_list|(
name|i
lambda|->
name|i
argument_list|)
operator|.
name|toArray
argument_list|()
decl_stmt|;
name|bi
operator|.
name|setText
argument_list|(
name|text
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|int
name|currentPos
init|=
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|20
argument_list|)
decl_stmt|;
name|int
name|lastEnd
init|=
operator|-
literal|1
decl_stmt|;
name|int
name|maxPassageLen
init|=
name|maxLen
operator|+
operator|(
name|maxTermLen
operator|*
literal|2
operator|)
decl_stmt|;
while|while
condition|(
name|currentPos
operator|<
name|offsets
operator|.
name|length
condition|)
block|{
comment|// find the passage that contains the current term
name|int
name|nextOffset
init|=
name|offsets
index|[
name|currentPos
index|]
decl_stmt|;
name|int
name|start
init|=
name|bi
operator|.
name|preceding
argument_list|(
name|nextOffset
operator|+
literal|1
argument_list|)
decl_stmt|;
name|int
name|end
init|=
name|bi
operator|.
name|following
argument_list|(
name|nextOffset
argument_list|)
decl_stmt|;
comment|// check that the passage is valid
name|assertThat
argument_list|(
name|start
argument_list|,
name|greaterThanOrEqualTo
argument_list|(
name|lastEnd
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|end
argument_list|,
name|greaterThan
argument_list|(
name|start
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|start
argument_list|,
name|lessThanOrEqualTo
argument_list|(
name|nextOffset
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|end
argument_list|,
name|greaterThanOrEqualTo
argument_list|(
name|nextOffset
argument_list|)
argument_list|)
expr_stmt|;
name|int
name|passageLen
init|=
name|end
operator|-
name|start
decl_stmt|;
name|assertThat
argument_list|(
name|passageLen
argument_list|,
name|lessThanOrEqualTo
argument_list|(
name|maxPassageLen
argument_list|)
argument_list|)
expr_stmt|;
comment|// checks that the start and end of the passage are on word boundaries.
name|int
name|startPos
init|=
name|Arrays
operator|.
name|binarySearch
argument_list|(
name|offsets
argument_list|,
name|start
argument_list|)
decl_stmt|;
name|int
name|endPos
init|=
name|Arrays
operator|.
name|binarySearch
argument_list|(
name|offsets
argument_list|,
name|end
argument_list|)
decl_stmt|;
if|if
condition|(
name|startPos
operator|<
literal|0
condition|)
block|{
name|int
name|lastWordEnd
init|=
name|offsets
index|[
name|Math
operator|.
name|abs
argument_list|(
name|startPos
argument_list|)
operator|-
literal|2
index|]
operator|+
name|sizes
index|[
name|Math
operator|.
name|abs
argument_list|(
name|startPos
argument_list|)
operator|-
literal|2
index|]
decl_stmt|;
name|assertThat
argument_list|(
name|start
argument_list|,
name|greaterThanOrEqualTo
argument_list|(
name|lastWordEnd
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|endPos
operator|<
literal|0
condition|)
block|{
if|if
condition|(
name|Math
operator|.
name|abs
argument_list|(
name|endPos
argument_list|)
operator|-
literal|2
operator|<
name|offsets
operator|.
name|length
condition|)
block|{
name|int
name|lastWordEnd
init|=
name|offsets
index|[
name|Math
operator|.
name|abs
argument_list|(
name|endPos
argument_list|)
operator|-
literal|2
index|]
operator|+
name|sizes
index|[
name|Math
operator|.
name|abs
argument_list|(
name|endPos
argument_list|)
operator|-
literal|2
index|]
decl_stmt|;
name|assertThat
argument_list|(
name|end
argument_list|,
name|greaterThanOrEqualTo
argument_list|(
name|lastWordEnd
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// advance the position to the end of the current passage
name|currentPos
operator|=
operator|(
name|Math
operator|.
name|abs
argument_list|(
name|endPos
argument_list|)
operator|-
literal|1
operator|)
expr_stmt|;
block|}
else|else
block|{
comment|// advance the position to the end of the current passage
name|currentPos
operator|=
name|endPos
expr_stmt|;
block|}
comment|// randomly advance to the next term to highlight
name|currentPos
operator|+=
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|20
argument_list|)
expr_stmt|;
name|lastEnd
operator|=
name|end
expr_stmt|;
block|}
block|}
DECL|method|testBoundedSentence
specifier|public
name|void
name|testBoundedSentence
parameter_list|()
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
literal|20
condition|;
name|i
operator|++
control|)
block|{
name|int
name|maxLen
init|=
name|randomIntBetween
argument_list|(
literal|10
argument_list|,
literal|500
argument_list|)
decl_stmt|;
name|testRandomAsciiTextCase
argument_list|(
name|BoundedBreakIteratorScanner
operator|.
name|getSentence
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|,
name|maxLen
argument_list|)
argument_list|,
name|maxLen
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit
