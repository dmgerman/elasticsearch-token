begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.support
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|support
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|threadpool
operator|.
name|ThreadPool
import|;
end_import

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|PlainListenableActionFuture
specifier|public
class|class
name|PlainListenableActionFuture
parameter_list|<
name|T
parameter_list|>
extends|extends
name|AbstractListenableActionFuture
argument_list|<
name|T
argument_list|,
name|T
argument_list|>
block|{
DECL|method|PlainListenableActionFuture
specifier|public
name|PlainListenableActionFuture
parameter_list|(
name|boolean
name|listenerThreaded
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|)
block|{
name|super
argument_list|(
name|listenerThreaded
argument_list|,
name|threadPool
argument_list|)
expr_stmt|;
block|}
DECL|method|convert
annotation|@
name|Override
specifier|protected
name|T
name|convert
parameter_list|(
name|T
name|response
parameter_list|)
block|{
return|return
name|response
return|;
block|}
block|}
end_class

end_unit

