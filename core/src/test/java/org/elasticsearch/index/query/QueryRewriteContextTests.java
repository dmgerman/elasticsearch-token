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
name|XContentHelper
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
name|index
operator|.
name|IndexSettings
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
name|IndicesQueriesRegistry
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
name|ScriptSettings
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
name|SearchModule
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
import|import static
name|java
operator|.
name|util
operator|.
name|Collections
operator|.
name|emptyList
import|;
end_import

begin_class
DECL|class|QueryRewriteContextTests
specifier|public
class|class
name|QueryRewriteContextTests
extends|extends
name|ESTestCase
block|{
DECL|method|testNewParseContextWithLegacyScriptLanguage
specifier|public
name|void
name|testNewParseContextWithLegacyScriptLanguage
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|defaultLegacyScriptLanguage
init|=
name|randomAsciiOfLength
argument_list|(
literal|4
argument_list|)
decl_stmt|;
name|IndexMetaData
operator|.
name|Builder
name|indexMetadata
init|=
operator|new
name|IndexMetaData
operator|.
name|Builder
argument_list|(
literal|"index"
argument_list|)
decl_stmt|;
name|indexMetadata
operator|.
name|settings
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"index.version.created"
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.number_of_shards"
argument_list|,
literal|1
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.number_of_replicas"
argument_list|,
literal|1
argument_list|)
argument_list|)
expr_stmt|;
specifier|final
name|long
name|nowInMills
init|=
name|randomPositiveLong
argument_list|()
decl_stmt|;
name|IndicesQueriesRegistry
name|indicesQueriesRegistry
init|=
operator|new
name|SearchModule
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
literal|false
argument_list|,
name|emptyList
argument_list|()
argument_list|)
operator|.
name|getQueryParserRegistry
argument_list|()
decl_stmt|;
name|IndexSettings
name|indexSettings
init|=
operator|new
name|IndexSettings
argument_list|(
name|indexMetadata
operator|.
name|build
argument_list|()
argument_list|,
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|ScriptSettings
operator|.
name|LEGACY_SCRIPT_SETTING
argument_list|,
name|defaultLegacyScriptLanguage
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|QueryRewriteContext
name|queryRewriteContext
init|=
operator|new
name|QueryRewriteContext
argument_list|(
name|indexSettings
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|indicesQueriesRegistry
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
parameter_list|()
lambda|->
name|nowInMills
argument_list|)
decl_stmt|;
comment|// verify that the default script language in the query parse context is equal to defaultLegacyScriptLanguage variable:
name|QueryParseContext
name|queryParseContext
init|=
name|queryRewriteContext
operator|.
name|newParseContextWithLegacyScriptLanguage
argument_list|(
name|XContentHelper
operator|.
name|createParser
argument_list|(
operator|new
name|BytesArray
argument_list|(
literal|"{}"
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|defaultLegacyScriptLanguage
argument_list|,
name|queryParseContext
operator|.
name|getDefaultScriptLanguage
argument_list|()
argument_list|)
expr_stmt|;
comment|// verify that the script query's script language is equal to defaultLegacyScriptLanguage variable:
name|XContentParser
name|parser
init|=
name|XContentHelper
operator|.
name|createParser
argument_list|(
operator|new
name|BytesArray
argument_list|(
literal|"{\"script\" : {\"script\": \"return true\"}}"
argument_list|)
argument_list|)
decl_stmt|;
name|queryParseContext
operator|=
name|queryRewriteContext
operator|.
name|newParseContextWithLegacyScriptLanguage
argument_list|(
name|parser
argument_list|)
expr_stmt|;
name|ScriptQueryBuilder
name|queryBuilder
init|=
operator|(
name|ScriptQueryBuilder
operator|)
name|queryParseContext
operator|.
name|parseInnerQueryBuilder
argument_list|()
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|defaultLegacyScriptLanguage
argument_list|,
name|queryBuilder
operator|.
name|script
argument_list|()
operator|.
name|getLang
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

