begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.ingest.processor.mutate
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|processor
operator|.
name|mutate
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|Data
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|processor
operator|.
name|Processor
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
name|Before
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
name|*
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|equalTo
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|is
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|nullValue
import|;
end_import

begin_class
DECL|class|MutateProcessorTests
specifier|public
class|class
name|MutateProcessorTests
extends|extends
name|ESTestCase
block|{
DECL|field|data
specifier|private
name|Data
name|data
decl_stmt|;
annotation|@
name|Before
DECL|method|setData
specifier|public
name|void
name|setData
parameter_list|()
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|document
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|document
operator|.
name|put
argument_list|(
literal|"foo"
argument_list|,
literal|"bar"
argument_list|)
expr_stmt|;
name|document
operator|.
name|put
argument_list|(
literal|"alpha"
argument_list|,
literal|"aBcD"
argument_list|)
expr_stmt|;
name|document
operator|.
name|put
argument_list|(
literal|"num"
argument_list|,
literal|"64"
argument_list|)
expr_stmt|;
name|document
operator|.
name|put
argument_list|(
literal|"to_strip"
argument_list|,
literal|" clean    "
argument_list|)
expr_stmt|;
name|document
operator|.
name|put
argument_list|(
literal|"arr"
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
literal|"1"
argument_list|,
literal|"2"
argument_list|,
literal|"3"
argument_list|)
argument_list|)
expr_stmt|;
name|document
operator|.
name|put
argument_list|(
literal|"ip"
argument_list|,
literal|"127.0.0.1"
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|fizz
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|fizz
operator|.
name|put
argument_list|(
literal|"buzz"
argument_list|,
literal|"hello world"
argument_list|)
expr_stmt|;
name|document
operator|.
name|put
argument_list|(
literal|"fizz"
argument_list|,
name|fizz
argument_list|)
expr_stmt|;
name|data
operator|=
operator|new
name|Data
argument_list|(
literal|"index"
argument_list|,
literal|"type"
argument_list|,
literal|"id"
argument_list|,
name|document
argument_list|)
expr_stmt|;
block|}
DECL|method|testUpdate
specifier|public
name|void
name|testUpdate
parameter_list|()
throws|throws
name|IOException
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|update
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|update
operator|.
name|put
argument_list|(
literal|"foo"
argument_list|,
literal|123
argument_list|)
expr_stmt|;
name|Processor
name|processor
init|=
operator|new
name|MutateProcessor
argument_list|(
name|update
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|processor
operator|.
name|execute
argument_list|(
name|data
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|data
operator|.
name|getDocument
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|7
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|data
operator|.
name|getProperty
argument_list|(
literal|"foo"
argument_list|,
name|Integer
operator|.
name|class
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|123
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testRename
specifier|public
name|void
name|testRename
parameter_list|()
throws|throws
name|IOException
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|rename
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|rename
operator|.
name|put
argument_list|(
literal|"foo"
argument_list|,
literal|"bar"
argument_list|)
expr_stmt|;
name|Processor
name|processor
init|=
operator|new
name|MutateProcessor
argument_list|(
literal|null
argument_list|,
name|rename
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|processor
operator|.
name|execute
argument_list|(
name|data
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|data
operator|.
name|getDocument
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|7
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|data
operator|.
name|getProperty
argument_list|(
literal|"bar"
argument_list|,
name|String
operator|.
name|class
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"bar"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|data
operator|.
name|containsProperty
argument_list|(
literal|"foo"
argument_list|)
argument_list|,
name|is
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testConvert
specifier|public
name|void
name|testConvert
parameter_list|()
throws|throws
name|IOException
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|convert
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|convert
operator|.
name|put
argument_list|(
literal|"num"
argument_list|,
literal|"integer"
argument_list|)
expr_stmt|;
name|Processor
name|processor
init|=
operator|new
name|MutateProcessor
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
name|convert
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|processor
operator|.
name|execute
argument_list|(
name|data
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|data
operator|.
name|getDocument
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|7
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|data
operator|.
name|getProperty
argument_list|(
literal|"num"
argument_list|,
name|Integer
operator|.
name|class
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|64
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testConvertNullField
specifier|public
name|void
name|testConvertNullField
parameter_list|()
throws|throws
name|IOException
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|convert
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|convert
operator|.
name|put
argument_list|(
literal|"null"
argument_list|,
literal|"integer"
argument_list|)
expr_stmt|;
name|Processor
name|processor
init|=
operator|new
name|MutateProcessor
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
name|convert
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
try|try
block|{
name|processor
operator|.
name|execute
argument_list|(
name|data
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"processor execute should have failed"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"Field \"null\" is null, cannot be converted to a/an integer"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testConvertList
specifier|public
name|void
name|testConvertList
parameter_list|()
throws|throws
name|IOException
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|convert
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|convert
operator|.
name|put
argument_list|(
literal|"arr"
argument_list|,
literal|"integer"
argument_list|)
expr_stmt|;
name|Processor
name|processor
init|=
operator|new
name|MutateProcessor
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
name|convert
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|processor
operator|.
name|execute
argument_list|(
name|data
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|data
operator|.
name|getDocument
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|7
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|data
operator|.
name|getProperty
argument_list|(
literal|"arr"
argument_list|,
name|List
operator|.
name|class
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|1
argument_list|,
literal|2
argument_list|,
literal|3
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testSplit
specifier|public
name|void
name|testSplit
parameter_list|()
throws|throws
name|IOException
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|split
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|split
operator|.
name|put
argument_list|(
literal|"ip"
argument_list|,
literal|"\\."
argument_list|)
expr_stmt|;
name|Processor
name|processor
init|=
operator|new
name|MutateProcessor
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|split
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|processor
operator|.
name|execute
argument_list|(
name|data
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|data
operator|.
name|getDocument
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|7
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|data
operator|.
name|getProperty
argument_list|(
literal|"ip"
argument_list|,
name|List
operator|.
name|class
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"127"
argument_list|,
literal|"0"
argument_list|,
literal|"0"
argument_list|,
literal|"1"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testSplitNullValue
specifier|public
name|void
name|testSplitNullValue
parameter_list|()
throws|throws
name|IOException
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|split
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|split
operator|.
name|put
argument_list|(
literal|"not.found"
argument_list|,
literal|"\\."
argument_list|)
expr_stmt|;
name|Processor
name|processor
init|=
operator|new
name|MutateProcessor
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|split
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
try|try
block|{
name|processor
operator|.
name|execute
argument_list|(
name|data
argument_list|)
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"Cannot split field. [not.found] is null."
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testGsub
specifier|public
name|void
name|testGsub
parameter_list|()
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|GsubExpression
argument_list|>
name|gsubExpressions
init|=
name|Collections
operator|.
name|singletonList
argument_list|(
operator|new
name|GsubExpression
argument_list|(
literal|"ip"
argument_list|,
name|Pattern
operator|.
name|compile
argument_list|(
literal|"\\."
argument_list|)
argument_list|,
literal|"-"
argument_list|)
argument_list|)
decl_stmt|;
name|Processor
name|processor
init|=
operator|new
name|MutateProcessor
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|gsubExpressions
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|processor
operator|.
name|execute
argument_list|(
name|data
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|data
operator|.
name|getDocument
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|7
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|data
operator|.
name|getProperty
argument_list|(
literal|"ip"
argument_list|,
name|String
operator|.
name|class
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"127-0-0-1"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testGsub_NullValue
specifier|public
name|void
name|testGsub_NullValue
parameter_list|()
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|GsubExpression
argument_list|>
name|gsubExpressions
init|=
name|Collections
operator|.
name|singletonList
argument_list|(
operator|new
name|GsubExpression
argument_list|(
literal|"null_field"
argument_list|,
name|Pattern
operator|.
name|compile
argument_list|(
literal|"\\."
argument_list|)
argument_list|,
literal|"-"
argument_list|)
argument_list|)
decl_stmt|;
name|Processor
name|processor
init|=
operator|new
name|MutateProcessor
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|gsubExpressions
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
try|try
block|{
name|processor
operator|.
name|execute
argument_list|(
name|data
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"processor execution should have failed"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"Field \"null_field\" is null, cannot match pattern."
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testJoin
specifier|public
name|void
name|testJoin
parameter_list|()
throws|throws
name|IOException
block|{
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|join
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|join
operator|.
name|put
argument_list|(
literal|"arr"
argument_list|,
literal|"-"
argument_list|)
expr_stmt|;
name|Processor
name|processor
init|=
operator|new
name|MutateProcessor
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|join
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|processor
operator|.
name|execute
argument_list|(
name|data
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|data
operator|.
name|getDocument
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|7
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|data
operator|.
name|getProperty
argument_list|(
literal|"arr"
argument_list|,
name|String
operator|.
name|class
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"1-2-3"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testRemove
specifier|public
name|void
name|testRemove
parameter_list|()
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|String
argument_list|>
name|remove
init|=
name|Arrays
operator|.
name|asList
argument_list|(
literal|"foo"
argument_list|,
literal|"ip"
argument_list|)
decl_stmt|;
name|Processor
name|processor
init|=
operator|new
name|MutateProcessor
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|remove
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|processor
operator|.
name|execute
argument_list|(
name|data
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|data
operator|.
name|getDocument
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|5
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|data
operator|.
name|getProperty
argument_list|(
literal|"foo"
argument_list|,
name|Object
operator|.
name|class
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|data
operator|.
name|getProperty
argument_list|(
literal|"ip"
argument_list|,
name|Object
operator|.
name|class
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testTrim
specifier|public
name|void
name|testTrim
parameter_list|()
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|String
argument_list|>
name|trim
init|=
name|Arrays
operator|.
name|asList
argument_list|(
literal|"to_strip"
argument_list|,
literal|"foo"
argument_list|)
decl_stmt|;
name|Processor
name|processor
init|=
operator|new
name|MutateProcessor
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|trim
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|processor
operator|.
name|execute
argument_list|(
name|data
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|data
operator|.
name|getDocument
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|7
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|data
operator|.
name|getProperty
argument_list|(
literal|"foo"
argument_list|,
name|String
operator|.
name|class
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"bar"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|data
operator|.
name|getProperty
argument_list|(
literal|"to_strip"
argument_list|,
name|String
operator|.
name|class
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"clean"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testTrimNullValue
specifier|public
name|void
name|testTrimNullValue
parameter_list|()
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|String
argument_list|>
name|trim
init|=
name|Collections
operator|.
name|singletonList
argument_list|(
literal|"not.found"
argument_list|)
decl_stmt|;
name|Processor
name|processor
init|=
operator|new
name|MutateProcessor
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|trim
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
try|try
block|{
name|processor
operator|.
name|execute
argument_list|(
name|data
argument_list|)
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"Cannot trim field. [not.found] is null."
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testUppercase
specifier|public
name|void
name|testUppercase
parameter_list|()
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|String
argument_list|>
name|uppercase
init|=
name|Collections
operator|.
name|singletonList
argument_list|(
literal|"foo"
argument_list|)
decl_stmt|;
name|Processor
name|processor
init|=
operator|new
name|MutateProcessor
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|uppercase
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|processor
operator|.
name|execute
argument_list|(
name|data
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|data
operator|.
name|getDocument
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|7
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|data
operator|.
name|getProperty
argument_list|(
literal|"foo"
argument_list|,
name|String
operator|.
name|class
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"BAR"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testUppercaseNullValue
specifier|public
name|void
name|testUppercaseNullValue
parameter_list|()
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|String
argument_list|>
name|uppercase
init|=
name|Collections
operator|.
name|singletonList
argument_list|(
literal|"not.found"
argument_list|)
decl_stmt|;
name|Processor
name|processor
init|=
operator|new
name|MutateProcessor
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|uppercase
argument_list|,
literal|null
argument_list|)
decl_stmt|;
try|try
block|{
name|processor
operator|.
name|execute
argument_list|(
name|data
argument_list|)
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"Cannot uppercase field. [not.found] is null."
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testLowercase
specifier|public
name|void
name|testLowercase
parameter_list|()
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|String
argument_list|>
name|lowercase
init|=
name|Collections
operator|.
name|singletonList
argument_list|(
literal|"alpha"
argument_list|)
decl_stmt|;
name|Processor
name|processor
init|=
operator|new
name|MutateProcessor
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|lowercase
argument_list|)
decl_stmt|;
name|processor
operator|.
name|execute
argument_list|(
name|data
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|data
operator|.
name|getDocument
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|7
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|data
operator|.
name|getProperty
argument_list|(
literal|"alpha"
argument_list|,
name|String
operator|.
name|class
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"abcd"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testLowercaseNullValue
specifier|public
name|void
name|testLowercaseNullValue
parameter_list|()
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|String
argument_list|>
name|lowercase
init|=
name|Collections
operator|.
name|singletonList
argument_list|(
literal|"not.found"
argument_list|)
decl_stmt|;
name|Processor
name|processor
init|=
operator|new
name|MutateProcessor
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|lowercase
argument_list|)
decl_stmt|;
try|try
block|{
name|processor
operator|.
name|execute
argument_list|(
name|data
argument_list|)
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"Cannot lowercase field. [not.found] is null."
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

