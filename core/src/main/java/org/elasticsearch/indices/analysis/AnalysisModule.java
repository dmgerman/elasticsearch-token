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
name|hunspell
operator|.
name|Dictionary
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchException
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
name|AbstractModule
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
name|index
operator|.
name|analysis
operator|.
name|AnalysisRegistry
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
name|AnalyzerProvider
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
name|CharFilterFactory
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
name|TokenFilterFactory
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
name|TokenizerFactory
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
name|Map
import|;
end_import

begin_comment
comment|/**  * The AnalysisModule is the main extension point for node and index level analysis components. The lucene classes  * {@link org.apache.lucene.analysis.Analyzer}, {@link org.apache.lucene.analysis.TokenFilter}, {@link org.apache.lucene.analysis.Tokenizer}  * and {@link org.apache.lucene.analysis.CharFilter} can be extended in plugins and registered on node startup when the analysis module  * gets loaded. Since elasticsearch needs to create multiple instances for different configurations dedicated factories need to be provided for  * each of the components:  *<ul>  *<li> {@link org.apache.lucene.analysis.Analyzer} can be exposed via {@link AnalyzerProvider} and registered on {@link #registerAnalyzer(String, AnalysisProvider)}</li>  *<li> {@link org.apache.lucene.analysis.TokenFilter} can be exposed via {@link TokenFilterFactory} and registered on {@link #registerTokenFilter(String, AnalysisProvider)}</li>  *<li> {@link org.apache.lucene.analysis.Tokenizer} can be exposed via {@link TokenizerFactory} and registered on {@link #registerTokenizer(String, AnalysisProvider)}</li>  *<li> {@link org.apache.lucene.analysis.CharFilter} can be exposed via {@link CharFilterFactory} and registered on {@link #registerCharFilter(String, AnalysisProvider)}</li>  *</ul>  *  * The {@link org.elasticsearch.indices.analysis.AnalysisModule.AnalysisProvider} is only a functional interface that allows to register factory constructors directly like the plugin example below:  *<pre>  *     public class MyAnalysisPlugin extends Plugin {  *       \@Override  *       public String name() {  *         return "analysis-my-plugin";  *       }  *  *       \@Override  *       public String description() {  *         return "my very fast and efficient analyzer";  *       }  *  *       public void onModule(AnalysisModule module) {  *         module.registerAnalyzer("my-analyzer-name", MyAnalyzer::new);  *       }  *     }  *</pre>  */
end_comment

