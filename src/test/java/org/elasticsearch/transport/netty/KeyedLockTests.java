begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.transport.netty
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|transport
operator|.
name|netty
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticSearchIllegalStateException
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
name|concurrent
operator|.
name|KeyedLock
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
name|ElasticSearchTestCase
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
name|Test
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
operator|.
name|Entry
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
name|ConcurrentHashMap
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
DECL|class|KeyedLockTests
specifier|public
class|class
name|KeyedLockTests
extends|extends
name|ElasticSearchTestCase
block|{
annotation|@
name|Test
DECL|method|checkIfMapEmptyAfterLotsOfAcquireAndReleases
specifier|public
name|void
name|checkIfMapEmptyAfterLotsOfAcquireAndReleases
parameter_list|()
throws|throws
name|InterruptedException
block|{
name|ConcurrentHashMap
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
name|counter
init|=
operator|new
name|ConcurrentHashMap
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
argument_list|()
decl_stmt|;
name|ConcurrentHashMap
argument_list|<
name|String
argument_list|,
name|AtomicInteger
argument_list|>
name|safeCounter
init|=
operator|new
name|ConcurrentHashMap
argument_list|<
name|String
argument_list|,
name|AtomicInteger
argument_list|>
argument_list|()
decl_stmt|;
name|KeyedLock
argument_list|<
name|String
argument_list|>
name|connectionLock
init|=
operator|new
name|KeyedLock
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|String
index|[]
name|names
init|=
operator|new
name|String
index|[
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|40
argument_list|)
index|]
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|names
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|names
index|[
name|i
index|]
operator|=
name|randomRealisticUnicodeOfLengthBetween
argument_list|(
literal|10
argument_list|,
literal|20
argument_list|)
expr_stmt|;
block|}
name|CountDownLatch
name|startLatch
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|int
name|numThreads
init|=
name|randomIntBetween
argument_list|(
literal|3
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|Thread
index|[]
name|threads
init|=
operator|new
name|Thread
index|[
name|numThreads
index|]
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numThreads
condition|;
name|i
operator|++
control|)
block|{
name|threads
index|[
name|i
index|]
operator|=
operator|new
name|AcquireAndReleaseThread
argument_list|(
name|startLatch
argument_list|,
name|connectionLock
argument_list|,
name|names
argument_list|,
name|counter
argument_list|,
name|safeCounter
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numThreads
condition|;
name|i
operator|++
control|)
block|{
name|threads
index|[
name|i
index|]
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
name|startLatch
operator|.
name|countDown
argument_list|()
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numThreads
condition|;
name|i
operator|++
control|)
block|{
name|threads
index|[
name|i
index|]
operator|.
name|join
argument_list|()
expr_stmt|;
block|}
name|assertThat
argument_list|(
name|connectionLock
operator|.
name|hasLockedKeys
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|Set
argument_list|<
name|Entry
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
argument_list|>
name|entrySet
init|=
name|counter
operator|.
name|entrySet
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|counter
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|safeCounter
operator|.
name|size
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|Entry
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
name|entry
range|:
name|entrySet
control|)
block|{
name|AtomicInteger
name|atomicInteger
init|=
name|safeCounter
operator|.
name|get
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|atomicInteger
argument_list|,
name|not
argument_list|(
name|Matchers
operator|.
name|nullValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|atomicInteger
operator|.
name|get
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|ElasticSearchIllegalStateException
operator|.
name|class
argument_list|)
DECL|method|checkCannotAcquireTwoLocks
specifier|public
name|void
name|checkCannotAcquireTwoLocks
parameter_list|()
throws|throws
name|InterruptedException
block|{
name|ConcurrentHashMap
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
name|counters
init|=
operator|new
name|ConcurrentHashMap
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
argument_list|()
decl_stmt|;
name|ConcurrentHashMap
argument_list|<
name|String
argument_list|,
name|AtomicInteger
argument_list|>
name|safeCounter
init|=
operator|new
name|ConcurrentHashMap
argument_list|<
name|String
argument_list|,
name|AtomicInteger
argument_list|>
argument_list|()
decl_stmt|;
name|KeyedLock
argument_list|<
name|String
argument_list|>
name|connectionLock
init|=
operator|new
name|KeyedLock
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|String
index|[]
name|names
init|=
operator|new
name|String
index|[
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|40
argument_list|)
index|]
decl_stmt|;
name|connectionLock
operator|=
operator|new
name|KeyedLock
argument_list|<
name|String
argument_list|>
argument_list|()
expr_stmt|;
name|String
name|name
init|=
name|randomRealisticUnicodeOfLength
argument_list|(
name|atLeast
argument_list|(
literal|10
argument_list|)
argument_list|)
decl_stmt|;
name|connectionLock
operator|.
name|acquire
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|connectionLock
operator|.
name|acquire
argument_list|(
name|name
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|ElasticSearchIllegalStateException
operator|.
name|class
argument_list|)
DECL|method|checkCannotReleaseUnacquiredLock
specifier|public
name|void
name|checkCannotReleaseUnacquiredLock
parameter_list|()
throws|throws
name|InterruptedException
block|{
name|ConcurrentHashMap
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
name|counters
init|=
operator|new
name|ConcurrentHashMap
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
argument_list|()
decl_stmt|;
name|ConcurrentHashMap
argument_list|<
name|String
argument_list|,
name|AtomicInteger
argument_list|>
name|safeCounter
init|=
operator|new
name|ConcurrentHashMap
argument_list|<
name|String
argument_list|,
name|AtomicInteger
argument_list|>
argument_list|()
decl_stmt|;
name|KeyedLock
argument_list|<
name|String
argument_list|>
name|connectionLock
init|=
operator|new
name|KeyedLock
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|String
index|[]
name|names
init|=
operator|new
name|String
index|[
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|40
argument_list|)
index|]
decl_stmt|;
name|connectionLock
operator|=
operator|new
name|KeyedLock
argument_list|<
name|String
argument_list|>
argument_list|()
expr_stmt|;
name|String
name|name
init|=
name|randomRealisticUnicodeOfLength
argument_list|(
name|atLeast
argument_list|(
literal|10
argument_list|)
argument_list|)
decl_stmt|;
name|connectionLock
operator|.
name|release
argument_list|(
name|name
argument_list|)
expr_stmt|;
block|}
DECL|class|AcquireAndReleaseThread
specifier|public
specifier|static
class|class
name|AcquireAndReleaseThread
extends|extends
name|Thread
block|{
DECL|field|startLatch
specifier|private
name|CountDownLatch
name|startLatch
decl_stmt|;
DECL|field|connectionLock
name|KeyedLock
argument_list|<
name|String
argument_list|>
name|connectionLock
decl_stmt|;
DECL|field|names
name|String
index|[]
name|names
decl_stmt|;
DECL|field|counter
name|ConcurrentHashMap
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
name|counter
decl_stmt|;
DECL|field|safeCounter
name|ConcurrentHashMap
argument_list|<
name|String
argument_list|,
name|AtomicInteger
argument_list|>
name|safeCounter
decl_stmt|;
DECL|method|AcquireAndReleaseThread
specifier|public
name|AcquireAndReleaseThread
parameter_list|(
name|CountDownLatch
name|startLatch
parameter_list|,
name|KeyedLock
argument_list|<
name|String
argument_list|>
name|connectionLock
parameter_list|,
name|String
index|[]
name|names
parameter_list|,
name|ConcurrentHashMap
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
name|counter
parameter_list|,
name|ConcurrentHashMap
argument_list|<
name|String
argument_list|,
name|AtomicInteger
argument_list|>
name|safeCounter
parameter_list|)
block|{
name|this
operator|.
name|startLatch
operator|=
name|startLatch
expr_stmt|;
name|this
operator|.
name|connectionLock
operator|=
name|connectionLock
expr_stmt|;
name|this
operator|.
name|names
operator|=
name|names
expr_stmt|;
name|this
operator|.
name|counter
operator|=
name|counter
expr_stmt|;
name|this
operator|.
name|safeCounter
operator|=
name|safeCounter
expr_stmt|;
block|}
DECL|method|run
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
name|startLatch
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
throw|throw
operator|new
name|RuntimeException
argument_list|()
throw|;
block|}
name|int
name|numRuns
init|=
name|atLeast
argument_list|(
literal|500
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numRuns
condition|;
name|i
operator|++
control|)
block|{
name|String
name|curName
init|=
name|names
index|[
name|randomInt
argument_list|(
name|names
operator|.
name|length
operator|-
literal|1
argument_list|)
index|]
decl_stmt|;
name|connectionLock
operator|.
name|acquire
argument_list|(
name|curName
argument_list|)
expr_stmt|;
try|try
block|{
name|Integer
name|integer
init|=
name|counter
operator|.
name|get
argument_list|(
name|curName
argument_list|)
decl_stmt|;
if|if
condition|(
name|integer
operator|==
literal|null
condition|)
block|{
name|counter
operator|.
name|put
argument_list|(
name|curName
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|counter
operator|.
name|put
argument_list|(
name|curName
argument_list|,
name|integer
operator|.
name|intValue
argument_list|()
operator|+
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|connectionLock
operator|.
name|release
argument_list|(
name|curName
argument_list|)
expr_stmt|;
block|}
name|AtomicInteger
name|atomicInteger
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|AtomicInteger
name|value
init|=
name|safeCounter
operator|.
name|putIfAbsent
argument_list|(
name|curName
argument_list|,
name|atomicInteger
argument_list|)
decl_stmt|;
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
name|atomicInteger
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|value
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
end_class

end_unit

