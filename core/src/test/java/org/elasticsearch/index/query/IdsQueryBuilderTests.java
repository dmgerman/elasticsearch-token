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
name|TermsQuery
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
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
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
comment|/**      * check that parser throws exception on missing values field      */
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|ParsingException
operator|.
name|class
argument_list|)
DECL|method|testIdsNotProvided
specifier|public
name|void
name|testIdsNotProvided
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|noIdsFieldQuery
init|=
literal|"{\"ids\" : { \"type\" : \"my_type\"  }"
decl_stmt|;
name|parseQuery
argument_list|(
name|noIdsFieldQuery
argument_list|)
expr_stmt|;
block|}
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
name|randomAsciiOfLengthBetween
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
name|randomAsciiOfLengthBetween
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
name|QueryShardContext
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
name|assertThat
argument_list|(
operator|(
operator|(
name|BooleanQuery
operator|)
name|query
operator|)
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
name|TermsQuery
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|getAlternateVersions
specifier|protected
name|Map
argument_list|<
name|String
argument_list|,
name|IdsQueryBuilder
argument_list|>
name|getAlternateVersions
parameter_list|()
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|IdsQueryBuilder
argument_list|>
name|alternateVersions
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|IdsQueryBuilder
name|tempQuery
init|=
name|createTestQueryBuilder
argument_list|()
decl_stmt|;
if|if
condition|(
name|tempQuery
operator|.
name|types
argument_list|()
operator|!=
literal|null
operator|&&
name|tempQuery
operator|.
name|types
argument_list|()
operator|.
name|length
operator|>
literal|0
condition|)
block|{
name|String
name|type
init|=
name|tempQuery
operator|.
name|types
argument_list|()
index|[
literal|0
index|]
decl_stmt|;
name|IdsQueryBuilder
name|testQuery
init|=
operator|new
name|IdsQueryBuilder
argument_list|(
name|type
argument_list|)
decl_stmt|;
comment|//single value type can also be called _type
name|String
name|contentString1
init|=
literal|"{\n"
operator|+
literal|"    \"ids\" : {\n"
operator|+
literal|"        \"_type\" : \""
operator|+
name|type
operator|+
literal|"\",\n"
operator|+
literal|"        \"values\" : []\n"
operator|+
literal|"    }\n"
operator|+
literal|"}"
decl_stmt|;
name|alternateVersions
operator|.
name|put
argument_list|(
name|contentString1
argument_list|,
name|testQuery
argument_list|)
expr_stmt|;
comment|//array of types can also be called type rather than types
name|String
name|contentString2
init|=
literal|"{\n"
operator|+
literal|"    \"ids\" : {\n"
operator|+
literal|"        \"type\" : [\""
operator|+
name|type
operator|+
literal|"\"],\n"
operator|+
literal|"        \"values\" : []\n"
operator|+
literal|"    }\n"
operator|+
literal|"}"
decl_stmt|;
name|alternateVersions
operator|.
name|put
argument_list|(
name|contentString2
argument_list|,
name|testQuery
argument_list|)
expr_stmt|;
block|}
return|return
name|alternateVersions
return|;
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
name|IdsQueryBuilder
argument_list|(
operator|(
name|String
index|[]
operator|)
literal|null
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"must be not null"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
comment|//all good
block|}
try|try
block|{
operator|new
name|IdsQueryBuilder
argument_list|()
operator|.
name|addIds
argument_list|(
operator|(
name|String
index|[]
operator|)
literal|null
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"must be not null"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
comment|//all good
block|}
block|}
block|}
end_class

end_unit

