begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.xcontent.support.filtering
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|xcontent
operator|.
name|support
operator|.
name|filtering
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
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|*
import|;
end_import

begin_class
DECL|class|FilterPathTests
specifier|public
class|class
name|FilterPathTests
extends|extends
name|ESTestCase
block|{
DECL|method|testSimpleFilterPath
specifier|public
name|void
name|testSimpleFilterPath
parameter_list|()
block|{
specifier|final
name|String
name|input
init|=
literal|"test"
decl_stmt|;
name|FilterPath
index|[]
name|filterPaths
init|=
name|FilterPath
operator|.
name|compile
argument_list|(
name|input
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|filterPaths
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPaths
argument_list|,
name|arrayWithSize
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|FilterPath
name|filterPath
init|=
name|filterPaths
index|[
literal|0
index|]
decl_stmt|;
name|assertNotNull
argument_list|(
name|filterPath
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPath
operator|.
name|matches
argument_list|()
argument_list|,
name|is
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPath
operator|.
name|getSegment
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"test"
argument_list|)
argument_list|)
expr_stmt|;
name|FilterPath
name|next
init|=
name|filterPath
operator|.
name|getNext
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
name|next
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|next
operator|.
name|matches
argument_list|()
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|next
operator|.
name|getSegment
argument_list|()
argument_list|,
name|isEmptyString
argument_list|()
argument_list|)
expr_stmt|;
name|assertSame
argument_list|(
name|next
argument_list|,
name|FilterPath
operator|.
name|EMPTY
argument_list|)
expr_stmt|;
block|}
DECL|method|testFilterPathWithSubField
specifier|public
name|void
name|testFilterPathWithSubField
parameter_list|()
block|{
specifier|final
name|String
name|input
init|=
literal|"foo.bar"
decl_stmt|;
name|FilterPath
index|[]
name|filterPaths
init|=
name|FilterPath
operator|.
name|compile
argument_list|(
name|input
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|filterPaths
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPaths
argument_list|,
name|arrayWithSize
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|FilterPath
name|filterPath
init|=
name|filterPaths
index|[
literal|0
index|]
decl_stmt|;
name|assertNotNull
argument_list|(
name|filterPath
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPath
operator|.
name|matches
argument_list|()
argument_list|,
name|is
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPath
operator|.
name|getSegment
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
name|filterPath
operator|=
name|filterPath
operator|.
name|getNext
argument_list|()
expr_stmt|;
name|assertNotNull
argument_list|(
name|filterPath
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPath
operator|.
name|matches
argument_list|()
argument_list|,
name|is
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPath
operator|.
name|getSegment
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"bar"
argument_list|)
argument_list|)
expr_stmt|;
name|filterPath
operator|=
name|filterPath
operator|.
name|getNext
argument_list|()
expr_stmt|;
name|assertNotNull
argument_list|(
name|filterPath
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPath
operator|.
name|matches
argument_list|()
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPath
operator|.
name|getSegment
argument_list|()
argument_list|,
name|isEmptyString
argument_list|()
argument_list|)
expr_stmt|;
name|assertSame
argument_list|(
name|filterPath
argument_list|,
name|FilterPath
operator|.
name|EMPTY
argument_list|)
expr_stmt|;
block|}
DECL|method|testFilterPathWithSubFields
specifier|public
name|void
name|testFilterPathWithSubFields
parameter_list|()
block|{
specifier|final
name|String
name|input
init|=
literal|"foo.bar.quz"
decl_stmt|;
name|FilterPath
index|[]
name|filterPaths
init|=
name|FilterPath
operator|.
name|compile
argument_list|(
name|input
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|filterPaths
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPaths
argument_list|,
name|arrayWithSize
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|FilterPath
name|filterPath
init|=
name|filterPaths
index|[
literal|0
index|]
decl_stmt|;
name|assertNotNull
argument_list|(
name|filterPath
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPath
operator|.
name|matches
argument_list|()
argument_list|,
name|is
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPath
operator|.
name|getSegment
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
name|filterPath
operator|=
name|filterPath
operator|.
name|getNext
argument_list|()
expr_stmt|;
name|assertNotNull
argument_list|(
name|filterPath
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPath
operator|.
name|matches
argument_list|()
argument_list|,
name|is
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPath
operator|.
name|getSegment
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"bar"
argument_list|)
argument_list|)
expr_stmt|;
name|filterPath
operator|=
name|filterPath
operator|.
name|getNext
argument_list|()
expr_stmt|;
name|assertNotNull
argument_list|(
name|filterPath
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPath
operator|.
name|matches
argument_list|()
argument_list|,
name|is
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPath
operator|.
name|getSegment
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"quz"
argument_list|)
argument_list|)
expr_stmt|;
name|filterPath
operator|=
name|filterPath
operator|.
name|getNext
argument_list|()
expr_stmt|;
name|assertNotNull
argument_list|(
name|filterPath
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPath
operator|.
name|matches
argument_list|()
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPath
operator|.
name|getSegment
argument_list|()
argument_list|,
name|isEmptyString
argument_list|()
argument_list|)
expr_stmt|;
name|assertSame
argument_list|(
name|filterPath
argument_list|,
name|FilterPath
operator|.
name|EMPTY
argument_list|)
expr_stmt|;
block|}
DECL|method|testEmptyFilterPath
specifier|public
name|void
name|testEmptyFilterPath
parameter_list|()
block|{
name|FilterPath
index|[]
name|filterPaths
init|=
name|FilterPath
operator|.
name|compile
argument_list|(
literal|""
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|filterPaths
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPaths
argument_list|,
name|arrayWithSize
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testNullFilterPath
specifier|public
name|void
name|testNullFilterPath
parameter_list|()
block|{
name|FilterPath
index|[]
name|filterPaths
init|=
name|FilterPath
operator|.
name|compile
argument_list|(
operator|(
name|String
operator|)
literal|null
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|filterPaths
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPaths
argument_list|,
name|arrayWithSize
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testFilterPathWithEscapedDots
specifier|public
name|void
name|testFilterPathWithEscapedDots
parameter_list|()
block|{
name|String
name|input
init|=
literal|"w.0.0.t"
decl_stmt|;
name|FilterPath
index|[]
name|filterPaths
init|=
name|FilterPath
operator|.
name|compile
argument_list|(
name|input
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|filterPaths
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPaths
argument_list|,
name|arrayWithSize
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|FilterPath
name|filterPath
init|=
name|filterPaths
index|[
literal|0
index|]
decl_stmt|;
name|assertNotNull
argument_list|(
name|filterPath
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPath
operator|.
name|matches
argument_list|()
argument_list|,
name|is
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPath
operator|.
name|getSegment
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"w"
argument_list|)
argument_list|)
expr_stmt|;
name|filterPath
operator|=
name|filterPath
operator|.
name|getNext
argument_list|()
expr_stmt|;
name|assertNotNull
argument_list|(
name|filterPath
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPath
operator|.
name|matches
argument_list|()
argument_list|,
name|is
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPath
operator|.
name|getSegment
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"0"
argument_list|)
argument_list|)
expr_stmt|;
name|filterPath
operator|=
name|filterPath
operator|.
name|getNext
argument_list|()
expr_stmt|;
name|assertNotNull
argument_list|(
name|filterPath
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPath
operator|.
name|matches
argument_list|()
argument_list|,
name|is
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPath
operator|.
name|getSegment
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"0"
argument_list|)
argument_list|)
expr_stmt|;
name|filterPath
operator|=
name|filterPath
operator|.
name|getNext
argument_list|()
expr_stmt|;
name|assertNotNull
argument_list|(
name|filterPath
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPath
operator|.
name|matches
argument_list|()
argument_list|,
name|is
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPath
operator|.
name|getSegment
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"t"
argument_list|)
argument_list|)
expr_stmt|;
name|filterPath
operator|=
name|filterPath
operator|.
name|getNext
argument_list|()
expr_stmt|;
name|assertNotNull
argument_list|(
name|filterPath
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPath
operator|.
name|matches
argument_list|()
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPath
operator|.
name|getSegment
argument_list|()
argument_list|,
name|isEmptyString
argument_list|()
argument_list|)
expr_stmt|;
name|assertSame
argument_list|(
name|filterPath
argument_list|,
name|FilterPath
operator|.
name|EMPTY
argument_list|)
expr_stmt|;
name|input
operator|=
literal|"w\\.0\\.0\\.t"
expr_stmt|;
name|filterPaths
operator|=
name|FilterPath
operator|.
name|compile
argument_list|(
name|input
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|filterPaths
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPaths
argument_list|,
name|arrayWithSize
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|filterPath
operator|=
name|filterPaths
index|[
literal|0
index|]
expr_stmt|;
name|assertNotNull
argument_list|(
name|filterPath
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPath
operator|.
name|matches
argument_list|()
argument_list|,
name|is
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPath
operator|.
name|getSegment
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"w.0.0.t"
argument_list|)
argument_list|)
expr_stmt|;
name|filterPath
operator|=
name|filterPath
operator|.
name|getNext
argument_list|()
expr_stmt|;
name|assertNotNull
argument_list|(
name|filterPath
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPath
operator|.
name|matches
argument_list|()
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPath
operator|.
name|getSegment
argument_list|()
argument_list|,
name|isEmptyString
argument_list|()
argument_list|)
expr_stmt|;
name|assertSame
argument_list|(
name|filterPath
argument_list|,
name|FilterPath
operator|.
name|EMPTY
argument_list|)
expr_stmt|;
name|input
operator|=
literal|"w\\.0.0\\.t"
expr_stmt|;
name|filterPaths
operator|=
name|FilterPath
operator|.
name|compile
argument_list|(
name|input
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|filterPaths
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPaths
argument_list|,
name|arrayWithSize
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|filterPath
operator|=
name|filterPaths
index|[
literal|0
index|]
expr_stmt|;
name|assertThat
argument_list|(
name|filterPath
operator|.
name|matches
argument_list|()
argument_list|,
name|is
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPath
operator|.
name|getSegment
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"w.0"
argument_list|)
argument_list|)
expr_stmt|;
name|filterPath
operator|=
name|filterPath
operator|.
name|getNext
argument_list|()
expr_stmt|;
name|assertNotNull
argument_list|(
name|filterPath
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPath
operator|.
name|matches
argument_list|()
argument_list|,
name|is
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPath
operator|.
name|getSegment
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"0.t"
argument_list|)
argument_list|)
expr_stmt|;
name|filterPath
operator|=
name|filterPath
operator|.
name|getNext
argument_list|()
expr_stmt|;
name|assertNotNull
argument_list|(
name|filterPath
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPath
operator|.
name|matches
argument_list|()
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPath
operator|.
name|getSegment
argument_list|()
argument_list|,
name|isEmptyString
argument_list|()
argument_list|)
expr_stmt|;
name|assertSame
argument_list|(
name|filterPath
argument_list|,
name|FilterPath
operator|.
name|EMPTY
argument_list|)
expr_stmt|;
block|}
DECL|method|testSimpleWildcardFilterPath
specifier|public
name|void
name|testSimpleWildcardFilterPath
parameter_list|()
block|{
name|FilterPath
index|[]
name|filterPaths
init|=
name|FilterPath
operator|.
name|compile
argument_list|(
literal|"*"
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|filterPaths
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPaths
argument_list|,
name|arrayWithSize
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|FilterPath
name|filterPath
init|=
name|filterPaths
index|[
literal|0
index|]
decl_stmt|;
name|assertNotNull
argument_list|(
name|filterPath
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPath
operator|.
name|matches
argument_list|()
argument_list|,
name|is
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPath
operator|.
name|isSimpleWildcard
argument_list|()
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPath
operator|.
name|getSegment
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"*"
argument_list|)
argument_list|)
expr_stmt|;
name|FilterPath
name|next
init|=
name|filterPath
operator|.
name|matchProperty
argument_list|(
name|randomAsciiOfLength
argument_list|(
literal|2
argument_list|)
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|next
argument_list|)
expr_stmt|;
name|assertSame
argument_list|(
name|next
argument_list|,
name|FilterPath
operator|.
name|EMPTY
argument_list|)
expr_stmt|;
block|}
DECL|method|testWildcardInNameFilterPath
specifier|public
name|void
name|testWildcardInNameFilterPath
parameter_list|()
block|{
name|String
name|input
init|=
literal|"f*o.bar"
decl_stmt|;
name|FilterPath
index|[]
name|filterPaths
init|=
name|FilterPath
operator|.
name|compile
argument_list|(
name|input
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|filterPaths
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPaths
argument_list|,
name|arrayWithSize
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|FilterPath
name|filterPath
init|=
name|filterPaths
index|[
literal|0
index|]
decl_stmt|;
name|assertNotNull
argument_list|(
name|filterPath
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPath
operator|.
name|matches
argument_list|()
argument_list|,
name|is
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPath
operator|.
name|getSegment
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"f*o"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPath
operator|.
name|matchProperty
argument_list|(
literal|"foo"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPath
operator|.
name|matchProperty
argument_list|(
literal|"flo"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPath
operator|.
name|matchProperty
argument_list|(
literal|"foooo"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPath
operator|.
name|matchProperty
argument_list|(
literal|"boo"
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|filterPath
operator|=
name|filterPath
operator|.
name|getNext
argument_list|()
expr_stmt|;
name|assertNotNull
argument_list|(
name|filterPath
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPath
operator|.
name|matches
argument_list|()
argument_list|,
name|is
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPath
operator|.
name|getSegment
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"bar"
argument_list|)
argument_list|)
expr_stmt|;
name|filterPath
operator|=
name|filterPath
operator|.
name|getNext
argument_list|()
expr_stmt|;
name|assertNotNull
argument_list|(
name|filterPath
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPath
operator|.
name|matches
argument_list|()
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPath
operator|.
name|getSegment
argument_list|()
argument_list|,
name|isEmptyString
argument_list|()
argument_list|)
expr_stmt|;
name|assertSame
argument_list|(
name|filterPath
argument_list|,
name|FilterPath
operator|.
name|EMPTY
argument_list|)
expr_stmt|;
block|}
DECL|method|testDoubleWildcardFilterPath
specifier|public
name|void
name|testDoubleWildcardFilterPath
parameter_list|()
block|{
name|FilterPath
index|[]
name|filterPaths
init|=
name|FilterPath
operator|.
name|compile
argument_list|(
literal|"**"
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|filterPaths
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPaths
argument_list|,
name|arrayWithSize
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|FilterPath
name|filterPath
init|=
name|filterPaths
index|[
literal|0
index|]
decl_stmt|;
name|assertNotNull
argument_list|(
name|filterPath
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPath
operator|.
name|matches
argument_list|()
argument_list|,
name|is
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPath
operator|.
name|isDoubleWildcard
argument_list|()
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPath
operator|.
name|getSegment
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"**"
argument_list|)
argument_list|)
expr_stmt|;
name|FilterPath
name|next
init|=
name|filterPath
operator|.
name|matchProperty
argument_list|(
name|randomAsciiOfLength
argument_list|(
literal|2
argument_list|)
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|next
argument_list|)
expr_stmt|;
name|assertSame
argument_list|(
name|next
argument_list|,
name|FilterPath
operator|.
name|EMPTY
argument_list|)
expr_stmt|;
block|}
DECL|method|testStartsWithDoubleWildcardFilterPath
specifier|public
name|void
name|testStartsWithDoubleWildcardFilterPath
parameter_list|()
block|{
name|String
name|input
init|=
literal|"**.bar"
decl_stmt|;
name|FilterPath
index|[]
name|filterPaths
init|=
name|FilterPath
operator|.
name|compile
argument_list|(
name|input
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|filterPaths
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPaths
argument_list|,
name|arrayWithSize
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|FilterPath
name|filterPath
init|=
name|filterPaths
index|[
literal|0
index|]
decl_stmt|;
name|assertNotNull
argument_list|(
name|filterPath
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPath
operator|.
name|matches
argument_list|()
argument_list|,
name|is
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPath
operator|.
name|getSegment
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"**"
argument_list|)
argument_list|)
expr_stmt|;
name|FilterPath
name|next
init|=
name|filterPath
operator|.
name|matchProperty
argument_list|(
name|randomAsciiOfLength
argument_list|(
literal|2
argument_list|)
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|next
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|next
operator|.
name|matches
argument_list|()
argument_list|,
name|is
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|next
operator|.
name|getSegment
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"bar"
argument_list|)
argument_list|)
expr_stmt|;
name|next
operator|=
name|next
operator|.
name|getNext
argument_list|()
expr_stmt|;
name|assertNotNull
argument_list|(
name|next
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|next
operator|.
name|matches
argument_list|()
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|next
operator|.
name|getSegment
argument_list|()
argument_list|,
name|isEmptyString
argument_list|()
argument_list|)
expr_stmt|;
name|assertSame
argument_list|(
name|next
argument_list|,
name|FilterPath
operator|.
name|EMPTY
argument_list|)
expr_stmt|;
block|}
DECL|method|testContainsDoubleWildcardFilterPath
specifier|public
name|void
name|testContainsDoubleWildcardFilterPath
parameter_list|()
block|{
name|String
name|input
init|=
literal|"foo.**.bar"
decl_stmt|;
name|FilterPath
index|[]
name|filterPaths
init|=
name|FilterPath
operator|.
name|compile
argument_list|(
name|input
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|filterPaths
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPaths
argument_list|,
name|arrayWithSize
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|FilterPath
name|filterPath
init|=
name|filterPaths
index|[
literal|0
index|]
decl_stmt|;
name|assertNotNull
argument_list|(
name|filterPath
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPath
operator|.
name|matches
argument_list|()
argument_list|,
name|is
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPath
operator|.
name|getSegment
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
name|filterPath
operator|=
name|filterPath
operator|.
name|getNext
argument_list|()
expr_stmt|;
name|assertNotNull
argument_list|(
name|filterPath
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPath
operator|.
name|matches
argument_list|()
argument_list|,
name|is
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPath
operator|.
name|isDoubleWildcard
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPath
operator|.
name|getSegment
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"**"
argument_list|)
argument_list|)
expr_stmt|;
name|filterPath
operator|=
name|filterPath
operator|.
name|getNext
argument_list|()
expr_stmt|;
name|assertNotNull
argument_list|(
name|filterPath
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPath
operator|.
name|matches
argument_list|()
argument_list|,
name|is
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPath
operator|.
name|getSegment
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"bar"
argument_list|)
argument_list|)
expr_stmt|;
name|filterPath
operator|=
name|filterPath
operator|.
name|getNext
argument_list|()
expr_stmt|;
name|assertNotNull
argument_list|(
name|filterPath
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPath
operator|.
name|matches
argument_list|()
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPath
operator|.
name|getSegment
argument_list|()
argument_list|,
name|isEmptyString
argument_list|()
argument_list|)
expr_stmt|;
name|assertSame
argument_list|(
name|filterPath
argument_list|,
name|FilterPath
operator|.
name|EMPTY
argument_list|)
expr_stmt|;
block|}
DECL|method|testMultipleFilterPaths
specifier|public
name|void
name|testMultipleFilterPaths
parameter_list|()
block|{
name|String
index|[]
name|inputs
init|=
block|{
literal|"foo.**.bar.*"
block|,
literal|"test.dot\\.ted"
block|}
decl_stmt|;
name|FilterPath
index|[]
name|filterPaths
init|=
name|FilterPath
operator|.
name|compile
argument_list|(
name|inputs
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|filterPaths
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPaths
argument_list|,
name|arrayWithSize
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
comment|// foo.**.bar.*
name|FilterPath
name|filterPath
init|=
name|filterPaths
index|[
literal|0
index|]
decl_stmt|;
name|assertNotNull
argument_list|(
name|filterPath
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPath
operator|.
name|matches
argument_list|()
argument_list|,
name|is
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPath
operator|.
name|getSegment
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
name|filterPath
operator|=
name|filterPath
operator|.
name|getNext
argument_list|()
expr_stmt|;
name|assertNotNull
argument_list|(
name|filterPath
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPath
operator|.
name|matches
argument_list|()
argument_list|,
name|is
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPath
operator|.
name|isDoubleWildcard
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPath
operator|.
name|getSegment
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"**"
argument_list|)
argument_list|)
expr_stmt|;
name|filterPath
operator|=
name|filterPath
operator|.
name|getNext
argument_list|()
expr_stmt|;
name|assertNotNull
argument_list|(
name|filterPath
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPath
operator|.
name|matches
argument_list|()
argument_list|,
name|is
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPath
operator|.
name|getSegment
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"bar"
argument_list|)
argument_list|)
expr_stmt|;
name|filterPath
operator|=
name|filterPath
operator|.
name|getNext
argument_list|()
expr_stmt|;
name|assertNotNull
argument_list|(
name|filterPath
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPath
operator|.
name|isSimpleWildcard
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPath
operator|.
name|getSegment
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"*"
argument_list|)
argument_list|)
expr_stmt|;
name|filterPath
operator|=
name|filterPath
operator|.
name|getNext
argument_list|()
expr_stmt|;
name|assertNotNull
argument_list|(
name|filterPath
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPath
operator|.
name|matches
argument_list|()
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPath
operator|.
name|getSegment
argument_list|()
argument_list|,
name|isEmptyString
argument_list|()
argument_list|)
expr_stmt|;
name|assertSame
argument_list|(
name|filterPath
argument_list|,
name|FilterPath
operator|.
name|EMPTY
argument_list|)
expr_stmt|;
comment|// test.dot\.ted
name|filterPath
operator|=
name|filterPaths
index|[
literal|1
index|]
expr_stmt|;
name|assertNotNull
argument_list|(
name|filterPath
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPath
operator|.
name|matches
argument_list|()
argument_list|,
name|is
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPath
operator|.
name|getSegment
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"test"
argument_list|)
argument_list|)
expr_stmt|;
name|filterPath
operator|=
name|filterPath
operator|.
name|getNext
argument_list|()
expr_stmt|;
name|assertNotNull
argument_list|(
name|filterPath
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPath
operator|.
name|matches
argument_list|()
argument_list|,
name|is
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPath
operator|.
name|getSegment
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"dot.ted"
argument_list|)
argument_list|)
expr_stmt|;
name|filterPath
operator|=
name|filterPath
operator|.
name|getNext
argument_list|()
expr_stmt|;
name|assertNotNull
argument_list|(
name|filterPath
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPath
operator|.
name|matches
argument_list|()
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|filterPath
operator|.
name|getSegment
argument_list|()
argument_list|,
name|isEmptyString
argument_list|()
argument_list|)
expr_stmt|;
name|assertSame
argument_list|(
name|filterPath
argument_list|,
name|FilterPath
operator|.
name|EMPTY
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

