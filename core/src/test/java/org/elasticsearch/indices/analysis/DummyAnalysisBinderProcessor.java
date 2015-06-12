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
name|index
operator|.
name|analysis
operator|.
name|AnalysisModule
import|;
end_import

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|DummyAnalysisBinderProcessor
specifier|public
class|class
name|DummyAnalysisBinderProcessor
extends|extends
name|AnalysisModule
operator|.
name|AnalysisBinderProcessor
block|{
annotation|@
name|Override
DECL|method|processAnalyzers
specifier|public
name|void
name|processAnalyzers
parameter_list|(
name|AnalyzersBindings
name|analyzersBindings
parameter_list|)
block|{
name|analyzersBindings
operator|.
name|processAnalyzer
argument_list|(
literal|"dummy"
argument_list|,
name|DummyAnalyzerProvider
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|processTokenFilters
specifier|public
name|void
name|processTokenFilters
parameter_list|(
name|TokenFiltersBindings
name|tokenFiltersBindings
parameter_list|)
block|{
name|tokenFiltersBindings
operator|.
name|processTokenFilter
argument_list|(
literal|"dummy_token_filter"
argument_list|,
name|DummyTokenFilterFactory
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|processTokenizers
specifier|public
name|void
name|processTokenizers
parameter_list|(
name|TokenizersBindings
name|tokenizersBindings
parameter_list|)
block|{
name|tokenizersBindings
operator|.
name|processTokenizer
argument_list|(
literal|"dummy_tokenizer"
argument_list|,
name|DummyTokenizerFactory
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|processCharFilters
specifier|public
name|void
name|processCharFilters
parameter_list|(
name|CharFiltersBindings
name|charFiltersBindings
parameter_list|)
block|{
name|charFiltersBindings
operator|.
name|processCharFilter
argument_list|(
literal|"dummy_char_filter"
argument_list|,
name|DummyCharFilterFactory
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit
