begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.network
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|network
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|support
operator|.
name|replication
operator|.
name|ReplicationTask
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|client
operator|.
name|node
operator|.
name|NodeClient
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
name|Table
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
name|component
operator|.
name|AbstractLifecycleComponent
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
name|io
operator|.
name|stream
operator|.
name|StreamInput
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
name|StreamOutput
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
name|transport
operator|.
name|BoundTransportAddress
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
name|xcontent
operator|.
name|XContentBuilder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|http
operator|.
name|HttpInfo
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|http
operator|.
name|HttpServerAdapter
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|http
operator|.
name|HttpServerTransport
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|http
operator|.
name|HttpStats
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|BaseRestHandler
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|RestChannel
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|RestRequest
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|action
operator|.
name|cat
operator|.
name|AbstractCatAction
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|tasks
operator|.
name|Task
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
name|transport
operator|.
name|Transport
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
name|io
operator|.
name|IOException
import|;
end_import

begin_class
DECL|class|NetworkModuleTests
specifier|public
class|class
name|NetworkModuleTests
extends|extends
name|ModuleTestCase
block|{
DECL|class|FakeTransportService
specifier|static
class|class
name|FakeTransportService
extends|extends
name|TransportService
block|{
DECL|method|FakeTransportService
specifier|public
name|FakeTransportService
parameter_list|()
block|{
name|super
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|FakeTransport
specifier|static
class|class
name|FakeTransport
extends|extends
name|AssertingLocalTransport
block|{
DECL|method|FakeTransport
specifier|public
name|FakeTransport
parameter_list|()
block|{
name|super
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|FakeHttpTransport
specifier|static
class|class
name|FakeHttpTransport
extends|extends
name|AbstractLifecycleComponent
implements|implements
name|HttpServerTransport
block|{
DECL|method|FakeHttpTransport
specifier|public
name|FakeHttpTransport
parameter_list|()
block|{
name|super
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doStart
specifier|protected
name|void
name|doStart
parameter_list|()
block|{}
annotation|@
name|Override
DECL|method|doStop
specifier|protected
name|void
name|doStop
parameter_list|()
block|{}
annotation|@
name|Override
DECL|method|doClose
specifier|protected
name|void
name|doClose
parameter_list|()
block|{}
annotation|@
name|Override
DECL|method|boundAddress
specifier|public
name|BoundTransportAddress
name|boundAddress
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|info
specifier|public
name|HttpInfo
name|info
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|stats
specifier|public
name|HttpStats
name|stats
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|httpServerAdapter
specifier|public
name|void
name|httpServerAdapter
parameter_list|(
name|HttpServerAdapter
name|httpServerAdapter
parameter_list|)
block|{}
block|}
DECL|class|FakeRestHandler
specifier|static
class|class
name|FakeRestHandler
extends|extends
name|BaseRestHandler
block|{
DECL|method|FakeRestHandler
specifier|public
name|FakeRestHandler
parameter_list|()
block|{
name|super
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|handleRequest
specifier|public
name|void
name|handleRequest
parameter_list|(
name|RestRequest
name|request
parameter_list|,
name|RestChannel
name|channel
parameter_list|,
name|NodeClient
name|client
parameter_list|)
throws|throws
name|Exception
block|{}
block|}
DECL|class|FakeCatRestHandler
specifier|static
class|class
name|FakeCatRestHandler
extends|extends
name|AbstractCatAction
block|{
DECL|method|FakeCatRestHandler
specifier|public
name|FakeCatRestHandler
parameter_list|()
block|{
name|super
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doRequest
specifier|protected
name|void
name|doRequest
parameter_list|(
name|RestRequest
name|request
parameter_list|,
name|RestChannel
name|channel
parameter_list|,
name|NodeClient
name|client
parameter_list|)
block|{}
annotation|@
name|Override
DECL|method|documentation
specifier|protected
name|void
name|documentation
parameter_list|(
name|StringBuilder
name|sb
parameter_list|)
block|{}
annotation|@
name|Override
DECL|method|getTableWithHeader
specifier|protected
name|Table
name|getTableWithHeader
parameter_list|(
name|RestRequest
name|request
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
block|}
DECL|method|testRegisterTransportService
specifier|public
name|void
name|testRegisterTransportService
parameter_list|()
block|{
name|Settings
name|settings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|NetworkModule
operator|.
name|TRANSPORT_SERVICE_TYPE_KEY
argument_list|,
literal|"custom"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|NetworkModule
name|module
init|=
operator|new
name|NetworkModule
argument_list|(
operator|new
name|NetworkService
argument_list|(
name|settings
argument_list|)
argument_list|,
name|settings
argument_list|,
literal|false
argument_list|,
operator|new
name|NamedWriteableRegistry
argument_list|()
argument_list|)
decl_stmt|;
name|module
operator|.
name|registerTransportService
argument_list|(
literal|"custom"
argument_list|,
name|FakeTransportService
operator|.
name|class
argument_list|)
expr_stmt|;
name|assertBinding
argument_list|(
name|module
argument_list|,
name|TransportService
operator|.
name|class
argument_list|,
name|FakeTransportService
operator|.
name|class
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|module
operator|.
name|isTransportClient
argument_list|()
argument_list|)
expr_stmt|;
comment|// check it works with transport only as well
name|module
operator|=
operator|new
name|NetworkModule
argument_list|(
operator|new
name|NetworkService
argument_list|(
name|settings
argument_list|)
argument_list|,
name|settings
argument_list|,
literal|true
argument_list|,
operator|new
name|NamedWriteableRegistry
argument_list|()
argument_list|)
expr_stmt|;
name|module
operator|.
name|registerTransportService
argument_list|(
literal|"custom"
argument_list|,
name|FakeTransportService
operator|.
name|class
argument_list|)
expr_stmt|;
name|assertBinding
argument_list|(
name|module
argument_list|,
name|TransportService
operator|.
name|class
argument_list|,
name|FakeTransportService
operator|.
name|class
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|module
operator|.
name|isTransportClient
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testRegisterTransport
specifier|public
name|void
name|testRegisterTransport
parameter_list|()
block|{
name|Settings
name|settings
init|=
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
literal|"custom"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|NetworkModule
name|module
init|=
operator|new
name|NetworkModule
argument_list|(
operator|new
name|NetworkService
argument_list|(
name|settings
argument_list|)
argument_list|,
name|settings
argument_list|,
literal|false
argument_list|,
operator|new
name|NamedWriteableRegistry
argument_list|()
argument_list|)
decl_stmt|;
name|module
operator|.
name|registerTransport
argument_list|(
literal|"custom"
argument_list|,
name|FakeTransport
operator|.
name|class
argument_list|)
expr_stmt|;
name|assertBinding
argument_list|(
name|module
argument_list|,
name|Transport
operator|.
name|class
argument_list|,
name|FakeTransport
operator|.
name|class
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|module
operator|.
name|isTransportClient
argument_list|()
argument_list|)
expr_stmt|;
comment|// check it works with transport only as well
name|module
operator|=
operator|new
name|NetworkModule
argument_list|(
operator|new
name|NetworkService
argument_list|(
name|settings
argument_list|)
argument_list|,
name|settings
argument_list|,
literal|true
argument_list|,
operator|new
name|NamedWriteableRegistry
argument_list|()
argument_list|)
expr_stmt|;
name|module
operator|.
name|registerTransport
argument_list|(
literal|"custom"
argument_list|,
name|FakeTransport
operator|.
name|class
argument_list|)
expr_stmt|;
name|assertBinding
argument_list|(
name|module
argument_list|,
name|Transport
operator|.
name|class
argument_list|,
name|FakeTransport
operator|.
name|class
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|module
operator|.
name|isTransportClient
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testRegisterHttpTransport
specifier|public
name|void
name|testRegisterHttpTransport
parameter_list|()
block|{
name|Settings
name|settings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|NetworkModule
operator|.
name|HTTP_TYPE_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
literal|"custom"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|NetworkModule
name|module
init|=
operator|new
name|NetworkModule
argument_list|(
operator|new
name|NetworkService
argument_list|(
name|settings
argument_list|)
argument_list|,
name|settings
argument_list|,
literal|false
argument_list|,
operator|new
name|NamedWriteableRegistry
argument_list|()
argument_list|)
decl_stmt|;
name|module
operator|.
name|registerHttpTransport
argument_list|(
literal|"custom"
argument_list|,
name|FakeHttpTransport
operator|.
name|class
argument_list|)
expr_stmt|;
name|assertBinding
argument_list|(
name|module
argument_list|,
name|HttpServerTransport
operator|.
name|class
argument_list|,
name|FakeHttpTransport
operator|.
name|class
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|module
operator|.
name|isTransportClient
argument_list|()
argument_list|)
expr_stmt|;
comment|// check registration not allowed for transport only
name|module
operator|=
operator|new
name|NetworkModule
argument_list|(
operator|new
name|NetworkService
argument_list|(
name|settings
argument_list|)
argument_list|,
name|settings
argument_list|,
literal|true
argument_list|,
operator|new
name|NamedWriteableRegistry
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|module
operator|.
name|isTransportClient
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
name|module
operator|.
name|registerHttpTransport
argument_list|(
literal|"custom"
argument_list|,
name|FakeHttpTransport
operator|.
name|class
argument_list|)
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
name|assertTrue
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"Cannot register http transport"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"for transport client"
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// not added if http is disabled
name|settings
operator|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|NetworkModule
operator|.
name|HTTP_ENABLED
operator|.
name|getKey
argument_list|()
argument_list|,
literal|false
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|module
operator|=
operator|new
name|NetworkModule
argument_list|(
operator|new
name|NetworkService
argument_list|(
name|settings
argument_list|)
argument_list|,
name|settings
argument_list|,
literal|false
argument_list|,
operator|new
name|NamedWriteableRegistry
argument_list|()
argument_list|)
expr_stmt|;
name|assertNotBound
argument_list|(
name|module
argument_list|,
name|HttpServerTransport
operator|.
name|class
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|module
operator|.
name|isTransportClient
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testRegisterTaskStatus
specifier|public
name|void
name|testRegisterTaskStatus
parameter_list|()
block|{
name|NamedWriteableRegistry
name|registry
init|=
operator|new
name|NamedWriteableRegistry
argument_list|()
decl_stmt|;
name|Settings
name|settings
init|=
name|Settings
operator|.
name|EMPTY
decl_stmt|;
name|NetworkModule
name|module
init|=
operator|new
name|NetworkModule
argument_list|(
operator|new
name|NetworkService
argument_list|(
name|settings
argument_list|)
argument_list|,
name|settings
argument_list|,
literal|false
argument_list|,
name|registry
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
name|module
operator|.
name|isTransportClient
argument_list|()
argument_list|)
expr_stmt|;
comment|// Builtin reader comes back
name|assertNotNull
argument_list|(
name|registry
operator|.
name|getReader
argument_list|(
name|Task
operator|.
name|Status
operator|.
name|class
argument_list|,
name|ReplicationTask
operator|.
name|Status
operator|.
name|NAME
argument_list|)
argument_list|)
expr_stmt|;
name|module
operator|.
name|registerTaskStatus
argument_list|(
name|DummyTaskStatus
operator|.
name|NAME
argument_list|,
name|DummyTaskStatus
operator|::
operator|new
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"test"
argument_list|,
name|expectThrows
argument_list|(
name|UnsupportedOperationException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|registry
operator|.
name|getReader
argument_list|(
name|Task
operator|.
name|Status
operator|.
name|class
argument_list|,
name|DummyTaskStatus
operator|.
name|NAME
argument_list|)
operator|.
name|read
argument_list|(
literal|null
argument_list|)
argument_list|)
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|class|DummyTaskStatus
specifier|private
class|class
name|DummyTaskStatus
implements|implements
name|Task
operator|.
name|Status
block|{
DECL|field|NAME
specifier|public
specifier|static
specifier|final
name|String
name|NAME
init|=
literal|"dummy"
decl_stmt|;
DECL|method|DummyTaskStatus
specifier|public
name|DummyTaskStatus
parameter_list|(
name|StreamInput
name|in
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"test"
argument_list|)
throw|;
block|}
annotation|@
name|Override
DECL|method|getWriteableName
specifier|public
name|String
name|getWriteableName
parameter_list|()
block|{
return|return
name|NAME
return|;
block|}
annotation|@
name|Override
DECL|method|writeTo
specifier|public
name|void
name|writeTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
DECL|method|toXContent
specifier|public
name|XContentBuilder
name|toXContent
parameter_list|(
name|XContentBuilder
name|builder
parameter_list|,
name|Params
name|params
parameter_list|)
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
block|}
block|}
end_class

end_unit

