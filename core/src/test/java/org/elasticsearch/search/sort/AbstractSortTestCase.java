begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|settings
operator|.
name|Settings
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
name|indices
operator|.
name|query
operator|.
name|IndicesQueriesRegistry
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|SearchModule
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

begin_class
DECL|class|AbstractSortTestCase
specifier|public
specifier|abstract
class|class
name|AbstractSortTestCase
parameter_list|<
name|T
extends|extends
name|SortBuilder
operator|&
name|NamedWriteable
parameter_list|<
name|T
parameter_list|>
operator|&
name|SortElementParserTemp
parameter_list|<
name|T
parameter_list|>
parameter_list|>
extends|extends
name|ESTestCase
block|{
DECL|field|namedWriteableRegistry
specifier|protected
specifier|static
name|NamedWriteableRegistry
name|namedWriteableRegistry
decl_stmt|;
DECL|field|NUMBER_OF_TESTBUILDERS
specifier|private
specifier|static
specifier|final
name|int
name|NUMBER_OF_TESTBUILDERS
init|=
literal|20
decl_stmt|;
DECL|field|indicesQueriesRegistry
specifier|static
name|IndicesQueriesRegistry
name|indicesQueriesRegistry
decl_stmt|;
annotation|@
name|BeforeClass
DECL|method|init
specifier|public
specifier|static
name|void
name|init
parameter_list|()
block|{
name|namedWriteableRegistry
operator|=
operator|new
name|NamedWriteableRegistry
argument_list|()
expr_stmt|;
name|namedWriteableRegistry
operator|.
name|registerPrototype
argument_list|(
name|SortBuilder
operator|.
name|class
argument_list|,
name|GeoDistanceSortBuilder
operator|.
name|PROTOTYPE
argument_list|)
expr_stmt|;
name|namedWriteableRegistry
operator|.
name|registerPrototype
argument_list|(
name|SortBuilder
operator|.
name|class
argument_list|,
name|ScoreSortBuilder
operator|.
name|PROTOTYPE
argument_list|)
expr_stmt|;
name|indicesQueriesRegistry
operator|=
operator|new
name|SearchModule
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
name|namedWriteableRegistry
argument_list|)
operator|.
name|buildQueryParserRegistry
argument_list|()
expr_stmt|;
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
comment|/** Returns random sort that is put under test */
DECL|method|createTestItem
specifier|protected
specifier|abstract
name|T
name|createTestItem
parameter_list|()
function_decl|;
comment|/** Returns mutated version of original so the returned sort is different in terms of equals/hashcode */
DECL|method|mutate
specifier|protected
specifier|abstract
name|T
name|mutate
parameter_list|(
name|T
name|original
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**      * Test that creates new sort from a random test sort and checks both for equality      */
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
name|T
name|testItem
init|=
name|createTestItem
argument_list|()
decl_stmt|;
name|XContentBuilder
name|builder
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
name|builder
operator|.
name|prettyPrint
argument_list|()
expr_stmt|;
block|}
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|testItem
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|ToXContent
operator|.
name|EMPTY_PARAMS
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|XContentParser
name|itemParser
init|=
name|XContentHelper
operator|.
name|createParser
argument_list|(
name|builder
operator|.
name|bytes
argument_list|()
argument_list|)
decl_stmt|;
name|itemParser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
comment|/*              * filter out name of sort, or field name to sort on for element fieldSort              */
name|itemParser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
name|String
name|elementName
init|=
name|itemParser
operator|.
name|currentName
argument_list|()
decl_stmt|;
name|itemParser
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
argument_list|)
decl_stmt|;
name|context
operator|.
name|reset
argument_list|(
name|itemParser
argument_list|)
expr_stmt|;
name|SortBuilder
name|parsedItem
init|=
name|testItem
operator|.
name|fromXContent
argument_list|(
name|context
argument_list|,
name|elementName
argument_list|)
decl_stmt|;
name|assertNotSame
argument_list|(
name|testItem
argument_list|,
name|parsedItem
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|testItem
argument_list|,
name|parsedItem
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|testItem
operator|.
name|hashCode
argument_list|()
argument_list|,
name|parsedItem
operator|.
name|hashCode
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      * Test serialization and deserialization of the test sort.      */
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
name|T
name|testsort
init|=
name|createTestItem
argument_list|()
decl_stmt|;
name|T
name|deserializedsort
init|=
name|copyItem
argument_list|(
name|testsort
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|testsort
argument_list|,
name|deserializedsort
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|testsort
operator|.
name|hashCode
argument_list|()
argument_list|,
name|deserializedsort
operator|.
name|hashCode
argument_list|()
argument_list|)
expr_stmt|;
name|assertNotSame
argument_list|(
name|testsort
argument_list|,
name|deserializedsort
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
name|T
name|firstsort
init|=
name|createTestItem
argument_list|()
decl_stmt|;
name|assertFalse
argument_list|(
literal|"sort is equal to null"
argument_list|,
name|firstsort
operator|.
name|equals
argument_list|(
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
literal|"sort is equal to incompatible type"
argument_list|,
name|firstsort
operator|.
name|equals
argument_list|(
literal|""
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"sort is not equal to self"
argument_list|,
name|firstsort
operator|.
name|equals
argument_list|(
name|firstsort
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"same sort's hashcode returns different values if called multiple times"
argument_list|,
name|firstsort
operator|.
name|hashCode
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|firstsort
operator|.
name|hashCode
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"different sorts should not be equal"
argument_list|,
name|mutate
argument_list|(
name|firstsort
argument_list|)
argument_list|,
name|not
argument_list|(
name|equalTo
argument_list|(
name|firstsort
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"different sorts should have different hashcode"
argument_list|,
name|mutate
argument_list|(
name|firstsort
argument_list|)
operator|.
name|hashCode
argument_list|()
argument_list|,
name|not
argument_list|(
name|equalTo
argument_list|(
name|firstsort
operator|.
name|hashCode
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|T
name|secondsort
init|=
name|copyItem
argument_list|(
name|firstsort
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"sort is not equal to self"
argument_list|,
name|secondsort
operator|.
name|equals
argument_list|(
name|secondsort
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"sort is not equal to its copy"
argument_list|,
name|firstsort
operator|.
name|equals
argument_list|(
name|secondsort
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"equals is not symmetric"
argument_list|,
name|secondsort
operator|.
name|equals
argument_list|(
name|firstsort
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"sort copy's hashcode is different from original hashcode"
argument_list|,
name|secondsort
operator|.
name|hashCode
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|firstsort
operator|.
name|hashCode
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|T
name|thirdsort
init|=
name|copyItem
argument_list|(
name|secondsort
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"sort is not equal to self"
argument_list|,
name|thirdsort
operator|.
name|equals
argument_list|(
name|thirdsort
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"sort is not equal to its copy"
argument_list|,
name|secondsort
operator|.
name|equals
argument_list|(
name|thirdsort
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"sort copy's hashcode is different from original hashcode"
argument_list|,
name|secondsort
operator|.
name|hashCode
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|thirdsort
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
name|firstsort
operator|.
name|equals
argument_list|(
name|thirdsort
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"sort copy's hashcode is different from original hashcode"
argument_list|,
name|firstsort
operator|.
name|hashCode
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|thirdsort
operator|.
name|hashCode
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"equals is not symmetric"
argument_list|,
name|thirdsort
operator|.
name|equals
argument_list|(
name|secondsort
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"equals is not symmetric"
argument_list|,
name|thirdsort
operator|.
name|equals
argument_list|(
name|firstsort
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|method|copyItem
specifier|protected
name|T
name|copyItem
parameter_list|(
name|T
name|original
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
name|original
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
name|StreamInput
operator|.
name|wrap
argument_list|(
name|output
operator|.
name|bytes
argument_list|()
argument_list|)
argument_list|,
name|namedWriteableRegistry
argument_list|)
init|)
block|{
name|T
name|prototype
init|=
operator|(
name|T
operator|)
name|namedWriteableRegistry
operator|.
name|getPrototype
argument_list|(
name|SortBuilder
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
name|prototype
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
return|;
block|}
block|}
block|}
block|}
end_class

end_unit

