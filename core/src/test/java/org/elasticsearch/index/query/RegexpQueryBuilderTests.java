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
name|Query
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
name|RegexpQuery
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
name|HashMap
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
name|Matchers
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
name|Matchers
operator|.
name|instanceOf
import|;
end_import

begin_class
DECL|class|RegexpQueryBuilderTests
specifier|public
class|class
name|RegexpQueryBuilderTests
extends|extends
name|AbstractQueryTestCase
argument_list|<
name|RegexpQueryBuilder
argument_list|>
block|{
annotation|@
name|Override
DECL|method|doCreateTestQueryBuilder
specifier|protected
name|RegexpQueryBuilder
name|doCreateTestQueryBuilder
parameter_list|()
block|{
name|RegexpQueryBuilder
name|query
init|=
name|randomRegexpQuery
argument_list|()
decl_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|List
argument_list|<
name|RegexpFlag
argument_list|>
name|flags
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|int
name|iter
init|=
name|randomInt
argument_list|(
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
name|iter
condition|;
name|i
operator|++
control|)
block|{
name|flags
operator|.
name|add
argument_list|(
name|randomFrom
argument_list|(
name|RegexpFlag
operator|.
name|values
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|query
operator|.
name|flags
argument_list|(
name|flags
operator|.
name|toArray
argument_list|(
operator|new
name|RegexpFlag
index|[
name|flags
operator|.
name|size
argument_list|()
index|]
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
name|maxDeterminizedStates
argument_list|(
name|randomInt
argument_list|(
literal|50000
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
name|rewrite
argument_list|(
name|randomFrom
argument_list|(
name|getRandomRewriteMethod
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
DECL|method|getAlternateVersions
specifier|protected
name|Map
argument_list|<
name|String
argument_list|,
name|RegexpQueryBuilder
argument_list|>
name|getAlternateVersions
parameter_list|()
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|RegexpQueryBuilder
argument_list|>
name|alternateVersions
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|RegexpQueryBuilder
name|regexpQuery
init|=
name|randomRegexpQuery
argument_list|()
decl_stmt|;
name|String
name|contentString
init|=
literal|"{\n"
operator|+
literal|"    \"regexp\" : {\n"
operator|+
literal|"        \""
operator|+
name|regexpQuery
operator|.
name|fieldName
argument_list|()
operator|+
literal|"\" : \""
operator|+
name|regexpQuery
operator|.
name|value
argument_list|()
operator|+
literal|"\"\n"
operator|+
literal|"    }\n"
operator|+
literal|"}"
decl_stmt|;
name|alternateVersions
operator|.
name|put
argument_list|(
name|contentString
argument_list|,
name|regexpQuery
argument_list|)
expr_stmt|;
return|return
name|alternateVersions
return|;
block|}
DECL|method|randomRegexpQuery
specifier|private
specifier|static
name|RegexpQueryBuilder
name|randomRegexpQuery
parameter_list|()
block|{
comment|// mapped or unmapped fields
name|String
name|fieldName
init|=
name|randomBoolean
argument_list|()
condition|?
name|STRING_FIELD_NAME
else|:
name|randomAlphaOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|String
name|value
init|=
name|randomAlphaOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
decl_stmt|;
return|return
operator|new
name|RegexpQueryBuilder
argument_list|(
name|fieldName
argument_list|,
name|value
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
name|RegexpQueryBuilder
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
name|assertThat
argument_list|(
name|query
argument_list|,
name|instanceOf
argument_list|(
name|RegexpQuery
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|RegexpQuery
name|regexpQuery
init|=
operator|(
name|RegexpQuery
operator|)
name|query
decl_stmt|;
name|assertThat
argument_list|(
name|regexpQuery
operator|.
name|getField
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|queryBuilder
operator|.
name|fieldName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
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
name|RegexpQueryBuilder
argument_list|(
literal|null
argument_list|,
literal|"text"
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"field name is null or empty"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
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
operator|new
name|RegexpQueryBuilder
argument_list|(
literal|""
argument_list|,
literal|"text"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"field name is null or empty"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
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
operator|new
name|RegexpQueryBuilder
argument_list|(
literal|"field"
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"value cannot be null"
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
literal|"  \"regexp\" : {\n"
operator|+
literal|"    \"name.first\" : {\n"
operator|+
literal|"      \"value\" : \"s.*y\",\n"
operator|+
literal|"      \"flags_value\" : 7,\n"
operator|+
literal|"      \"max_determinized_states\" : 20000,\n"
operator|+
literal|"      \"boost\" : 1.0\n"
operator|+
literal|"    }\n"
operator|+
literal|"  }\n"
operator|+
literal|"}"
decl_stmt|;
name|RegexpQueryBuilder
name|parsed
init|=
operator|(
name|RegexpQueryBuilder
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
literal|"s.*y"
argument_list|,
name|parsed
operator|.
name|value
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|json
argument_list|,
literal|20000
argument_list|,
name|parsed
operator|.
name|maxDeterminizedStates
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testNumeric
specifier|public
name|void
name|testNumeric
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
name|RegexpQueryBuilder
name|query
init|=
operator|new
name|RegexpQueryBuilder
argument_list|(
name|INT_FIELD_NAME
argument_list|,
literal|"12"
argument_list|)
decl_stmt|;
name|QueryShardContext
name|context
init|=
name|createShardContext
argument_list|()
decl_stmt|;
name|QueryShardException
name|e
init|=
name|expectThrows
argument_list|(
name|QueryShardException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|query
operator|.
name|toQuery
argument_list|(
name|context
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"Can only use regexp queries on keyword and text fields - not on [mapped_int] which is of type [integer]"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testParseFailsWithMultipleFields
specifier|public
name|void
name|testParseFailsWithMultipleFields
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|json
init|=
literal|"{\n"
operator|+
literal|"    \"regexp\": {\n"
operator|+
literal|"      \"user1\": {\n"
operator|+
literal|"        \"value\": \"k.*y\"\n"
operator|+
literal|"      },\n"
operator|+
literal|"      \"user2\": {\n"
operator|+
literal|"        \"value\": \"k.*y\"\n"
operator|+
literal|"      }\n"
operator|+
literal|"    }\n"
operator|+
literal|"}"
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
name|json
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"[regexp] query doesn't support multiple fields, found [user1] and [user2]"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|String
name|shortJson
init|=
literal|"{\n"
operator|+
literal|"    \"regexp\": {\n"
operator|+
literal|"      \"user1\": \"k.*y\",\n"
operator|+
literal|"      \"user2\": \"k.*y\"\n"
operator|+
literal|"    }\n"
operator|+
literal|"}"
decl_stmt|;
name|e
operator|=
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
name|shortJson
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"[regexp] query doesn't support multiple fields, found [user1] and [user2]"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

