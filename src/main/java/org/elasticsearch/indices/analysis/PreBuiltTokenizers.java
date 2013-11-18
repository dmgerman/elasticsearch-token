begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|Tokenizer
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
name|KeywordTokenizer
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
name|LetterTokenizer
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
name|LowerCaseTokenizer
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
name|WhitespaceTokenizer
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
name|EdgeNGramTokenizer
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
name|NGramTokenizer
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
name|path
operator|.
name|PathHierarchyTokenizer
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
name|pattern
operator|.
name|PatternTokenizer
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
name|ClassicTokenizer
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
name|StandardTokenizer
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
name|UAX29URLEmailTokenizer
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
name|PreBuiltCacheFactory
operator|.
name|CachingStrategy
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Reader
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
DECL|enum|PreBuiltTokenizers
specifier|public
enum|enum
name|PreBuiltTokenizers
block|{
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
specifier|protected
name|Tokenizer
name|create
parameter_list|(
name|Reader
name|reader
parameter_list|,
name|Version
name|version
parameter_list|)
block|{
return|return
operator|new
name|StandardTokenizer
argument_list|(
name|version
operator|.
name|luceneVersion
argument_list|,
name|reader
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
name|LUCENE
parameter_list|)
block|{
annotation|@
name|Override
specifier|protected
name|Tokenizer
name|create
parameter_list|(
name|Reader
name|reader
parameter_list|,
name|Version
name|version
parameter_list|)
block|{
return|return
operator|new
name|ClassicTokenizer
argument_list|(
name|version
operator|.
name|luceneVersion
argument_list|,
name|reader
argument_list|)
return|;
block|}
block|}
block|,
DECL|method|UAX_URL_EMAIL
DECL|method|UAX_URL_EMAIL
name|UAX_URL_EMAIL
parameter_list|(
name|CachingStrategy
operator|.
name|LUCENE
parameter_list|)
block|{
annotation|@
name|Override
specifier|protected
name|Tokenizer
name|create
parameter_list|(
name|Reader
name|reader
parameter_list|,
name|Version
name|version
parameter_list|)
block|{
return|return
operator|new
name|UAX29URLEmailTokenizer
argument_list|(
name|version
operator|.
name|luceneVersion
argument_list|,
name|reader
argument_list|)
return|;
block|}
block|}
block|,
DECL|method|PATH_HIERARCHY
DECL|method|PATH_HIERARCHY
name|PATH_HIERARCHY
parameter_list|(
name|CachingStrategy
operator|.
name|ONE
parameter_list|)
block|{
annotation|@
name|Override
specifier|protected
name|Tokenizer
name|create
parameter_list|(
name|Reader
name|reader
parameter_list|,
name|Version
name|version
parameter_list|)
block|{
return|return
operator|new
name|PathHierarchyTokenizer
argument_list|(
name|reader
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
name|Tokenizer
name|create
parameter_list|(
name|Reader
name|reader
parameter_list|,
name|Version
name|version
parameter_list|)
block|{
return|return
operator|new
name|KeywordTokenizer
argument_list|(
name|reader
argument_list|)
return|;
block|}
block|}
block|,
DECL|method|LETTER
DECL|method|LETTER
name|LETTER
parameter_list|(
name|CachingStrategy
operator|.
name|LUCENE
parameter_list|)
block|{
annotation|@
name|Override
specifier|protected
name|Tokenizer
name|create
parameter_list|(
name|Reader
name|reader
parameter_list|,
name|Version
name|version
parameter_list|)
block|{
return|return
operator|new
name|LetterTokenizer
argument_list|(
name|version
operator|.
name|luceneVersion
argument_list|,
name|reader
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
specifier|protected
name|Tokenizer
name|create
parameter_list|(
name|Reader
name|reader
parameter_list|,
name|Version
name|version
parameter_list|)
block|{
return|return
operator|new
name|LowerCaseTokenizer
argument_list|(
name|version
operator|.
name|luceneVersion
argument_list|,
name|reader
argument_list|)
return|;
block|}
block|}
block|,
DECL|method|WHITESPACE
DECL|method|WHITESPACE
name|WHITESPACE
parameter_list|(
name|CachingStrategy
operator|.
name|LUCENE
parameter_list|)
block|{
annotation|@
name|Override
specifier|protected
name|Tokenizer
name|create
parameter_list|(
name|Reader
name|reader
parameter_list|,
name|Version
name|version
parameter_list|)
block|{
return|return
operator|new
name|WhitespaceTokenizer
argument_list|(
name|version
operator|.
name|luceneVersion
argument_list|,
name|reader
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
specifier|protected
name|Tokenizer
name|create
parameter_list|(
name|Reader
name|reader
parameter_list|,
name|Version
name|version
parameter_list|)
block|{
return|return
operator|new
name|NGramTokenizer
argument_list|(
name|version
operator|.
name|luceneVersion
argument_list|,
name|reader
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
specifier|protected
name|Tokenizer
name|create
parameter_list|(
name|Reader
name|reader
parameter_list|,
name|Version
name|version
parameter_list|)
block|{
return|return
operator|new
name|EdgeNGramTokenizer
argument_list|(
name|version
operator|.
name|luceneVersion
argument_list|,
name|reader
argument_list|,
name|EdgeNGramTokenizer
operator|.
name|DEFAULT_MIN_GRAM_SIZE
argument_list|,
name|EdgeNGramTokenizer
operator|.
name|DEFAULT_MAX_GRAM_SIZE
argument_list|)
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
name|ONE
parameter_list|)
block|{
annotation|@
name|Override
specifier|protected
name|Tokenizer
name|create
parameter_list|(
name|Reader
name|reader
parameter_list|,
name|Version
name|version
parameter_list|)
block|{
return|return
operator|new
name|PatternTokenizer
argument_list|(
name|reader
argument_list|,
name|Regex
operator|.
name|compile
argument_list|(
literal|"\\W+"
argument_list|,
literal|null
argument_list|)
argument_list|,
operator|-
literal|1
argument_list|)
return|;
block|}
block|}
block|;
DECL|method|create
specifier|abstract
specifier|protected
name|Tokenizer
name|create
parameter_list|(
name|Reader
name|reader
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
name|TokenizerFactory
argument_list|>
name|cache
decl_stmt|;
DECL|method|PreBuiltTokenizers
name|PreBuiltTokenizers
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
DECL|method|getTokenizerFactory
specifier|public
specifier|synchronized
name|TokenizerFactory
name|getTokenizerFactory
parameter_list|(
specifier|final
name|Version
name|version
parameter_list|)
block|{
name|TokenizerFactory
name|tokenizerFactory
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
name|tokenizerFactory
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
name|tokenizerFactory
operator|=
operator|new
name|TokenizerFactory
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
name|Tokenizer
name|create
parameter_list|(
name|Reader
name|reader
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
name|reader
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
name|tokenizerFactory
argument_list|)
expr_stmt|;
block|}
return|return
name|tokenizerFactory
return|;
block|}
block|}
end_enum

end_unit

