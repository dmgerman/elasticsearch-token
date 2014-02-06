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
name|apache
operator|.
name|lucene
operator|.
name|analysis
operator|.
name|Analyzer
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
name|indices
operator|.
name|analysis
operator|.
name|PreBuiltAnalyzers
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|PreBuiltAnalyzerProviderFactory
specifier|public
class|class
name|PreBuiltAnalyzerProviderFactory
implements|implements
name|AnalyzerProviderFactory
block|{
DECL|field|analyzerProvider
specifier|private
specifier|final
name|PreBuiltAnalyzerProvider
name|analyzerProvider
decl_stmt|;
DECL|method|PreBuiltAnalyzerProviderFactory
specifier|public
name|PreBuiltAnalyzerProviderFactory
parameter_list|(
name|String
name|name
parameter_list|,
name|AnalyzerScope
name|scope
parameter_list|,
name|Analyzer
name|analyzer
parameter_list|)
block|{
name|analyzerProvider
operator|=
operator|new
name|PreBuiltAnalyzerProvider
argument_list|(
name|name
argument_list|,
name|scope
argument_list|,
name|analyzer
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|create
specifier|public
name|AnalyzerProvider
name|create
parameter_list|(
name|String
name|name
parameter_list|,
name|Settings
name|settings
parameter_list|)
block|{
name|Version
name|indexVersion
init|=
name|settings
operator|.
name|getAsVersion
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_VERSION_CREATED
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|Version
operator|.
name|CURRENT
operator|.
name|equals
argument_list|(
name|indexVersion
argument_list|)
condition|)
block|{
name|PreBuiltAnalyzers
name|preBuiltAnalyzers
init|=
name|PreBuiltAnalyzers
operator|.
name|getOrDefault
argument_list|(
name|name
argument_list|,
literal|null
argument_list|)
decl_stmt|;
if|if
condition|(
name|preBuiltAnalyzers
operator|!=
literal|null
condition|)
block|{
name|Analyzer
name|analyzer
init|=
name|preBuiltAnalyzers
operator|.
name|getAnalyzer
argument_list|(
name|indexVersion
argument_list|)
decl_stmt|;
return|return
operator|new
name|PreBuiltAnalyzerProvider
argument_list|(
name|name
argument_list|,
name|AnalyzerScope
operator|.
name|INDICES
argument_list|,
name|analyzer
argument_list|)
return|;
block|}
block|}
return|return
name|analyzerProvider
return|;
block|}
DECL|method|analyzer
specifier|public
name|Analyzer
name|analyzer
parameter_list|()
block|{
return|return
name|analyzerProvider
operator|.
name|get
argument_list|()
return|;
block|}
block|}
end_class

end_unit

