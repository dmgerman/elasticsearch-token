begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.util
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|util
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
name|ArrayUtil
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
name|util
operator|.
name|RamUsageEstimator
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cache
operator|.
name|recycler
operator|.
name|PageCacheRecycler
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|util
operator|.
name|BigArrays
operator|.
name|INT_PAGE_SIZE
import|;
end_import

begin_comment
comment|/**  * Int array abstraction able to support more than 2B values. This implementation slices data into fixed-sized blocks of  * configurable length.  */
end_comment

begin_class
DECL|class|BigIntArray
specifier|final
class|class
name|BigIntArray
extends|extends
name|AbstractBigArray
implements|implements
name|IntArray
block|{
DECL|field|pages
specifier|private
name|int
index|[]
index|[]
name|pages
decl_stmt|;
comment|/** Constructor. */
DECL|method|BigIntArray
specifier|public
name|BigIntArray
parameter_list|(
name|long
name|size
parameter_list|,
name|PageCacheRecycler
name|recycler
parameter_list|,
name|boolean
name|clearOnResize
parameter_list|)
block|{
name|super
argument_list|(
name|INT_PAGE_SIZE
argument_list|,
name|recycler
argument_list|,
name|clearOnResize
argument_list|)
expr_stmt|;
name|this
operator|.
name|size
operator|=
name|size
expr_stmt|;
name|pages
operator|=
operator|new
name|int
index|[
name|numPages
argument_list|(
name|size
argument_list|)
index|]
index|[]
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
name|pages
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|pages
index|[
name|i
index|]
operator|=
name|newIntPage
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|get
specifier|public
name|int
name|get
parameter_list|(
name|long
name|index
parameter_list|)
block|{
specifier|final
name|int
name|pageIndex
init|=
name|pageIndex
argument_list|(
name|index
argument_list|)
decl_stmt|;
specifier|final
name|int
name|indexInPage
init|=
name|indexInPage
argument_list|(
name|index
argument_list|)
decl_stmt|;
return|return
name|pages
index|[
name|pageIndex
index|]
index|[
name|indexInPage
index|]
return|;
block|}
annotation|@
name|Override
DECL|method|set
specifier|public
name|int
name|set
parameter_list|(
name|long
name|index
parameter_list|,
name|int
name|value
parameter_list|)
block|{
specifier|final
name|int
name|pageIndex
init|=
name|pageIndex
argument_list|(
name|index
argument_list|)
decl_stmt|;
specifier|final
name|int
name|indexInPage
init|=
name|indexInPage
argument_list|(
name|index
argument_list|)
decl_stmt|;
specifier|final
name|int
index|[]
name|page
init|=
name|pages
index|[
name|pageIndex
index|]
decl_stmt|;
specifier|final
name|int
name|ret
init|=
name|page
index|[
name|indexInPage
index|]
decl_stmt|;
name|page
index|[
name|indexInPage
index|]
operator|=
name|value
expr_stmt|;
return|return
name|ret
return|;
block|}
annotation|@
name|Override
DECL|method|increment
specifier|public
name|int
name|increment
parameter_list|(
name|long
name|index
parameter_list|,
name|int
name|inc
parameter_list|)
block|{
specifier|final
name|int
name|pageIndex
init|=
name|pageIndex
argument_list|(
name|index
argument_list|)
decl_stmt|;
specifier|final
name|int
name|indexInPage
init|=
name|indexInPage
argument_list|(
name|index
argument_list|)
decl_stmt|;
return|return
name|pages
index|[
name|pageIndex
index|]
index|[
name|indexInPage
index|]
operator|+=
name|inc
return|;
block|}
annotation|@
name|Override
DECL|method|numBytesPerElement
specifier|protected
name|int
name|numBytesPerElement
parameter_list|()
block|{
return|return
name|RamUsageEstimator
operator|.
name|NUM_BYTES_INT
return|;
block|}
comment|/** Change the size of this array. Content between indexes<code>0</code> and<code>min(size(), newSize)</code> will be preserved. */
DECL|method|resize
specifier|public
name|void
name|resize
parameter_list|(
name|long
name|newSize
parameter_list|)
block|{
specifier|final
name|int
name|numPages
init|=
name|numPages
argument_list|(
name|newSize
argument_list|)
decl_stmt|;
if|if
condition|(
name|numPages
operator|>
name|pages
operator|.
name|length
condition|)
block|{
name|pages
operator|=
name|Arrays
operator|.
name|copyOf
argument_list|(
name|pages
argument_list|,
name|ArrayUtil
operator|.
name|oversize
argument_list|(
name|numPages
argument_list|,
name|RamUsageEstimator
operator|.
name|NUM_BYTES_OBJECT_REF
argument_list|)
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
name|numPages
operator|-
literal|1
init|;
name|i
operator|>=
literal|0
operator|&&
name|pages
index|[
name|i
index|]
operator|==
literal|null
condition|;
operator|--
name|i
control|)
block|{
name|pages
index|[
name|i
index|]
operator|=
name|newIntPage
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
name|numPages
init|;
name|i
operator|<
name|pages
operator|.
name|length
operator|&&
name|pages
index|[
name|i
index|]
operator|!=
literal|null
condition|;
operator|++
name|i
control|)
block|{
name|pages
index|[
name|i
index|]
operator|=
literal|null
expr_stmt|;
name|releasePage
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|size
operator|=
name|newSize
expr_stmt|;
block|}
block|}
end_class

end_unit

