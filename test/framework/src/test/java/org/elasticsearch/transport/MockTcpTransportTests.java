begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.transport
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|transport
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
name|io
operator|.
name|stream
operator|.
name|NamedWriteableRegistry
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
name|network
operator|.
name|NetworkService
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
name|ClusterSettings
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
name|BigArrays
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
name|breaker
operator|.
name|NoneCircuitBreakerService
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
name|Collections
import|;
end_import

begin_class
DECL|class|MockTcpTransportTests
specifier|public
class|class
name|MockTcpTransportTests
extends|extends
name|AbstractSimpleTransportTestCase
block|{
annotation|@
name|Override
DECL|method|build
specifier|protected
name|MockTransportService
name|build
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|Version
name|version
parameter_list|,
name|ClusterSettings
name|clusterSettings
parameter_list|,
name|boolean
name|doHandshake
parameter_list|)
block|{
name|NamedWriteableRegistry
name|namedWriteableRegistry
init|=
operator|new
name|NamedWriteableRegistry
argument_list|(
name|Collections
operator|.
name|emptyList
argument_list|()
argument_list|)
decl_stmt|;
name|Transport
name|transport
init|=
operator|new
name|MockTcpTransport
argument_list|(
name|settings
argument_list|,
name|threadPool
argument_list|,
name|BigArrays
operator|.
name|NON_RECYCLING_INSTANCE
argument_list|,
operator|new
name|NoneCircuitBreakerService
argument_list|()
argument_list|,
name|namedWriteableRegistry
argument_list|,
operator|new
name|NetworkService
argument_list|(
name|settings
argument_list|,
name|Collections
operator|.
name|emptyList
argument_list|()
argument_list|)
argument_list|,
name|version
argument_list|)
block|{
annotation|@
name|Override
specifier|protected
name|Version
name|executeHandshake
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|,
name|MockChannel
name|mockChannel
parameter_list|,
name|TimeValue
name|timeout
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
if|if
condition|(
name|doHandshake
condition|)
block|{
return|return
name|super
operator|.
name|executeHandshake
argument_list|(
name|node
argument_list|,
name|mockChannel
argument_list|,
name|timeout
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|version
operator|.
name|minimumCompatibilityVersion
argument_list|()
return|;
block|}
block|}
block|}
decl_stmt|;
name|MockTransportService
name|mockTransportService
init|=
name|MockTransportService
operator|.
name|createNewService
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
name|transport
argument_list|,
name|version
argument_list|,
name|threadPool
argument_list|,
name|clusterSettings
argument_list|)
decl_stmt|;
name|mockTransportService
operator|.
name|start
argument_list|()
expr_stmt|;
return|return
name|mockTransportService
return|;
block|}
block|}
end_class

end_unit

