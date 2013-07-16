begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|ngram
operator|.
name|Lucene43EdgeNGramTokenizer
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticSearchIllegalArgumentException
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
name|util
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
name|java
operator|.
name|io
operator|.
name|Reader
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|analysis
operator|.
name|NGramTokenizerFactory
operator|.
name|parseTokenChars
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|EdgeNGramTokenizerFactory
specifier|public
class|class
name|EdgeNGramTokenizerFactory
extends|extends
name|AbstractTokenizerFactory
block|{
DECL|field|minGram
specifier|private
specifier|final
name|int
name|minGram
decl_stmt|;
DECL|field|maxGram
specifier|private
specifier|final
name|int
name|maxGram
decl_stmt|;
DECL|field|side
specifier|private
specifier|final
name|Lucene43EdgeNGramTokenizer
operator|.
name|Side
name|side
decl_stmt|;
DECL|field|matcher
specifier|private
specifier|final
name|CharMatcher
name|matcher
decl_stmt|;
annotation|@
name|Inject
DECL|method|EdgeNGramTokenizerFactory
specifier|public
name|EdgeNGramTokenizerFactory
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
name|minGram
operator|=
name|settings
operator|.
name|getAsInt
argument_list|(
literal|"min_gram"
argument_list|,
name|NGramTokenizer
operator|.
name|DEFAULT_MIN_NGRAM_SIZE
argument_list|)
expr_stmt|;
name|this
operator|.
name|maxGram
operator|=
name|settings
operator|.
name|getAsInt
argument_list|(
literal|"max_gram"
argument_list|,
name|NGramTokenizer
operator|.
name|DEFAULT_MAX_NGRAM_SIZE
argument_list|)
expr_stmt|;
name|this
operator|.
name|side
operator|=
name|Lucene43EdgeNGramTokenizer
operator|.
name|Side
operator|.
name|getSide
argument_list|(
name|settings
operator|.
name|get
argument_list|(
literal|"side"
argument_list|,
name|Lucene43EdgeNGramTokenizer
operator|.
name|DEFAULT_SIDE
operator|.
name|getLabel
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|matcher
operator|=
name|parseTokenChars
argument_list|(
name|settings
operator|.
name|getAsArray
argument_list|(
literal|"token_chars"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|create
specifier|public
name|Tokenizer
name|create
parameter_list|(
name|Reader
name|reader
parameter_list|)
block|{
specifier|final
name|Version
name|version
init|=
name|this
operator|.
name|version
operator|==
name|Version
operator|.
name|LUCENE_43
condition|?
name|Version
operator|.
name|LUCENE_44
else|:
name|this
operator|.
name|version
decl_stmt|;
comment|// we supported it since 4.3
if|if
condition|(
name|version
operator|.
name|onOrAfter
argument_list|(
name|Version
operator|.
name|LUCENE_44
argument_list|)
condition|)
block|{
if|if
condition|(
name|side
operator|==
name|Lucene43EdgeNGramTokenizer
operator|.
name|Side
operator|.
name|BACK
condition|)
block|{
throw|throw
operator|new
name|ElasticSearchIllegalArgumentException
argument_list|(
literal|"side=BACK is not supported anymore. Please fix your analysis chain or use"
operator|+
literal|" an older compatibility version (<=4.2) but beware that it might cause highlighting bugs."
argument_list|)
throw|;
block|}
comment|// LUCENE MONITOR: this token filter is a copy from lucene trunk and should go away once we upgrade to lucene 4.4
if|if
condition|(
name|matcher
operator|==
literal|null
condition|)
block|{
return|return
operator|new
name|EdgeNGramTokenizer
argument_list|(
name|version
argument_list|,
name|reader
argument_list|,
name|minGram
argument_list|,
name|maxGram
argument_list|)
return|;
block|}
else|else
block|{
return|return
operator|new
name|EdgeNGramTokenizer
argument_list|(
name|version
argument_list|,
name|reader
argument_list|,
name|minGram
argument_list|,
name|maxGram
argument_list|)
block|{
annotation|@
name|Override
specifier|protected
name|boolean
name|isTokenChar
parameter_list|(
name|int
name|chr
parameter_list|)
block|{
return|return
name|matcher
operator|.
name|isTokenChar
argument_list|(
name|chr
argument_list|)
return|;
block|}
block|}
return|;
block|}
block|}
else|else
block|{
return|return
operator|new
name|Lucene43EdgeNGramTokenizer
argument_list|(
name|version
argument_list|,
name|reader
argument_list|,
name|side
argument_list|,
name|minGram
argument_list|,
name|maxGram
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

