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
name|search
operator|.
name|SearchPhaseExecutionException
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
name|ShardSearchFailure
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
name|io
operator|.
name|BytesStream
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
name|io
operator|.
name|stream
operator|.
name|BytesStreamInput
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
name|io
operator|.
name|stream
operator|.
name|BytesStreamOutput
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
name|XContentFactory
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
name|query
operator|.
name|QueryParsingException
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
name|IndexMissingException
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
name|ElasticsearchTestCase
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
name|RemoteTransportException
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
name|io
operator|.
name|EOFException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|FileNotFoundException
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
DECL|class|ElasticsearchExceptionTests
specifier|public
class|class
name|ElasticsearchExceptionTests
extends|extends
name|ElasticsearchTestCase
block|{
annotation|@
name|Test
DECL|method|testStatus
specifier|public
name|void
name|testStatus
parameter_list|()
block|{
name|ElasticsearchException
name|exception
init|=
operator|new
name|ElasticsearchException
argument_list|(
literal|"test"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|exception
operator|.
name|status
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|RestStatus
operator|.
name|INTERNAL_SERVER_ERROR
argument_list|)
argument_list|)
expr_stmt|;
name|exception
operator|=
operator|new
name|ElasticsearchException
argument_list|(
literal|"test"
argument_list|,
operator|new
name|RuntimeException
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|exception
operator|.
name|status
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|RestStatus
operator|.
name|INTERNAL_SERVER_ERROR
argument_list|)
argument_list|)
expr_stmt|;
name|exception
operator|=
operator|new
name|ElasticsearchException
argument_list|(
literal|"test"
argument_list|,
operator|new
name|IndexMissingException
argument_list|(
operator|new
name|Index
argument_list|(
literal|"test"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|exception
operator|.
name|status
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|RestStatus
operator|.
name|INTERNAL_SERVER_ERROR
argument_list|)
argument_list|)
expr_stmt|;
name|exception
operator|=
operator|new
name|RemoteTransportException
argument_list|(
literal|"test"
argument_list|,
operator|new
name|IndexMissingException
argument_list|(
operator|new
name|Index
argument_list|(
literal|"test"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|exception
operator|.
name|status
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|RestStatus
operator|.
name|NOT_FOUND
argument_list|)
argument_list|)
expr_stmt|;
name|exception
operator|=
operator|new
name|RemoteTransportException
argument_list|(
literal|"test"
argument_list|,
operator|new
name|IllegalArgumentException
argument_list|(
literal|"foobar"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|exception
operator|.
name|status
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|RestStatus
operator|.
name|BAD_REQUEST
argument_list|)
argument_list|)
expr_stmt|;
name|exception
operator|=
operator|new
name|RemoteTransportException
argument_list|(
literal|"test"
argument_list|,
operator|new
name|IllegalStateException
argument_list|(
literal|"foobar"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|exception
operator|.
name|status
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|RestStatus
operator|.
name|INTERNAL_SERVER_ERROR
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testGuessRootCause
specifier|public
name|void
name|testGuessRootCause
parameter_list|()
block|{
block|{
name|ElasticsearchException
name|exception
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
name|ElasticsearchIllegalArgumentException
argument_list|(
literal|"index is closed"
argument_list|,
operator|new
name|RuntimeException
argument_list|(
literal|"foobar"
argument_list|)
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|ElasticsearchException
index|[]
name|rootCauses
init|=
name|exception
operator|.
name|guessRootCauses
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|rootCauses
operator|.
name|length
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|ElasticsearchException
operator|.
name|getExceptionName
argument_list|(
name|rootCauses
index|[
literal|0
index|]
argument_list|)
argument_list|,
literal|"illegal_argument_exception"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|rootCauses
index|[
literal|0
index|]
operator|.
name|getMessage
argument_list|()
argument_list|,
literal|"index is closed"
argument_list|)
expr_stmt|;
name|ShardSearchFailure
name|failure
init|=
operator|new
name|ShardSearchFailure
argument_list|(
operator|new
name|QueryParsingException
argument_list|(
operator|new
name|Index
argument_list|(
literal|"foo"
argument_list|)
argument_list|,
literal|"foobar"
argument_list|)
argument_list|,
operator|new
name|SearchShardTarget
argument_list|(
literal|"node_1"
argument_list|,
literal|"foo"
argument_list|,
literal|1
argument_list|)
argument_list|)
decl_stmt|;
name|ShardSearchFailure
name|failure1
init|=
operator|new
name|ShardSearchFailure
argument_list|(
operator|new
name|QueryParsingException
argument_list|(
operator|new
name|Index
argument_list|(
literal|"foo"
argument_list|)
argument_list|,
literal|"foobar"
argument_list|)
argument_list|,
operator|new
name|SearchShardTarget
argument_list|(
literal|"node_1"
argument_list|,
literal|"foo"
argument_list|,
literal|2
argument_list|)
argument_list|)
decl_stmt|;
name|SearchPhaseExecutionException
name|ex
init|=
operator|new
name|SearchPhaseExecutionException
argument_list|(
literal|"search"
argument_list|,
literal|"all shards failed"
argument_list|,
operator|new
name|ShardSearchFailure
index|[]
block|{
name|failure
block|,
name|failure1
block|}
argument_list|)
decl_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|rootCauses
operator|=
operator|(
name|randomBoolean
argument_list|()
condition|?
operator|new
name|RemoteTransportException
argument_list|(
literal|"remoteboom"
argument_list|,
name|ex
argument_list|)
else|:
name|ex
operator|)
operator|.
name|guessRootCauses
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|rootCauses
operator|=
name|ElasticsearchException
operator|.
name|guessRootCauses
argument_list|(
name|randomBoolean
argument_list|()
condition|?
operator|new
name|RemoteTransportException
argument_list|(
literal|"remoteboom"
argument_list|,
name|ex
argument_list|)
else|:
name|ex
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|ElasticsearchException
operator|.
name|getExceptionName
argument_list|(
name|rootCauses
index|[
literal|0
index|]
argument_list|)
argument_list|,
literal|"query_parsing_exception"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|rootCauses
index|[
literal|0
index|]
operator|.
name|getMessage
argument_list|()
argument_list|,
literal|"foobar"
argument_list|)
expr_stmt|;
name|ElasticsearchException
name|oneLevel
init|=
operator|new
name|ElasticsearchException
argument_list|(
literal|"foo"
argument_list|,
operator|new
name|RuntimeException
argument_list|(
literal|"foobar"
argument_list|)
argument_list|)
decl_stmt|;
name|rootCauses
operator|=
name|oneLevel
operator|.
name|guessRootCauses
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
name|ElasticsearchException
operator|.
name|getExceptionName
argument_list|(
name|rootCauses
index|[
literal|0
index|]
argument_list|)
argument_list|,
literal|"exception"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|rootCauses
index|[
literal|0
index|]
operator|.
name|getMessage
argument_list|()
argument_list|,
literal|"foo"
argument_list|)
expr_stmt|;
block|}
block|{
name|ShardSearchFailure
name|failure
init|=
operator|new
name|ShardSearchFailure
argument_list|(
operator|new
name|QueryParsingException
argument_list|(
operator|new
name|Index
argument_list|(
literal|"foo"
argument_list|)
argument_list|,
literal|"foobar"
argument_list|)
argument_list|,
operator|new
name|SearchShardTarget
argument_list|(
literal|"node_1"
argument_list|,
literal|"foo"
argument_list|,
literal|1
argument_list|)
argument_list|)
decl_stmt|;
name|ShardSearchFailure
name|failure1
init|=
operator|new
name|ShardSearchFailure
argument_list|(
operator|new
name|QueryParsingException
argument_list|(
operator|new
name|Index
argument_list|(
literal|"foo1"
argument_list|)
argument_list|,
literal|"foobar"
argument_list|)
argument_list|,
operator|new
name|SearchShardTarget
argument_list|(
literal|"node_1"
argument_list|,
literal|"foo1"
argument_list|,
literal|1
argument_list|)
argument_list|)
decl_stmt|;
name|ShardSearchFailure
name|failure2
init|=
operator|new
name|ShardSearchFailure
argument_list|(
operator|new
name|QueryParsingException
argument_list|(
operator|new
name|Index
argument_list|(
literal|"foo1"
argument_list|)
argument_list|,
literal|"foobar"
argument_list|)
argument_list|,
operator|new
name|SearchShardTarget
argument_list|(
literal|"node_1"
argument_list|,
literal|"foo1"
argument_list|,
literal|2
argument_list|)
argument_list|)
decl_stmt|;
name|SearchPhaseExecutionException
name|ex
init|=
operator|new
name|SearchPhaseExecutionException
argument_list|(
literal|"search"
argument_list|,
literal|"all shards failed"
argument_list|,
operator|new
name|ShardSearchFailure
index|[]
block|{
name|failure
block|,
name|failure1
block|,
name|failure2
block|}
argument_list|)
decl_stmt|;
specifier|final
name|ElasticsearchException
index|[]
name|rootCauses
init|=
name|ex
operator|.
name|guessRootCauses
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|rootCauses
operator|.
name|length
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|ElasticsearchException
operator|.
name|getExceptionName
argument_list|(
name|rootCauses
index|[
literal|0
index|]
argument_list|)
argument_list|,
literal|"query_parsing_exception"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|rootCauses
index|[
literal|0
index|]
operator|.
name|getMessage
argument_list|()
argument_list|,
literal|"foobar"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|(
operator|(
name|QueryParsingException
operator|)
name|rootCauses
index|[
literal|0
index|]
operator|)
operator|.
name|index
argument_list|()
operator|.
name|name
argument_list|()
argument_list|,
literal|"foo"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|ElasticsearchException
operator|.
name|getExceptionName
argument_list|(
name|rootCauses
index|[
literal|1
index|]
argument_list|)
argument_list|,
literal|"query_parsing_exception"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|rootCauses
index|[
literal|1
index|]
operator|.
name|getMessage
argument_list|()
argument_list|,
literal|"foobar"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|(
operator|(
name|QueryParsingException
operator|)
name|rootCauses
index|[
literal|1
index|]
operator|)
operator|.
name|index
argument_list|()
operator|.
name|name
argument_list|()
argument_list|,
literal|"foo1"
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testDeduplicate
specifier|public
name|void
name|testDeduplicate
parameter_list|()
throws|throws
name|IOException
block|{
block|{
name|ShardSearchFailure
name|failure
init|=
operator|new
name|ShardSearchFailure
argument_list|(
operator|new
name|QueryParsingException
argument_list|(
operator|new
name|Index
argument_list|(
literal|"foo"
argument_list|)
argument_list|,
literal|"foobar"
argument_list|)
argument_list|,
operator|new
name|SearchShardTarget
argument_list|(
literal|"node_1"
argument_list|,
literal|"foo"
argument_list|,
literal|1
argument_list|)
argument_list|)
decl_stmt|;
name|ShardSearchFailure
name|failure1
init|=
operator|new
name|ShardSearchFailure
argument_list|(
operator|new
name|QueryParsingException
argument_list|(
operator|new
name|Index
argument_list|(
literal|"foo"
argument_list|)
argument_list|,
literal|"foobar"
argument_list|)
argument_list|,
operator|new
name|SearchShardTarget
argument_list|(
literal|"node_1"
argument_list|,
literal|"foo"
argument_list|,
literal|2
argument_list|)
argument_list|)
decl_stmt|;
name|SearchPhaseExecutionException
name|ex
init|=
operator|new
name|SearchPhaseExecutionException
argument_list|(
literal|"search"
argument_list|,
literal|"all shards failed"
argument_list|,
operator|new
name|ShardSearchFailure
index|[]
block|{
name|failure
block|,
name|failure1
block|}
argument_list|)
decl_stmt|;
name|XContentBuilder
name|builder
init|=
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|prettyPrint
argument_list|()
decl_stmt|;
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|ex
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|ToXContent
operator|.
name|EMPTY_PARAMS
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|String
name|expected
init|=
literal|"{\n"
operator|+
literal|"  \"type\" : \"search_phase_execution_exception\",\n"
operator|+
literal|"  \"reason\" : \"all shards failed\",\n"
operator|+
literal|"  \"phase\" : \"search\",\n"
operator|+
literal|"  \"grouped\" : true,\n"
operator|+
literal|"  \"failed_shards\" : [ {\n"
operator|+
literal|"    \"shard\" : 1,\n"
operator|+
literal|"    \"index\" : \"foo\",\n"
operator|+
literal|"    \"node\" : \"node_1\",\n"
operator|+
literal|"    \"reason\" : {\n"
operator|+
literal|"      \"type\" : \"query_parsing_exception\",\n"
operator|+
literal|"      \"reason\" : \"foobar\",\n"
operator|+
literal|"      \"index\" : \"foo\"\n"
operator|+
literal|"    }\n"
operator|+
literal|"  } ]\n"
operator|+
literal|"}"
decl_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|builder
operator|.
name|string
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|{
name|ShardSearchFailure
name|failure
init|=
operator|new
name|ShardSearchFailure
argument_list|(
operator|new
name|QueryParsingException
argument_list|(
operator|new
name|Index
argument_list|(
literal|"foo"
argument_list|)
argument_list|,
literal|"foobar"
argument_list|)
argument_list|,
operator|new
name|SearchShardTarget
argument_list|(
literal|"node_1"
argument_list|,
literal|"foo"
argument_list|,
literal|1
argument_list|)
argument_list|)
decl_stmt|;
name|ShardSearchFailure
name|failure1
init|=
operator|new
name|ShardSearchFailure
argument_list|(
operator|new
name|QueryParsingException
argument_list|(
operator|new
name|Index
argument_list|(
literal|"foo1"
argument_list|)
argument_list|,
literal|"foobar"
argument_list|)
argument_list|,
operator|new
name|SearchShardTarget
argument_list|(
literal|"node_1"
argument_list|,
literal|"foo1"
argument_list|,
literal|1
argument_list|)
argument_list|)
decl_stmt|;
name|ShardSearchFailure
name|failure2
init|=
operator|new
name|ShardSearchFailure
argument_list|(
operator|new
name|QueryParsingException
argument_list|(
operator|new
name|Index
argument_list|(
literal|"foo1"
argument_list|)
argument_list|,
literal|"foobar"
argument_list|)
argument_list|,
operator|new
name|SearchShardTarget
argument_list|(
literal|"node_1"
argument_list|,
literal|"foo1"
argument_list|,
literal|2
argument_list|)
argument_list|)
decl_stmt|;
name|SearchPhaseExecutionException
name|ex
init|=
operator|new
name|SearchPhaseExecutionException
argument_list|(
literal|"search"
argument_list|,
literal|"all shards failed"
argument_list|,
operator|new
name|ShardSearchFailure
index|[]
block|{
name|failure
block|,
name|failure1
block|,
name|failure2
block|}
argument_list|)
decl_stmt|;
name|XContentBuilder
name|builder
init|=
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|prettyPrint
argument_list|()
decl_stmt|;
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|ex
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|ToXContent
operator|.
name|EMPTY_PARAMS
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|String
name|expected
init|=
literal|"{\n"
operator|+
literal|"  \"type\" : \"search_phase_execution_exception\",\n"
operator|+
literal|"  \"reason\" : \"all shards failed\",\n"
operator|+
literal|"  \"phase\" : \"search\",\n"
operator|+
literal|"  \"grouped\" : true,\n"
operator|+
literal|"  \"failed_shards\" : [ {\n"
operator|+
literal|"    \"shard\" : 1,\n"
operator|+
literal|"    \"index\" : \"foo\",\n"
operator|+
literal|"    \"node\" : \"node_1\",\n"
operator|+
literal|"    \"reason\" : {\n"
operator|+
literal|"      \"type\" : \"query_parsing_exception\",\n"
operator|+
literal|"      \"reason\" : \"foobar\",\n"
operator|+
literal|"      \"index\" : \"foo\"\n"
operator|+
literal|"    }\n"
operator|+
literal|"  }, {\n"
operator|+
literal|"    \"shard\" : 1,\n"
operator|+
literal|"    \"index\" : \"foo1\",\n"
operator|+
literal|"    \"node\" : \"node_1\",\n"
operator|+
literal|"    \"reason\" : {\n"
operator|+
literal|"      \"type\" : \"query_parsing_exception\",\n"
operator|+
literal|"      \"reason\" : \"foobar\",\n"
operator|+
literal|"      \"index\" : \"foo1\"\n"
operator|+
literal|"    }\n"
operator|+
literal|"  } ]\n"
operator|+
literal|"}"
decl_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|builder
operator|.
name|string
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testGetRootCause
specifier|public
name|void
name|testGetRootCause
parameter_list|()
block|{
name|Exception
name|root
init|=
operator|new
name|RuntimeException
argument_list|(
literal|"foobar"
argument_list|)
decl_stmt|;
name|ElasticsearchException
name|exception
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
name|ElasticsearchIllegalArgumentException
argument_list|(
literal|"index is closed"
argument_list|,
name|root
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|root
argument_list|,
name|exception
operator|.
name|getRootCause
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|exception
operator|.
name|contains
argument_list|(
name|RuntimeException
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|exception
operator|.
name|contains
argument_list|(
name|EOFException
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testToString
specifier|public
name|void
name|testToString
parameter_list|()
block|{
name|ElasticsearchException
name|exception
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
name|ElasticsearchIllegalArgumentException
argument_list|(
literal|"index is closed"
argument_list|,
operator|new
name|RuntimeException
argument_list|(
literal|"foobar"
argument_list|)
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"ElasticsearchException[foo]; nested: ElasticsearchException[bar]; nested: ElasticsearchIllegalArgumentException[index is closed]; nested: RuntimeException[foobar];"
argument_list|,
name|exception
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testToXContent
specifier|public
name|void
name|testToXContent
parameter_list|()
throws|throws
name|IOException
block|{
block|{
name|ElasticsearchException
name|ex
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
name|ElasticsearchIllegalArgumentException
argument_list|(
literal|"index is closed"
argument_list|,
operator|new
name|RuntimeException
argument_list|(
literal|"foobar"
argument_list|)
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|XContentBuilder
name|builder
init|=
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|prettyPrint
argument_list|()
decl_stmt|;
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|ex
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|ToXContent
operator|.
name|EMPTY_PARAMS
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|String
name|expected
init|=
literal|"{\n"
operator|+
literal|"  \"type\" : \"exception\",\n"
operator|+
literal|"  \"reason\" : \"foo\",\n"
operator|+
literal|"  \"caused_by\" : {\n"
operator|+
literal|"    \"type\" : \"exception\",\n"
operator|+
literal|"    \"reason\" : \"bar\",\n"
operator|+
literal|"    \"caused_by\" : {\n"
operator|+
literal|"      \"type\" : \"illegal_argument_exception\",\n"
operator|+
literal|"      \"reason\" : \"index is closed\",\n"
operator|+
literal|"      \"caused_by\" : {\n"
operator|+
literal|"        \"type\" : \"runtime_exception\",\n"
operator|+
literal|"        \"reason\" : \"foobar\"\n"
operator|+
literal|"      }\n"
operator|+
literal|"    }\n"
operator|+
literal|"  }\n"
operator|+
literal|"}"
decl_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|builder
operator|.
name|string
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|{
name|Exception
name|ex
init|=
operator|new
name|FileNotFoundException
argument_list|(
literal|"foo not found"
argument_list|)
decl_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
comment|// just a wrapper which is omitted
name|ex
operator|=
operator|new
name|RemoteTransportException
argument_list|(
literal|"foobar"
argument_list|,
name|ex
argument_list|)
expr_stmt|;
block|}
name|XContentBuilder
name|builder
init|=
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|prettyPrint
argument_list|()
decl_stmt|;
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|ElasticsearchException
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|ToXContent
operator|.
name|EMPTY_PARAMS
argument_list|,
name|ex
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|String
name|expected
init|=
literal|"{\n"
operator|+
literal|"  \"type\" : \"file_not_found_exception\",\n"
operator|+
literal|"  \"reason\" : \"foo not found\"\n"
operator|+
literal|"}"
decl_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|builder
operator|.
name|string
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|{
comment|// test equivalence
name|ElasticsearchException
name|ex
init|=
operator|new
name|RemoteTransportException
argument_list|(
literal|"foobar"
argument_list|,
operator|new
name|FileNotFoundException
argument_list|(
literal|"foo not found"
argument_list|)
argument_list|)
decl_stmt|;
name|XContentBuilder
name|builder
init|=
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|prettyPrint
argument_list|()
decl_stmt|;
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|ElasticsearchException
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|ToXContent
operator|.
name|EMPTY_PARAMS
argument_list|,
name|ex
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|XContentBuilder
name|otherBuilder
init|=
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|prettyPrint
argument_list|()
decl_stmt|;
name|otherBuilder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|ex
operator|.
name|toXContent
argument_list|(
name|otherBuilder
argument_list|,
name|ToXContent
operator|.
name|EMPTY_PARAMS
argument_list|)
expr_stmt|;
name|otherBuilder
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
name|otherBuilder
operator|.
name|string
argument_list|()
argument_list|,
name|builder
operator|.
name|string
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testSerializeElasticsearchException
specifier|public
name|void
name|testSerializeElasticsearchException
parameter_list|()
throws|throws
name|IOException
block|{
name|BytesStreamOutput
name|out
init|=
operator|new
name|BytesStreamOutput
argument_list|()
decl_stmt|;
name|QueryParsingException
name|ex
init|=
operator|new
name|QueryParsingException
argument_list|(
operator|new
name|Index
argument_list|(
literal|"foo"
argument_list|)
argument_list|,
literal|"foobar"
argument_list|)
decl_stmt|;
name|out
operator|.
name|writeThrowable
argument_list|(
name|ex
argument_list|)
expr_stmt|;
name|BytesStreamInput
name|in
init|=
operator|new
name|BytesStreamInput
argument_list|(
name|out
operator|.
name|bytes
argument_list|()
argument_list|)
decl_stmt|;
name|QueryParsingException
name|e
init|=
name|in
operator|.
name|readThrowable
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|ex
operator|.
name|index
argument_list|()
argument_list|,
name|e
operator|.
name|index
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|ex
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

