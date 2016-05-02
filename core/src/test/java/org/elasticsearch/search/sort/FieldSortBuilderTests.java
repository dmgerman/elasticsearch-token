begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/* x * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|QueryParseContext
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
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|function
operator|.
name|Supplier
import|;
end_import

begin_class
DECL|class|FieldSortBuilderTests
specifier|public
class|class
name|FieldSortBuilderTests
extends|extends
name|AbstractSortTestCase
argument_list|<
name|FieldSortBuilder
argument_list|>
block|{
annotation|@
name|Override
DECL|method|createTestItem
specifier|protected
name|FieldSortBuilder
name|createTestItem
parameter_list|()
block|{
return|return
name|randomFieldSortBuilder
argument_list|()
return|;
block|}
DECL|field|missingContent
specifier|private
name|List
argument_list|<
name|Object
argument_list|>
name|missingContent
init|=
name|Arrays
operator|.
name|asList
argument_list|(
literal|"_last"
argument_list|,
literal|"_first"
argument_list|,
name|ESTestCase
operator|.
name|randomAsciiOfLength
argument_list|(
literal|10
argument_list|)
argument_list|,
name|ESTestCase
operator|.
name|randomUnicodeOfCodepointLengthBetween
argument_list|(
literal|5
argument_list|,
literal|15
argument_list|)
argument_list|,
name|ESTestCase
operator|.
name|randomInt
argument_list|()
argument_list|)
decl_stmt|;
DECL|method|randomFieldSortBuilder
specifier|public
name|FieldSortBuilder
name|randomFieldSortBuilder
parameter_list|()
block|{
name|String
name|fieldName
init|=
name|rarely
argument_list|()
condition|?
name|FieldSortBuilder
operator|.
name|DOC_FIELD_NAME
else|:
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|FieldSortBuilder
name|builder
init|=
operator|new
name|FieldSortBuilder
argument_list|(
name|fieldName
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
name|builder
operator|.
name|missing
argument_list|(
name|randomFrom
argument_list|(
name|missingContent
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
name|unmappedType
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
name|randomFrom
argument_list|(
name|SortMode
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
name|FieldSortBuilder
name|mutate
parameter_list|(
name|FieldSortBuilder
name|original
parameter_list|)
throws|throws
name|IOException
block|{
name|FieldSortBuilder
name|mutated
init|=
operator|new
name|FieldSortBuilder
argument_list|(
name|original
argument_list|)
decl_stmt|;
name|int
name|parameter
init|=
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|5
argument_list|)
decl_stmt|;
switch|switch
condition|(
name|parameter
condition|)
block|{
case|case
literal|0
case|:
name|mutated
operator|.
name|setNestedPath
argument_list|(
name|ESTestCase
operator|.
name|randomValueOtherThan
argument_list|(
name|original
operator|.
name|getNestedPath
argument_list|()
argument_list|,
parameter_list|()
lambda|->
name|ESTestCase
operator|.
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
literal|1
case|:
name|mutated
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
literal|2
case|:
name|mutated
operator|.
name|sortMode
argument_list|(
name|ESTestCase
operator|.
name|randomValueOtherThan
argument_list|(
name|original
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
break|break;
case|case
literal|3
case|:
name|mutated
operator|.
name|unmappedType
argument_list|(
name|ESTestCase
operator|.
name|randomValueOtherThan
argument_list|(
name|original
operator|.
name|unmappedType
argument_list|()
argument_list|,
parameter_list|()
lambda|->
name|ESTestCase
operator|.
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
literal|4
case|:
name|mutated
operator|.
name|missing
argument_list|(
name|ESTestCase
operator|.
name|randomValueOtherThan
argument_list|(
name|original
operator|.
name|missing
argument_list|()
argument_list|,
parameter_list|()
lambda|->
name|randomFrom
argument_list|(
name|missingContent
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
literal|5
case|:
name|mutated
operator|.
name|order
argument_list|(
name|ESTestCase
operator|.
name|randomValueOtherThan
argument_list|(
name|original
operator|.
name|order
argument_list|()
argument_list|,
parameter_list|()
lambda|->
name|randomFrom
argument_list|(
name|SortOrder
operator|.
name|values
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Unsupported mutation."
argument_list|)
throw|;
block|}
return|return
name|mutated
return|;
block|}
annotation|@
name|Override
DECL|method|sortFieldAssertions
specifier|protected
name|void
name|sortFieldAssertions
parameter_list|(
name|FieldSortBuilder
name|builder
parameter_list|,
name|SortField
name|sortField
parameter_list|)
throws|throws
name|IOException
block|{
name|SortField
operator|.
name|Type
name|expectedType
decl_stmt|;
if|if
condition|(
name|builder
operator|.
name|getFieldName
argument_list|()
operator|.
name|equals
argument_list|(
name|FieldSortBuilder
operator|.
name|DOC_FIELD_NAME
argument_list|)
condition|)
block|{
name|expectedType
operator|=
name|SortField
operator|.
name|Type
operator|.
name|DOC
expr_stmt|;
block|}
else|else
block|{
name|expectedType
operator|=
name|SortField
operator|.
name|Type
operator|.
name|CUSTOM
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|expectedType
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
if|if
condition|(
name|expectedType
operator|==
name|SortField
operator|.
name|Type
operator|.
name|CUSTOM
condition|)
block|{
name|assertEquals
argument_list|(
name|builder
operator|.
name|getFieldName
argument_list|()
argument_list|,
name|sortField
operator|.
name|getField
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testReverseOptionFails
specifier|public
name|void
name|testReverseOptionFails
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|json
init|=
literal|"{ \"post_date\" : {\"reverse\" : true} },\n"
decl_stmt|;
name|XContentParser
name|parser
init|=
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|json
argument_list|)
operator|.
name|createParser
argument_list|(
name|json
argument_list|)
decl_stmt|;
comment|// need to skip until parser is located on second START_OBJECT
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
try|try
block|{
name|FieldSortBuilder
operator|.
name|fromXContent
argument_list|(
name|context
argument_list|,
literal|""
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"adding reverse sorting option should fail with an exception"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ParsingException
name|e
parameter_list|)
block|{
comment|// all good
block|}
block|}
annotation|@
name|Override
DECL|method|fromXContent
specifier|protected
name|FieldSortBuilder
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
name|FieldSortBuilder
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

