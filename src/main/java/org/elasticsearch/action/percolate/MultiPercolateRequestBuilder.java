begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements. See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the  * License for the specific language governing permissions and limitations under  * the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.percolate
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|percolate
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
name|ActionRequestBuilder
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
name|IgnoreIndices
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
name|Client
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
name|InternalClient
import|;
end_import

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|MultiPercolateRequestBuilder
specifier|public
class|class
name|MultiPercolateRequestBuilder
extends|extends
name|ActionRequestBuilder
argument_list|<
name|MultiPercolateRequest
argument_list|,
name|MultiPercolateResponse
argument_list|,
name|MultiPercolateRequestBuilder
argument_list|>
block|{
DECL|method|MultiPercolateRequestBuilder
specifier|public
name|MultiPercolateRequestBuilder
parameter_list|(
name|Client
name|client
parameter_list|)
block|{
name|super
argument_list|(
operator|(
name|InternalClient
operator|)
name|client
argument_list|,
operator|new
name|MultiPercolateRequest
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**      * Bundles the specified percolate request to the multi percolate request.      */
DECL|method|add
specifier|public
name|MultiPercolateRequestBuilder
name|add
parameter_list|(
name|PercolateRequest
name|percolateRequest
parameter_list|)
block|{
name|request
operator|.
name|add
argument_list|(
name|percolateRequest
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Bundles the specified percolate request build to the multi percolate request.      */
DECL|method|add
specifier|public
name|MultiPercolateRequestBuilder
name|add
parameter_list|(
name|PercolateRequestBuilder
name|percolateRequestBuilder
parameter_list|)
block|{
name|request
operator|.
name|add
argument_list|(
name|percolateRequestBuilder
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Specifies how to globally ignore indices that are not available.      */
DECL|method|setIgnoreIndices
specifier|public
name|MultiPercolateRequestBuilder
name|setIgnoreIndices
parameter_list|(
name|IgnoreIndices
name|ignoreIndices
parameter_list|)
block|{
name|request
operator|.
name|ignoreIndices
argument_list|(
name|ignoreIndices
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
name|MultiPercolateResponse
argument_list|>
name|listener
parameter_list|)
block|{
operator|(
operator|(
name|Client
operator|)
name|client
operator|)
operator|.
name|multiPercolate
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

