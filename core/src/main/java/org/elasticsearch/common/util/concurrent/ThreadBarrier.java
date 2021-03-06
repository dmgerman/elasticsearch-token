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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|BrokenBarrierException
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
name|CyclicBarrier
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
name|TimeUnit
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
name|TimeoutException
import|;
end_import

begin_comment
comment|/**  * A synchronization aid that allows a set of threads to all wait for each other  * to reach a common barrier point. Barriers are useful in programs involving a  * fixed sized party of threads that must occasionally wait for each other.  *<code>ThreadBarrier</code> adds a<i>cause</i> to  * {@link BrokenBarrierException} thrown by a {@link #reset()} operation defined  * by {@link CyclicBarrier}.  *<p>  *<b>Sample usage:</b><br>  *<ul>  *<li>Barrier as a synchronization and Exception handling aid</li>  *<li>Barrier as a trigger for elapsed notification events</li>  *</ul>  *<pre>  *    class MyTestClass implements RemoteEventListener  *    {  *      final ThreadBarrier barrier;  *  *      class Worker implements Runnable  *        {  *          public void run()  *            {  *              barrier.await();    //wait for all threads to reach run  *              try  *                {  *                  prepare();  *                  barrier.await();    //wait for all threads to prepare  *                  process();  *                  barrier.await();    //wait for all threads to process  *                }  *              catch(Exception e){  *                  log(&quot;Worker thread caught exception&quot;, e);  *                  barrier.reset(e);  *                }  *            }  *        }  *  *      public void testThreads() {  *          barrier = new ThreadBarrier(N_THREADS + 1);  *          for (int i = 0; i&lt; N; ++i)  *           new Thread(new Worker()).start();  *  *          try{  *              barrier.await();    //wait for all threads to reach run  *              barrier.await();    //wait for all threads to prepare  *              barrier.await();    //wait for all threads to process  *            }  *          catch(BrokenBarrierException bbe) {  *              Assert.fail(bbe);  *            }  *       }  *  *      int actualNotificationCount = 0;  *      public synchronized void notify (RemoteEvent event) {  *          try{  *              actualNotificationCount++;  *              if (actualNotificationCount == EXPECTED_COUNT)  *                  barrier.await();    //signal when all notifications arrive  *  *               // too many notifications?  *               Assert.assertFalse(&quot;Exceeded notification count&quot;,  *                                          actualNotificationCount&gt; EXPECTED_COUNT);  *            }  *          catch(Exception e) {  *              log(&quot;Worker thread caught exception&quot;, e);  *              barrier.reset(e);  *            }  *        }  *  *      public void testNotify() {  *          barrier = new ThreadBarrier(N_LISTENERS + 1);  *          registerNotification();  *          triggerNotifications();  *  *          //wait until either all notifications arrive, or  *          //until a MAX_TIMEOUT is reached.  *          barrier.await(MAX_TIMEOUT);  *  *          //check if all notifications were accounted for or timed-out  *          Assert.assertEquals(&quot;Notification count&quot;,  *                                      EXPECTED_COUNT, actualNotificationCount);  *  *          //inspect that the barrier isn't broken  *          barrier.inspect(); //throws BrokenBarrierException if broken  *        }  *    }  *</pre>  *  *  */
end_comment

