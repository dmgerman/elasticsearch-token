begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.indices.ttl
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|ttl
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
name|LeafReaderContext
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
name|search
operator|.
name|Query
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
name|Scorer
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
name|SimpleCollector
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchException
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
name|bulk
operator|.
name|BulkResponse
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
name|metadata
operator|.
name|IndexMetaData
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
name|MetaData
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
name|component
operator|.
name|AbstractLifecycleComponent
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
name|lucene
operator|.
name|uid
operator|.
name|Versions
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
name|common
operator|.
name|unit
operator|.
name|TimeValue
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
name|util
operator|.
name|concurrent
operator|.
name|EsExecutors
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
name|fieldvisitor
operator|.
name|UidAndRoutingFieldsVisitor
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
name|FieldMapper
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
name|FieldMappers
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
name|Uid
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
name|TTLFieldMapper
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
name|UidFieldMapper
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
name|service
operator|.
name|IndexService
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
name|index
operator|.
name|shard
operator|.
name|service
operator|.
name|IndexShard
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
name|node
operator|.
name|settings
operator|.
name|NodeSettingsService
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
name|concurrent
operator|.
name|CountDownLatch
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
name|TimeUnit
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
name|AtomicBoolean
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
name|locks
operator|.
name|Condition
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
name|locks
operator|.
name|ReentrantLock
import|;
end_import

begin_comment
comment|/**  * A node level service that delete expired docs on node primary shards.  */
end_comment

