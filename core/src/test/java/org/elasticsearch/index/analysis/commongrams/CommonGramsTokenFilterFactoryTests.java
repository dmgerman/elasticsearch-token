begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.analysis.commongrams
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|analysis
operator|.
name|commongrams
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
name|index
operator|.
name|analysis
operator|.
name|AnalysisService
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
name|ESTokenStreamTestCase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Assert
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
name|InputStream
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
import|import
name|java
operator|.
name|nio
operator|.
name|file
operator|.
name|Files
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|file
operator|.
name|Path
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
name|instanceOf
import|;
end_import

begin_class
DECL|class|CommonGramsTokenFilterFactoryTests
specifier|public
class|class
name|CommonGramsTokenFilterFactoryTests
extends|extends
name|ESTokenStreamTestCase
block|{
DECL|method|testDefault
specifier|public
name|void
name|testDefault
parameter_list|()
throws|throws
name|IOException
block|{
name|Settings
name|settings
init|=
name|Settings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"index.analysis.filter.common_grams_default.type"
argument_list|,
literal|"common_grams"
argument_list|)
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
name|build
argument_list|()
decl_stmt|;
try|try
block|{
name|AnalysisTestsHelper
operator|.
name|createAnalysisServiceFromSettings
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|fail
argument_list|(
literal|"[common_words] or [common_words_path] is set"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|e
operator|.
name|getCause
argument_list|()
argument_list|,
name|instanceOf
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testWithoutCommonWordsMatch
specifier|public
name|void
name|testWithoutCommonWordsMatch
parameter_list|()
throws|throws
name|IOException
block|{
block|{
name|Settings
name|settings
init|=
name|Settings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"index.analysis.filter.common_grams_default.type"
argument_list|,
literal|"common_grams"
argument_list|)
operator|.
name|putArray
argument_list|(
literal|"index.analysis.filter.common_grams_default.common_words"
argument_list|,
literal|"chromosome"
argument_list|,
literal|"protein"
argument_list|)
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
name|build
argument_list|()
decl_stmt|;
name|AnalysisService
name|analysisService
init|=
name|AnalysisTestsHelper
operator|.
name|createAnalysisServiceFromSettings
argument_list|(
name|settings
argument_list|)
decl_stmt|;
block|{
name|TokenFilterFactory
name|tokenFilter
init|=
name|analysisService
operator|.
name|tokenFilter
argument_list|(
literal|"common_grams_default"
argument_list|)
decl_stmt|;
name|String
name|source
init|=
literal|"the quick brown is a fox Or noT"
decl_stmt|;
name|String
index|[]
name|expected
init|=
operator|new
name|String
index|[]
block|{
literal|"the"
block|,
literal|"quick"
block|,
literal|"brown"
block|,
literal|"is"
block|,
literal|"a"
block|,
literal|"fox"
block|,
literal|"Or"
block|,
literal|"noT"
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
block|{
name|Settings
name|settings
init|=
name|Settings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"index.analysis.filter.common_grams_default.type"
argument_list|,
literal|"common_grams"
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.analysis.filter.common_grams_default.query_mode"
argument_list|,
literal|false
argument_list|)
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
name|putArray
argument_list|(
literal|"index.analysis.filter.common_grams_default.common_words"
argument_list|,
literal|"chromosome"
argument_list|,
literal|"protein"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|AnalysisService
name|analysisService
init|=
name|AnalysisTestsHelper
operator|.
name|createAnalysisServiceFromSettings
argument_list|(
name|settings
argument_list|)
decl_stmt|;
block|{
name|TokenFilterFactory
name|tokenFilter
init|=
name|analysisService
operator|.
name|tokenFilter
argument_list|(
literal|"common_grams_default"
argument_list|)
decl_stmt|;
name|String
name|source
init|=
literal|"the quick brown is a fox Or noT"
decl_stmt|;
name|String
index|[]
name|expected
init|=
operator|new
name|String
index|[]
block|{
literal|"the"
block|,
literal|"quick"
block|,
literal|"brown"
block|,
literal|"is"
block|,
literal|"a"
block|,
literal|"fox"
block|,
literal|"Or"
block|,
literal|"noT"
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
block|}
DECL|method|testSettings
specifier|public
name|void
name|testSettings
parameter_list|()
throws|throws
name|IOException
block|{
block|{
name|Settings
name|settings
init|=
name|Settings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"index.analysis.filter.common_grams_1.type"
argument_list|,
literal|"common_grams"
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.analysis.filter.common_grams_1.ignore_case"
argument_list|,
literal|true
argument_list|)
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
name|putArray
argument_list|(
literal|"index.analysis.filter.common_grams_1.common_words"
argument_list|,
literal|"the"
argument_list|,
literal|"Or"
argument_list|,
literal|"Not"
argument_list|,
literal|"a"
argument_list|,
literal|"is"
argument_list|,
literal|"an"
argument_list|,
literal|"they"
argument_list|,
literal|"are"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|AnalysisService
name|analysisService
init|=
name|AnalysisTestsHelper
operator|.
name|createAnalysisServiceFromSettings
argument_list|(
name|settings
argument_list|)
decl_stmt|;
name|TokenFilterFactory
name|tokenFilter
init|=
name|analysisService
operator|.
name|tokenFilter
argument_list|(
literal|"common_grams_1"
argument_list|)
decl_stmt|;
name|String
name|source
init|=
literal|"the quick brown is a fox or noT"
decl_stmt|;
name|String
index|[]
name|expected
init|=
operator|new
name|String
index|[]
block|{
literal|"the"
block|,
literal|"the_quick"
block|,
literal|"quick"
block|,
literal|"brown"
block|,
literal|"brown_is"
block|,
literal|"is"
block|,
literal|"is_a"
block|,
literal|"a"
block|,
literal|"a_fox"
block|,
literal|"fox"
block|,
literal|"fox_or"
block|,
literal|"or"
block|,
literal|"or_noT"
block|,
literal|"noT"
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
block|{
name|Settings
name|settings
init|=
name|Settings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"index.analysis.filter.common_grams_2.type"
argument_list|,
literal|"common_grams"
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.analysis.filter.common_grams_2.ignore_case"
argument_list|,
literal|false
argument_list|)
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
name|putArray
argument_list|(
literal|"index.analysis.filter.common_grams_2.common_words"
argument_list|,
literal|"the"
argument_list|,
literal|"Or"
argument_list|,
literal|"noT"
argument_list|,
literal|"a"
argument_list|,
literal|"is"
argument_list|,
literal|"an"
argument_list|,
literal|"they"
argument_list|,
literal|"are"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|AnalysisService
name|analysisService
init|=
name|AnalysisTestsHelper
operator|.
name|createAnalysisServiceFromSettings
argument_list|(
name|settings
argument_list|)
decl_stmt|;
name|TokenFilterFactory
name|tokenFilter
init|=
name|analysisService
operator|.
name|tokenFilter
argument_list|(
literal|"common_grams_2"
argument_list|)
decl_stmt|;
name|String
name|source
init|=
literal|"the quick brown is a fox or why noT"
decl_stmt|;
name|String
index|[]
name|expected
init|=
operator|new
name|String
index|[]
block|{
literal|"the"
block|,
literal|"the_quick"
block|,
literal|"quick"
block|,
literal|"brown"
block|,
literal|"brown_is"
block|,
literal|"is"
block|,
literal|"is_a"
block|,
literal|"a"
block|,
literal|"a_fox"
block|,
literal|"fox"
block|,
literal|"or"
block|,
literal|"why"
block|,
literal|"why_noT"
block|,
literal|"noT"
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
block|{
name|Settings
name|settings
init|=
name|Settings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"index.analysis.filter.common_grams_3.type"
argument_list|,
literal|"common_grams"
argument_list|)
operator|.
name|putArray
argument_list|(
literal|"index.analysis.filter.common_grams_3.common_words"
argument_list|,
literal|"the"
argument_list|,
literal|"or"
argument_list|,
literal|"not"
argument_list|,
literal|"a"
argument_list|,
literal|"is"
argument_list|,
literal|"an"
argument_list|,
literal|"they"
argument_list|,
literal|"are"
argument_list|)
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
name|build
argument_list|()
decl_stmt|;
name|AnalysisService
name|analysisService
init|=
name|AnalysisTestsHelper
operator|.
name|createAnalysisServiceFromSettings
argument_list|(
name|settings
argument_list|)
decl_stmt|;
name|TokenFilterFactory
name|tokenFilter
init|=
name|analysisService
operator|.
name|tokenFilter
argument_list|(
literal|"common_grams_3"
argument_list|)
decl_stmt|;
name|String
name|source
init|=
literal|"the quick brown is a fox Or noT"
decl_stmt|;
name|String
index|[]
name|expected
init|=
operator|new
name|String
index|[]
block|{
literal|"the"
block|,
literal|"the_quick"
block|,
literal|"quick"
block|,
literal|"brown"
block|,
literal|"brown_is"
block|,
literal|"is"
block|,
literal|"is_a"
block|,
literal|"a"
block|,
literal|"a_fox"
block|,
literal|"fox"
block|,
literal|"Or"
block|,
literal|"noT"
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
DECL|method|testCommonGramsAnalysis
specifier|public
name|void
name|testCommonGramsAnalysis
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|json
init|=
literal|"/org/elasticsearch/index/analysis/commongrams/commongrams.json"
decl_stmt|;
name|Settings
name|settings
init|=
name|Settings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|loadFromStream
argument_list|(
name|json
argument_list|,
name|getClass
argument_list|()
operator|.
name|getResourceAsStream
argument_list|(
name|json
argument_list|)
argument_list|)
operator|.
name|put
argument_list|(
literal|"path.home"
argument_list|,
name|createHome
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
block|{
name|AnalysisService
name|analysisService
init|=
name|AnalysisTestsHelper
operator|.
name|createAnalysisServiceFromSettings
argument_list|(
name|settings
argument_list|)
decl_stmt|;
name|Analyzer
name|analyzer
init|=
name|analysisService
operator|.
name|analyzer
argument_list|(
literal|"commongramsAnalyzer"
argument_list|)
operator|.
name|analyzer
argument_list|()
decl_stmt|;
name|String
name|source
init|=
literal|"the quick brown is a fox or not"
decl_stmt|;
name|String
index|[]
name|expected
init|=
operator|new
name|String
index|[]
block|{
literal|"the"
block|,
literal|"quick"
block|,
literal|"quick_brown"
block|,
literal|"brown"
block|,
literal|"brown_is"
block|,
literal|"is"
block|,
literal|"a"
block|,
literal|"a_fox"
block|,
literal|"fox"
block|,
literal|"fox_or"
block|,
literal|"or"
block|,
literal|"not"
block|}
decl_stmt|;
name|assertTokenStreamContents
argument_list|(
name|analyzer
operator|.
name|tokenStream
argument_list|(
literal|"test"
argument_list|,
name|source
argument_list|)
argument_list|,
name|expected
argument_list|)
expr_stmt|;
block|}
block|{
name|AnalysisService
name|analysisService
init|=
name|AnalysisTestsHelper
operator|.
name|createAnalysisServiceFromSettings
argument_list|(
name|settings
argument_list|)
decl_stmt|;
name|Analyzer
name|analyzer
init|=
name|analysisService
operator|.
name|analyzer
argument_list|(
literal|"commongramsAnalyzer_file"
argument_list|)
operator|.
name|analyzer
argument_list|()
decl_stmt|;
name|String
name|source
init|=
literal|"the quick brown is a fox or not"
decl_stmt|;
name|String
index|[]
name|expected
init|=
operator|new
name|String
index|[]
block|{
literal|"the"
block|,
literal|"quick"
block|,
literal|"quick_brown"
block|,
literal|"brown"
block|,
literal|"brown_is"
block|,
literal|"is"
block|,
literal|"a"
block|,
literal|"a_fox"
block|,
literal|"fox"
block|,
literal|"fox_or"
block|,
literal|"or"
block|,
literal|"not"
block|}
decl_stmt|;
name|assertTokenStreamContents
argument_list|(
name|analyzer
operator|.
name|tokenStream
argument_list|(
literal|"test"
argument_list|,
name|source
argument_list|)
argument_list|,
name|expected
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testQueryModeSettings
specifier|public
name|void
name|testQueryModeSettings
parameter_list|()
throws|throws
name|IOException
block|{
block|{
name|Settings
name|settings
init|=
name|Settings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"index.analysis.filter.common_grams_1.type"
argument_list|,
literal|"common_grams"
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.analysis.filter.common_grams_1.query_mode"
argument_list|,
literal|true
argument_list|)
operator|.
name|putArray
argument_list|(
literal|"index.analysis.filter.common_grams_1.common_words"
argument_list|,
literal|"the"
argument_list|,
literal|"Or"
argument_list|,
literal|"Not"
argument_list|,
literal|"a"
argument_list|,
literal|"is"
argument_list|,
literal|"an"
argument_list|,
literal|"they"
argument_list|,
literal|"are"
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.analysis.filter.common_grams_1.ignore_case"
argument_list|,
literal|true
argument_list|)
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
name|build
argument_list|()
decl_stmt|;
name|AnalysisService
name|analysisService
init|=
name|AnalysisTestsHelper
operator|.
name|createAnalysisServiceFromSettings
argument_list|(
name|settings
argument_list|)
decl_stmt|;
name|TokenFilterFactory
name|tokenFilter
init|=
name|analysisService
operator|.
name|tokenFilter
argument_list|(
literal|"common_grams_1"
argument_list|)
decl_stmt|;
name|String
name|source
init|=
literal|"the quick brown is a fox or noT"
decl_stmt|;
name|String
index|[]
name|expected
init|=
operator|new
name|String
index|[]
block|{
literal|"the_quick"
block|,
literal|"quick"
block|,
literal|"brown_is"
block|,
literal|"is_a"
block|,
literal|"a_fox"
block|,
literal|"fox_or"
block|,
literal|"or_noT"
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
block|{
name|Settings
name|settings
init|=
name|Settings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"index.analysis.filter.common_grams_2.type"
argument_list|,
literal|"common_grams"
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.analysis.filter.common_grams_2.query_mode"
argument_list|,
literal|true
argument_list|)
operator|.
name|putArray
argument_list|(
literal|"index.analysis.filter.common_grams_2.common_words"
argument_list|,
literal|"the"
argument_list|,
literal|"Or"
argument_list|,
literal|"noT"
argument_list|,
literal|"a"
argument_list|,
literal|"is"
argument_list|,
literal|"an"
argument_list|,
literal|"they"
argument_list|,
literal|"are"
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.analysis.filter.common_grams_2.ignore_case"
argument_list|,
literal|false
argument_list|)
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
name|build
argument_list|()
decl_stmt|;
name|AnalysisService
name|analysisService
init|=
name|AnalysisTestsHelper
operator|.
name|createAnalysisServiceFromSettings
argument_list|(
name|settings
argument_list|)
decl_stmt|;
name|TokenFilterFactory
name|tokenFilter
init|=
name|analysisService
operator|.
name|tokenFilter
argument_list|(
literal|"common_grams_2"
argument_list|)
decl_stmt|;
name|String
name|source
init|=
literal|"the quick brown is a fox or why noT"
decl_stmt|;
name|String
index|[]
name|expected
init|=
operator|new
name|String
index|[]
block|{
literal|"the_quick"
block|,
literal|"quick"
block|,
literal|"brown_is"
block|,
literal|"is_a"
block|,
literal|"a_fox"
block|,
literal|"fox"
block|,
literal|"or"
block|,
literal|"why_noT"
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
block|{
name|Settings
name|settings
init|=
name|Settings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"index.analysis.filter.common_grams_3.type"
argument_list|,
literal|"common_grams"
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.analysis.filter.common_grams_3.query_mode"
argument_list|,
literal|true
argument_list|)
operator|.
name|putArray
argument_list|(
literal|"index.analysis.filter.common_grams_3.common_words"
argument_list|,
literal|"the"
argument_list|,
literal|"Or"
argument_list|,
literal|"noT"
argument_list|,
literal|"a"
argument_list|,
literal|"is"
argument_list|,
literal|"an"
argument_list|,
literal|"they"
argument_list|,
literal|"are"
argument_list|)
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
name|build
argument_list|()
decl_stmt|;
name|AnalysisService
name|analysisService
init|=
name|AnalysisTestsHelper
operator|.
name|createAnalysisServiceFromSettings
argument_list|(
name|settings
argument_list|)
decl_stmt|;
name|TokenFilterFactory
name|tokenFilter
init|=
name|analysisService
operator|.
name|tokenFilter
argument_list|(
literal|"common_grams_3"
argument_list|)
decl_stmt|;
name|String
name|source
init|=
literal|"the quick brown is a fox or why noT"
decl_stmt|;
name|String
index|[]
name|expected
init|=
operator|new
name|String
index|[]
block|{
literal|"the_quick"
block|,
literal|"quick"
block|,
literal|"brown_is"
block|,
literal|"is_a"
block|,
literal|"a_fox"
block|,
literal|"fox"
block|,
literal|"or"
block|,
literal|"why_noT"
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
block|{
name|Settings
name|settings
init|=
name|Settings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"index.analysis.filter.common_grams_4.type"
argument_list|,
literal|"common_grams"
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.analysis.filter.common_grams_4.query_mode"
argument_list|,
literal|true
argument_list|)
operator|.
name|putArray
argument_list|(
literal|"index.analysis.filter.common_grams_4.common_words"
argument_list|,
literal|"the"
argument_list|,
literal|"or"
argument_list|,
literal|"not"
argument_list|,
literal|"a"
argument_list|,
literal|"is"
argument_list|,
literal|"an"
argument_list|,
literal|"they"
argument_list|,
literal|"are"
argument_list|)
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
name|build
argument_list|()
decl_stmt|;
name|AnalysisService
name|analysisService
init|=
name|AnalysisTestsHelper
operator|.
name|createAnalysisServiceFromSettings
argument_list|(
name|settings
argument_list|)
decl_stmt|;
name|TokenFilterFactory
name|tokenFilter
init|=
name|analysisService
operator|.
name|tokenFilter
argument_list|(
literal|"common_grams_4"
argument_list|)
decl_stmt|;
name|String
name|source
init|=
literal|"the quick brown is a fox Or noT"
decl_stmt|;
name|String
index|[]
name|expected
init|=
operator|new
name|String
index|[]
block|{
literal|"the_quick"
block|,
literal|"quick"
block|,
literal|"brown_is"
block|,
literal|"is_a"
block|,
literal|"a_fox"
block|,
literal|"fox"
block|,
literal|"Or"
block|,
literal|"noT"
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
DECL|method|testQueryModeCommonGramsAnalysis
specifier|public
name|void
name|testQueryModeCommonGramsAnalysis
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|json
init|=
literal|"/org/elasticsearch/index/analysis/commongrams/commongrams_query_mode.json"
decl_stmt|;
name|Settings
name|settings
init|=
name|Settings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|loadFromStream
argument_list|(
name|json
argument_list|,
name|getClass
argument_list|()
operator|.
name|getResourceAsStream
argument_list|(
name|json
argument_list|)
argument_list|)
operator|.
name|put
argument_list|(
literal|"path.home"
argument_list|,
name|createHome
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
block|{
name|AnalysisService
name|analysisService
init|=
name|AnalysisTestsHelper
operator|.
name|createAnalysisServiceFromSettings
argument_list|(
name|settings
argument_list|)
decl_stmt|;
name|Analyzer
name|analyzer
init|=
name|analysisService
operator|.
name|analyzer
argument_list|(
literal|"commongramsAnalyzer"
argument_list|)
operator|.
name|analyzer
argument_list|()
decl_stmt|;
name|String
name|source
init|=
literal|"the quick brown is a fox or not"
decl_stmt|;
name|String
index|[]
name|expected
init|=
operator|new
name|String
index|[]
block|{
literal|"the"
block|,
literal|"quick_brown"
block|,
literal|"brown_is"
block|,
literal|"is"
block|,
literal|"a_fox"
block|,
literal|"fox_or"
block|,
literal|"or"
block|,
literal|"not"
block|}
decl_stmt|;
name|assertTokenStreamContents
argument_list|(
name|analyzer
operator|.
name|tokenStream
argument_list|(
literal|"test"
argument_list|,
name|source
argument_list|)
argument_list|,
name|expected
argument_list|)
expr_stmt|;
block|}
block|{
name|AnalysisService
name|analysisService
init|=
name|AnalysisTestsHelper
operator|.
name|createAnalysisServiceFromSettings
argument_list|(
name|settings
argument_list|)
decl_stmt|;
name|Analyzer
name|analyzer
init|=
name|analysisService
operator|.
name|analyzer
argument_list|(
literal|"commongramsAnalyzer_file"
argument_list|)
operator|.
name|analyzer
argument_list|()
decl_stmt|;
name|String
name|source
init|=
literal|"the quick brown is a fox or not"
decl_stmt|;
name|String
index|[]
name|expected
init|=
operator|new
name|String
index|[]
block|{
literal|"the"
block|,
literal|"quick_brown"
block|,
literal|"brown_is"
block|,
literal|"is"
block|,
literal|"a_fox"
block|,
literal|"fox_or"
block|,
literal|"or"
block|,
literal|"not"
block|}
decl_stmt|;
name|assertTokenStreamContents
argument_list|(
name|analyzer
operator|.
name|tokenStream
argument_list|(
literal|"test"
argument_list|,
name|source
argument_list|)
argument_list|,
name|expected
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|createHome
specifier|private
name|Path
name|createHome
parameter_list|()
throws|throws
name|IOException
block|{
name|InputStream
name|words
init|=
name|getClass
argument_list|()
operator|.
name|getResourceAsStream
argument_list|(
literal|"common_words.txt"
argument_list|)
decl_stmt|;
name|Path
name|home
init|=
name|createTempDir
argument_list|()
decl_stmt|;
name|Path
name|config
init|=
name|home
operator|.
name|resolve
argument_list|(
literal|"config"
argument_list|)
decl_stmt|;
name|Files
operator|.
name|createDirectory
argument_list|(
name|config
argument_list|)
expr_stmt|;
name|Files
operator|.
name|copy
argument_list|(
name|words
argument_list|,
name|config
operator|.
name|resolve
argument_list|(
literal|"common_words.txt"
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|home
return|;
block|}
block|}
end_class

end_unit

