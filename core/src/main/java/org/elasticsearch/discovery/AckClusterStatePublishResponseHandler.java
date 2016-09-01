begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.discovery
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|discovery
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|logging
operator|.
name|log4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|logging
operator|.
name|log4j
operator|.
name|message
operator|.
name|ParameterizedMessage
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|logging
operator|.
name|log4j
operator|.
name|util
operator|.
name|Supplier
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
name|node
operator|.
name|DiscoveryNode
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
name|logging
operator|.
name|ESLoggerFactory
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_comment
comment|/**  * Allows to wait for all nodes to reply to the publish of a new cluster state  * and notifies the {@link org.elasticsearch.discovery.Discovery.AckListener}  * so that the cluster state update can be acknowledged  */
end_comment

begin_class
DECL|class|AckClusterStatePublishResponseHandler
specifier|public
class|class
name|AckClusterStatePublishResponseHandler
extends|extends
name|BlockingClusterStatePublishResponseHandler
block|{
DECL|field|logger
specifier|private
specifier|static
specifier|final
name|Logger
name|logger
init|=
name|ESLoggerFactory
operator|.
name|getLogger
argument_list|(
name|AckClusterStatePublishResponseHandler
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
DECL|field|ackListener
specifier|private
specifier|final
name|Discovery
operator|.
name|AckListener
name|ackListener
decl_stmt|;
comment|/**      * Creates a new AckClusterStatePublishResponseHandler      * @param publishingToNodes the set of nodes to which the cluster state will be published and should respond      * @param ackListener the {@link org.elasticsearch.discovery.Discovery.AckListener} to notify for each response      *                    gotten from non master nodes      */
DECL|method|AckClusterStatePublishResponseHandler
specifier|public
name|AckClusterStatePublishResponseHandler
parameter_list|(
name|Set
argument_list|<
name|DiscoveryNode
argument_list|>
name|publishingToNodes
parameter_list|,
name|Discovery
operator|.
name|AckListener
name|ackListener
parameter_list|)
block|{
comment|//Don't count the master as acknowledged, because it's not done yet
comment|//otherwise we might end up with all the nodes but the master holding the latest cluster state
name|super
argument_list|(
name|publishingToNodes
argument_list|)
expr_stmt|;
name|this
operator|.
name|ackListener
operator|=
name|ackListener
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|onResponse
specifier|public
name|void
name|onResponse
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|)
block|{
name|super
operator|.
name|onResponse
argument_list|(
name|node
argument_list|)
expr_stmt|;
name|onNodeAck
argument_list|(
name|ackListener
argument_list|,
name|node
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|onFailure
specifier|public
name|void
name|onFailure
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|,
name|Exception
name|e
parameter_list|)
block|{
try|try
block|{
name|super
operator|.
name|onFailure
argument_list|(
name|node
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|onNodeAck
argument_list|(
name|ackListener
argument_list|,
name|node
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|onNodeAck
specifier|private
name|void
name|onNodeAck
parameter_list|(
specifier|final
name|Discovery
operator|.
name|AckListener
name|ackListener
parameter_list|,
name|DiscoveryNode
name|node
parameter_list|,
name|Exception
name|e
parameter_list|)
block|{
try|try
block|{
name|ackListener
operator|.
name|onNodeAck
argument_list|(
name|node
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|inner
parameter_list|)
block|{
name|inner
operator|.
name|addSuppressed
argument_list|(
name|e
argument_list|)
expr_stmt|;
name|logger
operator|.
name|debug
argument_list|(
call|(
name|Supplier
argument_list|<
name|?
argument_list|>
call|)
argument_list|()
operator|->
operator|new
name|ParameterizedMessage
argument_list|(
literal|"error while processing ack for node [{}]"
argument_list|,
name|node
argument_list|)
argument_list|,
name|inner
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

