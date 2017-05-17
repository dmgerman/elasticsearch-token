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
name|Version
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
name|bytes
operator|.
name|BytesArray
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
name|io
operator|.
name|stream
operator|.
name|StreamInput
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
name|Streamable
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
name|lucene
operator|.
name|uid
operator|.
name|Versions
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
name|TimeValue
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|Script
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|ScriptType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|tasks
operator|.
name|TaskId
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
import|import
name|java
operator|.
name|util
operator|.
name|Collections
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
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|TestUtil
operator|.
name|randomSimpleString
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
name|parseTimeValue
import|;
end_import

begin_comment
comment|/**  * Round trip tests for all Streamable things declared in this plugin.  */
end_comment

begin_class
DECL|class|RoundTripTests
specifier|public
class|class
name|RoundTripTests
extends|extends
name|ESTestCase
block|{
DECL|method|testReindexRequest
specifier|public
name|void
name|testReindexRequest
parameter_list|()
throws|throws
name|IOException
block|{
name|ReindexRequest
name|reindex
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
name|randomRequest
argument_list|(
name|reindex
argument_list|)
expr_stmt|;
name|reindex
operator|.
name|getDestination
argument_list|()
operator|.
name|version
argument_list|(
name|randomFrom
argument_list|(
name|Versions
operator|.
name|MATCH_ANY
argument_list|,
name|Versions
operator|.
name|MATCH_DELETED
argument_list|,
literal|12L
argument_list|,
literal|1L
argument_list|,
literal|123124L
argument_list|,
literal|12L
argument_list|)
argument_list|)
expr_stmt|;
name|reindex
operator|.
name|getDestination
argument_list|()
operator|.
name|index
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|int
name|port
init|=
name|between
argument_list|(
literal|1
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
decl_stmt|;
name|BytesReference
name|query
init|=
operator|new
name|BytesArray
argument_list|(
name|randomAlphaOfLength
argument_list|(
literal|5
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|username
init|=
name|randomBoolean
argument_list|()
condition|?
name|randomAlphaOfLength
argument_list|(
literal|5
argument_list|)
else|:
literal|null
decl_stmt|;
name|String
name|password
init|=
name|username
operator|!=
literal|null
operator|&&
name|randomBoolean
argument_list|()
condition|?
name|randomAlphaOfLength
argument_list|(
literal|5
argument_list|)
else|:
literal|null
decl_stmt|;
name|int
name|headersCount
init|=
name|randomBoolean
argument_list|()
condition|?
literal|0
else|:
name|between
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
decl_stmt|;
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
argument_list|(
name|headersCount
argument_list|)
decl_stmt|;
while|while
condition|(
name|headers
operator|.
name|size
argument_list|()
operator|<
name|headersCount
condition|)
block|{
name|headers
operator|.
name|put
argument_list|(
name|randomAlphaOfLength
argument_list|(
literal|5
argument_list|)
argument_list|,
name|randomAlphaOfLength
argument_list|(
literal|5
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|TimeValue
name|socketTimeout
init|=
name|parseTimeValue
argument_list|(
name|randomPositiveTimeValue
argument_list|()
argument_list|,
literal|"socketTimeout"
argument_list|)
decl_stmt|;
name|TimeValue
name|connectTimeout
init|=
name|parseTimeValue
argument_list|(
name|randomPositiveTimeValue
argument_list|()
argument_list|,
literal|"connectTimeout"
argument_list|)
decl_stmt|;
name|reindex
operator|.
name|setRemoteInfo
argument_list|(
operator|new
name|RemoteInfo
argument_list|(
name|randomAlphaOfLength
argument_list|(
literal|5
argument_list|)
argument_list|,
name|randomAlphaOfLength
argument_list|(
literal|5
argument_list|)
argument_list|,
name|port
argument_list|,
name|query
argument_list|,
name|username
argument_list|,
name|password
argument_list|,
name|headers
argument_list|,
name|socketTimeout
argument_list|,
name|connectTimeout
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|ReindexRequest
name|tripped
init|=
operator|new
name|ReindexRequest
argument_list|()
decl_stmt|;
name|roundTrip
argument_list|(
name|reindex
argument_list|,
name|tripped
argument_list|)
expr_stmt|;
name|assertRequestEquals
argument_list|(
name|reindex
argument_list|,
name|tripped
argument_list|)
expr_stmt|;
comment|// Try slices with a version that doesn't support slices. That should fail.
name|reindex
operator|.
name|setSlices
argument_list|(
name|between
argument_list|(
literal|2
argument_list|,
literal|1000
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
name|roundTrip
argument_list|(
name|Version
operator|.
name|V_5_0_0_rc1
argument_list|,
name|reindex
argument_list|,
literal|null
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"Attempting to send sliced reindex-style request to a node that doesn't support it. "
operator|+
literal|"Version is [5.0.0-rc1] but must be [5.1.1]"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
comment|// Try without slices with a version that doesn't support slices. That should work.
name|tripped
operator|=
operator|new
name|ReindexRequest
argument_list|()
expr_stmt|;
name|reindex
operator|.
name|setSlices
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|roundTrip
argument_list|(
name|Version
operator|.
name|V_5_0_0_rc1
argument_list|,
name|reindex
argument_list|,
name|tripped
argument_list|)
expr_stmt|;
name|assertRequestEquals
argument_list|(
name|Version
operator|.
name|V_5_0_0_rc1
argument_list|,
name|reindex
argument_list|,
name|tripped
argument_list|)
expr_stmt|;
block|}
DECL|method|testUpdateByQueryRequest
specifier|public
name|void
name|testUpdateByQueryRequest
parameter_list|()
throws|throws
name|IOException
block|{
name|UpdateByQueryRequest
name|update
init|=
operator|new
name|UpdateByQueryRequest
argument_list|(
operator|new
name|SearchRequest
argument_list|()
argument_list|)
decl_stmt|;
name|randomRequest
argument_list|(
name|update
argument_list|)
expr_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|update
operator|.
name|setPipeline
argument_list|(
name|randomAlphaOfLength
argument_list|(
literal|5
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|UpdateByQueryRequest
name|tripped
init|=
operator|new
name|UpdateByQueryRequest
argument_list|()
decl_stmt|;
name|roundTrip
argument_list|(
name|update
argument_list|,
name|tripped
argument_list|)
expr_stmt|;
name|assertRequestEquals
argument_list|(
name|update
argument_list|,
name|tripped
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|update
operator|.
name|getPipeline
argument_list|()
argument_list|,
name|tripped
operator|.
name|getPipeline
argument_list|()
argument_list|)
expr_stmt|;
comment|// Try slices with a version that doesn't support slices. That should fail.
name|update
operator|.
name|setSlices
argument_list|(
name|between
argument_list|(
literal|2
argument_list|,
literal|1000
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
name|roundTrip
argument_list|(
name|Version
operator|.
name|V_5_0_0_rc1
argument_list|,
name|update
argument_list|,
literal|null
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"Attempting to send sliced reindex-style request to a node that doesn't support it. "
operator|+
literal|"Version is [5.0.0-rc1] but must be [5.1.1]"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
comment|// Try without slices with a version that doesn't support slices. That should work.
name|tripped
operator|=
operator|new
name|UpdateByQueryRequest
argument_list|()
expr_stmt|;
name|update
operator|.
name|setSlices
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|roundTrip
argument_list|(
name|Version
operator|.
name|V_5_0_0_rc1
argument_list|,
name|update
argument_list|,
name|tripped
argument_list|)
expr_stmt|;
name|assertRequestEquals
argument_list|(
name|update
argument_list|,
name|tripped
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|update
operator|.
name|getPipeline
argument_list|()
argument_list|,
name|tripped
operator|.
name|getPipeline
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testDeleteByQueryRequest
specifier|public
name|void
name|testDeleteByQueryRequest
parameter_list|()
throws|throws
name|IOException
block|{
name|DeleteByQueryRequest
name|delete
init|=
operator|new
name|DeleteByQueryRequest
argument_list|(
operator|new
name|SearchRequest
argument_list|()
argument_list|)
decl_stmt|;
name|randomRequest
argument_list|(
name|delete
argument_list|)
expr_stmt|;
name|DeleteByQueryRequest
name|tripped
init|=
operator|new
name|DeleteByQueryRequest
argument_list|()
decl_stmt|;
name|roundTrip
argument_list|(
name|delete
argument_list|,
name|tripped
argument_list|)
expr_stmt|;
name|assertRequestEquals
argument_list|(
name|delete
argument_list|,
name|tripped
argument_list|)
expr_stmt|;
comment|// Try slices with a version that doesn't support slices. That should fail.
name|delete
operator|.
name|setSlices
argument_list|(
name|between
argument_list|(
literal|2
argument_list|,
literal|1000
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
name|roundTrip
argument_list|(
name|Version
operator|.
name|V_5_0_0_rc1
argument_list|,
name|delete
argument_list|,
literal|null
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"Attempting to send sliced reindex-style request to a node that doesn't support it. "
operator|+
literal|"Version is [5.0.0-rc1] but must be [5.1.1]"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
comment|// Try without slices with a version that doesn't support slices. That should work.
name|tripped
operator|=
operator|new
name|DeleteByQueryRequest
argument_list|()
expr_stmt|;
name|delete
operator|.
name|setSlices
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|roundTrip
argument_list|(
name|Version
operator|.
name|V_5_0_0_rc1
argument_list|,
name|delete
argument_list|,
name|tripped
argument_list|)
expr_stmt|;
name|assertRequestEquals
argument_list|(
name|delete
argument_list|,
name|tripped
argument_list|)
expr_stmt|;
block|}
DECL|method|randomRequest
specifier|private
name|void
name|randomRequest
parameter_list|(
name|AbstractBulkByScrollRequest
argument_list|<
name|?
argument_list|>
name|request
parameter_list|)
block|{
name|request
operator|.
name|getSearchRequest
argument_list|()
operator|.
name|indices
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
name|request
operator|.
name|getSearchRequest
argument_list|()
operator|.
name|source
argument_list|()
operator|.
name|size
argument_list|(
name|between
argument_list|(
literal|1
argument_list|,
literal|1000
argument_list|)
argument_list|)
expr_stmt|;
name|request
operator|.
name|setSize
argument_list|(
name|random
argument_list|()
operator|.
name|nextBoolean
argument_list|()
condition|?
name|between
argument_list|(
literal|1
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
else|:
operator|-
literal|1
argument_list|)
expr_stmt|;
name|request
operator|.
name|setAbortOnVersionConflict
argument_list|(
name|random
argument_list|()
operator|.
name|nextBoolean
argument_list|()
argument_list|)
expr_stmt|;
name|request
operator|.
name|setRefresh
argument_list|(
name|rarely
argument_list|()
argument_list|)
expr_stmt|;
name|request
operator|.
name|setTimeout
argument_list|(
name|TimeValue
operator|.
name|parseTimeValue
argument_list|(
name|randomTimeValue
argument_list|()
argument_list|,
literal|null
argument_list|,
literal|"test"
argument_list|)
argument_list|)
expr_stmt|;
name|request
operator|.
name|setWaitForActiveShards
argument_list|(
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|10
argument_list|)
argument_list|)
expr_stmt|;
name|request
operator|.
name|setRequestsPerSecond
argument_list|(
name|between
argument_list|(
literal|0
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
argument_list|)
expr_stmt|;
name|request
operator|.
name|setSlices
argument_list|(
name|between
argument_list|(
literal|1
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|randomRequest
specifier|private
name|void
name|randomRequest
parameter_list|(
name|AbstractBulkIndexByScrollRequest
argument_list|<
name|?
argument_list|>
name|request
parameter_list|)
block|{
name|randomRequest
argument_list|(
operator|(
name|AbstractBulkByScrollRequest
argument_list|<
name|?
argument_list|>
operator|)
name|request
argument_list|)
expr_stmt|;
name|request
operator|.
name|setScript
argument_list|(
name|random
argument_list|()
operator|.
name|nextBoolean
argument_list|()
condition|?
literal|null
else|:
name|randomScript
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|assertRequestEquals
specifier|private
name|void
name|assertRequestEquals
parameter_list|(
name|Version
name|version
parameter_list|,
name|ReindexRequest
name|request
parameter_list|,
name|ReindexRequest
name|tripped
parameter_list|)
block|{
name|assertRequestEquals
argument_list|(
operator|(
name|AbstractBulkIndexByScrollRequest
argument_list|<
name|?
argument_list|>
operator|)
name|request
argument_list|,
operator|(
name|AbstractBulkIndexByScrollRequest
argument_list|<
name|?
argument_list|>
operator|)
name|tripped
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|request
operator|.
name|getDestination
argument_list|()
operator|.
name|version
argument_list|()
argument_list|,
name|tripped
operator|.
name|getDestination
argument_list|()
operator|.
name|version
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|request
operator|.
name|getDestination
argument_list|()
operator|.
name|index
argument_list|()
argument_list|,
name|tripped
operator|.
name|getDestination
argument_list|()
operator|.
name|index
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|request
operator|.
name|getRemoteInfo
argument_list|()
operator|==
literal|null
condition|)
block|{
name|assertNull
argument_list|(
name|tripped
operator|.
name|getRemoteInfo
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertNotNull
argument_list|(
name|tripped
operator|.
name|getRemoteInfo
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|request
operator|.
name|getRemoteInfo
argument_list|()
operator|.
name|getScheme
argument_list|()
argument_list|,
name|tripped
operator|.
name|getRemoteInfo
argument_list|()
operator|.
name|getScheme
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|request
operator|.
name|getRemoteInfo
argument_list|()
operator|.
name|getHost
argument_list|()
argument_list|,
name|tripped
operator|.
name|getRemoteInfo
argument_list|()
operator|.
name|getHost
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|request
operator|.
name|getRemoteInfo
argument_list|()
operator|.
name|getQuery
argument_list|()
argument_list|,
name|tripped
operator|.
name|getRemoteInfo
argument_list|()
operator|.
name|getQuery
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|request
operator|.
name|getRemoteInfo
argument_list|()
operator|.
name|getUsername
argument_list|()
argument_list|,
name|tripped
operator|.
name|getRemoteInfo
argument_list|()
operator|.
name|getUsername
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|request
operator|.
name|getRemoteInfo
argument_list|()
operator|.
name|getPassword
argument_list|()
argument_list|,
name|tripped
operator|.
name|getRemoteInfo
argument_list|()
operator|.
name|getPassword
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|request
operator|.
name|getRemoteInfo
argument_list|()
operator|.
name|getHeaders
argument_list|()
argument_list|,
name|tripped
operator|.
name|getRemoteInfo
argument_list|()
operator|.
name|getHeaders
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|version
operator|.
name|onOrAfter
argument_list|(
name|Version
operator|.
name|V_5_2_0
argument_list|)
condition|)
block|{
name|assertEquals
argument_list|(
name|request
operator|.
name|getRemoteInfo
argument_list|()
operator|.
name|getSocketTimeout
argument_list|()
argument_list|,
name|tripped
operator|.
name|getRemoteInfo
argument_list|()
operator|.
name|getSocketTimeout
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|request
operator|.
name|getRemoteInfo
argument_list|()
operator|.
name|getConnectTimeout
argument_list|()
argument_list|,
name|tripped
operator|.
name|getRemoteInfo
argument_list|()
operator|.
name|getConnectTimeout
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertEquals
argument_list|(
name|RemoteInfo
operator|.
name|DEFAULT_SOCKET_TIMEOUT
argument_list|,
name|tripped
operator|.
name|getRemoteInfo
argument_list|()
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
name|tripped
operator|.
name|getRemoteInfo
argument_list|()
operator|.
name|getConnectTimeout
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|assertRequestEquals
specifier|private
name|void
name|assertRequestEquals
parameter_list|(
name|AbstractBulkIndexByScrollRequest
argument_list|<
name|?
argument_list|>
name|request
parameter_list|,
name|AbstractBulkIndexByScrollRequest
argument_list|<
name|?
argument_list|>
name|tripped
parameter_list|)
block|{
name|assertRequestEquals
argument_list|(
operator|(
name|AbstractBulkByScrollRequest
argument_list|<
name|?
argument_list|>
operator|)
name|request
argument_list|,
operator|(
name|AbstractBulkByScrollRequest
argument_list|<
name|?
argument_list|>
operator|)
name|tripped
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|request
operator|.
name|getScript
argument_list|()
argument_list|,
name|tripped
operator|.
name|getScript
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|assertRequestEquals
specifier|private
name|void
name|assertRequestEquals
parameter_list|(
name|AbstractBulkByScrollRequest
argument_list|<
name|?
argument_list|>
name|request
parameter_list|,
name|AbstractBulkByScrollRequest
argument_list|<
name|?
argument_list|>
name|tripped
parameter_list|)
block|{
name|assertArrayEquals
argument_list|(
name|request
operator|.
name|getSearchRequest
argument_list|()
operator|.
name|indices
argument_list|()
argument_list|,
name|tripped
operator|.
name|getSearchRequest
argument_list|()
operator|.
name|indices
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|request
operator|.
name|getSearchRequest
argument_list|()
operator|.
name|source
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|tripped
operator|.
name|getSearchRequest
argument_list|()
operator|.
name|source
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|request
operator|.
name|isAbortOnVersionConflict
argument_list|()
argument_list|,
name|tripped
operator|.
name|isAbortOnVersionConflict
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|request
operator|.
name|isRefresh
argument_list|()
argument_list|,
name|tripped
operator|.
name|isRefresh
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|request
operator|.
name|getTimeout
argument_list|()
argument_list|,
name|tripped
operator|.
name|getTimeout
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|request
operator|.
name|getWaitForActiveShards
argument_list|()
argument_list|,
name|tripped
operator|.
name|getWaitForActiveShards
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|request
operator|.
name|getRetryBackoffInitialTime
argument_list|()
argument_list|,
name|tripped
operator|.
name|getRetryBackoffInitialTime
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|request
operator|.
name|getMaxRetries
argument_list|()
argument_list|,
name|tripped
operator|.
name|getMaxRetries
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|request
operator|.
name|getRequestsPerSecond
argument_list|()
argument_list|,
name|tripped
operator|.
name|getRequestsPerSecond
argument_list|()
argument_list|,
literal|0d
argument_list|)
expr_stmt|;
block|}
DECL|method|testRethrottleRequest
specifier|public
name|void
name|testRethrottleRequest
parameter_list|()
throws|throws
name|IOException
block|{
name|RethrottleRequest
name|request
init|=
operator|new
name|RethrottleRequest
argument_list|()
decl_stmt|;
name|request
operator|.
name|setRequestsPerSecond
argument_list|(
operator|(
name|float
operator|)
name|randomDoubleBetween
argument_list|(
literal|0
argument_list|,
name|Float
operator|.
name|POSITIVE_INFINITY
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|request
operator|.
name|setActions
argument_list|(
name|randomFrom
argument_list|(
name|UpdateByQueryAction
operator|.
name|NAME
argument_list|,
name|ReindexAction
operator|.
name|NAME
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|request
operator|.
name|setTaskId
argument_list|(
operator|new
name|TaskId
argument_list|(
name|randomAlphaOfLength
argument_list|(
literal|5
argument_list|)
argument_list|,
name|randomLong
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|RethrottleRequest
name|tripped
init|=
operator|new
name|RethrottleRequest
argument_list|()
decl_stmt|;
name|roundTrip
argument_list|(
name|request
argument_list|,
name|tripped
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|request
operator|.
name|getRequestsPerSecond
argument_list|()
argument_list|,
name|tripped
operator|.
name|getRequestsPerSecond
argument_list|()
argument_list|,
literal|0.00001
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
name|request
operator|.
name|getActions
argument_list|()
argument_list|,
name|tripped
operator|.
name|getActions
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|request
operator|.
name|getTaskId
argument_list|()
argument_list|,
name|tripped
operator|.
name|getTaskId
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|roundTrip
specifier|private
name|void
name|roundTrip
parameter_list|(
name|Streamable
name|example
parameter_list|,
name|Streamable
name|empty
parameter_list|)
throws|throws
name|IOException
block|{
name|roundTrip
argument_list|(
name|Version
operator|.
name|CURRENT
argument_list|,
name|example
argument_list|,
name|empty
argument_list|)
expr_stmt|;
block|}
DECL|method|roundTrip
specifier|private
name|void
name|roundTrip
parameter_list|(
name|Version
name|version
parameter_list|,
name|Streamable
name|example
parameter_list|,
name|Streamable
name|empty
parameter_list|)
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
name|out
operator|.
name|setVersion
argument_list|(
name|version
argument_list|)
expr_stmt|;
name|example
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|StreamInput
name|in
init|=
name|out
operator|.
name|bytes
argument_list|()
operator|.
name|streamInput
argument_list|()
decl_stmt|;
name|in
operator|.
name|setVersion
argument_list|(
name|version
argument_list|)
expr_stmt|;
name|empty
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
DECL|method|randomScript
specifier|private
name|Script
name|randomScript
parameter_list|()
block|{
name|ScriptType
name|type
init|=
name|randomFrom
argument_list|(
name|ScriptType
operator|.
name|values
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|lang
init|=
name|random
argument_list|()
operator|.
name|nextBoolean
argument_list|()
condition|?
name|Script
operator|.
name|DEFAULT_SCRIPT_LANG
else|:
name|randomSimpleString
argument_list|(
name|random
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|idOrCode
init|=
name|randomSimpleString
argument_list|(
name|random
argument_list|()
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|params
init|=
name|Collections
operator|.
name|emptyMap
argument_list|()
decl_stmt|;
return|return
operator|new
name|Script
argument_list|(
name|type
argument_list|,
name|lang
argument_list|,
name|idOrCode
argument_list|,
name|params
argument_list|)
return|;
block|}
block|}
end_class

end_unit

