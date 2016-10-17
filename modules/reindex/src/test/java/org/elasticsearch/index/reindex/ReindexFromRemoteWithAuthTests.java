begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.reindex
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|reindex
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchStatusException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchSecurityException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|ActionListener
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|ActionRequest
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|ActionResponse
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|admin
operator|.
name|cluster
operator|.
name|node
operator|.
name|info
operator|.
name|NodeInfo
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|search
operator|.
name|SearchAction
import|;
end_import

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
name|ActionFilter
import|;
end_import

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
name|ActionFilterChain
import|;
end_import

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
name|WriteRequest
operator|.
name|RefreshPolicy
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
name|bytes
operator|.
name|BytesArray
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
name|TransportAddress
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
name|concurrent
operator|.
name|ThreadContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|reindex
operator|.
name|remote
operator|.
name|RemoteInfo
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
name|ActionPlugin
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
name|rest
operator|.
name|RestStatus
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
name|ESSingleNodeTestCase
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
name|Netty4Plugin
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
name|Arrays
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
name|List
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|util
operator|.
name|Collections
operator|.
name|emptyMap
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|util
operator|.
name|Collections
operator|.
name|singletonList
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|util
operator|.
name|Collections
operator|.
name|singletonMap
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|reindex
operator|.
name|ReindexTestCase
operator|.
name|matcher
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
name|containsString
import|;
end_import

