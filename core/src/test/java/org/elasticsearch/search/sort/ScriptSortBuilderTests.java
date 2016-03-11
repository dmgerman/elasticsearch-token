begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.sort
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|sort
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
name|search
operator|.
name|sort
operator|.
name|ScriptSortBuilder
operator|.
name|ScriptSortType
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
DECL|class|ScriptSortBuilderTests
specifier|public
class|class
name|ScriptSortBuilderTests
extends|extends
name|AbstractSortTestCase
argument_list|<
name|ScriptSortBuilder
argument_list|>
block|{
annotation|@
name|Override
DECL|method|createTestItem
specifier|protected
name|ScriptSortBuilder
name|createTestItem
parameter_list|()
block|{
name|ScriptSortBuilder
name|builder
init|=
operator|new
name|ScriptSortBuilder
argument_list|(
operator|new
name|Script
argument_list|(
name|randomAsciiOfLengthBetween
argument_list|(
literal|5
argument_list|,
literal|10
argument_list|)
argument_list|)
argument_list|,
name|randomBoolean
argument_list|()
condition|?
name|ScriptSortType
operator|.
name|NUMBER
else|:
name|ScriptSortType
operator|.
name|STRING
argument_list|)
decl_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|builder
operator|.
name|order
argument_list|(
name|RandomSortDataGenerator
operator|.
name|order
argument_list|(
name|builder
operator|.
name|order
argument_list|()
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
name|builder
operator|.
name|sortMode
argument_list|(
name|RandomSortDataGenerator
operator|.
name|mode
argument_list|(
name|builder
operator|.
name|sortMode
argument_list|()
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
name|builder
operator|.
name|setNestedFilter
argument_list|(
name|RandomSortDataGenerator
operator|.
name|nestedFilter
argument_list|(
name|builder
operator|.
name|getNestedFilter
argument_list|()
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
name|builder
operator|.
name|setNestedPath
argument_list|(
name|RandomSortDataGenerator
operator|.
name|randomAscii
argument_list|(
name|builder
operator|.
name|getNestedPath
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|builder
return|;
block|}
annotation|@
name|Override
DECL|method|mutate
specifier|protected
name|ScriptSortBuilder
name|mutate
parameter_list|(
name|ScriptSortBuilder
name|original
parameter_list|)
throws|throws
name|IOException
block|{
name|ScriptSortBuilder
name|result
decl_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
comment|// change one of the constructor args, copy the rest over
name|Script
name|script
init|=
name|original
operator|.
name|script
argument_list|()
decl_stmt|;
name|ScriptSortType
name|type
init|=
name|original
operator|.
name|type
argument_list|()
decl_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|result
operator|=
operator|new
name|ScriptSortBuilder
argument_list|(
operator|new
name|Script
argument_list|(
name|script
operator|.
name|getScript
argument_list|()
operator|+
literal|"_suffix"
argument_list|)
argument_list|,
name|type
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|result
operator|=
operator|new
name|ScriptSortBuilder
argument_list|(
name|script
argument_list|,
name|type
operator|.
name|equals
argument_list|(
name|ScriptSortType
operator|.
name|NUMBER
argument_list|)
condition|?
name|ScriptSortType
operator|.
name|STRING
else|:
name|ScriptSortType
operator|.
name|NUMBER
argument_list|)
expr_stmt|;
block|}
name|result
operator|.
name|order
argument_list|(
name|original
operator|.
name|order
argument_list|()
argument_list|)
expr_stmt|;
name|result
operator|.
name|sortMode
argument_list|(
name|original
operator|.
name|sortMode
argument_list|()
argument_list|)
expr_stmt|;
name|result
operator|.
name|setNestedFilter
argument_list|(
name|original
operator|.
name|getNestedFilter
argument_list|()
argument_list|)
expr_stmt|;
name|result
operator|.
name|setNestedPath
argument_list|(
name|original
operator|.
name|getNestedPath
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
name|result
operator|=
operator|new
name|ScriptSortBuilder
argument_list|(
name|original
argument_list|)
expr_stmt|;
switch|switch
condition|(
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|3
argument_list|)
condition|)
block|{
case|case
literal|0
case|:
if|if
condition|(
name|original
operator|.
name|order
argument_list|()
operator|==
name|SortOrder
operator|.
name|ASC
condition|)
block|{
name|result
operator|.
name|order
argument_list|(
name|SortOrder
operator|.
name|DESC
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|result
operator|.
name|order
argument_list|(
name|SortOrder
operator|.
name|ASC
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
literal|1
case|:
name|result
operator|.
name|sortMode
argument_list|(
name|RandomSortDataGenerator
operator|.
name|mode
argument_list|(
name|original
operator|.
name|sortMode
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
literal|2
case|:
name|result
operator|.
name|setNestedFilter
argument_list|(
name|RandomSortDataGenerator
operator|.
name|nestedFilter
argument_list|(
name|original
operator|.
name|getNestedFilter
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
literal|3
case|:
name|result
operator|.
name|setNestedPath
argument_list|(
name|original
operator|.
name|getNestedPath
argument_list|()
operator|+
literal|"_some_suffix"
argument_list|)
expr_stmt|;
break|break;
block|}
return|return
name|result
return|;
block|}
DECL|method|testScriptSortType
specifier|public
name|void
name|testScriptSortType
parameter_list|()
block|{
comment|// we rely on these ordinals in serialization, so changing them breaks bwc.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|ScriptSortType
operator|.
name|STRING
operator|.
name|ordinal
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|ScriptSortType
operator|.
name|NUMBER
operator|.
name|ordinal
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"string"
argument_list|,
name|ScriptSortType
operator|.
name|STRING
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"number"
argument_list|,
name|ScriptSortType
operator|.
name|NUMBER
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|ScriptSortType
operator|.
name|STRING
argument_list|,
name|ScriptSortType
operator|.
name|fromString
argument_list|(
literal|"string"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|ScriptSortType
operator|.
name|STRING
argument_list|,
name|ScriptSortType
operator|.
name|fromString
argument_list|(
literal|"String"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|ScriptSortType
operator|.
name|STRING
argument_list|,
name|ScriptSortType
operator|.
name|fromString
argument_list|(
literal|"STRING"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|ScriptSortType
operator|.
name|NUMBER
argument_list|,
name|ScriptSortType
operator|.
name|fromString
argument_list|(
literal|"number"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|ScriptSortType
operator|.
name|NUMBER
argument_list|,
name|ScriptSortType
operator|.
name|fromString
argument_list|(
literal|"Number"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|ScriptSortType
operator|.
name|NUMBER
argument_list|,
name|ScriptSortType
operator|.
name|fromString
argument_list|(
literal|"NUMBER"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

