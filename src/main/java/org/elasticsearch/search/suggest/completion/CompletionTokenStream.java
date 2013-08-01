begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.suggest.completion
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|suggest
operator|.
name|completion
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
name|analysis
operator|.
name|TokenStream
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
name|analysis
operator|.
name|tokenattributes
operator|.
name|PayloadAttribute
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
name|analysis
operator|.
name|tokenattributes
operator|.
name|PositionIncrementAttribute
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
name|analysis
operator|.
name|tokenattributes
operator|.
name|TermToBytesRefAttribute
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
name|AttributeImpl
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
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|fst
operator|.
name|Util
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|CompletionTokenStream
specifier|public
specifier|final
class|class
name|CompletionTokenStream
extends|extends
name|TokenStream
block|{
DECL|field|payloadAttr
specifier|private
specifier|final
name|PayloadAttribute
name|payloadAttr
init|=
name|addAttribute
argument_list|(
name|PayloadAttribute
operator|.
name|class
argument_list|)
decl_stmt|;
empty_stmt|;
DECL|field|posAttr
specifier|private
specifier|final
name|PositionIncrementAttribute
name|posAttr
init|=
name|addAttribute
argument_list|(
name|PositionIncrementAttribute
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|bytesAtt
specifier|private
specifier|final
name|ByteTermAttribute
name|bytesAtt
init|=
name|addAttribute
argument_list|(
name|ByteTermAttribute
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|input
specifier|private
specifier|final
name|TokenStream
name|input
decl_stmt|;
DECL|field|payload
specifier|private
name|BytesRef
name|payload
decl_stmt|;
DECL|field|finiteStrings
specifier|private
name|Iterator
argument_list|<
name|IntsRef
argument_list|>
name|finiteStrings
decl_stmt|;
DECL|field|toFiniteStrings
specifier|private
name|ToFiniteStrings
name|toFiniteStrings
decl_stmt|;
DECL|field|posInc
specifier|private
name|int
name|posInc
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|MAX_PATHS
specifier|private
specifier|static
specifier|final
name|int
name|MAX_PATHS
init|=
literal|256
decl_stmt|;
DECL|field|scratch
specifier|private
specifier|final
name|BytesRef
name|scratch
init|=
operator|new
name|BytesRef
argument_list|()
decl_stmt|;
DECL|method|CompletionTokenStream
specifier|public
name|CompletionTokenStream
parameter_list|(
name|TokenStream
name|input
parameter_list|,
name|BytesRef
name|payload
parameter_list|,
name|ToFiniteStrings
name|toFiniteStrings
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|input
operator|=
name|input
expr_stmt|;
name|this
operator|.
name|payload
operator|=
name|payload
expr_stmt|;
name|this
operator|.
name|toFiniteStrings
operator|=
name|toFiniteStrings
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|incrementToken
specifier|public
name|boolean
name|incrementToken
parameter_list|()
throws|throws
name|IOException
block|{
name|clearAttributes
argument_list|()
expr_stmt|;
if|if
condition|(
name|finiteStrings
operator|==
literal|null
condition|)
block|{
name|Set
argument_list|<
name|IntsRef
argument_list|>
name|strings
init|=
name|toFiniteStrings
operator|.
name|toFiniteStrings
argument_list|(
name|input
argument_list|)
decl_stmt|;
if|if
condition|(
name|strings
operator|.
name|size
argument_list|()
operator|>
name|MAX_PATHS
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"TokenStream expanded to "
operator|+
name|strings
operator|.
name|size
argument_list|()
operator|+
literal|" finite strings. Only<= "
operator|+
name|MAX_PATHS
operator|+
literal|" finite strings are supported"
argument_list|)
throw|;
block|}
name|posInc
operator|=
name|strings
operator|.
name|size
argument_list|()
expr_stmt|;
name|finiteStrings
operator|=
name|strings
operator|.
name|iterator
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|finiteStrings
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|posAttr
operator|.
name|setPositionIncrement
argument_list|(
name|posInc
argument_list|)
expr_stmt|;
comment|/*              * this posInc encodes the number of paths that this surface form              * produced. Multi Fields have the same surface form and therefore sum up              */
name|posInc
operator|=
literal|0
expr_stmt|;
name|Util
operator|.
name|toBytesRef
argument_list|(
name|finiteStrings
operator|.
name|next
argument_list|()
argument_list|,
name|scratch
argument_list|)
expr_stmt|;
comment|// now we have UTF-8
name|bytesAtt
operator|.
name|setBytesRef
argument_list|(
name|scratch
argument_list|)
expr_stmt|;
if|if
condition|(
name|payload
operator|!=
literal|null
condition|)
block|{
name|payloadAttr
operator|.
name|setPayload
argument_list|(
name|this
operator|.
name|payload
argument_list|)
expr_stmt|;
block|}
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
annotation|@
name|Override
DECL|method|end
specifier|public
name|void
name|end
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|posInc
operator|==
operator|-
literal|1
condition|)
block|{
name|input
operator|.
name|end
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|posInc
operator|==
operator|-
literal|1
condition|)
block|{
name|input
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
DECL|interface|ToFiniteStrings
specifier|public
specifier|static
interface|interface
name|ToFiniteStrings
block|{
DECL|method|toFiniteStrings
specifier|public
name|Set
argument_list|<
name|IntsRef
argument_list|>
name|toFiniteStrings
parameter_list|(
name|TokenStream
name|stream
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
annotation|@
name|Override
DECL|method|reset
specifier|public
name|void
name|reset
parameter_list|()
throws|throws
name|IOException
block|{
name|super
operator|.
name|reset
argument_list|()
expr_stmt|;
name|finiteStrings
operator|=
literal|null
expr_stmt|;
name|posInc
operator|=
operator|-
literal|1
expr_stmt|;
block|}
DECL|interface|ByteTermAttribute
specifier|public
interface|interface
name|ByteTermAttribute
extends|extends
name|TermToBytesRefAttribute
block|{
DECL|method|setBytesRef
specifier|public
name|void
name|setBytesRef
parameter_list|(
name|BytesRef
name|bytes
parameter_list|)
function_decl|;
block|}
DECL|class|ByteTermAttributeImpl
specifier|public
specifier|static
specifier|final
class|class
name|ByteTermAttributeImpl
extends|extends
name|AttributeImpl
implements|implements
name|ByteTermAttribute
implements|,
name|TermToBytesRefAttribute
block|{
DECL|field|bytes
specifier|private
name|BytesRef
name|bytes
decl_stmt|;
annotation|@
name|Override
DECL|method|fillBytesRef
specifier|public
name|int
name|fillBytesRef
parameter_list|()
block|{
return|return
name|bytes
operator|.
name|hashCode
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|getBytesRef
specifier|public
name|BytesRef
name|getBytesRef
parameter_list|()
block|{
return|return
name|bytes
return|;
block|}
annotation|@
name|Override
DECL|method|setBytesRef
specifier|public
name|void
name|setBytesRef
parameter_list|(
name|BytesRef
name|bytes
parameter_list|)
block|{
name|this
operator|.
name|bytes
operator|=
name|bytes
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|clear
specifier|public
name|void
name|clear
parameter_list|()
block|{         }
annotation|@
name|Override
DECL|method|copyTo
specifier|public
name|void
name|copyTo
parameter_list|(
name|AttributeImpl
name|target
parameter_list|)
block|{
name|ByteTermAttributeImpl
name|other
init|=
operator|(
name|ByteTermAttributeImpl
operator|)
name|target
decl_stmt|;
name|other
operator|.
name|bytes
operator|=
name|bytes
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

