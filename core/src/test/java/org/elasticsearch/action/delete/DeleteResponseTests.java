begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.delete
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|delete
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
name|seqno
operator|.
name|SequenceNumbersService
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
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|index
operator|.
name|IndexResponseTests
operator|.
name|assertDocWriteResponse
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|metadata
operator|.
name|IndexMetaData
operator|.
name|INDEX_UUID_NA_VALUE
import|;
end_import

begin_class
DECL|class|DeleteResponseTests
specifier|public
class|class
name|DeleteResponseTests
extends|extends
name|ESTestCase
block|{
DECL|method|testToXContent
specifier|public
name|void
name|testToXContent
parameter_list|()
block|{
block|{
name|DeleteResponse
name|response
init|=
operator|new
name|DeleteResponse
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
literal|17
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
name|response
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"{\"found\":true,\"_index\":\"index\",\"_type\":\"type\",\"_id\":\"id\",\"_version\":5,\"result\":\"deleted\","
operator|+
literal|"\"_shards\":null,\"_seq_no\":3,\"_primary_term\":17}"
argument_list|,
name|output
argument_list|)
expr_stmt|;
block|}
block|{
name|DeleteResponse
name|response
init|=
operator|new
name|DeleteResponse
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
literal|0
argument_list|,
literal|7
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|response
operator|.
name|setForcedRefresh
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|response
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
name|response
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"{\"found\":true,\"_index\":\"index\",\"_type\":\"type\",\"_id\":\"id\",\"_version\":7,\"result\":\"deleted\","
operator|+
literal|"\"forced_refresh\":true,\"_shards\":{\"total\":10,\"successful\":5,\"failed\":0}}"
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
name|Tuple
argument_list|<
name|DeleteResponse
argument_list|,
name|DeleteResponse
argument_list|>
name|tuple
init|=
name|randomDeleteResponse
argument_list|()
decl_stmt|;
name|DeleteResponse
name|deleteResponse
init|=
name|tuple
operator|.
name|v1
argument_list|()
decl_stmt|;
name|DeleteResponse
name|expectedDeleteResponse
init|=
name|tuple
operator|.
name|v2
argument_list|()
decl_stmt|;
name|boolean
name|humanReadable
init|=
name|randomBoolean
argument_list|()
decl_stmt|;
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
name|BytesReference
name|deleteResponseBytes
init|=
name|toShuffledXContent
argument_list|(
name|deleteResponse
argument_list|,
name|xContentType
argument_list|,
name|ToXContent
operator|.
name|EMPTY_PARAMS
argument_list|,
name|humanReadable
argument_list|)
decl_stmt|;
name|DeleteResponse
name|parsedDeleteResponse
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
name|deleteResponseBytes
argument_list|)
init|)
block|{
name|parsedDeleteResponse
operator|=
name|DeleteResponse
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
comment|// We can't use equals() to compare the original and the parsed delete response
comment|// because the random delete response can contain shard failures with exceptions,
comment|// and those exceptions are not parsed back with the same types.
name|assertDocWriteResponse
argument_list|(
name|expectedDeleteResponse
argument_list|,
name|parsedDeleteResponse
argument_list|)
expr_stmt|;
block|}
comment|/**      * Returns a tuple of {@link DeleteResponse}s.      *<p>      * The left element is the actual {@link DeleteResponse} to serialize while the right element is the      * expected {@link DeleteResponse} after parsing.      */
DECL|method|randomDeleteResponse
specifier|public
specifier|static
name|Tuple
argument_list|<
name|DeleteResponse
argument_list|,
name|DeleteResponse
argument_list|>
name|randomDeleteResponse
parameter_list|()
block|{
name|String
name|index
init|=
name|randomAlphaOfLength
argument_list|(
literal|5
argument_list|)
decl_stmt|;
name|String
name|indexUUid
init|=
name|randomAlphaOfLength
argument_list|(
literal|5
argument_list|)
decl_stmt|;
name|int
name|shardId
init|=
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|5
argument_list|)
decl_stmt|;
name|String
name|type
init|=
name|randomAlphaOfLength
argument_list|(
literal|5
argument_list|)
decl_stmt|;
name|String
name|id
init|=
name|randomAlphaOfLength
argument_list|(
literal|5
argument_list|)
decl_stmt|;
name|long
name|seqNo
init|=
name|randomFrom
argument_list|(
name|SequenceNumbersService
operator|.
name|UNASSIGNED_SEQ_NO
argument_list|,
name|randomNonNegativeLong
argument_list|()
argument_list|,
operator|(
name|long
operator|)
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|10000
argument_list|)
argument_list|)
decl_stmt|;
name|long
name|primaryTerm
init|=
name|seqNo
operator|==
name|SequenceNumbersService
operator|.
name|UNASSIGNED_SEQ_NO
condition|?
literal|0
else|:
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|10000
argument_list|)
decl_stmt|;
name|long
name|version
init|=
name|randomBoolean
argument_list|()
condition|?
name|randomNonNegativeLong
argument_list|()
else|:
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|10000
argument_list|)
decl_stmt|;
name|boolean
name|found
init|=
name|randomBoolean
argument_list|()
decl_stmt|;
name|boolean
name|forcedRefresh
init|=
name|randomBoolean
argument_list|()
decl_stmt|;
name|Tuple
argument_list|<
name|ReplicationResponse
operator|.
name|ShardInfo
argument_list|,
name|ReplicationResponse
operator|.
name|ShardInfo
argument_list|>
name|shardInfos
init|=
name|RandomObjects
operator|.
name|randomShardInfo
argument_list|(
name|random
argument_list|()
argument_list|)
decl_stmt|;
name|DeleteResponse
name|actual
init|=
operator|new
name|DeleteResponse
argument_list|(
operator|new
name|ShardId
argument_list|(
name|index
argument_list|,
name|indexUUid
argument_list|,
name|shardId
argument_list|)
argument_list|,
name|type
argument_list|,
name|id
argument_list|,
name|seqNo
argument_list|,
name|primaryTerm
argument_list|,
name|version
argument_list|,
name|found
argument_list|)
decl_stmt|;
name|actual
operator|.
name|setForcedRefresh
argument_list|(
name|forcedRefresh
argument_list|)
expr_stmt|;
name|actual
operator|.
name|setShardInfo
argument_list|(
name|shardInfos
operator|.
name|v1
argument_list|()
argument_list|)
expr_stmt|;
name|DeleteResponse
name|expected
init|=
operator|new
name|DeleteResponse
argument_list|(
operator|new
name|ShardId
argument_list|(
name|index
argument_list|,
name|INDEX_UUID_NA_VALUE
argument_list|,
operator|-
literal|1
argument_list|)
argument_list|,
name|type
argument_list|,
name|id
argument_list|,
name|seqNo
argument_list|,
name|primaryTerm
argument_list|,
name|version
argument_list|,
name|found
argument_list|)
decl_stmt|;
name|expected
operator|.
name|setForcedRefresh
argument_list|(
name|forcedRefresh
argument_list|)
expr_stmt|;
name|expected
operator|.
name|setShardInfo
argument_list|(
name|shardInfos
operator|.
name|v2
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|Tuple
operator|.
name|tuple
argument_list|(
name|actual
argument_list|,
name|expected
argument_list|)
return|;
block|}
block|}
end_class

end_unit

