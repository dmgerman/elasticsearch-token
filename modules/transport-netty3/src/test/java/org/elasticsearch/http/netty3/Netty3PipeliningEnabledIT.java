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
name|transport
operator|.
name|Netty3Plugin
import|;
end_import

begin_import
import|import
name|org
operator|.
name|jboss
operator|.
name|netty
operator|.
name|handler
operator|.
name|codec
operator|.
name|http
operator|.
name|HttpResponse
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|InetSocketAddress
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
name|Locale
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
name|Netty3HttpClient
operator|.
name|returnOpaqueIds
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
name|hasSize
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

begin_class
DECL|class|Netty3PipeliningEnabledIT
specifier|public
class|class
name|Netty3PipeliningEnabledIT
extends|extends
name|ESIntegTestCase
block|{
annotation|@
name|Override
DECL|method|transportClientPlugins
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
name|transportClientPlugins
parameter_list|()
block|{
return|return
name|Arrays
operator|.
name|asList
argument_list|(
name|Netty3Plugin
operator|.
name|class
argument_list|)
return|;
block|}
DECL|method|testThatNettyHttpServerSupportsPipelining
specifier|public
name|void
name|testThatNettyHttpServerSupportsPipelining
parameter_list|()
throws|throws
name|Exception
block|{
name|String
index|[]
name|requests
init|=
operator|new
name|String
index|[]
block|{
literal|"/"
block|,
literal|"/_nodes/stats"
block|,
literal|"/"
block|,
literal|"/_cluster/state"
block|,
literal|"/"
block|}
decl_stmt|;
name|InetSocketAddress
name|inetSocketAddress
init|=
name|randomFrom
argument_list|(
name|cluster
argument_list|()
operator|.
name|httpAddresses
argument_list|()
argument_list|)
decl_stmt|;
try|try
init|(
name|Netty3HttpClient
name|nettyHttpClient
init|=
operator|new
name|Netty3HttpClient
argument_list|()
init|)
block|{
name|Collection
argument_list|<
name|HttpResponse
argument_list|>
name|responses
init|=
name|nettyHttpClient
operator|.
name|get
argument_list|(
name|inetSocketAddress
argument_list|,
name|requests
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|responses
argument_list|,
name|hasSize
argument_list|(
literal|5
argument_list|)
argument_list|)
expr_stmt|;
name|Collection
argument_list|<
name|String
argument_list|>
name|opaqueIds
init|=
name|returnOpaqueIds
argument_list|(
name|responses
argument_list|)
decl_stmt|;
name|assertOpaqueIdsInOrder
argument_list|(
name|opaqueIds
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|assertOpaqueIdsInOrder
specifier|private
name|void
name|assertOpaqueIdsInOrder
parameter_list|(
name|Collection
argument_list|<
name|String
argument_list|>
name|opaqueIds
parameter_list|)
block|{
comment|// check if opaque ids are monotonically increasing
name|int
name|i
init|=
literal|0
decl_stmt|;
name|String
name|msg
init|=
name|String
operator|.
name|format
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|,
literal|"Expected list of opaque ids to be monotonically increasing, got [%s]"
argument_list|,
name|opaqueIds
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|opaqueId
range|:
name|opaqueIds
control|)
block|{
name|assertThat
argument_list|(
name|msg
argument_list|,
name|opaqueId
argument_list|,
name|is
argument_list|(
name|String
operator|.
name|valueOf
argument_list|(
name|i
operator|++
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

