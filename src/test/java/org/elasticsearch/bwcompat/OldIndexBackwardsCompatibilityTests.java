begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.bwcompat
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|bwcompat
package|;
end_package

begin_import
import|import
name|com
operator|.
name|carrotsearch
operator|.
name|randomizedtesting
operator|.
name|LifecycleScope
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ListenableFuture
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
name|get
operator|.
name|GetIndexResponse
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
name|action
operator|.
name|search
operator|.
name|SearchRequestBuilder
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
name|io
operator|.
name|FileSystemUtils
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
name|index
operator|.
name|engine
operator|.
name|EngineConfig
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
name|merge
operator|.
name|policy
operator|.
name|MergePolicyModule
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
name|query
operator|.
name|FilterBuilders
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
name|query
operator|.
name|QueryBuilders
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|node
operator|.
name|Node
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|action
operator|.
name|admin
operator|.
name|indices
operator|.
name|upgrade
operator|.
name|UpgradeTest
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
name|SearchHit
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
name|bucket
operator|.
name|histogram
operator|.
name|Histogram
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
name|bucket
operator|.
name|terms
operator|.
name|Terms
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
name|sort
operator|.
name|SortOrder
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
name|hamcrest
operator|.
name|ElasticsearchAssertions
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
name|index
operator|.
name|merge
operator|.
name|NoMergePolicyProvider
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
name|rest
operator|.
name|client
operator|.
name|http
operator|.
name|HttpRequestBuilder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|AfterClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|BeforeClass
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
name|lang
operator|.
name|reflect
operator|.
name|Modifier
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URL
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
name|DirectoryStream
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
name|nio
operator|.
name|file
operator|.
name|Paths
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|*
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
name|CoreMatchers
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
name|greaterThanOrEqualTo
import|;
end_import

