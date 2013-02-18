begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.indices.analyze
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|admin
operator|.
name|indices
operator|.
name|analyze
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|ActionListener
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|support
operator|.
name|single
operator|.
name|custom
operator|.
name|SingleCustomOperationRequestBuilder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|client
operator|.
name|IndicesAdminClient
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|client
operator|.
name|internal
operator|.
name|InternalIndicesAdminClient
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|AnalyzeRequestBuilder
specifier|public
class|class
name|AnalyzeRequestBuilder
extends|extends
name|SingleCustomOperationRequestBuilder
argument_list|<
name|AnalyzeRequest
argument_list|,
name|AnalyzeResponse
argument_list|,
name|AnalyzeRequestBuilder
argument_list|>
block|{
DECL|method|AnalyzeRequestBuilder
specifier|public
name|AnalyzeRequestBuilder
parameter_list|(
name|IndicesAdminClient
name|indicesClient
parameter_list|)
block|{
name|super
argument_list|(
operator|(
name|InternalIndicesAdminClient
operator|)
name|indicesClient
argument_list|,
operator|new
name|AnalyzeRequest
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|AnalyzeRequestBuilder
specifier|public
name|AnalyzeRequestBuilder
parameter_list|(
name|IndicesAdminClient
name|indicesClient
parameter_list|,
name|String
name|index
parameter_list|,
name|String
name|text
parameter_list|)
block|{
name|super
argument_list|(
operator|(
name|InternalIndicesAdminClient
operator|)
name|indicesClient
argument_list|,
operator|new
name|AnalyzeRequest
argument_list|(
name|index
argument_list|,
name|text
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**      * Sets the index to use to analyzer the text (for example, if it holds specific analyzers      * registered).      */
DECL|method|setIndex
specifier|public
name|AnalyzeRequestBuilder
name|setIndex
parameter_list|(
name|String
name|index
parameter_list|)
block|{
name|request
operator|.
name|setIndex
argument_list|(
name|index
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets the analyzer name to use in order to analyze the text.      *      * @param analyzer The analyzer name.      */
DECL|method|setAnalyzer
specifier|public
name|AnalyzeRequestBuilder
name|setAnalyzer
parameter_list|(
name|String
name|analyzer
parameter_list|)
block|{
name|request
operator|.
name|setAnalyzer
argument_list|(
name|analyzer
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets the field that its analyzer will be used to analyze the text. Note, requires an index      * to be set.      */
DECL|method|setField
specifier|public
name|AnalyzeRequestBuilder
name|setField
parameter_list|(
name|String
name|field
parameter_list|)
block|{
name|request
operator|.
name|setField
argument_list|(
name|field
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Instead of setting the analyzer, sets the tokenizer that will be used as part of a custom      * analyzer.      */
DECL|method|setTokenizer
specifier|public
name|AnalyzeRequestBuilder
name|setTokenizer
parameter_list|(
name|String
name|tokenizer
parameter_list|)
block|{
name|request
operator|.
name|setTokenizer
argument_list|(
name|tokenizer
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets token filters that will be used on top of a tokenizer provided.      */
DECL|method|setTokenFilters
specifier|public
name|AnalyzeRequestBuilder
name|setTokenFilters
parameter_list|(
name|String
modifier|...
name|tokenFilters
parameter_list|)
block|{
name|request
operator|.
name|setTokenFilters
argument_list|(
name|tokenFilters
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
DECL|method|doExecute
specifier|protected
name|void
name|doExecute
parameter_list|(
name|ActionListener
argument_list|<
name|AnalyzeResponse
argument_list|>
name|listener
parameter_list|)
block|{
operator|(
operator|(
name|IndicesAdminClient
operator|)
name|client
operator|)
operator|.
name|analyze
argument_list|(
name|request
argument_list|,
name|listener
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

