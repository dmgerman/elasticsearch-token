begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.aliases
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|aliases
package|;
end_package

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
name|compress
operator|.
name|CompressedString
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
name|ToXContent
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
name|FilterBuilder
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
name|IndexQueryParserModule
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
name|InvalidAliasNameException
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
name|DummyCircuitBreakerService
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
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|FilterBuilders
operator|.
name|termFilter
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|MatcherAssert
operator|.
name|assertThat
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
name|nullValue
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|IndexAliasesServiceTests
specifier|public
class|class
name|IndexAliasesServiceTests
extends|extends
name|ElasticsearchTestCase
block|{
DECL|method|newIndexAliasesService
specifier|public
specifier|static
name|IndexAliasesService
name|newIndexAliasesService
parameter_list|()
block|{
return|return
operator|new
name|IndexAliasesService
argument_list|(
operator|new
name|Index
argument_list|(
literal|"test"
argument_list|)
argument_list|,
name|ImmutableSettings
operator|.
name|Builder
operator|.
name|EMPTY_SETTINGS
argument_list|,
name|newIndexQueryParserService
argument_list|()
argument_list|)
return|;
block|}
DECL|method|newIndexQueryParserService
specifier|public
specifier|static
name|IndexQueryParserService
name|newIndexQueryParserService
parameter_list|()
block|{
name|Injector
name|injector
init|=
operator|new
name|ModulesBuilder
argument_list|()
operator|.
name|add
argument_list|(
operator|new
name|IndicesQueriesModule
argument_list|()
argument_list|,
operator|new
name|CacheRecyclerModule
argument_list|(
name|ImmutableSettings
operator|.
name|Builder
operator|.
name|EMPTY_SETTINGS
argument_list|)
argument_list|,
operator|new
name|CodecModule
argument_list|(
name|ImmutableSettings
operator|.
name|Builder
operator|.
name|EMPTY_SETTINGS
argument_list|)
argument_list|,
operator|new
name|IndexSettingsModule
argument_list|(
operator|new
name|Index
argument_list|(
literal|"test"
argument_list|)
argument_list|,
name|ImmutableSettings
operator|.
name|Builder
operator|.
name|EMPTY_SETTINGS
argument_list|)
argument_list|,
operator|new
name|IndexNameModule
argument_list|(
operator|new
name|Index
argument_list|(
literal|"test"
argument_list|)
argument_list|)
argument_list|,
operator|new
name|IndexQueryParserModule
argument_list|(
name|ImmutableSettings
operator|.
name|Builder
operator|.
name|EMPTY_SETTINGS
argument_list|)
argument_list|,
operator|new
name|AnalysisModule
argument_list|(
name|ImmutableSettings
operator|.
name|Builder
operator|.
name|EMPTY_SETTINGS
argument_list|)
argument_list|,
operator|new
name|SimilarityModule
argument_list|(
name|ImmutableSettings
operator|.
name|Builder
operator|.
name|EMPTY_SETTINGS
argument_list|)
argument_list|,
operator|new
name|ScriptModule
argument_list|(
name|ImmutableSettings
operator|.
name|Builder
operator|.
name|EMPTY_SETTINGS
argument_list|)
argument_list|,
operator|new
name|SettingsModule
argument_list|(
name|ImmutableSettings
operator|.
name|Builder
operator|.
name|EMPTY_SETTINGS
argument_list|)
argument_list|,
operator|new
name|IndexEngineModule
argument_list|(
name|ImmutableSettings
operator|.
name|Builder
operator|.
name|EMPTY_SETTINGS
argument_list|)
argument_list|,
operator|new
name|IndexCacheModule
argument_list|(
name|ImmutableSettings
operator|.
name|Builder
operator|.
name|EMPTY_SETTINGS
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
name|DummyCircuitBreakerService
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
decl_stmt|;
return|return
name|injector
operator|.
name|getInstance
argument_list|(
name|IndexQueryParserService
operator|.
name|class
argument_list|)
return|;
block|}
DECL|method|filter
specifier|public
specifier|static
name|CompressedString
name|filter
parameter_list|(
name|FilterBuilder
name|filterBuilder
parameter_list|)
throws|throws
name|IOException
block|{
name|XContentBuilder
name|builder
init|=
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
decl_stmt|;
name|filterBuilder
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|ToXContent
operator|.
name|EMPTY_PARAMS
argument_list|)
expr_stmt|;
name|builder
operator|.
name|close
argument_list|()
expr_stmt|;
return|return
operator|new
name|CompressedString
argument_list|(
name|builder
operator|.
name|string
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Test
DECL|method|testFilteringAliases
specifier|public
name|void
name|testFilteringAliases
parameter_list|()
throws|throws
name|Exception
block|{
name|IndexAliasesService
name|indexAliasesService
init|=
name|newIndexAliasesService
argument_list|()
decl_stmt|;
name|indexAliasesService
operator|.
name|add
argument_list|(
literal|"cats"
argument_list|,
name|filter
argument_list|(
name|termFilter
argument_list|(
literal|"animal"
argument_list|,
literal|"cat"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|indexAliasesService
operator|.
name|add
argument_list|(
literal|"dogs"
argument_list|,
name|filter
argument_list|(
name|termFilter
argument_list|(
literal|"animal"
argument_list|,
literal|"dog"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|indexAliasesService
operator|.
name|add
argument_list|(
literal|"all"
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|indexAliasesService
operator|.
name|hasAlias
argument_list|(
literal|"cats"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|indexAliasesService
operator|.
name|hasAlias
argument_list|(
literal|"dogs"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|indexAliasesService
operator|.
name|hasAlias
argument_list|(
literal|"turtles"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|indexAliasesService
operator|.
name|aliasFilter
argument_list|(
literal|"cats"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"cache(animal:cat)"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|indexAliasesService
operator|.
name|aliasFilter
argument_list|(
literal|"cats"
argument_list|,
literal|"dogs"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"BooleanFilter(cache(animal:cat) cache(animal:dog))"
argument_list|)
argument_list|)
expr_stmt|;
comment|// Non-filtering alias should turn off all filters because filters are ORed
name|assertThat
argument_list|(
name|indexAliasesService
operator|.
name|aliasFilter
argument_list|(
literal|"all"
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|indexAliasesService
operator|.
name|aliasFilter
argument_list|(
literal|"cats"
argument_list|,
literal|"all"
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|indexAliasesService
operator|.
name|aliasFilter
argument_list|(
literal|"all"
argument_list|,
literal|"cats"
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|indexAliasesService
operator|.
name|add
argument_list|(
literal|"cats"
argument_list|,
name|filter
argument_list|(
name|termFilter
argument_list|(
literal|"animal"
argument_list|,
literal|"feline"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|indexAliasesService
operator|.
name|add
argument_list|(
literal|"dogs"
argument_list|,
name|filter
argument_list|(
name|termFilter
argument_list|(
literal|"animal"
argument_list|,
literal|"canine"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|indexAliasesService
operator|.
name|aliasFilter
argument_list|(
literal|"dogs"
argument_list|,
literal|"cats"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"BooleanFilter(cache(animal:canine) cache(animal:feline))"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testAliasFilters
specifier|public
name|void
name|testAliasFilters
parameter_list|()
throws|throws
name|Exception
block|{
name|IndexAliasesService
name|indexAliasesService
init|=
name|newIndexAliasesService
argument_list|()
decl_stmt|;
name|indexAliasesService
operator|.
name|add
argument_list|(
literal|"cats"
argument_list|,
name|filter
argument_list|(
name|termFilter
argument_list|(
literal|"animal"
argument_list|,
literal|"cat"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|indexAliasesService
operator|.
name|add
argument_list|(
literal|"dogs"
argument_list|,
name|filter
argument_list|(
name|termFilter
argument_list|(
literal|"animal"
argument_list|,
literal|"dog"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|indexAliasesService
operator|.
name|aliasFilter
argument_list|()
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|indexAliasesService
operator|.
name|aliasFilter
argument_list|(
literal|"dogs"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"cache(animal:dog)"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|indexAliasesService
operator|.
name|aliasFilter
argument_list|(
literal|"dogs"
argument_list|,
literal|"cats"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"BooleanFilter(cache(animal:dog) cache(animal:cat))"
argument_list|)
argument_list|)
expr_stmt|;
name|indexAliasesService
operator|.
name|add
argument_list|(
literal|"cats"
argument_list|,
name|filter
argument_list|(
name|termFilter
argument_list|(
literal|"animal"
argument_list|,
literal|"feline"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|indexAliasesService
operator|.
name|add
argument_list|(
literal|"dogs"
argument_list|,
name|filter
argument_list|(
name|termFilter
argument_list|(
literal|"animal"
argument_list|,
literal|"canine"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|indexAliasesService
operator|.
name|aliasFilter
argument_list|(
literal|"dogs"
argument_list|,
literal|"cats"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"BooleanFilter(cache(animal:canine) cache(animal:feline))"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|InvalidAliasNameException
operator|.
name|class
argument_list|)
DECL|method|testRemovedAliasFilter
specifier|public
name|void
name|testRemovedAliasFilter
parameter_list|()
throws|throws
name|Exception
block|{
name|IndexAliasesService
name|indexAliasesService
init|=
name|newIndexAliasesService
argument_list|()
decl_stmt|;
name|indexAliasesService
operator|.
name|add
argument_list|(
literal|"cats"
argument_list|,
name|filter
argument_list|(
name|termFilter
argument_list|(
literal|"animal"
argument_list|,
literal|"cat"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|indexAliasesService
operator|.
name|remove
argument_list|(
literal|"cats"
argument_list|)
expr_stmt|;
name|indexAliasesService
operator|.
name|aliasFilter
argument_list|(
literal|"cats"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testUnknownAliasFilter
specifier|public
name|void
name|testUnknownAliasFilter
parameter_list|()
throws|throws
name|Exception
block|{
name|IndexAliasesService
name|indexAliasesService
init|=
name|newIndexAliasesService
argument_list|()
decl_stmt|;
name|indexAliasesService
operator|.
name|add
argument_list|(
literal|"cats"
argument_list|,
name|filter
argument_list|(
name|termFilter
argument_list|(
literal|"animal"
argument_list|,
literal|"cat"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|indexAliasesService
operator|.
name|add
argument_list|(
literal|"dogs"
argument_list|,
name|filter
argument_list|(
name|termFilter
argument_list|(
literal|"animal"
argument_list|,
literal|"dog"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
try|try
block|{
name|indexAliasesService
operator|.
name|aliasFilter
argument_list|(
literal|"unknown"
argument_list|)
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InvalidAliasNameException
name|e
parameter_list|)
block|{
comment|// all is well
block|}
block|}
block|}
end_class

end_unit

