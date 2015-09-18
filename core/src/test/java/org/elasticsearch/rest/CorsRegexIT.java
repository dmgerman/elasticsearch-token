begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.rest
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|rest
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
name|logging
operator|.
name|ESLogger
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
name|logging
operator|.
name|Loggers
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
name|node
operator|.
name|Node
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
name|rest
operator|.
name|client
operator|.
name|http
operator|.
name|HttpResponse
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
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|http
operator|.
name|netty
operator|.
name|NettyHttpServerTransport
operator|.
name|SETTING_CORS_ALLOW_ORIGIN
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|http
operator|.
name|netty
operator|.
name|NettyHttpServerTransport
operator|.
name|SETTING_CORS_ALLOW_CREDENTIALS
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|http
operator|.
name|netty
operator|.
name|NettyHttpServerTransport
operator|.
name|SETTING_CORS_ENABLED
import|;
end_import

begin_import
import|import static
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
import|import static
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
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|*
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
name|is
import|;
end_import

begin_comment
comment|/**  *  */
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
name|numDataNodes
operator|=
literal|1
argument_list|)
DECL|class|CorsRegexIT
specifier|public
class|class
name|CorsRegexIT
extends|extends
name|ESIntegTestCase
block|{
DECL|field|logger
specifier|protected
specifier|static
specifier|final
name|ESLogger
name|logger
init|=
name|Loggers
operator|.
name|getLogger
argument_list|(
name|CorsRegexIT
operator|.
name|class
argument_list|)
decl_stmt|;
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
name|settingsBuilder
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
name|SETTING_CORS_ALLOW_ORIGIN
argument_list|,
literal|"/https?:\\/\\/localhost(:[0-9]+)?/"
argument_list|)
operator|.
name|put
argument_list|(
name|SETTING_CORS_ALLOW_CREDENTIALS
argument_list|,
literal|true
argument_list|)
operator|.
name|put
argument_list|(
name|SETTING_CORS_ENABLED
argument_list|,
literal|true
argument_list|)
operator|.
name|put
argument_list|(
name|Node
operator|.
name|HTTP_ENABLED
argument_list|,
literal|true
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
annotation|@
name|Test
DECL|method|testThatRegularExpressionWorksOnMatch
specifier|public
name|void
name|testThatRegularExpressionWorksOnMatch
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|corsValue
init|=
literal|"http://localhost:9200"
decl_stmt|;
name|HttpResponse
name|response
init|=
name|httpClient
argument_list|()
operator|.
name|method
argument_list|(
literal|"GET"
argument_list|)
operator|.
name|path
argument_list|(
literal|"/"
argument_list|)
operator|.
name|addHeader
argument_list|(
literal|"User-Agent"
argument_list|,
literal|"Mozilla Bar"
argument_list|)
operator|.
name|addHeader
argument_list|(
literal|"Origin"
argument_list|,
name|corsValue
argument_list|)
operator|.
name|execute
argument_list|()
decl_stmt|;
name|assertResponseWithOriginheader
argument_list|(
name|response
argument_list|,
name|corsValue
argument_list|)
expr_stmt|;
name|corsValue
operator|=
literal|"https://localhost:9200"
expr_stmt|;
name|response
operator|=
name|httpClient
argument_list|()
operator|.
name|method
argument_list|(
literal|"GET"
argument_list|)
operator|.
name|path
argument_list|(
literal|"/"
argument_list|)
operator|.
name|addHeader
argument_list|(
literal|"User-Agent"
argument_list|,
literal|"Mozilla Bar"
argument_list|)
operator|.
name|addHeader
argument_list|(
literal|"Origin"
argument_list|,
name|corsValue
argument_list|)
operator|.
name|execute
argument_list|()
expr_stmt|;
name|assertResponseWithOriginheader
argument_list|(
name|response
argument_list|,
name|corsValue
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getHeaders
argument_list|()
argument_list|,
name|hasKey
argument_list|(
literal|"Access-Control-Allow-Credentials"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getHeaders
argument_list|()
operator|.
name|get
argument_list|(
literal|"Access-Control-Allow-Credentials"
argument_list|)
argument_list|,
name|is
argument_list|(
literal|"true"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testThatRegularExpressionReturnsNullOnNonMatch
specifier|public
name|void
name|testThatRegularExpressionReturnsNullOnNonMatch
parameter_list|()
throws|throws
name|Exception
block|{
name|HttpResponse
name|response
init|=
name|httpClient
argument_list|()
operator|.
name|method
argument_list|(
literal|"GET"
argument_list|)
operator|.
name|path
argument_list|(
literal|"/"
argument_list|)
operator|.
name|addHeader
argument_list|(
literal|"User-Agent"
argument_list|,
literal|"Mozilla Bar"
argument_list|)
operator|.
name|addHeader
argument_list|(
literal|"Origin"
argument_list|,
literal|"http://evil-host:9200"
argument_list|)
operator|.
name|execute
argument_list|()
decl_stmt|;
name|assertResponseWithOriginheader
argument_list|(
name|response
argument_list|,
literal|"null"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testThatSendingNoOriginHeaderReturnsNoAccessControlHeader
specifier|public
name|void
name|testThatSendingNoOriginHeaderReturnsNoAccessControlHeader
parameter_list|()
throws|throws
name|Exception
block|{
name|HttpResponse
name|response
init|=
name|httpClient
argument_list|()
operator|.
name|method
argument_list|(
literal|"GET"
argument_list|)
operator|.
name|path
argument_list|(
literal|"/"
argument_list|)
operator|.
name|addHeader
argument_list|(
literal|"User-Agent"
argument_list|,
literal|"Mozilla Bar"
argument_list|)
operator|.
name|execute
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getStatusCode
argument_list|()
argument_list|,
name|is
argument_list|(
literal|200
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getHeaders
argument_list|()
argument_list|,
name|not
argument_list|(
name|hasKey
argument_list|(
literal|"Access-Control-Allow-Origin"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testThatRegularExpressionIsNotAppliedWithoutCorrectBrowserOnMatch
specifier|public
name|void
name|testThatRegularExpressionIsNotAppliedWithoutCorrectBrowserOnMatch
parameter_list|()
throws|throws
name|Exception
block|{
name|HttpResponse
name|response
init|=
name|httpClient
argument_list|()
operator|.
name|method
argument_list|(
literal|"GET"
argument_list|)
operator|.
name|path
argument_list|(
literal|"/"
argument_list|)
operator|.
name|execute
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getStatusCode
argument_list|()
argument_list|,
name|is
argument_list|(
literal|200
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getHeaders
argument_list|()
argument_list|,
name|not
argument_list|(
name|hasKey
argument_list|(
literal|"Access-Control-Allow-Origin"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testThatPreFlightRequestWorksOnMatch
specifier|public
name|void
name|testThatPreFlightRequestWorksOnMatch
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|corsValue
init|=
literal|"http://localhost:9200"
decl_stmt|;
name|HttpResponse
name|response
init|=
name|httpClient
argument_list|()
operator|.
name|method
argument_list|(
literal|"OPTIONS"
argument_list|)
operator|.
name|path
argument_list|(
literal|"/"
argument_list|)
operator|.
name|addHeader
argument_list|(
literal|"User-Agent"
argument_list|,
literal|"Mozilla Bar"
argument_list|)
operator|.
name|addHeader
argument_list|(
literal|"Origin"
argument_list|,
name|corsValue
argument_list|)
operator|.
name|execute
argument_list|()
decl_stmt|;
name|assertResponseWithOriginheader
argument_list|(
name|response
argument_list|,
name|corsValue
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testThatPreFlightRequestReturnsNullOnNonMatch
specifier|public
name|void
name|testThatPreFlightRequestReturnsNullOnNonMatch
parameter_list|()
throws|throws
name|Exception
block|{
name|HttpResponse
name|response
init|=
name|httpClient
argument_list|()
operator|.
name|method
argument_list|(
literal|"OPTIONS"
argument_list|)
operator|.
name|path
argument_list|(
literal|"/"
argument_list|)
operator|.
name|addHeader
argument_list|(
literal|"User-Agent"
argument_list|,
literal|"Mozilla Bar"
argument_list|)
operator|.
name|addHeader
argument_list|(
literal|"Origin"
argument_list|,
literal|"http://evil-host:9200"
argument_list|)
operator|.
name|execute
argument_list|()
decl_stmt|;
name|assertResponseWithOriginheader
argument_list|(
name|response
argument_list|,
literal|"null"
argument_list|)
expr_stmt|;
block|}
DECL|method|assertResponseWithOriginheader
specifier|public
specifier|static
name|void
name|assertResponseWithOriginheader
parameter_list|(
name|HttpResponse
name|response
parameter_list|,
name|String
name|expectedCorsHeader
parameter_list|)
block|{
name|assertThat
argument_list|(
name|response
operator|.
name|getStatusCode
argument_list|()
argument_list|,
name|is
argument_list|(
literal|200
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getHeaders
argument_list|()
argument_list|,
name|hasKey
argument_list|(
literal|"Access-Control-Allow-Origin"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getHeaders
argument_list|()
operator|.
name|get
argument_list|(
literal|"Access-Control-Allow-Origin"
argument_list|)
argument_list|,
name|is
argument_list|(
name|expectedCorsHeader
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit
