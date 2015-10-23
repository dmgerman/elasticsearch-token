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
name|pl
operator|.
name|PolishAnalyzer
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
name|analysis
operator|.
name|pl
operator|.
name|PolishAnalysisBinderProcessor
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
name|pl
operator|.
name|PolishStemTokenFilterFactory
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
name|hamcrest
operator|.
name|MatcherAssert
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
name|elasticsearch
operator|.
name|common
operator|.
name|settings
operator|.
name|Settings
operator|.
name|Builder
operator|.
name|EMPTY_SETTINGS
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

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|PolishAnalysisTests
specifier|public
class|class
name|PolishAnalysisTests
extends|extends
name|ESTestCase
block|{
DECL|method|testDefaultsPolishAnalysis
specifier|public
name|void
name|testDefaultsPolishAnalysis
parameter_list|()
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
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"path.home"
argument_list|,
name|createTempDir
argument_list|()
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
name|EMPTY_SETTINGS
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
name|AnalysisModule
argument_list|(
name|EMPTY_SETTINGS
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
operator|.
name|addProcessor
argument_list|(
operator|new
name|PolishAnalysisBinderProcessor
argument_list|()
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
name|tokenizerFactory
init|=
name|analysisService
operator|.
name|tokenFilter
argument_list|(
literal|"polish_stem"
argument_list|)
decl_stmt|;
name|MatcherAssert
operator|.
name|assertThat
argument_list|(
name|tokenizerFactory
argument_list|,
name|instanceOf
argument_list|(
name|PolishStemTokenFilterFactory
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|Analyzer
name|analyzer
init|=
name|analysisService
operator|.
name|analyzer
argument_list|(
literal|"polish"
argument_list|)
operator|.
name|analyzer
argument_list|()
decl_stmt|;
name|MatcherAssert
operator|.
name|assertThat
argument_list|(
name|analyzer
argument_list|,
name|instanceOf
argument_list|(
name|PolishAnalyzer
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

