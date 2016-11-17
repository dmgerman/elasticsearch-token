begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.discovery.zen
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|discovery
operator|.
name|zen
package|;
end_package

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
name|Version
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
name|ClusterName
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
name|discovery
operator|.
name|zen
operator|.
name|PendingClusterStatesQueue
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|discovery
operator|.
name|zen
operator|.
name|PendingClusterStatesQueue
operator|.
name|ClusterStateContext
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
name|ESTestCase
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
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
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
import|import static
name|java
operator|.
name|util
operator|.
name|Collections
operator|.
name|emptyMap
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|util
operator|.
name|Collections
operator|.
name|emptySet
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
name|containsString
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
name|empty
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

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|hasKey
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
name|notNullValue
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
name|nullValue
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
name|sameInstance
import|;
end_import

begin_class
DECL|class|PendingClusterStatesQueueTests
specifier|public
class|class
name|PendingClusterStatesQueueTests
extends|extends
name|ESTestCase
block|{
DECL|method|testSelectNextStateToProcess_empty
specifier|public
name|void
name|testSelectNextStateToProcess_empty
parameter_list|()
block|{
name|PendingClusterStatesQueue
name|queue
init|=
operator|new
name|PendingClusterStatesQueue
argument_list|(
name|logger
argument_list|,
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|200
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|queue
operator|.
name|getNextClusterStateToProcess
argument_list|()
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testDroppingStatesAtCapacity
specifier|public
name|void
name|testDroppingStatesAtCapacity
parameter_list|()
block|{
name|List
argument_list|<
name|ClusterState
argument_list|>
name|states
init|=
name|randomStates
argument_list|(
name|scaledRandomIntBetween
argument_list|(
literal|10
argument_list|,
literal|300
argument_list|)
argument_list|,
literal|"master1"
argument_list|,
literal|"master2"
argument_list|,
literal|"master3"
argument_list|,
literal|"master4"
argument_list|)
decl_stmt|;
name|Collections
operator|.
name|shuffle
argument_list|(
name|states
argument_list|,
name|random
argument_list|()
argument_list|)
expr_stmt|;
comment|// insert half of the states
specifier|final
name|int
name|numberOfStateToDrop
init|=
name|states
operator|.
name|size
argument_list|()
operator|/
literal|2
decl_stmt|;
name|List
argument_list|<
name|ClusterState
argument_list|>
name|stateToDrop
init|=
name|states
operator|.
name|subList
argument_list|(
literal|0
argument_list|,
name|numberOfStateToDrop
argument_list|)
decl_stmt|;
specifier|final
name|int
name|queueSize
init|=
name|states
operator|.
name|size
argument_list|()
operator|-
name|numberOfStateToDrop
decl_stmt|;
name|PendingClusterStatesQueue
name|queue
init|=
name|createQueueWithStates
argument_list|(
name|stateToDrop
argument_list|,
name|queueSize
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|ClusterStateContext
argument_list|>
name|committedContexts
init|=
name|randomCommitStates
argument_list|(
name|queue
argument_list|)
decl_stmt|;
for|for
control|(
name|ClusterState
name|state
range|:
name|states
operator|.
name|subList
argument_list|(
name|numberOfStateToDrop
argument_list|,
name|states
operator|.
name|size
argument_list|()
argument_list|)
control|)
block|{
name|queue
operator|.
name|addPending
argument_list|(
name|state
argument_list|)
expr_stmt|;
block|}
name|assertThat
argument_list|(
name|queue
operator|.
name|pendingClusterStates
argument_list|()
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
name|queueSize
argument_list|)
argument_list|)
expr_stmt|;
comment|// check all committed states got a failure due to the drop
for|for
control|(
name|ClusterStateContext
name|context
range|:
name|committedContexts
control|)
block|{
name|assertThat
argument_list|(
operator|(
operator|(
name|MockListener
operator|)
name|context
operator|.
name|listener
operator|)
operator|.
name|failure
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// all states that should have dropped are indeed dropped.
for|for
control|(
name|ClusterState
name|state
range|:
name|stateToDrop
control|)
block|{
name|assertThat
argument_list|(
name|queue
operator|.
name|findState
argument_list|(
name|state
operator|.
name|stateUUID
argument_list|()
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testSimpleQueueSameMaster
specifier|public
name|void
name|testSimpleQueueSameMaster
parameter_list|()
block|{
specifier|final
name|int
name|numUpdates
init|=
name|scaledRandomIntBetween
argument_list|(
literal|50
argument_list|,
literal|100
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|ClusterState
argument_list|>
name|states
init|=
name|randomStates
argument_list|(
name|numUpdates
argument_list|,
literal|"master"
argument_list|)
decl_stmt|;
name|Collections
operator|.
name|shuffle
argument_list|(
name|states
argument_list|,
name|random
argument_list|()
argument_list|)
expr_stmt|;
name|PendingClusterStatesQueue
name|queue
decl_stmt|;
name|queue
operator|=
name|createQueueWithStates
argument_list|(
name|states
argument_list|)
expr_stmt|;
comment|// no state is committed yet
name|assertThat
argument_list|(
name|queue
operator|.
name|getNextClusterStateToProcess
argument_list|()
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|ClusterState
name|highestCommitted
init|=
literal|null
decl_stmt|;
for|for
control|(
name|ClusterStateContext
name|context
range|:
name|randomCommitStates
argument_list|(
name|queue
argument_list|)
control|)
block|{
if|if
condition|(
name|highestCommitted
operator|==
literal|null
operator|||
name|context
operator|.
name|state
operator|.
name|supersedes
argument_list|(
name|highestCommitted
argument_list|)
condition|)
block|{
name|highestCommitted
operator|=
name|context
operator|.
name|state
expr_stmt|;
block|}
block|}
name|assertThat
argument_list|(
name|queue
operator|.
name|getNextClusterStateToProcess
argument_list|()
argument_list|,
name|sameInstance
argument_list|(
name|highestCommitted
argument_list|)
argument_list|)
expr_stmt|;
name|queue
operator|.
name|markAsProcessed
argument_list|(
name|highestCommitted
argument_list|)
expr_stmt|;
comment|// now there is nothing more to process
name|assertThat
argument_list|(
name|queue
operator|.
name|getNextClusterStateToProcess
argument_list|()
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testProcessedStateCleansStatesFromOtherMasters
specifier|public
name|void
name|testProcessedStateCleansStatesFromOtherMasters
parameter_list|()
block|{
name|List
argument_list|<
name|ClusterState
argument_list|>
name|states
init|=
name|randomStates
argument_list|(
name|scaledRandomIntBetween
argument_list|(
literal|10
argument_list|,
literal|300
argument_list|)
argument_list|,
literal|"master1"
argument_list|,
literal|"master2"
argument_list|,
literal|"master3"
argument_list|,
literal|"master4"
argument_list|)
decl_stmt|;
name|PendingClusterStatesQueue
name|queue
init|=
name|createQueueWithStates
argument_list|(
name|states
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|ClusterStateContext
argument_list|>
name|committedContexts
init|=
name|randomCommitStates
argument_list|(
name|queue
argument_list|)
decl_stmt|;
name|ClusterState
name|randomCommitted
init|=
name|randomFrom
argument_list|(
name|committedContexts
argument_list|)
operator|.
name|state
decl_stmt|;
name|queue
operator|.
name|markAsProcessed
argument_list|(
name|randomCommitted
argument_list|)
expr_stmt|;
specifier|final
name|String
name|processedMaster
init|=
name|randomCommitted
operator|.
name|nodes
argument_list|()
operator|.
name|getMasterNodeId
argument_list|()
decl_stmt|;
comment|// now check that queue doesn't contain anything pending from another master
for|for
control|(
name|ClusterStateContext
name|context
range|:
name|queue
operator|.
name|pendingStates
control|)
block|{
specifier|final
name|String
name|pendingMaster
init|=
name|context
operator|.
name|state
operator|.
name|nodes
argument_list|()
operator|.
name|getMasterNodeId
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
literal|"found a cluster state from ["
operator|+
name|pendingMaster
operator|+
literal|"], after a state from ["
operator|+
name|processedMaster
operator|+
literal|"] was processed"
argument_list|,
name|pendingMaster
argument_list|,
name|equalTo
argument_list|(
name|processedMaster
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// and check all committed contexts from another master were failed
for|for
control|(
name|ClusterStateContext
name|context
range|:
name|committedContexts
control|)
block|{
if|if
condition|(
name|context
operator|.
name|state
operator|.
name|nodes
argument_list|()
operator|.
name|getMasterNodeId
argument_list|()
operator|.
name|equals
argument_list|(
name|processedMaster
argument_list|)
operator|==
literal|false
condition|)
block|{
name|assertThat
argument_list|(
operator|(
operator|(
name|MockListener
operator|)
name|context
operator|.
name|listener
operator|)
operator|.
name|failure
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|testFailedStateCleansSupersededStatesOnly
specifier|public
name|void
name|testFailedStateCleansSupersededStatesOnly
parameter_list|()
block|{
name|List
argument_list|<
name|ClusterState
argument_list|>
name|states
init|=
name|randomStates
argument_list|(
name|scaledRandomIntBetween
argument_list|(
literal|10
argument_list|,
literal|50
argument_list|)
argument_list|,
literal|"master1"
argument_list|,
literal|"master2"
argument_list|,
literal|"master3"
argument_list|,
literal|"master4"
argument_list|)
decl_stmt|;
name|PendingClusterStatesQueue
name|queue
init|=
name|createQueueWithStates
argument_list|(
name|states
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|ClusterStateContext
argument_list|>
name|committedContexts
init|=
name|randomCommitStates
argument_list|(
name|queue
argument_list|)
decl_stmt|;
name|ClusterState
name|toFail
init|=
name|randomFrom
argument_list|(
name|committedContexts
argument_list|)
operator|.
name|state
decl_stmt|;
name|queue
operator|.
name|markAsFailed
argument_list|(
name|toFail
argument_list|,
operator|new
name|ElasticsearchException
argument_list|(
literal|"boo!"
argument_list|)
argument_list|)
expr_stmt|;
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|ClusterStateContext
argument_list|>
name|committedContextsById
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|ClusterStateContext
name|context
range|:
name|committedContexts
control|)
block|{
name|committedContextsById
operator|.
name|put
argument_list|(
name|context
operator|.
name|stateUUID
argument_list|()
argument_list|,
name|context
argument_list|)
expr_stmt|;
block|}
comment|// now check that queue doesn't contain superseded states
for|for
control|(
name|ClusterStateContext
name|context
range|:
name|queue
operator|.
name|pendingStates
control|)
block|{
if|if
condition|(
name|context
operator|.
name|committed
argument_list|()
condition|)
block|{
name|assertFalse
argument_list|(
literal|"found a committed cluster state, which is superseded by a failed state.\nFound:"
operator|+
name|context
operator|.
name|state
operator|+
literal|"\nfailed:"
operator|+
name|toFail
argument_list|,
name|toFail
operator|.
name|supersedes
argument_list|(
name|context
operator|.
name|state
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|// check no state has been erroneously removed
for|for
control|(
name|ClusterState
name|state
range|:
name|states
control|)
block|{
name|ClusterStateContext
name|pendingContext
init|=
name|queue
operator|.
name|findState
argument_list|(
name|state
operator|.
name|stateUUID
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|pendingContext
operator|!=
literal|null
condition|)
block|{
continue|continue;
block|}
if|if
condition|(
name|state
operator|.
name|equals
argument_list|(
name|toFail
argument_list|)
condition|)
block|{
continue|continue;
block|}
name|assertThat
argument_list|(
literal|"non-committed states should never be removed"
argument_list|,
name|committedContextsById
argument_list|,
name|hasKey
argument_list|(
name|state
operator|.
name|stateUUID
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
specifier|final
name|ClusterStateContext
name|context
init|=
name|committedContextsById
operator|.
name|get
argument_list|(
name|state
operator|.
name|stateUUID
argument_list|()
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
literal|"removed state is not superseded by failed state. \nRemoved state:"
operator|+
name|context
operator|+
literal|"\nfailed: "
operator|+
name|toFail
argument_list|,
name|toFail
operator|.
name|supersedes
argument_list|(
name|context
operator|.
name|state
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"removed state was failed with wrong exception"
argument_list|,
operator|(
operator|(
name|MockListener
operator|)
name|context
operator|.
name|listener
operator|)
operator|.
name|failure
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"removed state was failed with wrong exception"
argument_list|,
operator|(
operator|(
name|MockListener
operator|)
name|context
operator|.
name|listener
operator|)
operator|.
name|failure
operator|.
name|getMessage
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"boo"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testFailAllAndClear
specifier|public
name|void
name|testFailAllAndClear
parameter_list|()
block|{
name|List
argument_list|<
name|ClusterState
argument_list|>
name|states
init|=
name|randomStates
argument_list|(
name|scaledRandomIntBetween
argument_list|(
literal|10
argument_list|,
literal|50
argument_list|)
argument_list|,
literal|"master1"
argument_list|,
literal|"master2"
argument_list|,
literal|"master3"
argument_list|,
literal|"master4"
argument_list|)
decl_stmt|;
name|PendingClusterStatesQueue
name|queue
init|=
name|createQueueWithStates
argument_list|(
name|states
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|ClusterStateContext
argument_list|>
name|committedContexts
init|=
name|randomCommitStates
argument_list|(
name|queue
argument_list|)
decl_stmt|;
name|queue
operator|.
name|failAllStatesAndClear
argument_list|(
operator|new
name|ElasticsearchException
argument_list|(
literal|"boo!"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|queue
operator|.
name|pendingStates
argument_list|,
name|empty
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|queue
operator|.
name|getNextClusterStateToProcess
argument_list|()
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|ClusterStateContext
name|context
range|:
name|committedContexts
control|)
block|{
name|assertThat
argument_list|(
literal|"state was failed with wrong exception"
argument_list|,
operator|(
operator|(
name|MockListener
operator|)
name|context
operator|.
name|listener
operator|)
operator|.
name|failure
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"state was failed with wrong exception"
argument_list|,
operator|(
operator|(
name|MockListener
operator|)
name|context
operator|.
name|listener
operator|)
operator|.
name|failure
operator|.
name|getMessage
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"boo"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testQueueStats
specifier|public
name|void
name|testQueueStats
parameter_list|()
block|{
name|List
argument_list|<
name|ClusterState
argument_list|>
name|states
init|=
name|randomStates
argument_list|(
name|scaledRandomIntBetween
argument_list|(
literal|10
argument_list|,
literal|100
argument_list|)
argument_list|,
literal|"master"
argument_list|)
decl_stmt|;
name|PendingClusterStatesQueue
name|queue
init|=
name|createQueueWithStates
argument_list|(
name|states
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|queue
operator|.
name|stats
argument_list|()
operator|.
name|getTotal
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|states
operator|.
name|size
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|queue
operator|.
name|stats
argument_list|()
operator|.
name|getPending
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|states
operator|.
name|size
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|queue
operator|.
name|stats
argument_list|()
operator|.
name|getCommitted
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|ClusterStateContext
argument_list|>
name|committedContexts
init|=
name|randomCommitStates
argument_list|(
name|queue
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|queue
operator|.
name|stats
argument_list|()
operator|.
name|getTotal
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|states
operator|.
name|size
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|queue
operator|.
name|stats
argument_list|()
operator|.
name|getPending
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|states
operator|.
name|size
argument_list|()
operator|-
name|committedContexts
operator|.
name|size
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|queue
operator|.
name|stats
argument_list|()
operator|.
name|getCommitted
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|committedContexts
operator|.
name|size
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|ClusterState
name|highestCommitted
init|=
literal|null
decl_stmt|;
for|for
control|(
name|ClusterStateContext
name|context
range|:
name|committedContexts
control|)
block|{
if|if
condition|(
name|highestCommitted
operator|==
literal|null
operator|||
name|context
operator|.
name|state
operator|.
name|supersedes
argument_list|(
name|highestCommitted
argument_list|)
condition|)
block|{
name|highestCommitted
operator|=
name|context
operator|.
name|state
expr_stmt|;
block|}
block|}
assert|assert
name|highestCommitted
operator|!=
literal|null
assert|;
name|queue
operator|.
name|markAsProcessed
argument_list|(
name|highestCommitted
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
name|long
operator|)
name|queue
operator|.
name|stats
argument_list|()
operator|.
name|getTotal
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|states
operator|.
name|size
argument_list|()
operator|-
operator|(
literal|1
operator|+
name|highestCommitted
operator|.
name|version
argument_list|()
operator|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
name|long
operator|)
name|queue
operator|.
name|stats
argument_list|()
operator|.
name|getPending
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|states
operator|.
name|size
argument_list|()
operator|-
operator|(
literal|1
operator|+
name|highestCommitted
operator|.
name|version
argument_list|()
operator|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|queue
operator|.
name|stats
argument_list|()
operator|.
name|getCommitted
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|randomCommitStates
specifier|protected
name|List
argument_list|<
name|ClusterStateContext
argument_list|>
name|randomCommitStates
parameter_list|(
name|PendingClusterStatesQueue
name|queue
parameter_list|)
block|{
name|List
argument_list|<
name|ClusterStateContext
argument_list|>
name|committedContexts
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|iter
init|=
name|randomInt
argument_list|(
name|queue
operator|.
name|pendingStates
operator|.
name|size
argument_list|()
operator|-
literal|1
argument_list|)
init|;
name|iter
operator|>=
literal|0
condition|;
name|iter
operator|--
control|)
block|{
name|ClusterState
name|state
init|=
name|queue
operator|.
name|markAsCommitted
argument_list|(
name|randomFrom
argument_list|(
name|queue
operator|.
name|pendingStates
argument_list|)
operator|.
name|stateUUID
argument_list|()
argument_list|,
operator|new
name|MockListener
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|state
operator|!=
literal|null
condition|)
block|{
comment|// null cluster state means we committed twice
name|committedContexts
operator|.
name|add
argument_list|(
name|queue
operator|.
name|findState
argument_list|(
name|state
operator|.
name|stateUUID
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|committedContexts
return|;
block|}
DECL|method|createQueueWithStates
name|PendingClusterStatesQueue
name|createQueueWithStates
parameter_list|(
name|List
argument_list|<
name|ClusterState
argument_list|>
name|states
parameter_list|)
block|{
return|return
name|createQueueWithStates
argument_list|(
name|states
argument_list|,
name|states
operator|.
name|size
argument_list|()
operator|*
literal|2
argument_list|)
return|;
comment|// we don't care about limits (there are dedicated tests for that)
block|}
DECL|method|createQueueWithStates
name|PendingClusterStatesQueue
name|createQueueWithStates
parameter_list|(
name|List
argument_list|<
name|ClusterState
argument_list|>
name|states
parameter_list|,
name|int
name|maxQueueSize
parameter_list|)
block|{
name|PendingClusterStatesQueue
name|queue
decl_stmt|;
name|queue
operator|=
operator|new
name|PendingClusterStatesQueue
argument_list|(
name|logger
argument_list|,
name|maxQueueSize
argument_list|)
expr_stmt|;
for|for
control|(
name|ClusterState
name|state
range|:
name|states
control|)
block|{
name|queue
operator|.
name|addPending
argument_list|(
name|state
argument_list|)
expr_stmt|;
block|}
return|return
name|queue
return|;
block|}
DECL|method|randomStates
name|List
argument_list|<
name|ClusterState
argument_list|>
name|randomStates
parameter_list|(
name|int
name|count
parameter_list|,
name|String
modifier|...
name|masters
parameter_list|)
block|{
name|ArrayList
argument_list|<
name|ClusterState
argument_list|>
name|states
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|count
argument_list|)
decl_stmt|;
name|ClusterState
index|[]
name|lastClusterStatePerMaster
init|=
operator|new
name|ClusterState
index|[
name|masters
operator|.
name|length
index|]
decl_stmt|;
for|for
control|(
init|;
name|count
operator|>
literal|0
condition|;
name|count
operator|--
control|)
block|{
name|int
name|masterIndex
init|=
name|randomInt
argument_list|(
name|masters
operator|.
name|length
operator|-
literal|1
argument_list|)
decl_stmt|;
name|ClusterState
name|state
init|=
name|lastClusterStatePerMaster
index|[
name|masterIndex
index|]
decl_stmt|;
if|if
condition|(
name|state
operator|==
literal|null
condition|)
block|{
name|state
operator|=
name|ClusterState
operator|.
name|builder
argument_list|(
name|ClusterName
operator|.
name|CLUSTER_NAME_SETTING
operator|.
name|getDefault
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
argument_list|)
operator|.
name|nodes
argument_list|(
name|DiscoveryNodes
operator|.
name|builder
argument_list|()
operator|.
name|add
argument_list|(
operator|new
name|DiscoveryNode
argument_list|(
name|masters
index|[
name|masterIndex
index|]
argument_list|,
name|buildNewFakeTransportAddress
argument_list|()
argument_list|,
name|emptyMap
argument_list|()
argument_list|,
name|emptySet
argument_list|()
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
argument_list|)
operator|.
name|masterNodeId
argument_list|(
name|masters
index|[
name|masterIndex
index|]
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|state
operator|=
name|ClusterState
operator|.
name|builder
argument_list|(
name|state
argument_list|)
operator|.
name|incrementVersion
argument_list|()
operator|.
name|build
argument_list|()
expr_stmt|;
block|}
name|states
operator|.
name|add
argument_list|(
name|state
argument_list|)
expr_stmt|;
name|lastClusterStatePerMaster
index|[
name|masterIndex
index|]
operator|=
name|state
expr_stmt|;
block|}
return|return
name|states
return|;
block|}
DECL|class|MockListener
specifier|static
class|class
name|MockListener
implements|implements
name|PendingClusterStatesQueue
operator|.
name|StateProcessedListener
block|{
DECL|field|processed
specifier|volatile
name|boolean
name|processed
decl_stmt|;
DECL|field|failure
specifier|volatile
name|Throwable
name|failure
decl_stmt|;
annotation|@
name|Override
DECL|method|onNewClusterStateProcessed
specifier|public
name|void
name|onNewClusterStateProcessed
parameter_list|()
block|{
name|processed
operator|=
literal|true
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|onNewClusterStateFailed
specifier|public
name|void
name|onNewClusterStateFailed
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|failure
operator|=
name|e
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit
