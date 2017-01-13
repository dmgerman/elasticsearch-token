begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.gateway
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|gateway
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
name|cluster
operator|.
name|service
operator|.
name|ClusterService
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
name|UUIDs
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
name|test
operator|.
name|ESTestCase
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
name|NoopDiscovery
import|;
end_import

begin_import
import|import
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
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

begin_class
DECL|class|GatewayServiceTests
specifier|public
class|class
name|GatewayServiceTests
extends|extends
name|ESTestCase
block|{
DECL|method|createService
specifier|private
name|GatewayService
name|createService
parameter_list|(
name|Settings
operator|.
name|Builder
name|settings
parameter_list|)
block|{
name|ClusterService
name|clusterService
init|=
operator|new
name|ClusterService
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"cluster.name"
argument_list|,
literal|"GatewayServiceTests"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|,
operator|new
name|ClusterSettings
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
name|ClusterSettings
operator|.
name|BUILT_IN_CLUSTER_SETTINGS
argument_list|)
argument_list|,
literal|null
argument_list|,
parameter_list|()
lambda|->
operator|new
name|DiscoveryNode
argument_list|(
name|UUIDs
operator|.
name|randomBase64UUID
argument_list|()
argument_list|,
name|buildNewFakeTransportAddress
argument_list|()
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
argument_list|)
decl_stmt|;
return|return
operator|new
name|GatewayService
argument_list|(
name|settings
operator|.
name|build
argument_list|()
argument_list|,
literal|null
argument_list|,
name|clusterService
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
operator|new
name|NoopDiscovery
argument_list|()
argument_list|,
literal|null
argument_list|)
return|;
block|}
DECL|method|testDefaultRecoverAfterTime
specifier|public
name|void
name|testDefaultRecoverAfterTime
parameter_list|()
throws|throws
name|IOException
block|{
comment|// check that the default is not set
name|GatewayService
name|service
init|=
name|createService
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
argument_list|)
decl_stmt|;
name|assertNull
argument_list|(
name|service
operator|.
name|recoverAfterTime
argument_list|()
argument_list|)
expr_stmt|;
comment|// ensure default is set when setting expected_nodes
name|service
operator|=
name|createService
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"gateway.expected_nodes"
argument_list|,
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|service
operator|.
name|recoverAfterTime
argument_list|()
argument_list|,
name|Matchers
operator|.
name|equalTo
argument_list|(
name|GatewayService
operator|.
name|DEFAULT_RECOVER_AFTER_TIME_IF_EXPECTED_NODES_IS_SET
argument_list|)
argument_list|)
expr_stmt|;
comment|// ensure default is set when setting expected_data_nodes
name|service
operator|=
name|createService
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"gateway.expected_data_nodes"
argument_list|,
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|service
operator|.
name|recoverAfterTime
argument_list|()
argument_list|,
name|Matchers
operator|.
name|equalTo
argument_list|(
name|GatewayService
operator|.
name|DEFAULT_RECOVER_AFTER_TIME_IF_EXPECTED_NODES_IS_SET
argument_list|)
argument_list|)
expr_stmt|;
comment|// ensure default is set when setting expected_master_nodes
name|service
operator|=
name|createService
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"gateway.expected_master_nodes"
argument_list|,
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|service
operator|.
name|recoverAfterTime
argument_list|()
argument_list|,
name|Matchers
operator|.
name|equalTo
argument_list|(
name|GatewayService
operator|.
name|DEFAULT_RECOVER_AFTER_TIME_IF_EXPECTED_NODES_IS_SET
argument_list|)
argument_list|)
expr_stmt|;
comment|// ensure settings override default
name|TimeValue
name|timeValue
init|=
name|TimeValue
operator|.
name|timeValueHours
argument_list|(
literal|3
argument_list|)
decl_stmt|;
comment|// ensure default is set when setting expected_nodes
name|service
operator|=
name|createService
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"gateway.expected_nodes"
argument_list|,
literal|1
argument_list|)
operator|.
name|put
argument_list|(
literal|"gateway.recover_after_time"
argument_list|,
name|timeValue
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|service
operator|.
name|recoverAfterTime
argument_list|()
operator|.
name|millis
argument_list|()
argument_list|,
name|Matchers
operator|.
name|equalTo
argument_list|(
name|timeValue
operator|.
name|millis
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

