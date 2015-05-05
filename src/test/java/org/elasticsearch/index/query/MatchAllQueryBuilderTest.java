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
name|com
operator|.
name|carrotsearch
operator|.
name|randomizedtesting
operator|.
name|annotations
operator|.
name|Repeat
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
name|Matchers
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
name|Matchers
operator|.
name|is
import|;
end_import

begin_class
annotation|@
name|Repeat
argument_list|(
name|iterations
operator|=
literal|20
argument_list|)
DECL|class|MatchAllQueryBuilderTest
specifier|public
class|class
name|MatchAllQueryBuilderTest
extends|extends
name|BaseQueryTestCase
argument_list|<
name|MatchAllQueryBuilder
argument_list|>
block|{
annotation|@
name|Override
DECL|method|assertLuceneQuery
specifier|protected
name|void
name|assertLuceneQuery
parameter_list|(
name|MatchAllQueryBuilder
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
name|assertThat
argument_list|(
name|query
operator|.
name|getBoost
argument_list|()
argument_list|,
name|is
argument_list|(
name|queryBuilder
operator|.
name|boost
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|createEmptyQueryBuilder
specifier|protected
name|MatchAllQueryBuilder
name|createEmptyQueryBuilder
parameter_list|()
block|{
return|return
operator|new
name|MatchAllQueryBuilder
argument_list|()
return|;
block|}
comment|/**      * @return a MatchAllQuery with random boost between 0.1f and 2.0f      */
annotation|@
name|Override
DECL|method|createTestQueryBuilder
specifier|protected
name|MatchAllQueryBuilder
name|createTestQueryBuilder
parameter_list|()
block|{
name|MatchAllQueryBuilder
name|query
init|=
operator|new
name|MatchAllQueryBuilder
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
return|return
name|query
return|;
block|}
block|}
end_class

end_unit

