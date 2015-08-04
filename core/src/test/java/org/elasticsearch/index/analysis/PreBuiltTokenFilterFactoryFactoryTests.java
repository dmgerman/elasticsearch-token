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
name|indices
operator|.
name|analysis
operator|.
name|PreBuiltTokenFilters
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
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|CoreMatchers
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|PreBuiltTokenFilterFactoryFactoryTests
specifier|public
class|class
name|PreBuiltTokenFilterFactoryFactoryTests
extends|extends
name|ESTestCase
block|{
annotation|@
name|Test
DECL|method|testThatCachingWorksForCachingStrategyOne
specifier|public
name|void
name|testThatCachingWorksForCachingStrategyOne
parameter_list|()
block|{
name|PreBuiltTokenFilterFactoryFactory
name|factory
init|=
operator|new
name|PreBuiltTokenFilterFactoryFactory
argument_list|(
name|PreBuiltTokenFilters
operator|.
name|WORD_DELIMITER
operator|.
name|getTokenFilterFactory
argument_list|(
name|Version
operator|.
name|CURRENT
argument_list|)
argument_list|)
decl_stmt|;
name|TokenFilterFactory
name|former090TokenizerFactory
init|=
name|factory
operator|.
name|create
argument_list|(
literal|"word_delimiter"
argument_list|,
name|Settings
operator|.
name|settingsBuilder
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
name|V_0_90_1
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|TokenFilterFactory
name|former090TokenizerFactoryCopy
init|=
name|factory
operator|.
name|create
argument_list|(
literal|"word_delimiter"
argument_list|,
name|Settings
operator|.
name|settingsBuilder
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
name|V_0_90_2
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|TokenFilterFactory
name|currentTokenizerFactory
init|=
name|factory
operator|.
name|create
argument_list|(
literal|"word_delimiter"
argument_list|,
name|Settings
operator|.
name|settingsBuilder
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
name|CURRENT
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|currentTokenizerFactory
argument_list|,
name|is
argument_list|(
name|former090TokenizerFactory
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|currentTokenizerFactory
argument_list|,
name|is
argument_list|(
name|former090TokenizerFactoryCopy
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testThatDifferentVersionsCanBeLoaded
specifier|public
name|void
name|testThatDifferentVersionsCanBeLoaded
parameter_list|()
block|{
name|PreBuiltTokenFilterFactoryFactory
name|factory
init|=
operator|new
name|PreBuiltTokenFilterFactoryFactory
argument_list|(
name|PreBuiltTokenFilters
operator|.
name|STOP
operator|.
name|getTokenFilterFactory
argument_list|(
name|Version
operator|.
name|CURRENT
argument_list|)
argument_list|)
decl_stmt|;
name|TokenFilterFactory
name|former090TokenizerFactory
init|=
name|factory
operator|.
name|create
argument_list|(
literal|"stop"
argument_list|,
name|Settings
operator|.
name|settingsBuilder
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
name|V_0_90_1
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|TokenFilterFactory
name|former090TokenizerFactoryCopy
init|=
name|factory
operator|.
name|create
argument_list|(
literal|"stop"
argument_list|,
name|Settings
operator|.
name|settingsBuilder
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
name|V_0_90_2
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|TokenFilterFactory
name|currentTokenizerFactory
init|=
name|factory
operator|.
name|create
argument_list|(
literal|"stop"
argument_list|,
name|Settings
operator|.
name|settingsBuilder
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
name|CURRENT
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|currentTokenizerFactory
argument_list|,
name|is
argument_list|(
name|not
argument_list|(
name|former090TokenizerFactory
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|former090TokenizerFactory
argument_list|,
name|is
argument_list|(
name|former090TokenizerFactoryCopy
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

