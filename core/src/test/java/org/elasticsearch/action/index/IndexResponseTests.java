begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.index
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|index
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
name|support
operator|.
name|replication
operator|.
name|ReplicationResponse
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
name|util
operator|.
name|CollectionUtils
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
name|RandomObjects
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
name|List
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
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|xcontent
operator|.
name|XContentHelper
operator|.
name|toXContent
import|;
end_import

begin_class
DECL|class|IndexResponseTests
specifier|public
class|class
name|IndexResponseTests
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
block|{
name|IndexResponse
name|indexResponse
init|=
operator|new
name|IndexResponse
argument_list|(
operator|new
name|ShardId
argument_list|(
literal|"index"
argument_list|,
literal|"index_uuid"
argument_list|,
literal|0
argument_list|)
argument_list|,
literal|"type"
argument_list|,
literal|"id"
argument_list|,
literal|3
argument_list|,
literal|5
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|String
name|output
init|=
name|Strings
operator|.
name|toString
argument_list|(
name|indexResponse
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"{\"_index\":\"index\",\"_type\":\"type\",\"_id\":\"id\",\"_version\":5,\"result\":\"created\",\"_shards\":null,"
operator|+
literal|"\"_seq_no\":3,\"created\":true}"
argument_list|,
name|output
argument_list|)
expr_stmt|;
block|}
block|{
name|IndexResponse
name|indexResponse
init|=
operator|new
name|IndexResponse
argument_list|(
operator|new
name|ShardId
argument_list|(
literal|"index"
argument_list|,
literal|"index_uuid"
argument_list|,
literal|0
argument_list|)
argument_list|,
literal|"type"
argument_list|,
literal|"id"
argument_list|,
operator|-
literal|1
argument_list|,
literal|7
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|indexResponse
operator|.
name|setForcedRefresh
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|indexResponse
operator|.
name|setShardInfo
argument_list|(
operator|new
name|ReplicationResponse
operator|.
name|ShardInfo
argument_list|(
literal|10
argument_list|,
literal|5
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|output
init|=
name|Strings
operator|.
name|toString
argument_list|(
name|indexResponse
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"{\"_index\":\"index\",\"_type\":\"type\",\"_id\":\"id\",\"_version\":7,\"result\":\"created\","
operator|+
literal|"\"forced_refresh\":true,\"_shards\":{\"total\":10,\"successful\":5,\"failed\":0},\"created\":true}"
argument_list|,
name|output
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
name|XContentType
name|xContentType
init|=
name|randomFrom
argument_list|(
name|XContentType
operator|.
name|values
argument_list|()
argument_list|)
decl_stmt|;
comment|// Create a random IndexResponse and converts it to XContent in bytes
name|IndexResponse
name|indexResponse
init|=
name|randomIndexResponse
argument_list|()
decl_stmt|;
name|BytesReference
name|indexResponseBytes
init|=
name|toXContent
argument_list|(
name|indexResponse
argument_list|,
name|xContentType
argument_list|)
decl_stmt|;
comment|// Parse the XContent bytes to obtain a parsed
name|IndexResponse
name|parsedIndexResponse
decl_stmt|;
try|try
init|(
name|XContentParser
name|parser
init|=
name|createParser
argument_list|(
name|xContentType
operator|.
name|xContent
argument_list|()
argument_list|,
name|indexResponseBytes
argument_list|)
init|)
block|{
name|parsedIndexResponse
operator|=
name|IndexResponse
operator|.
name|fromXContent
argument_list|(
name|parser
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
comment|// We can't use equals() to compare the original and the parsed index response
comment|// because the random index response can contain shard failures with exceptions,
comment|// and those exceptions are not parsed back with the same types.
comment|// Print the parsed object out and test that the output is the same as the original output
name|BytesReference
name|parsedIndexResponseBytes
init|=
name|toXContent
argument_list|(
name|parsedIndexResponse
argument_list|,
name|xContentType
argument_list|)
decl_stmt|;
try|try
init|(
name|XContentParser
name|parser
init|=
name|createParser
argument_list|(
name|xContentType
operator|.
name|xContent
argument_list|()
argument_list|,
name|parsedIndexResponseBytes
argument_list|)
init|)
block|{
name|assertIndexResponse
argument_list|(
name|indexResponse
argument_list|,
name|parser
operator|.
name|map
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|assertIndexResponse
specifier|private
specifier|static
name|void
name|assertIndexResponse
parameter_list|(
name|IndexResponse
name|expected
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|actual
parameter_list|)
block|{
name|assertEquals
argument_list|(
name|expected
operator|.
name|getIndex
argument_list|()
argument_list|,
name|actual
operator|.
name|get
argument_list|(
literal|"_index"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
operator|.
name|getType
argument_list|()
argument_list|,
name|actual
operator|.
name|get
argument_list|(
literal|"_type"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
operator|.
name|getId
argument_list|()
argument_list|,
name|actual
operator|.
name|get
argument_list|(
literal|"_id"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
operator|.
name|getVersion
argument_list|()
argument_list|,
operator|(
operator|(
name|Integer
operator|)
name|actual
operator|.
name|get
argument_list|(
literal|"_version"
argument_list|)
operator|)
operator|.
name|longValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
operator|.
name|getResult
argument_list|()
operator|.
name|getLowercase
argument_list|()
argument_list|,
name|actual
operator|.
name|get
argument_list|(
literal|"result"
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|expected
operator|.
name|forcedRefresh
argument_list|()
condition|)
block|{
name|assertTrue
argument_list|(
operator|(
name|Boolean
operator|)
name|actual
operator|.
name|get
argument_list|(
literal|"forced_refresh"
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertFalse
argument_list|(
name|actual
operator|.
name|containsKey
argument_list|(
literal|"forced_refresh"
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|expected
operator|.
name|getSeqNo
argument_list|()
operator|>=
literal|0
condition|)
block|{
name|assertEquals
argument_list|(
name|expected
operator|.
name|getSeqNo
argument_list|()
argument_list|,
operator|(
operator|(
name|Integer
operator|)
name|actual
operator|.
name|get
argument_list|(
literal|"_seq_no"
argument_list|)
operator|)
operator|.
name|longValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertFalse
argument_list|(
name|actual
operator|.
name|containsKey
argument_list|(
literal|"_seq_no"
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|actualShards
init|=
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|actual
operator|.
name|get
argument_list|(
literal|"_shards"
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|actualShards
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
operator|.
name|getShardInfo
argument_list|()
operator|.
name|getTotal
argument_list|()
argument_list|,
name|actualShards
operator|.
name|get
argument_list|(
literal|"total"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
operator|.
name|getShardInfo
argument_list|()
operator|.
name|getSuccessful
argument_list|()
argument_list|,
name|actualShards
operator|.
name|get
argument_list|(
literal|"successful"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
operator|.
name|getShardInfo
argument_list|()
operator|.
name|getFailed
argument_list|()
argument_list|,
name|actualShards
operator|.
name|get
argument_list|(
literal|"failed"
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|>
name|actualFailures
init|=
operator|(
name|List
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|>
operator|)
name|actualShards
operator|.
name|get
argument_list|(
literal|"failures"
argument_list|)
decl_stmt|;
if|if
condition|(
name|CollectionUtils
operator|.
name|isEmpty
argument_list|(
name|expected
operator|.
name|getShardInfo
argument_list|()
operator|.
name|getFailures
argument_list|()
argument_list|)
condition|)
block|{
name|assertNull
argument_list|(
name|actualFailures
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertEquals
argument_list|(
name|expected
operator|.
name|getShardInfo
argument_list|()
operator|.
name|getFailures
argument_list|()
operator|.
name|length
argument_list|,
name|actualFailures
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|expected
operator|.
name|getShardInfo
argument_list|()
operator|.
name|getFailures
argument_list|()
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|ReplicationResponse
operator|.
name|ShardInfo
operator|.
name|Failure
name|failure
init|=
name|expected
operator|.
name|getShardInfo
argument_list|()
operator|.
name|getFailures
argument_list|()
index|[
name|i
index|]
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|actualFailure
init|=
name|actualFailures
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|failure
operator|.
name|index
argument_list|()
argument_list|,
name|actualFailure
operator|.
name|get
argument_list|(
literal|"_index"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|failure
operator|.
name|shardId
argument_list|()
argument_list|,
name|actualFailure
operator|.
name|get
argument_list|(
literal|"_shard"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|failure
operator|.
name|nodeId
argument_list|()
argument_list|,
name|actualFailure
operator|.
name|get
argument_list|(
literal|"_node"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|failure
operator|.
name|status
argument_list|()
argument_list|,
name|RestStatus
operator|.
name|valueOf
argument_list|(
operator|(
name|String
operator|)
name|actualFailure
operator|.
name|get
argument_list|(
literal|"status"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|failure
operator|.
name|primary
argument_list|()
argument_list|,
name|actualFailure
operator|.
name|get
argument_list|(
literal|"primary"
argument_list|)
argument_list|)
expr_stmt|;
name|Throwable
name|cause
init|=
name|failure
operator|.
name|getCause
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|actualClause
init|=
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|actualFailure
operator|.
name|get
argument_list|(
literal|"reason"
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|actualClause
argument_list|)
expr_stmt|;
while|while
condition|(
name|cause
operator|!=
literal|null
condition|)
block|{
comment|// The expected IndexResponse has been converted in XContent, then the resulting bytes have been
comment|// parsed to create a new parsed IndexResponse. During this process, the type of the exceptions
comment|// have been lost.
name|assertEquals
argument_list|(
literal|"exception"
argument_list|,
name|actualClause
operator|.
name|get
argument_list|(
literal|"type"
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|expectedMessage
init|=
literal|"Elasticsearch exception [type="
operator|+
name|ElasticsearchException
operator|.
name|getExceptionName
argument_list|(
name|cause
argument_list|)
operator|+
literal|", reason="
operator|+
name|cause
operator|.
name|getMessage
argument_list|()
operator|+
literal|"]"
decl_stmt|;
name|assertEquals
argument_list|(
name|expectedMessage
argument_list|,
name|actualClause
operator|.
name|get
argument_list|(
literal|"reason"
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|cause
operator|instanceof
name|ElasticsearchException
condition|)
block|{
name|ElasticsearchException
name|ex
init|=
operator|(
name|ElasticsearchException
operator|)
name|cause
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|actualHeaders
init|=
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|actualClause
operator|.
name|get
argument_list|(
literal|"header"
argument_list|)
decl_stmt|;
comment|// When a IndexResponse is converted to XContent, the exception headers that start with "es."
comment|// are added to the XContent as fields with the prefix removed. Other headers are added under
comment|// a "header" root object.
comment|// In the test, the "es." prefix is lost when the XContent is generating, so when the parsed
comment|// IndexResponse is converted back to XContent all exception headers are under the "header" object.
for|for
control|(
name|String
name|name
range|:
name|ex
operator|.
name|getHeaderKeys
argument_list|()
control|)
block|{
name|assertEquals
argument_list|(
name|ex
operator|.
name|getHeader
argument_list|(
name|name
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|actualHeaders
operator|.
name|get
argument_list|(
name|name
operator|.
name|replaceFirst
argument_list|(
literal|"es."
argument_list|,
literal|""
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|actualClause
operator|=
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|actualClause
operator|.
name|get
argument_list|(
literal|"caused_by"
argument_list|)
expr_stmt|;
name|cause
operator|=
name|cause
operator|.
name|getCause
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
DECL|method|randomIndexResponse
specifier|private
specifier|static
name|IndexResponse
name|randomIndexResponse
parameter_list|()
block|{
name|ShardId
name|shardId
init|=
operator|new
name|ShardId
argument_list|(
name|randomAsciiOfLength
argument_list|(
literal|5
argument_list|)
argument_list|,
name|randomAsciiOfLength
argument_list|(
literal|5
argument_list|)
argument_list|,
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|5
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|type
init|=
name|randomAsciiOfLength
argument_list|(
literal|5
argument_list|)
decl_stmt|;
name|String
name|id
init|=
name|randomAsciiOfLength
argument_list|(
literal|5
argument_list|)
decl_stmt|;
name|long
name|seqNo
init|=
name|randomIntBetween
argument_list|(
operator|-
literal|2
argument_list|,
literal|5
argument_list|)
decl_stmt|;
name|long
name|version
init|=
operator|(
name|long
operator|)
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|5
argument_list|)
decl_stmt|;
name|boolean
name|created
init|=
name|randomBoolean
argument_list|()
decl_stmt|;
name|IndexResponse
name|indexResponse
init|=
operator|new
name|IndexResponse
argument_list|(
name|shardId
argument_list|,
name|type
argument_list|,
name|id
argument_list|,
name|seqNo
argument_list|,
name|version
argument_list|,
name|created
argument_list|)
decl_stmt|;
name|indexResponse
operator|.
name|setForcedRefresh
argument_list|(
name|randomBoolean
argument_list|()
argument_list|)
expr_stmt|;
name|indexResponse
operator|.
name|setShardInfo
argument_list|(
name|RandomObjects
operator|.
name|randomShardInfo
argument_list|(
name|random
argument_list|()
argument_list|,
name|randomBoolean
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|indexResponse
return|;
block|}
block|}
end_class

end_unit

