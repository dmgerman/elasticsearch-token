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
name|AfterEffect
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
name|search
operator|.
name|similarities
operator|.
name|AfterEffectB
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
name|search
operator|.
name|similarities
operator|.
name|AfterEffectL
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
name|search
operator|.
name|similarities
operator|.
name|BasicModel
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
name|search
operator|.
name|similarities
operator|.
name|BasicModelBE
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
name|search
operator|.
name|similarities
operator|.
name|BasicModelD
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
name|search
operator|.
name|similarities
operator|.
name|BasicModelG
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
name|search
operator|.
name|similarities
operator|.
name|BasicModelIF
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
name|search
operator|.
name|similarities
operator|.
name|BasicModelIn
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
name|search
operator|.
name|similarities
operator|.
name|BasicModelIne
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
name|search
operator|.
name|similarities
operator|.
name|BasicModelP
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
name|search
operator|.
name|similarities
operator|.
name|DFRSimilarity
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
name|search
operator|.
name|similarities
operator|.
name|Normalization
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
name|search
operator|.
name|similarities
operator|.
name|Similarity
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
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|util
operator|.
name|Collections
operator|.
name|unmodifiableMap
import|;
end_import

begin_comment
comment|/**  * {@link SimilarityProvider} for {@link DFRSimilarity}.  *<p>  * Configuration options available:  *<ul>  *<li>basic_model</li>  *<li>after_effect</li>  *<li>normalization</li>  *</ul>  * @see DFRSimilarity For more information about configuration  */
end_comment

begin_class
DECL|class|DFRSimilarityProvider
specifier|public
class|class
name|DFRSimilarityProvider
extends|extends
name|AbstractSimilarityProvider
block|{
DECL|field|BASIC_MODELS
specifier|private
specifier|static
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|BasicModel
argument_list|>
name|BASIC_MODELS
decl_stmt|;
DECL|field|AFTER_EFFECTS
specifier|private
specifier|static
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|AfterEffect
argument_list|>
name|AFTER_EFFECTS
decl_stmt|;
static|static
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|BasicModel
argument_list|>
name|models
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|models
operator|.
name|put
argument_list|(
literal|"be"
argument_list|,
operator|new
name|BasicModelBE
argument_list|()
argument_list|)
expr_stmt|;
name|models
operator|.
name|put
argument_list|(
literal|"d"
argument_list|,
operator|new
name|BasicModelD
argument_list|()
argument_list|)
expr_stmt|;
name|models
operator|.
name|put
argument_list|(
literal|"g"
argument_list|,
operator|new
name|BasicModelG
argument_list|()
argument_list|)
expr_stmt|;
name|models
operator|.
name|put
argument_list|(
literal|"if"
argument_list|,
operator|new
name|BasicModelIF
argument_list|()
argument_list|)
expr_stmt|;
name|models
operator|.
name|put
argument_list|(
literal|"in"
argument_list|,
operator|new
name|BasicModelIn
argument_list|()
argument_list|)
expr_stmt|;
name|models
operator|.
name|put
argument_list|(
literal|"ine"
argument_list|,
operator|new
name|BasicModelIne
argument_list|()
argument_list|)
expr_stmt|;
name|models
operator|.
name|put
argument_list|(
literal|"p"
argument_list|,
operator|new
name|BasicModelP
argument_list|()
argument_list|)
expr_stmt|;
name|BASIC_MODELS
operator|=
name|unmodifiableMap
argument_list|(
name|models
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|AfterEffect
argument_list|>
name|effects
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|effects
operator|.
name|put
argument_list|(
literal|"no"
argument_list|,
operator|new
name|AfterEffect
operator|.
name|NoAfterEffect
argument_list|()
argument_list|)
expr_stmt|;
name|effects
operator|.
name|put
argument_list|(
literal|"b"
argument_list|,
operator|new
name|AfterEffectB
argument_list|()
argument_list|)
expr_stmt|;
name|effects
operator|.
name|put
argument_list|(
literal|"l"
argument_list|,
operator|new
name|AfterEffectL
argument_list|()
argument_list|)
expr_stmt|;
name|AFTER_EFFECTS
operator|=
name|unmodifiableMap
argument_list|(
name|effects
argument_list|)
expr_stmt|;
block|}
DECL|field|similarity
specifier|private
specifier|final
name|DFRSimilarity
name|similarity
decl_stmt|;
DECL|method|DFRSimilarityProvider
specifier|public
name|DFRSimilarityProvider
parameter_list|(
name|String
name|name
parameter_list|,
name|Settings
name|settings
parameter_list|,
name|Settings
name|indexSettings
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|BasicModel
name|basicModel
init|=
name|parseBasicModel
argument_list|(
name|settings
argument_list|)
decl_stmt|;
name|AfterEffect
name|afterEffect
init|=
name|parseAfterEffect
argument_list|(
name|settings
argument_list|)
decl_stmt|;
name|Normalization
name|normalization
init|=
name|parseNormalization
argument_list|(
name|settings
argument_list|)
decl_stmt|;
name|this
operator|.
name|similarity
operator|=
operator|new
name|DFRSimilarity
argument_list|(
name|basicModel
argument_list|,
name|afterEffect
argument_list|,
name|normalization
argument_list|)
expr_stmt|;
block|}
comment|/**      * Parses the given Settings and creates the appropriate {@link BasicModel}      *      * @param settings Settings to parse      * @return {@link BasicModel} referred to in the Settings      */
DECL|method|parseBasicModel
specifier|protected
name|BasicModel
name|parseBasicModel
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|String
name|basicModel
init|=
name|settings
operator|.
name|get
argument_list|(
literal|"basic_model"
argument_list|)
decl_stmt|;
name|BasicModel
name|model
init|=
name|BASIC_MODELS
operator|.
name|get
argument_list|(
name|basicModel
argument_list|)
decl_stmt|;
if|if
condition|(
name|model
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Unsupported BasicModel ["
operator|+
name|basicModel
operator|+
literal|"]"
argument_list|)
throw|;
block|}
return|return
name|model
return|;
block|}
comment|/**      * Parses the given Settings and creates the appropriate {@link AfterEffect}      *      * @param settings Settings to parse      * @return {@link AfterEffect} referred to in the Settings      */
DECL|method|parseAfterEffect
specifier|protected
name|AfterEffect
name|parseAfterEffect
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|String
name|afterEffect
init|=
name|settings
operator|.
name|get
argument_list|(
literal|"after_effect"
argument_list|)
decl_stmt|;
name|AfterEffect
name|effect
init|=
name|AFTER_EFFECTS
operator|.
name|get
argument_list|(
name|afterEffect
argument_list|)
decl_stmt|;
if|if
condition|(
name|effect
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Unsupported AfterEffect ["
operator|+
name|afterEffect
operator|+
literal|"]"
argument_list|)
throw|;
block|}
return|return
name|effect
return|;
block|}
comment|/**      * {@inheritDoc}      */
annotation|@
name|Override
DECL|method|get
specifier|public
name|Similarity
name|get
parameter_list|()
block|{
return|return
name|similarity
return|;
block|}
block|}
end_class

end_unit

