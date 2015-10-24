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
name|TokenStream
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
name|cjk
operator|.
name|CJKBigramFilter
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
name|inject
operator|.
name|assistedinject
operator|.
name|Assisted
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
name|IndexSettings
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_comment
comment|/**  * Factory that creates a {@link CJKBigramFilter} to form bigrams of CJK terms  * that are generated from StandardTokenizer or ICUTokenizer.  *<p>  * CJK types are set by these tokenizers, but you can also use flags to  * explicitly control which of the CJK scripts are turned into bigrams.  *<p>  * By default, when a CJK character has no adjacent characters to form a bigram,  * it is output in unigram form. If you want to always output both unigrams and  * bigrams, set the<code>outputUnigrams</code> flag. This can be used for a  * combined unigram+bigram approach.  *<p>  * In all cases, all non-CJK input is passed thru unmodified.  */
end_comment

begin_class
DECL|class|CJKBigramFilterFactory
specifier|public
specifier|final
class|class
name|CJKBigramFilterFactory
extends|extends
name|AbstractTokenFilterFactory
block|{
DECL|field|flags
specifier|private
specifier|final
name|int
name|flags
decl_stmt|;
DECL|field|outputUnigrams
specifier|private
specifier|final
name|boolean
name|outputUnigrams
decl_stmt|;
annotation|@
name|Inject
DECL|method|CJKBigramFilterFactory
specifier|public
name|CJKBigramFilterFactory
parameter_list|(
name|IndexSettings
name|indexSettings
parameter_list|,
annotation|@
name|Assisted
name|String
name|name
parameter_list|,
annotation|@
name|Assisted
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
name|outputUnigrams
operator|=
name|settings
operator|.
name|getAsBoolean
argument_list|(
literal|"output_unigrams"
argument_list|,
literal|false
argument_list|)
expr_stmt|;
specifier|final
name|String
index|[]
name|asArray
init|=
name|settings
operator|.
name|getAsArray
argument_list|(
literal|"ignored_scripts"
argument_list|)
decl_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|scripts
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"han"
argument_list|,
literal|"hiragana"
argument_list|,
literal|"katakana"
argument_list|,
literal|"hangul"
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|asArray
operator|!=
literal|null
condition|)
block|{
name|scripts
operator|.
name|removeAll
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|asArray
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|int
name|flags
init|=
literal|0
decl_stmt|;
for|for
control|(
name|String
name|script
range|:
name|scripts
control|)
block|{
if|if
condition|(
literal|"han"
operator|.
name|equals
argument_list|(
name|script
argument_list|)
condition|)
block|{
name|flags
operator||=
name|CJKBigramFilter
operator|.
name|HAN
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"hiragana"
operator|.
name|equals
argument_list|(
name|script
argument_list|)
condition|)
block|{
name|flags
operator||=
name|CJKBigramFilter
operator|.
name|HIRAGANA
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"katakana"
operator|.
name|equals
argument_list|(
name|script
argument_list|)
condition|)
block|{
name|flags
operator||=
name|CJKBigramFilter
operator|.
name|KATAKANA
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"hangul"
operator|.
name|equals
argument_list|(
name|script
argument_list|)
condition|)
block|{
name|flags
operator||=
name|CJKBigramFilter
operator|.
name|HANGUL
expr_stmt|;
block|}
block|}
name|this
operator|.
name|flags
operator|=
name|flags
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|create
specifier|public
name|TokenStream
name|create
parameter_list|(
name|TokenStream
name|tokenStream
parameter_list|)
block|{
return|return
operator|new
name|CJKBigramFilter
argument_list|(
name|tokenStream
argument_list|,
name|flags
argument_list|,
name|outputUnigrams
argument_list|)
return|;
block|}
block|}
end_class

end_unit

