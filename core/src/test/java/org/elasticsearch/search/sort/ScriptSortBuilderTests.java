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
name|apache
operator|.
name|lucene
operator|.
name|search
operator|.
name|SortField
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
name|ParseFieldMatcher
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
name|ParsingException
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
name|xcontent
operator|.
name|XContentFactory
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
name|xcontent
operator|.
name|XContentParser
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|QueryBuilder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|QueryParseContext
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
name|ScriptService
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
name|sort
operator|.
name|ScriptSortBuilder
operator|.
name|ScriptSortType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|ESTestCase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Rule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|rules
operator|.
name|ExpectedException
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
name|HashSet
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
return|return
name|randomScriptSortBuilder
argument_list|()
return|;
block|}
DECL|method|randomScriptSortBuilder
specifier|public
specifier|static
name|ScriptSortBuilder
name|randomScriptSortBuilder
parameter_list|()
block|{
name|ScriptSortType
name|type
init|=
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
decl_stmt|;
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
name|type
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
name|randomFrom
argument_list|(
name|SortOrder
operator|.
name|values
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
if|if
condition|(
name|type
operator|==
name|ScriptSortType
operator|.
name|NUMBER
condition|)
block|{
name|builder
operator|.
name|sortMode
argument_list|(
name|ESTestCase
operator|.
name|randomValueOtherThan
argument_list|(
name|builder
operator|.
name|sortMode
argument_list|()
argument_list|,
parameter_list|()
lambda|->
name|randomFrom
argument_list|(
name|SortMode
operator|.
name|values
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|Set
argument_list|<
name|SortMode
argument_list|>
name|exceptThis
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
name|exceptThis
operator|.
name|add
argument_list|(
name|SortMode
operator|.
name|SUM
argument_list|)
expr_stmt|;
name|exceptThis
operator|.
name|add
argument_list|(
name|SortMode
operator|.
name|AVG
argument_list|)
expr_stmt|;
name|exceptThis
operator|.
name|add
argument_list|(
name|SortMode
operator|.
name|MEDIAN
argument_list|)
expr_stmt|;
name|builder
operator|.
name|sortMode
argument_list|(
name|ESTestCase
operator|.
name|randomValueOtherThanMany
argument_list|(
name|exceptThis
operator|::
name|contains
argument_list|,
parameter_list|()
lambda|->
name|randomFrom
argument_list|(
name|SortMode
operator|.
name|values
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
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
name|NestedQueryBuilderGenerator
operator|.
name|randomNestedFilter
argument_list|()
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
name|ESTestCase
operator|.
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
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
if|if
condition|(
name|original
operator|.
name|sortMode
argument_list|()
operator|!=
literal|null
operator|&&
name|result
operator|.
name|type
argument_list|()
operator|==
name|ScriptSortType
operator|.
name|NUMBER
condition|)
block|{
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
block|}
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
if|if
condition|(
name|original
operator|.
name|type
argument_list|()
operator|==
name|ScriptSortType
operator|.
name|NUMBER
condition|)
block|{
name|result
operator|.
name|sortMode
argument_list|(
name|ESTestCase
operator|.
name|randomValueOtherThan
argument_list|(
name|result
operator|.
name|sortMode
argument_list|()
argument_list|,
parameter_list|()
lambda|->
name|randomFrom
argument_list|(
name|SortMode
operator|.
name|values
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// script sort type String only allows MIN and MAX, so we only switch
if|if
condition|(
name|original
operator|.
name|sortMode
argument_list|()
operator|==
name|SortMode
operator|.
name|MIN
condition|)
block|{
name|result
operator|.
name|sortMode
argument_list|(
name|SortMode
operator|.
name|MAX
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|result
operator|.
name|sortMode
argument_list|(
name|SortMode
operator|.
name|MIN
argument_list|)
expr_stmt|;
block|}
block|}
break|break;
case|case
literal|2
case|:
name|result
operator|.
name|setNestedFilter
argument_list|(
name|ESTestCase
operator|.
name|randomValueOtherThan
argument_list|(
name|original
operator|.
name|getNestedFilter
argument_list|()
argument_list|,
parameter_list|()
lambda|->
name|NestedQueryBuilderGenerator
operator|.
name|randomNestedFilter
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
annotation|@
name|Override
DECL|method|sortFieldAssertions
specifier|protected
name|void
name|sortFieldAssertions
parameter_list|(
name|ScriptSortBuilder
name|builder
parameter_list|,
name|SortField
name|sortField
parameter_list|)
throws|throws
name|IOException
block|{
name|assertEquals
argument_list|(
name|SortField
operator|.
name|Type
operator|.
name|CUSTOM
argument_list|,
name|sortField
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|builder
operator|.
name|order
argument_list|()
operator|==
name|SortOrder
operator|.
name|ASC
condition|?
literal|false
else|:
literal|true
argument_list|,
name|sortField
operator|.
name|getReverse
argument_list|()
argument_list|)
expr_stmt|;
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
annotation|@
name|Rule
DECL|field|exceptionRule
specifier|public
name|ExpectedException
name|exceptionRule
init|=
name|ExpectedException
operator|.
name|none
argument_list|()
decl_stmt|;
DECL|method|testScriptSortTypeNull
specifier|public
name|void
name|testScriptSortTypeNull
parameter_list|()
block|{
name|exceptionRule
operator|.
name|expect
argument_list|(
name|NullPointerException
operator|.
name|class
argument_list|)
expr_stmt|;
name|exceptionRule
operator|.
name|expectMessage
argument_list|(
literal|"input string is null"
argument_list|)
expr_stmt|;
name|ScriptSortType
operator|.
name|fromString
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
DECL|method|testScriptSortTypeIllegalArgument
specifier|public
name|void
name|testScriptSortTypeIllegalArgument
parameter_list|()
block|{
name|exceptionRule
operator|.
name|expect
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|)
expr_stmt|;
name|exceptionRule
operator|.
name|expectMessage
argument_list|(
literal|"Unknown ScriptSortType [xyz]"
argument_list|)
expr_stmt|;
name|ScriptSortType
operator|.
name|fromString
argument_list|(
literal|"xyz"
argument_list|)
expr_stmt|;
block|}
DECL|method|testParseJson
specifier|public
name|void
name|testParseJson
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|scriptSort
init|=
literal|"{\n"
operator|+
literal|"\"_script\" : {\n"
operator|+
literal|"\"type\" : \"number\",\n"
operator|+
literal|"\"script\" : {\n"
operator|+
literal|"\"inline\": \"doc['field_name'].value * factor\",\n"
operator|+
literal|"\"params\" : {\n"
operator|+
literal|"\"factor\" : 1.1\n"
operator|+
literal|"}\n"
operator|+
literal|"},\n"
operator|+
literal|"\"mode\" : \"max\",\n"
operator|+
literal|"\"order\" : \"asc\"\n"
operator|+
literal|"} }\n"
decl_stmt|;
name|XContentParser
name|parser
init|=
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|scriptSort
argument_list|)
operator|.
name|createParser
argument_list|(
name|scriptSort
argument_list|)
decl_stmt|;
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
name|QueryParseContext
name|context
init|=
operator|new
name|QueryParseContext
argument_list|(
name|indicesQueriesRegistry
argument_list|,
name|parser
argument_list|,
name|ParseFieldMatcher
operator|.
name|STRICT
argument_list|)
decl_stmt|;
name|ScriptSortBuilder
name|builder
init|=
name|ScriptSortBuilder
operator|.
name|fromXContent
argument_list|(
name|context
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"doc['field_name'].value * factor"
argument_list|,
name|builder
operator|.
name|script
argument_list|()
operator|.
name|getScript
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|builder
operator|.
name|script
argument_list|()
operator|.
name|getLang
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1.1
argument_list|,
name|builder
operator|.
name|script
argument_list|()
operator|.
name|getParams
argument_list|()
operator|.
name|get
argument_list|(
literal|"factor"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|ScriptType
operator|.
name|INLINE
argument_list|,
name|builder
operator|.
name|script
argument_list|()
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|ScriptSortType
operator|.
name|NUMBER
argument_list|,
name|builder
operator|.
name|type
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|SortOrder
operator|.
name|ASC
argument_list|,
name|builder
operator|.
name|order
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|SortMode
operator|.
name|MAX
argument_list|,
name|builder
operator|.
name|sortMode
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|builder
operator|.
name|getNestedFilter
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|builder
operator|.
name|getNestedPath
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testParseJsonOldStyle
specifier|public
name|void
name|testParseJsonOldStyle
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|scriptSort
init|=
literal|"{\n"
operator|+
literal|"\"_script\" : {\n"
operator|+
literal|"\"type\" : \"number\",\n"
operator|+
literal|"\"script\" : \"doc['field_name'].value * factor\",\n"
operator|+
literal|"\"params\" : {\n"
operator|+
literal|"\"factor\" : 1.1\n"
operator|+
literal|"},\n"
operator|+
literal|"\"mode\" : \"max\",\n"
operator|+
literal|"\"order\" : \"asc\"\n"
operator|+
literal|"} }\n"
decl_stmt|;
name|XContentParser
name|parser
init|=
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|scriptSort
argument_list|)
operator|.
name|createParser
argument_list|(
name|scriptSort
argument_list|)
decl_stmt|;
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
name|QueryParseContext
name|context
init|=
operator|new
name|QueryParseContext
argument_list|(
name|indicesQueriesRegistry
argument_list|,
name|parser
argument_list|,
name|ParseFieldMatcher
operator|.
name|STRICT
argument_list|)
decl_stmt|;
name|ScriptSortBuilder
name|builder
init|=
name|ScriptSortBuilder
operator|.
name|fromXContent
argument_list|(
name|context
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"doc['field_name'].value * factor"
argument_list|,
name|builder
operator|.
name|script
argument_list|()
operator|.
name|getScript
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|builder
operator|.
name|script
argument_list|()
operator|.
name|getLang
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1.1
argument_list|,
name|builder
operator|.
name|script
argument_list|()
operator|.
name|getParams
argument_list|()
operator|.
name|get
argument_list|(
literal|"factor"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|ScriptType
operator|.
name|INLINE
argument_list|,
name|builder
operator|.
name|script
argument_list|()
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|ScriptSortType
operator|.
name|NUMBER
argument_list|,
name|builder
operator|.
name|type
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|SortOrder
operator|.
name|ASC
argument_list|,
name|builder
operator|.
name|order
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|SortMode
operator|.
name|MAX
argument_list|,
name|builder
operator|.
name|sortMode
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|builder
operator|.
name|getNestedFilter
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|builder
operator|.
name|getNestedPath
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testParseBadFieldNameExceptions
specifier|public
name|void
name|testParseBadFieldNameExceptions
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|scriptSort
init|=
literal|"{\"_script\" : {"
operator|+
literal|"\"bad_field\" : \"number\""
operator|+
literal|"} }"
decl_stmt|;
name|XContentParser
name|parser
init|=
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|scriptSort
argument_list|)
operator|.
name|createParser
argument_list|(
name|scriptSort
argument_list|)
decl_stmt|;
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
name|QueryParseContext
name|context
init|=
operator|new
name|QueryParseContext
argument_list|(
name|indicesQueriesRegistry
argument_list|,
name|parser
argument_list|,
name|ParseFieldMatcher
operator|.
name|STRICT
argument_list|)
decl_stmt|;
name|exceptionRule
operator|.
name|expect
argument_list|(
name|ParsingException
operator|.
name|class
argument_list|)
expr_stmt|;
name|exceptionRule
operator|.
name|expectMessage
argument_list|(
literal|"failed to parse field [bad_field]"
argument_list|)
expr_stmt|;
name|ScriptSortBuilder
operator|.
name|fromXContent
argument_list|(
name|context
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
DECL|method|testParseBadFieldNameExceptionsOnStartObject
specifier|public
name|void
name|testParseBadFieldNameExceptionsOnStartObject
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|scriptSort
init|=
literal|"{\"_script\" : {"
operator|+
literal|"\"bad_field\" : { \"order\" : \"asc\" } } }"
decl_stmt|;
name|XContentParser
name|parser
init|=
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|scriptSort
argument_list|)
operator|.
name|createParser
argument_list|(
name|scriptSort
argument_list|)
decl_stmt|;
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
name|QueryParseContext
name|context
init|=
operator|new
name|QueryParseContext
argument_list|(
name|indicesQueriesRegistry
argument_list|,
name|parser
argument_list|,
name|ParseFieldMatcher
operator|.
name|STRICT
argument_list|)
decl_stmt|;
name|exceptionRule
operator|.
name|expect
argument_list|(
name|ParsingException
operator|.
name|class
argument_list|)
expr_stmt|;
name|exceptionRule
operator|.
name|expectMessage
argument_list|(
literal|"failed to parse field [bad_field]"
argument_list|)
expr_stmt|;
name|ScriptSortBuilder
operator|.
name|fromXContent
argument_list|(
name|context
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
DECL|method|testParseUnexpectedToken
specifier|public
name|void
name|testParseUnexpectedToken
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|scriptSort
init|=
literal|"{\"_script\" : {"
operator|+
literal|"\"script\" : [ \"order\" : \"asc\" ] } }"
decl_stmt|;
name|XContentParser
name|parser
init|=
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|scriptSort
argument_list|)
operator|.
name|createParser
argument_list|(
name|scriptSort
argument_list|)
decl_stmt|;
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
name|QueryParseContext
name|context
init|=
operator|new
name|QueryParseContext
argument_list|(
name|indicesQueriesRegistry
argument_list|,
name|parser
argument_list|,
name|ParseFieldMatcher
operator|.
name|STRICT
argument_list|)
decl_stmt|;
name|exceptionRule
operator|.
name|expect
argument_list|(
name|ParsingException
operator|.
name|class
argument_list|)
expr_stmt|;
name|exceptionRule
operator|.
name|expectMessage
argument_list|(
literal|"unexpected token [START_ARRAY]"
argument_list|)
expr_stmt|;
name|ScriptSortBuilder
operator|.
name|fromXContent
argument_list|(
name|context
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**      * script sort of type {@link ScriptSortType} does not work with {@link SortMode#AVG}, {@link SortMode#MEDIAN} or {@link SortMode#SUM}      */
DECL|method|testBadSortMode
specifier|public
name|void
name|testBadSortMode
parameter_list|()
throws|throws
name|IOException
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
literal|"something"
argument_list|)
argument_list|,
name|ScriptSortType
operator|.
name|STRING
argument_list|)
decl_stmt|;
name|exceptionRule
operator|.
name|expect
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|)
expr_stmt|;
name|exceptionRule
operator|.
name|expectMessage
argument_list|(
literal|"script sort of type [string] doesn't support mode"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|sortMode
argument_list|(
name|SortMode
operator|.
name|fromString
argument_list|(
name|randomFrom
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"avg"
block|,
literal|"median"
block|,
literal|"sum"
block|}
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|fromXContent
specifier|protected
name|ScriptSortBuilder
name|fromXContent
parameter_list|(
name|QueryParseContext
name|context
parameter_list|,
name|String
name|fieldName
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|ScriptSortBuilder
operator|.
name|fromXContent
argument_list|(
name|context
argument_list|,
name|fieldName
argument_list|)
return|;
block|}
block|}
end_class

end_unit

