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
name|cluster
operator|.
name|metadata
operator|.
name|MetaData
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
name|mapper
operator|.
name|object
operator|.
name|ObjectMapper
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
name|Collection
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
DECL|class|ExistsQueryBuilderTests
specifier|public
class|class
name|ExistsQueryBuilderTests
extends|extends
name|AbstractQueryTestCase
argument_list|<
name|ExistsQueryBuilder
argument_list|>
block|{
annotation|@
name|Override
DECL|method|doCreateTestQueryBuilder
specifier|protected
name|ExistsQueryBuilder
name|doCreateTestQueryBuilder
parameter_list|()
block|{
name|String
name|fieldPattern
decl_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|fieldPattern
operator|=
name|randomFrom
argument_list|(
name|MAPPED_FIELD_NAMES
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|fieldPattern
operator|=
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
expr_stmt|;
block|}
comment|// also sometimes test wildcard patterns
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
name|fieldPattern
operator|=
name|fieldPattern
operator|+
literal|"*"
expr_stmt|;
block|}
else|else
block|{
name|fieldPattern
operator|=
name|MetaData
operator|.
name|ALL
expr_stmt|;
block|}
block|}
return|return
operator|new
name|ExistsQueryBuilder
argument_list|(
name|fieldPattern
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
name|ExistsQueryBuilder
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
name|String
name|fieldPattern
init|=
name|queryBuilder
operator|.
name|fieldName
argument_list|()
decl_stmt|;
name|ObjectMapper
name|objectMapper
init|=
name|context
operator|.
name|getObjectMapper
argument_list|(
name|fieldPattern
argument_list|)
decl_stmt|;
if|if
condition|(
name|objectMapper
operator|!=
literal|null
condition|)
block|{
comment|// automatic make the object mapper pattern
name|fieldPattern
operator|=
name|fieldPattern
operator|+
literal|".*"
expr_stmt|;
block|}
name|Collection
argument_list|<
name|String
argument_list|>
name|fields
init|=
name|context
operator|.
name|simpleMatchToIndexNames
argument_list|(
name|fieldPattern
argument_list|)
decl_stmt|;
if|if
condition|(
name|getCurrentTypes
argument_list|()
operator|.
name|length
operator|==
literal|0
operator|||
name|fields
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
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
literal|0
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
name|constantScoreQuery
operator|.
name|getQuery
argument_list|()
decl_stmt|;
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
name|fields
operator|.
name|size
argument_list|()
argument_list|)
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
name|fields
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|BooleanClause
name|booleanClause
init|=
name|booleanQuery
operator|.
name|clauses
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|booleanClause
operator|.
name|getOccur
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|BooleanClause
operator|.
name|Occur
operator|.
name|SHOULD
argument_list|)
argument_list|)
expr_stmt|;
block|}
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
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
operator|new
name|ExistsQueryBuilder
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
else|else
block|{
operator|new
name|ExistsQueryBuilder
argument_list|(
literal|""
argument_list|)
expr_stmt|;
block|}
name|fail
argument_list|(
literal|"must not be null or empty"
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
literal|"  \"exists\" : {\n"
operator|+
literal|"    \"field\" : \"user\",\n"
operator|+
literal|"    \"boost\" : 42.0\n"
operator|+
literal|"  }\n"
operator|+
literal|"}"
decl_stmt|;
name|ExistsQueryBuilder
name|parsed
init|=
operator|(
name|ExistsQueryBuilder
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
literal|42.0
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
literal|"user"
argument_list|,
name|parsed
operator|.
name|fieldName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

