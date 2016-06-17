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
name|PerFieldSimilarityWrapper
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
name|index
operator|.
name|AbstractIndexComponent
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
name|IndexModule
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

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
operator|.
name|MappedFieldType
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
name|mapper
operator|.
name|MapperService
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
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
import|import
name|java
operator|.
name|util
operator|.
name|function
operator|.
name|BiFunction
import|;
end_import

begin_class
DECL|class|SimilarityService
specifier|public
specifier|final
class|class
name|SimilarityService
extends|extends
name|AbstractIndexComponent
block|{
DECL|field|DEFAULT_SIMILARITY
specifier|public
specifier|final
specifier|static
name|String
name|DEFAULT_SIMILARITY
init|=
literal|"BM25"
decl_stmt|;
DECL|field|defaultSimilarity
specifier|private
specifier|final
name|Similarity
name|defaultSimilarity
decl_stmt|;
DECL|field|baseSimilarity
specifier|private
specifier|final
name|Similarity
name|baseSimilarity
decl_stmt|;
DECL|field|similarities
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|SimilarityProvider
argument_list|>
name|similarities
decl_stmt|;
DECL|field|DEFAULTS
specifier|static
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|BiFunction
argument_list|<
name|String
argument_list|,
name|Settings
argument_list|,
name|SimilarityProvider
argument_list|>
argument_list|>
name|DEFAULTS
decl_stmt|;
DECL|field|BUILT_IN
specifier|public
specifier|static
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|BiFunction
argument_list|<
name|String
argument_list|,
name|Settings
argument_list|,
name|SimilarityProvider
argument_list|>
argument_list|>
name|BUILT_IN
decl_stmt|;
static|static
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|BiFunction
argument_list|<
name|String
argument_list|,
name|Settings
argument_list|,
name|SimilarityProvider
argument_list|>
argument_list|>
name|defaults
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|BiFunction
argument_list|<
name|String
argument_list|,
name|Settings
argument_list|,
name|SimilarityProvider
argument_list|>
argument_list|>
name|buildIn
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|defaults
operator|.
name|put
argument_list|(
literal|"classic"
argument_list|,
name|ClassicSimilarityProvider
operator|::
operator|new
argument_list|)
expr_stmt|;
name|defaults
operator|.
name|put
argument_list|(
literal|"BM25"
argument_list|,
name|BM25SimilarityProvider
operator|::
operator|new
argument_list|)
expr_stmt|;
name|buildIn
operator|.
name|put
argument_list|(
literal|"classic"
argument_list|,
name|ClassicSimilarityProvider
operator|::
operator|new
argument_list|)
expr_stmt|;
name|buildIn
operator|.
name|put
argument_list|(
literal|"BM25"
argument_list|,
name|BM25SimilarityProvider
operator|::
operator|new
argument_list|)
expr_stmt|;
name|buildIn
operator|.
name|put
argument_list|(
literal|"DFR"
argument_list|,
name|DFRSimilarityProvider
operator|::
operator|new
argument_list|)
expr_stmt|;
name|buildIn
operator|.
name|put
argument_list|(
literal|"IB"
argument_list|,
name|IBSimilarityProvider
operator|::
operator|new
argument_list|)
expr_stmt|;
name|buildIn
operator|.
name|put
argument_list|(
literal|"LMDirichlet"
argument_list|,
name|LMDirichletSimilarityProvider
operator|::
operator|new
argument_list|)
expr_stmt|;
name|buildIn
operator|.
name|put
argument_list|(
literal|"LMJelinekMercer"
argument_list|,
name|LMJelinekMercerSimilarityProvider
operator|::
operator|new
argument_list|)
expr_stmt|;
name|buildIn
operator|.
name|put
argument_list|(
literal|"DFI"
argument_list|,
name|DFISimilarityProvider
operator|::
operator|new
argument_list|)
expr_stmt|;
name|DEFAULTS
operator|=
name|Collections
operator|.
name|unmodifiableMap
argument_list|(
name|defaults
argument_list|)
expr_stmt|;
name|BUILT_IN
operator|=
name|Collections
operator|.
name|unmodifiableMap
argument_list|(
name|buildIn
argument_list|)
expr_stmt|;
block|}
DECL|method|SimilarityService
specifier|public
name|SimilarityService
parameter_list|(
name|IndexSettings
name|indexSettings
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|BiFunction
argument_list|<
name|String
argument_list|,
name|Settings
argument_list|,
name|SimilarityProvider
argument_list|>
argument_list|>
name|similarities
parameter_list|)
block|{
name|super
argument_list|(
name|indexSettings
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|SimilarityProvider
argument_list|>
name|providers
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|(
name|similarities
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Settings
argument_list|>
name|similaritySettings
init|=
name|this
operator|.
name|indexSettings
operator|.
name|getSettings
argument_list|()
operator|.
name|getGroups
argument_list|(
name|IndexModule
operator|.
name|SIMILARITY_SETTINGS_PREFIX
argument_list|)
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Settings
argument_list|>
name|entry
range|:
name|similaritySettings
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|name
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
comment|// Starting with v5.0 indices, it should no longer be possible to redefine built-in similarities
if|if
condition|(
name|BUILT_IN
operator|.
name|containsKey
argument_list|(
name|name
argument_list|)
operator|&&
name|indexSettings
operator|.
name|getIndexVersionCreated
argument_list|()
operator|.
name|onOrAfter
argument_list|(
name|Version
operator|.
name|V_5_0_0_alpha1
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Cannot redefine built-in Similarity ["
operator|+
name|name
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|Settings
name|settings
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|String
name|typeName
init|=
name|settings
operator|.
name|get
argument_list|(
literal|"type"
argument_list|)
decl_stmt|;
if|if
condition|(
name|typeName
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Similarity ["
operator|+
name|name
operator|+
literal|"] must have an associated type"
argument_list|)
throw|;
block|}
elseif|else
if|if
condition|(
operator|(
name|similarities
operator|.
name|containsKey
argument_list|(
name|typeName
argument_list|)
operator|||
name|BUILT_IN
operator|.
name|containsKey
argument_list|(
name|typeName
argument_list|)
operator|)
operator|==
literal|false
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Unknown Similarity type ["
operator|+
name|typeName
operator|+
literal|"] for ["
operator|+
name|name
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|BiFunction
argument_list|<
name|String
argument_list|,
name|Settings
argument_list|,
name|SimilarityProvider
argument_list|>
name|factory
init|=
name|similarities
operator|.
name|getOrDefault
argument_list|(
name|typeName
argument_list|,
name|BUILT_IN
operator|.
name|get
argument_list|(
name|typeName
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|settings
operator|==
literal|null
condition|)
block|{
name|settings
operator|=
name|Settings
operator|.
name|Builder
operator|.
name|EMPTY_SETTINGS
expr_stmt|;
block|}
name|providers
operator|.
name|put
argument_list|(
name|name
argument_list|,
name|factory
operator|.
name|apply
argument_list|(
name|name
argument_list|,
name|settings
argument_list|)
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|SimilarityProvider
argument_list|>
name|entry
range|:
name|addSimilarities
argument_list|(
name|similaritySettings
argument_list|,
name|DEFAULTS
argument_list|)
operator|.
name|entrySet
argument_list|()
control|)
block|{
comment|// Avoid overwriting custom providers for indices older that v5.0
if|if
condition|(
name|providers
operator|.
name|containsKey
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
operator|&&
name|indexSettings
operator|.
name|getIndexVersionCreated
argument_list|()
operator|.
name|before
argument_list|(
name|Version
operator|.
name|V_5_0_0_alpha1
argument_list|)
condition|)
block|{
continue|continue;
block|}
name|providers
operator|.
name|put
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|similarities
operator|=
name|providers
expr_stmt|;
name|defaultSimilarity
operator|=
operator|(
name|providers
operator|.
name|get
argument_list|(
literal|"default"
argument_list|)
operator|!=
literal|null
operator|)
condition|?
name|providers
operator|.
name|get
argument_list|(
literal|"default"
argument_list|)
operator|.
name|get
argument_list|()
else|:
name|providers
operator|.
name|get
argument_list|(
name|SimilarityService
operator|.
name|DEFAULT_SIMILARITY
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
comment|// Expert users can configure the base type as being different to default, but out-of-box we use default.
name|baseSimilarity
operator|=
operator|(
name|providers
operator|.
name|get
argument_list|(
literal|"base"
argument_list|)
operator|!=
literal|null
operator|)
condition|?
name|providers
operator|.
name|get
argument_list|(
literal|"base"
argument_list|)
operator|.
name|get
argument_list|()
else|:
name|defaultSimilarity
expr_stmt|;
block|}
DECL|method|similarity
specifier|public
name|Similarity
name|similarity
parameter_list|(
name|MapperService
name|mapperService
parameter_list|)
block|{
comment|// TODO we can maybe factor out MapperService here entirely by introducing an interface for the lookup?
return|return
operator|(
name|mapperService
operator|!=
literal|null
operator|)
condition|?
operator|new
name|PerFieldSimilarity
argument_list|(
name|defaultSimilarity
argument_list|,
name|baseSimilarity
argument_list|,
name|mapperService
argument_list|)
else|:
name|defaultSimilarity
return|;
block|}
DECL|method|addSimilarities
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|SimilarityProvider
argument_list|>
name|addSimilarities
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Settings
argument_list|>
name|similaritySettings
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|BiFunction
argument_list|<
name|String
argument_list|,
name|Settings
argument_list|,
name|SimilarityProvider
argument_list|>
argument_list|>
name|similarities
parameter_list|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|SimilarityProvider
argument_list|>
name|providers
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|(
name|similarities
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|BiFunction
argument_list|<
name|String
argument_list|,
name|Settings
argument_list|,
name|SimilarityProvider
argument_list|>
argument_list|>
name|entry
range|:
name|similarities
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|name
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|BiFunction
argument_list|<
name|String
argument_list|,
name|Settings
argument_list|,
name|SimilarityProvider
argument_list|>
name|factory
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|Settings
name|settings
init|=
name|similaritySettings
operator|.
name|get
argument_list|(
name|name
argument_list|)
decl_stmt|;
if|if
condition|(
name|settings
operator|==
literal|null
condition|)
block|{
name|settings
operator|=
name|Settings
operator|.
name|Builder
operator|.
name|EMPTY_SETTINGS
expr_stmt|;
block|}
name|providers
operator|.
name|put
argument_list|(
name|name
argument_list|,
name|factory
operator|.
name|apply
argument_list|(
name|name
argument_list|,
name|settings
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|providers
return|;
block|}
DECL|method|getSimilarity
specifier|public
name|SimilarityProvider
name|getSimilarity
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
name|similarities
operator|.
name|get
argument_list|(
name|name
argument_list|)
return|;
block|}
DECL|method|getDefaultSimilarity
name|Similarity
name|getDefaultSimilarity
parameter_list|()
block|{
return|return
name|defaultSimilarity
return|;
block|}
DECL|class|PerFieldSimilarity
specifier|static
class|class
name|PerFieldSimilarity
extends|extends
name|PerFieldSimilarityWrapper
block|{
DECL|field|defaultSimilarity
specifier|private
specifier|final
name|Similarity
name|defaultSimilarity
decl_stmt|;
DECL|field|baseSimilarity
specifier|private
specifier|final
name|Similarity
name|baseSimilarity
decl_stmt|;
DECL|field|mapperService
specifier|private
specifier|final
name|MapperService
name|mapperService
decl_stmt|;
DECL|method|PerFieldSimilarity
name|PerFieldSimilarity
parameter_list|(
name|Similarity
name|defaultSimilarity
parameter_list|,
name|Similarity
name|baseSimilarity
parameter_list|,
name|MapperService
name|mapperService
parameter_list|)
block|{
name|this
operator|.
name|defaultSimilarity
operator|=
name|defaultSimilarity
expr_stmt|;
name|this
operator|.
name|baseSimilarity
operator|=
name|baseSimilarity
expr_stmt|;
name|this
operator|.
name|mapperService
operator|=
name|mapperService
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|coord
specifier|public
name|float
name|coord
parameter_list|(
name|int
name|overlap
parameter_list|,
name|int
name|maxOverlap
parameter_list|)
block|{
return|return
name|baseSimilarity
operator|.
name|coord
argument_list|(
name|overlap
argument_list|,
name|maxOverlap
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|queryNorm
specifier|public
name|float
name|queryNorm
parameter_list|(
name|float
name|valueForNormalization
parameter_list|)
block|{
return|return
name|baseSimilarity
operator|.
name|queryNorm
argument_list|(
name|valueForNormalization
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|get
specifier|public
name|Similarity
name|get
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|MappedFieldType
name|fieldType
init|=
name|mapperService
operator|.
name|fullName
argument_list|(
name|name
argument_list|)
decl_stmt|;
return|return
operator|(
name|fieldType
operator|!=
literal|null
operator|&&
name|fieldType
operator|.
name|similarity
argument_list|()
operator|!=
literal|null
operator|)
condition|?
name|fieldType
operator|.
name|similarity
argument_list|()
operator|.
name|get
argument_list|()
else|:
name|defaultSimilarity
return|;
block|}
block|}
block|}
end_class

end_unit

