begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.cluster.node.stats
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
name|stats
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
name|NodesOperationRequest
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
name|io
operator|.
name|stream
operator|.
name|StreamInput
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
name|io
operator|.
name|stream
operator|.
name|StreamOutput
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

begin_comment
comment|/**  * A request to get node (cluster) level stats.  */
end_comment

begin_class
DECL|class|NodesStatsRequest
specifier|public
class|class
name|NodesStatsRequest
extends|extends
name|NodesOperationRequest
argument_list|<
name|NodesStatsRequest
argument_list|>
block|{
DECL|field|indices
specifier|private
name|boolean
name|indices
init|=
literal|true
decl_stmt|;
DECL|field|os
specifier|private
name|boolean
name|os
decl_stmt|;
DECL|field|process
specifier|private
name|boolean
name|process
decl_stmt|;
DECL|field|jvm
specifier|private
name|boolean
name|jvm
decl_stmt|;
DECL|field|threadPool
specifier|private
name|boolean
name|threadPool
decl_stmt|;
DECL|field|network
specifier|private
name|boolean
name|network
decl_stmt|;
DECL|field|fs
specifier|private
name|boolean
name|fs
decl_stmt|;
DECL|field|transport
specifier|private
name|boolean
name|transport
decl_stmt|;
DECL|field|http
specifier|private
name|boolean
name|http
decl_stmt|;
DECL|method|NodesStatsRequest
specifier|protected
name|NodesStatsRequest
parameter_list|()
block|{     }
comment|/**      * Get stats from nodes based on the nodes ids specified. If none are passed, stats      * for all nodes will be returned.      */
DECL|method|NodesStatsRequest
specifier|public
name|NodesStatsRequest
parameter_list|(
name|String
modifier|...
name|nodesIds
parameter_list|)
block|{
name|super
argument_list|(
name|nodesIds
argument_list|)
expr_stmt|;
block|}
comment|/**      * Sets all the request flags.      */
DECL|method|all
specifier|public
name|NodesStatsRequest
name|all
parameter_list|()
block|{
name|this
operator|.
name|indices
operator|=
literal|true
expr_stmt|;
name|this
operator|.
name|os
operator|=
literal|true
expr_stmt|;
name|this
operator|.
name|process
operator|=
literal|true
expr_stmt|;
name|this
operator|.
name|jvm
operator|=
literal|true
expr_stmt|;
name|this
operator|.
name|threadPool
operator|=
literal|true
expr_stmt|;
name|this
operator|.
name|network
operator|=
literal|true
expr_stmt|;
name|this
operator|.
name|fs
operator|=
literal|true
expr_stmt|;
name|this
operator|.
name|transport
operator|=
literal|true
expr_stmt|;
name|this
operator|.
name|http
operator|=
literal|true
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Clears all the request flags.      */
DECL|method|clear
specifier|public
name|NodesStatsRequest
name|clear
parameter_list|()
block|{
name|this
operator|.
name|indices
operator|=
literal|false
expr_stmt|;
name|this
operator|.
name|os
operator|=
literal|false
expr_stmt|;
name|this
operator|.
name|process
operator|=
literal|false
expr_stmt|;
name|this
operator|.
name|jvm
operator|=
literal|false
expr_stmt|;
name|this
operator|.
name|threadPool
operator|=
literal|false
expr_stmt|;
name|this
operator|.
name|network
operator|=
literal|false
expr_stmt|;
name|this
operator|.
name|fs
operator|=
literal|false
expr_stmt|;
name|this
operator|.
name|transport
operator|=
literal|false
expr_stmt|;
name|this
operator|.
name|http
operator|=
literal|false
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Should indices stats be returned.      */
DECL|method|isIndices
specifier|public
name|boolean
name|isIndices
parameter_list|()
block|{
return|return
name|this
operator|.
name|indices
return|;
block|}
comment|/**      * Should indices stats be returned.      */
DECL|method|setIndices
specifier|public
name|NodesStatsRequest
name|setIndices
parameter_list|(
name|boolean
name|indices
parameter_list|)
block|{
name|this
operator|.
name|indices
operator|=
name|indices
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Should the node OS be returned.      */
DECL|method|isOs
specifier|public
name|boolean
name|isOs
parameter_list|()
block|{
return|return
name|this
operator|.
name|os
return|;
block|}
comment|/**      * Should the node OS be returned.      */
DECL|method|setOs
specifier|public
name|NodesStatsRequest
name|setOs
parameter_list|(
name|boolean
name|os
parameter_list|)
block|{
name|this
operator|.
name|os
operator|=
name|os
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Should the node Process be returned.      */
DECL|method|isProcess
specifier|public
name|boolean
name|isProcess
parameter_list|()
block|{
return|return
name|this
operator|.
name|process
return|;
block|}
comment|/**      * Should the node Process be returned.      */
DECL|method|setProcess
specifier|public
name|NodesStatsRequest
name|setProcess
parameter_list|(
name|boolean
name|process
parameter_list|)
block|{
name|this
operator|.
name|process
operator|=
name|process
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Should the node JVM be returned.      */
DECL|method|isJvm
specifier|public
name|boolean
name|isJvm
parameter_list|()
block|{
return|return
name|this
operator|.
name|jvm
return|;
block|}
comment|/**      * Should the node JVM be returned.      */
DECL|method|setJvm
specifier|public
name|NodesStatsRequest
name|setJvm
parameter_list|(
name|boolean
name|jvm
parameter_list|)
block|{
name|this
operator|.
name|jvm
operator|=
name|jvm
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Should the node Thread Pool be returned.      */
DECL|method|isThreadPool
specifier|public
name|boolean
name|isThreadPool
parameter_list|()
block|{
return|return
name|this
operator|.
name|threadPool
return|;
block|}
comment|/**      * Should the node Thread Pool be returned.      */
DECL|method|setThreadPool
specifier|public
name|NodesStatsRequest
name|setThreadPool
parameter_list|(
name|boolean
name|threadPool
parameter_list|)
block|{
name|this
operator|.
name|threadPool
operator|=
name|threadPool
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Should the node Network be returned.      */
DECL|method|isNetwork
specifier|public
name|boolean
name|isNetwork
parameter_list|()
block|{
return|return
name|this
operator|.
name|network
return|;
block|}
comment|/**      * Should the node Network be returned.      */
DECL|method|setNetwork
specifier|public
name|NodesStatsRequest
name|setNetwork
parameter_list|(
name|boolean
name|network
parameter_list|)
block|{
name|this
operator|.
name|network
operator|=
name|network
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Should the node file system stats be returned.      */
DECL|method|isFs
specifier|public
name|boolean
name|isFs
parameter_list|()
block|{
return|return
name|this
operator|.
name|fs
return|;
block|}
comment|/**      * Should the node file system stats be returned.      */
DECL|method|setFs
specifier|public
name|NodesStatsRequest
name|setFs
parameter_list|(
name|boolean
name|fs
parameter_list|)
block|{
name|this
operator|.
name|fs
operator|=
name|fs
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Should the node Transport be returned.      */
DECL|method|isTransport
specifier|public
name|boolean
name|isTransport
parameter_list|()
block|{
return|return
name|this
operator|.
name|transport
return|;
block|}
comment|/**      * Should the node Transport be returned.      */
DECL|method|setTransport
specifier|public
name|NodesStatsRequest
name|setTransport
parameter_list|(
name|boolean
name|transport
parameter_list|)
block|{
name|this
operator|.
name|transport
operator|=
name|transport
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Should the node HTTP be returned.      */
DECL|method|isHttp
specifier|public
name|boolean
name|isHttp
parameter_list|()
block|{
return|return
name|this
operator|.
name|http
return|;
block|}
comment|/**      * Should the node HTTP be returned.      */
DECL|method|setHttp
specifier|public
name|NodesStatsRequest
name|setHttp
parameter_list|(
name|boolean
name|http
parameter_list|)
block|{
name|this
operator|.
name|http
operator|=
name|http
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
DECL|method|readFrom
specifier|public
name|void
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|indices
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
name|os
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
name|process
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
name|jvm
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
name|threadPool
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
name|network
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
name|fs
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
name|transport
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
name|http
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|writeTo
specifier|public
name|void
name|writeTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|indices
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|os
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|process
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|jvm
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|threadPool
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|network
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|fs
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|transport
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|http
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

