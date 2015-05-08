begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.bwcompat
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|bwcompat
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableMap
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
name|test
operator|.
name|ElasticsearchBackwardsCompatIntegrationTest
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

begin_class
DECL|class|ScriptTransformBackwardsCompatibilityTests
specifier|public
class|class
name|ScriptTransformBackwardsCompatibilityTests
extends|extends
name|ElasticsearchBackwardsCompatIntegrationTest
block|{
annotation|@
name|Test
DECL|method|testTransformWithNoLangSpecified
specifier|public
name|void
name|testTransformWithNoLangSpecified
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
name|getRandom
argument_list|()
operator|.
name|nextBoolean
argument_list|()
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
name|assertThat
argument_list|(
name|response
operator|.
name|getSource
argument_list|()
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
name|assertThat
argument_list|(
name|response
operator|.
name|getSource
argument_list|()
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
name|ImmutableMap
operator|.
name|of
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
block|}
end_class

end_unit

