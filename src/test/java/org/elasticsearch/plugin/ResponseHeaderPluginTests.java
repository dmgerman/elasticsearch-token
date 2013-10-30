begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.plugin
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|plugin
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Maps
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
name|ImmutableSettings
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
name|plugin
operator|.
name|responseheader
operator|.
name|TestResponseHeaderPlugin
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
name|rest
operator|.
name|helper
operator|.
name|HttpClient
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
name|helper
operator|.
name|HttpClientResponse
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
name|AbstractIntegrationTest
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
name|AbstractIntegrationTest
operator|.
name|ClusterScope
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
name|AbstractIntegrationTest
operator|.
name|Scope
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
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
comment|/**  * Test a rest action that sets special response headers  */
end_comment

begin_class
annotation|@
name|ClusterScope
argument_list|(
name|scope
operator|=
name|Scope
operator|.
name|SUITE
argument_list|,
name|numNodes
operator|=
literal|1
argument_list|)
DECL|class|ResponseHeaderPluginTests
specifier|public
class|class
name|ResponseHeaderPluginTests
extends|extends
name|AbstractIntegrationTest
block|{
annotation|@
name|Override
DECL|method|nodeSettings
specifier|protected
name|Settings
name|nodeSettings
parameter_list|(
name|int
name|nodeOrdinal
parameter_list|)
block|{
return|return
name|ImmutableSettings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"plugin.types"
argument_list|,
name|TestResponseHeaderPlugin
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
literal|"force.http.enabled"
argument_list|,
literal|true
argument_list|)
operator|.
name|put
argument_list|(
name|super
operator|.
name|nodeSettings
argument_list|(
name|nodeOrdinal
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
annotation|@
name|Test
DECL|method|testThatSettingHeadersWorks
specifier|public
name|void
name|testThatSettingHeadersWorks
parameter_list|()
throws|throws
name|Exception
block|{
name|ensureGreen
argument_list|()
expr_stmt|;
name|HttpClientResponse
name|response
init|=
name|httpClient
argument_list|()
operator|.
name|request
argument_list|(
literal|"/_protected"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|errorCode
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|RestStatus
operator|.
name|UNAUTHORIZED
operator|.
name|getStatus
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getHeader
argument_list|(
literal|"Secret"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"required"
argument_list|)
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|headers
init|=
name|Maps
operator|.
name|newHashMap
argument_list|()
decl_stmt|;
name|headers
operator|.
name|put
argument_list|(
literal|"Secret"
argument_list|,
literal|"password"
argument_list|)
expr_stmt|;
name|HttpClientResponse
name|authResponse
init|=
name|httpClient
argument_list|()
operator|.
name|request
argument_list|(
literal|"GET"
argument_list|,
literal|"_protected"
argument_list|,
name|headers
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|authResponse
operator|.
name|errorCode
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|RestStatus
operator|.
name|OK
operator|.
name|getStatus
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|authResponse
operator|.
name|getHeader
argument_list|(
literal|"Secret"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"granted"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|httpClient
specifier|private
name|HttpClient
name|httpClient
parameter_list|()
block|{
name|HttpServerTransport
name|httpServerTransport
init|=
name|cluster
argument_list|()
operator|.
name|getInstance
argument_list|(
name|HttpServerTransport
operator|.
name|class
argument_list|)
decl_stmt|;
return|return
operator|new
name|HttpClient
argument_list|(
name|httpServerTransport
operator|.
name|boundAddress
argument_list|()
operator|.
name|publishAddress
argument_list|()
argument_list|)
return|;
block|}
block|}
end_class

end_unit

