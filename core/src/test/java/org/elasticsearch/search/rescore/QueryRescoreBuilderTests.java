begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.rescore
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|rescore
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchParseException
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
name|ParseFieldMatcher
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
name|ParsingException
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
name|core
operator|.
name|StringFieldMapper
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
name|search
operator|.
name|rescore
operator|.
name|QueryRescorer
operator|.
name|QueryRescoreContext
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
DECL|class|QueryRescoreBuilderTests
specifier|public
class|class
name|QueryRescoreBuilderTests
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
DECL|field|indicesQueriesRegistry
specifier|private
specifier|static
name|IndicesQueriesRegistry
name|indicesQueriesRegistry
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
name|RescoreBuilder
operator|.
name|class
argument_list|,
name|QueryRescorerBuilder
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
name|indicesQueriesRegistry
operator|=
literal|null
expr_stmt|;
block|}
comment|/**      * Test serialization and deserialization of the rescore builder      */
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
name|RescoreBuilder
argument_list|<
name|?
argument_list|>
name|original
init|=
name|randomRescoreBuilder
argument_list|()
decl_stmt|;
name|RescoreBuilder
argument_list|<
name|?
argument_list|>
name|deserialized
init|=
name|serializedCopy
argument_list|(
name|original
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|deserialized
argument_list|,
name|original
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|deserialized
operator|.
name|hashCode
argument_list|()
argument_list|,
name|original
operator|.
name|hashCode
argument_list|()
argument_list|)
expr_stmt|;
name|assertNotSame
argument_list|(
name|deserialized
argument_list|,
name|original
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
name|RescoreBuilder
argument_list|<
name|?
argument_list|>
name|firstBuilder
init|=
name|randomRescoreBuilder
argument_list|()
decl_stmt|;
name|assertFalse
argument_list|(
literal|"rescore builder is equal to null"
argument_list|,
name|firstBuilder
operator|.
name|equals
argument_list|(
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
literal|"rescore builder is equal to incompatible type"
argument_list|,
name|firstBuilder
operator|.
name|equals
argument_list|(
literal|""
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"rescore builder is not equal to self"
argument_list|,
name|firstBuilder
operator|.
name|equals
argument_list|(
name|firstBuilder
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"same rescore builder's hashcode returns different values if called multiple times"
argument_list|,
name|firstBuilder
operator|.
name|hashCode
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|firstBuilder
operator|.
name|hashCode
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"different rescore builder should not be equal"
argument_list|,
name|mutate
argument_list|(
name|firstBuilder
argument_list|)
argument_list|,
name|not
argument_list|(
name|equalTo
argument_list|(
name|firstBuilder
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|RescoreBuilder
argument_list|<
name|?
argument_list|>
name|secondBuilder
init|=
name|serializedCopy
argument_list|(
name|firstBuilder
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"rescore builder is not equal to self"
argument_list|,
name|secondBuilder
operator|.
name|equals
argument_list|(
name|secondBuilder
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"rescore builder is not equal to its copy"
argument_list|,
name|firstBuilder
operator|.
name|equals
argument_list|(
name|secondBuilder
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"equals is not symmetric"
argument_list|,
name|secondBuilder
operator|.
name|equals
argument_list|(
name|firstBuilder
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"rescore builder copy's hashcode is different from original hashcode"
argument_list|,
name|secondBuilder
operator|.
name|hashCode
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|firstBuilder
operator|.
name|hashCode
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|RescoreBuilder
argument_list|<
name|?
argument_list|>
name|thirdBuilder
init|=
name|serializedCopy
argument_list|(
name|secondBuilder
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"rescore builder is not equal to self"
argument_list|,
name|thirdBuilder
operator|.
name|equals
argument_list|(
name|thirdBuilder
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"rescore builder is not equal to its copy"
argument_list|,
name|secondBuilder
operator|.
name|equals
argument_list|(
name|thirdBuilder
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"rescore builder copy's hashcode is different from original hashcode"
argument_list|,
name|secondBuilder
operator|.
name|hashCode
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|thirdBuilder
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
name|firstBuilder
operator|.
name|equals
argument_list|(
name|thirdBuilder
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"rescore builder copy's hashcode is different from original hashcode"
argument_list|,
name|firstBuilder
operator|.
name|hashCode
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|thirdBuilder
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
name|thirdBuilder
operator|.
name|equals
argument_list|(
name|secondBuilder
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"equals is not symmetric"
argument_list|,
name|thirdBuilder
operator|.
name|equals
argument_list|(
name|firstBuilder
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      *  creates random rescorer, renders it to xContent and back to new instance that should be equal to original      */
DECL|method|testFromXContent
specifier|public
name|void
name|testFromXContent
parameter_list|()
throws|throws
name|IOException
block|{
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
name|parseFieldMatcher
argument_list|(
operator|new
name|ParseFieldMatcher
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
argument_list|)
expr_stmt|;
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
name|RescoreBuilder
argument_list|<
name|?
argument_list|>
name|rescoreBuilder
init|=
name|randomRescoreBuilder
argument_list|()
decl_stmt|;
name|XContentParser
name|parser
init|=
name|createParser
argument_list|(
name|rescoreBuilder
argument_list|)
decl_stmt|;
name|context
operator|.
name|reset
argument_list|(
name|parser
argument_list|)
expr_stmt|;
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
name|RescoreBuilder
argument_list|<
name|?
argument_list|>
name|secondRescoreBuilder
init|=
name|RescoreBuilder
operator|.
name|parseFromXContent
argument_list|(
name|context
argument_list|)
decl_stmt|;
name|assertNotSame
argument_list|(
name|rescoreBuilder
argument_list|,
name|secondRescoreBuilder
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|rescoreBuilder
argument_list|,
name|secondRescoreBuilder
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|rescoreBuilder
operator|.
name|hashCode
argument_list|()
argument_list|,
name|secondRescoreBuilder
operator|.
name|hashCode
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|createParser
specifier|private
specifier|static
name|XContentParser
name|createParser
parameter_list|(
name|RescoreBuilder
argument_list|<
name|?
argument_list|>
name|rescoreBuilder
parameter_list|)
throws|throws
name|IOException
block|{
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
name|rescoreBuilder
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
return|return
name|XContentHelper
operator|.
name|createParser
argument_list|(
name|builder
operator|.
name|bytes
argument_list|()
argument_list|)
return|;
block|}
comment|/**      * test that build() outputs a {@link RescoreSearchContext} that is similar to the one      * we would get when parsing the xContent the test rescore builder is rendering out      */
DECL|method|testBuildRescoreSearchContext
specifier|public
name|void
name|testBuildRescoreSearchContext
parameter_list|()
throws|throws
name|ElasticsearchParseException
throws|,
name|IOException
block|{
name|Settings
name|indexSettings
init|=
name|Settings
operator|.
name|settingsBuilder
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
decl_stmt|;
name|IndexSettings
name|idxSettings
init|=
name|IndexSettingsModule
operator|.
name|newIndexSettings
argument_list|(
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
argument_list|,
name|indexSettings
argument_list|)
decl_stmt|;
comment|// shard context will only need indicesQueriesRegistry for building Query objects nested in query rescorer
name|QueryShardContext
name|mockShardContext
init|=
operator|new
name|QueryShardContext
argument_list|(
name|idxSettings
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|indicesQueriesRegistry
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|MappedFieldType
name|fieldMapper
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|StringFieldMapper
operator|.
name|Builder
name|builder
init|=
operator|new
name|StringFieldMapper
operator|.
name|Builder
argument_list|(
name|name
argument_list|)
decl_stmt|;
return|return
name|builder
operator|.
name|build
argument_list|(
operator|new
name|Mapper
operator|.
name|BuilderContext
argument_list|(
name|idxSettings
operator|.
name|getSettings
argument_list|()
argument_list|,
operator|new
name|ContentPath
argument_list|(
literal|1
argument_list|)
argument_list|)
argument_list|)
operator|.
name|fieldType
argument_list|()
return|;
block|}
block|}
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
name|RescoreBuilder
argument_list|<
name|?
argument_list|>
name|rescoreBuilder
init|=
name|randomRescoreBuilder
argument_list|()
decl_stmt|;
name|QueryRescoreContext
name|rescoreContext
init|=
name|rescoreBuilder
operator|.
name|build
argument_list|(
name|mockShardContext
argument_list|)
decl_stmt|;
name|XContentParser
name|parser
init|=
name|createParser
argument_list|(
name|rescoreBuilder
argument_list|)
decl_stmt|;
name|QueryRescoreContext
name|parsedRescoreContext
init|=
operator|(
name|QueryRescoreContext
operator|)
operator|new
name|RescoreParseElement
argument_list|()
operator|.
name|parseSingleRescoreContext
argument_list|(
name|parser
argument_list|,
name|mockShardContext
argument_list|)
decl_stmt|;
name|assertNotSame
argument_list|(
name|rescoreContext
argument_list|,
name|parsedRescoreContext
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|rescoreContext
operator|.
name|window
argument_list|()
argument_list|,
name|parsedRescoreContext
operator|.
name|window
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|rescoreContext
operator|.
name|query
argument_list|()
argument_list|,
name|parsedRescoreContext
operator|.
name|query
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|rescoreContext
operator|.
name|queryWeight
argument_list|()
argument_list|,
name|parsedRescoreContext
operator|.
name|queryWeight
argument_list|()
argument_list|,
name|Float
operator|.
name|MIN_VALUE
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|rescoreContext
operator|.
name|rescoreQueryWeight
argument_list|()
argument_list|,
name|parsedRescoreContext
operator|.
name|rescoreQueryWeight
argument_list|()
argument_list|,
name|Float
operator|.
name|MIN_VALUE
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|rescoreContext
operator|.
name|scoreMode
argument_list|()
argument_list|,
name|parsedRescoreContext
operator|.
name|scoreMode
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      * test parsing exceptions for incorrect rescorer syntax      */
DECL|method|testUnknownFieldsExpection
specifier|public
name|void
name|testUnknownFieldsExpection
parameter_list|()
throws|throws
name|IOException
block|{
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
name|parseFieldMatcher
argument_list|(
operator|new
name|ParseFieldMatcher
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|rescoreElement
init|=
literal|"{\n"
operator|+
literal|"    \"window_size\" : 20,\n"
operator|+
literal|"    \"bad_rescorer_name\" : { }\n"
operator|+
literal|"}\n"
decl_stmt|;
name|prepareContext
argument_list|(
name|context
argument_list|,
name|rescoreElement
argument_list|)
expr_stmt|;
try|try
block|{
name|RescoreBuilder
operator|.
name|parseFromXContent
argument_list|(
name|context
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"expected a parsing exception"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ParsingException
name|e
parameter_list|)
block|{
name|assertEquals
argument_list|(
literal|"rescore doesn't support rescorer with name [bad_rescorer_name]"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|rescoreElement
operator|=
literal|"{\n"
operator|+
literal|"    \"bad_fieldName\" : 20\n"
operator|+
literal|"}\n"
expr_stmt|;
name|prepareContext
argument_list|(
name|context
argument_list|,
name|rescoreElement
argument_list|)
expr_stmt|;
try|try
block|{
name|RescoreBuilder
operator|.
name|parseFromXContent
argument_list|(
name|context
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"expected a parsing exception"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ParsingException
name|e
parameter_list|)
block|{
name|assertEquals
argument_list|(
literal|"rescore doesn't support [bad_fieldName]"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|rescoreElement
operator|=
literal|"{\n"
operator|+
literal|"    \"window_size\" : 20,\n"
operator|+
literal|"    \"query\" : [ ]\n"
operator|+
literal|"}\n"
expr_stmt|;
name|prepareContext
argument_list|(
name|context
argument_list|,
name|rescoreElement
argument_list|)
expr_stmt|;
try|try
block|{
name|RescoreBuilder
operator|.
name|parseFromXContent
argument_list|(
name|context
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"expected a parsing exception"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ParsingException
name|e
parameter_list|)
block|{
name|assertEquals
argument_list|(
literal|"unexpected token [START_ARRAY] after [query]"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|rescoreElement
operator|=
literal|"{ }"
expr_stmt|;
name|prepareContext
argument_list|(
name|context
argument_list|,
name|rescoreElement
argument_list|)
expr_stmt|;
try|try
block|{
name|RescoreBuilder
operator|.
name|parseFromXContent
argument_list|(
name|context
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"expected a parsing exception"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ParsingException
name|e
parameter_list|)
block|{
name|assertEquals
argument_list|(
literal|"missing rescore type"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|rescoreElement
operator|=
literal|"{\n"
operator|+
literal|"    \"window_size\" : 20,\n"
operator|+
literal|"    \"query\" : { \"bad_fieldname\" : 1.0  } \n"
operator|+
literal|"}\n"
expr_stmt|;
name|prepareContext
argument_list|(
name|context
argument_list|,
name|rescoreElement
argument_list|)
expr_stmt|;
try|try
block|{
name|RescoreBuilder
operator|.
name|parseFromXContent
argument_list|(
name|context
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"expected a parsing exception"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
name|assertEquals
argument_list|(
literal|"[query] unknown field [bad_fieldname], parser not found"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|rescoreElement
operator|=
literal|"{\n"
operator|+
literal|"    \"window_size\" : 20,\n"
operator|+
literal|"    \"query\" : { \"rescore_query\" : { \"unknown_queryname\" : { } } } \n"
operator|+
literal|"}\n"
expr_stmt|;
name|prepareContext
argument_list|(
name|context
argument_list|,
name|rescoreElement
argument_list|)
expr_stmt|;
try|try
block|{
name|RescoreBuilder
operator|.
name|parseFromXContent
argument_list|(
name|context
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"expected a parsing exception"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ParsingException
name|e
parameter_list|)
block|{
name|assertEquals
argument_list|(
literal|"[query] failed to parse field [rescore_query]"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|rescoreElement
operator|=
literal|"{\n"
operator|+
literal|"    \"window_size\" : 20,\n"
operator|+
literal|"    \"query\" : { \"rescore_query\" : { \"match_all\" : { } } } \n"
operator|+
literal|"}\n"
expr_stmt|;
name|prepareContext
argument_list|(
name|context
argument_list|,
name|rescoreElement
argument_list|)
expr_stmt|;
name|RescoreBuilder
operator|.
name|parseFromXContent
argument_list|(
name|context
argument_list|)
expr_stmt|;
block|}
comment|/**      * create a new parser from the rescorer string representation and reset context with it      */
DECL|method|prepareContext
specifier|private
specifier|static
name|void
name|prepareContext
parameter_list|(
name|QueryParseContext
name|context
parameter_list|,
name|String
name|rescoreElement
parameter_list|)
throws|throws
name|IOException
block|{
name|XContentParser
name|parser
init|=
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|rescoreElement
argument_list|)
operator|.
name|createParser
argument_list|(
name|rescoreElement
argument_list|)
decl_stmt|;
name|context
operator|.
name|reset
argument_list|(
name|parser
argument_list|)
expr_stmt|;
comment|// move to first token, this is where the internal fromXContent
name|assertTrue
argument_list|(
name|parser
operator|.
name|nextToken
argument_list|()
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|START_OBJECT
argument_list|)
expr_stmt|;
block|}
DECL|method|mutate
specifier|private
specifier|static
name|RescoreBuilder
argument_list|<
name|?
argument_list|>
name|mutate
parameter_list|(
name|RescoreBuilder
argument_list|<
name|?
argument_list|>
name|original
parameter_list|)
throws|throws
name|IOException
block|{
name|RescoreBuilder
argument_list|<
name|?
argument_list|>
name|mutation
init|=
name|serializedCopy
argument_list|(
name|original
argument_list|)
decl_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|Integer
name|windowSize
init|=
name|original
operator|.
name|windowSize
argument_list|()
decl_stmt|;
if|if
condition|(
name|windowSize
operator|!=
literal|null
condition|)
block|{
name|mutation
operator|.
name|windowSize
argument_list|(
name|windowSize
operator|+
literal|1
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|mutation
operator|.
name|windowSize
argument_list|(
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|100
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|QueryRescorerBuilder
name|queryRescorer
init|=
operator|(
name|QueryRescorerBuilder
operator|)
name|mutation
decl_stmt|;
switch|switch
condition|(
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|3
argument_list|)
condition|)
block|{
case|case
literal|0
case|:
name|queryRescorer
operator|.
name|setQueryWeight
argument_list|(
name|queryRescorer
operator|.
name|getQueryWeight
argument_list|()
operator|+
literal|0.1f
argument_list|)
expr_stmt|;
break|break;
case|case
literal|1
case|:
name|queryRescorer
operator|.
name|setRescoreQueryWeight
argument_list|(
name|queryRescorer
operator|.
name|getRescoreQueryWeight
argument_list|()
operator|+
literal|0.1f
argument_list|)
expr_stmt|;
break|break;
case|case
literal|2
case|:
name|QueryRescoreMode
name|other
decl_stmt|;
do|do
block|{
name|other
operator|=
name|randomFrom
argument_list|(
name|QueryRescoreMode
operator|.
name|values
argument_list|()
argument_list|)
expr_stmt|;
block|}
do|while
condition|(
name|other
operator|==
name|queryRescorer
operator|.
name|getScoreMode
argument_list|()
condition|)
do|;
name|queryRescorer
operator|.
name|setScoreMode
argument_list|(
name|other
argument_list|)
expr_stmt|;
break|break;
case|case
literal|3
case|:
comment|// only increase the boost to make it a slightly different query
name|queryRescorer
operator|.
name|getRescoreQuery
argument_list|()
operator|.
name|boost
argument_list|(
name|queryRescorer
operator|.
name|getRescoreQuery
argument_list|()
operator|.
name|boost
argument_list|()
operator|+
literal|0.1f
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"unexpected random mutation in test"
argument_list|)
throw|;
block|}
block|}
return|return
name|mutation
return|;
block|}
comment|/**      * create random shape that is put under test      */
DECL|method|randomRescoreBuilder
specifier|public
specifier|static
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|rescore
operator|.
name|QueryRescorerBuilder
name|randomRescoreBuilder
parameter_list|()
block|{
name|QueryBuilder
argument_list|<
name|MatchAllQueryBuilder
argument_list|>
name|queryBuilder
init|=
operator|new
name|MatchAllQueryBuilder
argument_list|()
operator|.
name|boost
argument_list|(
name|randomFloat
argument_list|()
argument_list|)
operator|.
name|queryName
argument_list|(
name|randomAsciiOfLength
argument_list|(
literal|20
argument_list|)
argument_list|)
decl_stmt|;
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|rescore
operator|.
name|QueryRescorerBuilder
name|rescorer
init|=
operator|new
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|rescore
operator|.
name|QueryRescorerBuilder
argument_list|(
name|queryBuilder
argument_list|)
decl_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|rescorer
operator|.
name|setQueryWeight
argument_list|(
name|randomFloat
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|rescorer
operator|.
name|setRescoreQueryWeight
argument_list|(
name|randomFloat
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|rescorer
operator|.
name|setScoreMode
argument_list|(
name|randomFrom
argument_list|(
name|QueryRescoreMode
operator|.
name|values
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|rescorer
operator|.
name|windowSize
argument_list|(
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|100
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|rescorer
return|;
block|}
DECL|method|serializedCopy
specifier|private
specifier|static
name|RescoreBuilder
argument_list|<
name|?
argument_list|>
name|serializedCopy
parameter_list|(
name|RescoreBuilder
argument_list|<
name|?
argument_list|>
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
name|output
operator|.
name|writeRescorer
argument_list|(
name|original
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
return|return
name|in
operator|.
name|readRescorer
argument_list|()
return|;
block|}
block|}
block|}
block|}
end_class

end_unit

