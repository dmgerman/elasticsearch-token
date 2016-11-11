begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.script.mustache
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|mustache
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
name|xcontent
operator|.
name|XContentBuilder
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
name|XContentType
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
name|query
operator|.
name|BoolQueryBuilder
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
name|query
operator|.
name|MatchAllQueryBuilder
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
name|query
operator|.
name|MatchQueryBuilder
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
name|query
operator|.
name|QueryBuilder
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
name|query
operator|.
name|QueryShardContext
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
name|query
operator|.
name|TermQueryBuilder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|plugins
operator|.
name|Plugin
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|MockScriptPlugin
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|Script
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|ScriptType
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
name|org
operator|.
name|junit
operator|.
name|After
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
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
name|Arrays
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
import|import
name|java
operator|.
name|util
operator|.
name|Collections
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
import|import
name|java
operator|.
name|util
operator|.
name|function
operator|.
name|Function
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
name|containsString
import|;
end_import

begin_class
DECL|class|TemplateQueryBuilderTests
specifier|public
class|class
name|TemplateQueryBuilderTests
extends|extends
name|AbstractQueryTestCase
argument_list|<
name|TemplateQueryBuilder
argument_list|>
block|{
comment|/**      * The query type all template tests will be based on.      */
DECL|field|templateBase
specifier|private
name|QueryBuilder
name|templateBase
decl_stmt|;
comment|/**      * All tests create deprecation warnings when an new {@link TemplateQueryBuilder} is created.      * Instead of having to check them once in every single test, this is done here after each test is run      */
DECL|method|checkWarningHeaders
annotation|@
name|After
name|void
name|checkWarningHeaders
parameter_list|()
throws|throws
name|IOException
block|{
name|checkWarningHeaders
argument_list|(
literal|"[template] query is deprecated, use search template api instead"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getPlugins
specifier|protected
name|Collection
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|Plugin
argument_list|>
argument_list|>
name|getPlugins
parameter_list|()
block|{
return|return
name|Arrays
operator|.
name|asList
argument_list|(
name|MustachePlugin
operator|.
name|class
argument_list|,
name|CustomScriptPlugin
operator|.
name|class
argument_list|)
return|;
block|}
DECL|class|CustomScriptPlugin
specifier|public
specifier|static
class|class
name|CustomScriptPlugin
extends|extends
name|MockScriptPlugin
block|{
annotation|@
name|Override
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|method|pluginScripts
specifier|protected
name|Map
argument_list|<
name|String
argument_list|,
name|Function
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|,
name|Object
argument_list|>
argument_list|>
name|pluginScripts
parameter_list|()
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Function
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|,
name|Object
argument_list|>
argument_list|>
name|scripts
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|scripts
operator|.
name|put
argument_list|(
literal|"{ \"match_all\" : {}}"
argument_list|,
name|s
lambda|->
operator|new
name|BytesArray
argument_list|(
literal|"{ \"match_all\" : {}}"
argument_list|)
argument_list|)
expr_stmt|;
name|scripts
operator|.
name|put
argument_list|(
literal|"{ \"match_all\" : {\"_name\" : \"foobar\"}}"
argument_list|,
name|s
lambda|->
operator|new
name|BytesArray
argument_list|(
literal|"{ \"match_all\" : {\"_name\" : \"foobar\"}}"
argument_list|)
argument_list|)
expr_stmt|;
name|scripts
operator|.
name|put
argument_list|(
literal|"{\n"
operator|+
literal|"  \"term\" : {\n"
operator|+
literal|"    \"foo\" : {\n"
operator|+
literal|"      \"value\" : \"bar\",\n"
operator|+
literal|"      \"boost\" : 2.0\n"
operator|+
literal|"    }\n"
operator|+
literal|"  }\n"
operator|+
literal|"}"
argument_list|,
name|s
lambda|->
operator|new
name|BytesArray
argument_list|(
literal|"{\n"
operator|+
literal|"  \"term\" : {\n"
operator|+
literal|"    \"foo\" : {\n"
operator|+
literal|"      \"value\" : \"bar\",\n"
operator|+
literal|"      \"boost\" : 2.0\n"
operator|+
literal|"    }\n"
operator|+
literal|"  }\n"
operator|+
literal|"}"
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|scripts
return|;
block|}
block|}
annotation|@
name|Before
DECL|method|before
specifier|public
name|void
name|before
parameter_list|()
block|{
name|templateBase
operator|=
operator|new
name|MatchQueryBuilder
argument_list|(
literal|"field"
argument_list|,
literal|"some values"
argument_list|)
expr_stmt|;
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
annotation|@
name|Override
DECL|method|doCreateTestQueryBuilder
specifier|protected
name|TemplateQueryBuilder
name|doCreateTestQueryBuilder
parameter_list|()
block|{
return|return
operator|new
name|TemplateQueryBuilder
argument_list|(
operator|new
name|Script
argument_list|(
name|ScriptType
operator|.
name|INLINE
argument_list|,
literal|"mustache"
argument_list|,
name|templateBase
operator|.
name|toString
argument_list|()
argument_list|,
name|Collections
operator|.
name|emptyMap
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
name|TemplateQueryBuilder
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
name|QueryShardContext
name|queryShardContext
init|=
name|context
operator|.
name|getQueryShardContext
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|rewrite
argument_list|(
name|QueryBuilder
operator|.
name|rewriteQuery
argument_list|(
name|templateBase
argument_list|,
name|queryShardContext
argument_list|)
operator|.
name|toQuery
argument_list|(
name|queryShardContext
argument_list|)
argument_list|)
argument_list|,
name|rewrite
argument_list|(
name|query
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testIllegalArgument
specifier|public
name|void
name|testIllegalArgument
parameter_list|()
block|{
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
operator|new
name|TemplateQueryBuilder
argument_list|(
operator|(
name|Script
operator|)
literal|null
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**      * Override superclass test since template query doesn't support boost and queryName, so      * we need to mutate other existing field in the test query.      */
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
name|TemplateQueryBuilder
name|testQuery
init|=
name|createTestQueryBuilder
argument_list|()
decl_stmt|;
name|String
name|testQueryAsString
init|=
name|toXContent
argument_list|(
name|testQuery
argument_list|,
name|randomFrom
argument_list|(
name|XContentType
operator|.
name|JSON
argument_list|,
name|XContentType
operator|.
name|YAML
argument_list|)
argument_list|)
operator|.
name|string
argument_list|()
decl_stmt|;
name|String
name|queryAsString
init|=
name|testQueryAsString
operator|.
name|replace
argument_list|(
literal|"inline"
argument_list|,
literal|"bogusField"
argument_list|)
decl_stmt|;
try|try
block|{
name|parseQuery
argument_list|(
name|queryAsString
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"IllegalArgumentException expected"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"[script] unknown field [bogusField], parser not found"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testJSONGeneration
specifier|public
name|void
name|testJSONGeneration
parameter_list|()
throws|throws
name|IOException
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|vars
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|vars
operator|.
name|put
argument_list|(
literal|"template"
argument_list|,
literal|"filled"
argument_list|)
expr_stmt|;
name|TemplateQueryBuilder
name|builder
init|=
operator|new
name|TemplateQueryBuilder
argument_list|(
literal|"I am a $template string"
argument_list|,
name|ScriptType
operator|.
name|INLINE
argument_list|,
name|vars
argument_list|)
decl_stmt|;
name|XContentBuilder
name|content
init|=
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
decl_stmt|;
name|content
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|doXContent
argument_list|(
name|content
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|content
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|content
operator|.
name|close
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|"{\"template\":{\"inline\":\"I am a $template string\",\"lang\":\"mustache\",\"params\":{\"template\":\"filled\"}}}"
argument_list|,
name|content
operator|.
name|string
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testRawEscapedTemplate
specifier|public
name|void
name|testRawEscapedTemplate
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|expectedTemplateString
init|=
literal|"{\"match_{{template}}\": {}}\""
decl_stmt|;
name|String
name|query
init|=
literal|"{\"template\": {\"inline\": \"{\\\"match_{{template}}\\\": {}}\\\"\",\"params\" : {\"template\" : \"all\"}}}"
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|params
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|params
operator|.
name|put
argument_list|(
literal|"template"
argument_list|,
literal|"all"
argument_list|)
expr_stmt|;
name|QueryBuilder
name|expectedBuilder
init|=
operator|new
name|TemplateQueryBuilder
argument_list|(
name|expectedTemplateString
argument_list|,
name|ScriptType
operator|.
name|INLINE
argument_list|,
name|params
argument_list|)
decl_stmt|;
name|assertParsedQuery
argument_list|(
name|query
argument_list|,
name|expectedBuilder
argument_list|)
expr_stmt|;
block|}
DECL|method|testRawTemplate
specifier|public
name|void
name|testRawTemplate
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|expectedTemplateString
init|=
literal|"{\"match_{{template}}\":{}}"
decl_stmt|;
name|String
name|query
init|=
literal|"{\"template\": {\"inline\": {\"match_{{template}}\": {}},\"params\" : {\"template\" : \"all\"}}}"
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|params
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|params
operator|.
name|put
argument_list|(
literal|"template"
argument_list|,
literal|"all"
argument_list|)
expr_stmt|;
name|QueryBuilder
name|expectedBuilder
init|=
operator|new
name|TemplateQueryBuilder
argument_list|(
name|expectedTemplateString
argument_list|,
name|ScriptType
operator|.
name|INLINE
argument_list|,
name|params
argument_list|,
name|XContentType
operator|.
name|JSON
argument_list|)
decl_stmt|;
name|assertParsedQuery
argument_list|(
name|query
argument_list|,
name|expectedBuilder
argument_list|)
expr_stmt|;
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
name|String
name|query
init|=
literal|"{ \"match_all\" : {}}"
decl_stmt|;
name|QueryBuilder
name|builder
init|=
operator|new
name|TemplateQueryBuilder
argument_list|(
operator|new
name|Script
argument_list|(
name|ScriptType
operator|.
name|INLINE
argument_list|,
literal|"mockscript"
argument_list|,
name|query
argument_list|,
name|Collections
operator|.
name|singletonMap
argument_list|(
name|Script
operator|.
name|CONTENT_TYPE_OPTION
argument_list|,
name|XContentType
operator|.
name|JSON
operator|.
name|mediaType
argument_list|()
argument_list|)
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
try|try
block|{
name|builder
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
name|ex
parameter_list|)
block|{
name|assertEquals
argument_list|(
literal|"this query must be rewritten first"
argument_list|,
name|ex
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
operator|new
name|MatchAllQueryBuilder
argument_list|()
argument_list|,
name|builder
operator|.
name|rewrite
argument_list|(
name|createShardContext
argument_list|()
argument_list|)
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
specifier|final
name|String
name|query
init|=
literal|"{ \"match_all\" : {\"_name\" : \"foobar\"}}"
decl_stmt|;
name|QueryBuilder
name|builder
init|=
operator|new
name|TemplateQueryBuilder
argument_list|(
operator|new
name|Script
argument_list|(
name|ScriptType
operator|.
name|INLINE
argument_list|,
literal|"mockscript"
argument_list|,
name|query
argument_list|,
name|Collections
operator|.
name|singletonMap
argument_list|(
name|Script
operator|.
name|CONTENT_TYPE_OPTION
argument_list|,
name|XContentType
operator|.
name|JSON
operator|.
name|mediaType
argument_list|()
argument_list|)
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|)
argument_list|)
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
name|createShardContext
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|=
operator|new
name|TemplateQueryBuilder
argument_list|(
operator|new
name|Script
argument_list|(
name|ScriptType
operator|.
name|INLINE
argument_list|,
literal|"mockscript"
argument_list|,
name|query
argument_list|,
name|Collections
operator|.
name|singletonMap
argument_list|(
name|Script
operator|.
name|CONTENT_TYPE_OPTION
argument_list|,
name|XContentType
operator|.
name|JSON
operator|.
name|mediaType
argument_list|()
argument_list|)
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|)
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
name|createShardContext
argument_list|()
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
name|TemplateQueryBuilder
argument_list|(
operator|new
name|Script
argument_list|(
name|ScriptType
operator|.
name|INLINE
argument_list|,
literal|"mockscript"
argument_list|,
name|query
operator|.
name|toString
argument_list|()
argument_list|,
name|Collections
operator|.
name|singletonMap
argument_list|(
name|Script
operator|.
name|CONTENT_TYPE_OPTION
argument_list|,
name|XContentType
operator|.
name|JSON
operator|.
name|mediaType
argument_list|()
argument_list|)
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|query
argument_list|,
name|builder
operator|.
name|rewrite
argument_list|(
name|createShardContext
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|=
operator|new
name|TemplateQueryBuilder
argument_list|(
operator|new
name|Script
argument_list|(
name|ScriptType
operator|.
name|INLINE
argument_list|,
literal|"mockscript"
argument_list|,
name|query
operator|.
name|toString
argument_list|()
argument_list|,
name|Collections
operator|.
name|singletonMap
argument_list|(
name|Script
operator|.
name|CONTENT_TYPE_OPTION
argument_list|,
name|XContentType
operator|.
name|JSON
operator|.
name|mediaType
argument_list|()
argument_list|)
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|)
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
name|createShardContext
argument_list|()
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
comment|// TemplateQueryBuilder adds some optimization if the template and query builder have boosts / query names that wraps
comment|// the actual QueryBuilder that comes from the template into a BooleanQueryBuilder to give it an outer boost / name
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

