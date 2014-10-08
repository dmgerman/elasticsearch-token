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
name|ImmutableSettings
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
name|PreBuiltAnalyzers
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
name|ElasticsearchTestCase
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
name|Matchers
operator|.
name|is
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
name|not
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|PreBuiltAnalyzerProviderFactoryTests
specifier|public
class|class
name|PreBuiltAnalyzerProviderFactoryTests
extends|extends
name|ElasticsearchTestCase
block|{
annotation|@
name|Test
DECL|method|testVersioningInFactoryProvider
specifier|public
name|void
name|testVersioningInFactoryProvider
parameter_list|()
throws|throws
name|Exception
block|{
name|PreBuiltAnalyzerProviderFactory
name|factory
init|=
operator|new
name|PreBuiltAnalyzerProviderFactory
argument_list|(
literal|"default"
argument_list|,
name|AnalyzerScope
operator|.
name|INDEX
argument_list|,
name|PreBuiltAnalyzers
operator|.
name|STANDARD
operator|.
name|getAnalyzer
argument_list|(
name|Version
operator|.
name|CURRENT
argument_list|)
argument_list|)
decl_stmt|;
name|AnalyzerProvider
name|former090AnalyzerProvider
init|=
name|factory
operator|.
name|create
argument_list|(
literal|"default"
argument_list|,
name|ImmutableSettings
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
name|V_0_90_0
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|AnalyzerProvider
name|currentAnalyzerProviderReference
init|=
name|factory
operator|.
name|create
argument_list|(
literal|"default"
argument_list|,
name|ImmutableSettings
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
comment|// would love to access the version inside of the lucene analyzer, but that is not possible...
name|assertThat
argument_list|(
name|currentAnalyzerProviderReference
argument_list|,
name|is
argument_list|(
name|not
argument_list|(
name|former090AnalyzerProvider
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

