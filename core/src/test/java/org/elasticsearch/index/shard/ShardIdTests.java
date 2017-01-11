begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.shard
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|shard
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|metadata
operator|.
name|IndexMetaData
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

begin_class
DECL|class|ShardIdTests
specifier|public
class|class
name|ShardIdTests
extends|extends
name|ESTestCase
block|{
DECL|method|testShardIdFromString
specifier|public
name|void
name|testShardIdFromString
parameter_list|()
block|{
name|String
name|indexName
init|=
name|randomAsciiOfLengthBetween
argument_list|(
literal|3
argument_list|,
literal|50
argument_list|)
decl_stmt|;
name|int
name|shardId
init|=
name|randomInt
argument_list|()
decl_stmt|;
name|ShardId
name|id
init|=
name|ShardId
operator|.
name|fromString
argument_list|(
literal|"["
operator|+
name|indexName
operator|+
literal|"]["
operator|+
name|shardId
operator|+
literal|"]"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|indexName
argument_list|,
name|id
operator|.
name|getIndexName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|shardId
argument_list|,
name|id
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|indexName
argument_list|,
name|id
operator|.
name|getIndex
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|IndexMetaData
operator|.
name|INDEX_UUID_NA_VALUE
argument_list|,
name|id
operator|.
name|getIndex
argument_list|()
operator|.
name|getUUID
argument_list|()
argument_list|)
expr_stmt|;
name|id
operator|=
name|ShardId
operator|.
name|fromString
argument_list|(
literal|"[some]weird[0]Name][-125]"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"some]weird[0]Name"
argument_list|,
name|id
operator|.
name|getIndexName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|-
literal|125
argument_list|,
name|id
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"some]weird[0]Name"
argument_list|,
name|id
operator|.
name|getIndex
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|IndexMetaData
operator|.
name|INDEX_UUID_NA_VALUE
argument_list|,
name|id
operator|.
name|getIndex
argument_list|()
operator|.
name|getUUID
argument_list|()
argument_list|)
expr_stmt|;
name|String
name|badId
init|=
name|indexName
operator|+
literal|","
operator|+
name|shardId
decl_stmt|;
comment|// missing separator
name|IllegalArgumentException
name|ex
init|=
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|ShardId
operator|.
name|fromString
argument_list|(
name|badId
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"Unexpected shardId string format, expected [indexName][shardId] but got "
operator|+
name|badId
argument_list|,
name|ex
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|String
name|badId2
init|=
name|indexName
operator|+
literal|"]["
operator|+
name|shardId
operator|+
literal|"]"
decl_stmt|;
comment|// missing opening bracket
name|ex
operator|=
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|ShardId
operator|.
name|fromString
argument_list|(
name|badId2
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|badId3
init|=
literal|"["
operator|+
name|indexName
operator|+
literal|"]["
operator|+
name|shardId
decl_stmt|;
comment|// missing closing bracket
name|ex
operator|=
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|ShardId
operator|.
name|fromString
argument_list|(
name|badId3
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

