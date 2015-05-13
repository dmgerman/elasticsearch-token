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
name|ja
operator|.
name|JapaneseTokenizer
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
name|ja
operator|.
name|JapaneseTokenizer
operator|.
name|Mode
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
name|ja
operator|.
name|dict
operator|.
name|UserDictionary
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchException
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
name|IOException
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

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|KuromojiTokenizerFactory
specifier|public
class|class
name|KuromojiTokenizerFactory
extends|extends
name|AbstractTokenizerFactory
block|{
DECL|field|USER_DICT_OPTION
specifier|private
specifier|static
specifier|final
name|String
name|USER_DICT_OPTION
init|=
literal|"user_dictionary"
decl_stmt|;
DECL|field|userDictionary
specifier|private
specifier|final
name|UserDictionary
name|userDictionary
decl_stmt|;
DECL|field|mode
specifier|private
specifier|final
name|Mode
name|mode
decl_stmt|;
DECL|field|discartPunctuation
specifier|private
name|boolean
name|discartPunctuation
decl_stmt|;
annotation|@
name|Inject
DECL|method|KuromojiTokenizerFactory
specifier|public
name|KuromojiTokenizerFactory
parameter_list|(
name|Index
name|index
parameter_list|,
annotation|@
name|IndexSettings
name|Settings
name|indexSettings
parameter_list|,
name|Environment
name|env
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
name|mode
operator|=
name|getMode
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|userDictionary
operator|=
name|getUserDictionary
argument_list|(
name|env
argument_list|,
name|settings
argument_list|)
expr_stmt|;
name|discartPunctuation
operator|=
name|settings
operator|.
name|getAsBoolean
argument_list|(
literal|"discard_punctuation"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
DECL|method|getUserDictionary
specifier|public
specifier|static
name|UserDictionary
name|getUserDictionary
parameter_list|(
name|Environment
name|env
parameter_list|,
name|Settings
name|settings
parameter_list|)
block|{
try|try
block|{
specifier|final
name|Reader
name|reader
init|=
name|Analysis
operator|.
name|getReaderFromFile
argument_list|(
name|env
argument_list|,
name|settings
argument_list|,
name|USER_DICT_OPTION
argument_list|)
decl_stmt|;
if|if
condition|(
name|reader
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
else|else
block|{
try|try
block|{
return|return
name|UserDictionary
operator|.
name|open
argument_list|(
name|reader
argument_list|)
return|;
block|}
finally|finally
block|{
name|reader
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticsearchException
argument_list|(
literal|"failed to load kuromoji user dictionary"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
DECL|method|getMode
specifier|public
specifier|static
name|JapaneseTokenizer
operator|.
name|Mode
name|getMode
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|JapaneseTokenizer
operator|.
name|Mode
name|mode
init|=
name|JapaneseTokenizer
operator|.
name|DEFAULT_MODE
decl_stmt|;
name|String
name|modeSetting
init|=
name|settings
operator|.
name|get
argument_list|(
literal|"mode"
argument_list|,
literal|null
argument_list|)
decl_stmt|;
if|if
condition|(
name|modeSetting
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
literal|"search"
operator|.
name|equalsIgnoreCase
argument_list|(
name|modeSetting
argument_list|)
condition|)
block|{
name|mode
operator|=
name|JapaneseTokenizer
operator|.
name|Mode
operator|.
name|SEARCH
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"normal"
operator|.
name|equalsIgnoreCase
argument_list|(
name|modeSetting
argument_list|)
condition|)
block|{
name|mode
operator|=
name|JapaneseTokenizer
operator|.
name|Mode
operator|.
name|NORMAL
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"extended"
operator|.
name|equalsIgnoreCase
argument_list|(
name|modeSetting
argument_list|)
condition|)
block|{
name|mode
operator|=
name|JapaneseTokenizer
operator|.
name|Mode
operator|.
name|EXTENDED
expr_stmt|;
block|}
block|}
return|return
name|mode
return|;
block|}
annotation|@
name|Override
DECL|method|create
specifier|public
name|Tokenizer
name|create
parameter_list|()
block|{
return|return
operator|new
name|JapaneseTokenizer
argument_list|(
name|userDictionary
argument_list|,
name|discartPunctuation
argument_list|,
name|mode
argument_list|)
return|;
block|}
block|}
end_class

end_unit

