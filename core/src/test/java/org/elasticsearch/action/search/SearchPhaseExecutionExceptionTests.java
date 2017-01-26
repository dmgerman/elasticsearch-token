begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.search
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|search
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchException
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
name|TimestampParsingException
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
name|ParsingException
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
name|Strings
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
name|XContentHelper
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
name|index
operator|.
name|Index
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
name|IndexShardClosedException
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
name|indices
operator|.
name|InvalidIndexTemplateException
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
name|SearchShardTarget
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
name|io
operator|.
name|IOException
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
name|xcontent
operator|.
name|XContentFactory
operator|.
name|jsonBuilder
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
name|Matchers
operator|.
name|hasSize
import|;
end_import

begin_class
DECL|class|SearchPhaseExecutionExceptionTests
specifier|public
class|class
name|SearchPhaseExecutionExceptionTests
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
name|SearchPhaseExecutionException
name|exception
init|=
operator|new
name|SearchPhaseExecutionException
argument_list|(
literal|"test"
argument_list|,
literal|"all shards failed"
argument_list|,
operator|new
name|ShardSearchFailure
index|[]
block|{
operator|new
name|ShardSearchFailure
argument_list|(
operator|new
name|ParsingException
argument_list|(
literal|1
argument_list|,
literal|2
argument_list|,
literal|"foobar"
argument_list|,
literal|null
argument_list|)
argument_list|,
operator|new
name|SearchShardTarget
argument_list|(
literal|"node_1"
argument_list|,
operator|new
name|Index
argument_list|(
literal|"foo"
argument_list|,
literal|"_na_"
argument_list|)
argument_list|,
literal|0
argument_list|)
argument_list|)
block|,
operator|new
name|ShardSearchFailure
argument_list|(
operator|new
name|IndexShardClosedException
argument_list|(
operator|new
name|ShardId
argument_list|(
operator|new
name|Index
argument_list|(
literal|"foo"
argument_list|,
literal|"_na_"
argument_list|)
argument_list|,
literal|1
argument_list|)
argument_list|)
argument_list|,
operator|new
name|SearchShardTarget
argument_list|(
literal|"node_2"
argument_list|,
operator|new
name|Index
argument_list|(
literal|"foo"
argument_list|,
literal|"_na_"
argument_list|)
argument_list|,
literal|1
argument_list|)
argument_list|)
block|,
operator|new
name|ShardSearchFailure
argument_list|(
operator|new
name|ParsingException
argument_list|(
literal|5
argument_list|,
literal|7
argument_list|,
literal|"foobar"
argument_list|,
literal|null
argument_list|)
argument_list|,
operator|new
name|SearchShardTarget
argument_list|(
literal|"node_3"
argument_list|,
operator|new
name|Index
argument_list|(
literal|"foo"
argument_list|,
literal|"_na_"
argument_list|)
argument_list|,
literal|2
argument_list|)
argument_list|)
block|,                 }
argument_list|)
decl_stmt|;
comment|// Failures are grouped (by default)
name|assertEquals
argument_list|(
literal|"{"
operator|+
literal|"\"type\":\"search_phase_execution_exception\","
operator|+
literal|"\"reason\":\"all shards failed\","
operator|+
literal|"\"phase\":\"test\","
operator|+
literal|"\"grouped\":true,"
operator|+
literal|"\"failed_shards\":["
operator|+
literal|"{"
operator|+
literal|"\"shard\":0,"
operator|+
literal|"\"index\":\"foo\","
operator|+
literal|"\"node\":\"node_1\","
operator|+
literal|"\"reason\":{"
operator|+
literal|"\"type\":\"parsing_exception\","
operator|+
literal|"\"reason\":\"foobar\","
operator|+
literal|"\"line\":1,"
operator|+
literal|"\"col\":2"
operator|+
literal|"}"
operator|+
literal|"},"
operator|+
literal|"{"
operator|+
literal|"\"shard\":1,"
operator|+
literal|"\"index\":\"foo\","
operator|+
literal|"\"node\":\"node_2\","
operator|+
literal|"\"reason\":{"
operator|+
literal|"\"type\":\"index_shard_closed_exception\","
operator|+
literal|"\"reason\":\"CurrentState[CLOSED] Closed\","
operator|+
literal|"\"index_uuid\":\"_na_\","
operator|+
literal|"\"shard\":\"1\","
operator|+
literal|"\"index\":\"foo\""
operator|+
literal|"}"
operator|+
literal|"}"
operator|+
literal|"]}"
argument_list|,
name|Strings
operator|.
name|toString
argument_list|(
name|exception
argument_list|)
argument_list|)
expr_stmt|;
comment|// Failures are NOT grouped
name|ToXContent
operator|.
name|MapParams
name|params
init|=
operator|new
name|ToXContent
operator|.
name|MapParams
argument_list|(
name|singletonMap
argument_list|(
literal|"group_shard_failures"
argument_list|,
literal|"false"
argument_list|)
argument_list|)
decl_stmt|;
try|try
init|(
name|XContentBuilder
name|builder
init|=
name|jsonBuilder
argument_list|()
init|)
block|{
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|exception
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
name|assertEquals
argument_list|(
literal|"{"
operator|+
literal|"\"type\":\"search_phase_execution_exception\","
operator|+
literal|"\"reason\":\"all shards failed\","
operator|+
literal|"\"phase\":\"test\","
operator|+
literal|"\"grouped\":false,"
operator|+
literal|"\"failed_shards\":["
operator|+
literal|"{"
operator|+
literal|"\"shard\":0,"
operator|+
literal|"\"index\":\"foo\","
operator|+
literal|"\"node\":\"node_1\","
operator|+
literal|"\"reason\":{"
operator|+
literal|"\"type\":\"parsing_exception\","
operator|+
literal|"\"reason\":\"foobar\","
operator|+
literal|"\"line\":1,"
operator|+
literal|"\"col\":2"
operator|+
literal|"}"
operator|+
literal|"},"
operator|+
literal|"{"
operator|+
literal|"\"shard\":1,"
operator|+
literal|"\"index\":\"foo\","
operator|+
literal|"\"node\":\"node_2\","
operator|+
literal|"\"reason\":{"
operator|+
literal|"\"type\":\"index_shard_closed_exception\","
operator|+
literal|"\"reason\":\"CurrentState[CLOSED] Closed\","
operator|+
literal|"\"index_uuid\":\"_na_\","
operator|+
literal|"\"shard\":\"1\","
operator|+
literal|"\"index\":\"foo\""
operator|+
literal|"}"
operator|+
literal|"},"
operator|+
literal|"{"
operator|+
literal|"\"shard\":2,"
operator|+
literal|"\"index\":\"foo\","
operator|+
literal|"\"node\":\"node_3\","
operator|+
literal|"\"reason\":{"
operator|+
literal|"\"type\":\"parsing_exception\","
operator|+
literal|"\"reason\":\"foobar\","
operator|+
literal|"\"line\":5,"
operator|+
literal|"\"col\":7"
operator|+
literal|"}"
operator|+
literal|"}"
operator|+
literal|"]}"
argument_list|,
name|builder
operator|.
name|string
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testToAndFromXContent
specifier|public
name|void
name|testToAndFromXContent
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
name|ShardSearchFailure
index|[]
name|shardSearchFailures
init|=
operator|new
name|ShardSearchFailure
index|[
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|5
argument_list|)
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
name|shardSearchFailures
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|Exception
name|cause
init|=
name|randomFrom
argument_list|(
operator|new
name|ParsingException
argument_list|(
literal|1
argument_list|,
literal|2
argument_list|,
literal|"foobar"
argument_list|,
literal|null
argument_list|)
argument_list|,
operator|new
name|InvalidIndexTemplateException
argument_list|(
literal|"foo"
argument_list|,
literal|"bar"
argument_list|)
argument_list|,
operator|new
name|TimestampParsingException
argument_list|(
literal|"foo"
argument_list|,
literal|null
argument_list|)
argument_list|,
operator|new
name|NullPointerException
argument_list|()
argument_list|)
decl_stmt|;
name|shardSearchFailures
index|[
name|i
index|]
operator|=
operator|new
name|ShardSearchFailure
argument_list|(
name|cause
argument_list|,
operator|new
name|SearchShardTarget
argument_list|(
literal|"node_"
operator|+
name|i
argument_list|,
operator|new
name|Index
argument_list|(
literal|"test"
argument_list|,
literal|"_na_"
argument_list|)
argument_list|,
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|final
name|String
name|phase
init|=
name|randomFrom
argument_list|(
literal|"query"
argument_list|,
literal|"search"
argument_list|,
literal|"other"
argument_list|)
decl_stmt|;
name|SearchPhaseExecutionException
name|actual
init|=
operator|new
name|SearchPhaseExecutionException
argument_list|(
name|phase
argument_list|,
literal|"unexpected failures"
argument_list|,
name|shardSearchFailures
argument_list|)
decl_stmt|;
name|BytesReference
name|exceptionBytes
init|=
name|XContentHelper
operator|.
name|toXContent
argument_list|(
name|actual
argument_list|,
name|xContent
operator|.
name|type
argument_list|()
argument_list|,
name|randomBoolean
argument_list|()
argument_list|)
decl_stmt|;
name|ElasticsearchException
name|parsedException
decl_stmt|;
try|try
init|(
name|XContentParser
name|parser
init|=
name|createParser
argument_list|(
name|xContent
argument_list|,
name|exceptionBytes
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
name|parsedException
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
name|parsedException
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|parsedException
operator|.
name|getHeaderKeys
argument_list|()
argument_list|,
name|hasSize
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|parsedException
operator|.
name|getMetadataKeys
argument_list|()
argument_list|,
name|hasSize
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|parsedException
operator|.
name|getMetadata
argument_list|(
literal|"es.phase"
argument_list|)
argument_list|,
name|hasItem
argument_list|(
name|phase
argument_list|)
argument_list|)
expr_stmt|;
comment|// SearchPhaseExecutionException has no cause field
name|assertNull
argument_list|(
name|parsedException
operator|.
name|getCause
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

