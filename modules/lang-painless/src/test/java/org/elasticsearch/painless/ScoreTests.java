begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.painless
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|painless
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
name|search
operator|.
name|Scorer
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
name|Collections
import|;
end_import

begin_class
DECL|class|ScoreTests
specifier|public
class|class
name|ScoreTests
extends|extends
name|ScriptTestCase
block|{
comment|/** Most of a dummy scorer impl that requires overriding just score(). */
DECL|class|MockScorer
specifier|abstract
class|class
name|MockScorer
extends|extends
name|Scorer
block|{
DECL|method|MockScorer
name|MockScorer
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
literal|0
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
DECL|method|iterator
specifier|public
name|DocIdSetIterator
name|iterator
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
block|}
DECL|method|testScoreWorks
specifier|public
name|void
name|testScoreWorks
parameter_list|()
block|{
name|assertEquals
argument_list|(
literal|2.5
argument_list|,
name|exec
argument_list|(
literal|"_score"
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|,
operator|new
name|MockScorer
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|float
name|score
parameter_list|()
throws|throws
name|IOException
block|{
return|return
literal|2.5f
return|;
block|}
block|}
argument_list|,
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testScoreNotUsed
specifier|public
name|void
name|testScoreNotUsed
parameter_list|()
block|{
name|assertEquals
argument_list|(
literal|3.5
argument_list|,
name|exec
argument_list|(
literal|"3.5"
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|,
operator|new
name|MockScorer
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|float
name|score
parameter_list|()
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|AssertionError
argument_list|(
literal|"score() should not be called"
argument_list|)
throw|;
block|}
block|}
argument_list|,
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testScoreCached
specifier|public
name|void
name|testScoreCached
parameter_list|()
block|{
name|assertEquals
argument_list|(
literal|9.0
argument_list|,
name|exec
argument_list|(
literal|"_score + _score"
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|,
operator|new
name|MockScorer
argument_list|()
block|{
specifier|private
name|boolean
name|used
init|=
literal|false
decl_stmt|;
annotation|@
name|Override
specifier|public
name|float
name|score
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|used
operator|==
literal|false
condition|)
block|{
return|return
literal|4.5f
return|;
block|}
throw|throw
operator|new
name|AssertionError
argument_list|(
literal|"score() should not be called twice"
argument_list|)
throw|;
block|}
block|}
argument_list|,
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

