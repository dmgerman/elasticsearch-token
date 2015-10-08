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
name|Nullable
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
name|Strings
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
name|AbstractIndexComponent
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
name|mapper
operator|.
name|core
operator|.
name|StringFieldMapper
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
name|IndicesAnalysisService
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Closeable
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
name|unmodifiableMap
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|AnalysisService
specifier|public
class|class
name|AnalysisService
extends|extends
name|AbstractIndexComponent
implements|implements
name|Closeable
block|{
DECL|field|analyzers
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|NamedAnalyzer
argument_list|>
name|analyzers
decl_stmt|;
DECL|field|tokenizers
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|TokenizerFactory
argument_list|>
name|tokenizers
decl_stmt|;
DECL|field|charFilters
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|CharFilterFactory
argument_list|>
name|charFilters
decl_stmt|;
DECL|field|tokenFilters
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|TokenFilterFactory
argument_list|>
name|tokenFilters
decl_stmt|;
DECL|field|defaultIndexAnalyzer
specifier|private
specifier|final
name|NamedAnalyzer
name|defaultIndexAnalyzer
decl_stmt|;
DECL|field|defaultSearchAnalyzer
specifier|private
specifier|final
name|NamedAnalyzer
name|defaultSearchAnalyzer
decl_stmt|;
DECL|field|defaultSearchQuoteAnalyzer
specifier|private
specifier|final
name|NamedAnalyzer
name|defaultSearchQuoteAnalyzer
decl_stmt|;
DECL|method|AnalysisService
specifier|public
name|AnalysisService
parameter_list|(
name|Index
name|index
parameter_list|,
name|Settings
name|indexSettings
parameter_list|)
block|{
name|this
argument_list|(
name|index
argument_list|,
name|indexSettings
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Inject
DECL|method|AnalysisService
specifier|public
name|AnalysisService
parameter_list|(
name|Index
name|index
parameter_list|,
annotation|@
name|IndexSettings
name|Settings
name|indexSettings
parameter_list|,
annotation|@
name|Nullable
name|IndicesAnalysisService
name|indicesAnalysisService
parameter_list|,
annotation|@
name|Nullable
name|Map
argument_list|<
name|String
argument_list|,
name|AnalyzerProviderFactory
argument_list|>
name|analyzerFactoryFactories
parameter_list|,
annotation|@
name|Nullable
name|Map
argument_list|<
name|String
argument_list|,
name|TokenizerFactoryFactory
argument_list|>
name|tokenizerFactoryFactories
parameter_list|,
annotation|@
name|Nullable
name|Map
argument_list|<
name|String
argument_list|,
name|CharFilterFactoryFactory
argument_list|>
name|charFilterFactoryFactories
parameter_list|,
annotation|@
name|Nullable
name|Map
argument_list|<
name|String
argument_list|,
name|TokenFilterFactoryFactory
argument_list|>
name|tokenFilterFactoryFactories
parameter_list|)
block|{
name|super
argument_list|(
name|index
argument_list|,
name|indexSettings
argument_list|)
expr_stmt|;
name|Settings
name|defaultSettings
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
name|indexCreated
argument_list|(
name|indexSettings
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|TokenizerFactory
argument_list|>
name|tokenizers
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
if|if
condition|(
name|tokenizerFactoryFactories
operator|!=
literal|null
condition|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Settings
argument_list|>
name|tokenizersSettings
init|=
name|indexSettings
operator|.
name|getGroups
argument_list|(
literal|"index.analysis.tokenizer"
argument_list|)
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|TokenizerFactoryFactory
argument_list|>
name|entry
range|:
name|tokenizerFactoryFactories
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|tokenizerName
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|TokenizerFactoryFactory
name|tokenizerFactoryFactory
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|Settings
name|tokenizerSettings
init|=
name|tokenizersSettings
operator|.
name|get
argument_list|(
name|tokenizerName
argument_list|)
decl_stmt|;
if|if
condition|(
name|tokenizerSettings
operator|==
literal|null
condition|)
block|{
name|tokenizerSettings
operator|=
name|defaultSettings
expr_stmt|;
block|}
name|TokenizerFactory
name|tokenizerFactory
init|=
name|tokenizerFactoryFactory
operator|.
name|create
argument_list|(
name|tokenizerName
argument_list|,
name|tokenizerSettings
argument_list|)
decl_stmt|;
name|tokenizers
operator|.
name|put
argument_list|(
name|tokenizerName
argument_list|,
name|tokenizerFactory
argument_list|)
expr_stmt|;
name|tokenizers
operator|.
name|put
argument_list|(
name|Strings
operator|.
name|toCamelCase
argument_list|(
name|tokenizerName
argument_list|)
argument_list|,
name|tokenizerFactory
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|indicesAnalysisService
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|PreBuiltTokenizerFactoryFactory
argument_list|>
name|entry
range|:
name|indicesAnalysisService
operator|.
name|tokenizerFactories
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|name
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|tokenizers
operator|.
name|containsKey
argument_list|(
name|name
argument_list|)
condition|)
block|{
name|tokenizers
operator|.
name|put
argument_list|(
name|name
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|create
argument_list|(
name|name
argument_list|,
name|defaultSettings
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|name
operator|=
name|Strings
operator|.
name|toCamelCase
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|name
operator|.
name|equals
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
condition|)
block|{
if|if
condition|(
operator|!
name|tokenizers
operator|.
name|containsKey
argument_list|(
name|name
argument_list|)
condition|)
block|{
name|tokenizers
operator|.
name|put
argument_list|(
name|name
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|create
argument_list|(
name|name
argument_list|,
name|defaultSettings
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
name|this
operator|.
name|tokenizers
operator|=
name|unmodifiableMap
argument_list|(
name|tokenizers
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|CharFilterFactory
argument_list|>
name|charFilters
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
if|if
condition|(
name|charFilterFactoryFactories
operator|!=
literal|null
condition|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Settings
argument_list|>
name|charFiltersSettings
init|=
name|indexSettings
operator|.
name|getGroups
argument_list|(
literal|"index.analysis.char_filter"
argument_list|)
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|CharFilterFactoryFactory
argument_list|>
name|entry
range|:
name|charFilterFactoryFactories
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|charFilterName
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|CharFilterFactoryFactory
name|charFilterFactoryFactory
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|Settings
name|charFilterSettings
init|=
name|charFiltersSettings
operator|.
name|get
argument_list|(
name|charFilterName
argument_list|)
decl_stmt|;
if|if
condition|(
name|charFilterSettings
operator|==
literal|null
condition|)
block|{
name|charFilterSettings
operator|=
name|defaultSettings
expr_stmt|;
block|}
name|CharFilterFactory
name|tokenFilterFactory
init|=
name|charFilterFactoryFactory
operator|.
name|create
argument_list|(
name|charFilterName
argument_list|,
name|charFilterSettings
argument_list|)
decl_stmt|;
name|charFilters
operator|.
name|put
argument_list|(
name|charFilterName
argument_list|,
name|tokenFilterFactory
argument_list|)
expr_stmt|;
name|charFilters
operator|.
name|put
argument_list|(
name|Strings
operator|.
name|toCamelCase
argument_list|(
name|charFilterName
argument_list|)
argument_list|,
name|tokenFilterFactory
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|indicesAnalysisService
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|PreBuiltCharFilterFactoryFactory
argument_list|>
name|entry
range|:
name|indicesAnalysisService
operator|.
name|charFilterFactories
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|name
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|charFilters
operator|.
name|containsKey
argument_list|(
name|name
argument_list|)
condition|)
block|{
name|charFilters
operator|.
name|put
argument_list|(
name|name
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|create
argument_list|(
name|name
argument_list|,
name|defaultSettings
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|name
operator|=
name|Strings
operator|.
name|toCamelCase
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|name
operator|.
name|equals
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
condition|)
block|{
if|if
condition|(
operator|!
name|charFilters
operator|.
name|containsKey
argument_list|(
name|name
argument_list|)
condition|)
block|{
name|charFilters
operator|.
name|put
argument_list|(
name|name
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|create
argument_list|(
name|name
argument_list|,
name|defaultSettings
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
name|this
operator|.
name|charFilters
operator|=
name|unmodifiableMap
argument_list|(
name|charFilters
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|TokenFilterFactory
argument_list|>
name|tokenFilters
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
if|if
condition|(
name|tokenFilterFactoryFactories
operator|!=
literal|null
condition|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Settings
argument_list|>
name|tokenFiltersSettings
init|=
name|indexSettings
operator|.
name|getGroups
argument_list|(
literal|"index.analysis.filter"
argument_list|)
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|TokenFilterFactoryFactory
argument_list|>
name|entry
range|:
name|tokenFilterFactoryFactories
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|tokenFilterName
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|TokenFilterFactoryFactory
name|tokenFilterFactoryFactory
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|Settings
name|tokenFilterSettings
init|=
name|tokenFiltersSettings
operator|.
name|get
argument_list|(
name|tokenFilterName
argument_list|)
decl_stmt|;
if|if
condition|(
name|tokenFilterSettings
operator|==
literal|null
condition|)
block|{
name|tokenFilterSettings
operator|=
name|defaultSettings
expr_stmt|;
block|}
name|TokenFilterFactory
name|tokenFilterFactory
init|=
name|tokenFilterFactoryFactory
operator|.
name|create
argument_list|(
name|tokenFilterName
argument_list|,
name|tokenFilterSettings
argument_list|)
decl_stmt|;
name|tokenFilters
operator|.
name|put
argument_list|(
name|tokenFilterName
argument_list|,
name|tokenFilterFactory
argument_list|)
expr_stmt|;
name|tokenFilters
operator|.
name|put
argument_list|(
name|Strings
operator|.
name|toCamelCase
argument_list|(
name|tokenFilterName
argument_list|)
argument_list|,
name|tokenFilterFactory
argument_list|)
expr_stmt|;
block|}
block|}
comment|// pre initialize the globally registered ones into the map
if|if
condition|(
name|indicesAnalysisService
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|PreBuiltTokenFilterFactoryFactory
argument_list|>
name|entry
range|:
name|indicesAnalysisService
operator|.
name|tokenFilterFactories
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|name
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|tokenFilters
operator|.
name|containsKey
argument_list|(
name|name
argument_list|)
condition|)
block|{
name|tokenFilters
operator|.
name|put
argument_list|(
name|name
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|create
argument_list|(
name|name
argument_list|,
name|defaultSettings
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|name
operator|=
name|Strings
operator|.
name|toCamelCase
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|name
operator|.
name|equals
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
condition|)
block|{
if|if
condition|(
operator|!
name|tokenFilters
operator|.
name|containsKey
argument_list|(
name|name
argument_list|)
condition|)
block|{
name|tokenFilters
operator|.
name|put
argument_list|(
name|name
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|create
argument_list|(
name|name
argument_list|,
name|defaultSettings
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
name|this
operator|.
name|tokenFilters
operator|=
name|unmodifiableMap
argument_list|(
name|tokenFilters
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|AnalyzerProvider
argument_list|>
name|analyzerProviders
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
if|if
condition|(
name|analyzerFactoryFactories
operator|!=
literal|null
condition|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Settings
argument_list|>
name|analyzersSettings
init|=
name|indexSettings
operator|.
name|getGroups
argument_list|(
literal|"index.analysis.analyzer"
argument_list|)
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|AnalyzerProviderFactory
argument_list|>
name|entry
range|:
name|analyzerFactoryFactories
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|analyzerName
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|AnalyzerProviderFactory
name|analyzerFactoryFactory
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|Settings
name|analyzerSettings
init|=
name|analyzersSettings
operator|.
name|get
argument_list|(
name|analyzerName
argument_list|)
decl_stmt|;
if|if
condition|(
name|analyzerSettings
operator|==
literal|null
condition|)
block|{
name|analyzerSettings
operator|=
name|defaultSettings
expr_stmt|;
block|}
name|AnalyzerProvider
name|analyzerFactory
init|=
name|analyzerFactoryFactory
operator|.
name|create
argument_list|(
name|analyzerName
argument_list|,
name|analyzerSettings
argument_list|)
decl_stmt|;
name|analyzerProviders
operator|.
name|put
argument_list|(
name|analyzerName
argument_list|,
name|analyzerFactory
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|indicesAnalysisService
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|PreBuiltAnalyzerProviderFactory
argument_list|>
name|entry
range|:
name|indicesAnalysisService
operator|.
name|analyzerProviderFactories
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|name
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|Version
name|indexVersion
init|=
name|Version
operator|.
name|indexCreated
argument_list|(
name|indexSettings
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|analyzerProviders
operator|.
name|containsKey
argument_list|(
name|name
argument_list|)
condition|)
block|{
name|analyzerProviders
operator|.
name|put
argument_list|(
name|name
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|create
argument_list|(
name|name
argument_list|,
name|Settings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_VERSION_CREATED
argument_list|,
name|indexVersion
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|String
name|camelCaseName
init|=
name|Strings
operator|.
name|toCamelCase
argument_list|(
name|name
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|camelCaseName
operator|.
name|equals
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
operator|&&
operator|!
name|analyzerProviders
operator|.
name|containsKey
argument_list|(
name|camelCaseName
argument_list|)
condition|)
block|{
name|analyzerProviders
operator|.
name|put
argument_list|(
name|camelCaseName
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|create
argument_list|(
name|name
argument_list|,
name|Settings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_VERSION_CREATED
argument_list|,
name|indexVersion
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
operator|!
name|analyzerProviders
operator|.
name|containsKey
argument_list|(
literal|"default"
argument_list|)
condition|)
block|{
name|analyzerProviders
operator|.
name|put
argument_list|(
literal|"default"
argument_list|,
operator|new
name|StandardAnalyzerProvider
argument_list|(
name|index
argument_list|,
name|indexSettings
argument_list|,
literal|null
argument_list|,
literal|"default"
argument_list|,
name|Settings
operator|.
name|Builder
operator|.
name|EMPTY_SETTINGS
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|analyzerProviders
operator|.
name|containsKey
argument_list|(
literal|"default_index"
argument_list|)
condition|)
block|{
name|analyzerProviders
operator|.
name|put
argument_list|(
literal|"default_index"
argument_list|,
name|analyzerProviders
operator|.
name|get
argument_list|(
literal|"default"
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|analyzerProviders
operator|.
name|containsKey
argument_list|(
literal|"default_search"
argument_list|)
condition|)
block|{
name|analyzerProviders
operator|.
name|put
argument_list|(
literal|"default_search"
argument_list|,
name|analyzerProviders
operator|.
name|get
argument_list|(
literal|"default"
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|analyzerProviders
operator|.
name|containsKey
argument_list|(
literal|"default_search_quoted"
argument_list|)
condition|)
block|{
name|analyzerProviders
operator|.
name|put
argument_list|(
literal|"default_search_quoted"
argument_list|,
name|analyzerProviders
operator|.
name|get
argument_list|(
literal|"default_search"
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|Map
argument_list|<
name|String
argument_list|,
name|NamedAnalyzer
argument_list|>
name|analyzers
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|AnalyzerProvider
name|analyzerFactory
range|:
name|analyzerProviders
operator|.
name|values
argument_list|()
control|)
block|{
comment|/*              * Lucene defaults positionIncrementGap to 0 in all analyzers but              * Elasticsearch defaults them to 0 only before version 2.0              * and 100 afterwards so we override the positionIncrementGap if it              * doesn't match here.              */
name|int
name|overridePositionIncrementGap
init|=
name|StringFieldMapper
operator|.
name|Defaults
operator|.
name|positionIncrementGap
argument_list|(
name|Version
operator|.
name|indexCreated
argument_list|(
name|indexSettings
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|analyzerFactory
operator|instanceof
name|CustomAnalyzerProvider
condition|)
block|{
operator|(
operator|(
name|CustomAnalyzerProvider
operator|)
name|analyzerFactory
operator|)
operator|.
name|build
argument_list|(
name|this
argument_list|)
expr_stmt|;
comment|/*                  * Custom analyzers already default to the correct, version                  * dependent positionIncrementGap and the user is be able to                  * configure the positionIncrementGap directly on the analyzer so                  * we disable overriding the positionIncrementGap to preserve the                  * user's setting.                  */
name|overridePositionIncrementGap
operator|=
name|Integer
operator|.
name|MIN_VALUE
expr_stmt|;
block|}
name|Analyzer
name|analyzerF
init|=
name|analyzerFactory
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|analyzerF
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"analyzer ["
operator|+
name|analyzerFactory
operator|.
name|name
argument_list|()
operator|+
literal|"] created null analyzer"
argument_list|)
throw|;
block|}
name|NamedAnalyzer
name|analyzer
decl_stmt|;
if|if
condition|(
name|analyzerF
operator|instanceof
name|NamedAnalyzer
condition|)
block|{
comment|// if we got a named analyzer back, use it...
name|analyzer
operator|=
operator|(
name|NamedAnalyzer
operator|)
name|analyzerF
expr_stmt|;
if|if
condition|(
name|overridePositionIncrementGap
operator|>=
literal|0
operator|&&
name|analyzer
operator|.
name|getPositionIncrementGap
argument_list|(
name|analyzer
operator|.
name|name
argument_list|()
argument_list|)
operator|!=
name|overridePositionIncrementGap
condition|)
block|{
comment|// unless the positionIncrementGap needs to be overridden
name|analyzer
operator|=
operator|new
name|NamedAnalyzer
argument_list|(
name|analyzer
argument_list|,
name|overridePositionIncrementGap
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|analyzer
operator|=
operator|new
name|NamedAnalyzer
argument_list|(
name|analyzerFactory
operator|.
name|name
argument_list|()
argument_list|,
name|analyzerFactory
operator|.
name|scope
argument_list|()
argument_list|,
name|analyzerF
argument_list|,
name|overridePositionIncrementGap
argument_list|)
expr_stmt|;
block|}
name|analyzers
operator|.
name|put
argument_list|(
name|analyzerFactory
operator|.
name|name
argument_list|()
argument_list|,
name|analyzer
argument_list|)
expr_stmt|;
name|analyzers
operator|.
name|put
argument_list|(
name|Strings
operator|.
name|toCamelCase
argument_list|(
name|analyzerFactory
operator|.
name|name
argument_list|()
argument_list|)
argument_list|,
name|analyzer
argument_list|)
expr_stmt|;
name|String
name|strAliases
init|=
name|indexSettings
operator|.
name|get
argument_list|(
literal|"index.analysis.analyzer."
operator|+
name|analyzerFactory
operator|.
name|name
argument_list|()
operator|+
literal|".alias"
argument_list|)
decl_stmt|;
if|if
condition|(
name|strAliases
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|String
name|alias
range|:
name|Strings
operator|.
name|commaDelimitedListToStringArray
argument_list|(
name|strAliases
argument_list|)
control|)
block|{
name|analyzers
operator|.
name|put
argument_list|(
name|alias
argument_list|,
name|analyzer
argument_list|)
expr_stmt|;
block|}
block|}
name|String
index|[]
name|aliases
init|=
name|indexSettings
operator|.
name|getAsArray
argument_list|(
literal|"index.analysis.analyzer."
operator|+
name|analyzerFactory
operator|.
name|name
argument_list|()
operator|+
literal|".alias"
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|alias
range|:
name|aliases
control|)
block|{
name|analyzers
operator|.
name|put
argument_list|(
name|alias
argument_list|,
name|analyzer
argument_list|)
expr_stmt|;
block|}
block|}
name|NamedAnalyzer
name|defaultAnalyzer
init|=
name|analyzers
operator|.
name|get
argument_list|(
literal|"default"
argument_list|)
decl_stmt|;
if|if
condition|(
name|defaultAnalyzer
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"no default analyzer configured"
argument_list|)
throw|;
block|}
if|if
condition|(
name|analyzers
operator|.
name|containsKey
argument_list|(
literal|"default_index"
argument_list|)
condition|)
block|{
specifier|final
name|Version
name|createdVersion
init|=
name|Version
operator|.
name|indexCreated
argument_list|(
name|indexSettings
argument_list|)
decl_stmt|;
if|if
condition|(
name|createdVersion
operator|.
name|onOrAfter
argument_list|(
name|Version
operator|.
name|V_3_0_0
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"setting [index.analysis.analyzer.default_index] is not supported anymore, use [index.analysis.analyzer.default] instead for index ["
operator|+
name|index
operator|.
name|getName
argument_list|()
operator|+
literal|"]"
argument_list|)
throw|;
block|}
else|else
block|{
name|deprecationLogger
operator|.
name|deprecated
argument_list|(
literal|"setting [index.analysis.analyzer.default_index] is deprecated, use [index.analysis.analyzer.default] instead for index [{}]"
argument_list|,
name|index
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|defaultIndexAnalyzer
operator|=
name|analyzers
operator|.
name|containsKey
argument_list|(
literal|"default_index"
argument_list|)
condition|?
name|analyzers
operator|.
name|get
argument_list|(
literal|"default_index"
argument_list|)
else|:
name|defaultAnalyzer
expr_stmt|;
name|defaultSearchAnalyzer
operator|=
name|analyzers
operator|.
name|containsKey
argument_list|(
literal|"default_search"
argument_list|)
condition|?
name|analyzers
operator|.
name|get
argument_list|(
literal|"default_search"
argument_list|)
else|:
name|defaultAnalyzer
expr_stmt|;
name|defaultSearchQuoteAnalyzer
operator|=
name|analyzers
operator|.
name|containsKey
argument_list|(
literal|"default_search_quote"
argument_list|)
condition|?
name|analyzers
operator|.
name|get
argument_list|(
literal|"default_search_quote"
argument_list|)
else|:
name|defaultSearchAnalyzer
expr_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|NamedAnalyzer
argument_list|>
name|analyzer
range|:
name|analyzers
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
name|analyzer
operator|.
name|getKey
argument_list|()
operator|.
name|startsWith
argument_list|(
literal|"_"
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"analyzer name must not start with '_'. got \""
operator|+
name|analyzer
operator|.
name|getKey
argument_list|()
operator|+
literal|"\""
argument_list|)
throw|;
block|}
block|}
name|this
operator|.
name|analyzers
operator|=
name|unmodifiableMap
argument_list|(
name|analyzers
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
block|{
for|for
control|(
name|NamedAnalyzer
name|analyzer
range|:
name|analyzers
operator|.
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|analyzer
operator|.
name|scope
argument_list|()
operator|==
name|AnalyzerScope
operator|.
name|INDEX
condition|)
block|{
try|try
block|{
name|analyzer
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NullPointerException
name|e
parameter_list|)
block|{
comment|// because analyzers are aliased, they might be closed several times
comment|// an NPE is thrown in this case, so ignore....
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"failed to close analyzer "
operator|+
name|analyzer
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
DECL|method|analyzer
specifier|public
name|NamedAnalyzer
name|analyzer
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
name|analyzers
operator|.
name|get
argument_list|(
name|name
argument_list|)
return|;
block|}
DECL|method|defaultIndexAnalyzer
specifier|public
name|NamedAnalyzer
name|defaultIndexAnalyzer
parameter_list|()
block|{
return|return
name|defaultIndexAnalyzer
return|;
block|}
DECL|method|defaultSearchAnalyzer
specifier|public
name|NamedAnalyzer
name|defaultSearchAnalyzer
parameter_list|()
block|{
return|return
name|defaultSearchAnalyzer
return|;
block|}
DECL|method|defaultSearchQuoteAnalyzer
specifier|public
name|NamedAnalyzer
name|defaultSearchQuoteAnalyzer
parameter_list|()
block|{
return|return
name|defaultSearchQuoteAnalyzer
return|;
block|}
DECL|method|tokenizer
specifier|public
name|TokenizerFactory
name|tokenizer
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
name|tokenizers
operator|.
name|get
argument_list|(
name|name
argument_list|)
return|;
block|}
DECL|method|charFilter
specifier|public
name|CharFilterFactory
name|charFilter
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
name|charFilters
operator|.
name|get
argument_list|(
name|name
argument_list|)
return|;
block|}
DECL|method|tokenFilter
specifier|public
name|TokenFilterFactory
name|tokenFilter
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
name|tokenFilters
operator|.
name|get
argument_list|(
name|name
argument_list|)
return|;
block|}
block|}
end_class

end_unit

