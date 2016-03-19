begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.test.transport
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|test
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
name|NetworkModule
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
name|Setting
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
name|Setting
operator|.
name|Property
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
name|settings
operator|.
name|SettingsModule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|plugins
operator|.
name|Plugin
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
name|ESIntegTestCase
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
name|VersionUtils
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
name|hamcrest
operator|.
name|ElasticsearchAssertions
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

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|transport
operator|.
name|TransportException
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
name|TransportRequest
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
name|TransportRequestOptions
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
name|TransportResponse
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
name|TransportResponseHandler
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
name|local
operator|.
name|LocalTransport
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
name|Random
import|;
end_import

begin_class
DECL|class|AssertingLocalTransport
specifier|public
class|class
name|AssertingLocalTransport
extends|extends
name|LocalTransport
block|{
DECL|class|TestPlugin
specifier|public
specifier|static
class|class
name|TestPlugin
extends|extends
name|Plugin
block|{
annotation|@
name|Override
DECL|method|name
specifier|public
name|String
name|name
parameter_list|()
block|{
return|return
literal|"asserting-local-transport"
return|;
block|}
annotation|@
name|Override
DECL|method|description
specifier|public
name|String
name|description
parameter_list|()
block|{
return|return
literal|"an asserting transport for testing"
return|;
block|}
DECL|method|onModule
specifier|public
name|void
name|onModule
parameter_list|(
name|NetworkModule
name|module
parameter_list|)
block|{
name|module
operator|.
name|registerTransport
argument_list|(
literal|"mock"
argument_list|,
name|AssertingLocalTransport
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|additionalSettings
specifier|public
name|Settings
name|additionalSettings
parameter_list|()
block|{
return|return
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|NetworkModule
operator|.
name|TRANSPORT_TYPE_KEY
argument_list|,
literal|"mock"
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
DECL|method|onModule
specifier|public
name|void
name|onModule
parameter_list|(
name|SettingsModule
name|module
parameter_list|)
block|{
name|module
operator|.
name|registerSetting
argument_list|(
name|ASSERTING_TRANSPORT_MIN_VERSION_KEY
argument_list|)
expr_stmt|;
name|module
operator|.
name|registerSetting
argument_list|(
name|ASSERTING_TRANSPORT_MAX_VERSION_KEY
argument_list|)
expr_stmt|;
block|}
block|}
DECL|field|ASSERTING_TRANSPORT_MIN_VERSION_KEY
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|Version
argument_list|>
name|ASSERTING_TRANSPORT_MIN_VERSION_KEY
init|=
operator|new
name|Setting
argument_list|<>
argument_list|(
literal|"transport.asserting.version.min"
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|Version
operator|.
name|CURRENT
operator|.
name|minimumCompatibilityVersion
argument_list|()
operator|.
name|id
argument_list|)
argument_list|,
parameter_list|(
name|s
parameter_list|)
lambda|->
name|Version
operator|.
name|fromId
argument_list|(
name|Integer
operator|.
name|parseInt
argument_list|(
name|s
argument_list|)
argument_list|)
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|)
decl_stmt|;
DECL|field|ASSERTING_TRANSPORT_MAX_VERSION_KEY
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|Version
argument_list|>
name|ASSERTING_TRANSPORT_MAX_VERSION_KEY
init|=
operator|new
name|Setting
argument_list|<>
argument_list|(
literal|"transport.asserting.version.max"
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|Version
operator|.
name|CURRENT
operator|.
name|id
argument_list|)
argument_list|,
parameter_list|(
name|s
parameter_list|)
lambda|->
name|Version
operator|.
name|fromId
argument_list|(
name|Integer
operator|.
name|parseInt
argument_list|(
name|s
argument_list|)
argument_list|)
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|)
decl_stmt|;
DECL|field|random
specifier|private
specifier|final
name|Random
name|random
decl_stmt|;
DECL|field|minVersion
specifier|private
specifier|final
name|Version
name|minVersion
decl_stmt|;
DECL|field|maxVersion
specifier|private
specifier|final
name|Version
name|maxVersion
decl_stmt|;
annotation|@
name|Inject
DECL|method|AssertingLocalTransport
specifier|public
name|AssertingLocalTransport
parameter_list|(
name|Settings
name|settings
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
name|threadPool
argument_list|,
name|version
argument_list|,
name|namedWriteableRegistry
argument_list|)
expr_stmt|;
specifier|final
name|long
name|seed
init|=
name|ESIntegTestCase
operator|.
name|INDEX_TEST_SEED_SETTING
operator|.
name|get
argument_list|(
name|settings
argument_list|)
decl_stmt|;
name|random
operator|=
operator|new
name|Random
argument_list|(
name|seed
argument_list|)
expr_stmt|;
name|minVersion
operator|=
name|ASSERTING_TRANSPORT_MIN_VERSION_KEY
operator|.
name|get
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|maxVersion
operator|=
name|ASSERTING_TRANSPORT_MAX_VERSION_KEY
operator|.
name|get
argument_list|(
name|settings
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|handleParsedResponse
specifier|protected
name|void
name|handleParsedResponse
parameter_list|(
specifier|final
name|TransportResponse
name|response
parameter_list|,
specifier|final
name|TransportResponseHandler
name|handler
parameter_list|)
block|{
name|ElasticsearchAssertions
operator|.
name|assertVersionSerializable
argument_list|(
name|VersionUtils
operator|.
name|randomVersionBetween
argument_list|(
name|random
argument_list|,
name|minVersion
argument_list|,
name|maxVersion
argument_list|)
argument_list|,
name|response
argument_list|,
name|namedWriteableRegistry
argument_list|)
expr_stmt|;
name|super
operator|.
name|handleParsedResponse
argument_list|(
name|response
argument_list|,
name|handler
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|sendRequest
specifier|public
name|void
name|sendRequest
parameter_list|(
specifier|final
name|DiscoveryNode
name|node
parameter_list|,
specifier|final
name|long
name|requestId
parameter_list|,
specifier|final
name|String
name|action
parameter_list|,
specifier|final
name|TransportRequest
name|request
parameter_list|,
name|TransportRequestOptions
name|options
parameter_list|)
throws|throws
name|IOException
throws|,
name|TransportException
block|{
name|ElasticsearchAssertions
operator|.
name|assertVersionSerializable
argument_list|(
name|VersionUtils
operator|.
name|randomVersionBetween
argument_list|(
name|random
argument_list|,
name|minVersion
argument_list|,
name|maxVersion
argument_list|)
argument_list|,
name|request
argument_list|,
name|namedWriteableRegistry
argument_list|)
expr_stmt|;
name|super
operator|.
name|sendRequest
argument_list|(
name|node
argument_list|,
name|requestId
argument_list|,
name|action
argument_list|,
name|request
argument_list|,
name|options
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

