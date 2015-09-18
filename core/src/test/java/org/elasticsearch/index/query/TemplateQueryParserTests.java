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
name|Version
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
name|cluster
operator|.
name|metadata
operator|.
name|IndexMetaData
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
name|multibindings
operator|.
name|Multibinder
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
name|query
operator|.
name|functionscore
operator|.
name|ScoreFunctionParser
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
name|IndicesModule
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
name|analysis
operator|.
name|IndicesAnalysisService
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
name|ESTestCase
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
name|ThreadPool
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

begin_comment
comment|// NOTE: this can't be migrated to ESSingleNodeTestCase because of the custom path.conf
end_comment

begin_class
DECL|class|TemplateQueryParserTests
specifier|public
class|class
name|TemplateQueryParserTests
extends|extends
name|ESTestCase
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
name|Settings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"path.home"
argument_list|,
name|createTempDir
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
literal|"path.conf"
argument_list|,
name|this
operator|.
name|getDataPath
argument_list|(
literal|"config"
argument_list|)
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
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_VERSION_CREATED
argument_list|,
name|Version
operator|.
name|CURRENT
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
name|ThreadPoolModule
argument_list|(
operator|new
name|ThreadPool
argument_list|(
name|settings
argument_list|)
argument_list|)
argument_list|,
operator|new
name|IndicesModule
argument_list|(
name|settings
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|void
name|configure
parameter_list|()
block|{
comment|// skip services
name|bindQueryParsersExtension
argument_list|()
expr_stmt|;
block|}
block|}
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
argument_list|,
operator|new
name|IndicesAnalysisService
argument_list|(
name|settings
argument_list|)
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
name|Multibinder
operator|.
name|newSetBinder
argument_list|(
name|binder
argument_list|()
argument_list|,
name|ScoreFunctionParser
operator|.
name|class
argument_list|)
expr_stmt|;
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
name|Override
annotation|@
name|After
DECL|method|tearDown
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|tearDown
argument_list|()
expr_stmt|;
name|terminate
argument_list|(
name|injector
operator|.
name|getInstance
argument_list|(
name|ThreadPool
operator|.
name|class
argument_list|)
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
literal|"{"
operator|+
literal|"\"query\":{\"match_{{template}}\": {}},"
operator|+
literal|"\"params\":{\"template\":\"all\"}"
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
name|templateSourceParser
operator|.
name|nextToken
argument_list|()
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
name|MatchAllDocsQuery
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testParseTemplateAsSingleStringWithConditionalClause
specifier|public
name|void
name|testParseTemplateAsSingleStringWithConditionalClause
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|templateString
init|=
literal|"{"
operator|+
literal|"  \"inline\" : \"{ \\\"match_{{#use_it}}{{template}}{{/use_it}}\\\":{} }\","
operator|+
literal|"  \"params\":{"
operator|+
literal|"    \"template\":\"all\","
operator|+
literal|"    \"use_it\": true"
operator|+
literal|"  }"
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
name|MatchAllDocsQuery
argument_list|)
expr_stmt|;
block|}
comment|/**      * Test that the template query parser can parse and evaluate template      * expressed as a single string but still it expects only the query      * specification (thus this test should fail with specific exception).      */
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|ParsingException
operator|.
name|class
argument_list|)
DECL|method|testParseTemplateFailsToParseCompleteQueryAsSingleString
specifier|public
name|void
name|testParseTemplateFailsToParseCompleteQueryAsSingleString
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|templateString
init|=
literal|"{"
operator|+
literal|"  \"inline\" : \"{ \\\"size\\\": \\\"{{size}}\\\", \\\"query\\\":{\\\"match_all\\\":{}}}\","
operator|+
literal|"  \"params\":{"
operator|+
literal|"    \"size\":2"
operator|+
literal|"  }\n"
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
name|parser
operator|.
name|parse
argument_list|(
name|context
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
literal|"{ \"file\": \"storedTemplate\" ,\"params\":{\"template\":\"all\" } } "
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
name|templateSourceParser
operator|.
name|nextToken
argument_list|()
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
name|MatchAllDocsQuery
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

