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
name|common
operator|.
name|inject
operator|.
name|Inject
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
name|ModuleTestCase
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
name|indices
operator|.
name|breaker
operator|.
name|CircuitBreakerService
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
name|AssertingLocalTransport
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|threadpool
operator|.
name|ThreadPool
import|;
end_import

begin_comment
comment|/** Unit tests for module registering custom transport and transport service */
end_comment

begin_class
DECL|class|TransportModuleTests
specifier|public
class|class
name|TransportModuleTests
extends|extends
name|ModuleTestCase
block|{
DECL|class|FakeTransport
specifier|static
class|class
name|FakeTransport
extends|extends
name|AssertingLocalTransport
block|{
annotation|@
name|Inject
DECL|method|FakeTransport
specifier|public
name|FakeTransport
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|CircuitBreakerService
name|circuitBreakerService
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|,
name|Version
name|version
parameter_list|,
name|NamedWriteableRegistry
name|namedWriteableRegistry
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|circuitBreakerService
argument_list|,
name|threadPool
argument_list|,
name|version
argument_list|,
name|namedWriteableRegistry
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|FakeTransportService
specifier|static
class|class
name|FakeTransportService
extends|extends
name|TransportService
block|{
annotation|@
name|Inject
DECL|method|FakeTransportService
specifier|public
name|FakeTransportService
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|Transport
name|transport
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|transport
argument_list|,
name|threadPool
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

