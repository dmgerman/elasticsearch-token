begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.suggest.phrase
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|suggest
operator|.
name|phrase
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
name|analysis
operator|.
name|core
operator|.
name|WhitespaceAnalyzer
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
name|analysis
operator|.
name|AnalysisService
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
name|analysis
operator|.
name|NamedAnalyzer
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
name|MapperService
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
name|mapper
operator|.
name|core
operator|.
name|StringFieldMapper
operator|.
name|StringFieldType
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
name|TextFieldMapper
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
name|IndicesModule
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
name|suggest
operator|.
name|phrase
operator|.
name|PhraseSuggestionContext
operator|.
name|DirectCandidateGenerator
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
name|function
operator|.
name|Consumer
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
DECL|class|DirectCandidateGeneratorTests
specifier|public
class|class
name|DirectCandidateGeneratorTests
extends|extends
name|ESTestCase
block|{
DECL|field|NUMBER_OF_RUNS
specifier|private
specifier|static
specifier|final
name|int
name|NUMBER_OF_RUNS
init|=
literal|20
decl_stmt|;
comment|/**      * Test serialization and deserialization of the generator      */
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
name|NUMBER_OF_RUNS
condition|;
name|runs
operator|++
control|)
block|{
name|DirectCandidateGeneratorBuilder
name|original
init|=
name|randomCandidateGenerator
argument_list|()
decl_stmt|;
name|DirectCandidateGeneratorBuilder
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
name|NUMBER_OF_RUNS
condition|;
name|runs
operator|++
control|)
block|{
name|DirectCandidateGeneratorBuilder
name|first
init|=
name|randomCandidateGenerator
argument_list|()
decl_stmt|;
name|assertFalse
argument_list|(
literal|"generator is equal to null"
argument_list|,
name|first
operator|.
name|equals
argument_list|(
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
literal|"generator is equal to incompatible type"
argument_list|,
name|first
operator|.
name|equals
argument_list|(
literal|""
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"generator is not equal to self"
argument_list|,
name|first
operator|.
name|equals
argument_list|(
name|first
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"same generator's hashcode returns different values if called multiple times"
argument_list|,
name|first
operator|.
name|hashCode
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|first
operator|.
name|hashCode
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|DirectCandidateGeneratorBuilder
name|second
init|=
name|serializedCopy
argument_list|(
name|first
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"generator is not equal to self"
argument_list|,
name|second
operator|.
name|equals
argument_list|(
name|second
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"generator is not equal to its copy"
argument_list|,
name|first
operator|.
name|equals
argument_list|(
name|second
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"equals is not symmetric"
argument_list|,
name|second
operator|.
name|equals
argument_list|(
name|first
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"generator copy's hashcode is different from original hashcode"
argument_list|,
name|second
operator|.
name|hashCode
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|first
operator|.
name|hashCode
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|DirectCandidateGeneratorBuilder
name|third
init|=
name|serializedCopy
argument_list|(
name|second
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"generator is not equal to self"
argument_list|,
name|third
operator|.
name|equals
argument_list|(
name|third
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"generator is not equal to its copy"
argument_list|,
name|second
operator|.
name|equals
argument_list|(
name|third
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"generator copy's hashcode is different from original hashcode"
argument_list|,
name|second
operator|.
name|hashCode
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|third
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
name|first
operator|.
name|equals
argument_list|(
name|third
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"generator copy's hashcode is different from original hashcode"
argument_list|,
name|first
operator|.
name|hashCode
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|third
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
name|third
operator|.
name|equals
argument_list|(
name|second
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"equals is not symmetric"
argument_list|,
name|third
operator|.
name|equals
argument_list|(
name|first
argument_list|)
argument_list|)
expr_stmt|;
comment|// test for non-equality, check that all fields are covered by changing one by one
name|first
operator|=
operator|new
name|DirectCandidateGeneratorBuilder
argument_list|(
literal|"aaa"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|first
argument_list|,
name|serializedCopy
argument_list|(
name|first
argument_list|)
argument_list|)
expr_stmt|;
name|second
operator|=
operator|new
name|DirectCandidateGeneratorBuilder
argument_list|(
literal|"bbb"
argument_list|)
expr_stmt|;
name|assertNotEquals
argument_list|(
name|first
argument_list|,
name|second
argument_list|)
expr_stmt|;
name|assertNotEquals
argument_list|(
name|first
operator|.
name|accuracy
argument_list|(
literal|0.1f
argument_list|)
argument_list|,
name|serializedCopy
argument_list|(
name|first
argument_list|)
operator|.
name|accuracy
argument_list|(
literal|0.2f
argument_list|)
argument_list|)
expr_stmt|;
name|assertNotEquals
argument_list|(
name|first
operator|.
name|maxEdits
argument_list|(
literal|1
argument_list|)
argument_list|,
name|serializedCopy
argument_list|(
name|first
argument_list|)
operator|.
name|maxEdits
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertNotEquals
argument_list|(
name|first
operator|.
name|maxInspections
argument_list|(
literal|1
argument_list|)
argument_list|,
name|serializedCopy
argument_list|(
name|first
argument_list|)
operator|.
name|maxInspections
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertNotEquals
argument_list|(
name|first
operator|.
name|maxTermFreq
argument_list|(
literal|0.1f
argument_list|)
argument_list|,
name|serializedCopy
argument_list|(
name|first
argument_list|)
operator|.
name|maxTermFreq
argument_list|(
literal|0.2f
argument_list|)
argument_list|)
expr_stmt|;
name|assertNotEquals
argument_list|(
name|first
operator|.
name|minDocFreq
argument_list|(
literal|0.1f
argument_list|)
argument_list|,
name|serializedCopy
argument_list|(
name|first
argument_list|)
operator|.
name|minDocFreq
argument_list|(
literal|0.2f
argument_list|)
argument_list|)
expr_stmt|;
name|assertNotEquals
argument_list|(
name|first
operator|.
name|minWordLength
argument_list|(
literal|1
argument_list|)
argument_list|,
name|serializedCopy
argument_list|(
name|first
argument_list|)
operator|.
name|minWordLength
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertNotEquals
argument_list|(
name|first
operator|.
name|postFilter
argument_list|(
literal|"postFilter"
argument_list|)
argument_list|,
name|serializedCopy
argument_list|(
name|first
argument_list|)
operator|.
name|postFilter
argument_list|(
literal|"postFilter_other"
argument_list|)
argument_list|)
expr_stmt|;
name|assertNotEquals
argument_list|(
name|first
operator|.
name|preFilter
argument_list|(
literal|"preFilter"
argument_list|)
argument_list|,
name|serializedCopy
argument_list|(
name|first
argument_list|)
operator|.
name|preFilter
argument_list|(
literal|"preFilter_other"
argument_list|)
argument_list|)
expr_stmt|;
name|assertNotEquals
argument_list|(
name|first
operator|.
name|prefixLength
argument_list|(
literal|1
argument_list|)
argument_list|,
name|serializedCopy
argument_list|(
name|first
argument_list|)
operator|.
name|prefixLength
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertNotEquals
argument_list|(
name|first
operator|.
name|size
argument_list|(
literal|1
argument_list|)
argument_list|,
name|serializedCopy
argument_list|(
name|first
argument_list|)
operator|.
name|size
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertNotEquals
argument_list|(
name|first
operator|.
name|sort
argument_list|(
literal|"score"
argument_list|)
argument_list|,
name|serializedCopy
argument_list|(
name|first
argument_list|)
operator|.
name|sort
argument_list|(
literal|"frequency"
argument_list|)
argument_list|)
expr_stmt|;
name|assertNotEquals
argument_list|(
name|first
operator|.
name|stringDistance
argument_list|(
literal|"levenstein"
argument_list|)
argument_list|,
name|serializedCopy
argument_list|(
name|first
argument_list|)
operator|.
name|sort
argument_list|(
literal|"ngram"
argument_list|)
argument_list|)
expr_stmt|;
name|assertNotEquals
argument_list|(
name|first
operator|.
name|suggestMode
argument_list|(
literal|"missing"
argument_list|)
argument_list|,
name|serializedCopy
argument_list|(
name|first
argument_list|)
operator|.
name|suggestMode
argument_list|(
literal|"always"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      *  creates random candidate generator, renders it to xContent and back to new instance that should be equal to original      */
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
operator|new
name|IndicesQueriesRegistry
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|)
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
name|NUMBER_OF_RUNS
condition|;
name|runs
operator|++
control|)
block|{
name|DirectCandidateGeneratorBuilder
name|generator
init|=
name|randomCandidateGenerator
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
name|generator
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
name|XContentParser
name|parser
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
name|DirectCandidateGeneratorBuilder
name|secondGenerator
init|=
name|DirectCandidateGeneratorBuilder
operator|.
name|PROTOTYPE
operator|.
name|fromXContent
argument_list|(
name|context
argument_list|)
decl_stmt|;
name|assertNotSame
argument_list|(
name|generator
argument_list|,
name|secondGenerator
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|generator
argument_list|,
name|secondGenerator
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|generator
operator|.
name|hashCode
argument_list|()
argument_list|,
name|secondGenerator
operator|.
name|hashCode
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      * test that build() outputs a {@link DirectCandidateGenerator} that is similar to the one      * we would get when parsing the xContent the test generator is rendering out      */
DECL|method|testBuild
specifier|public
name|void
name|testBuild
parameter_list|()
throws|throws
name|IOException
block|{
name|long
name|start
init|=
name|System
operator|.
name|currentTimeMillis
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
name|Settings
operator|.
name|EMPTY
argument_list|)
decl_stmt|;
name|AnalysisService
name|mockAnalysisService
init|=
operator|new
name|AnalysisService
argument_list|(
name|idxSettings
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|NamedAnalyzer
name|analyzer
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
operator|new
name|NamedAnalyzer
argument_list|(
name|name
argument_list|,
operator|new
name|WhitespaceAnalyzer
argument_list|()
argument_list|)
return|;
block|}
block|}
decl_stmt|;
name|MapperService
name|mockMapperService
init|=
operator|new
name|MapperService
argument_list|(
name|idxSettings
argument_list|,
name|mockAnalysisService
argument_list|,
literal|null
argument_list|,
operator|new
name|IndicesModule
argument_list|()
operator|.
name|getMapperRegistry
argument_list|()
argument_list|,
literal|null
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|MappedFieldType
name|fullName
parameter_list|(
name|String
name|fullName
parameter_list|)
block|{
return|return
operator|new
name|StringFieldType
argument_list|()
return|;
block|}
block|}
decl_stmt|;
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
name|mockMapperService
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
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
name|TextFieldMapper
operator|.
name|Builder
name|builder
init|=
operator|new
name|TextFieldMapper
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
name|mockShardContext
operator|.
name|setMapUnmappedFieldAsString
argument_list|(
literal|true
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
name|NUMBER_OF_RUNS
condition|;
name|runs
operator|++
control|)
block|{
name|DirectCandidateGeneratorBuilder
name|generator
init|=
name|randomCandidateGenerator
argument_list|()
decl_stmt|;
comment|// first, build via DirectCandidateGenerator#build()
name|DirectCandidateGenerator
name|contextGenerator
init|=
name|generator
operator|.
name|build
argument_list|(
name|mockShardContext
argument_list|)
decl_stmt|;
comment|// second, render random test generator to xContent and parse using
comment|// PhraseSuggestParser
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
name|generator
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
name|XContentParser
name|parser
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
name|DirectCandidateGenerator
name|secondGenerator
init|=
name|PhraseSuggestParser
operator|.
name|parseCandidateGenerator
argument_list|(
name|parser
argument_list|,
name|mockShardContext
operator|.
name|getMapperService
argument_list|()
argument_list|,
name|mockShardContext
operator|.
name|parseFieldMatcher
argument_list|()
argument_list|)
decl_stmt|;
comment|// compare their properties
name|assertNotSame
argument_list|(
name|contextGenerator
argument_list|,
name|secondGenerator
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|contextGenerator
operator|.
name|field
argument_list|()
argument_list|,
name|secondGenerator
operator|.
name|field
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|contextGenerator
operator|.
name|accuracy
argument_list|()
argument_list|,
name|secondGenerator
operator|.
name|accuracy
argument_list|()
argument_list|,
name|Float
operator|.
name|MIN_VALUE
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|contextGenerator
operator|.
name|maxTermFreq
argument_list|()
argument_list|,
name|secondGenerator
operator|.
name|maxTermFreq
argument_list|()
argument_list|,
name|Float
operator|.
name|MIN_VALUE
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|contextGenerator
operator|.
name|maxEdits
argument_list|()
argument_list|,
name|secondGenerator
operator|.
name|maxEdits
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|contextGenerator
operator|.
name|maxInspections
argument_list|()
argument_list|,
name|secondGenerator
operator|.
name|maxInspections
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|contextGenerator
operator|.
name|minDocFreq
argument_list|()
argument_list|,
name|secondGenerator
operator|.
name|minDocFreq
argument_list|()
argument_list|,
name|Float
operator|.
name|MIN_VALUE
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|contextGenerator
operator|.
name|minWordLength
argument_list|()
argument_list|,
name|secondGenerator
operator|.
name|minWordLength
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|contextGenerator
operator|.
name|postFilter
argument_list|()
argument_list|,
name|secondGenerator
operator|.
name|postFilter
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|contextGenerator
operator|.
name|prefixLength
argument_list|()
argument_list|,
name|secondGenerator
operator|.
name|prefixLength
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|contextGenerator
operator|.
name|preFilter
argument_list|()
argument_list|,
name|secondGenerator
operator|.
name|preFilter
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|contextGenerator
operator|.
name|sort
argument_list|()
argument_list|,
name|secondGenerator
operator|.
name|sort
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|contextGenerator
operator|.
name|size
argument_list|()
argument_list|,
name|secondGenerator
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// some instances of StringDistance don't support equals, just checking the class here
name|assertEquals
argument_list|(
name|contextGenerator
operator|.
name|stringDistance
argument_list|()
operator|.
name|getClass
argument_list|()
argument_list|,
name|secondGenerator
operator|.
name|stringDistance
argument_list|()
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|contextGenerator
operator|.
name|suggestMode
argument_list|()
argument_list|,
name|secondGenerator
operator|.
name|suggestMode
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      * test that bad xContent throws exception      */
DECL|method|testIllegalXContent
specifier|public
name|void
name|testIllegalXContent
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
operator|new
name|IndicesQueriesRegistry
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|)
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
comment|// test missing fieldname
name|String
name|directGenerator
init|=
literal|"{ }"
decl_stmt|;
name|XContentParser
name|parser
init|=
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|directGenerator
argument_list|)
operator|.
name|createParser
argument_list|(
name|directGenerator
argument_list|)
decl_stmt|;
name|context
operator|.
name|reset
argument_list|(
name|parser
argument_list|)
expr_stmt|;
try|try
block|{
name|DirectCandidateGeneratorBuilder
operator|.
name|PROTOTYPE
operator|.
name|fromXContent
argument_list|(
name|context
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"expected an exception"
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
literal|"[direct_generator] expects exactly one field parameter, but found []"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// test two fieldnames
name|directGenerator
operator|=
literal|"{ \"field\" : \"f1\", \"field\" : \"f2\" }"
expr_stmt|;
name|parser
operator|=
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|directGenerator
argument_list|)
operator|.
name|createParser
argument_list|(
name|directGenerator
argument_list|)
expr_stmt|;
name|context
operator|.
name|reset
argument_list|(
name|parser
argument_list|)
expr_stmt|;
try|try
block|{
name|DirectCandidateGeneratorBuilder
operator|.
name|PROTOTYPE
operator|.
name|fromXContent
argument_list|(
name|context
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"expected an exception"
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
literal|"[direct_generator] expects exactly one field parameter, but found [f2, f1]"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// test unknown field
name|directGenerator
operator|=
literal|"{ \"unknown_param\" : \"f1\" }"
expr_stmt|;
name|parser
operator|=
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|directGenerator
argument_list|)
operator|.
name|createParser
argument_list|(
name|directGenerator
argument_list|)
expr_stmt|;
name|context
operator|.
name|reset
argument_list|(
name|parser
argument_list|)
expr_stmt|;
try|try
block|{
name|DirectCandidateGeneratorBuilder
operator|.
name|PROTOTYPE
operator|.
name|fromXContent
argument_list|(
name|context
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"expected an exception"
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
literal|"[direct_generator] unknown field [unknown_param], parser not found"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// test bad value for field (e.g. size expects an int)
name|directGenerator
operator|=
literal|"{ \"size\" : \"xxl\" }"
expr_stmt|;
name|parser
operator|=
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|directGenerator
argument_list|)
operator|.
name|createParser
argument_list|(
name|directGenerator
argument_list|)
expr_stmt|;
name|context
operator|.
name|reset
argument_list|(
name|parser
argument_list|)
expr_stmt|;
try|try
block|{
name|DirectCandidateGeneratorBuilder
operator|.
name|PROTOTYPE
operator|.
name|fromXContent
argument_list|(
name|context
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"expected an exception"
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
literal|"[direct_generator] failed to parse field [size]"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// test unexpected token
name|directGenerator
operator|=
literal|"{ \"size\" : [ \"xxl\" ] }"
expr_stmt|;
name|parser
operator|=
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|directGenerator
argument_list|)
operator|.
name|createParser
argument_list|(
name|directGenerator
argument_list|)
expr_stmt|;
name|context
operator|.
name|reset
argument_list|(
name|parser
argument_list|)
expr_stmt|;
try|try
block|{
name|DirectCandidateGeneratorBuilder
operator|.
name|PROTOTYPE
operator|.
name|fromXContent
argument_list|(
name|context
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"expected an exception"
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
literal|"[direct_generator] size doesn't support values of type: START_ARRAY"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      * create random {@link DirectCandidateGeneratorBuilder}      */
DECL|method|randomCandidateGenerator
specifier|public
specifier|static
name|DirectCandidateGeneratorBuilder
name|randomCandidateGenerator
parameter_list|()
block|{
name|DirectCandidateGeneratorBuilder
name|generator
init|=
operator|new
name|DirectCandidateGeneratorBuilder
argument_list|(
name|randomAsciiOfLength
argument_list|(
literal|10
argument_list|)
argument_list|)
decl_stmt|;
name|maybeSet
argument_list|(
name|generator
operator|::
name|accuracy
argument_list|,
name|randomFloat
argument_list|()
argument_list|)
expr_stmt|;
name|maybeSet
argument_list|(
name|generator
operator|::
name|maxEdits
argument_list|,
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|maybeSet
argument_list|(
name|generator
operator|::
name|maxInspections
argument_list|,
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|20
argument_list|)
argument_list|)
expr_stmt|;
name|maybeSet
argument_list|(
name|generator
operator|::
name|maxTermFreq
argument_list|,
name|randomFloat
argument_list|()
argument_list|)
expr_stmt|;
name|maybeSet
argument_list|(
name|generator
operator|::
name|minDocFreq
argument_list|,
name|randomFloat
argument_list|()
argument_list|)
expr_stmt|;
name|maybeSet
argument_list|(
name|generator
operator|::
name|minWordLength
argument_list|,
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|20
argument_list|)
argument_list|)
expr_stmt|;
name|maybeSet
argument_list|(
name|generator
operator|::
name|prefixLength
argument_list|,
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|20
argument_list|)
argument_list|)
expr_stmt|;
name|maybeSet
argument_list|(
name|generator
operator|::
name|preFilter
argument_list|,
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|20
argument_list|)
argument_list|)
expr_stmt|;
name|maybeSet
argument_list|(
name|generator
operator|::
name|postFilter
argument_list|,
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|20
argument_list|)
argument_list|)
expr_stmt|;
name|maybeSet
argument_list|(
name|generator
operator|::
name|size
argument_list|,
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|20
argument_list|)
argument_list|)
expr_stmt|;
name|maybeSet
argument_list|(
name|generator
operator|::
name|sort
argument_list|,
name|randomFrom
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"score"
block|,
literal|"frequency"
block|}
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|maybeSet
argument_list|(
name|generator
operator|::
name|stringDistance
argument_list|,
name|randomFrom
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"internal"
block|,
literal|"damerau_levenshtein"
block|,
literal|"levenstein"
block|,
literal|"jarowinkler"
block|,
literal|"ngram"
block|}
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|maybeSet
argument_list|(
name|generator
operator|::
name|suggestMode
argument_list|,
name|randomFrom
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"missing"
block|,
literal|"popular"
block|,
literal|"always"
block|}
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|generator
return|;
block|}
DECL|method|maybeSet
specifier|private
specifier|static
parameter_list|<
name|T
parameter_list|>
name|void
name|maybeSet
parameter_list|(
name|Consumer
argument_list|<
name|T
argument_list|>
name|consumer
parameter_list|,
name|T
name|value
parameter_list|)
block|{
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|consumer
operator|.
name|accept
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|serializedCopy
specifier|private
specifier|static
name|DirectCandidateGeneratorBuilder
name|serializedCopy
parameter_list|(
name|DirectCandidateGeneratorBuilder
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
name|StreamInput
operator|.
name|wrap
argument_list|(
name|output
operator|.
name|bytes
argument_list|()
argument_list|)
init|)
block|{
return|return
name|DirectCandidateGeneratorBuilder
operator|.
name|PROTOTYPE
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