begin_class
DECL|class|ThreadBarrier
specifier|public
class|class
name|ThreadBarrier
extends|extends
name|CyclicBarrier
block|{
comment|/**      * The cause of a {@link BrokenBarrierException} and {@link TimeoutException}      * thrown from an await() when {@link #reset(Exception)} was invoked.      */
DECL|field|cause
specifier|private
name|Exception
name|cause
decl_stmt|;
DECL|method|ThreadBarrier
specifier|public
name|ThreadBarrier
parameter_list|(
name|int
name|parties
parameter_list|)
block|{
name|super
argument_list|(
name|parties
argument_list|)
expr_stmt|;
block|}
DECL|method|ThreadBarrier
specifier|public
name|ThreadBarrier
parameter_list|(
name|int
name|parties
parameter_list|,
name|Runnable
name|barrierAction
parameter_list|)
block|{
name|super
argument_list|(
name|parties
argument_list|,
name|barrierAction
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|await
specifier|public
name|int
name|await
parameter_list|()
throws|throws
name|InterruptedException
throws|,
name|BrokenBarrierException
block|{
try|try
block|{
name|breakIfBroken
argument_list|()
expr_stmt|;
return|return
name|super
operator|.
name|await
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|BrokenBarrierException
name|bbe
parameter_list|)
block|{
name|initCause
argument_list|(
name|bbe
argument_list|)
expr_stmt|;
throw|throw
name|bbe
throw|;
block|}
block|}
annotation|@
name|Override
DECL|method|await
specifier|public
name|int
name|await
parameter_list|(
name|long
name|timeout
parameter_list|,
name|TimeUnit
name|unit
parameter_list|)
throws|throws
name|InterruptedException
throws|,
name|BrokenBarrierException
throws|,
name|TimeoutException
block|{
try|try
block|{
name|breakIfBroken
argument_list|()
expr_stmt|;
return|return
name|super
operator|.
name|await
argument_list|(
name|timeout
argument_list|,
name|unit
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|BrokenBarrierException
name|bbe
parameter_list|)
block|{
name|initCause
argument_list|(
name|bbe
argument_list|)
expr_stmt|;
throw|throw
name|bbe
throw|;
block|}
catch|catch
parameter_list|(
name|TimeoutException
name|te
parameter_list|)
block|{
name|initCause
argument_list|(
name|te
argument_list|)
expr_stmt|;
throw|throw
name|te
throw|;
block|}
block|}
comment|/**      * Resets the barrier to its initial state.  If any parties are      * currently waiting at the barrier, they will return with a      * {@link BrokenBarrierException}. Note that resets<em>after</em>      * a breakage has occurred for other reasons can be complicated to      * carry out; threads need to re-synchronize in some other way,      * and choose one to perform the reset.  It may be preferable to      * instead create a new barrier for subsequent use.      *      * @param cause The cause of the BrokenBarrierException      */
DECL|method|reset
specifier|public
specifier|synchronized
name|void
name|reset
parameter_list|(
name|Exception
name|cause
parameter_list|)
block|{
if|if
condition|(
operator|!
name|isBroken
argument_list|()
condition|)
block|{
name|super
operator|.
name|reset
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|this
operator|.
name|cause
operator|==
literal|null
condition|)
block|{
name|this
operator|.
name|cause
operator|=
name|cause
expr_stmt|;
block|}
block|}
comment|/**      * Queries if this barrier is in a broken state. Note that if      * {@link #reset(Exception)} is invoked the barrier will remain broken, while      * {@link #reset()} will reset the barrier to its initial state and      * {@link #isBroken()} will return false.      *      * @return {@code true} if one or more parties broke out of this barrier due      *         to interruption or timeout since construction or the last reset,      *         or a barrier action failed due to an exception; {@code false}      *         otherwise.      * @see #inspect()      */
annotation|@
name|Override
DECL|method|isBroken
specifier|public
specifier|synchronized
name|boolean
name|isBroken
parameter_list|()
block|{
return|return
name|this
operator|.
name|cause
operator|!=
literal|null
operator|||
name|super
operator|.
name|isBroken
argument_list|()
return|;
block|}
comment|/**      * Inspects if the barrier is broken. If for any reason, the barrier      * was broken, a {@link BrokenBarrierException} will be thrown. Otherwise,      * would return gracefully.      *      * @throws BrokenBarrierException With a nested broken cause.      */
DECL|method|inspect
specifier|public
specifier|synchronized
name|void
name|inspect
parameter_list|()
throws|throws
name|BrokenBarrierException
block|{
try|try
block|{
name|breakIfBroken
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|BrokenBarrierException
name|bbe
parameter_list|)
block|{
name|initCause
argument_list|(
name|bbe
argument_list|)
expr_stmt|;
throw|throw
name|bbe
throw|;
block|}
block|}
comment|/**      * breaks this barrier if it has been reset or broken for any other reason.      *<p>      * Note: This call is not atomic in respect to await/reset calls. A      * breakIfBroken() may be context switched to invoke a reset() prior to      * await(). This resets the barrier to its initial state - parties not      * currently waiting at the barrier will not be accounted for! An await that      * wasn't time limited, will block indefinitely.      *      * @throws BrokenBarrierException an empty BrokenBarrierException.      */
DECL|method|breakIfBroken
specifier|private
specifier|synchronized
name|void
name|breakIfBroken
parameter_list|()
throws|throws
name|BrokenBarrierException
block|{
if|if
condition|(
name|isBroken
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|BrokenBarrierException
argument_list|()
throw|;
block|}
block|}
comment|/**      * Initializes the cause of this throwable to the specified value. The cause      * is the throwable that was initialized by {@link #reset(Exception)}.      *      * @param t throwable.      */
DECL|method|initCause
specifier|private
specifier|synchronized
name|void
name|initCause
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|t
operator|.
name|initCause
argument_list|(
name|this
operator|.
name|cause
argument_list|)
expr_stmt|;
block|}
comment|/**      * A Barrier action to be used in conjunction with {@link ThreadBarrier} to      * measure performance between barrier awaits. This runnable will execute      * when the barrier is tripped. Make sure to reset() the timer before next      * Measurement.      *      * @see ThreadBarrier#ThreadBarrier(int, Runnable)      *<p>      *<B>Usage example:</B><br>      *<pre><code>      *                                                                                             BarrierTimer timer = new BarrierTimer();      *                                                                                             ThreadBarrier barrier = new ThreadBarrier( nTHREADS + 1, timer );      *                                                                                             ..      *                                                                                             barrier.await(); // starts timer when all threads trip on await      *                                                                                             barrier.await(); // stops  timer when all threads trip on await      *                                                                                             ..      *                                                                                             long time = timer.getTimeInNanos();      *                                                                                             long tpi = time / ((long)nREPEATS * nTHREADS); //throughput per thread iteration      *                                                                                             long secs = timer.getTimeInSeconds();    //total runtime in seconds      *                                                                                             ..      *                                                                                             timer.reset();  // reuse timer      *</code></pre>      */
DECL|class|BarrierTimer
specifier|public
specifier|static
class|class
name|BarrierTimer
implements|implements
name|Runnable
block|{
DECL|field|started
specifier|volatile
name|boolean
name|started
decl_stmt|;
DECL|field|startTime
specifier|volatile
name|long
name|startTime
decl_stmt|;
DECL|field|endTime
specifier|volatile
name|long
name|endTime
decl_stmt|;
annotation|@
name|Override
DECL|method|run
specifier|public
name|void
name|run
parameter_list|()
block|{
name|long
name|t
init|=
name|System
operator|.
name|nanoTime
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|started
condition|)
block|{
name|started
operator|=
literal|true
expr_stmt|;
name|startTime
operator|=
name|t
expr_stmt|;
block|}
else|else
name|endTime
operator|=
name|t
expr_stmt|;
block|}
comment|/**          * resets (clears) this timer before next execution.          */
DECL|method|reset
specifier|public
name|void
name|reset
parameter_list|()
block|{
name|started
operator|=
literal|false
expr_stmt|;
block|}
comment|/**          * Returns the elapsed time between two successive barrier executions.          *          * @return elapsed time in nanoseconds.          */
DECL|method|getTimeInNanos
specifier|public
name|long
name|getTimeInNanos
parameter_list|()
block|{
return|return
name|endTime
operator|-
name|startTime
return|;
block|}
comment|/**          * Returns the elapsed time between two successive barrier executions.          *          * @return elapsed time in seconds.          */
DECL|method|getTimeInSeconds
specifier|public
name|double
name|getTimeInSeconds
parameter_list|()
block|{
name|long
name|time
init|=
name|endTime
operator|-
name|startTime
decl_stmt|;
return|return
operator|(
name|time
operator|)
operator|/
literal|1000000000.0
return|;
block|}
block|}
block|}
end_class

end_unit