begin_class
DECL|class|IndicesTTLService
specifier|public
class|class
name|IndicesTTLService
extends|extends
name|AbstractLifecycleComponent
argument_list|<
name|IndicesTTLService
argument_list|>
block|{
DECL|field|INDICES_TTL_INTERVAL
specifier|public
specifier|static
specifier|final
name|String
name|INDICES_TTL_INTERVAL
init|=
literal|"indices.ttl.interval"
decl_stmt|;
DECL|field|INDEX_TTL_DISABLE_PURGE
specifier|public
specifier|static
specifier|final
name|String
name|INDEX_TTL_DISABLE_PURGE
init|=
literal|"index.ttl.disable_purge"
decl_stmt|;
DECL|field|clusterService
specifier|private
specifier|final
name|ClusterService
name|clusterService
decl_stmt|;
DECL|field|indicesService
specifier|private
specifier|final
name|IndicesService
name|indicesService
decl_stmt|;
DECL|field|bulkAction
specifier|private
specifier|final
name|TransportBulkAction
name|bulkAction
decl_stmt|;
DECL|field|bulkSize
specifier|private
specifier|final
name|int
name|bulkSize
decl_stmt|;
DECL|field|purgerThread
specifier|private
name|PurgerThread
name|purgerThread
decl_stmt|;
annotation|@
name|Inject
DECL|method|IndicesTTLService
specifier|public
name|IndicesTTLService
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ClusterService
name|clusterService
parameter_list|,
name|IndicesService
name|indicesService
parameter_list|,
name|NodeSettingsService
name|nodeSettingsService
parameter_list|,
name|TransportBulkAction
name|bulkAction
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|clusterService
operator|=
name|clusterService
expr_stmt|;
name|this
operator|.
name|indicesService
operator|=
name|indicesService
expr_stmt|;
name|TimeValue
name|interval
init|=
name|componentSettings
operator|.
name|getAsTime
argument_list|(
literal|"interval"
argument_list|,
name|TimeValue
operator|.
name|timeValueSeconds
argument_list|(
literal|60
argument_list|)
argument_list|)
decl_stmt|;
name|this
operator|.
name|bulkAction
operator|=
name|bulkAction
expr_stmt|;
name|this
operator|.
name|bulkSize
operator|=
name|componentSettings
operator|.
name|getAsInt
argument_list|(
literal|"bulk_size"
argument_list|,
literal|10000
argument_list|)
expr_stmt|;
name|this
operator|.
name|purgerThread
operator|=
operator|new
name|PurgerThread
argument_list|(
name|EsExecutors
operator|.
name|threadName
argument_list|(
name|settings
argument_list|,
literal|"[ttl_expire]"
argument_list|)
argument_list|,
name|interval
argument_list|)
expr_stmt|;
name|nodeSettingsService
operator|.
name|addListener
argument_list|(
operator|new
name|ApplySettings
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doStart
specifier|protected
name|void
name|doStart
parameter_list|()
throws|throws
name|ElasticsearchException
block|{
name|this
operator|.
name|purgerThread
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doStop
specifier|protected
name|void
name|doStop
parameter_list|()
throws|throws
name|ElasticsearchException
block|{
try|try
block|{
name|this
operator|.
name|purgerThread
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|Thread
operator|.
name|interrupted
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|doClose
specifier|protected
name|void
name|doClose
parameter_list|()
throws|throws
name|ElasticsearchException
block|{     }
DECL|class|PurgerThread
specifier|private
class|class
name|PurgerThread
extends|extends
name|Thread
block|{
DECL|field|running
specifier|private
specifier|final
name|AtomicBoolean
name|running
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|true
argument_list|)
decl_stmt|;
DECL|field|notifier
specifier|private
specifier|final
name|Notifier
name|notifier
decl_stmt|;
DECL|field|shutdownLatch
specifier|private
specifier|final
name|CountDownLatch
name|shutdownLatch
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
DECL|method|PurgerThread
specifier|public
name|PurgerThread
parameter_list|(
name|String
name|name
parameter_list|,
name|TimeValue
name|interval
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|setDaemon
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|this
operator|.
name|notifier
operator|=
operator|new
name|Notifier
argument_list|(
name|interval
argument_list|)
expr_stmt|;
block|}
DECL|method|shutdown
specifier|public
name|void
name|shutdown
parameter_list|()
throws|throws
name|InterruptedException
block|{
if|if
condition|(
name|running
operator|.
name|compareAndSet
argument_list|(
literal|true
argument_list|,
literal|false
argument_list|)
condition|)
block|{
name|notifier
operator|.
name|doNotify
argument_list|()
expr_stmt|;
name|shutdownLatch
operator|.
name|await
argument_list|()
expr_stmt|;
block|}
block|}
DECL|method|resetInterval
specifier|public
name|void
name|resetInterval
parameter_list|(
name|TimeValue
name|interval
parameter_list|)
block|{
name|notifier
operator|.
name|setTimeout
argument_list|(
name|interval
argument_list|)
expr_stmt|;
block|}
DECL|method|run
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
while|while
condition|(
name|running
operator|.
name|get
argument_list|()
condition|)
block|{
try|try
block|{
name|List
argument_list|<
name|IndexShard
argument_list|>
name|shardsToPurge
init|=
name|getShardsToPurge
argument_list|()
decl_stmt|;
name|purgeShards
argument_list|(
name|shardsToPurge
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
if|if
condition|(
name|running
operator|.
name|get
argument_list|()
condition|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"failed to execute ttl purge"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|running
operator|.
name|get
argument_list|()
condition|)
block|{
name|notifier
operator|.
name|await
argument_list|()
expr_stmt|;
block|}
block|}
block|}
finally|finally
block|{
name|shutdownLatch
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**          * Returns the shards to purge, i.e. the local started primary shards that have ttl enabled and disable_purge to false          */
DECL|method|getShardsToPurge
specifier|private
name|List
argument_list|<
name|IndexShard
argument_list|>
name|getShardsToPurge
parameter_list|()
block|{
name|List
argument_list|<
name|IndexShard
argument_list|>
name|shardsToPurge
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|MetaData
name|metaData
init|=
name|clusterService
operator|.
name|state
argument_list|()
operator|.
name|metaData
argument_list|()
decl_stmt|;
for|for
control|(
name|IndexService
name|indexService
range|:
name|indicesService
control|)
block|{
comment|// check the value of disable_purge for this index
name|IndexMetaData
name|indexMetaData
init|=
name|metaData
operator|.
name|index
argument_list|(
name|indexService
operator|.
name|index
argument_list|()
operator|.
name|name
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|indexMetaData
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
name|boolean
name|disablePurge
init|=
name|indexMetaData
operator|.
name|settings
argument_list|()
operator|.
name|getAsBoolean
argument_list|(
name|INDEX_TTL_DISABLE_PURGE
argument_list|,
literal|false
argument_list|)
decl_stmt|;
if|if
condition|(
name|disablePurge
condition|)
block|{
continue|continue;
block|}
comment|// should be optimized with the hasTTL flag
name|FieldMappers
name|ttlFieldMappers
init|=
name|indexService
operator|.
name|mapperService
argument_list|()
operator|.
name|name
argument_list|(
name|TTLFieldMapper
operator|.
name|NAME
argument_list|)
decl_stmt|;
if|if
condition|(
name|ttlFieldMappers
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
comment|// check if ttl is enabled for at least one type of this index
name|boolean
name|hasTTLEnabled
init|=
literal|false
decl_stmt|;
for|for
control|(
name|FieldMapper
name|ttlFieldMapper
range|:
name|ttlFieldMappers
control|)
block|{
if|if
condition|(
operator|(
operator|(
name|TTLFieldMapper
operator|)
name|ttlFieldMapper
operator|)
operator|.
name|enabled
argument_list|()
condition|)
block|{
name|hasTTLEnabled
operator|=
literal|true
expr_stmt|;
break|break;
block|}
block|}
if|if
condition|(
name|hasTTLEnabled
condition|)
block|{
for|for
control|(
name|IndexShard
name|indexShard
range|:
name|indexService
control|)
block|{
if|if
condition|(
name|indexShard
operator|.
name|state
argument_list|()
operator|==
name|IndexShardState
operator|.
name|STARTED
operator|&&
name|indexShard
operator|.
name|routingEntry
argument_list|()
operator|.
name|primary
argument_list|()
operator|&&
name|indexShard
operator|.
name|routingEntry
argument_list|()
operator|.
name|started
argument_list|()
condition|)
block|{
name|shardsToPurge
operator|.
name|add
argument_list|(
name|indexShard
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
return|return
name|shardsToPurge
return|;
block|}
DECL|method|getInterval
specifier|public
name|TimeValue
name|getInterval
parameter_list|()
block|{
return|return
name|notifier
operator|.
name|getTimeout
argument_list|()
return|;
block|}
block|}
DECL|method|purgeShards
specifier|private
name|void
name|purgeShards
parameter_list|(
name|List
argument_list|<
name|IndexShard
argument_list|>
name|shardsToPurge
parameter_list|)
block|{
for|for
control|(
name|IndexShard
name|shardToPurge
range|:
name|shardsToPurge
control|)
block|{
name|Query
name|query
init|=
name|shardToPurge
operator|.
name|indexService
argument_list|()
operator|.
name|mapperService
argument_list|()
operator|.
name|smartNameFieldMapper
argument_list|(
name|TTLFieldMapper
operator|.
name|NAME
argument_list|)
operator|.
name|rangeQuery
argument_list|(
literal|null
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|Engine
operator|.
name|Searcher
name|searcher
init|=
name|shardToPurge
operator|.
name|acquireSearcher
argument_list|(
literal|"indices_ttl"
argument_list|)
decl_stmt|;
try|try
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"[{}][{}] purging shard"
argument_list|,
name|shardToPurge
operator|.
name|routingEntry
argument_list|()
operator|.
name|index
argument_list|()
argument_list|,
name|shardToPurge
operator|.
name|routingEntry
argument_list|()
operator|.
name|id
argument_list|()
argument_list|)
expr_stmt|;
name|ExpiredDocsCollector
name|expiredDocsCollector
init|=
operator|new
name|ExpiredDocsCollector
argument_list|()
decl_stmt|;
name|searcher
operator|.
name|searcher
argument_list|()
operator|.
name|search
argument_list|(
name|query
argument_list|,
name|expiredDocsCollector
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|DocToPurge
argument_list|>
name|docsToPurge
init|=
name|expiredDocsCollector
operator|.
name|getDocsToPurge
argument_list|()
decl_stmt|;
name|BulkRequest
name|bulkRequest
init|=
operator|new
name|BulkRequest
argument_list|()
decl_stmt|;
for|for
control|(
name|DocToPurge
name|docToPurge
range|:
name|docsToPurge
control|)
block|{
name|bulkRequest
operator|.
name|add
argument_list|(
operator|new
name|DeleteRequest
argument_list|()
operator|.
name|index
argument_list|(
name|shardToPurge
operator|.
name|routingEntry
argument_list|()
operator|.
name|index
argument_list|()
argument_list|)
operator|.
name|type
argument_list|(
name|docToPurge
operator|.
name|type
argument_list|)
operator|.
name|id
argument_list|(
name|docToPurge
operator|.
name|id
argument_list|)
operator|.
name|version
argument_list|(
name|docToPurge
operator|.
name|version
argument_list|)
operator|.
name|routing
argument_list|(
name|docToPurge
operator|.
name|routing
argument_list|)
argument_list|)
expr_stmt|;
name|bulkRequest
operator|=
name|processBulkIfNeeded
argument_list|(
name|bulkRequest
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
name|processBulkIfNeeded
argument_list|(
name|bulkRequest
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"failed to purge"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|searcher
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
DECL|class|DocToPurge
specifier|private
specifier|static
class|class
name|DocToPurge
block|{
DECL|field|type
specifier|public
specifier|final
name|String
name|type
decl_stmt|;
DECL|field|id
specifier|public
specifier|final
name|String
name|id
decl_stmt|;
DECL|field|version
specifier|public
specifier|final
name|long
name|version
decl_stmt|;
DECL|field|routing
specifier|public
specifier|final
name|String
name|routing
decl_stmt|;
DECL|method|DocToPurge
specifier|public
name|DocToPurge
parameter_list|(
name|String
name|type
parameter_list|,
name|String
name|id
parameter_list|,
name|long
name|version
parameter_list|,
name|String
name|routing
parameter_list|)
block|{
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
name|this
operator|.
name|id
operator|=
name|id
expr_stmt|;
name|this
operator|.
name|version
operator|=
name|version
expr_stmt|;
name|this
operator|.
name|routing
operator|=
name|routing
expr_stmt|;
block|}
block|}
DECL|class|ExpiredDocsCollector
specifier|private
class|class
name|ExpiredDocsCollector
extends|extends
name|SimpleCollector
block|{
DECL|field|context
specifier|private
name|LeafReaderContext
name|context
decl_stmt|;
DECL|field|docsToPurge
specifier|private
name|List
argument_list|<
name|DocToPurge
argument_list|>
name|docsToPurge
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
DECL|method|ExpiredDocsCollector
specifier|public
name|ExpiredDocsCollector
parameter_list|()
block|{         }
DECL|method|setScorer
specifier|public
name|void
name|setScorer
parameter_list|(
name|Scorer
name|scorer
parameter_list|)
block|{         }
DECL|method|acceptsDocsOutOfOrder
specifier|public
name|boolean
name|acceptsDocsOutOfOrder
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
DECL|method|collect
specifier|public
name|void
name|collect
parameter_list|(
name|int
name|doc
parameter_list|)
block|{
try|try
block|{
name|UidAndRoutingFieldsVisitor
name|fieldsVisitor
init|=
operator|new
name|UidAndRoutingFieldsVisitor
argument_list|()
decl_stmt|;
name|context
operator|.
name|reader
argument_list|()
operator|.
name|document
argument_list|(
name|doc
argument_list|,
name|fieldsVisitor
argument_list|)
expr_stmt|;
name|Uid
name|uid
init|=
name|fieldsVisitor
operator|.
name|uid
argument_list|()
decl_stmt|;
specifier|final
name|long
name|version
init|=
name|Versions
operator|.
name|loadVersion
argument_list|(
name|context
operator|.
name|reader
argument_list|()
argument_list|,
operator|new
name|Term
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|,
name|uid
operator|.
name|toBytesRef
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|docsToPurge
operator|.
name|add
argument_list|(
operator|new
name|DocToPurge
argument_list|(
name|uid
operator|.
name|type
argument_list|()
argument_list|,
name|uid
operator|.
name|id
argument_list|()
argument_list|,
name|version
argument_list|,
name|fieldsVisitor
operator|.
name|routing
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"failed to collect doc"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|doSetNextReader
specifier|public
name|void
name|doSetNextReader
parameter_list|(
name|LeafReaderContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|context
operator|=
name|context
expr_stmt|;
block|}
DECL|method|getDocsToPurge
specifier|public
name|List
argument_list|<
name|DocToPurge
argument_list|>
name|getDocsToPurge
parameter_list|()
block|{
return|return
name|this
operator|.
name|docsToPurge
return|;
block|}
block|}
DECL|method|processBulkIfNeeded
specifier|private
name|BulkRequest
name|processBulkIfNeeded
parameter_list|(
name|BulkRequest
name|bulkRequest
parameter_list|,
name|boolean
name|force
parameter_list|)
block|{
if|if
condition|(
operator|(
name|force
operator|&&
name|bulkRequest
operator|.
name|numberOfActions
argument_list|()
operator|>
literal|0
operator|)
operator|||
name|bulkRequest
operator|.
name|numberOfActions
argument_list|()
operator|>=
name|bulkSize
condition|)
block|{
try|try
block|{
name|bulkAction
operator|.
name|executeBulk
argument_list|(
name|bulkRequest
argument_list|,
operator|new
name|ActionListener
argument_list|<
name|BulkResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onResponse
parameter_list|(
name|BulkResponse
name|bulkResponse
parameter_list|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"bulk took "
operator|+
name|bulkResponse
operator|.
name|getTookInMillis
argument_list|()
operator|+
literal|"ms"
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
name|logger
operator|.
name|warn
argument_list|(
literal|"failed to execute bulk"
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"failed to process bulk"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
name|bulkRequest
operator|=
operator|new
name|BulkRequest
argument_list|()
expr_stmt|;
block|}
return|return
name|bulkRequest
return|;
block|}
DECL|class|ApplySettings
class|class
name|ApplySettings
implements|implements
name|NodeSettingsService
operator|.
name|Listener
block|{
annotation|@
name|Override
DECL|method|onRefreshSettings
specifier|public
name|void
name|onRefreshSettings
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
specifier|final
name|TimeValue
name|currentInterval
init|=
name|IndicesTTLService
operator|.
name|this
operator|.
name|purgerThread
operator|.
name|getInterval
argument_list|()
decl_stmt|;
specifier|final
name|TimeValue
name|interval
init|=
name|settings
operator|.
name|getAsTime
argument_list|(
name|INDICES_TTL_INTERVAL
argument_list|,
name|currentInterval
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|interval
operator|.
name|equals
argument_list|(
name|currentInterval
argument_list|)
condition|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"updating indices.ttl.interval from [{}] to [{}]"
argument_list|,
name|currentInterval
argument_list|,
name|interval
argument_list|)
expr_stmt|;
name|IndicesTTLService
operator|.
name|this
operator|.
name|purgerThread
operator|.
name|resetInterval
argument_list|(
name|interval
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|class|Notifier
specifier|private
specifier|static
specifier|final
class|class
name|Notifier
block|{
DECL|field|lock
specifier|private
specifier|final
name|ReentrantLock
name|lock
init|=
operator|new
name|ReentrantLock
argument_list|()
decl_stmt|;
DECL|field|condition
specifier|private
specifier|final
name|Condition
name|condition
init|=
name|lock
operator|.
name|newCondition
argument_list|()
decl_stmt|;
DECL|field|timeout
specifier|private
specifier|volatile
name|TimeValue
name|timeout
decl_stmt|;
DECL|method|Notifier
specifier|public
name|Notifier
parameter_list|(
name|TimeValue
name|timeout
parameter_list|)
block|{
assert|assert
name|timeout
operator|!=
literal|null
assert|;
name|this
operator|.
name|timeout
operator|=
name|timeout
expr_stmt|;
block|}
DECL|method|await
specifier|public
name|void
name|await
parameter_list|()
block|{
name|lock
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
name|condition
operator|.
name|await
argument_list|(
name|timeout
operator|.
name|millis
argument_list|()
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|Thread
operator|.
name|interrupted
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
name|lock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
DECL|method|setTimeout
specifier|public
name|void
name|setTimeout
parameter_list|(
name|TimeValue
name|timeout
parameter_list|)
block|{
assert|assert
name|timeout
operator|!=
literal|null
assert|;
name|this
operator|.
name|timeout
operator|=
name|timeout
expr_stmt|;
name|doNotify
argument_list|()
expr_stmt|;
block|}
DECL|method|getTimeout
specifier|public
name|TimeValue
name|getTimeout
parameter_list|()
block|{
return|return
name|timeout
return|;
block|}
DECL|method|doNotify
specifier|public
name|void
name|doNotify
parameter_list|()
block|{
name|lock
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
name|condition
operator|.
name|signalAll
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
name|lock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

