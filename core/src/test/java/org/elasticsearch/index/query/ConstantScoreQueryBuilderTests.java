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
name|hamcrest
operator|.
name|CoreMatchers
operator|.
name|*
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
name|QueryShardContext
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
name|equalTo
argument_list|(
name|innerQuery
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      * test that missing "filter" element causes {@link QueryParsingException}      */
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|QueryParsingException
operator|.
name|class
argument_list|)
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
literal|"\" : {}"
decl_stmt|;
name|parseQuery
argument_list|(
name|queryString
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
name|QueryBuilder
name|innerQuery
init|=
literal|null
decl_stmt|;
name|int
name|totalExpectedErrors
init|=
literal|0
decl_stmt|;
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
name|innerQuery
operator|=
name|RandomQueryBuilder
operator|.
name|createInvalidQuery
argument_list|(
name|random
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|totalExpectedErrors
operator|++
expr_stmt|;
block|}
else|else
block|{
name|innerQuery
operator|=
name|RandomQueryBuilder
operator|.
name|createQuery
argument_list|(
name|random
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|ConstantScoreQueryBuilder
name|constantScoreQuery
init|=
operator|new
name|ConstantScoreQueryBuilder
argument_list|(
name|innerQuery
argument_list|)
decl_stmt|;
name|assertValidate
argument_list|(
name|constantScoreQuery
argument_list|,
name|totalExpectedErrors
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

