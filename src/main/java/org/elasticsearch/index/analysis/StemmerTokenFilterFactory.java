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
name|ar
operator|.
name|ArabicStemFilter
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
name|bg
operator|.
name|BulgarianStemFilter
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
name|br
operator|.
name|BrazilianStemFilter
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
name|ckb
operator|.
name|SoraniStemFilter
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
name|cz
operator|.
name|CzechStemFilter
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
name|de
operator|.
name|GermanLightStemFilter
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
name|de
operator|.
name|GermanMinimalStemFilter
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
name|el
operator|.
name|GreekStemFilter
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
name|en
operator|.
name|EnglishMinimalStemFilter
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
name|en
operator|.
name|EnglishPossessiveFilter
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
name|en
operator|.
name|KStemFilter
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
name|en
operator|.
name|PorterStemFilter
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
name|es
operator|.
name|SpanishLightStemFilter
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
name|fi
operator|.
name|FinnishLightStemFilter
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
name|fr
operator|.
name|FrenchLightStemFilter
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
name|fr
operator|.
name|FrenchMinimalStemFilter
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
name|gl
operator|.
name|GalicianMinimalStemFilter
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
name|gl
operator|.
name|GalicianStemFilter
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
name|hi
operator|.
name|HindiStemFilter
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
name|hu
operator|.
name|HungarianLightStemFilter
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
name|id
operator|.
name|IndonesianStemFilter
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
name|it
operator|.
name|ItalianLightStemFilter
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
name|lv
operator|.
name|LatvianStemFilter
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
name|no
operator|.
name|NorwegianLightStemFilter
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
name|no
operator|.
name|NorwegianLightStemmer
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
name|no
operator|.
name|NorwegianMinimalStemFilter
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
name|pt
operator|.
name|PortugueseLightStemFilter
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
name|pt
operator|.
name|PortugueseMinimalStemFilter
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
name|pt
operator|.
name|PortugueseStemFilter
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
name|ru
operator|.
name|RussianLightStemFilter
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
name|snowball
operator|.
name|SnowballFilter
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
name|sv
operator|.
name|SwedishLightStemFilter
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
name|settings
operator|.
name|IndexSettings
import|;
end_import

