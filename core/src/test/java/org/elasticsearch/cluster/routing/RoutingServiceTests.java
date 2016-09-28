begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.cluster.routing
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|routing
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
name|cluster
operator|.
name|ESAllocationTestCase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
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
name|AtomicBoolean
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

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|RoutingServiceTests
specifier|public
class|class
name|RoutingServiceTests
extends|extends
name|ESAllocationTestCase
block|{
DECL|field|routingService
specifier|private
name|TestRoutingService
name|routingService
decl_stmt|;
annotation|@
name|Before
DECL|method|createRoutingService
specifier|public
name|void
name|createRoutingService
parameter_list|()
block|{
name|routingService
operator|=
operator|new
name|TestRoutingService
argument_list|()
expr_stmt|;
block|}
DECL|method|testReroute
specifier|public
name|void
name|testReroute
parameter_list|()
block|{
name|assertThat
argument_list|(
name|routingService
operator|.
name|hasReroutedAndClear
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|routingService
operator|.
name|reroute
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|routingService
operator|.
name|hasReroutedAndClear
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|class|TestRoutingService
specifier|private
class|class
name|TestRoutingService
extends|extends
name|RoutingService
block|{
DECL|field|rerouted
specifier|private
name|AtomicBoolean
name|rerouted
init|=
operator|new
name|AtomicBoolean
argument_list|()
decl_stmt|;
DECL|method|TestRoutingService
specifier|public
name|TestRoutingService
parameter_list|()
block|{
name|super
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
DECL|method|hasReroutedAndClear
specifier|public
name|boolean
name|hasReroutedAndClear
parameter_list|()
block|{
return|return
name|rerouted
operator|.
name|getAndSet
argument_list|(
literal|false
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|performReroute
specifier|protected
name|void
name|performReroute
parameter_list|(
name|String
name|reason
parameter_list|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"--> performing fake reroute [{}]"
argument_list|,
name|reason
argument_list|)
expr_stmt|;
name|rerouted
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

