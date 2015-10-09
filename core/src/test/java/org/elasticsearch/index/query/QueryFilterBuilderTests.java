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
annotation|@
name|SuppressWarnings
argument_list|(
literal|"deprecation"
argument_list|)
DECL|class|QueryFilterBuilderTests
specifier|public
class|class
name|QueryFilterBuilderTests
extends|extends
name|AbstractQueryTestCase
argument_list|<
name|QueryFilterBuilder
argument_list|>
block|{
annotation|@
name|Override
DECL|method|doCreateTestQueryBuilder
specifier|protected
name|QueryFilterBuilder
name|doCreateTestQueryBuilder
parameter_list|()
block|{
name|QueryBuilder
name|innerQuery
init|=
name|RandomQueryBuilder
operator|.
name|createQuery
argument_list|(
name|random
argument_list|()
argument_list|)
decl_stmt|;
return|return
operator|new
name|QueryFilterBuilder
argument_list|(
name|innerQuery
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
name|QueryFilterBuilder
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
annotation|@
name|Override
DECL|method|supportsBoostAndQueryName
specifier|protected
name|boolean
name|supportsBoostAndQueryName
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
comment|/**      * test that wrapping an inner filter that returns<tt>null</tt> also returns<tt>null</tt> to pass on upwards      */
DECL|method|testInnerQueryReturnsNull
specifier|public
name|void
name|testInnerQueryReturnsNull
parameter_list|()
throws|throws
name|IOException
block|{
comment|// create inner filter
name|String
name|queryString
init|=
literal|"{ \"constant_score\" : { \"filter\" : {} } }"
decl_stmt|;
name|QueryBuilder
argument_list|<
name|?
argument_list|>
name|innerQuery
init|=
name|parseQuery
argument_list|(
name|queryString
argument_list|)
decl_stmt|;
comment|// check that when wrapping this filter, toQuery() returns null
name|QueryFilterBuilder
name|queryFilterQuery
init|=
operator|new
name|QueryFilterBuilder
argument_list|(
name|innerQuery
argument_list|)
decl_stmt|;
name|assertNull
argument_list|(
name|queryFilterQuery
operator|.
name|toQuery
argument_list|(
name|createShardContext
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testValidate
specifier|public
name|void
name|testValidate
parameter_list|()
block|{
try|try
block|{
operator|new
name|QueryFilterBuilder
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"cannot be null"
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
block|}
end_class

end_unit

