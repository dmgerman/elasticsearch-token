begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.search.child
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|search
operator|.
name|child
package|;
end_package

begin_import
import|import
name|com
operator|.
name|carrotsearch
operator|.
name|hppc
operator|.
name|FloatArrayList
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
name|Scorer
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
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_class
DECL|class|MockScorer
class|class
name|MockScorer
extends|extends
name|Scorer
block|{
DECL|field|scoreType
specifier|final
name|ScoreType
name|scoreType
decl_stmt|;
DECL|field|scores
name|FloatArrayList
name|scores
decl_stmt|;
DECL|method|MockScorer
name|MockScorer
parameter_list|(
name|ScoreType
name|scoreType
parameter_list|)
block|{
name|super
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|this
operator|.
name|scoreType
operator|=
name|scoreType
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|score
specifier|public
name|float
name|score
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|scoreType
operator|==
name|ScoreType
operator|.
name|NONE
condition|)
block|{
return|return
literal|1.0f
return|;
block|}
name|float
name|aggregateScore
init|=
literal|0
decl_stmt|;
comment|// in the case of a min value, it can't start at 0 (the lowest score); in all cases, it doesn't hurt to use the
comment|//  first score, so we can safely use the first value by skipping it in the loop
if|if
condition|(
name|scores
operator|.
name|elementsCount
operator|!=
literal|0
condition|)
block|{
name|aggregateScore
operator|=
name|scores
operator|.
name|buffer
index|[
literal|0
index|]
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|scores
operator|.
name|elementsCount
condition|;
name|i
operator|++
control|)
block|{
name|float
name|score
init|=
name|scores
operator|.
name|buffer
index|[
name|i
index|]
decl_stmt|;
switch|switch
condition|(
name|scoreType
condition|)
block|{
case|case
name|MIN
case|:
if|if
condition|(
name|aggregateScore
operator|>
name|score
condition|)
block|{
name|aggregateScore
operator|=
name|score
expr_stmt|;
block|}
break|break;
case|case
name|MAX
case|:
if|if
condition|(
name|aggregateScore
operator|<
name|score
condition|)
block|{
name|aggregateScore
operator|=
name|score
expr_stmt|;
block|}
break|break;
case|case
name|SUM
case|:
case|case
name|AVG
case|:
name|aggregateScore
operator|+=
name|score
expr_stmt|;
break|break;
block|}
block|}
if|if
condition|(
name|scoreType
operator|==
name|ScoreType
operator|.
name|AVG
condition|)
block|{
name|aggregateScore
operator|/=
name|scores
operator|.
name|elementsCount
expr_stmt|;
block|}
block|}
return|return
name|aggregateScore
return|;
block|}
annotation|@
name|Override
DECL|method|freq
specifier|public
name|int
name|freq
parameter_list|()
throws|throws
name|IOException
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
DECL|method|docID
specifier|public
name|int
name|docID
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
DECL|method|nextDoc
specifier|public
name|int
name|nextDoc
parameter_list|()
throws|throws
name|IOException
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
DECL|method|advance
specifier|public
name|int
name|advance
parameter_list|(
name|int
name|target
parameter_list|)
throws|throws
name|IOException
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
DECL|method|cost
specifier|public
name|long
name|cost
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
block|}
end_class

end_unit
