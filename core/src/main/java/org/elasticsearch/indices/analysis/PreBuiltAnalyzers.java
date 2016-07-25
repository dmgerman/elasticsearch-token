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
name|apache
operator|.
name|lucene
operator|.
name|analysis
operator|.
name|ar
operator|.
name|ArabicAnalyzer
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
name|BulgarianAnalyzer
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
name|BrazilianAnalyzer
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
name|ca
operator|.
name|CatalanAnalyzer
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
name|CJKAnalyzer
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
name|SoraniAnalyzer
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
name|core
operator|.
name|KeywordAnalyzer
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
name|core
operator|.
name|SimpleAnalyzer
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
name|core
operator|.
name|StopAnalyzer
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
name|core
operator|.
name|WhitespaceAnalyzer
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
name|CzechAnalyzer
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
name|da
operator|.
name|DanishAnalyzer
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
name|GermanAnalyzer
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
name|GreekAnalyzer
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
name|EnglishAnalyzer
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
name|SpanishAnalyzer
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
name|eu
operator|.
name|BasqueAnalyzer
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
name|fa
operator|.
name|PersianAnalyzer
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
name|FinnishAnalyzer
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
name|FrenchAnalyzer
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
name|ga
operator|.
name|IrishAnalyzer
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
name|GalicianAnalyzer
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
name|HindiAnalyzer
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
name|HungarianAnalyzer
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
name|hy
operator|.
name|ArmenianAnalyzer
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
name|IndonesianAnalyzer
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
name|ItalianAnalyzer
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
name|lt
operator|.
name|LithuanianAnalyzer
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
name|LatvianAnalyzer
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
name|nl
operator|.
name|DutchAnalyzer
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
name|NorwegianAnalyzer
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
name|PortugueseAnalyzer
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
name|ro
operator|.
name|RomanianAnalyzer
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
name|RussianAnalyzer
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
name|standard
operator|.
name|ClassicAnalyzer
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
name|standard
operator|.
name|StandardAnalyzer
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
name|SwedishAnalyzer
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
name|th
operator|.
name|ThaiAnalyzer
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
name|tr
operator|.
name|TurkishAnalyzer
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
name|util
operator|.
name|CharArraySet
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
name|regex
operator|.
name|Regex
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
name|PatternAnalyzer
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
name|SnowballAnalyzer
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
name|StandardHtmlStripAnalyzer
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
name|PreBuiltCacheFactory
operator|.
name|CachingStrategy
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

begin_comment
comment|/**  *  */
end_comment

