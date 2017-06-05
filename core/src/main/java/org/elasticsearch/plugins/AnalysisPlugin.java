begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.plugins
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|plugins
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
name|CharFilter
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
name|TokenFilter
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
name|Tokenizer
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
name|PreConfiguredCharFilter
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
name|PreConfiguredTokenFilter
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
name|PreConfiguredTokenizer
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
name|List
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
name|emptyList
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

begin_comment
comment|/**  * An additional extension point for {@link Plugin}s that extends Elasticsearch's analysis functionality. To add an additional  * {@link TokenFilter} just implement the interface and implement the {@link #getTokenFilters()} method:  *  *<pre>{@code  * public class AnalysisPhoneticPlugin extends Plugin implements AnalysisPlugin {  *&#64;Override  *     public Map<String, AnalysisProvider<TokenFilterFactory>> getTokenFilters() {  *         return singletonMap("phonetic", PhoneticTokenFilterFactory::new);  *     }  * }  * }</pre>  *  * Elasticsearch doesn't have any automatic mechanism to share these components between indexes. If any component is heavy enough to warrant  * such sharing then it is the Pugin's responsibility to do it in their {@link AnalysisProvider} implementation. We recommend against doing  * this unless absolutely necessary because it can be difficult to get the caching right given things like behavior changes across versions.  */
end_comment

begin_interface
DECL|interface|AnalysisPlugin
specifier|public
interface|interface
name|AnalysisPlugin
block|{
comment|/**      * Override to add additional {@link CharFilter}s. See {@link #requriesAnalysisSettings(AnalysisProvider)}      * how to on get the configuration from the index.      */
DECL|method|getCharFilters
specifier|default
name|Map
argument_list|<
name|String
argument_list|,
name|AnalysisProvider
argument_list|<
name|CharFilterFactory
argument_list|>
argument_list|>
name|getCharFilters
parameter_list|()
block|{
return|return
name|emptyMap
argument_list|()
return|;
block|}
comment|/**      * Override to add additional {@link TokenFilter}s. See {@link #requriesAnalysisSettings(AnalysisProvider)}      * how to on get the configuration from the index.      */
DECL|method|getTokenFilters
specifier|default
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
name|emptyMap
argument_list|()
return|;
block|}
comment|/**      * Override to add additional {@link Tokenizer}s. See {@link #requriesAnalysisSettings(AnalysisProvider)}      * how to on get the configuration from the index.      */
DECL|method|getTokenizers
specifier|default
name|Map
argument_list|<
name|String
argument_list|,
name|AnalysisProvider
argument_list|<
name|TokenizerFactory
argument_list|>
argument_list|>
name|getTokenizers
parameter_list|()
block|{
return|return
name|emptyMap
argument_list|()
return|;
block|}
comment|/**      * Override to add additional {@link Analyzer}s. See {@link #requriesAnalysisSettings(AnalysisProvider)}      * how to on get the configuration from the index.      */
DECL|method|getAnalyzers
specifier|default
name|Map
argument_list|<
name|String
argument_list|,
name|AnalysisProvider
argument_list|<
name|AnalyzerProvider
argument_list|<
name|?
extends|extends
name|Analyzer
argument_list|>
argument_list|>
argument_list|>
name|getAnalyzers
parameter_list|()
block|{
return|return
name|emptyMap
argument_list|()
return|;
block|}
comment|/**      * Override to add additional pre-configured {@link CharFilter}s.      */
DECL|method|getPreConfiguredCharFilters
specifier|default
name|List
argument_list|<
name|PreConfiguredCharFilter
argument_list|>
name|getPreConfiguredCharFilters
parameter_list|()
block|{
return|return
name|emptyList
argument_list|()
return|;
block|}
comment|/**      * Override to add additional pre-configured {@link TokenFilter}s.      */
DECL|method|getPreConfiguredTokenFilters
specifier|default
name|List
argument_list|<
name|PreConfiguredTokenFilter
argument_list|>
name|getPreConfiguredTokenFilters
parameter_list|()
block|{
return|return
name|emptyList
argument_list|()
return|;
block|}
comment|/**      * Override to add additional pre-configured {@link Tokenizer}.      */
DECL|method|getPreConfiguredTokenizers
specifier|default
name|List
argument_list|<
name|PreConfiguredTokenizer
argument_list|>
name|getPreConfiguredTokenizers
parameter_list|()
block|{
return|return
name|emptyList
argument_list|()
return|;
block|}
comment|/**      * Override to add additional hunspell {@link org.apache.lucene.analysis.hunspell.Dictionary}s.      */
DECL|method|getHunspellDictionaries
specifier|default
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
name|getHunspellDictionaries
parameter_list|()
block|{
return|return
name|emptyMap
argument_list|()
return|;
block|}
comment|/**      * Mark an {@link AnalysisProvider} as requiring the index's settings.      */
DECL|method|requriesAnalysisSettings
specifier|static
parameter_list|<
name|T
parameter_list|>
name|AnalysisProvider
argument_list|<
name|T
argument_list|>
name|requriesAnalysisSettings
parameter_list|(
name|AnalysisProvider
argument_list|<
name|T
argument_list|>
name|provider
parameter_list|)
block|{
return|return
operator|new
name|AnalysisProvider
argument_list|<
name|T
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
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
block|{
return|return
name|provider
operator|.
name|get
argument_list|(
name|indexSettings
argument_list|,
name|environment
argument_list|,
name|name
argument_list|,
name|settings
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|requiresAnalysisSettings
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
block|}
return|;
block|}
block|}
end_interface

end_unit