begin_class
DECL|class|ReindexFromRemoteWithAuthTests
specifier|public
class|class
name|ReindexFromRemoteWithAuthTests
extends|extends
name|ESSingleNodeTestCase
block|{
DECL|field|address
specifier|private
name|TransportAddress
name|address
decl_stmt|;
annotation|@
name|Override
DECL|method|getPlugins
specifier|protected
name|Collection
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|Plugin
argument_list|>
argument_list|>
name|getPlugins
parameter_list|()
block|{
return|return
name|Arrays
operator|.
name|asList
argument_list|(
name|Netty4Plugin
operator|.
name|class
argument_list|,
name|ReindexFromRemoteWithAuthTests
operator|.
name|TestPlugin
operator|.
name|class
argument_list|,
name|ReindexPlugin
operator|.
name|class
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|nodeSettings
specifier|protected
name|Settings
name|nodeSettings
parameter_list|()
block|{
name|Settings
operator|.
name|Builder
name|settings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|super
operator|.
name|nodeSettings
argument_list|()
argument_list|)
decl_stmt|;
comment|// Weird incantation required to test with netty
name|settings
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
literal|true
argument_list|)
expr_stmt|;
comment|// Whitelist reindexing from the http host we're going to use
name|settings
operator|.
name|put
argument_list|(
name|TransportReindexAction
operator|.
name|REMOTE_CLUSTER_WHITELIST
operator|.
name|getKey
argument_list|()
argument_list|,
literal|"myself"
argument_list|)
expr_stmt|;
name|settings
operator|.
name|put
argument_list|(
name|NetworkModule
operator|.
name|HTTP_TYPE_KEY
argument_list|,
name|Netty4Plugin
operator|.
name|NETTY_HTTP_TRANSPORT_NAME
argument_list|)
expr_stmt|;
return|return
name|settings
operator|.
name|build
argument_list|()
return|;
block|}
annotation|@
name|Before
DECL|method|setupSourceIndex
specifier|public
name|void
name|setupSourceIndex
parameter_list|()
block|{
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"source"
argument_list|,
literal|"test"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"test"
argument_list|,
literal|"test"
argument_list|)
operator|.
name|setRefreshPolicy
argument_list|(
name|RefreshPolicy
operator|.
name|IMMEDIATE
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Before
DECL|method|fetchTransportAddress
specifier|public
name|void
name|fetchTransportAddress
parameter_list|()
block|{
name|NodeInfo
name|nodeInfo
init|=
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareNodesInfo
argument_list|()
operator|.
name|get
argument_list|()
operator|.
name|getNodes
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|address
operator|=
name|nodeInfo
operator|.
name|getHttp
argument_list|()
operator|.
name|getAddress
argument_list|()
operator|.
name|publishAddress
argument_list|()
expr_stmt|;
block|}
DECL|method|testReindexFromRemoteWithAuthentication
specifier|public
name|void
name|testReindexFromRemoteWithAuthentication
parameter_list|()
throws|throws
name|Exception
block|{
name|RemoteInfo
name|remote
init|=
operator|new
name|RemoteInfo
argument_list|(
literal|"http"
argument_list|,
name|address
operator|.
name|getHost
argument_list|()
argument_list|,
name|address
operator|.
name|getPort
argument_list|()
argument_list|,
operator|new
name|BytesArray
argument_list|(
literal|"{\"match_all\":{}}"
argument_list|)
argument_list|,
literal|"Aladdin"
argument_list|,
literal|"open sesame"
argument_list|,
name|emptyMap
argument_list|()
argument_list|)
decl_stmt|;
name|ReindexRequestBuilder
name|request
init|=
name|ReindexAction
operator|.
name|INSTANCE
operator|.
name|newRequestBuilder
argument_list|(
name|client
argument_list|()
argument_list|)
operator|.
name|source
argument_list|(
literal|"source"
argument_list|)
operator|.
name|destination
argument_list|(
literal|"dest"
argument_list|)
operator|.
name|setRemoteInfo
argument_list|(
name|remote
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|get
argument_list|()
argument_list|,
name|matcher
argument_list|()
operator|.
name|created
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testReindexSendsHeaders
specifier|public
name|void
name|testReindexSendsHeaders
parameter_list|()
throws|throws
name|Exception
block|{
name|RemoteInfo
name|remote
init|=
operator|new
name|RemoteInfo
argument_list|(
literal|"http"
argument_list|,
name|address
operator|.
name|getHost
argument_list|()
argument_list|,
name|address
operator|.
name|getPort
argument_list|()
argument_list|,
operator|new
name|BytesArray
argument_list|(
literal|"{\"match_all\":{}}"
argument_list|)
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|singletonMap
argument_list|(
name|TestFilter
operator|.
name|EXAMPLE_HEADER
argument_list|,
literal|"doesn't matter"
argument_list|)
argument_list|)
decl_stmt|;
name|ReindexRequestBuilder
name|request
init|=
name|ReindexAction
operator|.
name|INSTANCE
operator|.
name|newRequestBuilder
argument_list|(
name|client
argument_list|()
argument_list|)
operator|.
name|source
argument_list|(
literal|"source"
argument_list|)
operator|.
name|destination
argument_list|(
literal|"dest"
argument_list|)
operator|.
name|setRemoteInfo
argument_list|(
name|remote
argument_list|)
decl_stmt|;
name|ElasticsearchStatusException
name|e
init|=
name|expectThrows
argument_list|(
name|ElasticsearchStatusException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|request
operator|.
name|get
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|RestStatus
operator|.
name|BAD_REQUEST
argument_list|,
name|e
operator|.
name|status
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"Hurray! Sent the header!"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testReindexWithoutAuthenticationWhenRequired
specifier|public
name|void
name|testReindexWithoutAuthenticationWhenRequired
parameter_list|()
throws|throws
name|Exception
block|{
name|RemoteInfo
name|remote
init|=
operator|new
name|RemoteInfo
argument_list|(
literal|"http"
argument_list|,
name|address
operator|.
name|getHost
argument_list|()
argument_list|,
name|address
operator|.
name|getPort
argument_list|()
argument_list|,
operator|new
name|BytesArray
argument_list|(
literal|"{\"match_all\":{}}"
argument_list|)
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|emptyMap
argument_list|()
argument_list|)
decl_stmt|;
name|ReindexRequestBuilder
name|request
init|=
name|ReindexAction
operator|.
name|INSTANCE
operator|.
name|newRequestBuilder
argument_list|(
name|client
argument_list|()
argument_list|)
operator|.
name|source
argument_list|(
literal|"source"
argument_list|)
operator|.
name|destination
argument_list|(
literal|"dest"
argument_list|)
operator|.
name|setRemoteInfo
argument_list|(
name|remote
argument_list|)
decl_stmt|;
name|ElasticsearchStatusException
name|e
init|=
name|expectThrows
argument_list|(
name|ElasticsearchStatusException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|request
operator|.
name|get
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|RestStatus
operator|.
name|UNAUTHORIZED
argument_list|,
name|e
operator|.
name|status
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"\"reason\":\"Authentication required\""
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"\"WWW-Authenticate\":\"Basic realm=auth-realm\""
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testReindexWithBadAuthentication
specifier|public
name|void
name|testReindexWithBadAuthentication
parameter_list|()
throws|throws
name|Exception
block|{
name|RemoteInfo
name|remote
init|=
operator|new
name|RemoteInfo
argument_list|(
literal|"http"
argument_list|,
name|address
operator|.
name|getHost
argument_list|()
argument_list|,
name|address
operator|.
name|getPort
argument_list|()
argument_list|,
operator|new
name|BytesArray
argument_list|(
literal|"{\"match_all\":{}}"
argument_list|)
argument_list|,
literal|"junk"
argument_list|,
literal|"auth"
argument_list|,
name|emptyMap
argument_list|()
argument_list|)
decl_stmt|;
name|ReindexRequestBuilder
name|request
init|=
name|ReindexAction
operator|.
name|INSTANCE
operator|.
name|newRequestBuilder
argument_list|(
name|client
argument_list|()
argument_list|)
operator|.
name|source
argument_list|(
literal|"source"
argument_list|)
operator|.
name|destination
argument_list|(
literal|"dest"
argument_list|)
operator|.
name|setRemoteInfo
argument_list|(
name|remote
argument_list|)
decl_stmt|;
name|ElasticsearchStatusException
name|e
init|=
name|expectThrows
argument_list|(
name|ElasticsearchStatusException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|request
operator|.
name|get
argument_list|()
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"\"reason\":\"Bad Authorization\""
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**      * Plugin that demands authentication.      */
DECL|class|TestPlugin
specifier|public
specifier|static
class|class
name|TestPlugin
extends|extends
name|Plugin
implements|implements
name|ActionPlugin
block|{
annotation|@
name|Override
DECL|method|getActionFilters
specifier|public
name|List
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|ActionFilter
argument_list|>
argument_list|>
name|getActionFilters
parameter_list|()
block|{
return|return
name|singletonList
argument_list|(
name|ReindexFromRemoteWithAuthTests
operator|.
name|TestFilter
operator|.
name|class
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|getRestHeaders
specifier|public
name|Collection
argument_list|<
name|String
argument_list|>
name|getRestHeaders
parameter_list|()
block|{
return|return
name|Arrays
operator|.
name|asList
argument_list|(
name|TestFilter
operator|.
name|AUTHORIZATION_HEADER
argument_list|,
name|TestFilter
operator|.
name|EXAMPLE_HEADER
argument_list|)
return|;
block|}
block|}
comment|/**      * Action filter that will reject the request if it isn't authenticated.      */
DECL|class|TestFilter
specifier|public
specifier|static
class|class
name|TestFilter
implements|implements
name|ActionFilter
block|{
comment|/**          * The authorization required. Corresponds to username="Aladdin" and password="open sesame". It is the example in          *<a href="https://tools.ietf.org/html/rfc1945#section-11.1">HTTP/1.0's RFC</a>.          */
DECL|field|REQUIRED_AUTH
specifier|private
specifier|static
specifier|final
name|String
name|REQUIRED_AUTH
init|=
literal|"Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ=="
decl_stmt|;
DECL|field|AUTHORIZATION_HEADER
specifier|private
specifier|static
specifier|final
name|String
name|AUTHORIZATION_HEADER
init|=
literal|"Authorization"
decl_stmt|;
DECL|field|EXAMPLE_HEADER
specifier|private
specifier|static
specifier|final
name|String
name|EXAMPLE_HEADER
init|=
literal|"Example-Header"
decl_stmt|;
DECL|field|context
specifier|private
specifier|final
name|ThreadContext
name|context
decl_stmt|;
annotation|@
name|Inject
DECL|method|TestFilter
specifier|public
name|TestFilter
parameter_list|(
name|ThreadPool
name|threadPool
parameter_list|)
block|{
name|context
operator|=
name|threadPool
operator|.
name|getThreadContext
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|order
specifier|public
name|int
name|order
parameter_list|()
block|{
return|return
name|Integer
operator|.
name|MIN_VALUE
return|;
block|}
annotation|@
name|Override
DECL|method|apply
specifier|public
parameter_list|<
name|Request
extends|extends
name|ActionRequest
argument_list|<
name|Request
argument_list|>
parameter_list|,
name|Response
extends|extends
name|ActionResponse
parameter_list|>
name|void
name|apply
parameter_list|(
name|Task
name|task
parameter_list|,
name|String
name|action
parameter_list|,
name|Request
name|request
parameter_list|,
name|ActionListener
argument_list|<
name|Response
argument_list|>
name|listener
parameter_list|,
name|ActionFilterChain
argument_list|<
name|Request
argument_list|,
name|Response
argument_list|>
name|chain
parameter_list|)
block|{
if|if
condition|(
literal|false
operator|==
name|action
operator|.
name|equals
argument_list|(
name|SearchAction
operator|.
name|NAME
argument_list|)
condition|)
block|{
name|chain
operator|.
name|proceed
argument_list|(
name|task
argument_list|,
name|action
argument_list|,
name|request
argument_list|,
name|listener
argument_list|)
expr_stmt|;
return|return;
block|}
if|if
condition|(
name|context
operator|.
name|getHeader
argument_list|(
name|EXAMPLE_HEADER
argument_list|)
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Hurray! Sent the header!"
argument_list|)
throw|;
block|}
name|String
name|auth
init|=
name|context
operator|.
name|getHeader
argument_list|(
name|AUTHORIZATION_HEADER
argument_list|)
decl_stmt|;
if|if
condition|(
name|auth
operator|==
literal|null
condition|)
block|{
name|ElasticsearchSecurityException
name|e
init|=
operator|new
name|ElasticsearchSecurityException
argument_list|(
literal|"Authentication required"
argument_list|,
name|RestStatus
operator|.
name|UNAUTHORIZED
argument_list|)
decl_stmt|;
name|e
operator|.
name|addHeader
argument_list|(
literal|"WWW-Authenticate"
argument_list|,
literal|"Basic realm=auth-realm"
argument_list|)
expr_stmt|;
throw|throw
name|e
throw|;
block|}
if|if
condition|(
literal|false
operator|==
name|REQUIRED_AUTH
operator|.
name|equals
argument_list|(
name|auth
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|ElasticsearchSecurityException
argument_list|(
literal|"Bad Authorization"
argument_list|,
name|RestStatus
operator|.
name|FORBIDDEN
argument_list|)
throw|;
block|}
name|chain
operator|.
name|proceed
argument_list|(
name|task
argument_list|,
name|action
argument_list|,
name|request
argument_list|,
name|listener
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|apply
specifier|public
parameter_list|<
name|Response
extends|extends
name|ActionResponse
parameter_list|>
name|void
name|apply
parameter_list|(
name|String
name|action
parameter_list|,
name|Response
name|response
parameter_list|,
name|ActionListener
argument_list|<
name|Response
argument_list|>
name|listener
parameter_list|,
name|ActionFilterChain
argument_list|<
name|?
argument_list|,
name|Response
argument_list|>
name|chain
parameter_list|)
block|{
name|chain
operator|.
name|proceed
argument_list|(
name|action
argument_list|,
name|response
argument_list|,
name|listener
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit
