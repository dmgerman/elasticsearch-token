begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|IcuAnalysisBinderProcessor
specifier|public
class|class
name|IcuAnalysisBinderProcessor
extends|extends
name|AnalysisModule
operator|.
name|AnalysisBinderProcessor
block|{
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
literal|"icu_tokenizer"
argument_list|,
name|IcuTokenizerFactory
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
literal|"icu_normalizer"
argument_list|,
name|IcuNormalizerTokenFilterFactory
operator|.
name|class
argument_list|)
expr_stmt|;
name|tokenFiltersBindings
operator|.
name|processTokenFilter
argument_list|(
literal|"icu_folding"
argument_list|,
name|IcuFoldingTokenFilterFactory
operator|.
name|class
argument_list|)
expr_stmt|;
name|tokenFiltersBindings
operator|.
name|processTokenFilter
argument_list|(
literal|"icu_collation"
argument_list|,
name|IcuCollationTokenFilterFactory
operator|.
name|class
argument_list|)
expr_stmt|;
name|tokenFiltersBindings
operator|.
name|processTokenFilter
argument_list|(
literal|"icu_transform"
argument_list|,
name|IcuTransformTokenFilterFactory
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