begin_class
annotation|@
name|LuceneTestCase
operator|.
name|SuppressCodecs
argument_list|(
block|{
literal|"Lucene3x"
block|,
literal|"MockFixedIntBlock"
block|,
literal|"MockVariableIntBlock"
block|,
literal|"MockSep"
block|,
literal|"MockRandom"
block|,
literal|"Lucene40"
block|,
literal|"Lucene41"
block|,
literal|"Appending"
block|,
literal|"Lucene42"
block|,
literal|"Lucene45"
block|,
literal|"Lucene46"
block|,
literal|"Lucene49"
block|}
argument_list|)
annotation|@
name|ElasticsearchIntegrationTest
operator|.
name|ClusterScope
argument_list|(
name|scope
operator|=
name|ElasticsearchIntegrationTest
operator|.
name|Scope
operator|.
name|TEST
argument_list|,
name|numDataNodes
operator|=
literal|0
argument_list|)
DECL|class|OldIndexBackwardsCompatibilityTests
specifier|public
class|class
name|OldIndexBackwardsCompatibilityTests
extends|extends
name|ElasticsearchIntegrationTest
block|{
comment|// TODO: test for proper exception on unsupported indexes (maybe via separate test?)
comment|// We have a 0.20.6.zip etc for this.
DECL|field|indexes
specifier|static
name|List
argument_list|<
name|String
argument_list|>
name|indexes
decl_stmt|;
DECL|field|indicesDir
specifier|static
name|Path
name|indicesDir
decl_stmt|;
annotation|@
name|BeforeClass
DECL|method|initIndexesList
specifier|public
specifier|static
name|void
name|initIndexesList
parameter_list|()
throws|throws
name|Exception
block|{
name|indexes
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
name|URL
name|dirUrl
init|=
name|OldIndexBackwardsCompatibilityTests
operator|.
name|class
operator|.
name|getResource
argument_list|(
literal|"."
argument_list|)
decl_stmt|;
name|Path
name|dir
init|=
name|Paths
operator|.
name|get
argument_list|(
name|dirUrl
operator|.
name|toURI
argument_list|()
argument_list|)
decl_stmt|;
try|try
init|(
name|DirectoryStream
argument_list|<
name|Path
argument_list|>
name|stream
init|=
name|Files
operator|.
name|newDirectoryStream
argument_list|(
name|dir
argument_list|,
literal|"index-*.zip"
argument_list|)
init|)
block|{
for|for
control|(
name|Path
name|path
range|:
name|stream
control|)
block|{
name|indexes
operator|.
name|add
argument_list|(
name|path
operator|.
name|getFileName
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|Collections
operator|.
name|sort
argument_list|(
name|indexes
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
DECL|method|tearDownStatics
specifier|public
specifier|static
name|void
name|tearDownStatics
parameter_list|()
block|{
name|indexes
operator|=
literal|null
expr_stmt|;
name|indicesDir
operator|=
literal|null
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|nodeSettings
specifier|public
name|Settings
name|nodeSettings
parameter_list|(
name|int
name|ord
parameter_list|)
block|{
return|return
name|ImmutableSettings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|Node
operator|.
name|HTTP_ENABLED
argument_list|,
literal|true
argument_list|)
comment|// for _upgrade
operator|.
name|put
argument_list|(
name|MergePolicyModule
operator|.
name|MERGE_POLICY_TYPE_KEY
argument_list|,
name|NoMergePolicyProvider
operator|.
name|class
argument_list|)
comment|// disable merging so no segments will be upgraded
operator|.
name|build
argument_list|()
return|;
block|}
DECL|method|setupCluster
name|void
name|setupCluster
parameter_list|()
throws|throws
name|Exception
block|{
name|ListenableFuture
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|replicas
init|=
name|internalCluster
argument_list|()
operator|.
name|startNodesAsync
argument_list|(
literal|2
argument_list|)
decl_stmt|;
comment|// for replicas
name|Path
name|dataDir
init|=
name|newTempDirPath
argument_list|(
name|LifecycleScope
operator|.
name|SUITE
argument_list|)
decl_stmt|;
name|ImmutableSettings
operator|.
name|Builder
name|nodeSettings
init|=
name|ImmutableSettings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"path.data"
argument_list|,
name|dataDir
operator|.
name|toAbsolutePath
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
literal|"node.master"
argument_list|,
literal|false
argument_list|)
decl_stmt|;
comment|// workaround for dangling index loading issue when node is master
name|String
name|loadingNode
init|=
name|internalCluster
argument_list|()
operator|.
name|startNode
argument_list|(
name|nodeSettings
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
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
name|loadingNode
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
name|indicesDir
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
name|indicesDir
argument_list|)
argument_list|)
expr_stmt|;
name|Files
operator|.
name|createDirectories
argument_list|(
name|indicesDir
argument_list|)
expr_stmt|;
name|replicas
operator|.
name|get
argument_list|()
expr_stmt|;
comment|// wait for replicas
block|}
DECL|method|loadIndex
name|String
name|loadIndex
parameter_list|(
name|String
name|indexFile
parameter_list|)
throws|throws
name|Exception
block|{
name|Path
name|unzipDir
init|=
name|newTempDirPath
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
name|String
name|indexName
init|=
name|indexFile
operator|.
name|replace
argument_list|(
literal|".zip"
argument_list|,
literal|""
argument_list|)
operator|.
name|toLowerCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
decl_stmt|;
comment|// decompress the index
name|Path
name|backwardsIndex
init|=
name|Paths
operator|.
name|get
argument_list|(
name|getClass
argument_list|()
operator|.
name|getResource
argument_list|(
name|indexFile
argument_list|)
operator|.
name|toURI
argument_list|()
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
comment|// check it is unique
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
index|[]
name|list
init|=
name|FileSystemUtils
operator|.
name|files
argument_list|(
name|unzipDataDir
argument_list|)
decl_stmt|;
if|if
condition|(
name|list
operator|.
name|length
operator|!=
literal|1
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Backwards index must contain exactly one cluster"
argument_list|)
throw|;
block|}
comment|// the bwc scripts packs the indices under this path
name|Path
name|src
init|=
name|list
index|[
literal|0
index|]
operator|.
name|resolve
argument_list|(
literal|"nodes/0/indices/"
operator|+
name|indexName
argument_list|)
decl_stmt|;
name|Path
name|dest
init|=
name|indicesDir
operator|.
name|resolve
argument_list|(
name|indexName
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"["
operator|+
name|indexFile
operator|+
literal|"] missing index dir: "
operator|+
name|src
operator|.
name|toString
argument_list|()
argument_list|,
name|Files
operator|.
name|exists
argument_list|(
name|src
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> injecting index [{}] into path [{}]"
argument_list|,
name|indexName
argument_list|,
name|dest
argument_list|)
expr_stmt|;
name|Files
operator|.
name|move
argument_list|(
name|src
argument_list|,
name|dest
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|Files
operator|.
name|exists
argument_list|(
name|src
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Files
operator|.
name|exists
argument_list|(
name|dest
argument_list|)
argument_list|)
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
return|return
name|indexName
return|;
block|}
DECL|method|unloadIndex
name|void
name|unloadIndex
parameter_list|(
name|String
name|indexName
parameter_list|)
throws|throws
name|Exception
block|{
name|ElasticsearchAssertions
operator|.
name|assertAcked
argument_list|(
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareDelete
argument_list|(
name|indexName
argument_list|)
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|ElasticsearchAssertions
operator|.
name|assertAllFilesClosed
argument_list|()
expr_stmt|;
block|}
DECL|method|testAllVersionsTested
specifier|public
name|void
name|testAllVersionsTested
parameter_list|()
throws|throws
name|Exception
block|{
name|SortedSet
argument_list|<
name|String
argument_list|>
name|expectedVersions
init|=
operator|new
name|TreeSet
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Field
name|field
range|:
name|Version
operator|.
name|class
operator|.
name|getDeclaredFields
argument_list|()
control|)
block|{
if|if
condition|(
name|Modifier
operator|.
name|isStatic
argument_list|(
name|field
operator|.
name|getModifiers
argument_list|()
argument_list|)
operator|&&
name|field
operator|.
name|getType
argument_list|()
operator|==
name|Version
operator|.
name|class
condition|)
block|{
name|Version
name|v
init|=
operator|(
name|Version
operator|)
name|field
operator|.
name|get
argument_list|(
name|Version
operator|.
name|class
argument_list|)
decl_stmt|;
if|if
condition|(
name|v
operator|.
name|snapshot
argument_list|()
condition|)
block|{
continue|continue;
comment|// snapshots are unreleased, so there is no backcompat yet
block|}
if|if
condition|(
name|v
operator|.
name|onOrBefore
argument_list|(
name|Version
operator|.
name|V_0_20_6
argument_list|)
condition|)
block|{
continue|continue;
comment|// we can only test back one major lucene version
block|}
if|if
condition|(
name|v
operator|.
name|equals
argument_list|(
name|Version
operator|.
name|CURRENT
argument_list|)
condition|)
block|{
continue|continue;
comment|// the current version is always compatible with itself
block|}
name|expectedVersions
operator|.
name|add
argument_list|(
literal|"index-"
operator|+
name|v
operator|.
name|toString
argument_list|()
operator|+
literal|".zip"
argument_list|)
expr_stmt|;
block|}
block|}
for|for
control|(
name|String
name|index
range|:
name|indexes
control|)
block|{
if|if
condition|(
name|expectedVersions
operator|.
name|remove
argument_list|(
name|index
argument_list|)
operator|==
literal|false
condition|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"Old indexes tests contain extra index: "
operator|+
name|index
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|expectedVersions
operator|.
name|isEmpty
argument_list|()
operator|==
literal|false
condition|)
block|{
name|StringBuilder
name|msg
init|=
operator|new
name|StringBuilder
argument_list|(
literal|"Old index tests are missing indexes:"
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|expected
range|:
name|expectedVersions
control|)
block|{
name|msg
operator|.
name|append
argument_list|(
literal|"\n"
operator|+
name|expected
argument_list|)
expr_stmt|;
block|}
name|fail
argument_list|(
name|msg
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|LuceneTestCase
operator|.
name|AwaitsFix
argument_list|(
name|bugUrl
operator|=
literal|"times out often , see : https://github.com/elastic/elasticsearch/issues/10434"
argument_list|)
DECL|method|testOldIndexes
specifier|public
name|void
name|testOldIndexes
parameter_list|()
throws|throws
name|Exception
block|{
name|setupCluster
argument_list|()
expr_stmt|;
name|Collections
operator|.
name|shuffle
argument_list|(
name|indexes
argument_list|,
name|getRandom
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|index
range|:
name|indexes
control|)
block|{
name|long
name|startTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> Testing old index "
operator|+
name|index
argument_list|)
expr_stmt|;
name|assertOldIndexWorks
argument_list|(
name|index
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> Done testing "
operator|+
name|index
operator|+
literal|", took "
operator|+
operator|(
operator|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|startTime
operator|)
operator|/
literal|1000.0
operator|)
operator|+
literal|" seconds"
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|assertOldIndexWorks
name|void
name|assertOldIndexWorks
parameter_list|(
name|String
name|index
parameter_list|)
throws|throws
name|Exception
block|{
name|Version
name|version
init|=
name|extractVersion
argument_list|(
name|index
argument_list|)
decl_stmt|;
name|String
name|indexName
init|=
name|loadIndex
argument_list|(
name|index
argument_list|)
decl_stmt|;
name|assertIndexSanity
argument_list|(
name|indexName
argument_list|)
expr_stmt|;
name|assertBasicSearchWorks
argument_list|(
name|indexName
argument_list|)
expr_stmt|;
name|assertBasicAggregationWorks
argument_list|(
name|indexName
argument_list|)
expr_stmt|;
name|assertRealtimeGetWorks
argument_list|(
name|indexName
argument_list|)
expr_stmt|;
name|assertNewReplicasWork
argument_list|(
name|indexName
argument_list|)
expr_stmt|;
name|assertUpgradeWorks
argument_list|(
name|indexName
argument_list|,
name|isLatestLuceneVersion
argument_list|(
name|version
argument_list|)
argument_list|)
expr_stmt|;
name|assertDeleteByQueryWorked
argument_list|(
name|indexName
argument_list|,
name|version
argument_list|)
expr_stmt|;
name|unloadIndex
argument_list|(
name|indexName
argument_list|)
expr_stmt|;
block|}
DECL|method|extractVersion
name|Version
name|extractVersion
parameter_list|(
name|String
name|index
parameter_list|)
block|{
return|return
name|Version
operator|.
name|fromString
argument_list|(
name|index
operator|.
name|substring
argument_list|(
name|index
operator|.
name|indexOf
argument_list|(
literal|'-'
argument_list|)
operator|+
literal|1
argument_list|,
name|index
operator|.
name|lastIndexOf
argument_list|(
literal|'.'
argument_list|)
argument_list|)
argument_list|)
return|;
block|}
DECL|method|isLatestLuceneVersion
name|boolean
name|isLatestLuceneVersion
parameter_list|(
name|Version
name|version
parameter_list|)
block|{
return|return
name|version
operator|.
name|luceneVersion
operator|.
name|major
operator|==
name|Version
operator|.
name|CURRENT
operator|.
name|luceneVersion
operator|.
name|major
operator|&&
name|version
operator|.
name|luceneVersion
operator|.
name|minor
operator|==
name|Version
operator|.
name|CURRENT
operator|.
name|luceneVersion
operator|.
name|minor
return|;
block|}
DECL|method|assertIndexSanity
name|void
name|assertIndexSanity
parameter_list|(
name|String
name|indexName
parameter_list|)
block|{
name|GetIndexResponse
name|getIndexResponse
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
name|prepareGetIndex
argument_list|()
operator|.
name|addIndices
argument_list|(
name|indexName
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|getIndexResponse
operator|.
name|indices
argument_list|()
operator|.
name|length
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|indexName
argument_list|,
name|getIndexResponse
operator|.
name|indices
argument_list|()
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|ensureYellow
argument_list|(
name|indexName
argument_list|)
expr_stmt|;
name|SearchResponse
name|test
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
name|indexName
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|test
operator|.
name|getHits
argument_list|()
operator|.
name|getTotalHits
argument_list|()
argument_list|,
name|greaterThanOrEqualTo
argument_list|(
literal|1l
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|assertBasicSearchWorks
name|void
name|assertBasicSearchWorks
parameter_list|(
name|String
name|indexName
parameter_list|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"--> testing basic search"
argument_list|)
expr_stmt|;
name|SearchRequestBuilder
name|searchReq
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
name|indexName
argument_list|)
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
name|matchAllQuery
argument_list|()
argument_list|)
decl_stmt|;
name|SearchResponse
name|searchRsp
init|=
name|searchReq
operator|.
name|get
argument_list|()
decl_stmt|;
name|ElasticsearchAssertions
operator|.
name|assertNoFailures
argument_list|(
name|searchRsp
argument_list|)
expr_stmt|;
name|long
name|numDocs
init|=
name|searchRsp
operator|.
name|getHits
argument_list|()
operator|.
name|getTotalHits
argument_list|()
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"Found "
operator|+
name|numDocs
operator|+
literal|" in old index"
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> testing basic search with sort"
argument_list|)
expr_stmt|;
name|searchReq
operator|.
name|addSort
argument_list|(
literal|"long_sort"
argument_list|,
name|SortOrder
operator|.
name|ASC
argument_list|)
expr_stmt|;
name|ElasticsearchAssertions
operator|.
name|assertNoFailures
argument_list|(
name|searchReq
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> testing exists filter"
argument_list|)
expr_stmt|;
name|searchReq
operator|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
name|indexName
argument_list|)
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
name|filteredQuery
argument_list|(
name|QueryBuilders
operator|.
name|matchAllQuery
argument_list|()
argument_list|,
name|FilterBuilders
operator|.
name|existsFilter
argument_list|(
literal|"string"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|searchRsp
operator|=
name|searchReq
operator|.
name|get
argument_list|()
expr_stmt|;
name|ElasticsearchAssertions
operator|.
name|assertNoFailures
argument_list|(
name|searchRsp
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|numDocs
argument_list|,
name|equalTo
argument_list|(
name|searchRsp
operator|.
name|getHits
argument_list|()
operator|.
name|getTotalHits
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|assertBasicAggregationWorks
name|void
name|assertBasicAggregationWorks
parameter_list|(
name|String
name|indexName
parameter_list|)
block|{
comment|// histogram on a long
name|SearchResponse
name|searchRsp
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
name|histogram
argument_list|(
literal|"histo"
argument_list|)
operator|.
name|field
argument_list|(
literal|"long_sort"
argument_list|)
operator|.
name|interval
argument_list|(
literal|10
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|ElasticsearchAssertions
operator|.
name|assertSearchResponse
argument_list|(
name|searchRsp
argument_list|)
expr_stmt|;
name|Histogram
name|histo
init|=
name|searchRsp
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"histo"
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|histo
argument_list|)
expr_stmt|;
name|long
name|totalCount
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Histogram
operator|.
name|Bucket
name|bucket
range|:
name|histo
operator|.
name|getBuckets
argument_list|()
control|)
block|{
name|totalCount
operator|+=
name|bucket
operator|.
name|getDocCount
argument_list|()
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|totalCount
argument_list|,
name|searchRsp
operator|.
name|getHits
argument_list|()
operator|.
name|getTotalHits
argument_list|()
argument_list|)
expr_stmt|;
comment|// terms on a boolean
name|searchRsp
operator|=
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
name|terms
argument_list|(
literal|"bool_terms"
argument_list|)
operator|.
name|field
argument_list|(
literal|"bool"
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|Terms
name|terms
init|=
name|searchRsp
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"bool_terms"
argument_list|)
decl_stmt|;
name|totalCount
operator|=
literal|0
expr_stmt|;
for|for
control|(
name|Terms
operator|.
name|Bucket
name|bucket
range|:
name|terms
operator|.
name|getBuckets
argument_list|()
control|)
block|{
name|totalCount
operator|+=
name|bucket
operator|.
name|getDocCount
argument_list|()
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|totalCount
argument_list|,
name|searchRsp
operator|.
name|getHits
argument_list|()
operator|.
name|getTotalHits
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|assertRealtimeGetWorks
name|void
name|assertRealtimeGetWorks
parameter_list|(
name|String
name|indexName
parameter_list|)
block|{
name|assertAcked
argument_list|(
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareUpdateSettings
argument_list|(
name|indexName
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
literal|"refresh_interval"
argument_list|,
operator|-
literal|1
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|SearchRequestBuilder
name|searchReq
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
name|indexName
argument_list|)
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
name|matchAllQuery
argument_list|()
argument_list|)
decl_stmt|;
name|SearchHit
name|hit
init|=
name|searchReq
operator|.
name|get
argument_list|()
operator|.
name|getHits
argument_list|()
operator|.
name|getAt
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|String
name|docId
init|=
name|hit
operator|.
name|getId
argument_list|()
decl_stmt|;
comment|// foo is new, it is not a field in the generated index
name|client
argument_list|()
operator|.
name|prepareUpdate
argument_list|(
name|indexName
argument_list|,
literal|"doc"
argument_list|,
name|docId
argument_list|)
operator|.
name|setDoc
argument_list|(
literal|"foo"
argument_list|,
literal|"bar"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|GetResponse
name|getRsp
init|=
name|client
argument_list|()
operator|.
name|prepareGet
argument_list|(
name|indexName
argument_list|,
literal|"doc"
argument_list|,
name|docId
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|source
init|=
name|getRsp
operator|.
name|getSourceAsMap
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|source
argument_list|,
name|Matchers
operator|.
name|hasKey
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
name|assertAcked
argument_list|(
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareUpdateSettings
argument_list|(
name|indexName
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
literal|"refresh_interval"
argument_list|,
name|EngineConfig
operator|.
name|DEFAULT_REFRESH_INTERVAL
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|assertNewReplicasWork
name|void
name|assertNewReplicasWork
parameter_list|(
name|String
name|indexName
parameter_list|)
throws|throws
name|Exception
block|{
specifier|final
name|int
name|numReplicas
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|2
argument_list|)
decl_stmt|;
name|logger
operator|.
name|debug
argument_list|(
literal|"Creating [{}] replicas for index [{}]"
argument_list|,
name|numReplicas
argument_list|,
name|indexName
argument_list|)
expr_stmt|;
name|assertAcked
argument_list|(
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareUpdateSettings
argument_list|(
name|indexName
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
literal|"number_of_replicas"
argument_list|,
name|numReplicas
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|(
name|indexName
argument_list|)
expr_stmt|;
comment|// TODO: do something with the replicas! query? index?
block|}
comment|// #10067: create-bwc-index.py deleted any doc with long_sort:[10-20]
DECL|method|assertDeleteByQueryWorked
name|void
name|assertDeleteByQueryWorked
parameter_list|(
name|String
name|indexName
parameter_list|,
name|Version
name|version
parameter_list|)
throws|throws
name|Exception
block|{
if|if
condition|(
name|version
operator|.
name|onOrBefore
argument_list|(
name|Version
operator|.
name|V_1_0_0_Beta2
argument_list|)
condition|)
block|{
comment|// TODO: remove this once #10262 is fixed
return|return;
block|}
name|SearchRequestBuilder
name|searchReq
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
name|indexName
argument_list|)
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
name|queryStringQuery
argument_list|(
literal|"long_sort:[10 TO 20]"
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|searchReq
operator|.
name|get
argument_list|()
operator|.
name|getHits
argument_list|()
operator|.
name|getTotalHits
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|assertUpgradeWorks
name|void
name|assertUpgradeWorks
parameter_list|(
name|String
name|indexName
parameter_list|,
name|boolean
name|alreadyLatest
parameter_list|)
throws|throws
name|Exception
block|{
name|HttpRequestBuilder
name|httpClient
init|=
name|httpClient
argument_list|()
decl_stmt|;
if|if
condition|(
name|alreadyLatest
operator|==
literal|false
condition|)
block|{
name|UpgradeTest
operator|.
name|assertNotUpgraded
argument_list|(
name|httpClient
argument_list|,
name|indexName
argument_list|)
expr_stmt|;
block|}
name|UpgradeTest
operator|.
name|runUpgrade
argument_list|(
name|httpClient
argument_list|,
name|indexName
argument_list|)
expr_stmt|;
name|UpgradeTest
operator|.
name|assertUpgraded
argument_list|(
name|httpClient
argument_list|,
name|indexName
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

