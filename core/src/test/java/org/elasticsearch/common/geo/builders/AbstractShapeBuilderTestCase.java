begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.geo.builders
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|geo
operator|.
name|builders
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
name|Writeable
operator|.
name|Reader
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
name|ToXContent
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
name|XContentBuilder
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
name|XContentHelper
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
name|common
operator|.
name|xcontent
operator|.
name|XContentType
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
name|AfterClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|BeforeClass
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
name|ArrayList
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
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|EqualsHashCodeTestUtils
operator|.
name|checkEqualsAndHashCode
import|;
end_import

begin_class
DECL|class|AbstractShapeBuilderTestCase
specifier|public
specifier|abstract
class|class
name|AbstractShapeBuilderTestCase
parameter_list|<
name|SB
extends|extends
name|ShapeBuilder
parameter_list|>
extends|extends
name|ESTestCase
block|{
DECL|field|NUMBER_OF_TESTBUILDERS
specifier|private
specifier|static
specifier|final
name|int
name|NUMBER_OF_TESTBUILDERS
init|=
literal|20
decl_stmt|;
DECL|field|namedWriteableRegistry
specifier|private
specifier|static
name|NamedWriteableRegistry
name|namedWriteableRegistry
decl_stmt|;
comment|/**      * setup for the whole base test class      */
annotation|@
name|BeforeClass
DECL|method|init
specifier|public
specifier|static
name|void
name|init
parameter_list|()
block|{
if|if
condition|(
name|namedWriteableRegistry
operator|==
literal|null
condition|)
block|{
name|List
argument_list|<
name|NamedWriteableRegistry
operator|.
name|Entry
argument_list|>
name|shapes
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|ShapeBuilders
operator|.
name|register
argument_list|(
name|shapes
argument_list|)
expr_stmt|;
name|namedWriteableRegistry
operator|=
operator|new
name|NamedWriteableRegistry
argument_list|(
name|shapes
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|AfterClass
DECL|method|afterClass
specifier|public
specifier|static
name|void
name|afterClass
parameter_list|()
throws|throws
name|Exception
block|{
name|namedWriteableRegistry
operator|=
literal|null
expr_stmt|;
block|}
comment|/**      * create random shape that is put under test      */
DECL|method|createTestShapeBuilder
specifier|protected
specifier|abstract
name|SB
name|createTestShapeBuilder
parameter_list|()
function_decl|;
comment|/**      * mutate the given shape so the returned shape is different      */
DECL|method|createMutation
specifier|protected
specifier|abstract
name|SB
name|createMutation
parameter_list|(
name|SB
name|original
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**      * Test that creates new shape from a random test shape and checks both for equality      */
DECL|method|testFromXContent
specifier|public
name|void
name|testFromXContent
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
name|NUMBER_OF_TESTBUILDERS
condition|;
name|runs
operator|++
control|)
block|{
name|SB
name|testShape
init|=
name|createTestShapeBuilder
argument_list|()
decl_stmt|;
name|XContentBuilder
name|contentBuilder
init|=
name|XContentFactory
operator|.
name|contentBuilder
argument_list|(
name|randomFrom
argument_list|(
name|XContentType
operator|.
name|values
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|contentBuilder
operator|.
name|prettyPrint
argument_list|()
expr_stmt|;
block|}
name|XContentBuilder
name|builder
init|=
name|testShape
operator|.
name|toXContent
argument_list|(
name|contentBuilder
argument_list|,
name|ToXContent
operator|.
name|EMPTY_PARAMS
argument_list|)
decl_stmt|;
name|XContentBuilder
name|shuffled
init|=
name|shuffleXContent
argument_list|(
name|builder
argument_list|)
decl_stmt|;
name|XContentParser
name|shapeParser
init|=
name|createParser
argument_list|(
name|shuffled
argument_list|)
decl_stmt|;
name|shapeParser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
name|ShapeBuilder
name|parsedShape
init|=
name|ShapeBuilder
operator|.
name|parse
argument_list|(
name|shapeParser
argument_list|)
decl_stmt|;
name|assertNotSame
argument_list|(
name|testShape
argument_list|,
name|parsedShape
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|testShape
argument_list|,
name|parsedShape
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|testShape
operator|.
name|hashCode
argument_list|()
argument_list|,
name|parsedShape
operator|.
name|hashCode
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      * Test serialization and deserialization of the test shape.      */
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
name|NUMBER_OF_TESTBUILDERS
condition|;
name|runs
operator|++
control|)
block|{
name|SB
name|testShape
init|=
name|createTestShapeBuilder
argument_list|()
decl_stmt|;
name|SB
name|deserializedShape
init|=
name|copyShape
argument_list|(
name|testShape
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|testShape
argument_list|,
name|deserializedShape
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|testShape
operator|.
name|hashCode
argument_list|()
argument_list|,
name|deserializedShape
operator|.
name|hashCode
argument_list|()
argument_list|)
expr_stmt|;
name|assertNotSame
argument_list|(
name|testShape
argument_list|,
name|deserializedShape
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      * Test equality and hashCode properties      */
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
name|NUMBER_OF_TESTBUILDERS
condition|;
name|runs
operator|++
control|)
block|{
name|checkEqualsAndHashCode
argument_list|(
name|createTestShapeBuilder
argument_list|()
argument_list|,
name|AbstractShapeBuilderTestCase
operator|::
name|copyShape
argument_list|,
name|this
operator|::
name|createMutation
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|copyShape
specifier|protected
specifier|static
parameter_list|<
name|T
extends|extends
name|NamedWriteable
parameter_list|>
name|T
name|copyShape
parameter_list|(
name|T
name|original
parameter_list|)
throws|throws
name|IOException
block|{
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
name|Reader
argument_list|<
name|T
argument_list|>
name|reader
init|=
operator|(
name|Reader
argument_list|<
name|T
argument_list|>
operator|)
name|namedWriteableRegistry
operator|.
name|getReader
argument_list|(
name|ShapeBuilder
operator|.
name|class
argument_list|,
name|original
operator|.
name|getWriteableName
argument_list|()
argument_list|)
decl_stmt|;
return|return
name|ESTestCase
operator|.
name|copyWriteable
argument_list|(
name|original
argument_list|,
name|namedWriteableRegistry
argument_list|,
name|reader
argument_list|)
return|;
block|}
block|}
end_class

end_unit

