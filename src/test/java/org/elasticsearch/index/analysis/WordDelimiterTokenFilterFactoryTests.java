begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

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
name|core
operator|.
name|WhitespaceTokenizer
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

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
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
name|StringReader
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|settings
operator|.
name|ImmutableSettings
operator|.
name|settingsBuilder
import|;
end_import

begin_class
DECL|class|WordDelimiterTokenFilterFactoryTests
specifier|public
class|class
name|WordDelimiterTokenFilterFactoryTests
extends|extends
name|ElasticsearchTokenStreamTestCase
block|{
annotation|@
name|Test
DECL|method|testDefault
specifier|public
name|void
name|testDefault
parameter_list|()
throws|throws
name|IOException
block|{
name|AnalysisService
name|analysisService
init|=
name|AnalysisTestsHelper
operator|.
name|createAnalysisServiceFromSettings
argument_list|(
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"path.home"
argument_list|,
name|createTempDir
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.analysis.filter.my_word_delimiter.type"
argument_list|,
literal|"word_delimiter"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|TokenFilterFactory
name|tokenFilter
init|=
name|analysisService
operator|.
name|tokenFilter
argument_list|(
literal|"my_word_delimiter"
argument_list|)
decl_stmt|;
name|String
name|source
init|=
literal|"PowerShot 500-42 wi-fi wi-fi-4000 j2se O'Neil's"
decl_stmt|;
name|String
index|[]
name|expected
init|=
operator|new
name|String
index|[]
block|{
literal|"Power"
block|,
literal|"Shot"
block|,
literal|"500"
block|,
literal|"42"
block|,
literal|"wi"
block|,
literal|"fi"
block|,
literal|"wi"
block|,
literal|"fi"
block|,
literal|"4000"
block|,
literal|"j"
block|,
literal|"2"
block|,
literal|"se"
block|,
literal|"O"
block|,
literal|"Neil"
block|}
decl_stmt|;
name|Tokenizer
name|tokenizer
init|=
operator|new
name|WhitespaceTokenizer
argument_list|()
decl_stmt|;
name|tokenizer
operator|.
name|setReader
argument_list|(
operator|new
name|StringReader
argument_list|(
name|source
argument_list|)
argument_list|)
expr_stmt|;
name|assertTokenStreamContents
argument_list|(
name|tokenFilter
operator|.
name|create
argument_list|(
name|tokenizer
argument_list|)
argument_list|,
name|expected
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testCatenateWords
specifier|public
name|void
name|testCatenateWords
parameter_list|()
throws|throws
name|IOException
block|{
name|AnalysisService
name|analysisService
init|=
name|AnalysisTestsHelper
operator|.
name|createAnalysisServiceFromSettings
argument_list|(
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"path.home"
argument_list|,
name|createTempDir
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.analysis.filter.my_word_delimiter.type"
argument_list|,
literal|"word_delimiter"
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.analysis.filter.my_word_delimiter.catenate_words"
argument_list|,
literal|"true"
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.analysis.filter.my_word_delimiter.generate_word_parts"
argument_list|,
literal|"false"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|TokenFilterFactory
name|tokenFilter
init|=
name|analysisService
operator|.
name|tokenFilter
argument_list|(
literal|"my_word_delimiter"
argument_list|)
decl_stmt|;
name|String
name|source
init|=
literal|"PowerShot 500-42 wi-fi wi-fi-4000 j2se O'Neil's"
decl_stmt|;
name|String
index|[]
name|expected
init|=
operator|new
name|String
index|[]
block|{
literal|"PowerShot"
block|,
literal|"500"
block|,
literal|"42"
block|,
literal|"wifi"
block|,
literal|"wifi"
block|,
literal|"4000"
block|,
literal|"j"
block|,
literal|"2"
block|,
literal|"se"
block|,
literal|"ONeil"
block|}
decl_stmt|;
name|Tokenizer
name|tokenizer
init|=
operator|new
name|WhitespaceTokenizer
argument_list|()
decl_stmt|;
name|tokenizer
operator|.
name|setReader
argument_list|(
operator|new
name|StringReader
argument_list|(
name|source
argument_list|)
argument_list|)
expr_stmt|;
name|assertTokenStreamContents
argument_list|(
name|tokenFilter
operator|.
name|create
argument_list|(
name|tokenizer
argument_list|)
argument_list|,
name|expected
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testCatenateNumbers
specifier|public
name|void
name|testCatenateNumbers
parameter_list|()
throws|throws
name|IOException
block|{
name|AnalysisService
name|analysisService
init|=
name|AnalysisTestsHelper
operator|.
name|createAnalysisServiceFromSettings
argument_list|(
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"path.home"
argument_list|,
name|createTempDir
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.analysis.filter.my_word_delimiter.type"
argument_list|,
literal|"word_delimiter"
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.analysis.filter.my_word_delimiter.generate_number_parts"
argument_list|,
literal|"false"
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.analysis.filter.my_word_delimiter.catenate_numbers"
argument_list|,
literal|"true"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|TokenFilterFactory
name|tokenFilter
init|=
name|analysisService
operator|.
name|tokenFilter
argument_list|(
literal|"my_word_delimiter"
argument_list|)
decl_stmt|;
name|String
name|source
init|=
literal|"PowerShot 500-42 wi-fi wi-fi-4000 j2se O'Neil's"
decl_stmt|;
name|String
index|[]
name|expected
init|=
operator|new
name|String
index|[]
block|{
literal|"Power"
block|,
literal|"Shot"
block|,
literal|"50042"
block|,
literal|"wi"
block|,
literal|"fi"
block|,
literal|"wi"
block|,
literal|"fi"
block|,
literal|"4000"
block|,
literal|"j"
block|,
literal|"2"
block|,
literal|"se"
block|,
literal|"O"
block|,
literal|"Neil"
block|}
decl_stmt|;
name|Tokenizer
name|tokenizer
init|=
operator|new
name|WhitespaceTokenizer
argument_list|()
decl_stmt|;
name|tokenizer
operator|.
name|setReader
argument_list|(
operator|new
name|StringReader
argument_list|(
name|source
argument_list|)
argument_list|)
expr_stmt|;
name|assertTokenStreamContents
argument_list|(
name|tokenFilter
operator|.
name|create
argument_list|(
name|tokenizer
argument_list|)
argument_list|,
name|expected
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testCatenateAll
specifier|public
name|void
name|testCatenateAll
parameter_list|()
throws|throws
name|IOException
block|{
name|AnalysisService
name|analysisService
init|=
name|AnalysisTestsHelper
operator|.
name|createAnalysisServiceFromSettings
argument_list|(
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"path.home"
argument_list|,
name|createTempDir
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.analysis.filter.my_word_delimiter.type"
argument_list|,
literal|"word_delimiter"
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.analysis.filter.my_word_delimiter.generate_word_parts"
argument_list|,
literal|"false"
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.analysis.filter.my_word_delimiter.generate_number_parts"
argument_list|,
literal|"false"
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.analysis.filter.my_word_delimiter.catenate_all"
argument_list|,
literal|"true"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|TokenFilterFactory
name|tokenFilter
init|=
name|analysisService
operator|.
name|tokenFilter
argument_list|(
literal|"my_word_delimiter"
argument_list|)
decl_stmt|;
name|String
name|source
init|=
literal|"PowerShot 500-42 wi-fi wi-fi-4000 j2se O'Neil's"
decl_stmt|;
name|String
index|[]
name|expected
init|=
operator|new
name|String
index|[]
block|{
literal|"PowerShot"
block|,
literal|"50042"
block|,
literal|"wifi"
block|,
literal|"wifi4000"
block|,
literal|"j2se"
block|,
literal|"ONeil"
block|}
decl_stmt|;
name|Tokenizer
name|tokenizer
init|=
operator|new
name|WhitespaceTokenizer
argument_list|()
decl_stmt|;
name|tokenizer
operator|.
name|setReader
argument_list|(
operator|new
name|StringReader
argument_list|(
name|source
argument_list|)
argument_list|)
expr_stmt|;
name|assertTokenStreamContents
argument_list|(
name|tokenFilter
operator|.
name|create
argument_list|(
name|tokenizer
argument_list|)
argument_list|,
name|expected
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testSplitOnCaseChange
specifier|public
name|void
name|testSplitOnCaseChange
parameter_list|()
throws|throws
name|IOException
block|{
name|AnalysisService
name|analysisService
init|=
name|AnalysisTestsHelper
operator|.
name|createAnalysisServiceFromSettings
argument_list|(
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"path.home"
argument_list|,
name|createTempDir
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.analysis.filter.my_word_delimiter.type"
argument_list|,
literal|"word_delimiter"
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.analysis.filter.my_word_delimiter.split_on_case_change"
argument_list|,
literal|"false"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|TokenFilterFactory
name|tokenFilter
init|=
name|analysisService
operator|.
name|tokenFilter
argument_list|(
literal|"my_word_delimiter"
argument_list|)
decl_stmt|;
name|String
name|source
init|=
literal|"PowerShot"
decl_stmt|;
name|String
index|[]
name|expected
init|=
operator|new
name|String
index|[]
block|{
literal|"PowerShot"
block|}
decl_stmt|;
name|Tokenizer
name|tokenizer
init|=
operator|new
name|WhitespaceTokenizer
argument_list|()
decl_stmt|;
name|tokenizer
operator|.
name|setReader
argument_list|(
operator|new
name|StringReader
argument_list|(
name|source
argument_list|)
argument_list|)
expr_stmt|;
name|assertTokenStreamContents
argument_list|(
name|tokenFilter
operator|.
name|create
argument_list|(
name|tokenizer
argument_list|)
argument_list|,
name|expected
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testPreserveOriginal
specifier|public
name|void
name|testPreserveOriginal
parameter_list|()
throws|throws
name|IOException
block|{
name|AnalysisService
name|analysisService
init|=
name|AnalysisTestsHelper
operator|.
name|createAnalysisServiceFromSettings
argument_list|(
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"path.home"
argument_list|,
name|createTempDir
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.analysis.filter.my_word_delimiter.type"
argument_list|,
literal|"word_delimiter"
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.analysis.filter.my_word_delimiter.preserve_original"
argument_list|,
literal|"true"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|TokenFilterFactory
name|tokenFilter
init|=
name|analysisService
operator|.
name|tokenFilter
argument_list|(
literal|"my_word_delimiter"
argument_list|)
decl_stmt|;
name|String
name|source
init|=
literal|"PowerShot 500-42 wi-fi wi-fi-4000 j2se O'Neil's"
decl_stmt|;
name|String
index|[]
name|expected
init|=
operator|new
name|String
index|[]
block|{
literal|"PowerShot"
block|,
literal|"Power"
block|,
literal|"Shot"
block|,
literal|"500-42"
block|,
literal|"500"
block|,
literal|"42"
block|,
literal|"wi-fi"
block|,
literal|"wi"
block|,
literal|"fi"
block|,
literal|"wi-fi-4000"
block|,
literal|"wi"
block|,
literal|"fi"
block|,
literal|"4000"
block|,
literal|"j2se"
block|,
literal|"j"
block|,
literal|"2"
block|,
literal|"se"
block|,
literal|"O'Neil's"
block|,
literal|"O"
block|,
literal|"Neil"
block|}
decl_stmt|;
name|Tokenizer
name|tokenizer
init|=
operator|new
name|WhitespaceTokenizer
argument_list|()
decl_stmt|;
name|tokenizer
operator|.
name|setReader
argument_list|(
operator|new
name|StringReader
argument_list|(
name|source
argument_list|)
argument_list|)
expr_stmt|;
name|assertTokenStreamContents
argument_list|(
name|tokenFilter
operator|.
name|create
argument_list|(
name|tokenizer
argument_list|)
argument_list|,
name|expected
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testStemEnglishPossessive
specifier|public
name|void
name|testStemEnglishPossessive
parameter_list|()
throws|throws
name|IOException
block|{
name|AnalysisService
name|analysisService
init|=
name|AnalysisTestsHelper
operator|.
name|createAnalysisServiceFromSettings
argument_list|(
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"path.home"
argument_list|,
name|createTempDir
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.analysis.filter.my_word_delimiter.type"
argument_list|,
literal|"word_delimiter"
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.analysis.filter.my_word_delimiter.stem_english_possessive"
argument_list|,
literal|"false"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|TokenFilterFactory
name|tokenFilter
init|=
name|analysisService
operator|.
name|tokenFilter
argument_list|(
literal|"my_word_delimiter"
argument_list|)
decl_stmt|;
name|String
name|source
init|=
literal|"PowerShot 500-42 wi-fi wi-fi-4000 j2se O'Neil's"
decl_stmt|;
name|String
index|[]
name|expected
init|=
operator|new
name|String
index|[]
block|{
literal|"Power"
block|,
literal|"Shot"
block|,
literal|"500"
block|,
literal|"42"
block|,
literal|"wi"
block|,
literal|"fi"
block|,
literal|"wi"
block|,
literal|"fi"
block|,
literal|"4000"
block|,
literal|"j"
block|,
literal|"2"
block|,
literal|"se"
block|,
literal|"O"
block|,
literal|"Neil"
block|,
literal|"s"
block|}
decl_stmt|;
name|Tokenizer
name|tokenizer
init|=
operator|new
name|WhitespaceTokenizer
argument_list|()
decl_stmt|;
name|tokenizer
operator|.
name|setReader
argument_list|(
operator|new
name|StringReader
argument_list|(
name|source
argument_list|)
argument_list|)
expr_stmt|;
name|assertTokenStreamContents
argument_list|(
name|tokenFilter
operator|.
name|create
argument_list|(
name|tokenizer
argument_list|)
argument_list|,
name|expected
argument_list|)
expr_stmt|;
block|}
comment|/** Correct offset order when doing both parts and concatenation: PowerShot is a synonym of Power */
annotation|@
name|Test
DECL|method|testPartsAndCatenate
specifier|public
name|void
name|testPartsAndCatenate
parameter_list|()
throws|throws
name|IOException
block|{
name|AnalysisService
name|analysisService
init|=
name|AnalysisTestsHelper
operator|.
name|createAnalysisServiceFromSettings
argument_list|(
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"path.home"
argument_list|,
name|createTempDir
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.analysis.filter.my_word_delimiter.type"
argument_list|,
literal|"word_delimiter"
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.analysis.filter.my_word_delimiter.catenate_words"
argument_list|,
literal|"true"
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.analysis.filter.my_word_delimiter.generate_word_parts"
argument_list|,
literal|"true"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|TokenFilterFactory
name|tokenFilter
init|=
name|analysisService
operator|.
name|tokenFilter
argument_list|(
literal|"my_word_delimiter"
argument_list|)
decl_stmt|;
name|String
name|source
init|=
literal|"PowerShot"
decl_stmt|;
name|String
index|[]
name|expected
init|=
operator|new
name|String
index|[]
block|{
literal|"Power"
block|,
literal|"PowerShot"
block|,
literal|"Shot"
block|}
decl_stmt|;
name|Tokenizer
name|tokenizer
init|=
operator|new
name|WhitespaceTokenizer
argument_list|()
decl_stmt|;
name|tokenizer
operator|.
name|setReader
argument_list|(
operator|new
name|StringReader
argument_list|(
name|source
argument_list|)
argument_list|)
expr_stmt|;
name|assertTokenStreamContents
argument_list|(
name|tokenFilter
operator|.
name|create
argument_list|(
name|tokenizer
argument_list|)
argument_list|,
name|expected
argument_list|)
expr_stmt|;
block|}
comment|/** Back compat:       * old offset order when doing both parts and concatenation: PowerShot is a synonym of Shot */
annotation|@
name|Test
DECL|method|testDeprecatedPartsAndCatenate
specifier|public
name|void
name|testDeprecatedPartsAndCatenate
parameter_list|()
throws|throws
name|IOException
block|{
name|AnalysisService
name|analysisService
init|=
name|AnalysisTestsHelper
operator|.
name|createAnalysisServiceFromSettings
argument_list|(
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"path.home"
argument_list|,
name|createTempDir
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.analysis.filter.my_word_delimiter.type"
argument_list|,
literal|"word_delimiter"
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.analysis.filter.my_word_delimiter.catenate_words"
argument_list|,
literal|"true"
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.analysis.filter.my_word_delimiter.generate_word_parts"
argument_list|,
literal|"true"
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.analysis.filter.my_word_delimiter.version"
argument_list|,
literal|"4.7"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|TokenFilterFactory
name|tokenFilter
init|=
name|analysisService
operator|.
name|tokenFilter
argument_list|(
literal|"my_word_delimiter"
argument_list|)
decl_stmt|;
name|String
name|source
init|=
literal|"PowerShot"
decl_stmt|;
name|String
index|[]
name|expected
init|=
operator|new
name|String
index|[]
block|{
literal|"Power"
block|,
literal|"Shot"
block|,
literal|"PowerShot"
block|}
decl_stmt|;
name|Tokenizer
name|tokenizer
init|=
operator|new
name|WhitespaceTokenizer
argument_list|()
decl_stmt|;
name|tokenizer
operator|.
name|setReader
argument_list|(
operator|new
name|StringReader
argument_list|(
name|source
argument_list|)
argument_list|)
expr_stmt|;
name|assertTokenStreamContents
argument_list|(
name|tokenFilter
operator|.
name|create
argument_list|(
name|tokenizer
argument_list|)
argument_list|,
name|expected
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

