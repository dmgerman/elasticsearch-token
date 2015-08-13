begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.indices.query
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|query
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableMap
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Maps
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
name|io
operator|.
name|stream
operator|.
name|NamedWriteableRegistry
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
name|EmptyQueryBuilder
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
name|QueryParser
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
name|Set
import|;
end_import

begin_class
DECL|class|IndicesQueriesRegistry
specifier|public
class|class
name|IndicesQueriesRegistry
extends|extends
name|AbstractComponent
block|{
DECL|field|queryParsers
specifier|private
name|ImmutableMap
argument_list|<
name|String
argument_list|,
name|QueryParser
argument_list|<
name|?
argument_list|>
argument_list|>
name|queryParsers
decl_stmt|;
annotation|@
name|Inject
DECL|method|IndicesQueriesRegistry
specifier|public
name|IndicesQueriesRegistry
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|Set
argument_list|<
name|QueryParser
argument_list|<
name|?
argument_list|>
argument_list|>
name|injectedQueryParsers
parameter_list|,
name|NamedWriteableRegistry
name|namedWriteableRegistry
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|QueryParser
argument_list|<
name|?
argument_list|>
argument_list|>
name|queryParsers
init|=
name|Maps
operator|.
name|newHashMap
argument_list|()
decl_stmt|;
for|for
control|(
name|QueryParser
argument_list|<
name|?
argument_list|>
name|queryParser
range|:
name|injectedQueryParsers
control|)
block|{
for|for
control|(
name|String
name|name
range|:
name|queryParser
operator|.
name|names
argument_list|()
control|)
block|{
name|queryParsers
operator|.
name|put
argument_list|(
name|name
argument_list|,
name|queryParser
argument_list|)
expr_stmt|;
block|}
name|namedWriteableRegistry
operator|.
name|registerPrototype
argument_list|(
name|QueryBuilder
operator|.
name|class
argument_list|,
name|queryParser
operator|.
name|getBuilderPrototype
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// EmptyQueryBuilder is not registered as query parser but used internally.
comment|// We need to register it with the NamedWriteableRegistry in order to serialize it
name|namedWriteableRegistry
operator|.
name|registerPrototype
argument_list|(
name|QueryBuilder
operator|.
name|class
argument_list|,
name|EmptyQueryBuilder
operator|.
name|PROTOTYPE
argument_list|)
expr_stmt|;
name|this
operator|.
name|queryParsers
operator|=
name|ImmutableMap
operator|.
name|copyOf
argument_list|(
name|queryParsers
argument_list|)
expr_stmt|;
block|}
comment|/**      * Returns all the registered query parsers      */
DECL|method|queryParsers
specifier|public
name|ImmutableMap
argument_list|<
name|String
argument_list|,
name|QueryParser
argument_list|<
name|?
argument_list|>
argument_list|>
name|queryParsers
parameter_list|()
block|{
return|return
name|queryParsers
return|;
block|}
block|}
end_class

end_unit

