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
name|inject
operator|.
name|Injector
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
name|inject
operator|.
name|ModulesBuilder
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
name|lucene
operator|.
name|all
operator|.
name|AllEntries
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
name|lucene
operator|.
name|all
operator|.
name|AllTokenStream
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
name|common
operator|.
name|settings
operator|.
name|SettingsModule
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
name|env
operator|.
name|EnvironmentModule
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
name|Index
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
name|IndexNameModule
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
name|compound
operator|.
name|DictionaryCompoundWordTokenFilterFactory
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
name|settings
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
name|indices
operator|.
name|analysis
operator|.
name|IndicesAnalysisModule
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
name|IndicesAnalysisService
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
name|ElasticsearchTestCase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|hamcrest
operator|.
name|MatcherAssert
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
name|List
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

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|CompoundAnalysisTests
specifier|public
class|class
name|CompoundAnalysisTests
extends|extends
name|ElasticsearchTestCase
block|{
annotation|@
name|Test
DECL|method|testDefaultsCompoundAnalysis
specifier|public
name|void
name|testDefaultsCompoundAnalysis
parameter_list|()
throws|throws
name|Exception
block|{
name|Index
name|index
init|=
operator|new
name|Index
argument_list|(
literal|"test"
argument_list|)
decl_stmt|;
name|Settings
name|settings
init|=
name|getJsonSettings
argument_list|()
decl_stmt|;
name|Injector
name|parentInjector
init|=
operator|new
name|ModulesBuilder
argument_list|()
operator|.
name|add
argument_list|(
operator|new
name|SettingsModule
argument_list|(
name|settings
argument_list|)
argument_list|,
operator|new
name|EnvironmentModule
argument_list|(
operator|new
name|Environment
argument_list|(
name|settings
argument_list|)
argument_list|)
argument_list|,
operator|new
name|IndicesAnalysisModule
argument_list|()
argument_list|)
operator|.
name|createInjector
argument_list|()
decl_stmt|;
name|Injector
name|injector
init|=
operator|new
name|ModulesBuilder
argument_list|()
operator|.
name|add
argument_list|(
operator|new
name|IndexSettingsModule
argument_list|(
name|index
argument_list|,
name|settings
argument_list|)
argument_list|,
operator|new
name|IndexNameModule
argument_list|(
name|index
argument_list|)
argument_list|,
operator|new
name|AnalysisModule
argument_list|(
name|settings
argument_list|,
name|parentInjector
operator|.
name|getInstance
argument_list|(
name|IndicesAnalysisService
operator|.
name|class
argument_list|)
argument_list|)
argument_list|)
operator|.
name|createChildInjector
argument_list|(
name|parentInjector
argument_list|)
decl_stmt|;
name|AnalysisService
name|analysisService
init|=
name|injector
operator|.
name|getInstance
argument_list|(
name|AnalysisService
operator|.
name|class
argument_list|)
decl_stmt|;
name|TokenFilterFactory
name|filterFactory
init|=
name|analysisService
operator|.
name|tokenFilter
argument_list|(
literal|"dict_dec"
argument_list|)
decl_stmt|;
name|MatcherAssert
operator|.
name|assertThat
argument_list|(
name|filterFactory
argument_list|,
name|instanceOf
argument_list|(
name|DictionaryCompoundWordTokenFilterFactory
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testDictionaryDecompounder
specifier|public
name|void
name|testDictionaryDecompounder
parameter_list|()
throws|throws
name|Exception
block|{
name|Settings
index|[]
name|settingsArr
init|=
operator|new
name|Settings
index|[]
block|{
name|getJsonSettings
argument_list|()
block|,
name|getYamlSettings
argument_list|()
block|}
decl_stmt|;
for|for
control|(
name|Settings
name|settings
range|:
name|settingsArr
control|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|terms
init|=
name|analyze
argument_list|(
name|settings
argument_list|,
literal|"decompoundingAnalyzer"
argument_list|,
literal|"donaudampfschiff spargelcremesuppe"
argument_list|)
decl_stmt|;
name|MatcherAssert
operator|.
name|assertThat
argument_list|(
name|terms
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|8
argument_list|)
argument_list|)
expr_stmt|;
name|MatcherAssert
operator|.
name|assertThat
argument_list|(
name|terms
argument_list|,
name|hasItems
argument_list|(
literal|"donau"
argument_list|,
literal|"dampf"
argument_list|,
literal|"schiff"
argument_list|,
literal|"donaudampfschiff"
argument_list|,
literal|"spargel"
argument_list|,
literal|"creme"
argument_list|,
literal|"suppe"
argument_list|,
literal|"spargelcremesuppe"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|analyze
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|analyze
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|String
name|analyzerName
parameter_list|,
name|String
name|text
parameter_list|)
throws|throws
name|IOException
block|{
name|Index
name|index
init|=
operator|new
name|Index
argument_list|(
literal|"test"
argument_list|)
decl_stmt|;
name|Injector
name|parentInjector
init|=
operator|new
name|ModulesBuilder
argument_list|()
operator|.
name|add
argument_list|(
operator|new
name|SettingsModule
argument_list|(
name|settings
argument_list|)
argument_list|,
operator|new
name|EnvironmentModule
argument_list|(
operator|new
name|Environment
argument_list|(
name|settings
argument_list|)
argument_list|)
argument_list|,
operator|new
name|IndicesAnalysisModule
argument_list|()
argument_list|)
operator|.
name|createInjector
argument_list|()
decl_stmt|;
name|Injector
name|injector
init|=
operator|new
name|ModulesBuilder
argument_list|()
operator|.
name|add
argument_list|(
operator|new
name|IndexSettingsModule
argument_list|(
name|index
argument_list|,
name|settings
argument_list|)
argument_list|,
operator|new
name|IndexNameModule
argument_list|(
name|index
argument_list|)
argument_list|,
operator|new
name|AnalysisModule
argument_list|(
name|settings
argument_list|,
name|parentInjector
operator|.
name|getInstance
argument_list|(
name|IndicesAnalysisService
operator|.
name|class
argument_list|)
argument_list|)
argument_list|)
operator|.
name|createChildInjector
argument_list|(
name|parentInjector
argument_list|)
decl_stmt|;
name|AnalysisService
name|analysisService
init|=
name|injector
operator|.
name|getInstance
argument_list|(
name|AnalysisService
operator|.
name|class
argument_list|)
decl_stmt|;
name|Analyzer
name|analyzer
init|=
name|analysisService
operator|.
name|analyzer
argument_list|(
name|analyzerName
argument_list|)
operator|.
name|analyzer
argument_list|()
decl_stmt|;
name|AllEntries
name|allEntries
init|=
operator|new
name|AllEntries
argument_list|()
decl_stmt|;
name|allEntries
operator|.
name|addText
argument_list|(
literal|"field1"
argument_list|,
name|text
argument_list|,
literal|1.0f
argument_list|)
expr_stmt|;
name|allEntries
operator|.
name|reset
argument_list|()
expr_stmt|;
name|TokenStream
name|stream
init|=
name|AllTokenStream
operator|.
name|allTokenStream
argument_list|(
literal|"_all"
argument_list|,
name|allEntries
argument_list|,
name|analyzer
argument_list|)
decl_stmt|;
name|stream
operator|.
name|reset
argument_list|()
expr_stmt|;
name|CharTermAttribute
name|termAtt
init|=
name|stream
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
name|terms
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
while|while
condition|(
name|stream
operator|.
name|incrementToken
argument_list|()
condition|)
block|{
name|String
name|tokText
init|=
name|termAtt
operator|.
name|toString
argument_list|()
decl_stmt|;
name|terms
operator|.
name|add
argument_list|(
name|tokText
argument_list|)
expr_stmt|;
block|}
return|return
name|terms
return|;
block|}
DECL|method|getJsonSettings
specifier|private
name|Settings
name|getJsonSettings
parameter_list|()
block|{
return|return
name|settingsBuilder
argument_list|()
operator|.
name|loadFromClasspath
argument_list|(
literal|"org/elasticsearch/index/analysis/test1.json"
argument_list|)
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
return|;
block|}
DECL|method|getYamlSettings
specifier|private
name|Settings
name|getYamlSettings
parameter_list|()
block|{
return|return
name|settingsBuilder
argument_list|()
operator|.
name|loadFromClasspath
argument_list|(
literal|"org/elasticsearch/index/analysis/test1.yml"
argument_list|)
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
return|;
block|}
block|}
end_class

end_unit

