begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.indices.recovery
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|recovery
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
name|analysis
operator|.
name|Analyzer
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
name|analysis
operator|.
name|Tokenizer
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
name|FlushRequest
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
name|xcontent
operator|.
name|XContentBuilder
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
name|index
operator|.
name|analysis
operator|.
name|AnalyzerProvider
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
name|analysis
operator|.
name|AnalyzerScope
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
name|indices
operator|.
name|analysis
operator|.
name|AnalysisModule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|plugins
operator|.
name|AnalysisPlugin
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|plugins
operator|.
name|Plugin
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
name|ESIntegTestCase
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
name|InternalTestCluster
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
name|junit
operator|.
name|annotations
operator|.
name|TestLogging
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
name|Collection
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
name|atomic
operator|.
name|AtomicReference
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
name|xcontent
operator|.
name|XContentFactory
operator|.
name|jsonBuilder
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|equalTo
import|;
end_import

begin_class
annotation|@
name|ESIntegTestCase
operator|.
name|ClusterScope
argument_list|(
name|scope
operator|=
name|ESIntegTestCase
operator|.
name|Scope
operator|.
name|TEST
argument_list|,
name|numDataNodes
operator|=
literal|0
argument_list|)
DECL|class|EvilPeerRecoveryIT
specifier|public
class|class
name|EvilPeerRecoveryIT
extends|extends
name|ESIntegTestCase
block|{
DECL|field|indexLatch
specifier|private
specifier|static
name|AtomicReference
argument_list|<
name|CountDownLatch
argument_list|>
name|indexLatch
init|=
operator|new
name|AtomicReference
argument_list|<>
argument_list|()
decl_stmt|;
DECL|field|waitForOpsToCompleteLatch
specifier|private
specifier|static
name|AtomicReference
argument_list|<
name|CountDownLatch
argument_list|>
name|waitForOpsToCompleteLatch
init|=
operator|new
name|AtomicReference
argument_list|<>
argument_list|()
decl_stmt|;
annotation|@
name|Override
DECL|method|nodePlugins
specifier|protected
name|Collection
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|Plugin
argument_list|>
argument_list|>
name|nodePlugins
parameter_list|()
block|{
return|return
name|Collections
operator|.
name|singletonList
argument_list|(
name|LatchAnalysisPlugin
operator|.
name|class
argument_list|)
return|;
block|}
DECL|class|LatchAnalysisPlugin
specifier|public
specifier|static
class|class
name|LatchAnalysisPlugin
extends|extends
name|Plugin
implements|implements
name|AnalysisPlugin
block|{
annotation|@
name|Override
DECL|method|getAnalyzers
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|AnalysisModule
operator|.
name|AnalysisProvider
argument_list|<
name|AnalyzerProvider
argument_list|<
name|?
extends|extends
name|Analyzer
argument_list|>
argument_list|>
argument_list|>
name|getAnalyzers
parameter_list|()
block|{
return|return
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"latch_analyzer"
argument_list|,
parameter_list|(
name|a
parameter_list|,
name|b
parameter_list|,
name|c
parameter_list|,
name|d
parameter_list|)
lambda|->
operator|new
name|LatchAnalyzerProvider
argument_list|()
argument_list|)
return|;
block|}
block|}
DECL|class|LatchAnalyzerProvider
specifier|static
class|class
name|LatchAnalyzerProvider
implements|implements
name|AnalyzerProvider
argument_list|<
name|LatchAnalyzer
argument_list|>
block|{
annotation|@
name|Override
DECL|method|name
specifier|public
name|String
name|name
parameter_list|()
block|{
return|return
literal|"latch_analyzer"
return|;
block|}
annotation|@
name|Override
DECL|method|scope
specifier|public
name|AnalyzerScope
name|scope
parameter_list|()
block|{
return|return
name|AnalyzerScope
operator|.
name|INDICES
return|;
block|}
annotation|@
name|Override
DECL|method|get
specifier|public
name|LatchAnalyzer
name|get
parameter_list|()
block|{
return|return
operator|new
name|LatchAnalyzer
argument_list|()
return|;
block|}
block|}
DECL|class|LatchAnalyzer
specifier|static
class|class
name|LatchAnalyzer
extends|extends
name|Analyzer
block|{
annotation|@
name|Override
DECL|method|createComponents
specifier|protected
name|TokenStreamComponents
name|createComponents
parameter_list|(
specifier|final
name|String
name|fieldName
parameter_list|)
block|{
return|return
operator|new
name|TokenStreamComponents
argument_list|(
operator|new
name|LatchTokenizer
argument_list|()
argument_list|)
return|;
block|}
block|}
DECL|class|LatchTokenizer
specifier|static
class|class
name|LatchTokenizer
extends|extends
name|Tokenizer
block|{
annotation|@
name|Override
DECL|method|incrementToken
specifier|public
specifier|final
name|boolean
name|incrementToken
parameter_list|()
throws|throws
name|IOException
block|{
try|try
block|{
if|if
condition|(
name|indexLatch
operator|.
name|get
argument_list|()
operator|!=
literal|null
condition|)
block|{
comment|// latch that all exected operations are in the engine
name|indexLatch
operator|.
name|get
argument_list|()
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|waitForOpsToCompleteLatch
operator|.
name|get
argument_list|()
operator|!=
literal|null
condition|)
block|{
comment|// latch that waits for the replica to restart and allows recovery to proceed
name|waitForOpsToCompleteLatch
operator|.
name|get
argument_list|()
operator|.
name|await
argument_list|()
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
specifier|final
name|InterruptedException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
return|return
literal|false
return|;
block|}
block|}
comment|/*      * This tests that sequence-number-based recoveries wait for in-flight operations to complete. The trick here is simple. We latch some      * in-flight operations inside the engine after sequence numbers are assigned. While these operations are latched, we restart a replica.      * Sequence-number-based recovery on this replica has to wait until these in-flight operations complete to proceed. We verify at the end      * of recovery that a file-based recovery was not completed, and that the expected number of operations was replayed via the translog.      */
annotation|@
name|TestLogging
argument_list|(
literal|"_root:DEBUG,org.elasticsearch.action.bulk:TRACE,org.elasticsearch.action.get:TRACE,discovery:TRACE,"
operator|+
literal|"org.elasticsearch.cluster.service:TRACE,org.elasticsearch.indices.recovery:TRACE,"
operator|+
literal|"org.elasticsearch.indices.cluster:TRACE,org.elasticsearch.index.shard:TRACE"
argument_list|)
annotation|@
name|AwaitsFix
argument_list|(
name|bugUrl
operator|=
literal|"boaz is looking into failures: https://elasticsearch-ci.elastic.co/job/elastic+elasticsearch+master+java9-periodic/1545"
argument_list|)
DECL|method|testRecoveryWaitsForOps
specifier|public
name|void
name|testRecoveryWaitsForOps
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|int
name|docs
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|64
argument_list|)
decl_stmt|;
try|try
block|{
name|internalCluster
argument_list|()
operator|.
name|startMasterOnlyNode
argument_list|()
expr_stmt|;
specifier|final
name|String
name|primaryNode
init|=
name|internalCluster
argument_list|()
operator|.
name|startDataOnlyNode
argument_list|(
name|nodeSettings
argument_list|(
literal|0
argument_list|)
argument_list|)
decl_stmt|;
comment|// prepare mapping that uses our latch analyzer
specifier|final
name|XContentBuilder
name|mapping
init|=
name|jsonBuilder
argument_list|()
decl_stmt|;
name|mapping
operator|.
name|startObject
argument_list|()
expr_stmt|;
block|{
name|mapping
operator|.
name|startObject
argument_list|(
literal|"type"
argument_list|)
expr_stmt|;
block|{
name|mapping
operator|.
name|startObject
argument_list|(
literal|"properties"
argument_list|)
expr_stmt|;
block|{
name|mapping
operator|.
name|startObject
argument_list|(
literal|"foo"
argument_list|)
expr_stmt|;
block|{
name|mapping
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"text"
argument_list|)
expr_stmt|;
name|mapping
operator|.
name|field
argument_list|(
literal|"analyzer"
argument_list|,
literal|"latch_analyzer"
argument_list|)
expr_stmt|;
name|mapping
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
name|mapping
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
name|mapping
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
name|mapping
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
comment|// create the index with our mapping
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareCreate
argument_list|(
literal|"index"
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"type"
argument_list|,
name|mapping
argument_list|)
operator|.
name|setSettings
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"number_of_shards"
argument_list|,
literal|1
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
comment|// start the replica node; we do this after creating the index so we can control which node is holds the primary shard
specifier|final
name|String
name|replicaNode
init|=
name|internalCluster
argument_list|()
operator|.
name|startDataOnlyNode
argument_list|(
name|nodeSettings
argument_list|(
literal|1
argument_list|)
argument_list|)
decl_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
comment|// index some documents so that the replica will attempt a sequence-number-based recovery upon restart
for|for
control|(
name|int
name|foo
init|=
literal|0
init|;
name|foo
operator|<
name|docs
condition|;
name|foo
operator|++
control|)
block|{
name|index
argument_list|(
name|randomFrom
argument_list|(
name|primaryNode
argument_list|,
name|replicaNode
argument_list|)
argument_list|,
name|foo
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|flush
argument_list|(
operator|new
name|FlushRequest
argument_list|()
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
comment|// start some in-flight operations that will get latched in the engine
specifier|final
name|List
argument_list|<
name|Thread
argument_list|>
name|threads
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
specifier|final
name|int
name|latchedDocs
init|=
name|internalCluster
argument_list|()
operator|.
name|getInstance
argument_list|(
name|ThreadPool
operator|.
name|class
argument_list|,
name|replicaNode
argument_list|)
operator|.
name|info
argument_list|(
name|ThreadPool
operator|.
name|Names
operator|.
name|BULK
argument_list|)
operator|.
name|getMax
argument_list|()
decl_stmt|;
name|indexLatch
operator|.
name|set
argument_list|(
operator|new
name|CountDownLatch
argument_list|(
name|latchedDocs
argument_list|)
argument_list|)
expr_stmt|;
name|waitForOpsToCompleteLatch
operator|.
name|set
argument_list|(
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
name|docs
init|;
name|i
operator|<
name|docs
operator|+
name|latchedDocs
condition|;
name|i
operator|++
control|)
block|{
specifier|final
name|int
name|foo
init|=
name|i
decl_stmt|;
comment|// we have to index through the primary since we are going to restart the replica
specifier|final
name|Thread
name|thread
init|=
operator|new
name|Thread
argument_list|(
parameter_list|()
lambda|->
name|index
argument_list|(
name|primaryNode
argument_list|,
name|foo
argument_list|)
argument_list|)
decl_stmt|;
name|threads
operator|.
name|add
argument_list|(
name|thread
argument_list|)
expr_stmt|;
name|thread
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
comment|// latch until all operations are inside the engine
name|indexLatch
operator|.
name|get
argument_list|()
operator|.
name|await
argument_list|()
expr_stmt|;
name|internalCluster
argument_list|()
operator|.
name|restartNode
argument_list|(
name|replicaNode
argument_list|,
operator|new
name|InternalTestCluster
operator|.
name|RestartCallback
argument_list|()
argument_list|)
expr_stmt|;
specifier|final
name|Index
name|index
init|=
name|resolveIndex
argument_list|(
literal|"index"
argument_list|)
decl_stmt|;
comment|// wait until recovery starts
name|assertBusy
argument_list|(
parameter_list|()
lambda|->
block|{
specifier|final
name|IndicesService
name|primaryService
init|=
name|internalCluster
argument_list|()
operator|.
name|getInstance
argument_list|(
name|IndicesService
operator|.
name|class
argument_list|,
name|primaryNode
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|primaryService
operator|.
name|indexServiceSafe
argument_list|(
name|index
argument_list|)
operator|.
name|getShard
argument_list|(
literal|0
argument_list|)
operator|.
name|recoveryStats
argument_list|()
operator|.
name|currentAsSource
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
specifier|final
name|IndicesService
name|replicaService
init|=
name|internalCluster
argument_list|()
operator|.
name|getInstance
argument_list|(
name|IndicesService
operator|.
name|class
argument_list|,
name|replicaNode
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|replicaService
operator|.
name|indexServiceSafe
argument_list|(
name|index
argument_list|)
operator|.
name|getShard
argument_list|(
literal|0
argument_list|)
operator|.
name|recoveryStats
argument_list|()
operator|.
name|currentAsTarget
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
comment|// unlatch the operations that are latched inside the engine
name|waitForOpsToCompleteLatch
operator|.
name|get
argument_list|()
operator|.
name|countDown
argument_list|()
expr_stmt|;
for|for
control|(
specifier|final
name|Thread
name|thread
range|:
name|threads
control|)
block|{
name|thread
operator|.
name|join
argument_list|()
expr_stmt|;
block|}
comment|// recovery should complete successfully
name|ensureGreen
argument_list|()
expr_stmt|;
comment|// verify that a sequence-number-based recovery was completed
specifier|final
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
name|response
init|=
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareRecoveries
argument_list|(
literal|"index"
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
specifier|final
name|List
argument_list|<
name|RecoveryState
argument_list|>
name|states
init|=
name|response
operator|.
name|shardRecoveryStates
argument_list|()
operator|.
name|get
argument_list|(
literal|"index"
argument_list|)
decl_stmt|;
for|for
control|(
specifier|final
name|RecoveryState
name|state
range|:
name|states
control|)
block|{
if|if
condition|(
name|state
operator|.
name|getTargetNode
argument_list|()
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
name|replicaNode
argument_list|)
condition|)
block|{
name|assertThat
argument_list|(
name|state
operator|.
name|getTranslog
argument_list|()
operator|.
name|recoveredOperations
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|latchedDocs
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|state
operator|.
name|getIndex
argument_list|()
operator|.
name|recoveredFilesPercent
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0f
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
finally|finally
block|{
name|internalCluster
argument_list|()
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
DECL|method|index
specifier|private
name|void
name|index
parameter_list|(
specifier|final
name|String
name|node
parameter_list|,
specifier|final
name|int
name|foo
parameter_list|)
block|{
name|client
argument_list|(
name|node
argument_list|)
operator|.
name|prepareIndex
argument_list|(
literal|"index"
argument_list|,
literal|"type"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"{\"foo\":\""
operator|+
name|Integer
operator|.
name|toString
argument_list|(
name|foo
argument_list|)
operator|+
literal|"\"}"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit
