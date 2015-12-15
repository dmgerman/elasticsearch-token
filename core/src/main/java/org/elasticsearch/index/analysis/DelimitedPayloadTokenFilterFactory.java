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
name|payloads
operator|.
name|*
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
DECL|class|DelimitedPayloadTokenFilterFactory
specifier|public
class|class
name|DelimitedPayloadTokenFilterFactory
extends|extends
name|AbstractTokenFilterFactory
block|{
DECL|field|DEFAULT_DELIMITER
specifier|public
specifier|static
specifier|final
name|char
name|DEFAULT_DELIMITER
init|=
literal|'|'
decl_stmt|;
DECL|field|DEFAULT_ENCODER
specifier|public
specifier|static
specifier|final
name|PayloadEncoder
name|DEFAULT_ENCODER
init|=
operator|new
name|FloatEncoder
argument_list|()
decl_stmt|;
DECL|field|ENCODING
specifier|static
specifier|final
name|String
name|ENCODING
init|=
literal|"encoding"
decl_stmt|;
DECL|field|DELIMITER
specifier|static
specifier|final
name|String
name|DELIMITER
init|=
literal|"delimiter"
decl_stmt|;
DECL|field|delimiter
name|char
name|delimiter
decl_stmt|;
DECL|field|encoder
name|PayloadEncoder
name|encoder
decl_stmt|;
DECL|method|DelimitedPayloadTokenFilterFactory
specifier|public
name|DelimitedPayloadTokenFilterFactory
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
name|String
name|delimiterConf
init|=
name|settings
operator|.
name|get
argument_list|(
name|DELIMITER
argument_list|)
decl_stmt|;
if|if
condition|(
name|delimiterConf
operator|!=
literal|null
condition|)
block|{
name|delimiter
operator|=
name|delimiterConf
operator|.
name|charAt
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|delimiter
operator|=
name|DEFAULT_DELIMITER
expr_stmt|;
block|}
if|if
condition|(
name|settings
operator|.
name|get
argument_list|(
name|ENCODING
argument_list|)
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|settings
operator|.
name|get
argument_list|(
name|ENCODING
argument_list|)
operator|.
name|equals
argument_list|(
literal|"float"
argument_list|)
condition|)
block|{
name|encoder
operator|=
operator|new
name|FloatEncoder
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|settings
operator|.
name|get
argument_list|(
name|ENCODING
argument_list|)
operator|.
name|equals
argument_list|(
literal|"int"
argument_list|)
condition|)
block|{
name|encoder
operator|=
operator|new
name|IntegerEncoder
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|settings
operator|.
name|get
argument_list|(
name|ENCODING
argument_list|)
operator|.
name|equals
argument_list|(
literal|"identity"
argument_list|)
condition|)
block|{
name|encoder
operator|=
operator|new
name|IdentityEncoder
argument_list|()
expr_stmt|;
block|}
block|}
else|else
block|{
name|encoder
operator|=
name|DEFAULT_ENCODER
expr_stmt|;
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
name|DelimitedPayloadTokenFilter
name|filter
init|=
operator|new
name|DelimitedPayloadTokenFilter
argument_list|(
name|tokenStream
argument_list|,
name|delimiter
argument_list|,
name|encoder
argument_list|)
decl_stmt|;
return|return
name|filter
return|;
block|}
block|}
end_class

end_unit

