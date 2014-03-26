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
name|ArabicNormalizationFilter
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
name|commongrams
operator|.
name|CommonGramsFilter
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
name|LowerCaseFilter
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
name|StopFilter
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
name|UpperCaseFilter
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
name|GermanStemFilter
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
name|fa
operator|.
name|PersianNormalizationFilter
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
name|fr
operator|.
name|FrenchStemFilter
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
name|miscellaneous
operator|.
name|*
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
name|ngram
operator|.
name|EdgeNGramTokenFilter
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
name|ngram
operator|.
name|NGramTokenFilter
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
name|DutchStemFilter
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
name|payloads
operator|.
name|TypeAsPayloadTokenFilter
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
name|reverse
operator|.
name|ReverseStringFilter
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
name|shingle
operator|.
name|ShingleFilter
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
name|standard
operator|.
name|ClassicFilter
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
name|StandardFilter
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
name|apache
operator|.
name|lucene
operator|.
name|analysis
operator|.
name|util
operator|.
name|ElisionFilter
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
DECL|enum|PreBuiltTokenFilters
specifier|public
enum|enum
name|PreBuiltTokenFilters
block|{
DECL|method|WORD_DELIMITER
DECL|method|WORD_DELIMITER
name|WORD_DELIMITER
parameter_list|(
name|CachingStrategy
operator|.
name|ONE
parameter_list|)
block|{
annotation|@
name|Override
specifier|public
name|TokenStream
name|create
parameter_list|(
name|TokenStream
name|tokenStream
parameter_list|,
name|Version
name|version
parameter_list|)
block|{
return|return
operator|new
name|WordDelimiterFilter
argument_list|(
name|tokenStream
argument_list|,
name|WordDelimiterFilter
operator|.
name|GENERATE_WORD_PARTS
operator||
name|WordDelimiterFilter
operator|.
name|GENERATE_NUMBER_PARTS
operator||
name|WordDelimiterFilter
operator|.
name|SPLIT_ON_CASE_CHANGE
operator||
name|WordDelimiterFilter
operator|.
name|SPLIT_ON_NUMERICS
operator||
name|WordDelimiterFilter
operator|.
name|STEM_ENGLISH_POSSESSIVE
argument_list|,
literal|null
argument_list|)
return|;
block|}
block|}
block|,
DECL|method|STOP
DECL|method|STOP
name|STOP
parameter_list|(
name|CachingStrategy
operator|.
name|LUCENE
parameter_list|)
block|{
annotation|@
name|Override
specifier|public
name|TokenStream
name|create
parameter_list|(
name|TokenStream
name|tokenStream
parameter_list|,
name|Version
name|version
parameter_list|)
block|{
return|return
operator|new
name|StopFilter
argument_list|(
name|version
operator|.
name|luceneVersion
argument_list|,
name|tokenStream
argument_list|,
name|StopAnalyzer
operator|.
name|ENGLISH_STOP_WORDS_SET
argument_list|)
return|;
block|}
block|}
block|,
DECL|method|TRIM
DECL|method|TRIM
name|TRIM
parameter_list|(
name|CachingStrategy
operator|.
name|LUCENE
parameter_list|)
block|{
annotation|@
name|Override
specifier|public
name|TokenStream
name|create
parameter_list|(
name|TokenStream
name|tokenStream
parameter_list|,
name|Version
name|version
parameter_list|)
block|{
return|return
operator|new
name|TrimFilter
argument_list|(
name|version
operator|.
name|luceneVersion
argument_list|,
name|tokenStream
argument_list|)
return|;
block|}
block|}
block|,
DECL|method|REVERSE
DECL|method|REVERSE
name|REVERSE
parameter_list|(
name|CachingStrategy
operator|.
name|LUCENE
parameter_list|)
block|{
annotation|@
name|Override
specifier|public
name|TokenStream
name|create
parameter_list|(
name|TokenStream
name|tokenStream
parameter_list|,
name|Version
name|version
parameter_list|)
block|{
return|return
operator|new
name|ReverseStringFilter
argument_list|(
name|version
operator|.
name|luceneVersion
argument_list|,
name|tokenStream
argument_list|)
return|;
block|}
block|}
block|,
DECL|method|ASCIIFOLDING
DECL|method|ASCIIFOLDING
name|ASCIIFOLDING
parameter_list|(
name|CachingStrategy
operator|.
name|ONE
parameter_list|)
block|{
annotation|@
name|Override
specifier|public
name|TokenStream
name|create
parameter_list|(
name|TokenStream
name|tokenStream
parameter_list|,
name|Version
name|version
parameter_list|)
block|{
return|return
operator|new
name|ASCIIFoldingFilter
argument_list|(
name|tokenStream
argument_list|)
return|;
block|}
block|}
block|,
DECL|method|LENGTH
DECL|method|LENGTH
name|LENGTH
parameter_list|(
name|CachingStrategy
operator|.
name|LUCENE
parameter_list|)
block|{
annotation|@
name|Override
specifier|public
name|TokenStream
name|create
parameter_list|(
name|TokenStream
name|tokenStream
parameter_list|,
name|Version
name|version
parameter_list|)
block|{
return|return
operator|new
name|LengthFilter
argument_list|(
name|version
operator|.
name|luceneVersion
argument_list|,
name|tokenStream
argument_list|,
literal|0
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
return|;
block|}
block|}
block|,
DECL|method|COMMON_GRAMS
DECL|method|COMMON_GRAMS
name|COMMON_GRAMS
parameter_list|(
name|CachingStrategy
operator|.
name|LUCENE
parameter_list|)
block|{
annotation|@
name|Override
specifier|public
name|TokenStream
name|create
parameter_list|(
name|TokenStream
name|tokenStream
parameter_list|,
name|Version
name|version
parameter_list|)
block|{
return|return
operator|new
name|CommonGramsFilter
argument_list|(
name|version
operator|.
name|luceneVersion
argument_list|,
name|tokenStream
argument_list|,
name|CharArraySet
operator|.
name|EMPTY_SET
argument_list|)
return|;
block|}
block|}
block|,
DECL|method|LOWERCASE
DECL|method|LOWERCASE
name|LOWERCASE
parameter_list|(
name|CachingStrategy
operator|.
name|LUCENE
parameter_list|)
block|{
annotation|@
name|Override
specifier|public
name|TokenStream
name|create
parameter_list|(
name|TokenStream
name|tokenStream
parameter_list|,
name|Version
name|version
parameter_list|)
block|{
return|return
operator|new
name|LowerCaseFilter
argument_list|(
name|version
operator|.
name|luceneVersion
argument_list|,
name|tokenStream
argument_list|)
return|;
block|}
block|}
block|,
DECL|method|UPPERCASE
DECL|method|UPPERCASE
name|UPPERCASE
parameter_list|(
name|CachingStrategy
operator|.
name|LUCENE
parameter_list|)
block|{
annotation|@
name|Override
specifier|public
name|TokenStream
name|create
parameter_list|(
name|TokenStream
name|tokenStream
parameter_list|,
name|Version
name|version
parameter_list|)
block|{
return|return
operator|new
name|UpperCaseFilter
argument_list|(
name|version
operator|.
name|luceneVersion
argument_list|,
name|tokenStream
argument_list|)
return|;
block|}
block|}
block|,
DECL|method|KSTEM
DECL|method|KSTEM
name|KSTEM
parameter_list|(
name|CachingStrategy
operator|.
name|ONE
parameter_list|)
block|{
annotation|@
name|Override
specifier|public
name|TokenStream
name|create
parameter_list|(
name|TokenStream
name|tokenStream
parameter_list|,
name|Version
name|version
parameter_list|)
block|{
return|return
operator|new
name|KStemFilter
argument_list|(
name|tokenStream
argument_list|)
return|;
block|}
block|}
block|,
DECL|method|PORTER_STEM
DECL|method|PORTER_STEM
name|PORTER_STEM
parameter_list|(
name|CachingStrategy
operator|.
name|ONE
parameter_list|)
block|{
annotation|@
name|Override
specifier|public
name|TokenStream
name|create
parameter_list|(
name|TokenStream
name|tokenStream
parameter_list|,
name|Version
name|version
parameter_list|)
block|{
return|return
operator|new
name|PorterStemFilter
argument_list|(
name|tokenStream
argument_list|)
return|;
block|}
block|}
block|,
DECL|method|STANDARD
DECL|method|STANDARD
name|STANDARD
parameter_list|(
name|CachingStrategy
operator|.
name|LUCENE
parameter_list|)
block|{
annotation|@
name|Override
specifier|public
name|TokenStream
name|create
parameter_list|(
name|TokenStream
name|tokenStream
parameter_list|,
name|Version
name|version
parameter_list|)
block|{
return|return
operator|new
name|StandardFilter
argument_list|(
name|version
operator|.
name|luceneVersion
argument_list|,
name|tokenStream
argument_list|)
return|;
block|}
block|}
block|,
DECL|method|CLASSIC
DECL|method|CLASSIC
name|CLASSIC
parameter_list|(
name|CachingStrategy
operator|.
name|ONE
parameter_list|)
block|{
annotation|@
name|Override
specifier|public
name|TokenStream
name|create
parameter_list|(
name|TokenStream
name|tokenStream
parameter_list|,
name|Version
name|version
parameter_list|)
block|{
return|return
operator|new
name|ClassicFilter
argument_list|(
name|tokenStream
argument_list|)
return|;
block|}
block|}
block|,
DECL|method|NGRAM
DECL|method|NGRAM
name|NGRAM
parameter_list|(
name|CachingStrategy
operator|.
name|LUCENE
parameter_list|)
block|{
annotation|@
name|Override
specifier|public
name|TokenStream
name|create
parameter_list|(
name|TokenStream
name|tokenStream
parameter_list|,
name|Version
name|version
parameter_list|)
block|{
return|return
operator|new
name|NGramTokenFilter
argument_list|(
name|version
operator|.
name|luceneVersion
argument_list|,
name|tokenStream
argument_list|)
return|;
block|}
block|}
block|,
DECL|method|EDGE_NGRAM
DECL|method|EDGE_NGRAM
name|EDGE_NGRAM
parameter_list|(
name|CachingStrategy
operator|.
name|LUCENE
parameter_list|)
block|{
annotation|@
name|Override
specifier|public
name|TokenStream
name|create
parameter_list|(
name|TokenStream
name|tokenStream
parameter_list|,
name|Version
name|version
parameter_list|)
block|{
return|return
operator|new
name|EdgeNGramTokenFilter
argument_list|(
name|version
operator|.
name|luceneVersion
argument_list|,
name|tokenStream
argument_list|,
name|EdgeNGramTokenFilter
operator|.
name|DEFAULT_MIN_GRAM_SIZE
argument_list|,
name|EdgeNGramTokenFilter
operator|.
name|DEFAULT_MAX_GRAM_SIZE
argument_list|)
return|;
block|}
block|}
block|,
DECL|method|UNIQUE
DECL|method|UNIQUE
name|UNIQUE
parameter_list|(
name|CachingStrategy
operator|.
name|ONE
parameter_list|)
block|{
annotation|@
name|Override
specifier|public
name|TokenStream
name|create
parameter_list|(
name|TokenStream
name|tokenStream
parameter_list|,
name|Version
name|version
parameter_list|)
block|{
return|return
operator|new
name|UniqueTokenFilter
argument_list|(
name|tokenStream
argument_list|)
return|;
block|}
block|}
block|,
DECL|method|TRUNCATE
DECL|method|TRUNCATE
name|TRUNCATE
parameter_list|(
name|CachingStrategy
operator|.
name|ONE
parameter_list|)
block|{
annotation|@
name|Override
specifier|public
name|TokenStream
name|create
parameter_list|(
name|TokenStream
name|tokenStream
parameter_list|,
name|Version
name|version
parameter_list|)
block|{
return|return
operator|new
name|TruncateTokenFilter
argument_list|(
name|tokenStream
argument_list|,
literal|10
argument_list|)
return|;
block|}
block|}
block|,
comment|// Extended Token Filters
DECL|method|SNOWBALL
DECL|method|SNOWBALL
name|SNOWBALL
parameter_list|(
name|CachingStrategy
operator|.
name|ONE
parameter_list|)
block|{
annotation|@
name|Override
specifier|public
name|TokenStream
name|create
parameter_list|(
name|TokenStream
name|tokenStream
parameter_list|,
name|Version
name|version
parameter_list|)
block|{
return|return
operator|new
name|SnowballFilter
argument_list|(
name|tokenStream
argument_list|,
literal|"English"
argument_list|)
return|;
block|}
block|}
block|,
DECL|method|STEMMER
DECL|method|STEMMER
name|STEMMER
parameter_list|(
name|CachingStrategy
operator|.
name|ONE
parameter_list|)
block|{
annotation|@
name|Override
specifier|public
name|TokenStream
name|create
parameter_list|(
name|TokenStream
name|tokenStream
parameter_list|,
name|Version
name|version
parameter_list|)
block|{
return|return
operator|new
name|PorterStemFilter
argument_list|(
name|tokenStream
argument_list|)
return|;
block|}
block|}
block|,
DECL|method|ELISION
DECL|method|ELISION
name|ELISION
parameter_list|(
name|CachingStrategy
operator|.
name|ONE
parameter_list|)
block|{
annotation|@
name|Override
specifier|public
name|TokenStream
name|create
parameter_list|(
name|TokenStream
name|tokenStream
parameter_list|,
name|Version
name|version
parameter_list|)
block|{
return|return
operator|new
name|ElisionFilter
argument_list|(
name|tokenStream
argument_list|,
name|FrenchAnalyzer
operator|.
name|DEFAULT_ARTICLES
argument_list|)
return|;
block|}
block|}
block|,
DECL|method|ARABIC_STEM
DECL|method|ARABIC_STEM
name|ARABIC_STEM
parameter_list|(
name|CachingStrategy
operator|.
name|ONE
parameter_list|)
block|{
annotation|@
name|Override
specifier|public
name|TokenStream
name|create
parameter_list|(
name|TokenStream
name|tokenStream
parameter_list|,
name|Version
name|version
parameter_list|)
block|{
return|return
operator|new
name|ArabicStemFilter
argument_list|(
name|tokenStream
argument_list|)
return|;
block|}
block|}
block|,
DECL|method|BRAZILIAN_STEM
DECL|method|BRAZILIAN_STEM
name|BRAZILIAN_STEM
parameter_list|(
name|CachingStrategy
operator|.
name|ONE
parameter_list|)
block|{
annotation|@
name|Override
specifier|public
name|TokenStream
name|create
parameter_list|(
name|TokenStream
name|tokenStream
parameter_list|,
name|Version
name|version
parameter_list|)
block|{
return|return
operator|new
name|BrazilianStemFilter
argument_list|(
name|tokenStream
argument_list|)
return|;
block|}
block|}
block|,
DECL|method|CZECH_STEM
DECL|method|CZECH_STEM
name|CZECH_STEM
parameter_list|(
name|CachingStrategy
operator|.
name|ONE
parameter_list|)
block|{
annotation|@
name|Override
specifier|public
name|TokenStream
name|create
parameter_list|(
name|TokenStream
name|tokenStream
parameter_list|,
name|Version
name|version
parameter_list|)
block|{
return|return
operator|new
name|CzechStemFilter
argument_list|(
name|tokenStream
argument_list|)
return|;
block|}
block|}
block|,
DECL|method|DUTCH_STEM
DECL|method|DUTCH_STEM
name|DUTCH_STEM
parameter_list|(
name|CachingStrategy
operator|.
name|ONE
parameter_list|)
block|{
annotation|@
name|Override
specifier|public
name|TokenStream
name|create
parameter_list|(
name|TokenStream
name|tokenStream
parameter_list|,
name|Version
name|version
parameter_list|)
block|{
return|return
operator|new
name|DutchStemFilter
argument_list|(
name|tokenStream
argument_list|)
return|;
block|}
block|}
block|,
DECL|method|FRENCH_STEM
DECL|method|FRENCH_STEM
name|FRENCH_STEM
parameter_list|(
name|CachingStrategy
operator|.
name|ONE
parameter_list|)
block|{
annotation|@
name|Override
specifier|public
name|TokenStream
name|create
parameter_list|(
name|TokenStream
name|tokenStream
parameter_list|,
name|Version
name|version
parameter_list|)
block|{
return|return
operator|new
name|FrenchStemFilter
argument_list|(
name|tokenStream
argument_list|)
return|;
block|}
block|}
block|,
DECL|method|GERMAN_STEM
DECL|method|GERMAN_STEM
name|GERMAN_STEM
parameter_list|(
name|CachingStrategy
operator|.
name|ONE
parameter_list|)
block|{
annotation|@
name|Override
specifier|public
name|TokenStream
name|create
parameter_list|(
name|TokenStream
name|tokenStream
parameter_list|,
name|Version
name|version
parameter_list|)
block|{
return|return
operator|new
name|GermanStemFilter
argument_list|(
name|tokenStream
argument_list|)
return|;
block|}
block|}
block|,
DECL|method|RUSSIAN_STEM
DECL|method|RUSSIAN_STEM
name|RUSSIAN_STEM
parameter_list|(
name|CachingStrategy
operator|.
name|ONE
parameter_list|)
block|{
annotation|@
name|Override
specifier|public
name|TokenStream
name|create
parameter_list|(
name|TokenStream
name|tokenStream
parameter_list|,
name|Version
name|version
parameter_list|)
block|{
return|return
operator|new
name|SnowballFilter
argument_list|(
name|tokenStream
argument_list|,
literal|"Russian"
argument_list|)
return|;
block|}
block|}
block|,
DECL|method|KEYWORD_REPEAT
DECL|method|KEYWORD_REPEAT
name|KEYWORD_REPEAT
parameter_list|(
name|CachingStrategy
operator|.
name|ONE
parameter_list|)
block|{
annotation|@
name|Override
specifier|public
name|TokenStream
name|create
parameter_list|(
name|TokenStream
name|tokenStream
parameter_list|,
name|Version
name|version
parameter_list|)
block|{
return|return
operator|new
name|KeywordRepeatFilter
argument_list|(
name|tokenStream
argument_list|)
return|;
block|}
block|}
block|,
DECL|method|ARABIC_NORMALIZATION
DECL|method|ARABIC_NORMALIZATION
name|ARABIC_NORMALIZATION
parameter_list|(
name|CachingStrategy
operator|.
name|ONE
parameter_list|)
block|{
annotation|@
name|Override
specifier|public
name|TokenStream
name|create
parameter_list|(
name|TokenStream
name|tokenStream
parameter_list|,
name|Version
name|version
parameter_list|)
block|{
return|return
operator|new
name|ArabicNormalizationFilter
argument_list|(
name|tokenStream
argument_list|)
return|;
block|}
block|}
block|,
DECL|method|PERSIAN_NORMALIZATION
DECL|method|PERSIAN_NORMALIZATION
name|PERSIAN_NORMALIZATION
parameter_list|(
name|CachingStrategy
operator|.
name|ONE
parameter_list|)
block|{
annotation|@
name|Override
specifier|public
name|TokenStream
name|create
parameter_list|(
name|TokenStream
name|tokenStream
parameter_list|,
name|Version
name|version
parameter_list|)
block|{
return|return
operator|new
name|PersianNormalizationFilter
argument_list|(
name|tokenStream
argument_list|)
return|;
block|}
block|}
block|,
DECL|method|TYPE_AS_PAYLOAD
DECL|method|TYPE_AS_PAYLOAD
name|TYPE_AS_PAYLOAD
parameter_list|(
name|CachingStrategy
operator|.
name|ONE
parameter_list|)
block|{
annotation|@
name|Override
specifier|public
name|TokenStream
name|create
parameter_list|(
name|TokenStream
name|tokenStream
parameter_list|,
name|Version
name|version
parameter_list|)
block|{
return|return
operator|new
name|TypeAsPayloadTokenFilter
argument_list|(
name|tokenStream
argument_list|)
return|;
block|}
block|}
block|,
DECL|method|SHINGLE
DECL|method|SHINGLE
name|SHINGLE
parameter_list|(
name|CachingStrategy
operator|.
name|ONE
parameter_list|)
block|{
annotation|@
name|Override
specifier|public
name|TokenStream
name|create
parameter_list|(
name|TokenStream
name|tokenStream
parameter_list|,
name|Version
name|version
parameter_list|)
block|{
return|return
operator|new
name|ShingleFilter
argument_list|(
name|tokenStream
argument_list|)
return|;
block|}
block|}
block|;
DECL|method|create
specifier|abstract
specifier|public
name|TokenStream
name|create
parameter_list|(
name|TokenStream
name|tokenStream
parameter_list|,
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
name|TokenFilterFactory
argument_list|>
name|cache
decl_stmt|;
DECL|method|PreBuiltTokenFilters
name|PreBuiltTokenFilters
parameter_list|(
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
DECL|method|getTokenFilterFactory
specifier|public
specifier|synchronized
name|TokenFilterFactory
name|getTokenFilterFactory
parameter_list|(
specifier|final
name|Version
name|version
parameter_list|)
block|{
name|TokenFilterFactory
name|factory
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
name|factory
operator|==
literal|null
condition|)
block|{
specifier|final
name|String
name|finalName
init|=
name|name
argument_list|()
decl_stmt|;
name|factory
operator|=
operator|new
name|TokenFilterFactory
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|String
name|name
parameter_list|()
block|{
return|return
name|finalName
operator|.
name|toLowerCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|TokenStream
name|create
parameter_list|(
name|TokenStream
name|tokenStream
parameter_list|)
block|{
return|return
name|valueOf
argument_list|(
name|finalName
argument_list|)
operator|.
name|create
argument_list|(
name|tokenStream
argument_list|,
name|version
argument_list|)
return|;
block|}
block|}
expr_stmt|;
name|cache
operator|.
name|put
argument_list|(
name|version
argument_list|,
name|factory
argument_list|)
expr_stmt|;
block|}
return|return
name|factory
return|;
block|}
comment|/**      * Get a pre built TokenFilter by its name or fallback to the default one      * @param name TokenFilter name      * @param defaultTokenFilter default TokenFilter if name not found      */
DECL|method|getOrDefault
specifier|public
specifier|static
name|PreBuiltTokenFilters
name|getOrDefault
parameter_list|(
name|String
name|name
parameter_list|,
name|PreBuiltTokenFilters
name|defaultTokenFilter
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
name|defaultTokenFilter
return|;
block|}
block|}
block|}
end_enum

end_unit

