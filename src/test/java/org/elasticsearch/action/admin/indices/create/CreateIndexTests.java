begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.indices.create
package|package
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
name|create
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
name|ActionRequestValidationException
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
name|cluster
operator|.
name|state
operator|.
name|ClusterStateResponse
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
name|ClusterState
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
name|cluster
operator|.
name|metadata
operator|.
name|MetaData
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
name|ImmutableOpenMap
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
name|test
operator|.
name|ElasticsearchIntegrationTest
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
operator|.
name|ClusterScope
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
operator|.
name|Scope
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
name|util
operator|.
name|HashMap
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
name|Matchers
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
name|core
operator|.
name|IsNull
operator|.
name|notNullValue
import|;
end_import

begin_class
annotation|@
name|ClusterScope
argument_list|(
name|scope
operator|=
name|Scope
operator|.
name|TEST
argument_list|)
DECL|class|CreateIndexTests
specifier|public
class|class
name|CreateIndexTests
extends|extends
name|ElasticsearchIntegrationTest
block|{
annotation|@
name|Test
DECL|method|testCreationDate_Given
specifier|public
name|void
name|testCreationDate_Given
parameter_list|()
block|{
name|prepareCreate
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setSettings
argument_list|(
name|ImmutableSettings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_CREATION_DATE
argument_list|,
literal|4l
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|ClusterStateResponse
name|response
init|=
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareState
argument_list|()
operator|.
name|get
argument_list|()
decl_stmt|;
name|ClusterState
name|state
init|=
name|response
operator|.
name|getState
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|state
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|MetaData
name|metadata
init|=
name|state
operator|.
name|getMetaData
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|metadata
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|ImmutableOpenMap
argument_list|<
name|String
argument_list|,
name|IndexMetaData
argument_list|>
name|indices
init|=
name|metadata
operator|.
name|getIndices
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|indices
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|indices
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|IndexMetaData
name|index
init|=
name|indices
operator|.
name|get
argument_list|(
literal|"test"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|index
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|index
operator|.
name|creationDate
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|4l
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testCreationDate_Generated
specifier|public
name|void
name|testCreationDate_Generated
parameter_list|()
block|{
name|long
name|timeBeforeRequest
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|prepareCreate
argument_list|(
literal|"test"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|long
name|timeAfterRequest
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|ClusterStateResponse
name|response
init|=
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareState
argument_list|()
operator|.
name|get
argument_list|()
decl_stmt|;
name|ClusterState
name|state
init|=
name|response
operator|.
name|getState
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|state
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|MetaData
name|metadata
init|=
name|state
operator|.
name|getMetaData
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|metadata
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|ImmutableOpenMap
argument_list|<
name|String
argument_list|,
name|IndexMetaData
argument_list|>
name|indices
init|=
name|metadata
operator|.
name|getIndices
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|indices
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|indices
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|IndexMetaData
name|index
init|=
name|indices
operator|.
name|get
argument_list|(
literal|"test"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|index
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|index
operator|.
name|creationDate
argument_list|()
argument_list|,
name|allOf
argument_list|(
name|lessThanOrEqualTo
argument_list|(
name|timeAfterRequest
argument_list|)
argument_list|,
name|greaterThanOrEqualTo
argument_list|(
name|timeBeforeRequest
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testDoubleAddMapping
specifier|public
name|void
name|testDoubleAddMapping
parameter_list|()
throws|throws
name|Exception
block|{
try|try
block|{
name|prepareCreate
argument_list|(
literal|"test"
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"type1"
argument_list|,
literal|"date"
argument_list|,
literal|"type=date"
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"type1"
argument_list|,
literal|"num"
argument_list|,
literal|"type=integer"
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"did not hit expected exception"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalStateException
name|ise
parameter_list|)
block|{
comment|// expected
block|}
try|try
block|{
name|prepareCreate
argument_list|(
literal|"test"
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"type1"
argument_list|,
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|()
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"type1"
argument_list|,
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"did not hit expected exception"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalStateException
name|ise
parameter_list|)
block|{
comment|// expected
block|}
try|try
block|{
name|prepareCreate
argument_list|(
literal|"test"
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"type1"
argument_list|,
name|jsonBuilder
argument_list|()
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"type1"
argument_list|,
name|jsonBuilder
argument_list|()
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"did not hit expected exception"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalStateException
name|ise
parameter_list|)
block|{
comment|// expected
block|}
block|}
annotation|@
name|Test
DECL|method|testInvalidShardCountSettings
specifier|public
name|void
name|testInvalidShardCountSettings
parameter_list|()
throws|throws
name|Exception
block|{
try|try
block|{
name|prepareCreate
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setSettings
argument_list|(
name|ImmutableSettings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_NUMBER_OF_SHARDS
argument_list|,
name|randomIntBetween
argument_list|(
operator|-
literal|10
argument_list|,
literal|0
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|fail
argument_list|(
literal|"should have thrown an exception about the primary shard count"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ActionRequestValidationException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
literal|"message contains error about shard count: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"index must have 1 or more primary shards"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|prepareCreate
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setSettings
argument_list|(
name|ImmutableSettings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_NUMBER_OF_REPLICAS
argument_list|,
name|randomIntBetween
argument_list|(
operator|-
literal|10
argument_list|,
operator|-
literal|1
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|fail
argument_list|(
literal|"should have thrown an exception about the replica shard count"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ActionRequestValidationException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
literal|"message contains error about shard count: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"index must have 0 or more replica shards"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|prepareCreate
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setSettings
argument_list|(
name|ImmutableSettings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_NUMBER_OF_SHARDS
argument_list|,
name|randomIntBetween
argument_list|(
operator|-
literal|10
argument_list|,
literal|0
argument_list|)
argument_list|)
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_NUMBER_OF_REPLICAS
argument_list|,
name|randomIntBetween
argument_list|(
operator|-
literal|10
argument_list|,
operator|-
literal|1
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|fail
argument_list|(
literal|"should have thrown an exception about the shard count"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ActionRequestValidationException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
literal|"message contains error about shard count: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"index must have 1 or more primary shards"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"message contains error about shard count: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"index must have 0 or more replica shards"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

