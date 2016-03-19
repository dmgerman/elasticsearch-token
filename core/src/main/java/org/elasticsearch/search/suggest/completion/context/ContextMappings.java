begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.suggest.completion.context
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|suggest
operator|.
name|completion
operator|.
name|context
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
name|suggest
operator|.
name|document
operator|.
name|CompletionQuery
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
name|suggest
operator|.
name|document
operator|.
name|ContextQuery
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
name|suggest
operator|.
name|document
operator|.
name|ContextSuggestField
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
name|CharsRefBuilder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchParseException
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
name|index
operator|.
name|mapper
operator|.
name|DocumentMapperParser
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
name|ParseContext
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
name|core
operator|.
name|CompletionFieldMapper
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
name|ArrayList
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
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
name|Objects
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

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|suggest
operator|.
name|completion
operator|.
name|context
operator|.
name|ContextMapping
operator|.
name|FIELD_NAME
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|suggest
operator|.
name|completion
operator|.
name|context
operator|.
name|ContextMapping
operator|.
name|FIELD_TYPE
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|suggest
operator|.
name|completion
operator|.
name|context
operator|.
name|ContextMapping
operator|.
name|Type
import|;
end_import

begin_comment
comment|/**  * ContextMappings indexes context-enabled suggestion fields  * and creates context queries for defined {@link ContextMapping}s  * for a {@link CompletionFieldMapper}  */
end_comment

