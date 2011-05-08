begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|common
operator|.
name|collect
operator|.
name|Lists
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
name|Scopes
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
name|assistedinject
operator|.
name|FactoryProvider
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
name|MapBinder
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
name|xcontent
operator|.
name|*
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|LinkedList
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

begin_comment
comment|/**  * @author kimchy (Shay Banon)  */
end_comment

begin_class
DECL|class|IndexQueryParserModule
specifier|public
class|class
name|IndexQueryParserModule
extends|extends
name|AbstractModule
block|{
comment|/**      * A custom processor that can be extended to process and bind custom implementations of      * {@link IndexQueryParserFactory}, {@link XContentQueryParserFactory}, and {@link XContentFilterParser}.      */
DECL|class|QueryParsersProcessor
specifier|public
specifier|static
class|class
name|QueryParsersProcessor
block|{
comment|/**          * Extension point to bind a custom {@link IndexQueryParserFactory}.          */
DECL|method|processIndexQueryParsers
specifier|public
name|void
name|processIndexQueryParsers
parameter_list|(
name|IndexQueryParsersBindings
name|bindings
parameter_list|)
block|{          }
DECL|class|IndexQueryParsersBindings
specifier|public
specifier|static
class|class
name|IndexQueryParsersBindings
block|{
DECL|field|binder
specifier|private
specifier|final
name|MapBinder
argument_list|<
name|String
argument_list|,
name|IndexQueryParserFactory
argument_list|>
name|binder
decl_stmt|;
DECL|field|groupSettings
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Settings
argument_list|>
name|groupSettings
decl_stmt|;
DECL|method|IndexQueryParsersBindings
specifier|public
name|IndexQueryParsersBindings
parameter_list|(
name|MapBinder
argument_list|<
name|String
argument_list|,
name|IndexQueryParserFactory
argument_list|>
name|binder
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Settings
argument_list|>
name|groupSettings
parameter_list|)
block|{
name|this
operator|.
name|binder
operator|=
name|binder
expr_stmt|;
name|this
operator|.
name|groupSettings
operator|=
name|groupSettings
expr_stmt|;
block|}
DECL|method|binder
specifier|public
name|MapBinder
argument_list|<
name|String
argument_list|,
name|IndexQueryParserFactory
argument_list|>
name|binder
parameter_list|()
block|{
return|return
name|binder
return|;
block|}
DECL|method|groupSettings
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|Settings
argument_list|>
name|groupSettings
parameter_list|()
block|{
return|return
name|groupSettings
return|;
block|}
DECL|method|processIndexQueryParser
specifier|public
name|void
name|processIndexQueryParser
parameter_list|(
name|String
name|name
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|IndexQueryParser
argument_list|>
name|indexQueryParser
parameter_list|)
block|{
if|if
condition|(
operator|!
name|groupSettings
operator|.
name|containsKey
argument_list|(
name|name
argument_list|)
condition|)
block|{
name|binder
operator|.
name|addBinding
argument_list|(
name|name
argument_list|)
operator|.
name|toProvider
argument_list|(
name|FactoryProvider
operator|.
name|newFactory
argument_list|(
name|IndexQueryParserFactory
operator|.
name|class
argument_list|,
name|indexQueryParser
argument_list|)
argument_list|)
operator|.
name|in
argument_list|(
name|Scopes
operator|.
name|SINGLETON
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**          * Extension point to bind a custom {@link XContentQueryParserFactory}.          */
DECL|method|processXContentQueryParsers
specifier|public
name|void
name|processXContentQueryParsers
parameter_list|(
name|XContentQueryParsersBindings
name|bindings
parameter_list|)
block|{          }
DECL|class|XContentQueryParsersBindings
specifier|public
specifier|static
class|class
name|XContentQueryParsersBindings
block|{
DECL|field|binder
specifier|private
specifier|final
name|MapBinder
argument_list|<
name|String
argument_list|,
name|XContentQueryParserFactory
argument_list|>
name|binder
decl_stmt|;
DECL|field|groupSettings
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Settings
argument_list|>
name|groupSettings
decl_stmt|;
DECL|method|XContentQueryParsersBindings
specifier|public
name|XContentQueryParsersBindings
parameter_list|(
name|MapBinder
argument_list|<
name|String
argument_list|,
name|XContentQueryParserFactory
argument_list|>
name|binder
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Settings
argument_list|>
name|groupSettings
parameter_list|)
block|{
name|this
operator|.
name|binder
operator|=
name|binder
expr_stmt|;
name|this
operator|.
name|groupSettings
operator|=
name|groupSettings
expr_stmt|;
block|}
DECL|method|binder
specifier|public
name|MapBinder
argument_list|<
name|String
argument_list|,
name|XContentQueryParserFactory
argument_list|>
name|binder
parameter_list|()
block|{
return|return
name|binder
return|;
block|}
DECL|method|groupSettings
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|Settings
argument_list|>
name|groupSettings
parameter_list|()
block|{
return|return
name|groupSettings
return|;
block|}
DECL|method|processXContentQueryParser
specifier|public
name|void
name|processXContentQueryParser
parameter_list|(
name|String
name|name
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|XContentQueryParser
argument_list|>
name|xcontentQueryParser
parameter_list|)
block|{
if|if
condition|(
operator|!
name|groupSettings
operator|.
name|containsKey
argument_list|(
name|name
argument_list|)
condition|)
block|{
name|binder
operator|.
name|addBinding
argument_list|(
name|name
argument_list|)
operator|.
name|toProvider
argument_list|(
name|FactoryProvider
operator|.
name|newFactory
argument_list|(
name|XContentQueryParserFactory
operator|.
name|class
argument_list|,
name|xcontentQueryParser
argument_list|)
argument_list|)
operator|.
name|in
argument_list|(
name|Scopes
operator|.
name|SINGLETON
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**          * Extension point to bind a custom {@link XContentFilterParserFactory}.          */
DECL|method|processXContentFilterParsers
specifier|public
name|void
name|processXContentFilterParsers
parameter_list|(
name|XContentFilterParsersBindings
name|bindings
parameter_list|)
block|{          }
DECL|class|XContentFilterParsersBindings
specifier|public
specifier|static
class|class
name|XContentFilterParsersBindings
block|{
DECL|field|binder
specifier|private
specifier|final
name|MapBinder
argument_list|<
name|String
argument_list|,
name|XContentFilterParserFactory
argument_list|>
name|binder
decl_stmt|;
DECL|field|groupSettings
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Settings
argument_list|>
name|groupSettings
decl_stmt|;
DECL|method|XContentFilterParsersBindings
specifier|public
name|XContentFilterParsersBindings
parameter_list|(
name|MapBinder
argument_list|<
name|String
argument_list|,
name|XContentFilterParserFactory
argument_list|>
name|binder
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Settings
argument_list|>
name|groupSettings
parameter_list|)
block|{
name|this
operator|.
name|binder
operator|=
name|binder
expr_stmt|;
name|this
operator|.
name|groupSettings
operator|=
name|groupSettings
expr_stmt|;
block|}
DECL|method|binder
specifier|public
name|MapBinder
argument_list|<
name|String
argument_list|,
name|XContentFilterParserFactory
argument_list|>
name|binder
parameter_list|()
block|{
return|return
name|binder
return|;
block|}
DECL|method|groupSettings
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|Settings
argument_list|>
name|groupSettings
parameter_list|()
block|{
return|return
name|groupSettings
return|;
block|}
DECL|method|processXContentQueryFilter
specifier|public
name|void
name|processXContentQueryFilter
parameter_list|(
name|String
name|name
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|XContentFilterParser
argument_list|>
name|xcontentFilterParser
parameter_list|)
block|{
if|if
condition|(
operator|!
name|groupSettings
operator|.
name|containsKey
argument_list|(
name|name
argument_list|)
condition|)
block|{
name|binder
operator|.
name|addBinding
argument_list|(
name|name
argument_list|)
operator|.
name|toProvider
argument_list|(
name|FactoryProvider
operator|.
name|newFactory
argument_list|(
name|XContentFilterParserFactory
operator|.
name|class
argument_list|,
name|xcontentFilterParser
argument_list|)
argument_list|)
operator|.
name|in
argument_list|(
name|Scopes
operator|.
name|SINGLETON
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
DECL|field|settings
specifier|private
specifier|final
name|Settings
name|settings
decl_stmt|;
DECL|field|processors
specifier|private
specifier|final
name|LinkedList
argument_list|<
name|QueryParsersProcessor
argument_list|>
name|processors
init|=
name|Lists
operator|.
name|newLinkedList
argument_list|()
decl_stmt|;
DECL|method|IndexQueryParserModule
specifier|public
name|IndexQueryParserModule
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|this
operator|.
name|settings
operator|=
name|settings
expr_stmt|;
name|this
operator|.
name|processors
operator|.
name|add
argument_list|(
operator|new
name|DefaultQueryProcessors
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|addProcessor
specifier|public
name|IndexQueryParserModule
name|addProcessor
parameter_list|(
name|QueryParsersProcessor
name|processor
parameter_list|)
block|{
name|processors
operator|.
name|addFirst
argument_list|(
name|processor
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|configure
annotation|@
name|Override
specifier|protected
name|void
name|configure
parameter_list|()
block|{
comment|// handle IndexQueryParsers
name|MapBinder
argument_list|<
name|String
argument_list|,
name|IndexQueryParserFactory
argument_list|>
name|qbinder
init|=
name|MapBinder
operator|.
name|newMapBinder
argument_list|(
name|binder
argument_list|()
argument_list|,
name|String
operator|.
name|class
argument_list|,
name|IndexQueryParserFactory
operator|.
name|class
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Settings
argument_list|>
name|queryParserGroupSettings
init|=
name|settings
operator|.
name|getGroups
argument_list|(
name|IndexQueryParserService
operator|.
name|Defaults
operator|.
name|PREFIX
argument_list|)
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Settings
argument_list|>
name|entry
range|:
name|queryParserGroupSettings
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|qName
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|Settings
name|qSettings
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|qbinder
operator|.
name|addBinding
argument_list|(
name|qName
argument_list|)
operator|.
name|toProvider
argument_list|(
name|FactoryProvider
operator|.
name|newFactory
argument_list|(
name|IndexQueryParserFactory
operator|.
name|class
argument_list|,
name|qSettings
operator|.
name|getAsClass
argument_list|(
literal|"type"
argument_list|,
name|XContentIndexQueryParser
operator|.
name|class
argument_list|)
argument_list|)
argument_list|)
operator|.
name|in
argument_list|(
name|Scopes
operator|.
name|SINGLETON
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|queryParserGroupSettings
operator|.
name|containsKey
argument_list|(
name|IndexQueryParserService
operator|.
name|Defaults
operator|.
name|DEFAULT
argument_list|)
condition|)
block|{
name|qbinder
operator|.
name|addBinding
argument_list|(
name|IndexQueryParserService
operator|.
name|Defaults
operator|.
name|DEFAULT
argument_list|)
operator|.
name|toProvider
argument_list|(
name|FactoryProvider
operator|.
name|newFactory
argument_list|(
name|IndexQueryParserFactory
operator|.
name|class
argument_list|,
name|XContentIndexQueryParser
operator|.
name|class
argument_list|)
argument_list|)
operator|.
name|in
argument_list|(
name|Scopes
operator|.
name|SINGLETON
argument_list|)
expr_stmt|;
block|}
name|QueryParsersProcessor
operator|.
name|IndexQueryParsersBindings
name|queryParsersBindings
init|=
operator|new
name|QueryParsersProcessor
operator|.
name|IndexQueryParsersBindings
argument_list|(
name|qbinder
argument_list|,
name|queryParserGroupSettings
argument_list|)
decl_stmt|;
for|for
control|(
name|QueryParsersProcessor
name|processor
range|:
name|processors
control|)
block|{
name|processor
operator|.
name|processIndexQueryParsers
argument_list|(
name|queryParsersBindings
argument_list|)
expr_stmt|;
block|}
comment|// handle XContenQueryParsers
name|MapBinder
argument_list|<
name|String
argument_list|,
name|XContentQueryParserFactory
argument_list|>
name|queryBinder
init|=
name|MapBinder
operator|.
name|newMapBinder
argument_list|(
name|binder
argument_list|()
argument_list|,
name|String
operator|.
name|class
argument_list|,
name|XContentQueryParserFactory
operator|.
name|class
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Settings
argument_list|>
name|xContentQueryParserGroups
init|=
name|settings
operator|.
name|getGroups
argument_list|(
name|XContentIndexQueryParser
operator|.
name|Defaults
operator|.
name|QUERY_PREFIX
argument_list|)
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Settings
argument_list|>
name|entry
range|:
name|xContentQueryParserGroups
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|qName
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|Settings
name|qSettings
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|Class
argument_list|<
name|?
extends|extends
name|XContentQueryParser
argument_list|>
name|type
init|=
name|qSettings
operator|.
name|getAsClass
argument_list|(
literal|"type"
argument_list|,
literal|null
argument_list|)
decl_stmt|;
if|if
condition|(
name|type
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Query Parser ["
operator|+
name|qName
operator|+
literal|"] must be provided with a type"
argument_list|)
throw|;
block|}
name|queryBinder
operator|.
name|addBinding
argument_list|(
name|qName
argument_list|)
operator|.
name|toProvider
argument_list|(
name|FactoryProvider
operator|.
name|newFactory
argument_list|(
name|XContentQueryParserFactory
operator|.
name|class
argument_list|,
name|qSettings
operator|.
name|getAsClass
argument_list|(
literal|"type"
argument_list|,
literal|null
argument_list|)
argument_list|)
argument_list|)
operator|.
name|in
argument_list|(
name|Scopes
operator|.
name|SINGLETON
argument_list|)
expr_stmt|;
block|}
name|QueryParsersProcessor
operator|.
name|XContentQueryParsersBindings
name|xContentQueryParsersBindings
init|=
operator|new
name|QueryParsersProcessor
operator|.
name|XContentQueryParsersBindings
argument_list|(
name|queryBinder
argument_list|,
name|xContentQueryParserGroups
argument_list|)
decl_stmt|;
for|for
control|(
name|QueryParsersProcessor
name|processor
range|:
name|processors
control|)
block|{
name|processor
operator|.
name|processXContentQueryParsers
argument_list|(
name|xContentQueryParsersBindings
argument_list|)
expr_stmt|;
block|}
comment|// handle XContentFilterParsers
name|MapBinder
argument_list|<
name|String
argument_list|,
name|XContentFilterParserFactory
argument_list|>
name|filterBinder
init|=
name|MapBinder
operator|.
name|newMapBinder
argument_list|(
name|binder
argument_list|()
argument_list|,
name|String
operator|.
name|class
argument_list|,
name|XContentFilterParserFactory
operator|.
name|class
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Settings
argument_list|>
name|xContentFilterParserGroups
init|=
name|settings
operator|.
name|getGroups
argument_list|(
name|XContentIndexQueryParser
operator|.
name|Defaults
operator|.
name|FILTER_PREFIX
argument_list|)
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Settings
argument_list|>
name|entry
range|:
name|xContentFilterParserGroups
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|fName
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|Settings
name|fSettings
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|Class
argument_list|<
name|?
extends|extends
name|XContentFilterParser
argument_list|>
name|type
init|=
name|fSettings
operator|.
name|getAsClass
argument_list|(
literal|"type"
argument_list|,
literal|null
argument_list|)
decl_stmt|;
if|if
condition|(
name|type
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Filter Parser ["
operator|+
name|fName
operator|+
literal|"] must be provided with a type"
argument_list|)
throw|;
block|}
name|filterBinder
operator|.
name|addBinding
argument_list|(
name|fName
argument_list|)
operator|.
name|toProvider
argument_list|(
name|FactoryProvider
operator|.
name|newFactory
argument_list|(
name|XContentFilterParserFactory
operator|.
name|class
argument_list|,
name|fSettings
operator|.
name|getAsClass
argument_list|(
literal|"type"
argument_list|,
literal|null
argument_list|)
argument_list|)
argument_list|)
operator|.
name|in
argument_list|(
name|Scopes
operator|.
name|SINGLETON
argument_list|)
expr_stmt|;
block|}
name|QueryParsersProcessor
operator|.
name|XContentFilterParsersBindings
name|xContentFilterParsersBindings
init|=
operator|new
name|QueryParsersProcessor
operator|.
name|XContentFilterParsersBindings
argument_list|(
name|filterBinder
argument_list|,
name|xContentFilterParserGroups
argument_list|)
decl_stmt|;
for|for
control|(
name|QueryParsersProcessor
name|processor
range|:
name|processors
control|)
block|{
name|processor
operator|.
name|processXContentFilterParsers
argument_list|(
name|xContentFilterParsersBindings
argument_list|)
expr_stmt|;
block|}
name|bind
argument_list|(
name|IndexQueryParserService
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
block|}
DECL|class|DefaultQueryProcessors
specifier|private
specifier|static
class|class
name|DefaultQueryProcessors
extends|extends
name|QueryParsersProcessor
block|{
DECL|method|processXContentQueryParsers
annotation|@
name|Override
specifier|public
name|void
name|processXContentQueryParsers
parameter_list|(
name|XContentQueryParsersBindings
name|bindings
parameter_list|)
block|{
name|bindings
operator|.
name|processXContentQueryParser
argument_list|(
name|TextQueryParser
operator|.
name|NAME
argument_list|,
name|TextQueryParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|bindings
operator|.
name|processXContentQueryParser
argument_list|(
name|HasChildQueryParser
operator|.
name|NAME
argument_list|,
name|HasChildQueryParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|bindings
operator|.
name|processXContentQueryParser
argument_list|(
name|TopChildrenQueryParser
operator|.
name|NAME
argument_list|,
name|TopChildrenQueryParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|bindings
operator|.
name|processXContentQueryParser
argument_list|(
name|DisMaxQueryParser
operator|.
name|NAME
argument_list|,
name|DisMaxQueryParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|bindings
operator|.
name|processXContentQueryParser
argument_list|(
name|IdsQueryParser
operator|.
name|NAME
argument_list|,
name|IdsQueryParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|bindings
operator|.
name|processXContentQueryParser
argument_list|(
name|MatchAllQueryParser
operator|.
name|NAME
argument_list|,
name|MatchAllQueryParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|bindings
operator|.
name|processXContentQueryParser
argument_list|(
name|QueryStringQueryParser
operator|.
name|NAME
argument_list|,
name|QueryStringQueryParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|bindings
operator|.
name|processXContentQueryParser
argument_list|(
name|BoostingQueryParser
operator|.
name|NAME
argument_list|,
name|BoostingQueryParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|bindings
operator|.
name|processXContentQueryParser
argument_list|(
name|BoolQueryParser
operator|.
name|NAME
argument_list|,
name|BoolQueryParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|bindings
operator|.
name|processXContentQueryParser
argument_list|(
name|TermQueryParser
operator|.
name|NAME
argument_list|,
name|TermQueryParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|bindings
operator|.
name|processXContentQueryParser
argument_list|(
name|TermsQueryParser
operator|.
name|NAME
argument_list|,
name|TermsQueryParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|bindings
operator|.
name|processXContentQueryParser
argument_list|(
name|FuzzyQueryParser
operator|.
name|NAME
argument_list|,
name|FuzzyQueryParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|bindings
operator|.
name|processXContentQueryParser
argument_list|(
name|FieldQueryParser
operator|.
name|NAME
argument_list|,
name|FieldQueryParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|bindings
operator|.
name|processXContentQueryParser
argument_list|(
name|RangeQueryParser
operator|.
name|NAME
argument_list|,
name|RangeQueryParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|bindings
operator|.
name|processXContentQueryParser
argument_list|(
name|PrefixQueryParser
operator|.
name|NAME
argument_list|,
name|PrefixQueryParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|bindings
operator|.
name|processXContentQueryParser
argument_list|(
name|WildcardQueryParser
operator|.
name|NAME
argument_list|,
name|WildcardQueryParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|bindings
operator|.
name|processXContentQueryParser
argument_list|(
name|FilteredQueryParser
operator|.
name|NAME
argument_list|,
name|FilteredQueryParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|bindings
operator|.
name|processXContentQueryParser
argument_list|(
name|ConstantScoreQueryParser
operator|.
name|NAME
argument_list|,
name|ConstantScoreQueryParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|bindings
operator|.
name|processXContentQueryParser
argument_list|(
name|CustomBoostFactorQueryParser
operator|.
name|NAME
argument_list|,
name|CustomBoostFactorQueryParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|bindings
operator|.
name|processXContentQueryParser
argument_list|(
name|CustomScoreQueryParser
operator|.
name|NAME
argument_list|,
name|CustomScoreQueryParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|bindings
operator|.
name|processXContentQueryParser
argument_list|(
name|SpanTermQueryParser
operator|.
name|NAME
argument_list|,
name|SpanTermQueryParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|bindings
operator|.
name|processXContentQueryParser
argument_list|(
name|SpanNotQueryParser
operator|.
name|NAME
argument_list|,
name|SpanNotQueryParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|bindings
operator|.
name|processXContentQueryParser
argument_list|(
name|SpanFirstQueryParser
operator|.
name|NAME
argument_list|,
name|SpanFirstQueryParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|bindings
operator|.
name|processXContentQueryParser
argument_list|(
name|SpanNearQueryParser
operator|.
name|NAME
argument_list|,
name|SpanNearQueryParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|bindings
operator|.
name|processXContentQueryParser
argument_list|(
name|SpanOrQueryParser
operator|.
name|NAME
argument_list|,
name|SpanOrQueryParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|bindings
operator|.
name|processXContentQueryParser
argument_list|(
name|MoreLikeThisQueryParser
operator|.
name|NAME
argument_list|,
name|MoreLikeThisQueryParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|bindings
operator|.
name|processXContentQueryParser
argument_list|(
name|MoreLikeThisFieldQueryParser
operator|.
name|NAME
argument_list|,
name|MoreLikeThisFieldQueryParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|bindings
operator|.
name|processXContentQueryParser
argument_list|(
name|FuzzyLikeThisQueryParser
operator|.
name|NAME
argument_list|,
name|FuzzyLikeThisQueryParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|bindings
operator|.
name|processXContentQueryParser
argument_list|(
name|FuzzyLikeThisFieldQueryParser
operator|.
name|NAME
argument_list|,
name|FuzzyLikeThisFieldQueryParser
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
DECL|method|processXContentFilterParsers
annotation|@
name|Override
specifier|public
name|void
name|processXContentFilterParsers
parameter_list|(
name|XContentFilterParsersBindings
name|bindings
parameter_list|)
block|{
name|bindings
operator|.
name|processXContentQueryFilter
argument_list|(
name|HasChildFilterParser
operator|.
name|NAME
argument_list|,
name|HasChildFilterParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|bindings
operator|.
name|processXContentQueryFilter
argument_list|(
name|TypeFilterParser
operator|.
name|NAME
argument_list|,
name|TypeFilterParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|bindings
operator|.
name|processXContentQueryFilter
argument_list|(
name|IdsFilterParser
operator|.
name|NAME
argument_list|,
name|IdsFilterParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|bindings
operator|.
name|processXContentQueryFilter
argument_list|(
name|TermFilterParser
operator|.
name|NAME
argument_list|,
name|TermFilterParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|bindings
operator|.
name|processXContentQueryFilter
argument_list|(
name|TermsFilterParser
operator|.
name|NAME
argument_list|,
name|TermsFilterParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|bindings
operator|.
name|processXContentQueryFilter
argument_list|(
name|RangeFilterParser
operator|.
name|NAME
argument_list|,
name|RangeFilterParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|bindings
operator|.
name|processXContentQueryFilter
argument_list|(
name|NumericRangeFilterParser
operator|.
name|NAME
argument_list|,
name|NumericRangeFilterParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|bindings
operator|.
name|processXContentQueryFilter
argument_list|(
name|PrefixFilterParser
operator|.
name|NAME
argument_list|,
name|PrefixFilterParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|bindings
operator|.
name|processXContentQueryFilter
argument_list|(
name|ScriptFilterParser
operator|.
name|NAME
argument_list|,
name|ScriptFilterParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|bindings
operator|.
name|processXContentQueryFilter
argument_list|(
name|GeoDistanceFilterParser
operator|.
name|NAME
argument_list|,
name|GeoDistanceFilterParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|bindings
operator|.
name|processXContentQueryFilter
argument_list|(
name|GeoDistanceRangeFilterParser
operator|.
name|NAME
argument_list|,
name|GeoDistanceRangeFilterParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|bindings
operator|.
name|processXContentQueryFilter
argument_list|(
name|GeoBoundingBoxFilterParser
operator|.
name|NAME
argument_list|,
name|GeoBoundingBoxFilterParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|bindings
operator|.
name|processXContentQueryFilter
argument_list|(
name|GeoPolygonFilterParser
operator|.
name|NAME
argument_list|,
name|GeoPolygonFilterParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|bindings
operator|.
name|processXContentQueryFilter
argument_list|(
name|QueryFilterParser
operator|.
name|NAME
argument_list|,
name|QueryFilterParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|bindings
operator|.
name|processXContentQueryFilter
argument_list|(
name|FQueryFilterParser
operator|.
name|NAME
argument_list|,
name|FQueryFilterParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|bindings
operator|.
name|processXContentQueryFilter
argument_list|(
name|BoolFilterParser
operator|.
name|NAME
argument_list|,
name|BoolFilterParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|bindings
operator|.
name|processXContentQueryFilter
argument_list|(
name|AndFilterParser
operator|.
name|NAME
argument_list|,
name|AndFilterParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|bindings
operator|.
name|processXContentQueryFilter
argument_list|(
name|OrFilterParser
operator|.
name|NAME
argument_list|,
name|OrFilterParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|bindings
operator|.
name|processXContentQueryFilter
argument_list|(
name|NotFilterParser
operator|.
name|NAME
argument_list|,
name|NotFilterParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|bindings
operator|.
name|processXContentQueryFilter
argument_list|(
name|MatchAllFilterParser
operator|.
name|NAME
argument_list|,
name|MatchAllFilterParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|bindings
operator|.
name|processXContentQueryFilter
argument_list|(
name|ExistsFilterParser
operator|.
name|NAME
argument_list|,
name|ExistsFilterParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|bindings
operator|.
name|processXContentQueryFilter
argument_list|(
name|MissingFilterParser
operator|.
name|NAME
argument_list|,
name|MissingFilterParser
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

