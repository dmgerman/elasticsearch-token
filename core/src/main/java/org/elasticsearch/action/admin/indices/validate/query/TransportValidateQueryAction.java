begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.indices.validate.query
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
name|validate
operator|.
name|query
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
name|search
operator|.
name|IndexSearcher
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
name|search
operator|.
name|MatchNoDocsQuery
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
name|search
operator|.
name|Query
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
name|ShardOperationFailedException
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
name|DefaultShardOperationFailedException
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
name|broadcast
operator|.
name|BroadcastShardOperationFailedException
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
name|broadcast
operator|.
name|TransportBroadcastAction
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
name|routing
operator|.
name|GroupShardsIterator
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
name|ParsingException
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
name|lease
operator|.
name|Releasables
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
name|query
operator|.
name|ParsedQuery
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
name|QueryShardException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|SearchService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|internal
operator|.
name|SearchContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|internal
operator|.
name|ShardSearchLocalRequest
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
name|Map
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
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicReferenceArray
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|TransportValidateQueryAction
specifier|public
class|class
name|TransportValidateQueryAction
extends|extends
name|TransportBroadcastAction
argument_list|<
name|ValidateQueryRequest
argument_list|,
name|ValidateQueryResponse
argument_list|,
name|ShardValidateQueryRequest
argument_list|,
name|ShardValidateQueryResponse
argument_list|>
block|{
DECL|field|searchService
specifier|private
specifier|final
name|SearchService
name|searchService
decl_stmt|;
annotation|@
name|Inject
DECL|method|TransportValidateQueryAction
specifier|public
name|TransportValidateQueryAction
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|,
name|ClusterService
name|clusterService
parameter_list|,
name|TransportService
name|transportService
parameter_list|,
name|SearchService
name|searchService
parameter_list|,
name|ActionFilters
name|actionFilters
parameter_list|,
name|IndexNameExpressionResolver
name|indexNameExpressionResolver
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|ValidateQueryAction
operator|.
name|NAME
argument_list|,
name|threadPool
argument_list|,
name|clusterService
argument_list|,
name|transportService
argument_list|,
name|actionFilters
argument_list|,
name|indexNameExpressionResolver
argument_list|,
name|ValidateQueryRequest
operator|::
operator|new
argument_list|,
name|ShardValidateQueryRequest
operator|::
operator|new
argument_list|,
name|ThreadPool
operator|.
name|Names
operator|.
name|SEARCH
argument_list|)
expr_stmt|;
name|this
operator|.
name|searchService
operator|=
name|searchService
expr_stmt|;
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
name|ValidateQueryRequest
name|request
parameter_list|,
name|ActionListener
argument_list|<
name|ValidateQueryResponse
argument_list|>
name|listener
parameter_list|)
block|{
name|request
operator|.
name|nowInMillis
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
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
DECL|method|newShardRequest
specifier|protected
name|ShardValidateQueryRequest
name|newShardRequest
parameter_list|(
name|int
name|numShards
parameter_list|,
name|ShardRouting
name|shard
parameter_list|,
name|ValidateQueryRequest
name|request
parameter_list|)
block|{
name|String
index|[]
name|filteringAliases
init|=
name|indexNameExpressionResolver
operator|.
name|filteringAliases
argument_list|(
name|clusterService
operator|.
name|state
argument_list|()
argument_list|,
name|shard
operator|.
name|getIndexName
argument_list|()
argument_list|,
name|request
operator|.
name|indices
argument_list|()
argument_list|)
decl_stmt|;
return|return
operator|new
name|ShardValidateQueryRequest
argument_list|(
name|shard
operator|.
name|shardId
argument_list|()
argument_list|,
name|filteringAliases
argument_list|,
name|request
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|newShardResponse
specifier|protected
name|ShardValidateQueryResponse
name|newShardResponse
parameter_list|()
block|{
return|return
operator|new
name|ShardValidateQueryResponse
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|shards
specifier|protected
name|GroupShardsIterator
name|shards
parameter_list|(
name|ClusterState
name|clusterState
parameter_list|,
name|ValidateQueryRequest
name|request
parameter_list|,
name|String
index|[]
name|concreteIndices
parameter_list|)
block|{
comment|// Hard-code routing to limit request to a single shard, but still, randomize it...
name|Map
argument_list|<
name|String
argument_list|,
name|Set
argument_list|<
name|String
argument_list|>
argument_list|>
name|routingMap
init|=
name|indexNameExpressionResolver
operator|.
name|resolveSearchRouting
argument_list|(
name|clusterState
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|Randomness
operator|.
name|get
argument_list|()
operator|.
name|nextInt
argument_list|(
literal|1000
argument_list|)
argument_list|)
argument_list|,
name|request
operator|.
name|indices
argument_list|()
argument_list|)
decl_stmt|;
return|return
name|clusterService
operator|.
name|operationRouting
argument_list|()
operator|.
name|searchShards
argument_list|(
name|clusterState
argument_list|,
name|concreteIndices
argument_list|,
name|routingMap
argument_list|,
literal|"_local"
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|checkGlobalBlock
specifier|protected
name|ClusterBlockException
name|checkGlobalBlock
parameter_list|(
name|ClusterState
name|state
parameter_list|,
name|ValidateQueryRequest
name|request
parameter_list|)
block|{
return|return
name|state
operator|.
name|blocks
argument_list|()
operator|.
name|globalBlockedException
argument_list|(
name|ClusterBlockLevel
operator|.
name|READ
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|checkRequestBlock
specifier|protected
name|ClusterBlockException
name|checkRequestBlock
parameter_list|(
name|ClusterState
name|state
parameter_list|,
name|ValidateQueryRequest
name|countRequest
parameter_list|,
name|String
index|[]
name|concreteIndices
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
name|READ
argument_list|,
name|concreteIndices
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|newResponse
specifier|protected
name|ValidateQueryResponse
name|newResponse
parameter_list|(
name|ValidateQueryRequest
name|request
parameter_list|,
name|AtomicReferenceArray
name|shardsResponses
parameter_list|,
name|ClusterState
name|clusterState
parameter_list|)
block|{
name|int
name|successfulShards
init|=
literal|0
decl_stmt|;
name|int
name|failedShards
init|=
literal|0
decl_stmt|;
name|boolean
name|valid
init|=
literal|true
decl_stmt|;
name|List
argument_list|<
name|ShardOperationFailedException
argument_list|>
name|shardFailures
init|=
literal|null
decl_stmt|;
name|List
argument_list|<
name|QueryExplanation
argument_list|>
name|queryExplanations
init|=
literal|null
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|shardsResponses
operator|.
name|length
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|Object
name|shardResponse
init|=
name|shardsResponses
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|shardResponse
operator|==
literal|null
condition|)
block|{
comment|// simply ignore non active shards
block|}
elseif|else
if|if
condition|(
name|shardResponse
operator|instanceof
name|BroadcastShardOperationFailedException
condition|)
block|{
name|failedShards
operator|++
expr_stmt|;
if|if
condition|(
name|shardFailures
operator|==
literal|null
condition|)
block|{
name|shardFailures
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
block|}
name|shardFailures
operator|.
name|add
argument_list|(
operator|new
name|DefaultShardOperationFailedException
argument_list|(
operator|(
name|BroadcastShardOperationFailedException
operator|)
name|shardResponse
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|ShardValidateQueryResponse
name|validateQueryResponse
init|=
operator|(
name|ShardValidateQueryResponse
operator|)
name|shardResponse
decl_stmt|;
name|valid
operator|=
name|valid
operator|&&
name|validateQueryResponse
operator|.
name|isValid
argument_list|()
expr_stmt|;
if|if
condition|(
name|request
operator|.
name|explain
argument_list|()
operator|||
name|request
operator|.
name|rewrite
argument_list|()
condition|)
block|{
if|if
condition|(
name|queryExplanations
operator|==
literal|null
condition|)
block|{
name|queryExplanations
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
block|}
name|queryExplanations
operator|.
name|add
argument_list|(
operator|new
name|QueryExplanation
argument_list|(
name|validateQueryResponse
operator|.
name|getIndex
argument_list|()
argument_list|,
name|validateQueryResponse
operator|.
name|isValid
argument_list|()
argument_list|,
name|validateQueryResponse
operator|.
name|getExplanation
argument_list|()
argument_list|,
name|validateQueryResponse
operator|.
name|getError
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|successfulShards
operator|++
expr_stmt|;
block|}
block|}
return|return
operator|new
name|ValidateQueryResponse
argument_list|(
name|valid
argument_list|,
name|queryExplanations
argument_list|,
name|shardsResponses
operator|.
name|length
argument_list|()
argument_list|,
name|successfulShards
argument_list|,
name|failedShards
argument_list|,
name|shardFailures
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|shardOperation
specifier|protected
name|ShardValidateQueryResponse
name|shardOperation
parameter_list|(
name|ShardValidateQueryRequest
name|request
parameter_list|)
block|{
name|boolean
name|valid
decl_stmt|;
name|String
name|explanation
init|=
literal|null
decl_stmt|;
name|String
name|error
init|=
literal|null
decl_stmt|;
name|ShardSearchLocalRequest
name|shardSearchLocalRequest
init|=
operator|new
name|ShardSearchLocalRequest
argument_list|(
name|request
operator|.
name|shardId
argument_list|()
argument_list|,
name|request
operator|.
name|types
argument_list|()
argument_list|,
name|request
operator|.
name|nowInMillis
argument_list|()
argument_list|,
name|request
operator|.
name|filteringAliases
argument_list|()
argument_list|)
decl_stmt|;
name|SearchContext
name|searchContext
init|=
name|searchService
operator|.
name|createSearchContext
argument_list|(
name|shardSearchLocalRequest
argument_list|,
name|SearchService
operator|.
name|NO_TIMEOUT
argument_list|,
literal|null
argument_list|)
decl_stmt|;
try|try
block|{
name|ParsedQuery
name|parsedQuery
init|=
name|searchContext
operator|.
name|getQueryShardContext
argument_list|()
operator|.
name|toQuery
argument_list|(
name|request
operator|.
name|query
argument_list|()
argument_list|)
decl_stmt|;
name|searchContext
operator|.
name|parsedQuery
argument_list|(
name|parsedQuery
argument_list|)
expr_stmt|;
name|searchContext
operator|.
name|preProcess
argument_list|(
name|request
operator|.
name|rewrite
argument_list|()
argument_list|)
expr_stmt|;
name|valid
operator|=
literal|true
expr_stmt|;
name|explanation
operator|=
name|explain
argument_list|(
name|searchContext
argument_list|,
name|request
operator|.
name|rewrite
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|QueryShardException
decl||
name|ParsingException
name|e
parameter_list|)
block|{
name|valid
operator|=
literal|false
expr_stmt|;
name|error
operator|=
name|e
operator|.
name|getDetailedMessage
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|AssertionError
decl||
name|IOException
name|e
parameter_list|)
block|{
name|valid
operator|=
literal|false
expr_stmt|;
name|error
operator|=
name|e
operator|.
name|getMessage
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
name|Releasables
operator|.
name|close
argument_list|(
name|searchContext
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|ShardValidateQueryResponse
argument_list|(
name|request
operator|.
name|shardId
argument_list|()
argument_list|,
name|valid
argument_list|,
name|explanation
argument_list|,
name|error
argument_list|)
return|;
block|}
DECL|method|explain
specifier|private
name|String
name|explain
parameter_list|(
name|SearchContext
name|context
parameter_list|,
name|boolean
name|rewritten
parameter_list|)
throws|throws
name|IOException
block|{
name|Query
name|query
init|=
name|context
operator|.
name|query
argument_list|()
decl_stmt|;
if|if
condition|(
name|rewritten
operator|&&
name|query
operator|instanceof
name|MatchNoDocsQuery
condition|)
block|{
return|return
name|context
operator|.
name|parsedQuery
argument_list|()
operator|.
name|query
argument_list|()
operator|.
name|toString
argument_list|()
return|;
block|}
else|else
block|{
return|return
name|query
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
block|}
end_class

end_unit

