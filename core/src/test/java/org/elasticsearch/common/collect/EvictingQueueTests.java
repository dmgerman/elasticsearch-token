begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright (C) 2012 The Guava Authors  *  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.collect
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|collect
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
name|util
operator|.
name|CollectionUtils
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
name|NoSuchElementException
import|;
end_import

begin_class
DECL|class|EvictingQueueTests
specifier|public
class|class
name|EvictingQueueTests
extends|extends
name|ESTestCase
block|{
DECL|method|testCreateWithNegativeSize
specifier|public
name|void
name|testCreateWithNegativeSize
parameter_list|()
throws|throws
name|Exception
block|{
try|try
block|{
operator|new
name|EvictingQueue
argument_list|<>
argument_list|(
operator|-
literal|1
argument_list|)
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|expected
parameter_list|)
block|{         }
block|}
DECL|method|testCreateWithZeroSize
specifier|public
name|void
name|testCreateWithZeroSize
parameter_list|()
throws|throws
name|Exception
block|{
name|EvictingQueue
argument_list|<
name|String
argument_list|>
name|queue
init|=
operator|new
name|EvictingQueue
argument_list|<>
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|queue
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|queue
operator|.
name|add
argument_list|(
literal|"hi"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|queue
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|queue
operator|.
name|offer
argument_list|(
literal|"hi"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|queue
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|queue
operator|.
name|remove
argument_list|(
literal|"hi"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|queue
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
name|queue
operator|.
name|element
argument_list|()
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NoSuchElementException
name|expected
parameter_list|)
block|{}
name|assertNull
argument_list|(
name|queue
operator|.
name|peek
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|queue
operator|.
name|poll
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
name|queue
operator|.
name|remove
argument_list|()
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NoSuchElementException
name|expected
parameter_list|)
block|{}
block|}
DECL|method|testRemainingCapacityMaximumSizeZero
specifier|public
name|void
name|testRemainingCapacityMaximumSizeZero
parameter_list|()
block|{
name|EvictingQueue
argument_list|<
name|String
argument_list|>
name|queue
init|=
operator|new
name|EvictingQueue
argument_list|<>
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|queue
operator|.
name|remainingCapacity
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testRemainingCapacityMaximumSizeOne
specifier|public
name|void
name|testRemainingCapacityMaximumSizeOne
parameter_list|()
block|{
name|EvictingQueue
argument_list|<
name|String
argument_list|>
name|queue
init|=
operator|new
name|EvictingQueue
argument_list|<>
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|queue
operator|.
name|remainingCapacity
argument_list|()
argument_list|)
expr_stmt|;
name|queue
operator|.
name|add
argument_list|(
literal|"hi"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|queue
operator|.
name|remainingCapacity
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testRemainingCapacityMaximumSizeThree
specifier|public
name|void
name|testRemainingCapacityMaximumSizeThree
parameter_list|()
block|{
name|EvictingQueue
argument_list|<
name|String
argument_list|>
name|queue
init|=
operator|new
name|EvictingQueue
argument_list|<>
argument_list|(
literal|3
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|queue
operator|.
name|remainingCapacity
argument_list|()
argument_list|)
expr_stmt|;
name|queue
operator|.
name|add
argument_list|(
literal|"hi"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|queue
operator|.
name|remainingCapacity
argument_list|()
argument_list|)
expr_stmt|;
name|queue
operator|.
name|add
argument_list|(
literal|"hi"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|queue
operator|.
name|remainingCapacity
argument_list|()
argument_list|)
expr_stmt|;
name|queue
operator|.
name|add
argument_list|(
literal|"hi"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|queue
operator|.
name|remainingCapacity
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testEvictingAfterOne
specifier|public
name|void
name|testEvictingAfterOne
parameter_list|()
throws|throws
name|Exception
block|{
name|EvictingQueue
argument_list|<
name|String
argument_list|>
name|queue
init|=
operator|new
name|EvictingQueue
argument_list|<>
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|queue
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|queue
operator|.
name|remainingCapacity
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|queue
operator|.
name|add
argument_list|(
literal|"hi"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"hi"
argument_list|,
name|queue
operator|.
name|element
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"hi"
argument_list|,
name|queue
operator|.
name|peek
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|queue
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|queue
operator|.
name|remainingCapacity
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|queue
operator|.
name|add
argument_list|(
literal|"there"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"there"
argument_list|,
name|queue
operator|.
name|element
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"there"
argument_list|,
name|queue
operator|.
name|peek
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|queue
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|queue
operator|.
name|remainingCapacity
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"there"
argument_list|,
name|queue
operator|.
name|remove
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|queue
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|queue
operator|.
name|remainingCapacity
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testEvictingAfterThree
specifier|public
name|void
name|testEvictingAfterThree
parameter_list|()
throws|throws
name|Exception
block|{
name|EvictingQueue
argument_list|<
name|String
argument_list|>
name|queue
init|=
operator|new
name|EvictingQueue
argument_list|<>
argument_list|(
literal|3
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|queue
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|queue
operator|.
name|remainingCapacity
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|queue
operator|.
name|add
argument_list|(
literal|"one"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|queue
operator|.
name|add
argument_list|(
literal|"two"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|queue
operator|.
name|add
argument_list|(
literal|"three"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"one"
argument_list|,
name|queue
operator|.
name|element
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"one"
argument_list|,
name|queue
operator|.
name|peek
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|queue
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|queue
operator|.
name|remainingCapacity
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|queue
operator|.
name|add
argument_list|(
literal|"four"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"two"
argument_list|,
name|queue
operator|.
name|element
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"two"
argument_list|,
name|queue
operator|.
name|peek
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|queue
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|queue
operator|.
name|remainingCapacity
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"two"
argument_list|,
name|queue
operator|.
name|remove
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|queue
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|queue
operator|.
name|remainingCapacity
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testAddAll
specifier|public
name|void
name|testAddAll
parameter_list|()
throws|throws
name|Exception
block|{
name|EvictingQueue
argument_list|<
name|String
argument_list|>
name|queue
init|=
operator|new
name|EvictingQueue
argument_list|<>
argument_list|(
literal|3
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|queue
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|queue
operator|.
name|remainingCapacity
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|queue
operator|.
name|addAll
argument_list|(
name|CollectionUtils
operator|.
name|arrayAsArrayList
argument_list|(
literal|"one"
argument_list|,
literal|"two"
argument_list|,
literal|"three"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"one"
argument_list|,
name|queue
operator|.
name|element
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"one"
argument_list|,
name|queue
operator|.
name|peek
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|queue
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|queue
operator|.
name|remainingCapacity
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|queue
operator|.
name|addAll
argument_list|(
name|Collections
operator|.
name|singletonList
argument_list|(
literal|"four"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"two"
argument_list|,
name|queue
operator|.
name|element
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"two"
argument_list|,
name|queue
operator|.
name|peek
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|queue
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|queue
operator|.
name|remainingCapacity
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"two"
argument_list|,
name|queue
operator|.
name|remove
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|queue
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|queue
operator|.
name|remainingCapacity
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit
