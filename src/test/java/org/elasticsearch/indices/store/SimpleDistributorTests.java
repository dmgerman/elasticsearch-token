begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.indices.store
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|store
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
name|store
operator|.
name|Directory
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
name|shard
operator|.
name|service
operator|.
name|InternalIndexShard
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
name|IndicesService
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
import|import
name|java
operator|.
name|io
operator|.
name|File
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
name|util
operator|.
name|Locale
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
name|common
operator|.
name|settings
operator|.
name|ImmutableSettings
operator|.
name|settingsBuilder
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
comment|/**  *  */
end_comment

begin_class
DECL|class|SimpleDistributorTests
specifier|public
class|class
name|SimpleDistributorTests
extends|extends
name|ElasticsearchIntegrationTest
block|{
DECL|field|STORE_TYPES
specifier|public
specifier|final
specifier|static
name|String
index|[]
name|STORE_TYPES
init|=
block|{
literal|"fs"
block|,
literal|"simplefs"
block|,
literal|"niofs"
block|,
literal|"mmapfs"
block|}
decl_stmt|;
annotation|@
name|Test
DECL|method|testAvailableSpaceDetection
specifier|public
name|void
name|testAvailableSpaceDetection
parameter_list|()
block|{
for|for
control|(
name|String
name|store
range|:
name|STORE_TYPES
control|)
block|{
name|createIndexWithStoreType
argument_list|(
literal|"test"
argument_list|,
name|store
argument_list|,
name|StrictDistributor
operator|.
name|class
operator|.
name|getCanonicalName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
DECL|method|testDirectoryToString
specifier|public
name|void
name|testDirectoryToString
parameter_list|()
throws|throws
name|IOException
block|{
name|cluster
argument_list|()
operator|.
name|wipeTemplates
argument_list|()
expr_stmt|;
comment|// no random settings please
name|createIndexWithStoreType
argument_list|(
literal|"test"
argument_list|,
literal|"niofs"
argument_list|,
literal|"least_used"
argument_list|)
expr_stmt|;
name|String
name|storeString
init|=
name|getStoreDirectory
argument_list|(
literal|"test"
argument_list|,
literal|0
argument_list|)
operator|.
name|toString
argument_list|()
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
name|storeString
argument_list|)
expr_stmt|;
name|File
index|[]
name|dataPaths
init|=
name|dataPaths
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|storeString
operator|.
name|toLowerCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
argument_list|,
name|startsWith
argument_list|(
literal|"store(least_used[rate_limited(niofs("
operator|+
name|dataPaths
index|[
literal|0
index|]
operator|.
name|getAbsolutePath
argument_list|()
operator|.
name|toLowerCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|dataPaths
operator|.
name|length
operator|>
literal|1
condition|)
block|{
name|assertThat
argument_list|(
name|storeString
operator|.
name|toLowerCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
argument_list|,
name|containsString
argument_list|(
literal|"), rate_limited(niofs("
operator|+
name|dataPaths
index|[
literal|1
index|]
operator|.
name|getAbsolutePath
argument_list|()
operator|.
name|toLowerCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|assertThat
argument_list|(
name|storeString
argument_list|,
name|endsWith
argument_list|(
literal|", type=MERGE, rate=20.0)])"
argument_list|)
argument_list|)
expr_stmt|;
name|createIndexWithStoreType
argument_list|(
literal|"test"
argument_list|,
literal|"niofs"
argument_list|,
literal|"random"
argument_list|)
expr_stmt|;
name|storeString
operator|=
name|getStoreDirectory
argument_list|(
literal|"test"
argument_list|,
literal|0
argument_list|)
operator|.
name|toString
argument_list|()
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
name|storeString
argument_list|)
expr_stmt|;
name|dataPaths
operator|=
name|dataPaths
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|storeString
operator|.
name|toLowerCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
argument_list|,
name|startsWith
argument_list|(
literal|"store(random[rate_limited(niofs("
operator|+
name|dataPaths
index|[
literal|0
index|]
operator|.
name|getAbsolutePath
argument_list|()
operator|.
name|toLowerCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|dataPaths
operator|.
name|length
operator|>
literal|1
condition|)
block|{
name|assertThat
argument_list|(
name|storeString
operator|.
name|toLowerCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
argument_list|,
name|containsString
argument_list|(
literal|"), rate_limited(niofs("
operator|+
name|dataPaths
index|[
literal|1
index|]
operator|.
name|getAbsolutePath
argument_list|()
operator|.
name|toLowerCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|assertThat
argument_list|(
name|storeString
argument_list|,
name|endsWith
argument_list|(
literal|", type=MERGE, rate=20.0)])"
argument_list|)
argument_list|)
expr_stmt|;
name|createIndexWithStoreType
argument_list|(
literal|"test"
argument_list|,
literal|"mmapfs"
argument_list|,
literal|"least_used"
argument_list|)
expr_stmt|;
name|storeString
operator|=
name|getStoreDirectory
argument_list|(
literal|"test"
argument_list|,
literal|0
argument_list|)
operator|.
name|toString
argument_list|()
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
name|storeString
argument_list|)
expr_stmt|;
name|dataPaths
operator|=
name|dataPaths
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|storeString
operator|.
name|toLowerCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
argument_list|,
name|startsWith
argument_list|(
literal|"store(least_used[rate_limited(mmapfs("
operator|+
name|dataPaths
index|[
literal|0
index|]
operator|.
name|getAbsolutePath
argument_list|()
operator|.
name|toLowerCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|dataPaths
operator|.
name|length
operator|>
literal|1
condition|)
block|{
name|assertThat
argument_list|(
name|storeString
operator|.
name|toLowerCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
argument_list|,
name|containsString
argument_list|(
literal|"), rate_limited(mmapfs("
operator|+
name|dataPaths
index|[
literal|1
index|]
operator|.
name|getAbsolutePath
argument_list|()
operator|.
name|toLowerCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|assertThat
argument_list|(
name|storeString
argument_list|,
name|endsWith
argument_list|(
literal|", type=MERGE, rate=20.0)])"
argument_list|)
argument_list|)
expr_stmt|;
name|createIndexWithStoreType
argument_list|(
literal|"test"
argument_list|,
literal|"simplefs"
argument_list|,
literal|"least_used"
argument_list|)
expr_stmt|;
name|storeString
operator|=
name|getStoreDirectory
argument_list|(
literal|"test"
argument_list|,
literal|0
argument_list|)
operator|.
name|toString
argument_list|()
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
name|storeString
argument_list|)
expr_stmt|;
name|dataPaths
operator|=
name|dataPaths
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|storeString
operator|.
name|toLowerCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
argument_list|,
name|startsWith
argument_list|(
literal|"store(least_used[rate_limited(simplefs("
operator|+
name|dataPaths
index|[
literal|0
index|]
operator|.
name|getAbsolutePath
argument_list|()
operator|.
name|toLowerCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|dataPaths
operator|.
name|length
operator|>
literal|1
condition|)
block|{
name|assertThat
argument_list|(
name|storeString
operator|.
name|toLowerCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
argument_list|,
name|containsString
argument_list|(
literal|"), rate_limited(simplefs("
operator|+
name|dataPaths
index|[
literal|1
index|]
operator|.
name|getAbsolutePath
argument_list|()
operator|.
name|toLowerCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|assertThat
argument_list|(
name|storeString
argument_list|,
name|endsWith
argument_list|(
literal|", type=MERGE, rate=20.0)])"
argument_list|)
argument_list|)
expr_stmt|;
name|createIndexWithStoreType
argument_list|(
literal|"test"
argument_list|,
literal|"memory"
argument_list|,
literal|"least_used"
argument_list|)
expr_stmt|;
name|storeString
operator|=
name|getStoreDirectory
argument_list|(
literal|"test"
argument_list|,
literal|0
argument_list|)
operator|.
name|toString
argument_list|()
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
name|storeString
argument_list|)
expr_stmt|;
name|dataPaths
operator|=
name|dataPaths
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|storeString
argument_list|,
name|equalTo
argument_list|(
literal|"store(least_used[ram])"
argument_list|)
argument_list|)
expr_stmt|;
name|createIndexWithoutRateLimitingStoreType
argument_list|(
literal|"test"
argument_list|,
literal|"niofs"
argument_list|,
literal|"least_used"
argument_list|)
expr_stmt|;
name|storeString
operator|=
name|getStoreDirectory
argument_list|(
literal|"test"
argument_list|,
literal|0
argument_list|)
operator|.
name|toString
argument_list|()
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
name|storeString
argument_list|)
expr_stmt|;
name|dataPaths
operator|=
name|dataPaths
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|storeString
operator|.
name|toLowerCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
argument_list|,
name|startsWith
argument_list|(
literal|"store(least_used[niofs("
operator|+
name|dataPaths
index|[
literal|0
index|]
operator|.
name|getAbsolutePath
argument_list|()
operator|.
name|toLowerCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|dataPaths
operator|.
name|length
operator|>
literal|1
condition|)
block|{
name|assertThat
argument_list|(
name|storeString
operator|.
name|toLowerCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
argument_list|,
name|containsString
argument_list|(
literal|"), niofs("
operator|+
name|dataPaths
index|[
literal|1
index|]
operator|.
name|getAbsolutePath
argument_list|()
operator|.
name|toLowerCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|assertThat
argument_list|(
name|storeString
argument_list|,
name|endsWith
argument_list|(
literal|")])"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|createIndexWithStoreType
specifier|private
name|void
name|createIndexWithStoreType
parameter_list|(
name|String
name|index
parameter_list|,
name|String
name|storeType
parameter_list|,
name|String
name|distributor
parameter_list|)
block|{
name|immutableCluster
argument_list|()
operator|.
name|wipeIndices
argument_list|(
name|index
argument_list|)
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
name|prepareCreate
argument_list|(
name|index
argument_list|)
operator|.
name|setSettings
argument_list|(
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"index.store.distributor"
argument_list|,
name|distributor
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.store.type"
argument_list|,
name|storeType
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.number_of_replicas"
argument_list|,
literal|0
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.number_of_shards"
argument_list|,
literal|1
argument_list|)
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
argument_list|(
literal|"test"
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
operator|.
name|isTimedOut
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|createIndexWithoutRateLimitingStoreType
specifier|private
name|void
name|createIndexWithoutRateLimitingStoreType
parameter_list|(
name|String
name|index
parameter_list|,
name|String
name|storeType
parameter_list|,
name|String
name|distributor
parameter_list|)
block|{
name|immutableCluster
argument_list|()
operator|.
name|wipeIndices
argument_list|(
name|index
argument_list|)
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
name|prepareCreate
argument_list|(
name|index
argument_list|)
operator|.
name|setSettings
argument_list|(
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"index.store.distributor"
argument_list|,
name|distributor
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.store.type"
argument_list|,
name|storeType
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.store.throttle.type"
argument_list|,
literal|"none"
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.number_of_replicas"
argument_list|,
literal|0
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.number_of_shards"
argument_list|,
literal|1
argument_list|)
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
argument_list|(
literal|"test"
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
operator|.
name|isTimedOut
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|dataPaths
specifier|private
name|File
index|[]
name|dataPaths
parameter_list|()
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|nodes
init|=
name|cluster
argument_list|()
operator|.
name|nodesInclude
argument_list|(
literal|"test"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|nodes
operator|.
name|isEmpty
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|NodeEnvironment
name|env
init|=
name|cluster
argument_list|()
operator|.
name|getInstance
argument_list|(
name|NodeEnvironment
operator|.
name|class
argument_list|,
name|nodes
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
argument_list|)
decl_stmt|;
return|return
name|env
operator|.
name|nodeDataLocations
argument_list|()
return|;
block|}
DECL|method|getStoreDirectory
specifier|private
name|Directory
name|getStoreDirectory
parameter_list|(
name|String
name|index
parameter_list|,
name|int
name|shardId
parameter_list|)
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|nodes
init|=
name|cluster
argument_list|()
operator|.
name|nodesInclude
argument_list|(
literal|"test"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|nodes
operator|.
name|isEmpty
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|IndicesService
name|indicesService
init|=
name|cluster
argument_list|()
operator|.
name|getInstance
argument_list|(
name|IndicesService
operator|.
name|class
argument_list|,
name|nodes
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
argument_list|)
decl_stmt|;
name|InternalIndexShard
name|indexShard
init|=
call|(
name|InternalIndexShard
call|)
argument_list|(
name|indicesService
operator|.
name|indexService
argument_list|(
name|index
argument_list|)
operator|.
name|shard
argument_list|(
name|shardId
argument_list|)
argument_list|)
decl_stmt|;
return|return
name|indexShard
operator|.
name|store
argument_list|()
operator|.
name|directory
argument_list|()
return|;
block|}
block|}
end_class

end_unit

