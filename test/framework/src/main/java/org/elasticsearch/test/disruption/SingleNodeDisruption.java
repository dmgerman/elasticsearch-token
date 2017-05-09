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
name|apache
operator|.
name|logging
operator|.
name|log4j
operator|.
name|Logger
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
name|java
operator|.
name|util
operator|.
name|Random
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
DECL|class|SingleNodeDisruption
specifier|public
specifier|abstract
class|class
name|SingleNodeDisruption
implements|implements
name|ServiceDisruptionScheme
block|{
DECL|field|logger
specifier|protected
specifier|final
name|Logger
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
DECL|field|disruptedNode
specifier|protected
specifier|volatile
name|String
name|disruptedNode
decl_stmt|;
DECL|field|cluster
specifier|protected
specifier|volatile
name|InternalTestCluster
name|cluster
decl_stmt|;
DECL|field|random
specifier|protected
specifier|final
name|Random
name|random
decl_stmt|;
DECL|method|SingleNodeDisruption
specifier|public
name|SingleNodeDisruption
parameter_list|(
name|String
name|disruptedNode
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
name|disruptedNode
operator|=
name|disruptedNode
expr_stmt|;
block|}
DECL|method|SingleNodeDisruption
specifier|public
name|SingleNodeDisruption
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
name|disruptedNode
operator|==
literal|null
condition|)
block|{
name|String
index|[]
name|nodes
init|=
name|cluster
operator|.
name|getNodeNames
argument_list|()
decl_stmt|;
name|disruptedNode
operator|=
name|nodes
index|[
name|random
operator|.
name|nextInt
argument_list|(
name|nodes
operator|.
name|length
argument_list|)
index|]
expr_stmt|;
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
if|if
condition|(
name|disruptedNode
operator|!=
literal|null
condition|)
block|{
name|removeFromNode
argument_list|(
name|disruptedNode
argument_list|,
name|cluster
argument_list|)
expr_stmt|;
block|}
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
block|{      }
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
if|if
condition|(
name|disruptedNode
operator|==
literal|null
condition|)
block|{
return|return;
block|}
if|if
condition|(
operator|!
name|node
operator|.
name|equals
argument_list|(
name|disruptedNode
argument_list|)
condition|)
block|{
return|return;
block|}
name|stopDisrupting
argument_list|()
expr_stmt|;
name|disruptedNode
operator|=
literal|null
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|testClusterClosed
specifier|public
specifier|synchronized
name|void
name|testClusterClosed
parameter_list|()
block|{
name|disruptedNode
operator|=
literal|null
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
name|String
operator|.
name|valueOf
argument_list|(
name|cluster
operator|.
name|size
argument_list|()
argument_list|)
argument_list|)
operator|.
name|setWaitForNoRelocatingShards
argument_list|(
literal|true
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
block|}
end_class

end_unit

