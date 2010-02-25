begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.shard
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|shard
package|;
end_package

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
name|index
operator|.
name|analysis
operator|.
name|AnalysisService
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
name|cache
operator|.
name|filter
operator|.
name|FilterCache
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
name|cache
operator|.
name|filter
operator|.
name|none
operator|.
name|NoneFilterCache
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
name|deletionpolicy
operator|.
name|KeepOnlyLastDeletionPolicy
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
name|deletionpolicy
operator|.
name|SnapshotDeletionPolicy
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
name|Engine
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
name|robin
operator|.
name|RobinEngine
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
name|index
operator|.
name|merge
operator|.
name|policy
operator|.
name|LogByteSizeMergePolicyProvider
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
name|scheduler
operator|.
name|SerialMergeSchedulerProvider
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
name|IndexQueryParserService
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
name|IndexShard
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
name|index
operator|.
name|similarity
operator|.
name|SimilarityService
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
name|store
operator|.
name|Store
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
name|store
operator|.
name|ram
operator|.
name|RamStore
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
name|translog
operator|.
name|memory
operator|.
name|MemoryTranslog
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|threadpool
operator|.
name|ThreadPool
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|threadpool
operator|.
name|dynamic
operator|.
name|DynamicThreadPool
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|Unicode
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
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
name|testng
operator|.
name|annotations
operator|.
name|AfterMethod
import|;
end_import

begin_import
import|import
name|org
operator|.
name|testng
operator|.
name|annotations
operator|.
name|BeforeMethod
import|;
end_import

begin_import
import|import
name|org
operator|.
name|testng
operator|.
name|annotations
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
name|index
operator|.
name|query
operator|.
name|json
operator|.
name|JsonQueryBuilders
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
name|util
operator|.
name|settings
operator|.
name|ImmutableSettings
operator|.
name|Builder
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
name|MatcherAssert
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
name|*
import|;
end_import

begin_comment
comment|/**  * @author kimchy  */
end_comment

