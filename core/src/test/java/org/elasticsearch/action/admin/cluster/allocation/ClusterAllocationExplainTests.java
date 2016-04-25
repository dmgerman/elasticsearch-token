begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.cluster.allocation
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|admin
operator|.
name|cluster
operator|.
name|allocation
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|client
operator|.
name|Requests
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
name|Decision
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
name|ESSingleNodeTestCase
import|;
end_import

begin_comment
comment|/**  * Tests for the cluster allocation explanation  */
end_comment

begin_class
DECL|class|ClusterAllocationExplainTests
specifier|public
specifier|final
class|class
name|ClusterAllocationExplainTests
extends|extends
name|ESSingleNodeTestCase
block|{
DECL|method|testShardExplain
specifier|public
name|void
name|testShardExplain
parameter_list|()
throws|throws
name|Exception
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
name|prepareCreate
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setSettings
argument_list|(
literal|"index.number_of_shards"
argument_list|,
literal|1
argument_list|,
literal|"index.number_of_replicas"
argument_list|,
literal|1
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|health
argument_list|(
name|Requests
operator|.
name|clusterHealthRequest
argument_list|(
literal|"test"
argument_list|)
operator|.
name|waitForYellowStatus
argument_list|()
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|ClusterAllocationExplainResponse
name|resp
init|=
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareAllocationExplain
argument_list|()
operator|.
name|setIndex
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setShard
argument_list|(
literal|0
argument_list|)
operator|.
name|setPrimary
argument_list|(
literal|false
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|ClusterAllocationExplanation
name|cae
init|=
name|resp
operator|.
name|getExplanation
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
literal|"should always have an explanation"
argument_list|,
name|cae
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"test"
argument_list|,
name|cae
operator|.
name|getShard
argument_list|()
operator|.
name|getIndexName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|cae
operator|.
name|getShard
argument_list|()
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|false
argument_list|,
name|cae
operator|.
name|isPrimary
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|cae
operator|.
name|getAssignedNodeId
argument_list|()
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|cae
operator|.
name|getUnassignedInfo
argument_list|()
argument_list|)
expr_stmt|;
name|ClusterAllocationExplanation
operator|.
name|NodeExplanation
name|explanation
init|=
name|cae
operator|.
name|getNodeExplanations
argument_list|()
operator|.
name|values
argument_list|()
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
decl_stmt|;
name|ClusterAllocationExplanation
operator|.
name|FinalDecision
name|fd
init|=
name|explanation
operator|.
name|getFinalDecision
argument_list|()
decl_stmt|;
name|ClusterAllocationExplanation
operator|.
name|StoreCopy
name|storeCopy
init|=
name|explanation
operator|.
name|getStoreCopy
argument_list|()
decl_stmt|;
name|String
name|finalExplanation
init|=
name|explanation
operator|.
name|getFinalExplanation
argument_list|()
decl_stmt|;
name|Decision
name|d
init|=
name|explanation
operator|.
name|getDecision
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
literal|"should have a decision"
argument_list|,
name|d
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Decision
operator|.
name|Type
operator|.
name|NO
argument_list|,
name|d
operator|.
name|type
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|ClusterAllocationExplanation
operator|.
name|FinalDecision
operator|.
name|NO
argument_list|,
name|fd
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|ClusterAllocationExplanation
operator|.
name|StoreCopy
operator|.
name|AVAILABLE
argument_list|,
name|storeCopy
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|d
operator|.
name|toString
argument_list|()
argument_list|,
name|d
operator|.
name|toString
argument_list|()
operator|.
name|contains
argument_list|(
literal|"NO(the shard cannot be allocated on the same node id"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|d
operator|instanceof
name|Decision
operator|.
name|Multi
argument_list|)
expr_stmt|;
name|Decision
operator|.
name|Multi
name|md
init|=
operator|(
name|Decision
operator|.
name|Multi
operator|)
name|d
decl_stmt|;
name|Decision
name|ssd
init|=
name|md
operator|.
name|getDecisions
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|Decision
operator|.
name|Type
operator|.
name|NO
argument_list|,
name|ssd
operator|.
name|type
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|ssd
operator|.
name|toString
argument_list|()
argument_list|,
name|ssd
operator|.
name|toString
argument_list|()
operator|.
name|contains
argument_list|(
literal|"NO(the shard cannot be allocated on the same node id"
argument_list|)
argument_list|)
expr_stmt|;
name|Float
name|weight
init|=
name|explanation
operator|.
name|getWeight
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
literal|"should have a weight"
argument_list|,
name|weight
argument_list|)
expr_stmt|;
name|resp
operator|=
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareAllocationExplain
argument_list|()
operator|.
name|setIndex
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setShard
argument_list|(
literal|0
argument_list|)
operator|.
name|setPrimary
argument_list|(
literal|true
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|cae
operator|=
name|resp
operator|.
name|getExplanation
argument_list|()
expr_stmt|;
name|assertNotNull
argument_list|(
literal|"should always have an explanation"
argument_list|,
name|cae
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"test"
argument_list|,
name|cae
operator|.
name|getShard
argument_list|()
operator|.
name|getIndexName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|cae
operator|.
name|getShard
argument_list|()
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|cae
operator|.
name|isPrimary
argument_list|()
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
literal|"shard should have assigned node id"
argument_list|,
name|cae
operator|.
name|getAssignedNodeId
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
literal|"assigned shard should not have unassigned info"
argument_list|,
name|cae
operator|.
name|getUnassignedInfo
argument_list|()
argument_list|)
expr_stmt|;
name|explanation
operator|=
name|cae
operator|.
name|getNodeExplanations
argument_list|()
operator|.
name|values
argument_list|()
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
expr_stmt|;
name|d
operator|=
name|explanation
operator|.
name|getDecision
argument_list|()
expr_stmt|;
name|fd
operator|=
name|explanation
operator|.
name|getFinalDecision
argument_list|()
expr_stmt|;
name|storeCopy
operator|=
name|explanation
operator|.
name|getStoreCopy
argument_list|()
expr_stmt|;
name|finalExplanation
operator|=
name|explanation
operator|.
name|getFinalExplanation
argument_list|()
expr_stmt|;
name|assertNotNull
argument_list|(
literal|"should have a decision"
argument_list|,
name|d
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Decision
operator|.
name|Type
operator|.
name|NO
argument_list|,
name|d
operator|.
name|type
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|ClusterAllocationExplanation
operator|.
name|FinalDecision
operator|.
name|ALREADY_ASSIGNED
argument_list|,
name|fd
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|ClusterAllocationExplanation
operator|.
name|StoreCopy
operator|.
name|AVAILABLE
argument_list|,
name|storeCopy
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|d
operator|.
name|toString
argument_list|()
argument_list|,
name|d
operator|.
name|toString
argument_list|()
operator|.
name|contains
argument_list|(
literal|"NO(the shard cannot be allocated on the same node id"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|d
operator|instanceof
name|Decision
operator|.
name|Multi
argument_list|)
expr_stmt|;
name|md
operator|=
operator|(
name|Decision
operator|.
name|Multi
operator|)
name|d
expr_stmt|;
name|ssd
operator|=
name|md
operator|.
name|getDecisions
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Decision
operator|.
name|Type
operator|.
name|NO
argument_list|,
name|ssd
operator|.
name|type
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|ssd
operator|.
name|toString
argument_list|()
argument_list|,
name|ssd
operator|.
name|toString
argument_list|()
operator|.
name|contains
argument_list|(
literal|"NO(the shard cannot be allocated on the same node id"
argument_list|)
argument_list|)
expr_stmt|;
name|weight
operator|=
name|explanation
operator|.
name|getWeight
argument_list|()
expr_stmt|;
name|assertNotNull
argument_list|(
literal|"should have a weight"
argument_list|,
name|weight
argument_list|)
expr_stmt|;
name|resp
operator|=
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareAllocationExplain
argument_list|()
operator|.
name|useAnyUnassignedShard
argument_list|()
operator|.
name|get
argument_list|()
expr_stmt|;
name|cae
operator|=
name|resp
operator|.
name|getExplanation
argument_list|()
expr_stmt|;
name|assertNotNull
argument_list|(
literal|"should always have an explanation"
argument_list|,
name|cae
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"test"
argument_list|,
name|cae
operator|.
name|getShard
argument_list|()
operator|.
name|getIndexName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|cae
operator|.
name|getShard
argument_list|()
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|false
argument_list|,
name|cae
operator|.
name|isPrimary
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

