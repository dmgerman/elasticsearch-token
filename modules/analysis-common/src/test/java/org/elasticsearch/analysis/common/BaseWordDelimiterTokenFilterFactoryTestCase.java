begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.analysis.common
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|analysis
operator|.
name|common
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
name|common
operator|.
name|settings
operator|.
name|Settings
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|env
operator|.
name|Environment
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
name|analysis
operator|.
name|AnalysisTestsHelper
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
name|analysis
operator|.
name|TokenFilterFactory
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
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|ESTokenStreamTestCase
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

begin_comment
comment|/**  * Base class to test {@link WordDelimiterTokenFilterFactory} and  * {@link WordDelimiterGraphTokenFilterFactory}.  */
end_comment

begin_class
DECL|class|BaseWordDelimiterTokenFilterFactoryTestCase
specifier|public
specifier|abstract
class|class
name|BaseWordDelimiterTokenFilterFactoryTestCase
extends|extends
name|ESTokenStreamTestCase
block|{
DECL|field|type
specifier|final
name|String
name|type
decl_stmt|;
DECL|method|BaseWordDelimiterTokenFilterFactoryTestCase
specifier|public
name|BaseWordDelimiterTokenFilterFactoryTestCase
parameter_list|(
name|String
name|type
parameter_list|)
block|{
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
block|}
DECL|method|testDefault
specifier|public
name|void
name|testDefault
parameter_list|()
throws|throws
name|IOException
block|{
name|ESTestCase
operator|.
name|TestAnalysis
name|analysis
init|=
name|AnalysisTestsHelper
operator|.
name|createTestAnalysisFromSettings
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|Environment
operator|.
name|PATH_HOME_SETTING
operator|.
name|getKey
argument_list|()
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
name|type
argument_list|)
operator|.
name|build
argument_list|()
argument_list|,
operator|new
name|CommonAnalysisPlugin
argument_list|()
argument_list|)
decl_stmt|;
name|TokenFilterFactory
name|tokenFilter
init|=
name|analysis
operator|.
name|tokenFilter
operator|.
name|get
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
DECL|method|testCatenateWords
specifier|public
name|void
name|testCatenateWords
parameter_list|()
throws|throws
name|IOException
block|{
name|ESTestCase
operator|.
name|TestAnalysis
name|analysis
init|=
name|AnalysisTestsHelper
operator|.
name|createTestAnalysisFromSettings
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|Environment
operator|.
name|PATH_HOME_SETTING
operator|.
name|getKey
argument_list|()
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
name|type
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
argument_list|,
operator|new
name|CommonAnalysisPlugin
argument_list|()
argument_list|)
decl_stmt|;
name|TokenFilterFactory
name|tokenFilter
init|=
name|analysis
operator|.
name|tokenFilter
operator|.
name|get
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
DECL|method|testCatenateNumbers
specifier|public
name|void
name|testCatenateNumbers
parameter_list|()
throws|throws
name|IOException
block|{
name|ESTestCase
operator|.
name|TestAnalysis
name|analysis
init|=
name|AnalysisTestsHelper
operator|.
name|createTestAnalysisFromSettings
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|Environment
operator|.
name|PATH_HOME_SETTING
operator|.
name|getKey
argument_list|()
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
name|type
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
argument_list|,
operator|new
name|CommonAnalysisPlugin
argument_list|()
argument_list|)
decl_stmt|;
name|TokenFilterFactory
name|tokenFilter
init|=
name|analysis
operator|.
name|tokenFilter
operator|.
name|get
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
DECL|method|testCatenateAll
specifier|public
name|void
name|testCatenateAll
parameter_list|()
throws|throws
name|IOException
block|{
name|ESTestCase
operator|.
name|TestAnalysis
name|analysis
init|=
name|AnalysisTestsHelper
operator|.
name|createTestAnalysisFromSettings
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|Environment
operator|.
name|PATH_HOME_SETTING
operator|.
name|getKey
argument_list|()
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
name|type
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
argument_list|,
operator|new
name|CommonAnalysisPlugin
argument_list|()
argument_list|)
decl_stmt|;
name|TokenFilterFactory
name|tokenFilter
init|=
name|analysis
operator|.
name|tokenFilter
operator|.
name|get
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
DECL|method|testSplitOnCaseChange
specifier|public
name|void
name|testSplitOnCaseChange
parameter_list|()
throws|throws
name|IOException
block|{
name|ESTestCase
operator|.
name|TestAnalysis
name|analysis
init|=
name|AnalysisTestsHelper
operator|.
name|createTestAnalysisFromSettings
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|Environment
operator|.
name|PATH_HOME_SETTING
operator|.
name|getKey
argument_list|()
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
name|type
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
argument_list|,
operator|new
name|CommonAnalysisPlugin
argument_list|()
argument_list|)
decl_stmt|;
name|TokenFilterFactory
name|tokenFilter
init|=
name|analysis
operator|.
name|tokenFilter
operator|.
name|get
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
DECL|method|testPreserveOriginal
specifier|public
name|void
name|testPreserveOriginal
parameter_list|()
throws|throws
name|IOException
block|{
name|ESTestCase
operator|.
name|TestAnalysis
name|analysis
init|=
name|AnalysisTestsHelper
operator|.
name|createTestAnalysisFromSettings
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|Environment
operator|.
name|PATH_HOME_SETTING
operator|.
name|getKey
argument_list|()
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
name|type
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
argument_list|,
operator|new
name|CommonAnalysisPlugin
argument_list|()
argument_list|)
decl_stmt|;
name|TokenFilterFactory
name|tokenFilter
init|=
name|analysis
operator|.
name|tokenFilter
operator|.
name|get
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
DECL|method|testStemEnglishPossessive
specifier|public
name|void
name|testStemEnglishPossessive
parameter_list|()
throws|throws
name|IOException
block|{
name|ESTestCase
operator|.
name|TestAnalysis
name|analysis
init|=
name|AnalysisTestsHelper
operator|.
name|createTestAnalysisFromSettings
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|Environment
operator|.
name|PATH_HOME_SETTING
operator|.
name|getKey
argument_list|()
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
name|type
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
argument_list|,
operator|new
name|CommonAnalysisPlugin
argument_list|()
argument_list|)
decl_stmt|;
name|TokenFilterFactory
name|tokenFilter
init|=
name|analysis
operator|.
name|tokenFilter
operator|.
name|get
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
block|}
end_class

end_unit