begin_enum
DECL|enum|PreBuiltAnalyzers
specifier|public
enum|enum
name|PreBuiltAnalyzers
block|{
DECL|method|STANDARD
DECL|method|STANDARD
name|STANDARD
parameter_list|(
name|CachingStrategy
operator|.
name|ELASTICSEARCH
parameter_list|)
block|{
annotation|@
name|Override
specifier|protected
name|Analyzer
name|create
parameter_list|(
name|Version
name|version
parameter_list|)
block|{
specifier|final
name|Analyzer
name|a
init|=
operator|new
name|StandardAnalyzer
argument_list|(
name|CharArraySet
operator|.
name|EMPTY_SET
argument_list|)
decl_stmt|;
name|a
operator|.
name|setVersion
argument_list|(
name|version
operator|.
name|luceneVersion
argument_list|)
expr_stmt|;
return|return
name|a
return|;
block|}
block|}
block|,
DECL|method|DEFAULT
DECL|method|DEFAULT
name|DEFAULT
parameter_list|(
name|CachingStrategy
operator|.
name|ELASTICSEARCH
parameter_list|)
block|{
annotation|@
name|Override
specifier|protected
name|Analyzer
name|create
parameter_list|(
name|Version
name|version
parameter_list|)
block|{
comment|// by calling get analyzer we are ensuring reuse of the same STANDARD analyzer for DEFAULT!
comment|// this call does not create a new instance
return|return
name|STANDARD
operator|.
name|getAnalyzer
argument_list|(
name|version
argument_list|)
return|;
block|}
block|}
block|,
DECL|method|KEYWORD
DECL|method|KEYWORD
name|KEYWORD
parameter_list|(
name|CachingStrategy
operator|.
name|ONE
parameter_list|)
block|{
annotation|@
name|Override
specifier|protected
name|Analyzer
name|create
parameter_list|(
name|Version
name|version
parameter_list|)
block|{
return|return
operator|new
name|KeywordAnalyzer
argument_list|()
return|;
block|}
block|}
block|,
DECL|enum constant|STOP
name|STOP
block|{
annotation|@
name|Override
specifier|protected
name|Analyzer
name|create
parameter_list|(
name|Version
name|version
parameter_list|)
block|{
name|Analyzer
name|a
init|=
operator|new
name|StopAnalyzer
argument_list|()
decl_stmt|;
name|a
operator|.
name|setVersion
argument_list|(
name|version
operator|.
name|luceneVersion
argument_list|)
expr_stmt|;
return|return
name|a
return|;
block|}
block|}
block|,
DECL|enum constant|WHITESPACE
name|WHITESPACE
block|{
annotation|@
name|Override
specifier|protected
name|Analyzer
name|create
parameter_list|(
name|Version
name|version
parameter_list|)
block|{
name|Analyzer
name|a
init|=
operator|new
name|WhitespaceAnalyzer
argument_list|()
decl_stmt|;
name|a
operator|.
name|setVersion
argument_list|(
name|version
operator|.
name|luceneVersion
argument_list|)
expr_stmt|;
return|return
name|a
return|;
block|}
block|}
block|,
DECL|enum constant|SIMPLE
name|SIMPLE
block|{
annotation|@
name|Override
specifier|protected
name|Analyzer
name|create
parameter_list|(
name|Version
name|version
parameter_list|)
block|{
name|Analyzer
name|a
init|=
operator|new
name|SimpleAnalyzer
argument_list|()
decl_stmt|;
name|a
operator|.
name|setVersion
argument_list|(
name|version
operator|.
name|luceneVersion
argument_list|)
expr_stmt|;
return|return
name|a
return|;
block|}
block|}
block|,
DECL|enum constant|CLASSIC
name|CLASSIC
block|{
annotation|@
name|Override
specifier|protected
name|Analyzer
name|create
parameter_list|(
name|Version
name|version
parameter_list|)
block|{
name|Analyzer
name|a
init|=
operator|new
name|ClassicAnalyzer
argument_list|()
decl_stmt|;
name|a
operator|.
name|setVersion
argument_list|(
name|version
operator|.
name|luceneVersion
argument_list|)
expr_stmt|;
return|return
name|a
return|;
block|}
block|}
block|,
DECL|enum constant|SNOWBALL
name|SNOWBALL
block|{
annotation|@
name|Override
specifier|protected
name|Analyzer
name|create
parameter_list|(
name|Version
name|version
parameter_list|)
block|{
name|Analyzer
name|analyzer
init|=
operator|new
name|SnowballAnalyzer
argument_list|(
literal|"English"
argument_list|,
name|StopAnalyzer
operator|.
name|ENGLISH_STOP_WORDS_SET
argument_list|)
decl_stmt|;
name|analyzer
operator|.
name|setVersion
argument_list|(
name|version
operator|.
name|luceneVersion
argument_list|)
expr_stmt|;
return|return
name|analyzer
return|;
block|}
block|}
block|,
DECL|method|PATTERN
DECL|method|PATTERN
name|PATTERN
parameter_list|(
name|CachingStrategy
operator|.
name|ELASTICSEARCH
parameter_list|)
block|{
annotation|@
name|Override
specifier|protected
name|Analyzer
name|create
parameter_list|(
name|Version
name|version
parameter_list|)
block|{
return|return
operator|new
name|PatternAnalyzer
argument_list|(
name|Regex
operator|.
name|compile
argument_list|(
literal|"\\W+"
comment|/*PatternAnalyzer.NON_WORD_PATTERN*/
argument_list|,
literal|null
argument_list|)
argument_list|,
literal|true
argument_list|,
name|CharArraySet
operator|.
name|EMPTY_SET
argument_list|)
return|;
block|}
block|}
block|,
DECL|method|STANDARD_HTML_STRIP
DECL|method|STANDARD_HTML_STRIP
name|STANDARD_HTML_STRIP
parameter_list|(
name|CachingStrategy
operator|.
name|ELASTICSEARCH
parameter_list|)
block|{
annotation|@
name|Override
specifier|protected
name|Analyzer
name|create
parameter_list|(
name|Version
name|version
parameter_list|)
block|{
specifier|final
name|Analyzer
name|analyzer
init|=
operator|new
name|StandardHtmlStripAnalyzer
argument_list|(
name|CharArraySet
operator|.
name|EMPTY_SET
argument_list|)
decl_stmt|;
name|analyzer
operator|.
name|setVersion
argument_list|(
name|version
operator|.
name|luceneVersion
argument_list|)
expr_stmt|;
return|return
name|analyzer
return|;
block|}
block|}
block|,
DECL|enum constant|ARABIC
name|ARABIC
block|{
annotation|@
name|Override
specifier|protected
name|Analyzer
name|create
parameter_list|(
name|Version
name|version
parameter_list|)
block|{
name|Analyzer
name|a
init|=
operator|new
name|ArabicAnalyzer
argument_list|()
decl_stmt|;
name|a
operator|.
name|setVersion
argument_list|(
name|version
operator|.
name|luceneVersion
argument_list|)
expr_stmt|;
return|return
name|a
return|;
block|}
block|}
block|,
DECL|enum constant|ARMENIAN
name|ARMENIAN
block|{
annotation|@
name|Override
specifier|protected
name|Analyzer
name|create
parameter_list|(
name|Version
name|version
parameter_list|)
block|{
name|Analyzer
name|a
init|=
operator|new
name|ArmenianAnalyzer
argument_list|()
decl_stmt|;
name|a
operator|.
name|setVersion
argument_list|(
name|version
operator|.
name|luceneVersion
argument_list|)
expr_stmt|;
return|return
name|a
return|;
block|}
block|}
block|,
DECL|enum constant|BASQUE
name|BASQUE
block|{
annotation|@
name|Override
specifier|protected
name|Analyzer
name|create
parameter_list|(
name|Version
name|version
parameter_list|)
block|{
name|Analyzer
name|a
init|=
operator|new
name|BasqueAnalyzer
argument_list|()
decl_stmt|;
name|a
operator|.
name|setVersion
argument_list|(
name|version
operator|.
name|luceneVersion
argument_list|)
expr_stmt|;
return|return
name|a
return|;
block|}
block|}
block|,
DECL|enum constant|BRAZILIAN
name|BRAZILIAN
block|{
annotation|@
name|Override
specifier|protected
name|Analyzer
name|create
parameter_list|(
name|Version
name|version
parameter_list|)
block|{
name|Analyzer
name|a
init|=
operator|new
name|BrazilianAnalyzer
argument_list|()
decl_stmt|;
name|a
operator|.
name|setVersion
argument_list|(
name|version
operator|.
name|luceneVersion
argument_list|)
expr_stmt|;
return|return
name|a
return|;
block|}
block|}
block|,
DECL|enum constant|BULGARIAN
name|BULGARIAN
block|{
annotation|@
name|Override
specifier|protected
name|Analyzer
name|create
parameter_list|(
name|Version
name|version
parameter_list|)
block|{
name|Analyzer
name|a
init|=
operator|new
name|BulgarianAnalyzer
argument_list|()
decl_stmt|;
name|a
operator|.
name|setVersion
argument_list|(
name|version
operator|.
name|luceneVersion
argument_list|)
expr_stmt|;
return|return
name|a
return|;
block|}
block|}
block|,
DECL|enum constant|CATALAN
name|CATALAN
block|{
annotation|@
name|Override
specifier|protected
name|Analyzer
name|create
parameter_list|(
name|Version
name|version
parameter_list|)
block|{
name|Analyzer
name|a
init|=
operator|new
name|CatalanAnalyzer
argument_list|()
decl_stmt|;
name|a
operator|.
name|setVersion
argument_list|(
name|version
operator|.
name|luceneVersion
argument_list|)
expr_stmt|;
return|return
name|a
return|;
block|}
block|}
block|,
DECL|method|CHINESE
DECL|method|CHINESE
name|CHINESE
parameter_list|(
name|CachingStrategy
operator|.
name|ONE
parameter_list|)
block|{
annotation|@
name|Override
specifier|protected
name|Analyzer
name|create
parameter_list|(
name|Version
name|version
parameter_list|)
block|{
name|Analyzer
name|a
init|=
operator|new
name|StandardAnalyzer
argument_list|()
decl_stmt|;
name|a
operator|.
name|setVersion
argument_list|(
name|version
operator|.
name|luceneVersion
argument_list|)
expr_stmt|;
return|return
name|a
return|;
block|}
block|}
block|,
DECL|enum constant|CJK
name|CJK
block|{
annotation|@
name|Override
specifier|protected
name|Analyzer
name|create
parameter_list|(
name|Version
name|version
parameter_list|)
block|{
name|Analyzer
name|a
init|=
operator|new
name|CJKAnalyzer
argument_list|()
decl_stmt|;
name|a
operator|.
name|setVersion
argument_list|(
name|version
operator|.
name|luceneVersion
argument_list|)
expr_stmt|;
return|return
name|a
return|;
block|}
block|}
block|,
DECL|enum constant|CZECH
name|CZECH
block|{
annotation|@
name|Override
specifier|protected
name|Analyzer
name|create
parameter_list|(
name|Version
name|version
parameter_list|)
block|{
name|Analyzer
name|a
init|=
operator|new
name|CzechAnalyzer
argument_list|()
decl_stmt|;
name|a
operator|.
name|setVersion
argument_list|(
name|version
operator|.
name|luceneVersion
argument_list|)
expr_stmt|;
return|return
name|a
return|;
block|}
block|}
block|,
DECL|enum constant|DUTCH
name|DUTCH
block|{
annotation|@
name|Override
specifier|protected
name|Analyzer
name|create
parameter_list|(
name|Version
name|version
parameter_list|)
block|{
name|Analyzer
name|a
init|=
operator|new
name|DutchAnalyzer
argument_list|()
decl_stmt|;
name|a
operator|.
name|setVersion
argument_list|(
name|version
operator|.
name|luceneVersion
argument_list|)
expr_stmt|;
return|return
name|a
return|;
block|}
block|}
block|,
DECL|enum constant|DANISH
name|DANISH
block|{
annotation|@
name|Override
specifier|protected
name|Analyzer
name|create
parameter_list|(
name|Version
name|version
parameter_list|)
block|{
name|Analyzer
name|a
init|=
operator|new
name|DanishAnalyzer
argument_list|()
decl_stmt|;
name|a
operator|.
name|setVersion
argument_list|(
name|version
operator|.
name|luceneVersion
argument_list|)
expr_stmt|;
return|return
name|a
return|;
block|}
block|}
block|,
DECL|enum constant|ENGLISH
name|ENGLISH
block|{
annotation|@
name|Override
specifier|protected
name|Analyzer
name|create
parameter_list|(
name|Version
name|version
parameter_list|)
block|{
name|Analyzer
name|a
init|=
operator|new
name|EnglishAnalyzer
argument_list|()
decl_stmt|;
name|a
operator|.
name|setVersion
argument_list|(
name|version
operator|.
name|luceneVersion
argument_list|)
expr_stmt|;
return|return
name|a
return|;
block|}
block|}
block|,
DECL|enum constant|FINNISH
name|FINNISH
block|{
annotation|@
name|Override
specifier|protected
name|Analyzer
name|create
parameter_list|(
name|Version
name|version
parameter_list|)
block|{
name|Analyzer
name|a
init|=
operator|new
name|FinnishAnalyzer
argument_list|()
decl_stmt|;
name|a
operator|.
name|setVersion
argument_list|(
name|version
operator|.
name|luceneVersion
argument_list|)
expr_stmt|;
return|return
name|a
return|;
block|}
block|}
block|,
DECL|enum constant|FRENCH
name|FRENCH
block|{
annotation|@
name|Override
specifier|protected
name|Analyzer
name|create
parameter_list|(
name|Version
name|version
parameter_list|)
block|{
name|Analyzer
name|a
init|=
operator|new
name|FrenchAnalyzer
argument_list|()
decl_stmt|;
name|a
operator|.
name|setVersion
argument_list|(
name|version
operator|.
name|luceneVersion
argument_list|)
expr_stmt|;
return|return
name|a
return|;
block|}
block|}
block|,
DECL|enum constant|GALICIAN
name|GALICIAN
block|{
annotation|@
name|Override
specifier|protected
name|Analyzer
name|create
parameter_list|(
name|Version
name|version
parameter_list|)
block|{
name|Analyzer
name|a
init|=
operator|new
name|GalicianAnalyzer
argument_list|()
decl_stmt|;
name|a
operator|.
name|setVersion
argument_list|(
name|version
operator|.
name|luceneVersion
argument_list|)
expr_stmt|;
return|return
name|a
return|;
block|}
block|}
block|,
DECL|enum constant|GERMAN
name|GERMAN
block|{
annotation|@
name|Override
specifier|protected
name|Analyzer
name|create
parameter_list|(
name|Version
name|version
parameter_list|)
block|{
name|Analyzer
name|a
init|=
operator|new
name|GermanAnalyzer
argument_list|()
decl_stmt|;
name|a
operator|.
name|setVersion
argument_list|(
name|version
operator|.
name|luceneVersion
argument_list|)
expr_stmt|;
return|return
name|a
return|;
block|}
block|}
block|,
DECL|enum constant|GREEK
name|GREEK
block|{
annotation|@
name|Override
specifier|protected
name|Analyzer
name|create
parameter_list|(
name|Version
name|version
parameter_list|)
block|{
name|Analyzer
name|a
init|=
operator|new
name|GreekAnalyzer
argument_list|()
decl_stmt|;
name|a
operator|.
name|setVersion
argument_list|(
name|version
operator|.
name|luceneVersion
argument_list|)
expr_stmt|;
return|return
name|a
return|;
block|}
block|}
block|,
DECL|enum constant|HINDI
name|HINDI
block|{
annotation|@
name|Override
specifier|protected
name|Analyzer
name|create
parameter_list|(
name|Version
name|version
parameter_list|)
block|{
name|Analyzer
name|a
init|=
operator|new
name|HindiAnalyzer
argument_list|()
decl_stmt|;
name|a
operator|.
name|setVersion
argument_list|(
name|version
operator|.
name|luceneVersion
argument_list|)
expr_stmt|;
return|return
name|a
return|;
block|}
block|}
block|,
DECL|enum constant|HUNGARIAN
name|HUNGARIAN
block|{
annotation|@
name|Override
specifier|protected
name|Analyzer
name|create
parameter_list|(
name|Version
name|version
parameter_list|)
block|{
name|Analyzer
name|a
init|=
operator|new
name|HungarianAnalyzer
argument_list|()
decl_stmt|;
name|a
operator|.
name|setVersion
argument_list|(
name|version
operator|.
name|luceneVersion
argument_list|)
expr_stmt|;
return|return
name|a
return|;
block|}
block|}
block|,
DECL|enum constant|INDONESIAN
name|INDONESIAN
block|{
annotation|@
name|Override
specifier|protected
name|Analyzer
name|create
parameter_list|(
name|Version
name|version
parameter_list|)
block|{
name|Analyzer
name|a
init|=
operator|new
name|IndonesianAnalyzer
argument_list|()
decl_stmt|;
name|a
operator|.
name|setVersion
argument_list|(
name|version
operator|.
name|luceneVersion
argument_list|)
expr_stmt|;
return|return
name|a
return|;
block|}
block|}
block|,
DECL|enum constant|IRISH
name|IRISH
block|{
annotation|@
name|Override
specifier|protected
name|Analyzer
name|create
parameter_list|(
name|Version
name|version
parameter_list|)
block|{
name|Analyzer
name|a
init|=
operator|new
name|IrishAnalyzer
argument_list|()
decl_stmt|;
name|a
operator|.
name|setVersion
argument_list|(
name|version
operator|.
name|luceneVersion
argument_list|)
expr_stmt|;
return|return
name|a
return|;
block|}
block|}
block|,
DECL|enum constant|ITALIAN
name|ITALIAN
block|{
annotation|@
name|Override
specifier|protected
name|Analyzer
name|create
parameter_list|(
name|Version
name|version
parameter_list|)
block|{
name|Analyzer
name|a
init|=
operator|new
name|ItalianAnalyzer
argument_list|()
decl_stmt|;
name|a
operator|.
name|setVersion
argument_list|(
name|version
operator|.
name|luceneVersion
argument_list|)
expr_stmt|;
return|return
name|a
return|;
block|}
block|}
block|,
DECL|enum constant|LATVIAN
name|LATVIAN
block|{
annotation|@
name|Override
specifier|protected
name|Analyzer
name|create
parameter_list|(
name|Version
name|version
parameter_list|)
block|{
name|Analyzer
name|a
init|=
operator|new
name|LatvianAnalyzer
argument_list|()
decl_stmt|;
name|a
operator|.
name|setVersion
argument_list|(
name|version
operator|.
name|luceneVersion
argument_list|)
expr_stmt|;
return|return
name|a
return|;
block|}
block|}
block|,
DECL|enum constant|LITHUANIAN
name|LITHUANIAN
block|{
annotation|@
name|Override
specifier|protected
name|Analyzer
name|create
parameter_list|(
name|Version
name|version
parameter_list|)
block|{
name|Analyzer
name|a
init|=
operator|new
name|LithuanianAnalyzer
argument_list|()
decl_stmt|;
name|a
operator|.
name|setVersion
argument_list|(
name|version
operator|.
name|luceneVersion
argument_list|)
expr_stmt|;
return|return
name|a
return|;
block|}
block|}
block|,
DECL|enum constant|NORWEGIAN
name|NORWEGIAN
block|{
annotation|@
name|Override
specifier|protected
name|Analyzer
name|create
parameter_list|(
name|Version
name|version
parameter_list|)
block|{
name|Analyzer
name|a
init|=
operator|new
name|NorwegianAnalyzer
argument_list|()
decl_stmt|;
name|a
operator|.
name|setVersion
argument_list|(
name|version
operator|.
name|luceneVersion
argument_list|)
expr_stmt|;
return|return
name|a
return|;
block|}
block|}
block|,
DECL|enum constant|PERSIAN
name|PERSIAN
block|{
annotation|@
name|Override
specifier|protected
name|Analyzer
name|create
parameter_list|(
name|Version
name|version
parameter_list|)
block|{
name|Analyzer
name|a
init|=
operator|new
name|PersianAnalyzer
argument_list|()
decl_stmt|;
name|a
operator|.
name|setVersion
argument_list|(
name|version
operator|.
name|luceneVersion
argument_list|)
expr_stmt|;
return|return
name|a
return|;
block|}
block|}
block|,
DECL|enum constant|PORTUGUESE
name|PORTUGUESE
block|{
annotation|@
name|Override
specifier|protected
name|Analyzer
name|create
parameter_list|(
name|Version
name|version
parameter_list|)
block|{
name|Analyzer
name|a
init|=
operator|new
name|PortugueseAnalyzer
argument_list|()
decl_stmt|;
name|a
operator|.
name|setVersion
argument_list|(
name|version
operator|.
name|luceneVersion
argument_list|)
expr_stmt|;
return|return
name|a
return|;
block|}
block|}
block|,
DECL|enum constant|ROMANIAN
name|ROMANIAN
block|{
annotation|@
name|Override
specifier|protected
name|Analyzer
name|create
parameter_list|(
name|Version
name|version
parameter_list|)
block|{
name|Analyzer
name|a
init|=
operator|new
name|RomanianAnalyzer
argument_list|()
decl_stmt|;
name|a
operator|.
name|setVersion
argument_list|(
name|version
operator|.
name|luceneVersion
argument_list|)
expr_stmt|;
return|return
name|a
return|;
block|}
block|}
block|,
DECL|enum constant|RUSSIAN
name|RUSSIAN
block|{
annotation|@
name|Override
specifier|protected
name|Analyzer
name|create
parameter_list|(
name|Version
name|version
parameter_list|)
block|{
name|Analyzer
name|a
init|=
operator|new
name|RussianAnalyzer
argument_list|()
decl_stmt|;
name|a
operator|.
name|setVersion
argument_list|(
name|version
operator|.
name|luceneVersion
argument_list|)
expr_stmt|;
return|return
name|a
return|;
block|}
block|}
block|,
DECL|enum constant|SORANI
name|SORANI
block|{
annotation|@
name|Override
specifier|protected
name|Analyzer
name|create
parameter_list|(
name|Version
name|version
parameter_list|)
block|{
name|Analyzer
name|a
init|=
operator|new
name|SoraniAnalyzer
argument_list|()
decl_stmt|;
name|a
operator|.
name|setVersion
argument_list|(
name|version
operator|.
name|luceneVersion
argument_list|)
expr_stmt|;
return|return
name|a
return|;
block|}
block|}
block|,
DECL|enum constant|SPANISH
name|SPANISH
block|{
annotation|@
name|Override
specifier|protected
name|Analyzer
name|create
parameter_list|(
name|Version
name|version
parameter_list|)
block|{
name|Analyzer
name|a
init|=
operator|new
name|SpanishAnalyzer
argument_list|()
decl_stmt|;
name|a
operator|.
name|setVersion
argument_list|(
name|version
operator|.
name|luceneVersion
argument_list|)
expr_stmt|;
return|return
name|a
return|;
block|}
block|}
block|,
DECL|enum constant|SWEDISH
name|SWEDISH
block|{
annotation|@
name|Override
specifier|protected
name|Analyzer
name|create
parameter_list|(
name|Version
name|version
parameter_list|)
block|{
name|Analyzer
name|a
init|=
operator|new
name|SwedishAnalyzer
argument_list|()
decl_stmt|;
name|a
operator|.
name|setVersion
argument_list|(
name|version
operator|.
name|luceneVersion
argument_list|)
expr_stmt|;
return|return
name|a
return|;
block|}
block|}
block|,
DECL|enum constant|TURKISH
name|TURKISH
block|{
annotation|@
name|Override
specifier|protected
name|Analyzer
name|create
parameter_list|(
name|Version
name|version
parameter_list|)
block|{
name|Analyzer
name|a
init|=
operator|new
name|TurkishAnalyzer
argument_list|()
decl_stmt|;
name|a
operator|.
name|setVersion
argument_list|(
name|version
operator|.
name|luceneVersion
argument_list|)
expr_stmt|;
return|return
name|a
return|;
block|}
block|}
block|,
DECL|enum constant|THAI
name|THAI
block|{
annotation|@
name|Override
specifier|protected
name|Analyzer
name|create
parameter_list|(
name|Version
name|version
parameter_list|)
block|{
name|Analyzer
name|a
init|=
operator|new
name|ThaiAnalyzer
argument_list|()
decl_stmt|;
name|a
operator|.
name|setVersion
argument_list|(
name|version
operator|.
name|luceneVersion
argument_list|)
expr_stmt|;
return|return
name|a
return|;
block|}
block|}
block|;
DECL|method|create
specifier|protected
specifier|abstract
name|Analyzer
name|create
parameter_list|(
name|Version
name|version
parameter_list|)
function_decl|;
DECL|field|cache
specifier|protected
specifier|final
name|PreBuiltCacheFactory
operator|.
name|PreBuiltCache
argument_list|<
name|Analyzer
argument_list|>
name|cache
decl_stmt|;
DECL|method|PreBuiltAnalyzers
name|PreBuiltAnalyzers
parameter_list|()
block|{
name|this
argument_list|(
name|PreBuiltCacheFactory
operator|.
name|CachingStrategy
operator|.
name|LUCENE
argument_list|)
expr_stmt|;
block|}
DECL|method|PreBuiltAnalyzers
name|PreBuiltAnalyzers
parameter_list|(
name|PreBuiltCacheFactory
operator|.
name|CachingStrategy
name|cachingStrategy
parameter_list|)
block|{
name|cache
operator|=
name|PreBuiltCacheFactory
operator|.
name|getCache
argument_list|(
name|cachingStrategy
argument_list|)
expr_stmt|;
block|}
DECL|method|getCache
name|PreBuiltCacheFactory
operator|.
name|PreBuiltCache
argument_list|<
name|Analyzer
argument_list|>
name|getCache
parameter_list|()
block|{
return|return
name|cache
return|;
block|}
DECL|method|getAnalyzer
specifier|public
specifier|synchronized
name|Analyzer
name|getAnalyzer
parameter_list|(
name|Version
name|version
parameter_list|)
block|{
name|Analyzer
name|analyzer
init|=
name|cache
operator|.
name|get
argument_list|(
name|version
argument_list|)
decl_stmt|;
if|if
condition|(
name|analyzer
operator|==
literal|null
condition|)
block|{
name|analyzer
operator|=
name|this
operator|.
name|create
argument_list|(
name|version
argument_list|)
expr_stmt|;
name|cache
operator|.
name|put
argument_list|(
name|version
argument_list|,
name|analyzer
argument_list|)
expr_stmt|;
block|}
return|return
name|analyzer
return|;
block|}
comment|/**      * Get a pre built Analyzer by its name or fallback to the default one      * @param name Analyzer name      * @param defaultAnalyzer default Analyzer if name not found      */
DECL|method|getOrDefault
specifier|public
specifier|static
name|PreBuiltAnalyzers
name|getOrDefault
parameter_list|(
name|String
name|name
parameter_list|,
name|PreBuiltAnalyzers
name|defaultAnalyzer
parameter_list|)
block|{
try|try
block|{
return|return
name|valueOf
argument_list|(
name|name
operator|.
name|toUpperCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
return|return
name|defaultAnalyzer
return|;
block|}
block|}
block|}
end_enum

end_unit

