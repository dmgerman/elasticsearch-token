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
name|BooleanClause
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
name|BooleanQuery
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
name|MatchAllDocsQuery
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
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
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
name|CoreMatchers
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
name|CoreMatchers
operator|.
name|instanceOf
import|;
end_import

begin_class
DECL|class|BoolQueryBuilderTest
specifier|public
class|class
name|BoolQueryBuilderTest
extends|extends
name|BaseQueryTestCase
argument_list|<
name|BoolQueryBuilder
argument_list|>
block|{
annotation|@
name|Override
DECL|method|doCreateTestQueryBuilder
specifier|protected
name|BoolQueryBuilder
name|doCreateTestQueryBuilder
parameter_list|()
block|{
name|BoolQueryBuilder
name|query
init|=
operator|new
name|BoolQueryBuilder
argument_list|()
decl_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|query
operator|.
name|adjustPureNegative
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
name|query
operator|.
name|disableCoord
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
name|query
operator|.
name|minimumNumberShouldMatch
argument_list|(
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|int
name|mustClauses
init|=
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|3
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
name|mustClauses
condition|;
name|i
operator|++
control|)
block|{
name|query
operator|.
name|must
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
name|int
name|mustNotClauses
init|=
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|3
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
name|mustNotClauses
condition|;
name|i
operator|++
control|)
block|{
name|query
operator|.
name|mustNot
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
name|int
name|shouldClauses
init|=
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|3
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
name|shouldClauses
condition|;
name|i
operator|++
control|)
block|{
name|query
operator|.
name|should
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
name|int
name|filterClauses
init|=
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|3
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
name|filterClauses
condition|;
name|i
operator|++
control|)
block|{
name|query
operator|.
name|filter
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
return|return
name|query
return|;
block|}
annotation|@
name|Override
DECL|method|doAssertLuceneQuery
specifier|protected
name|void
name|doAssertLuceneQuery
parameter_list|(
name|BoolQueryBuilder
name|queryBuilder
parameter_list|,
name|Query
name|query
parameter_list|,
name|QueryParseContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|queryBuilder
operator|.
name|hasClauses
argument_list|()
condition|)
block|{
name|assertThat
argument_list|(
name|query
argument_list|,
name|instanceOf
argument_list|(
name|MatchAllDocsQuery
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|List
argument_list|<
name|BooleanClause
argument_list|>
name|clauses
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|clauses
operator|.
name|addAll
argument_list|(
name|getBooleanClauses
argument_list|(
name|queryBuilder
operator|.
name|must
argument_list|()
argument_list|,
name|BooleanClause
operator|.
name|Occur
operator|.
name|MUST
argument_list|,
name|context
argument_list|)
argument_list|)
expr_stmt|;
name|clauses
operator|.
name|addAll
argument_list|(
name|getBooleanClauses
argument_list|(
name|queryBuilder
operator|.
name|mustNot
argument_list|()
argument_list|,
name|BooleanClause
operator|.
name|Occur
operator|.
name|MUST_NOT
argument_list|,
name|context
argument_list|)
argument_list|)
expr_stmt|;
name|clauses
operator|.
name|addAll
argument_list|(
name|getBooleanClauses
argument_list|(
name|queryBuilder
operator|.
name|should
argument_list|()
argument_list|,
name|BooleanClause
operator|.
name|Occur
operator|.
name|SHOULD
argument_list|,
name|context
argument_list|)
argument_list|)
expr_stmt|;
name|clauses
operator|.
name|addAll
argument_list|(
name|getBooleanClauses
argument_list|(
name|queryBuilder
operator|.
name|filter
argument_list|()
argument_list|,
name|BooleanClause
operator|.
name|Occur
operator|.
name|FILTER
argument_list|,
name|context
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|clauses
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|assertThat
argument_list|(
name|query
argument_list|,
name|instanceOf
argument_list|(
name|MatchAllDocsQuery
operator|.
name|class
argument_list|)
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
name|BooleanQuery
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|BooleanQuery
name|booleanQuery
init|=
operator|(
name|BooleanQuery
operator|)
name|query
decl_stmt|;
if|if
condition|(
name|queryBuilder
operator|.
name|adjustPureNegative
argument_list|()
condition|)
block|{
name|boolean
name|isNegative
init|=
literal|true
decl_stmt|;
for|for
control|(
name|BooleanClause
name|clause
range|:
name|clauses
control|)
block|{
if|if
condition|(
name|clause
operator|.
name|isProhibited
argument_list|()
operator|==
literal|false
condition|)
block|{
name|isNegative
operator|=
literal|false
expr_stmt|;
break|break;
block|}
block|}
if|if
condition|(
name|isNegative
condition|)
block|{
name|clauses
operator|.
name|add
argument_list|(
operator|new
name|BooleanClause
argument_list|(
operator|new
name|MatchAllDocsQuery
argument_list|()
argument_list|,
name|BooleanClause
operator|.
name|Occur
operator|.
name|MUST
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|assertThat
argument_list|(
name|booleanQuery
operator|.
name|clauses
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|clauses
operator|.
name|size
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|Iterator
argument_list|<
name|BooleanClause
argument_list|>
name|clauseIterator
init|=
name|clauses
operator|.
name|iterator
argument_list|()
decl_stmt|;
for|for
control|(
name|BooleanClause
name|booleanClause
range|:
name|booleanQuery
operator|.
name|getClauses
argument_list|()
control|)
block|{
name|assertThat
argument_list|(
name|booleanClause
argument_list|,
name|equalTo
argument_list|(
name|clauseIterator
operator|.
name|next
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
DECL|method|getBooleanClauses
specifier|private
specifier|static
name|List
argument_list|<
name|BooleanClause
argument_list|>
name|getBooleanClauses
parameter_list|(
name|List
argument_list|<
name|QueryBuilder
argument_list|>
name|queryBuilders
parameter_list|,
name|BooleanClause
operator|.
name|Occur
name|occur
parameter_list|,
name|QueryParseContext
name|parseContext
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|BooleanClause
argument_list|>
name|clauses
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|QueryBuilder
name|query
range|:
name|queryBuilders
control|)
block|{
name|Query
name|innerQuery
init|=
name|query
operator|.
name|toQuery
argument_list|(
name|parseContext
argument_list|)
decl_stmt|;
if|if
condition|(
name|innerQuery
operator|!=
literal|null
condition|)
block|{
name|clauses
operator|.
name|add
argument_list|(
operator|new
name|BooleanClause
argument_list|(
name|innerQuery
argument_list|,
name|occur
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|clauses
return|;
block|}
annotation|@
name|Test
DECL|method|testValidate
specifier|public
name|void
name|testValidate
parameter_list|()
block|{
name|BoolQueryBuilder
name|booleanQuery
init|=
operator|new
name|BoolQueryBuilder
argument_list|()
decl_stmt|;
name|int
name|iters
init|=
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|3
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
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|booleanQuery
operator|.
name|must
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
block|}
else|else
block|{
name|booleanQuery
operator|.
name|must
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
name|totalExpectedErrors
operator|++
expr_stmt|;
block|}
else|else
block|{
name|booleanQuery
operator|.
name|must
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
name|iters
operator|=
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|3
argument_list|)
expr_stmt|;
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
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|booleanQuery
operator|.
name|should
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
block|}
else|else
block|{
name|booleanQuery
operator|.
name|should
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
name|totalExpectedErrors
operator|++
expr_stmt|;
block|}
else|else
block|{
name|booleanQuery
operator|.
name|should
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
name|iters
operator|=
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|3
argument_list|)
expr_stmt|;
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
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|booleanQuery
operator|.
name|mustNot
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
block|}
else|else
block|{
name|booleanQuery
operator|.
name|mustNot
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
name|totalExpectedErrors
operator|++
expr_stmt|;
block|}
else|else
block|{
name|booleanQuery
operator|.
name|mustNot
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
name|iters
operator|=
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|3
argument_list|)
expr_stmt|;
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
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|booleanQuery
operator|.
name|filter
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
block|}
else|else
block|{
name|booleanQuery
operator|.
name|filter
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
name|totalExpectedErrors
operator|++
expr_stmt|;
block|}
else|else
block|{
name|booleanQuery
operator|.
name|filter
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
name|booleanQuery
argument_list|,
name|totalExpectedErrors
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

