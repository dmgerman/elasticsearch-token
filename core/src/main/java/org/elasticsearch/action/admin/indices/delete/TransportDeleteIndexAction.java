begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.indices.delete
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|admin
operator|.
name|indices
operator|.
name|delete
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
name|support
operator|.
name|ActionFilters
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
name|DestructiveOperations
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
name|master
operator|.
name|TransportMasterNodeAction
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
name|ClusterState
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
name|ack
operator|.
name|ClusterStateUpdateResponse
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
name|block
operator|.
name|ClusterBlockException
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
name|block
operator|.
name|ClusterBlockLevel
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
name|metadata
operator|.
name|IndexNameExpressionResolver
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
name|metadata
operator|.
name|MetaDataDeleteIndexService
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
name|service
operator|.
name|ClusterService
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
name|inject
operator|.
name|Inject
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
name|settings
operator|.
name|Settings
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|Index
import|;
end_import

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

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|transport
operator|.
name|TransportService
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
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
comment|/**  * Delete index action.  */
end_comment

begin_class
DECL|class|TransportDeleteIndexAction
specifier|public
class|class
name|TransportDeleteIndexAction
extends|extends
name|TransportMasterNodeAction
argument_list|<
name|DeleteIndexRequest
argument_list|,
name|DeleteIndexResponse
argument_list|>
block|{
DECL|field|deleteIndexService
specifier|private
specifier|final
name|MetaDataDeleteIndexService
name|deleteIndexService
decl_stmt|;
DECL|field|destructiveOperations
specifier|private
specifier|final
name|DestructiveOperations
name|destructiveOperations
decl_stmt|;
annotation|@
name|Inject
DECL|method|TransportDeleteIndexAction
specifier|public
name|TransportDeleteIndexAction
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|TransportService
name|transportService
parameter_list|,
name|ClusterService
name|clusterService
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|,
name|MetaDataDeleteIndexService
name|deleteIndexService
parameter_list|,
name|ActionFilters
name|actionFilters
parameter_list|,
name|IndexNameExpressionResolver
name|indexNameExpressionResolver
parameter_list|,
name|DestructiveOperations
name|destructiveOperations
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|DeleteIndexAction
operator|.
name|NAME
argument_list|,
name|transportService
argument_list|,
name|clusterService
argument_list|,
name|threadPool
argument_list|,
name|actionFilters
argument_list|,
name|indexNameExpressionResolver
argument_list|,
name|DeleteIndexRequest
operator|::
operator|new
argument_list|)
expr_stmt|;
name|this
operator|.
name|deleteIndexService
operator|=
name|deleteIndexService
expr_stmt|;
name|this
operator|.
name|destructiveOperations
operator|=
name|destructiveOperations
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|executor
specifier|protected
name|String
name|executor
parameter_list|()
block|{
return|return
name|ThreadPool
operator|.
name|Names
operator|.
name|SAME
return|;
block|}
annotation|@
name|Override
DECL|method|newResponse
specifier|protected
name|DeleteIndexResponse
name|newResponse
parameter_list|()
block|{
return|return
operator|new
name|DeleteIndexResponse
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|doExecute
specifier|protected
name|void
name|doExecute
parameter_list|(
name|Task
name|task
parameter_list|,
name|DeleteIndexRequest
name|request
parameter_list|,
name|ActionListener
argument_list|<
name|DeleteIndexResponse
argument_list|>
name|listener
parameter_list|)
block|{
name|destructiveOperations
operator|.
name|failDestructive
argument_list|(
name|request
operator|.
name|indices
argument_list|()
argument_list|)
expr_stmt|;
name|super
operator|.
name|doExecute
argument_list|(
name|task
argument_list|,
name|request
argument_list|,
name|listener
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|checkBlock
specifier|protected
name|ClusterBlockException
name|checkBlock
parameter_list|(
name|DeleteIndexRequest
name|request
parameter_list|,
name|ClusterState
name|state
parameter_list|)
block|{
return|return
name|state
operator|.
name|blocks
argument_list|()
operator|.
name|indicesBlockedException
argument_list|(
name|ClusterBlockLevel
operator|.
name|METADATA_WRITE
argument_list|,
name|indexNameExpressionResolver
operator|.
name|concreteIndexNames
argument_list|(
name|state
argument_list|,
name|request
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|masterOperation
specifier|protected
name|void
name|masterOperation
parameter_list|(
specifier|final
name|DeleteIndexRequest
name|request
parameter_list|,
specifier|final
name|ClusterState
name|state
parameter_list|,
specifier|final
name|ActionListener
argument_list|<
name|DeleteIndexResponse
argument_list|>
name|listener
parameter_list|)
block|{
specifier|final
name|Set
argument_list|<
name|Index
argument_list|>
name|concreteIndices
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|indexNameExpressionResolver
operator|.
name|concreteIndices
argument_list|(
name|state
argument_list|,
name|request
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|concreteIndices
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|listener
operator|.
name|onResponse
argument_list|(
operator|new
name|DeleteIndexResponse
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
return|return;
block|}
name|DeleteIndexClusterStateUpdateRequest
name|deleteRequest
init|=
operator|new
name|DeleteIndexClusterStateUpdateRequest
argument_list|()
operator|.
name|ackTimeout
argument_list|(
name|request
operator|.
name|timeout
argument_list|()
argument_list|)
operator|.
name|masterNodeTimeout
argument_list|(
name|request
operator|.
name|masterNodeTimeout
argument_list|()
argument_list|)
operator|.
name|indices
argument_list|(
name|concreteIndices
operator|.
name|toArray
argument_list|(
operator|new
name|Index
index|[
name|concreteIndices
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|)
decl_stmt|;
name|deleteIndexService
operator|.
name|deleteIndices
argument_list|(
name|deleteRequest
argument_list|,
operator|new
name|ActionListener
argument_list|<
name|ClusterStateUpdateResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onResponse
parameter_list|(
name|ClusterStateUpdateResponse
name|response
parameter_list|)
block|{
name|listener
operator|.
name|onResponse
argument_list|(
operator|new
name|DeleteIndexResponse
argument_list|(
name|response
operator|.
name|isAcknowledged
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|onFailure
parameter_list|(
name|Exception
name|t
parameter_list|)
block|{
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
literal|"failed to delete indices [{}]"
argument_list|,
name|concreteIndices
argument_list|)
argument_list|,
name|t
argument_list|)
expr_stmt|;
name|listener
operator|.
name|onFailure
argument_list|(
name|t
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

