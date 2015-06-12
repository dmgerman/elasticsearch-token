begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.indices.analysis
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|analysis
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|component
operator|.
name|AbstractComponent
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
name|Inject
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
name|*
import|;
end_import

begin_class
DECL|class|DummyIndicesAnalysis
specifier|public
class|class
name|DummyIndicesAnalysis
extends|extends
name|AbstractComponent
block|{
annotation|@
name|Inject
DECL|method|DummyIndicesAnalysis
specifier|public
name|DummyIndicesAnalysis
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|IndicesAnalysisService
name|indicesAnalysisService
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|indicesAnalysisService
operator|.
name|analyzerProviderFactories
argument_list|()
operator|.
name|put
argument_list|(
literal|"dummy"
argument_list|,
operator|new
name|PreBuiltAnalyzerProviderFactory
argument_list|(
literal|"dummy"
argument_list|,
name|AnalyzerScope
operator|.
name|INDICES
argument_list|,
operator|new
name|DummyAnalyzer
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|indicesAnalysisService
operator|.
name|tokenFilterFactories
argument_list|()
operator|.
name|put
argument_list|(
literal|"dummy_token_filter"
argument_list|,
operator|new
name|PreBuiltTokenFilterFactoryFactory
argument_list|(
operator|new
name|DummyTokenFilterFactory
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|indicesAnalysisService
operator|.
name|charFilterFactories
argument_list|()
operator|.
name|put
argument_list|(
literal|"dummy_char_filter"
argument_list|,
operator|new
name|PreBuiltCharFilterFactoryFactory
argument_list|(
operator|new
name|DummyCharFilterFactory
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|indicesAnalysisService
operator|.
name|tokenizerFactories
argument_list|()
operator|.
name|put
argument_list|(
literal|"dummy_tokenizer"
argument_list|,
operator|new
name|PreBuiltTokenizerFactoryFactory
argument_list|(
operator|new
name|DummyTokenizerFactory
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit
