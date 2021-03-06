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
name|apache
operator|.
name|lucene
operator|.
name|search
operator|.
name|SortField
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|Accountable
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|Version
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|metadata
operator|.
name|IndexMetaData
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
name|NamedXContentRegistry
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
name|env
operator|.
name|Environment
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
name|Index
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
name|IndexSettings
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
name|cache
operator|.
name|bitset
operator|.
name|BitsetFilterCache
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
name|fielddata
operator|.
name|IndexFieldDataService
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
name|mapper
operator|.
name|ContentPath
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
name|mapper
operator|.
name|MappedFieldType
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
name|mapper
operator|.
name|Mapper
operator|.
name|BuilderContext
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
name|mapper
operator|.
name|NumberFieldMapper
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
name|mapper
operator|.
name|ObjectMapper
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
name|mapper
operator|.
name|ObjectMapper
operator|.
name|Nested
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
name|IdsQueryBuilder
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
name|MatchAllQueryBuilder
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
name|QueryBuilder
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
name|index
operator|.
name|query
operator|.
name|QueryShardContext
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
name|TermQueryBuilder
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
name|shard
operator|.
name|ShardId
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
name|fielddata
operator|.
name|cache
operator|.
name|IndicesFieldDataCache
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|MockScriptEngine
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|ScriptContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|ScriptEngine
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|ScriptModule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|ScriptService
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
name|DocValueFormat
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
name|elasticsearch
operator|.
name|test
operator|.
name|IndexSettingsModule
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
name|nio
operator|.
name|file
operator|.
name|Path
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
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|function
operator|.
name|Function
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|util
operator|.
name|Collections
operator|.
name|emptyList
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
DECL|class|AbstractSortTestCase
specifier|public
specifier|abstract
class|class
name|AbstractSortTestCase
parameter_list|<
name|T
extends|extends
name|SortBuilder
parameter_list|<
name|T
parameter_list|>
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
specifier|protected
specifier|static
name|NamedWriteableRegistry
name|namedWriteableRegistry
decl_stmt|;
DECL|field|xContentRegistry
specifier|private
specifier|static
name|NamedXContentRegistry
name|xContentRegistry
decl_stmt|;
DECL|field|scriptService
specifier|private
specifier|static
name|ScriptService
name|scriptService
decl_stmt|;
annotation|@
name|BeforeClass
DECL|method|init
specifier|public
specifier|static
name|void
name|init
parameter_list|()
throws|throws
name|IOException
block|{
name|Path
name|genericConfigFolder
init|=
name|createTempDir
argument_list|()
decl_stmt|;
name|Settings
name|baseSettings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|Environment
operator|.
name|PATH_HOME_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
name|createTempDir
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|Environment
operator|.
name|PATH_CONF_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
name|genericConfigFolder
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Function
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|,
name|Object
argument_list|>
argument_list|>
name|scripts
init|=
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"dummy"
argument_list|,
name|p
lambda|->
literal|null
argument_list|)
decl_stmt|;
name|ScriptEngine
name|engine
init|=
operator|new
name|MockScriptEngine
argument_list|(
name|MockScriptEngine
operator|.
name|NAME
argument_list|,
name|scripts
argument_list|)
decl_stmt|;
name|scriptService
operator|=
operator|new
name|ScriptService
argument_list|(
name|baseSettings
argument_list|,
name|Collections
operator|.
name|singletonMap
argument_list|(
name|engine
operator|.
name|getType
argument_list|()
argument_list|,
name|engine
argument_list|)
argument_list|,
name|ScriptModule
operator|.
name|CORE_CONTEXTS
argument_list|)
expr_stmt|;
name|SearchModule
name|searchModule
init|=
operator|new
name|SearchModule
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
literal|false
argument_list|,
name|emptyList
argument_list|()
argument_list|)
decl_stmt|;
name|namedWriteableRegistry
operator|=
operator|new
name|NamedWriteableRegistry
argument_list|(
name|searchModule
operator|.
name|getNamedWriteables
argument_list|()
argument_list|)
expr_stmt|;
name|xContentRegistry
operator|=
operator|new
name|NamedXContentRegistry
argument_list|(
name|searchModule
operator|.
name|getNamedXContents
argument_list|()
argument_list|)
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
name|xContentRegistry
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
comment|/** Parse the sort from xContent. Just delegate to the SortBuilder's static fromXContent method. */
DECL|method|fromXContent
specifier|protected
specifier|abstract
name|T
name|fromXContent
parameter_list|(
name|QueryParseContext
name|context
parameter_list|,
name|String
name|fieldName
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
name|XContentBuilder
name|shuffled
init|=
name|shuffleXContent
argument_list|(
name|builder
argument_list|)
decl_stmt|;
name|XContentParser
name|itemParser
init|=
name|createParser
argument_list|(
name|shuffled
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
name|itemParser
argument_list|)
decl_stmt|;
name|T
name|parsedItem
init|=
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
comment|/**      * test that build() outputs a {@link SortField} that is similar to the one      * we would get when parsing the xContent the sort builder is rendering out      */
DECL|method|testBuildSortField
specifier|public
name|void
name|testBuildSortField
parameter_list|()
throws|throws
name|IOException
block|{
name|QueryShardContext
name|mockShardContext
init|=
name|createMockShardContext
argument_list|()
decl_stmt|;
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
name|sortBuilder
init|=
name|createTestItem
argument_list|()
decl_stmt|;
name|SortFieldAndFormat
name|sortField
init|=
name|sortBuilder
operator|.
name|build
argument_list|(
name|mockShardContext
argument_list|)
decl_stmt|;
name|sortFieldAssertions
argument_list|(
name|sortBuilder
argument_list|,
name|sortField
operator|.
name|field
argument_list|,
name|sortField
operator|.
name|format
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|sortFieldAssertions
specifier|protected
specifier|abstract
name|void
name|sortFieldAssertions
parameter_list|(
name|T
name|builder
parameter_list|,
name|SortField
name|sortField
parameter_list|,
name|DocValueFormat
name|format
parameter_list|)
throws|throws
name|IOException
function_decl|;
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
name|copy
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
name|checkEqualsAndHashCode
argument_list|(
name|createTestItem
argument_list|()
argument_list|,
name|this
operator|::
name|copy
argument_list|,
name|this
operator|::
name|mutate
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|createMockShardContext
specifier|protected
name|QueryShardContext
name|createMockShardContext
parameter_list|()
block|{
name|Index
name|index
init|=
operator|new
name|Index
argument_list|(
name|randomAlphaOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
argument_list|,
literal|"_na_"
argument_list|)
decl_stmt|;
name|IndexSettings
name|idxSettings
init|=
name|IndexSettingsModule
operator|.
name|newIndexSettings
argument_list|(
name|index
argument_list|,
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_VERSION_CREATED
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|IndicesFieldDataCache
name|cache
init|=
operator|new
name|IndicesFieldDataCache
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|IndexFieldDataService
name|ifds
init|=
operator|new
name|IndexFieldDataService
argument_list|(
name|IndexSettingsModule
operator|.
name|newIndexSettings
argument_list|(
literal|"test"
argument_list|,
name|Settings
operator|.
name|EMPTY
argument_list|)
argument_list|,
name|cache
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|BitsetFilterCache
name|bitsetFilterCache
init|=
operator|new
name|BitsetFilterCache
argument_list|(
name|idxSettings
argument_list|,
operator|new
name|BitsetFilterCache
operator|.
name|Listener
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onRemoval
parameter_list|(
name|ShardId
name|shardId
parameter_list|,
name|Accountable
name|accountable
parameter_list|)
block|{             }
annotation|@
name|Override
specifier|public
name|void
name|onCache
parameter_list|(
name|ShardId
name|shardId
parameter_list|,
name|Accountable
name|accountable
parameter_list|)
block|{             }
block|}
argument_list|)
decl_stmt|;
name|long
name|nowInMillis
init|=
name|randomNonNegativeLong
argument_list|()
decl_stmt|;
return|return
operator|new
name|QueryShardContext
argument_list|(
literal|0
argument_list|,
name|idxSettings
argument_list|,
name|bitsetFilterCache
argument_list|,
name|ifds
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|scriptService
argument_list|,
name|xContentRegistry
argument_list|()
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
parameter_list|()
lambda|->
name|nowInMillis
argument_list|)
block|{             @
name|Override
specifier|public
name|MappedFieldType
name|fieldMapper
argument_list|(
name|String
name|name
argument_list|)
block|{
return|return
name|provideMappedFieldType
argument_list|(
name|name
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|ObjectMapper
name|getObjectMapper
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|BuilderContext
name|context
init|=
operator|new
name|BuilderContext
argument_list|(
name|this
operator|.
name|getIndexSettings
argument_list|()
operator|.
name|getSettings
argument_list|()
argument_list|,
operator|new
name|ContentPath
argument_list|()
argument_list|)
decl_stmt|;
return|return
operator|new
name|ObjectMapper
operator|.
name|Builder
argument_list|<>
argument_list|(
name|name
argument_list|)
operator|.
name|nested
argument_list|(
name|Nested
operator|.
name|newNested
argument_list|(
literal|false
argument_list|,
literal|false
argument_list|)
argument_list|)
operator|.
name|build
argument_list|(
name|context
argument_list|)
return|;
block|}
block|}
empty_stmt|;
block|}
end_class

begin_comment
comment|/**      * Return a field type. We use {@link NumberFieldMapper.NumberFieldType} by default since it is compatible with all sort modes      * Tests that require other field type than double can override this.      */
end_comment

begin_function
DECL|method|provideMappedFieldType
specifier|protected
name|MappedFieldType
name|provideMappedFieldType
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|NumberFieldMapper
operator|.
name|NumberFieldType
name|doubleFieldType
init|=
operator|new
name|NumberFieldMapper
operator|.
name|NumberFieldType
argument_list|(
name|NumberFieldMapper
operator|.
name|NumberType
operator|.
name|DOUBLE
argument_list|)
decl_stmt|;
name|doubleFieldType
operator|.
name|setName
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|doubleFieldType
operator|.
name|setHasDocValues
argument_list|(
literal|true
argument_list|)
expr_stmt|;
return|return
name|doubleFieldType
return|;
block|}
end_function

begin_function
annotation|@
name|Override
DECL|method|xContentRegistry
specifier|protected
name|NamedXContentRegistry
name|xContentRegistry
parameter_list|()
block|{
return|return
name|xContentRegistry
return|;
block|}
end_function

begin_function
DECL|method|randomNestedFilter
specifier|protected
specifier|static
name|QueryBuilder
name|randomNestedFilter
parameter_list|()
block|{
name|int
name|id
init|=
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|2
argument_list|)
decl_stmt|;
switch|switch
condition|(
name|id
condition|)
block|{
case|case
literal|0
case|:
return|return
operator|(
operator|new
name|MatchAllQueryBuilder
argument_list|()
operator|)
operator|.
name|boost
argument_list|(
name|randomFloat
argument_list|()
argument_list|)
return|;
case|case
literal|1
case|:
return|return
operator|(
operator|new
name|IdsQueryBuilder
argument_list|()
operator|)
operator|.
name|boost
argument_list|(
name|randomFloat
argument_list|()
argument_list|)
return|;
case|case
literal|2
case|:
return|return
operator|(
operator|new
name|TermQueryBuilder
argument_list|(
name|randomAlphaOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
argument_list|,
name|randomDouble
argument_list|()
argument_list|)
operator|.
name|boost
argument_list|(
name|randomFloat
argument_list|()
argument_list|)
operator|)
return|;
default|default:
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Only three query builders supported for testing sort"
argument_list|)
throw|;
block|}
block|}
end_function

begin_function
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|method|copy
specifier|private
name|T
name|copy
parameter_list|(
name|T
name|original
parameter_list|)
throws|throws
name|IOException
block|{
comment|/* The cast below is required to make Java 9 happy. Java 8 infers the T in copyWriterable to be the same as AbstractSortTestCase's          * T but Java 9 infers it to be SortBuilder. */
return|return
operator|(
name|T
operator|)
name|copyWriteable
argument_list|(
name|original
argument_list|,
name|namedWriteableRegistry
argument_list|,
name|namedWriteableRegistry
operator|.
name|getReader
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
argument_list|)
return|;
block|}
end_function

unit|}
end_unit

