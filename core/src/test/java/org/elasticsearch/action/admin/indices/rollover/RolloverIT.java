begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.indices.rollover
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
name|rollover
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
name|alias
operator|.
name|Alias
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
name|indices
operator|.
name|IndexAlreadyExistsException
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
name|ESIntegTestCase
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
name|test
operator|.
name|hamcrest
operator|.
name|ElasticsearchAssertions
operator|.
name|assertAcked
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
annotation|@
name|ESIntegTestCase
operator|.
name|ClusterScope
argument_list|(
name|scope
operator|=
name|ESIntegTestCase
operator|.
name|Scope
operator|.
name|TEST
argument_list|)
DECL|class|RolloverIT
specifier|public
class|class
name|RolloverIT
extends|extends
name|ESIntegTestCase
block|{
DECL|method|testRolloverOnEmptyIndex
specifier|public
name|void
name|testRolloverOnEmptyIndex
parameter_list|()
throws|throws
name|Exception
block|{
name|assertAcked
argument_list|(
name|prepareCreate
argument_list|(
literal|"test_index-1"
argument_list|)
operator|.
name|addAlias
argument_list|(
operator|new
name|Alias
argument_list|(
literal|"test_alias"
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
specifier|final
name|RolloverResponse
name|response
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
name|prepareRolloverIndex
argument_list|(
literal|"test_alias"
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getOldIndex
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"test_index-1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getNewIndex
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"test_index-000002"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|isDryRun
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|isRolledOver
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getConditionStatus
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
specifier|final
name|ClusterState
name|state
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
operator|.
name|getState
argument_list|()
decl_stmt|;
specifier|final
name|IndexMetaData
name|oldIndex
init|=
name|state
operator|.
name|metaData
argument_list|()
operator|.
name|index
argument_list|(
literal|"test_index-1"
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
name|oldIndex
operator|.
name|getAliases
argument_list|()
operator|.
name|containsKey
argument_list|(
literal|"test_alias"
argument_list|)
argument_list|)
expr_stmt|;
specifier|final
name|IndexMetaData
name|newIndex
init|=
name|state
operator|.
name|metaData
argument_list|()
operator|.
name|index
argument_list|(
literal|"test_index-000002"
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|newIndex
operator|.
name|getAliases
argument_list|()
operator|.
name|containsKey
argument_list|(
literal|"test_alias"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testRollover
specifier|public
name|void
name|testRollover
parameter_list|()
throws|throws
name|Exception
block|{
name|assertAcked
argument_list|(
name|prepareCreate
argument_list|(
literal|"test_index-2"
argument_list|)
operator|.
name|addAlias
argument_list|(
operator|new
name|Alias
argument_list|(
literal|"test_alias"
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|index
argument_list|(
literal|"test_index-2"
argument_list|,
literal|"type1"
argument_list|,
literal|"1"
argument_list|,
literal|"field"
argument_list|,
literal|"value"
argument_list|)
expr_stmt|;
name|flush
argument_list|(
literal|"test_index-2"
argument_list|)
expr_stmt|;
specifier|final
name|RolloverResponse
name|response
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
name|prepareRolloverIndex
argument_list|(
literal|"test_alias"
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getOldIndex
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"test_index-2"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getNewIndex
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"test_index-000003"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|isDryRun
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|isRolledOver
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getConditionStatus
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
specifier|final
name|ClusterState
name|state
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
operator|.
name|getState
argument_list|()
decl_stmt|;
specifier|final
name|IndexMetaData
name|oldIndex
init|=
name|state
operator|.
name|metaData
argument_list|()
operator|.
name|index
argument_list|(
literal|"test_index-2"
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
name|oldIndex
operator|.
name|getAliases
argument_list|()
operator|.
name|containsKey
argument_list|(
literal|"test_alias"
argument_list|)
argument_list|)
expr_stmt|;
specifier|final
name|IndexMetaData
name|newIndex
init|=
name|state
operator|.
name|metaData
argument_list|()
operator|.
name|index
argument_list|(
literal|"test_index-000003"
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|newIndex
operator|.
name|getAliases
argument_list|()
operator|.
name|containsKey
argument_list|(
literal|"test_alias"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testRolloverWithIndexSettings
specifier|public
name|void
name|testRolloverWithIndexSettings
parameter_list|()
throws|throws
name|Exception
block|{
name|assertAcked
argument_list|(
name|prepareCreate
argument_list|(
literal|"test_index-2"
argument_list|)
operator|.
name|addAlias
argument_list|(
operator|new
name|Alias
argument_list|(
literal|"test_alias"
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|index
argument_list|(
literal|"test_index-2"
argument_list|,
literal|"type1"
argument_list|,
literal|"1"
argument_list|,
literal|"field"
argument_list|,
literal|"value"
argument_list|)
expr_stmt|;
name|flush
argument_list|(
literal|"test_index-2"
argument_list|)
expr_stmt|;
specifier|final
name|Settings
name|settings
init|=
name|Settings
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
literal|1
argument_list|)
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_NUMBER_OF_REPLICAS
argument_list|,
literal|0
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
specifier|final
name|RolloverResponse
name|response
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
name|prepareRolloverIndex
argument_list|(
literal|"test_alias"
argument_list|)
operator|.
name|settings
argument_list|(
name|settings
argument_list|)
operator|.
name|alias
argument_list|(
operator|new
name|Alias
argument_list|(
literal|"extra_alias"
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getOldIndex
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"test_index-2"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getNewIndex
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"test_index-000003"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|isDryRun
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|isRolledOver
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getConditionStatus
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
specifier|final
name|ClusterState
name|state
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
operator|.
name|getState
argument_list|()
decl_stmt|;
specifier|final
name|IndexMetaData
name|oldIndex
init|=
name|state
operator|.
name|metaData
argument_list|()
operator|.
name|index
argument_list|(
literal|"test_index-2"
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
name|oldIndex
operator|.
name|getAliases
argument_list|()
operator|.
name|containsKey
argument_list|(
literal|"test_alias"
argument_list|)
argument_list|)
expr_stmt|;
specifier|final
name|IndexMetaData
name|newIndex
init|=
name|state
operator|.
name|metaData
argument_list|()
operator|.
name|index
argument_list|(
literal|"test_index-000003"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|newIndex
operator|.
name|getNumberOfShards
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|newIndex
operator|.
name|getNumberOfReplicas
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|newIndex
operator|.
name|getAliases
argument_list|()
operator|.
name|containsKey
argument_list|(
literal|"test_alias"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|newIndex
operator|.
name|getAliases
argument_list|()
operator|.
name|containsKey
argument_list|(
literal|"extra_alias"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testRolloverDryRun
specifier|public
name|void
name|testRolloverDryRun
parameter_list|()
throws|throws
name|Exception
block|{
name|assertAcked
argument_list|(
name|prepareCreate
argument_list|(
literal|"test_index-1"
argument_list|)
operator|.
name|addAlias
argument_list|(
operator|new
name|Alias
argument_list|(
literal|"test_alias"
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|index
argument_list|(
literal|"test_index-1"
argument_list|,
literal|"type1"
argument_list|,
literal|"1"
argument_list|,
literal|"field"
argument_list|,
literal|"value"
argument_list|)
expr_stmt|;
name|flush
argument_list|(
literal|"test_index-1"
argument_list|)
expr_stmt|;
specifier|final
name|RolloverResponse
name|response
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
name|prepareRolloverIndex
argument_list|(
literal|"test_alias"
argument_list|)
operator|.
name|dryRun
argument_list|(
literal|true
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getOldIndex
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"test_index-1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getNewIndex
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"test_index-000002"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|isDryRun
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|isRolledOver
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getConditionStatus
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
specifier|final
name|ClusterState
name|state
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
operator|.
name|getState
argument_list|()
decl_stmt|;
specifier|final
name|IndexMetaData
name|oldIndex
init|=
name|state
operator|.
name|metaData
argument_list|()
operator|.
name|index
argument_list|(
literal|"test_index-1"
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|oldIndex
operator|.
name|getAliases
argument_list|()
operator|.
name|containsKey
argument_list|(
literal|"test_alias"
argument_list|)
argument_list|)
expr_stmt|;
specifier|final
name|IndexMetaData
name|newIndex
init|=
name|state
operator|.
name|metaData
argument_list|()
operator|.
name|index
argument_list|(
literal|"test_index-000002"
argument_list|)
decl_stmt|;
name|assertNull
argument_list|(
name|newIndex
argument_list|)
expr_stmt|;
block|}
DECL|method|testRolloverConditionsNotMet
specifier|public
name|void
name|testRolloverConditionsNotMet
parameter_list|()
throws|throws
name|Exception
block|{
name|assertAcked
argument_list|(
name|prepareCreate
argument_list|(
literal|"test_index-0"
argument_list|)
operator|.
name|addAlias
argument_list|(
operator|new
name|Alias
argument_list|(
literal|"test_alias"
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|index
argument_list|(
literal|"test_index-0"
argument_list|,
literal|"type1"
argument_list|,
literal|"1"
argument_list|,
literal|"field"
argument_list|,
literal|"value"
argument_list|)
expr_stmt|;
name|flush
argument_list|(
literal|"test_index-0"
argument_list|)
expr_stmt|;
specifier|final
name|RolloverResponse
name|response
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
name|prepareRolloverIndex
argument_list|(
literal|"test_alias"
argument_list|)
operator|.
name|addMaxIndexAgeCondition
argument_list|(
name|TimeValue
operator|.
name|timeValueHours
argument_list|(
literal|4
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getOldIndex
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"test_index-0"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getNewIndex
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"test_index-0"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|isDryRun
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|isRolledOver
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getConditionStatus
argument_list|()
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
specifier|final
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Boolean
argument_list|>
name|conditionEntry
init|=
name|response
operator|.
name|getConditionStatus
argument_list|()
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|conditionEntry
operator|.
name|getKey
argument_list|()
argument_list|,
name|equalTo
argument_list|(
operator|new
name|MaxAgeCondition
argument_list|(
name|TimeValue
operator|.
name|timeValueHours
argument_list|(
literal|4
argument_list|)
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|conditionEntry
operator|.
name|getValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
specifier|final
name|ClusterState
name|state
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
operator|.
name|getState
argument_list|()
decl_stmt|;
specifier|final
name|IndexMetaData
name|oldIndex
init|=
name|state
operator|.
name|metaData
argument_list|()
operator|.
name|index
argument_list|(
literal|"test_index-0"
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|oldIndex
operator|.
name|getAliases
argument_list|()
operator|.
name|containsKey
argument_list|(
literal|"test_alias"
argument_list|)
argument_list|)
expr_stmt|;
specifier|final
name|IndexMetaData
name|newIndex
init|=
name|state
operator|.
name|metaData
argument_list|()
operator|.
name|index
argument_list|(
literal|"test_index-000001"
argument_list|)
decl_stmt|;
name|assertNull
argument_list|(
name|newIndex
argument_list|)
expr_stmt|;
block|}
DECL|method|testRolloverWithNewIndexName
specifier|public
name|void
name|testRolloverWithNewIndexName
parameter_list|()
throws|throws
name|Exception
block|{
name|assertAcked
argument_list|(
name|prepareCreate
argument_list|(
literal|"test_index"
argument_list|)
operator|.
name|addAlias
argument_list|(
operator|new
name|Alias
argument_list|(
literal|"test_alias"
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|index
argument_list|(
literal|"test_index"
argument_list|,
literal|"type1"
argument_list|,
literal|"1"
argument_list|,
literal|"field"
argument_list|,
literal|"value"
argument_list|)
expr_stmt|;
name|flush
argument_list|(
literal|"test_index"
argument_list|)
expr_stmt|;
specifier|final
name|RolloverResponse
name|response
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
name|prepareRolloverIndex
argument_list|(
literal|"test_alias"
argument_list|)
operator|.
name|setNewIndexName
argument_list|(
literal|"test_new_index"
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getOldIndex
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"test_index"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getNewIndex
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"test_new_index"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|isDryRun
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|isRolledOver
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getConditionStatus
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
specifier|final
name|ClusterState
name|state
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
operator|.
name|getState
argument_list|()
decl_stmt|;
specifier|final
name|IndexMetaData
name|oldIndex
init|=
name|state
operator|.
name|metaData
argument_list|()
operator|.
name|index
argument_list|(
literal|"test_index"
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
name|oldIndex
operator|.
name|getAliases
argument_list|()
operator|.
name|containsKey
argument_list|(
literal|"test_alias"
argument_list|)
argument_list|)
expr_stmt|;
specifier|final
name|IndexMetaData
name|newIndex
init|=
name|state
operator|.
name|metaData
argument_list|()
operator|.
name|index
argument_list|(
literal|"test_new_index"
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|newIndex
operator|.
name|getAliases
argument_list|()
operator|.
name|containsKey
argument_list|(
literal|"test_alias"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testRolloverOnExistingIndex
specifier|public
name|void
name|testRolloverOnExistingIndex
parameter_list|()
throws|throws
name|Exception
block|{
name|assertAcked
argument_list|(
name|prepareCreate
argument_list|(
literal|"test_index-0"
argument_list|)
operator|.
name|addAlias
argument_list|(
operator|new
name|Alias
argument_list|(
literal|"test_alias"
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|index
argument_list|(
literal|"test_index-0"
argument_list|,
literal|"type1"
argument_list|,
literal|"1"
argument_list|,
literal|"field"
argument_list|,
literal|"value"
argument_list|)
expr_stmt|;
name|assertAcked
argument_list|(
name|prepareCreate
argument_list|(
literal|"test_index-000001"
argument_list|)
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|index
argument_list|(
literal|"test_index-000001"
argument_list|,
literal|"type1"
argument_list|,
literal|"1"
argument_list|,
literal|"field"
argument_list|,
literal|"value"
argument_list|)
expr_stmt|;
name|flush
argument_list|(
literal|"test_index-0"
argument_list|,
literal|"test_index-000001"
argument_list|)
expr_stmt|;
try|try
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
name|prepareRolloverIndex
argument_list|(
literal|"test_alias"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|fail
argument_list|(
literal|"expected failure due to existing rollover index"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IndexAlreadyExistsException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|e
operator|.
name|getIndex
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"test_index-000001"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit
