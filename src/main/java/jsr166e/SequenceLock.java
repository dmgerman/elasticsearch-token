begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Written by Doug Lea with assistance from members of JCP JSR-166  * Expert Group and released to the public domain, as explained at  * http://creativecommons.org/publicdomain/zero/1.0/  */
end_comment

begin_package
DECL|package|jsr166e
package|package
name|jsr166e
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|locks
operator|.
name|Lock
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
name|locks
operator|.
name|ReentrantLock
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
name|locks
operator|.
name|Condition
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
name|locks
operator|.
name|AbstractQueuedLongSynchronizer
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ObjectOutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ObjectInputStream
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

begin_comment
comment|/**  * A reentrant mutual exclusion {@link Lock} in which each lock  * acquisition or release advances a sequence number.  When the  * sequence number (accessible using {@link #getSequence()}) is odd,  * the lock is held. When it is even (i.e., ({@code lock.getSequence()  *& 1L) == 0L}), the lock is released. Method {@link  * #awaitAvailability} can be used to await availability of the lock,  * returning its current sequence number. Sequence numbers (as well as  * reentrant hold counts) are of type {@code long} to ensure that they  * will not wrap around until hundreds of years of use under current  * processor rates.  A SequenceLock can be created with a specified  * number of spins. Attempts to acquire the lock in method {@link  * #lock} will retry at least the given number of times before  * blocking. If not specified, a default, possibly platform-specific,  * value is used.  *  *<p>Except for the lack of support for specified fairness policies,  * or {@link Condition} objects, a SequenceLock can be used in the  * same way as {@link ReentrantLock}. It provides similar status and  * monitoring methods, such as {@link #isHeldByCurrentThread}.  * SequenceLocks may be preferable in contexts in which multiple  * threads invoke short read-only methods much more frequently than  * fully locked methods.  *  *<p> Methods {@code awaitAvailability} and {@code getSequence} can  * be used together to define (partially) optimistic read-only methods  * that are usually more efficient than ReadWriteLocks when they  * apply.  These methods should in general be structured as loops that  * await lock availability, then read {@code volatile} fields into  * local variables (and may further read other values derived from  * these, for example the {@code length} of a {@code volatile} array),  * and retry if the sequence number changed while doing so.  * Alternatively, because {@code awaitAvailability} accommodates  * reentrancy, a method can retry a bounded number of times before  * switching to locking mode.  While conceptually straightforward,  * expressing these ideas can be verbose. For example:  *  *<pre> {@code  * class Point {  *   private volatile double x, y;  *   private final SequenceLock sl = new SequenceLock();  *  *   // an exclusively locked method  *   void move(double deltaX, double deltaY) {  *     sl.lock();  *     try {  *       x += deltaX;  *       y += deltaY;  *     } finally {  *       sl.unlock();  *     }  *   }  *  *   // A read-only method  *   double distanceFromOriginV1() {  *     double currentX, currentY;  *     long seq;  *     do {  *       seq = sl.awaitAvailability();  *       currentX = x;  *       currentY = y;  *     } while (sl.getSequence() != seq); // retry if sequence changed  *     return Math.sqrt(currentX * currentX + currentY * currentY);  *   }  *  *   // Uses bounded retries before locking  *   double distanceFromOriginV2() {  *     double currentX, currentY;  *     long seq;  *     int retries = RETRIES_BEFORE_LOCKING; // for example 8  *     try {  *       do {  *         if (--retries< 0)  *           sl.lock();  *         seq = sl.awaitAvailability();  *         currentX = x;  *         currentY = y;  *       } while (sl.getSequence() != seq);  *     } finally {  *       if (retries< 0)  *         sl.unlock();  *     }  *     return Math.sqrt(currentX * currentX + currentY * currentY);  *   }  * }}</pre>  *  * @since 1.8  * @author Doug Lea  */
end_comment

