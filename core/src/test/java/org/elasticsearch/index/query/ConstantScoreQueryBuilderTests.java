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
name|apache
operator|.
name|lucene
operator|.
name|search
operator|.
name|ConstantScoreQuery
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
name|search
operator|.
name|Query
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
name|search
operator|.
name|internal
operator|.
name|SearchContext
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
name|AbstractQueryTestCase
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
name|Optional
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|CoreMatchers
operator|.
name|instanceOf
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|CoreMatchers
operator|.
name|nullValue
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|CoreMatchers
operator|.
name|startsWith
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
DECL|class|ConstantScoreQueryBuilderTests
specifier|public
class|class
name|ConstantScoreQueryBuilderTests
extends|extends
name|AbstractQueryTestCase
argument_list|<
name|ConstantScoreQueryBuilder
argument_list|>
block|{
comment|/**      * @return a {@link ConstantScoreQueryBuilder} with random boost between 0.1f and 2.0f      */
annotation|@
name|Override
DECL|method|doCreateTestQueryBuilder
specifier|protected
name|ConstantScoreQueryBuilder
name|doCreateTestQueryBuilder
parameter_list|()
block|{
return|return
operator|new
name|ConstantScoreQueryBuilder
argument_list|(
name|RandomQueryBuilder
operator|.
name|createQuery
argument_list|(
name|random
argument_list|()
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|doAssertLuceneQuery
specifier|protected
name|void
name|doAssertLuceneQuery
parameter_list|(
name|ConstantScoreQueryBuilder
name|queryBuilder
parameter_list|,
name|Query
name|query
parameter_list|,
name|SearchContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|Query
name|innerQuery
init|=
name|queryBuilder
operator|.
name|innerQuery
argument_list|()
operator|.
name|toQuery
argument_list|(
name|context
operator|.
name|getQueryShardContext
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|innerQuery
operator|==
literal|null
condition|)
block|{
name|assertThat
argument_list|(
name|query
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertThat
argument_list|(
name|query
argument_list|,
name|instanceOf
argument_list|(
name|ConstantScoreQuery
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|ConstantScoreQuery
name|constantScoreQuery
init|=
operator|(
name|ConstantScoreQuery
operator|)
name|query
decl_stmt|;
name|assertThat
argument_list|(
name|constantScoreQuery
operator|.
name|getQuery
argument_list|()
argument_list|,
name|instanceOf
argument_list|(
name|innerQuery
operator|.
name|getClass
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      * test that missing "filter" element causes {@link ParsingException}      */
DECL|method|testFilterElement
specifier|public
name|void
name|testFilterElement
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|queryString
init|=
literal|"{ \""
operator|+
name|ConstantScoreQueryBuilder
operator|.
name|NAME
operator|+
literal|"\" : {} }"
decl_stmt|;
name|ParsingException
name|e
init|=
name|expectThrows
argument_list|(
name|ParsingException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|parseQuery
argument_list|(
name|queryString
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"requires a 'filter' element"
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**      * test that multiple "filter" elements causes {@link ParsingException}      */
DECL|method|testMultipleFilterElements
specifier|public
name|void
name|testMultipleFilterElements
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|queryString
init|=
literal|"{ \""
operator|+
name|ConstantScoreQueryBuilder
operator|.
name|NAME
operator|+
literal|"\" : {\n"
operator|+
literal|"\"filter\" : { \"term\": { \"foo\": \"a\" } },\n"
operator|+
literal|"\"filter\" : { \"term\": { \"foo\": \"x\" } },\n"
operator|+
literal|"} }"
decl_stmt|;
name|ParsingException
name|e
init|=
name|expectThrows
argument_list|(
name|ParsingException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|parseQuery
argument_list|(
name|queryString
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"accepts only one 'filter' element"
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**      * test that "filter" does not accept an array of queries, throws {@link ParsingException}      */
DECL|method|testNoArrayAsFilterElements
specifier|public
name|void
name|testNoArrayAsFilterElements
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|queryString
init|=
literal|"{ \""
operator|+
name|ConstantScoreQueryBuilder
operator|.
name|NAME
operator|+
literal|"\" : {\n"
operator|+
literal|"\"filter\" : [ { \"term\": { \"foo\": \"a\" } },\n"
operator|+
literal|"{ \"term\": { \"foo\": \"x\" } } ]\n"
operator|+
literal|"} }"
decl_stmt|;
name|ParsingException
name|e
init|=
name|expectThrows
argument_list|(
name|ParsingException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|parseQuery
argument_list|(
name|queryString
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"unexpected token [START_ARRAY]"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testIllegalArguments
specifier|public
name|void
name|testIllegalArguments
parameter_list|()
block|{
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
operator|new
name|ConstantScoreQueryBuilder
argument_list|(
operator|(
name|QueryBuilder
operator|)
literal|null
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|testUnknownField
specifier|public
name|void
name|testUnknownField
parameter_list|()
throws|throws
name|IOException
block|{
name|assumeTrue
argument_list|(
literal|"test doesn't apply for query filter queries"
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
DECL|method|testFromJson
specifier|public
name|void
name|testFromJson
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|json
init|=
literal|"{\n"
operator|+
literal|"  \"constant_score\" : {\n"
operator|+
literal|"    \"filter\" : {\n"
operator|+
literal|"      \"terms\" : {\n"
operator|+
literal|"        \"user\" : [ \"kimchy\", \"elasticsearch\" ],\n"
operator|+
literal|"        \"boost\" : 42.0\n"
operator|+
literal|"      }\n"
operator|+
literal|"    },\n"
operator|+
literal|"    \"boost\" : 23.0\n"
operator|+
literal|"  }\n"
operator|+
literal|"}"
decl_stmt|;
name|ConstantScoreQueryBuilder
name|parsed
init|=
operator|(
name|ConstantScoreQueryBuilder
operator|)
name|parseQuery
argument_list|(
name|json
argument_list|)
decl_stmt|;
name|checkGeneratedJson
argument_list|(
name|json
argument_list|,
name|parsed
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|json
argument_list|,
literal|23.0
argument_list|,
name|parsed
operator|.
name|boost
argument_list|()
argument_list|,
literal|0.0001
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|json
argument_list|,
literal|42.0
argument_list|,
name|parsed
operator|.
name|innerQuery
argument_list|()
operator|.
name|boost
argument_list|()
argument_list|,
literal|0.0001
argument_list|)
expr_stmt|;
block|}
comment|/**      * we bubble up empty query bodies as an empty optional      */
DECL|method|testFromJsonEmptyQueryBody
specifier|public
name|void
name|testFromJsonEmptyQueryBody
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|query
init|=
literal|"{ \"constant_score\" : {"
operator|+
literal|"    \"filter\" : { }"
operator|+
literal|"  }"
operator|+
literal|"}"
decl_stmt|;
name|XContentParser
name|parser
init|=
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|query
argument_list|)
operator|.
name|createParser
argument_list|(
name|query
argument_list|)
decl_stmt|;
name|QueryParseContext
name|context
init|=
name|createParseContext
argument_list|(
name|parser
argument_list|,
name|ParseFieldMatcher
operator|.
name|EMPTY
argument_list|)
decl_stmt|;
name|Optional
argument_list|<
name|QueryBuilder
argument_list|>
name|innerQueryBuilder
init|=
name|context
operator|.
name|parseInnerQueryBuilder
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|innerQueryBuilder
operator|.
name|isPresent
argument_list|()
operator|==
literal|false
argument_list|)
expr_stmt|;
name|parser
operator|=
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|query
argument_list|)
operator|.
name|createParser
argument_list|(
name|query
argument_list|)
expr_stmt|;
name|QueryParseContext
name|otherContext
init|=
name|createParseContext
argument_list|(
name|parser
argument_list|,
name|ParseFieldMatcher
operator|.
name|STRICT
argument_list|)
decl_stmt|;
name|IllegalArgumentException
name|ex
init|=
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|otherContext
operator|.
name|parseInnerQueryBuilder
argument_list|()
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|ex
operator|.
name|getMessage
argument_list|()
argument_list|,
name|startsWith
argument_list|(
literal|"query malformed, empty clause found at"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

