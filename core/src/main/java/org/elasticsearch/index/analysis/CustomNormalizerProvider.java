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
name|PreBuiltTokenizers
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
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

begin_comment
comment|/**  * A custom normalizer that is built out of a char and token filters. On the  * contrary to analyzers, it does not support tokenizers and only supports a  * subset of char and token filters.  */
end_comment

begin_class
DECL|class|CustomNormalizerProvider
specifier|public
specifier|final
class|class
name|CustomNormalizerProvider
extends|extends
name|AbstractIndexAnalyzerProvider
argument_list|<
name|CustomAnalyzer
argument_list|>
block|{
DECL|field|analyzerSettings
specifier|private
specifier|final
name|Settings
name|analyzerSettings
decl_stmt|;
DECL|field|customAnalyzer
specifier|private
name|CustomAnalyzer
name|customAnalyzer
decl_stmt|;
DECL|method|CustomNormalizerProvider
specifier|public
name|CustomNormalizerProvider
parameter_list|(
name|IndexSettings
name|indexSettings
parameter_list|,
name|String
name|name
parameter_list|,
name|Settings
name|settings
parameter_list|)
block|{
name|super
argument_list|(
name|indexSettings
argument_list|,
name|name
argument_list|,
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|analyzerSettings
operator|=
name|settings
expr_stmt|;
block|}
DECL|method|build
specifier|public
name|void
name|build
parameter_list|(
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|CharFilterFactory
argument_list|>
name|charFilters
parameter_list|,
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|TokenFilterFactory
argument_list|>
name|tokenFilters
parameter_list|)
block|{
name|String
name|tokenizerName
init|=
name|analyzerSettings
operator|.
name|get
argument_list|(
literal|"tokenizer"
argument_list|)
decl_stmt|;
if|if
condition|(
name|tokenizerName
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Custom normalizer ["
operator|+
name|name
argument_list|()
operator|+
literal|"] cannot configure a tokenizer"
argument_list|)
throw|;
block|}
name|String
index|[]
name|charFilterNames
init|=
name|analyzerSettings
operator|.
name|getAsArray
argument_list|(
literal|"char_filter"
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|CharFilterFactory
argument_list|>
name|charFiltersList
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|charFilterNames
operator|.
name|length
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|charFilterName
range|:
name|charFilterNames
control|)
block|{
name|CharFilterFactory
name|charFilter
init|=
name|charFilters
operator|.
name|get
argument_list|(
name|charFilterName
argument_list|)
decl_stmt|;
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
literal|"Custom normalizer ["
operator|+
name|name
argument_list|()
operator|+
literal|"] failed to find char_filter under name ["
operator|+
name|charFilterName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
if|if
condition|(
name|charFilter
operator|instanceof
name|MultiTermAwareComponent
operator|==
literal|false
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Custom normalizer ["
operator|+
name|name
argument_list|()
operator|+
literal|"] may not use char filter ["
operator|+
name|charFilterName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|charFilter
operator|=
call|(
name|CharFilterFactory
call|)
argument_list|(
operator|(
name|MultiTermAwareComponent
operator|)
name|charFilter
argument_list|)
operator|.
name|getMultiTermComponent
argument_list|()
expr_stmt|;
name|charFiltersList
operator|.
name|add
argument_list|(
name|charFilter
argument_list|)
expr_stmt|;
block|}
name|String
index|[]
name|tokenFilterNames
init|=
name|analyzerSettings
operator|.
name|getAsArray
argument_list|(
literal|"filter"
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|TokenFilterFactory
argument_list|>
name|tokenFilterList
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|tokenFilterNames
operator|.
name|length
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|tokenFilterName
range|:
name|tokenFilterNames
control|)
block|{
name|TokenFilterFactory
name|tokenFilter
init|=
name|tokenFilters
operator|.
name|get
argument_list|(
name|tokenFilterName
argument_list|)
decl_stmt|;
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
literal|"Custom Analyzer ["
operator|+
name|name
argument_list|()
operator|+
literal|"] failed to find filter under name ["
operator|+
name|tokenFilterName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
if|if
condition|(
name|tokenFilter
operator|instanceof
name|MultiTermAwareComponent
operator|==
literal|false
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Custom normalizer ["
operator|+
name|name
argument_list|()
operator|+
literal|"] may not use filter ["
operator|+
name|tokenFilterName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|tokenFilter
operator|=
call|(
name|TokenFilterFactory
call|)
argument_list|(
operator|(
name|MultiTermAwareComponent
operator|)
name|tokenFilter
argument_list|)
operator|.
name|getMultiTermComponent
argument_list|()
expr_stmt|;
name|tokenFilterList
operator|.
name|add
argument_list|(
name|tokenFilter
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|customAnalyzer
operator|=
operator|new
name|CustomAnalyzer
argument_list|(
literal|"keyword"
argument_list|,
name|PreBuiltTokenizers
operator|.
name|KEYWORD
operator|.
name|getTokenizerFactory
argument_list|(
name|indexSettings
operator|.
name|getIndexVersionCreated
argument_list|()
argument_list|)
argument_list|,
name|charFiltersList
operator|.
name|toArray
argument_list|(
operator|new
name|CharFilterFactory
index|[
name|charFiltersList
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|,
name|tokenFilterList
operator|.
name|toArray
argument_list|(
operator|new
name|TokenFilterFactory
index|[
name|tokenFilterList
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|get
specifier|public
name|CustomAnalyzer
name|get
parameter_list|()
block|{
return|return
name|this
operator|.
name|customAnalyzer
return|;
block|}
block|}
end_class

end_unit

