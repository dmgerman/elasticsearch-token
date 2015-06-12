begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.lucene
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|lucene
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
name|*
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
comment|/**  *  */
end_comment

begin_class
DECL|class|MinimumScoreCollector
specifier|public
class|class
name|MinimumScoreCollector
extends|extends
name|SimpleCollector
block|{
DECL|field|collector
specifier|private
specifier|final
name|Collector
name|collector
decl_stmt|;
DECL|field|minimumScore
specifier|private
specifier|final
name|float
name|minimumScore
decl_stmt|;
DECL|field|scorer
specifier|private
name|Scorer
name|scorer
decl_stmt|;
DECL|field|leafCollector
specifier|private
name|LeafCollector
name|leafCollector
decl_stmt|;
DECL|method|MinimumScoreCollector
specifier|public
name|MinimumScoreCollector
parameter_list|(
name|Collector
name|collector
parameter_list|,
name|float
name|minimumScore
parameter_list|)
block|{
name|this
operator|.
name|collector
operator|=
name|collector
expr_stmt|;
name|this
operator|.
name|minimumScore
operator|=
name|minimumScore
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|setScorer
specifier|public
name|void
name|setScorer
parameter_list|(
name|Scorer
name|scorer
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
operator|(
name|scorer
operator|instanceof
name|ScoreCachingWrappingScorer
operator|)
condition|)
block|{
name|scorer
operator|=
operator|new
name|ScoreCachingWrappingScorer
argument_list|(
name|scorer
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|scorer
operator|=
name|scorer
expr_stmt|;
name|leafCollector
operator|.
name|setScorer
argument_list|(
name|scorer
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|collect
specifier|public
name|void
name|collect
parameter_list|(
name|int
name|doc
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|scorer
operator|.
name|score
argument_list|()
operator|>=
name|minimumScore
condition|)
block|{
name|leafCollector
operator|.
name|collect
argument_list|(
name|doc
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|doSetNextReader
specifier|public
name|void
name|doSetNextReader
parameter_list|(
name|LeafReaderContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|leafCollector
operator|=
name|collector
operator|.
name|getLeafCollector
argument_list|(
name|context
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|needsScores
specifier|public
name|boolean
name|needsScores
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
block|}
end_class

end_unit
