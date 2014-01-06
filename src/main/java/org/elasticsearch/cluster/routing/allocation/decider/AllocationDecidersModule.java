begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.cluster.routing.allocation.decider
package|package
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
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableSet
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
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
name|AbstractModule
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
name|multibindings
operator|.
name|Multibinder
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
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_comment
comment|/**  * This module configures several {@link AllocationDecider}s  * that make configuration specific decisions if shards can be allocated on certain nodes.  *  * @see Decision  * @see AllocationDecider  */
end_comment

begin_class
DECL|class|AllocationDecidersModule
specifier|public
class|class
name|AllocationDecidersModule
extends|extends
name|AbstractModule
block|{
DECL|field|settings
specifier|private
specifier|final
name|Settings
name|settings
decl_stmt|;
DECL|field|allocations
specifier|private
name|List
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|AllocationDecider
argument_list|>
argument_list|>
name|allocations
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
DECL|method|AllocationDecidersModule
specifier|public
name|AllocationDecidersModule
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|this
operator|.
name|settings
operator|=
name|settings
expr_stmt|;
block|}
DECL|method|add
specifier|public
name|AllocationDecidersModule
name|add
parameter_list|(
name|Class
argument_list|<
name|?
extends|extends
name|AllocationDecider
argument_list|>
name|allocationDecider
parameter_list|)
block|{
name|this
operator|.
name|allocations
operator|.
name|add
argument_list|(
name|allocationDecider
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
DECL|method|configure
specifier|protected
name|void
name|configure
parameter_list|()
block|{
name|Multibinder
argument_list|<
name|AllocationDecider
argument_list|>
name|allocationMultibinder
init|=
name|Multibinder
operator|.
name|newSetBinder
argument_list|(
name|binder
argument_list|()
argument_list|,
name|AllocationDecider
operator|.
name|class
argument_list|)
decl_stmt|;
for|for
control|(
name|Class
argument_list|<
name|?
extends|extends
name|AllocationDecider
argument_list|>
name|deciderClass
range|:
name|DEFAULT_ALLOCATION_DECIDERS
control|)
block|{
name|allocationMultibinder
operator|.
name|addBinding
argument_list|()
operator|.
name|to
argument_list|(
name|deciderClass
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|Class
argument_list|<
name|?
extends|extends
name|AllocationDecider
argument_list|>
name|allocation
range|:
name|allocations
control|)
block|{
name|allocationMultibinder
operator|.
name|addBinding
argument_list|()
operator|.
name|to
argument_list|(
name|allocation
argument_list|)
expr_stmt|;
block|}
name|bind
argument_list|(
name|AllocationDeciders
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
block|}
DECL|field|DEFAULT_ALLOCATION_DECIDERS
specifier|public
specifier|static
specifier|final
name|ImmutableSet
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|AllocationDecider
argument_list|>
argument_list|>
name|DEFAULT_ALLOCATION_DECIDERS
init|=
name|ImmutableSet
operator|.
expr|<
name|Class
argument_list|<
name|?
extends|extends
name|AllocationDecider
argument_list|>
operator|>
name|builder
argument_list|()
operator|.
name|add
argument_list|(
name|SameShardAllocationDecider
operator|.
name|class
argument_list|)
operator|.
name|add
argument_list|(
name|FilterAllocationDecider
operator|.
name|class
argument_list|)
operator|.
name|add
argument_list|(
name|ReplicaAfterPrimaryActiveAllocationDecider
operator|.
name|class
argument_list|)
operator|.
name|add
argument_list|(
name|ThrottlingAllocationDecider
operator|.
name|class
argument_list|)
operator|.
name|add
argument_list|(
name|RebalanceOnlyWhenActiveAllocationDecider
operator|.
name|class
argument_list|)
operator|.
name|add
argument_list|(
name|ClusterRebalanceAllocationDecider
operator|.
name|class
argument_list|)
operator|.
name|add
argument_list|(
name|ConcurrentRebalanceAllocationDecider
operator|.
name|class
argument_list|)
operator|.
name|add
argument_list|(
name|EnableAllocationDecider
operator|.
name|class
argument_list|)
operator|.
comment|// new enable allocation logic should proceed old disable allocation logic
name|add
argument_list|(
name|DisableAllocationDecider
operator|.
name|class
argument_list|)
operator|.
name|add
argument_list|(
name|AwarenessAllocationDecider
operator|.
name|class
argument_list|)
operator|.
name|add
argument_list|(
name|ShardsLimitAllocationDecider
operator|.
name|class
argument_list|)
operator|.
name|add
argument_list|(
name|NodeVersionAllocationDecider
operator|.
name|class
argument_list|)
operator|.
name|add
argument_list|(
name|DiskThresholdDecider
operator|.
name|class
argument_list|)
operator|.
name|add
argument_list|(
name|SnapshotInProgressAllocationDecider
operator|.
name|class
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
block|}
end_class

end_unit

