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

begin_comment
comment|/**  * A GC friendly long[].  * Allocating large arrays (that are not short-lived) generate fragmentation  * in old-gen space. This breaks such large long array into fixed size pages  * to avoid that problem.  */
end_comment

begin_class
DECL|class|BigLongArray
specifier|public
class|class
name|BigLongArray
block|{
DECL|field|DEFAULT_PAGE_SIZE
specifier|private
specifier|static
specifier|final
name|int
name|DEFAULT_PAGE_SIZE
init|=
literal|4096
decl_stmt|;
DECL|field|pages
specifier|private
specifier|final
name|long
index|[]
index|[]
name|pages
decl_stmt|;
DECL|field|size
specifier|public
specifier|final
name|int
name|size
decl_stmt|;
DECL|field|pageSize
specifier|private
specifier|final
name|int
name|pageSize
decl_stmt|;
DECL|field|pageCount
specifier|private
specifier|final
name|int
name|pageCount
decl_stmt|;
DECL|method|BigLongArray
specifier|public
name|BigLongArray
parameter_list|(
name|int
name|size
parameter_list|)
block|{
name|this
argument_list|(
name|size
argument_list|,
name|DEFAULT_PAGE_SIZE
argument_list|)
expr_stmt|;
block|}
DECL|method|BigLongArray
specifier|public
name|BigLongArray
parameter_list|(
name|int
name|size
parameter_list|,
name|int
name|pageSize
parameter_list|)
block|{
name|this
operator|.
name|size
operator|=
name|size
expr_stmt|;
name|this
operator|.
name|pageSize
operator|=
name|pageSize
expr_stmt|;
name|int
name|lastPageSize
init|=
name|size
operator|%
name|pageSize
decl_stmt|;
name|int
name|fullPageCount
init|=
name|size
operator|/
name|pageSize
decl_stmt|;
name|pageCount
operator|=
name|fullPageCount
operator|+
operator|(
name|lastPageSize
operator|==
literal|0
condition|?
literal|0
else|:
literal|1
operator|)
expr_stmt|;
name|pages
operator|=
operator|new
name|long
index|[
name|pageCount
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
name|fullPageCount
condition|;
operator|++
name|i
control|)
name|pages
index|[
name|i
index|]
operator|=
operator|new
name|long
index|[
name|pageSize
index|]
expr_stmt|;
if|if
condition|(
name|lastPageSize
operator|!=
literal|0
condition|)
name|pages
index|[
name|pages
operator|.
name|length
operator|-
literal|1
index|]
operator|=
operator|new
name|long
index|[
name|lastPageSize
index|]
expr_stmt|;
block|}
DECL|method|set
specifier|public
name|void
name|set
parameter_list|(
name|int
name|idx
parameter_list|,
name|long
name|value
parameter_list|)
block|{
if|if
condition|(
name|idx
argument_list|<
literal|0
operator|||
name|idx
argument_list|>
name|size
condition|)
throw|throw
operator|new
name|IndexOutOfBoundsException
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"%d is not whithin [0, %d)"
argument_list|,
name|idx
argument_list|,
name|size
argument_list|)
argument_list|)
throw|;
name|int
name|page
init|=
name|idx
operator|/
name|pageSize
decl_stmt|;
name|int
name|pageIdx
init|=
name|idx
operator|%
name|pageSize
decl_stmt|;
name|pages
index|[
name|page
index|]
index|[
name|pageIdx
index|]
operator|=
name|value
expr_stmt|;
block|}
DECL|method|get
specifier|public
name|long
name|get
parameter_list|(
name|int
name|idx
parameter_list|)
block|{
if|if
condition|(
name|idx
argument_list|<
literal|0
operator|||
name|idx
argument_list|>
name|size
condition|)
throw|throw
operator|new
name|IndexOutOfBoundsException
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"%d is not whithin [0, %d)"
argument_list|,
name|idx
argument_list|,
name|size
argument_list|)
argument_list|)
throw|;
name|int
name|page
init|=
name|idx
operator|/
name|pageSize
decl_stmt|;
name|int
name|pageIdx
init|=
name|idx
operator|%
name|pageSize
decl_stmt|;
return|return
name|pages
index|[
name|page
index|]
index|[
name|pageIdx
index|]
return|;
block|}
block|}
end_class

end_unit

