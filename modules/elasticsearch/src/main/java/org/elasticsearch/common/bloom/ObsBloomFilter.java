begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.bloom
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|bloom
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
name|util
operator|.
name|OpenBitSet
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
name|RamUsage
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|ByteBuffer
import|;
end_import

begin_class
DECL|class|ObsBloomFilter
specifier|public
class|class
name|ObsBloomFilter
implements|implements
name|BloomFilter
block|{
DECL|field|hashCount
specifier|private
specifier|final
name|int
name|hashCount
decl_stmt|;
DECL|field|bitset
specifier|private
specifier|final
name|OpenBitSet
name|bitset
decl_stmt|;
DECL|field|size
specifier|private
specifier|final
name|long
name|size
decl_stmt|;
DECL|method|ObsBloomFilter
name|ObsBloomFilter
parameter_list|(
name|int
name|hashCount
parameter_list|,
name|long
name|size
parameter_list|)
block|{
name|this
operator|.
name|hashCount
operator|=
name|hashCount
expr_stmt|;
name|this
operator|.
name|bitset
operator|=
operator|new
name|OpenBitSet
argument_list|(
name|size
argument_list|)
expr_stmt|;
name|this
operator|.
name|size
operator|=
name|size
expr_stmt|;
block|}
DECL|method|emptyBuckets
name|long
name|emptyBuckets
parameter_list|()
block|{
name|long
name|n
init|=
literal|0
decl_stmt|;
for|for
control|(
name|long
name|i
init|=
literal|0
init|;
name|i
operator|<
name|buckets
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
operator|!
name|bitset
operator|.
name|get
argument_list|(
name|i
argument_list|)
condition|)
block|{
name|n
operator|++
expr_stmt|;
block|}
block|}
return|return
name|n
return|;
block|}
DECL|method|buckets
specifier|private
name|long
name|buckets
parameter_list|()
block|{
return|return
name|size
return|;
block|}
DECL|method|getHashBuckets
specifier|private
name|long
index|[]
name|getHashBuckets
parameter_list|(
name|ByteBuffer
name|key
parameter_list|)
block|{
return|return
name|getHashBuckets
argument_list|(
name|key
argument_list|,
name|hashCount
argument_list|,
name|buckets
argument_list|()
argument_list|)
return|;
block|}
DECL|method|getHashBuckets
specifier|private
name|long
index|[]
name|getHashBuckets
parameter_list|(
name|byte
index|[]
name|key
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
block|{
return|return
name|getHashBuckets
argument_list|(
name|key
argument_list|,
name|offset
argument_list|,
name|length
argument_list|,
name|hashCount
argument_list|,
name|buckets
argument_list|()
argument_list|)
return|;
block|}
comment|// Murmur is faster than an SHA-based approach and provides as-good collision
comment|// resistance.  The combinatorial generation approach described in
comment|// http://www.eecs.harvard.edu/~kirsch/pubs/bbbf/esa06.pdf
comment|// does prove to work in actual tests, and is obviously faster
comment|// than performing further iterations of murmur.
DECL|method|getHashBuckets
specifier|static
name|long
index|[]
name|getHashBuckets
parameter_list|(
name|ByteBuffer
name|b
parameter_list|,
name|int
name|hashCount
parameter_list|,
name|long
name|max
parameter_list|)
block|{
name|long
index|[]
name|result
init|=
operator|new
name|long
index|[
name|hashCount
index|]
decl_stmt|;
name|long
name|hash1
init|=
name|MurmurHash
operator|.
name|hash64
argument_list|(
name|b
argument_list|,
name|b
operator|.
name|position
argument_list|()
argument_list|,
name|b
operator|.
name|remaining
argument_list|()
argument_list|,
literal|0L
argument_list|)
decl_stmt|;
name|long
name|hash2
init|=
name|MurmurHash
operator|.
name|hash64
argument_list|(
name|b
argument_list|,
name|b
operator|.
name|position
argument_list|()
argument_list|,
name|b
operator|.
name|remaining
argument_list|()
argument_list|,
name|hash1
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
name|hashCount
condition|;
operator|++
name|i
control|)
block|{
name|result
index|[
name|i
index|]
operator|=
name|Math
operator|.
name|abs
argument_list|(
operator|(
name|hash1
operator|+
operator|(
name|long
operator|)
name|i
operator|*
name|hash2
operator|)
operator|%
name|max
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
comment|// Murmur is faster than an SHA-based approach and provides as-good collision
comment|// resistance.  The combinatorial generation approach described in
comment|// http://www.eecs.harvard.edu/~kirsch/pubs/bbbf/esa06.pdf
comment|// does prove to work in actual tests, and is obviously faster
comment|// than performing further iterations of murmur.
DECL|method|getHashBuckets
specifier|static
name|long
index|[]
name|getHashBuckets
parameter_list|(
name|byte
index|[]
name|b
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|,
name|int
name|hashCount
parameter_list|,
name|long
name|max
parameter_list|)
block|{
name|long
index|[]
name|result
init|=
operator|new
name|long
index|[
name|hashCount
index|]
decl_stmt|;
name|long
name|hash1
init|=
name|MurmurHash
operator|.
name|hash64
argument_list|(
name|b
argument_list|,
name|offset
argument_list|,
name|length
argument_list|,
literal|0L
argument_list|)
decl_stmt|;
name|long
name|hash2
init|=
name|MurmurHash
operator|.
name|hash64
argument_list|(
name|b
argument_list|,
name|offset
argument_list|,
name|length
argument_list|,
name|hash1
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
name|hashCount
condition|;
operator|++
name|i
control|)
block|{
name|result
index|[
name|i
index|]
operator|=
name|Math
operator|.
name|abs
argument_list|(
operator|(
name|hash1
operator|+
operator|(
name|long
operator|)
name|i
operator|*
name|hash2
operator|)
operator|%
name|max
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
DECL|method|add
annotation|@
name|Override
specifier|public
name|void
name|add
parameter_list|(
name|byte
index|[]
name|key
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
block|{
for|for
control|(
name|long
name|bucketIndex
range|:
name|getHashBuckets
argument_list|(
name|key
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
control|)
block|{
name|bitset
operator|.
name|fastSet
argument_list|(
name|bucketIndex
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|add
specifier|public
name|void
name|add
parameter_list|(
name|ByteBuffer
name|key
parameter_list|)
block|{
for|for
control|(
name|long
name|bucketIndex
range|:
name|getHashBuckets
argument_list|(
name|key
argument_list|)
control|)
block|{
name|bitset
operator|.
name|fastSet
argument_list|(
name|bucketIndex
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|isPresent
annotation|@
name|Override
specifier|public
name|boolean
name|isPresent
parameter_list|(
name|byte
index|[]
name|key
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
block|{
for|for
control|(
name|long
name|bucketIndex
range|:
name|getHashBuckets
argument_list|(
name|key
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
control|)
block|{
if|if
condition|(
operator|!
name|bitset
operator|.
name|fastGet
argument_list|(
name|bucketIndex
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
return|return
literal|true
return|;
block|}
DECL|method|isPresent
specifier|public
name|boolean
name|isPresent
parameter_list|(
name|ByteBuffer
name|key
parameter_list|)
block|{
for|for
control|(
name|long
name|bucketIndex
range|:
name|getHashBuckets
argument_list|(
name|key
argument_list|)
control|)
block|{
if|if
condition|(
operator|!
name|bitset
operator|.
name|fastGet
argument_list|(
name|bucketIndex
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
return|return
literal|true
return|;
block|}
DECL|method|clear
specifier|public
name|void
name|clear
parameter_list|()
block|{
name|bitset
operator|.
name|clear
argument_list|(
literal|0
argument_list|,
name|bitset
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|sizeInBytes
annotation|@
name|Override
specifier|public
name|long
name|sizeInBytes
parameter_list|()
block|{
return|return
name|bitset
operator|.
name|getBits
argument_list|()
operator|.
name|length
operator|*
name|RamUsage
operator|.
name|NUM_BYTES_LONG
operator|+
name|RamUsage
operator|.
name|NUM_BYTES_ARRAY_HEADER
operator|+
name|RamUsage
operator|.
name|NUM_BYTES_INT
comment|/* wlen */
return|;
block|}
block|}
end_class

end_unit

