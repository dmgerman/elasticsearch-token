begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.analysis.common
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|analysis
operator|.
name|common
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
name|CharArraySet
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
name|TokenFilter
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
name|CharTermAttribute
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
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_comment
comment|/**  * A token filter that generates unique tokens. Can remove unique tokens only on the same  * position increments as well.  */
end_comment

begin_class
DECL|class|UniqueTokenFilter
class|class
name|UniqueTokenFilter
extends|extends
name|TokenFilter
block|{
DECL|field|termAttribute
specifier|private
specifier|final
name|CharTermAttribute
name|termAttribute
init|=
name|addAttribute
argument_list|(
name|CharTermAttribute
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|posIncAttribute
specifier|private
specifier|final
name|PositionIncrementAttribute
name|posIncAttribute
init|=
name|addAttribute
argument_list|(
name|PositionIncrementAttribute
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|previous
specifier|private
specifier|final
name|CharArraySet
name|previous
init|=
operator|new
name|CharArraySet
argument_list|(
literal|8
argument_list|,
literal|false
argument_list|)
decl_stmt|;
DECL|field|onlyOnSamePosition
specifier|private
specifier|final
name|boolean
name|onlyOnSamePosition
decl_stmt|;
DECL|method|UniqueTokenFilter
name|UniqueTokenFilter
parameter_list|(
name|TokenStream
name|in
parameter_list|)
block|{
name|this
argument_list|(
name|in
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
DECL|method|UniqueTokenFilter
name|UniqueTokenFilter
parameter_list|(
name|TokenStream
name|in
parameter_list|,
name|boolean
name|onlyOnSamePosition
parameter_list|)
block|{
name|super
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|this
operator|.
name|onlyOnSamePosition
operator|=
name|onlyOnSamePosition
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|incrementToken
specifier|public
specifier|final
name|boolean
name|incrementToken
parameter_list|()
throws|throws
name|IOException
block|{
while|while
condition|(
name|input
operator|.
name|incrementToken
argument_list|()
condition|)
block|{
specifier|final
name|char
name|term
index|[]
init|=
name|termAttribute
operator|.
name|buffer
argument_list|()
decl_stmt|;
specifier|final
name|int
name|length
init|=
name|termAttribute
operator|.
name|length
argument_list|()
decl_stmt|;
name|boolean
name|duplicate
decl_stmt|;
if|if
condition|(
name|onlyOnSamePosition
condition|)
block|{
specifier|final
name|int
name|posIncrement
init|=
name|posIncAttribute
operator|.
name|getPositionIncrement
argument_list|()
decl_stmt|;
if|if
condition|(
name|posIncrement
operator|>
literal|0
condition|)
block|{
name|previous
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
name|duplicate
operator|=
operator|(
name|posIncrement
operator|==
literal|0
operator|&&
name|previous
operator|.
name|contains
argument_list|(
name|term
argument_list|,
literal|0
argument_list|,
name|length
argument_list|)
operator|)
expr_stmt|;
block|}
else|else
block|{
name|duplicate
operator|=
name|previous
operator|.
name|contains
argument_list|(
name|term
argument_list|,
literal|0
argument_list|,
name|length
argument_list|)
expr_stmt|;
block|}
comment|// clone the term, and add to the set of seen terms.
name|char
name|saved
index|[]
init|=
operator|new
name|char
index|[
name|length
index|]
decl_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|term
argument_list|,
literal|0
argument_list|,
name|saved
argument_list|,
literal|0
argument_list|,
name|length
argument_list|)
expr_stmt|;
name|previous
operator|.
name|add
argument_list|(
name|saved
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|duplicate
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
annotation|@
name|Override
DECL|method|reset
specifier|public
specifier|final
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
name|previous
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit
