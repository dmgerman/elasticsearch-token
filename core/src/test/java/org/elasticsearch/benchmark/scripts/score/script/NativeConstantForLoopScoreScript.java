begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.benchmark.scripts.score.script
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|benchmark
operator|.
name|scripts
operator|.
name|score
operator|.
name|script
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|Nullable
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
name|AbstractSearchScript
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
name|ExecutableScript
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
name|NativeScriptFactory
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
DECL|class|NativeConstantForLoopScoreScript
specifier|public
class|class
name|NativeConstantForLoopScoreScript
extends|extends
name|AbstractSearchScript
block|{
DECL|field|NATIVE_CONSTANT_FOR_LOOP_SCRIPT_SCORE
specifier|public
specifier|static
specifier|final
name|String
name|NATIVE_CONSTANT_FOR_LOOP_SCRIPT_SCORE
init|=
literal|"native_constant_for_loop_script_score"
decl_stmt|;
DECL|class|Factory
specifier|public
specifier|static
class|class
name|Factory
implements|implements
name|NativeScriptFactory
block|{
annotation|@
name|Override
DECL|method|newScript
specifier|public
name|ExecutableScript
name|newScript
parameter_list|(
annotation|@
name|Nullable
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|params
parameter_list|)
block|{
return|return
operator|new
name|NativeConstantForLoopScoreScript
argument_list|(
name|params
argument_list|)
return|;
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
literal|false
return|;
block|}
block|}
DECL|method|NativeConstantForLoopScoreScript
specifier|private
name|NativeConstantForLoopScoreScript
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|params
parameter_list|)
block|{      }
annotation|@
name|Override
DECL|method|run
specifier|public
name|Object
name|run
parameter_list|()
block|{
name|float
name|score
init|=
literal|0
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
literal|10
condition|;
name|i
operator|++
control|)
block|{
name|score
operator|+=
name|Math
operator|.
name|log
argument_list|(
literal|2
argument_list|)
expr_stmt|;
block|}
return|return
name|score
return|;
block|}
block|}
end_class

end_unit

