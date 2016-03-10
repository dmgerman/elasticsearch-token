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
name|com
operator|.
name|carrotsearch
operator|.
name|randomizedtesting
operator|.
name|generators
operator|.
name|RandomPicks
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
name|en
operator|.
name|EnglishAnalyzer
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
name|standard
operator|.
name|StandardAnalyzer
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
name|Version
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|metadata
operator|.
name|IndexMetaData
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
name|IndexSettings
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|analysis
operator|.
name|PreBuiltAnalyzers
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
name|IndexSettingsModule
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
name|VersionUtils
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
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
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
name|Settings
operator|.
name|settingsBuilder
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
DECL|class|AnalysisServiceTests
specifier|public
class|class
name|AnalysisServiceTests
extends|extends
name|ESTestCase
block|{
DECL|method|analyzerProvider
specifier|private
specifier|static
name|AnalyzerProvider
name|analyzerProvider
parameter_list|(
specifier|final
name|String
name|name
parameter_list|)
block|{
return|return
operator|new
name|PreBuiltAnalyzerProvider
argument_list|(
name|name
argument_list|,
name|AnalyzerScope
operator|.
name|INDEX
argument_list|,
operator|new
name|EnglishAnalyzer
argument_list|()
argument_list|)
return|;
block|}
DECL|method|testDefaultAnalyzers
specifier|public
name|void
name|testDefaultAnalyzers
parameter_list|()
throws|throws
name|IOException
block|{
name|Version
name|version
init|=
name|VersionUtils
operator|.
name|randomVersion
argument_list|(
name|getRandom
argument_list|()
argument_list|)
decl_stmt|;
name|Settings
name|settings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_VERSION_CREATED
argument_list|,
name|version
argument_list|)
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
name|build
argument_list|()
decl_stmt|;
name|IndexSettings
name|idxSettings
init|=
name|IndexSettingsModule
operator|.
name|newIndexSettings
argument_list|(
literal|"index"
argument_list|,
name|settings
argument_list|)
decl_stmt|;
name|AnalysisService
name|analysisService
init|=
operator|new
name|AnalysisRegistry
argument_list|(
literal|null
argument_list|,
operator|new
name|Environment
argument_list|(
name|settings
argument_list|)
argument_list|)
operator|.
name|build
argument_list|(
name|idxSettings
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|analysisService
operator|.
name|defaultIndexAnalyzer
argument_list|()
operator|.
name|analyzer
argument_list|()
argument_list|,
name|instanceOf
argument_list|(
name|StandardAnalyzer
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|analysisService
operator|.
name|defaultSearchAnalyzer
argument_list|()
operator|.
name|analyzer
argument_list|()
argument_list|,
name|instanceOf
argument_list|(
name|StandardAnalyzer
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|analysisService
operator|.
name|defaultSearchQuoteAnalyzer
argument_list|()
operator|.
name|analyzer
argument_list|()
argument_list|,
name|instanceOf
argument_list|(
name|StandardAnalyzer
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testOverrideDefaultAnalyzer
specifier|public
name|void
name|testOverrideDefaultAnalyzer
parameter_list|()
throws|throws
name|IOException
block|{
name|Version
name|version
init|=
name|VersionUtils
operator|.
name|randomVersion
argument_list|(
name|getRandom
argument_list|()
argument_list|)
decl_stmt|;
name|Settings
name|settings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_VERSION_CREATED
argument_list|,
name|version
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|AnalysisService
name|analysisService
init|=
operator|new
name|AnalysisService
argument_list|(
name|IndexSettingsModule
operator|.
name|newIndexSettings
argument_list|(
literal|"index"
argument_list|,
name|settings
argument_list|)
argument_list|,
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"default"
argument_list|,
name|analyzerProvider
argument_list|(
literal|"default"
argument_list|)
argument_list|)
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|analysisService
operator|.
name|defaultIndexAnalyzer
argument_list|()
operator|.
name|analyzer
argument_list|()
argument_list|,
name|instanceOf
argument_list|(
name|EnglishAnalyzer
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|analysisService
operator|.
name|defaultSearchAnalyzer
argument_list|()
operator|.
name|analyzer
argument_list|()
argument_list|,
name|instanceOf
argument_list|(
name|EnglishAnalyzer
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|analysisService
operator|.
name|defaultSearchQuoteAnalyzer
argument_list|()
operator|.
name|analyzer
argument_list|()
argument_list|,
name|instanceOf
argument_list|(
name|EnglishAnalyzer
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testOverrideDefaultIndexAnalyzer
specifier|public
name|void
name|testOverrideDefaultIndexAnalyzer
parameter_list|()
block|{
name|Version
name|version
init|=
name|VersionUtils
operator|.
name|randomVersionBetween
argument_list|(
name|getRandom
argument_list|()
argument_list|,
name|Version
operator|.
name|V_5_0_0
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
decl_stmt|;
name|Settings
name|settings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_VERSION_CREATED
argument_list|,
name|version
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
try|try
block|{
name|AnalysisService
name|analysisService
init|=
operator|new
name|AnalysisService
argument_list|(
name|IndexSettingsModule
operator|.
name|newIndexSettings
argument_list|(
literal|"index"
argument_list|,
name|settings
argument_list|)
argument_list|,
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"default_index"
argument_list|,
operator|new
name|PreBuiltAnalyzerProvider
argument_list|(
literal|"default_index"
argument_list|,
name|AnalyzerScope
operator|.
name|INDEX
argument_list|,
operator|new
name|EnglishAnalyzer
argument_list|()
argument_list|)
argument_list|)
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|)
decl_stmt|;
name|fail
argument_list|(
literal|"Expected ISE"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
comment|// expected
name|assertTrue
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"[index.analysis.analyzer.default_index] is not supported"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testBackCompatOverrideDefaultIndexAnalyzer
specifier|public
name|void
name|testBackCompatOverrideDefaultIndexAnalyzer
parameter_list|()
block|{
name|Version
name|version
init|=
name|VersionUtils
operator|.
name|randomVersionBetween
argument_list|(
name|getRandom
argument_list|()
argument_list|,
name|VersionUtils
operator|.
name|getFirstVersion
argument_list|()
argument_list|,
name|VersionUtils
operator|.
name|getPreviousVersion
argument_list|(
name|Version
operator|.
name|V_5_0_0
argument_list|)
argument_list|)
decl_stmt|;
name|Settings
name|settings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_VERSION_CREATED
argument_list|,
name|version
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|AnalysisService
name|analysisService
init|=
operator|new
name|AnalysisService
argument_list|(
name|IndexSettingsModule
operator|.
name|newIndexSettings
argument_list|(
literal|"index"
argument_list|,
name|settings
argument_list|)
argument_list|,
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"default_index"
argument_list|,
name|analyzerProvider
argument_list|(
literal|"default_index"
argument_list|)
argument_list|)
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|analysisService
operator|.
name|defaultIndexAnalyzer
argument_list|()
operator|.
name|analyzer
argument_list|()
argument_list|,
name|instanceOf
argument_list|(
name|EnglishAnalyzer
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|analysisService
operator|.
name|defaultSearchAnalyzer
argument_list|()
operator|.
name|analyzer
argument_list|()
argument_list|,
name|instanceOf
argument_list|(
name|StandardAnalyzer
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|analysisService
operator|.
name|defaultSearchQuoteAnalyzer
argument_list|()
operator|.
name|analyzer
argument_list|()
argument_list|,
name|instanceOf
argument_list|(
name|StandardAnalyzer
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testOverrideDefaultSearchAnalyzer
specifier|public
name|void
name|testOverrideDefaultSearchAnalyzer
parameter_list|()
block|{
name|Version
name|version
init|=
name|VersionUtils
operator|.
name|randomVersion
argument_list|(
name|getRandom
argument_list|()
argument_list|)
decl_stmt|;
name|Settings
name|settings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_VERSION_CREATED
argument_list|,
name|version
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|AnalysisService
name|analysisService
init|=
operator|new
name|AnalysisService
argument_list|(
name|IndexSettingsModule
operator|.
name|newIndexSettings
argument_list|(
literal|"index"
argument_list|,
name|settings
argument_list|)
argument_list|,
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"default_search"
argument_list|,
name|analyzerProvider
argument_list|(
literal|"default_search"
argument_list|)
argument_list|)
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|analysisService
operator|.
name|defaultIndexAnalyzer
argument_list|()
operator|.
name|analyzer
argument_list|()
argument_list|,
name|instanceOf
argument_list|(
name|StandardAnalyzer
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|analysisService
operator|.
name|defaultSearchAnalyzer
argument_list|()
operator|.
name|analyzer
argument_list|()
argument_list|,
name|instanceOf
argument_list|(
name|EnglishAnalyzer
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|analysisService
operator|.
name|defaultSearchQuoteAnalyzer
argument_list|()
operator|.
name|analyzer
argument_list|()
argument_list|,
name|instanceOf
argument_list|(
name|EnglishAnalyzer
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testBackCompatOverrideDefaultIndexAndSearchAnalyzer
specifier|public
name|void
name|testBackCompatOverrideDefaultIndexAndSearchAnalyzer
parameter_list|()
block|{
name|Version
name|version
init|=
name|VersionUtils
operator|.
name|randomVersionBetween
argument_list|(
name|getRandom
argument_list|()
argument_list|,
name|VersionUtils
operator|.
name|getFirstVersion
argument_list|()
argument_list|,
name|VersionUtils
operator|.
name|getPreviousVersion
argument_list|(
name|Version
operator|.
name|V_5_0_0
argument_list|)
argument_list|)
decl_stmt|;
name|Settings
name|settings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_VERSION_CREATED
argument_list|,
name|version
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|AnalyzerProvider
argument_list|>
name|analyzers
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|analyzers
operator|.
name|put
argument_list|(
literal|"default_index"
argument_list|,
name|analyzerProvider
argument_list|(
literal|"default_index"
argument_list|)
argument_list|)
expr_stmt|;
name|analyzers
operator|.
name|put
argument_list|(
literal|"default_search"
argument_list|,
name|analyzerProvider
argument_list|(
literal|"default_search"
argument_list|)
argument_list|)
expr_stmt|;
name|AnalysisService
name|analysisService
init|=
operator|new
name|AnalysisService
argument_list|(
name|IndexSettingsModule
operator|.
name|newIndexSettings
argument_list|(
literal|"index"
argument_list|,
name|settings
argument_list|)
argument_list|,
name|analyzers
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|analysisService
operator|.
name|defaultIndexAnalyzer
argument_list|()
operator|.
name|analyzer
argument_list|()
argument_list|,
name|instanceOf
argument_list|(
name|EnglishAnalyzer
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|analysisService
operator|.
name|defaultSearchAnalyzer
argument_list|()
operator|.
name|analyzer
argument_list|()
argument_list|,
name|instanceOf
argument_list|(
name|EnglishAnalyzer
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|analysisService
operator|.
name|defaultSearchQuoteAnalyzer
argument_list|()
operator|.
name|analyzer
argument_list|()
argument_list|,
name|instanceOf
argument_list|(
name|EnglishAnalyzer
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testConfigureCamelCaseTokenFilter
specifier|public
name|void
name|testConfigureCamelCaseTokenFilter
parameter_list|()
throws|throws
name|IOException
block|{
comment|// tests a filter that
name|Settings
name|settings
init|=
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
name|build
argument_list|()
decl_stmt|;
name|Settings
name|indexSettings
init|=
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_VERSION_CREATED
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.analysis.filter.wordDelimiter.type"
argument_list|,
literal|"word_delimiter"
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.analysis.filter.wordDelimiter.split_on_numerics"
argument_list|,
literal|false
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.analysis.analyzer.custom_analyzer.tokenizer"
argument_list|,
literal|"whitespace"
argument_list|)
operator|.
name|putArray
argument_list|(
literal|"index.analysis.analyzer.custom_analyzer.filter"
argument_list|,
literal|"lowercase"
argument_list|,
literal|"wordDelimiter"
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.analysis.analyzer.custom_analyzer_1.tokenizer"
argument_list|,
literal|"whitespace"
argument_list|)
operator|.
name|putArray
argument_list|(
literal|"index.analysis.analyzer.custom_analyzer_1.filter"
argument_list|,
literal|"lowercase"
argument_list|,
literal|"word_delimiter"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|IndexSettings
name|idxSettings
init|=
name|IndexSettingsModule
operator|.
name|newIndexSettings
argument_list|(
literal|"index"
argument_list|,
name|indexSettings
argument_list|)
decl_stmt|;
name|AnalysisService
name|analysisService
init|=
operator|new
name|AnalysisRegistry
argument_list|(
literal|null
argument_list|,
operator|new
name|Environment
argument_list|(
name|settings
argument_list|)
argument_list|)
operator|.
name|build
argument_list|(
name|idxSettings
argument_list|)
decl_stmt|;
try|try
init|(
name|NamedAnalyzer
name|custom_analyser
init|=
name|analysisService
operator|.
name|analyzer
argument_list|(
literal|"custom_analyzer"
argument_list|)
init|)
block|{
name|assertNotNull
argument_list|(
name|custom_analyser
argument_list|)
expr_stmt|;
name|TokenStream
name|tokenStream
init|=
name|custom_analyser
operator|.
name|tokenStream
argument_list|(
literal|"foo"
argument_list|,
literal|"J2SE j2ee"
argument_list|)
decl_stmt|;
name|tokenStream
operator|.
name|reset
argument_list|()
expr_stmt|;
name|CharTermAttribute
name|charTermAttribute
init|=
name|tokenStream
operator|.
name|addAttribute
argument_list|(
name|CharTermAttribute
operator|.
name|class
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|token
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
while|while
condition|(
name|tokenStream
operator|.
name|incrementToken
argument_list|()
condition|)
block|{
name|token
operator|.
name|add
argument_list|(
name|charTermAttribute
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|token
operator|.
name|toString
argument_list|()
argument_list|,
literal|2
argument_list|,
name|token
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"j2se"
argument_list|,
name|token
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"j2ee"
argument_list|,
name|token
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
try|try
init|(
name|NamedAnalyzer
name|custom_analyser
init|=
name|analysisService
operator|.
name|analyzer
argument_list|(
literal|"custom_analyzer_1"
argument_list|)
init|)
block|{
name|assertNotNull
argument_list|(
name|custom_analyser
argument_list|)
expr_stmt|;
name|TokenStream
name|tokenStream
init|=
name|custom_analyser
operator|.
name|tokenStream
argument_list|(
literal|"foo"
argument_list|,
literal|"J2SE j2ee"
argument_list|)
decl_stmt|;
name|tokenStream
operator|.
name|reset
argument_list|()
expr_stmt|;
name|CharTermAttribute
name|charTermAttribute
init|=
name|tokenStream
operator|.
name|addAttribute
argument_list|(
name|CharTermAttribute
operator|.
name|class
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|token
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
while|while
condition|(
name|tokenStream
operator|.
name|incrementToken
argument_list|()
condition|)
block|{
name|token
operator|.
name|add
argument_list|(
name|charTermAttribute
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|token
operator|.
name|toString
argument_list|()
argument_list|,
literal|6
argument_list|,
name|token
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"j"
argument_list|,
name|token
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"2"
argument_list|,
name|token
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"se"
argument_list|,
name|token
operator|.
name|get
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"j"
argument_list|,
name|token
operator|.
name|get
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"2"
argument_list|,
name|token
operator|.
name|get
argument_list|(
literal|4
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"ee"
argument_list|,
name|token
operator|.
name|get
argument_list|(
literal|5
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testCameCaseOverride
specifier|public
name|void
name|testCameCaseOverride
parameter_list|()
throws|throws
name|IOException
block|{
name|Settings
name|settings
init|=
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
name|build
argument_list|()
decl_stmt|;
name|Settings
name|indexSettings
init|=
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_VERSION_CREATED
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.analysis.filter.wordDelimiter.type"
argument_list|,
literal|"word_delimiter"
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.analysis.filter.wordDelimiter.split_on_numerics"
argument_list|,
literal|false
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.analysis.analyzer.custom_analyzer.tokenizer"
argument_list|,
literal|"whitespace"
argument_list|)
operator|.
name|putArray
argument_list|(
literal|"index.analysis.analyzer.custom_analyzer.filter"
argument_list|,
literal|"lowercase"
argument_list|,
literal|"wordDelimiter"
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.analysis.analyzer.custom_analyzer_1.tokenizer"
argument_list|,
literal|"whitespace"
argument_list|)
operator|.
name|putArray
argument_list|(
literal|"index.analysis.analyzer.custom_analyzer_1.filter"
argument_list|,
literal|"lowercase"
argument_list|,
literal|"word_delimiter"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|IndexSettings
name|idxSettings
init|=
name|IndexSettingsModule
operator|.
name|newIndexSettings
argument_list|(
literal|"index"
argument_list|,
name|indexSettings
argument_list|)
decl_stmt|;
name|AnalysisService
name|analysisService
init|=
operator|new
name|AnalysisRegistry
argument_list|(
literal|null
argument_list|,
operator|new
name|Environment
argument_list|(
name|settings
argument_list|)
argument_list|)
operator|.
name|build
argument_list|(
name|idxSettings
argument_list|)
decl_stmt|;
name|TokenFilterFactory
name|word_delimiter
init|=
name|analysisService
operator|.
name|tokenFilter
argument_list|(
literal|"word_delimiter"
argument_list|)
decl_stmt|;
name|TokenFilterFactory
name|override
init|=
name|analysisService
operator|.
name|tokenFilter
argument_list|(
literal|"wordDelimiter"
argument_list|)
decl_stmt|;
name|assertNotEquals
argument_list|(
name|word_delimiter
operator|.
name|name
argument_list|()
argument_list|,
name|override
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
name|assertNotSame
argument_list|(
name|analysisService
operator|.
name|tokenFilter
argument_list|(
literal|"wordDelimiter"
argument_list|)
argument_list|,
name|analysisService
operator|.
name|tokenFilter
argument_list|(
literal|"word_delimiter"
argument_list|)
argument_list|)
expr_stmt|;
name|assertSame
argument_list|(
name|analysisService
operator|.
name|tokenFilter
argument_list|(
literal|"porterStem"
argument_list|)
argument_list|,
name|analysisService
operator|.
name|tokenFilter
argument_list|(
literal|"porter_stem"
argument_list|)
argument_list|)
expr_stmt|;
comment|//unconfigured
name|IndexSettings
name|idxSettings1
init|=
name|IndexSettingsModule
operator|.
name|newIndexSettings
argument_list|(
literal|"index"
argument_list|,
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_VERSION_CREATED
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|AnalysisService
name|analysisService1
init|=
operator|new
name|AnalysisRegistry
argument_list|(
literal|null
argument_list|,
operator|new
name|Environment
argument_list|(
name|settings
argument_list|)
argument_list|)
operator|.
name|build
argument_list|(
name|idxSettings1
argument_list|)
decl_stmt|;
name|assertSame
argument_list|(
name|analysisService1
operator|.
name|tokenFilter
argument_list|(
literal|"wordDelimiter"
argument_list|)
argument_list|,
name|analysisService1
operator|.
name|tokenFilter
argument_list|(
literal|"word_delimiter"
argument_list|)
argument_list|)
expr_stmt|;
name|assertSame
argument_list|(
name|analysisService1
operator|.
name|tokenFilter
argument_list|(
literal|"porterStem"
argument_list|)
argument_list|,
name|analysisService1
operator|.
name|tokenFilter
argument_list|(
literal|"porter_stem"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testBuiltInAnalyzersAreCached
specifier|public
name|void
name|testBuiltInAnalyzersAreCached
parameter_list|()
throws|throws
name|IOException
block|{
name|Settings
name|settings
init|=
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
name|build
argument_list|()
decl_stmt|;
name|Settings
name|indexSettings
init|=
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_VERSION_CREATED
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|IndexSettings
name|idxSettings
init|=
name|IndexSettingsModule
operator|.
name|newIndexSettings
argument_list|(
literal|"index"
argument_list|,
name|indexSettings
argument_list|)
decl_stmt|;
name|AnalysisService
name|analysisService
init|=
operator|new
name|AnalysisRegistry
argument_list|(
literal|null
argument_list|,
operator|new
name|Environment
argument_list|(
name|settings
argument_list|)
argument_list|)
operator|.
name|build
argument_list|(
name|idxSettings
argument_list|)
decl_stmt|;
name|AnalysisService
name|otherAnalysisSergice
init|=
operator|new
name|AnalysisRegistry
argument_list|(
literal|null
argument_list|,
operator|new
name|Environment
argument_list|(
name|settings
argument_list|)
argument_list|)
operator|.
name|build
argument_list|(
name|idxSettings
argument_list|)
decl_stmt|;
specifier|final
name|int
name|numIters
init|=
name|randomIntBetween
argument_list|(
literal|5
argument_list|,
literal|20
argument_list|)
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
name|numIters
condition|;
name|i
operator|++
control|)
block|{
name|PreBuiltAnalyzers
name|preBuiltAnalyzers
init|=
name|RandomPicks
operator|.
name|randomFrom
argument_list|(
name|random
argument_list|()
argument_list|,
name|PreBuiltAnalyzers
operator|.
name|values
argument_list|()
argument_list|)
decl_stmt|;
name|assertSame
argument_list|(
name|analysisService
operator|.
name|analyzer
argument_list|(
name|preBuiltAnalyzers
operator|.
name|name
argument_list|()
argument_list|)
argument_list|,
name|otherAnalysisSergice
operator|.
name|analyzer
argument_list|(
name|preBuiltAnalyzers
operator|.
name|name
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit
