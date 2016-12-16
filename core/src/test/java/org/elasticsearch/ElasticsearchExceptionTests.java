begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch
package|package
name|org
operator|.
name|elasticsearch
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
name|RoutingMissingException
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
name|broadcast
operator|.
name|BroadcastShardOperationFailedException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|block
operator|.
name|ClusterBlockException
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
name|ToXContent
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
name|XContent
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
name|XContentType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|discovery
operator|.
name|DiscoverySettings
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
name|shard
operator|.
name|IndexShardRecoveringException
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
name|shard
operator|.
name|ShardId
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
name|hamcrest
operator|.
name|Matcher
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
name|Collections
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
name|singleton
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|CoreMatchers
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
name|CoreMatchers
operator|.
name|hasItem
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|CoreMatchers
operator|.
name|startsWith
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

begin_class
DECL|class|ElasticsearchExceptionTests
specifier|public
class|class
name|ElasticsearchExceptionTests
extends|extends
name|ESTestCase
block|{
DECL|method|testToXContent
specifier|public
name|void
name|testToXContent
parameter_list|()
throws|throws
name|IOException
block|{
name|ElasticsearchException
name|e
init|=
operator|new
name|ElasticsearchException
argument_list|(
literal|"test"
argument_list|)
decl_stmt|;
name|assertExceptionAsJson
argument_list|(
name|e
argument_list|,
literal|false
argument_list|,
name|equalTo
argument_list|(
literal|"{\"type\":\"exception\",\"reason\":\"test\"}"
argument_list|)
argument_list|)
expr_stmt|;
name|e
operator|=
operator|new
name|IndexShardRecoveringException
argument_list|(
operator|new
name|ShardId
argument_list|(
literal|"_test"
argument_list|,
literal|"_0"
argument_list|,
literal|5
argument_list|)
argument_list|)
expr_stmt|;
name|assertExceptionAsJson
argument_list|(
name|e
argument_list|,
literal|false
argument_list|,
name|equalTo
argument_list|(
literal|"{\"type\":\"index_shard_recovering_exception\","
operator|+
literal|"\"reason\":\"CurrentState[RECOVERING] Already recovering\",\"index_uuid\":\"_0\",\"shard\":\"5\",\"index\":\"_test\"}"
argument_list|)
argument_list|)
expr_stmt|;
name|e
operator|=
operator|new
name|BroadcastShardOperationFailedException
argument_list|(
operator|new
name|ShardId
argument_list|(
literal|"_index"
argument_list|,
literal|"_uuid"
argument_list|,
literal|12
argument_list|)
argument_list|,
literal|"foo"
argument_list|,
operator|new
name|IllegalStateException
argument_list|(
literal|"bar"
argument_list|)
argument_list|)
expr_stmt|;
name|assertExceptionAsJson
argument_list|(
name|e
argument_list|,
literal|false
argument_list|,
name|equalTo
argument_list|(
literal|"{\"type\":\"illegal_state_exception\",\"reason\":\"bar\"}"
argument_list|)
argument_list|)
expr_stmt|;
name|e
operator|=
operator|new
name|ElasticsearchException
argument_list|(
operator|new
name|IllegalArgumentException
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
name|assertExceptionAsJson
argument_list|(
name|e
argument_list|,
literal|false
argument_list|,
name|equalTo
argument_list|(
literal|"{\"type\":\"exception\",\"reason\":\"java.lang.IllegalArgumentException: foo\","
operator|+
literal|"\"caused_by\":{\"type\":\"illegal_argument_exception\",\"reason\":\"foo\"}}"
argument_list|)
argument_list|)
expr_stmt|;
name|e
operator|=
operator|new
name|ElasticsearchException
argument_list|(
literal|"foo"
argument_list|,
operator|new
name|IllegalStateException
argument_list|(
literal|"bar"
argument_list|)
argument_list|)
expr_stmt|;
name|assertExceptionAsJson
argument_list|(
name|e
argument_list|,
literal|false
argument_list|,
name|equalTo
argument_list|(
literal|"{\"type\":\"exception\",\"reason\":\"foo\","
operator|+
literal|"\"caused_by\":{\"type\":\"illegal_state_exception\",\"reason\":\"bar\"}}"
argument_list|)
argument_list|)
expr_stmt|;
comment|// Test the same exception but with the "rest.exception.stacktrace.skip" parameter disabled: the stack_trace must be present
comment|// in the JSON. Since the stack can be large, it only checks the beginning of the JSON.
name|assertExceptionAsJson
argument_list|(
name|e
argument_list|,
literal|true
argument_list|,
name|startsWith
argument_list|(
literal|"{\"type\":\"exception\",\"reason\":\"foo\","
operator|+
literal|"\"caused_by\":{\"type\":\"illegal_state_exception\",\"reason\":\"bar\","
operator|+
literal|"\"stack_trace\":\"java.lang.IllegalStateException: bar"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testToXContentWithHeaders
specifier|public
name|void
name|testToXContentWithHeaders
parameter_list|()
throws|throws
name|IOException
block|{
name|ElasticsearchException
name|e
init|=
operator|new
name|ElasticsearchException
argument_list|(
literal|"foo"
argument_list|,
operator|new
name|ElasticsearchException
argument_list|(
literal|"bar"
argument_list|,
operator|new
name|ElasticsearchException
argument_list|(
literal|"baz"
argument_list|,
operator|new
name|ClusterBlockException
argument_list|(
name|singleton
argument_list|(
name|DiscoverySettings
operator|.
name|NO_MASTER_BLOCK_WRITES
argument_list|)
argument_list|)
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|e
operator|.
name|addHeader
argument_list|(
literal|"foo_0"
argument_list|,
literal|"0"
argument_list|)
expr_stmt|;
name|e
operator|.
name|addHeader
argument_list|(
literal|"foo_1"
argument_list|,
literal|"1"
argument_list|)
expr_stmt|;
name|e
operator|.
name|addHeader
argument_list|(
literal|"es.header_foo_0"
argument_list|,
literal|"foo_0"
argument_list|)
expr_stmt|;
name|e
operator|.
name|addHeader
argument_list|(
literal|"es.header_foo_1"
argument_list|,
literal|"foo_1"
argument_list|)
expr_stmt|;
specifier|final
name|String
name|expectedJson
init|=
literal|"{"
operator|+
literal|"\"type\":\"exception\","
operator|+
literal|"\"reason\":\"foo\","
operator|+
literal|"\"header_foo_0\":\"foo_0\","
operator|+
literal|"\"header_foo_1\":\"foo_1\","
operator|+
literal|"\"caused_by\":{"
operator|+
literal|"\"type\":\"exception\","
operator|+
literal|"\"reason\":\"bar\","
operator|+
literal|"\"caused_by\":{"
operator|+
literal|"\"type\":\"exception\","
operator|+
literal|"\"reason\":\"baz\","
operator|+
literal|"\"caused_by\":{"
operator|+
literal|"\"type\":\"cluster_block_exception\","
operator|+
literal|"\"reason\":\"blocked by: [SERVICE_UNAVAILABLE/2/no master];\""
operator|+
literal|"}"
operator|+
literal|"}"
operator|+
literal|"},"
operator|+
literal|"\"header\":{"
operator|+
literal|"\"foo_0\":\"0\","
operator|+
literal|"\"foo_1\":\"1\""
operator|+
literal|"}"
operator|+
literal|"}"
decl_stmt|;
name|assertExceptionAsJson
argument_list|(
name|e
argument_list|,
literal|false
argument_list|,
name|equalTo
argument_list|(
name|expectedJson
argument_list|)
argument_list|)
expr_stmt|;
name|ElasticsearchException
name|parsed
decl_stmt|;
try|try
init|(
name|XContentParser
name|parser
init|=
name|XContentType
operator|.
name|JSON
operator|.
name|xContent
argument_list|()
operator|.
name|createParser
argument_list|(
name|expectedJson
argument_list|)
init|)
block|{
name|assertEquals
argument_list|(
name|XContentParser
operator|.
name|Token
operator|.
name|START_OBJECT
argument_list|,
name|parser
operator|.
name|nextToken
argument_list|()
argument_list|)
expr_stmt|;
name|parsed
operator|=
name|ElasticsearchException
operator|.
name|fromXContent
argument_list|(
name|parser
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|XContentParser
operator|.
name|Token
operator|.
name|END_OBJECT
argument_list|,
name|parser
operator|.
name|currentToken
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|parser
operator|.
name|nextToken
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|assertNotNull
argument_list|(
name|parsed
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|parsed
operator|.
name|getMessage
argument_list|()
argument_list|,
literal|"Elasticsearch exception [type=exception, reason=foo]"
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|parsed
operator|.
name|getHeaderKeys
argument_list|()
argument_list|,
name|hasSize
argument_list|(
literal|4
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|parsed
operator|.
name|getHeader
argument_list|(
literal|"header_foo_0"
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
literal|"foo_0"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|parsed
operator|.
name|getHeader
argument_list|(
literal|"header_foo_1"
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
literal|"foo_1"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|parsed
operator|.
name|getHeader
argument_list|(
literal|"foo_0"
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
literal|"0"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|parsed
operator|.
name|getHeader
argument_list|(
literal|"foo_1"
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
literal|"1"
argument_list|)
expr_stmt|;
name|ElasticsearchException
name|cause
init|=
operator|(
name|ElasticsearchException
operator|)
name|parsed
operator|.
name|getCause
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|cause
operator|.
name|getMessage
argument_list|()
argument_list|,
literal|"Elasticsearch exception [type=exception, reason=bar]"
argument_list|)
expr_stmt|;
name|cause
operator|=
operator|(
name|ElasticsearchException
operator|)
name|cause
operator|.
name|getCause
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
name|cause
operator|.
name|getMessage
argument_list|()
argument_list|,
literal|"Elasticsearch exception [type=exception, reason=baz]"
argument_list|)
expr_stmt|;
name|cause
operator|=
operator|(
name|ElasticsearchException
operator|)
name|cause
operator|.
name|getCause
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
name|cause
operator|.
name|getMessage
argument_list|()
argument_list|,
literal|"Elasticsearch exception [type=cluster_block_exception, reason=blocked by: [SERVICE_UNAVAILABLE/2/no master];]"
argument_list|)
expr_stmt|;
block|}
DECL|method|testFromXContent
specifier|public
name|void
name|testFromXContent
parameter_list|()
throws|throws
name|IOException
block|{
specifier|final
name|XContent
name|xContent
init|=
name|randomFrom
argument_list|(
name|XContentType
operator|.
name|values
argument_list|()
argument_list|)
operator|.
name|xContent
argument_list|()
decl_stmt|;
name|XContentBuilder
name|builder
init|=
name|XContentBuilder
operator|.
name|builder
argument_list|(
name|xContent
argument_list|)
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"foo"
argument_list|)
operator|.
name|field
argument_list|(
literal|"reason"
argument_list|,
literal|"something went wrong"
argument_list|)
operator|.
name|field
argument_list|(
literal|"stack_trace"
argument_list|,
literal|"..."
argument_list|)
operator|.
name|endObject
argument_list|()
decl_stmt|;
name|ElasticsearchException
name|parsed
decl_stmt|;
try|try
init|(
name|XContentParser
name|parser
init|=
name|xContent
operator|.
name|createParser
argument_list|(
name|builder
operator|.
name|bytes
argument_list|()
argument_list|)
init|)
block|{
name|assertEquals
argument_list|(
name|XContentParser
operator|.
name|Token
operator|.
name|START_OBJECT
argument_list|,
name|parser
operator|.
name|nextToken
argument_list|()
argument_list|)
expr_stmt|;
name|parsed
operator|=
name|ElasticsearchException
operator|.
name|fromXContent
argument_list|(
name|parser
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|XContentParser
operator|.
name|Token
operator|.
name|END_OBJECT
argument_list|,
name|parser
operator|.
name|currentToken
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|parser
operator|.
name|nextToken
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|assertNotNull
argument_list|(
name|parsed
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|parsed
operator|.
name|getMessage
argument_list|()
argument_list|,
literal|"Elasticsearch exception [type=foo, reason=something went wrong, stack_trace=...]"
argument_list|)
expr_stmt|;
block|}
DECL|method|testFromXContentWithCause
specifier|public
name|void
name|testFromXContentWithCause
parameter_list|()
throws|throws
name|IOException
block|{
name|ElasticsearchException
name|e
init|=
operator|new
name|ElasticsearchException
argument_list|(
literal|"foo"
argument_list|,
operator|new
name|ElasticsearchException
argument_list|(
literal|"bar"
argument_list|,
operator|new
name|ElasticsearchException
argument_list|(
literal|"baz"
argument_list|,
operator|new
name|RoutingMissingException
argument_list|(
literal|"_test"
argument_list|,
literal|"_type"
argument_list|,
literal|"_id"
argument_list|)
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
specifier|final
name|XContent
name|xContent
init|=
name|randomFrom
argument_list|(
name|XContentType
operator|.
name|values
argument_list|()
argument_list|)
operator|.
name|xContent
argument_list|()
decl_stmt|;
name|XContentBuilder
name|builder
init|=
name|XContentBuilder
operator|.
name|builder
argument_list|(
name|xContent
argument_list|)
operator|.
name|startObject
argument_list|()
operator|.
name|value
argument_list|(
name|e
argument_list|)
operator|.
name|endObject
argument_list|()
decl_stmt|;
name|ElasticsearchException
name|parsed
decl_stmt|;
try|try
init|(
name|XContentParser
name|parser
init|=
name|xContent
operator|.
name|createParser
argument_list|(
name|builder
operator|.
name|bytes
argument_list|()
argument_list|)
init|)
block|{
name|assertEquals
argument_list|(
name|XContentParser
operator|.
name|Token
operator|.
name|START_OBJECT
argument_list|,
name|parser
operator|.
name|nextToken
argument_list|()
argument_list|)
expr_stmt|;
name|parsed
operator|=
name|ElasticsearchException
operator|.
name|fromXContent
argument_list|(
name|parser
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|XContentParser
operator|.
name|Token
operator|.
name|END_OBJECT
argument_list|,
name|parser
operator|.
name|currentToken
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|parser
operator|.
name|nextToken
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|assertNotNull
argument_list|(
name|parsed
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|parsed
operator|.
name|getMessage
argument_list|()
argument_list|,
literal|"Elasticsearch exception [type=exception, reason=foo]"
argument_list|)
expr_stmt|;
name|ElasticsearchException
name|cause
init|=
operator|(
name|ElasticsearchException
operator|)
name|parsed
operator|.
name|getCause
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|cause
operator|.
name|getMessage
argument_list|()
argument_list|,
literal|"Elasticsearch exception [type=exception, reason=bar]"
argument_list|)
expr_stmt|;
name|cause
operator|=
operator|(
name|ElasticsearchException
operator|)
name|cause
operator|.
name|getCause
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
name|cause
operator|.
name|getMessage
argument_list|()
argument_list|,
literal|"Elasticsearch exception [type=exception, reason=baz]"
argument_list|)
expr_stmt|;
name|cause
operator|=
operator|(
name|ElasticsearchException
operator|)
name|cause
operator|.
name|getCause
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
name|cause
operator|.
name|getMessage
argument_list|()
argument_list|,
literal|"Elasticsearch exception [type=routing_missing_exception, reason=routing is required for [_test]/[_type]/[_id]]"
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|cause
operator|.
name|getHeaderKeys
argument_list|()
argument_list|,
name|hasSize
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|cause
operator|.
name|getHeader
argument_list|(
literal|"index"
argument_list|)
argument_list|,
name|hasItem
argument_list|(
literal|"_test"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|cause
operator|.
name|getHeader
argument_list|(
literal|"index_uuid"
argument_list|)
argument_list|,
name|hasItem
argument_list|(
literal|"_na_"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testFromXContentWithHeaders
specifier|public
name|void
name|testFromXContentWithHeaders
parameter_list|()
throws|throws
name|IOException
block|{
name|RoutingMissingException
name|routing
init|=
operator|new
name|RoutingMissingException
argument_list|(
literal|"_test"
argument_list|,
literal|"_type"
argument_list|,
literal|"_id"
argument_list|)
decl_stmt|;
name|ElasticsearchException
name|baz
init|=
operator|new
name|ElasticsearchException
argument_list|(
literal|"baz"
argument_list|,
name|routing
argument_list|)
decl_stmt|;
name|baz
operator|.
name|addHeader
argument_list|(
literal|"baz_0"
argument_list|,
literal|"baz0"
argument_list|)
expr_stmt|;
name|baz
operator|.
name|addHeader
argument_list|(
literal|"es.baz_1"
argument_list|,
literal|"baz1"
argument_list|)
expr_stmt|;
name|baz
operator|.
name|addHeader
argument_list|(
literal|"baz_2"
argument_list|,
literal|"baz2"
argument_list|)
expr_stmt|;
name|baz
operator|.
name|addHeader
argument_list|(
literal|"es.baz_3"
argument_list|,
literal|"baz3"
argument_list|)
expr_stmt|;
name|ElasticsearchException
name|bar
init|=
operator|new
name|ElasticsearchException
argument_list|(
literal|"bar"
argument_list|,
name|baz
argument_list|)
decl_stmt|;
name|bar
operator|.
name|addHeader
argument_list|(
literal|"es.bar_0"
argument_list|,
literal|"bar0"
argument_list|)
expr_stmt|;
name|bar
operator|.
name|addHeader
argument_list|(
literal|"bar_1"
argument_list|,
literal|"bar1"
argument_list|)
expr_stmt|;
name|bar
operator|.
name|addHeader
argument_list|(
literal|"es.bar_2"
argument_list|,
literal|"bar2"
argument_list|)
expr_stmt|;
name|ElasticsearchException
name|foo
init|=
operator|new
name|ElasticsearchException
argument_list|(
literal|"foo"
argument_list|,
name|bar
argument_list|)
decl_stmt|;
name|foo
operator|.
name|addHeader
argument_list|(
literal|"es.foo_0"
argument_list|,
literal|"foo0"
argument_list|)
expr_stmt|;
name|foo
operator|.
name|addHeader
argument_list|(
literal|"foo_1"
argument_list|,
literal|"foo1"
argument_list|)
expr_stmt|;
specifier|final
name|XContent
name|xContent
init|=
name|randomFrom
argument_list|(
name|XContentType
operator|.
name|values
argument_list|()
argument_list|)
operator|.
name|xContent
argument_list|()
decl_stmt|;
name|XContentBuilder
name|builder
init|=
name|XContentBuilder
operator|.
name|builder
argument_list|(
name|xContent
argument_list|)
operator|.
name|startObject
argument_list|()
operator|.
name|value
argument_list|(
name|foo
argument_list|)
operator|.
name|endObject
argument_list|()
decl_stmt|;
name|ElasticsearchException
name|parsed
decl_stmt|;
try|try
init|(
name|XContentParser
name|parser
init|=
name|xContent
operator|.
name|createParser
argument_list|(
name|builder
operator|.
name|bytes
argument_list|()
argument_list|)
init|)
block|{
name|assertEquals
argument_list|(
name|XContentParser
operator|.
name|Token
operator|.
name|START_OBJECT
argument_list|,
name|parser
operator|.
name|nextToken
argument_list|()
argument_list|)
expr_stmt|;
name|parsed
operator|=
name|ElasticsearchException
operator|.
name|fromXContent
argument_list|(
name|parser
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|XContentParser
operator|.
name|Token
operator|.
name|END_OBJECT
argument_list|,
name|parser
operator|.
name|currentToken
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|parser
operator|.
name|nextToken
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|assertNotNull
argument_list|(
name|parsed
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|parsed
operator|.
name|getMessage
argument_list|()
argument_list|,
literal|"Elasticsearch exception [type=exception, reason=foo]"
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|parsed
operator|.
name|getHeaderKeys
argument_list|()
argument_list|,
name|hasSize
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|parsed
operator|.
name|getHeader
argument_list|(
literal|"foo_0"
argument_list|)
argument_list|,
name|hasItem
argument_list|(
literal|"foo0"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|parsed
operator|.
name|getHeader
argument_list|(
literal|"foo_1"
argument_list|)
argument_list|,
name|hasItem
argument_list|(
literal|"foo1"
argument_list|)
argument_list|)
expr_stmt|;
name|ElasticsearchException
name|cause
init|=
operator|(
name|ElasticsearchException
operator|)
name|parsed
operator|.
name|getCause
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|cause
operator|.
name|getMessage
argument_list|()
argument_list|,
literal|"Elasticsearch exception [type=exception, reason=bar]"
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|cause
operator|.
name|getHeaderKeys
argument_list|()
argument_list|,
name|hasSize
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|cause
operator|.
name|getHeader
argument_list|(
literal|"bar_0"
argument_list|)
argument_list|,
name|hasItem
argument_list|(
literal|"bar0"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|cause
operator|.
name|getHeader
argument_list|(
literal|"bar_1"
argument_list|)
argument_list|,
name|hasItem
argument_list|(
literal|"bar1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|cause
operator|.
name|getHeader
argument_list|(
literal|"bar_2"
argument_list|)
argument_list|,
name|hasItem
argument_list|(
literal|"bar2"
argument_list|)
argument_list|)
expr_stmt|;
name|cause
operator|=
operator|(
name|ElasticsearchException
operator|)
name|cause
operator|.
name|getCause
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
name|cause
operator|.
name|getMessage
argument_list|()
argument_list|,
literal|"Elasticsearch exception [type=exception, reason=baz]"
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|cause
operator|.
name|getHeaderKeys
argument_list|()
argument_list|,
name|hasSize
argument_list|(
literal|4
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|cause
operator|.
name|getHeader
argument_list|(
literal|"baz_0"
argument_list|)
argument_list|,
name|hasItem
argument_list|(
literal|"baz0"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|cause
operator|.
name|getHeader
argument_list|(
literal|"baz_1"
argument_list|)
argument_list|,
name|hasItem
argument_list|(
literal|"baz1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|cause
operator|.
name|getHeader
argument_list|(
literal|"baz_2"
argument_list|)
argument_list|,
name|hasItem
argument_list|(
literal|"baz2"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|cause
operator|.
name|getHeader
argument_list|(
literal|"baz_3"
argument_list|)
argument_list|,
name|hasItem
argument_list|(
literal|"baz3"
argument_list|)
argument_list|)
expr_stmt|;
name|cause
operator|=
operator|(
name|ElasticsearchException
operator|)
name|cause
operator|.
name|getCause
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
name|cause
operator|.
name|getMessage
argument_list|()
argument_list|,
literal|"Elasticsearch exception [type=routing_missing_exception, reason=routing is required for [_test]/[_type]/[_id]]"
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|cause
operator|.
name|getHeaderKeys
argument_list|()
argument_list|,
name|hasSize
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|cause
operator|.
name|getHeader
argument_list|(
literal|"index"
argument_list|)
argument_list|,
name|hasItem
argument_list|(
literal|"_test"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|cause
operator|.
name|getHeader
argument_list|(
literal|"index_uuid"
argument_list|)
argument_list|,
name|hasItem
argument_list|(
literal|"_na_"
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**      * Builds a {@link ToXContent} using a JSON XContentBuilder and check the resulting string with the given {@link Matcher}.      *      * By default, the stack trace of the exception is not rendered. The parameter `errorTrace` forces the stack trace to      * be rendered like the REST API does when the "error_trace" parameter is set to true.      */
DECL|method|assertExceptionAsJson
specifier|private
specifier|static
name|void
name|assertExceptionAsJson
parameter_list|(
name|ElasticsearchException
name|e
parameter_list|,
name|boolean
name|errorTrace
parameter_list|,
name|Matcher
argument_list|<
name|String
argument_list|>
name|expected
parameter_list|)
throws|throws
name|IOException
block|{
name|ToXContent
operator|.
name|Params
name|params
init|=
name|ToXContent
operator|.
name|EMPTY_PARAMS
decl_stmt|;
if|if
condition|(
name|errorTrace
condition|)
block|{
name|params
operator|=
operator|new
name|ToXContent
operator|.
name|MapParams
argument_list|(
name|Collections
operator|.
name|singletonMap
argument_list|(
name|ElasticsearchException
operator|.
name|REST_EXCEPTION_SKIP_STACK_TRACE
argument_list|,
literal|"false"
argument_list|)
argument_list|)
expr_stmt|;
block|}
try|try
init|(
name|XContentBuilder
name|builder
init|=
name|XContentBuilder
operator|.
name|builder
argument_list|(
name|XContentType
operator|.
name|JSON
operator|.
name|xContent
argument_list|()
argument_list|)
init|)
block|{
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|e
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|builder
operator|.
name|bytes
argument_list|()
operator|.
name|utf8ToString
argument_list|()
argument_list|,
name|expected
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

