begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.similarity
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|similarity
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
name|search
operator|.
name|similarities
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
name|ElasticsearchIllegalArgumentException
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

begin_comment
comment|/**  * Abstract implementation of {@link SimilarityProvider} providing common behaviour  */
end_comment

begin_class
DECL|class|AbstractSimilarityProvider
specifier|public
specifier|abstract
class|class
name|AbstractSimilarityProvider
implements|implements
name|SimilarityProvider
block|{
DECL|field|NO_NORMALIZATION
specifier|protected
specifier|static
specifier|final
name|Normalization
name|NO_NORMALIZATION
init|=
operator|new
name|Normalization
operator|.
name|NoNormalization
argument_list|()
decl_stmt|;
DECL|field|name
specifier|private
specifier|final
name|String
name|name
decl_stmt|;
comment|/**      * Creates a new AbstractSimilarityProvider with the given name      *      * @param name Name of the Provider      */
DECL|method|AbstractSimilarityProvider
specifier|protected
name|AbstractSimilarityProvider
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
block|}
comment|/**      * {@inheritDoc}      */
annotation|@
name|Override
DECL|method|name
specifier|public
name|String
name|name
parameter_list|()
block|{
return|return
name|this
operator|.
name|name
return|;
block|}
comment|/**      * Parses the given Settings and creates the appropriate {@link Normalization}      *      * @param settings Settings to parse      * @return {@link Normalization} referred to in the Settings      */
DECL|method|parseNormalization
specifier|protected
name|Normalization
name|parseNormalization
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|String
name|normalization
init|=
name|settings
operator|.
name|get
argument_list|(
literal|"normalization"
argument_list|)
decl_stmt|;
if|if
condition|(
literal|"no"
operator|.
name|equals
argument_list|(
name|normalization
argument_list|)
condition|)
block|{
return|return
name|NO_NORMALIZATION
return|;
block|}
elseif|else
if|if
condition|(
literal|"h1"
operator|.
name|equals
argument_list|(
name|normalization
argument_list|)
condition|)
block|{
name|float
name|c
init|=
name|settings
operator|.
name|getAsFloat
argument_list|(
literal|"normalization.h1.c"
argument_list|,
literal|1f
argument_list|)
decl_stmt|;
return|return
operator|new
name|NormalizationH1
argument_list|(
name|c
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
literal|"h2"
operator|.
name|equals
argument_list|(
name|normalization
argument_list|)
condition|)
block|{
name|float
name|c
init|=
name|settings
operator|.
name|getAsFloat
argument_list|(
literal|"normalization.h2.c"
argument_list|,
literal|1f
argument_list|)
decl_stmt|;
return|return
operator|new
name|NormalizationH2
argument_list|(
name|c
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
literal|"h3"
operator|.
name|equals
argument_list|(
name|normalization
argument_list|)
condition|)
block|{
name|float
name|c
init|=
name|settings
operator|.
name|getAsFloat
argument_list|(
literal|"normalization.h3.c"
argument_list|,
literal|800f
argument_list|)
decl_stmt|;
return|return
operator|new
name|NormalizationH3
argument_list|(
name|c
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
literal|"z"
operator|.
name|equals
argument_list|(
name|normalization
argument_list|)
condition|)
block|{
name|float
name|z
init|=
name|settings
operator|.
name|getAsFloat
argument_list|(
literal|"normalization.z.z"
argument_list|,
literal|0.30f
argument_list|)
decl_stmt|;
return|return
operator|new
name|NormalizationZ
argument_list|(
name|z
argument_list|)
return|;
block|}
else|else
block|{
throw|throw
operator|new
name|ElasticsearchIllegalArgumentException
argument_list|(
literal|"Unsupported Normalization ["
operator|+
name|normalization
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

