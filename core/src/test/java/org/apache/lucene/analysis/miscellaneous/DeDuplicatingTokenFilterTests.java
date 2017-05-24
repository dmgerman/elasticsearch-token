begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.apache.lucene.analysis.miscellaneous
package|package
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|analysis
operator|.
name|miscellaneous
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
name|Analyzer
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
name|MockTokenizer
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
name|analysis
operator|.
name|Tokenizer
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
name|io
operator|.
name|IOException
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
name|equalTo
import|;
end_import

begin_class
DECL|class|DeDuplicatingTokenFilterTests
specifier|public
class|class
name|DeDuplicatingTokenFilterTests
extends|extends
name|ESTestCase
block|{
DECL|method|testSimple
specifier|public
name|void
name|testSimple
parameter_list|()
throws|throws
name|IOException
block|{
name|DuplicateByteSequenceSpotter
name|bytesDeDuper
init|=
operator|new
name|DuplicateByteSequenceSpotter
argument_list|()
decl_stmt|;
name|Analyzer
name|analyzer
init|=
operator|new
name|Analyzer
argument_list|()
block|{
annotation|@
name|Override
specifier|protected
name|TokenStreamComponents
name|createComponents
parameter_list|(
name|String
name|fieldName
parameter_list|)
block|{
name|Tokenizer
name|t
init|=
operator|new
name|MockTokenizer
argument_list|(
name|MockTokenizer
operator|.
name|WHITESPACE
argument_list|,
literal|false
argument_list|)
decl_stmt|;
return|return
operator|new
name|TokenStreamComponents
argument_list|(
name|t
argument_list|,
operator|new
name|DeDuplicatingTokenFilter
argument_list|(
name|t
argument_list|,
name|bytesDeDuper
argument_list|)
argument_list|)
return|;
block|}
block|}
decl_stmt|;
name|String
name|input
init|=
literal|"a b c 1 2 3 4 5 6 7 a b c d 1 2 3 4 5 6 7 e f 1 2 3 4 5 6 7"
decl_stmt|;
name|String
name|expectedOutput
init|=
literal|"a b c 1 2 3 4 5 6 7 a b c d e f"
decl_stmt|;
name|TokenStream
name|test
init|=
name|analyzer
operator|.
name|tokenStream
argument_list|(
literal|"test"
argument_list|,
name|input
argument_list|)
decl_stmt|;
name|CharTermAttribute
name|termAttribute
init|=
name|test
operator|.
name|addAttribute
argument_list|(
name|CharTermAttribute
operator|.
name|class
argument_list|)
decl_stmt|;
name|test
operator|.
name|reset
argument_list|()
expr_stmt|;
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
while|while
condition|(
name|test
operator|.
name|incrementToken
argument_list|()
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|termAttribute
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|" "
argument_list|)
expr_stmt|;
block|}
name|String
name|output
init|=
name|sb
operator|.
name|toString
argument_list|()
operator|.
name|trim
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|output
argument_list|,
name|equalTo
argument_list|(
name|expectedOutput
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testHitCountLimits
specifier|public
name|void
name|testHitCountLimits
parameter_list|()
throws|throws
name|IOException
block|{
name|DuplicateByteSequenceSpotter
name|bytesDeDuper
init|=
operator|new
name|DuplicateByteSequenceSpotter
argument_list|()
decl_stmt|;
name|long
name|peakMemoryUsed
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
name|DuplicateByteSequenceSpotter
operator|.
name|MAX_HIT_COUNT
operator|*
literal|2
condition|;
name|i
operator|++
control|)
block|{
name|Analyzer
name|analyzer
init|=
operator|new
name|Analyzer
argument_list|()
block|{
annotation|@
name|Override
specifier|protected
name|TokenStreamComponents
name|createComponents
parameter_list|(
name|String
name|fieldName
parameter_list|)
block|{
name|Tokenizer
name|t
init|=
operator|new
name|MockTokenizer
argument_list|(
name|MockTokenizer
operator|.
name|WHITESPACE
argument_list|,
literal|false
argument_list|)
decl_stmt|;
return|return
operator|new
name|TokenStreamComponents
argument_list|(
name|t
argument_list|,
operator|new
name|DeDuplicatingTokenFilter
argument_list|(
name|t
argument_list|,
name|bytesDeDuper
argument_list|,
literal|true
argument_list|)
argument_list|)
return|;
block|}
block|}
decl_stmt|;
try|try
block|{
name|String
name|input
init|=
literal|"1 2 3 4 5 6"
decl_stmt|;
name|bytesDeDuper
operator|.
name|startNewSequence
argument_list|()
expr_stmt|;
name|TokenStream
name|test
init|=
name|analyzer
operator|.
name|tokenStream
argument_list|(
literal|"test"
argument_list|,
name|input
argument_list|)
decl_stmt|;
name|DuplicateSequenceAttribute
name|dsa
init|=
name|test
operator|.
name|addAttribute
argument_list|(
name|DuplicateSequenceAttribute
operator|.
name|class
argument_list|)
decl_stmt|;
name|test
operator|.
name|reset
argument_list|()
expr_stmt|;
while|while
condition|(
name|test
operator|.
name|incrementToken
argument_list|()
condition|)
block|{
name|assertEquals
argument_list|(
name|Math
operator|.
name|min
argument_list|(
name|DuplicateByteSequenceSpotter
operator|.
name|MAX_HIT_COUNT
argument_list|,
name|i
argument_list|)
argument_list|,
name|dsa
operator|.
name|getNumPriorUsesInASequence
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|i
operator|==
literal|0
condition|)
block|{
name|peakMemoryUsed
operator|=
name|bytesDeDuper
operator|.
name|getEstimatedSizeInBytes
argument_list|()
expr_stmt|;
block|}
else|else
block|{
comment|// Given we are feeding the same content repeatedly the
comment|// actual memory
comment|// used by bytesDeDuper should not grow
name|assertEquals
argument_list|(
name|peakMemoryUsed
argument_list|,
name|bytesDeDuper
operator|.
name|getEstimatedSizeInBytes
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|analyzer
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
DECL|method|testTaggedFrequencies
specifier|public
name|void
name|testTaggedFrequencies
parameter_list|()
throws|throws
name|IOException
block|{
name|DuplicateByteSequenceSpotter
name|bytesDeDuper
init|=
operator|new
name|DuplicateByteSequenceSpotter
argument_list|()
decl_stmt|;
name|Analyzer
name|analyzer
init|=
operator|new
name|Analyzer
argument_list|()
block|{
annotation|@
name|Override
specifier|protected
name|TokenStreamComponents
name|createComponents
parameter_list|(
name|String
name|fieldName
parameter_list|)
block|{
name|Tokenizer
name|t
init|=
operator|new
name|MockTokenizer
argument_list|(
name|MockTokenizer
operator|.
name|WHITESPACE
argument_list|,
literal|false
argument_list|)
decl_stmt|;
return|return
operator|new
name|TokenStreamComponents
argument_list|(
name|t
argument_list|,
operator|new
name|DeDuplicatingTokenFilter
argument_list|(
name|t
argument_list|,
name|bytesDeDuper
argument_list|,
literal|true
argument_list|)
argument_list|)
return|;
block|}
block|}
decl_stmt|;
try|try
block|{
name|String
name|input
init|=
literal|"a b c 1 2 3 4 5 6 7 a b c d 1 2 3 4 5 6 7 e f 1 2 3 4 5 6 7"
decl_stmt|;
name|short
index|[]
name|expectedFrequencies
init|=
block|{
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|1
block|,
literal|1
block|,
literal|1
block|,
literal|1
block|,
literal|1
block|,
literal|1
block|,
literal|1
block|,
literal|0
block|,
literal|0
block|,
literal|2
block|,
literal|2
block|,
literal|2
block|,
literal|2
block|,
literal|2
block|,
literal|2
block|,
literal|2
block|}
decl_stmt|;
name|TokenStream
name|test
init|=
name|analyzer
operator|.
name|tokenStream
argument_list|(
literal|"test"
argument_list|,
name|input
argument_list|)
decl_stmt|;
name|DuplicateSequenceAttribute
name|seqAtt
init|=
name|test
operator|.
name|addAttribute
argument_list|(
name|DuplicateSequenceAttribute
operator|.
name|class
argument_list|)
decl_stmt|;
name|test
operator|.
name|reset
argument_list|()
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
name|expectedFrequencies
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|assertThat
argument_list|(
name|test
operator|.
name|incrementToken
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|seqAtt
operator|.
name|getNumPriorUsesInASequence
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|expectedFrequencies
index|[
name|i
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|assertThat
argument_list|(
name|test
operator|.
name|incrementToken
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|analyzer
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

