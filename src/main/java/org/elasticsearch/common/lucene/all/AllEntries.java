begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.lucene.all
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|lucene
operator|.
name|all
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
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
name|io
operator|.
name|FastCharArrayWriter
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
name|io
operator|.
name|FastStringReader
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
name|io
operator|.
name|Reader
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_import
import|import static
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Sets
operator|.
name|newHashSet
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|AllEntries
specifier|public
class|class
name|AllEntries
extends|extends
name|Reader
block|{
DECL|class|Entry
specifier|public
specifier|static
class|class
name|Entry
block|{
DECL|field|name
specifier|private
specifier|final
name|String
name|name
decl_stmt|;
DECL|field|reader
specifier|private
specifier|final
name|FastStringReader
name|reader
decl_stmt|;
DECL|field|startOffset
specifier|private
specifier|final
name|int
name|startOffset
decl_stmt|;
DECL|field|boost
specifier|private
specifier|final
name|float
name|boost
decl_stmt|;
DECL|method|Entry
specifier|public
name|Entry
parameter_list|(
name|String
name|name
parameter_list|,
name|FastStringReader
name|reader
parameter_list|,
name|int
name|startOffset
parameter_list|,
name|float
name|boost
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
name|this
operator|.
name|reader
operator|=
name|reader
expr_stmt|;
name|this
operator|.
name|startOffset
operator|=
name|startOffset
expr_stmt|;
name|this
operator|.
name|boost
operator|=
name|boost
expr_stmt|;
block|}
DECL|method|startOffset
specifier|public
name|int
name|startOffset
parameter_list|()
block|{
return|return
name|startOffset
return|;
block|}
DECL|method|name
specifier|public
name|String
name|name
parameter_list|()
block|{
return|return
name|this
operator|.
name|name
return|;
block|}
DECL|method|boost
specifier|public
name|float
name|boost
parameter_list|()
block|{
return|return
name|this
operator|.
name|boost
return|;
block|}
DECL|method|reader
specifier|public
name|FastStringReader
name|reader
parameter_list|()
block|{
return|return
name|this
operator|.
name|reader
return|;
block|}
block|}
DECL|field|entries
specifier|private
specifier|final
name|List
argument_list|<
name|Entry
argument_list|>
name|entries
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
DECL|field|current
specifier|private
name|Entry
name|current
decl_stmt|;
DECL|field|it
specifier|private
name|Iterator
argument_list|<
name|Entry
argument_list|>
name|it
decl_stmt|;
DECL|field|itsSeparatorTime
specifier|private
name|boolean
name|itsSeparatorTime
init|=
literal|false
decl_stmt|;
DECL|field|customBoost
specifier|private
name|boolean
name|customBoost
init|=
literal|false
decl_stmt|;
DECL|method|addText
specifier|public
name|void
name|addText
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|text
parameter_list|,
name|float
name|boost
parameter_list|)
block|{
if|if
condition|(
name|boost
operator|!=
literal|1.0f
condition|)
block|{
name|customBoost
operator|=
literal|true
expr_stmt|;
block|}
specifier|final
name|int
name|lastStartOffset
decl_stmt|;
if|if
condition|(
name|entries
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|lastStartOffset
operator|=
operator|-
literal|1
expr_stmt|;
block|}
else|else
block|{
specifier|final
name|Entry
name|last
init|=
name|entries
operator|.
name|get
argument_list|(
name|entries
operator|.
name|size
argument_list|()
operator|-
literal|1
argument_list|)
decl_stmt|;
name|lastStartOffset
operator|=
name|last
operator|.
name|startOffset
argument_list|()
operator|+
name|last
operator|.
name|reader
argument_list|()
operator|.
name|length
argument_list|()
expr_stmt|;
block|}
specifier|final
name|int
name|startOffset
init|=
name|lastStartOffset
operator|+
literal|1
decl_stmt|;
comment|// +1 because we insert a space between tokens
name|Entry
name|entry
init|=
operator|new
name|Entry
argument_list|(
name|name
argument_list|,
operator|new
name|FastStringReader
argument_list|(
name|text
argument_list|)
argument_list|,
name|startOffset
argument_list|,
name|boost
argument_list|)
decl_stmt|;
name|entries
operator|.
name|add
argument_list|(
name|entry
argument_list|)
expr_stmt|;
block|}
DECL|method|customBoost
specifier|public
name|boolean
name|customBoost
parameter_list|()
block|{
return|return
name|customBoost
return|;
block|}
DECL|method|clear
specifier|public
name|void
name|clear
parameter_list|()
block|{
name|this
operator|.
name|entries
operator|.
name|clear
argument_list|()
expr_stmt|;
name|this
operator|.
name|current
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|it
operator|=
literal|null
expr_stmt|;
name|itsSeparatorTime
operator|=
literal|false
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|reset
specifier|public
name|void
name|reset
parameter_list|()
block|{
try|try
block|{
for|for
control|(
name|Entry
name|entry
range|:
name|entries
control|)
block|{
name|entry
operator|.
name|reader
argument_list|()
operator|.
name|reset
argument_list|()
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"should not happen"
argument_list|)
throw|;
block|}
name|it
operator|=
name|entries
operator|.
name|iterator
argument_list|()
expr_stmt|;
if|if
condition|(
name|it
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|current
operator|=
name|it
operator|.
name|next
argument_list|()
expr_stmt|;
name|itsSeparatorTime
operator|=
literal|true
expr_stmt|;
block|}
block|}
DECL|method|buildText
specifier|public
name|String
name|buildText
parameter_list|()
block|{
name|reset
argument_list|()
expr_stmt|;
name|FastCharArrayWriter
name|writer
init|=
operator|new
name|FastCharArrayWriter
argument_list|()
decl_stmt|;
for|for
control|(
name|Entry
name|entry
range|:
name|entries
control|)
block|{
name|writer
operator|.
name|append
argument_list|(
name|entry
operator|.
name|reader
argument_list|()
argument_list|)
expr_stmt|;
name|writer
operator|.
name|append
argument_list|(
literal|' '
argument_list|)
expr_stmt|;
block|}
name|reset
argument_list|()
expr_stmt|;
return|return
name|writer
operator|.
name|toString
argument_list|()
return|;
block|}
DECL|method|entries
specifier|public
name|List
argument_list|<
name|Entry
argument_list|>
name|entries
parameter_list|()
block|{
return|return
name|this
operator|.
name|entries
return|;
block|}
DECL|method|fields
specifier|public
name|Set
argument_list|<
name|String
argument_list|>
name|fields
parameter_list|()
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|fields
init|=
name|newHashSet
argument_list|()
decl_stmt|;
for|for
control|(
name|Entry
name|entry
range|:
name|entries
control|)
block|{
name|fields
operator|.
name|add
argument_list|(
name|entry
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|fields
return|;
block|}
comment|// compute the boost for a token with the given startOffset
DECL|method|boost
specifier|public
name|float
name|boost
parameter_list|(
name|int
name|startOffset
parameter_list|)
block|{
if|if
condition|(
operator|!
name|entries
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|int
name|lo
init|=
literal|0
decl_stmt|,
name|hi
init|=
name|entries
operator|.
name|size
argument_list|()
operator|-
literal|1
decl_stmt|;
while|while
condition|(
name|lo
operator|<=
name|hi
condition|)
block|{
specifier|final
name|int
name|mid
init|=
operator|(
name|lo
operator|+
name|hi
operator|)
operator|>>>
literal|1
decl_stmt|;
specifier|final
name|int
name|midOffset
init|=
name|entries
operator|.
name|get
argument_list|(
name|mid
argument_list|)
operator|.
name|startOffset
argument_list|()
decl_stmt|;
if|if
condition|(
name|startOffset
operator|<
name|midOffset
condition|)
block|{
name|hi
operator|=
name|mid
operator|-
literal|1
expr_stmt|;
block|}
else|else
block|{
name|lo
operator|=
name|mid
operator|+
literal|1
expr_stmt|;
block|}
block|}
specifier|final
name|int
name|index
init|=
name|Math
operator|.
name|max
argument_list|(
literal|0
argument_list|,
name|hi
argument_list|)
decl_stmt|;
comment|// protection against broken token streams
assert|assert
name|entries
operator|.
name|get
argument_list|(
name|index
argument_list|)
operator|.
name|startOffset
argument_list|()
operator|<=
name|startOffset
assert|;
assert|assert
name|index
operator|==
name|entries
operator|.
name|size
argument_list|()
operator|-
literal|1
operator|||
name|entries
operator|.
name|get
argument_list|(
name|index
operator|+
literal|1
argument_list|)
operator|.
name|startOffset
argument_list|()
operator|>
name|startOffset
assert|;
return|return
name|entries
operator|.
name|get
argument_list|(
name|index
argument_list|)
operator|.
name|boost
argument_list|()
return|;
block|}
return|return
literal|1.0f
return|;
block|}
annotation|@
name|Override
DECL|method|read
specifier|public
name|int
name|read
parameter_list|(
name|char
index|[]
name|cbuf
parameter_list|,
name|int
name|off
parameter_list|,
name|int
name|len
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|current
operator|==
literal|null
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
if|if
condition|(
name|customBoost
condition|)
block|{
name|int
name|result
init|=
name|current
operator|.
name|reader
argument_list|()
operator|.
name|read
argument_list|(
name|cbuf
argument_list|,
name|off
argument_list|,
name|len
argument_list|)
decl_stmt|;
if|if
condition|(
name|result
operator|==
operator|-
literal|1
condition|)
block|{
if|if
condition|(
name|itsSeparatorTime
condition|)
block|{
name|itsSeparatorTime
operator|=
literal|false
expr_stmt|;
name|cbuf
index|[
name|off
index|]
operator|=
literal|' '
expr_stmt|;
return|return
literal|1
return|;
block|}
name|itsSeparatorTime
operator|=
literal|true
expr_stmt|;
comment|// close(); No need to close, we work on in mem readers
if|if
condition|(
name|it
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|current
operator|=
name|it
operator|.
name|next
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|current
operator|=
literal|null
expr_stmt|;
block|}
return|return
name|read
argument_list|(
name|cbuf
argument_list|,
name|off
argument_list|,
name|len
argument_list|)
return|;
block|}
return|return
name|result
return|;
block|}
else|else
block|{
name|int
name|read
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|len
operator|>
literal|0
condition|)
block|{
name|int
name|result
init|=
name|current
operator|.
name|reader
argument_list|()
operator|.
name|read
argument_list|(
name|cbuf
argument_list|,
name|off
argument_list|,
name|len
argument_list|)
decl_stmt|;
if|if
condition|(
name|result
operator|==
operator|-
literal|1
condition|)
block|{
if|if
condition|(
name|it
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|current
operator|=
name|it
operator|.
name|next
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|current
operator|=
literal|null
expr_stmt|;
if|if
condition|(
name|read
operator|==
literal|0
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
return|return
name|read
return|;
block|}
name|cbuf
index|[
name|off
operator|++
index|]
operator|=
literal|' '
expr_stmt|;
name|read
operator|++
expr_stmt|;
name|len
operator|--
expr_stmt|;
block|}
else|else
block|{
name|read
operator|+=
name|result
expr_stmt|;
name|off
operator|+=
name|result
expr_stmt|;
name|len
operator|-=
name|result
expr_stmt|;
block|}
block|}
return|return
name|read
return|;
block|}
block|}
annotation|@
name|Override
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
block|{
if|if
condition|(
name|current
operator|!=
literal|null
condition|)
block|{
comment|// no need to close, these are readers on strings
name|current
operator|=
literal|null
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|ready
specifier|public
name|boolean
name|ready
parameter_list|()
throws|throws
name|IOException
block|{
return|return
operator|(
name|current
operator|!=
literal|null
operator|)
operator|&&
name|current
operator|.
name|reader
argument_list|()
operator|.
name|ready
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|Entry
name|entry
range|:
name|entries
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|entry
operator|.
name|name
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|','
argument_list|)
expr_stmt|;
block|}
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
end_class

end_unit

