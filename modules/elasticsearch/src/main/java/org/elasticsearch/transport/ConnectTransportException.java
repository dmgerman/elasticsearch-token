begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|cluster
operator|.
name|node
operator|.
name|DiscoveryNode
import|;
end_import

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|ConnectTransportException
specifier|public
class|class
name|ConnectTransportException
extends|extends
name|ActionTransportException
block|{
DECL|field|node
specifier|private
specifier|final
name|DiscoveryNode
name|node
decl_stmt|;
DECL|method|ConnectTransportException
specifier|public
name|ConnectTransportException
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|,
name|String
name|msg
parameter_list|)
block|{
name|this
argument_list|(
name|node
argument_list|,
name|msg
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
DECL|method|ConnectTransportException
specifier|public
name|ConnectTransportException
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|,
name|String
name|msg
parameter_list|,
name|String
name|action
parameter_list|)
block|{
name|this
argument_list|(
name|node
argument_list|,
name|msg
argument_list|,
name|action
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
DECL|method|ConnectTransportException
specifier|public
name|ConnectTransportException
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|,
name|String
name|msg
parameter_list|,
name|Throwable
name|cause
parameter_list|)
block|{
name|this
argument_list|(
name|node
argument_list|,
name|msg
argument_list|,
literal|null
argument_list|,
name|cause
argument_list|)
expr_stmt|;
block|}
DECL|method|ConnectTransportException
specifier|public
name|ConnectTransportException
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|,
name|String
name|msg
parameter_list|,
name|String
name|action
parameter_list|,
name|Throwable
name|cause
parameter_list|)
block|{
name|super
argument_list|(
name|node
operator|.
name|name
argument_list|()
argument_list|,
name|node
operator|.
name|address
argument_list|()
argument_list|,
name|action
argument_list|,
name|cause
argument_list|)
expr_stmt|;
name|this
operator|.
name|node
operator|=
name|node
expr_stmt|;
block|}
DECL|method|node
specifier|public
name|DiscoveryNode
name|node
parameter_list|()
block|{
return|return
name|node
return|;
block|}
block|}
end_class

end_unit

