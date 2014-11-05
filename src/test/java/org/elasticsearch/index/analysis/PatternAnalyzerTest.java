begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_package
DECL|package|org.elasticsearch.index.analysis
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|analysis
package|;
end_package

begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

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
name|lang
operator|.
name|Thread
operator|.
name|UncaughtExceptionHandler
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
name|regex
operator|.
name|Pattern
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
name|core
operator|.
name|StopAnalyzer
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
name|ElasticsearchTokenStreamTestCase
import|;
end_import

begin_comment
comment|/**  * Verifies the behavior of PatternAnalyzer.  */
end_comment

begin_class
DECL|class|PatternAnalyzerTest
specifier|public
class|class
name|PatternAnalyzerTest
extends|extends
name|ElasticsearchTokenStreamTestCase
block|{
comment|/**    * Test PatternAnalyzer when it is configured with a non-word pattern.    */
DECL|method|testNonWordPattern
specifier|public
name|void
name|testNonWordPattern
parameter_list|()
throws|throws
name|IOException
block|{
comment|// Split on non-letter pattern, do not lowercase, no stopwords
name|PatternAnalyzer
name|a
init|=
operator|new
name|PatternAnalyzer
argument_list|(
name|Pattern
operator|.
name|compile
argument_list|(
literal|"\\W+"
argument_list|)
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|assertAnalyzesTo
argument_list|(
name|a
argument_list|,
literal|"The quick brown Fox,the abcd1234 (56.78) dc."
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"The"
block|,
literal|"quick"
block|,
literal|"brown"
block|,
literal|"Fox"
block|,
literal|"the"
block|,
literal|"abcd1234"
block|,
literal|"56"
block|,
literal|"78"
block|,
literal|"dc"
block|}
argument_list|)
expr_stmt|;
comment|// split on non-letter pattern, lowercase, english stopwords
name|PatternAnalyzer
name|b
init|=
operator|new
name|PatternAnalyzer
argument_list|(
name|Pattern
operator|.
name|compile
argument_list|(
literal|"\\W+"
argument_list|)
argument_list|,
literal|true
argument_list|,
name|StopAnalyzer
operator|.
name|ENGLISH_STOP_WORDS_SET
argument_list|)
decl_stmt|;
name|assertAnalyzesTo
argument_list|(
name|b
argument_list|,
literal|"The quick brown Fox,the abcd1234 (56.78) dc."
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"quick"
block|,
literal|"brown"
block|,
literal|"fox"
block|,
literal|"abcd1234"
block|,
literal|"56"
block|,
literal|"78"
block|,
literal|"dc"
block|}
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test PatternAnalyzer when it is configured with a whitespace pattern.    * Behavior can be similar to WhitespaceAnalyzer (depending upon options)    */
DECL|method|testWhitespacePattern
specifier|public
name|void
name|testWhitespacePattern
parameter_list|()
throws|throws
name|IOException
block|{
comment|// Split on whitespace patterns, do not lowercase, no stopwords
name|PatternAnalyzer
name|a
init|=
operator|new
name|PatternAnalyzer
argument_list|(
name|Pattern
operator|.
name|compile
argument_list|(
literal|"\\s+"
argument_list|)
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|assertAnalyzesTo
argument_list|(
name|a
argument_list|,
literal|"The quick brown Fox,the abcd1234 (56.78) dc."
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"The"
block|,
literal|"quick"
block|,
literal|"brown"
block|,
literal|"Fox,the"
block|,
literal|"abcd1234"
block|,
literal|"(56.78)"
block|,
literal|"dc."
block|}
argument_list|)
expr_stmt|;
comment|// Split on whitespace patterns, lowercase, english stopwords
name|PatternAnalyzer
name|b
init|=
operator|new
name|PatternAnalyzer
argument_list|(
name|Pattern
operator|.
name|compile
argument_list|(
literal|"\\s+"
argument_list|)
argument_list|,
literal|true
argument_list|,
name|StopAnalyzer
operator|.
name|ENGLISH_STOP_WORDS_SET
argument_list|)
decl_stmt|;
name|assertAnalyzesTo
argument_list|(
name|b
argument_list|,
literal|"The quick brown Fox,the abcd1234 (56.78) dc."
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"quick"
block|,
literal|"brown"
block|,
literal|"fox,the"
block|,
literal|"abcd1234"
block|,
literal|"(56.78)"
block|,
literal|"dc."
block|}
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test PatternAnalyzer when it is configured with a custom pattern. In this    * case, text is tokenized on the comma ","    */
DECL|method|testCustomPattern
specifier|public
name|void
name|testCustomPattern
parameter_list|()
throws|throws
name|IOException
block|{
comment|// Split on comma, do not lowercase, no stopwords
name|PatternAnalyzer
name|a
init|=
operator|new
name|PatternAnalyzer
argument_list|(
name|Pattern
operator|.
name|compile
argument_list|(
literal|","
argument_list|)
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|assertAnalyzesTo
argument_list|(
name|a
argument_list|,
literal|"Here,Are,some,Comma,separated,words,"
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"Here"
block|,
literal|"Are"
block|,
literal|"some"
block|,
literal|"Comma"
block|,
literal|"separated"
block|,
literal|"words"
block|}
argument_list|)
expr_stmt|;
comment|// split on comma, lowercase, english stopwords
name|PatternAnalyzer
name|b
init|=
operator|new
name|PatternAnalyzer
argument_list|(
name|Pattern
operator|.
name|compile
argument_list|(
literal|","
argument_list|)
argument_list|,
literal|true
argument_list|,
name|StopAnalyzer
operator|.
name|ENGLISH_STOP_WORDS_SET
argument_list|)
decl_stmt|;
name|assertAnalyzesTo
argument_list|(
name|b
argument_list|,
literal|"Here,Are,some,Comma,separated,words,"
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"here"
block|,
literal|"some"
block|,
literal|"comma"
block|,
literal|"separated"
block|,
literal|"words"
block|}
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test PatternAnalyzer against a large document.    */
DECL|method|testHugeDocument
specifier|public
name|void
name|testHugeDocument
parameter_list|()
throws|throws
name|IOException
block|{
name|StringBuilder
name|document
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
comment|// 5000 a's
name|char
name|largeWord
index|[]
init|=
operator|new
name|char
index|[
literal|5000
index|]
decl_stmt|;
name|Arrays
operator|.
name|fill
argument_list|(
name|largeWord
argument_list|,
literal|'a'
argument_list|)
expr_stmt|;
name|document
operator|.
name|append
argument_list|(
name|largeWord
argument_list|)
expr_stmt|;
comment|// a space
name|document
operator|.
name|append
argument_list|(
literal|' '
argument_list|)
expr_stmt|;
comment|// 2000 b's
name|char
name|largeWord2
index|[]
init|=
operator|new
name|char
index|[
literal|2000
index|]
decl_stmt|;
name|Arrays
operator|.
name|fill
argument_list|(
name|largeWord2
argument_list|,
literal|'b'
argument_list|)
expr_stmt|;
name|document
operator|.
name|append
argument_list|(
name|largeWord2
argument_list|)
expr_stmt|;
comment|// Split on whitespace patterns, do not lowercase, no stopwords
name|PatternAnalyzer
name|a
init|=
operator|new
name|PatternAnalyzer
argument_list|(
name|Pattern
operator|.
name|compile
argument_list|(
literal|"\\s+"
argument_list|)
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|assertAnalyzesTo
argument_list|(
name|a
argument_list|,
name|document
operator|.
name|toString
argument_list|()
argument_list|,
operator|new
name|String
index|[]
block|{
operator|new
name|String
argument_list|(
name|largeWord
argument_list|)
block|,
operator|new
name|String
argument_list|(
name|largeWord2
argument_list|)
block|}
argument_list|)
expr_stmt|;
block|}
comment|/** blast some random strings through the analyzer */
DECL|method|testRandomStrings
specifier|public
name|void
name|testRandomStrings
parameter_list|()
throws|throws
name|Exception
block|{
name|Analyzer
name|a
init|=
operator|new
name|PatternAnalyzer
argument_list|(
name|Pattern
operator|.
name|compile
argument_list|(
literal|","
argument_list|)
argument_list|,
literal|true
argument_list|,
name|StopAnalyzer
operator|.
name|ENGLISH_STOP_WORDS_SET
argument_list|)
decl_stmt|;
comment|// dodge jre bug http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=7104012
specifier|final
name|UncaughtExceptionHandler
name|savedHandler
init|=
name|Thread
operator|.
name|getDefaultUncaughtExceptionHandler
argument_list|()
decl_stmt|;
name|Thread
operator|.
name|setDefaultUncaughtExceptionHandler
argument_list|(
operator|new
name|Thread
operator|.
name|UncaughtExceptionHandler
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|uncaughtException
parameter_list|(
name|Thread
name|thread
parameter_list|,
name|Throwable
name|throwable
parameter_list|)
block|{
name|assumeTrue
argument_list|(
literal|"not failing due to jre bug "
argument_list|,
operator|!
name|isJREBug7104012
argument_list|(
name|throwable
argument_list|)
argument_list|)
expr_stmt|;
comment|// otherwise its some other bug, pass to default handler
name|savedHandler
operator|.
name|uncaughtException
argument_list|(
name|thread
argument_list|,
name|throwable
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
try|try
block|{
name|Thread
operator|.
name|getDefaultUncaughtExceptionHandler
argument_list|()
expr_stmt|;
name|checkRandomData
argument_list|(
name|random
argument_list|()
argument_list|,
name|a
argument_list|,
literal|10000
operator|*
name|RANDOM_MULTIPLIER
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ArrayIndexOutOfBoundsException
name|ex
parameter_list|)
block|{
name|assumeTrue
argument_list|(
literal|"not failing due to jre bug "
argument_list|,
operator|!
name|isJREBug7104012
argument_list|(
name|ex
argument_list|)
argument_list|)
expr_stmt|;
throw|throw
name|ex
throw|;
comment|// otherwise rethrow
block|}
finally|finally
block|{
name|Thread
operator|.
name|setDefaultUncaughtExceptionHandler
argument_list|(
name|savedHandler
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|isJREBug7104012
specifier|static
name|boolean
name|isJREBug7104012
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
if|if
condition|(
operator|!
operator|(
name|t
operator|instanceof
name|ArrayIndexOutOfBoundsException
operator|)
condition|)
block|{
comment|// BaseTokenStreamTestCase now wraps exc in a new RuntimeException:
name|t
operator|=
name|t
operator|.
name|getCause
argument_list|()
expr_stmt|;
if|if
condition|(
operator|!
operator|(
name|t
operator|instanceof
name|ArrayIndexOutOfBoundsException
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
name|StackTraceElement
name|trace
index|[]
init|=
name|t
operator|.
name|getStackTrace
argument_list|()
decl_stmt|;
for|for
control|(
name|StackTraceElement
name|st
range|:
name|trace
control|)
block|{
if|if
condition|(
literal|"java.text.RuleBasedBreakIterator"
operator|.
name|equals
argument_list|(
name|st
operator|.
name|getClassName
argument_list|()
argument_list|)
operator|||
literal|"sun.util.locale.provider.RuleBasedBreakIterator"
operator|.
name|equals
argument_list|(
name|st
operator|.
name|getClassName
argument_list|()
argument_list|)
operator|&&
literal|"lookupBackwardState"
operator|.
name|equals
argument_list|(
name|st
operator|.
name|getMethodName
argument_list|()
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
block|}
end_class

end_unit

