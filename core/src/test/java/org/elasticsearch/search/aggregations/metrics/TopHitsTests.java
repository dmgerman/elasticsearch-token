begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.metrics
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|metrics
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
name|index
operator|.
name|query
operator|.
name|AbstractQueryTestCase
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
name|search
operator|.
name|aggregations
operator|.
name|AggregationInitializationException
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
name|aggregations
operator|.
name|BaseAggregationTestCase
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
name|aggregations
operator|.
name|metrics
operator|.
name|tophits
operator|.
name|TopHitsAggregatorBuilder
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
name|source
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
name|hamcrest
operator|.
name|Matchers
operator|.
name|containsString
import|;
end_import

begin_class
DECL|class|TopHitsTests
specifier|public
class|class
name|TopHitsTests
extends|extends
name|BaseAggregationTestCase
argument_list|<
name|TopHitsAggregatorBuilder
argument_list|>
block|{
annotation|@
name|Override
DECL|method|createTestAggregatorBuilder
specifier|protected
specifier|final
name|TopHitsAggregatorBuilder
name|createTestAggregatorBuilder
parameter_list|()
block|{
name|TopHitsAggregatorBuilder
name|factory
init|=
operator|new
name|TopHitsAggregatorBuilder
argument_list|(
literal|"foo"
argument_list|)
decl_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|factory
operator|.
name|from
argument_list|(
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|10000
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
name|factory
operator|.
name|size
argument_list|(
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|10000
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
name|factory
operator|.
name|explain
argument_list|(
name|randomBoolean
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
name|factory
operator|.
name|version
argument_list|(
name|randomBoolean
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
name|factory
operator|.
name|trackScores
argument_list|(
name|randomBoolean
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
name|int
name|fieldsSize
init|=
name|randomInt
argument_list|(
literal|25
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|fields
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|fieldsSize
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
name|fieldsSize
condition|;
name|i
operator|++
control|)
block|{
name|fields
operator|.
name|add
argument_list|(
name|randomAsciiOfLengthBetween
argument_list|(
literal|5
argument_list|,
literal|50
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|factory
operator|.
name|fields
argument_list|(
name|fields
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|int
name|fieldDataFieldsSize
init|=
name|randomInt
argument_list|(
literal|25
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
name|fieldDataFieldsSize
condition|;
name|i
operator|++
control|)
block|{
name|factory
operator|.
name|fieldDataField
argument_list|(
name|randomAsciiOfLengthBetween
argument_list|(
literal|5
argument_list|,
literal|50
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|int
name|scriptFieldsSize
init|=
name|randomInt
argument_list|(
literal|25
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
name|scriptFieldsSize
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|factory
operator|.
name|scriptField
argument_list|(
name|randomAsciiOfLengthBetween
argument_list|(
literal|5
argument_list|,
literal|50
argument_list|)
argument_list|,
operator|new
name|Script
argument_list|(
literal|"foo"
argument_list|)
argument_list|,
name|randomBoolean
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|factory
operator|.
name|scriptField
argument_list|(
name|randomAsciiOfLengthBetween
argument_list|(
literal|5
argument_list|,
literal|50
argument_list|)
argument_list|,
operator|new
name|Script
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|FetchSourceContext
name|fetchSourceContext
decl_stmt|;
name|int
name|branch
init|=
name|randomInt
argument_list|(
literal|5
argument_list|)
decl_stmt|;
name|String
index|[]
name|includes
init|=
operator|new
name|String
index|[
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|20
argument_list|)
index|]
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
name|includes
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|includes
index|[
name|i
index|]
operator|=
name|randomAsciiOfLengthBetween
argument_list|(
literal|5
argument_list|,
literal|20
argument_list|)
expr_stmt|;
block|}
name|String
index|[]
name|excludes
init|=
operator|new
name|String
index|[
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|20
argument_list|)
index|]
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
name|excludes
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|excludes
index|[
name|i
index|]
operator|=
name|randomAsciiOfLengthBetween
argument_list|(
literal|5
argument_list|,
literal|20
argument_list|)
expr_stmt|;
block|}
switch|switch
condition|(
name|branch
condition|)
block|{
case|case
literal|0
case|:
name|fetchSourceContext
operator|=
operator|new
name|FetchSourceContext
argument_list|(
name|randomBoolean
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
literal|1
case|:
name|fetchSourceContext
operator|=
operator|new
name|FetchSourceContext
argument_list|(
name|includes
argument_list|,
name|excludes
argument_list|)
expr_stmt|;
break|break;
case|case
literal|2
case|:
name|fetchSourceContext
operator|=
operator|new
name|FetchSourceContext
argument_list|(
name|randomAsciiOfLengthBetween
argument_list|(
literal|5
argument_list|,
literal|20
argument_list|)
argument_list|,
name|randomAsciiOfLengthBetween
argument_list|(
literal|5
argument_list|,
literal|20
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
literal|3
case|:
name|fetchSourceContext
operator|=
operator|new
name|FetchSourceContext
argument_list|(
literal|true
argument_list|,
name|includes
argument_list|,
name|excludes
argument_list|)
expr_stmt|;
break|break;
case|case
literal|4
case|:
name|fetchSourceContext
operator|=
operator|new
name|FetchSourceContext
argument_list|(
name|includes
argument_list|)
expr_stmt|;
break|break;
case|case
literal|5
case|:
name|fetchSourceContext
operator|=
operator|new
name|FetchSourceContext
argument_list|(
name|randomAsciiOfLengthBetween
argument_list|(
literal|5
argument_list|,
literal|20
argument_list|)
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|IllegalStateException
argument_list|()
throw|;
block|}
name|factory
operator|.
name|fetchSource
argument_list|(
name|fetchSourceContext
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|int
name|numSorts
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|5
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
name|numSorts
condition|;
name|i
operator|++
control|)
block|{
name|int
name|branch
init|=
name|randomInt
argument_list|(
literal|5
argument_list|)
decl_stmt|;
switch|switch
condition|(
name|branch
condition|)
block|{
case|case
literal|0
case|:
name|factory
operator|.
name|sort
argument_list|(
name|SortBuilders
operator|.
name|fieldSort
argument_list|(
name|randomAsciiOfLengthBetween
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
expr_stmt|;
break|break;
case|case
literal|1
case|:
name|factory
operator|.
name|sort
argument_list|(
name|SortBuilders
operator|.
name|geoDistanceSort
argument_list|(
name|randomAsciiOfLengthBetween
argument_list|(
literal|5
argument_list|,
literal|20
argument_list|)
argument_list|,
name|AbstractQueryTestCase
operator|.
name|randomGeohash
argument_list|(
literal|1
argument_list|,
literal|12
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
expr_stmt|;
break|break;
case|case
literal|2
case|:
name|factory
operator|.
name|sort
argument_list|(
name|SortBuilders
operator|.
name|scoreSort
argument_list|()
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
expr_stmt|;
break|break;
case|case
literal|3
case|:
name|factory
operator|.
name|sort
argument_list|(
name|SortBuilders
operator|.
name|scriptSort
argument_list|(
operator|new
name|Script
argument_list|(
literal|"foo"
argument_list|)
argument_list|,
literal|"number"
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
expr_stmt|;
break|break;
case|case
literal|4
case|:
name|factory
operator|.
name|sort
argument_list|(
name|randomAsciiOfLengthBetween
argument_list|(
literal|5
argument_list|,
literal|20
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
literal|5
case|:
name|factory
operator|.
name|sort
argument_list|(
name|randomAsciiOfLengthBetween
argument_list|(
literal|5
argument_list|,
literal|20
argument_list|)
argument_list|,
name|randomFrom
argument_list|(
name|SortOrder
operator|.
name|values
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
block|}
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|factory
operator|.
name|highlighter
argument_list|(
name|HighlightBuilderTests
operator|.
name|randomHighlighterBuilder
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|factory
return|;
block|}
DECL|method|testFailWithSubAgg
specifier|public
name|void
name|testFailWithSubAgg
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|source
init|=
literal|"{\n"
operator|+
literal|"    \"top-tags\": {\n"
operator|+
literal|"      \"terms\": {\n"
operator|+
literal|"        \"field\": \"tags\"\n"
operator|+
literal|"      },\n"
operator|+
literal|"      \"aggs\": {\n"
operator|+
literal|"        \"top_tags_hits\": {\n"
operator|+
literal|"          \"top_hits\": {},\n"
operator|+
literal|"          \"aggs\": {\n"
operator|+
literal|"            \"max\": {\n"
operator|+
literal|"              \"max\": {\n"
operator|+
literal|"                \"field\": \"age\"\n"
operator|+
literal|"              }\n"
operator|+
literal|"            }\n"
operator|+
literal|"          }\n"
operator|+
literal|"        }\n"
operator|+
literal|"      }\n"
operator|+
literal|"    }\n"
operator|+
literal|"}"
decl_stmt|;
try|try
block|{
name|XContentParser
name|parser
init|=
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|source
argument_list|)
operator|.
name|createParser
argument_list|(
name|source
argument_list|)
decl_stmt|;
name|QueryParseContext
name|parseContext
init|=
operator|new
name|QueryParseContext
argument_list|(
name|queriesRegistry
argument_list|)
decl_stmt|;
name|parseContext
operator|.
name|reset
argument_list|(
name|parser
argument_list|)
expr_stmt|;
name|parseContext
operator|.
name|parseFieldMatcher
argument_list|(
name|parseFieldMatcher
argument_list|)
expr_stmt|;
name|assertSame
argument_list|(
name|XContentParser
operator|.
name|Token
operator|.
name|START_OBJECT
argument_list|,
name|parser
operator|.
name|nextToken
argument_list|()
argument_list|)
expr_stmt|;
name|aggParsers
operator|.
name|parseAggregators
argument_list|(
name|parser
argument_list|,
name|parseContext
argument_list|)
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|AggregationInitializationException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|e
operator|.
name|toString
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"Aggregator [top_tags_hits] of type [top_hits] cannot accept sub-aggregations"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

