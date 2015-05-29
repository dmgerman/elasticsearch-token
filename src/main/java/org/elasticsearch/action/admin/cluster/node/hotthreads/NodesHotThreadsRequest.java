begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.cluster.node.hotthreads
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
name|hotthreads
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
name|BaseNodesRequest
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
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|unit
operator|.
name|TimeValue
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|TimeUnit
import|;
end_import

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|NodesHotThreadsRequest
specifier|public
class|class
name|NodesHotThreadsRequest
extends|extends
name|BaseNodesRequest
argument_list|<
name|NodesHotThreadsRequest
argument_list|>
block|{
DECL|field|threads
name|int
name|threads
init|=
literal|3
decl_stmt|;
DECL|field|type
name|String
name|type
init|=
literal|"cpu"
decl_stmt|;
DECL|field|interval
name|TimeValue
name|interval
init|=
operator|new
name|TimeValue
argument_list|(
literal|500
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
decl_stmt|;
DECL|field|snapshots
name|int
name|snapshots
init|=
literal|10
decl_stmt|;
DECL|field|ignoreIdleThreads
name|boolean
name|ignoreIdleThreads
init|=
literal|true
decl_stmt|;
comment|// for serialization
DECL|method|NodesHotThreadsRequest
name|NodesHotThreadsRequest
parameter_list|()
block|{      }
comment|/**      * Get hot threads from nodes based on the nodes ids specified. If none are passed, hot      * threads for all nodes is used.      */
DECL|method|NodesHotThreadsRequest
specifier|public
name|NodesHotThreadsRequest
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
DECL|method|threads
specifier|public
name|int
name|threads
parameter_list|()
block|{
return|return
name|this
operator|.
name|threads
return|;
block|}
DECL|method|threads
specifier|public
name|NodesHotThreadsRequest
name|threads
parameter_list|(
name|int
name|threads
parameter_list|)
block|{
name|this
operator|.
name|threads
operator|=
name|threads
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|ignoreIdleThreads
specifier|public
name|boolean
name|ignoreIdleThreads
parameter_list|()
block|{
return|return
name|this
operator|.
name|ignoreIdleThreads
return|;
block|}
DECL|method|ignoreIdleThreads
specifier|public
name|NodesHotThreadsRequest
name|ignoreIdleThreads
parameter_list|(
name|boolean
name|ignoreIdleThreads
parameter_list|)
block|{
name|this
operator|.
name|ignoreIdleThreads
operator|=
name|ignoreIdleThreads
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|type
specifier|public
name|NodesHotThreadsRequest
name|type
parameter_list|(
name|String
name|type
parameter_list|)
block|{
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|type
specifier|public
name|String
name|type
parameter_list|()
block|{
return|return
name|this
operator|.
name|type
return|;
block|}
DECL|method|interval
specifier|public
name|NodesHotThreadsRequest
name|interval
parameter_list|(
name|TimeValue
name|interval
parameter_list|)
block|{
name|this
operator|.
name|interval
operator|=
name|interval
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|interval
specifier|public
name|TimeValue
name|interval
parameter_list|()
block|{
return|return
name|this
operator|.
name|interval
return|;
block|}
DECL|method|snapshots
specifier|public
name|int
name|snapshots
parameter_list|()
block|{
return|return
name|this
operator|.
name|snapshots
return|;
block|}
DECL|method|snapshots
specifier|public
name|NodesHotThreadsRequest
name|snapshots
parameter_list|(
name|int
name|snapshots
parameter_list|)
block|{
name|this
operator|.
name|snapshots
operator|=
name|snapshots
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
name|threads
operator|=
name|in
operator|.
name|readInt
argument_list|()
expr_stmt|;
name|ignoreIdleThreads
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
name|type
operator|=
name|in
operator|.
name|readString
argument_list|()
expr_stmt|;
name|interval
operator|=
name|TimeValue
operator|.
name|readTimeValue
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|snapshots
operator|=
name|in
operator|.
name|readInt
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
name|writeInt
argument_list|(
name|threads
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|ignoreIdleThreads
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeString
argument_list|(
name|type
argument_list|)
expr_stmt|;
name|interval
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|snapshots
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

