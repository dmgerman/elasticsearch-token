begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
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
name|search
operator|.
name|MatchAllDocsQuery
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
name|search
operator|.
name|TopDocs
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
name|compress
operator|.
name|CompressedXContent
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
name|xcontent
operator|.
name|ToXContent
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
name|xcontent
operator|.
name|XContentBuilder
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
name|xcontent
operator|.
name|XContentFactory
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
name|xcontent
operator|.
name|XContentType
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
name|query
operator|.
name|QueryBuilder
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
name|translog
operator|.
name|Translog
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
name|ESSingleNodeTestCase
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
name|concurrent
operator|.
name|CountDownLatch
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
name|atomic
operator|.
name|AtomicInteger
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
name|atomic
operator|.
name|AtomicReference
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|metadata
operator|.
name|IndexMetaData
operator|.
name|SETTING_NUMBER_OF_REPLICAS
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|metadata
operator|.
name|IndexMetaData
operator|.
name|SETTING_NUMBER_OF_SHARDS
import|;
end_import

begin_comment
comment|/** Unit test(s) for IndexService */
end_comment

begin_class
DECL|class|IndexServiceTests
specifier|public
class|class
name|IndexServiceTests
extends|extends
name|ESSingleNodeTestCase
block|{
DECL|method|testDetermineShadowEngineShouldBeUsed
specifier|public
name|void
name|testDetermineShadowEngineShouldBeUsed
parameter_list|()
block|{
name|IndexSettings
name|regularSettings
init|=
operator|new
name|IndexSettings
argument_list|(
name|IndexMetaData
operator|.
name|builder
argument_list|(
literal|"regular"
argument_list|)
operator|.
name|settings
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|SETTING_NUMBER_OF_SHARDS
argument_list|,
literal|2
argument_list|)
operator|.
name|put
argument_list|(
name|SETTING_NUMBER_OF_REPLICAS
argument_list|,
literal|1
argument_list|)
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
name|build
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
argument_list|,
name|Settings
operator|.
name|EMPTY
argument_list|)
decl_stmt|;
name|IndexSettings
name|shadowSettings
init|=
operator|new
name|IndexSettings
argument_list|(
name|IndexMetaData
operator|.
name|builder
argument_list|(
literal|"shadow"
argument_list|)
operator|.
name|settings
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|SETTING_NUMBER_OF_SHARDS
argument_list|,
literal|2
argument_list|)
operator|.
name|put
argument_list|(
name|SETTING_NUMBER_OF_REPLICAS
argument_list|,
literal|1
argument_list|)
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_SHADOW_REPLICAS
argument_list|,
literal|true
argument_list|)
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
name|build
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
argument_list|,
name|Settings
operator|.
name|EMPTY
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
literal|"no shadow replicas for normal settings"
argument_list|,
name|IndexService
operator|.
name|useShadowEngine
argument_list|(
literal|true
argument_list|,
name|regularSettings
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
literal|"no shadow replicas for normal settings"
argument_list|,
name|IndexService
operator|.
name|useShadowEngine
argument_list|(
literal|false
argument_list|,
name|regularSettings
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
literal|"no shadow replicas for primary shard with shadow settings"
argument_list|,
name|IndexService
operator|.
name|useShadowEngine
argument_list|(
literal|true
argument_list|,
name|shadowSettings
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"shadow replicas for replica shards with shadow settings"
argument_list|,
name|IndexService
operator|.
name|useShadowEngine
argument_list|(
literal|false
argument_list|,
name|shadowSettings
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|filter
specifier|public
specifier|static
name|CompressedXContent
name|filter
parameter_list|(
name|QueryBuilder
name|filterBuilder
parameter_list|)
throws|throws
name|IOException
block|{
name|XContentBuilder
name|builder
init|=
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
decl_stmt|;
name|filterBuilder
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|ToXContent
operator|.
name|EMPTY_PARAMS
argument_list|)
expr_stmt|;
name|builder
operator|.
name|close
argument_list|()
expr_stmt|;
return|return
operator|new
name|CompressedXContent
argument_list|(
name|builder
operator|.
name|string
argument_list|()
argument_list|)
return|;
block|}
DECL|method|testBaseAsyncTask
specifier|public
name|void
name|testBaseAsyncTask
parameter_list|()
throws|throws
name|InterruptedException
throws|,
name|IOException
block|{
name|IndexService
name|indexService
init|=
name|createIndex
argument_list|(
literal|"test"
argument_list|,
name|Settings
operator|.
name|EMPTY
argument_list|)
decl_stmt|;
name|AtomicReference
argument_list|<
name|CountDownLatch
argument_list|>
name|latch
init|=
operator|new
name|AtomicReference
argument_list|<>
argument_list|(
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
argument_list|)
decl_stmt|;
name|AtomicReference
argument_list|<
name|CountDownLatch
argument_list|>
name|latch2
init|=
operator|new
name|AtomicReference
argument_list|<>
argument_list|(
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
argument_list|)
decl_stmt|;
specifier|final
name|AtomicInteger
name|count
init|=
operator|new
name|AtomicInteger
argument_list|()
decl_stmt|;
name|IndexService
operator|.
name|BaseAsyncTask
name|task
init|=
operator|new
name|IndexService
operator|.
name|BaseAsyncTask
argument_list|(
name|indexService
argument_list|,
name|TimeValue
operator|.
name|timeValueMillis
argument_list|(
literal|1
argument_list|)
argument_list|)
block|{
annotation|@
name|Override
specifier|protected
name|void
name|runInternal
parameter_list|()
block|{
specifier|final
name|CountDownLatch
name|l1
init|=
name|latch
operator|.
name|get
argument_list|()
decl_stmt|;
specifier|final
name|CountDownLatch
name|l2
init|=
name|latch2
operator|.
name|get
argument_list|()
decl_stmt|;
name|count
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
literal|"generic threadpool is configured"
argument_list|,
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|getName
argument_list|()
operator|.
name|contains
argument_list|(
literal|"[generic]"
argument_list|)
argument_list|)
expr_stmt|;
name|l1
operator|.
name|countDown
argument_list|()
expr_stmt|;
try|try
block|{
name|l2
operator|.
name|await
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|fail
argument_list|(
literal|"interrupted"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
comment|// task can throw exceptions!!
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"foo"
argument_list|)
throw|;
block|}
else|else
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"bar"
argument_list|)
throw|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|protected
name|String
name|getThreadPool
parameter_list|()
block|{
return|return
name|ThreadPool
operator|.
name|Names
operator|.
name|GENERIC
return|;
block|}
block|}
decl_stmt|;
name|latch
operator|.
name|get
argument_list|()
operator|.
name|await
argument_list|()
expr_stmt|;
name|latch
operator|.
name|set
argument_list|(
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|count
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
comment|// here we need to swap first before we let it go otherwise threads might be very fast and run that task twice due to
comment|// random exception and the schedule interval is 1ms
name|latch2
operator|.
name|getAndSet
argument_list|(
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
argument_list|)
operator|.
name|countDown
argument_list|()
expr_stmt|;
name|latch
operator|.
name|get
argument_list|()
operator|.
name|await
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|count
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|task
operator|.
name|close
argument_list|()
expr_stmt|;
name|latch2
operator|.
name|get
argument_list|()
operator|.
name|countDown
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|count
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|task
operator|=
operator|new
name|IndexService
operator|.
name|BaseAsyncTask
argument_list|(
name|indexService
argument_list|,
name|TimeValue
operator|.
name|timeValueMillis
argument_list|(
literal|1000000
argument_list|)
argument_list|)
block|{
annotation|@
name|Override
specifier|protected
name|void
name|runInternal
parameter_list|()
block|{              }
block|}
expr_stmt|;
name|assertTrue
argument_list|(
name|task
operator|.
name|mustReschedule
argument_list|()
argument_list|)
expr_stmt|;
name|indexService
operator|.
name|close
argument_list|(
literal|"simon says"
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
literal|"no shards left"
argument_list|,
name|task
operator|.
name|mustReschedule
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|task
operator|.
name|isScheduled
argument_list|()
argument_list|)
expr_stmt|;
name|task
operator|.
name|close
argument_list|()
expr_stmt|;
name|assertFalse
argument_list|(
name|task
operator|.
name|isScheduled
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testRefreshTaskIsUpdated
specifier|public
name|void
name|testRefreshTaskIsUpdated
parameter_list|()
throws|throws
name|IOException
block|{
name|IndexService
name|indexService
init|=
name|createIndex
argument_list|(
literal|"test"
argument_list|,
name|Settings
operator|.
name|EMPTY
argument_list|)
decl_stmt|;
name|IndexService
operator|.
name|AsyncRefreshTask
name|refreshTask
init|=
name|indexService
operator|.
name|getRefreshTask
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|1000
argument_list|,
name|refreshTask
operator|.
name|getInterval
argument_list|()
operator|.
name|millis
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|indexService
operator|.
name|getRefreshTask
argument_list|()
operator|.
name|mustReschedule
argument_list|()
argument_list|)
expr_stmt|;
comment|// now disable
name|IndexMetaData
name|metaData
init|=
name|IndexMetaData
operator|.
name|builder
argument_list|(
name|indexService
operator|.
name|getMetaData
argument_list|()
argument_list|)
operator|.
name|settings
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|indexService
operator|.
name|getMetaData
argument_list|()
operator|.
name|getSettings
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|IndexSettings
operator|.
name|INDEX_REFRESH_INTERVAL_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
operator|-
literal|1
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|indexService
operator|.
name|updateMetaData
argument_list|(
name|metaData
argument_list|)
expr_stmt|;
name|assertNotSame
argument_list|(
name|refreshTask
argument_list|,
name|indexService
operator|.
name|getRefreshTask
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|refreshTask
operator|.
name|isClosed
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|refreshTask
operator|.
name|isScheduled
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|indexService
operator|.
name|getRefreshTask
argument_list|()
operator|.
name|mustReschedule
argument_list|()
argument_list|)
expr_stmt|;
comment|// set it to 100ms
name|metaData
operator|=
name|IndexMetaData
operator|.
name|builder
argument_list|(
name|indexService
operator|.
name|getMetaData
argument_list|()
argument_list|)
operator|.
name|settings
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|indexService
operator|.
name|getMetaData
argument_list|()
operator|.
name|getSettings
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|IndexSettings
operator|.
name|INDEX_REFRESH_INTERVAL_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
literal|"100ms"
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|indexService
operator|.
name|updateMetaData
argument_list|(
name|metaData
argument_list|)
expr_stmt|;
name|assertNotSame
argument_list|(
name|refreshTask
argument_list|,
name|indexService
operator|.
name|getRefreshTask
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|refreshTask
operator|.
name|isClosed
argument_list|()
argument_list|)
expr_stmt|;
name|refreshTask
operator|=
name|indexService
operator|.
name|getRefreshTask
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
name|refreshTask
operator|.
name|mustReschedule
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|refreshTask
operator|.
name|isScheduled
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|100
argument_list|,
name|refreshTask
operator|.
name|getInterval
argument_list|()
operator|.
name|millis
argument_list|()
argument_list|)
expr_stmt|;
comment|// set it to 200ms
name|metaData
operator|=
name|IndexMetaData
operator|.
name|builder
argument_list|(
name|indexService
operator|.
name|getMetaData
argument_list|()
argument_list|)
operator|.
name|settings
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|indexService
operator|.
name|getMetaData
argument_list|()
operator|.
name|getSettings
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|IndexSettings
operator|.
name|INDEX_REFRESH_INTERVAL_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
literal|"200ms"
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|indexService
operator|.
name|updateMetaData
argument_list|(
name|metaData
argument_list|)
expr_stmt|;
name|assertNotSame
argument_list|(
name|refreshTask
argument_list|,
name|indexService
operator|.
name|getRefreshTask
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|refreshTask
operator|.
name|isClosed
argument_list|()
argument_list|)
expr_stmt|;
name|refreshTask
operator|=
name|indexService
operator|.
name|getRefreshTask
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
name|refreshTask
operator|.
name|mustReschedule
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|refreshTask
operator|.
name|isScheduled
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|200
argument_list|,
name|refreshTask
operator|.
name|getInterval
argument_list|()
operator|.
name|millis
argument_list|()
argument_list|)
expr_stmt|;
comment|// set it to 200ms again
name|metaData
operator|=
name|IndexMetaData
operator|.
name|builder
argument_list|(
name|indexService
operator|.
name|getMetaData
argument_list|()
argument_list|)
operator|.
name|settings
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|indexService
operator|.
name|getMetaData
argument_list|()
operator|.
name|getSettings
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|IndexSettings
operator|.
name|INDEX_REFRESH_INTERVAL_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
literal|"200ms"
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|indexService
operator|.
name|updateMetaData
argument_list|(
name|metaData
argument_list|)
expr_stmt|;
name|assertSame
argument_list|(
name|refreshTask
argument_list|,
name|indexService
operator|.
name|getRefreshTask
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|indexService
operator|.
name|getRefreshTask
argument_list|()
operator|.
name|mustReschedule
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|refreshTask
operator|.
name|isScheduled
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|refreshTask
operator|.
name|isClosed
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|200
argument_list|,
name|refreshTask
operator|.
name|getInterval
argument_list|()
operator|.
name|millis
argument_list|()
argument_list|)
expr_stmt|;
name|indexService
operator|.
name|close
argument_list|(
literal|"simon says"
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|refreshTask
operator|.
name|isScheduled
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|refreshTask
operator|.
name|isClosed
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testFsyncTaskIsRunning
specifier|public
name|void
name|testFsyncTaskIsRunning
parameter_list|()
throws|throws
name|IOException
block|{
name|IndexService
name|indexService
init|=
name|createIndex
argument_list|(
literal|"test"
argument_list|,
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|IndexSettings
operator|.
name|INDEX_TRANSLOG_DURABILITY_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
name|Translog
operator|.
name|Durability
operator|.
name|ASYNC
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|IndexService
operator|.
name|AsyncTranslogFSync
name|fsyncTask
init|=
name|indexService
operator|.
name|getFsyncTask
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
name|fsyncTask
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|5000
argument_list|,
name|fsyncTask
operator|.
name|getInterval
argument_list|()
operator|.
name|millis
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|fsyncTask
operator|.
name|mustReschedule
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|fsyncTask
operator|.
name|isScheduled
argument_list|()
argument_list|)
expr_stmt|;
name|indexService
operator|.
name|close
argument_list|(
literal|"simon says"
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|fsyncTask
operator|.
name|isScheduled
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|fsyncTask
operator|.
name|isClosed
argument_list|()
argument_list|)
expr_stmt|;
name|indexService
operator|=
name|createIndex
argument_list|(
literal|"test1"
argument_list|,
name|Settings
operator|.
name|EMPTY
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|indexService
operator|.
name|getFsyncTask
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testGlobalCheckpointTaskIsRunning
specifier|public
name|void
name|testGlobalCheckpointTaskIsRunning
parameter_list|()
throws|throws
name|IOException
block|{
name|IndexService
name|indexService
init|=
name|createIndex
argument_list|(
literal|"test"
argument_list|,
name|Settings
operator|.
name|EMPTY
argument_list|)
decl_stmt|;
name|IndexService
operator|.
name|AsyncGlobalCheckpointTask
name|task
init|=
name|indexService
operator|.
name|getGlobalCheckpointTask
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
name|task
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|IndexSettings
operator|.
name|INDEX_SEQ_NO_CHECKPOINT_SYNC_INTERVAL
operator|.
name|getDefault
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
argument_list|,
name|task
operator|.
name|getInterval
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|task
operator|.
name|mustReschedule
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|task
operator|.
name|isScheduled
argument_list|()
argument_list|)
expr_stmt|;
name|indexService
operator|.
name|close
argument_list|(
literal|"simon says"
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|task
operator|.
name|isScheduled
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|task
operator|.
name|isClosed
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testRefreshActuallyWorks
specifier|public
name|void
name|testRefreshActuallyWorks
parameter_list|()
throws|throws
name|Exception
block|{
name|IndexService
name|indexService
init|=
name|createIndex
argument_list|(
literal|"test"
argument_list|,
name|Settings
operator|.
name|EMPTY
argument_list|)
decl_stmt|;
name|ensureGreen
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
name|IndexService
operator|.
name|AsyncRefreshTask
name|refreshTask
init|=
name|indexService
operator|.
name|getRefreshTask
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|1000
argument_list|,
name|refreshTask
operator|.
name|getInterval
argument_list|()
operator|.
name|millis
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|indexService
operator|.
name|getRefreshTask
argument_list|()
operator|.
name|mustReschedule
argument_list|()
argument_list|)
expr_stmt|;
comment|// now disable
name|IndexMetaData
name|metaData
init|=
name|IndexMetaData
operator|.
name|builder
argument_list|(
name|indexService
operator|.
name|getMetaData
argument_list|()
argument_list|)
operator|.
name|settings
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|indexService
operator|.
name|getMetaData
argument_list|()
operator|.
name|getSettings
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|IndexSettings
operator|.
name|INDEX_REFRESH_INTERVAL_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
operator|-
literal|1
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|indexService
operator|.
name|updateMetaData
argument_list|(
name|metaData
argument_list|)
expr_stmt|;
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"test"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"{\"foo\": \"bar\"}"
argument_list|,
name|XContentType
operator|.
name|JSON
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|IndexShard
name|shard
init|=
name|indexService
operator|.
name|getShard
argument_list|(
literal|0
argument_list|)
decl_stmt|;
try|try
init|(
name|Engine
operator|.
name|Searcher
name|searcher
init|=
name|shard
operator|.
name|acquireSearcher
argument_list|(
literal|"test"
argument_list|)
init|)
block|{
name|TopDocs
name|search
init|=
name|searcher
operator|.
name|searcher
argument_list|()
operator|.
name|search
argument_list|(
operator|new
name|MatchAllDocsQuery
argument_list|()
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|search
operator|.
name|totalHits
argument_list|)
expr_stmt|;
block|}
comment|// refresh every millisecond
name|metaData
operator|=
name|IndexMetaData
operator|.
name|builder
argument_list|(
name|indexService
operator|.
name|getMetaData
argument_list|()
argument_list|)
operator|.
name|settings
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|indexService
operator|.
name|getMetaData
argument_list|()
operator|.
name|getSettings
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|IndexSettings
operator|.
name|INDEX_REFRESH_INTERVAL_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
literal|"1ms"
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|indexService
operator|.
name|updateMetaData
argument_list|(
name|metaData
argument_list|)
expr_stmt|;
name|assertBusy
argument_list|(
parameter_list|()
lambda|->
block|{
try|try
init|(
name|Engine
operator|.
name|Searcher
name|searcher
init|=
name|shard
operator|.
name|acquireSearcher
argument_list|(
literal|"test"
argument_list|)
init|)
block|{
name|TopDocs
name|search
init|=
name|searcher
operator|.
name|searcher
argument_list|()
operator|.
name|search
argument_list|(
operator|new
name|MatchAllDocsQuery
argument_list|()
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|search
operator|.
name|totalHits
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|fail
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
DECL|method|testAsyncFsyncActuallyWorks
specifier|public
name|void
name|testAsyncFsyncActuallyWorks
parameter_list|()
throws|throws
name|Exception
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
name|IndexSettings
operator|.
name|INDEX_TRANSLOG_SYNC_INTERVAL_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
literal|"100ms"
argument_list|)
comment|// very often :)
operator|.
name|put
argument_list|(
name|IndexSettings
operator|.
name|INDEX_TRANSLOG_DURABILITY_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
name|Translog
operator|.
name|Durability
operator|.
name|ASYNC
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|IndexService
name|indexService
init|=
name|createIndex
argument_list|(
literal|"test"
argument_list|,
name|settings
argument_list|)
decl_stmt|;
name|ensureGreen
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|indexService
operator|.
name|getRefreshTask
argument_list|()
operator|.
name|mustReschedule
argument_list|()
argument_list|)
expr_stmt|;
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"test"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"{\"foo\": \"bar\"}"
argument_list|,
name|XContentType
operator|.
name|JSON
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|IndexShard
name|shard
init|=
name|indexService
operator|.
name|getShard
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|assertBusy
argument_list|(
parameter_list|()
lambda|->
block|{
name|assertFalse
argument_list|(
name|shard
operator|.
name|getTranslog
argument_list|()
operator|.
name|syncNeeded
argument_list|()
argument_list|)
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
block|}
DECL|method|testRescheduleAsyncFsync
specifier|public
name|void
name|testRescheduleAsyncFsync
parameter_list|()
throws|throws
name|Exception
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
name|IndexSettings
operator|.
name|INDEX_TRANSLOG_SYNC_INTERVAL_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
literal|"100ms"
argument_list|)
comment|// very often :)
operator|.
name|put
argument_list|(
name|IndexSettings
operator|.
name|INDEX_TRANSLOG_DURABILITY_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
name|Translog
operator|.
name|Durability
operator|.
name|REQUEST
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|IndexService
name|indexService
init|=
name|createIndex
argument_list|(
literal|"test"
argument_list|,
name|settings
argument_list|)
decl_stmt|;
name|ensureGreen
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|indexService
operator|.
name|getFsyncTask
argument_list|()
argument_list|)
expr_stmt|;
name|IndexMetaData
name|metaData
init|=
name|IndexMetaData
operator|.
name|builder
argument_list|(
name|indexService
operator|.
name|getMetaData
argument_list|()
argument_list|)
operator|.
name|settings
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|indexService
operator|.
name|getMetaData
argument_list|()
operator|.
name|getSettings
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|IndexSettings
operator|.
name|INDEX_TRANSLOG_DURABILITY_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
name|Translog
operator|.
name|Durability
operator|.
name|ASYNC
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|indexService
operator|.
name|updateMetaData
argument_list|(
name|metaData
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|indexService
operator|.
name|getFsyncTask
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|indexService
operator|.
name|getRefreshTask
argument_list|()
operator|.
name|mustReschedule
argument_list|()
argument_list|)
expr_stmt|;
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"test"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"{\"foo\": \"bar\"}"
argument_list|,
name|XContentType
operator|.
name|JSON
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|IndexShard
name|shard
init|=
name|indexService
operator|.
name|getShard
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|assertBusy
argument_list|(
parameter_list|()
lambda|->
block|{
name|assertFalse
argument_list|(
name|shard
operator|.
name|getTranslog
argument_list|()
operator|.
name|syncNeeded
argument_list|()
argument_list|)
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
name|metaData
operator|=
name|IndexMetaData
operator|.
name|builder
argument_list|(
name|indexService
operator|.
name|getMetaData
argument_list|()
argument_list|)
operator|.
name|settings
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|indexService
operator|.
name|getMetaData
argument_list|()
operator|.
name|getSettings
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|IndexSettings
operator|.
name|INDEX_TRANSLOG_DURABILITY_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
name|Translog
operator|.
name|Durability
operator|.
name|REQUEST
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|indexService
operator|.
name|updateMetaData
argument_list|(
name|metaData
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|indexService
operator|.
name|getFsyncTask
argument_list|()
argument_list|)
expr_stmt|;
name|metaData
operator|=
name|IndexMetaData
operator|.
name|builder
argument_list|(
name|indexService
operator|.
name|getMetaData
argument_list|()
argument_list|)
operator|.
name|settings
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|indexService
operator|.
name|getMetaData
argument_list|()
operator|.
name|getSettings
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|IndexSettings
operator|.
name|INDEX_TRANSLOG_DURABILITY_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
name|Translog
operator|.
name|Durability
operator|.
name|ASYNC
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|indexService
operator|.
name|updateMetaData
argument_list|(
name|metaData
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|indexService
operator|.
name|getFsyncTask
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testIllegalFsyncInterval
specifier|public
name|void
name|testIllegalFsyncInterval
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
name|IndexSettings
operator|.
name|INDEX_TRANSLOG_SYNC_INTERVAL_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
literal|"0ms"
argument_list|)
comment|// disable
operator|.
name|build
argument_list|()
decl_stmt|;
try|try
block|{
name|createIndex
argument_list|(
literal|"test"
argument_list|,
name|settings
argument_list|)
expr_stmt|;
name|fail
argument_list|()
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
literal|"Failed to parse value [0ms] for setting [index.translog.sync_interval] must be>= 100ms"
argument_list|,
name|ex
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

