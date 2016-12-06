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
name|ActionListenerResponseHandler
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
name|cluster
operator|.
name|node
operator|.
name|DiscoveryNodes
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
name|TransportService
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
name|apply
parameter_list|(
name|Task
name|task
parameter_list|,
name|String
name|action
parameter_list|,
name|Request
name|request
parameter_list|,
name|ActionListener
argument_list|<
name|Response
argument_list|>
name|listener
parameter_list|,
name|ActionFilterChain
argument_list|<
name|Request
argument_list|,
name|Response
argument_list|>
name|chain
parameter_list|)
block|{
name|Action
name|ingestAction
decl_stmt|;
switch|switch
condition|(
name|action
condition|)
block|{
case|case
name|IndexAction
operator|.
name|NAME
case|:
name|ingestAction
operator|=
name|IndexAction
operator|.
name|INSTANCE
expr_stmt|;
name|IndexRequest
name|indexRequest
init|=
operator|(
name|IndexRequest
operator|)
name|request
decl_stmt|;
if|if
condition|(
name|Strings
operator|.
name|hasText
argument_list|(
name|indexRequest
operator|.
name|getPipeline
argument_list|()
argument_list|)
condition|)
block|{
name|forwardIngestRequest
argument_list|(
name|ingestAction
argument_list|,
name|request
argument_list|,
name|listener
argument_list|)
expr_stmt|;
block|}
else|else
block|{
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
break|break;
case|case
name|BulkAction
operator|.
name|NAME
case|:
name|ingestAction
operator|=
name|BulkAction
operator|.
name|INSTANCE
expr_stmt|;
name|BulkRequest
name|bulkRequest
init|=
operator|(
name|BulkRequest
operator|)
name|request
decl_stmt|;
if|if
condition|(
name|bulkRequest
operator|.
name|hasIndexRequestsWithPipelines
argument_list|()
condition|)
block|{
name|forwardIngestRequest
argument_list|(
name|ingestAction
argument_list|,
name|request
argument_list|,
name|listener
argument_list|)
expr_stmt|;
block|}
else|else
block|{
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
break|break;
default|default:
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
break|break;
block|}
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|method|forwardIngestRequest
specifier|private
name|void
name|forwardIngestRequest
parameter_list|(
name|Action
argument_list|<
name|?
argument_list|,
name|?
argument_list|,
name|?
argument_list|>
name|action
parameter_list|,
name|ActionRequest
name|request
parameter_list|,
name|ActionListener
argument_list|<
name|?
argument_list|>
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
name|ActionListenerResponseHandler
argument_list|(
name|listener
argument_list|,
name|action
operator|::
name|newResponse
argument_list|)
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
assert|assert
name|clusterService
operator|.
name|localNode
argument_list|()
operator|.
name|isIngestNode
argument_list|()
operator|==
literal|false
assert|;
name|DiscoveryNodes
name|nodes
init|=
name|clusterService
operator|.
name|state
argument_list|()
operator|.
name|getNodes
argument_list|()
decl_stmt|;
name|DiscoveryNode
index|[]
name|ingestNodes
init|=
name|nodes
operator|.
name|getIngestNodes
argument_list|()
operator|.
name|values
argument_list|()
operator|.
name|toArray
argument_list|(
name|DiscoveryNode
operator|.
name|class
argument_list|)
decl_stmt|;
if|if
condition|(
name|ingestNodes
operator|.
name|length
operator|==
literal|0
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
index|[
operator|(
name|index
operator|)
operator|%
name|ingestNodes
operator|.
name|length
index|]
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

