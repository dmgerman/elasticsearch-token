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
name|reverse
operator|.
name|ReverseStringFilter
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
comment|/**  *  */
end_comment

begin_class
DECL|class|EdgeNGramTokenFilterFactory
specifier|public
class|class
name|EdgeNGramTokenFilterFactory
extends|extends
name|AbstractTokenFilterFactory
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
DECL|field|SIDE_FRONT
specifier|public
specifier|static
specifier|final
name|int
name|SIDE_FRONT
init|=
literal|1
decl_stmt|;
DECL|field|SIDE_BACK
specifier|public
specifier|static
specifier|final
name|int
name|SIDE_BACK
init|=
literal|2
decl_stmt|;
DECL|field|side
specifier|private
specifier|final
name|int
name|side
decl_stmt|;
DECL|method|EdgeNGramTokenFilterFactory
specifier|public
name|EdgeNGramTokenFilterFactory
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
name|minGram
operator|=
name|settings
operator|.
name|getAsInt
argument_list|(
literal|"min_gram"
argument_list|,
name|NGramTokenFilter
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
name|NGramTokenFilter
operator|.
name|DEFAULT_MAX_NGRAM_SIZE
argument_list|)
expr_stmt|;
name|this
operator|.
name|side
operator|=
name|parseSide
argument_list|(
name|settings
operator|.
name|get
argument_list|(
literal|"side"
argument_list|,
literal|"front"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|parseSide
specifier|static
name|int
name|parseSide
parameter_list|(
name|String
name|side
parameter_list|)
block|{
switch|switch
condition|(
name|side
condition|)
block|{
case|case
literal|"front"
case|:
return|return
name|SIDE_FRONT
return|;
case|case
literal|"back"
case|:
return|return
name|SIDE_BACK
return|;
default|default:
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"invalid side: "
operator|+
name|side
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
name|TokenStream
name|result
init|=
name|tokenStream
decl_stmt|;
comment|// side=BACK is not supported anymore but applying ReverseStringFilter up-front and after the token filter has the same effect
if|if
condition|(
name|side
operator|==
name|SIDE_BACK
condition|)
block|{
name|result
operator|=
operator|new
name|ReverseStringFilter
argument_list|(
name|result
argument_list|)
expr_stmt|;
block|}
name|result
operator|=
operator|new
name|EdgeNGramTokenFilter
argument_list|(
name|result
argument_list|,
name|minGram
argument_list|,
name|maxGram
argument_list|)
expr_stmt|;
comment|// side=BACK is not supported anymore but applying ReverseStringFilter up-front and after the token filter has the same effect
if|if
condition|(
name|side
operator|==
name|SIDE_BACK
condition|)
block|{
name|result
operator|=
operator|new
name|ReverseStringFilter
argument_list|(
name|result
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
block|}
end_class

end_unit

