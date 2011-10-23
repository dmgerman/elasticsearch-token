begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.lucene.docset
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|lucene
operator|.
name|docset
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
name|index
operator|.
name|IndexReader
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
name|search
operator|.
name|DocIdSet
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
name|search
operator|.
name|DocIdSetIterator
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
name|FixedBitSet
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
name|OpenBitSet
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
comment|/**  */
end_comment

begin_class
DECL|class|DocSets
specifier|public
class|class
name|DocSets
block|{
DECL|method|createFixedBitSet
specifier|public
specifier|static
name|FixedBitSet
name|createFixedBitSet
parameter_list|(
name|DocIdSetIterator
name|disi
parameter_list|,
name|int
name|numBits
parameter_list|)
throws|throws
name|IOException
block|{
name|FixedBitSet
name|set
init|=
operator|new
name|FixedBitSet
argument_list|(
name|numBits
argument_list|)
decl_stmt|;
name|int
name|doc
decl_stmt|;
while|while
condition|(
operator|(
name|doc
operator|=
name|disi
operator|.
name|nextDoc
argument_list|()
operator|)
operator|!=
name|DocIdSetIterator
operator|.
name|NO_MORE_DOCS
condition|)
block|{
name|set
operator|.
name|set
argument_list|(
name|doc
argument_list|)
expr_stmt|;
block|}
return|return
name|set
return|;
block|}
DECL|method|or
specifier|public
specifier|static
name|void
name|or
parameter_list|(
name|FixedBitSet
name|into
parameter_list|,
name|DocIdSet
name|other
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|other
operator|==
literal|null
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|other
operator|instanceof
name|FixedBitSet
condition|)
block|{
name|into
operator|.
name|or
argument_list|(
operator|(
name|FixedBitSet
operator|)
name|other
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|other
operator|instanceof
name|FixedBitDocSet
condition|)
block|{
name|into
operator|.
name|or
argument_list|(
operator|(
operator|(
name|FixedBitDocSet
operator|)
name|other
operator|)
operator|.
name|set
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|DocIdSetIterator
name|disi
init|=
name|other
operator|.
name|iterator
argument_list|()
decl_stmt|;
if|if
condition|(
name|disi
operator|!=
literal|null
condition|)
block|{
name|into
operator|.
name|or
argument_list|(
name|disi
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|and
specifier|public
specifier|static
name|void
name|and
parameter_list|(
name|FixedBitSet
name|into
parameter_list|,
name|DocIdSet
name|other
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|other
operator|instanceof
name|FixedBitDocSet
condition|)
block|{
name|other
operator|=
operator|(
operator|(
name|FixedBitDocSet
operator|)
name|other
operator|)
operator|.
name|set
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|other
operator|instanceof
name|FixedBitSet
condition|)
block|{
comment|// copied from OpenBitSet#and
name|long
index|[]
name|intoBits
init|=
name|into
operator|.
name|getBits
argument_list|()
decl_stmt|;
name|long
index|[]
name|otherBits
init|=
operator|(
operator|(
name|FixedBitSet
operator|)
name|other
operator|)
operator|.
name|getBits
argument_list|()
decl_stmt|;
assert|assert
name|intoBits
operator|.
name|length
operator|==
name|otherBits
operator|.
name|length
assert|;
comment|// testing against zero can be more efficient
name|int
name|pos
init|=
name|intoBits
operator|.
name|length
decl_stmt|;
while|while
condition|(
operator|--
name|pos
operator|>=
literal|0
condition|)
block|{
name|intoBits
index|[
name|pos
index|]
operator|&=
name|otherBits
index|[
name|pos
index|]
expr_stmt|;
block|}
block|}
else|else
block|{
if|if
condition|(
name|other
operator|==
literal|null
condition|)
block|{
name|into
operator|.
name|clear
argument_list|(
literal|0
argument_list|,
name|into
operator|.
name|length
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// copied from OpenBitSetDISI#inPlaceAnd
name|DocIdSetIterator
name|disi
init|=
name|other
operator|.
name|iterator
argument_list|()
decl_stmt|;
if|if
condition|(
name|disi
operator|==
literal|null
condition|)
block|{
name|into
operator|.
name|clear
argument_list|(
literal|0
argument_list|,
name|into
operator|.
name|length
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|int
name|numBits
init|=
name|into
operator|.
name|length
argument_list|()
decl_stmt|;
name|int
name|disiDoc
decl_stmt|,
name|bitSetDoc
init|=
name|into
operator|.
name|nextSetBit
argument_list|(
literal|0
argument_list|)
decl_stmt|;
while|while
condition|(
name|bitSetDoc
operator|!=
operator|-
literal|1
operator|&&
operator|(
name|disiDoc
operator|=
name|disi
operator|.
name|advance
argument_list|(
name|bitSetDoc
argument_list|)
operator|)
operator|<
name|numBits
condition|)
block|{
name|into
operator|.
name|clear
argument_list|(
name|bitSetDoc
argument_list|,
name|disiDoc
argument_list|)
expr_stmt|;
name|disiDoc
operator|++
expr_stmt|;
name|bitSetDoc
operator|=
operator|(
name|disiDoc
operator|<
name|numBits
operator|)
condition|?
name|into
operator|.
name|nextSetBit
argument_list|(
name|disiDoc
argument_list|)
else|:
operator|-
literal|1
expr_stmt|;
block|}
if|if
condition|(
name|bitSetDoc
operator|!=
operator|-
literal|1
condition|)
block|{
name|into
operator|.
name|clear
argument_list|(
name|bitSetDoc
argument_list|,
name|numBits
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
DECL|method|andNot
specifier|public
specifier|static
name|void
name|andNot
parameter_list|(
name|FixedBitSet
name|into
parameter_list|,
name|DocIdSet
name|other
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|other
operator|==
literal|null
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|other
operator|instanceof
name|FixedBitDocSet
condition|)
block|{
name|other
operator|=
operator|(
operator|(
name|FixedBitDocSet
operator|)
name|other
operator|)
operator|.
name|set
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|other
operator|instanceof
name|FixedBitSet
condition|)
block|{
comment|// copied from OpenBitSet#andNot
name|long
index|[]
name|intoBits
init|=
name|into
operator|.
name|getBits
argument_list|()
decl_stmt|;
name|long
index|[]
name|otherBits
init|=
operator|(
operator|(
name|FixedBitSet
operator|)
name|other
operator|)
operator|.
name|getBits
argument_list|()
decl_stmt|;
assert|assert
name|intoBits
operator|.
name|length
operator|==
name|otherBits
operator|.
name|length
assert|;
name|int
name|idx
init|=
name|intoBits
operator|.
name|length
decl_stmt|;
while|while
condition|(
operator|--
name|idx
operator|>=
literal|0
condition|)
block|{
name|intoBits
index|[
name|idx
index|]
operator|&=
operator|~
name|otherBits
index|[
name|idx
index|]
expr_stmt|;
block|}
block|}
else|else
block|{
comment|// copied from OpenBitSetDISI#inPlaceNot
name|DocIdSetIterator
name|disi
init|=
name|other
operator|.
name|iterator
argument_list|()
decl_stmt|;
if|if
condition|(
name|disi
operator|!=
literal|null
condition|)
block|{
name|int
name|doc
decl_stmt|;
while|while
condition|(
operator|(
name|doc
operator|=
name|disi
operator|.
name|nextDoc
argument_list|()
operator|)
operator|!=
name|DocIdSetIterator
operator|.
name|NO_MORE_DOCS
condition|)
block|{
name|into
operator|.
name|clear
argument_list|(
name|doc
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
DECL|method|convert
specifier|public
specifier|static
name|DocSet
name|convert
parameter_list|(
name|IndexReader
name|reader
parameter_list|,
name|DocIdSet
name|docIdSet
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|docIdSet
operator|==
literal|null
condition|)
block|{
return|return
name|DocSet
operator|.
name|EMPTY_DOC_SET
return|;
block|}
elseif|else
if|if
condition|(
name|docIdSet
operator|instanceof
name|DocSet
condition|)
block|{
return|return
operator|(
name|DocSet
operator|)
name|docIdSet
return|;
block|}
elseif|else
if|if
condition|(
name|docIdSet
operator|instanceof
name|FixedBitSet
condition|)
block|{
return|return
operator|new
name|FixedBitDocSet
argument_list|(
operator|(
name|FixedBitSet
operator|)
name|docIdSet
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|docIdSet
operator|instanceof
name|OpenBitSet
condition|)
block|{
return|return
operator|new
name|OpenBitDocSet
argument_list|(
operator|(
name|OpenBitSet
operator|)
name|docIdSet
argument_list|)
return|;
block|}
else|else
block|{
specifier|final
name|DocIdSetIterator
name|it
init|=
name|docIdSet
operator|.
name|iterator
argument_list|()
decl_stmt|;
comment|// null is allowed to be returned by iterator(),
comment|// in this case we wrap with the empty set,
comment|// which is cacheable.
return|return
operator|(
name|it
operator|==
literal|null
operator|)
condition|?
name|DocSet
operator|.
name|EMPTY_DOC_SET
else|:
operator|new
name|FixedBitDocSet
argument_list|(
name|createFixedBitSet
argument_list|(
name|it
argument_list|,
name|reader
operator|.
name|maxDoc
argument_list|()
argument_list|)
argument_list|)
return|;
block|}
block|}
comment|/**      * Returns a cacheable version of the doc id set (might be the same instance provided as a parameter).      */
DECL|method|cacheable
specifier|public
specifier|static
name|DocSet
name|cacheable
parameter_list|(
name|IndexReader
name|reader
parameter_list|,
name|DocIdSet
name|docIdSet
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|docIdSet
operator|==
literal|null
condition|)
block|{
return|return
name|DocSet
operator|.
name|EMPTY_DOC_SET
return|;
block|}
elseif|else
if|if
condition|(
name|docIdSet
operator|.
name|isCacheable
argument_list|()
operator|&&
operator|(
name|docIdSet
operator|instanceof
name|DocSet
operator|)
condition|)
block|{
return|return
operator|(
name|DocSet
operator|)
name|docIdSet
return|;
block|}
elseif|else
if|if
condition|(
name|docIdSet
operator|instanceof
name|FixedBitSet
condition|)
block|{
return|return
operator|new
name|FixedBitDocSet
argument_list|(
operator|(
name|FixedBitSet
operator|)
name|docIdSet
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|docIdSet
operator|instanceof
name|OpenBitSet
condition|)
block|{
return|return
operator|new
name|OpenBitDocSet
argument_list|(
operator|(
name|OpenBitSet
operator|)
name|docIdSet
argument_list|)
return|;
block|}
else|else
block|{
specifier|final
name|DocIdSetIterator
name|it
init|=
name|docIdSet
operator|.
name|iterator
argument_list|()
decl_stmt|;
comment|// null is allowed to be returned by iterator(),
comment|// in this case we wrap with the empty set,
comment|// which is cacheable.
return|return
operator|(
name|it
operator|==
literal|null
operator|)
condition|?
name|DocSet
operator|.
name|EMPTY_DOC_SET
else|:
operator|new
name|FixedBitDocSet
argument_list|(
name|createFixedBitSet
argument_list|(
name|it
argument_list|,
name|reader
operator|.
name|maxDoc
argument_list|()
argument_list|)
argument_list|)
return|;
block|}
block|}
DECL|method|DocSets
specifier|private
name|DocSets
parameter_list|()
block|{      }
block|}
end_class

end_unit