begin_import
import|import
name|org
operator|.
name|tartarus
operator|.
name|snowball
operator|.
name|ext
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|StemmerTokenFilterFactory
specifier|public
class|class
name|StemmerTokenFilterFactory
extends|extends
name|AbstractTokenFilterFactory
block|{
DECL|field|language
specifier|private
name|String
name|language
decl_stmt|;
annotation|@
name|Inject
DECL|method|StemmerTokenFilterFactory
specifier|public
name|StemmerTokenFilterFactory
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
name|index
argument_list|,
name|indexSettings
argument_list|,
name|name
argument_list|,
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|language
operator|=
name|Strings
operator|.
name|capitalize
argument_list|(
name|settings
operator|.
name|get
argument_list|(
literal|"language"
argument_list|,
name|settings
operator|.
name|get
argument_list|(
literal|"name"
argument_list|,
literal|"porter"
argument_list|)
argument_list|)
argument_list|)
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
specifier|final
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
literal|"arabic"
operator|.
name|equalsIgnoreCase
argument_list|(
name|language
argument_list|)
condition|)
block|{
return|return
operator|new
name|ArabicStemFilter
argument_list|(
name|tokenStream
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
literal|"armenian"
operator|.
name|equalsIgnoreCase
argument_list|(
name|language
argument_list|)
condition|)
block|{
return|return
operator|new
name|SnowballFilter
argument_list|(
name|tokenStream
argument_list|,
operator|new
name|ArmenianStemmer
argument_list|()
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
literal|"basque"
operator|.
name|equalsIgnoreCase
argument_list|(
name|language
argument_list|)
condition|)
block|{
return|return
operator|new
name|SnowballFilter
argument_list|(
name|tokenStream
argument_list|,
operator|new
name|BasqueStemmer
argument_list|()
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
literal|"brazilian"
operator|.
name|equalsIgnoreCase
argument_list|(
name|language
argument_list|)
condition|)
block|{
return|return
operator|new
name|BrazilianStemFilter
argument_list|(
name|tokenStream
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
literal|"bulgarian"
operator|.
name|equalsIgnoreCase
argument_list|(
name|language
argument_list|)
condition|)
block|{
return|return
operator|new
name|BulgarianStemFilter
argument_list|(
name|tokenStream
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
literal|"catalan"
operator|.
name|equalsIgnoreCase
argument_list|(
name|language
argument_list|)
condition|)
block|{
return|return
operator|new
name|SnowballFilter
argument_list|(
name|tokenStream
argument_list|,
operator|new
name|CatalanStemmer
argument_list|()
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
literal|"czech"
operator|.
name|equalsIgnoreCase
argument_list|(
name|language
argument_list|)
condition|)
block|{
return|return
operator|new
name|CzechStemFilter
argument_list|(
name|tokenStream
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
literal|"danish"
operator|.
name|equalsIgnoreCase
argument_list|(
name|language
argument_list|)
condition|)
block|{
return|return
operator|new
name|SnowballFilter
argument_list|(
name|tokenStream
argument_list|,
operator|new
name|DanishStemmer
argument_list|()
argument_list|)
return|;
comment|// Dutch stemmers
block|}
elseif|else
if|if
condition|(
literal|"dutch"
operator|.
name|equalsIgnoreCase
argument_list|(
name|language
argument_list|)
condition|)
block|{
return|return
operator|new
name|SnowballFilter
argument_list|(
name|tokenStream
argument_list|,
operator|new
name|DutchStemmer
argument_list|()
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
literal|"dutch_kp"
operator|.
name|equalsIgnoreCase
argument_list|(
name|language
argument_list|)
operator|||
literal|"dutchKp"
operator|.
name|equalsIgnoreCase
argument_list|(
name|language
argument_list|)
operator|||
literal|"kp"
operator|.
name|equalsIgnoreCase
argument_list|(
name|language
argument_list|)
condition|)
block|{
return|return
operator|new
name|SnowballFilter
argument_list|(
name|tokenStream
argument_list|,
operator|new
name|KpStemmer
argument_list|()
argument_list|)
return|;
comment|// English stemmers
block|}
elseif|else
if|if
condition|(
literal|"english"
operator|.
name|equalsIgnoreCase
argument_list|(
name|language
argument_list|)
condition|)
block|{
if|if
condition|(
name|indexVersion
operator|.
name|onOrAfter
argument_list|(
name|Version
operator|.
name|V_1_3_0
argument_list|)
condition|)
block|{
return|return
operator|new
name|PorterStemFilter
argument_list|(
name|tokenStream
argument_list|)
return|;
block|}
else|else
block|{
return|return
operator|new
name|SnowballFilter
argument_list|(
name|tokenStream
argument_list|,
operator|new
name|EnglishStemmer
argument_list|()
argument_list|)
return|;
block|}
block|}
elseif|else
if|if
condition|(
literal|"light_english"
operator|.
name|equalsIgnoreCase
argument_list|(
name|language
argument_list|)
operator|||
literal|"lightEnglish"
operator|.
name|equalsIgnoreCase
argument_list|(
name|language
argument_list|)
operator|||
literal|"kstem"
operator|.
name|equalsIgnoreCase
argument_list|(
name|language
argument_list|)
condition|)
block|{
return|return
operator|new
name|KStemFilter
argument_list|(
name|tokenStream
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
literal|"lovins"
operator|.
name|equalsIgnoreCase
argument_list|(
name|language
argument_list|)
condition|)
block|{
return|return
operator|new
name|SnowballFilter
argument_list|(
name|tokenStream
argument_list|,
operator|new
name|LovinsStemmer
argument_list|()
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
literal|"porter"
operator|.
name|equalsIgnoreCase
argument_list|(
name|language
argument_list|)
condition|)
block|{
return|return
operator|new
name|PorterStemFilter
argument_list|(
name|tokenStream
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
literal|"porter2"
operator|.
name|equalsIgnoreCase
argument_list|(
name|language
argument_list|)
condition|)
block|{
if|if
condition|(
name|indexVersion
operator|.
name|onOrAfter
argument_list|(
name|Version
operator|.
name|V_1_3_0
argument_list|)
condition|)
block|{
return|return
operator|new
name|SnowballFilter
argument_list|(
name|tokenStream
argument_list|,
operator|new
name|EnglishStemmer
argument_list|()
argument_list|)
return|;
block|}
else|else
block|{
return|return
operator|new
name|SnowballFilter
argument_list|(
name|tokenStream
argument_list|,
operator|new
name|PorterStemmer
argument_list|()
argument_list|)
return|;
block|}
block|}
elseif|else
if|if
condition|(
literal|"minimal_english"
operator|.
name|equalsIgnoreCase
argument_list|(
name|language
argument_list|)
operator|||
literal|"minimalEnglish"
operator|.
name|equalsIgnoreCase
argument_list|(
name|language
argument_list|)
condition|)
block|{
return|return
operator|new
name|EnglishMinimalStemFilter
argument_list|(
name|tokenStream
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
literal|"possessive_english"
operator|.
name|equalsIgnoreCase
argument_list|(
name|language
argument_list|)
operator|||
literal|"possessiveEnglish"
operator|.
name|equalsIgnoreCase
argument_list|(
name|language
argument_list|)
condition|)
block|{
return|return
operator|new
name|EnglishPossessiveFilter
argument_list|(
name|version
argument_list|,
name|tokenStream
argument_list|)
return|;
comment|// Finnish stemmers
block|}
elseif|else
if|if
condition|(
literal|"finnish"
operator|.
name|equalsIgnoreCase
argument_list|(
name|language
argument_list|)
condition|)
block|{
return|return
operator|new
name|SnowballFilter
argument_list|(
name|tokenStream
argument_list|,
operator|new
name|FinnishStemmer
argument_list|()
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
literal|"light_finish"
operator|.
name|equalsIgnoreCase
argument_list|(
name|language
argument_list|)
operator|||
literal|"lightFinish"
operator|.
name|equalsIgnoreCase
argument_list|(
name|language
argument_list|)
condition|)
block|{
comment|// leaving this for backward compatibility
return|return
operator|new
name|FinnishLightStemFilter
argument_list|(
name|tokenStream
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
literal|"light_finnish"
operator|.
name|equalsIgnoreCase
argument_list|(
name|language
argument_list|)
operator|||
literal|"lightFinnish"
operator|.
name|equalsIgnoreCase
argument_list|(
name|language
argument_list|)
condition|)
block|{
return|return
operator|new
name|FinnishLightStemFilter
argument_list|(
name|tokenStream
argument_list|)
return|;
comment|// French stemmers
block|}
elseif|else
if|if
condition|(
literal|"french"
operator|.
name|equalsIgnoreCase
argument_list|(
name|language
argument_list|)
condition|)
block|{
return|return
operator|new
name|SnowballFilter
argument_list|(
name|tokenStream
argument_list|,
operator|new
name|FrenchStemmer
argument_list|()
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
literal|"light_french"
operator|.
name|equalsIgnoreCase
argument_list|(
name|language
argument_list|)
operator|||
literal|"lightFrench"
operator|.
name|equalsIgnoreCase
argument_list|(
name|language
argument_list|)
condition|)
block|{
return|return
operator|new
name|FrenchLightStemFilter
argument_list|(
name|tokenStream
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
literal|"minimal_french"
operator|.
name|equalsIgnoreCase
argument_list|(
name|language
argument_list|)
operator|||
literal|"minimalFrench"
operator|.
name|equalsIgnoreCase
argument_list|(
name|language
argument_list|)
condition|)
block|{
return|return
operator|new
name|FrenchMinimalStemFilter
argument_list|(
name|tokenStream
argument_list|)
return|;
comment|// Galician stemmers
block|}
elseif|else
if|if
condition|(
literal|"galician"
operator|.
name|equalsIgnoreCase
argument_list|(
name|language
argument_list|)
condition|)
block|{
return|return
operator|new
name|GalicianStemFilter
argument_list|(
name|tokenStream
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
literal|"minimal_galician"
operator|.
name|equalsIgnoreCase
argument_list|(
name|language
argument_list|)
condition|)
block|{
return|return
operator|new
name|GalicianMinimalStemFilter
argument_list|(
name|tokenStream
argument_list|)
return|;
comment|// German stemmers
block|}
elseif|else
if|if
condition|(
literal|"german"
operator|.
name|equalsIgnoreCase
argument_list|(
name|language
argument_list|)
condition|)
block|{
return|return
operator|new
name|SnowballFilter
argument_list|(
name|tokenStream
argument_list|,
operator|new
name|GermanStemmer
argument_list|()
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
literal|"german2"
operator|.
name|equalsIgnoreCase
argument_list|(
name|language
argument_list|)
condition|)
block|{
return|return
operator|new
name|SnowballFilter
argument_list|(
name|tokenStream
argument_list|,
operator|new
name|German2Stemmer
argument_list|()
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
literal|"light_german"
operator|.
name|equalsIgnoreCase
argument_list|(
name|language
argument_list|)
operator|||
literal|"lightGerman"
operator|.
name|equalsIgnoreCase
argument_list|(
name|language
argument_list|)
condition|)
block|{
return|return
operator|new
name|GermanLightStemFilter
argument_list|(
name|tokenStream
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
literal|"minimal_german"
operator|.
name|equalsIgnoreCase
argument_list|(
name|language
argument_list|)
operator|||
literal|"minimalGerman"
operator|.
name|equalsIgnoreCase
argument_list|(
name|language
argument_list|)
condition|)
block|{
return|return
operator|new
name|GermanMinimalStemFilter
argument_list|(
name|tokenStream
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
literal|"greek"
operator|.
name|equalsIgnoreCase
argument_list|(
name|language
argument_list|)
condition|)
block|{
return|return
operator|new
name|GreekStemFilter
argument_list|(
name|tokenStream
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
literal|"hindi"
operator|.
name|equalsIgnoreCase
argument_list|(
name|language
argument_list|)
condition|)
block|{
return|return
operator|new
name|HindiStemFilter
argument_list|(
name|tokenStream
argument_list|)
return|;
comment|// Hungarian stemmers
block|}
elseif|else
if|if
condition|(
literal|"hungarian"
operator|.
name|equalsIgnoreCase
argument_list|(
name|language
argument_list|)
condition|)
block|{
return|return
operator|new
name|SnowballFilter
argument_list|(
name|tokenStream
argument_list|,
operator|new
name|HungarianStemmer
argument_list|()
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
literal|"light_hungarian"
operator|.
name|equalsIgnoreCase
argument_list|(
name|language
argument_list|)
operator|||
literal|"lightHungarian"
operator|.
name|equalsIgnoreCase
argument_list|(
name|language
argument_list|)
condition|)
block|{
return|return
operator|new
name|HungarianLightStemFilter
argument_list|(
name|tokenStream
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
literal|"indonesian"
operator|.
name|equalsIgnoreCase
argument_list|(
name|language
argument_list|)
condition|)
block|{
return|return
operator|new
name|IndonesianStemFilter
argument_list|(
name|tokenStream
argument_list|)
return|;
comment|// Irish stemmer
block|}
elseif|else
if|if
condition|(
literal|"irish"
operator|.
name|equalsIgnoreCase
argument_list|(
name|language
argument_list|)
condition|)
block|{
return|return
operator|new
name|SnowballFilter
argument_list|(
name|tokenStream
argument_list|,
operator|new
name|IrishStemmer
argument_list|()
argument_list|)
return|;
comment|// Italian stemmers
block|}
elseif|else
if|if
condition|(
literal|"italian"
operator|.
name|equalsIgnoreCase
argument_list|(
name|language
argument_list|)
condition|)
block|{
return|return
operator|new
name|SnowballFilter
argument_list|(
name|tokenStream
argument_list|,
operator|new
name|ItalianStemmer
argument_list|()
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
literal|"light_italian"
operator|.
name|equalsIgnoreCase
argument_list|(
name|language
argument_list|)
operator|||
literal|"lightItalian"
operator|.
name|equalsIgnoreCase
argument_list|(
name|language
argument_list|)
condition|)
block|{
return|return
operator|new
name|ItalianLightStemFilter
argument_list|(
name|tokenStream
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
literal|"latvian"
operator|.
name|equalsIgnoreCase
argument_list|(
name|language
argument_list|)
condition|)
block|{
return|return
operator|new
name|LatvianStemFilter
argument_list|(
name|tokenStream
argument_list|)
return|;
comment|// Norwegian (BokmÃ¥l) stemmers
block|}
elseif|else
if|if
condition|(
literal|"norwegian"
operator|.
name|equalsIgnoreCase
argument_list|(
name|language
argument_list|)
condition|)
block|{
return|return
operator|new
name|SnowballFilter
argument_list|(
name|tokenStream
argument_list|,
operator|new
name|NorwegianStemmer
argument_list|()
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
literal|"light_norwegian"
operator|.
name|equalsIgnoreCase
argument_list|(
name|language
argument_list|)
operator|||
literal|"lightNorwegian"
operator|.
name|equalsIgnoreCase
argument_list|(
name|language
argument_list|)
condition|)
block|{
return|return
operator|new
name|NorwegianLightStemFilter
argument_list|(
name|tokenStream
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
literal|"minimal_norwegian"
operator|.
name|equalsIgnoreCase
argument_list|(
name|language
argument_list|)
operator|||
literal|"minimalNorwegian"
operator|.
name|equals
argument_list|(
name|language
argument_list|)
condition|)
block|{
return|return
operator|new
name|NorwegianMinimalStemFilter
argument_list|(
name|tokenStream
argument_list|)
return|;
comment|// Norwegian (Nynorsk) stemmers
block|}
elseif|else
if|if
condition|(
literal|"light_nynorsk"
operator|.
name|equalsIgnoreCase
argument_list|(
name|language
argument_list|)
operator|||
literal|"lightNynorsk"
operator|.
name|equalsIgnoreCase
argument_list|(
name|language
argument_list|)
condition|)
block|{
return|return
operator|new
name|NorwegianLightStemFilter
argument_list|(
name|tokenStream
argument_list|,
name|NorwegianLightStemmer
operator|.
name|NYNORSK
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
literal|"minimal_nynorsk"
operator|.
name|equalsIgnoreCase
argument_list|(
name|language
argument_list|)
operator|||
literal|"minimalNynorsk"
operator|.
name|equalsIgnoreCase
argument_list|(
name|language
argument_list|)
condition|)
block|{
return|return
operator|new
name|NorwegianMinimalStemFilter
argument_list|(
name|tokenStream
argument_list|,
name|NorwegianLightStemmer
operator|.
name|NYNORSK
argument_list|)
return|;
comment|// Portuguese stemmers
block|}
elseif|else
if|if
condition|(
literal|"portuguese"
operator|.
name|equalsIgnoreCase
argument_list|(
name|language
argument_list|)
condition|)
block|{
return|return
operator|new
name|SnowballFilter
argument_list|(
name|tokenStream
argument_list|,
operator|new
name|PortugueseStemmer
argument_list|()
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
literal|"light_portuguese"
operator|.
name|equalsIgnoreCase
argument_list|(
name|language
argument_list|)
operator|||
literal|"lightPortuguese"
operator|.
name|equalsIgnoreCase
argument_list|(
name|language
argument_list|)
condition|)
block|{
return|return
operator|new
name|PortugueseLightStemFilter
argument_list|(
name|tokenStream
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
literal|"minimal_portuguese"
operator|.
name|equalsIgnoreCase
argument_list|(
name|language
argument_list|)
operator|||
literal|"minimalPortuguese"
operator|.
name|equalsIgnoreCase
argument_list|(
name|language
argument_list|)
condition|)
block|{
return|return
operator|new
name|PortugueseMinimalStemFilter
argument_list|(
name|tokenStream
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
literal|"portuguese_rslp"
operator|.
name|equalsIgnoreCase
argument_list|(
name|language
argument_list|)
condition|)
block|{
return|return
operator|new
name|PortugueseStemFilter
argument_list|(
name|tokenStream
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
literal|"romanian"
operator|.
name|equalsIgnoreCase
argument_list|(
name|language
argument_list|)
condition|)
block|{
return|return
operator|new
name|SnowballFilter
argument_list|(
name|tokenStream
argument_list|,
operator|new
name|RomanianStemmer
argument_list|()
argument_list|)
return|;
comment|// Russian stemmers
block|}
elseif|else
if|if
condition|(
literal|"russian"
operator|.
name|equalsIgnoreCase
argument_list|(
name|language
argument_list|)
condition|)
block|{
return|return
operator|new
name|SnowballFilter
argument_list|(
name|tokenStream
argument_list|,
operator|new
name|RussianStemmer
argument_list|()
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
literal|"light_russian"
operator|.
name|equalsIgnoreCase
argument_list|(
name|language
argument_list|)
operator|||
literal|"lightRussian"
operator|.
name|equalsIgnoreCase
argument_list|(
name|language
argument_list|)
condition|)
block|{
return|return
operator|new
name|RussianLightStemFilter
argument_list|(
name|tokenStream
argument_list|)
return|;
comment|// Spanish stemmers
block|}
elseif|else
if|if
condition|(
literal|"spanish"
operator|.
name|equalsIgnoreCase
argument_list|(
name|language
argument_list|)
condition|)
block|{
return|return
operator|new
name|SnowballFilter
argument_list|(
name|tokenStream
argument_list|,
operator|new
name|SpanishStemmer
argument_list|()
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
literal|"light_spanish"
operator|.
name|equalsIgnoreCase
argument_list|(
name|language
argument_list|)
operator|||
literal|"lightSpanish"
operator|.
name|equalsIgnoreCase
argument_list|(
name|language
argument_list|)
condition|)
block|{
return|return
operator|new
name|SpanishLightStemFilter
argument_list|(
name|tokenStream
argument_list|)
return|;
comment|// Sorani Kurdish stemmer
block|}
elseif|else
if|if
condition|(
literal|"sorani"
operator|.
name|equalsIgnoreCase
argument_list|(
name|language
argument_list|)
condition|)
block|{
return|return
operator|new
name|SoraniStemFilter
argument_list|(
name|tokenStream
argument_list|)
return|;
comment|// Swedish stemmers
block|}
elseif|else
if|if
condition|(
literal|"swedish"
operator|.
name|equalsIgnoreCase
argument_list|(
name|language
argument_list|)
condition|)
block|{
return|return
operator|new
name|SnowballFilter
argument_list|(
name|tokenStream
argument_list|,
operator|new
name|SwedishStemmer
argument_list|()
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
literal|"light_swedish"
operator|.
name|equalsIgnoreCase
argument_list|(
name|language
argument_list|)
operator|||
literal|"lightSwedish"
operator|.
name|equalsIgnoreCase
argument_list|(
name|language
argument_list|)
condition|)
block|{
return|return
operator|new
name|SwedishLightStemFilter
argument_list|(
name|tokenStream
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
literal|"turkish"
operator|.
name|equalsIgnoreCase
argument_list|(
name|language
argument_list|)
condition|)
block|{
return|return
operator|new
name|SnowballFilter
argument_list|(
name|tokenStream
argument_list|,
operator|new
name|TurkishStemmer
argument_list|()
argument_list|)
return|;
block|}
return|return
operator|new
name|SnowballFilter
argument_list|(
name|tokenStream
argument_list|,
name|language
argument_list|)
return|;
block|}
block|}
end_class

end_unit

