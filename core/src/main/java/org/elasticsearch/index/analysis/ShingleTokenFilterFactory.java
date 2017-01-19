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
name|shingle
operator|.
name|ShingleFilter
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

begin_class
DECL|class|ShingleTokenFilterFactory
specifier|public
class|class
name|ShingleTokenFilterFactory
extends|extends
name|AbstractTokenFilterFactory
block|{
DECL|field|factory
specifier|private
specifier|final
name|Factory
name|factory
decl_stmt|;
DECL|method|ShingleTokenFilterFactory
specifier|public
name|ShingleTokenFilterFactory
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
name|Integer
name|maxShingleSize
init|=
name|settings
operator|.
name|getAsInt
argument_list|(
literal|"max_shingle_size"
argument_list|,
name|ShingleFilter
operator|.
name|DEFAULT_MAX_SHINGLE_SIZE
argument_list|)
decl_stmt|;
name|Integer
name|minShingleSize
init|=
name|settings
operator|.
name|getAsInt
argument_list|(
literal|"min_shingle_size"
argument_list|,
name|ShingleFilter
operator|.
name|DEFAULT_MIN_SHINGLE_SIZE
argument_list|)
decl_stmt|;
name|Boolean
name|outputUnigrams
init|=
name|settings
operator|.
name|getAsBooleanLenientForPreEs6Indices
argument_list|(
name|indexSettings
operator|.
name|getIndexVersionCreated
argument_list|()
argument_list|,
literal|"output_unigrams"
argument_list|,
literal|true
argument_list|,
name|deprecationLogger
argument_list|)
decl_stmt|;
name|Boolean
name|outputUnigramsIfNoShingles
init|=
name|settings
operator|.
name|getAsBooleanLenientForPreEs6Indices
argument_list|(
name|indexSettings
operator|.
name|getIndexVersionCreated
argument_list|()
argument_list|,
literal|"output_unigrams_if_no_shingles"
argument_list|,
literal|false
argument_list|,
name|deprecationLogger
argument_list|)
decl_stmt|;
name|String
name|tokenSeparator
init|=
name|settings
operator|.
name|get
argument_list|(
literal|"token_separator"
argument_list|,
name|ShingleFilter
operator|.
name|DEFAULT_TOKEN_SEPARATOR
argument_list|)
decl_stmt|;
name|String
name|fillerToken
init|=
name|settings
operator|.
name|get
argument_list|(
literal|"filler_token"
argument_list|,
name|ShingleFilter
operator|.
name|DEFAULT_FILLER_TOKEN
argument_list|)
decl_stmt|;
name|factory
operator|=
operator|new
name|Factory
argument_list|(
literal|"shingle"
argument_list|,
name|minShingleSize
argument_list|,
name|maxShingleSize
argument_list|,
name|outputUnigrams
argument_list|,
name|outputUnigramsIfNoShingles
argument_list|,
name|tokenSeparator
argument_list|,
name|fillerToken
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
return|return
name|factory
operator|.
name|create
argument_list|(
name|tokenStream
argument_list|)
return|;
block|}
DECL|method|getInnerFactory
specifier|public
name|Factory
name|getInnerFactory
parameter_list|()
block|{
return|return
name|this
operator|.
name|factory
return|;
block|}
DECL|class|Factory
specifier|public
specifier|static
specifier|final
class|class
name|Factory
implements|implements
name|TokenFilterFactory
block|{
DECL|field|maxShingleSize
specifier|private
specifier|final
name|int
name|maxShingleSize
decl_stmt|;
DECL|field|outputUnigrams
specifier|private
specifier|final
name|boolean
name|outputUnigrams
decl_stmt|;
DECL|field|outputUnigramsIfNoShingles
specifier|private
specifier|final
name|boolean
name|outputUnigramsIfNoShingles
decl_stmt|;
DECL|field|tokenSeparator
specifier|private
specifier|final
name|String
name|tokenSeparator
decl_stmt|;
DECL|field|fillerToken
specifier|private
specifier|final
name|String
name|fillerToken
decl_stmt|;
DECL|field|minShingleSize
specifier|private
name|int
name|minShingleSize
decl_stmt|;
DECL|field|name
specifier|private
specifier|final
name|String
name|name
decl_stmt|;
DECL|method|Factory
specifier|public
name|Factory
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|this
argument_list|(
name|name
argument_list|,
name|ShingleFilter
operator|.
name|DEFAULT_MIN_SHINGLE_SIZE
argument_list|,
name|ShingleFilter
operator|.
name|DEFAULT_MAX_SHINGLE_SIZE
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|,
name|ShingleFilter
operator|.
name|DEFAULT_TOKEN_SEPARATOR
argument_list|,
name|ShingleFilter
operator|.
name|DEFAULT_FILLER_TOKEN
argument_list|)
expr_stmt|;
block|}
DECL|method|Factory
name|Factory
parameter_list|(
name|String
name|name
parameter_list|,
name|int
name|minShingleSize
parameter_list|,
name|int
name|maxShingleSize
parameter_list|,
name|boolean
name|outputUnigrams
parameter_list|,
name|boolean
name|outputUnigramsIfNoShingles
parameter_list|,
name|String
name|tokenSeparator
parameter_list|,
name|String
name|fillerToken
parameter_list|)
block|{
name|this
operator|.
name|maxShingleSize
operator|=
name|maxShingleSize
expr_stmt|;
name|this
operator|.
name|outputUnigrams
operator|=
name|outputUnigrams
expr_stmt|;
name|this
operator|.
name|outputUnigramsIfNoShingles
operator|=
name|outputUnigramsIfNoShingles
expr_stmt|;
name|this
operator|.
name|tokenSeparator
operator|=
name|tokenSeparator
expr_stmt|;
name|this
operator|.
name|minShingleSize
operator|=
name|minShingleSize
expr_stmt|;
name|this
operator|.
name|fillerToken
operator|=
name|fillerToken
expr_stmt|;
name|this
operator|.
name|name
operator|=
name|name
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
name|ShingleFilter
name|filter
init|=
operator|new
name|ShingleFilter
argument_list|(
name|tokenStream
argument_list|,
name|minShingleSize
argument_list|,
name|maxShingleSize
argument_list|)
decl_stmt|;
name|filter
operator|.
name|setOutputUnigrams
argument_list|(
name|outputUnigrams
argument_list|)
expr_stmt|;
name|filter
operator|.
name|setOutputUnigramsIfNoShingles
argument_list|(
name|outputUnigramsIfNoShingles
argument_list|)
expr_stmt|;
name|filter
operator|.
name|setTokenSeparator
argument_list|(
name|tokenSeparator
argument_list|)
expr_stmt|;
name|filter
operator|.
name|setFillerToken
argument_list|(
name|fillerToken
argument_list|)
expr_stmt|;
return|return
name|filter
return|;
block|}
DECL|method|getMaxShingleSize
specifier|public
name|int
name|getMaxShingleSize
parameter_list|()
block|{
return|return
name|maxShingleSize
return|;
block|}
DECL|method|getMinShingleSize
specifier|public
name|int
name|getMinShingleSize
parameter_list|()
block|{
return|return
name|minShingleSize
return|;
block|}
DECL|method|getOutputUnigrams
specifier|public
name|boolean
name|getOutputUnigrams
parameter_list|()
block|{
return|return
name|outputUnigrams
return|;
block|}
DECL|method|getOutputUnigramsIfNoShingles
specifier|public
name|boolean
name|getOutputUnigramsIfNoShingles
parameter_list|()
block|{
return|return
name|outputUnigramsIfNoShingles
return|;
block|}
annotation|@
name|Override
DECL|method|name
specifier|public
name|String
name|name
parameter_list|()
block|{
return|return
name|name
return|;
block|}
block|}
block|}
end_class

end_unit

