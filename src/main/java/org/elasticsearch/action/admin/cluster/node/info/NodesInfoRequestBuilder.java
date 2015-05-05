begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.cluster.node.info
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|admin
operator|.
name|cluster
operator|.
name|node
operator|.
name|info
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
name|support
operator|.
name|nodes
operator|.
name|NodesOperationRequestBuilder
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
name|ElasticsearchClient
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|NodesInfoRequestBuilder
specifier|public
class|class
name|NodesInfoRequestBuilder
extends|extends
name|NodesOperationRequestBuilder
argument_list|<
name|NodesInfoRequest
argument_list|,
name|NodesInfoResponse
argument_list|,
name|NodesInfoRequestBuilder
argument_list|>
block|{
DECL|method|NodesInfoRequestBuilder
specifier|public
name|NodesInfoRequestBuilder
parameter_list|(
name|ElasticsearchClient
name|client
parameter_list|,
name|NodesInfoAction
name|action
parameter_list|)
block|{
name|super
argument_list|(
name|client
argument_list|,
name|action
argument_list|,
operator|new
name|NodesInfoRequest
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**      * Clears all info flags.      */
DECL|method|clear
specifier|public
name|NodesInfoRequestBuilder
name|clear
parameter_list|()
block|{
name|request
operator|.
name|clear
argument_list|()
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets to reutrn all the data.      */
DECL|method|all
specifier|public
name|NodesInfoRequestBuilder
name|all
parameter_list|()
block|{
name|request
operator|.
name|all
argument_list|()
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Should the node settings be returned.      */
DECL|method|setSettings
specifier|public
name|NodesInfoRequestBuilder
name|setSettings
parameter_list|(
name|boolean
name|settings
parameter_list|)
block|{
name|request
operator|.
name|settings
argument_list|(
name|settings
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Should the node OS info be returned.      */
DECL|method|setOs
specifier|public
name|NodesInfoRequestBuilder
name|setOs
parameter_list|(
name|boolean
name|os
parameter_list|)
block|{
name|request
operator|.
name|os
argument_list|(
name|os
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Should the node OS process be returned.      */
DECL|method|setProcess
specifier|public
name|NodesInfoRequestBuilder
name|setProcess
parameter_list|(
name|boolean
name|process
parameter_list|)
block|{
name|request
operator|.
name|process
argument_list|(
name|process
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Should the node JVM info be returned.      */
DECL|method|setJvm
specifier|public
name|NodesInfoRequestBuilder
name|setJvm
parameter_list|(
name|boolean
name|jvm
parameter_list|)
block|{
name|request
operator|.
name|jvm
argument_list|(
name|jvm
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Should the node thread pool info be returned.      */
DECL|method|setThreadPool
specifier|public
name|NodesInfoRequestBuilder
name|setThreadPool
parameter_list|(
name|boolean
name|threadPool
parameter_list|)
block|{
name|request
operator|.
name|threadPool
argument_list|(
name|threadPool
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Should the node Network info be returned.      */
DECL|method|setNetwork
specifier|public
name|NodesInfoRequestBuilder
name|setNetwork
parameter_list|(
name|boolean
name|network
parameter_list|)
block|{
name|request
operator|.
name|network
argument_list|(
name|network
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Should the node Transport info be returned.      */
DECL|method|setTransport
specifier|public
name|NodesInfoRequestBuilder
name|setTransport
parameter_list|(
name|boolean
name|transport
parameter_list|)
block|{
name|request
operator|.
name|transport
argument_list|(
name|transport
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Should the node HTTP info be returned.      */
DECL|method|setHttp
specifier|public
name|NodesInfoRequestBuilder
name|setHttp
parameter_list|(
name|boolean
name|http
parameter_list|)
block|{
name|request
operator|.
name|http
argument_list|(
name|http
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Should the node plugins info be returned.      */
DECL|method|setPlugins
specifier|public
name|NodesInfoRequestBuilder
name|setPlugins
parameter_list|(
name|boolean
name|plugins
parameter_list|)
block|{
name|request
argument_list|()
operator|.
name|plugins
argument_list|(
name|plugins
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
block|}
end_class

end_unit

