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
name|java
operator|.
name|io
operator|.
name|IOException
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
name|not
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertFalse
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertThat
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertTrue
import|;
end_import

begin_comment
comment|/**  * Utility class that encapsulates standard checks and assertions around testing the equals() and hashCode()  * methods of objects that implement them.  */
end_comment

begin_class
DECL|class|EqualsHashCodeTestUtils
specifier|public
class|class
name|EqualsHashCodeTestUtils
block|{
DECL|field|someObjects
specifier|private
specifier|static
name|Object
index|[]
name|someObjects
init|=
operator|new
name|Object
index|[]
block|{
literal|"some string"
block|,
operator|new
name|Integer
argument_list|(
literal|1
argument_list|)
block|,
operator|new
name|Double
argument_list|(
literal|1.0
argument_list|)
block|}
decl_stmt|;
comment|/**      * A function that makes a copy of its input argument      */
DECL|interface|CopyFunction
specifier|public
interface|interface
name|CopyFunction
parameter_list|<
name|T
parameter_list|>
block|{
DECL|method|copy
name|T
name|copy
parameter_list|(
name|T
name|t
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
empty_stmt|;
comment|/**      * A function that creates a copy of its input argument that is different from its      * input in exactly one aspect (e.g. one parameter of a class instance should change)      */
DECL|interface|MutateFunction
specifier|public
interface|interface
name|MutateFunction
parameter_list|<
name|T
parameter_list|>
block|{
DECL|method|mutate
name|T
name|mutate
parameter_list|(
name|T
name|t
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
empty_stmt|;
comment|/**      * Perform common equality and hashCode checks on the input object      * @param original the object under test      * @param copyFunction a function that creates a deep copy of the input object      */
DECL|method|checkEqualsAndHashCode
specifier|public
specifier|static
parameter_list|<
name|T
parameter_list|>
name|void
name|checkEqualsAndHashCode
parameter_list|(
name|T
name|original
parameter_list|,
name|CopyFunction
argument_list|<
name|T
argument_list|>
name|copyFunction
parameter_list|)
block|{
name|checkEqualsAndHashCode
argument_list|(
name|original
argument_list|,
name|copyFunction
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**      * Perform common equality and hashCode checks on the input object      * @param original the object under test      * @param copyFunction a function that creates a deep copy of the input object      * @param mutationFunction a function that creates a copy of the input object that is different      * from the input in one aspect. The output of this call is used to check that it is not equal()      * to the input object      */
DECL|method|checkEqualsAndHashCode
specifier|public
specifier|static
parameter_list|<
name|T
parameter_list|>
name|void
name|checkEqualsAndHashCode
parameter_list|(
name|T
name|original
parameter_list|,
name|CopyFunction
argument_list|<
name|T
argument_list|>
name|copyFunction
parameter_list|,
name|MutateFunction
argument_list|<
name|T
argument_list|>
name|mutationFunction
parameter_list|)
block|{
try|try
block|{
name|String
name|objectName
init|=
name|original
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
decl_stmt|;
name|assertFalse
argument_list|(
name|objectName
operator|+
literal|" is equal to null"
argument_list|,
name|original
operator|.
name|equals
argument_list|(
literal|null
argument_list|)
argument_list|)
expr_stmt|;
comment|// TODO not sure how useful the following test is
name|assertFalse
argument_list|(
name|objectName
operator|+
literal|" is equal to incompatible type"
argument_list|,
name|original
operator|.
name|equals
argument_list|(
name|ESTestCase
operator|.
name|randomFrom
argument_list|(
name|someObjects
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|objectName
operator|+
literal|" is not equal to self"
argument_list|,
name|original
operator|.
name|equals
argument_list|(
name|original
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|objectName
operator|+
literal|" hashcode returns different values if called multiple times"
argument_list|,
name|original
operator|.
name|hashCode
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|original
operator|.
name|hashCode
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|mutationFunction
operator|!=
literal|null
condition|)
block|{
name|T
name|mutation
init|=
name|mutationFunction
operator|.
name|mutate
argument_list|(
name|original
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|objectName
operator|+
literal|" mutation should not be equal to original"
argument_list|,
name|mutation
argument_list|,
name|not
argument_list|(
name|equalTo
argument_list|(
name|original
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|T
name|copy
init|=
name|copyFunction
operator|.
name|copy
argument_list|(
name|original
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|objectName
operator|+
literal|" copy is not equal to self"
argument_list|,
name|copy
operator|.
name|equals
argument_list|(
name|copy
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|objectName
operator|+
literal|" is not equal to its copy"
argument_list|,
name|original
operator|.
name|equals
argument_list|(
name|copy
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"equals is not symmetric"
argument_list|,
name|copy
operator|.
name|equals
argument_list|(
name|original
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|objectName
operator|+
literal|" hashcode is different from copies hashcode"
argument_list|,
name|copy
operator|.
name|hashCode
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|original
operator|.
name|hashCode
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|T
name|secondCopy
init|=
name|copyFunction
operator|.
name|copy
argument_list|(
name|copy
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"second copy is not equal to self"
argument_list|,
name|secondCopy
operator|.
name|equals
argument_list|(
name|secondCopy
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"copy is not equal to its second copy"
argument_list|,
name|copy
operator|.
name|equals
argument_list|(
name|secondCopy
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"second copy's hashcode is different from original hashcode"
argument_list|,
name|copy
operator|.
name|hashCode
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|secondCopy
operator|.
name|hashCode
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"equals is not transitive"
argument_list|,
name|original
operator|.
name|equals
argument_list|(
name|secondCopy
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"equals is not symmetric"
argument_list|,
name|secondCopy
operator|.
name|equals
argument_list|(
name|copy
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"equals is not symmetric"
argument_list|,
name|secondCopy
operator|.
name|equals
argument_list|(
name|original
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

