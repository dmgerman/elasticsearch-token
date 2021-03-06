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
name|MockTokenFilter
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
name|AnalysisModule
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
name|AnalysisModule
operator|.
name|AnalysisProvider
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
name|plugins
operator|.
name|AnalysisPlugin
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
name|Map
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|util
operator|.
name|Collections
operator|.
name|emptyMap
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|util
operator|.
name|Collections
operator|.
name|singletonList
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|util
operator|.
name|Collections
operator|.
name|singletonMap
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
DECL|class|AnalysisRegistryTests
specifier|public
class|class
name|AnalysisRegistryTests
extends|extends
name|ESTestCase
block|{
DECL|field|emptyRegistry
specifier|private
name|AnalysisRegistry
name|emptyRegistry
decl_stmt|;
DECL|method|analyzerProvider
specifier|private
specifier|static
name|AnalyzerProvider
argument_list|<
name|?
argument_list|>
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
DECL|method|emptyAnalysisRegistry
specifier|private
specifier|static
name|AnalysisRegistry
name|emptyAnalysisRegistry
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
return|return
operator|new
name|AnalysisRegistry
argument_list|(
operator|new
name|Environment
argument_list|(
name|settings
argument_list|)
argument_list|,
name|emptyMap
argument_list|()
argument_list|,
name|emptyMap
argument_list|()
argument_list|,
name|emptyMap
argument_list|()
argument_list|,
name|emptyMap
argument_list|()
argument_list|,
name|emptyMap
argument_list|()
argument_list|,
name|emptyMap
argument_list|()
argument_list|,
name|emptyMap
argument_list|()
argument_list|,
name|emptyMap
argument_list|()
argument_list|)
return|;
block|}
DECL|method|indexSettingsOfCurrentVersion
specifier|private
specifier|static
name|IndexSettings
name|indexSettingsOfCurrentVersion
parameter_list|(
name|Settings
operator|.
name|Builder
name|settings
parameter_list|)
block|{
return|return
name|IndexSettingsModule
operator|.
name|newIndexSettings
argument_list|(
literal|"index"
argument_list|,
name|settings
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
return|;
block|}
annotation|@
name|Override
DECL|method|setUp
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|setUp
argument_list|()
expr_stmt|;
name|emptyRegistry
operator|=
name|emptyAnalysisRegistry
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
name|build
argument_list|()
argument_list|)
expr_stmt|;
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
name|random
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
name|IndexAnalyzers
name|indexAnalyzers
init|=
name|emptyRegistry
operator|.
name|build
argument_list|(
name|idxSettings
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|indexAnalyzers
operator|.
name|getDefaultIndexAnalyzer
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
name|indexAnalyzers
operator|.
name|getDefaultSearchAnalyzer
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
name|indexAnalyzers
operator|.
name|getDefaultSearchQuoteAnalyzer
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
name|random
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
name|IndexAnalyzers
name|indexAnalyzers
init|=
name|emptyRegistry
operator|.
name|build
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
name|emptyMap
argument_list|()
argument_list|,
name|emptyMap
argument_list|()
argument_list|,
name|emptyMap
argument_list|()
argument_list|,
name|emptyMap
argument_list|()
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|indexAnalyzers
operator|.
name|getDefaultIndexAnalyzer
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
name|indexAnalyzers
operator|.
name|getDefaultSearchAnalyzer
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
name|indexAnalyzers
operator|.
name|getDefaultSearchQuoteAnalyzer
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
DECL|method|testOverrideDefaultIndexAnalyzerIsUnsupported
specifier|public
name|void
name|testOverrideDefaultIndexAnalyzerIsUnsupported
parameter_list|()
block|{
name|Version
name|version
init|=
name|VersionUtils
operator|.
name|randomVersionBetween
argument_list|(
name|random
argument_list|()
argument_list|,
name|Version
operator|.
name|V_5_0_0_alpha1
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
name|AnalyzerProvider
argument_list|<
name|?
argument_list|>
name|defaultIndex
init|=
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
decl_stmt|;
name|IllegalArgumentException
name|e
init|=
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|emptyRegistry
operator|.
name|build
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
name|singletonMap
argument_list|(
literal|"default_index"
argument_list|,
name|defaultIndex
argument_list|)
argument_list|,
name|emptyMap
argument_list|()
argument_list|,
name|emptyMap
argument_list|()
argument_list|,
name|emptyMap
argument_list|()
argument_list|,
name|emptyMap
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
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
name|random
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
name|IndexAnalyzers
name|indexAnalyzers
init|=
name|emptyRegistry
operator|.
name|build
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
name|emptyMap
argument_list|()
argument_list|,
name|emptyMap
argument_list|()
argument_list|,
name|emptyMap
argument_list|()
argument_list|,
name|emptyMap
argument_list|()
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|indexAnalyzers
operator|.
name|getDefaultIndexAnalyzer
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
name|indexAnalyzers
operator|.
name|getDefaultSearchAnalyzer
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
name|indexAnalyzers
operator|.
name|getDefaultSearchQuoteAnalyzer
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
comment|/**      * Tests that {@code camelCase} filter names and {@code snake_case} filter names don't collide.      */
DECL|method|testConfigureCamelCaseTokenFilter
specifier|public
name|void
name|testConfigureCamelCaseTokenFilter
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
name|Version
operator|.
name|CURRENT
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.analysis.filter.testFilter.type"
argument_list|,
literal|"mock"
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.analysis.filter.test_filter.type"
argument_list|,
literal|"mock"
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.analysis.analyzer.custom_analyzer_with_camel_case.tokenizer"
argument_list|,
literal|"standard"
argument_list|)
operator|.
name|putArray
argument_list|(
literal|"index.analysis.analyzer.custom_analyzer_with_camel_case.filter"
argument_list|,
literal|"lowercase"
argument_list|,
literal|"testFilter"
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.analysis.analyzer.custom_analyzer_with_snake_case.tokenizer"
argument_list|,
literal|"standard"
argument_list|)
operator|.
name|putArray
argument_list|(
literal|"index.analysis.analyzer.custom_analyzer_with_snake_case.filter"
argument_list|,
literal|"lowercase"
argument_list|,
literal|"test_filter"
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
comment|/* The snake_case version of the name should not filter out any stopwords while the          * camelCase version will filter out English stopwords. */
name|AnalysisPlugin
name|plugin
init|=
operator|new
name|AnalysisPlugin
argument_list|()
block|{
class|class
name|MockFactory
extends|extends
name|AbstractTokenFilterFactory
block|{
name|MockFactory
parameter_list|(
name|IndexSettings
name|indexSettings
parameter_list|,
name|Environment
name|env
parameter_list|,
name|String
name|name
parameter_list|,
name|Settings
name|settings
parameter_list|)
block|{
name|super
argument_list|(
name|indexSettings
argument_list|,
name|name
argument_list|,
name|settings
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|TokenStream
name|create
parameter_list|(
name|TokenStream
name|tokenStream
parameter_list|)
block|{
if|if
condition|(
name|name
argument_list|()
operator|.
name|equals
argument_list|(
literal|"test_filter"
argument_list|)
condition|)
block|{
return|return
operator|new
name|MockTokenFilter
argument_list|(
name|tokenStream
argument_list|,
name|MockTokenFilter
operator|.
name|EMPTY_STOPSET
argument_list|)
return|;
block|}
return|return
operator|new
name|MockTokenFilter
argument_list|(
name|tokenStream
argument_list|,
name|MockTokenFilter
operator|.
name|ENGLISH_STOPSET
argument_list|)
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|AnalysisProvider
argument_list|<
name|TokenFilterFactory
argument_list|>
argument_list|>
name|getTokenFilters
parameter_list|()
block|{
return|return
name|singletonMap
argument_list|(
literal|"mock"
argument_list|,
name|MockFactory
operator|::
operator|new
argument_list|)
return|;
block|}
block|}
decl_stmt|;
name|IndexAnalyzers
name|indexAnalyzers
init|=
operator|new
name|AnalysisModule
argument_list|(
operator|new
name|Environment
argument_list|(
name|settings
argument_list|)
argument_list|,
name|singletonList
argument_list|(
name|plugin
argument_list|)
argument_list|)
operator|.
name|getAnalysisRegistry
argument_list|()
operator|.
name|build
argument_list|(
name|idxSettings
argument_list|)
decl_stmt|;
comment|// This shouldn't contain English stopwords
try|try
init|(
name|NamedAnalyzer
name|custom_analyser
init|=
name|indexAnalyzers
operator|.
name|get
argument_list|(
literal|"custom_analyzer_with_camel_case"
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
literal|"has a foo"
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
name|assertTrue
argument_list|(
name|tokenStream
operator|.
name|incrementToken
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"has"
argument_list|,
name|charTermAttribute
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|tokenStream
operator|.
name|incrementToken
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"foo"
argument_list|,
name|charTermAttribute
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|tokenStream
operator|.
name|incrementToken
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// This *should* contain English stopwords
try|try
init|(
name|NamedAnalyzer
name|custom_analyser
init|=
name|indexAnalyzers
operator|.
name|get
argument_list|(
literal|"custom_analyzer_with_snake_case"
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
literal|"has a foo"
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
name|assertTrue
argument_list|(
name|tokenStream
operator|.
name|incrementToken
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"has"
argument_list|,
name|charTermAttribute
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|tokenStream
operator|.
name|incrementToken
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"a"
argument_list|,
name|charTermAttribute
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|tokenStream
operator|.
name|incrementToken
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"foo"
argument_list|,
name|charTermAttribute
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|tokenStream
operator|.
name|incrementToken
argument_list|()
argument_list|)
expr_stmt|;
block|}
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
name|IndexAnalyzers
name|indexAnalyzers
init|=
name|emptyAnalysisRegistry
argument_list|(
name|settings
argument_list|)
operator|.
name|build
argument_list|(
name|idxSettings
argument_list|)
decl_stmt|;
name|IndexAnalyzers
name|otherIndexAnalyzers
init|=
name|emptyAnalysisRegistry
argument_list|(
name|settings
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
name|indexAnalyzers
operator|.
name|get
argument_list|(
name|preBuiltAnalyzers
operator|.
name|name
argument_list|()
argument_list|)
argument_list|,
name|otherIndexAnalyzers
operator|.
name|get
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
DECL|method|testNoTypeOrTokenizerErrorMessage
specifier|public
name|void
name|testNoTypeOrTokenizerErrorMessage
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
name|random
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
name|putArray
argument_list|(
literal|"index.analysis.analyzer.test_analyzer.filter"
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"lowercase"
block|,
literal|"stop"
block|,
literal|"shingle"
block|}
argument_list|)
operator|.
name|putArray
argument_list|(
literal|"index.analysis.analyzer.test_analyzer.char_filter"
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"html_strip"
block|}
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
name|IllegalArgumentException
name|e
init|=
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|emptyAnalysisRegistry
argument_list|(
name|settings
argument_list|)
operator|.
name|build
argument_list|(
name|idxSettings
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"analyzer [test_analyzer] must specify either an analyzer type, or a tokenizer"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testCloseIndexAnalyzersMultipleTimes
specifier|public
name|void
name|testCloseIndexAnalyzersMultipleTimes
parameter_list|()
throws|throws
name|IOException
block|{
name|IndexAnalyzers
name|indexAnalyzers
init|=
name|emptyRegistry
operator|.
name|build
argument_list|(
name|indexSettingsOfCurrentVersion
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|indexAnalyzers
operator|.
name|close
argument_list|()
expr_stmt|;
name|indexAnalyzers
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

