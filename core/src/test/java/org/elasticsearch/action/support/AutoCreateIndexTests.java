begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.support
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|support
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
name|ClusterName
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
name|IndexNameExpressionResolver
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
name|settings
operator|.
name|ClusterSettings
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
name|index
operator|.
name|IndexNotFoundException
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
name|mapper
operator|.
name|MapperService
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
name|HashMap
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
name|Map
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|CoreMatchers
operator|.
name|equalTo
import|;
end_import

begin_class
DECL|class|AutoCreateIndexTests
specifier|public
class|class
name|AutoCreateIndexTests
extends|extends
name|ESTestCase
block|{
DECL|method|testParseFailed
specifier|public
name|void
name|testParseFailed
parameter_list|()
block|{
try|try
block|{
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
literal|"action.auto_create_index"
argument_list|,
literal|",,,"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|newAutoCreateIndex
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"initialization should have failed"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|ex
parameter_list|)
block|{
name|assertEquals
argument_list|(
literal|"Can't parse [,,,] for setting [action.auto_create_index] must be either [true, false, or a "
operator|+
literal|"comma separated list of index patterns]"
argument_list|,
name|ex
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testParseFailedMissingIndex
specifier|public
name|void
name|testParseFailedMissingIndex
parameter_list|()
block|{
name|String
name|prefix
init|=
name|randomFrom
argument_list|(
literal|"+"
argument_list|,
literal|"-"
argument_list|)
decl_stmt|;
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
name|AutoCreateIndex
operator|.
name|AUTO_CREATE_INDEX_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
name|prefix
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
try|try
block|{
name|newAutoCreateIndex
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"initialization should have failed"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|ex
parameter_list|)
block|{
name|assertEquals
argument_list|(
literal|"Can't parse ["
operator|+
name|prefix
operator|+
literal|"] for setting [action.auto_create_index] must contain an index name after ["
operator|+
name|prefix
operator|+
literal|"]"
argument_list|,
name|ex
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testHandleSpaces
specifier|public
name|void
name|testHandleSpaces
parameter_list|()
block|{
comment|// see #21449
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
name|AutoCreateIndex
operator|.
name|AUTO_CREATE_INDEX_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
name|randomFrom
argument_list|(
literal|".marvel-, .security, .watches, .triggered_watches, .watcher-history-"
argument_list|,
literal|".marvel-,.security,.watches,.triggered_watches,.watcher-history-"
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|AutoCreateIndex
name|autoCreateIndex
init|=
name|newAutoCreateIndex
argument_list|(
name|settings
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Tuple
argument_list|<
name|String
argument_list|,
name|Boolean
argument_list|>
argument_list|>
name|expressions
init|=
name|autoCreateIndex
operator|.
name|getAutoCreate
argument_list|()
operator|.
name|getExpressions
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Boolean
argument_list|>
name|map
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|Tuple
argument_list|<
name|String
argument_list|,
name|Boolean
argument_list|>
name|t
range|:
name|expressions
control|)
block|{
name|map
operator|.
name|put
argument_list|(
name|t
operator|.
name|v1
argument_list|()
argument_list|,
name|t
operator|.
name|v2
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|assertTrue
argument_list|(
name|map
operator|.
name|get
argument_list|(
literal|".marvel-"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|map
operator|.
name|get
argument_list|(
literal|".security"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|map
operator|.
name|get
argument_list|(
literal|".watches"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|map
operator|.
name|get
argument_list|(
literal|".triggered_watches"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|map
operator|.
name|get
argument_list|(
literal|".watcher-history-"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|5
argument_list|,
name|map
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testAutoCreationDisabled
specifier|public
name|void
name|testAutoCreationDisabled
parameter_list|()
block|{
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
name|AutoCreateIndex
operator|.
name|AUTO_CREATE_INDEX_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
literal|false
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|AutoCreateIndex
name|autoCreateIndex
init|=
name|newAutoCreateIndex
argument_list|(
name|settings
argument_list|)
decl_stmt|;
name|IndexNotFoundException
name|e
init|=
name|expectThrows
argument_list|(
name|IndexNotFoundException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|autoCreateIndex
operator|.
name|shouldAutoCreate
argument_list|(
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
argument_list|,
name|buildClusterState
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"no such index and [action.auto_create_index] is [false]"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testAutoCreationEnabled
specifier|public
name|void
name|testAutoCreationEnabled
parameter_list|()
block|{
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
name|AutoCreateIndex
operator|.
name|AUTO_CREATE_INDEX_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
literal|true
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|AutoCreateIndex
name|autoCreateIndex
init|=
name|newAutoCreateIndex
argument_list|(
name|settings
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|autoCreateIndex
operator|.
name|shouldAutoCreate
argument_list|(
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
argument_list|,
name|buildClusterState
argument_list|()
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testDefaultAutoCreation
specifier|public
name|void
name|testDefaultAutoCreation
parameter_list|()
block|{
name|AutoCreateIndex
name|autoCreateIndex
init|=
name|newAutoCreateIndex
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|autoCreateIndex
operator|.
name|shouldAutoCreate
argument_list|(
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
argument_list|,
name|buildClusterState
argument_list|()
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testExistingIndex
specifier|public
name|void
name|testExistingIndex
parameter_list|()
block|{
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
name|AutoCreateIndex
operator|.
name|AUTO_CREATE_INDEX_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
name|randomFrom
argument_list|(
literal|true
argument_list|,
literal|false
argument_list|,
name|randomAsciiOfLengthBetween
argument_list|(
literal|7
argument_list|,
literal|10
argument_list|)
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|AutoCreateIndex
name|autoCreateIndex
init|=
name|newAutoCreateIndex
argument_list|(
name|settings
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|autoCreateIndex
operator|.
name|shouldAutoCreate
argument_list|(
name|randomFrom
argument_list|(
literal|"index1"
argument_list|,
literal|"index2"
argument_list|,
literal|"index3"
argument_list|)
argument_list|,
name|buildClusterState
argument_list|(
literal|"index1"
argument_list|,
literal|"index2"
argument_list|,
literal|"index3"
argument_list|)
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testDynamicMappingDisabled
specifier|public
name|void
name|testDynamicMappingDisabled
parameter_list|()
block|{
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
name|AutoCreateIndex
operator|.
name|AUTO_CREATE_INDEX_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
name|randomFrom
argument_list|(
literal|true
argument_list|,
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
argument_list|)
argument_list|)
operator|.
name|put
argument_list|(
name|MapperService
operator|.
name|INDEX_MAPPER_DYNAMIC_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
literal|false
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|AutoCreateIndex
name|autoCreateIndex
init|=
name|newAutoCreateIndex
argument_list|(
name|settings
argument_list|)
decl_stmt|;
name|IndexNotFoundException
name|e
init|=
name|expectThrows
argument_list|(
name|IndexNotFoundException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|autoCreateIndex
operator|.
name|shouldAutoCreate
argument_list|(
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
argument_list|,
name|buildClusterState
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"no such index and [index.mapper.dynamic] is [false]"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testAutoCreationPatternEnabled
specifier|public
name|void
name|testAutoCreationPatternEnabled
parameter_list|()
block|{
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
name|AutoCreateIndex
operator|.
name|AUTO_CREATE_INDEX_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
name|randomFrom
argument_list|(
literal|"+index*"
argument_list|,
literal|"index*"
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|AutoCreateIndex
name|autoCreateIndex
init|=
name|newAutoCreateIndex
argument_list|(
name|settings
argument_list|)
decl_stmt|;
name|ClusterState
name|clusterState
init|=
name|ClusterState
operator|.
name|builder
argument_list|(
operator|new
name|ClusterName
argument_list|(
literal|"test"
argument_list|)
argument_list|)
operator|.
name|metaData
argument_list|(
name|MetaData
operator|.
name|builder
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|autoCreateIndex
operator|.
name|shouldAutoCreate
argument_list|(
literal|"index"
operator|+
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|5
argument_list|)
argument_list|,
name|clusterState
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|expectNotMatch
argument_list|(
name|clusterState
argument_list|,
name|autoCreateIndex
argument_list|,
literal|"does_not_match"
operator|+
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|5
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testAutoCreationPatternDisabled
specifier|public
name|void
name|testAutoCreationPatternDisabled
parameter_list|()
block|{
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
name|AutoCreateIndex
operator|.
name|AUTO_CREATE_INDEX_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
literal|"-index*"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|AutoCreateIndex
name|autoCreateIndex
init|=
name|newAutoCreateIndex
argument_list|(
name|settings
argument_list|)
decl_stmt|;
name|ClusterState
name|clusterState
init|=
name|ClusterState
operator|.
name|builder
argument_list|(
operator|new
name|ClusterName
argument_list|(
literal|"test"
argument_list|)
argument_list|)
operator|.
name|metaData
argument_list|(
name|MetaData
operator|.
name|builder
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|expectForbidden
argument_list|(
name|clusterState
argument_list|,
name|autoCreateIndex
argument_list|,
literal|"index"
operator|+
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|5
argument_list|)
argument_list|,
literal|"-index*"
argument_list|)
expr_stmt|;
comment|/* When patterns are specified, even if the are all negative, the default is can't create. So a pure negative pattern is the same          * as false, really. */
name|expectNotMatch
argument_list|(
name|clusterState
argument_list|,
name|autoCreateIndex
argument_list|,
literal|"does_not_match"
operator|+
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|5
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testAutoCreationMultiplePatternsWithWildcards
specifier|public
name|void
name|testAutoCreationMultiplePatternsWithWildcards
parameter_list|()
block|{
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
name|AutoCreateIndex
operator|.
name|AUTO_CREATE_INDEX_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
name|randomFrom
argument_list|(
literal|"+test*,-index*"
argument_list|,
literal|"test*,-index*"
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|AutoCreateIndex
name|autoCreateIndex
init|=
name|newAutoCreateIndex
argument_list|(
name|settings
argument_list|)
decl_stmt|;
name|ClusterState
name|clusterState
init|=
name|ClusterState
operator|.
name|builder
argument_list|(
operator|new
name|ClusterName
argument_list|(
literal|"test"
argument_list|)
argument_list|)
operator|.
name|metaData
argument_list|(
name|MetaData
operator|.
name|builder
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|expectForbidden
argument_list|(
name|clusterState
argument_list|,
name|autoCreateIndex
argument_list|,
literal|"index"
operator|+
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|5
argument_list|)
argument_list|,
literal|"-index*"
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|autoCreateIndex
operator|.
name|shouldAutoCreate
argument_list|(
literal|"test"
operator|+
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|5
argument_list|)
argument_list|,
name|clusterState
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|expectNotMatch
argument_list|(
name|clusterState
argument_list|,
name|autoCreateIndex
argument_list|,
literal|"does_not_match"
operator|+
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|5
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testAutoCreationMultiplePatternsNoWildcards
specifier|public
name|void
name|testAutoCreationMultiplePatternsNoWildcards
parameter_list|()
block|{
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
name|AutoCreateIndex
operator|.
name|AUTO_CREATE_INDEX_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
literal|"+test1,-index1"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|AutoCreateIndex
name|autoCreateIndex
init|=
name|newAutoCreateIndex
argument_list|(
name|settings
argument_list|)
decl_stmt|;
name|ClusterState
name|clusterState
init|=
name|ClusterState
operator|.
name|builder
argument_list|(
operator|new
name|ClusterName
argument_list|(
literal|"test"
argument_list|)
argument_list|)
operator|.
name|metaData
argument_list|(
name|MetaData
operator|.
name|builder
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|autoCreateIndex
operator|.
name|shouldAutoCreate
argument_list|(
literal|"test1"
argument_list|,
name|clusterState
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|expectNotMatch
argument_list|(
name|clusterState
argument_list|,
name|autoCreateIndex
argument_list|,
literal|"index"
operator|+
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|5
argument_list|)
argument_list|)
expr_stmt|;
name|expectNotMatch
argument_list|(
name|clusterState
argument_list|,
name|autoCreateIndex
argument_list|,
literal|"test"
operator|+
name|randomAsciiOfLengthBetween
argument_list|(
literal|2
argument_list|,
literal|5
argument_list|)
argument_list|)
expr_stmt|;
name|expectNotMatch
argument_list|(
name|clusterState
argument_list|,
name|autoCreateIndex
argument_list|,
literal|"does_not_match"
operator|+
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|5
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testAutoCreationMultipleIndexNames
specifier|public
name|void
name|testAutoCreationMultipleIndexNames
parameter_list|()
block|{
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
name|AutoCreateIndex
operator|.
name|AUTO_CREATE_INDEX_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
literal|"test1,test2"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|AutoCreateIndex
name|autoCreateIndex
init|=
name|newAutoCreateIndex
argument_list|(
name|settings
argument_list|)
decl_stmt|;
name|ClusterState
name|clusterState
init|=
name|ClusterState
operator|.
name|builder
argument_list|(
operator|new
name|ClusterName
argument_list|(
literal|"test"
argument_list|)
argument_list|)
operator|.
name|metaData
argument_list|(
name|MetaData
operator|.
name|builder
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|autoCreateIndex
operator|.
name|shouldAutoCreate
argument_list|(
literal|"test1"
argument_list|,
name|clusterState
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
name|autoCreateIndex
operator|.
name|shouldAutoCreate
argument_list|(
literal|"test2"
argument_list|,
name|clusterState
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|expectNotMatch
argument_list|(
name|clusterState
argument_list|,
name|autoCreateIndex
argument_list|,
literal|"does_not_match"
operator|+
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|5
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testAutoCreationConflictingPatternsFirstWins
specifier|public
name|void
name|testAutoCreationConflictingPatternsFirstWins
parameter_list|()
block|{
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
name|AutoCreateIndex
operator|.
name|AUTO_CREATE_INDEX_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
literal|"+test1,-test1,-test2,+test2"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|AutoCreateIndex
name|autoCreateIndex
init|=
name|newAutoCreateIndex
argument_list|(
name|settings
argument_list|)
decl_stmt|;
name|ClusterState
name|clusterState
init|=
name|ClusterState
operator|.
name|builder
argument_list|(
operator|new
name|ClusterName
argument_list|(
literal|"test"
argument_list|)
argument_list|)
operator|.
name|metaData
argument_list|(
name|MetaData
operator|.
name|builder
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|autoCreateIndex
operator|.
name|shouldAutoCreate
argument_list|(
literal|"test1"
argument_list|,
name|clusterState
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|expectForbidden
argument_list|(
name|clusterState
argument_list|,
name|autoCreateIndex
argument_list|,
literal|"test2"
argument_list|,
literal|"-test2"
argument_list|)
expr_stmt|;
name|expectNotMatch
argument_list|(
name|clusterState
argument_list|,
name|autoCreateIndex
argument_list|,
literal|"does_not_match"
operator|+
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|5
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testUpdate
specifier|public
name|void
name|testUpdate
parameter_list|()
block|{
name|boolean
name|value
init|=
name|randomBoolean
argument_list|()
decl_stmt|;
name|Settings
name|settings
decl_stmt|;
if|if
condition|(
name|value
operator|&&
name|randomBoolean
argument_list|()
condition|)
block|{
name|settings
operator|=
name|Settings
operator|.
name|EMPTY
expr_stmt|;
block|}
else|else
block|{
name|settings
operator|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|AutoCreateIndex
operator|.
name|AUTO_CREATE_INDEX_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
name|value
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
block|}
name|ClusterSettings
name|clusterSettings
init|=
operator|new
name|ClusterSettings
argument_list|(
name|settings
argument_list|,
name|ClusterSettings
operator|.
name|BUILT_IN_CLUSTER_SETTINGS
argument_list|)
decl_stmt|;
name|AutoCreateIndex
name|autoCreateIndex
init|=
operator|new
name|AutoCreateIndex
argument_list|(
name|settings
argument_list|,
name|clusterSettings
argument_list|,
operator|new
name|IndexNameExpressionResolver
argument_list|(
name|settings
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|autoCreateIndex
operator|.
name|getAutoCreate
argument_list|()
operator|.
name|isAutoCreateIndex
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
name|Settings
name|newSettings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|AutoCreateIndex
operator|.
name|AUTO_CREATE_INDEX_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
operator|!
name|value
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|clusterSettings
operator|.
name|applySettings
argument_list|(
name|newSettings
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|autoCreateIndex
operator|.
name|getAutoCreate
argument_list|()
operator|.
name|isAutoCreateIndex
argument_list|()
argument_list|,
name|equalTo
argument_list|(
operator|!
name|value
argument_list|)
argument_list|)
expr_stmt|;
name|newSettings
operator|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|AutoCreateIndex
operator|.
name|AUTO_CREATE_INDEX_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
literal|"logs-*"
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|clusterSettings
operator|.
name|applySettings
argument_list|(
name|newSettings
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|autoCreateIndex
operator|.
name|getAutoCreate
argument_list|()
operator|.
name|isAutoCreateIndex
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
name|autoCreateIndex
operator|.
name|getAutoCreate
argument_list|()
operator|.
name|getExpressions
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
name|assertThat
argument_list|(
name|autoCreateIndex
operator|.
name|getAutoCreate
argument_list|()
operator|.
name|getExpressions
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|v1
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"logs-*"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|buildClusterState
specifier|private
specifier|static
name|ClusterState
name|buildClusterState
parameter_list|(
name|String
modifier|...
name|indices
parameter_list|)
block|{
name|MetaData
operator|.
name|Builder
name|metaData
init|=
name|MetaData
operator|.
name|builder
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|index
range|:
name|indices
control|)
block|{
name|metaData
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|builder
argument_list|(
name|index
argument_list|)
operator|.
name|settings
argument_list|(
name|settings
argument_list|(
name|Version
operator|.
name|CURRENT
argument_list|)
argument_list|)
operator|.
name|numberOfShards
argument_list|(
literal|1
argument_list|)
operator|.
name|numberOfReplicas
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|ClusterState
operator|.
name|builder
argument_list|(
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|ClusterName
operator|.
name|CLUSTER_NAME_SETTING
operator|.
name|getDefault
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
argument_list|)
operator|.
name|metaData
argument_list|(
name|metaData
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
DECL|method|newAutoCreateIndex
specifier|private
name|AutoCreateIndex
name|newAutoCreateIndex
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
return|return
operator|new
name|AutoCreateIndex
argument_list|(
name|settings
argument_list|,
operator|new
name|ClusterSettings
argument_list|(
name|settings
argument_list|,
name|ClusterSettings
operator|.
name|BUILT_IN_CLUSTER_SETTINGS
argument_list|)
argument_list|,
operator|new
name|IndexNameExpressionResolver
argument_list|(
name|settings
argument_list|)
argument_list|)
return|;
block|}
DECL|method|expectNotMatch
specifier|private
name|void
name|expectNotMatch
parameter_list|(
name|ClusterState
name|clusterState
parameter_list|,
name|AutoCreateIndex
name|autoCreateIndex
parameter_list|,
name|String
name|index
parameter_list|)
block|{
name|IndexNotFoundException
name|e
init|=
name|expectThrows
argument_list|(
name|IndexNotFoundException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|autoCreateIndex
operator|.
name|shouldAutoCreate
argument_list|(
name|index
argument_list|,
name|clusterState
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"no such index and [action.auto_create_index] (["
operator|+
name|autoCreateIndex
operator|.
name|getAutoCreate
argument_list|()
operator|+
literal|"]) doesn't match"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|expectForbidden
specifier|private
name|void
name|expectForbidden
parameter_list|(
name|ClusterState
name|clusterState
parameter_list|,
name|AutoCreateIndex
name|autoCreateIndex
parameter_list|,
name|String
name|index
parameter_list|,
name|String
name|forbiddingPattern
parameter_list|)
block|{
name|IndexNotFoundException
name|e
init|=
name|expectThrows
argument_list|(
name|IndexNotFoundException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|autoCreateIndex
operator|.
name|shouldAutoCreate
argument_list|(
name|index
argument_list|,
name|clusterState
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"no such index and [action.auto_create_index] contains ["
operator|+
name|forbiddingPattern
operator|+
literal|"] which forbids automatic creation of the index"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

