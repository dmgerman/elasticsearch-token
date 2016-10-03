begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.routing
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|routing
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
name|IndicesAliasesRequest
operator|.
name|AliasActions
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
name|support
operator|.
name|IndicesOptions
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
name|IndexNameExpressionResolver
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
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ExecutionException
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
name|util
operator|.
name|set
operator|.
name|Sets
operator|.
name|newHashSet
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|QueryBuilders
operator|.
name|matchQuery
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
name|assertHitCount
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
name|nullValue
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|AliasResolveRoutingIT
specifier|public
class|class
name|AliasResolveRoutingIT
extends|extends
name|ESIntegTestCase
block|{
comment|// see https://github.com/elastic/elasticsearch/issues/13278
DECL|method|testSearchClosedWildcardIndex
specifier|public
name|void
name|testSearchClosedWildcardIndex
parameter_list|()
throws|throws
name|ExecutionException
throws|,
name|InterruptedException
block|{
name|createIndex
argument_list|(
literal|"test-0"
argument_list|)
expr_stmt|;
name|createIndex
argument_list|(
literal|"test-1"
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareAliases
argument_list|()
operator|.
name|addAlias
argument_list|(
literal|"test-0"
argument_list|,
literal|"alias-0"
argument_list|)
operator|.
name|addAlias
argument_list|(
literal|"test-1"
argument_list|,
literal|"alias-1"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareClose
argument_list|(
literal|"test-1"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|indexRandom
argument_list|(
literal|true
argument_list|,
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test-0"
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
literal|"the quick brown fox jumps"
argument_list|)
argument_list|,
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test-0"
argument_list|,
literal|"type1"
argument_list|,
literal|"2"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"field1"
argument_list|,
literal|"quick brown"
argument_list|)
argument_list|,
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test-0"
argument_list|,
literal|"type1"
argument_list|,
literal|"3"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"field1"
argument_list|,
literal|"quick"
argument_list|)
argument_list|)
expr_stmt|;
name|refresh
argument_list|(
literal|"test-*"
argument_list|)
expr_stmt|;
name|assertHitCount
argument_list|(
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|()
operator|.
name|setIndices
argument_list|(
literal|"alias-*"
argument_list|)
operator|.
name|setIndicesOptions
argument_list|(
name|IndicesOptions
operator|.
name|lenientExpandOpen
argument_list|()
argument_list|)
operator|.
name|setQuery
argument_list|(
name|matchQuery
argument_list|(
literal|"_all"
argument_list|,
literal|"quick"
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
argument_list|,
literal|3L
argument_list|)
expr_stmt|;
block|}
DECL|method|testResolveIndexRouting
specifier|public
name|void
name|testResolveIndexRouting
parameter_list|()
throws|throws
name|Exception
block|{
name|createIndex
argument_list|(
literal|"test1"
argument_list|)
expr_stmt|;
name|createIndex
argument_list|(
literal|"test2"
argument_list|)
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
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareAliases
argument_list|()
operator|.
name|addAliasAction
argument_list|(
name|AliasActions
operator|.
name|add
argument_list|()
operator|.
name|index
argument_list|(
literal|"test1"
argument_list|)
operator|.
name|alias
argument_list|(
literal|"alias"
argument_list|)
argument_list|)
operator|.
name|addAliasAction
argument_list|(
name|AliasActions
operator|.
name|add
argument_list|()
operator|.
name|index
argument_list|(
literal|"test1"
argument_list|)
operator|.
name|alias
argument_list|(
literal|"alias10"
argument_list|)
operator|.
name|routing
argument_list|(
literal|"0"
argument_list|)
argument_list|)
operator|.
name|addAliasAction
argument_list|(
name|AliasActions
operator|.
name|add
argument_list|()
operator|.
name|index
argument_list|(
literal|"test1"
argument_list|)
operator|.
name|alias
argument_list|(
literal|"alias110"
argument_list|)
operator|.
name|searchRouting
argument_list|(
literal|"1,0"
argument_list|)
argument_list|)
operator|.
name|addAliasAction
argument_list|(
name|AliasActions
operator|.
name|add
argument_list|()
operator|.
name|index
argument_list|(
literal|"test1"
argument_list|)
operator|.
name|alias
argument_list|(
literal|"alias12"
argument_list|)
operator|.
name|routing
argument_list|(
literal|"2"
argument_list|)
argument_list|)
operator|.
name|addAliasAction
argument_list|(
name|AliasActions
operator|.
name|add
argument_list|()
operator|.
name|index
argument_list|(
literal|"test2"
argument_list|)
operator|.
name|alias
argument_list|(
literal|"alias20"
argument_list|)
operator|.
name|routing
argument_list|(
literal|"0"
argument_list|)
argument_list|)
operator|.
name|addAliasAction
argument_list|(
name|AliasActions
operator|.
name|add
argument_list|()
operator|.
name|index
argument_list|(
literal|"test2"
argument_list|)
operator|.
name|alias
argument_list|(
literal|"alias21"
argument_list|)
operator|.
name|routing
argument_list|(
literal|"1"
argument_list|)
argument_list|)
operator|.
name|addAliasAction
argument_list|(
name|AliasActions
operator|.
name|add
argument_list|()
operator|.
name|index
argument_list|(
literal|"test1"
argument_list|)
operator|.
name|alias
argument_list|(
literal|"alias0"
argument_list|)
operator|.
name|routing
argument_list|(
literal|"0"
argument_list|)
argument_list|)
operator|.
name|addAliasAction
argument_list|(
name|AliasActions
operator|.
name|add
argument_list|()
operator|.
name|index
argument_list|(
literal|"test2"
argument_list|)
operator|.
name|alias
argument_list|(
literal|"alias0"
argument_list|)
operator|.
name|routing
argument_list|(
literal|"0"
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|clusterService
argument_list|()
operator|.
name|state
argument_list|()
operator|.
name|metaData
argument_list|()
operator|.
name|resolveIndexRouting
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
literal|"test1"
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|clusterService
argument_list|()
operator|.
name|state
argument_list|()
operator|.
name|metaData
argument_list|()
operator|.
name|resolveIndexRouting
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
literal|"alias"
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|clusterService
argument_list|()
operator|.
name|state
argument_list|()
operator|.
name|metaData
argument_list|()
operator|.
name|resolveIndexRouting
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
literal|"test1"
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|clusterService
argument_list|()
operator|.
name|state
argument_list|()
operator|.
name|metaData
argument_list|()
operator|.
name|resolveIndexRouting
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
literal|"alias10"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"0"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|clusterService
argument_list|()
operator|.
name|state
argument_list|()
operator|.
name|metaData
argument_list|()
operator|.
name|resolveIndexRouting
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
literal|"alias20"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"0"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|clusterService
argument_list|()
operator|.
name|state
argument_list|()
operator|.
name|metaData
argument_list|()
operator|.
name|resolveIndexRouting
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
literal|"alias21"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|clusterService
argument_list|()
operator|.
name|state
argument_list|()
operator|.
name|metaData
argument_list|()
operator|.
name|resolveIndexRouting
argument_list|(
literal|null
argument_list|,
literal|"3"
argument_list|,
literal|"test1"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"3"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|clusterService
argument_list|()
operator|.
name|state
argument_list|()
operator|.
name|metaData
argument_list|()
operator|.
name|resolveIndexRouting
argument_list|(
literal|null
argument_list|,
literal|"0"
argument_list|,
literal|"alias10"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"0"
argument_list|)
argument_list|)
expr_stmt|;
comment|// Force the alias routing and ignore the parent.
name|assertThat
argument_list|(
name|clusterService
argument_list|()
operator|.
name|state
argument_list|()
operator|.
name|metaData
argument_list|()
operator|.
name|resolveIndexRouting
argument_list|(
literal|"1"
argument_list|,
literal|null
argument_list|,
literal|"alias10"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"0"
argument_list|)
argument_list|)
expr_stmt|;
try|try
block|{
name|clusterService
argument_list|()
operator|.
name|state
argument_list|()
operator|.
name|metaData
argument_list|()
operator|.
name|resolveIndexRouting
argument_list|(
literal|null
argument_list|,
literal|"1"
argument_list|,
literal|"alias10"
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"should fail"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
comment|// all is well, we can't have two mappings, one provided, and one in the alias
block|}
try|try
block|{
name|clusterService
argument_list|()
operator|.
name|state
argument_list|()
operator|.
name|metaData
argument_list|()
operator|.
name|resolveIndexRouting
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
literal|"alias0"
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"should fail"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|ex
parameter_list|)
block|{
comment|// Expected
block|}
block|}
DECL|method|testResolveSearchRouting
specifier|public
name|void
name|testResolveSearchRouting
parameter_list|()
throws|throws
name|Exception
block|{
name|createIndex
argument_list|(
literal|"test1"
argument_list|)
expr_stmt|;
name|createIndex
argument_list|(
literal|"test2"
argument_list|)
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
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareAliases
argument_list|()
operator|.
name|addAliasAction
argument_list|(
name|AliasActions
operator|.
name|add
argument_list|()
operator|.
name|index
argument_list|(
literal|"test1"
argument_list|)
operator|.
name|alias
argument_list|(
literal|"alias"
argument_list|)
argument_list|)
operator|.
name|addAliasAction
argument_list|(
name|AliasActions
operator|.
name|add
argument_list|()
operator|.
name|index
argument_list|(
literal|"test1"
argument_list|)
operator|.
name|alias
argument_list|(
literal|"alias10"
argument_list|)
operator|.
name|routing
argument_list|(
literal|"0"
argument_list|)
argument_list|)
operator|.
name|addAliasAction
argument_list|(
name|AliasActions
operator|.
name|add
argument_list|()
operator|.
name|index
argument_list|(
literal|"test2"
argument_list|)
operator|.
name|alias
argument_list|(
literal|"alias20"
argument_list|)
operator|.
name|routing
argument_list|(
literal|"0"
argument_list|)
argument_list|)
operator|.
name|addAliasAction
argument_list|(
name|AliasActions
operator|.
name|add
argument_list|()
operator|.
name|index
argument_list|(
literal|"test2"
argument_list|)
operator|.
name|alias
argument_list|(
literal|"alias21"
argument_list|)
operator|.
name|routing
argument_list|(
literal|"1"
argument_list|)
argument_list|)
operator|.
name|addAliasAction
argument_list|(
name|AliasActions
operator|.
name|add
argument_list|()
operator|.
name|index
argument_list|(
literal|"test1"
argument_list|)
operator|.
name|alias
argument_list|(
literal|"alias0"
argument_list|)
operator|.
name|routing
argument_list|(
literal|"0"
argument_list|)
argument_list|)
operator|.
name|addAliasAction
argument_list|(
name|AliasActions
operator|.
name|add
argument_list|()
operator|.
name|index
argument_list|(
literal|"test2"
argument_list|)
operator|.
name|alias
argument_list|(
literal|"alias0"
argument_list|)
operator|.
name|routing
argument_list|(
literal|"0"
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|ClusterState
name|state
init|=
name|clusterService
argument_list|()
operator|.
name|state
argument_list|()
decl_stmt|;
name|IndexNameExpressionResolver
name|indexNameExpressionResolver
init|=
name|internalCluster
argument_list|()
operator|.
name|getInstance
argument_list|(
name|IndexNameExpressionResolver
operator|.
name|class
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|indexNameExpressionResolver
operator|.
name|resolveSearchRouting
argument_list|(
name|state
argument_list|,
literal|null
argument_list|,
literal|"alias"
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|indexNameExpressionResolver
operator|.
name|resolveSearchRouting
argument_list|(
name|state
argument_list|,
literal|"0,1"
argument_list|,
literal|"alias"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|newMap
argument_list|(
literal|"test1"
argument_list|,
name|newSet
argument_list|(
literal|"0"
argument_list|,
literal|"1"
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|indexNameExpressionResolver
operator|.
name|resolveSearchRouting
argument_list|(
name|state
argument_list|,
literal|null
argument_list|,
literal|"alias10"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|newMap
argument_list|(
literal|"test1"
argument_list|,
name|newSet
argument_list|(
literal|"0"
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|indexNameExpressionResolver
operator|.
name|resolveSearchRouting
argument_list|(
name|state
argument_list|,
literal|null
argument_list|,
literal|"alias10"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|newMap
argument_list|(
literal|"test1"
argument_list|,
name|newSet
argument_list|(
literal|"0"
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|indexNameExpressionResolver
operator|.
name|resolveSearchRouting
argument_list|(
name|state
argument_list|,
literal|"0"
argument_list|,
literal|"alias10"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|newMap
argument_list|(
literal|"test1"
argument_list|,
name|newSet
argument_list|(
literal|"0"
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|indexNameExpressionResolver
operator|.
name|resolveSearchRouting
argument_list|(
name|state
argument_list|,
literal|"1"
argument_list|,
literal|"alias10"
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|indexNameExpressionResolver
operator|.
name|resolveSearchRouting
argument_list|(
name|state
argument_list|,
literal|null
argument_list|,
literal|"alias0"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|newMap
argument_list|(
literal|"test1"
argument_list|,
name|newSet
argument_list|(
literal|"0"
argument_list|)
argument_list|,
literal|"test2"
argument_list|,
name|newSet
argument_list|(
literal|"0"
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|indexNameExpressionResolver
operator|.
name|resolveSearchRouting
argument_list|(
name|state
argument_list|,
literal|null
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"alias10"
block|,
literal|"alias20"
block|}
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|newMap
argument_list|(
literal|"test1"
argument_list|,
name|newSet
argument_list|(
literal|"0"
argument_list|)
argument_list|,
literal|"test2"
argument_list|,
name|newSet
argument_list|(
literal|"0"
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|indexNameExpressionResolver
operator|.
name|resolveSearchRouting
argument_list|(
name|state
argument_list|,
literal|null
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"alias10"
block|,
literal|"alias21"
block|}
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|newMap
argument_list|(
literal|"test1"
argument_list|,
name|newSet
argument_list|(
literal|"0"
argument_list|)
argument_list|,
literal|"test2"
argument_list|,
name|newSet
argument_list|(
literal|"1"
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|indexNameExpressionResolver
operator|.
name|resolveSearchRouting
argument_list|(
name|state
argument_list|,
literal|null
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"alias20"
block|,
literal|"alias21"
block|}
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|newMap
argument_list|(
literal|"test2"
argument_list|,
name|newSet
argument_list|(
literal|"0"
argument_list|,
literal|"1"
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|indexNameExpressionResolver
operator|.
name|resolveSearchRouting
argument_list|(
name|state
argument_list|,
literal|null
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"test1"
block|,
literal|"alias10"
block|}
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|indexNameExpressionResolver
operator|.
name|resolveSearchRouting
argument_list|(
name|state
argument_list|,
literal|null
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"alias10"
block|,
literal|"test1"
block|}
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|indexNameExpressionResolver
operator|.
name|resolveSearchRouting
argument_list|(
name|state
argument_list|,
literal|"0"
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"alias10"
block|,
literal|"alias20"
block|}
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|newMap
argument_list|(
literal|"test1"
argument_list|,
name|newSet
argument_list|(
literal|"0"
argument_list|)
argument_list|,
literal|"test2"
argument_list|,
name|newSet
argument_list|(
literal|"0"
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|indexNameExpressionResolver
operator|.
name|resolveSearchRouting
argument_list|(
name|state
argument_list|,
literal|"0,1"
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"alias10"
block|,
literal|"alias20"
block|}
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|newMap
argument_list|(
literal|"test1"
argument_list|,
name|newSet
argument_list|(
literal|"0"
argument_list|)
argument_list|,
literal|"test2"
argument_list|,
name|newSet
argument_list|(
literal|"0"
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|indexNameExpressionResolver
operator|.
name|resolveSearchRouting
argument_list|(
name|state
argument_list|,
literal|"1"
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"alias10"
block|,
literal|"alias20"
block|}
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|indexNameExpressionResolver
operator|.
name|resolveSearchRouting
argument_list|(
name|state
argument_list|,
literal|"0"
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"alias10"
block|,
literal|"alias21"
block|}
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|newMap
argument_list|(
literal|"test1"
argument_list|,
name|newSet
argument_list|(
literal|"0"
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|indexNameExpressionResolver
operator|.
name|resolveSearchRouting
argument_list|(
name|state
argument_list|,
literal|"1"
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"alias10"
block|,
literal|"alias21"
block|}
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|newMap
argument_list|(
literal|"test2"
argument_list|,
name|newSet
argument_list|(
literal|"1"
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|indexNameExpressionResolver
operator|.
name|resolveSearchRouting
argument_list|(
name|state
argument_list|,
literal|"0,1,2"
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"alias10"
block|,
literal|"alias21"
block|}
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|newMap
argument_list|(
literal|"test1"
argument_list|,
name|newSet
argument_list|(
literal|"0"
argument_list|)
argument_list|,
literal|"test2"
argument_list|,
name|newSet
argument_list|(
literal|"1"
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|indexNameExpressionResolver
operator|.
name|resolveSearchRouting
argument_list|(
name|state
argument_list|,
literal|"0,1,2"
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"test1"
block|,
literal|"alias10"
block|,
literal|"alias21"
block|}
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|newMap
argument_list|(
literal|"test1"
argument_list|,
name|newSet
argument_list|(
literal|"0"
argument_list|,
literal|"1"
argument_list|,
literal|"2"
argument_list|)
argument_list|,
literal|"test2"
argument_list|,
name|newSet
argument_list|(
literal|"1"
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|newSet
specifier|private
parameter_list|<
name|T
parameter_list|>
name|Set
argument_list|<
name|T
argument_list|>
name|newSet
parameter_list|(
name|T
modifier|...
name|elements
parameter_list|)
block|{
return|return
name|newHashSet
argument_list|(
name|elements
argument_list|)
return|;
block|}
DECL|method|newMap
specifier|private
parameter_list|<
name|K
parameter_list|,
name|V
parameter_list|>
name|Map
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
name|newMap
parameter_list|(
name|K
name|key
parameter_list|,
name|V
name|value
parameter_list|)
block|{
name|Map
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
name|r
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|r
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
return|return
name|r
return|;
block|}
DECL|method|newMap
specifier|private
parameter_list|<
name|K
parameter_list|,
name|V
parameter_list|>
name|Map
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
name|newMap
parameter_list|(
name|K
name|key1
parameter_list|,
name|V
name|value1
parameter_list|,
name|K
name|key2
parameter_list|,
name|V
name|value2
parameter_list|)
block|{
name|Map
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
name|r
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|r
operator|.
name|put
argument_list|(
name|key1
argument_list|,
name|value1
argument_list|)
expr_stmt|;
name|r
operator|.
name|put
argument_list|(
name|key2
argument_list|,
name|value2
argument_list|)
expr_stmt|;
return|return
name|r
return|;
block|}
block|}
end_class

end_unit

