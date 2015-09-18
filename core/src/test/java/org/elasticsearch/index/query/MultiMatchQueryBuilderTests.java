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
name|index
operator|.
name|Term
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
name|queries
operator|.
name|ExtendedCommonTermsQuery
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
name|*
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
name|lucene
operator|.
name|all
operator|.
name|AllTermQuery
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
name|lucene
operator|.
name|search
operator|.
name|MultiPhrasePrefixQuery
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
name|search
operator|.
name|MatchQuery
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
name|List
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
name|multiMatchQuery
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
name|assertBooleanSubQuery
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
name|*
import|;
end_import

begin_class
DECL|class|MultiMatchQueryBuilderTests
specifier|public
class|class
name|MultiMatchQueryBuilderTests
extends|extends
name|AbstractQueryTestCase
argument_list|<
name|MultiMatchQueryBuilder
argument_list|>
block|{
annotation|@
name|Override
DECL|method|doCreateTestQueryBuilder
specifier|protected
name|MultiMatchQueryBuilder
name|doCreateTestQueryBuilder
parameter_list|()
block|{
name|String
name|fieldName
init|=
name|randomFrom
argument_list|(
name|STRING_FIELD_NAME
argument_list|,
name|INT_FIELD_NAME
argument_list|,
name|DOUBLE_FIELD_NAME
argument_list|,
name|BOOLEAN_FIELD_NAME
argument_list|,
name|DATE_FIELD_NAME
argument_list|)
decl_stmt|;
if|if
condition|(
name|fieldName
operator|.
name|equals
argument_list|(
name|DATE_FIELD_NAME
argument_list|)
condition|)
block|{
name|assumeTrue
argument_list|(
literal|"test with date fields runs only when at least a type is registered"
argument_list|,
name|getCurrentTypes
argument_list|()
operator|.
name|length
operator|>
literal|0
argument_list|)
expr_stmt|;
block|}
comment|// creates the query with random value and field name
name|Object
name|value
decl_stmt|;
if|if
condition|(
name|fieldName
operator|.
name|equals
argument_list|(
name|STRING_FIELD_NAME
argument_list|)
condition|)
block|{
name|value
operator|=
name|getRandomQueryText
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|value
operator|=
name|getRandomValueForFieldName
argument_list|(
name|fieldName
argument_list|)
expr_stmt|;
block|}
name|MultiMatchQueryBuilder
name|query
init|=
operator|new
name|MultiMatchQueryBuilder
argument_list|(
name|value
argument_list|,
name|fieldName
argument_list|)
decl_stmt|;
comment|// field with random boost
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|query
operator|.
name|field
argument_list|(
name|fieldName
argument_list|,
name|randomFloat
argument_list|()
operator|*
literal|10
argument_list|)
expr_stmt|;
block|}
comment|// sets other parameters of the multi match query
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|query
operator|.
name|type
argument_list|(
name|randomFrom
argument_list|(
name|MultiMatchQueryBuilder
operator|.
name|Type
operator|.
name|values
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
name|query
operator|.
name|operator
argument_list|(
name|randomFrom
argument_list|(
name|Operator
operator|.
name|values
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
name|query
operator|.
name|analyzer
argument_list|(
name|randomAnalyzer
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
name|slop
argument_list|(
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|5
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
name|query
operator|.
name|fuzziness
argument_list|(
name|randomFuzziness
argument_list|(
name|fieldName
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
name|query
operator|.
name|prefixLength
argument_list|(
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|5
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
name|query
operator|.
name|maxExpansions
argument_list|(
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|5
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
name|query
operator|.
name|minimumShouldMatch
argument_list|(
name|randomMinimumShouldMatch
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
name|fuzzyRewrite
argument_list|(
name|getRandomRewriteMethod
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
name|useDisMax
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
name|tieBreaker
argument_list|(
name|randomFloat
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
name|lenient
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
name|cutoffFrequency
argument_list|(
operator|(
name|float
operator|)
literal|10
operator|/
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|100
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
name|query
operator|.
name|zeroTermsQuery
argument_list|(
name|randomFrom
argument_list|(
name|MatchQuery
operator|.
name|ZeroTermsQuery
operator|.
name|values
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// test with fields with boost and patterns delegated to the tests further below
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
name|MultiMatchQueryBuilder
name|queryBuilder
parameter_list|,
name|Query
name|query
parameter_list|,
name|QueryShardContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
comment|// we rely on integration tests for deeper checks here
name|assertThat
argument_list|(
name|query
argument_list|,
name|either
argument_list|(
name|instanceOf
argument_list|(
name|TermQuery
operator|.
name|class
argument_list|)
argument_list|)
operator|.
name|or
argument_list|(
name|instanceOf
argument_list|(
name|AllTermQuery
operator|.
name|class
argument_list|)
argument_list|)
operator|.
name|or
argument_list|(
name|instanceOf
argument_list|(
name|BooleanQuery
operator|.
name|class
argument_list|)
argument_list|)
operator|.
name|or
argument_list|(
name|instanceOf
argument_list|(
name|DisjunctionMaxQuery
operator|.
name|class
argument_list|)
argument_list|)
operator|.
name|or
argument_list|(
name|instanceOf
argument_list|(
name|FuzzyQuery
operator|.
name|class
argument_list|)
argument_list|)
operator|.
name|or
argument_list|(
name|instanceOf
argument_list|(
name|MultiPhrasePrefixQuery
operator|.
name|class
argument_list|)
argument_list|)
operator|.
name|or
argument_list|(
name|instanceOf
argument_list|(
name|MatchAllDocsQuery
operator|.
name|class
argument_list|)
argument_list|)
operator|.
name|or
argument_list|(
name|instanceOf
argument_list|(
name|ExtendedCommonTermsQuery
operator|.
name|class
argument_list|)
argument_list|)
operator|.
name|or
argument_list|(
name|instanceOf
argument_list|(
name|MatchNoDocsQuery
operator|.
name|class
argument_list|)
argument_list|)
operator|.
name|or
argument_list|(
name|instanceOf
argument_list|(
name|PhraseQuery
operator|.
name|class
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testIllegaArguments
specifier|public
name|void
name|testIllegaArguments
parameter_list|()
block|{
try|try
block|{
operator|new
name|MultiMatchQueryBuilder
argument_list|(
literal|null
argument_list|,
literal|"field"
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"value must not be null"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
comment|// expected
block|}
try|try
block|{
operator|new
name|MultiMatchQueryBuilder
argument_list|(
literal|"value"
argument_list|,
operator|(
name|String
index|[]
operator|)
literal|null
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"initial fields must be supplied at construction time must not be null"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
comment|// expected
block|}
try|try
block|{
operator|new
name|MultiMatchQueryBuilder
argument_list|(
literal|"value"
argument_list|,
operator|new
name|String
index|[]
block|{
literal|""
block|}
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"field names cannot be empty"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
comment|// expected
block|}
block|}
annotation|@
name|Override
DECL|method|assertBoost
specifier|protected
name|void
name|assertBoost
parameter_list|(
name|MultiMatchQueryBuilder
name|queryBuilder
parameter_list|,
name|Query
name|query
parameter_list|)
throws|throws
name|IOException
block|{
comment|//we delegate boost checks to specific boost tests below
block|}
annotation|@
name|Test
DECL|method|testToQueryBoost
specifier|public
name|void
name|testToQueryBoost
parameter_list|()
throws|throws
name|IOException
block|{
name|assumeTrue
argument_list|(
literal|"test runs only when at least a type is registered"
argument_list|,
name|getCurrentTypes
argument_list|()
operator|.
name|length
operator|>
literal|0
argument_list|)
expr_stmt|;
name|QueryShardContext
name|shardContext
init|=
name|createShardContext
argument_list|()
decl_stmt|;
name|MultiMatchQueryBuilder
name|multiMatchQueryBuilder
init|=
operator|new
name|MultiMatchQueryBuilder
argument_list|(
literal|"test"
argument_list|)
decl_stmt|;
name|multiMatchQueryBuilder
operator|.
name|field
argument_list|(
name|STRING_FIELD_NAME
argument_list|,
literal|5
argument_list|)
expr_stmt|;
name|Query
name|query
init|=
name|multiMatchQueryBuilder
operator|.
name|toQuery
argument_list|(
name|shardContext
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|query
argument_list|,
name|instanceOf
argument_list|(
name|TermQuery
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|query
operator|.
name|getBoost
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|5f
argument_list|)
argument_list|)
expr_stmt|;
name|multiMatchQueryBuilder
operator|=
operator|new
name|MultiMatchQueryBuilder
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
name|multiMatchQueryBuilder
operator|.
name|field
argument_list|(
name|STRING_FIELD_NAME
argument_list|,
literal|5
argument_list|)
expr_stmt|;
name|multiMatchQueryBuilder
operator|.
name|boost
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|query
operator|=
name|multiMatchQueryBuilder
operator|.
name|toQuery
argument_list|(
name|shardContext
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|query
argument_list|,
name|instanceOf
argument_list|(
name|TermQuery
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|query
operator|.
name|getBoost
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|10f
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testToQueryMultipleTermsBooleanQuery
specifier|public
name|void
name|testToQueryMultipleTermsBooleanQuery
parameter_list|()
throws|throws
name|Exception
block|{
name|assumeTrue
argument_list|(
literal|"test runs only when at least a type is registered"
argument_list|,
name|getCurrentTypes
argument_list|()
operator|.
name|length
operator|>
literal|0
argument_list|)
expr_stmt|;
name|Query
name|query
init|=
name|multiMatchQuery
argument_list|(
literal|"test1 test2"
argument_list|)
operator|.
name|field
argument_list|(
name|STRING_FIELD_NAME
argument_list|)
operator|.
name|useDisMax
argument_list|(
literal|false
argument_list|)
operator|.
name|toQuery
argument_list|(
name|createShardContext
argument_list|()
argument_list|)
decl_stmt|;
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
name|bQuery
init|=
operator|(
name|BooleanQuery
operator|)
name|query
decl_stmt|;
name|assertThat
argument_list|(
name|bQuery
operator|.
name|clauses
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|assertBooleanSubQuery
argument_list|(
name|query
argument_list|,
name|TermQuery
operator|.
name|class
argument_list|,
literal|0
argument_list|)
operator|.
name|getTerm
argument_list|()
argument_list|,
name|equalTo
argument_list|(
operator|new
name|Term
argument_list|(
name|STRING_FIELD_NAME
argument_list|,
literal|"test1"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|assertBooleanSubQuery
argument_list|(
name|query
argument_list|,
name|TermQuery
operator|.
name|class
argument_list|,
literal|1
argument_list|)
operator|.
name|getTerm
argument_list|()
argument_list|,
name|equalTo
argument_list|(
operator|new
name|Term
argument_list|(
name|STRING_FIELD_NAME
argument_list|,
literal|"test2"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testToQueryMultipleFieldsBooleanQuery
specifier|public
name|void
name|testToQueryMultipleFieldsBooleanQuery
parameter_list|()
throws|throws
name|Exception
block|{
name|assumeTrue
argument_list|(
literal|"test runs only when at least a type is registered"
argument_list|,
name|getCurrentTypes
argument_list|()
operator|.
name|length
operator|>
literal|0
argument_list|)
expr_stmt|;
name|Query
name|query
init|=
name|multiMatchQuery
argument_list|(
literal|"test"
argument_list|)
operator|.
name|field
argument_list|(
name|STRING_FIELD_NAME
argument_list|)
operator|.
name|field
argument_list|(
name|STRING_FIELD_NAME_2
argument_list|)
operator|.
name|useDisMax
argument_list|(
literal|false
argument_list|)
operator|.
name|toQuery
argument_list|(
name|createShardContext
argument_list|()
argument_list|)
decl_stmt|;
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
name|bQuery
init|=
operator|(
name|BooleanQuery
operator|)
name|query
decl_stmt|;
name|assertThat
argument_list|(
name|bQuery
operator|.
name|clauses
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|assertBooleanSubQuery
argument_list|(
name|query
argument_list|,
name|TermQuery
operator|.
name|class
argument_list|,
literal|0
argument_list|)
operator|.
name|getTerm
argument_list|()
argument_list|,
name|equalTo
argument_list|(
operator|new
name|Term
argument_list|(
name|STRING_FIELD_NAME
argument_list|,
literal|"test"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|assertBooleanSubQuery
argument_list|(
name|query
argument_list|,
name|TermQuery
operator|.
name|class
argument_list|,
literal|1
argument_list|)
operator|.
name|getTerm
argument_list|()
argument_list|,
name|equalTo
argument_list|(
operator|new
name|Term
argument_list|(
name|STRING_FIELD_NAME_2
argument_list|,
literal|"test"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testToQueryMultipleFieldsDisMaxQuery
specifier|public
name|void
name|testToQueryMultipleFieldsDisMaxQuery
parameter_list|()
throws|throws
name|Exception
block|{
name|assumeTrue
argument_list|(
literal|"test runs only when at least a type is registered"
argument_list|,
name|getCurrentTypes
argument_list|()
operator|.
name|length
operator|>
literal|0
argument_list|)
expr_stmt|;
name|Query
name|query
init|=
name|multiMatchQuery
argument_list|(
literal|"test"
argument_list|)
operator|.
name|field
argument_list|(
name|STRING_FIELD_NAME
argument_list|)
operator|.
name|field
argument_list|(
name|STRING_FIELD_NAME_2
argument_list|)
operator|.
name|useDisMax
argument_list|(
literal|true
argument_list|)
operator|.
name|toQuery
argument_list|(
name|createShardContext
argument_list|()
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|query
argument_list|,
name|instanceOf
argument_list|(
name|DisjunctionMaxQuery
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|DisjunctionMaxQuery
name|disMaxQuery
init|=
operator|(
name|DisjunctionMaxQuery
operator|)
name|query
decl_stmt|;
name|List
argument_list|<
name|Query
argument_list|>
name|disjuncts
init|=
name|disMaxQuery
operator|.
name|getDisjuncts
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|TermQuery
operator|)
name|disjuncts
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|)
operator|.
name|getTerm
argument_list|()
argument_list|,
name|equalTo
argument_list|(
operator|new
name|Term
argument_list|(
name|STRING_FIELD_NAME
argument_list|,
literal|"test"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|TermQuery
operator|)
name|disjuncts
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|)
operator|.
name|getTerm
argument_list|()
argument_list|,
name|equalTo
argument_list|(
operator|new
name|Term
argument_list|(
name|STRING_FIELD_NAME_2
argument_list|,
literal|"test"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testToQueryFieldsWildcard
specifier|public
name|void
name|testToQueryFieldsWildcard
parameter_list|()
throws|throws
name|Exception
block|{
name|assumeTrue
argument_list|(
literal|"test runs only when at least a type is registered"
argument_list|,
name|getCurrentTypes
argument_list|()
operator|.
name|length
operator|>
literal|0
argument_list|)
expr_stmt|;
name|Query
name|query
init|=
name|multiMatchQuery
argument_list|(
literal|"test"
argument_list|)
operator|.
name|field
argument_list|(
literal|"mapped_str*"
argument_list|)
operator|.
name|useDisMax
argument_list|(
literal|false
argument_list|)
operator|.
name|toQuery
argument_list|(
name|createShardContext
argument_list|()
argument_list|)
decl_stmt|;
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
name|bQuery
init|=
operator|(
name|BooleanQuery
operator|)
name|query
decl_stmt|;
name|assertThat
argument_list|(
name|bQuery
operator|.
name|clauses
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|assertBooleanSubQuery
argument_list|(
name|query
argument_list|,
name|TermQuery
operator|.
name|class
argument_list|,
literal|0
argument_list|)
operator|.
name|getTerm
argument_list|()
argument_list|,
name|equalTo
argument_list|(
operator|new
name|Term
argument_list|(
name|STRING_FIELD_NAME
argument_list|,
literal|"test"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|assertBooleanSubQuery
argument_list|(
name|query
argument_list|,
name|TermQuery
operator|.
name|class
argument_list|,
literal|1
argument_list|)
operator|.
name|getTerm
argument_list|()
argument_list|,
name|equalTo
argument_list|(
operator|new
name|Term
argument_list|(
name|STRING_FIELD_NAME_2
argument_list|,
literal|"test"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit
