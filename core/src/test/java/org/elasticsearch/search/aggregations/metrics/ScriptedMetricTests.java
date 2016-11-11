begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.metrics
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|metrics
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|Script
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
name|ScriptType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|BaseAggregationTestCase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|metrics
operator|.
name|scripted
operator|.
name|ScriptedMetricAggregationBuilder
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
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
DECL|class|ScriptedMetricTests
specifier|public
class|class
name|ScriptedMetricTests
extends|extends
name|BaseAggregationTestCase
argument_list|<
name|ScriptedMetricAggregationBuilder
argument_list|>
block|{
annotation|@
name|Override
DECL|method|createTestAggregatorBuilder
specifier|protected
name|ScriptedMetricAggregationBuilder
name|createTestAggregatorBuilder
parameter_list|()
block|{
name|ScriptedMetricAggregationBuilder
name|factory
init|=
operator|new
name|ScriptedMetricAggregationBuilder
argument_list|(
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|20
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|factory
operator|.
name|initScript
argument_list|(
name|randomScript
argument_list|(
literal|"initScript"
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|factory
operator|.
name|mapScript
argument_list|(
name|randomScript
argument_list|(
literal|"mapScript"
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|factory
operator|.
name|combineScript
argument_list|(
name|randomScript
argument_list|(
literal|"combineScript"
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|factory
operator|.
name|reduceScript
argument_list|(
name|randomScript
argument_list|(
literal|"reduceScript"
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|params
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|()
decl_stmt|;
name|params
operator|.
name|put
argument_list|(
literal|"foo"
argument_list|,
literal|"bar"
argument_list|)
expr_stmt|;
name|factory
operator|.
name|params
argument_list|(
name|params
argument_list|)
expr_stmt|;
block|}
return|return
name|factory
return|;
block|}
DECL|method|randomScript
specifier|private
name|Script
name|randomScript
parameter_list|(
name|String
name|script
parameter_list|)
block|{
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
return|return
operator|new
name|Script
argument_list|(
name|script
argument_list|)
return|;
block|}
else|else
block|{
return|return
operator|new
name|Script
argument_list|(
name|randomFrom
argument_list|(
name|ScriptType
operator|.
name|values
argument_list|()
argument_list|)
argument_list|,
name|randomFrom
argument_list|(
literal|"my_lang"
argument_list|,
name|Script
operator|.
name|DEFAULT_SCRIPT_LANG
argument_list|)
argument_list|,
name|script
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

