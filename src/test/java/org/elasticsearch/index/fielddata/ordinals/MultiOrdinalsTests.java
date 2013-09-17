begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  *  Unless required by applicable law or agreed to in writing,  *  software distributed under the License is distributed on an  *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  *  KIND, either express or implied.  See the License for the  *  specific language governing permissions and limitations  *  under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.fielddata.ordinals
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|fielddata
operator|.
name|ordinals
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
name|LongsRef
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
name|packed
operator|.
name|PackedInts
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
name|settings
operator|.
name|ImmutableSettings
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
name|ElasticSearchTestCase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
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
name|*
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

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|MultiOrdinalsTests
specifier|public
class|class
name|MultiOrdinalsTests
extends|extends
name|ElasticSearchTestCase
block|{
DECL|method|creationMultiOrdinals
specifier|protected
specifier|final
name|Ordinals
name|creationMultiOrdinals
parameter_list|(
name|OrdinalsBuilder
name|builder
parameter_list|)
block|{
return|return
name|this
operator|.
name|creationMultiOrdinals
argument_list|(
name|builder
argument_list|,
name|ImmutableSettings
operator|.
name|builder
argument_list|()
argument_list|)
return|;
block|}
DECL|method|creationMultiOrdinals
specifier|protected
name|Ordinals
name|creationMultiOrdinals
parameter_list|(
name|OrdinalsBuilder
name|builder
parameter_list|,
name|ImmutableSettings
operator|.
name|Builder
name|settings
parameter_list|)
block|{
return|return
name|builder
operator|.
name|build
argument_list|(
name|settings
operator|.
name|build
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Test
DECL|method|testRandomValues
specifier|public
name|void
name|testRandomValues
parameter_list|()
throws|throws
name|IOException
block|{
name|Random
name|random
init|=
name|getRandom
argument_list|()
decl_stmt|;
name|int
name|numDocs
init|=
literal|100
operator|+
name|random
operator|.
name|nextInt
argument_list|(
literal|1000
argument_list|)
decl_stmt|;
name|int
name|numOrdinals
init|=
literal|1
operator|+
name|random
operator|.
name|nextInt
argument_list|(
literal|200
argument_list|)
decl_stmt|;
name|int
name|numValues
init|=
literal|100
operator|+
name|random
operator|.
name|nextInt
argument_list|(
literal|100000
argument_list|)
decl_stmt|;
name|OrdinalsBuilder
name|builder
init|=
operator|new
name|OrdinalsBuilder
argument_list|(
name|numDocs
argument_list|)
decl_stmt|;
name|Set
argument_list|<
name|OrdAndId
argument_list|>
name|ordsAndIdSet
init|=
operator|new
name|HashSet
argument_list|<
name|OrdAndId
argument_list|>
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
name|numValues
condition|;
name|i
operator|++
control|)
block|{
name|ordsAndIdSet
operator|.
name|add
argument_list|(
operator|new
name|OrdAndId
argument_list|(
literal|1
operator|+
name|random
operator|.
name|nextInt
argument_list|(
name|numOrdinals
argument_list|)
argument_list|,
name|random
operator|.
name|nextInt
argument_list|(
name|numDocs
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|OrdAndId
argument_list|>
name|ordsAndIds
init|=
operator|new
name|ArrayList
argument_list|<
name|OrdAndId
argument_list|>
argument_list|(
name|ordsAndIdSet
argument_list|)
decl_stmt|;
name|Collections
operator|.
name|sort
argument_list|(
name|ordsAndIds
argument_list|,
operator|new
name|Comparator
argument_list|<
name|OrdAndId
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|int
name|compare
parameter_list|(
name|OrdAndId
name|o1
parameter_list|,
name|OrdAndId
name|o2
parameter_list|)
block|{
if|if
condition|(
name|o1
operator|.
name|ord
operator|<
name|o2
operator|.
name|ord
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
if|if
condition|(
name|o1
operator|.
name|ord
operator|==
name|o2
operator|.
name|ord
condition|)
block|{
if|if
condition|(
name|o1
operator|.
name|id
operator|<
name|o2
operator|.
name|id
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
if|if
condition|(
name|o1
operator|.
name|id
operator|>
name|o2
operator|.
name|id
condition|)
block|{
return|return
literal|1
return|;
block|}
return|return
literal|0
return|;
block|}
return|return
literal|1
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|long
name|lastOrd
init|=
operator|-
literal|1
decl_stmt|;
for|for
control|(
name|OrdAndId
name|ordAndId
range|:
name|ordsAndIds
control|)
block|{
if|if
condition|(
name|lastOrd
operator|!=
name|ordAndId
operator|.
name|ord
condition|)
block|{
name|lastOrd
operator|=
name|ordAndId
operator|.
name|ord
expr_stmt|;
name|builder
operator|.
name|nextOrdinal
argument_list|()
expr_stmt|;
block|}
name|ordAndId
operator|.
name|ord
operator|=
name|builder
operator|.
name|currentOrdinal
argument_list|()
expr_stmt|;
comment|// remap the ordinals in case we have gaps?
name|builder
operator|.
name|addDoc
argument_list|(
name|ordAndId
operator|.
name|id
argument_list|)
expr_stmt|;
block|}
name|Collections
operator|.
name|sort
argument_list|(
name|ordsAndIds
argument_list|,
operator|new
name|Comparator
argument_list|<
name|OrdAndId
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|int
name|compare
parameter_list|(
name|OrdAndId
name|o1
parameter_list|,
name|OrdAndId
name|o2
parameter_list|)
block|{
if|if
condition|(
name|o1
operator|.
name|id
operator|<
name|o2
operator|.
name|id
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
if|if
condition|(
name|o1
operator|.
name|id
operator|==
name|o2
operator|.
name|id
condition|)
block|{
if|if
condition|(
name|o1
operator|.
name|ord
operator|<
name|o2
operator|.
name|ord
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
if|if
condition|(
name|o1
operator|.
name|ord
operator|>
name|o2
operator|.
name|ord
condition|)
block|{
return|return
literal|1
return|;
block|}
return|return
literal|0
return|;
block|}
return|return
literal|1
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|Ordinals
name|ords
init|=
name|creationMultiOrdinals
argument_list|(
name|builder
argument_list|)
decl_stmt|;
name|Ordinals
operator|.
name|Docs
name|docs
init|=
name|ords
operator|.
name|ordinals
argument_list|()
decl_stmt|;
name|int
name|docId
init|=
name|ordsAndIds
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|id
decl_stmt|;
name|List
argument_list|<
name|Long
argument_list|>
name|docOrds
init|=
operator|new
name|ArrayList
argument_list|<
name|Long
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|OrdAndId
name|ordAndId
range|:
name|ordsAndIds
control|)
block|{
if|if
condition|(
name|docId
operator|==
name|ordAndId
operator|.
name|id
condition|)
block|{
name|docOrds
operator|.
name|add
argument_list|(
name|ordAndId
operator|.
name|ord
argument_list|)
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
operator|!
name|docOrds
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|assertThat
argument_list|(
name|docs
operator|.
name|getOrd
argument_list|(
name|docId
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|docOrds
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|LongsRef
name|ref
init|=
name|docs
operator|.
name|getOrds
argument_list|(
name|docId
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|ref
operator|.
name|offset
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
name|ref
operator|.
name|offset
init|;
name|i
operator|<
name|ref
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|assertThat
argument_list|(
literal|"index: "
operator|+
name|i
operator|+
literal|" offset: "
operator|+
name|ref
operator|.
name|offset
operator|+
literal|" len: "
operator|+
name|ref
operator|.
name|length
argument_list|,
name|ref
operator|.
name|longs
index|[
name|i
index|]
argument_list|,
name|equalTo
argument_list|(
name|docOrds
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|final
name|long
index|[]
name|array
init|=
operator|new
name|long
index|[
name|docOrds
operator|.
name|size
argument_list|()
index|]
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
name|array
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|array
index|[
name|i
index|]
operator|=
name|docOrds
operator|.
name|get
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
name|assertIter
argument_list|(
name|docs
operator|.
name|getIter
argument_list|(
name|docId
argument_list|)
argument_list|,
name|array
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
name|docId
operator|+
literal|1
init|;
name|i
operator|<
name|ordAndId
operator|.
name|id
condition|;
name|i
operator|++
control|)
block|{
name|assertThat
argument_list|(
name|docs
operator|.
name|getOrd
argument_list|(
name|i
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|docId
operator|=
name|ordAndId
operator|.
name|id
expr_stmt|;
name|docOrds
operator|.
name|clear
argument_list|()
expr_stmt|;
name|docOrds
operator|.
name|add
argument_list|(
name|ordAndId
operator|.
name|ord
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|class|OrdAndId
specifier|public
specifier|static
class|class
name|OrdAndId
block|{
DECL|field|ord
name|long
name|ord
decl_stmt|;
DECL|field|id
specifier|final
name|int
name|id
decl_stmt|;
DECL|method|OrdAndId
specifier|public
name|OrdAndId
parameter_list|(
name|long
name|ord
parameter_list|,
name|int
name|id
parameter_list|)
block|{
name|this
operator|.
name|ord
operator|=
name|ord
expr_stmt|;
name|this
operator|.
name|id
operator|=
name|id
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|hashCode
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
specifier|final
name|int
name|prime
init|=
literal|31
decl_stmt|;
name|int
name|result
init|=
literal|1
decl_stmt|;
name|result
operator|=
name|prime
operator|*
name|result
operator|+
name|id
expr_stmt|;
name|result
operator|=
name|prime
operator|*
name|result
operator|+
operator|(
name|int
operator|)
name|ord
expr_stmt|;
return|return
name|result
return|;
block|}
annotation|@
name|Override
DECL|method|equals
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|obj
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
name|obj
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|getClass
argument_list|()
operator|!=
name|obj
operator|.
name|getClass
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
name|OrdAndId
name|other
init|=
operator|(
name|OrdAndId
operator|)
name|obj
decl_stmt|;
if|if
condition|(
name|id
operator|!=
name|other
operator|.
name|id
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|ord
operator|!=
name|other
operator|.
name|ord
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
block|}
annotation|@
name|Test
DECL|method|testOrdinals
specifier|public
name|void
name|testOrdinals
parameter_list|()
throws|throws
name|Exception
block|{
name|int
name|maxDoc
init|=
literal|7
decl_stmt|;
name|long
name|maxOrds
init|=
literal|32
decl_stmt|;
name|OrdinalsBuilder
name|builder
init|=
operator|new
name|OrdinalsBuilder
argument_list|(
name|maxDoc
argument_list|)
decl_stmt|;
name|builder
operator|.
name|nextOrdinal
argument_list|()
expr_stmt|;
comment|// 1
name|builder
operator|.
name|addDoc
argument_list|(
literal|1
argument_list|)
operator|.
name|addDoc
argument_list|(
literal|4
argument_list|)
operator|.
name|addDoc
argument_list|(
literal|5
argument_list|)
operator|.
name|addDoc
argument_list|(
literal|6
argument_list|)
expr_stmt|;
name|builder
operator|.
name|nextOrdinal
argument_list|()
expr_stmt|;
comment|// 2
name|builder
operator|.
name|addDoc
argument_list|(
literal|0
argument_list|)
operator|.
name|addDoc
argument_list|(
literal|5
argument_list|)
operator|.
name|addDoc
argument_list|(
literal|6
argument_list|)
expr_stmt|;
name|builder
operator|.
name|nextOrdinal
argument_list|()
expr_stmt|;
comment|// 3
name|builder
operator|.
name|addDoc
argument_list|(
literal|2
argument_list|)
operator|.
name|addDoc
argument_list|(
literal|4
argument_list|)
operator|.
name|addDoc
argument_list|(
literal|5
argument_list|)
operator|.
name|addDoc
argument_list|(
literal|6
argument_list|)
expr_stmt|;
name|builder
operator|.
name|nextOrdinal
argument_list|()
expr_stmt|;
comment|// 4
name|builder
operator|.
name|addDoc
argument_list|(
literal|0
argument_list|)
operator|.
name|addDoc
argument_list|(
literal|4
argument_list|)
operator|.
name|addDoc
argument_list|(
literal|5
argument_list|)
operator|.
name|addDoc
argument_list|(
literal|6
argument_list|)
expr_stmt|;
name|builder
operator|.
name|nextOrdinal
argument_list|()
expr_stmt|;
comment|// 5
name|builder
operator|.
name|addDoc
argument_list|(
literal|4
argument_list|)
operator|.
name|addDoc
argument_list|(
literal|5
argument_list|)
operator|.
name|addDoc
argument_list|(
literal|6
argument_list|)
expr_stmt|;
name|long
name|ord
init|=
name|builder
operator|.
name|nextOrdinal
argument_list|()
decl_stmt|;
comment|// 6
name|builder
operator|.
name|addDoc
argument_list|(
literal|4
argument_list|)
operator|.
name|addDoc
argument_list|(
literal|5
argument_list|)
operator|.
name|addDoc
argument_list|(
literal|6
argument_list|)
expr_stmt|;
for|for
control|(
name|long
name|i
init|=
name|ord
init|;
name|i
operator|<
name|maxOrds
condition|;
name|i
operator|++
control|)
block|{
name|builder
operator|.
name|nextOrdinal
argument_list|()
expr_stmt|;
name|builder
operator|.
name|addDoc
argument_list|(
literal|5
argument_list|)
operator|.
name|addDoc
argument_list|(
literal|6
argument_list|)
expr_stmt|;
block|}
name|long
index|[]
index|[]
name|ordinalPlan
init|=
operator|new
name|long
index|[]
index|[]
block|{
block|{
literal|2
block|,
literal|4
block|}
block|,
block|{
literal|1
block|}
block|,
block|{
literal|3
block|}
block|,
block|{}
block|,
block|{
literal|1
block|,
literal|3
block|,
literal|4
block|,
literal|5
block|,
literal|6
block|}
block|,
block|{
literal|1
block|,
literal|2
block|,
literal|3
block|,
literal|4
block|,
literal|5
block|,
literal|6
block|,
literal|7
block|,
literal|8
block|,
literal|9
block|,
literal|10
block|,
literal|11
block|,
literal|12
block|,
literal|13
block|,
literal|14
block|,
literal|15
block|,
literal|16
block|,
literal|17
block|,
literal|18
block|,
literal|19
block|,
literal|20
block|,
literal|21
block|,
literal|22
block|,
literal|23
block|,
literal|24
block|,
literal|25
block|,
literal|26
block|,
literal|27
block|,
literal|28
block|,
literal|29
block|,
literal|30
block|,
literal|31
block|,
literal|32
block|}
block|,
block|{
literal|1
block|,
literal|2
block|,
literal|3
block|,
literal|4
block|,
literal|5
block|,
literal|6
block|,
literal|7
block|,
literal|8
block|,
literal|9
block|,
literal|10
block|,
literal|11
block|,
literal|12
block|,
literal|13
block|,
literal|14
block|,
literal|15
block|,
literal|16
block|,
literal|17
block|,
literal|18
block|,
literal|19
block|,
literal|20
block|,
literal|21
block|,
literal|22
block|,
literal|23
block|,
literal|24
block|,
literal|25
block|,
literal|26
block|,
literal|27
block|,
literal|28
block|,
literal|29
block|,
literal|30
block|,
literal|31
block|,
literal|32
block|}
block|}
decl_stmt|;
name|Ordinals
name|ordinals
init|=
name|creationMultiOrdinals
argument_list|(
name|builder
argument_list|)
decl_stmt|;
name|Ordinals
operator|.
name|Docs
name|docs
init|=
name|ordinals
operator|.
name|ordinals
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|docs
argument_list|,
name|ordinalPlan
argument_list|)
expr_stmt|;
block|}
DECL|method|assertIter
specifier|protected
specifier|static
name|void
name|assertIter
parameter_list|(
name|Ordinals
operator|.
name|Docs
operator|.
name|Iter
name|iter
parameter_list|,
name|long
modifier|...
name|expectedOrdinals
parameter_list|)
block|{
for|for
control|(
name|long
name|expectedOrdinal
range|:
name|expectedOrdinals
control|)
block|{
name|assertThat
argument_list|(
name|iter
operator|.
name|next
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|expectedOrdinal
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|assertThat
argument_list|(
name|iter
operator|.
name|next
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
comment|// Last one should always be 0
name|assertThat
argument_list|(
name|iter
operator|.
name|next
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
comment|// Just checking it stays 0
block|}
annotation|@
name|Test
DECL|method|testMultiValuesDocsWithOverlappingStorageArrays
specifier|public
name|void
name|testMultiValuesDocsWithOverlappingStorageArrays
parameter_list|()
throws|throws
name|Exception
block|{
name|int
name|maxDoc
init|=
literal|7
decl_stmt|;
name|long
name|maxOrds
init|=
literal|15
decl_stmt|;
name|OrdinalsBuilder
name|builder
init|=
operator|new
name|OrdinalsBuilder
argument_list|(
name|maxDoc
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
name|maxOrds
condition|;
name|i
operator|++
control|)
block|{
name|builder
operator|.
name|nextOrdinal
argument_list|()
expr_stmt|;
if|if
condition|(
name|i
operator|<
literal|10
condition|)
block|{
name|builder
operator|.
name|addDoc
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|addDoc
argument_list|(
literal|1
argument_list|)
expr_stmt|;
if|if
condition|(
name|i
operator|==
literal|0
condition|)
block|{
name|builder
operator|.
name|addDoc
argument_list|(
literal|2
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|i
operator|<
literal|5
condition|)
block|{
name|builder
operator|.
name|addDoc
argument_list|(
literal|3
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|i
operator|<
literal|6
condition|)
block|{
name|builder
operator|.
name|addDoc
argument_list|(
literal|4
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|i
operator|==
literal|1
condition|)
block|{
name|builder
operator|.
name|addDoc
argument_list|(
literal|5
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|i
operator|<
literal|10
condition|)
block|{
name|builder
operator|.
name|addDoc
argument_list|(
literal|6
argument_list|)
expr_stmt|;
block|}
block|}
name|long
index|[]
index|[]
name|ordinalPlan
init|=
operator|new
name|long
index|[]
index|[]
block|{
block|{
literal|1
block|,
literal|2
block|,
literal|3
block|,
literal|4
block|,
literal|5
block|,
literal|6
block|,
literal|7
block|,
literal|8
block|,
literal|9
block|,
literal|10
block|}
block|,
block|{
literal|1
block|,
literal|2
block|,
literal|3
block|,
literal|4
block|,
literal|5
block|,
literal|6
block|,
literal|7
block|,
literal|8
block|,
literal|9
block|,
literal|10
block|,
literal|11
block|,
literal|12
block|,
literal|13
block|,
literal|14
block|,
literal|15
block|}
block|,
block|{
literal|1
block|}
block|,
block|{
literal|1
block|,
literal|2
block|,
literal|3
block|,
literal|4
block|,
literal|5
block|}
block|,
block|{
literal|1
block|,
literal|2
block|,
literal|3
block|,
literal|4
block|,
literal|5
block|,
literal|6
block|}
block|,
block|{
literal|2
block|}
block|,
block|{
literal|1
block|,
literal|2
block|,
literal|3
block|,
literal|4
block|,
literal|5
block|,
literal|6
block|,
literal|7
block|,
literal|8
block|,
literal|9
block|,
literal|10
block|}
block|}
decl_stmt|;
name|Ordinals
name|ordinals
init|=
operator|new
name|MultiOrdinals
argument_list|(
name|builder
argument_list|,
name|PackedInts
operator|.
name|FASTEST
argument_list|)
decl_stmt|;
name|Ordinals
operator|.
name|Docs
name|docs
init|=
name|ordinals
operator|.
name|ordinals
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|docs
argument_list|,
name|ordinalPlan
argument_list|)
expr_stmt|;
block|}
DECL|method|assertEquals
specifier|private
name|void
name|assertEquals
parameter_list|(
name|Ordinals
operator|.
name|Docs
name|docs
parameter_list|,
name|long
index|[]
index|[]
name|ordinalPlan
parameter_list|)
block|{
name|long
name|numOrds
init|=
literal|0
decl_stmt|;
for|for
control|(
name|int
name|doc
init|=
literal|0
init|;
name|doc
operator|<
name|ordinalPlan
operator|.
name|length
condition|;
operator|++
name|doc
control|)
block|{
if|if
condition|(
name|ordinalPlan
index|[
name|doc
index|]
operator|.
name|length
operator|>
literal|0
condition|)
block|{
name|numOrds
operator|=
name|Math
operator|.
name|max
argument_list|(
name|numOrds
argument_list|,
name|ordinalPlan
index|[
name|doc
index|]
index|[
name|ordinalPlan
index|[
name|doc
index|]
operator|.
name|length
operator|-
literal|1
index|]
argument_list|)
expr_stmt|;
block|}
block|}
name|assertThat
argument_list|(
name|docs
operator|.
name|getNumDocs
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|ordinalPlan
operator|.
name|length
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|docs
operator|.
name|getNumOrds
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|numOrds
argument_list|)
argument_list|)
expr_stmt|;
comment|// Includes null ord
name|assertThat
argument_list|(
name|docs
operator|.
name|getMaxOrd
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|numOrds
operator|+
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|docs
operator|.
name|isMultiValued
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|doc
init|=
literal|0
init|;
name|doc
operator|<
name|ordinalPlan
operator|.
name|length
condition|;
operator|++
name|doc
control|)
block|{
name|LongsRef
name|ref
init|=
name|docs
operator|.
name|getOrds
argument_list|(
name|doc
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|ref
operator|.
name|offset
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|long
index|[]
name|ords
init|=
name|ordinalPlan
index|[
name|doc
index|]
decl_stmt|;
name|assertThat
argument_list|(
name|ref
argument_list|,
name|equalTo
argument_list|(
operator|new
name|LongsRef
argument_list|(
name|ords
argument_list|,
literal|0
argument_list|,
name|ords
operator|.
name|length
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertIter
argument_list|(
name|docs
operator|.
name|getIter
argument_list|(
name|doc
argument_list|)
argument_list|,
name|ords
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

