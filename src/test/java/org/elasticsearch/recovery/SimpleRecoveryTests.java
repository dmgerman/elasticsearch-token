begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.recovery
package|package
name|org
operator|.
name|elasticsearch
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
name|action
operator|.
name|admin
operator|.
name|indices
operator|.
name|flush
operator|.
name|FlushResponse
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
name|admin
operator|.
name|indices
operator|.
name|refresh
operator|.
name|RefreshResponse
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
name|settings
operator|.
name|ImmutableSettings
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
name|settings
operator|.
name|Settings
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
name|elasticsearch
operator|.
name|client
operator|.
name|Requests
operator|.
name|*
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
comment|/**  *  */
end_comment

begin_class
DECL|class|SimpleRecoveryTests
specifier|public
class|class
name|SimpleRecoveryTests
extends|extends
name|ElasticsearchIntegrationTest
block|{
annotation|@
name|Override
DECL|method|indexSettings
specifier|public
name|Settings
name|indexSettings
parameter_list|()
block|{
return|return
name|recoverySettings
argument_list|()
return|;
block|}
DECL|method|recoverySettings
specifier|protected
name|Settings
name|recoverySettings
parameter_list|()
block|{
return|return
name|ImmutableSettings
operator|.
name|Builder
operator|.
name|EMPTY_SETTINGS
return|;
block|}
annotation|@
name|Test
DECL|method|testSimpleRecovery
specifier|public
name|void
name|testSimpleRecovery
parameter_list|()
throws|throws
name|Exception
block|{
name|prepareCreate
argument_list|(
literal|"test"
argument_list|,
literal|1
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|(
literal|5000
argument_list|)
expr_stmt|;
name|NumShards
name|numShards
init|=
name|getNumShards
argument_list|(
literal|"test"
argument_list|)
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"Running Cluster Health"
argument_list|)
expr_stmt|;
name|ensureYellow
argument_list|()
expr_stmt|;
name|client
argument_list|()
operator|.
name|index
argument_list|(
name|indexRequest
argument_list|(
literal|"test"
argument_list|)
operator|.
name|type
argument_list|(
literal|"type1"
argument_list|)
operator|.
name|id
argument_list|(
literal|"1"
argument_list|)
operator|.
name|source
argument_list|(
name|source
argument_list|(
literal|"1"
argument_list|,
literal|"test"
argument_list|)
argument_list|)
argument_list|)
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|FlushResponse
name|flushResponse
init|=
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|flush
argument_list|(
name|flushRequest
argument_list|(
literal|"test"
argument_list|)
argument_list|)
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|flushResponse
operator|.
name|getTotalShards
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|numShards
operator|.
name|totalNumShards
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|flushResponse
operator|.
name|getSuccessfulShards
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|numShards
operator|.
name|numPrimaries
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|flushResponse
operator|.
name|getFailedShards
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|client
argument_list|()
operator|.
name|index
argument_list|(
name|indexRequest
argument_list|(
literal|"test"
argument_list|)
operator|.
name|type
argument_list|(
literal|"type1"
argument_list|)
operator|.
name|id
argument_list|(
literal|"2"
argument_list|)
operator|.
name|source
argument_list|(
name|source
argument_list|(
literal|"2"
argument_list|,
literal|"test"
argument_list|)
argument_list|)
argument_list|)
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|RefreshResponse
name|refreshResponse
init|=
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|refresh
argument_list|(
name|refreshRequest
argument_list|(
literal|"test"
argument_list|)
argument_list|)
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|refreshResponse
operator|.
name|getTotalShards
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|numShards
operator|.
name|totalNumShards
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|refreshResponse
operator|.
name|getSuccessfulShards
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|numShards
operator|.
name|numPrimaries
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|refreshResponse
operator|.
name|getFailedShards
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|allowNodes
argument_list|(
literal|"test"
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"Running Cluster Health"
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
name|GetResponse
name|getResult
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
literal|5
condition|;
name|i
operator|++
control|)
block|{
name|getResult
operator|=
name|client
argument_list|()
operator|.
name|get
argument_list|(
name|getRequest
argument_list|(
literal|"test"
argument_list|)
operator|.
name|type
argument_list|(
literal|"type1"
argument_list|)
operator|.
name|id
argument_list|(
literal|"1"
argument_list|)
operator|.
name|operationThreaded
argument_list|(
literal|false
argument_list|)
argument_list|)
operator|.
name|actionGet
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|getResult
operator|.
name|getSourceAsString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|source
argument_list|(
literal|"1"
argument_list|,
literal|"test"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|getResult
operator|=
name|client
argument_list|()
operator|.
name|get
argument_list|(
name|getRequest
argument_list|(
literal|"test"
argument_list|)
operator|.
name|type
argument_list|(
literal|"type1"
argument_list|)
operator|.
name|id
argument_list|(
literal|"1"
argument_list|)
operator|.
name|operationThreaded
argument_list|(
literal|false
argument_list|)
argument_list|)
operator|.
name|actionGet
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|getResult
operator|.
name|getSourceAsString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|source
argument_list|(
literal|"1"
argument_list|,
literal|"test"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|getResult
operator|=
name|client
argument_list|()
operator|.
name|get
argument_list|(
name|getRequest
argument_list|(
literal|"test"
argument_list|)
operator|.
name|type
argument_list|(
literal|"type1"
argument_list|)
operator|.
name|id
argument_list|(
literal|"2"
argument_list|)
operator|.
name|operationThreaded
argument_list|(
literal|true
argument_list|)
argument_list|)
operator|.
name|actionGet
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|getResult
operator|.
name|getSourceAsString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|source
argument_list|(
literal|"2"
argument_list|,
literal|"test"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|getResult
operator|=
name|client
argument_list|()
operator|.
name|get
argument_list|(
name|getRequest
argument_list|(
literal|"test"
argument_list|)
operator|.
name|type
argument_list|(
literal|"type1"
argument_list|)
operator|.
name|id
argument_list|(
literal|"2"
argument_list|)
operator|.
name|operationThreaded
argument_list|(
literal|true
argument_list|)
argument_list|)
operator|.
name|actionGet
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|getResult
operator|.
name|getSourceAsString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|source
argument_list|(
literal|"2"
argument_list|,
literal|"test"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// now start another one so we move some primaries
name|allowNodes
argument_list|(
literal|"test"
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|200
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"Running Cluster Health"
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|()
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
literal|5
condition|;
name|i
operator|++
control|)
block|{
name|getResult
operator|=
name|client
argument_list|()
operator|.
name|get
argument_list|(
name|getRequest
argument_list|(
literal|"test"
argument_list|)
operator|.
name|type
argument_list|(
literal|"type1"
argument_list|)
operator|.
name|id
argument_list|(
literal|"1"
argument_list|)
argument_list|)
operator|.
name|actionGet
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|getResult
operator|.
name|getSourceAsString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|source
argument_list|(
literal|"1"
argument_list|,
literal|"test"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|getResult
operator|=
name|client
argument_list|()
operator|.
name|get
argument_list|(
name|getRequest
argument_list|(
literal|"test"
argument_list|)
operator|.
name|type
argument_list|(
literal|"type1"
argument_list|)
operator|.
name|id
argument_list|(
literal|"1"
argument_list|)
argument_list|)
operator|.
name|actionGet
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|getResult
operator|.
name|getSourceAsString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|source
argument_list|(
literal|"1"
argument_list|,
literal|"test"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|getResult
operator|=
name|client
argument_list|()
operator|.
name|get
argument_list|(
name|getRequest
argument_list|(
literal|"test"
argument_list|)
operator|.
name|type
argument_list|(
literal|"type1"
argument_list|)
operator|.
name|id
argument_list|(
literal|"1"
argument_list|)
argument_list|)
operator|.
name|actionGet
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|getResult
operator|.
name|getSourceAsString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|source
argument_list|(
literal|"1"
argument_list|,
literal|"test"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|getResult
operator|=
name|client
argument_list|()
operator|.
name|get
argument_list|(
name|getRequest
argument_list|(
literal|"test"
argument_list|)
operator|.
name|type
argument_list|(
literal|"type1"
argument_list|)
operator|.
name|id
argument_list|(
literal|"2"
argument_list|)
operator|.
name|operationThreaded
argument_list|(
literal|true
argument_list|)
argument_list|)
operator|.
name|actionGet
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|getResult
operator|.
name|getSourceAsString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|source
argument_list|(
literal|"2"
argument_list|,
literal|"test"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|getResult
operator|=
name|client
argument_list|()
operator|.
name|get
argument_list|(
name|getRequest
argument_list|(
literal|"test"
argument_list|)
operator|.
name|type
argument_list|(
literal|"type1"
argument_list|)
operator|.
name|id
argument_list|(
literal|"2"
argument_list|)
operator|.
name|operationThreaded
argument_list|(
literal|true
argument_list|)
argument_list|)
operator|.
name|actionGet
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|getResult
operator|.
name|getSourceAsString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|source
argument_list|(
literal|"2"
argument_list|,
literal|"test"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|getResult
operator|=
name|client
argument_list|()
operator|.
name|get
argument_list|(
name|getRequest
argument_list|(
literal|"test"
argument_list|)
operator|.
name|type
argument_list|(
literal|"type1"
argument_list|)
operator|.
name|id
argument_list|(
literal|"2"
argument_list|)
operator|.
name|operationThreaded
argument_list|(
literal|true
argument_list|)
argument_list|)
operator|.
name|actionGet
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|getResult
operator|.
name|getSourceAsString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|source
argument_list|(
literal|"2"
argument_list|,
literal|"test"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|source
specifier|private
name|String
name|source
parameter_list|(
name|String
name|id
parameter_list|,
name|String
name|nameValue
parameter_list|)
block|{
return|return
literal|"{ type1 : { \"id\" : \""
operator|+
name|id
operator|+
literal|"\", \"name\" : \""
operator|+
name|nameValue
operator|+
literal|"\" } }"
return|;
block|}
block|}
end_class

end_unit