begin_class
DECL|class|SimpleIndexShardTests
specifier|public
class|class
name|SimpleIndexShardTests
block|{
DECL|field|threadPool
specifier|private
name|ThreadPool
name|threadPool
decl_stmt|;
DECL|field|indexShard
specifier|private
name|IndexShard
name|indexShard
decl_stmt|;
DECL|method|createIndexShard
annotation|@
name|BeforeMethod
specifier|public
name|void
name|createIndexShard
parameter_list|()
block|{
name|Settings
name|settings
init|=
name|EMPTY_SETTINGS
decl_stmt|;
name|Environment
name|environment
init|=
operator|new
name|Environment
argument_list|(
name|settings
argument_list|)
decl_stmt|;
name|ShardId
name|shardId
init|=
operator|new
name|ShardId
argument_list|(
literal|"test"
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|AnalysisService
name|analysisService
init|=
operator|new
name|AnalysisService
argument_list|(
name|shardId
operator|.
name|index
argument_list|()
argument_list|)
decl_stmt|;
name|MapperService
name|mapperService
init|=
operator|new
name|MapperService
argument_list|(
name|shardId
operator|.
name|index
argument_list|()
argument_list|,
name|settings
argument_list|,
name|environment
argument_list|,
name|analysisService
argument_list|)
decl_stmt|;
name|IndexQueryParserService
name|queryParserService
init|=
operator|new
name|IndexQueryParserService
argument_list|(
name|shardId
operator|.
name|index
argument_list|()
argument_list|,
name|mapperService
argument_list|,
operator|new
name|NoneFilterCache
argument_list|(
name|shardId
operator|.
name|index
argument_list|()
argument_list|,
name|EMPTY_SETTINGS
argument_list|)
argument_list|,
name|analysisService
argument_list|)
decl_stmt|;
name|FilterCache
name|filterCache
init|=
operator|new
name|NoneFilterCache
argument_list|(
name|shardId
operator|.
name|index
argument_list|()
argument_list|,
name|settings
argument_list|)
decl_stmt|;
name|SnapshotDeletionPolicy
name|policy
init|=
operator|new
name|SnapshotDeletionPolicy
argument_list|(
operator|new
name|KeepOnlyLastDeletionPolicy
argument_list|(
name|shardId
argument_list|,
name|settings
argument_list|)
argument_list|)
decl_stmt|;
name|Store
name|store
init|=
operator|new
name|RamStore
argument_list|(
name|shardId
argument_list|,
name|settings
argument_list|)
decl_stmt|;
name|MemoryTranslog
name|translog
init|=
operator|new
name|MemoryTranslog
argument_list|(
name|shardId
argument_list|,
name|settings
argument_list|)
decl_stmt|;
name|Engine
name|engine
init|=
operator|new
name|RobinEngine
argument_list|(
name|shardId
argument_list|,
name|settings
argument_list|,
name|store
argument_list|,
name|policy
argument_list|,
name|translog
argument_list|,
operator|new
name|LogByteSizeMergePolicyProvider
argument_list|(
name|store
argument_list|)
argument_list|,
operator|new
name|SerialMergeSchedulerProvider
argument_list|(
name|shardId
argument_list|,
name|settings
argument_list|)
argument_list|,
name|analysisService
argument_list|,
operator|new
name|SimilarityService
argument_list|(
name|shardId
operator|.
name|index
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|threadPool
operator|=
operator|new
name|DynamicThreadPool
argument_list|()
expr_stmt|;
name|indexShard
operator|=
operator|new
name|InternalIndexShard
argument_list|(
name|shardId
argument_list|,
name|EMPTY_SETTINGS
argument_list|,
name|store
argument_list|,
name|engine
argument_list|,
name|translog
argument_list|,
name|threadPool
argument_list|,
name|mapperService
argument_list|,
name|queryParserService
argument_list|,
name|filterCache
argument_list|)
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
DECL|method|tearDown
annotation|@
name|AfterMethod
specifier|public
name|void
name|tearDown
parameter_list|()
block|{
name|indexShard
operator|.
name|close
argument_list|()
expr_stmt|;
name|threadPool
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
DECL|method|testSimpleIndexGetDelete
annotation|@
name|Test
specifier|public
name|void
name|testSimpleIndexGetDelete
parameter_list|()
block|{
name|String
name|source1
init|=
literal|"{ type1 : { _id : \"1\", name : \"test\", age : 35 } }"
decl_stmt|;
name|indexShard
operator|.
name|index
argument_list|(
literal|"type1"
argument_list|,
literal|"1"
argument_list|,
name|Unicode
operator|.
name|fromStringAsBytes
argument_list|(
name|source1
argument_list|)
argument_list|)
expr_stmt|;
name|indexShard
operator|.
name|refresh
argument_list|(
operator|new
name|Engine
operator|.
name|Refresh
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|sourceFetched
init|=
name|Unicode
operator|.
name|fromBytes
argument_list|(
name|indexShard
operator|.
name|get
argument_list|(
literal|"type1"
argument_list|,
literal|"1"
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|sourceFetched
argument_list|,
name|equalTo
argument_list|(
name|source1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|indexShard
operator|.
name|count
argument_list|(
literal|0
argument_list|,
name|termQuery
argument_list|(
literal|"age"
argument_list|,
literal|35
argument_list|)
operator|.
name|buildAsBytes
argument_list|()
argument_list|,
literal|null
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|1l
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|indexShard
operator|.
name|count
argument_list|(
literal|0
argument_list|,
name|queryString
argument_list|(
literal|"name:test"
argument_list|)
operator|.
name|buildAsBytes
argument_list|()
argument_list|,
literal|null
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|1l
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|indexShard
operator|.
name|count
argument_list|(
literal|0
argument_list|,
name|queryString
argument_list|(
literal|"age:35"
argument_list|)
operator|.
name|buildAsBytes
argument_list|()
argument_list|,
literal|null
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|1l
argument_list|)
argument_list|)
expr_stmt|;
name|indexShard
operator|.
name|delete
argument_list|(
literal|"type1"
argument_list|,
literal|"1"
argument_list|)
expr_stmt|;
name|indexShard
operator|.
name|refresh
argument_list|(
operator|new
name|Engine
operator|.
name|Refresh
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|indexShard
operator|.
name|get
argument_list|(
literal|"type1"
argument_list|,
literal|"1"
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|indexShard
operator|.
name|index
argument_list|(
literal|"type1"
argument_list|,
literal|"1"
argument_list|,
name|Unicode
operator|.
name|fromStringAsBytes
argument_list|(
name|source1
argument_list|)
argument_list|)
expr_stmt|;
name|indexShard
operator|.
name|refresh
argument_list|(
operator|new
name|Engine
operator|.
name|Refresh
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|sourceFetched
operator|=
name|Unicode
operator|.
name|fromBytes
argument_list|(
name|indexShard
operator|.
name|get
argument_list|(
literal|"type1"
argument_list|,
literal|"1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|sourceFetched
argument_list|,
name|equalTo
argument_list|(
name|source1
argument_list|)
argument_list|)
expr_stmt|;
name|indexShard
operator|.
name|deleteByQuery
argument_list|(
name|termQuery
argument_list|(
literal|"name"
argument_list|,
literal|"test"
argument_list|)
operator|.
name|buildAsBytes
argument_list|()
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|indexShard
operator|.
name|refresh
argument_list|(
operator|new
name|Engine
operator|.
name|Refresh
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|indexShard
operator|.
name|get
argument_list|(
literal|"type1"
argument_list|,
literal|"1"
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|indexShard
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

