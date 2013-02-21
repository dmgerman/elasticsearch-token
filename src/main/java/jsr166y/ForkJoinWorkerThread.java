begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Written by Doug Lea with assistance from members of JCP JSR-166  * Expert Group and released to the public domain, as explained at  * http://creativecommons.org/publicdomain/zero/1.0/  */
end_comment

begin_package
DECL|package|jsr166y
package|package
name|jsr166y
package|;
end_package

begin_comment
comment|/**  * A thread managed by a {@link ForkJoinPool}, which executes  * {@link ForkJoinTask}s.  * This class is subclassable solely for the sake of adding  * functionality -- there are no overridable methods dealing with  * scheduling or execution.  However, you can override initialization  * and termination methods surrounding the main task processing loop.  * If you do create such a subclass, you will also need to supply a  * custom {@link ForkJoinPool.ForkJoinWorkerThreadFactory} to use it  * in a {@code ForkJoinPool}.  *  * @since 1.7  * @author Doug Lea  */
end_comment

begin_class
DECL|class|ForkJoinWorkerThread
specifier|public
class|class
name|ForkJoinWorkerThread
extends|extends
name|Thread
block|{
comment|/*      * ForkJoinWorkerThreads are managed by ForkJoinPools and perform      * ForkJoinTasks. For explanation, see the internal documentation      * of class ForkJoinPool.      *      * This class just maintains links to its pool and WorkQueue.  The      * pool field is set immediately upon construction, but the      * workQueue field is not set until a call to registerWorker      * completes. This leads to a visibility race, that is tolerated      * by requiring that the workQueue field is only accessed by the      * owning thread.      */
DECL|field|pool
specifier|final
name|ForkJoinPool
name|pool
decl_stmt|;
comment|// the pool this thread works in
DECL|field|workQueue
specifier|final
name|ForkJoinPool
operator|.
name|WorkQueue
name|workQueue
decl_stmt|;
comment|// work-stealing mechanics
comment|/**      * Creates a ForkJoinWorkerThread operating in the given pool.      *      * @param pool the pool this thread works in      * @throws NullPointerException if pool is null      */
DECL|method|ForkJoinWorkerThread
specifier|protected
name|ForkJoinWorkerThread
parameter_list|(
name|ForkJoinPool
name|pool
parameter_list|)
block|{
comment|// Use a placeholder until a useful name can be set in registerWorker
name|super
argument_list|(
literal|"aForkJoinWorkerThread"
argument_list|)
expr_stmt|;
name|this
operator|.
name|pool
operator|=
name|pool
expr_stmt|;
name|this
operator|.
name|workQueue
operator|=
name|pool
operator|.
name|registerWorker
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
comment|/**      * Returns the pool hosting this thread.      *      * @return the pool      */
DECL|method|getPool
specifier|public
name|ForkJoinPool
name|getPool
parameter_list|()
block|{
return|return
name|pool
return|;
block|}
comment|/**      * Returns the index number of this thread in its pool.  The      * returned value ranges from zero to the maximum number of      * threads (minus one) that have ever been created in the pool.      * This method may be useful for applications that track status or      * collect results per-worker rather than per-task.      *      * @return the index number      */
DECL|method|getPoolIndex
specifier|public
name|int
name|getPoolIndex
parameter_list|()
block|{
return|return
name|workQueue
operator|.
name|poolIndex
return|;
block|}
comment|/**      * Initializes internal state after construction but before      * processing any tasks. If you override this method, you must      * invoke {@code super.onStart()} at the beginning of the method.      * Initialization requires care: Most fields must have legal      * default values, to ensure that attempted accesses from other      * threads work correctly even before this thread starts      * processing tasks.      */
DECL|method|onStart
specifier|protected
name|void
name|onStart
parameter_list|()
block|{     }
comment|/**      * Performs cleanup associated with termination of this worker      * thread.  If you override this method, you must invoke      * {@code super.onTermination} at the end of the overridden method.      *      * @param exception the exception causing this thread to abort due      * to an unrecoverable error, or {@code null} if completed normally      */
DECL|method|onTermination
specifier|protected
name|void
name|onTermination
parameter_list|(
name|Throwable
name|exception
parameter_list|)
block|{     }
comment|/**      * This method is required to be public, but should never be      * called explicitly. It performs the main run loop to execute      * {@link ForkJoinTask}s.      */
DECL|method|run
specifier|public
name|void
name|run
parameter_list|()
block|{
name|Throwable
name|exception
init|=
literal|null
decl_stmt|;
try|try
block|{
name|onStart
argument_list|()
expr_stmt|;
name|pool
operator|.
name|runWorker
argument_list|(
name|workQueue
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|ex
parameter_list|)
block|{
name|exception
operator|=
name|ex
expr_stmt|;
block|}
finally|finally
block|{
try|try
block|{
name|onTermination
argument_list|(
name|exception
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|ex
parameter_list|)
block|{
if|if
condition|(
name|exception
operator|==
literal|null
condition|)
name|exception
operator|=
name|ex
expr_stmt|;
block|}
finally|finally
block|{
name|pool
operator|.
name|deregisterWorker
argument_list|(
name|this
argument_list|,
name|exception
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

