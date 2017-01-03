begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
package|;
end_package

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
name|common
operator|.
name|xcontent
operator|.
name|NamedXContentRegistry
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
name|analysis
operator|.
name|IndexAnalyzers
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
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|similarity
operator|.
name|SimilarityService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|IndicesModule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|mapper
operator|.
name|MapperRegistry
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
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|file
operator|.
name|Path
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
name|elasticsearch
operator|.
name|test
operator|.
name|ESTestCase
operator|.
name|createTestAnalysis
import|;
end_import

begin_class
DECL|class|MapperTestUtils
specifier|public
class|class
name|MapperTestUtils
block|{
DECL|method|newMapperService
specifier|public
specifier|static
name|MapperService
name|newMapperService
parameter_list|(
name|NamedXContentRegistry
name|xContentRegistry
parameter_list|,
name|Path
name|tempDir
parameter_list|,
name|Settings
name|indexSettings
parameter_list|)
throws|throws
name|IOException
block|{
name|IndicesModule
name|indicesModule
init|=
operator|new
name|IndicesModule
argument_list|(
name|Collections
operator|.
name|emptyList
argument_list|()
argument_list|)
decl_stmt|;
return|return
name|newMapperService
argument_list|(
name|xContentRegistry
argument_list|,
name|tempDir
argument_list|,
name|indexSettings
argument_list|,
name|indicesModule
argument_list|)
return|;
block|}
DECL|method|newMapperService
specifier|public
specifier|static
name|MapperService
name|newMapperService
parameter_list|(
name|NamedXContentRegistry
name|xContentRegistry
parameter_list|,
name|Path
name|tempDir
parameter_list|,
name|Settings
name|settings
parameter_list|,
name|IndicesModule
name|indicesModule
parameter_list|)
throws|throws
name|IOException
block|{
name|Settings
operator|.
name|Builder
name|settingsBuilder
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|Environment
operator|.
name|PATH_HOME_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
name|tempDir
argument_list|)
operator|.
name|put
argument_list|(
name|settings
argument_list|)
decl_stmt|;
if|if
condition|(
name|settings
operator|.
name|get
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_VERSION_CREATED
argument_list|)
operator|==
literal|null
condition|)
block|{
name|settingsBuilder
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_VERSION_CREATED
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
expr_stmt|;
block|}
name|Settings
name|finalSettings
init|=
name|settingsBuilder
operator|.
name|build
argument_list|()
decl_stmt|;
name|MapperRegistry
name|mapperRegistry
init|=
name|indicesModule
operator|.
name|getMapperRegistry
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
name|finalSettings
argument_list|)
decl_stmt|;
name|IndexAnalyzers
name|indexAnalyzers
init|=
name|createTestAnalysis
argument_list|(
name|indexSettings
argument_list|,
name|finalSettings
argument_list|)
operator|.
name|indexAnalyzers
decl_stmt|;
name|SimilarityService
name|similarityService
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
return|return
operator|new
name|MapperService
argument_list|(
name|indexSettings
argument_list|,
name|indexAnalyzers
argument_list|,
name|xContentRegistry
argument_list|,
name|similarityService
argument_list|,
name|mapperRegistry
argument_list|,
parameter_list|()
lambda|->
literal|null
argument_list|)
return|;
block|}
block|}
end_class

end_unit

