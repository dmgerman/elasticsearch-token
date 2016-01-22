begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.mapper.murmur3
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
operator|.
name|murmur3
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|LuceneTestCase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|TestUtil
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
name|search
operator|.
name|SearchResponse
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
name|env
operator|.
name|Environment
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|env
operator|.
name|NodeEnvironment
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|plugin
operator|.
name|mapper
operator|.
name|MapperMurmur3Plugin
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|plugins
operator|.
name|Plugin
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|AggregationBuilders
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|metrics
operator|.
name|cardinality
operator|.
name|Cardinality
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
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|InternalTestCluster
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
name|hamcrest
operator|.
name|ElasticsearchAssertions
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|InputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|file
operator|.
name|Files
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|file
operator|.
name|Path
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
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
name|concurrent
operator|.
name|ExecutionException
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
argument_list|,
name|numDataNodes
operator|=
literal|0
argument_list|)
annotation|@
name|LuceneTestCase
operator|.
name|SuppressFileSystems
argument_list|(
literal|"ExtrasFS"
argument_list|)
DECL|class|Murmur3FieldMapperUpgradeTests
specifier|public
class|class
name|Murmur3FieldMapperUpgradeTests
extends|extends
name|ESIntegTestCase
block|{
annotation|@
name|Override
DECL|method|nodePlugins
specifier|protected
name|Collection
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|Plugin
argument_list|>
argument_list|>
name|nodePlugins
parameter_list|()
block|{
return|return
name|Collections
operator|.
name|singleton
argument_list|(
name|MapperMurmur3Plugin
operator|.
name|class
argument_list|)
return|;
block|}
DECL|method|testUpgradeOldMapping
specifier|public
name|void
name|testUpgradeOldMapping
parameter_list|()
throws|throws
name|IOException
throws|,
name|ExecutionException
throws|,
name|InterruptedException
block|{
specifier|final
name|String
name|indexName
init|=
literal|"index-mapper-murmur3-2.0.0"
decl_stmt|;
name|InternalTestCluster
operator|.
name|Async
argument_list|<
name|String
argument_list|>
name|master
init|=
name|internalCluster
argument_list|()
operator|.
name|startNodeAsync
argument_list|()
decl_stmt|;
name|Path
name|unzipDir
init|=
name|createTempDir
argument_list|()
decl_stmt|;
name|Path
name|unzipDataDir
init|=
name|unzipDir
operator|.
name|resolve
argument_list|(
literal|"data"
argument_list|)
decl_stmt|;
name|Path
name|backwardsIndex
init|=
name|getBwcIndicesPath
argument_list|()
operator|.
name|resolve
argument_list|(
name|indexName
operator|+
literal|".zip"
argument_list|)
decl_stmt|;
try|try
init|(
name|InputStream
name|stream
init|=
name|Files
operator|.
name|newInputStream
argument_list|(
name|backwardsIndex
argument_list|)
init|)
block|{
name|TestUtil
operator|.
name|unzip
argument_list|(
name|stream
argument_list|,
name|unzipDir
argument_list|)
expr_stmt|;
block|}
name|assertTrue
argument_list|(
name|Files
operator|.
name|exists
argument_list|(
name|unzipDataDir
argument_list|)
argument_list|)
expr_stmt|;
name|Path
name|dataPath
init|=
name|createTempDir
argument_list|()
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
name|Environment
operator|.
name|PATH_DATA_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
name|dataPath
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
specifier|final
name|String
name|node
init|=
name|internalCluster
argument_list|()
operator|.
name|startDataOnlyNode
argument_list|(
name|settings
argument_list|)
decl_stmt|;
comment|// workaround for dangling index loading issue when node is master
name|Path
index|[]
name|nodePaths
init|=
name|internalCluster
argument_list|()
operator|.
name|getInstance
argument_list|(
name|NodeEnvironment
operator|.
name|class
argument_list|,
name|node
argument_list|)
operator|.
name|nodeDataPaths
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|nodePaths
operator|.
name|length
argument_list|)
expr_stmt|;
name|dataPath
operator|=
name|nodePaths
index|[
literal|0
index|]
operator|.
name|resolve
argument_list|(
name|NodeEnvironment
operator|.
name|INDICES_FOLDER
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|Files
operator|.
name|exists
argument_list|(
name|dataPath
argument_list|)
argument_list|)
expr_stmt|;
name|Path
name|src
init|=
name|unzipDataDir
operator|.
name|resolve
argument_list|(
name|indexName
operator|+
literal|"/nodes/0/indices"
argument_list|)
decl_stmt|;
name|Files
operator|.
name|move
argument_list|(
name|src
argument_list|,
name|dataPath
argument_list|)
expr_stmt|;
name|master
operator|.
name|get
argument_list|()
expr_stmt|;
comment|// force reloading dangling indices with a cluster state republish
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareReroute
argument_list|()
operator|.
name|get
argument_list|()
expr_stmt|;
name|ensureGreen
argument_list|(
name|indexName
argument_list|)
expr_stmt|;
specifier|final
name|SearchResponse
name|countResponse
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
name|indexName
argument_list|)
operator|.
name|setSize
argument_list|(
literal|0
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|ElasticsearchAssertions
operator|.
name|assertHitCount
argument_list|(
name|countResponse
argument_list|,
literal|3L
argument_list|)
expr_stmt|;
specifier|final
name|SearchResponse
name|cardinalityResponse
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
name|indexName
argument_list|)
operator|.
name|addAggregation
argument_list|(
name|AggregationBuilders
operator|.
name|cardinality
argument_list|(
literal|"card"
argument_list|)
operator|.
name|field
argument_list|(
literal|"foo.hash"
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|Cardinality
name|cardinality
init|=
name|cardinalityResponse
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"card"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|3L
argument_list|,
name|cardinality
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

