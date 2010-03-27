begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.shard.service
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|shard
operator|.
name|service
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
name|index
operator|.
name|Term
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
name|index
operator|.
name|engine
operator|.
name|Engine
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
name|engine
operator|.
name|EngineException
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
name|ParsedDocument
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
name|shard
operator|.
name|IndexShardComponent
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
name|shard
operator|.
name|IndexShardLifecycle
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
name|shard
operator|.
name|IndexShardState
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|Nullable
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|SizeValue
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|component
operator|.
name|CloseableComponent
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ThreadSafe
import|;
end_import

begin_comment
comment|/**  * @author kimchy (Shay Banon)  */
end_comment

begin_interface
annotation|@
name|IndexShardLifecycle
annotation|@
name|ThreadSafe
DECL|interface|IndexShard
specifier|public
interface|interface
name|IndexShard
extends|extends
name|IndexShardComponent
extends|,
name|CloseableComponent
block|{
DECL|method|routingEntry
name|ShardRouting
name|routingEntry
parameter_list|()
function_decl|;
DECL|method|state
name|IndexShardState
name|state
parameter_list|()
function_decl|;
comment|/**      * Returns the estimated flushable memory size. Returns<tt>null</tt> if not available.      */
DECL|method|estimateFlushableMemorySize
name|SizeValue
name|estimateFlushableMemorySize
parameter_list|()
throws|throws
name|ElasticSearchException
function_decl|;
DECL|method|create
name|ParsedDocument
name|create
parameter_list|(
name|String
name|type
parameter_list|,
name|String
name|id
parameter_list|,
name|byte
index|[]
name|source
parameter_list|)
throws|throws
name|ElasticSearchException
function_decl|;
DECL|method|index
name|ParsedDocument
name|index
parameter_list|(
name|String
name|type
parameter_list|,
name|String
name|id
parameter_list|,
name|byte
index|[]
name|source
parameter_list|)
throws|throws
name|ElasticSearchException
function_decl|;
DECL|method|delete
name|void
name|delete
parameter_list|(
name|String
name|type
parameter_list|,
name|String
name|id
parameter_list|)
function_decl|;
DECL|method|delete
name|void
name|delete
parameter_list|(
name|Term
name|uid
parameter_list|)
function_decl|;
DECL|method|deleteByQuery
name|void
name|deleteByQuery
parameter_list|(
name|byte
index|[]
name|querySource
parameter_list|,
annotation|@
name|Nullable
name|String
name|queryParserName
parameter_list|,
name|String
modifier|...
name|types
parameter_list|)
throws|throws
name|ElasticSearchException
function_decl|;
DECL|method|get
name|byte
index|[]
name|get
parameter_list|(
name|String
name|type
parameter_list|,
name|String
name|id
parameter_list|)
throws|throws
name|ElasticSearchException
function_decl|;
DECL|method|count
name|long
name|count
parameter_list|(
name|float
name|minScore
parameter_list|,
name|byte
index|[]
name|querySource
parameter_list|,
annotation|@
name|Nullable
name|String
name|queryParserName
parameter_list|,
name|String
modifier|...
name|types
parameter_list|)
throws|throws
name|ElasticSearchException
function_decl|;
DECL|method|refresh
name|void
name|refresh
parameter_list|(
name|Engine
operator|.
name|Refresh
name|refresh
parameter_list|)
throws|throws
name|ElasticSearchException
function_decl|;
DECL|method|flush
name|void
name|flush
parameter_list|(
name|Engine
operator|.
name|Flush
name|flush
parameter_list|)
throws|throws
name|ElasticSearchException
function_decl|;
DECL|method|optimize
name|void
name|optimize
parameter_list|(
name|Engine
operator|.
name|Optimize
name|optimize
parameter_list|)
throws|throws
name|ElasticSearchException
function_decl|;
DECL|method|snapshot
name|void
name|snapshot
parameter_list|(
name|Engine
operator|.
name|SnapshotHandler
name|snapshotHandler
parameter_list|)
throws|throws
name|EngineException
function_decl|;
DECL|method|recover
name|void
name|recover
parameter_list|(
name|Engine
operator|.
name|RecoveryHandler
name|recoveryHandler
parameter_list|)
throws|throws
name|EngineException
function_decl|;
DECL|method|searcher
name|Engine
operator|.
name|Searcher
name|searcher
parameter_list|()
function_decl|;
comment|/**      * Returns<tt>true</tt> if this shard can ignore a recovery attempt made to it (since the already doing/done it)      */
DECL|method|ignoreRecoveryAttempt
specifier|public
name|boolean
name|ignoreRecoveryAttempt
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

