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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableMap
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
name|collect
operator|.
name|MapBuilder
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
name|util
operator|.
name|Map
import|;
end_import

begin_comment
comment|/**  * Service for looking up configured {@link SimilarityProvider} implementations by name.  *<p>  * The service instantiates the Providers through their Factories using configuration  * values found with the {@link SimilarityModule#SIMILARITY_SETTINGS_PREFIX} prefix.  */
end_comment

begin_class
DECL|class|SimilarityLookupService
specifier|public
class|class
name|SimilarityLookupService
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
literal|"default"
decl_stmt|;
DECL|field|similarities
specifier|private
specifier|final
name|ImmutableMap
argument_list|<
name|String
argument_list|,
name|SimilarityProvider
argument_list|>
name|similarities
decl_stmt|;
DECL|method|SimilarityLookupService
specifier|public
name|SimilarityLookupService
parameter_list|(
name|Index
name|index
parameter_list|,
name|Settings
name|indexSettings
parameter_list|)
block|{
name|this
argument_list|(
name|index
argument_list|,
name|indexSettings
argument_list|,
name|ImmutableMap
operator|.
expr|<
name|String
argument_list|,
name|SimilarityProvider
operator|.
name|Factory
operator|>
name|of
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Inject
DECL|method|SimilarityLookupService
specifier|public
name|SimilarityLookupService
parameter_list|(
name|Index
name|index
parameter_list|,
annotation|@
name|IndexSettings
name|Settings
name|indexSettings
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|SimilarityProvider
operator|.
name|Factory
argument_list|>
name|similarities
parameter_list|)
block|{
name|super
argument_list|(
name|index
argument_list|,
name|indexSettings
argument_list|)
expr_stmt|;
name|MapBuilder
argument_list|<
name|String
argument_list|,
name|SimilarityProvider
argument_list|>
name|providers
init|=
name|MapBuilder
operator|.
name|newMapBuilder
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Settings
argument_list|>
name|similaritySettings
init|=
name|indexSettings
operator|.
name|getGroups
argument_list|(
name|SimilarityModule
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
name|SimilarityProvider
operator|.
name|Factory
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
name|SimilarityProvider
operator|.
name|Factory
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
name|create
argument_list|(
name|name
argument_list|,
name|settings
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// For testing
for|for
control|(
name|PreBuiltSimilarityProvider
operator|.
name|Factory
name|factory
range|:
name|Similarities
operator|.
name|listFactories
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|providers
operator|.
name|containsKey
argument_list|(
name|factory
operator|.
name|name
argument_list|()
argument_list|)
condition|)
block|{
name|providers
operator|.
name|put
argument_list|(
name|factory
operator|.
name|name
argument_list|()
argument_list|,
name|factory
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|this
operator|.
name|similarities
operator|=
name|providers
operator|.
name|immutableMap
argument_list|()
expr_stmt|;
block|}
comment|/**      * Returns the {@link SimilarityProvider} with the given name      *      * @param name Name of the SimilarityProvider to find      * @return {@link SimilarityProvider} with the given name, or {@code null} if no Provider exists      */
DECL|method|similarity
specifier|public
name|SimilarityProvider
name|similarity
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
block|}
end_class

end_unit

