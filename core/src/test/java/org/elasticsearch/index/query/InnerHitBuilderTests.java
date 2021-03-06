begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.query
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
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
name|Strings
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
name|script
operator|.
name|Script
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
name|ScriptType
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
name|builder
operator|.
name|SearchSourceBuilder
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
name|fetch
operator|.
name|subphase
operator|.
name|FetchSourceContext
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
name|fetch
operator|.
name|subphase
operator|.
name|highlight
operator|.
name|HighlightBuilderTests
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
name|internal
operator|.
name|ShardSearchLocalRequest
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
name|sort
operator|.
name|SortBuilder
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
name|sort
operator|.
name|SortBuilders
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
name|sort
operator|.
name|SortOrder
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
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
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
name|Supplier
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
DECL|class|InnerHitBuilderTests
specifier|public
class|class
name|InnerHitBuilderTests
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
DECL|field|xContentRegistry
specifier|private
specifier|static
name|NamedXContentRegistry
name|xContentRegistry
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
name|InnerHitBuilder
name|original
init|=
name|randomInnerHits
argument_list|()
decl_stmt|;
name|InnerHitBuilder
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
comment|/**      * Test that if we serialize and deserialize an object, further      * serialization leads to identical bytes representation.      *      * This is necessary to ensure because we use the serialized BytesReference      * of this builder as part of the cacheKey in      * {@link ShardSearchLocalRequest} (via      * {@link SearchSourceBuilder#collapse(org.elasticsearch.search.collapse.CollapseBuilder)})      */
DECL|method|testSerializationOrder
specifier|public
name|void
name|testSerializationOrder
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
name|InnerHitBuilder
name|original
init|=
name|randomInnerHits
argument_list|()
decl_stmt|;
name|InnerHitBuilder
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
name|BytesStreamOutput
name|out1
init|=
operator|new
name|BytesStreamOutput
argument_list|()
decl_stmt|;
name|BytesStreamOutput
name|out2
init|=
operator|new
name|BytesStreamOutput
argument_list|()
decl_stmt|;
name|original
operator|.
name|writeTo
argument_list|(
name|out1
argument_list|)
expr_stmt|;
name|deserialized
operator|.
name|writeTo
argument_list|(
name|out2
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|out1
operator|.
name|bytes
argument_list|()
argument_list|,
name|out2
operator|.
name|bytes
argument_list|()
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
name|InnerHitBuilder
name|innerHit
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
name|innerHit
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
comment|//fields is printed out as an object but parsed into a List where order matters, we disable shuffling
name|XContentBuilder
name|shuffled
init|=
name|shuffleXContent
argument_list|(
name|builder
argument_list|,
literal|"fields"
argument_list|)
decl_stmt|;
name|XContentParser
name|parser
init|=
name|createParser
argument_list|(
name|shuffled
argument_list|)
decl_stmt|;
name|QueryParseContext
name|context
init|=
operator|new
name|QueryParseContext
argument_list|(
name|parser
argument_list|)
decl_stmt|;
name|InnerHitBuilder
name|secondInnerHits
init|=
name|InnerHitBuilder
operator|.
name|fromXContent
argument_list|(
name|context
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|innerHit
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
name|innerHit
argument_list|,
name|equalTo
argument_list|(
name|secondInnerHits
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|innerHit
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
name|randomInnerHits
argument_list|()
argument_list|,
name|InnerHitBuilderTests
operator|::
name|serializedCopy
argument_list|,
name|InnerHitBuilderTests
operator|::
name|mutate
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|randomInnerHits
specifier|public
specifier|static
name|InnerHitBuilder
name|randomInnerHits
parameter_list|()
block|{
name|InnerHitBuilder
name|innerHits
init|=
operator|new
name|InnerHitBuilder
argument_list|()
decl_stmt|;
name|innerHits
operator|.
name|setName
argument_list|(
name|randomAlphaOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|16
argument_list|)
argument_list|)
expr_stmt|;
name|innerHits
operator|.
name|setFrom
argument_list|(
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|128
argument_list|)
argument_list|)
expr_stmt|;
name|innerHits
operator|.
name|setSize
argument_list|(
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|128
argument_list|)
argument_list|)
expr_stmt|;
name|innerHits
operator|.
name|setExplain
argument_list|(
name|randomBoolean
argument_list|()
argument_list|)
expr_stmt|;
name|innerHits
operator|.
name|setVersion
argument_list|(
name|randomBoolean
argument_list|()
argument_list|)
expr_stmt|;
name|innerHits
operator|.
name|setTrackScores
argument_list|(
name|randomBoolean
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|innerHits
operator|.
name|setStoredFieldNames
argument_list|(
name|randomListStuff
argument_list|(
literal|16
argument_list|,
parameter_list|()
lambda|->
name|randomAlphaOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|16
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|innerHits
operator|.
name|setDocValueFields
argument_list|(
name|randomListStuff
argument_list|(
literal|16
argument_list|,
parameter_list|()
lambda|->
name|randomAlphaOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|16
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// Random script fields deduped on their field name.
name|Map
argument_list|<
name|String
argument_list|,
name|SearchSourceBuilder
operator|.
name|ScriptField
argument_list|>
name|scriptFields
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|SearchSourceBuilder
operator|.
name|ScriptField
name|field
range|:
name|randomListStuff
argument_list|(
literal|16
argument_list|,
name|InnerHitBuilderTests
operator|::
name|randomScript
argument_list|)
control|)
block|{
name|scriptFields
operator|.
name|put
argument_list|(
name|field
operator|.
name|fieldName
argument_list|()
argument_list|,
name|field
argument_list|)
expr_stmt|;
block|}
name|innerHits
operator|.
name|setScriptFields
argument_list|(
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|scriptFields
operator|.
name|values
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|FetchSourceContext
name|randomFetchSourceContext
decl_stmt|;
name|int
name|randomInt
init|=
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|2
argument_list|)
decl_stmt|;
if|if
condition|(
name|randomInt
operator|==
literal|0
condition|)
block|{
name|randomFetchSourceContext
operator|=
operator|new
name|FetchSourceContext
argument_list|(
literal|true
argument_list|,
name|Strings
operator|.
name|EMPTY_ARRAY
argument_list|,
name|Strings
operator|.
name|EMPTY_ARRAY
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|randomInt
operator|==
literal|1
condition|)
block|{
name|randomFetchSourceContext
operator|=
operator|new
name|FetchSourceContext
argument_list|(
literal|true
argument_list|,
name|generateRandomStringArray
argument_list|(
literal|12
argument_list|,
literal|16
argument_list|,
literal|false
argument_list|)
argument_list|,
name|generateRandomStringArray
argument_list|(
literal|12
argument_list|,
literal|16
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|randomFetchSourceContext
operator|=
operator|new
name|FetchSourceContext
argument_list|(
name|randomBoolean
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|innerHits
operator|.
name|setFetchSourceContext
argument_list|(
name|randomFetchSourceContext
argument_list|)
expr_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|innerHits
operator|.
name|setSorts
argument_list|(
name|randomListStuff
argument_list|(
literal|16
argument_list|,
parameter_list|()
lambda|->
name|SortBuilders
operator|.
name|fieldSort
argument_list|(
name|randomAlphaOfLengthBetween
argument_list|(
literal|5
argument_list|,
literal|20
argument_list|)
argument_list|)
operator|.
name|order
argument_list|(
name|randomFrom
argument_list|(
name|SortOrder
operator|.
name|values
argument_list|()
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|innerHits
operator|.
name|setHighlightBuilder
argument_list|(
name|HighlightBuilderTests
operator|.
name|randomHighlighterBuilder
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|innerHits
return|;
block|}
DECL|method|mutate
specifier|static
name|InnerHitBuilder
name|mutate
parameter_list|(
name|InnerHitBuilder
name|original
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|InnerHitBuilder
name|copy
init|=
name|serializedCopy
argument_list|(
name|original
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Runnable
argument_list|>
name|modifiers
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
literal|12
argument_list|)
decl_stmt|;
name|modifiers
operator|.
name|add
argument_list|(
parameter_list|()
lambda|->
name|copy
operator|.
name|setFrom
argument_list|(
name|randomValueOtherThan
argument_list|(
name|copy
operator|.
name|getFrom
argument_list|()
argument_list|,
parameter_list|()
lambda|->
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|128
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|modifiers
operator|.
name|add
argument_list|(
parameter_list|()
lambda|->
name|copy
operator|.
name|setSize
argument_list|(
name|randomValueOtherThan
argument_list|(
name|copy
operator|.
name|getSize
argument_list|()
argument_list|,
parameter_list|()
lambda|->
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|128
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|modifiers
operator|.
name|add
argument_list|(
parameter_list|()
lambda|->
name|copy
operator|.
name|setExplain
argument_list|(
operator|!
name|copy
operator|.
name|isExplain
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|modifiers
operator|.
name|add
argument_list|(
parameter_list|()
lambda|->
name|copy
operator|.
name|setVersion
argument_list|(
operator|!
name|copy
operator|.
name|isVersion
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|modifiers
operator|.
name|add
argument_list|(
parameter_list|()
lambda|->
name|copy
operator|.
name|setTrackScores
argument_list|(
operator|!
name|copy
operator|.
name|isTrackScores
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|modifiers
operator|.
name|add
argument_list|(
parameter_list|()
lambda|->
name|copy
operator|.
name|setName
argument_list|(
name|randomValueOtherThan
argument_list|(
name|copy
operator|.
name|getName
argument_list|()
argument_list|,
parameter_list|()
lambda|->
name|randomAlphaOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|16
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|modifiers
operator|.
name|add
argument_list|(
parameter_list|()
lambda|->
block|{
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|copy
operator|.
name|setDocValueFields
argument_list|(
name|randomValueOtherThan
argument_list|(
name|copy
operator|.
name|getDocValueFields
argument_list|()
argument_list|,
parameter_list|()
lambda|->
block|{
return|return
name|randomListStuff
argument_list|(
literal|16
argument_list|,
parameter_list|()
lambda|->
name|randomAlphaOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|16
argument_list|)
argument_list|)
return|;
block|}
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|copy
operator|.
name|addDocValueField
argument_list|(
name|randomAlphaOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|16
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
name|modifiers
operator|.
name|add
argument_list|(
parameter_list|()
lambda|->
block|{
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|copy
operator|.
name|setScriptFields
argument_list|(
name|randomValueOtherThan
argument_list|(
name|copy
operator|.
name|getScriptFields
argument_list|()
argument_list|,
parameter_list|()
lambda|->
block|{
return|return
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|randomListStuff
argument_list|(
literal|16
argument_list|,
name|InnerHitBuilderTests
operator|::
name|randomScript
argument_list|)
argument_list|)
return|;
block|}
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|SearchSourceBuilder
operator|.
name|ScriptField
name|script
init|=
name|randomScript
argument_list|()
decl_stmt|;
name|copy
operator|.
name|addScriptField
argument_list|(
name|script
operator|.
name|fieldName
argument_list|()
argument_list|,
name|script
operator|.
name|script
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
name|modifiers
operator|.
name|add
argument_list|(
parameter_list|()
lambda|->
name|copy
operator|.
name|setFetchSourceContext
argument_list|(
name|randomValueOtherThan
argument_list|(
name|copy
operator|.
name|getFetchSourceContext
argument_list|()
argument_list|,
parameter_list|()
lambda|->
block|{
name|FetchSourceContext
name|randomFetchSourceContext
argument_list|;             if
operator|(
name|randomBoolean
argument_list|()
operator|)
block|{
name|randomFetchSourceContext
operator|=
operator|new
name|FetchSourceContext
argument_list|(
name|randomBoolean
argument_list|()
argument_list|)
block|;             }
else|else
block|{
name|randomFetchSourceContext
operator|=
operator|new
name|FetchSourceContext
argument_list|(
literal|true
argument_list|,
name|generateRandomStringArray
argument_list|(
literal|12
argument_list|,
literal|16
argument_list|,
literal|false
argument_list|)
argument_list|,
name|generateRandomStringArray
argument_list|(
literal|12
argument_list|,
literal|16
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
argument_list|return
name|randomFetchSourceContext
argument_list|;
block|}
block|)
end_class

begin_empty_stmt
unit|))
empty_stmt|;
end_empty_stmt

begin_expr_stmt
name|modifiers
operator|.
name|add
argument_list|(
parameter_list|()
lambda|->
block|{
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
specifier|final
name|List
argument_list|<
name|SortBuilder
argument_list|<
name|?
argument_list|>
argument_list|>
name|sortBuilders
init|=
name|randomValueOtherThan
argument_list|(
name|copy
operator|.
name|getSorts
argument_list|()
argument_list|,
parameter_list|()
lambda|->
block|{
name|List
argument_list|<
name|SortBuilder
argument_list|<
name|?
argument_list|>
argument_list|>
name|builders
init|=
name|randomListStuff
argument_list|(
literal|16
argument_list|,
parameter_list|()
lambda|->
name|SortBuilders
operator|.
name|fieldSort
argument_list|(
name|randomAlphaOfLengthBetween
argument_list|(
literal|5
argument_list|,
literal|20
argument_list|)
argument_list|)
operator|.
name|order
argument_list|(
name|randomFrom
argument_list|(
name|SortOrder
operator|.
name|values
argument_list|()
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
return|return
name|builders
return|;
block|}
argument_list|)
decl_stmt|;
name|copy
operator|.
name|setSorts
argument_list|(
name|sortBuilders
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|copy
operator|.
name|addSort
argument_list|(
name|SortBuilders
operator|.
name|fieldSort
argument_list|(
name|randomAlphaOfLengthBetween
argument_list|(
literal|5
argument_list|,
literal|20
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
end_expr_stmt

begin_expr_stmt
name|modifiers
operator|.
name|add
argument_list|(
parameter_list|()
lambda|->
name|copy
operator|.
name|setHighlightBuilder
argument_list|(
name|randomValueOtherThan
argument_list|(
name|copy
operator|.
name|getHighlightBuilder
argument_list|()
argument_list|,
name|HighlightBuilderTests
operator|::
name|randomHighlighterBuilder
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
end_expr_stmt

begin_expr_stmt
name|modifiers
operator|.
name|add
argument_list|(
parameter_list|()
lambda|->
block|{
if|if
condition|(
name|copy
operator|.
name|getStoredFieldsContext
argument_list|()
operator|==
literal|null
operator|||
name|randomBoolean
argument_list|()
condition|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|previous
init|=
name|copy
operator|.
name|getStoredFieldsContext
argument_list|()
operator|==
literal|null
condition|?
name|Collections
operator|.
name|emptyList
argument_list|()
else|:
name|copy
operator|.
name|getStoredFieldsContext
argument_list|()
operator|.
name|fieldNames
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|newValues
init|=
name|randomValueOtherThan
argument_list|(
name|previous
argument_list|,
parameter_list|()
lambda|->
name|randomListStuff
argument_list|(
literal|1
argument_list|,
literal|16
argument_list|,
parameter_list|()
lambda|->
name|randomAlphaOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|16
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|copy
operator|.
name|setStoredFieldNames
argument_list|(
name|newValues
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|copy
operator|.
name|getStoredFieldsContext
argument_list|()
operator|.
name|addFieldName
argument_list|(
name|randomAlphaOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|16
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
end_expr_stmt

begin_expr_stmt
name|randomFrom
argument_list|(
name|modifiers
argument_list|)
operator|.
name|run
argument_list|()
expr_stmt|;
end_expr_stmt

begin_return
return|return
name|copy
return|;
end_return

begin_function
unit|}      static
DECL|method|randomScript
name|SearchSourceBuilder
operator|.
name|ScriptField
name|randomScript
parameter_list|()
block|{
name|ScriptType
name|randomScriptType
init|=
name|randomFrom
argument_list|(
name|ScriptType
operator|.
name|values
argument_list|()
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|randomMap
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|int
name|numEntries
init|=
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|32
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
name|numEntries
condition|;
name|i
operator|++
control|)
block|{
name|randomMap
operator|.
name|put
argument_list|(
name|String
operator|.
name|valueOf
argument_list|(
name|i
argument_list|)
argument_list|,
name|randomAlphaOfLength
argument_list|(
literal|16
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|Script
name|script
init|=
operator|new
name|Script
argument_list|(
name|randomScriptType
argument_list|,
name|randomScriptType
operator|==
name|ScriptType
operator|.
name|STORED
condition|?
literal|null
else|:
name|randomAlphaOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|4
argument_list|)
argument_list|,
name|randomAlphaOfLength
argument_list|(
literal|128
argument_list|)
argument_list|,
name|randomMap
argument_list|)
decl_stmt|;
return|return
operator|new
name|SearchSourceBuilder
operator|.
name|ScriptField
argument_list|(
name|randomAlphaOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|32
argument_list|)
argument_list|,
name|script
argument_list|,
name|randomBoolean
argument_list|()
argument_list|)
return|;
block|}
end_function

begin_function
DECL|method|randomListStuff
specifier|static
parameter_list|<
name|T
parameter_list|>
name|List
argument_list|<
name|T
argument_list|>
name|randomListStuff
parameter_list|(
name|int
name|maxSize
parameter_list|,
name|Supplier
argument_list|<
name|T
argument_list|>
name|valueSupplier
parameter_list|)
block|{
return|return
name|randomListStuff
argument_list|(
literal|0
argument_list|,
name|maxSize
argument_list|,
name|valueSupplier
argument_list|)
return|;
block|}
end_function

begin_function
DECL|method|randomListStuff
specifier|static
parameter_list|<
name|T
parameter_list|>
name|List
argument_list|<
name|T
argument_list|>
name|randomListStuff
parameter_list|(
name|int
name|minSize
parameter_list|,
name|int
name|maxSize
parameter_list|,
name|Supplier
argument_list|<
name|T
argument_list|>
name|valueSupplier
parameter_list|)
block|{
name|int
name|size
init|=
name|randomIntBetween
argument_list|(
name|minSize
argument_list|,
name|maxSize
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|T
argument_list|>
name|list
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|size
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
name|size
condition|;
name|i
operator|++
control|)
block|{
name|list
operator|.
name|add
argument_list|(
name|valueSupplier
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|list
return|;
block|}
end_function

begin_function
DECL|method|serializedCopy
specifier|private
specifier|static
name|InnerHitBuilder
name|serializedCopy
parameter_list|(
name|InnerHitBuilder
name|original
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|ESTestCase
operator|.
name|copyWriteable
argument_list|(
name|original
argument_list|,
name|namedWriteableRegistry
argument_list|,
name|InnerHitBuilder
operator|::
operator|new
argument_list|)
return|;
block|}
end_function

unit|}
end_unit

