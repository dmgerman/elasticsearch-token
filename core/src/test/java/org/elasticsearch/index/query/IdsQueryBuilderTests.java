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
name|TermInSetQuery
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
name|MatchNoDocsQuery
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
name|index
operator|.
name|mapper
operator|.
name|UidFieldMapper
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
name|Matchers
operator|.
name|contains
import|;
end_import

begin_class
DECL|class|IdsQueryBuilderTests
specifier|public
class|class
name|IdsQueryBuilderTests
extends|extends
name|AbstractQueryTestCase
argument_list|<
name|IdsQueryBuilder
argument_list|>
block|{
annotation|@
name|Override
DECL|method|doCreateTestQueryBuilder
specifier|protected
name|IdsQueryBuilder
name|doCreateTestQueryBuilder
parameter_list|()
block|{
name|String
index|[]
name|types
decl_stmt|;
if|if
condition|(
name|getCurrentTypes
argument_list|()
operator|.
name|length
operator|>
literal|0
operator|&&
name|randomBoolean
argument_list|()
condition|)
block|{
name|int
name|numberOfTypes
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
name|getCurrentTypes
argument_list|()
operator|.
name|length
argument_list|)
decl_stmt|;
name|types
operator|=
operator|new
name|String
index|[
name|numberOfTypes
index|]
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
name|numberOfTypes
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|frequently
argument_list|()
condition|)
block|{
name|types
index|[
name|i
index|]
operator|=
name|randomFrom
argument_list|(
name|getCurrentTypes
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|types
index|[
name|i
index|]
operator|=
name|randomAlphaOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|types
operator|=
operator|new
name|String
index|[]
block|{
name|MetaData
operator|.
name|ALL
block|}
expr_stmt|;
block|}
else|else
block|{
name|types
operator|=
operator|new
name|String
index|[
literal|0
index|]
expr_stmt|;
block|}
block|}
name|int
name|numberOfIds
init|=
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|String
index|[]
name|ids
init|=
operator|new
name|String
index|[
name|numberOfIds
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
name|numberOfIds
condition|;
name|i
operator|++
control|)
block|{
name|ids
index|[
name|i
index|]
operator|=
name|randomAlphaOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
expr_stmt|;
block|}
name|IdsQueryBuilder
name|query
decl_stmt|;
if|if
condition|(
name|types
operator|.
name|length
operator|>
literal|0
operator|||
name|randomBoolean
argument_list|()
condition|)
block|{
name|query
operator|=
operator|new
name|IdsQueryBuilder
argument_list|()
operator|.
name|types
argument_list|(
name|types
argument_list|)
expr_stmt|;
name|query
operator|.
name|addIds
argument_list|(
name|ids
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|query
operator|=
operator|new
name|IdsQueryBuilder
argument_list|()
expr_stmt|;
name|query
operator|.
name|addIds
argument_list|(
name|ids
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
name|IdsQueryBuilder
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
if|if
condition|(
name|queryBuilder
operator|.
name|ids
argument_list|()
operator|.
name|size
argument_list|()
operator|==
literal|0
operator|||
name|context
operator|.
name|getQueryShardContext
argument_list|()
operator|.
name|fieldMapper
argument_list|(
name|UidFieldMapper
operator|.
name|NAME
argument_list|)
operator|==
literal|null
condition|)
block|{
name|assertThat
argument_list|(
name|query
argument_list|,
name|instanceOf
argument_list|(
name|MatchNoDocsQuery
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
name|TermInSetQuery
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
name|IllegalArgumentException
name|e
init|=
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
operator|new
name|IdsQueryBuilder
argument_list|()
operator|.
name|types
argument_list|(
operator|(
name|String
index|[]
operator|)
literal|null
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"[ids] types cannot be null"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|IdsQueryBuilder
name|idsQueryBuilder
init|=
operator|new
name|IdsQueryBuilder
argument_list|()
decl_stmt|;
name|e
operator|=
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|idsQueryBuilder
operator|.
name|addIds
argument_list|(
operator|(
name|String
index|[]
operator|)
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"[ids] ids cannot be null"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// see #7686.
DECL|method|testIdsQueryWithInvalidValues
specifier|public
name|void
name|testIdsQueryWithInvalidValues
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|query
init|=
literal|"{ \"ids\": { \"values\": [[1]] } }"
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
name|query
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"[ids] failed to parse field [values]"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
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
literal|"  \"ids\" : {\n"
operator|+
literal|"    \"type\" : [ \"my_type\" ],\n"
operator|+
literal|"    \"values\" : [ \"1\", \"100\", \"4\" ],\n"
operator|+
literal|"    \"boost\" : 1.0\n"
operator|+
literal|"  }\n"
operator|+
literal|"}"
decl_stmt|;
name|IdsQueryBuilder
name|parsed
init|=
operator|(
name|IdsQueryBuilder
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
name|assertThat
argument_list|(
name|parsed
operator|.
name|ids
argument_list|()
argument_list|,
name|contains
argument_list|(
literal|"1"
argument_list|,
literal|"100"
argument_list|,
literal|"4"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|json
argument_list|,
literal|"my_type"
argument_list|,
name|parsed
operator|.
name|types
argument_list|()
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
comment|// check that type that is not an array and also ids that are numbers are parsed
name|json
operator|=
literal|"{\n"
operator|+
literal|"  \"ids\" : {\n"
operator|+
literal|"    \"type\" : \"my_type\",\n"
operator|+
literal|"    \"values\" : [ 1, 100, 4 ],\n"
operator|+
literal|"    \"boost\" : 1.0\n"
operator|+
literal|"  }\n"
operator|+
literal|"}"
expr_stmt|;
name|parsed
operator|=
operator|(
name|IdsQueryBuilder
operator|)
name|parseQuery
argument_list|(
name|json
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|parsed
operator|.
name|ids
argument_list|()
argument_list|,
name|contains
argument_list|(
literal|"1"
argument_list|,
literal|"100"
argument_list|,
literal|"4"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|json
argument_list|,
literal|"my_type"
argument_list|,
name|parsed
operator|.
name|types
argument_list|()
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
comment|// check with empty type array
name|json
operator|=
literal|"{\n"
operator|+
literal|"  \"ids\" : {\n"
operator|+
literal|"    \"type\" : [ ],\n"
operator|+
literal|"    \"values\" : [ \"1\", \"100\", \"4\" ],\n"
operator|+
literal|"    \"boost\" : 1.0\n"
operator|+
literal|"  }\n"
operator|+
literal|"}"
expr_stmt|;
name|parsed
operator|=
operator|(
name|IdsQueryBuilder
operator|)
name|parseQuery
argument_list|(
name|json
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|parsed
operator|.
name|ids
argument_list|()
argument_list|,
name|contains
argument_list|(
literal|"1"
argument_list|,
literal|"100"
argument_list|,
literal|"4"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|json
argument_list|,
literal|0
argument_list|,
name|parsed
operator|.
name|types
argument_list|()
operator|.
name|length
argument_list|)
expr_stmt|;
comment|// check without type
name|json
operator|=
literal|"{\n"
operator|+
literal|"  \"ids\" : {\n"
operator|+
literal|"    \"values\" : [ \"1\", \"100\", \"4\" ],\n"
operator|+
literal|"    \"boost\" : 1.0\n"
operator|+
literal|"  }\n"
operator|+
literal|"}"
expr_stmt|;
name|parsed
operator|=
operator|(
name|IdsQueryBuilder
operator|)
name|parseQuery
argument_list|(
name|json
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|parsed
operator|.
name|ids
argument_list|()
argument_list|,
name|contains
argument_list|(
literal|"1"
argument_list|,
literal|"100"
argument_list|,
literal|"4"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|json
argument_list|,
literal|0
argument_list|,
name|parsed
operator|.
name|types
argument_list|()
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
DECL|method|testFromJsonDeprecatedSyntax
specifier|public
name|void
name|testFromJsonDeprecatedSyntax
parameter_list|()
throws|throws
name|IOException
block|{
name|IdsQueryBuilder
name|testQuery
init|=
operator|new
name|IdsQueryBuilder
argument_list|()
operator|.
name|types
argument_list|(
literal|"my_type"
argument_list|)
decl_stmt|;
comment|//single value type can also be called _type
specifier|final
name|String
name|contentString
init|=
literal|"{\n"
operator|+
literal|"    \"ids\" : {\n"
operator|+
literal|"        \"_type\" : \"my_type\",\n"
operator|+
literal|"        \"values\" : [ ]\n"
operator|+
literal|"    }\n"
operator|+
literal|"}"
decl_stmt|;
name|IdsQueryBuilder
name|parsed
init|=
operator|(
name|IdsQueryBuilder
operator|)
name|parseQuery
argument_list|(
name|contentString
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|testQuery
argument_list|,
name|parsed
argument_list|)
expr_stmt|;
name|parseQuery
argument_list|(
name|contentString
argument_list|)
expr_stmt|;
name|assertWarnings
argument_list|(
literal|"Deprecated field [_type] used, expected [type] instead"
argument_list|)
expr_stmt|;
comment|//array of types can also be called types rather than type
specifier|final
name|String
name|contentString2
init|=
literal|"{\n"
operator|+
literal|"    \"ids\" : {\n"
operator|+
literal|"        \"types\" : [\"my_type\"],\n"
operator|+
literal|"        \"values\" : [ ]\n"
operator|+
literal|"    }\n"
operator|+
literal|"}"
decl_stmt|;
name|parsed
operator|=
operator|(
name|IdsQueryBuilder
operator|)
name|parseQuery
argument_list|(
name|contentString2
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|testQuery
argument_list|,
name|parsed
argument_list|)
expr_stmt|;
name|parseQuery
argument_list|(
name|contentString2
argument_list|)
expr_stmt|;
name|assertWarnings
argument_list|(
literal|"Deprecated field [types] used, expected [type] instead"
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

