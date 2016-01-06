begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.transport
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|transport
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|tasks
operator|.
name|Task
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_interface
DECL|interface|TransportRequestHandler
specifier|public
interface|interface
name|TransportRequestHandler
parameter_list|<
name|T
extends|extends
name|TransportRequest
parameter_list|>
block|{
comment|/**      * Override this method if access to the Task parameter is needed      */
DECL|method|messageReceived
specifier|default
name|void
name|messageReceived
parameter_list|(
specifier|final
name|T
name|request
parameter_list|,
specifier|final
name|TransportChannel
name|channel
parameter_list|,
name|Task
name|task
parameter_list|)
throws|throws
name|Exception
block|{
name|messageReceived
argument_list|(
name|request
argument_list|,
name|channel
argument_list|)
expr_stmt|;
block|}
DECL|method|messageReceived
name|void
name|messageReceived
parameter_list|(
name|T
name|request
parameter_list|,
name|TransportChannel
name|channel
parameter_list|)
throws|throws
name|Exception
function_decl|;
block|}
end_interface

end_unit

