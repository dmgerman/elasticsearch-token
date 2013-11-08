begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.timestamp
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|timestamp
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
name|get
operator|.
name|GetResponse
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
name|Priority
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
name|test
operator|.
name|ElasticsearchIntegrationTest
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
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|SimpleTimestampTests
specifier|public
class|class
name|SimpleTimestampTests
extends|extends
name|ElasticsearchIntegrationTest
block|{
annotation|@
name|Test
DECL|method|testSimpleTimestamp
specifier|public
name|void
name|testSimpleTimestamp
parameter_list|()
throws|throws
name|Exception
block|{
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareCreate
argument_list|(
literal|"test"
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"type1"
argument_list|,
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"type1"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"_timestamp"
argument_list|)
operator|.
name|field
argument_list|(
literal|"enabled"
argument_list|,
literal|true
argument_list|)
operator|.
name|field
argument_list|(
literal|"store"
argument_list|,
literal|"yes"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareHealth
argument_list|()
operator|.
name|setWaitForEvents
argument_list|(
name|Priority
operator|.
name|LANGUID
argument_list|)
operator|.
name|setWaitForGreenStatus
argument_list|()
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> check with automatic timestamp"
argument_list|)
expr_stmt|;
name|long
name|now1
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"field1"
argument_list|,
literal|"value1"
argument_list|)
operator|.
name|setRefresh
argument_list|(
literal|true
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|long
name|now2
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
comment|// we check both realtime get and non realtime get
name|GetResponse
name|getResponse
init|=
name|client
argument_list|()
operator|.
name|prepareGet
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setFields
argument_list|(
literal|"_timestamp"
argument_list|)
operator|.
name|setRealtime
argument_list|(
literal|true
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|long
name|timestamp
init|=
operator|(
operator|(
name|Number
operator|)
name|getResponse
operator|.
name|getField
argument_list|(
literal|"_timestamp"
argument_list|)
operator|.
name|getValue
argument_list|()
operator|)
operator|.
name|longValue
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|timestamp
argument_list|,
name|greaterThanOrEqualTo
argument_list|(
name|now1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|timestamp
argument_list|,
name|lessThanOrEqualTo
argument_list|(
name|now2
argument_list|)
argument_list|)
expr_stmt|;
comment|// verify its the same timestamp when going the replica
name|getResponse
operator|=
name|client
argument_list|()
operator|.
name|prepareGet
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setFields
argument_list|(
literal|"_timestamp"
argument_list|)
operator|.
name|setRealtime
argument_list|(
literal|true
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|Number
operator|)
name|getResponse
operator|.
name|getField
argument_list|(
literal|"_timestamp"
argument_list|)
operator|.
name|getValue
argument_list|()
operator|)
operator|.
name|longValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|timestamp
argument_list|)
argument_list|)
expr_stmt|;
comment|// non realtime get (stored)
name|getResponse
operator|=
name|client
argument_list|()
operator|.
name|prepareGet
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setFields
argument_list|(
literal|"_timestamp"
argument_list|)
operator|.
name|setRealtime
argument_list|(
literal|false
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|timestamp
operator|=
operator|(
operator|(
name|Number
operator|)
name|getResponse
operator|.
name|getField
argument_list|(
literal|"_timestamp"
argument_list|)
operator|.
name|getValue
argument_list|()
operator|)
operator|.
name|longValue
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|timestamp
argument_list|,
name|greaterThanOrEqualTo
argument_list|(
name|now1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|timestamp
argument_list|,
name|lessThanOrEqualTo
argument_list|(
name|now2
argument_list|)
argument_list|)
expr_stmt|;
comment|// verify its the same timestamp when going the replica
name|getResponse
operator|=
name|client
argument_list|()
operator|.
name|prepareGet
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setFields
argument_list|(
literal|"_timestamp"
argument_list|)
operator|.
name|setRealtime
argument_list|(
literal|false
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|Number
operator|)
name|getResponse
operator|.
name|getField
argument_list|(
literal|"_timestamp"
argument_list|)
operator|.
name|getValue
argument_list|()
operator|)
operator|.
name|longValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|timestamp
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> check with custom timestamp (numeric)"
argument_list|)
expr_stmt|;
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"field1"
argument_list|,
literal|"value1"
argument_list|)
operator|.
name|setTimestamp
argument_list|(
literal|"10"
argument_list|)
operator|.
name|setRefresh
argument_list|(
literal|true
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|getResponse
operator|=
name|client
argument_list|()
operator|.
name|prepareGet
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setFields
argument_list|(
literal|"_timestamp"
argument_list|)
operator|.
name|setRealtime
argument_list|(
literal|false
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|timestamp
operator|=
operator|(
operator|(
name|Number
operator|)
name|getResponse
operator|.
name|getField
argument_list|(
literal|"_timestamp"
argument_list|)
operator|.
name|getValue
argument_list|()
operator|)
operator|.
name|longValue
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|timestamp
argument_list|,
name|equalTo
argument_list|(
literal|10l
argument_list|)
argument_list|)
expr_stmt|;
comment|// verify its the same timestamp when going the replica
name|getResponse
operator|=
name|client
argument_list|()
operator|.
name|prepareGet
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setFields
argument_list|(
literal|"_timestamp"
argument_list|)
operator|.
name|setRealtime
argument_list|(
literal|false
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|Number
operator|)
name|getResponse
operator|.
name|getField
argument_list|(
literal|"_timestamp"
argument_list|)
operator|.
name|getValue
argument_list|()
operator|)
operator|.
name|longValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|timestamp
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> check with custom timestamp (string)"
argument_list|)
expr_stmt|;
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"field1"
argument_list|,
literal|"value1"
argument_list|)
operator|.
name|setTimestamp
argument_list|(
literal|"1970-01-01T00:00:00.020"
argument_list|)
operator|.
name|setRefresh
argument_list|(
literal|true
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|getResponse
operator|=
name|client
argument_list|()
operator|.
name|prepareGet
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setFields
argument_list|(
literal|"_timestamp"
argument_list|)
operator|.
name|setRealtime
argument_list|(
literal|false
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|timestamp
operator|=
operator|(
operator|(
name|Number
operator|)
name|getResponse
operator|.
name|getField
argument_list|(
literal|"_timestamp"
argument_list|)
operator|.
name|getValue
argument_list|()
operator|)
operator|.
name|longValue
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|timestamp
argument_list|,
name|equalTo
argument_list|(
literal|20l
argument_list|)
argument_list|)
expr_stmt|;
comment|// verify its the same timestamp when going the replica
name|getResponse
operator|=
name|client
argument_list|()
operator|.
name|prepareGet
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setFields
argument_list|(
literal|"_timestamp"
argument_list|)
operator|.
name|setRealtime
argument_list|(
literal|false
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|Number
operator|)
name|getResponse
operator|.
name|getField
argument_list|(
literal|"_timestamp"
argument_list|)
operator|.
name|getValue
argument_list|()
operator|)
operator|.
name|longValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|timestamp
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

