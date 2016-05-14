begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.util.iterable
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|util
operator|.
name|iterable
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
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
name|Iterator
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
name|NoSuchElementException
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
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|object
operator|.
name|HasToString
operator|.
name|hasToString
import|;
end_import

begin_class
DECL|class|IterablesTests
specifier|public
class|class
name|IterablesTests
extends|extends
name|ESTestCase
block|{
DECL|method|testGetOverList
specifier|public
name|void
name|testGetOverList
parameter_list|()
block|{
name|test
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"a"
argument_list|,
literal|"b"
argument_list|,
literal|"c"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testGetOverIterable
specifier|public
name|void
name|testGetOverIterable
parameter_list|()
block|{
name|Iterable
argument_list|<
name|String
argument_list|>
name|iterable
init|=
parameter_list|()
lambda|->
operator|new
name|Iterator
argument_list|<
name|String
argument_list|>
argument_list|()
block|{
specifier|private
name|int
name|position
operator|=
literal|0
decl_stmt|;
annotation|@
name|Override
specifier|public
name|boolean
name|hasNext
parameter_list|()
block|{
return|return
name|position
operator|<
literal|3
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|next
parameter_list|()
block|{
if|if
condition|(
name|position
operator|<
literal|3
condition|)
block|{
name|String
name|s
init|=
name|position
operator|==
literal|0
condition|?
literal|"a"
else|:
name|position
operator|==
literal|1
condition|?
literal|"b"
else|:
literal|"c"
decl_stmt|;
name|position
operator|++
expr_stmt|;
return|return
name|s
return|;
block|}
else|else
block|{
throw|throw
operator|new
name|NoSuchElementException
argument_list|()
throw|;
block|}
block|}
block|}
empty_stmt|;
name|test
parameter_list|(
name|iterable
parameter_list|)
constructor_decl|;
block|}
end_class

begin_function
DECL|method|testFlatten
specifier|public
name|void
name|testFlatten
parameter_list|()
block|{
name|List
argument_list|<
name|List
argument_list|<
name|Integer
argument_list|>
argument_list|>
name|list
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|Iterable
argument_list|<
name|Integer
argument_list|>
name|allInts
init|=
name|Iterables
operator|.
name|flatten
argument_list|(
name|list
argument_list|)
decl_stmt|;
name|int
name|count
init|=
literal|0
decl_stmt|;
for|for
control|(
name|int
name|x
range|:
name|allInts
control|)
block|{
name|count
operator|++
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|count
argument_list|)
expr_stmt|;
name|list
operator|.
name|add
argument_list|(
operator|new
name|ArrayList
argument_list|<>
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|x
range|:
name|allInts
control|)
block|{
name|count
operator|++
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|count
argument_list|)
expr_stmt|;
name|list
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|add
argument_list|(
literal|0
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|x
range|:
name|allInts
control|)
block|{
name|count
operator|++
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|count
argument_list|)
expr_stmt|;
block|}
end_function

begin_function
DECL|method|test
specifier|private
name|void
name|test
parameter_list|(
name|Iterable
argument_list|<
name|String
argument_list|>
name|iterable
parameter_list|)
block|{
try|try
block|{
name|Iterables
operator|.
name|get
argument_list|(
name|iterable
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"expected IllegalArgumentException"
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
argument_list|,
name|hasToString
argument_list|(
literal|"java.lang.IllegalArgumentException: position>= 0"
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|"a"
argument_list|,
name|Iterables
operator|.
name|get
argument_list|(
name|iterable
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"b"
argument_list|,
name|Iterables
operator|.
name|get
argument_list|(
name|iterable
argument_list|,
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"c"
argument_list|,
name|Iterables
operator|.
name|get
argument_list|(
name|iterable
argument_list|,
literal|2
argument_list|)
argument_list|)
expr_stmt|;
try|try
block|{
name|Iterables
operator|.
name|get
argument_list|(
name|iterable
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"expected IndexOutOfBoundsException"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IndexOutOfBoundsException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|e
argument_list|,
name|hasToString
argument_list|(
literal|"java.lang.IndexOutOfBoundsException: 3"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_function

unit|}
end_unit