begin_class
DECL|class|SequenceLock
specifier|public
class|class
name|SequenceLock
implements|implements
name|Lock
implements|,
name|java
operator|.
name|io
operator|.
name|Serializable
block|{
DECL|field|serialVersionUID
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|7373984872572414699L
decl_stmt|;
DECL|class|Sync
specifier|static
specifier|final
class|class
name|Sync
extends|extends
name|AbstractQueuedLongSynchronizer
block|{
DECL|field|serialVersionUID
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|2540673546047039555L
decl_stmt|;
comment|/**          * The number of times to spin in lock() and awaitAvailability().          */
DECL|field|spins
specifier|final
name|int
name|spins
decl_stmt|;
comment|/**          * The number of reentrant holds on this lock. Uses a long for          * compatibility with other AbstractQueuedLongSynchronizer          * operations. Accessed only by lock holder.          */
DECL|field|holds
name|long
name|holds
decl_stmt|;
DECL|method|Sync
name|Sync
parameter_list|(
name|int
name|spins
parameter_list|)
block|{
name|this
operator|.
name|spins
operator|=
name|spins
expr_stmt|;
block|}
comment|// overrides of AQLS methods
DECL|method|isHeldExclusively
specifier|public
specifier|final
name|boolean
name|isHeldExclusively
parameter_list|()
block|{
return|return
operator|(
name|getState
argument_list|()
operator|&
literal|1L
operator|)
operator|!=
literal|0L
operator|&&
name|getExclusiveOwnerThread
argument_list|()
operator|==
name|Thread
operator|.
name|currentThread
argument_list|()
return|;
block|}
DECL|method|tryAcquire
specifier|public
specifier|final
name|boolean
name|tryAcquire
parameter_list|(
name|long
name|acquires
parameter_list|)
block|{
name|Thread
name|current
init|=
name|Thread
operator|.
name|currentThread
argument_list|()
decl_stmt|;
name|long
name|c
init|=
name|getState
argument_list|()
decl_stmt|;
if|if
condition|(
operator|(
name|c
operator|&
literal|1L
operator|)
operator|==
literal|0L
condition|)
block|{
if|if
condition|(
name|compareAndSetState
argument_list|(
name|c
argument_list|,
name|c
operator|+
literal|1L
argument_list|)
condition|)
block|{
name|holds
operator|=
name|acquires
expr_stmt|;
name|setExclusiveOwnerThread
argument_list|(
name|current
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
block|}
elseif|else
if|if
condition|(
name|current
operator|==
name|getExclusiveOwnerThread
argument_list|()
condition|)
block|{
name|holds
operator|+=
name|acquires
expr_stmt|;
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
DECL|method|tryRelease
specifier|public
specifier|final
name|boolean
name|tryRelease
parameter_list|(
name|long
name|releases
parameter_list|)
block|{
if|if
condition|(
name|Thread
operator|.
name|currentThread
argument_list|()
operator|!=
name|getExclusiveOwnerThread
argument_list|()
condition|)
throw|throw
operator|new
name|IllegalMonitorStateException
argument_list|()
throw|;
if|if
condition|(
operator|(
name|holds
operator|-=
name|releases
operator|)
operator|==
literal|0L
condition|)
block|{
name|setExclusiveOwnerThread
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|setState
argument_list|(
name|getState
argument_list|()
operator|+
literal|1L
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
DECL|method|tryAcquireShared
specifier|public
specifier|final
name|long
name|tryAcquireShared
parameter_list|(
name|long
name|unused
parameter_list|)
block|{
return|return
operator|(
operator|(
operator|(
name|getState
argument_list|()
operator|&
literal|1L
operator|)
operator|==
literal|0L
operator|)
condition|?
literal|1L
else|:
operator|(
name|getExclusiveOwnerThread
argument_list|()
operator|==
name|Thread
operator|.
name|currentThread
argument_list|()
operator|)
condition|?
literal|0L
else|:
operator|-
literal|1L
operator|)
return|;
block|}
DECL|method|tryReleaseShared
specifier|public
specifier|final
name|boolean
name|tryReleaseShared
parameter_list|(
name|long
name|unused
parameter_list|)
block|{
return|return
operator|(
name|getState
argument_list|()
operator|&
literal|1L
operator|)
operator|==
literal|0L
return|;
block|}
DECL|method|newCondition
specifier|public
specifier|final
name|Condition
name|newCondition
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
comment|// Other methods in support of SequenceLock
DECL|method|getSequence
specifier|final
name|long
name|getSequence
parameter_list|()
block|{
return|return
name|getState
argument_list|()
return|;
block|}
DECL|method|lock
specifier|final
name|void
name|lock
parameter_list|()
block|{
name|int
name|k
init|=
name|spins
decl_stmt|;
while|while
condition|(
operator|!
name|tryAcquire
argument_list|(
literal|1L
argument_list|)
condition|)
block|{
if|if
condition|(
name|k
operator|==
literal|0
condition|)
block|{
name|acquire
argument_list|(
literal|1L
argument_list|)
expr_stmt|;
break|break;
block|}
operator|--
name|k
expr_stmt|;
block|}
block|}
DECL|method|awaitAvailability
specifier|final
name|long
name|awaitAvailability
parameter_list|()
block|{
name|long
name|s
decl_stmt|;
while|while
condition|(
operator|(
operator|(
name|s
operator|=
name|getState
argument_list|()
operator|)
operator|&
literal|1L
operator|)
operator|!=
literal|0L
operator|&&
name|getExclusiveOwnerThread
argument_list|()
operator|!=
name|Thread
operator|.
name|currentThread
argument_list|()
condition|)
block|{
name|acquireShared
argument_list|(
literal|1L
argument_list|)
expr_stmt|;
name|releaseShared
argument_list|(
literal|1L
argument_list|)
expr_stmt|;
block|}
return|return
name|s
return|;
block|}
DECL|method|tryAwaitAvailability
specifier|final
name|long
name|tryAwaitAvailability
parameter_list|(
name|long
name|nanos
parameter_list|)
throws|throws
name|InterruptedException
throws|,
name|TimeoutException
block|{
name|Thread
name|current
init|=
name|Thread
operator|.
name|currentThread
argument_list|()
decl_stmt|;
for|for
control|(
init|;
condition|;
control|)
block|{
name|long
name|s
init|=
name|getState
argument_list|()
decl_stmt|;
if|if
condition|(
operator|(
name|s
operator|&
literal|1L
operator|)
operator|==
literal|0L
operator|||
name|getExclusiveOwnerThread
argument_list|()
operator|==
name|current
condition|)
block|{
name|releaseShared
argument_list|(
literal|1L
argument_list|)
expr_stmt|;
return|return
name|s
return|;
block|}
if|if
condition|(
operator|!
name|tryAcquireSharedNanos
argument_list|(
literal|1L
argument_list|,
name|nanos
argument_list|)
condition|)
throw|throw
operator|new
name|TimeoutException
argument_list|()
throw|;
comment|// since tryAcquireSharedNanos doesn't return seq
comment|// retry with minimal wait time.
name|nanos
operator|=
literal|1L
expr_stmt|;
block|}
block|}
DECL|method|isLocked
specifier|final
name|boolean
name|isLocked
parameter_list|()
block|{
return|return
operator|(
name|getState
argument_list|()
operator|&
literal|1L
operator|)
operator|!=
literal|0L
return|;
block|}
DECL|method|getOwner
specifier|final
name|Thread
name|getOwner
parameter_list|()
block|{
return|return
operator|(
name|getState
argument_list|()
operator|&
literal|1L
operator|)
operator|==
literal|0L
condition|?
literal|null
else|:
name|getExclusiveOwnerThread
argument_list|()
return|;
block|}
DECL|method|getHoldCount
specifier|final
name|long
name|getHoldCount
parameter_list|()
block|{
return|return
name|isHeldExclusively
argument_list|()
condition|?
name|holds
else|:
literal|0
return|;
block|}
DECL|method|readObject
specifier|private
name|void
name|readObject
parameter_list|(
name|ObjectInputStream
name|s
parameter_list|)
throws|throws
name|IOException
throws|,
name|ClassNotFoundException
block|{
name|s
operator|.
name|defaultReadObject
argument_list|()
expr_stmt|;
name|holds
operator|=
literal|0L
expr_stmt|;
name|setState
argument_list|(
literal|0L
argument_list|)
expr_stmt|;
comment|// reset to unlocked state
block|}
block|}
DECL|field|sync
specifier|private
specifier|final
name|Sync
name|sync
decl_stmt|;
comment|/**      * The default spin value for constructor. Future versions of this      * class might choose platform-specific values.  Currently, except      * on uniprocessors, it is set to a small value that overcomes near      * misses between releases and acquires.      */
DECL|field|DEFAULT_SPINS
specifier|static
specifier|final
name|int
name|DEFAULT_SPINS
init|=
name|Runtime
operator|.
name|getRuntime
argument_list|()
operator|.
name|availableProcessors
argument_list|()
operator|>
literal|1
condition|?
literal|64
else|:
literal|0
decl_stmt|;
comment|/**      * Creates an instance of {@code SequenceLock} with the default      * number of retry attempts to acquire the lock before blocking.      */
DECL|method|SequenceLock
specifier|public
name|SequenceLock
parameter_list|()
block|{
name|sync
operator|=
operator|new
name|Sync
argument_list|(
name|DEFAULT_SPINS
argument_list|)
expr_stmt|;
block|}
comment|/**      * Creates an instance of {@code SequenceLock} that will retry      * attempts to acquire the lock at least the given number times      * before blocking.      */
DECL|method|SequenceLock
specifier|public
name|SequenceLock
parameter_list|(
name|int
name|spins
parameter_list|)
block|{
name|sync
operator|=
operator|new
name|Sync
argument_list|(
name|spins
argument_list|)
expr_stmt|;
block|}
comment|/**      * Returns the current sequence number of this lock.  The sequence      * number is advanced upon each acquire or release action. When      * this value is odd, the lock is held; when even, it is released.      *      * @return the current sequence number      */
DECL|method|getSequence
specifier|public
name|long
name|getSequence
parameter_list|()
block|{
return|return
name|sync
operator|.
name|getSequence
argument_list|()
return|;
block|}
comment|/**      * Returns the current sequence number when the lock is, or      * becomes, available. A lock is available if it is either      * released, or is held by the current thread.  If the lock is not      * available, the current thread becomes disabled for thread      * scheduling purposes and lies dormant until the lock has been      * released by some other thread.      *      * @return the current sequence number      */
DECL|method|awaitAvailability
specifier|public
name|long
name|awaitAvailability
parameter_list|()
block|{
return|return
name|sync
operator|.
name|awaitAvailability
argument_list|()
return|;
block|}
comment|/**      * Returns the current sequence number if the lock is, or      * becomes, available within the specified waiting time.      *      *<p>If the lock is not available, the current thread becomes      * disabled for thread scheduling purposes and lies dormant until      * one of three things happens:      *      *<ul>      *      *<li>The lock becomes available, in which case the current      * sequence number is returned.      *      *<li>Some other thread {@linkplain Thread#interrupt interrupts}      * the current thread, in which case this method throws      * {@link InterruptedException}.      *      *<li>The specified waiting time elapses, in which case      * this method throws {@link TimeoutException}.      *      *</ul>      *      * @param timeout the time to wait for availability      * @param unit the time unit of the timeout argument      * @return the current sequence number if the lock is available      *         upon return from this method      * @throws InterruptedException if the current thread is interrupted      * @throws TimeoutException if the lock was not available within      * the specified waiting time      * @throws NullPointerException if the time unit is null      */
DECL|method|tryAwaitAvailability
specifier|public
name|long
name|tryAwaitAvailability
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
name|TimeoutException
block|{
return|return
name|sync
operator|.
name|tryAwaitAvailability
argument_list|(
name|unit
operator|.
name|toNanos
argument_list|(
name|timeout
argument_list|)
argument_list|)
return|;
block|}
comment|/**      * Acquires the lock.      *      *<p>If the current thread already holds this lock then the hold count      * is incremented by one and the method returns immediately without      * incrementing the sequence number.      *      *<p>If this lock not held by another thread, this method      * increments the sequence number (which thus becomes an odd      * number), sets the lock hold count to one, and returns      * immediately.      *      *<p>If the lock is held by another thread then the current      * thread may retry acquiring this lock, depending on the {@code      * spin} count established in constructor.  If the lock is still      * not acquired, the current thread becomes disabled for thread      * scheduling purposes and lies dormant until enabled by      * some other thread releasing the lock.      */
DECL|method|lock
specifier|public
name|void
name|lock
parameter_list|()
block|{
name|sync
operator|.
name|lock
argument_list|()
expr_stmt|;
block|}
comment|/**      * Acquires the lock unless the current thread is      * {@linkplain Thread#interrupt interrupted}.      *      *<p>If the current thread already holds this lock then the hold count      * is incremented by one and the method returns immediately without      * incrementing the sequence number.      *      *<p>If this lock not held by another thread, this method      * increments the sequence number (which thus becomes an odd      * number), sets the lock hold count to one, and returns      * immediately.      *      *<p>If the lock is held by another thread then the current      * thread may retry acquiring this lock, depending on the {@code      * spin} count established in constructor.  If the lock is still      * not acquired, the current thread becomes disabled for thread      * scheduling purposes and lies dormant until one of two things      * happens:      *      *<ul>      *      *<li>The lock is acquired by the current thread; or      *      *<li>Some other thread {@linkplain Thread#interrupt interrupts} the      * current thread.      *      *</ul>      *      *<p>If the lock is acquired by the current thread then the lock hold      * count is set to one and the sequence number is incremented.      *      *<p>If the current thread:      *      *<ul>      *      *<li>has its interrupted status set on entry to this method; or      *      *<li>is {@linkplain Thread#interrupt interrupted} while acquiring      * the lock,      *      *</ul>      *      * then {@link InterruptedException} is thrown and the current thread's      * interrupted status is cleared.      *      *<p>In this implementation, as this method is an explicit      * interruption point, preference is given to responding to the      * interrupt over normal or reentrant acquisition of the lock.      *      * @throws InterruptedException if the current thread is interrupted      */
DECL|method|lockInterruptibly
specifier|public
name|void
name|lockInterruptibly
parameter_list|()
throws|throws
name|InterruptedException
block|{
name|sync
operator|.
name|acquireInterruptibly
argument_list|(
literal|1L
argument_list|)
expr_stmt|;
block|}
comment|/**      * Acquires the lock only if it is not held by another thread at the time      * of invocation.      *      *<p>If the current thread already holds this lock then the hold      * count is incremented by one and the method returns {@code true}      * without incrementing the sequence number.      *      *<p>If this lock not held by another thread, this method      * increments the sequence number (which thus becomes an odd      * number), sets the lock hold count to one, and returns {@code      * true}.      *      *<p>If the lock is held by another thread then this method      * returns {@code false}.      *      * @return {@code true} if the lock was free and was acquired by the      *         current thread, or the lock was already held by the current      *         thread; and {@code false} otherwise      */
DECL|method|tryLock
specifier|public
name|boolean
name|tryLock
parameter_list|()
block|{
return|return
name|sync
operator|.
name|tryAcquire
argument_list|(
literal|1L
argument_list|)
return|;
block|}
comment|/**      * Acquires the lock if it is not held by another thread within the given      * waiting time and the current thread has not been      * {@linkplain Thread#interrupt interrupted}.      *      *<p>If the current thread already holds this lock then the hold count      * is incremented by one and the method returns immediately without      * incrementing the sequence number.      *      *<p>If this lock not held by another thread, this method      * increments the sequence number (which thus becomes an odd      * number), sets the lock hold count to one, and returns      * immediately.      *      *<p>If the lock is held by another thread then the current      * thread may retry acquiring this lock, depending on the {@code      * spin} count established in constructor.  If the lock is still      * not acquired, the current thread becomes disabled for thread      * scheduling purposes and lies dormant until one of three things      * happens:      *      *<ul>      *      *<li>The lock is acquired by the current thread; or      *      *<li>Some other thread {@linkplain Thread#interrupt interrupts}      * the current thread; or      *      *<li>The specified waiting time elapses      *      *</ul>      *      *<p>If the lock is acquired then the value {@code true} is returned and      * the lock hold count is set to one.      *      *<p>If the current thread:      *      *<ul>      *      *<li>has its interrupted status set on entry to this method; or      *      *<li>is {@linkplain Thread#interrupt interrupted} while      * acquiring the lock,      *      *</ul>      * then {@link InterruptedException} is thrown and the current thread's      * interrupted status is cleared.      *      *<p>If the specified waiting time elapses then the value {@code false}      * is returned.  If the time is less than or equal to zero, the method      * will not wait at all.      *      *<p>In this implementation, as this method is an explicit      * interruption point, preference is given to responding to the      * interrupt over normal or reentrant acquisition of the lock, and      * over reporting the elapse of the waiting time.      *      * @param timeout the time to wait for the lock      * @param unit the time unit of the timeout argument      * @return {@code true} if the lock was free and was acquired by the      *         current thread, or the lock was already held by the current      *         thread; and {@code false} if the waiting time elapsed before      *         the lock could be acquired      * @throws InterruptedException if the current thread is interrupted      * @throws NullPointerException if the time unit is null      *      */
DECL|method|tryLock
specifier|public
name|boolean
name|tryLock
parameter_list|(
name|long
name|timeout
parameter_list|,
name|TimeUnit
name|unit
parameter_list|)
throws|throws
name|InterruptedException
block|{
return|return
name|sync
operator|.
name|tryAcquireNanos
argument_list|(
literal|1L
argument_list|,
name|unit
operator|.
name|toNanos
argument_list|(
name|timeout
argument_list|)
argument_list|)
return|;
block|}
comment|/**      * Attempts to release this lock.      *      *<p>If the current thread is the holder of this lock then the      * hold count is decremented.  If the hold count is now zero then      * the sequence number is incremented (thus becoming an even      * number) and the lock is released.  If the current thread is not      * the holder of this lock then {@link      * IllegalMonitorStateException} is thrown.      *      * @throws IllegalMonitorStateException if the current thread does not      *         hold this lock      */
DECL|method|unlock
specifier|public
name|void
name|unlock
parameter_list|()
block|{
name|sync
operator|.
name|release
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
comment|/**      * Throws UnsupportedOperationException. SequenceLocks      * do not support Condition objects.      *      * @throws UnsupportedOperationException      */
DECL|method|newCondition
specifier|public
name|Condition
name|newCondition
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
comment|/**      * Queries the number of holds on this lock by the current thread.      *      *<p>A thread has a hold on a lock for each lock action that is not      * matched by an unlock action.      *      *<p>The hold count information is typically only used for testing and      * debugging purposes.      *      * @return the number of holds on this lock by the current thread,      *         or zero if this lock is not held by the current thread      */
DECL|method|getHoldCount
specifier|public
name|long
name|getHoldCount
parameter_list|()
block|{
return|return
name|sync
operator|.
name|getHoldCount
argument_list|()
return|;
block|}
comment|/**      * Queries if this lock is held by the current thread.      *      * @return {@code true} if current thread holds this lock and      *         {@code false} otherwise      */
DECL|method|isHeldByCurrentThread
specifier|public
name|boolean
name|isHeldByCurrentThread
parameter_list|()
block|{
return|return
name|sync
operator|.
name|isHeldExclusively
argument_list|()
return|;
block|}
comment|/**      * Queries if this lock is held by any thread. This method is      * designed for use in monitoring of the system state,      * not for synchronization control.      *      * @return {@code true} if any thread holds this lock and      *         {@code false} otherwise      */
DECL|method|isLocked
specifier|public
name|boolean
name|isLocked
parameter_list|()
block|{
return|return
name|sync
operator|.
name|isLocked
argument_list|()
return|;
block|}
comment|/**      * Returns the thread that currently owns this lock, or      * {@code null} if not owned. When this method is called by a      * thread that is not the owner, the return value reflects a      * best-effort approximation of current lock status. For example,      * the owner may be momentarily {@code null} even if there are      * threads trying to acquire the lock but have not yet done so.      * This method is designed to facilitate construction of      * subclasses that provide more extensive lock monitoring      * facilities.      *      * @return the owner, or {@code null} if not owned      */
DECL|method|getOwner
specifier|protected
name|Thread
name|getOwner
parameter_list|()
block|{
return|return
name|sync
operator|.
name|getOwner
argument_list|()
return|;
block|}
comment|/**      * Queries whether any threads are waiting to acquire this lock. Note that      * because cancellations may occur at any time, a {@code true}      * return does not guarantee that any other thread will ever      * acquire this lock.  This method is designed primarily for use in      * monitoring of the system state.      *      * @return {@code true} if there may be other threads waiting to      *         acquire the lock      */
DECL|method|hasQueuedThreads
specifier|public
specifier|final
name|boolean
name|hasQueuedThreads
parameter_list|()
block|{
return|return
name|sync
operator|.
name|hasQueuedThreads
argument_list|()
return|;
block|}
comment|/**      * Queries whether the given thread is waiting to acquire this      * lock. Note that because cancellations may occur at any time, a      * {@code true} return does not guarantee that this thread      * will ever acquire this lock.  This method is designed primarily for use      * in monitoring of the system state.      *      * @param thread the thread      * @return {@code true} if the given thread is queued waiting for this lock      * @throws NullPointerException if the thread is null      */
DECL|method|hasQueuedThread
specifier|public
specifier|final
name|boolean
name|hasQueuedThread
parameter_list|(
name|Thread
name|thread
parameter_list|)
block|{
return|return
name|sync
operator|.
name|isQueued
argument_list|(
name|thread
argument_list|)
return|;
block|}
comment|/**      * Returns an estimate of the number of threads waiting to      * acquire this lock.  The value is only an estimate because the number of      * threads may change dynamically while this method traverses      * internal data structures.  This method is designed for use in      * monitoring of the system state, not for synchronization      * control.      *      * @return the estimated number of threads waiting for this lock      */
DECL|method|getQueueLength
specifier|public
specifier|final
name|int
name|getQueueLength
parameter_list|()
block|{
return|return
name|sync
operator|.
name|getQueueLength
argument_list|()
return|;
block|}
comment|/**      * Returns a collection containing threads that may be waiting to      * acquire this lock.  Because the actual set of threads may change      * dynamically while constructing this result, the returned      * collection is only a best-effort estimate.  The elements of the      * returned collection are in no particular order.  This method is      * designed to facilitate construction of subclasses that provide      * more extensive monitoring facilities.      *      * @return the collection of threads      */
DECL|method|getQueuedThreads
specifier|protected
name|Collection
argument_list|<
name|Thread
argument_list|>
name|getQueuedThreads
parameter_list|()
block|{
return|return
name|sync
operator|.
name|getQueuedThreads
argument_list|()
return|;
block|}
comment|/**      * Returns a string identifying this lock, as well as its lock state.      * The state, in brackets, includes either the String {@code "Unlocked"}      * or the String {@code "Locked by"} followed by the      * {@linkplain Thread#getName name} of the owning thread.      *      * @return a string identifying this lock, as well as its lock state      */
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|Thread
name|o
init|=
name|sync
operator|.
name|getOwner
argument_list|()
decl_stmt|;
return|return
name|super
operator|.
name|toString
argument_list|()
operator|+
operator|(
operator|(
name|o
operator|==
literal|null
operator|)
condition|?
literal|"[Unlocked]"
else|:
literal|"[Locked by thread "
operator|+
name|o
operator|.
name|getName
argument_list|()
operator|+
literal|"]"
operator|)
return|;
block|}
block|}
end_class

end_unit

