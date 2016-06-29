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
name|memory
operator|.
name|MemoryIndex
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
name|elasticsearch
operator|.
name|action
operator|.
name|support
operator|.
name|ToXContentToBytes
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
name|common
operator|.
name|bytes
operator|.
name|BytesArray
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
name|bytes
operator|.
name|BytesReference
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
name|io
operator|.
name|UnsupportedEncodingException
import|;
end_import

begin_class
DECL|class|WrapperQueryBuilderTests
specifier|public
class|class
name|WrapperQueryBuilderTests
extends|extends
name|AbstractQueryTestCase
argument_list|<
name|WrapperQueryBuilder
argument_list|>
block|{
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
annotation|@
name|Override
DECL|method|doCreateTestQueryBuilder
specifier|protected
name|WrapperQueryBuilder
name|doCreateTestQueryBuilder
parameter_list|()
block|{
name|QueryBuilder
name|wrappedQuery
init|=
name|RandomQueryBuilder
operator|.
name|createQuery
argument_list|(
name|random
argument_list|()
argument_list|)
decl_stmt|;
switch|switch
condition|(
name|randomInt
argument_list|(
literal|2
argument_list|)
condition|)
block|{
case|case
literal|0
case|:
return|return
operator|new
name|WrapperQueryBuilder
argument_list|(
name|wrappedQuery
operator|.
name|toString
argument_list|()
argument_list|)
return|;
case|case
literal|1
case|:
return|return
operator|new
name|WrapperQueryBuilder
argument_list|(
operator|(
operator|(
name|ToXContentToBytes
operator|)
name|wrappedQuery
operator|)
operator|.
name|buildAsBytes
argument_list|()
operator|.
name|toBytes
argument_list|()
argument_list|)
return|;
case|case
literal|2
case|:
return|return
operator|new
name|WrapperQueryBuilder
argument_list|(
operator|(
operator|(
name|ToXContentToBytes
operator|)
name|wrappedQuery
operator|)
operator|.
name|buildAsBytes
argument_list|()
argument_list|)
return|;
default|default:
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
block|}
annotation|@
name|Override
DECL|method|doAssertLuceneQuery
specifier|protected
name|void
name|doAssertLuceneQuery
parameter_list|(
name|WrapperQueryBuilder
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
name|QueryBuilder
name|innerQuery
init|=
name|queryBuilder
operator|.
name|rewrite
argument_list|(
name|createShardContext
argument_list|()
argument_list|)
decl_stmt|;
name|Query
name|expected
init|=
name|rewrite
argument_list|(
name|innerQuery
operator|.
name|toQuery
argument_list|(
name|context
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|rewrite
argument_list|(
name|query
argument_list|)
argument_list|,
name|expected
argument_list|)
expr_stmt|;
block|}
DECL|method|testIllegalArgument
specifier|public
name|void
name|testIllegalArgument
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
name|WrapperQueryBuilder
argument_list|(
operator|(
name|byte
index|[]
operator|)
literal|null
argument_list|)
expr_stmt|;
block|}
else|else
block|{
operator|new
name|WrapperQueryBuilder
argument_list|(
operator|new
name|byte
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
block|}
name|fail
argument_list|(
literal|"cannot be null or empty"
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
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
operator|new
name|WrapperQueryBuilder
argument_list|(
operator|(
name|String
operator|)
literal|null
argument_list|)
expr_stmt|;
block|}
else|else
block|{
operator|new
name|WrapperQueryBuilder
argument_list|(
literal|""
argument_list|)
expr_stmt|;
block|}
name|fail
argument_list|(
literal|"cannot be null or empty"
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
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
operator|new
name|WrapperQueryBuilder
argument_list|(
operator|(
name|BytesReference
operator|)
literal|null
argument_list|)
expr_stmt|;
block|}
else|else
block|{
operator|new
name|WrapperQueryBuilder
argument_list|(
operator|new
name|BytesArray
argument_list|(
operator|new
name|byte
index|[
literal|0
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|fail
argument_list|(
literal|"cannot be null or empty"
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
comment|/**      * Replace the generic test from superclass, wrapper query only expects      * to find `query` field with nested query and should throw exception for      * anything else.      */
annotation|@
name|Override
DECL|method|testUnknownField
specifier|public
name|void
name|testUnknownField
parameter_list|()
throws|throws
name|IOException
block|{
try|try
block|{
name|parseQuery
argument_list|(
literal|"{ \""
operator|+
name|WrapperQueryBuilder
operator|.
name|NAME
operator|+
literal|"\" : {\"bogusField\" : \"someValue\"} }"
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"ParsingException expected."
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ParsingException
name|e
parameter_list|)
block|{
name|assertTrue
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"bogusField"
argument_list|)
argument_list|)
expr_stmt|;
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
literal|"  \"wrapper\" : {\n"
operator|+
literal|"    \"query\" : \"e30=\"\n"
operator|+
literal|"  }\n"
operator|+
literal|"}"
decl_stmt|;
name|WrapperQueryBuilder
name|parsed
init|=
operator|(
name|WrapperQueryBuilder
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
try|try
block|{
name|assertEquals
argument_list|(
name|json
argument_list|,
literal|"{}"
argument_list|,
operator|new
name|String
argument_list|(
name|parsed
operator|.
name|source
argument_list|()
argument_list|,
literal|"UTF-8"
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UnsupportedEncodingException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
DECL|method|testMustRewrite
specifier|public
name|void
name|testMustRewrite
parameter_list|()
throws|throws
name|IOException
block|{
name|TermQueryBuilder
name|tqb
init|=
operator|new
name|TermQueryBuilder
argument_list|(
literal|"foo"
argument_list|,
literal|"bar"
argument_list|)
decl_stmt|;
name|WrapperQueryBuilder
name|qb
init|=
operator|new
name|WrapperQueryBuilder
argument_list|(
name|tqb
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
name|qb
operator|.
name|toQuery
argument_list|(
name|createShardContext
argument_list|()
argument_list|)
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UnsupportedOperationException
name|e
parameter_list|)
block|{
name|assertEquals
argument_list|(
literal|"this query must be rewritten first"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
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
name|assertEquals
argument_list|(
name|tqb
argument_list|,
name|rewrite
argument_list|)
expr_stmt|;
block|}
DECL|method|testRewriteWithInnerName
specifier|public
name|void
name|testRewriteWithInnerName
parameter_list|()
throws|throws
name|IOException
block|{
name|QueryBuilder
name|builder
init|=
operator|new
name|WrapperQueryBuilder
argument_list|(
literal|"{ \"match_all\" : {\"_name\" : \"foobar\"}}"
argument_list|)
decl_stmt|;
name|QueryShardContext
name|shardContext
init|=
name|createShardContext
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
operator|new
name|MatchAllQueryBuilder
argument_list|()
operator|.
name|queryName
argument_list|(
literal|"foobar"
argument_list|)
argument_list|,
name|builder
operator|.
name|rewrite
argument_list|(
name|shardContext
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|=
operator|new
name|WrapperQueryBuilder
argument_list|(
literal|"{ \"match_all\" : {\"_name\" : \"foobar\"}}"
argument_list|)
operator|.
name|queryName
argument_list|(
literal|"outer"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|new
name|BoolQueryBuilder
argument_list|()
operator|.
name|must
argument_list|(
operator|new
name|MatchAllQueryBuilder
argument_list|()
operator|.
name|queryName
argument_list|(
literal|"foobar"
argument_list|)
argument_list|)
operator|.
name|queryName
argument_list|(
literal|"outer"
argument_list|)
argument_list|,
name|builder
operator|.
name|rewrite
argument_list|(
name|shardContext
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testRewriteWithInnerBoost
specifier|public
name|void
name|testRewriteWithInnerBoost
parameter_list|()
throws|throws
name|IOException
block|{
specifier|final
name|TermQueryBuilder
name|query
init|=
operator|new
name|TermQueryBuilder
argument_list|(
literal|"foo"
argument_list|,
literal|"bar"
argument_list|)
operator|.
name|boost
argument_list|(
literal|2
argument_list|)
decl_stmt|;
name|QueryBuilder
name|builder
init|=
operator|new
name|WrapperQueryBuilder
argument_list|(
name|query
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
name|QueryShardContext
name|shardContext
init|=
name|createShardContext
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|query
argument_list|,
name|builder
operator|.
name|rewrite
argument_list|(
name|shardContext
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|=
operator|new
name|WrapperQueryBuilder
argument_list|(
name|query
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|boost
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|new
name|BoolQueryBuilder
argument_list|()
operator|.
name|must
argument_list|(
name|query
argument_list|)
operator|.
name|boost
argument_list|(
literal|3
argument_list|)
argument_list|,
name|builder
operator|.
name|rewrite
argument_list|(
name|shardContext
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|rewrite
specifier|protected
name|Query
name|rewrite
parameter_list|(
name|Query
name|query
parameter_list|)
throws|throws
name|IOException
block|{
comment|// WrapperQueryBuilder adds some optimization if the wrapper and query builder have boosts / query names that wraps
comment|// the actual QueryBuilder that comes from the binary blob into a BooleanQueryBuilder to give it an outer boost / name
comment|// this causes some queries to be not exactly equal but equivalent such that we need to rewrite them before comparing.
if|if
condition|(
name|query
operator|!=
literal|null
condition|)
block|{
name|MemoryIndex
name|idx
init|=
operator|new
name|MemoryIndex
argument_list|()
decl_stmt|;
return|return
name|idx
operator|.
name|createSearcher
argument_list|()
operator|.
name|rewrite
argument_list|(
name|query
argument_list|)
return|;
block|}
return|return
operator|new
name|MatchAllDocsQuery
argument_list|()
return|;
comment|// null == *:*
block|}
block|}
end_class

end_unit

