begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.reindex
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|reindex
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
name|delete
operator|.
name|DeleteRequest
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
name|HandledTransportAction
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
name|ParentTaskAssigningClient
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
name|script
operator|.
name|ScriptService
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

begin_class
DECL|class|TransportDeleteByQueryAction
specifier|public
class|class
name|TransportDeleteByQueryAction
extends|extends
name|HandledTransportAction
argument_list|<
name|DeleteByQueryRequest
argument_list|,
name|BulkIndexByScrollResponse
argument_list|>
block|{
DECL|field|client
specifier|private
specifier|final
name|Client
name|client
decl_stmt|;
DECL|field|scriptService
specifier|private
specifier|final
name|ScriptService
name|scriptService
decl_stmt|;
DECL|field|clusterService
specifier|private
specifier|final
name|ClusterService
name|clusterService
decl_stmt|;
annotation|@
name|Inject
DECL|method|TransportDeleteByQueryAction
specifier|public
name|TransportDeleteByQueryAction
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|,
name|ActionFilters
name|actionFilters
parameter_list|,
name|IndexNameExpressionResolver
name|resolver
parameter_list|,
name|Client
name|client
parameter_list|,
name|TransportService
name|transportService
parameter_list|,
name|ScriptService
name|scriptService
parameter_list|,
name|ClusterService
name|clusterService
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|DeleteByQueryAction
operator|.
name|NAME
argument_list|,
name|threadPool
argument_list|,
name|transportService
argument_list|,
name|actionFilters
argument_list|,
name|resolver
argument_list|,
name|DeleteByQueryRequest
operator|::
operator|new
argument_list|)
expr_stmt|;
name|this
operator|.
name|client
operator|=
name|client
expr_stmt|;
name|this
operator|.
name|scriptService
operator|=
name|scriptService
expr_stmt|;
name|this
operator|.
name|clusterService
operator|=
name|clusterService
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doExecute
specifier|public
name|void
name|doExecute
parameter_list|(
name|Task
name|task
parameter_list|,
name|DeleteByQueryRequest
name|request
parameter_list|,
name|ActionListener
argument_list|<
name|BulkIndexByScrollResponse
argument_list|>
name|listener
parameter_list|)
block|{
if|if
condition|(
name|request
operator|.
name|getSlices
argument_list|()
operator|>
literal|1
condition|)
block|{
name|ReindexParallelizationHelper
operator|.
name|startSlices
argument_list|(
name|client
argument_list|,
name|taskManager
argument_list|,
name|DeleteByQueryAction
operator|.
name|INSTANCE
argument_list|,
name|clusterService
operator|.
name|localNode
argument_list|()
operator|.
name|getId
argument_list|()
argument_list|,
operator|(
name|ParentBulkByScrollTask
operator|)
name|task
argument_list|,
name|request
argument_list|,
name|listener
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|ClusterState
name|state
init|=
name|clusterService
operator|.
name|state
argument_list|()
decl_stmt|;
name|ParentTaskAssigningClient
name|client
init|=
operator|new
name|ParentTaskAssigningClient
argument_list|(
name|this
operator|.
name|client
argument_list|,
name|clusterService
operator|.
name|localNode
argument_list|()
argument_list|,
name|task
argument_list|)
decl_stmt|;
operator|new
name|AsyncDeleteBySearchAction
argument_list|(
operator|(
name|WorkingBulkByScrollTask
operator|)
name|task
argument_list|,
name|logger
argument_list|,
name|client
argument_list|,
name|threadPool
argument_list|,
name|request
argument_list|,
name|scriptService
argument_list|,
name|state
argument_list|,
name|listener
argument_list|)
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|doExecute
specifier|protected
name|void
name|doExecute
parameter_list|(
name|DeleteByQueryRequest
name|request
parameter_list|,
name|ActionListener
argument_list|<
name|BulkIndexByScrollResponse
argument_list|>
name|listener
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"task required"
argument_list|)
throw|;
block|}
comment|/**      * Implementation of delete-by-query using scrolling and bulk.      */
DECL|class|AsyncDeleteBySearchAction
specifier|static
class|class
name|AsyncDeleteBySearchAction
extends|extends
name|AbstractAsyncBulkByScrollAction
argument_list|<
name|DeleteByQueryRequest
argument_list|>
block|{
DECL|method|AsyncDeleteBySearchAction
specifier|public
name|AsyncDeleteBySearchAction
parameter_list|(
name|WorkingBulkByScrollTask
name|task
parameter_list|,
name|Logger
name|logger
parameter_list|,
name|ParentTaskAssigningClient
name|client
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|,
name|DeleteByQueryRequest
name|request
parameter_list|,
name|ScriptService
name|scriptService
parameter_list|,
name|ClusterState
name|clusterState
parameter_list|,
name|ActionListener
argument_list|<
name|BulkIndexByScrollResponse
argument_list|>
name|listener
parameter_list|)
block|{
name|super
argument_list|(
name|task
argument_list|,
name|logger
argument_list|,
name|client
argument_list|,
name|threadPool
argument_list|,
name|request
argument_list|,
name|scriptService
argument_list|,
name|clusterState
argument_list|,
name|listener
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|needsSourceDocumentVersions
specifier|protected
name|boolean
name|needsSourceDocumentVersions
parameter_list|()
block|{
comment|/*              * We always need the version of the source document so we can report a version conflict if we try to delete it and it has been              * changed.              */
return|return
literal|true
return|;
block|}
annotation|@
name|Override
DECL|method|accept
specifier|protected
name|boolean
name|accept
parameter_list|(
name|ScrollableHitSource
operator|.
name|Hit
name|doc
parameter_list|)
block|{
comment|// Delete-by-query does not require the source to delete a document
comment|// and the default implementation checks for it
return|return
literal|true
return|;
block|}
annotation|@
name|Override
DECL|method|buildRequest
specifier|protected
name|RequestWrapper
argument_list|<
name|DeleteRequest
argument_list|>
name|buildRequest
parameter_list|(
name|ScrollableHitSource
operator|.
name|Hit
name|doc
parameter_list|)
block|{
name|DeleteRequest
name|delete
init|=
operator|new
name|DeleteRequest
argument_list|()
decl_stmt|;
name|delete
operator|.
name|index
argument_list|(
name|doc
operator|.
name|getIndex
argument_list|()
argument_list|)
expr_stmt|;
name|delete
operator|.
name|type
argument_list|(
name|doc
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|delete
operator|.
name|id
argument_list|(
name|doc
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
name|delete
operator|.
name|version
argument_list|(
name|doc
operator|.
name|getVersion
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|wrap
argument_list|(
name|delete
argument_list|)
return|;
block|}
comment|/**          * Overrides the parent's implementation is much more Update/Reindex oriented and so also copies things like timestamp/ttl which we          * don't care for a deletion.          */
annotation|@
name|Override
DECL|method|copyMetadata
specifier|protected
name|RequestWrapper
argument_list|<
name|?
argument_list|>
name|copyMetadata
parameter_list|(
name|RequestWrapper
argument_list|<
name|?
argument_list|>
name|request
parameter_list|,
name|ScrollableHitSource
operator|.
name|Hit
name|doc
parameter_list|)
block|{
name|request
operator|.
name|setParent
argument_list|(
name|doc
operator|.
name|getParent
argument_list|()
argument_list|)
expr_stmt|;
name|request
operator|.
name|setRouting
argument_list|(
name|doc
operator|.
name|getRouting
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|request
return|;
block|}
block|}
block|}
end_class

end_unit

