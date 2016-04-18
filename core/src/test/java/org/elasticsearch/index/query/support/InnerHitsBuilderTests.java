begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.query.support
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|support
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

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|sameInstance
import|;
end_import

begin_class
DECL|class|InnerHitsBuilderTests
specifier|public
class|class
name|InnerHitsBuilderTests
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
name|getQueryParserRegistry
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
DECL|method|testSerialization
specifier|public
name|void
name|testSerialization
parameter_list|()
throws|throws
name|Exception
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
name|InnerHitsBuilder
name|original
init|=
name|randomInnerHits
argument_list|()
decl_stmt|;
name|InnerHitsBuilder
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
DECL|method|testFromAndToXContent
specifier|public
name|void
name|testFromAndToXContent
parameter_list|()
throws|throws
name|Exception
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
name|InnerHitsBuilder
name|innerHits
init|=
name|randomInnerHits
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
name|innerHits
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
name|QueryParseContext
name|context
init|=
operator|new
name|QueryParseContext
argument_list|(
name|indicesQueriesRegistry
argument_list|,
name|parser
argument_list|,
name|ParseFieldMatcher
operator|.
name|EMPTY
argument_list|)
decl_stmt|;
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
name|InnerHitsBuilder
name|secondInnerHits
init|=
name|InnerHitsBuilder
operator|.
name|fromXContent
argument_list|(
name|context
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|innerHits
argument_list|,
name|not
argument_list|(
name|sameInstance
argument_list|(
name|secondInnerHits
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|innerHits
argument_list|,
name|equalTo
argument_list|(
name|secondInnerHits
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|innerHits
operator|.
name|hashCode
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|secondInnerHits
operator|.
name|hashCode
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
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
name|InnerHitsBuilder
name|firstInnerHits
init|=
name|randomInnerHits
argument_list|()
decl_stmt|;
name|assertFalse
argument_list|(
literal|"inner hit is equal to null"
argument_list|,
name|firstInnerHits
operator|.
name|equals
argument_list|(
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
literal|"inner hit is equal to incompatible type"
argument_list|,
name|firstInnerHits
operator|.
name|equals
argument_list|(
literal|""
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"inner it is not equal to self"
argument_list|,
name|firstInnerHits
operator|.
name|equals
argument_list|(
name|firstInnerHits
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"same inner hit's hashcode returns different values if called multiple times"
argument_list|,
name|firstInnerHits
operator|.
name|hashCode
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|firstInnerHits
operator|.
name|hashCode
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|InnerHitsBuilder
name|secondBuilder
init|=
name|serializedCopy
argument_list|(
name|firstInnerHits
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"inner hit is not equal to self"
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
literal|"inner hit is not equal to its copy"
argument_list|,
name|firstInnerHits
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
name|firstInnerHits
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"inner hits copy's hashcode is different from original hashcode"
argument_list|,
name|secondBuilder
operator|.
name|hashCode
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|firstInnerHits
operator|.
name|hashCode
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|InnerHitsBuilder
name|thirdBuilder
init|=
name|serializedCopy
argument_list|(
name|secondBuilder
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"inner hit is not equal to self"
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
literal|"inner hit is not equal to its copy"
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
literal|"inner hit copy's hashcode is different from original hashcode"
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
name|firstInnerHits
operator|.
name|equals
argument_list|(
name|thirdBuilder
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"inner hit copy's hashcode is different from original hashcode"
argument_list|,
name|firstInnerHits
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
name|firstInnerHits
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|randomInnerHits
specifier|public
specifier|static
name|InnerHitsBuilder
name|randomInnerHits
parameter_list|()
block|{
name|InnerHitsBuilder
name|innerHits
init|=
operator|new
name|InnerHitsBuilder
argument_list|()
decl_stmt|;
name|int
name|numInnerHits
init|=
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|12
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numInnerHits
condition|;
name|i
operator|++
control|)
block|{
name|innerHits
operator|.
name|addInnerHit
argument_list|(
name|randomAsciiOfLength
argument_list|(
literal|5
argument_list|)
argument_list|,
name|InnerHitBuilderTests
operator|.
name|randomInnerHits
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|innerHits
return|;
block|}
DECL|method|serializedCopy
specifier|private
specifier|static
name|InnerHitsBuilder
name|serializedCopy
parameter_list|(
name|InnerHitsBuilder
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
return|return
name|InnerHitsBuilder
operator|.
name|PROTO
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

