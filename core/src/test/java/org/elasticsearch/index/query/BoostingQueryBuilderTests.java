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
name|queries
operator|.
name|BoostingQuery
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

begin_class
DECL|class|BoostingQueryBuilderTests
specifier|public
class|class
name|BoostingQueryBuilderTests
extends|extends
name|AbstractQueryTestCase
argument_list|<
name|BoostingQueryBuilder
argument_list|>
block|{
annotation|@
name|Override
DECL|method|doCreateTestQueryBuilder
specifier|protected
name|BoostingQueryBuilder
name|doCreateTestQueryBuilder
parameter_list|()
block|{
name|BoostingQueryBuilder
name|query
init|=
operator|new
name|BoostingQueryBuilder
argument_list|(
name|RandomQueryBuilder
operator|.
name|createQuery
argument_list|(
name|random
argument_list|()
argument_list|)
argument_list|,
name|RandomQueryBuilder
operator|.
name|createQuery
argument_list|(
name|random
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|query
operator|.
name|negativeBoost
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
name|BoostingQueryBuilder
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
name|Query
name|positive
init|=
name|queryBuilder
operator|.
name|positiveQuery
argument_list|()
operator|.
name|toQuery
argument_list|(
name|context
argument_list|)
decl_stmt|;
name|Query
name|negative
init|=
name|queryBuilder
operator|.
name|negativeQuery
argument_list|()
operator|.
name|toQuery
argument_list|(
name|context
argument_list|)
decl_stmt|;
if|if
condition|(
name|positive
operator|==
literal|null
operator|||
name|negative
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
name|BoostingQuery
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testIllegalArguments
specifier|public
name|void
name|testIllegalArguments
parameter_list|()
block|{
try|try
block|{
operator|new
name|BoostingQueryBuilder
argument_list|(
literal|null
argument_list|,
operator|new
name|MatchAllQueryBuilder
argument_list|()
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"must not be null"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
comment|//
block|}
try|try
block|{
operator|new
name|BoostingQueryBuilder
argument_list|(
operator|new
name|MatchAllQueryBuilder
argument_list|()
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"must not be null"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
comment|//
block|}
try|try
block|{
operator|new
name|BoostingQueryBuilder
argument_list|(
operator|new
name|MatchAllQueryBuilder
argument_list|()
argument_list|,
operator|new
name|MatchAllQueryBuilder
argument_list|()
argument_list|)
operator|.
name|negativeBoost
argument_list|(
operator|-
literal|1.0f
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"must not be negative"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
comment|//
block|}
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
name|query
init|=
literal|"{\n"
operator|+
literal|"  \"boosting\" : {\n"
operator|+
literal|"    \"positive\" : {\n"
operator|+
literal|"      \"term\" : {\n"
operator|+
literal|"        \"field1\" : {\n"
operator|+
literal|"          \"value\" : \"value1\",\n"
operator|+
literal|"          \"boost\" : 5.0\n"
operator|+
literal|"        }\n"
operator|+
literal|"      }\n"
operator|+
literal|"    },\n"
operator|+
literal|"    \"negative\" : {\n"
operator|+
literal|"      \"term\" : {\n"
operator|+
literal|"        \"field2\" : {\n"
operator|+
literal|"          \"value\" : \"value2\",\n"
operator|+
literal|"          \"boost\" : 8.0\n"
operator|+
literal|"        }\n"
operator|+
literal|"      }\n"
operator|+
literal|"    },\n"
operator|+
literal|"    \"negative_boost\" : 23.0,\n"
operator|+
literal|"    \"boost\" : 42.0\n"
operator|+
literal|"  }\n"
operator|+
literal|"}"
decl_stmt|;
name|BoostingQueryBuilder
name|queryBuilder
init|=
operator|(
name|BoostingQueryBuilder
operator|)
name|parseQuery
argument_list|(
name|query
argument_list|)
decl_stmt|;
name|checkGeneratedJson
argument_list|(
name|query
argument_list|,
name|queryBuilder
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|query
argument_list|,
literal|42
argument_list|,
name|queryBuilder
operator|.
name|boost
argument_list|()
argument_list|,
literal|0.00001
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|query
argument_list|,
literal|23
argument_list|,
name|queryBuilder
operator|.
name|negativeBoost
argument_list|()
argument_list|,
literal|0.00001
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|query
argument_list|,
literal|8
argument_list|,
name|queryBuilder
operator|.
name|negativeQuery
argument_list|()
operator|.
name|boost
argument_list|()
argument_list|,
literal|0.00001
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|query
argument_list|,
literal|5
argument_list|,
name|queryBuilder
operator|.
name|positiveQuery
argument_list|()
operator|.
name|boost
argument_list|()
argument_list|,
literal|0.00001
argument_list|)
expr_stmt|;
block|}
DECL|method|testRewrite
specifier|public
name|void
name|testRewrite
parameter_list|()
throws|throws
name|IOException
block|{
name|QueryBuilder
name|positive
init|=
name|randomBoolean
argument_list|()
condition|?
operator|new
name|MatchAllQueryBuilder
argument_list|()
else|:
operator|new
name|WrapperQueryBuilder
argument_list|(
operator|new
name|TermQueryBuilder
argument_list|(
literal|"pos"
argument_list|,
literal|"bar"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
name|QueryBuilder
name|negative
init|=
name|randomBoolean
argument_list|()
condition|?
operator|new
name|MatchAllQueryBuilder
argument_list|()
else|:
operator|new
name|WrapperQueryBuilder
argument_list|(
operator|new
name|TermQueryBuilder
argument_list|(
literal|"neg"
argument_list|,
literal|"bar"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
name|BoostingQueryBuilder
name|qb
init|=
operator|new
name|BoostingQueryBuilder
argument_list|(
name|positive
argument_list|,
name|negative
argument_list|)
decl_stmt|;
name|QueryBuilder
name|rewrite
init|=
name|qb
operator|.
name|rewrite
argument_list|(
name|createShardContext
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|positive
operator|instanceof
name|MatchAllQueryBuilder
operator|&&
name|negative
operator|instanceof
name|MatchAllQueryBuilder
condition|)
block|{
name|assertSame
argument_list|(
name|rewrite
argument_list|,
name|qb
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertNotSame
argument_list|(
name|rewrite
argument_list|,
name|qb
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|new
name|BoostingQueryBuilder
argument_list|(
name|positive
operator|.
name|rewrite
argument_list|(
name|createShardContext
argument_list|()
argument_list|)
argument_list|,
name|negative
operator|.
name|rewrite
argument_list|(
name|createShardContext
argument_list|()
argument_list|)
argument_list|)
argument_list|,
name|rewrite
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

