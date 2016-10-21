begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
package|;
end_package

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
name|Map
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
name|instanceOf
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

begin_class
DECL|class|TableTests
specifier|public
class|class
name|TableTests
extends|extends
name|ESTestCase
block|{
DECL|method|testFailOnStartRowWithoutHeader
specifier|public
name|void
name|testFailOnStartRowWithoutHeader
parameter_list|()
block|{
name|Table
name|table
init|=
operator|new
name|Table
argument_list|()
decl_stmt|;
name|Exception
name|e
init|=
name|expectThrows
argument_list|(
name|IllegalStateException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|table
operator|.
name|startRow
argument_list|()
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"no headers added..."
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testFailOnEndHeadersWithoutStart
specifier|public
name|void
name|testFailOnEndHeadersWithoutStart
parameter_list|()
block|{
name|Table
name|table
init|=
operator|new
name|Table
argument_list|()
decl_stmt|;
name|Exception
name|e
init|=
name|expectThrows
argument_list|(
name|IllegalStateException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|table
operator|.
name|endHeaders
argument_list|()
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"no headers added..."
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testFailOnAddCellWithoutHeader
specifier|public
name|void
name|testFailOnAddCellWithoutHeader
parameter_list|()
block|{
name|Table
name|table
init|=
operator|new
name|Table
argument_list|()
decl_stmt|;
name|Exception
name|e
init|=
name|expectThrows
argument_list|(
name|IllegalStateException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|table
operator|.
name|addCell
argument_list|(
literal|"error"
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"no block started..."
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testFailOnAddCellWithoutRow
specifier|public
name|void
name|testFailOnAddCellWithoutRow
parameter_list|()
block|{
name|Table
name|table
init|=
name|this
operator|.
name|getTableWithHeaders
argument_list|()
decl_stmt|;
name|Exception
name|e
init|=
name|expectThrows
argument_list|(
name|IllegalStateException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|table
operator|.
name|addCell
argument_list|(
literal|"error"
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"no block started..."
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testFailOnEndRowWithoutStart
specifier|public
name|void
name|testFailOnEndRowWithoutStart
parameter_list|()
block|{
name|Table
name|table
init|=
name|this
operator|.
name|getTableWithHeaders
argument_list|()
decl_stmt|;
name|Exception
name|e
init|=
name|expectThrows
argument_list|(
name|IllegalStateException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|table
operator|.
name|endRow
argument_list|()
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"no row started..."
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testFailOnLessCellsThanDeclared
specifier|public
name|void
name|testFailOnLessCellsThanDeclared
parameter_list|()
block|{
name|Table
name|table
init|=
name|this
operator|.
name|getTableWithHeaders
argument_list|()
decl_stmt|;
name|table
operator|.
name|startRow
argument_list|()
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
literal|"foo"
argument_list|)
expr_stmt|;
name|Exception
name|e
init|=
name|expectThrows
argument_list|(
name|IllegalStateException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|table
operator|.
name|endRow
argument_list|()
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"mismatch on number of cells 1 in a row compared to header 2"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testOnLessCellsThanDeclaredUnchecked
specifier|public
name|void
name|testOnLessCellsThanDeclaredUnchecked
parameter_list|()
block|{
name|Table
name|table
init|=
name|this
operator|.
name|getTableWithHeaders
argument_list|()
decl_stmt|;
name|table
operator|.
name|startRow
argument_list|()
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
literal|"foo"
argument_list|)
expr_stmt|;
name|table
operator|.
name|endRow
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
DECL|method|testFailOnMoreCellsThanDeclared
specifier|public
name|void
name|testFailOnMoreCellsThanDeclared
parameter_list|()
block|{
name|Table
name|table
init|=
name|this
operator|.
name|getTableWithHeaders
argument_list|()
decl_stmt|;
name|table
operator|.
name|startRow
argument_list|()
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
literal|"foo"
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
literal|"bar"
argument_list|)
expr_stmt|;
name|Exception
name|e
init|=
name|expectThrows
argument_list|(
name|IllegalStateException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|table
operator|.
name|addCell
argument_list|(
literal|"foobar"
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"can't add more cells to a row than the header"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testSimple
specifier|public
name|void
name|testSimple
parameter_list|()
block|{
name|Table
name|table
init|=
name|this
operator|.
name|getTableWithHeaders
argument_list|()
decl_stmt|;
name|table
operator|.
name|startRow
argument_list|()
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
literal|"foo1"
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
literal|"bar1"
argument_list|)
expr_stmt|;
name|table
operator|.
name|endRow
argument_list|()
expr_stmt|;
name|table
operator|.
name|startRow
argument_list|()
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
literal|"foo2"
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
literal|"bar2"
argument_list|)
expr_stmt|;
name|table
operator|.
name|endRow
argument_list|()
expr_stmt|;
comment|// Check headers
name|List
argument_list|<
name|Table
operator|.
name|Cell
argument_list|>
name|headers
init|=
name|table
operator|.
name|getHeaders
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|headers
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"foo"
argument_list|,
name|headers
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|value
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|headers
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|attr
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"f"
argument_list|,
name|headers
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|attr
operator|.
name|get
argument_list|(
literal|"alias"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"foo"
argument_list|,
name|headers
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|attr
operator|.
name|get
argument_list|(
literal|"desc"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"bar"
argument_list|,
name|headers
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|value
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|headers
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|attr
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"b"
argument_list|,
name|headers
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|attr
operator|.
name|get
argument_list|(
literal|"alias"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"bar"
argument_list|,
name|headers
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|attr
operator|.
name|get
argument_list|(
literal|"desc"
argument_list|)
argument_list|)
expr_stmt|;
comment|// Check rows
name|List
argument_list|<
name|List
argument_list|<
name|Table
operator|.
name|Cell
argument_list|>
argument_list|>
name|rows
init|=
name|table
operator|.
name|getRows
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|rows
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Table
operator|.
name|Cell
argument_list|>
name|row
init|=
name|rows
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"foo1"
argument_list|,
name|row
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|value
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"bar1"
argument_list|,
name|row
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|value
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|row
operator|=
name|rows
operator|.
name|get
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"foo2"
argument_list|,
name|row
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|value
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"bar2"
argument_list|,
name|row
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|value
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
comment|// Check getAsMap
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|Table
operator|.
name|Cell
argument_list|>
argument_list|>
name|map
init|=
name|table
operator|.
name|getAsMap
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|map
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|row
operator|=
name|map
operator|.
name|get
argument_list|(
literal|"foo"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"foo1"
argument_list|,
name|row
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|value
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"foo2"
argument_list|,
name|row
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|value
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|row
operator|=
name|map
operator|.
name|get
argument_list|(
literal|"bar"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"bar1"
argument_list|,
name|row
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|value
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"bar2"
argument_list|,
name|row
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|value
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
comment|// Check getHeaderMap
name|Map
argument_list|<
name|String
argument_list|,
name|Table
operator|.
name|Cell
argument_list|>
name|headerMap
init|=
name|table
operator|.
name|getHeaderMap
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|headerMap
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Table
operator|.
name|Cell
name|cell
init|=
name|headerMap
operator|.
name|get
argument_list|(
literal|"foo"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"foo"
argument_list|,
name|cell
operator|.
name|value
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|cell
operator|=
name|headerMap
operator|.
name|get
argument_list|(
literal|"bar"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"bar"
argument_list|,
name|cell
operator|.
name|value
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
comment|// Check findHeaderByName
name|cell
operator|=
name|table
operator|.
name|findHeaderByName
argument_list|(
literal|"foo"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"foo"
argument_list|,
name|cell
operator|.
name|value
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|cell
operator|=
name|table
operator|.
name|findHeaderByName
argument_list|(
literal|"missing"
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|cell
argument_list|)
expr_stmt|;
block|}
DECL|method|testWithTimestamp
specifier|public
name|void
name|testWithTimestamp
parameter_list|()
block|{
name|Table
name|table
init|=
operator|new
name|Table
argument_list|()
decl_stmt|;
name|table
operator|.
name|startHeadersWithTimestamp
argument_list|()
expr_stmt|;
name|table
operator|.
name|endHeaders
argument_list|()
expr_stmt|;
name|List
argument_list|<
name|Table
operator|.
name|Cell
argument_list|>
name|headers
init|=
name|table
operator|.
name|getHeaders
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|headers
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Table
operator|.
name|EPOCH
argument_list|,
name|headers
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|value
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Table
operator|.
name|TIMESTAMP
argument_list|,
name|headers
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|value
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|headers
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|attr
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"t,time"
argument_list|,
name|headers
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|attr
operator|.
name|get
argument_list|(
literal|"alias"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"seconds since 1970-01-01 00:00:00"
argument_list|,
name|headers
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|attr
operator|.
name|get
argument_list|(
literal|"desc"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|headers
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|attr
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"ts,hms,hhmmss"
argument_list|,
name|headers
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|attr
operator|.
name|get
argument_list|(
literal|"alias"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"time in HH:MM:SS"
argument_list|,
name|headers
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|attr
operator|.
name|get
argument_list|(
literal|"desc"
argument_list|)
argument_list|)
expr_stmt|;
comment|// check row's timestamp
name|table
operator|.
name|startRow
argument_list|()
expr_stmt|;
name|table
operator|.
name|endRow
argument_list|()
expr_stmt|;
name|List
argument_list|<
name|List
argument_list|<
name|Table
operator|.
name|Cell
argument_list|>
argument_list|>
name|rows
init|=
name|table
operator|.
name|getRows
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|rows
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|rows
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|rows
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|value
argument_list|,
name|instanceOf
argument_list|(
name|Long
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testAliasMap
specifier|public
name|void
name|testAliasMap
parameter_list|()
block|{
name|Table
name|table
init|=
operator|new
name|Table
argument_list|()
decl_stmt|;
name|table
operator|.
name|startHeaders
argument_list|()
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
literal|"asdf"
argument_list|,
literal|"alias:a"
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
literal|"ghij"
argument_list|,
literal|"alias:g,h"
argument_list|)
expr_stmt|;
name|table
operator|.
name|endHeaders
argument_list|()
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|aliasMap
init|=
name|table
operator|.
name|getAliasMap
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|5
argument_list|,
name|aliasMap
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"asdf"
argument_list|,
name|aliasMap
operator|.
name|get
argument_list|(
literal|"a"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"ghij"
argument_list|,
name|aliasMap
operator|.
name|get
argument_list|(
literal|"g"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"ghij"
argument_list|,
name|aliasMap
operator|.
name|get
argument_list|(
literal|"h"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|getTableWithHeaders
specifier|private
name|Table
name|getTableWithHeaders
parameter_list|()
block|{
name|Table
name|table
init|=
operator|new
name|Table
argument_list|()
decl_stmt|;
name|table
operator|.
name|startHeaders
argument_list|()
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
literal|"foo"
argument_list|,
literal|"alias:f;desc:foo"
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
literal|"bar"
argument_list|,
literal|"alias:b;desc:bar"
argument_list|)
expr_stmt|;
name|table
operator|.
name|endHeaders
argument_list|()
expr_stmt|;
return|return
name|table
return|;
block|}
block|}
end_class

end_unit

