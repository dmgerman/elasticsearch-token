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
name|ElasticsearchIllegalStateException
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
name|DiscoveryNodes
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
name|ElasticsearchTestCase
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
name|LinkedList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Queue
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|discovery
operator|.
name|zen
operator|.
name|ZenDiscovery
operator|.
name|ProcessClusterState
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|discovery
operator|.
name|zen
operator|.
name|ZenDiscovery
operator|.
name|shouldIgnoreOrRejectNewClusterState
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
name|*
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|core
operator|.
name|IsNull
operator|.
name|nullValue
import|;
end_import

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|ZenDiscoveryUnitTest
specifier|public
class|class
name|ZenDiscoveryUnitTest
extends|extends
name|ElasticsearchTestCase
block|{
DECL|method|testShouldIgnoreNewClusterState
specifier|public
name|void
name|testShouldIgnoreNewClusterState
parameter_list|()
block|{
name|ClusterName
name|clusterName
init|=
operator|new
name|ClusterName
argument_list|(
literal|"abc"
argument_list|)
decl_stmt|;
name|DiscoveryNodes
operator|.
name|Builder
name|currentNodes
init|=
name|DiscoveryNodes
operator|.
name|builder
argument_list|()
decl_stmt|;
name|currentNodes
operator|.
name|masterNodeId
argument_list|(
literal|"a"
argument_list|)
expr_stmt|;
name|DiscoveryNodes
operator|.
name|Builder
name|newNodes
init|=
name|DiscoveryNodes
operator|.
name|builder
argument_list|()
decl_stmt|;
name|newNodes
operator|.
name|masterNodeId
argument_list|(
literal|"a"
argument_list|)
expr_stmt|;
name|ClusterState
operator|.
name|Builder
name|currentState
init|=
name|ClusterState
operator|.
name|builder
argument_list|(
name|clusterName
argument_list|)
decl_stmt|;
name|currentState
operator|.
name|nodes
argument_list|(
name|currentNodes
argument_list|)
expr_stmt|;
name|ClusterState
operator|.
name|Builder
name|newState
init|=
name|ClusterState
operator|.
name|builder
argument_list|(
name|clusterName
argument_list|)
decl_stmt|;
name|newState
operator|.
name|nodes
argument_list|(
name|newNodes
argument_list|)
expr_stmt|;
name|currentState
operator|.
name|version
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|newState
operator|.
name|version
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"should ignore, because new state's version is lower to current state's version"
argument_list|,
name|shouldIgnoreOrRejectNewClusterState
argument_list|(
name|logger
argument_list|,
name|currentState
operator|.
name|build
argument_list|()
argument_list|,
name|newState
operator|.
name|build
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|currentState
operator|.
name|version
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|newState
operator|.
name|version
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
literal|"should not ignore, because new state's version is equal to current state's version"
argument_list|,
name|shouldIgnoreOrRejectNewClusterState
argument_list|(
name|logger
argument_list|,
name|currentState
operator|.
name|build
argument_list|()
argument_list|,
name|newState
operator|.
name|build
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|currentState
operator|.
name|version
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|newState
operator|.
name|version
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
literal|"should not ignore, because new state's version is higher to current state's version"
argument_list|,
name|shouldIgnoreOrRejectNewClusterState
argument_list|(
name|logger
argument_list|,
name|currentState
operator|.
name|build
argument_list|()
argument_list|,
name|newState
operator|.
name|build
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|currentNodes
operator|=
name|DiscoveryNodes
operator|.
name|builder
argument_list|()
expr_stmt|;
name|currentNodes
operator|.
name|masterNodeId
argument_list|(
literal|"b"
argument_list|)
expr_stmt|;
comment|// version isn't taken into account, so randomize it to ensure this.
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|currentState
operator|.
name|version
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|newState
operator|.
name|version
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|currentState
operator|.
name|version
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|newState
operator|.
name|version
argument_list|(
literal|2
argument_list|)
expr_stmt|;
block|}
name|currentState
operator|.
name|nodes
argument_list|(
name|currentNodes
argument_list|)
expr_stmt|;
try|try
block|{
name|shouldIgnoreOrRejectNewClusterState
argument_list|(
name|logger
argument_list|,
name|currentState
operator|.
name|build
argument_list|()
argument_list|,
name|newState
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"should ignore, because current state's master is not equal to new state's master"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ElasticsearchIllegalStateException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"cluster state from a different master then the current one, rejecting"
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|currentNodes
operator|=
name|DiscoveryNodes
operator|.
name|builder
argument_list|()
expr_stmt|;
name|currentNodes
operator|.
name|masterNodeId
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|currentState
operator|.
name|nodes
argument_list|(
name|currentNodes
argument_list|)
expr_stmt|;
comment|// version isn't taken into account, so randomize it to ensure this.
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|currentState
operator|.
name|version
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|newState
operator|.
name|version
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|currentState
operator|.
name|version
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|newState
operator|.
name|version
argument_list|(
literal|2
argument_list|)
expr_stmt|;
block|}
name|assertFalse
argument_list|(
literal|"should not ignore, because current state doesn't have a master"
argument_list|,
name|shouldIgnoreOrRejectNewClusterState
argument_list|(
name|logger
argument_list|,
name|currentState
operator|.
name|build
argument_list|()
argument_list|,
name|newState
operator|.
name|build
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testSelectNextStateToProcess_empty
specifier|public
name|void
name|testSelectNextStateToProcess_empty
parameter_list|()
block|{
name|Queue
argument_list|<
name|ProcessClusterState
argument_list|>
name|queue
init|=
operator|new
name|LinkedList
argument_list|<>
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|ZenDiscovery
operator|.
name|selectNextStateToProcess
argument_list|(
name|queue
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testSelectNextStateToProcess
specifier|public
name|void
name|testSelectNextStateToProcess
parameter_list|()
block|{
name|ClusterName
name|clusterName
init|=
operator|new
name|ClusterName
argument_list|(
literal|"abc"
argument_list|)
decl_stmt|;
name|DiscoveryNodes
name|nodes
init|=
name|DiscoveryNodes
operator|.
name|builder
argument_list|()
operator|.
name|masterNodeId
argument_list|(
literal|"a"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
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
name|LinkedList
argument_list|<
name|ProcessClusterState
argument_list|>
name|queue
init|=
operator|new
name|LinkedList
argument_list|<>
argument_list|()
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
name|numUpdates
condition|;
name|i
operator|++
control|)
block|{
name|queue
operator|.
name|add
argument_list|(
operator|new
name|ProcessClusterState
argument_list|(
name|ClusterState
operator|.
name|builder
argument_list|(
name|clusterName
argument_list|)
operator|.
name|version
argument_list|(
name|i
argument_list|)
operator|.
name|nodes
argument_list|(
name|nodes
argument_list|)
operator|.
name|build
argument_list|()
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|ProcessClusterState
name|mostRecent
init|=
name|queue
operator|.
name|get
argument_list|(
name|numUpdates
operator|-
literal|1
argument_list|)
decl_stmt|;
name|Collections
operator|.
name|shuffle
argument_list|(
name|queue
argument_list|,
name|getRandom
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ZenDiscovery
operator|.
name|selectNextStateToProcess
argument_list|(
name|queue
argument_list|)
argument_list|,
name|sameInstance
argument_list|(
name|mostRecent
operator|.
name|clusterState
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|mostRecent
operator|.
name|processed
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|queue
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testSelectNextStateToProcess_differentMasters
specifier|public
name|void
name|testSelectNextStateToProcess_differentMasters
parameter_list|()
block|{
name|ClusterName
name|clusterName
init|=
operator|new
name|ClusterName
argument_list|(
literal|"abc"
argument_list|)
decl_stmt|;
name|DiscoveryNodes
name|nodes1
init|=
name|DiscoveryNodes
operator|.
name|builder
argument_list|()
operator|.
name|masterNodeId
argument_list|(
literal|"a"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|DiscoveryNodes
name|nodes2
init|=
name|DiscoveryNodes
operator|.
name|builder
argument_list|()
operator|.
name|masterNodeId
argument_list|(
literal|"b"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|LinkedList
argument_list|<
name|ProcessClusterState
argument_list|>
name|queue
init|=
operator|new
name|LinkedList
argument_list|<>
argument_list|()
decl_stmt|;
name|ProcessClusterState
name|thirdMostRecent
init|=
operator|new
name|ProcessClusterState
argument_list|(
name|ClusterState
operator|.
name|builder
argument_list|(
name|clusterName
argument_list|)
operator|.
name|version
argument_list|(
literal|1
argument_list|)
operator|.
name|nodes
argument_list|(
name|nodes1
argument_list|)
operator|.
name|build
argument_list|()
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|queue
operator|.
name|offer
argument_list|(
name|thirdMostRecent
argument_list|)
expr_stmt|;
name|ProcessClusterState
name|secondMostRecent
init|=
operator|new
name|ProcessClusterState
argument_list|(
name|ClusterState
operator|.
name|builder
argument_list|(
name|clusterName
argument_list|)
operator|.
name|version
argument_list|(
literal|2
argument_list|)
operator|.
name|nodes
argument_list|(
name|nodes1
argument_list|)
operator|.
name|build
argument_list|()
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|queue
operator|.
name|offer
argument_list|(
name|secondMostRecent
argument_list|)
expr_stmt|;
name|ProcessClusterState
name|mostRecent
init|=
operator|new
name|ProcessClusterState
argument_list|(
name|ClusterState
operator|.
name|builder
argument_list|(
name|clusterName
argument_list|)
operator|.
name|version
argument_list|(
literal|3
argument_list|)
operator|.
name|nodes
argument_list|(
name|nodes1
argument_list|)
operator|.
name|build
argument_list|()
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|queue
operator|.
name|offer
argument_list|(
name|mostRecent
argument_list|)
expr_stmt|;
name|Collections
operator|.
name|shuffle
argument_list|(
name|queue
argument_list|,
name|getRandom
argument_list|()
argument_list|)
expr_stmt|;
name|queue
operator|.
name|offer
argument_list|(
operator|new
name|ProcessClusterState
argument_list|(
name|ClusterState
operator|.
name|builder
argument_list|(
name|clusterName
argument_list|)
operator|.
name|version
argument_list|(
literal|4
argument_list|)
operator|.
name|nodes
argument_list|(
name|nodes2
argument_list|)
operator|.
name|build
argument_list|()
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|queue
operator|.
name|offer
argument_list|(
operator|new
name|ProcessClusterState
argument_list|(
name|ClusterState
operator|.
name|builder
argument_list|(
name|clusterName
argument_list|)
operator|.
name|version
argument_list|(
literal|5
argument_list|)
operator|.
name|nodes
argument_list|(
name|nodes1
argument_list|)
operator|.
name|build
argument_list|()
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ZenDiscovery
operator|.
name|selectNextStateToProcess
argument_list|(
name|queue
argument_list|)
argument_list|,
name|sameInstance
argument_list|(
name|mostRecent
operator|.
name|clusterState
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|thirdMostRecent
operator|.
name|processed
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|secondMostRecent
operator|.
name|processed
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|mostRecent
operator|.
name|processed
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|queue
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|queue
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|processed
argument_list|,
name|is
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|queue
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|processed
argument_list|,
name|is
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

