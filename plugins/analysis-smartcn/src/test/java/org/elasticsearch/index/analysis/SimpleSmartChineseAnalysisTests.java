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
name|plugin
operator|.
name|analysis
operator|.
name|smartcn
operator|.
name|AnalysisSmartChinesePlugin
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
name|hamcrest
operator|.
name|MatcherAssert
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
DECL|class|SimpleSmartChineseAnalysisTests
specifier|public
class|class
name|SimpleSmartChineseAnalysisTests
extends|extends
name|ESTestCase
block|{
DECL|method|testDefaultsIcuAnalysis
specifier|public
name|void
name|testDefaultsIcuAnalysis
parameter_list|()
throws|throws
name|IOException
block|{
specifier|final
name|AnalysisService
name|analysisService
init|=
name|createAnalysisService
argument_list|(
operator|new
name|Index
argument_list|(
literal|"test"
argument_list|,
literal|"_na_"
argument_list|)
argument_list|,
name|Settings
operator|.
name|EMPTY
argument_list|,
operator|new
name|AnalysisSmartChinesePlugin
argument_list|()
operator|::
name|onModule
argument_list|)
decl_stmt|;
name|TokenizerFactory
name|tokenizerFactory
init|=
name|analysisService
operator|.
name|tokenizer
argument_list|(
literal|"smartcn_tokenizer"
argument_list|)
decl_stmt|;
name|MatcherAssert
operator|.
name|assertThat
argument_list|(
name|tokenizerFactory
argument_list|,
name|instanceOf
argument_list|(
name|SmartChineseTokenizerTokenizerFactory
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