begin_class
DECL|class|ContextMappings
specifier|public
class|class
name|ContextMappings
implements|implements
name|ToXContent
block|{
DECL|field|contextMappings
specifier|private
specifier|final
name|List
argument_list|<
name|ContextMapping
argument_list|>
name|contextMappings
decl_stmt|;
DECL|field|contextNameMap
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|ContextMapping
argument_list|>
name|contextNameMap
decl_stmt|;
DECL|method|ContextMappings
specifier|public
name|ContextMappings
parameter_list|(
name|List
argument_list|<
name|ContextMapping
argument_list|>
name|contextMappings
parameter_list|)
block|{
if|if
condition|(
name|contextMappings
operator|.
name|size
argument_list|()
operator|>
literal|255
condition|)
block|{
comment|// we can support more, but max of 255 (1 byte) unique context types per suggest field
comment|// seems reasonable?
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Maximum of 10 context types are supported was: "
operator|+
name|contextMappings
operator|.
name|size
argument_list|()
argument_list|)
throw|;
block|}
name|this
operator|.
name|contextMappings
operator|=
name|contextMappings
expr_stmt|;
name|contextNameMap
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|(
name|contextMappings
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|ContextMapping
name|mapping
range|:
name|contextMappings
control|)
block|{
name|contextNameMap
operator|.
name|put
argument_list|(
name|mapping
operator|.
name|name
argument_list|()
argument_list|,
name|mapping
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      * @return number of context mappings      * held by this instance      */
DECL|method|size
specifier|public
name|int
name|size
parameter_list|()
block|{
return|return
name|contextMappings
operator|.
name|size
argument_list|()
return|;
block|}
comment|/**      * Returns a context mapping by its name      */
DECL|method|get
specifier|public
name|ContextMapping
name|get
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|ContextMapping
name|contextMapping
init|=
name|contextNameMap
operator|.
name|get
argument_list|(
name|name
argument_list|)
decl_stmt|;
if|if
condition|(
name|contextMapping
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Unknown context name["
operator|+
name|name
operator|+
literal|"], must be one of "
operator|+
name|contextNameMap
operator|.
name|size
argument_list|()
argument_list|)
throw|;
block|}
return|return
name|contextMapping
return|;
block|}
comment|/**      * Adds a context-enabled field for all the defined mappings to<code>document</code>      * see {@link org.elasticsearch.search.suggest.completion.context.ContextMappings.TypedContextField}      */
DECL|method|addField
specifier|public
name|void
name|addField
parameter_list|(
name|ParseContext
operator|.
name|Document
name|document
parameter_list|,
name|String
name|name
parameter_list|,
name|String
name|input
parameter_list|,
name|int
name|weight
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Set
argument_list|<
name|CharSequence
argument_list|>
argument_list|>
name|contexts
parameter_list|)
block|{
name|document
operator|.
name|add
argument_list|(
operator|new
name|TypedContextField
argument_list|(
name|name
argument_list|,
name|input
argument_list|,
name|weight
argument_list|,
name|contexts
argument_list|,
name|document
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**      * Field prepends context values with a suggestion      * Context values are associated with a type, denoted by      * a type id, which is prepended to the context value.      *      * Every defined context mapping yields a unique type id (index of the      * corresponding context mapping in the context mappings list)      * for all its context values      *      * The type, context and suggestion values are encoded as follows:      *<p>      *     TYPE_ID | CONTEXT_VALUE | CONTEXT_SEP | SUGGESTION_VALUE      *</p>      *      * Field can also use values of other indexed fields as contexts      * at index time      */
DECL|class|TypedContextField
specifier|private
class|class
name|TypedContextField
extends|extends
name|ContextSuggestField
block|{
DECL|field|contexts
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Set
argument_list|<
name|CharSequence
argument_list|>
argument_list|>
name|contexts
decl_stmt|;
DECL|field|document
specifier|private
specifier|final
name|ParseContext
operator|.
name|Document
name|document
decl_stmt|;
DECL|method|TypedContextField
specifier|public
name|TypedContextField
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|value
parameter_list|,
name|int
name|weight
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Set
argument_list|<
name|CharSequence
argument_list|>
argument_list|>
name|contexts
parameter_list|,
name|ParseContext
operator|.
name|Document
name|document
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|,
name|value
argument_list|,
name|weight
argument_list|)
expr_stmt|;
name|this
operator|.
name|contexts
operator|=
name|contexts
expr_stmt|;
name|this
operator|.
name|document
operator|=
name|document
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|contexts
specifier|protected
name|Iterable
argument_list|<
name|CharSequence
argument_list|>
name|contexts
parameter_list|()
block|{
name|Set
argument_list|<
name|CharSequence
argument_list|>
name|typedContexts
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
specifier|final
name|CharsRefBuilder
name|scratch
init|=
operator|new
name|CharsRefBuilder
argument_list|()
decl_stmt|;
name|scratch
operator|.
name|grow
argument_list|(
literal|1
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|typeId
init|=
literal|0
init|;
name|typeId
operator|<
name|contextMappings
operator|.
name|size
argument_list|()
condition|;
name|typeId
operator|++
control|)
block|{
name|scratch
operator|.
name|setCharAt
argument_list|(
literal|0
argument_list|,
operator|(
name|char
operator|)
name|typeId
argument_list|)
expr_stmt|;
name|scratch
operator|.
name|setLength
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|ContextMapping
name|mapping
init|=
name|contextMappings
operator|.
name|get
argument_list|(
name|typeId
argument_list|)
decl_stmt|;
name|Set
argument_list|<
name|CharSequence
argument_list|>
name|contexts
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|mapping
operator|.
name|parseContext
argument_list|(
name|document
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|this
operator|.
name|contexts
operator|.
name|get
argument_list|(
name|mapping
operator|.
name|name
argument_list|()
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|contexts
operator|.
name|addAll
argument_list|(
name|this
operator|.
name|contexts
operator|.
name|get
argument_list|(
name|mapping
operator|.
name|name
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|CharSequence
name|context
range|:
name|contexts
control|)
block|{
name|scratch
operator|.
name|append
argument_list|(
name|context
argument_list|)
expr_stmt|;
name|typedContexts
operator|.
name|add
argument_list|(
name|scratch
operator|.
name|toCharsRef
argument_list|()
argument_list|)
expr_stmt|;
name|scratch
operator|.
name|setLength
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|typedContexts
return|;
block|}
block|}
comment|/**      * Wraps a {@link CompletionQuery} with context queries      *      * @param query base completion query to wrap      * @param queryContexts a map of context mapping name and collected query contexts      * @return a context-enabled query      */
DECL|method|toContextQuery
specifier|public
name|ContextQuery
name|toContextQuery
parameter_list|(
name|CompletionQuery
name|query
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|ContextMapping
operator|.
name|InternalQueryContext
argument_list|>
argument_list|>
name|queryContexts
parameter_list|)
block|{
name|ContextQuery
name|typedContextQuery
init|=
operator|new
name|ContextQuery
argument_list|(
name|query
argument_list|)
decl_stmt|;
if|if
condition|(
name|queryContexts
operator|.
name|isEmpty
argument_list|()
operator|==
literal|false
condition|)
block|{
name|CharsRefBuilder
name|scratch
init|=
operator|new
name|CharsRefBuilder
argument_list|()
decl_stmt|;
name|scratch
operator|.
name|grow
argument_list|(
literal|1
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|typeId
init|=
literal|0
init|;
name|typeId
operator|<
name|contextMappings
operator|.
name|size
argument_list|()
condition|;
name|typeId
operator|++
control|)
block|{
name|scratch
operator|.
name|setCharAt
argument_list|(
literal|0
argument_list|,
operator|(
name|char
operator|)
name|typeId
argument_list|)
expr_stmt|;
name|scratch
operator|.
name|setLength
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|ContextMapping
name|mapping
init|=
name|contextMappings
operator|.
name|get
argument_list|(
name|typeId
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|ContextMapping
operator|.
name|InternalQueryContext
argument_list|>
name|internalQueryContext
init|=
name|queryContexts
operator|.
name|get
argument_list|(
name|mapping
operator|.
name|name
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|internalQueryContext
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|ContextMapping
operator|.
name|InternalQueryContext
name|context
range|:
name|internalQueryContext
control|)
block|{
name|scratch
operator|.
name|append
argument_list|(
name|context
operator|.
name|context
argument_list|)
expr_stmt|;
name|typedContextQuery
operator|.
name|addContext
argument_list|(
name|scratch
operator|.
name|toCharsRef
argument_list|()
argument_list|,
name|context
operator|.
name|boost
argument_list|,
operator|!
name|context
operator|.
name|isPrefix
argument_list|)
expr_stmt|;
name|scratch
operator|.
name|setLength
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
return|return
name|typedContextQuery
return|;
block|}
comment|/**      * Maps an output context list to a map of context mapping names and their values      *      * see {@link org.elasticsearch.search.suggest.completion.context.ContextMappings.TypedContextField}      * @return a map of context names and their values      *      */
DECL|method|getNamedContexts
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|Set
argument_list|<
name|CharSequence
argument_list|>
argument_list|>
name|getNamedContexts
parameter_list|(
name|List
argument_list|<
name|CharSequence
argument_list|>
name|contexts
parameter_list|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Set
argument_list|<
name|CharSequence
argument_list|>
argument_list|>
name|contextMap
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|(
name|contexts
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|CharSequence
name|typedContext
range|:
name|contexts
control|)
block|{
name|int
name|typeId
init|=
name|typedContext
operator|.
name|charAt
argument_list|(
literal|0
argument_list|)
decl_stmt|;
assert|assert
name|typeId
operator|<
name|contextMappings
operator|.
name|size
argument_list|()
operator|:
literal|"Returned context has invalid type"
assert|;
name|ContextMapping
name|mapping
init|=
name|contextMappings
operator|.
name|get
argument_list|(
name|typeId
argument_list|)
decl_stmt|;
name|Set
argument_list|<
name|CharSequence
argument_list|>
name|contextEntries
init|=
name|contextMap
operator|.
name|get
argument_list|(
name|mapping
operator|.
name|name
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|contextEntries
operator|==
literal|null
condition|)
block|{
name|contextEntries
operator|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
expr_stmt|;
name|contextMap
operator|.
name|put
argument_list|(
name|mapping
operator|.
name|name
argument_list|()
argument_list|,
name|contextEntries
argument_list|)
expr_stmt|;
block|}
name|contextEntries
operator|.
name|add
argument_list|(
name|typedContext
operator|.
name|subSequence
argument_list|(
literal|1
argument_list|,
name|typedContext
operator|.
name|length
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|contextMap
return|;
block|}
comment|/**      * Loads {@link ContextMappings} from configuration      *      * Expected configuration:      *  List of maps representing {@link ContextMapping}      *  [{"name": .., "type": .., ..}, {..}]      *      */
DECL|method|load
specifier|public
specifier|static
name|ContextMappings
name|load
parameter_list|(
name|Object
name|configuration
parameter_list|,
name|Version
name|indexVersionCreated
parameter_list|)
throws|throws
name|ElasticsearchParseException
block|{
specifier|final
name|List
argument_list|<
name|ContextMapping
argument_list|>
name|contextMappings
decl_stmt|;
if|if
condition|(
name|configuration
operator|instanceof
name|List
condition|)
block|{
name|contextMappings
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
name|List
argument_list|<
name|Object
argument_list|>
name|configurations
init|=
operator|(
name|List
argument_list|<
name|Object
argument_list|>
operator|)
name|configuration
decl_stmt|;
for|for
control|(
name|Object
name|contextConfig
range|:
name|configurations
control|)
block|{
name|contextMappings
operator|.
name|add
argument_list|(
name|load
argument_list|(
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|contextConfig
argument_list|,
name|indexVersionCreated
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|contextMappings
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
throw|throw
operator|new
name|ElasticsearchParseException
argument_list|(
literal|"expected at least one context mapping"
argument_list|)
throw|;
block|}
block|}
elseif|else
if|if
condition|(
name|configuration
operator|instanceof
name|Map
condition|)
block|{
name|contextMappings
operator|=
name|Collections
operator|.
name|singletonList
argument_list|(
name|load
argument_list|(
operator|(
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|configuration
operator|)
argument_list|,
name|indexVersionCreated
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|ElasticsearchParseException
argument_list|(
literal|"expected a list or an entry of context mapping"
argument_list|)
throw|;
block|}
return|return
operator|new
name|ContextMappings
argument_list|(
name|contextMappings
argument_list|)
return|;
block|}
DECL|method|load
specifier|private
specifier|static
name|ContextMapping
name|load
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|contextConfig
parameter_list|,
name|Version
name|indexVersionCreated
parameter_list|)
block|{
name|String
name|name
init|=
name|extractRequiredValue
argument_list|(
name|contextConfig
argument_list|,
name|FIELD_NAME
argument_list|)
decl_stmt|;
name|String
name|type
init|=
name|extractRequiredValue
argument_list|(
name|contextConfig
argument_list|,
name|FIELD_TYPE
argument_list|)
decl_stmt|;
specifier|final
name|ContextMapping
name|contextMapping
decl_stmt|;
switch|switch
condition|(
name|Type
operator|.
name|fromString
argument_list|(
name|type
argument_list|)
condition|)
block|{
case|case
name|CATEGORY
case|:
name|contextMapping
operator|=
name|CategoryContextMapping
operator|.
name|load
argument_list|(
name|name
argument_list|,
name|contextConfig
argument_list|)
expr_stmt|;
break|break;
case|case
name|GEO
case|:
name|contextMapping
operator|=
name|GeoContextMapping
operator|.
name|load
argument_list|(
name|name
argument_list|,
name|contextConfig
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|ElasticsearchParseException
argument_list|(
literal|"unknown context type["
operator|+
name|type
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|DocumentMapperParser
operator|.
name|checkNoRemainingFields
argument_list|(
name|name
argument_list|,
name|contextConfig
argument_list|,
name|indexVersionCreated
argument_list|)
expr_stmt|;
return|return
name|contextMapping
return|;
block|}
DECL|method|extractRequiredValue
specifier|private
specifier|static
name|String
name|extractRequiredValue
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|contextConfig
parameter_list|,
name|String
name|paramName
parameter_list|)
block|{
specifier|final
name|Object
name|paramValue
init|=
name|contextConfig
operator|.
name|get
argument_list|(
name|paramName
argument_list|)
decl_stmt|;
if|if
condition|(
name|paramValue
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ElasticsearchParseException
argument_list|(
literal|"missing ["
operator|+
name|paramName
operator|+
literal|"] in context mapping"
argument_list|)
throw|;
block|}
name|contextConfig
operator|.
name|remove
argument_list|(
name|paramName
argument_list|)
expr_stmt|;
return|return
name|paramValue
operator|.
name|toString
argument_list|()
return|;
block|}
comment|/**      * Writes a list of objects specified by the defined {@link ContextMapping}s      *      * see {@link ContextMapping#toXContent(XContentBuilder, Params)}      */
annotation|@
name|Override
DECL|method|toXContent
specifier|public
name|XContentBuilder
name|toXContent
parameter_list|(
name|XContentBuilder
name|builder
parameter_list|,
name|Params
name|params
parameter_list|)
throws|throws
name|IOException
block|{
for|for
control|(
name|ContextMapping
name|contextMapping
range|:
name|contextMappings
control|)
block|{
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|contextMapping
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
return|return
name|builder
return|;
block|}
annotation|@
name|Override
DECL|method|hashCode
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|Objects
operator|.
name|hash
argument_list|(
name|contextMappings
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|equals
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
if|if
condition|(
name|obj
operator|==
literal|null
operator|||
operator|(
name|obj
operator|instanceof
name|ContextMappings
operator|)
operator|==
literal|false
condition|)
block|{
return|return
literal|false
return|;
block|}
name|ContextMappings
name|other
init|=
operator|(
operator|(
name|ContextMappings
operator|)
name|obj
operator|)
decl_stmt|;
return|return
name|contextMappings
operator|.
name|equals
argument_list|(
name|other
operator|.
name|contextMappings
argument_list|)
return|;
block|}
block|}
end_class

end_unit

