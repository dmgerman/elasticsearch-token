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
name|action
operator|.
name|index
operator|.
name|IndexRequest
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
name|SearchRequest
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
name|ParseFieldMatcher
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
name|BytesReference
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
name|common
operator|.
name|xcontent
operator|.
name|XContentParser
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
name|json
operator|.
name|JsonXContent
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
name|RestReindexAction
operator|.
name|ReindexParseContext
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
name|rest
operator|.
name|RestController
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|SearchRequestParsers
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
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|rest
operator|.
name|FakeRestRequest
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
name|HashMap
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
name|common
operator|.
name|unit
operator|.
name|TimeValue
operator|.
name|timeValueSeconds
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|mock
import|;
end_import

begin_class
DECL|class|RestReindexActionTests
specifier|public
class|class
name|RestReindexActionTests
extends|extends
name|ESTestCase
block|{
DECL|method|testBuildRemoteInfoNoRemote
specifier|public
name|void
name|testBuildRemoteInfoNoRemote
parameter_list|()
throws|throws
name|IOException
block|{
name|assertNull
argument_list|(
name|RestReindexAction
operator|.
name|buildRemoteInfo
argument_list|(
operator|new
name|HashMap
argument_list|<>
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testBuildRemoteInfoFullyLoaded
specifier|public
name|void
name|testBuildRemoteInfoFullyLoaded
parameter_list|()
throws|throws
name|IOException
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|headers
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|headers
operator|.
name|put
argument_list|(
literal|"first"
argument_list|,
literal|"a"
argument_list|)
expr_stmt|;
name|headers
operator|.
name|put
argument_list|(
literal|"second"
argument_list|,
literal|"b"
argument_list|)
expr_stmt|;
name|headers
operator|.
name|put
argument_list|(
literal|"third"
argument_list|,
literal|""
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|remote
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|remote
operator|.
name|put
argument_list|(
literal|"host"
argument_list|,
literal|"https://example.com:9200"
argument_list|)
expr_stmt|;
name|remote
operator|.
name|put
argument_list|(
literal|"username"
argument_list|,
literal|"testuser"
argument_list|)
expr_stmt|;
name|remote
operator|.
name|put
argument_list|(
literal|"password"
argument_list|,
literal|"testpass"
argument_list|)
expr_stmt|;
name|remote
operator|.
name|put
argument_list|(
literal|"headers"
argument_list|,
name|headers
argument_list|)
expr_stmt|;
name|remote
operator|.
name|put
argument_list|(
literal|"socket_timeout"
argument_list|,
literal|"90s"
argument_list|)
expr_stmt|;
name|remote
operator|.
name|put
argument_list|(
literal|"connect_timeout"
argument_list|,
literal|"10s"
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|query
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|query
operator|.
name|put
argument_list|(
literal|"a"
argument_list|,
literal|"b"
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|source
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|source
operator|.
name|put
argument_list|(
literal|"remote"
argument_list|,
name|remote
argument_list|)
expr_stmt|;
name|source
operator|.
name|put
argument_list|(
literal|"query"
argument_list|,
name|query
argument_list|)
expr_stmt|;
name|RemoteInfo
name|remoteInfo
init|=
name|RestReindexAction
operator|.
name|buildRemoteInfo
argument_list|(
name|source
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"https"
argument_list|,
name|remoteInfo
operator|.
name|getScheme
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"example.com"
argument_list|,
name|remoteInfo
operator|.
name|getHost
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|9200
argument_list|,
name|remoteInfo
operator|.
name|getPort
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"{\n  \"a\" : \"b\"\n}"
argument_list|,
name|remoteInfo
operator|.
name|getQuery
argument_list|()
operator|.
name|utf8ToString
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"testuser"
argument_list|,
name|remoteInfo
operator|.
name|getUsername
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"testpass"
argument_list|,
name|remoteInfo
operator|.
name|getPassword
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|headers
argument_list|,
name|remoteInfo
operator|.
name|getHeaders
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|timeValueSeconds
argument_list|(
literal|90
argument_list|)
argument_list|,
name|remoteInfo
operator|.
name|getSocketTimeout
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|timeValueSeconds
argument_list|(
literal|10
argument_list|)
argument_list|,
name|remoteInfo
operator|.
name|getConnectTimeout
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testBuildRemoteInfoWithoutAllParts
specifier|public
name|void
name|testBuildRemoteInfoWithoutAllParts
parameter_list|()
throws|throws
name|IOException
block|{
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|buildRemoteInfoHostTestCase
argument_list|(
literal|"example.com"
argument_list|)
argument_list|)
expr_stmt|;
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|buildRemoteInfoHostTestCase
argument_list|(
literal|"example.com:9200"
argument_list|)
argument_list|)
expr_stmt|;
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|buildRemoteInfoHostTestCase
argument_list|(
literal|"http://example.com"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testBuildRemoteInfoWithAllHostParts
specifier|public
name|void
name|testBuildRemoteInfoWithAllHostParts
parameter_list|()
throws|throws
name|IOException
block|{
name|RemoteInfo
name|info
init|=
name|buildRemoteInfoHostTestCase
argument_list|(
literal|"http://example.com:9200"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"http"
argument_list|,
name|info
operator|.
name|getScheme
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"example.com"
argument_list|,
name|info
operator|.
name|getHost
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|9200
argument_list|,
name|info
operator|.
name|getPort
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|RemoteInfo
operator|.
name|DEFAULT_SOCKET_TIMEOUT
argument_list|,
name|info
operator|.
name|getSocketTimeout
argument_list|()
argument_list|)
expr_stmt|;
comment|// Didn't set the timeout so we should get the default
name|assertEquals
argument_list|(
name|RemoteInfo
operator|.
name|DEFAULT_CONNECT_TIMEOUT
argument_list|,
name|info
operator|.
name|getConnectTimeout
argument_list|()
argument_list|)
expr_stmt|;
comment|// Didn't set the timeout so we should get the default
name|info
operator|=
name|buildRemoteInfoHostTestCase
argument_list|(
literal|"https://other.example.com:9201"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"https"
argument_list|,
name|info
operator|.
name|getScheme
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"other.example.com"
argument_list|,
name|info
operator|.
name|getHost
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|9201
argument_list|,
name|info
operator|.
name|getPort
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|RemoteInfo
operator|.
name|DEFAULT_SOCKET_TIMEOUT
argument_list|,
name|info
operator|.
name|getSocketTimeout
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|RemoteInfo
operator|.
name|DEFAULT_CONNECT_TIMEOUT
argument_list|,
name|info
operator|.
name|getConnectTimeout
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testReindexFromRemoteRequestParsing
specifier|public
name|void
name|testReindexFromRemoteRequestParsing
parameter_list|()
throws|throws
name|IOException
block|{
name|BytesReference
name|request
decl_stmt|;
try|try
init|(
name|XContentBuilder
name|b
init|=
name|JsonXContent
operator|.
name|contentBuilder
argument_list|()
init|)
block|{
name|b
operator|.
name|startObject
argument_list|()
expr_stmt|;
block|{
name|b
operator|.
name|startObject
argument_list|(
literal|"source"
argument_list|)
expr_stmt|;
block|{
name|b
operator|.
name|startObject
argument_list|(
literal|"remote"
argument_list|)
expr_stmt|;
block|{
name|b
operator|.
name|field
argument_list|(
literal|"host"
argument_list|,
literal|"http://localhost:9200"
argument_list|)
expr_stmt|;
block|}
name|b
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|b
operator|.
name|field
argument_list|(
literal|"index"
argument_list|,
literal|"source"
argument_list|)
expr_stmt|;
block|}
name|b
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|b
operator|.
name|startObject
argument_list|(
literal|"dest"
argument_list|)
expr_stmt|;
block|{
name|b
operator|.
name|field
argument_list|(
literal|"index"
argument_list|,
literal|"dest"
argument_list|)
expr_stmt|;
block|}
name|b
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
name|b
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|request
operator|=
name|b
operator|.
name|bytes
argument_list|()
expr_stmt|;
block|}
try|try
init|(
name|XContentParser
name|p
init|=
name|createParser
argument_list|(
name|JsonXContent
operator|.
name|jsonXContent
argument_list|,
name|request
argument_list|)
init|)
block|{
name|ReindexRequest
name|r
init|=
operator|new
name|ReindexRequest
argument_list|(
operator|new
name|SearchRequest
argument_list|()
argument_list|,
operator|new
name|IndexRequest
argument_list|()
argument_list|)
decl_stmt|;
name|SearchRequestParsers
name|searchParsers
init|=
operator|new
name|SearchRequestParsers
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|RestReindexAction
operator|.
name|PARSER
operator|.
name|parse
argument_list|(
name|p
argument_list|,
name|r
argument_list|,
operator|new
name|ReindexParseContext
argument_list|(
name|searchParsers
argument_list|,
name|ParseFieldMatcher
operator|.
name|STRICT
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"localhost"
argument_list|,
name|r
operator|.
name|getRemoteInfo
argument_list|()
operator|.
name|getHost
argument_list|()
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"source"
block|}
argument_list|,
name|r
operator|.
name|getSearchRequest
argument_list|()
operator|.
name|indices
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testPipelineQueryParameterIsError
specifier|public
name|void
name|testPipelineQueryParameterIsError
parameter_list|()
throws|throws
name|IOException
block|{
name|SearchRequestParsers
name|parsers
init|=
operator|new
name|SearchRequestParsers
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|RestReindexAction
name|action
init|=
operator|new
name|RestReindexAction
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
name|mock
argument_list|(
name|RestController
operator|.
name|class
argument_list|)
argument_list|,
name|parsers
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|FakeRestRequest
operator|.
name|Builder
name|request
init|=
operator|new
name|FakeRestRequest
operator|.
name|Builder
argument_list|(
name|xContentRegistry
argument_list|()
argument_list|)
decl_stmt|;
try|try
init|(
name|XContentBuilder
name|body
init|=
name|JsonXContent
operator|.
name|contentBuilder
argument_list|()
operator|.
name|prettyPrint
argument_list|()
init|)
block|{
name|body
operator|.
name|startObject
argument_list|()
expr_stmt|;
block|{
name|body
operator|.
name|startObject
argument_list|(
literal|"source"
argument_list|)
expr_stmt|;
block|{
name|body
operator|.
name|field
argument_list|(
literal|"index"
argument_list|,
literal|"source"
argument_list|)
expr_stmt|;
block|}
name|body
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|body
operator|.
name|startObject
argument_list|(
literal|"dest"
argument_list|)
expr_stmt|;
block|{
name|body
operator|.
name|field
argument_list|(
literal|"index"
argument_list|,
literal|"dest"
argument_list|)
expr_stmt|;
block|}
name|body
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
name|body
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|request
operator|.
name|withContent
argument_list|(
name|body
operator|.
name|bytes
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|request
operator|.
name|withParams
argument_list|(
name|singletonMap
argument_list|(
literal|"pipeline"
argument_list|,
literal|"doesn't matter"
argument_list|)
argument_list|)
expr_stmt|;
name|Exception
name|e
init|=
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|action
operator|.
name|buildRequest
argument_list|(
name|request
operator|.
name|build
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"_reindex doesn't support [pipeline] as a query parmaeter. Specify it in the [dest] object instead."
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|buildRemoteInfoHostTestCase
specifier|private
name|RemoteInfo
name|buildRemoteInfoHostTestCase
parameter_list|(
name|String
name|hostInRest
parameter_list|)
throws|throws
name|IOException
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|remote
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|remote
operator|.
name|put
argument_list|(
literal|"host"
argument_list|,
name|hostInRest
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|source
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|source
operator|.
name|put
argument_list|(
literal|"remote"
argument_list|,
name|remote
argument_list|)
expr_stmt|;
return|return
name|RestReindexAction
operator|.
name|buildRemoteInfo
argument_list|(
name|source
argument_list|)
return|;
block|}
block|}
end_class

end_unit

