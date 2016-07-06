begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.bulk
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|bulk
package|;
end_package

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
name|java
operator|.
name|util
operator|.
name|Iterator
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|NoSuchElementException
import|;
end_import

begin_comment
comment|/**  * Provides a backoff policy for bulk requests. Whenever a bulk request is rejected due to resource constraints (i.e. the client's internal  * thread pool is full), the backoff policy decides how long the bulk processor will wait before the operation is retried internally.  *  * Notes for implementing custom subclasses:  *  * The underlying mathematical principle of<code>BackoffPolicy</code> are progressions which can be either finite or infinite although  * the latter should not be used for retrying. A progression can be mapped to a<code>java.util.Iterator</code> with the following  * semantics:  *  *<ul>  *<li><code>#hasNext()</code> determines whether the progression has more elements. Return<code>true</code> for infinite progressions</li>  *<li><code>#next()</code> determines the next element in the progression, i.e. the next wait time period</li>  *</ul>  *  * Note that backoff policies are exposed as<code>Iterables</code> in order to be consumed multiple times.  */
end_comment

begin_class
DECL|class|BackoffPolicy
specifier|public
specifier|abstract
class|class
name|BackoffPolicy
implements|implements
name|Iterable
argument_list|<
name|TimeValue
argument_list|>
block|{
DECL|field|NO_BACKOFF
specifier|private
specifier|static
specifier|final
name|BackoffPolicy
name|NO_BACKOFF
init|=
operator|new
name|NoBackoff
argument_list|()
decl_stmt|;
comment|/**      * Creates a backoff policy that will not allow any backoff, i.e. an operation will fail after the first attempt.      *      * @return A backoff policy without any backoff period. The returned instance is thread safe.      */
DECL|method|noBackoff
specifier|public
specifier|static
name|BackoffPolicy
name|noBackoff
parameter_list|()
block|{
return|return
name|NO_BACKOFF
return|;
block|}
comment|/**      * Creates an new constant backoff policy with the provided configuration.      *      * @param delay              The delay defines how long to wait between retry attempts. Must not be null.      *                           Must be&lt;=<code>Integer.MAX_VALUE</code> ms.      * @param maxNumberOfRetries The maximum number of retries. Must be a non-negative number.      * @return A backoff policy with a constant wait time between retries. The returned instance is thread safe but each      * iterator created from it should only be used by a single thread.      */
DECL|method|constantBackoff
specifier|public
specifier|static
name|BackoffPolicy
name|constantBackoff
parameter_list|(
name|TimeValue
name|delay
parameter_list|,
name|int
name|maxNumberOfRetries
parameter_list|)
block|{
return|return
operator|new
name|ConstantBackoff
argument_list|(
name|checkDelay
argument_list|(
name|delay
argument_list|)
argument_list|,
name|maxNumberOfRetries
argument_list|)
return|;
block|}
comment|/**      * Creates an new exponential backoff policy with a default configuration of 50 ms initial wait period and 8 retries taking      * roughly 5.1 seconds in total.      *      * @return A backoff policy with an exponential increase in wait time for retries. The returned instance is thread safe but each      * iterator created from it should only be used by a single thread.      */
DECL|method|exponentialBackoff
specifier|public
specifier|static
name|BackoffPolicy
name|exponentialBackoff
parameter_list|()
block|{
return|return
name|exponentialBackoff
argument_list|(
name|TimeValue
operator|.
name|timeValueMillis
argument_list|(
literal|50
argument_list|)
argument_list|,
literal|8
argument_list|)
return|;
block|}
comment|/**      * Creates an new exponential backoff policy with the provided configuration.      *      * @param initialDelay       The initial delay defines how long to wait for the first retry attempt. Must not be null.      *                           Must be&lt;=<code>Integer.MAX_VALUE</code> ms.      * @param maxNumberOfRetries The maximum number of retries. Must be a non-negative number.      * @return A backoff policy with an exponential increase in wait time for retries. The returned instance is thread safe but each      * iterator created from it should only be used by a single thread.      */
DECL|method|exponentialBackoff
specifier|public
specifier|static
name|BackoffPolicy
name|exponentialBackoff
parameter_list|(
name|TimeValue
name|initialDelay
parameter_list|,
name|int
name|maxNumberOfRetries
parameter_list|)
block|{
return|return
operator|new
name|ExponentialBackoff
argument_list|(
operator|(
name|int
operator|)
name|checkDelay
argument_list|(
name|initialDelay
argument_list|)
operator|.
name|millis
argument_list|()
argument_list|,
name|maxNumberOfRetries
argument_list|)
return|;
block|}
comment|/**      * Wraps the backoff policy in one that calls a method every time a new backoff is taken from the policy.      */
DECL|method|wrap
specifier|public
specifier|static
name|BackoffPolicy
name|wrap
parameter_list|(
name|BackoffPolicy
name|delegate
parameter_list|,
name|Runnable
name|onBackoff
parameter_list|)
block|{
return|return
operator|new
name|WrappedBackoffPolicy
argument_list|(
name|delegate
argument_list|,
name|onBackoff
argument_list|)
return|;
block|}
DECL|method|checkDelay
specifier|private
specifier|static
name|TimeValue
name|checkDelay
parameter_list|(
name|TimeValue
name|delay
parameter_list|)
block|{
if|if
condition|(
name|delay
operator|.
name|millis
argument_list|()
operator|>
name|Integer
operator|.
name|MAX_VALUE
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"delay must be<= "
operator|+
name|Integer
operator|.
name|MAX_VALUE
operator|+
literal|" ms"
argument_list|)
throw|;
block|}
return|return
name|delay
return|;
block|}
DECL|class|NoBackoff
specifier|private
specifier|static
class|class
name|NoBackoff
extends|extends
name|BackoffPolicy
block|{
annotation|@
name|Override
DECL|method|iterator
specifier|public
name|Iterator
argument_list|<
name|TimeValue
argument_list|>
name|iterator
parameter_list|()
block|{
return|return
operator|new
name|Iterator
argument_list|<
name|TimeValue
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|hasNext
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|TimeValue
name|next
parameter_list|()
block|{
throw|throw
operator|new
name|NoSuchElementException
argument_list|(
literal|"No backoff"
argument_list|)
throw|;
block|}
block|}
return|;
block|}
block|}
DECL|class|ExponentialBackoff
specifier|private
specifier|static
class|class
name|ExponentialBackoff
extends|extends
name|BackoffPolicy
block|{
DECL|field|start
specifier|private
specifier|final
name|int
name|start
decl_stmt|;
DECL|field|numberOfElements
specifier|private
specifier|final
name|int
name|numberOfElements
decl_stmt|;
DECL|method|ExponentialBackoff
specifier|private
name|ExponentialBackoff
parameter_list|(
name|int
name|start
parameter_list|,
name|int
name|numberOfElements
parameter_list|)
block|{
assert|assert
name|start
operator|>=
literal|0
assert|;
assert|assert
name|numberOfElements
operator|>=
literal|0
assert|;
name|this
operator|.
name|start
operator|=
name|start
expr_stmt|;
name|this
operator|.
name|numberOfElements
operator|=
name|numberOfElements
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|iterator
specifier|public
name|Iterator
argument_list|<
name|TimeValue
argument_list|>
name|iterator
parameter_list|()
block|{
return|return
operator|new
name|ExponentialBackoffIterator
argument_list|(
name|start
argument_list|,
name|numberOfElements
argument_list|)
return|;
block|}
block|}
DECL|class|ExponentialBackoffIterator
specifier|private
specifier|static
class|class
name|ExponentialBackoffIterator
implements|implements
name|Iterator
argument_list|<
name|TimeValue
argument_list|>
block|{
DECL|field|numberOfElements
specifier|private
specifier|final
name|int
name|numberOfElements
decl_stmt|;
DECL|field|start
specifier|private
specifier|final
name|int
name|start
decl_stmt|;
DECL|field|currentlyConsumed
specifier|private
name|int
name|currentlyConsumed
decl_stmt|;
DECL|method|ExponentialBackoffIterator
specifier|private
name|ExponentialBackoffIterator
parameter_list|(
name|int
name|start
parameter_list|,
name|int
name|numberOfElements
parameter_list|)
block|{
name|this
operator|.
name|start
operator|=
name|start
expr_stmt|;
name|this
operator|.
name|numberOfElements
operator|=
name|numberOfElements
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|hasNext
specifier|public
name|boolean
name|hasNext
parameter_list|()
block|{
return|return
name|currentlyConsumed
operator|<
name|numberOfElements
return|;
block|}
annotation|@
name|Override
DECL|method|next
specifier|public
name|TimeValue
name|next
parameter_list|()
block|{
if|if
condition|(
operator|!
name|hasNext
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|NoSuchElementException
argument_list|(
literal|"Only up to "
operator|+
name|numberOfElements
operator|+
literal|" elements"
argument_list|)
throw|;
block|}
name|int
name|result
init|=
name|start
operator|+
literal|10
operator|*
operator|(
operator|(
name|int
operator|)
name|Math
operator|.
name|exp
argument_list|(
literal|0.8d
operator|*
operator|(
name|currentlyConsumed
operator|)
argument_list|)
operator|-
literal|1
operator|)
decl_stmt|;
name|currentlyConsumed
operator|++
expr_stmt|;
return|return
name|TimeValue
operator|.
name|timeValueMillis
argument_list|(
name|result
argument_list|)
return|;
block|}
block|}
DECL|class|ConstantBackoff
specifier|private
specifier|static
specifier|final
class|class
name|ConstantBackoff
extends|extends
name|BackoffPolicy
block|{
DECL|field|delay
specifier|private
specifier|final
name|TimeValue
name|delay
decl_stmt|;
DECL|field|numberOfElements
specifier|private
specifier|final
name|int
name|numberOfElements
decl_stmt|;
DECL|method|ConstantBackoff
specifier|public
name|ConstantBackoff
parameter_list|(
name|TimeValue
name|delay
parameter_list|,
name|int
name|numberOfElements
parameter_list|)
block|{
assert|assert
name|numberOfElements
operator|>=
literal|0
assert|;
name|this
operator|.
name|delay
operator|=
name|delay
expr_stmt|;
name|this
operator|.
name|numberOfElements
operator|=
name|numberOfElements
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|iterator
specifier|public
name|Iterator
argument_list|<
name|TimeValue
argument_list|>
name|iterator
parameter_list|()
block|{
return|return
operator|new
name|ConstantBackoffIterator
argument_list|(
name|delay
argument_list|,
name|numberOfElements
argument_list|)
return|;
block|}
block|}
DECL|class|ConstantBackoffIterator
specifier|private
specifier|static
specifier|final
class|class
name|ConstantBackoffIterator
implements|implements
name|Iterator
argument_list|<
name|TimeValue
argument_list|>
block|{
DECL|field|delay
specifier|private
specifier|final
name|TimeValue
name|delay
decl_stmt|;
DECL|field|numberOfElements
specifier|private
specifier|final
name|int
name|numberOfElements
decl_stmt|;
DECL|field|curr
specifier|private
name|int
name|curr
decl_stmt|;
DECL|method|ConstantBackoffIterator
specifier|public
name|ConstantBackoffIterator
parameter_list|(
name|TimeValue
name|delay
parameter_list|,
name|int
name|numberOfElements
parameter_list|)
block|{
name|this
operator|.
name|delay
operator|=
name|delay
expr_stmt|;
name|this
operator|.
name|numberOfElements
operator|=
name|numberOfElements
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|hasNext
specifier|public
name|boolean
name|hasNext
parameter_list|()
block|{
return|return
name|curr
operator|<
name|numberOfElements
return|;
block|}
annotation|@
name|Override
DECL|method|next
specifier|public
name|TimeValue
name|next
parameter_list|()
block|{
if|if
condition|(
operator|!
name|hasNext
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|NoSuchElementException
argument_list|()
throw|;
block|}
name|curr
operator|++
expr_stmt|;
return|return
name|delay
return|;
block|}
block|}
DECL|class|WrappedBackoffPolicy
specifier|private
specifier|static
specifier|final
class|class
name|WrappedBackoffPolicy
extends|extends
name|BackoffPolicy
block|{
DECL|field|delegate
specifier|private
specifier|final
name|BackoffPolicy
name|delegate
decl_stmt|;
DECL|field|onBackoff
specifier|private
specifier|final
name|Runnable
name|onBackoff
decl_stmt|;
DECL|method|WrappedBackoffPolicy
specifier|public
name|WrappedBackoffPolicy
parameter_list|(
name|BackoffPolicy
name|delegate
parameter_list|,
name|Runnable
name|onBackoff
parameter_list|)
block|{
name|this
operator|.
name|delegate
operator|=
name|delegate
expr_stmt|;
name|this
operator|.
name|onBackoff
operator|=
name|onBackoff
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|iterator
specifier|public
name|Iterator
argument_list|<
name|TimeValue
argument_list|>
name|iterator
parameter_list|()
block|{
return|return
operator|new
name|WrappedBackoffIterator
argument_list|(
name|delegate
operator|.
name|iterator
argument_list|()
argument_list|,
name|onBackoff
argument_list|)
return|;
block|}
block|}
DECL|class|WrappedBackoffIterator
specifier|private
specifier|static
specifier|final
class|class
name|WrappedBackoffIterator
implements|implements
name|Iterator
argument_list|<
name|TimeValue
argument_list|>
block|{
DECL|field|delegate
specifier|private
specifier|final
name|Iterator
argument_list|<
name|TimeValue
argument_list|>
name|delegate
decl_stmt|;
DECL|field|onBackoff
specifier|private
specifier|final
name|Runnable
name|onBackoff
decl_stmt|;
DECL|method|WrappedBackoffIterator
specifier|public
name|WrappedBackoffIterator
parameter_list|(
name|Iterator
argument_list|<
name|TimeValue
argument_list|>
name|delegate
parameter_list|,
name|Runnable
name|onBackoff
parameter_list|)
block|{
name|this
operator|.
name|delegate
operator|=
name|delegate
expr_stmt|;
name|this
operator|.
name|onBackoff
operator|=
name|onBackoff
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|hasNext
specifier|public
name|boolean
name|hasNext
parameter_list|()
block|{
return|return
name|delegate
operator|.
name|hasNext
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|next
specifier|public
name|TimeValue
name|next
parameter_list|()
block|{
if|if
condition|(
literal|false
operator|==
name|delegate
operator|.
name|hasNext
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|NoSuchElementException
argument_list|()
throw|;
block|}
name|onBackoff
operator|.
name|run
argument_list|()
expr_stmt|;
return|return
name|delegate
operator|.
name|next
argument_list|()
return|;
block|}
block|}
block|}
end_class

end_unit

