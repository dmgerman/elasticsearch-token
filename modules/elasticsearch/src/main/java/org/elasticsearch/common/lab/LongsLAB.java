begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.lab
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|lab
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
name|Preconditions
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

begin_class
DECL|class|LongsLAB
specifier|public
class|class
name|LongsLAB
block|{
DECL|field|curChunk
specifier|private
name|AtomicReference
argument_list|<
name|Chunk
argument_list|>
name|curChunk
init|=
operator|new
name|AtomicReference
argument_list|<
name|Chunk
argument_list|>
argument_list|()
decl_stmt|;
DECL|field|chunkSize
specifier|final
name|int
name|chunkSize
decl_stmt|;
DECL|field|maxAlloc
specifier|final
name|int
name|maxAlloc
decl_stmt|;
DECL|method|LongsLAB
specifier|public
name|LongsLAB
parameter_list|(
name|int
name|chunkSize
parameter_list|,
name|int
name|maxAlloc
parameter_list|)
block|{
name|this
operator|.
name|chunkSize
operator|=
name|chunkSize
expr_stmt|;
name|this
operator|.
name|maxAlloc
operator|=
name|maxAlloc
expr_stmt|;
comment|// if we don't exclude allocations>CHUNK_SIZE, we'd infiniteloop on one!
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|maxAlloc
operator|<=
name|chunkSize
argument_list|)
expr_stmt|;
block|}
comment|/**      * Allocate a slice of the given length.      *      * If the size is larger than the maximum size specified for this      * allocator, returns null.      */
DECL|method|allocateLongs
specifier|public
name|Allocation
name|allocateLongs
parameter_list|(
name|int
name|size
parameter_list|)
block|{
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|size
operator|>=
literal|0
argument_list|,
literal|"negative size"
argument_list|)
expr_stmt|;
comment|// Callers should satisfy large allocations directly from JVM since they
comment|// don't cause fragmentation as badly.
if|if
condition|(
name|size
operator|>
name|maxAlloc
condition|)
block|{
return|return
literal|null
return|;
block|}
while|while
condition|(
literal|true
condition|)
block|{
name|Chunk
name|c
init|=
name|getOrMakeChunk
argument_list|()
decl_stmt|;
comment|// Try to allocate from this chunk
name|int
name|allocOffset
init|=
name|c
operator|.
name|alloc
argument_list|(
name|size
argument_list|)
decl_stmt|;
if|if
condition|(
name|allocOffset
operator|!=
operator|-
literal|1
condition|)
block|{
comment|// We succeeded - this is the common case - small alloc
comment|// from a big buffer
return|return
operator|new
name|Allocation
argument_list|(
name|c
operator|.
name|data
argument_list|,
name|allocOffset
argument_list|)
return|;
block|}
comment|// not enough space!
comment|// try to retire this chunk
name|tryRetireChunk
argument_list|(
name|c
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      * Try to retire the current chunk if it is still      *<code>c</code>. Postcondition is that curChunk.get()      * != c      */
DECL|method|tryRetireChunk
specifier|private
name|void
name|tryRetireChunk
parameter_list|(
name|Chunk
name|c
parameter_list|)
block|{
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
name|boolean
name|weRetiredIt
init|=
name|curChunk
operator|.
name|compareAndSet
argument_list|(
name|c
argument_list|,
literal|null
argument_list|)
decl_stmt|;
comment|// If the CAS succeeds, that means that we won the race
comment|// to retire the chunk. We could use this opportunity to
comment|// update metrics on external fragmentation.
comment|//
comment|// If the CAS fails, that means that someone else already
comment|// retired the chunk for us.
block|}
comment|/**      * Get the current chunk, or, if there is no current chunk,      * allocate a new one from the JVM.      */
DECL|method|getOrMakeChunk
specifier|private
name|Chunk
name|getOrMakeChunk
parameter_list|()
block|{
while|while
condition|(
literal|true
condition|)
block|{
comment|// Try to get the chunk
name|Chunk
name|c
init|=
name|curChunk
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|c
operator|!=
literal|null
condition|)
block|{
return|return
name|c
return|;
block|}
comment|// No current chunk, so we want to allocate one. We race
comment|// against other allocators to CAS in an uninitialized chunk
comment|// (which is cheap to allocate)
name|c
operator|=
operator|new
name|Chunk
argument_list|(
name|chunkSize
argument_list|)
expr_stmt|;
if|if
condition|(
name|curChunk
operator|.
name|compareAndSet
argument_list|(
literal|null
argument_list|,
name|c
argument_list|)
condition|)
block|{
comment|// we won race - now we need to actually do the expensive
comment|// allocation step
name|c
operator|.
name|init
argument_list|()
expr_stmt|;
return|return
name|c
return|;
block|}
comment|// someone else won race - that's fine, we'll try to grab theirs
comment|// in the next iteration of the loop.
block|}
block|}
comment|/**      * A chunk of memory out of which allocations are sliced.      */
DECL|class|Chunk
specifier|private
specifier|static
class|class
name|Chunk
block|{
comment|/**          * Actual underlying data          */
DECL|field|data
specifier|private
name|long
index|[]
name|data
decl_stmt|;
DECL|field|UNINITIALIZED
specifier|private
specifier|static
specifier|final
name|int
name|UNINITIALIZED
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|OOM
specifier|private
specifier|static
specifier|final
name|int
name|OOM
init|=
operator|-
literal|2
decl_stmt|;
comment|/**          * Offset for the next allocation, or the sentinel value -1          * which implies that the chunk is still uninitialized.          */
DECL|field|nextFreeOffset
specifier|private
name|AtomicInteger
name|nextFreeOffset
init|=
operator|new
name|AtomicInteger
argument_list|(
name|UNINITIALIZED
argument_list|)
decl_stmt|;
comment|/**          * Total number of allocations satisfied from this buffer          */
DECL|field|allocCount
specifier|private
name|AtomicInteger
name|allocCount
init|=
operator|new
name|AtomicInteger
argument_list|()
decl_stmt|;
comment|/**          * Size of chunk in longs          */
DECL|field|size
specifier|private
specifier|final
name|int
name|size
decl_stmt|;
comment|/**          * Create an uninitialized chunk. Note that memory is not allocated yet, so          * this is cheap.          *          * @param size in longs          */
DECL|method|Chunk
specifier|private
name|Chunk
parameter_list|(
name|int
name|size
parameter_list|)
block|{
name|this
operator|.
name|size
operator|=
name|size
expr_stmt|;
block|}
comment|/**          * Actually claim the memory for this chunk. This should only be called from          * the thread that constructed the chunk. It is thread-safe against other          * threads calling alloc(), who will block until the allocation is complete.          */
DECL|method|init
specifier|public
name|void
name|init
parameter_list|()
block|{
assert|assert
name|nextFreeOffset
operator|.
name|get
argument_list|()
operator|==
name|UNINITIALIZED
assert|;
try|try
block|{
name|data
operator|=
operator|new
name|long
index|[
name|size
index|]
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|OutOfMemoryError
name|e
parameter_list|)
block|{
name|boolean
name|failInit
init|=
name|nextFreeOffset
operator|.
name|compareAndSet
argument_list|(
name|UNINITIALIZED
argument_list|,
name|OOM
argument_list|)
decl_stmt|;
assert|assert
name|failInit
assert|;
comment|// should be true.
throw|throw
name|e
throw|;
block|}
comment|// Mark that it's ready for use
name|boolean
name|initted
init|=
name|nextFreeOffset
operator|.
name|compareAndSet
argument_list|(
name|UNINITIALIZED
argument_list|,
literal|0
argument_list|)
decl_stmt|;
comment|// We should always succeed the above CAS since only one thread
comment|// calls init()!
name|Preconditions
operator|.
name|checkState
argument_list|(
name|initted
argument_list|,
literal|"Multiple threads tried to init same chunk"
argument_list|)
expr_stmt|;
block|}
comment|/**          * Try to allocate<code>size</code> longs from the chunk.          *          * @return the offset of the successful allocation, or -1 to indicate not-enough-space          */
DECL|method|alloc
specifier|public
name|int
name|alloc
parameter_list|(
name|int
name|size
parameter_list|)
block|{
while|while
condition|(
literal|true
condition|)
block|{
name|int
name|oldOffset
init|=
name|nextFreeOffset
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|oldOffset
operator|==
name|UNINITIALIZED
condition|)
block|{
comment|// The chunk doesn't have its data allocated yet.
comment|// Since we found this in curChunk, we know that whoever
comment|// CAS-ed it there is allocating it right now. So spin-loop
comment|// shouldn't spin long!
name|Thread
operator|.
name|yield
argument_list|()
expr_stmt|;
continue|continue;
block|}
if|if
condition|(
name|oldOffset
operator|==
name|OOM
condition|)
block|{
comment|// doh we ran out of ram. return -1 to chuck this away.
return|return
operator|-
literal|1
return|;
block|}
if|if
condition|(
name|oldOffset
operator|+
name|size
operator|>
name|data
operator|.
name|length
condition|)
block|{
return|return
operator|-
literal|1
return|;
comment|// alloc doesn't fit
block|}
comment|// Try to atomically claim this chunk
if|if
condition|(
name|nextFreeOffset
operator|.
name|compareAndSet
argument_list|(
name|oldOffset
argument_list|,
name|oldOffset
operator|+
name|size
argument_list|)
condition|)
block|{
comment|// we got the alloc
name|allocCount
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
return|return
name|oldOffset
return|;
block|}
comment|// we raced and lost alloc, try again
block|}
block|}
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"Chunk@"
operator|+
name|System
operator|.
name|identityHashCode
argument_list|(
name|this
argument_list|)
operator|+
literal|" allocs="
operator|+
name|allocCount
operator|.
name|get
argument_list|()
operator|+
literal|"waste="
operator|+
operator|(
name|data
operator|.
name|length
operator|-
name|nextFreeOffset
operator|.
name|get
argument_list|()
operator|)
return|;
block|}
block|}
comment|/**      * The result of a single allocation. Contains the chunk that the      * allocation points into, and the offset in this array where the      * slice begins.      */
DECL|class|Allocation
specifier|public
specifier|static
class|class
name|Allocation
block|{
DECL|field|data
specifier|private
specifier|final
name|long
index|[]
name|data
decl_stmt|;
DECL|field|offset
specifier|private
specifier|final
name|int
name|offset
decl_stmt|;
DECL|method|Allocation
specifier|private
name|Allocation
parameter_list|(
name|long
index|[]
name|data
parameter_list|,
name|int
name|off
parameter_list|)
block|{
name|this
operator|.
name|data
operator|=
name|data
expr_stmt|;
name|this
operator|.
name|offset
operator|=
name|off
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"Allocation(data="
operator|+
name|data
operator|+
literal|" with capacity="
operator|+
name|data
operator|.
name|length
operator|+
literal|", off="
operator|+
name|offset
operator|+
literal|")"
return|;
block|}
DECL|method|getData
specifier|public
name|long
index|[]
name|getData
parameter_list|()
block|{
return|return
name|data
return|;
block|}
DECL|method|getOffset
specifier|public
name|int
name|getOffset
parameter_list|()
block|{
return|return
name|offset
return|;
block|}
block|}
block|}
end_class

end_unit

