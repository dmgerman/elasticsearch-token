begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.bench
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|bench
package|;
end_package

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

begin_comment
comment|/**  * Request builder for benchmark status  */
end_comment

begin_class
DECL|class|BenchmarkStatusRequestBuilder
specifier|public
class|class
name|BenchmarkStatusRequestBuilder
extends|extends
name|ActionRequestBuilder
argument_list|<
name|BenchmarkStatusRequest
argument_list|,
name|BenchmarkStatusResponse
argument_list|,
name|BenchmarkStatusRequestBuilder
argument_list|>
block|{
DECL|method|BenchmarkStatusRequestBuilder
specifier|public
name|BenchmarkStatusRequestBuilder
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
name|BenchmarkStatusRequest
argument_list|()
argument_list|)
expr_stmt|;
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
name|BenchmarkStatusResponse
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
name|benchStatus
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

