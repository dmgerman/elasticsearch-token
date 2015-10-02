begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.messy.tests
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|messy
operator|.
name|tests
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
name|util
operator|.
name|LuceneTestCase
operator|.
name|SuppressCodecs
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|get
operator|.
name|GetResponse
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|search
operator|.
name|SearchResponse
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|suggest
operator|.
name|SuggestResponse
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
name|XContentType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|plugins
operator|.
name|Plugin
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
name|groovy
operator|.
name|GroovyPlugin
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
name|groovy
operator|.
name|GroovyScriptEngineService
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
name|SuggestBuilders
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
name|completion
operator|.
name|CompletionSuggestion
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
name|ESIntegTestCase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
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
name|Collection
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
name|concurrent
operator|.
name|ExecutionException
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
name|singletonMap
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|QueryBuilders
operator|.
name|termQuery
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
name|hamcrest
operator|.
name|ElasticsearchAssertions
operator|.
name|assertAcked
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
name|hamcrest
operator|.
name|ElasticsearchAssertions
operator|.
name|assertExists
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
name|hamcrest
operator|.
name|ElasticsearchAssertions
operator|.
name|assertHitCount
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
name|hamcrest
operator|.
name|ElasticsearchAssertions
operator|.
name|assertSearchHits
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
name|hamcrest
operator|.
name|ElasticsearchAssertions
operator|.
name|assertSuggestion
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
name|both
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
name|hasEntry
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
name|hasKey
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

begin_comment
comment|/**  * Tests for transforming the source document before indexing.  */
end_comment

