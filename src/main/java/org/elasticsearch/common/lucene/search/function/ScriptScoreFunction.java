begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.lucene.search.function
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|lucene
operator|.
name|search
operator|.
name|function
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
name|LeafReaderContext
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
name|Explanation
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
name|elasticsearch
operator|.
name|script
operator|.
name|ExplainableSearchScript
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|LeafSearchScript
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|ScriptException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|SearchScript
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
name|Map
import|;
end_import

begin_class
DECL|class|ScriptScoreFunction
specifier|public
class|class
name|ScriptScoreFunction
extends|extends
name|ScoreFunction
block|{
DECL|class|CannedScorer
specifier|static
specifier|final
class|class
name|CannedScorer
extends|extends
name|Scorer
block|{
DECL|field|docid
specifier|protected
name|int
name|docid
decl_stmt|;
DECL|field|score
specifier|protected
name|float
name|score
decl_stmt|;
DECL|method|CannedScorer
specifier|public
name|CannedScorer
parameter_list|()
block|{
name|super
argument_list|(
literal|null
argument_list|)
expr_stmt|;
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
name|docid
return|;
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
return|return
name|score
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
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
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
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
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
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
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
literal|1
return|;
block|}
block|}
DECL|field|sScript
specifier|private
specifier|final
name|String
name|sScript
decl_stmt|;
DECL|field|params
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|params
decl_stmt|;
DECL|field|script
specifier|private
specifier|final
name|SearchScript
name|script
decl_stmt|;
DECL|method|ScriptScoreFunction
specifier|public
name|ScriptScoreFunction
parameter_list|(
name|String
name|sScript
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|params
parameter_list|,
name|SearchScript
name|script
parameter_list|)
block|{
name|super
argument_list|(
name|CombineFunction
operator|.
name|REPLACE
argument_list|)
expr_stmt|;
name|this
operator|.
name|sScript
operator|=
name|sScript
expr_stmt|;
name|this
operator|.
name|params
operator|=
name|params
expr_stmt|;
name|this
operator|.
name|script
operator|=
name|script
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getLeafScoreFunction
specifier|public
name|LeafScoreFunction
name|getLeafScoreFunction
parameter_list|(
name|LeafReaderContext
name|ctx
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|LeafSearchScript
name|leafScript
init|=
name|script
operator|.
name|getLeafSearchScript
argument_list|(
name|ctx
argument_list|)
decl_stmt|;
specifier|final
name|CannedScorer
name|scorer
init|=
operator|new
name|CannedScorer
argument_list|()
decl_stmt|;
name|leafScript
operator|.
name|setScorer
argument_list|(
name|scorer
argument_list|)
expr_stmt|;
return|return
operator|new
name|LeafScoreFunction
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|double
name|score
parameter_list|(
name|int
name|docId
parameter_list|,
name|float
name|subQueryScore
parameter_list|)
block|{
name|leafScript
operator|.
name|setDocument
argument_list|(
name|docId
argument_list|)
expr_stmt|;
name|scorer
operator|.
name|docid
operator|=
name|docId
expr_stmt|;
name|scorer
operator|.
name|score
operator|=
name|subQueryScore
expr_stmt|;
name|double
name|result
init|=
name|leafScript
operator|.
name|runAsDouble
argument_list|()
decl_stmt|;
if|if
condition|(
name|Double
operator|.
name|isNaN
argument_list|(
name|result
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|ScriptException
argument_list|(
literal|"script_score returned NaN"
argument_list|)
throw|;
block|}
return|return
name|result
return|;
block|}
annotation|@
name|Override
specifier|public
name|Explanation
name|explainScore
parameter_list|(
name|int
name|docId
parameter_list|,
name|Explanation
name|subQueryScore
parameter_list|)
throws|throws
name|IOException
block|{
name|Explanation
name|exp
decl_stmt|;
if|if
condition|(
name|leafScript
operator|instanceof
name|ExplainableSearchScript
condition|)
block|{
name|leafScript
operator|.
name|setDocument
argument_list|(
name|docId
argument_list|)
expr_stmt|;
name|scorer
operator|.
name|docid
operator|=
name|docId
expr_stmt|;
name|scorer
operator|.
name|score
operator|=
name|subQueryScore
operator|.
name|getValue
argument_list|()
expr_stmt|;
name|exp
operator|=
operator|(
operator|(
name|ExplainableSearchScript
operator|)
name|leafScript
operator|)
operator|.
name|explain
argument_list|(
name|subQueryScore
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|double
name|score
init|=
name|score
argument_list|(
name|docId
argument_list|,
name|subQueryScore
operator|.
name|getValue
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|explanation
init|=
literal|"script score function, computed with script:\""
operator|+
name|sScript
decl_stmt|;
if|if
condition|(
name|params
operator|!=
literal|null
condition|)
block|{
name|explanation
operator|+=
literal|"\" and parameters: \n"
operator|+
name|params
operator|.
name|toString
argument_list|()
expr_stmt|;
block|}
name|Explanation
name|scoreExp
init|=
name|Explanation
operator|.
name|match
argument_list|(
name|subQueryScore
operator|.
name|getValue
argument_list|()
argument_list|,
literal|"_score: "
argument_list|,
name|subQueryScore
argument_list|)
decl_stmt|;
return|return
name|Explanation
operator|.
name|match
argument_list|(
name|CombineFunction
operator|.
name|toFloat
argument_list|(
name|score
argument_list|)
argument_list|,
name|explanation
argument_list|,
name|scoreExp
argument_list|)
return|;
block|}
return|return
name|exp
return|;
block|}
block|}
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
return|return
literal|"script["
operator|+
name|sScript
operator|+
literal|"], params ["
operator|+
name|params
operator|+
literal|"]"
return|;
block|}
block|}
end_class

end_unit

