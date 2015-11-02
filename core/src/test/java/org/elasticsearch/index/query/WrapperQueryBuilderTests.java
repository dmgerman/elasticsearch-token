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
name|common
operator|.
name|xcontent
operator|.
name|XContentFactory
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
name|xcontent
operator|.
name|XContentParser
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
name|equalTo
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
try|try
init|(
name|XContentParser
name|qSourceParser
init|=
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|queryBuilder
operator|.
name|source
argument_list|()
argument_list|)
operator|.
name|createParser
argument_list|(
name|queryBuilder
operator|.
name|source
argument_list|()
argument_list|)
init|)
block|{
specifier|final
name|QueryShardContext
name|contextCopy
init|=
operator|new
name|QueryShardContext
argument_list|(
name|context
operator|.
name|indexQueryParserService
argument_list|()
argument_list|)
decl_stmt|;
name|contextCopy
operator|.
name|reset
argument_list|(
name|qSourceParser
argument_list|)
expr_stmt|;
name|QueryBuilder
argument_list|<
name|?
argument_list|>
name|innerQuery
init|=
name|contextCopy
operator|.
name|parseContext
argument_list|()
operator|.
name|parseInnerQueryBuilder
argument_list|()
decl_stmt|;
name|Query
name|expected
init|=
name|innerQuery
operator|.
name|toQuery
argument_list|(
name|context
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|query
argument_list|,
name|equalTo
argument_list|(
name|expected
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|assertBoost
specifier|protected
name|void
name|assertBoost
parameter_list|(
name|WrapperQueryBuilder
name|queryBuilder
parameter_list|,
name|Query
name|query
parameter_list|)
throws|throws
name|IOException
block|{
comment|//no-op boost is checked already above as part of doAssertLuceneQuery as we rely on lucene equals impl
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
block|}
end_class

end_unit

