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
name|CharArraySet
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
name|analysis
operator|.
name|hu
operator|.
name|HungarianAnalyzer
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
name|IndexSettings
import|;
end_import

begin_class
DECL|class|HungarianAnalyzerProvider
specifier|public
class|class
name|HungarianAnalyzerProvider
extends|extends
name|AbstractIndexAnalyzerProvider
argument_list|<
name|HungarianAnalyzer
argument_list|>
block|{
DECL|field|analyzer
specifier|private
specifier|final
name|HungarianAnalyzer
name|analyzer
decl_stmt|;
DECL|method|HungarianAnalyzerProvider
specifier|public
name|HungarianAnalyzerProvider
parameter_list|(
name|IndexSettings
name|indexSettings
parameter_list|,
name|Environment
name|env
parameter_list|,
name|String
name|name
parameter_list|,
name|Settings
name|settings
parameter_list|)
block|{
name|super
argument_list|(
name|indexSettings
argument_list|,
name|name
argument_list|,
name|settings
argument_list|)
expr_stmt|;
name|analyzer
operator|=
operator|new
name|HungarianAnalyzer
argument_list|(
name|Analysis
operator|.
name|parseStopWords
argument_list|(
name|env
argument_list|,
name|settings
argument_list|,
name|HungarianAnalyzer
operator|.
name|getDefaultStopSet
argument_list|()
argument_list|)
argument_list|,
name|Analysis
operator|.
name|parseStemExclusion
argument_list|(
name|settings
argument_list|,
name|CharArraySet
operator|.
name|EMPTY_SET
argument_list|)
argument_list|)
expr_stmt|;
name|analyzer
operator|.
name|setVersion
argument_list|(
name|version
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|get
specifier|public
name|HungarianAnalyzer
name|get
parameter_list|()
block|{
return|return
name|this
operator|.
name|analyzer
return|;
block|}
block|}
end_class

end_unit

