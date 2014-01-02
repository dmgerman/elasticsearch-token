begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.lookup
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|lookup
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
name|BytesRef
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
name|IntsRef
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
name|Iterator
import|;
end_import

begin_comment
comment|/*  * Can iterate over the positions of a term an arbotrary number of times.   * */
end_comment

begin_class
DECL|class|CachedPositionIterator
specifier|public
class|class
name|CachedPositionIterator
extends|extends
name|PositionIterator
block|{
DECL|method|CachedPositionIterator
specifier|public
name|CachedPositionIterator
parameter_list|(
name|ScriptTerm
name|termInfo
parameter_list|)
block|{
name|super
argument_list|(
name|termInfo
argument_list|)
expr_stmt|;
block|}
comment|// all payloads of the term in the current document in one bytes array.
comment|// payloadStarts and payloadLength mark the start and end of one payload.
DECL|field|payloads
specifier|final
name|BytesRef
name|payloads
init|=
operator|new
name|BytesRef
argument_list|()
decl_stmt|;
DECL|field|payloadsLengths
specifier|final
name|IntsRef
name|payloadsLengths
init|=
operator|new
name|IntsRef
argument_list|(
literal|0
argument_list|)
decl_stmt|;
DECL|field|payloadsStarts
specifier|final
name|IntsRef
name|payloadsStarts
init|=
operator|new
name|IntsRef
argument_list|(
literal|0
argument_list|)
decl_stmt|;
DECL|field|positions
specifier|final
name|IntsRef
name|positions
init|=
operator|new
name|IntsRef
argument_list|(
literal|0
argument_list|)
decl_stmt|;
DECL|field|startOffsets
specifier|final
name|IntsRef
name|startOffsets
init|=
operator|new
name|IntsRef
argument_list|(
literal|0
argument_list|)
decl_stmt|;
DECL|field|endOffsets
specifier|final
name|IntsRef
name|endOffsets
init|=
operator|new
name|IntsRef
argument_list|(
literal|0
argument_list|)
decl_stmt|;
annotation|@
name|Override
DECL|method|reset
specifier|public
name|Iterator
argument_list|<
name|TermPosition
argument_list|>
name|reset
parameter_list|()
block|{
return|return
operator|new
name|Iterator
argument_list|<
name|TermPosition
argument_list|>
argument_list|()
block|{
specifier|private
name|int
name|pos
init|=
literal|0
decl_stmt|;
specifier|private
specifier|final
name|TermPosition
name|termPosition
init|=
operator|new
name|TermPosition
argument_list|()
decl_stmt|;
annotation|@
name|Override
specifier|public
name|boolean
name|hasNext
parameter_list|()
block|{
return|return
name|pos
operator|<
name|freq
return|;
block|}
annotation|@
name|Override
specifier|public
name|TermPosition
name|next
parameter_list|()
block|{
name|termPosition
operator|.
name|position
operator|=
name|positions
operator|.
name|ints
index|[
name|pos
index|]
expr_stmt|;
name|termPosition
operator|.
name|startOffset
operator|=
name|startOffsets
operator|.
name|ints
index|[
name|pos
index|]
expr_stmt|;
name|termPosition
operator|.
name|endOffset
operator|=
name|endOffsets
operator|.
name|ints
index|[
name|pos
index|]
expr_stmt|;
name|termPosition
operator|.
name|payload
operator|=
name|payloads
expr_stmt|;
name|payloads
operator|.
name|offset
operator|=
name|payloadsStarts
operator|.
name|ints
index|[
name|pos
index|]
expr_stmt|;
name|payloads
operator|.
name|length
operator|=
name|payloadsLengths
operator|.
name|ints
index|[
name|pos
index|]
expr_stmt|;
name|pos
operator|++
expr_stmt|;
return|return
name|termPosition
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|remove
parameter_list|()
block|{             }
block|}
return|;
block|}
DECL|method|record
specifier|private
name|void
name|record
parameter_list|()
throws|throws
name|IOException
block|{
name|TermPosition
name|termPosition
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
name|freq
condition|;
name|i
operator|++
control|)
block|{
name|termPosition
operator|=
name|super
operator|.
name|next
argument_list|()
expr_stmt|;
name|positions
operator|.
name|ints
index|[
name|i
index|]
operator|=
name|termPosition
operator|.
name|position
expr_stmt|;
name|addPayload
argument_list|(
name|i
argument_list|,
name|termPosition
operator|.
name|payload
argument_list|)
expr_stmt|;
name|startOffsets
operator|.
name|ints
index|[
name|i
index|]
operator|=
name|termPosition
operator|.
name|startOffset
expr_stmt|;
name|endOffsets
operator|.
name|ints
index|[
name|i
index|]
operator|=
name|termPosition
operator|.
name|endOffset
expr_stmt|;
block|}
block|}
DECL|method|ensureSize
specifier|private
name|void
name|ensureSize
parameter_list|(
name|int
name|freq
parameter_list|)
block|{
if|if
condition|(
name|freq
operator|==
literal|0
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|startOffsets
operator|.
name|ints
operator|.
name|length
operator|<
name|freq
condition|)
block|{
name|startOffsets
operator|.
name|grow
argument_list|(
name|freq
argument_list|)
expr_stmt|;
name|endOffsets
operator|.
name|grow
argument_list|(
name|freq
argument_list|)
expr_stmt|;
name|positions
operator|.
name|grow
argument_list|(
name|freq
argument_list|)
expr_stmt|;
name|payloadsLengths
operator|.
name|grow
argument_list|(
name|freq
argument_list|)
expr_stmt|;
name|payloadsStarts
operator|.
name|grow
argument_list|(
name|freq
argument_list|)
expr_stmt|;
block|}
name|payloads
operator|.
name|offset
operator|=
literal|0
expr_stmt|;
name|payloadsLengths
operator|.
name|offset
operator|=
literal|0
expr_stmt|;
name|payloadsStarts
operator|.
name|offset
operator|=
literal|0
expr_stmt|;
name|payloads
operator|.
name|grow
argument_list|(
name|freq
operator|*
literal|8
argument_list|)
expr_stmt|;
comment|// this is just a guess....
block|}
DECL|method|addPayload
specifier|private
name|void
name|addPayload
parameter_list|(
name|int
name|i
parameter_list|,
name|BytesRef
name|currPayload
parameter_list|)
block|{
if|if
condition|(
name|currPayload
operator|!=
literal|null
condition|)
block|{
name|payloadsLengths
operator|.
name|ints
index|[
name|i
index|]
operator|=
name|currPayload
operator|.
name|length
expr_stmt|;
name|payloadsStarts
operator|.
name|ints
index|[
name|i
index|]
operator|=
name|i
operator|==
literal|0
condition|?
literal|0
else|:
name|payloadsStarts
operator|.
name|ints
index|[
name|i
operator|-
literal|1
index|]
operator|+
name|payloadsLengths
operator|.
name|ints
index|[
name|i
operator|-
literal|1
index|]
expr_stmt|;
if|if
condition|(
name|payloads
operator|.
name|bytes
operator|.
name|length
operator|<
name|payloadsStarts
operator|.
name|ints
index|[
name|i
index|]
operator|+
name|payloadsLengths
operator|.
name|ints
index|[
name|i
index|]
condition|)
block|{
name|payloads
operator|.
name|offset
operator|=
literal|0
expr_stmt|;
comment|// the offset serves no purpose here. but
comment|// we must assure that it is 0 before
comment|// grow() is called
name|payloads
operator|.
name|grow
argument_list|(
name|payloads
operator|.
name|bytes
operator|.
name|length
operator|*
literal|2
argument_list|)
expr_stmt|;
comment|// just a guess
block|}
name|System
operator|.
name|arraycopy
argument_list|(
name|currPayload
operator|.
name|bytes
argument_list|,
name|currPayload
operator|.
name|offset
argument_list|,
name|payloads
operator|.
name|bytes
argument_list|,
name|payloadsStarts
operator|.
name|ints
index|[
name|i
index|]
argument_list|,
name|currPayload
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|payloadsLengths
operator|.
name|ints
index|[
name|i
index|]
operator|=
literal|0
expr_stmt|;
name|payloadsStarts
operator|.
name|ints
index|[
name|i
index|]
operator|=
name|i
operator|==
literal|0
condition|?
literal|0
else|:
name|payloadsStarts
operator|.
name|ints
index|[
name|i
operator|-
literal|1
index|]
operator|+
name|payloadsLengths
operator|.
name|ints
index|[
name|i
operator|-
literal|1
index|]
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|nextDoc
specifier|public
name|void
name|nextDoc
parameter_list|()
throws|throws
name|IOException
block|{
name|super
operator|.
name|nextDoc
argument_list|()
expr_stmt|;
name|ensureSize
argument_list|(
name|freq
argument_list|)
expr_stmt|;
name|record
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|next
specifier|public
name|TermPosition
name|next
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
block|}
end_class

end_unit

