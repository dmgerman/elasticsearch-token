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
name|DisjunctionMaxQuery
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

begin_class
DECL|class|DisMaxQueryBuilderTest
specifier|public
class|class
name|DisMaxQueryBuilderTest
extends|extends
name|BaseQueryTestCase
argument_list|<
name|DisMaxQueryBuilder
argument_list|>
block|{
annotation|@
name|Override
DECL|method|createExpectedQuery
specifier|protected
name|Query
name|createExpectedQuery
parameter_list|(
name|DisMaxQueryBuilder
name|testBuilder
parameter_list|,
name|QueryParseContext
name|context
parameter_list|)
throws|throws
name|QueryParsingException
throws|,
name|IOException
block|{
name|Query
name|query
init|=
operator|new
name|DisjunctionMaxQuery
argument_list|(
name|AbstractQueryBuilder
operator|.
name|toQueries
argument_list|(
name|testBuilder
operator|.
name|queries
argument_list|()
argument_list|,
name|context
argument_list|)
argument_list|,
name|testBuilder
operator|.
name|tieBreaker
argument_list|()
argument_list|)
decl_stmt|;
name|query
operator|.
name|setBoost
argument_list|(
name|testBuilder
operator|.
name|boost
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|testBuilder
operator|.
name|queryName
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|context
operator|.
name|addNamedQuery
argument_list|(
name|testBuilder
operator|.
name|queryName
argument_list|()
argument_list|,
name|query
argument_list|)
expr_stmt|;
block|}
return|return
name|query
return|;
block|}
comment|/**      * @return a {@link DisMaxQueryBuilder} with random inner queries      */
annotation|@
name|Override
DECL|method|createTestQueryBuilder
specifier|protected
name|DisMaxQueryBuilder
name|createTestQueryBuilder
parameter_list|()
block|{
name|DisMaxQueryBuilder
name|dismax
init|=
operator|new
name|DisMaxQueryBuilder
argument_list|()
decl_stmt|;
name|int
name|clauses
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
name|clauses
condition|;
name|i
operator|++
control|)
block|{
name|dismax
operator|.
name|add
argument_list|(
name|RandomQueryBuilder
operator|.
name|createQuery
argument_list|(
name|random
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
name|dismax
operator|.
name|boost
argument_list|(
literal|2.0f
operator|/
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|20
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
name|dismax
operator|.
name|tieBreaker
argument_list|(
literal|2.0f
operator|/
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|20
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
name|dismax
operator|.
name|queryName
argument_list|(
name|randomUnicodeOfLengthBetween
argument_list|(
literal|3
argument_list|,
literal|15
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|dismax
return|;
block|}
comment|/**      * test `null`return value for missing inner queries      * @throws IOException      * @throws QueryParsingException      */
annotation|@
name|Test
DECL|method|testNoInnerQueries
specifier|public
name|void
name|testNoInnerQueries
parameter_list|()
throws|throws
name|QueryParsingException
throws|,
name|IOException
block|{
name|DisMaxQueryBuilder
name|disMaxBuilder
init|=
operator|new
name|DisMaxQueryBuilder
argument_list|()
decl_stmt|;
name|assertNull
argument_list|(
name|disMaxBuilder
operator|.
name|toQuery
argument_list|(
name|createContext
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|disMaxBuilder
operator|.
name|validate
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**      * Test inner query parsing to null. Current DSL allows inner filter element to parse to<tt>null</tt>.      * Those should be ignored upstream. To test this, we use inner {@link ConstantScoreQueryBuilder}      * with empty inner filter.      */
annotation|@
name|Test
DECL|method|testInnerQueryReturnsNull
specifier|public
name|void
name|testInnerQueryReturnsNull
parameter_list|()
throws|throws
name|IOException
block|{
name|QueryParseContext
name|context
init|=
name|createContext
argument_list|()
decl_stmt|;
name|String
name|queryId
init|=
name|ConstantScoreQueryBuilder
operator|.
name|PROTOTYPE
operator|.
name|getName
argument_list|()
decl_stmt|;
name|String
name|queryString
init|=
literal|"{ \""
operator|+
name|queryId
operator|+
literal|"\" : { \"filter\" : { } }"
decl_stmt|;
name|XContentParser
name|parser
init|=
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|queryString
argument_list|)
operator|.
name|createParser
argument_list|(
name|queryString
argument_list|)
decl_stmt|;
name|context
operator|.
name|reset
argument_list|(
name|parser
argument_list|)
expr_stmt|;
name|assertQueryHeader
argument_list|(
name|parser
argument_list|,
name|queryId
argument_list|)
expr_stmt|;
name|ConstantScoreQueryBuilder
name|innerQueryBuilder
init|=
operator|(
name|ConstantScoreQueryBuilder
operator|)
name|context
operator|.
name|indexQueryParserService
argument_list|()
operator|.
name|queryParser
argument_list|(
name|queryId
argument_list|)
operator|.
name|fromXContent
argument_list|(
name|context
argument_list|)
decl_stmt|;
name|DisMaxQueryBuilder
name|disMaxBuilder
init|=
operator|new
name|DisMaxQueryBuilder
argument_list|()
operator|.
name|add
argument_list|(
name|innerQueryBuilder
argument_list|)
decl_stmt|;
name|assertNull
argument_list|(
name|disMaxBuilder
operator|.
name|toQuery
argument_list|(
name|context
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testValidate
specifier|public
name|void
name|testValidate
parameter_list|()
block|{
name|DisMaxQueryBuilder
name|disMaxQuery
init|=
operator|new
name|DisMaxQueryBuilder
argument_list|()
decl_stmt|;
name|int
name|iters
init|=
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|5
argument_list|)
decl_stmt|;
name|int
name|totalExpectedErrors
init|=
literal|0
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
name|iters
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
name|disMaxQuery
operator|.
name|add
argument_list|(
name|RandomQueryBuilder
operator|.
name|createInvalidQuery
argument_list|(
name|random
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|totalExpectedErrors
operator|++
expr_stmt|;
block|}
else|else
block|{
name|disMaxQuery
operator|.
name|add
argument_list|(
name|RandomQueryBuilder
operator|.
name|createQuery
argument_list|(
name|random
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|assertValidate
argument_list|(
name|disMaxQuery
argument_list|,
name|totalExpectedErrors
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

