begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Maps
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
name|admin
operator|.
name|cluster
operator|.
name|health
operator|.
name|ClusterHealthAction
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
name|admin
operator|.
name|cluster
operator|.
name|health
operator|.
name|TransportClusterHealthAction
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
name|admin
operator|.
name|cluster
operator|.
name|node
operator|.
name|hotthreads
operator|.
name|NodesHotThreadsAction
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
name|admin
operator|.
name|cluster
operator|.
name|node
operator|.
name|hotthreads
operator|.
name|TransportNodesHotThreadsAction
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
name|admin
operator|.
name|cluster
operator|.
name|node
operator|.
name|info
operator|.
name|NodesInfoAction
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
name|admin
operator|.
name|cluster
operator|.
name|node
operator|.
name|info
operator|.
name|TransportNodesInfoAction
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
name|admin
operator|.
name|cluster
operator|.
name|node
operator|.
name|restart
operator|.
name|NodesRestartAction
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
name|admin
operator|.
name|cluster
operator|.
name|node
operator|.
name|restart
operator|.
name|TransportNodesRestartAction
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
name|admin
operator|.
name|cluster
operator|.
name|node
operator|.
name|shutdown
operator|.
name|NodesShutdownAction
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
name|admin
operator|.
name|cluster
operator|.
name|node
operator|.
name|shutdown
operator|.
name|TransportNodesShutdownAction
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
name|admin
operator|.
name|cluster
operator|.
name|node
operator|.
name|stats
operator|.
name|NodesStatsAction
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
name|admin
operator|.
name|cluster
operator|.
name|node
operator|.
name|stats
operator|.
name|TransportNodesStatsAction
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
name|admin
operator|.
name|cluster
operator|.
name|reroute
operator|.
name|ClusterRerouteAction
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
name|admin
operator|.
name|cluster
operator|.
name|reroute
operator|.
name|TransportClusterRerouteAction
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
name|admin
operator|.
name|cluster
operator|.
name|settings
operator|.
name|ClusterUpdateSettingsAction
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
name|admin
operator|.
name|cluster
operator|.
name|settings
operator|.
name|TransportClusterUpdateSettingsAction
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
name|admin
operator|.
name|cluster
operator|.
name|shards
operator|.
name|ClusterSearchShardsAction
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
name|admin
operator|.
name|cluster
operator|.
name|shards
operator|.
name|TransportClusterSearchShardsAction
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
name|admin
operator|.
name|cluster
operator|.
name|state
operator|.
name|ClusterStateAction
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
name|admin
operator|.
name|cluster
operator|.
name|state
operator|.
name|TransportClusterStateAction
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
name|admin
operator|.
name|indices
operator|.
name|alias
operator|.
name|IndicesAliasesAction
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
name|admin
operator|.
name|indices
operator|.
name|alias
operator|.
name|TransportIndicesAliasesAction
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
name|admin
operator|.
name|indices
operator|.
name|analyze
operator|.
name|AnalyzeAction
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
name|admin
operator|.
name|indices
operator|.
name|analyze
operator|.
name|TransportAnalyzeAction
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
name|admin
operator|.
name|indices
operator|.
name|cache
operator|.
name|clear
operator|.
name|ClearIndicesCacheAction
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
name|admin
operator|.
name|indices
operator|.
name|cache
operator|.
name|clear
operator|.
name|TransportClearIndicesCacheAction
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
name|admin
operator|.
name|indices
operator|.
name|close
operator|.
name|CloseIndexAction
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
name|admin
operator|.
name|indices
operator|.
name|close
operator|.
name|TransportCloseIndexAction
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
name|admin
operator|.
name|indices
operator|.
name|create
operator|.
name|CreateIndexAction
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
name|admin
operator|.
name|indices
operator|.
name|create
operator|.
name|TransportCreateIndexAction
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
name|admin
operator|.
name|indices
operator|.
name|delete
operator|.
name|DeleteIndexAction
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
name|admin
operator|.
name|indices
operator|.
name|delete
operator|.
name|TransportDeleteIndexAction
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
name|admin
operator|.
name|indices
operator|.
name|exists
operator|.
name|indices
operator|.
name|IndicesExistsAction
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
name|admin
operator|.
name|indices
operator|.
name|exists
operator|.
name|indices
operator|.
name|TransportIndicesExistsAction
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
name|admin
operator|.
name|indices
operator|.
name|exists
operator|.
name|types
operator|.
name|TransportTypesExistsAction
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
name|admin
operator|.
name|indices
operator|.
name|exists
operator|.
name|types
operator|.
name|TypesExistsAction
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
name|admin
operator|.
name|indices
operator|.
name|flush
operator|.
name|FlushAction
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
name|admin
operator|.
name|indices
operator|.
name|flush
operator|.
name|TransportFlushAction
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
name|admin
operator|.
name|indices
operator|.
name|gateway
operator|.
name|snapshot
operator|.
name|GatewaySnapshotAction
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
name|admin
operator|.
name|indices
operator|.
name|gateway
operator|.
name|snapshot
operator|.
name|TransportGatewaySnapshotAction
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
name|admin
operator|.
name|indices
operator|.
name|mapping
operator|.
name|delete
operator|.
name|DeleteMappingAction
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
name|admin
operator|.
name|indices
operator|.
name|mapping
operator|.
name|delete
operator|.
name|TransportDeleteMappingAction
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
name|admin
operator|.
name|indices
operator|.
name|mapping
operator|.
name|put
operator|.
name|PutMappingAction
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
name|admin
operator|.
name|indices
operator|.
name|mapping
operator|.
name|put
operator|.
name|TransportPutMappingAction
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
name|admin
operator|.
name|indices
operator|.
name|open
operator|.
name|OpenIndexAction
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
name|admin
operator|.
name|indices
operator|.
name|open
operator|.
name|TransportOpenIndexAction
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
name|admin
operator|.
name|indices
operator|.
name|optimize
operator|.
name|OptimizeAction
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
name|admin
operator|.
name|indices
operator|.
name|optimize
operator|.
name|TransportOptimizeAction
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
name|admin
operator|.
name|indices
operator|.
name|refresh
operator|.
name|RefreshAction
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
name|admin
operator|.
name|indices
operator|.
name|refresh
operator|.
name|TransportRefreshAction
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
name|admin
operator|.
name|indices
operator|.
name|segments
operator|.
name|IndicesSegmentsAction
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
name|admin
operator|.
name|indices
operator|.
name|segments
operator|.
name|TransportIndicesSegmentsAction
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
name|admin
operator|.
name|indices
operator|.
name|settings
operator|.
name|TransportUpdateSettingsAction
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
name|admin
operator|.
name|indices
operator|.
name|settings
operator|.
name|UpdateSettingsAction
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
name|admin
operator|.
name|indices
operator|.
name|stats
operator|.
name|IndicesStatsAction
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
name|admin
operator|.
name|indices
operator|.
name|stats
operator|.
name|TransportIndicesStatsAction
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
name|admin
operator|.
name|indices
operator|.
name|status
operator|.
name|IndicesStatusAction
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
name|admin
operator|.
name|indices
operator|.
name|status
operator|.
name|TransportIndicesStatusAction
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
name|admin
operator|.
name|indices
operator|.
name|template
operator|.
name|delete
operator|.
name|DeleteIndexTemplateAction
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
name|admin
operator|.
name|indices
operator|.
name|template
operator|.
name|delete
operator|.
name|TransportDeleteIndexTemplateAction
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
name|admin
operator|.
name|indices
operator|.
name|template
operator|.
name|put
operator|.
name|PutIndexTemplateAction
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
name|admin
operator|.
name|indices
operator|.
name|template
operator|.
name|put
operator|.
name|TransportPutIndexTemplateAction
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
name|admin
operator|.
name|indices
operator|.
name|validate
operator|.
name|query
operator|.
name|TransportValidateQueryAction
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
name|admin
operator|.
name|indices
operator|.
name|validate
operator|.
name|query
operator|.
name|ValidateQueryAction
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
name|admin
operator|.
name|indices
operator|.
name|warmer
operator|.
name|delete
operator|.
name|DeleteWarmerAction
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
name|admin
operator|.
name|indices
operator|.
name|warmer
operator|.
name|delete
operator|.
name|TransportDeleteWarmerAction
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
name|admin
operator|.
name|indices
operator|.
name|warmer
operator|.
name|put
operator|.
name|PutWarmerAction
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
name|admin
operator|.
name|indices
operator|.
name|warmer
operator|.
name|put
operator|.
name|TransportPutWarmerAction
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
name|bulk
operator|.
name|BulkAction
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
name|bulk
operator|.
name|TransportBulkAction
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
name|bulk
operator|.
name|TransportShardBulkAction
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
name|count
operator|.
name|CountAction
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
name|count
operator|.
name|TransportCountAction
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
name|DeleteAction
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
name|TransportDeleteAction
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
name|index
operator|.
name|TransportIndexDeleteAction
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
name|index
operator|.
name|TransportShardDeleteAction
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
name|deletebyquery
operator|.
name|DeleteByQueryAction
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
name|deletebyquery
operator|.
name|TransportDeleteByQueryAction
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
name|deletebyquery
operator|.
name|TransportIndexDeleteByQueryAction
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
name|deletebyquery
operator|.
name|TransportShardDeleteByQueryAction
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
name|explain
operator|.
name|ExplainAction
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
name|explain
operator|.
name|TransportExplainAction
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
name|get
operator|.
name|*
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
name|index
operator|.
name|IndexAction
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
name|index
operator|.
name|TransportIndexAction
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
name|mlt
operator|.
name|MoreLikeThisAction
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
name|mlt
operator|.
name|TransportMoreLikeThisAction
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
name|percolate
operator|.
name|PercolateAction
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
name|percolate
operator|.
name|TransportPercolateAction
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
name|search
operator|.
name|*
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
name|search
operator|.
name|type
operator|.
name|*
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
name|TransportAction
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
name|update
operator|.
name|TransportUpdateAction
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
name|update
operator|.
name|UpdateAction
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
name|AbstractModule
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
name|multibindings
operator|.
name|MapBinder
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

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|ActionModule
specifier|public
class|class
name|ActionModule
extends|extends
name|AbstractModule
block|{
DECL|field|actions
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|ActionEntry
argument_list|>
name|actions
init|=
name|Maps
operator|.
name|newHashMap
argument_list|()
decl_stmt|;
DECL|class|ActionEntry
specifier|static
class|class
name|ActionEntry
parameter_list|<
name|Request
extends|extends
name|ActionRequest
parameter_list|,
name|Response
extends|extends
name|ActionResponse
parameter_list|>
block|{
DECL|field|action
specifier|public
specifier|final
name|GenericAction
argument_list|<
name|Request
argument_list|,
name|Response
argument_list|>
name|action
decl_stmt|;
DECL|field|transportAction
specifier|public
specifier|final
name|Class
argument_list|<
name|?
extends|extends
name|TransportAction
argument_list|<
name|Request
argument_list|,
name|Response
argument_list|>
argument_list|>
name|transportAction
decl_stmt|;
DECL|field|supportTransportActions
specifier|public
specifier|final
name|Class
index|[]
name|supportTransportActions
decl_stmt|;
DECL|method|ActionEntry
name|ActionEntry
parameter_list|(
name|GenericAction
argument_list|<
name|Request
argument_list|,
name|Response
argument_list|>
name|action
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|TransportAction
argument_list|<
name|Request
argument_list|,
name|Response
argument_list|>
argument_list|>
name|transportAction
parameter_list|,
name|Class
modifier|...
name|supportTransportActions
parameter_list|)
block|{
name|this
operator|.
name|action
operator|=
name|action
expr_stmt|;
name|this
operator|.
name|transportAction
operator|=
name|transportAction
expr_stmt|;
name|this
operator|.
name|supportTransportActions
operator|=
name|supportTransportActions
expr_stmt|;
block|}
block|}
DECL|field|proxy
specifier|private
specifier|final
name|boolean
name|proxy
decl_stmt|;
DECL|method|ActionModule
specifier|public
name|ActionModule
parameter_list|(
name|boolean
name|proxy
parameter_list|)
block|{
name|this
operator|.
name|proxy
operator|=
name|proxy
expr_stmt|;
block|}
comment|/**      * Registers an action.      *      * @param action                  The action type.      * @param transportAction         The transport action implementing the actual action.      * @param supportTransportActions Any support actions that are needed by the transport action.      * @param<Request>               The request type.      * @param<Response>              The response type.      */
DECL|method|registerAction
specifier|public
parameter_list|<
name|Request
extends|extends
name|ActionRequest
parameter_list|,
name|Response
extends|extends
name|ActionResponse
parameter_list|>
name|void
name|registerAction
parameter_list|(
name|GenericAction
argument_list|<
name|Request
argument_list|,
name|Response
argument_list|>
name|action
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|TransportAction
argument_list|<
name|Request
argument_list|,
name|Response
argument_list|>
argument_list|>
name|transportAction
parameter_list|,
name|Class
modifier|...
name|supportTransportActions
parameter_list|)
block|{
name|actions
operator|.
name|put
argument_list|(
name|action
operator|.
name|name
argument_list|()
argument_list|,
operator|new
name|ActionEntry
argument_list|<
name|Request
argument_list|,
name|Response
argument_list|>
argument_list|(
name|action
argument_list|,
name|transportAction
argument_list|,
name|supportTransportActions
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|configure
specifier|protected
name|void
name|configure
parameter_list|()
block|{
name|registerAction
argument_list|(
name|NodesInfoAction
operator|.
name|INSTANCE
argument_list|,
name|TransportNodesInfoAction
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerAction
argument_list|(
name|NodesStatsAction
operator|.
name|INSTANCE
argument_list|,
name|TransportNodesStatsAction
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerAction
argument_list|(
name|NodesShutdownAction
operator|.
name|INSTANCE
argument_list|,
name|TransportNodesShutdownAction
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerAction
argument_list|(
name|NodesRestartAction
operator|.
name|INSTANCE
argument_list|,
name|TransportNodesRestartAction
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerAction
argument_list|(
name|NodesHotThreadsAction
operator|.
name|INSTANCE
argument_list|,
name|TransportNodesHotThreadsAction
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerAction
argument_list|(
name|ClusterStateAction
operator|.
name|INSTANCE
argument_list|,
name|TransportClusterStateAction
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerAction
argument_list|(
name|ClusterHealthAction
operator|.
name|INSTANCE
argument_list|,
name|TransportClusterHealthAction
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerAction
argument_list|(
name|ClusterUpdateSettingsAction
operator|.
name|INSTANCE
argument_list|,
name|TransportClusterUpdateSettingsAction
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerAction
argument_list|(
name|ClusterRerouteAction
operator|.
name|INSTANCE
argument_list|,
name|TransportClusterRerouteAction
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerAction
argument_list|(
name|ClusterSearchShardsAction
operator|.
name|INSTANCE
argument_list|,
name|TransportClusterSearchShardsAction
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerAction
argument_list|(
name|IndicesStatsAction
operator|.
name|INSTANCE
argument_list|,
name|TransportIndicesStatsAction
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerAction
argument_list|(
name|IndicesStatusAction
operator|.
name|INSTANCE
argument_list|,
name|TransportIndicesStatusAction
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerAction
argument_list|(
name|IndicesSegmentsAction
operator|.
name|INSTANCE
argument_list|,
name|TransportIndicesSegmentsAction
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerAction
argument_list|(
name|CreateIndexAction
operator|.
name|INSTANCE
argument_list|,
name|TransportCreateIndexAction
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerAction
argument_list|(
name|DeleteIndexAction
operator|.
name|INSTANCE
argument_list|,
name|TransportDeleteIndexAction
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerAction
argument_list|(
name|OpenIndexAction
operator|.
name|INSTANCE
argument_list|,
name|TransportOpenIndexAction
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerAction
argument_list|(
name|CloseIndexAction
operator|.
name|INSTANCE
argument_list|,
name|TransportCloseIndexAction
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerAction
argument_list|(
name|IndicesExistsAction
operator|.
name|INSTANCE
argument_list|,
name|TransportIndicesExistsAction
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerAction
argument_list|(
name|TypesExistsAction
operator|.
name|INSTANCE
argument_list|,
name|TransportTypesExistsAction
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerAction
argument_list|(
name|PutMappingAction
operator|.
name|INSTANCE
argument_list|,
name|TransportPutMappingAction
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerAction
argument_list|(
name|DeleteMappingAction
operator|.
name|INSTANCE
argument_list|,
name|TransportDeleteMappingAction
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerAction
argument_list|(
name|IndicesAliasesAction
operator|.
name|INSTANCE
argument_list|,
name|TransportIndicesAliasesAction
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerAction
argument_list|(
name|UpdateSettingsAction
operator|.
name|INSTANCE
argument_list|,
name|TransportUpdateSettingsAction
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerAction
argument_list|(
name|AnalyzeAction
operator|.
name|INSTANCE
argument_list|,
name|TransportAnalyzeAction
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerAction
argument_list|(
name|PutIndexTemplateAction
operator|.
name|INSTANCE
argument_list|,
name|TransportPutIndexTemplateAction
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerAction
argument_list|(
name|DeleteIndexTemplateAction
operator|.
name|INSTANCE
argument_list|,
name|TransportDeleteIndexTemplateAction
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerAction
argument_list|(
name|ValidateQueryAction
operator|.
name|INSTANCE
argument_list|,
name|TransportValidateQueryAction
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerAction
argument_list|(
name|GatewaySnapshotAction
operator|.
name|INSTANCE
argument_list|,
name|TransportGatewaySnapshotAction
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerAction
argument_list|(
name|RefreshAction
operator|.
name|INSTANCE
argument_list|,
name|TransportRefreshAction
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerAction
argument_list|(
name|FlushAction
operator|.
name|INSTANCE
argument_list|,
name|TransportFlushAction
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerAction
argument_list|(
name|OptimizeAction
operator|.
name|INSTANCE
argument_list|,
name|TransportOptimizeAction
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerAction
argument_list|(
name|ClearIndicesCacheAction
operator|.
name|INSTANCE
argument_list|,
name|TransportClearIndicesCacheAction
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerAction
argument_list|(
name|PutWarmerAction
operator|.
name|INSTANCE
argument_list|,
name|TransportPutWarmerAction
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerAction
argument_list|(
name|DeleteWarmerAction
operator|.
name|INSTANCE
argument_list|,
name|TransportDeleteWarmerAction
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerAction
argument_list|(
name|IndexAction
operator|.
name|INSTANCE
argument_list|,
name|TransportIndexAction
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerAction
argument_list|(
name|GetAction
operator|.
name|INSTANCE
argument_list|,
name|TransportGetAction
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerAction
argument_list|(
name|DeleteAction
operator|.
name|INSTANCE
argument_list|,
name|TransportDeleteAction
operator|.
name|class
argument_list|,
name|TransportIndexDeleteAction
operator|.
name|class
argument_list|,
name|TransportShardDeleteAction
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerAction
argument_list|(
name|CountAction
operator|.
name|INSTANCE
argument_list|,
name|TransportCountAction
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerAction
argument_list|(
name|UpdateAction
operator|.
name|INSTANCE
argument_list|,
name|TransportUpdateAction
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerAction
argument_list|(
name|MultiGetAction
operator|.
name|INSTANCE
argument_list|,
name|TransportMultiGetAction
operator|.
name|class
argument_list|,
name|TransportShardMultiGetAction
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerAction
argument_list|(
name|BulkAction
operator|.
name|INSTANCE
argument_list|,
name|TransportBulkAction
operator|.
name|class
argument_list|,
name|TransportShardBulkAction
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerAction
argument_list|(
name|DeleteByQueryAction
operator|.
name|INSTANCE
argument_list|,
name|TransportDeleteByQueryAction
operator|.
name|class
argument_list|,
name|TransportIndexDeleteByQueryAction
operator|.
name|class
argument_list|,
name|TransportShardDeleteByQueryAction
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerAction
argument_list|(
name|SearchAction
operator|.
name|INSTANCE
argument_list|,
name|TransportSearchAction
operator|.
name|class
argument_list|,
name|TransportSearchCache
operator|.
name|class
argument_list|,
name|TransportSearchDfsQueryThenFetchAction
operator|.
name|class
argument_list|,
name|TransportSearchQueryThenFetchAction
operator|.
name|class
argument_list|,
name|TransportSearchDfsQueryAndFetchAction
operator|.
name|class
argument_list|,
name|TransportSearchQueryAndFetchAction
operator|.
name|class
argument_list|,
name|TransportSearchScanAction
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerAction
argument_list|(
name|SearchScrollAction
operator|.
name|INSTANCE
argument_list|,
name|TransportSearchScrollAction
operator|.
name|class
argument_list|,
name|TransportSearchScrollScanAction
operator|.
name|class
argument_list|,
name|TransportSearchScrollQueryThenFetchAction
operator|.
name|class
argument_list|,
name|TransportSearchScrollQueryAndFetchAction
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerAction
argument_list|(
name|MultiSearchAction
operator|.
name|INSTANCE
argument_list|,
name|TransportMultiSearchAction
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerAction
argument_list|(
name|MoreLikeThisAction
operator|.
name|INSTANCE
argument_list|,
name|TransportMoreLikeThisAction
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerAction
argument_list|(
name|PercolateAction
operator|.
name|INSTANCE
argument_list|,
name|TransportPercolateAction
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerAction
argument_list|(
name|ExplainAction
operator|.
name|INSTANCE
argument_list|,
name|TransportExplainAction
operator|.
name|class
argument_list|)
expr_stmt|;
comment|// register Name -> GenericAction Map that can be injected to instances.
name|MapBinder
argument_list|<
name|String
argument_list|,
name|GenericAction
argument_list|>
name|actionsBinder
init|=
name|MapBinder
operator|.
name|newMapBinder
argument_list|(
name|binder
argument_list|()
argument_list|,
name|String
operator|.
name|class
argument_list|,
name|GenericAction
operator|.
name|class
argument_list|)
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|ActionEntry
argument_list|>
name|entry
range|:
name|actions
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|actionsBinder
operator|.
name|addBinding
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
operator|.
name|toInstance
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|action
argument_list|)
expr_stmt|;
block|}
comment|// register GenericAction -> transportAction Map that can be injected to instances.
comment|// also register any supporting classes
if|if
condition|(
operator|!
name|proxy
condition|)
block|{
name|MapBinder
argument_list|<
name|GenericAction
argument_list|,
name|TransportAction
argument_list|>
name|transportActionsBinder
init|=
name|MapBinder
operator|.
name|newMapBinder
argument_list|(
name|binder
argument_list|()
argument_list|,
name|GenericAction
operator|.
name|class
argument_list|,
name|TransportAction
operator|.
name|class
argument_list|)
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|ActionEntry
argument_list|>
name|entry
range|:
name|actions
operator|.
name|entrySet
argument_list|()
control|)
block|{
comment|// bind the action as eager singleton, so the map binder one will reuse it
name|bind
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|transportAction
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|transportActionsBinder
operator|.
name|addBinding
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|action
argument_list|)
operator|.
name|to
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|transportAction
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
for|for
control|(
name|Class
name|supportAction
range|:
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|supportTransportActions
control|)
block|{
name|bind
argument_list|(
name|supportAction
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
end_class

end_unit

