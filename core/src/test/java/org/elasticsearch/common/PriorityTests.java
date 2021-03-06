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
name|common
operator|.
name|io
operator|.
name|stream
operator|.
name|BytesStreamOutput
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
name|Collections
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

begin_class
DECL|class|PriorityTests
specifier|public
class|class
name|PriorityTests
extends|extends
name|ESTestCase
block|{
DECL|method|testValueOf
specifier|public
name|void
name|testValueOf
parameter_list|()
block|{
for|for
control|(
name|Priority
name|p
range|:
name|Priority
operator|.
name|values
argument_list|()
control|)
block|{
name|assertSame
argument_list|(
name|p
argument_list|,
name|Priority
operator|.
name|valueOf
argument_list|(
name|p
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|IllegalArgumentException
name|exception
init|=
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
block|{
name|Priority
operator|.
name|valueOf
argument_list|(
literal|"foobar"
argument_list|)
expr_stmt|;
block|}
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"No enum constant org.elasticsearch.common.Priority.foobar"
argument_list|,
name|exception
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testToString
specifier|public
name|void
name|testToString
parameter_list|()
block|{
name|assertEquals
argument_list|(
literal|"IMMEDIATE"
argument_list|,
name|Priority
operator|.
name|IMMEDIATE
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"HIGH"
argument_list|,
name|Priority
operator|.
name|HIGH
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"LANGUID"
argument_list|,
name|Priority
operator|.
name|LANGUID
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"LOW"
argument_list|,
name|Priority
operator|.
name|LOW
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"URGENT"
argument_list|,
name|Priority
operator|.
name|URGENT
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"NORMAL"
argument_list|,
name|Priority
operator|.
name|NORMAL
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|6
argument_list|,
name|Priority
operator|.
name|values
argument_list|()
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
DECL|method|testSerialization
specifier|public
name|void
name|testSerialization
parameter_list|()
throws|throws
name|IOException
block|{
for|for
control|(
name|Priority
name|p
range|:
name|Priority
operator|.
name|values
argument_list|()
control|)
block|{
name|BytesStreamOutput
name|out
init|=
operator|new
name|BytesStreamOutput
argument_list|()
decl_stmt|;
name|Priority
operator|.
name|writeTo
argument_list|(
name|p
argument_list|,
name|out
argument_list|)
expr_stmt|;
name|Priority
name|priority
init|=
name|Priority
operator|.
name|readFrom
argument_list|(
name|out
operator|.
name|bytes
argument_list|()
operator|.
name|streamInput
argument_list|()
argument_list|)
decl_stmt|;
name|assertSame
argument_list|(
name|p
argument_list|,
name|priority
argument_list|)
expr_stmt|;
block|}
name|assertSame
argument_list|(
name|Priority
operator|.
name|IMMEDIATE
argument_list|,
name|Priority
operator|.
name|fromByte
argument_list|(
operator|(
name|byte
operator|)
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertSame
argument_list|(
name|Priority
operator|.
name|HIGH
argument_list|,
name|Priority
operator|.
name|fromByte
argument_list|(
operator|(
name|byte
operator|)
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertSame
argument_list|(
name|Priority
operator|.
name|LANGUID
argument_list|,
name|Priority
operator|.
name|fromByte
argument_list|(
operator|(
name|byte
operator|)
literal|5
argument_list|)
argument_list|)
expr_stmt|;
name|assertSame
argument_list|(
name|Priority
operator|.
name|LOW
argument_list|,
name|Priority
operator|.
name|fromByte
argument_list|(
operator|(
name|byte
operator|)
literal|4
argument_list|)
argument_list|)
expr_stmt|;
name|assertSame
argument_list|(
name|Priority
operator|.
name|NORMAL
argument_list|,
name|Priority
operator|.
name|fromByte
argument_list|(
operator|(
name|byte
operator|)
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|assertSame
argument_list|(
name|Priority
operator|.
name|URGENT
argument_list|,
name|Priority
operator|.
name|fromByte
argument_list|(
operator|(
name|byte
operator|)
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|6
argument_list|,
name|Priority
operator|.
name|values
argument_list|()
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
DECL|method|testCompareTo
specifier|public
name|void
name|testCompareTo
parameter_list|()
block|{
name|assertTrue
argument_list|(
name|Priority
operator|.
name|IMMEDIATE
operator|.
name|compareTo
argument_list|(
name|Priority
operator|.
name|URGENT
argument_list|)
operator|<
literal|0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Priority
operator|.
name|URGENT
operator|.
name|compareTo
argument_list|(
name|Priority
operator|.
name|HIGH
argument_list|)
operator|<
literal|0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Priority
operator|.
name|HIGH
operator|.
name|compareTo
argument_list|(
name|Priority
operator|.
name|NORMAL
argument_list|)
operator|<
literal|0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Priority
operator|.
name|NORMAL
operator|.
name|compareTo
argument_list|(
name|Priority
operator|.
name|LOW
argument_list|)
operator|<
literal|0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Priority
operator|.
name|LOW
operator|.
name|compareTo
argument_list|(
name|Priority
operator|.
name|LANGUID
argument_list|)
operator|<
literal|0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Priority
operator|.
name|URGENT
operator|.
name|compareTo
argument_list|(
name|Priority
operator|.
name|IMMEDIATE
argument_list|)
operator|>
literal|0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Priority
operator|.
name|HIGH
operator|.
name|compareTo
argument_list|(
name|Priority
operator|.
name|URGENT
argument_list|)
operator|>
literal|0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Priority
operator|.
name|NORMAL
operator|.
name|compareTo
argument_list|(
name|Priority
operator|.
name|HIGH
argument_list|)
operator|>
literal|0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Priority
operator|.
name|LOW
operator|.
name|compareTo
argument_list|(
name|Priority
operator|.
name|NORMAL
argument_list|)
operator|>
literal|0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Priority
operator|.
name|LANGUID
operator|.
name|compareTo
argument_list|(
name|Priority
operator|.
name|LOW
argument_list|)
operator|>
literal|0
argument_list|)
expr_stmt|;
for|for
control|(
name|Priority
name|p
range|:
name|Priority
operator|.
name|values
argument_list|()
control|)
block|{
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|p
operator|.
name|compareTo
argument_list|(
name|p
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|Priority
argument_list|>
name|shuffeledAndSorted
init|=
name|Arrays
operator|.
name|asList
argument_list|(
name|Priority
operator|.
name|values
argument_list|()
argument_list|)
decl_stmt|;
name|Collections
operator|.
name|shuffle
argument_list|(
name|shuffeledAndSorted
argument_list|,
name|random
argument_list|()
argument_list|)
expr_stmt|;
name|Collections
operator|.
name|sort
argument_list|(
name|shuffeledAndSorted
argument_list|)
expr_stmt|;
for|for
control|(
name|List
argument_list|<
name|Priority
argument_list|>
name|priorities
range|:
name|Arrays
operator|.
name|asList
argument_list|(
name|shuffeledAndSorted
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|Priority
operator|.
name|values
argument_list|()
argument_list|)
argument_list|)
control|)
block|{
comment|// #values() guarantees order!
name|assertSame
argument_list|(
name|Priority
operator|.
name|IMMEDIATE
argument_list|,
name|priorities
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertSame
argument_list|(
name|Priority
operator|.
name|URGENT
argument_list|,
name|priorities
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertSame
argument_list|(
name|Priority
operator|.
name|HIGH
argument_list|,
name|priorities
operator|.
name|get
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertSame
argument_list|(
name|Priority
operator|.
name|NORMAL
argument_list|,
name|priorities
operator|.
name|get
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|assertSame
argument_list|(
name|Priority
operator|.
name|LOW
argument_list|,
name|priorities
operator|.
name|get
argument_list|(
literal|4
argument_list|)
argument_list|)
expr_stmt|;
name|assertSame
argument_list|(
name|Priority
operator|.
name|LANGUID
argument_list|,
name|priorities
operator|.
name|get
argument_list|(
literal|5
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

