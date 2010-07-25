begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|elasticsearch
operator|.
name|common
operator|.
name|inject
operator|.
name|Guice
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
name|settings
operator|.
name|IndexSettingsModule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|testng
operator|.
name|annotations
operator|.
name|Test
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
name|*
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|MatcherAssert
operator|.
name|*
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
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|AnalysisModuleTests
specifier|public
class|class
name|AnalysisModuleTests
block|{
DECL|method|testSimpleConfigurationJson
annotation|@
name|Test
specifier|public
name|void
name|testSimpleConfigurationJson
parameter_list|()
block|{
name|Settings
name|settings
init|=
name|settingsBuilder
argument_list|()
operator|.
name|loadFromClasspath
argument_list|(
literal|"org/elasticsearch/index/analysis/test1.json"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|testSimpleConfiguration
argument_list|(
name|settings
argument_list|)
expr_stmt|;
block|}
DECL|method|testSimpleConfigurationYaml
annotation|@
name|Test
specifier|public
name|void
name|testSimpleConfigurationYaml
parameter_list|()
block|{
name|Settings
name|settings
init|=
name|settingsBuilder
argument_list|()
operator|.
name|loadFromClasspath
argument_list|(
literal|"org/elasticsearch/index/analysis/test1.yml"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|testSimpleConfiguration
argument_list|(
name|settings
argument_list|)
expr_stmt|;
block|}
DECL|method|testSimpleConfiguration
specifier|private
name|void
name|testSimpleConfiguration
parameter_list|(
name|Settings
name|settings
parameter_list|)
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
name|injector
init|=
name|Guice
operator|.
name|createInjector
argument_list|(
operator|new
name|IndexSettingsModule
argument_list|(
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
argument_list|)
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
literal|"custom1"
argument_list|)
operator|.
name|analyzer
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|analyzer
argument_list|,
name|instanceOf
argument_list|(
name|CustomAnalyzer
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|CustomAnalyzer
name|custom1
init|=
operator|(
name|CustomAnalyzer
operator|)
name|analyzer
decl_stmt|;
name|assertThat
argument_list|(
name|custom1
operator|.
name|tokenizerFactory
argument_list|()
argument_list|,
name|instanceOf
argument_list|(
name|StandardTokenizerFactory
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|custom1
operator|.
name|tokenFilters
argument_list|()
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|StopTokenFilterFactory
name|stop1
init|=
operator|(
name|StopTokenFilterFactory
operator|)
name|custom1
operator|.
name|tokenFilters
argument_list|()
index|[
literal|0
index|]
decl_stmt|;
name|assertThat
argument_list|(
name|stop1
operator|.
name|stopWords
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|stop1
operator|.
name|stopWords
argument_list|()
argument_list|,
name|hasItem
argument_list|(
literal|"test-stop"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

