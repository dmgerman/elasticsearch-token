begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.query.plugin
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|plugin
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
name|common
operator|.
name|settings
operator|.
name|Settings
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
name|IndexQueryParserService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|IndicesService
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
name|test
operator|.
name|ESIntegTestCase
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
name|util
operator|.
name|Collection
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|QueryBuilders
operator|.
name|boolQuery
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|QueryBuilders
operator|.
name|constantScoreQuery
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|hamcrest
operator|.
name|ElasticsearchAssertions
operator|.
name|assertHitCount
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
DECL|class|CustomQueryParserIT
specifier|public
class|class
name|CustomQueryParserIT
extends|extends
name|ESIntegTestCase
block|{
annotation|@
name|Override
DECL|method|nodePlugins
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
name|nodePlugins
parameter_list|()
block|{
return|return
name|pluginList
argument_list|(
name|DummyQueryParserPlugin
operator|.
name|class
argument_list|)
return|;
block|}
annotation|@
name|Before
DECL|method|setUp
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|setUp
argument_list|()
expr_stmt|;
name|createIndex
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"index"
argument_list|,
literal|"type"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"field"
argument_list|,
literal|"value"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|refresh
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|numberOfShards
specifier|protected
name|int
name|numberOfShards
parameter_list|()
block|{
return|return
name|cluster
argument_list|()
operator|.
name|numDataNodes
argument_list|()
return|;
block|}
annotation|@
name|Test
DECL|method|testCustomDummyQuery
specifier|public
name|void
name|testCustomDummyQuery
parameter_list|()
block|{
name|assertHitCount
argument_list|(
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"index"
argument_list|)
operator|.
name|setQuery
argument_list|(
operator|new
name|DummyQueryParserPlugin
operator|.
name|DummyQueryBuilder
argument_list|()
argument_list|)
operator|.
name|get
argument_list|()
argument_list|,
literal|1l
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testCustomDummyQueryWithinBooleanQuery
specifier|public
name|void
name|testCustomDummyQueryWithinBooleanQuery
parameter_list|()
block|{
name|assertHitCount
argument_list|(
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"index"
argument_list|)
operator|.
name|setQuery
argument_list|(
operator|new
name|BoolQueryBuilder
argument_list|()
operator|.
name|must
argument_list|(
operator|new
name|DummyQueryParserPlugin
operator|.
name|DummyQueryBuilder
argument_list|()
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
argument_list|,
literal|1l
argument_list|)
expr_stmt|;
block|}
DECL|method|queryParser
specifier|private
specifier|static
name|IndexQueryParserService
name|queryParser
parameter_list|()
block|{
name|IndicesService
name|indicesService
init|=
name|internalCluster
argument_list|()
operator|.
name|getDataNodeInstance
argument_list|(
name|IndicesService
operator|.
name|class
argument_list|)
decl_stmt|;
return|return
name|indicesService
operator|.
name|indexServiceSafe
argument_list|(
literal|"index"
argument_list|)
operator|.
name|queryParserService
argument_list|()
return|;
block|}
annotation|@
name|Test
comment|//see #11120
DECL|method|testConstantScoreParsesFilter
specifier|public
name|void
name|testConstantScoreParsesFilter
parameter_list|()
throws|throws
name|Exception
block|{
name|IndexQueryParserService
name|queryParser
init|=
name|queryParser
argument_list|()
decl_stmt|;
name|Query
name|q
init|=
name|queryParser
operator|.
name|parse
argument_list|(
name|constantScoreQuery
argument_list|(
operator|new
name|DummyQueryParserPlugin
operator|.
name|DummyQueryBuilder
argument_list|()
argument_list|)
argument_list|)
operator|.
name|query
argument_list|()
decl_stmt|;
name|Query
name|inner
init|=
operator|(
operator|(
name|ConstantScoreQuery
operator|)
name|q
operator|)
operator|.
name|getQuery
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|inner
argument_list|,
name|instanceOf
argument_list|(
name|DummyQueryParserPlugin
operator|.
name|DummyQuery
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|true
argument_list|,
operator|(
operator|(
name|DummyQueryParserPlugin
operator|.
name|DummyQuery
operator|)
name|inner
operator|)
operator|.
name|isFilter
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
comment|//see #11120
DECL|method|testBooleanParsesFilter
specifier|public
name|void
name|testBooleanParsesFilter
parameter_list|()
throws|throws
name|Exception
block|{
name|IndexQueryParserService
name|queryParser
init|=
name|queryParser
argument_list|()
decl_stmt|;
comment|// single clause, serialized as inner object
name|Query
name|q
init|=
name|queryParser
operator|.
name|parse
argument_list|(
name|boolQuery
argument_list|()
operator|.
name|should
argument_list|(
operator|new
name|DummyQueryParserPlugin
operator|.
name|DummyQueryBuilder
argument_list|()
argument_list|)
operator|.
name|must
argument_list|(
operator|new
name|DummyQueryParserPlugin
operator|.
name|DummyQueryBuilder
argument_list|()
argument_list|)
operator|.
name|filter
argument_list|(
operator|new
name|DummyQueryParserPlugin
operator|.
name|DummyQueryBuilder
argument_list|()
argument_list|)
operator|.
name|mustNot
argument_list|(
operator|new
name|DummyQueryParserPlugin
operator|.
name|DummyQueryBuilder
argument_list|()
argument_list|)
argument_list|)
operator|.
name|query
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|q
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
name|bq
init|=
operator|(
name|BooleanQuery
operator|)
name|q
decl_stmt|;
name|assertEquals
argument_list|(
literal|4
argument_list|,
name|bq
operator|.
name|clauses
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|BooleanClause
name|clause
range|:
name|bq
operator|.
name|clauses
argument_list|()
control|)
block|{
name|DummyQueryParserPlugin
operator|.
name|DummyQuery
name|dummy
init|=
operator|(
name|DummyQueryParserPlugin
operator|.
name|DummyQuery
operator|)
name|clause
operator|.
name|getQuery
argument_list|()
decl_stmt|;
switch|switch
condition|(
name|clause
operator|.
name|getOccur
argument_list|()
condition|)
block|{
case|case
name|FILTER
case|:
case|case
name|MUST_NOT
case|:
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|dummy
operator|.
name|isFilter
argument_list|)
expr_stmt|;
break|break;
case|case
name|MUST
case|:
case|case
name|SHOULD
case|:
name|assertEquals
argument_list|(
literal|false
argument_list|,
name|dummy
operator|.
name|isFilter
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|AssertionError
argument_list|()
throw|;
block|}
block|}
comment|// multiple clauses, serialized as inner arrays
name|q
operator|=
name|queryParser
operator|.
name|parse
argument_list|(
name|boolQuery
argument_list|()
operator|.
name|should
argument_list|(
operator|new
name|DummyQueryParserPlugin
operator|.
name|DummyQueryBuilder
argument_list|()
argument_list|)
operator|.
name|should
argument_list|(
operator|new
name|DummyQueryParserPlugin
operator|.
name|DummyQueryBuilder
argument_list|()
argument_list|)
operator|.
name|must
argument_list|(
operator|new
name|DummyQueryParserPlugin
operator|.
name|DummyQueryBuilder
argument_list|()
argument_list|)
operator|.
name|must
argument_list|(
operator|new
name|DummyQueryParserPlugin
operator|.
name|DummyQueryBuilder
argument_list|()
argument_list|)
operator|.
name|filter
argument_list|(
operator|new
name|DummyQueryParserPlugin
operator|.
name|DummyQueryBuilder
argument_list|()
argument_list|)
operator|.
name|filter
argument_list|(
operator|new
name|DummyQueryParserPlugin
operator|.
name|DummyQueryBuilder
argument_list|()
argument_list|)
operator|.
name|mustNot
argument_list|(
operator|new
name|DummyQueryParserPlugin
operator|.
name|DummyQueryBuilder
argument_list|()
argument_list|)
operator|.
name|mustNot
argument_list|(
operator|new
name|DummyQueryParserPlugin
operator|.
name|DummyQueryBuilder
argument_list|()
argument_list|)
argument_list|)
operator|.
name|query
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|q
argument_list|,
name|instanceOf
argument_list|(
name|BooleanQuery
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|bq
operator|=
operator|(
name|BooleanQuery
operator|)
name|q
expr_stmt|;
name|assertEquals
argument_list|(
literal|8
argument_list|,
name|bq
operator|.
name|clauses
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|BooleanClause
name|clause
range|:
name|bq
operator|.
name|clauses
argument_list|()
control|)
block|{
name|DummyQueryParserPlugin
operator|.
name|DummyQuery
name|dummy
init|=
operator|(
name|DummyQueryParserPlugin
operator|.
name|DummyQuery
operator|)
name|clause
operator|.
name|getQuery
argument_list|()
decl_stmt|;
switch|switch
condition|(
name|clause
operator|.
name|getOccur
argument_list|()
condition|)
block|{
case|case
name|FILTER
case|:
case|case
name|MUST_NOT
case|:
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|dummy
operator|.
name|isFilter
argument_list|)
expr_stmt|;
break|break;
case|case
name|MUST
case|:
case|case
name|SHOULD
case|:
name|assertEquals
argument_list|(
literal|false
argument_list|,
name|dummy
operator|.
name|isFilter
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|AssertionError
argument_list|()
throw|;
block|}
block|}
block|}
block|}
end_class

end_unit

