begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
package|;
end_package

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
name|SearchPhaseExecutionException
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
name|xcontent
operator|.
name|json
operator|.
name|JsonXContent
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
name|ElasticsearchIntegrationTest
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
name|security
operator|.
name|SecureRandom
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Matcher
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
import|;
end_import

begin_class
DECL|class|ParsingTests
specifier|public
class|class
name|ParsingTests
extends|extends
name|ElasticsearchIntegrationTest
block|{
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|SearchPhaseExecutionException
operator|.
name|class
argument_list|)
DECL|method|testTwoTypes
specifier|public
name|void
name|testTwoTypes
parameter_list|()
throws|throws
name|Exception
block|{
name|createIndex
argument_list|(
literal|"idx"
argument_list|)
expr_stmt|;
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"idx"
argument_list|)
operator|.
name|setAggregations
argument_list|(
name|JsonXContent
operator|.
name|contentBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"in_stock"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"filter"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"range"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"stock"
argument_list|)
operator|.
name|field
argument_list|(
literal|"gt"
argument_list|,
literal|0
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"terms"
argument_list|)
operator|.
name|field
argument_list|(
literal|"field"
argument_list|,
literal|"stock"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|SearchPhaseExecutionException
operator|.
name|class
argument_list|)
DECL|method|testInvalidAggregationName
specifier|public
name|void
name|testInvalidAggregationName
parameter_list|()
throws|throws
name|Exception
block|{
name|Matcher
name|matcher
init|=
name|Pattern
operator|.
name|compile
argument_list|(
literal|"[a-zA-Z0-9\\-_]+"
argument_list|)
operator|.
name|matcher
argument_list|(
literal|""
argument_list|)
decl_stmt|;
name|String
name|name
decl_stmt|;
name|SecureRandom
name|rand
init|=
operator|new
name|SecureRandom
argument_list|()
decl_stmt|;
name|int
name|len
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|5
argument_list|)
decl_stmt|;
name|char
index|[]
name|word
init|=
operator|new
name|char
index|[
name|len
index|]
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|word
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|word
index|[
name|i
index|]
operator|=
operator|(
name|char
operator|)
name|rand
operator|.
name|nextInt
argument_list|(
literal|127
argument_list|)
expr_stmt|;
block|}
name|name
operator|=
name|String
operator|.
name|valueOf
argument_list|(
name|word
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|matcher
operator|.
name|reset
argument_list|(
name|name
argument_list|)
operator|.
name|matches
argument_list|()
condition|)
block|{
break|break;
block|}
block|}
name|createIndex
argument_list|(
literal|"idx"
argument_list|)
expr_stmt|;
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"idx"
argument_list|)
operator|.
name|setAggregations
argument_list|(
name|JsonXContent
operator|.
name|contentBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
name|name
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"filter"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"range"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"stock"
argument_list|)
operator|.
name|field
argument_list|(
literal|"gt"
argument_list|,
literal|0
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

