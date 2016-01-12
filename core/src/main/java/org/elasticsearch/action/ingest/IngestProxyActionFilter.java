begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.ingest
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|ingest
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
name|Action
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
name|ActionRequest
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
name|BulkRequest
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
name|IndexRequest
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
name|ActionFilter
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
name|ActionFilterChain
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
name|ClusterService
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
name|Randomness
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
name|Strings
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
name|ingest
operator|.
name|IngestModule
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
name|TransportException
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
name|TransportResponse
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
name|TransportResponseHandler
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
name|ArrayList
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
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicInteger
import|;
end_import

begin_class
DECL|class|IngestProxyActionFilter
specifier|public
specifier|final
class|class
name|IngestProxyActionFilter
implements|implements
name|ActionFilter
block|{
DECL|field|clusterService
specifier|private
specifier|final
name|ClusterService
name|clusterService
decl_stmt|;
DECL|field|transportService
specifier|private
specifier|final
name|TransportService
name|transportService
decl_stmt|;
DECL|field|randomNodeGenerator
specifier|private
specifier|final
name|AtomicInteger
name|randomNodeGenerator
init|=
operator|new
name|AtomicInteger
argument_list|(
name|Randomness
operator|.
name|get
argument_list|()
operator|.
name|nextInt
argument_list|()
argument_list|)
decl_stmt|;
annotation|@
name|Inject
DECL|method|IngestProxyActionFilter
specifier|public
name|IngestProxyActionFilter
parameter_list|(
name|ClusterService
name|clusterService
parameter_list|,
name|TransportService
name|transportService
parameter_list|)
block|{
assert|assert
name|IngestModule
operator|.
name|isIngestEnabled
argument_list|(
name|clusterService
operator|.
name|state
argument_list|()
operator|.
name|nodes
argument_list|()
operator|.
name|localNode
argument_list|()
operator|.
name|attributes
argument_list|()
argument_list|)
operator|==
literal|false
assert|;
name|this
operator|.
name|clusterService
operator|=
name|clusterService
expr_stmt|;
name|this
operator|.
name|transportService
operator|=
name|transportService
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|apply
specifier|public
name|void
name|apply
parameter_list|(
name|Task
name|task
parameter_list|,
name|String
name|action
parameter_list|,
name|ActionRequest
name|request
parameter_list|,
name|ActionListener
name|listener
parameter_list|,
name|ActionFilterChain
name|chain
parameter_list|)
block|{
name|Action
name|ingestAction
init|=
literal|null
decl_stmt|;
name|boolean
name|isIngestRequest
init|=
literal|false
decl_stmt|;
if|if
condition|(
name|IndexAction
operator|.
name|NAME
operator|.
name|equals
argument_list|(
name|action
argument_list|)
condition|)
block|{
name|ingestAction
operator|=
name|IndexAction
operator|.
name|INSTANCE
expr_stmt|;
assert|assert
name|request
operator|instanceof
name|IndexRequest
assert|;
name|IndexRequest
name|indexRequest
init|=
operator|(
name|IndexRequest
operator|)
name|request
decl_stmt|;
name|isIngestRequest
operator|=
name|Strings
operator|.
name|hasText
argument_list|(
name|indexRequest
operator|.
name|pipeline
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|BulkAction
operator|.
name|NAME
operator|.
name|equals
argument_list|(
name|action
argument_list|)
condition|)
block|{
name|ingestAction
operator|=
name|BulkAction
operator|.
name|INSTANCE
expr_stmt|;
assert|assert
name|request
operator|instanceof
name|BulkRequest
assert|;
name|BulkRequest
name|bulkRequest
init|=
operator|(
name|BulkRequest
operator|)
name|request
decl_stmt|;
for|for
control|(
name|ActionRequest
name|actionRequest
range|:
name|bulkRequest
operator|.
name|requests
argument_list|()
control|)
block|{
if|if
condition|(
name|actionRequest
operator|instanceof
name|IndexRequest
condition|)
block|{
name|IndexRequest
name|indexRequest
init|=
operator|(
name|IndexRequest
operator|)
name|actionRequest
decl_stmt|;
if|if
condition|(
name|Strings
operator|.
name|hasText
argument_list|(
name|indexRequest
operator|.
name|pipeline
argument_list|()
argument_list|)
condition|)
block|{
name|isIngestRequest
operator|=
literal|true
expr_stmt|;
break|break;
block|}
block|}
block|}
block|}
if|if
condition|(
name|isIngestRequest
condition|)
block|{
assert|assert
name|ingestAction
operator|!=
literal|null
assert|;
name|forwardIngestRequest
argument_list|(
name|ingestAction
argument_list|,
name|request
argument_list|,
name|listener
argument_list|)
expr_stmt|;
return|return;
block|}
name|chain
operator|.
name|proceed
argument_list|(
name|task
argument_list|,
name|action
argument_list|,
name|request
argument_list|,
name|listener
argument_list|)
expr_stmt|;
block|}
DECL|method|forwardIngestRequest
specifier|private
name|void
name|forwardIngestRequest
parameter_list|(
name|Action
name|action
parameter_list|,
name|ActionRequest
name|request
parameter_list|,
name|ActionListener
name|listener
parameter_list|)
block|{
name|transportService
operator|.
name|sendRequest
argument_list|(
name|randomIngestNode
argument_list|()
argument_list|,
name|action
operator|.
name|name
argument_list|()
argument_list|,
name|request
argument_list|,
operator|new
name|TransportResponseHandler
argument_list|<
name|TransportResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|TransportResponse
name|newInstance
parameter_list|()
block|{
return|return
name|action
operator|.
name|newResponse
argument_list|()
return|;
block|}
annotation|@
name|Override
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
name|void
name|handleResponse
parameter_list|(
name|TransportResponse
name|response
parameter_list|)
block|{
name|listener
operator|.
name|onResponse
argument_list|(
name|response
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|handleException
parameter_list|(
name|TransportException
name|exp
parameter_list|)
block|{
name|listener
operator|.
name|onFailure
argument_list|(
name|exp
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
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
block|}
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|apply
specifier|public
name|void
name|apply
parameter_list|(
name|String
name|action
parameter_list|,
name|ActionResponse
name|response
parameter_list|,
name|ActionListener
name|listener
parameter_list|,
name|ActionFilterChain
name|chain
parameter_list|)
block|{
name|chain
operator|.
name|proceed
argument_list|(
name|action
argument_list|,
name|response
argument_list|,
name|listener
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|order
specifier|public
name|int
name|order
parameter_list|()
block|{
return|return
name|Integer
operator|.
name|MAX_VALUE
return|;
block|}
DECL|method|randomIngestNode
specifier|private
name|DiscoveryNode
name|randomIngestNode
parameter_list|()
block|{
name|ClusterState
name|state
init|=
name|clusterService
operator|.
name|state
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|DiscoveryNode
argument_list|>
name|ingestNodes
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|DiscoveryNode
name|node
range|:
name|state
operator|.
name|nodes
argument_list|()
control|)
block|{
if|if
condition|(
name|IngestModule
operator|.
name|isIngestEnabled
argument_list|(
name|node
operator|.
name|getAttributes
argument_list|()
argument_list|)
condition|)
block|{
name|ingestNodes
operator|.
name|add
argument_list|(
name|node
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|ingestNodes
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"There are no ingest nodes in this cluster, unable to forward request to an ingest node."
argument_list|)
throw|;
block|}
name|int
name|index
init|=
name|getNodeNumber
argument_list|()
decl_stmt|;
return|return
name|ingestNodes
operator|.
name|get
argument_list|(
operator|(
name|index
operator|)
operator|%
name|ingestNodes
operator|.
name|size
argument_list|()
argument_list|)
return|;
block|}
DECL|method|getNodeNumber
specifier|private
name|int
name|getNodeNumber
parameter_list|()
block|{
name|int
name|index
init|=
name|randomNodeGenerator
operator|.
name|incrementAndGet
argument_list|()
decl_stmt|;
if|if
condition|(
name|index
operator|<
literal|0
condition|)
block|{
name|index
operator|=
literal|0
expr_stmt|;
name|randomNodeGenerator
operator|.
name|set
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
return|return
name|index
return|;
block|}
block|}
end_class

end_unit

