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
name|IndexReader
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
name|util
operator|.
name|SetOnce
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|client
operator|.
name|Client
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
name|ParseFieldMatcher
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
name|ParseFieldMatcherSupplier
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
name|ClusterState
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
name|index
operator|.
name|mapper
operator|.
name|MapperService
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
name|ExecutableScript
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
name|ScriptContext
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
name|ScriptService
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
name|java
operator|.
name|util
operator|.
name|Collections
import|;
end_import

begin_comment
comment|/**  * Context object used to rewrite {@link QueryBuilder} instances into simplified version.  */
end_comment

begin_class
DECL|class|QueryRewriteContext
specifier|public
class|class
name|QueryRewriteContext
implements|implements
name|ParseFieldMatcherSupplier
block|{
DECL|field|mapperService
specifier|protected
specifier|final
name|MapperService
name|mapperService
decl_stmt|;
DECL|field|scriptService
specifier|protected
specifier|final
name|ScriptService
name|scriptService
decl_stmt|;
DECL|field|indexSettings
specifier|protected
specifier|final
name|IndexSettings
name|indexSettings
decl_stmt|;
DECL|field|indicesQueriesRegistry
specifier|protected
specifier|final
name|IndicesQueriesRegistry
name|indicesQueriesRegistry
decl_stmt|;
DECL|field|client
specifier|protected
specifier|final
name|Client
name|client
decl_stmt|;
DECL|field|reader
specifier|protected
specifier|final
name|IndexReader
name|reader
decl_stmt|;
DECL|field|clusterState
specifier|protected
specifier|final
name|ClusterState
name|clusterState
decl_stmt|;
DECL|field|cachable
specifier|protected
name|boolean
name|cachable
init|=
literal|true
decl_stmt|;
DECL|field|executionMode
specifier|private
specifier|final
name|SetOnce
argument_list|<
name|Boolean
argument_list|>
name|executionMode
init|=
operator|new
name|SetOnce
argument_list|<>
argument_list|()
decl_stmt|;
DECL|method|QueryRewriteContext
specifier|public
name|QueryRewriteContext
parameter_list|(
name|IndexSettings
name|indexSettings
parameter_list|,
name|MapperService
name|mapperService
parameter_list|,
name|ScriptService
name|scriptService
parameter_list|,
name|IndicesQueriesRegistry
name|indicesQueriesRegistry
parameter_list|,
name|Client
name|client
parameter_list|,
name|IndexReader
name|reader
parameter_list|,
name|ClusterState
name|clusterState
parameter_list|)
block|{
name|this
operator|.
name|mapperService
operator|=
name|mapperService
expr_stmt|;
name|this
operator|.
name|scriptService
operator|=
name|scriptService
expr_stmt|;
name|this
operator|.
name|indexSettings
operator|=
name|indexSettings
expr_stmt|;
name|this
operator|.
name|indicesQueriesRegistry
operator|=
name|indicesQueriesRegistry
expr_stmt|;
name|this
operator|.
name|client
operator|=
name|client
expr_stmt|;
name|this
operator|.
name|reader
operator|=
name|reader
expr_stmt|;
name|this
operator|.
name|clusterState
operator|=
name|clusterState
expr_stmt|;
block|}
comment|/**      * Returns a clients to fetch resources from local or remove nodes.      */
DECL|method|getClient
specifier|public
specifier|final
name|Client
name|getClient
parameter_list|()
block|{
return|return
name|client
return|;
block|}
comment|/**      * Returns the index settings for this context. This might return null if the      * context has not index scope.      */
DECL|method|getIndexSettings
specifier|public
specifier|final
name|IndexSettings
name|getIndexSettings
parameter_list|()
block|{
return|return
name|indexSettings
return|;
block|}
comment|/**      * Return the MapperService.      */
DECL|method|getMapperService
specifier|public
specifier|final
name|MapperService
name|getMapperService
parameter_list|()
block|{
return|return
name|mapperService
return|;
block|}
comment|/** Return the current {@link IndexReader}, or {@code null} if we are on the coordinating node. */
DECL|method|getIndexReader
specifier|public
name|IndexReader
name|getIndexReader
parameter_list|()
block|{
return|return
name|reader
return|;
block|}
annotation|@
name|Override
DECL|method|getParseFieldMatcher
specifier|public
name|ParseFieldMatcher
name|getParseFieldMatcher
parameter_list|()
block|{
return|return
name|this
operator|.
name|indexSettings
operator|.
name|getParseFieldMatcher
argument_list|()
return|;
block|}
comment|/**      * Returns the cluster state as is when the operation started.      */
DECL|method|getClusterState
specifier|public
name|ClusterState
name|getClusterState
parameter_list|()
block|{
return|return
name|clusterState
return|;
block|}
comment|/**      * Returns a new {@link QueryParseContext} that wraps the provided parser, using the ParseFieldMatcher settings that      * are configured in the index settings. The default script language will always default to Painless.      */
DECL|method|newParseContext
specifier|public
name|QueryParseContext
name|newParseContext
parameter_list|(
name|XContentParser
name|parser
parameter_list|)
block|{
return|return
operator|new
name|QueryParseContext
argument_list|(
name|indicesQueriesRegistry
argument_list|,
name|parser
argument_list|,
name|indexSettings
operator|.
name|getParseFieldMatcher
argument_list|()
argument_list|)
return|;
block|}
comment|/**      * Returns a new {@link QueryParseContext} like {@link #newParseContext(XContentParser)} with the only diffence, that      * the default script language will default to what has been set in the 'script.legacy.default_lang' setting.      */
DECL|method|newParseContextWithLegacyScriptLanguage
specifier|public
name|QueryParseContext
name|newParseContextWithLegacyScriptLanguage
parameter_list|(
name|XContentParser
name|parser
parameter_list|)
block|{
name|String
name|defaultScriptLanguage
init|=
name|ScriptSettings
operator|.
name|getLegacyDefaultLang
argument_list|(
name|indexSettings
operator|.
name|getNodeSettings
argument_list|()
argument_list|)
decl_stmt|;
return|return
operator|new
name|QueryParseContext
argument_list|(
name|defaultScriptLanguage
argument_list|,
name|indicesQueriesRegistry
argument_list|,
name|parser
argument_list|,
name|indexSettings
operator|.
name|getParseFieldMatcher
argument_list|()
argument_list|)
return|;
block|}
comment|/**      * Returns<code>true</code> iff the result of the processed search request is cachable. Otherwise<code>false</code>      */
DECL|method|isCachable
specifier|public
name|boolean
name|isCachable
parameter_list|()
block|{
return|return
name|cachable
return|;
block|}
DECL|method|getTemplateBytes
specifier|public
name|BytesReference
name|getTemplateBytes
parameter_list|(
name|Script
name|template
parameter_list|)
block|{
name|failIfExecutionMode
argument_list|()
expr_stmt|;
name|ExecutableScript
name|executable
init|=
name|scriptService
operator|.
name|executable
argument_list|(
name|template
argument_list|,
name|ScriptContext
operator|.
name|Standard
operator|.
name|SEARCH
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|)
decl_stmt|;
return|return
operator|(
name|BytesReference
operator|)
name|executable
operator|.
name|run
argument_list|()
return|;
block|}
DECL|method|setExecutionMode
specifier|public
name|void
name|setExecutionMode
parameter_list|()
block|{
name|this
operator|.
name|executionMode
operator|.
name|set
argument_list|(
name|Boolean
operator|.
name|TRUE
argument_list|)
expr_stmt|;
block|}
DECL|method|failIfExecutionMode
specifier|protected
name|void
name|failIfExecutionMode
parameter_list|()
block|{
name|this
operator|.
name|cachable
operator|=
literal|false
expr_stmt|;
if|if
condition|(
name|executionMode
operator|.
name|get
argument_list|()
operator|==
name|Boolean
operator|.
name|TRUE
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"features that prevent cachability are disabled on this context"
argument_list|)
throw|;
block|}
else|else
block|{
assert|assert
name|executionMode
operator|.
name|get
argument_list|()
operator|==
literal|null
operator|:
name|executionMode
operator|.
name|get
argument_list|()
assert|;
block|}
block|}
block|}
end_class

end_unit

