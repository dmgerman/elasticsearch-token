begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.test
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|test
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
name|common
operator|.
name|io
operator|.
name|stream
operator|.
name|NamedWriteable
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
name|io
operator|.
name|stream
operator|.
name|NamedWriteableAwareStreamInput
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
name|io
operator|.
name|stream
operator|.
name|NamedWriteableRegistry
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
name|io
operator|.
name|stream
operator|.
name|StreamInput
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
name|io
operator|.
name|stream
operator|.
name|Streamable
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
name|Collections
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

begin_class
DECL|class|AbstractStreamableTestCase
specifier|public
specifier|abstract
class|class
name|AbstractStreamableTestCase
parameter_list|<
name|T
extends|extends
name|Streamable
parameter_list|>
extends|extends
name|ESTestCase
block|{
DECL|field|NUMBER_OF_TEST_RUNS
specifier|protected
specifier|static
specifier|final
name|int
name|NUMBER_OF_TEST_RUNS
init|=
literal|20
decl_stmt|;
comment|/**      * Creates a random test instance to use in the tests. This method will be      * called multiple times during test execution and should return a different      * random instance each time it is called.      */
DECL|method|createTestInstance
specifier|protected
specifier|abstract
name|T
name|createTestInstance
parameter_list|()
function_decl|;
comment|/**      * Creates an empty instance to use when deserialising the      * {@link Streamable}. This usually returns an instance created using the      * zer-arg constructor      */
DECL|method|createBlankInstance
specifier|protected
specifier|abstract
name|T
name|createBlankInstance
parameter_list|()
function_decl|;
comment|/**      * Tests that the equals and hashcode methods are consistent and copied      * versions of the instance have are equal.      */
DECL|method|testEqualsAndHashcode
specifier|public
name|void
name|testEqualsAndHashcode
parameter_list|()
throws|throws
name|IOException
block|{
for|for
control|(
name|int
name|runs
init|=
literal|0
init|;
name|runs
operator|<
name|NUMBER_OF_TEST_RUNS
condition|;
name|runs
operator|++
control|)
block|{
name|T
name|firstInstance
init|=
name|createTestInstance
argument_list|()
decl_stmt|;
name|assertFalse
argument_list|(
literal|"instance is equal to null"
argument_list|,
name|firstInstance
operator|.
name|equals
argument_list|(
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
literal|"instance is equal to incompatible type"
argument_list|,
name|firstInstance
operator|.
name|equals
argument_list|(
literal|""
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"instance is not equal to self"
argument_list|,
name|firstInstance
argument_list|,
name|firstInstance
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"same instance's hashcode returns different values if called multiple times"
argument_list|,
name|firstInstance
operator|.
name|hashCode
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|firstInstance
operator|.
name|hashCode
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|T
name|secondInstance
init|=
name|copyInstance
argument_list|(
name|firstInstance
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"instance is not equal to self"
argument_list|,
name|secondInstance
argument_list|,
name|secondInstance
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"instance is not equal to its copy"
argument_list|,
name|firstInstance
argument_list|,
name|secondInstance
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"equals is not symmetric"
argument_list|,
name|secondInstance
argument_list|,
name|firstInstance
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"instance copy's hashcode is different from original hashcode"
argument_list|,
name|secondInstance
operator|.
name|hashCode
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|firstInstance
operator|.
name|hashCode
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|T
name|thirdInstance
init|=
name|copyInstance
argument_list|(
name|secondInstance
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"instance is not equal to self"
argument_list|,
name|thirdInstance
argument_list|,
name|thirdInstance
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"instance is not equal to its copy"
argument_list|,
name|secondInstance
argument_list|,
name|thirdInstance
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"instance copy's hashcode is different from original hashcode"
argument_list|,
name|secondInstance
operator|.
name|hashCode
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|thirdInstance
operator|.
name|hashCode
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"equals is not transitive"
argument_list|,
name|firstInstance
argument_list|,
name|thirdInstance
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"instance copy's hashcode is different from original hashcode"
argument_list|,
name|firstInstance
operator|.
name|hashCode
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|thirdInstance
operator|.
name|hashCode
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"equals is not symmetric"
argument_list|,
name|thirdInstance
argument_list|,
name|secondInstance
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"equals is not symmetric"
argument_list|,
name|thirdInstance
argument_list|,
name|firstInstance
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      * Test serialization and deserialization of the test instance.      */
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
name|int
name|runs
init|=
literal|0
init|;
name|runs
operator|<
name|NUMBER_OF_TEST_RUNS
condition|;
name|runs
operator|++
control|)
block|{
name|T
name|testInstance
init|=
name|createTestInstance
argument_list|()
decl_stmt|;
name|assertSerialization
argument_list|(
name|testInstance
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      * Serialize the given instance and asserts that both are equal      */
DECL|method|assertSerialization
specifier|protected
name|T
name|assertSerialization
parameter_list|(
name|T
name|testInstance
parameter_list|)
throws|throws
name|IOException
block|{
name|T
name|deserializedInstance
init|=
name|copyInstance
argument_list|(
name|testInstance
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|testInstance
argument_list|,
name|deserializedInstance
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|testInstance
operator|.
name|hashCode
argument_list|()
argument_list|,
name|deserializedInstance
operator|.
name|hashCode
argument_list|()
argument_list|)
expr_stmt|;
name|assertNotSame
argument_list|(
name|testInstance
argument_list|,
name|deserializedInstance
argument_list|)
expr_stmt|;
return|return
name|deserializedInstance
return|;
block|}
DECL|method|copyInstance
specifier|private
name|T
name|copyInstance
parameter_list|(
name|T
name|instance
parameter_list|)
throws|throws
name|IOException
block|{
try|try
init|(
name|BytesStreamOutput
name|output
init|=
operator|new
name|BytesStreamOutput
argument_list|()
init|)
block|{
name|instance
operator|.
name|writeTo
argument_list|(
name|output
argument_list|)
expr_stmt|;
try|try
init|(
name|StreamInput
name|in
init|=
operator|new
name|NamedWriteableAwareStreamInput
argument_list|(
name|output
operator|.
name|bytes
argument_list|()
operator|.
name|streamInput
argument_list|()
argument_list|,
name|getNamedWriteableRegistry
argument_list|()
argument_list|)
init|)
block|{
name|T
name|newInstance
init|=
name|createBlankInstance
argument_list|()
decl_stmt|;
name|newInstance
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
return|return
name|newInstance
return|;
block|}
block|}
block|}
comment|/**      * Get the {@link NamedWriteableRegistry} to use when de-serializing the object.      *       * Override this method if you need to register {@link NamedWriteable}s for the test object to de-serialize.      *       * By default this will return a {@link NamedWriteableRegistry} with no registered {@link NamedWriteable}s      */
DECL|method|getNamedWriteableRegistry
specifier|protected
name|NamedWriteableRegistry
name|getNamedWriteableRegistry
parameter_list|()
block|{
return|return
operator|new
name|NamedWriteableRegistry
argument_list|(
name|Collections
operator|.
name|emptyList
argument_list|()
argument_list|)
return|;
block|}
block|}
end_class

end_unit

