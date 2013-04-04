begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.mlt
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|mlt
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|document
operator|.
name|Field
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|index
operator|.
name|Term
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|BytesRef
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticSearchException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticSearchIllegalStateException
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
name|get
operator|.
name|GetRequest
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
name|GetResponse
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
name|TransportGetAction
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
name|SearchRequest
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
name|SearchResponse
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
name|TransportSearchAction
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
name|cluster
operator|.
name|routing
operator|.
name|MutableShardRouting
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
name|routing
operator|.
name|RoutingNode
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
name|routing
operator|.
name|ShardIterator
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
name|routing
operator|.
name|ShardRouting
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
name|get
operator|.
name|GetField
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
name|mapper
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
name|index
operator|.
name|mapper
operator|.
name|internal
operator|.
name|SourceFieldMapper
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
name|query
operator|.
name|BoolQueryBuilder
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
name|query
operator|.
name|MoreLikeThisFieldQueryBuilder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|IndicesService
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
name|*
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
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

begin_import
import|import static
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Sets
operator|.
name|newHashSet
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|client
operator|.
name|Requests
operator|.
name|getRequest
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|client
operator|.
name|Requests
operator|.
name|searchRequest
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|QueryBuilders
operator|.
name|*
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|builder
operator|.
name|SearchSourceBuilder
operator|.
name|searchSource
import|;
end_import

begin_comment
comment|/**  * The more like this action.  */
end_comment

begin_class
DECL|class|TransportMoreLikeThisAction
specifier|public
class|class
name|TransportMoreLikeThisAction
extends|extends
name|TransportAction
argument_list|<
name|MoreLikeThisRequest
argument_list|,
name|SearchResponse
argument_list|>
block|{
DECL|field|searchAction
specifier|private
specifier|final
name|TransportSearchAction
name|searchAction
decl_stmt|;
DECL|field|getAction
specifier|private
specifier|final
name|TransportGetAction
name|getAction
decl_stmt|;
DECL|field|indicesService
specifier|private
specifier|final
name|IndicesService
name|indicesService
decl_stmt|;
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
annotation|@
name|Inject
DECL|method|TransportMoreLikeThisAction
specifier|public
name|TransportMoreLikeThisAction
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|,
name|TransportSearchAction
name|searchAction
parameter_list|,
name|TransportGetAction
name|getAction
parameter_list|,
name|ClusterService
name|clusterService
parameter_list|,
name|IndicesService
name|indicesService
parameter_list|,
name|TransportService
name|transportService
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|threadPool
argument_list|)
expr_stmt|;
name|this
operator|.
name|searchAction
operator|=
name|searchAction
expr_stmt|;
name|this
operator|.
name|getAction
operator|=
name|getAction
expr_stmt|;
name|this
operator|.
name|indicesService
operator|=
name|indicesService
expr_stmt|;
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
name|transportService
operator|.
name|registerHandler
argument_list|(
name|MoreLikeThisAction
operator|.
name|NAME
argument_list|,
operator|new
name|TransportHandler
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doExecute
specifier|protected
name|void
name|doExecute
parameter_list|(
specifier|final
name|MoreLikeThisRequest
name|request
parameter_list|,
specifier|final
name|ActionListener
argument_list|<
name|SearchResponse
argument_list|>
name|listener
parameter_list|)
block|{
comment|// update to actual index name
name|ClusterState
name|clusterState
init|=
name|clusterService
operator|.
name|state
argument_list|()
decl_stmt|;
comment|// update to the concrete index
specifier|final
name|String
name|concreteIndex
init|=
name|clusterState
operator|.
name|metaData
argument_list|()
operator|.
name|concreteIndex
argument_list|(
name|request
operator|.
name|index
argument_list|()
argument_list|)
decl_stmt|;
name|RoutingNode
name|routingNode
init|=
name|clusterState
operator|.
name|getRoutingNodes
argument_list|()
operator|.
name|nodesToShards
argument_list|()
operator|.
name|get
argument_list|(
name|clusterService
operator|.
name|localNode
argument_list|()
operator|.
name|getId
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|routingNode
operator|==
literal|null
condition|)
block|{
name|redirect
argument_list|(
name|request
argument_list|,
name|listener
argument_list|,
name|clusterState
argument_list|)
expr_stmt|;
return|return;
block|}
name|boolean
name|hasIndexLocally
init|=
literal|false
decl_stmt|;
for|for
control|(
name|MutableShardRouting
name|shardRouting
range|:
name|routingNode
operator|.
name|shards
argument_list|()
control|)
block|{
if|if
condition|(
name|concreteIndex
operator|.
name|equals
argument_list|(
name|shardRouting
operator|.
name|index
argument_list|()
argument_list|)
condition|)
block|{
name|hasIndexLocally
operator|=
literal|true
expr_stmt|;
break|break;
block|}
block|}
if|if
condition|(
operator|!
name|hasIndexLocally
condition|)
block|{
name|redirect
argument_list|(
name|request
argument_list|,
name|listener
argument_list|,
name|clusterState
argument_list|)
expr_stmt|;
return|return;
block|}
name|Set
argument_list|<
name|String
argument_list|>
name|getFields
init|=
name|newHashSet
argument_list|()
decl_stmt|;
if|if
condition|(
name|request
operator|.
name|fields
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|Collections
operator|.
name|addAll
argument_list|(
name|getFields
argument_list|,
name|request
operator|.
name|fields
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// add the source, in case we need to parse it to get fields
name|getFields
operator|.
name|add
argument_list|(
name|SourceFieldMapper
operator|.
name|NAME
argument_list|)
expr_stmt|;
name|GetRequest
name|getRequest
init|=
name|getRequest
argument_list|(
name|concreteIndex
argument_list|)
operator|.
name|fields
argument_list|(
name|getFields
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
name|getFields
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|)
operator|.
name|type
argument_list|(
name|request
operator|.
name|type
argument_list|()
argument_list|)
operator|.
name|id
argument_list|(
name|request
operator|.
name|id
argument_list|()
argument_list|)
operator|.
name|routing
argument_list|(
name|request
operator|.
name|routing
argument_list|()
argument_list|)
operator|.
name|listenerThreaded
argument_list|(
literal|true
argument_list|)
operator|.
name|operationThreaded
argument_list|(
literal|true
argument_list|)
decl_stmt|;
name|request
operator|.
name|beforeLocalFork
argument_list|()
expr_stmt|;
name|getAction
operator|.
name|execute
argument_list|(
name|getRequest
argument_list|,
operator|new
name|ActionListener
argument_list|<
name|GetResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onResponse
parameter_list|(
name|GetResponse
name|getResponse
parameter_list|)
block|{
if|if
condition|(
operator|!
name|getResponse
operator|.
name|isExists
argument_list|()
condition|)
block|{
name|listener
operator|.
name|onFailure
argument_list|(
operator|new
name|ElasticSearchException
argument_list|(
literal|"document missing"
argument_list|)
argument_list|)
expr_stmt|;
return|return;
block|}
specifier|final
name|BoolQueryBuilder
name|boolBuilder
init|=
name|boolQuery
argument_list|()
decl_stmt|;
try|try
block|{
specifier|final
name|DocumentMapper
name|docMapper
init|=
name|indicesService
operator|.
name|indexServiceSafe
argument_list|(
name|concreteIndex
argument_list|)
operator|.
name|mapperService
argument_list|()
operator|.
name|documentMapper
argument_list|(
name|request
operator|.
name|type
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|docMapper
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ElasticSearchException
argument_list|(
literal|"No DocumentMapper found for type ["
operator|+
name|request
operator|.
name|type
argument_list|()
operator|+
literal|"]"
argument_list|)
throw|;
block|}
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|fields
init|=
name|newHashSet
argument_list|()
decl_stmt|;
if|if
condition|(
name|request
operator|.
name|fields
argument_list|()
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|String
name|field
range|:
name|request
operator|.
name|fields
argument_list|()
control|)
block|{
name|FieldMappers
name|fieldMappers
init|=
name|docMapper
operator|.
name|mappers
argument_list|()
operator|.
name|smartName
argument_list|(
name|field
argument_list|)
decl_stmt|;
if|if
condition|(
name|fieldMappers
operator|!=
literal|null
condition|)
block|{
name|fields
operator|.
name|add
argument_list|(
name|fieldMappers
operator|.
name|mapper
argument_list|()
operator|.
name|names
argument_list|()
operator|.
name|indexName
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|fields
operator|.
name|add
argument_list|(
name|field
argument_list|)
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
operator|!
name|fields
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
comment|// if fields are not empty, see if we got them in the response
for|for
control|(
name|Iterator
argument_list|<
name|String
argument_list|>
name|it
init|=
name|fields
operator|.
name|iterator
argument_list|()
init|;
name|it
operator|.
name|hasNext
argument_list|()
condition|;
control|)
block|{
name|String
name|field
init|=
name|it
operator|.
name|next
argument_list|()
decl_stmt|;
name|GetField
name|getField
init|=
name|getResponse
operator|.
name|getField
argument_list|(
name|field
argument_list|)
decl_stmt|;
if|if
condition|(
name|getField
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Object
name|value
range|:
name|getField
operator|.
name|getValues
argument_list|()
control|)
block|{
name|addMoreLikeThis
argument_list|(
name|request
argument_list|,
name|boolBuilder
argument_list|,
name|getField
operator|.
name|getName
argument_list|()
argument_list|,
name|value
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|it
operator|.
name|remove
argument_list|()
expr_stmt|;
block|}
block|}
if|if
condition|(
operator|!
name|fields
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
comment|// if we don't get all the fields in the get response, see if we can parse the source
name|parseSource
argument_list|(
name|getResponse
argument_list|,
name|boolBuilder
argument_list|,
name|docMapper
argument_list|,
name|fields
argument_list|,
name|request
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
comment|// we did not ask for any fields, try and get it from the source
name|parseSource
argument_list|(
name|getResponse
argument_list|,
name|boolBuilder
argument_list|,
name|docMapper
argument_list|,
name|fields
argument_list|,
name|request
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|boolBuilder
operator|.
name|hasClauses
argument_list|()
condition|)
block|{
comment|// no field added, fail
name|listener
operator|.
name|onFailure
argument_list|(
operator|new
name|ElasticSearchException
argument_list|(
literal|"No fields found to fetch the 'likeText' from"
argument_list|)
argument_list|)
expr_stmt|;
return|return;
block|}
comment|// exclude myself
name|Term
name|uidTerm
init|=
name|docMapper
operator|.
name|uidMapper
argument_list|()
operator|.
name|term
argument_list|(
name|request
operator|.
name|type
argument_list|()
argument_list|,
name|request
operator|.
name|id
argument_list|()
argument_list|)
decl_stmt|;
name|boolBuilder
operator|.
name|mustNot
argument_list|(
name|termQuery
argument_list|(
name|uidTerm
operator|.
name|field
argument_list|()
argument_list|,
name|uidTerm
operator|.
name|text
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|listener
operator|.
name|onFailure
argument_list|(
name|e
argument_list|)
expr_stmt|;
return|return;
block|}
name|String
index|[]
name|searchIndices
init|=
name|request
operator|.
name|searchIndices
argument_list|()
decl_stmt|;
if|if
condition|(
name|searchIndices
operator|==
literal|null
condition|)
block|{
name|searchIndices
operator|=
operator|new
name|String
index|[]
block|{
name|request
operator|.
name|index
argument_list|()
block|}
expr_stmt|;
block|}
name|String
index|[]
name|searchTypes
init|=
name|request
operator|.
name|searchTypes
argument_list|()
decl_stmt|;
if|if
condition|(
name|searchTypes
operator|==
literal|null
condition|)
block|{
name|searchTypes
operator|=
operator|new
name|String
index|[]
block|{
name|request
operator|.
name|type
argument_list|()
block|}
expr_stmt|;
block|}
name|int
name|size
init|=
name|request
operator|.
name|searchSize
argument_list|()
operator|!=
literal|0
condition|?
name|request
operator|.
name|searchSize
argument_list|()
else|:
literal|10
decl_stmt|;
name|int
name|from
init|=
name|request
operator|.
name|searchFrom
argument_list|()
operator|!=
literal|0
condition|?
name|request
operator|.
name|searchFrom
argument_list|()
else|:
literal|0
decl_stmt|;
name|SearchRequest
name|searchRequest
init|=
name|searchRequest
argument_list|(
name|searchIndices
argument_list|)
operator|.
name|types
argument_list|(
name|searchTypes
argument_list|)
operator|.
name|searchType
argument_list|(
name|request
operator|.
name|searchType
argument_list|()
argument_list|)
operator|.
name|scroll
argument_list|(
name|request
operator|.
name|searchScroll
argument_list|()
argument_list|)
operator|.
name|extraSource
argument_list|(
name|searchSource
argument_list|()
operator|.
name|query
argument_list|(
name|boolBuilder
argument_list|)
operator|.
name|from
argument_list|(
name|from
argument_list|)
operator|.
name|size
argument_list|(
name|size
argument_list|)
argument_list|)
operator|.
name|listenerThreaded
argument_list|(
name|request
operator|.
name|listenerThreaded
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|request
operator|.
name|searchSource
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|searchRequest
operator|.
name|source
argument_list|(
name|request
operator|.
name|searchSource
argument_list|()
argument_list|,
name|request
operator|.
name|searchSourceUnsafe
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|searchAction
operator|.
name|execute
argument_list|(
name|searchRequest
argument_list|,
operator|new
name|ActionListener
argument_list|<
name|SearchResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onResponse
parameter_list|(
name|SearchResponse
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
name|onFailure
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|listener
operator|.
name|onFailure
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|onFailure
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|listener
operator|.
name|onFailure
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
comment|// Redirects the request to a data node, that has the index meta data locally available.
DECL|method|redirect
specifier|private
name|void
name|redirect
parameter_list|(
name|MoreLikeThisRequest
name|request
parameter_list|,
specifier|final
name|ActionListener
argument_list|<
name|SearchResponse
argument_list|>
name|listener
parameter_list|,
name|ClusterState
name|clusterState
parameter_list|)
block|{
name|ShardIterator
name|shardIterator
init|=
name|clusterService
operator|.
name|operationRouting
argument_list|()
operator|.
name|getShards
argument_list|(
name|clusterState
argument_list|,
name|request
operator|.
name|index
argument_list|()
argument_list|,
name|request
operator|.
name|type
argument_list|()
argument_list|,
name|request
operator|.
name|id
argument_list|()
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|ShardRouting
name|shardRouting
init|=
name|shardIterator
operator|.
name|firstOrNull
argument_list|()
decl_stmt|;
if|if
condition|(
name|shardRouting
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ElasticSearchException
argument_list|(
literal|"No shards for index "
operator|+
name|request
operator|.
name|index
argument_list|()
argument_list|)
throw|;
block|}
name|String
name|nodeId
init|=
name|shardRouting
operator|.
name|currentNodeId
argument_list|()
decl_stmt|;
name|DiscoveryNode
name|discoveryNode
init|=
name|clusterState
operator|.
name|nodes
argument_list|()
operator|.
name|get
argument_list|(
name|nodeId
argument_list|)
decl_stmt|;
name|transportService
operator|.
name|sendRequest
argument_list|(
name|discoveryNode
argument_list|,
name|MoreLikeThisAction
operator|.
name|NAME
argument_list|,
name|request
argument_list|,
operator|new
name|TransportResponseHandler
argument_list|<
name|SearchResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|SearchResponse
name|newInstance
parameter_list|()
block|{
return|return
operator|new
name|SearchResponse
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|handleResponse
parameter_list|(
name|SearchResponse
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
DECL|method|parseSource
specifier|private
name|void
name|parseSource
parameter_list|(
name|GetResponse
name|getResponse
parameter_list|,
specifier|final
name|BoolQueryBuilder
name|boolBuilder
parameter_list|,
name|DocumentMapper
name|docMapper
parameter_list|,
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|fields
parameter_list|,
specifier|final
name|MoreLikeThisRequest
name|request
parameter_list|)
block|{
if|if
condition|(
name|getResponse
operator|.
name|isSourceEmpty
argument_list|()
condition|)
block|{
return|return;
block|}
name|docMapper
operator|.
name|parse
argument_list|(
name|SourceToParse
operator|.
name|source
argument_list|(
name|getResponse
operator|.
name|getSourceAsBytesRef
argument_list|()
argument_list|)
operator|.
name|type
argument_list|(
name|request
operator|.
name|type
argument_list|()
argument_list|)
operator|.
name|id
argument_list|(
name|request
operator|.
name|id
argument_list|()
argument_list|)
argument_list|,
operator|new
name|DocumentMapper
operator|.
name|ParseListenerAdapter
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|beforeFieldAdded
parameter_list|(
name|FieldMapper
name|fieldMapper
parameter_list|,
name|Field
name|field
parameter_list|,
name|Object
name|parseContext
parameter_list|)
block|{
if|if
condition|(
name|fieldMapper
operator|instanceof
name|InternalMapper
condition|)
block|{
return|return
literal|true
return|;
block|}
name|String
name|value
init|=
name|fieldMapper
operator|.
name|value
argument_list|(
name|convertField
argument_list|(
name|field
argument_list|)
argument_list|)
operator|.
name|toString
argument_list|()
decl_stmt|;
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|fields
operator|.
name|isEmpty
argument_list|()
operator|||
name|fields
operator|.
name|contains
argument_list|(
name|field
operator|.
name|name
argument_list|()
argument_list|)
condition|)
block|{
name|addMoreLikeThis
argument_list|(
name|request
argument_list|,
name|boolBuilder
argument_list|,
name|fieldMapper
argument_list|,
name|field
argument_list|)
expr_stmt|;
block|}
return|return
literal|false
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
DECL|method|convertField
specifier|private
name|Object
name|convertField
parameter_list|(
name|Field
name|field
parameter_list|)
block|{
if|if
condition|(
name|field
operator|.
name|stringValue
argument_list|()
operator|!=
literal|null
condition|)
block|{
return|return
name|field
operator|.
name|stringValue
argument_list|()
return|;
block|}
elseif|else
if|if
condition|(
name|field
operator|.
name|binaryValue
argument_list|()
operator|!=
literal|null
condition|)
block|{
return|return
name|BytesRef
operator|.
name|deepCopyOf
argument_list|(
name|field
operator|.
name|binaryValue
argument_list|()
argument_list|)
operator|.
name|bytes
return|;
block|}
elseif|else
if|if
condition|(
name|field
operator|.
name|numericValue
argument_list|()
operator|!=
literal|null
condition|)
block|{
return|return
name|field
operator|.
name|numericValue
argument_list|()
return|;
block|}
else|else
block|{
throw|throw
operator|new
name|ElasticSearchIllegalStateException
argument_list|(
literal|"Field should have either a string, numeric or binary value"
argument_list|)
throw|;
block|}
block|}
DECL|method|addMoreLikeThis
specifier|private
name|void
name|addMoreLikeThis
parameter_list|(
name|MoreLikeThisRequest
name|request
parameter_list|,
name|BoolQueryBuilder
name|boolBuilder
parameter_list|,
name|FieldMapper
name|fieldMapper
parameter_list|,
name|Field
name|field
parameter_list|)
block|{
name|addMoreLikeThis
argument_list|(
name|request
argument_list|,
name|boolBuilder
argument_list|,
name|field
operator|.
name|name
argument_list|()
argument_list|,
name|fieldMapper
operator|.
name|value
argument_list|(
name|convertField
argument_list|(
name|field
argument_list|)
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|addMoreLikeThis
specifier|private
name|void
name|addMoreLikeThis
parameter_list|(
name|MoreLikeThisRequest
name|request
parameter_list|,
name|BoolQueryBuilder
name|boolBuilder
parameter_list|,
name|String
name|fieldName
parameter_list|,
name|String
name|likeText
parameter_list|)
block|{
name|MoreLikeThisFieldQueryBuilder
name|mlt
init|=
name|moreLikeThisFieldQuery
argument_list|(
name|fieldName
argument_list|)
operator|.
name|likeText
argument_list|(
name|likeText
argument_list|)
operator|.
name|percentTermsToMatch
argument_list|(
name|request
operator|.
name|percentTermsToMatch
argument_list|()
argument_list|)
operator|.
name|boostTerms
argument_list|(
name|request
operator|.
name|boostTerms
argument_list|()
argument_list|)
operator|.
name|minDocFreq
argument_list|(
name|request
operator|.
name|minDocFreq
argument_list|()
argument_list|)
operator|.
name|maxDocFreq
argument_list|(
name|request
operator|.
name|maxDocFreq
argument_list|()
argument_list|)
operator|.
name|minWordLen
argument_list|(
name|request
operator|.
name|minWordLen
argument_list|()
argument_list|)
operator|.
name|maxWordLen
argument_list|(
name|request
operator|.
name|maxWordLen
argument_list|()
argument_list|)
operator|.
name|minTermFreq
argument_list|(
name|request
operator|.
name|minTermFreq
argument_list|()
argument_list|)
operator|.
name|maxQueryTerms
argument_list|(
name|request
operator|.
name|maxQueryTerms
argument_list|()
argument_list|)
operator|.
name|stopWords
argument_list|(
name|request
operator|.
name|stopWords
argument_list|()
argument_list|)
decl_stmt|;
name|boolBuilder
operator|.
name|should
argument_list|(
name|mlt
argument_list|)
expr_stmt|;
block|}
DECL|class|TransportHandler
specifier|private
class|class
name|TransportHandler
extends|extends
name|BaseTransportRequestHandler
argument_list|<
name|MoreLikeThisRequest
argument_list|>
block|{
annotation|@
name|Override
DECL|method|newInstance
specifier|public
name|MoreLikeThisRequest
name|newInstance
parameter_list|()
block|{
return|return
operator|new
name|MoreLikeThisRequest
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|messageReceived
specifier|public
name|void
name|messageReceived
parameter_list|(
name|MoreLikeThisRequest
name|request
parameter_list|,
specifier|final
name|TransportChannel
name|channel
parameter_list|)
throws|throws
name|Exception
block|{
comment|// no need to have a threaded listener since we just send back a response
name|request
operator|.
name|listenerThreaded
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|execute
argument_list|(
name|request
argument_list|,
operator|new
name|ActionListener
argument_list|<
name|SearchResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onResponse
parameter_list|(
name|SearchResponse
name|result
parameter_list|)
block|{
try|try
block|{
name|channel
operator|.
name|sendResponse
argument_list|(
name|result
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|onFailure
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|onFailure
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
try|try
block|{
name|channel
operator|.
name|sendResponse
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e1
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"Failed to send response for get"
argument_list|,
name|e1
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|executor
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
block|}
end_class

end_unit

