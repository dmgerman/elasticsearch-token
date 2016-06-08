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
name|admin
operator|.
name|indices
operator|.
name|alias
operator|.
name|IndicesAliasesClusterStateUpdateRequest
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
name|AliasAction
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
name|AliasMetaData
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
name|UUIDs
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
name|common
operator|.
name|util
operator|.
name|set
operator|.
name|Sets
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
name|DocsStats
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
name|util
operator|.
name|ArrayList
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
name|HashSet
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
name|Set
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
name|admin
operator|.
name|indices
operator|.
name|rollover
operator|.
name|TransportRolloverAction
operator|.
name|evaluateConditions
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

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|not
import|;
end_import

begin_class
DECL|class|TransportRolloverActionTests
specifier|public
class|class
name|TransportRolloverActionTests
extends|extends
name|ESTestCase
block|{
DECL|method|testEvaluateConditions
specifier|public
name|void
name|testEvaluateConditions
parameter_list|()
throws|throws
name|Exception
block|{
name|MaxDocsCondition
name|maxDocsCondition
init|=
operator|new
name|MaxDocsCondition
argument_list|(
literal|100L
argument_list|)
decl_stmt|;
name|MaxAgeCondition
name|maxAgeCondition
init|=
operator|new
name|MaxAgeCondition
argument_list|(
name|TimeValue
operator|.
name|timeValueHours
argument_list|(
literal|2
argument_list|)
argument_list|)
decl_stmt|;
name|long
name|matchMaxDocs
init|=
name|randomIntBetween
argument_list|(
literal|100
argument_list|,
literal|1000
argument_list|)
decl_stmt|;
name|long
name|notMatchMaxDocs
init|=
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|99
argument_list|)
decl_stmt|;
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
name|SETTING_VERSION_CREATED
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_INDEX_UUID
argument_list|,
name|UUIDs
operator|.
name|randomBase64UUID
argument_list|()
argument_list|)
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
name|IndexMetaData
name|metaData
init|=
name|IndexMetaData
operator|.
name|builder
argument_list|(
name|randomAsciiOfLength
argument_list|(
literal|10
argument_list|)
argument_list|)
operator|.
name|creationDate
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|TimeValue
operator|.
name|timeValueHours
argument_list|(
literal|3
argument_list|)
operator|.
name|getMillis
argument_list|()
argument_list|)
operator|.
name|settings
argument_list|(
name|settings
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
specifier|final
name|HashSet
argument_list|<
name|Condition
argument_list|>
name|conditions
init|=
name|Sets
operator|.
name|newHashSet
argument_list|(
name|maxDocsCondition
argument_list|,
name|maxAgeCondition
argument_list|)
decl_stmt|;
name|Set
argument_list|<
name|Condition
operator|.
name|Result
argument_list|>
name|results
init|=
name|evaluateConditions
argument_list|(
name|conditions
argument_list|,
operator|new
name|DocsStats
argument_list|(
name|matchMaxDocs
argument_list|,
literal|0L
argument_list|)
argument_list|,
name|metaData
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|results
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|Condition
operator|.
name|Result
name|result
range|:
name|results
control|)
block|{
name|assertThat
argument_list|(
name|result
operator|.
name|matched
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|results
operator|=
name|evaluateConditions
argument_list|(
name|conditions
argument_list|,
operator|new
name|DocsStats
argument_list|(
name|notMatchMaxDocs
argument_list|,
literal|0
argument_list|)
argument_list|,
name|metaData
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|results
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|Condition
operator|.
name|Result
name|result
range|:
name|results
control|)
block|{
if|if
condition|(
name|result
operator|.
name|condition
operator|instanceof
name|MaxAgeCondition
condition|)
block|{
name|assertThat
argument_list|(
name|result
operator|.
name|matched
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|result
operator|.
name|condition
operator|instanceof
name|MaxDocsCondition
condition|)
block|{
name|assertThat
argument_list|(
name|result
operator|.
name|matched
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|fail
argument_list|(
literal|"unknown condition result found "
operator|+
name|result
operator|.
name|condition
argument_list|)
expr_stmt|;
block|}
block|}
name|results
operator|=
name|evaluateConditions
argument_list|(
name|conditions
argument_list|,
literal|null
argument_list|,
name|metaData
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|results
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|Condition
operator|.
name|Result
name|result
range|:
name|results
control|)
block|{
if|if
condition|(
name|result
operator|.
name|condition
operator|instanceof
name|MaxAgeCondition
condition|)
block|{
name|assertThat
argument_list|(
name|result
operator|.
name|matched
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|result
operator|.
name|condition
operator|instanceof
name|MaxDocsCondition
condition|)
block|{
name|assertThat
argument_list|(
name|result
operator|.
name|matched
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|fail
argument_list|(
literal|"unknown condition result found "
operator|+
name|result
operator|.
name|condition
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|testCreateUpdateAliasRequest
specifier|public
name|void
name|testCreateUpdateAliasRequest
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|sourceAlias
init|=
name|randomAsciiOfLength
argument_list|(
literal|10
argument_list|)
decl_stmt|;
name|String
name|sourceIndex
init|=
name|randomAsciiOfLength
argument_list|(
literal|10
argument_list|)
decl_stmt|;
name|String
name|targetIndex
init|=
name|randomAsciiOfLength
argument_list|(
literal|10
argument_list|)
decl_stmt|;
specifier|final
name|RolloverRequest
name|rolloverRequest
init|=
operator|new
name|RolloverRequest
argument_list|(
name|sourceAlias
argument_list|)
decl_stmt|;
specifier|final
name|IndicesAliasesClusterStateUpdateRequest
name|updateRequest
init|=
name|TransportRolloverAction
operator|.
name|prepareIndicesAliasesRequest
argument_list|(
name|sourceIndex
argument_list|,
name|targetIndex
argument_list|,
name|rolloverRequest
argument_list|)
decl_stmt|;
specifier|final
name|AliasAction
index|[]
name|actions
init|=
name|updateRequest
operator|.
name|actions
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|actions
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|boolean
name|foundAdd
init|=
literal|false
decl_stmt|;
name|boolean
name|foundRemove
init|=
literal|false
decl_stmt|;
for|for
control|(
name|AliasAction
name|action
range|:
name|actions
control|)
block|{
if|if
condition|(
name|action
operator|.
name|actionType
argument_list|()
operator|==
name|AliasAction
operator|.
name|Type
operator|.
name|ADD
condition|)
block|{
name|foundAdd
operator|=
literal|true
expr_stmt|;
name|assertThat
argument_list|(
name|action
operator|.
name|index
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|targetIndex
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|action
operator|.
name|alias
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|sourceAlias
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|action
operator|.
name|actionType
argument_list|()
operator|==
name|AliasAction
operator|.
name|Type
operator|.
name|REMOVE
condition|)
block|{
name|foundRemove
operator|=
literal|true
expr_stmt|;
name|assertThat
argument_list|(
name|action
operator|.
name|index
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|sourceIndex
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|action
operator|.
name|alias
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|sourceAlias
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|assertTrue
argument_list|(
name|foundAdd
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|foundRemove
argument_list|)
expr_stmt|;
block|}
DECL|method|testValidation
specifier|public
name|void
name|testValidation
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|index1
init|=
name|randomAsciiOfLength
argument_list|(
literal|10
argument_list|)
decl_stmt|;
name|String
name|alias
init|=
name|randomAsciiOfLength
argument_list|(
literal|10
argument_list|)
decl_stmt|;
name|String
name|index2
init|=
name|randomAsciiOfLength
argument_list|(
literal|10
argument_list|)
decl_stmt|;
name|String
name|aliasWithMultipleIndices
init|=
name|randomAsciiOfLength
argument_list|(
literal|10
argument_list|)
decl_stmt|;
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
name|SETTING_VERSION_CREATED
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_INDEX_UUID
argument_list|,
name|UUIDs
operator|.
name|randomBase64UUID
argument_list|()
argument_list|)
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
name|MetaData
name|metaData
init|=
name|MetaData
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|builder
argument_list|(
name|index1
argument_list|)
operator|.
name|settings
argument_list|(
name|settings
argument_list|)
operator|.
name|putAlias
argument_list|(
name|AliasMetaData
operator|.
name|builder
argument_list|(
name|alias
argument_list|)
argument_list|)
operator|.
name|putAlias
argument_list|(
name|AliasMetaData
operator|.
name|builder
argument_list|(
name|aliasWithMultipleIndices
argument_list|)
argument_list|)
argument_list|)
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|builder
argument_list|(
name|index2
argument_list|)
operator|.
name|settings
argument_list|(
name|settings
argument_list|)
operator|.
name|putAlias
argument_list|(
name|AliasMetaData
operator|.
name|builder
argument_list|(
name|aliasWithMultipleIndices
argument_list|)
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|TransportRolloverAction
operator|.
name|validate
argument_list|(
name|metaData
argument_list|,
operator|new
name|RolloverRequest
argument_list|(
name|aliasWithMultipleIndices
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|TransportRolloverAction
operator|.
name|validate
argument_list|(
name|metaData
argument_list|,
operator|new
name|RolloverRequest
argument_list|(
name|randomFrom
argument_list|(
name|index1
argument_list|,
name|index2
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|TransportRolloverAction
operator|.
name|validate
argument_list|(
name|metaData
argument_list|,
operator|new
name|RolloverRequest
argument_list|(
name|randomAsciiOfLength
argument_list|(
literal|5
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|TransportRolloverAction
operator|.
name|validate
argument_list|(
name|metaData
argument_list|,
operator|new
name|RolloverRequest
argument_list|(
name|alias
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testGenerateRolloverIndexName
specifier|public
name|void
name|testGenerateRolloverIndexName
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|invalidIndexName
init|=
name|randomAsciiOfLength
argument_list|(
literal|10
argument_list|)
operator|+
literal|"A"
decl_stmt|;
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|TransportRolloverAction
operator|.
name|generateRolloverIndexName
argument_list|(
name|invalidIndexName
argument_list|)
argument_list|)
expr_stmt|;
name|int
name|num
init|=
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|100
argument_list|)
decl_stmt|;
specifier|final
name|String
name|indexPrefix
init|=
name|randomAsciiOfLength
argument_list|(
literal|10
argument_list|)
decl_stmt|;
name|String
name|indexEndingInNumbers
init|=
name|indexPrefix
operator|+
literal|"-"
operator|+
name|num
decl_stmt|;
name|assertThat
argument_list|(
name|TransportRolloverAction
operator|.
name|generateRolloverIndexName
argument_list|(
name|indexEndingInNumbers
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|indexPrefix
operator|+
literal|"-"
operator|+
operator|(
name|num
operator|+
literal|1
operator|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|TransportRolloverAction
operator|.
name|generateRolloverIndexName
argument_list|(
literal|"index-name-1"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"index-name-2"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|TransportRolloverAction
operator|.
name|generateRolloverIndexName
argument_list|(
literal|"index-name-2"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"index-name-3"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

