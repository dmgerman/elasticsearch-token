begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.http.netty
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|http
operator|.
name|netty
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
name|common
operator|.
name|transport
operator|.
name|InetSocketTransportAddress
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
name|common
operator|.
name|settings
operator|.
name|Settings
operator|.
name|settingsBuilder
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
name|NettyHttpClient
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
name|containsInAnyOrder
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
name|TEST
argument_list|,
name|numDataNodes
operator|=
literal|1
argument_list|)
DECL|class|NettyPipeliningDisabledIT
specifier|public
class|class
name|NettyPipeliningDisabledIT
extends|extends
name|ESIntegTestCase
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
name|Node
operator|.
name|HTTP_ENABLED
argument_list|,
literal|true
argument_list|)
operator|.
name|put
argument_list|(
literal|"http.pipelining"
argument_list|,
literal|false
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
DECL|method|testThatNettyHttpServerDoesNotSupportPipelining
specifier|public
name|void
name|testThatNettyHttpServerDoesNotSupportPipelining
parameter_list|()
throws|throws
name|Exception
block|{
name|ensureGreen
argument_list|()
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|requests
init|=
name|Arrays
operator|.
name|asList
argument_list|(
literal|"/"
argument_list|,
literal|"/_nodes/stats"
argument_list|,
literal|"/"
argument_list|,
literal|"/_cluster/state"
argument_list|,
literal|"/"
argument_list|,
literal|"/_nodes"
argument_list|,
literal|"/"
argument_list|)
decl_stmt|;
name|HttpServerTransport
name|httpServerTransport
init|=
name|internalCluster
argument_list|()
operator|.
name|getInstance
argument_list|(
name|HttpServerTransport
operator|.
name|class
argument_list|)
decl_stmt|;
name|InetSocketTransportAddress
name|inetSocketTransportAddress
init|=
operator|(
name|InetSocketTransportAddress
operator|)
name|randomFrom
argument_list|(
name|httpServerTransport
operator|.
name|boundAddress
argument_list|()
operator|.
name|boundAddresses
argument_list|()
argument_list|)
decl_stmt|;
try|try
init|(
name|NettyHttpClient
name|nettyHttpClient
init|=
operator|new
name|NettyHttpClient
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
name|sendRequests
argument_list|(
name|inetSocketTransportAddress
operator|.
name|address
argument_list|()
argument_list|,
name|requests
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[]
block|{}
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|responses
argument_list|,
name|hasSize
argument_list|(
name|requests
operator|.
name|size
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|opaqueIds
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|returnOpaqueIds
argument_list|(
name|responses
argument_list|)
argument_list|)
decl_stmt|;
name|assertResponsesOutOfOrder
argument_list|(
name|opaqueIds
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      * checks if all responses are there, but also tests that they are out of order because pipelining is disabled      */
DECL|method|assertResponsesOutOfOrder
specifier|private
name|void
name|assertResponsesOutOfOrder
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|opaqueIds
parameter_list|)
block|{
name|String
name|message
init|=
name|String
operator|.
name|format
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|,
literal|"Expected returned http message ids to be in any order of: %s"
argument_list|,
name|opaqueIds
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|message
argument_list|,
name|opaqueIds
argument_list|,
name|containsInAnyOrder
argument_list|(
literal|"0"
argument_list|,
literal|"1"
argument_list|,
literal|"2"
argument_list|,
literal|"3"
argument_list|,
literal|"4"
argument_list|,
literal|"5"
argument_list|,
literal|"6"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

