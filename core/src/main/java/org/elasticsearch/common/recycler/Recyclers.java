begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.recycler
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|recycler
package|;
end_package

begin_import
import|import
name|com
operator|.
name|carrotsearch
operator|.
name|hppc
operator|.
name|BitMixer
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayDeque
import|;
end_import

begin_enum
DECL|enum|Recyclers
specifier|public
enum|enum
name|Recyclers
block|{     ;
comment|/**      * Return a {@link Recycler} that never recycles entries.      */
DECL|method|none
specifier|public
specifier|static
parameter_list|<
name|T
parameter_list|>
name|Recycler
argument_list|<
name|T
argument_list|>
name|none
parameter_list|(
name|Recycler
operator|.
name|C
argument_list|<
name|T
argument_list|>
name|c
parameter_list|)
block|{
return|return
operator|new
name|NoneRecycler
argument_list|<>
argument_list|(
name|c
argument_list|)
return|;
block|}
comment|/**      * Return a concurrent recycler based on a deque.      */
DECL|method|concurrentDeque
specifier|public
specifier|static
parameter_list|<
name|T
parameter_list|>
name|Recycler
argument_list|<
name|T
argument_list|>
name|concurrentDeque
parameter_list|(
name|Recycler
operator|.
name|C
argument_list|<
name|T
argument_list|>
name|c
parameter_list|,
name|int
name|limit
parameter_list|)
block|{
return|return
operator|new
name|ConcurrentDequeRecycler
argument_list|<>
argument_list|(
name|c
argument_list|,
name|limit
argument_list|)
return|;
block|}
comment|/**      * Return a recycler based on a deque.      */
DECL|method|deque
specifier|public
specifier|static
parameter_list|<
name|T
parameter_list|>
name|Recycler
argument_list|<
name|T
argument_list|>
name|deque
parameter_list|(
name|Recycler
operator|.
name|C
argument_list|<
name|T
argument_list|>
name|c
parameter_list|,
name|int
name|limit
parameter_list|)
block|{
return|return
operator|new
name|DequeRecycler
argument_list|<>
argument_list|(
name|c
argument_list|,
operator|new
name|ArrayDeque
argument_list|<>
argument_list|()
argument_list|,
name|limit
argument_list|)
return|;
block|}
comment|/**      * Return a recycler based on a deque.      */
DECL|method|dequeFactory
specifier|public
specifier|static
parameter_list|<
name|T
parameter_list|>
name|Recycler
operator|.
name|Factory
argument_list|<
name|T
argument_list|>
name|dequeFactory
parameter_list|(
specifier|final
name|Recycler
operator|.
name|C
argument_list|<
name|T
argument_list|>
name|c
parameter_list|,
specifier|final
name|int
name|limit
parameter_list|)
block|{
return|return
operator|new
name|Recycler
operator|.
name|Factory
argument_list|<
name|T
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Recycler
argument_list|<
name|T
argument_list|>
name|build
parameter_list|()
block|{
return|return
name|deque
argument_list|(
name|c
argument_list|,
name|limit
argument_list|)
return|;
block|}
block|}
return|;
block|}
comment|/**      * Wrap two recyclers and forward to calls to<code>smallObjectRecycler</code> when<code>size&lt; minSize</code> and to      *<code>defaultRecycler</code> otherwise.      */
DECL|method|sizing
specifier|public
specifier|static
parameter_list|<
name|T
parameter_list|>
name|Recycler
argument_list|<
name|T
argument_list|>
name|sizing
parameter_list|(
specifier|final
name|Recycler
argument_list|<
name|T
argument_list|>
name|defaultRecycler
parameter_list|,
specifier|final
name|Recycler
argument_list|<
name|T
argument_list|>
name|smallObjectRecycler
parameter_list|,
specifier|final
name|int
name|minSize
parameter_list|)
block|{
return|return
operator|new
name|FilterRecycler
argument_list|<
name|T
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|protected
name|Recycler
argument_list|<
name|T
argument_list|>
name|getDelegate
parameter_list|()
block|{
return|return
name|defaultRecycler
return|;
block|}
annotation|@
name|Override
specifier|public
name|Recycler
operator|.
name|V
argument_list|<
name|T
argument_list|>
name|obtain
parameter_list|(
name|int
name|sizing
parameter_list|)
block|{
if|if
condition|(
name|sizing
operator|>
literal|0
operator|&&
name|sizing
operator|<
name|minSize
condition|)
block|{
return|return
name|smallObjectRecycler
operator|.
name|obtain
argument_list|(
name|sizing
argument_list|)
return|;
block|}
return|return
name|super
operator|.
name|obtain
argument_list|(
name|sizing
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
block|{
name|defaultRecycler
operator|.
name|close
argument_list|()
expr_stmt|;
name|smallObjectRecycler
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
return|;
block|}
comment|/**      * Wrap the provided recycler so that calls to {@link Recycler#obtain()} and {@link Recycler.V#close()} are protected by      * a lock.      */
DECL|method|locked
specifier|public
specifier|static
parameter_list|<
name|T
parameter_list|>
name|Recycler
argument_list|<
name|T
argument_list|>
name|locked
parameter_list|(
specifier|final
name|Recycler
argument_list|<
name|T
argument_list|>
name|recycler
parameter_list|)
block|{
return|return
operator|new
name|FilterRecycler
argument_list|<
name|T
argument_list|>
argument_list|()
block|{
specifier|private
specifier|final
name|Object
name|lock
decl_stmt|;
block|{
name|this
operator|.
name|lock
operator|=
operator|new
name|Object
argument_list|()
block|;             }
annotation|@
name|Override
specifier|protected
name|Recycler
argument_list|<
name|T
argument_list|>
name|getDelegate
parameter_list|()
block|{
return|return
name|recycler
return|;
block|}
annotation|@
name|Override
specifier|public
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|recycler
operator|.
name|Recycler
operator|.
name|V
argument_list|<
name|T
argument_list|>
name|obtain
parameter_list|(
name|int
name|sizing
parameter_list|)
block|{
synchronized|synchronized
init|(
name|lock
init|)
block|{
return|return
name|super
operator|.
name|obtain
argument_list|(
name|sizing
argument_list|)
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|recycler
operator|.
name|Recycler
operator|.
name|V
argument_list|<
name|T
argument_list|>
name|obtain
parameter_list|()
block|{
synchronized|synchronized
init|(
name|lock
init|)
block|{
return|return
name|super
operator|.
name|obtain
argument_list|()
return|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|Recycler
operator|.
name|V
argument_list|<
name|T
argument_list|>
name|wrap
parameter_list|(
specifier|final
name|Recycler
operator|.
name|V
argument_list|<
name|T
argument_list|>
name|delegate
parameter_list|)
block|{
return|return
operator|new
name|Recycler
operator|.
name|V
argument_list|<
name|T
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
block|{
synchronized|synchronized
init|(
name|lock
init|)
block|{
name|delegate
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|T
name|v
parameter_list|()
block|{
return|return
name|delegate
operator|.
name|v
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isRecycled
parameter_list|()
block|{
return|return
name|delegate
operator|.
name|isRecycled
argument_list|()
return|;
block|}
block|}
return|;
block|}
block|}
return|;
block|}
comment|/**      * Create a concurrent implementation that can support concurrent access from<code>concurrencyLevel</code> threads with little contention.      */
DECL|method|concurrent
specifier|public
specifier|static
parameter_list|<
name|T
parameter_list|>
name|Recycler
argument_list|<
name|T
argument_list|>
name|concurrent
parameter_list|(
specifier|final
name|Recycler
operator|.
name|Factory
argument_list|<
name|T
argument_list|>
name|factory
parameter_list|,
specifier|final
name|int
name|concurrencyLevel
parameter_list|)
block|{
if|if
condition|(
name|concurrencyLevel
operator|<
literal|1
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"concurrencyLevel must be>= 1"
argument_list|)
throw|;
block|}
if|if
condition|(
name|concurrencyLevel
operator|==
literal|1
condition|)
block|{
return|return
name|locked
argument_list|(
name|factory
operator|.
name|build
argument_list|()
argument_list|)
return|;
block|}
return|return
operator|new
name|FilterRecycler
argument_list|<
name|T
argument_list|>
argument_list|()
block|{
specifier|private
specifier|final
name|Recycler
argument_list|<
name|T
argument_list|>
index|[]
name|recyclers
decl_stmt|;
block|{
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|final
name|Recycler
argument_list|<
name|T
argument_list|>
index|[]
name|recyclers
init|=
operator|new
name|Recycler
index|[
name|concurrencyLevel
index|]
decl_stmt|;
name|this
operator|.
name|recyclers
operator|=
name|recyclers
block|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|concurrencyLevel
condition|;
operator|++
name|i
control|)
block|{
name|recyclers
index|[
name|i
index|]
operator|=
name|locked
argument_list|(
name|factory
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|int
name|slot
parameter_list|()
block|{
specifier|final
name|long
name|id
init|=
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|getId
argument_list|()
decl_stmt|;
comment|// don't trust Thread.hashCode to have equiprobable low bits
name|int
name|slot
init|=
operator|(
name|int
operator|)
name|BitMixer
operator|.
name|mix64
argument_list|(
name|id
argument_list|)
decl_stmt|;
comment|// make positive, otherwise % may return negative numbers
name|slot
operator|&=
literal|0x7FFFFFFF
expr_stmt|;
name|slot
operator|%=
name|concurrencyLevel
expr_stmt|;
return|return
name|slot
return|;
block|}
annotation|@
name|Override
specifier|protected
name|Recycler
argument_list|<
name|T
argument_list|>
name|getDelegate
parameter_list|()
block|{
return|return
name|recyclers
index|[
name|slot
argument_list|()
index|]
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
block|{
for|for
control|(
name|Recycler
argument_list|<
name|T
argument_list|>
name|recycler
range|:
name|recyclers
control|)
block|{
name|recycler
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
return|;
block|}
DECL|method|concurrent
specifier|public
specifier|static
parameter_list|<
name|T
parameter_list|>
name|Recycler
argument_list|<
name|T
argument_list|>
name|concurrent
parameter_list|(
specifier|final
name|Recycler
operator|.
name|Factory
argument_list|<
name|T
argument_list|>
name|factory
parameter_list|)
block|{
return|return
name|concurrent
argument_list|(
name|factory
argument_list|,
name|Runtime
operator|.
name|getRuntime
argument_list|()
operator|.
name|availableProcessors
argument_list|()
argument_list|)
return|;
block|}
block|}
end_enum

end_unit

