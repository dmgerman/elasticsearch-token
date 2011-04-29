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
name|List
import|;
end_import

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|AndDocIdSet
specifier|public
class|class
name|AndDocIdSet
extends|extends
name|DocIdSet
block|{
DECL|field|sets
specifier|private
specifier|final
name|List
argument_list|<
name|DocIdSet
argument_list|>
name|sets
decl_stmt|;
DECL|method|AndDocIdSet
specifier|public
name|AndDocIdSet
parameter_list|(
name|List
argument_list|<
name|DocIdSet
argument_list|>
name|sets
parameter_list|)
block|{
name|this
operator|.
name|sets
operator|=
name|sets
expr_stmt|;
block|}
DECL|method|isCacheable
annotation|@
name|Override
specifier|public
name|boolean
name|isCacheable
parameter_list|()
block|{
comment|// not cacheable, the reason is that by default, when constructing the filter, it is not cacheable,
comment|// so if someone wants it to be cacheable, we might as well construct a cached version of the result
return|return
literal|false
return|;
comment|//        for (DocIdSet set : sets) {
comment|//            if (!set.isCacheable()) {
comment|//                return false;
comment|//            }
comment|//        }
comment|//        return true;
block|}
DECL|method|iterator
annotation|@
name|Override
specifier|public
name|DocIdSetIterator
name|iterator
parameter_list|()
throws|throws
name|IOException
block|{
return|return
operator|new
name|AndDocIdSetIterator
argument_list|()
return|;
block|}
DECL|class|AndDocIdSetIterator
class|class
name|AndDocIdSetIterator
extends|extends
name|DocIdSetIterator
block|{
DECL|field|lastReturn
name|int
name|lastReturn
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|iterators
specifier|private
name|DocIdSetIterator
index|[]
name|iterators
init|=
literal|null
decl_stmt|;
DECL|method|AndDocIdSetIterator
name|AndDocIdSetIterator
parameter_list|()
throws|throws
name|IOException
block|{
name|iterators
operator|=
operator|new
name|DocIdSetIterator
index|[
name|sets
operator|.
name|size
argument_list|()
index|]
expr_stmt|;
name|int
name|j
init|=
literal|0
decl_stmt|;
for|for
control|(
name|DocIdSet
name|set
range|:
name|sets
control|)
block|{
if|if
condition|(
name|set
operator|==
literal|null
condition|)
block|{
name|lastReturn
operator|=
name|DocIdSetIterator
operator|.
name|NO_MORE_DOCS
expr_stmt|;
comment|// non matching
break|break;
block|}
else|else
block|{
name|DocIdSetIterator
name|dcit
init|=
name|set
operator|.
name|iterator
argument_list|()
decl_stmt|;
if|if
condition|(
name|dcit
operator|==
literal|null
condition|)
block|{
name|lastReturn
operator|=
name|DocIdSetIterator
operator|.
name|NO_MORE_DOCS
expr_stmt|;
comment|// non matching
break|break;
block|}
name|iterators
index|[
name|j
operator|++
index|]
operator|=
name|dcit
expr_stmt|;
block|}
block|}
if|if
condition|(
name|lastReturn
operator|!=
name|DocIdSetIterator
operator|.
name|NO_MORE_DOCS
condition|)
block|{
name|lastReturn
operator|=
operator|(
name|iterators
operator|.
name|length
operator|>
literal|0
condition|?
operator|-
literal|1
else|:
name|DocIdSetIterator
operator|.
name|NO_MORE_DOCS
operator|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|docID
specifier|public
specifier|final
name|int
name|docID
parameter_list|()
block|{
return|return
name|lastReturn
return|;
block|}
annotation|@
name|Override
DECL|method|nextDoc
specifier|public
specifier|final
name|int
name|nextDoc
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|lastReturn
operator|==
name|DocIdSetIterator
operator|.
name|NO_MORE_DOCS
condition|)
return|return
name|DocIdSetIterator
operator|.
name|NO_MORE_DOCS
return|;
name|DocIdSetIterator
name|dcit
init|=
name|iterators
index|[
literal|0
index|]
decl_stmt|;
name|int
name|target
init|=
name|dcit
operator|.
name|nextDoc
argument_list|()
decl_stmt|;
name|int
name|size
init|=
name|iterators
operator|.
name|length
decl_stmt|;
name|int
name|skip
init|=
literal|0
decl_stmt|;
name|int
name|i
init|=
literal|1
decl_stmt|;
while|while
condition|(
name|i
operator|<
name|size
condition|)
block|{
if|if
condition|(
name|i
operator|!=
name|skip
condition|)
block|{
name|dcit
operator|=
name|iterators
index|[
name|i
index|]
expr_stmt|;
name|int
name|docid
init|=
name|dcit
operator|.
name|advance
argument_list|(
name|target
argument_list|)
decl_stmt|;
if|if
condition|(
name|docid
operator|>
name|target
condition|)
block|{
name|target
operator|=
name|docid
expr_stmt|;
if|if
condition|(
name|i
operator|!=
literal|0
condition|)
block|{
name|skip
operator|=
name|i
expr_stmt|;
name|i
operator|=
literal|0
expr_stmt|;
continue|continue;
block|}
else|else
name|skip
operator|=
literal|0
expr_stmt|;
block|}
block|}
name|i
operator|++
expr_stmt|;
block|}
return|return
operator|(
name|lastReturn
operator|=
name|target
operator|)
return|;
block|}
annotation|@
name|Override
DECL|method|advance
specifier|public
specifier|final
name|int
name|advance
parameter_list|(
name|int
name|target
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|lastReturn
operator|==
name|DocIdSetIterator
operator|.
name|NO_MORE_DOCS
condition|)
return|return
name|DocIdSetIterator
operator|.
name|NO_MORE_DOCS
return|;
name|DocIdSetIterator
name|dcit
init|=
name|iterators
index|[
literal|0
index|]
decl_stmt|;
name|target
operator|=
name|dcit
operator|.
name|advance
argument_list|(
name|target
argument_list|)
expr_stmt|;
name|int
name|size
init|=
name|iterators
operator|.
name|length
decl_stmt|;
name|int
name|skip
init|=
literal|0
decl_stmt|;
name|int
name|i
init|=
literal|1
decl_stmt|;
while|while
condition|(
name|i
operator|<
name|size
condition|)
block|{
if|if
condition|(
name|i
operator|!=
name|skip
condition|)
block|{
name|dcit
operator|=
name|iterators
index|[
name|i
index|]
expr_stmt|;
name|int
name|docid
init|=
name|dcit
operator|.
name|advance
argument_list|(
name|target
argument_list|)
decl_stmt|;
if|if
condition|(
name|docid
operator|>
name|target
condition|)
block|{
name|target
operator|=
name|docid
expr_stmt|;
if|if
condition|(
name|i
operator|!=
literal|0
condition|)
block|{
name|skip
operator|=
name|i
expr_stmt|;
name|i
operator|=
literal|0
expr_stmt|;
continue|continue;
block|}
else|else
block|{
name|skip
operator|=
literal|0
expr_stmt|;
block|}
block|}
block|}
name|i
operator|++
expr_stmt|;
block|}
return|return
operator|(
name|lastReturn
operator|=
name|target
operator|)
return|;
block|}
block|}
block|}
end_class

end_unit

