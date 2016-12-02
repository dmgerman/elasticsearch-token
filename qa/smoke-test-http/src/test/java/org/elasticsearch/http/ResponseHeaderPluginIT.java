begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.http
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|http
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|message
operator|.
name|BasicHeader
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
name|Response
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
name|ResponseException
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
name|ESIntegTestCase
operator|.
name|Scope
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
name|Collection
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
name|supportsDedicatedMasters
operator|=
literal|false
argument_list|,
name|numDataNodes
operator|=
literal|1
argument_list|)
DECL|class|ResponseHeaderPluginIT
specifier|public
class|class
name|ResponseHeaderPluginIT
extends|extends
name|HttpSmokeTestCase
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
argument_list|(
name|nodeOrdinal
argument_list|)
argument_list|)
operator|.
name|put
argument_list|(
literal|"force.http.enabled"
argument_list|,
literal|true
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|nodePlugins
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
name|nodePlugins
parameter_list|()
block|{
name|ArrayList
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|Plugin
argument_list|>
argument_list|>
name|plugins
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|super
operator|.
name|nodePlugins
argument_list|()
argument_list|)
decl_stmt|;
name|plugins
operator|.
name|add
argument_list|(
name|TestResponseHeaderPlugin
operator|.
name|class
argument_list|)
expr_stmt|;
return|return
name|plugins
return|;
block|}
DECL|method|testThatSettingHeadersWorks
specifier|public
name|void
name|testThatSettingHeadersWorks
parameter_list|()
throws|throws
name|IOException
block|{
name|ensureGreen
argument_list|()
expr_stmt|;
try|try
block|{
name|getRestClient
argument_list|()
operator|.
name|performRequest
argument_list|(
literal|"GET"
argument_list|,
literal|"/_protected"
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"request should have failed"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ResponseException
name|e
parameter_list|)
block|{
name|Response
name|response
init|=
name|e
operator|.
name|getResponse
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getStatusLine
argument_list|()
operator|.
name|getStatusCode
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|401
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
block|}
name|Response
name|authResponse
init|=
name|getRestClient
argument_list|()
operator|.
name|performRequest
argument_list|(
literal|"GET"
argument_list|,
literal|"/_protected"
argument_list|,
operator|new
name|BasicHeader
argument_list|(
literal|"Secret"
argument_list|,
literal|"password"
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|authResponse
operator|.
name|getStatusLine
argument_list|()
operator|.
name|getStatusCode
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|200
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
block|}
end_class

end_unit

