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
name|org
operator|.
name|elasticsearch
operator|.
name|cache
operator|.
name|recycler
operator|.
name|CacheRecyclerModule
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
name|ClusterService
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
name|inject
operator|.
name|AbstractModule
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
name|inject
operator|.
name|Injector
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
name|inject
operator|.
name|ModulesBuilder
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
name|inject
operator|.
name|util
operator|.
name|Providers
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
name|ImmutableSettings
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
name|common
operator|.
name|settings
operator|.
name|SettingsModule
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
name|org
operator|.
name|elasticsearch
operator|.
name|env
operator|.
name|Environment
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|env
operator|.
name|EnvironmentModule
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
name|Index
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
name|IndexNameModule
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
name|analysis
operator|.
name|AnalysisModule
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
name|cache
operator|.
name|IndexCacheModule
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
name|codec
operator|.
name|CodecModule
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
name|engine
operator|.
name|IndexEngineModule
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
name|functionscore
operator|.
name|FunctionScoreModule
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
name|settings
operator|.
name|IndexSettingsModule
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
name|similarity
operator|.
name|SimilarityModule
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
name|fielddata
operator|.
name|breaker
operator|.
name|CircuitBreakerService
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
name|fielddata
operator|.
name|breaker
operator|.
name|NoneCircuitBreakerService
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
name|query
operator|.
name|IndicesQueriesModule
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
name|ScriptModule
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
name|ElasticsearchTestCase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|threadpool
operator|.
name|ThreadPoolModule
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
name|io
operator|.
name|IOException
import|;
end_import

begin_comment
comment|/**  * Test parsing and executing a template request.  */
end_comment

begin_class
DECL|class|TemplateQueryParserTest
specifier|public
class|class
name|TemplateQueryParserTest
extends|extends
name|ElasticsearchTestCase
block|{
DECL|field|injector
specifier|private
name|Injector
name|injector
decl_stmt|;
DECL|field|context
specifier|private
name|QueryParseContext
name|context
decl_stmt|;
annotation|@
name|Before
DECL|method|setup
specifier|public
name|void
name|setup
parameter_list|()
throws|throws
name|IOException
block|{
name|Settings
name|settings
init|=
name|ImmutableSettings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"path.conf"
argument_list|,
name|this
operator|.
name|getResource
argument_list|(
literal|"config"
argument_list|)
operator|.
name|getPath
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
literal|"name"
argument_list|,
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|Index
name|index
init|=
operator|new
name|Index
argument_list|(
literal|"test"
argument_list|)
decl_stmt|;
name|injector
operator|=
operator|new
name|ModulesBuilder
argument_list|()
operator|.
name|add
argument_list|(
operator|new
name|EnvironmentModule
argument_list|(
operator|new
name|Environment
argument_list|(
name|settings
argument_list|)
argument_list|)
argument_list|,
operator|new
name|SettingsModule
argument_list|(
name|settings
argument_list|)
argument_list|,
operator|new
name|CacheRecyclerModule
argument_list|(
name|settings
argument_list|)
argument_list|,
operator|new
name|CodecModule
argument_list|(
name|settings
argument_list|)
argument_list|,
operator|new
name|ThreadPoolModule
argument_list|(
name|settings
argument_list|)
argument_list|,
operator|new
name|IndicesQueriesModule
argument_list|()
argument_list|,
operator|new
name|ScriptModule
argument_list|(
name|settings
argument_list|)
argument_list|,
operator|new
name|IndexSettingsModule
argument_list|(
name|index
argument_list|,
name|settings
argument_list|)
argument_list|,
operator|new
name|IndexCacheModule
argument_list|(
name|settings
argument_list|)
argument_list|,
operator|new
name|AnalysisModule
argument_list|(
name|settings
argument_list|)
argument_list|,
operator|new
name|IndexEngineModule
argument_list|(
name|settings
argument_list|)
argument_list|,
operator|new
name|SimilarityModule
argument_list|(
name|settings
argument_list|)
argument_list|,
operator|new
name|IndexNameModule
argument_list|(
name|index
argument_list|)
argument_list|,
operator|new
name|IndexQueryParserModule
argument_list|(
name|settings
argument_list|)
argument_list|,
operator|new
name|FunctionScoreModule
argument_list|()
argument_list|,
operator|new
name|AbstractModule
argument_list|()
block|{
annotation|@
name|Override
specifier|protected
name|void
name|configure
parameter_list|()
block|{
name|bind
argument_list|(
name|ClusterService
operator|.
name|class
argument_list|)
operator|.
name|toProvider
argument_list|(
name|Providers
operator|.
name|of
argument_list|(
operator|(
name|ClusterService
operator|)
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|bind
argument_list|(
name|CircuitBreakerService
operator|.
name|class
argument_list|)
operator|.
name|to
argument_list|(
name|NoneCircuitBreakerService
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
operator|.
name|createInjector
argument_list|()
expr_stmt|;
name|IndexQueryParserService
name|queryParserService
init|=
name|injector
operator|.
name|getInstance
argument_list|(
name|IndexQueryParserService
operator|.
name|class
argument_list|)
decl_stmt|;
name|context
operator|=
operator|new
name|QueryParseContext
argument_list|(
name|index
argument_list|,
name|queryParserService
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testParser
specifier|public
name|void
name|testParser
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|templateString
init|=
literal|"{\"template\": {"
operator|+
literal|"\"query\":{\"match_{{template}}\": {}},"
operator|+
literal|"\"params\":{\"template\":\"all\"}}"
operator|+
literal|"}"
decl_stmt|;
name|XContentParser
name|templateSourceParser
init|=
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|templateString
argument_list|)
operator|.
name|createParser
argument_list|(
name|templateString
argument_list|)
decl_stmt|;
name|context
operator|.
name|reset
argument_list|(
name|templateSourceParser
argument_list|)
expr_stmt|;
name|TemplateQueryParser
name|parser
init|=
name|injector
operator|.
name|getInstance
argument_list|(
name|TemplateQueryParser
operator|.
name|class
argument_list|)
decl_stmt|;
name|Query
name|query
init|=
name|parser
operator|.
name|parse
argument_list|(
name|context
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"Parsing template query failed."
argument_list|,
name|query
operator|instanceof
name|ConstantScoreQuery
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testParserCanExtractTemplateNames
specifier|public
name|void
name|testParserCanExtractTemplateNames
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|templateString
init|=
literal|"{ \"template\": { \"query\": \"storedTemplate\" ,\"params\":{\"template\":\"all\" } } } "
decl_stmt|;
name|XContentParser
name|templateSourceParser
init|=
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|templateString
argument_list|)
operator|.
name|createParser
argument_list|(
name|templateString
argument_list|)
decl_stmt|;
name|context
operator|.
name|reset
argument_list|(
name|templateSourceParser
argument_list|)
expr_stmt|;
name|TemplateQueryParser
name|parser
init|=
name|injector
operator|.
name|getInstance
argument_list|(
name|TemplateQueryParser
operator|.
name|class
argument_list|)
decl_stmt|;
name|Query
name|query
init|=
name|parser
operator|.
name|parse
argument_list|(
name|context
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"Parsing template query failed."
argument_list|,
name|query
operator|instanceof
name|ConstantScoreQuery
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

