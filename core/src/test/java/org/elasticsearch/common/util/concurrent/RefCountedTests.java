begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.util.concurrent
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|util
operator|.
name|concurrent
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
name|AlreadyClosedException
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
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
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
name|CopyOnWriteArrayList
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
name|AtomicBoolean
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
name|is
import|;
end_import

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|RefCountedTests
specifier|public
class|class
name|RefCountedTests
extends|extends
name|ESTestCase
block|{
DECL|method|testRefCount
specifier|public
name|void
name|testRefCount
parameter_list|()
throws|throws
name|IOException
block|{
name|MyRefCounted
name|counted
init|=
operator|new
name|MyRefCounted
argument_list|()
decl_stmt|;
name|int
name|incs
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|100
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
name|incs
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|counted
operator|.
name|incRef
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|assertTrue
argument_list|(
name|counted
operator|.
name|tryIncRef
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|counted
operator|.
name|ensureOpen
argument_list|()
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
name|incs
condition|;
name|i
operator|++
control|)
block|{
name|counted
operator|.
name|decRef
argument_list|()
expr_stmt|;
name|counted
operator|.
name|ensureOpen
argument_list|()
expr_stmt|;
block|}
name|counted
operator|.
name|incRef
argument_list|()
expr_stmt|;
name|counted
operator|.
name|decRef
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
name|incs
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|counted
operator|.
name|incRef
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|assertTrue
argument_list|(
name|counted
operator|.
name|tryIncRef
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|counted
operator|.
name|ensureOpen
argument_list|()
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
name|incs
condition|;
name|i
operator|++
control|)
block|{
name|counted
operator|.
name|decRef
argument_list|()
expr_stmt|;
name|counted
operator|.
name|ensureOpen
argument_list|()
expr_stmt|;
block|}
name|counted
operator|.
name|decRef
argument_list|()
expr_stmt|;
name|assertFalse
argument_list|(
name|counted
operator|.
name|tryIncRef
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
name|counted
operator|.
name|incRef
argument_list|()
expr_stmt|;
name|fail
argument_list|(
literal|" expected exception"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|AlreadyClosedException
name|ex
parameter_list|)
block|{
name|assertThat
argument_list|(
name|ex
operator|.
name|getMessage
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"test is already closed can't increment refCount current count [0]"
argument_list|)
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|counted
operator|.
name|ensureOpen
argument_list|()
expr_stmt|;
name|fail
argument_list|(
literal|" expected exception"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|AlreadyClosedException
name|ex
parameter_list|)
block|{
name|assertThat
argument_list|(
name|ex
operator|.
name|getMessage
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"closed"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testMultiThreaded
specifier|public
name|void
name|testMultiThreaded
parameter_list|()
throws|throws
name|InterruptedException
block|{
specifier|final
name|MyRefCounted
name|counted
init|=
operator|new
name|MyRefCounted
argument_list|()
decl_stmt|;
name|Thread
index|[]
name|threads
init|=
operator|new
name|Thread
index|[
name|randomIntBetween
argument_list|(
literal|2
argument_list|,
literal|5
argument_list|)
index|]
decl_stmt|;
specifier|final
name|CountDownLatch
name|latch
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
specifier|final
name|CopyOnWriteArrayList
argument_list|<
name|Throwable
argument_list|>
name|exceptions
init|=
operator|new
name|CopyOnWriteArrayList
argument_list|<>
argument_list|()
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
name|threads
operator|.
name|length
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
name|Thread
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
name|latch
operator|.
name|await
argument_list|()
expr_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
literal|10000
condition|;
name|j
operator|++
control|)
block|{
name|counted
operator|.
name|incRef
argument_list|()
expr_stmt|;
try|try
block|{
name|counted
operator|.
name|ensureOpen
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
name|counted
operator|.
name|decRef
argument_list|()
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|exceptions
operator|.
name|add
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
expr_stmt|;
name|threads
index|[
name|i
index|]
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
name|latch
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
name|threads
operator|.
name|length
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
name|counted
operator|.
name|decRef
argument_list|()
expr_stmt|;
try|try
block|{
name|counted
operator|.
name|ensureOpen
argument_list|()
expr_stmt|;
name|fail
argument_list|(
literal|"expected to be closed"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|AlreadyClosedException
name|ex
parameter_list|)
block|{
name|assertThat
argument_list|(
name|ex
operator|.
name|getMessage
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"closed"
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|assertThat
argument_list|(
name|counted
operator|.
name|refCount
argument_list|()
argument_list|,
name|is
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|exceptions
argument_list|,
name|Matchers
operator|.
name|emptyIterable
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|class|MyRefCounted
specifier|private
specifier|final
class|class
name|MyRefCounted
extends|extends
name|AbstractRefCounted
block|{
DECL|field|closed
specifier|private
specifier|final
name|AtomicBoolean
name|closed
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
DECL|method|MyRefCounted
specifier|public
name|MyRefCounted
parameter_list|()
block|{
name|super
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|closeInternal
specifier|protected
name|void
name|closeInternal
parameter_list|()
block|{
name|this
operator|.
name|closed
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
DECL|method|ensureOpen
specifier|public
name|void
name|ensureOpen
parameter_list|()
block|{
if|if
condition|(
name|closed
operator|.
name|get
argument_list|()
condition|)
block|{
assert|assert
name|this
operator|.
name|refCount
argument_list|()
operator|==
literal|0
assert|;
throw|throw
operator|new
name|AlreadyClosedException
argument_list|(
literal|"closed"
argument_list|)
throw|;
block|}
block|}
block|}
block|}
end_class

end_unit

