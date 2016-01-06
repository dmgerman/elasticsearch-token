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
name|commons
operator|.
name|codec
operator|.
name|Encoder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|codec
operator|.
name|language
operator|.
name|Caverphone1
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|codec
operator|.
name|language
operator|.
name|Caverphone2
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|codec
operator|.
name|language
operator|.
name|ColognePhonetic
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|codec
operator|.
name|language
operator|.
name|DaitchMokotoffSoundex
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|codec
operator|.
name|language
operator|.
name|Metaphone
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|codec
operator|.
name|language
operator|.
name|RefinedSoundex
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|codec
operator|.
name|language
operator|.
name|Soundex
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|codec
operator|.
name|language
operator|.
name|bm
operator|.
name|Languages
operator|.
name|LanguageSet
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|codec
operator|.
name|language
operator|.
name|bm
operator|.
name|NameType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|codec
operator|.
name|language
operator|.
name|bm
operator|.
name|PhoneticEngine
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|codec
operator|.
name|language
operator|.
name|bm
operator|.
name|RuleType
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
name|phonetic
operator|.
name|BeiderMorseFilter
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
name|phonetic
operator|.
name|DoubleMetaphoneFilter
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
name|phonetic
operator|.
name|PhoneticFilter
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
name|phonetic
operator|.
name|HaasePhonetik
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
name|phonetic
operator|.
name|KoelnerPhonetik
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
name|phonetic
operator|.
name|Nysiis
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

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|PhoneticTokenFilterFactory
specifier|public
class|class
name|PhoneticTokenFilterFactory
extends|extends
name|AbstractTokenFilterFactory
block|{
DECL|field|encoder
specifier|private
specifier|final
name|Encoder
name|encoder
decl_stmt|;
DECL|field|replace
specifier|private
specifier|final
name|boolean
name|replace
decl_stmt|;
DECL|field|maxcodelength
specifier|private
name|int
name|maxcodelength
decl_stmt|;
DECL|field|languageset
specifier|private
name|String
index|[]
name|languageset
decl_stmt|;
DECL|field|nametype
specifier|private
name|NameType
name|nametype
decl_stmt|;
DECL|field|ruletype
specifier|private
name|RuleType
name|ruletype
decl_stmt|;
annotation|@
name|Inject
DECL|method|PhoneticTokenFilterFactory
specifier|public
name|PhoneticTokenFilterFactory
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
name|languageset
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|nametype
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|ruletype
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|maxcodelength
operator|=
literal|0
expr_stmt|;
name|this
operator|.
name|replace
operator|=
name|settings
operator|.
name|getAsBoolean
argument_list|(
literal|"replace"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|// weird, encoder is null at last step in SimplePhoneticAnalysisTests, so we set it to metaphone as default
name|String
name|encodername
init|=
name|settings
operator|.
name|get
argument_list|(
literal|"encoder"
argument_list|,
literal|"metaphone"
argument_list|)
decl_stmt|;
if|if
condition|(
literal|"metaphone"
operator|.
name|equalsIgnoreCase
argument_list|(
name|encodername
argument_list|)
condition|)
block|{
name|this
operator|.
name|encoder
operator|=
operator|new
name|Metaphone
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"soundex"
operator|.
name|equalsIgnoreCase
argument_list|(
name|encodername
argument_list|)
condition|)
block|{
name|this
operator|.
name|encoder
operator|=
operator|new
name|Soundex
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"caverphone1"
operator|.
name|equalsIgnoreCase
argument_list|(
name|encodername
argument_list|)
condition|)
block|{
name|this
operator|.
name|encoder
operator|=
operator|new
name|Caverphone1
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"caverphone2"
operator|.
name|equalsIgnoreCase
argument_list|(
name|encodername
argument_list|)
condition|)
block|{
name|this
operator|.
name|encoder
operator|=
operator|new
name|Caverphone2
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"caverphone"
operator|.
name|equalsIgnoreCase
argument_list|(
name|encodername
argument_list|)
condition|)
block|{
name|this
operator|.
name|encoder
operator|=
operator|new
name|Caverphone2
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"refined_soundex"
operator|.
name|equalsIgnoreCase
argument_list|(
name|encodername
argument_list|)
operator|||
literal|"refinedSoundex"
operator|.
name|equalsIgnoreCase
argument_list|(
name|encodername
argument_list|)
condition|)
block|{
name|this
operator|.
name|encoder
operator|=
operator|new
name|RefinedSoundex
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"cologne"
operator|.
name|equalsIgnoreCase
argument_list|(
name|encodername
argument_list|)
condition|)
block|{
name|this
operator|.
name|encoder
operator|=
operator|new
name|ColognePhonetic
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"double_metaphone"
operator|.
name|equalsIgnoreCase
argument_list|(
name|encodername
argument_list|)
operator|||
literal|"doubleMetaphone"
operator|.
name|equalsIgnoreCase
argument_list|(
name|encodername
argument_list|)
condition|)
block|{
name|this
operator|.
name|encoder
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|maxcodelength
operator|=
name|settings
operator|.
name|getAsInt
argument_list|(
literal|"max_code_len"
argument_list|,
literal|4
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"bm"
operator|.
name|equalsIgnoreCase
argument_list|(
name|encodername
argument_list|)
operator|||
literal|"beider_morse"
operator|.
name|equalsIgnoreCase
argument_list|(
name|encodername
argument_list|)
operator|||
literal|"beidermorse"
operator|.
name|equalsIgnoreCase
argument_list|(
name|encodername
argument_list|)
condition|)
block|{
name|this
operator|.
name|encoder
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|languageset
operator|=
name|settings
operator|.
name|getAsArray
argument_list|(
literal|"languageset"
argument_list|)
expr_stmt|;
name|String
name|ruleType
init|=
name|settings
operator|.
name|get
argument_list|(
literal|"rule_type"
argument_list|,
literal|"approx"
argument_list|)
decl_stmt|;
if|if
condition|(
literal|"approx"
operator|.
name|equalsIgnoreCase
argument_list|(
name|ruleType
argument_list|)
condition|)
block|{
name|ruletype
operator|=
name|RuleType
operator|.
name|APPROX
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"exact"
operator|.
name|equalsIgnoreCase
argument_list|(
name|ruleType
argument_list|)
condition|)
block|{
name|ruletype
operator|=
name|RuleType
operator|.
name|EXACT
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"No matching rule type ["
operator|+
name|ruleType
operator|+
literal|"] for beider morse encoder"
argument_list|)
throw|;
block|}
name|String
name|nameType
init|=
name|settings
operator|.
name|get
argument_list|(
literal|"name_type"
argument_list|,
literal|"generic"
argument_list|)
decl_stmt|;
if|if
condition|(
literal|"GENERIC"
operator|.
name|equalsIgnoreCase
argument_list|(
name|nameType
argument_list|)
condition|)
block|{
name|nametype
operator|=
name|NameType
operator|.
name|GENERIC
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"ASHKENAZI"
operator|.
name|equalsIgnoreCase
argument_list|(
name|nameType
argument_list|)
condition|)
block|{
name|nametype
operator|=
name|NameType
operator|.
name|ASHKENAZI
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"SEPHARDIC"
operator|.
name|equalsIgnoreCase
argument_list|(
name|nameType
argument_list|)
condition|)
block|{
name|nametype
operator|=
name|NameType
operator|.
name|SEPHARDIC
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
literal|"koelnerphonetik"
operator|.
name|equalsIgnoreCase
argument_list|(
name|encodername
argument_list|)
condition|)
block|{
name|this
operator|.
name|encoder
operator|=
operator|new
name|KoelnerPhonetik
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"haasephonetik"
operator|.
name|equalsIgnoreCase
argument_list|(
name|encodername
argument_list|)
condition|)
block|{
name|this
operator|.
name|encoder
operator|=
operator|new
name|HaasePhonetik
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"nysiis"
operator|.
name|equalsIgnoreCase
argument_list|(
name|encodername
argument_list|)
condition|)
block|{
name|this
operator|.
name|encoder
operator|=
operator|new
name|Nysiis
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"daitch_mokotoff"
operator|.
name|equalsIgnoreCase
argument_list|(
name|encodername
argument_list|)
condition|)
block|{
name|this
operator|.
name|encoder
operator|=
operator|new
name|DaitchMokotoffSoundex
argument_list|()
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"unknown encoder ["
operator|+
name|encodername
operator|+
literal|"] for phonetic token filter"
argument_list|)
throw|;
block|}
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
if|if
condition|(
name|encoder
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|ruletype
operator|!=
literal|null
operator|&&
name|nametype
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|languageset
operator|!=
literal|null
condition|)
block|{
specifier|final
name|LanguageSet
name|languages
init|=
name|LanguageSet
operator|.
name|from
argument_list|(
operator|new
name|HashSet
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|languageset
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
return|return
operator|new
name|BeiderMorseFilter
argument_list|(
name|tokenStream
argument_list|,
operator|new
name|PhoneticEngine
argument_list|(
name|nametype
argument_list|,
name|ruletype
argument_list|,
literal|true
argument_list|)
argument_list|,
name|languages
argument_list|)
return|;
block|}
return|return
operator|new
name|BeiderMorseFilter
argument_list|(
name|tokenStream
argument_list|,
operator|new
name|PhoneticEngine
argument_list|(
name|nametype
argument_list|,
name|ruletype
argument_list|,
literal|true
argument_list|)
argument_list|)
return|;
block|}
if|if
condition|(
name|maxcodelength
operator|>
literal|0
condition|)
block|{
return|return
operator|new
name|DoubleMetaphoneFilter
argument_list|(
name|tokenStream
argument_list|,
name|maxcodelength
argument_list|,
operator|!
name|replace
argument_list|)
return|;
block|}
block|}
else|else
block|{
return|return
operator|new
name|PhoneticFilter
argument_list|(
name|tokenStream
argument_list|,
name|encoder
argument_list|,
operator|!
name|replace
argument_list|)
return|;
block|}
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"encoder error"
argument_list|)
throw|;
block|}
block|}
end_class

end_unit

