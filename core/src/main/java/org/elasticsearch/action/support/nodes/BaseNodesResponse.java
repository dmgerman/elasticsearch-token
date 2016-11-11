begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.support.nodes
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|support
operator|.
name|nodes
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
name|ActionResponse
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
name|FailedNodeException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|ClusterName
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Objects
import|;
end_import

begin_class
DECL|class|BaseNodesResponse
specifier|public
specifier|abstract
class|class
name|BaseNodesResponse
parameter_list|<
name|TNodeResponse
extends|extends
name|BaseNodeResponse
parameter_list|>
extends|extends
name|ActionResponse
block|{
DECL|field|clusterName
specifier|private
name|ClusterName
name|clusterName
decl_stmt|;
DECL|field|failures
specifier|private
name|List
argument_list|<
name|FailedNodeException
argument_list|>
name|failures
decl_stmt|;
DECL|field|nodes
specifier|private
name|List
argument_list|<
name|TNodeResponse
argument_list|>
name|nodes
decl_stmt|;
DECL|field|nodesMap
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|TNodeResponse
argument_list|>
name|nodesMap
decl_stmt|;
DECL|method|BaseNodesResponse
specifier|protected
name|BaseNodesResponse
parameter_list|()
block|{     }
DECL|method|BaseNodesResponse
specifier|protected
name|BaseNodesResponse
parameter_list|(
name|ClusterName
name|clusterName
parameter_list|,
name|List
argument_list|<
name|TNodeResponse
argument_list|>
name|nodes
parameter_list|,
name|List
argument_list|<
name|FailedNodeException
argument_list|>
name|failures
parameter_list|)
block|{
name|this
operator|.
name|clusterName
operator|=
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|clusterName
argument_list|)
expr_stmt|;
name|this
operator|.
name|failures
operator|=
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|failures
argument_list|)
expr_stmt|;
name|this
operator|.
name|nodes
operator|=
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|nodes
argument_list|)
expr_stmt|;
block|}
comment|/**      * Get the {@link ClusterName} associated with all of the nodes.      *      * @return Never {@code null}.      */
DECL|method|getClusterName
specifier|public
name|ClusterName
name|getClusterName
parameter_list|()
block|{
return|return
name|clusterName
return|;
block|}
comment|/**      * Get the failed node exceptions.      *      * @return Never {@code null}. Can be empty.      */
DECL|method|failures
specifier|public
name|List
argument_list|<
name|FailedNodeException
argument_list|>
name|failures
parameter_list|()
block|{
return|return
name|failures
return|;
block|}
comment|/**      * Determine if there are any node failures in {@link #failures}.      *      * @return {@code true} if {@link #failures} contains at least 1 {@link FailedNodeException}.      */
DECL|method|hasFailures
specifier|public
name|boolean
name|hasFailures
parameter_list|()
block|{
return|return
name|failures
operator|.
name|isEmpty
argument_list|()
operator|==
literal|false
return|;
block|}
comment|/**      * Get the<em>successful</em> node responses.      *      * @return Never {@code null}. Can be empty.      * @see #hasFailures()      */
DECL|method|getNodes
specifier|public
name|List
argument_list|<
name|TNodeResponse
argument_list|>
name|getNodes
parameter_list|()
block|{
return|return
name|nodes
return|;
block|}
comment|/**      * Lazily build and get a map of Node ID to node response.      *      * @return Never {@code null}. Can be empty.      * @see #getNodes()      */
DECL|method|getNodesMap
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|TNodeResponse
argument_list|>
name|getNodesMap
parameter_list|()
block|{
if|if
condition|(
name|nodesMap
operator|==
literal|null
condition|)
block|{
name|nodesMap
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
expr_stmt|;
for|for
control|(
name|TNodeResponse
name|nodeResponse
range|:
name|nodes
control|)
block|{
name|nodesMap
operator|.
name|put
argument_list|(
name|nodeResponse
operator|.
name|getNode
argument_list|()
operator|.
name|getId
argument_list|()
argument_list|,
name|nodeResponse
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|nodesMap
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
name|clusterName
operator|=
operator|new
name|ClusterName
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|nodes
operator|=
name|readNodesFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|failures
operator|=
name|in
operator|.
name|readList
argument_list|(
name|FailedNodeException
operator|::
operator|new
argument_list|)
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
name|clusterName
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|writeNodesTo
argument_list|(
name|out
argument_list|,
name|nodes
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeList
argument_list|(
name|failures
argument_list|)
expr_stmt|;
block|}
comment|/**      * Read the {@link #nodes} from the stream.      *      * @return Never {@code null}.      */
DECL|method|readNodesFrom
specifier|protected
specifier|abstract
name|List
argument_list|<
name|TNodeResponse
argument_list|>
name|readNodesFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**      * Write the {@link #nodes} to the stream.      */
DECL|method|writeNodesTo
specifier|protected
specifier|abstract
name|void
name|writeNodesTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|,
name|List
argument_list|<
name|TNodeResponse
argument_list|>
name|nodes
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
end_class

end_unit

