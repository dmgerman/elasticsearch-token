begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
package|;
end_package

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
name|AliasMetaData
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
name|Nullable
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
name|CompressedXContent
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
name|shard
operator|.
name|IndexShard
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
name|test
operator|.
name|ESSingleNodeTestCase
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
name|QueryBuilders
operator|.
name|termQuery
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
comment|/** Unit test(s) for IndexService */
end_comment

begin_class
DECL|class|IndexServiceTests
specifier|public
class|class
name|IndexServiceTests
extends|extends
name|ESSingleNodeTestCase
block|{
DECL|method|testDetermineShadowEngineShouldBeUsed
specifier|public
name|void
name|testDetermineShadowEngineShouldBeUsed
parameter_list|()
block|{
name|Settings
name|regularSettings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_NUMBER_OF_SHARDS
argument_list|,
literal|2
argument_list|)
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_NUMBER_OF_REPLICAS
argument_list|,
literal|1
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|Settings
name|shadowSettings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_NUMBER_OF_SHARDS
argument_list|,
literal|2
argument_list|)
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_NUMBER_OF_REPLICAS
argument_list|,
literal|1
argument_list|)
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_SHADOW_REPLICAS
argument_list|,
literal|true
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|assertFalse
argument_list|(
literal|"no shadow replicas for normal settings"
argument_list|,
name|IndexService
operator|.
name|useShadowEngine
argument_list|(
literal|true
argument_list|,
name|regularSettings
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
literal|"no shadow replicas for normal settings"
argument_list|,
name|IndexService
operator|.
name|useShadowEngine
argument_list|(
literal|false
argument_list|,
name|regularSettings
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
literal|"no shadow replicas for primary shard with shadow settings"
argument_list|,
name|IndexService
operator|.
name|useShadowEngine
argument_list|(
literal|true
argument_list|,
name|shadowSettings
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"shadow replicas for replica shards with shadow settings"
argument_list|,
name|IndexService
operator|.
name|useShadowEngine
argument_list|(
literal|false
argument_list|,
name|shadowSettings
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|newIndexService
specifier|public
name|IndexService
name|newIndexService
parameter_list|()
block|{
name|Settings
name|settings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"name"
argument_list|,
literal|"indexServiceTests"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
return|return
name|createIndex
argument_list|(
literal|"test"
argument_list|,
name|settings
argument_list|)
return|;
block|}
DECL|method|filter
specifier|public
specifier|static
name|CompressedXContent
name|filter
parameter_list|(
name|QueryBuilder
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
name|CompressedXContent
argument_list|(
name|builder
operator|.
name|string
argument_list|()
argument_list|)
return|;
block|}
DECL|method|testFilteringAliases
specifier|public
name|void
name|testFilteringAliases
parameter_list|()
throws|throws
name|Exception
block|{
name|IndexService
name|indexService
init|=
name|newIndexService
argument_list|()
decl_stmt|;
name|IndexShard
name|shard
init|=
name|indexService
operator|.
name|getShard
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|add
argument_list|(
name|indexService
argument_list|,
literal|"cats"
argument_list|,
name|filter
argument_list|(
name|termQuery
argument_list|(
literal|"animal"
argument_list|,
literal|"cat"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|add
argument_list|(
name|indexService
argument_list|,
literal|"dogs"
argument_list|,
name|filter
argument_list|(
name|termQuery
argument_list|(
literal|"animal"
argument_list|,
literal|"dog"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|add
argument_list|(
name|indexService
argument_list|,
literal|"all"
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|indexService
operator|.
name|getMetaData
argument_list|()
operator|.
name|getAliases
argument_list|()
operator|.
name|containsKey
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
name|indexService
operator|.
name|getMetaData
argument_list|()
operator|.
name|getAliases
argument_list|()
operator|.
name|containsKey
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
name|indexService
operator|.
name|getMetaData
argument_list|()
operator|.
name|getAliases
argument_list|()
operator|.
name|containsKey
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
name|indexService
operator|.
name|aliasFilter
argument_list|(
name|shard
operator|.
name|getQueryShardContext
argument_list|()
argument_list|,
literal|"cats"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"animal:cat"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|indexService
operator|.
name|aliasFilter
argument_list|(
name|shard
operator|.
name|getQueryShardContext
argument_list|()
argument_list|,
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
literal|"animal:cat animal:dog"
argument_list|)
argument_list|)
expr_stmt|;
comment|// Non-filtering alias should turn off all filters because filters are ORed
name|assertThat
argument_list|(
name|indexService
operator|.
name|aliasFilter
argument_list|(
name|shard
operator|.
name|getQueryShardContext
argument_list|()
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
name|indexService
operator|.
name|aliasFilter
argument_list|(
name|shard
operator|.
name|getQueryShardContext
argument_list|()
argument_list|,
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
name|indexService
operator|.
name|aliasFilter
argument_list|(
name|shard
operator|.
name|getQueryShardContext
argument_list|()
argument_list|,
literal|"all"
argument_list|,
literal|"cats"
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|add
argument_list|(
name|indexService
argument_list|,
literal|"cats"
argument_list|,
name|filter
argument_list|(
name|termQuery
argument_list|(
literal|"animal"
argument_list|,
literal|"feline"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|add
argument_list|(
name|indexService
argument_list|,
literal|"dogs"
argument_list|,
name|filter
argument_list|(
name|termQuery
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
name|indexService
operator|.
name|aliasFilter
argument_list|(
name|shard
operator|.
name|getQueryShardContext
argument_list|()
argument_list|,
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
literal|"animal:canine animal:feline"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testAliasFilters
specifier|public
name|void
name|testAliasFilters
parameter_list|()
throws|throws
name|Exception
block|{
name|IndexService
name|indexService
init|=
name|newIndexService
argument_list|()
decl_stmt|;
name|IndexShard
name|shard
init|=
name|indexService
operator|.
name|getShard
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|add
argument_list|(
name|indexService
argument_list|,
literal|"cats"
argument_list|,
name|filter
argument_list|(
name|termQuery
argument_list|(
literal|"animal"
argument_list|,
literal|"cat"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|add
argument_list|(
name|indexService
argument_list|,
literal|"dogs"
argument_list|,
name|filter
argument_list|(
name|termQuery
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
name|indexService
operator|.
name|aliasFilter
argument_list|(
name|shard
operator|.
name|getQueryShardContext
argument_list|()
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|indexService
operator|.
name|aliasFilter
argument_list|(
name|shard
operator|.
name|getQueryShardContext
argument_list|()
argument_list|,
literal|"dogs"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"animal:dog"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|indexService
operator|.
name|aliasFilter
argument_list|(
name|shard
operator|.
name|getQueryShardContext
argument_list|()
argument_list|,
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
literal|"animal:dog animal:cat"
argument_list|)
argument_list|)
expr_stmt|;
name|add
argument_list|(
name|indexService
argument_list|,
literal|"cats"
argument_list|,
name|filter
argument_list|(
name|termQuery
argument_list|(
literal|"animal"
argument_list|,
literal|"feline"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|add
argument_list|(
name|indexService
argument_list|,
literal|"dogs"
argument_list|,
name|filter
argument_list|(
name|termQuery
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
name|indexService
operator|.
name|aliasFilter
argument_list|(
name|shard
operator|.
name|getQueryShardContext
argument_list|()
argument_list|,
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
literal|"animal:canine animal:feline"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testRemovedAliasFilter
specifier|public
name|void
name|testRemovedAliasFilter
parameter_list|()
throws|throws
name|Exception
block|{
name|IndexService
name|indexService
init|=
name|newIndexService
argument_list|()
decl_stmt|;
name|IndexShard
name|shard
init|=
name|indexService
operator|.
name|getShard
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|add
argument_list|(
name|indexService
argument_list|,
literal|"cats"
argument_list|,
name|filter
argument_list|(
name|termQuery
argument_list|(
literal|"animal"
argument_list|,
literal|"cat"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|remove
argument_list|(
name|indexService
argument_list|,
literal|"cats"
argument_list|)
expr_stmt|;
try|try
block|{
name|indexService
operator|.
name|aliasFilter
argument_list|(
name|shard
operator|.
name|getQueryShardContext
argument_list|()
argument_list|,
literal|"cats"
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Expected InvalidAliasNameException"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InvalidAliasNameException
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
literal|"Invalid alias name [cats]"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testUnknownAliasFilter
specifier|public
name|void
name|testUnknownAliasFilter
parameter_list|()
throws|throws
name|Exception
block|{
name|IndexService
name|indexService
init|=
name|newIndexService
argument_list|()
decl_stmt|;
name|IndexShard
name|shard
init|=
name|indexService
operator|.
name|getShard
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|add
argument_list|(
name|indexService
argument_list|,
literal|"cats"
argument_list|,
name|filter
argument_list|(
name|termQuery
argument_list|(
literal|"animal"
argument_list|,
literal|"cat"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|add
argument_list|(
name|indexService
argument_list|,
literal|"dogs"
argument_list|,
name|filter
argument_list|(
name|termQuery
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
name|indexService
operator|.
name|aliasFilter
argument_list|(
name|shard
operator|.
name|getQueryShardContext
argument_list|()
argument_list|,
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
DECL|method|remove
specifier|private
name|void
name|remove
parameter_list|(
name|IndexService
name|service
parameter_list|,
name|String
name|alias
parameter_list|)
block|{
name|IndexMetaData
name|build
init|=
name|IndexMetaData
operator|.
name|builder
argument_list|(
name|service
operator|.
name|getMetaData
argument_list|()
argument_list|)
operator|.
name|removeAlias
argument_list|(
name|alias
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|service
operator|.
name|updateMetaData
argument_list|(
name|build
argument_list|)
expr_stmt|;
block|}
DECL|method|add
specifier|private
name|void
name|add
parameter_list|(
name|IndexService
name|service
parameter_list|,
name|String
name|alias
parameter_list|,
annotation|@
name|Nullable
name|CompressedXContent
name|filter
parameter_list|)
block|{
name|IndexMetaData
name|build
init|=
name|IndexMetaData
operator|.
name|builder
argument_list|(
name|service
operator|.
name|getMetaData
argument_list|()
argument_list|)
operator|.
name|putAlias
argument_list|(
name|AliasMetaData
operator|.
name|builder
argument_list|(
name|alias
argument_list|)
operator|.
name|filter
argument_list|(
name|filter
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|service
operator|.
name|updateMetaData
argument_list|(
name|build
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

