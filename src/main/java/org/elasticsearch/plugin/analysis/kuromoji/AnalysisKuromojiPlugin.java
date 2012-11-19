begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.plugin.analysis.kuromoji
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|plugin
operator|.
name|analysis
operator|.
name|kuromoji
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
name|collect
operator|.
name|ImmutableList
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
name|Module
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
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|analysis
operator|.
name|KuromojiIndicesAnalysisModule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|plugins
operator|.
name|AbstractPlugin
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|AnalysisKuromojiPlugin
specifier|public
class|class
name|AnalysisKuromojiPlugin
extends|extends
name|AbstractPlugin
block|{
annotation|@
name|Override
DECL|method|name
specifier|public
name|String
name|name
parameter_list|()
block|{
return|return
literal|"analysis-kuromoji"
return|;
block|}
annotation|@
name|Override
DECL|method|description
specifier|public
name|String
name|description
parameter_list|()
block|{
return|return
literal|"Kuromoji analysis support"
return|;
block|}
annotation|@
name|Override
DECL|method|modules
specifier|public
name|Collection
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|Module
argument_list|>
argument_list|>
name|modules
parameter_list|()
block|{
return|return
name|ImmutableList
operator|.
expr|<
name|Class
argument_list|<
name|?
extends|extends
name|Module
argument_list|>
operator|>
name|of
argument_list|(
name|KuromojiIndicesAnalysisModule
operator|.
name|class
argument_list|)
return|;
block|}
DECL|method|onModule
specifier|public
name|void
name|onModule
parameter_list|(
name|AnalysisModule
name|module
parameter_list|)
block|{
name|module
operator|.
name|addAnalyzer
argument_list|(
literal|"kuromoji"
argument_list|,
name|KuromojiAnalyzerProvider
operator|.
name|class
argument_list|)
expr_stmt|;
name|module
operator|.
name|addTokenizer
argument_list|(
literal|"kuromoji_tokenizer"
argument_list|,
name|KuromojiTokenizerFactory
operator|.
name|class
argument_list|)
expr_stmt|;
name|module
operator|.
name|addTokenFilter
argument_list|(
literal|"kuromoji_baseform"
argument_list|,
name|KuromojiBaseFormFilterFactory
operator|.
name|class
argument_list|)
expr_stmt|;
name|module
operator|.
name|addTokenFilter
argument_list|(
literal|"kuromoji_part_of_speech"
argument_list|,
name|KuromojiPartOfSpeechFilterFactory
operator|.
name|class
argument_list|)
expr_stmt|;
name|module
operator|.
name|addTokenFilter
argument_list|(
literal|"kuromoji_readingform"
argument_list|,
name|KuromojiReadingFormFilterFactory
operator|.
name|class
argument_list|)
expr_stmt|;
name|module
operator|.
name|addTokenFilter
argument_list|(
literal|"kuromoji_stemmer"
argument_list|,
name|KuromojiKatakanaStemmerFactory
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

