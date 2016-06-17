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
name|BM25Similarity
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
name|ClassicSimilarity
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
name|cluster
operator|.
name|metadata
operator|.
name|IndexMetaData
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
name|IndexSettings
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|ESTestCase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|IndexSettingsModule
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
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|instanceOf
import|;
end_import

begin_class
DECL|class|SimilarityServiceTests
specifier|public
class|class
name|SimilarityServiceTests
extends|extends
name|ESTestCase
block|{
DECL|method|testDefaultSimilarity
specifier|public
name|void
name|testDefaultSimilarity
parameter_list|()
block|{
name|Settings
name|settings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|build
argument_list|()
decl_stmt|;
name|IndexSettings
name|indexSettings
init|=
name|IndexSettingsModule
operator|.
name|newIndexSettings
argument_list|(
literal|"test"
argument_list|,
name|settings
argument_list|)
decl_stmt|;
name|SimilarityService
name|service
init|=
operator|new
name|SimilarityService
argument_list|(
name|indexSettings
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|service
operator|.
name|getDefaultSimilarity
argument_list|()
argument_list|,
name|instanceOf
argument_list|(
name|BM25Similarity
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// Tests #16594
DECL|method|testOverrideBuiltInSimilarity
specifier|public
name|void
name|testOverrideBuiltInSimilarity
parameter_list|()
block|{
name|Settings
name|settings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"index.similarity.BM25.type"
argument_list|,
literal|"classic"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|IndexSettings
name|indexSettings
init|=
name|IndexSettingsModule
operator|.
name|newIndexSettings
argument_list|(
literal|"test"
argument_list|,
name|settings
argument_list|)
decl_stmt|;
try|try
block|{
operator|new
name|SimilarityService
argument_list|(
name|indexSettings
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"can't override bm25"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|ex
parameter_list|)
block|{
name|assertEquals
argument_list|(
name|ex
operator|.
name|getMessage
argument_list|()
argument_list|,
literal|"Cannot redefine built-in Similarity [BM25]"
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Pre v3 indices could override built-in similarities
DECL|method|testOverrideBuiltInSimilarityPreV3
specifier|public
name|void
name|testOverrideBuiltInSimilarityPreV3
parameter_list|()
block|{
name|Settings
name|settings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_VERSION_CREATED
argument_list|,
name|Version
operator|.
name|V_2_0_0
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.similarity.BM25.type"
argument_list|,
literal|"classic"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|IndexSettings
name|indexSettings
init|=
name|IndexSettingsModule
operator|.
name|newIndexSettings
argument_list|(
literal|"test"
argument_list|,
name|settings
argument_list|)
decl_stmt|;
name|SimilarityService
name|service
init|=
operator|new
name|SimilarityService
argument_list|(
name|indexSettings
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|service
operator|.
name|getSimilarity
argument_list|(
literal|"BM25"
argument_list|)
operator|instanceof
name|ClassicSimilarityProvider
argument_list|)
expr_stmt|;
block|}
comment|// Tests #16594
DECL|method|testOverrideDefaultSimilarity
specifier|public
name|void
name|testOverrideDefaultSimilarity
parameter_list|()
block|{
name|Settings
name|settings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"index.similarity.default.type"
argument_list|,
literal|"classic"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|IndexSettings
name|indexSettings
init|=
name|IndexSettingsModule
operator|.
name|newIndexSettings
argument_list|(
literal|"test"
argument_list|,
name|settings
argument_list|)
decl_stmt|;
name|SimilarityService
name|service
init|=
operator|new
name|SimilarityService
argument_list|(
name|indexSettings
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|service
operator|.
name|getDefaultSimilarity
argument_list|()
operator|instanceof
name|ClassicSimilarity
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

