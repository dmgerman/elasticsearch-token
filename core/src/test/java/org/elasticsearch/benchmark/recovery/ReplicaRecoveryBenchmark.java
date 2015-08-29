begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.benchmark.recovery
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|benchmark
operator|.
name|recovery
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
name|admin
operator|.
name|indices
operator|.
name|recovery
operator|.
name|RecoveryResponse
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|bootstrap
operator|.
name|BootstrapForTesting
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
name|routing
operator|.
name|allocation
operator|.
name|decider
operator|.
name|DiskThresholdDecider
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
name|ESLogger
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
name|SizeValue
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
name|index
operator|.
name|IndexNotFoundException
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
name|recovery
operator|.
name|RecoveryState
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
name|Node
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|BackgroundIndexer
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
name|TransportModule
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
name|Random
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
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|metadata
operator|.
name|IndexMetaData
operator|.
name|SETTING_NUMBER_OF_REPLICAS
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|metadata
operator|.
name|IndexMetaData
operator|.
name|SETTING_NUMBER_OF_SHARDS
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|settings
operator|.
name|Settings
operator|.
name|settingsBuilder
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|node
operator|.
name|NodeBuilder
operator|.
name|nodeBuilder
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|ReplicaRecoveryBenchmark
specifier|public
class|class
name|ReplicaRecoveryBenchmark
block|{
DECL|field|INDEX_NAME
specifier|private
specifier|static
specifier|final
name|String
name|INDEX_NAME
init|=
literal|"index"
decl_stmt|;
DECL|field|TYPE_NAME
specifier|private
specifier|static
specifier|final
name|String
name|TYPE_NAME
init|=
literal|"type"
decl_stmt|;
DECL|field|DOC_COUNT
specifier|static
name|int
name|DOC_COUNT
init|=
operator|(
name|int
operator|)
name|SizeValue
operator|.
name|parseSizeValue
argument_list|(
literal|"40k"
argument_list|)
operator|.
name|singles
argument_list|()
decl_stmt|;
DECL|field|CONCURRENT_INDEXERS
specifier|static
name|int
name|CONCURRENT_INDEXERS
init|=
literal|2
decl_stmt|;
DECL|method|main
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|Exception
block|{
name|System
operator|.
name|setProperty
argument_list|(
literal|"es.logger.prefix"
argument_list|,
literal|""
argument_list|)
expr_stmt|;
name|BootstrapForTesting
operator|.
name|ensureInitialized
argument_list|()
expr_stmt|;
name|Settings
name|settings
init|=
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"gateway.type"
argument_list|,
literal|"local"
argument_list|)
operator|.
name|put
argument_list|(
name|DiskThresholdDecider
operator|.
name|CLUSTER_ROUTING_ALLOCATION_DISK_THRESHOLD_ENABLED
argument_list|,
literal|"false"
argument_list|)
operator|.
name|put
argument_list|(
name|SETTING_NUMBER_OF_SHARDS
argument_list|,
literal|1
argument_list|)
operator|.
name|put
argument_list|(
name|SETTING_NUMBER_OF_REPLICAS
argument_list|,
literal|0
argument_list|)
operator|.
name|put
argument_list|(
name|TransportModule
operator|.
name|TRANSPORT_TYPE_KEY
argument_list|,
literal|"local"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|String
name|clusterName
init|=
name|ReplicaRecoveryBenchmark
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
decl_stmt|;
name|Node
name|node1
init|=
name|nodeBuilder
argument_list|()
operator|.
name|clusterName
argument_list|(
name|clusterName
argument_list|)
operator|.
name|settings
argument_list|(
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
name|settings
argument_list|)
argument_list|)
operator|.
name|node
argument_list|()
decl_stmt|;
specifier|final
name|ESLogger
name|logger
init|=
name|ESLoggerFactory
operator|.
name|getLogger
argument_list|(
literal|"benchmark"
argument_list|)
decl_stmt|;
specifier|final
name|Client
name|client1
init|=
name|node1
operator|.
name|client
argument_list|()
decl_stmt|;
name|client1
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareUpdateSettings
argument_list|()
operator|.
name|setPersistentSettings
argument_list|(
literal|"logger.indices.recovery: TRACE"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
specifier|final
name|BackgroundIndexer
name|indexer
init|=
operator|new
name|BackgroundIndexer
argument_list|(
name|INDEX_NAME
argument_list|,
name|TYPE_NAME
argument_list|,
name|client1
argument_list|,
literal|0
argument_list|,
name|CONCURRENT_INDEXERS
argument_list|,
literal|false
argument_list|,
operator|new
name|Random
argument_list|()
argument_list|)
decl_stmt|;
name|indexer
operator|.
name|setMinFieldSize
argument_list|(
literal|10
argument_list|)
expr_stmt|;
name|indexer
operator|.
name|setMaxFieldSize
argument_list|(
literal|150
argument_list|)
expr_stmt|;
try|try
block|{
name|client1
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareDelete
argument_list|(
name|INDEX_NAME
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IndexNotFoundException
name|e
parameter_list|)
block|{         }
name|client1
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareCreate
argument_list|(
name|INDEX_NAME
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|indexer
operator|.
name|start
argument_list|(
name|DOC_COUNT
operator|/
literal|2
argument_list|)
expr_stmt|;
while|while
condition|(
name|indexer
operator|.
name|totalIndexedDocs
argument_list|()
operator|<
name|DOC_COUNT
operator|/
literal|2
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|5000
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> indexed {} of {}"
argument_list|,
name|indexer
operator|.
name|totalIndexedDocs
argument_list|()
argument_list|,
name|DOC_COUNT
argument_list|)
expr_stmt|;
block|}
name|client1
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareFlush
argument_list|()
operator|.
name|get
argument_list|()
expr_stmt|;
name|indexer
operator|.
name|continueIndexing
argument_list|(
name|DOC_COUNT
operator|/
literal|2
argument_list|)
expr_stmt|;
while|while
condition|(
name|indexer
operator|.
name|totalIndexedDocs
argument_list|()
operator|<
name|DOC_COUNT
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|5000
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> indexed {} of {}"
argument_list|,
name|indexer
operator|.
name|totalIndexedDocs
argument_list|()
argument_list|,
name|DOC_COUNT
argument_list|)
expr_stmt|;
block|}
name|logger
operator|.
name|info
argument_list|(
literal|"--> starting another node and allocating a shard on it"
argument_list|)
expr_stmt|;
name|Node
name|node2
init|=
name|nodeBuilder
argument_list|()
operator|.
name|clusterName
argument_list|(
name|clusterName
argument_list|)
operator|.
name|settings
argument_list|(
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
name|settings
argument_list|)
argument_list|)
operator|.
name|node
argument_list|()
decl_stmt|;
name|client1
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareUpdateSettings
argument_list|(
name|INDEX_NAME
argument_list|)
operator|.
name|setSettings
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_NUMBER_OF_REPLICAS
operator|+
literal|": 1"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
specifier|final
name|AtomicBoolean
name|end
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
specifier|final
name|Thread
name|backgroundLogger
init|=
operator|new
name|Thread
argument_list|(
operator|new
name|Runnable
argument_list|()
block|{
name|long
name|lastTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|long
name|lastDocs
init|=
name|indexer
operator|.
name|totalIndexedDocs
argument_list|()
decl_stmt|;
name|long
name|lastBytes
init|=
literal|0
decl_stmt|;
name|long
name|lastTranslogOps
init|=
literal|0
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
while|while
condition|(
literal|true
condition|)
block|{
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|5000
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{                      }
if|if
condition|(
name|end
operator|.
name|get
argument_list|()
condition|)
block|{
return|return;
block|}
name|long
name|currentTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|long
name|currentDocs
init|=
name|indexer
operator|.
name|totalIndexedDocs
argument_list|()
decl_stmt|;
name|RecoveryResponse
name|recoveryResponse
init|=
name|client1
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareRecoveries
argument_list|(
name|INDEX_NAME
argument_list|)
operator|.
name|setActiveOnly
argument_list|(
literal|true
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|RecoveryState
argument_list|>
name|indexRecoveries
init|=
name|recoveryResponse
operator|.
name|shardRecoveryStates
argument_list|()
operator|.
name|get
argument_list|(
name|INDEX_NAME
argument_list|)
decl_stmt|;
name|long
name|translogOps
decl_stmt|;
name|long
name|bytes
decl_stmt|;
if|if
condition|(
name|indexRecoveries
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|translogOps
operator|=
name|indexRecoveries
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getTranslog
argument_list|()
operator|.
name|recoveredOperations
argument_list|()
expr_stmt|;
name|bytes
operator|=
name|recoveryResponse
operator|.
name|shardRecoveryStates
argument_list|()
operator|.
name|get
argument_list|(
name|INDEX_NAME
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getIndex
argument_list|()
operator|.
name|recoveredBytes
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|bytes
operator|=
name|lastBytes
operator|=
literal|0
expr_stmt|;
name|translogOps
operator|=
name|lastTranslogOps
operator|=
literal|0
expr_stmt|;
block|}
name|float
name|seconds
init|=
operator|(
name|currentTime
operator|-
name|lastTime
operator|)
operator|/
literal|1000.0F
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> indexed [{}];[{}] doc/s, recovered [{}] MB/s , translog ops [{}]/s "
argument_list|,
name|currentDocs
argument_list|,
operator|(
name|currentDocs
operator|-
name|lastDocs
operator|)
operator|/
name|seconds
argument_list|,
operator|(
name|bytes
operator|-
name|lastBytes
operator|)
operator|/
literal|1024.0F
operator|/
literal|1024F
operator|/
name|seconds
argument_list|,
operator|(
name|translogOps
operator|-
name|lastTranslogOps
operator|)
operator|/
name|seconds
argument_list|)
expr_stmt|;
name|lastBytes
operator|=
name|bytes
expr_stmt|;
name|lastTranslogOps
operator|=
name|translogOps
expr_stmt|;
name|lastTime
operator|=
name|currentTime
expr_stmt|;
name|lastDocs
operator|=
name|currentDocs
expr_stmt|;
block|}
block|}
block|}
argument_list|)
decl_stmt|;
name|backgroundLogger
operator|.
name|start
argument_list|()
expr_stmt|;
name|client1
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareHealth
argument_list|()
operator|.
name|setWaitForGreenStatus
argument_list|()
operator|.
name|get
argument_list|()
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> green. starting relocation cycles"
argument_list|)
expr_stmt|;
name|long
name|startDocIndexed
init|=
name|indexer
operator|.
name|totalIndexedDocs
argument_list|()
decl_stmt|;
name|indexer
operator|.
name|continueIndexing
argument_list|(
name|DOC_COUNT
operator|*
literal|50
argument_list|)
expr_stmt|;
name|long
name|totalRecoveryTime
init|=
literal|0
decl_stmt|;
name|long
name|startTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|long
index|[]
name|recoveryTimes
init|=
operator|new
name|long
index|[
literal|3
index|]
decl_stmt|;
for|for
control|(
name|int
name|iteration
init|=
literal|0
init|;
name|iteration
operator|<
literal|3
condition|;
name|iteration
operator|++
control|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"--> removing replicas"
argument_list|)
expr_stmt|;
name|client1
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareUpdateSettings
argument_list|(
name|INDEX_NAME
argument_list|)
operator|.
name|setSettings
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_NUMBER_OF_REPLICAS
operator|+
literal|": 0"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> adding replica again"
argument_list|)
expr_stmt|;
name|long
name|recoveryStart
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|client1
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareUpdateSettings
argument_list|(
name|INDEX_NAME
argument_list|)
operator|.
name|setSettings
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_NUMBER_OF_REPLICAS
operator|+
literal|": 1"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|client1
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareHealth
argument_list|(
name|INDEX_NAME
argument_list|)
operator|.
name|setWaitForGreenStatus
argument_list|()
operator|.
name|setTimeout
argument_list|(
literal|"15m"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|long
name|recoveryTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|recoveryStart
decl_stmt|;
name|totalRecoveryTime
operator|+=
name|recoveryTime
expr_stmt|;
name|recoveryTimes
index|[
name|iteration
index|]
operator|=
name|recoveryTime
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> recovery done in [{}]"
argument_list|,
operator|new
name|TimeValue
argument_list|(
name|recoveryTime
argument_list|)
argument_list|)
expr_stmt|;
comment|// sleep some to let things clean up
name|Thread
operator|.
name|sleep
argument_list|(
literal|10000
argument_list|)
expr_stmt|;
block|}
name|long
name|endDocIndexed
init|=
name|indexer
operator|.
name|totalIndexedDocs
argument_list|()
decl_stmt|;
name|long
name|totalTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|startTime
decl_stmt|;
name|indexer
operator|.
name|stop
argument_list|()
expr_stmt|;
name|end
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|backgroundLogger
operator|.
name|interrupt
argument_list|()
expr_stmt|;
name|backgroundLogger
operator|.
name|join
argument_list|()
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"average doc/s [{}], average relocation time [{}], taking [{}], [{}], [{}]"
argument_list|,
operator|(
name|endDocIndexed
operator|-
name|startDocIndexed
operator|)
operator|*
literal|1000.0
operator|/
name|totalTime
argument_list|,
operator|new
name|TimeValue
argument_list|(
name|totalRecoveryTime
operator|/
literal|3
argument_list|)
argument_list|,
name|TimeValue
operator|.
name|timeValueMillis
argument_list|(
name|recoveryTimes
index|[
literal|0
index|]
argument_list|)
argument_list|,
name|TimeValue
operator|.
name|timeValueMillis
argument_list|(
name|recoveryTimes
index|[
literal|1
index|]
argument_list|)
argument_list|,
name|TimeValue
operator|.
name|timeValueMillis
argument_list|(
name|recoveryTimes
index|[
literal|2
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|client1
operator|.
name|close
argument_list|()
expr_stmt|;
name|node1
operator|.
name|close
argument_list|()
expr_stmt|;
name|node2
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