begin_class
DECL|class|AnalysisModule
specifier|public
specifier|final
class|class
name|AnalysisModule
extends|extends
name|AbstractModule
block|{
static|static
block|{
name|Settings
name|build
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
name|IndexMetaData
operator|.
name|SETTING_NUMBER_OF_REPLICAS
argument_list|,
literal|1
argument_list|)
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_NUMBER_OF_SHARDS
argument_list|,
literal|1
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|IndexMetaData
name|metaData
init|=
name|IndexMetaData
operator|.
name|builder
argument_list|(
literal|"_na_"
argument_list|)
operator|.
name|settings
argument_list|(
name|build
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|NA_INDEX_SETTINGS
operator|=
operator|new
name|IndexSettings
argument_list|(
name|metaData
argument_list|,
name|Settings
operator|.
name|EMPTY
argument_list|)
expr_stmt|;
block|}
DECL|field|NA_INDEX_SETTINGS
specifier|private
specifier|static
specifier|final
name|IndexSettings
name|NA_INDEX_SETTINGS
decl_stmt|;
DECL|field|environment
specifier|private
specifier|final
name|Environment
name|environment
decl_stmt|;
DECL|field|charFilters
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|AnalysisProvider
argument_list|<
name|CharFilterFactory
argument_list|>
argument_list|>
name|charFilters
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
DECL|field|tokenFilters
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|AnalysisProvider
argument_list|<
name|TokenFilterFactory
argument_list|>
argument_list|>
name|tokenFilters
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
DECL|field|tokenizers
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|AnalysisProvider
argument_list|<
name|TokenizerFactory
argument_list|>
argument_list|>
name|tokenizers
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
DECL|field|analyzers
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|AnalysisProvider
argument_list|<
name|AnalyzerProvider
argument_list|>
argument_list|>
name|analyzers
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
DECL|field|knownDictionaries
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|analysis
operator|.
name|hunspell
operator|.
name|Dictionary
argument_list|>
name|knownDictionaries
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
comment|/**      * Creates a new AnalysisModule      */
DECL|method|AnalysisModule
specifier|public
name|AnalysisModule
parameter_list|(
name|Environment
name|environment
parameter_list|)
block|{
name|this
operator|.
name|environment
operator|=
name|environment
expr_stmt|;
block|}
comment|/**      * Registers a new {@link AnalysisProvider} to create      * {@link CharFilterFactory} instance per node as well as per index.      */
DECL|method|registerCharFilter
specifier|public
name|void
name|registerCharFilter
parameter_list|(
name|String
name|name
parameter_list|,
name|AnalysisProvider
argument_list|<
name|CharFilterFactory
argument_list|>
name|charFilter
parameter_list|)
block|{
if|if
condition|(
name|charFilter
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"char_filter provider must not be null"
argument_list|)
throw|;
block|}
if|if
condition|(
name|charFilters
operator|.
name|putIfAbsent
argument_list|(
name|name
argument_list|,
name|charFilter
argument_list|)
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"char_filter provider for name "
operator|+
name|name
operator|+
literal|" already registered"
argument_list|)
throw|;
block|}
block|}
comment|/**      * Registers a new {@link AnalysisProvider} to create      * {@link TokenFilterFactory} instance per node as well as per index.      */
DECL|method|registerTokenFilter
specifier|public
name|void
name|registerTokenFilter
parameter_list|(
name|String
name|name
parameter_list|,
name|AnalysisProvider
argument_list|<
name|TokenFilterFactory
argument_list|>
name|tokenFilter
parameter_list|)
block|{
if|if
condition|(
name|tokenFilter
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"token_filter provider must not be null"
argument_list|)
throw|;
block|}
if|if
condition|(
name|tokenFilters
operator|.
name|putIfAbsent
argument_list|(
name|name
argument_list|,
name|tokenFilter
argument_list|)
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"token_filter provider for name "
operator|+
name|name
operator|+
literal|" already registered"
argument_list|)
throw|;
block|}
block|}
comment|/**      * Registers a new {@link AnalysisProvider} to create      * {@link TokenizerFactory} instance per node as well as per index.      */
DECL|method|registerTokenizer
specifier|public
name|void
name|registerTokenizer
parameter_list|(
name|String
name|name
parameter_list|,
name|AnalysisProvider
argument_list|<
name|TokenizerFactory
argument_list|>
name|tokenizer
parameter_list|)
block|{
if|if
condition|(
name|tokenizer
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"tokenizer provider must not be null"
argument_list|)
throw|;
block|}
if|if
condition|(
name|tokenizers
operator|.
name|putIfAbsent
argument_list|(
name|name
argument_list|,
name|tokenizer
argument_list|)
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"tokenizer provider for name "
operator|+
name|name
operator|+
literal|" already registered"
argument_list|)
throw|;
block|}
block|}
comment|/**      * Registers a new {@link AnalysisProvider} to create      * {@link AnalyzerProvider} instance per node as well as per index.      */
DECL|method|registerAnalyzer
specifier|public
name|void
name|registerAnalyzer
parameter_list|(
name|String
name|name
parameter_list|,
name|AnalysisProvider
argument_list|<
name|AnalyzerProvider
argument_list|>
name|analyzer
parameter_list|)
block|{
if|if
condition|(
name|analyzer
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"analyzer provider must not be null"
argument_list|)
throw|;
block|}
if|if
condition|(
name|analyzers
operator|.
name|putIfAbsent
argument_list|(
name|name
argument_list|,
name|analyzer
argument_list|)
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"analyzer provider for name "
operator|+
name|name
operator|+
literal|" already registered"
argument_list|)
throw|;
block|}
block|}
comment|/**      * Registers a new hunspell {@link Dictionary} that can be referenced by the given name in      * hunspell analysis configuration.      */
DECL|method|registerHunspellDictionary
specifier|public
name|void
name|registerHunspellDictionary
parameter_list|(
name|String
name|name
parameter_list|,
name|Dictionary
name|dictionary
parameter_list|)
block|{
if|if
condition|(
name|knownDictionaries
operator|.
name|putIfAbsent
argument_list|(
name|name
argument_list|,
name|dictionary
argument_list|)
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"dictionary for ["
operator|+
name|name
operator|+
literal|"] is already registered"
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
DECL|method|configure
specifier|protected
name|void
name|configure
parameter_list|()
block|{
try|try
block|{
name|AnalysisRegistry
name|registry
init|=
name|buildRegistry
argument_list|()
decl_stmt|;
name|bind
argument_list|(
name|HunspellService
operator|.
name|class
argument_list|)
operator|.
name|toInstance
argument_list|(
name|registry
operator|.
name|getHunspellService
argument_list|()
argument_list|)
expr_stmt|;
name|bind
argument_list|(
name|AnalysisRegistry
operator|.
name|class
argument_list|)
operator|.
name|toInstance
argument_list|(
name|registry
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticsearchException
argument_list|(
literal|"failed to load hunspell service"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**      * Builds an {@link AnalysisRegistry} from the current configuration.      */
DECL|method|buildRegistry
specifier|public
name|AnalysisRegistry
name|buildRegistry
parameter_list|()
throws|throws
name|IOException
block|{
return|return
operator|new
name|AnalysisRegistry
argument_list|(
operator|new
name|HunspellService
argument_list|(
name|environment
operator|.
name|settings
argument_list|()
argument_list|,
name|environment
argument_list|,
name|knownDictionaries
argument_list|)
argument_list|,
name|environment
argument_list|,
name|charFilters
argument_list|,
name|tokenFilters
argument_list|,
name|tokenizers
argument_list|,
name|analyzers
argument_list|)
return|;
block|}
comment|/**      * AnalysisProvider is the basic factory interface for registering analysis components like:      *<ul>      *<li>{@link TokenizerFactory} - see {@link AnalysisModule#registerTokenizer(String, AnalysisProvider)}</li>      *<li>{@link CharFilterFactory} - see {@link AnalysisModule#registerCharFilter(String, AnalysisProvider)}</li>      *<li>{@link AnalyzerProvider} - see {@link AnalysisModule#registerAnalyzer(String, AnalysisProvider)}</li>      *<li>{@link TokenFilterFactory}- see {@link AnalysisModule#registerTokenFilter(String, AnalysisProvider)} )}</li>      *</ul>      */
DECL|interface|AnalysisProvider
specifier|public
interface|interface
name|AnalysisProvider
parameter_list|<
name|T
parameter_list|>
block|{
comment|/**          * Creates a new analysis provider.          * @param indexSettings the index settings for the index this provider is created for          * @param environment the nodes environment to load resources from persistent storage          * @param name the name of the analysis component          * @param settings the component specific settings without context prefixes          * @return a new provider instance          * @throws IOException if an {@link IOException} occurs          */
DECL|method|get
name|T
name|get
parameter_list|(
name|IndexSettings
name|indexSettings
parameter_list|,
name|Environment
name|environment
parameter_list|,
name|String
name|name
parameter_list|,
name|Settings
name|settings
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**          * Creates a new global scope analysis provider without index specific settings not settings for the provider itself.          * This can be used to get a default instance of an analysis factory without binding to an index.          *          * @param environment the nodes environment to load resources from persistent storage          * @param name the name of the analysis component          * @return a new provider instance          * @throws IOException if an {@link IOException} occurs          * @throws IllegalArgumentException if the provider requires analysis settings ie. if {@link #requiresAnalysisSettings()} returns<code>true</code>          */
DECL|method|get
specifier|default
name|T
name|get
parameter_list|(
name|Environment
name|environment
parameter_list|,
name|String
name|name
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|requiresAnalysisSettings
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Analysis settings required - can't instantiate analysis factory"
argument_list|)
throw|;
block|}
return|return
name|get
argument_list|(
name|NA_INDEX_SETTINGS
argument_list|,
name|environment
argument_list|,
name|name
argument_list|,
name|NA_INDEX_SETTINGS
operator|.
name|getSettings
argument_list|()
argument_list|)
return|;
block|}
comment|/**          * If<code>true</code> the analysis component created by this provider requires certain settings to be instantiated.          * it can't be created with defaults. The default is<code>false</code>.          */
DECL|method|requiresAnalysisSettings
specifier|default
name|boolean
name|requiresAnalysisSettings
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
block|}
block|}
end_class

end_unit

