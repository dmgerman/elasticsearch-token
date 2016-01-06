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
name|miscellaneous
operator|.
name|KeepWordFilter
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
name|Lucene43KeepWordFilter
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

begin_comment
comment|/**  * A {@link TokenFilterFactory} for {@link KeepWordFilter}. This filter only  * keep tokens that are contained in the term set configured via  * {@value #KEEP_WORDS_KEY} setting. This filter acts like an inverse stop  * filter.  *<p>  * Configuration options:  *<ul>  *<li>{@value #KEEP_WORDS_KEY} the array of words / tokens to keep.</li>  *<li>{@value #KEEP_WORDS_PATH_KEY} an reference to a file containing the words  * / tokens to keep. Note: this is an alternative to {@value #KEEP_WORDS_KEY} if  * both are set an exception will be thrown.</li>  *<li>{@value #ENABLE_POS_INC_KEY}<code>true</code> iff the filter should  * maintain position increments for dropped tokens. The default is  *<code>true</code>.</li>  *<li>{@value #KEEP_WORDS_CASE_KEY} to use case sensitive keep words. The  * default is<code>false</code> which corresponds to case-sensitive.</li>  *</ul>  *  * @see StopTokenFilterFactory  */
end_comment

begin_class
DECL|class|KeepWordFilterFactory
specifier|public
class|class
name|KeepWordFilterFactory
extends|extends
name|AbstractTokenFilterFactory
block|{
DECL|field|keepWords
specifier|private
specifier|final
name|CharArraySet
name|keepWords
decl_stmt|;
DECL|field|enablePositionIncrements
specifier|private
specifier|final
name|boolean
name|enablePositionIncrements
decl_stmt|;
DECL|field|KEEP_WORDS_KEY
specifier|private
specifier|static
specifier|final
name|String
name|KEEP_WORDS_KEY
init|=
literal|"keep_words"
decl_stmt|;
DECL|field|KEEP_WORDS_PATH_KEY
specifier|private
specifier|static
specifier|final
name|String
name|KEEP_WORDS_PATH_KEY
init|=
name|KEEP_WORDS_KEY
operator|+
literal|"_path"
decl_stmt|;
DECL|field|KEEP_WORDS_CASE_KEY
specifier|private
specifier|static
specifier|final
name|String
name|KEEP_WORDS_CASE_KEY
init|=
name|KEEP_WORDS_KEY
operator|+
literal|"_case"
decl_stmt|;
comment|// for javadoc
DECL|field|ENABLE_POS_INC_KEY
specifier|private
specifier|static
specifier|final
name|String
name|ENABLE_POS_INC_KEY
init|=
literal|"enable_position_increments"
decl_stmt|;
DECL|method|KeepWordFilterFactory
specifier|public
name|KeepWordFilterFactory
parameter_list|(
name|IndexSettings
name|indexSettings
parameter_list|,
name|Environment
name|env
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
specifier|final
name|String
index|[]
name|arrayKeepWords
init|=
name|settings
operator|.
name|getAsArray
argument_list|(
name|KEEP_WORDS_KEY
argument_list|,
literal|null
argument_list|)
decl_stmt|;
specifier|final
name|String
name|keepWordsPath
init|=
name|settings
operator|.
name|get
argument_list|(
name|KEEP_WORDS_PATH_KEY
argument_list|,
literal|null
argument_list|)
decl_stmt|;
if|if
condition|(
operator|(
name|arrayKeepWords
operator|==
literal|null
operator|&&
name|keepWordsPath
operator|==
literal|null
operator|)
operator|||
operator|(
name|arrayKeepWords
operator|!=
literal|null
operator|&&
name|keepWordsPath
operator|!=
literal|null
operator|)
condition|)
block|{
comment|// we don't allow both or none
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"keep requires either `"
operator|+
name|KEEP_WORDS_KEY
operator|+
literal|"` or `"
operator|+
name|KEEP_WORDS_PATH_KEY
operator|+
literal|"` to be configured"
argument_list|)
throw|;
block|}
if|if
condition|(
name|version
operator|.
name|onOrAfter
argument_list|(
name|Version
operator|.
name|LUCENE_4_4
argument_list|)
operator|&&
name|settings
operator|.
name|get
argument_list|(
name|ENABLE_POS_INC_KEY
argument_list|)
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|ENABLE_POS_INC_KEY
operator|+
literal|" is not supported anymore. Please fix your analysis chain or use"
operator|+
literal|" an older compatibility version (<=4.3) but beware that it might cause highlighting bugs."
argument_list|)
throw|;
block|}
name|enablePositionIncrements
operator|=
name|version
operator|.
name|onOrAfter
argument_list|(
name|Version
operator|.
name|LUCENE_4_4
argument_list|)
condition|?
literal|true
else|:
name|settings
operator|.
name|getAsBoolean
argument_list|(
name|ENABLE_POS_INC_KEY
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|this
operator|.
name|keepWords
operator|=
name|Analysis
operator|.
name|getWordSet
argument_list|(
name|env
argument_list|,
name|settings
argument_list|,
name|KEEP_WORDS_KEY
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
if|if
condition|(
name|version
operator|.
name|onOrAfter
argument_list|(
name|Version
operator|.
name|LUCENE_4_4
argument_list|)
condition|)
block|{
return|return
operator|new
name|KeepWordFilter
argument_list|(
name|tokenStream
argument_list|,
name|keepWords
argument_list|)
return|;
block|}
else|else
block|{
annotation|@
name|SuppressWarnings
argument_list|(
literal|"deprecation"
argument_list|)
specifier|final
name|TokenStream
name|filter
init|=
operator|new
name|Lucene43KeepWordFilter
argument_list|(
name|enablePositionIncrements
argument_list|,
name|tokenStream
argument_list|,
name|keepWords
argument_list|)
decl_stmt|;
return|return
name|filter
return|;
block|}
block|}
block|}
end_class

end_unit

