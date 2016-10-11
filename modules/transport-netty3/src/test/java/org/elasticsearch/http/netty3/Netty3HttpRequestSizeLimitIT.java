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
name|ESNetty3IntegTestCase
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
name|collect
operator|.
name|Tuple
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
name|unit
operator|.
name|ByteSizeUnit
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
name|unit
operator|.
name|ByteSizeValue
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
name|indices
operator|.
name|breaker
operator|.
name|HierarchyCircuitBreakerService
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
name|HttpResponseStatus
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

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|greaterThan
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
comment|/**  * This test checks that in-flight requests are limited on HTTP level and that requests that are excluded from limiting can pass.  *  * As the same setting is also used to limit in-flight requests on transport level, we avoid transport messages by forcing  * a single node "cluster". We also force test infrastructure to use the node client instead of the transport client for the same reason.  */
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
name|supportsDedicatedMasters
operator|=
literal|false
argument_list|,
name|numClientNodes
operator|=
literal|0
argument_list|,
name|numDataNodes
operator|=
literal|1
argument_list|,
name|transportClientRatio
operator|=
literal|0
argument_list|)
DECL|class|Netty3HttpRequestSizeLimitIT
specifier|public
class|class
name|Netty3HttpRequestSizeLimitIT
extends|extends
name|ESNetty3IntegTestCase
block|{
DECL|field|LIMIT
specifier|private
specifier|static
specifier|final
name|ByteSizeValue
name|LIMIT
init|=
operator|new
name|ByteSizeValue
argument_list|(
literal|2
argument_list|,
name|ByteSizeUnit
operator|.
name|KB
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
name|NetworkModule
operator|.
name|HTTP_ENABLED
operator|.
name|getKey
argument_list|()
argument_list|,
literal|true
argument_list|)
operator|.
name|put
argument_list|(
name|HierarchyCircuitBreakerService
operator|.
name|IN_FLIGHT_REQUESTS_CIRCUIT_BREAKER_LIMIT_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
name|LIMIT
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
DECL|method|testLimitsInFlightRequests
specifier|public
name|void
name|testLimitsInFlightRequests
parameter_list|()
throws|throws
name|Exception
block|{
name|ensureGreen
argument_list|()
expr_stmt|;
comment|// we use the limit size as a (very) rough indication on how many requests we should sent to hit the limit
name|int
name|numRequests
init|=
name|LIMIT
operator|.
name|bytesAsInt
argument_list|()
operator|/
literal|100
decl_stmt|;
name|StringBuilder
name|bulkRequest
init|=
operator|new
name|StringBuilder
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
name|numRequests
condition|;
name|i
operator|++
control|)
block|{
name|bulkRequest
operator|.
name|append
argument_list|(
literal|"{\"index\": {}}"
argument_list|)
expr_stmt|;
name|bulkRequest
operator|.
name|append
argument_list|(
name|System
operator|.
name|lineSeparator
argument_list|()
argument_list|)
expr_stmt|;
name|bulkRequest
operator|.
name|append
argument_list|(
literal|"{ \"field\" : \"value\" }"
argument_list|)
expr_stmt|;
name|bulkRequest
operator|.
name|append
argument_list|(
name|System
operator|.
name|lineSeparator
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
name|Tuple
argument_list|<
name|String
argument_list|,
name|CharSequence
argument_list|>
index|[]
name|requests
init|=
operator|new
name|Tuple
index|[
literal|150
index|]
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
name|requests
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|requests
index|[
name|i
index|]
operator|=
name|Tuple
operator|.
name|tuple
argument_list|(
literal|"/index/type/_bulk"
argument_list|,
name|bulkRequest
argument_list|)
expr_stmt|;
block|}
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
name|TransportAddress
name|transportAddress
init|=
operator|(
name|TransportAddress
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
name|singleResponse
init|=
name|nettyHttpClient
operator|.
name|post
argument_list|(
name|transportAddress
operator|.
name|address
argument_list|()
argument_list|,
name|requests
index|[
literal|0
index|]
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|singleResponse
argument_list|,
name|hasSize
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertAtLeastOnceExpectedStatus
argument_list|(
name|singleResponse
argument_list|,
name|HttpResponseStatus
operator|.
name|OK
argument_list|)
expr_stmt|;
name|Collection
argument_list|<
name|HttpResponse
argument_list|>
name|multipleResponses
init|=
name|nettyHttpClient
operator|.
name|post
argument_list|(
name|transportAddress
operator|.
name|address
argument_list|()
argument_list|,
name|requests
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|multipleResponses
argument_list|,
name|hasSize
argument_list|(
name|requests
operator|.
name|length
argument_list|)
argument_list|)
expr_stmt|;
name|assertAtLeastOnceExpectedStatus
argument_list|(
name|multipleResponses
argument_list|,
name|HttpResponseStatus
operator|.
name|SERVICE_UNAVAILABLE
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testDoesNotLimitExcludedRequests
specifier|public
name|void
name|testDoesNotLimitExcludedRequests
parameter_list|()
throws|throws
name|Exception
block|{
name|ensureGreen
argument_list|()
expr_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
name|Tuple
argument_list|<
name|String
argument_list|,
name|CharSequence
argument_list|>
index|[]
name|requestUris
init|=
operator|new
name|Tuple
index|[
literal|1500
index|]
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
name|requestUris
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|requestUris
index|[
name|i
index|]
operator|=
name|Tuple
operator|.
name|tuple
argument_list|(
literal|"/_cluster/settings"
argument_list|,
literal|"{ \"transient\": {\"indices.ttl.interval\": \"40s\" } }"
argument_list|)
expr_stmt|;
block|}
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
name|TransportAddress
name|transportAddress
init|=
operator|(
name|TransportAddress
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
name|put
argument_list|(
name|transportAddress
operator|.
name|address
argument_list|()
argument_list|,
name|requestUris
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|responses
argument_list|,
name|hasSize
argument_list|(
name|requestUris
operator|.
name|length
argument_list|)
argument_list|)
expr_stmt|;
name|assertAllInExpectedStatus
argument_list|(
name|responses
argument_list|,
name|HttpResponseStatus
operator|.
name|OK
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|assertAtLeastOnceExpectedStatus
specifier|private
name|void
name|assertAtLeastOnceExpectedStatus
parameter_list|(
name|Collection
argument_list|<
name|HttpResponse
argument_list|>
name|responses
parameter_list|,
name|HttpResponseStatus
name|expectedStatus
parameter_list|)
block|{
name|long
name|countExpectedStatus
init|=
name|responses
operator|.
name|stream
argument_list|()
operator|.
name|filter
argument_list|(
name|r
lambda|->
name|r
operator|.
name|getStatus
argument_list|()
operator|.
name|equals
argument_list|(
name|expectedStatus
argument_list|)
argument_list|)
operator|.
name|count
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
literal|"Expected at least one request with status ["
operator|+
name|expectedStatus
operator|+
literal|"]"
argument_list|,
name|countExpectedStatus
argument_list|,
name|greaterThan
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|assertAllInExpectedStatus
specifier|private
name|void
name|assertAllInExpectedStatus
parameter_list|(
name|Collection
argument_list|<
name|HttpResponse
argument_list|>
name|responses
parameter_list|,
name|HttpResponseStatus
name|expectedStatus
parameter_list|)
block|{
name|long
name|countUnexpectedStatus
init|=
name|responses
operator|.
name|stream
argument_list|()
operator|.
name|filter
argument_list|(
name|r
lambda|->
name|r
operator|.
name|getStatus
argument_list|()
operator|.
name|equals
argument_list|(
name|expectedStatus
argument_list|)
operator|==
literal|false
argument_list|)
operator|.
name|count
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
literal|"Expected all requests with status ["
operator|+
name|expectedStatus
operator|+
literal|"] but ["
operator|+
name|countUnexpectedStatus
operator|+
literal|"] requests had a different one"
argument_list|,
name|countUnexpectedStatus
argument_list|,
name|equalTo
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