begin_class
annotation|@
name|SuppressCodecs
argument_list|(
literal|"*"
argument_list|)
comment|// requires custom completion format
DECL|class|TransformOnIndexMapperTests
specifier|public
class|class
name|TransformOnIndexMapperTests
extends|extends
name|ESIntegTestCase
block|{
annotation|@
name|Override
DECL|method|nodePlugins
specifier|protected
name|Collection
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|Plugin
argument_list|>
argument_list|>
name|nodePlugins
parameter_list|()
block|{
return|return
name|Collections
operator|.
name|singleton
argument_list|(
name|GroovyPlugin
operator|.
name|class
argument_list|)
return|;
block|}
annotation|@
name|Test
DECL|method|searchOnTransformed
specifier|public
name|void
name|searchOnTransformed
parameter_list|()
throws|throws
name|Exception
block|{
name|setup
argument_list|(
literal|true
argument_list|)
expr_stmt|;
comment|// Searching by the field created in the transport finds the entry
name|SearchResponse
name|response
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|termQuery
argument_list|(
literal|"destination"
argument_list|,
literal|"findme"
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertSearchHits
argument_list|(
name|response
argument_list|,
literal|"righttitle"
argument_list|)
expr_stmt|;
comment|// The field built in the transform isn't in the source but source is,
comment|// even though we didn't index it!
name|assertRightTitleSourceUntransformed
argument_list|(
name|response
operator|.
name|getHits
argument_list|()
operator|.
name|getAt
argument_list|(
literal|0
argument_list|)
operator|.
name|sourceAsMap
argument_list|()
argument_list|)
expr_stmt|;
comment|// Can't find by a field removed from the document by the transform
name|response
operator|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|termQuery
argument_list|(
literal|"content"
argument_list|,
literal|"findme"
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|assertHitCount
argument_list|(
name|response
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|getTransformed
specifier|public
name|void
name|getTransformed
parameter_list|()
throws|throws
name|Exception
block|{
name|setup
argument_list|(
name|getRandom
argument_list|()
operator|.
name|nextBoolean
argument_list|()
argument_list|)
expr_stmt|;
name|GetResponse
name|response
init|=
name|client
argument_list|()
operator|.
name|prepareGet
argument_list|(
literal|"test"
argument_list|,
literal|"test"
argument_list|,
literal|"righttitle"
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertExists
argument_list|(
name|response
argument_list|)
expr_stmt|;
name|assertRightTitleSourceUntransformed
argument_list|(
name|response
operator|.
name|getSource
argument_list|()
argument_list|)
expr_stmt|;
name|response
operator|=
name|client
argument_list|()
operator|.
name|prepareGet
argument_list|(
literal|"test"
argument_list|,
literal|"test"
argument_list|,
literal|"righttitle"
argument_list|)
operator|.
name|setTransformSource
argument_list|(
literal|true
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|assertExists
argument_list|(
name|response
argument_list|)
expr_stmt|;
name|assertRightTitleSourceTransformed
argument_list|(
name|response
operator|.
name|getSource
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// TODO: the completion suggester currently returns payloads with no reencoding so this test
comment|// exists to make sure that _source transformation and completion work well together. If we
comment|// ever fix the completion suggester to reencode the payloads then we can remove this test.
annotation|@
name|Test
DECL|method|contextSuggestPayloadTransformed
specifier|public
name|void
name|contextSuggestPayloadTransformed
parameter_list|()
throws|throws
name|Exception
block|{
name|XContentBuilder
name|builder
init|=
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
decl_stmt|;
name|builder
operator|.
name|startObject
argument_list|(
literal|"properties"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|startObject
argument_list|(
literal|"suggest"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"completion"
argument_list|)
operator|.
name|field
argument_list|(
literal|"payloads"
argument_list|,
literal|true
argument_list|)
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|startObject
argument_list|(
literal|"transform"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"script"
argument_list|,
literal|"ctx._source.suggest = ['input': ctx._source.text];ctx._source.suggest.payload = ['display': ctx._source.text, 'display_detail': 'on the fly']"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"lang"
argument_list|,
name|GroovyScriptEngineService
operator|.
name|NAME
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|assertAcked
argument_list|(
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareCreate
argument_list|(
literal|"test"
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"test"
argument_list|,
name|builder
argument_list|)
argument_list|)
expr_stmt|;
comment|// Payload is stored using original source format (json, smile, yaml, whatever)
name|XContentType
name|type
init|=
name|XContentType
operator|.
name|values
argument_list|()
index|[
name|between
argument_list|(
literal|0
argument_list|,
name|XContentType
operator|.
name|values
argument_list|()
operator|.
name|length
operator|-
literal|1
argument_list|)
index|]
decl_stmt|;
name|XContentBuilder
name|source
init|=
name|XContentFactory
operator|.
name|contentBuilder
argument_list|(
name|type
argument_list|)
decl_stmt|;
name|source
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"text"
argument_list|,
literal|"findme"
argument_list|)
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|indexRandom
argument_list|(
literal|true
argument_list|,
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"test"
argument_list|,
literal|"findme"
argument_list|)
operator|.
name|setSource
argument_list|(
name|source
argument_list|)
argument_list|)
expr_stmt|;
name|SuggestResponse
name|response
init|=
name|client
argument_list|()
operator|.
name|prepareSuggest
argument_list|(
literal|"test"
argument_list|)
operator|.
name|addSuggestion
argument_list|(
name|SuggestBuilders
operator|.
name|completionSuggestion
argument_list|(
literal|"test"
argument_list|)
operator|.
name|field
argument_list|(
literal|"suggest"
argument_list|)
operator|.
name|text
argument_list|(
literal|"findme"
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertSuggestion
argument_list|(
name|response
operator|.
name|getSuggest
argument_list|()
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|"test"
argument_list|,
literal|"findme"
argument_list|)
expr_stmt|;
name|CompletionSuggestion
operator|.
name|Entry
operator|.
name|Option
name|option
init|=
operator|(
name|CompletionSuggestion
operator|.
name|Entry
operator|.
name|Option
operator|)
name|response
operator|.
name|getSuggest
argument_list|()
operator|.
name|getSuggestion
argument_list|(
literal|"test"
argument_list|)
operator|.
name|getEntries
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getOptions
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
comment|// And it comes back in exactly that way.
name|XContentBuilder
name|expected
init|=
name|XContentFactory
operator|.
name|contentBuilder
argument_list|(
name|type
argument_list|)
decl_stmt|;
name|expected
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"display"
argument_list|,
literal|"findme"
argument_list|)
operator|.
name|field
argument_list|(
literal|"display_detail"
argument_list|,
literal|"on the fly"
argument_list|)
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
operator|.
name|string
argument_list|()
argument_list|,
name|option
operator|.
name|getPayloadAsString
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**      * Setup an index with some source transforms. Randomly picks the number of      * transforms but all but one of the transforms is a noop. The other is a      * script that fills the 'destination' field with the 'content' field only      * if the 'title' field starts with 't' and then always removes the      * 'content' field regarless of the contents of 't'. The actual script      * randomly uses parameters or not.      *      * @param forceRefresh      *            should the data be flushed to disk? Set to false to test real      *            time fetching      */
DECL|method|setup
specifier|private
name|void
name|setup
parameter_list|(
name|boolean
name|forceRefresh
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
throws|,
name|ExecutionException
block|{
name|XContentBuilder
name|builder
init|=
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
decl_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"transform"
argument_list|)
expr_stmt|;
if|if
condition|(
name|getRandom
argument_list|()
operator|.
name|nextBoolean
argument_list|()
condition|)
block|{
comment|// Single transform
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|buildTransformScript
argument_list|(
name|builder
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"lang"
argument_list|,
name|randomFrom
argument_list|(
literal|null
argument_list|,
name|GroovyScriptEngineService
operator|.
name|NAME
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
else|else
block|{
comment|// Multiple transforms
name|int
name|total
init|=
name|between
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|int
name|actual
init|=
name|between
argument_list|(
literal|0
argument_list|,
name|total
operator|-
literal|1
argument_list|)
decl_stmt|;
name|builder
operator|.
name|startArray
argument_list|()
expr_stmt|;
for|for
control|(
name|int
name|s
init|=
literal|0
init|;
name|s
operator|<
name|total
condition|;
name|s
operator|++
control|)
block|{
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
if|if
condition|(
name|s
operator|==
name|actual
condition|)
block|{
name|buildTransformScript
argument_list|(
name|builder
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"script"
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|field
argument_list|(
literal|"lang"
argument_list|,
name|randomFrom
argument_list|(
literal|null
argument_list|,
name|GroovyScriptEngineService
operator|.
name|NAME
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
name|builder
operator|.
name|endArray
argument_list|()
expr_stmt|;
block|}
name|assertAcked
argument_list|(
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareCreate
argument_list|(
literal|"test"
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"test"
argument_list|,
name|builder
argument_list|)
argument_list|)
expr_stmt|;
name|indexRandom
argument_list|(
name|forceRefresh
argument_list|,
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"test"
argument_list|,
literal|"notitle"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"content"
argument_list|,
literal|"findme"
argument_list|)
argument_list|,
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"test"
argument_list|,
literal|"badtitle"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"content"
argument_list|,
literal|"findme"
argument_list|,
literal|"title"
argument_list|,
literal|"cat"
argument_list|)
argument_list|,
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"test"
argument_list|,
literal|"righttitle"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"content"
argument_list|,
literal|"findme"
argument_list|,
literal|"title"
argument_list|,
literal|"table"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|buildTransformScript
specifier|private
name|void
name|buildTransformScript
parameter_list|(
name|XContentBuilder
name|builder
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|script
init|=
literal|"if (ctx._source['title']?.startsWith('t')) { ctx._source['destination'] = ctx._source[sourceField] }; ctx._source.remove(sourceField);"
decl_stmt|;
if|if
condition|(
name|getRandom
argument_list|()
operator|.
name|nextBoolean
argument_list|()
condition|)
block|{
name|script
operator|=
name|script
operator|.
name|replace
argument_list|(
literal|"sourceField"
argument_list|,
literal|"'content'"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"params"
argument_list|,
name|singletonMap
argument_list|(
literal|"sourceField"
argument_list|,
literal|"content"
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|field
argument_list|(
literal|"script"
argument_list|,
name|script
argument_list|)
expr_stmt|;
block|}
DECL|method|assertRightTitleSourceUntransformed
specifier|private
name|void
name|assertRightTitleSourceUntransformed
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|source
parameter_list|)
block|{
name|assertThat
argument_list|(
name|source
argument_list|,
name|both
argument_list|(
name|hasEntry
argument_list|(
literal|"content"
argument_list|,
operator|(
name|Object
operator|)
literal|"findme"
argument_list|)
argument_list|)
operator|.
name|and
argument_list|(
name|not
argument_list|(
name|hasKey
argument_list|(
literal|"destination"
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|assertRightTitleSourceTransformed
specifier|private
name|void
name|assertRightTitleSourceTransformed
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|source
parameter_list|)
block|{
name|assertThat
argument_list|(
name|source
argument_list|,
name|both
argument_list|(
name|hasEntry
argument_list|(
literal|"destination"
argument_list|,
operator|(
name|Object
operator|)
literal|"findme"
argument_list|)
argument_list|)
operator|.
name|and
argument_list|(
name|not
argument_list|(
name|hasKey
argument_list|(
literal|"content"
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

