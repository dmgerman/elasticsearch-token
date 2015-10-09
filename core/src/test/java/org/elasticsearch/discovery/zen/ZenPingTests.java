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
name|common
operator|.
name|transport
operator|.
name|DummyTransportAddress
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
name|ping
operator|.
name|ZenPing
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
DECL|class|ZenPingTests
specifier|public
class|class
name|ZenPingTests
extends|extends
name|ESTestCase
block|{
DECL|method|testPingCollection
specifier|public
name|void
name|testPingCollection
parameter_list|()
block|{
name|DiscoveryNode
index|[]
name|nodes
init|=
operator|new
name|DiscoveryNode
index|[
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|30
argument_list|)
index|]
decl_stmt|;
name|long
name|maxIdPerNode
index|[]
init|=
operator|new
name|long
index|[
name|nodes
operator|.
name|length
index|]
decl_stmt|;
name|DiscoveryNode
name|masterPerNode
index|[]
init|=
operator|new
name|DiscoveryNode
index|[
name|nodes
operator|.
name|length
index|]
decl_stmt|;
name|boolean
name|hasJoinedOncePerNode
index|[]
init|=
operator|new
name|boolean
index|[
name|nodes
operator|.
name|length
index|]
decl_stmt|;
name|ArrayList
argument_list|<
name|ZenPing
operator|.
name|PingResponse
argument_list|>
name|pings
init|=
operator|new
name|ArrayList
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
name|nodes
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|nodes
index|[
name|i
index|]
operator|=
operator|new
name|DiscoveryNode
argument_list|(
literal|""
operator|+
name|i
argument_list|,
name|DummyTransportAddress
operator|.
name|INSTANCE
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|pingCount
init|=
name|scaledRandomIntBetween
argument_list|(
literal|10
argument_list|,
name|nodes
operator|.
name|length
operator|*
literal|10
argument_list|)
init|;
name|pingCount
operator|>
literal|0
condition|;
name|pingCount
operator|--
control|)
block|{
name|int
name|node
init|=
name|randomInt
argument_list|(
name|nodes
operator|.
name|length
operator|-
literal|1
argument_list|)
decl_stmt|;
name|DiscoveryNode
name|masterNode
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|masterNode
operator|=
name|nodes
index|[
name|randomInt
argument_list|(
name|nodes
operator|.
name|length
operator|-
literal|1
argument_list|)
index|]
expr_stmt|;
block|}
name|boolean
name|hasJoinedOnce
init|=
name|randomBoolean
argument_list|()
decl_stmt|;
name|ZenPing
operator|.
name|PingResponse
name|ping
init|=
operator|new
name|ZenPing
operator|.
name|PingResponse
argument_list|(
name|nodes
index|[
name|node
index|]
argument_list|,
name|masterNode
argument_list|,
name|ClusterName
operator|.
name|DEFAULT
argument_list|,
name|hasJoinedOnce
argument_list|)
decl_stmt|;
if|if
condition|(
name|rarely
argument_list|()
condition|)
block|{
comment|// ignore some pings
continue|continue;
block|}
comment|// update max ping info
name|maxIdPerNode
index|[
name|node
index|]
operator|=
name|ping
operator|.
name|id
argument_list|()
expr_stmt|;
name|masterPerNode
index|[
name|node
index|]
operator|=
name|masterNode
expr_stmt|;
name|hasJoinedOncePerNode
index|[
name|node
index|]
operator|=
name|hasJoinedOnce
expr_stmt|;
name|pings
operator|.
name|add
argument_list|(
name|ping
argument_list|)
expr_stmt|;
block|}
comment|// shuffle
name|Collections
operator|.
name|shuffle
argument_list|(
name|pings
argument_list|)
expr_stmt|;
name|ZenPing
operator|.
name|PingCollection
name|collection
init|=
operator|new
name|ZenPing
operator|.
name|PingCollection
argument_list|()
decl_stmt|;
name|collection
operator|.
name|addPings
argument_list|(
name|pings
operator|.
name|toArray
argument_list|(
operator|new
name|ZenPing
operator|.
name|PingResponse
index|[
name|pings
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|ZenPing
operator|.
name|PingResponse
index|[]
name|aggregate
init|=
name|collection
operator|.
name|toArray
argument_list|()
decl_stmt|;
for|for
control|(
name|ZenPing
operator|.
name|PingResponse
name|ping
range|:
name|aggregate
control|)
block|{
name|int
name|nodeId
init|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|ping
operator|.
name|node
argument_list|()
operator|.
name|id
argument_list|()
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|maxIdPerNode
index|[
name|nodeId
index|]
argument_list|,
name|equalTo
argument_list|(
name|ping
operator|.
name|id
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|masterPerNode
index|[
name|nodeId
index|]
argument_list|,
name|equalTo
argument_list|(
name|ping
operator|.
name|master
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|hasJoinedOncePerNode
index|[
name|nodeId
index|]
argument_list|,
name|equalTo
argument_list|(
name|ping
operator|.
name|hasJoinedOnce
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|maxIdPerNode
index|[
name|nodeId
index|]
operator|=
operator|-
literal|1
expr_stmt|;
comment|// mark as seen
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|maxIdPerNode
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|assertTrue
argument_list|(
literal|"node "
operator|+
name|i
operator|+
literal|" had pings but it was not found in collection"
argument_list|,
name|maxIdPerNode
index|[
name|i
index|]
operator|<=
literal|0
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

