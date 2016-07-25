begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.bulk
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|bulk
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
name|WriteRequest
operator|.
name|RefreshPolicy
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

begin_class
DECL|class|BulkShardRequestTests
specifier|public
class|class
name|BulkShardRequestTests
extends|extends
name|ESTestCase
block|{
DECL|method|testToString
specifier|public
name|void
name|testToString
parameter_list|()
block|{
name|String
name|index
init|=
name|randomSimpleString
argument_list|(
name|random
argument_list|()
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|int
name|count
init|=
name|between
argument_list|(
literal|1
argument_list|,
literal|100
argument_list|)
decl_stmt|;
name|BulkShardRequest
name|r
init|=
operator|new
name|BulkShardRequest
argument_list|(
operator|new
name|ShardId
argument_list|(
name|index
argument_list|,
literal|"ignored"
argument_list|,
literal|0
argument_list|)
argument_list|,
name|RefreshPolicy
operator|.
name|NONE
argument_list|,
operator|new
name|BulkItemRequest
index|[
name|count
index|]
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"BulkShardRequest to ["
operator|+
name|index
operator|+
literal|"] containing ["
operator|+
name|count
operator|+
literal|"] requests"
argument_list|,
name|r
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|r
operator|=
operator|new
name|BulkShardRequest
argument_list|(
operator|new
name|ShardId
argument_list|(
name|index
argument_list|,
literal|"ignored"
argument_list|,
literal|0
argument_list|)
argument_list|,
name|RefreshPolicy
operator|.
name|IMMEDIATE
argument_list|,
operator|new
name|BulkItemRequest
index|[
name|count
index|]
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"BulkShardRequest to ["
operator|+
name|index
operator|+
literal|"] containing ["
operator|+
name|count
operator|+
literal|"] requests and a refresh"
argument_list|,
name|r
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|r
operator|=
operator|new
name|BulkShardRequest
argument_list|(
operator|new
name|ShardId
argument_list|(
name|index
argument_list|,
literal|"ignored"
argument_list|,
literal|0
argument_list|)
argument_list|,
name|RefreshPolicy
operator|.
name|WAIT_UNTIL
argument_list|,
operator|new
name|BulkItemRequest
index|[
name|count
index|]
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"BulkShardRequest to ["
operator|+
name|index
operator|+
literal|"] containing ["
operator|+
name|count
operator|+
literal|"] requests blocking until refresh"
argument_list|,
name|r
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

