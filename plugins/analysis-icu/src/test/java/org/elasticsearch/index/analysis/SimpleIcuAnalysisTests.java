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
name|test
operator|.
name|ESTestCase
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|settings
operator|.
name|Settings
operator|.
name|settingsBuilder
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|analysis
operator|.
name|AnalysisTestUtils
operator|.
name|createAnalysisService
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

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|SimpleIcuAnalysisTests
specifier|public
class|class
name|SimpleIcuAnalysisTests
extends|extends
name|ESTestCase
block|{
DECL|method|testDefaultsIcuAnalysis
specifier|public
name|void
name|testDefaultsIcuAnalysis
parameter_list|()
block|{
name|Settings
name|settings
init|=
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"path.home"
argument_list|,
name|createTempDir
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|AnalysisService
name|analysisService
init|=
name|createAnalysisService
argument_list|(
name|settings
argument_list|)
decl_stmt|;
name|TokenizerFactory
name|tokenizerFactory
init|=
name|analysisService
operator|.
name|tokenizer
argument_list|(
literal|"icu_tokenizer"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|tokenizerFactory
argument_list|,
name|instanceOf
argument_list|(
name|IcuTokenizerFactory
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|TokenFilterFactory
name|filterFactory
init|=
name|analysisService
operator|.
name|tokenFilter
argument_list|(
literal|"icu_normalizer"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|filterFactory
argument_list|,
name|instanceOf
argument_list|(
name|IcuNormalizerTokenFilterFactory
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|filterFactory
operator|=
name|analysisService
operator|.
name|tokenFilter
argument_list|(
literal|"icu_folding"
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterFactory
argument_list|,
name|instanceOf
argument_list|(
name|IcuFoldingTokenFilterFactory
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|filterFactory
operator|=
name|analysisService
operator|.
name|tokenFilter
argument_list|(
literal|"icu_collation"
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterFactory
argument_list|,
name|instanceOf
argument_list|(
name|IcuCollationTokenFilterFactory
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|filterFactory
operator|=
name|analysisService
operator|.
name|tokenFilter
argument_list|(
literal|"icu_transform"
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterFactory
argument_list|,
name|instanceOf
argument_list|(
name|IcuTransformTokenFilterFactory
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|CharFilterFactory
name|charFilterFactory
init|=
name|analysisService
operator|.
name|charFilter
argument_list|(
literal|"icu_normalizer"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|charFilterFactory
argument_list|,
name|instanceOf
argument_list|(
name|IcuNormalizerCharFilterFactory
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

