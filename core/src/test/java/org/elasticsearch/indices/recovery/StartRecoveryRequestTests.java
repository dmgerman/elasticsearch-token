begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.indices.recovery
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|recovery
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
name|cluster
operator|.
name|node
operator|.
name|DiscoveryNode
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
name|InputStreamStreamInput
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
name|OutputStreamStreamOutput
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
name|LocalTransportAddress
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
name|index
operator|.
name|store
operator|.
name|Store
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
name|ByteArrayInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayOutputStream
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|VersionUtils
operator|.
name|randomVersion
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

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|StartRecoveryRequestTests
specifier|public
class|class
name|StartRecoveryRequestTests
extends|extends
name|ESTestCase
block|{
DECL|method|testSerialization
specifier|public
name|void
name|testSerialization
parameter_list|()
throws|throws
name|Exception
block|{
name|Version
name|targetNodeVersion
init|=
name|randomVersion
argument_list|(
name|random
argument_list|()
argument_list|)
decl_stmt|;
name|StartRecoveryRequest
name|outRequest
init|=
operator|new
name|StartRecoveryRequest
argument_list|(
operator|new
name|ShardId
argument_list|(
literal|"test"
argument_list|,
literal|"_na_"
argument_list|,
literal|0
argument_list|)
argument_list|,
operator|new
name|DiscoveryNode
argument_list|(
literal|"a"
argument_list|,
operator|new
name|LocalTransportAddress
argument_list|(
literal|"1"
argument_list|)
argument_list|,
name|targetNodeVersion
argument_list|)
argument_list|,
operator|new
name|DiscoveryNode
argument_list|(
literal|"b"
argument_list|,
operator|new
name|LocalTransportAddress
argument_list|(
literal|"1"
argument_list|)
argument_list|,
name|targetNodeVersion
argument_list|)
argument_list|,
literal|true
argument_list|,
name|Store
operator|.
name|MetadataSnapshot
operator|.
name|EMPTY
argument_list|,
name|RecoveryState
operator|.
name|Type
operator|.
name|RELOCATION
argument_list|,
literal|1L
argument_list|)
decl_stmt|;
name|ByteArrayOutputStream
name|outBuffer
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|OutputStreamStreamOutput
name|out
init|=
operator|new
name|OutputStreamStreamOutput
argument_list|(
name|outBuffer
argument_list|)
decl_stmt|;
name|out
operator|.
name|setVersion
argument_list|(
name|targetNodeVersion
argument_list|)
expr_stmt|;
name|outRequest
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|ByteArrayInputStream
name|inBuffer
init|=
operator|new
name|ByteArrayInputStream
argument_list|(
name|outBuffer
operator|.
name|toByteArray
argument_list|()
argument_list|)
decl_stmt|;
name|InputStreamStreamInput
name|in
init|=
operator|new
name|InputStreamStreamInput
argument_list|(
name|inBuffer
argument_list|)
decl_stmt|;
name|in
operator|.
name|setVersion
argument_list|(
name|targetNodeVersion
argument_list|)
expr_stmt|;
name|StartRecoveryRequest
name|inRequest
init|=
operator|new
name|StartRecoveryRequest
argument_list|()
decl_stmt|;
name|inRequest
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|outRequest
operator|.
name|shardId
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|inRequest
operator|.
name|shardId
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|outRequest
operator|.
name|sourceNode
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|inRequest
operator|.
name|sourceNode
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|outRequest
operator|.
name|targetNode
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|inRequest
operator|.
name|targetNode
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|outRequest
operator|.
name|markAsRelocated
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|inRequest
operator|.
name|markAsRelocated
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|outRequest
operator|.
name|metadataSnapshot
argument_list|()
operator|.
name|asMap
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|inRequest
operator|.
name|metadataSnapshot
argument_list|()
operator|.
name|asMap
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|outRequest
operator|.
name|recoveryId
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|inRequest
operator|.
name|recoveryId
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|outRequest
operator|.
name|recoveryType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|inRequest
operator|.
name|recoveryType
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

