begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.cluster.metadata
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|metadata
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|admin
operator|.
name|indices
operator|.
name|alias
operator|.
name|Alias
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
name|Strings
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
name|component
operator|.
name|AbstractComponent
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
name|Inject
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
name|NamedXContentRegistry
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
name|QueryParseContext
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
name|indices
operator|.
name|InvalidAliasNameException
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
name|function
operator|.
name|Function
import|;
end_import

begin_comment
comment|/**  * Validator for an alias, to be used before adding an alias to the index metadata  * and make sure the alias is valid  */
end_comment

begin_class
DECL|class|AliasValidator
specifier|public
class|class
name|AliasValidator
extends|extends
name|AbstractComponent
block|{
annotation|@
name|Inject
DECL|method|AliasValidator
specifier|public
name|AliasValidator
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
block|}
comment|/**      * Allows to validate an {@link org.elasticsearch.action.admin.indices.alias.Alias} and make sure      * it's valid before it gets added to the index metadata. Doesn't validate the alias filter.      * @throws IllegalArgumentException if the alias is not valid      */
DECL|method|validateAlias
specifier|public
name|void
name|validateAlias
parameter_list|(
name|Alias
name|alias
parameter_list|,
name|String
name|index
parameter_list|,
name|MetaData
name|metaData
parameter_list|)
block|{
name|validateAlias
argument_list|(
name|alias
operator|.
name|name
argument_list|()
argument_list|,
name|index
argument_list|,
name|alias
operator|.
name|indexRouting
argument_list|()
argument_list|,
name|name
lambda|->
name|metaData
operator|.
name|index
argument_list|(
name|name
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**      * Allows to validate an {@link org.elasticsearch.cluster.metadata.AliasMetaData} and make sure      * it's valid before it gets added to the index metadata. Doesn't validate the alias filter.      * @throws IllegalArgumentException if the alias is not valid      */
DECL|method|validateAliasMetaData
specifier|public
name|void
name|validateAliasMetaData
parameter_list|(
name|AliasMetaData
name|aliasMetaData
parameter_list|,
name|String
name|index
parameter_list|,
name|MetaData
name|metaData
parameter_list|)
block|{
name|validateAlias
argument_list|(
name|aliasMetaData
operator|.
name|alias
argument_list|()
argument_list|,
name|index
argument_list|,
name|aliasMetaData
operator|.
name|indexRouting
argument_list|()
argument_list|,
name|name
lambda|->
name|metaData
operator|.
name|index
argument_list|(
name|name
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**      * Allows to partially validate an alias, without knowing which index it'll get applied to.      * Useful with index templates containing aliases. Checks also that it is possible to parse      * the alias filter via {@link org.elasticsearch.common.xcontent.XContentParser},      * without validating it as a filter though.      * @throws IllegalArgumentException if the alias is not valid      */
DECL|method|validateAliasStandalone
specifier|public
name|void
name|validateAliasStandalone
parameter_list|(
name|Alias
name|alias
parameter_list|)
block|{
name|validateAliasStandalone
argument_list|(
name|alias
operator|.
name|name
argument_list|()
argument_list|,
name|alias
operator|.
name|indexRouting
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|Strings
operator|.
name|hasLength
argument_list|(
name|alias
operator|.
name|filter
argument_list|()
argument_list|)
condition|)
block|{
try|try
block|{
name|XContentHelper
operator|.
name|convertToMap
argument_list|(
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|alias
operator|.
name|filter
argument_list|()
argument_list|)
argument_list|,
name|alias
operator|.
name|filter
argument_list|()
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"failed to parse filter for alias ["
operator|+
name|alias
operator|.
name|name
argument_list|()
operator|+
literal|"]"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
comment|/**      * Validate a proposed alias.      */
DECL|method|validateAlias
specifier|public
name|void
name|validateAlias
parameter_list|(
name|String
name|alias
parameter_list|,
name|String
name|index
parameter_list|,
annotation|@
name|Nullable
name|String
name|indexRouting
parameter_list|,
name|Function
argument_list|<
name|String
argument_list|,
name|IndexMetaData
argument_list|>
name|indexLookup
parameter_list|)
block|{
name|validateAliasStandalone
argument_list|(
name|alias
argument_list|,
name|indexRouting
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|Strings
operator|.
name|hasText
argument_list|(
name|index
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"index name is required"
argument_list|)
throw|;
block|}
name|IndexMetaData
name|indexNamedSameAsAlias
init|=
name|indexLookup
operator|.
name|apply
argument_list|(
name|alias
argument_list|)
decl_stmt|;
if|if
condition|(
name|indexNamedSameAsAlias
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|InvalidAliasNameException
argument_list|(
name|indexNamedSameAsAlias
operator|.
name|getIndex
argument_list|()
argument_list|,
name|alias
argument_list|,
literal|"an index exists with the same name as the alias"
argument_list|)
throw|;
block|}
block|}
DECL|method|validateAliasStandalone
name|void
name|validateAliasStandalone
parameter_list|(
name|String
name|alias
parameter_list|,
name|String
name|indexRouting
parameter_list|)
block|{
if|if
condition|(
operator|!
name|Strings
operator|.
name|hasText
argument_list|(
name|alias
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"alias name is required"
argument_list|)
throw|;
block|}
name|MetaDataCreateIndexService
operator|.
name|validateIndexOrAliasName
argument_list|(
name|alias
argument_list|,
name|InvalidAliasNameException
operator|::
operator|new
argument_list|)
expr_stmt|;
if|if
condition|(
name|indexRouting
operator|!=
literal|null
operator|&&
name|indexRouting
operator|.
name|indexOf
argument_list|(
literal|','
argument_list|)
operator|!=
operator|-
literal|1
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"alias ["
operator|+
name|alias
operator|+
literal|"] has several index routing values associated with it"
argument_list|)
throw|;
block|}
block|}
comment|/**      * Validates an alias filter by parsing it using the      * provided {@link org.elasticsearch.index.query.QueryShardContext}      * @throws IllegalArgumentException if the filter is not valid      */
DECL|method|validateAliasFilter
specifier|public
name|void
name|validateAliasFilter
parameter_list|(
name|String
name|alias
parameter_list|,
name|String
name|filter
parameter_list|,
name|QueryShardContext
name|queryShardContext
parameter_list|,
name|NamedXContentRegistry
name|xContentRegistry
parameter_list|)
block|{
assert|assert
name|queryShardContext
operator|!=
literal|null
assert|;
try|try
init|(
name|XContentParser
name|parser
init|=
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|filter
argument_list|)
operator|.
name|createParser
argument_list|(
name|xContentRegistry
argument_list|,
name|filter
argument_list|)
init|)
block|{
name|validateAliasFilter
argument_list|(
name|parser
argument_list|,
name|queryShardContext
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"failed to parse filter for alias ["
operator|+
name|alias
operator|+
literal|"]"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**      * Validates an alias filter by parsing it using the      * provided {@link org.elasticsearch.index.query.QueryShardContext}      * @throws IllegalArgumentException if the filter is not valid      */
DECL|method|validateAliasFilter
specifier|public
name|void
name|validateAliasFilter
parameter_list|(
name|String
name|alias
parameter_list|,
name|byte
index|[]
name|filter
parameter_list|,
name|QueryShardContext
name|queryShardContext
parameter_list|,
name|NamedXContentRegistry
name|xContentRegistry
parameter_list|)
block|{
assert|assert
name|queryShardContext
operator|!=
literal|null
assert|;
try|try
init|(
name|XContentParser
name|parser
init|=
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|filter
argument_list|)
operator|.
name|createParser
argument_list|(
name|xContentRegistry
argument_list|,
name|filter
argument_list|)
init|)
block|{
name|validateAliasFilter
argument_list|(
name|parser
argument_list|,
name|queryShardContext
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"failed to parse filter for alias ["
operator|+
name|alias
operator|+
literal|"]"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
DECL|method|validateAliasFilter
specifier|private
specifier|static
name|void
name|validateAliasFilter
parameter_list|(
name|XContentParser
name|parser
parameter_list|,
name|QueryShardContext
name|queryShardContext
parameter_list|)
throws|throws
name|IOException
block|{
name|QueryParseContext
name|queryParseContext
init|=
name|queryShardContext
operator|.
name|newParseContext
argument_list|(
name|parser
argument_list|)
decl_stmt|;
name|QueryBuilder
name|parseInnerQueryBuilder
init|=
name|queryParseContext
operator|.
name|parseInnerQueryBuilder
argument_list|()
decl_stmt|;
name|QueryBuilder
name|queryBuilder
init|=
name|QueryBuilder
operator|.
name|rewriteQuery
argument_list|(
name|parseInnerQueryBuilder
argument_list|,
name|queryShardContext
argument_list|)
decl_stmt|;
name|queryBuilder
operator|.
name|toFilter
argument_list|(
name|queryShardContext
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

