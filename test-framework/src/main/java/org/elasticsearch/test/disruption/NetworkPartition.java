begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.test.disruption
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|disruption
package|;
end_package

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
name|Loggers
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
name|transport
operator|.
name|MockTransportService
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
name|HashSet
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
name|Set
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertFalse
import|;
end_import

begin_class
DECL|class|NetworkPartition
specifier|public
specifier|abstract
class|class
name|NetworkPartition
implements|implements
name|ServiceDisruptionScheme
block|{
DECL|field|logger
specifier|protected
specifier|final
name|ESLogger
name|logger
init|=
name|Loggers
operator|.
name|getLogger
argument_list|(
name|getClass
argument_list|()
argument_list|)
decl_stmt|;
DECL|field|nodesSideOne
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|nodesSideOne
decl_stmt|;
DECL|field|nodesSideTwo
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|nodesSideTwo
decl_stmt|;
DECL|field|autoExpand
specifier|volatile
name|boolean
name|autoExpand
decl_stmt|;
DECL|field|random
specifier|protected
specifier|final
name|Random
name|random
decl_stmt|;
DECL|field|cluster
specifier|protected
specifier|volatile
name|InternalTestCluster
name|cluster
decl_stmt|;
DECL|field|activeDisruption
specifier|protected
specifier|volatile
name|boolean
name|activeDisruption
init|=
literal|false
decl_stmt|;
DECL|method|NetworkPartition
specifier|public
name|NetworkPartition
parameter_list|(
name|Random
name|random
parameter_list|)
block|{
name|this
operator|.
name|random
operator|=
operator|new
name|Random
argument_list|(
name|random
operator|.
name|nextLong
argument_list|()
argument_list|)
expr_stmt|;
name|nodesSideOne
operator|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
expr_stmt|;
name|nodesSideTwo
operator|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
expr_stmt|;
name|autoExpand
operator|=
literal|true
expr_stmt|;
block|}
DECL|method|NetworkPartition
specifier|public
name|NetworkPartition
parameter_list|(
name|String
name|node1
parameter_list|,
name|String
name|node2
parameter_list|,
name|Random
name|random
parameter_list|)
block|{
name|this
argument_list|(
name|random
argument_list|)
expr_stmt|;
name|nodesSideOne
operator|.
name|add
argument_list|(
name|node1
argument_list|)
expr_stmt|;
name|nodesSideTwo
operator|.
name|add
argument_list|(
name|node2
argument_list|)
expr_stmt|;
name|autoExpand
operator|=
literal|false
expr_stmt|;
block|}
DECL|method|NetworkPartition
specifier|public
name|NetworkPartition
parameter_list|(
name|Set
argument_list|<
name|String
argument_list|>
name|nodesSideOne
parameter_list|,
name|Set
argument_list|<
name|String
argument_list|>
name|nodesSideTwo
parameter_list|,
name|Random
name|random
parameter_list|)
block|{
name|this
argument_list|(
name|random
argument_list|)
expr_stmt|;
name|this
operator|.
name|nodesSideOne
operator|.
name|addAll
argument_list|(
name|nodesSideOne
argument_list|)
expr_stmt|;
name|this
operator|.
name|nodesSideTwo
operator|.
name|addAll
argument_list|(
name|nodesSideTwo
argument_list|)
expr_stmt|;
name|autoExpand
operator|=
literal|false
expr_stmt|;
block|}
DECL|method|getNodesSideOne
specifier|public
name|Collection
argument_list|<
name|String
argument_list|>
name|getNodesSideOne
parameter_list|()
block|{
return|return
name|Collections
operator|.
name|unmodifiableCollection
argument_list|(
name|nodesSideOne
argument_list|)
return|;
block|}
DECL|method|getNodesSideTwo
specifier|public
name|Collection
argument_list|<
name|String
argument_list|>
name|getNodesSideTwo
parameter_list|()
block|{
return|return
name|Collections
operator|.
name|unmodifiableCollection
argument_list|(
name|nodesSideTwo
argument_list|)
return|;
block|}
DECL|method|getMajoritySide
specifier|public
name|Collection
argument_list|<
name|String
argument_list|>
name|getMajoritySide
parameter_list|()
block|{
if|if
condition|(
name|nodesSideOne
operator|.
name|size
argument_list|()
operator|>=
name|nodesSideTwo
operator|.
name|size
argument_list|()
condition|)
block|{
return|return
name|getNodesSideOne
argument_list|()
return|;
block|}
else|else
block|{
return|return
name|getNodesSideTwo
argument_list|()
return|;
block|}
block|}
DECL|method|getMinoritySide
specifier|public
name|Collection
argument_list|<
name|String
argument_list|>
name|getMinoritySide
parameter_list|()
block|{
if|if
condition|(
name|nodesSideOne
operator|.
name|size
argument_list|()
operator|>=
name|nodesSideTwo
operator|.
name|size
argument_list|()
condition|)
block|{
return|return
name|getNodesSideTwo
argument_list|()
return|;
block|}
else|else
block|{
return|return
name|getNodesSideOne
argument_list|()
return|;
block|}
block|}
annotation|@
name|Override
DECL|method|applyToCluster
specifier|public
name|void
name|applyToCluster
parameter_list|(
name|InternalTestCluster
name|cluster
parameter_list|)
block|{
name|this
operator|.
name|cluster
operator|=
name|cluster
expr_stmt|;
if|if
condition|(
name|autoExpand
condition|)
block|{
for|for
control|(
name|String
name|node
range|:
name|cluster
operator|.
name|getNodeNames
argument_list|()
control|)
block|{
name|applyToNode
argument_list|(
name|node
argument_list|,
name|cluster
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
DECL|method|removeFromCluster
specifier|public
name|void
name|removeFromCluster
parameter_list|(
name|InternalTestCluster
name|cluster
parameter_list|)
block|{
name|stopDisrupting
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|removeAndEnsureHealthy
specifier|public
name|void
name|removeAndEnsureHealthy
parameter_list|(
name|InternalTestCluster
name|cluster
parameter_list|)
block|{
name|removeFromCluster
argument_list|(
name|cluster
argument_list|)
expr_stmt|;
name|ensureNodeCount
argument_list|(
name|cluster
argument_list|)
expr_stmt|;
block|}
DECL|method|ensureNodeCount
specifier|protected
name|void
name|ensureNodeCount
parameter_list|(
name|InternalTestCluster
name|cluster
parameter_list|)
block|{
name|assertFalse
argument_list|(
literal|"cluster failed to form after disruption was healed"
argument_list|,
name|cluster
operator|.
name|client
argument_list|()
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
name|setWaitForNodes
argument_list|(
literal|""
operator|+
name|cluster
operator|.
name|size
argument_list|()
argument_list|)
operator|.
name|setWaitForRelocatingShards
argument_list|(
literal|0
argument_list|)
operator|.
name|get
argument_list|()
operator|.
name|isTimedOut
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|applyToNode
specifier|public
specifier|synchronized
name|void
name|applyToNode
parameter_list|(
name|String
name|node
parameter_list|,
name|InternalTestCluster
name|cluster
parameter_list|)
block|{
if|if
condition|(
operator|!
name|autoExpand
operator|||
name|nodesSideOne
operator|.
name|contains
argument_list|(
name|node
argument_list|)
operator|||
name|nodesSideTwo
operator|.
name|contains
argument_list|(
name|node
argument_list|)
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|nodesSideOne
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|nodesSideOne
operator|.
name|add
argument_list|(
name|node
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|nodesSideTwo
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|nodesSideTwo
operator|.
name|add
argument_list|(
name|node
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|random
operator|.
name|nextBoolean
argument_list|()
condition|)
block|{
name|nodesSideOne
operator|.
name|add
argument_list|(
name|node
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|nodesSideTwo
operator|.
name|add
argument_list|(
name|node
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|removeFromNode
specifier|public
specifier|synchronized
name|void
name|removeFromNode
parameter_list|(
name|String
name|node
parameter_list|,
name|InternalTestCluster
name|cluster
parameter_list|)
block|{
name|MockTransportService
name|transportService
init|=
operator|(
name|MockTransportService
operator|)
name|cluster
operator|.
name|getInstance
argument_list|(
name|TransportService
operator|.
name|class
argument_list|,
name|node
argument_list|)
decl_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|otherSideNodes
decl_stmt|;
if|if
condition|(
name|nodesSideOne
operator|.
name|contains
argument_list|(
name|node
argument_list|)
condition|)
block|{
name|otherSideNodes
operator|=
name|nodesSideTwo
expr_stmt|;
name|nodesSideOne
operator|.
name|remove
argument_list|(
name|node
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|nodesSideTwo
operator|.
name|contains
argument_list|(
name|node
argument_list|)
condition|)
block|{
name|otherSideNodes
operator|=
name|nodesSideOne
expr_stmt|;
name|nodesSideTwo
operator|.
name|remove
argument_list|(
name|node
argument_list|)
expr_stmt|;
block|}
else|else
block|{
return|return;
block|}
for|for
control|(
name|String
name|node2
range|:
name|otherSideNodes
control|)
block|{
name|MockTransportService
name|transportService2
init|=
operator|(
name|MockTransportService
operator|)
name|cluster
operator|.
name|getInstance
argument_list|(
name|TransportService
operator|.
name|class
argument_list|,
name|node2
argument_list|)
decl_stmt|;
name|removeDisruption
argument_list|(
name|transportService
argument_list|,
name|transportService2
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|testClusterClosed
specifier|public
specifier|synchronized
name|void
name|testClusterClosed
parameter_list|()
block|{      }
DECL|method|getPartitionDescription
specifier|protected
specifier|abstract
name|String
name|getPartitionDescription
parameter_list|()
function_decl|;
annotation|@
name|Override
DECL|method|startDisrupting
specifier|public
specifier|synchronized
name|void
name|startDisrupting
parameter_list|()
block|{
if|if
condition|(
name|nodesSideOne
operator|.
name|size
argument_list|()
operator|==
literal|0
operator|||
name|nodesSideTwo
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
return|return;
block|}
name|logger
operator|.
name|info
argument_list|(
literal|"nodes {} will be partitioned from {}. partition type [{}]"
argument_list|,
name|nodesSideOne
argument_list|,
name|nodesSideTwo
argument_list|,
name|getPartitionDescription
argument_list|()
argument_list|)
expr_stmt|;
name|activeDisruption
operator|=
literal|true
expr_stmt|;
for|for
control|(
name|String
name|node1
range|:
name|nodesSideOne
control|)
block|{
name|MockTransportService
name|transportService1
init|=
operator|(
name|MockTransportService
operator|)
name|cluster
operator|.
name|getInstance
argument_list|(
name|TransportService
operator|.
name|class
argument_list|,
name|node1
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|node2
range|:
name|nodesSideTwo
control|)
block|{
name|MockTransportService
name|transportService2
init|=
operator|(
name|MockTransportService
operator|)
name|cluster
operator|.
name|getInstance
argument_list|(
name|TransportService
operator|.
name|class
argument_list|,
name|node2
argument_list|)
decl_stmt|;
name|applyDisruption
argument_list|(
name|transportService1
argument_list|,
name|transportService2
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
DECL|method|stopDisrupting
specifier|public
specifier|synchronized
name|void
name|stopDisrupting
parameter_list|()
block|{
if|if
condition|(
name|nodesSideOne
operator|.
name|size
argument_list|()
operator|==
literal|0
operator|||
name|nodesSideTwo
operator|.
name|size
argument_list|()
operator|==
literal|0
operator|||
operator|!
name|activeDisruption
condition|)
block|{
return|return;
block|}
name|logger
operator|.
name|info
argument_list|(
literal|"restoring partition between nodes {}& nodes {}"
argument_list|,
name|nodesSideOne
argument_list|,
name|nodesSideTwo
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|node1
range|:
name|nodesSideOne
control|)
block|{
name|MockTransportService
name|transportService1
init|=
operator|(
name|MockTransportService
operator|)
name|cluster
operator|.
name|getInstance
argument_list|(
name|TransportService
operator|.
name|class
argument_list|,
name|node1
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|node2
range|:
name|nodesSideTwo
control|)
block|{
name|MockTransportService
name|transportService2
init|=
operator|(
name|MockTransportService
operator|)
name|cluster
operator|.
name|getInstance
argument_list|(
name|TransportService
operator|.
name|class
argument_list|,
name|node2
argument_list|)
decl_stmt|;
name|removeDisruption
argument_list|(
name|transportService1
argument_list|,
name|transportService2
argument_list|)
expr_stmt|;
block|}
block|}
name|activeDisruption
operator|=
literal|false
expr_stmt|;
block|}
DECL|method|applyDisruption
specifier|abstract
name|void
name|applyDisruption
parameter_list|(
name|MockTransportService
name|transportService1
parameter_list|,
name|MockTransportService
name|transportService2
parameter_list|)
function_decl|;
DECL|method|removeDisruption
specifier|protected
name|void
name|removeDisruption
parameter_list|(
name|MockTransportService
name|transportService1
parameter_list|,
name|MockTransportService
name|transportService2
parameter_list|)
block|{
name|transportService1
operator|.
name|clearRule
argument_list|(
name|transportService2
argument_list|)
expr_stmt|;
name|transportService2
operator|.
name|clearRule
argument_list|(
name|transportService1
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

