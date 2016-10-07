begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.http.netty3
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|http
operator|.
name|netty3
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
name|network
operator|.
name|NetworkUtils
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
name|http
operator|.
name|BindHttpException
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
name|HttpTransportSettings
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
name|java
operator|.
name|net
operator|.
name|UnknownHostException
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
name|util
operator|.
name|List
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|net
operator|.
name|InetAddress
operator|.
name|getByName
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|util
operator|.
name|Arrays
operator|.
name|asList
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
name|netty3
operator|.
name|Netty3HttpServerTransport
operator|.
name|resolvePublishPort
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

begin_class
DECL|class|Netty3HttpPublishPortTests
specifier|public
class|class
name|Netty3HttpPublishPortTests
extends|extends
name|ESTestCase
block|{
DECL|method|testHttpPublishPort
specifier|public
name|void
name|testHttpPublishPort
parameter_list|()
throws|throws
name|Exception
block|{
name|int
name|boundPort
init|=
name|randomIntBetween
argument_list|(
literal|9000
argument_list|,
literal|9100
argument_list|)
decl_stmt|;
name|int
name|otherBoundPort
init|=
name|randomIntBetween
argument_list|(
literal|9200
argument_list|,
literal|9300
argument_list|)
decl_stmt|;
name|int
name|publishPort
init|=
name|resolvePublishPort
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|HttpTransportSettings
operator|.
name|SETTING_HTTP_PUBLISH_PORT
operator|.
name|getKey
argument_list|()
argument_list|,
literal|9080
argument_list|)
operator|.
name|build
argument_list|()
argument_list|,
name|randomAddresses
argument_list|()
argument_list|,
name|getByName
argument_list|(
literal|"127.0.0.2"
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
literal|"Publish port should be explicitly set to 9080"
argument_list|,
name|publishPort
argument_list|,
name|equalTo
argument_list|(
literal|9080
argument_list|)
argument_list|)
expr_stmt|;
name|publishPort
operator|=
name|resolvePublishPort
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
name|asList
argument_list|(
name|address
argument_list|(
literal|"127.0.0.1"
argument_list|,
name|boundPort
argument_list|)
argument_list|,
name|address
argument_list|(
literal|"127.0.0.2"
argument_list|,
name|otherBoundPort
argument_list|)
argument_list|)
argument_list|,
name|getByName
argument_list|(
literal|"127.0.0.1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"Publish port should be derived from matched address"
argument_list|,
name|publishPort
argument_list|,
name|equalTo
argument_list|(
name|boundPort
argument_list|)
argument_list|)
expr_stmt|;
name|publishPort
operator|=
name|resolvePublishPort
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
name|asList
argument_list|(
name|address
argument_list|(
literal|"127.0.0.1"
argument_list|,
name|boundPort
argument_list|)
argument_list|,
name|address
argument_list|(
literal|"127.0.0.2"
argument_list|,
name|boundPort
argument_list|)
argument_list|)
argument_list|,
name|getByName
argument_list|(
literal|"127.0.0.3"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"Publish port should be derived from unique port of bound addresses"
argument_list|,
name|publishPort
argument_list|,
name|equalTo
argument_list|(
name|boundPort
argument_list|)
argument_list|)
expr_stmt|;
try|try
block|{
name|resolvePublishPort
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
name|asList
argument_list|(
name|address
argument_list|(
literal|"127.0.0.1"
argument_list|,
name|boundPort
argument_list|)
argument_list|,
name|address
argument_list|(
literal|"127.0.0.2"
argument_list|,
name|otherBoundPort
argument_list|)
argument_list|)
argument_list|,
name|getByName
argument_list|(
literal|"127.0.0.3"
argument_list|)
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Expected BindHttpException as publish_port not specified and non-unique port of bound addresses"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|BindHttpException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"Failed to auto-resolve http publish port"
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|publishPort
operator|=
name|resolvePublishPort
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
name|asList
argument_list|(
name|address
argument_list|(
literal|"0.0.0.0"
argument_list|,
name|boundPort
argument_list|)
argument_list|,
name|address
argument_list|(
literal|"127.0.0.2"
argument_list|,
name|otherBoundPort
argument_list|)
argument_list|)
argument_list|,
name|getByName
argument_list|(
literal|"127.0.0.1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"Publish port should be derived from matching wildcard address"
argument_list|,
name|publishPort
argument_list|,
name|equalTo
argument_list|(
name|boundPort
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|NetworkUtils
operator|.
name|SUPPORTS_V6
condition|)
block|{
name|publishPort
operator|=
name|resolvePublishPort
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
name|asList
argument_list|(
name|address
argument_list|(
literal|"0.0.0.0"
argument_list|,
name|boundPort
argument_list|)
argument_list|,
name|address
argument_list|(
literal|"127.0.0.2"
argument_list|,
name|otherBoundPort
argument_list|)
argument_list|)
argument_list|,
name|getByName
argument_list|(
literal|"::1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"Publish port should be derived from matching wildcard address"
argument_list|,
name|publishPort
argument_list|,
name|equalTo
argument_list|(
name|boundPort
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|address
specifier|private
name|TransportAddress
name|address
parameter_list|(
name|String
name|host
parameter_list|,
name|int
name|port
parameter_list|)
throws|throws
name|UnknownHostException
block|{
return|return
operator|new
name|TransportAddress
argument_list|(
name|getByName
argument_list|(
name|host
argument_list|)
argument_list|,
name|port
argument_list|)
return|;
block|}
DECL|method|randomAddress
specifier|private
name|TransportAddress
name|randomAddress
parameter_list|()
throws|throws
name|UnknownHostException
block|{
return|return
name|address
argument_list|(
literal|"127.0.0."
operator|+
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|100
argument_list|)
argument_list|,
name|randomIntBetween
argument_list|(
literal|9200
argument_list|,
literal|9300
argument_list|)
argument_list|)
return|;
block|}
DECL|method|randomAddresses
specifier|private
name|List
argument_list|<
name|TransportAddress
argument_list|>
name|randomAddresses
parameter_list|()
throws|throws
name|UnknownHostException
block|{
name|List
argument_list|<
name|TransportAddress
argument_list|>
name|addresses
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|5
argument_list|)
condition|;
name|i
operator|++
control|)
block|{
name|addresses
operator|.
name|add
argument_list|(
name|randomAddress
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|addresses
return|;
block|}
block|}
end_class

end_unit

