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
name|Version
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
name|common
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ConcurrentCollections
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Locale
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
name|ImmutableSettings
operator|.
name|Builder
operator|.
name|EMPTY_SETTINGS
import|;
end_import

begin_comment
comment|/**  * A node level registry of analyzers, to be reused by different indices which use default analyzers.  */
end_comment

begin_class
DECL|class|IndicesAnalysisService
specifier|public
class|class
name|IndicesAnalysisService
extends|extends
name|AbstractComponent
block|{
DECL|field|analyzerProviderFactories
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|PreBuiltAnalyzerProviderFactory
argument_list|>
name|analyzerProviderFactories
init|=
name|ConcurrentCollections
operator|.
name|newConcurrentMap
argument_list|()
decl_stmt|;
DECL|field|tokenizerFactories
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|PreBuiltTokenizerFactoryFactory
argument_list|>
name|tokenizerFactories
init|=
name|ConcurrentCollections
operator|.
name|newConcurrentMap
argument_list|()
decl_stmt|;
DECL|field|tokenFilterFactories
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|PreBuiltTokenFilterFactoryFactory
argument_list|>
name|tokenFilterFactories
init|=
name|ConcurrentCollections
operator|.
name|newConcurrentMap
argument_list|()
decl_stmt|;
DECL|field|charFilterFactories
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|PreBuiltCharFilterFactoryFactory
argument_list|>
name|charFilterFactories
init|=
name|ConcurrentCollections
operator|.
name|newConcurrentMap
argument_list|()
decl_stmt|;
DECL|method|IndicesAnalysisService
specifier|public
name|IndicesAnalysisService
parameter_list|()
block|{
name|super
argument_list|(
name|EMPTY_SETTINGS
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Inject
DECL|method|IndicesAnalysisService
specifier|public
name|IndicesAnalysisService
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
comment|// Analyzers
for|for
control|(
name|PreBuiltAnalyzers
name|preBuiltAnalyzerEnum
range|:
name|PreBuiltAnalyzers
operator|.
name|values
argument_list|()
control|)
block|{
name|String
name|name
init|=
name|preBuiltAnalyzerEnum
operator|.
name|name
argument_list|()
operator|.
name|toLowerCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
decl_stmt|;
name|analyzerProviderFactories
operator|.
name|put
argument_list|(
name|name
argument_list|,
operator|new
name|PreBuiltAnalyzerProviderFactory
argument_list|(
name|name
argument_list|,
name|AnalyzerScope
operator|.
name|INDICES
argument_list|,
name|preBuiltAnalyzerEnum
operator|.
name|getAnalyzer
argument_list|(
name|Version
operator|.
name|CURRENT
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// Tokenizers
for|for
control|(
name|PreBuiltTokenizers
name|preBuiltTokenizer
range|:
name|PreBuiltTokenizers
operator|.
name|values
argument_list|()
control|)
block|{
name|String
name|name
init|=
name|preBuiltTokenizer
operator|.
name|name
argument_list|()
operator|.
name|toLowerCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
decl_stmt|;
name|tokenizerFactories
operator|.
name|put
argument_list|(
name|name
argument_list|,
operator|new
name|PreBuiltTokenizerFactoryFactory
argument_list|(
name|preBuiltTokenizer
operator|.
name|getTokenizerFactory
argument_list|(
name|Version
operator|.
name|CURRENT
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// Tokenizer aliases
name|tokenizerFactories
operator|.
name|put
argument_list|(
literal|"nGram"
argument_list|,
operator|new
name|PreBuiltTokenizerFactoryFactory
argument_list|(
name|PreBuiltTokenizers
operator|.
name|NGRAM
operator|.
name|getTokenizerFactory
argument_list|(
name|Version
operator|.
name|CURRENT
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|tokenizerFactories
operator|.
name|put
argument_list|(
literal|"edgeNGram"
argument_list|,
operator|new
name|PreBuiltTokenizerFactoryFactory
argument_list|(
name|PreBuiltTokenizers
operator|.
name|EDGE_NGRAM
operator|.
name|getTokenizerFactory
argument_list|(
name|Version
operator|.
name|CURRENT
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// Token filters
for|for
control|(
name|PreBuiltTokenFilters
name|preBuiltTokenFilter
range|:
name|PreBuiltTokenFilters
operator|.
name|values
argument_list|()
control|)
block|{
name|String
name|name
init|=
name|preBuiltTokenFilter
operator|.
name|name
argument_list|()
operator|.
name|toLowerCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
decl_stmt|;
name|tokenFilterFactories
operator|.
name|put
argument_list|(
name|name
argument_list|,
operator|new
name|PreBuiltTokenFilterFactoryFactory
argument_list|(
name|preBuiltTokenFilter
operator|.
name|getTokenFilterFactory
argument_list|(
name|Version
operator|.
name|CURRENT
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// Token filter aliases
name|tokenFilterFactories
operator|.
name|put
argument_list|(
literal|"nGram"
argument_list|,
operator|new
name|PreBuiltTokenFilterFactoryFactory
argument_list|(
name|PreBuiltTokenFilters
operator|.
name|NGRAM
operator|.
name|getTokenFilterFactory
argument_list|(
name|Version
operator|.
name|CURRENT
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|tokenFilterFactories
operator|.
name|put
argument_list|(
literal|"edgeNGram"
argument_list|,
operator|new
name|PreBuiltTokenFilterFactoryFactory
argument_list|(
name|PreBuiltTokenFilters
operator|.
name|EDGE_NGRAM
operator|.
name|getTokenFilterFactory
argument_list|(
name|Version
operator|.
name|CURRENT
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// Char Filters
for|for
control|(
name|PreBuiltCharFilters
name|preBuiltCharFilter
range|:
name|PreBuiltCharFilters
operator|.
name|values
argument_list|()
control|)
block|{
name|String
name|name
init|=
name|preBuiltCharFilter
operator|.
name|name
argument_list|()
operator|.
name|toLowerCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
decl_stmt|;
name|charFilterFactories
operator|.
name|put
argument_list|(
name|name
argument_list|,
operator|new
name|PreBuiltCharFilterFactoryFactory
argument_list|(
name|preBuiltCharFilter
operator|.
name|getCharFilterFactory
argument_list|(
name|Version
operator|.
name|CURRENT
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// Char filter aliases
name|charFilterFactories
operator|.
name|put
argument_list|(
literal|"htmlStrip"
argument_list|,
operator|new
name|PreBuiltCharFilterFactoryFactory
argument_list|(
name|PreBuiltCharFilters
operator|.
name|HTML_STRIP
operator|.
name|getCharFilterFactory
argument_list|(
name|Version
operator|.
name|CURRENT
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|hasCharFilter
specifier|public
name|boolean
name|hasCharFilter
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
name|charFilterFactoryFactory
argument_list|(
name|name
argument_list|)
operator|!=
literal|null
return|;
block|}
DECL|method|charFilterFactories
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|PreBuiltCharFilterFactoryFactory
argument_list|>
name|charFilterFactories
parameter_list|()
block|{
return|return
name|charFilterFactories
return|;
block|}
DECL|method|charFilterFactoryFactory
specifier|public
name|CharFilterFactoryFactory
name|charFilterFactoryFactory
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
name|charFilterFactories
operator|.
name|get
argument_list|(
name|name
argument_list|)
return|;
block|}
DECL|method|hasTokenFilter
specifier|public
name|boolean
name|hasTokenFilter
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
name|tokenFilterFactoryFactory
argument_list|(
name|name
argument_list|)
operator|!=
literal|null
return|;
block|}
DECL|method|tokenFilterFactories
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|PreBuiltTokenFilterFactoryFactory
argument_list|>
name|tokenFilterFactories
parameter_list|()
block|{
return|return
name|tokenFilterFactories
return|;
block|}
DECL|method|tokenFilterFactoryFactory
specifier|public
name|TokenFilterFactoryFactory
name|tokenFilterFactoryFactory
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
name|tokenFilterFactories
operator|.
name|get
argument_list|(
name|name
argument_list|)
return|;
block|}
DECL|method|hasTokenizer
specifier|public
name|boolean
name|hasTokenizer
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
name|tokenizerFactoryFactory
argument_list|(
name|name
argument_list|)
operator|!=
literal|null
return|;
block|}
DECL|method|tokenizerFactories
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|PreBuiltTokenizerFactoryFactory
argument_list|>
name|tokenizerFactories
parameter_list|()
block|{
return|return
name|tokenizerFactories
return|;
block|}
DECL|method|tokenizerFactoryFactory
specifier|public
name|TokenizerFactoryFactory
name|tokenizerFactoryFactory
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
name|tokenizerFactories
operator|.
name|get
argument_list|(
name|name
argument_list|)
return|;
block|}
DECL|method|analyzerProviderFactories
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|PreBuiltAnalyzerProviderFactory
argument_list|>
name|analyzerProviderFactories
parameter_list|()
block|{
return|return
name|analyzerProviderFactories
return|;
block|}
DECL|method|analyzerProviderFactory
specifier|public
name|PreBuiltAnalyzerProviderFactory
name|analyzerProviderFactory
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
name|analyzerProviderFactories
operator|.
name|get
argument_list|(
name|name
argument_list|)
return|;
block|}
DECL|method|hasAnalyzer
specifier|public
name|boolean
name|hasAnalyzer
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
name|analyzerProviderFactories
operator|.
name|containsKey
argument_list|(
name|name
argument_list|)
return|;
block|}
DECL|method|analyzer
specifier|public
name|Analyzer
name|analyzer
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|PreBuiltAnalyzerProviderFactory
name|analyzerProviderFactory
init|=
name|analyzerProviderFactory
argument_list|(
name|name
argument_list|)
decl_stmt|;
if|if
condition|(
name|analyzerProviderFactory
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|analyzerProviderFactory
operator|.
name|analyzer
argument_list|()
return|;
block|}
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
block|{
for|for
control|(
name|PreBuiltAnalyzerProviderFactory
name|analyzerProviderFactory
range|:
name|analyzerProviderFactories
operator|.
name|values
argument_list|()
control|)
block|{
try|try
block|{
name|analyzerProviderFactory
operator|.
name|analyzer
argument_list|()
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// ignore
block|}
block|}
block|}
block|}
end_class

end_unit

